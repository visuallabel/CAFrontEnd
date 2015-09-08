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

import java.util.Date;
import java.util.EnumSet;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.beans.Field;

import service.tut.pori.contentanalysis.CAContentCore.ServiceType;
import service.tut.pori.contentanalysis.CAContentCore.Visibility;
import service.tut.pori.contentanalysis.video.TimecodeList;
import core.tut.pori.dao.SolrDAO;
import core.tut.pori.http.parameters.DataGroups;
import core.tut.pori.users.UserIdentity;
import core.tut.pori.utils.MediaUrlValidator.MediaType;

/**
 * A class that defines a single media object. All media objects are by default of type {@link core.tut.pori.utils.MediaUrlValidator.MediaType#UNKNOWN}}.
 * 
 * Note: when using this class, never pass partial objects to database methods, missing information 
 * (e.g. value == null) may be assumed to be marked as "deleted", and will be removed from the database.
 * 
 * media objects are objects which were identified from the content given for analysis (e.g. from photos).
 * 
 * Back-ends may only edit objects which have been created by them (checked by objectId, {@link #getObjectId()} and backendId, {@link #getBackendId()}). Back-ends may not edit objects which have been confirmed by the user.
 * 
 * If a back-end wants to modify an object created by another back-end, it must generate and submit a new ({@link service.tut.pori.contentanalysis.MediaObject.ConfirmationStatus#CANDIDATE}) media object.
 * 
 * <h2>Optional Elements</h2>
 * 
 * <ul>
 *  <li>{@value service.tut.pori.contentanalysis.Definitions#ELEMENT_BACKEND_ID}, missing if the object is not generated by a back-end.</li>
 *  <li>{@value service.tut.pori.contentanalysis.Definitions#ELEMENT_VALUE}</li>
 *  <li>{@value service.tut.pori.contentanalysis.Definitions#ELEMENT_NAME}</li>
 *  <li>{@value service.tut.pori.contentanalysis.Definitions#ELEMENT_CONFIDENCE}, value of < 1 or null means that the confidence is unknown.</li>
 *  <li>{@value service.tut.pori.contentanalysis.Definitions#ELEMENT_OBJECT_ID}, may not be present on objects created by the system.</li>
 *  <li>{@value service.tut.pori.contentanalysis.Definitions#ELEMENT_MEDIA_TYPE}, if omitted it will default to {@link core.tut.pori.utils.MediaUrlValidator.MediaType#UNKNOWN} OR if object is part of a container object, such as {@link service.tut.pori.contentanalysis.Media} the container's type will be used (see {@link service.tut.pori.contentanalysis.Media#getMediaType()}).</li>
 *  <li>{@value service.tut.pori.contentanalysis.Definitions#ELEMENT_MEDIA_OBJECT_ID}, can be omitted (and is ignored) for new objects. Automatically generated by the system.</li>
 *  <li>{@value service.tut.pori.contentanalysis.Definitions#ELEMENT_RANK}, if missing, the value should be assumed to be 0 (neutral).</li>
 *  <li>{@value service.tut.pori.contentanalysis.Definitions#ELEMENT_RESULT_INFO}</li>
 *  <li>{@value service.tut.pori.contentanalysis.Definitions#ELEMENT_SERVICE_ID}, not present for objects created by user or a back-end.</li>
 *  <li>{@value service.tut.pori.contentanalysis.video.Definitions#ELEMENT_TIMECODELIST}. If timecode list is missing or is empty, the object is assumed to describe the entire content.</li>
 *  <li>{@value service.tut.pori.contentanalysis.Definitions#ELEMENT_VISUAL_SHAPE}</li>
 *  <li>{@value service.tut.pori.contentanalysis.Definitions#ELEMENT_VISIBILITY}, if missing should be assumed to be {@link service.tut.pori.contentanalysis.CAContentCore.Visibility#PRIVATE}.</li>
 * </ul>
 * 
 * <h3>XML Example</h3>
 * 
 * {@doc.restlet service="[service.tut.pori.contentanalysis.reference.Definitions#SERVICE_CA_REFERENCE_EXAMPLE]" method="[service.tut.pori.contentanalysis.Definitions#ELEMENT_MEDIA_OBJECT]" type="GET" query="" body_uri=""}
 *
 * @see service.tut.pori.contentanalysis.VisualShape
 */
