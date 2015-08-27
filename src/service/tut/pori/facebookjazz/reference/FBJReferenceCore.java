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

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.log4j.Logger;

import service.tut.pori.contentanalysis.AbstractTaskDetails;
import service.tut.pori.contentanalysis.AsyncTask.TaskStatus;
import service.tut.pori.contentanalysis.AsyncTask.TaskType;
import service.tut.pori.contentanalysis.PhotoList;
import service.tut.pori.contentanalysis.PhotoTaskResponse;
import service.tut.pori.contentanalysis.MediaObjectList;
import service.tut.pori.contentanalysis.reference.CAReferenceCore;
import service.tut.pori.contentanalysis.reference.CAXMLObjectCreator;
import service.tut.pori.facebookjazz.Definitions;
import service.tut.pori.facebookjazz.FBFeedbackTaskDetails;
import service.tut.pori.facebookjazz.FBSummarizationTaskDetails;
import service.tut.pori.facebookjazz.FBTaskResponse;
import service.tut.pori.facebookjazz.FacebookComment;
import service.tut.pori.facebookjazz.FacebookEvent;
import service.tut.pori.facebookjazz.FacebookExtractor.ContentType;
import service.tut.pori.facebookjazz.FacebookGroup;
import service.tut.pori.facebookjazz.FacebookLike;
import service.tut.pori.facebookjazz.FacebookLocation;
import service.tut.pori.facebookjazz.FacebookPhotoDescription;
import service.tut.pori.facebookjazz.FacebookPhotoTag;
import service.tut.pori.facebookjazz.FacebookProfile;
import service.tut.pori.facebookjazz.FacebookRelationship;
import service.tut.pori.facebookjazz.FacebookStatusMessage;
import service.tut.pori.facebookjazz.FacebookUserDetails;
import service.tut.pori.facebookjazz.FacebookVideoDescription;
import service.tut.pori.facebookjazz.WeightModifier;
import service.tut.pori.facebookjazz.WeightModifierList;
import core.tut.pori.http.parameters.DataGroups;
import core.tut.pori.http.parameters.Limits;
import core.tut.pori.http.parameters.SortOptions;
import core.tut.pori.users.UserIdentity;
import core.tut.pori.utils.XMLFormatter;

/**
 * The reference implementations for FacebookJazz Service.
 *
 */
public final class FBJReferenceCore {
	private static final FBJXMLObjectCreator CREATOR = new FBJXMLObjectCreator(null);
	private static final DataGroups DATAGROUPS_BACKEND_RESPONSE = new DataGroups(CAXMLObjectCreator.DATA_GROUP_BACKEND_RESPONSE);	// data groups for add task callback
	private static final Limits DEFAULT_LIMITS = new Limits(0, 0);	// default number of limits for references
	private static final Logger LOGGER = Logger.getLogger(FBJReferenceCore.class);
	
	/**
	 * 
	 */
	private FBJReferenceCore(){
		// nothing needed
	}

	/**
	 * 
	 * @param authenticatedUser
 	 * @param dataGroups
	 * @param limits only the amount of results is processed for the limits. This is because the results are randomly generated, and thus have no
	 * order which could be used for paging.
	 * @param sortOptions sort options are ignored. This is because of the complexity of the sort operations performed for randomly generated data.
	 * @return example list of media objects for the given values
	 */
	public static MediaObjectList retrieveTagsForUser(UserIdentity authenticatedUser, DataGroups dataGroups, Limits limits, SortOptions sortOptions) {
		LOGGER.info((authenticatedUser == null ? "No logged in user." : "Ignoring the logged in user, id: "+authenticatedUser.getUserId()));	// only notify of the logged in status
		return CREATOR.createTagList(CREATOR.createBackendId(), dataGroups, limits, authenticatedUser);
	}

