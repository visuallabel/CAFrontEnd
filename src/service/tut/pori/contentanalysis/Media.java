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
package service.tut.pori.contentanalysis;

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.beans.Field;

import service.tut.pori.contentanalysis.CAContentCore.ServiceType;
import service.tut.pori.contentanalysis.CAContentCore.Visibility;
import core.tut.pori.dao.SolrDAO;
import core.tut.pori.users.UserIdentity;
import core.tut.pori.utils.MediaUrlValidator.MediaType;

/**
 * Abstract base class for media objects
 *
 */
@XmlAccessorType(value=XmlAccessType.NONE)
public abstract class Media {
	private static final Logger LOGGER = Logger.getLogger(Media.class);
	@XmlElement(name = Definitions.ELEMENT_BACKEND_STATUS_LIST)
	private BackendStatusList _backendStatus = null;
	@Field(Definitions.SOLR_FIELD_CREDITS)
	@XmlElement(name = Definitions.ELEMENT_CREDITS)
	private String _credits = null;
	@Field(Definitions.SOLR_FIELD_DESCRIPTION)
	@XmlElement(name = Definitions.ELEMENT_DESCRIPTION)
	private String _description = null;
	@Field(SolrDAO.SOLR_FIELD_ID)
	@XmlElement(name = Definitions.ELEMENT_GUID)
	private String _guid = null;
	private MediaType _mediaType = MediaType.UNKNOWN;
	@Field(Definitions.SOLR_FIELD_NAME)
	@XmlElement(name = Definitions.ELEMENT_NAME)
	private String _name = null;
	@XmlElement(name = Definitions.ELEMENT_SERVICE_ID)
	private ServiceType _serviceType = null;
	private String _url = null;
	private UserIdentity _userId = null;   // the owner of the media
	@XmlElement(name = Definitions.ELEMENT_VISIBILITY)
	private Visibility _visibility = null;
	@XmlElement(name = Definitions.ELEMENT_MEDIA_OBJECTLIST)
	private MediaObjectList _mediaObjects = null;
	@Field(Definitions.SOLR_FIELD_UPDATED)
	private Date _updated = null;

	/**
	 * 
	 * @param url
	 * @see #getUrl()
	 */
	public void setUrl(String url) {
		_url = url;
	}

	/**
	 * Default data groups: {@value core.tut.pori.http.parameters.DataGroups#DATA_GROUP_BASIC}, {@value core.tut.pori.http.parameters.DataGroups#DATA_GROUP_DEFAULTS}
	 * 
	 * @return url
	 * @see #setUrl(String)
	 */  
	@XmlElement(name = Definitions.ELEMENT_URL)
	public String getUrl() {
		return _url;
	}

	/**
	 * Default data groups: {@value service.tut.pori.contentanalysis.Definitions#DATA_GROUP_BACKEND_STATUS}
	 * 
	 * @return back-end status for this media or null if not available
	 * @see #setBackendStatus(BackendStatusList)
	 */
	public BackendStatusList getBackendStatus() {
		return _backendStatus;
	}

	/**
	 * 
	 * @param backendStatus
	 * @see #getBackendStatus()
	 */
	public void setBackendStatus(BackendStatusList backendStatus) {
		_backendStatus = backendStatus;
	}

	/**
	 * 
	 * @param backend
	 * @see #getBackendStatus()
	 */
	public void addackendStatus(BackendStatus backend) {
		if(_backendStatus == null){
			_backendStatus = new BackendStatusList();
		}
		_backendStatus.setBackendStatus(backend);
	}

	/**
	 * Default data groups: {@value core.tut.pori.http.parameters.DataGroups#DATA_GROUP_DEFAULTS}
	 * 
	 * @return media objects or null if not available
	 * @see #setMediaObjects(MediaObjectList)
	 */
	public MediaObjectList getMediaObjects() {
		return _mediaObjects;
	}

	/**
	 * 
	 * @param mediaObjects
	 * @see #getMediaObjects()
	 */
	public void setMediaObjects(MediaObjectList mediaObjects) {
		_mediaObjects = mediaObjects;
	}

	/**
	 * Adds the given object, NOTE: this will NOT check for duplicates.
	 * 
	 * @param object 
	 * @see #getMediaObjects()
	 */
	public void addMediaObject(MediaObject object){
		if(object == null){
			LOGGER.debug("Ignored null object.");
			return;
		}
		if(_mediaObjects == null){
			_mediaObjects = new MediaObjectList();
		}
		_mediaObjects.addMediaObject(object);
	}
	
