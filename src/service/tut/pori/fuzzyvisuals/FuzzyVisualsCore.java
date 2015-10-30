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
package service.tut.pori.fuzzyvisuals;

import org.apache.log4j.Logger;

import core.tut.pori.context.ServiceInitializer;
import service.tut.pori.contentanalysis.Definitions;
import service.tut.pori.contentanalysis.AsyncTask.TaskType;

/**
 * Fuzzy Visuals core methods
 * 
 */
public final class FuzzyVisualsCore {
	private static final Logger LOGGER = Logger.getLogger(FuzzyVisualsCore.class);

	/**
	 * 
	 * @param taskDetails
	 * @throws IllegalArgumentException 
	 */
	public static void addTask(TaskDetails taskDetails) throws IllegalArgumentException {
		TaskType taskType = taskDetails.getTaskType();
		if(taskType == null){
			throw new IllegalArgumentException(Definitions.ELEMENT_TASK_TYPE+" is missing.");
		}
		switch(taskType){
			case BACKEND_FEEDBACK:
			case FEEDBACK:
				LOGGER.debug("Receiving task of type "+taskType.name()+", returning OK.");
				break;
			case ANALYSIS:
				ServiceInitializer.getExecutorHandler().getExecutor().execute(new FuzzyAnalysisTask(taskDetails.getBackendId(), taskDetails.getCallbackUri(), taskDetails.getMedia(), taskDetails.getTaskId()));
				break;
			default:
				throw new IllegalArgumentException("Unsupported "+Definitions.ELEMENT_TASK_TYPE+" : "+taskType.name());
		}
	}
}
