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

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrException;
import org.springframework.beans.factory.annotation.Autowired;

import service.tut.pori.contentanalysis.AccessDetails;
import service.tut.pori.contentanalysis.AssociationDAO;
import service.tut.pori.contentanalysis.CAContentCore.ServiceType;
import service.tut.pori.contentanalysis.CAContentCore.Visibility;
import service.tut.pori.contentanalysis.ResultInfo;
import service.tut.pori.contentanalysis.MediaObject;
import service.tut.pori.contentanalysis.MediaObjectDAO;
import service.tut.pori.contentanalysis.MediaObjectList;
import core.tut.pori.dao.SQLSelectBuilder.OrderDirection;
import core.tut.pori.dao.SimpleSolrTemplate;
import core.tut.pori.dao.SolrDAO;
import core.tut.pori.dao.SolrQueryBuilder;
import core.tut.pori.dao.filter.AbstractQueryFilter;
import core.tut.pori.dao.filter.AndQueryFilter;
import core.tut.pori.dao.filter.AndSubQueryFilter;
import core.tut.pori.dao.filter.OrQueryFilter;
import core.tut.pori.http.parameters.DataGroups;
import core.tut.pori.http.parameters.Limits;
import core.tut.pori.http.parameters.SortOptions;
import core.tut.pori.users.UserIdentity;
import core.tut.pori.utils.MediaUrlValidator.MediaType;

/**
 * The DAO for storing and retrieving video objects.
 */
public class VideoDAO extends SolrDAO {
	private static final String BEAN_ID_SOLR_SERVER = "solrServerVideos";
	private static final SortOptions DEFAULT_SORT_OPTIONS;
	static{
		DEFAULT_SORT_OPTIONS = new SortOptions();
		DEFAULT_SORT_OPTIONS.addSortOption(new SortOptions.Option(SOLR_FIELD_ID, OrderDirection.ASCENDING, Definitions.ELEMENT_VIDEOLIST));
	}
	private static final String[] FIELDS_DATA_GROUP_DEFAULTS = new String[]{SOLR_FIELD_ID, Definitions.SOLR_FIELD_SERVICE_ID, Definitions.SOLR_FIELD_USER_ID};
	private static final String FIELDS_DATA_GROUP_VISIBILITY = Definitions.SOLR_FIELD_VISIBILITY;
	private static final String[] FIELDS_DATA_GROUP_BASIC = ArrayUtils.addAll(FIELDS_DATA_GROUP_DEFAULTS, FIELDS_DATA_GROUP_VISIBILITY, Definitions.SOLR_FIELD_CREDITS, Definitions.SOLR_FIELD_NAME, Definitions.SOLR_FIELD_DESCRIPTION);
	private static final String[] FIELDS_GET_ACCESS_DETAILS = new String[]{Definitions.SOLR_FIELD_USER_ID, Definitions.SOLR_FIELD_VISIBILITY, SOLR_FIELD_ID};
	private static final String[] FIELDS_SET_OWNERS = new String[]{SOLR_FIELD_ID, Definitions.SOLR_FIELD_USER_ID};
	private static final Logger LOGGER = Logger.getLogger(VideoDAO.class);
	private static final EnumSet<MediaType> MEDIA_TYPES = EnumSet.of(MediaType.VIDEO);
	@Autowired
	private AssociationDAO _associationDAO = null;
	@Autowired
	private VideoTaskDAO _videoTaskDAO = null;
	@Autowired
	private MediaObjectDAO _mediaObjectDAO = null;
	
	/**
	 * Inserts the objects and sets all media types to {@link core.tut.pori.utils.MediaUrlValidator.MediaType#VIDEO} for objects with {@link core.tut.pori.utils.MediaUrlValidator.MediaType#UNKNOWN} or null media type.
	 * @param objects
	 * @return true on success
	 * @see service.tut.pori.contentanalysis.MediaObjectDAO#insert(MediaObjectList)
	 */
	public boolean insert(MediaObjectList objects){
		if(MediaObjectList.isEmpty(objects)){
			LOGGER.debug("Empty list ignored.");
			return true;
		}
		for(MediaObject object : objects.getMediaObjects()){
			MediaType mediaType = object.getMediaType();
			if(mediaType == null || MediaType.UNKNOWN.equals(mediaType)){
				object.setMediaType(MediaType.VIDEO);	//set all media object MediaType to VIDEO
			}
		}
		return _mediaObjectDAO.insert(objects);
	}
	
