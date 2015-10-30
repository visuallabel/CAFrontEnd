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
package service.tut.pori.contentanalysis.video;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import service.tut.pori.contentanalysis.AnalysisBackend;
import service.tut.pori.contentanalysis.AnalysisBackend.Capability;
import service.tut.pori.contentanalysis.AsyncTask;
import service.tut.pori.contentanalysis.BackendStatus;
import service.tut.pori.contentanalysis.BackendStatusList;
import service.tut.pori.contentanalysis.MediaObject;
import service.tut.pori.contentanalysis.MediaObjectDAO;
import service.tut.pori.contentanalysis.MediaObjectList;
import core.tut.pori.context.ServiceInitializer;
import core.tut.pori.users.UserIdentity;


/**
 * An implementation of ASyncTask, meant for executing a feedback task.
 * 
 * Requires a valid taskId for execution, provided in a JobExecutionContext.
 * 
 */
public class VideoFeedbackTask extends AsyncTask{
	private static final Logger LOGGER = Logger.getLogger(VideoFeedbackTask.class);
	
	/**
	 * 
	 * @param response
	 * @throws IllegalArgumentException
	 */
	public static void taskFinished(VideoTaskResponse response) throws IllegalArgumentException{
		Integer backendId = response.getBackendId();
		Long taskId = response.getTaskId();

		VideoTaskDAO taskDAO = ServiceInitializer.getDAOHandler().getSQLDAO(VideoTaskDAO.class);
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
				if(!UserIdentity.isValid(userId)){  // if this video does not exist, there won't be userId
					LOGGER.warn("Ignoring non-existing video, GUID: "+guid+" from backend, id: "+backendId);
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
						status = TaskStatus.ERROR;
						throw new IllegalArgumentException("Multiple backend statuses.");
					}
					if(!backendId.equals(sList.get(0).getBackendId())){
						status = TaskStatus.ERROR;
						throw new IllegalArgumentException("Invalid backend status.");
					}
				}
				MediaObjectList vObjects = video.getMediaObjects();
				if(!MediaObjectList.isEmpty(vObjects)){  // make sure all objects have proper user
					for(MediaObject mediaObject : vObjects.getMediaObjects()){ // check that the objects are valid
						if(!backendId.equals(mediaObject.getBackendId())){
							LOGGER.warn("Task backend id "+backendId+" does not match the backend id "+mediaObject.getBackendId()+" given for media object, objectId: "+mediaObject.getObjectId());
							mediaObject.setBackendId(backendId);
						}
						mediaObject.setOwnerUserId(userId);
					}
					vdao.resolveObjectIds(vObjects); // resolve ids for update/insert sort
					Video iVideo = null;
					for(MediaObject vo : vObjects.getMediaObjects()){ // re-sort to to updated and new
						if(StringUtils.isBlank(vo.getMediaObjectId())){ // no media object id, this is a new one
							if(iVideo == null){
								associations.getVideo(guid); // get target video for insertion
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
				} // for objects
			}

			if(MediaObjectList.isEmpty(insert)){
				LOGGER.debug("Nothing to insert.");
			}else if(!MediaObjectList.isValid(insert)){
				status = TaskStatus.ERROR;
				throw new IllegalArgumentException("Invalid media object list.");
			}else if(!videoDAO.insert(insert)){
				LOGGER.warn("Failed to insert new objects.");		
			}else{
				videoDAO.associate(associations);
			}

			if(MediaObjectList.isEmpty(update)){
				LOGGER.debug("Nothing to update");
			}else if(!MediaObjectList.isValid(update)){
				status = TaskStatus.ERROR;
				throw new IllegalArgumentException("Invalid media object list.");
			}else if(!vdao.update(update)){
				LOGGER.warn("Failed to update objects.");
			}

			taskDAO.updateMediaStatus(results.getVideos(), taskId);
			taskDAO.updateTaskStatus(taskStatus, taskId);
		} finally {
			ServiceInitializer.getEventHandler().publishEvent(new AsyncTaskEvent(backendId, VideoFeedbackTask.class, status, taskId, TaskType.FEEDBACK));
		}
	}

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		executeAddTask(EnumSet.of(Capability.VIDEO_ANALYSIS, Capability.USER_FEEDBACK), ServiceInitializer.getDAOHandler().getSQLDAO(VideoTaskDAO.class), getTaskId(context.getMergedJobDataMap()));
	}
	
	/**
	 * A helper class building VideoTaskDetails usable with {@link VideoFeedbackTask} and executable using {@link  service.tut.pori.contentanalysis.video.VideoContentCore#scheduleTask(VideoTaskDetails)}
	 * @see service.tut.pori.contentanalysis.video.VideoContentCore
	 * @see service.tut.pori.contentanalysis.video.VideoTaskDetails
	 */
	public static class FeedbackTaskBuilder{
		private VideoTaskDetails _details = null;
		
		/**
		 * 
		 * @param taskType {@link service.tut.pori.contentanalysis.AsyncTask.TaskType#FEEDBACK}
		 * @throws IllegalArgumentException on unsupported/invalid task type
		 */
		public FeedbackTaskBuilder(TaskType taskType) throws IllegalArgumentException {
			if(taskType != TaskType.FEEDBACK){
				throw new IllegalArgumentException("Invalid task type.");
			}
			_details = new VideoTaskDetails(taskType);
		}
		
		/**
		 * Add video to feedback task if the given video has (valid) changes
		 * 
		 * @param video
		 * @return this
		 */
		public FeedbackTaskBuilder addVideo(Video video){
			if(video == null){
				LOGGER.warn("Ignored null video.");
			}else{
				_details.addVideo(video);
			}
			return this;
		}
		
		/**
		 * 
		 * @param videos
		 * @return this
		 */
		public FeedbackTaskBuilder addVideos(VideoList videos){
			if(VideoList.isEmpty(videos)){
				LOGGER.warn("Ignored empty video list.");
			}else{
				for(Video p : videos.getVideos()){
					addVideo(p);
				}
			}
			return this;
		}
		
		/**
		 * 
		 * @param video
		 * @return this
		 * @throws IllegalArgumentException
		 */
		public FeedbackTaskBuilder addDeletedVideo(Video video) throws IllegalArgumentException{
			if(video == null){
				LOGGER.warn("Ignored null video.");
				return this;
			}else if(StringUtils.isBlank(video.getGUID())){
				throw new IllegalArgumentException("No GUID.");
			}
			_details.addDeletedVideo(video);
			return this;
		}
		
		/**
		 * 
		 * @param guids
		 * @return this
		 */
		public FeedbackTaskBuilder addDeletedVideos(Collection<String> guids){
			if(guids == null || guids.isEmpty()){
				LOGGER.warn("Ignored empty deleted video list.");
				return this;
			}
			for(String guid : guids){
				addDeletedVideo(new Video(guid));
			}
			return this;
		}
		
		/**
		 * 
		 * @param videos
		 * @return this
		 */
		public FeedbackTaskBuilder addDeletedVideos(DeletedVideoList videos){
			if(DeletedVideoList.isEmpty(videos)){
				LOGGER.warn("Ignored empty deleted video list.");
				return this;
			}
			DeletedVideoList deleted = _details.getDeletedVideoList();
			if(DeletedVideoList.isEmpty(deleted)){
				_details.setDeletedVideoList(videos);
			}else{
				deleted.addVideos(videos);
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
				LOGGER.debug("Empty backend status list.");
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
		public FeedbackTaskBuilder clearDeletedVideos(){
			_details.setDeletedVideoList(null);
			return this;
		}
		
		/**
		 * 
		 * @return this
		 */
		public FeedbackTaskBuilder clearVideos(){
			_details.setVideoList(null);
			return this;
		}
		
		/**
		 * 
		 * @return this
		 * @throws IllegalArgumentException
		 */
		private VideoTaskDetails buildFeedback() throws IllegalArgumentException {
			boolean hasDeleted = !VideoList.isEmpty(_details.getDeletedVideoList());
			VideoList videoList = _details.getVideoList();
			boolean hasVideos = !VideoList.isEmpty(videoList);
			
			// check for validity:
			if(hasDeleted){
				if(hasVideos){
					throw new IllegalArgumentException("Deleted videos must appear alone.");
				} // no need to validate the deleted video list, it only requires GUIDs
			}else if(!hasVideos){
				LOGGER.debug("No content.");
				return null;
			}

			return _details;
		}
		
		/**
		 * 
		 * @return video task details created from the given values
		 * @throws IllegalArgumentException on invalid value combination
		 */
		private VideoTaskDetails buildBackendFeedback() throws IllegalArgumentException {
			VideoList videoList = _details.getVideoList();
			if(VideoList.isEmpty(videoList)){
				throw new IllegalArgumentException("Back-end feedback must contain video list.");
			}else if(!DeletedVideoList.isEmpty(_details.getDeletedVideoList())){
				throw new IllegalArgumentException("Back-end feedback can only contain video list.");
			}

			return _details;
		}
		
		/**
		 * 
		 * @return new task details based on the given data or null if no data was given
		 */
		public VideoTaskDetails build() {
			if(_details.getTaskType() == TaskType.FEEDBACK){
				return buildFeedback();
			}else{
				return buildBackendFeedback();
			}
		}
	} // class FeedbackTaskBuilder
}
