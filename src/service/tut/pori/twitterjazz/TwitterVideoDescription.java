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
package service.tut.pori.twitterjazz;

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;

import core.tut.pori.utils.ISODateAdapter;
import twitter4j.MediaEntity;
import twitter4j.Status;


/**
 * Video description retrieved from Twitter.
 * 
 * <h2>Optional Elements</h2>
 * <ul>
 *  <li>{@value service.tut.pori.twitterjazz.Definitions#ELEMENT_LOCATION}</li>
 * </ul>
 * 
 * <h3>XML Example</h3>
 * 
 * {@doc.restlet service="[service.tut.pori.twitterjazz.reference.Definitions#SERVICE_TJ_REFERENCE_EXAMPLE]" method="[service.tut.pori.twitterjazz.Definitions#ELEMENT_VIDEO_DESCRIPTION]" type="GET" query="" body_uri=""}
 * 
 * @see twitter4j.Status
 */
@XmlRootElement(name=Definitions.ELEMENT_VIDEO_DESCRIPTION)
@XmlAccessorType(XmlAccessType.NONE)
public class TwitterVideoDescription {
	private static final Logger LOGGER = Logger.getLogger(TwitterVideoDescription.class);
	@XmlJavaTypeAdapter(ISODateAdapter.class)
	@XmlElement(name = Definitions.ELEMENT_CREATED_TIMESTAMP)
	private Date _createdTime = null;
	@XmlElement(name = Definitions.ELEMENT_LOCATION)
	private TwitterLocation _location = null;
	@XmlElement(name = Definitions.ELEMENT_DESCRIPTION)
	private String _description = null;
	@XmlElement(name = Definitions.ELEMENT_MESSAGE_POSTER)
	private String _fromScreenName = null;
	
	/**
	 * 
	 * @param status
	 * @return video description or null if the passes status was null or not a valid video description
	 */
	public static TwitterVideoDescription getTwitterVideoDescription(Status status){
		if(status == null){
			return null;
		}
		
		MediaEntity[] entityArray = status.getMediaEntities();
		if(ArrayUtils.isEmpty(entityArray)){
			LOGGER.debug("The status object has no entities.");
			return null;
		}
		
		boolean isVideoDescription = false;
		for(MediaEntity e : entityArray){
			if(Definitions.TWITTER_TYPE_VIDEO.equalsIgnoreCase(e.getType())){
				isVideoDescription = true;
				break;
			}
		}
		
		if(!isVideoDescription){
			LOGGER.debug("The status object contains no valid video URLs.");
			return null;
		}
		
		TwitterVideoDescription d = new TwitterVideoDescription();
		d._createdTime = status.getCreatedAt();
		d._location = TwitterLocation.getTwitterLocation(status.getGeoLocation());
		d._description = status.getText();
		d._fromScreenName = status.getUser().getScreenName();
		return d;
	}

	/**
	 * @see twitter4j.Status#getCreatedAt()
	 * 
	 * @return the createdTime
	 */
	public Date getCreatedTime() {
		return _createdTime;
	}

	/**
	 * 
	 * @param createdTime the createdTime to set
	 */
	public void setCreatedTime(Date createdTime) {
		_createdTime = createdTime;
	}

	/**
	 * @see twitter4j.Status#getGeoLocation()
	 * 
	 * @return the location
	 */
	public TwitterLocation getLocation() {
		return _location;
	}

	/**
	 * @param location the location to set
	 */
	public void setLocation(TwitterLocation location) {
		_location = location;
	}

	/**
	 * @see twitter4j.Status#getText()
	 * 
	 * @return the description
	 */
	public String getDescription() {
		return _description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		_description = description;
	}

	/**
	 * @see twitter4j.Status#getUser()
	 * @see twitter4j.User#getScreenName()
	 * 
	 * @return the fromScreenName
	 */
	public String getFromScreenName() {
		return _fromScreenName;
	}

	/**
	 * @param fromScreenName the fromScreenName to set
	 */
	public void setFromScreenName(String fromScreenName) {
		_fromScreenName = fromScreenName;
	}
}
