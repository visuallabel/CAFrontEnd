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
package service.tut.pori.subtitles;

/**
 * Subtitles service definitions
 * 
 */
public final class Definitions {

	/* services */
	/** service name declaration */
	public static final String SERVICE_SUBS = "subs";
	
	/* methods */
	/** service method declaration */
	public static final String METHOD_GENERATE_SUBTITLES = "getSubtitles";
	
	/* parameters */
	/** service method parameter declaration */
	public static final String PARAMETER_FILE_FORMAT = "file_format";
	/** service method parameter declaration */
	public static final String PARAMETER_SUBTITLE_FORMAT = "subtitle_format";
	
	/* common */
	/** content type as defined by <a href="http://dev.w3.org/html5/webvtt/#webvtt-file-structure">WebVTT: The Web Video Text Tracks Format</a> */
	public static final String CONTENT_TYPE_WEBVTT = "text/vtt";
	
	/* file formats */
	/** file format string for WebVTT <a href="http://dev.w3.org/html5/webvtt">WebVTT: The Web Video Text Tracks Format</a> */
	public static final String FILE_FORMAT_WEBVTT = "WEBVTT";
	
	/* subtitle formats */
	/** subtitle format for grouped media objects */
	public static final String SUBTITLE_FORMAT_GROUPED = "GROUPED";
	/** subtitle format for individual media objects */
	public static final String SUBTITLE_FORMAT_INDIVIDUAL = "INDIVIDUAL";
	
	/**
	 * 
	 */
	private Definitions(){
		// nothing needed
	}
}
