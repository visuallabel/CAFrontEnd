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
package service.tut.pori.contentanalysis.video.reference;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.log4j.Logger;

import service.tut.pori.contentanalysis.AnalysisBackend;
import service.tut.pori.contentanalysis.PhotoParameters.AnalysisType;
import service.tut.pori.contentanalysis.AsyncTask.TaskStatus;
import service.tut.pori.contentanalysis.AsyncTask.TaskType;
import service.tut.pori.contentanalysis.BackendStatus;
import service.tut.pori.contentanalysis.CAContentCore.ServiceType;
import service.tut.pori.contentanalysis.CAContentCore.Visibility;
import service.tut.pori.contentanalysis.Definitions;
import service.tut.pori.contentanalysis.MediaObjectList;
import service.tut.pori.contentanalysis.reference.CAReferenceCore;
import service.tut.pori.contentanalysis.reference.CAXMLObjectCreator;
import service.tut.pori.contentanalysis.video.DeletedVideoList;
import service.tut.pori.contentanalysis.video.Timecode;
import service.tut.pori.contentanalysis.video.TimecodeList;
import service.tut.pori.contentanalysis.video.Video;
import service.tut.pori.contentanalysis.video.VideoList;
import service.tut.pori.contentanalysis.video.VideoParameters;
import service.tut.pori.contentanalysis.video.VideoTaskDetails;
import service.tut.pori.contentanalysis.video.VideoTaskResponse;
import core.tut.pori.http.RedirectResponse;
import core.tut.pori.http.Response;
import core.tut.pori.http.parameters.DataGroups;
import core.tut.pori.http.parameters.Limits;
import core.tut.pori.users.UserIdentity;
import core.tut.pori.utils.XMLFormatter;

/**
 * The reference implementations for Video Content Analysis Service.
 *
 */
public final class VideoReferenceCore {
	private static final VideoXMLObjectCreator CREATOR = new VideoXMLObjectCreator(null);
	private static final DataGroups DATAGROUPS_BACKEND_RESPONSE = new DataGroups(CAXMLObjectCreator.DATA_GROUP_BACKEND_RESPONSE);	// data groups for add task callback
	private static final Limits DEFAULT_LIMITS = new Limits(0, 0);	// default number of limits for references
	private static final DataGroups DATAGROUPS_ALL = new DataGroups(DataGroups.DATA_GROUP_ALL);
	private static final String EXAMPLE_URI = "http://users.ics.aalto.fi/jorma/d2i/hs-00.mp4";
	private static final Logger LOGGER = Logger.getLogger(VideoReferenceCore.class);
	
	/**
	 * 
	 */
	private VideoReferenceCore(){
		// nothing needed
	}

	/**
	 * 
	 * @param authenticatedUser
	 * @param serviceId
	 * @param guid
	 * @return an example response for the given values
	 */
	public static RedirectResponse generateTargetUrl(UserIdentity authenticatedUser, ServiceType serviceId, String guid) {
		LOGGER.info((authenticatedUser == null ? "No logged in user." : "Ignoring the logged in user, id: "+authenticatedUser.getUserId()));	// only notify of the logged in status
		return new RedirectResponse(EXAMPLE_URI);
	}

	/**
	 * 
	 * @param dataGroups
	 * @return a randomly generated video
	 */
	public static Video generateVideo(DataGroups dataGroups) {
		return CREATOR.createVideo(null, (DataGroups.isEmpty(dataGroups) ? DATAGROUPS_ALL : dataGroups), DEFAULT_LIMITS, null, null);
	}

	/**
	 * 
	 * @param dataGroups
	 * @param limits
	 * @param cls
	 * @return a randomly generated video list
	 */
	public static VideoList generateVideoList(DataGroups dataGroups, Limits limits, Class<? extends VideoList> cls) {
		if(VideoList.class == cls){
			return CREATOR.createVideoList(null, (DataGroups.isEmpty(dataGroups) ? DATAGROUPS_ALL : dataGroups), limits, null, null);
		}else if(DeletedVideoList.class == cls){
			return CREATOR.createDeletedVideoList(limits);
		}else{
			throw new IllegalArgumentException("Unsupported class : "+cls);
		}
	}

	/**
	 * 
	 * @param dataGroups
	 * @param limits
	 * @param taskType
	 * @return randomly generated task details
	 */
	public static VideoTaskDetails generateVideoTaskDetails(DataGroups dataGroups, Limits limits, TaskType taskType) {
		switch(taskType){
			case ANALYSIS:
			case FEEDBACK:
				break;
			default:
				throw new IllegalArgumentException("Unsupported task type: "+taskType.name());
		}
		limits.setTypeLimits(-1, -1, service.tut.pori.contentanalysis.Definitions.ELEMENT_BACKEND_STATUS_LIST); // do not add back-end status list
		return CREATOR.createVideoTaskDetails(null, (DataGroups.isEmpty(dataGroups) ? DATAGROUPS_ALL : dataGroups), limits, null, taskType);
	}

