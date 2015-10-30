/**
 * Copyright 2014 Tampere University of Technology, Pori Department
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
package service.tut.pori.facebookjazz;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import service.tut.pori.contentanalysis.CAContentCore.ServiceType;

import com.restfb.types.CategorizedFacebookType;
import com.restfb.types.NamedFacebookType;
import com.restfb.types.Photo;
import com.restfb.types.Photo.Tag;
import com.restfb.types.Place;

import core.tut.pori.utils.ISODateAdapter;


/**
 * Photo description retrieved from Facebook.
 * 
 * <h2>Optional Elements</h2>
 * <ul>
 *  <li>{@value service.tut.pori.facebookjazz.Definitions#ELEMENT_COMMENT_LIST}</li>
 *  <li>{@value service.tut.pori.facebookjazz.Definitions#ELEMENT_LIKE_COUNT}</li>
 * </ul>
 * 
 * <h3>XML Example</h3>
 * 
 * {@doc.restlet service="[service.tut.pori.facebookjazz.reference.Definitions#SERVICE_FBJ_REFERENCE_EXAMPLE]" method="[service.tut.pori.facebookjazz.Definitions#ELEMENT_PHOTO_DESCRIPTION]" type="GET" query="" body_uri=""}
 *
 * @see com.restfb.types.Photo
 */
@XmlRootElement(name=Definitions.ELEMENT_PHOTO_DESCRIPTION)
@XmlAccessorType(XmlAccessType.NONE)
public class FacebookPhotoDescription {
	private static final Logger LOGGER = Logger.getLogger(FacebookPhotoDescription.class);
	private List<FacebookPhotoTag> _tags = null;
	private List<FacebookComment> _comments = null;
	private Integer _descriptionWeight = null;
	private Photo _photo = null;
	@XmlElement(name = Definitions.ELEMENT_PHOTO_GUID)
	private String _photoGUID = null;
	@XmlElement(name = service.tut.pori.contentanalysis.Definitions.ELEMENT_SERVICE_ID)
	private ServiceType _serviceType = null;
	
	/**
	 * 
	 */
	public FacebookPhotoDescription(){
		_photo = new Photo();
	}
	
	/**
	 * 
	 * @param photo
	 * @throws IllegalArgumentException
	 */
	public FacebookPhotoDescription(Photo photo) throws IllegalArgumentException{
		if(photo == null){
			throw new IllegalArgumentException("Invalid photo.");
		}
		_photo = photo;
	}

	/**
	 * 
	 * @return true if this is a valid photo description
	 */
	public boolean isValid(){
		if(StringUtils.isBlank(_photo.getName()) && _photo.getComments().isEmpty() && getFacebookLocation() == null){
			return false;
		}else{
			return true;
		}
	}

	/**
	 * @see com.restfb.types.Photo#getFrom()
	 * @see com.restfb.types.CategorizedFacebookType#getName()
	 * @see #setFromName(String)
	 * 
	 * @return sender name
	 */
	@XmlElement(name = Definitions.ELEMENT_MESSAGE_POSTER)
	public String getFromName() {
		CategorizedFacebookType from = _photo.getFrom();
		return (from == null ? null : from.getName());
	}
	
	/**
	 * @param name 
	 * @see com.restfb.types.Photo#setFrom(com.restfb.types.CategorizedFacebookType)
	 * @see #getFromName()
	 */
	public void setFromName(String name) {
		if(StringUtils.isBlank(name)){
			_photo.setFrom(null);
		}else{
			CategorizedFacebookType from = _photo.getFrom();
			if(from == null){
				from = new CategorizedFacebookType();
				_photo.setFrom(from);
			}
			from.setName(name);
		}
	}

	/**
	 * @see com.restfb.types.Photo#getPlace()
	 * @see #setFacebookLocation(FacebookLocation)
	 * 
	 * @return location
	 */
	@XmlElement(name = Definitions.ELEMENT_LOCATION)
	public FacebookLocation getFacebookLocation() {
		return FacebookLocation.getFacebookLocation(_photo.getPlace());
	}
	
	/**
	 * @param location 
	 * @see com.restfb.types.Photo#setPlace(Place)
	 * @see #getFacebookLocation()
	 */
	public void setFacebookLocation(FacebookLocation location) {
		_photo.setPlace((location == null ? null : location.toPlace()));
	}

