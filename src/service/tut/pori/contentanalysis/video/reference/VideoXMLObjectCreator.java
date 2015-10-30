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
package service.tut.pori.contentanalysis.video.reference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import service.tut.pori.contentanalysis.PhotoParameters.AnalysisType;
import service.tut.pori.contentanalysis.AsyncTask.TaskType;
import service.tut.pori.contentanalysis.BackendStatusList;
import service.tut.pori.contentanalysis.CAContentCore.ServiceType;
import service.tut.pori.contentanalysis.CAContentCore.Visibility;
import service.tut.pori.contentanalysis.Definitions;
import service.tut.pori.contentanalysis.ResultInfo;
import service.tut.pori.contentanalysis.MediaObject;
import service.tut.pori.contentanalysis.MediaObjectList;
import service.tut.pori.contentanalysis.reference.CAXMLObjectCreator;
import service.tut.pori.contentanalysis.video.DeletedVideoList;
import service.tut.pori.contentanalysis.video.Timecode;
import service.tut.pori.contentanalysis.video.TimecodeList;
import service.tut.pori.contentanalysis.video.Video;
import service.tut.pori.contentanalysis.video.VideoList;
import service.tut.pori.contentanalysis.video.VideoParameters;
import service.tut.pori.contentanalysis.video.VideoParameters.SequenceType;
import service.tut.pori.contentanalysis.video.VideoTaskDetails;
import service.tut.pori.contentanalysis.video.VideoTaskResponse;
import core.tut.pori.context.ServiceInitializer;
import core.tut.pori.http.parameters.DataGroups;
import core.tut.pori.http.parameters.Limits;
import core.tut.pori.users.UserIdentity;
import core.tut.pori.utils.MediaUrlValidator.MediaType;

/**
 * Class that can be used to created example objects/object lists.
 */
public class VideoXMLObjectCreator {
	private static final Limits LIMITS_NO_MEDIA_OBJECTS;
	static{
		LIMITS_NO_MEDIA_OBJECTS = new Limits(0, 0);
		LIMITS_NO_MEDIA_OBJECTS.setTypeLimits(0, -1, Definitions.ELEMENT_MEDIA_OBJECTLIST);
	}
	private static final Logger LOGGER = Logger.getLogger(VideoXMLObjectCreator.class);
	private static final int TEXT_LENGTH = 64;
	private CAXMLObjectCreator _CACreator = null;
	
	/**
	 * 
	 * @param seed for random generator, or null to use default (system time in nanoseconds)
	 */
	public VideoXMLObjectCreator(Long seed){
		if(seed == null){
			seed = System.nanoTime();
		}
		_CACreator = new CAXMLObjectCreator(seed);
	}

