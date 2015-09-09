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
package service.tut.pori.contentanalysis.reference;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;

import service.tut.pori.contentanalysis.AnalysisBackend;
import service.tut.pori.contentanalysis.AsyncTask.TaskStatus;
import service.tut.pori.contentanalysis.AsyncTask.TaskType;
import service.tut.pori.contentanalysis.BackendStatus;
import service.tut.pori.contentanalysis.BackendStatusList;
import service.tut.pori.contentanalysis.CAContentCore.ServiceType;
import service.tut.pori.contentanalysis.CAContentCore.Visibility;
import service.tut.pori.contentanalysis.Definitions;
import service.tut.pori.contentanalysis.DeletedPhotoList;
import service.tut.pori.contentanalysis.DissimilarPhotoList;
import service.tut.pori.contentanalysis.MediaObject;
import service.tut.pori.contentanalysis.MediaObjectList;
import service.tut.pori.contentanalysis.Photo;
import service.tut.pori.contentanalysis.PhotoFeedbackList;
import service.tut.pori.contentanalysis.PhotoList;
import service.tut.pori.contentanalysis.PhotoParameters;
import service.tut.pori.contentanalysis.PhotoParameters.AnalysisType;
import service.tut.pori.contentanalysis.PhotoTaskDetails;
import service.tut.pori.contentanalysis.PhotoTaskResponse;
import service.tut.pori.contentanalysis.ReferencePhotoList;
import service.tut.pori.contentanalysis.ResultInfo;
import service.tut.pori.contentanalysis.SimilarPhotoList;
import service.tut.pori.contentanalysis.VisualShape;
import core.tut.pori.context.ServiceInitializer;
import core.tut.pori.http.RedirectResponse;
import core.tut.pori.http.Response;
import core.tut.pori.http.parameters.DataGroups;
import core.tut.pori.http.parameters.Limits;
import core.tut.pori.users.UserIdentity;
import core.tut.pori.utils.XMLFormatter;

/**
 * The reference implementations for Content Analysis Service.
 *
 */
public final class CAReferenceCore {
	private static final CAXMLObjectCreator CREATOR = new CAXMLObjectCreator(null);
	private static final Limits DEFAULT_LIMITS = new Limits(0, 0);	// default number of limits for references
	private static final DataGroups DATAGROUPS_ALL = new DataGroups(DataGroups.DATA_GROUP_ALL);
	private static final DataGroups DATAGROUPS_BACKEND_RESPONSE = new DataGroups(CAXMLObjectCreator.DATA_GROUP_BACKEND_RESPONSE);	// data groups for add task callback
	private static final String EXAMPLE_URI = "http://otula.pori.tut.fi/d2i/leijona_album_art.jpg";
	private static final Logger LOGGER = Logger.getLogger(CAReferenceCore.class);
	
	/**
	 * 
	 */
	private CAReferenceCore(){
		// nothing needed
	}

	/**
	 * This performs a trivial check for the task contents, checking for the presence of a few key values.
	 * The validity of the actual task contents will not checked.
	 * 
	 * @param response
	 */
	public static void taskFinished(PhotoTaskResponse response) {
		Integer tBackendId = response.getBackendId();
		if(tBackendId == null){
			throw new IllegalArgumentException("Invalid backendId: "+tBackendId);
		}
		Long tTaskId = response.getTaskId();
		if(tTaskId == null){
			throw new IllegalArgumentException("Invalid taskId: "+tTaskId);
		}

		TaskStatus status = response.getStatus();
		if(status == null){
			throw new IllegalArgumentException("TaskStatus is invalid or missing.");
		}

		TaskType type = response.getTaskType();
		if(type == null){
			throw new IllegalArgumentException("TaskType is invalid or missing.");
		}

		try{
			switch(type){
				case ANALYSIS:
					PhotoList pl = response.getPhotoList();
					if(!PhotoList.isEmpty(pl)){
						if(!PhotoList.isValid(pl)){
							LOGGER.warn("Invalid "+Definitions.ELEMENT_PHOTOLIST);
						}
						for(Photo p : response.getPhotoList().getPhotos()){
							MediaObjectList vObjects = p.getMediaObjects();
							if(MediaObjectList.isEmpty(vObjects)){
								LOGGER.info("No media objects for photo, GUID: "+p.getGUID());
							}else if(!MediaObjectList.isValid(vObjects)){
								throw new IllegalArgumentException("Invalid "+Definitions.ELEMENT_MEDIA_OBJECTLIST);
							}
						}
					}
					break;
				case BACKEND_FEEDBACK: // should not have any content, so accept anything
					break;
				case FEEDBACK:
					if(!PhotoList.isValid(response.getPhotoList())){
						throw new IllegalArgumentException("Invalid "+Definitions.ELEMENT_PHOTOLIST);
					}
					break;
				case SEARCH:
				default:			
					throw new IllegalArgumentException("Tasks of type: "+type.name()+" are not supported by this validator.");
			}
		}catch(ClassCastException ex){
			LOGGER.debug(ex, ex);
			throw new IllegalArgumentException("Task content data was not of the expected type.");
		}
	}

