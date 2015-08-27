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
package service.tut.pori.facebookjazz.reference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.log4j.Logger;

import service.tut.pori.contentanalysis.AsyncTask.TaskType;
import service.tut.pori.contentanalysis.CAContentCore.ServiceType;
import service.tut.pori.contentanalysis.CAContentCore.Visibility;
import service.tut.pori.contentanalysis.Definitions;
import service.tut.pori.contentanalysis.PhotoList;
import service.tut.pori.contentanalysis.MediaObject;
import service.tut.pori.contentanalysis.MediaObject.ConfirmationStatus;
import service.tut.pori.contentanalysis.MediaObjectList;
import service.tut.pori.contentanalysis.reference.CAXMLObjectCreator;
import service.tut.pori.contentstorage.FacebookPhotoStorage;
import service.tut.pori.facebookjazz.FBFeedbackTaskDetails;
import service.tut.pori.facebookjazz.FBSummarizationTaskDetails;
import service.tut.pori.facebookjazz.FBTaskResponse;
import service.tut.pori.facebookjazz.FacebookComment;
import service.tut.pori.facebookjazz.FacebookEvent;
import service.tut.pori.facebookjazz.FacebookEvent.RSVPStatus;
import service.tut.pori.facebookjazz.FacebookExtractor.ContentType;
import service.tut.pori.facebookjazz.FacebookGroup;
import service.tut.pori.facebookjazz.FacebookGroup.Privacy;
import service.tut.pori.facebookjazz.FacebookLike;
import service.tut.pori.facebookjazz.FacebookLocation;
import service.tut.pori.facebookjazz.FacebookPhotoDescription;
import service.tut.pori.facebookjazz.FacebookPhotoTag;
import service.tut.pori.facebookjazz.FacebookProfile;
import service.tut.pori.facebookjazz.FacebookRelationship;
import service.tut.pori.facebookjazz.FacebookStatusMessage;
import service.tut.pori.facebookjazz.FacebookUserDetails;
import service.tut.pori.facebookjazz.FacebookVideoDescription;
import service.tut.pori.facebookjazz.WeightModifier;
import service.tut.pori.facebookjazz.WeightModifier.WeightModifierType;
import service.tut.pori.facebookjazz.WeightModifierList;

import com.restfb.types.CategorizedFacebookType;
import com.restfb.types.Location;
import com.restfb.types.NamedFacebookType;

import core.tut.pori.context.ServiceInitializer;
import core.tut.pori.http.parameters.DataGroups;
import core.tut.pori.http.parameters.Limits;
import core.tut.pori.users.UserIdentity;

/**
 * 
 * class that can be used to created example objects/object lists
 * 
 */
public class FBJXMLObjectCreator {
	private static final DataGroups DATA_GROUPS_ALL = new DataGroups(DataGroups.DATA_GROUP_ALL);
	private static final Logger LOGGER = Logger.getLogger(FBJXMLObjectCreator.class);
	private static final int TEXT_LENGTH = 64;
	private static final int WEIGHT_MODIFIER_MAX = 5;
	private boolean _includeResultInfo = false;
	private CAXMLObjectCreator _CACreator = null;

	/**
	 * 
	 * @param seed for random generator, or null to use default (system time in nanoseconds)
	 */
	public FBJXMLObjectCreator(Long seed){
		if(seed == null){
			seed = System.nanoTime();
		}
		_CACreator = new CAXMLObjectCreator(seed);
	}

	/**
	 * @see service.tut.pori.contentanalysis.reference.CAXMLObjectCreator#getSeed()
	 * 
	 * @return the seed
	 */
	public long getSeed() {
		return _CACreator.getSeed();
	}

	/**
	 * 
	 * @return randomly generated comment
	 */
	public FacebookComment createComment(){
		FacebookComment comment = new FacebookComment();
		comment.setMessage(RandomStringUtils.randomAlphabetic(TEXT_LENGTH));
		Random r = _CACreator.getRandom();
		comment.setLikeCount(Math.abs(r.nextLong()));
		comment.setCreatedTime(CAXMLObjectCreator.createRandomDate(new Date(), r));
		comment.setFromName(RandomStringUtils.randomAlphabetic(TEXT_LENGTH));
		return comment;
	}