	/**
	 * 
	 * @param analysisTypes 
	 * @param dataGroups
	 * @param limits 
	 * @param serviceTypes 
	 * @param userIdentity 
	 * @return a randomly generated video
	 */
	public Video createVideo(Collection<AnalysisType> analysisTypes, DataGroups dataGroups, Limits limits, EnumSet<ServiceType> serviceTypes, UserIdentity userIdentity) {
		Video video = new Video();
		String guid = UUID.randomUUID().toString();
		video.setGUID(guid);
		ServiceType serviceType = createVideoServiceType();
		video.setServiceType(serviceType);
		UserIdentity userId = (UserIdentity.isValid(userIdentity) ? userIdentity : _CACreator.createUserIdentity());
		video.setOwnerUserId(userId);
		
		int backendStatusCount = (!DataGroups.hasDataGroup(Definitions.DATA_GROUP_BACKEND_STATUS, dataGroups) || limits == null ? 0 : limits.getMaxItems(Definitions.ELEMENT_BACKEND_STATUS_LIST));

		BackendStatusList backendStatus = null;
		if(DataGroups.hasDataGroup(DataGroups.DATA_GROUP_ALL,dataGroups) || DataGroups.hasDataGroup(DataGroups.DATA_GROUP_BASIC, dataGroups)){
			video.setCredits(RandomStringUtils.randomAlphabetic(TEXT_LENGTH));
			video.setName(RandomStringUtils.randomAlphabetic(TEXT_LENGTH));
			video.setDescription(RandomStringUtils.randomAlphabetic(TEXT_LENGTH));
			video.setVisibility(_CACreator.createVisibility());
		}else if(DataGroups.hasDataGroup(CAXMLObjectCreator.DATA_GROUP_BACKEND_RESPONSE, dataGroups)){
			backendStatus = _CACreator.createBackendStatusContainer(backendStatusCount);
			video.setBackendStatus(backendStatus);
		}else if(DataGroups.hasDataGroup(Definitions.DATA_GROUP_VISIBILITY, dataGroups)){
			video.setVisibility(_CACreator.createVisibility());
		}

		MediaObjectList mediaObjectList = createMediaObjectList(analysisTypes, dataGroups, limits, serviceTypes);
		if(!MediaObjectList.isEmpty(mediaObjectList)){
			for(Iterator<MediaObject> vIter = mediaObjectList.getMediaObjects().iterator(); vIter.hasNext();){	// make sure all the new media objects have the same user identity as the created video
				vIter.next().setOwnerUserId(userId);
			}
			video.setMediaObjects(mediaObjectList);
		}

		if(DataGroups.hasDataGroup(Definitions.DATA_GROUP_STATUS, dataGroups)){
			video.setBackendStatus((backendStatus != null ? _CACreator.createBackendStatusContainer(backendStatusCount) : backendStatus));
		}
		
		video.setUrl(generateRedirectUrl(guid, serviceType));
		
		return video;
	}
	
	/**
	 * Create media object list using {@link service.tut.pori.contentanalysis.MediaObject}} class.
	 * 
	 * Note that regardless of the given analysis types, this will only return objects of type {@link core.tut.pori.utils.MediaUrlValidator.MediaType#VIDEO} or {@link core.tut.pori.utils.MediaUrlValidator.MediaType#AUDIO}
	 * 
	 * @param analysisTypes  
	 * @param dataGroups
	 * @param limits
	 * @param serviceTypes
	 * @return randomly generated media object list
	 */
	public MediaObjectList createMediaObjectList(Collection<AnalysisType> analysisTypes, DataGroups dataGroups, Limits limits, EnumSet<ServiceType> serviceTypes) {
		MediaObjectList objects = _CACreator.createMediaObjectList(analysisTypes, dataGroups, limits, serviceTypes);
		if(!MediaObjectList.isEmpty(objects) && (DataGroups.hasDataGroup(Definitions.DATA_GROUP_TIMECODES, dataGroups) || DataGroups.hasDataGroup(DataGroups.DATA_GROUP_ALL, dataGroups))){
			boolean hasAudio = (analysisTypes != null && analysisTypes.contains(AnalysisType.AUDIO));
			boolean hasVideo = (!hasAudio || analysisTypes.contains(AnalysisType.VISUAL));
			Random r = _CACreator.getRandom();
			for(MediaObject vo : objects.getMediaObjects()){
				if(hasAudio){
					if(hasVideo && r.nextBoolean()){
						vo.setMediaType(MediaType.VIDEO);
					}else{
						vo.setMediaType(MediaType.AUDIO);
					}
				}else{
					vo.setMediaType(MediaType.VIDEO);
				}
				vo.setTimecodes(createTimecodeList(limits, false));
			}
		}
		return objects;
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
		return ServiceInitializer.getPropertyHandler().getRESTBindContext()+service.tut.pori.contentanalysis.video.reference.Definitions.SERVICE_VCA_REFERENCE_CLIENT+"/"+service.tut.pori.contentanalysis.Definitions.METHOD_REDIRECT+"?"+service.tut.pori.contentanalysis.Definitions.PARAMETER_GUID+"="+guid+"&"+service.tut.pori.contentanalysis.Definitions.PARAMETER_SERVICE_ID+"="+type.getServiceId();
	}
	
