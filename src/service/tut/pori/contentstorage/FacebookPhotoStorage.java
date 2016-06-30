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
import service.tut.pori.contentanalysis.CAProperties;
import service.tut.pori.contentanalysis.PhotoParameters;
import service.tut.pori.contentanalysis.PhotoParameters.AnalysisType;
import service.tut.pori.contentanalysis.AsyncTask.TaskType;
import service.tut.pori.contentanalysis.CAContentCore;
import service.tut.pori.contentanalysis.CAContentCore.ServiceType;
import service.tut.pori.contentanalysis.PhotoFeedbackTask.FeedbackTaskBuilder;
import service.tut.pori.contentanalysis.Photo;
import service.tut.pori.contentanalysis.CAContentCore.Visibility;
import service.tut.pori.contentanalysis.DeletedPhotoList;
import service.tut.pori.contentanalysis.PhotoDAO;
import service.tut.pori.contentanalysis.PhotoList;
import service.tut.pori.contentanalysis.PhotoTaskDetails;
import service.tut.pori.contentanalysis.MediaObject;
import service.tut.pori.contentanalysis.MediaObject.ConfirmationStatus;
import service.tut.pori.contentanalysis.MediaObject.MediaObjectType;
import service.tut.pori.contentanalysis.MediaObjectList;
import service.tut.pori.facebookjazz.FacebookExtractor;
import service.tut.pori.facebookjazz.FacebookPhotoDescription;
import service.tut.pori.facebookjazz.FacebookPhotoTag;
import core.tut.pori.context.ServiceInitializer;
import core.tut.pori.users.UserIdentity;
import core.tut.pori.utils.MediaUrlValidator.MediaType;
import core.tut.pori.utils.UserIdentityLock;


/**
 * A storage service for retrieving content from Facebook and creating photo analysis and feedback tasks based on the content.
 *
 * This class is only for photo content, use FacebookJazz if you require summarization support.
 */
public final class FacebookPhotoStorage extends ContentStorage {
	/** Service type declaration for this storage */
	public static final ServiceType SERVICE_TYPE = ServiceType.FACEBOOK_PHOTO;
	private static final PhotoParameters ANALYSIS_PARAMETERS;
	static{
		ANALYSIS_PARAMETERS = new PhotoParameters();
		ANALYSIS_PARAMETERS.setAnalysisTypes(EnumSet.of(AnalysisType.FACE_DETECTION, AnalysisType.KEYWORD_EXTRACTION, AnalysisType.VISUAL));
	}
	private static final EnumSet<Capability> CAPABILITIES = EnumSet.of(Capability.PHOTO_ANALYSIS);
	private static final Visibility DEFAULT_VISIBILITY = Visibility.PRIVATE;
	private static final Logger LOGGER = Logger.getLogger(FacebookPhotoStorage.class);
	private static final String PREFIX_VISUAL_OBJECT = "facebook_";
	private static final UserIdentityLock USER_IDENTITY_LOCK = new UserIdentityLock();

	/**
	 * 
	 */
	public FacebookPhotoStorage(){
		super();
	}

	/**
	 * 
	 * @param autoSchedule
	 */
	public FacebookPhotoStorage(boolean autoSchedule){
		super(autoSchedule);
	}

	@Override
	public String getTargetUrl(AccessDetails details){
		return ServiceInitializer.getDAOHandler().getDAO(FacebookDAO.class).getUrl(details.getGuid());
	}

	@Override
	public EnumSet<Capability> getBackendCapabilities() {
		return CAPABILITIES;
	}

