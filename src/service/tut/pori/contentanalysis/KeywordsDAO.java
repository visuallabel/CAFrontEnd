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
package service.tut.pori.contentanalysis;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowCallbackHandler;

import service.tut.pori.contentanalysis.MediaObject.ConfirmationStatus;
import service.tut.pori.contentanalysis.MediaObject.MediaObjectType;
import core.tut.pori.context.ServiceInitializer;
import core.tut.pori.dao.SQLDAO;
import core.tut.pori.dao.SQLSelectBuilder;
import core.tut.pori.dao.clause.AndClause;
import core.tut.pori.dao.clause.SQLClause.SQLType;

/**
 * A DAO that can be used to handle keywords / friendly keywords mapping
 */
public class KeywordsDAO extends SQLDAO{
	private static final Logger LOGGER = Logger.getLogger(KeywordsDAO.class);
	/* tables */
	private static final String TABLE_FRIENDLY_KEYWORDS = DATABASE+".ca_photo_friendly_keywords";
	/* columns */
	private static final String COLUMN_FRIENDLY_VALUE = "friendly_value";
	/* sql scripts */
	private static final String[] ASSIGN_FRIENDLY_SELECT_COLUMNS = new String[]{Definitions.COLUMN_BACKEND_ID, COLUMN_FRIENDLY_VALUE, Definitions.COLUMN_VALUE};

	/**
	 * Replaces all media object values for media objects of type keyword,
	 * which have a valid known friendly keyword replacement:
	 * - media objects without backendId will be ignored
	 * - media objects not of type keyword will be ignored
	 * - media objects with ConfirmationStatus not of Candidate will be ignored
	 * - media object (keyword) value is matched case-sensitively, an identical value with friendly keyword must be found
	 * - if media object value is found in the database without a friendly value, no friendly value replacement is performed, and the object is set to ConfirmationStatus.NO_FRIENDLY_KEYWORD
	 * 
	 * Note: this will set the friendly keyword as the name of the object, not as the value, the original value is preserved as the name. Old name (if any) is overridden.
	 * 
	 * @param objects
	 */
	public void assignFriendlyKeywords(MediaObjectList objects){
		if(!ServiceInitializer.getPropertyHandler().getSystemProperties(CAProperties.class).isResolveFriendlyKeywords()){
			LOGGER.debug("Friendly keyword assignment disabled by property configuration.");
			return;
		}
		
		if(MediaObjectList.isEmpty(objects)){
			LOGGER.debug("Empty media object list.");
			return;
		}
		
		/**
		 * list of values for which the friendly values will be retrieved for.
		 * We could also create value-backendId map, as the values are mapped by backend in the database,
		 * but in the current implementation (and database listing) the value-backend relations are unique
		 * (one value maps only to a single backend), and thus, creating extra sql where (value=? AND (backend_id=? OR backend_id IS NULL) pairs is not needed.
		 */
		ArrayList<String> values = new ArrayList<>();
		
		final LinkedList<MediaObject> keywords = new LinkedList<>();
		for(Iterator<MediaObject> iter = objects.getMediaObjects().iterator(); iter.hasNext();){
			MediaObject o = iter.next();
			if(o.getBackendId() != null && MediaObjectType.KEYWORD.equals(o.getMediaObjectType()) && ConfirmationStatus.CANDIDATE.equals(o.getConfirmationStatus())){	// only take keywords with backendIds and with status candidate
				keywords.add(o);
				values.add(o.getValue());
			}
		}
		
		if(values.isEmpty()){
			LOGGER.debug("No valid media objects for friendly keyword replacement.");
			return;
		}
		
		SQLSelectBuilder sql = new SQLSelectBuilder(TABLE_FRIENDLY_KEYWORDS);
		sql.addSelectColumns(ASSIGN_FRIENDLY_SELECT_COLUMNS);
		sql.addWhereClause(new AndClause(Definitions.COLUMN_VALUE, values.toArray(), SQLType.STRING));
		getJdbcTemplate().query(sql.toSQLString(), sql.getValues(), sql.getValueTypes(), new RowCallbackHandler() {	
			@Override
			public void processRow(ResultSet set) throws SQLException {
				String value = set.getString(Definitions.COLUMN_VALUE);
				String friendlyValue = set.getString(COLUMN_FRIENDLY_VALUE);
				Integer backendId = set.getInt(Definitions.COLUMN_BACKEND_ID);
				if(set.wasNull()){
					backendId = null;
				}
				for(Iterator<MediaObject> kIter = keywords.iterator(); kIter.hasNext();){
					MediaObject keyword = kIter.next();
					if(value.equals(keyword.getValue())){
						if(backendId != null && backendId.equals(keyword.getBackendId())){ // if the mapping does not contain backendId, set the friendly value, but do not remove from the list (in case more accurate match is found later on)
							kIter.remove();	// remove if the backend ids match
						}
						if(StringUtils.isBlank(friendlyValue)){	// do not replace name if no friendly value
							keyword.setConfirmationStatus(ConfirmationStatus.NO_FRIENDLY_KEYWORD);
						}else{
							keyword.setName(friendlyValue);	// set the friendly value as the name
						}
					}	// if, do not break here, there might be duplicates in the list
				}	// for keywords
			}	// processRow
		});
		for(Iterator<MediaObject> kIter = keywords.iterator(); kIter.hasNext();){ // set NO_FRIENDLY_KEYWORD for everything left over
			kIter.next().setConfirmationStatus(ConfirmationStatus.NO_FRIENDLY_KEYWORD);
		}
	}
}
