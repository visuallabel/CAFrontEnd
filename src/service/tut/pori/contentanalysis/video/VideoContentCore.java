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
package service.tut.pori.contentanalysis.video;

import java.util.EnumSet;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.quartz.JobBuilder;

import service.tut.pori.contentanalysis.AccessDetails;
import service.tut.pori.contentanalysis.AccessDetails.Permission;
import service.tut.pori.contentanalysis.AnalysisBackend;
import service.tut.pori.contentanalysis.AnalysisBackend.Capability;
import service.tut.pori.contentanalysis.AsyncTask.TaskStatus;
import service.tut.pori.contentanalysis.AsyncTask.TaskType;
import service.tut.pori.contentanalysis.BackendDAO;
import service.tut.pori.contentanalysis.BackendStatus;
import service.tut.pori.contentanalysis.BackendStatusList;
import service.tut.pori.contentanalysis.CAContentCore;
import service.tut.pori.contentanalysis.PhotoTaskDAO;
import service.tut.pori.contentanalysis.CAContentCore.ServiceType;
import service.tut.pori.contentstorage.ContentStorageCore;
import core.tut.pori.context.ServiceInitializer;
import core.tut.pori.http.RedirectResponse;
import core.tut.pori.http.parameters.DataGroups;
import core.tut.pori.http.parameters.Limits;
import core.tut.pori.users.UserIdentity;

/**
 * Video content analysis core methods.
 */
public final class VideoContentCore {
	private static final Logger LOGGER = Logger.getLogger(VideoContentCore.class);
	
	/**
	 * 
	 */
	private VideoContentCore(){
		// nothing needed
	}

	/**
	 * 
	 * @param guid
	 * @param type
	 * @return redirection URL for the given GUID and type or null if either one the given values was null
	 */
	public static String generateRedirectUrl(String guid, ServiceType type){
		if(type == null || StringUtils.isBlank(guid)){
			LOGGER.error("GUID or service type was null.");
			return null;
		}
		return ServiceInitializer.getPropertyHandler().getRESTBindContext()+Definitions.SERVICE_VCA+"/"+service.tut.pori.contentanalysis.Definitions.METHOD_REDIRECT+"?"+service.tut.pori.contentanalysis.Definitions.PARAMETER_GUID+"="+guid+"&"+service.tut.pori.contentanalysis.Definitions.PARAMETER_SERVICE_ID+"="+type.getServiceId();
	}

	/**
	 * resolves dynamic /rest/r? redirection URL to static access URL
	 * 
	 * @param authenticatedUser
	 * @param serviceType
	 * @param guid
	 * @return redirection to static URL referenced by the given parameters
	 */
	public static RedirectResponse generateTargetUrl(UserIdentity authenticatedUser, ServiceType serviceType, String guid) {		
		AccessDetails details = ServiceInitializer.getDAOHandler().getSolrDAO(VideoDAO.class).getAccessDetails(authenticatedUser, guid);
		if(details == null){
			throw new IllegalArgumentException("Not Found.");
		}
		Permission access = details.getPermission();
		if(access == Permission.NO_ACCESS){
			LOGGER.debug("Access denied for GUID: "+guid+" for userId: "+(UserIdentity.isValid(authenticatedUser) ? authenticatedUser.getUserId() : "none"));
			throw new IllegalArgumentException("Not Found.");
		}
		LOGGER.debug("Granting access with "+Permission.class.toString()+" : "+access.name());
		
		String url = ContentStorageCore.getContentStorage(false, serviceType).getTargetUrl(details);
		if(url == null){
			throw new IllegalArgumentException("Not Found.");
		}else{
			return new RedirectResponse(url);
		}
	}

	/**
	 * 
	 * This method is called by back-ends to retrieve a list of videos to be analyzed.
	 * To query tasks status from back-end use queryTaskStatus.
	 * 
	 * @param backendId
	 * @param taskId
	 * @param dataGroups
	 * @param limits
	 * @return the task or null if not found
	 */
	public static VideoTaskDetails queryTaskDetails(Integer backendId, Long taskId, DataGroups dataGroups, Limits limits) {
		return ServiceInitializer.getDAOHandler().getSQLDAO(VideoTaskDAO.class).getTask(backendId, dataGroups, limits, taskId);
	}

	/**
	 * 
	 * @param response
	 * @throws IllegalArgumentException
	 */
	public static void taskFinished(VideoTaskResponse response) throws IllegalArgumentException{
		CAContentCore.validateTaskResponse(response);

		LOGGER.debug("TaskId: "+response.getTaskId()+", backendId: "+response.getBackendId());

		switch(response.getTaskType()){
			case BACKEND_FEEDBACK:
				LOGGER.debug("Using "+VideoFeedbackTask.class.toString()+" for task of type "+TaskType.BACKEND_FEEDBACK);
			case FEEDBACK:
				VideoFeedbackTask.taskFinished(response);
				break;
			case ANALYSIS:
				VideoAnalysisTask.taskFinished(response);
				break;
			default:
				throw new IllegalArgumentException("Unsupported "+service.tut.pori.contentanalysis.Definitions.ELEMENT_TASK_TYPE);
		}
	}