@XmlRootElement(name=Definitions.ELEMENT_MEDIA_OBJECT)
@XmlAccessorType(XmlAccessType.NONE)
public class MediaObject implements VisualShape.VisualShapeSolrCapable {
	private static final String CONFIRMATIONSTATUS_BACKEND_REMOVED = "BACKEND_REMOVED";
	private static final String CONFIRMATIONSTATUS_CANDIDATE = "CANDIDATE";
	private static final String CONFIRMATIONSTATUS_NO_FRIENDLY_KEYWORD = "NO_FRIENDLY_KEYWORD";
	private static final String CONFIRMATIONSTATUS_USER_CONFIRMED = "USER_CONFIRMED";
	private static final String CONFIRMATIONSTATUS_USER_REJECTED = "USER_REJECTED";
	private static final String MEDIAOBJECTTYPE_FACE = "FACE";
	private static final String MEDIAOBJECTTYPE_KEYWORD = "KEYWORD";
	private static final String MEDIAOBJECTTYPE_METADATA = "METADATA";
	private static final String MEDIAOBJECTTYPE_OBJECT = "OBJECT";
	private static final Logger LOGGER = Logger.getLogger(MediaObject.class);
	@Field(Definitions.SOLR_FIELD_BACKEND_ID)
	@XmlElement(name = Definitions.ELEMENT_BACKEND_ID)
	private Integer _backendId = null;
	@Field(Definitions.SOLR_FIELD_CONFIDENCE)
	@XmlElement(name = Definitions.ELEMENT_CONFIDENCE)
	private Double _confidence = null;
	/** Unique, internally generated id */
	@Field(SolrDAO.SOLR_FIELD_ID)
	@XmlElement(name = Definitions.ELEMENT_MEDIA_OBJECT_ID)
	private String _mediaObjectId = null;
	@XmlElement(name = Definitions.ELEMENT_MEDIA_TYPE)
	private MediaType _mediaType = MediaType.UNKNOWN;
	@Field(Definitions.SOLR_FIELD_NAME)
	@XmlElement(name = Definitions.ELEMENT_NAME)
	private String _name = null;
	/** Externally generated id. Unique when used in combination with back-end id, or if back-end id is missing, with user id. */
	@Field(Definitions.SOLR_FIELD_CREATOR_OBJECT_ID)
	@XmlElement(name = Definitions.ELEMENT_OBJECT_ID)
	private String _objectId = null;
	@Field(Definitions.SOLR_FIELD_RANK)
	@XmlElement(name = Definitions.ELEMENT_RANK)
	private Integer _rank = null;
	@XmlElement(name = Definitions.ELEMENT_SERVICE_ID)
	private ServiceType _serviceType = null;
	@XmlElement(name = Definitions.ELEMENT_VISUAL_SHAPE)
	private VisualShape _shape = null;
	@XmlElement(name = Definitions.ELEMENT_STATUS)
	private ConfirmationStatus _status = null;
	@XmlElement(name = Definitions.ELEMENT_MEDIA_OBJECT_TYPE)
	private MediaObjectType _type = null;
	@Field(Definitions.SOLR_FIELD_UPDATED)
	private Date _updated = null;
	private UserIdentity _userId = null;
	@Field(Definitions.SOLR_FIELD_VALUE)
	@XmlElement(name = Definitions.ELEMENT_VALUE)
	private String _value = null;
	@XmlElement(name = Definitions.ELEMENT_VISIBILITY)
	private Visibility _visibility = null;
	@XmlElement(name = service.tut.pori.contentanalysis.video.Definitions.ELEMENT_TIMECODELIST)
	private TimecodeList _timecodes = null;

