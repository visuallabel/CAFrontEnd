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
package service.tut.pori.contentanalysis.video;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import service.tut.pori.contentanalysis.TaskResponse;

/**
 * Class for representing a response received from a back-end to a previously submitted analysis task.
 * 
 * <h3>XML Example</h3>
 * 
 * {@doc.restlet service="[service.tut.pori.contentanalysis.video.reference.Definitions#SERVICE_VCA_REFERENCE_EXAMPLE]" method="[service.tut.pori.contentanalysis.Definitions#ELEMENT_TASK_RESULTS]" type="GET" query="" body_uri=""}
 * 
 * @see service.tut.pori.contentanalysis.video.VideoList
 */
@XmlRootElement(name=service.tut.pori.contentanalysis.Definitions.ELEMENT_TASK_RESULTS)
@XmlAccessorType(XmlAccessType.NONE)
public class VideoTaskResponse extends TaskResponse {
	@XmlElement(name = Definitions.ELEMENT_VIDEOLIST)
	private VideoList _videoList = null;

	/**
	 * @return the videoList
	 * @see #setVideoList(VideoList)
	 */
	public VideoList getVideoList() {
		return _videoList;
	}

	/**
	 * @param videoList the videoList to set
	 * @see #getVideoList()
	 */
	public void setVideoList(VideoList videoList) {
		_videoList = videoList;
	}
}