	/**
	 * return map of user's photos, the user is taken from the passed extractor object
	 *  
	 * @param extractor
	 * @return list of photos or null if the user has none
	 */
	private Map<FacebookEntry, Photo> getFacebookPhotos(FacebookExtractor extractor){  
		List<FacebookPhotoDescription> photoDescriptions = extractor.getPhotoDescriptions(false, true);
		if(photoDescriptions == null){
			LOGGER.debug("No photos found.");
			return null;
		}

		Map<FacebookEntry, Photo> retval = new HashMap<>(photoDescriptions.size());
		UserIdentity userId = extractor.getUserId();

		for(Iterator<FacebookPhotoDescription> iter = photoDescriptions.iterator(); iter.hasNext();){
			FacebookPhotoDescription d = iter.next();
			Photo photo = new Photo();
			photo.setVisibility(DEFAULT_VISIBILITY);
			photo.setOwnerUserId(userId);
			photo.setServiceType(SERVICE_TYPE);
			photo.setName(d.getDescription());
			Date updated = d.getUpdatedTime();
			photo.setUpdated(updated);
			String url = d.getSource();
			photo.setUrl(url);

			List<FacebookPhotoTag> tags = d.getTagList();
			String photoId = d.getId();
			if(tags != null){
				List<MediaObject> objects = new ArrayList<>(tags.size());
				for(FacebookPhotoTag t : tags){
					MediaObject o = new MediaObject(MediaType.PHOTO, MediaObjectType.KEYWORD);
					o.setConfirmationStatus(ConfirmationStatus.USER_CONFIRMED);
					String value = t.getName();
					o.setValue(value);
					o.setOwnerUserId(userId);
					o.setServiceType(t.getServiceType());
					o.setUpdated(updated);
					o.setVisibility(DEFAULT_VISIBILITY);
					o.setConfidence(Definitions.DEFAULT_CONFIDENCE);
					o.setObjectId(PREFIX_VISUAL_OBJECT+photoId+"_"+value);
					o.setRank(Definitions.DEFAULT_RANK);
					objects.add(o);
				} // for
				photo.setMediaObjects(MediaObjectList.getMediaObjectList(objects, null));
			} // if
			retval.put(new FacebookEntry(null, url, photoId, userId), photo);
		}  // for

		return retval;
	}

