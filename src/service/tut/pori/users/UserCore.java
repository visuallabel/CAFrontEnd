/**
 * Copyright 2014 Tampere University of Technology, Pori Department
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
package service.tut.pori.users;

import java.util.EnumSet;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import core.tut.pori.context.EventHandler;
import core.tut.pori.context.ServiceInitializer;
import core.tut.pori.http.ResponseData;
import core.tut.pori.users.ExternalAccountConnection;
import core.tut.pori.users.ExternalAccountConnection.UserServiceType;
import core.tut.pori.users.UserAuthority;
import core.tut.pori.users.UserEvent.EventType;
import core.tut.pori.users.UserIdentity;

/**
 * User Core methods.
 * 
 * This class emits events of type {@link service.tut.pori.users.UserServiceEvent} for user account modifications with one of the listed {@link core.tut.pori.users.UserEvent.EventType} :
 * <ul>
 *  <li>{@link core.tut.pori.users.UserEvent.EventType#USER_CREATED} for newly created user accounts.</li>
 *  <li>{@link core.tut.pori.users.UserEvent.EventType#USER_REMOVED} for removed user accounts.</li>
 *  <li>{@link core.tut.pori.users.UserEvent.EventType#USER_AUTHORIZATION_REVOKED} for removed external account connection. The external connection type will can be retrieved from the service type getter ({@link service.tut.pori.users.UserServiceEvent#getUserServiceType()})</li>
 * </ul>
 */
public class UserCore {
	private static final Logger LOGGER = Logger.getLogger(UserCore.class);
	
	/**
	 * The status of registration process.
	 *
	 */
	public enum RegistrationStatus{
		/** registeration completed successfully */
		OK,
		/** given username was invalid or reserved */
		BAD_USERNAME,
		/** given password was invalid (too short or contained invalid characters */
		BAD_PASSWORD,
		/** required data was not given */
		NULL_DATA, 
		/** Registeration attempt was forbidden. */
		FORBIDDEN;
		
		/**
		 * 
		 * @return this status as a string
		 */
		public String toStatusString(){
			return name();
		}
	} // enum RegistrationStatus
	
	/**
	 * 
	 */
	private UserCore(){
		// nothing needed
	}
	
	/**
	 * @param serviceTypes
	 * @param userId
	 * @throws IllegalArgumentException on bad values
	 */
	public static void deleteExternalAccountConnections(EnumSet<UserServiceType> serviceTypes, UserIdentity userId) throws IllegalArgumentException {
		if(!UserIdentity.isValid(userId)){
			throw new IllegalArgumentException("Invalid user identity.");
		}
		
		if(serviceTypes == null || serviceTypes.isEmpty()){
			LOGGER.warn("Ignored empty service type list.");
			return;
		}
		
		UserDAO userDao = ServiceInitializer.getDAOHandler().getDAO(UserDAO.class);
		EventHandler eventHandler = ServiceInitializer.getEventHandler();
		for(UserServiceType t : serviceTypes){
			if(userDao.deleteExternalAccountConnection(t, userId)){
				eventHandler.publishEvent(new UserServiceEvent(EventType.USER_AUTHORIZATION_REVOKED, t, UserCore.class, userId));
			}else{
				LOGGER.warn("Could not remove requested user service connection "+t.toUserServiceTypeString()+" for user, id: "+userId.getUserId());
			}
		}
	}
	
	/**
	 * 
	 * @param username
	 * @return user identity for the username or null if not found
	 */
	public static UserIdentity getUserIdentity(String username){
		return ServiceInitializer.getDAOHandler().getDAO(UserDAO.class).getUser(username);
	}
	
	/**
	 * 
	 * @param userId
	 * @return user identity for the given id or null if not found
	 */
	public static UserIdentity getUserIdentity(Long userId){
		return ServiceInitializer.getDAOHandler().getDAO(UserDAO.class).getUser(userId);
	}
	
