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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import service.tut.pori.contentsuggest.AutoCompleteResult;
import service.tut.pori.contentsuggest.Definitions;
import core.tut.pori.http.ResponseData;

/**
 * Response example
 * 
 */
@XmlRootElement(name=service.tut.pori.contentsuggest.reference.Definitions.ELEMENT_EXAMPLE)
@XmlAccessorType(XmlAccessType.NONE)
public class Example extends ResponseData {
	@XmlElement(name=Definitions.ELEMENT_AUTOCOMPLETE_RESULTS)
	private AutoCompleteResult _autoCompleteResult = null;

	/**
	 * @return the autoCompleteResult
	 */
	public AutoCompleteResult getAutoCompleteResult() {
		return _autoCompleteResult;
	}

	/**
	 * @param autoCompleteResult the autoCompleteResult to set
	 */
	public void setAutoCompleteResult(AutoCompleteResult autoCompleteResult) {
		_autoCompleteResult = autoCompleteResult;
	}
}
