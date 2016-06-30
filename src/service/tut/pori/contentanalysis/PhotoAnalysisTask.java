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
package service.tut.pori.contentanalysis;

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
 * An implementation of ASyncTask, meant for executing an analysis task.
 * 
 * Requires a valid taskId for execution, provided in a JobExecutionContext.
 * 
 */
public class PhotoAnalysisTask extends AsyncTask{ 
	private static final Logger LOGGER = Logger.getLogger(PhotoAnalysisTask.class);

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		executeAddTask(EnumSet.of(Capability.PHOTO_ANALYSIS), ServiceInitializer.getDAOHandler().getDAO(PhotoTaskDAO.class), getTaskId(context.getMergedJobDataMap()));
	}

	/**
	 * Checks that the given response is for a valid task, that the backend has permissions for the task, and that the given result data is valid.
	 * On valid data, media objects for the photos will be updated (if there are changes). The given response will be modified to be valid, if possible.
	 * 
	 * @param response
	 * @throws IllegalArgumentException
	 */
	public static void taskFinished(PhotoTaskResponse response) throws IllegalArgumentException {
		Integer backendId = response.getBackendId();
		Long taskId = response.getTaskId();

		PhotoTaskDAO taskDAO = ServiceInitializer.getDAOHandler().getDAO(PhotoTaskDAO.class);
		BackendStatus backendStatus = taskDAO.getBackendStatus(backendId, taskId);
		if(backendStatus == null){
			LOGGER.warn("Backend, id: "+backendId+" returned results for task, not given to the backend. TaskId: "+taskId);
			throw new IllegalArgumentException("This task is not given for backend, id: "+backendId);
		}

		TaskStatus status = response.getStatus();
		if(status == null){
			LOGGER.warn("Task status not available.");
			status = TaskStatus.UNKNOWN;
		}
		backendStatus.setStatus(status);

		try{
			PhotoList results = response.getPhotoList();
			if(PhotoList.isEmpty(results)){
				LOGGER.warn("No results returned by the backendId: "+backendId);
				return;
			}

			if(!PhotoList.isValid(results)){
				LOGGER.warn("Invalid "+Definitions.ELEMENT_PHOTOLIST+".");
			}

			PhotoDAO photoDAO = ServiceInitializer.getDAOHandler().getDAO(PhotoDAO.class);
			if(!photoDAO.setOwners(results)){
				LOGGER.warn("Could not get owner information for all photos.");
			}

			PhotoList associations = new PhotoList();
			MediaObjectList insert = new MediaObjectList();
			MediaObjectList update = new MediaObjectList();
			MediaObjectDAO vdao = ServiceInitializer.getDAOHandler().getDAO(MediaObjectDAO.class);
			for(Iterator<Photo> photoIter = results.getPhotos().iterator(); photoIter.hasNext();){
				Photo photo = photoIter.next();
				String guid = photo.getGUID();
				UserIdentity userId = photo.getOwnerUserId();
				if(!UserIdentity.isValid(userId)){  // if this photo does not exist, there won't be userId
					LOGGER.warn("Ignoring non-existing photo, GUID: "+guid+" from back-end, id: "+backendId);
					photoIter.remove();
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
						backendStatus.setStatus(TaskStatus.ERROR);
						throw new IllegalArgumentException("Multiple back-end statuses.");
					}
					if(!backendId.equals(sList.get(0).getBackendId())){
						backendStatus.setStatus(TaskStatus.ERROR);
						throw new IllegalArgumentException("Invalid back-end status.");
					}
				}
				MediaObjectList vObjects = photo.getMediaObjects();
				if(!MediaObjectList.isEmpty(vObjects)){  // make sure all objects have proper user
					for(MediaObject mediaObject : vObjects.getMediaObjects()){ // check that the objects are valid
						if(!backendId.equals(mediaObject.getBackendId())){
							LOGGER.warn("Task backend id "+backendId+" does not match the back-end id "+mediaObject.getBackendId()+" given for media object, objectId: "+mediaObject.getObjectId());
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
				}else{
					LOGGER.warn("Ignored photo without objects, GUID : "+guid);
					photoIter.remove();
				}
			}

			if(MediaObjectList.isEmpty(insert)){
				LOGGER.debug("Nothing to insert.");
			}else if(!MediaObjectList.isValid(insert)){
				backendStatus.setStatus(TaskStatus.ERROR);
				throw new IllegalArgumentException("Invalid media object list.");
			}else if(!photoDAO.insert(insert)){
				LOGGER.warn("Failed to insert new objects.");		
			}else{
				photoDAO.associate(associations);
			}

			if(MediaObjectList.isEmpty(update)){
				LOGGER.debug("Nothing to update");
			}else if(!MediaObjectList.isValid(update)){
				backendStatus.setStatus(TaskStatus.ERROR);
				throw new IllegalArgumentException("Invalid media object list.");
			}else if(!photoDAO.update(update)){
				LOGGER.warn("Failed to update objects.");
			}

			taskDAO.updateMediaStatus(results.getPhotos(), taskId);	
			CAContentCore.scheduleBackendFeedback(backendId, results, taskId);
		} finally {
			taskDAO.updateTaskStatus(backendStatus, taskId);
			ServiceInitializer.getEventHandler().publishEvent(new AsyncTaskEvent(backendId, PhotoAnalysisTask.class, status, taskId, TaskType.ANALYSIS));
		}
	}
}
