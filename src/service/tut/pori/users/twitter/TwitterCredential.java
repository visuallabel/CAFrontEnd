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
package service.tut.pori.users.twitter;

import com.google.gson.annotations.SerializedName;

import core.tut.pori.users.UserIdentity;

/**
 * User credentials for a Twitter account.
 */
public class TwitterCredential {
	private transient UserIdentity _userId = null;
	@SerializedName(value="id")
	private String _id = null;
	@SerializedName(value="name")
	private String _name = null;
	@SerializedName(value="screen_name")
	private String _screenName = null;
	@SerializedName(value="protected")
	private Boolean _protectedAccount = null;
	@SerializedName(value="lang")
	private String _lang = null;
	@SerializedName(value="utc_offset")
	private Integer _utcOffSet = null; // in seconds
	@SerializedName(value="verified")
	private Boolean _verified = null;
	
	/**
	 * @return the userId
	 */
	public UserIdentity getUserId() {
		return _userId;
	}
	
	/**
	 * @param userId the userId to set
	 */
	public void setUserId(UserIdentity userId) {
		_userId = userId;
	}
	
	/**
	 * @return the id
	 */
	public String getId() {
		return _id;
	}
	
	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		_id = id;
	}
	
	/**
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
	 * @return the protectedAccount
	 */
	public Boolean getProtectedAccount() {
		return _protectedAccount;
	}
	
	/**
	 * @param protectedAccount the protectedAccount to set
	 */
	public void setProtectedAccount(Boolean protectedAccount) {
		_protectedAccount = protectedAccount;
	}
	
	/**
	 * @return the lang
	 */
	public String getLang() {
		return _lang;
	}
	
	/**
	 * @param lang the lang to set
	 */
	public void setLang(String lang) {
		_lang = lang;
	}
	
	/**
	 * @return the utcOffSet
	 */
	public Integer getUtcOffSet() {
		return _utcOffSet;
	}
	
	/**
	 * @param utcOffSet the utcOffSet to set
	 */
	public void setUtcOffSet(Integer utcOffSet) {
		_utcOffSet = utcOffSet;
	}
	
	/**
	 * @return the verified
	 */
	public Boolean getVerified() {
		return _verified;
	}
	
	/**
	 * @param verified the verified to set
	 */
	public void setVerified(Boolean verified) {
		_verified = verified;
	}
}
