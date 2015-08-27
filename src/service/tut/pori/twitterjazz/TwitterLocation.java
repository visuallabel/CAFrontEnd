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
package service.tut.pori.twitterjazz;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import twitter4j.GeoLocation;


/**
 * A location received from Twitter.
 * 
 * <h2>Conditional Elements</h2>
 * <ul>
 *  <li>{@value service.tut.pori.twitterjazz.Definitions#ELEMENT_LATITUDE}</li>
 *  <li>{@value service.tut.pori.twitterjazz.Definitions#ELEMENT_LONGITUDE}</li>
 *  <li>{@value service.tut.pori.twitterjazz.Definitions#ELEMENT_NAME}</li>
 * </ul>
 * 
 * Any of the conditional fields can be omitted, but at least one of the fields must be present. If latitude is given, longitude must also be given, and vice versa.
 * 
 * <h3>XML Example</h3>
 * 
 * {@doc.restlet service="[service.tut.pori.twitterjazz.reference.Definitions#SERVICE_TJ_REFERENCE_EXAMPLE]" method="[service.tut.pori.twitterjazz.Definitions#ELEMENT_LOCATION]" type="GET" query="" body_uri=""}
 * 
 */
@XmlRootElement(name=Definitions.ELEMENT_LOCATION)
@XmlAccessorType(XmlAccessType.NONE)
public class TwitterLocation {
	private static final Logger LOGGER = Logger.getLogger(TwitterLocation.class);
	@XmlElement(name=Definitions.ELEMENT_NAME)
	private String _name = null;
	@XmlElement(name=Definitions.ELEMENT_LATITUDE)
	private Double _latitude = null;
	@XmlElement(name=Definitions.ELEMENT_LONGITUDE)
	private Double _longitude = null;
	
	/**
	 * 
	 * @param location
	 * @return the location or null if the given string was null or empty
	 */
	public static TwitterLocation getTwitterLocation(String location){
		if(StringUtils.isBlank(location)){
			LOGGER.debug("Bad location string: "+location);
			return null;
		}
		TwitterLocation tl = new TwitterLocation();
		tl._name = location;
		return tl;
	}
	
	/**
	 * 
	 * @param location
	 * @return the location or null if location was null
	 */
	public static TwitterLocation getTwitterLocation(GeoLocation location) {
		if(location == null){
			return null;
		}
		
		TwitterLocation tl = new TwitterLocation();
		tl._latitude = location.getLatitude();
		tl._longitude = location.getLongitude();
		return tl;
	}
	
	/**
	 * 
	 */
	public TwitterLocation() {
		// nothing needed
	}

	/**
	 * @return the name
	 * @see #setName(String)
	 */
	public String getName() {
		return _name;
	}

	/**
	 * @param name the name to set
	 * @see #getName()
	 */
	public void setName(String name) {
		_name = name;
	}

	/**
	 * @return the latitude
	 * @see #setLatitude(Double)
	 */
	public Double getLatitude() {
		return _latitude;
	}

	/**
	 * @param latitude the latitude to set
	 * @see #getLatitude()
	 */
	public void setLatitude(Double latitude) {
		_latitude = latitude;
	}

	/**
	 * @return the longitude
	 * @see #setLongitude(Double)
	 */
	public Double getLongitude() {
		return _longitude;
	}

	/**
	 * @param longitude the longitude to set
	 * @see #getLongitude()
	 */
	public void setLongitude(Double longitude) {
		_longitude = longitude;
	}
}
