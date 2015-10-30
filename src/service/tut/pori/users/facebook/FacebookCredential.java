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
package service.tut.pori.users.facebook;

import com.google.gson.annotations.SerializedName;

import core.tut.pori.users.UserIdentity;

/**
 * Facebook user credential with user details.
 *
 */
public class FacebookCredential {
	private transient UserIdentity _userId = null;
	@SerializedName(value="id")
	private String _id = null;
	@SerializedName(value="name")
	private String _name = null;
	@SerializedName(value="first_name")
	private String _givenName = null;
	@SerializedName(value="last_name")
	private String _familyName = null;
	@SerializedName(value="link")
	private String _link = null;
	@SerializedName(value="gender")
	private String _gender = null;
	@SerializedName(value="locale")
	private String _locale = null;
	@SerializedName(value="email")
	private String _email = null;
	
	/**
	 * @param userId the userId to set
	 */
	protected void setUserId(UserIdentity userId) {
		_userId = userId;
	}

	/**
	 * @return the userId
	 */
	public UserIdentity getUserId() {
		return _userId;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return _id;
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
	 * @return the email
	 */
	public String getEmail() {
		return _email;
	}
}