	/**
	 * The type of the media object. 
	 * 
	 */
	@XmlEnum
	public enum MediaObjectType{
		/** media object containing a keyword or a tag */
		@XmlEnumValue(value = MEDIAOBJECTTYPE_KEYWORD)
		KEYWORD(1),
		/** media object containing generic name-value type metadata */
		@XmlEnumValue(value = MEDIAOBJECTTYPE_METADATA)
		METADATA(2),
		/** generic unspecified media object */
		@XmlEnumValue(value = MEDIAOBJECTTYPE_OBJECT)
		OBJECT(3),
		/** media object containing a face recognition data */
		@XmlEnumValue(value = MEDIAOBJECTTYPE_FACE)
		FACE(4);

		private int _value;

		/**
		 * 
		 * @param value
		 */
		private MediaObjectType(int value){
			_value = value;
		}

		/**
		 * 
		 * @return the type as integer
		 */
		public int toInt(){
			return _value;
		}

		/**
		 * 
		 * @param types
		 * @return the set of types converted to int array or null if null or empty set was passed
		 */
		public static int[] toIntArray(EnumSet<MediaObjectType> types){
			if(types == null || types.isEmpty()){
				LOGGER.debug("Empty set.");
				return null;
			}
			int[] array = new int[types.size()];
			int index = 0;
			for(MediaObjectType t : types){
				array[index++] = t.toInt();
			}
			return array;
		}

		/**
		 * 
		 * @param types
		 * @return data groups associated with the given types or null if null or empty set was passed
		 */
		public static DataGroups getDataGroup(EnumSet<MediaObjectType> types){
			if(types == null || types.isEmpty())
				return null;
			DataGroups groups = new DataGroups();
			for(MediaObjectType type : types){
				switch(type){
					case FACE:
						groups.addDataGroup(Definitions.DATA_GROUP_FACE);
						break;
					case KEYWORD:
						groups.addDataGroup(Definitions.DATA_GROUP_KEYWORDS);
						break;
					case METADATA:
						groups.addDataGroup(Definitions.DATA_GROUP_METADATA);
						break;
					case OBJECT:
						groups.addDataGroup(Definitions.DATA_GROUP_OBJECT);
						break;
					default:
						LOGGER.warn("Ignored unknown object type: "+type.toInt());
				}  // switch
			}  // for
			return groups;
		}

		/**
		 * 
		 * @param value
		 * @return value converted to a type
		 * @throws IllegalArgumentException on bad value
		 */
		public static MediaObjectType fromInt(int value) throws IllegalArgumentException{
			for(MediaObjectType t : MediaObjectType.values()){
				if(t._value == value){
					return t;
				}
			}
			throw new IllegalArgumentException("Bad "+MediaObjectType.class.toString()+" : "+value);
		}

		/**
		 * Allows convertion of data group string to enumerations based on the enumeration value (string/name)
		 * 
		 * @param dataGroups
		 * @return set of types associated with the given data groups or null if empty or null data group was passed
		 */
		public static EnumSet<MediaObjectType> fromDataGroups(DataGroups dataGroups){
			if(DataGroups.isEmpty(dataGroups)){
				LOGGER.debug("Empty data group.");
				return null;
			}

			if(DataGroups.hasDataGroup(DataGroups.DATA_GROUP_ALL, dataGroups)){
				return EnumSet.allOf(MediaObjectType.class);
			}

			EnumSet<MediaObjectType> results = EnumSet.noneOf(MediaObjectType.class);
			if(DataGroups.hasDataGroup(Definitions.DATA_GROUP_KEYWORDS, dataGroups)){
				results.add(KEYWORD);
			}
			if(DataGroups.hasDataGroup(Definitions.DATA_GROUP_FACE, dataGroups)){
				results.add(FACE);
			}
			if(DataGroups.hasDataGroup(Definitions.DATA_GROUP_METADATA, dataGroups)){
				results.add(METADATA);
			}
			if(DataGroups.hasDataGroup(Definitions.DATA_GROUP_OBJECT, dataGroups)){
				results.add(OBJECT);
			}

			if(results.isEmpty()){
				return null;
			}else{
				return results;
			}
		}
	}  // enum MediaObjectType

