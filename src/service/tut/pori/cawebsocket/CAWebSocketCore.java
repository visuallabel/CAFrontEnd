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

import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationListener;

import com.google.common.collect.Sets;

import service.tut.pori.contentanalysis.AbstractTaskDetails;
import service.tut.pori.contentanalysis.Definitions;
import service.tut.pori.contentanalysis.PhotoTaskDAO;
import service.tut.pori.contentanalysis.AsyncTask.AsyncTaskEvent;
import service.tut.pori.contentanalysis.AsyncTask.TaskType;
import core.tut.pori.context.ServiceInitializer;
import core.tut.pori.http.parameters.DataGroups;
import core.tut.pori.users.UserAuthority;
import core.tut.pori.users.UserIdentity;

/**
 * Core methods for CAWebSocketService
 * 
 */
public final class CAWebSocketCore {
	private static final Logger LOGGER = Logger.getLogger(CAWebSocketCore.class);
	private static final ConcurrentHashMap<Long, Registration> REGISTERED_USERS = new ConcurrentHashMap<>(); // user-id, registration map
	
	/**
	 * 
	 */
	private CAWebSocketCore(){
		// nothing needed
	}
	
	/**
	 * Registers the given user as a listener for finished tasks. 
	 * If the registration object has no user id filter and anonymous task listener is disabled this method will automatically add the authenticatedUser as a filter.
	 * 
	 * @param authenticatedUser
	 * @param registration
	 */
	public static void taskFinishedRegistered(UserIdentity authenticatedUser, Registration registration){
		Long userId = authenticatedUser.getUserId();
		if(registration == null){
			LOGGER.debug("No registration details, using defaults for user, id: "+userId);
			registration = new Registration();
			registration.setUserIds(Sets.newHashSet(userId));
		}else{
			boolean extendedPermissions = UserIdentity.hasAuthority(UserAuthority.AUTHORITY_ROLE_BACKEND, authenticatedUser);
			Set<Long> userIdFilter = registration.getUserIds();
			if(registration.isListenAnonymousTasks() && !extendedPermissions){ // only allow user with extended permissions to listen for anonymous tasks. If anonymous listening is enabled (and permissions match), the user id filter can be given or omitted.
				LOGGER.warn("User, id: "+userId+" attempted to listen for anonymous tasks without appropriate permissions.");
				ServiceInitializer.getWebSocketHandler().getSocketService(TaskFinishedService.class).close(authenticatedUser, core.tut.pori.websocket.Definitions.CLOSE_REASON_FORBIDDEN);
				return;
			}else if(userIdFilter != null){
				if(userIdFilter.isEmpty()){ 
					LOGGER.debug("Empty user id filter, adding the authenticated user as a listener.");
					registration.setUserIds(Sets.newHashSet(userId));
				}else if(!extendedPermissions && (userIdFilter.size() != 1 || !userIdFilter.iterator().next().equals(userId))){ // check that there are no other user ids in addition to the authenticatedUser if the user has no extended permissions
					LOGGER.warn("Bad user id filter for user, id: "+userId);
					ServiceInitializer.getWebSocketHandler().getSocketService(TaskFinishedService.class).close(authenticatedUser, core.tut.pori.websocket.Definitions.CLOSE_REASON_FORBIDDEN);
					return;
				}
			}else{ // userIdFilter == null
				LOGGER.debug("No user id filter, adding the authenticated user as a listener.");
				registration.setUserIds(Sets.newHashSet(userId));
			}
			
			LOGGER.debug("Registering listener for user, id: "+userId);
		}
		REGISTERED_USERS.put(userId, registration);
	}
	
	/**
	 * 
	 * @param authenticatedUser
	 */
	public static void taskFinishedUnregistered(UserIdentity authenticatedUser){
		Long userId = authenticatedUser.getUserId();
		LOGGER.debug("Unregistering listener for user, id: "+userId);
		REGISTERED_USERS.remove(userId);
	}
	
	/**
	 * Listens for TaskStatus messages from completed tasks
	 *
	 */
	@SuppressWarnings("unused") // instance created automatically
	private static class TaskFinishedListener implements ApplicationListener<AsyncTaskEvent>{

		@Override
		public void onApplicationEvent(AsyncTaskEvent event) {
			if(event.getStatus() != service.tut.pori.contentanalysis.AsyncTask.TaskStatus.COMPLETED){ // ignore everything but completed
				return;
			}
			if(REGISTERED_USERS.isEmpty()){
				LOGGER.debug("No listeners...");
				return;
			}
			
			Long taskId = event.getTaskId();
			Integer backendId = event.getBackendId();
			AbstractTaskDetails details = ServiceInitializer.getDAOHandler().getSQLDAO(PhotoTaskDAO.class).getTask(backendId, new DataGroups(DataGroups.DATA_GROUP_BASIC, Definitions.DATA_GROUP_BACKEND_STATUS), null, taskId);
			if(details == null){
				LOGGER.warn("Received task finished for non-existing task, id: "+taskId+", for back-end, id: "+backendId);
				return;
			}
			
			LOGGER.debug("Received "+service.tut.pori.contentanalysis.AsyncTask.TaskStatus.COMPLETED.name()+", sending notifications...");
			Long taskUserId = details.getUserIdValue();
			TaskFinishedService service = ServiceInitializer.getWebSocketHandler().getSocketService(TaskFinishedService.class);
			UserIdentity userId = new UserIdentity();
			TaskType taskType = event.getTaskType();
			TaskStatus taskStatus = new TaskStatus();
			taskStatus.setTaskId(taskId);
			taskStatus.setTaskType(taskType);
			taskStatus.setBackendStatusList(details.getBackends());
			for(Entry<Long, Registration> e : REGISTERED_USERS.entrySet()){
				Registration registration = e.getValue();
				if(((taskUserId == null && registration.isListenAnonymousTasks()) || registration.hasUserId(taskUserId)) && registration.hasBackendId(backendId) && registration.hasTaskId(taskId) && registration.hasTaskType(taskType)){
					userId.setUserId(e.getKey());
					service.send(userId, taskStatus);
				}
			}
		}
	} // class TaskFinishedListener
}
