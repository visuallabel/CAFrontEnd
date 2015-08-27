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
package service.tut.pori.contentstorage.reference;

import core.tut.pori.http.Response;
import core.tut.pori.http.annotations.HTTPMethodParameter;
import core.tut.pori.http.annotations.HTTPService;
import core.tut.pori.http.annotations.HTTPServiceMethod;
import core.tut.pori.http.parameters.Limits;

/**
 * Generates example XML output
 * 
 */
@HTTPService(name = service.tut.pori.contentstorage.reference.Definitions.SERVICE_COS_REFERENCE_EXAMPLE)
public class ExampleService {
	/**
	 * Generates example media list
	 * @param limits optional paging limits
	 * @return MediaList
	 * @see service.tut.pori.contentstorage.MediaList
	 */
	@HTTPServiceMethod(name = service.tut.pori.contentstorage.Definitions.ELEMENT_MEDIALIST)
	public Response videoList(
			@HTTPMethodParameter(name = Limits.PARAMETER_DEFAULT_NAME, required=false, defaultValue="0-0") Limits limits
			) 
	{
		Example example = new Example();
		example.setMediaList(ContentStorageReferenceCore.generateMediaList(limits));
		return new Response(example);
	}
	
}
