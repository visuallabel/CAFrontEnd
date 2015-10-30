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
package service.tut.pori.contentanalysis.video;

import java.util.ArrayList;
import java.util.Collection;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import service.tut.pori.contentanalysis.ResultInfo;

/**
 * A special video list used to list deleted video.
 * 
 * Deleted Video List element is generally found in the Feedback Task. It is used to notify back-ends that the video or a list of videos is no longer available.
 * 
 * Note that the information in the Video object is provided only for reference, and the details provided are not guaranteed to valid. Moreover, the Video object is not guaranteed to be valid. The list is generally only meant to provide the UIDs of the deleted videos; other information may or may not be present.
 *
 * <h3>XML Example</h3>
 * 
 * {@doc.restlet service="[service.tut.pori.contentanalysis.video.reference.Definitions#SERVICE_VCA_REFERENCE_EXAMPLE]" method="[service.tut.pori.contentanalysis.video.Definitions#ELEMENT_DELETED_VIDEOLIST]" type="GET" query="" body_uri=""}
 *  
 */
@XmlRootElement(name=Definitions.ELEMENT_DELETED_VIDEOLIST)
@XmlAccessorType(value=XmlAccessType.NONE)
public class DeletedVideoList extends VideoList {
	private static final Logger LOGGER = Logger.getLogger(DeletedVideoList.class);
	
	@Override
	protected boolean isValid() {
		if(isEmpty()){
			return false;
		}
		LOGGER.debug("Using "+DeletedVideoList.class.toString()+" for validation of a video list.");
		for(Video p : getVideos()){
			if(StringUtils.isBlank(p.getGUID())){
				return false;
			}
		}
		return true;
	}

	/**
	 * 
	 * @param videos
	 * @param resultInfo
	 * @return new video list or null if the given collection of videos was null or empty
	 */
	public static DeletedVideoList getVideoList(Collection<Video> videos, ResultInfo resultInfo) {
		if(videos == null || videos.isEmpty()){
			LOGGER.debug("Empty video list.");
			return null;
		}
		
		DeletedVideoList videoList = new DeletedVideoList();
		videoList.setResultInfo(resultInfo);
		videoList.setVideos(new ArrayList<>(videos));
		return videoList;
	}
}
