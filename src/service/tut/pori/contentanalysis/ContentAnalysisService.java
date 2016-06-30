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
package service.tut.pori.contentanalysis;

import java.util.EnumSet;

import service.tut.pori.contentanalysis.PhotoParameters.AnalysisType;
import service.tut.pori.contentanalysis.CAContentCore.ServiceType;
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
import core.tut.pori.utils.MediaUrlValidator.MediaType;
import core.tut.pori.utils.XMLFormatter;


/**
 * This service enables content analysis and provides search features.
 * 
 * @see service.tut.pori.contentanalysis.reference.ClientService
 * @see service.tut.pori.contentanalysis.reference.ServerService
 */
@HTTPService(name=Definitions.SERVICE_CA)
public class ContentAnalysisService{
	private static final EnumSet<MediaType> MEDIA_TYPES = EnumSet.of(MediaType.PHOTO);
	private XMLFormatter _formatter = new XMLFormatter();

	/**
	 * Returns photos that are associated with the given keywords.
	 * 
	 * @see service.tut.pori.contentanalysis.reference.ClientService#searchSimilarByKeyword(AuthenticationParameter, StringParameter, DataGroups, Limits, IntegerParameter, LongParameter)
	 * 
	 * @param authenticatedUser
	 * @param keywords
	 * @param dataGroups
	 * @param limits
	 * @param serviceIds
	 * @param userIdFilters
	 * @return response
	 */
	@HTTPServiceMethod(name = Definitions.METHOD_SEARCH_SIMILAR_BY_KEYWORD, acceptedMethods={core.tut.pori.http.Definitions.METHOD_GET})
	public Response searchSimilarByKeyword(
			@HTTPAuthenticationParameter(required = false) AuthenticationParameter authenticatedUser,
			@HTTPMethodParameter(name = Definitions.PARAMETER_KEYWORDS) StringParameter keywords, 
			@HTTPMethodParameter(name = DataGroups.PARAMETER_DEFAULT_NAME, required = false) DataGroups dataGroups,
			@HTTPMethodParameter(name = Limits.PARAMETER_DEFAULT_NAME, required = false) Limits limits,
			@HTTPMethodParameter(name = Definitions.PARAMETER_SERVICE_ID, required = false) IntegerParameter serviceIds,
			@HTTPMethodParameter(name = service.tut.pori.users.Definitions.PARAMETER_USER_ID, required = false) LongParameter userIdFilters
			) 
	{
		return new Response(CASearchCore.searchByKeyword(authenticatedUser.getUserIdentity(), keywords.getValues(), dataGroups, limits, ServiceType.fromIdArray(serviceIds.getValues()), userIdFilters.getValues()));
	}

	/**
	 * @see service.tut.pori.contentanalysis.reference.ClientService#updatePhotos(AuthenticationParameter, InputStreamParameter)
	 * 
	 * @param authenticatedUser
	 * @param xml
	 */
	@HTTPServiceMethod(name = Definitions.METHOD_UPDATE_PHOTO_METADATA, acceptedMethods={core.tut.pori.http.Definitions.METHOD_POST})
	public void updatePhotos(
			@HTTPAuthenticationParameter AuthenticationParameter authenticatedUser,
			@HTTPMethodParameter(name=InputStreamParameter.PARAMETER_DEFAULT_NAME, bodyParameter=true) InputStreamParameter xml
			)
	{
		CAContentCore.updatePhotos(authenticatedUser.getUserIdentity(), _formatter.toObject(xml.getValue(), PhotoList.class));
	}