	/**
	 * 
	 * @return randomly generated timecode
	 */
	public static Timecode generateTimecode() {
		return CREATOR.createTimecode(null);
	}

	/**
	 * 
	 * @param limits
	 * @return a randomly generated timecode list
	 */
	public static TimecodeList generateTimecodeList(Limits limits) {
		return CREATOR.createTimecodeList(limits, false);
	}

	/**
	 * 
	 * @return randomly generated video options
	 */
	public static VideoParameters generateVideoOptions() {
		return CREATOR.createVideoOptions();
	}

	/**
	 * 
	 * @param limits
	 * @return a randomly generated task response
	 */
	public static VideoTaskResponse generateTaskResponse(Limits limits) {
		return CREATOR.createTaskResponse(null, DATAGROUPS_BACKEND_RESPONSE, limits, null, TaskType.ANALYSIS);
	}

	/**
	 * This performs a trivial check for the task contents, checking for the presence of a few key values.
	 * The validity of the actual task contents will not checked.
	 * 
	 * @param response
	 */
	public static void taskFinished(VideoTaskResponse response) {
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
				case ANALYSIS:
					VideoList vl = response.getVideoList();
					if(!VideoList.isEmpty(vl)){
						if(!VideoList.isValid(vl)){
							LOGGER.warn("Invalid "+service.tut.pori.contentanalysis.video.Definitions.ELEMENT_VIDEOLIST);
						}
						for(Video v : response.getVideoList().getVideos()){
							MediaObjectList vObjects = v.getMediaObjects();
							if(MediaObjectList.isEmpty(vObjects)){
								LOGGER.info("No media objects for photo, GUID: "+v.getGUID());
							}else if(!MediaObjectList.isValid(vObjects)){
								throw new IllegalArgumentException("Invalid "+service.tut.pori.contentanalysis.Definitions.ELEMENT_MEDIA_OBJECTLIST);
							}
						}
					}
					break;
				case FEEDBACK:
					if(!VideoList.isValid(response.getVideoList())){
						throw new IllegalArgumentException("Invalid "+service.tut.pori.contentanalysis.video.Definitions.ELEMENT_VIDEOLIST);
					}
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
	 * @param backendId
	 * @param taskId
	 * @param dataGroups
	 * @param limits
	 * @return an example response for the given values
	 */
	public static Response queryTaskDetails(Integer backendId, Long taskId, DataGroups dataGroups, Limits limits) {
		if(limits.getMaxItems(service.tut.pori.contentanalysis.video.Definitions.ELEMENT_VIDEOLIST) >= Limits.DEFAULT_MAX_ITEMS){
			LOGGER.debug("Reseting limits for "+service.tut.pori.contentanalysis.video.Definitions.ELEMENT_VIDEOLIST+": max items was "+Limits.DEFAULT_MAX_ITEMS);
			limits.setTypeLimits(DEFAULT_LIMITS.getStartItem(service.tut.pori.contentanalysis.video.Definitions.ELEMENT_VIDEOLIST), DEFAULT_LIMITS.getEndItem(service.tut.pori.contentanalysis.video.Definitions.ELEMENT_VIDEOLIST), service.tut.pori.contentanalysis.video.Definitions.ELEMENT_VIDEOLIST); // startItem makes no difference for random
		}
		
		if(limits.getMaxItems(service.tut.pori.contentanalysis.Definitions.ELEMENT_MEDIA_OBJECTLIST) >= Limits.DEFAULT_MAX_ITEMS){
			LOGGER.debug("Reseting limits for "+service.tut.pori.contentanalysis.Definitions.ELEMENT_MEDIA_OBJECTLIST+": max items was "+Limits.DEFAULT_MAX_ITEMS);
			limits.setTypeLimits(DEFAULT_LIMITS.getStartItem(service.tut.pori.contentanalysis.Definitions.ELEMENT_MEDIA_OBJECTLIST), DEFAULT_LIMITS.getEndItem(service.tut.pori.contentanalysis.Definitions.ELEMENT_MEDIA_OBJECTLIST), service.tut.pori.contentanalysis.Definitions.ELEMENT_MEDIA_OBJECTLIST); // startItem makes no difference for random
		}
		limits.setTypeLimits(-1, -1, null); // disable all other photo lists
		return new Response(CREATOR.createVideoTaskDetails(backendId, dataGroups, limits, taskId, TaskType.ANALYSIS));
	}

	/**
	 * 
	 * @param authenticatedUser
	 * @param objects
	 * @param dataGroups
	 * @param limits
	 * @param serviceTypes
	 * @param userIdFilters
	 * @return an example response for the given values
	 */
	public static Response similarVideosByObject(UserIdentity authenticatedUser, MediaObjectList objects, DataGroups dataGroups, Limits limits, EnumSet<ServiceType> serviceTypes, long[] userIdFilters) {
		LOGGER.info((authenticatedUser == null ? "No logged in user." : "Ignoring the logged in user, id: "+authenticatedUser.getUserId()));	// only notify of the logged in status
		if(limits.getMaxItems(service.tut.pori.contentanalysis.video.Definitions.ELEMENT_VIDEOLIST) >= Limits.DEFAULT_MAX_ITEMS){
			LOGGER.debug("Reseting limits: Default max items was "+Limits.DEFAULT_MAX_ITEMS);
			limits = DEFAULT_LIMITS; // startItem makes no difference for random
		}
		return new Response(CREATOR.createSearchResults(null, dataGroups, limits, serviceTypes, userIdFilters, objects));
	}

	/**
	 * 
	 * @param authenticatedUser
	 * @param guids
	 * @param dataGroups
	 * @param limits
	 * @param serviceTypes
	 * @param userIdFilter 
	 * @return an example response for the given values
	 */
	public static Response getVideos(UserIdentity authenticatedUser, List<String> guids, DataGroups dataGroups, Limits limits, EnumSet<ServiceType> serviceTypes, long[] userIdFilter) {
		LOGGER.info((authenticatedUser == null ? "No logged in user." : "Ignoring the logged in user, id: "+authenticatedUser.getUserId()));	// only notify of the logged in status
		if(limits.getMaxItems(service.tut.pori.contentanalysis.video.Definitions.ELEMENT_VIDEOLIST) >= Limits.DEFAULT_MAX_ITEMS){
			LOGGER.debug("Reseting limits: Default max items was "+Limits.DEFAULT_MAX_ITEMS);
			limits = DEFAULT_LIMITS; // startItem makes no difference for random
		}
		int userIdCount = (ArrayUtils.isEmpty(userIdFilter) ? 0 : userIdFilter.length);
		VideoList list = CREATOR.createVideoList(null, dataGroups, limits, serviceTypes, null);
		if(list != null && guids != null && !guids.isEmpty()){
			for(Iterator<Video> iter = list.getVideos().iterator();iter.hasNext();){	// remove all extra guids, we could also modify the limit parameter, but for this testing method, the implementation does not matter
				Video video = iter.next();
				if(guids.isEmpty()){	// we have used up all given guids
					iter.remove();
				}else{
					video.setGUID(guids.remove(0));
					video.setVisibility(Visibility.PUBLIC); // there could also be private photos for the authenticated user, but to make sure the results are valid, return only PUBLIC photos
				}
				if(userIdCount > 1){
					video.setOwnerUserId(new UserIdentity(userIdFilter[CREATOR.getRandom().nextInt(userIdCount)]));
				}
			}	// for
		}
		return new Response(list);
	}
	
	/**
	 * This performs a trivial check for the task contents, checking for the presence of a few key values.
	 * The validity of the actual task contents will not be checked.
	 * 
	 * @param taskDetails
	 */
	public static void addTask(VideoTaskDetails taskDetails) {
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
			case BACKEND_FEEDBACK:
				if(!VideoList.isValid(taskDetails.getVideoList())){
					throw new IllegalArgumentException("Invalid "+service.tut.pori.contentanalysis.video.Definitions.ELEMENT_VIDEOLIST);
				}
				addTaskAsyncCallback(taskDetails, null);
				break;
			case ANALYSIS:
				VideoList videoList = taskDetails.getVideoList();
				if(!VideoList.isValid(videoList)){
					throw new IllegalArgumentException("Invalid "+service.tut.pori.contentanalysis.video.Definitions.ELEMENT_VIDEOLIST);
				}
				if(taskDetails.getDeletedVideoList() != null){
					throw new IllegalArgumentException(service.tut.pori.contentanalysis.video.Definitions.ELEMENT_DELETED_VIDEOLIST+" cannot appear in a task of type: "+TaskType.ANALYSIS.name());
				}
				VideoParameters vp = taskDetails.getTaskParameters();
				for(Video video : videoList.getVideos()){
					MediaObjectList mediaObjects = CREATOR.createMediaObjectList((vp == null ? null : vp.getAnalysisTypes()), DATAGROUPS_BACKEND_RESPONSE, DEFAULT_LIMITS, null);
					for(service.tut.pori.contentanalysis.MediaObject o : mediaObjects.getMediaObjects()){
						o.setOwnerUserId(null);
						o.setBackendId(tBackendId);
						o.setMediaObjectId(null);
						o.setServiceType(null);
					}
					video.addackendStatus(new BackendStatus(new AnalysisBackend(tBackendId), TaskStatus.COMPLETED));
					video.addMediaObjects(mediaObjects);
				}
				addTaskAsyncCallback(taskDetails, videoList);
				break;
			case FEEDBACK:
				if(taskDetails.getVideoList() != null && !VideoList.isValid(taskDetails.getVideoList())){
					throw new IllegalArgumentException("Invalid "+service.tut.pori.contentanalysis.video.Definitions.ELEMENT_VIDEOLIST);
				}else if(taskDetails.getDeletedVideoList() == null){
					throw new IllegalArgumentException(Definitions.ELEMENT_TASK_DETAILS+" requires at least one of "+service.tut.pori.contentanalysis.video.Definitions.ELEMENT_VIDEOLIST+" or "+service.tut.pori.contentanalysis.video.Definitions.ELEMENT_DELETED_VIDEOLIST);
				}else if(!DeletedVideoList.isValid(taskDetails.getDeletedVideoList())){
					throw new IllegalArgumentException("Invalid "+service.tut.pori.contentanalysis.video.Definitions.ELEMENT_DELETED_VIDEOLIST);
				}
				
				addTaskAsyncCallback(taskDetails, null);
				break;
			default:
				throw new IllegalArgumentException("Tasks of type: "+type.name()+" are not supported by this validator.");
		}
	}
	