	/**
	 * This performs a trivial check for the task contents, checking for the presence of a few key values.
	 * The validity of the actual task contents will not be checked.
	 * 
	 * @param taskDetails
	 */
	public static void addTask(PhotoTaskDetails taskDetails) {
		Integer tBackendId = taskDetails.getBackendId();
		if(tBackendId == null){
			throw new IllegalArgumentException("Invalid backendId: "+tBackendId);
		}
		Long tTaskId = taskDetails.getTaskId();
		if(tTaskId == null){
			throw new IllegalArgumentException("Invalid taskId: "+tTaskId);
		}

		String uri = taskDetails.getCallbackUri();
		if(StringUtils.isBlank(uri)){
			throw new IllegalArgumentException("Invalid callbackUri: "+uri);
		}

		TaskType type = taskDetails.getTaskType();
		if(type == null){
			throw new IllegalArgumentException("TaskType is invalid or missing.");
		}

		switch(type){
			case BACKEND_FEEDBACK:
				if(!PhotoList.isValid(taskDetails.getPhotoList())){
					throw new IllegalArgumentException("Invalid "+Definitions.ELEMENT_PHOTOLIST);
				}
				addTaskAsyncCallback(taskDetails, null);
				break;
			case ANALYSIS:
				PhotoList photoList = taskDetails.getPhotoList();
				if(!PhotoList.isValid(photoList)){
					throw new IllegalArgumentException("Invalid "+Definitions.ELEMENT_PHOTOLIST);
				}
				if(taskDetails.getSimilarPhotoList() != null || taskDetails.getDissimilarPhotoList() != null || taskDetails.getDeletedPhotoList() != null){
					throw new IllegalArgumentException(Definitions.ELEMENT_SIMILAR_PHOTOLIST+", "+Definitions.ELEMENT_DISSIMILAR_PHOTOLIST+" or "+Definitions.ELEMENT_DELETED_PHOTOLIST+" cannot appear in tasks of type: "+TaskType.ANALYSIS.name()+" or "+TaskType.SEARCH.name());
				}
				PhotoParameters ap = taskDetails.getTaskParameters();
				for(Photo photo : photoList.getPhotos()){
					MediaObjectList mediaObjects = CREATOR.createMediaObjectList((ap == null ? null : ap.getAnalysisTypes()), DATAGROUPS_BACKEND_RESPONSE, DEFAULT_LIMITS, null);
					for(service.tut.pori.contentanalysis.MediaObject o : mediaObjects.getMediaObjects()){
						o.setOwnerUserId(null);
						o.setBackendId(tBackendId);
						o.setMediaObjectId(null);
						o.setServiceType(null);
					}
					photo.addackendStatus(new BackendStatus(new AnalysisBackend(tBackendId), TaskStatus.COMPLETED));
					photo.addMediaObjects(mediaObjects);
				}
				addTaskAsyncCallback(taskDetails, photoList);
				break;
			case FEEDBACK:
				if(taskDetails.getReferencePhotoList() != null){
					if(taskDetails.getSimilarPhotoList() == null && taskDetails.getDissimilarPhotoList() == null){
						throw new IllegalArgumentException(Definitions.ELEMENT_REFERENCE_PHOTOLIST+" requires at least one of "+Definitions.ELEMENT_SIMILAR_PHOTOLIST+" or "+Definitions.ELEMENT_DISSIMILAR_PHOTOLIST);
					}
					if(taskDetails.getDeletedPhotoList() != null){
						throw new IllegalArgumentException(Definitions.ELEMENT_DELETED_PHOTOLIST+" cannot appear together with "+Definitions.ELEMENT_REFERENCE_PHOTOLIST);
					}
					if(taskDetails.getPhotoList() != null){
						throw new IllegalArgumentException(Definitions.ELEMENT_PHOTOLIST+" cannot appear together with "+Definitions.ELEMENT_REFERENCE_PHOTOLIST);
					}
				}else if(taskDetails.getPhotoList() != null){
					if(taskDetails.getSimilarPhotoList() != null || taskDetails.getDissimilarPhotoList() != null || taskDetails.getDeletedPhotoList() != null){
						throw new IllegalArgumentException(Definitions.ELEMENT_PHOTOLIST+" cannot appear together with "+Definitions.ELEMENT_DELETED_PHOTOLIST+", "+Definitions.ELEMENT_SIMILAR_PHOTOLIST+" or "+Definitions.ELEMENT_DISSIMILAR_PHOTOLIST);
					}
				}else if(taskDetails.getDeletedPhotoList() != null){
					if(taskDetails.getSimilarPhotoList() != null || taskDetails.getDissimilarPhotoList() != null){
						throw new IllegalArgumentException(Definitions.ELEMENT_DELETED_PHOTOLIST+" cannot appear together with "+Definitions.ELEMENT_SIMILAR_PHOTOLIST+" or "+Definitions.ELEMENT_DISSIMILAR_PHOTOLIST);
					}
				}else{
					throw new IllegalArgumentException("At least one of "+Definitions.ELEMENT_PHOTOLIST+", "+ Definitions.ELEMENT_REFERENCE_PHOTOLIST+", "+Definitions.ELEMENT_DELETED_PHOTOLIST+", "+Definitions.ELEMENT_SIMILAR_PHOTOLIST+" or "+Definitions.ELEMENT_DISSIMILAR_PHOTOLIST+" must be present");
				}

				if(taskDetails.getPhotoList() != null && !PhotoList.isValid(taskDetails.getPhotoList())){
					throw new IllegalArgumentException("Invalid "+Definitions.ELEMENT_PHOTOLIST);
				}

				if(taskDetails.getSimilarPhotoList() != null && !SimilarPhotoList.isValid(taskDetails.getSimilarPhotoList())){
					throw new IllegalArgumentException("Invalid "+Definitions.ELEMENT_SIMILAR_PHOTOLIST);
				}

				if(taskDetails.getDissimilarPhotoList() != null && !DissimilarPhotoList.isValid(taskDetails.getDissimilarPhotoList())){
					throw new IllegalArgumentException("Invalid "+Definitions.ELEMENT_DISSIMILAR_PHOTOLIST);
				}

				if(taskDetails.getDeletedPhotoList() != null && !DeletedPhotoList.isValid(taskDetails.getDeletedPhotoList())){
					throw new IllegalArgumentException("Invalid "+Definitions.ELEMENT_DELETED_PHOTOLIST);
				}
				addTaskAsyncCallback(taskDetails, null);
				break;
			case SEARCH:
				LOGGER.warn("Accepting task of type "+TaskType.SEARCH.name()+" without validation.");
				break;
			default:
				throw new IllegalArgumentException("Tasks of type: "+type.name()+" are not supported by this validator.");
		}
	}

