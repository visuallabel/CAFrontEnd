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
package service.tut.pori.facebookjazz;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang3.StringUtils;

import service.tut.pori.facebookjazz.FacebookGroup.Privacy;

import com.restfb.types.Event;
import com.restfb.types.Event.Owner;

import core.tut.pori.utils.ISODateAdapter;


/**
 * Details of an event retrieved from Facebook.
 * 
 * <h2>Optional Elements</h2>
 * <ul>
 *  <li>{@value service.tut.pori.facebookjazz.Definitions#ELEMENT_DESCRIPTION}</li>
 *  <li>{@value service.tut.pori.facebookjazz.Definitions#ELEMENT_PRIVACY}</li>
 *  <li>{@value service.tut.pori.facebookjazz.Definitions#ELEMENT_LOCATION}</li>
 * </ul>
 * 
 * <h3>XML Example</h3>
 * 
 * {@doc.restlet service="[service.tut.pori.facebookjazz.reference.Definitions#SERVICE_FBJ_REFERENCE_EXAMPLE]" method="[service.tut.pori.facebookjazz.Definitions#ELEMENT_EVENT]" type="GET" query="" body_uri=""}
 *
 * @see com.restfb.types.Event
 */
@XmlRootElement(name=Definitions.ELEMENT_EVENT)
@XmlAccessorType(XmlAccessType.NONE)
public class FacebookEvent {
	private Event _event = null;
	private Integer _nameWeight = null;
	private Integer _descriptionWeight = null;

	/**
	 * Reply status for a Facebook event.
	 * 
	 * Note that the values returned by Facebook are not clearly defined, and the values listed here are most likely only a subset of the available values.
	 * 
	 */
	@XmlEnum
	public enum RSVPStatus{
		/** user has not replied to the invitation */
		NOT_REPLIED,
		/** user has nor decided */
		UNSURE,
		/** user is attending to the event */
		ATTENDING,
		/** user has declined the invitation */
		DECLINED;

		/**
		 * 
		 * @return status as string
		 */
		public String toRSVPStatusString(){
			return name();
		}

		/**
		 * 
		 * @param statusString
		 * @return RSVPStatus or null if invalid string
		 */
		public static RSVPStatus fromRSVPStatusString(String statusString){
			if(statusString != null){
				for(RSVPStatus r : RSVPStatus.values()){
					if(r.toRSVPStatusString().equalsIgnoreCase(statusString)){
						return r;
					}
				}
			}
			return null;
		}
	}  // enum RSVPStatus
	
	/**
	 * 
	 */
	public FacebookEvent() {
		_event = new Event();
	}

	/**
	 * 
	 * @param event
	 * @throws IllegalArgumentException 
	 */
	public FacebookEvent(Event event) throws IllegalArgumentException{
		if(event == null){
			throw new IllegalArgumentException("Invalid event.");
		}
		_event = event;
	}

	/**
	 * @see com.restfb.types.Event#getOwner()
	 * @see #setEventOwnerName(String)
	 * 
	 * @return owner name
	 */
	@XmlElement(name = Definitions.ELEMENT_OWNER)
	public String getEventOwnerName() {
		Owner owner = _event.getOwner();
		return (owner == null ? null : owner.getName());
	}
	
	/**
	 * @param name 
	 * @see com.restfb.types.Event#setOwner(com.restfb.types.Event.Owner)
	 * @see #getEventOwnerName()
	 */
	public void setEventOwnerName(String name) {
		if(StringUtils.isBlank(name)){
			_event.setOwner(null);
		}else{
			Owner owner = _event.getOwner();
			if(owner == null){
				owner = new Owner();
				_event.setOwner(owner);
			}
			owner.setName(name);
		}
	}

	/**
	 * @see com.restfb.types.Event#getDescription()
	 * @see #getDescription()
	 * 
	 * @return description
	 */
	@XmlElement(name = Definitions.ELEMENT_DESCRIPTION)
	public WeightedStringElement getWDescription() {
		String description = _event.getDescription();
		if(StringUtils.isBlank(description)){
			return null;
		}else{
			return new WeightedStringElement(description, _descriptionWeight);
		}
	}
	
	/**
	 * for serialization
	 * @param description 
	 * @see #setDescription(String)
	 */
	@SuppressWarnings("unused")
	private void setWDescription(WeightedStringElement description) {
		if(description == null){
			setDescription(null);
			setDescriptionWeight(null);
		}else{
			setDescription(description.getValue());
			setDescriptionWeight(description.getWeight());
		}
	}
	
	/**
	 * @see com.restfb.types.Event#getStartTime()
	 * @see #setStartTime(Date)
	 * 
	 * @return event start time
	 */
	@XmlJavaTypeAdapter(ISODateAdapter.class)
	@XmlElement(name = Definitions.ELEMENT_START_TIMESTAMP)
	public Date getStartTime() {
		return _event.getStartTime();
	}

	/**
	 * @see com.restfb.types.Event#getEndTime()
	 * @see #setEndTime(Date)
	 * 
	 * @return event end time
	 */
	@XmlJavaTypeAdapter(ISODateAdapter.class)
	@XmlElement(name = Definitions.ELEMENT_END_TIMESTAMP)
	public Date getEndTime() {
		return _event.getEndTime();
	}

	/**
	 * @see com.restfb.types.Event#getVenue()
	 * @see com.restfb.types.Event#getLocation()
	 * @see #setFacebookLocation(FacebookLocation)
	 * 
	 * @return location
	 */
	@XmlElement(name = Definitions.ELEMENT_LOCATION)
	public FacebookLocation getFacebookLocation() {
		FacebookLocation l = FacebookLocation.getFacebookLocation(_event.getVenue());
		if(l == null){
			l = FacebookLocation.getFacebookLocation(_event.getLocation());
		}
		return l;
	}
	