	/**
	 * 
	 * @return randomly generated type
	 */
	public CategorizedFacebookType createCategorizedFacebookType(){
		CategorizedFacebookType type = new CategorizedFacebookType();
		populateCategorizedFacebookType(type);
		return type;
	}

	/**
	 * 
	 * @param type
	 */
	public void populateCategorizedFacebookType(CategorizedFacebookType type){
		type.setCategory(RandomStringUtils.randomAlphabetic(TEXT_LENGTH));
		populateNamedFacebookType(type);
	}

	/**
	 * 
	 * @param type
	 */
	public void populateNamedFacebookType(NamedFacebookType type){
		type.setName(RandomStringUtils.randomAlphabetic(TEXT_LENGTH));
	}

	/**
	 * 
	 * @return randomly generated type
	 */
	public NamedFacebookType createNamedFacebookType(){
		NamedFacebookType type = new NamedFacebookType();
		populateNamedFacebookType(type);
		return type;
	}

	/**
	 * 
	 * @return randomly generated id
	 */
	public long createUserId(){
		return Math.abs(_CACreator.getRandom().nextLong());
	}

	/**
	 * 
	 * @return randomly generated list
	 */
	public WeightModifierList createWeightModifierList(){
		WeightModifierList modifiers = new WeightModifierList();
		WeightModifierType[] types = WeightModifierType.values();
		for(int i=0;i<types.length;++i){
			modifiers.setWeightModifier(createWeightModifier(types[i]));
		}
		return modifiers;
	}
	
	/**
	 * 
	 * @param type if null, the type will be randomly generated
	 * @return randomly generated weight modifier
	 */
	public WeightModifier createWeightModifier(WeightModifierType type) {
		if(type == null){
			type = createWeightModifierType();
		}
		return new WeightModifier(type, _CACreator.getRandom().nextInt(WEIGHT_MODIFIER_MAX));
	}
	
	/**
	 * 
	 * @return random weight modifier type
	 */
	public WeightModifierType createWeightModifierType(){
		WeightModifierType[] types = WeightModifierType.values();
		return types[_CACreator.getRandom().nextInt(types.length)];
	}

	/**
	 * 
	 * @return randomly generated status
	 */
	public ConfirmationStatus createConfirmationStatus(){
		return ConfirmationStatus.fromInt(_CACreator.getRandom().nextInt(5)+1);
	}

	/**
	 * @see service.tut.pori.contentanalysis.reference.CAXMLObjectCreator#createVisibility()
	 * 
	 * @return randomly generated visiblity
	 */
	public Visibility createVisibility(){
		return _CACreator.createVisibility();
	}

	/**
	 * 
	 * @param likeCount
	 * @param eventCount
	 * @param groupCount
	 * @param photoDescriptionCount
	 * @param videoDescriptionCount
	 * @param statusMessageCount
	 * @param tagCount 
	 * @param commentCount 
	 * @return randomly generated details
	 */
	public FBSummarizationTaskDetails createFBSummarizationTaskDetails(int likeCount, int eventCount, int groupCount, int photoDescriptionCount, int videoDescriptionCount, int statusMessageCount, int tagCount, int commentCount){
		FBSummarizationTaskDetails details = new FBSummarizationTaskDetails();
		Random r = _CACreator.getRandom();
		_CACreator.populateAbstractTaskDetails(Math.abs(r.nextInt()), details, Math.abs(r.nextLong()), TaskType.FACEBOOK_PROFILE_SUMMARIZATION);
		details.setProfile(createFacebookProfile(likeCount, eventCount, groupCount, photoDescriptionCount, videoDescriptionCount, statusMessageCount, tagCount, commentCount));
		details.setContentTypes(EnumSet.allOf(ContentType.class));
		details.setCallbackUri(generateFinishedCallbackUri()); // override the default uri
		return details;
	}
	
