/**
 * Copyright 2014 Tampere University of Technology, Pori Department
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package service.tut.pori.users.google;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationListener;

import service.tut.pori.users.UserCore;
import service.tut.pori.users.UserCore.Registration;
import service.tut.pori.users.UserCore.RegistrationStatus;
import service.tut.pori.users.UserServiceEvent;
import service.tut.pori.users.google.Definitions.OAuth2GrantType;
import core.tut.pori.context.ServiceInitializer;
import core.tut.pori.http.RedirectResponse;
import core.tut.pori.http.Response;
import core.tut.pori.http.Response.Status;
import core.tut.pori.users.ExternalAccountConnection.UserServiceType;
import core.tut.pori.users.UserEvent.EventType;
import core.tut.pori.users.ExternalAccountConnection;
import core.tut.pori.users.UserEvent;
import core.tut.pori.users.UserIdentity;

/**
 * Google User core methods.
 * 
 * Google scope apis:
 * - https://gist.github.com/comewalk/5457791
 * - http://www.subinsb.com/2013/04/list-google-oauth-scopes.html
 * 
 * Implemented according to: https://developers.google.com/accounts/docs/OAuth2WebServer
 * 
 * This requires that matching (valid) parameters have been set in the system properties file,
 * and in the Google API Console.
 * 
 * This class emits events of type {@link core.tut.pori.users.UserEvent} for user account modifications with one of the listed {@link core.tut.pori.users.UserEvent.EventType} :
 * <ul>
 *  <li>{@link core.tut.pori.users.UserEvent.EventType#USER_AUTHORIZATION_GIVEN} for new user account authorizations.</li>
 *  <li>{@link core.tut.pori.users.UserEvent.EventType#USER_AUTHORIZATION_REVOKED} for removed user account authorizations.</li>
 * </ul>
 */
public final class GoogleUserCore {
	private static final Logger LOGGER = Logger.getLogger(GoogleUserCore.class);
	private static final String PARAMETER_VALUE_ACCESS_TYPE = "offline";
	private static final String PARAMETER_VALUE_APPROVAL_PROMPT = "force";
	private static final String PARAMETER_VALUE_RESPONSE_TYPE = "code";

	/**
	 * 
	 */
	private GoogleUserCore(){
		// nothing needed
	}

	/**
	 * 
	 * @param authorizationCode
	 * @param errorCode
	 * @param nonce
	 * @return response
	 */
	public static Response processOAuth2Callback(String authorizationCode, String errorCode, String nonce){
		if(StringUtils.isBlank(nonce)){
			LOGGER.debug("nonce is missing.");
			return new Response(Status.BAD_REQUEST);
		}

		GoogleUserDAO dao = ServiceInitializer.getDAOHandler().getSQLDAO(GoogleUserDAO.class);

		if(!StringUtils.isEmpty(errorCode)){
			dao.removeNonce(nonce);	// in any case, do not allow to use this nonce again
			LOGGER.debug("Received callback request with errorCode: "+errorCode);
			return new Response(Status.OK, errorCode);
		}

		UserIdentity userId = dao.getUser(nonce);
		if(userId == null){	// the nonce do not exist or has expired
			LOGGER.debug("Received callback request with expired or invalid nonce: "+nonce);
			return new Response(Status.BAD_REQUEST);
		}

		dao.removeNonce(nonce);	// in any case, do not allow to use this nonce again

		if(StringUtils.isBlank(authorizationCode)){
			LOGGER.debug("no authorization code.");
			return new Response(Status.BAD_REQUEST);
		}

		try (CloseableHttpClient client = HttpClients.createDefault()) {
			GoogleProperties gp = ServiceInitializer.getPropertyHandler().getSystemProperties(GoogleProperties.class);

			List<BasicNameValuePair> params = new ArrayList<>();
			params.add(new BasicNameValuePair(Definitions.PARAMETER_OAUTH2_AUTHORIZATION_CODE, authorizationCode));
			params.add(new BasicNameValuePair(Definitions.PARAMETER_OAUTH2_CLIENT_ID, gp.getClientId()));
			params.add(new BasicNameValuePair(Definitions.PARAMETER_OAUTH2_CLIENT_SECRET, gp.getClientSecret()));
			params.add(new BasicNameValuePair(Definitions.PARAMETER_OAUTH2_GRANT_TYPE, OAuth2GrantType.authorization_code.toOAuth2GrantType()));
			params.add(new BasicNameValuePair(Definitions.PARAMETER_OAUTH2_REDIRECT_URI, gp.getoAuth2RedirectUri()));

			HttpPost post = new HttpPost(gp.getoAuth2Uri()+Definitions.METHOD_GOOGLE_TOKEN);
			post.setEntity(new UrlEncodedFormEntity(params));

			OAuth2Token newToken = (OAuth2Token.getTokenGSONSerializer()).fromJson(client.execute(post, new BasicResponseHandler()), OAuth2Token.class);
			if(newToken != null && newToken.isValid()){
				GoogleCredential gc = getCredential(newToken.getAccessToken());
				if(gc == null){
					LOGGER.error("Failed to resolve credentials for the new token.");
					return new Response(Status.INTERNAL_SERVER_ERROR);
				}
				
				if(!dao.setToken(gc.getId(), newToken, userId)){
					LOGGER.warn("Failed to set new token.");
					return new Response(Status.BAD_REQUEST);
				}
				
				ServiceInitializer.getEventHandler().publishEvent(new UserEvent(GoogleUserCore.class, userId, EventType.USER_AUTHORIZATION_GIVEN));
			}else{
				LOGGER.debug("Did not receive a valid token.");
			}
		} catch (IOException ex) {
			LOGGER.error(ex, ex);
		}

		return new Response();
	}

