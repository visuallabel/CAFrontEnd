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

import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import service.tut.pori.contentanalysis.AnalysisBackend.Capability;
import service.tut.pori.contentanalysis.AsyncTask.TaskStatus;
import service.tut.pori.contentanalysis.AsyncTask.TaskType;
import service.tut.pori.contentanalysis.PhotoFeedbackTask.FeedbackTaskBuilder;
import service.tut.pori.contentstorage.ContentStorageCore;
import core.tut.pori.context.ServiceInitializer;
import core.tut.pori.http.RedirectResponse;
import core.tut.pori.http.parameters.DataGroups;
import core.tut.pori.http.parameters.Limits;
import core.tut.pori.users.UserIdentity;
import core.tut.pori.utils.MediaUrlValidator.MediaType;

/**
 * 
 * This class includes functions for general content operations, such as updating the keywords,
 * modifying photo details, and uploading new content 
 * 
 * Note: this does not have UserEventListener for user removal, this is because currently all content is managed by ContentStorage service, which will call all
 * the necessary method for removing photo content (when needed)
 */
public final class CAContentCore {
	/** default capabilities for photo tasks */
	public static final EnumSet<Capability> DEFAULT_CAPABILITIES = EnumSet.of(Capability.USER_FEEDBACK, Capability.PHOTO_ANALYSIS, Capability.BACKEND_FEEDBACK);
	private static final Logger LOGGER = Logger.getLogger(CAContentCore.class);

	/**
	 * Service type declarations.
	 *
	 */
	@XmlEnum
	public enum ServiceType {
		/** content has been retrieved from Picasa, service id: {@value service.tut.pori.contentanalysis.Definitions#SERVICE_ID_PICASA} */
		@XmlEnumValue(value=Definitions.SERVICE_ID_PICASA)
		PICASA_STORAGE_SERVICE(1),
		/** content has been retrieved from FSIO, service id: {@value service.tut.pori.contentanalysis.Definitions#SERVICE_ID_FSIO}  */
		@Deprecated
		@XmlEnumValue(value=Definitions.SERVICE_ID_FSIO)
		FSIO(2),
		/** 
		 * content has been retrieved from Facebook using the Facebook Jazz Service 
		 * 
		 * service id: {@value service.tut.pori.contentanalysis.Definitions#SERVICE_ID_FACEBOOK_JAZZ} 
		 * 
		 * @see service.tut.pori.facebookjazz.FBJContentCore
		 */
		@XmlEnumValue(value=Definitions.SERVICE_ID_FACEBOOK_JAZZ)
		FACEBOOK_JAZZ(3),
		/** content has been retrieved from Facebook, service id: {@value service.tut.pori.contentanalysis.Definitions#SERVICE_ID_FACEBOOK_PHOTO}  */
		@XmlEnumValue(value=Definitions.SERVICE_ID_FACEBOOK_PHOTO)
		FACEBOOK_PHOTO(4),
		/** 
		 * content has been retrieved from Twitter using the Twitter Jazz Service 
		 * 
		 * service id: {@value service.tut.pori.contentanalysis.Definitions#SERVICE_ID_TWITTER_JAZZ} 
		 * 
		 * @see service.tut.pori.twitterjazz.TJContentCore
		 */
		@XmlEnumValue(value=Definitions.SERVICE_ID_TWITTER_JAZZ)
		TWITTER_JAZZ(5),
		/** content has been retrieved from Twitter, service id: {@value service.tut.pori.contentanalysis.Definitions#SERVICE_ID_TWITTER_PHOTO}  */
		@XmlEnumValue(value=Definitions.SERVICE_ID_TWITTER_PHOTO)
		TWITTER_PHOTO(6),
		/** 
		 * content has been uploaded directly to the service using URLs
		 * 
		 * service id: {@value service.tut.pori.contentanalysis.Definitions#SERVICE_ID_URL_STORAGE} 
		 * 
		 * @see service.tut.pori.contentstorage.ContentStorageCore#addUrls(UserIdentity, int[], List)
		 * */
		@XmlEnumValue(value=Definitions.SERVICE_ID_URL_STORAGE)
		URL_STORAGE(7);

