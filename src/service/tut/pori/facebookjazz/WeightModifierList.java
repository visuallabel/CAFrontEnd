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

import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import core.tut.pori.http.ResponseData;
import service.tut.pori.facebookjazz.WeightModifier.WeightModifierType;

/**
 * The list of weight modifiers. This class can be directly used in a Response.
 * 
 * <h3>XML Example</h3>
 * 
 * {@doc.restlet service="[service.tut.pori.facebookjazz.reference.Definitions#SERVICE_FBJ_REFERENCE_EXAMPLE]" method="[service.tut.pori.facebookjazz.Definitions#ELEMENT_WEIGHT_MODIFIER_LIST]" type="GET" query="" body_uri=""}
 * 
 * @see service.tut.pori.facebookjazz.WeightModifier
 */
@XmlRootElement(name=Definitions.ELEMENT_WEIGHT_MODIFIER_LIST)
@XmlAccessorType(XmlAccessType.NONE)
public class WeightModifierList extends ResponseData {
	@XmlElement(name=Definitions.ELEMENT_WEIGHT_MODIFIER)
	private Set<WeightModifier> _modifiers = null;

	/**
	 * for sub-classing, use the static
	 * 
	 * @return true if the list is valid
	 * @see #isValid(WeightModifierList)
	 */
	protected boolean isValid(){
		if(isEmpty()){
			return false;
		}
		for(WeightModifier wm : _modifiers){
			if(!WeightModifier.isValid(wm)){
				return false;
			}
		}
		return true;
	}
	
	/**
	 * for sub-classing, use the static
	 * 
	 * @return true if the list is empty
	 * @see #isEmpty(WeightModifierList)
	 */
	protected boolean isEmpty(){
		return (_modifiers == null ? true : _modifiers.isEmpty());
	}
	
	/**
	 * 
	 * @param list
	 * @return false if list is null, empty or contains invalid modifiers
	 */
	public static boolean isValid(WeightModifierList list){
		return (list == null ? false : list.isValid());
	}
	
	/**
	 * 
	 * @param type
	 * @return the modifier value for the given type or null if not found
	 * @see #setWeightModifier(WeightModifier)
	 */
	public Integer getModifier(WeightModifierType type){
		if(type == null || isEmpty()){
			return null;
		}
		for(WeightModifier wm : _modifiers){
			if(type.equals(wm.getType())){
				return wm.getValue();
			}
		}
		return null;
	}
	
	/**
	 * 
	 * @param list
	 * @return true if list is null or empty
	 */
	public static boolean isEmpty(WeightModifierList list){
		return (list == null ? true : list.isEmpty());
	}

	/**
	 * 
	 * @param weightModifier
	 * @see #getModifier(service.tut.pori.facebookjazz.WeightModifier.WeightModifierType)
	 */
	public void setWeightModifier(WeightModifier weightModifier) {
		if(weightModifier == null){
			return;
		}
		if(_modifiers == null){
			_modifiers = new HashSet<>();
		}
		_modifiers.add(weightModifier);
	}

	/**
	 * 
	 * @return the modifiers
	 * @see #setWeightModifier(WeightModifier)
	 */
	public Set<WeightModifier> getModifiers() {
		return _modifiers;
	}
}
