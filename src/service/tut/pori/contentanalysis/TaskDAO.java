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
package service.tut.pori.contentanalysis;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;

import service.tut.pori.contentanalysis.AbstractTaskDetails.TaskParameters;
import service.tut.pori.contentanalysis.AnalysisBackend.Capability;
import service.tut.pori.contentanalysis.AsyncTask.TaskStatus;
import service.tut.pori.contentanalysis.AsyncTask.TaskType;
import core.tut.pori.dao.SQLDAO;
import core.tut.pori.dao.clause.SQLClause.SQLType;
import core.tut.pori.http.parameters.DataGroups;
import core.tut.pori.http.parameters.Limits;
import core.tut.pori.users.UserIdentity;

/**
 * Base class for task DAOs
 * 
 * Note that this class is <i>NOT</i> abstract, and it can be used to implement core task details ({@link service.tut.pori.contentanalysis.AbstractTaskDetails}),
 * but the class is not guaranteed to function with extended capabilities provided by inherited classes. Use the service specific implementation when needed.
 * 
 */
public class TaskDAO extends SQLDAO {
	private static final Logger LOGGER = Logger.getLogger(TaskDAO.class);
	private static final String METADATA_TASK_PARAMETER_CLASS = "METADATA_TP_CLS";
	/* tables */
	private static final String TABLE_TASKS = DATABASE +".ca_tasks";
	private static final String TABLE_TASK_BACKENDS = DATABASE +".ca_tasks_backends";
	private static final String TABLE_TASK_METADATA = DATABASE +".ca_tasks_metadata";
	/* columns */
	/** default column name for status messages */
	protected static final String COLUMN_MESSAGE = "message";
	/** default column name for task ids */
	protected static final String COLUMN_TASK_ID = "task_id";
	/** default column name for task type */
	protected static final String COLUMN_TASK_TYPE = "task_type";
	private static final String[] SQL_COLUMNS_INSERT_TASK = new String[]{COLUMN_TASK_TYPE, COLUMN_ROW_CREATED, COLUMN_USER_ID};

	/* sql strings */
	private static final String SQL_CHECK_TASK_BACKEND = "SELECT "+COLUMN_COUNT+" FROM "+TABLE_TASK_BACKENDS+" WHERE "+Definitions.COLUMN_BACKEND_ID+"=? AND "+COLUMN_TASK_ID+"=? LIMIT 1";
	private static final int[] SQL_CHECK_TASK_BACKEND_TYPES = new int[]{SQLType.INTEGER.toInt(), SQLType.LONG.toInt()};
	
	private static final String SQL_GET_BACKEND_STATUSES = "SELECT "+Definitions.COLUMN_BACKEND_ID+", "+Definitions.COLUMN_STATUS+", "+COLUMN_MESSAGE+" FROM "+TABLE_TASK_BACKENDS+" WHERE "+COLUMN_TASK_ID+"=?";
	private static final int[] SQL_GET_BACKEND_STATUSES_SQL_TYPES = new int[]{SQLType.LONG.toInt()};

	private static final String SQL_INSERT_TASK_BACKEND = "INSERT INTO "+TABLE_TASK_BACKENDS+" ("+COLUMN_TASK_ID+", "+Definitions.COLUMN_BACKEND_ID+", "+Definitions.COLUMN_STATUS+", "+COLUMN_MESSAGE+", "+COLUMN_ROW_CREATED+") VALUES (?,?,?,?,NOW())";
	private static final int[] SQL_INSERT_TASK_BACKEND_TYPES = new int[]{SQLType.LONG.toInt(), SQLType.INTEGER.toInt(), SQLType.INTEGER.toInt(), SQLType.STRING.toInt()};

	private static final String SQL_INSERT_TASK_METADATA = "INSERT INTO "+TABLE_TASK_METADATA+" ("+COLUMN_TASK_ID+", "+Definitions.COLUMN_NAME+", "+Definitions.COLUMN_VALUE+", "+COLUMN_ROW_CREATED+") VALUES (?,?,?,NOW())";
	private static final int[] SQL_INSERT_TASK_METADATA_TYPES = new int[]{SQLType.LONG.toInt(), SQLType.STRING.toInt(), SQLType.STRING.toInt()};

