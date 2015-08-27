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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import service.tut.pori.contentanalysis.AsyncTask.TaskStatus;
import service.tut.pori.contentanalysis.AsyncTask.TaskType;
import core.tut.pori.http.ResponseData;

/**
 * Class for representing a response received from a back-end to a previously submitted analysis task.
 * 
 * <h2>Optional Elements</h2>
 * <ul>
 *  <li>{@value service.tut.pori.contentanalysis.Definitions#ELEMENT_MESSAGE}</li>
 * </ul>
 * 
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public abstract class TaskResponse extends ResponseData{
	@XmlElement(name=Definitions.ELEMENT_BACKEND_ID)
	private Integer _backendId = null;
	@XmlElement(name=Definitions.ELEMENT_MESSAGE)
	private String _message = null;
	@XmlElement(name=Definitions.ELEMENT_STATUS)
	private TaskStatus _status = null;
	@XmlElement(name=Definitions.ELEMENT_TASK_ID)
	private Long _taskId = null;
	@XmlElement(name=Definitions.ELEMENT_TASK_TYPE)
	private TaskType _taskType = null;
	
	/**
	 * @return the taskId
	 * @see #setTaskId(Long)
	 */
	public Long getTaskId() {
		return _taskId;
	}
	
	/**
	 * @param taskId the taskId to set
	 * @see #getTaskId()
	 */
	public void setTaskId(Long taskId) {
		_taskId = taskId;
	}
	
	/**
	 * @return the backendId
	 * @see #setBackendId(Integer)
	 */
	public Integer getBackendId() {
		return _backendId;
	}
	
	/**
	 * @param backendId the backendId to set
	 * @see #getBackendId()
	 */
	public void setBackendId(Integer backendId) {
		_backendId = backendId;
	}
	
	/**
	 * @return the status
	 * @see #setStatus(service.tut.pori.contentanalysis.AsyncTask.TaskStatus)
	 */
	public TaskStatus getStatus() {
		return _status;
	}
	
	/**
	 * @param status the status to set
	 * @see #getStatus()
	 */
	public void setStatus(TaskStatus status) {
		_status = status;
	}
	
	/**
	 * @return the status message or null if not available
	 * @see #setMessage(String)
	 */
	public String getMessage() {
		return _message;
	}
	
	/**
	 * @param message the message to set
	 * @see #getMessage()
	 */
	public void setMessage(String message) {
		_message = message;
	}

	/**
	 * @return the taskType
	 * @see #setTaskType(service.tut.pori.contentanalysis.AsyncTask.TaskType)
	 */
	public TaskType getTaskType() {
		return _taskType;
	}

	/**
	 * @param taskType the taskType to set
	 * @see #getTaskType()
	 */
	public void setTaskType(TaskType taskType) {
		_taskType = taskType;
	}
}
