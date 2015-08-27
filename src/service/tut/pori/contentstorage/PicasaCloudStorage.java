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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import service.tut.pori.contentanalysis.AccessDetails;
import service.tut.pori.contentanalysis.AnalysisBackend.Capability;
import service.tut.pori.contentanalysis.CAProperties;
import service.tut.pori.contentanalysis.PhotoParameters;
import service.tut.pori.contentanalysis.PhotoParameters.AnalysisType;
import service.tut.pori.contentanalysis.CAContentCore;
import service.tut.pori.contentanalysis.AsyncTask.TaskType;
import service.tut.pori.contentanalysis.CAContentCore.ServiceType;
import service.tut.pori.contentanalysis.PhotoFeedbackTask.FeedbackTaskBuilder;
import service.tut.pori.contentanalysis.PhotoDAO;
import service.tut.pori.contentanalysis.Photo;
import service.tut.pori.contentanalysis.CAContentCore.Visibility;
import service.tut.pori.contentanalysis.DeletedPhotoList;
import service.tut.pori.contentanalysis.PhotoList;
import service.tut.pori.contentanalysis.PhotoTaskDetails;
import service.tut.pori.contentanalysis.MediaObject;
import service.tut.pori.contentanalysis.MediaObject.ConfirmationStatus;
import service.tut.pori.contentanalysis.MediaObject.MediaObjectType;
import service.tut.pori.contentanalysis.MediaObjectList;
import service.tut.pori.contentstorage.PicasawebClient.PhotoEntry;
import service.tut.pori.users.google.GoogleCredential;
import service.tut.pori.users.google.GoogleUserCore;
import core.tut.pori.context.ServiceInitializer;
import core.tut.pori.users.UserIdentity;
import core.tut.pori.utils.MediaUrlValidator.MediaType;
import core.tut.pori.utils.UserIdentityLock;


/**
 * A storage handler for retrieving content from Picasa and creating photo analysis and feedback tasks based on the retrieved content.
 *
 */
public final class PicasaCloudStorage extends ContentStorage {
	/** Service type declaration for this storage */
	public static final ServiceType SERVICE_TYPE = ServiceType.PICASA_STORAGE_SERVICE;
	private static final String ALBUM_ACCESS_PUBLIC = "public";
	private static final PhotoParameters ANALYSIS_PARAMETERS;
	static{
		ANALYSIS_PARAMETERS = new PhotoParameters();
		ANALYSIS_PARAMETERS.setAnalysisTypes(EnumSet.of(AnalysisType.FACE_DETECTION, AnalysisType.KEYWORD_EXTRACTION, AnalysisType.VISUAL));
	}
	private static final EnumSet<Capability> CAPABILITIES = EnumSet.of(Capability.PHOTO_ANALYSIS);
	private static final Logger LOGGER = Logger.getLogger(PicasaCloudStorage.class);
	private static final String PREFIX_VISUAL_OBJECT = "picasa_";
	private static final UserIdentityLock USER_IDENTITY_LOCK = new UserIdentityLock();

	/**
	 * 
	 */
	public PicasaCloudStorage(){
		super();
	}

	/**
	 * 
	 * @param autoSchedule
	 */
	public PicasaCloudStorage(boolean autoSchedule){
		super(autoSchedule);
	}

	@Override
	public EnumSet<Capability> getBackendCapabilities() {
		return CAPABILITIES;
	}

	@Override
	public String getTargetUrl(AccessDetails details){
		String guid = details.getGuid();
		PicasaDAO picasaDAO = ServiceInitializer.getDAOHandler().getSQLDAO(PicasaDAO.class);
		PicasaEntry entry = picasaDAO.getEntry(guid);
		if(entry == null){
			LOGGER.debug("No entry for GUID: "+guid);
			return null;
		}
		String url = entry.getStaticUrl();
		if(url != null){ // return static URL if it is known
			return url;
		}
		LOGGER.debug("No static URL known, trying to resolve...");

		GoogleCredential gc = GoogleUserCore.getCredential(details.getOwner());
		if(gc == null){
			LOGGER.warn("Failed to resolve Google credentials.");
			return null;
		}
		try(PicasawebClient client = new PicasawebClient(gc)){
			url = client.generateStaticUrl(entry.getAlbumId(), entry.getPhotoId());
		} catch (IllegalArgumentException | IOException ex) {
			LOGGER.error(ex, ex);
		}
		if(url == null){
			LOGGER.warn("Could not resolve url.");
		}else{ // store the URL 
			LOGGER.debug("URL resolved, updating entries...");
			entry.setStaticUrl(url);
			picasaDAO.updateEntry(entry);
		}
		return url;
	}

