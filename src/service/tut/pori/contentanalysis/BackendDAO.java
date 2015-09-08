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

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;

import service.tut.pori.contentanalysis.AnalysisBackend.Capability;
import core.tut.pori.dao.clause.AndClause;
import core.tut.pori.dao.clause.SQLClause.SQLType;
import core.tut.pori.dao.SQLDAO;
import core.tut.pori.dao.SQLSelectBuilder;
import core.tut.pori.http.parameters.DataGroups;

/**
 * A DAO for inserting, removing and modifying the analysis back-ends known by the system.
 *
 */
public class BackendDAO extends SQLDAO{
	private static final Logger LOGGER = Logger.getLogger(BackendDAO.class);
	/* table names */
	private static final String TABLE_BACKEND_CAPABILITIES = DATABASE+".ca_backend_capabilities";
	private static final String TABLE_BACKENDS = DATABASE+".ca_backends";
	/* columns */
	private static final String COLUMN_ANALYSIS_URI = "analysis_uri";
	private static final String COLUMN_CAPABILITY = "capability";
	private static final String COLUMN_ENABLED = "enabled";
	private static final String COLUMN_DEFAULT_TASK_DATAGROUPS = "default_task_datagroups";
	/* sql strings */
	private static final String SQL_DELETE_BACKEND = "DELETE FROM "+TABLE_BACKENDS+" WHERE "+Definitions.COLUMN_BACKEND_ID+"=?";
	private static final int[] SQL_DELETE_BACKEND_SQL_TYPES = new int[]{SQLType.STRING.toInt()};

	private static final String SQL_DELETE_CAPABILITIES = "DELETE FROM "+TABLE_BACKEND_CAPABILITIES+" WHERE "+Definitions.COLUMN_BACKEND_ID+"=?";
	private static final int[] SQL_DELETE_CAPABILITIES_SQL_TYPES = new int[]{SQLType.STRING.toInt()};

	private static final String SQL_INSERT_BACKEND = "INSERT INTO " + TABLE_BACKENDS + " ("+Definitions.COLUMN_BACKEND_ID+", "+Definitions.COLUMN_DESCRIPTION+", "+COLUMN_ENABLED+", "+COLUMN_ANALYSIS_URI+", "+COLUMN_DEFAULT_TASK_DATAGROUPS+", "+COLUMN_ROW_CREATED+") VALUES (?,?,?,?,?,NOW())";
	private static final int[] SQL_INSERT_BACKEND_SQL_TYPES = new int[]{SQLType.INTEGER.toInt(),SQLType.STRING.toInt(),SQLType.INTEGER.toInt(),SQLType.STRING.toInt(),SQLType.STRING.toInt()};

	private static final String SQL_INSERT_CAPABILITIES = "INSERT INTO "+TABLE_BACKEND_CAPABILITIES+" ("+Definitions.COLUMN_BACKEND_ID+","+COLUMN_CAPABILITY+","+COLUMN_ROW_CREATED+") VALUES (?,?,NOW()) ON DUPLICATE KEY UPDATE "+COLUMN_ROW_UPDATED+"=NOW()";
	private static final int[] SQL_INSERT_CAPABILITIES_SQL_TYPES = new int[]{SQLType.STRING.toInt(),SQLType.STRING.toInt()};
	
	private static final String SQL_SELECT_BACKENDS = "SELECT "+TABLE_BACKENDS+"."+Definitions.COLUMN_BACKEND_ID+","+TABLE_BACKENDS+"."+COLUMN_ANALYSIS_URI+","+TABLE_BACKENDS+"."+COLUMN_ENABLED+","+TABLE_BACKENDS+"."+Definitions.COLUMN_DESCRIPTION+","+TABLE_BACKENDS+"."+COLUMN_DEFAULT_TASK_DATAGROUPS+
			" FROM "+TABLE_BACKENDS+" INNER JOIN "+TABLE_BACKEND_CAPABILITIES+" ON "+TABLE_BACKEND_CAPABILITIES+"."+Definitions.COLUMN_BACKEND_ID+"="+TABLE_BACKENDS+"."+Definitions.COLUMN_BACKEND_ID+" WHERE "+TABLE_BACKEND_CAPABILITIES+"."+COLUMN_CAPABILITY+"=? AND "+TABLE_BACKENDS+"."+COLUMN_ENABLED+"="+BooleanUtils.toInteger(true);
	private static final int[] SQL_SELECT_BACKENDS_SQL_TYPES = new int[]{SQLType.STRING.toInt()};
	
