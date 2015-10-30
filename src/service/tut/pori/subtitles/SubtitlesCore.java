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
package service.tut.pori.subtitles;

import java.util.Arrays;

import org.apache.log4j.Logger;

import service.tut.pori.contentanalysis.video.VideoDAO;
import service.tut.pori.contentanalysis.video.VideoList;
import core.tut.pori.context.ServiceInitializer;
import core.tut.pori.http.StringResponse.StringData;
import core.tut.pori.http.parameters.DataGroups;
import core.tut.pori.users.UserIdentity;


/**
 * Subtitle service core methods.
 * 
 */
public final class SubtitlesCore {
	private static final DataGroups DATA_GROUP_TIMECODES = new DataGroups(service.tut.pori.contentanalysis.Definitions.DATA_GROUP_TIMECODES);
	private static final Logger LOGGER = Logger.getLogger(SubtitlesCore.class);
	
	/**
	 * Subtitle file format
	 * 
	 */
	public enum FileFormat {
		/**  
		 * File format for WebVTT
		 * 
		 * <a href="http://dev.w3.org/html5/webvtt">WebVTT: The Web Video Text Tracks Format</a> 
		 */
		WEBVTT;
		
		/**
		 * 
		 * @param value
		 * @return the value converted to file format
		 * @throws IllegalArgumentException on bad value
		 */
		public static FileFormat fromFormatString(String value) throws IllegalArgumentException {
			if(value != null){
				switch(value.toUpperCase()){
					case Definitions.FILE_FORMAT_WEBVTT:
						return WEBVTT;
					default:
						break;
				}
			}
			throw new IllegalArgumentException("Bad value : "+value);
		}
		
		/**
		 * 
		 * @return this format as a string
		 */
		public String toFormatString(){
			switch(this){
				case WEBVTT:
					return Definitions.FILE_FORMAT_WEBVTT;
				default:
					throw new UnsupportedOperationException("Unhandeled "+FileFormat.class.toString()+" : "+name());
			}
		}
	} // enum FileFormat
	
	/**
	 * Formatting option for media objects in a subtitle
	 * 
	 */
	public enum SubtitleFormat {
		/** 
		 * media objects will appear grouped by timecodes, combined in a single line 
		 * 
		 * An example in <a href="http://dev.w3.org/html5/webvtt">WEBVTT</a> format:
		 * <pre>
		 * 00:44.000 --&gt; 01:19.000
		 * Keyword1, Keyword2
		 *
		 * 01:24.000 --&gt; 05:00.000
		 * Keyword1, Keyword3
		 * </pre>
		 */
		GROUPED,
		/** 
		 * media objects will appear in individual lines, with one timecode sequence per media object 
		 * 
		 * An example in <a href="http://dev.w3.org/html5/webvtt">WEBVTT</a> format:
		 * <pre>
		 * 00:44.000 --&gt; 01:19.000
		 * Keyword1
		 *
		 * 00:44.000 --&gt; 01:19.000
		 * Keyword2
		 *
		 * 01:24.000 --&gt; 05:00.000
		 * Keyword1
		 * 
		 * 01:24.000 --&gt; 05:00.000
		 * Keyword3
		 * </pre>
		 */
		INDIVIDUAL;
		
		/**
		 * 
		 * @param value
		 * @return value converted to SubtitleFormat
		 * @throws IllegalArgumentException on bad value
		 */
		public static SubtitleFormat fromFormatString(String value) throws IllegalArgumentException {
			if(value != null){
				switch(value.toUpperCase()){
					case Definitions.SUBTITLE_FORMAT_GROUPED:
						return GROUPED;
					case Definitions.SUBTITLE_FORMAT_INDIVIDUAL:
						return INDIVIDUAL;
					default:
						break;
				}
			}
			throw new IllegalArgumentException("Bad value: "+value);
		}
		
		/**
		 * 
		 * @return the formatting option as a string
		 */
		public String toFormatString() {
			switch(this){
				case GROUPED:
					return Definitions.SUBTITLE_FORMAT_GROUPED;
				case INDIVIDUAL:
					return Definitions.SUBTITLE_FORMAT_INDIVIDUAL;
				default:
					throw new UnsupportedOperationException("Unhandeled "+SubtitleFormat.class.toString()+" : "+name());
			}
		}
	} // enum SubtitleFormat
	
	/**
	 * 
	 */
	private SubtitlesCore(){
		// nothing needed
	}

	/**
	 * 
	 * @param authenticatedUser
	 * @param guid video GUID
	 * @param fileFormat
	 * @param subtitleFormat
	 * @param userIdFilter optional user id filter
	 * @return the formatted subtitle track or null on failure
	 * @throws IllegalArgumentException on bad values
	 */
	public static StringData generateSubtitles(UserIdentity authenticatedUser, String guid, FileFormat fileFormat, SubtitleFormat subtitleFormat, long[] userIdFilter) throws IllegalArgumentException {
		StringData data = null;
		switch(fileFormat){
			case WEBVTT:
				if(subtitleFormat != SubtitleFormat.INDIVIDUAL){
					throw new IllegalArgumentException("Unsupported subtitle format : "+subtitleFormat.toFormatString()+" for file format : "+fileFormat.toFormatString());
				}
				VideoList videos = ServiceInitializer.getDAOHandler().getSolrDAO(VideoDAO.class).search(authenticatedUser, DATA_GROUP_TIMECODES, Arrays.asList(guid), null, null, null, userIdFilter);
				if(VideoList.isEmpty(videos)){
					LOGGER.warn("Video not found or permission denied, GUID: "+guid);
				}else{
					WebVTTSubtitle vsub = new WebVTTSubtitle();
					vsub.setSubtitleFormat(subtitleFormat);
					vsub.setMediaObjects(videos.getVideos().get(0).getMediaObjects());
					data = vsub;
				}
				break;
			default:
				throw new UnsupportedOperationException("Unhandeled "+FileFormat.class.toString()+" : "+fileFormat.name());
		}
		
		return data;
	}
}
