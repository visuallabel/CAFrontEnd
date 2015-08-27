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
package service.tut.pori.contentanalysis.video;

import service.tut.pori.contentanalysis.AbstractTaskDetails;
import service.tut.pori.contentanalysis.PhotoParameters.AnalysisType;
import service.tut.pori.contentanalysis.CAContentCore.ServiceType;
import service.tut.pori.contentanalysis.MediaObjectList;
import core.tut.pori.http.RedirectResponse;
import core.tut.pori.http.Response;
import core.tut.pori.http.Response.Status;
import core.tut.pori.http.annotations.HTTPAuthenticationParameter;
import core.tut.pori.http.annotations.HTTPMethodParameter;
import core.tut.pori.http.annotations.HTTPService;
import core.tut.pori.http.annotations.HTTPServiceMethod;
import core.tut.pori.http.parameters.AuthenticationParameter;
import core.tut.pori.http.parameters.DataGroups;
import core.tut.pori.http.parameters.InputStreamParameter;
import core.tut.pori.http.parameters.IntegerParameter;
import core.tut.pori.http.parameters.Limits;
import core.tut.pori.http.parameters.LongParameter;
import core.tut.pori.http.parameters.StringParameter;
import core.tut.pori.utils.XMLFormatter;

/**
 * This service enables content analysis and provides search features.
 * 
 * @see service.tut.pori.contentanalysis.video.reference.ClientService
 * @see service.tut.pori.contentanalysis.video.reference.ServerService
 */
@HTTPService(name=Definitions.SERVICE_VCA)
public class VideoAnalysisService {
	private XMLFormatter _formatter = new XMLFormatter();
	
	/**
	 * @see service.tut.pori.contentanalysis.video.reference.ClientService#r(AuthenticationParameter, IntegerParameter, StringParameter)
	 * 
	 * @param authenticatedUser
	 * @param serviceId
	 * @param guid
	 * @return response
	 */
	@HTTPServiceMethod(name = service.tut.pori.contentanalysis.Definitions.METHOD_REDIRECT, acceptedMethods={core.tut.pori.http.Definitions.METHOD_GET})
	public RedirectResponse r(
			@HTTPAuthenticationParameter(required = false) AuthenticationParameter authenticatedUser,
			@HTTPMethodParameter(name = service.tut.pori.contentanalysis.Definitions.PARAMETER_SERVICE_ID) IntegerParameter serviceId,
			@HTTPMethodParameter(name = service.tut.pori.contentanalysis.Definitions.PARAMETER_GUID) StringParameter guid
			)
	{
		return VideoContentCore.generateTargetUrl(authenticatedUser.getUserIdentity(), ServiceType.fromServiceId(serviceId.getValue()), guid.getValue());
	}
	
	/**
	 * @see service.tut.pori.contentanalysis.video.reference.ServerService#queryTaskDetails(IntegerParameter, LongParameter, DataGroups, Limits)
	 * 
	 * @param taskId
	 * @param backendId
	 * @param dataGroups
	 * @param limits
	 * @return response
	 */
	@HTTPServiceMethod(name = service.tut.pori.contentanalysis.Definitions.METHOD_QUERY_TASK_DETAILS, acceptedMethods={core.tut.pori.http.Definitions.METHOD_GET})
	public Response queryTaskDetails(
			@HTTPMethodParameter(name = service.tut.pori.contentanalysis.Definitions.PARAMETER_BACKEND_ID) IntegerParameter backendId,
			@HTTPMethodParameter(name = service.tut.pori.contentanalysis.Definitions.PARAMETER_TASK_ID) LongParameter taskId,
			@HTTPMethodParameter(name = DataGroups.PARAMETER_DEFAULT_NAME, required = false) DataGroups dataGroups,
			@HTTPMethodParameter(name = Limits.PARAMETER_DEFAULT_NAME, required = false) Limits limits
			) 
	{
		AbstractTaskDetails details = VideoContentCore.queryTaskDetails(backendId.getValue(), taskId.getValue(), dataGroups, limits);
		if(details == null){
			return new Response(Status.BAD_REQUEST, "Invalid "+service.tut.pori.contentanalysis.Definitions.PARAMETER_TASK_ID+" or "+service.tut.pori.contentanalysis.Definitions.PARAMETER_BACKEND_ID+".");
		}else{
			return new Response(details);
		}
	}

	/**
	 * @see service.tut.pori.contentanalysis.video.reference.ServerService#taskFinished(InputStreamParameter)
	 * 
	 * @param xml
	 */
	@HTTPServiceMethod(name = service.tut.pori.contentanalysis.Definitions.METHOD_TASK_FINISHED, acceptedMethods={core.tut.pori.http.Definitions.METHOD_POST})
	public void taskFinished(@HTTPMethodParameter(name = InputStreamParameter.PARAMETER_DEFAULT_NAME, bodyParameter = true) InputStreamParameter xml) {
		VideoContentCore.taskFinished(_formatter.toObject(xml.getValue(), VideoTaskResponse.class));
	}
	
