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
package service.tut.pori.facebookjazz.reference;

import service.tut.pori.contentanalysis.AsyncTask.TaskType;
import service.tut.pori.facebookjazz.Definitions;
import core.tut.pori.http.Response;
import core.tut.pori.http.annotations.HTTPMethodParameter;
import core.tut.pori.http.annotations.HTTPService;
import core.tut.pori.http.annotations.HTTPServiceMethod;
import core.tut.pori.http.parameters.Limits;
import core.tut.pori.http.parameters.StringParameter;

/**
 * Generates example XML output
 * 
 */
@HTTPService(name = service.tut.pori.facebookjazz.reference.Definitions.SERVICE_FBJ_REFERENCE_EXAMPLE)
public class ExampleService {
	/**
	 * Generates example weight modifier list
	 * 
	 * @return WeightModifierList
	 * @see service.tut.pori.facebookjazz.WeightModifierList
	 */
	@HTTPServiceMethod(name = Definitions.ELEMENT_WEIGHT_MODIFIER_LIST)
	public Response weightModifierList() {
		Example example = new Example();
		example.setWeightModifierList(FBJReferenceCore.generateWeighModifierList());
		return new Response(example);
	}
	
	/**
	 * Generates example weight modifier
	 * 
	 * @return WeightModifier
	 * @see service.tut.pori.facebookjazz.WeightModifier
	 */
	@HTTPServiceMethod(name = Definitions.ELEMENT_WEIGHT_MODIFIER)
	public Response weightModifier() {
		Example example = new Example();
		example.setWeightModifier(FBJReferenceCore.generateWeighModifier());
		return new Response(example);
	}
	
	/**
	 * Generates example task response
	 * 
	 * @param limits paging limits
	 * @return FBTaskResponse
	 * @see service.tut.pori.facebookjazz.FBTaskResponse
	 */
	@HTTPServiceMethod(name = service.tut.pori.contentanalysis.Definitions.ELEMENT_TASK_RESULTS)
	public Response taskResults(
			@HTTPMethodParameter(name = Limits.PARAMETER_DEFAULT_NAME, required=false, defaultValue="0-0") Limits limits
			) 
	{
		Example example = new Example();
		example.setTaskResponse(FBJReferenceCore.generateTaskResponse(limits));
		return new Response(example);
	}
	
	/**
	 * Generates example task details
	 * 
	 * @param limits paging limits
	 * @param taskType Will default to {@link service.tut.pori.contentanalysis.AsyncTask.TaskType#FACEBOOK_PROFILE_SUMMARIZATION} if missing
	 * @return FBSummarizationTaskDetails
	 * @throws IllegalArgumentException on invalid task type
	 * @see service.tut.pori.facebookjazz.FBSummarizationTaskDetails
	 */
	@HTTPServiceMethod(name = service.tut.pori.contentanalysis.Definitions.ELEMENT_TASK_DETAILS)
	public Response taskDetails(
			@HTTPMethodParameter(name = Limits.PARAMETER_DEFAULT_NAME, required=false, defaultValue="0-0") Limits limits,
			@HTTPMethodParameter(name = service.tut.pori.facebookjazz.reference.Definitions.PARAMETER_TASK_TYPE, required=false) StringParameter taskType
			) throws IllegalArgumentException
	{
		Example example = new Example();
		TaskType type = TaskType.FACEBOOK_PROFILE_SUMMARIZATION;
		if(taskType.hasValues()){
			type = TaskType.fromString(taskType.getValue());
		}
		switch(type){
			case FACEBOOK_PROFILE_SUMMARIZATION:
				example.setTaskDetails(FBJReferenceCore.generateFBSummarizationTaskDetails(limits));
				break;
			case FACEBOOK_PROFILE_SUMMARIZATION_FEEDBACK:
				example.setTaskDetails(FBJReferenceCore.generateFBFeedbackTaskDetails(limits));
				break;
			default:
				throw new IllegalArgumentException("Unsupported task type: "+type.name());
		}
		return new Response(example);
	}
	
	/**
	 * Generates example FacebookPhotoTag
	 * 
	 * @return FacebookPhotoTag
	 * @see service.tut.pori.facebookjazz.FacebookPhotoTag
	 */
	@HTTPServiceMethod(name = Definitions.ELEMENT_PHOTO_TAG)
	public Response facebookPhotoTag() {
		Example example = new Example();
		example.setFacebookPhotoTag(FBJReferenceCore.generateFacebookPhotoTag());
		return new Response(example);
	}
	
	/**
	 * Generates example FacebookComment
	 * 
	 * @return FacebookComment
	 * @see service.tut.pori.facebookjazz.FacebookComment
	 */
	@HTTPServiceMethod(name = Definitions.ELEMENT_COMMENT)
	public Response facebookComment() {
		Example example = new Example();
		example.setFacebookComment(FBJReferenceCore.generateFacebookComment());
		return new Response(example);
	}
	
