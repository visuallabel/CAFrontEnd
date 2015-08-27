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
package service.tut.pori.contentanalysis.reference;

import service.tut.pori.contentanalysis.AsyncTask.TaskType;
import service.tut.pori.contentanalysis.Definitions;
import service.tut.pori.contentanalysis.DeletedPhotoList;
import service.tut.pori.contentanalysis.DissimilarPhotoList;
import service.tut.pori.contentanalysis.PhotoList;
import service.tut.pori.contentanalysis.ReferencePhotoList;
import service.tut.pori.contentanalysis.SimilarPhotoList;
import core.tut.pori.http.Response;
import core.tut.pori.http.annotations.HTTPMethodParameter;
import core.tut.pori.http.annotations.HTTPService;
import core.tut.pori.http.annotations.HTTPServiceMethod;
import core.tut.pori.http.parameters.DataGroups;
import core.tut.pori.http.parameters.Limits;
import core.tut.pori.http.parameters.StringParameter;

/**
 * Generates example XML output
 * 
 */
@HTTPService(name = service.tut.pori.contentanalysis.reference.Definitions.SERVICE_CA_REFERENCE_EXAMPLE)
public class ExampleService {
	/**
	 * Generates example feedback list
	 * 
	 * @param limits paging limits
	 * @return FeedbackList
	 * @see service.tut.pori.contentanalysis.PhotoFeedbackList
	 */
	@HTTPServiceMethod(name = Definitions.ELEMENT_FEEDBACKLIST)
	public Response feedbackList(
			@HTTPMethodParameter(name = Limits.PARAMETER_DEFAULT_NAME, required=false, defaultValue="0-0") Limits limits
			) 
	{
		Example example = new Example();
		example.setFeedbackList(CAReferenceCore.generateFeedbackList(limits));
		return new Response(example);
	}
	
	/**
	 * Generates example photo
	 * @param dataGroups For supported data groups, see {@link service.tut.pori.contentanalysis.reference.ClientService#retrieveMediaObjects(core.tut.pori.http.parameters.AuthenticationParameter, DataGroups, Limits, core.tut.pori.http.parameters.IntegerParameter, core.tut.pori.http.parameters.StringParameter)}.
	 * @return Photo
	 * @see service.tut.pori.contentanalysis.Photo
	 */
	@HTTPServiceMethod(name = Definitions.ELEMENT_PHOTO)
	public Response photo(
			@HTTPMethodParameter(name = DataGroups.PARAMETER_DEFAULT_NAME, required=false) DataGroups dataGroups) 
	{
		Example example = new Example();
		example.setPhoto(CAReferenceCore.generatePhoto(dataGroups));
		return new Response(example);
	}
	
	/**
	 * Generates example photo list
	 * @param dataGroups For supported data groups, see {@link service.tut.pori.contentanalysis.reference.ClientService#retrieveMediaObjects(core.tut.pori.http.parameters.AuthenticationParameter, DataGroups, Limits, core.tut.pori.http.parameters.IntegerParameter, core.tut.pori.http.parameters.StringParameter)}.
	 * @param limits paging limits
	 * @return PhotoList
	 * @see service.tut.pori.contentanalysis.PhotoList
	 */
	@HTTPServiceMethod(name = Definitions.ELEMENT_PHOTOLIST)
	public Response photoList(
			@HTTPMethodParameter(name = DataGroups.PARAMETER_DEFAULT_NAME, required=false) DataGroups dataGroups,
			@HTTPMethodParameter(name = Limits.PARAMETER_DEFAULT_NAME, required=false, defaultValue="0-0") Limits limits
			) 
	{
		Example example = new Example();
		example.setPhotoList(CAReferenceCore.generatePhotoList(dataGroups, limits, PhotoList.class));
		return new Response(example);
	}
	
	/**
	 * Generates example deleted photo list
	 * 
	 * @param limits paging limits
	 * @return DeletedPhotoList
	 * @see service.tut.pori.contentanalysis.DeletedPhotoList
	 */
	@HTTPServiceMethod(name = Definitions.ELEMENT_DELETED_PHOTOLIST)
	public Response deletedPhotoList(
			@HTTPMethodParameter(name = Limits.PARAMETER_DEFAULT_NAME, required=false, defaultValue="0-0") Limits limits
			) 
	{
		Example example = new Example();
		example.setPhotoList(CAReferenceCore.generatePhotoList(null, limits, DeletedPhotoList.class));
		return new Response(example);
	}
	
