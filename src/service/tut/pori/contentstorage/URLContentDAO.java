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
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import service.tut.pori.contentstorage.URLContentStorage.URLEntry;
import core.tut.pori.dao.clause.AndClause;
import core.tut.pori.dao.clause.SQLClause.SQLType;
import core.tut.pori.dao.SQLDAO;
import core.tut.pori.dao.SQLDeleteBuilder;
import core.tut.pori.dao.SQLSelectBuilder;
import core.tut.pori.users.UserIdentity;
import core.tut.pori.utils.MediaUrlValidator.MediaType;

/**
 * DAO used for storing and retrieving URL content.
 */
public class URLContentDAO extends SQLDAO {
	private static final Logger LOGGER = Logger.getLogger(URLContentDAO.class);
	/* tables */
	private static final String TABLE_URLS = DATABASE+".ca_url_storage_entries";
	/* columns */
	private static final String COLUMN_MEDIA_TYPE = "media_type";
	private static final String COLUMN_URL = "url";
	/* sql scripts */
	private static final String[] COLUMNS_ENTRY = {COLUMN_USER_ID, COLUMN_MEDIA_TYPE, COLUMN_URL, COLUMN_GUID};
	
	private static final String SQL_INSERT_URL = "INSERT INTO "+TABLE_URLS+" ("+COLUMN_GUID+", "+COLUMN_MEDIA_TYPE+", "+COLUMN_URL+", "+COLUMN_USER_ID+", "+COLUMN_ROW_CREATED+") VALUES (?,?,?,?,NOW())";
	private static final int[] SQL_INSERT_URL_SQL_TYPES = {SQLType.STRING.toInt(), SQLType.INTEGER.toInt(), SQLType.STRING.toInt(), SQLType.LONG.toInt()};
	
	private static final String SQL_GET_URL = "SELECT "+COLUMN_COUNT+","+COLUMN_URL+" FROM "+TABLE_URLS+" WHERE "+COLUMN_GUID+"=?"; // add count to force result
	private static final int[] SQL_GET_URL_SQL_TYPES = {SQLType.STRING.toInt()};
	
	/**
	 * 
	 * @param guids optional GUID filter
	 * @param mediaTypes optional media type filter
	 * @param urls optional url filter
	 * @param userId
	 * @return list of entries or null if none available
	 */
	public List<URLEntry> getEntries(Collection<String> guids, EnumSet<MediaType> mediaTypes, Collection<String> urls, UserIdentity userId){
		SQLSelectBuilder sql = new SQLSelectBuilder(TABLE_URLS);
		sql.addSelectColumns(COLUMNS_ENTRY);
		sql.addWhereClause(new AndClause(COLUMN_USER_ID, userId.getUserId(), SQLType.LONG));
		
		if(guids != null && !guids.isEmpty()){
			LOGGER.debug("Adding GUID filter...");
			sql.addWhereClause(new AndClause(COLUMN_GUID, guids, SQLType.STRING));
		}
		
		if(urls != null && !urls.isEmpty()){
			LOGGER.debug("Adding URL filter...");
			sql.addWhereClause(new AndClause(COLUMN_URL, urls, SQLType.STRING));
		}
		
		if(mediaTypes != null && !mediaTypes.isEmpty()){
			LOGGER.debug("Adding MediaType filter...");
			sql.addWhereClause(new AndClause(COLUMN_MEDIA_TYPE, MediaType.toInt(mediaTypes)));
		}
		
		List<Map<String, Object>> rows = getJdbcTemplate().queryForList(sql.toSQLString(), sql.getValues(), sql.getValueTypes());
		int count = rows.size();
		if(count < 1){
			LOGGER.debug("No known entries.");
			return null;
		}
		
		List<URLEntry> entries = new ArrayList<>();
		for(Map<String, Object> row : rows){
			entries.add(extractEntry(row));
		}
		
		return entries;
	}
	
	/**
	 * 
	 * @param row
	 * @return URL entry extracted from the given database row
	 */
	private URLEntry extractEntry(Map<String, Object> row){
		URLEntry ue = new URLEntry();
		for(Entry<String, Object> e : row.entrySet()){
			String column = e.getKey();
			switch(column){
				case COLUMN_GUID:
					ue.setGUID((String) e.getValue());
					break;
				case COLUMN_USER_ID:
					ue.setUserId(new UserIdentity((Long) e.getValue()));
					break;
				case COLUMN_MEDIA_TYPE:
					ue.setMediaType(MediaType.fromInt((int) e.getValue()));
					break;
				case COLUMN_URL:
					ue.setUrl((String) e.getValue());
					break;
				default:
					LOGGER.warn("Ignored unknown column: "+column);
					break;
			}
		}
		return ue;
	}
	
	/**
	 * @param entry
	 */
	public void addEntry(URLEntry entry){
		getJdbcTemplate().update(SQL_INSERT_URL, new Object[]{entry.getGUID(), entry.getMediaType().toInt(), entry.getUrl(), entry.getUserId().getUserId()}, SQL_INSERT_URL_SQL_TYPES);
	}
	
	/**
	 * 
	 * @param guid
	 * @return the URL or null if not found
	 */
	public String getUrl(String guid){
		return (String) getJdbcTemplate().queryForMap(SQL_GET_URL, new Object[]{guid}, SQL_GET_URL_SQL_TYPES).get(COLUMN_URL);
	}

	/**
	 * 
	 * @param guids
	 */
	public void removeEntries(Collection<String> guids) {
		if(guids == null || guids.isEmpty()){
			LOGGER.debug("Ignored empty GUID list.");
			return;
		}
		
		SQLDeleteBuilder sql = new SQLDeleteBuilder(TABLE_URLS);
		sql.addWhereClause(new AndClause(COLUMN_GUID, guids, SQLType.STRING));
		LOGGER.debug("Urls removed: "+sql.execute(getJdbcTemplate()));
	}
}
