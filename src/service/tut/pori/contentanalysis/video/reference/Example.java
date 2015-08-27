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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

import service.tut.pori.contentanalysis.video.Timecode;
import service.tut.pori.contentanalysis.video.TimecodeList;
import service.tut.pori.contentanalysis.video.Video;
import service.tut.pori.contentanalysis.video.VideoList;
import service.tut.pori.contentanalysis.video.VideoParameters;
import service.tut.pori.contentanalysis.video.VideoTaskDetails;
import service.tut.pori.contentanalysis.video.VideoTaskResponse;
import core.tut.pori.http.ResponseData;

/**
 * Response example
 * 
 */
@XmlRootElement(name=service.tut.pori.contentanalysis.video.reference.Definitions.ELEMENT_EXAMPLE)
@XmlAccessorType(XmlAccessType.NONE)
public class Example extends ResponseData {
	@XmlElementRef
	private VideoTaskDetails _taskDetails = null;
	@XmlElementRef
	private VideoTaskResponse _taskResponse = null;
	@XmlElementRef
	private Timecode _timecode = null;
	@XmlElementRef
	private TimecodeList _timecodeList = null;
	@XmlElementRef
	private Video _video = null;
	@XmlElementRef
	private VideoList _videoList = null;
	@XmlElementRef
	private VideoParameters _videoOptions = null;

	/**
	 * @return the video
	 */
	public Video getVideo() {
		return _video;
	}

	/**
	 * @param video the video to set
	 */
	public void setVideo(Video video) {
		_video = video;
	}

	/**
	 * @return the videoList
	 */
	public VideoList getVideoList() {
		return _videoList;
	}

	/**
	 * @param videoList the videoList to set
	 */
	public void setVideoList(VideoList videoList) {
		_videoList = videoList;
	}

	/**
	 * @return the taskDetails
	 */
	public VideoTaskDetails getTaskDetails() {
		return _taskDetails;
	}

	/**
	 * @param taskDetails the taskDetails to set
	 */
	public void setTaskDetails(VideoTaskDetails taskDetails) {
		_taskDetails = taskDetails;
	}

	/**
	 * @return the timecode
	 */
	public Timecode getTimecode() {
		return _timecode;
	}

	/**
	 * @param timecode the timecode to set
	 */
	public void setTimecode(Timecode timecode) {
		_timecode = timecode;
	}

	/**
	 * @return the timecodeList
	 */
	public TimecodeList getTimecodeList() {
		return _timecodeList;
	}

	/**
	 * @param timecodeList the timecodeList to set
	 */
	public void setTimecodeList(TimecodeList timecodeList) {
		_timecodeList = timecodeList;
	}

	/**
	 * @return the videoOptions
	 */
	public VideoParameters getVideoOptions() {
		return _videoOptions;
	}

	/**
	 * @param videoOptions the videoOptions to set
	 */
	public void setVideoOptions(VideoParameters videoOptions) {
		_videoOptions = videoOptions;
	}

	/**
	 * @return the taskResponse
	 */
	public VideoTaskResponse getTaskResponse() {
		return _taskResponse;
	}

	/**
	 * @param taskResponse the taskResponse to set
	 */
	public void setTaskResponse(VideoTaskResponse taskResponse) {
		_taskResponse = taskResponse;
	}

	@Override
	public Class<?>[] getDataClasses() {
		List<Class<?>> classes = new ArrayList<>();
		classes.add(getClass());
		if(_video != null){
			classes.add(_video.getClass());
		}
		if(_videoList != null){
			classes.add(_videoList.getClass());
		}
		if(_taskDetails != null){
			classes.addAll(Arrays.asList(_taskDetails.getDataClasses()));
		}
		if(_timecode != null){
			classes.add(_timecode.getClass());
		}
		if(_timecodeList != null){
			classes.add(_timecodeList.getClass());
		}
		if(_videoOptions != null){
			classes.add(_videoOptions.getClass());
		}
		if(_taskResponse != null){
			classes.addAll(Arrays.asList(_taskResponse.getDataClasses()));
		}
		return classes.toArray(new Class<?>[classes.size()]);
	}
}
