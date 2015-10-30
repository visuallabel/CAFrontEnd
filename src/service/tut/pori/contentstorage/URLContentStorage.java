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
package service.tut.pori.contentstorage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import service.tut.pori.contentanalysis.AbstractTaskDetails;
import service.tut.pori.contentanalysis.AccessDetails;
import service.tut.pori.contentanalysis.AnalysisBackend.Capability;
import service.tut.pori.contentanalysis.PhotoParameters;
import service.tut.pori.contentanalysis.PhotoParameters.AnalysisType;
import service.tut.pori.contentanalysis.AsyncTask.TaskType;
import service.tut.pori.contentanalysis.CAContentCore;
import service.tut.pori.contentanalysis.CAContentCore.ServiceType;
import service.tut.pori.contentanalysis.CAContentCore.Visibility;
import service.tut.pori.contentanalysis.DeletedPhotoList;
import service.tut.pori.contentanalysis.Photo;
import service.tut.pori.contentanalysis.PhotoDAO;
import service.tut.pori.contentanalysis.PhotoFeedbackTask.FeedbackTaskBuilder;
import service.tut.pori.contentanalysis.PhotoList;
import service.tut.pori.contentanalysis.PhotoTaskDetails;
import service.tut.pori.contentanalysis.video.DeletedVideoList;
import service.tut.pori.contentanalysis.video.Video;
import service.tut.pori.contentanalysis.video.VideoContentCore;
import service.tut.pori.contentanalysis.video.VideoDAO;
import service.tut.pori.contentanalysis.video.VideoFeedbackTask;
import service.tut.pori.contentanalysis.video.VideoList;
import service.tut.pori.contentanalysis.video.VideoParameters;
import service.tut.pori.contentanalysis.video.VideoTaskDetails;
import core.tut.pori.context.ServiceInitializer;
import core.tut.pori.users.UserIdentity;
import core.tut.pori.utils.MediaUrlValidator;
import core.tut.pori.utils.MediaUrlValidator.MediaType;
import core.tut.pori.utils.UserIdentityLock;

/**
 * <p>Storage handler that supports saving arbitrary URLs to for the analysis. 
 * The URLs must be of valid image content. ImageValidator is used for performing a simple content check.</p>
 * <p>This storage service does not split tasks into smaller chunks like the other content storage services.</p>
 */
public final class URLContentStorage extends ContentStorage {
	/** Service type declaration for this storage */
	public static final ServiceType SERVICE_TYPE = ServiceType.URL_STORAGE;
	private static final PhotoParameters ANALYSIS_PARAMETERS_PHOTO;
	private static final VideoParameters ANALYSIS_PARAMETERS_VIDEO;
	static{
		ANALYSIS_PARAMETERS_PHOTO = new PhotoParameters();
		ANALYSIS_PARAMETERS_PHOTO.setAnalysisTypes(EnumSet.of(AnalysisType.FACE_DETECTION, AnalysisType.KEYWORD_EXTRACTION, AnalysisType.VISUAL));
		ANALYSIS_PARAMETERS_VIDEO = new VideoParameters();
		ANALYSIS_PARAMETERS_VIDEO.setSequenceDuration(1);
		ANALYSIS_PARAMETERS_VIDEO.setAnalysisTypes(EnumSet.of(AnalysisType.VISUAL, AnalysisType.KEYWORD_EXTRACTION));
	}
	private static final EnumSet<Capability> CAPABILITIES = EnumSet.of(Capability.PHOTO_ANALYSIS);
	private static final Logger LOGGER = Logger.getLogger(URLContentStorage.class);
	private static final UserIdentityLock USER_IDENTITY_LOCK = new UserIdentityLock();
	
	/**
	 * 
	 */
	public URLContentStorage(){
		super();
	}

	/**
	 * 
	 * @param autoSchedule
	 */
	public URLContentStorage(boolean autoSchedule){
		super(autoSchedule);
	}

	@Override
	public ServiceType getServiceType() {
		return SERVICE_TYPE;
	}

	@Override
	public EnumSet<Capability> getBackendCapabilities() {
		return CAPABILITIES;
	}

	@Override
	public String getTargetUrl(AccessDetails details) {
		return ServiceInitializer.getDAOHandler().getSQLDAO(URLContentDAO.class).getUrl(details.getGuid());
	}

	@Override
	public void removeMetadata(UserIdentity userId, Collection<String> guids) {
		removePhotoMetadata(userId, guids);
		removeVideoMetadata(userId, guids);
	}
	