	/**
	 * Update the objects and sets all media types to {@link core.tut.pori.utils.MediaUrlValidator.MediaType#VIDEO} for objects with {@link core.tut.pori.utils.MediaUrlValidator.MediaType#UNKNOWN} or null media type.
	 * @param objects
	 * @return true on success
	 * @see service.tut.pori.contentanalysis.MediaObjectDAO#update(MediaObjectList)
	 */
	public boolean update(MediaObjectList objects){
		if(MediaObjectList.isEmpty(objects)){
			LOGGER.debug("Empty list ignored.");
			return true;
		}
		for(MediaObject object : objects.getMediaObjects()){
			MediaType mediaType = object.getMediaType();
			if(mediaType == null || MediaType.UNKNOWN.equals(mediaType)){
				object.setMediaType(MediaType.VIDEO);	//set all media object MediaType to VIDEO
			}
		}
		return _mediaObjectDAO.update(objects);
	}
	
	/**
	 * 
	 * @param video
	 * @return true on success
	 */
	public boolean insert(Video video){
		VideoList vl = new VideoList();
		vl.setVideos(Arrays.asList(video));
		return insert(vl);
	}

	/**
	 * 
	 * @param videos
	 * @return true on success
	 */
	public boolean insert(VideoList videos){
		if(VideoList.isEmpty(videos)){
			LOGGER.debug("No videos given.");
			return false;
		}
		LOGGER.debug("Adding videos...");
		List<Video> v = videos.getVideos();
		Date updated = new Date();
		MediaObjectList combined = new MediaObjectList();
		for(Video video : v){
			if(video.getUpdated() == null){
				video.setUpdated(updated);
			}
			
			String guid = UUID.randomUUID().toString();
			if(video.getGUID() != null){
				LOGGER.warn("Replacing GUID for video with existing GUID: "+video.getGUID()+", new GUID: "+guid);		
			}
			video.setGUID(guid);

			MediaObjectList objects = video.getMediaObjects();
			Visibility visibility = video.getVisibility();
			UserIdentity userId = video.getOwnerUserId();
			if(!MediaObjectList.isEmpty(objects)){
				for(Iterator<MediaObject> vIter = objects.getMediaObjects().iterator(); vIter.hasNext();){
					MediaObject object = vIter.next();
					if(!UserIdentity.equals(userId, object.getOwnerUserId())){
						LOGGER.warn("Invalid user identity for media object in video, GUID: "+guid);
						return false;
					}
					MediaType mediaType = object.getMediaType();
					if(!MediaType.VIDEO.equals(mediaType)){
						LOGGER.debug("Replacing unsupported/incompatible media type: "+mediaType);
						object.setMediaType(MediaType.VIDEO);
					}
					if(object.getVisibility() == null){
						LOGGER.debug("Object missing visibility value, using video's visibility.");
						object.setVisibility(visibility);
					}
					combined.addMediaObject(object);
				}	// for
			}
		}
		if(!VideoList.isValid(videos)){	// check validity after ids have been generated
			LOGGER.warn("Tried to add invalid video list.");
			return false;
		}
		SimpleSolrTemplate template = getSolrTemplate(BEAN_ID_SOLR_SERVER); 
		UpdateResponse response = template.addBeans(v);

		if(response.getStatus() != SolrException.ErrorCode.UNKNOWN.code){
			LOGGER.warn("Failed to add videos.");
			return false;
		}

		if(insert(combined)){
			associate(videos);
		}else{
			LOGGER.warn("Insert failed for combined video list.");
			return false;
		}
		return true;
	}
	
	/**
	 * 
	 * @param authenticatedUser
	 * @param guid
	 * @return access details for the photo, or null if the photo does not exist
	 */
	public AccessDetails getAccessDetails(UserIdentity authenticatedUser, String guid) {
		SolrQueryBuilder solr = new SolrQueryBuilder();
		solr.addFields(FIELDS_GET_ACCESS_DETAILS);
		solr.addCustomFilter(new AndQueryFilter(SOLR_FIELD_ID, guid));

		List<Video> videos = getSolrTemplate(BEAN_ID_SOLR_SERVER).queryForList(solr.toSolrQuery(Definitions.ELEMENT_VIDEOLIST), Video.class);
		if(videos == null){
			LOGGER.debug("GUID does not exist: "+guid);
			return null;
		}
		return AccessDetails.getAccessDetails(authenticatedUser, videos.iterator().next());
	}
	
