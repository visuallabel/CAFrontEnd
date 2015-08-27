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
package service.tut.pori.contentanalysis;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.StringUtils;

/**
 * Defines a visual shape used in combination with a media object.
 * 
 * <h3>XML Example</h3>
 * 
 * {@doc.restlet service="[service.tut.pori.contentanalysis.reference.Definitions#SERVICE_CA_REFERENCE_EXAMPLE]" method="[service.tut.pori.contentanalysis.Definitions#ELEMENT_VISUAL_SHAPE]" type="GET" query="" body_uri=""}
 * 
 */
@XmlRootElement(name=Definitions.ELEMENT_VISUAL_SHAPE)
@XmlAccessorType(XmlAccessType.NONE)
public class VisualShape {
	private static final String VISUALSHAPETYPE_CIRCLE = "CIRCLE";
	private static final String VISUALSHAPETYPE_POLYGON = "POLYGON";
	private static final String VISUALSHAPETYPE_RECTANGLE = "RECTANGLE";
	private static final String VISUALSHAPETYPE_TRIANGLE = "TRIANGLE";
	@XmlElement(name = Definitions.ELEMENT_VISUAL_SHAPE_TYPE)
	private VisualShapeType _type = null;
	@XmlElement(name = Definitions.ELEMENT_VALUE)
	private String _value = null;

	/**
	 * Type of the shape object.
	 */
	@XmlEnum
	public enum VisualShapeType{
		/** Rectangle bounding box. Valid value format is: "topLeftX,topLeftY,bottomRightX,bottomRightY", where all values are positive integers detonating pixel positions in the photo. */
		@XmlEnumValue(value=VISUALSHAPETYPE_RECTANGLE)
		RECTANGLE(1),
		/** Triangle bounding area. Valid value format is: "topX,topY,bottomLeftX,bottomLeftY,bottomRightX,bottomRightY", where all values are positive integers detonating pixel positions in the photo. */
		@XmlEnumValue(value=VISUALSHAPETYPE_TRIANGLE)
		TRIANGLE(2),
		/** Cricle bounding area. Valid value format is: "centerX,centerY,radius", where all values are positive integers detonating pixel positions in the photo. */
		@XmlEnumValue(value=VISUALSHAPETYPE_CIRCLE)
		CIRCLE(3),
		/** Polygon bounding area. Valid value format is: "1X,1Y,2X,2Y...nX,nY", where all values are positive integers detonating pixel positions in the photo. There should be at least 3 coordinates for a valid polygon, though it is recommended to use {@link service.tut.pori.contentanalysis.VisualShape.VisualShapeType#TRIANGLE} and {@link service.tut.pori.contentanalysis.VisualShape.VisualShapeType#RECTANGLE} for simpler objects.*/
		@XmlEnumValue(value=VISUALSHAPETYPE_POLYGON)
		POLYGON(4);

		private int _shapeType;

		/**
		 * 
		 * @param shapeType
		 */
		private VisualShapeType(int shapeType){
			_shapeType = shapeType;
		}

		/**
		 * 
		 * @return the type id
		 */
		public final int toShapeTypeId(){
			return _shapeType;
		}

		/**
		 * 
		 * @param shapeType
		 * @return the passed value as shape type
		 * @throws IllegalArgumentException on bad input
		 */
		public static VisualShapeType fromShapeTypeId(int shapeType) throws IllegalArgumentException{
			for(VisualShapeType e : VisualShapeType.values()){
				if(e._shapeType == shapeType){
					return e;
				}
			}
			throw new IllegalArgumentException("Bad "+VisualShapeType.class.toString()+" : "+shapeType);
		}
	}  // enum VisualShapeType


	/**
	 * 
	 * @param type
	 * @param value
	 * 
	 */
	public VisualShape(VisualShapeType type, String value){
		_type = type;
		_value = value;
	}

	/**
	 * 
	 * @return value for the shape. The value differs per shape type.
	 * @see #setValue(String)
	 * @see VisualShapeType
	 */
	public String getValue() {
		return _value;
	}

