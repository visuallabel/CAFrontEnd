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
package service.tut.pori.users;

/**
 * Definitions for users package.
 *
 */
public final class Definitions {
	/* services */
	/** service name declaration */
	public static final String SERVICE_USERS = "user";
	/** service name declaration */
	public static final String SERVICE_USERS_FACEBOOK = "facebook";
	/** service name declaration */
	public static final String SERVICE_USERS_FSIO = "fsio";
	/** service name declaration */
	public static final String SERVICE_USERS_GOOGLE = "google";
	/** service name declaration */
	public static final String SERVICE_USERS_TWITTER = "twitter";
	
	/* local methods */
	/** local service method declaration */
	public static final String METHOD_GET_EXTERNAL_ACCOUNT_CONNECTIONS = "getExternalAccountConnections";
	/** local service method declaration */
	public static final String METHOD_GET_USER_DETAILS = "getUserDetails";
	/** local service method declaration */
	public static final String METHOD_LOGIN = "login";
	/** local service method declaration */
	public static final String METHOD_OAUTH_AUTHORIZE_CALLBACK = "oAuthAuthorizeCallback";
	/** local service method declaration */
	public static final String METHOD_OAUTH_LOGIN_CALLBACK = "oAuthLoginCallback";
	/** local service method declaration */
	public static final String METHOD_OAUTH_REGISTER_CALLBACK = "oAuthRegisterCallback";
	/** local service method declaration */
	public static final String METHOD_OAUTH_AUTHORIZATION_REDIRECT = "authorize";
	/** local service method declaration */
	public static final String METHOD_OAUTH2_AUTHORIZATION_REDIRECT = METHOD_OAUTH_AUTHORIZATION_REDIRECT;
	/** local service method declaration */
	public static final String METHOD_OAUTH2_CALLBACK = "oAuth2callback";
	/** local service method declaration */
	public static final String METHOD_REGISTER = "register";
	/** local service method declaration */
	public static final String METHOD_DELETE_EXTERNAL_ACCOUNT_CONNECTION = "deleteExternalAccountConnection";
	/** local service method declaration */
	public static final String METHOD_UNAUTHORIZE = "unauthorize";
	/** local service method declaration */
	public static final String METHOD_UNREGISTER = "unregister";
	/* remote methods */
	/** remote service method declaration */
	public static final String METHOD_FACEBOOK_ACCESS_TOKEN = "access_token";
	/** remote service method declaration */
	public static final String METHOD_FACEBOOK_AUTH = "oauth";
	/** remote service method declaration */
	public static final String METHOD_FSIO_OAUTH = "authorization.oauth2";
	/** remote service method declaration */
	public static final String METHOD_GOOGLE_AUTH = "auth";
	/** remote service method declaration */
	public static final String METHOD_GOOGLE_REVOKE = "revoke";
	/** remote service method declaration */
	public static final String METHOD_GOOGLE_TOKEN = "token";
	/** remote service method declaration */
	public static final String METHOD_GOOGLE_USER_INFO = "userinfo";
	/** remote service method declaration */
	public static final String METHOD_TWITTER_ACCESS_TOKEN = "access_token";
	/** remote service method declaration */
	public static final String METHOD_TWITTER_AUTHENTICATE = "authenticate";
	/** remote service method declaration */
	public static final String METHOD_TWITTER_AUTHORIZE = "authorize";
	/** remote service method declaration */
	public static final String METHOD_TWITTER_REQUEST_TOKEN = "request_token";
	
