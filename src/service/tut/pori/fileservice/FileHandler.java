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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

/**
 * A simple helper class for saving and deleting a file, as well generating a random filename.
 */
public class FileHandler {
	private static final char SEPARATOR_FILE_EXTENSION = '.';
	private static final Logger LOGGER = Logger.getLogger(FileHandler.class);
	
	/**
	 * Saves the content of the input stream to the given path using the given filename
	 * 
	 * @param filePath absolute file path of the file to be created
	 * @param file the inputstream containing the file, the stream will NOT be closed
	 * @return true on success
	 */
	public boolean save(String filePath, InputStream file){
		try (FileOutputStream out = new FileOutputStream(filePath)) {
			LOGGER.debug("Saved new file "+filePath+", size: "+IOUtils.copy(file, out));
			return true;
		} catch (IOException ex) {
			LOGGER.error(ex, ex);
		}
		return false;
	}
	
	/**
	 * 
	 * @param filePath the absolute file path of the file to be deleted
	 * @return true on success
	 */
	public boolean delete(String filePath){
		LOGGER.debug("Deleting file: "+filePath);
		return FileUtils.deleteQuietly(new File(filePath));
	}
	
	/**
	 * helper method for generating a new file name based on the given original filename.
	 * 
	 * The file names should be highly unique, but they are random, and it is up to the caller to take appropriate measures for validating the uniqueness of the file names.
	 * 
	 * @param filename
	 * @return randomly generated URI-safe (no URL encoding needed) file name
	 */
	public String generateFilename(String filename){
		String name = UUID.randomUUID().toString();
		
		String[] parts = StringUtils.split(filename, FileHandler.SEPARATOR_FILE_EXTENSION);
		if(ArrayUtils.getLength(parts) < 2){
			LOGGER.debug("No filename or missing file extension.");
		}else{
			String extension = parts[parts.length-1];
			StringBuilder sb = new StringBuilder(name.length()+extension.length()+2); // initialize with fixed size because the size is known
			sb.append(name);
			sb.append(FileHandler.SEPARATOR_FILE_EXTENSION);
			sb.append(extension);
			name = sb.toString();
		}
		return name;
	}
}
