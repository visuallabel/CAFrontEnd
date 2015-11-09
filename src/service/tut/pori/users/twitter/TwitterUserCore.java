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
package service.tut.pori.users.twitter;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationListener;

import service.tut.pori.users.UserCore;
import service.tut.pori.users.UserCore.Registration;
import service.tut.pori.users.UserCore.RegistrationStatus;
import service.tut.pori.users.UserServiceEvent;
import twitter4j.auth.AccessToken;
import core.tut.pori.context.ServiceInitializer;
import core.tut.pori.http.RedirectResponse;
import core.tut.pori.http.Response;
import core.tut.pori.http.Response.Status;
import core.tut.pori.properties.NonceProperties;
import core.tut.pori.users.ExternalAccountConnection;
import core.tut.pori.users.ExternalAccountConnection.UserServiceType;
import core.tut.pori.users.UserEvent;
import core.tut.pori.users.UserEvent.EventType;
import core.tut.pori.users.UserIdentity;
import core.tut.pori.utils.JSONFormatter;

/**
 * Twitter User Service core methods.
 * 
 * This class emits events of type {@link core.tut.pori.users.UserEvent} for user account modifications with one of the listed {@link core.tut.pori.users.UserEvent.EventType} :
 * <ul>
 *  <li>{@link core.tut.pori.users.UserEvent.EventType#USER_AUTHORIZATION_GIVEN} for new user account authorizations.</li>
 *  <li>{@link core.tut.pori.users.UserEvent.EventType#USER_AUTHORIZATION_REVOKED} for removed user account authorizations.</li>
 * </ul>
 */
public final class TwitterUserCore {
	private static final String ERROR_MESSAGE_TWITTER = "An error occurred while connecting Twitter service.";
	private static final Logger LOGGER = Logger.getLogger(TwitterUserCore.class);
	private static final int NONCE_LENGTH = 32;
	private static final String PARAMETER_VALUE_FALSE = "false";
	private static final String PARAMETER_VALUE_OAUTH = "OAuth ";
	private static final String PARAMETER_VALUE_SIGNATURE_METHOD = "HMAC-SHA1";
	private static final String PARAMETER_VALUE_TRUE = "true";
	private static final String PARAMETER_VALUE_VERSION = "1.0";
	private static final String QUOTATION_MARK = "\"";
	private static final String SIGNING_ALGORITHM = "HmacSHA1";

	/**
	 * 
	 */
	private TwitterUserCore(){
		// nothing needed
	}

	/**
	 * 
	 * @param userIdentity
	 * @return response
	 */
	public static Response removeAuthorization(UserIdentity userIdentity) {
		if(!UserIdentity.isValid(userIdentity)){
			LOGGER.warn("Invalid user identity.");
			return new Response(Status.INTERNAL_SERVER_ERROR);
		}

		ServiceInitializer.getDAOHandler().getSQLDAO(TwitterUserDAO.class).removeTokens(userIdentity);

		ServiceInitializer.getEventHandler().publishEvent(new UserEvent(TwitterUserCore.class, userIdentity, EventType.USER_AUTHORIZATION_REVOKED)); // send revoked event, this should trigger clean up on all relevant services
		return new Response(); // default OK
	}

	/**
	 * 
	 * @param session 
	 * @param token
	 * @param verifier
	 * @return response
	 */
	public static Response processOAuthLoginCallback(HttpSession session, String token, String verifier) {
		RequestToken requestToken = ServiceInitializer.getDAOHandler().getSQLDAO(TwitterUserDAO.class).getRequestToken(token);
		if(requestToken == null){
			LOGGER.warn("The token does not exist: "+token);
			return new Response(Status.BAD_REQUEST);
		}
		requestToken.setVerifier(verifier);
		AccessToken accessToken = getAccessToken(requestToken);
		if(accessToken == null){
			LOGGER.warn("Failed to get access token for request token: "+token);
			return new Response(Status.BAD_REQUEST);
		}
		
		TwitterCredential tc = getCredential(accessToken);
		if(tc == null){
			LOGGER.warn("Failed to resolve twitter credentials with access token: "+accessToken.getToken());
			return new Response(Status.INTERNAL_SERVER_ERROR);
		}
		
		UserIdentity userId = UserCore.getUserId(new ExternalAccountConnection(tc.getId(), UserServiceType.TWITTER));
		if(!UserIdentity.isValid(userId)){
			return new Response(Status.FORBIDDEN, "The given Facebook user is not registered with this service, please register before login.");
		}
		
		ServiceInitializer.getSessionHandler().registerAndAuthenticate(session.getId(), userId);
		return new Response();
	}

