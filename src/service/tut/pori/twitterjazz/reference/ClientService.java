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
package service.tut.pori.twitterjazz.reference;

import service.tut.pori.twitterjazz.Definitions;
import core.tut.pori.http.Response;
import core.tut.pori.http.annotations.HTTPAuthenticationParameter;
import core.tut.pori.http.annotations.HTTPMethodParameter;
import core.tut.pori.http.annotations.HTTPService;
import core.tut.pori.http.annotations.HTTPServiceMethod;
import core.tut.pori.http.parameters.AuthenticationParameter;
import core.tut.pori.http.parameters.BooleanParameter;
import core.tut.pori.http.parameters.DataGroups;
import core.tut.pori.http.parameters.Limits;
import core.tut.pori.http.parameters.SortOptions;
import core.tut.pori.http.parameters.StringParameter;

/**
 * Reference implementation for client API methods. This class defines the APIs available for clients.
 * 
 * <h1>Implementation Service path {@value service.tut.pori.twitterjazz.Definitions#SERVICE_TJ}</h1>
 * 
 * @see service.tut.pori.twitterjazz.TwitterJazzService
 *
 */
@HTTPService(name = service.tut.pori.twitterjazz.reference.Definitions.SERVICE_TJ_REFERENCE_CLIENT)
public class ClientService {
	/**
	 * Clients can use this method for retrieving a list of tags extracted by the profile summarization. This will always return the tags for the currently authenticated user.
	 * 
	 * <h2>Example Query:</h2>
	 *
	 * GET /rest/{@value service.tut.pori.twitterjazz.reference.Definitions#SERVICE_TJ_REFERENCE_CLIENT}/{@value service.tut.pori.twitterjazz.Definitions#METHOD_RETRIEVE_TAGS_FOR_USER}<br>
	 *
	 * <h2>Example Result:</h2>
	 * 
	 * {@doc.restlet service="[service.tut.pori.twitterjazz.reference.Definitions#SERVICE_TJ_REFERENCE_CLIENT]" method="[service.tut.pori.twitterjazz.Definitions#METHOD_RETRIEVE_TAGS_FOR_USER]" type="GET" query="" body_uri=""}
	 * 
	 * @param authenticatedUser Note: this method requires authentication, but for the reference implementation, anonymous access is granted.
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
		return new Response(TJReferenceCore.retrieveTagsForUser(authenticatedUser.getUserIdentity(), dataGroups, limits, sortOptions));
	}
	
	/**
	 * Clients can use this method for ranking (rating) tags.
	 * 
	 * <h2>Example Query:</h2>
	 *
	 * GET /rest/{@value service.tut.pori.twitterjazz.reference.Definitions#SERVICE_TJ_REFERENCE_CLIENT}/{@value service.tut.pori.twitterjazz.Definitions#METHOD_SET_RANK}?{@value service.tut.pori.twitterjazz.Definitions#PARAMETER_RANK}=1;100,2;-10<br>
	 *
	 * <h2>Example Result:</h2>
	 * 
	 * {@doc.restlet service="[service.tut.pori.twitterjazz.reference.Definitions#SERVICE_TJ_REFERENCE_CLIENT]" method="[service.tut.pori.twitterjazz.Definitions#METHOD_SET_RANK]" type="POST" query="[service.tut.pori.twitterjazz.Definitions#PARAMETER_RANK]=1;100,2;-10" body_uri=""}
	 * 
	 * @param authenticatedUser Note: this method requires authentication and the user must have authorized the use of his/her Twitter account, but for the reference implementation, anonymous access is granted.
	 * @param ranks with format {@value service.tut.pori.facebookjazz.Definitions#PARAMETER_RANK}={@value service.tut.pori.contentanalysis.Definitions#ELEMENT_MEDIA_OBJECT_ID}{@value core.tut.pori.http.Definitions#SEPARATOR_URI_QUERY_TYPE_VALUE}VALUE{@value core.tut.pori.http.Definitions#SEPARATOR_URI_QUERY_PARAM_VALUES}{@value service.tut.pori.contentanalysis.Definitions#ELEMENT_MEDIA_OBJECT_ID}{@value core.tut.pori.http.Definitions#SEPARATOR_URI_QUERY_TYPE_VALUE}VALUE{@value core.tut.pori.http.Definitions#SEPARATOR_URI_QUERY_PARAM_VALUES} ...
	 */
	@HTTPServiceMethod(name = Definitions.METHOD_SET_RANK, acceptedMethods={core.tut.pori.http.Definitions.METHOD_POST})
	public void setRank(
			@HTTPAuthenticationParameter(required=false) AuthenticationParameter authenticatedUser,
			@HTTPMethodParameter(name = Definitions.PARAMETER_RANK) StringParameter ranks)
	{
		TJReferenceCore.setRank(authenticatedUser.getUserIdentity(), ranks.getValues());
	}
	
	/**
	 * Clients can use this method to initialize account summarization, and optionally perform synchronization for photo content.
	 * 
	 * <h2>Example Query:</h2>
	 *
	 * GET /rest/{@value service.tut.pori.twitterjazz.reference.Definitions#SERVICE_TJ_REFERENCE_CLIENT}/{@value service.tut.pori.twitterjazz.Definitions#METHOD_SUMMARIZE}<br>
	 *
	 * <h2>Example Result:</h2>
	 * 
	 * {@doc.restlet service="[service.tut.pori.twitterjazz.reference.Definitions#SERVICE_TJ_REFERENCE_CLIENT]" method="[service.tut.pori.twitterjazz.Definitions#METHOD_SUMMARIZE]" type="GET" query="" body_uri=""}
	 * 
	 * @param authenticatedUser Note: this method requires authentication, but for the reference implementation, anonymous access is granted.
	 * @param contentTypes Any combination of content types. By default, all content types will be used. See {@link service.tut.pori.twitterjazz.TwitterExtractor.ContentType}
	 * @param screenNames The screen names of the accounts to be summarized. The authenticated user must have permissions granted by Twitter to summarize the accounts for the operation to complete successfully. If no screen names are given, the summarization is performed for the user's own Twitter account.
	 * @param synchronize On <i>true</i> synchronizes the photo content of the user's account. Photo analysis tasks will be created and fully executed for new content using default analysis back-ends prior to starting the summarization task.
	 */
	@HTTPServiceMethod(name = Definitions.METHOD_SUMMARIZE)
	public void summarize(
			@HTTPAuthenticationParameter(required=false) AuthenticationParameter authenticatedUser,
			@HTTPMethodParameter(name = Definitions.PARAMETER_CONTENT_TYPES, required = false) StringParameter contentTypes,
			@HTTPMethodParameter(name = Definitions.PARAMETER_SCREEN_NAMES, required = false) StringParameter screenNames,
			@HTTPMethodParameter(name = Definitions.PARAMETER_SYNCHRONIZE, required = false) BooleanParameter synchronize)
	{
		TJReferenceCore.summarize(authenticatedUser.getUserIdentity(), contentTypes.getValues(), screenNames.getValues(), synchronize.isTrue());
	}
}
