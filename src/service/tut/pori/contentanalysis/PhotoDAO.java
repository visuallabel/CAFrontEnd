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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrException;
import org.springframework.beans.factory.annotation.Autowired;

import service.tut.pori.contentanalysis.CAContentCore.ServiceType;
import service.tut.pori.contentanalysis.CAContentCore.Visibility;
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
 * This class can be used to add, remove and modify photo metadata, and also to associate the photos with media objects.
 */
public class PhotoDAO extends SolrDAO{
	private static final String BEAN_ID_SOLR_SERVER = "solrServerPhotos";
	private static final SortOptions DEFAULT_SORT_OPTIONS;
	static{
		DEFAULT_SORT_OPTIONS = new SortOptions();
		DEFAULT_SORT_OPTIONS.addSortOption(new SortOptions.Option(SOLR_FIELD_ID, OrderDirection.ASCENDING, Definitions.ELEMENT_PHOTOLIST));
	}
	private static final String[] FIELDS_DATA_GROUP_DEFAULTS = new String[]{SOLR_FIELD_ID, Definitions.SOLR_FIELD_SERVICE_ID, Definitions.SOLR_FIELD_USER_ID};
	private static final String FIELDS_DATA_GROUP_VISIBILITY = Definitions.SOLR_FIELD_VISIBILITY;
	private static final String[] FIELDS_DATA_GROUP_BASIC = ArrayUtils.addAll(FIELDS_DATA_GROUP_DEFAULTS, FIELDS_DATA_GROUP_VISIBILITY, Definitions.SOLR_FIELD_CREDITS, Definitions.SOLR_FIELD_NAME, Definitions.SOLR_FIELD_DESCRIPTION);
	private static final String[] FIELDS_GET_ACCESS_DETAILS = new String[]{Definitions.SOLR_FIELD_USER_ID, Definitions.SOLR_FIELD_VISIBILITY, SOLR_FIELD_ID};
	private static final String[] FIELDS_SET_OWNERS = new String[]{SOLR_FIELD_ID, Definitions.SOLR_FIELD_USER_ID};
	private static final String[] FIELDS_UPDATE = new String[]{Definitions.SOLR_FIELD_USER_ID, SOLR_FIELD_ID};
	private static final Logger LOGGER = Logger.getLogger(PhotoDAO.class);
	private static final EnumSet<MediaType> MEDIA_TYPES = EnumSet.of(MediaType.PHOTO);
	@Autowired
	private AssociationDAO _associationDAO = null;
	@Autowired
	private PhotoTaskDAO _photoTaskDAO = null;
	@Autowired
	private MediaObjectDAO _mediaObjectDAO = null;
	
	/**
	 * Inserts the objects and sets all media types to {@link core.tut.pori.utils.MediaUrlValidator.MediaType#PHOTO} for all objects with {@link core.tut.pori.utils.MediaUrlValidator.MediaType#UNKNOWN} or null media type-
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
				object.setMediaType(MediaType.PHOTO);	//set all media object MediaType to PHOTO
			}
		}
		return _mediaObjectDAO.insert(objects);
	}
	
	/**
	 * Update the objects and sets all media types to {@link core.tut.pori.utils.MediaUrlValidator.MediaType#PHOTO} for all objects with {@link core.tut.pori.utils.MediaUrlValidator.MediaType#UNKNOWN} or null media type-
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
				object.setMediaType(MediaType.PHOTO);	//set all media object MediaType to PHOTO
			}
		}
		return _mediaObjectDAO.update(objects);
	}
	
	/**
	 * 
	 * @param photo
	 * @return true on success
	 */
	public boolean insert(Photo photo){
		return insert(PhotoList.getPhotoList(Arrays.asList(photo), null));
	}