	/**
	 * 
	 * @param authenticatedUser
	 * @param ranks list of ranks in the form of rank=guid:VALUE,guid:VALUE,...
	 */
	public static void setRank(UserIdentity authenticatedUser, List<String> ranks) {
		LOGGER.info((authenticatedUser == null ? "No logged in user." : "Ignoring the logged in user, id: "+authenticatedUser.getUserId()));	// only notify of the logged in status
		if(ranks == null || ranks.isEmpty()){
			throw new IllegalArgumentException("Failed to process rank parameter.");
		}
		
		for(String r : ranks){
			String[] parts = r.split(core.tut.pori.http.Definitions.SEPARATOR_URI_QUERY_TYPE_VALUE);
			if(parts.length != 2){
				throw new IllegalArgumentException("Failed to process rank parameter: "+r);
			}
		}  // for
	}

	/**
	 * 
	 * @param response
	 */
	public static void taskFinished(PhotoTaskResponse response) {
		Integer tBackendId = response.getBackendId();
		if(tBackendId == null){
			throw new IllegalArgumentException("Invalid backendId: "+tBackendId);
		}
		Long tTaskId = response.getTaskId();
		if(tTaskId == null){
			throw new IllegalArgumentException("Invalid taskId: "+tTaskId);
		}
		
		TaskStatus status = response.getStatus();
		if(status == null){
			throw new IllegalArgumentException("TaskStatus is invalid or missing.");
		}

		TaskType type = response.getTaskType();
		if(type == null){
			throw new IllegalArgumentException("TaskType is invalid or missing.");
		}

		try{
			switch(type){
				case FACEBOOK_PROFILE_SUMMARIZATION:
					FBTaskResponse fr = (FBTaskResponse) response;
					if(!PhotoList.isValid(fr.getPhotoList()) && !MediaObjectList.isValid(fr.getMediaObjects())){
						LOGGER.warn("No valid media object list or photo list.");
					}
					break;
				case BACKEND_FEEDBACK: // should not have any content, so accept anything
					break;
				default:			
					throw new IllegalArgumentException("Tasks of type: "+type.name()+" are not supported by this validator.");
			}
		}catch(ClassCastException ex){
			LOGGER.debug(ex, ex);
			throw new IllegalArgumentException("Task content data was not of the expected type.");
		}
	}

	/**
	 * 
	 * @param taskDetails
	 */
	public static void addTask(FBTaskDetails taskDetails) {
		Integer tBackendId = taskDetails.getBackendId();
		if(tBackendId == null){
			throw new IllegalArgumentException("Invalid backendId: "+tBackendId);
		}
		Long tTaskId = taskDetails.getTaskId();
		if(tTaskId == null){
			throw new IllegalArgumentException("Invalid taskId: "+tTaskId);
		}

		String uri = taskDetails.getCallbackUri();
		if(StringUtils.isBlank(uri)){
			throw new IllegalArgumentException("Invalid callbackUri: "+uri);
		}

		TaskType type = taskDetails.getTaskType();
		if(type == null){
			throw new IllegalArgumentException("TaskType is invalid or missing.");
		}

		switch(type){
			case FACEBOOK_PROFILE_SUMMARIZATION:
				if(taskDetails.getProfile() == null){
					throw new IllegalArgumentException("Invalid "+Definitions.ELEMENT_FACEBOOK_PROFILE);
				}
				addTaskAsyncCallback(taskDetails, CREATOR.createTagList(tBackendId, DATAGROUPS_BACKEND_RESPONSE, DEFAULT_LIMITS, taskDetails.getUserId()));
				break;
			case FACEBOOK_PROFILE_SUMMARIZATION_FEEDBACK:
				if(!MediaObjectList.isValid(taskDetails.getTags())){
					LOGGER.warn("Invalid media object list.");
				}
				addTaskAsyncCallback(taskDetails, null);
				break;
			case BACKEND_FEEDBACK: 
				if(!PhotoList.isValid(taskDetails.getPhotoList())){
					throw new IllegalArgumentException("Invalid "+service.tut.pori.contentanalysis.Definitions.ELEMENT_PHOTOLIST);
				}
				addTaskAsyncCallback(taskDetails, null);
				break;
			default:
				throw new IllegalArgumentException("Tasks of type: "+type.name()+" are not supported by this validator.");
		}
	}
	
