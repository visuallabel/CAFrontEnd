/**
 * Copyright 2015 Tampere University of Technology, Pori Unit
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
package service.tut.pori.contentanalysis;

import service.tut.pori.contentanalysis.CAContentCore.Visibility;
import core.tut.pori.users.UserAuthority;
import core.tut.pori.users.UserIdentity;

/**
 * Contains access details for an object denoted by a GUID.
 * 
 */
public class AccessDetails {
	private Permission _permission = null; // what access the "accessor" has to the requested content
	private UserIdentity _accessorUserId = null; // who tried to resolve the content's owner details
	private UserIdentity _ownerUserId = null; // the content's actual owner
	private String _guid = null;

	/**
	 * access permission
	 */
	public enum Permission{
		/** there is no access available (access denied) */
		NO_ACCESS,
		/** private access to the content (probably the content's owner is accessing the content) */
		PRIVATE_ACCESS,
		/**  there is a public access to the content (the content is freely available) */
		PUBLIC_ACCESS,
		/** the accessing user was an back-end */
		BACKEND_ACCESS
	}

	/**
	 * 
	 * @param permission
	 * @param accessor
	 * @param owner
	 * @param guid
	 */
	public AccessDetails(Permission permission, UserIdentity accessor, UserIdentity owner, String guid){
		_permission = permission;
		_accessorUserId = accessor;
		_ownerUserId = owner;
		_guid = guid;
	}

	/**
	 * 
	 * @return access permission
	 */
	public Permission getPermission(){
		return _permission;
	}

	/**
	 * 
	 * @return user identity of the owner of the content
	 */
	public UserIdentity getOwner(){
		return _ownerUserId;
	}

	/**
	 * 
	 * @return user identity of the target of permission check
	 */
	public UserIdentity getAccessor(){
		return _accessorUserId;
	}

	/**
	 * @return the guid
	 */
	public String getGuid() {
		return _guid;
	}
	
	/**
	 * convert the photo object to photo access details object, userId, visibility and guid must be set.
	 * 
	 * @param authenticatedUser
	 * @param media
	 * @return access details for the photo for the given user
	 */
	public static AccessDetails getAccessDetails(UserIdentity authenticatedUser, Media media){
		UserIdentity ownerUserId = media.getOwnerUserId();
		String guid = media.getGUID();
		if(UserIdentity.equals(authenticatedUser, ownerUserId)){ // this is the owner
			return new AccessDetails(Permission.PRIVATE_ACCESS, authenticatedUser, ownerUserId, guid);
		}else if(UserIdentity.hasAuthority(UserAuthority.AUTHORITY_ROLE_BACKEND, authenticatedUser)){ // not owner, but has back-end permissions
			return new AccessDetails(Permission.BACKEND_ACCESS, authenticatedUser, ownerUserId, guid);
		}else if(media.getVisibility() == Visibility.PUBLIC){
			return new AccessDetails(Permission.PUBLIC_ACCESS, authenticatedUser, ownerUserId, guid);
		}else{
			return new AccessDetails(Permission.NO_ACCESS, authenticatedUser, ownerUserId, guid);
		}
	}
}
