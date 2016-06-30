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
package service.tut.pori.contentanalysis.reference;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.sql.DataSource;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.datasource.DataSourceUtils;

import service.tut.pori.contentanalysis.AbstractTaskDetails;
import service.tut.pori.contentanalysis.AnalysisBackend;
import service.tut.pori.contentanalysis.AnalysisBackend.Capability;
import service.tut.pori.contentanalysis.PhotoParameters.AnalysisType;
import service.tut.pori.contentanalysis.PhotoParameters;
import service.tut.pori.contentanalysis.AsyncTask.TaskStatus;
import service.tut.pori.contentanalysis.AsyncTask.TaskType;
import service.tut.pori.contentanalysis.BackendStatus;
import service.tut.pori.contentanalysis.BackendStatusList;
import service.tut.pori.contentanalysis.CAContentCore.ServiceType;
import service.tut.pori.contentanalysis.CAContentCore.Visibility;
import service.tut.pori.contentanalysis.Definitions;
import service.tut.pori.contentanalysis.DeletedPhotoList;
import service.tut.pori.contentanalysis.DissimilarPhotoList;
import service.tut.pori.contentanalysis.PhotoFeedbackList;
import service.tut.pori.contentanalysis.Photo;
import service.tut.pori.contentanalysis.PhotoList;
import service.tut.pori.contentanalysis.PhotoTaskDetails;
import service.tut.pori.contentanalysis.PhotoTaskResponse;
import service.tut.pori.contentanalysis.ReferencePhotoList;
import service.tut.pori.contentanalysis.ResultInfo;
import service.tut.pori.contentanalysis.SimilarPhotoList;
import service.tut.pori.contentanalysis.MediaObject;
import service.tut.pori.contentanalysis.MediaObject.ConfirmationStatus;
import service.tut.pori.contentanalysis.MediaObject.MediaObjectType;
import service.tut.pori.contentanalysis.MediaObjectList;
import service.tut.pori.contentanalysis.VisualShape;
import service.tut.pori.contentanalysis.VisualShape.VisualShapeType;
import core.tut.pori.context.ServiceInitializer;
import core.tut.pori.http.parameters.DataGroups;
import core.tut.pori.http.parameters.Limits;
import core.tut.pori.users.UserIdentity;
import core.tut.pori.utils.MediaUrlValidator.MediaType;

/**
 * 
 * Class that can be used to created example objects/object lists.
 *
 */
public class CAXMLObjectCreator {
	/** special datagroup used for testing, creates tags with data generally returned by an analysis backend */
	public static final String DATA_GROUP_BACKEND_RESPONSE = "backend_response";
	private static final int BACKEND_STATUS_MAX = 5;
	private static final Limits LIMITS_NO_MEDIA_OBJECTS;
	static{
		LIMITS_NO_MEDIA_OBJECTS = new Limits(0,0);
		LIMITS_NO_MEDIA_OBJECTS.setTypeLimits(0, -1, Definitions.ELEMENT_MEDIA_OBJECTLIST);
	}
	private static final Logger LOGGER = Logger.getLogger(CAXMLObjectCreator.class);
	private static final int TEXT_LENGTH = 64;
	private Random _random = null;
	private long _seed = 0;

	/**
	 * 
	 * @param seed for random generator, or null to use default (system time in nanoseconds)
	 */
	public CAXMLObjectCreator(Long seed){
		if(seed == null){
			seed = System.nanoTime();
		}
		_seed = seed;
		_random = new Random(seed);
	}

	/**
	 * @return the seed
	 */
	public long getSeed() {
		return _seed;
	}

	/**
	 * @return the random
	 */
	public Random getRandom() {
		return _random;
	}

	/**
	 * TaskType will not be set if already set
	 * @param backendId
	 * @param details
	 * @param taskId 
	 * @param taskType if null, generated randomly
	 */
	public void populateAbstractTaskDetails(Integer backendId, AbstractTaskDetails details, Long taskId, TaskType taskType){
		details.setTaskType((taskType == null ? createTaskType() : taskType));
		details.setTaskId(taskId);
		details.setBackendId(backendId);
		details.setUserId(createUserIdentity());
	}

	/**
	 * 
	 * @return randomly generated task type
	 */
	public TaskType createTaskType(){
		TaskType[] values = TaskType.values();
		return values[_random.nextInt(values.length)];
	}

	/**
	 * 
	 * @return randomly generated user identity
	 */
	public UserIdentity createUserIdentity(){
		return new UserIdentity(Math.abs(_random.nextLong()));
	}

	/**
	 * 
	 * @return randomly generated task status
	 */
	public TaskStatus createTaskStatus(){
		return TaskStatus.fromInt(_random.nextInt(5)+1);
	}

