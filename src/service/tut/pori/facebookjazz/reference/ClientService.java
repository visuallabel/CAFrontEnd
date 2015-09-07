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
package service.tut.pori.facebookjazz.reference;

import service.tut.pori.facebookjazz.Definitions;
import service.tut.pori.facebookjazz.WeightModifierList;
import core.tut.pori.http.Response;
import core.tut.pori.http.annotations.HTTPAuthenticationParameter;
import core.tut.pori.http.annotations.HTTPMethodParameter;
import core.tut.pori.http.annotations.HTTPService;
import core.tut.pori.http.annotations.HTTPServiceMethod;
import core.tut.pori.http.parameters.AuthenticationParameter;
import core.tut.pori.http.parameters.BooleanParameter;
import core.tut.pori.http.parameters.DataGroups;
import core.tut.pori.http.parameters.InputStreamParameter;
import core.tut.pori.http.parameters.Limits;
import core.tut.pori.http.parameters.LongParameter;
import core.tut.pori.http.parameters.SortOptions;
import core.tut.pori.http.parameters.StringParameter;
import core.tut.pori.utils.XMLFormatter;

/**
 * Reference implementation for client API methods.
 * 
 * <h1>Implementation Service path {@value service.tut.pori.facebookjazz.Definitions#SERVICE_FBJ}</h1>
 * 
 * @see service.tut.pori.facebookjazz.FacebookJazzService
 *
 */
@HTTPService(name = service.tut.pori.facebookjazz.reference.Definitions.SERVICE_FBJ_REFERENCE_CLIENT)
public class ClientService {
	private XMLFormatter _formatter = new XMLFormatter();
	/**
	 * Clients can use this method for retrieving a list of tags extracted by the profile summarization. This will always return the tags for the currently authenticated user.
	 * 
	 * <h2>Example Query:</h2>
	 *
	 * GET /rest/{@value service.tut.pori.facebookjazz.reference.Definitions#SERVICE_FBJ_REFERENCE_CLIENT}/{@value service.tut.pori.facebookjazz.Definitions#METHOD_RETRIEVE_TAGS_FOR_USER}<br>
	 *
	 * <h2>Example Result:</h2>
	 * 
	 * {@doc.restlet service="[service.tut.pori.facebookjazz.reference.Definitions#SERVICE_FBJ_REFERENCE_CLIENT]" method="[service.tut.pori.facebookjazz.Definitions#METHOD_RETRIEVE_TAGS_FOR_USER]" type="GET" query="" body_uri=""}
	 * 
	 * @param authenticatedUser This method requires user authentication. The user must have authorized the use of his/her Facebook account. Note: this method requires authentication, but for the reference implementation, anonymous access is granted.
	 * @param sortOptions supported element names are {@value service.tut.pori.contentanalysis.Definitions#ELEMENT_CONFIDENCE}, {@value service.tut.pori.contentanalysis.Definitions#ELEMENT_RANK} and {@value service.tut.pori.contentanalysis.Definitions#ELEMENT_VALUE}. Default sorting order is the original addition order of the objects.
	 * @param dataGroups For supported data groups see {@link service.tut.pori.contentanalysis.reference.ClientService#retrieveMediaObjects(AuthenticationParameter, DataGroups, Limits, core.tut.pori.http.parameters.IntegerParameter, StringParameter)}.
	 * @param limits paging limits
	 * @return See {@link service.tut.pori.contentanalysis.MediaObjectList}
	 */
	@HTTPServiceMethod(name = Definitions.METHOD_RETRIEVE_TAGS_FOR_USER, acceptedMethods={core.tut.pori.http.Definitions.METHOD_GET})
	public Response retrieveTagsForUser(
			@HTTPAuthenticationParameter(required=false) AuthenticationParameter authenticatedUser,
			@HTTPMethodParameter(name = DataGroups.PARAMETER_DEFAULT_NAME, required = false) DataGroups dataGroups,
			@HTTPMethodParameter(name = Limits.PARAMETER_DEFAULT_NAME, required = false) Limits limits,
			@HTTPMethodParameter(name = service.tut.pori.contentanalysis.Definitions.PARAMETER_SORT, required = false) SortOptions sortOptions
			)
	{
		return new Response(FBJReferenceCore.retrieveTagsForUser(authenticatedUser.getUserIdentity(), dataGroups, limits, sortOptions));
	}
	