	/**
	 * Adds the given objects, NOTE: this will NOT check for duplicates.
	 * 
	 * @param mediaObjects
	 * @see #getMediaObjects()
	 */
	public void addMediaObjects(MediaObjectList mediaObjects){
		if(MediaObjectList.isEmpty(mediaObjects)){
			LOGGER.debug("Ignored empty object list.");
			return;
		}
		ResultInfo info = mediaObjects.getResultInfo();
		if(_mediaObjects == null){
			if(info != null){ // duplicate the info to preserve original
				ResultInfo oInfo = info;
				info = new ResultInfo();
				info.setEndItem(oInfo.getEndItem());
				info.setStartItem(oInfo.getStartItem());
				info.setResultCount(oInfo.getResultCount());
			}
			_mediaObjects = MediaObjectList.getMediaObjectList(mediaObjects.getMediaObjects(), info);
		}else{
			_mediaObjects.addMediaObjects(mediaObjects);
		}
	}

	/**
	 * 
	 * @param type
	 * @see #getServiceType()
	 */
	public void setServiceType(ServiceType type) {
		_serviceType = type;
	}  

	/**
	 * Default data groups: {@value core.tut.pori.http.parameters.DataGroups#DATA_GROUP_BASIC}, {@value core.tut.pori.http.parameters.DataGroups#DATA_GROUP_DEFAULTS}
	 * 
	 * @return the service this media originated from
	 * @see #setServiceType(service.tut.pori.contentanalysis.CAContentCore.ServiceType)
	 */
	public ServiceType getServiceType() {
		return _serviceType;
	}
	
	/**
	 * 
	 * @param serviceId
	 */
	@Field(Definitions.SOLR_FIELD_SERVICE_ID)
	private void setServiceId(Integer serviceId){
		_serviceType = (serviceId == null ? null : ServiceType.fromServiceId(serviceId));
	}
	
	/**
	 * @see #getServiceType()
	 * 
	 * @return the service id
	 */
	public Integer getServiceId() {
		return (_serviceType == null ? null : _serviceType.getServiceId());
	}

	/**
	 * Default data groups: {@value core.tut.pori.http.parameters.DataGroups#DATA_GROUP_BASIC}, {@value core.tut.pori.http.parameters.DataGroups#DATA_GROUP_DEFAULTS}
	 * 
	 * @return guid
	 * @see #setGUID(String)
	 */
	public String getGUID() {
		return _guid;
	}

	/**
	 * 
	 * @param guid
	 * @see #getGUID()
	 */
	public void setGUID(String guid) {
		_guid = guid;
	}

	/**
	 * @see #getOwnerUserId()
	 * 
	 * @return user identity value
	 */
	@XmlElement(name = core.tut.pori.users.Definitions.ELEMENT_USER_ID)
	public Long getOwnerUserIdValue() {
		return (_userId == null ? null : _userId.getUserId());
	}

	/**
	 * for serialization
	 * @param userId
	 * @see #setOwnerUserId(UserIdentity)
	 */
	@Field(Definitions.SOLR_FIELD_USER_ID)
	private void setOwnerUserIdValue(Long userId) {
		_userId = (userId == null ? null : new UserIdentity(userId));
	}

	/**
	 * Default data groups: {@value core.tut.pori.http.parameters.DataGroups#DATA_GROUP_BASIC}, {@value core.tut.pori.http.parameters.DataGroups#DATA_GROUP_DEFAULTS}
	 * 
	 * @return media owner
	 * @see #setOwnerUserId(UserIdentity)
	 */
	public UserIdentity getOwnerUserId() {
		return _userId;
	}

	/**
	 * 
	 * @param ownerId the media's owner's user id
	 * @see #getOwnerUserId()
	 */
	public void setOwnerUserId(UserIdentity ownerId) {
		_userId = ownerId;
	} 

	/**
	 * for serialization, must be public for solr.
	 */
	public Media(){
		// nothing needed
	}
	
	/**
	 * 
	 * @param guid
	 */
	public Media(String guid){
		_guid = guid;
	}
	
	/**
	 * 
	 * @param guid
	 * @param ownerUserId
	 * @param serviceType
	 * @param visibility
	 */
	public Media(String guid, UserIdentity ownerUserId, ServiceType serviceType, Visibility visibility){
		_guid = guid;
		_userId = ownerUserId;
		_serviceType = serviceType;
		_visibility = visibility;
	}