	/**
	 * 
	 * @param taskId
	 * @param dataGroups
	 * @param limits
	 * @return an example response for the given values
	 */
	public static Response queryTaskStatus(Long taskId, DataGroups dataGroups, Limits limits) {
		if(limits.getMaxItems() >= Limits.DEFAULT_MAX_ITEMS){
			LOGGER.debug("Reseting limits: Default max items was "+Limits.DEFAULT_MAX_ITEMS);
			limits = DEFAULT_LIMITS; // startItem makes no difference for random
		}
		return new Response(CREATOR.createTaskResponse(null, dataGroups, limits, taskId, TaskType.ANALYSIS));
	}

	/**
	 * Call asynchronously the callback given in the details, returning an example task response
	 * 
	 * @param details
	 * @param videoList
	 * @throws UnsupportedOperationException on unsupported task details
	 * @see service.tut.pori.contentanalysis.video.VideoTaskResponse
	 */
	public static void addTaskAsyncCallback(VideoTaskDetails details, VideoList videoList) throws UnsupportedOperationException{
		TaskType type = details.getTaskType();
		if(!TaskType.ANALYSIS.equals(type)){
			throw new UnsupportedOperationException("Unsupported task type: "+type);
		}
		HttpPost post = new HttpPost(details.getCallbackUri());
		VideoTaskResponse r = new VideoTaskResponse();
		r.setBackendId(details.getBackendId());
		r.setTaskId(details.getTaskId());
		r.setStatus(TaskStatus.COMPLETED);
		r.setTaskType(details.getTaskType());
		r.setVideoList(videoList);
		post.setEntity(new StringEntity((new XMLFormatter()).toString(r), core.tut.pori.http.Definitions.ENCODING_UTF8));
		CAReferenceCore.executeAsyncCallback(post);
	}

