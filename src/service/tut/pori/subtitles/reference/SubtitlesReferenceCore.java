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
package service.tut.pori.subtitles.reference;

import org.apache.log4j.Logger;

import service.tut.pori.contentanalysis.Definitions;
import service.tut.pori.contentanalysis.MediaObjectList;
import service.tut.pori.contentanalysis.video.reference.VideoXMLObjectCreator;
import service.tut.pori.subtitles.WebVTTSubtitle;
import service.tut.pori.subtitles.SubtitlesCore.FileFormat;
import service.tut.pori.subtitles.SubtitlesCore.SubtitleFormat;
import core.tut.pori.http.StringResponse.StringData;
import core.tut.pori.http.parameters.DataGroups;
import core.tut.pori.http.parameters.Limits;
import core.tut.pori.users.UserIdentity;

/**
 * Subtitle service reference core
 * 
 */
public final class SubtitlesReferenceCore {
	private static final VideoXMLObjectCreator CREATOR = new VideoXMLObjectCreator(null);
	private static final DataGroups DEFAULT_DATAGROUPS = new DataGroups(Definitions.DATA_GROUP_TIMECODES);
	private static final Limits DEFAULT_LIMTS;
	static{
		DEFAULT_LIMTS = new Limits(0, 0);
		DEFAULT_LIMTS.setTypeLimits(0, 9, Definitions.ELEMENT_MEDIA_OBJECTLIST);
	}
	private static final Logger LOGGER = Logger.getLogger(SubtitlesReferenceCore.class);
	
	/**
	 * 
	 */
	private SubtitlesReferenceCore(){
		// nothing needed
	}

	/**
	 * 
	 * @param authenticatedUser value is only logged
	 * @param guid value is ignored
	 * @param fileFormat
	 * @param subtitleFormat
	 * @param userIdFilter value is ignored
	 * @return the formatted data
	 * @throws UnsupportedOperationException on unsupported {@link service.tut.pori.subtitles.SubtitlesCore.FileFormat} or {@link service.tut.pori.subtitles.SubtitlesCore.SubtitleFormat}
	 */
	public static StringData generateSubtitles(UserIdentity authenticatedUser, String guid, FileFormat fileFormat, SubtitleFormat subtitleFormat, long[] userIdFilter) throws UnsupportedOperationException {
		if(subtitleFormat != SubtitleFormat.INDIVIDUAL){
			throw new IllegalArgumentException("Unsupported subtitle format for this validator: "+subtitleFormat.toFormatString());
		}
		
		LOGGER.info((authenticatedUser == null ? "No logged in user." : "Ignoring the logged in user, id: "+authenticatedUser.getUserId()));	// only notify of the logged in status
		MediaObjectList mediaObjects = CREATOR.createMediaObjectList(null, DEFAULT_DATAGROUPS, DEFAULT_LIMTS, null);
		
		StringData data = null;
		switch(fileFormat){
			case WEBVTT:
				WebVTTSubtitle vSub = new WebVTTSubtitle();
				vSub.setSubtitleFormat(subtitleFormat);
				vSub.setMediaObjects(mediaObjects);
				data = vSub;
				break;
			default:
				throw new UnsupportedOperationException("Unsupported "+FileFormat.class.toString()+" : "+fileFormat.name());
		}
		
		return data;
	}
}