	/**
	 * 
	 * @return serviceType valid for videos (not facebook jazz)
	 */
	public ServiceType createVideoServiceType(){
		return ServiceType.PICASA_STORAGE_SERVICE;
	}

	/**
	 * @param analysisTypes 
	 * @param dataGroups
	 * @param limits 
	 * @param serviceTypes
	 * @return randomly generated media object
	 */
	public MediaObject createVideoMediaObject(Collection<AnalysisType> analysisTypes, DataGroups dataGroups, Limits limits, EnumSet<ServiceType> serviceTypes) {
		MediaObject vvo = _CACreator.createMediaObject(analysisTypes, dataGroups, serviceTypes);
		vvo.setMediaType(MediaType.VIDEO);
		vvo.setTimecodes(createTimecodeList(limits, false));
		return vvo;
	}

	/**
	 * 
	 * @param previousEnd if not null, the start time will be after this time
	 * @return randomly generated timecode
	 */
	public Timecode createTimecode(Double previousEnd) {
		Timecode timecode = new Timecode();
		Random r = _CACreator.getRandom();
		double start = r.nextDouble()*r.nextInt(3600);
		if(previousEnd != null){
			start += previousEnd;
		}
		timecode.setStart(start);
		timecode.setEnd(start+r.nextDouble()*r.nextInt(3600));
		return timecode;
	}

	/**
	 * 
	 * @param limits
	 * @param sequential if true the timecodes will appear in sequential order, otherwise they are random and may cover duplicate time periods
	 * @return a randomly generated time code list
	 */
	public TimecodeList createTimecodeList(Limits limits, boolean sequential) {
		int count = (limits == null ? 0 : limits.getMaxItems(service.tut.pori.contentanalysis.video.Definitions.ELEMENT_TIMECODELIST));
		if(count < 1){
			LOGGER.warn("count < 1");
			return null;
		}else if(count >= Limits.DEFAULT_MAX_ITEMS){
			LOGGER.debug("Count was "+Limits.DEFAULT_MAX_ITEMS+", using 1.");
			count = 1;
		}

		TimecodeList tcl = new TimecodeList();
		Double previousEnd = null;
		for(int i=0;i<count;++i){
			Timecode tc = createTimecode(previousEnd);
			if(sequential){
				previousEnd = tc.getEnd();
			}
			tcl.addTimecode(tc);
		}
		return tcl;
	}

	/**
	 * 
	 * @param analysisTypes 
	 * @param dataGroups
	 * @param limits
	 * @param serviceTypes
	 * @param userIdentity 
	 * @return randomly generated video list
	 */
	public VideoList createVideoList(Collection<AnalysisType> analysisTypes, DataGroups dataGroups, Limits limits, EnumSet<ServiceType> serviceTypes, UserIdentity userIdentity) {
		int count = limits.getMaxItems(service.tut.pori.contentanalysis.video.Definitions.ELEMENT_VIDEOLIST);
		if(count < 1){
			LOGGER.warn("count < 1");
			return null;
		}else if(count >= Limits.DEFAULT_MAX_ITEMS){
			LOGGER.debug("Count was "+Limits.DEFAULT_MAX_ITEMS+", using 1.");
			count = 1;
		}
		VideoList list = new VideoList();
		for(int i=0;i<count;++i){
			list.addVideo(createVideo(analysisTypes, dataGroups, limits, serviceTypes, userIdentity));
		}
		if(DataGroups.hasDataGroup(Definitions.DATA_GROUP_RESULT_INFO, dataGroups)){
			list.setResultInfo(new ResultInfo(limits.getStartItem(service.tut.pori.contentanalysis.video.Definitions.ELEMENT_VIDEOLIST), limits.getEndItem(service.tut.pori.contentanalysis.video.Definitions.ELEMENT_VIDEOLIST), count));
		}
		return list;
	}