	/**
	 * 
	 * @param token
	 * @param verifier
	 * @return response
	 */
	public static Response processOAuthAuthorizeCallback(String token, String verifier) {
		TwitterUserDAO dao = ServiceInitializer.getDAOHandler().getSQLDAO(TwitterUserDAO.class);
		RequestToken requestToken = dao.getRequestToken(token);
		if(requestToken == null){
			LOGGER.warn("The token does not exist: "+token);
			return new Response(Status.BAD_REQUEST);
		}
		requestToken.setVerifier(verifier);
		AccessToken accessToken = getAccessToken(requestToken);
		if(accessToken == null){
			LOGGER.warn("Failed to get access token for request token: "+token);
			return new Response(Status.BAD_REQUEST);
		}
		
		TwitterCredential tc = getCredential(accessToken);
		if(tc == null){
			LOGGER.warn("Failed to resolve twitter credentials with access token: "+accessToken.getToken());
			return new Response(Status.INTERNAL_SERVER_ERROR);
		}
		
		UserIdentity userId = requestToken.getUserId();
		if(!dao.setAccessToken(tc.getId(), accessToken, userId)){
			LOGGER.warn("Failed to set new token.");
			return new Response(Status.BAD_REQUEST);
		}
		
		ServiceInitializer.getEventHandler().publishEvent(new UserEvent(TwitterUserCore.class, userId, EventType.USER_AUTHORIZATION_GIVEN));
		
		String redirectUri = requestToken.getRedirectUri();
		if(StringUtils.isBlank(redirectUri)){
			return new Response();
		}else{
			return new RedirectResponse(redirectUri);
		}
	}

	/**
	 * 
	 * @param token
	 * @param verifier
	 * @return response
	 */
	public static Response processOAuthRegisterCallback(String token, String verifier) {
		RequestToken requestToken = ServiceInitializer.getDAOHandler().getSQLDAO(TwitterUserDAO.class).getRequestToken(token);
		if(requestToken == null){
			LOGGER.warn("The token does not exist: "+token);
			return new Response(Status.BAD_REQUEST);
		}
		requestToken.setVerifier(verifier);
		AccessToken accessToken = getAccessToken(requestToken);
		if(accessToken == null){
			LOGGER.warn("Failed to get access token for request token: "+token);
			return new Response(Status.BAD_REQUEST);
		}
		
		TwitterCredential tc = getCredential(accessToken);
		if(tc == null){
			LOGGER.warn("Failed to resolve twitter credentials with access token: "+accessToken.getToken());
			return new Response(Status.INTERNAL_SERVER_ERROR);
		}
		
		String twitterUserId = tc.getId();
		ExternalAccountConnection connection = new ExternalAccountConnection(twitterUserId, UserServiceType.TWITTER);
		UserIdentity userId = UserCore.getUserId(connection);
		if(userId != null){
			return new Response(Status.BAD_REQUEST, "The user is already registered with this service.");
		}
		
		Registration registration = new Registration();
		registration.setUsername(UserServiceType.TWITTER.name()+twitterUserId); // use prefix to prevent collisions with other services
		registration.setPassword(RandomStringUtils.randomAlphanumeric(50)); // create a random password for this account
		
		RegistrationStatus status = UserCore.createUser(registration); // register as new user
		if(status != RegistrationStatus.OK){
			return new Response(Status.BAD_REQUEST, "Failed to create new user: "+status.name());
		}
		
		connection.setExternalId(twitterUserId);
		UserCore.insertExternalAccountConnection(connection, registration.getRegisteredUserId()); // link the created user to the given account
		return new Response();
	}
	
