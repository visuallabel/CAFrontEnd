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
package service.tut.pori.contentstorage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import service.tut.pori.contentanalysis.AccessDetails;
import service.tut.pori.contentanalysis.AnalysisBackend.Capability;
import service.tut.pori.contentanalysis.PhotoParameters;
import service.tut.pori.contentanalysis.PhotoParameters.AnalysisType;
import service.tut.pori.contentanalysis.AsyncTask.TaskType;
import service.tut.pori.contentanalysis.CAContentCore;
import service.tut.pori.contentanalysis.CAContentCore.ServiceType;
import service.tut.pori.contentanalysis.PhotoFeedbackTask.FeedbackTaskBuilder;
import service.tut.pori.contentanalysis.Photo;
import service.tut.pori.contentanalysis.CAContentCore.Visibility;
import service.tut.pori.contentanalysis.PhotoDAO;
import service.tut.pori.contentanalysis.MediaObject.ConfirmationStatus;
import service.tut.pori.contentanalysis.MediaObject.MediaObjectType;
import service.tut.pori.contentanalysis.CAProperties;
import service.tut.pori.contentanalysis.DeletedPhotoList;
import service.tut.pori.contentanalysis.PhotoList;
import service.tut.pori.contentanalysis.PhotoTaskDetails;
import service.tut.pori.contentanalysis.MediaObject;
import service.tut.pori.twitterjazz.TwitterExtractor;
import service.tut.pori.twitterjazz.TwitterExtractor.ContentType;
import service.tut.pori.twitterjazz.TwitterPhotoDescription;
import service.tut.pori.twitterjazz.TwitterProfile;
import service.tut.pori.twitterjazz.TwitterUserDetails;
import core.tut.pori.context.ServiceInitializer;
import core.tut.pori.users.UserIdentity;
import core.tut.pori.utils.MediaUrlValidator.MediaType;
import core.tut.pori.utils.UserIdentityLock;

/**
 * A storage handler, which can be used to retrieve data from Twitter for analysis.
 * 
 * This class is only for photo content, use TwitterJazz if you require summarization support.
 */
public final class TwitterPhotoStorage extends ContentStorage {
	/** 
	 * media object {@link service.tut.pori.contentanalysis.MediaObject.MediaObjectType#METADATA} name for twitter id 
	 * @see service.tut.pori.twitterjazz.TwitterUserDetails#getTwitterId()
	 * @see service.tut.pori.contentanalysis.MediaObject
	 */
	public static final String METADATA_TWITTER_ID = "twitterId";
	/** 
	 * media object {@link service.tut.pori.contentanalysis.MediaObject.MediaObjectType#METADATA} name for twitter screen name 
	 * @see service.tut.pori.twitterjazz.TwitterUserDetails#getScreenName()
	 * @see service.tut.pori.contentanalysis.MediaObject
	 */
	public static final String METADATA_TWITTER_SCREEN_NAME = "twitterScreenName";
	/** Service type declaration for this storage */
	public static final ServiceType SERVICE_TYPE = ServiceType.TWITTER_PHOTO;
	private static final PhotoParameters ANALYSIS_PARAMETERS;
	static{
		ANALYSIS_PARAMETERS = new PhotoParameters();
		ANALYSIS_PARAMETERS.setAnalysisTypes(EnumSet.of(AnalysisType.FACE_DETECTION, AnalysisType.KEYWORD_EXTRACTION, AnalysisType.VISUAL));
	}
	private static final EnumSet<Capability> CAPABILITIES = EnumSet.of(Capability.PHOTO_ANALYSIS);
	private static final Logger LOGGER = Logger.getLogger(TwitterPhotoStorage.class);
	private static final String PREFIX_MEDIA_OBJECT = "twitter_"; // prefix for created metadata objects
	private static final UserIdentityLock USER_IDENTITY_LOCK = new UserIdentityLock();

	/**
	 * Create a Twitter photo storage with default autoschedule options.
	 */
	public TwitterPhotoStorage(){
		super();
	}

	/**
	 * 
	 * @param autoSchedule
	 */
	public TwitterPhotoStorage(boolean autoSchedule){
		super(autoSchedule);
	}
	