	/**
	 * Call asynchronously the callback given in the details, returning an example task response
	 * 
	 * @param details
	 * @param photoList
	 * @see service.tut.pori.contentanalysis.PhotoTaskResponse
	 */
	public static void addTaskAsyncCallback(PhotoTaskDetails details, PhotoList photoList) {
		HttpPost post = new HttpPost(details.getCallbackUri());
		PhotoTaskResponse r = new PhotoTaskResponse();
		r.setBackendId(details.getBackendId());
		r.setTaskId(details.getTaskId());
		r.setStatus(TaskStatus.COMPLETED);
		r.setTaskType(details.getTaskType());
		r.setPhotoList(photoList);
		post.setEntity(new StringEntity((new XMLFormatter()).toString(r), core.tut.pori.http.Definitions.ENCODING_UTF8));
		executeAsyncCallback(post);
	}

	/**
	 * 
	 * @param post
	 */
	public static void executeAsyncCallback(final HttpPost post){
		ServiceInitializer.getExecutorHandler().getExecutor().execute(
			new Runnable() {
				@Override
				public void run() {
					try (CloseableHttpClient client = HttpClients.createDefault()) {
						LOGGER.debug("Waiting "+service.tut.pori.contentanalysis.reference.Definitions.ASYNC_CALLBACK_DELAY/1000+" seconds...");
						Thread.sleep(service.tut.pori.contentanalysis.reference.Definitions.ASYNC_CALLBACK_DELAY);
	
						LOGGER.debug("Calling uri: "+post.getURI().toString());
						LOGGER.debug("Server responded: "+client.execute(post, new BasicResponseHandler()));
					} catch (IOException | InterruptedException ex) {
						LOGGER.error(ex, ex);
					}
				}
		});
	}

