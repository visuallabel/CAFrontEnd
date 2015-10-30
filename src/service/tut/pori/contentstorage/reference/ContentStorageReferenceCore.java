/**
 * Copyright 2015 Tampere University of Technology, Pori Department
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
package service.tut.pori.contentstorage.reference;

import java.util.EnumSet;
import java.util.List;

import service.tut.pori.contentanalysis.CAContentCore.ServiceType;
import service.tut.pori.contentstorage.MediaList;
import service.tut.pori.contentstorage.URLContentStorage;
import twitter4j.Logger;
import core.tut.pori.http.parameters.Limits;
import core.tut.pori.users.UserIdentity;

/**
 * The reference implementations for Content Storage Service.
 *
 */
public final class ContentStorageReferenceCore {
	private static final ContentXMLObjectCreator CREATOR = new ContentXMLObjectCreator(null);
	private static final Logger LOGGER = Logger.getLogger(ContentStorageReferenceCore.class);
	private static final EnumSet<ServiceType> SERVICE_TYPES = EnumSet.of(URLContentStorage.SERVICE_TYPE);

	/**
	 * 
	 */
	private ContentStorageReferenceCore(){
		// nothing needed
	}
	
	/**
	 * 
	 * @param authenticatedUser
	 * @param backendId
	 * @param serviceTypes
	 */
	public static void synchronize(UserIdentity authenticatedUser, int[] backendId, EnumSet<ServiceType> serviceTypes) {
		LOGGER.info((authenticatedUser == null ? "No logged in user." : "Ignoring the logged in user, id: "+authenticatedUser.getUserId()));	// only notify of the logged in status
	}

	/**
	 * simulates adding URLs for analysis. Note that the URLs will not be validated by this method, and can be any strings.
	 * 
	 * @param authenticatedUser
	 * @param backendId value is ignored
	 * @param urls values themselves are ignored, count is used for populating appropriate media list
	 * @return media list or null if no URLs were given
	 */
	public static MediaList addUrls(UserIdentity authenticatedUser, int[] backendId, List<String> urls) {
		if(urls == null || urls.isEmpty()){
			LOGGER.debug("No URLs given.");
			return null;
		}
		
		if(UserIdentity.isValid(authenticatedUser)){
			LOGGER.info("Logged in user, id: "+authenticatedUser.getUserId());
		}else{
			LOGGER.info("No logged in user.");
			authenticatedUser = CREATOR.createUserIdentity();
		}
		Limits limits = new Limits(0, urls.size());
		
		return CREATOR.createMediaList(limits, SERVICE_TYPES, authenticatedUser);
	}

	/**
	 * 
	 * @param limits
	 * @return randomly generated media list
	 */
	public static MediaList generateMediaList(Limits limits) {
		return CREATOR.createMediaList(limits, SERVICE_TYPES, CREATOR.createUserIdentity());
	}
}
