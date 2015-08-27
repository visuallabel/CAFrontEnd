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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import core.tut.pori.users.UserIdentity;

/**
 * Details of a single uploaded file.
 * 
 * The saved name (file system name) should not be shown to the user, even though it can be figured out from the URL. 
 * The name parameter is the filename used provided when uploading/creating the file.
 * 
 * <h3>XML Example</h3>
 * 
 * {@doc.restlet service="[service.tut.pori.fileservice.reference.Definitions#SERVICE_FS_REFERENCE_EXAMPLE]" method="[service.tut.pori.fileservice.Definitions#ELEMENT_FILE]" type="GET" query="" body_uri=""}
 * 
 * <h2>Optional elements</h2>
 * <ul>
 *  <li>{@link service.tut.pori.fileservice.Definitions#ELEMENT_NAME}</li>
 * </ul>
 * 
 */
@XmlRootElement(name=Definitions.ELEMENT_FILE)
@XmlAccessorType(value=XmlAccessType.NONE)
public class File {
	@XmlElement(name = Definitions.ELEMENT_FILE_ID)
	private Long _fileId = null;
	@XmlElement(name = Definitions.ELEMENT_NAME)
	private String _name = null;
	private String _savedName = null;
	@XmlElement(name = Definitions.ELEMENT_URL)
	private String _url = null;
	private UserIdentity _userId = null;
	
	/**
	 * System specified file identifier.
	 * 
	 * @return the fileId
	 * @see #setFileId(Long)
	 */
	public Long getFileId() {
		return _fileId;
	}
	
	/**
	 * @param fileId the fileId to set
	 * @see #getFileId()
	 */
	public void setFileId(Long fileId) {
		_fileId = fileId;
	}
	
	/**
	 * @return the original file name or null if none was given
	 * @see #setName(String)
	 */
	public String getName() {
		return _name;
	}
	
	/**
	 * @param name the orginal file name
	 * @see #getName()
	 */
	public void setName(String name) {
		_name = name;
	}
	
	/**
	 * @return the userId of the file uploader.
	 * @see #setUserId(UserIdentity)
	 */
	public UserIdentity getUserId() {
		return _userId;
	}
	
	/**
	 * @param userId the userId to set
	 * @see #getUserId()
	 */
	public void setUserId(UserIdentity userId) {
		_userId = userId;
	}
	
	/**
	 * 
	 * @param userId
	 * @see #setUserId(UserIdentity)
	 */
	@SuppressWarnings("unused") // for serialization
	private void setUserIdValue(Long userId){
		_userId = (userId == null ? null : new UserIdentity(userId));
	}
	
	/**
	 * 
	 * @return user id value
	 * @see #getUserId()
	 */
	public Long getUserIdValue(){
		return (_userId == null ? null : _userId.getUserId());
	}

	/**
	 * @return the file download url
	 * @see #setUrl(String)
	 */
	public String getUrl() {
		return _url;
	}

	/**
	 * @param url the url to set
	 * @see #getUrl()
	 */
	public void setUrl(String url) {
		_url = url;
	}

	/**
	 * @return the file name used to save the file to the system
	 */
	public String getSavedName() {
		return _savedName;
	}

	/**
	 * @param savedName the name used to save the file to the system
	 */
	public void setSavedName(String savedName) {
		_savedName = savedName;
	}
}
