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
package service.tut.pori.facebookjazz;

import java.util.EnumSet;

import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import service.tut.pori.contentanalysis.AnalysisBackend;
import service.tut.pori.contentanalysis.AnalysisBackend.Capability;
import service.tut.pori.contentanalysis.PhotoAnalysisTask;
import service.tut.pori.contentanalysis.AsyncTask;
import service.tut.pori.contentanalysis.BackendStatus;
import service.tut.pori.contentanalysis.PhotoTaskResponse;
import core.tut.pori.context.ServiceInitializer;

/**
 * An implementation of ASyncTask, meant for executing a Facebook summarization feedback task.
 * 
 * Requires a valid taskId for execution, provided in a JobExecutionContext.
 */
public class FBSummarizationFeedbackTask extends AsyncTask {
	private static final Logger LOGGER = Logger.getLogger(FBSummarizationFeedbackTask.class);

	/**
	 * 
	 * @param response
	 */
	public static void taskFinished(PhotoTaskResponse response) {
		TaskStatus status = response.getStatus();
		if(status == null){
			LOGGER.warn("Task status not available.");
			status = TaskStatus.UNKNOWN;
		}

		Long taskId = response.getTaskId();
		LOGGER.debug("Task, id: "+taskId+" finished for back-end, id: "+response.getBackendId());
		
		ServiceInitializer.getDAOHandler().getSQLDAO(FBTaskDAO.class).updateTaskStatus(new BackendStatus(new AnalysisBackend(response.getBackendId()), status), taskId);
		
		ServiceInitializer.getEventHandler().publishEvent(new AsyncTaskEvent(response.getBackendId(), PhotoAnalysisTask.class, status, response.getTaskId(), TaskType.FACEBOOK_PROFILE_SUMMARIZATION_FEEDBACK));
	}

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		executeAddTask(EnumSet.of(Capability.USER_FEEDBACK, Capability.FACEBOOK_SUMMARIZATION), ServiceInitializer.getDAOHandler().getSQLDAO(FBTaskDAO.class), getTaskId(context.getMergedJobDataMap()));
	}
}