		private int _id;

		/**
		 * 
		 * @param id
		 */
		private ServiceType(int id){
			_id = id;
		}

		/**
		 * 
		 * @param id
		 * @return the service id converted to ServiceType
		 * @throws IllegalArgumentException on bad input
		 */
		public static ServiceType fromServiceId(Integer id) throws IllegalArgumentException{
			if(id != null){
				for(ServiceType e : ServiceType.values()){
					if(e._id == id)
						return e;
				}
			}
			throw new IllegalArgumentException("Bad "+ServiceType.class.toString()+" : "+id);
		}

		/**
		 * 
		 * @param types
		 * @return true if the given set was null or empty
		 */
		public static boolean isEmpty(EnumSet<ServiceType> types){
			return (types == null || types.isEmpty() ? true : false);
		}

		/**
		 * 
		 * @return service id of this type
		 */
		public int getServiceId(){
			return _id;
		}

		/**
		 * 
		 * 
		 * @param serviceIds
		 * @return set of service types or null (if empty array is passed OR the array contains only NONE)
		 * @throws IllegalArgumentException on bad input
		 */
		public static EnumSet<ServiceType> fromIdArray(int[] serviceIds) throws IllegalArgumentException{
			if(ArrayUtils.isEmpty(serviceIds)){
				return null;
			}
			EnumSet<ServiceType> set = EnumSet.noneOf(ServiceType.class);
			for(int i=0;i<serviceIds.length;++i){
				set.add(fromServiceId(serviceIds[i]));
			}
			return set;
		}

		/**
		 * 
		 * @param types
		 * @return the types as integer list or null if null or empty list was passed
		 */
		public static int[] toIdArray(EnumSet<ServiceType> types){
			if(ServiceType.isEmpty(types)){
				LOGGER.debug("No types.");
				return null;
			}
			int[] array = new int[types.size()];
			int index = 0;
			for(Iterator<ServiceType> iter = types.iterator(); iter.hasNext(); ++index){
				array[index] = iter.next().getServiceId();
			}
			return array;
		}

		/**
		 * 
		 * @param set
		 * @return the passed set as a id string (service_id=ID,ID,ID...) or null, if null, empty set, or set containing ServiceType.ALL is passed 
		 */
		public static String toServiceIdString(EnumSet<ServiceType> set){
			if(set == null || set.size() < 1){
				return null;
			}else{
				StringBuilder sb = new StringBuilder(Definitions.PARAMETER_SERVICE_ID+"=");
				Iterator<ServiceType> iter = set.iterator();
				sb.append(iter.next().getServiceId());
				while(iter.hasNext()){
					sb.append(",");
					sb.append(iter.next().getServiceId());
				}
				return sb.toString();
			}
		}
	}  // enum ServiceType
	
	/**
	 * The visibility.
	 */
	@XmlEnum
	public enum Visibility{
		/** content can be accessed by anyone */
		@XmlEnumValue(value=Definitions.VISIBILITY_PUBLIC)
		PUBLIC(0),
		/** content can be accessed only by the owner */
		@XmlEnumValue(value=Definitions.VISIBILITY_PRIVATE)
		PRIVATE(1),
		/** content can be accessed only by the users in the defined group */
		@XmlEnumValue(value=Definitions.VISIBILITY_GROUP)
		GROUP(2);

		private int _value;

		/**
		 * 
		 * @param value
		 */
		private Visibility(int value){
			_value = value;
		}

		/**
		 * 
		 * @return the visibility as integer
		 */
		public final int toInt(){
			return _value;
		}

		/**
		 * 
		 * @param value
		 * @return the value converted to Visibility
		 * @throws IllegalArgumentException on bad input
		 */
		public static Visibility fromInt(int value) throws IllegalArgumentException{
			for(Visibility v : Visibility.values()){
				if(v._value == value){
					return v;
				}
			}
			throw new IllegalArgumentException("Bad "+Visibility.class.toString()+" : "+value);
		}
	}  // enum Visibility