	/**
	 * 
	 * @param userId
	 * @return TwitterCredential for the requested userId or null if none available
	 */
	public static TwitterCredential getCredential(UserIdentity userId){
		if(!UserIdentity.isValid(userId)){
			LOGGER.warn("Given userId was not valid.");
			return null;
		}

		AccessToken token = getToken(userId);
		if(token == null){
			LOGGER.debug("User does not have valid Twitter credentials.");
			return null;
		}
		
		TwitterCredential credential = getCredential(token);
		if(credential == null){
			LOGGER.debug("Failed to resolve credentials.");
		}else{
			credential.setUserId(userId);
		}
		return credential;
	}
	
	/**
	 * 
	 * @param authorizedUser
	 * @return the token or null if not found
	 */
	public static AccessToken getToken(UserIdentity authorizedUser) {
		if(!UserIdentity.isValid(authorizedUser)){
			LOGGER.warn("Invalid user identity.");
			return null;
		}
		return ServiceInitializer.getDAOHandler().getSQLDAO(TwitterUserDAO.class).getAccessToken(authorizedUser); // there is no way to refresh twitter's token, nor do we really know if it has been invalidated, so simply return the token
	}

	/**
	 * Helper method for retrieving credentials from twitter servers
	 * 
	 * https://dev.twitter.com/docs/api/1.1/get/account/verify_credentials
	 * 
	 * @param accessToken
	 * @return credentials for the given token or null if none was found
	 */
	private static TwitterCredential getCredential(AccessToken accessToken){
		TwitterCredential credential = null;
		try(CloseableHttpClient client = HttpClients.createDefault()){
			TwitterProperties tp = ServiceInitializer.getPropertyHandler().getSystemProperties(TwitterProperties.class);
			String requestUri = tp.getUserInfoUri();
			
			URLCodec codec = new URLCodec(core.tut.pori.http.Definitions.ENCODING_UTF8);
			SortedMap<String, String> encodedParameters = new TreeMap<>();
			encodedParameters.put(Definitions.PARAMETER_OAUTH_CONSUMER_KEY, tp.getEncodedApiKey());
			encodedParameters.put(Definitions.PARAMETER_OAUTH_NONCE, RandomStringUtils.randomAlphanumeric(NONCE_LENGTH));
			encodedParameters.put(Definitions.PARAMETER_OAUTH_SIGNATURE_METHOD, PARAMETER_VALUE_SIGNATURE_METHOD);
			encodedParameters.put(Definitions.PARAMETER_OAUTH_TIMESTAMP, String.valueOf(System.currentTimeMillis()/1000));
			encodedParameters.put(Definitions.PARAMETER_OAUTH_VERSION, PARAMETER_VALUE_VERSION);
			encodedParameters.put(Definitions.PARAMETER_OAUTH_TOKEN, codec.encode(accessToken.getToken()));
			
			encodedParameters.put(Definitions.PARAMETER_TWITTER_INCLUDE_ENTITIES, PARAMETER_VALUE_FALSE); // add temporarily for signature generation
			encodedParameters.put(Definitions.PARAMETER_TWITTER_SKIP_STATUS, PARAMETER_VALUE_TRUE); // add temporarily for signature generation
			
			encodedParameters.put(Definitions.PARAMETER_OAUTH_SIGNATURE, createSignature(tp.getEncodedClientSecret(), encodedParameters, accessToken.getTokenSecret(), core.tut.pori.http.Definitions.METHOD_GET, requestUri));
			
			encodedParameters.remove(Definitions.PARAMETER_TWITTER_INCLUDE_ENTITIES); // remove from header, these are part of the query uri
			encodedParameters.remove(Definitions.PARAMETER_TWITTER_SKIP_STATUS); // remove from header, these are part of the query uri
			
			HttpGet get = new HttpGet(requestUri+core.tut.pori.http.Definitions.SEPARATOR_URI_METHOD_PARAMS+Definitions.PARAMETER_TWITTER_INCLUDE_ENTITIES+core.tut.pori.http.Definitions.SEPARATOR_URI_QUERY_PARAM_VALUE_SEPARATOR+PARAMETER_VALUE_FALSE+core.tut.pori.http.Definitions.SEPARATOR_URI_QUERY_PARAMS+Definitions.PARAMETER_TWITTER_SKIP_STATUS+core.tut.pori.http.Definitions.SEPARATOR_URI_QUERY_PARAM_VALUE_SEPARATOR+PARAMETER_VALUE_TRUE);
			get.setHeader(Definitions.HEADER_OAUTH_AUTHORIZATION, createOAuthHeader(encodedParameters));	

			credential = JSONFormatter.createGsonSerializer().fromJson(client.execute(get, new BasicResponseHandler()), TwitterCredential.class);
		} catch (IOException | EncoderException | InvalidKeyException | NoSuchAlgorithmException ex) {
			LOGGER.error(ex, ex);
		}
		
		return credential;
	}

