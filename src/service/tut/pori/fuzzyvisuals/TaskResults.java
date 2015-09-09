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
package service.tut.pori.fuzzyvisuals;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import service.tut.pori.contentanalysis.AsyncTask.TaskStatus;
import service.tut.pori.contentanalysis.AsyncTask.TaskType;
import service.tut.pori.contentanalysis.TaskResponse;

/**
 * Minimal implementation of TaskResults for Fuzzy Visuals
 *
 */
@XmlRootElement(name=service.tut.pori.contentanalysis.Definitions.ELEMENT_TASK_RESULTS)
@XmlAccessorType(XmlAccessType.NONE)
public class TaskResults extends TaskResponse {
	@XmlElement(name = Definitions.ELEMENT_MEDIA)
	@XmlElementWrapper(name = Definitions.ELEMENT_MEDIA_LIST)
	private List<FuzzyMedia> _media = null;

	/**
	 * @return the media
	 * @see #setMedia(List)
	 */
	public List<FuzzyMedia> getMedia() {
		return _media;
	}

	/**
	 * @param media the media to set
	 * @see #getMedia()
	 */
	public void setMedia(List<FuzzyMedia> media) {
		_media = media;
	}
	
	/**
	 * for serialization
	 */
	public TaskResults(){
		// nothing needed
	}
	
	/**
	 * 
	 * @param backendId
	 * @param taskId 
	 * @param taskStatus
	 * @param taskType
	 */
	public TaskResults(Integer backendId, Long taskId, TaskStatus taskStatus, TaskType taskType){
		setBackendId(backendId);
		setTaskId(taskId);
		setStatus(taskStatus);
		setTaskType(taskType);
	}
}
