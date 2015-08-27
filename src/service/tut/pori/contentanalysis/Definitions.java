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
package service.tut.pori.contentanalysis;

/**
 * Definitions for the content analysis package.
 * 
 */
public final class Definitions {
	/* services */
	/** service name declaration */
	public static final String SERVICE_CA = "ca";

	/* methods */
	/** service method declaration */
	public static final String METHOD_ADD_TASK = "addTask";
	/** service method declaration */
	public static final String METHOD_DELETE_PHOTO_METADATA = "deletePhotos";
	/** service method declaration */
	public static final String METHOD_PHOTO_ANALYSIS = "photoAnalysis";
	/** implemented by Front-end */
	public static final String METHOD_QUERY_TASK_DETAILS = "queryTaskDetails";
	/** implemented by analysis back-ends */
	public static final String METHOD_QUERY_TASK_STATUS = "queryTaskStatus";
	/** service method declaration */
	public static final String METHOD_REDIRECT = "r";
	/** service method declaration */
	public static final String METHOD_RETRIEVE_MEDIA_OBJECTS = "getObjects";
	/** service method declaration */
	public static final String METHOD_RETRIEVE_PHOTO_METADATA = "getPhotos";
	/** service method declaration */
	public static final String METHOD_SEARCH_SIMILAR_BY_CONTENT = "similarPhotosByContent";
	/** service method declaration */
	public static final String METHOD_SEARCH_SIMILAR_BY_ID = "similarPhotosById";
	/** service method declaration */
	public static final String METHOD_SEARCH_SIMILAR_BY_KEYWORD = "similarPhotosByKeyword";
	/** service method declaration */
	public static final String METHOD_SEARCH_SIMILAR_BY_OBJECT = "similarPhotosByObject";
	/** service method declaration */
	public static final String METHOD_SIMILARITY_FEEDBACK = "similarityFeedback";
	/** service method declaration */
	public static final String METHOD_TASK_FINISHED = "taskFinished";
	/** service method declaration */
	public static final String METHOD_UPDATE_PHOTO_METADATA = "updatePhotos";

