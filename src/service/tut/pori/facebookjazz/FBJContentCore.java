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
package service.tut.pori.facebookjazz;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.context.ApplicationListener;

import service.tut.pori.contentanalysis.AsyncTask;
import service.tut.pori.contentanalysis.AnalysisBackend.Capability;
import service.tut.pori.contentanalysis.AsyncTask.TaskStatus;
import service.tut.pori.contentanalysis.AsyncTask.TaskType;
import service.tut.pori.contentanalysis.CAContentCore;
import service.tut.pori.contentanalysis.CAContentCore.ServiceType;
import service.tut.pori.contentanalysis.AnalysisBackend;
import service.tut.pori.contentanalysis.BackendDAO;
import service.tut.pori.contentanalysis.BackendStatusList;
import service.tut.pori.contentanalysis.MediaObject;
import service.tut.pori.contentanalysis.MediaObjectDAO;
import service.tut.pori.contentanalysis.MediaObjectList;
import service.tut.pori.contentanalysis.PhotoDAO;
import service.tut.pori.users.facebook.FacebookUserCore;
import core.tut.pori.context.ServiceInitializer;
import core.tut.pori.http.parameters.DataGroups;
import core.tut.pori.http.parameters.Limits;
import core.tut.pori.http.parameters.SortOptions;
import core.tut.pori.users.UserEvent;
import core.tut.pori.users.UserEvent.EventType;
import core.tut.pori.users.UserIdentity;
import core.tut.pori.utils.MediaUrlValidator.MediaType;


/**
 * FacebookJazz core methods.
 */
public final class FBJContentCore {
	/** default capabilities for Facebook tasks */
	public static final EnumSet<Capability> DEFAULT_CAPABILITIES = EnumSet.of(Capability.FACEBOOK_SUMMARIZATION, Capability.PHOTO_ANALYSIS, Capability.BACKEND_FEEDBACK);
	private static final DataGroups DATA_GROUP_ALL = new DataGroups(DataGroups.DATA_GROUP_ALL);
	private static final String JOB_KEY_USER_ID = "userId";
	private static final Logger LOGGER = Logger.getLogger(FBJContentCore.class);
	private static final EnumSet<MediaType> MEDIA_TYPES_FBJ = EnumSet.allOf(MediaType.class);
	private static final EnumSet<ServiceType> SERVICE_TYPES_FBJ = EnumSet.of(ServiceType.FACEBOOK_JAZZ);

	/**
	 * 
	 */
	private FBJContentCore(){
		// nothing needed
	}

	/**
	 * 
	 * @param authenticatedUser
	 * @param dataGroups
	 * @param limits
	 * @param sortOptions
	 * @return list of media objects or null if none was found
	 */
	public static MediaObjectList retrieveTagsForUser(UserIdentity authenticatedUser, DataGroups dataGroups, Limits limits, SortOptions sortOptions) {
		return ServiceInitializer.getDAOHandler().getSolrDAO(MediaObjectDAO.class).search(authenticatedUser, dataGroups, limits, MEDIA_TYPES_FBJ, SERVICE_TYPES_FBJ, sortOptions, null, null);
	}

	/**
	 * Parses the ranks into a {@link service.tut.pori.contentanalysis.MediaObjectList}. Non-existing media object ids will be ignored.
	 * 
	 * @param ranks list of rank strings with {@value core.tut.pori.http.Definitions#SEPARATOR_URI_QUERY_TYPE_VALUE} as separator between media object id and rank value
	 * @return null if ranks is null or did not contain valid ranks
	 * @throws IllegalArgumentException on invalid rank string
	 */
	public static MediaObjectList parseRankStrings(List<String> ranks) throws IllegalArgumentException {
		if(ranks == null || ranks.isEmpty()){
			LOGGER.debug("No ranks given.");
			return null;
		}

		HashMap<String, Integer> rankMap = new HashMap<>(ranks.size()); // mediaObjectId, rank map

		for(String r : ranks){
			String[] parts = r.split(core.tut.pori.http.Definitions.SEPARATOR_URI_QUERY_TYPE_VALUE);
			if(parts.length != 2){
				throw new IllegalArgumentException("Failed to process rank parameter: "+r);
			}

			rankMap.put(parts[0], Integer.valueOf(parts[1])); // let it throw
		}  // for

		if(rankMap.isEmpty()){
			LOGGER.debug("No ranks found.");
			return null;
		}

		MediaObjectDAO vdao = ServiceInitializer.getDAOHandler().getSolrDAO(MediaObjectDAO.class);
		MediaObjectList objects = vdao.getMediaObjects(DATA_GROUP_ALL, null, EnumSet.allOf(MediaType.class), null, rankMap.keySet(), null); 
		if(MediaObjectList.isEmpty(objects)){
			LOGGER.debug("No objects found.");
			return null;
		}

		for(Iterator<Entry<String, Integer>> iter = rankMap.entrySet().iterator(); iter.hasNext();){ // update ranks, remove non-existing
			Entry<String, Integer> e = iter.next();
			String mediaObjectId = e .getKey();
			MediaObject object = objects.getMediaObject(mediaObjectId);
			if(object == null){
				LOGGER.debug("Ignored non-existing media object, id: "+mediaObjectId);
				iter.remove();
			}else{
				object.setRank(e.getValue());
			}
		} // for

		return (MediaObjectList.isEmpty(objects) ? null : objects);
	}

