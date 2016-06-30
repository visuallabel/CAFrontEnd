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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;

import service.tut.pori.contentstorage.PicasaCloudStorage.PicasaEntry;
import core.tut.pori.dao.clause.AndClause;
import core.tut.pori.dao.clause.SQLClause.SQLType;
import core.tut.pori.dao.SQLDAO;
import core.tut.pori.dao.SQLDeleteBuilder;

/**
 * DAO for saving and retrieving Picasa content entries.
 */
public class PicasaDAO extends SQLDAO{
	private static final Logger LOGGER = Logger.getLogger(PicasaDAO.class);
	/* tables */
	private static final String TABLE_PICASA_ENTRIES = DATABASE+".ca_picasa_entries";
	/* columns */
	private static final String COLUMN_ALBUM_ID = "album_id";
	private static final String COLUMN_GOOGLE_USER_ID = "google_user_id";
	private static final String COLUMN_PHOTO_ID = "photo_id";
	private static final String COLUMN_STATIC_URL = "static_url";
	/* sql scripts */
	private static final String SQL_CREATE_ENTRY = "INSERT INTO "+TABLE_PICASA_ENTRIES+" ("+COLUMN_GUID+", "+COLUMN_ALBUM_ID+", "+COLUMN_GOOGLE_USER_ID+", "+COLUMN_PHOTO_ID+", "+COLUMN_STATIC_URL+", "+COLUMN_ROW_CREATED+") VALUES (?,?,?,?,?,NOW())";
	private static final int[] SQL_CREATE_ENTRY_SQL_TYPES = new int[]{SQLType.STRING.toInt(), SQLType.STRING.toInt(), SQLType.STRING.toInt(), SQLType.STRING.toInt(), SQLType.STRING.toInt()};

	private static final String SQL_GET_ENTRY_BY_GUID = "SELECT "+COLUMN_COUNT+", "+COLUMN_GUID+", "+COLUMN_ALBUM_ID+", "+COLUMN_PHOTO_ID+", "+COLUMN_GOOGLE_USER_ID+", "+COLUMN_STATIC_URL+" FROM "+TABLE_PICASA_ENTRIES+" WHERE "+COLUMN_GUID+"=?";
	private static final int[] SQL_GET_ENTRY_BY_GUID_SQL_TYPES = new int[]{SQLType.STRING.toInt()};

	private static final String SQL_GET_ENTRY_BY_GOOGLE_ID = "SELECT "+COLUMN_GUID+", "+COLUMN_ALBUM_ID+", "+COLUMN_PHOTO_ID+", "+COLUMN_GOOGLE_USER_ID+" FROM "+TABLE_PICASA_ENTRIES+" WHERE "+COLUMN_GOOGLE_USER_ID+"=?";
	private static final int[] SQL_GET_ENTRY_BY_GOOGLE_ID_SQL_TYPES = new int[]{SQLType.STRING.toInt()};

	private static final String SQL_UPDATE_ENTRY = "UPDATE "+TABLE_PICASA_ENTRIES+" SET "+COLUMN_ALBUM_ID+"=?, "+COLUMN_PHOTO_ID+"=?, "+COLUMN_GOOGLE_USER_ID+"=?, "+COLUMN_STATIC_URL+"=?, "+COLUMN_ROW_UPDATED+"=NOW() WHERE "+COLUMN_GUID+"=?";
	private static final int[] SQL_UPDATE_ENTRY_SQL_TYPES = new int[]{SQLType.STRING.toInt(),SQLType.STRING.toInt(),SQLType.STRING.toInt(),SQLType.STRING.toInt(),SQLType.STRING.toInt()};


	/**
	 * 
	 * @param guid
	 * @return the entry or null if none
	 */
	public PicasaEntry getEntry(String guid){
		List<Map<String,Object>> rows = getJdbcTemplate().queryForList(SQL_GET_ENTRY_BY_GUID, new Object[]{guid}, SQL_GET_ENTRY_BY_GUID_SQL_TYPES);
		if(rows.isEmpty()){
			return null;
		}else{
			return extractEntry(rows.get(0));
		}
	}

