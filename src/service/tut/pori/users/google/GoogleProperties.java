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

import java.util.Properties;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.net.URLCodec;
import org.apache.log4j.Logger;

import core.tut.pori.properties.SystemProperty;

/**
 * Properties for Google User Service.
 */
public class GoogleProperties extends SystemProperty{
	/* properties */
	private static final String PROPERTY_SERVICE_USERS_GOOGLE = PROPERTY_SERVICE_PORI+".users.google";
	private static final String PROPERTY_SERVICE_USERS_GOOGLE_CLIENT_ID = PROPERTY_SERVICE_USERS_GOOGLE+".google_client_id";
	private static final String PROPERTY_SERVICE_USERS_GOOGLE_CLIENT_SECRET = PROPERTY_SERVICE_USERS_GOOGLE+".google_client_secret";
	private static final String PROPERTY_SERVICE_USERS_GOOGLE_OAUTH2_REDIRECT_URI = PROPERTY_SERVICE_USERS_GOOGLE+".google_oauth2_redirect_uri";
	private static final String PROPERTY_SERVICE_USERS_GOOGLE_OAUTH2_SCOPE = PROPERTY_SERVICE_USERS_GOOGLE+".google_oauth2_scope";
	private static final String PROPERTY_SERVICE_USERS_GOOGLE_OAUTH2_URI = PROPERTY_SERVICE_USERS_GOOGLE+".google_oauth2_uri";
	private static final String PROPERTY_SERVICE_USERS_GOOGLE_OAUTH2_USER_INFO_URI = PROPERTY_SERVICE_USERS_GOOGLE+".google_oauth2_user_info_uri";
	private String _clientId = null;
	private String _clientSecret = null;
	private String _encodedOAuth2RedirectUri = null;
	private String _oAuth2RedirectUri = null;
	private String _oAuth2Scope = null;
	private String _oAuth2Uri = null;
	private String _oAuth2UserInfoUri = null;

	@Override
	public void initialize(Properties properties) throws IllegalArgumentException{
		_clientId = properties.getProperty(PROPERTY_SERVICE_USERS_GOOGLE_CLIENT_ID);
		_clientSecret = properties.getProperty(PROPERTY_SERVICE_USERS_GOOGLE_CLIENT_SECRET);
		if(_clientId == null || _clientSecret == null){
			throw new IllegalArgumentException(PROPERTY_SERVICE_USERS_GOOGLE_CLIENT_ID+" or "+PROPERTY_SERVICE_USERS_GOOGLE_CLIENT_SECRET+" is missing.");
		}
		_oAuth2Scope = properties.getProperty(PROPERTY_SERVICE_USERS_GOOGLE_OAUTH2_SCOPE);
		_oAuth2Uri = properties.getProperty(PROPERTY_SERVICE_USERS_GOOGLE_OAUTH2_URI);
		_oAuth2RedirectUri = properties.getProperty(PROPERTY_SERVICE_USERS_GOOGLE_OAUTH2_REDIRECT_URI);
		_oAuth2UserInfoUri = properties.getProperty(PROPERTY_SERVICE_USERS_GOOGLE_OAUTH2_USER_INFO_URI);
		if(_oAuth2Uri == null || _oAuth2Scope == null || _oAuth2RedirectUri == null || _oAuth2UserInfoUri == null){
			throw new IllegalArgumentException(PROPERTY_SERVICE_USERS_GOOGLE_OAUTH2_USER_INFO_URI+", "+PROPERTY_SERVICE_USERS_GOOGLE_OAUTH2_URI+", "+PROPERTY_SERVICE_USERS_GOOGLE_OAUTH2_REDIRECT_URI+" or "+PROPERTY_SERVICE_USERS_GOOGLE_OAUTH2_SCOPE+" is missing.");
		}
		try {
			_encodedOAuth2RedirectUri = (new URLCodec(core.tut.pori.http.Definitions.ENCODING_UTF8)).encode(_oAuth2RedirectUri);
		} catch (EncoderException ex) {
			Logger.getLogger(getClass()).error(ex);
			throw new IllegalArgumentException("Bad "+PROPERTY_SERVICE_USERS_GOOGLE_OAUTH2_REDIRECT_URI);
		}
	}

	/**
	 * @return the clientId
	 */
	public String getClientId() {
		return _clientId;
	}

	/**
	 * @return the clientSecret
	 */
	public String getClientSecret() {
		return _clientSecret;
	}

	/**
	 * @return the oAuth2Uri
	 */
	public String getoAuth2Uri() {
		return _oAuth2Uri;
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
	 * @return the oAuth2UserInfoUri
	 */
	public String getoAuth2UserInfoUri() {
		return _oAuth2UserInfoUri;
	}
}
