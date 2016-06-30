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
package service.tut.pori.twitterjazz.reference;

import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.apache.commons.lang3.RandomStringUtils;

import service.tut.pori.contentanalysis.AsyncTask.TaskType;
import service.tut.pori.contentanalysis.CAContentCore.ServiceType;
import service.tut.pori.contentanalysis.PhotoList;
import service.tut.pori.contentanalysis.MediaObject;
import service.tut.pori.contentanalysis.MediaObjectList;
import service.tut.pori.contentanalysis.reference.CAXMLObjectCreator;
import service.tut.pori.contentstorage.TwitterPhotoStorage;
import service.tut.pori.twitterjazz.TwitterExtractor.ContentType;
import service.tut.pori.twitterjazz.TwitterLocation;
import service.tut.pori.twitterjazz.TwitterPhotoDescription;
import service.tut.pori.twitterjazz.TwitterPhotoTag;
import service.tut.pori.twitterjazz.TwitterProfile;
import service.tut.pori.twitterjazz.TwitterStatusMessage;
import service.tut.pori.twitterjazz.TwitterSummarizationTaskDetails;
import service.tut.pori.twitterjazz.TwitterTaskResponse;
import service.tut.pori.twitterjazz.TwitterUserDetails;
import service.tut.pori.twitterjazz.TwitterVideoDescription;
import core.tut.pori.context.ServiceInitializer;
import core.tut.pori.http.parameters.DataGroups;
import core.tut.pori.http.parameters.Limits;
import core.tut.pori.users.UserIdentity;

/**
 * 
 * class that can be used to created example objects/object lists
 * 
 */
public class TJXMLObjectCreator {
	private static final int TEXT_LENGTH = 64;
	private static int TWITTER_CHARACTER_LIMIT = 140;
	private static int WORD_MIN_LENGTH = 3;
	private static int WORD_MAX_LENGTH = 10;
	private CAXMLObjectCreator _CACreator = null;
	
	/**
	 * 
	 * @param seed for random generator, or null to use default (system time in nanoseconds)
	 */
	public TJXMLObjectCreator(Long seed){
		_CACreator = new CAXMLObjectCreator(seed);
	}

