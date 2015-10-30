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

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import service.tut.pori.contentanalysis.AsyncTask.TaskStatus;
import core.tut.pori.dao.SQLSelectBuilder;
import core.tut.pori.dao.SQLSelectBuilder.OrderDirection;
import core.tut.pori.dao.clause.AndClause;
import core.tut.pori.dao.clause.SQLClause.SQLType;
import core.tut.pori.http.parameters.DataGroups;
import core.tut.pori.http.parameters.Limits;
import core.tut.pori.utils.MediaUrlValidator.MediaType;

/**
 * Abstract base class for tasks that use media content.
 * 
 */
public abstract class MediaTaskDAO extends TaskDAO {
	private static final Logger LOGGER = Logger.getLogger(MediaTaskDAO.class);
	/* tables */
	private static final String TABLE_TASK_GUIDS = DATABASE +".ca_tasks_guids";
	private static final String TABLE_TASK_GUIDS_STATUS = DATABASE +".ca_tasks_guids_status";
	private static final String TABLE_TASK_MEDIA_OBJECTS = DATABASE +".ca_tasks_media_objects";
	/* columns */
	private static final String COLUMN_GUID_TYPE = "type";
	/* sql scripts */
	private static final String[] SQL_GET_MEDIA_STATUS_SELECT_COLUMNS = {Definitions.COLUMN_BACKEND_ID, COLUMN_GUID, Definitions.COLUMN_STATUS, COLUMN_MESSAGE};

	private static final int[] SQL_DELETE_GUID_SQL_TYPES = {SQLType.STRING.toInt()};
	private static final String SQL_DELETE_TASK_GUIDS = "DELETE FROM "+TABLE_TASK_GUIDS+" WHERE "+COLUMN_GUID+"=?";
	private static final String SQL_DELETE_TASK_GUIDS_STATUS = "DELETE FROM "+TABLE_TASK_GUIDS_STATUS+" WHERE "+COLUMN_GUID+"=?";
	private static final String SQL_DELETE_TASK_MEDIA_OBJECTS = "DELETE FROM "+TABLE_TASK_MEDIA_OBJECTS+" WHERE "+COLUMN_GUID+"=?";

	private static final String SQL_INSERT_TASK_GUID = "INSERT INTO "+TABLE_TASK_GUIDS+" ("+COLUMN_TASK_ID+", "+COLUMN_GUID+", "+COLUMN_GUID_TYPE+", "+COLUMN_ROW_CREATED+") VALUES (?,?,?,NOW())";
	private static final int[] SQL_INSERT_TASK_GUID_TYPES = {SQLType.LONG.toInt(), SQLType.STRING.toInt(), SQLType.INTEGER.toInt()};

	private static final String SQL_INSERT_TASK_MEDIA_OBJECTS = "INSERT INTO "+TABLE_TASK_MEDIA_OBJECTS+" ("+COLUMN_TASK_ID+", "+COLUMN_GUID+", "+Definitions.COLUMN_MEDIA_OBJECT_ID+","+COLUMN_ROW_CREATED+") VALUES (?,?,?,NOW())";
	private static final int[] SQL_INSERT_TASK_MEDIA_OBJECTS_TYPES = {SQLType.LONG.toInt(), SQLType.STRING.toInt(), SQLType.STRING.toInt()};

	private static final String SQL_UPDATE_MEDIA_STATUS = "INSERT INTO "+TABLE_TASK_GUIDS_STATUS+" ("+Definitions.COLUMN_BACKEND_ID+", "+COLUMN_GUID+", "+COLUMN_TASK_ID+", "+Definitions.COLUMN_STATUS+", "+COLUMN_MESSAGE+", "+COLUMN_ROW_CREATED+") VALUES (?,?,?,?,?, NOW()) ON DUPLICATE KEY UPDATE "+COLUMN_TASK_ID+"=VALUES("+COLUMN_TASK_ID+"), "+Definitions.COLUMN_STATUS+"=VALUES("+Definitions.COLUMN_STATUS+"), "+COLUMN_MESSAGE+"=VALUES("+COLUMN_MESSAGE+"), "+COLUMN_ROW_UPDATED+"=NOW()";
	private static final int[] SQL_UPDATE_MEDIA_STATUS_TYPES = {SQLType.INTEGER.toInt(), SQLType.STRING.toInt(), SQLType.LONG.toInt(), SQLType.INTEGER.toInt(), SQLType.STRING.toInt()};

