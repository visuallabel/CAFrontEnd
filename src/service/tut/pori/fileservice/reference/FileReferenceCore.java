/**
 * Copyright 2015 Tampere University of Technology, Pori Unit
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
package service.tut.pori.fileservice.reference;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import service.tut.pori.fileservice.Definitions;
import service.tut.pori.fileservice.File;
import service.tut.pori.fileservice.FileList;
import service.tut.pori.fileservice.FileProperties;
import core.tut.pori.context.ServiceInitializer;
import core.tut.pori.http.parameters.Limits;
import core.tut.pori.users.UserIdentity;

/**
 * The reference implementations for File Service.
 *
 */
public final class FileReferenceCore {
	private static final int BUFFER_SIZE = 8192;
	private static final FSXMLObjectCreator CREATOR = new FSXMLObjectCreator(null);
	private static final Logger LOGGER = Logger.getLogger(FileReferenceCore.class);
	private static final int MAX_FILES = 100;
	
	/**
	 * 
	 */
	private FileReferenceCore(){
		// nothing needed
	}
	
	/**
	 * 
	 * @param authenticatedUser
	 * @param fileId
	 */
	public static void deleteFile(UserIdentity authenticatedUser, Long fileId) {
		LOGGER.info((authenticatedUser == null ? "No logged in user." : "Ignoring the logged in user, id: "+authenticatedUser.getUserId()));	// only notify of the logged in status
	}

	/**
	 * Simulates uploading a file. The actual will is discarded and the returned data is for reference only.
	 * 
	 * @param authenticatedUser
	 * @param file
	 * @param filename
	 * @return file object representing the added file
	 * @throws IllegalArgumentException on failure
	 */
	public static File addFile(UserIdentity authenticatedUser, InputStream file, String filename) throws IllegalArgumentException {
		if(UserIdentity.isValid(authenticatedUser)){
			LOGGER.info("Logged in user, id: "+authenticatedUser.getUserId());
		}else{
			LOGGER.info("No logged in user.");
			authenticatedUser = CREATOR.createUserIdentity();
		}
		int byteCount = 0;
		byte[] bytes = new byte[BUFFER_SIZE];
		int read = 0;
		try {
			while((read = IOUtils.read(file, bytes)) > 0){ // read fully, but discard all
				byteCount += read;
			}
		} catch (IOException ex) {
			LOGGER.error(ex, ex);
			throw new IllegalArgumentException("I/O error occurred.");
		}
		File savedFile = new File();
		if(StringUtils.isBlank(filename)){
			LOGGER.debug("No filename given, bytes read: "+byteCount);
		}else{
			LOGGER.debug("Filename: "+filename+", bytes read: "+byteCount);
			savedFile.setName(filename);
		}
		String savedName = CREATOR.generateFilename(filename);
		savedFile.setSavedName(savedName);
		savedFile.setUserId(authenticatedUser);
		savedFile.setFileId(CREATOR.generateFileId());
		savedFile.setUrl(ServiceInitializer.getPropertyHandler().getSystemProperties(FileProperties.class).getUriPath()+savedName);
		return savedFile;
	}

	/**
	 * 
	 * @param authenticatedUser
	 * @param fileIds
	 * @param limits
	 * @return example file list
	 */
	public static FileList listFiles(UserIdentity authenticatedUser, long[] fileIds, Limits limits) {
		if(UserIdentity.isValid(authenticatedUser)){
			LOGGER.info("Logged in user, id: "+authenticatedUser.getUserId());
		}else{
			LOGGER.info("No logged in user.");
			authenticatedUser = CREATOR.createUserIdentity();
		}
		
		int maxFiles = limits.getMaxItems(Definitions.ELEMENT_FILELIST);
		if(maxFiles >= Limits.DEFAULT_MAX_ITEMS){
			LOGGER.info("Maximum limit given, capping to "+MAX_FILES);
			maxFiles = MAX_FILES;
		}
		
		return CREATOR.generateFileList(authenticatedUser, maxFiles, fileIds);
	}

	/**
	 * 
	 * @param limits
	 * @return generated file list
	 */
	public static FileList generateFileList(Limits limits) {
		int count = limits.getMaxItems(Definitions.ELEMENT_FILELIST);
		if(count >= Limits.DEFAULT_MAX_ITEMS){
			LOGGER.debug("Max files was >= "+Limits.DEFAULT_MAX_ITEMS+". Limiting to "+MAX_FILES+".");
			count = MAX_FILES;
		}
		return CREATOR.generateFileList(null, count, null);
	}

	/**
	 * 
	 * @return generated file
	 */
	public static File generateFile() {
		return CREATOR.generateFile();
	}
}
