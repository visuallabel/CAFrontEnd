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
package service.tut.pori.contentanalysis.video;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import service.tut.pori.contentanalysis.AnalysisBackend;
import service.tut.pori.contentanalysis.Definitions;
import service.tut.pori.contentanalysis.AnalysisBackend.Capability;
import service.tut.pori.contentanalysis.PhotoParameters.AnalysisType;
import service.tut.pori.contentanalysis.BackendDAO;
import service.tut.pori.contentanalysis.CAContentCore.ServiceType;
import core.tut.pori.context.ServiceInitializer;
import core.tut.pori.http.Response;
import core.tut.pori.http.parameters.DataGroups;
import core.tut.pori.http.parameters.Limits;
import core.tut.pori.users.UserIdentity;
import core.tut.pori.utils.XMLFormatter;

/**
 * Search task used to query back-ends for results. 
 * 
 * This class is not asynchronous and will block for the duration of execution, also this task cannot be submitted to system schedulers.
 * 
 */
public class VideoSearchTask{
	/** Back-end capability required for search tasks */
	public static final Capability SEARCH_CAPABILITY = Capability.VIDEO_SEARCH;
	private static final int CONNECTION_SOCKET_TIME_OUT = 10000;  // connection socket time out in milliseconds
	private static final XMLFormatter FORMATTER = new XMLFormatter();
	private static final Logger LOGGER = Logger.getLogger(VideoSearchTask.class);
	private EnumSet<AnalysisType> _analysisTypes = null;
	private UserIdentity _authenticatedUser = null;
	private DataGroups _dataGroups = null;
	private String _guid = null;
	private Limits _limits = null;
	private EnumSet<ServiceType> _serviceTypeFilter = null;
	private long[] _userIdFilter = null;
	
	/**
	 * Execute this task
	 * 
	 * @return results of the search or null if no results
	 */
	public VideoList execute(){
		List<AnalysisBackend> backends = ServiceInitializer.getDAOHandler().getSQLDAO(BackendDAO.class).getBackends(SEARCH_CAPABILITY);
		if(backends == null){
			LOGGER.warn("No backends with capability "+SEARCH_CAPABILITY.name());
			return null;
		}
		
		try (PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(); CloseableHttpClient client =  HttpClientBuilder.create().setConnectionManager(cm).setDefaultSocketConfig(SocketConfig.custom().setSoTimeout(CONNECTION_SOCKET_TIME_OUT).build()).build()){
			ArrayList<Searcher> searchers = new ArrayList<>(backends.size());
			String baseParameterString = generateParameterString();	// create the default parameter string
			for(AnalysisBackend end : backends){
				searchers.add(new Searcher(end, baseParameterString));
			}
			
			List<Future<VideoList>> results = ServiceInitializer.getExecutorHandler().getExecutor().invokeAll(searchers);
			List<VideoList> videoLists = new ArrayList<>(searchers.size());
			for(Future<VideoList> result : results){
				if(result.isDone() && !result.isCancelled()){
					try {
						VideoList r = result.get();
						if(!VideoList.isEmpty(r)){
							videoLists.add(r);
						}
					} catch (ExecutionException ex) {
						LOGGER.warn(ex, ex);
					}
				}else{
					LOGGER.debug("Ignoring unsuccessful result.");
				}	// for
			}
			if(videoLists.isEmpty()){
				LOGGER.debug("No search results.");
			}else{
				VideoList videos = combineResults(_authenticatedUser, _dataGroups, _limits, videoLists);
				return removeTargetVideo(videos);
			}
		} catch (IOException | InterruptedException ex) {
			LOGGER.error(ex, ex);
		}
		return null;
	}
	