	/**
	 * Call asynchronously the callback given in the details, returning an example task response
	 * 
	 * @param details
	 * @param mediaObjectList
	 * @see service.tut.pori.facebookjazz.FBTaskResponse
	 */
	public static void addTaskAsyncCallback(AbstractTaskDetails details, MediaObjectList mediaObjectList) {
		HttpPost post = new HttpPost(details.getCallbackUri());
		FBTaskResponse r = new FBTaskResponse();
		r.setBackendId(details.getBackendId());
		r.setTaskId(details.getTaskId());
		r.setStatus(TaskStatus.COMPLETED);
		r.setTaskType(details.getTaskType());
		r.setMediaObjects(mediaObjectList);
		post.setEntity(new StringEntity((new XMLFormatter()).toString(r), core.tut.pori.http.Definitions.ENCODING_UTF8));

		CAReferenceCore.executeAsyncCallback(post);
	}

	/**
	 * 
	 * @param authenticatedUser
	 * @param weightModifierList
	 */
	public static void setTagWeights(UserIdentity authenticatedUser, WeightModifierList weightModifierList) {
		LOGGER.info((authenticatedUser == null ? "No logged in user." : "Ignoring the logged in user, id: "+authenticatedUser.getUserId()));	// only notify of the logged in status
		
		if(!WeightModifierList.isValid(weightModifierList)){
			throw new IllegalArgumentException("Invalid "+Definitions.ELEMENT_WEIGHT_MODIFIER_LIST+".");
		}
	}

	/**
	 * 
	 * @param authenticatedUser
	 * @param userId
	 * @return example list of weights for the given values
	 */
	public static WeightModifierList retrieveTagWeights(UserIdentity authenticatedUser, Long userId) {
		LOGGER.info((authenticatedUser == null ? "No logged in user, with user id filter: "+userId : "Ignoring the logged in user, id: "+authenticatedUser.getUserId()+", with user id filter: "+userId));	// only notify of the logged in status
		return CREATOR.createWeightModifierList();
	}

	/**
	 * 
	 * @param authenticatedUser
	 * @param contentTypes
	 * @param synchronize
	 */
	public static void summarize(UserIdentity authenticatedUser, List<String> contentTypes, boolean synchronize) {
		LOGGER.info((authenticatedUser == null ? "No logged in user." : "Ignoring the logged in user, id: "+authenticatedUser.getUserId()));	// only notify of the logged in status
		ContentType.fromString(contentTypes); // throws an exception on invalid values
	}

	/**
	 * 
	 * @return randomly generated weihgt modifier list
	 */
	public static WeightModifierList generateWeighModifierList() {
		return CREATOR.createWeightModifierList();
	}

	/**
	 * 
	 * @param limits
	 * @return randomly generated task response
	 */
	public static FBTaskResponse generateTaskResponse(Limits limits) {
		return CREATOR.createTaskResponse(DATAGROUPS_BACKEND_RESPONSE, limits, null, TaskType.FACEBOOK_PROFILE_SUMMARIZATION);
	}

