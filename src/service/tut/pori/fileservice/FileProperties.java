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
package service.tut.pori.fileservice;

import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

import core.tut.pori.properties.SystemProperty;

/**
 * System properties for File Service.
 */
public class FileProperties extends SystemProperty {
	private static final String PROPERTY_SERVICE_PORI_FILE_SERVICE = PROPERTY_SERVICE_PORI+".fileservice";
	private static final String PROPERTY_SERVICE_PORI_FILE_SERVICE_FILE_PATH = PROPERTY_SERVICE_PORI_FILE_SERVICE+".file_path";
	private static final String PROPERTY_SERVICE_PORI_FILE_SERVICE_URI_PATH = PROPERTY_SERVICE_PORI_FILE_SERVICE+".uri_path";
	private String _filePath = null;
	private String _uriPath = null;

	@Override
	public void initialize(Properties properties) throws IllegalArgumentException {
		_filePath = properties.getProperty(PROPERTY_SERVICE_PORI_FILE_SERVICE_FILE_PATH);
		_uriPath = properties.getProperty(PROPERTY_SERVICE_PORI_FILE_SERVICE_URI_PATH);
		if(StringUtils.isBlank(_filePath) || StringUtils.isBlank(_uriPath)){
			throw new IllegalArgumentException("Bad "+PROPERTY_SERVICE_PORI_FILE_SERVICE_FILE_PATH+" or "+PROPERTY_SERVICE_PORI_FILE_SERVICE_URI_PATH);
		}
	}

	/**
	 * @return the file system path where the actual files are located
	 */
	public String getFilePath() {
		return _filePath;
	}

	/**
	 * @return the uriPath
	 */
	public String getUriPath() {
		return _uriPath;
	}
}