	/**
	 * 
	 * @param authenticatedUser
	 * @param rankedObjects
	 * @throws NumberFormatException
	 * @throws IllegalArgumentException
	 */
	public static void setRanks(UserIdentity authenticatedUser, MediaObjectList rankedObjects) throws NumberFormatException, IllegalArgumentException{
		if(!MediaObjectList.isValid(rankedObjects)){
			throw new IllegalArgumentException("Invalid media object list.");
		}

		if(!UserIdentity.isValid(authenticatedUser)){
			LOGGER.warn("Invalid user.");
			return;
		}

		List<MediaObject> objects = rankedObjects.getMediaObjects();
		HashSet<String> voids = new HashSet<>(objects.size());
		for(MediaObject object : objects){
			String mediaObjectId = object.getMediaObjectId();
			if(!UserIdentity.equals(authenticatedUser, object.getOwnerUserId())){
				LOGGER.debug("User ids do not match for media object id: "+mediaObjectId+", user, id: "+authenticatedUser.getUserId());
				throw new IllegalArgumentException("Bad media object id.");
			}
			voids.add(mediaObjectId);
		}

		if(!ServiceInitializer.getDAOHandler().getSolrDAO(PhotoDAO.class).update(rankedObjects)){
			LOGGER.warn("Failed to update media objects.");
		}

		FBFeedbackTaskDetails details = new FBFeedbackTaskDetails();
		details.setUserId(authenticatedUser);
		details.setTags(rankedObjects);
		scheduleTask(details);
	}

	/**
	 * 
	 * @param details
	 * @return the task id of the generated task or null on failure
	 * @throws UnsupportedOperationException on unsupported task type
	 * @throws IllegalArgumentException on failed schedule
	 */
	public static Long scheduleTask(FBFeedbackTaskDetails details) throws UnsupportedOperationException {
		if(details.getTaskType() != TaskType.FACEBOOK_PROFILE_SUMMARIZATION_FEEDBACK){
			throw new UnsupportedOperationException("Unsupported TaskType: "+details.getTaskType().name());
		}
		Long taskId = ServiceInitializer.getDAOHandler().getSQLDAO(FBTaskDAO.class).insertTask(details);
		if(taskId == null){
			LOGGER.error("Task schedule failed: failed to insert new task.");
			return null;
		}
		
		JobDataMap data = new JobDataMap();
		AsyncTask.setTaskId(data, taskId);		
		LOGGER.debug("Scheduling task, id: "+taskId);
		JobBuilder builder = JobBuilder.newJob(FBSummarizationFeedbackTask.class);
		builder.setJobData(data);
		if(CAContentCore.schedule(builder)){
			return taskId;
		}else{
			LOGGER.warn("Failed to schedule task, id: "+taskId);
			return null;
		}
	}
	
	/**
	 * Create and schedule facebook summarization task with the given details. If details have taskId given, the task will not be re-added, and will simply be (re-)scheduled.
	 * 
	 * If the details contains no back-ends, default back-ends will be added. See {@link #DEFAULT_CAPABILITIES}
	 *  
	 * @param details details of the task, if profile object is given, it will be ignored.
	 * @return the id of the generated task or null on failure
	 */
	public static Long summarize(FBSummarizationTaskDetails details) {
		Long taskId = details.getTaskId();
		if(taskId != null){
			LOGGER.debug("Task id was given, will not add task.");
		}else{
			BackendStatusList backends = details.getBackends();
			if(BackendStatusList.isEmpty(backends)){
				LOGGER.debug("No back-ends given, using defaults...");
				List<AnalysisBackend> ends = ServiceInitializer.getDAOHandler().getSQLDAO(BackendDAO.class).getBackends(DEFAULT_CAPABILITIES);
				if(ends == null){
					LOGGER.warn("Aborting task, no capable back-ends.");
					return null;
				}
				
				backends = new BackendStatusList();
				backends.setBackendStatus(ends, TaskStatus.NOT_STARTED);
				details.setBackends(backends);
			}
			
			taskId = ServiceInitializer.getDAOHandler().getSQLDAO(FBTaskDAO.class).insertTask(details);
			if(taskId == null){
				LOGGER.error("Task schedule failed: failed to insert new task.");
				return null;
			}
		}
		
		JobDataMap data = new JobDataMap();
		FBSummarizationTask.setTaskId(data, taskId);
		LOGGER.debug("Scheduling task, id: "+taskId);
		JobBuilder builder = JobBuilder.newJob(FBSummarizationTask.class);
		builder.setJobData(data);
		if(CAContentCore.schedule(builder)){
			return taskId;
		}else{
			LOGGER.warn("Failed to schedule task, id: "+taskId);
			return null;
		}
	}