	@Autowired
	private BackendDAO _backendDAO = null;
	@Autowired
	private MediaObjectDAO _mediaObjectDAO = null;
	
	/**
	 * the type of GUID in tasks GUIDs table
	 */
	protected enum GUIDType{
		/** GUID describes a basic media item */
		MEDIA(0),
		/** GUID describes a reference media item */
		REFERENCE_MEDIA(1),
		/** GUID describes media item similar with a reference item */
		SIMILAR_MEDIA(2),
		/** GUID describes media item dissimilar with a reference item */
		DISSIMILAR_MEDIA(3),
		/** GUID describes a deleted media item */
		DELETED_MEDIA(4);

		private int _value;

		/**
		 * 
		 * @param value
		 */
		private GUIDType(int value){
			_value = value;
		}

		/**
		 * 
		 * @return type as a string
		 */
		public int toInt(){
			return _value;
		}

		/**
		 * 
		 * @param value
		 * @return value converted to type
		 * @throws IllegalArgumentException on bad value
		 */
		public static GUIDType fromInt(int value) throws IllegalArgumentException {
			for(GUIDType t : GUIDType.values()){
				if(t._value == value){
					return t;
				}
			}
			throw new IllegalArgumentException("Bad "+GUIDType.class.toString()+" : "+value);
		}
	} // enum GUIDType

	/**
	 * 
	 * @param limits 
	 * @param taskId
	 * @param type
	 * @return list of GUIDs for the given task, which are of the given type or null if none was found
	 */
	protected List<String> getTaskGUIDs(Limits limits, Long taskId, GUIDType type) {
		SQLSelectBuilder sql = new SQLSelectBuilder(TABLE_TASK_GUIDS);
		sql.addSelectColumn(COLUMN_GUID);
		sql.setLimits(limits);
		sql.addWhereClause(new AndClause(COLUMN_TASK_ID, taskId, SQLType.LONG));
		sql.addWhereClause(new AndClause(COLUMN_GUID_TYPE, type.toInt(), SQLType.INTEGER));
		
		List<String> guids = getJdbcTemplate().queryForList(sql.toSQLString(), sql.getValues(), sql.getValueTypes(), String.class);
		if(guids.isEmpty()){
			LOGGER.warn("No types found for task, id: "+taskId);
			return null;
		}
		return guids;
	}	
	
	/**
	 * This will also set photo statuses, if any are present. Note that even through status elements can appear in any photo list,
	 * creating two different lists with identical GUIDs, and conflicting status lists may create undefined behavior.
	 * 
	 * @param media
	 * @param taskId
	 * @param type 
	 */
	protected <T extends Media> void insertTaskGUIDs(Collection<T> media, Long taskId, GUIDType type){
		if(media == null || media.isEmpty()){
			LOGGER.debug("No media for task, id: "+taskId);
		}else{
			JdbcTemplate t = getJdbcTemplate();
			Object[] values = new Object[]{taskId,null,type.toInt()};
			for(T m : media){
				values[1] = m.getGUID();
				t.update(SQL_INSERT_TASK_GUID, values, SQL_INSERT_TASK_GUID_TYPES);
			}
			updateMediaStatus(media, taskId);
		}
	}

	/**
	 * 
	 * @param guid
	 * @param taskId
	 * @param mediaObjects list of media objects to add
	 */
	protected void insertTaskMediaObjects(String guid, Long taskId, MediaObjectList mediaObjects){
		if(MediaObjectList.isEmpty(mediaObjects)){
			LOGGER.debug("Ignored empty media object list for GUID: "+guid);
			return;
		}
		if(guid == null){
			LOGGER.debug("Adding task media object without GUID.");
		}
		Object[] ob = {taskId, guid, null};
		for(MediaObject vo : mediaObjects.getMediaObjects()){
			ob[2] = vo.getMediaObjectId();
			getJdbcTemplate().update(SQL_INSERT_TASK_MEDIA_OBJECTS, ob, SQL_INSERT_TASK_MEDIA_OBJECTS_TYPES);
		}
	}

