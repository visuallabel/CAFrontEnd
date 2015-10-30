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
package service.tut.pori.contentanalysis.video.reference;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import service.tut.pori.contentanalysis.PhotoParameters.AnalysisType;
import service.tut.pori.contentanalysis.CAContentCore.ServiceType;
import service.tut.pori.contentanalysis.Definitions;
import service.tut.pori.contentanalysis.video.VideoParameters;
import service.tut.pori.contentanalysis.video.VideoTaskDetails;
import core.tut.pori.http.Response;
import core.tut.pori.http.annotations.HTTPMethodParameter;
import core.tut.pori.http.annotations.HTTPService;
import core.tut.pori.http.annotations.HTTPServiceMethod;
import core.tut.pori.http.parameters.DataGroups;
import core.tut.pori.http.parameters.InputStreamParameter;
import core.tut.pori.http.parameters.IntegerParameter;
import core.tut.pori.http.parameters.Limits;
import core.tut.pori.http.parameters.LongParameter;
import core.tut.pori.http.parameters.StringParameter;
import core.tut.pori.utils.XMLFormatter;

/**
 * 
 * Reference implementation for Back-end APIs
 *
 */
@HTTPService(name = service.tut.pori.contentanalysis.video.reference.Definitions.SERVICE_VCA_REFERENCE_BACKEND)
public class BackendService {
	private static final Logger LOGGER = Logger.getLogger(BackendService.class);
	private XMLFormatter _formatter = new XMLFormatter();
	
	/**
	 * The request is to be sent in the body of POST method. The Content-Type header MUST be set to "text/xml". The character set MUST be UTF-8. For example, "Content-Type: text/xml; charset=UTF-8".
	 * 
	 * <h2>Example Query:</h2>
	 *
	 * POST /rest/{@value service.tut.pori.contentanalysis.video.reference.Definitions#SERVICE_VCA_REFERENCE_BACKEND}/{@value service.tut.pori.contentanalysis.Definitions#METHOD_ADD_TASK}<br>
	 * Content-Type: text/xml; charset=UTF-8<br><br>
	 *
	 * <b>[HTTP BODY STARTS]</b><br>
	 * 
	 * {@doc.restlet service="[service.tut.pori.contentanalysis.video.reference.Definitions#SERVICE_VCA_REFERENCE_EXAMPLE]" method="[service.tut.pori.contentanalysis.Definitions#ELEMENT_TASK_DETAILS]" type="GET" query="" body_uri=""} <br>
	 * 
	 * <b>[HTTP BODY ENDS]</b><br>
	 *
	 * <h2>Example Result:</h2>
	 * 
	 * {@doc.restlet service="[service.tut.pori.contentanalysis.video.reference.Definitions#SERVICE_VCA_REFERENCE_BACKEND]" method="[service.tut.pori.contentanalysis.Definitions#METHOD_ADD_TASK]" type="POST" query="" body_uri="[service.tut.pori.contentanalysis.video.reference.Definitions#SERVICE_VCA_REFERENCE_EXAMPLE]/[service.tut.pori.contentanalysis.Definitions#ELEMENT_TASK_DETAILS]"}
	 * 
	 * @param xml Only the workload data should be in the body. See {@link service.tut.pori.contentanalysis.video.VideoTaskDetails}
	 */
	@HTTPServiceMethod(name = service.tut.pori.contentanalysis.Definitions.METHOD_ADD_TASK, acceptedMethods={core.tut.pori.http.Definitions.METHOD_POST})
	public void addTask(@HTTPMethodParameter(name = InputStreamParameter.PARAMETER_DEFAULT_NAME, bodyParameter = true) InputStreamParameter xml) {
		try {
			String body = IOUtils.toString(xml.getValue()); // read the body
			LOGGER.debug(body); // print to debug
			try(InputStream input = IOUtils.toInputStream(body)){ // convert back to stream for unmarshal
				VideoReferenceCore.addTask(_formatter.toObject(input, VideoTaskDetails.class, VideoParameters.class));
			}
		} catch (IOException ex) {
			LOGGER.error(ex, ex);
		}
	}
	
