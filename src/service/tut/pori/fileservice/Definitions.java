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
package service.tut.pori.fileservice;

/**
 * Definitions for fileservice package.
 */
public final class Definitions {
	/**
	 * 
	 */
	private Definitions(){
		// nothing needed
	}
	
	/* services */
	/** service name declaration */
	public static final String SERVICE_FS = "fs";
	
	/* methods */
	/** service method declaration */
	public static final String METHOD_ADD_FILE = "addFile";
	/** service method declaration */
	public static final String METHOD_DELETE_FILE = "deleteFile";
	/** service method declaration */
	public static final String METHOD_LIST_FILES = "listFiles";
	
	/* elements */
	/** xml element declaration */
	public static final String ELEMENT_FILELIST  = "fileList";
	/** xml element declaration */
	public static final String ELEMENT_FILE = "file";
	/** xml element declaration */
	public static final String ELEMENT_FILE_ID = "fileId";
	/** xml element declaration */
	public static final String ELEMENT_NAME = "name";
	/** xml element declaration */
	public static final String ELEMENT_URL = "url";
	
	/* parameters */
	/** method parameter declaration */
	public static final String PARAMETER_FILE_ID = "file_id";
	/** method parameter declaration */
	public static final String PARAMETER_FILE_NAME = "filename";

	/** property file for the service */
	protected static final String PROPERTY_FILE = "fs.properties";
}
