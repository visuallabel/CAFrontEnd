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
package service.tut.pori.fileservice;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

import core.tut.pori.dao.clause.AndClause;
import core.tut.pori.dao.clause.SQLClause.SQLType;
import core.tut.pori.dao.SQLDAO;
import core.tut.pori.dao.SQLDeleteBuilder;
import core.tut.pori.dao.SQLSelectBuilder;
import core.tut.pori.http.parameters.Limits;
import core.tut.pori.users.UserIdentity;

/**
 * DAO for saving, retrieving and modifying the details of saved files.
 */
public class FileDAO extends SQLDAO {
	private static final Logger LOGGER = Logger.getLogger(FileDAO.class);
	/* tables */
	private static final String TABLE_FILES = DATABASE+".fs_files";
	/* columns */
	private static final String COLUMN_FILE_ID = "file_id";
	private static final String COLUMN_ORIGINAL_NAME = "original_name";
	private static final String COLUMN_SAVED_NAME = "saved_name";
	/* sql scripts */
	private static final String[] COLUMNS_GET_FILES = {COLUMN_FILE_ID, COLUMN_ORIGINAL_NAME, COLUMN_SAVED_NAME, COLUMN_USER_ID};
	private static final String[] COLUMNS_SAVE_FILE = {COLUMN_ORIGINAL_NAME, COLUMN_SAVED_NAME, COLUMN_USER_ID};

	private static final String SQL_DELETE_FILES_FOR_USER = "DELETE FROM "+TABLE_FILES+" WHERE "+COLUMN_USER_ID+"=?";
	private static final int[] SQL_DELETE_FILES_FOR_USER_SQL_TYPES = {SQLType.LONG.toInt()};
	
	/**
	 * 
	 * @param file
	 */
	public void save(File file) {
		SimpleJdbcInsert insert = new SimpleJdbcInsert(getJdbcTemplate());
		insert.withTableName(TABLE_FILES);
		insert.withoutTableColumnMetaDataAccess();
		insert.usingColumns(COLUMNS_SAVE_FILE);
		insert.setGeneratedKeyName(COLUMN_FILE_ID);
		
		Map<String, Object> values = new HashMap<>(COLUMNS_SAVE_FILE.length);
		values.put(COLUMN_ORIGINAL_NAME, file.getName());
		values.put(COLUMN_SAVED_NAME, file.getSavedName());
		values.put(COLUMN_USER_ID, file.getUserId().getUserId());
		file.setFileId((Long) insert.executeAndReturnKey(values));
	}

	/**
	 * 
	 * @param authenticatedUser
	 * @param fileIds if null or empty, all files for the given user will be deleted
	 * @return list of deleted files or null if nothing was deleted
	 */
	public FileList delete(UserIdentity authenticatedUser, long[] fileIds) {
		FileList files = getFiles(authenticatedUser, fileIds, null);
		if(FileList.isEmpty(files)){
			LOGGER.debug("No files found.");
			return null;
		}
		
		if(ArrayUtils.isEmpty(fileIds)){
			Object[] ob = new Object[]{authenticatedUser.getUserId()};
			LOGGER.debug("Removed "+getJdbcTemplate().update(SQL_DELETE_FILES_FOR_USER, ob, SQL_DELETE_FILES_FOR_USER_SQL_TYPES)+" files for user, id: "+ob[0]);
		}else{
			List<File> fileList = files.getFiles();
			List<Long> fileIdList = new ArrayList<>(fileList.size());
			for(File file : fileList){
				fileIdList.add(file.getFileId());
			}
			SQLDeleteBuilder sql = new SQLDeleteBuilder(TABLE_FILES);
			sql.addWhereClause(new AndClause(COLUMN_FILE_ID, fileIdList, SQLType.LONG));
			LOGGER.debug("Removed "+sql.execute(getJdbcTemplate())+" files for user, id: "+authenticatedUser.getUserId());
		}
		return files;
	}

	/**
	 * 
	 * @param authenticatedUser
	 * @param fileIds if null or empty, or users files will be returned
	 * @param limits optional limits parameter, if missing, all files will be returned
	 * @return list of files or null if none found
	 */
	public FileList getFiles(UserIdentity authenticatedUser, long[] fileIds, Limits limits) {
		SQLSelectBuilder sql = new SQLSelectBuilder(TABLE_FILES);
		sql.addSelectColumns(COLUMNS_GET_FILES);
		sql.addWhereClause(new AndClause(COLUMN_USER_ID, authenticatedUser.getUserId(), SQLType.LONG));
		if(!ArrayUtils.isEmpty(fileIds)){
			LOGGER.debug("Adding file id filter.");
			sql.addWhereClause(new AndClause(COLUMN_FILE_ID, fileIds));
		}
		sql.setLimits(limits);
		List<Map<String, Object>> rows = getJdbcTemplate().queryForList(sql.toSQLString(), sql.getValues(), sql.getValueTypes());
		if(rows.isEmpty()){
			LOGGER.debug("No files found.");
			return null;
		}
		
		List<File> files = new ArrayList<>(rows.size());
		for(Map<String, Object> row : rows){
			files.add(extractFile(row));
		}
		
		return FileList.getFileList(files);
	}
	
	/**
	 * 
	 * @param row
	 * @return file extracted from the given row map
	 */
	private File extractFile(Map<String, Object> row){
		File file = new File();
		for(Entry<String, Object> e : row.entrySet()){
			switch(e.getKey()){
				case COLUMN_FILE_ID:
					file.setFileId((Long) e.getValue());
					break;
				case COLUMN_ORIGINAL_NAME:
					file.setName((String) e.getValue());
					break;
				case COLUMN_SAVED_NAME:
					file.setSavedName((String) e.getValue());
					break;
				case COLUMN_USER_ID:
					file.setUserId(new UserIdentity((Long) e.getValue()));
					break;
				default:
					LOGGER.warn("Ignored unknown column: "+e.getKey());
					break;
			} // switch
		}
		return file;
	}
}