	/**
	 * 
	 * @param limits
	 * @return randomly generated task details
	 */
	public static FBSummarizationTaskDetails generateFBSummarizationTaskDetails(Limits limits) {
		int likeCount = limits.getMaxItems(Definitions.ELEMENT_LIKE_LIST);
		if(likeCount >= Limits.DEFAULT_MAX_ITEMS){
			LOGGER.debug("Reseting limits for "+Definitions.ELEMENT_LIKE_LIST+": max items was "+Limits.DEFAULT_MAX_ITEMS);
			likeCount = DEFAULT_LIMITS.getMaxItems(Definitions.ELEMENT_LIKE_LIST);
		}
		int eventCount = limits.getMaxItems(Definitions.ELEMENT_EVENT_LIST);
		if(eventCount >= Limits.DEFAULT_MAX_ITEMS){
			LOGGER.debug("Reseting limits for "+Definitions.ELEMENT_EVENT_LIST+": max items was "+Limits.DEFAULT_MAX_ITEMS);
			eventCount = DEFAULT_LIMITS.getMaxItems(Definitions.ELEMENT_EVENT_LIST);
		}
		int groupCount = limits.getMaxItems(Definitions.ELEMENT_GROUP_LIST);
		if(groupCount >= Limits.DEFAULT_MAX_ITEMS){
			LOGGER.debug("Reseting limits for "+Definitions.ELEMENT_GROUP_LIST+": max items was "+Limits.DEFAULT_MAX_ITEMS);
			groupCount = DEFAULT_LIMITS.getMaxItems(Definitions.ELEMENT_GROUP_LIST);
		}
		int photoDescriptionCount = limits.getMaxItems(Definitions.ELEMENT_PHOTO_DESCRIPTION_LIST);
		if(photoDescriptionCount >= Limits.DEFAULT_MAX_ITEMS){
			LOGGER.debug("Reseting limits for "+Definitions.ELEMENT_PHOTO_DESCRIPTION_LIST+": max items was "+Limits.DEFAULT_MAX_ITEMS);
			photoDescriptionCount = DEFAULT_LIMITS.getMaxItems(Definitions.ELEMENT_PHOTO_DESCRIPTION_LIST);
		}
		int videoDescriptionCount = limits.getMaxItems(Definitions.ELEMENT_VIDEO_DESCRIPTION_LIST);
		if(videoDescriptionCount >= Limits.DEFAULT_MAX_ITEMS){
			LOGGER.debug("Reseting limits for "+Definitions.ELEMENT_VIDEO_DESCRIPTION_LIST+": max items was "+Limits.DEFAULT_MAX_ITEMS);
			videoDescriptionCount = DEFAULT_LIMITS.getMaxItems(Definitions.ELEMENT_VIDEO_DESCRIPTION_LIST);
		}
		int statusMessageCount = limits.getMaxItems(Definitions.ELEMENT_STATUS_MESSAGE_LIST);
		if(statusMessageCount >= Limits.DEFAULT_MAX_ITEMS){
			LOGGER.debug("Reseting limits for "+Definitions.ELEMENT_STATUS_MESSAGE_LIST+": max items was "+Limits.DEFAULT_MAX_ITEMS);
			statusMessageCount = DEFAULT_LIMITS.getMaxItems(Definitions.ELEMENT_STATUS_MESSAGE_LIST);
		}
		int tagCount = limits.getMaxItems(Definitions.ELEMENT_PHOTO_TAG_LIST);
		if(tagCount >= Limits.DEFAULT_MAX_ITEMS){
			LOGGER.debug("Reseting limits for "+Definitions.ELEMENT_PHOTO_TAG_LIST+": max items was "+Limits.DEFAULT_MAX_ITEMS);
			tagCount = DEFAULT_LIMITS.getMaxItems(Definitions.ELEMENT_PHOTO_TAG_LIST);
		}
		int commentCount = limits.getMaxItems(Definitions.ELEMENT_COMMENT_LIST);
		if(commentCount >= Limits.DEFAULT_MAX_ITEMS){
			LOGGER.debug("Reseting limits for "+Definitions.ELEMENT_COMMENT_LIST+": max items was "+Limits.DEFAULT_MAX_ITEMS);
			commentCount = DEFAULT_LIMITS.getMaxItems(Definitions.ELEMENT_COMMENT_LIST);
		}
		return CREATOR.createFBSummarizationTaskDetails(likeCount, eventCount, groupCount, photoDescriptionCount, videoDescriptionCount, statusMessageCount, tagCount, commentCount);
	}

	/**
	 * 
	 * @param limits
	 * @return randomly generated task details
	 */
	public static FBFeedbackTaskDetails generateFBFeedbackTaskDetails(Limits limits) {
		int objectCount = limits.getMaxItems(service.tut.pori.contentanalysis.Definitions.ELEMENT_MEDIA_OBJECTLIST);
		if(objectCount >= Limits.DEFAULT_MAX_ITEMS){
			LOGGER.debug("Reseting limits for "+service.tut.pori.contentanalysis.Definitions.ELEMENT_MEDIA_OBJECTLIST+": max items was "+Limits.DEFAULT_MAX_ITEMS);
			objectCount = DEFAULT_LIMITS.getMaxItems(service.tut.pori.contentanalysis.Definitions.ELEMENT_MEDIA_OBJECTLIST);
		}

		return CREATOR.createFBFeedbackTaskDetails(objectCount);
	}