	/**
	 * 
	 * @return serviceType valid for photos (not facebook jazz)
	 */
	public ServiceType createPhotoServiceType(){
		ServiceType type = null;
		ServiceType[] types = ServiceType.values();
		do{
			type = types[_random.nextInt(types.length)];
		}while(type != ServiceType.PICASA_STORAGE_SERVICE && type != ServiceType.FACEBOOK_PHOTO && type != ServiceType.TWITTER_PHOTO);
		return type;
	}

	/**
	 * 
	 * @param backendId
	 * @return randomly generated back-end status with the given back-end id
	 */
	public BackendStatus createBackendStatus(Integer backendId){
		BackendStatus status = new BackendStatus(createAnalysisBackend(), createTaskStatus());
		status.setMessage(RandomStringUtils.randomAlphanumeric(TEXT_LENGTH));
		return status;
	}

	/**
	 * 
	 * @param statusCount
	 * @return randomly generated back-end status list with the given amount of statuses
	 */
	public BackendStatusList createBackendStatusContainer(int statusCount){
		if(statusCount < 1){
			LOGGER.warn("count < 1");
			return null;
		}
		BackendStatusList c = new BackendStatusList();
		for(int i=0;i<statusCount;++i){
			c.setBackendStatus(createBackendStatus(createBackendId()));
		}
		return c;
	}
	
	/**
	 * 
	 * @return randomly generated back-end id
	 */
	public int createBackendId(){
		return Math.abs(_random.nextInt());
	}

	/**
	 * 
	 * @return randomly generated analysis back-end
	 */
	public AnalysisBackend createAnalysisBackend(){
		AnalysisBackend end = new AnalysisBackend();
		end.setBackendId(Math.abs(_random.nextInt()));
		end.setAnalysisUri(createRandomUrl());
		end.setEnabled(_random.nextBoolean());
		end.setDescription(RandomStringUtils.randomAlphabetic(TEXT_LENGTH));
		int count = _random.nextInt(Capability.values().length);
		if(count > 0){
			EnumSet<Capability> caps = EnumSet.noneOf(Capability.class);
			for(int i=0;i<count;++i){
				caps.add(createCapability());
			}
			end.setCapabilities(caps);
		}
		DataGroups dg = new DataGroups();
		for(int i=0;i<TaskType.values().length;++i){
			for(int j=0;j<5;++j){	// take random 5 for nice not-too-small-count
				dg.addDataGroup(createDataGroup());
			}
		}
		end.setDefaultTaskDataGroups(dg);
		return end;
	}

	/**
	 * 
	 * @return randomly generated Capability
	 */
	public Capability createCapability(){
		Capability[] values = Capability.values();
		return values[_random.nextInt(values.length)];
	}

	/**
	 * 
	 * @return randomly generated data group
	 */
	public String createDataGroup(){
		try {
			Field[] fields = Definitions.class.getDeclaredFields();
			HashSet<String> groups = new HashSet<>();
			for(int i=0;i<fields.length;++i){
				if(fields[i].getName().contains("DATA_GROUP")){

					groups.add((String) fields[i].get(null));
				}
			}
			if(!groups.isEmpty()){
				int group = _random.nextInt(groups.size());
				Iterator<String> iter = groups.iterator();
				for(int i=0;i<group;++i){
					iter.next();
				}
				return iter.next();
			}
		} catch (IllegalArgumentException | IllegalAccessException ex) {
			LOGGER.error(ex, ex);
		}

		return null;
	}

	/**
	 * 
	 * @return randomly generated visual shape
	 */
	public VisualShape createVisualShape(){
		VisualShapeType type = createVisualShapeType();
		String value = null;
		switch(type){
			case CIRCLE:
				value = Math.abs(_random.nextInt())+","+ Math.abs(_random.nextInt())+","+ Math.abs(_random.nextInt());
				break;
			case POLYGON:
			case TRIANGLE:
				value = Math.abs(_random.nextInt())+","+ Math.abs(_random.nextInt())+","+ Math.abs(_random.nextInt())+","+ Math.abs(_random.nextInt())+","+ Math.abs(_random.nextInt())+","+ Math.abs(_random.nextInt());
				break;
			case RECTANGLE:
				value = Math.abs(_random.nextInt())+","+ Math.abs(_random.nextInt())+","+ Math.abs(_random.nextInt())+","+ Math.abs(_random.nextInt());
				break;
			default:
				LOGGER.error("Unknown VisulaShapeType.");
				return null;
		}
		return new VisualShape(type, value);
	}

	/**
	 * 
	 * @return randomly generated visual shape type
	 */
	public VisualShapeType createVisualShapeType(){
		VisualShapeType[] types = VisualShapeType.values();
		return types[_random.nextInt(types.length)];
	}

