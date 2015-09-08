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
package service.tut.pori.contentanalysis;

import java.util.EnumSet;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import service.tut.pori.contentanalysis.AsyncTask.TaskType;
import core.tut.pori.http.parameters.DataGroups;
import core.tut.pori.http.parameters.Limits;
import core.tut.pori.users.UserIdentity;
import core.tut.pori.utils.MediaUrlValidator.MediaType;

/**
 * A class for storing photo tasks.
 * 
 * This class can be used to store various other tasks, but one should read carefully what the methods do, to make sure no data gets lost (not stored).
 * A better option would be to sub-class this class and provide a new implementation for the required methods.
 */
public class PhotoTaskDAO extends MediaTaskDAO{
	private static final DataGroups BASIC = new DataGroups(DataGroups.DATA_GROUP_BASIC);
	private static final Logger LOGGER = Logger.getLogger(PhotoTaskDAO.class);
	private static final EnumSet<MediaType> MEDIA_TYPES = EnumSet.of(MediaType.PHOTO);
	@Autowired
	private PhotoDAO _photoDAO = null;
	
	/**
	 * Note that media objects provided for any other list than the basic list (see {@link service.tut.pori.contentanalysis.PhotoTaskDetails#getPhotoList()}) will be ignored.
	 * 
	 * @param details
	 * @see service.tut.pori.contentanalysis.PhotoTaskDetails#getPhotoList()
	 */
	private void insertTaskMediaObjects(PhotoTaskDetails details){
		PhotoList photos = details.getPhotoList();
		Long taskId = details.getTaskId();
		if(PhotoList.isEmpty(photos)){
			LOGGER.debug("No photos for task, id: "+taskId);
		}else{
			for(Photo p : photos.getPhotos()){
				insertTaskMediaObjects(p.getGUID(), taskId, p.getMediaObjects());
			}	// for photos
		}
	}
	
	/**
	 * 
	 * @param details
	 * @return created row id or null on failure
	 * @throws UnsupportedOperationException on unsupported task type
	 * @throws IllegalArgumentException on bad task content
	 */
	public Long insertTask(PhotoTaskDetails details) throws UnsupportedOperationException, IllegalArgumentException{
		TaskType type = details.getTaskType();
		switch(type){
			case ANALYSIS:
			case FEEDBACK:
			case BACKEND_FEEDBACK:
				break;
			default:
				throw new UnsupportedOperationException("TaskType not supported: "+type.name());
		}

		Long taskId = insertTask((AbstractTaskDetails) details);
		if(taskId == null){
			throw new IllegalArgumentException("Failed to add new task.");
		}

		insertTaskGUIDs(details);
		insertTaskMediaObjects(details);

		return taskId;
	}
	
	/**
	 * This will also set photo statuses, if any are present. Note that even through status elements can appear in any photo list,
	 * creating two different lists with identical GUIDs, and conflicting status lists may create undefined behavior.
	 * 
	 * @param details
	 */
	private void insertTaskGUIDs(PhotoTaskDetails details){
		Long taskId = details.getTaskId();
		PhotoList photos = details.getPhotoList();
		if(PhotoList.isEmpty(photos)){
			LOGGER.debug("No photos for task, id: "+taskId);
		}else{
			insertTaskGUIDs(photos.getPhotos(), taskId, GUIDType.MEDIA);
		}

		ReferencePhotoList refPhotos = details.getReferencePhotoList();
		if(PhotoList.isEmpty(refPhotos)){
			LOGGER.debug("No reference photos for task, id: "+taskId);
		}else{
			insertTaskGUIDs(refPhotos.getPhotos(), taskId, GUIDType.REFERENCE_MEDIA);
		}

		SimilarPhotoList simPhotos = details.getSimilarPhotoList();
		if(PhotoList.isEmpty(simPhotos)){
			LOGGER.debug("No similar photos for task, id: "+taskId);
		}else{
			insertTaskGUIDs(simPhotos.getPhotos(), taskId, GUIDType.SIMILAR_MEDIA);
		}

		DissimilarPhotoList disPhotos = details.getDissimilarPhotoList();
		if(PhotoList.isEmpty(disPhotos)){
			LOGGER.debug("No dissimilar photos for task, id: "+taskId);
		}else{
			insertTaskGUIDs(disPhotos.getPhotos(), taskId, GUIDType.DISSIMILAR_MEDIA);
		}

		DeletedPhotoList delPhotos = details.getDeletedPhotoList();
		if(PhotoList.isEmpty(delPhotos)){
			LOGGER.debug("No deleted photos for task, id: "+taskId);
		}else{
			insertTaskGUIDs(delPhotos.getPhotos(), taskId, GUIDType.DELETED_MEDIA);
		}
	}
	
