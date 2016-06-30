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
package service.tut.pori.twitterjazz;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import service.tut.pori.contentanalysis.AsyncTask.TaskType;
import service.tut.pori.contentanalysis.Definitions;
import service.tut.pori.contentanalysis.TaskDAO;
import core.tut.pori.http.parameters.DataGroups;
import core.tut.pori.http.parameters.Limits;
import core.tut.pori.users.UserIdentity;

/**
 * DAO for inserting and retrieving Twitter summarization and synchronization tasks.
 * 
 */
public class TwitterTaskDAO extends TaskDAO {
	private static final Logger LOGGER = Logger.getLogger(TwitterTaskDAO.class);

	@Override
	public TwitterSummarizationTaskDetails getTask(Integer backendId, DataGroups dataGroups, Limits limits, Long taskId) throws IllegalArgumentException, UnsupportedOperationException {
		Pair<TaskType, UserIdentity> type = getTaskType(backendId, taskId);
		if(type == null){
			LOGGER.warn("Failed to resolve task type.");
			return null;
		}

		switch(type.getLeft()){
			case TWITTER_PROFILE_SUMMARIZATION:
				return getSummarizationTask(dataGroups, taskId, type.getRight());
			default:
				throw new UnsupportedOperationException("Unsupported task type: "+type.getLeft().name());
		}
	}

	/**
	 * 
	 * @param dataGroups
	 * @param taskId
	 * @param userIdentity 
	 * @return task details
	 */
	private TwitterSummarizationTaskDetails getSummarizationTask(DataGroups dataGroups, Long taskId, UserIdentity userIdentity) {
		TwitterSummarizationTaskDetails details = new TwitterSummarizationTaskDetails();
		details.setTaskId(taskId);
		details.setTaskType(TaskType.TWITTER_PROFILE_SUMMARIZATION);
		details.setUserId(userIdentity);

		getTaskMetadata(details);

		if(DataGroups.hasDataGroup(Definitions.DATA_GROUP_BACKEND_STATUS, dataGroups)){
			LOGGER.debug("Retrieving backend status list.");
			getBackendStatusList(details);
		}

		return details;
	}
}
