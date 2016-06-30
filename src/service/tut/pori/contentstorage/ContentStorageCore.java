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
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.context.ApplicationListener;

import service.tut.pori.contentanalysis.AbstractTaskDetails;
import service.tut.pori.contentanalysis.AccessDetails;
import service.tut.pori.contentanalysis.AccessDetails.Permission;
import service.tut.pori.contentanalysis.AnalysisBackend;
import service.tut.pori.contentanalysis.AsyncTask;
import service.tut.pori.contentanalysis.AsyncTask.TaskStatus;
import service.tut.pori.contentanalysis.AsyncTask.TaskType;
import service.tut.pori.contentanalysis.BackendDAO;
import service.tut.pori.contentanalysis.BackendStatus;
import service.tut.pori.contentanalysis.BackendStatusList;
import service.tut.pori.contentanalysis.CAContentCore;
import service.tut.pori.contentanalysis.CAContentCore.ServiceType;
import service.tut.pori.contentanalysis.Media;
import service.tut.pori.contentanalysis.Photo;
import service.tut.pori.contentanalysis.PhotoDAO;
import service.tut.pori.contentanalysis.PhotoList;
import service.tut.pori.contentanalysis.PhotoTaskDetails;
import service.tut.pori.contentanalysis.TaskDAO;
import service.tut.pori.contentanalysis.video.Video;
import service.tut.pori.contentanalysis.video.VideoList;
import service.tut.pori.contentanalysis.video.VideoTaskDetails;
import service.tut.pori.users.facebook.FacebookUserCore;
import service.tut.pori.users.google.GoogleUserCore;
import service.tut.pori.users.twitter.TwitterUserCore;
import core.tut.pori.context.ServiceInitializer;
import core.tut.pori.http.RedirectResponse;
import core.tut.pori.users.UserEvent;
import core.tut.pori.users.UserEvent.EventType;
import core.tut.pori.users.UserIdentity;
import core.tut.pori.utils.ListUtils;
import core.tut.pori.utils.MediaUrlValidator;
import core.tut.pori.utils.MediaUrlValidator.MediaType;

/**
 * The core methods for managing metadata content storage.
 */
public final class ContentStorageCore {
	private static final String JOB_KEY_SERVICE_TYPES = "serviceType"; // type is EnumSet<ServiceType>
	private static final String JOB_KEY_USER_ID = "userId"; // type is Long
	private static final Logger LOGGER = Logger.getLogger(ContentStorageCore.class);
	
	/**
	 * 
	 */
	private ContentStorageCore(){
		// nothing needed
	}
	
	/**
	 * 
	 * @param backendIds
	 * @param status
	 * @return all of the requested back-ends with the given status
	 * @throws IllegalArgumentException on invalid id or if no back-ends are available
	 */
	private static BackendStatusList getBackendStatuses(int[] backendIds, TaskStatus status) throws IllegalArgumentException {
		BackendStatusList backendStatusList = new BackendStatusList();
		BackendDAO backendDAO = ServiceInitializer.getDAOHandler().getDAO(BackendDAO.class);
		if(!ArrayUtils.isEmpty(backendIds)){
			LOGGER.debug("Adding back-ends...");
			List<Integer> backendIdList = ListUtils.createList(backendIds);
			List<AnalysisBackend> backends = backendDAO.getBackends(backendIdList);
			if(backends == null){
				throw new IllegalArgumentException("Invalid back-end ids.");
			}
			for(Integer backendId : backendIdList){ // validate the user given back-end ids
				AnalysisBackend end = null;
				for(AnalysisBackend backend : backends){
					if(backend.getBackendId().equals(backendId)){
						end = backend;
						break;
					}
				}
				if(end == null){
					throw new IllegalArgumentException("Invalid back-end id: "+backendId);
				}
				backendStatusList.setBackendStatus(new BackendStatus(end, status));
			}
		}else{
			LOGGER.debug("No back-ends given, attempting to use defaults...");
			List<AnalysisBackend> backends = backendDAO.getEnabledBackends();
			if(backends == null){
				throw new IllegalArgumentException("No enabled back-ends available.");
			}
			for(AnalysisBackend end : backends){
				backendStatusList.setBackendStatus(new BackendStatus(end, status));
			}
		}
		return backendStatusList;
	}
	
