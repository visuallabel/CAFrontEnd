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
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;

import com.restfb.types.NamedFacebookType;
import com.restfb.types.User;
import com.restfb.util.DateUtils;

import core.tut.pori.users.UserIdentity;
import core.tut.pori.utils.ISODateAdapter;

/**
 * output class for facebook user details.
 * 
 * <h2>Optional Elements</h2>
 * <ul>
 *  <li>{@value service.tut.pori.facebookjazz.Definitions#ELEMENT_BIO}</li>
 *  <li>{@value service.tut.pori.facebookjazz.Definitions#ELEMENT_BIRTHDAY}</li>
 *  <li>{@value service.tut.pori.facebookjazz.Definitions#ELEMENT_GENDER}</li>
 *  <li>{@value service.tut.pori.facebookjazz.Definitions#ELEMENT_HOMETOWN}</li>
 *  <li>{@value service.tut.pori.facebookjazz.Definitions#ELEMENT_LOCATION}</li>
 *  <li>{@value service.tut.pori.facebookjazz.Definitions#ELEMENT_POLITICAL}</li>
 *  <li>{@value service.tut.pori.facebookjazz.Definitions#ELEMENT_RELATIONSHIP_LIST}</li>
 *  <li>{@value service.tut.pori.facebookjazz.Definitions#ELEMENT_RELIGION}</li>
 * </ul>
 * 
 * <h3>XML Example</h3>
 * 
 * {@doc.restlet service="[service.tut.pori.facebookjazz.reference.Definitions#SERVICE_FBJ_REFERENCE_EXAMPLE]" method="[service.tut.pori.facebookjazz.Definitions#ELEMENT_USER_DETAILS]" type="GET" query="" body_uri=""}
 *
 * @see com.restfb.types.User
 */
@XmlRootElement(name=Definitions.ELEMENT_USER_DETAILS)
@XmlAccessorType(XmlAccessType.NONE)
public class FacebookUserDetails {
	private static final FastDateFormat RESTFB_FB_SHORT = FastDateFormat.getInstance(DateUtils.FACEBOOK_SHORT_DATE_FORMAT);
	private static final String SEPARATOR_LASTNAME_FIRSTNAME = ",";
	private static final String SEPARATOR_FIRSTNAME_MIDDLENAME = " ";
	private User _user = null;
	private UserIdentity _userId = null;
	
	/**
	 * 
	 */
	public FacebookUserDetails() {
		_user = new User();
	}
	
	/**
	 * 
	 * @param user
	 * @throws IllegalArgumentException 
	 */
	public FacebookUserDetails(User user) throws IllegalArgumentException{
		if(user == null){
			throw new IllegalArgumentException("Invalid user.");
		}
		_user = user;
	}

	/**
	 * @see com.restfb.types.User#getBio()
	 * @see #setBio(String)
	 * 
	 * @return user's bio
	 */
	@XmlElement(name = Definitions.ELEMENT_BIO)
	public String getBio() {
		return _user.getBio();
	}

	/**
	 * @see com.restfb.types.User#getFirstName()
	 * @see com.restfb.types.User#getMiddleName()
	 * @see com.restfb.types.User#getLastName()
	 * 
	 * @return full name of the user in format: LASTNAME, FIRSTNAME MIDDLENAME
	 */
	@XmlElement(name = Definitions.ELEMENT_NAME)
	public String getFullName() {
		StringBuilder name = new StringBuilder(_user.getLastName());
		name.append(SEPARATOR_LASTNAME_FIRSTNAME);
		name.append(' ');
		name.append(_user.getFirstName());
		String middleName = _user.getMiddleName();
		if(!StringUtils.isBlank(middleName)){
			name.append(SEPARATOR_FIRSTNAME_MIDDLENAME);
			name.append(middleName);
		}
		return name.toString();
	}
	
	/**
	 * For serialization
	 * 
	 * @see com.restfb.types.User#setFirstName(String)
	 * @see com.restfb.types.User#setMiddleName(String)
	 * @see com.restfb.types.User#setLastName(String)
	 * 
	 * @param name full name of the user in format: LASTNAME, FIRSTNAME MIDDLENAME where MIDDLENAME is optional
	 * @throws IllegalArgumentException
	 */
	@SuppressWarnings("unused")
	private void setFullName(String name) throws IllegalArgumentException{
		String firstname = null;
		String middlename = null;
		String lastname = null;
		String[] parts = StringUtils.split(name, SEPARATOR_LASTNAME_FIRSTNAME);
		if(!ArrayUtils.isEmpty(parts)){
			lastname = parts[0].trim();
			if(parts.length == 2){
				parts = StringUtils.split(parts[1].trim(), SEPARATOR_FIRSTNAME_MIDDLENAME, 1); // split once so that multiple names will be split to a single firstname and the rest will be as "middle name"
				firstname = parts[0].trim();
				if(parts.length > 1){
					middlename = parts[1].trim();
				}
			}else{
				throw new IllegalArgumentException("Invalid fullname: "+name);
			}
		}
		_user.setFirstName(firstname);
		_user.setMiddleName(middlename);
		_user.setLastName(lastname);
	}