	/**
	 * @return the seed
	 */
	public long getSeed() {
		return _CACreator.getSeed();
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
	 * @param dataGroups 
	 * @param limits
	 * @param taskId if null, generated randomly
	 * @param taskType  if null, generated randomly
	 * @return randomly generated task response
	 */
	public TwitterTaskResponse createTwitterTaskResponse(DataGroups dataGroups, Limits limits, Long taskId, TaskType taskType) {
		TwitterTaskResponse r = new TwitterTaskResponse();
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
	 * @param photoDescriptionCount
	 * @param statusMessageCount
	 * @param videoDescriptionCount
	 * @param tagCount 
	 * @return randomly generated profile
	 */
	public TwitterProfile createTwitterProfile(int photoDescriptionCount, int statusMessageCount, int videoDescriptionCount, int tagCount){
		TwitterProfile profile = new TwitterProfile(createTwitterUserDetails());
		
		if(photoDescriptionCount > 0){
			List<TwitterPhotoDescription> descriptions = new ArrayList<>(photoDescriptionCount);
			for(int i=0;i<photoDescriptionCount;++i){
				descriptions.add(createTwitterPhotoDescription(tagCount));
			}
			profile.setPhotoDescriptions(descriptions);
		}
		
		if(videoDescriptionCount > 0){
			List<TwitterVideoDescription> descriptions = new ArrayList<>(videoDescriptionCount);
			for(int i=0;i<videoDescriptionCount;++i){
				descriptions.add(createTwitterVideoDescription());
			}
			profile.setVideoDescriptions(descriptions);
		}
		
		if(statusMessageCount > 0){
			List<TwitterStatusMessage> statuses = new ArrayList<>(statusMessageCount);
			for(int i=0;i<statusMessageCount;++i){
				statuses.add(createTwitterStatusMessage());
			}
			profile.setStatusMessages(statuses);
		}
		
		return profile;
	}
	
	/**
	 * 
	 * @return randomly generated details
	 */
	public TwitterUserDetails createTwitterUserDetails(){
		TwitterUserDetails tud = new TwitterUserDetails();
		tud.setBio(RandomStringUtils.randomAlphabetic(TEXT_LENGTH));
		Random r = _CACreator.getRandom();
		tud.setFavoritesCount(Math.abs(r.nextInt()));
		tud.setFriendsCount(Math.abs(r.nextInt()));
		tud.setFollowersCount(Math.abs(r.nextInt()));
		tud.setName(RandomStringUtils.randomAlphabetic(TEXT_LENGTH));
		tud.setScreenName(RandomStringUtils.randomAlphabetic(TEXT_LENGTH));
		tud.setTwitterId(String.valueOf(Math.abs(r.nextLong())));
		tud.setLocation(createTwitterLocation());
		tud.setProtected(r.nextBoolean());
		tud.setUserId(_CACreator.createUserIdentity());
		return tud;
	}
	
	/**
	 * 
	 * @return randomly generated video description
	 */
	public TwitterVideoDescription createTwitterVideoDescription(){
		TwitterVideoDescription d = new TwitterVideoDescription();
		d.setCreatedTime(CAXMLObjectCreator.createRandomDate(new Date(), _CACreator.getRandom()));
		d.setLocation(createTwitterLocation());
		d.setDescription(createStatusMessageText());
		d.setFromScreenName(RandomStringUtils.randomAlphabetic(TEXT_LENGTH));
		return d;
	}
	
	/**
	 * 
	 * @param tagCount 
	 * @return randomly generated photo description
	 */
	public TwitterPhotoDescription createTwitterPhotoDescription(int tagCount){
		TwitterPhotoDescription tp = new TwitterPhotoDescription();
		Random r = _CACreator.getRandom();
		tp.setCreatedTime(CAXMLObjectCreator.createRandomDate(new Date(),r));
		tp.setDescription(createStatusMessageText());
		tp.setFromName(RandomStringUtils.randomAlphabetic(TEXT_LENGTH));
		tp.setLocation(createTwitterLocation());
		tp.setEntityUrl(createRandomSecureUrl());
		tp.setEntityId(String.valueOf(Math.abs(r.nextLong())));
		tp.setPhotoGUID(UUID.randomUUID().toString());
		tp.setServiceType(TwitterPhotoStorage.SERVICE_TYPE);
		for(int i=0;i<tagCount;++i){
			tp.addTag(createTwitterPhotoTag());
		}
		return tp;
	}
	
	/**
	 * 
	 * @return randomly generated location
	 */
	public TwitterLocation createTwitterLocation(){
		TwitterLocation location = new TwitterLocation();
		location.setName(RandomStringUtils.randomAlphabetic(TEXT_LENGTH));
		Random r = _CACreator.getRandom();
		location.setLatitude((double) (r.nextInt(180)-90));
		location.setLongitude((double) (r.nextInt(360)-180));
		return location;
	}
	
	/**
	 * 
	 * @return randomly generated tag
	 */
	public TwitterPhotoTag createTwitterPhotoTag(){
		TwitterPhotoTag tag = new TwitterPhotoTag();
		tag.setServiceType(TwitterPhotoStorage.SERVICE_TYPE);
		tag.setValue(RandomStringUtils.randomAlphabetic(TEXT_LENGTH));
		return tag;
	}
	
	/**
	 * 
	 * @return randomly generated status message
	 */
	public TwitterStatusMessage createTwitterStatusMessage(){
		TwitterStatusMessage message = new TwitterStatusMessage();
		message.setFromName(RandomStringUtils.randomAlphabetic(TEXT_LENGTH));
		message.setMessage(createStatusMessageText());
		message.setUpdatedTime(CAXMLObjectCreator.createRandomDate(new Date(), _CACreator.getRandom()));
		return message;
	}
	
	/**
	 * 
	 * @return randomly generated URL with SSL-prefix (HTTPS)
	 */
	public static String createRandomSecureUrl(){
		return CAXMLObjectCreator.createRandomUrl().replace("http://", "https://");
	}
	
	/**
	 * 
	 * @param photoDescriptionCount
	 * @param statusMessageCount
	 * @param videoDescriptionCount
	 * @param tagCount 
	 * @return randomly generated task details
	 */
	public TwitterSummarizationTaskDetails createTwitterSummarizationTaskDetails(int photoDescriptionCount, int statusMessageCount, int videoDescriptionCount, int tagCount) {
		TwitterSummarizationTaskDetails details = new TwitterSummarizationTaskDetails();
		Random r = _CACreator.getRandom();
		_CACreator.populateAbstractTaskDetails(Math.abs(r.nextInt()), details, Math.abs(r.nextLong()), TaskType.TWITTER_PROFILE_SUMMARIZATION);
		details.setProfile(createTwitterProfile(photoDescriptionCount, videoDescriptionCount, statusMessageCount, tagCount));
		details.setContentTypes(EnumSet.allOf(ContentType.class));
		details.setCallbackUri(generateFinishedCallbackUri()); // override the default uri
		return details;
	}
	
	/**
	 * 
	 * @return the default task finished callback uri
	 */
	public String generateFinishedCallbackUri(){
		return ServiceInitializer.getPropertyHandler().getRESTBindContext()+service.tut.pori.twitterjazz.reference.Definitions.SERVICE_TJ_REFERENCE_SERVER+"/"+service.tut.pori.contentanalysis.Definitions.METHOD_TASK_FINISHED;
	}
	
	/**
	 * 
	 * @return randomly generated text
	 */
	public String createStatusMessageText(){
		StringBuilder text = new StringBuilder(TWITTER_CHARACTER_LIMIT);
		for(int length=0;length<TWITTER_CHARACTER_LIMIT;length=text.length()){
			String word = createRandomWord();
			if(_CACreator.getRandom().nextBoolean()){ // make occasionally a hashtag
				if(word.length()+length+1 < TWITTER_CHARACTER_LIMIT){
					text.append("#");
					text.append(word);
				}else{ // no more room for a hashtag
					break;
				}
			}else if(word.length()+length < TWITTER_CHARACTER_LIMIT){
				text.append(word);
			}else{ // no more room for a word
				break;
			}
			text.append(' ');
		}
		return text.toString().trim();
	}
	
	/**
	 * 
	 * @return randomly generated word
	 */
	private String createRandomWord(){
		return RandomStringUtils.randomAlphanumeric(_CACreator.getRandom().nextInt(WORD_MAX_LENGTH-WORD_MIN_LENGTH)+WORD_MIN_LENGTH);
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
		MediaObjectList objects = _CACreator.createMediaObjectList(null, dataGroups, limits, EnumSet.of(ServiceType.TWITTER_JAZZ));
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
}