	/**
	 * 
	 * @param authenticatedUser
	 * @param backendIds
	 * @param serviceTypes
	 * @return task id of the scheduled task or null if schedule failed
	 */
	public static Long synchronize(UserIdentity authenticatedUser, int[] backendIds, EnumSet<ServiceType> serviceTypes) {
		SynchronizationTaskDetails details = new SynchronizationTaskDetails();
		details.setBackends(getBackendStatuses(backendIds, TaskStatus.NOT_STARTED));
		SynchronizationTaskDetails.setServiceTypes(details, serviceTypes);
		details.setUserId(authenticatedUser);
		
		Long taskId = ServiceInitializer.getDAOHandler().getDAO(TaskDAO.class).insertTask(details); // use the default implementation to insert as there is only metadata content
		if(taskId == null){
			LOGGER.warn("Failed to add new synchronization task.");
		}else{
			CAContentCore.scheduleTask(JobBuilder.newJob(MetadataSynchronizationJob.class), taskId);
		}
		return taskId;
	}
	
	/**
	 * resolves dynamic /rest/r? redirection URL to static access URL
	 * 
	 * @param authenticatedUser
	 * @param serviceType
	 * @param guid
	 * @return redirection to dynamic URL
	 */
	public static RedirectResponse generateTargetUrl(UserIdentity authenticatedUser, ServiceType serviceType, String guid){
		AccessDetails details = ServiceInitializer.getDAOHandler().getDAO(PhotoDAO.class).getAccessDetails(authenticatedUser, guid);
		if(details == null){
			throw new IllegalArgumentException("Not Found.");
		}
		Permission access = details.getPermission();
		if(access == Permission.NO_ACCESS){
			LOGGER.debug("Access denied for GUID: "+guid+" for userId: "+(UserIdentity.isValid(authenticatedUser) ? authenticatedUser.getUserId() : "none"));
			throw new IllegalArgumentException("Not Found.");
		}
		LOGGER.debug("Granting access with "+Permission.class.toString()+" : "+access.name());
		
		String url = getContentStorage(false, serviceType).getTargetUrl(details);
		if(url == null){
			throw new IllegalArgumentException("Not Found.");
		}else{
			return new RedirectResponse(url);
		}
	}
	
	/**
	 * 
	 * @param autoScheduleEnabled
	 * @param serviceType
	 * @return content storage for the given service type
	 * @throws UnsupportedOperationException
	 */
	public static final ContentStorage getContentStorage(boolean autoScheduleEnabled, ServiceType serviceType) throws UnsupportedOperationException{
		switch(serviceType){
			case PICASA_STORAGE_SERVICE:
				return new PicasaCloudStorage(autoScheduleEnabled);
			case FACEBOOK_PHOTO:
				return new FacebookPhotoStorage(autoScheduleEnabled);
			case TWITTER_PHOTO:
				return new TwitterPhotoStorage(autoScheduleEnabled);
			case URL_STORAGE:
				return new URLContentStorage(autoScheduleEnabled);
			default:
				throw new UnsupportedOperationException("Unsupported ServiceType: "+serviceType.name());
		}
	}
	
	/**
	 * Removes all (photo) metadata associated with the given user. 
	 * This contains all service specific entries and the related media and media objects.
	 * 
	 * @param authenticatedUser
	 * @param guids optional filter
	 * @param serviceTypes
	 */
	public static void removeMetadata(UserIdentity authenticatedUser, Collection<String> guids, EnumSet<ServiceType> serviceTypes){
		if(ServiceType.isEmpty(serviceTypes)){
			LOGGER.warn("No service types given.");
			return;
		}
		
		for(ServiceType type : serviceTypes){
			try{
				getContentStorage(true, type).removeMetadata(authenticatedUser, guids);
			}catch(UnsupportedOperationException ex){
				LOGGER.warn(ex, ex);
			}
		} // for
	}
	
	/**
	 * 
	 * @param authenticatedUser
	 * @param backendIds
	 * @param urls
	 * @return details of the files added to the analysis task, note that not all files are necessary new ones, if the given URLs were already known by the system
	 */
	public static MediaList addUrls(UserIdentity authenticatedUser, int[] backendIds, List<String> urls) {
		List<String> photoUrls = new ArrayList<>();
		List<String> videoUrls = new ArrayList<>();
		MediaUrlValidator validator = new MediaUrlValidator();
		for(String url : urls){
			switch(validator.validateUrl(url)){
				case PHOTO:
					LOGGER.debug("Detected photo: "+url);
					photoUrls.add(url);
					break;
				case VIDEO:
					LOGGER.debug("Detected video: "+url);
					videoUrls.add(url);
					break;
				default:
					LOGGER.warn("Unknown media type for URL: "+url);
					break;
			}
		}
		
		BackendStatusList backends = getBackendStatuses(backendIds, TaskStatus.NOT_STARTED);
		URLContentStorage storage = new URLContentStorage();
		storage.setBackends(backends);
		ContentStorageListener listener = new ContentStorageListener();
		storage.setContentStorageListener(listener);
		
		if(photoUrls.isEmpty()){
			LOGGER.debug("No photo URLs.");
		}else{
			storage.addUrls(MediaType.PHOTO, authenticatedUser, photoUrls);
		}
		
		if(videoUrls.isEmpty()){
			LOGGER.debug("No video URLs.");
		}else{
			storage.addUrls(MediaType.VIDEO, authenticatedUser, videoUrls);
		}
		
		return listener.getMediaList();
	}
	
