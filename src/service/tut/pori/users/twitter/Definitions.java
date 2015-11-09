/**
 * Copyright 2015 Tampere University of Technology, Pori Department
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

/**
 * Definitions for users/twitter package.
 *
 */
public final class Definitions {
	/* headers */
	/** oauth http authorization header */
	public static final String HEADER_OAUTH_AUTHORIZATION = "Authorization";
	
	/* local methods */
	/** local service method declaration */
	public static final String METHOD_LOGIN = "login";
	/** local service method declaration */
	public static final String METHOD_OAUTH_AUTHORIZATION_REDIRECT = "authorize";
	/** local service method declaration */
	public static final String METHOD_OAUTH_AUTHORIZE_CALLBACK = "oAuthAuthorizeCallback";
	/** local service method declaration */
	public static final String METHOD_OAUTH_LOGIN_CALLBACK = "oAuthLoginCallback";
	/** local service method declaration */
	public static final String METHOD_OAUTH_REGISTER_CALLBACK = "oAuthRegisterCallback";
	/** local service method declaration */
	public static final String METHOD_UNAUTHORIZE = "unauthorize";
	
	/* remote methods */
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
	public static final String PARAMETER_TWITTER_INCLUDE_ENTITIES = "include_entities";
	/** method parameter declaration */
	public static final String PARAMETER_TWITTER_REDIRECT_URI = "redirect_uri";
	/** method parameter declaration */
	public static final String PARAMETER_TWITTER_SKIP_STATUS = "skip_status";
	
	/* services */
	/** service name declaration */
	public static final String SERVICE_USERS_TWITTER = "twitter";
	
	/**
	 * 
	 */
	private Definitions(){
		// nothing needed
	}
}
