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

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.context.ApplicationListener;

import service.tut.pori.contentanalysis.AnalysisBackend;
import service.tut.pori.contentanalysis.AnalysisBackend.Capability;
import service.tut.pori.contentanalysis.AsyncTask.TaskStatus;
import service.tut.pori.contentanalysis.BackendDAO;
import service.tut.pori.contentanalysis.BackendStatusList;
import service.tut.pori.contentanalysis.CAContentCore;
import service.tut.pori.contentanalysis.CAContentCore.ServiceType;
import service.tut.pori.contentanalysis.MediaObject;
import service.tut.pori.contentanalysis.MediaObjectDAO;
import service.tut.pori.contentanalysis.MediaObjectList;
import service.tut.pori.contentanalysis.PhotoDAO;
import service.tut.pori.twitterjazz.TwitterExtractor.ContentType;
import service.tut.pori.users.twitter.TwitterUserCore;
import core.tut.pori.context.ServiceInitializer;
import core.tut.pori.http.parameters.DataGroups;
import core.tut.pori.http.parameters.Limits;
import core.tut.pori.http.parameters.SortOptions;
import core.tut.pori.users.UserEvent;
import core.tut.pori.users.UserEvent.EventType;
import core.tut.pori.users.UserIdentity;
import core.tut.pori.utils.MediaUrlValidator.MediaType;

/**
 * TwitterJazz core methods.
 * 
 */
public final class TJContentCore {
	/** default capabilities for Twitter tasks */
	public static final EnumSet<Capability> DEFAULT_CAPABILITIES = EnumSet.of(Capability.TWITTER_SUMMARIZATION, Capability.PHOTO_ANALYSIS, Capability.BACKEND_FEEDBACK);
	private static final Logger LOGGER = Logger.getLogger(TJContentCore.class);
	private static final String JOB_KEY_USER_ID = "userId";
	private static final EnumSet<MediaType> MEDIA_TYPES_TJ = EnumSet.allOf(MediaType.class);
	private static final EnumSet<ServiceType> SERVICE_TYPES_TJ = EnumSet.of(ServiceType.TWITTER_JAZZ);
	
	/**
	 * 
	 */
	private TJContentCore(){
		// nothing needed
	}
	
	/**
	 * 
	 * @param response
	 * @throws IllegalArgumentException
	 */
	public static void taskFinished(TwitterTaskResponse response) throws IllegalArgumentException {
		CAContentCore.validateTaskResponse(response);

		LOGGER.debug("TaskId: "+response.getTaskId()+", backendId: "+response.getBackendId());
		
		switch(response.getTaskType()){
			case TWITTER_PROFILE_SUMMARIZATION:
				TwitterSummarizationTask.taskFinished(response);
				break;
			default:
				throw new IllegalArgumentException("Unsupported "+service.tut.pori.contentanalysis.Definitions.ELEMENT_TASK_TYPE+": "+response.getTaskType().name());
		}
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
		return ServiceInitializer.getDAOHandler().getSolrDAO(MediaObjectDAO.class).search(authenticatedUser, dataGroups, limits, MEDIA_TYPES_TJ, SERVICE_TYPES_TJ, sortOptions, null, null);
	}
	
	/**
	 * Create and schedule twitter summarization task with the given details. If details have taskId given, the task will not be re-added, and will simply be (re-)scheduled.
	 * 
	 * If the details contains no back-ends, default back-ends will be added. See {@link #DEFAULT_CAPABILITIES}
	 * 
	 * @param details details of the task, if profile object is given, it will be ignored.
	 * @return the id of the generated task or null on failure
	 */
	public static Long summarize(TwitterSummarizationTaskDetails details) {
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
			
			taskId = ServiceInitializer.getDAOHandler().getSQLDAO(TwitterTaskDAO.class).insertTask(details);
			if(taskId == null){
				LOGGER.error("Task schedule failed: failed to insert new task.");
				return null;
			}
		}
		