	/**
	 * 
	 * @param photos
	 * @return true on success
	 */
	public boolean insert(PhotoList photos){
		if(PhotoList.isEmpty(photos)){
			LOGGER.debug("No photos given.");
			return false;
		}
		LOGGER.debug("Adding photos...");
		List<Photo> p = photos.getPhotos();
		Date updated = new Date();
		MediaObjectList combined = new MediaObjectList();
		for(Photo photo : p){
			if(photo.getUpdated() == null){
				photo.setUpdated(updated);
			}
			
			String guid = UUID.randomUUID().toString();
			if(photo.getGUID() != null){
				LOGGER.warn("Replacing GUID for photo with existing GUID: "+photo.getGUID()+", new GUID: "+guid);		
			}
			photo.setGUID(guid);

			MediaObjectList objects = photo.getMediaObjects();
			Visibility visibility = photo.getVisibility();
			UserIdentity userId = photo.getOwnerUserId();
			if(!MediaObjectList.isEmpty(objects)){
				for(Iterator<MediaObject> vIter = objects.getMediaObjects().iterator(); vIter.hasNext();){
					MediaObject object = vIter.next();
					if(!UserIdentity.equals(userId, object.getOwnerUserId())){
						LOGGER.warn("Invalid user identity for media object in photo, GUID: "+guid);
						return false;
					}
					if(object.getVisibility() == null){
						LOGGER.debug("Object missing visibility value, using photo's visibility.");
						object.setVisibility(visibility);
					}
					combined.addMediaObject(object);
				}	// for
			}
		}
		if(!PhotoList.isValid(photos)){	// check validity after ids have been generated
			LOGGER.warn("Tried to add invalid photo list.");
			return false;
		}
		SimpleSolrTemplate template = getSolrTemplate(BEAN_ID_SOLR_SERVER); 
		UpdateResponse response = template.addBeans(p);

		if(response.getStatus() != SolrException.ErrorCode.UNKNOWN.code){
			LOGGER.warn("Failed to add photos.");
			return false;
		}

		if(insert(combined)){
			associate(photos);
		}else{
			LOGGER.warn("Insert failed for combined photo list.");
			return false;
		}
		return true;
	}

