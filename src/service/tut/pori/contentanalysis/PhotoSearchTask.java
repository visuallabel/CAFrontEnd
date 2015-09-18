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
package service.tut.pori.contentanalysis;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.commons.codec.net.URLCodec;
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

import service.tut.pori.contentanalysis.AnalysisBackend.Capability;
import service.tut.pori.contentanalysis.CAContentCore.ServiceType;
import service.tut.pori.contentanalysis.PhotoParameters.AnalysisType;
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
public class PhotoSearchTask{
	/** Back-end capability required for search tasks */
	public static final Capability SEARCH_CAPABILITY = Capability.PHOTO_SEARCH;
	private static final int CONNECTION_SOCKET_TIME_OUT = 10000;  // connection socket time out in milliseconds
	private static final XMLFormatter FORMATTER = new XMLFormatter();
	private static final Logger LOGGER = Logger.getLogger(PhotoSearchTask.class);
	private static final URLCodec URLCODEC = new URLCodec(core.tut.pori.http.Definitions.ENCODING_UTF8); 
	private EnumSet<AnalysisType> _analysisTypes = null;
	private UserIdentity _authenticatedUser = null;
	private DataGroups _dataGroups = null;
	private String _guid = null;
	private Limits _limits = null;
	private EnumSet<ServiceType> _serviceTypeFilter = null;
	private String _url = null;
	private long[] _userIdFilter = null;
	
