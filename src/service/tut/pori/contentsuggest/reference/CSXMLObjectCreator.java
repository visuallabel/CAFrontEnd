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
package service.tut.pori.contentsuggest.reference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import service.tut.pori.contentsuggest.AutoCompleteResult;
import core.tut.pori.http.parameters.Limits;

/**
 * 
 * class that can be used to created example objects/object lists
 *
 */
public class CSXMLObjectCreator {
	private static final Logger LOGGER = Logger.getLogger(CSXMLObjectCreator.class);
	private static final int MAX_SUGGESTIONS = 100;
	private static final int QUERY_LENGTH = 10;
	private static final int SUGGESTION_LENGTH = 10;

	/**
	 * 
	 * @param limits
	 * @param query
	 * @return randomly generated autocomplete result
	 */
	public AutoCompleteResult createAutoCompleteResult(Limits limits, String query) {
		String queryArray[] = StringUtils.split(query, " ");
		if(queryArray.length < 1){
			throw new IllegalArgumentException("Query cannot be empty");
		}
		int maxSuggestions = limits.getMaxItems();
		if(maxSuggestions > MAX_SUGGESTIONS){
			LOGGER.debug("Max items more than max, limiting to "+MAX_SUGGESTIONS);
			maxSuggestions = MAX_SUGGESTIONS;
		}
		List<String> suggestions = new ArrayList<>(maxSuggestions);
		for(int i=0;i<maxSuggestions;++i){
			suggestions.add(queryArray[queryArray.length-1] + RandomStringUtils.randomAlphabetic(SUGGESTION_LENGTH));
		}
		
		AutoCompleteResult acr = new AutoCompleteResult();
		acr.setSuggestionList(suggestions);
		String collation[] = Arrays.copyOf(queryArray, queryArray.length-1);
		acr.setCollation(StringUtils.join(collation, " "));
		return acr;
	}

	/**
	 * 
	 * @param limits
	 * @return randomly generated autocomplete result
	 */
	public AutoCompleteResult createAutoCompleteResult(Limits limits) {
		return createAutoCompleteResult(limits, RandomStringUtils.randomAlphabetic(QUERY_LENGTH));
	}

}
