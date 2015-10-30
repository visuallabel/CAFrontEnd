/**
 * Copyright 2015 Tampere University of Technology, Pori Department
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

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import service.tut.pori.contentanalysis.AbstractTaskDetails.TaskParameters;

/**
 * Analysis parameters for an analysis task.
 * 
 * <h2>Optional Elements</h2>
 * <ul>
 *  <li>{@value service.tut.pori.contentanalysis.Definitions#ELEMENT_ANALYSIS_TYPELIST}. If not given, the defaults {@link service.tut.pori.contentanalysis.PhotoParameters.AnalysisType#KEYWORD_EXTRACTION} and {@link service.tut.pori.contentanalysis.PhotoParameters.AnalysisType#VISUAL} should be used.</li>
 * </ul>
 * 
 * <h3>XML Example</h3>
 * 
 * {@doc.restlet service="[service.tut.pori.contentanalysis.reference.Definitions#SERVICE_CA_REFERENCE_EXAMPLE]" method="[service.tut.pori.contentanalysis.Definitions#ELEMENT_TASK_PARAMETERS]" type="GET" query="" body_uri=""}
 * 
 */
@XmlRootElement(name=Definitions.ELEMENT_TASK_PARAMETERS)
@XmlAccessorType(value=XmlAccessType.NONE)
public final class PhotoParameters extends TaskParameters {
	private static final String ANALYSIS_TYPE_AUDIO = "AUDIO";
	private static final String ANALYSIS_TYPE_FACE_DETECTION = "FACE_DETECTION";
	private static final String ANALYSIS_TYPE_KEYWORD_EXTRACTION = "KEYWORD_EXTRACTION";
	private static final String ANALYSIS_TYPE_VISUAL = "VISUAL";
	@XmlElement(name = Definitions.ELEMENT_ANALYSIS_TYPE)
	@XmlElementWrapper(name = Definitions.ELEMENT_ANALYSIS_TYPELIST)
	private Set<AnalysisType> _analysisTypes = null;

	/**
	 * The analysis type for the provided content.
	 *
	 */
	@XmlEnum
	public enum AnalysisType{
		/**
		 * Audio content should be analyzed.
		 */
		@XmlEnumValue(value = ANALYSIS_TYPE_AUDIO)
		AUDIO,
		/**
		 * Face detection should be performed for the task contents.
		 */
		@XmlEnumValue(value = ANALYSIS_TYPE_FACE_DETECTION)
		FACE_DETECTION,
		/**
		 * The content should be analyzed for keywords.
		 */
		@XmlEnumValue(value = ANALYSIS_TYPE_KEYWORD_EXTRACTION)
		KEYWORD_EXTRACTION,
		/**
		 * Visual content (photos, videos) should be analyzed.
		 */
		@XmlEnumValue(value = ANALYSIS_TYPE_VISUAL)
		VISUAL;
		
		/**
		 * 
		 * @return this enumeration as analysis type string
		 */
		public String toAnalysisTypeString(){
			switch(this){
				case AUDIO:
					return ANALYSIS_TYPE_AUDIO;
				case FACE_DETECTION:
					return ANALYSIS_TYPE_FACE_DETECTION;
				case KEYWORD_EXTRACTION:
					return ANALYSIS_TYPE_KEYWORD_EXTRACTION;
				case VISUAL:
					return ANALYSIS_TYPE_VISUAL;
				default:
					throw new UnsupportedOperationException("Unhandeled "+AnalysisType.class.toString()+" : "+name());
			}
		}
		
		/**
		 * 
		 * @param value
		 * @return the given value converted to analysis type
		 * @throws IllegalArgumentException on bad value
		 */
		public static AnalysisType fromAnalysisTypeString(String value) throws IllegalArgumentException {
			if(!StringUtils.isBlank(value)){
				for(AnalysisType t : AnalysisType.values()){
					if(t.toAnalysisTypeString().equalsIgnoreCase(value)){
						return t;
					}
				}
			}
			throw new IllegalArgumentException("Invalid analysis type value : "+value);
		}

		/**
		 * 
		 * @param values
		 * @return the values converted to analysis types or null if empty or null value collection was passed
		 * @throws IllegalArgumentException on invalid value
		 */
		public static EnumSet<AnalysisType> fromAnalysisTypeString(Collection<String> values) throws IllegalArgumentException {
			if(values == null || values.isEmpty()){
				return null;
			}
			EnumSet<AnalysisType> types = EnumSet.noneOf(AnalysisType.class);
			for(String value : values){
				types.add(fromAnalysisTypeString(value));
			}
			return types;
		}
	} // enum AnalysisType

	/**
	 * @return the analysisTypes
	 * @see #setAnalysisTypes(Set)
	 */
	public Set<AnalysisType> getAnalysisTypes() {
		return _analysisTypes;
	}

	/**
	 * @param analysisTypes the analysisTypes to set
	 * @see #getAnalysisTypes()
	 */
	public void setAnalysisTypes(Set<AnalysisType> analysisTypes) {
		_analysisTypes = analysisTypes;
	}

	@Override
	public void initialize(Map<String, String> metadata) throws IllegalArgumentException {
		_analysisTypes = null;
		if(metadata != null && !metadata.isEmpty()){
			String[] analysisTypes = StringUtils.split(metadata.get(Definitions.ELEMENT_ANALYSIS_TYPELIST), core.tut.pori.http.Definitions.SEPARATOR_URI_QUERY_PARAM_VALUES);
			if(!ArrayUtils.isEmpty(analysisTypes)){
				_analysisTypes = EnumSet.of(AnalysisType.fromAnalysisTypeString(analysisTypes[0]));
				for(int i=1;i<analysisTypes.length;++i){
					_analysisTypes.add(AnalysisType.fromAnalysisTypeString(analysisTypes[i]));
				}
			}
		}
	}

	@Override
	public HashMap<String, String> toMetadata() {
		if(_analysisTypes == null || _analysisTypes.isEmpty()){
			return null;
		}else{
			HashMap<String, String> metadata = new HashMap<>(1);
			Iterator<AnalysisType> typeIter = _analysisTypes.iterator();
			if(_analysisTypes.size() == 1){
				metadata.put(Definitions.ELEMENT_ANALYSIS_TYPELIST, typeIter.next().toAnalysisTypeString());
			}else{
				StringBuilder tb = new StringBuilder(typeIter.next().toAnalysisTypeString());
				while(typeIter.hasNext()){
					tb.append(core.tut.pori.http.Definitions.SEPARATOR_URI_QUERY_PARAM_VALUES);
					tb.append(typeIter.next().toAnalysisTypeString());
				}
				metadata.put(Definitions.ELEMENT_ANALYSIS_TYPELIST, tb.toString());
			}
			return metadata;
		}
	}
}