	/**
	 * 
	 */
	private CAContentCore() {
		// nothing needed
	}

	/**
	 * 
	 * @param response
	 * @throws IllegalArgumentException
	 */
	public static void taskFinished(PhotoTaskResponse response) throws IllegalArgumentException{
		validateTaskResponse(response);

		LOGGER.debug("TaskId: "+response.getTaskId()+", backendId: "+response.getBackendId());

		switch(response.getTaskType()){
			case ANALYSIS:
				PhotoAnalysisTask.taskFinished(response);
				break;
			case BACKEND_FEEDBACK:
				LOGGER.debug("Using "+PhotoFeedbackTask.class.toString()+" for task of type "+TaskType.BACKEND_FEEDBACK);
			case FEEDBACK:
				PhotoFeedbackTask.taskFinished(response);
				break;
			case SEARCH:
				LOGGER.debug("Received taskFinished to a search task: asynchronous search tasks are not supported.");
			default:
				throw new IllegalArgumentException("Unsupported "+Definitions.ELEMENT_TASK_TYPE);
		}
	}

	/**
	 * 
	 * @param response
	 * @throws IllegalArgumentException on null response, bad task id, bad backend id and/or bad task type
	 */
	public static void validateTaskResponse(TaskResponse response) throws IllegalArgumentException{
		if(response == null){
			throw new IllegalArgumentException("Failed to process response.");
		}
		Long taskId = response.getTaskId();
		if(taskId == null){
			throw new IllegalArgumentException("Invalid "+Definitions.ELEMENT_TASK_ID);
		}
		Integer backendId = response.getBackendId();
		if(backendId == null){
			throw new IllegalArgumentException("Invalid "+Definitions.ELEMENT_BACKEND_ID);
		}
		TaskType type = response.getTaskType();
		if(type == null){
			throw new IllegalArgumentException("Invalid "+Definitions.ELEMENT_TASK_TYPE);
		}
	}

	/**
	 * 
	 * @param guid
	 * @param type
	 * @return redirection URL for the given GUID and type or null if either one the given values was null
	 */
	public static String generateRedirectUrl(String guid, ServiceType type){
		if(type == null || StringUtils.isBlank(guid)){
			LOGGER.warn("GUID or service type was null.");
			return null;
		}
		return ServiceInitializer.getPropertyHandler().getRESTBindContext()+Definitions.SERVICE_CA+"/"+Definitions.METHOD_REDIRECT+"?"+Definitions.PARAMETER_GUID+"="+guid+"&"+Definitions.PARAMETER_SERVICE_ID+"="+type.getServiceId();
	}

	/**
	 * resolves dynamic /rest/r? redirection URL to static access URL
	 * 
	 * @param authenticatedUser
	 * @param serviceType
	 * @param guid
	 * @return redirection to static URL referenced by the given parameters
	 */
	public static RedirectResponse generateTargetUrl(UserIdentity authenticatedUser, ServiceType serviceType, String guid){
		return ContentStorageCore.generateTargetUrl(authenticatedUser, serviceType, guid);
	}

	/**
	 * 
	 * This method is called by back-ends to retrieve a list of photos to be analyzed.
	 * To query tasks status from back-end use queryTaskStatus.
	 * 
	 * @param backendId
	 * @param taskId
	 * @param dataGroups
	 * @param limits
	 * @return the task or null if not found
	 */
	public static AbstractTaskDetails queryTaskDetails(Integer backendId, Long taskId, DataGroups dataGroups, Limits limits) {
		return ServiceInitializer.getDAOHandler().getDAO(PhotoTaskDAO.class).getTask(backendId, dataGroups, limits, taskId);
	}

