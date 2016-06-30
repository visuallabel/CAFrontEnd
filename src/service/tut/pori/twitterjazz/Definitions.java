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
package service.tut.pori.twitterjazz;

/**
 * Definitions for package twitterjazz.
 */
public final class Definitions {
	/* services */
	/** service name declaration */
	public static final String SERVICE_TJ = "tj";
	/* elements */
	/** xml element declaration */
	public static final String ELEMENT_TWITTER_PROFILE = "profile";
	/** xml element declaration */
	public static final String ELEMENT_BIO = "bio";
	/** xml element declaration */
	public static final String ELEMENT_CREATED_TIMESTAMP = "createdTimestamp";
	/** xml element declaration */
	public static final String ELEMENT_DESCRIPTION = "description";
	/** xml element declaration */
	public static final String ELEMENT_FAVORITES_COUNT = "favoritesCount";
	/** xml element declaration */
	public static final String ELEMENT_FOLLOWERS_COUNT = "followersCount";
	/** xml element declaration */
	public static final String ELEMENT_FRIENDS_COUNT = "friendsCount";
	/** xml element declaration */
	public static final String ELEMENT_LATITUDE = "latitude";
	/** xml element declaration */
	public static final String ELEMENT_LOCATION = "location";
	/** xml element declaration */
	public static final String ELEMENT_LONGITUDE = "longitude";
	/** xml element declaration */
	public static final String ELEMENT_NAME = "name";
	/** xml element declaration */
	public static final String ELEMENT_MESSAGE = "message";
	/** xml element declaration */
	public static final String ELEMENT_MESSAGE_POSTER = "from";
	/** xml element declaration */
	public static final String ELEMENT_PHOTO_DESCRIPTION = "photoDescription";
	/** xml element declaration */
	public static final String ELEMENT_PHOTO_DESCRIPTION_LIST = "photoDescriptionList";
	/** xml element declaration */
	public static final String ELEMENT_PHOTO_GUID = "photoUID";
	/** xml element declaration */
	public static final String ELEMENT_PHOTO_TAG = "photoTag";
	/** xml element declaration */
	public static final String ELEMENT_PHOTO_TAG_LIST = "photoTagList";
	/** xml element declaration */
	public static final String ELEMENT_SCREEN_NAME = "screenName";
	/** xml element declaration */
	public static final String ELEMENT_STATUS_MESSAGE = "statusMessage";
	/** xml element declaration */
	public static final String ELEMENT_STATUS_MESSAGE_LIST = "statusMessageList";
	/** xml element declaration */
	public static final String ELEMENT_TWITTER_ID = "twitterId";
	/** xml element declaration */
	public static final String ELEMENT_UPDATED_TIMESTAMP = "updatedTimestamp";
	/** xml element declaration */
	public static final String ELEMENT_USER_DETAILS = "userDetails";
	/** xml element declaration */
	public static final String ELEMENT_VALUE = "value";
	/** xml element declaration */
	public static final String ELEMENT_VIDEO_DESCRIPTION = "videoDescription";
	/** xml element declaration */
	public static final String ELEMENT_VIDEO_DESCRIPTION_LIST = "videoDescriptionList";
	
	/* methods */
	/** service method declaration */
	public static final String METHOD_RETRIEVE_TAGS_FOR_USER = "getTags";
	/** service method declaration */
	public static final String METHOD_SET_RANK = "setRank";
	/** service method declaration */
	public static final String METHOD_SUMMARIZE = "summarize";
	
	/* params */
	/** service method parameter declaration */
	public static final String PARAMETER_CONTENT_TYPES = "content_types";
	/** service method parameter declaration */
	public static final String PARAMETER_RANK = "rank";
	/** service method parameter declaration */
	public static final String PARAMETER_SCREEN_NAMES = "screen_names";
	/** service method parameter declaration */
	public static final String PARAMETER_SYNCHRONIZE = "synchronize";
	
	/* types */
	/** media entity type for photos */
	public static final String TWITTER_TYPE_PHOTO = "photo";
	/** media entity type for videos */
	public static final String TWITTER_TYPE_VIDEO = "video";
	
	/**
	 * 
	 */
	private Definitions(){
		// nothing needed
	}
}
