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
package service.tut.pori.contentstorage;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

import service.tut.pori.contentanalysis.AbstractTaskDetails;
import service.tut.pori.contentanalysis.AccessDetails;
import service.tut.pori.contentanalysis.AnalysisBackend;
import service.tut.pori.contentanalysis.AnalysisBackend.Capability;
import service.tut.pori.contentanalysis.AsyncTask.TaskStatus;
import service.tut.pori.contentanalysis.AsyncTask.TaskType;
import service.tut.pori.contentanalysis.BackendStatus;
import service.tut.pori.contentanalysis.BackendStatusList;
import service.tut.pori.contentanalysis.CAContentCore.ServiceType;
import core.tut.pori.users.UserIdentity;

/**
 * Abstract base class for ContentStorage handlers.
 * 
 * Note that sub-classing this class does not automatically add the storage handler as an usable handler, changes to the ContentStorageCore are also required.
 */
public abstract class ContentStorage {
	private boolean _autoSchedule = true;
	private ContentStorageListener _listener = null;
	private BackendStatusList _backends = null;
	
	/**
	 * 
	 * @param autoSchedule
	 */
	public ContentStorage(boolean autoSchedule){
		_autoSchedule = autoSchedule;
	}
	
	
	
	/**
	 * @return the backends
	 */
	public BackendStatusList getBackends() {
		return _backends;
	}

	/**
	 * 
	 * @param allOf
	 * @return backend statuses for all backends with all of the given capabilities
	 */
	public BackendStatusList getBackends(EnumSet<Capability> allOf) {
		if(_backends == null){
			return null;
		}
		return BackendStatusList.getBackendStatusList(_backends.getBackendStatuses(allOf));
	}

	/**
	 * @param backends the backends to set
	 */
	public void setBackends(BackendStatusList backends) {
		if(BackendStatusList.isEmpty(backends)){
			_backends = null;
		}else{
			_backends = backends;
		}
	}

	/**
	 * 
	 * @param backends all backends will be added with status NOT_STARTED
	 */
	public void setBackends(List<AnalysisBackend> backends){
		if(backends == null || backends.isEmpty()){
			_backends = null;
			return;
		}
		if(_backends == null){
			_backends = new BackendStatusList();
		}
		for(AnalysisBackend backend : backends){
			_backends.setBackendStatus(new BackendStatus(backend, TaskStatus.NOT_STARTED));
		}
	}

	/**
	 * Create new storage with autoschedule set to true
	 */
	public ContentStorage() {
		// nothing needed
	}

	/**
	 * @return the autoSchedule
	 */
	public boolean isAutoSchedule() {
		return _autoSchedule;
	}
	
	/**
	 * @return the listener
	 */
	public ContentStorageListener getContentStorageListener() {
		return _listener;
	}

	/**
	 * @param listener the listener to set
	 */
	public void setContentStorageListener(ContentStorageListener listener) {
		_listener = listener;
	}
	
	/**
	 * helper for notifying listener (if given) about a new photo task
	 * 
	 * @param details
	 * @throws IllegalArgumentException on invalid details
	 */
	protected void notifyFeedbackTaskCreated(AbstractTaskDetails details) throws IllegalArgumentException{
		if(details == null || details.getTaskType() != TaskType.FEEDBACK){
			throw new IllegalArgumentException("Invalid task details.");
		}
		
		ContentStorageListener listener = getContentStorageListener();
		if(listener != null){
			listener.feedbackTaskCreated(details);
		}
	}
	
	/**
	 * helper for notifying listener (if given) about a new photo task
	 * 
	 * @param details
	 * @throws IllegalArgumentException on invalid details
	 */
	protected void notifyAnalysisTaskCreated(AbstractTaskDetails details) throws IllegalArgumentException{
		if(details == null || details.getTaskType() != TaskType.ANALYSIS){
			throw new IllegalArgumentException("Invalid task details.");
		}
		
		ContentStorageListener listener = getContentStorageListener();
		if(listener != null){
			listener.analysisTaskCreated(details);
		}
	}

	/**
	 * 
	 * @return service type of the storage
	 */
	public abstract ServiceType getServiceType();
	
	/**
	 * 
	 * @param details
	 * @return static target URL or null if not available
	 */
	public abstract String getTargetUrl(AccessDetails details);
	
	/**
	 * Remove the synchronized content
	 * 
	 * @param userId this user's metadata will be removed from the front-end
	 * @param guids optional list of GUIDs, if null, everything is removed
	 */
	public abstract void removeMetadata(UserIdentity userId, Collection<String> guids);
	
	/**
	 * 
	 * @param userId
	 * @return true on success
	 */
	public abstract boolean synchronizeAccount(UserIdentity userId);
	
	/**
	 * 
	 * @return set of accepted back-end capabilities for this storage
	 */
	public abstract EnumSet<Capability> getBackendCapabilities();
	
	/**
	 * A listener interface, which can be used in combination with ContentStorage, either with or without task auto schedule.
	 * 
	 */
	public interface ContentStorageListener {
		/**
		 * Called when a new analyze task has been created. Note that this may be called multiple times, if multiple tasks are created.
		 * 
		 * @param details
		 */
		public void analysisTaskCreated(AbstractTaskDetails details);
		
		/**
		 * Called when a new feedback task has been created. Note that this may be called multiple times, if multiple tasks are created.
		 * 
		 * @param details
		 */
		public void feedbackTaskCreated(AbstractTaskDetails details);
	} // interface ContentStorageListener
}
