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
package service.tut.pori.contentanalysis;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import service.tut.pori.contentanalysis.PhotoParameters.AnalysisType;
import service.tut.pori.contentanalysis.CAContentCore.ServiceType;
import core.tut.pori.context.ServiceInitializer;
import core.tut.pori.http.parameters.DataGroups;
import core.tut.pori.http.parameters.Limits;
import core.tut.pori.users.UserIdentity;


/**
 * 
 * This class includes functions related to search queries
 * 
 */
public final class CASearchCore {

	/**
	 * 
	 */
	private CASearchCore() {
		// nothing needed
	}

	/**
	 * 
	 * @param authenticatedUser
	 * @param keywords
	 * @param dataGroups
	 * @param limits
	 * @param serviceTypes
	 * @param userIdFilters
	 * @return list of photos or null if none was found
	 */
	public static PhotoList searchByKeyword(UserIdentity authenticatedUser, List<String> keywords, DataGroups dataGroups, Limits limits, EnumSet<ServiceType> serviceTypes, long[] userIdFilters) {
		return ServiceInitializer.getDAOHandler().getSolrDAO(PhotoDAO.class).search(authenticatedUser, dataGroups, null, limits, MediaObjectList.getMediaObjectListFromKeywords(keywords), serviceTypes, userIdFilters);
	}

	/**
	 * 
	 * This will synchronously execute a content based search
	 * 
	 * The searchTask is not stored in the database 
	 * (because in case of server crash or other error the delay caused by restart will 
	 *  probably make the search irrelevant to the user anyway)
	 * 
	 * @param authenticatedUser
	 * @param analysisTypes 
	 * @param url
	 * @param dataGroups
	 * @param limits
	 * @param serviceTypes
	 * @param userIdFilters
	 * @return list of photos or null if none was found
	 */
	public static PhotoList searchByContent(UserIdentity authenticatedUser, EnumSet<AnalysisType> analysisTypes, String url, DataGroups dataGroups, Limits limits, EnumSet<ServiceType> serviceTypes, long[] userIdFilters) {
		return PhotoSearchTask.getTaskByUrl(authenticatedUser, analysisTypes, dataGroups, limits, serviceTypes, url, userIdFilters).execute(); 
	}

	/**
	 * 
	 * @param authenticatedUser
	 * @param objects
	 * @param dataGroups
	 * @param limits
	 * @param serviceTypes
	 * @param userIdFilters
	 * @return list of photos or null if none was found
	 * @throws IllegalArgumentException on bad input data
	 */
	public static PhotoList similarPhotosByObject(UserIdentity authenticatedUser, MediaObjectList objects, DataGroups dataGroups, Limits limits, EnumSet<ServiceType> serviceTypes, long[] userIdFilters) throws IllegalArgumentException{
		if(!MediaObjectList.isEmpty(objects)){
			return ServiceInitializer.getDAOHandler().getSolrDAO(PhotoDAO.class).search(authenticatedUser, dataGroups, null, limits, objects, serviceTypes, userIdFilters);
		}else{
			throw new IllegalArgumentException("Empty object list.");
		}
	}

	/**
	 * 
	 * @param authenticatedUser
	 * @param analysisTypes 
	 * @param guid
	 * @param dataGroups
	 * @param limits
	 * @param serviceTypes
	 * @param userIdFilters
	 * @return list of photos or null if none was found
	 * @throws IllegalArgumentException
	 */
	public static PhotoList searchByGUID(UserIdentity authenticatedUser, EnumSet<AnalysisType> analysisTypes, String guid, DataGroups dataGroups, Limits limits, EnumSet<ServiceType> serviceTypes, long[] userIdFilters) throws IllegalArgumentException{
		if(PhotoList.isEmpty(ServiceInitializer.getDAOHandler().getSolrDAO(PhotoDAO.class).search(authenticatedUser, null, Collections.nCopies(1, guid), null, null, null, null))){  // do not use serviceTypes or userIdFilters for existence check of reference photo
			throw new IllegalArgumentException("Invalid "+Definitions.PARAMETER_GUID+" or permission denied.");
		}
		
		return PhotoSearchTask.getTaskByGUID(authenticatedUser, analysisTypes, dataGroups, guid, limits, serviceTypes, userIdFilters).execute();
	}
}