	/**
	 * Generates example dissimilar photo list
	 * 
	 * @param limits paging limits
	 * @return DissimilarPhotoList
	 * @see service.tut.pori.contentanalysis.DissimilarPhotoList
	 */
	@HTTPServiceMethod(name = Definitions.ELEMENT_DISSIMILAR_PHOTOLIST)
	public Response dissimilarPhotoList(
			@HTTPMethodParameter(name = Limits.PARAMETER_DEFAULT_NAME, required=false, defaultValue="0-0") Limits limits
			) 
	{
		Example example = new Example();
		example.setPhotoList(CAReferenceCore.generatePhotoList(null, limits, DissimilarPhotoList.class));
		return new Response(example);
	}
	
	/**
	 * Generates example similar photo list
	 * 
	 * @param limits paging limits
	 * @return SimilarPhotoList
	 * @see service.tut.pori.contentanalysis.SimilarPhotoList
	 */
	@HTTPServiceMethod(name = Definitions.ELEMENT_SIMILAR_PHOTOLIST)
	public Response similarPhotoList(
			@HTTPMethodParameter(name = Limits.PARAMETER_DEFAULT_NAME, required=false, defaultValue="0-0") Limits limits
			) 
	{
		Example example = new Example();
		example.setPhotoList(CAReferenceCore.generatePhotoList(null, limits, SimilarPhotoList.class));
		return new Response(example);
	}
	
	/**
	 * Generates example reference photo list
	 * 
	 * @param limits paging limits
	 * @return ReferencePhotoList
	 * @see service.tut.pori.contentanalysis.ReferencePhotoList
	 */
	@HTTPServiceMethod(name = Definitions.ELEMENT_REFERENCE_PHOTOLIST)
	public Response referencePhotoList(
			@HTTPMethodParameter(name = Limits.PARAMETER_DEFAULT_NAME, required=false, defaultValue="0-0") Limits limits
			) 
	{
		Example example = new Example();
		example.setPhotoList(CAReferenceCore.generatePhotoList(null, limits, ReferencePhotoList.class));
		return new Response(example);
	}
	
	/**
	 * Generates example media object list
	 * @param dataGroups For supported data groups, see {@link service.tut.pori.contentanalysis.reference.ClientService#retrieveMediaObjects(core.tut.pori.http.parameters.AuthenticationParameter, DataGroups, Limits, core.tut.pori.http.parameters.IntegerParameter, core.tut.pori.http.parameters.StringParameter)}.
	 * @param limits paging limits
	 * @return MediaObjectList
	 * @see service.tut.pori.contentanalysis.MediaObjectList
	 */
	@HTTPServiceMethod(name = Definitions.ELEMENT_MEDIA_OBJECTLIST)
	public Response mediaObjectList(
			@HTTPMethodParameter(name = DataGroups.PARAMETER_DEFAULT_NAME, required=false) DataGroups dataGroups,
			@HTTPMethodParameter(name = Limits.PARAMETER_DEFAULT_NAME, required=false, defaultValue="0-0") Limits limits
			) 
	{
		Example example = new Example();
		example.setMediaObjectList(CAReferenceCore.generateMediaObjectList(dataGroups, limits));
		return new Response(example);
	}
	
	/**
	 * Generates example media object
	 * @param dataGroups For supported data groups, see {@link service.tut.pori.contentanalysis.reference.ClientService#retrieveMediaObjects(core.tut.pori.http.parameters.AuthenticationParameter, DataGroups, Limits, core.tut.pori.http.parameters.IntegerParameter, core.tut.pori.http.parameters.StringParameter)}.
	 * @return MediaObject
	 * @see service.tut.pori.contentanalysis.MediaObject
	 */
	@HTTPServiceMethod(name = Definitions.ELEMENT_MEDIA_OBJECT)
	public Response mediaObject(
			@HTTPMethodParameter(name = DataGroups.PARAMETER_DEFAULT_NAME, required=false) DataGroups dataGroups) 
	{
		Example example = new Example();
		example.setMediaObject(CAReferenceCore.generateMediaObject(dataGroups));
		return new Response(example);
	}
	
	/**
	 * Generates example visual shape
	 * 
	 * @return VisualShape
	 * @see service.tut.pori.contentanalysis.VisualShape
	 */
	@HTTPServiceMethod(name = Definitions.ELEMENT_VISUAL_SHAPE)
	public Response visualShape() {
		Example example = new Example();
		example.setVisualShape(CAReferenceCore.generateVisualShape());
		return new Response(example);
	}
	
