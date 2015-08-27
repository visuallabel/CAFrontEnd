/**
 * Copyright 2014 Tampere University of Technology, Pori Unit
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
package service.tut.pori.users.facebook;

import java.io.IOException;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationListener;

import service.tut.pori.users.Definitions;
import service.tut.pori.users.OAuth2Token;
import service.tut.pori.users.UserCore;
import service.tut.pori.users.UserCore.Registration;
import service.tut.pori.users.UserCore.RegistrationStatus;
import service.tut.pori.users.UserServiceEvent;
import core.tut.pori.context.ServiceInitializer;
import core.tut.pori.http.RedirectResponse;
import core.tut.pori.http.Response;
import core.tut.pori.http.Response.Status;
import core.tut.pori.users.ExternalAccountConnection;
import core.tut.pori.users.UserEvent;
import core.tut.pori.users.ExternalAccountConnection.UserServiceType;
import core.tut.pori.users.UserEvent.EventType;
import core.tut.pori.users.UserIdentity;

/**
 * Facebook User Service Core methods.
 * 
 * This implementation follows:
 * 
 * refreshing tokens:
 * http://developers.facebook.com/docs/facebook-login/access-tokens/#extending
 * http://stackoverflow.com/questions/16563692/refresh-token-and-access-token-in-facebook-api
 * 
 * http://developers.facebook.com/docs/facebook-login/login-flow-for-web-no-jssdk/
 * 
 * This class emits events of type {@link core.tut.pori.users.UserEvent} for user account modifications with one of the listed {@link core.tut.pori.users.UserEvent.EventType} :
 * <ul>
 *  <li>{@link core.tut.pori.users.UserEvent.EventType#USER_AUTHORIZATION_GIVEN} for new user account authorizations.</li>
 *  <li>{@link core.tut.pori.users.UserEvent.EventType#USER_AUTHORIZATION_REVOKED} for removed user account authorizations.</li>
 * </ul>
 */
public final class FacebookUserCore {
	private static final Logger LOGGER = Logger.getLogger(FacebookUserCore.class);
	private static final char DELIMITER_PARAMETER_VALUE = '=';
	private static final char DELIMITER_PARAMETERS = '&';
	private static final String PARAMETER_ACCESS_TOKEN = Definitions.PARAMETER_OAUTH2_ACCESS_TOKEN; // facebook specific body-parameter
	private static final String PARAMETER_EXPIRES = "expires"; // facebook specific body parameter
	private static final String PARAMETER_VALUE_REFRESH_GRANT_TYPE = "fb_exchange_token";
	private static final String PARAMETER_VALUE_RESPONSE_TYPE = "code";
	

	/**
	 * 
	 */
	private FacebookUserCore(){
		// nothing needed
	}

