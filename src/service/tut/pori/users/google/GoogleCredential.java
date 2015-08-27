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
package service.tut.pori.users.google;

import com.google.gson.annotations.SerializedName;

import core.tut.pori.users.UserIdentity;

/**
 * A custom implementation of Google Credential with additional details for user account.
 */
public class GoogleCredential {
	private transient UserIdentity _userId = null;
	@SerializedName(value="id")
	private String _id = null; // google user id
	@SerializedName(value="name")
	private String _name = null;
	@SerializedName(value="given_name")
	private String _givenName = null;
	@SerializedName(value="family_name")
	private String _familyName = null;
	@SerializedName(value="link")
	private String _link = null;
	@SerializedName(value="picture")
	private String _pictureLink = null;
	@SerializedName(value="gender")
	private String _gender = null;
	@SerializedName(value="locale")
	private String _locale = null;
	
	/**
	 * 
	 */
	protected GoogleCredential(){
		// nothing needed
	}

	/**
	 * @return the google user id
	 */
	public String getId() {
		return _id;
	}

	/**
	 * @return the userId
	 */
	public UserIdentity getUserId() {
		return _userId;
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return _name;
	}

	/**
	 * @return the givenName
	 */
	public String getGivenName() {
		return _givenName;
	}

	/**
	 * @return the familyName
	 */
	public String getFamilyName() {
		return _familyName;
	}

	/**
	 * @return the link
	 */
	public String getLink() {
		return _link;
	}

	/**
	 * @return the pictureLink
	 */
	public String getPictureLink() {
		return _pictureLink;
	}

	/**
	 * @return the gender
	 */
	public String getGender() {
		return _gender;
	}

	/**
	 * @return the locale
	 */
	public String getLocale() {
		return _locale;
	}

	/**
	 * 
	 * @param userId
	 */
	protected void setUserId(UserIdentity userId){
		_userId = userId;
	}
}