	/**
	 * 
	 * @param limits
	 * @return randomly generated deleted video list
	 */
	public DeletedVideoList createDeletedVideoList(Limits limits) {
		int count = limits.getMaxItems(service.tut.pori.contentanalysis.video.Definitions.ELEMENT_DELETED_VIDEOLIST);
		if(count < 1){
			LOGGER.warn("count < 1");
			return null;
		}else if(count >= Limits.DEFAULT_MAX_ITEMS){
			LOGGER.debug("Count was "+Limits.DEFAULT_MAX_ITEMS+", using 1.");
			count = 1;
		}
		DeletedVideoList list = new DeletedVideoList();
		for(int i=0;i<count;++i){
			list.addVideo(createVideo(null, null, LIMITS_NO_MEDIA_OBJECTS, null, null));
		}
		return list;
	}

	/**
	 * Create video options for an analysis task.
	 * 
	 * @return randomly generated options
	 */
	public VideoParameters createVideoOptions() {
		VideoParameters options = new VideoParameters();
		SequenceType t = getSequenceType();
		options.setSequenceType(t);
		Random r = _CACreator.getRandom();
		switch(t){
			case SECOND:
				options.setSequenceDuration(Math.abs(r.nextInt()));
				break;
			case FRAME: // do not generate duration if frame-based analysis is chosen
			case FULL: // do not generate duration if entire video is chosen
			case SHOT: // do not generate duration if variable-length shots are chosen
				break;
			default:
				throw new UnsupportedOperationException("Unhandelled "+SequenceType.class.toString()+" : "+t.name());
		}
		
		if(r.nextBoolean()){
			options.setTimecodes(createTimecodeList(new Limits(0,r.nextInt(5)), true));
		}
		options.setAnalysisTypes(EnumSet.allOf(AnalysisType.class));
		return options;
	}
	