	/**
	 * @see com.restfb.types.Photo#getCreatedTime()
	 * @see #setCreatedTime(Date)
	 * 
	 * @return created timestamp
	 */
	@XmlElement(name = Definitions.ELEMENT_CREATED_TIMESTAMP)
	@XmlJavaTypeAdapter(ISODateAdapter.class)
	public Date getCreatedTime() {
		return _photo.getCreatedTime();
	}

	/**
	 * @see com.restfb.types.Photo#getUpdatedTime()
	 * @see #setUpdatedTime(Date)
	 * 
	 * @return updated timestamp
	 */
	@XmlElement(name = Definitions.ELEMENT_UPDATED_TIMESTAMP)
	@XmlJavaTypeAdapter(ISODateAdapter.class)
	public Date getUpdatedTime() {
		return _photo.getUpdatedTime();
	}

	/**
	 * @see com.restfb.types.Photo#getComments()
	 * @see #setDescriptionComments(List)
	 * 
	 * @return comments or null if none
	 */
	@XmlElementWrapper(name = Definitions.ELEMENT_COMMENT_LIST)
	@XmlElement(name = Definitions.ELEMENT_COMMENT)
	public List<FacebookComment> getDescriptionComments() {
		if(_comments == null){
			_comments = FacebookComment.getCommentList(_photo.getComments());
		}
		return _comments;
	}
	
	/**
	 * 
	 * @param comments
	 * @see #getDescriptionComments()
	 */
	public void setDescriptionComments(List<FacebookComment> comments) {
		_comments = comments;
	}

	/**
	 * @see com.restfb.types.Photo#getLikes()
	 * 
	 * @return like count
	 */
	@XmlElement(name = Definitions.ELEMENT_LIKE_COUNT)
	public Long getLikeCount() {
		return (long) _photo.getLikes().size();  // this is long elsewhere, so make it long here
	}
	
	/**
	 * This method does nothing and exists only to enable unmarshalling/marshalling.
	 * 
	 * If you want to add likes use addLike()
	 * 
	 * @see #addLike(NamedFacebookType)
	 * @see #getLikeCount()
	 * 
	 * @param likeCount
	 */
	@SuppressWarnings("unused")
	private void setLikeCount(Long likeCount){
		LOGGER.warn("Ignored "+Definitions.ELEMENT_LIKE_COUNT+" : "+likeCount);
	}

	/**
	 * @see com.restfb.types.Photo#getName()
	 * @see #getDescription()
	 * 
	 * @return description
	 */
	@XmlElement(name = Definitions.ELEMENT_DESCRIPTION)
	public WeightedStringElement getWDescription() {
		String description = _photo.getName();
		if(StringUtils.isBlank(description)){
			return null;
		}else{
			return new WeightedStringElement(description, _descriptionWeight);
		}
	}
	
	/**
	 * for serialization
	 * 
	 * @param description
	 * @see #setDescription(String)
	 */
	@SuppressWarnings("unused")
	private void setWDescription(WeightedStringElement description) {
		if(description == null){
			_photo.setName(null);
			setDescriptionWeight(null);
		}else{
			_photo.setName(description.getValue());
			setDescriptionWeight(description.getWeight());
		}
	}

	/**
	 * @see com.restfb.types.Photo#getSource()
	 * 
	 * @return source
	 */
	@XmlElement(name = Definitions.ELEMENT_SOURCE)
	public String getSource() {
		return _photo.getSource();
	}
	
	/**
	 * @see com.restfb.types.Photo#getTags()
	 * @see #setTagList(List)
	 * 
	 * @return tags converted to TagList or null if none
	 */
	@XmlElementWrapper(name = Definitions.ELEMENT_PHOTO_TAG_LIST)
	@XmlElement(name = Definitions.ELEMENT_PHOTO_TAG)
	public List<FacebookPhotoTag> getTagList(){
		if(_tags != null){
			return _tags;
		} 
		
		List<Tag> tags = _photo.getTags();
		if(tags == null || tags.isEmpty()){
			return null;
		}
		_tags = new ArrayList<>();
		for(Tag t : tags){
			FacebookPhotoTag tag = FacebookPhotoTag.getFacebookTag(t);
			if(tag != null){
				_tags.add(tag);
			}	
		} // for
		if(_tags.isEmpty()){
			return null;
		}else{
			return _tags;
		}
	}
	