	/**
	 * return map of user's photos, the user is taken from the passed client object
	 *  
	 * @param client
	 * @return list of photos or null if the user has none
	 */
	private Map<PicasaEntry, Photo> getPicasaPhotos(PicasawebClient client){  
		List<PhotoEntry> photos = client.getPhotos();
		if(photos == null){
			LOGGER.debug("No photos found.");
			return null;
		}

		Map<PicasaEntry, Photo> retval = new HashMap<>(photos.size());
		UserIdentity userId = client.getUserIdentity();
		String googleUserId = client.getGoogleUserId();

		for(Iterator<PhotoEntry> iter = photos.iterator(); iter.hasNext();){
			PhotoEntry e = iter.next();
			Photo photo = new Photo();
			Visibility visibility = null;
			if(ALBUM_ACCESS_PUBLIC.equalsIgnoreCase(e.getAlbumAccess())){
				visibility = Visibility.PUBLIC;
			}else{
				visibility = Visibility.PRIVATE;
			}
			photo.setVisibility(visibility);
			photo.setOwnerUserId(userId);
			photo.setServiceType(SERVICE_TYPE);

			String temp = e.getSummary();
			if(!StringUtils.isBlank(temp)){
				photo.setDescription(temp);
			}
			temp = e.getTitle();
			if(!StringUtils.isBlank(temp)){
				photo.setName(temp);
			}

			Date updated = e.getUpdated();
			photo.setUpdated(updated);

			String photoId = e.getGphotoId();
			List<String> mk = e.getKeywords();
			if(mk != null){
				List<MediaObject> objects = new ArrayList<>();
				for(Iterator<String> kiter = mk.iterator(); kiter.hasNext(); ){
					MediaObject o = new MediaObject(MediaType.PHOTO, MediaObjectType.KEYWORD);
					o.setConfirmationStatus(ConfirmationStatus.USER_CONFIRMED);
					String value = kiter.next();
					o.setValue(value);
					o.setOwnerUserId(userId);
					o.setServiceType(SERVICE_TYPE);
					o.setUpdated(updated);
					o.setVisibility(visibility);
					o.setConfidence(Definitions.DEFAULT_CONFIDENCE);
					o.setObjectId(PREFIX_VISUAL_OBJECT+photoId+"_"+value);
					o.setRank(Definitions.DEFAULT_RANK);
					objects.add(o);
				} // for
				photo.setMediaObjects(MediaObjectList.getMediaObjectList(objects, null));
			}

			String url = e.getUrl();
			photo.setUrl(url);

			retval.put(new PicasaEntry(null, e.getAlbumId(), photoId, googleUserId, url), photo);
		}  // for

		return (retval.isEmpty() ? null : retval);
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
		ServiceInitializer.getDAOHandler().getSQLDAO(PicasaDAO.class).removeEntries(remove);

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
	 * Note: the synchronization is only one-way, from Picasa to front-end, 
	 * no information will be transmitted to the other direction.
	 * Also, tags removed from Picasa will NOT be removed from front-end.
	 * 
	 * @param userId
	 * @return true on success
	 */
	@Override
	public boolean synchronizeAccount(UserIdentity userId){
		USER_IDENTITY_LOCK.acquire(userId);
		LOGGER.debug("Synchronizing account for user, id: "+userId.getUserId());
		try{
			GoogleCredential gc = GoogleUserCore.getCredential(userId);
			if(gc == null){
				LOGGER.warn("Could not resolve credentials.");
				return false;
			}
			Map<PicasaEntry, Photo> picasaPhotos = null;
			try(PicasawebClient client = new PicasawebClient(gc)){
				picasaPhotos = getPicasaPhotos(client); // in the end this will contain all the new items
			} catch (IllegalArgumentException | IOException ex) {
				LOGGER.error(ex, ex);
				return false;
			}
			PicasaDAO picasaDAO = ServiceInitializer.getDAOHandler().getSQLDAO(PicasaDAO.class);

			List<PicasaEntry> existing = picasaDAO.getEntries(gc.getId());  // in the end this will contain "lost" items:
			PhotoDAO photoDao = ServiceInitializer.getDAOHandler().getSolrDAO(PhotoDAO.class);
			if(picasaPhotos != null){
				if(existing != null){
					LOGGER.debug("Processing existing photos...");
					List<Photo> updatedPhotos = new ArrayList<>();
					List<PicasaEntry> updatedEntries = new ArrayList<>();
					for(Iterator<Entry<PicasaEntry, Photo>> entryIter = picasaPhotos.entrySet().iterator(); entryIter.hasNext();){
						Entry<PicasaEntry, Photo> entry = entryIter.next();
						PicasaEntry picasaEntry = entry.getKey();
						String photoId = picasaEntry.getPhotoId();
						for(Iterator<PicasaEntry> existingIter = existing.iterator();existingIter.hasNext();){
							PicasaEntry exEntry = existingIter.next();
							if(exEntry.getPhotoId().equals(photoId)){  // already added
								String guid = exEntry.getGUID();
								picasaEntry.setGUID(guid);
								if(!exEntry.getAlbumId().equals(picasaEntry.getAlbumId())){ // album changed
									updatedEntries.add(picasaEntry);	// update the entry
								}
								Photo p = entry.getValue();
								p.setGUID(guid);
								updatedPhotos.add(p); // something in addition to albumId may have changed
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

					if(updatedEntries.size() > 0){
						LOGGER.debug("Updating photo entries...");
						picasaDAO.updateEntries(updatedEntries);
					}
				}else{
					LOGGER.debug("No existing photos.");
				}

				if(picasaPhotos.isEmpty()){
					LOGGER.debug("No new photos.");
				}else{
					LOGGER.debug("Inserting photos...");
					if(!photoDao.insert(PhotoList.getPhotoList(picasaPhotos.values(), null))){
						LOGGER.error("Failed to add photos to database.");
						return false;
					}

					for(Entry<PicasaEntry, Photo> e : picasaPhotos.entrySet()){	// update entries with correct guids
						e.getKey().setGUID(e.getValue().getGUID());
					}

					LOGGER.debug("Creating photo entries...");
					picasaDAO.createEntries(picasaPhotos.keySet());
				}
			}else{
				LOGGER.debug("No photos retrieved.");
			}
			
			int taskLimit = ServiceInitializer.getPropertyHandler().getSystemProperties(CAProperties.class).getMaxTaskSize();

			int missing = (existing == null ? 0 : existing.size());
			if(missing > 0){  // remove all "lost" items if any
				LOGGER.debug("Deleting removed photos...");
				List<String> guids = new ArrayList<>();
				for(Iterator<PicasaEntry> iter = existing.iterator();iter.hasNext();){
					guids.add(iter.next().getGUID());
				}

				photoDao.remove(guids); // remove photos
				picasaDAO.removeEntries(guids); // remove entries

				FeedbackTaskBuilder builder = new FeedbackTaskBuilder(TaskType.FEEDBACK);
				builder.setUser(userId); // create builder for deleted photo feedback task
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

			int picasaPhotoCount = (picasaPhotos == null ? 0 : picasaPhotos.size());
			LOGGER.debug("Added "+picasaPhotoCount+" photos, removed "+missing+" photos for user: "+userId.getUserId());

			if(picasaPhotoCount > 0){
				LOGGER.debug("Creating a new analysis task...");
				PhotoTaskDetails details = new PhotoTaskDetails(TaskType.ANALYSIS);
				details.setUserId(userId);
				details.setBackends(getBackends(getBackendCapabilities()));
				details.setTaskParameters(ANALYSIS_PARAMETERS);
				
				if(taskLimit == CAProperties.MAX_TASK_SIZE_DISABLED || picasaPhotoCount <= taskLimit){ // if task limit is disabled or there are less photos than the limit
					details.setPhotoList(PhotoList.getPhotoList(picasaPhotos.values(), null));
					notifyAnalysis(details);
				}else{ // loop to stay below max limit
					List<Photo> partial = new ArrayList<>(taskLimit);
					PhotoList partialContainer = new PhotoList();
					details.setPhotoList(partialContainer);
					int pCount = 0;
					for(Photo photo : picasaPhotos.values()){
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
	 * Represents a single picasa content entry
	 */
	public static class PicasaEntry{
		private String _guid = null;
		private String _albumId = null;
		private String _photoId = null;
		private String _googleUserId = null;
		private String _staticUrl = null;

		/**
		 * @param guid
		 * @param albumId
		 * @param photoId
		 * @param googleUserId
		 * @param staticUrl
		 */
		public PicasaEntry(String guid, String albumId, String photoId, String googleUserId, String staticUrl) {
			_guid = guid;
			_albumId = albumId;
			_photoId = photoId;
			_googleUserId = googleUserId;
			_staticUrl = staticUrl;
		}

		/**
		 * 
		 */
		protected PicasaEntry(){
			// nothing needed
		}

		/**
		 * @return the guid
		 */
		public String getGUID() {
			return _guid;
		}

		/**
		 * @return the albumId
		 */
		public String getAlbumId() {
			return _albumId;
		}

		/**
		 * @return the photoId
		 */
		public String getPhotoId() {
			return _photoId;
		}

		/**
		 * @return the googleUserId
		 */
		public String getGoogleUserId() {
			return _googleUserId;
		}

		/**
		 * @param guid the guid to set
		 */
		public void setGUID(String guid) {
			_guid = guid;
		}

		/**
		 * @param albumId the albumId to set
		 */
		public void setAlbumId(String albumId) {
			_albumId = albumId;
		}

		/**
		 * @param photoId the photoId to set
		 */
		public void setPhotoId(String photoId) {
			_photoId = photoId;
		}

		/**
		 * @param googleUserId the googleUserId to set
		 */
		public void setGoogleUserId(String googleUserId) {
			_googleUserId = googleUserId;
		}

		/**
		 * @return the staticUrl
		 */
		public String getStaticUrl() {
			return _staticUrl;
		}

		/**
		 * @param staticUrl the staticUrl to set
		 */
		public void setStaticUrl(String staticUrl) {
			_staticUrl = staticUrl;
		}
	} // class PicasaEntry
}