	/**
	 * Note that regardless of the given analysis types, this method will always return media objects of type {@link core.tut.pori.utils.MediaUrlValidator.MediaType#PHOTO}.
	 * 
	 * @param analysisTypes optional analysis types the created media object should confirm to
	 * @param dataGroups optional data groups the created media object should confirm to
	 * @param serviceTypes
	 * @return new media object or null if the given parameters prevented creating one
	 */
	public MediaObject createMediaObject(Collection<AnalysisType> analysisTypes, DataGroups dataGroups, EnumSet<ServiceType> serviceTypes){
		boolean onlyBasic = (!DataGroups.hasDataGroup(DataGroups.DATA_GROUP_ALL, dataGroups) && DataGroups.hasDataGroup(DataGroups.DATA_GROUP_BASIC, dataGroups));	// check if basic is present, and no data group all
	
		MediaObject object = new MediaObject();
		object.setMediaType(MediaType.PHOTO);
		boolean createKeywords = (analysisTypes == null || analysisTypes.contains(AnalysisType.KEYWORD_EXTRACTION));
		if(DataGroups.hasDataGroup(Definitions.DATA_GROUP_KEYWORDS, dataGroups) && createKeywords ){
			object.setMediaObjectType(MediaObjectType.KEYWORD);
			object.setValue(RandomStringUtils.randomAlphabetic(TEXT_LENGTH));
			onlyBasic = false;
		}else if(DataGroups.hasDataGroup(Definitions.DATA_GROUP_METADATA, dataGroups)){
			object.setMediaObjectType(MediaObjectType.METADATA);
			object.setValue(RandomStringUtils.randomAlphabetic(TEXT_LENGTH));
			object.setName(RandomStringUtils.randomAlphabetic(TEXT_LENGTH));
			onlyBasic = false;
		}else if(DataGroups.hasDataGroup(Definitions.DATA_GROUP_OBJECT, dataGroups)){
			LOGGER.warn("Objects of type "+MediaObjectType.OBJECT.name()+" are not supported by this method.");
			return null;
		}else if(DataGroups.hasDataGroup(Definitions.DATA_GROUP_FACE, dataGroups) && (analysisTypes == null || analysisTypes.contains(AnalysisType.FACE_DETECTION))){
			object.setMediaObjectType(MediaObjectType.FACE);
			object.setVisualShape(createVisualShape());
			object.setValue(RandomStringUtils.randomAlphabetic(TEXT_LENGTH));
			onlyBasic = false;
		}else{	// default to keyword
			object.setMediaObjectType(MediaObjectType.KEYWORD);
			object.setValue(RandomStringUtils.randomAlphabetic(TEXT_LENGTH));
		}
		
		if(DataGroups.hasDataGroup(Definitions.DATA_GROUP_CANDIDATE, dataGroups)){
			object.setConfirmationStatus(ConfirmationStatus.CANDIDATE);
			onlyBasic = false;
		}else if(DataGroups.hasDataGroup(Definitions.DATA_GROUP_USER_CONFIRMED, dataGroups)){
			object.setConfirmationStatus(ConfirmationStatus.USER_CONFIRMED);
			onlyBasic = false;
		}else if(DataGroups.hasDataGroup(Definitions.DATA_GROUP_USER_REJECTED, dataGroups)){
			object.setConfirmationStatus(ConfirmationStatus.USER_REJECTED);
			onlyBasic = false;
		}else if(DataGroups.hasDataGroup(Definitions.DATA_GROUP_BACKEND_REMOVED, dataGroups)){
			object.setConfirmationStatus(ConfirmationStatus.BACKEND_REMOVED);
			onlyBasic = false;
		}else if(createKeywords){
			object.setConfirmationStatus(createConfirmationStatus());
		}else{
			LOGGER.warn("Cannot create media object with the given parameters.");
			return null;
		}
		
		if(onlyBasic){
			LOGGER.debug("Data group "+DataGroups.DATA_GROUP_BASIC+" was given without any media object specific filters.");
			return null;
		}
		
		object.setObjectId(RandomStringUtils.randomAlphabetic(TEXT_LENGTH));
		object.setMediaObjectId(String.valueOf(Math.abs(_random.nextLong())));
		object.setBackendId(Math.abs(_random.nextInt()));
		object.setRank(Math.abs(_random.nextInt()));
		object.setConfidence(_random.nextDouble());
		object.setVisibility(createVisibility());
		if(serviceTypes == null || serviceTypes.isEmpty()){
			object.setServiceType(createPhotoServiceType());
		}else{
			object.setServiceType(getRandomServiceType(_random, serviceTypes));
		}
		object.setOwnerUserId(createUserIdentity());
		
		return object;
	}
	