	/**
	 * 
	 * @param backendId
	 * @param taskId
	 * @param dataGroups
	 * @param limits
	 * @return an example response for the given values
	 */
	public static Response queryTaskDetails(Integer backendId, Long taskId, DataGroups dataGroups, Limits limits) {
		if(limits.getMaxItems(Definitions.ELEMENT_PHOTOLIST) >= Limits.DEFAULT_MAX_ITEMS){
			LOGGER.debug("Reseting limits for "+Definitions.ELEMENT_PHOTOLIST+": max items was "+Limits.DEFAULT_MAX_ITEMS);
			limits.setTypeLimits(DEFAULT_LIMITS.getStartItem(Definitions.ELEMENT_PHOTOLIST), DEFAULT_LIMITS.getEndItem(Definitions.ELEMENT_PHOTOLIST), Definitions.ELEMENT_PHOTOLIST); // startItem makes no difference for random
		}

		if(limits.getMaxItems(Definitions.ELEMENT_MEDIA_OBJECTLIST) >= Limits.DEFAULT_MAX_ITEMS){
			LOGGER.debug("Reseting limits for "+Definitions.ELEMENT_MEDIA_OBJECTLIST+": max items was "+Limits.DEFAULT_MAX_ITEMS);
			limits.setTypeLimits(DEFAULT_LIMITS.getStartItem(Definitions.ELEMENT_MEDIA_OBJECTLIST), DEFAULT_LIMITS.getEndItem(Definitions.ELEMENT_MEDIA_OBJECTLIST), Definitions.ELEMENT_MEDIA_OBJECTLIST); // startItem makes no difference for random
		}
		limits.setTypeLimits(-1, -1, null); // disable all other photo lists
		return new Response(CREATOR.createPhotoTaskDetails(backendId, dataGroups, limits, taskId, TaskType.ANALYSIS));
	}

	/**
	 * 
	 * @param taskId
	 * @param dataGroups
	 * @param limits
	 * @return an example response for the given values
	 */
	public static Response queryTaskStatus(Long taskId, DataGroups dataGroups, Limits limits) {
		if(limits.getMaxItems() >= Limits.DEFAULT_MAX_ITEMS){
			LOGGER.debug("Reseting limits: Default max items was "+Limits.DEFAULT_MAX_ITEMS);
			limits = DEFAULT_LIMITS; // startItem makes no difference for random
		}
		return new Response(CREATOR.createTaskResponse(null, dataGroups, limits, taskId, TaskType.ANALYSIS));
	}

	/**
	 * back-end API variant
	 * @param analysisTypes 
	 * @param serviceTypes
	 * @param url
	 * @param userIds
	 * @param dataGroups
	 * @param limits
	 * @return an example response for the given values
	 */
	public static Response searchSimilarByContent(EnumSet<AnalysisType> analysisTypes, String url, DataGroups dataGroups, Limits limits, EnumSet<ServiceType> serviceTypes, long[] userIds) {
		if(limits.getMaxItems() >= Limits.DEFAULT_MAX_ITEMS){
			LOGGER.debug("Reseting limits: Default max items was "+Limits.DEFAULT_MAX_ITEMS);
			limits = DEFAULT_LIMITS; // startItem makes no difference for random
		}
		return new Response(CREATOR.createSearchResults(null, dataGroups, limits, serviceTypes, userIds, null));
	}

