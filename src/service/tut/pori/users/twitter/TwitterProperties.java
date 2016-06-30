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

import java.util.Properties;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import core.tut.pori.context.ServiceInitializer;
import core.tut.pori.properties.SystemProperty;

/**
 * Twitter User Service properties.
 */
public class TwitterProperties extends SystemProperty {
	/* properties */
	private static final String PROPERTY_SERVICE_USERS_TWITTER = PROPERTY_SERVICE_PORI+".users.twitter";
	private static final String PROPERTY_SERVICE_USERS_TWITTER_API_KEY = PROPERTY_SERVICE_USERS_TWITTER+".twitter_api_key";
	private static final String PROPERTY_SERVICE_USERS_TWITTER_CLIENT_SECRET = PROPERTY_SERVICE_USERS_TWITTER+".twitter_client_secret";
	private static final String PROPERTY_SERVICE_USERS_TWITTER_OAUTH_AUTHORIZE_REDIRECT_URI = PROPERTY_SERVICE_USERS_TWITTER+".twitter_oauth_authorize_redirect_uri";
	private static final String PROPERTY_SERVICE_USERS_TWITTER_OAUTH_LOGIN_REDIRECT_URI = PROPERTY_SERVICE_USERS_TWITTER+".twitter_oauth_login_redirect_uri";
	private static final String PROPERTY_SERVICE_USERS_TWITTER_OAUTH_REGISTER_REDIRECT_URI = PROPERTY_SERVICE_USERS_TWITTER+".twitter_oauth_register_redirect_uri";
	private static final String PROPERTY_SERVICE_USERS_TWITTER_OAUTH_URI = PROPERTY_SERVICE_USERS_TWITTER+".twitter_oauth_uri";
	private static final String PROPERTY_SERVICE_USERS_TWITTER_USER_INFO_URI = PROPERTY_SERVICE_USERS_TWITTER+".twitter_user_info_uri";
	private static final String PROPERTY_SERVICE_USERS_TWITTER_DEBUG_ENABLED = PROPERTY_SERVICE_USERS_TWITTER+".twitter_debug_enabled";
	/* members */
	private String _apiKey = null;
	private String _clientSecret = null;
	private boolean _debugEnabled = false;
	private String _encodedApiKey = null;
	private String _encodedClientSecret = null;
	private String _encodedOAuthAuthorizeRedirectUri = null;
	private String _encodedOAuthLoginRedirectUri = null;
	private String _encodedOAuthRegisterRedirectUri = null;
	private String _oAuthAuthorizeRedirectUri = null;
	private String _oAuthLoginRedirectUri = null;
	private String _oAuthRegisterRedirectUri = null;
	private String _oAuthUri = null;
	private String _userInfoUri = null;