	/**
	 * The confirmation status of the media object.
	 *
	 * 
	 */
	@XmlEnum
	public enum ConfirmationStatus{
		/** object has been automatically generated and user has not confirmed whether he/she wants it or not */
		@XmlEnumValue(value = CONFIRMATIONSTATUS_CANDIDATE)
		CANDIDATE(1),
		/** this object has been accepted by the user, it can originally be created by a back-end or by the user */
		@XmlEnumValue(value = CONFIRMATIONSTATUS_USER_CONFIRMED)
		USER_CONFIRMED(2),
		/** basically this means that user does not want the object, and it should be deleted. */
		@XmlEnumValue(value = CONFIRMATIONSTATUS_USER_REJECTED)
		USER_REJECTED(3),
		/** back-end has requested this object to be removed as not being valid, possibly because of previously inaccurate analysis results. */
		@XmlEnumValue(value = CONFIRMATIONSTATUS_BACKEND_REMOVED)
		BACKEND_REMOVED(4),
		/** special status declaring that no friendly keyword data is available */
		@XmlEnumValue(value = CONFIRMATIONSTATUS_NO_FRIENDLY_KEYWORD)
		NO_FRIENDLY_KEYWORD(5);

		private int _value;

		/**
		 * 
		 * @param value
		 */
		private ConfirmationStatus(int value){
			_value = value;
		}

		/**
		 * 
		 * @return status as integer
		 */
		public int toInt(){
			return _value;
		}

		/**
		 * 
		 * @param status
		 * @return the given set as an integer array or null if null or empty set was passed
		 */
		public static int[] toIntArray(EnumSet<ConfirmationStatus> status){
			if(status == null || status.isEmpty()){
				return null;
			}
			int[] array = new int[status.size()];
			int index = 0;
			for(ConfirmationStatus c : status){
				array[index++] = c.toInt();
			}
			return array;
		}

		/**
		 * 
		 * @param value
		 * @return the value converted to status
		 * @throws IllegalArgumentException
		 */
		public static ConfirmationStatus fromInt(int value) throws IllegalArgumentException{
			for(ConfirmationStatus t : ConfirmationStatus.values()){
				if(t._value == value){
					return t;
				}
			}
			throw new IllegalArgumentException("Bad "+ConfirmationStatus.class.toString()+" : "+value);
		}

		/**
		 * 
		 * @param dataGroups
		 * @return null if datagroups is null or empty or does not contain any valid datagroups
		 * 
		 */
		public static EnumSet<ConfirmationStatus> fromDataGroups(DataGroups dataGroups){
			if(dataGroups == null){
				return null;
			}
			
			if(DataGroups.hasDataGroup(DataGroups.DATA_GROUP_ALL, dataGroups)){
				return EnumSet.allOf(ConfirmationStatus.class);
			}
			
			EnumSet<ConfirmationStatus> results = EnumSet.noneOf(ConfirmationStatus.class);
			if(DataGroups.hasDataGroup(Definitions.DATA_GROUP_BACKEND_REMOVED, dataGroups)){
				results.add(BACKEND_REMOVED);
			}		
			if(DataGroups.hasDataGroup(Definitions.DATA_GROUP_CANDIDATE, dataGroups)){
				results.add(CANDIDATE);
			}		
			if(DataGroups.hasDataGroup(Definitions.DATA_GROUP_USER_CONFIRMED, dataGroups)){
				results.add(USER_CONFIRMED);
			}	
			if(DataGroups.hasDataGroup(Definitions.DATA_GROUP_USER_REJECTED, dataGroups)){
				results.add(USER_REJECTED);
			}
			
			if(results.isEmpty()){
				return null;
			}else{
				return results;
			}
		}
	}  // enum ConfirmationStatus