	/**
	 * 
	 * @param authorizationCode
	 * @param errorCode
	 * @param nonce
	 * @return response
	 */
	public static Response processOAuth2Callback(String authorizationCode, String errorCode, String nonce) {
		if(StringUtils.isBlank(nonce)){
			LOGGER.debug("nonce is missing.");
			return new Response(Status.BAD_REQUEST);
		}

		//Try to split nonce for a redirection URL
		Pair<String, String> nonceAndUrl = UserCore.getNonceAndRedirectUri(nonce);
		String redirectUri = null;
		if(nonceAndUrl != null){
			nonce = nonceAndUrl.getLeft();
			redirectUri = nonceAndUrl.getRight();
		}else{
			redirectUri = null;
		}
		
		FacebookUserDAO dao = ServiceInitializer.getDAOHandler().getSQLDAO(FacebookUserDAO.class);

		if(!StringUtils.isEmpty(errorCode)){
			dao.removeNonce(nonce);	// in any case, do not allow to use this nonce again
			LOGGER.debug("Received callback request with errorCode: "+errorCode);
			if(StringUtils.isEmpty(redirectUri)){
				return new Response(Status.OK, errorCode);
			}else if(redirectUri.contains("?")){
				return new RedirectResponse(redirectUri+"&error="+errorCode);
			}else{
				return new RedirectResponse(redirectUri+"?error="+errorCode);
			}
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
			FacebookProperties fp = ServiceInitializer.getPropertyHandler().getSystemProperties(FacebookProperties.class);
			StringBuilder uri = new StringBuilder(fp.getoAuth2TokenUri());
			uri.append(Definitions.METHOD_FACEBOOK_ACCESS_TOKEN+"?"+Definitions.PARAMETER_OAUTH2_CLIENT_ID+"=");
			uri.append(fp.getApplicationId());

			uri.append("&"+Definitions.PARAMETER_OAUTH2_REDIRECT_URI+"=");
			uri.append(fp.getEncodedOAuth2RedirectUri());

			uri.append("&"+Definitions.PARAMETER_OAUTH2_CLIENT_SECRET+"=");
			uri.append(fp.getSharedKey());

			uri.append("&"+Definitions.PARAMETER_OAUTH2_AUTHORIZATION_CODE+"=");
			uri.append(authorizationCode);

			OAuth2Token newToken = fromResponse(client.execute(new HttpGet(uri.toString()), new BasicResponseHandler()));
			if(newToken != null && newToken.isValid()){
				FacebookCredential fc = getCredential(newToken.getAccessToken());
				if(fc == null){
					LOGGER.error("Failed to resolve credentials for the new token.");
					return new Response(Status.INTERNAL_SERVER_ERROR);
				}		
				
				if(!dao.setToken(fc.getId(), newToken, userId)){
					LOGGER.warn("Failed to set new token.");
					return new Response(Status.BAD_REQUEST);
				}
				
				ServiceInitializer.getEventHandler().publishEvent(new UserEvent(FacebookUserCore.class, userId, EventType.USER_AUTHORIZATION_GIVEN));
			}else{
				LOGGER.debug("Did not receive a valid token.");
			}
		} catch (IOException ex) {
			LOGGER.error(ex, ex);
		}	
		
		if(StringUtils.isEmpty(redirectUri)){
			return new Response();
		}else{
			return new RedirectResponse(redirectUri);
		}
	}

	/**
	 * parses a facebook token response of format: access_token=ACCESS_TOKEN&expires=SECONDS_TILL_EXPIRATION
	 * 
	 * @param response
	 * @return the token or null if invalid string
	 */
	private static OAuth2Token fromResponse(String response){
		String[] params = StringUtils.split(response, DELIMITER_PARAMETERS);
		if(params == null || params.length  < 2){
			LOGGER.debug("Invalid response: "+response);
			return null;
		}
		String accessToken = null;
		Long expiresIn = null;
		try{
			for(int i=0;i<params.length;++i){
				String[] parts = StringUtils.split(params[i],DELIMITER_PARAMETER_VALUE);
				if(parts.length != 2){
					LOGGER.debug("Ignored invalid parameter: "+parts[i]);
				}else if(PARAMETER_EXPIRES.equalsIgnoreCase(parts[0])){
					expiresIn = Long.valueOf(parts[1]);
				}else if(PARAMETER_ACCESS_TOKEN.equalsIgnoreCase(parts[0])){
					accessToken = parts[1];
				}
			}
		} catch (NumberFormatException ex){
			LOGGER.error(ex, ex);
		}

		if(expiresIn == null || accessToken == null){
			LOGGER.debug("Did not receive valid token. "+PARAMETER_ACCESS_TOKEN+": "+accessToken+", "+PARAMETER_EXPIRES+": "+expiresIn);
			return null;
		}

		OAuth2Token token = new OAuth2Token();
		token.setAccessToken(accessToken);
		token.setExpiresIn(expiresIn);
		return token;
	}