	/**
	 * 
	 * @param value
	 * @see #getValue()
	 */
	public void setValue(String value) {
		_value = value;
	}

	/**
	 * 
	 * @return type
	 * @see #setVisualShapeType(VisualShapeType)
	 */
	public VisualShapeType getVisualShapeType() {
		return _type;
	}

	/**
	 * 
	 * @param type
	 * @see #getVisualShapeType()
	 */
	public void setVisualShapeType(VisualShapeType type) {
		_type = type;
	}

	/**
	 * for serialization
	 */
	protected VisualShape(){
		// nothing needed
	}

	/**
	 * 
	 * @param shape can be null
	 * @return true if the passed type is valid
	 */
	public static boolean isValid(VisualShape shape){
		if(shape == null){
			return false;
		}else{
			return shape.isValid();
		}
	}

	/**
	 * use the static, only for sub-classing
	 * @return true if the shape is valid
	 */
	protected boolean isValid(){
		if(StringUtils.isBlank(_value) || _type == null){
			return false;
		}else{
			return true;
		}
	}

	/**
	 * 
	 * @param shape
	 * @return the value or null if none or the given shape was null
	 */
	public static String getValue(VisualShape shape){
		return (shape == null ? null : shape.getValue());
	}

	/**
	 * Sets the given value to the given Shape. If shape is null, new shape will be returned (with the value).
	 * If value is blank and the given shape is null, null will be returned.
	 * 
	 * @param shape can be null
	 * @param value
	 * @return the passed shape, new object or null
	 */
	public static VisualShape setValue(VisualShape shape, String value){
		if(StringUtils.isBlank(value)){
			if(shape == null){	// no value, no pre-existing shape given
				return null;
			}
		}else if(shape == null){
			shape = new VisualShape();
		}
		shape.setValue(value);
		return shape;
	}
	
	/**
	 * 
	 * @param shape
	 * @return the shape type or null if shape was null or no type given for the shape
	 */
	public static Integer getVisualShapeTypeId(VisualShape shape){
		if(shape == null){
			return null;
		}
		VisualShapeType type = shape.getVisualShapeType();
		return (type == null ? null : type.toShapeTypeId());
	}
	
	/**
	 * Sets the given value to the given Shape. If shape is null, new shape will be returned (with the value).
	 * 
	 * @param shape can be null
	 * @param value
	 * @return the passed shape, new object or null
	 * @throws IllegalArgumentException on bad value
	 */
	public static VisualShape setVisualShapeTypeId(VisualShape shape, Integer value) throws IllegalArgumentException{
		if(value == null){
			throw new IllegalArgumentException("Value cannot be null.");
		}else if(shape == null){
			shape = new VisualShape();
		}
		shape.setVisualShapeType(VisualShapeType.fromShapeTypeId(value));
		return shape;
	}

	/**
	 * Classes implementing this interface will contain functionality for handling an VisualShape.
	 * 
	 * Remember to annotate the implemented setter methods if you wish to use them with Solr (\@Field annotation).
	 * 
	 * When implementing the methods, the static methods in VisualShapeCapable class may come handy.
	 *
	 */
	public interface VisualShapeSolrCapable{
		/**
		 * 
		 * @param type
		 * @throws IllegalArgumentException on bad type value
		 * @see #getVisualShapeTypeId()
		 */
		public void setVisualShapeTypeId(Integer type) throws IllegalArgumentException;
		
		/**
		 * 
		 * @return shape type id
		 * @see #getVisualShapeTypeId()
		 */
		public Integer getVisualShapeTypeId();
		
		/**
		 * 
		 * @param value
		 * @see #getVisualShapeValue()
		 */
		public void setVisualShapeValue(String value);
		
		/**
		 * 
		 * @return shape value
		 * @see #setVisualShapeValue(String)
		 */
		public String getVisualShapeValue();
	} // interface VisualShapeSolrCapable
}
