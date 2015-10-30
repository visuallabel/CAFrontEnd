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
package service.tut.pori.users.facebook;

import java.util.Properties;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.net.URLCodec;
import org.apache.log4j.Logger;

import core.tut.pori.properties.SystemProperty;

/**
 * System properties for Facebook User Service.
 */
public class FacebookProperties extends SystemProperty{
	/* properties */
	private static final String PROPERTY_SERVICE_USERS_FACEBOOK = PROPERTY_SERVICE_PORI+".users.facebook";
	private static final String PROPERTY_SERVICE_USERS_FACEBOOK_APPLICATION_ID = PROPERTY_SERVICE_USERS_FACEBOOK+".facebook_application_id";
	private static final String PROPERTY_SERVICE_USERS_FACEBOOK_OAUTH2_AUTH_URI = PROPERTY_SERVICE_USERS_FACEBOOK+".facebook_oauth2_auth_uri";
	private static final String PROPERTY_SERVICE_USERS_FACEBOOK_OAUTH2_REDIRECT_URI = PROPERTY_SERVICE_USERS_FACEBOOK+".facebook_oauth2_redirect_uri";
	private static final String PROPERTY_SERVICE_USERS_FACEBOOK_OAUTH2_SCOPE = PROPERTY_SERVICE_USERS_FACEBOOK+".facebook_oauth2_scope";
	private static final String PROPERTY_SERVICE_USERS_FACEBOOK_SHARED_KEY = PROPERTY_SERVICE_USERS_FACEBOOK+".facebook_shared_key";
	private static final String PROPERTY_SERVICE_USERS_FACEBOOK_OAUTH2_TOKEN_URI = PROPERTY_SERVICE_USERS_FACEBOOK+".facebook_oauth2_token_uri";
	private static final String PROPERTY_SERVICE_USERS_FACEBOOK_TOKEN_AUTOREFRESH = PROPERTY_SERVICE_USERS_FACEBOOK+".facebook_token_autorefresh";
	private static final String PROPERTY_SERVICE_USERS_GOOGLE_OAUTH2_USER_INFO_URI = PROPERTY_SERVICE_USERS_FACEBOOK+".facebook_oauth2_user_info_uri";
	private String _applicationId = null;
	private String _encodedOAuth2RedirectUri = null;
	private String _oAuth2AuthUri = null;
	private String _oAuth2RedirectUri = null;
	private String _oAuth2Scope = null;
	private String _oAuth2TokenUri = null;
	private String _oAuth2UserInfoUri = null;
	private String _sharedKey = null;
	private long _tokenAutorefresh = -1;
	
	@Override
	public void initialize(Properties properties) throws IllegalArgumentException{
		_applicationId = properties.getProperty(PROPERTY_SERVICE_USERS_FACEBOOK_APPLICATION_ID);
		_sharedKey = properties.getProperty(PROPERTY_SERVICE_USERS_FACEBOOK_SHARED_KEY);
		if(_applicationId == null || _sharedKey == null){
			throw new IllegalArgumentException(PROPERTY_SERVICE_USERS_FACEBOOK_APPLICATION_ID+" or "+PROPERTY_SERVICE_USERS_FACEBOOK_SHARED_KEY+" is missing.");
		}
		_oAuth2Scope = properties.getProperty(PROPERTY_SERVICE_USERS_FACEBOOK_OAUTH2_SCOPE);
		_oAuth2AuthUri = properties.getProperty(PROPERTY_SERVICE_USERS_FACEBOOK_OAUTH2_AUTH_URI);
		_oAuth2TokenUri = properties.getProperty(PROPERTY_SERVICE_USERS_FACEBOOK_OAUTH2_TOKEN_URI);
		_oAuth2RedirectUri = properties.getProperty(PROPERTY_SERVICE_USERS_FACEBOOK_OAUTH2_REDIRECT_URI);
		_oAuth2UserInfoUri = properties.getProperty(PROPERTY_SERVICE_USERS_GOOGLE_OAUTH2_USER_INFO_URI);
		if(_oAuth2AuthUri == null || _oAuth2Scope == null || _oAuth2RedirectUri == null || _oAuth2TokenUri == null || _oAuth2UserInfoUri == null){
			throw new IllegalArgumentException(PROPERTY_SERVICE_USERS_GOOGLE_OAUTH2_USER_INFO_URI+", "+PROPERTY_SERVICE_USERS_FACEBOOK_OAUTH2_TOKEN_URI+", "+PROPERTY_SERVICE_USERS_FACEBOOK_OAUTH2_AUTH_URI+", "+PROPERTY_SERVICE_USERS_FACEBOOK_OAUTH2_REDIRECT_URI+" or "+PROPERTY_SERVICE_USERS_FACEBOOK_OAUTH2_SCOPE+" is missing.");
		}
		try {
			_encodedOAuth2RedirectUri  = (new URLCodec(core.tut.pori.http.Definitions.ENCODING_UTF8)).encode(_oAuth2RedirectUri);
		} catch (EncoderException ex) {
			Logger.getLogger(getClass()).error(ex, ex);
			throw new IllegalArgumentException("Bad "+PROPERTY_SERVICE_USERS_FACEBOOK_OAUTH2_REDIRECT_URI);
		}
		try{
			_tokenAutorefresh = Long.parseLong(properties.getProperty(PROPERTY_SERVICE_USERS_FACEBOOK_TOKEN_AUTOREFRESH));
		} catch (NumberFormatException ex){
			Logger.getLogger(getClass()).error(ex, ex);
			throw new IllegalArgumentException("Bad "+PROPERTY_SERVICE_USERS_FACEBOOK_TOKEN_AUTOREFRESH);
		}
	}

	/**
	 * @return the applicationId
	 */
	public String getApplicationId() {
		return _applicationId;
	}

	/**
	 * @return the sharedKey
	 */
	public String getSharedKey() {
		return _sharedKey;
	}

	/**
	 * @return the oAuth2Scope
	 */
	public String getoAuth2Scope() {
		return _oAuth2Scope;
	}

	/**
	 * @return the oAuth2RedirectUri
	 */
	public String getoAuth2RedirectUri() {
		return _oAuth2RedirectUri;
	}

	/**
	 * @return the encodedOAuth2RedirectUri
	 */
	public String getEncodedOAuth2RedirectUri() {
		return _encodedOAuth2RedirectUri;
	}

	/**
	 * @return the oAuth2AuthUri
	 */
	public String getoAuth2AuthUri() {
		return _oAuth2AuthUri;
	}

	/**
	 * @return the oAuth2TokenUri
	 */
	public String getoAuth2TokenUri() {
		return _oAuth2TokenUri;
	}

	/**
	 * @return the tokenAutorefresh
	 */
	public long getTokenAutorefresh() {
		return _tokenAutorefresh;
	}

	/**
	 * @return the oAuth2UserInfoUri
	 */
	public String getoAuth2UserInfoUri() {
		return _oAuth2UserInfoUri;
	}
}
