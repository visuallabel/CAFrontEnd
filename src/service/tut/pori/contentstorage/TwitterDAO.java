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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import service.tut.pori.contentstorage.TwitterPhotoStorage.TwitterEntry;
import core.tut.pori.dao.clause.AndClause;
import core.tut.pori.dao.clause.SQLClause.SQLType;
import core.tut.pori.dao.SQLDAO;
import core.tut.pori.dao.SQLDeleteBuilder;
import core.tut.pori.dao.SQLSelectBuilder;
import core.tut.pori.users.UserIdentity;

/**
 * DAO for saving and retrieving Twitter content entries.
 *
 */
public class TwitterDAO extends SQLDAO {
	private static final Logger LOGGER = Logger.getLogger(TwitterDAO.class);
	/* tables */
	private static final String TABLE_ENTRIES = DATABASE+".ca_twitter_entries";
	/* columns */
	private static final String COLUMN_ENTITY_ID = "entity_id";
	private static final String COLUMN_ENTITY_URL = "entity_url";
	private static final String COLUMN_SCREEN_NAME = "screen_name";
	/* sql scripts */
	private static final String SQL_CREATE_ENTRY = "INSERT INTO "+TABLE_ENTRIES+" ("+COLUMN_GUID+", "+COLUMN_ENTITY_ID+", "+COLUMN_ENTITY_URL+", "+COLUMN_SCREEN_NAME+", "+COLUMN_USER_ID+", "+COLUMN_ROW_CREATED+") VALUES (?,?,?,?,?,NOW())";
	private static final int[] SQL_CREATE_ENTRY_SQL_TYPES = new int[]{SQLType.STRING.toInt(), SQLType.STRING.toInt(), SQLType.STRING.toInt(), SQLType.STRING.toInt(), SQLType.LONG.toInt()};
	
	private static final String[] SQL_SELECT_COLUMNS = {COLUMN_GUID, COLUMN_USER_ID, COLUMN_ENTITY_ID, COLUMN_ENTITY_URL, COLUMN_SCREEN_NAME};
	
	private static final String SQL_GET_URL = "SELECT "+COLUMN_COUNT+", "+COLUMN_ENTITY_URL+" FROM "+TABLE_ENTRIES+" WHERE "+COLUMN_GUID+"=?"; // add count to force result
	private static final int[] SQL_GET_URL_SQL_TYPES = {SQLType.STRING.toInt()};

	/**
	 * 
	 * @param entityIds optional list of entity ids used as a filter
	 * @param userId
	 * @return list of entries or null if none was found
	 */
	public List<TwitterEntry> getEntriesByEntityId(List<String> entityIds, UserIdentity userId) {
		if(!UserIdentity.isValid(userId)){
			LOGGER.warn("Invalid userId.");
			return null;
		}
		
		SQLSelectBuilder sql = new SQLSelectBuilder(TABLE_ENTRIES);
		sql.addSelectColumns(SQL_SELECT_COLUMNS);
		sql.addWhereClause(new AndClause(COLUMN_USER_ID, userId.getUserId(), SQLType.LONG));
		if(entityIds != null && !entityIds.isEmpty()){
			sql.addWhereClause(new AndClause(COLUMN_ENTITY_ID, entityIds, SQLType.STRING));
		}
		List<Map<String, Object>> rows = getJdbcTemplate().queryForList(sql.toSQLString(), sql.getValues(), sql.getValueTypes());
		
		if(rows.isEmpty()){
			LOGGER.debug("No results.");
			return null;
		}
		
		List<TwitterEntry> entries = new LinkedList<>();
		for(Map<String, Object> row : rows){
			entries.add(extractEntry(row));
		}
		
		return entries;
	}

	/**
	 * 
	 * @param row
	 * @return the entry extracted from the given row map
	 */
	private TwitterEntry extractEntry(Map<String, Object> row) {
		TwitterEntry te = new TwitterEntry();
		for(Entry<String, Object> e : row.entrySet()){
			String column = e.getKey();
			switch(column){
				case COLUMN_GUID:
					te.setGUID((String) e.getValue());
					break;
				case COLUMN_USER_ID:
					te.setUserId(new UserIdentity((Long) e.getValue()));
					break;
				case COLUMN_ENTITY_ID:
					te.setEntityId((String)e.getValue());
					break;
				case COLUMN_ENTITY_URL:
					te.setEntityUrl((String) e.getValue());
					break;
				case COLUMN_SCREEN_NAME:
					te.setScreenName((String) e.getValue());
					break;
				default:
					LOGGER.warn("Ignored unknown column: "+column);
					break;
			}
		}
		return te;
	}

	/**
	 * 
	 * @param screenNames optional screen name filter, if null or empty, the filter is ignored
	 * @param userId
	 * @return list of entries or null if none was found
	 */
	public List<TwitterEntry> getEntriesByScreenName(Collection<String> screenNames, UserIdentity userId) {
		if(!UserIdentity.isValid(userId)){
			LOGGER.warn("Invalid userId.");
			return null;
		}
	
		SQLSelectBuilder sql = new SQLSelectBuilder(TABLE_ENTRIES);
		sql.addSelectColumns(SQL_SELECT_COLUMNS);
		sql.addWhereClause(new AndClause(COLUMN_USER_ID, userId.getUserId(), SQLType.LONG));
		if(screenNames != null && !screenNames.isEmpty()){
			LOGGER.debug("Adding screen name filter...");
			sql.addWhereClause(new AndClause(COLUMN_SCREEN_NAME, screenNames, SQLType.STRING));
		}
		
		List<Map<String, Object>> rows = getJdbcTemplate().queryForList(sql.toSQLString(), sql.getValues(), sql.getValueTypes());
		if(rows.isEmpty()){
			LOGGER.debug("No results.");
			return null;
		}
		
		List<TwitterEntry> entries = new ArrayList<>(rows.size());
		for(Map<String, Object> row : rows){
			entries.add(extractEntry(row));
		}
		return entries;
	}

	/**
	 * 
	 * @param guid
	 * @return static URL for the given GUID or null if not found
	 */
	public String getUrl(String guid) {
		Map<String, Object> rows = getJdbcTemplate().queryForMap(SQL_GET_URL, new Object[]{guid}, SQL_GET_URL_SQL_TYPES);
		return (String) rows.get(COLUMN_ENTITY_URL);
	}

	/**
	 * 
	 * @param guids
	 */
	public void removeEntries(Collection<String> guids) {
		if(guids != null && !guids.isEmpty()){
			SQLDeleteBuilder sql = new SQLDeleteBuilder(TABLE_ENTRIES);
			sql.addWhereClause(new AndClause(COLUMN_GUID, guids, SQLType.STRING));
			LOGGER.debug("Removed entries count: "+getJdbcTemplate().update(sql.toSQLString(), sql.getValues(), sql.getValueTypes()));
		}else{
			LOGGER.debug("Empty GUID list.");
		}
	}

	/**
	 * 
	 * @param entries
	 */
	public void createEntries(Collection<TwitterEntry> entries) {
		if(entries == null || entries.isEmpty()){
			LOGGER.debug("Ignored empty entry list.");
			return;
		}
		Object[] ob = new Object[5];
		for(TwitterEntry e : entries){
			ob[0] = e.getGUID();
			ob[1] = e.getEntityId();
			ob[2] = e.getEntityUrl();
			ob[3] = e.getScreenName();
			ob[4] = e.getUserId().getUserId();
			getJdbcTemplate().update(SQL_CREATE_ENTRY, ob, SQL_CREATE_ENTRY_SQL_TYPES);
		}
	}
}