	/**
	 * @see service.tut.pori.contentanalysis.reference.ClientService#retrieveMediaObjects(AuthenticationParameter, DataGroups, Limits, IntegerParameter, StringParameter)
	 * 
	 * @param authenticatedUser
	 * @param dataGroups
	 * @param limits 
	 * @param serviceIds
	 * @param mediaObjectIds 
	 * @return response
	 */
	@HTTPServiceMethod(name = Definitions.METHOD_RETRIEVE_MEDIA_OBJECTS, acceptedMethods={core.tut.pori.http.Definitions.METHOD_GET})
	public Response retrieveMediaObjects(
			@HTTPAuthenticationParameter(required = false) AuthenticationParameter authenticatedUser,
			@HTTPMethodParameter(name = DataGroups.PARAMETER_DEFAULT_NAME, required = false) DataGroups dataGroups,
			@HTTPMethodParameter(name = Limits.PARAMETER_DEFAULT_NAME, required = false) Limits limits,
			@HTTPMethodParameter(name = Definitions.PARAMETER_SERVICE_ID, required = false) IntegerParameter serviceIds,
			@HTTPMethodParameter(name = Definitions.PARAMETER_MEDIA_OBJECT_ID, required = false) StringParameter mediaObjectIds
			)
	{
		return new Response(CAContentCore.getMediaObjects(authenticatedUser.getUserIdentity(), dataGroups, limits, MEDIA_TYPES, ServiceType.fromIdArray(serviceIds.getValues()), mediaObjectIds.getValues()));
	}

	/**
	 * @see service.tut.pori.contentanalysis.reference.ClientService#similarityFeedback(AuthenticationParameter, InputStreamParameter)
	 * 
	 * @param authenticatedUser
	 * @param xml 
	 */
	@HTTPServiceMethod(name = Definitions.METHOD_SIMILARITY_FEEDBACK, acceptedMethods={core.tut.pori.http.Definitions.METHOD_POST})
	public void similarityFeedback(
			@HTTPAuthenticationParameter AuthenticationParameter authenticatedUser,
			@HTTPMethodParameter(name = InputStreamParameter.PARAMETER_DEFAULT_NAME, bodyParameter = true) InputStreamParameter xml
			)
	{
		CAContentCore.similarityFeedback(authenticatedUser.getUserIdentity(), _formatter.toObject(xml.getValue(), PhotoFeedbackList.class));
	}

	/**
	 * Returns photos that are similar with the photo associated with the given id.
	 * 
	 * @see service.tut.pori.contentanalysis.reference.ClientService#searchSimilarById(AuthenticationParameter, StringParameter, StringParameter, DataGroups, Limits, IntegerParameter, LongParameter)
	 * 
	 * @param authenticatedUser
	 * @param analysisType 
	 * @param guid
	 * @param dataGroups
	 * @param limits
	 * @param serviceIds
	 * @param userIdFilters
	 * @return response
	 */
	@HTTPServiceMethod(name = Definitions.METHOD_SEARCH_SIMILAR_BY_ID, acceptedMethods={core.tut.pori.http.Definitions.METHOD_GET})
	public Response searchSimilarById(
			@HTTPAuthenticationParameter(required = false) AuthenticationParameter authenticatedUser, 
			@HTTPMethodParameter(name = Definitions.PARAMETER_GUID) StringParameter guid,
			@HTTPMethodParameter(name = Definitions.PARAMETER_ANALYSIS_TYPE, required = false) StringParameter analysisType,
			@HTTPMethodParameter(name = DataGroups.PARAMETER_DEFAULT_NAME, required = false) DataGroups dataGroups,
			@HTTPMethodParameter(name = Limits.PARAMETER_DEFAULT_NAME, required = false) Limits limits,
			@HTTPMethodParameter(name = Definitions.PARAMETER_SERVICE_ID, required = false) IntegerParameter serviceIds,
			@HTTPMethodParameter(name = service.tut.pori.users.Definitions.PARAMETER_USER_ID, required = false) LongParameter userIdFilters
			) 
	{
		return new Response(CASearchCore.searchByGUID(authenticatedUser.getUserIdentity(), AnalysisType.fromAnalysisTypeString(analysisType.getValues()), guid.getValue(), dataGroups, limits, ServiceType.fromIdArray(serviceIds.getValues()), userIdFilters.getValues()));
	}