	/**
	 * @param random
	 * @param serviceTypes list of service types, not null, not empty
	 * @return random service type from the set of servicetypes
	 */
	public static ServiceType getRandomServiceType(Random random, EnumSet<ServiceType> serviceTypes){
		Iterator<ServiceType> iter = serviceTypes.iterator();
		int targetIndex = random.nextInt(serviceTypes.size());
		ServiceType retval = null;
		for(int i=0;i<=targetIndex;++i){
			retval = iter.next();
		}
		return retval;
	}

	/**
	 * 
	 * @return randomly generated media object type
	 */
	public MediaObjectType createMediaObjectType(){
		MediaObjectType[] t = MediaObjectType.values();
		return t[_random.nextInt(t.length)];
	}

	/**
	 * 
	 * @return randomly generated confirmation status
	 */
	public ConfirmationStatus createConfirmationStatus(){
		ConfirmationStatus[] s = ConfirmationStatus.values();
		return s[_random.nextInt(s.length)];
	}

	/**
	 * 
	 * @param analysisTypes 
	 * @param dataGroups
	 * @param limits
	 * @param serviceTypes
	 * @return randomly generated media object list
	 */
	public MediaObjectList createMediaObjectList(Collection<AnalysisType> analysisTypes, DataGroups dataGroups, Limits limits, EnumSet<ServiceType> serviceTypes){
		int count = limits.getMaxItems(Definitions.ELEMENT_MEDIA_OBJECTLIST);
		if(count < 1){
			LOGGER.warn("count < 1");
			return null;
		}else if(count >= Limits.DEFAULT_MAX_ITEMS){
			LOGGER.debug("Count was "+Limits.DEFAULT_MAX_ITEMS+", using 1.");
			count = 1;
		}
		MediaObjectList list = new MediaObjectList();
		for(int i=0;i<count;++i){
			MediaObject v = createMediaObject(analysisTypes, dataGroups, serviceTypes);
			if(v != null){
				list.addMediaObject(v);
			}
		}
		if(DataGroups.hasDataGroup(Definitions.DATA_GROUP_RESULT_INFO, dataGroups)){
			list.setResultInfo(new ResultInfo(limits.getStartItem(Definitions.ELEMENT_MEDIA_OBJECTLIST), limits.getEndItem(Definitions.ELEMENT_MEDIA_OBJECTLIST), count));
		}
		return (MediaObjectList.isEmpty(list) ? null : list);
	}

	/**
	 * @param analysisTypes 
	 * @param dataGroups
	 * @param limits
	 * @param serviceTypes
	 * @param userIdentity 
	 * @return randomly generated photo
	 */
	public Photo createPhoto(Collection<AnalysisType> analysisTypes, DataGroups dataGroups, Limits limits, EnumSet<ServiceType> serviceTypes, UserIdentity userIdentity){
		Photo photo = new Photo();
		String guid = UUID.randomUUID().toString();
		photo.setGUID(guid);
		ServiceType serviceType = createPhotoServiceType();
		photo.setServiceType(serviceType);
		UserIdentity userId = (UserIdentity.isValid(userIdentity) ? userIdentity : createUserIdentity());
		photo.setOwnerUserId(userId);
		
		int backendStatusCount = (!DataGroups.hasDataGroup(Definitions.DATA_GROUP_BACKEND_STATUS, dataGroups) || limits == null ? 0 : limits.getMaxItems(Definitions.ELEMENT_BACKEND_STATUS_LIST));
		if(backendStatusCount > BACKEND_STATUS_MAX){
			LOGGER.warn("Back-end status count was more than "+BACKEND_STATUS_MAX+" defaulting to "+BACKEND_STATUS_MAX);
			backendStatusCount = BACKEND_STATUS_MAX;
		}

		BackendStatusList backendStatus = null;
		if(DataGroups.hasDataGroup(DataGroups.DATA_GROUP_ALL,dataGroups) || DataGroups.hasDataGroup(DataGroups.DATA_GROUP_BASIC, dataGroups)){
			photo.setCredits(RandomStringUtils.randomAlphabetic(TEXT_LENGTH));
			photo.setName(RandomStringUtils.randomAlphabetic(TEXT_LENGTH));
			photo.setDescription(RandomStringUtils.randomAlphabetic(TEXT_LENGTH));
			photo.setVisibility(createVisibility());
		}else if(DataGroups.hasDataGroup(DATA_GROUP_BACKEND_RESPONSE, dataGroups)){
			backendStatus = createBackendStatusContainer(backendStatusCount);
			photo.setBackendStatus(backendStatus);
		}else if(DataGroups.hasDataGroup(Definitions.DATA_GROUP_VISIBILITY, dataGroups)){
			photo.setVisibility(createVisibility());
		}

		MediaObjectList mediaObjectList = createMediaObjectList(analysisTypes, dataGroups, limits, serviceTypes);
		if(!MediaObjectList.isEmpty(mediaObjectList)){
			for(Iterator<MediaObject> vIter = mediaObjectList.getMediaObjects().iterator(); vIter.hasNext();){	// make sure all the new media objects have the same user identity as the created photo
				vIter.next().setOwnerUserId(userId);
			}
			photo.setMediaObjects(mediaObjectList);
		}

		if(DataGroups.hasDataGroup(Definitions.DATA_GROUP_STATUS, dataGroups)){
			photo.setBackendStatus((backendStatus != null ? createBackendStatusContainer(backendStatusCount) : backendStatus));
		}
		
		photo.setUrl(generateRedirectUrl(guid, serviceType));
		
		return photo;
	}
	