	/**
	 * 
	 * @return the default task finished callback uri
	 */
	public String generateFinishedCallbackUri(){
		return ServiceInitializer.getPropertyHandler().getRESTBindContext()+service.tut.pori.facebookjazz.reference.Definitions.SERVICE_FBJ_REFERENCE_SERVER+"/"+Definitions.METHOD_TASK_FINISHED;
	}

	/**
	 * 
	 * @param likeCount
	 * @param eventCount
	 * @param groupCount
	 * @param photoDescriptionCount
	 * @param videoDescriptionCount
	 * @param statusMessageCount
	 * @param tagCount 
	 * @param commentCount 
	 * @return randombly generated profile
	 */
	public FacebookProfile createFacebookProfile(int likeCount, int eventCount, int groupCount, int photoDescriptionCount, int videoDescriptionCount, int statusMessageCount, int tagCount, int commentCount){
		FacebookProfile profile = new FacebookProfile(createFacebookUserDetails());

		if(likeCount > 0){
			ArrayList<FacebookLike> likes = new ArrayList<>(likeCount);
			for(int i=0;i<likeCount;++i){
				likes.add(createFacebookLike());
			}
			profile.setLikes(likes);
		}

		if(statusMessageCount > 0){
			ArrayList<FacebookStatusMessage> messages = new ArrayList<>(statusMessageCount);
			for(int i=0;i<statusMessageCount;++i){
				messages.add(createFacebookStatusMessage(commentCount));
			}
			profile.setStatusMessages(messages);
		}

		if(eventCount > 0){
			ArrayList<FacebookEvent> events = new ArrayList<>(eventCount);
			for(int i=0;i<eventCount;++i){
				events.add(createFacebookEvent());
			}
			profile.setEvents(events);
		}

		if(photoDescriptionCount > 0){
			ArrayList<FacebookPhotoDescription> descs = new ArrayList<>(photoDescriptionCount);
			for(int i=0;i<photoDescriptionCount;++i){
				descs.add(createFacebookPhotoDescription(commentCount, tagCount));
			}
			profile.setPhotoDescriptions(descs);
		}

		if(groupCount > 0){
			ArrayList<FacebookGroup> groups = new ArrayList<>(groupCount);
			for(int i=0;i<groupCount;++i){
				groups.add(createFacebookGroup());
			}
			profile.setGroups(groups);
		}

		if(videoDescriptionCount > 0){
			ArrayList<FacebookVideoDescription> descs = new ArrayList<>(videoDescriptionCount);
			for(int i=0;i<videoDescriptionCount;++i){
				descs.add(createFacebookVideoDescription(commentCount));
			}
			profile.setVideoDescriptions(descs);
		}

		return profile;
	}

	/**
	 * 
	 * @param commentCount 
	 * @return randomly generated description
	 */
	public FacebookVideoDescription createFacebookVideoDescription(int commentCount){
		FacebookVideoDescription vid = new FacebookVideoDescription();

		Random r = _CACreator.getRandom();
		vid.setLikeCount(Math.abs(r.nextLong()));

		vid.setId(String.valueOf(Math.abs(r.nextLong())));

		vid.setDescriptionComments(new ArrayList<>(Arrays.asList(createFacebookComment(),createFacebookComment())));

		vid.setFromName(RandomStringUtils.randomAlphabetic(TEXT_LENGTH));

		Date updated = CAXMLObjectCreator.createRandomDate(new Date(),r);
		vid.setUpdatedTime(updated);

		vid.setCreatedTime(CAXMLObjectCreator.createRandomDate(updated,r));

		vid.setDescription(RandomStringUtils.randomAlphabetic(TEXT_LENGTH));

		vid.setDescriptionComments(createFacebookComments(commentCount));
		return vid;
	}