	/**
	 * Back-end API variation of search by GUID
	 * 
	 * @param analysisTypes not used
	 * @param serviceTypes
	 * @param guid
	 * @param userIds
	 * @param dataGroups
	 * @param limits
	 * @return an example response for the given values
	 */
	public static Response searchSimilarById(EnumSet<AnalysisType> analysisTypes, String guid, DataGroups dataGroups, Limits limits, EnumSet<ServiceType> serviceTypes, long[] userIds) {
		if(limits.getMaxItems() >= Limits.DEFAULT_MAX_ITEMS){
			LOGGER.debug("Reseting limits: Default max items was "+Limits.DEFAULT_MAX_ITEMS);
			limits = DEFAULT_LIMITS; // startItem makes no difference for random
		}
		return new Response(CREATOR.createSearchResults(guid, dataGroups, limits, serviceTypes, userIds, null));
	}

	/**
	 * 
	 * @param authenticatedUser
	 * @param keywords
	 * @param dataGroups
	 * @param limits
	 * @param serviceTypes
	 * @param userIdFilters
	 * @return an example response for the given values
	 */
	public static Response searchByKeyword(UserIdentity authenticatedUser, List<String> keywords, DataGroups dataGroups, Limits limits, EnumSet<ServiceType> serviceTypes, long[] userIdFilters) {
		LOGGER.info((authenticatedUser == null ? "No logged in user." : "Ignoring the logged in user, id: "+authenticatedUser.getUserId()));	// only notify of the logged in status
		if(limits.getMaxItems() >= Limits.DEFAULT_MAX_ITEMS){
			LOGGER.debug("Reseting limits: Default max items was "+Limits.DEFAULT_MAX_ITEMS);
			limits = DEFAULT_LIMITS; // startItem makes no difference for random
		}
		return new Response(CREATOR.createSearchResults(null, dataGroups, limits, serviceTypes, userIdFilters, MediaObjectList.getMediaObjectListFromKeywords(keywords)));
	}

	/**
	 * Client API variation of search by GUID
	 * 
	 * @param authenticatedUser
	 * @param analysisTypes
	 * @param guid
	 * @param dataGroups
	 * @param limits
	 * @param serviceTypes
	 * @param userIdFilters
	 * @return an example response for the given values
	 */
	public static Response searchSimilarById(UserIdentity authenticatedUser, EnumSet<AnalysisType> analysisTypes, String guid, DataGroups dataGroups, Limits limits, EnumSet<ServiceType> serviceTypes, long[] userIdFilters) {
		LOGGER.info((authenticatedUser == null ? "No logged in user." : "Ignoring the logged in user, id: "+authenticatedUser.getUserId()));	// only notify of the logged in status
		return searchSimilarById(analysisTypes, guid, dataGroups, limits, serviceTypes, userIdFilters);	// we can directly call the back-end API reference implementation
	}

	/**
	 * 
	 * @param authenticatedUser
	 * @param serviceId
	 * @param guid
	 * @return an example response for the given values
	 */
	public static RedirectResponse generateTargetUrl(UserIdentity authenticatedUser, ServiceType serviceId, String guid) {
		LOGGER.info((authenticatedUser == null ? "No logged in user." : "Ignoring the logged in user, id: "+authenticatedUser.getUserId()));	// only notify of the logged in status
		return new RedirectResponse(EXAMPLE_URI);
	}

	/**
	 * 
	 * @param authenticatedUser
	 * @param objects
	 * @param dataGroups
	 * @param limits
	 * @param serviceTypes
	 * @param userIdFilters
	 * @return an example response for the given values
	 */
	public static Response similarPhotosByObject(UserIdentity authenticatedUser, MediaObjectList objects, DataGroups dataGroups, Limits limits, EnumSet<ServiceType> serviceTypes, long[] userIdFilters) {
		LOGGER.info((authenticatedUser == null ? "No logged in user." : "Ignoring the logged in user, id: "+authenticatedUser.getUserId()));	// only notify of the logged in status
		if(limits.getMaxItems(Definitions.ELEMENT_PHOTOLIST) >= Limits.DEFAULT_MAX_ITEMS){
			LOGGER.debug("Reseting limits: Default max items was "+Limits.DEFAULT_MAX_ITEMS);
			limits = DEFAULT_LIMITS; // startItem makes no difference for random
		}
		return new Response(CREATOR.createSearchResults(null, dataGroups, limits, serviceTypes, userIdFilters, objects));
	}

