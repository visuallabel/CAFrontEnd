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
package service.tut.pori.twitterjazz.reference;

import service.tut.pori.twitterjazz.Definitions;
import core.tut.pori.http.Response;
import core.tut.pori.http.annotations.HTTPMethodParameter;
import core.tut.pori.http.annotations.HTTPService;
import core.tut.pori.http.annotations.HTTPServiceMethod;
import core.tut.pori.http.parameters.Limits;

/**
 * Generates example XML output
 * 
 */
@HTTPService(name = service.tut.pori.twitterjazz.reference.Definitions.SERVICE_TJ_REFERENCE_EXAMPLE)
public class ExampleService {
	/**
	 * Generates example task response
	 * 
	 * @param limits paging limits
	 * @return TwitterTaskResponse
	 * @see service.tut.pori.twitterjazz.TwitterTaskResponse
	 */
	@HTTPServiceMethod(name = service.tut.pori.contentanalysis.Definitions.ELEMENT_TASK_RESULTS)
	public Response taskResults(
			@HTTPMethodParameter(name = Limits.PARAMETER_DEFAULT_NAME, required=false, defaultValue="0-0") Limits limits
			) 
	{
		Example example = new Example();
		example.setTaskResponse(TJReferenceCore.generateTaskResponse(limits));
		return new Response(example);
	}
	
	/**
	 * Generates example task details
	 * 
	 * @param limits paging limits
	 * @return TwitterSummarizationTaskDetails
	 * @see service.tut.pori.twitterjazz.TwitterSummarizationTaskDetails
	 */
	@HTTPServiceMethod(name = service.tut.pori.contentanalysis.Definitions.ELEMENT_TASK_DETAILS)
	public Response taskDetails(
			@HTTPMethodParameter(name = Limits.PARAMETER_DEFAULT_NAME, required=false, defaultValue="0-0") Limits limits
			) 
	{
		Example example = new Example();
		example.setTaskDetails(TJReferenceCore.generateTwitterSummarizationTaskDetails(limits));
		return new Response(example);
	}
	
	/**
	 * Generates example TwitterLocation
	 * 
	 * @return TwitterLocation
	 * @see service.tut.pori.twitterjazz.TwitterLocation
	 */
	@HTTPServiceMethod(name = Definitions.ELEMENT_LOCATION)
	public Response twitterLocation() {
		Example example = new Example();
		example.setTwitterLocation(TJReferenceCore.generateTwitterLocation());
		return new Response(example);
	}
	
	/**
	 * Generates example TwitterPhotoDescription
	 * 
	 * @return TwitterPhotoDescription
	 * @see service.tut.pori.twitterjazz.TwitterPhotoDescription
	 */
	@HTTPServiceMethod(name = Definitions.ELEMENT_PHOTO_DESCRIPTION)
	public Response twitterPhotoDescription() {
		Example example = new Example();
		example.setTwitterPhotoDescription(TJReferenceCore.generateTwitterPhotoDescription());
		return new Response(example);
	}
	
	/**
	 * Generates example TwitterPhotoTag
	 * 
	 * @return TwitterPhotoTag
	 * @see service.tut.pori.twitterjazz.TwitterPhotoTag
	 */
	@HTTPServiceMethod(name = Definitions.ELEMENT_PHOTO_TAG)
	public Response twitterPhotoTag() {
		Example example = new Example();
		example.setTwitterPhotoTag(TJReferenceCore.generateTwitterPhotoTag());
		return new Response(example);
	}
	
	/**
	 * Generates example TwitterProfile
	 * 
	 * @return TwitterProfile
	 * @see service.tut.pori.twitterjazz.TwitterProfile
	 */
	@HTTPServiceMethod(name = Definitions.ELEMENT_TWITTER_PROFILE)
	public Response twitterProfile() {
		Example example = new Example();
		example.setTwitterProfile(TJReferenceCore.generateTwitterProfile());
		return new Response(example);
	}
	
	/**
	 * Generates example TwitterStatusMessage
	 * 
	 * @return TwitterStatusMessage
	 * @see service.tut.pori.twitterjazz.TwitterStatusMessage
	 */
	@HTTPServiceMethod(name = Definitions.ELEMENT_STATUS_MESSAGE)
	public Response twitterStatusMessage() {
		Example example = new Example();
		example.setTwitterStatusMessage(TJReferenceCore.generateTwitterStatusMessage());
		return new Response(example);
	}
	
	/**
	 * Generates example TwitterUserDetails
	 * 
	 * @return TwitterUserDetails
	 * @see service.tut.pori.twitterjazz.TwitterUserDetails
	 */
	@HTTPServiceMethod(name = Definitions.ELEMENT_USER_DETAILS)
	public Response twitterUserDetails() {
		Example example = new Example();
		example.setTwitterUserDetails(TJReferenceCore.generateTwitterUserDetails());
		return new Response(example);
	}
	
	/**
	 * Generates example TwitterVideoDescription
	 * 
	 * @return TwitterVideoDescription
	 * @see service.tut.pori.twitterjazz.TwitterVideoDescription
	 */
	@HTTPServiceMethod(name = Definitions.ELEMENT_VIDEO_DESCRIPTION)
	public Response twitterVideoDescription() {
		Example example = new Example();
		example.setTwitterVideoDescription(TJReferenceCore.generateTwitterVideoDescription());
		return new Response(example);
	}
}