	/**
	 * 
	 * @param googleUserId
	 * @return list of entries or null if none
	 */
	public List<PicasaEntry> getEntries(String googleUserId){
		List<Map<String,Object>> rows = getJdbcTemplate().queryForList(SQL_GET_ENTRY_BY_GOOGLE_ID, new Object[]{googleUserId}, SQL_GET_ENTRY_BY_GOOGLE_ID_SQL_TYPES);
		if(rows.isEmpty()){
			return null;
		}else{
			List<PicasaEntry> entries = new ArrayList<>(rows.size());
			for(Iterator<Map<String, Object>> rowIter = rows.iterator(); rowIter.hasNext();){
				PicasaEntry e = extractEntry(rowIter.next());
				if(e == null){
					LOGGER.warn("Failed to extract entry for Google user, id: "+googleUserId);
				}else{
					entries.add(e);
				}
			}
			if(entries.isEmpty()){
				LOGGER.warn("Could not get any entries for Google user, id: "+googleUserId);
				return null;
			}else{
				return entries;
			}
		}
	}

	/**
	 * 
	 * @param row
	 * @return the entry extracted from the given row map
	 */
	private PicasaEntry extractEntry(Map<String, Object> row){
		PicasaEntry pe = new PicasaEntry();
		for(Entry<String, Object> e : row.entrySet()){
			String columnName = e.getKey();
			switch(columnName){
				case COLUMN_ALBUM_ID:
					pe.setAlbumId((String) e.getValue());
					break;
				case COLUMN_GUID:
					pe.setGUID((String) e.getValue());
					break;
				case COLUMN_GOOGLE_USER_ID:
					pe.setGoogleUserId((String) e.getValue());
					break;
				case COLUMN_PHOTO_ID:
					pe.setPhotoId((String) e.getValue());
					break;
				case COLUMN_STATIC_URL:
					pe.setStaticUrl((String) e.getValue());
					break;
				default:
					if(checkCountColumn(columnName, e.getValue()) < 1){
						LOGGER.debug("Unknown column name, or no results.");
						return null;
					}
					break;
			}	// switch
		}
		return pe;
	}

	/**
	 * 
	 * @param entries
	 */
	public void createEntries(Collection<PicasaEntry> entries) {
		JdbcTemplate t = getJdbcTemplate();
		Object[] ob = new Object[5];
		for(PicasaEntry e : entries){
			ob[0] = e.getGUID();
			ob[1] = e.getAlbumId();
			ob[2] = e.getGoogleUserId();
			ob[3] = e.getPhotoId();
			ob[4] = e.getStaticUrl();
			t.update(SQL_CREATE_ENTRY, ob, SQL_CREATE_ENTRY_SQL_TYPES);
		}
	}

	/**
	 * This method will not abort on failure, all updated will be performed that are possible.
	 * 
	 * @param entries
	 * @return true on success, false on failure or partial failure
	 */
	public boolean updateEntries(List<PicasaEntry> entries) {
		JdbcTemplate t = getJdbcTemplate();
		Object[] ob = new Object[5];
		boolean retval = true;
		for(PicasaEntry e : entries){
			ob[0] = e.getAlbumId();
			ob[1] = e.getPhotoId();
			ob[2] = e.getGoogleUserId();
			ob[3] = e.getStaticUrl();
			ob[4] = e.getGUID();
			if(t.update(SQL_UPDATE_ENTRY, ob, SQL_UPDATE_ENTRY_SQL_TYPES) != 1){
				LOGGER.warn("Did not update entry, GUID: "+ob[3]);
				retval = false;
			}
		}
		return retval;
	}

	/**
	 * 
	 * @param entry
	 * @return true on success
	 */
	public boolean updateEntry(PicasaEntry entry){
		if(getJdbcTemplate().update(SQL_UPDATE_ENTRY, new Object[]{entry.getAlbumId(), entry.getPhotoId(), entry.getGoogleUserId(), entry.getStaticUrl(), entry.getGUID()}, SQL_UPDATE_ENTRY_SQL_TYPES) != 1){
			LOGGER.warn("Did not update entry, GUID: "+entry.getGUID());
			return false;
		}else{
			return true;
		}
	}

	/**
	 * Removes the entries
	 * 
	 * @param guids
	 */
	public void removeEntries(List<String> guids) {
		if(guids != null && !guids.isEmpty()){
			SQLDeleteBuilder sql = new SQLDeleteBuilder(TABLE_PICASA_ENTRIES);
			sql.addWhereClause(new AndClause(COLUMN_GUID, guids, SQLType.STRING));
			LOGGER.debug("Entries removed: "+getJdbcTemplate().update(sql.toSQLString(), sql.getValues(), sql.getValueTypes()));
		}else{
			LOGGER.debug("Ignored empty GUID list.");
		}
	}
}