	/**
	 * 
	 * @param media list of media items
	 * @param taskId optional taskId. This should be (if given) the id of the most recent task.
	 */
	public <T extends Media> void updateMediaStatus(Collection<T> media, Long taskId){
		if(media == null || media.isEmpty()){
			LOGGER.debug("No media given.");
			return;
		}

		JdbcTemplate t = getJdbcTemplate();
		Object[] values = new Object[]{null,null,taskId,null,null};
		for(Media m : media){
			values[1] = m.getGUID();
			BackendStatusList statuses = m.getBackendStatus();
			if(BackendStatusList.isEmpty(statuses)){
				LOGGER.debug("No statuses for photo, guid: "+values[1]);
			}else{
				for(Iterator<BackendStatus> sIter = statuses.getBackendStatuses().iterator(); sIter.hasNext();){
					BackendStatus s = sIter.next();
					values[0] = s.getBackendId();
					values[3] = s.getStatus().toInt();
					values[4] = s.getMessage();
					t.update(SQL_UPDATE_MEDIA_STATUS, values, SQL_UPDATE_MEDIA_STATUS_TYPES);
				}	// for
			}
		}	// for
	}

	/**
	 * Removes the list of GUIDs from all tasks
	 * 
	 * @param guids
	 */
	public void remove(Collection<String> guids){
		if(guids == null || guids.isEmpty()){
			LOGGER.debug("Ignored empty guids list.");
			return;
		}
		JdbcTemplate t = getJdbcTemplate();
		Object[] ob = new Object[1];
		for(Iterator<String> iter = guids.iterator(); iter.hasNext();){
			ob[0] = iter.next();
			t.update(SQL_DELETE_TASK_GUIDS, ob, SQL_DELETE_GUID_SQL_TYPES);
			t.update(SQL_DELETE_TASK_GUIDS_STATUS, ob, SQL_DELETE_GUID_SQL_TYPES);
			t.update(SQL_DELETE_TASK_MEDIA_OBJECTS, ob, SQL_DELETE_GUID_SQL_TYPES);
		}
	}

	@Override
	public abstract AbstractTaskDetails getTask(Integer backendId, DataGroups dataGroups, Limits limits, Long taskId) throws IllegalArgumentException;

	/**
	 * 
	 * @param dataGroups
	 * @param limits
	 * @param media
	 * @param mediaTypes media object media types to retrieve
	 * @param taskId
	 */
	protected <T extends Media> void setMediaObjects(DataGroups dataGroups, Limits limits, Collection<T> media, EnumSet<MediaType> mediaTypes, Long taskId){
		Map<String, List<String>> GUIDMediaObjectIdMap = getMediaObjectIds(limits, taskId);
		if(GUIDMediaObjectIdMap == null){
			return;
		}

		for(T m : media){
			List<String> voids = GUIDMediaObjectIdMap.get(m.getGUID());
			if(voids != null){
				m.setMediaObjects(_mediaObjectDAO.getMediaObjects(dataGroups, null, mediaTypes, null, voids, null));
			}
		}
	}

