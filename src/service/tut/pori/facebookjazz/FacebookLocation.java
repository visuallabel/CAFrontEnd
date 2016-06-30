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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.restfb.types.Location;
import com.restfb.types.NamedFacebookType;
import com.restfb.types.Place;
import com.restfb.types.Venue;

/**
 * Location retrieved from Facebook.
 * 
 * This class is used to map venue, namedfacebooktype and other zillion different kinds of facebook locations to a single object.
 * 
 * <h2>Conditional Elements</h2>
 * <ul>
 *  <li>{@value service.tut.pori.facebookjazz.Definitions#ELEMENT_CITY}</li>
 *  <li>{@value service.tut.pori.facebookjazz.Definitions#ELEMENT_COUNTRY}</li>
 *  <li>{@value service.tut.pori.facebookjazz.Definitions#ELEMENT_NAME}</li>
 *  <li>{@value service.tut.pori.facebookjazz.Definitions#ELEMENT_STATE}</li>
 * </ul>
 * 
 * Any of the conditional fields can be omitted, but at least one of the fields must be present.
 * 
 * <h3>XML Example</h3>
 * 
 * {@doc.restlet service="[service.tut.pori.facebookjazz.reference.Definitions#SERVICE_FBJ_REFERENCE_EXAMPLE]" method="[service.tut.pori.facebookjazz.Definitions#ELEMENT_LOCATION]" type="GET" query="" body_uri=""}
 *
 * @see com.restfb.types.Location
 */
@XmlRootElement(name=Definitions.ELEMENT_LOCATION)
@XmlAccessorType(XmlAccessType.NONE)
public class FacebookLocation {
	private static final Logger LOGGER = Logger.getLogger(FacebookLocation.class);
	private String _name = null;
	private Location _location = null;
	
	/**
	 * 
	 */
	public FacebookLocation(){
		_location = new Location();
	}
	
	/**
	 * 
	 * @param location
	 * @throws IllegalArgumentException
	 */
	public FacebookLocation(Location location) throws IllegalArgumentException{
		if(location == null){
			throw new IllegalArgumentException("Invalid location.");
		}
		_location = location;
	}
	
	/**
	 * 
	 * @param venue
	 * @return venue converted to a location
	 */
	public static FacebookLocation getFacebookLocation(Venue venue){
		if(venue == null){
			return null;
		}
		String country = venue.getCountry();
		String city = venue.getCity();
		String state = venue.getState();
		if(StringUtils.isBlank(city) && StringUtils.isBlank(country) && StringUtils.isBlank(state)){
			LOGGER.debug("Venue contained no valid details.");
			return null;
		}
		
		FacebookLocation l = new FacebookLocation();	
		l.setCountry(country);	
		l.setCity(city);	
		l.setState(state);
		return l;
	}

	/**
	 * 
	 * @param place
	 * @return place converted to a location
	 */
	public static FacebookLocation getFacebookLocation(Place place){
		if(place == null){
			return null;
		}
		FacebookLocation l = getFacebookLocation((NamedFacebookType)place);
		Location location = place.getLocation();
		boolean hadBaseDetails = true;
		if(l == null){ // there were no namedfacebooktype details
			if(location == null){   // and there are no location, nor any base details
				LOGGER.debug("Place contained no valid details.");
				return null;
			}else{   // no base details, but there is a location
				l = new FacebookLocation();
				hadBaseDetails = false;
			}       
		}else if(location == null){   // there are no location details, but there were name
			return l;
		}

		String country = location.getCountry();
		String city = location.getCity();
		String state = location.getState();
		
		if(!hadBaseDetails && StringUtils.isBlank(city) && StringUtils.isBlank(country) && StringUtils.isBlank(state)){
			LOGGER.debug("Place contained no valid details.");
			return null;
		}
		
		l.setCountry(country);	
		l.setCity(city);	
		l.setState(state);
		return l;
	}