	/**
	 * Generates example FacebookEvent
	 * 
	 * @return FacebookEvent
	 * @see service.tut.pori.facebookjazz.FacebookEvent
	 */
	@HTTPServiceMethod(name = Definitions.ELEMENT_EVENT)
	public Response facebookEvent() {
		Example example = new Example();
		example.setFacebookEvent(FBJReferenceCore.generateFacebookEvent());
		return new Response(example);
	}
	
	/**
	 * Generates example FacebookGroup
	 * 
	 * @return FacebookGroup
	 * @see service.tut.pori.facebookjazz.FacebookGroup
	 */
	@HTTPServiceMethod(name = Definitions.ELEMENT_GROUP)
	public Response facebookGroup() {
		Example example = new Example();
		example.setFacebookGroup(FBJReferenceCore.generateFacebookGroup());
		return new Response(example);
	}
	
	/**
	 * Generates example FacebookLocation
	 * 
	 * @return FacebookLocation
	 * @see service.tut.pori.facebookjazz.FacebookLocation
	 */
	@HTTPServiceMethod(name = Definitions.ELEMENT_LOCATION)
	public Response facebookLocation() {
		Example example = new Example();
		example.setFacebookLocation(FBJReferenceCore.generateFacebookLocation());
		return new Response(example);
	}
	
	/**
	 * Generates example FacebookPhotoDescription
	 * 
	 * @return FacebookPhotoDescription
	 * @see service.tut.pori.facebookjazz.FacebookPhotoDescription
	 */
	@HTTPServiceMethod(name = Definitions.ELEMENT_PHOTO_DESCRIPTION)
	public Response facebookPhotoDescription() {
		Example example = new Example();
		example.setFacebookPhotoDescription(FBJReferenceCore.generateFacebookPhotoDescription());
		return new Response(example);
	}
	
	/**
	 * Generates example FacebookPhotoDescription
	 * 
	 * @return FacebookVideoDescription
	 * @see service.tut.pori.facebookjazz.FacebookVideoDescription
	 */
	@HTTPServiceMethod(name = Definitions.ELEMENT_VIDEO_DESCRIPTION)
	public Response facebookVideoDescription() {
		Example example = new Example();
		example.setFacebookVideoDescription(FBJReferenceCore.generateFacebookVideoDescription());
		return new Response(example);
	}
	
	/**
	 * Generates example FacebookProfile
	 * 
	 * @return FacebookProfile
	 * @see service.tut.pori.facebookjazz.FacebookProfile
	 */
	@HTTPServiceMethod(name = Definitions.ELEMENT_FACEBOOK_PROFILE)
	public Response facebookProfile() {
		Example example = new Example();
		example.setFacebookProfile(FBJReferenceCore.generateFacebookProfile());
		return new Response(example);
	}
	
	/**
	 * Generates example FacebookRelationship
	 * 
	 * @return FacebookRelationship
	 * @see service.tut.pori.facebookjazz.FacebookRelationship
	 */
	@HTTPServiceMethod(name = Definitions.ELEMENT_RELATIONSHIP)
	public Response facebookRelationship() {
		Example example = new Example();
		example.setFacebookRelationship(FBJReferenceCore.generateFacebookRelationship());
		return new Response(example);
	}
	
	/**
	 * Generates example FacebookStatusMessage
	 * 
	 * @return FacebookStatusMessage
	 * @see service.tut.pori.facebookjazz.FacebookStatusMessage
	 */
	@HTTPServiceMethod(name = Definitions.ELEMENT_STATUS_MESSAGE)
	public Response facebookStatusMessage() {
		Example example = new Example();
		example.setFacebookStatusMessage(FBJReferenceCore.generateFacebookStatusMessage());
		return new Response(example);
	}
	
	/**
	 * Generates example FacebookUserDetails
	 * 
	 * @return FacebookUserDetails
	 * @see service.tut.pori.facebookjazz.FacebookUserDetails
	 */
	@HTTPServiceMethod(name = Definitions.ELEMENT_USER_DETAILS)
	public Response facebookUserDetails() {
		Example example = new Example();
		example.setFacebookUserDetails(FBJReferenceCore.generateFacebookUserDetails());
		return new Response(example);
	}
	
	/**
	 * Generates example FacebookLike
	 * 
	 * @return FacebookLike
	 * @see service.tut.pori.facebookjazz.FacebookLike
	 */
	@HTTPServiceMethod(name = Definitions.ELEMENT_LIKE)
	public Response facebookLike() {
		Example example = new Example();
		example.setFacebookLike(FBJReferenceCore.generateFacebookLike());
		return new Response(example);
	}
}
