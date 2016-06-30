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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.response.Group;
import org.apache.solr.client.solrj.response.GroupCommand;
import org.apache.solr.client.solrj.response.QueryResponse;

import core.tut.pori.http.ResponseData;

/**
 * Class for representing a response received from Solr Autocomplete. This class can also be used directly as a http Response payload.
 * 
 * <h3>XML Example</h3>
 * 
 * {@doc.restlet service="[service.tut.pori.contentsuggest.reference.Definitions#SERVICE_CS_REFERENCE_EXAMPLE]" method="[service.tut.pori.contentsuggest.Definitions#ELEMENT_AUTOCOMPLETE_RESULTS]" type="GET" query="" body_uri=""}
 */
@XmlRootElement(name=Definitions.ELEMENT_AUTOCOMPLETE_RESULTS)
@XmlAccessorType(XmlAccessType.NONE)
public class AutoCompleteResult extends ResponseData {
	@XmlElement(name=Definitions.ELEMENT_COLLATION)
	private String _collation = null;
	@XmlElementWrapper(name=Definitions.ELEMENT_SUGGESTION_LIST)
	@XmlElement(name=Definitions.ELEMENT_SUGGESTION)
	private List<String> _suggestionList = null;
	
	/**
	 * The collection of query strings that are not part of the suggested element.
	 * 
	 * @return the collation
	 * @see #setCollation(String)
	 */
	public String getCollation() {
		return _collation;
	}
	
	/**
	 * 
	 * @param collation
	 */
	public void setCollation(String collation) {
		_collation = collation;
	}
	
	/**
	 * 
	 * @return the list of suggestions for this auto-complete response
	 * @see #setSuggestionList(List)
	 */
	public List<String> getSuggestionList() {
		return _suggestionList;
	}
	
	/**
	 * 
	 * @param suggestionList
	 * @see #setSuggestionList(List)
	 */
	public void setSuggestionList(List<String> suggestionList) {
		_suggestionList = suggestionList;
	}
	
	/**
	 * Generates a new AutoCompleteResponse based on Solr QueryResponse
	 * @param response
	 * @return the given response converted to AutoCompleteResponse
	 * @see QueryResponse
	 */
	protected static AutoCompleteResult fromQueryResponse(QueryResponse response){
		Logger.getLogger(AutoCompleteResult.class).debug(response.getResponseHeader().getAll("params"));
		
		AutoCompleteResult acr = new AutoCompleteResult();
		List<String> suggestionList = null;
		if(response.getGroupResponse().getValues().size() > 0){
			suggestionList = new ArrayList<>();
		}
		for(GroupCommand groupResult : response.getGroupResponse().getValues()){
			//Logger.getLogger(AutoCompleteResponse.class).debug(groupResult.getName()+" "+groupResult.getMatches());
			for(Group group : groupResult.getValues()){
				suggestionList.add(group.getGroupValue()); //group.getResult().get(0).getFieldValue("value_s"));
			}
		}
		if(suggestionList != null && suggestionList.size() > 0){
			acr.setSuggestionList(suggestionList);
		}
		return acr;
	}
}
