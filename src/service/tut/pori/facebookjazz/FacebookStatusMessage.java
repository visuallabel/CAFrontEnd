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

import com.restfb.types.NamedFacebookType;
import com.restfb.types.StatusMessage;

import core.tut.pori.utils.ISODateAdapter;


/**
 * Status message retrieved from Facebook.
 * 
 * <h2>Optional Elements</h2>
 * <ul>
 *  <li>{@value service.tut.pori.facebookjazz.Definitions#ELEMENT_COMMENT_LIST}</li>
 *  <li>{@value service.tut.pori.facebookjazz.Definitions#ELEMENT_LIKE_COUNT}</li>
 * </ul>
 * 
 * <h3>XML Example</h3>
 * 
 * {@doc.restlet service="[service.tut.pori.facebookjazz.reference.Definitions#SERVICE_FBJ_REFERENCE_EXAMPLE]" method="[service.tut.pori.facebookjazz.Definitions#ELEMENT_STATUS_MESSAGE]" type="GET" query="" body_uri=""}
 *
 * @see com.restfb.types.StatusMessage
 */
@XmlRootElement(name=Definitions.ELEMENT_STATUS_MESSAGE)
@XmlAccessorType(XmlAccessType.NONE)
public class FacebookStatusMessage {
	private static final Logger LOGGER = Logger.getLogger(FacebookStatusMessage.class);
	private List<FacebookComment> _comments = null;
	private Integer _messageWeight = null;
	private StatusMessage _statusMessage = null;
	
	/**
	 * 
	 */
	public FacebookStatusMessage() {
		_statusMessage = new StatusMessage();
	}
	
	/**
	 * 
	 * @param statusMessage
	 * @throws IllegalArgumentException
	 */
	public FacebookStatusMessage(StatusMessage statusMessage) throws IllegalArgumentException{
		if(statusMessage == null){
			throw new IllegalArgumentException("Invalid status message.");
		}
		_statusMessage = statusMessage;
	}

	/**
	 * @see com.restfb.types.StatusMessage#getComments()
	 * @see #setMessageComments(List)
	 * 
	 * @return a list of facebook comments based on contained list of comments (if any)
	 */
	@XmlElementWrapper(name = Definitions.ELEMENT_COMMENT_LIST)
	@XmlElement(name = Definitions.ELEMENT_COMMENT)
	public List<FacebookComment> getMessageComments() {
		if(_comments == null){
			_comments = FacebookComment.getCommentList(_statusMessage.getComments());
		}
		return _comments;
	}
	
	/**
	 * 
	 * @param comments
	 * @see #getMessageComments()
	 */
	public void setMessageComments(List<FacebookComment> comments) {
		_comments = comments;
	}

	/**
	 * @see com.restfb.types.StatusMessage#getLikes()
	 * 
	 * @return like count
	 */
	@XmlElement(name = Definitions.ELEMENT_LIKE_COUNT)
	public Long getLikeCount(){
		long count = _statusMessage.getLikes().size();  // create long as it is also long in the FacebookComment
		return count;
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
	 * @see com.restfb.types.StatusMessage#getFrom()
	 * @see com.restfb.types.NamedFacebookType#getName()
	 * @see #setFromName(String)
	 * 
	 * @return sender name
	 */
	@XmlElement(name = Definitions.ELEMENT_MESSAGE_POSTER)
	public String getFromName() {
		NamedFacebookType from = _statusMessage.getFrom();
		return (from == null ? null : from.getName());
	}
	
	/**
	 * @see com.restfb.types.StatusMessage#setFrom(com.restfb.types.NamedFacebookType)
	 * @see #getFromName()
	 * 
	 * @param name 
	 */
	public void setFromName(String name){
		if(StringUtils.isBlank(name)){
			_statusMessage.setFrom(null);
		}else{
			NamedFacebookType from = _statusMessage.getFrom();
			if(from == null){
				from = new NamedFacebookType();
				_statusMessage.setFrom(from);
			}
			from.setName(name);
		}
	}

	/**
	 * @see com.restfb.types.StatusMessage#getMessage()
	 * @see #getMessage()
	 * 
	 * @return message
	 */
	@XmlElement(name = Definitions.ELEMENT_MESSAGE)
	public WeightedStringElement getWMessage() {
		String message = _statusMessage.getMessage();
		if(StringUtils.isBlank(message)){
			return null;
		}else{
			return new WeightedStringElement(message, _messageWeight);
		}
	}
	
	/**
	 * for serialization
	 * 
	 * @param message 
	 * @see com.restfb.types.StatusMessage#setMessage(String)
	 * @see #setMessage(String)
	 */
	@SuppressWarnings("unused")
	private void setWMessage(WeightedStringElement message) {
		if(message == null){
			_statusMessage.setMessage(null);
			setMessageWeight(null);
		}else{
			_statusMessage.setMessage(message.getValue());
			setMessageWeight(message.getWeight());
		}
	}

	/**
	 * @see com.restfb.types.StatusMessage#getUpdatedTime()
	 * @see #setUpdatedTime(Date)
	 * 
	 * @return updated timestamp
	 */
	@XmlElement(name = Definitions.ELEMENT_UPDATED_TIMESTAMP)
	@XmlJavaTypeAdapter(ISODateAdapter.class)
	public Date getUpdatedTime() {
		return _statusMessage.getUpdatedTime();
	}

	/**
	 * 
	 * @return message weight
	 * @see #setMessageWeight(Integer)
	 */
	public Integer getMessageWeight() {
		return _messageWeight;
	}

	/**
	 * 
	 * @param messageWeight
	 * @see #getMessageWeight()
	 */
	public void setMessageWeight(Integer messageWeight) {
		_messageWeight = messageWeight;
	}

	/**
	 * @param like
	 * @return true on success
	 * @see com.restfb.types.StatusMessage#addLike(com.restfb.types.NamedFacebookType)
	 * @see #getLikeCount()
	 */
	public boolean addLike(NamedFacebookType like) {
		return _statusMessage.addLike(like);
	}

	/**
	 * @param message
	 * @see com.restfb.types.StatusMessage#setMessage(java.lang.String)
	 * @see #getMessage()
	 */
	public void setMessage(String message) {
		_statusMessage.setMessage(message);
	}

	/**
	 * @param date
	 * @see com.restfb.types.StatusMessage#setUpdatedTime(java.util.Date)
	 * @see #getUpdatedTime()
	 */
	public void setUpdatedTime(Date date) {
		_statusMessage.setUpdatedTime(date);
	}
	
	/**
	 * 
	 * @param statuses
	 * @return the given statuses wrapped to status messages or null if null or empty list was passed
	 */
	public static List<FacebookStatusMessage> getFacebookStatusMessages(List<StatusMessage> statuses){
		if(statuses == null || statuses.isEmpty()){
			return null;
		}
		List<FacebookStatusMessage> fsm = new ArrayList<>(statuses.size());
		for(StatusMessage sm : statuses){
			fsm.add(new FacebookStatusMessage(sm));
		}
		return fsm;
	}

	/**
	 * @return message
	 * @see com.restfb.types.StatusMessage#getMessage()
	 * @see #setMessage(String)
	 */
	public String getMessage() {
		return _statusMessage.getMessage();
	}
}