	/**
	 * 
	 * @param commentCount 
	 * @param tagCount 
	 * @return randomly generated description
	 */
	public FacebookPhotoDescription createFacebookPhotoDescription(int commentCount, int tagCount){
		FacebookPhotoDescription d = new FacebookPhotoDescription();

		Random r = _CACreator.getRandom();
		Date updated = CAXMLObjectCreator.createRandomDate(new Date(),r);
		d.setUpdatedTime(updated);

		d.setCreatedTime(CAXMLObjectCreator.createRandomDate(updated,r));

		d.addLike(createNamedFacebookType());

		d.setDescription(RandomStringUtils.randomAlphabetic(TEXT_LENGTH));

		d.setFromName(RandomStringUtils.randomAlphabetic(TEXT_LENGTH));
		
		d.setId(String.valueOf(Math.abs(r.nextLong())));

		d.setFacebookLocation(createFacebookLocation());

		d.setPhotoGUID(UUID.randomUUID().toString());
		d.setServiceType(FacebookPhotoStorage.SERVICE_TYPE);

		d.setDescriptionComments(createFacebookComments(commentCount));
		if(tagCount < 1){
			LOGGER.debug("Tag count < 1.");
		}else{
			for(int i=0;i<tagCount;++i){
				d.addTag(createFacebookPhotoTag());
			}
		}
		return d;
	}

	/**
	 * 
	 * @return randomly generated photo tag
	 */
	public FacebookPhotoTag createFacebookPhotoTag() {
		FacebookPhotoTag tag = new FacebookPhotoTag();
		tag.setName(RandomStringUtils.randomAlphabetic(TEXT_LENGTH));
		tag.setServiceType(FacebookPhotoStorage.SERVICE_TYPE);
		return tag;
	}

	/**
	 * 
	 * @return randomly generated event
	 */
	public FacebookEvent createFacebookEvent(){
		FacebookEvent event = new FacebookEvent();

		event.setDescription(RandomStringUtils.randomAlphabetic(TEXT_LENGTH));

		Random r = _CACreator.getRandom();
		Date endTime = CAXMLObjectCreator.createRandomDate(new Date(), r);
		event.setStartTime(CAXMLObjectCreator.createRandomDate(endTime, r));

		event.setEventStatus(createRSVPStatus());

		event.setPrivacy(createPrivacy());

		event.setUpdatedTime(CAXMLObjectCreator.createRandomDate(new Date(), r));

		event.setName(RandomStringUtils.randomAlphabetic(TEXT_LENGTH));

		event.setFacebookLocation(createFacebookLocation());

		event.setEventOwnerName(RandomStringUtils.randomAlphabetic(TEXT_LENGTH));

		return event;
	}

	/**
	 * 
	 * @return randomly generated status
	 */
	public RSVPStatus createRSVPStatus(){
		RSVPStatus[] statuses = RSVPStatus.values();
		return statuses[_CACreator.getRandom().nextInt(statuses.length)];
	}

	/**
	 * 
	 * @return randomly generated privacy
	 */
	public Privacy createPrivacy(){
		Privacy[] priv = Privacy.values();
		return priv[_CACreator.getRandom().nextInt(priv.length)];
	}

	/**
	 * 
	 * @param commentCount 
	 * @return randomly generated message
	 */
	public FacebookStatusMessage createFacebookStatusMessage(int commentCount){
		FacebookStatusMessage message = new FacebookStatusMessage();
		message.setMessage(RandomStringUtils.randomAlphabetic(TEXT_LENGTH));

		message.setFromName(RandomStringUtils.randomAlphabetic(TEXT_LENGTH));

		message.setUpdatedTime(CAXMLObjectCreator.createRandomDate(new Date(), _CACreator.getRandom()));

		message.addLike(createNamedFacebookType());

		message.setMessageComments(createFacebookComments(commentCount));

		message.setMessageComments(createFacebookComments(commentCount));
		return message;
	}

	/**
	 * 
	 * @param count
	 * @return list of comment or null if count was < 1
	 */
	public List<FacebookComment> createFacebookComments(int count){
		if(count < 1){
			LOGGER.debug("Count < 1.");
			return null;
		}
		List<FacebookComment> comments = new ArrayList<>(count);
		for(int i=0;i<count;++i){
			comments.add(createFacebookComment());
		}
		return comments;
	}

	/**
	 * 
	 * @return randomly generated comment
	 */
	public FacebookComment createFacebookComment(){
		FacebookComment comment = new FacebookComment();
		Random r = _CACreator.getRandom();
		comment.setCreatedTime(CAXMLObjectCreator.createRandomDate(new Date(), r));
		comment.setFromName(RandomStringUtils.randomAlphabetic(TEXT_LENGTH));
		comment.setMessage(RandomStringUtils.randomAlphabetic(TEXT_LENGTH));
		comment.setLikeCount(Math.abs(r.nextLong()));
		return comment;
	}