	/**
	 * for serialization, must be public for solr.
	 */
	public MediaObject(){
		// nothing needed
	}

	/**
	 * 
	 * @param mediaType
	 * @param mediaObjectType
	 */
	public MediaObject(MediaType mediaType, MediaObjectType mediaObjectType){
		_type = mediaObjectType;
		_mediaType = mediaType;
	}

	/**
	 * 
	 * @return confidence estimate for this object, if < 1 or null, the confidence is assumed to be unknown.
	 * @see #setConfidence(Double)
	 */
	public Double getConfidence() {
		return _confidence;
	}

	/**
	 * 
	 * @param confidence
	 * @see #getConfidence()
	 */
	public void setConfidence(Double confidence) {
		_confidence = confidence;
	}

	/**
	 * 
	 * @return the owner user identity
	 * @see #setOwnerUserId(UserIdentity)
	 */
	public UserIdentity getOwnerUserId() {
		return _userId;
	}

	/**
	 * 
	 * @param userId
	 * @see #getOwnerUserId()
	 */
	public void setOwnerUserId(UserIdentity userId) {
		_userId = userId;
	}
	
	/**
	 * @see #getOwnerUserId()
	 * 
	 * @return owner user identity value
	 */
	@XmlElement(name = core.tut.pori.users.Definitions.ELEMENT_USER_ID)
	public Long getOwnerUserIdValue() {
		return (_userId == null ? null : _userId.getUserId());
	}

	/**
	 * for serialization
	 * @param userId
	 * @see #setOwnerUserId(UserIdentity)
	 */
	@Field(Definitions.SOLR_FIELD_USER_ID)
	private void setOwnerUserIdValue(Long userId) {
		_userId = (userId == null ? null : new UserIdentity(userId));
	}

	/**
	 * 
	 * @return service type
	 * @see #setServiceType(service.tut.pori.contentanalysis.CAContentCore.ServiceType)
	 */
	public ServiceType getServiceType() {
		return _serviceType;
	}

	/**
	 * 
	 * @param serviceType
	 * @see #getServiceType()
	 */
	public void setServiceType(ServiceType serviceType) {
		_serviceType = serviceType;
	}

	/**
	 * 
	 * @return object value, can be same as name
	 * @see #getName()
	 * @see #setValue(String)
	 */
	public String getValue() {
		return _value;
	}

	/**
	 * Generally externally generated id, unique only when used in combination with back-end id, or if back-end id is missing, with user id.
	 * 
	 * @return non-unique creator specific object id
	 * @see #setObjectId(String)
	 */
	public String getObjectId() {
		return _objectId;
	}

	/**
	 * 
	 * @param objectId
	 * @see #getObjectId()
	 */
	public void setObjectId(String objectId) {
		_objectId = objectId;
	}

	/**
	 * Internally generated, unique Id.
	 * 
	 * @return DB (row) id
	 * @see #setMediaObjectId(String)
	 */
	public String getMediaObjectId() {
		return _mediaObjectId;
	}

	/**
	 * 
	 * @param id DB (row) id
	 * @see #getMediaObjectId()
	 */
	public void setMediaObjectId(String id) {
		_mediaObjectId = id;
	}

	/**
	 * 
	 * @return back-end id
	 * @see #setBackendId(Integer)
	 */
	public Integer getBackendId() {
		return _backendId;
	}

	/**
	 * 
	 * @param backendId
	 * @see #getBackendId()
	 */
	public void setBackendId(Integer backendId) {
		_backendId = backendId;
	}

	/**
	 * 
	 * @return status
	 * @see #setConfirmationStatus(ConfirmationStatus)
	 */
	public ConfirmationStatus getConfirmationStatus() {
		return _status;
	}

	/**
	 * 
	 * @param status
	 * @see #getConfirmationStatus()
	 */
	public void setConfirmationStatus(ConfirmationStatus status) {
		_status = status;
	}
	
