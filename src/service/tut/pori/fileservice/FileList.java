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

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import core.tut.pori.http.ResponseData;

/**
 * A container for file objects, which can be directly used with XML Response.
 * 
 * <h3>XML Example</h3>
 * 
 * {@doc.restlet service="[service.tut.pori.fileservice.reference.Definitions#SERVICE_FS_REFERENCE_EXAMPLE]" method="[service.tut.pori.fileservice.Definitions#ELEMENT_FILELIST]" type="GET" query="" body_uri=""}
 * 
 * @see service.tut.pori.fileservice.File
 */
@XmlRootElement(name=Definitions.ELEMENT_FILELIST)
@XmlAccessorType(value=XmlAccessType.NONE)
public class FileList extends ResponseData {
	@XmlElement(name = Definitions.ELEMENT_FILE)
	private List<File> _files = null;

	/**
	 * @return the files
	 */
	public List<File> getFiles() {
		return _files;
	}

	/**
	 * @param files the files to set
	 */
	public void setFiles(List<File> files) {
		_files = files;
	}
	
	/**
	 * 
	 */
	public FileList(){
		// nothing needed
	}
	
	/**
	 * 
	 * @param files
	 * @return new file list or null if null or empty list was passed
	 */
	public static FileList getFileList(List<File> files){
		if(files == null || files.isEmpty()){
			return null;
		}else{
			FileList fileList = new FileList();
			fileList._files = files;
			return fileList;
		}
	}
	
	/**
	 * for sub-classing, use the static
	 * 
	 * @return true if this list is empty
	 * @see #isEmpty(FileList)
	 */
	protected boolean isEmpty(){
		return (_files == null || _files.isEmpty());
	}
	
	/**
	 * 
	 * @param fileList
	 * @return true if the given list is null or empty
	 */
	public static boolean isEmpty(FileList fileList){
		return (fileList == null ? true : fileList.isEmpty());
	}
}
