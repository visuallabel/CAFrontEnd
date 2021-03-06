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
package service.tut.pori.twitterjazz.reference;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import service.tut.pori.contentanalysis.Definitions;
import service.tut.pori.contentanalysis.PhotoTaskResponse;
import service.tut.pori.twitterjazz.TwitterTaskResponse;
import core.tut.pori.http.annotations.HTTPMethodParameter;
import core.tut.pori.http.annotations.HTTPService;
import core.tut.pori.http.annotations.HTTPServiceMethod;
import core.tut.pori.http.parameters.InputStreamParameter;
import core.tut.pori.utils.XMLFormatter;

/**
 * 
 * Reference implementation for Server APIs. This class defines the APIs offered by the Front-end to Analysis Back-ends.
 *
 * <h1>Implementation Service path {@value service.tut.pori.twitterjazz.Definitions#SERVICE_TJ}</h1>
 * 
 * @see service.tut.pori.twitterjazz.TwitterJazzService
 */
@HTTPService(name = service.tut.pori.twitterjazz.reference.Definitions.SERVICE_TJ_REFERENCE_SERVER)
public class ServerService {
	private static final Logger LOGGER = Logger.getLogger(ServerService.class);
	private XMLFormatter _formatter = new XMLFormatter();
	
	/**
	 * The request is to be sent in the body of POST method. The Content-Type header MUST be set to "text/xml". The character set MUST be UTF-8. For example, "Content-Type: text/xml; charset=UTF-8".
	 * 
	 * The method can be called multiple times by the back-ends. Each call is assumed to be an incremental update on the previous one and can contain any amount of new information. 
	 * It is also possible to update previously submitted data by using the same identifiers: photo GUID (for photos), and media object id or a combination of back-end id and object id (for media objects).
	 * 
	 * Task results may contain photo specific back-end status information, though it is not required.
	 * 
	 * Using an invalid back-end id (both for the task and the generated media objects) can result in an error, causing the entire task result to be rejected. It is not allowed for a one back-end to update/modify media objects previously submitted by another back-end, or by the user.
	 * 
	 * <h2>Example Query:</h2>
	 *
	 * POST /rest/{@value service.tut.pori.twitterjazz.reference.Definitions#SERVICE_TJ_REFERENCE_SERVER}/{@value service.tut.pori.contentanalysis.Definitions#METHOD_TASK_FINISHED}<br>
	 * Content-Type: text/xml; charset=UTF-8<br><br>
	 *
	 * <b>[HTTP BODY STARTS]</b><br>
	 * 
	 * {@doc.restlet service="[service.tut.pori.twitterjazz.reference.Definitions#SERVICE_TJ_REFERENCE_EXAMPLE]" method="[service.tut.pori.contentanalysis.Definitions#ELEMENT_TASK_RESULTS]" type="GET" query="" body_uri=""} <br>
	 * 
	 * <b>[HTTP BODY ENDS]</b><br>
	 *
	 * <h2>Example Result:</h2>
	 * 
	 * {@doc.restlet service="[service.tut.pori.twitterjazz.reference.Definitions#SERVICE_TJ_REFERENCE_SERVER]" method="[service.tut.pori.contentanalysis.Definitions#METHOD_TASK_FINISHED]" type="POST" query="" body_uri="[service.tut.pori.twitterjazz.reference.Definitions#SERVICE_TJ_REFERENCE_EXAMPLE]/[service.tut.pori.contentanalysis.Definitions#ELEMENT_TASK_RESULTS]"}
	 * 
	 * @param xml The HTTP body. Only the result data should be in the body. See {@link service.tut.pori.twitterjazz.TwitterTaskResponse}
	 */
	@HTTPServiceMethod(name = Definitions.METHOD_TASK_FINISHED, acceptedMethods={core.tut.pori.http.Definitions.METHOD_POST})
	public void taskFinished(@HTTPMethodParameter(name = InputStreamParameter.PARAMETER_DEFAULT_NAME, bodyParameter = true) InputStreamParameter xml) {
		try {
			String body = IOUtils.toString(xml.getValue(), core.tut.pori.http.Definitions.CHARSET_UTF8); // read the body
			LOGGER.debug(body); // print to debug
			try(InputStream input = IOUtils.toInputStream(body, core.tut.pori.http.Definitions.CHARSET_UTF8)){ // convert back to stream for unmarshal
				TJReferenceCore.taskFinished(_formatter.toObject(input, TwitterTaskResponse.class, PhotoTaskResponse.class));
			}
		} catch (IOException ex) {
			LOGGER.error(ex, ex);
		}
	}
}
