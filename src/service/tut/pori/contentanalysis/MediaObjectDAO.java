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
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
import service.tut.pori.contentanalysis.MediaObject.ConfirmationStatus;
import service.tut.pori.contentanalysis.MediaObject.MediaObjectType;
import core.tut.pori.dao.SimpleSolrTemplate;
import core.tut.pori.dao.SolrDAO;
import core.tut.pori.dao.SolrQueryBuilder;
import core.tut.pori.dao.SQLSelectBuilder.OrderDirection;
import core.tut.pori.dao.filter.AbstractQueryFilter;
import core.tut.pori.dao.filter.AndQueryFilter;
import core.tut.pori.dao.filter.AndSubQueryFilter;
import core.tut.pori.dao.filter.OrQueryFilter;
import core.tut.pori.dao.filter.OrSubQueryFilter;
import core.tut.pori.dao.filter.RangeQueryFilter;
import core.tut.pori.http.parameters.DataGroups;
import core.tut.pori.http.parameters.Limits;
import core.tut.pori.http.parameters.QueryParameter;
import core.tut.pori.http.parameters.SortOptions;
import core.tut.pori.http.parameters.SortOptions.Option;
import core.tut.pori.users.UserIdentity;
import core.tut.pori.utils.MediaUrlValidator.MediaType;


/**
 * The DAO for storing and retrieving media objects.
 * 
 * This class can also be used to retrieve media object suggestions. Note that if you wish to associate media objects with photos you must use PhotoDAO, not this class.
 *
 */
public class MediaObjectDAO extends SolrDAO{
	/** Default media object type for search operations, when object type is not specified */
	public static final MediaObjectType DEFAULT_MEDIA_OBJECT_TYPE = MediaObjectType.KEYWORD;
	private static final String BEAN_ID_SOLR_SERVER = "solrServerMediaObjects";
	private static final int[] DEFAULT_CONFIRMATION_STATUS_LIST = ConfirmationStatus.toIntArray(EnumSet.of(ConfirmationStatus.CANDIDATE, ConfirmationStatus.USER_CONFIRMED));
	private static final SortOptions DEFAULT_SORT_OPTIONS;
	static{
		DEFAULT_SORT_OPTIONS = new SortOptions();
		DEFAULT_SORT_OPTIONS.addSortOption(new SortOptions.Option(SOLR_FIELD_ID, OrderDirection.ASCENDING, Definitions.ELEMENT_MEDIA_OBJECTLIST));
	}
	private static final Visibility DEFAULT_VISIBILITY = Visibility.PRIVATE;
	private static final String[] FIELDS_DATA_GROUP_DEFAULTS = new String[]{
		Definitions.SOLR_FIELD_BACKEND_ID, Definitions.SOLR_FIELD_CONFIDENCE, SOLR_FIELD_ID, Definitions.SOLR_FIELD_NAME, Definitions.SOLR_FIELD_CREATOR_OBJECT_ID, Definitions.SOLR_FIELD_RANK, Definitions.SOLR_FIELD_UPDATED, Definitions.SOLR_FIELD_VALUE, // members
		Definitions.SOLR_FIELD_USER_ID, // user identity
		Definitions.SOLR_FIELD_SERVICE_ID, // service type
		Definitions.SOLR_FIELD_MEDIA_OBJECT_TYPE, // object type
		Definitions.SOLR_FIELD_MEDIA_TYPE, // media type
		Definitions.SOLR_FIELD_VISUAL_SHAPE_VALUE, // for visual shape
		Definitions.SOLR_FIELD_VISUAL_SHAPE_TYPE,  // for visual shape
		Definitions.SOLR_FIELD_STATUS // confirmation status
		};
	private static final String[] FIELDS_DATA_GROUP_TIMECODES = new String[]{Definitions.SOLR_FIELD_TIMECODES};
	private static final String[] FIELDS_RESOLVE_OBJECT_IDS = new String[]{SOLR_FIELD_ID, Definitions.SOLR_FIELD_BACKEND_ID, Definitions.SOLR_FIELD_USER_ID, Definitions.SOLR_FIELD_CREATOR_OBJECT_ID};
	private static final String[] FIELDS_UPDATE = new String[]{Definitions.SOLR_FIELD_USER_ID, SOLR_FIELD_ID, Definitions.SOLR_FIELD_BACKEND_ID, Definitions.SOLR_FIELD_CREATOR_OBJECT_ID};
	private static final Logger LOGGER = Logger.getLogger(MediaObjectDAO.class);
	@Autowired
	private KeywordsDAO _keywordsDAO = null;
	@Autowired
	private PhotoDAO _photoDAO = null;
	
