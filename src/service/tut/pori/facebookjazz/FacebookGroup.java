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

import com.restfb.types.Group;
import com.restfb.types.NamedFacebookType;

import core.tut.pori.utils.ISODateAdapter;


/**
 * A group retrieved from Facebook.
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
 * {@doc.restlet service="[service.tut.pori.facebookjazz.reference.Definitions#SERVICE_FBJ_REFERENCE_EXAMPLE]" method="[service.tut.pori.facebookjazz.Definitions#ELEMENT_GROUP]" type="GET" query="" body_uri=""}
 *
 * @see com.restfb.types.Group
 */
@XmlRootElement(name=Definitions.ELEMENT_GROUP)
@XmlAccessorType(XmlAccessType.NONE)
public class FacebookGroup{
	private Integer _descriptionWeight = null;
	private Group _group = null;
	private Integer _nameWeight = null;

	/**
	 * Facebook Privacy setting.
	 */
	@XmlEnum
	public enum Privacy{
		/** group is open */
		OPEN,
		/** group is closed */
		CLOSED,
		/** group is not publicly visible */
		SECRET;

		/**
		 * 
		 * @return the privacy as string
		 */
		public String toPrivacyString(){
			return name();
		}

		/**
		 * 
		 * @param privacy
		 * @return the Privacy or null if invalid string
		 */
		public static Privacy fromPrivacyString(String privacy){
			if(privacy != null){
				for(Privacy p : Privacy.values()){
					if(p.toPrivacyString().equalsIgnoreCase(privacy)){
						return p;
					}
				}
			}
			return null;
		}
	}  // enum Privacy
	
	/**
	 * 
	 */
	public FacebookGroup(){
		_group = new Group();
	}
	
	/**
	 * 
	 * @param group
	 * @throws IllegalArgumentException
	 */
	public FacebookGroup(Group group) throws IllegalArgumentException{
		if(group == null){
			throw new IllegalArgumentException("Invalid group.");
		}
		_group = group;
	}

	/**
	 * @see com.restfb.types.Group#getDescription()
	 * @see #setDescription(String)
	 * 
	 * @return description
	 */
	@XmlElement(name = Definitions.ELEMENT_DESCRIPTION)
	public WeightedStringElement getWDescription() {
		String description = _group.getDescription();
		if(StringUtils.isBlank(description)){
			return null;
		}else{
			return new WeightedStringElement(description, _descriptionWeight);
		}
	}
	
	/**
	 * for serialization
	 * @param description
	 * @see #getDescription()
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
	 * @see com.restfb.types.Group#getOwner()
	 * @see com.restfb.types.NamedFacebookType#getName()
	 * @see #setGroupOwnerName(String)
	 * 
	 * @return owner name
	 */
	@XmlElement(name = Definitions.ELEMENT_OWNER)
	public String getGroupOwnerName() {
		NamedFacebookType owner = _group.getOwner();
		return (owner == null ? null : owner.getName());
	}
	
	/**
	 * @param name
	 * @see com.restfb.types.Group#setOwner(com.restfb.types.NamedFacebookType)
	 * @see #getGroupOwnerName()
	 */
	public void setGroupOwnerName(String name) {
		if(StringUtils.isBlank(name)){
			_group.setOwner(null);
		}else{
			NamedFacebookType owner = _group.getOwner();
			if(owner == null){
				owner = new NamedFacebookType();
				_group.setOwner(owner);
			}
			owner.setName(name);
		}
	}

	/**
	 * @see com.restfb.types.Group#getPrivacy()
	 * @see #setPrivacy(Privacy)
	 * 
	 * @return privacy
	 */
	@XmlElement(name = Definitions.ELEMENT_PRIVACY)
	public Privacy getPrivacy(){
		return Privacy.fromPrivacyString(_group.getPrivacy());  
	}
	