	/**
	 * 
	 * @param tags
	 * @see #getTagList()
	 */
	public void setTagList(List<FacebookPhotoTag> tags){
		if(tags == null){
			_tags = new ArrayList<>(); // clear the tag list
		}else{ // allow empty
			_tags = tags;
		}
	}
	
	/**
	 * 
	 * @param tag
	 * @see #getTagList()
	 */
	public void addTag(FacebookPhotoTag tag){
		List<FacebookPhotoTag> tags = getTagList();
		if(tags == null){
			tags = new ArrayList<>();
		}
		tags.add(tag);
		_tags = tags; // make sure the updated tag list is the current one
	}

	/**
	 * @return the descriptionWeight
	 * @see #setDescriptionWeight(Integer)
	 */
	public Integer getDescriptionWeight() {
		return _descriptionWeight;
	}

	/**
	 * @param descriptionWeight the descriptionWeight to set
	 * @see #getDescriptionWeight()
	 */
	public void setDescriptionWeight(Integer descriptionWeight) {
		_descriptionWeight = descriptionWeight;
	}

	/**
	 * @return the photoGUID
	 * @see #setPhotoGUID(String)
	 */
	public String getPhotoGUID() {
		return _photoGUID;
	}

	/**
	 * @param photoGUID the photoGUID to set
	 * @see #getPhotoGUID()
	 */
	public void setPhotoGUID(String photoGUID) {
		_photoGUID = photoGUID;
	}

	/**
	 * @return the serviceType
	 * @see #setServiceType(service.tut.pori.contentanalysis.CAContentCore.ServiceType)
	 */
	public ServiceType getServiceType() {
		return _serviceType;
	}

	/**
	 * @param serviceType the serviceType to set
	 * @see #getServiceType()
	 */
	public void setServiceType(ServiceType serviceType) {
		_serviceType = serviceType;
	}

	/**
	 * @param like
	 * @return true on success
	 * @see com.restfb.types.Photo#addLike(com.restfb.types.NamedFacebookType)
	 * @see #getLikeCount()
	 */
	public boolean addLike(NamedFacebookType like) {
		return _photo.addLike(like);
	}

	/**
	 * @return id
	 * @see com.restfb.types.FacebookType#getId()
	 * @see #setId(String)
	 */
	public String getId() {
		return _photo.getId();
	}

	/**
	 * @param date
	 * @see com.restfb.types.Photo#setCreatedTime(java.util.Date)
	 * @see #getCreatedTime()
	 */
	public void setCreatedTime(Date date) {
		_photo.setCreatedTime(date);
	}

	/**
	 * @param date
	 * @see com.restfb.types.Photo#setUpdatedTime(java.util.Date)
	 * @see #getUpdatedTime()
	 */
	public void setUpdatedTime(Date date) {
		_photo.setUpdatedTime(date);
	}

	/**
	 * @return name
	 * @see com.restfb.types.NamedFacebookType#getName()
	 * @see #setDescription(String)
	 */
	public String getDescription() {
		return _photo.getName();
	}

	/**
	 * @param description
	 * @see com.restfb.types.NamedFacebookType#setName(java.lang.String)
	 * @see #getDescription()
	 */
	public void setDescription(String description) {
		_photo.setName(description);
	}
	
	/**
	 * 
	 * @param photos
	 * @return the photos wrapped as descriptions or null if null or empty list was passed
	 */
	public static List<FacebookPhotoDescription> getFacebookPhotoDescriptions(List<Photo> photos){
		if(photos == null || photos.isEmpty()){
			return null;
		}
		List<FacebookPhotoDescription> pds = new ArrayList<>(photos.size());
		for(Photo p : photos){
			pds.add(new FacebookPhotoDescription(p));
		}
		return pds;
	}

	/**
	 * @param id
	 * @see com.restfb.types.FacebookType#setId(java.lang.String)
	 * @see #getId()
	 */
	public void setId(String id) {
		_photo.setId(id);
	}
	
	
}