	@Override
	public void initialize(Properties properties) throws IllegalArgumentException {
		_apiKey = properties.getProperty(PROPERTY_SERVICE_USERS_TWITTER_API_KEY);
		_clientSecret = properties.getProperty(PROPERTY_SERVICE_USERS_TWITTER_CLIENT_SECRET);
		if(_apiKey == null || _clientSecret == null){
			throw new IllegalArgumentException(PROPERTY_SERVICE_USERS_TWITTER_API_KEY+" or "+PROPERTY_SERVICE_USERS_TWITTER_CLIENT_SECRET+" is missing.");
		}
		_oAuthUri = properties.getProperty(PROPERTY_SERVICE_USERS_TWITTER_OAUTH_URI);
		_oAuthLoginRedirectUri = properties.getProperty(PROPERTY_SERVICE_USERS_TWITTER_OAUTH_LOGIN_REDIRECT_URI);
		_oAuthRegisterRedirectUri = properties.getProperty(PROPERTY_SERVICE_USERS_TWITTER_OAUTH_REGISTER_REDIRECT_URI);
		_oAuthAuthorizeRedirectUri = properties.getProperty(PROPERTY_SERVICE_USERS_TWITTER_OAUTH_AUTHORIZE_REDIRECT_URI);
		_userInfoUri = properties.getProperty(PROPERTY_SERVICE_USERS_TWITTER_USER_INFO_URI);
		if(_oAuthAuthorizeRedirectUri == null || _oAuthUri == null || _oAuthRegisterRedirectUri == null || _oAuthAuthorizeRedirectUri == null || _userInfoUri == null){
			throw new IllegalArgumentException(PROPERTY_SERVICE_USERS_TWITTER_USER_INFO_URI+", "+PROPERTY_SERVICE_USERS_TWITTER_OAUTH_LOGIN_REDIRECT_URI+", "+PROPERTY_SERVICE_USERS_TWITTER_OAUTH_REGISTER_REDIRECT_URI+", "+PROPERTY_SERVICE_USERS_TWITTER_OAUTH_AUTHORIZE_REDIRECT_URI+" or "+PROPERTY_SERVICE_USERS_TWITTER_OAUTH_URI+" is missing.");
		}
		try {
			URLCodec codec = new URLCodec(core.tut.pori.http.Definitions.ENCODING_UTF8);
			_encodedOAuthLoginRedirectUri = codec.encode(_oAuthLoginRedirectUri);
			_encodedOAuthRegisterRedirectUri = codec.encode(_oAuthRegisterRedirectUri);
			_encodedOAuthAuthorizeRedirectUri = codec.encode(_oAuthAuthorizeRedirectUri);
			_encodedApiKey = codec.encode(_apiKey);
			_encodedClientSecret = codec.encode(_clientSecret);
		} catch (EncoderException ex) {
			Logger.getLogger(getClass()).error(ex);
			throw new IllegalArgumentException("Bad "+PROPERTY_SERVICE_USERS_TWITTER_API_KEY+", "+PROPERTY_SERVICE_USERS_TWITTER_CLIENT_SECRET+", "+PROPERTY_SERVICE_USERS_TWITTER_OAUTH_LOGIN_REDIRECT_URI+", "+PROPERTY_SERVICE_USERS_TWITTER_OAUTH_REGISTER_REDIRECT_URI+" or "+PROPERTY_SERVICE_USERS_TWITTER_OAUTH_AUTHORIZE_REDIRECT_URI);
		}
		String debugEnabled = properties.getProperty(PROPERTY_SERVICE_USERS_TWITTER_DEBUG_ENABLED);
		if(StringUtils.isBlank(debugEnabled)){
			throw new IllegalArgumentException("Bad "+PROPERTY_SERVICE_USERS_TWITTER_DEBUG_ENABLED);
		}
		_debugEnabled = BooleanUtils.toBoolean(debugEnabled);
	}

	/**
	 * @return the apiKey
	 */
	public String getApiKey() {
		return _apiKey;
	}

	/**
	 * @return the clientSecret
	 */
	public String getClientSecret() {
		return _clientSecret;
	}

	/**
	 * @return the encodedOAuthAuthorizeRedirectUri
	 */
	public String getEncodedOAuthAuthorizeRedirectUri() {
		return _encodedOAuthAuthorizeRedirectUri;
	}

	/**
	 * @return the oAuthAuthorizeRedirectUri
	 */
	public String getoAuthAuthorizeRedirectUri() {
		return _oAuthAuthorizeRedirectUri;
	}

	/**
	 * @return the oAuthUri
	 */
	public String getoAuthUri() {
		return _oAuthUri;
	}

	/**
	 * @return the userInfoUri
	 */
	public String getUserInfoUri() {
		return _userInfoUri;
	}

	/**
	 * @return the encodedOAuthLoginRedirectUri
	 */
	public String getEncodedOAuthLoginRedirectUri() {
		return _encodedOAuthLoginRedirectUri;
	}

	/**
	 * @return the encodedOAuthRegisterRedirectUri
	 */
	public String getEncodedOAuthRegisterRedirectUri() {
		return _encodedOAuthRegisterRedirectUri;
	}

	/**
	 * @return the debugEnabled
	 */
	public boolean isDebugEnabled() {
		return _debugEnabled;
	}

	/**
	 * @return the oAuthLoginRedirectUri
	 */
	public String getoAuthLoginRedirectUri() {
		return _oAuthLoginRedirectUri;
	}

	/**
	 * @return the oAuthRegisterRedirectUri
	 */
	public String getoAuthRegisterRedirectUri() {
		return _oAuthRegisterRedirectUri;
	}

	/**
	 * @return the encodedApiKey
	 */
	public String getEncodedApiKey() {
		return _encodedApiKey;
	}

	/**
	 * @return the encodedClientSecret
	 */
	public String getEncodedClientSecret() {
		return _encodedClientSecret;
	}
	
	@Override
	public String getPropertyFilePath() {
		return ServiceInitializer.getConfigHandler().getPropertyFilePath()+"twitter.properties";
	}
}
