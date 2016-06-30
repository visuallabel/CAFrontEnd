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
package service.tut.pori.contentstorage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import service.tut.pori.contentstorage.FacebookPhotoStorage.FacebookEntry;
import core.tut.pori.dao.clause.AndClause;
import core.tut.pori.dao.clause.SQLClause.SQLType;
import core.tut.pori.dao.SQLDAO;
import core.tut.pori.dao.SQLDeleteBuilder;
import core.tut.pori.dao.SQLSelectBuilder;
import core.tut.pori.users.UserIdentity;

/**
 * DAO for saving and retrieving facebook content entries.
 *
 */
public class FacebookDAO extends SQLDAO{
	private static final Logger LOGGER = Logger.getLogger(FacebookDAO.class);
	private static final String TABLE_ENTRIES = DATABASE+".ca_facebook_entries";
	/* columns */
	private static final String COLUMN_STATIC_URL = "static_url";
	private static final String COLUMN_OBJECT_ID = "object_id";
	/* sql scripts */
	private static final String[] SQL_SELECT_COLUMNS = new String[]{COLUMN_GUID, COLUMN_STATIC_URL, COLUMN_OBJECT_ID};
	
	private static final String SQL_CREATE_ENTRY = "INSERT INTO "+TABLE_ENTRIES+" ("+COLUMN_GUID+", "+COLUMN_STATIC_URL+", "+COLUMN_OBJECT_ID+", "+COLUMN_USER_ID+", "+COLUMN_ROW_CREATED+") VALUES (?,?,?,?,NOW())";
	private static final int[] SQL_CREATE_ENTRY_SQL_TYPES = new int[]{SQLType.STRING.toInt(), SQLType.STRING.toInt(), SQLType.STRING.toInt(), SQLType.LONG.toInt()};
	
	private static final String SQL_GET_ENTRIES = "SELECT "+StringUtils.join(SQL_SELECT_COLUMNS, ',')+" FROM "+TABLE_ENTRIES+" WHERE "+COLUMN_USER_ID+"=?";
	private static final int[] SQL_GET_ENTRIES_SQL_TYPES = new int[]{SQLType.STRING.toInt()};
	
	private static final String SQL_GET_URL = "SELECT "+COLUMN_COUNT+", "+COLUMN_STATIC_URL+" FROM "+TABLE_ENTRIES+" WHERE "+COLUMN_GUID+"=?"; // add count to force result
	private static final int[] SQL_GET_URL_SQL_TYPES = new int[]{SQLType.STRING.toInt()};
	
	/**
	 * @param entries
	 */
	public void createEntries(Collection<FacebookEntry> entries){
		if(entries == null || entries.isEmpty()){
			LOGGER.debug("Ignored empty entry list.");
			return;
		}
		Object[] ob = new Object[4];
		for(FacebookEntry e : entries){
			ob[0] = e.getGUID();
			ob[1] = e.getStaticUrl();
			ob[2] = e.getObjectId();
			ob[3] = e.getUserId().getUserId();
			getJdbcTemplate().update(SQL_CREATE_ENTRY, ob, SQL_CREATE_ENTRY_SQL_TYPES);
		}	
	}
	
	/**
	 * 
	 * @param guid
	 * @return static URL for the GUID or null if none was found
	 */
	public String getUrl(String guid){
		Map<String, Object> rows = getJdbcTemplate().queryForMap(SQL_GET_URL, new Object[]{guid}, SQL_GET_URL_SQL_TYPES);
		return (String) rows.get(COLUMN_STATIC_URL);
	}
	
	/**
	 * 
	 * @param userId
	 * @return entries or null if none
	 */
	public List<FacebookEntry> getEntries(UserIdentity userId){
		if(!UserIdentity.isValid(userId)){
			LOGGER.debug("Invalid userId.");
			return null;
		}
		
		List<Map<String, Object>> rows = getJdbcTemplate().queryForList(SQL_GET_ENTRIES, new Object[]{userId.getUserId()}, SQL_GET_ENTRIES_SQL_TYPES);
		if(rows.isEmpty()){
			LOGGER.debug("No results.");
			return null;
		}
		
		List<FacebookEntry> entries = new ArrayList<>(rows.size());
		for(Map<String, Object> row : rows){
			entries.add(extractEntry(row));
		}
		return entries;
	}
	
	/**
	 * 
	 * @param objectIds list of facebook object ids used as a filter
	 * @param userId
	 * @return entries or null if none was found
	 */
	public List<FacebookEntry> getEntries(Collection<String> objectIds, UserIdentity userId){
		if(objectIds == null || objectIds.isEmpty()){
			LOGGER.debug("No ids given.");
			return getEntries(userId);
		}
		
		if(!UserIdentity.isValid(userId)){
			LOGGER.debug("Invalid userId.");
			return null;
		}
		
		SQLSelectBuilder sql = new SQLSelectBuilder(TABLE_ENTRIES);
		sql.addSelectColumns(SQL_SELECT_COLUMNS);
		sql.addWhereClause(new AndClause(COLUMN_USER_ID, userId.getUserId(), SQLType.LONG));
		sql.addWhereClause(new AndClause(COLUMN_OBJECT_ID, objectIds, SQLType.STRING));
		List<Map<String, Object>> rows = getJdbcTemplate().queryForList(sql.toSQLString(), sql.getValues(), sql.getValueTypes());
		
		if(rows.isEmpty()){
			LOGGER.debug("No results.");
			return null;
		}
		
		List<FacebookEntry> entries = new LinkedList<>();
		for(Map<String, Object> row : rows){
			entries.add(extractEntry(row));
		}
		return entries;
	}
	
	/**
	 * 
	 * @param row
	 * @return entry extracted from the row map
	 */
	private FacebookEntry extractEntry(Map<String, Object> row){
		FacebookEntry fe = new FacebookEntry();
		for(Entry<String, Object> e : row.entrySet()){
			String column = e.getKey();
			switch(column){
				case COLUMN_GUID:
					fe.setGUID((String) e.getValue());
					break;
				case COLUMN_OBJECT_ID:
					fe.setObjectId((String) e.getValue());
					break;
				case COLUMN_STATIC_URL:
					fe.setStaticUrl((String) e.getValue());
					break;
				case COLUMN_USER_ID:
					fe.setUserId(new UserIdentity((Long) e.getValue()));
					break;
				default:
					if(checkCountColumn(column, e.getValue()) < 1){
						LOGGER.debug("No results or unknown column.");
						return null;
					}
					break;
			} // switch
		}
		return fe;
	}
	
	/**
	 * 
	 * @param guids
	 */
	public void removeEntries(Collection<String> guids){
		if(guids != null && !guids.isEmpty()){
			SQLDeleteBuilder sql = new SQLDeleteBuilder(TABLE_ENTRIES);
			sql.addWhereClause(new AndClause(COLUMN_GUID, guids, SQLType.STRING));
			LOGGER.debug("Removed entries count: "+getJdbcTemplate().update(sql.toSQLString(), sql.getValues(), sql.getValueTypes()));
		}else{
			LOGGER.debug("Empty GUID list.");
		}
	}
}
