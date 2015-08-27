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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;

import service.tut.pori.contentanalysis.AsyncTask.TaskStatus;
import service.tut.pori.contentanalysis.AsyncTask.TaskType;
import service.tut.pori.contentanalysis.Definitions;
import service.tut.pori.contentanalysis.MediaObject;
import service.tut.pori.contentanalysis.MediaObject.ConfirmationStatus;
import service.tut.pori.contentanalysis.MediaObject.MediaObjectType;
import core.tut.pori.context.ServiceInitializer;
import core.tut.pori.utils.HTTPHeaderUtil;
import core.tut.pori.utils.MediaUrlValidator.MediaType;
import core.tut.pori.utils.XMLFormatter;

/**
 * Simple runnable for executing FuzzyAnalysisTask
 * 
 * Assumes that the given task is of type {@link service.tut.pori.contentanalysis.AsyncTask.TaskType#ANALYSIS}
 * 
 */
public class FuzzyAnalysisTask implements Runnable {
	private static final Logger LOGGER = Logger.getLogger(FuzzyAnalysisTask.class);
	private static final int MAX_MEDIA_ITEMS = 10;
	private Integer _backendId = null;
	private String _callbackUri = null;
	private List<FuzzyMedia> _media = null;
	private Random _random = null;
	private Long _taskId = null;

	/**
	 * 
	 * @param backendId
	 * @param callbackUri
	 * @param media
	 * @param taskId
	 * @throws IllegalArgumentException on bad data
	 */
	public FuzzyAnalysisTask(Integer backendId, String callbackUri, List<FuzzyMedia> media, Long taskId) throws IllegalArgumentException {
		if(taskId == null){
			throw new IllegalArgumentException("Invalid "+Definitions.ELEMENT_TASK_ID);
		}
		_taskId = taskId;
		
		if(backendId == null){
			throw new IllegalArgumentException("Invalid "+Definitions.ELEMENT_BACKEND_ID+" for task, id : "+_taskId);
		}
		_backendId = backendId;
		if(StringUtils.isBlank(callbackUri)){
			throw new IllegalArgumentException("Invalid "+Definitions.ELEMENT_CALLBACK_URI+" : "+callbackUri+" for task, id : "+_taskId);
		}
		_callbackUri = callbackUri;
		
		if(media == null || media.isEmpty()){
			throw new IllegalArgumentException("No content for task, id : "+_taskId);
		}
		for(FuzzyMedia m : media){
			if(!FuzzyMedia.isValid(m)){
				throw new IllegalArgumentException("Invalid media, "+Definitions.ELEMENT_GUID+" : "+m.getGUID()+" for task, id : "+_taskId);
			}
		}
		
		_media = new ArrayList<>(media); // preserve the original list
		_random = new Random();
		if(_media.size() > MAX_MEDIA_ITEMS){ // limit the result size to prevent excessive abusing of Google's Translation service
			media = _media;
			Collections.shuffle(media, _random); // shuffle to get random items
			
			_media = new ArrayList<>(MAX_MEDIA_ITEMS);
			Iterator<FuzzyMedia> iter = media.iterator();
			for(int i=0;i<MAX_MEDIA_ITEMS;++i){
				_media.add(iter.next());
			}
		}
	}

	@Override
	public void run() {
		List<FuzzyMedia> results = new ArrayList<>(_media.size());
		try(FuzzyAnalyzer fa = new FuzzyAnalyzer()){
			for(FuzzyMedia m : _media){
				Set<String> words = fa.analyze(m.getUrl());
				if(words != null){
					MediaType mediaType = m.getMediaType();
					String guid = m.getGUID();
					List<MediaObject> objects = new ArrayList<>(words.size());
					for(String word : words){
						MediaObject mo = new MediaObject(mediaType, MediaObjectType.KEYWORD);
						mo.setBackendId(_backendId);
						mo.setConfirmationStatus(ConfirmationStatus.CANDIDATE);
						mo.setValue(word);
						mo.setOwnerUserId(m.getOwnerUserId());
						mo.setObjectId(guid+"_"+word);
						mo.setConfidence(_random.nextInt(100)/100.0);
						objects.add(mo);
					} // for
					
					results.add(new FuzzyMedia(guid, objects, mediaType));
				} // if
			} // for
		} // try
		
		TaskResults tr = new TaskResults(_backendId, _taskId, TaskStatus.COMPLETED, TaskType.ANALYSIS);
		
		if(results.isEmpty()){
			LOGGER.debug("No results for task, id : "+_taskId);
		}else{
			tr.setMedia(results);
		}
		
		try(CloseableHttpClient client = HttpClients.createDefault()){
			HttpPost post = new HttpPost(_callbackUri);
			FuzzyProperties fp = ServiceInitializer.getPropertyHandler().getSystemProperties(FuzzyProperties.class);
			String username = fp.getAuthUsername();
			if(username != null){ // checking either password or username is OK
				LOGGER.debug("Using authentication...");
				HTTPHeaderUtil.setHTTPBasicAuthHeader(post, username, fp.getAuthPassword());
			}
			
			post.setEntity(new StringEntity(new XMLFormatter().toString(tr)));
			
			LOGGER.debug("Calling POST "+_callbackUri);
			LOGGER.debug("Server responded : "+client.execute(post, new BasicResponseHandler()));
		} catch (IOException ex) {
			LOGGER.error(ex, ex);
		}
	}
}
