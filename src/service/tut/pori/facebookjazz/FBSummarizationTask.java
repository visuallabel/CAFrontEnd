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
package service.tut.pori.facebookjazz;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;

import service.tut.pori.contentanalysis.AnalysisBackend;
import service.tut.pori.contentanalysis.AnalysisBackend.Capability;
import service.tut.pori.contentanalysis.AsyncTask;
import service.tut.pori.contentanalysis.BackendStatus;
import service.tut.pori.contentanalysis.BackendStatusList;
import service.tut.pori.contentanalysis.CAContentCore.ServiceType;
import service.tut.pori.contentanalysis.Definitions;
import service.tut.pori.contentanalysis.Photo;
import service.tut.pori.contentanalysis.CAContentCore.Visibility;
import service.tut.pori.contentanalysis.CAContentCore;
import service.tut.pori.contentanalysis.PhotoDAO;
import service.tut.pori.contentanalysis.PhotoList;
import service.tut.pori.contentanalysis.MediaObject;
import service.tut.pori.contentanalysis.MediaObjectDAO;
import service.tut.pori.contentanalysis.MediaObjectList;
import service.tut.pori.contentstorage.FacebookPhotoStorage;
import core.tut.pori.context.ServiceInitializer;
import core.tut.pori.users.UserIdentity;
import core.tut.pori.utils.XMLFormatter;


/**
 * An implementation of ASyncTask, meant for executing a Facebook summarization task.
 * 
 * Requires a valid taskId for execution, provided in a JobExecutionContext. Optionally, backendIdList can be provided.
 *
 */
public class FBSummarizationTask extends AsyncTask{
	private static final Double DEFAULT_TAG_CONFIDENCE = 1.0;
	private static final Integer DEFAULT_TAG_RANK = 1;
	private static final Logger LOGGER = Logger.getLogger(FBSummarizationTask.class);

	@Override
	public void execute(JobExecutionContext context) {
		try{
			LOGGER.debug("Executing task...");
			JobDataMap data = context.getMergedJobDataMap();

			Long taskId = getTaskId(data);
			if(taskId == null){
				LOGGER.warn("No taskId.");
				return;
			}

			FBTaskDAO taskDAO = ServiceInitializer.getDAOHandler().getDAO(FBTaskDAO.class);
			BackendStatusList backends = taskDAO.getBackendStatus(taskId, TaskStatus.NOT_STARTED);
			if(BackendStatusList.isEmpty(backends)){
				LOGGER.warn("No analysis back-ends available for taskId: "+taskId+" with status "+TaskStatus.NOT_STARTED.name());
				return;
			}

			FBSummarizationTaskDetails details = (FBSummarizationTaskDetails) taskDAO.getTask(null, null, null, taskId); // no need to retrive per back-end as the details are the same for each back-end
			if(details == null){
				LOGGER.warn("Task not found, id: "+taskId);
				return;
			}

			UserIdentity userId = details.getUserId();
			if(details.isSynchronize()){
				LOGGER.debug("Synchronizing...");
				new FacebookPhotoStorage().synchronizeAccount(userId); // synchronize the account ignoring possible errors, there is no need to wait for the generated photo analysis tasks to finish
			}else{
				LOGGER.debug("Not synchronizing...");
			}
			
			backends = BackendStatusList.getBackendStatusList(backends.getBackendStatuses(EnumSet.of(Capability.FACEBOOK_SUMMARIZATION))); // filter out incapable back-ends
			if(BackendStatusList.isEmpty(backends)){
				LOGGER.warn("Aborting summarization... no back-ends with capability "+Capability.FACEBOOK_SUMMARIZATION.name());
				return;
			}

			FacebookExtractor e = FacebookExtractor.getExtractor(userId);
			if(e == null){
				LOGGER.error("Failed to create extractor for the given user.");
				return;
			}

			FacebookProfile p = e.getProfile(details.getContentTypes());
			if(p == null){
				LOGGER.error("Failed to retrieve profile for the given user from Facebook.");
				return;
			}

			details.setProfile(p);

			try (CloseableHttpClient client = HttpClients.createDefault()) {
				BasicResponseHandler h = new BasicResponseHandler();
				for(BackendStatus status : backends.getBackendStatuses()){
					AnalysisBackend end = status.getBackend();
					try {
						Integer backendId = end.getBackendId();
						String url = end.getAnalysisUri()+Definitions.METHOD_ADD_TASK;
						LOGGER.debug("Task, id: "+taskId+", back-end id: "+backendId+". Executing POST "+url);
						HttpPost taskRequest = new HttpPost(url);
						details.setBackendId(backendId);
						taskRequest.setHeader("Content-Type", "text/xml; charset=UTF-8");
						taskRequest.setEntity(new StringEntity((new XMLFormatter()).toString(details), core.tut.pori.http.Definitions.ENCODING_UTF8));				

						LOGGER.debug("Backend with id: "+backendId+" responded "+client.execute(taskRequest,h));
					} catch (IOException ex) {
						LOGGER.warn(ex, ex);
					}
				}
			} catch (IOException ex) {
				LOGGER.error(ex, ex);
			}
		} catch(Throwable ex){	// catch all exceptions to prevent re-scheduling on error
			LOGGER.error(ex, ex);
		}
	}