	/**
	 * Return token for the given userId, refreshing the old token if necessary
	 * 
	 * Note: if the token expires, you should use this method to retrieve a new one, refreshing the token
	 * manually may cause race condition with other services using the tokens (there can be only one active
	 * and valid access token at any time)
	 * 
	 * @param authorizedUser
	 * @return the token or null if none
	 */
	public static OAuth2Token getToken(UserIdentity authorizedUser){
		GoogleUserDAO dao = ServiceInitializer.getDAOHandler().getSQLDAO(GoogleUserDAO.class);
		OAuth2Token token = dao.getToken(authorizedUser);
		if(token == null){
			return null;
		}
		String googleUserId = dao.getGoogleUserId(authorizedUser);
		if(googleUserId == null){
			LOGGER.warn("Failed to resolve google user id.");
			return null;
		}

		if(token.isExpired()){
			token = refreshToken(token);
			if(token != null){
				dao.setToken(googleUserId, token, authorizedUser);
			}else{
				dao.removeToken(authorizedUser);
			}
		}
		return token;
	}

	/**
	 * 
	 * @param token
	 * @return the refreshed token or null on failure, Note: this is not the same token as was passed as a parameter
	 */
	private static OAuth2Token refreshToken(OAuth2Token token){
		String refreshToken = token.getRefreshToken();
		if(refreshToken == null){
			LOGGER.debug("No refresh_token provided.");
			return null;
		}

		try (CloseableHttpClient client = HttpClients.createDefault()) {
			GoogleProperties gp = ServiceInitializer.getPropertyHandler().getSystemProperties(GoogleProperties.class);

			List<BasicNameValuePair> params = new ArrayList<>();
			params.add(new BasicNameValuePair(Definitions.PARAMETER_OAUTH2_CLIENT_ID, gp.getClientId()));
			params.add(new BasicNameValuePair(Definitions.PARAMETER_OAUTH2_CLIENT_SECRET, gp.getClientSecret()));
			params.add(new BasicNameValuePair(Definitions.PARAMETER_OAUTH2_GRANT_TYPE, OAuth2GrantType.refresh_token.toOAuth2GrantType()));
			params.add(new BasicNameValuePair(Definitions.PARAMETER_OAUTH2_REFRESH_TOKEN, refreshToken));

			HttpPost post = new HttpPost(gp.getoAuth2Uri()+Definitions.METHOD_GOOGLE_TOKEN);
			post.setEntity(new UrlEncodedFormEntity(params));

			OAuth2Token newToken = (OAuth2Token.getTokenGSONSerializer()).fromJson(client.execute(post, new BasicResponseHandler()), OAuth2Token.class);
			if(!newToken.isExpired()){
				newToken.setRefreshToken(refreshToken);	// set the used refresh token to the new token
				return newToken;
			}
		} catch (IOException ex) {
			LOGGER.error(ex, ex);
		}
		return null;
	}

