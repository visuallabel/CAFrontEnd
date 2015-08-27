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
package service.tut.pori.facebookjazz;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;

import service.tut.pori.contentanalysis.Definitions;
import service.tut.pori.facebookjazz.WeightModifier.WeightModifierType;
import core.tut.pori.dao.clause.AndClause;
import core.tut.pori.dao.clause.SQLClause.SQLType;
import core.tut.pori.dao.SQLDAO;
import core.tut.pori.dao.SQLSelectBuilder;
import core.tut.pori.users.UserIdentity;


/**
 * DAO for storing facebook summarization information.
 * 
 * Note that this class stores details about the user's preferences for the summarization, the actual results are stored as media objects, using {@link service.tut.pori.contentanalysis.MediaObjectDAO}.
 *
 */
public class FacebookJazzDAO extends SQLDAO{
	private static final Logger LOGGER = Logger.getLogger(FacebookJazzDAO.class);
	/* table */
	private static final String TABLE_WEIGHT_MODIFIERS = DATABASE+".fbj_weight_modifiers";
	/* columns */
	private static final String COLUMN_WEIGHT_MODIFIER_TYPE = "modifier_type";
	/* sql scripts */
	private static final String[] SQL_COLUMNS_GET_WEIGHT_MODIFIERS = new String[]{Definitions.COLUMN_VALUE,COLUMN_WEIGHT_MODIFIER_TYPE};	
	
	private static final String SQL_REMOVE_WEIGHT_MODIFIERS = "DELETE FROM "+TABLE_WEIGHT_MODIFIERS+" WHERE "+COLUMN_USER_ID+"=?";
	private static final int[] SQL_REMOVE_WEIGHT_MODIFIERS_SQL_TYPES = new int[]{SQLType.LONG.toInt()};

	private static final String SQL_SET_WEIGHT_MODIFIERS = "INSERT INTO "+TABLE_WEIGHT_MODIFIERS+" ("+COLUMN_USER_ID+", "+Definitions.COLUMN_VALUE+", "+COLUMN_WEIGHT_MODIFIER_TYPE+", "+COLUMN_ROW_CREATED+") VALUES (?,?,?,NOW()) ON DUPLICATE KEY UPDATE "+Definitions.COLUMN_VALUE+"=?";
	private static final int[] SQL_SET_WEIGHT_MODIFIERS_SQL_TYPES = new int[]{SQLType.LONG.toInt(), SQLType.INTEGER.toInt(), SQLType.INTEGER.toInt(), SQLType.INTEGER.toInt()};
	
	/**
	 * 
	 * @param userId use null to retrieve defaults
	 * @return list of weight modifiers for the given user of null if none available
	 */
	public WeightModifierList getWeightModifiers(UserIdentity userId){
		SQLSelectBuilder sql = new SQLSelectBuilder(TABLE_WEIGHT_MODIFIERS);
		sql.addSelectColumns(SQL_COLUMNS_GET_WEIGHT_MODIFIERS);
		Long userIdValue = null;
		if(!UserIdentity.isValid(userId)){
			LOGGER.debug("Invalid user id, returning default values...");
		}else{
			userIdValue = userId.getUserId();
		}
		sql.addWhereClause(new AndClause(COLUMN_USER_ID, userIdValue, SQLType.LONG));
			
		List<Map<String,Object>> rows = getJdbcTemplate().queryForList(sql.toSQLString(), sql.getValues(), sql.getValueTypes());
		if(rows.isEmpty()){
			LOGGER.debug("No modifiers.");
			return null;
		}
		
		WeightModifierList list = new WeightModifierList();
		for(Map<String, Object> row : rows){
			list.setWeightModifier(new WeightModifier(WeightModifierType.fromInt((int) row.get(COLUMN_WEIGHT_MODIFIER_TYPE)), (Integer) row.get(Definitions.COLUMN_VALUE)));
		}
		return list;
	}
	
	/**
	 * Set weight modifiers for the given user
	 * 
	 * @param userId use null to set defaults
	 * @param modifiers
	 */
	public void setWeightModifiers(UserIdentity userId, WeightModifierList modifiers){
		if(!WeightModifierList.isValid(modifiers)){
			LOGGER.warn("Invalid modifiers.");
			return;
		}
		
		Object[] ob = new Object[]{null, null, null, null};
		if(!UserIdentity.isValid(userId)){
			LOGGER.debug("Invalid userId, setting default weights...");
		}else{
			ob[0] = userId.getUserId();
			LOGGER.debug("Clearing all previous weight values for user, id: "+ob[0]);
			removeWeightModifers(userId);
		}
		
		JdbcTemplate t = getJdbcTemplate();
		for(WeightModifier modifier : modifiers.getModifiers()){
			ob[1] = modifier.getValue();
			ob[2] = modifier.getType().toInt();
			ob[3] = ob[1];
			t.update(SQL_SET_WEIGHT_MODIFIERS, ob, SQL_SET_WEIGHT_MODIFIERS_SQL_TYPES);
		}
	}
	
	/**
	 * Note: this will NOT allow removal of default values
	 * @param userId non-null user
	 */
	public void removeWeightModifers(UserIdentity userId){
		if(!UserIdentity.isValid(userId)){
			LOGGER.warn("Invalid user.");
		}else{
			getJdbcTemplate().update(SQL_REMOVE_WEIGHT_MODIFIERS, new Object[]{userId.getUserId()}, SQL_REMOVE_WEIGHT_MODIFIERS_SQL_TYPES);
		}
	}
}