	/**
	 * 
	 * @param guid
	 * @param type
	 * @return redirection URL for the given GUID and type or null if either one the given values was null
	 */
	public String generateRedirectUrl(String guid, ServiceType type){
		if(type == null || StringUtils.isBlank(guid)){
			LOGGER.error("GUID or service type was null.");
			return null;
		}
		return ServiceInitializer.getPropertyHandler().getRESTBindContext()+service.tut.pori.contentanalysis.reference.Definitions.SERVICE_CA_REFERENCE_CLIENT+"/"+Definitions.METHOD_REDIRECT+"?"+Definitions.PARAMETER_GUID+"="+guid+"&"+Definitions.PARAMETER_SERVICE_ID+"="+type.getServiceId();
	}

	/**
	 * 
	 * @return randomly generated visibility
	 */
	public Visibility createVisibility(){
		return Visibility.fromInt(_random.nextInt(3));
	}

	/**
	 * 
	 * @param analysisTypes 
	 * @param dataGroups
	 * @param limits
	 * @param serviceTypes
	 * @param userIdentity 
	 * @return randomly generated photo list
	 */
	public PhotoList createPhotoList(Collection<AnalysisType> analysisTypes, DataGroups dataGroups, Limits limits, EnumSet<ServiceType> serviceTypes, UserIdentity userIdentity){
		int photoCount = limits.getMaxItems(service.tut.pori.contentanalysis.Definitions.ELEMENT_PHOTOLIST);
		if(photoCount < 1){
			LOGGER.warn("count < 1");
			return null;
		}else if(photoCount >= Limits.DEFAULT_MAX_ITEMS){
			LOGGER.debug("Count was "+Limits.DEFAULT_MAX_ITEMS+", using 1.");
			photoCount = 1;
		}
		
		PhotoList list = new PhotoList();
		for(int i=0;i<photoCount;++i){
			list.addPhoto(createPhoto(analysisTypes, dataGroups, limits, serviceTypes, userIdentity));
		}
		if(DataGroups.hasDataGroup(Definitions.DATA_GROUP_RESULT_INFO, dataGroups)){
			list.setResultInfo(new ResultInfo(limits.getStartItem(Definitions.ELEMENT_PHOTOLIST), limits.getStartItem(Definitions.ELEMENT_PHOTOLIST), photoCount));
		}
		return list;
	}

	/**
	 * 
	 * @param limits
	 * @return randomly generated similar photo list
	 */
	public SimilarPhotoList createSimilarPhotoList(Limits limits){
		int photoCount = limits.getMaxItems(Definitions.ELEMENT_SIMILAR_PHOTOLIST);
		if(photoCount < 1){
			LOGGER.warn("count < 1");
			return null;
		}else if(photoCount >= Limits.DEFAULT_MAX_ITEMS){
			LOGGER.debug("Count was "+Limits.DEFAULT_MAX_ITEMS+", using 1.");
			photoCount = 1;
		}
		SimilarPhotoList list = new SimilarPhotoList();
		for(int i=0;i<photoCount;++i){
			list.addPhoto(createPhoto(null, null, LIMITS_NO_MEDIA_OBJECTS, null, null));
		}
		return list;
	}

	/**
	 * 
	 * @param limits
	 * @return randomly generated dissimilar photo list
	 */
	public DissimilarPhotoList createDissimilarPhotoList(Limits limits){
		int photoCount = limits.getMaxItems(Definitions.ELEMENT_DISSIMILAR_PHOTOLIST);
		if(photoCount < 1){
			LOGGER.warn("count < 1");
			return null;
		}else if(photoCount >= Limits.DEFAULT_MAX_ITEMS){
			LOGGER.debug("Count was "+Limits.DEFAULT_MAX_ITEMS+", using 1.");
			photoCount = 1;
		}
		DissimilarPhotoList list = new DissimilarPhotoList();
		for(int i=0;i<photoCount;++i){
			list.addPhoto(createPhoto(null, null, LIMITS_NO_MEDIA_OBJECTS, null, null));
		}
		return list;
	}