	/**
	 * Note: if the details already contain a taskId, the task will NOT be re-added to the database, but simply re-scheduled.
	 * 
	 * If the details contains no back-ends, default back-ends will be added. See {@link #DEFAULT_CAPABILITIES}
	 * 
	 * @param details
	 * @return task id of the generated task, null if task could not be created
	 */
	public static Long scheduleTask(PhotoTaskDetails details) {
		JobBuilder builder = getBuilder(details.getTaskType());
		Long taskId = details.getTaskId();
		if(taskId != null){
			LOGGER.debug("Task id already present for task, id: "+taskId);
		}else{
			BackendStatusList backends = details.getBackends();
			if(BackendStatusList.isEmpty(backends)){
				LOGGER.debug("No back-ends given, using defaults...");
				List<AnalysisBackend> ends = ServiceInitializer.getDAOHandler().getDAO(BackendDAO.class).getBackends(DEFAULT_CAPABILITIES);
				if(ends == null){
					LOGGER.warn("Aborting task, no capable back-ends.");
					return null;
				}
				
				backends = new BackendStatusList();
				backends.setBackendStatus(ends, TaskStatus.NOT_STARTED);
				details.setBackends(backends);
			}
			
			taskId = ServiceInitializer.getDAOHandler().getDAO(PhotoTaskDAO.class).insertTask(details);
			if(taskId == null){
				LOGGER.error("Task schedule failed: failed to insert new photo task.");
				return null;
			}
		}

		if(scheduleTask(builder, taskId)){
			return taskId;
		}else{
			LOGGER.error("Failed to schedule new task.");
			return null;
		}
	}
	
	/**
	 * 
	 * @param builder
	 * @param taskId
	 * @return true if the task was successfully scheduled
	 * @throws IllegalArgumentException on bad values
	 * @see #schedule(JobBuilder)
	 */
	public static boolean scheduleTask(JobBuilder builder, Long taskId) throws IllegalArgumentException{
		if(taskId == null || builder == null){
			throw new IllegalArgumentException("Invalid task id or builder.");
		}
		JobDataMap data = new JobDataMap();
		AsyncTask.setTaskId(data, taskId);
		builder.setJobData(data);
		LOGGER.debug("Scheduling task, id: "+taskId);
		return schedule(builder);
	}
	
	/**
	 * Uses the platform defined scheduler to schedule the given builder. 
	 * This may add a scheduling delay depending on the system property configuration.
	 * 
	 * Note that the task may not necessarily start <i>immediately</i>, but may be delayed because of other tasks already  added into the queue.
	 * 
	 * @param builder
	 * @return true if the job was successfully scheduled
	 * @throws IllegalArgumentException on bad parameters
	 */
	public static boolean schedule(JobBuilder builder) throws IllegalArgumentException {
		if(builder == null){
			throw new IllegalArgumentException("No builder given.");
		}
		TriggerBuilder<Trigger> trigger = null;
		long delay = ServiceInitializer.getPropertyHandler().getSystemProperties(CAProperties.class).getScheduleTaskDelay();
		if(delay == CAProperties.TASK_DELAY_DISABLED){
			LOGGER.debug("Scheduling new task to start NOW.");
			trigger = TriggerBuilder.newTrigger().startNow();
		}else{
			LOGGER.debug("Scheduling new task to start in "+delay+" milliseconds.");
			trigger = TriggerBuilder.newTrigger().startAt(new Date(System.currentTimeMillis()+delay));
		}
		
		try {
			ServiceInitializer.getExecutorHandler().getScheduler().scheduleJob(builder.build(), trigger.build());
		} catch (SchedulerException ex) {
			LOGGER.error(ex, ex);
			return false;
		}
		return true;
	}
	
	/**
	 * 
	 * @param type
	 * @return new builder for the given type
	 * @throws UnsupportedOperationException on unsupported type
	 * @throws IllegalArgumentException on bad type
	 */
	private static JobBuilder getBuilder(TaskType type) throws UnsupportedOperationException, IllegalArgumentException{
		if(type == null){
			throw new IllegalArgumentException("Null type.");
		}
		switch (type) {
			case ANALYSIS:
				return JobBuilder.newJob(PhotoAnalysisTask.class);
			case BACKEND_FEEDBACK:
				return JobBuilder.newJob(PhotoBackendFeedbackTask.class);
			case FEEDBACK:
				return JobBuilder.newJob(PhotoFeedbackTask.class);
			case SEARCH:
				LOGGER.debug("Task schedule failed: asynchronous search tasks are not supported.");
			default:
				throw new UnsupportedOperationException("Unsupported TaskType: "+type.name());
		}
	}

