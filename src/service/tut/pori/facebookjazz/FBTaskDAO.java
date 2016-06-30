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
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import service.tut.pori.contentanalysis.AbstractTaskDetails;
import service.tut.pori.contentanalysis.AnalysisBackend;
import service.tut.pori.contentanalysis.AsyncTask.TaskType;
import service.tut.pori.contentanalysis.BackendDAO;
import service.tut.pori.contentanalysis.Definitions;
import service.tut.pori.contentanalysis.MediaObjectDAO;
import service.tut.pori.contentanalysis.MediaTaskDAO;
import core.tut.pori.context.ServiceInitializer;
import core.tut.pori.http.parameters.DataGroups;
import core.tut.pori.http.parameters.Limits;
import core.tut.pori.users.UserIdentity;
import core.tut.pori.utils.MediaUrlValidator.MediaType;

/**
 * DAO for saving and retrieving FacebookJazz tasks.
 */
public class FBTaskDAO extends MediaTaskDAO {
	private static final Logger LOGGER = Logger.getLogger(FBTaskDAO.class);
	private static final EnumSet<MediaType> MEDIA_TYPES = EnumSet.allOf(MediaType.class);
	@Autowired
	private BackendDAO _backendDAO = null;
	
	/**
	 * 
	 * @param details
	 * @return created row id or null on failure
	 * @throws UnsupportedOperationException on unsupported task type
	 * @throws IllegalArgumentException on bad task content
	 */
	public Long insertTask(FBFeedbackTaskDetails details) throws UnsupportedOperationException, IllegalArgumentException {
		TaskType type = details.getTaskType();
		if(type != TaskType.FACEBOOK_PROFILE_SUMMARIZATION_FEEDBACK){
			throw new UnsupportedOperationException("TaskType not supported.");
		}
		
		Long taskId = insertTask((AbstractTaskDetails)details);
		if(taskId == null){
			throw new IllegalArgumentException("Failed to add new task.");
		}
		
		insertTaskMediaObjects(null, taskId, details.getTags());
		return taskId;
	}
	
	/**
	 * 
	 * @param details
	 * @return created row id or null on failure
	 * @throws UnsupportedOperationException on unsupported task type
	 * @throws IllegalArgumentException on bad task content
	 */
	public Long insertTask(FBSummarizationTaskDetails details) throws UnsupportedOperationException, IllegalArgumentException {
		TaskType type = details.getTaskType();
		if(type != TaskType.FACEBOOK_PROFILE_SUMMARIZATION){
			throw new UnsupportedOperationException("TaskType not supported.");
		}
		
		Long taskId = insertTask((AbstractTaskDetails)details);
		if(taskId == null){
			throw new IllegalArgumentException("Failed to add new task.");
		}
		return taskId;
	}
	
	@Override
	public AbstractTaskDetails getTask(Integer backendId, DataGroups dataGroups, Limits limits, Long taskId) throws IllegalArgumentException, UnsupportedOperationException {
		Pair<TaskType, UserIdentity> type = getTaskType(backendId, taskId);
		if(type == null){
			LOGGER.warn("Failed to resolve task type.");
			return null;
		}
		
		switch(type.getLeft()){
			case FACEBOOK_PROFILE_SUMMARIZATION:
				return getSummarizationTask(dataGroups, taskId, type.getRight());
			case FACEBOOK_PROFILE_SUMMARIZATION_FEEDBACK:
				return getFeedbackTask(backendId, dataGroups, limits, taskId, type.getRight());
			default:
				throw new UnsupportedOperationException(("Using super: Unsupported task type: "+type.getLeft().name()));
		}
	}
	
	/**
	 * 
	 * @param backendId
	 * @param dataGroups
	 * @param limits optional limits filter
	 * @param taskId
	 * @param userId
	 * @return the feedback task or null if not found
	 * @throws IllegalArgumentException on bad values
	 * @throws UnsupportedOperationException on unsupported task type (not a feedback task)
	 */
	private FBFeedbackTaskDetails getFeedbackTask(Integer backendId, DataGroups dataGroups, Limits limits, Long taskId, UserIdentity userId){
		if(DataGroups.isEmpty(dataGroups)){
			LOGGER.debug("No datagroups given, retrieving default data groups.");
			AnalysisBackend backend = _backendDAO.getBackend(backendId);
			if(backend == null){
				throw new IllegalArgumentException("Backend, id: "+backendId+" does not exist.");
			}
			dataGroups = backend.getDefaultTaskDataGroups();
		}
		
		FBFeedbackTaskDetails details = new FBFeedbackTaskDetails();
		details.setBackendId(backendId);
		details.setTaskId(taskId);
		details.setTaskType(TaskType.FACEBOOK_PROFILE_SUMMARIZATION_FEEDBACK);
		details.setUserId(userId);
		
		getTaskMetadata(details);
		
		Map<String, List<String>> map = getMediaObjectIds(limits, taskId);
		if(map == null){
			LOGGER.warn("No valid media objects for task, id: "+taskId);
			return null;
		}
		
		if(DataGroups.hasDataGroup(Definitions.DATA_GROUP_BACKEND_STATUS, dataGroups)){
			LOGGER.debug("Retrieving backend status list.");
			getBackendStatusList(details);
		}
		
		details.setTags(ServiceInitializer.getDAOHandler().getDAO(MediaObjectDAO.class).getMediaObjects(dataGroups, null, MEDIA_TYPES, null, map.get(null), null));	
		if(FBFeedbackTaskDetails.isEmpty(details)){
			LOGGER.warn("Task, id: "+taskId+" has no content.");
			return null;
		}else{
			return details;
		}
	}
	
	/**
	 * 
	 * @param dataGroups
	 * @param taskId
	 * @param userIdentity 
	 * @return the summarization task
	 */
	private FBSummarizationTaskDetails getSummarizationTask(DataGroups dataGroups, Long taskId, UserIdentity userIdentity) {
		FBSummarizationTaskDetails details = new FBSummarizationTaskDetails();
		details.setTaskId(taskId);
		details.setTaskType(TaskType.FACEBOOK_PROFILE_SUMMARIZATION);
		details.setUserId(userIdentity);
		
		getTaskMetadata(details);
		
		if(DataGroups.hasDataGroup(Definitions.DATA_GROUP_BACKEND_STATUS, dataGroups)){
			LOGGER.debug("Retrieving backend status list.");
			getBackendStatusList(details);
		}
		
		return details;
	}
}