	/**
	 * Execute this task
	 * 
	 * @return results of the search or null if no results
	 */
	public PhotoList execute(){
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
			
			List<Future<PhotoList>> results = ServiceInitializer.getExecutorHandler().getExecutor().invokeAll(searchers);
			List<PhotoList> photoLists = new ArrayList<>(searchers.size());
			for(Future<PhotoList> result : results){
				if(result.isDone() && !result.isCancelled()){
					try {
						PhotoList r = result.get();
						if(!PhotoList.isEmpty(r)){
							photoLists.add(r);
						}
					} catch (ExecutionException ex) {
						LOGGER.warn(ex, ex);
					}
				}else{
					LOGGER.debug("Ignoring unsuccessful result.");
				}	// for
			}
			if(photoLists.isEmpty()){
				LOGGER.debug("No search results.");
			}else{
				PhotoList photos = combineResults(_authenticatedUser, _dataGroups, _limits, photoLists);
				return removeTargetPhoto(photos);
			}
		} catch (IOException | InterruptedException ex) {
			LOGGER.error(ex, ex);
		}
		return null;
	}
	
	/**
	 * Combine the given lists of photos. Non-existent and duplicate photos and photos which the user cannot access will be automatically removed.
	 * 
	 * @param authenticatedUser
	 * @param dataGroups
	 * @param limits note that if there is a limit for maximum number of results, the photos for the combined list will be selected randomly from the given photo lists
	 * @param photoLists the photo lists, note that only GUIDs have any meaning, all other details will be retrieved from the database based on the given data groups.
	 * @return the given photo lists combined into a single list, removing duplicates or null if no content or null collection was passed
	 */
	private PhotoList combineResults(UserIdentity authenticatedUser, DataGroups dataGroups, Limits limits, Collection<PhotoList> photoLists){
		if(photoLists == null || photoLists.isEmpty()){
			return null;
		}
		Set<String> guids = new LinkedHashSet<>();
		for(PhotoList photoList : photoLists){	// collect all unique GUIDs. Note: this will prioritize the FASTER back-end, and does not guarantee that MORE APPLICABLE results appear first. This is because we have no easy (defined) way to decide which results are better than others.
			for(Photo photo : photoList.getPhotos()){
				guids.add(photo.getGUID());
			}
		}
		if(!StringUtils.isBlank(_guid)){ // if search target is given, make sure it does not appear in the results
			guids.remove(_guid);
		}
		
		PhotoList results = ServiceInitializer.getDAOHandler().getSolrDAO(PhotoDAO.class).search(authenticatedUser, dataGroups, guids, limits, null, null, null); // the back-ends are not guaranteed to return results the user has permissions to view, so re-search everything just in case
		if(PhotoList.isEmpty(results)){
			LOGGER.debug("No photos found.");
			return null;
		}
		
		List<Photo> sorted = new ArrayList<>(guids.size());
		for(String guid : guids){ // sort the results back to the original order
			Photo photo = results.getPhoto(guid);
			if(photo == null){
				LOGGER.warn("Ignored photo with non-existing GUID: "+guid);
			}else{
				sorted.add(photo);
			}
		}
		results.setPhotos(sorted);
		return results;
	}
	
	/**
	 * Removes the photo with the given search term (GUID), if GUID based search was performed.
	 * 
	 * @param photos the list from which the reference photo is to be removed.
	 * @return the list with the reference photo removed or null if nothing was left after removal
	 */
	private PhotoList removeTargetPhoto(PhotoList photos){
		if(PhotoList.isEmpty(photos)){
			LOGGER.debug("Empty photo list.");
			return null;
		}
		if(_guid != null){
			LOGGER.debug("Removing target photo, if present in the search results...");
			for(Iterator<Photo> iter = photos.getPhotos().iterator();iter.hasNext();){
				if(iter.next().getGUID().equals(_guid)){
					iter.remove();
				}
			}
		}	// if
		
		if(PhotoList.isEmpty(photos)){
			LOGGER.debug("No photos were left in the list, returning null..");
			return null;
		}else{
			return photos;
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
	public static PhotoSearchTask getTaskByGUID(UserIdentity authenticatedUser, EnumSet<AnalysisType> analysisTypes, DataGroups dataGroups, String guid, Limits limits, EnumSet<ServiceType> serviceTypeFilter, long[] userIdFilter) throws IllegalArgumentException{
		if(StringUtils.isBlank(guid)){
			throw new IllegalArgumentException("GUID was null or empty.");
		}
		PhotoSearchTask task = new PhotoSearchTask(authenticatedUser, analysisTypes, dataGroups, limits, serviceTypeFilter, userIdFilter);
		task._guid = guid;
		return task;
	}
	
	/**
	 * Create new search task using the URL as a search parameter. The URL is assumed to be valid, and no validation is performed.
	 * 
	 * @param authenticatedUser optional authenticated user
	 * @param analysisTypes 
	 * @param dataGroups optional data groups
	 * @param limits optional limits
	 * @param serviceTypeFilter optional service type filter
	 * @param url
	 * @param userIdFilter optional user id filter, if given only the content for the specific users will searched for
	 * @return new task
	 * @throws IllegalArgumentException on bad URL
	 */
	public static PhotoSearchTask getTaskByUrl(UserIdentity authenticatedUser, EnumSet<AnalysisType> analysisTypes, DataGroups dataGroups, Limits limits, EnumSet<ServiceType> serviceTypeFilter, String url, long[] userIdFilter) throws IllegalArgumentException{
		if(StringUtils.isBlank(url)){
			throw new IllegalArgumentException("Url was null or empty.");
		}
		PhotoSearchTask task = new PhotoSearchTask(authenticatedUser, analysisTypes, dataGroups, limits, serviceTypeFilter, userIdFilter);
		task._url = url;
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
	private PhotoSearchTask(UserIdentity authenticatedUser, EnumSet<AnalysisType> analysisTypes, DataGroups dataGroups, Limits limits, EnumSet<ServiceType> serviceTypeFilter, long[] userIdFilter){
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
			_analysisTypes  = analysisTypes;
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
			sb.append(core.tut.pori.http.Definitions.SEPARATOR_URI_QUERY_PARAMS+Definitions.PARAMETER_SERVICE_ID+core.tut.pori.http.Definitions.SEPARATOR_URI_QUERY_PARAM_VALUE_SEPARATOR);
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
	private class Searcher implements Callable<PhotoList>{
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
		public PhotoList call() throws Exception {
			StringBuilder uri = new StringBuilder(_backend.getAnalysisUri());
			if(_url != null){
				uri.append(Definitions.METHOD_SEARCH_SIMILAR_BY_CONTENT+"?"+Definitions.PARAMETER_URL+"=");
				uri.append(URLCODEC.encode(_url));
			}else{	// guid != null
				uri.append(Definitions.METHOD_SEARCH_SIMILAR_BY_ID+"?"+Definitions.PARAMETER_GUID+"=");
				uri.append(_guid);
			}
			
			if(_baseParameterString != null){
				uri.append(_baseParameterString);
			}	
			String url = uri.toString();
			LOGGER.debug("Calling URL: "+url+" for back-end, id: "+_backend.getBackendId());
			try (CloseableHttpClient client = HttpClients.createDefault(); CloseableHttpResponse response = client.execute(new HttpPost(url))) {
				HttpEntity entity = response.getEntity();
				try (InputStream content = entity.getContent()) {
					Response r = FORMATTER.toResponse(content, PhotoList.class);
					if(r == null){
						LOGGER.warn("No results returned by backend, id: "+_backend.getBackendId());
					}else{
						return (PhotoList) r.getResponseData();
					}
				} finally {
					EntityUtils.consume(entity);
				}	// try content
			}	// try response
			return null;
		}
		
	} //  class Searcher
}