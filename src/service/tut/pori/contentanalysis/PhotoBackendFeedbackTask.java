/**
 * Copyright 2015 Tampere University of Technology, Pori Department
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

import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import service.tut.pori.contentanalysis.AnalysisBackend.Capability;
import core.tut.pori.context.ServiceInitializer;
import core.tut.pori.users.UserIdentity;

/**
 * An implementation of ASyncTask, meant for executing a back-end feedback task.
 * 
 * Requires a valid taskId for execution, provided in a JobExecutionContext.
 * 
 */
public class PhotoBackendFeedbackTask extends PhotoFeedbackTask {

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		executeAddTask(EnumSet.of(Capability.PHOTO_ANALYSIS, Capability.BACKEND_FEEDBACK), ServiceInitializer.getDAOHandler().getDAO(PhotoTaskDAO.class), getTaskId(context.getMergedJobDataMap()));
	}

	/**
	 * A helper class building PhotoTaskDetails usable with {@link PhotoBackendFeedbackTask} and executable using {@link service.tut.pori.contentanalysis.CAContentCore#scheduleTask(PhotoTaskDetails)}}
	 * @see service.tut.pori.contentanalysis.CAContentCore
	 * @see service.tut.pori.contentanalysis.PhotoTaskDetails
	 */
	public static class FeedbackTaskBuilder{
		private static final Logger LOGGER = Logger.getLogger(FeedbackTaskBuilder.class);
		private PhotoTaskDetails _details = null;
		
		/**
		 * 
		 * @param taskType {@link service.tut.pori.contentanalysis.AsyncTask.TaskType#BACKEND_FEEDBACK}
		 * @throws IllegalArgumentException on unsupported/invalid task type
		 */
		public FeedbackTaskBuilder(TaskType taskType) throws IllegalArgumentException {
			super();
			if(taskType != TaskType.BACKEND_FEEDBACK){
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
			if(end == null || !end.hasCapability(Capability.BACKEND_FEEDBACK)){
				throw new IllegalArgumentException("The given back-end, id: "+end.getBackendId()+" does not have the required capabilities: "+Capability.USER_FEEDBACK.name());
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
		public FeedbackTaskBuilder clearPhotos(){
			_details.setPhotoList(null);
			return this;
		}
		
		/**
		 * 
		 * @return new task details based on the given data or null if no data was given
		 * @throws IllegalArgumentException 
		 */
		public PhotoTaskDetails build() throws IllegalArgumentException {
			PhotoList photoList = _details.getPhotoList();
			if(PhotoList.isEmpty(photoList)){
				throw new IllegalArgumentException("Back-end feedback must contain photo list.");
			}else if(!PhotoList.isEmpty(_details.getReferencePhotoList()) || !PhotoList.isEmpty(_details.getSimilarPhotoList()) || !PhotoList.isEmpty(_details.getDeletedPhotoList()) || !PhotoList.isEmpty(_details.getDissimilarPhotoList())){
				throw new IllegalArgumentException("Back-end feedback can only contain photo list.");
			}

			return _details;
		}
	} // class FeedbackTaskBuilder
}