	/**
	 * 
	 * @param photoList
	 * @param authenticatedUser
	 */
	public static void updatePhotos(UserIdentity authenticatedUser, PhotoList photoList) {
		LOGGER.info((authenticatedUser == null ? "No logged in user." : "Ignoring the logged in user, id: "+authenticatedUser.getUserId()));	// only notify of the logged in status
		if(!PhotoList.isValid(photoList)){
			throw new IllegalArgumentException("Received empty or invalid photoList.");
		}
	}

	/**
	 * client API variant
	 * 
	 * @param url
	 * @param serviceTypes
	 * @param authenticatedUser
	 * @param analysisTypes 
	 * @param userIdFilters
	 * @param dataGroups
	 * @param limits
	 * @return an example response for the given values
	 */
	public static Response searchByContent(UserIdentity authenticatedUser, EnumSet<AnalysisType> analysisTypes, String url, DataGroups dataGroups, Limits limits, EnumSet<ServiceType> serviceTypes, long[] userIdFilters) {
		LOGGER.info((authenticatedUser == null ? "No logged in user." : "Ignoring the logged in user, id: "+authenticatedUser.getUserId()));	// only notify of the logged in status
		if(limits.getMaxItems(Definitions.ELEMENT_PHOTOLIST) >= Limits.DEFAULT_MAX_ITEMS){
			LOGGER.debug("Reseting limits: Default max items was "+Limits.DEFAULT_MAX_ITEMS);
			limits = DEFAULT_LIMITS; // startItem makes no difference for random
		}
		return searchSimilarByContent(analysisTypes, url, dataGroups, limits, serviceTypes, userIdFilters);	// we can use the back-end variant
	}

	/**
	 * 
	 * @param authenticatedUser
	 * @param guids
	 * @param dataGroups
	 * @param limits
	 * @param serviceTypes
	 * @param userIdFilter 
	 * @return an example response for the given values
	 */
	public static Response getPhotos(UserIdentity authenticatedUser, List<String> guids, DataGroups dataGroups, Limits limits, EnumSet<ServiceType> serviceTypes, long[] userIdFilter) {
		LOGGER.info((authenticatedUser == null ? "No logged in user." : "Ignoring the logged in user, id: "+authenticatedUser.getUserId()));	// only notify of the logged in status
		if(limits.getMaxItems(Definitions.ELEMENT_PHOTOLIST) >= Limits.DEFAULT_MAX_ITEMS){
			LOGGER.debug("Reseting limits: Default max items was "+Limits.DEFAULT_MAX_ITEMS);
			limits = DEFAULT_LIMITS; // startItem makes no difference for random
		}
		int userIdCount = (ArrayUtils.isEmpty(userIdFilter) ? 0 : userIdFilter.length);
		PhotoList list = CREATOR.createPhotoList(null, dataGroups, limits, serviceTypes, null);
		if(list != null && guids != null && !guids.isEmpty()){
			for(Iterator<Photo> iter = list.getPhotos().iterator();iter.hasNext();){	// remove all extra GUIDs, we could also modify the limit parameter, but for this testing method, the implementation does not matter
				Photo photo = iter.next();
				if(guids.isEmpty()){	// we have used up all given GUIDs
					iter.remove();
				}else{
					photo.setGUID(guids.remove(0));
					photo.setVisibility(Visibility.PUBLIC); // there could also be private photos for the authenticated user, but to make sure the results are valid, return only PUBLIC photos
				}
				if(userIdCount > 1){
					photo.setOwnerUserId(new UserIdentity(userIdFilter[CREATOR.getRandom().nextInt(userIdCount)]));
				}
			}	// for
		}
		return new Response(list);
	}