	/**
	 * 
	 * @return random sequence type
	 */
	public SequenceType getSequenceType() {
		Random r = _CACreator.getRandom();
		SequenceType[] types = SequenceType.values();
		return types[r.nextInt(types.length)];
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
	public VideoTaskResponse createTaskResponse(Collection<AnalysisType> analysisTypes, DataGroups dataGroups, Limits limits, Long taskId, TaskType taskType) {
		VideoTaskResponse response = new VideoTaskResponse();
		Random r = _CACreator.getRandom();
		response.setTaskId((taskId == null ? Math.abs(r.nextLong()) : taskId));
		Integer backendId = Math.abs(r.nextInt());
		response.setBackendId(backendId);
		response.setMessage(RandomStringUtils.randomAlphabetic(TEXT_LENGTH));
		response.setTaskType((taskType == null ? _CACreator.createTaskType() : taskType));
		response.setStatus(_CACreator.createTaskStatus());
		VideoList videoList = createVideoList(analysisTypes, dataGroups, limits, null, _CACreator.createUserIdentity());
		if(!VideoList.isEmpty(videoList)){
			for(Video v : videoList.getVideos()){ // make sure all media objects have the same back-end id as the task has
				MediaObjectList mediaObjectList = v.getMediaObjects();
				if(!MediaObjectList.isEmpty(mediaObjectList)){
					for(MediaObject vo : mediaObjectList.getMediaObjects()){
						vo.setBackendId(backendId);
					}
				}
			}
			response.setVideoList(videoList);
		}
		return response;
	}

	/**
	 * @param backendId if null, the value is randomly generated
	 * @param dataGroups
	 * @param limits
	 * @param taskId if null, the value is randomly generated
	 * @param taskType ANALYSIS or FEEDBACK, if null, type is chosen randomly
	 * @return randomly generated video task details
	 * @throws UnsupportedOperationException on bad type
	 */
	public VideoTaskDetails createVideoTaskDetails(Integer backendId, DataGroups dataGroups, Limits limits, Long taskId, TaskType taskType) {
		if(taskType == null){
			taskType = createVideoTaskDetailsType();
		}
		VideoTaskDetails details = new VideoTaskDetails(taskType);
		UserIdentity userIdentity = _CACreator.createUserIdentity();
		Random r = _CACreator.getRandom();
		switch(taskType){
			case ANALYSIS:
				details.setTaskParameters(createVideoOptions());
				details.setVideoList(createVideoList(null, dataGroups, limits, null, userIdentity));
				break;
			case FEEDBACK:
				if(r.nextBoolean()){	// create randomly deleted feedback
					details.setDeletedVideoList(createDeletedVideoList(limits));
				}else{	// normal video list
					details.setVideoList(createVideoList(null, dataGroups, limits, null, userIdentity));
				}
				break;
			default:
				throw new UnsupportedOperationException("Unsupported type: "+taskType.name());
		}
		
		details.setBackendId((backendId == null ? Math.abs(r.nextInt()) : backendId));
		details.setTaskId((taskId == null ? Math.abs(r.nextLong()) : taskId));
		details.setBackends(_CACreator.createBackendStatusContainer(limits.getMaxItems(Definitions.ELEMENT_BACKEND_STATUS_LIST)));
		details.setUserId(userIdentity);
		details.setCallbackUri(generateFinishedCallbackUri()); // override the default uri
		return details;
	}
	
	/**
	 * 
	 * @return the default task finished callback uri
	 */
	public String generateFinishedCallbackUri(){
		return ServiceInitializer.getPropertyHandler().getRESTBindContext()+service.tut.pori.contentanalysis.video.reference.Definitions.SERVICE_VCA_REFERENCE_SERVER+"/"+service.tut.pori.contentanalysis.Definitions.METHOD_TASK_FINISHED;
	}
	
	/**
	 * 
	 * @return randomly generated task type
	 */
	public TaskType createVideoTaskDetailsType(){
		return (_CACreator.getRandom().nextBoolean() ? TaskType.ANALYSIS : TaskType.FEEDBACK);
	}

	/**
	 * Create example search results, making sure that one of the given userIds, mediaObjects and serviceTypes is
	 * set for the videos, if userIds, mediaObjects or serviceTypes is null or empty, a random value will be generated. Note:
	 * for videos with visibility PUBLIC, any userId is OK, and the given user id will not necessarily be set.
	 * If GUID is given, the first video in the list (when the given limits permit it) will have the given GUID.
	 * @param guid
	 * @param dataGroups
	 * @param limits
	 * @param serviceTypes
	 * @param userIds
	 * @param mediaObjects
	 * @return  randomly generated video list
	 */
	public VideoList createSearchResults(String guid, DataGroups dataGroups, Limits limits, EnumSet<ServiceType> serviceTypes, long[] userIds, MediaObjectList mediaObjects) {
		VideoList list = createVideoList(null, dataGroups, limits, serviceTypes, null);
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
		
		Random r = _CACreator.getRandom();
		for(Video video : list.getVideos()){
			if(guid != null){	// if uid has been given, and has not been set already
				video.setGUID(guid);
				guid = null;
			}
			if(hasUserIds){	
				if(!Visibility.PUBLIC.equals(video.getVisibility())){ // only change for photos that do not have public visibility
					video.setOwnerUserId(new UserIdentity(userIds[r.nextInt(userIds.length)]));
				}
			}else{
				video.setVisibility(Visibility.PUBLIC); // there could also be photos with visibility PRIVATE, if the user was logged in, but setting all to PUBLIC will ensure that the example result is valid
			}
			if(types != null){
				video.setServiceType(types.get(r.nextInt(serviceTypeCount)));	// make sure there are service types only from the given set
			}
			if(mediaObjectCount > 0){
				List<MediaObject> objects = video.getMediaObjects().getMediaObjects();
				if(objects != null){	// don't do anything if there are no other objects (probably filtered out by limits or datagroups)
					objects.remove(0);	// remove one just in case, to keep the limits...
					video.addMediaObject(mediaObjects.getMediaObjects().get(r.nextInt(mediaObjectCount))); // ...and add new one for the removed one
				}
			}
		}
		return list;
	}

	/**
	 * @return the random generator used for this object creator
	 * @see service.tut.pori.contentanalysis.reference.CAXMLObjectCreator#getRandom()
	 */
	public Random getRandom() {
		return _CACreator.getRandom();
	}
}
