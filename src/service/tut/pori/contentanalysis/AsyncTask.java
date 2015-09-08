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
package service.tut.pori.contentanalysis;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.springframework.context.ApplicationEvent;

import service.tut.pori.contentanalysis.AnalysisBackend.Capability;
import core.tut.pori.utils.XMLFormatter;

/**
 * Base class for tasks. There is a default implementation to call addTask, which should suffice in many cases.
 */
public abstract class AsyncTask implements Job{
	/** Job data key, content type is long */
	private static final String JOB_DATA_TASK_ID = "taskId";
	private static final Logger LOGGER = Logger.getLogger(AsyncTask.class);
	private static final String TASKSTATUS_COMPLETED = "COMPLETED";
	private static final String TASKSTATUS_ERROR = "ERROR";
	private static final String TASKSTATUS_EXECUTING = "EXECUTING";
	private static final String TASKSTATUS_NOT_STARTED = "NOT_STARTED";
	private static final String TASKSTATUS_PENDING = "PENDING";
	private static final String TASKSTATUS_UNKNOWN = "UNKNOWN";
	private static final String TASKTYPE_ANALYSIS = "ANALYSIS";
	private static final String TASK_TYPE_BACKEND_FEEDBACK = "BACKEND_FEEDBACK";
	private static final String TASKTYPE_FACEBOOK_PROFILE_SUMMARIZATION = "FACEBOOK_PROFILE_SUMMARIZATION";
	private static final String TASK_TYPE_FACEBOOK_PROFILE_SUMMARIZATION_FEEDBACK = "FACEBOOK_PROFILE_SUMMARIZATION_FEEDBACK";
	private static final String TASK_TYPE_TWITTER_PROFILE_SUMMARIZATION = "TWITTER_PROFILE_SUMMARIZATION";
	private static final String TASKTYPE_FEEDBACK = "FEEDBACK";
	private static final String TASKTYPE_SEARCH = "SEARCH";
	private static final String TASKTYPE_UNDEFINED = "UNDEFINED";
	
	/**
	 * The status of the task.
	 */
	@XmlEnum
	public enum TaskStatus{
		/** unknown or unspecified task status */
		@XmlEnumValue(value = TASKSTATUS_UNKNOWN)
		UNKNOWN(0),
		/** task has been created, but back-ends have not yet started to process it, or the the task has not been delivered to back-ends */
		@XmlEnumValue(value = TASKSTATUS_NOT_STARTED)
		NOT_STARTED(1),
		/** task has been delivered to back-end, but the analysis has not yet started */
		@XmlEnumValue(value = TASKSTATUS_PENDING)
		PENDING(2),
		/** the task is being executed */
		@XmlEnumValue(value = TASKSTATUS_EXECUTING)
		EXECUTING(3),
		/** task has completed */
		@XmlEnumValue(value = TASKSTATUS_COMPLETED)
		COMPLETED(4),
		/** an error condition has prevented to execution of the task */
		@XmlEnumValue(value = TASKSTATUS_ERROR)
		ERROR(5);

		private int _value;


		/**
		 * 
		 * @param value
		 */
		private TaskStatus(int value){
			_value = value;
		}


		/**
		 * 
		 * @return TaskStatus as integer
		 */
		public int toInt(){
			return _value;
		}

		/**
		 * 
		 * @param value
		 * @return the value converted to TaskStatus
		 * @throws IllegalArgumentException on bad input
		 */
		public static TaskStatus fromInt(int value) throws IllegalArgumentException{
			for(TaskStatus s : TaskStatus.values()){
				if(s._value == value){
					return s;
				}
			}
			throw new IllegalArgumentException("Bad "+TaskStatus.class.toString()+" : "+value);
		}

		/**
		 * 
		 * @param statusList
		 * @return the combined status for the list of status codes or null if null or empty list was passed
		 */
		public static TaskStatus getCombinedTaskStatus(Collection<TaskStatus> statusList) {
			if(statusList == null || statusList.size() < 1){
				return null;
			}
			TaskStatus status = UNKNOWN;
			for(Iterator<TaskStatus> iter = statusList.iterator();iter.hasNext();){
				switch(iter.next()){
					case COMPLETED:
						if(status == NOT_STARTED){
							status = PENDING;
						}else if(status!=PENDING && status!=EXECUTING && status!=COMPLETED){
							status = COMPLETED;
						}
						break;
					case ERROR:
						return ERROR;
					case EXECUTING:
						status = EXECUTING;
						break;
					case NOT_STARTED:
						if(status != PENDING && status != EXECUTING)
							status = NOT_STARTED;
						break;
					case PENDING:
						status = PENDING;
						break;
					case UNKNOWN:
						LOGGER.debug("Unknown Task Status detected.");
						break;
					default:
						LOGGER.error("Unhandled "+TaskStatus.class.toString());
						break;
				}
			}
			return status;
		}
	}  // enum TaskStatus