	/**
	 * Process the response. After this method has finished, the response will not contain non-existent photos (if any were present).
	 * 
	 * @param response
	 * @throws IllegalArgumentException on bad data
	 */
	public static void taskFinished(FBTaskResponse response) throws IllegalArgumentException {
		Integer backendId = response.getBackendId();
		if(backendId == null){
			throw new IllegalArgumentException("Invalid backendId.");
		}
		Long taskId = response.getTaskId();
		if(taskId == null){
			throw new IllegalArgumentException("Invalid taskId.");
		}

		FBTaskDAO taskDAO = ServiceInitializer.getDAOHandler().getDAO(FBTaskDAO.class);
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
			PhotoList photoList = response.getPhotoList();
			if(PhotoList.isEmpty(photoList)){
				LOGGER.debug("No photo list returned by backend, id: "+backendId+", task, id: "+taskId);
			}else{ // create media objects and associate
				PhotoDAO pdao = ServiceInitializer.getDAOHandler().getDAO(PhotoDAO.class);
				List<String> foundGUIDs = PhotoList.getGUIDs(pdao.getPhotos(null, photoList.getGUIDs(), null, null, null));
				if(foundGUIDs == null){
					LOGGER.warn("None of the photos exist, will not process media objects for backend, id: "+backendId+" for task, id: "+taskId);
					photoList = null; // prevents generation of feedback task for invalid content
				}else{
					List<Photo> photos = photoList.getPhotos();
					LOGGER.debug("New media objects for photos, photo count: "+photos.size()+", backend, id: "+backendId);
					for(Iterator<Photo> iter = photos.iterator(); iter.hasNext();){
						Photo photo = iter.next();
						MediaObjectList mediaObjects = photo.getMediaObjects();
						if(MediaObjectList.isEmpty(mediaObjects)){
							LOGGER.warn("Ignored empty media object list for backend, id: "+backendId+" for task, id: "+taskId+", photo, GUID: "+photo.getGUID());
							iter.remove();
						}else if(!foundGUIDs.contains(photo.getGUID())){
							LOGGER.warn("Ignored non-existing photo for backend, id: "+backendId+" for task, id: "+taskId+", photo, GUID: "+photo.getGUID());
							iter.remove(); // remove to prevent association
						}else if(!validate(mediaObjects, backendId, photo.getOwnerUserId()) || !insertOrUpdate(mediaObjects)){
							backendStatus.setStatus(TaskStatus.ERROR);
							throw new IllegalArgumentException("Invalid object list returned by backend, id: "+backendId+" for task, id: "+taskId);
						}
					} // for
					pdao.associate(photoList);
				} // else
			}

			MediaObjectList objects = response.getMediaObjects();
			if(MediaObjectList.isEmpty(objects)){
				LOGGER.debug("No media object list returned by backend, id: "+backendId+" for task, id: "+taskId);
			}else if(!validate(objects, backendId, null) || !insertOrUpdate(objects)){
				backendStatus.setStatus(TaskStatus.ERROR);
				throw new IllegalArgumentException("Invalid object list returned by backend, id: "+backendId+" for task, id: "+taskId);
			}else{
				LOGGER.debug("New media objects: "+objects.getMediaObjects().size()+" backend, id: "+backendId);
			}

			CAContentCore.scheduleBackendFeedback(backendId, photoList, taskId);
		} finally {
			taskDAO.updateTaskStatus(backendStatus, taskId);
			ServiceInitializer.getEventHandler().publishEvent(new AsyncTaskEvent(backendId, FBSummarizationTask.class, status, taskId, TaskType.FACEBOOK_PROFILE_SUMMARIZATION));
		}
	}

	/**
	 * 
	 * @param mediaObjects non-null, non-empty validated object list
	 * @return true on success
	 */
	private static boolean insertOrUpdate(MediaObjectList mediaObjects){
		MediaObjectList updates = new MediaObjectList();
		MediaObjectList inserts = new MediaObjectList();

		for(MediaObject o : mediaObjects.getMediaObjects()){
			if(StringUtils.isBlank(o.getMediaObjectId())){ // no media object id
				inserts.addMediaObject(o);
			}else{
				updates.addMediaObject(o);
			}
		}
		
		PhotoDAO photoDAO = ServiceInitializer.getDAOHandler().getDAO(PhotoDAO.class);
		if(MediaObjectList.isEmpty(inserts)){
			LOGGER.debug("Nothing to insert.");
		}else if(!photoDAO.insert(inserts)){
			LOGGER.warn("Failed to insert media objects.");
			return false;
		}

		if(MediaObjectList.isEmpty(updates)){
			LOGGER.debug("Nothing to update.");
		}else if(!photoDAO.update(updates)){
			LOGGER.warn("Failed to update media objects.");
			return false;
		}
		return true;
	}

	/**
	 * Validate the given list of media objects, if confidence is missing, this method will automatically set it to default, rank will also be set to 0
	 * 
	 * This also set the correct serviceType and visibility ({service.tut.pori.contentanalysis.CAContentCore.Visibility#PRIVATE}, if not given), and resolves mediaObjectIds
	 * 
	 * @param mediaObjects non-empty and non-null list of objects
	 * @param backendId non-null id
	 * @param userId if null, the check will be ignored
	 * @return true if the given values were valid
	 */
	private static boolean validate(MediaObjectList mediaObjects, Integer backendId, UserIdentity userId){
		MediaObjectDAO vdao = ServiceInitializer.getDAOHandler().getDAO(MediaObjectDAO.class);
		vdao.resolveObjectIds(mediaObjects);
		for(MediaObject object : mediaObjects.getMediaObjects()){
			if(backendId != object.getBackendId()){
				LOGGER.warn("Backend id mismatch.");
				return false;
			}else if(userId != null && !UserIdentity.equals(object.getOwnerUserId(), userId)){
				LOGGER.warn("Media objects user identity does not match the given user identity.");
				return false;
			}
			Integer rank = object.getRank();
			if(rank == null){
				object.setRank(DEFAULT_TAG_RANK);
			}
			Double confidence = object.getConfidence();
			if(confidence == null){
				object.setConfidence(DEFAULT_TAG_CONFIDENCE);
			}
			object.setServiceType(ServiceType.FACEBOOK_JAZZ);
			if(object.getVisibility() == null){
				object.setVisibility(Visibility.PRIVATE);
			}
		}
		return true;
	}
}