	/**
	 * @see com.restfb.types.User#getUpdatedTime()
	 * @see #setUpdatedTime(Date)
	 * 
	 * @return updated timestamp
	 */
	@XmlJavaTypeAdapter(ISODateAdapter.class)
	@XmlElement(name = Definitions.ELEMENT_UPDATED_TIMESTAMP)
	public Date getUpdatedTime() {
		return _user.getUpdatedTime();
	}

	/**
	 * @see com.restfb.types.User#getBirthdayAsDate()
	 * @see #setBirthdayAsDate(Date)
	 * 
	 * @return birthday
	 */
	@XmlJavaTypeAdapter(ISODateAdapter.class)
	@XmlElement(name = Definitions.ELEMENT_BIRTHDAY)
	public Date getBirthdayAsDate() {
		return _user.getBirthdayAsDate();
	}

	/**
	 * @see com.restfb.types.User#getGender()
	 * @see #setGender(String)
	 * 
	 * @return gender
	 */
	@XmlElement(name = Definitions.ELEMENT_GENDER)
	public String getGender() {
		String gender = _user.getGender();
		if(gender != null){
			gender = gender.toUpperCase();   // make sure it is always upper case
		}
		return gender;
	}

	/**
	 * 
	 * @return list of relationships or null if none
	 */
	@XmlElementWrapper(name = Definitions.ELEMENT_RELATIONSHIP_LIST)
	@XmlElement(name = Definitions.ELEMENT_RELATIONSHIP)
	public List<FacebookRelationship> getRelationShip() {
		FacebookRelationship r = FacebookRelationship.getFacebookRelationShip(_user);
		if(r == null){
			return null;
		}else{
			return new ArrayList<>(Arrays.asList(r));
		}
	}
	
	/**
	 * for serialization
	 * 
	 * @param relations currently only a single relationship is supported
	 * @throws IllegalArgumentException
	 */
	@SuppressWarnings("unused")
	private void setRelationShip(List<FacebookRelationship> relations) throws IllegalArgumentException{
		if(relations == null || relations.isEmpty()){
			new FacebookRelationship().toUser(_user);
		}else if(relations.size() > 1){
			throw new IllegalArgumentException("Only a single relationship is supported.");
		}else{
			relations.get(0).toUser(_user);
		}
	}

	/**
	 * @see com.restfb.types.User#getHometownName()
	 * @see #setHometownName(String)
	 * 
	 * @return hometown
	 */
	@XmlElement(name = Definitions.ELEMENT_HOMETOWN)
	public String getHometownName() {
		return _user.getHometownName();
	}
	
	/**
	 * @param name
	 * @see com.restfb.types.User#setHometown(com.restfb.types.NamedFacebookType)
	 * @see #getHometownName()
	 */
	public void setHometownName(String name) {
		NamedFacebookType hometown = new NamedFacebookType();
		hometown.setName(name);
		_user.setHometown(hometown);
	}

	/**
	 * @see com.restfb.types.User#getLocation()
	 * @see #setFacebookLocation(FacebookLocation)
	 * 
	 * @return location
	 */
	@XmlElement(name = Definitions.ELEMENT_LOCATION)
	public FacebookLocation getFacebookLocation() {
		return FacebookLocation.getFacebookLocation(_user.getLocation());
	}
	
	/**
	 * @see com.restfb.types.User#setLocation(NamedFacebookType)
	 * @see #getFacebookLocation()
	 * 
	 * @param location
	 */
	public void setFacebookLocation(FacebookLocation location) {
		_user.setLocation((location == null ? null : location.toNamedFacebookType()));
	}

	/**
	 * @see com.restfb.types.User#getPolitical()
	 * @see #setPolitical(String)
	 * 
	 * @return political
	 */
	@XmlElement(name = Definitions.ELEMENT_POLITICAL)
	public String getPolitical() {
		return _user.getPolitical();
	}

