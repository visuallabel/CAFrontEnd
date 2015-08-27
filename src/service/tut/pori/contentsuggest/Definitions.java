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
package service.tut.pori.contentsuggest;

/**
 * Definitions for contentsuggest.
 *
 */
public final class Definitions {
	/**
	 * 
	 */
	private Definitions(){
		// nothing needed
	}
	
	/* services */
	/** service name declaration */
	public static final String SERVICE_CS = "cs";
	
	/* methods */
	/** service method declaration */
	public static final String METHOD_SUGGEST = "suggest";
	
	/* parameters */
	/** service method parameter declaration */
	public static final String PARAMETER_QUERY = "query";

	/* xml elements */
	/** xml element declaration */
	public static final String ELEMENT_AUTOCOMPLETE_RESULTS = "autoCompleteResults";
	/** xml element declaration */
	public static final String ELEMENT_COLLATION = "collation";
	/** xml element declaration */
	public static final String ELEMENT_SUGGESTION = "suggestion";
	/** xml element declaration */
	public static final String ELEMENT_SUGGESTION_LIST = "suggestionList";
	
	/* common */
	/** default query limits */
	public static final String DEFAULT_LIMITS = "0-9";
	/** separator for multiple queries */
	public static final char QUERY_SEPARATOR = ' ';
}
