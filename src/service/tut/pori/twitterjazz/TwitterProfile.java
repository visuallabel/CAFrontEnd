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

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import core.tut.pori.http.ResponseData;

/**
 * Details of a Twitter profile usable with Response.
 * 
 * <h2>Optional Elements</h2>
 * <ul>
 *  <li>{@value service.tut.pori.twitterjazz.Definitions#ELEMENT_PHOTO_DESCRIPTION_LIST}</li>
 *  <li>{@value service.tut.pori.twitterjazz.Definitions#ELEMENT_STATUS_MESSAGE_LIST}</li>
 *  <li>{@value service.tut.pori.twitterjazz.Definitions#ELEMENT_VIDEO_DESCRIPTION_LIST}</li>
 * </ul>
 * 
 * <h3>XML Example</h3>
 * 
 * {@doc.restlet service="[service.tut.pori.twitterjazz.reference.Definitions#SERVICE_TJ_REFERENCE_EXAMPLE]" method="[service.tut.pori.twitterjazz.Definitions#ELEMENT_TWITTER_PROFILE]" type="GET" query="" body_uri=""}
 * 
 * @see service.tut.pori.twitterjazz.TwitterUserDetails
 * @see service.tut.pori.twitterjazz.TwitterStatusMessage
 * @see service.tut.pori.twitterjazz.TwitterPhotoDescription
 * @see service.tut.pori.twitterjazz.TwitterVideoDescription
 */
@XmlRootElement(name=Definitions.ELEMENT_TWITTER_PROFILE)
@XmlAccessorType(XmlAccessType.NONE)
public class TwitterProfile extends ResponseData {
	@XmlElement(name = Definitions.ELEMENT_USER_DETAILS)
	private TwitterUserDetails _user = null;
	@XmlElementWrapper(name = Definitions.ELEMENT_STATUS_MESSAGE_LIST)
	@XmlElement(name = Definitions.ELEMENT_STATUS_MESSAGE)
	private List<TwitterStatusMessage> _statusMessages = null;
	@XmlElementWrapper(name = Definitions.ELEMENT_PHOTO_DESCRIPTION_LIST)
	@XmlElement(name = Definitions.ELEMENT_PHOTO_DESCRIPTION)
	private List<TwitterPhotoDescription> _photoDescriptions = null;
	@XmlElementWrapper(name = Definitions.ELEMENT_VIDEO_DESCRIPTION_LIST)
	@XmlElement(name = Definitions.ELEMENT_VIDEO_DESCRIPTION)
	private List<TwitterVideoDescription> _videoDescriptions = null;
	
	/**
	 * 
	 * @param user
	 */
	public TwitterProfile(TwitterUserDetails user){
		_user = user;
	}
	
	/**
	 * for serialization
	 */
	protected TwitterProfile(){
		// nothing needed
	}

	/**
	 * @return the user
	 */ 
	public TwitterUserDetails getUser() {
		return _user;
	}

	/**
	 * @return the statusMessages
	 * @see #setStatusMessages(List)
	 */
	public List<TwitterStatusMessage> getStatusMessages() {
		return _statusMessages;
	}

	/**
	 * @param statusMessages the statusMessages to set
	 * @see #getStatusMessages()
	 */
	public void setStatusMessages(List<TwitterStatusMessage> statusMessages) {
		_statusMessages = statusMessages;
	}

	/**
	 * @return the photoDescriptions
	 * @see #setPhotoDescriptions(List)
	 */
	public List<TwitterPhotoDescription> getPhotoDescriptions() {
		return _photoDescriptions;
	}

	/**
	 * @param photoDescriptions the photoDescriptions to set
	 * @see #getPhotoDescriptions()
	 */
	public void setPhotoDescriptions(List<TwitterPhotoDescription> photoDescriptions) {
		_photoDescriptions = photoDescriptions;
	}

	/**
	 * @return the videoDescriptions
	 * @see #setVideoDescriptions(List)
	 */
	public List<TwitterVideoDescription> getVideoDescriptions() {
		return _videoDescriptions;
	}

	/**
	 * @param videoDescriptions the videoDescriptions to set
	 * @see #getVideoDescriptions()
	 */
	public void setVideoDescriptions(List<TwitterVideoDescription> videoDescriptions) {
		_videoDescriptions = videoDescriptions;
	}
}