	/**
	 * 
	 * @return randomly generated details
	 */
	public FacebookUserDetails createFacebookUserDetails(){
		FacebookUserDetails details = new FacebookUserDetails();
		details.setUserId(_CACreator.createUserIdentity());

		details.setBio(RandomStringUtils.randomAlphabetic(TEXT_LENGTH));

		details.setFirstName(RandomStringUtils.randomAlphabetic(TEXT_LENGTH));

		details.setMiddleName(RandomStringUtils.randomAlphabetic(TEXT_LENGTH));

		details.setLastName(RandomStringUtils.randomAlphabetic(TEXT_LENGTH));

		details.setGender(RandomStringUtils.randomAlphabetic(TEXT_LENGTH));

		details.setHometownName(RandomStringUtils.randomAlphabetic(TEXT_LENGTH));

		Random r = _CACreator.getRandom();
		details.setUpdatedTime(CAXMLObjectCreator.createRandomDate(new Date(), r));

		details.setBirthdayAsDate(CAXMLObjectCreator.createRandomDate(new Date(), r));

		details.setRelationshipStatus(RandomStringUtils.randomAlphabetic(TEXT_LENGTH));

		details.setSignificantOther(createNamedFacebookType());

		details.setFacebookLocation(createFacebookLocation());

		details.setReligion(RandomStringUtils.randomAlphabetic(TEXT_LENGTH));

		details.setPolitical(RandomStringUtils.randomAlphabetic(TEXT_LENGTH));
		
		details.setId(String.valueOf(Math.abs(r.nextLong())));

		return details;
	}

	/**
	 * 
	 * @return randomly generated like
	 */
	public FacebookLike createFacebookLike(){
		return new FacebookLike(createCategorizedFacebookType());
	}

	/**
	 * 
	 * @return randomly generated relationship
	 */
	public FacebookRelationship createFacebookRelationship(){
		FacebookRelationship ship = new FacebookRelationship();
		ship.setType(RandomStringUtils.randomAlphabetic(TEXT_LENGTH));
		ship.setWith(RandomStringUtils.randomAlphabetic(TEXT_LENGTH));
		return ship;
	}

	/**
	 * 
	 * @return randomly generated location
	 */
	public FacebookLocation createFacebookLocation(){
		FacebookLocation loc = new FacebookLocation(createLocation());
		loc.setName(RandomStringUtils.randomAlphabetic(TEXT_LENGTH));
		return loc;
	}
	
	/**
	 * 
	 * @return randomly generated location
	 */
	public Location createLocation(){
		Location loc = new Location();
		loc.setCountry(RandomStringUtils.randomAlphabetic(TEXT_LENGTH));
		loc.setState(RandomStringUtils.randomAlphabetic(TEXT_LENGTH));
		loc.setCity(RandomStringUtils.randomAlphabetic(TEXT_LENGTH));
		return loc;
	}

	/**
	 * 
	 * @return randomly generated group
	 */
	public FacebookGroup createFacebookGroup(){
		FacebookGroup group = new FacebookGroup();
		group.setDescription(RandomStringUtils.randomAlphabetic(TEXT_LENGTH));

		group.setPrivacy(createPrivacy());

		group.setUpdatedTime(CAXMLObjectCreator.createRandomDate(new Date(), _CACreator.getRandom()));

		group.setName(RandomStringUtils.randomAlphabetic(TEXT_LENGTH));

		group.setGroupOwnerName(RandomStringUtils.randomAlphabetic(TEXT_LENGTH));

		group.setFacebookLocation(createFacebookLocation());

		return group;

	}

	/**
	 * @return the includeResultInfo
	 */
	public boolean isIncludeResultInfo() {
		return _includeResultInfo;
	}

	/**
	 * @param includeResultInfo the includeResultInfo to set
	 */
	public void setIncludeResultInfo(boolean includeResultInfo) {
		_includeResultInfo = includeResultInfo;
	}