	/**
	 * 
	 * @param authorizedUser valid user id
	 * @param redirectUri 
	 * @return redirection response
	 */
	public static Response createAuthorizationRedirection(UserIdentity authorizedUser, String redirectUri) {
		FacebookProperties fp = ServiceInitializer.getPropertyHandler().getSystemProperties(FacebookProperties.class);
		StringBuilder uri = new StringBuilder(fp.getoAuth2AuthUri());

		uri.append(Definitions.METHOD_FACEBOOK_AUTH+"?"+Definitions.PARAMETER_OAUTH2_SCOPE+"=");
		uri.append(fp.getoAuth2Scope());

		uri.append("&"+Definitions.PARAMETER_OAUTH2_STATE+"=");
		uri.append(UserCore.urlEncodedCombinedNonce(ServiceInitializer.getDAOHandler().getSQLDAO(FacebookUserDAO.class).generateNonce(authorizedUser), redirectUri));

		uri.append("&"+Definitions.PARAMETER_OAUTH2_REDIRECT_URI+"=");
		uri.append(fp.getEncodedOAuth2RedirectUri());

		uri.append("&"+Definitions.PARAMETER_OAUTH2_CLIENT_ID+"=");
		uri.append(fp.getApplicationId());

		uri.append("&"+Definitions.PARAMETER_OAUTH2_RESPONSE_TYPE+"="+PARAMETER_VALUE_RESPONSE_TYPE);

		return new RedirectResponse(uri.toString());
	}
	
	/**
	 * 
	 * @param authenticatedUser
	 * @return response
	 */
	public static Response removeAuthorization(UserIdentity authenticatedUser) {
		ServiceInitializer.getDAOHandler().getSQLDAO(FacebookUserDAO.class).removeToken(authenticatedUser); // simply remove the token, as it is not possible to revoke facebook tokens, they should auto-expire after a certain time
		
		ServiceInitializer.getEventHandler().publishEvent(new UserEvent(FacebookUserCore.class, authenticatedUser, EventType.USER_AUTHORIZATION_REVOKED));  // send revoked event, this should trigger clean up on all relevant services
		return null;
	}

	/**
	 * Retrieves the current token for the user if one is available
	 * 
	 * Note: if the token expires, you should use this method to retrieve a new one, refreshing the token
	 * manually may cause race condition with other services using the tokens (there can be only one active
	 * and valid access token at any time)
	 * 
	 * @param authorizedUser
	 * @return token for the given user or null if not found
	 */
	public static OAuth2Token getToken(UserIdentity authorizedUser){
		FacebookUserDAO dao = ServiceInitializer.getDAOHandler().getSQLDAO(FacebookUserDAO.class);
		OAuth2Token token = dao.getToken(authorizedUser);
		if(token == null){
			return null;
		}
		
		String facebookUserId = dao.getFacebookUserId(authorizedUser);
		if(facebookUserId == null){
			LOGGER.warn("Failed to resolve facebook user id.");
			return null;
		}

		if(token.expiresIn(ServiceInitializer.getPropertyHandler().getSystemProperties(FacebookProperties.class).getTokenAutorefresh())){	// Facebook does not support refreshing tokens, so if a token has expired there really isn't much we can do
			token = refreshToken(token);
			if(token == null){
				dao.removeToken(authorizedUser);	// remove the invalid token from the database
			}else{
				dao.setToken(facebookUserId, token, authorizedUser);
			}
		}
		return token;
	}

	/**
	 * 
	 * @param token
	 * @return the refreshed token or null on failure, Note: the returned token may not be the same object as the passed object
	 */
	private static OAuth2Token refreshToken(OAuth2Token token){
		if(token.isExpired()){
			LOGGER.debug("Expired or invalid token given.");
			return null;
		}
		try (CloseableHttpClient client = HttpClients.createDefault()) {
			FacebookProperties fp = ServiceInitializer.getPropertyHandler().getSystemProperties(FacebookProperties.class);
			StringBuilder uri = new StringBuilder(fp.getoAuth2TokenUri());
			uri.append(Definitions.METHOD_FACEBOOK_ACCESS_TOKEN+"?"+Definitions.PARAMETER_OAUTH2_GRANT_TYPE+"="+PARAMETER_VALUE_REFRESH_GRANT_TYPE);

			uri.append("&"+Definitions.PARAMETER_OAUTH2_CLIENT_ID+"=");
			uri.append(fp.getApplicationId());

			uri.append("&"+Definitions.PARAMETER_OAUTH2_CLIENT_SECRET+"=");
			uri.append(fp.getSharedKey());

			uri.append("&"+Definitions.PARAMETER_FACEBOOK_EXCHANGE_TOKEN+"=");
			uri.append(token.getAccessToken());

			OAuth2Token newToken = fromResponse(client.execute(new HttpGet(uri.toString()), new BasicResponseHandler()));
			if(newToken != null && newToken.isValid()){
				return newToken;
			}
		} catch (IOException ex) {
			LOGGER.error(ex, ex);
		}
		return null;
	}
	