	/**
	 * 
	 * @param authenticatedUser
	 * @param feedbackList
	 */
	public static void similarityFeedback(UserIdentity authenticatedUser, PhotoFeedbackList feedbackList) {
		LOGGER.info((authenticatedUser == null ? "No logged in user." : "Ignoring the logged in user, id: "+authenticatedUser.getUserId()));	// only notify of the logged in status
		if(!PhotoFeedbackList.isValid(feedbackList)){
			throw new IllegalArgumentException("Received empty or invalid feedbackList.");
		}
	}

	/**
	 * 
	 * @param authenticatedUser
	 * @param dataGroups
	 * @param limits
	 * @param serviceTypes
	 * @param mediaObjectIds 
	 * @return an example response for the given values
	 */
	public static Response getMediaObjects(UserIdentity authenticatedUser, DataGroups dataGroups, Limits limits, EnumSet<ServiceType> serviceTypes, List<String> mediaObjectIds) {
		LOGGER.info((authenticatedUser == null ? "No logged in user." : "Ignoring the logged in user, id: "+authenticatedUser.getUserId()));	// only notify of the logged in status

		if(limits.getMaxItems(Definitions.ELEMENT_MEDIA_OBJECTLIST) >= Limits.DEFAULT_MAX_ITEMS){
			LOGGER.debug("Reseting limits for "+Definitions.ELEMENT_MEDIA_OBJECTLIST+": max items was "+Limits.DEFAULT_MAX_ITEMS);
			limits.setTypeLimits(DEFAULT_LIMITS.getStartItem(Definitions.ELEMENT_MEDIA_OBJECTLIST), DEFAULT_LIMITS.getEndItem(Definitions.ELEMENT_MEDIA_OBJECTLIST), Definitions.ELEMENT_MEDIA_OBJECTLIST); // startItem makes no difference for random
		}

		MediaObjectList mediaObjects = CREATOR.createMediaObjectList(null, dataGroups, limits, serviceTypes);
		if(!MediaObjectList.isEmpty(mediaObjects) && mediaObjectIds != null && !mediaObjectIds.isEmpty()){
			Iterator<String> voidIter = mediaObjectIds.iterator();
			for(Iterator<MediaObject> voIter = mediaObjects.getMediaObjects().iterator(); voIter.hasNext();){
				MediaObject vo = voIter.next();
				if(voidIter.hasNext()){
					vo.setMediaObjectId(voidIter.next());
				}else{
					voIter.remove(); // we have used all available media object ids
				}
			} // for
		}
		return new Response(mediaObjects);
	}

	/**
	 * 
	 * @param authenticatedUser
	 * @param guids
	 */
	public static void deletePhotos(UserIdentity authenticatedUser, List<String> guids) {
		LOGGER.info((authenticatedUser == null ? "No logged in user." : "Ignoring the logged in user, id: "+authenticatedUser.getUserId()));	// only notify of the logged in status
	}

	/**
	 * 
	 * @param limits
	 * @return generated feedback list
	 */
	public static PhotoFeedbackList generateFeedbackList(Limits limits) {
		return CREATOR.createFeedbackList(limits);
	}

	/**
	 * 
	 * @param dataGroups has only effect for photo list, if null or empty, data groups {@value core.tut.pori.http.parameters.DataGroups#DATA_GROUP_ALL}
	 * @param limits
	 * @param cls 
	 * @return generated photo list
	 * @throws IllegalArgumentException 
	 */
	public static PhotoList generatePhotoList(DataGroups dataGroups, Limits limits, Class<? extends PhotoList> cls) throws IllegalArgumentException {
		if(PhotoList.class == cls){
			return CREATOR.createPhotoList(null, (DataGroups.isEmpty(dataGroups) ? DATAGROUPS_ALL : dataGroups), limits, null, null);
		}else if(DeletedPhotoList.class == cls){
			return CREATOR.createDeletedPhotoList(limits);
		}else if(DissimilarPhotoList.class == cls){
			return CREATOR.createDissimilarPhotoList(limits);
		}else if(SimilarPhotoList.class == cls){
			return CREATOR.createSimilarPhotoList(limits);
		}else if(ReferencePhotoList.class == cls){
			return CREATOR.createReferencePhotoList(limits);
		}else{
			throw new IllegalArgumentException("Unsupported class : "+cls);
		}
	}

