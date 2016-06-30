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
package service.tut.pori.contentanalysis.video;

import java.util.Collections;
import java.util.EnumSet;

import service.tut.pori.contentanalysis.PhotoParameters.AnalysisType;
import service.tut.pori.contentanalysis.CAContentCore.ServiceType;
import service.tut.pori.contentanalysis.Definitions;
import service.tut.pori.contentanalysis.MediaObjectList;
import core.tut.pori.context.ServiceInitializer;
import core.tut.pori.http.parameters.DataGroups;
import core.tut.pori.http.parameters.Limits;
import core.tut.pori.users.UserIdentity;

/**
 * Video search core methods.
 * 
 */
public final class VideoSearchCore {
	/**
	 * 
	 */
	private VideoSearchCore(){
		// nothing needed
	}

	/**
	 * 
	 * @param authenticatedUser
	 * @param objects
	 * @param dataGroups
	 * @param limits
	 * @param serviceTypes
	 * @param userIdFilters
	 * @return list of videos or null if none was found
	 * @throws IllegalArgumentException on bad input data
	 */
	public static VideoList similarVideosByObject(UserIdentity authenticatedUser, MediaObjectList objects, DataGroups dataGroups, Limits limits, EnumSet<ServiceType> serviceTypes, long[] userIdFilters) throws IllegalArgumentException {
		if(!MediaObjectList.isEmpty(objects)){
			return ServiceInitializer.getDAOHandler().getDAO(VideoDAO.class).search(authenticatedUser, dataGroups, null, limits, objects, serviceTypes, userIdFilters);
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
	 * @return list of videos or null if none was found
	 * @throws IllegalArgumentException
	 */
	public static VideoList searchByGUID(UserIdentity authenticatedUser, EnumSet<AnalysisType> analysisTypes, String guid, DataGroups dataGroups, Limits limits, EnumSet<ServiceType> serviceTypes, long[] userIdFilters) throws IllegalArgumentException{
		if(VideoList.isEmpty(ServiceInitializer.getDAOHandler().getDAO(VideoDAO.class).search(authenticatedUser, null, Collections.nCopies(1, guid), null, null, null, null))){  // do not use serviceTypes or userIdFilters for existence check of reference video
			throw new IllegalArgumentException("Invalid "+Definitions.PARAMETER_GUID+" or permission denied.");
		}
		
		return VideoSearchTask.getTaskByGUID(authenticatedUser, analysisTypes, dataGroups, guid, limits, serviceTypes, userIdFilters).execute();
	}
}