	/**
	 * revokes the access_token and refresh_token if any are present and sets the member values to null (on success)
	 * 
	 * This will not remove the token from the database, use the overloaded version if for that.
	 * 
	 * @param token
	 */
	private static void revokeToken(OAuth2Token token){
		if(token == null){
			LOGGER.debug("null token.");
			return;
		}
		String accessToken = token.getAccessToken();
		String refreshToken = token.getRefreshToken();
		if(accessToken == null && refreshToken == null){
			LOGGER.debug("No token values.");
			return;
		}
		try (CloseableHttpClient client = HttpClients.createDefault()) {
			GoogleProperties gp = ServiceInitializer.getPropertyHandler().getSystemProperties(GoogleProperties.class);
			BasicResponseHandler h = new BasicResponseHandler();
			if(accessToken != null){
				LOGGER.debug("Server responded :"+client.execute(new HttpGet(gp.getoAuth2Uri()+Definitions.METHOD_GOOGLE_REVOKE+"?"+Definitions.PARAMETER_GOOGLE_TOKEN+"="+accessToken),h));	
			}
			token.setAccessToken(null);
			if(refreshToken != null){
				LOGGER.debug("Server responded :"+client.execute(new HttpGet(gp.getoAuth2Uri()+Definitions.METHOD_GOOGLE_REVOKE+"?"+Definitions.PARAMETER_GOOGLE_TOKEN+"="+refreshToken),h));
			}
			token.setRefreshToken(null);
		} catch (IOException ex) { // simply catch the exception, unfortunately we cannot do much if revoke fails
			LOGGER.error(ex, ex);
		}
	}

	/**
	 * 
	 * @param authorizedUser valid user id
	 * @return redirection response
	 */
	public static Response createAuthorizationRedirection(UserIdentity authorizedUser){
		GoogleProperties gp = ServiceInitializer.getPropertyHandler().getSystemProperties(GoogleProperties.class);
		StringBuilder uri = new StringBuilder(gp.getoAuth2Uri());
		uri.append(Definitions.METHOD_GOOGLE_AUTH+"?"+Definitions.PARAMETER_OAUTH2_SCOPE+"=");
		uri.append(gp.getoAuth2Scope());

		uri.append("&"+Definitions.PARAMETER_OAUTH2_STATE+"=");

		uri.append(ServiceInitializer.getDAOHandler().getSQLDAO(GoogleUserDAO.class).generateNonce(authorizedUser));

		uri.append("&"+Definitions.PARAMETER_OAUTH2_REDIRECT_URI+"=");
		uri.append(gp.getEncodedOAuth2RedirectUri());

		uri.append("&"+Definitions.PARAMETER_OAUTH2_CLIENT_ID+"=");
		uri.append(gp.getClientId());

		uri.append("&"+Definitions.PARAMETER_GOOGLE_ACCESS_TYPE+"="+PARAMETER_VALUE_ACCESS_TYPE+"&"+Definitions.PARAMETER_OAUTH2_RESPONSE_TYPE+"="+PARAMETER_VALUE_RESPONSE_TYPE+"&"+Definitions.PARAMETER_GOOGLE_APPROVAL_PROMPT+"="+PARAMETER_VALUE_APPROVAL_PROMPT);

		String redirectUri = uri.toString();
		LOGGER.debug("Redirecting authorization request to: "+redirectUri);
		return new RedirectResponse(redirectUri);
	}
	
	/**
	 * 
	 * @param userIdentity
	 * @return response
	 */
	public static Response removeAuthorization(UserIdentity userIdentity) {
		Response r = revoke(userIdentity);
		ServiceInitializer.getEventHandler().publishEvent(new UserEvent(GoogleUserCore.class, userIdentity, EventType.USER_AUTHORIZATION_REVOKED)); // send revoked event, this should trigger clean up on all relevant services
		return r;
	}

	/**
	 * 
	 * @param userId
	 * @return response
	 */
	public static Response revoke(UserIdentity userId) {
		if(!UserIdentity.isValid(userId)){	// should not be called with invalid userId
			LOGGER.warn("Invalid "+UserIdentity.class.toString());
			return new Response(Status.INTERNAL_SERVER_ERROR);
		}
		GoogleUserDAO dao = ServiceInitializer.getDAOHandler().getSQLDAO(GoogleUserDAO.class);

		OAuth2Token token = dao.getToken(userId);
		if(token == null){
			LOGGER.debug("No token found for user, id: "+userId.getUserId());
		}else{
			revokeToken(token);
			dao.removeToken(userId);
		}
		
		return new Response();
	}
	
