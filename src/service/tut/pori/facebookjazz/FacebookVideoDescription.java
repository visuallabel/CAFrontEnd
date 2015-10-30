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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang3.StringUtils;

import com.restfb.types.CategorizedFacebookType;
import com.restfb.types.Comments;
import com.restfb.types.Video;

import core.tut.pori.utils.ISODateAdapter;
import core.tut.pori.utils.ListUtils;


/**
 * Video description retrieved from Facebook.
 * 
 * <h2>Optional Elements</h2>
 * <ul>
 *  <li>{@value service.tut.pori.facebookjazz.Definitions#ELEMENT_COMMENT_LIST}</li>
 *  <li>{@value service.tut.pori.facebookjazz.Definitions#ELEMENT_LIKE_COUNT}</li>
 *  <li>{@value service.tut.pori.facebookjazz.Definitions#ELEMENT_LOCATION}</li>
 * </ul>
 * 
 * <h3>XML Example</h3>
 * 
 * {@doc.restlet service="[service.tut.pori.facebookjazz.reference.Definitions#SERVICE_FBJ_REFERENCE_EXAMPLE]" method="[service.tut.pori.facebookjazz.Definitions#ELEMENT_VIDEO_DESCRIPTION]" type="GET" query="" body_uri=""}
 * 
 * @see com.restfb.types.Video
 */
@XmlRootElement(name=Definitions.ELEMENT_VIDEO_DESCRIPTION)
@XmlAccessorType(XmlAccessType.NONE)
public class FacebookVideoDescription {
	private List<FacebookComment> _comments = null;
	@XmlElement(name = Definitions.ELEMENT_LIKE_COUNT)
	private Long _likeCount = null;
	private Integer _descriptionWeight = null;
	private Video _video = null;

	/**
	 * 
	 */
	public FacebookVideoDescription(){
		_video = new Video();
	}

	/**
	 * 
	 * @param video
	 * @throws IllegalArgumentException 
	 */
	public FacebookVideoDescription(Video video) throws IllegalArgumentException{
		if(video == null){
			throw new IllegalArgumentException("Invalid video.");
		}
		_video = video;
	}

	/**
	 * 
	 * @return true if this is a valid photo description
	 * 
	 */
	public boolean isValid(){
		String description = _video.getDescription();
		Comments comments = _video.getComments();
		if(StringUtils.isBlank(description) && (comments == null || ListUtils.isEmpty(comments.getData()))){
			return false;
		}else{
			return true;
		}
	}

	/**
	 * 
	 * @param name 
	 * @see #getFromName()
	 */
	public void setFromName(String name){
		if(StringUtils.isBlank(name)){
			_video.setFrom(null);
		}else{
			CategorizedFacebookType from = _video.getFrom();
			if(from == null){
				from = new CategorizedFacebookType();
				_video.setFrom(from);
			}
			from.setName(name);
		}
	}

	/**
	 * @see com.restfb.types.Video#getFrom()
	 * @see com.restfb.types.CategorizedFacebookType#getName()
	 * @see #setFromName(String)
	 * 
	 * @return sender name
	 */
	@XmlElement(name = Definitions.ELEMENT_MESSAGE_POSTER)
	public String getFromName() {
		CategorizedFacebookType from = _video.getFrom();
		return (from == null ? null : from.getName());
	}

	/**
	 * @see com.restfb.types.Video#getDescription()
	 * @see #getDescription()
	 * 
	 * @return description
	 */
	@XmlElement(name = Definitions.ELEMENT_DESCRIPTION)
	public WeightedStringElement getWDescription() {
		String description = _video.getDescription();
		if(description == null){
			description = _video.getName();
		}
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
			setDescription(null);
			setDescriptionWeight(null);
		}else{
			setDescription(description.getValue());
			setDescriptionWeight(description.getWeight());
		}
	}

	/**
	 * @see com.restfb.types.Video#getComments()
	 * @see #setDescriptionComments(List)
	 * 
	 * @return comments or null if none
	 */
	@XmlElement(name = Definitions.ELEMENT_COMMENT_LIST)
	public List<FacebookComment> getDescriptionComments() {
		if(_comments == null){
			Comments comments = _video.getComments();
			return (comments == null ? null : FacebookComment.getCommentList(comments.getData()));
		}else{
			return _comments;
		}
	}

	/**
	 * 
	 * @param comments replace currently set comments with the passed list of comments
	 * @see #getDescriptionComments()
	 */
	public void setDescriptionComments(List<FacebookComment> comments){
		_comments = comments;
	}

	/**
	 * @see com.restfb.types.Video#getCreatedTime()
	 * @see #setCreatedTime(Date)
	 * 
	 * @return created timestamp
	 */
	@XmlJavaTypeAdapter(ISODateAdapter.class)
	@XmlElement(name = Definitions.ELEMENT_CREATED_TIMESTAMP)
	public Date getCreatedTime() {
		return _video.getCreatedTime();
	}

	/**
	 * @see com.restfb.types.Video#getUpdatedTime()
	 * @see #setUpdatedTime(Date)
	 * 
	 * @return  updated timestamp
	 */
	@XmlJavaTypeAdapter(ISODateAdapter.class)
	@XmlElement(name = Definitions.ELEMENT_UPDATED_TIMESTAMP)
	public Date getUpdatedTime() {
		return _video.getUpdatedTime();
	}

	/**
	 * 
	 * @return like count
	 * @see #setLikeCount(Long)
	 */
	public Long getLikeCount() {
		return _likeCount;
	}

	/**
	 * 
	 * @param likeCount 
	 * @see #getLikeCount()
	 */
	public void setLikeCount(Long likeCount) {
		_likeCount = likeCount;
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
	 * @param date
	 * @see com.restfb.types.Video#setCreatedTime(java.util.Date)
	 * @see #getCreatedTime()
	 */
	public void setCreatedTime(Date date) {
		_video.setCreatedTime(date);
	}

	/**
	 * @param description
	 * @see com.restfb.types.Video#setDescription(java.lang.String)
	 * @see #getDescription()
	 */
	public void setDescription(String description) {
		_video.setDescription(description);
	}

	/**
	 * @param date
	 * @see com.restfb.types.Video#setUpdatedTime(java.util.Date)
	 * @see #getUpdatedTime()
	 */
	public void setUpdatedTime(Date date) {
		_video.setUpdatedTime(date);
	}

	/**
	 * @return id
	 * @see com.restfb.types.FacebookType#getId()
	 * @see #setId(String)
	 */
	public String getId() {
		return _video.getId();
	}

	/**
	 * @param id
	 * @see com.restfb.types.FacebookType#setId(java.lang.String)
	 * @see #getId()
	 */
	public void setId(String id) {
		_video.setId(id);
	}

	/**
	 * 
	 * @param videos
	 * @return videos wrapped to video descriptions or null if null or empty list was passed
	 */
	public static List<FacebookVideoDescription> getFacebookVideoDescriptions(List<Video> videos){
		if(videos == null || videos.isEmpty()){
			return null;
		}
		List<FacebookVideoDescription> descriptions = new ArrayList<>(videos.size());
		for(Video v : videos){
			descriptions.add(new FacebookVideoDescription(v));
		}
		return descriptions;
	}

	/**
	 * @return description
	 * @see com.restfb.types.Video#getDescription()
	 * @see #setDescription(String)
	 */
	public String getDescription() {
		return _video.getDescription();
	}
}