	/* xml elements */
	/** xml element declaration */
	public static final String ELEMENT_ANALYSIS_TYPE = "analysisType";
	/** xml element declaration */
	public static final String ELEMENT_ANALYSIS_TYPELIST = "analysisTypeList";
	/** xml element declaration */
	public static final String ELEMENT_BACKEND = "backend";
	/** xml element declaration */
	public static final String ELEMENT_BACKEND_ID = "backendId";
	/** xml element declaration */
	public static final String ELEMENT_BACKEND_STATUS = "backendStatus";
	/** xml element declaration */
	public static final String ELEMENT_BACKEND_STATUS_LIST = "backendStatusList";
	/** xml element declaration */
	public static final String ELEMENT_CALLBACK_URI = "callbackUri";
	/** xml element declaration */
	public static final String ELEMENT_CAPABILITY = "capability";
	/** xml element declaration */
	public static final String ELEMENT_CAPABILITY_LIST = "capabilityList";
	/** xml element declaration */
	public static final String ELEMENT_CONFIDENCE = "confidence";
	/** xml element declaration */
	public static final String ELEMENT_CREDITS = "credits";
	/** xml element declaration */
	public static final String ELEMENT_DELETED_PHOTOLIST = "deletedMediaList";
	/** xml element declaration */
	public static final String ELEMENT_DESCRIPTION = "description";
	/** xml element declaration */
	public static final String ELEMENT_DISSIMILAR_PHOTOLIST = "dissimilarMediaList";
	/** xml element declaration */
	public static final String ELEMENT_ENABLED = "enabled";
	/** xml element declaration */
	public static final String ELEMENT_END_ITEM = "endItem";
	/** xml element declaration */
	public static final String ELEMENT_FEEDBACKLIST= "feedbackList";
	/** xml element declaration */
	public static final String ELEMENT_GUID = "UID";
	/** xml element declaration */
	public static final String ELEMENT_MEDIA_OBJECT = "object";
	/** xml element declaration */
	public static final String ELEMENT_MEDIA_OBJECTLIST = "objectList";
	/** xml element declaration */
	public static final String ELEMENT_MEDIA_OBJECT_ID = "mediaObjectId";
	/** xml element declaration */
	public static final String ELEMENT_MEDIA_OBJECT_TYPE = "objectType";
	/** xml element declaration */
	public static final String ELEMENT_MEDIA_TYPE = "mediaType";
	/** xml element declaration */
	public static final String ELEMENT_MESSAGE = "message";
	/** xml element declaration */
	public static final String ELEMENT_NAME = "name";
	/** xml element declaration */
	public static final String ELEMENT_OBJECT_ID = "objectId";
	/** xml element declaration */
	public static final String ELEMENT_PHOTO = "media";
	/** xml element declaration */
	public static final String ELEMENT_PHOTOLIST = "mediaList";
	/** xml element declaration */
	public static final String ELEMENT_RANK = "rank";
	/** xml element declaration */
	public static final String ELEMENT_REFERENCE_PHOTOLIST = "referenceMediaList";
	/** xml element declaration */
	public static final String ELEMENT_RESULT_COUNT = "resultCount";
	/** xml element declaration */
	public static final String ELEMENT_RESULT_INFO = "resultInfo";
	/** xml element declaration */
	public static final String ELEMENT_SERVICE_ID = "serviceId";
	/** xml element declaration */
	public static final String ELEMENT_SIMILAR_PHOTOLIST = "similarMediaList";
	/** xml element declaration */
	public static final String ELEMENT_START_ITEM = "startItem";
	/** xml element declaration */
	public static final String ELEMENT_STATUS = "status";
	/** xml element declaration */
	public static final String ELEMENT_TASK_DATAGROUPS = "taskDatagroups";
	/** xml element declaration */
	public static final String ELEMENT_TASK_ID = "taskId";
	/** xml element declaration */
	public static final String ELEMENT_TASK_DETAILS = "taskDetails";
	/** xml element declaration */
	public static final String ELEMENT_TASK_PARAMETERS = "taskParameters";
	/** xml element declaration */
	public static final String ELEMENT_TASK_RESULTS = "taskResults";
	/** xml element declaration */
	public static final String ELEMENT_TASK_TYPE = "taskType";
	/** xml element declaration */
	public static final String ELEMENT_URL = "url";
	/** xml element declaration */
	public static final String ELEMENT_USER_CONFIDENCE = "userConfidence";
	/** xml element declaration */
	public static final String ELEMENT_VALUE = "value";
	/** xml element declaration */
	public static final String ELEMENT_VISIBILITY = "visibility";
	/** xml element declaration */
	public static final String ELEMENT_VISUAL_SHAPE = "shape";
	/** xml element declaration */
	public static final String ELEMENT_VISUAL_SHAPE_TYPE = "shapeType";
	
	/* visibility values */
	/** visibility type name for group visibility */
	public static final String VISIBILITY_GROUP = "GROUP";
	/** visibility type name for private visibility */
	public static final String VISIBILITY_PRIVATE = "PRIVATE";
	/** visibility type name for public visibility */
	public static final String VISIBILITY_PUBLIC = "PUBLIC";

	/* parameters */
	/** service method parameter declaration */
	public static final String PARAMETER_ANALYSIS_TYPE = "analysis_type";
	/** service method parameter declaration */
	public static final String PARAMETER_BACKEND_ID = "backend_id";
	/** service method parameter declaration */
	public static final String PARAMETER_GUID = "uid";
	/** service method parameter declaration */
	public static final String PARAMETER_KEYWORDS = "keywords";
	/** service method parameter declaration */
	public static final String PARAMETER_MEDIA_OBJECT_ID = "media_object_id";
	/** service method parameter declaration */
	public static final String PARAMETER_SERVICE_ID = "service_id";
	/** service method parameter declaration */
	public static final String PARAMETER_SORT = "sort";
	/** service method parameter declaration */
	public static final String PARAMETER_TASK_ID = "task_id";
	/** service method parameter declaration */
	public static final String PARAMETER_URL = "url";
	