	/* parameters */
	/** method parameter declaration */
	public static final String PARAMETER_FACEBOOK_EXCHANGE_TOKEN = "fb_exchange_token";
	/** method parameter declaration */
	public static final String PARAMETER_FACEBOOK_HACK_REDIRECT_URI = "fb_redirect_uri";
	/** method parameter declaration */
	public static final String PARAMETER_GOOGLE_ACCESS_TYPE = "access_type";
	/** method parameter declaration */
	public static final String PARAMETER_GOOGLE_APPROVAL_PROMPT = "approval_prompt";
	/** method parameter declaration */
	public static final String PARAMETER_GOOGLE_TOKEN = "token";
	/** method parameter declaration */
	public static final String PARAMETER_OAUTH_CALLBACK = "oauth_callback";
	/** method parameter declaration */
	public static final String PARAMETER_OAUTH_CALLBACK_CONFIRMED = "oauth_callback_confirmed";
	/** method parameter declaration */
	public static final String PARAMETER_OAUTH_CONSUMER_KEY = "oauth_consumer_key";
	/** method parameter declaration */
	public static final String PARAMETER_OAUTH_NONCE = "oauth_nonce";
	/** method parameter declaration */
	public static final String PARAMETER_OAUTH_SIGNATURE = "oauth_signature";
	/** method parameter declaration */
	public static final String PARAMETER_OAUTH_SIGNATURE_METHOD = "oauth_signature_method";
	/** method parameter declaration */
	public static final String PARAMETER_OAUTH_TIMESTAMP = "oauth_timestamp";
	/** method parameter declaration */
	public static final String PARAMETER_OAUTH_TOKEN = "oauth_token";
	/** method parameter declaration */
	public static final String PARAMETER_OAUTH_TOKEN_SECRET = "oauth_token_secret";
	/** method parameter declaration */
	public static final String PARAMETER_OAUTH_VERIFIER = "oauth_verifier";
	/** method parameter declaration */
	public static final String PARAMETER_OAUTH_VERSION = "oauth_version";
	/** method parameter declaration */
	public static final String PARAMETER_OAUTH2_ACCESS_TOKEN = "access_token";
	/** method parameter declaration */
	public static final String PARAMETER_OAUTH2_AUTHORIZATION_CODE = "code";
	/** method parameter declaration */
	public static final String PARAMETER_OAUTH2_CLIENT_ID = "client_id";
	/** method parameter declaration */
	public static final String PARAMETER_OAUTH2_CLIENT_SECRET = "client_secret";
	/** method parameter declaration */
	public static final String PARAMETER_OAUTH2_ERROR_CODE = "error";
	/** method parameter declaration */
	public static final String PARAMETER_OAUTH2_GRANT_TYPE = "grant_type";
	/** method parameter declaration */
	public static final String PARAMETER_OAUTH2_REDIRECT_URI = "redirect_uri";
	/** method parameter declaration */
	public static final String PARAMETER_OAUTH2_REFRESH_TOKEN = "refresh_token";
	/** method parameter declaration */
	public static final String PARAMETER_OAUTH2_RESPONSE_TYPE = "response_type";
	/** method parameter declaration */
	public static final String PARAMETER_OAUTH2_SCOPE = "scope";
	/** method parameter declaration */
	public static final String PARAMETER_OAUTH2_STATE = "state";
	/** method parameter declaration */
	public static final String PARAMETER_SERVICE_TYPE = "service_type";
	/** method parameter declaration */
	public static final String PARAMETER_USER_ID = "user_id";
	/** method parameter declaration */
	public static final String PARAMETER_TWITTER_INCLUDE_ENTITIES = "include_entities";
	/** method parameter declaration */
	public static final String PARAMETER_TWITTER_REDIRECT_URI = "redirect_uri";
	/** method parameter declaration */
	public static final String PARAMETER_TWITTER_SKIP_STATUS = "skip_status";
	
	/* headers */
	/** oauth http authorization header */
	public static final String HEADER_OAUTH_AUTHORIZATION = "Authorization";
	
	/* JSON definitions */
	/** JSON name/object declaration */
	public static final String JSON_NAME_OAUTH2_ACCESS_TOKEN = "access_token";
	/** JSON name/object declaration */
	public static final String JSON_NAME_OAUTH2_EXPIRES_IN = "expires_in";
	/** JSON name/object declaration */
	public static final String JSON_NAME_OAUTH2_REFRESH_TOKEN = "refresh_token";
	/** JSON name/object declaration */
	public static final String JSON_NAME_OAUTH2_TOKEN_TYPE = "token_type";
	
	/* elements */
	/** xml element declaration */
	public static final String ELEMENT_EXTERNAL_ACCOUNT_CONNECTION_LIST = "externalAccountConnectionList";
	/** xml element declaration */
	public static final String ELEMENT_REGISTRATION = "registration";
	/** xml element declaration */
	public static final String ELEMENT_USER_IDENTITY_LIST = "userDetailsList";
	/** xml element declaration */
	public static final String ELEMENT_PASSWORD = "password";
	/** xml element declaration */
	public static final String ELEMENT_REGISTER_PASSWORD = "registerPassword";
	
	/* others */
	/** value coding separator in randomly generated nonce */
	protected static final String NONCE_SEPARATOR = ".-.";

	/**
	 * The grant type of the OAuth2 token.
	 */
	public enum OAuth2GrantType{
		/** oauth2 authorization code (<a href="https://tools.ietf.org/html/rfc6749#section-1.3.1">OAuth 2 authorization code</a>) */
		authorization_code,
		/** oauth2 refresh token (<a href="https://tools.ietf.org/html/rfc6749#section-1.5">OAuth 2 refresh token</a>) */
		refresh_token;
		
		/**
		 * 
		 * @return this grant type as string
		 */
		public String toOAuth2GrantType(){
			return name();
		}
	} // enum OAuth2GrantType
	
	/**
	 * 
	 */
	private Definitions(){
		// nothing needed
	}
}