	/**
	 * 
	 * @param limits
	 * @return randomly generated deleted photo list
	 */
	public DeletedPhotoList createDeletedPhotoList(Limits limits){
		int photoCount = limits.getMaxItems(Definitions.ELEMENT_DELETED_PHOTOLIST);
		if(photoCount < 1){
			LOGGER.warn("count < 1");
			return null;
		}else if(photoCount >= Limits.DEFAULT_MAX_ITEMS){
			LOGGER.debug("Count was "+Limits.DEFAULT_MAX_ITEMS+", using 1.");
			photoCount = 1;
		}
		DeletedPhotoList list = new DeletedPhotoList();
		for(int i=0;i<photoCount;++i){
			list.addPhoto(createPhoto(null, null, LIMITS_NO_MEDIA_OBJECTS, null, null));
		}
		return list;
	}

	/**
	 * 
	 * @param limits
	 * @return randomly generated feedback list
	 */
	public PhotoFeedbackList createFeedbackList(Limits limits){
		if(limits.getMaxItems(Definitions.ELEMENT_DISSIMILAR_PHOTOLIST) < 1 && limits.getMaxItems(Definitions.ELEMENT_SIMILAR_PHOTOLIST) < 1){
			LOGGER.warn("Similar and dissimilar count < 1.");
			return null;
		}
		
		return new PhotoFeedbackList(createDissimilarPhotoList(limits), createReferencePhotoList(limits), createSimilarPhotoList(limits));
	}
	
	/**
	 * 
	 * @param limits
	 * @return randomly generated reference photolist
	 */
	public ReferencePhotoList createReferencePhotoList(Limits limits){
		int referenceCount = limits.getMaxItems(Definitions.ELEMENT_REFERENCE_PHOTOLIST);
		if(referenceCount < 1 || referenceCount >= Limits.DEFAULT_MAX_ITEMS){
			LOGGER.debug("Reference count was less than 1 or MAX, using 1.");
			referenceCount = 1;
		}
		
		ReferencePhotoList refs = new ReferencePhotoList();
		for(int i=0;i<referenceCount;++i){
			refs.addPhoto(createPhoto(null, null, LIMITS_NO_MEDIA_OBJECTS, null, null));
		}
		return refs;
	}
	
	/**
	 * 
	 * @param backendId
	 * @param dataGroups
	 * @param limits
	 * @param taskId
	 * @param taskType if null, generated randomly
	 * @return randomly generated photo task details
	 */
	public PhotoTaskDetails createPhotoTaskDetails(Integer backendId, DataGroups dataGroups, Limits limits, Long taskId, TaskType taskType){
		PhotoTaskDetails details = new PhotoTaskDetails();
		populateAbstractTaskDetails(backendId, details, taskId, taskType);
		details.setPhotoList(createPhotoList(null, dataGroups,limits,null, details.getUserId()));
		details.setSimilarPhotoList(createSimilarPhotoList(limits));
		details.setDissimilarPhotoList(createDissimilarPhotoList(limits));
		details.setDeletedPhotoList(createDeletedPhotoList(limits));
		details.setUserConfidence(Math.abs(_random.nextDouble()));
		return details;
	}
	
	/**
	 * 
	 * @param analysisTypes 
	 * @param dataGroups
	 * @param limits
	 * @param taskId if null, value is randomly generated
	 * @param taskType if null, value is randomly generated
	 * @return randomly generated task response
	 */
	public PhotoTaskResponse createTaskResponse(Collection<AnalysisType> analysisTypes, DataGroups dataGroups, Limits limits, Long taskId, TaskType taskType){
		PhotoTaskResponse r = new PhotoTaskResponse();
		r.setTaskId((taskId == null ? Math.abs(_random.nextLong()) : taskId));
		Integer backendId = Math.abs(_random.nextInt());
		r.setBackendId(backendId);
		r.setMessage(RandomStringUtils.randomAlphabetic(TEXT_LENGTH));
		r.setTaskType((taskType == null ? createTaskType() : taskType));
		r.setStatus(createTaskStatus());
		PhotoList photoList = createPhotoList(analysisTypes, dataGroups, limits, null, createUserIdentity());
		if(!PhotoList.isEmpty(photoList)){
			for(Photo p : photoList.getPhotos()){ // make sure all media objects have the same back-end id as the task has
				MediaObjectList mediaObjectList = p.getMediaObjects();
				if(!MediaObjectList.isEmpty(mediaObjectList)){
					for(MediaObject vo : mediaObjectList.getMediaObjects()){
						vo.setBackendId(backendId);
					}
				}
			}
			r.setPhotoList(photoList);
		}
		return r;
	}
	