	/**
	 * Note: content added through ContentStorage MUST be removed through ContentStorage, removing the metadata directly using this method may cause undefined behavior.
	 * 
	 * @param guids
	 * @see service.tut.pori.contentstorage.ContentStorageCore
	 */
	public void remove(Collection<String> guids){
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
			_photoTaskDAO.remove(guids);
		}
	}
	
	/**
	 * 
	 * @param dataGroups optional filter
	 * @param guids optional filter
	 * @param limits optional filter
	 * @param serviceTypes optional filter
	 * @param userIdFilter optional filter
	 * @return list of photos or null if none
	 */
	public PhotoList getPhotos(DataGroups dataGroups, Collection<String> guids, Limits limits, EnumSet<ServiceType> serviceTypes, long[] userIdFilter){
		return getPhotoList(dataGroups, guids, limits, serviceTypes, userIdFilter);
	}

	/**
	 * 
	 * @param dataGroups
	 * @param guids
	 * @param limits
	 * @param serviceTypes
	 * @param userIdFilter
	 * @return list of photos or null if none was found
	 */
	private PhotoList getPhotoList(DataGroups dataGroups, Collection<String> guids, Limits limits, EnumSet<ServiceType> serviceTypes, long[] userIdFilter){
		SolrQueryBuilder solr = new SolrQueryBuilder(null);
		if(guids != null && !guids.isEmpty()){
			LOGGER.debug("Adding guid filter...");
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

		QueryResponse response = getSolrTemplate(BEAN_ID_SOLR_SERVER).query(solr.toSolrQuery(Definitions.ELEMENT_PHOTOLIST));
		List<Photo> photos = SimpleSolrTemplate.getList(response, Photo.class);
		if(photos == null){
			LOGGER.debug("No photos");
			return null;
		}

		ResultInfo info = null;
		if(DataGroups.hasDataGroup(Definitions.DATA_GROUP_RESULT_INFO, dataGroups)){
			LOGGER.debug("Resolving result info for the requested photos.");
			info = new ResultInfo(limits.getStartItem(Definitions.ELEMENT_PHOTOLIST), limits.getEndItem(Definitions.ELEMENT_PHOTOLIST), response.getResults().getNumFound());
		}

		PhotoList photoList = PhotoList.getPhotoList(photos, info);
		Map<String, Set<String>> guidVoidMap = _associationDAO.getAssociationsForGUIDs(photoList.getGUIDs());
		if(guidVoidMap == null){
			LOGGER.debug("No objects for the photos.");
		}else{
			for(Entry<String, Set<String>> e : guidVoidMap.entrySet()){
				MediaObjectList objects = _mediaObjectDAO.getMediaObjects(dataGroups, limits, MEDIA_TYPES, null, e.getValue(), null); // do NOT give serviceTypes as filter, we are searching photos with specific serviceTypes, not mediaObjects
				if(MediaObjectList.isEmpty(objects)){
					LOGGER.warn("Could not retrieve objects for GUID: "+e.getKey());
				}else{
					photoList.getPhoto(e.getKey()).addMediaObjects(objects);
				}
			}
		}

		if(DataGroups.hasDataGroup(Definitions.DATA_GROUP_STATUS, dataGroups)){
			_photoTaskDAO.getMediaStatus(photoList.getPhotos());
		}

		return photoList;
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
			boolean hasGroup = DataGroups.hasDataGroup(Definitions.DATA_GROUP_VISIBILITY, dataGroups);
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
	public PhotoList search(UserIdentity authenticatedUser, DataGroups dataGroups, Collection<String> guids, Limits limits, MediaObjectList objects, EnumSet<ServiceType> serviceTypes, long[] userIdFilter) throws IllegalArgumentException{
		SolrQueryBuilder solr = new SolrQueryBuilder();
		setDataGroups(dataGroups, solr);
		solr.setLimits(limits);

		if(guids != null && !guids.isEmpty()){
			solr.addCustomFilter(new AndQueryFilter(SOLR_FIELD_ID, guids));
		}

		Map<String, Set<String>> guidVoidMap = null;
		if(!MediaObjectList.isEmpty(objects)){ // if media objects have been given as a search term, do a media object look-up first
			List<String> mediaObjectIds = _mediaObjectDAO.getMediaObjectIds(authenticatedUser, dataGroups, null, null, userIdFilter, objects); // do NOT give serviceTypes as filter, we are searching photos with specific serviceTypes, not mediaObjects
			if(mediaObjectIds == null){
				LOGGER.debug("No objects found.");
				return null;
			}
			
			guidVoidMap = _associationDAO.getAssociationsForMediaObjectIds(mediaObjectIds);
			if(guidVoidMap == null){
				LOGGER.debug("No photos associated with the media object results.");
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
		QueryResponse response = getSolrTemplate(BEAN_ID_SOLR_SERVER).query(solr.toSolrQuery(Definitions.ELEMENT_PHOTOLIST));
		List<Photo> photos = SimpleSolrTemplate.getList(response, Photo.class);
		if(photos == null){
			LOGGER.debug("No photos");
			return null;
		}

		ResultInfo info = null;
		if(DataGroups.hasDataGroup(Definitions.DATA_GROUP_RESULT_INFO, dataGroups)){
			LOGGER.debug("Resolving result info for the requested photos.");
			info = new ResultInfo(limits.getStartItem(Definitions.ELEMENT_PHOTOLIST), limits.getEndItem(Definitions.ELEMENT_PHOTOLIST), response.getResults().getNumFound());
		}

		PhotoList photoList = PhotoList.getPhotoList(photos, info);
		if(guidVoidMap == null){	// resolve media object relations if not resolved already, depending on the data groups given we may not even need the media objects, but let's ignore it for now
			guidVoidMap = _associationDAO.getAssociationsForGUIDs(photoList.getGUIDs());
		}
		
		if(guidVoidMap == null){
			LOGGER.debug("No photo-media object associations...");
		}else{
			LOGGER.debug("Retrieving media objects for the list of photos, if needed...");
			for(Entry<String, Set<String>> e : guidVoidMap.entrySet()){
				MediaObjectList photoObject = _mediaObjectDAO.getMediaObjects(dataGroups, limits, MEDIA_TYPES, null, e.getValue(), null); // do NOT give serviceTypes as filter, we are searching photos with specific serviceTypes, not mediaObjects
				if(MediaObjectList.isEmpty(photoObject)){
					LOGGER.debug("Could not retrieve objects for GUID: "+e.getKey());
				}else{
					Photo photo = photoList.getPhoto(e.getKey());
					if(photo == null){
						LOGGER.warn("Could not find photo, GUID: "+e.getKey());
					}else{
						photo.addMediaObjects(photoObject);
					}
				}
			}
		}

		if(DataGroups.hasDataGroup(Definitions.DATA_GROUP_STATUS, dataGroups)){
			_photoTaskDAO.getMediaStatus(photoList.getPhotos());
		}

		return photoList;
	}

	/**
	 * Sets the owner details (userId) to the given photos, requires that GUID has been set to the photo object
	 * 
	 * @param photos
	 * @return true on success, Note: the failed photos will have userId of null, thus, this method can also be used to check the existence of the given photos.
	 */
	public boolean setOwners(PhotoList photos){
		if(PhotoList.isEmpty(photos)){
			LOGGER.debug("Ignored empty "+PhotoList.class.toString());
			return true;
		}

		List<String> guids = photos.getGUIDs();
		if(guids == null){
			LOGGER.debug("No guids.");
			return false;
		}

		SolrQueryBuilder solr = new SolrQueryBuilder();
		solr.addFields(FIELDS_SET_OWNERS);
		solr.addCustomFilter(new AndQueryFilter(SOLR_FIELD_ID, guids));
		PhotoList found = PhotoList.getPhotoList(getSolrTemplate(BEAN_ID_SOLR_SERVER).queryForList(solr.toSolrQuery(Definitions.ELEMENT_PHOTOLIST), Photo.class),null);
		if(PhotoList.isEmpty(found)){
			LOGGER.debug("No photos found.");
			for(Photo photo : photos.getPhotos()){	// null all user ids
				photo.setOwnerUserId(null);
			}
		}else{
			for(Photo photo : photos.getPhotos()){
				Photo foundPhoto = found.getPhoto(photo.getGUID());
				if(foundPhoto != null){
					photo.setOwnerUserId(foundPhoto.getOwnerUserId());
				}else{
					photo.setOwnerUserId(null);
				}
			} // for
		}
		return true;
	}

	/**
	 * Note: media objects, if present on the photos, will be updated/inserted, old objects will never be removed 
	 * (i.e. this will NOT SET object, but only updates the object details). If you want to remove previously existing
	 * media objects, use {@link service.tut.pori.contentanalysis.MediaObjectDAO}.
	 * 
	 * update the photo details IF the given userId has the permission to do so
	 * @param authenticatedUser
	 * @param onlyNewer if true, only the objects which are newer will be added to the database. If the passed object does not have updated set, it will be ignored if onlyNewer is true.
	 * @param photos
	 * @return true on success
	 */
	private boolean updatePhotos(UserIdentity authenticatedUser, boolean onlyNewer, PhotoList photos){
		if(!UserIdentity.isValid(authenticatedUser)){
			LOGGER.warn("Cannot update for null user.");
			return false;
		}
		if(PhotoList.isEmpty(photos)){
			LOGGER.debug("Ignored empty photo list.");
			return true;
		}
		if(!PhotoList.isValid(photos)){
			LOGGER.warn("Tried update with invalid photo list.");
			return false;
		}

		LOGGER.debug("Updating photos...");
		List<String> guids = photos.getGUIDs();
		SolrQueryBuilder solr = new SolrQueryBuilder();
		solr.addFields(FIELDS_UPDATE);
		if(onlyNewer){
			solr.addField(Definitions.SOLR_FIELD_UPDATED);
		}
		List<Photo> photoList = photos.getPhotos(); // the original list of photos
		List<Photo> checkPhotos = new LinkedList<>(photoList); // make copy to preserve the original, this is used to validate that no bad data is left over
		List<Photo> update = new ArrayList<>(photoList.size()); // contains the photos that will actual be updated
		Date updated = new Date();
		SimpleSolrTemplate t = getSolrTemplate(BEAN_ID_SOLR_SERVER);
		
		solr.clearCustomFilters();
		solr.addCustomFilter(new AndQueryFilter(SOLR_FIELD_ID, guids));
		PhotoList references = PhotoList.getPhotoList(t.queryForList(solr.toSolrQuery(Definitions.ELEMENT_PHOTOLIST), Photo.class), null);
		if(PhotoList.isEmpty(references)){
			LOGGER.warn("Could not find the photos requested for update.");
			return false;
		}
		for(Iterator<Photo> iter = checkPhotos.iterator(); iter.hasNext();){
			Photo check = iter.next();
			String cguid = check.getGUID();
			Photo reference = references.getPhoto(cguid);
			if(reference != null){
				UserIdentity rUserId = reference.getOwnerUserId();
				if(UserIdentity.equals(rUserId, check.getOwnerUserId()) && UserIdentity.equals(rUserId, authenticatedUser)){ // check that the user is not trying to change the userId of the photo, and that he/she actually owns the photo
					iter.remove(); // in any case, remove the photo
					Date cUpdated = check.getUpdated();
					if(onlyNewer){
						if(cUpdated == null){
							LOGGER.warn("Ignored photo without updated timestamp, guid: "+cguid);
							continue;
						}else if(cUpdated.before(reference.getUpdated())){
							LOGGER.debug("Ignored photo with older timestamp, guid: "+cguid);
							continue;
						}
					}else if(cUpdated == null){
						check.setUpdated(updated);	// make sure there is an updated timestamp
					}
					update.add(check);
				}else{ // reference photo was found, but user ids did not match
					LOGGER.warn("Permission denied for user: "+authenticatedUser.getUserId());
					return false;
				}
			} // do not break, in case there are duplicates in the list
		} //for

		if(!checkPhotos.isEmpty()){ // something was left over
			LOGGER.warn("Tried to update non-existing photos, or access was denied for user, id: "+authenticatedUser.getUserId());
			return false;
		}

		if(update.isEmpty()){
			LOGGER.debug("Nothing to update.");
			return true;
		}

		if(!insertOrUpdateMediaObjects(photos)){
			LOGGER.warn("Failed to insert/update media objects.");
			return false;
		}

		if(t.addBeans(update).getStatus() == SolrException.ErrorCode.UNKNOWN.code){
			return true;
		}else{
			LOGGER.warn("Update failed.");
			return false;
		}
	}

	/**
	 * Update the photos only the given photos have newer timestamps than the ones in the database
	 * 
	 * Note: media objects, if present on the photos, will be updated/inserted, old objects will never be removed 
	 * (i.e. this will NOT SET object, but only updates the object details). If you want to remove previously existing
	 * media objects, use {@link service.tut.pori.contentanalysis.MediaObjectDAO}.
	 * 
	 * @param authenticatedUser
	 * @param photos
	 * @return true on success, note that <i>nothing updated</i> also equals to true if no other errors occur
	 */
	public boolean updatePhotosIfNewer(UserIdentity authenticatedUser, PhotoList photos){
		return updatePhotos(authenticatedUser, true, photos);
	}

	/**
	 * Update the photos only the given photos have newer timestamps than the ones in the database
	 * 
	 * Note: media objects, if present on the photos, will be updated/inserted, old objects will never be removed 
	 * (i.e. this will NOT SET object, but only updates the object details). If you want to remove previously existing
	 * media objects, use {@link service.tut.pori.contentanalysis.MediaObjectDAO}.
	 * 
	 * @param authenticatedUser
	 * @param photos
	 * @return true on success
	 */
	public boolean updatePhotos(UserIdentity authenticatedUser, PhotoList photos) {
		return updatePhotos(authenticatedUser, false, photos);
	}

	/**
	 * resolves ids for the given list of objects and performs insert for non-existing objects and update for others.
	 * 
	 * This is a helper method for update methods.
	 * 
	 * @param photos
	 * @return true on success
	 */
	private boolean insertOrUpdateMediaObjects(PhotoList photos){
		MediaObjectList combined = new MediaObjectList();
		List<Photo> photoList = photos.getPhotos();
		for(Photo photo : photoList){
			MediaObjectList objects = photo.getMediaObjects();
			if(!MediaObjectList.isEmpty(objects)){
				combined.addMediaObjects(objects);
			}
		}

		if(MediaObjectList.isEmpty(combined)){
			LOGGER.debug("No objects to update or insert.");
			return true;
		}

		_mediaObjectDAO.resolveObjectIds(combined);	// resolve ids to find out what needs to be updated
		MediaObjectList update = new MediaObjectList();
		MediaObjectList insert = new MediaObjectList();
		for(Photo photo : photoList){
			MediaObjectList objects = photo.getMediaObjects();
			if(!MediaObjectList.isEmpty(objects)){
				UserIdentity userId = photo.getOwnerUserId();
				for(MediaObject object : objects.getMediaObjects()){
					Visibility visibility = photo.getVisibility();
					UserIdentity oUserId = object.getOwnerUserId();
					if(oUserId == null){
						object.setOwnerUserId((oUserId = userId));
					}
					if(!UserIdentity.equals(userId, oUserId)){
						LOGGER.warn("For update photo and media object must have the same user.");
						return false;
					}else if(StringUtils.isBlank(object.getMediaObjectId())){ // a new one
						insert.addMediaObject(object);
					}else{ // old one
						update.addMediaObject(object);
					} // else
					if(object.getVisibility() == null){
						LOGGER.debug("Object visibility missing, using photo's visibility.");
						object.setVisibility(visibility);
					}
				} // for
			} // if
		}

		boolean associate = true;
		if(MediaObjectList.isEmpty(insert)){
			LOGGER.debug("No new objects to insert.");
			associate = false;
		}else if(!insert(insert)){
			LOGGER.warn("Failed to insert new objects.");
			return false;
		}

		if(MediaObjectList.isEmpty(update)){
			associate = (associate ? true : false);
			LOGGER.debug("No objects to update.");
		}else if(!_mediaObjectDAO.update(update)){
			LOGGER.warn("Failed to update objects.");
			return false;
		}

		if(associate){
			LOGGER.debug("Associating...");
			associate(photos);
		}else{
			LOGGER.debug("Skipping association: not needed.");
		}

		return true;
	}
	
	/**
	 * 
	 * @param authenticatedUser
	 * @param guids
	 * @return access details for the photos, or null if none of the photo exist
	 */
	public List<AccessDetails> getAccessDetails(UserIdentity authenticatedUser, Collection<String> guids){
		SolrQueryBuilder solr = new SolrQueryBuilder();
		solr.addFields(FIELDS_GET_ACCESS_DETAILS);
		solr.addCustomFilter(new AndQueryFilter(SOLR_FIELD_ID, guids));

		List<Photo> photos = getSolrTemplate(BEAN_ID_SOLR_SERVER).queryForList(solr.toSolrQuery(Definitions.ELEMENT_PHOTOLIST), Photo.class);
		if(photos == null){
			LOGGER.debug("Guids do not exist.");
			return null;
		}
		List<AccessDetails> details = new ArrayList<>(photos.size());
		for(Photo photo : photos){
			details.add(AccessDetails.getAccessDetails(authenticatedUser, photo));
		}
		return details;
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

		List<Photo> photos = getSolrTemplate(BEAN_ID_SOLR_SERVER).queryForList(solr.toSolrQuery(Definitions.ELEMENT_PHOTOLIST), Photo.class);
		if(photos == null){
			LOGGER.debug("GUID does not exist: "+guid);
			return null;
		}
		return AccessDetails.getAccessDetails(authenticatedUser, photos.iterator().next());
	}

	/**
	 * Note: this will NOT remove the media object, and it will only remove the association between the given GUID and media object id.
	 * Use {@link service.tut.pori.contentanalysis.MediaObjectDAO} if you want to remove the media objects.
	 * 
	 * @param guid if null, the given media object will be de-associated from all GUIDs
	 * @param mediaObjectId if null, all media objects for the given GUID will be de-associated
	 */
	public void deassociate(String guid, String mediaObjectId) {
		_associationDAO.deassociate(guid, mediaObjectId);
	}
	
	/**
	 * create photo-media object associations from the given photo list
	 * 
	 * @param photos
	 */
	public void associate(PhotoList photos){
		_associationDAO.associate(photos.getPhotos());
	}
}