	/**
	 * Client API variation of search by GUID
	 * 
	 * @param authenticatedUser
	 * @param analysisTypes 
	 * @param guid
	 * @param dataGroups
	 * @param limits
	 * @param serviceTypes
	 * @param userIdFilters
	 * @return an example response for the given values
	 */
	public static Response searchSimilarById(UserIdentity authenticatedUser, EnumSet<AnalysisType> analysisTypes, String guid, DataGroups dataGroups, Limits limits, EnumSet<ServiceType> serviceTypes, long[] userIdFilters) {
		LOGGER.info((authenticatedUser == null ? "No logged in user." : "Ignoring the logged in user, id: "+authenticatedUser.getUserId()));	// only notify of the logged in status
		return searchSimilarById(analysisTypes, guid, dataGroups, limits, serviceTypes, userIdFilters);	// we can directly call the back-end API reference implementation
	}
	
	/**
	 * Back-end API variation of search by GUID
	 * 
	 * @param analysisTypes 
	 * @param serviceTypes
	 * @param guid
	 * @param userIds
	 * @param dataGroups
	 * @param limits
	 * @return an example response for the given values
	 */
	public static Response searchSimilarById(EnumSet<AnalysisType> analysisTypes, String guid, DataGroups dataGroups, Limits limits, EnumSet<ServiceType> serviceTypes, long[] userIds) {
		if(limits.getMaxItems() >= Limits.DEFAULT_MAX_ITEMS){
			LOGGER.debug("Reseting limits: Default max items was "+Limits.DEFAULT_MAX_ITEMS);
			limits = DEFAULT_LIMITS; // startItem makes no difference for random
		}
		return new Response(CREATOR.createSearchResults(guid, dataGroups, limits, serviceTypes, userIds, null));
	}
}