	/**
	 * @param objects
	 * @return true on success
	 */
	public boolean insert(MediaObjectList objects){
		if(MediaObjectList.isEmpty(objects)){
			LOGGER.debug("Ignored empty object list.");
			return true;
		}
		Date updated = new Date();
		List<MediaObject> v = objects.getMediaObjects();
		for(Iterator<MediaObject> vIter = v.iterator(); vIter.hasNext();){	// make sure all objects has updated timestamps
			MediaObject o = vIter.next();
			if(o.getUpdated() == null){
				o.setUpdated(updated);
			}
			if(o.getMediaObjectId() != null){
				LOGGER.warn("Replacing Media object Id exist for media object with existing id, id: "+o.getMediaObjectId());
			}
			String mediaObjectId = UUID.randomUUID().toString();
			o.setMediaObjectId(mediaObjectId);
			if(o.getObjectId() == null){
				LOGGER.debug("No objectId given, using media object id.");
				o.setObjectId(mediaObjectId);
			}
			if(o.getOwnerUserId() == null){
				LOGGER.debug("Adding media object without owner.");
			}
			if(o.getVisibility() == null){
				LOGGER.debug("No visibility value given, using default: "+DEFAULT_VISIBILITY.name());
				o.setVisibility(DEFAULT_VISIBILITY);
			}
		}
		if(!MediaObjectList.isValid(objects)){	// check validity after ids have been generated
			LOGGER.warn("Tried to add invalid object list.");
			return false;
		}
		SimpleSolrTemplate template = getSolrTemplate(BEAN_ID_SOLR_SERVER); 
		UpdateResponse response = template.addBeans(v);	
		if(response.getStatus() == SolrException.ErrorCode.UNKNOWN.code){
			return true;
		}else{
			LOGGER.warn("Failed to add media objects.");
			return false;
		}
	}
	
	/**
	 * helper methods for resolving object ids (backendId, mediaObjectId, objectId, userId)
	 * 
	 * @param backendIdObjectIdMap
	 * @param mediaObjectIds
	 * @param objects
	 */
	private void resolveObjectIds(Map<Integer, HashSet<String>> backendIdObjectIdMap, Set<String> mediaObjectIds, List<MediaObject> objects){
		boolean noMediaObjectIds = mediaObjectIds.isEmpty();
		boolean noObjectIds = backendIdObjectIdMap.isEmpty();
		
		if(noMediaObjectIds && noObjectIds){
			LOGGER.debug("No media object ids or object ids.");
			return;
		}
		
		SolrQueryBuilder query = new SolrQueryBuilder();
		query.addFields(FIELDS_RESOLVE_OBJECT_IDS);
		if(!noMediaObjectIds){
			query.addCustomFilter(new OrQueryFilter(SOLR_FIELD_ID, mediaObjectIds));
		}
		
		if(!noObjectIds){
			for(Entry<Integer, HashSet<String>> e : backendIdObjectIdMap.entrySet()){
				query.addCustomFilter(new OrSubQueryFilter(new AbstractQueryFilter[]{new AndQueryFilter(Definitions.SOLR_FIELD_BACKEND_ID, e.getKey()), new AndQueryFilter(Definitions.SOLR_FIELD_CREATOR_OBJECT_ID, e.getValue())}));
			}
		}
		
		List<MediaObject> results = getSolrTemplate(BEAN_ID_SOLR_SERVER).queryForList(query.toSolrQuery(Definitions.ELEMENT_MEDIA_OBJECTLIST), MediaObject.class);
		if(results == null){
			LOGGER.debug("No results.");
			return;
		}
		
		for(MediaObject object : objects){
			for(MediaObject result : results){
				if(result.getMediaObjectId().equals(object.getMediaObjectId())){
					UserIdentity oUserId = object.getOwnerUserId();
					UserIdentity rUserId = result.getOwnerUserId();
					if(oUserId != null && !UserIdentity.equals(oUserId, rUserId)){
						LOGGER.warn("Replacing conflicting userId.");
					}
					object.setOwnerUserId(rUserId);
					
					String oObjectId = object.getObjectId();
					String rObjectId = result.getObjectId();
					if(oObjectId != null && !oObjectId.equals(rObjectId )){
						LOGGER.warn("Replacing conflicting objectId.");
					}
					object.setObjectId(rObjectId);
					
					Integer oBackendId = object.getBackendId();
					Integer rBackendId = result.getBackendId();
					if(oBackendId != null && !oBackendId.equals(rBackendId)){
						LOGGER.warn("Replacing conflicting backendId.");
					}
					object.setBackendId(rBackendId);
				}else if(result.getObjectId().equals(object.getObjectId())){
					Integer rBackendId = result.getBackendId();
					Integer oBackendId = object.getBackendId();
					if((rBackendId == null && oBackendId == null) || (rBackendId != null && rBackendId.equals(oBackendId))){
						UserIdentity oUserId = object.getOwnerUserId();
						UserIdentity rUserId = result.getOwnerUserId();
						if(oUserId != null && !UserIdentity.equals(oUserId, rUserId)){
							LOGGER.warn("Replacing conflicting userId.");
						}
						object.setOwnerUserId(rUserId);
						object.setMediaObjectId(result.getMediaObjectId());
					}	// if it is a match
				}	// else ignore non-matching media object, this can be later matched or new object without a match
			} // for results
		} // for objects
	}
	
