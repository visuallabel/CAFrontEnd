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
package service.tut.pori.twitterjazz;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.http.client.utils.DateUtils;
import org.apache.log4j.Logger;

import service.tut.pori.contentanalysis.Definitions;
import service.tut.pori.contentanalysis.Photo;
import service.tut.pori.contentanalysis.PhotoDAO;
import service.tut.pori.contentanalysis.PhotoList;
import service.tut.pori.contentanalysis.MediaObject;
import service.tut.pori.contentanalysis.MediaObjectList;
import service.tut.pori.contentstorage.TwitterDAO;
import service.tut.pori.contentstorage.TwitterPhotoStorage;
import service.tut.pori.contentstorage.TwitterPhotoStorage.TwitterEntry;
import service.tut.pori.users.twitter.TwitterProperties;
import service.tut.pori.users.twitter.TwitterUserDAO;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import core.tut.pori.context.ServiceInitializer;
import core.tut.pori.http.parameters.DataGroups;
import core.tut.pori.users.UserIdentity;

/**
 * High-level client for extracting profile details from Twitter.
 * 
 * This class is NOT thread-safe
 */
public class TwitterExtractor {
	private static final int DEFAULT_LIMIT = 200;
	private static final String HEADER_DATE = "date";
	private static final Logger LOGGER = Logger.getLogger(TwitterExtractor.class);
	private static final int MAX_RETRIES = 5;
	private static final int STATUS_CODE_LIMIT_EXCEEDED_V1 = 420;
	private static final int STATUS_CODE_LIMIT_EXCEEDED_V11 = 429;
	private UserIdentity _userId = null;
	private Twitter _twitter = null;
	private boolean _filterDescriptions = true; // filter video and photo descriptions for descriptions not belonging to the profile owner
	private boolean _abortOnRateLimit = false;

	/**
	 * Valid content types for a profile
	 * 
	 */
	public enum ContentType{
		/** Include tags generated by other back-ends. Can only be used in combination with PHOTO_DESCRIPTION */
		GENERATED_TAGS,
		/** photo descriptions generated from Twitter photos and status messages */
		PHOTO_DESCRIPTIONS,
		/** Twitter status messages */
		STATUS_MESSAGES,
		/** video descriptions generated from Twitter photos and status messages */
		VIDEO_DESCRIPTIONS;
		
		/**
		 * 
		 * @param values
		 * @return set of ContentTypes or null if null or empty collection was passed
		 * @throws IllegalArgumentException on bad value
		 */
		public static EnumSet<ContentType> fromString(Collection<String> values) throws IllegalArgumentException {
			EnumSet<ContentType> contentTypes = null;
			if(values != null && !values.isEmpty()){
				contentTypes = EnumSet.noneOf(ContentType.class);
				for(String value : values){
					ContentType found = null;
					for(ContentType t : values()){
						if(t.name().equalsIgnoreCase(value)){
							found = t;
							break;
						}
					} // for types
					if(found == null){
						throw new IllegalArgumentException("Bad ContentType: "+value);
					}
					contentTypes.add(found);
				} // for values
			}
			return contentTypes;
		}
	} // enum ContentType

	/**
	 * 
	 * @param userId
	 * @return the extractor or null on failure
	 */
	public static TwitterExtractor getExtractor(UserIdentity userId){
		TwitterExtractor extractor = null;
		AccessToken token = ServiceInitializer.getDAOHandler().getSQLDAO(TwitterUserDAO.class).getAccessToken(userId);
		if(token == null){
			LOGGER.warn("No token.");
			return null;
		}
		extractor = new TwitterExtractor(userId);
		extractor._twitter = ServiceInitializer.getPropertyHandler().getSystemProperties(TwitterProperties.class).getTwitterFactory().getInstance(token);
		return extractor;
	}

	/**
	 * 
	 * @param userIdentity
	 */
	private TwitterExtractor(UserIdentity userIdentity){
		_userId = userIdentity;
	}