	/**
	 * @param location
	 * @see com.restfb.types.Event#setLocation(java.lang.String)
	 * @see #getFacebookLocation()
	 */
	public void setFacebookLocation(FacebookLocation location) {
		_event.setLocation((location == null ? null : location.getName()));
	}

	/**
	 * @see com.restfb.types.Event#getRsvpStatus()
	 * @see #setEventStatus(RSVPStatus)
	 * 
	 * @return status
	 */
	@XmlElement(name = Definitions.ELEMENT_RSVP_STATUS)
	public RSVPStatus getEventStatus(){
		return RSVPStatus.fromRSVPStatusString(_event.getRsvpStatus());
	}
	
	/**
	 * @param status
	 * @see com.restfb.types.Event#setRsvpStatus(java.lang.String)
	 * @see #getEventStatus()
	 */
	public void setEventStatus(RSVPStatus status) {
		_event.setRsvpStatus((status == null ? null : status.toRSVPStatusString()));
	}

	/**
	 * @see com.restfb.types.Event#getPrivacy()
	 * @see #setPrivacy(service.tut.pori.facebookjazz.FacebookGroup.Privacy)
	 * 
	 * @return privacy
	 */
	@XmlElement(name = Definitions.ELEMENT_PRIVACY)
	public Privacy getPrivacy() {
		return Privacy.fromPrivacyString(_event.getPrivacy()); 
	}
	
	/**
	 * @param privacy
	 * @see com.restfb.types.Event#setPrivacy(java.lang.String)
	 * @see #getPrivacy()
	 */
	public void setPrivacy(Privacy privacy) {
		_event.setPrivacy((privacy == null ? null : privacy.toPrivacyString()));
	}

	/**
	 * @see com.restfb.types.Event#getUpdatedTime()
	 * @see #setUpdatedTime(Date)
	 * 
	 * @return updated time
	 */
	@XmlJavaTypeAdapter(ISODateAdapter.class)
	@XmlElement(name = Definitions.ELEMENT_UPDATED_TIMESTAMP)
	public Date getUpdatedTime() {
		return _event.getUpdatedTime();
	}

	/**
	 * @see com.restfb.types.Event#getName()
	 * @see #getName()
	 * 
	 * @return name
	 */
	@XmlElement(name = Definitions.ELEMENT_NAME)
	public WeightedStringElement getWName() {
		String name = _event.getName();
		if(StringUtils.isBlank(name)){
			return null;
		}else{
			return new WeightedStringElement(name, _nameWeight);
		}
	}
	
	/**
	 * for serialization
	 * @param name 
	 * @see #setName(String)
	 */
	@SuppressWarnings("unused")
	private void setWName(WeightedStringElement name) {
		if(name == null){
			setName(null);
			setNameWeight(null);
		}else{
			setName(name.getValue());
			setNameWeight(name.getWeight());
		}
	}

	/**
	 * @return the nameWeight
	 * @see #setNameWeight(Integer)
	 */
	public Integer getNameWeight() {
		return _nameWeight;
	}

	/**
	 * @param nameWeight the nameWeight to set
	 * @see #getNameWeight()
	 */
	public void setNameWeight(Integer nameWeight) {
		_nameWeight = nameWeight;
	}

	/**
	 * @return the descriptionWeight
	 * @see #setDescriptionWeight(Integer)
	 */
	public Integer getDescriptionWeight() {
		return _descriptionWeight;
	}

	/**
	 * @param descriptionWeight the descriptionWeight to set
	 * @see #getDescriptionWeight()
	 */
	public void setDescriptionWeight(Integer descriptionWeight) {
		_descriptionWeight = descriptionWeight;
	}

	/**
	 * @param description
	 * @see com.restfb.types.Event#setDescription(java.lang.String)
	 * @see #getDescription()
	 */
	public void setDescription(String description) {
		_event.setDescription(description);
	}

	/**
	 * @param date
	 * @see com.restfb.types.Event#setEndTime(java.util.Date)
	 * @see #getEndTime()
	 */
	public void setEndTime(Date date) {
		_event.setEndTime(date);
	}

	/**
	 * @param name
	 * @see com.restfb.types.NamedFacebookType#setName(java.lang.String)
	 * @see #getName()
	 */
	public void setName(String name) {
		_event.setName(name);
	}

	/**
	 * @param date
	 * @see com.restfb.types.Event#setStartTime(java.util.Date)
	 * @see #getStartTime()
	 */
	public void setStartTime(Date date) {
		_event.setStartTime(date);
	}

	/**
	 * @param date
	 * @see com.restfb.types.Event#setUpdatedTime(java.util.Date)
	 * @see #getUpdatedTime()
	 */
	public void setUpdatedTime(Date date) {
		_event.setUpdatedTime(date);
	}
	
	/**
	 * 
	 * @param events
	 * @return the given events wrapped as facebook events or null if null or empty list was passed
	 */
	public static List<FacebookEvent> getFacebookEvents(List<Event> events){
		if(events == null || events.isEmpty()){
			return null;
		}
		List<FacebookEvent> fEvents = new ArrayList<>(events.size());
		for(Event e : events){
			fEvents.add(new FacebookEvent(e));
		}
		return fEvents;
	}

	/**
	 * @return description
	 * @see com.restfb.types.Event#getDescription()
	 * @see #setDescription(String)
	 */
	public String getDescription() {
		return _event.getDescription();
	}

	/**
	 * @return name
	 * @see com.restfb.types.NamedFacebookType#getName()
	 * @see #setName(String)
	 */
	public String getName() {
		return _event.getName();
	}
}