	/**
	 * Sets all missing ids for the given media objects if ids are found. 
	 * Objects without valid mediaObjectId or backendId+objectId pair are ignored.
	 * 
	 * @param mediaObjects
	 */
	public void resolveObjectIds(MediaObjectList mediaObjects){
		if(MediaObjectList.isEmpty(mediaObjects)){
			LOGGER.debug("Empty object list.");
			return;
		}
	
		HashSet<String> mediaObjectIds = new HashSet<>();
		HashMap<Integer, HashSet<String>> backendIdObjectIdMap = new HashMap<>();
		List<MediaObject> objects = mediaObjects.getMediaObjects();
		for(Iterator<MediaObject> vIter = objects.iterator(); vIter.hasNext();){ // get media object ids, backend ids and object ids
			MediaObject vo = vIter.next();
			String mediaObjectId = vo.getMediaObjectId();
			if(!StringUtils.isBlank(mediaObjectId)){
				mediaObjectIds.add(mediaObjectId);
			}else{
				String objectId = vo.getObjectId();
				if(!StringUtils.isBlank(objectId)){
					Integer backendId = vo.getBackendId();
					HashSet<String> objectIds = backendIdObjectIdMap.get(backendId);
					if(objectIds == null){
						objectIds = new HashSet<>();
						backendIdObjectIdMap.put(backendId, objectIds);
					}
					objectIds.add(objectId);
				}else{
					LOGGER.debug("Ignored media object without objectId and mediaObjectId.");
				}
			}
		}
		resolveObjectIds(backendIdObjectIdMap, mediaObjectIds, objects);
	}
	
	/**
	 * 
	 * @param objects
	 * @return true on success. Note that <i>nothing updated</i> equals to success if no other errors are present.
	 */
	public boolean updateIfNewer(MediaObjectList objects){
		return update(objects, true);
	}
	
	/**
	 * 
	 * @param objects
	 * @return true on success
	 */
	public boolean update(MediaObjectList objects){
		return update(objects, false);
	}

