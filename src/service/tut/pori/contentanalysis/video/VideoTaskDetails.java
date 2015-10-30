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
package service.tut.pori.contentanalysis.video;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import service.tut.pori.contentanalysis.AbstractTaskDetails;
import service.tut.pori.contentanalysis.AsyncTask.TaskType;

/**
 * An implementation of AbstractTaskDetails, which can be used to define a video analysis task, or a feedback task.
 * 
 * <h2>Conditional Elements</h2>
 * <ul>
 *  <li>{@value service.tut.pori.contentanalysis.video.Definitions#ELEMENT_DELETED_VIDEOLIST}</li>
 *  <li>{@value service.tut.pori.contentanalysis.video.Definitions#ELEMENT_VIDEOLIST}</li>
 * </ul>
 * 
 * One of {@value service.tut.pori.contentanalysis.video.Definitions#ELEMENT_VIDEOLIST} or {@value service.tut.pori.contentanalysis.video.Definitions#ELEMENT_DELETED_VIDEOLIST} must be present in a task.
 * 
 * If no analysis options have been given, the default options should be used : {@link service.tut.pori.contentanalysis.video.VideoParameters.SequenceType#FULL}.
 * 
 * <h3>XML Example - Analysis Task</h3>
 * 
 * {@doc.restlet service="[service.tut.pori.contentanalysis.video.reference.Definitions#SERVICE_VCA_REFERENCE_EXAMPLE]" method="[service.tut.pori.contentanalysis.Definitions#ELEMENT_TASK_DETAILS]" type="GET" query="[service.tut.pori.contentanalysis.video.reference.Definitions#PARAMETER_TASK_TYPE]=[service.tut.pori.contentanalysis.AsyncTask$TaskType#ANALYSIS]" body_uri=""}
 * 
 * <h3>XML Example - Feedback Task</h3>
 * 
 * {@doc.restlet service="[service.tut.pori.contentanalysis.video.reference.Definitions#SERVICE_VCA_REFERENCE_EXAMPLE]" method="[service.tut.pori.contentanalysis.Definitions#ELEMENT_TASK_DETAILS]" type="GET" query="[service.tut.pori.contentanalysis.video.reference.Definitions#PARAMETER_TASK_TYPE]=[service.tut.pori.contentanalysis.AsyncTask$TaskType#FEEDBACK]" body_uri=""}
 *  
 */
@XmlRootElement(name=service.tut.pori.contentanalysis.Definitions.ELEMENT_TASK_DETAILS)
@XmlAccessorType(XmlAccessType.NONE)
public final class VideoTaskDetails extends AbstractTaskDetails {
	@XmlElement(name = Definitions.ELEMENT_DELETED_VIDEOLIST)
	private DeletedVideoList _deletedVideoList = null;
	@XmlElement(name = Definitions.ELEMENT_VIDEOLIST)
	private VideoList _videoList = null;
	private VideoParameters _videoParameters = null;
	
	/**
	 * 
	 * @param taskType
	 */
	public VideoTaskDetails(TaskType taskType) {
		setTaskType(taskType);
	}
	
	/**
	 * 
	 */
	public VideoTaskDetails() {
		super();
	}

	/**
	 * @return the deletedVideoList
	 * @see #setDeletedVideoList(DeletedVideoList)
	 */
	public DeletedVideoList getDeletedVideoList() {
		return _deletedVideoList;
	}
	
	/**
	 * @param deletedVideoList the deletedVideoList to set
	 * @see #getDeletedVideoList()
	 */
	public void setDeletedVideoList(DeletedVideoList deletedVideoList) {
		_deletedVideoList = deletedVideoList;
	}
	
	/**
	 * @return the videoList
	 * @see #setVideoList(VideoList)
	 */
	public VideoList getVideoList() {
		return _videoList;
	}
	
	/**
	 * @param videoList the videoList to set
	 * @see #getDeletedVideoList()
	 */
	public void setVideoList(VideoList videoList) {
		_videoList = videoList;
	}

	/**
	 * Add video to the video list
	 * 
	 * @param video
	 * @see #getVideoList()
	 */
	public void addVideo(Video video) {
		if(_videoList == null){
			_videoList = new VideoList();
		}
		_videoList.addVideo(video);
	}

	/**
	 * add video to the deleted video list
	 * 
	 * @param video
	 * @see #getDeletedVideoList()
	 */
	public void addDeletedVideo(Video video) {
		if(_deletedVideoList == null){
			_deletedVideoList = new DeletedVideoList();
		}
		_deletedVideoList.addVideo(video);
	}

	/**
	 * 
	 * @return true if the task has no content
	 */
	public boolean isEmpty() {
		return (VideoList.isEmpty(_videoList) && DeletedVideoList.isEmpty(_deletedVideoList));
	}

	@Override
	public VideoParameters getTaskParameters() {
		return _videoParameters;
	}

	@Override
	public void setTaskParameters(TaskParameters parameters) {
		if(parameters == null){
			_videoParameters = null;
		}else if(parameters instanceof VideoParameters){
			setTaskParameters((VideoParameters) parameters);
		}else{
			_videoParameters = new VideoParameters();
		}
	}
	
	/**
	 * 
	 * @param parameters
	 */
	public void setTaskParameters(VideoParameters parameters) {
		_videoParameters = parameters;
	}
}
