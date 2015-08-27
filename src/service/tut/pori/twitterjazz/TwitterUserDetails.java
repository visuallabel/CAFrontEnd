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
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.log4j.Logger;

import twitter4j.User;
import core.tut.pori.users.UserIdentity;

/**
 * Details of a Twitter user.
 * 
 * <h2>Optional Elements</h2>
 * <ul>
 *  <li>{@value service.tut.pori.twitterjazz.Definitions#ELEMENT_BIO}</li>
 *  <li>{@value service.tut.pori.twitterjazz.Definitions#ELEMENT_LOCATION}</li>
 * </ul>
 * 
 * <h3>XML Example</h3>
 * 
 * {@doc.restlet service="[service.tut.pori.twitterjazz.reference.Definitions#SERVICE_TJ_REFERENCE_EXAMPLE]" method="[service.tut.pori.twitterjazz.Definitions#ELEMENT_USER_DETAILS]" type="GET" query="" body_uri=""}
 * 
 * @see twitter4j.User
 */
@XmlRootElement(name=Definitions.ELEMENT_USER_DETAILS)
@XmlAccessorType(XmlAccessType.NONE)
public class TwitterUserDetails {
	private static final Logger LOGGER = Logger.getLogger(TwitterUserDetails.class);
	@XmlElement(name=Definitions.ELEMENT_BIO)
	private String _bio = null;
	@XmlElement(name=Definitions.ELEMENT_FAVORITES_COUNT)
	private Integer _favoritesCount = null;
	@XmlElement(name=Definitions.ELEMENT_FRIENDS_COUNT)
	private Integer _friendsCount = null;
	@XmlElement(name=Definitions.ELEMENT_FOLLOWERS_COUNT)
	private Integer _followersCount = null;
	@XmlElement(name=Definitions.ELEMENT_NAME)
	private String _name = null;
	private Boolean _protected = null;
	@XmlElement(name=Definitions.ELEMENT_SCREEN_NAME)
	private String _screenName = null;
	@XmlElement(name=Definitions.ELEMENT_TWITTER_ID)
	private String _twitterId = null;
	private UserIdentity _userId = null;
	@XmlElement(name=Definitions.ELEMENT_LOCATION)
	private TwitterLocation _location = null;
	
	/**
	 * 
	 * @param user
	 * @return twitter user details or null if the passes user was null
	 */
	public static TwitterUserDetails getTwitterUserDetails(User user){
		if(user == null){
			LOGGER.warn("Null user.");
			return null;
		}
		TwitterUserDetails tud = new TwitterUserDetails();
		tud._bio = user.getDescription();
		tud._favoritesCount = user.getFavouritesCount();
		tud._friendsCount = user.getFriendsCount();
		tud._followersCount = user.getFollowersCount();
		tud._name = user.getName();
		tud._screenName = user.getScreenName();
		Long id = user.getId();
		tud._twitterId = (id == null ? null : String.valueOf(id));
		tud._location = TwitterLocation.getTwitterLocation(user.getLocation());
		tud._protected = user.isProtected();
		return tud;
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
	 * For serialization
	 * 
	 * @see #setUserId(UserIdentity)
	 * 
	 * @param userId
	 */
	@SuppressWarnings("unused")
	private void setUserIdValue(Long userId){
		_userId = (userId == null ? null : new UserIdentity(userId));
	}

	/**
	 * 
	 * @return user identity
	 */
	public UserIdentity getUserId() {
		return _userId;
	}

	/**
	 * 
	 * @param userId
	 */
	public void setUserId(UserIdentity userId) {
		_userId = userId;
	}

	/**
	 * 
	 * @param users
	 * @return the collection of user converted to twitter user details or null if null or empty collection was passed
	 */
	public static List<TwitterUserDetails> getTwitterUserDetails(Collection<User> users) {
		if(users == null || users.isEmpty()){
			LOGGER.warn("Null or empty user list.");
			return null;
		}
		
		List<TwitterUserDetails> details = new ArrayList<>(users.size());
		for(User user : users){
			TwitterUserDetails ud = getTwitterUserDetails(user);
			if(ud == null){
				LOGGER.warn("Ignored bad user object.");
			}else{
				details.add(ud);
			}
		}
		
		return (details.isEmpty() ? null : details);
	}

	/**
	 * @see twitter4j.User#getDescription()
	 * 
	 * @return the bio
	 */
	public String getBio() {
		return _bio;
	}

	/**
	 * @param bio the bio to set
	 */
	public void setBio(String bio) {
		_bio = bio;
	}

	/**
	 * @see twitter4j.User#getFavouritesCount()
	 * 
	 * @return the favoritesCount
	 */
	public Integer getFavoritesCount() {
		return _favoritesCount;
	}

	/**
	 * @param favoritesCount the favoritesCount to set
	 */
	public void setFavoritesCount(Integer favoritesCount) {
		_favoritesCount = favoritesCount;
	}

	/**
	 * @see twitter4j.User#getFriendsCount()
	 * 
	 * @return the friendsCount
	 */
	public Integer getFriendsCount() {
		return _friendsCount;
	}

	/**
	 * @param friendsCount the friendsCount to set
	 */
	public void setFriendsCount(Integer friendsCount) {
		_friendsCount = friendsCount;
	}

	/**
	 * @see twitter4j.User#getFollowersCount()
	 * 
	 * @return the followersCount
	 */
	public Integer getFollowersCount() {
		return _followersCount;
	}

	/**
	 * @param followersCount the followersCount to set
	 */
	public void setFollowersCount(Integer followersCount) {
		_followersCount = followersCount;
	}

	/**
	 * @see twitter4j.User#getName()
	 * 
	 * @return the name
	 */
	public String getName() {
		return _name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		_name = name;
	}

	/**
	 * @see twitter4j.User#isProtected()
	 * 
	 * @return the protected
	 */
	public Boolean isProtected() {
		return _protected;
	}

	/**
	 * @param protected1 the protected to set
	 */
	public void setProtected(Boolean protected1) {
		_protected = protected1;
	}

	/**
	 * @see twitter4j.User#getScreenName()
	 * 
	 * @return the screenName
	 */
	public String getScreenName() {
		return _screenName;
	}

	/**
	 * @param screenName the screenName to set
	 */
	public void setScreenName(String screenName) {
		_screenName = screenName;
	}

	/**
	 * @see twitter4j.User#getId()
	 * 
	 * @return the twitterId
	 */
	public String getTwitterId() {
		return _twitterId;
	}

	/**
	 * @param twitterId the twitterId to set
	 */
	public void setTwitterId(String twitterId) {
		_twitterId = twitterId;
	}

	/**
	 * @see twitter4j.User#getLocation()
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
}
