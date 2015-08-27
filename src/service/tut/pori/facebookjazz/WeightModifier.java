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
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A user defined weight modifier.
 * 
 * <h3>XML Example</h3>
 * 
 * {@doc.restlet service="[service.tut.pori.facebookjazz.reference.Definitions#SERVICE_FBJ_REFERENCE_EXAMPLE]" method="[service.tut.pori.facebookjazz.Definitions#ELEMENT_WEIGHT_MODIFIER]" type="GET" query="" body_uri=""}
 * 
 */
@XmlRootElement(name=Definitions.ELEMENT_WEIGHT_MODIFIER)
@XmlAccessorType(XmlAccessType.NONE)
public class WeightModifier {
	@XmlElement(name=Definitions.ELEMENT_VALUE)
	private Integer _value = null;
	@XmlElement(name=Definitions.ELEMENT_WEIGHT_MODIFIER_TYPE)
	private WeightModifierType _type = null;
	
	/**
	 * content weight type
	 * 
	 */
	@XmlEnum
	public enum WeightModifierType{
		/** 
		 * Facebook group name 
		 * 
		 * @see service.tut.pori.facebookjazz.FacebookGroup#getWName()
		 */
		GROUP__NAME(1),
		/** 
		 * Facebook group description 
		 * 
		 * @see service.tut.pori.facebookjazz.FacebookGroup#getWDescription()
		 */
		GROUP__DESCRIPTION(2),
		/**
		 * Facebook status message
		 * 
		 * @see service.tut.pori.facebookjazz.FacebookStatusMessage#getWMessage()
		 */
		STATUS_MESSAGE__MESSAGE(3),
		/**
		 * Facebook status message comments
		 * 
		 * @see service.tut.pori.facebookjazz.FacebookStatusMessage#getMessageComments()
		 * @see service.tut.pori.facebookjazz.FacebookComment#getWMessage()
		 */
		STATUS_MESSAGE__COMMENT_MESSAGE(4),
		/**
		 * Facebook photo description
		 * 
		 * @see service.tut.pori.facebookjazz.FacebookPhotoDescription#getWDescription()
		 */
		PHOTO_DESCRIPTION__DESCRIPTION(5),
		/**
		 * Facebook photo description comments
		 * 
		 * @see service.tut.pori.facebookjazz.FacebookPhotoDescription#getDescriptionComments()
		 * @see service.tut.pori.facebookjazz.FacebookComment#getWMessage()
		 */
		PHOTO_DESCRIPTION__COMMENT_MESSAGE(6),
		/**
		 * Facebook video description
		 * 
		 * @see service.tut.pori.facebookjazz.FacebookVideoDescription#getWDescription()
		 */
		VIDEO_DESCRIPTION__DESCRIPTION(7),
		/**
		 * Facebook video description comments
		 * 
		 * @see service.tut.pori.facebookjazz.FacebookVideoDescription#getDescriptionComments()
		 * @see service.tut.pori.facebookjazz.FacebookComment#getWMessage()
		 */
		VIDEO_DESCRIPTION__COMMENT_MESSAGE(8),
		/**
		 * Facebook event name
		 * 
		 * @see service.tut.pori.facebookjazz.FacebookEvent#getWName()
		 */
		EVENT__NAME(9),
		/**
		 * Facebook event description
		 * 
		 * @see service.tut.pori.facebookjazz.FacebookEvent#getWDescription()
		 */
		EVENT__DESCRIPTION(10);
	
		private int _value;
		
		/**
		 * 
		 * @param value
		 */
		private WeightModifierType(int value){
			_value = value;
		}
		
		/**
		 * 
		 * @return the modifier as integer
		 */
		public int toInt(){
			return _value;
		}
		
		/**
		 * 
		 * @param value
		 * @return the value converted to modifier type
		 * @throws IllegalArgumentException
		 */
		public static WeightModifierType fromInt(int value) throws IllegalArgumentException{
			for(WeightModifierType t : values()){
				if(t._value == value){
					return t;
				}
			}
			throw new IllegalArgumentException("Bad "+WeightModifierType.class.toString()+" : "+value);
		}
	} //enum ContentWeightType

	/**
	 * @return the value
	 * @see #setValue(Integer)
	 */
	public Integer getValue() {
		return _value;
	}

	/**
	 * @param value the value to set
	 * @see #getValue()
	 */
	public void setValue(Integer value) {
		_value = value;
	}

	/**
	 * @return the type
	 * @see #setType(WeightModifierType)
	 */
	public WeightModifierType getType() {
		return _type;
	}

	/**
	 * @param type the type to set
	 * @see #getType()
	 */
	public void setType(WeightModifierType type) {
		_type = type;
	}
	
	/**
	 * for sub-classing, use the static
	 * 
	 * @return true if the modifier is valid
	 * @see #isValid(WeightModifier)
	 */
	protected boolean isValid(){
		if(_type == null || _value == null){
			return false;
		}else{
			return true;
		}
	}
	
	/**
	 * 
	 * @param modifier
	 * @return false if modifier is null, contains invalid modifiers
	 */
	public static boolean isValid(WeightModifier modifier){
		return (modifier == null ? false : modifier.isValid());
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_type == null) ? 0 : _type.hashCode());
		result = prime * result + ((_value == null) ? 0 : _value.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		WeightModifier other = (WeightModifier) obj;
		if (_type != other._type)
			return false;
		if (_value == null) {
			if (other._value != null)
				return false;
		} else if (!_value.equals(other._value))
			return false;
		return true;
	}
	
	/**
	 * for serialization
	 */
	@SuppressWarnings("unused")
	private WeightModifier(){
		// nothing needed
	}
	
	/**
	 * 
	 * @param type
	 * @param value
	 */
	public WeightModifier(WeightModifierType type, Integer value){
		_type = type;
		_value = value;
	}
}
