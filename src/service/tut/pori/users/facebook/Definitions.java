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
package service.tut.pori.users.facebook;

/**
 * Definitions for users/facebook package.
 *
 */
public final class Definitions {
	
	/* local methods */
	/** local service method declaration */
	public static final String METHOD_OAUTH2_AUTHORIZATION_REDIRECT = "authorize";
	/** local service method declaration */
	public static final String METHOD_OAUTH2_CALLBACK = "oAuth2callback";
	/** local service method declaration */
	public static final String METHOD_LOGIN = "login";
	/** local service method declaration */
	public static final String METHOD_UNAUTHORIZE = "unauthorize";
	
	/* remote methods */
	/** remote service method declaration */
	public static final String METHOD_FACEBOOK_ACCESS_TOKEN = "access_token";
	/** remote service method declaration */
	public static final String METHOD_FACEBOOK_AUTH = "oauth";
	
	/* parameters */
	/** method parameter declaration */
	public static final String PARAMETER_FACEBOOK_EXCHANGE_TOKEN = "fb_exchange_token";
	/** method parameter declaration */
	public static final String PARAMETER_FACEBOOK_HACK_REDIRECT_URI = "fb_redirect_uri";
	/** method parameter declaration */
	public static final String PARAMETER_OAUTH2_ACCESS_TOKEN = "access_token";
	/** method parameter declaration */
	public static final String PARAMETER_OAUTH2_AUTHORIZATION_CODE = "code";
	/** method parameter declaration */
	public static final String PARAMETER_OAUTH2_CLIENT_ID = "client_id";
	/** method parameter declaration */
	public static final String PARAMETER_OAUTH2_CLIENT_SECRET = "client_secret";
	/** method parameter declaration */
	public static final String PARAMETER_OAUTH2_GRANT_TYPE = "grant_type";
	/** method parameter declaration */
	public static final String PARAMETER_OAUTH2_ERROR_CODE = "error";
	/** method parameter declaration */
	public static final String PARAMETER_OAUTH2_REDIRECT_URI = "redirect_uri";
	/** method parameter declaration */
	public static final String PARAMETER_OAUTH2_RESPONSE_TYPE = "response_type";
	/** method parameter declaration */
	public static final String PARAMETER_OAUTH2_SCOPE = "scope";
	/** method parameter declaration */
	public static final String PARAMETER_OAUTH2_STATE = "state";
	
	/* services */
	/** service name declaration */
	public static final String SERVICE_USERS_FACEBOOK = "facebook";
	
	/* common */
	/** value coding separator in randomly generated nonce */
	protected static final String NONCE_SEPARATOR = ".-.";

	/**
	 * 
	 */
	private Definitions(){
		// nothing needed
	}
}