	/**
	 * 
	 * @param authenticatedUser
	 * @param guids
	 * @param dataGroups
	 * @param limits
	 * @param serviceTypes
	 * @param userIdFilters
	 * @return list of videos or null if none was found with the given parameters
	 */
	public static VideoList getVideos(UserIdentity authenticatedUser, List<String> guids, DataGroups dataGroups, Limits limits, EnumSet<ServiceType> serviceTypes, long[] userIdFilters) {
		return ServiceInitializer.getDAOHandler().getSolrDAO(VideoDAO.class).search(authenticatedUser, dataGroups, guids, limits, null, serviceTypes, userIdFilters);
	}
	
	/**
	 * Note: if the details already contain a taskId, the task will NOT be re-added to the database, but simply re-scheduled.
	 * 
	 * @param details
	 * @return task id of the generated task, null if task could not be created
	 */
	public static Long scheduleTask(VideoTaskDetails details) {
		JobBuilder builder = getBuilder(details.getTaskType());
		Long taskId = details.getTaskId();
		if(taskId != null){
			LOGGER.debug("Task id already present for task, id: "+taskId);
		}else{
			taskId = ServiceInitializer.getDAOHandler().getSQLDAO(VideoTaskDAO.class).insertTask(details);
			if(taskId == null){
				LOGGER.error("Task schedule failed: failed to insert new video task.");
				return null;
			}
		}

		if(CAContentCore.scheduleTask(builder, taskId)){
			return taskId;
		}else{
			LOGGER.error("Failed to schedule new task.");
			return null;
		}
	}
	
	/**
	 * 
	 * @param type
	 * @return new builder for the given type
	 * @throws UnsupportedOperationException on unsupported type
	 * @throws IllegalArgumentException on bad type
	 */
	private static JobBuilder getBuilder(TaskType type) throws UnsupportedOperationException, IllegalArgumentException{
		if(type == null){
			throw new IllegalArgumentException("Null type.");
		}
		switch (type) {
			case ANALYSIS:
				return JobBuilder.newJob(VideoAnalysisTask.class);
			default:
				throw new UnsupportedOperationException("Unsupported TaskType: "+type.name());
		}
	}

	/**
	 * Create and schedule the task for all capable back-ends included in the task designated by the task Id. The given back-end Id will not participate in the feedback task.
	 * 
	 * @param backendId the back-end that send the task finished call, this back-end is automatically omitted from the list of target back-ends
	 * @param videos videos returned in task finished call
	 * @param taskId the id of the finished analysis task
	 */
	public static void scheduleBackendFeedback(Integer backendId, VideoList videos, Long taskId) {
		if(VideoList.isEmpty(videos)){
			LOGGER.debug("Not scheduling back-end feedback: empty photo list.");
			return;
		}
		
		BackendStatusList tBackends = ServiceInitializer.getDAOHandler().getSQLDAO(PhotoTaskDAO.class).getBackendStatus(taskId, null);
		if(BackendStatusList.isEmpty(tBackends)){
			LOGGER.warn("No back-ends for the given task, or the task does not exist. Task id: "+taskId);
			return;
		}
		
		List<AnalysisBackend> backends = ServiceInitializer.getDAOHandler().getSQLDAO(BackendDAO.class).getBackends(Capability.BACKEND_FEEDBACK); // get list of back-ends with compatible capabilities
		if(backends == null){
			LOGGER.debug("No capable back-ends for back-end feedback.");
			return;
		}

		BackendStatusList statuses = new BackendStatusList();
		for(AnalysisBackend backend : backends){
			Integer id = backend.getBackendId();
			if(id.equals(backendId)){ // ignore the given back-end id
				LOGGER.debug("Ignoring the back-end id of task results, back-end id: "+backendId+", task, id: "+taskId);
			}else if(tBackends.getBackendStatus(id) != null){ // and all back-ends not part of the task
				statuses.setBackendStatus(new BackendStatus(backend, TaskStatus.NOT_STARTED));
			}
		}
		if(BackendStatusList.isEmpty(statuses)){
			LOGGER.debug("No capable back-ends for back-end feedback.");
			return;
		}
		
		scheduleTask(
				(new VideoFeedbackTask.FeedbackTaskBuilder(TaskType.BACKEND_FEEDBACK))
				.setBackends(statuses)
				.addVideos(videos)
				.build()
			);
	}
}