	/**
	 * 
	 * @return randomly generated photo tag
	 */
	public static FacebookPhotoTag generateFacebookPhotoTag() {
		return CREATOR.createFacebookPhotoTag();
	}

	/**
	 * 
	 * @return randomly generated weight modifier
	 */
	public static WeightModifier generateWeighModifier() {
		return CREATOR.createWeightModifier(null);
	}

	/**
	 * 
	 * @return randomly generated comment
	 */
	public static FacebookComment generateFacebookComment() {
		return CREATOR.createComment();
	}

	/**
	 * 
	 * @return randomly generated event
	 */
	public static FacebookEvent generateFacebookEvent() {
		return CREATOR.createFacebookEvent();
	}

	/**
	 * 
	 * @return randomly generated group
	 */
	public static FacebookGroup generateFacebookGroup() {
		return CREATOR.createFacebookGroup();
	}

	/**
	 * 
	 * @return randomly generated location
	 */
	public static FacebookLocation generateFacebookLocation() {
		return CREATOR.createFacebookLocation();
	}

	/**
	 * 
	 * @return randomly generated description
	 */
	public static FacebookPhotoDescription generateFacebookPhotoDescription() {
		return CREATOR.createFacebookPhotoDescription(DEFAULT_LIMITS.getMaxItems(Definitions.ELEMENT_COMMENT_LIST), DEFAULT_LIMITS.getMaxItems(Definitions.ELEMENT_PHOTO_TAG_LIST));
	}

	/**
	 * 
	 * @return randomly generated profile
	 */
	public static FacebookProfile generateFacebookProfile() {
		return CREATOR.createFacebookProfile(DEFAULT_LIMITS.getMaxItems(Definitions.ELEMENT_LIKE_LIST), DEFAULT_LIMITS.getMaxItems(Definitions.ELEMENT_EVENT_LIST), DEFAULT_LIMITS.getMaxItems(Definitions.ELEMENT_GROUP_LIST), DEFAULT_LIMITS.getMaxItems(Definitions.ELEMENT_PHOTO_DESCRIPTION_LIST), DEFAULT_LIMITS.getMaxItems(Definitions.ELEMENT_VIDEO_DESCRIPTION_LIST), DEFAULT_LIMITS.getMaxItems(Definitions.ELEMENT_STATUS_MESSAGE_LIST), DEFAULT_LIMITS.getMaxItems(Definitions.ELEMENT_PHOTO_TAG_LIST), DEFAULT_LIMITS.getMaxItems(Definitions.ELEMENT_COMMENT_LIST));
	}

	/**
	 * 
	 * @return randomly generated profile
	 */
	public static FacebookRelationship generateFacebookRelationship() {
		return CREATOR.createFacebookRelationship();
	}

	/**
	 * 
	 * @return randomly generated status message
	 */
	public static FacebookStatusMessage generateFacebookStatusMessage() {
		return CREATOR.createFacebookStatusMessage(DEFAULT_LIMITS.getMaxItems(Definitions.ELEMENT_COMMENT_LIST));
	}

	/**
	 * 
	 * @return randomly generated user details
	 */
	public static FacebookUserDetails generateFacebookUserDetails() {
		return CREATOR.createFacebookUserDetails();
	}

	/**
	 * 
	 * @return randomly generated video description
	 */
	public static FacebookVideoDescription generateFacebookVideoDescription() {
		return CREATOR.createFacebookVideoDescription(DEFAULT_LIMITS.getMaxItems(Definitions.ELEMENT_COMMENT_LIST));
	}

	/**
	 * 
	 * @return randomly generated like
	 */
	public static FacebookLike generateFacebookLike() {
		return CREATOR.createFacebookLike();
	}
}