	/* (non-Javadoc)
	 * @see service.tut.pori.contentstorage.ContentStorage#getBackendCapabilities()
	 */
	@Override
	public EnumSet<Capability> getBackendCapabilities() {
		return CAPABILITIES;
	}

	/**
	 * 
	 * @param extractor
	 * @return map of entries and photos or null if none available
	 */
	private Map<TwitterEntry, Photo> getTwitterPhotos(TwitterExtractor extractor){
		return getTwitterPhotos(extractor.getUserId(), null, extractor.getProfile(EnumSet.of(ContentType.PHOTO_DESCRIPTIONS)));
	}

	/**
	 * A helper method for converting TwitterPhotoDescriptions to Photo objects
	 * 
	 * @param authenticatedUser
	 * @param map
	 * @param profile
	 * @return the passed map, new map if null was passed, or null if null or empty map was passed AND no new photos were extracted
	 */
	private Map<TwitterEntry, Photo> getTwitterPhotos(UserIdentity authenticatedUser, Map<TwitterEntry, Photo> map, TwitterProfile profile){
		if(profile == null){
			LOGGER.warn("Null profile.");
			return map;
		}

		List<TwitterPhotoDescription> photoDescriptions = profile.getPhotoDescriptions();
		if(photoDescriptions == null){
			LOGGER.debug("No photos found.");
			return map;
		}

		TwitterUserDetails user = profile.getUser();
		Visibility visibility = (user.isProtected() ? Visibility.PRIVATE : Visibility.PUBLIC);
		String twitterId = user.getTwitterId();
		String screenName = user.getScreenName();

		if(map == null){
			map = new HashMap<>(photoDescriptions.size());
		}
		for(Iterator<TwitterPhotoDescription> iter = photoDescriptions.iterator(); iter.hasNext();){
			TwitterPhotoDescription d = iter.next();
			Photo photo = new Photo();
			photo.setVisibility(visibility);
			photo.setOwnerUserId(authenticatedUser);
			photo.setServiceType(SERVICE_TYPE);
			photo.setDescription(d.getDescription());
			Date updated = d.getCreatedTime();
			photo.setUpdated(updated);
			String url = d.getEntityUrl();
			photo.setUrl(url);
			String entityId = d.getEntityId();

			MediaObject object = new MediaObject(MediaType.PHOTO, MediaObjectType.METADATA); // add the origin of the photo as metadata
			object.setVisibility(visibility);
			object.setOwnerUserId(authenticatedUser);
			object.setServiceType(SERVICE_TYPE);
			object.setUpdated(updated);
			object.setObjectId(PREFIX_MEDIA_OBJECT+entityId+"_"+twitterId);
			object.setValue(twitterId);
			object.setName(METADATA_TWITTER_ID);
			object.setConfirmationStatus(ConfirmationStatus.USER_CONFIRMED);
			object.setConfidence(Definitions.DEFAULT_CONFIDENCE);
			object.setRank(Definitions.DEFAULT_RANK);
			photo.addMediaObject(object);

			object = new MediaObject(MediaType.PHOTO, MediaObjectType.METADATA); // add the origin of the photo as metadata
			object.setVisibility(visibility);
			object.setOwnerUserId(authenticatedUser);
			object.setServiceType(SERVICE_TYPE);
			object.setUpdated(updated);
			object.setObjectId(PREFIX_MEDIA_OBJECT+entityId+"_"+screenName);
			object.setValue(screenName);
			object.setName(METADATA_TWITTER_SCREEN_NAME);
			object.setConfirmationStatus(ConfirmationStatus.USER_CONFIRMED);
			object.setConfidence(Definitions.DEFAULT_CONFIDENCE);
			object.setRank(Definitions.DEFAULT_RANK);
			photo.addMediaObject(object);

			map.put(new TwitterEntry(entityId, url, null, screenName, authenticatedUser), photo);
		}  // for

		return map;
	}

	/**
	 * 
	 * @param extractor
	 * @param screenNames list of screen names
	 * @return map of entries and photos or null if none available
	 */
	private Map<TwitterEntry, Photo> getTwitterPhotos(TwitterExtractor extractor, Collection<String> screenNames){
		List<TwitterProfile> profiles = extractor.getProfiles(EnumSet.of(ContentType.PHOTO_DESCRIPTIONS), screenNames.toArray(new String[screenNames.size()]));
		if(profiles == null){
			LOGGER.warn("No profiles found.");
			return null;
		}

		Map<TwitterEntry, Photo> retval = null;
		for(TwitterProfile profile : profiles){
			retval = getTwitterPhotos(extractor.getUserId(), retval, profile);
		}

		return retval;
	}

