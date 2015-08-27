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
package service.tut.pori.cawebsocket;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import service.tut.pori.contentanalysis.AsyncTask.TaskType;
import service.tut.pori.contentanalysis.BackendStatusList;

/**
 * Details of a finished task
 *
 */
@XmlRootElement(name=Definitions.ELEMENT_TASK_STATUS)
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name="websocketTaskStatus") // change the type name as JAXB does not play nice with identical Class Names (ASyncTask.TaskStatus)
public class TaskStatus {
	@XmlElement(name=service.tut.pori.contentanalysis.Definitions.ELEMENT_BACKEND_STATUS_LIST)
	private BackendStatusList _backendStatusList = null;
	@XmlElement(name=service.tut.pori.contentanalysis.Definitions.ELEMENT_TASK_ID)
	private Long _taskId = null;
	@XmlElement(name=service.tut.pori.contentanalysis.Definitions.ELEMENT_TASK_TYPE)
	private TaskType _taskType = null;
	
	/**
	 * @return the backendStatusList
	 */
	public BackendStatusList getBackendStatusList() {
		return _backendStatusList;
	}
	
	/**
	 * @param backendStatusList the backendStatusList to set
	 */
	public void setBackendStatusList(BackendStatusList backendStatusList) {
		_backendStatusList = backendStatusList;
	}
	
	/**
	 * @return the taskId
	 */
	public Long getTaskId() {
		return _taskId;
	}
	
	/**
	 * @param taskId the taskId to set
	 */
	public void setTaskId(Long taskId) {
		_taskId = taskId;
	}

	/**
	 * @return the taskType
	 */
	public TaskType getTaskType() {
		return _taskType;
	}

	/**
	 * @param taskType the taskType to set
	 */
	public void setTaskType(TaskType taskType) {
		_taskType = taskType;
	}	
}
