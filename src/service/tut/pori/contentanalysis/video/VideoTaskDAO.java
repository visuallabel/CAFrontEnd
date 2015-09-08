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

import java.util.EnumSet;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import service.tut.pori.contentanalysis.AbstractTaskDetails;
import service.tut.pori.contentanalysis.AnalysisBackend;
import service.tut.pori.contentanalysis.AsyncTask.TaskType;
import service.tut.pori.contentanalysis.MediaTaskDAO;
import core.tut.pori.http.parameters.DataGroups;
import core.tut.pori.http.parameters.Limits;
import core.tut.pori.users.UserIdentity;
import core.tut.pori.utils.MediaUrlValidator.MediaType;

/**
 * Class for storing video tasks
 */
public class VideoTaskDAO extends MediaTaskDAO {
	private static final DataGroups BASIC = new DataGroups(DataGroups.DATA_GROUP_BASIC);
	private static final Logger LOGGER = Logger.getLogger(VideoTaskDAO.class);
	private static final EnumSet<MediaType> MEDIA_TYPES = EnumSet.of(MediaType.VIDEO);
	@Autowired
	private VideoDAO _videoDAO = null;
	
	@Override
	public VideoTaskDetails getTask(Integer backendId, DataGroups dataGroups, Limits limits, Long taskId) throws IllegalArgumentException {
		Pair<TaskType, UserIdentity> type = getTaskType(backendId, taskId);
		if(type == null){
			LOGGER.warn("Failed to resolve task type.");
			return null;
		}

		if(backendId == null){
			LOGGER.debug("No backend id given, will not check data groups.");
		}else if(DataGroups.isEmpty(dataGroups)){
			LOGGER.debug("No datagroups given, retrieving default data groups.");
			AnalysisBackend backend = getBackendDAO().getBackend(backendId);
			if(backend == null){
				throw new IllegalArgumentException("Backend, id: "+backendId+" does not exist.");
			}
			dataGroups = backend.getDefaultTaskDataGroups();
		}

		VideoTaskDetails details = new VideoTaskDetails(type.getLeft());
		details.setBackendId(backendId);
		details.setTaskId(taskId);
		details.setUserId(type.getRight());
		getVideos(dataGroups, details, limits);

		getTaskMetadata(details);

		if(DataGroups.hasDataGroup(service.tut.pori.contentanalysis.Definitions.DATA_GROUP_BACKEND_STATUS, dataGroups)){
			getBackendStatusList(details);
		}

		if(details.isEmpty()){
			LOGGER.warn("Task, id: "+taskId+" has no content.");
			return null;
		}else{
			return details;
		}
	}
	
	/**
	 * retrieve and set the videos for the task
	 * 
	 * @param dataGroups
	 * @param details
	 * @param limits
	 */
	private void getVideos(DataGroups dataGroups, VideoTaskDetails details, Limits limits){
		Long taskId = details.getTaskId();
		
		List<String> videoGUIDs = getTaskGUIDs(limits, taskId, GUIDType.MEDIA);
		if(videoGUIDs != null){
			LOGGER.debug("Retrieving video list...");
			TaskType taskType = details.getTaskType();
			switch(taskType){
				case ANALYSIS:	// if task type is analysis, retrieve video list based solely on the given datagroups
					LOGGER.debug("Retrieving all videos for the task based on the given dataGroups, for task of type "+TaskType.ANALYSIS.name()+", id: "+taskId);
					details.setVideoList(_videoDAO.getVideos(dataGroups, videoGUIDs, null, null, null));
					break;
				case BACKEND_FEEDBACK:
				case FEEDBACK:	// ignore result_info datagroup for FEEDBACK, status will be checked later
					LOGGER.debug("Retrieving all videos for the task based on datagroup "+DataGroups.DATA_GROUP_BASIC+", for task of type "+taskType.name()+", id: "+taskId);
					VideoList videos = _videoDAO.getVideos(BASIC, videoGUIDs, null, null, null);	// use basic to get all basic/core details of the video
					if(!VideoList.isEmpty(videos)){
						details.setVideoList(videos);
						setMediaObjects(dataGroups, limits, videos.getVideos(), MEDIA_TYPES, taskId);
					}
					break;
				default:	// should not happen
					throw new UnsupportedOperationException("Unsupported "+TaskType.class.toString());
			}
		}else if((videoGUIDs = getTaskGUIDs(limits, taskId, GUIDType.DELETED_MEDIA)) != null){
			LOGGER.debug("Retrieving deleted video list...");
			for(String guid : videoGUIDs){
				details.addDeletedVideo(new Video(guid));
			}
		}else{
			LOGGER.warn("No content...");
			return;
		}

		if(DataGroups.hasDataGroup(service.tut.pori.contentanalysis.Definitions.DATA_GROUP_STATUS, dataGroups)){
			LOGGER.debug("Retrieving video status information for task, id: "+taskId);
			getMediaStatus(details.getVideoList().getVideos());
			getMediaStatus(details.getDeletedVideoList().getVideos());
		}
	}

	/**
	 * 
	 * @param details
	 * @return id of the generated task
	 * @throws UnsupportedOperationException
	 * @throws IllegalArgumentException
	 */
	public Long insertTask(VideoTaskDetails details) throws UnsupportedOperationException, IllegalArgumentException {
		TaskType type = details.getTaskType();
		switch(type){
			case ANALYSIS:
			case FEEDBACK:
			case BACKEND_FEEDBACK:
				break;
			default:
				throw new UnsupportedOperationException("TaskType not supported: "+type.name());
		}

		Long taskId = insertTask((AbstractTaskDetails) details);
		if(taskId == null){
			throw new IllegalArgumentException("Failed to add new task.");
		}

		insertTaskGUIDs(details);
		insertTaskMediaObjects(details);

		return taskId;
	}
	
	/**
	 * This will also set video statuses, if any are present. Note that even through status elements can appear in any video list,
	 * creating two different lists with identical GUIDs, and conflicting status lists may create undefined behavior.
	 * 
	 * @param details
	 */
	private void insertTaskGUIDs(VideoTaskDetails details){
		Long taskId = details.getTaskId();
		VideoList videos = details.getVideoList();
		if(VideoList.isEmpty(videos)){
			LOGGER.debug("No videos for task, id: "+taskId);
		}else{
			insertTaskGUIDs(videos.getVideos(), taskId, GUIDType.MEDIA);
		}

		DeletedVideoList delVideos = details.getDeletedVideoList();
		if(DeletedVideoList.isEmpty(delVideos)){
			LOGGER.debug("No deleted videos for task, id: "+taskId);
		}else{
			insertTaskGUIDs(delVideos.getVideos(), taskId, GUIDType.DELETED_MEDIA);
		}
	}
	
	/**
	 * Note that media objects provided for any other list than the basic VideoList will be ignored.
	 * 
	 * @param details
	 * @see service.tut.pori.contentanalysis.video.VideoTaskDetails#getVideoList()
	 */
	private void insertTaskMediaObjects(VideoTaskDetails details){
		VideoList videos = details.getVideoList();
		Long taskId = details.getTaskId();
		if(VideoList.isEmpty(videos)){
			LOGGER.debug("No videos for task, id: "+taskId);
		}else{
			for(Video v : videos.getVideos()){
				insertTaskMediaObjects(v.getGUID(), taskId, v.getMediaObjects());
			}	// for videos
		}
	}
}