	/**
	 * @see service.tut.pori.contentanalysis.reference.CAXMLObjectCreator#createBackendId()
	 * 
	 * @return randomly generated back-end id
	 */
	public Integer createBackendId() {
		return _CACreator.createBackendId();
	}

	/**
	 * 
	 * @param backendId
	 * @param dataGroups
	 * @param limits
	 * @param userId
	 * @return randomly generated tag list
	 */
	public MediaObjectList createTagList(Integer backendId, DataGroups dataGroups, Limits limits, UserIdentity userId) {
		MediaObjectList objects = _CACreator.createMediaObjectList(null, dataGroups, limits, EnumSet.of(ServiceType.FACEBOOK_JAZZ));
		if(!MediaObjectList.isEmpty(objects)){	
			userId = UserIdentity.isValid(userId) ? userId : _CACreator.createUserIdentity();
			backendId = createBackendId();
			for(MediaObject o : objects.getMediaObjects()){
				o.setOwnerUserId(userId);
				o.setBackendId(backendId);
			}
		}
		return objects;
	}

	/**
	 * 
	 * @param dataGroups
	 * @param limits
	 * @param taskId randomly generated if null
	 * @param taskType randomly generated if null
	 * @return randomly generated task response
	 */
	public FBTaskResponse createTaskResponse(DataGroups dataGroups, Limits limits, Long taskId, TaskType taskType) {
		FBTaskResponse r = new FBTaskResponse();
		Random random = _CACreator.getRandom();
		r.setTaskId((taskId == null ? Math.abs(random.nextLong()) : taskId));
		Integer backendId = Math.abs(random.nextInt());
		r.setBackendId(backendId);
		r.setMessage(RandomStringUtils.randomAlphabetic(TEXT_LENGTH));
		r.setTaskType((taskType == null ? _CACreator.createTaskType() : taskType));
		r.setStatus(_CACreator.createTaskStatus());
		PhotoList photoList = _CACreator.createPhotoList(null, dataGroups, limits, null, _CACreator.createUserIdentity());
		if(!PhotoList.isEmpty(photoList)){
			for(service.tut.pori.contentanalysis.Photo p : photoList.getPhotos()){ // make sure all media objects have the same back-end id as the task has, and that service types match
				MediaObjectList mediaObjectList = p.getMediaObjects();
				p.setServiceType(ServiceType.FACEBOOK_PHOTO);
				if(!MediaObjectList.isEmpty(mediaObjectList)){
					for(MediaObject vo : mediaObjectList.getMediaObjects()){
						vo.setBackendId(backendId);
						vo.setServiceType(ServiceType.FACEBOOK_JAZZ);
					}
				}
			}
			r.setPhotoList(photoList);
		}
		MediaObjectList mediaObjectList = _CACreator.createMediaObjectList(null, dataGroups, limits, EnumSet.of(ServiceType.FACEBOOK_JAZZ));
		if(!MediaObjectList.isEmpty(mediaObjectList)){
			for(MediaObject vo : mediaObjectList.getMediaObjects()){ // make sure all media objects have the same back-end id as the task has
				vo.setBackendId(backendId);
			}
			r.setMediaObjects(mediaObjectList);
		}
		return r;
	}

	/**
	 * 
	 * @param objectCount
	 * @return randomly generated task details
	 */
	public FBFeedbackTaskDetails createFBFeedbackTaskDetails(int objectCount) {
		if(objectCount < 1){
			LOGGER.warn("Object count was < 1.");
			return null;
		}
		FBFeedbackTaskDetails details = new FBFeedbackTaskDetails();
		Random r = _CACreator.getRandom();
		_CACreator.populateAbstractTaskDetails(Math.abs(r.nextInt()), details, Math.abs(r.nextLong()), TaskType.FACEBOOK_PROFILE_SUMMARIZATION_FEEDBACK);
		details.setTags(_CACreator.createMediaObjectList(null, DATA_GROUPS_ALL, new Limits(0, objectCount-1), null));
		details.setCallbackUri(generateFinishedCallbackUri());
		return details;
	}
}