	@Override
	public String getTargetUrl(AccessDetails details){
		return ServiceInitializer.getDAOHandler().getSQLDAO(TwitterDAO.class).getUrl(details.getGuid());
	}

	@Override
	public void removeMetadata(UserIdentity userId, Collection<String> guids){
		LOGGER.debug("Removing metadata for user, id: "+userId.getUserId());
		PhotoDAO photoDAO = ServiceInitializer.getDAOHandler().getSolrDAO(PhotoDAO.class);
		PhotoList photos = photoDAO.getPhotos(null, guids, null, EnumSet.of(SERVICE_TYPE), new long[]{userId.getUserId()});
		if(PhotoList.isEmpty(photos)){
			LOGGER.debug("User, id: "+userId.getUserId()+" has no photos.");
			return;
		}
		List<String> remove = photos.getGUIDs();
		photoDAO.remove(remove);
		ServiceInitializer.getDAOHandler().getSQLDAO(TwitterDAO.class).removeEntries(remove);

		FeedbackTaskBuilder builder = new FeedbackTaskBuilder(TaskType.FEEDBACK); // create builder for deleted photo feedback task
		builder.setUser(userId);
		builder.setBackends(getBackends());
		builder.addDeletedPhotos(DeletedPhotoList.getPhotoList(photos.getPhotos(), photos.getResultInfo()));
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
	 * Note: the synchronization is only one-way, from Twitter to front-end, 
	 * no information will be transmitted to the other direction.
	 * Also, tags removed from Twitter will NOT be removed from front-end.
	 * 
	 * @param userId
	 * @return true on success
	 */
	@Override
	public boolean synchronizeAccount(UserIdentity userId){
		return synchronizeAccount(userId, null);
	}

	/**
	 * Note: the synchronization is only one-way, from Twitter to front-end, 
	 * no information will be transmitted to the other direction.
	 * Also, tags removed from Twitter will NOT be removed from front-end.
	 * 
	 * Note that if screenNames are given, the synchronization is targeted ONLY to the given screen names. If screenNames are NOT given, the synchronization is targeted to ALL content.
	 * An example use case:
	 * <ul>
	 *  <li>User synchronized his/her account with screenName </i>name1</i>, which in this case is also the name of the user's own Twitter account</li>
	 *  <li>Then, the user re-syncs with different name, <i>name2</i>. The previously used content is left as it is, because the name </i>name1</i> is not given.</li>
	 *  <li>Now, re-syncing with the name <i>name1</i> will ignore all content previously synced with <i>name2</i>, and only synchronize <i>name1</i>. Likewise, using <i>name2</i> would only sync <i>name2</i>, and ignore all content for <i>name1</i>.</li>
	 *  <li>Re-syncing with both <i>name1</i> and <i>name2</> would retrieve content for both accounts.</li>
	 *  <li>Re-syncing without screenNames will default the retrieval to user's own account name (<i>name1</i>), but synchronize ALL content. In practice this means, that the content for <i>name1</i> will be synchronized, and all content for <i>name2</i> will be removed.</li>
	 * </ul>
	 * 
	 * @param userId
	 * @param screenNames use the given collection of screen names instead of the authenticated user's account for synchronization
	 * @return true on success
	 */
	public boolean synchronizeAccount(UserIdentity userId, Collection<String> screenNames){
		USER_IDENTITY_LOCK.acquire(userId);
		LOGGER.debug("Synchronizing account for user, id: "+userId.getUserId());
		try{
			TwitterExtractor extractor = TwitterExtractor.getExtractor(userId);
			if(extractor == null){
				LOGGER.warn("Could not get extractor.");
				return false;
			}

			Map<TwitterEntry, Photo> twitterPhotos = (screenNames == null || screenNames.isEmpty() ? getTwitterPhotos(extractor) : getTwitterPhotos(extractor, screenNames)); // in the end this will contain all the new items
			TwitterDAO twitterDAO = ServiceInitializer.getDAOHandler().getSQLDAO(TwitterDAO.class);

			List<TwitterEntry> existing = twitterDAO.getEntriesByScreenName(screenNames, userId);  // in the end this will contain "lost" items:
			PhotoDAO photoDao = ServiceInitializer.getDAOHandler().getSolrDAO(PhotoDAO.class);
			if(twitterPhotos != null){
				if(existing != null){
					LOGGER.debug("Processing existing photos...");
					List<Photo> updatedPhotos = new ArrayList<>();
					for(Iterator<Entry<TwitterEntry, Photo>> entryIter = twitterPhotos.entrySet().iterator(); entryIter.hasNext();){
						Entry<TwitterEntry, Photo> entry = entryIter.next();
						TwitterEntry twitterEntry = entry.getKey();
						String entityId = twitterEntry.getEntityId();
						for(Iterator<TwitterEntry> existingIter = existing.iterator();existingIter.hasNext();){
							TwitterEntry exEntry = existingIter.next();
							if(exEntry.getEntityId().equals(entityId)){  // already added
								String guid = exEntry.getGUID();
								twitterEntry.setGUID(guid);
								Photo p = entry.getValue();
								p.setGUID(guid);
								updatedPhotos.add(p); // something may have changed
								existingIter.remove();	// remove from existing to prevent deletion
								entryIter.remove();	// remove from entries to prevent duplicate addition
								break;
							}
						}  // for siter
					}  // for
					if(updatedPhotos.size() > 0){
						LOGGER.debug("Updating photo details...");
						photoDao.updatePhotosIfNewer(userId, PhotoList.getPhotoList(updatedPhotos, null));
					}
				}else{
					LOGGER.debug("No existing photos.");
				}

				if(twitterPhotos.isEmpty()){
					LOGGER.debug("No new photos.");
				}else{
					LOGGER.debug("Inserting photos...");
					if(!photoDao.insert(PhotoList.getPhotoList(twitterPhotos.values(), null))){
						LOGGER.error("Failed to add photos to database.");
						return false;
					}

					for(Entry<TwitterEntry, Photo> e : twitterPhotos.entrySet()){	// update entries with correct GUIDs
						e.getKey().setGUID(e.getValue().getGUID());
					}

					LOGGER.debug("Creating photo entries...");
					twitterDAO.createEntries(twitterPhotos.keySet());
				}
			}else{
				LOGGER.debug("No photos retrieved.");
			}

			int taskLimit = ServiceInitializer.getPropertyHandler().getSystemProperties(CAProperties.class).getMaxTaskSize();
			
			int missing = (existing == null ? 0 : existing.size());
			if(missing > 0){  // remove all "lost" items if any
				LOGGER.debug("Deleting removed photos...");
				List<String> guids = new ArrayList<>();
				for(Iterator<TwitterEntry> iter = existing.iterator();iter.hasNext();){
					guids.add(iter.next().getGUID());
				}

				photoDao.remove(guids); // remove photos
				twitterDAO.removeEntries(guids); // remove entries

				FeedbackTaskBuilder builder = new FeedbackTaskBuilder(TaskType.FEEDBACK); // create builder for deleted photo feedback task
				builder.setUser(userId);
				builder.setBackends(getBackends());
				
				if(taskLimit == CAProperties.MAX_TASK_SIZE_DISABLED || missing <= taskLimit){ // if task limit is disabled or there are less photos than the limit
					builder.addDeletedPhotos(guids);
					buildAndNotifyFeedback(builder);
				}else{ // loop to stay below max limit
					List<String> partial = new ArrayList<>(taskLimit);
					int pCount = 0;
					for(String guid : guids){
						if(pCount == taskLimit){
							builder.addDeletedPhotos(guids);
							buildAndNotifyFeedback(builder);
							pCount = 0;
							partial.clear();
							builder.clearDeletedPhotos();
						}
						++pCount;
						partial.add(guid);
					} // for
					if(!partial.isEmpty()){
						builder.addDeletedPhotos(guids);
						buildAndNotifyFeedback(builder);
					}
				} // else
			}

			int twitterPhotoCount = (twitterPhotos == null ? 0 : twitterPhotos.size());
			LOGGER.debug("Added "+twitterPhotoCount+" photos, removed "+missing+" photos for user: "+userId.getUserId());

			if(twitterPhotoCount > 0){
				LOGGER.debug("Creating a new analysis task...");
				PhotoTaskDetails details = new PhotoTaskDetails(TaskType.ANALYSIS);
				details.setUserId(userId);
				details.setBackends(getBackends(getBackendCapabilities()));
				details.setTaskParameters(ANALYSIS_PARAMETERS);
				
				if(taskLimit == CAProperties.MAX_TASK_SIZE_DISABLED || twitterPhotoCount <= taskLimit){ // if task limit is disabled or there are less photos than the limit
					details.setPhotoList(PhotoList.getPhotoList(twitterPhotos.values(), null));
					notifyAnalysis(details);
				}else{ // loop to stay below max limit
					List<Photo> partial = new ArrayList<>(taskLimit);
					PhotoList partialContainer = new PhotoList();
					details.setPhotoList(partialContainer);
					int pCount = 0;
					for(Photo photo : twitterPhotos.values()){
						if(pCount == taskLimit){
							partialContainer.setPhotos(partial);
							notifyAnalysis(details);
							pCount = 0;
							partial.clear();
						}
						++pCount;
						partial.add(photo);
					} // for
					if(!partial.isEmpty()){
						partialContainer.setPhotos(partial);
						notifyAnalysis(details);
					}
				} // else
			}else{
				LOGGER.debug("No new photos, will not create analysis task.");
			}
			return true;
		} finally {
			USER_IDENTITY_LOCK.release(userId);
		}
	}
	
	/**
	 * Helper method for calling notify
	 * 
	 * @param details
	 */
	private void notifyAnalysis(PhotoTaskDetails details){
		details.setTaskId(null);	//make sure the task id is not set for a new task
		if(isAutoSchedule()){
			LOGGER.debug("Scheduling analysis task.");
			CAContentCore.scheduleTask(details);
		}else{
			LOGGER.debug("Auto-schedule is disabled.");
		}

		notifyAnalysisTaskCreated(details);
	}
	
	/**
	 * helper method for building the task and calling notify
	 * 
	 * @param builder
	 */
	private void buildAndNotifyFeedback(FeedbackTaskBuilder builder){
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

	@Override
	public ServiceType getServiceType() {
		return SERVICE_TYPE;
	}

	/**
	 * A class that represents a single Twitter content entry.
	 */
	public static class TwitterEntry{
		private String _entityId = null;
		private String _entityUrl = null;
		private String _guid = null;
		private String _screenName = null;
		private UserIdentity _userId = null;

		/**
		 * 
		 * @param entityId
		 * @param entityUrl
		 * @param guid
		 * @param screenName 
		 * @param userId
		 */
		public TwitterEntry(String  entityId, String entityUrl,  String guid, String screenName, UserIdentity userId){
			_entityId = entityId;
			_entityUrl = entityUrl;
			_guid = guid;
			_screenName = screenName;
			_userId = userId;
		}

		/**
		 * 
		 */
		protected TwitterEntry(){
			// nothing needed
		}

		/**
		 * 
		 * @param guid
		 */
		protected void setGUID(String guid) {
			_guid = guid;
		}

		/**
		 * 
		 * @return entity id
		 */
		public String getEntityId() {
			return _entityId;
		}

		/**
		 * 
		 * @return guid
		 */
		public String getGUID() {
			return _guid;
		}

		/**
		 * @return the entityUrl
		 */
		public String getEntityUrl() {
			return _entityUrl;
		}

		/**
		 * @return the userId
		 */
		public UserIdentity getUserId() {
			return _userId;
		}

		/**
		 * @return the screenName
		 */
		public String getScreenName() {
			return _screenName;
		}

		/**
		 * @param screenName the screenName to set
		 */
		protected void setScreenName(String screenName) {
			_screenName = screenName;
		}

		/**
		 * 
		 * @param userId
		 */
		protected void setUserId(UserIdentity userId) {
			_userId = userId;
		}

		/**
		 * 
		 * @param entityId
		 */
		protected void setEntityId(String entityId) {
			_entityId = entityId;
		}

		/**
		 * 
		 * @param entityUrl
		 */
		protected void setEntityUrl(String entityUrl) {
			_entityUrl = entityUrl;
		}
	} // class TwitterEntry
}