	/**
	 * 
	 * @param userId
	 * @param guids
	 */
	public void removePhotoMetadata(UserIdentity userId, Collection<String> guids) {
		PhotoDAO photoDAO = ServiceInitializer.getDAOHandler().getSolrDAO(PhotoDAO.class);
		PhotoList photos = photoDAO.getPhotos(null, guids, null, EnumSet.of(SERVICE_TYPE), new long[]{userId.getUserId()});
		if(PhotoList.isEmpty(photos)){
			LOGGER.debug("User, id: "+userId.getUserId()+" has no photos.");
			return;
		}
		List<String> remove = photos.getGUIDs();
		photoDAO.remove(remove);
		ServiceInitializer.getDAOHandler().getSQLDAO(URLContentDAO.class).removeEntries(remove);

		FeedbackTaskBuilder builder = new FeedbackTaskBuilder(TaskType.FEEDBACK); // create builder for deleted photo feedback task
		builder.setUser(userId);
		builder.addDeletedPhotos(DeletedPhotoList.getPhotoList(photos.getPhotos(), photos.getResultInfo()));
		builder.setBackends(getBackends());
		PhotoTaskDetails details = builder.build();
		if(details == null){
			LOGGER.warn("No content.");
		}else{
			if(isAutoSchedule()){
				LOGGER.debug("Scheduling feedback task.");
				CAContentCore.scheduleTask(details);
			}else{
				LOGGER.debug("Auto-schedule is disabled.");
			}

			notifyFeedbackTaskCreated(details);
		}
	}

	/**
	 * 
	 * @param userId
	 * @param guids
	 */
	public void removeVideoMetadata(UserIdentity userId, Collection<String> guids) {
		VideoDAO videoDAO = ServiceInitializer.getDAOHandler().getSolrDAO(VideoDAO.class);
		VideoList videos = videoDAO.getVideos(null, guids, null, EnumSet.of(SERVICE_TYPE), new long[]{userId.getUserId()});
		if(VideoList.isEmpty(videos)){
			LOGGER.debug("User, id: "+userId.getUserId()+" has no videos.");
			return;
		}
		List<String> remove = videos.getGUIDs();
		videoDAO.remove(remove);
		ServiceInitializer.getDAOHandler().getSQLDAO(URLContentDAO.class).removeEntries(remove);

		service.tut.pori.contentanalysis.video.VideoFeedbackTask.FeedbackTaskBuilder builder = new service.tut.pori.contentanalysis.video.VideoFeedbackTask.FeedbackTaskBuilder(TaskType.FEEDBACK); // create builder for deleted video feedback task
		builder.setUser(userId);
		builder.addDeletedVideos(DeletedVideoList.getVideoList(videos.getVideos(), videos.getResultInfo()));
		builder.setBackends(getBackends());
		VideoTaskDetails details = builder.build();
		if(details == null){
			LOGGER.warn("No content.");
		}else{
			if(isAutoSchedule()){
				LOGGER.debug("Scheduling feedback task.");
				VideoContentCore.scheduleTask(details);
			}else{
				LOGGER.debug("Auto-schedule is disabled.");
			}

			notifyFeedbackTaskCreated(details);
		}
	}
	
	/**
	 * this will simply remove all non-existing images
	 */
	@Override
	public boolean synchronizeAccount(UserIdentity userId) {
		USER_IDENTITY_LOCK.acquire(userId);
		LOGGER.debug("Synchronizing account for user, id: "+userId.getUserId());
		try{
			URLContentDAO urlContentDAO = ServiceInitializer.getDAOHandler().getSQLDAO(URLContentDAO.class);
			List<URLEntry> entries = urlContentDAO.getEntries(null, null, null, userId);
			MediaUrlValidator validator = new MediaUrlValidator();
			for(Iterator<URLEntry> iter = entries.iterator(); iter.hasNext();){ // loop through the entries, leave invalid uris in the list
				URLEntry e = iter.next();
				String url = e.getUrl();
				MediaType detectedType = validator.validateUrl(url);
				if(detectedType == MediaType.UNKNOWN){
					LOGGER.debug("Invalid URL detected: "+url);
				}else if(!detectedType.equals(e.getMediaType())){
					LOGGER.warn("Detected type "+detectedType.toInt()+" does not match the stored type "+e.getMediaType().toInt());
				}else{
					iter.remove();			
				}
			}

			if(!entries.isEmpty()){
				LOGGER.debug("Removing invalid URLs.");
				List<String> photoGUIDs = new ArrayList<>();
				List<String> videoGUIDs = new ArrayList<>();
				for(URLEntry e : entries){
					switch(e.getMediaType()){
						case PHOTO:
							photoGUIDs.add(e.getGUID());
							break;
						case VIDEO:
							videoGUIDs.remove(e.getGUID());
							break;
						default:
							throw new UnsupportedOperationException("Unhandeled media type: "+e.getMediaType().name());
					}
				}
				
				if(photoGUIDs.isEmpty()){
					LOGGER.debug("No photos to remove.");
				}else{
					ServiceInitializer.getDAOHandler().getSolrDAO(PhotoDAO.class).remove(photoGUIDs);
					urlContentDAO.removeEntries(photoGUIDs);
					CAContentCore.scheduleTask(
							(new FeedbackTaskBuilder(TaskType.FEEDBACK))
							.setBackends(getBackends())
							.addDeletedPhotos(photoGUIDs)
							.build()
						);
				}
				
				if(videoGUIDs.isEmpty()){
					LOGGER.debug("No videos to remove.");
				}else{
					ServiceInitializer.getDAOHandler().getSolrDAO(VideoDAO.class).remove(videoGUIDs);
					urlContentDAO.removeEntries(videoGUIDs);
					VideoContentCore.scheduleTask(
							(new VideoFeedbackTask.FeedbackTaskBuilder(TaskType.FEEDBACK))
							.setBackends(getBackends())
							.addDeletedVideos(videoGUIDs)
							.build()
						);
				}
			}
		}finally{
			USER_IDENTITY_LOCK.release(userId);
		}
		return true;
	}