	/**
	 * 
	 * @param contentTypes
	 * @param screenNames
	 * @return list of found profiles or null on failure
	 */
	public List<TwitterProfile> getProfiles(EnumSet<ContentType> contentTypes, String[] screenNames){
		if(ArrayUtils.isEmpty(screenNames)){
			LOGGER.warn("Empty screen name list.");
			return null;
		}

		List<TwitterUserDetails> userDetails = null;
		int retriesLeft = MAX_RETRIES;
		while(userDetails == null && retriesLeft > 0){
			try {
				LOGGER.debug("Retrieving user details...");
				userDetails = TwitterUserDetails.getTwitterUserDetails(_twitter.lookupUsers(screenNames));
				if(userDetails == null){
					LOGGER.warn("Failed to resolve user details.");
					return null;
				}
			} catch (TwitterException ex) {
				if(!handleTwitterException(ex)){
					return null;
				}
				--retriesLeft;
			}
		}

		List<TwitterProfile> profiles = new ArrayList<>(userDetails.size());

		if(contentTypes != null && !contentTypes.isEmpty()){
			boolean generatedTags = contentTypes.contains(ContentType.GENERATED_TAGS);
			if(generatedTags && contentTypes.size() == 1){
				throw new IllegalArgumentException("Only "+ContentType.GENERATED_TAGS.name()+" given.");
			}
			boolean hasStatusMessages = contentTypes.contains(ContentType.STATUS_MESSAGES);
			boolean hasVideoDescriptions = contentTypes.contains(ContentType.VIDEO_DESCRIPTIONS);
			boolean hasPhotoDescriptions = contentTypes.contains(ContentType.PHOTO_DESCRIPTIONS);
			if(hasVideoDescriptions || hasPhotoDescriptions || hasStatusMessages){
				for(TwitterUserDetails details : userDetails){
					TwitterProfile profile = new TwitterProfile(details);
					profiles.add(profile);

					List<Status> statuses = getStatuses(details.getScreenName());
					if(statuses != null){
						if(hasStatusMessages){
							profile.setStatusMessages(getStatusMessages(statuses));
						}

						List<String> filter = null;
						if(_filterDescriptions){
							filter = Arrays.asList(details.getTwitterId());
						}

						if(hasPhotoDescriptions){
							List<TwitterPhotoDescription> descriptions = getPhotoDescriptions(generatedTags, statuses, filter);
							LOGGER.debug("Photo descriptions extracted: "+(descriptions == null ? "0" : descriptions.size()));
							profile.setPhotoDescriptions(descriptions);
						}

						if(hasVideoDescriptions){
							List<TwitterVideoDescription> descriptions = getVideoDescriptions(statuses, filter);
							LOGGER.debug("Video descriptions extracted: "+(descriptions == null ? "0" : descriptions.size()));
							profile.setVideoDescriptions(descriptions);
						}
					} // if
				} // for
			} // if
		}else{
			LOGGER.debug("No content types requested.");
			for(TwitterUserDetails details : userDetails){
				profiles.add(new TwitterProfile(details));		
			}
		}

		return profiles;
	}