	/**
	 * 
	 * @param dataGroups optional filter
	 * @param guids optional filter
	 * @param limits optional filter
	 * @param serviceTypes optional filter
	 * @param userIdFilter optional filter
	 * @return list of videos or null if none
	 */
	public VideoList getVideos(DataGroups dataGroups, Collection<String> guids, Limits limits, EnumSet<ServiceType> serviceTypes, long[] userIdFilter){
		return getVideoList(dataGroups, guids, limits, serviceTypes, userIdFilter);
	}
	
	/**
	 * 
	 * @param dataGroups
	 * @param guids
	 * @param limits
	 * @param serviceTypes
	 * @param userIdFilter
	 * @return list of videos or null if none was found
	 */
	private VideoList getVideoList(DataGroups dataGroups, Collection<String> guids, Limits limits, EnumSet<ServiceType> serviceTypes, long[] userIdFilter){
		SolrQueryBuilder solr = new SolrQueryBuilder(null);
		if(guids != null && !guids.isEmpty()){
			LOGGER.debug("Adding GUID filter...");
			solr.addCustomFilter(new AndQueryFilter(SOLR_FIELD_ID, guids));
		}
		if(!ServiceType.isEmpty(serviceTypes)){
			LOGGER.debug("Adding service type filter...");
			solr.addCustomFilter(new AndQueryFilter(Definitions.SOLR_FIELD_SERVICE_ID, ServiceType.toIdArray(serviceTypes)));
		}

		if(!ArrayUtils.isEmpty(userIdFilter)){
			LOGGER.debug("Adding user id filter...");
			solr.addCustomFilter(new AndQueryFilter(Definitions.SOLR_FIELD_USER_ID, userIdFilter));
		}

		solr.setLimits(limits);
		solr.setSortOptions(DEFAULT_SORT_OPTIONS);
		setDataGroups(dataGroups, solr);

		QueryResponse response = getSolrTemplate(BEAN_ID_SOLR_SERVER).query(solr.toSolrQuery(Definitions.ELEMENT_VIDEOLIST));
		List<Video> videos = SimpleSolrTemplate.getList(response, Video.class);
		if(videos == null){
			LOGGER.debug("No videos");
			return null;
		}

		ResultInfo info = null;
		if(DataGroups.hasDataGroup(service.tut.pori.contentanalysis.Definitions.DATA_GROUP_RESULT_INFO, dataGroups)){
			LOGGER.debug("Resolving result info for the requested videos.");
			info = new ResultInfo(limits.getStartItem(Definitions.ELEMENT_VIDEOLIST), limits.getEndItem(Definitions.ELEMENT_VIDEOLIST), response.getResults().getNumFound());
		}

		VideoList videoList = VideoList.getVideoList(videos, info);
		Map<String, Set<String>> guidVoidMap = _associationDAO.getAssociationsForGUIDs(videoList.getGUIDs());
		if(guidVoidMap == null){
			LOGGER.debug("No objects for the videos.");
		}else{
			for(Entry<String, Set<String>> e : guidVoidMap.entrySet()){
				MediaObjectList objects = _mediaObjectDAO.getMediaObjects(dataGroups, limits, MEDIA_TYPES, null, e.getValue(), null); // do NOT give serviceTypes as filter, we are searching videos with specific serviceTypes, not mediaObjects
				if(MediaObjectList.isEmpty(objects)){
					LOGGER.warn("Could not retrieve objects for guid: "+e.getKey());
				}else{
					videoList.getVideo(e.getKey()).addMediaObjects(objects);
				}
			}
		}

		if(DataGroups.hasDataGroup(service.tut.pori.contentanalysis.Definitions.DATA_GROUP_STATUS, dataGroups)){
			_videoTaskDAO.getMediaStatus(videoList.getVideos());
		}

		return videoList;
	}
	
	/**
	 * 
	 * @param dataGroups
	 * @param solr
	 */
	private void setDataGroups(DataGroups dataGroups, SolrQueryBuilder solr){
		if(DataGroups.hasDataGroup(DataGroups.DATA_GROUP_ALL, dataGroups)){
			LOGGER.debug("Data group "+DataGroups.DATA_GROUP_ALL+" found, will not set field list.");
		}else if(DataGroups.hasDataGroup(DataGroups.DATA_GROUP_BASIC, dataGroups)){
			solr.addFields(FIELDS_DATA_GROUP_BASIC);
		}else{
			boolean hasGroup = DataGroups.hasDataGroup(service.tut.pori.contentanalysis.Definitions.DATA_GROUP_VISIBILITY, dataGroups);
			if(hasGroup){
				solr.addField(FIELDS_DATA_GROUP_VISIBILITY);
			}

			if(DataGroups.hasDataGroup(DataGroups.DATA_GROUP_DEFAULTS, dataGroups) || DataGroups.isEmpty(dataGroups)){	// if defaults are explicitly given or there are not datagroups
				solr.addFields(FIELDS_DATA_GROUP_DEFAULTS);
			}else if(!hasGroup){
				LOGGER.debug("No valid data groups, using "+DataGroups.DATA_GROUP_DEFAULTS);
				solr.addFields(FIELDS_DATA_GROUP_DEFAULTS);
			}
		}
	}