	private static final String SQL_UPDATE_BACKENDS = "UPDATE "+TABLE_BACKENDS+" SET "+COLUMN_ANALYSIS_URI+"=?,"+COLUMN_ENABLED+"=?,"+Definitions.COLUMN_DESCRIPTION+"=?,"+COLUMN_DEFAULT_TASK_DATAGROUPS+"=? WHERE "+Definitions.COLUMN_BACKEND_ID+"=?";
	private static final int[] SQL_UPDATE_BACKENDS_SQL_TYPES = new int[]{SQLType.STRING.toInt(),SQLType.STRING.toInt(),SQLType.STRING.toInt(),SQLType.STRING.toInt(),SQLType.STRING.toInt()};
	
	private static final String[] TABLE_BACKEND_CAPABILITIES_ALL_COLUMNS = new String[]{Definitions.COLUMN_BACKEND_ID,COLUMN_CAPABILITY};
	private static final String[] TABLE_BACKENDS_ALL_COLUMNS = new String[]{Definitions.COLUMN_BACKEND_ID,COLUMN_ANALYSIS_URI,COLUMN_ENABLED,Definitions.COLUMN_DESCRIPTION,COLUMN_DEFAULT_TASK_DATAGROUPS};
	
	private static final String SQL_GET_ENABLED_BACKENDS = "SELECT "+StringUtils.join(TABLE_BACKENDS_ALL_COLUMNS, ',')+" FROM "+TABLE_BACKENDS+" WHERE "+COLUMN_ENABLED+"="+BooleanUtils.toInteger(true);
	
	private static final String SQL_SELECT_BACKEND_BY_BACKEND_ID = "SELECT "+StringUtils.join(TABLE_BACKENDS_ALL_COLUMNS, ',')+" FROM "+TABLE_BACKENDS+" WHERE "+Definitions.COLUMN_BACKEND_ID+"=? LIMIT 1";
	private static final int[] SQL_SELECT_BACKEND_BY_BACKEND_ID_TYPES = new int[]{SQLType.INTEGER.toInt()};
	

	/**
	 * 
	 * @param end
	 * 
	 */
	public void createBackend(final AnalysisBackend end){
		getTransactionTemplate().execute(new TransactionCallback<Void>() {

			@Override
			public Void doInTransaction(TransactionStatus status) {
				JdbcTemplate t = getJdbcTemplate();
				t.update(SQL_INSERT_BACKEND, new Object[]{end.getBackendId(),end.getDescription(),BooleanUtils.toInteger(end.isEnabled()),end.getAnalysisUri(),end.getDefaultTaskDataGroups().toDataGroupString()}, SQL_INSERT_BACKEND_SQL_TYPES);
				
				addCapabilities(t.queryForObject(SQL_SELECT_LAST_INSERT_ID, Integer.class), end.getCapabilities());
				return null;
			}
		});
	}
	
	/**
	 * add the list of capabilities for the given backend
	 * 
	 * @param backendId
	 * @param capabilities
	 */
	private void addCapabilities(Integer backendId, EnumSet<Capability> capabilities){
		if(capabilities == null){
			LOGGER.debug("No capabilities.");
			return;
		}
		JdbcTemplate t = getJdbcTemplate();
		Object[] ob = new Object[]{backendId,null};
		for(Iterator<Capability> iter = capabilities.iterator();iter.hasNext();){
			ob[1] = iter.next().toInt();
			t.update(SQL_INSERT_CAPABILITIES, ob, SQL_INSERT_CAPABILITIES_SQL_TYPES);
		}
	}
	
