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

import service.tut.pori.contentanalysis.AsyncTask.TaskType;
import service.tut.pori.contentanalysis.Definitions;
import service.tut.pori.contentanalysis.video.DeletedVideoList;
import service.tut.pori.contentanalysis.video.VideoList;
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
@HTTPService(name = service.tut.pori.contentanalysis.video.reference.Definitions.SERVICE_VCA_REFERENCE_EXAMPLE)
public class ExampleService {
	/**
	 * Generates example video
	 * @param dataGroups For supported data groups, see {@link service.tut.pori.contentanalysis.reference.ClientService#retrieveMediaObjects(core.tut.pori.http.parameters.AuthenticationParameter, DataGroups, Limits, core.tut.pori.http.parameters.IntegerParameter, StringParameter)}.
	 * @return Video
	 * @see service.tut.pori.contentanalysis.video.Video
	 */
	@HTTPServiceMethod(name = service.tut.pori.contentanalysis.video.Definitions.ELEMENT_VIDEO)
	public Response video(
			@HTTPMethodParameter(name = DataGroups.PARAMETER_DEFAULT_NAME, required=false) DataGroups dataGroups) 
	{
		Example example = new Example();
		example.setVideo(VideoReferenceCore.generateVideo(dataGroups));
		return new Response(example);
	}
	
	/**
	 * Generates example video list
	 * @param dataGroups For supported data groups, see {@link service.tut.pori.contentanalysis.reference.ClientService#retrieveMediaObjects(core.tut.pori.http.parameters.AuthenticationParameter, DataGroups, Limits, core.tut.pori.http.parameters.IntegerParameter, core.tut.pori.http.parameters.StringParameter)}.
	 * @param limits paging limits
	 * @return VideoList
	 * @see service.tut.pori.contentanalysis.video.VideoList
	 */
	@HTTPServiceMethod(name = service.tut.pori.contentanalysis.video.Definitions.ELEMENT_VIDEOLIST)
	public Response videoList(
			@HTTPMethodParameter(name = DataGroups.PARAMETER_DEFAULT_NAME, required=false) DataGroups dataGroups,
			@HTTPMethodParameter(name = Limits.PARAMETER_DEFAULT_NAME, required=false, defaultValue="0-0") Limits limits
			) 
	{
		Example example = new Example();
		example.setVideoList(VideoReferenceCore.generateVideoList(dataGroups, limits, VideoList.class));
		return new Response(example);
	}
	
	/**
	 * Generates example time code
	 * @return Timecode
	 * @see service.tut.pori.contentanalysis.video.Timecode
	 */
	@HTTPServiceMethod(name = service.tut.pori.contentanalysis.video.Definitions.ELEMENT_TIMECODE)
	public Response timecode() {
		Example example = new Example();
		example.setTimecode(VideoReferenceCore.generateTimecode());
		return new Response(example);
	}
	
	/**
	 * Generates example time code list
	 * @param limits paing limits
	 * @return TimecodeList
	 * @see service.tut.pori.contentanalysis.video.TimecodeList
	 */
	@HTTPServiceMethod(name = service.tut.pori.contentanalysis.video.Definitions.ELEMENT_TIMECODELIST)
	public Response timecodeList(
			@HTTPMethodParameter(name = Limits.PARAMETER_DEFAULT_NAME, required=false, defaultValue="0-0") Limits limits)
	{
		Example example = new Example();
		example.setTimecodeList(VideoReferenceCore.generateTimecodeList(limits));
		return new Response(example);
	}
	
	/**
	 * Generates example video options
	 * @return VideoOptions
	 * @see service.tut.pori.contentanalysis.video.VideoParameters
	 */
	@HTTPServiceMethod(name = Definitions.ELEMENT_TASK_PARAMETERS)
	public Response analysisParameters() {
		Example example = new Example();
		example.setVideoOptions(VideoReferenceCore.generateVideoOptions());
		return new Response(example);
	}
	
	/**
	 * Generates example deleted video list
	 * @param limits paging limits
	 * @return DeletedVideoList
	 * @see service.tut.pori.contentanalysis.video.DeletedVideoList
	 */
	@HTTPServiceMethod(name = service.tut.pori.contentanalysis.video.Definitions.ELEMENT_DELETED_VIDEOLIST)
	public Response deletedVideoList(
			@HTTPMethodParameter(name = Limits.PARAMETER_DEFAULT_NAME, required=false, defaultValue="0-0") Limits limits
			) 
	{
		Example example = new Example();
		example.setVideoList(VideoReferenceCore.generateVideoList(null, limits, DeletedVideoList.class));
		return new Response(example);
	}
	
	/**
	 * Generates example task response
	 * 
	 * @param limits paging limits
	 * @return VideoTaskResponse
	 * @see service.tut.pori.contentanalysis.video.VideoTaskResponse
	 */
	@HTTPServiceMethod(name = Definitions.ELEMENT_TASK_RESULTS)
	public Response taskResults(
			@HTTPMethodParameter(name = Limits.PARAMETER_DEFAULT_NAME, required=false, defaultValue="0-0") Limits limits
			) 
	{
		Example example = new Example();
		example.setTaskResponse(VideoReferenceCore.generateTaskResponse(limits));
		return new Response(example);
	}
	
	/**
	 * Generates example task details
	 * @param dataGroups For supported data groups, see {@link service.tut.pori.contentanalysis.reference.ClientService#retrieveMediaObjects(core.tut.pori.http.parameters.AuthenticationParameter, DataGroups, Limits, core.tut.pori.http.parameters.IntegerParameter, core.tut.pori.http.parameters.StringParameter)}.
	 * @param limits paging limits
	 * @param taskType Will default to {@link service.tut.pori.contentanalysis.AsyncTask.TaskType#ANALYSIS}, if missing
	 * @return VideoTaskDetails
	 * @see service.tut.pori.contentanalysis.video.VideoTaskDetails
	 */
	@HTTPServiceMethod(name = Definitions.ELEMENT_TASK_DETAILS)
	public Response taskDetails(
			@HTTPMethodParameter(name = DataGroups.PARAMETER_DEFAULT_NAME, required=false) DataGroups dataGroups,
			@HTTPMethodParameter(name = Limits.PARAMETER_DEFAULT_NAME, required=false, defaultValue="0-0") Limits limits,
			@HTTPMethodParameter(name = service.tut.pori.contentanalysis.video.reference.Definitions.PARAMETER_TASK_TYPE, required=false) StringParameter taskType
			) 
	{
		Example example = new Example();
		TaskType type = TaskType.ANALYSIS;
		if(taskType.hasValues()){
			type = TaskType.fromString(taskType.getValue());
		}
		example.setTaskDetails(VideoReferenceCore.generateVideoTaskDetails(dataGroups, limits, type));
		
		return new Response(example);
	}
}
