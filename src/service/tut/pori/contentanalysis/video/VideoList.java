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
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.log4j.Logger;

import core.tut.pori.http.ResponseData;
import service.tut.pori.contentanalysis.ResultInfo;

/**
 * 
 * This is a container class for video elements, which can be used directly with Response to provide XML output.
 * 
 * <h3>XML Example</h3>
 * 
 * {@doc.restlet service="[service.tut.pori.contentanalysis.video.reference.Definitions#SERVICE_VCA_REFERENCE_EXAMPLE]" method="[service.tut.pori.contentanalysis.video.Definitions#ELEMENT_VIDEOLIST]" type="GET" query="" body_uri=""}
 *  
 * @see service.tut.pori.contentanalysis.video.Video
 * @see service.tut.pori.contentanalysis.ResultInfo
 */
@XmlRootElement(name=Definitions.ELEMENT_VIDEOLIST)
@XmlAccessorType(value=XmlAccessType.NONE)
public class VideoList extends ResponseData {
	private static final Logger LOGGER = Logger.getLogger(VideoList.class);
	@XmlElement(name = service.tut.pori.contentanalysis.Definitions.ELEMENT_RESULT_INFO)
	private ResultInfo _resultInfo = null;
	@XmlElement(name = Definitions.ELEMENT_VIDEO)
	private List<Video> _videos = null;

	
	/**
	 * 
	 * @param videos
	 * @see #getVideos()
	 */
	public void addVideos(VideoList videos){
		if(VideoList.isEmpty(videos)){
			LOGGER.debug("Ignored empty photo list.");
			return;
		}
		if(_videos == null){
			_videos = new ArrayList<>(videos.getVideos());
		}else{
			_videos.addAll(videos.getVideos());
		}
	}
	
	/**
	 * 
	 * @param guid
	 * @return video with the given GUID or null if not in the list
	 */
	public Video getVideo(String guid){
		if(isEmpty()){
			return null;
		}
		if(guid == null){
			LOGGER.debug("Ignored null guid.");
			return null;
		}
		for(Iterator<Video> iter = _videos.iterator(); iter.hasNext();){
			Video p = iter.next();
			if(guid.equals(p.getGUID())){
				return p;
			}
		}
		return null;
	}
	
	/**
	 * 
	 * @return list of GUIDs in this list or null if the list contains no GUIDs.
	 */
	public List<String> getGUIDs(){
		return getGUIDs(this);
	}
	
	/**
	 * 
	 * @param videoList
	 * @return GUIDs contained in the given video list or null if none. Videos without GUID will be ignored.
	 */
	public static List<String> getGUIDs(VideoList videoList){
		if(isEmpty(videoList)){
			LOGGER.debug("Empty video list.");
			return null;
		}else{
			List<Video> videos = videoList.getVideos();
			List<String> guids = new ArrayList<>(videos.size());
			for(Iterator<Video> iter = videos.iterator(); iter.hasNext();){
				String guid = iter.next().getGUID();
				if(guid == null){
					LOGGER.debug("Skipped video without GUID.");
				}else{
					guids.add(guid);
				}
			}
			return (guids.isEmpty() ? null : guids);
		}
	}
	
	/**
	 * @return the resultInfo
	 * @see #setResultInfo(ResultInfo)
	 */
	public ResultInfo getResultInfo() {
		return _resultInfo;
	}

	/**
	 * @param resultInfo the resultInfo to set
	 * @see #getResultInfo()
	 */
	public void setResultInfo(ResultInfo resultInfo) {
		_resultInfo = resultInfo;
	}

	/**
	 * @return the videos
	 * @see #setVideos(List)
	 */
	public List<Video> getVideos() {
		return _videos;
	}

	/**
	 * @param videos the videos to set
	 * @see #getVideos()
	 */
	public void setVideos(List<Video> videos) {
		_videos = videos;
	}
	
	/**
	 * 
	 * @param videos
	 * @param resultInfo optional resultInfo
	 * @return new video list of null if null or empty videos given
	 */
	public static VideoList getVideoList(Collection<Video> videos, ResultInfo resultInfo){
		if(videos == null || videos.isEmpty()){
			return null;
		}
		
		VideoList videoList = new VideoList();
		videoList.setVideos(new ArrayList<>(videos));
		videoList.setResultInfo(resultInfo);
		return videoList;
	}	
	
	/**
	 * @param list
	 * @return true if the given list is empty or null
	 */
	public static boolean isEmpty(VideoList list){
		if(list == null || list.isEmpty()){
			return true;
		}else{
			return false;
		}
	}

	/**
	 * use the static, only for sub-classing
	 * @return true if this list is empty
	 * @see #isEmpty(VideoList)
	 */
	protected boolean isEmpty(){
		return (_videos == null ? true : _videos.isEmpty());
	}

	/**
	 * 
	 * @param list can be null
	 * @return true if the given list is valid
	 */
	public static boolean isValid(VideoList list){
		return (list == null ? false : list.isValid());
	}

	/**
	 * use the static, only for sub-classing
	 * @return true if this list is valid
	 * @see #isValid(VideoList)
	 */
	protected boolean isValid(){
		if(isEmpty()){
			return false;
		}else{
			for(Video video : _videos){
				if(!Video.isValid(video)){
					return false;
				}
			}
			return true;
		}
	}
	
	/**
	 * use the static, only for sub-classing
	 * @return number of videos in the list
	 * @see #count(VideoList)
	 */
	protected int count(){
		return (_videos == null ? 0 : _videos.size());
	}
	
	/**
	 * 
	 * @param list
	 * @return number of videos in the list or 0 if null or empty list passed
	 */
	public static int count(VideoList list){
		return (list == null ? 0 : list.count());
	}

	/**
	 * 
	 * @param video
	 */
	public void addVideo(Video video) {
		if(_videos == null){
			_videos = new ArrayList<>();
		}
		_videos.add(video);
	}
}
