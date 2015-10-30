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
package service.tut.pori.fileservice.reference;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.RandomStringUtils;

import service.tut.pori.contentanalysis.reference.CAXMLObjectCreator;
import service.tut.pori.fileservice.File;
import service.tut.pori.fileservice.FileHandler;
import service.tut.pori.fileservice.FileList;
import service.tut.pori.fileservice.FileProperties;
import twitter4j.Logger;
import core.tut.pori.context.ServiceInitializer;
import core.tut.pori.users.UserIdentity;

/**
 * 
 * class that can be used to created example objects/object lists
 *
 * Note: this class is NOT guaranteed to be thread-safe in any way.
 *
 */
public class FSXMLObjectCreator {
	private static final int FILENAME_LENGTH = 32;
	private static final Logger LOGGER = Logger.getLogger(FSXMLObjectCreator.class);
	private CAXMLObjectCreator _CACreator = null;
	private FileHandler _fileHander = new FileHandler();

	/**
	 * 
	 * @param seed for random generator, or null to use default (system time in nanoseconds)
	 */
	public FSXMLObjectCreator(Long seed){
		if(seed == null){
			seed = System.nanoTime();
		}
		_CACreator = new CAXMLObjectCreator(seed);
	}
	
	/**
	 * @see service.tut.pori.fileservice.FileHandler#generateFilename(String)
	 * 
	 * @param filename
	 * @return random filename
	 */
	public String generateFilename(String filename) {
		return _fileHander.generateFilename(filename);
	}

	/**
	 * 
	 * @return random file id
	 */
	public Long generateFileId() {
		return Math.abs(_CACreator.getRandom().nextLong());
	}

	/**
	 * @see service.tut.pori.contentanalysis.reference.CAXMLObjectCreator#createUserIdentity()
	 * 
	 * @return random user identity
	 */
	public UserIdentity createUserIdentity() {
		return _CACreator.createUserIdentity();
	}

	/**
	 * 
	 * @param userIdentity if null or invalid, new one will be randomly generated
	 * @param fileCount if file ids are given and the count is lower than the file id count, only the count amount of files will be returned. If count is more than file id count, only file id count of files will be returned.
	 * @param fileIds optional file ids
	 * @return list of files or null if 1 or less files was requested
	 */
	public FileList generateFileList(UserIdentity userIdentity, int fileCount, long[] fileIds) {
		if(fileCount < 1){
			LOGGER.debug("Count < 1.");
			return null;
		}
		
		if(!UserIdentity.isValid(userIdentity)){
			userIdentity = createUserIdentity();
		}
		
		String filePath = ServiceInitializer.getPropertyHandler().getSystemProperties(FileProperties.class).getUriPath();
		List<File> files = new ArrayList<>();
		if(ArrayUtils.isEmpty(fileIds)){
			long fileId = 0;
			for(int i=0;i<fileCount;++i){
				files.add(generateFile(userIdentity, ++fileId, filePath));
			}
		}else{
			for(int i=0;i<fileCount && i<fileIds.length;++i){
				files.add(generateFile(userIdentity, fileIds[i], filePath));
			}
		}
		
		return FileList.getFileList(files);
	}
	
	/**
	 * 
	 * @return random file
	 */
	public File generateFile(){
		return generateFile(createUserIdentity(), Math.abs(_CACreator.getRandom().nextLong()), ServiceInitializer.getPropertyHandler().getSystemProperties(FileProperties.class).getUriPath());
	}
	
	/**
	 * 
	 * @param userId
	 * @param fileId
	 * @param filePath 
	 * @return random file
	 */
	public File generateFile(UserIdentity userId, long fileId, String filePath){
		File file = new File();
		file.setFileId(fileId);
		String filename = RandomStringUtils.randomAlphanumeric(FILENAME_LENGTH);
		String savedName = _fileHander.generateFilename(filename);
		file.setName(filename);
		file.setSavedName(savedName);
		file.setUrl(filePath+savedName);
		return file;
	}
}