	/**
	 * This method can be used to query the current status of an analysis task from the back-end.
	 * 
	 * 
	 * <h2>Example Query:</h2>
	 *
	 * GET /rest/{@value service.tut.pori.contentanalysis.video.reference.Definitions#SERVICE_VCA_REFERENCE_BACKEND}/{@value service.tut.pori.contentanalysis.Definitions#METHOD_QUERY_TASK_STATUS}?{@value service.tut.pori.contentanalysis.Definitions#PARAMETER_TASK_ID}=1<br>
	 *
	 * <h2>Example Result:</h2>
	 * 
	 * {@doc.restlet service="[service.tut.pori.contentanalysis.video.reference.Definitions#SERVICE_VCA_REFERENCE_BACKEND]" method="[service.tut.pori.contentanalysis.Definitions#METHOD_QUERY_TASK_STATUS]" type="GET" query="[service.tut.pori.contentanalysis.Definitions#PARAMETER_TASK_ID]=1" body_uri=""}
	 * 
	 * @param taskId
	 * @param dataGroups For supported data groups, see {@link service.tut.pori.contentanalysis.reference.ClientService#retrieveMediaObjects(core.tut.pori.http.parameters.AuthenticationParameter, DataGroups, Limits, core.tut.pori.http.parameters.IntegerParameter, core.tut.pori.http.parameters.StringParameter)}.
	 * @param limits paging limits
	 * @return See {@link service.tut.pori.contentanalysis.PhotoTaskResponse}
	 */
	@HTTPServiceMethod(name = service.tut.pori.contentanalysis.Definitions.METHOD_QUERY_TASK_STATUS, acceptedMethods={core.tut.pori.http.Definitions.METHOD_GET})
	public Response queryTaskStatus(
			@HTTPMethodParameter(name = service.tut.pori.contentanalysis.Definitions.PARAMETER_TASK_ID) LongParameter taskId,
			@HTTPMethodParameter(name = DataGroups.PARAMETER_DEFAULT_NAME, required = false) DataGroups dataGroups,
			@HTTPMethodParameter(name = Limits.PARAMETER_DEFAULT_NAME, required = false) Limits limits
			)
	{
		return VideoReferenceCore.queryTaskStatus(taskId.getValue(), dataGroups, limits);
	}
	
	/**
	 * This method is applicable for analysis back-ends that store the GUID and user information.
	 * 
	 * <h2>Example Query:</h2>
	 *
	 * GET /rest/{@value service.tut.pori.contentanalysis.video.reference.Definitions#SERVICE_VCA_REFERENCE_BACKEND}/{@value service.tut.pori.contentanalysis.video.Definitions#METHOD_SEARCH_SIMILAR_BY_ID}?{@value service.tut.pori.contentanalysis.Definitions#PARAMETER_GUID}=1<br>
	 *
	 * <h2>Example Result:</h2>
	 * 
	 * {@doc.restlet service="[service.tut.pori.contentanalysis.video.reference.Definitions#SERVICE_VCA_REFERENCE_BACKEND]" method="[service.tut.pori.contentanalysis.video.Definitions#METHOD_SEARCH_SIMILAR_BY_ID]" type="GET" query="[service.tut.pori.contentanalysis.Definitions#PARAMETER_GUID]=1" body_uri=""}
	 * 
	 * @param guid video GUID
	 * @param analysisType optional list of analysis types to use for the search operation {@link service.tut.pori.contentanalysis.PhotoParameters.AnalysisType}
	 * @param dataGroups For supported data groups, see {@link service.tut.pori.contentanalysis.reference.ClientService#retrieveMediaObjects(core.tut.pori.http.parameters.AuthenticationParameter, DataGroups, Limits, core.tut.pori.http.parameters.IntegerParameter, core.tut.pori.http.parameters.StringParameter)}.
	 * @param limits paging limits
	 * @param serviceIds serviceIds If given, search is targeted only to the services with the listed ids. For supported service types, see {@link service.tut.pori.contentanalysis.CAContentCore.ServiceType}.
	 * @param userIdFilters If parameter is missing, search should be targeted only to publicly available photos.
	 * @return See {@link service.tut.pori.contentanalysis.PhotoList}
	 */
	@HTTPServiceMethod(name = service.tut.pori.contentanalysis.video.Definitions.METHOD_SEARCH_SIMILAR_BY_ID, acceptedMethods={core.tut.pori.http.Definitions.METHOD_GET})
	public Response similarVideosById(
			@HTTPMethodParameter(name = Definitions.PARAMETER_GUID) StringParameter guid,
			@HTTPMethodParameter(name = service.tut.pori.contentanalysis.Definitions.PARAMETER_ANALYSIS_TYPE, required = false) StringParameter analysisType, 
			@HTTPMethodParameter(name = DataGroups.PARAMETER_DEFAULT_NAME, required = false) DataGroups dataGroups,
			@HTTPMethodParameter(name = Limits.PARAMETER_DEFAULT_NAME, required = false) Limits limits,
			@HTTPMethodParameter(name = Definitions.PARAMETER_SERVICE_ID, required = false) IntegerParameter serviceIds,
			@HTTPMethodParameter(name = service.tut.pori.users.Definitions.PARAMETER_USER_ID, required = false) LongParameter userIdFilters
			)
	{
		return VideoReferenceCore.searchSimilarById(AnalysisType.fromAnalysisTypeString(analysisType.getValues()), guid.getValue(), dataGroups, limits, ServiceType.fromIdArray(serviceIds.getValues()), userIdFilters.getValues());
	}
}