	/**
	 * The type of the task.
	 * 
	 * New task types cannot be defined by the services. 
	 * If new task type is required one option is to use {@link service.tut.pori.contentanalysis.AsyncTask.TaskType#UNDEFINED} and add the proper task type as a metadata for the task.
	 */
	@XmlEnum
	public enum TaskType{
		/** task type is unknown or undefined */
		@XmlEnumValue(value = TASKTYPE_UNDEFINED)
		UNDEFINED(0),
		/** media analysis task */
		@XmlEnumValue(value = TASKTYPE_ANALYSIS)
		ANALYSIS(1),
		/** search task */
		@XmlEnumValue(value = TASKTYPE_SEARCH)
		SEARCH(2),
		/** Feedback task. This can either be direct or indirect user feedback (e.g. deleted content) */
		@XmlEnumValue(value = TASKTYPE_FEEDBACK)
		FEEDBACK(3), // user feedback
		/** facebook user profile summarization */
		@XmlEnumValue(value = TASKTYPE_FACEBOOK_PROFILE_SUMMARIZATION)
		FACEBOOK_PROFILE_SUMMARIZATION(4),
		/** user feedback for facebook profile summarization */
		@XmlEnumValue(value = TASK_TYPE_FACEBOOK_PROFILE_SUMMARIZATION_FEEDBACK)
		FACEBOOK_PROFILE_SUMMARIZATION_FEEDBACK(5),
		/** twitter user profile summarization */
		@XmlEnumValue(value = TASK_TYPE_TWITTER_PROFILE_SUMMARIZATION)
		TWITTER_PROFILE_SUMMARIZATION(6),
		/** feedback generated based on results received from back-ends. This task is always targeted to other back-ends. */
		@XmlEnumValue(value = TASK_TYPE_BACKEND_FEEDBACK)
		BACKEND_FEEDBACK(7);

		private int _value;


		/**
		 * 
		 * @param value
		 */
		private TaskType(int value){
			_value = value;
		}


		/**
		 * 
		 * @return TaskType as integer
		 */
		public int toInt(){
			return _value;
		}

		/**
		 * 
		 * @param value
		 * @return the value converted to TaskType
		 * @throws IllegalArgumentException on bad input
		 */
		public static TaskType fromInt(int value) throws IllegalArgumentException{
			for(TaskType s : TaskType.values()){
				if(s._value == value){
					return s;
				}
			}
			throw new IllegalArgumentException("Bad "+TaskType.class.toString()+" : "+value);
		}

		/**
		 * 
		 * @param value
		 * @return the value converted to TaskType
		 * @throws IllegalArgumentException on bad input
		 */
		public static TaskType fromString(String value) throws IllegalArgumentException {
			if(!StringUtils.isBlank(value)){
				switch(value.toUpperCase()){
					case TASKTYPE_ANALYSIS:
						return TaskType.ANALYSIS;
					case TASK_TYPE_BACKEND_FEEDBACK:
						return TaskType.BACKEND_FEEDBACK;
					case TASKTYPE_FACEBOOK_PROFILE_SUMMARIZATION:
						return FACEBOOK_PROFILE_SUMMARIZATION;
					case TASK_TYPE_FACEBOOK_PROFILE_SUMMARIZATION_FEEDBACK:
						return FACEBOOK_PROFILE_SUMMARIZATION_FEEDBACK;
					case TASK_TYPE_TWITTER_PROFILE_SUMMARIZATION:
						return TWITTER_PROFILE_SUMMARIZATION;
					case TASKTYPE_FEEDBACK:
						return FEEDBACK;
					case TASKTYPE_SEARCH:
						return SEARCH;
					case TASKTYPE_UNDEFINED:
						return UNDEFINED;
					default:
						break;
				}
			}
			throw new IllegalArgumentException("Bad "+TaskType.class.toString()+" : "+value);
		}
	} //  enum TaskType