	private static final String SQL_SELECT_BACKEND_STATUS_BY_BACKEND_ID = "SELECT "+Definitions.COLUMN_STATUS+", "+COLUMN_MESSAGE+", "+Definitions.COLUMN_BACKEND_ID+" FROM "+TABLE_TASK_BACKENDS+" WHERE "+Definitions.COLUMN_BACKEND_ID+"=? AND "+COLUMN_TASK_ID+"=? LIMIT 1";
	private static final int[] SQL_SELECT_BACKEND_STATUS_BY_BACKEND_ID_TYPES = new int[]{SQLType.INTEGER.toInt(), SQLType.LONG.toInt()};

	private static final String SQL_SELECT_BACKEND_STATUS_BY_TASK_ID = "SELECT "+Definitions.COLUMN_STATUS+", "+COLUMN_MESSAGE+", "+Definitions.COLUMN_BACKEND_ID+" FROM "+TABLE_TASK_BACKENDS+" WHERE "+COLUMN_TASK_ID+"=?";
	private static final int[] SQL_SELECT_BACKEND_STATUS_BY_TASK_ID_TYPES = new int[]{SQLType.LONG.toInt()};

	private static final String SQL_SELECT_BACKEND_STATUS_BY_TASK_STATUS = "SELECT "+Definitions.COLUMN_STATUS+", "+COLUMN_MESSAGE+", "+Definitions.COLUMN_BACKEND_ID+" FROM "+TABLE_TASK_BACKENDS+" WHERE "+Definitions.COLUMN_STATUS+"=? AND "+COLUMN_TASK_ID+"=?";
	private static final int[] SQL_SELECT_BACKEND_STATUS_BY_TASK_STATUS_TYPES = new int[]{SQLType.INTEGER.toInt(), SQLType.LONG.toInt()};

	private static final String SQL_SELECT_TASK_METADATA = "SELECT "+Definitions.COLUMN_NAME+", "+Definitions.COLUMN_VALUE+" FROM "+TABLE_TASK_METADATA+" WHERE "+COLUMN_TASK_ID+"=?";
	private static final int[] SQL_SELECT_TASK_METADATA_TYPES = new int[]{SQLType.LONG.toInt()};

	private static final String SQL_UPDATE_TASK_STATUS = "UPDATE "+TABLE_TASK_BACKENDS+" SET "+Definitions.COLUMN_STATUS+"=?, "+COLUMN_MESSAGE+"=? WHERE "+COLUMN_TASK_ID+"=? AND "+Definitions.COLUMN_BACKEND_ID+"=? LIMIT 1";
	private static final int[] SQL_UPDATE_TASK_STATUS_TYPES = new int[]{SQLType.INTEGER.toInt(), SQLType.STRING.toInt(), SQLType.LONG.toInt(), SQLType.INTEGER.toInt()};
	
	private static final String SQL_SELECT_TASK_TYPE = "SELECT "+COLUMN_TASK_TYPE+", "+COLUMN_USER_ID+" FROM "+TABLE_TASKS+" WHERE "+COLUMN_TASK_ID+"=?";
	private static final int[] SQL_SELECT_TASK_TYPE_TYPES = new int[]{SQLType.LONG.toInt()};

	private static final String SQL_SELECT_TASK_TYPE_BY_BACKEND_ID = "SELECT "+TABLE_TASKS+"."+COLUMN_TASK_TYPE+", "+COLUMN_USER_ID+" FROM "+TABLE_TASKS+" INNER JOIN "+TABLE_TASK_BACKENDS+" ON "+TABLE_TASKS+"."+COLUMN_TASK_ID+"="+TABLE_TASK_BACKENDS+"."+COLUMN_TASK_ID+" WHERE "+TABLE_TASK_BACKENDS+"."+Definitions.COLUMN_BACKEND_ID+"=? AND "+TABLE_TASK_BACKENDS+"."+COLUMN_TASK_ID+"=? LIMIT 1";
	private static final int[] SQL_SELECT_TASK_TYPE_BY_BACKEND_ID_TYPES = new int[]{SQLType.INTEGER.toInt(), SQLType.LONG.toInt()};

	@Autowired
	private BackendDAO _backendDAO = null;

