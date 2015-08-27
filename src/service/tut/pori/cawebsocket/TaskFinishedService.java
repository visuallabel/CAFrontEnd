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

import org.apache.log4j.Logger;

import core.tut.pori.users.UserIdentity;
import core.tut.pori.utils.XMLFormatter;
import core.tut.pori.websocket.SocketService;

/**
 * Task Finished Service method definitions.
 * 
 */
public class TaskFinishedService extends SocketService {
	private static final Logger LOGGER = Logger.getLogger(TaskFinishedService.class);
	private XMLFormatter _formatter = new XMLFormatter();

	@Override
	public boolean accept(UserIdentity authenticatedUser) {
		LOGGER.debug("Accepting new user, id: "+authenticatedUser.getUserId());
		return true;
	}

	@Override
	public boolean accept() {
		LOGGER.debug("Not accepting unauthorized connection...");
		return false;
	}

	@Override
	public void received(UserIdentity authenticatedUser, String message) {
		CAWebSocketCore.taskFinishedRegistered(authenticatedUser, _formatter.toObject(message, Registration.class));
	}

	@Override
	public void received(String message) throws UnsupportedOperationException {
		throw new UnsupportedOperationException("Received message from unauthorized client."); // this should never be called
	}

	@Override
	public void disconnected(UserIdentity authenticatedUser) {
		CAWebSocketCore.taskFinishedUnregistered(authenticatedUser);
	}

	@Override
	public void disconnected() {
		LOGGER.warn("Unauthorized user disconnected."); // should never happen
	}

	@Override
	public String getEndPointName() {
		return Definitions.SERVICE_TASK_FINISHED;
	}

	/**
	 * 
	 * @param authenticatedUser
	 * @param taskStatus
	 * @return true on success
	 */
	public boolean send(UserIdentity authenticatedUser, TaskStatus taskStatus) {
		return super.send(authenticatedUser, _formatter.toString(taskStatus));
	}
}
