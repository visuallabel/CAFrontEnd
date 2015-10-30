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

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;

import core.tut.pori.dao.clause.AndClause;
import core.tut.pori.dao.clause.SQLClause.SQLType;
import core.tut.pori.dao.SQLDAO;
import core.tut.pori.dao.SQLSelectBuilder;
import core.tut.pori.users.ExternalAccountConnection;
import core.tut.pori.users.UserAuthority;
import core.tut.pori.users.ExternalAccountConnection.UserServiceType;
import core.tut.pori.users.UserIdentity;

/**
 * DAO for retrieving user details, and for creating new users. This class can also be used to modify existing users as well as list and modify user's external account connections.
 *
 */
public class UserDAO extends SQLDAO{
	private static final Logger LOGGER = Logger.getLogger(UserDAO.class);
	/* tables */
	private static final String TABLE_USERS = DATABASE+".users";
	private static final String TABLE_USERS_EXTERNAL_IDS = DATABASE+".users_external_ids";
	private static final String TABLE_USERS_ROLES = DATABASE+".users_roles";
	/* columns */
	private static final String COLUMN_EXTERNAL_ID = "external_id";
	private static final String COLUMN_PASSWORD_HASH = "password_hash";
	private static final String COLUMN_ROLE = "role";
	private static final String COLUMN_USERNAME = "username";
	private static final String COLUMN_USER_SERVICE_ID = "user_service_id";
	/* sql strings */
	private static final String SQL_ADD_USER_ROLE = "INSERT INTO "+TABLE_USERS_ROLES+" ("+COLUMN_USER_ID+", "+COLUMN_ROLE+", "+COLUMN_ROW_CREATED+") VALUES (?,?,NOW()) ON DUPLICATE KEY UPDATE "+COLUMN_ROW_UPDATED+"=NOW()";
	private static final int[] SQL_ADD_USER_ROLE_SQL_TYPES = {SQLType.LONG.toInt(), SQLType.STRING.toInt()};
	
	private static final String SQL_DELETE_EXTERNAL_ACCOUNT_CONNECTION = "DELETE FROM "+TABLE_USERS_EXTERNAL_IDS+" WHERE "+COLUMN_USER_ID+"=? AND "+COLUMN_USER_SERVICE_ID+"=?";
	private static final int[] SQL_DELETE_EXTERNAL_ACCOUNT_CONNECTION_TYPES = {SQLType.LONG.toInt(), SQLType.INTEGER.toInt()};
	
	private static final String[] SQL_GET_EXTERNAL_ACCOUNT_CONNECTIONS_COLUMNS = {COLUMN_EXTERNAL_ID, COLUMN_USER_SERVICE_ID};
	
	private static final String SQL_GET_EXTERNAL_ID = "SELECT "+COLUMN_EXTERNAL_ID+" FROM "+TABLE_USERS_EXTERNAL_IDS+" WHERE "+COLUMN_USER_ID+"=? AND "+COLUMN_USER_SERVICE_ID+"=? LIMIT 1";
	private static final int[] SQL_GET_EXTERNAL_ID_SQL_TYPES = {SQLType.LONG.toInt(), SQLType.INTEGER.toInt()};
	
	private static final String SQL_GET_USER_ID = "SELECT "+COLUMN_COUNT+", "+COLUMN_USER_ID+" FROM "+TABLE_USERS_EXTERNAL_IDS+" WHERE "+COLUMN_EXTERNAL_ID+"=? AND "+COLUMN_USER_SERVICE_ID+"=? LIMIT 1"; // count added to force result
	private static final int[] SQL_GET_USER_ID_SQL_TYPES = {SQLType.STRING.toInt(), SQLType.INTEGER.toInt()};
	
	private static final String SQL_GET_USER_ROLES = "SELECT "+COLUMN_ROLE+" FROM "+TABLE_USERS_ROLES+" WHERE "+COLUMN_USER_ID+"=?";
	private static final int[] SQL_GET_USER_ROLES_SQL_TYPES = {SQLType.LONG.toInt()};
	
	private static final String SQL_INSERT_EXTERNAL_ID = "INSERT INTO "+TABLE_USERS_EXTERNAL_IDS+" ("+COLUMN_USER_ID+", "+COLUMN_EXTERNAL_ID+", "+COLUMN_USER_SERVICE_ID+", "+COLUMN_ROW_CREATED+") VALUES (?,?,?,NOW())";
	private static final int[] SQL_INSERT_EXTERNAL_ID_SQL_TYPES = {SQLType.LONG.toInt(), SQLType.STRING.toInt(), SQLType.INTEGER.toInt()};
	
