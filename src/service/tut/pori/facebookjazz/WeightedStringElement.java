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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

/**
 * An extension of an XML element with weight added as an attribute. The element content is String.
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class WeightedStringElement {
	@XmlValue
	private String _value = null;
	@XmlAttribute(name=Definitions.ATTRIBUTE_WEIGHT)
	private Integer _weight = null;
	
	/**
	 * for serialization
	 */
	@SuppressWarnings("unused")
	private WeightedStringElement(){
		// nothing needed
	}
	
	/**
	 * 
	 * @param value
	 * @param weight
	 */
	public WeightedStringElement(String value, Integer weight){
		_value = value;
		_weight = weight;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return _value;
	}

	/**
	 * @return the weight
	 */
	public Integer getWeight() {
		return _weight;
	}
}
