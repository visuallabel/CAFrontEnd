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

import java.util.EnumSet;

import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import service.tut.pori.contentanalysis.AnalysisBackend;
import service.tut.pori.contentanalysis.AnalysisBackend.Capability;
import service.tut.pori.contentanalysis.BackendStatus;
import service.tut.pori.contentanalysis.BackendStatusList;
import core.tut.pori.context.ServiceInitializer;
import core.tut.pori.users.UserIdentity;

/**
 * An implementation of ASyncTask, meant for executing a back-end feedback task.
 * 
 * Requires a valid taskId for execution, provided in a JobExecutionContext.
 * 
 */
public class VideoBackendFeedbackTask extends VideoFeedbackTask {
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		executeAddTask(EnumSet.of(Capability.VIDEO_ANALYSIS, Capability.BACKEND_FEEDBACK), ServiceInitializer.getDAOHandler().getSQLDAO(VideoTaskDAO.class), getTaskId(context.getMergedJobDataMap()));
	}
	
	/**
	 * A helper class building VideoTaskDetails usable with {@link VideoBackendFeedbackTask} and executable using {@link  service.tut.pori.contentanalysis.video.VideoContentCore#scheduleTask(VideoTaskDetails)}
	 * @see service.tut.pori.contentanalysis.video.VideoContentCore
	 * @see service.tut.pori.contentanalysis.video.VideoTaskDetails
	 */
	public static class FeedbackTaskBuilder{
		private static final Logger LOGGER = Logger.getLogger(FeedbackTaskBuilder.class);
		private VideoTaskDetails _details = null;
		
		/**
		 * 
		 * @param taskType {@link service.tut.pori.contentanalysis.AsyncTask.TaskType#BACKEND_FEEDBACK}
		 * @throws IllegalArgumentException on unsupported/invalid task type
		 */
		public FeedbackTaskBuilder(TaskType taskType) throws IllegalArgumentException {
			if(taskType != TaskType.BACKEND_FEEDBACK){
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
			if(end == null || !end.hasCapability(Capability.BACKEND_FEEDBACK)){
				throw new IllegalArgumentException("The given back-end, id: "+end.getBackendId()+" does not have the required capability: "+Capability.BACKEND_FEEDBACK.name());
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
			}else if((backendStatusList = BackendStatusList.getBackendStatusList(backendStatusList.getBackendStatuses(EnumSet.of(Capability.BACKEND_FEEDBACK)))) == null){ // filter out back-ends with invalid capabilities
				LOGGER.warn("List contains no back-ends with valid capability "+Capability.BACKEND_FEEDBACK.name()+"for task type "+TaskType.BACKEND_FEEDBACK.name());
			}
			_details.setBackends(backendStatusList);
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
		 * @return video task details created from the given values
		 * @throws IllegalArgumentException on invalid value combination
		 */
		public VideoTaskDetails build() throws IllegalArgumentException {
			VideoList videoList = _details.getVideoList();
			if(VideoList.isEmpty(videoList)){
				throw new IllegalArgumentException("Back-end feedback must contain video list.");
			}else if(!DeletedVideoList.isEmpty(_details.getDeletedVideoList())){
				throw new IllegalArgumentException("Back-end feedback can only contain video list.");
			}

			return _details;
		}
	} // class FeedbackTaskBuilder
}
