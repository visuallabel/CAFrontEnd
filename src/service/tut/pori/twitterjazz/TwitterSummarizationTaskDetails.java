/**
 * Copyright 2014 Tampere University of Technology, Pori Department
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
package service.tut.pori.twitterjazz;

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

import service.tut.pori.contentanalysis.AbstractTaskDetails;
import service.tut.pori.contentanalysis.AsyncTask.TaskType;
import service.tut.pori.twitterjazz.TwitterExtractor.ContentType;
import core.tut.pori.context.ServiceInitializer;

/**
 * Details for a twitter summarization task.
 * 
 * <h3>XML Example</h3>
 * 
 * {@doc.restlet service="[service.tut.pori.twitterjazz.reference.Definitions#SERVICE_TJ_REFERENCE_EXAMPLE]" method="[service.tut.pori.contentanalysis.Definitions#ELEMENT_TASK_DETAILS]" type="GET" query="" body_uri=""}
 * 
 * @see service.tut.pori.twitterjazz.TwitterProfile
 */
@XmlRootElement(name=service.tut.pori.contentanalysis.Definitions.ELEMENT_TASK_DETAILS)
@XmlAccessorType(XmlAccessType.NONE)
public final class TwitterSummarizationTaskDetails extends AbstractTaskDetails{
	private static final Logger LOGGER = Logger.getLogger(TwitterSummarizationTaskDetails.class);
	private static final String METADATA_CONTENT_TYPES = "contentTypes";
	private static final String METADATA_SCREEN_NAME = "screenName";
	private static final String METADATA_SUMMARIZE = "summarize";
	private static final String METADATA_SYNCHRONIZE = "synchronize";
	@XmlElement(name = Definitions.ELEMENT_TWITTER_PROFILE)
	private TwitterProfile _profile = null;

	/**
	 * 
	 * @return profile
	 */
	public TwitterProfile getProfile() {
		return _profile;
	}

	/**
	 * 
	 * @param profile
	 */
	public void setProfile(TwitterProfile profile) {
		_profile = profile;
	}
	
	/**
	 * 
	 */
	public TwitterSummarizationTaskDetails(){
		setTaskType(TaskType.TWITTER_PROFILE_SUMMARIZATION);
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
		return ServiceInitializer.getPropertyHandler().getRESTBindContext()+Definitions.SERVICE_TJ+"/"+service.tut.pori.contentanalysis.Definitions.METHOD_TASK_FINISHED;
	}
	
	/**
	 * 
	 * @param screenName
	 */
	public void setScreenName(String screenName){
		Map<String, String> metadata = getMetadata();
		if(StringUtils.isBlank(screenName)){
			LOGGER.debug("No screen name.");
			if(metadata != null){
				metadata.remove(METADATA_SCREEN_NAME);
				if(metadata.isEmpty()){
					setMetadata(null);
				}
			}
		}else{
			metadata.put(METADATA_SCREEN_NAME, screenName);
			setMetadata(metadata);
		}
	}
	
	/**
	 * 
	 * @return twitter screen name
	 */
	public String getScreenName(){
		Map<String, String> metadata = getMetadata();
		if(metadata == null || metadata.isEmpty()){
			LOGGER.debug("No metadata.");
			return null;
		}

		return metadata.get(METADATA_SCREEN_NAME);
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
	 * @param summarize
	 */
	public void setSummarize(boolean summarize){
		Map<String, String> metadata = getMetadata();
		if(metadata == null){
			LOGGER.debug("No metadata, creating metadata.");
			metadata = new HashMap<>(1);
			setMetadata(metadata);
		}
		metadata.put(METADATA_SUMMARIZE, BooleanUtils.toStringTrueFalse(summarize));
	}
	
	/**
	 * 
	 * @return true if summarization is enabled for this task
	 */
	public boolean isSummarize(){
		Map<String, String> metadata = getMetadata();
		if(metadata == null || metadata.isEmpty()){
			LOGGER.debug("No metadata "+METADATA_SUMMARIZE+", returning default: false.");
			return false;
		}
		return BooleanUtils.toBoolean(metadata.get(METADATA_SUMMARIZE));
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
	 * @return true if synchronization is enabled for this task
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