	/**
	 * 
	 * @param contentTypes
	 * @return the profile or null on failure
	 * @throws IllegalArgumentException on bad content types
	 */
	public TwitterProfile getProfile(EnumSet<ContentType> contentTypes) throws IllegalArgumentException{
		TwitterUserDetails userDetails = null;
		int retriesLeft = MAX_RETRIES;
		while(userDetails == null && retriesLeft > 0){
			try {
				LOGGER.debug("Retrieving user details...");
				userDetails = TwitterUserDetails.getTwitterUserDetails(_twitter.verifyCredentials());
				if(userDetails == null){
					LOGGER.warn("Failed to resolve user details.");
					return null;
				}
			} catch (TwitterException ex) {
				if(!handleTwitterException(ex)){
					return null;
				}
				--retriesLeft;
			}
		}
		userDetails.setUserId(_userId);
		TwitterProfile profile = new TwitterProfile(userDetails);

		if(contentTypes != null && !contentTypes.isEmpty()){
			boolean generatedTags = contentTypes.contains(ContentType.GENERATED_TAGS);
			if(generatedTags && contentTypes.size() == 1){
				throw new IllegalArgumentException("Only "+ContentType.GENERATED_TAGS.name()+" given.");
			}
			boolean hasStatusMessages = contentTypes.contains(ContentType.STATUS_MESSAGES);
			boolean hasVideoDescriptions = contentTypes.contains(ContentType.VIDEO_DESCRIPTIONS);
			boolean hasPhotoDescriptions = contentTypes.contains(ContentType.PHOTO_DESCRIPTIONS);
			if(hasVideoDescriptions || hasPhotoDescriptions || hasStatusMessages){
				List<Status> statuses = getStatuses(null);
				if(statuses != null){
					if(hasStatusMessages){
						profile.setStatusMessages(getStatusMessages(statuses));
					}

					List<String> filter = null;
					if(_filterDescriptions){
						filter = Arrays.asList(userDetails.getTwitterId());
					}

					if(hasPhotoDescriptions){
						profile.setPhotoDescriptions(getPhotoDescriptions(generatedTags, statuses, filter));
					}

					if(hasVideoDescriptions){
						profile.setVideoDescriptions(getVideoDescriptions(statuses, filter));
					}
				} // if
			} // if
		}else{
			LOGGER.debug("No content types requested.");
		}

		return profile;
	}

	/**
	 * 
	 * @return all photo descriptions found on the current user's timeline without generated tags
	 */
	public List<TwitterPhotoDescription> getPhotoDescriptions() {
		return getPhotoDescriptions(false, null);
	}
	
	/**
	 * 
	 * @param descriptions
	 * @param entityId
	 * @return true if the description collection already contains a description with the given entity id
	 */
	private boolean photosContains(Collection<TwitterPhotoDescription> descriptions, String entityId){
		for(TwitterPhotoDescription d : descriptions){
			if(entityId.equals(d.getEntityId())){
				return true;
			}
		}
		return false;
	}

	/**
	 * 
	 * @param generatedTags 
	 * @param statuses
	 * @param twitterUserIdFilter
	 * @return list of photo descriptions or null if none was found
	 */
	private List<TwitterPhotoDescription> getPhotoDescriptions(boolean generatedTags, List<Status> statuses, Collection<String> twitterUserIdFilter) {
		if(statuses == null){
			return null;
		}
		int sCount = statuses.size();
		LOGGER.debug("Processing status messages for photo descriptions, messages: "+sCount);

		List<String> entityIds = new ArrayList<>(sCount);
		List<TwitterPhotoDescription> descriptions = new ArrayList<>(sCount);
		for(Status s : statuses){
			if(twitterUserIdFilter != null && !twitterUserIdFilter.contains(String.valueOf(s.getUser().getId()))){
				LOGGER.debug("Ignoring status message based on the given filter twitter user id filter.");
				continue;
			}
			List<TwitterPhotoDescription> p = TwitterPhotoDescription.getTwitterPhotoDescriptions(s); // the factory object will check for the correct type
			if(p != null){
				for(TwitterPhotoDescription d : p){
					String entityId = d.getEntityId();
					if(photosContains(descriptions, entityId)){ // ignore duplicates
						continue;
					}
					descriptions.add(d);
					entityIds.add(entityId);
				}
			} //if
		}

		if(descriptions.isEmpty()){
			LOGGER.debug("No photo descriptions in the given list of statuses.");
			return null;
		}

		List<TwitterEntry> entries = ServiceInitializer.getDAOHandler().getSQLDAO(TwitterDAO.class).getEntriesByEntityId(entityIds, _userId);
		if(entries == null){
			LOGGER.debug("None of the photos are known by the system.");
		}else{
			PhotoList gTags = null;
			if(generatedTags){
				LOGGER.debug("Retrieving generated tags.");
				List<String> guids = new ArrayList<>(entries.size());
				for(TwitterEntry e : entries){
					guids.add(e.getGUID());
				}
				gTags = ServiceInitializer.getDAOHandler().getSolrDAO(PhotoDAO.class).getPhotos(new DataGroups(Definitions.DATA_GROUP_KEYWORDS), guids, null, null, null);
			}
			
			LOGGER.debug("Resolving photo GUIDs for descriptions.");
			for(TwitterPhotoDescription d : descriptions){
				String objectId = d.getEntityId();
				String screenName = d.getFromName();
				for(Iterator<TwitterEntry> eIter = entries.iterator(); eIter.hasNext();){
					TwitterEntry e = eIter.next();
					if(e.getEntityId().equals(objectId) && e.getScreenName().equals(screenName)){ // the same entity id might appear for different users' content so also match by screen name
						String guid = e.getGUID();
						d.setPhotoGUID(guid);
						d.setServiceType(TwitterPhotoStorage.SERVICE_TYPE); // no need to check from database, all photos from TwitterDAO entries are of the same type
						if(gTags != null){  // if tags were found
							Photo p = gTags.getPhoto(guid); // in practice this should always return a photo...
							if(p != null){
								MediaObjectList objects = p.getMediaObjects();
								if(!MediaObjectList.isEmpty(objects)){
									for(MediaObject vo : objects.getMediaObjects()){
										d.addTag(TwitterPhotoTag.getTwitterTag(vo));
									} // for
								} // if photo had media objects
							}else{ // ..though there is theoretical possibility that the photo has been removed in between retrievals (which are not in a transaction), and does not exist anymore
								LOGGER.warn("No photo found, GUID: "+guid);
								d.setPhotoGUID(null); // not valid anymore
							} // else
						} // if
						eIter.remove(); // remove entry to prevent unnecessary looping in the following loops
					}
				} // for entries
			} // for descriptions
		}

		return descriptions;
	}		

