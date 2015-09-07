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
package service.tut.pori.contentanalysis.video;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import service.tut.pori.contentanalysis.AsyncTask;
import service.tut.pori.contentanalysis.BackendStatus;
import service.tut.pori.contentanalysis.BackendStatusList;
import service.tut.pori.contentanalysis.MediaObject;
import service.tut.pori.contentanalysis.MediaObjectDAO;
import service.tut.pori.contentanalysis.MediaObjectList;
import core.tut.pori.context.ServiceInitializer;
import core.tut.pori.users.UserIdentity;

/**
 * Analysis task for video content.
 */
public class VideoAnalysisTask extends AsyncTask {
	private static final Logger LOGGER = Logger.getLogger(VideoAnalysisTask.class);

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		executeAddTask(ServiceInitializer.getDAOHandler().getSQLDAO(VideoTaskDAO.class), getTaskId(context.getMergedJobDataMap()));
	}

	/**
	 * Checks that the given response is for a valid task, that the back-end has permissions for the task, and that the given result data is valid.
	 * On valid data, media objects for the videos will be updated (if there are changes). The given response will be modified to be valid, if possible.
	 * 
	 * @param response
	 * @throws IllegalArgumentException
	 */
	public static void taskFinished(VideoTaskResponse response) throws IllegalArgumentException {
		Integer backendId = response.getBackendId();
		Long taskId = response.getTaskId();

		VideoTaskDAO taskDAO = ServiceInitializer.getDAOHandler().getSQLDAO(VideoTaskDAO.class);
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
			VideoList results = response.getVideoList();
			if(VideoList.isEmpty(results)){
				LOGGER.warn("No results returned by the backendId: "+backendId);
				return;
			}

			if(!VideoList.isValid(results)){
				LOGGER.warn("Invalid "+Definitions.ELEMENT_VIDEOLIST+".");
			}

			VideoDAO videoDAO = ServiceInitializer.getDAOHandler().getSolrDAO(VideoDAO.class);
			if(!videoDAO.setOwners(results)){
				LOGGER.warn("Could not get owner information for all videos.");
			}

			VideoList associations = new VideoList();
			MediaObjectList insert = new MediaObjectList();
			MediaObjectList update = new MediaObjectList();
			MediaObjectDAO vdao = ServiceInitializer.getDAOHandler().getSolrDAO(MediaObjectDAO.class);
			for(Iterator<Video> videoIter = results.getVideos().iterator(); videoIter.hasNext();){
				Video video = videoIter.next();
				String guid = video.getGUID();
				UserIdentity userId = video.getOwnerUserId();
				if(!UserIdentity.isValid(userId)){  // if this photo does not exist, there won't be userId
					LOGGER.warn("Ignoring non-existing video, GUID: "+guid+" from backend, id: "+backendId);
					videoIter.remove();
					continue;
				}
				BackendStatusList c = video.getBackendStatus();
				if(BackendStatusList.isEmpty(c)){
					LOGGER.debug("Backend status not available for video, GUID: "+guid);
				}else if(c.getCombinedStatus() == TaskStatus.ERROR){
					LOGGER.warn("Error condition detected for video, GUID: "+guid);
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
				MediaObjectList vObjects = video.getMediaObjects();
				if(!MediaObjectList.isEmpty(vObjects)){  // make sure all objects have proper user
					for(MediaObject mediaObject : vObjects.getMediaObjects()){ // check that the objects are valid
						if(!backendId.equals(mediaObject.getBackendId())){
							LOGGER.warn("Task backend id: "+backendId+" does not match the back-end id: "+mediaObject.getBackendId()+" given for media object, objectId: "+mediaObject.getObjectId());
							mediaObject.setBackendId(backendId);
						}
						mediaObject.setOwnerUserId(userId);
					}
					vdao.resolveObjectIds(vObjects); // resolve ids for update/insert sort
					Video iVideo = null;
					for(MediaObject vo : vObjects.getMediaObjects()){ // re-sort to to updated and new
						if(StringUtils.isBlank(vo.getMediaObjectId())){ // no media object id, this is a new one
							if(iVideo == null){
								associations.getVideo(guid); // get target photo for insertion
								if(iVideo == null){
									iVideo = new Video(guid);
									associations.addVideo(iVideo);
								}
							}				
							iVideo.addMediaObject(vo);
							insert.addMediaObject(vo);
						}else{
							update.addMediaObject(vo);
						}
					} // for
				}else{
					LOGGER.warn("Ignored video without objects, GUID: "+guid);
					videoIter.remove();
				}
			}

			if(MediaObjectList.isEmpty(insert)){
				LOGGER.debug("Nothing to insert.");
			}else if(!MediaObjectList.isValid(insert)){
				backendStatus.setStatus(TaskStatus.ERROR);
				throw new IllegalArgumentException("Invalid media object list.");
			}else if(!videoDAO.insert(insert)){
				LOGGER.warn("Failed to insert new objects.");		
			}else{
				videoDAO.associate(associations);
			}

			if(MediaObjectList.isEmpty(update)){
				LOGGER.debug("Nothing to update");
			}else if(!MediaObjectList.isValid(update)){
				backendStatus.setStatus(TaskStatus.ERROR);
				throw new IllegalArgumentException("Invalid media object list.");
			}else if(!videoDAO.update(update)){
				LOGGER.warn("Failed to update objects.");
			}

			taskDAO.updateMediaStatus(results.getVideos(), taskId);	
			VideoContentCore.scheduleBackendFeedback(backendId, results, taskId);
		} finally {
			taskDAO.updateTaskStatus(backendStatus, taskId);
			ServiceInitializer.getEventHandler().publishEvent(new AsyncTaskEvent(backendId, VideoAnalysisTask.class, status, taskId, TaskType.ANALYSIS));
		}
	}
}