	/**
	 * 
	 * @param backendId
	 * @param dataGroups optional dataGroups filter, if not given, default backend-specific datagroups will be used
	 * @param limits optional limits filter
	 * @param taskId
	 * @return the task or null if not found
	 * @throws IllegalArgumentException on bad values
	 */
	@Override
	public PhotoTaskDetails getTask(Integer backendId, DataGroups dataGroups, Limits limits, Long taskId) throws IllegalArgumentException{
		Pair<TaskType, UserIdentity> type = getTaskType(backendId, taskId);
		if(type == null){
			LOGGER.warn("Failed to resolve task type.");
			return null;
		}

		if(backendId == null){
			LOGGER.debug("No backend id given, will not check data groups.");
		}else if(DataGroups.isEmpty(dataGroups)){
			LOGGER.debug("No datagroups given, retrieving default data groups.");
			AnalysisBackend backend = getBackendDAO().getBackend(backendId);
			if(backend == null){
				throw new IllegalArgumentException("Backend, id: "+backendId+" does not exist.");
			}
			dataGroups = backend.getDefaultTaskDataGroups();
		}

		PhotoTaskDetails details = new PhotoTaskDetails(type.getLeft());
		details.setBackendId(backendId);
		details.setTaskId(taskId);
		details.setUserId(type.getRight());
		getPhotos(dataGroups, details, limits);

		getTaskMetadata(details);

		if(DataGroups.hasDataGroup(Definitions.DATA_GROUP_BACKEND_STATUS, dataGroups)){
			getBackendStatusList(details);
		}

		if(details.isEmpty()){
			LOGGER.warn("Task, id: "+taskId+" has no content.");
			return null;
		}else{
			return details;
		}
	}
	
	/**
	 * retrieve and set the photos for the task
	 * 
	 * @param dataGroups
	 * @param details
	 * @param limits
	 */
	private void getPhotos(DataGroups dataGroups, PhotoTaskDetails details, Limits limits){
		Long taskId = details.getTaskId();
	
		List<String> photoGUIDs = getTaskGUIDs(limits, taskId, GUIDType.MEDIA);
		if(photoGUIDs != null){
			LOGGER.debug("Retrieving photo list...");
			TaskType taskType = details.getTaskType();
			switch(taskType){
				case ANALYSIS:	// if task type is analysis, retrieve photo list based solely on the given datagroups
					LOGGER.debug("Retrieving all photos for the task based on the given dataGroups, for task of type "+TaskType.ANALYSIS.name()+", id: "+taskId);
					details.setPhotoList(_photoDAO.getPhotos(dataGroups, photoGUIDs, null, null, null));
					break;
				case BACKEND_FEEDBACK:
				case FEEDBACK:	// ignore result_info datagroup for FEEDBACK, status will be checked later
					LOGGER.debug("Retrieving all photos for the task based on datagroup "+DataGroups.DATA_GROUP_BASIC+", for task of type "+taskType.name()+", id: "+taskId);
					PhotoList photos = _photoDAO.getPhotos(BASIC, photoGUIDs, null, null, null);	// use basic to get all basic/core details of the photo
					if(!PhotoList.isEmpty(photos)){
						details.setPhotoList(photos);
						setMediaObjects(dataGroups, limits, photos.getPhotos(), MEDIA_TYPES, taskId);
					}
					break;
				default:	// should not happen
					throw new UnsupportedOperationException("Unsupported "+TaskType.class.toString());
			}
		}else if((photoGUIDs = getTaskGUIDs(limits, taskId, GUIDType.DELETED_MEDIA)) != null){
			LOGGER.debug("Retrieving deleted photo list...");
			for(String guid : photoGUIDs){
				details.addDeletedPhoto(new Photo(guid));
			}
		}else{
			LOGGER.debug("Retrieving similarity feedback data...");
			photoGUIDs = getTaskGUIDs(limits, taskId, GUIDType.REFERENCE_MEDIA);
			if(photoGUIDs == null){
				LOGGER.debug("No content: reference item missing.");
				return;
			}
			for(String guid : photoGUIDs){
				details.addReferencePhoto(new Photo(guid));
			}
			
			photoGUIDs = getTaskGUIDs(limits, taskId, GUIDType.SIMILAR_MEDIA);
			if(photoGUIDs != null){
				for(String guid : photoGUIDs){
					details.addSimilarPhoto(new Photo(guid));
				}
			}
			
			photoGUIDs = getTaskGUIDs(limits, taskId, GUIDType.DISSIMILAR_MEDIA);
			if(photoGUIDs != null){
				for(String guid : photoGUIDs){
					details.addDissimilarPhoto(new Photo(guid));
				}
			}	
		}

		if(DataGroups.hasDataGroup(Definitions.DATA_GROUP_STATUS, dataGroups)){
			LOGGER.debug("Retrieving photo status information for task, id: "+taskId);
			getMediaStatus(details.getPhotoList().getPhotos());
			getMediaStatus(details.getDeletedPhotoList().getPhotos());
			getMediaStatus(details.getReferencePhotoList().getPhotos());
			getMediaStatus(details.getSimilarPhotoList().getPhotos());
			getMediaStatus(details.getDissimilarPhotoList().getPhotos());
		}
	}
}