	/**
	 * 
	 * @param authenticatedUser
	 * @param guids
	 * @param dataGroups
	 * @param limits
	 * @param serviceTypes
	 * @param userIdFilters
	 * @return list of photos or null if none was found with the given parameters
	 */
	public static PhotoList getPhotos(UserIdentity authenticatedUser, List<String> guids, DataGroups dataGroups, Limits limits, EnumSet<ServiceType> serviceTypes, long[] userIdFilters){
		return ServiceInitializer.getDAOHandler().getDAO(PhotoDAO.class).search(authenticatedUser, dataGroups, guids, limits, null, serviceTypes, userIdFilters);
	}

	/**
	 * This does NOT sync the changes back to content storage (e.g. picasa) to prevent conflicts in future synchronizations
	 * 
	 * @param authenticatedUser
	 * @param photoList
	 * @throws IllegalArgumentException
	 */
	public static void updatePhotos(UserIdentity authenticatedUser, PhotoList photoList) throws IllegalArgumentException{
		if(!PhotoList.isValid(photoList)){
			throw new IllegalArgumentException("Received empty or invalid photoList.");
		}else{
			FeedbackTaskBuilder builder = new FeedbackTaskBuilder(TaskType.FEEDBACK);
			builder.setUser(authenticatedUser);
			for(Photo photo :  photoList.getPhotos()){
				if(MediaObjectList.isEmpty(photo.getMediaObjects())){
					LOGGER.debug("Ignored photo without media objects.");
				}else{
					builder.addPhoto(photo);	// add to feedback task
				}
			}

			if(!ServiceInitializer.getDAOHandler().getDAO(PhotoDAO.class).updatePhotos(authenticatedUser, photoList)){
				throw new IllegalArgumentException("Could not update photos.");
			}

			PhotoTaskDetails details = builder.build();
			if(details == null){
				LOGGER.debug("Nothing updated, will not generate feedback.");
			}else{
				scheduleTask(details);
			}
		}
	}