	/**
	 * Listener for user related events.
	 *
	 * Automatically instantiated by Spring as a bean.
	 */
	@SuppressWarnings("unused")
	private static class UserEventListener implements ApplicationListener<UserEvent>{

		@Override
		public void onApplicationEvent(UserEvent event) {
			EventType type = event.getType();
			switch(type){
				case USER_AUTHORIZATION_REVOKED:
					Long userId = event.getUserId().getUserId();
					LOGGER.debug("Detected event of type "+type.name()+", scheduling removal of all metadata content for user, id: "+userId);
					Class<?> source = event.getSource();
					if(source == FacebookUserCore.class){
						createJob(userId, EnumSet.of(ServiceType.FACEBOOK_PHOTO));
					}else if(source == GoogleUserCore.class){
						createJob(userId, EnumSet.of(ServiceType.PICASA_STORAGE_SERVICE));
					}else if(source == TwitterUserCore.class){
						createJob(userId, EnumSet.of(ServiceType.TWITTER_PHOTO));
					}
					break;
				case USER_REMOVED:
					userId = event.getUserId().getUserId();
					LOGGER.debug("Detected event of type "+type.name()+", scheduling removal of all metadata content for user, id: "+userId);
					createJob(userId, EnumSet.allOf(ServiceType.class));
					break;
				default: // ignore everything else
					break;
			}
		}
		
		/**
		 * 
		 * @param userId
		 * @param serviceTypes
		 */
		private void createJob(Long userId, EnumSet<ServiceType> serviceTypes){
			JobBuilder builder = JobBuilder.newJob(MetadataRemovalJob.class);
			JobDataMap data = new JobDataMap();
			data.put(JOB_KEY_USER_ID, userId);
			data.put(JOB_KEY_SERVICE_TYPES, serviceTypes);
			builder.setJobData(data);
			CAContentCore.schedule(builder);
		}
	} // class UserEventListener
	
	/**
	 * Job for removing content for the user designated by data key JOB_KEY_USER_ID for services designated by data key JOB_KEY_SERVICE_TYPES
	 *
	 */
	public static class MetadataRemovalJob implements Job{

		@SuppressWarnings("unchecked")
		@Override
		public void execute(JobExecutionContext context) throws JobExecutionException {
			JobDataMap data = context.getMergedJobDataMap();
			Long userId = data.getLong(JOB_KEY_USER_ID);
			LOGGER.debug("Removing all metadata content for user, id: "+userId);
			removeMetadata(new UserIdentity(userId), null, (EnumSet<ServiceType>) data.get(JOB_KEY_SERVICE_TYPES));
		}
	} // class MetadataRemovalJob
	
	/**
	 * Implementation of AbstractTaskDetails used internally for scheduling synchronization tasks.
	 */
	public static class SynchronizationTaskDetails extends AbstractTaskDetails{
		private static final String METADATA_SERVICE_TYPES = "serviceTypes";
		
		/**
		 * 
		 */
		public SynchronizationTaskDetails(){
			super();
			setTaskType(TaskType.UNDEFINED);
		}
		
		/**
		 * 
		 * @param details
		 * @param serviceTypes
		 */
		public static void setServiceTypes(AbstractTaskDetails details, EnumSet<ServiceType> serviceTypes){
			Map<String, String> metadata = details.getMetadata();
			if(serviceTypes == null || serviceTypes.isEmpty()){
				LOGGER.debug("No service types.");
				if(metadata != null){
					metadata.remove(METADATA_SERVICE_TYPES);
					if(metadata.isEmpty()){
						details.setMetadata(null);
					}
				}
				return;
			}
			StringBuilder cb = new StringBuilder();
			for(ServiceType s : serviceTypes){
				cb.append(s.getServiceId());
				cb.append(core.tut.pori.http.Definitions.SEPARATOR_URI_QUERY_PARAM_VALUES);
			}
			if(metadata == null){
				metadata = new HashMap<>(1);
			}
			metadata.put(METADATA_SERVICE_TYPES, cb.substring(0, cb.length()-1));
			details.setMetadata(metadata);
		}
		