	/**
	 * for serialization
	 * 
	 * @param value
	 * @see #setConfirmationStatus(ConfirmationStatus)
	 */
	@Field(Definitions.SOLR_FIELD_STATUS)
	private void setConfirmationStatusValue(Integer value){
		_status = (value == null ? null : ConfirmationStatus.fromInt(value));
	}
	
	/**
	 * @see #getConfirmationStatus()
	 * 
	 * @return status value
	 */
	public Integer getConfirmationStatusValue(){
		return (_status == null ? null : _status.toInt());
	}

	/**
	 * 
	 * @return shape
	 * @see #setVisualShape(VisualShape)
	 */
	public VisualShape getVisualShape() {
		return _shape;
	}

	/**
	 * 
	 * @param shape
	 * @see #getVisualShape()
	 */
	public void setVisualShape(VisualShape shape) {
		_shape = shape;
	}

	/**
	 * 
	 * @param value
	 * @see #getValue()
	 */
	public void setValue(String value) {
		_value = value;
	}

	/**
	 * 
	 * @param type
	 * @see #getMediaObjectType()
	 */
	public void setMediaObjectType(MediaObjectType type) {
		_type = type;
	}
	
	/**
	 * 
	 * @return object type
	 * @see #setMediaObjectType(MediaObjectType)
	 */
	public MediaObjectType getMediaObjectType() {
		return _type;
	}
	
	/**
	 * 
	 * @param value
	 * @see #setMediaObjectType(MediaObjectType)
	 */
	@Field(Definitions.SOLR_FIELD_MEDIA_OBJECT_TYPE)
	private void setMediaObjectTypeValue(Integer value) {
		_type = (value == null ? null : MediaObjectType.fromInt(value));
	}
	
	/**
	 * @see #getMediaObjectType()
	 * 
	 * @return object type value
	 */
	public Integer getMediaObjectTypeValue() {
		return (_type == null ? null : _type.toInt());
	}
	
	/**
	 * 
	 * @param serviceId
	 * @see #setServiceType(service.tut.pori.contentanalysis.CAContentCore.ServiceType)
	 */
	@Field(Definitions.SOLR_FIELD_SERVICE_ID)
	private void setServiceId(Integer serviceId){
		_serviceType = (serviceId == null ? null : ServiceType.fromServiceId(serviceId));
	}
	
	/**
	 * @see #getServiceType()
	 * 
	 * @return service type id
	 */
	public Integer getServiceId() {
		return (_serviceType == null ? null : _serviceType.getServiceId());
	}

	/**
	 * 
	 * @return name or short description of the object
	 * @see #setName(String)
	 */
	public String getName() {
		return _name;
	} 

	/**
	 * 
	 * @param name
	 * @see #getName()
	 */
	public void setName(String name) {
		_name = name;
	}

	/**
	 * 
	 * @param object can be null
	 * @return true if the passed object is valid
	 */
	public static boolean isValid(MediaObject object){
		if(object == null){
			return false;
		}else{
			return object.isValid();
		}
	}

	/**
	 * 
	 * @return true if the object is valid
	 * @see #isValid(MediaObject)
	 */
	protected boolean isValid(){
		if(_objectId == null || _status == null || _type == null){
			if(_mediaObjectId == null){
				LOGGER.debug("No mediaObjectId.");
			}
			return false;
		}else if(MediaObjectType.KEYWORD.equals(_type) && StringUtils.isBlank(_value) && StringUtils.isBlank(_value)){
			LOGGER.warn("Invalid name or value for object of type "+MediaObjectType.KEYWORD.name());
			return false;
		}else if(MediaObjectType.METADATA.equals(_type) && (StringUtils.isBlank(_value) || StringUtils.isBlank(_value))){
			LOGGER.warn("Invalid name/value pair for object of type "+MediaObjectType.METADATA.name());
			return false;
		}else if(_timecodes != null && !TimecodeList.isValid(_timecodes)){
			LOGGER.warn("Invalid timecode list.");
			return false;
		}else if(_shape != null){
			return VisualShape.isValid(_shape);
		}else{
			return true;
		}
	}
	