	/**
	 * The objects must have a valid id, either objectId (provided by back-end or client) + backendId (Note: null is a valid backendId if object created by the user) or mediaObjectId (provided by system)
	 * 
	 * @param objects
	 * @param onlyNewer if true, only the objects which are newer will be added to the database. If the passed object does not have updated set, it will be ignored if onlyNewer is true.
	 * @return true on success
	 */
	private boolean update(MediaObjectList objects, boolean onlyNewer){
		if(MediaObjectList.isEmpty(objects)){
			LOGGER.debug("Ignored empty object list.");
			return true;
		}
		if(!MediaObjectList.isValid(objects)){
			LOGGER.warn("Tried update with invalid object list.");
			return false;
		}
		
		List<String> voids = objects.getMediaObjectIds();
		if(voids == null){
			LOGGER.warn("Could not get media object ids.");
			return false;
		}
		
		SolrQueryBuilder solr = new SolrQueryBuilder();
		solr.addFields(FIELDS_UPDATE);
		if(onlyNewer){
			solr.addField(Definitions.SOLR_FIELD_UPDATED);
		}
		SimpleSolrTemplate t = getSolrTemplate(BEAN_ID_SOLR_SERVER);
		List<MediaObject> refList = new ArrayList<>();

		solr.clearCustomFilters();
		solr.addCustomFilter(new AndQueryFilter(SOLR_FIELD_ID, voids));
		refList = t.queryForList(solr.toSolrQuery(Definitions.ELEMENT_MEDIA_OBJECTLIST), MediaObject.class);
		if(refList.isEmpty()){
			LOGGER.warn("Tried to update non-existent objects.");
			return false;
		}
		
		MediaObjectList references = MediaObjectList.getMediaObjectList(refList, null);
		
		List<MediaObject> mediaObjects = objects.getMediaObjects();
		List<MediaObject> update = new ArrayList<>(mediaObjects.size());
		Date updated = new Date();
		for(MediaObject object : mediaObjects){ // check for that userId, backendId or objectId is not being changed
			String mediaObjectId = object.getMediaObjectId();
			MediaObject reference = references.getMediaObject(mediaObjectId);
			if(reference == null){
				LOGGER.warn("Tried to update non-existent object, id: "+mediaObjectId);
				return false;
			}else if(!reference.getObjectId().equals(object.getObjectId())){
				LOGGER.warn("Object creator id mismatch for object, id: "+mediaObjectId);
				return false;
			}else if(!UserIdentity.equals(reference.getOwnerUserId(), object.getOwnerUserId())){
				LOGGER.warn("Object user identity mismatch for object, id: "+mediaObjectId);
				return false;
			}else{
				Integer rBackendId = reference.getBackendId();
				Integer oBackendId = object.getBackendId();
				if(rBackendId != null){
					if(!rBackendId.equals(oBackendId)){
						LOGGER.warn("Object backend id mismatch for object, id: "+mediaObjectId);
						return false;
					}
				}else if(oBackendId != null){	// both are not null
					LOGGER.warn("Object backend id mismatch for object, id: "+mediaObjectId);
					return false;
				}
			}
			Date oUpdated = object.getUpdated();
			if(onlyNewer){
				if(oUpdated == null){
					LOGGER.warn("Ignored object without updated timestamp, id: "+mediaObjectId);
					continue;
				}else if(oUpdated.before(reference.getUpdated())){
					LOGGER.debug("Ignored object with older timestamp, id: "+mediaObjectId);
					continue;
				}
			}else if(oUpdated == null){
				object.setUpdated(updated);	// make sure there is a timestamp
			}
			if(object.getVisibility() == null){
				LOGGER.debug("Object is missing visibility, using default: "+DEFAULT_VISIBILITY.name());
				object.setVisibility(DEFAULT_VISIBILITY);
			}
			update.add(object);
		}
		
		if(t.addBeans(update).getStatus() == SolrException.ErrorCode.UNKNOWN.code){
			return true;
		}else{
			LOGGER.warn("Failed to update media objects.");
			return false;
		}
	}

	
	
	/**
	 * This will also automatically remove any associations between the given media objects and their photos.
	 * 
	 * @param mediaobjectIds
	 * @return true on success
	 */
	public boolean remove(Collection<String> mediaobjectIds){
		if(mediaobjectIds == null || mediaobjectIds.isEmpty()){
			LOGGER.debug("Ignored empty media object id list.");
			return true;
		}
		
		for(String mediaObjectId : mediaobjectIds){ // remove associations
			_photoDAO.deassociate(null, mediaObjectId);
		}
		SimpleSolrTemplate template = getSolrTemplate(BEAN_ID_SOLR_SERVER);
		if(template.deleteById(mediaobjectIds).getStatus() == SolrException.ErrorCode.UNKNOWN.code){
			return true;
		}else{
			LOGGER.warn("Failed to remove media objects.");
			return false;
		}
	}
	
	/**
	 * 
	 * @param dataGroups
	 * @param limits
	 * @param mediaTypes target media types for the retrieval
	 * @param serviceTypes
	 * @param mediaObjectIds
	 * @param userIdFilter
	 * @return list of media objects or null if none was found
	 * @throws IllegalArgumentException on bad query terms
	 */
	public MediaObjectList getMediaObjects(DataGroups dataGroups, Limits limits, EnumSet<MediaType> mediaTypes, EnumSet<ServiceType> serviceTypes, Collection<String> mediaObjectIds, long[] userIdFilter) throws IllegalArgumentException {
		if(mediaTypes == null || mediaTypes.isEmpty()){
			throw new IllegalArgumentException("Invalid MediaType "+MediaType.class.toString()+" given.");
		}
		return getMediaObjectList(dataGroups, limits, mediaTypes, serviceTypes, mediaObjectIds, userIdFilter);
	}