	/**
	 * 
	 * @param taskId
	 * @param taskStatus optional status filter, if null all back-ends matching the given id will be returned
	 * @return list of back-ends associated with the given taskId
	 */
	public BackendStatusList getBackendStatus(Long taskId, TaskStatus taskStatus){
		List<Map<String, Object>> rows = null;
		if(taskStatus == null){
			rows = getJdbcTemplate().queryForList(SQL_SELECT_BACKEND_STATUS_BY_TASK_ID, new Object[]{taskId}, SQL_SELECT_BACKEND_STATUS_BY_TASK_ID_TYPES);
		}else{
			rows = getJdbcTemplate().queryForList(SQL_SELECT_BACKEND_STATUS_BY_TASK_STATUS, new Object[]{taskStatus.toInt(), taskId}, SQL_SELECT_BACKEND_STATUS_BY_TASK_STATUS_TYPES);
		}

		if(rows.isEmpty()){
			return null;
		}else{
			BackendStatusList list = new BackendStatusList();
			for(Iterator<Map<String,Object>> iter = rows.iterator(); iter.hasNext();){
				BackendStatus backendStatus = extractBackendStatus(iter.next());
				if(backendStatus == null){
					LOGGER.warn("Ignored non-existing back-end.");
				}else{
					list.setBackendStatus(backendStatus);
				}
			}
			if(BackendStatusList.isEmpty(list)){
				LOGGER.warn("No valid backends for task, id: "+taskId);
				return null;
			}else{
				return list;
			} // else	
		} // else
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
	 * 
	 * @param backendId if null, match is made simply by the task id. The parameter can be used to check whether the given back-end is associated with the task id.
	 * @param taskId
	 * @return the task type and task's owner/creator or null if the given task does not exists or if the backend is not set for the task 
	 */
	protected Pair<TaskType, UserIdentity> getTaskType(Integer backendId, Long taskId) {
		List<Map<String, Object>> rows = null;
		if(backendId == null){
			LOGGER.debug("Retrieving task type without backend id filter.");
			rows = getJdbcTemplate().queryForList(SQL_SELECT_TASK_TYPE, new Object[]{taskId}, SQL_SELECT_TASK_TYPE_TYPES);
		}else{
			rows = getJdbcTemplate().queryForList(SQL_SELECT_TASK_TYPE_BY_BACKEND_ID, new Object[]{backendId, taskId}, SQL_SELECT_TASK_TYPE_BY_BACKEND_ID_TYPES);
		}
		if(rows.isEmpty()){
			LOGGER.warn("Task, id: "+taskId+" was not found for backend, id: "+backendId);
			return null;
		}
		Map<String, Object> row = rows.iterator().next();
		Integer taskType = (Integer) row.get(COLUMN_TASK_TYPE);
		if(taskType == null){
			LOGGER.warn("Task, id: "+taskId+" was not found for backend, id: "+backendId);
			return null;
		}else{
			Long userId = (Long) row.get(COLUMN_USER_ID);
			return Pair.of(TaskType.fromInt(taskType), (userId == null ? null : new UserIdentity(userId)));
		}
	}

	
	/**
	 * 
	 * @param details
	 */
	protected void insertTaskBackends(AbstractTaskDetails details){
		BackendStatusList statuses = details.getBackends();
		if(statuses == null){
			LOGGER.debug("No backendStatusList, creating a new one...");
			statuses = new BackendStatusList();
		}

		Integer backendId = details.getBackendId();
		if(backendId != null){
			BackendStatus s = statuses.getBackendStatus(backendId);
			if(s == null){	// make sure the backend of the task is in the status list
				LOGGER.debug("BackendId was given, inserting backend to the list of target backends with TaskStatus: "+TaskStatus.NOT_STARTED.name());
				s = new BackendStatus(new AnalysisBackend(backendId), TaskStatus.NOT_STARTED);
				statuses.setBackendStatus(s);
			}
		}

		Object[] values = new Object[]{details.getTaskId(), null, null, null};

		if(BackendStatusList.isEmpty(statuses)){
			LOGGER.warn("No backends given, checking if any capable backends exists. Task, id: "+values[0]);
			TaskType taskType = details.getTaskType();
			Capability capability = resolveCapability(taskType);
			if(capability == null){
				LOGGER.debug("No known capabilities for task type "+taskType+", the task may not start properly, task id: "+values[0]);
				return;
			}
			List<AnalysisBackend> backends = _backendDAO.getBackends(capability);
			if(backends == null){
				LOGGER.debug("No capable backends available, the task may not start properly, task id: "+values[0]);
				return;
			}
			LOGGER.debug("Adding task, id: "+values[0]+" for all backend with capability: "+capability.name());
			for(AnalysisBackend end : backends){
				statuses.setBackendStatus(new BackendStatus(end, TaskStatus.NOT_STARTED));
			}
		}

		JdbcTemplate t = getJdbcTemplate();
		for(BackendStatus s : statuses.getBackendStatuses()){
			values[1] = s.getBackendId();
			values[2] = s.getStatus().toInt();
			values[3] = s.getMessage();
			t.update(SQL_INSERT_TASK_BACKEND, values, SQL_INSERT_TASK_BACKEND_TYPES);
		}
	}
	
	/**
	 * Supported conversions:
	 * <ul>
	 *  <li>{@link service.tut.pori.contentanalysis.AsyncTask.TaskType#BACKEND_FEEDBACK} to {@link service.tut.pori.contentanalysis.AnalysisBackend.Capability#BACKEND_FEEDBACK}</li>
	 *  <li>{@link service.tut.pori.contentanalysis.AsyncTask.TaskType#FEEDBACK} to {@link service.tut.pori.contentanalysis.AnalysisBackend.Capability#USER_FEEDBACK}</li> 
	 * </ul>
	 * @param taskType
	 * @return the capability or null if no capability found
	 * @throws IllegalArgumentException on bad taskType
	 */
	public Capability resolveCapability(TaskType taskType) throws IllegalArgumentException {
		if(taskType == null){
			throw new IllegalArgumentException("TaskType was null.");
		}
		switch(taskType){
			case BACKEND_FEEDBACK:
				return Capability.BACKEND_FEEDBACK;
			case FEEDBACK: // user feedback
				return Capability.USER_FEEDBACK;
			default:
				LOGGER.warn("Unknown "+TaskType.class.toString()+" : "+taskType.name());
				return null;
		}
	}
	
	/**
	 * 
	 * @param details
	 */
	protected void getBackendStatusList(AbstractTaskDetails details) {
		Long taskId = details.getTaskId();
		List<Map<String, Object>> rows = getJdbcTemplate().queryForList(SQL_GET_BACKEND_STATUSES, new Object[]{taskId}, SQL_GET_BACKEND_STATUSES_SQL_TYPES);
		if(rows.isEmpty()){
			LOGGER.warn("No back-end for task, id: "+taskId);
		}else{
			BackendStatusList statuses = new BackendStatusList();
			for(Map<String, Object> row : rows){
				statuses.setBackendStatus(extractBackendStatus(row));
			}
			details.setBackends(statuses);
		}
	}
	
	/**
	 * Update the given status list for the given task.
	 * 
	 * @param status
	 * @param taskId
	 */
	public void updateTaskStatus(BackendStatusList status, Long taskId){
		if(BackendStatusList.isEmpty(status)){
			LOGGER.debug("Status list was empty for task, id: "+taskId);
			return;
		}
		for(BackendStatus s : status.getBackendStatuses()){
			updateTaskStatus(s, taskId);
		}
	}
	
	/**
	 * 
	 * @param backendId
	 * @param taskId
	 * @return the task status for the given backend for the given task, or null if no such task is given for the backend
	 */
	public BackendStatus getBackendStatus(Integer backendId, Long taskId){
		List<Map<String, Object>> rows = getJdbcTemplate().queryForList(SQL_SELECT_BACKEND_STATUS_BY_BACKEND_ID, new Object[]{backendId, taskId}, SQL_SELECT_BACKEND_STATUS_BY_BACKEND_ID_TYPES);
		if(rows.isEmpty()){
			return null;
		}else{
			return extractBackendStatus(rows.get(0));
		}
	}
	
	/**
	 * @param backendId
	 * @param dataGroups optional dataGroups filter, if not given, default backend-specific datagroups will be used
	 * @param limits optional limits filter
	 * @param taskId
	 * @return the task or null if not found
	 * @throws IllegalArgumentException on bad values
	 */
	public AbstractTaskDetails getTask(Integer backendId, DataGroups dataGroups, Limits limits, Long taskId) throws IllegalArgumentException{
		Pair<TaskType, UserIdentity> type = getTaskType(backendId, taskId);
		if(type == null){
			LOGGER.warn("Failed to resolve task type.");
			return null;
		}

		if(backendId == null){
			LOGGER.debug("No backend id given, will not check data groups.");
		}else if(DataGroups.isEmpty(dataGroups)){
			LOGGER.debug("No datagroups given, retrieving default data groups.");
			AnalysisBackend backend = _backendDAO.getBackend(backendId);
			if(backend == null){
				throw new IllegalArgumentException("Backend, id: "+backendId+" does not exist.");
			}
			dataGroups = backend.getDefaultTaskDataGroups();
		}

		AbstractTaskDetailsImpl details = new AbstractTaskDetailsImpl(type.getLeft());
		details.setBackendId(backendId);
		details.setTaskId(taskId);
		details.setUserId(type.getRight());

		getTaskMetadata(details);

		if(DataGroups.hasDataGroup(Definitions.DATA_GROUP_BACKEND_STATUS, dataGroups)){
			getBackendStatusList(details);
		}

		return details;
	}
	
	/**
	 * Update the given status for the given task. If the status does not previously exist for this backend, new database entry is automatically created.
	 * 
	 * @param status
	 * @param taskId
	 */
	public void updateTaskStatus(final BackendStatus status, final Long taskId){
		getTransactionTemplate().execute(new TransactionCallback<Void>() {

			@Override
			public Void doInTransaction(TransactionStatus s) {
				JdbcTemplate t = getJdbcTemplate();
				Integer backendId = status.getBackendId();
				if(t.queryForObject(SQL_CHECK_TASK_BACKEND, new Object[]{backendId, taskId}, SQL_CHECK_TASK_BACKEND_TYPES, Long.class) > 0){ // already exists
					if(t.update(SQL_UPDATE_TASK_STATUS, new Object[]{status.getStatus().toInt(), status.getMessage(), taskId, status.getBackendId()}, SQL_UPDATE_TASK_STATUS_TYPES) != 1){
						LOGGER.debug("Nothing updated for task, id: "+taskId+", backend, id: "+status.getBackendId());
					}
				}else{ // add new status
					t.update(SQL_INSERT_TASK_BACKEND, new Object[]{backendId, status.getStatus().toInt(), status.getMessage()}, SQL_INSERT_TASK_BACKEND_TYPES);
				}
				return null;
			}
		});
	} // updateTaskStatus
	
	/**
	 * This will create the basic task and insert metadata and back-ends
	 * 
	 * @param details
	 * @return created row id or null on failure
	 * @throws IllegalArgumentException
	 */
	public Long insertTask(AbstractTaskDetails details) throws IllegalArgumentException{
		TaskType type = details.getTaskType();
		if(type == null){
			throw new IllegalArgumentException("No task type given.");
		}
		SimpleJdbcInsert taskInsert = new SimpleJdbcInsert(getJdbcTemplate());
		taskInsert.withTableName(TABLE_TASKS);
		taskInsert.setGeneratedKeyName(COLUMN_TASK_ID);
		taskInsert.usingColumns(SQL_COLUMNS_INSERT_TASK);
		taskInsert.withoutTableColumnMetaDataAccess();

		HashMap<String, Object> parameters = new HashMap<>(SQL_COLUMNS_INSERT_TASK.length);
		parameters.put(COLUMN_TASK_TYPE, type.toInt());
		parameters.put(COLUMN_ROW_CREATED, null);
		parameters.put(COLUMN_USER_ID, details.getUserIdValue());
		Number key = taskInsert.executeAndReturnKey(parameters);
		if(key == null){
			LOGGER.error("Failed to add new task.");
			return null;
		}

		Long taskId = key.longValue();
		details.setTaskId(taskId);

		insertTaskMetadata(details);
		insertTaskParameters(details);
		insertTaskBackends(details);

		return taskId;
	}
	
	/**
	 * 
	 * @param details
	 */
	protected void insertTaskParameters(AbstractTaskDetails details){
		TaskParameters params = details.getTaskParameters();
		if(params == null){
			LOGGER.debug("No task parameters.");
			return;
		}
		
		Long taskId = details.getTaskId();
		Map<String, String> metadata = params.toMetadata();
		if(metadata == null || metadata.isEmpty()){
			LOGGER.debug("No task parameter metadata for task, id: "+taskId);
		}else{
			metadata.put(METADATA_TASK_PARAMETER_CLASS, params.getClass().getName());
			insertTaskMetadata(metadata, taskId);
		}
	}

	/**
	 * 
	 * @param details
	 */
	protected void insertTaskMetadata(AbstractTaskDetails details){
		Long taskId = details.getTaskId();
		Map<String, String> metadata = details.getMetadata();
		if(metadata == null || metadata.isEmpty()){
			LOGGER.debug("No metadata for task, id: "+taskId);
		}else{
			insertTaskMetadata(metadata, taskId);
		}
	}
	
	/**
	 * 
	 * @param metadata
	 * @param taskId
	 */
	private void insertTaskMetadata(Map<String, String> metadata, Long taskId) {
		JdbcTemplate t = getJdbcTemplate();
		Object[] ob = {taskId, null, null};
		for(Entry<String, String> e : metadata.entrySet()){
			ob[1] = e.getKey();
			ob[2] = e.getValue();
			t.update(SQL_INSERT_TASK_METADATA, ob, SQL_INSERT_TASK_METADATA_TYPES);
		}
	}
	
	/**
	 * Retrieves (and sets) the task metadata for the given details.
	 * 
	 * This will also retrieve task parameters {@link service.tut.pori.contentanalysis.AbstractTaskDetails#getTaskParameters()} as they are in the default implementation stored in the metadata table.
	 * 
	 * @param details
	 * @see #getTaskParameters(AbstractTaskDetails, Map)
	 */
	protected void getTaskMetadata(AbstractTaskDetails details){
		Long taskId = details.getTaskId();
		List<Map<String,Object>> rows = getJdbcTemplate().queryForList(SQL_SELECT_TASK_METADATA, new Object[]{taskId}, SQL_SELECT_TASK_METADATA_TYPES);
		int count = rows.size();
		if(count < 1){
			LOGGER.debug("No metadata for task, id: "+taskId);
			return;
		}

		HashMap<String, String> metadata = new HashMap<>(count);	
		for(Map<String, Object> row : rows){
			metadata.put((String) row.get(Definitions.COLUMN_NAME), (String) row.get(Definitions.COLUMN_VALUE));
		}	// for
		details.setMetadata(metadata);
		
		getTaskParameters(details, metadata);
	}
	
	/**
	 * 
	 * @param details
	 * @param metadata
	 * @throws IllegalArgumentException on invalid class name
	 */
	protected void getTaskParameters(AbstractTaskDetails details, Map<String, String> metadata) throws IllegalArgumentException {
		String clsName = metadata.get(METADATA_TASK_PARAMETER_CLASS);
		if(StringUtils.isBlank(clsName)){
			LOGGER.debug("No task parameters for task, id: "+details.getTaskId());
			return;
		}
		
		try {
			TaskParameters params = (TaskParameters) Class.forName(clsName).newInstance();
			params.initialize(metadata);
			details.setTaskParameters(params);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
			LOGGER.error(ex, ex);
			throw new IllegalArgumentException("Unknown parameter class : "+clsName);
		}
	}
	
	/**
	 * Basic implementation for Task details
	 *
	 */
	private class AbstractTaskDetailsImpl extends AbstractTaskDetails {
		private TaskParameters _taskParameters = null;
		
		/**
		 * 
		 * @param taskType
		 */
		public AbstractTaskDetailsImpl(TaskType taskType) {
			setTaskType(taskType);
		}

		@Override
		public TaskParameters getTaskParameters() {
			return _taskParameters;
		}

		@Override
		public void setTaskParameters(TaskParameters parameters) {
			_taskParameters = parameters;
		}
	} // class AbstractTaskDetailsImpl
}
