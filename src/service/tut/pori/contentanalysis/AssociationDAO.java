/**
 * Copyright 2015 Tampere University of Technology, Pori Department
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
package service.tut.pori.contentanalysis;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowCallbackHandler;

import core.tut.pori.dao.SQLDAO;
import core.tut.pori.dao.SQLSelectBuilder;
import core.tut.pori.dao.clause.AndClause;
import core.tut.pori.dao.clause.SQLClause.SQLType;

/**
 * Used to associate media objects to photos.
 * 
 * Note that even though it is possible to retrieve an instance of this class through DAOHandler, it is not recommended to use this DAO to directly modify media object relations. An attempt to do so may cause undefined behavior.
 */
public class AssociationDAO extends SQLDAO {
	private static final Logger LOGGER = Logger.getLogger(AssociationDAO.class);
	private static final String TABLE_ASSOCIATIONS = DATABASE+".ca_media_object_associations";
	/* sql scripts */
	private static final int[] SQL_ASSOCIATE_SQL_TYPES = new int[]{SQLType.STRING.toInt(), SQLType.STRING.toInt()};
	private static final String[] SQL_ASSOCIATION_COLUMNS = new String[]{COLUMN_GUID, Definitions.COLUMN_MEDIA_OBJECT_ID};
	private static final String SQL_DEASSOCIATE = "DELETE FROM "+TABLE_ASSOCIATIONS+" WHERE "+COLUMN_GUID+"=? AND "+Definitions.COLUMN_MEDIA_OBJECT_ID+"=?";
	private static final int[] SQL_DEASSOCIATE_SQL_TYPES = SQL_ASSOCIATE_SQL_TYPES;
	private static final String SQL_DEASSOCIATE_BY_GUID = "DELETE FROM "+TABLE_ASSOCIATIONS+" WHERE "+COLUMN_GUID+"=?";
	private static final String SQL_DEASSOCIASTE_BY_MEDIA_OBJECT_ID = "DELETE FROM "+TABLE_ASSOCIATIONS+" WHERE "+Definitions.COLUMN_MEDIA_OBJECT_ID+"=?";
	private static final int[] SQL_DEASSOCIATE_BY_SQL_TYPES = new int[]{SQLType.STRING.toInt()};

	/**
	 * 
	 * @param mediaList
	 */
	public void associate(Collection<? extends Media> mediaList) {
		if(mediaList == null || mediaList.isEmpty()){
			LOGGER.debug("Empty media list.");
			return;
		}
		StringBuilder sql = new StringBuilder("INSERT INTO "+TABLE_ASSOCIATIONS+" ("+COLUMN_GUID+","+Definitions.COLUMN_MEDIA_OBJECT_ID+", "+COLUMN_ROW_CREATED+") VALUES ");
		List<Object> objects = new ArrayList<>();
		Date updated = new Date();
		for(Media media : mediaList){ // this is method is generally called when external accounts are synced, and the amount to of photos can be quite high, i.e. it is much faster to parse a new sql string manually than use template
			MediaObjectList mediaObjects = media.getMediaObjects();
			String guid = media.getGUID();
			if(!MediaObjectList.isEmpty(mediaObjects)){
				for(MediaObject o : mediaObjects.getMediaObjects()){
					sql.append("(?,?,?),");
					objects.add(guid);
					objects.add(o.getMediaObjectId());
					objects.add(updated);
				}
			}
		}
		if(objects.isEmpty()){
			LOGGER.debug("No objects to associate.");
			return;
		}

		sql.setLength(sql.length()-1);	// chop the extra ,
		getJdbcTemplate().update(sql.toString(), objects.toArray()); // this will duplicate previously existing associations, but that's ok for now
	}

	/**
	 * GUID and/or mediaObjectId must be given.
	 * 
	 * @param guid if null, the associations between the given mediaObjectId and ANY GUID will be removed
	 * @param mediaObjectId if null, the associations between the given GUID and ANY mediaObjectId will be removed
	 */
	public void deassociate(String guid, String mediaObjectId){
		// select delete sql by the given parameters, note that it does not matter if both parameters are null, the database does not allow null values so the update will just do nothing on bad (null) values.
		if(guid == null){
			getJdbcTemplate().update(SQL_DEASSOCIASTE_BY_MEDIA_OBJECT_ID, new Object[]{mediaObjectId}, SQL_DEASSOCIATE_BY_SQL_TYPES);
		}else if(mediaObjectId == null){
			getJdbcTemplate().update(SQL_DEASSOCIATE_BY_GUID, new Object[]{guid}, SQL_DEASSOCIATE_BY_SQL_TYPES);
		}else{ // mediaObjectId != null && guid != null
			getJdbcTemplate().update(SQL_DEASSOCIATE, new Object[]{guid, mediaObjectId}, SQL_DEASSOCIATE_SQL_TYPES);
		}
	}

	/**
	 * 
	 * @param guids
	 * @return GUID-mediaobject id relation map or null if none found or if guids was null or empty
	 */
	public Map<String, Set<String>> getAssociationsForGUIDs(Collection<String> guids){
		if(guids == null || guids.isEmpty()){
			LOGGER.debug("No GUIDs.");
			return null;
		}
		SQLSelectBuilder sql = new SQLSelectBuilder(TABLE_ASSOCIATIONS);
		sql.addSelectColumns(SQL_ASSOCIATION_COLUMNS);
		sql.addWhereClause(new AndClause(COLUMN_GUID, guids, SQLType.STRING));
		return extractMap(sql);
	}

	/**
	 * helper for extracting guid-void map from the given builder
	 * 
	 * @param builder
	 * @return guid-void map
	 */
	private Map<String, Set<String>> extractMap(SQLSelectBuilder builder){
		final Map<String, Set<String>> map = new HashMap<>();
		getJdbcTemplate().query(builder.toSQLString(), builder.getValues(), builder.getValueTypes(), new RowCallbackHandler() {		
			@Override
			public void processRow(ResultSet set) throws SQLException {
				String guid = set.getString(COLUMN_GUID);
				Set<String> voids = map.get(guid);
				if(voids == null){
					voids = new HashSet<>();
					map.put(guid, voids);
				}
				voids.add(set.getString(Definitions.COLUMN_MEDIA_OBJECT_ID));
			}
		});

		return (map.isEmpty() ? null : map);
	}

	/**
	 * 
	 * @param mediaObjectIds
	 * @return guid-media object map or null if no associations found
	 */
	public Map<String, Set<String>> getAssociationsForMediaObjectIds(Collection<String> mediaObjectIds){
		if(mediaObjectIds == null || mediaObjectIds.isEmpty()){
			LOGGER.debug("No media object ids.");
			return null;
		}

		SQLSelectBuilder sql = new SQLSelectBuilder(TABLE_ASSOCIATIONS);
		final List<String> guids = new ArrayList<>();
		sql.addSelectColumn(COLUMN_GUID);
		sql.addWhereClause(new AndClause(Definitions.COLUMN_MEDIA_OBJECT_ID, mediaObjectIds, SQLType.STRING));
		sql.addGroupBy(COLUMN_GUID);
		getJdbcTemplate().query(sql.toSQLString(), sql.getValues(), sql.getValueTypes(), new RowCallbackHandler() {
			@Override
			public void processRow(ResultSet set) throws SQLException {
				guids.add(set.getString(COLUMN_GUID));
			}
		});

		return getAssociationsForGUIDs(guids);
	}
}