	/**
	 * updates the details for the given backend
	 * 
	 * @param end
	 * 
	 */
	public void updateBackend(AnalysisBackend end){
		JdbcTemplate t = getJdbcTemplate();
		Integer backendId = end.getBackendId();
		t.update(SQL_UPDATE_BACKENDS, new Object[]{end.getAnalysisUri(),BooleanUtils.toInteger(end.isEnabled()),end.getDescription(),end.getDefaultTaskDataGroups().toDataGroupString(),backendId}, SQL_UPDATE_BACKENDS_SQL_TYPES);
		
		removeCapabilities(end.getBackendId());	// remove all previously set capabilities
		
		addCapabilities(backendId, end.getCapabilities());
	}
	
	/**
	 * 
	 * @param backendId
	 */
	private void removeCapabilities(Integer backendId){
		getJdbcTemplate().update(SQL_DELETE_CAPABILITIES, new Object[]{backendId}, SQL_DELETE_CAPABILITIES_SQL_TYPES);
	}

	/**
	 * 
	 * @param backendId
	 * 
	 */
	public void removeBackend(Integer backendId){
		getJdbcTemplate().update(SQL_DELETE_BACKEND, new Object[]{backendId}, SQL_DELETE_BACKEND_SQL_TYPES);
		removeCapabilities(backendId);
	}

	/**
	 * convenience method for retrieving all known backends
	 * @return list of backends
	 */
	public List<AnalysisBackend> getBackends(){
		return getBackends((List<Integer>)null);
	}

	/**
	 * Only enabled backends will be returned.
	 * 
	 * @param capability
	 * @return the list of backends with the given capability or null if none available
	 */
	public List<AnalysisBackend> getBackends(Capability capability){
		List<AnalysisBackend> ends = extractBackends(getJdbcTemplate().queryForList(SQL_SELECT_BACKENDS, new Object[]{capability.toInt()}, SQL_SELECT_BACKENDS_SQL_TYPES));
		setCapabilities(ends);
		return ends;
	}
	
	/**
	 * 
	 * @param capabilities
	 * @return list of back-ends matching ANY of the given capabilities or null if none was found
	 */
	public List<AnalysisBackend> getBackends(Set<Capability> capabilities){
		if(capabilities == null || capabilities.isEmpty()){
			LOGGER.debug("Empty capability list.");
			return null;
		}
		List<AnalysisBackend> ends = new ArrayList<>();
		for(Capability c : capabilities){ // loop all, we could also replace this with SQL join
			List<AnalysisBackend> tempEnds = getBackends(c);
			if(tempEnds != null){
				ends.addAll(tempEnds);
			}else{
				LOGGER.debug("No back-ends with capability: "+c.name());
			}
		}
		return (ends.isEmpty() ? null : ends);
	}

	/**
	 * 
	 * @param rows
	 * @return list of backends from the row map or null if none
	 */
	private List<AnalysisBackend> extractBackends(List<Map<String,Object>> rows){
		if(rows.isEmpty()){
			LOGGER.debug("No backends.");
			return null;
		}
		List<AnalysisBackend> ends = new ArrayList<>(rows.size());
		for(Iterator<Map<String,Object>> rowIter = rows.iterator();rowIter.hasNext();){
			AnalysisBackend end = new AnalysisBackend();
			for(Entry<String,Object> rowEntry : rowIter.next().entrySet()){
				switch(rowEntry.getKey()){
					case Definitions.COLUMN_BACKEND_ID:
						Integer id = (Integer) rowEntry.getValue();
						end.setBackendId(id);
						break;
					case COLUMN_ANALYSIS_URI:
						end.setAnalysisUri((String) rowEntry.getValue());
						break;
					case COLUMN_ENABLED:
						end.setEnabled(BooleanUtils.toBoolean((Integer) rowEntry.getValue()));
						break;
					case Definitions.COLUMN_DESCRIPTION:
						end.setDescription((String) rowEntry.getValue());
						break;
					case COLUMN_DEFAULT_TASK_DATAGROUPS:
						DataGroups dg = new DataGroups();
						dg.initialize((String)rowEntry.getValue());
						end.setDefaultTaskDataGroups(dg);
						break;
					default:
						LOGGER.warn("Ignored unknown column: "+rowEntry.getKey());
						break;
				}
			}
			ends.add(end);
		}
		return ends;
	}
	
