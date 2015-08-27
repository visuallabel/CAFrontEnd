/**
 * Copyright 2014 Tampere University of Technology, Pori Unit
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
package service.tut.pori.facebookjazz;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.StringUtils;

import service.tut.pori.contentanalysis.AbstractTaskDetails;
import service.tut.pori.contentanalysis.AsyncTask.TaskType;
import service.tut.pori.contentanalysis.Definitions;
import service.tut.pori.contentanalysis.MediaObjectList;

/**
 * Task details for a Facebook feedback task.
 * 
 * <h3>XML Example</h3>
 * 
 * {@doc.restlet service="[service.tut.pori.facebookjazz.reference.Definitions#SERVICE_FBJ_REFERENCE_EXAMPLE]" method="[service.tut.pori.contentanalysis.Definitions#ELEMENT_TASK_DETAILS]" type="GET" query="[service.tut.pori.facebookjazz.reference.Definitions#PARAMETER_TASK_TYPE]=[service.tut.pori.contentanalysis.AsyncTask$TaskType#FACEBOOK_PROFILE_SUMMARIZATION_FEEDBACK]" body_uri=""} 
 * 
 * @see service.tut.pori.contentanalysis.MediaObjectList
 */
@XmlRootElement(name=Definitions.ELEMENT_TASK_DETAILS)
@XmlAccessorType(XmlAccessType.NONE)
public final class FBFeedbackTaskDetails extends AbstractTaskDetails {
	@XmlElement(name=Definitions.ELEMENT_MEDIA_OBJECTLIST)
	private MediaObjectList _tags = null;

	/**
	 * 
	 */
	public FBFeedbackTaskDetails(){
		setTaskType(TaskType.FACEBOOK_PROFILE_SUMMARIZATION_FEEDBACK);
	}

	/**
	 * @return the tags
	 * @see #setTags(MediaObjectList)
	 */
	public MediaObjectList getTags() {
		return _tags;
	}

	/**
	 * @param tags the tags to set
	 * @see #getTags()
	 */
	public void setTags(MediaObjectList tags) {
		_tags = tags;
	}
	
	@XmlElement(name = service.tut.pori.contentanalysis.Definitions.ELEMENT_CALLBACK_URI)
	@Override
	public String getCallbackUri() {
		String callbackUri = super.getCallbackUri();
		return (StringUtils.isBlank(callbackUri) ? FBSummarizationTaskDetails.generateFinishedCallbackUri() : callbackUri);
	}

	/**
	 * for sub-classing, use the static
	 * 
	 * @return true if this task has no content
	 * @see #isEmpty(FBFeedbackTaskDetails)
	 */
	protected boolean isEmpty() {
		return MediaObjectList.isEmpty(_tags);
	}
	
	/**
	 * 
	 * @param details
	 * @return true if the given list was null or contained no data
	 */
	public static boolean isEmpty(FBFeedbackTaskDetails details){
		return (details == null || details.isEmpty());
	}

	@Override
	public TaskParameters getTaskParameters() {
		return null;
	}

	@Override
	public void setTaskParameters(TaskParameters parameters) {
		throw new UnsupportedOperationException("Method not supported.");
	}
}
