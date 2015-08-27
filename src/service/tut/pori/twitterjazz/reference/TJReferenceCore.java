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
package service.tut.pori.twitterjazz.reference;

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
import service.tut.pori.twitterjazz.Definitions;
import service.tut.pori.twitterjazz.TwitterExtractor.ContentType;
import service.tut.pori.twitterjazz.TwitterLocation;
import service.tut.pori.twitterjazz.TwitterPhotoDescription;
import service.tut.pori.twitterjazz.TwitterPhotoTag;
import service.tut.pori.twitterjazz.TwitterProfile;
import service.tut.pori.twitterjazz.TwitterStatusMessage;
import service.tut.pori.twitterjazz.TwitterSummarizationTaskDetails;
import service.tut.pori.twitterjazz.TwitterTaskResponse;
import service.tut.pori.twitterjazz.TwitterUserDetails;
import service.tut.pori.twitterjazz.TwitterVideoDescription;
import core.tut.pori.http.parameters.DataGroups;
import core.tut.pori.http.parameters.Limits;
import core.tut.pori.http.parameters.SortOptions;
import core.tut.pori.users.UserIdentity;
import core.tut.pori.utils.XMLFormatter;

/**
 * The reference implementations for TwitterJazz Service.
 *
 */
public final class TJReferenceCore {
	private static final TJXMLObjectCreator CREATOR = new TJXMLObjectCreator(null);
	private static final DataGroups DATAGROUPS_BACKEND_RESPONSE = new DataGroups(CAXMLObjectCreator.DATA_GROUP_BACKEND_RESPONSE);	// data groups for add task callback
	private static final Limits DEFAULT_LIMITS = new Limits(0, 0);	// default number of limits for references
	private static final Logger LOGGER = Logger.getLogger(TJReferenceCore.class);	

	/**
	 * 
	 */
	private TJReferenceCore(){
		// nothing needed
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
				case TWITTER_PROFILE_SUMMARIZATION:
					TwitterTaskResponse fr = (TwitterTaskResponse) response;
					if(!PhotoList.isValid(fr.getPhotoList()) && !MediaObjectList.isValid(fr.getMediaObjects())){
						throw new IllegalArgumentException("No content.");
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
	public static void addTask(TwitterTaskDetails taskDetails) {
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
			case TWITTER_PROFILE_SUMMARIZATION:
				if(taskDetails.getProfile() == null){
					throw new IllegalArgumentException("Invalid "+Definitions.ELEMENT_TWITTER_PROFILE);
				}
				addTaskAsyncCallback(taskDetails, CREATOR.createTagList(tBackendId, DATAGROUPS_BACKEND_RESPONSE, DEFAULT_LIMITS, taskDetails.getUserId()));
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
	 * @see service.tut.pori.twitterjazz.TwitterTaskResponse
	 */
	public static void addTaskAsyncCallback(AbstractTaskDetails details, MediaObjectList mediaObjectList){
		HttpPost post = new HttpPost(details.getCallbackUri());
		TwitterTaskResponse r = new TwitterTaskResponse();
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
	 * @param dataGroups
	 * @param limits only the amount of results is processed for the limits. This is because the results are randomly generated, and thus have no
	 * order which could be used for paging.
	 * @param sortOptions sort options are ignored. This is because of the complexity of the sort operations performed for randomly generated data.
	 * @return media object list
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
	 * @param authenticatedUser
	 * @param contentTypes
	 * @param screenNames
	 * @param synchronize
	 */
	public static void summarize(UserIdentity authenticatedUser, List<String> contentTypes, List<String> screenNames, boolean synchronize) {
		LOGGER.info((authenticatedUser == null ? "No logged in user." : "Ignoring the logged in user, id: "+authenticatedUser.getUserId()));	// only notify of the logged in status
		ContentType.fromString(contentTypes); // throws an exception on invalid values
	}

	/**
	 * 
	 * @param limits
	 * @return randomly generated task response
	 */
	public static TwitterTaskResponse generateTaskResponse(Limits limits) {
		return CREATOR.createTwitterTaskResponse(DATAGROUPS_BACKEND_RESPONSE, limits, null, TaskType.TWITTER_PROFILE_SUMMARIZATION);
	}

	/**
	 * 
	 * @param limits
	 * @return randomly generated task details
	 */
	public static TwitterSummarizationTaskDetails generateTwitterSummarizationTaskDetails(Limits limits) {
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
		return CREATOR.createTwitterSummarizationTaskDetails(photoDescriptionCount, statusMessageCount, videoDescriptionCount, tagCount);
	}

	/**
	 * 
	 * @return randomly generated location
	 */
	public static TwitterLocation generateTwitterLocation() {
		return CREATOR.createTwitterLocation();
	}

	/**
	 * 
	 * @return randomly generated photo description
	 */
	public static TwitterPhotoDescription generateTwitterPhotoDescription() {
		return CREATOR.createTwitterPhotoDescription(DEFAULT_LIMITS.getMaxItems(Definitions.ELEMENT_PHOTO_TAG_LIST));
	}

	/**
	 * 
	 * @return randomly generated photo tag
	 */
	public static TwitterPhotoTag generateTwitterPhotoTag() {
		return CREATOR.createTwitterPhotoTag();
	}

	/**
	 * 
	 * @return randomly generated profile
	 */
	public static TwitterProfile generateTwitterProfile() {
		return CREATOR.createTwitterProfile(DEFAULT_LIMITS.getMaxItems(Definitions.ELEMENT_PHOTO_DESCRIPTION_LIST), DEFAULT_LIMITS.getMaxItems(Definitions.ELEMENT_STATUS_MESSAGE_LIST), DEFAULT_LIMITS.getMaxItems(Definitions.ELEMENT_VIDEO_DESCRIPTION_LIST), DEFAULT_LIMITS.getMaxItems(Definitions.ELEMENT_PHOTO_TAG_LIST));
	}

	/**
	 * 
	 * @return randomly generated status message
	 */
	public static TwitterStatusMessage generateTwitterStatusMessage() {
		return CREATOR.createTwitterStatusMessage();
	}

	/**
	 * 
	 * @return randomly generated user details
	 */
	public static TwitterUserDetails generateTwitterUserDetails() {
		return CREATOR.createTwitterUserDetails();
	}

	/**
	 * 
	 * @return randomly generated video description
	 */
	public static TwitterVideoDescription generateTwitterVideoDescription() {
		return CREATOR.createTwitterVideoDescription();
	}
}