	/* common database columns */
	/** SQL database column declaration */
	public static final String COLUMN_BACKEND_ID = "backend_id";
	/** SQL database column declaration */
	public static final String COLUMN_CONFIDENCE = "confidence";
	/** SQL database column declaration */
	public static final String COLUMN_CREDITS = "credits";
	/** SQL database column declaration */
	public static final String COLUMN_DESCRIPTION = "description";
	/** SQL database column declaration */
	public static final String COLUMN_MEDIA_OBJECT_ID = "media_object_id";
	/** SQL database column declaration */
	public static final String COLUMN_NAME = "name";
	/** SQL database column declaration */
	public static final String COLUMN_SERVICE_ID = "service_id";
	/** SQL database column declaration */
	public static final String COLUMN_STATUS = "status";
	/** SQL database column declaration */
	public static final String COLUMN_VALUE = "value";
	/** SQL database column declaration */
	public static final String COLUMN_VISIBILITY = "visibility";
	
	/* data groups */
	/** not included in data group {@value core.tut.pori.http.parameters.DataGroups#DATA_GROUP_ALL} */
	public static final String DATA_GROUP_BACKEND_STATUS = "backend_status";
	/** not included in data group {@value core.tut.pori.http.parameters.DataGroups#DATA_GROUP_ALL} */
	public static final String DATA_GROUP_RESULT_INFO = "result_info";
	/** not included in data group {@value core.tut.pori.http.parameters.DataGroups#DATA_GROUP_ALL} */
	public static final String DATA_GROUP_STATUS = "status";
	/** 
	 * timecodes should be included in media objects 
	 * 
	 * @see service.tut.pori.contentanalysis.MediaObject#getTimecodes()
	 */
	public static final String DATA_GROUP_TIMECODES = "timecodes";
	/** in general internally used data group for requesting the presence of visibility data in a response @see service.tut.pori.contentanalysis.CAContentCore.Visibility */
	public static final String DATA_GROUP_VISIBILITY = "visibility";
	/* media object datagroups */
	/** data group for requesting media objects of type {@link service.tut.pori.contentanalysis.MediaObject.MediaObjectType#FACE} */
	public static final String DATA_GROUP_FACE = "face";
	/** data group for requesting media objects of type {@link service.tut.pori.contentanalysis.MediaObject.MediaObjectType#KEYWORD} */
	public static final String DATA_GROUP_KEYWORDS = "keywords";
	/** data group for requesting media objects of type {@link service.tut.pori.contentanalysis.MediaObject.MediaObjectType#METADATA} */
    public static final String DATA_GROUP_METADATA = "metadata";
    /** data group for requesting media objects of type {@link service.tut.pori.contentanalysis.MediaObject.MediaObjectType#OBJECT} */
    public static final String DATA_GROUP_OBJECT = "object";
    /* confirmation status datagroups */
    /** data group for requesting media objects with status {@link service.tut.pori.contentanalysis.MediaObject.ConfirmationStatus#BACKEND_REMOVED} */
	public static final String DATA_GROUP_BACKEND_REMOVED = "backend_removed";
	/** data group for requesting media objects with status {@link service.tut.pori.contentanalysis.MediaObject.ConfirmationStatus#CANDIDATE} */
    public static final String DATA_GROUP_CANDIDATE = "candidate";
    /** data group for requesting media objects with status {@link service.tut.pori.contentanalysis.MediaObject.ConfirmationStatus#USER_CONFIRMED} */
    public static final String DATA_GROUP_USER_CONFIRMED = "user_confirmed";
    /** data group for requesting media objects with status {@link service.tut.pori.contentanalysis.MediaObject.ConfirmationStatus#USER_REJECTED} */
	public static final String DATA_GROUP_USER_REJECTED = "user_rejected";
	
	/* service types */
	/** service id declaration */
	public static final String SERVICE_ID_PICASA = "1";
	/** service id declaration */
	public static final String SERVICE_ID_FSIO = "2";
	/** service id declaration */
	public static final String SERVICE_ID_FACEBOOK_JAZZ = "3";
	/** service id declaration */
	public static final String SERVICE_ID_FACEBOOK_PHOTO = "4";
	/** service id declaration */
	public static final String SERVICE_ID_TWITTER_JAZZ = "5";
	/** service id declaration */
	public static final String SERVICE_ID_TWITTER_PHOTO = "6";
	/** service id declaration */
	public static final String SERVICE_ID_URL_STORAGE = "7";
	
