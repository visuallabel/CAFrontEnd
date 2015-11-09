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
package service.tut.pori.users.google;

/**
 * Definitions for users/google package.
 *
 */
public final class Definitions {

	/* JSON definitions */
	/** JSON name/object declaration */
	public static final String JSON_NAME_OAUTH2_ACCESS_TOKEN = "access_token";
	/** JSON name/object declaration */
	public static final String JSON_NAME_OAUTH2_EXPIRES_IN = "expires_in";
	/** JSON name/object declaration */
	public static final String JSON_NAME_OAUTH2_REFRESH_TOKEN = "refresh_token";
	/** JSON name/object declaration */
	public static final String JSON_NAME_OAUTH2_TOKEN_TYPE = "token_type";
	
	/* local methods */
	/** local service method declaration */
	public static final String METHOD_LOGIN = "login";
	/** local service method declaration */
	public static final String METHOD_OAUTH2_AUTHORIZATION_REDIRECT = "authorize";
	/** local service method declaration */
	public static final String METHOD_OAUTH2_CALLBACK = "oAuth2callback";
	/** local service method declaration */
	public static final String METHOD_UNAUTHORIZE = "unauthorize";
	
	/* remote methods */
	/** remote service method declaration */
	public static final String METHOD_GOOGLE_AUTH = "auth";
	/** remote service method declaration */
	public static final String METHOD_GOOGLE_REVOKE = "revoke";
	/** remote service method declaration */
	public static final String METHOD_GOOGLE_TOKEN = "token";
	/** remote service method declaration */
	public static final String METHOD_GOOGLE_USER_INFO = "userinfo";
	
	/* parameters */
	/** method parameter declaration */
	public static final String PARAMETER_GOOGLE_ACCESS_TYPE = "access_type";
	/** method parameter declaration */
	public static final String PARAMETER_GOOGLE_APPROVAL_PROMPT = "approval_prompt";
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
	public static final String PARAMETER_GOOGLE_TOKEN = "token";
	
	/* services */
	/** service name declaration */
	public static final String SERVICE_USERS_GOOGLE = "google";
	
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
