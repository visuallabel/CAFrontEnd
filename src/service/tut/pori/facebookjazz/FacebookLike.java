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
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.restfb.types.CategorizedFacebookType;

/**
 * A Facebook like.
 * 
 * <h3>XML Example</h3>
 * 
 * {@doc.restlet service="[service.tut.pori.facebookjazz.reference.Definitions#SERVICE_FBJ_REFERENCE_EXAMPLE]" method="[service.tut.pori.facebookjazz.Definitions#ELEMENT_LIKE]" type="GET" query="" body_uri=""}
 *
 * @see com.restfb.types.CategorizedFacebookType
 */
@XmlRootElement(name=Definitions.ELEMENT_LIKE)
@XmlAccessorType(XmlAccessType.NONE)
public class FacebookLike {
	private CategorizedFacebookType _facebookType = null;
	
	/**
	 * 
	 */
	public FacebookLike() {
		_facebookType = new CategorizedFacebookType();
	}
	
	/**
	 * 
	 * @param type
	 * @throws IllegalArgumentException
	 */
	public FacebookLike(CategorizedFacebookType type) throws IllegalArgumentException{
		if(type == null){
			throw new IllegalArgumentException("Invalid type.");
		}
		_facebookType = type;
	}

	/**
	 * @see com.restfb.types.CategorizedFacebookType#getCategory()
	 * 
	 * @return category
	 */
	@XmlElement(name = Definitions.ELEMENT_CATEGORY)
	public String getCategory() {
		return _facebookType.getCategory();
	}

	/**
	 * @see com.restfb.types.NamedFacebookType#getName()
	 * 
	 * @return name of the like
	 */
	@XmlElement(name = Definitions.ELEMENT_NAME)
	public String getName() {
		return _facebookType.getName(); 
	}
	
	/**
	 * 
	 * @param types
	 * @return the types wrapped to likes or null if null or empty list was passed
	 */
	public static List<FacebookLike> getFacebookLikes(List<CategorizedFacebookType> types){
		if(types == null || types.isEmpty()){
			return null;
		}
		List<FacebookLike> likes = new ArrayList<>(types.size());
		for(CategorizedFacebookType t : types){
			likes.add(new FacebookLike(t));
		}
		return likes;
	}

	/**
	 * @param name
	 * @see com.restfb.types.NamedFacebookType#setName(java.lang.String)
	 */
	public void setName(String name) {
		_facebookType.setName(name);
	}

	/**
	 * @param category
	 * @see com.restfb.types.CategorizedFacebookType#setCategory(java.lang.String)
	 */
	public void setCategory(String category) {
		_facebookType.setCategory(category);
	}
}