	/**
	 * helper method for adding photo URLs
	 * 
	 * @param userId
	 * @param urls list of URLs, validity will NOT be checked
	 * @return the generated task details
	 * @see #addUrls(core.tut.pori.utils.MediaUrlValidator.MediaType, UserIdentity, Collection)
	 */
	private PhotoTaskDetails addPhotoUrls(UserIdentity userId, Collection<String> urls) {
		USER_IDENTITY_LOCK.acquire(userId);
		PhotoList forAnalysis = new PhotoList();
		try{
			for(String url : urls){
				Photo photo = new Photo(null, userId, SERVICE_TYPE, Visibility.PUBLIC);
				photo.setUrl(url);
				forAnalysis.addPhoto(photo);
			}

			PhotoDAO photoDAO = ServiceInitializer.getDAOHandler().getSolrDAO(PhotoDAO.class);
			URLContentDAO urlContentDAO = ServiceInitializer.getDAOHandler().getSQLDAO(URLContentDAO.class);
			List<URLEntry> entries = urlContentDAO.getEntries(null, EnumSet.of(MediaType.PHOTO), urls, userId);
			if(entries == null){ // no previously known URLs
				photoDAO.insert(forAnalysis); // this will generate GUIDs
				for(Photo p : forAnalysis.getPhotos()){
					urlContentDAO.addEntry(new URLEntry(p.getGUID(), MediaType.PHOTO, p.getUrl(), userId));
				}
			}else{ // some or all of the URLs are known
				for(Photo p : forAnalysis.getPhotos()){
					String url = p.getUrl();
					String guid = findGUID(entries, url);
					if(guid == null){ // URL not known previously
						photoDAO.insert(p); // this will generate GUID
						urlContentDAO.addEntry(new URLEntry(p.getGUID(), MediaType.PHOTO, url, userId));
					}else{
						p.setGUID(guid);
					}
				} // for
			}
		} finally {
			USER_IDENTITY_LOCK.release(userId);
		}

		LOGGER.debug("Creating a new analysis task...");
		PhotoTaskDetails details = new PhotoTaskDetails(TaskType.ANALYSIS);
		details.setUserId(userId);
		details.setBackends(getBackends(getBackendCapabilities()));
		details.setPhotoList(forAnalysis);
		details.setTaskParameters(ANALYSIS_PARAMETERS_PHOTO);

		if(isAutoSchedule()){
			LOGGER.debug("Scheduling photo analysis task.");
			CAContentCore.scheduleTask(details);
		}else{
			LOGGER.debug("Auto-schedule is disabled.");
		}

		return details;
	}
	
	/**
	 * helper method for finding GUID matching with the given URL from the given list of entries
	 * 
	 * If there are multiple matches, this will return the first match (first depending on the iteration order of the passed collection)
	 * 
	 * @param entries
	 * @param url
	 * @return GUID or null if not found
	 */
	private String findGUID(Collection<URLEntry> entries, String url){
		if(entries == null){
			LOGGER.warn("Null entries.");
			return null;
		}
		
		for(URLEntry e : entries){
			if(url.equals(e.getUrl())){
				return e.getGUID();
			}
		}
		return url;
	}

