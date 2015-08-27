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
package service.tut.pori.contentsuggest.reference;

import org.apache.log4j.Logger;

import service.tut.pori.contentsuggest.AutoCompleteResult;
import core.tut.pori.http.parameters.DataGroups;
import core.tut.pori.http.parameters.Limits;
import core.tut.pori.users.UserIdentity;

/**
 * The reference implementations for Content Suggest Service.
 *
 */ 
public final class ContentSuggestReferenceCore {
	private static final CSXMLObjectCreator CREATOR = new CSXMLObjectCreator();
	private static final Logger LOGGER = Logger.getLogger(ContentSuggestReferenceCore.class);
	
	/**
	 * 
	 */
	private ContentSuggestReferenceCore(){
		// nothing needed
	}
	
	/**
	 * 
	 * @param authenticatedUser 
	 * @param dataGroups
	 * @param limits
	 * @param query the user provided query. Note that the query is not validated and bears no relation to the returned results.
	 * @return response
	 * @throws IllegalArgumentException 
	 */
	public static AutoCompleteResult suggest(UserIdentity authenticatedUser, DataGroups dataGroups, Limits limits, String query) throws IllegalArgumentException {
		LOGGER.info((authenticatedUser == null ? "No logged in user." : "Ignoring the logged in user, id: "+authenticatedUser.getUserId()));	// only notify of the logged in status
		return CREATOR.createAutoCompleteResult(limits, query);
	}

	/**
	 * 
	 * @param limits
	 * @return randomly generated result
	 */
	public static AutoCompleteResult generateAutoCompleteResult(Limits limits) {
		return CREATOR.createAutoCompleteResult(limits);
	}
}