	/**
	 * 
	 * @param limits
	 * @param taskId
	 * @return GUID-void map of media object ids or null if none were found for the given task
	 */
	protected Map<String, List<String>> getMediaObjectIds(Limits limits, Long taskId){
		SQLSelectBuilder sql = new SQLSelectBuilder(TABLE_TASK_MEDIA_OBJECTS);
		sql.addSelectColumns(new String[]{COLUMN_GUID, Definitions.COLUMN_MEDIA_OBJECT_ID});
		sql.addOrderBy(Definitions.COLUMN_MEDIA_OBJECT_ID, OrderDirection.ASCENDING);
		sql.setLimits(limits);
		sql.addWhereClause(new AndClause(COLUMN_TASK_ID, taskId, SQLType.LONG));
		
		List<Map<String,Object>> rows = getJdbcTemplate().queryForList(sql.toSQLString(Definitions.ELEMENT_MEDIA_OBJECTLIST), sql.getValues(), sql.getValueTypes());
		if(rows.isEmpty()){
			LOGGER.debug("No media objects for task, id: "+taskId);
			return null;
		}
		HashMap<String, List<String>> map = new HashMap<>(rows.size());
		for(Map<String, Object> row : rows){
			String guid = (String) row.get(COLUMN_GUID);
			List<String> voids = map.get(guid);
			if(voids == null){
				map.put(guid, (voids = new ArrayList<>()));
			}
			voids.add((String) row.get(Definitions.COLUMN_MEDIA_OBJECT_ID));
		}
		return map;
	}

	/**
	 * retrieves the media status information for the list of media-items if available
	 * 
	 * @param mediaList the list to which the status information is to be set. If previous information exists, it is overridden.
	 * @return the passed list
	 */
	public <T extends Media> Collection<T> getMediaStatus(Collection<T> mediaList){
		if(mediaList == null || mediaList.isEmpty()){
			LOGGER.debug("Empty media list.");
		}else{
			SQLSelectBuilder sql = new SQLSelectBuilder(TABLE_TASK_GUIDS_STATUS);
			
			List<String> guids = new ArrayList<>(mediaList.size());
			for (Media media : mediaList) {
				guids.add(media.getGUID());
			}			
			sql.addSelectColumns(SQL_GET_MEDIA_STATUS_SELECT_COLUMNS);
			sql.addWhereClause(new AndClause(COLUMN_GUID, guids.toArray(), SQLType.STRING));
			List<Map<String,Object>> rows = getJdbcTemplate().queryForList(sql.toSQLString(Definitions.ELEMENT_BACKEND_STATUS_LIST), sql.getValues(), sql.getValueTypes());
			if(rows.isEmpty()){
				LOGGER.debug("No status for the given media items.");
				return mediaList;
			}

			for(Iterator<Map<String, Object>> rIter = rows.iterator(); rIter.hasNext();){
				Map<String,Object> row = rIter.next();
				BackendStatus status = extractBackendStatus(row);
				if(status == null){
					LOGGER.debug("Ignored status for non-existent backend.");
					continue;
				}
				String guid = (String) row.get(COLUMN_GUID);
				for(Iterator<? extends Media> pIter = mediaList.iterator(); pIter.hasNext();){
					Media p = pIter.next();
					if(guid.equals(p.getGUID())){
						p.addackendStatus(status);
					}
				}	// for mediaList
			}	// for rows
		}	// else
		return mediaList;
	}
	
	/**
	 * extract new BackendStatus from the given row
	 * 
	 * @param row
	 * @return status extracted from the given row map
	 */
	private BackendStatus extractBackendStatus(Map<String, Object> row){
		BackendStatus s = new BackendStatus();
		for(Entry<String, Object> e : row.entrySet()){
			switch (e.getKey()) {
				case Definitions.COLUMN_BACKEND_ID:
					AnalysisBackend end = _backendDAO.getBackend((Integer)e.getValue());
					if(end == null){
						LOGGER.warn("Detected non-existent backend.");
						return null;
					}
					s.setBackend(end);
					break;
				case Definitions.COLUMN_STATUS:
					s.setStatus(TaskStatus.fromInt((Integer)e.getValue()));
					break;
				case COLUMN_MESSAGE:
					s.setMessage((String) e.getValue());
					break;
				case COLUMN_GUID:		// valid column, but not handled by extractor
				case COLUMN_TASK_ID:	// valid column, but not handled by extractor
					break;
				default:
					throw new IllegalArgumentException("Unhandeled column: "+e.getKey());
			}
		}	// for
		return s;
	}

	/**
	 * @return the backendDAO
	 */
	protected BackendDAO getBackendDAO() {
		return _backendDAO;
	}

	/**
	 * @return the mediaObjectDAO
	 */
	protected MediaObjectDAO getMediaObjectDAO() {
		return _mediaObjectDAO;
	}
}