	/**
	 * Clients can use this method for ranking (rating) tags.
	 * 
	 * <h2>Example Query:</h2>
	 *
	 * GET /rest/{@value service.tut.pori.facebookjazz.reference.Definitions#SERVICE_FBJ_REFERENCE_CLIENT}/{@value service.tut.pori.facebookjazz.Definitions#METHOD_SET_RANK}?{@value service.tut.pori.facebookjazz.Definitions#PARAMETER_RANK}=1;100,2;-10<br>
	 *
	 * <h2>Example Result:</h2>
	 * 
	 * {@doc.restlet service="[service.tut.pori.facebookjazz.reference.Definitions#SERVICE_FBJ_REFERENCE_CLIENT]" method="[service.tut.pori.facebookjazz.Definitions#METHOD_SET_RANK]" type="POST" query="[service.tut.pori.facebookjazz.Definitions#PARAMETER_RANK]=1;100,2;-10" body_uri=""}
	 * 
	 * @param authenticatedUser This method requires user authentication. The user must have authorized the use of his/her Facebook account. Note: this method requires authentication, but for the reference implementation, anonymous access is granted.
	 * @param ranks with format {@value service.tut.pori.facebookjazz.Definitions#PARAMETER_RANK}={@value service.tut.pori.contentanalysis.Definitions#ELEMENT_MEDIA_OBJECT_ID}{@value core.tut.pori.http.Definitions#SEPARATOR_URI_QUERY_TYPE_VALUE}VALUE{@value core.tut.pori.http.Definitions#SEPARATOR_URI_QUERY_PARAM_VALUES}{@value service.tut.pori.contentanalysis.Definitions#ELEMENT_MEDIA_OBJECT_ID}{@value core.tut.pori.http.Definitions#SEPARATOR_URI_QUERY_TYPE_VALUE}VALUE{@value core.tut.pori.http.Definitions#SEPARATOR_URI_QUERY_PARAM_VALUES} ...
	 */
	@HTTPServiceMethod(name = Definitions.METHOD_SET_RANK, acceptedMethods={core.tut.pori.http.Definitions.METHOD_POST})
	public void setRank(
			@HTTPAuthenticationParameter(required=false) AuthenticationParameter authenticatedUser,
			@HTTPMethodParameter(name = Definitions.PARAMETER_RANK) StringParameter ranks)
	{
		FBJReferenceCore.setRank(authenticatedUser.getUserIdentity(), ranks.getValues());
	}
	
	/**
	 * Clients can use this method for retrieving tag weight modifiers.
	 * 
	 * <h2>Example Query:</h2>
	 *
	 * GET /rest/{@value service.tut.pori.facebookjazz.reference.Definitions#SERVICE_FBJ_REFERENCE_CLIENT}/{@value service.tut.pori.facebookjazz.Definitions#METHOD_RETRIEVE_TAG_WEIGHTS}<br>
	 *
	 * <h2>Example Result:</h2>
	 * 
	 * {@doc.restlet service="[service.tut.pori.facebookjazz.reference.Definitions#SERVICE_FBJ_REFERENCE_CLIENT]" method="[service.tut.pori.facebookjazz.Definitions#METHOD_RETRIEVE_TAG_WEIGHTS]" type="GET" query="" body_uri=""}
	 *  
	 * @param authenticatedUser This method requires user authentication. The user must have authorized the use of his/her Facebook account. Note: this method requires authentication, but for the reference implementation, anonymous access is granted.
	 * @param userIdFilter User id filter, if not given the default tag weights will be returned.
	 * @return See {@link service.tut.pori.facebookjazz.WeightModifierList}
	 */
	@HTTPServiceMethod(name = Definitions.METHOD_RETRIEVE_TAG_WEIGHTS, acceptedMethods={core.tut.pori.http.Definitions.METHOD_GET})
	public Response retrieveTagWeights(
			@HTTPAuthenticationParameter(required = false) AuthenticationParameter authenticatedUser,
			@HTTPMethodParameter(name = service.tut.pori.users.Definitions.PARAMETER_USER_ID, required = false) LongParameter userIdFilter
			)
	{
		return new Response(FBJReferenceCore.retrieveTagWeights(authenticatedUser.getUserIdentity(), userIdFilter.getValue()));
	}
	
