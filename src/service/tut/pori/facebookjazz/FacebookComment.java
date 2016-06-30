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
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang3.StringUtils;

import com.restfb.types.CategorizedFacebookType;
import com.restfb.types.Comment;

import core.tut.pori.utils.ISODateAdapter;


/**
 * A user comment retrieved from Facebook.
 * 
 * <h2>Optional Elements</h2>
 * <ul>
 *  <li>{@value service.tut.pori.facebookjazz.Definitions#ELEMENT_LIKE_COUNT}</li>
 * </ul>
 * 
 * <h3>XML Example</h3>
 * 
 * {@doc.restlet service="[service.tut.pori.facebookjazz.reference.Definitions#SERVICE_FBJ_REFERENCE_EXAMPLE]" method="[service.tut.pori.facebookjazz.Definitions#ELEMENT_COMMENT]" type="GET" query="" body_uri=""}
 *
 * @see com.restfb.types.Comment
 */
@XmlRootElement(name=Definitions.ELEMENT_COMMENT)
@XmlAccessorType(XmlAccessType.NONE)
public class FacebookComment {
	private Comment _comment = null;
	private Integer _messageWeight = null;
	
	/**
	 * 
	 */
	public FacebookComment(){
		_comment = new Comment();
	}
	
	/**
	 * 
	 * @param comment
	 * @throws IllegalArgumentException
	 */
	public FacebookComment(Comment comment) throws IllegalArgumentException{
		if(comment == null){
			throw new IllegalArgumentException("Invalid comment.");
		}
		_comment = comment;
	}

	/**
	 * @param date
	 * @see com.restfb.types.Comment#setCreatedTime(java.util.Date)
	 * @see #getCreatedTime()
	 */
	public void setCreatedTime(Date date) {
		_comment.setCreatedTime(date);
	}

	/**
	 * @param count
	 * @see com.restfb.types.Comment#setLikeCount(java.lang.Long)
	 * @see #getLikeCount()
	 */
	public void setLikeCount(Long count) {
		_comment.setLikeCount(count);
	}

	/**
	 * @param message
	 * @see com.restfb.types.Comment#setMessage(java.lang.String)
	 * @see #getMessage()
	 */
	public void setMessage(String message) {
		_comment.setMessage(message);
	}

	/**
	 * convert the given list of comments to FacebookComments
	 * 
	 * @param comments
	 * @return the given comments converted to facebook comment or null if null or empty list was passed
	 */
	public static List<FacebookComment> getCommentList(List<Comment> comments){
		List<FacebookComment> list = null;
		if(comments != null && !comments.isEmpty()){
			list = new ArrayList<>();
			for(Iterator<Comment> iter = comments.iterator();iter.hasNext();){
				FacebookComment comment = new FacebookComment(iter.next());
				if(comment != null){
					list.add(comment);
				}   
			}  // for
			if(list.isEmpty()){
				list = null;
			}
		}
		return list;
	}

	/**
	 * @see com.restfb.types.Comment#getCreatedTime()
	 * 
	 * @return created time
	 * @see #setCreatedTime(Date)
	 */
	@XmlJavaTypeAdapter(ISODateAdapter.class)
	@XmlElement(name = Definitions.ELEMENT_CREATED_TIMESTAMP)
	public Date getCreatedTime() {
		return _comment.getCreatedTime();
	}

	/**
	 * @see com.restfb.types.Comment#getFrom()
	 * @see com.restfb.types.CategorizedFacebookType#getName()
	 * @see #setFromName(String)
	 * 
	 * @return sender name
	 */
	@XmlElement(name = Definitions.ELEMENT_MESSAGE_POSTER)
	public String getFromName() {
		return _comment.getFrom().getName();
	}
	
	/**
	 * @param fromName the fromName to set
	 * @see #getFromName()
	 */
	public void setFromName(String fromName) {
		if(StringUtils.isBlank(fromName)){
			_comment.setFrom(null);
		}else{
			CategorizedFacebookType from = _comment.getFrom();
			if(from == null){
				from = new CategorizedFacebookType();
				_comment.setFrom(from);
			}
			from.setName(fromName);
		}
	}
	
	/**
	 * @see com.restfb.types.Comment#getMessage()
	 * 
	 * @return message
	 * @see #getMessage()
	 */
	@XmlElement(name = Definitions.ELEMENT_MESSAGE)
	public WeightedStringElement getWMessage() {
		String message = _comment.getMessage();
		if(StringUtils.isBlank(message)){
			return null;
		}else{
			return new WeightedStringElement(message, _messageWeight);
		}
	}
	
	/**
	 * for serialization
	 * @param message 
	 * @see #setMessage(String)
	 */
	@SuppressWarnings("unused")
	private void setWMessage(WeightedStringElement message) {
		if(message == null){
			setMessage(null);
			setMessageWeight(null);
		}else{
			setMessage(message.getValue());
			setMessageWeight(message.getWeight());
		}
	}

	/**
	 * @see com.restfb.types.Comment#getLikeCount()
	 * 
	 * @return like count
	 * @see #setLikeCount(Long)
	 */
	@XmlElement(name = Definitions.ELEMENT_LIKE_COUNT)
	public Long getLikeCount() {
		return _comment.getLikeCount();
	}

	/**
	 * @return the commentWeight
	 * @see #setMessageWeight(Integer)
	 */
	public Integer getMessageWeight() {
		return _messageWeight;
	}

	/**
	 * @param messageWeight the commentWeight to set
	 * @see #getMessageWeight()
	 */
	public void setMessageWeight(Integer messageWeight) {
		_messageWeight = messageWeight;
	}

	/**
	 * @return message
	 * @see com.restfb.types.Comment#getMessage()
	 * @see #setMessage(String)
	 */
	public String getMessage() {
		return _comment.getMessage();
	}
}