	/**
	 * @see service.tut.pori.contentanalysis.video.reference.ClientService#similarVideosByObject(AuthenticationParameter, DataGroups, Limits, IntegerParameter, LongParameter, InputStreamParameter)
	 * 
	 * @param authenticatedUser
	 * @param dataGroups
	 * @param limits
	 * @param serviceIds
	 * @param userIdFilters
	 * @param xml
	 * @return response
	 */
	@HTTPServiceMethod(name = Definitions.METHOD_SEARCH_SIMILAR_BY_OBJECT, acceptedMethods={core.tut.pori.http.Definitions.METHOD_POST})
	public Response similarVideosByObject(
			@HTTPAuthenticationParameter(required = false) AuthenticationParameter authenticatedUser,
			@HTTPMethodParameter(name = DataGroups.PARAMETER_DEFAULT_NAME, required = false) DataGroups dataGroups,
			@HTTPMethodParameter(name = Limits.PARAMETER_DEFAULT_NAME, required = false) Limits limits,
			@HTTPMethodParameter(name = service.tut.pori.contentanalysis.Definitions.PARAMETER_SERVICE_ID, required = false) IntegerParameter serviceIds,
			@HTTPMethodParameter(name = service.tut.pori.users.Definitions.PARAMETER_USER_ID, required = false) LongParameter userIdFilters,
			@HTTPMethodParameter(name = InputStreamParameter.PARAMETER_DEFAULT_NAME, bodyParameter = true) InputStreamParameter xml
			)
	{
		return new Response(VideoSearchCore.similarVideosByObject(authenticatedUser.getUserIdentity(), _formatter.toObject(xml.getValue(), MediaObjectList.class), dataGroups, limits, ServiceType.fromIdArray(serviceIds.getValues()), userIdFilters.getValues()));
	}
	
	/**
	 * @see service.tut.pori.contentanalysis.video.reference.ClientService#getVideos(AuthenticationParameter, StringParameter, DataGroups, Limits, IntegerParameter, LongParameter)
	 * 
	 * @param authenticatedUser
	 * @param guid
	 * @param dataGroups
	 * @param limits
	 * @param serviceIds
	 * @param userIds 
	 * @return response
	 */
	@HTTPServiceMethod(name = Definitions.METHOD_RETRIEVE_VIDEO_METADATA, acceptedMethods={core.tut.pori.http.Definitions.METHOD_GET})
	public Response getVideos(
			@HTTPAuthenticationParameter(required = false) AuthenticationParameter authenticatedUser,
			@HTTPMethodParameter(name = service.tut.pori.contentanalysis.Definitions.PARAMETER_GUID, required = false) StringParameter guid,
			@HTTPMethodParameter(name = DataGroups.PARAMETER_DEFAULT_NAME, required = false) DataGroups dataGroups,
			@HTTPMethodParameter(name = Limits.PARAMETER_DEFAULT_NAME, required = false) Limits limits,
			@HTTPMethodParameter(name = service.tut.pori.contentanalysis.Definitions.PARAMETER_SERVICE_ID, required = false) IntegerParameter serviceIds,
			@HTTPMethodParameter(name = service.tut.pori.users.Definitions.PARAMETER_USER_ID, required = false) LongParameter userIds
			)
	{
		return new Response(VideoContentCore.getVideos(authenticatedUser.getUserIdentity(), guid.getValues(), dataGroups, limits, ServiceType.fromIdArray(serviceIds.getValues()), userIds.getValues()));
	}
	
	/**
	 * Returns videos that are similar with the video associated with the given id.
	 * 
	 * @see service.tut.pori.contentanalysis.video.reference.ClientService#searchSimilarById(AuthenticationParameter, StringParameter, StringParameter, DataGroups, Limits, IntegerParameter, LongParameter)
	 * 
	 * @param authenticatedUser
	 * @param guid
	 * @param analysisType 
	 * @param dataGroups
	 * @param limits
	 * @param serviceIds
	 * @param userIdFilters
	 * @return response
	 */
	@HTTPServiceMethod(name = Definitions.METHOD_SEARCH_SIMILAR_BY_ID, acceptedMethods={core.tut.pori.http.Definitions.METHOD_GET})
	public Response searchSimilarById(
			@HTTPAuthenticationParameter(required = false) AuthenticationParameter authenticatedUser,
			@HTTPMethodParameter(name = service.tut.pori.contentanalysis.Definitions.PARAMETER_GUID) StringParameter guid,
			@HTTPMethodParameter(name = service.tut.pori.contentanalysis.Definitions.PARAMETER_ANALYSIS_TYPE, required = false) StringParameter analysisType,
			@HTTPMethodParameter(name = DataGroups.PARAMETER_DEFAULT_NAME, required = false) DataGroups dataGroups,
			@HTTPMethodParameter(name = Limits.PARAMETER_DEFAULT_NAME, required = false) Limits limits,
			@HTTPMethodParameter(name = service.tut.pori.contentanalysis.Definitions.PARAMETER_SERVICE_ID, required = false) IntegerParameter serviceIds,
			@HTTPMethodParameter(name = service.tut.pori.users.Definitions.PARAMETER_USER_ID, required = false) LongParameter userIdFilters
			) 
	{
		return new Response(VideoSearchCore.searchByGUID(authenticatedUser.getUserIdentity(), AnalysisType.fromAnalysisTypeString(analysisType.getValues()), guid.getValue(), dataGroups, limits, ServiceType.fromIdArray(serviceIds.getValues()), userIdFilters.getValues()));
	}
}
