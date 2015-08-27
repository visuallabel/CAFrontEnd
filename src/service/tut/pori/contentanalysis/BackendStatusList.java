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

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.log4j.Logger;

import service.tut.pori.contentanalysis.AnalysisBackend.Capability;
import service.tut.pori.contentanalysis.AsyncTask.TaskStatus;


/**
 * Contains back-end specific status information.
 * 
 * <h3>XML Example</h3>
 * 
 * {@doc.restlet service="[service.tut.pori.contentanalysis.reference.Definitions#SERVICE_CA_REFERENCE_EXAMPLE]" method="[service.tut.pori.contentanalysis.Definitions#ELEMENT_BACKEND_STATUS_LIST]" type="GET" query="" body_uri=""}
 * 
 * @see service.tut.pori.contentanalysis.BackendStatus
 */
@XmlRootElement(name=Definitions.ELEMENT_BACKEND_STATUS_LIST)
@XmlAccessorType(XmlAccessType.NONE)
public class BackendStatusList{
	private static final Logger LOGGER = Logger.getLogger(BackendStatusList.class);
	@XmlElement(name = Definitions.ELEMENT_BACKEND_STATUS)
	private List<BackendStatus> _backendStatuses = null;     

	/**
	 * 
	 * @return list of back-end statuses
	 * @see #setBackendStatus(BackendStatus)
	 */
	public List<BackendStatus> getBackendStatuses() {
		return _backendStatuses;
	}

	/**
	 * 
	 * @return true if all back-ends in this list have completed their task
	 */
	public boolean isCompleted(){
		if(_backendStatuses == null){
			LOGGER.warn("No back-ends.");
			return false;
		}else{
			for(Iterator<BackendStatus> iter = _backendStatuses.iterator(); iter.hasNext();){
				if(iter.next().getStatus() != TaskStatus.COMPLETED){
					return false;
				}
			}
			return true;
		}
	}

	/**
	 * 
	 * @param allOf
	 * @return all back-ends that have all of the given capabilities or null if none found
	 */
	public List<BackendStatus> getBackendStatuses(EnumSet<Capability> allOf){
		if(isEmpty()){
			LOGGER.debug("No statuses.");
			return null;
		}
		if(allOf == null || allOf.isEmpty()){
			LOGGER.warn("Empty capability list.");
			return null;
		}
		List<BackendStatus> statusesWithCapability = new ArrayList<>();
		for(BackendStatus status : _backendStatuses){
			AnalysisBackend backend = status.getBackend();
			if(backend.hasCapabilities(allOf)){
				statusesWithCapability.add(status);
			}
		}
		return (statusesWithCapability.isEmpty() ? null : statusesWithCapability);
	}

	/**
	 * 
	 * @return the overall (combined) status for all backends
	 * 
	 */
	public TaskStatus getCombinedStatus(){
		if(_backendStatuses != null){
			List<TaskStatus> statusList = new ArrayList<>();
			for(Iterator<BackendStatus> iter = _backendStatuses.iterator(); iter.hasNext();){
				statusList.add(iter.next().getStatus());
			}  // for
			return TaskStatus.getCombinedTaskStatus(statusList);
		}else{
			return TaskStatus.UNKNOWN;
		}
	}

	/**
	 * If a status for the given backend already exists, it is replaced
	 * 
	 * @param backendStatus
	 * @see #getBackendStatuses()
	 */
	public void setBackendStatus(BackendStatus backendStatus) {
		if(_backendStatuses == null){
			_backendStatuses = new ArrayList<>();
		}else{
			Integer backendId = backendStatus.getBackend().getBackendId();
			for(Iterator<BackendStatus> iter = _backendStatuses.iterator(); iter.hasNext();){	// remove old one if it exists
				BackendStatus status = iter.next();
				if(status.getBackend().getBackendId().equals(backendId)){
					iter.remove();
					break;
				}
			}	// for
		}
		_backendStatuses.add(backendStatus);
	}

	/**
	 * 
	 * @param backendId
	 * @return status for the backend, or null if none available
	 */
	public BackendStatus getBackendStatus(Integer backendId){
		if(_backendStatuses == null){
			return null;
		}
		for(Iterator<BackendStatus> iter = _backendStatuses.iterator(); iter.hasNext();){	// remove old one if it exists
			BackendStatus status = iter.next();
			if(status.getBackend().getBackendId().equals(backendId)){
				return status;
			}
		}	// for
		return null;
	}

	/**
	 * 
	 * @param container 
	 * @return true if the container is null or empty
	 */
	public static boolean isEmpty(BackendStatusList container){
		if(container == null){
			return true;
		}else{
			return container.isEmpty();
		}
	}

	/**
	 * use the static, this is only for sub-classing
	 * @return true if this container is empty
	 * @see #isEmpty(BackendStatusList)
	 */
	protected boolean isEmpty(){
		return (_backendStatuses == null || _backendStatuses.isEmpty() ? true : false);
	}

	/**
	 * 
	 * @param statuses
	 * @return new status list or null if empty or null list was passed
	 */
	public static BackendStatusList getBackendStatusList(List<BackendStatus> statuses){
		if(statuses == null || statuses.isEmpty()){
			return null;
		}
		BackendStatusList list = new BackendStatusList();
		list._backendStatuses = statuses;
		return list;
	}
}