	private static final String SQL_REMOVE_EXTERNAL_IDS_BY_USER_ID = "DELETE FROM "+TABLE_USERS_EXTERNAL_IDS+" WHERE "+COLUMN_USER_ID+"=?";
	private static final String SQL_REMOVE_USER = "DELETE FROM "+TABLE_USERS+" WHERE "+COLUMN_USER_ID+"=?";
	private static final String SQL_REMOVE_USER_ROLES = "DELETE FROM "+TABLE_USERS_ROLES+" WHERE "+COLUMN_USER_ID+"=?";
	private static final int[] SQL_REMOVE_USER_SQL_TYPES = {SQLType.LONG.toInt()};
	
	private static final String[] SQL_SELECT_COLUMNS_GET_USERS = {COLUMN_USER_ID, COLUMN_USERNAME, COLUMN_PASSWORD_HASH};

	private static final String SQL_SELECT_BY_USERNAME = "SELECT "+COLUMN_COUNT+","+StringUtils.join(SQL_SELECT_COLUMNS_GET_USERS, ',')+" FROM "+TABLE_USERS+" WHERE "+COLUMN_USERNAME+"=? LIMIT 1";
	private static final int[] SQL_SELECT_BY_USER_NAME_SQL_TYPES = {SQLType.STRING.toInt()};
	
	private static final String SQL_SELECT_BY_USER_ID = "SELECT "+COLUMN_COUNT+", "+StringUtils.join(SQL_SELECT_COLUMNS_GET_USERS, ',')+" FROM "+TABLE_USERS+" WHERE "+COLUMN_USER_ID+"=? LIMIT 1";
	private static final int[] SQL_SELECT_BY_USER_ID_SQL_TYPES = {SQLType.LONG.toInt()};
	
	private static final String[] SQL_COLUMNS_ADD_USER = {COLUMN_USERNAME, COLUMN_PASSWORD_HASH, COLUMN_ROW_CREATED};
	
	/**
	 * 
	 * @param username
	 * @return the user or null if not found
	 */
	public UserIdentity getUser(String username) {
		LOGGER.debug("Searching user by username...");
		UserIdentity userIdentity = extractUserIdentity(getJdbcTemplate().queryForMap(SQL_SELECT_BY_USERNAME, new Object[]{username}, SQL_SELECT_BY_USER_NAME_SQL_TYPES));
		resolveRoles(userIdentity);
		return userIdentity;
	}
	
	/**
	 * 
	 * @param userId
	 * @return the user or null if not found
	 */
	public UserIdentity getUser(Long userId) {
		LOGGER.debug("Searching user by user id...");
		UserIdentity userIdentity = extractUserIdentity(getJdbcTemplate().queryForMap(SQL_SELECT_BY_USER_ID, new Object[]{userId}, SQL_SELECT_BY_USER_ID_SQL_TYPES));
		resolveRoles(userIdentity);
		return userIdentity;
	}
	
	/**
	 * Note: this will NOT resolve user roles
	 * 
	 * @param userIds
	 * @return list of matching user identities or null if none
	 */
	public UserIdentityList getUsers(long[] userIds){
		SQLSelectBuilder sql = new SQLSelectBuilder(TABLE_USERS);
		sql.addSelectColumns(SQL_SELECT_COLUMNS_GET_USERS);
		sql.addWhereClause(new AndClause(COLUMN_USER_ID, userIds));
		List<Map<String, Object>> rows = getJdbcTemplate().queryForList(sql.toSQLString(), sql.getValues(), sql.getValueTypes());
		if(rows.isEmpty()){
			return null;
		}
		UserIdentityList list = new UserIdentityList();
		for(Map<String,Object> row : rows){
			list.addUserId(extractUserIdentity(row));
		}
		if(UserIdentityList.isEmpty(list)){
			LOGGER.warn("Row count was !=0, but failed to extract any results.");
			return null;
		}
		return list;
	}
	
	/**
	 * Resolve and set user roles for the given user id. This is a helper method.
	 * 
	 * @param userId
	 */
	private void resolveRoles(UserIdentity userId){
		if(!UserIdentity.isValid(userId)){
			LOGGER.debug("Ignored invalid user identity.");
			return;
		}
		
		for(String role : getJdbcTemplate().queryForList(SQL_GET_USER_ROLES, new Object[]{userId.getUserId()}, SQL_GET_USER_ROLES_SQL_TYPES, String.class)){
			userId.addAuthority(UserAuthority.getGrantedAuthority(role));
		}
	}
	
	/**
	 * 
	 * @param connection
	 * @return UserIdentity with the id value set or null if none is found
	 */
	public UserIdentity getUserId(ExternalAccountConnection connection){
		Long userId = (Long) getJdbcTemplate().queryForMap(SQL_GET_USER_ID, new Object[]{connection.getExternalId(), connection.getServiceType().toInt()}, SQL_GET_USER_ID_SQL_TYPES).get(COLUMN_USER_ID);
		if(userId == null){
			return null;
		}else{
			return new UserIdentity(userId);
		}
	}
	