	/**
	 * 
	 * @param authenticatedUser optional filter
	 * @param dataGroups optional filter
	 * @param guids optional filter
	 * @param limits optional filter
	 * @param objects optional filter
	 * @param serviceTypes optional filter
	 * @param userIdFilter optional filter
	 * @return the results, or null if none.
	 * @throws IllegalArgumentException on bad search terms
	 */
	public VideoList search(UserIdentity authenticatedUser, DataGroups dataGroups, Collection<String> guids, Limits limits, MediaObjectList objects, EnumSet<ServiceType> serviceTypes, long[] userIdFilter) {
		SolrQueryBuilder solr = new SolrQueryBuilder();
		setDataGroups(dataGroups, solr);
		solr.setLimits(limits);

		if(guids != null && !guids.isEmpty()){
			solr.addCustomFilter(new AndQueryFilter(SOLR_FIELD_ID, guids));
		}

		Map<String, Set<String>> guidVoidMap = null;
		if(!MediaObjectList.isEmpty(objects)){ // if media objects have been given as a search term, do a media object look-up first
			List<String> mediaObjectIds = _mediaObjectDAO.getMediaObjectIds(authenticatedUser, dataGroups, null, null, userIdFilter, objects); // do NOT give serviceTypes as filter, we are searching videos with specific serviceTypes, not mediaObjects
			if(mediaObjectIds == null){
				LOGGER.debug("No objects found.");
				return null;
			}
			
			guidVoidMap = _associationDAO.getAssociationsForMediaObjectIds(mediaObjectIds);
			if(guidVoidMap == null){
				LOGGER.debug("No videos associated with the media object results.");
				return null;
			}
			solr.addCustomFilter(new AndQueryFilter(SOLR_FIELD_ID, guidVoidMap.keySet()));
		}

		if(!ServiceType.isEmpty(serviceTypes)){
			solr.addCustomFilter(new AndQueryFilter(Definitions.SOLR_FIELD_SERVICE_ID, ServiceType.toIdArray(serviceTypes)));
		}

		if(!ArrayUtils.isEmpty(userIdFilter)){
			solr.addCustomFilter(new AndQueryFilter(Definitions.SOLR_FIELD_USER_ID, userIdFilter));
		}

		if(!UserIdentity.isValid(authenticatedUser)){
			LOGGER.debug("Invalid authenticated user, limiting search to public content.");
			solr.addCustomFilter(new AndQueryFilter(Definitions.SOLR_FIELD_VISIBILITY, Visibility.PUBLIC.toInt()));
		}else{
			solr.addCustomFilter(new AndSubQueryFilter(new AbstractQueryFilter[]{new OrQueryFilter(Definitions.SOLR_FIELD_USER_ID, authenticatedUser.getUserId()), new OrQueryFilter(Definitions.SOLR_FIELD_VISIBILITY, Visibility.PUBLIC.toInt())}));
		}

		solr.setSortOptions(DEFAULT_SORT_OPTIONS);
		QueryResponse response = getSolrTemplate(BEAN_ID_SOLR_SERVER).query(solr.toSolrQuery(Definitions.ELEMENT_VIDEOLIST));
		List<Video> videos = SimpleSolrTemplate.getList(response, Video.class);
		if(videos == null){
			LOGGER.debug("No videos");
			return null;
		}

		ResultInfo info = null;
		if(DataGroups.hasDataGroup(service.tut.pori.contentanalysis.Definitions.DATA_GROUP_RESULT_INFO, dataGroups)){
			LOGGER.debug("Resolving result info for the requested videos.");
			info = new ResultInfo(limits.getStartItem(Definitions.ELEMENT_VIDEOLIST), limits.getEndItem(Definitions.ELEMENT_VIDEOLIST), response.getResults().getNumFound());
		}

		VideoList videoList = VideoList.getVideoList(videos, info);
		if(guidVoidMap == null){	// resolve media object relations if not resolved already, depending on the data groups given we may not even need the media objects, but let's ignore it for now
			guidVoidMap = _associationDAO.getAssociationsForGUIDs(videoList.getGUIDs());
		}
		
		if(guidVoidMap == null){
			LOGGER.debug("No video-media object associations...");
		}else{
			LOGGER.debug("Retrieving media objects for the list of videos, if needed...");
			for(Entry<String, Set<String>> e : guidVoidMap.entrySet()){
				MediaObjectList videoObject = _mediaObjectDAO.getMediaObjects(dataGroups, limits, MEDIA_TYPES, null, e.getValue(), null); // do NOT give serviceTypes as filter, we are searching videos with specific serviceTypes, not mediaObjects
				if(MediaObjectList.isEmpty(videoObject)){
					LOGGER.debug("Could not retrieve objects for GUID: "+e.getKey());
				}else{
					Video video = videoList.getVideo(e.getKey());
					if(video == null){
						LOGGER.warn("Could not find video, GUID: "+e.getKey());
					}else{
						video.addMediaObjects(videoObject);
					}
				}
			}
		}

		if(DataGroups.hasDataGroup(service.tut.pori.contentanalysis.Definitions.DATA_GROUP_STATUS, dataGroups)){
			_videoTaskDAO.getMediaStatus(videoList.getVideos());
		}

		return videoList;
	}