	/**
	 * 
	 * @param generatedTags if true the generated tags will be retrieved from the database
	 * @param twitterUserIdFilter if given, only descriptions from the given users will be returned
	 * @return list of photo descriptions or null if none was found
	 */
	public List<TwitterPhotoDescription> getPhotoDescriptions(boolean generatedTags, Collection<String> twitterUserIdFilter) {
		return getPhotoDescriptions(generatedTags, getStatuses(null), twitterUserIdFilter);
	}

	/**
	 * 
	 * @return all video descriptions found on user's timeline
	 */
	public List<TwitterVideoDescription> getVideoDescriptions(){
		return getVideoDescriptions(null);
	}

	/**
	 * 
	 * @param twitterUserIdFilter if given, only descriptions from these users will be returned
	 * @return list of video descriptions or null if none was found
	 */
	public List<TwitterVideoDescription> getVideoDescriptions(Collection<String> twitterUserIdFilter) {
		return getVideoDescriptions(getStatuses(null), twitterUserIdFilter);
	}

	/**
	 * extract video descriptions from the given list of statuses
	 * 
	 * @param statuses
	 * @param twitterUserIdFilter
	 * @return list of video descriptions or null if none was found
	 */
	private List<TwitterVideoDescription> getVideoDescriptions(List<Status> statuses, Collection<String> twitterUserIdFilter){
		if(statuses == null){
			return null;
		}

		int sCount = statuses.size();
		LOGGER.debug("Processing status messages for photo descriptions, messages: "+sCount);

		List<TwitterVideoDescription> descriptions = new ArrayList<>(sCount);
		for(Status s : statuses){
			if(twitterUserIdFilter != null && !twitterUserIdFilter.contains(String.valueOf(s.getUser().getId()))){
				LOGGER.debug("Ignoring status message based on the given filter twitter user id filter.");
				continue;
			}
			TwitterVideoDescription v = TwitterVideoDescription.getTwitterVideoDescription(s);
			if(v != null){
				descriptions.add(v);
			}
		}

		return (descriptions.isEmpty() ? null : descriptions);
	}

