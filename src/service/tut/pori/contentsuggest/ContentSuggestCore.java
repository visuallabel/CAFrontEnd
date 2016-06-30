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
package service.tut.pori.contentsuggest;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.response.QueryResponse;

import service.tut.pori.contentanalysis.MediaObjectDAO;
import core.tut.pori.context.ServiceInitializer;
import core.tut.pori.http.parameters.DataGroups;
import core.tut.pori.http.parameters.Limits;
import core.tut.pori.users.UserIdentity;

/**
 * Content suggest core methods.
 *
 */
public final class ContentSuggestCore {

	/**
	 * Auto-complete method. Only the last part of the query ({@value service.tut.pori.contentsuggest.Definitions#QUERY_SEPARATOR} delimiter) is sent to the suggestion back-end.
	 * 
	 * @param authenticatedUser
	 * @param dataGroups 
	 * @param limits 
	 * @param query 
	 * @return response
	 * @throws IllegalArgumentException 
	 */
	public static AutoCompleteResult suggest(UserIdentity authenticatedUser, DataGroups dataGroups, Limits limits, String query) throws IllegalArgumentException{
		String queryArray[] = StringUtils.split(query, Definitions.QUERY_SEPARATOR);
		if(queryArray.length < 1){
			throw new IllegalArgumentException("Query cannot be empty");
		}
		QueryResponse res = ServiceInitializer.getDAOHandler().getDAO(MediaObjectDAO.class).getSuggestions(authenticatedUser, dataGroups, limits, queryArray[queryArray.length-1]);
		AutoCompleteResult acr = AutoCompleteResult.fromQueryResponse(res);
		String collation[] = Arrays.copyOf(queryArray, queryArray.length-1);
		acr.setCollation(StringUtils.join(collation, " "));
		return acr;
	}
	
	/**
	 * 
	 */
	private ContentSuggestCore(){
		// nothing needed
	}
}