	/**
	 * 
	 * @param media can be null
	 * @return true if the given media is valid
	 */
	public static boolean isValid(Media media){
		if(media == null){
			return false;
		}else{
			return media.isValid();
		}
	}

	/**
	 * only for sub-classing, use the static
	 * @return true if this media is valid
	 * @see #isValid(Media)
	 */
	protected boolean isValid(){
		if(_guid == null || _serviceType == null || !UserIdentity.isValid(_userId) || _visibility == null){
			return false;
		}else if(MediaObjectList.isEmpty(_mediaObjects)){
			return true;
		}else{
			return MediaObjectList.isValid(_mediaObjects);
		}
	}

	/**
	 * Default data groups: {@value core.tut.pori.http.parameters.DataGroups#DATA_GROUP_BASIC}
	 * 
	 * @return credits information for the media (e.g. Creative Commons) or null if not available
	 * @see #setCredits(String)
	 */  
	public String getCredits() {
		return _credits;
	}

	/**
	 * 
	 * @param credits
	 * @see #getCredits()
	 */
	public void setCredits(String credits) {
		_credits = credits;
	}

	/**
	 * Default data groups: {@value core.tut.pori.http.parameters.DataGroups#DATA_GROUP_BASIC}
	 * 
	 * @return name of the media or null if not available
	 * @see #setName(String)
	 */  
	public String getName() {
		return _name;
	}

	/**
	 * 
	 * @param name
	 * @see #getName()
	 */
	public void setName(String name) {
		_name = name;
	}

	/**
	 * Default data groups: {@value core.tut.pori.http.parameters.DataGroups#DATA_GROUP_BASIC}
	 * 
	 * @return description of the media or null if not available
	 * @see #setDescription(String)
	 */  
	public String getDescription() {
		return _description;
	}

	/**
	 * 
	 * @param description
	 * @see #getDescription()
	 */
	public void setDescription(String description) {
		_description = description;
	}

	/**
	 * 
	 * @param visibility 
	 * @see #getVisibility()
	 */
	public void setVisibility(Visibility visibility){
		_visibility = visibility;
	}

	/**
	 * Default data groups: {@value core.tut.pori.http.parameters.DataGroups#DATA_GROUP_BASIC}, {@value service.tut.pori.contentanalysis.Definitions#DATA_GROUP_VISIBILITY}
	 * 
	 * If visibility is not available, it should be assumed to be {@link service.tut.pori.contentanalysis.CAContentCore.Visibility#PRIVATE}
	 * 
	 * @return visibility or null if not available
	 * @see #setVisibility(service.tut.pori.contentanalysis.CAContentCore.Visibility)
	 */
	public Visibility getVisibility(){
		return _visibility;
	}
	
	/**
	 * @see #getVisibility()
	 * 
	 * @return visibility value
	 */
	public Integer getVisibilityValue(){
		return (_visibility == null ? null : _visibility.toInt());
	}
	
	/**
	 * 
	 * @param visibility
	 * @see #setVisibility(service.tut.pori.contentanalysis.CAContentCore.Visibility)
	 */
	@Field(Definitions.SOLR_FIELD_VISIBILITY)
	private void setVisibilityValue(Integer visibility){
		_visibility = (visibility == null ? null : Visibility.fromInt(visibility));
	}

	/**
	 * @return the updated
	 */
	public Date getUpdated() {
		return _updated;
	}

	/**
	 * @param updated the updated to set
	 */
	public void setUpdated(Date updated) {
		_updated = updated;
	}

	/**
	 * @return the mediaType
	 */
	@XmlElement(name = Definitions.ELEMENT_MEDIA_TYPE)
	public MediaType getMediaType() {
		return _mediaType;
	}

	/**
	 * @param mediaType the mediaType to set
	 */
	public void setMediaType(MediaType mediaType) {
		_mediaType = mediaType;
	}
	
	/**
	 * @see #getMediaType()
	 * 
	 * @return mediaType value
	 */
	public Integer getMediaTypeValue(){
		return (_mediaType == null ? null : _mediaType.toInt());
	}
	
	/**
	 * 
	 * @param mediaType
	 * @see #setMediaType(core.tut.pori.utils.MediaUrlValidator.MediaType)
	 */
	@Field(Definitions.SOLR_FIELD_MEDIA_TYPE)
	private void setMediaTypeValue(Integer mediaType){
		_mediaType = (mediaType == null ? null : MediaType.fromInt(mediaType));
	}
}