	/**
	 * 
	 * @param visibility 
	 * @see #getVisibility()
	 */
	public void setVisibility(Visibility visibility){
		_visibility = visibility;
	}

	/**
	 * 
	 * @return object visibility
	 * @see #setVisibility(service.tut.pori.contentanalysis.CAContentCore.Visibility)
	 */
	public Visibility getVisibility(){
		return _visibility;
	}
	
	/**
	 * @see #getVisibility()
	 * 
	 * @return visibility value
	 */
	public Integer getVisibilityValue(){
		return (_visibility == null ? null : _visibility.toInt());
	}
	
	/**
	 * 
	 * @param visibility
	 * @see #setVisibility(service.tut.pori.contentanalysis.CAContentCore.Visibility)
	 */
	@Field(Definitions.SOLR_FIELD_VISIBILITY)
	private void setVisibilityValue(Integer visibility){
		_visibility = (visibility == null ? null : Visibility.fromInt(visibility));
	}

	/**
	 * @return the updated
	 */
	public Date getUpdated() {
		return _updated;
	}

	/**
	 * @param updated the updated to set
	 */
	public void setUpdated(Date updated) {
		_updated = updated;
	}

	@Field(Definitions.SOLR_FIELD_VISUAL_SHAPE_TYPE)
	@Override
	public void setVisualShapeTypeId(Integer type) throws IllegalArgumentException {
		_shape = VisualShape.setVisualShapeTypeId(_shape, type);
	}

	@Override
	public Integer getVisualShapeTypeId() {
		return VisualShape.getVisualShapeTypeId(_shape);
	}

	@Field(Definitions.SOLR_FIELD_VISUAL_SHAPE_VALUE)
	@Override
	public void setVisualShapeValue(String value) {
		_shape = VisualShape.setValue(_shape, value);
	}

	@Override
	public String getVisualShapeValue() {
		return VisualShape.getValue(_shape);
	}

	/**
	 * @return the rank
	 * @see #setRank(Integer)
	 */
	public Integer getRank() {
		return _rank;
	}

	/**
	 * @param rank the rank to set
	 * @see #getRank()
	 */
	public void setRank(Integer rank) {
		_rank = rank;
	}

	/**
	 * @return the mediaType
	 */
	public MediaType getMediaType() {
		return _mediaType;
	}

	/**
	 * @param mediaType the mediaType to set
	 */
	public void setMediaType(MediaType mediaType) {
		_mediaType = mediaType;
	}
	
	/**
	 * for serialization
	 * 
	 * @param value
	 * @see #setMediaType(core.tut.pori.utils.MediaUrlValidator.MediaType)
	 */
	@Field(Definitions.SOLR_FIELD_MEDIA_TYPE)
	private void setMediaTypeValue(Integer value){
		setMediaType(value == null ? null : MediaType.fromInt(value));
	}
	
	/**
	 * @see #getMediaType()
	 * 
	 * @return status value
	 */
	public Integer getMediaTypeValue(){
		MediaType mediaType = getMediaType();
		return (mediaType == null ? null : mediaType.toInt());
	}
	
	/**
	 * @return the timecodes
	 */
	public TimecodeList getTimecodes() {
		return _timecodes;
	}

	/**
	 * Solr serializing helper method
	 * @param timecodes the timecodes to set in SolrJ format.
	 */
	@Field(Definitions.SOLR_FIELD_TIMECODES)
	public void setSolrTimecodes(List<String> timecodes) {
		_timecodes = TimecodeList.populateTimecodes(timecodes);
	}
	
	/**
	 * Solr serializing helper method
	 * @return the SolrJ formatted list of timecodes
	 */
	public List<String> getSolrTimecodes() {
		return TimecodeList.getSolrTimecodes(_timecodes);
	}
	
	/**
	 * @param timecodes the timecodes to set
	 */
	public void setTimecodes(TimecodeList timecodes) {
		_timecodes = timecodes;
	}
}