	/**
	 * 
	 * @param connection
	 * @param userId
	 * @throws IllegalArgumentException
	 */
	public void insertExternalAccountConnection(final ExternalAccountConnection connection, final UserIdentity userId) throws IllegalArgumentException{
		getTransactionTemplate().execute(new TransactionCallback<Void>() {
			@Override
			public Void doInTransaction(TransactionStatus status) {
				Long userIdValue = userId.getUserId();
				String externalId = connection.getExternalId();
				UserServiceType userServiceType = connection.getServiceType();
				
				UserIdentity existing = getUserId(connection);
				if(existing != null){
					if(UserIdentity.equals(userId, existing)){
						LOGGER.debug("Id already connected for the given user.");
						return null;
					}else{
						LOGGER.debug("Cannot connect external id: "+externalId+" of "+UserServiceType.class.toString()+" : "+userServiceType.name()+" for user, id: "+userIdValue+". Already connected with another account, id: "+existing.getUserId());
						throw new IllegalArgumentException("Failed to connect account, bad external id.");
					}
				}
				getJdbcTemplate().update(SQL_INSERT_EXTERNAL_ID, new Object[]{userIdValue, externalId, userServiceType.toInt()}, SQL_INSERT_EXTERNAL_ID_SQL_TYPES);
				return null;
			}
		});
	}
	
	/**
	 * Remove the user and all external id connections.
	 * 
	 * @param userId
	 * @return true if user of the given id was removed
	 */
	public boolean removeUser(UserIdentity userId){
		Object[] ob = new Object[]{userId.getUserId()};
		JdbcTemplate t = getJdbcTemplate();
		if(t.update(SQL_REMOVE_USER, ob, SQL_REMOVE_USER_SQL_TYPES) != 1){
			LOGGER.warn("Nothing was removed.");
			return false;
		}
		if(t.update(SQL_REMOVE_USER_ROLES, ob, SQL_REMOVE_USER_SQL_TYPES) < 1){
			LOGGER.warn("No roles were removed.");
		}
		LOGGER.debug("Removed "+t.update(SQL_REMOVE_EXTERNAL_IDS_BY_USER_ID, ob, SQL_REMOVE_USER_SQL_TYPES)+" external ids for user, id: "+ob[0]);
		return true;
	}
	
	/**
	 * On success, the passed userId will have the generated userId value set.
	 * 
	 * @param userId must have username, password and role set
	 * @return true on success, generally means invalid username, as generic database errors will throw an exception
	 * @throws IllegalArgumentException on bad userId
	 */
	public boolean addUser(final UserIdentity userId) throws IllegalArgumentException{
		final Collection<? extends GrantedAuthority> authorities = userId.getAuthorities();
		if(authorities == null || authorities.isEmpty()){ // there must be at least one authority
			throw new IllegalArgumentException("Invalid authorities.");
		}
		
		return getTransactionTemplate().execute(new TransactionCallback<Boolean>() {
			@Override
			public Boolean doInTransaction(TransactionStatus status) {
				String username = userId.getUsername();
				UserIdentity check = getUser(username);
				if(check != null){
					LOGGER.debug("Username already in use: "+username);
					return false;
				}
				
				SimpleJdbcInsert userInsert = new SimpleJdbcInsert(getJdbcTemplate());
				userInsert.withTableName(TABLE_USERS);
				userInsert.setGeneratedKeyName(COLUMN_USER_ID);
				userInsert.usingColumns(SQL_COLUMNS_ADD_USER);
				userInsert.withoutTableColumnMetaDataAccess();
				
				HashMap<String, Object> parameters = new HashMap<>(SQL_COLUMNS_ADD_USER.length);
				parameters.put(COLUMN_USERNAME, userId.getUsername());
				parameters.put(COLUMN_PASSWORD_HASH, userId.getPassword());
				parameters.put(COLUMN_ROW_CREATED, null);
				Number key = userInsert.executeAndReturnKey(parameters);
				if(key == null){
					LOGGER.error("Failed to add new user.");
					return false;
				}
				
				userId.setUserId(key.longValue());
				for(GrantedAuthority authority : authorities){
					addUserRole(userId, authority);
				}
				
				return true;
			}
		});
	}
	
	/**
	 * Helper method for adding a new user role. Duplicate roles are ignored (no-op).
	 * 
	 * Note: this will NOT check for the existence of the given user.
	 * 
	 * @param userId
	 * @param authority
	 */
	private void addUserRole(UserIdentity userId, GrantedAuthority authority){
		getJdbcTemplate().update(SQL_ADD_USER_ROLE, new Object[]{userId.getUserId(), authority.getAuthority()}, SQL_ADD_USER_ROLE_SQL_TYPES);
	}

