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
package service.tut.pori.contentanalysistest;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import core.tut.pori.http.ResponseData;
import service.tut.pori.contentanalysis.AnalysisBackend;

/**
 * A list of back-end details, which can be directly used as a payload for Response.
 *
 */
@XmlRootElement(name="backends")
@XmlAccessorType(XmlAccessType.NONE)
public class BackendDetails extends ResponseData{
	@XmlElement(name = "backend")
	private List<AnalysisBackend> _ends = null;

	/**
	 * 
	 * @return the list of back-ends
	 */
	public List<AnalysisBackend> getBackends(){
		return _ends;
	}
	
	/**
	 * 
	 * @param ends
	 */
	public void setBackends(List<AnalysisBackend> ends){
		_ends = ends;
	}
}