	/**
	 * @see service.tut.pori.contentanalysis.reference.ClientService#searchSimilarByContent(AuthenticationParameter, StringParameter, StringParameter, DataGroups, Limits, IntegerParameter, LongParameter)
	 * 
	 * @param authenticatedUser
	 * @param analysisType 
	 * @param url
	 * @param dataGroups
	 * @param limits
	 * @param serviceIds
	 * @param userIdFilters
	 * @return response
	 */
	@HTTPServiceMethod(name = Definitions.METHOD_SEARCH_SIMILAR_BY_CONTENT, acceptedMethods={core.tut.pori.http.Definitions.METHOD_GET})
	public Response searchSimilarByContent(
			@HTTPAuthenticationParameter(required = false) AuthenticationParameter authenticatedUser,
			@HTTPMethodParameter(name = Definitions.PARAMETER_ANALYSIS_TYPE, required = false) StringParameter analysisType, 
			@HTTPMethodParameter(name = Definitions.PARAMETER_URL) StringParameter url, 
			@HTTPMethodParameter(name = DataGroups.PARAMETER_DEFAULT_NAME, required = false) DataGroups dataGroups, 
			@HTTPMethodParameter(name = Limits.PARAMETER_DEFAULT_NAME, required = false) Limits limits,
			@HTTPMethodParameter(name = Definitions.PARAMETER_SERVICE_ID, required = false) IntegerParameter serviceIds, 
			@HTTPMethodParameter(name = service.tut.pori.users.Definitions.PARAMETER_USER_ID, required = false) LongParameter userIdFilters
			) 
	{  
		return new Response(CASearchCore.searchByContent(authenticatedUser.getUserIdentity(), AnalysisType.fromAnalysisTypeString(analysisType.getValues()), url.getValue(), dataGroups, limits, ServiceType.fromIdArray(serviceIds.getValues()), userIdFilters.getValues()));
	}

	/**
	 * @see service.tut.pori.contentanalysis.reference.ServerService#queryTaskDetails(IntegerParameter, LongParameter, DataGroups, Limits)
	 * 
	 * @param taskId
	 * @param backendId
	 * @param dataGroups
	 * @param limits
	 * @return response
	 */
	@HTTPServiceMethod(name = Definitions.METHOD_QUERY_TASK_DETAILS, acceptedMethods={core.tut.pori.http.Definitions.METHOD_GET})
	public Response queryTaskDetails(
			@HTTPMethodParameter(name = Definitions.PARAMETER_BACKEND_ID) IntegerParameter backendId,
			@HTTPMethodParameter(name = Definitions.PARAMETER_TASK_ID) LongParameter taskId,
			@HTTPMethodParameter(name = DataGroups.PARAMETER_DEFAULT_NAME, required = false) DataGroups dataGroups,
			@HTTPMethodParameter(name = Limits.PARAMETER_DEFAULT_NAME, required = false) Limits limits
			) 
	{
		AbstractTaskDetails details = CAContentCore.queryTaskDetails(backendId.getValue(), taskId.getValue(), dataGroups, limits);
		if(details == null){
			return new Response(Status.BAD_REQUEST, "Invalid "+Definitions.PARAMETER_TASK_ID+" or "+Definitions.PARAMETER_BACKEND_ID+".");
		}else{
			return new Response(details);
		}
	}

	/**
	 * @see service.tut.pori.contentanalysis.reference.ServerService#taskFinished(InputStreamParameter)
	 * 
	 * @param xml
	 */
	@HTTPServiceMethod(name = Definitions.METHOD_TASK_FINISHED, acceptedMethods={core.tut.pori.http.Definitions.METHOD_POST})
	public void taskFinished(@HTTPMethodParameter(name = InputStreamParameter.PARAMETER_DEFAULT_NAME, bodyParameter = true) InputStreamParameter xml) {
		CAContentCore.taskFinished(_formatter.toObject(xml.getValue(), PhotoTaskResponse.class));
	}

