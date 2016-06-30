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

import java.util.Date;

import core.tut.pori.users.UserIdentity;

/**
 * Twitter OAuth request token.
 */
public class RequestToken {
	private String _redirectUri = null;
	private String _secret = null;
	private String _token = null;
	private Date _updated = null;
	private UserIdentity _userId = null;
	private String _verifier = null;
	
	/**
	 * 
	 */
	public RequestToken(){
		// nothing needed
	}
	
	/**
	 * 
	 * @param redirectUri
	 * @param token
	 * @param tokenSecret
	 * @param userId
	 */
	public RequestToken(String redirectUri, String token, String tokenSecret, UserIdentity userId){
		_redirectUri = redirectUri;
		_token = token;
		_secret = tokenSecret;
		_userId = userId;
	}

	/**
	 * @return the requestToken
	 */
	public String getToken() {
		return _token;
	}

	/**
	 * @param requestToken the requestToken to set
	 */
	public void setToken(String requestToken) {
		_token = requestToken;
	}

	/**
	 * @return the requestTokenSecret
	 */
	public String getSecret() {
		return _secret;
	}

	/**
	 * @param requestTokenSecret the requestTokenSecret to set
	 */
	public void setSecret(String requestTokenSecret) {
		_secret = requestTokenSecret;
	}

	/**
	 * @return the redirectUri
	 */
	public String getRedirectUri() {
		return _redirectUri;
	}

	/**
	 * @param redirectUri the redirectUri to set
	 */
	public void setRedirectUri(String redirectUri) {
		_redirectUri = redirectUri;
	}

	/**
	 * @return the updated
	 */
	public Date getUpdated() {
		return _updated;
	}

	/**
	 * @param updated the updated to set
	 */
	public void setUpdated(Date updated) {
		_updated = updated;
	}

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
	 * 
	 * @return user id value or null if no user id given
	 */
	public Long getUserIdValue(){
		return (UserIdentity.isValid(_userId) ? _userId.getUserId() : null);
	}

	/**
	 * @return the tokenVerifier
	 */
	public String getVerifier() {
		return _verifier;
	}

	/**
	 * @param tokenVerifier the tokenVerifier to set
	 */
	public void setVerifier(String tokenVerifier) {
		_verifier = tokenVerifier;
	}
}
