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
package service.tut.pori.twitterjazz.reference;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import service.tut.pori.contentanalysis.Definitions;
import core.tut.pori.http.annotations.HTTPMethodParameter;
import core.tut.pori.http.annotations.HTTPService;
import core.tut.pori.http.annotations.HTTPServiceMethod;
import core.tut.pori.http.parameters.InputStreamParameter;
import core.tut.pori.utils.XMLFormatter;

/**
 * 
 * Reference implementation for Back-end APIs. This class defines the APIs implemented by analysis back-end services.
 * As a general note, the back-end should ignore any unknown elements it cannot process.
 * 
 */
@HTTPService(name = service.tut.pori.twitterjazz.reference.Definitions.SERVICE_TJ_REFERENCE_BACKEND)
public class BackendService {
	private static final Logger LOGGER = Logger.getLogger(BackendService.class);
	private XMLFormatter _xmlFormatter = new XMLFormatter();
	
	/**
	 * The request is to be sent in the body of POST method. The Content-Type header MUST be set to "text/xml". The character set MUST be UTF-8. For example, "Content-Type: text/xml; charset=UTF-8".
	 * 
	 * <h2>Example Query:</h2>
	 *
	 * POST /rest/{@value service.tut.pori.twitterjazz.reference.Definitions#SERVICE_TJ_REFERENCE_BACKEND}/{@value service.tut.pori.contentanalysis.Definitions#METHOD_ADD_TASK}<br>
	 * Content-Type: text/xml; charset=UTF-8<br><br>
	 *
	 * <b>[HTTP BODY STARTS]</b><br>
	 * 
	 * {@doc.restlet service="[service.tut.pori.twitterjazz.reference.Definitions#SERVICE_TJ_REFERENCE_EXAMPLE]" method="[service.tut.pori.contentanalysis.Definitions#ELEMENT_TASK_DETAILS]" type="GET" query="" body_uri=""} <br>
	 * 
	 * <b>[HTTP BODY ENDS]</b><br>
	 *
	 * <h2>Example Result:</h2>
	 * 
	 * {@doc.restlet service="[service.tut.pori.twitterjazz.reference.Definitions#SERVICE_TJ_REFERENCE_BACKEND]" method="[service.tut.pori.contentanalysis.Definitions#METHOD_ADD_TASK]" type="POST" query="" body_uri="[service.tut.pori.twitterjazz.reference.Definitions#SERVICE_TJ_REFERENCE_EXAMPLE]/[service.tut.pori.contentanalysis.Definitions#ELEMENT_TASK_DETAILS]"}
	 * 
	 * @param xml The HTTP body. Only the workload data should be in the body. See {@link service.tut.pori.twitterjazz.reference.TwitterTaskDetails}
	 */
	@HTTPServiceMethod(name = Definitions.METHOD_ADD_TASK, acceptedMethods={core.tut.pori.http.Definitions.METHOD_POST})
	public void addTask(@HTTPMethodParameter(name = InputStreamParameter.PARAMETER_DEFAULT_NAME, bodyParameter = true) InputStreamParameter xml) {
		try {
			String body = IOUtils.toString(xml.getValue()); // read the body
			LOGGER.debug(body); // print to debug
			try(InputStream input = IOUtils.toInputStream(body)){ // convert back to stream for unmarshal
				TJReferenceCore.addTask(_xmlFormatter.toObject(input, TwitterTaskDetails.class));
			}
		} catch (IOException ex) {
			LOGGER.error(ex, ex);
		}
	}
}