	/**
	 * 
	 * @param screenName if null, the home timeline of the authenticated user will be retrieved
	 * @return list of statuses or null if none found 
	 * @throws IllegalArgumentException
	 */
	private List<Status> getStatuses(String screenName) throws IllegalArgumentException{
		Paging paging = new Paging();
		paging.setCount(DEFAULT_LIMIT);
		List<Status> statuses = new ArrayList<>();
		int retriesLeft = MAX_RETRIES;
		while(retriesLeft > 0){
			try {
				ResponseList<Status> temp = (screenName == null ? _twitter.getHomeTimeline(paging) : _twitter.getUserTimeline(screenName, paging));
				int received = temp.size();
				if(received < 1){ // did not receive anything
					break;
				}else if(received == DEFAULT_LIMIT){ // there may be more
					long lowestId = Long.MAX_VALUE;
					for(Status s : temp){
						long tempId = s.getId();
						if(tempId < lowestId){
							lowestId = tempId;
						}
						statuses.add(s);
					} // for

					paging.setMaxId(lowestId-1);
				}else{ // this is all there is
					for(Status s : temp){
						statuses.add(s);
					}
					break;
				} // else
			} catch (TwitterException ex) {
				if(!handleTwitterException(ex)){
					return null;
				}
				--retriesLeft;
			}
		} // while

		return (statuses.isEmpty() ? null : statuses);
	}

	/**
	 * 
	 * @return list of status messages or null if none is available
	 */
	public List<TwitterStatusMessage> getStatusMessages() {
		return getStatusMessages(getStatuses(null));
	}

	/**
	 * extracts status messages from the given list of statuses
	 * 
	 * @param statuses
	 * @return list of status messages or null if none was found
	 */
	private List<TwitterStatusMessage> getStatusMessages(List<Status> statuses){
		if(statuses == null){
			return null;
		}
		List<TwitterStatusMessage> messages = new ArrayList<>(statuses.size());
		for(Status s : statuses){
			messages.add(TwitterStatusMessage.getTwitterStatusMessage(s));
		}

		return (messages.isEmpty() ? null : messages);
	}

	/**
	 * @return the userId
	 */
	public UserIdentity getUserId() {
		return _userId;
	}

	/**
	 * filter video and photo descriptions for descriptions not belonging to the profile owner
	 * 
	 * @return the filterDescriptions
	 */
	public boolean isFilterDescriptions() {
		return _filterDescriptions;
	}

	/**
	 * filter video and photo descriptions for descriptions not belonging to the profile owner
	 * 
	 * @param filterDescriptions the filterDescriptions to set
	 */
	public void setFilterDescriptions(boolean filterDescriptions) {
		_filterDescriptions = filterDescriptions;
	}

	/**
	 * @return the abortOnRateLimit
	 */
	public boolean isAbortOnRateLimit() {
		return _abortOnRateLimit;
	}

	/**
	 * @param abortOnRateLimit the abortOnRateLimit to set
	 */
	public void setAbortOnRateLimit(boolean abortOnRateLimit) {
		_abortOnRateLimit = abortOnRateLimit;
	}

	/**
	 * 
	 * @param exception
	 * @return true if the exception was handled successfully
	 */
	private boolean handleTwitterException(TwitterException exception) {
		int code = exception.getStatusCode();
		LOGGER.debug("Twitter responded with code: "+code);
		if(code != STATUS_CODE_LIMIT_EXCEEDED_V11 && code != STATUS_CODE_LIMIT_EXCEEDED_V1){
			LOGGER.error(exception, exception);
			return false;	
		}

		LOGGER.debug(exception, exception);
		if(_abortOnRateLimit){
			LOGGER.warn("Abort on rate limit on, aborting...");
			return false;
		}

		long waitTime = ((long)exception.getRateLimitStatus().getResetTimeInSeconds())*1000-DateUtils.parseDate(exception.getResponseHeader(HEADER_DATE)).getTime()+2000; // use twitter's time, as the server clocks may be out-of-sync, add random +2 second delay just in case
		if(waitTime < 1){
			LOGGER.warn("Invalid wait time: "+waitTime);
			return false;
		}

		try {
			LOGGER.debug("Waiting for next request window: "+waitTime);
			Thread.sleep(waitTime);
		} catch (InterruptedException ex) {
			LOGGER.error(ex, ex);
			return false;
		}

		return true;
	}
}