	/**
	 * 
	 * @param name
	 * @return name converted to a location
	 */
	public static FacebookLocation getFacebookLocation(String name){
		if(StringUtils.isBlank(name)){
			return null;
		}else{
			FacebookLocation l = new FacebookLocation();
			l._name = name;
			return l;
		}
	}

	/**
	 * 
	 * @param type
	 * @return named type converted to a location
	 */
	public static FacebookLocation getFacebookLocation(NamedFacebookType type){
		if(type == null){
			return null;
		}
		FacebookLocation l = new FacebookLocation();
		l._name = type.getName();
		if(l._name == null){
			LOGGER.debug("Type contained no valid details.");
			return null;
		}else{
			return l;
		}
	}
	
	/**
	 * @see #getName()
	 * @see #setName(String)
	 * 
	 * @return this object stripped to named facebook type or null if no valid data is present. Note that the generated object may only be partial representation of this object, depending on the object data (only name member is used)
	 */
	public NamedFacebookType toNamedFacebookType(){
		String name = getName();
		if(StringUtils.isBlank(name)){
			LOGGER.warn("No name.");
			return null;
		}
		NamedFacebookType t = new NamedFacebookType();
		t.setName(name);
		return t;
	}

	/**
	 * @see com.restfb.types.Location#getCity()
	 * 
	 * @return city
	 * @see #setCity(String)
	 */
	@XmlElement(name = Definitions.ELEMENT_CITY)
	public String getCity() {
		return _location.getCity();  
	}

	/**
	 * 
	 * @return name
	 * @see #setName(String)
	 */
	@XmlElement(name = Definitions.ELEMENT_NAME)
	public String getName() {
		return _name;
	}

	/**
	 * @see com.restfb.types.Location#getState()
	 * 
	 * @return state
	 * @see #setState(String)
	 */
	@XmlElement(name = Definitions.ELEMENT_STATE)
	public String getState() {
		return _location.getState();
	}

	/**
	 * @see com.restfb.types.Location#getCountry()
	 * 
	 * @return country
	 * @see #setCountry(String)
	 */
	@XmlElement(name = Definitions.ELEMENT_COUNTRY)
	public String getCountry() {
		return _location.getCountry(); 
	}

	/**
	 * @param name the name to set
	 * @see #getName()
	 */
	public void setName(String name) {
		_name = name;
	}

	/**
	 * @param city
	 * @see com.restfb.types.Location#setCity(java.lang.String)
	 * @see #getCity()
	 */
	public void setCity(String city) {
		_location.setCity(city);
	}

	/**
	 * @param country
	 * @see com.restfb.types.Location#setCountry(java.lang.String)
	 * @see #getCountry()
	 */
	public void setCountry(String country) {
		_location.setCountry(country);
	}

	/**
	 * @param state
	 * @see com.restfb.types.Location#setState(java.lang.String)
	 * @see #getState()
	 */
	public void setState(String state) {
		_location.setState(state);
	}

	/**
	 * @see #setCountry(String)
	 * @see #getCountry()
	 * @see #getState()
	 * @see #setState(String)
	 * @see #getState()
	 * @see #setCity(String)
	 * @return this object stripped to place. Note that the generated object may only be partial representation of this object, depending on the object data (only country, city and state members are used)
	 */
	public Place toPlace() {
		Place p = new Place();
		p.setLocation(_location);
		return p;
	}

	/**
	 * @see #setCountry(String)
	 * @see #getCountry()
	 * @see #getState()
	 * @see #setState(String)
	 * @see #getState()
	 * @see #setCity(String)
	 * @return this object stripped to Venue or null if no valid data. Note that the generated object may only be partial representation of this object, depending on the object data (only country, city and state members are used)
	 */
	public Venue toVenue() {
		String country = getCountry();
		String city = getCity();
		String state = getState();
		if(StringUtils.isBlank(city) && StringUtils.isBlank(country) && StringUtils.isBlank(state)){
			LOGGER.debug("No country, city or state.");
			return null;
		}
		
		Venue v = new Venue();
		v.setCity(city);
		v.setCountry(country);
		v.setState(state);
		return v;
	}
}
