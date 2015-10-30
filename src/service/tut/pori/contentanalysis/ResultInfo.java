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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


/**
 * A simple class that shows details about a search query. 
 * Generally used with lists to indicate the total number of results in case the list itself contains only a partial set of results.
 * 
 * ResultInfo should not be present, if not especially requested with data group {@value service.tut.pori.contentanalysis.Definitions#DATA_GROUP_RESULT_INFO}
 * 
 * <h2>Optional Elements</h2>
 * <ul>
 *  <li>{@value service.tut.pori.contentanalysis.Definitions#ELEMENT_START_ITEM}. The requested start item, if available.</li>
 *  <li>{@value service.tut.pori.contentanalysis.Definitions#ELEMENT_END_ITEM}. The requested end item, if available.</li>
 * </ul>
 * 
 * <h3>XML Example</h3>
 * 
 * {@doc.restlet service="[service.tut.pori.contentanalysis.reference.Definitions#SERVICE_CA_REFERENCE_EXAMPLE]" method="[service.tut.pori.contentanalysis.Definitions#ELEMENT_RESULT_INFO]" type="GET" query="" body_uri=""}
 * 
 */
@XmlRootElement(name=Definitions.ELEMENT_RESULT_INFO)
@XmlAccessorType(XmlAccessType.NONE)
public class ResultInfo {
	@XmlElement(name = Definitions.ELEMENT_END_ITEM)
	private Long _endItem = null;
	@XmlElement(name = Definitions.ELEMENT_RESULT_COUNT)
	private Long _resultCount = null;
	@XmlElement(name = Definitions.ELEMENT_START_ITEM)
	private Long _startItem = null;

	/**
	 * for serialization
	 */
	public ResultInfo(){
		// nothing needed
	}
	
	/**
	 * 
	 * @param startItem
	 * @param endItem
	 * @param resultCount
	 */
	public ResultInfo(long startItem, long endItem, long resultCount){
		_startItem = startItem;
		_endItem = endItem;
		_resultCount = resultCount;
	}
	
	/**
	 * The total number of results for the request. Note that in some cases this may only be an estimate.
	 * 
	 * @return number of results
	 * @see #setResultCount(Long)
	 */  
	public Long getResultCount() {
		return _resultCount;
	}

	/**
	 * 
	 * @param resultCount
	 * @see #getResultCount()
	 */
	public void setResultCount(Long resultCount){
		_resultCount = resultCount;
	}

	/**
	 * @return the endItem
	 */
	public Long getEndItem() {
		return _endItem;
	}

	/**
	 * @param endItem the endItem to set
	 */
	public void setEndItem(Long endItem) {
		_endItem = endItem;
	}

	/**
	 * @return the startItem
	 */
	public Long getStartItem() {
		return _startItem;
	}

	/**
	 * @param startItem the startItem to set
	 */
	public void setStartItem(Long startItem) {
		_startItem = startItem;
	}
}