	/**
	 * Note: this will NOT save the access token to the database, but this WILL remove the given request token from the database whether the request was successful or not
	 * This will automatically discard expired tokens.
	 * 
	 * @param token
	 * @return the access token or null if failed to retrieve one
	 */
	private static final AccessToken getAccessToken(RequestToken token){
		TwitterUserDAO dao = ServiceInitializer.getDAOHandler().getSQLDAO(TwitterUserDAO.class);
		if(token.getUpdated().getTime()+ServiceInitializer.getPropertyHandler().getSystemProperties(NonceProperties.class).getNonceExpiresIn() < (System.currentTimeMillis())){
			LOGGER.warn("The request token has expired: "+token);
			dao.removeRequestToken(token); // remove the expired token
			return null;
		}
		
		AccessToken at = null;
		try (CloseableHttpClient client = HttpClients.createDefault()){
			TwitterProperties tp = ServiceInitializer.getPropertyHandler().getSystemProperties(TwitterProperties.class);

			URLCodec codec = new URLCodec(core.tut.pori.http.Definitions.ENCODING_UTF8);
			String requestUri = tp.getoAuthUri()+Definitions.METHOD_TWITTER_ACCESS_TOKEN;
			String encodedVerifier = codec.encode(token.getVerifier());

			SortedMap<String, String> encodedParameters = new TreeMap<>();
			encodedParameters.put(Definitions.PARAMETER_OAUTH_CONSUMER_KEY, tp.getEncodedApiKey());
			encodedParameters.put(Definitions.PARAMETER_OAUTH_NONCE, RandomStringUtils.randomAlphanumeric(NONCE_LENGTH));
			encodedParameters.put(Definitions.PARAMETER_OAUTH_SIGNATURE_METHOD, PARAMETER_VALUE_SIGNATURE_METHOD);
			encodedParameters.put(Definitions.PARAMETER_OAUTH_TIMESTAMP, String.valueOf(System.currentTimeMillis()/1000));
			encodedParameters.put(Definitions.PARAMETER_OAUTH_VERSION, PARAMETER_VALUE_VERSION);
			encodedParameters.put(Definitions.PARAMETER_OAUTH_VERIFIER, encodedVerifier); // for signature
			encodedParameters.put(Definitions.PARAMETER_OAUTH_TOKEN, codec.encode(token.getToken()));
			encodedParameters.put(Definitions.PARAMETER_OAUTH_SIGNATURE, createSignature(tp.getEncodedClientSecret(), encodedParameters, token.getSecret(), core.tut.pori.http.Definitions.METHOD_POST, requestUri));
			encodedParameters.remove(Definitions.PARAMETER_OAUTH_VERIFIER); // do not add it to the header

			HttpPost post = new HttpPost(requestUri);
			post.setHeader(Definitions.HEADER_OAUTH_AUTHORIZATION, createOAuthHeader(encodedParameters));
			post.setEntity(new StringEntity(Definitions.PARAMETER_OAUTH_VERIFIER+core.tut.pori.http.Definitions.SEPARATOR_URI_QUERY_PARAM_VALUE_SEPARATOR+encodedVerifier, ContentType.APPLICATION_FORM_URLENCODED));

			String[] responseParams = StringUtils.split(client.execute(post, new BasicResponseHandler()), core.tut.pori.http.Definitions.SEPARATOR_URI_QUERY_PARAMS);
			if(responseParams == null || responseParams.length < 2){
				LOGGER.warn("Failed to retrieve token.");
			}else{
				String accessToken = null;
				String accessTokenSecret = null;
				for(int i=0;i<responseParams.length;++i){
					String[] parts = StringUtils.split(responseParams[i], core.tut.pori.http.Definitions.SEPARATOR_URI_QUERY_PARAM_VALUE_SEPARATOR);
					if(parts.length != 2){
						LOGGER.warn("Invalid response parameter: "+responseParams[i]);
						break;
					}else if(Definitions.PARAMETER_OAUTH_TOKEN.equals(parts[0])){
						accessToken = codec.decode(parts[1]);
					}else if(Definitions.PARAMETER_OAUTH_TOKEN_SECRET.equals(parts[0])){
						accessTokenSecret = codec.decode(parts[1]);
					} // else ignore everything else
				}

				if(StringUtils.isBlank(accessToken) || StringUtils.isBlank(accessTokenSecret)){
					LOGGER.warn("Invalid "+Definitions.PARAMETER_OAUTH_TOKEN+" or "+Definitions.PARAMETER_OAUTH_TOKEN_SECRET);
				}else{
					at = new AccessToken(accessToken, accessTokenSecret);
				} // else
			} // else

		} catch (IOException | InvalidKeyException | NoSuchAlgorithmException | EncoderException | DecoderException ex) {
			LOGGER.error(ex, ex);
		}

		dao.removeRequestToken(token); // remove the request token to prevent further authentication attempts using this token

		return at;
	}