	/**
	 * This will allow some amount of bad GUIDs to exist as long as there are enough to make a proper request:
	 * at least one ref photo must exist, at least one similar or dissimilar photo must exist. The GUIDs must be unique,
	 * the same GUID may not appear in similar, dissimilar and ref list.
	 * 
	 * @param authenticatedUser must be given
	 * @param feedbackList
	 */
	public static void similarityFeedback(UserIdentity authenticatedUser, PhotoFeedbackList feedbackList){
		if(!UserIdentity.isValid(authenticatedUser)){
			throw new IllegalArgumentException("Invalid user.");
		}

		if(!PhotoFeedbackList.isValid(feedbackList)){
			throw new IllegalArgumentException("Invalid feedback.");
		}
		
		ReferencePhotoList referenceList = feedbackList.getReferencePhotos();
		List<String> guids = referenceList.getGUIDs();
		
		SimilarPhotoList simList = feedbackList.getSimilarPhotos();
		if(SimilarPhotoList.isEmpty(simList)){
			LOGGER.debug("No similar photos.");
			simList = null; // make sure it is really null
		}else{
			guids.addAll(simList.getGUIDs());
		}
		
		DissimilarPhotoList disList = feedbackList.getDissimilarPhotos();
		if(DissimilarPhotoList.isEmpty(disList)){
			LOGGER.debug("No dissimilar photos.");
			disList = null; // make sure it is really null
		}else{
			guids.addAll(disList.getGUIDs());
		}

		PhotoList found = ServiceInitializer.getDAOHandler().getDAO(PhotoDAO.class).search(authenticatedUser, null, guids, null, null, null, null); // we use search as the user can give feedback to all photos he/she would get as search results
		if(PhotoList.isEmpty(found)){
			LOGGER.warn("Ignored feedback for non-existing or unauthorized photos, for user, id: "+authenticatedUser.getUserId());
			return;
		}
		
		FeedbackTaskBuilder builder = new FeedbackTaskBuilder(TaskType.FEEDBACK);
		for(Photo photo : referenceList.getPhotos()){ // validate the given photos through found to make sure no bad data has been given
			String guid = photo.getGUID();
			Photo p = found.getPhoto(guid);
			if(p == null){
				LOGGER.debug("Ignored reference photo with bad GUID: "+guid+" for user, id: "+authenticatedUser.getUserId());
			}
			builder.addReferencePhoto(p);
		}
		
		if(simList != null){
			for(Photo photo : simList.getPhotos()){ // validate the given photos through found to make sure no bad data has been given
				String guid = photo.getGUID();
				Photo p = found.getPhoto(guid);
				if(p == null){
					LOGGER.debug("Ignored similar photo with bad GUID: "+guid+" for user, id: "+authenticatedUser.getUserId());
				}
				builder.addSimilarPhoto(p);
			}
		}
		
		if(disList != null){
			for(Photo photo : disList.getPhotos()){ // validate the given photos through found to make sure no bad data has been given
				String guid = photo.getGUID();
				Photo p = found.getPhoto(guid);
				if(p == null){
					LOGGER.debug("Ignored dissimilar photo with bad GUID: "+guid+" for user, id: "+authenticatedUser.getUserId());
				}
				builder.addDissimilarPhoto(p);
			}
		}

		builder.setUser(authenticatedUser);
		PhotoTaskDetails details = builder.build();
		if(details == null){
			throw new IllegalArgumentException("Bad reference list.");
		}
		scheduleTask(details);
	}

	/**
	 * 
	 * @param authenticatedUser
	 * @param dataGroups
	 * @param limits
	 * @param mediaTypes optional media types for the retrieval, if null or empty, all types will be searched for
	 * @param serviceTypes
	 * @param mediaObjectIdFilters
	 * @return list of media objects or null if none was found with the given parameters
	 */
	public static MediaObjectList getMediaObjects(UserIdentity authenticatedUser, DataGroups dataGroups, Limits limits, EnumSet<MediaType> mediaTypes, EnumSet<ServiceType> serviceTypes, List<String> mediaObjectIdFilters) {
		MediaObjectList objects = null;
		if(mediaObjectIdFilters != null){ // if there are ids, convert to objects to use as a filter
			objects = new MediaObjectList();
			for(String mediaObjectId : mediaObjectIdFilters){
				MediaObject o = new MediaObject(); // the media object will have type of UNKNOWN
				o.setMediaObjectId(mediaObjectId);
				objects.addMediaObject(o);
			}
		}
		if(mediaTypes == null || mediaTypes.isEmpty()){
			LOGGER.debug("Empty media type set given, using all...");
			mediaTypes = EnumSet.allOf(MediaType.class);
		}
		return ServiceInitializer.getDAOHandler().getDAO(MediaObjectDAO.class).search(authenticatedUser, dataGroups, limits, mediaTypes, serviceTypes, null, null, objects);
	}

