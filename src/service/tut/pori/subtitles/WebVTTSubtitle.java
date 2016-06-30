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

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import service.tut.pori.contentanalysis.MediaObject;
import service.tut.pori.contentanalysis.MediaObjectList;
import service.tut.pori.contentanalysis.video.Timecode;
import service.tut.pori.contentanalysis.video.TimecodeList;
import service.tut.pori.subtitles.SubtitlesCore.SubtitleFormat;
import core.tut.pori.http.StringResponse;

/**
 * Represents a WebVTT subtitle
 * 
 * <a href="http://dev.w3.org/html5/webvtt/">WebVTT: The Web Video Text Tracks Format</a>
 * 
 * The default formating for the subtitles is {@link service.tut.pori.subtitles.SubtitlesCore.SubtitleFormat#INDIVIDUAL}
 */
public class WebVTTSubtitle implements StringResponse.StringData {
	private static final char ZERO_1 = '0';
	private static final String ZERO_2 = "00";
	private static final String ZERO_3 = "000";
	private static final String HEADER = "WEBVTT\n\nNOTE\nSubtitles automatically generated by VisualLabel\n\n";
	private static final Logger LOGGER = Logger.getLogger(WebVTTSubtitle.class);
	private static final char SEPARATOR_HOUR_MIN_SECONDS = ':';
	private static final char SEPARATOR_SECONDS_MILLISECONDS = '.';
	private static final char SEPARATOR_TIMECODE_VALUE = '\n';
	private static final String SEPARATOR_TIMECODES = " --> ";
	private SubtitleFormat _subtitleFormat = null;
	private MediaObjectList _mediaObjects = null;

	/**
	 * @return the mediaObjects
	 */
	public MediaObjectList getMediaObjects() {
		return _mediaObjects;
	}

	/**
	 * @param mediaObjects the mediaObjects to set
	 */
	public void setMediaObjects(MediaObjectList mediaObjects) {
		_mediaObjects = mediaObjects;
	}

	/**
	 * @return the subtitleFormat
	 */
	public SubtitleFormat getSubtitleFormat() {
		return (_subtitleFormat == null ? SubtitleFormat.INDIVIDUAL : _subtitleFormat);
	}

	/**
	 * @param subtitleFormat if null, the format will default to {@link SubtitlesCore.SubtitleFormat#INDIVIDUAL}
	 */
	public void setSubtitleFormat(SubtitleFormat subtitleFormat) {
		_subtitleFormat = subtitleFormat;
	}

	@Override
	public String toResponseString() throws IllegalArgumentException{
		if(MediaObjectList.isEmpty(_mediaObjects)){
			LOGGER.debug("No content.");
			return HEADER;
		}
		
		SubtitleFormat format = getSubtitleFormat();
		switch(format){
			case INDIVIDUAL:
				return toIndividual();
			case GROUPED:
			default:
				throw new UnsupportedOperationException("Unsupported format : "+format.toFormatString());
		}
	}
	
	/**
	 * This will use the media objects timecode list to produce the subtitle track.
	 * The media object's value is used if present, if not, the name will be used. If both are missing, the media object will be ignored.
	 * 
	 * This will automatically ignore all media objects without a valid timecode list.
	 * 
	 * @return the media object list as individually formatted subtitle track
	 * @see service.tut.pori.contentanalysis.MediaObject#getTimecodes()
	 */
	private String toIndividual(){
		StringBuilder vtt = new StringBuilder(HEADER);
		for(MediaObject vo : _mediaObjects.getMediaObjects()){
			TimecodeList timecodes = vo.getTimecodes();
			if(!TimecodeList.isValid(timecodes)){
				LOGGER.debug("Ignored media object without valid timecode list, media object id: "+vo.getMediaObjectId());
				continue;
			}
			
			String value = vo.getValue();
			if(StringUtils.isBlank(value)){
				value = vo.getName();
				if(StringUtils.isBlank(value)){
					LOGGER.warn("No usable value for media object, id: "+vo.getMediaObjectId());
					continue;
				}
			}
			
			for(Timecode tc : timecodes.getTimecodes()){
				formatTimecode(vtt, tc.getStart());
				vtt.append(SEPARATOR_TIMECODES);
				formatTimecode(vtt, tc.getEnd());
				vtt.append(SEPARATOR_TIMECODE_VALUE);
				vtt.append(value);
				
				vtt.append(SEPARATOR_TIMECODE_VALUE);
				vtt.append(SEPARATOR_TIMECODE_VALUE);
			} // for timecodes
		} // for objects
		
		return vtt.toString();
	}
	
	/**
	 * append the value to the builder, prepending the required amount of zeroes for two digit number
	 * 
	 * @param vtt
	 * @param value ranging from 0 to 99
	 */
	private void append2DigitValue(StringBuilder vtt, long value){
		if(value < 1){
			vtt.append(ZERO_2);
		}else{
			if(value < 10){
				vtt.append(ZERO_1);
			}
			vtt.append(value);
		}
	}
	
	/**
	 * append the value to the builder, prepending the required amount of zeroes for three digit number
	 * 
	 * @param vtt
	 * @param value ranging from 0 to 999
	 */
	private void append3DigitValue(StringBuilder vtt, long value){
		if(value < 1){
			vtt.append(ZERO_3);
		}else{
			if(value < 10){
				vtt.append(ZERO_2);
			}else if(value < 100){
				vtt.append(ZERO_1);
			}
			vtt.append(value);
		}
	}
	
	/**
	 * print the timecode as WebVTT formatted string (00:00:00:000 or HH:MM:SS:MiS) to the given builder
	 * 
	 * @param vtt
	 * @param timecode timecode value in seconds
	 */
	private void formatTimecode(StringBuilder vtt, double timecode){
		long val = (long) (timecode*1000.0); // convert to milliseconds, chop fractions
		val %= 86400000;
		long tmp = val / 3600000; // hours
		append2DigitValue(vtt, tmp);
		vtt.append(SEPARATOR_HOUR_MIN_SECONDS);
		
		val %= 3600000;
		
		tmp = val / 60000; // minutes
		append2DigitValue(vtt, tmp);
		vtt.append(SEPARATOR_HOUR_MIN_SECONDS);
		
		tmp = (val % 60000) / 1000; // seconds
		append2DigitValue(vtt, tmp);
		vtt.append(SEPARATOR_SECONDS_MILLISECONDS);
		
		tmp = val % 1000; // milliseconds
		append3DigitValue(vtt, tmp);
	}

	@Override
	public String getContentType() {
		return Definitions.CONTENT_TYPE_WEBVTT;
	}

	@Override
	public String getEncoding() {
		return core.tut.pori.http.Definitions.ENCODING_UTF8;
	}
}