	/**
	 * 
	 * @param userId
	 * @return GoogleCredential for the requested userId or null if none available
	 */
	public static GoogleCredential getCredential(UserIdentity userId){
		if(!UserIdentity.isValid(userId)){
			LOGGER.warn("Given userId was not valid.");
			return null;
		}

		OAuth2Token token = GoogleUserCore.getToken(userId);
		if(token == null){
			LOGGER.debug("User does not have valid Google credentials.");
			return null;
		}
		
		GoogleCredential credential = getCredential(token.getAccessToken());
		if(credential == null){
			LOGGER.debug("Failed to resolve credentials.");
		}else{
			credential.setUserId(userId);
		}
		return credential;
	}
	
	/**
	 * Helper method for retrieving credentials from google servers
	 * 
	 * @param accessToken
	 * @return credentials for the given token or null if none was found
	 */
	private static GoogleCredential getCredential(String accessToken){
		GoogleCredential credential = null;
		try(CloseableHttpClient client = HttpClients.createDefault()){
			GoogleProperties gp = ServiceInitializer.getPropertyHandler().getSystemProperties(GoogleProperties.class);
			credential = (OAuth2Token.getTokenGSONSerializer()).fromJson(client.execute(new HttpGet(gp.getoAuth2UserInfoUri()+Definitions.METHOD_GOOGLE_USER_INFO+"?alt=json&"+Definitions.PARAMETER_OAUTH2_ACCESS_TOKEN+"="+accessToken), new BasicResponseHandler()), GoogleCredential.class);
		} catch (IOException ex) {
			LOGGER.error(ex, ex);
		}
		
		return credential;
	}
	
	/**
	 * 
	 * @param session
	 * @param accessToken
	 * @return response
	 */
	public static Response login(HttpSession session, String accessToken) {
		GoogleCredential credential = getCredential(accessToken);
		if(credential == null){
			return new Response(Status.BAD_REQUEST, "Failed to process the given token.");
		}
		
		UserIdentity userId = UserCore.getUserId(new ExternalAccountConnection(credential.getId(), UserServiceType.GOOGLE));
		if(!UserIdentity.isValid(userId)){
			return new Response(Status.FORBIDDEN, "The given Google user is not registered with this service, please register before login.");
		}
		
		ServiceInitializer.getSessionHandler().registerAndAuthenticate(session.getId(), userId);
		return new Response();
	}

	/**
	 * 
	 * @param accessToken
	 * @return response
	 */
	public static Response register(String accessToken) {
		GoogleCredential credential = getCredential(accessToken);
		if(credential == null){
			return new Response(Status.BAD_REQUEST, "Failed to process the given token.");
		}
		
		ExternalAccountConnection connection = new ExternalAccountConnection(credential.getId(), UserServiceType.GOOGLE);
		UserIdentity userId = UserCore.getUserId(connection);
		if(userId != null){
			return new Response(Status.BAD_REQUEST, "The user is already registered with this service.");
		}
		
		Registration registration = new Registration();
		String googleUserId = credential.getId();
		registration.setUsername(UserServiceType.GOOGLE.name()+googleUserId); // use prefix to prevent collisions with other services
		registration.setPassword(RandomStringUtils.randomAlphanumeric(50)); // create a random password for this account
		
		RegistrationStatus status = UserCore.createUser(registration); // register as new user
		if(status != RegistrationStatus.OK){
			return new Response(Status.BAD_REQUEST, "Failed to create new user: "+status.name());
		}
		
		connection.setExternalId(googleUserId);
		UserCore.insertExternalAccountConnection(connection, registration.getRegisteredUserId()); // link the created user to the given account
		return new Response();
	}
	
	/**
	 * Event listener for user related events.
	 * 
	 * Automatically instantiated by Spring as a bean.
	 */
	@SuppressWarnings("unused")
	private static class UserEventListener implements ApplicationListener<UserServiceEvent>{

		@Override
		public void onApplicationEvent(UserServiceEvent event) {
			EventType type = event.getType();
			if(type == EventType.USER_REMOVED || (type == EventType.USER_AUTHORIZATION_REVOKED && event.getSource().equals(UserCore.class) && UserServiceType.GOOGLE.equals(event.getUserServiceType()))){
				UserIdentity userId = event.getUserId();
				LOGGER.debug("Detected event of type "+type.name()+", removing tokens for user, id: "+userId.getUserId());
				ServiceInitializer.getDAOHandler().getSQLDAO(GoogleUserDAO.class).removeToken(userId);
				
				ServiceInitializer.getEventHandler().publishEvent(new UserEvent(GoogleUserCore.class, userId, EventType.USER_AUTHORIZATION_REVOKED)); // send revoked event, this should trigger clean up on all relevant services
			}
		}
	} // class UserEventListener
}