	/**
	 * Delete the given photos. Normal user (ROLE_USER) can only delete his/her own photos. Back-end user (ROLE_BACKEND) can delete any photos.
	 * 
	 * @param authenticatedUser
	 * @param guids list of guids. Non-existent guids will be ignored.
	 * @return true on success, false on failure. Failure generally means a permission problem.
	 * @throws IllegalArgumentException on bad user id
	 */
	public static boolean deletePhotos(UserIdentity authenticatedUser, List<String> guids) throws IllegalArgumentException {
		if(!UserIdentity.isValid(authenticatedUser)){
			throw new IllegalArgumentException("Invalid user.");
		}
		if(guids == null || guids.isEmpty()){
			LOGGER.warn("Ignored empty guid list.");
			return true;
		}
		
		PhotoDAO dao = ServiceInitializer.getDAOHandler().getDAO(PhotoDAO.class);
		List<AccessDetails> details = dao.getAccessDetails(authenticatedUser, guids);
		if(details == null){
			LOGGER.debug("None of the guids were found.");
			return true; // the user has requested that the photos be deleted, and they are gone, so return success
		}
		
		guids = new ArrayList<>(guids.size()); // the found GUIDs
		for(AccessDetails d : details){ // go through the list of found details and check the permissions
			switch(d.getPermission()){
				case BACKEND_ACCESS:
					LOGGER.debug("Granting delete permissions for user, id: "+authenticatedUser.getUserId()+" for photo, GUID: "+d.getGuid()+" using permissions: "+AccessDetails.Permission.BACKEND_ACCESS.name());
				case PRIVATE_ACCESS:
					guids.add(d.getGuid());
					break;
				default:
					LOGGER.warn("Permission denied for user, id: "+authenticatedUser.getUserId()+" for photo, GUID: "+d.getGuid());
					return false;
			}
		}
		
		ContentStorageCore.removeMetadata(authenticatedUser, guids, EnumSet.allOf(ServiceType.class)); // remove from content storage (if added)
		dao.remove(guids); // remove from photo DAO
		
		scheduleTask(new FeedbackTaskBuilder(TaskType.FEEDBACK)
						.setUser(authenticatedUser)
						.addDeletedPhotos(guids)
						.build()
					);
	
		return true;
	}
	
	/**
	 * Create and schedule the task for all capable back-ends included in the task designated by the task Id. The given back-end Id will not participate in the feedback task.
	 * 
	 * @param backendId the back-end that send the task finished call, this back-end is automatically omitted from the list of target back-ends
	 * @param photos photos returned in task finished call
	 * @param taskId the id of the finished analysis task
	 */
	public static void scheduleBackendFeedback(Integer backendId, PhotoList photos, Long taskId){
		if(PhotoList.isEmpty(photos)){
			LOGGER.debug("Not scheduling back-end feedback: empty photo list.");
			return;
		}
		
		BackendStatusList tBackends = ServiceInitializer.getDAOHandler().getDAO(PhotoTaskDAO.class).getBackendStatus(taskId, null);
		if(BackendStatusList.isEmpty(tBackends)){
			LOGGER.warn("No back-ends for the given task, or the task does not exist. Task id: "+taskId);
			return;
		}
		
		List<AnalysisBackend> backends = ServiceInitializer.getDAOHandler().getDAO(BackendDAO.class).getBackends(Capability.BACKEND_FEEDBACK); // get list of back-ends with compatible capabilities
		if(backends == null){
			LOGGER.debug("No capable back-ends for back-end feedback.");
			return;
		}

		BackendStatusList statuses = new BackendStatusList();
		for(AnalysisBackend backend : backends){
			Integer id = backend.getBackendId();
			if(id.equals(backendId)){ // ignore the given back-end id
				LOGGER.debug("Ignoring the back-end id of task results, back-end id: "+backendId+", task, id: "+taskId);
			}else if(tBackends.getBackendStatus(id) != null){ // and all back-ends not part of the task
				statuses.setBackendStatus(new BackendStatus(backend, TaskStatus.NOT_STARTED));
			}
		}
		if(BackendStatusList.isEmpty(statuses)){
			LOGGER.debug("No capable back-ends for back-end feedback.");
			return;
		}
		
		PhotoTaskDetails details = (new service.tut.pori.contentanalysis.PhotoBackendFeedbackTask.FeedbackTaskBuilder(TaskType.BACKEND_FEEDBACK))
				.setBackends(statuses)
				.addPhotos(photos)
				.build();
		Map<String, String> metadata = new HashMap<>(1);
		metadata.put(Definitions.METADATA_RELATED_TASK_ID, taskId.toString());
		details.setMetadata(metadata);
		
		scheduleTask(details);
	}
}
