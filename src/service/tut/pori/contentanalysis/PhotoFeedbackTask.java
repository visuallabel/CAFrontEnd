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

import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import service.tut.pori.contentanalysis.AnalysisBackend.Capability;
import core.tut.pori.context.ServiceInitializer;
import core.tut.pori.users.UserIdentity;


/**
 * An implementation of ASyncTask, meant for executing a feedback task.
 * 
 * Requires a valid taskId for execution, provided in a JobExecutionContext.
 * 
 */
public class PhotoFeedbackTask extends AsyncTask{
	private static final Logger LOGGER = Logger.getLogger(PhotoFeedbackTask.class);
	
	/**
	 * 
	 * @param response
	 * @throws IllegalArgumentException
	 */
	public static void taskFinished(PhotoTaskResponse response) throws IllegalArgumentException{
		Integer backendId = response.getBackendId();
		Long taskId = response.getTaskId();

		PhotoTaskDAO taskDAO = ServiceInitializer.getDAOHandler().getSQLDAO(PhotoTaskDAO.class);
		BackendStatus taskStatus = taskDAO.getBackendStatus(backendId, taskId);
		if(taskStatus == null){
			LOGGER.warn("Backend, id: "+backendId+" returned results for task, not given to the backend. TaskId: "+taskId);
			throw new IllegalArgumentException("This task is not given for backend, id: "+backendId);
		}

		TaskStatus status = response.getStatus();
		if(status == null){
			LOGGER.warn("Task status not available.");
			status = TaskStatus.UNKNOWN;
		}
		taskStatus.setStatus(status);

		try{
			PhotoList results = response.getPhotoList();
			if(PhotoList.isEmpty(results)){
				LOGGER.warn("No results returned by the backendId: "+backendId);
				return;
			}

			if(!PhotoList.isValid(results)){
				LOGGER.warn("Invalid photoList.");
			}

			PhotoDAO photoDAO = ServiceInitializer.getDAOHandler().getSolrDAO(PhotoDAO.class);
			if(!photoDAO.setOwners(results)){
				LOGGER.warn("Could not get owner information for all photos.");
			}

			PhotoList associations = new PhotoList();
			MediaObjectList insert = new MediaObjectList();
			MediaObjectList update = new MediaObjectList();
			MediaObjectDAO vdao = ServiceInitializer.getDAOHandler().getSolrDAO(MediaObjectDAO.class);
			for(Iterator<Photo> photoIter = results.getPhotos().iterator(); photoIter.hasNext();){
				Photo photo = photoIter.next();
				String guid = photo.getGUID();
				UserIdentity userId = photo.getOwnerUserId();
				if(!UserIdentity.isValid(userId)){  // if this photo does not exist, there won't be userId
					LOGGER.warn("Ignoring non-existing photo, GUID: "+guid+" from backend, id: "+backendId);
					continue;
				}
				BackendStatusList c = photo.getBackendStatus();
				if(BackendStatusList.isEmpty(c)){
					LOGGER.debug("Backend status not available for photo, GUID: "+guid);
				}else if(c.getCombinedStatus() == TaskStatus.ERROR){
					LOGGER.warn("Error condition detected for photo, GUID: "+guid);
				}else{
					List<BackendStatus> sList = c.getBackendStatuses();
					if(sList.size() > 1){
						status = TaskStatus.ERROR;
						throw new IllegalArgumentException("Multiple backend statuses.");
					}
					if(!backendId.equals(sList.get(0).getBackendId())){
						status = TaskStatus.ERROR;
						throw new IllegalArgumentException("Invalid backend status.");
					}
				}
				MediaObjectList vObjects = photo.getMediaObjects();
				if(!MediaObjectList.isEmpty(vObjects)){  // make sure all objects have proper user
					for(MediaObject mediaObject : vObjects.getMediaObjects()){ // check that the objects are valid
						if(!backendId.equals(mediaObject.getBackendId())){
							LOGGER.warn("Task backend id "+backendId+" does not match the backend id "+mediaObject.getBackendId()+" given for media object, objectId: "+mediaObject.getObjectId());
							mediaObject.setBackendId(backendId);
						}
						mediaObject.setOwnerUserId(userId);
					}
					vdao.resolveObjectIds(vObjects); // resolve ids for update/insert sort
					Photo iPhoto = null;
					for(MediaObject vo : vObjects.getMediaObjects()){ // re-sort to to updated and new
						if(StringUtils.isBlank(vo.getMediaObjectId())){ // no media object id, this is a new one
							if(iPhoto == null){
								associations.getPhoto(guid); // get target photo for insertion
								if(iPhoto == null){
									iPhoto = new Photo(guid);
									associations.addPhoto(iPhoto);
								}
							}				
							iPhoto.addMediaObject(vo);
							insert.addMediaObject(vo);
						}else{
							update.addMediaObject(vo);
						}
					} // for
				} // for objects
			}

			if(MediaObjectList.isEmpty(insert)){
				LOGGER.debug("Nothing to insert.");
			}else if(!MediaObjectList.isValid(insert)){
				status = TaskStatus.ERROR;
				throw new IllegalArgumentException("Invalid media object list.");
			}else if(!photoDAO.insert(insert)){
				LOGGER.warn("Failed to insert new objects.");		
			}else{
				photoDAO.associate(associations);
			}

			if(MediaObjectList.isEmpty(update)){
				LOGGER.debug("Nothing to update");
			}else if(!MediaObjectList.isValid(update)){
				status = TaskStatus.ERROR;
				throw new IllegalArgumentException("Invalid media object list.");
			}else if(!photoDAO.update(update)){
				LOGGER.warn("Failed to update objects.");
			}

			taskDAO.updateMediaStatus(results.getPhotos(), taskId);
			taskDAO.updateTaskStatus(taskStatus, taskId);
		} finally {
			ServiceInitializer.getEventHandler().publishEvent(new AsyncTaskEvent(backendId, PhotoFeedbackTask.class, status, taskId, TaskType.FEEDBACK));
		}
	}

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		executeAddTask(EnumSet.of(Capability.PHOTO_ANALYSIS, Capability.USER_FEEDBACK), ServiceInitializer.getDAOHandler().getSQLDAO(PhotoTaskDAO.class), getTaskId(context.getMergedJobDataMap()));
	}
	
	/**
	 * A helper class building PhotoTaskDetails usable with {@link PhotoFeedbackTask} and executable using {@link service.tut.pori.contentanalysis.CAContentCore#scheduleTask(PhotoTaskDetails)}}
	 * @see service.tut.pori.contentanalysis.CAContentCore
	 * @see service.tut.pori.contentanalysis.PhotoTaskDetails
	 */
	public static class FeedbackTaskBuilder{
		private PhotoTaskDetails _details = null;
		
		/**
		 * for sub-classing
		 */
		protected FeedbackTaskBuilder(){
			// nothing needed
		}
		
		/**
		 * 
		 * @param taskType {@link service.tut.pori.contentanalysis.AsyncTask.TaskType#FEEDBACK}
		 * @throws IllegalArgumentException on unsupported/invalid task type
		 */
		public FeedbackTaskBuilder(TaskType taskType) throws IllegalArgumentException {
			if(taskType != TaskType.FEEDBACK){
				throw new IllegalArgumentException("Invalid task type.");
			}
			_details = new PhotoTaskDetails(taskType);
		}
		
		/**
		 * Add photo to feedback task if the given photo has (valid) changes
		 * 
		 * @param photo
		 * @return this
		 */
		public FeedbackTaskBuilder addPhoto(Photo photo){
			if(photo == null){
				LOGGER.warn("Ignored null photo.");
			}else{
				_details.addPhoto(photo);
			}
			return this;
		}
		
		/**
		 * 
		 * @param photos
		 * @return this
		 */
		public FeedbackTaskBuilder addPhotos(PhotoList photos){
			if(PhotoList.isEmpty(photos)){
				LOGGER.warn("Ignored empty photo list.");
			}else{
				for(Photo p : photos.getPhotos()){
					addPhoto(p);
				}
			}
			return this;
		}
		
		/**
		 * Set this photo as the reference photo
		 * 
		 * @param photo
		 * @return this
		 * @throws IllegalArgumentException if the given photo is already present in similar or dissimilar photo list
		 */
		public FeedbackTaskBuilder addReferencePhoto(Photo photo) throws IllegalArgumentException{
			if(photo == null){
				LOGGER.warn("Ignored null photo.");
				return this;
			}
			String guid = photo.getGUID();
			if(StringUtils.isEmpty(guid)){
				throw new IllegalArgumentException("No GUID for the given photo.");
			}
			
			SimilarPhotoList similar = _details.getSimilarPhotoList();
			if(!PhotoList.isEmpty(similar) && similar.getPhoto(guid) != null){
				throw new IllegalArgumentException("Same photo cannot appear in reference list and similar photo list.");
			}
			DissimilarPhotoList dissimilar = _details.getDissimilarPhotoList();
			if(!PhotoList.isEmpty(dissimilar) && dissimilar.getPhoto(guid) != null){
				throw new IllegalArgumentException("Same photo cannot appear in reference list and dissimilar photo list.");
			}
			_details.addReferencePhoto(photo);
			return this;
		}
		
		/**
		 * 
		 * @param photo
		 * @return this
		 * @throws IllegalArgumentException if the given photo is already present in reference or dissimilar photo list
		 */
		public FeedbackTaskBuilder addSimilarPhoto(Photo photo) throws IllegalArgumentException{
			if(photo == null){
				LOGGER.warn("Ignored null photo.");
				return this;
			}
			
			String guid = photo.getGUID();
			if(StringUtils.isEmpty(guid)){
				throw new IllegalArgumentException("No GUID for the given photo.");
			}
			
			ReferencePhotoList references = _details.getReferencePhotoList();
			if(!PhotoList.isEmpty(references) && references.getPhoto(guid) != null){
				throw new IllegalArgumentException("Same photo cannot appear in reference list and similar photo list.");
			}
			DissimilarPhotoList dissimilar = _details.getDissimilarPhotoList();
			if(!PhotoList.isEmpty(dissimilar) && dissimilar.getPhoto(guid) != null){
				throw new IllegalArgumentException("Same photo cannot appear in similar list and dissimilar photo list.");
			}
			_details.addSimilarPhoto(photo);
			return this;
		}
		
		/**
		 * 
		 * @param photo
		 * @return this
		 * @throws IllegalArgumentException if the given photo is already present in reference or similar photo list
		 */
		public FeedbackTaskBuilder addDissimilarPhoto(Photo photo) throws IllegalArgumentException{
			if(photo == null){
				LOGGER.warn("Ignored invalid photo.");
				return this;
			}
			
			String guid = photo.getGUID();
			if(StringUtils.isEmpty(guid)){
				throw new IllegalArgumentException("No GUID for the given photo.");
			}
			
			ReferencePhotoList references = _details.getReferencePhotoList();
			if(!PhotoList.isEmpty(references) && references.getPhoto(guid) != null){
				throw new IllegalArgumentException("Same photo cannot appear in reference list and similar photo list.");
			}
			SimilarPhotoList similar = _details.getSimilarPhotoList();
			if(!PhotoList.isEmpty(similar) && similar.getPhoto(guid) != null){
				throw new IllegalArgumentException("Same photo cannot appear in similar list and dissimilar photo list.");
			}
			_details.addDissimilarPhoto(photo);
			return this;
		}
		
		/**
		 * 
		 * @param photo
		 * @return this
		 * @throws IllegalArgumentException
		 */
		public FeedbackTaskBuilder addDeletedPhoto(Photo photo) throws IllegalArgumentException{
			if(photo == null){
				LOGGER.warn("Ignored null photo.");
				return this;
			}else if(StringUtils.isBlank(photo.getGUID())){
				throw new IllegalArgumentException("No GUID.");
			}
			_details.addDeletedPhoto(photo);
			return this;
		}
		
		/**
		 * 
		 * @param guids
		 * @return this
		 */
		public FeedbackTaskBuilder addDeletedPhotos(Collection<String> guids){
			if(guids == null || guids.isEmpty()){
				LOGGER.warn("Ignored empty deleted photo list.");
				return this;
			}
			for(String guid : guids){
				addDeletedPhoto(new Photo(guid));
			}
			return this;
		}
		
		/**
		 * 
		 * @param photos
		 * @return this
		 */
		public FeedbackTaskBuilder addDeletedPhotos(DeletedPhotoList photos){
			if(DeletedPhotoList.isEmpty(photos)){
				LOGGER.warn("Ignored empty deleted photo list.");
				return this;
			}
			DeletedPhotoList deleted = _details.getDeletedPhotoList();
			if(DeletedPhotoList.isEmpty(deleted)){
				_details.setDeletedPhotoList(photos);
			}else{
				deleted.addPhotos(photos);
			}
			return this;
		}
		
		/**
		 * 
		 * @param photos
		 * @return this
		 */
		public FeedbackTaskBuilder addDissimilarPhotos(SimilarPhotoList photos){
			if(SimilarPhotoList.isEmpty(photos)){
				LOGGER.warn("Ignored empty similar photo list.");
				return this;
			}
			for(Photo photo : photos.getPhotos()){
				addSimilarPhoto(photo);
			}
			return this;
		}
		
		/**
		 * 
		 * @param photos
		 * @return this
		 */
		public FeedbackTaskBuilder addSimilarPhotos(DissimilarPhotoList photos){
			if(DissimilarPhotoList.isEmpty(photos)){
				LOGGER.warn("Ignored empty dissimilar photo list.");
				return this;
			}
			for(Photo photo : photos.getPhotos()){
				addDissimilarPhoto(photo);
			}
			return this;
		}
		
		/**
		 * 
		 * @param userId
		 * @return this
		 */
		public FeedbackTaskBuilder setUser(UserIdentity userId){
			_details.setUserId(userId);
			return this;
		}
		
		/**
		 * 
		 * @param confidence
		 * @return this
		 */
		public FeedbackTaskBuilder setUserConfidence(Double confidence){
			_details.setUserConfidence(confidence);
			return this;
		}
		
		/**
		 * 
		 * @param end
		 * @return this
		 * @throws IllegalArgumentException on null or invalid back-end
		 */
		public FeedbackTaskBuilder addBackend(AnalysisBackend end) throws IllegalArgumentException{
			if(end == null || !end.hasCapability(Capability.USER_FEEDBACK)){
				throw new IllegalArgumentException("The given back-end, id: "+end.getBackendId()+" does not have the required capability: "+Capability.USER_FEEDBACK.name());
			}
			_details.setBackend(new BackendStatus(end, TaskStatus.NOT_STARTED));
			return this;
		}
		
		/**
		 * This will automatically filter out back-end with inadequate capabilities
		 * 
		 * @param backendStatusList
		 * @return this
		 */
		public FeedbackTaskBuilder setBackends(BackendStatusList backendStatusList){
			if(BackendStatusList.isEmpty(backendStatusList)){
				LOGGER.warn("Empty backend status list.");
				backendStatusList = null;
			}else if((backendStatusList = BackendStatusList.getBackendStatusList(backendStatusList.getBackendStatuses(EnumSet.of(Capability.USER_FEEDBACK)))) == null){ // filter out back-ends with invalid capabilities
				LOGGER.warn("List contains no back-ends with valid capability "+Capability.USER_FEEDBACK.name()+"for task type "+TaskType.FEEDBACK.name());
			}
			_details.setBackends(backendStatusList);
			return this;
		}
		
		/**
		 * 
		 * @return this
		 */
		public FeedbackTaskBuilder clearDeletedPhotos(){
			_details.setDeletedPhotoList(null);
			return this;
		}
		
		/**
		 * 
		 * @return this
		 */
		public FeedbackTaskBuilder clearSimilarPhotos(){
			_details.setSimilarPhotoList(null);
			return this;
		}
		
		/**
		 * 
		 * @return this
		 */
		public FeedbackTaskBuilder clearDissimilarPhotos(){
			_details.setDissimilarPhotoList(null);
			return this;
		}
		
		/**
		 * 
		 * @return this
		 */
		public FeedbackTaskBuilder clearPhotos(){
			_details.setPhotoList(null);
			return this;
		}
		
		/**
		 * 
		 * @return this
		 */
		public FeedbackTaskBuilder clearReferencePhotos(){
			_details.setReferencePhotoList(null);
			return this;
		}
		
		/**
		 * 
		 * @return new task details based on the given data or null if no data was given
		 * @throws IllegalArgumentException on bad data
		 */
		public PhotoTaskDetails build() throws IllegalArgumentException {
			boolean hasDeleted = !PhotoList.isEmpty(_details.getDeletedPhotoList());
			PhotoList photoList = _details.getPhotoList();
			boolean hasPhotos = !PhotoList.isEmpty(photoList);
			SimilarPhotoList similarPhotoList = _details.getSimilarPhotoList();
			boolean hasSimilar = !PhotoList.isEmpty(similarPhotoList);
			DissimilarPhotoList dissimilarPhotoList = _details.getDissimilarPhotoList();
			boolean hasDissimilar = !PhotoList.isEmpty(dissimilarPhotoList);
			ReferencePhotoList referencePhotoList = _details.getReferencePhotoList();
			boolean hasReferences = !PhotoList.isEmpty(referencePhotoList);
			
			// check for validity:
			if(hasDeleted){
				if(hasPhotos || hasSimilar || hasDissimilar || hasReferences){
					throw new IllegalArgumentException("Deleted photos must appear alone.");
				} // no need to validate the deleted photo list, it only requires guids
			}else if(hasPhotos){
				if(hasSimilar || hasDissimilar || hasReferences){
					throw new IllegalArgumentException("Photos must appear alone.");
				}else if(!PhotoList.isValid(photoList)){
					throw new IllegalArgumentException("Invalid photo list.");
				}
			}else if(hasReferences){ // this will accept both similar and dissimilar to be present, if they contain valid photos
				if(!hasDissimilar && !hasSimilar){
					throw new IllegalArgumentException("References must have similar or dissimilar photos.");
				}
				
				if(hasDissimilar && !DissimilarPhotoList.isValid(dissimilarPhotoList)){
					throw new IllegalArgumentException("Invalid dissimilar photo list.");
				}
				
				if(hasSimilar && !SimilarPhotoList.isValid(similarPhotoList)){
					throw new IllegalArgumentException("Invalid similar photo list.");
				}
				
				if(!ReferencePhotoList.isValid(referencePhotoList)){
					throw new IllegalArgumentException("Invalid reference photo list.");
				}
			}else if(hasSimilar || hasDissimilar){
				throw new IllegalArgumentException("Similar and dissimilar photos cannot appear without references.");
			}else{
				LOGGER.debug("No content.");
				return null;
			}

			return _details;
		}
	} // class FeedbackTaskBuilder
}
