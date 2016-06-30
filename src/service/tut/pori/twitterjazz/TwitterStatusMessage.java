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
package service.tut.pori.twitterjazz;

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import twitter4j.Status;
import core.tut.pori.utils.ISODateAdapter;

/**
 * A status message received from Twitter.
 * 
 * <h3>XML Example</h3>
 * 
 * {@doc.restlet service="[service.tut.pori.twitterjazz.reference.Definitions#SERVICE_TJ_REFERENCE_EXAMPLE]" method="[service.tut.pori.twitterjazz.Definitions#ELEMENT_STATUS_MESSAGE]" type="GET" query="" body_uri=""}
 * 
 * @see twitter4j.Status
 */
@XmlRootElement(name=Definitions.ELEMENT_STATUS_MESSAGE)
@XmlAccessorType(XmlAccessType.NONE)
public class TwitterStatusMessage {
	@XmlElement(name = Definitions.ELEMENT_MESSAGE_POSTER)
	private String _fromName = null;
	@XmlElement(name = Definitions.ELEMENT_MESSAGE)
	private String _message = null;
	@XmlJavaTypeAdapter(ISODateAdapter.class)
	@XmlElement(name = Definitions.ELEMENT_UPDATED_TIMESTAMP)
	private Date _updatedTime = null;
	
	/**
	 * 
	 * @param status
	 * @return status message or null if the passed status was null
	 */
	public static TwitterStatusMessage getTwitterStatusMessage(Status status){
		if(status == null){
			return null;
		}
		TwitterStatusMessage message = new TwitterStatusMessage();
		message._fromName = status.getUser().getScreenName();
		message._message = status.getText();
		message._updatedTime = status.getCreatedAt();
		return message;
	}

	/**
	 * @see twitter4j.Status#getUser()
	 * @see twitter4j.User#getScreenName()
	 * @see #setFromName(String)
	 * 
	 * @return the fromName
	 */
	public String getFromName() {
		return _fromName;
	}

	/**
	 * @param fromName the fromName to set
	 * @see #getFromName()
	 */
	public void setFromName(String fromName) {
		_fromName = fromName;
	}

	/**
	 * @see twitter4j.Status#getText()
	 * 
	 * @return the message
	 * @see #setMessage(String)
	 */
	public String getMessage() {
		return _message;
	}

	/**
	 * @param message the message to set
	 * @see #getMessage()
	 */
	public void setMessage(String message) {
		_message = message;
	}

	/**
	 * @see twitter4j.Status#getCreatedAt()
	 * @see #setUpdatedTime(Date)
	 * 
	 * @return the updatedTime
	 */
	public Date getUpdatedTime() {
		return _updatedTime;
	}

	/**
	 * @param updatedTime the updatedTime to set
	 * @see #getUpdatedTime()
	 */
	public void setUpdatedTime(Date updatedTime) {
		_updatedTime = updatedTime;
	}
}