	/**
	 * Clients can use this method to set tag weights.
	 * 
	 * POST /rest/{@value service.tut.pori.facebookjazz.reference.Definitions#SERVICE_FBJ_REFERENCE_CLIENT}/{@value service.tut.pori.facebookjazz.Definitions#METHOD_SET_TAG_WEIGHTS}<br>
	 * Content-Type: text/xml; charset=UTF-8<br><br>
	 *
	 * <b>[HTTP BODY STARTS]</b><br>
	 * 
	 * {@doc.restlet service="[service.tut.pori.facebookjazz.reference.Definitions#SERVICE_FBJ_REFERENCE_EXAMPLE]" method="[service.tut.pori.facebookjazz.Definitions#ELEMENT_WEIGHT_MODIFIER_LIST]" type="GET" query="" body_uri=""} <br>
	 * 
	 * <b>[HTTP BODY ENDS]</b><br>
	 *
	 * <h2>Example Result:</h2>
	 * 
	 * {@doc.restlet service="[service.tut.pori.facebookjazz.reference.Definitions#SERVICE_FBJ_REFERENCE_CLIENT]" method="[service.tut.pori.facebookjazz.Definitions#METHOD_SET_TAG_WEIGHTS]" type="POST" query="" body_uri="[service.tut.pori.facebookjazz.reference.Definitions#SERVICE_FBJ_REFERENCE_EXAMPLE]/[service.tut.pori.facebookjazz.Definitions#ELEMENT_WEIGHT_MODIFIER_LIST]"}
	 * 
	 * @param authenticatedUser This method requires user authentication. The user must have authorized the use of his/her Facebook account. Note: this method requires authentication, but for the reference implementation, anonymous access is granted.
	 * @param xml The tag weight modifiers for the user. See {@link service.tut.pori.facebookjazz.WeightModifierList}
	 */
	@HTTPServiceMethod(name = Definitions.METHOD_SET_TAG_WEIGHTS, acceptedMethods={core.tut.pori.http.Definitions.METHOD_POST})
	public void setTagWeights(
			@HTTPAuthenticationParameter(required = false) AuthenticationParameter authenticatedUser,
			@HTTPMethodParameter(name = InputStreamParameter.PARAMETER_DEFAULT_NAME, bodyParameter = true) InputStreamParameter xml
			)
	{
		FBJReferenceCore.setTagWeights(authenticatedUser.getUserIdentity(), _formatter.toObject(xml.getValue(), WeightModifierList.class));
	}
	
	/**
	 * Clients can use this method to initialize account summarization, and optionally perform synchronization for photo content.
	 * 
	 * <h2>Example Query:</h2>
	 *
	 * GET /rest/{@value service.tut.pori.facebookjazz.reference.Definitions#SERVICE_FBJ_REFERENCE_CLIENT}/{@value service.tut.pori.facebookjazz.Definitions#METHOD_SUMMARIZE}<br>
	 *
	 * <h2>Example Result:</h2>
	 * 
	 * {@doc.restlet service="[service.tut.pori.facebookjazz.reference.Definitions#SERVICE_FBJ_REFERENCE_CLIENT]" method="[service.tut.pori.facebookjazz.Definitions#METHOD_SUMMARIZE]" type="GET" query="" body_uri=""}
	 * 
	 * @param authenticatedUser This method requires user authentication. The user must have authorized the use of his/her Facebook account. Note: this method requires authentication, but for the reference implementation, anonymous access is granted.
	 * @param contentTypes Any combination of content types. Valid types are: events, generated_tags, groups, likes, photo_descriptions, status_messages, video_descriptions. See {@link service.tut.pori.facebookjazz.FacebookExtractor.ContentType}
	 * @param synchronize On <i>true</i> synchronizes the photo content of the user's account prior to starting the summarization process. Photo analysis tasks will be created for new content using default analysis back-ends.
	 */
	@HTTPServiceMethod(name = Definitions.METHOD_SUMMARIZE)
	public void summarize(
			@HTTPAuthenticationParameter(required=false) AuthenticationParameter authenticatedUser,
			@HTTPMethodParameter(name = Definitions.PARAMETER_CONTENT_TYPES, required = false) StringParameter contentTypes,
			@HTTPMethodParameter(name = Definitions.PARAMETER_SYNCHRONIZE, required = false) BooleanParameter synchronize)
	{
		FBJReferenceCore.summarize(authenticatedUser.getUserIdentity(), contentTypes.getValues(), synchronize.isTrue());
	}
}
