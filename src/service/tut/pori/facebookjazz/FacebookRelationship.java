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

import com.restfb.types.CategorizedFacebookType;
import com.restfb.types.User;


/**
 * Container for a relationship information retrieved from Facebook.
 * 
 * <h2>Optional Elements</h2>
 * <ul>
 *  <li>{@value service.tut.pori.facebookjazz.Definitions#ELEMENT_RELATIONSHIP_WITH}</li>
 * </ul>
 * 
 * <h3>XML Example</h3>
 * 
 * {@doc.restlet service="[service.tut.pori.facebookjazz.reference.Definitions#SERVICE_FBJ_REFERENCE_EXAMPLE]" method="[service.tut.pori.facebookjazz.Definitions#ELEMENT_RELATIONSHIP]" type="GET" query="" body_uri=""}
 *
 */
@XmlRootElement(name=Definitions.ELEMENT_RELATIONSHIP)
@XmlAccessorType(XmlAccessType.NONE)
public class FacebookRelationship {
	private static final Logger LOGGER = Logger.getLogger(FacebookRelationship.class);
	@XmlElement(name = Definitions.ELEMENT_RELATIONSHIP_WITH)
	private String _with = null;
	@XmlElement(name = Definitions.ELEMENT_RELATIONSHIP_TYPE)
	private String _type = null;


	/**
	 * 
	 * @param user
	 * @return facebook relationship or null if none
	 */
	public static FacebookRelationship getFacebookRelationShip(User user){
		String type = user.getRelationshipStatus();
		if(StringUtils.isBlank(type)){
			return null;
		}
		FacebookRelationship fr = new FacebookRelationship();
		fr._with = user.getSignificantOther().getName();
		fr._type = type;
		return fr;
	}
	
	/**
	 * set values from this relationship to the given user
	 * 
	 * @param user
	 */
	public void toUser(User user) {
		CategorizedFacebookType with = null;
		if(StringUtils.isBlank(_with)){
			LOGGER.warn("Invalid with.");
			user.setSignificantOther(null);
			user.setRelationshipStatus(null);
			return;
		}else{
			with = new CategorizedFacebookType();
			with.setName(_with);
		}
		user.setSignificantOther(with);
		user.setRelationshipStatus(_type);
	}

	/**
	 * The type is assigned by the content service (e.g. Facebook), and may (in practice) have enumerated values, but the element value should not be considered to be "enumerated".
	 * 
	 * @return type
	 * @see #setType(String)
	 */
	public String getType() {
		return _type;
	}

	/**
	 * 
	 * @return the other half
	 * @see #setWith(String)
	 */
	public String getWith() {
		return _with;
	}

	/**
	 * @param type the type to set
	 * @see #getWith()
	 */
	public void setType(String type) {
		_type = type;
	}

	/**
	 * @param with the with to set
	 * @see #getWith()
	 */
	public void setWith(String with) {
		_with = with;
	}
}
