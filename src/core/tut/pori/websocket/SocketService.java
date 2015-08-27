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
package core.tut.pori.websocket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.websocket.CloseReason;
import javax.websocket.Session;

import org.apache.log4j.Logger;

import core.tut.pori.users.UserIdentity;

/**
 * 
 * An abstract class for handling simple text/broadcast to users.
 * 
 * Sub-classing this class will automatically add an instance to the WebSocketHandler, retrievable through ServiceInitializer.
 * 
 * 
 */
public abstract class SocketService {
	private static final Logger LOGGER = Logger.getLogger(SocketService.class);
	private Map<Long, List<Session>> AUTHORIZED_USERS = new HashMap<>(); // userId, sessions map
	private List<Session> UNAUTHORIZED_USERS = new ArrayList<>();
	
	/**
	 * The default implementation will simply print the exception to log and close the session.
	 * 
	 * @param session
	 * @param throwable 
	 */
	public void onError(Session session, Throwable throwable){
		LOGGER.debug(throwable.toString());
		onClose(session, null);
	}
	
	/**
	 * Close all sessions belonging to the given user identity. 
	 * Note that calling this method may or may not cause disconnected() to be called depending on whether the user has active sessions or not.
	 * 
	 * @param authorizedUser
	 * @param closeReason
	 */
	public void close(UserIdentity authorizedUser, CloseReason closeReason){
		Long userId = authorizedUser.getUserId();
		synchronized (AUTHORIZED_USERS) {
			List<Session> sessions = AUTHORIZED_USERS.get(userId);
			if(sessions == null){
				LOGGER.debug("No sessions for user, id: "+userId);
				return;
			}
			for(Session session : sessions){
				try {
					session.close(closeReason);
				} catch (IOException ex) {
					LOGGER.error(ex, ex); // simply log the exception, the session will be automatically removed in onClose
				}
			} // for
		} // synchronized
	}
	