	/**
	 * 
	 * @param serviceTypes optional service type filters
	 * @param userId
	 * @return list of connections for the given user or null if none was found
	 */
	public static ExternalAccountConnectionList getExternalAccountConnections(EnumSet<UserServiceType> serviceTypes, UserIdentity userId){
		return ServiceInitializer.getDAOHandler().getDAO(UserDAO.class).getExternalAccountConnections(serviceTypes, userId);
	}
	
	/**
	 *  Register a new user, checking for valid system registration password, if one is set in the system properties.
	 * 
	 * @param registration
	 * @return status
	 */
	public static RegistrationStatus register(Registration registration) {
		String registerPassword = ServiceInitializer.getPropertyHandler().getSystemProperties(UserServiceProperties.class).getRegisterPassword();
		if(!StringUtils.isBlank(registerPassword) && !registerPassword.equals(registration.getRegisterPassword())){
			LOGGER.warn("The given registeration password was invalid.");
			return RegistrationStatus.FORBIDDEN;
		}
		
		return createUser(registration);
	}
	
	/**
	 * 
	 * @param connection
	 * @return UserIdentity with the id value set or null if none is found
	 */
	public static UserIdentity getUserId(ExternalAccountConnection connection){
		return ServiceInitializer.getDAOHandler().getDAO(UserDAO.class).getUserId(connection);
	}
	
	/**
	 * 
	 * @param connection
	 * @param userId
	 * @throws IllegalArgumentException
	 */
	public static void insertExternalAccountConnection(ExternalAccountConnection connection, UserIdentity userId) throws IllegalArgumentException{
		if(!UserIdentity.isValid(userId)){
			throw new IllegalArgumentException("Bad userId.");
		}
		ServiceInitializer.getDAOHandler().getDAO(UserDAO.class).insertExternalAccountConnection(connection, userId);
	}
	
	/**
	 * Crate user based on the registration information. On success this will publish event notification for newly created user.
	 * 
	 * @param registration
	 * @return status
	 */
	public static RegistrationStatus createUser(Registration registration){
		RegistrationStatus status = Registration.isValid(registration);
		if(status != RegistrationStatus.OK){
			LOGGER.debug("Invalid registration.");
			return status;
		}
		
		UserIdentity userId = new UserIdentity(registration.getEncryptedPassword(), null, registration.getUsername());
		userId.addAuthority(UserAuthority.AUTHORITY_ROLE_USER); // add with role user
		if(ServiceInitializer.getDAOHandler().getDAO(UserDAO.class).addUser(userId)){
			registration.setRegisteredUserId(userId);
			ServiceInitializer.getEventHandler().publishEvent(new UserServiceEvent(EventType.USER_CREATED, null, UserCore.class, userId));
			return RegistrationStatus.OK;
		}else{
			LOGGER.debug("Failed to add new user: reserved username.");
			return RegistrationStatus.BAD_USERNAME;
		}
	}
	
	/**
	 * Remove the user from the system. Successful call will publish event notification for removed user.
	 * 
	 * @param userId
	 * @throws IllegalArgumentException
	 */
	public static void unregister(UserIdentity userId) throws IllegalArgumentException{
		if(ServiceInitializer.getDAOHandler().getDAO(UserDAO.class).removeUser(userId)){
			ServiceInitializer.getSessionHandler().removeSessionInformation(userId); // remove all user's sessions
			ServiceInitializer.getEventHandler().publishEvent(new UserServiceEvent(EventType.USER_REMOVED, null, UserCore.class, userId));
		}else{
			throw new IllegalArgumentException("Failed to remove user, id: "+userId.getUserId());
		}
	}
	