	/**
	 * @see com.restfb.types.User#getReligion()
	 * @see #setReligion(String)
	 * 
	 * @return religion
	 */
	@XmlElement(name = Definitions.ELEMENT_RELIGION)
	public String getReligion() {
		return _user.getReligion();
	}

	/**
	 * @see #getUserId()
	 * 
	 * @return user identity value
	 */
	@XmlElement(name = core.tut.pori.users.Definitions.ELEMENT_USER_ID)
	public Long getUserIdValue(){
		return (_userId == null ? null : _userId.getUserId());
	}
	
	/**
	 * for serialization
	 * 
	 * @param value 
	 * @see #setUserId(UserIdentity)
	 */
	@SuppressWarnings("unused")
	private void setUserIdValue(Long value){
		setUserId((value == null ? null : new UserIdentity(value)));
	}

	/**
	 * 
	 * @return user identity
	 * @see #setUserId(UserIdentity)
	 */
	public UserIdentity getUserId() {
		return _userId;
	}

	/**
	 * 
	 * @param userId
	 * @see #getUserId()
	 */
	public void setUserId(UserIdentity userId) {
		_userId = userId;
	}

	/**
	 * @see com.restfb.types.User#getId()
	 * @see #setId(String)
	 * 
	 * @return facebook user id
	 */
	@XmlElement(name = Definitions.ELEMENT_FACEBOOK_ID)
	public String getId() {
		return _user.getId();
	}

	/**
	 * @param id
	 * @see com.restfb.types.FacebookType#setId(java.lang.String)
	 * @see #getId()
	 */
	public void setId(String id) {
		_user.setId(id);
	}

	/**
	 * @param bio
	 * @see com.restfb.types.User#setBio(java.lang.String)
	 * @see #getBio()
	 */
	public void setBio(String bio) {
		_user.setBio(bio);
	}

	/**
	 * @param birthday
	 * @see com.restfb.types.User#setBirthday(java.lang.String)
	 * @see #getBirthdayAsDate()
	 */
	public void setBirthdayAsDate(Date birthday) {
		_user.setBirthday(toRestFBShortDateString(birthday));
	}

	/**
	 * @param name
	 * @see com.restfb.types.User#setFirstName(java.lang.String)
	 */
	public void setFirstName(String name) {
		_user.setFirstName(name);
	}

	/**
	 * @param gender
	 * @see com.restfb.types.User#setGender(java.lang.String)
	 * @see #getGender()
	 */
	public void setGender(String gender) {
		_user.setGender(gender);
	}

	/**
	 * @param name
	 * @see com.restfb.types.User#setLastName(java.lang.String)
	 */
	public void setLastName(String name) {
		_user.setLastName(name);
	}

	/**
	 * @param name
	 * @see com.restfb.types.User#setMiddleName(java.lang.String)
	 */
	public void setMiddleName(String name) {
		_user.setMiddleName(name);
	}

	/**
	 * @param political
	 * @see com.restfb.types.User#setPolitical(java.lang.String)
	 * @see #getPolitical()
	 */
	public void setPolitical(String political) {
		_user.setPolitical(political);
	}

	/**
	 * @param status
	 * @see com.restfb.types.User#setRelationshipStatus(java.lang.String)
	 * @see #getRelationShip()
	 */
	public void setRelationshipStatus(String status) {
		_user.setRelationshipStatus(status);
	}

	/**
	 * @param religion
	 * @see com.restfb.types.User#setReligion(java.lang.String)
	 * @see #getReligion()
	 */
	public void setReligion(String religion) {
		_user.setReligion(religion);
	}

	/**
	 * @param sOther
	 * @see com.restfb.types.User#setSignificantOther(com.restfb.types.NamedFacebookType)
	 * @see #getRelationShip()
	 */
	public void setSignificantOther(NamedFacebookType sOther) {
		_user.setSignificantOther(sOther);
	}

	/**
	 * @param date
	 * @see com.restfb.types.User#setUpdatedTime(java.util.Date)
	 * @see #getUpdatedTime()
	 */
	public void setUpdatedTime(Date date) {
		_user.setUpdatedTime(date);
	}

	/**
	 * @return facebook user name
	 * @see com.restfb.types.NamedFacebookType#getName()
	 */
	public String getName() {
		return _user.getName();
	}
	
	/**
	 * 
	 * @param date
	 * @return the given date converted to string in RestFB short format or null if null was passed
	 */
	public static String toRestFBShortDateString(Date date){
		if(date == null){
			return null;
		}
		synchronized (RESTFB_FB_SHORT) {
			return RESTFB_FB_SHORT.format(date);
		}
	}
}
