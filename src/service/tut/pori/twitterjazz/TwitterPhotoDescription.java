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
package service.tut.pori.twitterjazz;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;

import service.tut.pori.contentanalysis.CAContentCore.ServiceType;
import twitter4j.MediaEntity;
import twitter4j.Status;
import core.tut.pori.utils.ISODateAdapter;

/**
 * A photo description received from Twitter.
 * 
 * <h2>Optional Elements</h2>
 * <ul>
 *  <li>{@value service.tut.pori.twitterjazz.Definitions#ELEMENT_LOCATION}</li>
 *  <li>{@value service.tut.pori.twitterjazz.Definitions#ELEMENT_PHOTO_GUID}, missing if the photo is not known by the system.</li>
 *  <li>{@value service.tut.pori.twitterjazz.Definitions#ELEMENT_PHOTO_TAG_LIST}</li>
 *  <li>{@value service.tut.pori.contentanalysis.Definitions#ELEMENT_SERVICE_ID}, missing if the photo is not known by the system.</li>
 * </ul>
 * 
 * <h3>XML Example</h3>
 * 
 * {@doc.restlet service="[service.tut.pori.twitterjazz.reference.Definitions#SERVICE_TJ_REFERENCE_EXAMPLE]" method="[service.tut.pori.twitterjazz.Definitions#ELEMENT_PHOTO_DESCRIPTION]" type="GET" query="" body_uri=""}
 * 
 * @see twitter4j.Status
 */
@XmlRootElement(name=Definitions.ELEMENT_PHOTO_DESCRIPTION)
@XmlAccessorType(XmlAccessType.NONE)
public class TwitterPhotoDescription {
	private static final Logger LOGGER = Logger.getLogger(TwitterPhotoDescription.class);
	@XmlJavaTypeAdapter(ISODateAdapter.class)
	@XmlElement(name = Definitions.ELEMENT_CREATED_TIMESTAMP)
	private Date _createdTime = null;
	@XmlElement(name = Definitions.ELEMENT_DESCRIPTION)
	private String _description = null;
	private String _entityId = null;
	private String _entityUrl = null;
	@XmlElement(name = Definitions.ELEMENT_MESSAGE_POSTER)
	private String _fromName = null;
	@XmlElement(name = Definitions.ELEMENT_LOCATION)
	private TwitterLocation _location = null;
	@XmlElement(name = Definitions.ELEMENT_PHOTO_GUID)
	private String _photoGUID = null;
	@XmlElement(name = service.tut.pori.contentanalysis.Definitions.ELEMENT_SERVICE_ID)
	private ServiceType _serviceType = null;
	@XmlElementWrapper(name = Definitions.ELEMENT_PHOTO_TAG_LIST)
	@XmlElement(name = Definitions.ELEMENT_PHOTO_TAG)
	private List<TwitterPhotoTag> _tags = null;
	
	/**
	 * 
	 * @param twitterTag
	 * @see #getTags()
	 */
	public void addTag(TwitterPhotoTag twitterTag) {
		if(_tags == null){
			_tags = new ArrayList<>();
		}
		_tags.add(twitterTag);
	}

	/**
	 * @return the tags
	 * @see #setTags(List)
	 */
	public List<TwitterPhotoTag> getTags() {
		return _tags;
	}

	/**
	 * @param tags the tags to set
	 * @see #getTags()
	 */
	public void setTags(List<TwitterPhotoTag> tags) {
		_tags = tags;
	}

	/**
	 * @return the photoGUID
	 * @see #setPhotoGUID(String)
	 */
	public String getPhotoGUID() {
		return _photoGUID;
	}

	/**
	 * @param photoGUID the photoGUID to set
	 * @see #getPhotoGUID()
	 */
	public void setPhotoGUID(String photoGUID) {
		_photoGUID  = photoGUID;
	}

	/**
	 * @return the entityId
	 * @see #setEntityId(String)
	 */
	public String getEntityId() {
		return _entityId;
	}

	/**
	 * @return the entityUrl
	 * @see #setEntityUrl(String)
	 */
	public String getEntityUrl() {
		return _entityUrl;
	}

	/**
	 * 
	 * @param status
	 * @return photo descriptions or null if the passes status was null or did not contain valid photo descriptions
	 */
	public static List<TwitterPhotoDescription> getTwitterPhotoDescriptions(Status status){
		if(status == null){
			LOGGER.warn("null status.");
			return null;
		}

		MediaEntity[] entities = status.getMediaEntities();
		if(ArrayUtils.isEmpty(entities)){
			LOGGER.debug("No entities.");
			return null;
		}

		TwitterLocation location = TwitterLocation.getTwitterLocation(status.getGeoLocation());
		ArrayList<TwitterPhotoDescription> descriptions = new ArrayList<>(entities.length);
		for(MediaEntity e : entities){
			if(Definitions.TWITTER_TYPE_PHOTO.equalsIgnoreCase(e.getType())){
				TwitterPhotoDescription d = new TwitterPhotoDescription();
				d._createdTime = status.getCreatedAt();
				d._description = status.getText();
				d._fromName = status.getUser().getScreenName();
				d._location = location;
				d._entityUrl = e.getMediaURL();
				d._entityId = String.valueOf(e.getId());
				descriptions.add(d);
			}
		}

		return (descriptions.isEmpty() ? null : descriptions);
	}

	/**
	 * @return the serviceType
	 * @see #setServiceType(service.tut.pori.contentanalysis.CAContentCore.ServiceType)
	 */
	public ServiceType getServiceType() {
		return _serviceType;
	}

	/**
	 * @param serviceType the serviceType to set
	 * @see #getServiceType()
	 */
	public void setServiceType(ServiceType serviceType) {
		_serviceType = serviceType;
	}

	/**
	 * @see twitter4j.Status#getCreatedAt()
	 * @see #setCreatedTime(Date)
	 * 
	 * @return the createdTime
	 */
	public Date getCreatedTime() {
		return _createdTime;
	}

	/**
	 * @param createdTime the createdAt to set
	 * @see #getCreatedTime()
	 */
	public void setCreatedTime(Date createdTime) {
		_createdTime = createdTime;
	}

	/**
	 * @see twitter4j.Status#getText()
	 * @see #setDescription(String)
	 * 
	 * @return the description
	 */
	public String getDescription() {
		return _description;
	}

	/**
	 * @param description the description to set
	 * @see #getDescription()
	 */
	public void setDescription(String description) {
		_description = description;
	}

	/**
	 * @see twitter4j.Status#getUser()
	 * @see twitter4j.User#getScreenName()
	 * @see #setFromName(String)
	 * 
	 * @return the fromName
	 */
	public String getFromName() {
		return _fromName;
	}

	/**
	 * @param fromName the fromName to set
	 * @see #getFromName()
	 */
	public void setFromName(String fromName) {
		_fromName = fromName;
	}

	/**
	 * @see twitter4j.Status#getGeoLocation()
	 * @see #setLocation(TwitterLocation)
	 * 
	 * @return the location
	 */
	public TwitterLocation getLocation() {
		return _location;
	}

	/**
	 * @param location the location to set
	 * @see #getLocation()
	 */
	public void setLocation(TwitterLocation location) {
		_location = location;
	}

	/**
	 * @param entityId the entityId to set
	 * @see #getEntityId()
	 */
	public void setEntityId(String entityId) {
		_entityId = entityId;
	}

	/**
	 * @param entityUrl the entityUrl to set
	 * @see #getEntityUrl()
	 */
	public void setEntityUrl(String entityUrl) {
		_entityUrl = entityUrl;
	}
}
