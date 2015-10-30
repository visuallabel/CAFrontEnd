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
package service.tut.pori.contentsuggest.reference;

import service.tut.pori.contentsuggest.Definitions;
import core.tut.pori.http.Response;
import core.tut.pori.http.annotations.HTTPMethodParameter;
import core.tut.pori.http.annotations.HTTPService;
import core.tut.pori.http.annotations.HTTPServiceMethod;
import core.tut.pori.http.parameters.Limits;

/**
 * Generates example XML output
 * 
 */
@HTTPService(name = service.tut.pori.contentsuggest.reference.Definitions.SERVICE_CS_REFERENCE_EXAMPLE)
public class ExampleService {
	/**
	 * Generates example autocomplete result
	 * 
	 * @param limits paging limits
	 * @return AutoCompleteResult
	 * @see service.tut.pori.contentsuggest.AutoCompleteResult
	 */
	@HTTPServiceMethod(name = Definitions.ELEMENT_AUTOCOMPLETE_RESULTS)
	public Response autoCompleteResult(
			@HTTPMethodParameter(name = Limits.PARAMETER_DEFAULT_NAME, required=false, defaultValue="0-0") Limits limits
			) 
	{
		Example example = new Example();
		example.setAutoCompleteResult(ContentSuggestReferenceCore.generateAutoCompleteResult(limits));
		return new Response(example);
	}
}