	/* solr dynamic field datatypes */
	private static final String SOLR_DATE = "_dt";
	private static final String SOLR_DOUBLE = "_d";
	private static final String SOLR_INTEGER = "_i";
	private static final String SOLR_LONG = "_l";
	private static final String SOLR_SEARCH_FIELD = "_search";	//field created in mediaObjects solr schema for case insensitive searches (actually value_search)
	private static final String SOLR_STRING = "_s";
	private static final String SOLR_TEXT = "_s";
	private static final String SOLR_TEXT_LIST = "_ss";	//_ss dynamic field for multi valued strings
	
	/* solr fields */
	/** SOLR field declaration */
	protected static final String SOLR_FIELD_BACKEND_ID = ELEMENT_BACKEND_ID+SOLR_INTEGER;
	/** SOLR field declaration */
	protected static final String SOLR_FIELD_CONFIDENCE = ELEMENT_CONFIDENCE+SOLR_DOUBLE;
	/** SOLR field declaration */
	protected static final String SOLR_FIELD_CREATOR_OBJECT_ID = ELEMENT_OBJECT_ID+SOLR_STRING;
	/** SOLR field declaration */
	protected static final String SOLR_FIELD_CREDITS = ELEMENT_CREDITS+SOLR_STRING;
	/** SOLR field declaration */
	protected static final String SOLR_FIELD_DESCRIPTION = ELEMENT_DESCRIPTION+SOLR_TEXT;
	/** SOLR field declaration */
	protected static final String SOLR_FIELD_MEDIA_OBJECT_TYPE = ELEMENT_MEDIA_OBJECT_TYPE+SOLR_INTEGER;
	/** SOLR field declaration */
	protected static final String SOLR_FIELD_MEDIA_TYPE = ELEMENT_MEDIA_TYPE+SOLR_INTEGER;
	/** SOLR field declaration */
	protected static final String SOLR_FIELD_NAME = ELEMENT_NAME+SOLR_STRING;
	/** SOLR field declaration */
	protected static final String SOLR_FIELD_RANK = ELEMENT_RANK+SOLR_INTEGER;
	/** SOLR field declaration */
	protected static final String SOLR_FIELD_SERVICE_ID = ELEMENT_SERVICE_ID+SOLR_INTEGER;
	/** SOLR field declaration */
	protected static final String SOLR_FIELD_STATUS = ELEMENT_STATUS+SOLR_INTEGER;
	/** SOLR field declaration */
	protected static final String SOLR_FIELD_TIMECODES = service.tut.pori.contentanalysis.video.Definitions.ELEMENT_TIMECODE+SOLR_TEXT_LIST;
	/** SOLR field declaration */
	protected static final String SOLR_FIELD_UPDATED = "updated"+SOLR_DATE;
	/** SOLR field declaration */
	protected static final String SOLR_FIELD_USER_ID = core.tut.pori.users.Definitions.ELEMENT_USER_ID+SOLR_LONG;
	/** SOLR field declaration */
	protected static final String SOLR_FIELD_VALUE = ELEMENT_VALUE+SOLR_TEXT;
	/** SOLR field declaration */
	protected static final String SOLR_FIELD_VALUE_SEARCH = ELEMENT_VALUE+SOLR_SEARCH_FIELD;
	/** SOLR field declaration */
	protected static final String SOLR_FIELD_VISIBILITY = ELEMENT_VISIBILITY+SOLR_INTEGER;
	/** SOLR field declaration */
	protected static final String SOLR_FIELD_VISUAL_SHAPE_TYPE = ELEMENT_VISUAL_SHAPE_TYPE+SOLR_INTEGER;
	/** SOLR field declaration */
	protected static final String SOLR_FIELD_VISUAL_SHAPE_VALUE = ELEMENT_VISUAL_SHAPE+SOLR_STRING;
	
	/**
	 * 
	 */
	private Definitions(){
		// nothing needed
	}
}