		JobDataMap data = new JobDataMap();
		TwitterSummarizationTask.setTaskId(data, taskId);
		LOGGER.debug("Scheduling task, id: "+taskId);
		JobBuilder builder = JobBuilder.newJob(TwitterSummarizationTask.class);
		builder.setJobData(data);
		if(CAContentCore.schedule(builder)){
			return taskId;
		}else{
			LOGGER.warn("Failed to schedule task, id: "+taskId);
			return null;
		}
	}
	
	/**
	 * Create and schedule twitter summarization task(s) with the given details. A separate task is created for each of the given screen names. If no screen names are given, task is generated for the authenticated user's twitter account.
	 * 
	 * @param authenticatedUser 
	 * @param contentTypes
	 * @param screenNames
	 * @param summarize
	 * @param synchronize
	 */
	public static void summarize(UserIdentity authenticatedUser, Set<ContentType> contentTypes, Collection<String> screenNames, boolean summarize, boolean synchronize){
		if(!UserIdentity.isValid(authenticatedUser)){
			LOGGER.warn("Invalid user.");
			return;
		}
		if((!summarize && !synchronize) || (contentTypes == null || contentTypes.isEmpty())){
			LOGGER.warn("Ignored no-op task: no summarize or synchronize requested, or no content types.");
			return;
		}
		
		TwitterSummarizationTaskDetails details = new TwitterSummarizationTaskDetails();
		details.setUserId(authenticatedUser);
		details.setContentTypes(contentTypes);
		details.setSummarize(summarize);
		details.setSynchronize(synchronize);
		
		if(screenNames == null || screenNames.isEmpty()){
			LOGGER.debug("No screen names.");
			TJContentCore.summarize(details);
		}else{
			LOGGER.debug("Screen names given, generating "+screenNames.size()+" tasks.");
			for(String screenName : screenNames){
				details.setScreenName(screenName);
				TJContentCore.summarize(details);
			}
		}
	}

	/**
	 * 
	 * @param authenticatedUser
	 * @param rankedObjects
	 * @throws NumberFormatException
	 * @throws IllegalArgumentException
	 */
	public static void setRanks(UserIdentity authenticatedUser, MediaObjectList rankedObjects) {
		if(!MediaObjectList.isValid(rankedObjects)){
			throw new IllegalArgumentException("Invalid media object list.");
		}

		if(!UserIdentity.isValid(authenticatedUser)){
			LOGGER.warn("Invalid user.");
			return;
		}
		
		List<MediaObject> objects = rankedObjects.getMediaObjects();
		for(MediaObject object : objects){
			String mediaObjectId = object.getMediaObjectId();
			if(!UserIdentity.equals(authenticatedUser, object.getOwnerUserId())){
				LOGGER.debug("User ids do not match for media object id: "+mediaObjectId+", user, id: "+authenticatedUser.getUserId());
				throw new IllegalArgumentException("Bad media object id.");
			}
		}
		
		if(!ServiceInitializer.getDAOHandler().getSolrDAO(PhotoDAO.class).update(rankedObjects)){
			LOGGER.warn("Failed to update media objects.");
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
			if(event.getType() == EventType.USER_AUTHORIZATION_REVOKED && event.getSource().equals(TwitterUserCore.class)){
				Long userId = event.getUserId().getUserId();
				LOGGER.debug("Detected event of type "+EventType.USER_AUTHORIZATION_REVOKED.name()+", scheduling removal of weight modifiers for user, id: "+userId);

				JobDataMap data = new JobDataMap();
				data.put(JOB_KEY_USER_ID, userId);
				JobBuilder builder = JobBuilder.newJob(TagRemovalJob.class);
				builder.setJobData(data);
				CAContentCore.schedule(builder);
			}
		}
	} // class UserEventListener
	
	/**
	 * A job for removing all user content generated by TwitterJazz service.
	 *
	 */
	public static class TagRemovalJob implements Job{

		@Override
		public void execute(JobExecutionContext context) throws JobExecutionException {
			JobDataMap data = context.getMergedJobDataMap();
			Long userId = data.getLong(JOB_KEY_USER_ID);
			LOGGER.debug("Removing all content for user, id: "+userId);
			MediaObjectDAO vDAO = ServiceInitializer.getDAOHandler().getSolrDAO(MediaObjectDAO.class);
			MediaObjectList mediaObjects = vDAO.search(new UserIdentity(userId), null, null, MEDIA_TYPES_TJ, SERVICE_TYPES_TJ, null , new long[]{userId}, null);
			if(MediaObjectList.isEmpty(mediaObjects)){
				LOGGER.debug("User, id: "+userId+" has no media objects.");
				return;
			}
			List<String> remove = mediaObjects.getMediaObjectIds();
			if(!vDAO.remove(remove)){
				LOGGER.debug("Failed to remove objects for user, id: "+userId);
			}
		}
	} // class TagRemovalJob
}