		/**
		 * 
		 * @param details 
		 * @return service types associated with the details
		 */
		public static EnumSet<ServiceType> getServiceTypes(AbstractTaskDetails details){
			Map<String, String> metadata = details.getMetadata();
			if(metadata == null || metadata.isEmpty()){
				LOGGER.debug("No metadata.");
				return null;
			}

			String[] serviceTypes = StringUtils.split(metadata.get(METADATA_SERVICE_TYPES), core.tut.pori.http.Definitions.SEPARATOR_URI_QUERY_PARAM_VALUES);
			if(serviceTypes == null){
				LOGGER.debug("No service names.");
				return null;
			}
			
			EnumSet<ServiceType> types = EnumSet.noneOf(ServiceType.class);
			for(int i=0;i<serviceTypes.length;++i){
				types.add(ServiceType.fromServiceId(Integer.valueOf(serviceTypes[i])));
			}
			return types;
		}

		@Override
		public TaskParameters getTaskParameters() {
			return null;
		}

		@Override
		public void setTaskParameters(TaskParameters parameters) throws UnsupportedOperationException {
			throw new UnsupportedOperationException("Method not supported.");			
		}
	} // class SynchronizationTaskDetails
	
	/**
	 * A schedulable task used for synchronizing metadata
	 */
	public static class MetadataSynchronizationJob implements Job{

		@Override
		public void execute(JobExecutionContext context) throws JobExecutionException {
			JobDataMap data = context.getMergedJobDataMap();
			Long taskId = AsyncTask.getTaskId(data);
			if(taskId == null){
				LOGGER.warn("No taskId.");
				return;
			}

			TaskDAO taskDAO = ServiceInitializer.getDAOHandler().getDAO(TaskDAO.class);
			BackendStatusList backends = taskDAO.getBackendStatus(taskId, TaskStatus.NOT_STARTED);
			if(BackendStatusList.isEmpty(backends)){
				LOGGER.warn("No analysis back-ends available for taskId: "+taskId+" with status "+TaskStatus.NOT_STARTED.name());
				return;
			}
			
			AbstractTaskDetails details = taskDAO.getTask(null, null, null, taskId); // no need to retrieve per back-end as the details are the same for each back-end
			if(details == null){
				LOGGER.warn("Task not found, id: "+taskId);
				return;
			}

			UserIdentity userId = details.getUserId();
			LOGGER.debug("Execution started for user id: "+userId.getUserId());
			
			for(ServiceType type : SynchronizationTaskDetails.getServiceTypes(details)){
				try{
					ContentStorage storage = getContentStorage(true, type);
					storage.setBackends(backends);
					if(!storage.synchronizeAccount(userId)){
						LOGGER.warn("Failed to synchronize service of type "+type.name()+" for user, id: "+userId.getUserId());
					}
				}catch(Throwable ex){ // catch exceptions to prevent re-scheduling of the task on error
					LOGGER.warn(ex, ex);
				}
			}
			LOGGER.debug("Synchronization completed.");
		}	
	} // class MetadataSynchronizationJob
	
	/**
	 * internally used listener class
	 *
	 */
	private static class ContentStorageListener implements service.tut.pori.contentstorage.ContentStorage.ContentStorageListener {
		private PhotoList _analysisTaskPhotoList = null;
		private VideoList _analysisTaskVideoList = null;
		
		@Override
		public void analysisTaskCreated(AbstractTaskDetails details) {
			if(details != null){
				if(details instanceof PhotoTaskDetails){
					_analysisTaskPhotoList = ((PhotoTaskDetails) details).getPhotoList();
				}else if(details instanceof VideoTaskDetails){
					_analysisTaskVideoList = ((VideoTaskDetails) details).getVideoList();
				}else{
					LOGGER.debug("Ignored unsupported task details of type "+details.getClass());
				}
			}else{
				LOGGER.warn("Received null task details.");
			}
		}

		@Override
		public void feedbackTaskCreated(AbstractTaskDetails details) {
			// nothing needed
		}
		
		/**
		 * 
		 * @return combined media list of task photos and videos
		 */
		public MediaList getMediaList() {
			if(_analysisTaskPhotoList == null && _analysisTaskVideoList == null){
				return null;
			}
			
			List<Media> media = new ArrayList<>(PhotoList.count(_analysisTaskPhotoList)+VideoList.count(_analysisTaskVideoList));
			if(_analysisTaskPhotoList != null){
				for(Photo p : _analysisTaskPhotoList.getPhotos()){
					media.add(p);
				}
			}
			if(_analysisTaskVideoList != null){
				for(Video v : _analysisTaskVideoList.getVideos()){
					media.add(v);
				}
			}
			
			MediaList mediaList = new MediaList();
			mediaList.setMedia(media);
			return mediaList;
		}
	} // class ContentStorageListener
}
