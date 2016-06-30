package service.tut.pori.twitterjazz;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import service.tut.pori.contentanalysis.MediaObject;
import service.tut.pori.contentanalysis.CAContentCore.ServiceType;
import service.tut.pori.contentanalysis.MediaObject.MediaObjectType;

/**
 * Tag of a photo retrieved from Twitter.
 * 
 * <h2>Optional Elements</h2>
 * <ul>
 *  <li>{@value service.tut.pori.contentanalysis.Definitions#ELEMENT_SERVICE_ID}. Not present if the tag was created by the user or internally by the service.</li>
 * </ul>
 * 
 * <h3>XML Example</h3>
 * 
 * {@doc.restlet service="[service.tut.pori.twitterjazz.reference.Definitions#SERVICE_TJ_REFERENCE_EXAMPLE]" method="[service.tut.pori.twitterjazz.Definitions#ELEMENT_PHOTO_TAG]" type="GET" query="" body_uri=""}
 * 
 */
@XmlRootElement(name=Definitions.ELEMENT_PHOTO_TAG)
@XmlAccessorType(XmlAccessType.NONE)
public class TwitterPhotoTag {
	private static final Logger LOGGER = Logger.getLogger(TwitterPhotoTag.class);
	@XmlElement(name=service.tut.pori.contentanalysis.Definitions.ELEMENT_SERVICE_ID)
	private ServiceType _serviceType = null;
	@XmlElement(name=Definitions.ELEMENT_VALUE)
	private String _value = null;
	
	/**
	 * @return the serviceType
	 */
	public ServiceType getServiceType() {
		return _serviceType;
	}
	
	/**
	 * @param serviceType the serviceType to set
	 */
	public void setServiceType(ServiceType serviceType) {
		_serviceType = serviceType;
	}
	
	/**
	 * @return the value
	 */
	public String getValue() {
		return _value;
	}
	
	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		_value = value;
	}
	
	/**
	 * 
	 * @param object
	 * @return the object as a tag or null if null was passed
	 * @throws IllegalArgumentException on bad object
	 */
	public static TwitterPhotoTag getTwitterTag(MediaObject object) throws IllegalArgumentException {
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
		TwitterPhotoTag tag = new TwitterPhotoTag();
		tag._value = value;
		tag._serviceType = object.getServiceType();
		return tag;
	}
}
