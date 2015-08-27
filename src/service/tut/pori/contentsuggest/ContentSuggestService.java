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
package service.tut.pori.contentsuggest;

import service.tut.pori.contentanalysis.MediaObject.MediaObjectType;
import core.tut.pori.http.Response;
import core.tut.pori.http.annotations.HTTPAuthenticationParameter;
import core.tut.pori.http.annotations.HTTPMethodParameter;
import core.tut.pori.http.annotations.HTTPService;
import core.tut.pori.http.annotations.HTTPServiceMethod;
import core.tut.pori.http.parameters.AuthenticationParameter;
import core.tut.pori.http.parameters.DataGroups;
import core.tut.pori.http.parameters.Limits;
import core.tut.pori.http.parameters.StringParameter;

/**
 * Service for content suggestions
 *
 * @see service.tut.pori.contentsuggest.reference.ClientService
 */
@HTTPService(name=Definitions.SERVICE_CS)
public class ContentSuggestService {

	/**
	 * Autocomplete service method to get suggestions based on the indexed data.
	 * 
	 * @see service.tut.pori.contentsuggest.reference.ClientService#suggest(AuthenticationParameter, DataGroups, StringParameter, Limits)
	 * 
	 * @param authenticatedUser
	 * @param dataGroups filters based on VisualObjectType.
	 * @param query the term to be searched for.
	 * @param limits paging options
	 * @return response
	 * 
	 * @see MediaObjectType
	 */
	@HTTPServiceMethod(name = Definitions.METHOD_SUGGEST, acceptedMethods={core.tut.pori.http.Definitions.METHOD_GET})
	public Response suggest(
			@HTTPAuthenticationParameter AuthenticationParameter authenticatedUser,
			@HTTPMethodParameter(name = DataGroups.PARAMETER_DEFAULT_NAME, required = false) DataGroups dataGroups,
			@HTTPMethodParameter(name = Definitions.PARAMETER_QUERY, required = true) StringParameter query,
			@HTTPMethodParameter(name = Limits.PARAMETER_DEFAULT_NAME, required = false, defaultValue=Definitions.DEFAULT_LIMITS) Limits limits)
	{
		return new Response(ContentSuggestCore.suggest(authenticatedUser.getUserIdentity(), dataGroups, limits, query.getValue()));
	}
}