	/**
	 * 
	 * @return redirection response
	 */
	public static Response createLoginRedirection() {
		TwitterProperties tp = ServiceInitializer.getPropertyHandler().getSystemProperties(TwitterProperties.class);
		return createRedirection(tp.getEncodedOAuthLoginRedirectUri(), tp.getoAuthUri()+Definitions.METHOD_TWITTER_AUTHENTICATE, null, null);
	}

	/**
	 * 
	 * @return redirection response
	 */
	public static Response createRegisterRedirection() {
		TwitterProperties tp = ServiceInitializer.getPropertyHandler().getSystemProperties(TwitterProperties.class);
		return createRedirection(tp.getEncodedOAuthRegisterRedirectUri(), tp.getoAuthUri()+Definitions.METHOD_TWITTER_AUTHENTICATE, null, null);
	}

	/**
	 * 
	 * @param authenticatedUser
	 * @param redirectUri
	 * @return redirection response
	 */
	public static Response createAuthorizationRedirection(UserIdentity authenticatedUser, String redirectUri) {
		TwitterProperties tp = ServiceInitializer.getPropertyHandler().getSystemProperties(TwitterProperties.class);
		return createRedirection(tp.getEncodedOAuthAuthorizeRedirectUri(), tp.getoAuthUri()+Definitions.METHOD_TWITTER_AUTHORIZE, redirectUri, authenticatedUser);
	}

	/**
	 * 
	 * @param encodedClientSecret
	 * @param encodedParameters non-empty, non-null list of parameters
	 * @param encodedTokenSecret
	 * @param httpMethod
	 * @param requestUri
	 * @return signature string created from the given values
	 * @throws EncoderException 
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeyException 
	 */
	private static final String createSignature(String encodedClientSecret, SortedMap<String, String> encodedParameters, String encodedTokenSecret, String httpMethod, String requestUri) throws EncoderException, NoSuchAlgorithmException, InvalidKeyException{
		StringBuilder baseString = new StringBuilder();
		for(Entry<String, String> e : encodedParameters.entrySet()){
			baseString.append(e.getKey());
			baseString.append(core.tut.pori.http.Definitions.SEPARATOR_URI_QUERY_PARAM_VALUE_SEPARATOR);
			baseString.append(e.getValue());
			baseString.append(core.tut.pori.http.Definitions.SEPARATOR_URI_QUERY_PARAMS);
		}

		URLCodec codec = new URLCodec(core.tut.pori.http.Definitions.ENCODING_UTF8);
		String signature = httpMethod+core.tut.pori.http.Definitions.SEPARATOR_URI_QUERY_PARAMS+codec.encode(requestUri)+core.tut.pori.http.Definitions.SEPARATOR_URI_QUERY_PARAMS+
				codec.encode(baseString.substring(0, baseString.length()-1)); // chop the last character

		Mac mac = Mac.getInstance(SIGNING_ALGORITHM);
		mac.init(new SecretKeySpec((encodedClientSecret+core.tut.pori.http.Definitions.SEPARATOR_URI_QUERY_PARAMS+(StringUtils.isBlank(encodedTokenSecret) ? "" : encodedTokenSecret)).getBytes(), SIGNING_ALGORITHM));
		return codec.encode(new String(Base64.encodeBase64(mac.doFinal(signature.getBytes()))).trim());
	}

