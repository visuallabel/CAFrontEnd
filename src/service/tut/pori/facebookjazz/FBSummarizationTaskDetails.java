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

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import core.tut.pori.context.ServiceInitializer;
import service.tut.pori.contentanalysis.AbstractTaskDetails;
import service.tut.pori.contentanalysis.AsyncTask.TaskType;
import service.tut.pori.facebookjazz.FacebookExtractor.ContentType;

/**
 * Details of a Facebook Summarization Task.
 * 
 * <h3>XML Example</h3>
 * 
 * {@doc.restlet service="[service.tut.pori.facebookjazz.reference.Definitions#SERVICE_FBJ_REFERENCE_EXAMPLE]" method="[service.tut.pori.contentanalysis.Definitions#ELEMENT_TASK_DETAILS]" type="GET" query="[service.tut.pori.facebookjazz.reference.Definitions#PARAMETER_TASK_TYPE]=[service.tut.pori.contentanalysis.AsyncTask$TaskType#FACEBOOK_PROFILE_SUMMARIZATION]" body_uri=""} 
 * 
 * @see service.tut.pori.facebookjazz.FacebookProfile
 */
@XmlRootElement(name=service.tut.pori.contentanalysis.Definitions.ELEMENT_TASK_DETAILS)
@XmlAccessorType(XmlAccessType.NONE)
public final class FBSummarizationTaskDetails extends AbstractTaskDetails{
	private static final Logger LOGGER = Logger.getLogger(FBSummarizationTaskDetails.class);
	private static final String METADATA_CONTENT_TYPES = "contentTypes";
	private static final String METADATA_SYNCHRONIZE = "synchronize";
	@XmlElement(name = Definitions.ELEMENT_FACEBOOK_PROFILE)
	private FacebookProfile _profile = null;

	/**
	 * 
	 * @return profile
	 * @see #setProfile(FacebookProfile)
	 */
	public FacebookProfile getProfile() {
		return _profile;
	}

	/**
	 * 
	 * @param profile
	 * @see #getProfile()
	 */
	public void setProfile(FacebookProfile profile) {
		_profile = profile;
	}
	
	/**
	 * 
	 */
	public FBSummarizationTaskDetails(){
		setTaskType(TaskType.FACEBOOK_PROFILE_SUMMARIZATION);
	}
	
	@XmlElement(name = service.tut.pori.contentanalysis.Definitions.ELEMENT_CALLBACK_URI)
	@Override
	public String getCallbackUri() {
		String callbackUri = super.getCallbackUri();
		return (StringUtils.isBlank(callbackUri) ? generateFinishedCallbackUri() : callbackUri);
	}
	
	/**
	 * 
	 * @return the default task finished callback uri
	 */
	public static String generateFinishedCallbackUri(){
		return ServiceInitializer.getPropertyHandler().getRESTBindContext()+Definitions.SERVICE_FBJ+"/"+service.tut.pori.contentanalysis.Definitions.METHOD_TASK_FINISHED;
	}

	/**
	 * @return the contentTypes
	 */
	public EnumSet<ContentType> getContentTypes() {
		Map<String, String> metadata = getMetadata();
		if(metadata == null || metadata.isEmpty()){
			LOGGER.debug("No metadata.");
			return null;
		}

		String[] types = StringUtils.split(metadata.get(METADATA_CONTENT_TYPES), core.tut.pori.http.Definitions.SEPARATOR_URI_QUERY_PARAM_VALUES);
		if(types == null){
			LOGGER.debug("No content types.");
			return null;
		}		
		
		EnumSet<ContentType> ctypes = EnumSet.noneOf(ContentType.class);
		ContentType[] tValues = ContentType.values();
		for(String type : types){
			for(ContentType t : tValues){
				if(t.name().equals(type)){
					ctypes.add(t);
				}
			}
		}
		
		if(ctypes.isEmpty()){
			LOGGER.warn("No valid content types.");
			return null;
		}else{
			return ctypes;
		}
	}

	/**
	 * @param contentTypes the contentTypes to set
	 */
	public void setContentTypes(Set<ContentType> contentTypes) {
		Map<String, String> metadata = getMetadata();
		if(contentTypes == null || contentTypes.isEmpty()){
			LOGGER.debug("No content types.");
			if(metadata != null){
				metadata.remove(METADATA_CONTENT_TYPES);
				if(metadata.isEmpty()){
					setMetadata(null);
				}
			}
			return;
		}
		StringBuilder cb = new StringBuilder();
		for(ContentType t : contentTypes){
			cb.append(t.name());
			cb.append(core.tut.pori.http.Definitions.SEPARATOR_URI_QUERY_PARAM_VALUES);
		}
		if(metadata == null){
			metadata = new HashMap<>(1);
		}
		metadata.put(METADATA_CONTENT_TYPES, cb.substring(0, cb.length()-1));
		setMetadata(metadata);
	}
	
	/**
	 * 
	 * @param synchronize
	 */
	public void setSynchronize(boolean synchronize){
		Map<String, String> metadata = getMetadata();
		if(metadata == null){
			LOGGER.debug("No metadata, creating metadata.");
			metadata = new HashMap<>(1);
			setMetadata(metadata);
		}
		metadata.put(METADATA_SYNCHRONIZE, BooleanUtils.toStringTrueFalse(synchronize));
	}
	
	/**
	 * 
	 * @return true if synchronization is enabled for the task
	 */
	public boolean isSynchronize(){
		Map<String, String> metadata = getMetadata();
		if(metadata == null || metadata.isEmpty()){
			LOGGER.debug("No metadata "+METADATA_SYNCHRONIZE+", returning default: false.");
			return false;
		}
		return BooleanUtils.toBoolean(metadata.get(METADATA_SYNCHRONIZE));
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