	/**
	 * 
	 * @param dataGroups has only effect for photo list, if null or empty, data groups {@value core.tut.pori.http.parameters.DataGroups#DATA_GROUP_ALL}
	 * @param limits
	 * @return generated media object list
	 */
	public static MediaObjectList generateMediaObjectList(DataGroups dataGroups, Limits limits) {
		return CREATOR.createMediaObjectList(null, (DataGroups.isEmpty(dataGroups) ? DATAGROUPS_ALL : dataGroups), limits, null);
	}

	/**
	 * 
	 * @param limits
	 * @return generated task response
	 */
	public static PhotoTaskResponse generateTaskResponse(Limits limits) {
		return CREATOR.createTaskResponse(null, DATAGROUPS_BACKEND_RESPONSE, limits, null, TaskType.ANALYSIS);
	}

	/**
	 * 
	 * @param dataGroups has only effect for photo list, if null or empty, data groups {@value core.tut.pori.http.parameters.DataGroups#DATA_GROUP_ALL}
	 * @param limits
	 * @param taskType 
	 * @return generated task details
	 * @throws IllegalArgumentException 
	 */
	public static PhotoTaskDetails generatePhotoTaskDetails(DataGroups dataGroups, Limits limits, TaskType taskType) throws IllegalArgumentException {
		switch(taskType){
			case ANALYSIS:
			case BACKEND_FEEDBACK:
			case FEEDBACK:
				break;
			default:
				throw new IllegalArgumentException("Unsupported task type: "+taskType.name());
		}
		limits.setTypeLimits(-1, -1, Definitions.ELEMENT_BACKEND_STATUS_LIST); // do not add back-end status list
		return CREATOR.createPhotoTaskDetails((DataGroups.isEmpty(dataGroups) ? DATAGROUPS_ALL : dataGroups), limits, taskType);
	}

	/**
	 * 
	 * @param limits
	 * @return generated back-end status list
	 */
	public static BackendStatusList generateBackendStatusList(Limits limits) {
		int count = limits.getMaxItems();
		if(count >= Limits.DEFAULT_MAX_ITEMS){
			LOGGER.debug("Reseting limits: Default max items was "+Limits.DEFAULT_MAX_ITEMS);
			count = DEFAULT_LIMITS.getMaxItems(Definitions.ELEMENT_BACKEND_STATUS_LIST); // startItem makes no difference for random
		}
		return CREATOR.createBackendStatusContainer(count);
	}

	/**
	 * 
	 * @param dataGroups has only effect for photo list, if null or empty, data groups {@value core.tut.pori.http.parameters.DataGroups#DATA_GROUP_ALL}
	 * @return generated photo
	 */
	public static Photo generatePhoto(DataGroups dataGroups) {
		return CREATOR.createPhoto(null, (DataGroups.isEmpty(dataGroups) ? DATAGROUPS_ALL : dataGroups), DEFAULT_LIMITS, null, null);
	}

	/**
	 * 
	 * @return generated back-end status
	 */
	public static BackendStatus generateBackendStatus() {
		return CREATOR.createBackendStatus(CREATOR.createBackendId());
	}

	/**
	 * 
	 * @return generated result info
	 */
	public static ResultInfo generateResultInfo() {
		return CREATOR.createResultInfo();
	}

	/**
	 * 
	 * @param dataGroups has only effect for photo list, if null or empty, data groups {@value core.tut.pori.http.parameters.DataGroups#DATA_GROUP_ALL}
	 * @return generated media object
	 */
	public static MediaObject generateMediaObject(DataGroups dataGroups) {
		return CREATOR.createMediaObject(null, (DataGroups.isEmpty(dataGroups) ? DATAGROUPS_ALL : dataGroups), null);
	}

	/**
	 * 
	 * @return generated visual shape
	 */
	public static VisualShape generateVisualShape() {
		return CREATOR.createVisualShape();
	}

	/**
	 * 
	 * @return generated analysis back-end
	 */
	public static AnalysisBackend generateAnalysisBackend() {
		return CREATOR.createAnalysisBackend();
	}

	/**
	 * 
	 * @return randomly generated analysis parameters
	 */
	public static PhotoParameters generateAnalysisParameters() {
		return CREATOR.createAnalysisParameters();
	}
}
