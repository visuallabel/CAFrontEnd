/**
 * Copyright 2015 Tampere University of Technology, Pori Unit
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
package service.tut.pori.contentanalysis.video;

/**
 * Definitions for video content analysis.
 */
public final class Definitions {
	/* elements */
	/** xml element declaration */
	public static final String ELEMENT_DELETED_VIDEOLIST = "deletedMediaList";
	/** xml element declaration */
	public static final String ELEMENT_SEQUENCE_DURATION = "sequenceDuration";
	/** xml element declaration */
	public static final String ELEMENT_SEQUENCE_TYPE = "sequenceType";
	/** xml element declaration */
	public static final String ELEMENT_TIMECODE = "timeCode";
	/** xml element declaration */
	public static final String ELEMENT_TIMECODELIST = "timeCodeList";
	/** xml element declaration */
	public static final String ELEMENT_TIMECODE_START = "from";
	/** xml element declaration */
	public static final String ELEMENT_TIMECODE_END = "to";
	/** xml element declaration */
	public static final String ELEMENT_VIDEO = "media";
	/** xml element declaration */
	public static final String ELEMENT_VIDEOLIST = "mediaList";
	
	/* methods */
	/** service method declaration */
	public static final String METHOD_RETRIEVE_VIDEO_METADATA = "getVideos";
	/** service method declaration */
	public static final String METHOD_SEARCH_SIMILAR_BY_ID = "similarVideosById";
	/** service method declaration */
	public static final String METHOD_SEARCH_SIMILAR_BY_OBJECT = "similarVideosByObject";
	
	/* services */
	/** service name declaration */
	public static final String SERVICE_VCA = "vca";
	
	/* solr dynamic field datatypes */
	private static final String SOLR_DATE = "_dt";
	private static final String SOLR_DOUBLE = "_d";
	private static final String SOLR_INTEGER = "_i";
	private static final String SOLR_LONG = "_l";
	private static final String SOLR_STRING = "_s";
	private static final String SOLR_TEXT = "_s";
	
	/* solr fields */
	/** SOLR field declaration */
	protected static final String SOLR_FIELD_CONFIDENCE = service.tut.pori.contentanalysis.Definitions.ELEMENT_CONFIDENCE+SOLR_DOUBLE;
	/** SOLR field declaration */
	protected static final String SOLR_FIELD_CREDITS = service.tut.pori.contentanalysis.Definitions.ELEMENT_CREDITS+SOLR_STRING;
	/** SOLR field declaration */
	protected static final String SOLR_FIELD_DESCRIPTION = service.tut.pori.contentanalysis.Definitions.ELEMENT_DESCRIPTION+SOLR_TEXT;
	/** SOLR field declaration */
	protected static final String SOLR_FIELD_NAME = service.tut.pori.contentanalysis.Definitions.ELEMENT_NAME+SOLR_STRING;
	/** SOLR field declaration */
	protected static final String SOLR_FIELD_SERVICE_ID = service.tut.pori.contentanalysis.Definitions.ELEMENT_SERVICE_ID+SOLR_INTEGER;
	/** SOLR field declaration */
	protected static final String SOLR_FIELD_UPDATED = "updated"+SOLR_DATE;
	/** SOLR field declaration */
	protected static final String SOLR_FIELD_USER_ID = core.tut.pori.users.Definitions.ELEMENT_USER_ID+SOLR_LONG;
	/** SOLR field declaration */
	protected static final String SOLR_FIELD_VISIBILITY = service.tut.pori.contentanalysis.Definitions.ELEMENT_VISIBILITY+SOLR_INTEGER;
	
	/**
	 * 
	 */
	private Definitions() {
		// nothing needed
	}
}