	/**
	 * 
	 * @param session
	 * @return true on successful accept, false on rejection. Upon rejection the session will be automatically closed.
	 */
	public boolean accept(Session session){
		UserIdentity userIdentity = (UserIdentity) session.getUserPrincipal();
		if(userIdentity == null){
			if(accept()){
				synchronized (UNAUTHORIZED_USERS) {
					UNAUTHORIZED_USERS.add(session);
				}
				LOGGER.debug("Added new unauthorized session.");
				return true;
			}else{
				return false;
			}
		}else if(accept(userIdentity)){
			Long userId = userIdentity.getUserId();
			synchronized (AUTHORIZED_USERS) {
				List<Session> sessions = AUTHORIZED_USERS.get(userId);
				if(sessions == null){
					AUTHORIZED_USERS.put(userId, (sessions = new ArrayList<>()));
				}
				sessions.add(session);
			}
			LOGGER.debug("Added new authorized session for user, id: "+userId);
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * 
	 * @param session
	 * @param closeReason
	 */
	public void onClose(Session session, CloseReason closeReason){
		LOGGER.debug("Closing session, reason: "+(closeReason == null ? "unknown" : closeReason.getCloseCode()+" "+closeReason.getReasonPhrase()));
		
		UserIdentity userIdentity = (UserIdentity) session.getUserPrincipal();
		if(userIdentity == null){
			synchronized (UNAUTHORIZED_USERS) {
				if(!UNAUTHORIZED_USERS.remove(session)){
					LOGGER.warn("Failed to remove session for an unauthorized user.");
				}
			}
			disconnected();
		}else{
			Long userId = userIdentity.getUserId();
			synchronized (AUTHORIZED_USERS) {
				List<Session> sessions = AUTHORIZED_USERS.get(userId);
				if(sessions == null){
					LOGGER.warn("No known sessions for user, id: "+userId);
				}else if(!sessions.remove(session)){ // the sessions list may not always have the closed session. This may happen e.g. if the connection was outright rejected or broken whilst establishing the connection
					LOGGER.warn("Failed to remove closed session for user, id: "+userId);
				}
				
				if(sessions.isEmpty()){
					AUTHORIZED_USERS.remove(userId);
				}
			} // synchronized
			disconnected(userIdentity);
		} // else
	}
	
	/**
	 * 
	 * @param session
	 * @param message
	 */
	public void received(Session session, String message){
		UserIdentity userIdentity = (UserIdentity) session.getUserPrincipal();
		if(userIdentity == null){
			received(message);
		}else{
			received(userIdentity, message);
		}
	}
	
	/**
	 * Send message to the given authorized user
	 * 
	 * @param authenticatedUser
	 * @param message
	 * @return true if the message was successfully sent to at least one of the user's sessions
	 */
	public boolean send(UserIdentity authenticatedUser, String message){
		Long userId = authenticatedUser.getUserId();
		boolean retval = false;
		synchronized (AUTHORIZED_USERS) {
			List<Session> sessions = AUTHORIZED_USERS.get(userId);
			if(sessions == null){
				LOGGER.warn("The user has no valid sessions.");
			}else{
				for(Session session : sessions){
					try {
						session.getBasicRemote().sendText(message);
						retval = true;
					} catch (IOException ex) {
						LOGGER.warn(ex, ex); // simply print the message, if the connection is broken, a call to onClose should follow
					}
				} // for
			} // else
		}
		return retval;
	}
	
	/**
	 * Send message to all unauthorized users
	 * 
	 * @param message
	 */
	public void send(String message){
		synchronized (UNAUTHORIZED_USERS) {
			for(Session session : UNAUTHORIZED_USERS){
				try {
					session.getBasicRemote().sendText(message);
				} catch (IOException ex) {
					LOGGER.warn(ex, ex); // simply print the message, if the connection is broken, a call to onClose should follow
				}
			}
		}
	}
	
	/**
	 * Send message to all connected users.
	 * 
	 * @param message
	 */
	public void broadcast(String message){
		LOGGER.debug("Sending message to all authorized users.");
		synchronized (AUTHORIZED_USERS) {
			for(List<Session> sessionList : AUTHORIZED_USERS.values()){
				for(Session session : sessionList){
					try {
						session.getBasicRemote().sendText(message);
					} catch (IOException ex) {
						LOGGER.warn(ex, ex); // simply print the message, if the connection is broken, a call to onClose should follow
					}
				} // for
			} // for
		} // synchronized
		
		LOGGER.debug("Sending message to all unauthorized users.");
		send(message);
	}
	
	/**
	 * 
	 * @param authenticatedUser
	 * @return true if the given user has active sessions. Note that in this case "active" is loosely defined, it is possible that the session has timed out, but the socket service has not yet registered the drop.
	 */
	public boolean hasSessions(UserIdentity authenticatedUser){
		synchronized (AUTHORIZED_USERS) {
			return (AUTHORIZED_USERS.get(authenticatedUser.getUserId()) != null);
		}
	}
	
	/**
	 * 
	 * @param authenticatedUser
	 * @return true if the connection for the given user should be accepted, otherwise, the connection will be closed
	 */
	public abstract boolean accept(UserIdentity authenticatedUser);

	/**
	 * 
	 * @return true if an anonymous connection should be accepted, otherwise, the connection will be closed
	 */
	public abstract boolean accept();

	/**
	 * 
	 * @param authenticatedUser
	 * @param message
	 */
	public abstract void received(UserIdentity authenticatedUser, String message);

	/**
	 * Message received for an anonymous user.
	 * 
	 * @param message
	 */
	public abstract void received(String message);

	/**
	 * The user has been disconnected.
	 * 
	 * @param authenticatedUser
	 */
	public abstract void disconnected(UserIdentity authenticatedUser);

	/**
	 * An anonymous user has been disconnected.
	 */
	public abstract void disconnected();
	
	/**
	 * Note: the name is checked on initialization, changing the name run-time has no effect.
	 * 
	 * @return the end point name
	 */
	public abstract String getEndPointName();
}