	/**
	 * 
	 * @param userId
	 * @return FacebookCredential for the requested userId or null if none available
	 */
	public static FacebookCredential getCredential(UserIdentity userId){
		if(!UserIdentity.isValid(userId)){
			LOGGER.warn("Given userId was not valid.");
			return null;
		}

		OAuth2Token token = FacebookUserCore.getToken(userId);
		if(token == null){
			LOGGER.debug("User does not have valid Facebook credentials.");
			return null;
		}
		
		FacebookCredential credential = getCredential(token.getAccessToken());
		if(credential == null){
			LOGGER.debug("Failed to resolve credentials.");
		}else{
			credential.setUserId(userId);
		}
		return credential;
	}
	
	/**
	 * Helper method for retrieving credentials from facebook servers
	 * 
	 * @param accessToken
	 * @return the credential for the access token or null if not found
	 */
	private static FacebookCredential getCredential(String accessToken){
		FacebookCredential credential = null;
		try(CloseableHttpClient client = HttpClients.createDefault()){
			FacebookProperties fp = ServiceInitializer.getPropertyHandler().getSystemProperties(FacebookProperties.class);
			credential = (OAuth2Token.getTokenGSONSerializer()).fromJson(client.execute(new HttpGet(fp.getoAuth2UserInfoUri()+"?"+Definitions.PARAMETER_OAUTH2_ACCESS_TOKEN+"="+accessToken), new BasicResponseHandler()), FacebookCredential.class);
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
		FacebookCredential credential = getCredential(accessToken);
		if(credential == null){
			return new Response(Status.BAD_REQUEST, "Failed to process the given token.");
		}
		
		UserIdentity userId = UserCore.getUserId(new ExternalAccountConnection(credential.getId(), UserServiceType.FACEBOOK));
		if(!UserIdentity.isValid(userId)){
			return new Response(Status.FORBIDDEN, "The given Facebook user is not registered with this service, please register before login.");
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
		FacebookCredential credential = getCredential(accessToken);
		if(credential == null){
			return new Response(Status.BAD_REQUEST, "Failed to process the given token.");
		}
		
		ExternalAccountConnection connection = new ExternalAccountConnection(credential.getId(), UserServiceType.FACEBOOK);
		UserIdentity userId = UserCore.getUserId(connection);
		if(userId != null){
			return new Response(Status.BAD_REQUEST, "The user is already registered with this service.");
		}
		
		Registration registration = new Registration();
		String facebookUserId = credential.getId();
		registration.setUsername(UserServiceType.FACEBOOK.name()+facebookUserId); // use prefix to prevent collisions with other services
		registration.setPassword(RandomStringUtils.randomAlphanumeric(50)); // create a random password for this account
		
		RegistrationStatus status = UserCore.createUser(registration); // register as new user
		if(status != RegistrationStatus.OK){
			return new Response(Status.BAD_REQUEST, "Failed to create new user: "+status.name());
		}
		
		connection.setExternalId(facebookUserId);
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
			if(type == EventType.USER_REMOVED || (type == EventType.USER_AUTHORIZATION_REVOKED && event.getSource().equals(UserCore.class) && UserServiceType.FACEBOOK.equals(event.getUserServiceType()))){
				UserIdentity userId = event.getUserId();
				LOGGER.debug("Detected event of type "+type.name()+", removing tokens for user, id: "+userId.getUserId());
				ServiceInitializer.getDAOHandler().getSQLDAO(FacebookUserDAO.class).removeToken(userId);
				
				ServiceInitializer.getEventHandler().publishEvent(new UserEvent(FacebookUserCore.class, userId, EventType.USER_AUTHORIZATION_REVOKED));  // send revoked event, this should trigger clean up on all relevant services
			}
		}
	} // class UserEventListener
}
