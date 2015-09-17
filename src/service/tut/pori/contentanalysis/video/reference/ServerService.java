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
package service.tut.pori.contentanalysis.video.reference;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import service.tut.pori.contentanalysis.Definitions;
import service.tut.pori.contentanalysis.video.VideoTaskResponse;
import core.tut.pori.http.Response;
import core.tut.pori.http.annotations.HTTPMethodParameter;
import core.tut.pori.http.annotations.HTTPService;
import core.tut.pori.http.annotations.HTTPServiceMethod;
import core.tut.pori.http.parameters.DataGroups;
import core.tut.pori.http.parameters.InputStreamParameter;
import core.tut.pori.http.parameters.IntegerParameter;
import core.tut.pori.http.parameters.Limits;
import core.tut.pori.http.parameters.LongParameter;
import core.tut.pori.utils.XMLFormatter;

/**
 * 
 * Reference implementation for Server APIs.
 * 
 * <h1>Implementation Service path {@value service.tut.pori.contentanalysis.video.Definitions#SERVICE_VCA}</h1>
 * 
 * @see service.tut.pori.contentanalysis.video.VideoAnalysisService
 *
 */
@HTTPService(name = service.tut.pori.contentanalysis.video.reference.Definitions.SERVICE_VCA_REFERENCE_SERVER)
public class ServerService {
	private static final Logger LOGGER = Logger.getLogger(ServerService.class);
	private XMLFormatter _formatter = new XMLFormatter();
	
	/**
	 * The request is to be sent in the body of POST method. The Content-Type header MUST be set to "text/xml". The character set MUST be UTF-8. For example, "Content-Type: text/xml; charset=UTF-8".
	 * 
	 * The method can be called multiple times by the back-ends. Each call is assumed to be an incremental update on the previous one and can contain any amount of new information. 
	 * It is also possible to update previously submitted data by using the same identifiers: video GUID (for videos), and media object id or a combination of back-end id and object id (for media objects).
	 * 
	 * Task results may contain video specific back-end status information, though it is not required.
	 * 
	 * Using an invalid back-end id (both for the task and the generated media objects) can result in an error, causing the entire task result to be rejected. It is not allowed for a one back-end to update/modify media objects previously submitted by another back-end, or by the user.
	 * 
	 * <h2>Example Query:</h2>
	 *
	 * POST /rest/{@value service.tut.pori.contentanalysis.video.reference.Definitions#SERVICE_VCA_REFERENCE_SERVER}/{@value service.tut.pori.contentanalysis.Definitions#METHOD_TASK_FINISHED}<br>
	 * Content-Type: text/xml; charset=UTF-8<br><br>
	 *
	 * <b>[HTTP BODY STARTS]</b><br>
	 * 
	 * {@doc.restlet service="[service.tut.pori.contentanalysis.video.reference.Definitions#SERVICE_VCA_REFERENCE_EXAMPLE]" method="[service.tut.pori.contentanalysis.Definitions#ELEMENT_TASK_RESULTS]" type="GET" query="" body_uri=""} <br>
	 * 
	 * <b>[HTTP BODY ENDS]</b><br>
	 *
	 * <h2>Example Result:</h2>
	 * 
	 * {@doc.restlet service="[service.tut.pori.contentanalysis.video.reference.Definitions#SERVICE_VCA_REFERENCE_SERVER]" method="[service.tut.pori.contentanalysis.Definitions#METHOD_TASK_FINISHED]" type="POST" query="" body_uri="[service.tut.pori.contentanalysis.video.reference.Definitions#SERVICE_VCA_REFERENCE_EXAMPLE]/[service.tut.pori.contentanalysis.Definitions#ELEMENT_TASK_RESULTS]"}
	 * 
	 * @param xml Only the result data should be in the body. See {@link service.tut.pori.contentanalysis.video.VideoTaskResponse}
	 */
	@HTTPServiceMethod(name = Definitions.METHOD_TASK_FINISHED, acceptedMethods={core.tut.pori.http.Definitions.METHOD_POST})
	public void taskFinished(@HTTPMethodParameter(name = InputStreamParameter.PARAMETER_DEFAULT_NAME, bodyParameter = true) InputStreamParameter xml) {
		try {
			String body = IOUtils.toString(xml.getValue()); // read the body
			LOGGER.debug(body); // print to debug
			try(InputStream input = IOUtils.toInputStream(body)){ // convert back to stream for unmarshal
				VideoReferenceCore.taskFinished(_formatter.toObject(input, VideoTaskResponse.class));
			}
		} catch (IOException ex) {
			LOGGER.error(ex, ex);
		}
	}
	
	/**
	 * This method can be used to retrieve the up-to-date details and progress of a previously scheduled task.
	 * 
	 * <h2>Example Query:</h2>
	 *
	 * GET /rest/{@value service.tut.pori.contentanalysis.video.reference.Definitions#SERVICE_VCA_REFERENCE_SERVER}/{@value service.tut.pori.contentanalysis.Definitions#METHOD_QUERY_TASK_DETAILS}?{@value service.tut.pori.contentanalysis.Definitions#PARAMETER_TASK_ID}=1&amp;{@value service.tut.pori.contentanalysis.Definitions#PARAMETER_BACKEND_ID}=1<br>
	 *
	 * <h2>Example Result:</h2>
	 * 
	 * {@doc.restlet service="[service.tut.pori.contentanalysis.video.reference.Definitions#SERVICE_VCA_REFERENCE_SERVER]" method="[service.tut.pori.contentanalysis.Definitions#METHOD_QUERY_TASK_DETAILS]" type="GET" query="[service.tut.pori.contentanalysis.Definitions#PARAMETER_TASK_ID]=1&[service.tut.pori.contentanalysis.Definitions#PARAMETER_BACKEND_ID]=1" body_uri=""}
	 * 
	 * @param taskId
	 * @param backendId
	 * @param dataGroups
	 * @param limits
	 * @return response
	 */
	@HTTPServiceMethod(name = Definitions.METHOD_QUERY_TASK_DETAILS, acceptedMethods={core.tut.pori.http.Definitions.METHOD_GET})
	public Response queryTaskDetails(
			@HTTPMethodParameter(name = Definitions.PARAMETER_BACKEND_ID) IntegerParameter backendId,
			@HTTPMethodParameter(name = Definitions.PARAMETER_TASK_ID) LongParameter taskId,
			@HTTPMethodParameter(name = DataGroups.PARAMETER_DEFAULT_NAME, required = false) DataGroups dataGroups,
			@HTTPMethodParameter(name = Limits.PARAMETER_DEFAULT_NAME, required = false) Limits limits
			) 
	{
		return VideoReferenceCore.queryTaskDetails(backendId.getValue(), taskId.getValue(), dataGroups, limits);
	}
}
