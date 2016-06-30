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

import java.io.InputStream;

import org.apache.log4j.Logger;

import core.tut.pori.context.ServiceInitializer;
import core.tut.pori.http.parameters.Limits;
import core.tut.pori.users.UserIdentity;

/**
 * File Service core methods.
 */
public final class FileCore {
	private static final FileHandler FILE_HANDLER = new FileHandler();
	private static final Logger LOGGER = Logger.getLogger(FileCore.class);
	
	/**
	 * 
	 */
	private FileCore(){
		// nothing needed
	}

	/**
	 * 
	 * @param authenticatedUser
	 * @param file
	 * @param filename optional original filename. The real filename used to store the file on the system will be generated, thus giving filename as a parameter is optional.
	 * @return details of the added file or null on failure
	 */
	public static File addFile(UserIdentity authenticatedUser, InputStream file, String filename) {
		FileProperties fp = ServiceInitializer.getPropertyHandler().getSystemProperties(FileProperties.class);
		
		String savedName = FILE_HANDLER.generateFilename(filename);
		FileDAO fileDAO = ServiceInitializer.getDAOHandler().getDAO(FileDAO.class);
		File savedFile = new File();
		savedFile.setSavedName(savedName);
		savedFile.setName(filename);
		savedFile.setUserId(authenticatedUser);
		fileDAO.save(savedFile); // save first to reserve the generated savedName in the database table, in theory this may also throw data violation exception if the name is already in use, though in practice that'll never happen
		
		if(!FILE_HANDLER.save(fp.getFilePath()+savedName, file)){ // save the file to file system
			LOGGER.warn("Failed to save file: "+savedName);
			fileDAO.delete(authenticatedUser, new long[]{savedFile.getFileId()});
			return null;
		}
		savedFile.setUrl(createUrl(fp.getUriPath(),savedName));
		
		return savedFile;
	}

	/**
	 * Delete the files given as a list of file ids, if array is empty or null, all user's files will be deleted
	 * 
	 * @param authenticatedUser
	 * @param fileIds
	 */
	public static void deleteFiles(UserIdentity authenticatedUser, long[] fileIds) {
		/* process the deletion immediately, this could also be made into a quartz job if it starts to slow down or cause issues */
		FileList files = ServiceInitializer.getDAOHandler().getDAO(FileDAO.class).delete(authenticatedUser, fileIds); // delete files, this will also check permissions
		if(FileList.isEmpty(files)){
			LOGGER.debug("No files deleted.");
		}else{
			StringBuilder directoryPath = new StringBuilder(ServiceInitializer.getPropertyHandler().getSystemProperties(FileProperties.class).getFilePath());
			int length = directoryPath.length();
			for(File file : files.getFiles()){
				directoryPath.setLength(length);
				directoryPath.append(file.getSavedName());
				if(!FILE_HANDLER.delete(directoryPath.toString())){
					LOGGER.warn("Failed to delete file: "+file.getSavedName());
				}
			} // for
		}
	}

	/**
	 * 
	 * @param authenticatedUser
	 * @param fileIds optional list of file ids, if missing all user's files will be returned
	 * @param limits
	 * @return list of files or null if none found
	 */
	public static FileList listFiles(UserIdentity authenticatedUser, long[] fileIds, Limits limits) {
		FileList files = ServiceInitializer.getDAOHandler().getDAO(FileDAO.class).getFiles(authenticatedUser, fileIds, limits);
		if(FileList.isEmpty(files)){
			return null;
		}else{
			LOGGER.debug("Resolving URLs for file list...");
			String uri = ServiceInitializer.getPropertyHandler().getSystemProperties(FileProperties.class).getUriPath();
			for(File file : files.getFiles()){
				file.setUrl(createUrl(uri,file.getSavedName()));
			}
			return files;
		} // else
	}
	
	/**
	 * 
	 * @param filePath
	 * @param savedName
	 * @return URL for the file created from the given values
	 */
	private static final String createUrl(String filePath, String savedName){
		return filePath+savedName;
	}
}