	/**
	 * helper method for retrieving (and setting) the capabilities for the given list of backend
	 * 
	 * @param ends
	 */
	private void setCapabilities(List<AnalysisBackend> ends){
		if(ends == null){
			LOGGER.debug("No backends in the list.");
			return;
		}
		
		Integer[] ids = new Integer[ends.size()];
		int index = 0;
		for(Iterator<AnalysisBackend> iter = ends.iterator();iter.hasNext();){	// get ids
			ids[index++] = iter.next().getBackendId();
		}
	
		SQLSelectBuilder sql = new SQLSelectBuilder(TABLE_BACKEND_CAPABILITIES);
		sql.addSelectColumns(TABLE_BACKEND_CAPABILITIES_ALL_COLUMNS);
		sql.addWhereClause(new AndClause(Definitions.COLUMN_BACKEND_ID, ids, SQLType.INTEGER));
		
		List<Map<String,Object>> rows = getJdbcTemplate().queryForList(sql.toSQLString(), sql.getValues(), sql.getValueTypes());
		for(Iterator<Map<String,Object>> rowIter = rows.iterator();rowIter.hasNext();){
			Integer backendId = null;
			Capability capability = null;
			for(Entry<String,Object> rowEntry : rowIter.next().entrySet()){	// go through the returned rows
				switch(rowEntry.getKey()){
					case Definitions.COLUMN_BACKEND_ID:
						backendId = (Integer) rowEntry.getValue();
						break;
					case COLUMN_CAPABILITY:
						capability = Capability.fromInt((Integer) rowEntry.getValue());
						break;
					default:
						LOGGER.warn("Ignored unknown column: "+rowEntry.getKey());
						break;
				}
			}	// for row
			
			for(Iterator<AnalysisBackend> bIter = ends.iterator();bIter.hasNext();){	// find the owner of this capability
				AnalysisBackend end = bIter.next();
				if(end.getBackendId().equals(backendId)){
					end.addCapability(capability);
					break;
				}
			}	// for backends
		}	// for rows
	}

	/**
	 * 
	 * @param backendIds if != null, only the requested backends will be returned
	 * @return list of backends or null if none available. 
	 */
	public List<AnalysisBackend> getBackends(List<Integer> backendIds){
		SQLSelectBuilder sqlBuilder = new SQLSelectBuilder(TABLE_BACKENDS);
		sqlBuilder.addSelectColumns(TABLE_BACKENDS_ALL_COLUMNS);

		if(backendIds != null){
			sqlBuilder.addWhereClause(new AndClause(Definitions.COLUMN_BACKEND_ID, backendIds.toArray(), SQLType.INTEGER));
		}
		
		List<AnalysisBackend> ends = extractBackends(getJdbcTemplate().queryForList(sqlBuilder.toSQLString(), sqlBuilder.getValues(), sqlBuilder.getValueTypes()));
		setCapabilities(ends);
		return ends;
	}
	
	/**
	 * 
	 * @return list of back-ends or null if none was found
	 */
	public List<AnalysisBackend> getEnabledBackends(){
		return extractBackends(getJdbcTemplate().queryForList(SQL_GET_ENABLED_BACKENDS));
	}
	
	/**
	 * 
	 * @param backendId
	 * @return the back-end with the given is or null if not found
	 */
	public AnalysisBackend getBackend(Integer backendId){
		List<Map<String,Object>> rows = getJdbcTemplate().queryForList(SQL_SELECT_BACKEND_BY_BACKEND_ID, new Object[]{backendId}, SQL_SELECT_BACKEND_BY_BACKEND_ID_TYPES);
		if(rows.isEmpty()){
			return null;
		}else{
			List<AnalysisBackend> backends = extractBackends(rows);
			setCapabilities(backends);
			return backends.get(0);
		}
	}
}