	/**
	 * Combine the given lists of videos. Non-existent and duplicate videos and videos which the user cannot access will be automatically removed.
	 * 
	 * @param authenticatedUser
	 * @param dataGroups
	 * @param limits note that if there is a limit for maximum number of results, the videos for the combined list will be selected randomly from the given video lists
	 * @param videoLists the video lists, note that only GUIDs have any meaning, all other details will be retrieved from the database based on the given data groups.
	 * @return the given video lists combined into a single list, removing duplicates or null if no content or null collection was passed
	 */
	private VideoList combineResults(UserIdentity authenticatedUser, DataGroups dataGroups, Limits limits, Collection<VideoList> videoLists){
		if(videoLists == null || videoLists.isEmpty()){
			return null;
		}
		Set<String> guids = new HashSet<>();
		for(VideoList videoList : videoLists){	// collect all unique GUIDs
			for(Video video : videoList.getVideos()){
				guids.add(video.getGUID());
			}
		}
		VideoList l = ServiceInitializer.getDAOHandler().getSolrDAO(VideoDAO.class).search(authenticatedUser, dataGroups, guids, limits, null, null, null); // the back-ends are not guaranteed to return results the user has permissions to view, so re-search everything just in case
		if(VideoList.isEmpty(l)){
			LOGGER.debug("No videos found.");
		}
		return l;
	}
	
	/**
	 * Removes the video with the given search term (GUID), if GUID based search was performed.
	 * 
	 * @param videos the list from which the reference video is to be removed.
	 * @return the list with the reference video removed or null if nothing was left after removal
	 */
	private VideoList removeTargetVideo(VideoList videos){
		if(VideoList.isEmpty(videos)){
			LOGGER.debug("Empty video list.");
			return null;
		}
		if(_guid != null){
			LOGGER.debug("Removing target video, if present in the search results...");
			for(Iterator<Video> iter = videos.getVideos().iterator();iter.hasNext();){
				if(iter.next().getGUID().equals(_guid)){
					iter.remove();
				}
			}
		}	// if
		
		if(VideoList.isEmpty(videos)){
			LOGGER.debug("No videos were left in the list, returning null..");
			return null;
		}else{
			return videos;
		}
	}
	
	/**
	 * Create new search task using the GUID as a reference, the GUID is assumed to exist, no database lookup is going to be done to confirm it
	 * 
	 * @param authenticatedUser optional authenticated user
	 * @param analysisTypes 
	 * @param dataGroups optional data groups
	 * @param guid
	 * @param limits optional limits
	 * @param serviceTypeFilter optional service type filter
	 * @param userIdFilter optional user id filter, if given only the content for the specific users will searched for
	 * @return new task
	 * @throws IllegalArgumentException on bad GUID
	 */
	public static VideoSearchTask getTaskByGUID(UserIdentity authenticatedUser, EnumSet<AnalysisType> analysisTypes, DataGroups dataGroups, String guid, Limits limits, EnumSet<ServiceType> serviceTypeFilter, long[] userIdFilter) throws IllegalArgumentException{
		if(StringUtils.isBlank(guid)){
			throw new IllegalArgumentException("GUID was null or empty.");
		}
		VideoSearchTask task = new VideoSearchTask(authenticatedUser, analysisTypes, dataGroups, limits, serviceTypeFilter, userIdFilter);
		task._guid = guid;
		return task;
	}
	
	/**
	 * 
	 * @param authenticatedUser
	 * @param analysisTypes 
	 * @param dataGroups
	 * @param limits
	 * @param serviceTypeFilter
	 * @param userIdFilter
	 */
	private VideoSearchTask(UserIdentity authenticatedUser, EnumSet<AnalysisType> analysisTypes, DataGroups dataGroups, Limits limits, EnumSet<ServiceType> serviceTypeFilter, long[] userIdFilter){
		if(UserIdentity.isValid(authenticatedUser)){
			_authenticatedUser = authenticatedUser;
			LOGGER.debug("Searching with authenticated user, id: "+authenticatedUser.getUserId());
		}else{
			LOGGER.debug("No authenticated user.");
		}
		
		if(!DataGroups.isEmpty(dataGroups)){
			_dataGroups = dataGroups;
		}
		
		_limits = limits;
		
		if(serviceTypeFilter != null && !serviceTypeFilter.isEmpty()){
			LOGGER.debug("Using service type filter...");
			_serviceTypeFilter = serviceTypeFilter;
		}
		
		if(!ArrayUtils.isEmpty(userIdFilter)){
			LOGGER.debug("Using user id filter...");
			_userIdFilter = userIdFilter;
		}
		
		if(analysisTypes != null && !analysisTypes.isEmpty()){
			LOGGER.debug("Analysis types specified...");
			_analysisTypes = analysisTypes;
		}
	}
	