	/**
	 * Default implementation for addTask.
	 * 
	 * This will:
	 * - use the default TaskDAO to retrieve the back-ends associated with the task
	 * - use the default TaskDAO to retrieve the task details for the task
	 * - call each backend's addTask method with default parameters
	 * - use the default TaskDAO to update the back-end details for the associated back-ends
	 * 
	 * @param requiredCapabilities capabilities required for the participating back-ends, all back-ends for the task not having ALL of the capabilities are ignored. If null no check is performed.
	 * @param taskDAO used to resolve task details with taskDAO.getTask(backendId, dataGroups, limits, taskId) with limit parameter null
	 * @param taskId
	 */
	protected void executeAddTask(Set<Capability> requiredCapabilities, TaskDAO taskDAO, Long taskId) {
		try{
			BackendStatusList backends = taskDAO.getBackendStatus(taskId, TaskStatus.NOT_STARTED);
			if(BackendStatusList.isEmpty(backends)){
				LOGGER.warn("No analysis back-ends available for taskId: "+taskId+" with status "+TaskStatus.NOT_STARTED.name());
				return;
			}
			
			if(requiredCapabilities != null && !requiredCapabilities.isEmpty()){
				backends = BackendStatusList.getBackendStatusList(backends.getBackendStatuses(requiredCapabilities)); // filter back-ends
				if(BackendStatusList.isEmpty(backends)){
					LOGGER.warn("Aborting execute... no back-end given with required capabilities for task, id: "+taskId);
					return;
				}
			}else{
				LOGGER.debug("Ignoring capability check...");
			}

			try (CloseableHttpClient client = HttpClients.createDefault()) {
				BasicResponseHandler h = new BasicResponseHandler();
				for(BackendStatus status : backends.getBackendStatuses()){              
					AnalysisBackend end = status.getBackend();
					Integer backendId = end.getBackendId();
					AbstractTaskDetails details = taskDAO.getTask(backendId, end.getDefaultTaskDataGroups(), null, taskId);
					if(details == null){
						LOGGER.warn("Task, id: "+taskId+" does not exist for backend, id: "+backendId);
						continue;
					}

					if(details.getUserId() == null && !end.hasCapability(Capability.ANONYMOUS_TASK)){
						LOGGER.warn("backendId: "+backendId+" cannot process anonymous tasks. Ignoring backend...");
						continue;
					}

					try {
						String url = end.getAnalysisUri()+Definitions.METHOD_ADD_TASK;
						LOGGER.debug("Sending "+Definitions.METHOD_ADD_TASK+" to URL: "+url);
						HttpPost taskRequest = new HttpPost(url);
						details.setBackendId(backendId);
						taskRequest.setHeader("Content-Type", "text/xml; charset=UTF-8");
						taskRequest.setEntity(new StringEntity((new XMLFormatter()).toString(details), core.tut.pori.http.Definitions.ENCODING_UTF8));

						LOGGER.debug("Backend with id: "+backendId+" responded "+client.execute(taskRequest,h));

						status.setStatus(TaskStatus.EXECUTING); //updates the status of the task for this back-end
					} catch (IOException ex) {
						LOGGER.warn(ex, ex);
					}
				}  // for
			} catch (IOException ex) {
				LOGGER.error(ex, ex);
			}
			taskDAO.updateTaskStatus(backends, taskId);
		}catch (Throwable ex) {	// catch all exceptions to prevent re-scheduling on error
			LOGGER.error(ex, ex);
		}
	}  // run

	/**
	 * 
	 * @param map non-null map
	 * @param taskId if null or empty, previous value will be removed
	 */
	public static void setTaskId(JobDataMap map, Long taskId){
		if(taskId == null){
			map.remove(JOB_DATA_TASK_ID);
		}else{
			map.put(JOB_DATA_TASK_ID, taskId);
		}
	}

	/**
	 * 
	 * @param map
	 * @return the taskId or null if not found or the map was null
	 */
	public static Long getTaskId(JobDataMap map){
		if(map == null || !map.containsKey(JOB_DATA_TASK_ID)){
			return null;
		}else{
			return map.getLong(JOB_DATA_TASK_ID);
		}
	}
	
	/**
	 * An application event used to notify listeners about progress or change in status of an ASyncTask execution.
	 *
	 */
	public static class AsyncTaskEvent extends ApplicationEvent{
		/** serial version id */
		private static final long serialVersionUID = 2342360048902381597L;
		private Integer _backendId = null;
		private Class<?> _source = null;
		private TaskStatus _status = null;
		private Long _taskId = null;
		private TaskType _taskType = null;

		/**
		 * 
		 * @param source
		 */
		public AsyncTaskEvent(Class<?> source) {
			super(source);
			_source = source;
		}
		
		/**
		 * 
		 * @param source
		 * @param status
		 * @param taskId
		 * @param taskType
		 */
		public AsyncTaskEvent(Class<?> source, TaskStatus status, Long taskId, TaskType taskType) {
			super(source);
			_status = status;
			_taskId = taskId;
			_taskType = taskType;
		}
		
		/**
		 * 
		 * @param backendId
		 * @param source
		 * @param status
		 * @param taskId
		 * @param taskType
		 */
		public AsyncTaskEvent(Integer backendId, Class<?> source, TaskStatus status, Long taskId, TaskType taskType) {
			super(source);
			_backendId = backendId;
			_status = status;
			_taskId = taskId;
			_taskType = taskType;
		}

		/**
		 * @return the backendId
		 */
		public Integer getBackendId() {
			return _backendId;
		}

		@Override
		public Class<?> getSource() {
			return _source;
		}

		/**
		 * @return the status
		 */
		public TaskStatus getStatus() {
			return _status;
		}

		/**
		 * @return the taskId
		 */
		public Long getTaskId() {
			return _taskId;
		}

		/**
		 * @return the taskType
		 */
		public TaskType getTaskType() {
			return _taskType;
		}
	} // class AsyncTaskEvent
}