	/**
	 * Generates example task response
	 * 
	 * @param limits paging limits
	 * @return TaskResponse
	 * @see service.tut.pori.contentanalysis.PhotoTaskResponse
	 */
	@HTTPServiceMethod(name = Definitions.ELEMENT_TASK_RESULTS)
	public Response taskResults(
			@HTTPMethodParameter(name = Limits.PARAMETER_DEFAULT_NAME, required=false, defaultValue="0-0") Limits limits
			) 
	{
		Example example = new Example();
		example.setTaskResponse(CAReferenceCore.generateTaskResponse(limits));
		return new Response(example);
	}
	
	/**
	 * Generates example task details
	 * @param dataGroups For supported data groups, see {@link service.tut.pori.contentanalysis.reference.ClientService#retrieveMediaObjects(core.tut.pori.http.parameters.AuthenticationParameter, DataGroups, Limits, core.tut.pori.http.parameters.IntegerParameter, core.tut.pori.http.parameters.StringParameter)}.
	 * @param limits paging limits
	 * @param taskType Will default to {@link service.tut.pori.contentanalysis.AsyncTask.TaskType#ANALYSIS}, if missing
	 * @return PhotoTaskDetails
	 * @see service.tut.pori.contentanalysis.PhotoTaskDetails
	 */
	@HTTPServiceMethod(name = Definitions.ELEMENT_TASK_DETAILS)
	public Response taskDetails(
			@HTTPMethodParameter(name = DataGroups.PARAMETER_DEFAULT_NAME, required=false) DataGroups dataGroups,
			@HTTPMethodParameter(name = Limits.PARAMETER_DEFAULT_NAME, required=false, defaultValue="0-0") Limits limits,
			@HTTPMethodParameter(name = service.tut.pori.contentanalysis.reference.Definitions.PARAMETER_TASK_TYPE, required=false) StringParameter taskType
			) 
	{
		Example example = new Example();
		TaskType type = TaskType.ANALYSIS;
		if(taskType.hasValues()){
			type = TaskType.fromString(taskType.getValue());
		}
		example.setTaskDetails(CAReferenceCore.generatePhotoTaskDetails(dataGroups, limits, type));
		
		return new Response(example);
	}
	
	/**
	 * Generates example back-end status list list
	 * 
	 * @param limits paging limits
	 * @return BackendStatusList
	 * @see service.tut.pori.contentanalysis.BackendStatusList
	 */
	@HTTPServiceMethod(name = Definitions.ELEMENT_BACKEND_STATUS_LIST)
	public Response backendStatusList(
			@HTTPMethodParameter(name = Limits.PARAMETER_DEFAULT_NAME, required=false, defaultValue="0-0") Limits limits
			) 
	{
		Example example = new Example();
		example.setBackendStatusList(CAReferenceCore.generateBackendStatusList(limits));
		return new Response(example);
	}
	
	/**
	 * Generates example back-end status
	 * 
	 * @return BackendStatus
	 * @see service.tut.pori.contentanalysis.BackendStatus
	 */
	@HTTPServiceMethod(name = Definitions.ELEMENT_BACKEND_STATUS)
	public Response backendStatus() {
		Example example = new Example();
		example.setBackendStatus(CAReferenceCore.generateBackendStatus());
		return new Response(example);
	}
	
	/**
	 * Generates example AnalysisBackend
	 * 
	 * @return AnalysisBackend
	 * @see service.tut.pori.contentanalysis.AnalysisBackend
	 */
	@HTTPServiceMethod(name = Definitions.ELEMENT_BACKEND)
	public Response analysisBackend() {
		Example example = new Example();
		example.setAnalysisBackend(CAReferenceCore.generateAnalysisBackend());
		return new Response(example);
	}
	
	/**
	 * Generates example result info
	 * 
	 * @return BackendStatus
	 * @see service.tut.pori.contentanalysis.ResultInfo
	 */
	@HTTPServiceMethod(name = Definitions.ELEMENT_RESULT_INFO)
	public Response resultInfo() {
		Example example = new Example();
		example.setResultInfo(CAReferenceCore.generateResultInfo());
		return new Response(example);
	}
	
	/**
	 * Generates example analysis parameters
	 * @return AnalysisParameters
	 * @see service.tut.pori.contentanalysis.PhotoParameters
	 */
	@HTTPServiceMethod(name = Definitions.ELEMENT_TASK_PARAMETERS)
	public Response analysisParameters() {
		Example example = new Example();
		example.setAnalysisParameters(CAReferenceCore.generateAnalysisParameters());
		return new Response(example);
	}
}