	/**
	 * helper method for retrieving the media object list
	 * 
	 * @param dataGroups
	 * @param limits
	 * @param mediaTypes target media types, not null, nor empty
	 * @param serviceTypes
	 * @param mediaObjectIds
	 * @param userIdFilter
	 * @return list of media objects or null if none was found
	 */
	private MediaObjectList getMediaObjectList(DataGroups dataGroups, Limits limits, EnumSet<MediaType> mediaTypes, EnumSet<ServiceType> serviceTypes, Collection<String> mediaObjectIds, long[] userIdFilter) {
		SolrQueryBuilder solr = new SolrQueryBuilder();
		if(!processDataGroups(dataGroups, solr)){
			LOGGER.debug("Process data groups did not result viable combination for search, returning null...");
			return null;
		}
		
		solr.addCustomFilter(new AndQueryFilter(Definitions.SOLR_FIELD_MEDIA_TYPE, MediaType.toInt(mediaTypes)));
		
		if(!ServiceType.isEmpty(serviceTypes)){
			solr.addCustomFilter(new AndQueryFilter(Definitions.SOLR_FIELD_SERVICE_ID, ServiceType.toIdArray(serviceTypes)));
		}
		
		if(mediaObjectIds != null && !mediaObjectIds.isEmpty()){
			solr.addCustomFilter(new AndQueryFilter(SOLR_FIELD_ID, mediaObjectIds));
		}
		
		if(userIdFilter != null){
			solr.addCustomFilter(new AndQueryFilter(Definitions.SOLR_FIELD_USER_ID, userIdFilter));
		}
		
		solr.setSortOptions(DEFAULT_SORT_OPTIONS);
		solr.setLimits(limits);
		
		QueryResponse response = getSolrTemplate(BEAN_ID_SOLR_SERVER).query(solr.toSolrQuery(Definitions.ELEMENT_MEDIA_OBJECTLIST));
		List<MediaObject> mediaObjects = SimpleSolrTemplate.getList(response, MediaObject.class);
		if(mediaObjects == null){
			LOGGER.debug("No results.");
			return null;
		}
		
		ResultInfo info = null;
		if(DataGroups.hasDataGroup(Definitions.DATA_GROUP_RESULT_INFO, dataGroups)){
			LOGGER.debug("Resolving result info for the requested objects.");
			info = new ResultInfo(limits.getStartItem(Definitions.ELEMENT_MEDIA_OBJECTLIST), limits.getEndItem(Definitions.ELEMENT_MEDIA_OBJECTLIST), response.getResults().getNumFound());
		}
		
		MediaObjectList voList = MediaObjectList.getMediaObjectList(mediaObjects, info);
		_keywordsDAO.assignFriendlyKeywords(voList);
		return voList;
	}
	
	/**
	 * 
	 * @param authenticatedUser
	 * @param dataGroups
	 * @param limits
	 * @param serviceTypes
	 * @param userIdFilter
	 * @param mediaObjectTerms list of terms to use for search. Note: if the objects have mediaObjectIds set, these will be directly used as filter
	 * @return list of ids or null if none
	 */
	public List<String> getMediaObjectIds(UserIdentity authenticatedUser, DataGroups dataGroups, Limits limits, EnumSet<ServiceType> serviceTypes, long[] userIdFilter, MediaObjectList mediaObjectTerms) {
		SolrQueryBuilder solr = new SolrQueryBuilder();
		if(!processDataGroups(dataGroups, solr)){
			LOGGER.debug("Process data groups did not result viable combination for search, returning null...");
			return null;
		}
		
		if(!UserIdentity.isValid(authenticatedUser)){
			LOGGER.debug("Invalid authenticated user, limiting search to public content.");
			solr.addCustomFilter(new AndQueryFilter(Definitions.SOLR_FIELD_VISIBILITY, Visibility.PUBLIC.toInt()));
		}else{
			solr.addCustomFilter(new AndSubQueryFilter(new AbstractQueryFilter[]{new OrQueryFilter(Definitions.SOLR_FIELD_USER_ID, authenticatedUser.getUserId()), new OrQueryFilter(Definitions.SOLR_FIELD_VISIBILITY, Visibility.PUBLIC.toInt())}));
		}
		
		if(!ServiceType.isEmpty(serviceTypes)){
			solr.addCustomFilter(new AndQueryFilter(Definitions.SOLR_FIELD_SERVICE_ID, ServiceType.toIdArray(serviceTypes)));
		}
		
		if(!ArrayUtils.isEmpty(userIdFilter)){
			solr.addCustomFilter(new AndQueryFilter(Definitions.SOLR_FIELD_USER_ID, userIdFilter));
		}
		
		processMediaObjects(solr, mediaObjectTerms);
		
		solr.setLimits(limits);
		solr.setSortOptions(DEFAULT_SORT_OPTIONS);
		solr.addField(SOLR_FIELD_ID);
		
		return SimpleSolrTemplate.getObjects(getSolrTemplate(BEAN_ID_SOLR_SERVER).query(solr.toSolrQuery(Definitions.ELEMENT_MEDIA_OBJECTLIST)), SOLR_FIELD_ID, String.class);
	}
	
