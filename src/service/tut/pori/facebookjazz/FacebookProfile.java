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
package service.tut.pori.facebookjazz;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import core.tut.pori.http.ResponseData;

/**
 * a class containing a profile of a single facebook user
 * 
 * <h2>Optional Elements</h2>
 * <ul>
 *  <li>{@value service.tut.pori.facebookjazz.Definitions#ELEMENT_EVENT_LIST}</li>
 *  <li>{@value service.tut.pori.facebookjazz.Definitions#ELEMENT_GROUP_LIST}</li>
 *  <li>{@value service.tut.pori.facebookjazz.Definitions#ELEMENT_LIKE_LIST}</li>
 *  <li>{@value service.tut.pori.facebookjazz.Definitions#ELEMENT_PHOTO_DESCRIPTION_LIST}</li>
 *  <li>{@value service.tut.pori.facebookjazz.Definitions#ELEMENT_STATUS_MESSAGE_LIST}</li>
 *  <li>{@value service.tut.pori.facebookjazz.Definitions#ELEMENT_VIDEO_DESCRIPTION_LIST}</li>
 * </ul>
 * 
 * <h3>XML Example</h3>
 * 
 * {@doc.restlet service="[service.tut.pori.facebookjazz.reference.Definitions#SERVICE_FBJ_REFERENCE_EXAMPLE]" method="[service.tut.pori.facebookjazz.Definitions#ELEMENT_FACEBOOK_PROFILE]" type="GET" query="" body_uri=""}
 *
 * @see service.tut.pori.facebookjazz.FacebookEvent
 * @see service.tut.pori.facebookjazz.FacebookGroup
 * @see service.tut.pori.facebookjazz.FacebookLike
 * @see service.tut.pori.facebookjazz.FacebookPhotoDescription
 * @see service.tut.pori.facebookjazz.FacebookStatusMessage
 * @see service.tut.pori.facebookjazz.FacebookUserDetails
 * @see service.tut.pori.facebookjazz.FacebookVideoDescription
 */
@XmlRootElement(name=Definitions.ELEMENT_FACEBOOK_PROFILE)
@XmlAccessorType(XmlAccessType.NONE)
public class FacebookProfile extends ResponseData{
	@XmlElementWrapper(name = Definitions.ELEMENT_EVENT_LIST)
	@XmlElement(name = Definitions.ELEMENT_EVENT)
	private List<FacebookEvent> _events = null;
	@XmlElementWrapper(name = Definitions.ELEMENT_GROUP_LIST)
	@XmlElement(name = Definitions.ELEMENT_GROUP)
	private List<FacebookGroup> _groups = null;
	@XmlElementWrapper(name = Definitions.ELEMENT_LIKE_LIST)
	@XmlElement(name = Definitions.ELEMENT_LIKE)
	private List<FacebookLike> _likes = null;
	@XmlElementWrapper(name = Definitions.ELEMENT_PHOTO_DESCRIPTION_LIST)
	@XmlElement(name = Definitions.ELEMENT_PHOTO_DESCRIPTION)
	private List<FacebookPhotoDescription> _photoDescriptions = null;
	@XmlElementWrapper(name = Definitions.ELEMENT_STATUS_MESSAGE_LIST)
	@XmlElement(name = Definitions.ELEMENT_STATUS_MESSAGE)
	private List<FacebookStatusMessage> _statusMessages = null;
	@XmlElement(name = Definitions.ELEMENT_USER_DETAILS)
	private FacebookUserDetails _user = null;
	@XmlElementWrapper(name = Definitions.ELEMENT_VIDEO_DESCRIPTION_LIST)
	@XmlElement(name = Definitions.ELEMENT_VIDEO_DESCRIPTION)
	private List<FacebookVideoDescription> _videoDescriptions = null;

	/**
	 * @param user
	 */
	public FacebookProfile(FacebookUserDetails user){
		_user = user;
	}
	
	/**
	 * for serialization
	 */
	protected FacebookProfile(){
		// nothing needed
	}

	/**
	 * 
	 * @return user details
	 */
	public FacebookUserDetails getUser(){
		return _user;
	}

	/**
	 * 
	 * @param like
	 * @see #getLikes()
	 */
	public void addLike(FacebookLike like) {
		if(_likes == null){
			_likes = new ArrayList<>();
		}
		_likes.add(like);
	}

	/**
	 * 
	 * @param likes 
	 * @see #getLikes()
	 */
	public void setLikes(List<FacebookLike> likes){
		_likes = likes;
	}

	/**
	 * 
	 * @return likes
	 * @see #setLikes(List)
	 */
	public List<FacebookLike> getLikes(){
		return _likes;
	}

	/**
	 * 
	 * @param groups 
	 * @see #getGroups()
	 */
	public void setGroups(List<FacebookGroup> groups){
		_groups = groups;
	}

	/**
	 * 
	 * @param group
	 * @see #getGroups()
	 */
	public void addGroup(FacebookGroup group) {
		if(_groups == null){
			_groups = new ArrayList<>();
		}
		_groups.add(group);
	}

	/**
	 * 
	 * @return groups
	 * @see #setGroups(List)
	 */
	public List<FacebookGroup> getGroups(){
		return _groups;
	}

	/**
	 * 
	 * @return events
	 * @see #setEvents(List)
	 */
	public List<FacebookEvent> getEvents() {
		return _events;
	}

	/**
	 * 
	 * @param events 
	 * @see #getEvents()
	 */
	public void setEvents(List<FacebookEvent> events) {
		_events = events;
	}

	/**
	 * 
	 * @param messages 
	 * @see #getStatusMessages()
	 */
	public void setStatusMessages(List<FacebookStatusMessage> messages){
		_statusMessages = messages;
	}

	/**
	 * 
	 * @param statusMessage
	 * @see #getStatusMessages()
	 */
	public void addStatusMessage(FacebookStatusMessage statusMessage) {
		if(_statusMessages == null){
			_statusMessages = new ArrayList<>();
		}
		_statusMessages.add(statusMessage);
	}

	/**
	 * 
	 * @return status messages
	 * @see #setStatusMessages(List)
	 */
	public List<FacebookStatusMessage> getStatusMessages(){
		return _statusMessages;
	}

	/**
	 * 
	 * @return photo descriptions
	 * @see #setPhotoDescriptions(List)
	 */
	public List<FacebookPhotoDescription> getPhotoDescriptions() {
		return _photoDescriptions;
	}

	/**
	 * 
	 * @param photoDescriptions 
	 * @see #getPhotoDescriptions()
	 */
	public void setPhotoDescriptions(List<FacebookPhotoDescription> photoDescriptions) {
		_photoDescriptions = photoDescriptions;
	}

	/**
	 * 
	 * @return video descriptions
	 * @see #setVideoDescriptions(List)
	 */
	public List<FacebookVideoDescription> getVideoDescriptions() {
		return _videoDescriptions;
	}

	/**
	 * 
	 * @param videoDescriptions 
	 * @see #getVideoDescriptions()
	 */
	public void setVideoDescriptions(List<FacebookVideoDescription> videoDescriptions) {
		_videoDescriptions = videoDescriptions;
	}
}
