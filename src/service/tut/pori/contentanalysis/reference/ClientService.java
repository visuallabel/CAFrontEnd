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
package service.tut.pori.contentanalysis.reference;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import service.tut.pori.contentanalysis.PhotoParameters.AnalysisType;
import service.tut.pori.contentanalysis.CAContentCore.ServiceType;
import service.tut.pori.contentanalysis.Definitions;
import service.tut.pori.contentanalysis.PhotoFeedbackList;
import service.tut.pori.contentanalysis.PhotoList;
import service.tut.pori.contentanalysis.MediaObjectList;
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
 * <h1>Implementation Service path {@value service.tut.pori.contentanalysis.Definitions#SERVICE_CA}</h1>
 * 
 * @see service.tut.pori.contentanalysis.ContentAnalysisService
 *
 */
@HTTPService(name = service.tut.pori.contentanalysis.reference.Definitions.SERVICE_CA_REFERENCE_CLIENT)
public class ClientService {
	private static final Logger LOGGER = Logger.getLogger(ClientService.class);
	private XMLFormatter _formatter = new XMLFormatter();
	
	/**
	 * Search for photos which contain the given keywords, or keywords closely related or associated with the keywords. Non-authenticated users only have access to publicly available content.
	 * 
	 * <h2>Example Query:</h2>
	 *
	 * GET /rest/{@value service.tut.pori.contentanalysis.reference.Definitions#SERVICE_CA_REFERENCE_CLIENT}/{@value service.tut.pori.contentanalysis.Definitions#METHOD_SEARCH_SIMILAR_BY_KEYWORD}?{@value service.tut.pori.contentanalysis.Definitions#PARAMETER_KEYWORDS}=cute<br>
	 *
	 * <h2>Example Result:</h2>
	 * 
	 * {@doc.restlet service="[service.tut.pori.contentanalysis.reference.Definitions#SERVICE_CA_REFERENCE_CLIENT]" method="[service.tut.pori.contentanalysis.Definitions#METHOD_SEARCH_SIMILAR_BY_KEYWORD]" type="GET" query="[service.tut.pori.contentanalysis.Definitions#PARAMETER_KEYWORDS]=cute" body_uri=""}
	 * 
	 * @param authenticatedUser
	 * @param keywords any number of keywords for the search. 
	 * @param dataGroups For supported data groups, see {@link #retrieveMediaObjects(AuthenticationParameter, DataGroups, Limits, IntegerParameter, StringParameter)}
	 * @param limits paging limits
	 * @param serviceIds If given, search is targeted only to the services with the listed ids. For supported service types, see {@link service.tut.pori.contentanalysis.CAContentCore.ServiceType}.
	 * @param userIdFilters If given, the search will return photos owned by the given user (provided that the currently logged in user has the required permissions).
	 * @return See {@link service.tut.pori.contentanalysis.PhotoList}
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
		return CAReferenceCore.searchByKeyword(authenticatedUser.getUserIdentity(), keywords.getValues(), dataGroups, limits, ServiceType.fromIdArray(serviceIds.getValues()), userIdFilters.getValues());
	}
	
	/**
	 * Search for photos which are similar to the photo designated by the given GUID parameter. Non-authenticated users only have access to publicly available content.
	 * 
	 * <h2>Example Query:</h2>
	 *
	 * GET /rest/{@value service.tut.pori.contentanalysis.reference.Definitions#SERVICE_CA_REFERENCE_CLIENT}/{@value service.tut.pori.contentanalysis.Definitions#METHOD_SEARCH_SIMILAR_BY_ID}?{@value service.tut.pori.contentanalysis.Definitions#PARAMETER_GUID}=1<br>
	 *
	 * <h2>Example Result:</h2>
	 * 
	 * {@doc.restlet service="[service.tut.pori.contentanalysis.reference.Definitions#SERVICE_CA_REFERENCE_CLIENT]" method="[service.tut.pori.contentanalysis.Definitions#METHOD_SEARCH_SIMILAR_BY_ID]" type="GET" query="[service.tut.pori.contentanalysis.Definitions#PARAMETER_GUID]=1" body_uri=""}
	 * 
	 * @param authenticatedUser
	 * @param analysisType optional list of analysis types to use for the search operation {@link service.tut.pori.contentanalysis.PhotoParameters.AnalysisType}
	 * @param guid photo GUID
	 * @param dataGroups For supported data groups, see {@link #retrieveMediaObjects(AuthenticationParameter, DataGroups, Limits, IntegerParameter, StringParameter)}
	 * @param limits paging limits
	 * @param serviceIds If given, search is targeted only to the services with the listed ids. For supported service types, see {@link service.tut.pori.contentanalysis.CAContentCore.ServiceType}.
	 * @param userIdFilters If given, the search will return photos owned by the given user (provided that the currently logged in user has the required permissions).
	 * @return See {@link service.tut.pori.contentanalysis.PhotoList}
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
		return CAReferenceCore.searchSimilarById(authenticatedUser.getUserIdentity(), AnalysisType.fromAnalysisTypeString(analysisType.getValues()), guid.getValue(), dataGroups, limits, ServiceType.fromIdArray(serviceIds.getValues()), userIdFilters.getValues());
	}
	
	/**
	 * Search similar photos by giving a list of reference objects. All elements applicable to media objects are accepted as restrictive search terms. Note that missing elements (and element values) are assumed not to be included in the search, except "status" field, which is automatically assumed to be "USER_CONFIRMED" when no value is specified. The Media Object list is to be sent in the body of POST method. The Content-Type header MUST be set to "text/xml". The character set MUST be UTF-8. For example, "Content-Type: text/xml; charset=UTF-8".
	 * 
	 * <h2>Example Query:</h2>
	 * 
	 * POST /rest/{@value service.tut.pori.contentanalysis.reference.Definitions#SERVICE_CA_REFERENCE_CLIENT}/{@value service.tut.pori.contentanalysis.Definitions#METHOD_SEARCH_SIMILAR_BY_OBJECT}<br>
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
	 * {@doc.restlet service="[service.tut.pori.contentanalysis.reference.Definitions#SERVICE_CA_REFERENCE_CLIENT]" method="[service.tut.pori.contentanalysis.Definitions#METHOD_SEARCH_SIMILAR_BY_OBJECT]" type="POST" query="" body_uri="[service.tut.pori.contentanalysis.reference.Definitions#SERVICE_CA_REFERENCE_EXAMPLE]/[service.tut.pori.contentanalysis.Definitions#ELEMENT_MEDIA_OBJECTLIST]"}
	 * 
	 * @param authenticatedUser
	 * @param dataGroups For supported data groups, see {@link #retrieveMediaObjects(AuthenticationParameter, DataGroups, Limits, IntegerParameter, StringParameter)}
	 * @param limits paging limits
	 * @param serviceIds If given, search is targeted only to the services with the listed ids. For supported service types, see {@link service.tut.pori.contentanalysis.CAContentCore.ServiceType}.
	 * @param userIdFilters If given, the search will return photos owned by the given user (provided that the currently logged in user has the required permissions).
	 * @param xml See {@link service.tut.pori.contentanalysis.MediaObjectList}
	 * @return See {@link service.tut.pori.contentanalysis.PhotoList}
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
		try {
			String body = IOUtils.toString(xml.getValue(), core.tut.pori.http.Definitions.CHARSET_UTF8); // read the body
			LOGGER.debug(body); // print to debug
			try(InputStream input = IOUtils.toInputStream(body, core.tut.pori.http.Definitions.CHARSET_UTF8)){ // convert back to stream for unmarshal
				return CAReferenceCore.similarPhotosByObject(authenticatedUser.getUserIdentity(), _formatter.toObject(input, MediaObjectList.class), dataGroups, limits, ServiceType.fromIdArray(serviceIds.getValues()), userIdFilters.getValues());
			}
		} catch (IOException ex) {
			LOGGER.error(ex, ex);
			return null;
		}
	}
	
	/**
	 * Search for photos which are similar to the photo designated by the given URL parameter. Non-authenticated users only have access to publicly available content.
	 * 
	 * <h2>Example Query:</h2>
	 *
	 * GET /rest/{@value service.tut.pori.contentanalysis.reference.Definitions#SERVICE_CA_REFERENCE_CLIENT}/{@value service.tut.pori.contentanalysis.Definitions#METHOD_SEARCH_SIMILAR_BY_CONTENT}?{@value service.tut.pori.contentanalysis.Definitions#PARAMETER_URL}=http%3A%2F%2Fexample.org%2Fimage.jpg<br>
	 *
	 * <h2>Example Result:</h2>
	 * 
	 * {@doc.restlet service="[service.tut.pori.contentanalysis.reference.Definitions#SERVICE_CA_REFERENCE_CLIENT]" method="[service.tut.pori.contentanalysis.Definitions#METHOD_SEARCH_SIMILAR_BY_CONTENT]" type="GET" query="[service.tut.pori.contentanalysis.Definitions#PARAMETER_URL]=http%3A%2F%2Fexample.org%2Fimage.jpg" body_uri=""}
	 * 
	 * @param authenticatedUser
	 * @param url publicly accessible URL with photo content
	 * @param analysisType optional list of analysis types to use for the search operation {@link service.tut.pori.contentanalysis.PhotoParameters.AnalysisType}
	 * @param dataGroups For supported data groups, see {@link #retrieveMediaObjects(AuthenticationParameter, DataGroups, Limits, IntegerParameter, StringParameter)}
	 * @param limits paging limits
	 * @param serviceIds If given, search is targeted only to the services with the listed ids. For supported service types, see {@link service.tut.pori.contentanalysis.CAContentCore.ServiceType}.
	 * @param userIdFilters If given, the search will return photos owned by the given user (provided that the currently logged in user has the required permissions).
	 * @return See {@link service.tut.pori.contentanalysis.PhotoList}
	 */
	@HTTPServiceMethod(name = Definitions.METHOD_SEARCH_SIMILAR_BY_CONTENT, acceptedMethods={core.tut.pori.http.Definitions.METHOD_GET})
	public Response searchSimilarByContent(
			@HTTPAuthenticationParameter(required = false) AuthenticationParameter authenticatedUser,
			@HTTPMethodParameter(name = Definitions.PARAMETER_URL) StringParameter url, 
			@HTTPMethodParameter(name = Definitions.PARAMETER_ANALYSIS_TYPE, required = false) StringParameter analysisType, 
			@HTTPMethodParameter(name = DataGroups.PARAMETER_DEFAULT_NAME, required = false) DataGroups dataGroups, 
			@HTTPMethodParameter(name = Limits.PARAMETER_DEFAULT_NAME, required = false) Limits limits,
			@HTTPMethodParameter(name = Definitions.PARAMETER_SERVICE_ID, required = false) IntegerParameter serviceIds, 
			@HTTPMethodParameter(name = service.tut.pori.users.Definitions.PARAMETER_USER_ID, required = false) LongParameter userIdFilters
			) 
	{  
		return CAReferenceCore.searchByContent(authenticatedUser.getUserIdentity(), AnalysisType.fromAnalysisTypeString(analysisType.getValues()), url.getValue(), dataGroups, limits, ServiceType.fromIdArray(serviceIds.getValues()), userIdFilters.getValues());
	}
	
	/**
	 * Redirects the client to the actual photo location (i.e. redirects calls targeted to static front-end URLs to dynamic service specific URLs).
	 * The redirection method takes GUID and service id as a parameter. The redirection URLs are automatically generated for analysis tasks and user's search results by the front-end.
	 * 
	 * <h2>Example Query:</h2>
	 *
	 * GET /rest/{@value service.tut.pori.contentanalysis.reference.Definitions#SERVICE_CA_REFERENCE_CLIENT}/{@value service.tut.pori.contentanalysis.Definitions#METHOD_REDIRECT}?{@value service.tut.pori.contentanalysis.Definitions#PARAMETER_GUID}=1&amp;{@value service.tut.pori.contentanalysis.Definitions#PARAMETER_SERVICE_ID}=1
	 *
	 * <h2>Example Result:</h2>
	 * 
	 * REDIRECT / PHOTO CONTENT
	 * 
	 * @param authenticatedUser
	 * @param serviceId One of the supported service types, see {@link service.tut.pori.contentanalysis.CAContentCore.ServiceType}.
	 * @param guid photo GUID
	 * @return redirection
	 */
	@HTTPServiceMethod(name = Definitions.METHOD_REDIRECT, acceptedMethods={core.tut.pori.http.Definitions.METHOD_GET})
	public RedirectResponse r(
			@HTTPAuthenticationParameter(required = false) AuthenticationParameter authenticatedUser,
			@HTTPMethodParameter(name = Definitions.PARAMETER_SERVICE_ID) IntegerParameter serviceId,
			@HTTPMethodParameter(name = Definitions.PARAMETER_GUID) StringParameter guid
			)
	{
		return CAReferenceCore.generateTargetUrl(authenticatedUser.getUserIdentity(), ServiceType.fromServiceId(serviceId.getValue()), guid.getValue());
	}
	
	/**
	 * The clients can use this method to update the details of photos. If media objects are updated, calling this method may trigger the generation of feedback task. 
	 * The request is to be sent in the body of POST method. The Content-Type header MUST be set to "text/xml". The character set MUST be UTF-8. For example, "Content-Type: text/xml; charset=UTF-8".
	 * 
	 * <h2>Example Query:</h2>
	 *
	 * POST /rest/{@value service.tut.pori.contentanalysis.reference.Definitions#SERVICE_CA_REFERENCE_CLIENT}/{@value service.tut.pori.contentanalysis.Definitions#METHOD_UPDATE_PHOTO_METADATA}<br>
	 * Content-Type: text/xml; charset=UTF-8<br><br>
	 *
	 * <b>[HTTP BODY STARTS]</b><br>
	 * 
	 * {@doc.restlet service="[service.tut.pori.contentanalysis.reference.Definitions#SERVICE_CA_REFERENCE_EXAMPLE]" method="[service.tut.pori.contentanalysis.Definitions#ELEMENT_PHOTOLIST]" type="GET" query="" body_uri=""} <br>
	 * 
	 * <b>[HTTP BODY ENDS]</b><br>
	 *
	 * <h2>Example Result:</h2>
	 * 
	 * {@doc.restlet service="[service.tut.pori.contentanalysis.reference.Definitions#SERVICE_CA_REFERENCE_CLIENT]" method="[service.tut.pori.contentanalysis.Definitions#METHOD_UPDATE_PHOTO_METADATA]" type="POST" query="" body_uri="[service.tut.pori.contentanalysis.reference.Definitions#SERVICE_CA_REFERENCE_EXAMPLE]/[service.tut.pori.contentanalysis.Definitions#ELEMENT_PHOTOLIST]"}
	 * 
	 * @param authenticatedUser Note: this method requires authentication, but for the reference implementation, anonymous access is granted.
	 * @param xml Only the result data should be in the body. See {@link service.tut.pori.contentanalysis.PhotoList}
	 */
	@HTTPServiceMethod(name = Definitions.METHOD_UPDATE_PHOTO_METADATA, acceptedMethods={core.tut.pori.http.Definitions.METHOD_POST})
	public void updatePhotos(
			@HTTPAuthenticationParameter(required = false) AuthenticationParameter authenticatedUser,	// in the real implementation this is required
			@HTTPMethodParameter(name=InputStreamParameter.PARAMETER_DEFAULT_NAME, bodyParameter=true) InputStreamParameter xml
			)
	{
		try {
			String body = IOUtils.toString(xml.getValue(), core.tut.pori.http.Definitions.CHARSET_UTF8); // read the body
			LOGGER.debug(body); // print to debug
			try(InputStream input = IOUtils.toInputStream(body, core.tut.pori.http.Definitions.CHARSET_UTF8)){ // convert back to stream for unmarshal
				CAReferenceCore.updatePhotos(authenticatedUser.getUserIdentity(), _formatter.toObject(input, PhotoList.class));
			}
		} catch (IOException ex) {
			LOGGER.error(ex, ex);
		}
	}
	
	/**
	 * This method can be used to retrieve the metadata of one or multiple photos. Any combination of parameters may be given for retrieving only the desired content (filtering the results). If you only want a list of media objects, this can be done with Retrieve media Objects method. If the user is not authenticated, access is only permitted to publicly available content.
	 *
	 * <h2>Example Query:</h2>
	 *
	 * GET /rest/{@value service.tut.pori.contentanalysis.reference.Definitions#SERVICE_CA_REFERENCE_CLIENT}/{@value service.tut.pori.contentanalysis.Definitions#METHOD_RETRIEVE_PHOTO_METADATA}<br>
	 *
	 * <h2>Example Result:</h2>
	 * 
	 * {@doc.restlet service="[service.tut.pori.contentanalysis.reference.Definitions#SERVICE_CA_REFERENCE_CLIENT]" method="[service.tut.pori.contentanalysis.Definitions#METHOD_RETRIEVE_PHOTO_METADATA]" type="GET" query="" body_uri=""}
	 * 
	 * @param authenticatedUser
	 * @param guid One or more photo GUIDs for retrieval.
	 * @param dataGroups dataGroups For supported data groups, see {@link #retrieveMediaObjects(AuthenticationParameter, DataGroups, Limits, IntegerParameter, StringParameter)}
	 * @param limits paing limits
	 * @param serviceIds If given, only photos from the given services will be returned. For supported service types, see {@link service.tut.pori.contentanalysis.CAContentCore.ServiceType}.
	 * @param userIds If given, the search will return photos owned by the given user (provided that the currently logged in user has the required permissions).
	 * @return See {@link service.tut.pori.contentanalysis.PhotoList}
	 */
	@HTTPServiceMethod(name = Definitions.METHOD_RETRIEVE_PHOTO_METADATA, acceptedMethods={core.tut.pori.http.Definitions.METHOD_GET})
	public Response getPhotos(
			@HTTPAuthenticationParameter(required = false) AuthenticationParameter authenticatedUser,
			@HTTPMethodParameter(name = Definitions.PARAMETER_GUID, required=false) StringParameter guid,
			@HTTPMethodParameter(name = DataGroups.PARAMETER_DEFAULT_NAME, required = false) DataGroups dataGroups,
			@HTTPMethodParameter(name = Limits.PARAMETER_DEFAULT_NAME, required = false) Limits limits,
			@HTTPMethodParameter(name = Definitions.PARAMETER_SERVICE_ID, required = false) IntegerParameter serviceIds,
			@HTTPMethodParameter(name = service.tut.pori.users.Definitions.PARAMETER_USER_ID, required = false) LongParameter userIds
			)
	{
		return CAReferenceCore.getPhotos(authenticatedUser.getUserIdentity(), guid.getValues(), dataGroups, limits, ServiceType.fromIdArray(serviceIds.getValues()), userIds.getValues());
	}
	
	/**
	 * 
	 * The clients can use this method to deliver similarity feedback. An example case would be to notify the front-end that the results returned by a search query contain invalid results. Calling this method will trigger the generation of a feedback task.
	 * 
	 * The request must always contain a Photo List element, which contains at least one Photo for a reference, and a Similar Photo List and/or a Dissimilar Photo List element. The photo(s) in Dissimilar and Similar Photo Lists are thus similar or not similar with the passed reference photo, respectively.
	 * 
	 * The request is to be sent in the body of POST method. The Content-Type header MUST be set to "text/xml". The character set MUST be UTF-8. For example, "Content-Type: text/xml; charset=UTF-8".
	 * 
	 * <h2>Example Query:</h2>
	 *
	 * POST /rest/{@value service.tut.pori.contentanalysis.reference.Definitions#SERVICE_CA_REFERENCE_CLIENT}/{@value service.tut.pori.contentanalysis.Definitions#METHOD_SIMILARITY_FEEDBACK}<br>
	 * Content-Type: text/xml; charset=UTF-8<br><br>
	 *
	 * <b>[HTTP BODY STARTS]</b><br>
	 * 
	 * {@doc.restlet service="[service.tut.pori.contentanalysis.reference.Definitions#SERVICE_CA_REFERENCE_EXAMPLE]" method="[service.tut.pori.contentanalysis.Definitions#ELEMENT_FEEDBACKLIST]" type="GET" query="" body_uri=""} <br>
	 * 
	 * <b>[HTTP BODY ENDS]</b><br>
	 *
	 * <h2>Example Result:</h2>
	 * 
	 * {@doc.restlet service="[service.tut.pori.contentanalysis.reference.Definitions#SERVICE_CA_REFERENCE_CLIENT]" method="[service.tut.pori.contentanalysis.Definitions#METHOD_SIMILARITY_FEEDBACK]" type="POST" query="" body_uri="[service.tut.pori.contentanalysis.reference.Definitions#SERVICE_CA_REFERENCE_EXAMPLE]/[service.tut.pori.contentanalysis.Definitions#ELEMENT_FEEDBACKLIST]"}
	 * 
	 * @param authenticatedUser Note: this method requires authentication, but for the reference implementation, anonymous access is granted.
	 * @param xml Only the result data should be in the body. See {@link service.tut.pori.contentanalysis.PhotoFeedbackList}
	 */
	@HTTPServiceMethod(name = Definitions.METHOD_SIMILARITY_FEEDBACK, acceptedMethods={core.tut.pori.http.Definitions.METHOD_POST})
	public void similarityFeedback(
			@HTTPAuthenticationParameter(required = false) AuthenticationParameter authenticatedUser,
			@HTTPMethodParameter(name = InputStreamParameter.PARAMETER_DEFAULT_NAME, bodyParameter = true) InputStreamParameter xml
			)
	{
		try {
			String body = IOUtils.toString(xml.getValue(), core.tut.pori.http.Definitions.CHARSET_UTF8); // read the body
			LOGGER.debug(body); // print to debug
			try(InputStream input = IOUtils.toInputStream(body, core.tut.pori.http.Definitions.CHARSET_UTF8)){ // convert back to stream for unmarshal
				CAReferenceCore.similarityFeedback(authenticatedUser.getUserIdentity(), _formatter.toObject(input, PhotoFeedbackList.class));
			}
		} catch (IOException ex) {
			LOGGER.error(ex, ex);
		}
	}
	
	/**
	 * This method is for retrieving a list of media objects for the currently authenticated user by providing a set of request parameters (filters). If you want to retrieve media objects for a single photo, or for a list of photos, it can be done by using the Retrieve Photo Metadata method.
	 * 
	 * <h2>Example Query:</h2>
	 *
	 * GET /rest/{@value service.tut.pori.contentanalysis.reference.Definitions#SERVICE_CA_REFERENCE_CLIENT}/{@value service.tut.pori.contentanalysis.Definitions#METHOD_RETRIEVE_MEDIA_OBJECTS}<br>
	 *
	 * <h2>Example Result:</h2>
	 * 
	 * {@doc.restlet service="[service.tut.pori.contentanalysis.reference.Definitions#SERVICE_CA_REFERENCE_CLIENT]" method="[service.tut.pori.contentanalysis.Definitions#METHOD_RETRIEVE_MEDIA_OBJECTS]" type="GET" query="" body_uri=""}
	 * 
	 * @param authenticatedUser Note: this method requires authentication, but for the reference implementation, anonymous access is granted.
	 * @param dataGroups In addition to basic data groups the following groups are also supported: {@value service.tut.pori.contentanalysis.Definitions#DATA_GROUP_TIMECODES}, {@value service.tut.pori.contentanalysis.Definitions#DATA_GROUP_FACE}, {@value service.tut.pori.contentanalysis.Definitions#DATA_GROUP_KEYWORDS}, {@value service.tut.pori.contentanalysis.Definitions#DATA_GROUP_METADATA}, {@value service.tut.pori.contentanalysis.Definitions#DATA_GROUP_OBJECT}, {@value service.tut.pori.contentanalysis.Definitions#DATA_GROUP_BACKEND_REMOVED}, {@value service.tut.pori.contentanalysis.Definitions#DATA_GROUP_CANDIDATE}, {@value service.tut.pori.contentanalysis.Definitions#DATA_GROUP_USER_CONFIRMED} and {@value service.tut.pori.contentanalysis.Definitions#DATA_GROUP_USER_REJECTED}.
	 * @param limits paging limits
	 * @param serviceIds Return only objects retrieved from specific service(s). For supported service types, see {@link service.tut.pori.contentanalysis.CAContentCore.ServiceType}.
	 * @param mediaObjectIds Return only objects with the specified ids.
	 * @return See {@link service.tut.pori.contentanalysis.MediaObjectList}
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
		return CAReferenceCore.getMediaObjects(authenticatedUser.getUserIdentity(), dataGroups, limits, ServiceType.fromIdArray(serviceIds.getValues()), mediaObjectIds.getValues());
	}
	
	/**
	 * 
	 * This method is for removing photo metadata. Note that even though all metadata for the given photo(s) will be removed, the removal operation happens only in the scope of the system known ("front-end local") UID and no changes will be propagated to the content storage service hosting the original photo. This also means that synchronizing the external account will re-insert the photo with a different UID and without the original metadata content.
	 * 
	 * After this method call the UID will be invalid and feedback tasks will be generated and submitted to back-ends notifying the deletion of the photo.
	 * 
	 * This method will in general ignore non-existent UIDs, if provided, but will give an error on permission problem.
	 * 
	 * <h2>Example Query:</h2>
	 *
	 * DELETE /rest/{@value service.tut.pori.contentanalysis.reference.Definitions#SERVICE_CA_REFERENCE_CLIENT}/{@value service.tut.pori.contentanalysis.Definitions#METHOD_DELETE_PHOTO_METADATA}?{@value service.tut.pori.contentanalysis.Definitions#PARAMETER_GUID}=0<br>
	 *
	 * <h2>Example Result:</h2>
	 * 
	 * {@doc.restlet service="[service.tut.pori.contentanalysis.reference.Definitions#SERVICE_CA_REFERENCE_CLIENT]" method="[service.tut.pori.contentanalysis.Definitions#METHOD_DELETE_PHOTO_METADATA]" type="DELETE" query="[service.tut.pori.contentanalysis.Definitions#PARAMETER_GUID]=0" body_uri=""}
	 * 
	 * @param authenticatedUser  Note: this method requires authentication, but for the reference implementation, anonymous access is granted.
	 * @param guid One or more photo GUIDs for deletion.
	 * @return response
	 */
	@HTTPServiceMethod(name = Definitions.METHOD_DELETE_PHOTO_METADATA, acceptedMethods={core.tut.pori.http.Definitions.METHOD_DELETE})
	public Response deletePhotos(
			@HTTPAuthenticationParameter(required=false) AuthenticationParameter authenticatedUser,
			@HTTPMethodParameter(name = Definitions.PARAMETER_GUID) StringParameter guid
			)
	{
		CAReferenceCore.deletePhotos(authenticatedUser.getUserIdentity(), guid.getValues());
		return new Response();
	}
}
