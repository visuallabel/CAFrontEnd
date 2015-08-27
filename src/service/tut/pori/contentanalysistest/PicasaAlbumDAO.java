/**
 * Copyright 2015 Tampere University of Technology, Pori Unit
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
package service.tut.pori.contentanalysistest;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import core.tut.pori.dao.clause.AndClause;
import core.tut.pori.dao.clause.SQLClause.SQLType;
import core.tut.pori.dao.SQLDAO;
import core.tut.pori.dao.SQLSelectBuilder;

/**
 * Class for handling picasa album information.
 * 
 * By-passes {@link service.tut.pori.contentstorage.PicasaDAO}
 * 
 */
@Deprecated
public class PicasaAlbumDAO extends SQLDAO {
	private static final Logger LOGGER = Logger.getLogger(PicasaAlbumDAO.class);
	/* tables */
	private static final String TABLE_PICASA_ENTRIES = DATABASE+".ca_picasa_entries";
	/* columns */
	private static final String COLUMN_ALBUM_ID = "album_id";
	private static final String COLUMN_GOOGLE_USER_ID = "google_user_id";
	private static final String COLUMN_PHOTO_ID = "photo_id";
	
	private static final String SQL_GET_IDS = "SELECT "+COLUMN_COUNT+", "+COLUMN_ALBUM_ID+", "+COLUMN_PHOTO_ID+" FROM "+TABLE_PICASA_ENTRIES+" WHERE "+COLUMN_GOOGLE_USER_ID+"=? AND "+COLUMN_GUID+"=?";
	private static final int[] SQL_GET_IDS_SQL_TYPES = {SQLType.STRING.toInt(), SQLType.STRING.toInt()};
	
	private static final String SQL_GET_GUIDS = "SELECT "+COLUMN_GUID+" FROM "+TABLE_PICASA_ENTRIES+" WHERE "+COLUMN_ALBUM_ID+"=?";
	private static final int[] SQL_GET_GUIDS_SQL_TYPES = {SQLType.STRING.toInt()};
	
	/**
	 * 
	 * @param googleUserId
	 * @param guid
	 * @return album id/photoId pair for the given GUID or null if not found
	 */
	public Pair<String, String> getIdPair(String googleUserId, String guid){
		Map<String, Object> map = getJdbcTemplate().queryForMap(SQL_GET_IDS, new Object[]{googleUserId, guid}, SQL_GET_IDS_SQL_TYPES);
		String albumId = (String) map.get(COLUMN_ALBUM_ID);
		if(StringUtils.isBlank(albumId)){
			LOGGER.warn("Could not find album id for guid : "+guid+", google user id : "+googleUserId);
			return null;
		}
		String photoId = (String) map.get(COLUMN_PHOTO_ID);
		if(StringUtils.isBlank(photoId)){
			LOGGER.warn("Could not find photo id for guid : "+guid+", google user id : "+googleUserId);
			return null;
		}
		return Pair.of(albumId, photoId);
	}
	
	/**
	 * 
	 * @param albumIds optional filter
	 * @param googleUserId
	 * @return list of album ids or null if none was found
	 */
	public List<String> getAlbumIds(Collection<String> albumIds, String googleUserId){
		SQLSelectBuilder sql = new SQLSelectBuilder(TABLE_PICASA_ENTRIES);
		sql.addSelectColumn(COLUMN_ALBUM_ID);
		sql.addWhereClause(new AndClause(COLUMN_GOOGLE_USER_ID, googleUserId, SQLType.STRING));
		sql.addGroupBy(COLUMN_ALBUM_ID);
		
		if(albumIds != null && !albumIds.isEmpty()){
			sql.addWhereClause(new AndClause(COLUMN_ALBUM_ID, albumIds, SQLType.STRING));
		}
		
		
		List<String> ids = getJdbcTemplate().queryForList(sql.toSQLString(), sql.getValues(), sql.getValueTypes(), String.class);
		return (ids.isEmpty() ? null : ids);
	}
	
	/**
	 * 
	 * @param albumId
	 * @return list of GUIDs for the album or null if none was found
	 */
	public List<String> getGUIDs(String albumId){
		List<String> guids = getJdbcTemplate().queryForList(SQL_GET_GUIDS, new Object[]{albumId}, SQL_GET_GUIDS_SQL_TYPES, String.class);
		return (guids.isEmpty() ? null : guids);
	}
}