	/**
	 * extracts the contents of the given row
	 * 
	 * @param row
	 * @return the user or null if null or empty map was passed
	 */
	private UserIdentity extractUserIdentity(Map<String,Object> row){
		if(row.isEmpty()){
			LOGGER.debug("Row contains no columns.");
			return null;
		}
		
		UserIdentity userId = new UserIdentity();
		for(Entry<String,Object> entry : row.entrySet()){
			switch(entry.getKey()){
				case COLUMN_USER_ID:
					userId.setUserId((Long) entry.getValue());
					break;
				case COLUMN_USERNAME:
					userId.setUsername((String) entry.getValue());
					break;
				case COLUMN_PASSWORD_HASH:
					userId.setPassword((String) entry.getValue());
					break;
				default:				
					if(checkCountColumn(entry.getKey(), entry.getValue()) < 1){	// this should be count(*)
						LOGGER.debug("Unknown column name, or no results.");
						return null;
					}
					break;
			}
		}
		return userId;
	}
	
	/**
	 * 
	 * @param serviceTypes optional service type filter
	 * @param userId
	 * @return list of connections or null if none found
	 */
	public ExternalAccountConnectionList getExternalAccountConnections(EnumSet<UserServiceType> serviceTypes, UserIdentity userId) {
		SQLSelectBuilder sql = new SQLSelectBuilder(TABLE_USERS_EXTERNAL_IDS);
		sql.addSelectColumns(SQL_GET_EXTERNAL_ACCOUNT_CONNECTIONS_COLUMNS);
		sql.addWhereClause(new AndClause(COLUMN_USER_ID, userId.getUserId(), SQLType.LONG));
		if(serviceTypes != null && !serviceTypes.isEmpty()){
			sql.addWhereClause(new AndClause(COLUMN_USER_SERVICE_ID, UserServiceType.toInt(serviceTypes)));
		}
		
		List<Map<String, Object>> rows = getJdbcTemplate().queryForList(sql.toSQLString(), sql.getValues(), sql.getValueTypes());
		if(rows.isEmpty()){
			return null;
		}
		ExternalAccountConnectionList list = new ExternalAccountConnectionList();
		for(Map<String, Object> row : rows){
			list.addConnection(extractExternalAccountConnection(row));
		}
		
		if(ExternalAccountConnectionList.isEmpty(list)){
			LOGGER.warn("Row count was !=0, but failed to extract any results.");
			return null;
		}else{
			return list;
		}
	}
	
	/**
	 * 
	 * @param userId
	 * @param serviceType
	 * @return the matching external account connection or null if none found
	 */
	public ExternalAccountConnection getExternalAccountConnection(UserIdentity userId, UserServiceType serviceType){
		List<String> externalIds = getJdbcTemplate().queryForList(SQL_GET_EXTERNAL_ID, new Object[]{userId.getUserId(), serviceType.toInt()}, SQL_GET_EXTERNAL_ID_SQL_TYPES, String.class);
		if(externalIds.isEmpty()){
			LOGGER.debug("No external ids found for user, id: "+userId.getUserId());
			return null;
		}
		return new ExternalAccountConnection(externalIds.get(0), serviceType);
	}
	
	/**
	 * 
	 * @param row
	 * @return the connection or null if extraction was not possible
	 */
	private ExternalAccountConnection extractExternalAccountConnection(Map<String, Object> row){
		if(row.isEmpty()){
			LOGGER.debug("Row contains no columns.");
			return null;
		}
		
		ExternalAccountConnection connection = new ExternalAccountConnection();
		for(Entry<String, Object> e : row.entrySet()){
			switch(e.getKey()){
				case COLUMN_EXTERNAL_ID:
					connection.setExternalId((String) e.getValue());
					break;
				case COLUMN_USER_SERVICE_ID:
					connection.setServiceType(UserServiceType.fromInt((Integer)e.getValue()));
					break;
				default:
					if(checkCountColumn(e.getKey(), e.getValue()) < 1){	// this should be count(*)
						LOGGER.debug("Unknown column name, or no results.");
						return null;
					}
					break;	
			} // switch
		} // for
		return connection;
	}

	/**
	 * 
	 * @param userServiceType
	 * @param userId
	 * @return true if connection was removed. Note that false means that the connection did not exist, on database error an appropriate exception will be thrown.
	 */
	public boolean deleteExternalAccountConnection(UserServiceType userServiceType, UserIdentity userId) {
		return (getJdbcTemplate().update(SQL_DELETE_EXTERNAL_ACCOUNT_CONNECTION, new Object[]{userId.getUserId(), userServiceType.toInt()}, SQL_DELETE_EXTERNAL_ACCOUNT_CONNECTION_TYPES) > 0);
	}
}