	/**
	 * 
	 * @param solr
	 * @param mediaObjectTerms
	 * @throws IllegalArgumentException on too high term count
	 */
	private void processMediaObjects(SolrQueryBuilder solr, MediaObjectList mediaObjectTerms) throws IllegalArgumentException {
		if(MediaObjectList.isEmpty(mediaObjectTerms)){
			LOGGER.debug("No media objects.");
			return;
		}
		LOGGER.debug("Creating media object filters...");
		
		List<MediaObject> terms = mediaObjectTerms.getMediaObjects();
		List<String> mediaObjectIds = new ArrayList<>();
		List<AbstractQueryFilter> filters = new ArrayList<>();
		for(MediaObject term : terms){
			String mediaObjectId = term.getMediaObjectId();
			if(!StringUtils.isBlank(mediaObjectId)){
				LOGGER.debug("media object id was given, using as filter, id: "+mediaObjectId);
				mediaObjectIds.add(mediaObjectId);
			}else{
				String value = term.getValue();
				if(StringUtils.isBlank(value)){
					LOGGER.debug("No value for visual object, attempting to use name.");
					value = term.getName();
					if(StringUtils.isBlank(value)){
						LOGGER.warn("Ignording media object search term without value or name.");
						continue;
					}
				}
				
				MediaObjectType type = term.getMediaObjectType();
				if(type == null){
					LOGGER.warn("No media object type, using default : "+DEFAULT_MEDIA_OBJECT_TYPE.name());
					type = DEFAULT_MEDIA_OBJECT_TYPE;
				}
				OrSubQueryFilter subFilter = new OrSubQueryFilter();
				
				subFilter.addFilter(new AndQueryFilter(Definitions.SOLR_FIELD_VALUE_SEARCH, value));
				subFilter.addFilter(new AndQueryFilter(Definitions.SOLR_FIELD_MEDIA_OBJECT_TYPE, type.toInt()));
				
				Visibility visibility = term.getVisibility();
				if(visibility != null){
					subFilter.addFilter(new AndQueryFilter(Definitions.SOLR_FIELD_VISIBILITY, visibility.toInt()));
				}
				
				ServiceType serviceType = term.getServiceType();
				if(serviceType != null){
					subFilter.addFilter(new AndQueryFilter(Definitions.SOLR_FIELD_SERVICE_ID, serviceType.getServiceId()));
				}
				
				ConfirmationStatus status = term.getConfirmationStatus();
				if(status != null){
					subFilter.addFilter(new AndQueryFilter(Definitions.SOLR_FIELD_STATUS, status.toInt()));
				}
				
				Integer temp = term.getRank();
				if(temp != null){
					subFilter.addFilter(new RangeQueryFilter(Definitions.SOLR_FIELD_RANK, temp, null));
				}
				
				temp = term.getBackendId();
				if(temp != null){
					subFilter.addFilter(new RangeQueryFilter(Definitions.SOLR_FIELD_BACKEND_ID, temp, null));
				}
				
				Double confidence = term.getConfidence();
				if(confidence != null){
					subFilter.addFilter(new RangeQueryFilter(Definitions.SOLR_FIELD_CONFIDENCE, confidence, null));
				}
				
				filters.add(subFilter);
			}
		} // for
		
		if(!filters.isEmpty()){
			solr.addCustomFilter(new AndSubQueryFilter(filters.toArray(new AbstractQueryFilter[filters.size()])));
		}

		if(!mediaObjectIds.isEmpty()){
			solr.addCustomFilter(new AndQueryFilter(SOLR_FIELD_ID, mediaObjectIds));
		}
	}
	
