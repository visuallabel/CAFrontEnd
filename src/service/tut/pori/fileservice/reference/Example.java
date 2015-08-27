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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import core.tut.pori.http.ResponseData;
import service.tut.pori.fileservice.Definitions;
import service.tut.pori.fileservice.File;
import service.tut.pori.fileservice.FileList;

/**
 * Response example
 * 
 */
@XmlRootElement(name=service.tut.pori.fileservice.reference.Definitions.ELEMENT_EXAMPLE)
@XmlAccessorType(XmlAccessType.NONE)
public class Example extends ResponseData {
	@XmlElement(name=Definitions.ELEMENT_FILE)
	private File _file = null;
	@XmlElement(name=Definitions.ELEMENT_FILELIST)
	private FileList _fileList = null;

	/**
	 * @return the fileList
	 */
	public FileList getFileList() {
		return _fileList;
	}

	/**
	 * @param fileList the fileList to set
	 */
	public void setFileList(FileList fileList) {
		_fileList = fileList;
	}

	/**
	 * @return the file
	 */
	public File getFile() {
		return _file;
	}

	/**
	 * @param file the file to set
	 */
	public void setFile(File file) {
		_file = file;
	}
}