	/**
	 * Sets the owner details (userId) to the given videos, requires that GUID has been set to the video object
	 * 
	 * @param videos
	 * @return true on success, Note: the failed videos will have userId of null, thus, this method can also be used to check the existence of the given videos.
	 */
	public boolean setOwners(VideoList videos) {
		if(VideoList.isEmpty(videos)){
			LOGGER.debug("Ignored empty "+VideoList.class.toString());
			return true;
		}

		List<String> guids = videos.getGUIDs();
		if(guids == null){
			LOGGER.debug("No GUIDs.");
			return false;
		}

		SolrQueryBuilder solr = new SolrQueryBuilder();
		solr.addFields(FIELDS_SET_OWNERS);
		solr.addCustomFilter(new AndQueryFilter(SOLR_FIELD_ID, guids));
		VideoList found = VideoList.getVideoList(getSolrTemplate(BEAN_ID_SOLR_SERVER).queryForList(solr.toSolrQuery(Definitions.ELEMENT_VIDEOLIST), Video.class),null);
		if(VideoList.isEmpty(found)){
			LOGGER.debug("No videos found.");
			for(Video video : videos.getVideos()){	// null all user ids
				video.setOwnerUserId(null);
			}
		}else{
			for(Video video : videos.getVideos()){
				Video foundVideo = found.getVideo(video.getGUID());
				if(foundVideo != null){
					video.setOwnerUserId(foundVideo.getOwnerUserId());
				}else{
					video.setOwnerUserId(null);
				}
			} // for
		}
		return true;
	}
	
	/**
	 * create video-media object associations from the given video list
	 * 
	 * @param videos
	 */
	public void associate(VideoList videos){
		_associationDAO.associate(videos.getVideos());
	}

	/**
	 * Note: content added through ContentStorage MUST be removed through ContentStorage, removing the metadata directly using this method may cause undefined behavior.
	 * 
	 * @param guids
	 * @see service.tut.pori.contentstorage.ContentStorageCore
	 */
	public void remove(Collection<String> guids) {
		if(guids == null || guids.isEmpty()){
			LOGGER.debug("Ignored empty GUIDs list.");
			return;
		}
		SimpleSolrTemplate template = getSolrTemplate(BEAN_ID_SOLR_SERVER);
		if(template.deleteById(guids).getStatus() != SolrException.ErrorCode.UNKNOWN.code){
			LOGGER.warn("Failed to delete by GUID.");
		}else{
			Map<String, Set<String>> guidVoidMap = _associationDAO.getAssociationsForGUIDs(guids);
			if(guidVoidMap == null){
				LOGGER.debug("No media objects for the GUID.");
			}else{
				for(Entry<String, Set<String>> e : guidVoidMap.entrySet()){
					if(!_mediaObjectDAO.remove(e.getValue())){ // we do not need to de-associate, this will automatically cleanup the association table (by media object dao)
						LOGGER.warn("Failed to remove media objects for GUID: "+e.getKey());
					}
				}
			}
			_videoTaskDAO.remove(guids);
		}
	}
}
