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
package service.tut.pori.fuzzyvisuals;

import core.tut.pori.http.annotations.HTTPAuthenticationParameter;
import core.tut.pori.http.annotations.HTTPMethodParameter;
import core.tut.pori.http.annotations.HTTPService;
import core.tut.pori.http.annotations.HTTPServiceMethod;
import core.tut.pori.http.parameters.AuthenticationParameter;
import core.tut.pori.http.parameters.InputStreamParameter;
import core.tut.pori.utils.XMLFormatter;

/**
 * FuzzyVisuals back-end service
 * 
 */
@HTTPService(name = Definitions.SERVICE_FV)
public class FuzzyVisualsService {
	private XMLFormatter _formatter = new XMLFormatter();
	
	/**
	 * 
	 * @param authenticatedUser 
	 * @param xml
	 */
	@HTTPServiceMethod(name = service.tut.pori.contentanalysis.Definitions.METHOD_ADD_TASK, acceptedMethods={core.tut.pori.http.Definitions.METHOD_POST})
	public void addTask(
			@HTTPAuthenticationParameter AuthenticationParameter authenticatedUser, // accept any authorized user
			@HTTPMethodParameter(name = InputStreamParameter.PARAMETER_DEFAULT_NAME, bodyParameter = true) InputStreamParameter xml) 
	{
		FuzzyVisualsCore.addTask(_formatter.toObject(xml.getValue(), TaskDetails.class));
	}
}