	/**
	 * Note: Java dates may go crazy if a date more than year 9000 is used
	 * 
	 * @param latest the latest date possible, if null, long max will be used
	 * @param random
	 * @return random date between latest and unix epoch
	 */
	public static Date createRandomDate(Date latest, Random random){
		long latestTime = Long.MAX_VALUE;
		if(latest != null){
			latestTime = latest.getTime();
		}
		return new Date(1 + (long)(random.nextDouble() * latestTime));
	}
	
	/**
	 * Create example search results, making sure that one of the given userIds, mediaObjects and serviceTypes is
	 * set for the photos, if userIds, mediaObjects or serviceTypes is null or empty, a random value will be generated. Note:
	 * for photos with visibility PUBLIC, any userId is OK, and the given user id will not necessarily be set.
	 * If GUID is given, the first photo in the list (when the given limits permit it) will have the given GUID.
	 * @param guid
	 * @param dataGroups
	 * @param limits
	 * @param serviceTypes
	 * @param userIds
	 * @param mediaObjects
	 * @return  randomly generated photo list
	 */
	public PhotoList createSearchResults(String guid, DataGroups dataGroups, Limits limits, EnumSet<ServiceType> serviceTypes, long[] userIds, MediaObjectList mediaObjects){
		PhotoList list = createPhotoList(null, dataGroups, limits, serviceTypes, null);
		if(list == null){
			return null;
		}
		
		boolean hasUserIds = !ArrayUtils.isEmpty(userIds);

		List<ServiceType> types = null;
		int serviceTypeCount = 0;
		if(serviceTypes != null && !serviceTypes.isEmpty()){
			types = new ArrayList<>(serviceTypes);
			serviceTypeCount = serviceTypes.size();
		}
		
		int mediaObjectCount = 0;
		if(!MediaObjectList.isEmpty(mediaObjects)){
			mediaObjectCount = mediaObjects.getMediaObjects().size();
		}
		
		if(!hasUserIds && types == null && mediaObjectCount < 1){
			LOGGER.debug("No userIds, mediaObjects or types.");
			return list;
		}
		
		for(Photo photo : list.getPhotos()){
			if(guid != null){	// if uid has been given, and has not been set already
				photo.setGUID(guid);
				guid = null;
			}
			if(hasUserIds){	
				if(!Visibility.PUBLIC.equals(photo.getVisibility())){ // only change for photos that do not have public visibility
					photo.setOwnerUserId(new UserIdentity(userIds[_random.nextInt(userIds.length)]));
				}
			}else{
				photo.setVisibility(Visibility.PUBLIC); // there could also be photos with visibility PRIVATE, if the user was logged in, but setting all to PUBLIC will ensure that the example result is valid
			}
			if(types != null){
				photo.setServiceType(types.get(_random.nextInt(serviceTypeCount)));	// make sure there are service types only from the given set
			}
			if(mediaObjectCount > 0){
				List<MediaObject> objects = photo.getMediaObjects().getMediaObjects();
				if(objects != null){	// don't do anything if there are no other objects (probably filtered out by limits or datagroups)
					objects.remove(0);	// remove one just in case, to keep the limits...
					photo.addMediaObject(mediaObjects.getMediaObjects().get(_random.nextInt(mediaObjectCount))); //... and add new one for the removed one
				}			
			}
		}
		return list;
	}
	
	/**
	 * @return randomly generated URL
	 */
	public static String createRandomUrl(){
		return "http://example.org/"+RandomStringUtils.randomAlphabetic(20);
	}
	
	/**
	 * 
	 * @return randomly generated task type
	 */
	public TaskType createPhotoTaskDetailsType(){
		switch(_random.nextInt(3)){
			case 0:
				return TaskType.ANALYSIS;
			case 1:
				return TaskType.BACKEND_FEEDBACK;
			case 2:
				return TaskType.FEEDBACK;
			default: // will never happen
				return null;
		}
	}
	
