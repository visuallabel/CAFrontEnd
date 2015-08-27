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

import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.log4j.Logger;

import service.tut.pori.contentanalysis.AsyncTask.TaskType;

/**
 * Registration details for Task Finished Web Socket Service.
 * 
 * Backend ids, task ids and task types work as filters, not giving any values equals to "accept all values".
 * 
 * When using user id or anonymous task filter, the user must have the appropriate permissions to access these tasks.
 * In practice this means that the user must be the owner/creator of the task or have extended user permissions (e.g. ROLE_BACKEND).
 */
@XmlRootElement(name=Definitions.ELEMENT_REGISTRATION)
@XmlAccessorType(XmlAccessType.NONE)
public class Registration {
	private static final Logger LOGGER = Logger.getLogger(Registration.class);
	@XmlElementWrapper(name=Definitions.ELEMENT_BACKEND_ID_LIST)
	@XmlElement(name=service.tut.pori.contentanalysis.Definitions.ELEMENT_BACKEND_ID)
	private Set<Integer> _backendIds = null;
	@XmlElement(name=Definitions.ELEMENT_LISTEN_ANONYMOUS_TASKS)
	private boolean _listenAnonymousTasks = false;
	@XmlElementWrapper(name=Definitions.ELEMENT_TASK_ID_LIST)
	@XmlElement(name=service.tut.pori.contentanalysis.Definitions.ELEMENT_TASK_ID)
	private Set<Long> _taskIds = null;
	@XmlElementWrapper(name=Definitions.ELEMENT_TASK_TYPE_LIST)
	@XmlElement(name=service.tut.pori.contentanalysis.Definitions.ELEMENT_TASK_TYPE)
	private Set<TaskType> _tasktypes = null;
	@XmlElementWrapper(name=Definitions.ELEMENT_USER_ID_LIST)
	@XmlElement(name=core.tut.pori.users.Definitions.ELEMENT_USER_ID)
	private Set<Long> _userIds = null;
	
	/**
	 * @return the taskIds
	 */
	public Set<Long> getTaskIds() {
		return _taskIds;
	}
	
	/**
	 * @param taskIds the taskIds to set
	 */
	public void setTaskIds(Set<Long> taskIds) {
		_taskIds = taskIds;
	}
	
	/**
	 * @return the tasktypes
	 */
	public Set<TaskType> getTasktypes() {
		return _tasktypes;
	}
	
	/**
	 * @param tasktypes the tasktypes to set
	 */
	public void setTasktypes(Set<TaskType> tasktypes) {
		_tasktypes = tasktypes;
	}

	/**
	 * @return the backendIds
	 */
	public Set<Integer> getBackendIds() {
		return _backendIds;
	}

	/**
	 * @param backendIds the backendIds to set
	 */
	public void setBackendIds(Set<Integer> backendIds) {
		_backendIds = backendIds;
	}
	
	/**
	 * 
	 * @param backendId
	 * @return true if the given backendId has been given or the backendId set is empty or null
	 */
	public boolean hasBackendId(Integer backendId){
		if(_backendIds == null || _backendIds.isEmpty()){
			LOGGER.debug("No "+Definitions.ELEMENT_BACKEND_ID_LIST+" : returning true.");
			return true;
		}else{
			return _backendIds.contains(backendId);
		}
	}
	
	/**
	 * 
	 * @param taskType
	 * @return true if the given TaskType has been given or the taskType set is empty or null
	 */
	public boolean hasTaskType(TaskType taskType){
		if(_tasktypes == null || _tasktypes.isEmpty()){
			LOGGER.debug("No "+Definitions.ELEMENT_TASK_TYPE_LIST+" : returning true.");
			return true;
		}else{
			return _tasktypes.contains(taskType);
		}
	}
	
	/**
	 * 
	 * @param taskId
	 * @return true if the given taskId has been given or the taskId set is empty or null
	 */
	public boolean hasTaskId(Long taskId){
		if(_taskIds == null || _taskIds.isEmpty()){
			LOGGER.debug("No "+Definitions.ELEMENT_TASK_ID_LIST+" : returning true.");
			return true;
		}else{
			return _taskIds.contains(taskId);
		}
	}

	/**
	 * @return the listenAnonymousTasks
	 */
	public boolean isListenAnonymousTasks() {
		return _listenAnonymousTasks;
	}

	/**
	 * @param listenAnonymousTasks the listenAnonymousTasks to set
	 */
	public void setListenAnonymousTasks(boolean listenAnonymousTasks) {
		_listenAnonymousTasks = listenAnonymousTasks;
	}

	/**
	 * @return the userIds
	 */
	public Set<Long> getUserIds() {
		return _userIds;
	}

	/**
	 * @param userIds the userIds to set
	 */
	public void setUserIds(Set<Long> userIds) {
		_userIds = userIds;
	}
	
	/**
	 * 
	 * @param userId
	 * @return true if and only if the user id list contains the given user id
	 */
	public boolean hasUserId(Long userId){
		return (_userIds == null ? false : _userIds.contains(userId));
	}
}