	/**
	 * 
	 * @param encodedParameters non-null, non-empty list of parameters
	 * @return authentication header created from the given values
	 */
	private static final String createOAuthHeader(SortedMap<String, String> encodedParameters){
		StringBuilder header = new StringBuilder(PARAMETER_VALUE_OAUTH);
		for(Entry<String, String> e : encodedParameters.entrySet()){
			header.append(e.getKey());
			header.append(core.tut.pori.http.Definitions.SEPARATOR_URI_QUERY_PARAM_VALUE_SEPARATOR);
			header.append(QUOTATION_MARK);
			header.append(e.getValue());
			header.append(QUOTATION_MARK);
			header.append(core.tut.pori.http.Definitions.SEPARATOR_URI_QUERY_PARAM_VALUES);
		}

		return header.substring(0, header.length()-1); // chop the last character
	}

	/**
	 * Generates a redirection request. Stores the given user identity and target URL into database (if given AND the operation finished successfully).
	 * This is a helper method for the create*Redirection methods.
	 * 
	 * As defined in:
	 * https://dev.twitter.com/docs/auth/implementing-sign-twitter
	 * and
	 * https://dev.twitter.com/docs/auth/authorizing-request
	 * and
	 * https://dev.twitter.com/docs/auth/3-legged-authorization
	 * and
	 * https://dev.twitter.com/docs/auth/creating-signature
	 * 
	 * @param callbackUrl the local method where the request should return, this MUST be URL encoded
	 * @param serviceUrl where the initial request will be redirected to, ie. the Twitter endpoint (e.g. https://api.twitter.com/oauth/authenticate), without trailing / or uri parameters
	 * @param targetUrl the user provided final target, only stored to database. Can be null.
	 * @param userId the requester, only stored to database
	 * @return redirection response
	 */
	private static Response createRedirection(String callbackUrl, String serviceUrl, String targetUrl, UserIdentity userId){
		try(CloseableHttpClient client = HttpClients.createDefault()){
			TwitterProperties tp = ServiceInitializer.getPropertyHandler().getSystemProperties(TwitterProperties.class);

			String requestUri = tp.getoAuthUri()+Definitions.METHOD_TWITTER_REQUEST_TOKEN;

			SortedMap<String, String> encodedParameters = new TreeMap<>();
			encodedParameters.put(Definitions.PARAMETER_OAUTH_CALLBACK, callbackUrl);
			encodedParameters.put(Definitions.PARAMETER_OAUTH_CONSUMER_KEY, tp.getEncodedApiKey());
			encodedParameters.put(Definitions.PARAMETER_OAUTH_NONCE, RandomStringUtils.randomAlphanumeric(NONCE_LENGTH));
			encodedParameters.put(Definitions.PARAMETER_OAUTH_SIGNATURE_METHOD, PARAMETER_VALUE_SIGNATURE_METHOD);
			encodedParameters.put(Definitions.PARAMETER_OAUTH_TIMESTAMP, String.valueOf(System.currentTimeMillis()/1000));
			encodedParameters.put(Definitions.PARAMETER_OAUTH_VERSION, PARAMETER_VALUE_VERSION);
			encodedParameters.put(Definitions.PARAMETER_OAUTH_SIGNATURE, createSignature(tp.getEncodedClientSecret(), encodedParameters, null, core.tut.pori.http.Definitions.METHOD_POST, requestUri));			

			HttpPost post = new HttpPost(requestUri);
			post.setHeader(Definitions.HEADER_OAUTH_AUTHORIZATION, createOAuthHeader(encodedParameters));

			String[] responseParams = StringUtils.split(client.execute(post, new BasicResponseHandler()), core.tut.pori.http.Definitions.SEPARATOR_URI_QUERY_PARAMS);
			if(responseParams == null || responseParams.length < 3){
				LOGGER.warn("Did not receive the required confirmation parameters.");
				return new Response(Status.INTERNAL_SERVER_ERROR, ERROR_MESSAGE_TWITTER);
			}

			URLCodec codec = new URLCodec(core.tut.pori.http.Definitions.ENCODING_UTF8);
			String requestToken = null;
			String encodedRequestToken = null;
			String tokenSecret = null;
			boolean callbackConfirmed = false;
			for(int i=0;i<responseParams.length;++i){
				String[] parts = StringUtils.split(responseParams[i], core.tut.pori.http.Definitions.SEPARATOR_URI_QUERY_PARAM_VALUE_SEPARATOR);
				if(parts.length != 2){
					LOGGER.warn("Bad response parameter: "+responseParams[i]);
					return new Response(Status.INTERNAL_SERVER_ERROR, ERROR_MESSAGE_TWITTER);
				}else if(Definitions.PARAMETER_OAUTH_TOKEN.equals(parts[0])){
					requestToken = codec.decode(parts[1]);
					encodedRequestToken = parts[1]; // hard to say whether the tokens need to be decoded/encoded or not, but let's do so just in case
				}else if(Definitions.PARAMETER_OAUTH_TOKEN_SECRET.equals(parts[0])){
					tokenSecret = codec.decode(parts[1]);
				}else if(Definitions.PARAMETER_OAUTH_CALLBACK_CONFIRMED.equals(parts[0])){
					callbackConfirmed = BooleanUtils.toBoolean(parts[1]);
				} // else ignore everything else
			}

			if(!callbackConfirmed){
				LOGGER.warn(Definitions.PARAMETER_OAUTH_CALLBACK+" was not confirmed: "+callbackUrl);
				return new Response(Status.INTERNAL_SERVER_ERROR);
			}
			if(StringUtils.isBlank(requestToken) || StringUtils.isBlank(tokenSecret)){
				LOGGER.warn("Invalid "+Definitions.PARAMETER_OAUTH_TOKEN+" : "+requestToken+" or "+Definitions.PARAMETER_OAUTH_TOKEN_SECRET+" : "+tokenSecret);
				return new Response(Status.INTERNAL_SERVER_ERROR);
			}

			ServiceInitializer.getDAOHandler().getSQLDAO(TwitterUserDAO.class).setRequestToken(new RequestToken(targetUrl, requestToken, tokenSecret, userId));

			return new RedirectResponse(serviceUrl+core.tut.pori.http.Definitions.SEPARATOR_URI_METHOD_PARAMS+Definitions.PARAMETER_OAUTH_TOKEN+core.tut.pori.http.Definitions.SEPARATOR_URI_QUERY_PARAM_VALUE_SEPARATOR+encodedRequestToken);
		} catch (IOException ex) {
			LOGGER.error(ex, ex);
			return new Response(Status.INTERNAL_SERVER_ERROR, ERROR_MESSAGE_TWITTER);
		} catch (DecoderException | EncoderException | NoSuchAlgorithmException | InvalidKeyException ex){
			LOGGER.error(ex, ex);
			return new Response(Status.INTERNAL_SERVER_ERROR);
		}
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
			if(type == EventType.USER_REMOVED || (type == EventType.USER_AUTHORIZATION_REVOKED && event.getSource().equals(UserCore.class) && UserServiceType.TWITTER.equals(event.getUserServiceType()))){
				UserIdentity userId = event.getUserId();
				LOGGER.debug("Detected event of type "+type.name()+", removing tokens for user, id: "+userId.getUserId());
				ServiceInitializer.getDAOHandler().getSQLDAO(TwitterUserDAO.class).removeTokens(userId);
				
				ServiceInitializer.getEventHandler().publishEvent(new UserEvent(TwitterUserCore.class, userId, EventType.USER_AUTHORIZATION_REVOKED)); // send revoked event, this should trigger clean up on all relevant services
			}
		}
	} // class UserEventListener
}