	/**
	 * @param dataGroups
	 * @param limits
	 * @param type ANALYSIS, BACKEND_FEEDBACK or FEEDBACK, if null, type is chosen randomly
	 * @return randomly generated photo task details
	 * @throws UnsupportedOperationException on bad type
	 */
	public PhotoTaskDetails createPhotoTaskDetails(DataGroups dataGroups, Limits limits, TaskType type) throws UnsupportedOperationException{
		if(type == null){
			type = createPhotoTaskDetailsType();
		}
		PhotoTaskDetails details = new PhotoTaskDetails(type);
		UserIdentity userIdentity = createUserIdentity();
		switch(type){
			case BACKEND_FEEDBACK:
				LOGGER.debug("Using task type "+TaskType.ANALYSIS.name()+" as template for task of type "+TaskType.BACKEND_FEEDBACK.name());
			case ANALYSIS:
				details.setPhotoList(createPhotoList(null, dataGroups, limits, null, userIdentity));
				break;
			case FEEDBACK:
				details.setUserConfidence(_random.nextDouble());
				if(_random.nextBoolean()){	// create randomly a similarity feedback
					details.addReferencePhoto(new Photo(String.valueOf(Math.abs(_random.nextLong()))));
					details.setDissimilarPhotoList(createDissimilarPhotoList(limits));
					details.setSimilarPhotoList(createSimilarPhotoList(limits));
				}else if(_random.nextBoolean()){	// create randomly deleted feedback
					details.setDeletedPhotoList(createDeletedPhotoList(limits));
				}else{	// normal photo list
					details.setPhotoList(createPhotoList(null, dataGroups, limits, null, userIdentity));
				}
				break;
			default:
				throw new UnsupportedOperationException("Unsupported type: "+type.name());
		}
		
		details.setBackendId(Math.abs(_random.nextInt()));
		details.setTaskId(Math.abs(_random.nextLong()));
		int backendStatusCount = limits.getMaxItems(Definitions.ELEMENT_BACKEND_STATUS_LIST);
		if(backendStatusCount >= Limits.DEFAULT_MAX_ITEMS){
			LOGGER.debug(Definitions.ELEMENT_BACKEND_STATUS_LIST+" >= "+Limits.DEFAULT_MAX_ITEMS+" using 1.");
			backendStatusCount = 1;
		}
		details.setBackends(createBackendStatusContainer(backendStatusCount));
		details.setUserId(userIdentity);
		details.setCallbackUri(generateFinishedCallbackUri()); // override the default uri
		details.setTaskParameters(createAnalysisParameters());
		return details;
	}
	
	/**
	 * 
	 * @return the default task finished callback uri
	 */
	public String generateFinishedCallbackUri(){
		return ServiceInitializer.getPropertyHandler().getRESTBindContext()+service.tut.pori.contentanalysis.reference.Definitions.SERVICE_CA_REFERENCE_SERVER+"/"+Definitions.METHOD_TASK_FINISHED;
	}
	
	/**
	 * 
	 * @return media object list with keywords that have known friendly values, or null if no values could be created
	 */
	public MediaObjectList createFriendlyKeywordableList(){
		final List<Pair<String, Integer>> values = new ArrayList<>();	
		try (ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(ServiceInitializer.getConfigHandler().getPropertyFilePath()+"database-context.xml") ; Connection connection = DataSourceUtils.getConnection((DataSource) ctx.getBean("dataSource")); Statement stmnt = connection.createStatement()){ // by-pass the dao to retrieve raw values for testing
			ResultSet set = stmnt.executeQuery("SELECT value, backend_id FROM ca_frontend.ca_photo_friendly_keywords where friendly_value IS NOT NULL");
			while(set.next()){
				int backendId = set.getInt("backend_id");
				values.add(Pair.of(set.getString("value"), (set.wasNull() ? null : backendId)));
			}
		} catch (CannotGetJdbcConnectionException | BeansException | SQLException ex) {
			LOGGER.error(ex, ex);
		}
		
		if(values.isEmpty()){
			LOGGER.warn("No values.");
			return null;
		}
		
		MediaObjectList list = new MediaObjectList();
		UserIdentity userId = createUserIdentity();
		int mediaObjectId = 0;
		for(Pair<String, Integer> p : values){
			MediaObject o = new MediaObject(MediaType.PHOTO,MediaObjectType.KEYWORD);
			o.setValue(p.getLeft());
			o.setBackendId(p.getRight());
			o.setMediaObjectId(String.valueOf(++mediaObjectId));
			o.setConfirmationStatus(ConfirmationStatus.CANDIDATE);
			o.setOwnerUserId(userId);
			list.addMediaObject(o);
		}
		return list;
	}

	/**
	 * 
	 * @return randomly generated result info
	 */
	public ResultInfo createResultInfo() {
		long start = Math.abs(_random.nextInt());
		return new ResultInfo(start, start+Math.abs(_random.nextInt()), Math.abs(_random.nextLong()));
	}

	/**
	 * 
	 * @return randomly generated analysis parameters
	 */
	public PhotoParameters createAnalysisParameters() {
		PhotoParameters ap = new PhotoParameters();
		ap.setAnalysisTypes(EnumSet.of(AnalysisType.FACE_DETECTION, AnalysisType.KEYWORD_EXTRACTION, AnalysisType.VISUAL));
		return ap;
	}
}