	/**
	 * 
	 * @param solr
	 * @param sortOptions 
	 */
	private void setOrderBy(SolrQueryBuilder solr, SortOptions sortOptions){
		if(sortOptions == null || !sortOptions.hasValues()){
			solr.setSortOptions(DEFAULT_SORT_OPTIONS);
			return;
		}
		
		Set<Option> so = sortOptions.getSortOptions(Definitions.ELEMENT_MEDIA_OBJECTLIST);
		if(so == null){
			return;
		}
		
		for(Iterator<Option> iter = so.iterator();iter.hasNext();){
			Option o = iter.next();
			String elementName = o.getElementName();
			if(Definitions.ELEMENT_CONFIDENCE.equals(elementName)){
				solr.addSortOption(new Option(Definitions.SOLR_FIELD_CONFIDENCE, o.getOrderDirection(), Definitions.ELEMENT_MEDIA_OBJECTLIST));
			}else if(Definitions.ELEMENT_RANK.equals(elementName)){
				solr.addSortOption(new Option(Definitions.SOLR_FIELD_RANK, o.getOrderDirection(), Definitions.ELEMENT_MEDIA_OBJECTLIST));
			}else if(Definitions.ELEMENT_VALUE.equals(elementName)){
				solr.addSortOption(new Option(Definitions.SOLR_FIELD_VALUE, o.getOrderDirection(), Definitions.ELEMENT_MEDIA_OBJECTLIST));
			}else{
				LOGGER.debug("Ignored unknown sort element: "+elementName);
			}
		}
	}

	/**
	 * Note that because of solr limitations, the media object count cannot exceed MAX_FILTER_COUNT.
	 * 
	 * @param authenticatedUser
	 * @param dataGroups
	 * @param limits
	 * @param mediaTypes list of target media types for the search
	 * @param serviceTypes
	 * @param sortOptions
	 * @param userIdFilter
	 * @param mediaObjectTerms list of terms to use for search. Note: if the objects have mediaObjectIds set, these will be directly used as filter
	 * @return list of media objects or null if none was found
	 * @throws IllegalArgumentException on bad values
	 */
	public MediaObjectList search(UserIdentity authenticatedUser, DataGroups dataGroups, Limits limits, EnumSet<MediaType> mediaTypes, EnumSet<ServiceType> serviceTypes, SortOptions sortOptions, long[] userIdFilter, MediaObjectList mediaObjectTerms) throws IllegalArgumentException {
		if(mediaTypes == null || mediaTypes.isEmpty()){
			throw new IllegalArgumentException("Invalid "+MediaType.class.toString()+" given.");
		}
		
		SolrQueryBuilder solr = new SolrQueryBuilder();
		
		if(!processDataGroups(dataGroups, solr)){
			LOGGER.debug("Process data groups did not result viable combination for search, returning null...");
			return null;
		}
		
		solr.addCustomFilter(new AndQueryFilter(Definitions.SOLR_FIELD_MEDIA_TYPE, MediaType.toInt(mediaTypes)));
		
		processMediaObjects(solr, mediaObjectTerms); // throws IllegalArgumentException if term count exceeds maximum
		
		if(!UserIdentity.isValid(authenticatedUser)){
			LOGGER.debug("Invalid authenticated user, limiting search to public content.");
			solr.addCustomFilter(new AndQueryFilter(Definitions.SOLR_FIELD_VISIBILITY, Visibility.PUBLIC.toInt()));
		}else{
			solr.addCustomFilter(new AndSubQueryFilter(new AbstractQueryFilter[]{new OrQueryFilter(Definitions.SOLR_FIELD_USER_ID, authenticatedUser.getUserId()), new OrQueryFilter(Definitions.SOLR_FIELD_VISIBILITY, Visibility.PUBLIC.toInt())}));
		}
		
		if(!ServiceType.isEmpty(serviceTypes)){
			solr.addCustomFilter(new AndQueryFilter(Definitions.SOLR_FIELD_SERVICE_ID, ServiceType.toIdArray(serviceTypes)));
		}
		
		if(!ArrayUtils.isEmpty(userIdFilter)){
			solr.addCustomFilter(new AndQueryFilter(Definitions.SOLR_FIELD_USER_ID, userIdFilter));
		}
		
		solr.setLimits(limits);
		setOrderBy(solr, sortOptions);
		
		QueryResponse response = getSolrTemplate(BEAN_ID_SOLR_SERVER).query(solr.toSolrQuery(Definitions.ELEMENT_MEDIA_OBJECTLIST));
		List<? extends MediaObject> mediaObjects = SimpleSolrTemplate.getList(response, MediaObject.class);
		if(mediaObjects == null){
			LOGGER.debug("No results.");
			return null;
		}
		
		ResultInfo info = null;
		if(DataGroups.hasDataGroup(Definitions.DATA_GROUP_RESULT_INFO, dataGroups)){
			LOGGER.debug("Resolving result info for the requested objects.");
			info = new ResultInfo(limits.getStartItem(Definitions.ELEMENT_MEDIA_OBJECTLIST), limits.getEndItem(Definitions.ELEMENT_MEDIA_OBJECTLIST), response.getResults().getNumFound());
		}
		
		MediaObjectList voList = MediaObjectList.getMediaObjectList(mediaObjects, info);
		_keywordsDAO.assignFriendlyKeywords(voList);
		return voList;
	}
	
