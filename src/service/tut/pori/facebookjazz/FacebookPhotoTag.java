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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import service.tut.pori.contentanalysis.CAContentCore.ServiceType;
import service.tut.pori.contentanalysis.MediaObject;
import service.tut.pori.contentanalysis.MediaObject.MediaObjectType;
import service.tut.pori.contentstorage.FacebookPhotoStorage;

import com.restfb.types.Photo.Tag;

/**
 * Tag of a photo retrieved from Facebook.
 * 
 * <h2>Optional Elements</h2>
 * <ul>
 *  <li>{@value service.tut.pori.contentanalysis.Definitions#ELEMENT_SERVICE_ID}. Not present if the tag was created by the user or internally by the service.</li>
 * </ul>
 * 
 * <h3>XML Example</h3>
 * 
 * {@doc.restlet service="[service.tut.pori.facebookjazz.reference.Definitions#SERVICE_FBJ_REFERENCE_EXAMPLE]" method="[service.tut.pori.facebookjazz.Definitions#ELEMENT_PHOTO_TAG]" type="GET" query="" body_uri=""}
 *
 * @see com.restfb.types.Photo.Tag
 */
@XmlRootElement(name=Definitions.ELEMENT_PHOTO_TAG)
@XmlAccessorType(XmlAccessType.NONE)
public class FacebookPhotoTag {
	private static final Logger LOGGER = Logger.getLogger(FacebookPhotoTag.class);
	@XmlElement(name = service.tut.pori.contentanalysis.Definitions.ELEMENT_SERVICE_ID)
	private ServiceType _serviceType = FacebookPhotoStorage.SERVICE_TYPE;
	private Tag _tag = null;

	/**
	 * 
	 */
	public FacebookPhotoTag() {
		_tag = new Tag();
	}
	
	/**
	 * 
	 * @param tag
	 * @throws IllegalArgumentException
	 */
	public FacebookPhotoTag(Tag tag) throws IllegalArgumentException {
		if(tag == null){
			throw new IllegalArgumentException("Invalid tag.");
		}
		_tag = tag;
	}
	
	/**
	 * @see com.restfb.types.NamedFacebookType#getName()
	 * @see #setName(String)
	 * 
	 * @return tag name
	 */
	@XmlElement(name = Definitions.ELEMENT_VALUE)
	public String getName() {
		return _tag.getName();
	}
	
	/**
	 * 
	 * @param tag
	 * @return new tag or null if the given tag was invalid
	 */
	public static FacebookPhotoTag getFacebookTag(Tag tag){
		if(tag == null){
			LOGGER.debug("null tag.");
			return null;
		}
		String name = tag.getName();
		if(StringUtils.isBlank(name)){
			LOGGER.debug("Invalid name.");
			return null;
		}
		return new FacebookPhotoTag(tag);
	}
	
	/**
	 * 
	 * @param object
	 * @return the object as a tag or null if null was passed
	 * @throws IllegalArgumentException on bad object
	 */
	public static FacebookPhotoTag getFacebookTag(MediaObject object) throws IllegalArgumentException {
		if(object == null){
			LOGGER.debug("null tag.");
			return null;
		}
		if(!MediaObjectType.KEYWORD.equals(object.getMediaObjectType())){
			throw new IllegalArgumentException("Invalid media object type: "+object.getMediaObjectTypeValue());
		}
		String value = object.getValue();
		if(StringUtils.isBlank(value) && StringUtils.isBlank((value = object.getName()))){
			throw new IllegalArgumentException("Invalid name and/or value for tag.");
		}
		FacebookPhotoTag tag = new FacebookPhotoTag();
		tag.setName(value);
		tag.setServiceType(object.getServiceType());
		return tag;
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
	 * @param name
	 * @see com.restfb.types.NamedFacebookType#setName(java.lang.String)
	 * @see #getName()
	 */
	public void setName(String name) {
		_tag.setName(name);
	}
}
