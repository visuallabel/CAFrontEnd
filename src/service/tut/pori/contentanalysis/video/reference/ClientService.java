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
package service.tut.pori.contentanalysis.video.reference;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import service.tut.pori.contentanalysis.PhotoParameters.AnalysisType;
import service.tut.pori.contentanalysis.CAContentCore.ServiceType;
import service.tut.pori.contentanalysis.MediaObjectList;
import service.tut.pori.contentanalysis.video.Definitions;
import core.tut.pori.http.RedirectResponse;
import core.tut.pori.http.Response;
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
 * Reference implementation for client API methods.
 * 
 * <h1>Implementation Service path {@value service.tut.pori.contentanalysis.video.Definitions#SERVICE_VCA}</h1>
 * 
 * @see service.tut.pori.contentanalysis.video.VideoAnalysisService
 *
 */
@HTTPService(name = service.tut.pori.contentanalysis.video.reference.Definitions.SERVICE_VCA_REFERENCE_CLIENT)
public class ClientService {
	private static final Logger LOGGER = Logger.getLogger(ClientService.class);
	private XMLFormatter _formatter = new XMLFormatter();
	
	/**
	 * Redirects the client to the actual video location (i.e. redirects calls targeted to static front-end URLs to dynamic service specific URLs).
	 * The redirection method takes GUID and service id as a parameter. The redirection URLs are automatically generated for analysis tasks and user's search results by the front-end.
	 * 
	 * <h2>Example Query:</h2>
	 *
	 * GET /rest/{@value service.tut.pori.contentanalysis.video.reference.Definitions#SERVICE_VCA_REFERENCE_CLIENT}/{@value service.tut.pori.contentanalysis.Definitions#METHOD_REDIRECT}?{@value service.tut.pori.contentanalysis.Definitions#PARAMETER_GUID}=1&amp;{@value service.tut.pori.contentanalysis.Definitions#PARAMETER_SERVICE_ID}=1<br>
	 *
	 * <h2>Example Result:</h2>
	 * 
	 * REDIRECT / VIDEO CONTENT
	 * 
	 * @param authenticatedUser
	 * @param serviceId One of the supported service types, see {@link service.tut.pori.contentanalysis.CAContentCore.ServiceType}.
	 * @param guid video GUID
	 * @return redirection
	 */
	@HTTPServiceMethod(name = service.tut.pori.contentanalysis.Definitions.METHOD_REDIRECT, acceptedMethods={core.tut.pori.http.Definitions.METHOD_GET})
	public RedirectResponse r(
			@HTTPAuthenticationParameter(required = false) AuthenticationParameter authenticatedUser,
			@HTTPMethodParameter(name = service.tut.pori.contentanalysis.Definitions.PARAMETER_SERVICE_ID) IntegerParameter serviceId,
			@HTTPMethodParameter(name = service.tut.pori.contentanalysis.Definitions.PARAMETER_GUID) StringParameter guid
			)
	{
		return VideoReferenceCore.generateTargetUrl(authenticatedUser.getUserIdentity(), ServiceType.fromServiceId(serviceId.getValue()), guid.getValue());
	}
	
	/**
	 * Search similar videos by giving a list of reference objects. All elements applicable to media objects are accepted as restrictive search terms. Note that missing elements (and element values) are assumed not to be included in the search, except "status" field, which is automatically assumed to be "USER_CONFIRMED" when no value is specified. The Media Object list is to be sent in the body of POST method. The Content-Type header MUST be set to "text/xml". The character set MUST be UTF-8. For example, "Content-Type: text/xml; charset=UTF-8".
	 * 
	 * <h2>Example Query:</h2>
	 * 
	 * POST /rest/{@value service.tut.pori.contentanalysis.video.reference.Definitions#SERVICE_VCA_REFERENCE_CLIENT}/{@value service.tut.pori.contentanalysis.video.Definitions#METHOD_SEARCH_SIMILAR_BY_OBJECT}<br>
	 * Content-Type: text/xml; charset=UTF-8<br><br>
	 *
	 * <b>[HTTP BODY STARTS]</b><br>
	 * 
	 * {@doc.restlet service="[service.tut.pori.contentanalysis.reference.Definitions#SERVICE_CA_REFERENCE_EXAMPLE]" method="[service.tut.pori.contentanalysis.Definitions#ELEMENT_MEDIA_OBJECTLIST]" type="GET" query="" body_uri=""} <br>
	 * 
	 * <b>[HTTP BODY ENDS]</b><br>
	 *
	 * <h2>Example Result:</h2>
	 * 
	 * {@doc.restlet service="[service.tut.pori.contentanalysis.video.reference.Definitions#SERVICE_VCA_REFERENCE_CLIENT]" method="[service.tut.pori.contentanalysis.video.Definitions#METHOD_SEARCH_SIMILAR_BY_OBJECT]" type="POST" query="" body_uri="[service.tut.pori.contentanalysis.reference.Definitions#SERVICE_CA_REFERENCE_EXAMPLE]/[service.tut.pori.contentanalysis.Definitions#ELEMENT_MEDIA_OBJECTLIST]"}
	 * 
	 * @param authenticatedUser
	 * @param dataGroups For supported data groups, see {@link service.tut.pori.contentanalysis.reference.ClientService#retrieveMediaObjects(AuthenticationParameter, DataGroups, Limits, IntegerParameter, StringParameter)}.
	 * @param limits paging limits
	 * @param serviceIds serviceIds If given, search is targeted only to the services with the listed ids. For supported service types, see {@link service.tut.pori.contentanalysis.CAContentCore.ServiceType}.
	 * @param userIdFilters If given, the search will return videos owned by the given user (provided that the currently logged in user has the required permissions).
	 * @param xml See {@link service.tut.pori.contentanalysis.MediaObjectList}
	 * @return See {@link service.tut.pori.contentanalysis.video.VideoList}
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
		try {
			String body = IOUtils.toString(xml.getValue()); // read the body
			LOGGER.debug(body); // print to debug
			try(InputStream input = IOUtils.toInputStream(body)){ // convert back to stream for unmarshal
				return VideoReferenceCore.similarVideosByObject(authenticatedUser.getUserIdentity(), _formatter.toObject(input, MediaObjectList.class), dataGroups, limits, ServiceType.fromIdArray(serviceIds.getValues()), userIdFilters.getValues());
			}
		} catch (IOException ex) {
			LOGGER.error(ex, ex);
			return null;
		}
	}
	
	/**
	 * This method can be used to retrieve the metadata of one or multiple videos. Any combination of parameters may be given for retrieving only the desired content (filtering the results). If you only want a list of media objects, this can be done with the method {@link service.tut.pori.contentanalysis.reference.ClientService#retrieveMediaObjects(AuthenticationParameter, DataGroups, Limits, IntegerParameter, StringParameter)}. If the user is not authenticated, access is only permitted to publicly available content.
	 *
	 * <h2>Example Query:</h2>
	 *
	 * GET /rest/{@value service.tut.pori.contentanalysis.video.reference.Definitions#SERVICE_VCA_REFERENCE_CLIENT}/{@value service.tut.pori.contentanalysis.video.Definitions#METHOD_RETRIEVE_VIDEO_METADATA}<br>
	 *
	 * <h2>Example Result:</h2>
	 * 
	 * {@doc.restlet service="[service.tut.pori.contentanalysis.video.reference.Definitions#SERVICE_VCA_REFERENCE_CLIENT]" method="[service.tut.pori.contentanalysis.video.Definitions#METHOD_RETRIEVE_VIDEO_METADATA]" type="GET" query="" body_uri=""}
	 * 
	 * @param authenticatedUser
	 * @param guid One or more GUIDs for retrieval.
	 * @param dataGroups For supported data groups, see {@link service.tut.pori.contentanalysis.reference.ClientService#retrieveMediaObjects(AuthenticationParameter, DataGroups, Limits, IntegerParameter, StringParameter)}.
	 * @param limits paging limits
	 * @param serviceIds serviceIds If given, search is targeted only to the services with the listed ids. For supported service types, see {@link service.tut.pori.contentanalysis.CAContentCore.ServiceType}. 
	 * @param userIds If given, the search will return videos owned by the given user (provided that the currently logged in user has the required permissions).
	 * @return See {@link service.tut.pori.contentanalysis.video.VideoList}
	 */
	@HTTPServiceMethod(name = Definitions.METHOD_RETRIEVE_VIDEO_METADATA, acceptedMethods={core.tut.pori.http.Definitions.METHOD_GET})
	public Response getVideos(
			@HTTPAuthenticationParameter(required = false) AuthenticationParameter authenticatedUser,
			@HTTPMethodParameter(name = service.tut.pori.contentanalysis.Definitions.PARAMETER_GUID, required=false) StringParameter guid,
			@HTTPMethodParameter(name = DataGroups.PARAMETER_DEFAULT_NAME, required = false) DataGroups dataGroups,
			@HTTPMethodParameter(name = Limits.PARAMETER_DEFAULT_NAME, required = false) Limits limits,
			@HTTPMethodParameter(name = service.tut.pori.contentanalysis.Definitions.PARAMETER_SERVICE_ID, required = false) IntegerParameter serviceIds,
			@HTTPMethodParameter(name = service.tut.pori.users.Definitions.PARAMETER_USER_ID, required = false) LongParameter userIds
			)
	{
		return VideoReferenceCore.getVideos(authenticatedUser.getUserIdentity(), guid.getValues(), dataGroups, limits, ServiceType.fromIdArray(serviceIds.getValues()), userIds.getValues());
	}
	
	/**
	 * Search for videos which are similar to the video designated by the given GUID parameter. Non-authenticated users only have access to publicly available content.
	 * 
	 * <h2>Example Query:</h2>
	 *
	 * GET /rest/{@value service.tut.pori.contentanalysis.video.reference.Definitions#SERVICE_VCA_REFERENCE_CLIENT}/{@value service.tut.pori.contentanalysis.video.Definitions#METHOD_SEARCH_SIMILAR_BY_ID}?{@value service.tut.pori.contentanalysis.Definitions#PARAMETER_GUID}=1<br>
	 *
	 * <h2>Example Result:</h2>
	 * 
	 * {@doc.restlet service="[service.tut.pori.contentanalysis.video.reference.Definitions#SERVICE_VCA_REFERENCE_CLIENT]" method="[service.tut.pori.contentanalysis.video.Definitions#METHOD_SEARCH_SIMILAR_BY_ID]" type="GET" query="[service.tut.pori.contentanalysis.Definitions#PARAMETER_GUID]=1" body_uri=""}
	 * 
	 * @param authenticatedUser
	 * @param guid photo GUID
	 * @param analysisType optional list of analysis types to use for the search operation {@link service.tut.pori.contentanalysis.PhotoParameters.AnalysisType}
	 * @param dataGroups For supported data groups, see {@link service.tut.pori.contentanalysis.reference.ClientService#retrieveMediaObjects(AuthenticationParameter, DataGroups, Limits, IntegerParameter, StringParameter)}
	 * @param limits paging limits
	 * @param serviceIds If given, search is targeted only to the services with the listed ids. For supported service types, see {@link service.tut.pori.contentanalysis.CAContentCore.ServiceType}.
	 * @param userIdFilters If given, the search will return photos owned by the given user (provided that the currently logged in user has the required permissions).
	 * @return See {@link service.tut.pori.contentanalysis.video.VideoList}
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
		return VideoReferenceCore.searchSimilarById(authenticatedUser.getUserIdentity(), AnalysisType.fromAnalysisTypeString(analysisType.getValues()), guid.getValue(), dataGroups, limits, ServiceType.fromIdArray(serviceIds.getValues()), userIdFilters.getValues());
	}
}
