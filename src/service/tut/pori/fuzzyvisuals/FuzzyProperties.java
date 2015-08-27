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
package service.tut.pori.fuzzyvisuals;

import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import core.tut.pori.properties.SystemProperty;

/**
 * Properties for fuzzy visuals
 */
public class FuzzyProperties extends SystemProperty {
	private static final String PROPERTY_SERVICE_TUT_PORI_FV = PROPERTY_SERVICE_PORI+".fuzzyvisuals";
	private static final String PROPERTY_SERVICE_TUT_PORI_FV_AUTH_PASSWORD = PROPERTY_SERVICE_TUT_PORI_FV+".auth_username";
	private static final String PROPERTY_SERVICE_TUT_PORI_FV_AUTH_USERNAME = PROPERTY_SERVICE_TUT_PORI_FV+".auth_password";
	private String _authPassword = null;
	private String _authUsername = null;

	@Override
	public void initialize(Properties properties) throws IllegalArgumentException {
		String temp = properties.getProperty(PROPERTY_SERVICE_TUT_PORI_FV_AUTH_USERNAME);
		if(StringUtils.isBlank(temp)){
			Logger.getLogger(getClass()).debug("No "+PROPERTY_SERVICE_TUT_PORI_FV_AUTH_USERNAME+" set.");
			return; // do not bother checking for password if no username is given
		}
		_authUsername = temp;
		
		_authPassword = properties.getProperty(PROPERTY_SERVICE_TUT_PORI_FV_AUTH_PASSWORD);
		if(StringUtils.isBlank(_authPassword)){
			_authUsername = null;
			throw new IllegalArgumentException("Bad "+PROPERTY_SERVICE_TUT_PORI_FV_AUTH_PASSWORD);
		}
	}

	@Override
	public String getPropertyFilePath() {
		return CONFIGURATION_FILE_PATH+Definitions.PROPERTY_FILE;
	}

	/**
	 * @return HTTP basic auth username or null if not set
	 */
	public String getAuthPassword() {
		return _authPassword;
	}

	/**
	 * @return HTTP basic auth password or null if not set
	 */
	public String getAuthUsername() {
		return _authUsername;
	}
}