	/**
	 * @see service.tut.pori.contentanalysis.reference.ClientService#getPhotos(AuthenticationParameter, StringParameter, DataGroups, Limits, IntegerParameter, LongParameter)
	 * 
	 * @param authenticatedUser
	 * @param guid
	 * @param dataGroups
	 * @param limits
	 * @param serviceIds
	 * @param userIds 
	 * @return response
	 */
	@HTTPServiceMethod(name = Definitions.METHOD_RETRIEVE_PHOTO_METADATA, acceptedMethods={core.tut.pori.http.Definitions.METHOD_GET})
	public Response getPhotos(
			@HTTPAuthenticationParameter(required = false) AuthenticationParameter authenticatedUser,
			@HTTPMethodParameter(name = Definitions.PARAMETER_GUID, required = false) StringParameter guid,
			@HTTPMethodParameter(name = DataGroups.PARAMETER_DEFAULT_NAME, required = false) DataGroups dataGroups,
			@HTTPMethodParameter(name = Limits.PARAMETER_DEFAULT_NAME, required = false) Limits limits,
			@HTTPMethodParameter(name = Definitions.PARAMETER_SERVICE_ID, required = false) IntegerParameter serviceIds,
			@HTTPMethodParameter(name = service.tut.pori.users.Definitions.PARAMETER_USER_ID, required = false) LongParameter userIds
			)
	{
		return new Response(CAContentCore.getPhotos(authenticatedUser.getUserIdentity(), guid.getValues(), dataGroups, limits, ServiceType.fromIdArray(serviceIds.getValues()), userIds.getValues()));
	}

	/**
	 * @see service.tut.pori.contentanalysis.reference.ClientService#similarPhotosByObject(AuthenticationParameter, DataGroups, Limits, IntegerParameter, LongParameter, InputStreamParameter)
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
	public Response similarPhotosByObject(
			@HTTPAuthenticationParameter(required = false) AuthenticationParameter authenticatedUser,
			@HTTPMethodParameter(name = DataGroups.PARAMETER_DEFAULT_NAME, required = false) DataGroups dataGroups,
			@HTTPMethodParameter(name = Limits.PARAMETER_DEFAULT_NAME, required = false) Limits limits,
			@HTTPMethodParameter(name = Definitions.PARAMETER_SERVICE_ID, required = false) IntegerParameter serviceIds,
			@HTTPMethodParameter(name = service.tut.pori.users.Definitions.PARAMETER_USER_ID, required = false) LongParameter userIdFilters,
			@HTTPMethodParameter(name = InputStreamParameter.PARAMETER_DEFAULT_NAME, bodyParameter = true) InputStreamParameter xml
			)
	{
		return new Response(CASearchCore.similarPhotosByObject(authenticatedUser.getUserIdentity(), _formatter.toObject(xml.getValue(), MediaObjectList.class), dataGroups, limits, ServiceType.fromIdArray(serviceIds.getValues()), userIdFilters.getValues()));
	}

	/**
	 * @see service.tut.pori.contentanalysis.reference.ClientService#r(AuthenticationParameter, IntegerParameter, StringParameter)
	 * 
	 * @param authenticatedUser
	 * @param serviceId
	 * @param guid
	 * @return response
	 */
	@HTTPServiceMethod(name = Definitions.METHOD_REDIRECT, acceptedMethods={core.tut.pori.http.Definitions.METHOD_GET})
	public RedirectResponse r(
			@HTTPAuthenticationParameter(required = false) AuthenticationParameter authenticatedUser,
			@HTTPMethodParameter(name = Definitions.PARAMETER_SERVICE_ID) IntegerParameter serviceId,
			@HTTPMethodParameter(name = Definitions.PARAMETER_GUID) StringParameter guid
			)
	{
		return CAContentCore.generateTargetUrl(authenticatedUser.getUserIdentity(), ServiceType.fromServiceId(serviceId.getValue()), guid.getValue());
	}
	
	/**
	 * @see service.tut.pori.contentanalysis.reference.ClientService#deletePhotos(AuthenticationParameter, StringParameter)
	 * 
	 * @param authenticatedUser
	 * @param guid
	 * @return response
	 */
	@HTTPServiceMethod(name = Definitions.METHOD_DELETE_PHOTO_METADATA, acceptedMethods={core.tut.pori.http.Definitions.METHOD_DELETE})
	public Response deletePhotos(
			@HTTPAuthenticationParameter AuthenticationParameter authenticatedUser,
			@HTTPMethodParameter(name = Definitions.PARAMETER_GUID) StringParameter guid
			)
	{
		return (CAContentCore.deletePhotos(authenticatedUser.getUserIdentity(), guid.getValues()) ? new Response() : new Response(Status.FORBIDDEN));
	}
}