	/**
	 * helper method for processing the data groups and setting filters for the query builder.
	 * 
	 * @param dataGroups For applicable values see {@link service.tut.pori.contentanalysis.MediaObject.MediaObjectType#fromDataGroups(DataGroups)}.
	 * @param solr
	 * @return true if the data groups can returned results.
	 */
	private boolean processDataGroups(DataGroups dataGroups, SolrQueryBuilder solr){
		if(!DataGroups.hasDataGroup(DataGroups.DATA_GROUP_ALL, dataGroups, Definitions.ELEMENT_MEDIA_OBJECTLIST)){ // do not care about filters if ALL is present
			boolean onlyBasic = DataGroups.hasDataGroup(DataGroups.DATA_GROUP_BASIC, dataGroups, Definitions.ELEMENT_MEDIA_OBJECTLIST);
			
			EnumSet<ConfirmationStatus> statuses = ConfirmationStatus.fromDataGroups(dataGroups);
			if(statuses == null){
				LOGGER.debug("No "+ConfirmationStatus.class.toString()+" filter, using defaults.");
				solr.addCustomFilter(new AndQueryFilter(Definitions.SOLR_FIELD_STATUS, DEFAULT_CONFIRMATION_STATUS_LIST));
			}else{
				onlyBasic = false;
				solr.addCustomFilter(new AndQueryFilter(Definitions.SOLR_FIELD_STATUS, ConfirmationStatus.toIntArray(statuses)));
			}
		
			EnumSet<MediaObjectType> types = MediaObjectType.fromDataGroups(dataGroups);
			if(types != null){
				onlyBasic = false;
				solr.addCustomFilter(new AndQueryFilter(Definitions.SOLR_FIELD_MEDIA_OBJECT_TYPE, MediaObjectType.toIntArray(types)));
			}
			
			if(DataGroups.hasDataGroup(Definitions.DATA_GROUP_TIMECODES, dataGroups, Definitions.ELEMENT_MEDIA_OBJECTLIST)){
				onlyBasic = false;
				solr.addFields(FIELDS_DATA_GROUP_TIMECODES);
			}
			
			if(onlyBasic){
				LOGGER.debug("Data group "+DataGroups.DATA_GROUP_BASIC+" given without other media object related data groups, no results can be found.");
				return false;
			}
			
			solr.addFields(FIELDS_DATA_GROUP_DEFAULTS);
		}
		return true;
	}
	
	/**
	 * Suggestion/Autocomplete from Solr
	 * @param authenticatedUser 
	 * @param dataGroups filters based on MediaObjectType. For applicable values see {@link service.tut.pori.contentanalysis.MediaObject.MediaObjectType#fromDataGroups(DataGroups)}.
	 * @param limits 
	 * @param query the term to be searched for.
	 * @return response
	 */
	public QueryResponse getSuggestions(UserIdentity authenticatedUser, DataGroups dataGroups, Limits limits, String query) {
		SolrQueryBuilder solr = new SolrQueryBuilder();
		
		//set visibility restrictions
		if(!UserIdentity.isValid(authenticatedUser)){
			LOGGER.debug("Invalid authenticated user, limiting search to public content.");
			solr.addCustomFilter(new AndQueryFilter(Definitions.SOLR_FIELD_VISIBILITY, Visibility.PUBLIC.toInt()));
		}else{
			solr.addCustomFilter(new AndSubQueryFilter(new AbstractQueryFilter[]{new OrQueryFilter(Definitions.SOLR_FIELD_USER_ID, authenticatedUser.getUserId()), new OrQueryFilter(Definitions.SOLR_FIELD_VISIBILITY, Visibility.PUBLIC.toInt())}));
		}
		
		processDataGroups(dataGroups, solr); //sets the query filter for MediaObjectType
		solr.setQueryParameter(new QueryParameter(query));
		solr.setLimits(limits);
		
		return getSolrTemplate(BEAN_ID_SOLR_SERVER).query(SolrQueryBuilder.setRequestHandler(solr.toSolrQuery(), SolrQueryBuilder.RequestHandlerType.SUGGEST));
	}
}
