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
package service.tut.pori.fuzzyvisuals;

import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

import service.tut.pori.contentanalysis.AbstractTaskDetails;

/**
 * Minimal implementation of Task Details for Fuzzy Visuals
 *
 */
@XmlRootElement(name=service.tut.pori.contentanalysis.Definitions.ELEMENT_TASK_DETAILS)
@XmlAccessorType(XmlAccessType.NONE)
@XmlSeeAlso(TaskDetails.FuzzyTaskParameters.class)
public class TaskDetails extends AbstractTaskDetails {
	@XmlElement(name = Definitions.ELEMENT_MEDIA)
	@XmlElementWrapper(name = Definitions.ELEMENT_MEDIA_LIST)
	private List<FuzzyMedia> _media = null;

	@Override
	public TaskParameters getTaskParameters() {
		return null; // don't care about the parameters
	}

	@Override
	public void setTaskParameters(TaskParameters parameters) {
		// don't care about the parameters
	}
	
	/**
	 * 
	 * @param parameters
	 * @see #setTaskParameters(TaskParameters)
	 */
	public void setTaskParameters(FuzzyTaskParameters parameters) {
		// don't care about the parameters
	}

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
	 * minimal implementation required for serialization
	 *
	 */
	@XmlRootElement(name=service.tut.pori.contentanalysis.Definitions.ELEMENT_TASK_PARAMETERS)
	@XmlAccessorType(value=XmlAccessType.NONE)
	public static class FuzzyTaskParameters extends TaskParameters{

		@Override
		public void initialize(Map<String, String> metadata) throws IllegalArgumentException {
			// nothing needed
		}

		@Override
		public Map<String, String> toMetadata() {
			return null;
		}
		
	} // class FuzzyTaskParameters
}