	/**
	 * @param privacy
	 * @see com.restfb.types.Group#setPrivacy(java.lang.String)
	 * @see #getPrivacy()
	 */
	public void setPrivacy(Privacy privacy) {
		_group.setPrivacy((privacy == null ? null : privacy.toPrivacyString()));
	}

	/**
	 * @see com.restfb.types.Group#getUpdatedTime()
	 * @see #setUpdatedTime(Date)
	 * 
	 * @return updated timestamp
	 */
	@XmlElement(name = Definitions.ELEMENT_UPDATED_TIMESTAMP)
	@XmlJavaTypeAdapter(ISODateAdapter.class)
	public Date getUpdatedTime() {
		return _group.getUpdatedTime();
	}

	/**
	 * @see com.restfb.types.Group#getName()
	 * @see #setName(String)
	 * 
	 * @return name
	 */
	@XmlElement(name = Definitions.ELEMENT_NAME)
	public WeightedStringElement getWName() {
		String name = _group.getName();
		if(StringUtils.isBlank(name)){
			return null;
		}else{
			return new WeightedStringElement(name, _nameWeight);
		}
	}
	
	/**
	 * for serialization
	 * @param name 
	 * @see #getName()
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
	 * @return name
	 * @see com.restfb.types.NamedFacebookType#getName()
	 * @see #setName(String)
	 */
	public String getName() {
		return _group.getName();
	}

	/**
	 * @see com.restfb.types.Group#getVenue()
	 * @see #setFacebookLocation(FacebookLocation)
	 * 
	 * @return location
	 */
	@XmlElement(name = Definitions.ELEMENT_LOCATION)
	public FacebookLocation getFacebookLocation() {
		return FacebookLocation.getFacebookLocation(_group.getVenue());
	}
	
	
	/**
	 * @param location
	 * @see com.restfb.types.Group#setVenue(com.restfb.types.Venue)
	 * @see #getFacebookLocation()
	 */
	public void setFacebookLocation(FacebookLocation location) {
		_group.setVenue((location == null ? null : location.toVenue()));
	}

	/**
	 * 
	 * @return description weight
	 * @see #setDescriptionWeight(Integer)
	 */
	public Integer getDescriptionWeight() {
		return _descriptionWeight;
	}

	/**
	 * 
	 * @param descriptionWeight
	 * @see #getDescriptionWeight()
	 */
	public void setDescriptionWeight(Integer descriptionWeight) {
		_descriptionWeight = descriptionWeight;
	}

	/**
	 * 
	 * @return name weight
	 * @see #setNameWeight(Integer)
	 */
	public Integer getNameWeight() {
		return _nameWeight;
	}

	/**
	 * 
	 * @param nameWeight
	 * @see #getNameWeight()
	 */
	public void setNameWeight(Integer nameWeight) {
		_nameWeight = nameWeight;
	}

	/**
	 * @param description
	 * @see com.restfb.types.Group#setDescription(java.lang.String)
	 * @see #getDescription()
	 */
	public void setDescription(String description) {
		_group.setDescription(description);
	}

	/**
	 * @param name
	 * @see com.restfb.types.NamedFacebookType#setName(java.lang.String)
	 * @see #getName()
	 */
	public void setName(String name) {
		_group.setName(name);
	}

	/**
	 * @param date
	 * @see com.restfb.types.Group#setUpdatedTime(java.util.Date)
	 * @see #getUpdatedTime()
	 */
	public void setUpdatedTime(Date date) {
		_group.setUpdatedTime(date);
	}
	
	/**
	 * 
	 * @param groups
	 * @return the groups wrapped to facebook groups or null if null or empty list was passed
	 */
	public static List<FacebookGroup> getFacebookGroups(List<Group> groups){
		List<FacebookGroup> fg = new ArrayList<>(groups.size());
		for(Group g : groups){
			fg.add(new FacebookGroup(g));
		}
		return fg;
	}

	/**
	 * @return description
	 * @see com.restfb.types.Group#getDescription()
	 * @see #setDescription(String)
	 */
	public String getDescription() {
		return _group.getDescription();
	}
}