	/**
	 * helper method for adding video URLs
	 * 
	 * @param userId
	 * @param urls
	 * @return the generated task details or null if no valid content
	 * @see #addUrls(core.tut.pori.utils.MediaUrlValidator.MediaType, UserIdentity, Collection)
	 */
	private VideoTaskDetails addVideoUrls(UserIdentity userId, Collection<String> urls) {
		USER_IDENTITY_LOCK.acquire(userId);
		VideoList forAnalysis = new VideoList();
		try{
			for(String url : urls){
				Video video = new Video(null, userId, SERVICE_TYPE, Visibility.PUBLIC);
				video.setUrl(url);
				forAnalysis.addVideo(video);
			}

			VideoDAO videoDAO = ServiceInitializer.getDAOHandler().getSolrDAO(VideoDAO.class);
			URLContentDAO urlContentDAO = ServiceInitializer.getDAOHandler().getSQLDAO(URLContentDAO.class);
			List<URLEntry> entries = urlContentDAO.getEntries(null, EnumSet.of(MediaType.VIDEO), urls, userId);
			if(entries == null){ // no previously known urls
				videoDAO.insert(forAnalysis); // this will generate guids
				for(Video v : forAnalysis.getVideos()){
					urlContentDAO.addEntry(new URLEntry(v.getGUID(), MediaType.VIDEO, v.getUrl(), userId));
				}
			}else{ // some or all of the urls are known
				for(Video v : forAnalysis.getVideos()){
					String url = v.getUrl();
					String guid = findGUID(entries, url);
					if(guid == null){ // url not known previously
						videoDAO.insert(v); // this will generate guid
						urlContentDAO.addEntry(new URLEntry(v.getGUID(), MediaType.VIDEO, url, userId));
					}else{
						v.setGUID(guid);
					}
				} // for
			}
		} finally {
			USER_IDENTITY_LOCK.release(userId);
		}

		LOGGER.debug("Creating a new analysis task...");
		VideoTaskDetails details = new VideoTaskDetails(TaskType.ANALYSIS);
		details.setUserId(userId);
		details.setBackends(getBackends(getBackendCapabilities()));
		details.setVideoList(forAnalysis);
		details.setTaskParameters(ANALYSIS_PARAMETERS_VIDEO);

		if(isAutoSchedule()){
			LOGGER.debug("Scheduling video analysis task.");
			VideoContentCore.scheduleTask(details);
		}else{
			LOGGER.debug("Auto-schedule is disabled.");
		}

		return details;
	}

	/**
	 * Add the given list of URLs. 
	 * 
	 * If a given URL already exists in the database, it will not be re-added, but new analysis task for the url will be created and scheduled.
	 * 
	 * Note that this method CANNOT be used to remove previously added (and valid) URLs, which are now invalid. The invalid URLs will simply be ignored.
	 * Use synchronize if you want to clear the database of invalid URLs.
	 * 
	 * @param mediaType 
	 * @param userId
	 * @param urls
	 * @throws IllegalArgumentException on bad input data
	 * @throws UnsupportedOperationException on unsuuported media type
	 * @see #synchronizeAccount(UserIdentity)
	 */
	public void addUrls(MediaType mediaType, UserIdentity userId, Collection<String> urls) throws IllegalArgumentException, UnsupportedOperationException{
		if(urls == null || urls.isEmpty()){
			LOGGER.warn("Empty url list.");
			return;
		}

		AbstractTaskDetails details = null;
		switch(mediaType){
			case PHOTO:
				details = addPhotoUrls(userId, urls);
				break;
			case VIDEO:
				details = addVideoUrls(userId, urls);
				break;
			default:
				throw new UnsupportedOperationException("Unsupported media type: "+mediaType.name());
		}

		if(details == null){
			throw new IllegalArgumentException("No valid content.");
		}

		notifyAnalysisTaskCreated(details);
	}
	
	/**
	 * A URL entry.
	 */
	public static class URLEntry {
		private String _guid = null;
		private MediaType _mediaType = null;
		private String _url = null;
		private UserIdentity _userId = null;
		
		/**
		 * @param guid
		 * @param mediaType
		 * @param url
		 * @param userId
		 */
		public URLEntry(String guid, MediaType mediaType, String url, UserIdentity userId) {
			super();
			_guid = guid;
			_mediaType = mediaType;
			_url = url;
			_userId = userId;
		}
		
		/**
		 * 
		 */
		protected URLEntry(){
			// nothing needed
		}

		/**
		 * @return the guid
		 */
		public String getGUID() {
			return _guid;
		}

		/**
		 * @return the mediaType
		 */
		public MediaType getMediaType() {
			return _mediaType;
		}

		/**
		 * @return the URL
		 */
		public String getUrl() {
			return _url;
		}

		/**
		 * @return the userId
		 */
		public UserIdentity getUserId() {
			return _userId;
		}

		/**
		 * @param guid the GUID to set
		 */
		protected void setGUID(String guid) {
			_guid = guid;
		}

		/**
		 * @param mediaType the mediaType to set
		 */
		protected void setMediaType(MediaType mediaType) {
			_mediaType = mediaType;
		}

		/**
		 * @param url the URL to set
		 */
		protected void setUrl(String url) {
			_url = url;
		}

		/**
		 * @param userId the userId to set
		 */
		protected void setUserId(UserIdentity userId) {
			_userId = userId;
		}
	} // class URLEntry
}