	@Override
	public void removeMetadata(UserIdentity userId, Collection<String> guids){
		LOGGER.debug("Removing metadata for user, id: "+userId.getUserId());
		PhotoDAO photoDAO = ServiceInitializer.getDAOHandler().getDAO(PhotoDAO.class);
		PhotoList photos = photoDAO.getPhotos(null, guids, null, EnumSet.of(SERVICE_TYPE), new long[]{userId.getUserId()});
		if(PhotoList.isEmpty(photos)){
			LOGGER.debug("User, id: "+userId.getUserId()+" has no photos.");
			return;
		}
		List<String> remove = photos.getGUIDs();
		photoDAO.remove(remove);
		ServiceInitializer.getDAOHandler().getDAO(FacebookDAO.class).removeEntries(remove);

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
	 * Note: the synchronization is only one-way, from Facebook to front-end, 
	 * no information will be transmitted to the other direction.
	 * Also, tags removed from Facebook will NOT be removed from front-end.
	 * 
	 * @param userId
	 * @return true on success
	 */
	@Override
	public boolean synchronizeAccount(UserIdentity userId){
		USER_IDENTITY_LOCK.acquire(userId);
		LOGGER.debug("Synchronizing account for user, id: "+userId.getUserId());
		try{
			FacebookExtractor extractor = FacebookExtractor.getExtractor(userId);
			if(extractor == null){
				LOGGER.warn("Could not resolve credentials.");
				return false;
			}

			Map<FacebookEntry, Photo> facebookPhotos = getFacebookPhotos(extractor); // in the end this will contain all the new items
			FacebookDAO facebookDAO = ServiceInitializer.getDAOHandler().getDAO(FacebookDAO.class);

			List<FacebookEntry> existing = facebookDAO.getEntries(userId);  // in the end this will contain "lost" items:
			PhotoDAO photoDao = ServiceInitializer.getDAOHandler().getDAO(PhotoDAO.class);
			if(facebookPhotos != null){
				if(existing != null){
					LOGGER.debug("Processing existing photos...");
					List<Photo> updatedPhotos = new ArrayList<>();
					for(Iterator<Entry<FacebookEntry, Photo>> entryIter = facebookPhotos.entrySet().iterator(); entryIter.hasNext();){
						Entry<FacebookEntry, Photo> entry = entryIter.next();
						FacebookEntry facebookEntry = entry.getKey();
						String objectId = facebookEntry.getObjectId();
						for(Iterator<FacebookEntry> existingIter = existing.iterator();existingIter.hasNext();){
							FacebookEntry exEntry = existingIter.next();
							if(exEntry.getObjectId().equals(objectId)){  // already added
								String guid = exEntry.getGUID();
								facebookEntry.setGUID(guid);
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

				if(facebookPhotos.isEmpty()){
					LOGGER.debug("No new photos.");
				}else{
					LOGGER.debug("Inserting photos...");
					if(!photoDao.insert(PhotoList.getPhotoList(facebookPhotos.values(), null))){
						LOGGER.error("Failed to add photos to database.");
						return false;
					}

					for(Entry<FacebookEntry, Photo> e : facebookPhotos.entrySet()){	// update entries with correct guids
						e.getKey().setGUID(e.getValue().getGUID());
					}

					LOGGER.debug("Creating photo entries...");
					facebookDAO.createEntries(facebookPhotos.keySet());
				}
			}else{
				LOGGER.debug("No photos retrieved.");
			}
			int taskLimit = ServiceInitializer.getPropertyHandler().getSystemProperties(CAProperties.class).getMaxTaskSize();
			
			int missing = (existing == null ? 0 : existing.size());
			if(missing > 0){  // remove all "lost" items if any
				LOGGER.debug("Deleting removed photos...");
				List<String> guids = new ArrayList<>();
				for(Iterator<FacebookEntry> iter = existing.iterator();iter.hasNext();){
					guids.add(iter.next().getGUID());
				}

				photoDao.remove(guids); // remove photos
				facebookDAO.removeEntries(guids); // remove entries

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

			int facebookPhotoCount = (facebookPhotos == null ? 0 : facebookPhotos.size());
			LOGGER.debug("Added "+facebookPhotoCount+" photos, removed "+missing+" photos for user: "+userId.getUserId());

			if(facebookPhotoCount > 0){
				LOGGER.debug("Creating a new analysis task...");
				PhotoTaskDetails details = new PhotoTaskDetails(TaskType.ANALYSIS);
				details.setUserId(userId);
				details.setBackends(getBackends(getBackendCapabilities()));
				details.setTaskParameters(ANALYSIS_PARAMETERS);
				
				if(taskLimit == CAProperties.MAX_TASK_SIZE_DISABLED || facebookPhotoCount <= taskLimit){ // if task limit is disabled or there are less photos than the limit
					details.setPhotoList(PhotoList.getPhotoList(facebookPhotos.values(), null));
					notifyAnalysis(details);
				}else{ // loop to stay below max limit
					List<Photo> partial = new ArrayList<>(taskLimit);
					PhotoList partialContainer = new PhotoList();
					details.setPhotoList(partialContainer);
					int pCount = 0;
					for(Photo photo : facebookPhotos.values()){
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
	 * Represents a single Facebook content entry.
	 *
	 */
	public static class FacebookEntry {
		private String _guid = null;
		private String _staticUrl = null;
		private String _objectId = null;
		private UserIdentity _userId = null;

		/**
		 * 
		 * @param guid
		 * @param staticUrl
		 * @param objectId
		 * @param userId
		 */
		public FacebookEntry(String guid, String staticUrl, String objectId, UserIdentity userId) {
			_guid = guid;
			_staticUrl = staticUrl;
			_objectId = objectId;
			_userId = userId;
		}

		/**
		 * 
		 */
		protected FacebookEntry(){
			// nothing needed
		}

		/**
		 * @return the guid
		 */
		public String getGUID() {
			return _guid;
		}

		/**
		 * @return the staticUrl
		 */
		public String getStaticUrl() {
			return _staticUrl;
		}

		/**
		 * @return the objectId
		 */
		public String getObjectId() {
			return _objectId;
		}

		/**
		 * @return the userId
		 */
		public UserIdentity getUserId() {
			return _userId;
		}

		/**
		 * @param guid the guid to set
		 */
		protected void setGUID(String guid) {
			_guid = guid;
		}

		/**
		 * @param staticUrl the staticUrl to set
		 */
		protected void setStaticUrl(String staticUrl) {
			_staticUrl = staticUrl;
		}

		/**
		 * @param objectId the objectId to set
		 */
		protected void setObjectId(String objectId) {
			_objectId = objectId;
		}

		/**
		 * @param userId the userId to set
		 */
		protected void setUserId(UserIdentity userId) {
			_userId = userId;
		}
	} // class FacebookEntry
}