	/**
	 * 
	 * @param authenticatedUser
	 * @param userIdFilter optional filter for retrieving a list of users, if null, the details of the authenticatedUser will be returned
	 * @return user details for the requested userId or null if not available
	 * @throws IllegalArgumentException
	 */
	public static UserIdentityList getUserDetails(UserIdentity authenticatedUser, long[] userIdFilter) throws IllegalArgumentException{
		if(!UserIdentity.isValid(authenticatedUser)){
			throw new IllegalArgumentException("Bad authenticated user.");
		}
		Long authId = authenticatedUser.getUserId();
		UserDAO userDAO = ServiceInitializer.getDAOHandler().getDAO(UserDAO.class);
		authenticatedUser = userDAO.getUser(authId); // populate the details
		if(!UserIdentity.isValid(authenticatedUser)){
			LOGGER.warn("Could not resolve user identity for user, id: "+authId);
			throw new IllegalArgumentException("Bad authenticated user.");
		}
		
		if(ArrayUtils.isEmpty(userIdFilter) || (userIdFilter.length == 1 && userIdFilter[0] == authId)){ // if no filters have been given or the only filter is the authenticated user
			UserIdentityList list = new UserIdentityList();
			list.addUserId(authenticatedUser);
			return list;
		}
		
		if(!authenticatedUser.getAuthorities().contains(UserAuthority.AUTHORITY_ROLE_ADMIN)){
			LOGGER.warn("User, id: "+authId+" tried to access user details, but does not have the required role: "+UserAuthority.AUTHORITY_ROLE_ADMIN.getAuthority());
			return null;
		}else{
			return userDAO.getUsers(userIdFilter);
		}
	}
	
	/**
	 * User registration details.
	 *
	 */
	@XmlRootElement(name=Definitions.ELEMENT_REGISTRATION)
	@XmlAccessorType(XmlAccessType.NONE)
	public static class Registration extends ResponseData{
		@XmlElement(name=core.tut.pori.users.Definitions.ELEMENT_USERNAME)
		private String _username = null;
		private String _password = null;
		private String _encryptedPassword = null;
		private BCryptPasswordEncoder _encoder = null;
		@XmlElement(name=Definitions.ELEMENT_REGISTER_PASSWORD)
		private String _registerPassword = null;
		private UserIdentity _registeredUserId = null; // contains the registered user details after successful registration or null if not available
		
		/**
		 * @return the username
		 */
		public String getUsername() {
			return _username;
		}
		
		/**
		 * @param username the username to set
		 */
		public void setUsername(String username) {
			_username = username;
		}
		
		/**
		 * @return the password
		 */
		@XmlElement(name=Definitions.ELEMENT_PASSWORD)
		public String getPassword() {
			return _password;
		}
		
		/**
		 * 
		 * @return encrypted password
		 */
		public String getEncryptedPassword(){
			if(_password == null){
				LOGGER.debug("No password.");
				return null;
			}
			if(_encryptedPassword == null){
				if(_encoder == null){
					_encoder = new BCryptPasswordEncoder();
				}
				_encryptedPassword = _encoder.encode(_password);
			}
			return _encryptedPassword;
		}
		
		/**
		 * @param password the password to set
		 */
		public void setPassword(String password) {
			_encryptedPassword = null; // may have changed
			_password = password;
		}
		
		/**
		 * for sub-classing, use the static
		 * 
		 * @return true if the registration object is valid
		 */
		protected RegistrationStatus isValid(){
			if(StringUtils.isBlank(_username)){
				LOGGER.debug("No username.");
				return RegistrationStatus.BAD_USERNAME;
			}else if(StringUtils.isBlank(_password)){
				LOGGER.debug("No password.");
				return RegistrationStatus.BAD_PASSWORD;
			}else{
				return RegistrationStatus.OK;
			}
		}
		
		/**
		 * 
		 * @param registration can be null
		 * @return true if the passed registration object is valid
		 */
		public static RegistrationStatus isValid(Registration registration){
			if(registration == null){
				return RegistrationStatus.NULL_DATA;
			}else{
				return registration.isValid();
			}
		}

		/**
		 * Returns the registered user details after successful registration
		 * 
		 * @return the registeredUserId
		 */
		public UserIdentity getRegisteredUserId() {
			return _registeredUserId;
		}

		/**
		 * @param registeredUserId the registeredUserId to set
		 */
		protected void setRegisteredUserId(UserIdentity registeredUserId) {
			_registeredUserId = registeredUserId;
		}

		/**
		 * @return the registerPassword
		 */
		public String getRegisterPassword() {
			return _registerPassword;
		}

		/**
		 * @param registerPassword the registerPassword to set
		 */
		public void setRegisterPassword(String registerPassword) {
			_registerPassword = registerPassword;
		}
	} // class Registration
}