	/**
	 * 
	 * @param authenticatedUser
	 * @param userId
	 * @return list of weight modifiers or null if none was found or permission was denied
	 */
	public static WeightModifierList retrieveTagWeights(UserIdentity authenticatedUser, Long userId) {
		if(userId != null){
			if(!UserIdentity.equals(authenticatedUser, userId)){
				LOGGER.warn("Permission was denied for tag weights of user, id: "+userId);
				return null;
			}else{
				return ServiceInitializer.getDAOHandler().getSQLDAO(FacebookJazzDAO.class).getWeightModifiers(new UserIdentity(userId));
			}
		}else{ // no filter, return defaults
			return ServiceInitializer.getDAOHandler().getSQLDAO(FacebookJazzDAO.class).getWeightModifiers(null);
		}
	}

	/**
	 * 
	 * @param userIdentity
	 * @param weightModifierList
	 */
	public static void setTagWeights(UserIdentity userIdentity, WeightModifierList weightModifierList) {
		if(!WeightModifierList.isValid(weightModifierList)){
			throw new IllegalArgumentException("Invalid "+Definitions.ELEMENT_WEIGHT_MODIFIER_LIST+".");
		}
		ServiceInitializer.getDAOHandler().getSQLDAO(FacebookJazzDAO.class).setWeightModifiers(userIdentity, weightModifierList);
	}

	/**
	 * 
	 * @param response
	 * @throws IllegalArgumentException
	 */
	public static void taskFinished(FBTaskResponse response) throws IllegalArgumentException{
		CAContentCore.validateTaskResponse(response);

		LOGGER.debug("TaskId: "+response.getTaskId()+", backendId: "+response.getBackendId());

		switch(response.getTaskType()){
			case FACEBOOK_PROFILE_SUMMARIZATION:
				FBSummarizationTask.taskFinished(response);
				break;
			case FACEBOOK_PROFILE_SUMMARIZATION_FEEDBACK:
				FBSummarizationFeedbackTask.taskFinished(response);
				break;
			default:
				throw new IllegalArgumentException("Unsupported "+service.tut.pori.contentanalysis.Definitions.ELEMENT_TASK_TYPE+": "+response.getTaskType().name());
		}
	}
	
	/**
	 * Listener for user related events.
	 *
	 * Automatically instantiated by Spring as a bean.
	 */
	@SuppressWarnings("unused")
	private static class UserEventListener implements ApplicationListener<UserEvent>{

		@Override
		public void onApplicationEvent(UserEvent event) {
			if(event.getType() == EventType.USER_AUTHORIZATION_REVOKED && event.getSource().equals(FacebookUserCore.class)){
				Long userId = event.getUserId().getUserId();
				LOGGER.debug("Detected event of type "+EventType.USER_AUTHORIZATION_REVOKED.name()+", scheduling removal of weight modifiers and tags for user, id: "+userId);

				JobDataMap data = new JobDataMap();
				data.put(JOB_KEY_USER_ID, userId);
				JobBuilder builder = JobBuilder.newJob(WeightModifierRemovalJob.class);
				builder.setJobData(data);
				CAContentCore.schedule(builder);
				
				builder = JobBuilder.newJob(TagRemovalJob.class);
				builder.setJobData(data);
				CAContentCore.schedule(builder);
			}
		}
	} // class UserEventListener
	
	/**
	 * A job for removing all tags of a single user, generated by the Facebook Jazz service.
	 *
	 */
	public static class TagRemovalJob implements Job{

		@Override
		public void execute(JobExecutionContext context) throws JobExecutionException {
			JobDataMap data = context.getMergedJobDataMap();
			Long userId = data.getLong(JOB_KEY_USER_ID);
			LOGGER.debug("Removing all content for user, id: "+userId);
			MediaObjectDAO vDAO = ServiceInitializer.getDAOHandler().getSolrDAO(MediaObjectDAO.class);
			MediaObjectList mediaObjects = vDAO.search(new UserIdentity(userId), null, null, MEDIA_TYPES_FBJ, SERVICE_TYPES_FBJ, null , new long[]{userId}, null);
			if(MediaObjectList.isEmpty(mediaObjects)){
				LOGGER.debug("No media objects for user, id: "+userId);
				return;
			}
			List<String> remove = mediaObjects.getMediaObjectIds();
			if(!vDAO.remove(remove)){
				LOGGER.debug("Failed to remove objects for user, id: "+userId);
			}
		}
	} // class TagRemovalJob

	/**
	 * Job for removing content for the user designated by data key JOB_KEY_USER_ID for services designated by data key JOB_KEY_SERVICE_TYPES
	 *
	 */
	public static class WeightModifierRemovalJob implements Job{

		@Override
		public void execute(JobExecutionContext context) throws JobExecutionException {
			JobDataMap data = context.getMergedJobDataMap();
			Long userId = data.getLong(JOB_KEY_USER_ID);
			LOGGER.debug("Removing all weight modifiers for user, id: "+userId);
			ServiceInitializer.getDAOHandler().getSQLDAO(FacebookJazzDAO.class).removeWeightModifers(new UserIdentity(userId));
		}
	} // class MetadataRemovalJob
}