	/**
	 * Helper method for creating the default parameter string: filters, limits & data groups
	 * 
	 * @return parameter string or null if none
	 */
	private String generateParameterString(){
		StringBuilder sb = new StringBuilder();
		if(_userIdFilter != null){
			sb.append(core.tut.pori.http.Definitions.SEPARATOR_URI_QUERY_PARAMS+service.tut.pori.users.Definitions.PARAMETER_USER_ID+core.tut.pori.http.Definitions.SEPARATOR_URI_QUERY_PARAM_VALUE_SEPARATOR);
			core.tut.pori.utils.StringUtils.append(sb, _userIdFilter, core.tut.pori.http.Definitions.SEPARATOR_URI_QUERY_PARAM_VALUES);
		}
		
		if(_serviceTypeFilter != null){
			sb.append(core.tut.pori.http.Definitions.SEPARATOR_URI_QUERY_PARAMS+service.tut.pori.contentanalysis.Definitions.PARAMETER_SERVICE_ID+core.tut.pori.http.Definitions.SEPARATOR_URI_QUERY_PARAM_VALUE_SEPARATOR);
			core.tut.pori.utils.StringUtils.append(sb, ServiceType.toIdArray(_serviceTypeFilter), core.tut.pori.http.Definitions.SEPARATOR_URI_QUERY_PARAM_VALUES);
		}
		
		if(_limits != null){
			sb.append(core.tut.pori.http.Definitions.SEPARATOR_URI_QUERY_PARAMS+Limits.PARAMETER_DEFAULT_NAME+core.tut.pori.http.Definitions.SEPARATOR_URI_QUERY_PARAM_VALUE_SEPARATOR);
			sb.append(_limits.toLimitString());			
		}
		
		if(_analysisTypes != null){
			sb.append(core.tut.pori.http.Definitions.SEPARATOR_URI_QUERY_PARAMS+Definitions.PARAMETER_ANALYSIS_TYPE+core.tut.pori.http.Definitions.SEPARATOR_URI_QUERY_PARAM_VALUE_SEPARATOR);
			Iterator<AnalysisType> iter = _analysisTypes.iterator();
			sb.append(iter.next().toAnalysisTypeString());
			while(iter.hasNext()){
				sb.append(core.tut.pori.http.Definitions.SEPARATOR_URI_QUERY_PARAM_VALUES);
				sb.append(iter.next().toAnalysisTypeString());
			}
		}
		
		if(sb.length() < 1){
			return null;
		}else{
			return sb.toString();
		}
	}

	/**
	 * Internal class, which executes the search queries to back-ends.
	 *
	 */
	private class Searcher implements Callable<VideoList>{
		private AnalysisBackend _backend = null;
		private String _baseParameterString = null;
		
		/**
		 * 
		 * @param end
		 * @param baseParameterString 
		 */
		public Searcher(AnalysisBackend end, String baseParameterString){
			_backend = end;
			_baseParameterString = baseParameterString;
		}

		@Override
		public VideoList call() throws Exception {
			StringBuilder uri = new StringBuilder(_backend.getAnalysisUri());
			uri.append(Definitions.METHOD_SEARCH_SIMILAR_BY_ID+"?"+service.tut.pori.contentanalysis.Definitions.PARAMETER_GUID+"=");
			uri.append(_guid);
			
			if(_baseParameterString != null){
				uri.append(_baseParameterString);
			}	
			String url = uri.toString();
			LOGGER.debug("Calling URL: "+url);
			try (CloseableHttpClient client = HttpClients.createDefault(); CloseableHttpResponse response = client.execute(new HttpPost(url))) {
				HttpEntity entity = response.getEntity();
				try (InputStream content = entity.getContent()) {
					Response r = FORMATTER.toResponse(content, VideoList.class);
					if(r == null){
						LOGGER.warn("No results returned by backend, id: "+_backend.getBackendId());
					}else{
						return (VideoList) r.getResponseData();
					}
				} finally {
					EntityUtils.consume(entity);
				}	// try content
			}	// try response
			return null;
		}
		
	} //  class Searcher
}