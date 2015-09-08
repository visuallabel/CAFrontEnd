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
package service.tut.pori.twitterjazz;

import java.util.EnumSet;

import service.tut.pori.contentanalysis.PhotoTaskResponse;
import service.tut.pori.facebookjazz.FBJContentCore;
import service.tut.pori.twitterjazz.TwitterExtractor.ContentType;
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
import core.tut.pori.http.parameters.SortOptions;
import core.tut.pori.http.parameters.StringParameter;
import core.tut.pori.utils.XMLFormatter;

/**
 * This service enables twitter profile summarization
 * 
 * @see service.tut.pori.twitterjazz.reference.ClientService
 * @see service.tut.pori.twitterjazz.reference.BackendService
 */
@HTTPService(name = Definitions.SERVICE_TJ)
public class TwitterJazzService {
	private XMLFormatter _formatter = new XMLFormatter();

	/**
	 * @see service.tut.pori.twitterjazz.reference.ServerService#taskFinished(InputStreamParameter)
	 * 
	 * @param xml
	 */
	@HTTPServiceMethod(name = service.tut.pori.contentanalysis.Definitions.METHOD_TASK_FINISHED, acceptedMethods={core.tut.pori.http.Definitions.METHOD_POST})
	public void taskFinished(@HTTPMethodParameter(name = InputStreamParameter.PARAMETER_DEFAULT_NAME, bodyParameter = true) InputStreamParameter xml) {
		TJContentCore.taskFinished(_formatter.toObject(xml.getValue(), TwitterTaskResponse.class, PhotoTaskResponse.class));
	}
	
	/**
	 * @see service.tut.pori.twitterjazz.reference.ClientService#retrieveTagsForUser(AuthenticationParameter, DataGroups, Limits, SortOptions)
	 * 
	 * @param authenticatedUser
	 * @param sortOptions
	 * @param dataGroups
	 * @param limits
	 * @return response
	 */
	@HTTPServiceMethod(name = Definitions.METHOD_RETRIEVE_TAGS_FOR_USER, acceptedMethods={core.tut.pori.http.Definitions.METHOD_GET})
	public Response retrieveTagsForUser(
			@HTTPAuthenticationParameter AuthenticationParameter authenticatedUser,
			@HTTPMethodParameter(name = DataGroups.PARAMETER_DEFAULT_NAME, required = false) DataGroups dataGroups,
			@HTTPMethodParameter(name = Limits.PARAMETER_DEFAULT_NAME, required = false) Limits limits,
			@HTTPMethodParameter(name = service.tut.pori.contentanalysis.Definitions.PARAMETER_SORT, required = false) SortOptions sortOptions
			)
	{
		return new Response(TJContentCore.retrieveTagsForUser(authenticatedUser.getUserIdentity(), dataGroups, limits, sortOptions));
	}

	/**
	 * @see service.tut.pori.twitterjazz.reference.ClientService#setRank(AuthenticationParameter, StringParameter)
	 * 
	 * @param authenticatedUser
	 * @param ranks
	 */
	@HTTPServiceMethod(name = Definitions.METHOD_SET_RANK, acceptedMethods={core.tut.pori.http.Definitions.METHOD_POST})
	public void setRank(
			@HTTPAuthenticationParameter AuthenticationParameter authenticatedUser,
			@HTTPMethodParameter(name = Definitions.PARAMETER_RANK) StringParameter ranks)
	{
		TJContentCore.setRanks(authenticatedUser.getUserIdentity(), FBJContentCore.parseRankStrings(ranks.getValues()));
	}
	
	/**
	 * @see service.tut.pori.twitterjazz.reference.ClientService#summarize(AuthenticationParameter, StringParameter, StringParameter, BooleanParameter)
	 * 
	 * @param authenticatedUser
	 * @param contentTypes
	 * @param screenNames
	 * @param synchronize
	 */
	@HTTPServiceMethod(name = Definitions.METHOD_SUMMARIZE)
	public void summarize(
			@HTTPAuthenticationParameter AuthenticationParameter authenticatedUser,
			@HTTPMethodParameter(name = Definitions.PARAMETER_CONTENT_TYPES, required = false) StringParameter contentTypes,
			@HTTPMethodParameter(name = Definitions.PARAMETER_SCREEN_NAMES, required = false) StringParameter screenNames,
			@HTTPMethodParameter(name = Definitions.PARAMETER_SYNCHRONIZE, required = false) BooleanParameter synchronize)
	{
		TJContentCore.summarize(authenticatedUser.getUserIdentity(), (contentTypes.hasValues() ? ContentType.fromString(contentTypes.getValues()) : EnumSet.allOf(ContentType.class)), screenNames.getValues(), true, synchronize.isTrue());
	}
}
