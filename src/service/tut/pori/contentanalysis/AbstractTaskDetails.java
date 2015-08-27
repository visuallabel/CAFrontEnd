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

import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.ArrayUtils;

import service.tut.pori.contentanalysis.AsyncTask.TaskType;
import core.tut.pori.http.ResponseData;
import core.tut.pori.users.UserIdentity;


/**
 * The base class for task details (task workload). Contains basic members and functionality needed for task submission.
 * 
 * Note: remember to define \@XmlRootElement(name=ELEMENT_NAME) for your inherited class
 * 
 * <h2>Optional Elements</h2>
 * <ul>
 *  <li>{@value core.tut.pori.users.Definitions#ELEMENT_USER_ID}, missing if task is generated by the system, or the task is anonymous.</li>
 *  <li>{@value service.tut.pori.contentanalysis.Definitions#ELEMENT_TASK_PARAMETERS}. The parameters depend on the task in question. If the element is missing the back-ends should run back-end specific default operation. </li>
 * </ul>
 * 
 * @see service.tut.pori.contentanalysis.BackendStatusList
 */
@XmlAccessorType(XmlAccessType.NONE)
public abstract class AbstractTaskDetails extends ResponseData{
	@XmlElement(name = Definitions.ELEMENT_BACKEND_ID)
	private Integer _backendId = null;
	private String _callbackUri = null;
	@XmlElement(name = Definitions.ELEMENT_TASK_ID)
	private Long _taskId = null;
	@XmlElement(name = Definitions.ELEMENT_TASK_TYPE)
	private TaskType _taskType = null;
	private UserIdentity _userId = null;
	@XmlElement(name = Definitions.ELEMENT_BACKEND_STATUS_LIST)
	private BackendStatusList _backends = null;	// an optional list of back-ends participating in this task
	private Map<String, String> _metadata = null;
	
	/**
	 * 
	 * @return the value of user identity
	 * @see #getUserId()
	 */
	@XmlElement(name = core.tut.pori.users.Definitions.ELEMENT_USER_ID)
	public Long getUserIdValue(){
		return (_userId == null ? null : _userId.getUserId());
	}
	
	/**
	 * for serialization
	 * @param value
	 * @see #setUserId(UserIdentity)
	 */
	@SuppressWarnings("unused")
	private void setUserIdValue(Long value){
		if(value != null){
			_userId = new UserIdentity(value);
		}
	}

	/**
	 * 
	 * @return user identity
	 * @see #setUserId(UserIdentity)
	 */
	public UserIdentity getUserId() {
		return _userId;
	}

	/**
	 * 
	 * @param userId
	 * @see #getUserId()
	 */
	public void setUserId(UserIdentity userId) {
		_userId = userId;
	}

	/**
	 * 
	 * @param type
	 * @see #getTaskType()
	 */
	public void setTaskType(TaskType type){
		_taskType = type;
	}

	/**
	 * 
	 * @return task type
	 * @see #setTaskType(service.tut.pori.contentanalysis.AsyncTask.TaskType)
	 */
	public TaskType getTaskType() {
		return _taskType;
	}

	/**
	 * 
	 * @return back-end id of the first back-end in the status list
	 * @see #getBackendId()
	 */
	public Integer getBackendId() {
		return _backendId;
	}

	/**
	 * @param backendId
	 * @see #getBackendId()
	 */
	public void setBackendId(Integer backendId) {
		_backendId = backendId;
	}

	/**
	 * 
	 * @return task id
	 * @see #setTaskId(Long)
	 */
	public Long getTaskId() {
		return _taskId;
	}

	/**
	 * 
	 * @param taskId
	 * @see #getTaskId()
	 */
	public void setTaskId(Long taskId){
		_taskId = taskId;     
	}

	/**
	 * 
	 * @return callback uri
	 * @see #setCallbackUri(String)
	 */
	@XmlElement(name = Definitions.ELEMENT_CALLBACK_URI)
	public String getCallbackUri() {
		return _callbackUri;
	}

	/**
	 * Can be used to override the default, generated call back uri
	 * 
	 * @param callbackUri
	 * @see #getCallbackUri()
	 */
	public void setCallbackUri(String callbackUri) {
		_callbackUri = callbackUri;
	}

	/**
	 * Default data groups: {@value service.tut.pori.contentanalysis.Definitions#DATA_GROUP_BACKEND_STATUS}
	 * 
	 * @return the backends
	 * @see #setBackends(BackendStatusList)
	 */
	public BackendStatusList getBackends() {
		return _backends;
	}

	/**
	 * @param backends the backends to set
	 * @see #getBackends()
	 */
	public void setBackends(BackendStatusList backends) {
		_backends = backends;
	}
	
	/**
	 * 
	 * @param status
	 * @see #getBackends()
	 */
	public void setBackend(BackendStatus status) {
		if(_backends == null){
			_backends = new BackendStatusList();
		}
		_backends.setBackendStatus(status);
	}

	/**
	 * @return the metadata
	 * @see #setMetadata(Map)
	 */
	public Map<String, String> getMetadata() {
		return _metadata;
	}

	/**
	 * @param metadata the metadata to set
	 * @see #getMetadata()
	 */
	public void setMetadata(Map<String, String> metadata) {
		_metadata = metadata;
	}

	@Override
	public Class<?>[] getDataClasses() {
		Class<?>[] classes = super.getDataClasses();
		TaskParameters params = getTaskParameters();
		if(params != null){
			classes = ArrayUtils.add(classes, params.getClass());
		}
		return classes;
	}

	/**
	 * @return the parameters
	 * @see #setTaskParameters(TaskParameters)
	 */
	@XmlElementRef
	public abstract TaskParameters getTaskParameters();

	/**
	 * @param parameters the parameters to set
	 * @see #getTaskParameters()
	 */
	public abstract void setTaskParameters(TaskParameters parameters);
	
	/**
	 * Abstract base class for optional task parameters.
	 * 
	 * The class should have no-args constructor for database serialization.
	 *
	 */
	@XmlRootElement(name = Definitions.ELEMENT_TASK_PARAMETERS)
	@XmlAccessorType(XmlAccessType.NONE)
	public static abstract class TaskParameters {
		/**
		 * initialize the the parameters from the given metadata map
		 * 
		 * @param metadata
		 * @throws IllegalArgumentException on bad values
		 */
		public abstract void initialize(Map<String, String> metadata) throws IllegalArgumentException;

		/**
		 * 
		 * @return this object converted to metadata map or null if no content
		 */
		public abstract Map<String, String> toMetadata();
		
		/**
		 * For database serialization
		 */
		public TaskParameters(){
			// nothing needed
		}
	} // class TaskParameters
}
