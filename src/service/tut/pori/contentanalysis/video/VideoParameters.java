/**
 * Copyright 2015 Tampere University of Technology, Pori Unit
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
package service.tut.pori.contentanalysis.video;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
import service.tut.pori.contentanalysis.PhotoParameters.AnalysisType;

/**
 * Options for video analysis.
 * 
 * <h2>Conditional Elements</h2>
 * <ul>
 *  <li>{@value service.tut.pori.contentanalysis.video.Definitions#ELEMENT_SEQUENCE_DURATION}</li>
 * </ul>
 * 
 * If sequence type {@link service.tut.pori.contentanalysis.video.VideoParameters.SequenceType#SECOND} is used, the element {@value service.tut.pori.contentanalysis.video.Definitions#ELEMENT_SEQUENCE_DURATION} must be present. 
 * 
 * <h2>Optional Elements</h2>
 * <ul>
 *  <li>{@value service.tut.pori.contentanalysis.Definitions#ELEMENT_ANALYSIS_TYPELIST}. If not given, the defaults {@link service.tut.pori.contentanalysis.PhotoParameters.AnalysisType#KEYWORD_EXTRACTION} and {@link service.tut.pori.contentanalysis.PhotoParameters.AnalysisType#VISUAL} should be used.</li>
 *  <li>{@value service.tut.pori.contentanalysis.video.Definitions#ELEMENT_TIMECODELIST}. Can be used to specify which parts of the video should be analysed.</li>
 * </ul>
 * 
 * <h3>XML Example</h3>
 * 
 * {@doc.restlet service="[service.tut.pori.contentanalysis.video.reference.Definitions#SERVICE_VCA_REFERENCE_EXAMPLE]" method="[service.tut.pori.contentanalysis.Definitions#ELEMENT_TASK_PARAMETERS]" type="GET" query="" body_uri=""}
 * 
 * @see service.tut.pori.contentanalysis.video.TimecodeList
 */
@XmlRootElement(name=service.tut.pori.contentanalysis.Definitions.ELEMENT_TASK_PARAMETERS)
@XmlAccessorType(value=XmlAccessType.NONE)
public final class VideoParameters extends TaskParameters {
	private static final String SEQUENCE_TYPE_FRAME = "FRAME";
	private static final String SEQUENCE_TYPE_FULL = "FULL";
	private static final String SEQUENCE_TYPE_SECOND = "SECOND";
	private static final String SEQUENCE_TYPE_SHOT  = "SHOT";
	@XmlElement(name = service.tut.pori.contentanalysis.Definitions.ELEMENT_ANALYSIS_TYPE)
	@XmlElementWrapper(name = service.tut.pori.contentanalysis.Definitions.ELEMENT_ANALYSIS_TYPELIST)
	private Set<AnalysisType> _analysisTypes = null;
	@XmlElement(name = Definitions.ELEMENT_SEQUENCE_DURATION)
	private Integer _sequenceDuration = null;
	@XmlElement(name = Definitions.ELEMENT_SEQUENCE_TYPE)
	private SequenceType _sequenceType = null;
	@XmlElement(name = Definitions.ELEMENT_TIMECODELIST)
	private TimecodeList _timecodes = null;
	
	/**
	 * Type of the requested analysis sequence
	 * 
	 */
	@XmlEnum
	public enum SequenceType {
		/** Analysis is based on each individual frame */
		@XmlEnumValue(value=SEQUENCE_TYPE_FRAME)
		FRAME(0),
		/** 
		 * Analysis is based on timed intervals, reported in seconds. 
		 * 
		 * This can be used to define the accuracy of the analysis.
		 * 
		 * For example, sequence duration of 5 would mean that the analysis should be performed in 5 second blocks, i.e. there would be one keyword per 5 seconds of video (if any are found).
		 * 
		 * @see service.tut.pori.contentanalysis.video.VideoParameters#getSequenceDuration()
		 */
		@XmlEnumValue(value=SEQUENCE_TYPE_SECOND)
		SECOND(1),
		/** Analysis is based on back-end detected shots. I.e. variable length sequences. The generated keywords may or may not have timecodes, depending on the back-ends decision. */
		@XmlEnumValue(value=SEQUENCE_TYPE_SHOT)
		SHOT(2),
		/** The back-end should analyze the entire video, and only report the detected tags without any timecode information. */
		@XmlEnumValue(value=SEQUENCE_TYPE_FULL)
		FULL(3);
		
		private int _value;
		
		/**
		 * 
		 * @param value
		 */
		private SequenceType(int value){
			_value = value;
		}
		
		/**
		 * 
		 * @return the sequence as integer value
		 */
		public int toInt(){
			return _value;
		}
		
		/**
		 * 
		 * @param value
		 * @return the value converted to SequenceType
		 * @throws IllegalArgumentException on bad value
		 */
		public static SequenceType fromInt(int value) throws IllegalArgumentException {
			for(SequenceType t : SequenceType.values()){
				if(t._value == value){
					return t;
				}
			}
			throw new IllegalArgumentException("Bad "+SequenceType.class.toString()+" : "+value);
		}
		
		/**
		 * 
		 * @return this type as sequence type string
		 */
		public String toSequenceTypeString(){
			switch(this){
				case FRAME:
					return SEQUENCE_TYPE_FRAME;
				case FULL:
					return SEQUENCE_TYPE_FULL;
				case SECOND:
					return SEQUENCE_TYPE_SECOND;
				case SHOT:
					return SEQUENCE_TYPE_SHOT;
				default:
					throw new UnsupportedOperationException("Unhandeled "+SequenceType.class.toString()+" : "+name());
			}
		}
		
		/**
		 * 
		 * @param value
		 * @return the value converted to sequence type
		 * @throws IllegalArgumentException on bad value
		 */
		public static SequenceType fromSequenceTypeString(String value) throws IllegalArgumentException {
			if(!StringUtils.isBlank(value)){
				for(SequenceType t : SequenceType.values()){
					if(t.toSequenceTypeString().equalsIgnoreCase(value)){
						return t;
					}
				}
			}
			throw new IllegalArgumentException("Invalid sequence type : "+value);
		}
	} // enum SequenceType

	/**
	 * @return the sequence duration in seconds or null if none set
	 * @see #setSequenceDuration(Integer)
	 */
	public Integer getSequenceDuration() {
		return _sequenceDuration;
	}

	/**
	 * @param sequenceDuration the sequence duration in seconds
	 * @see #getSequenceDuration()
	 */
	public void setSequenceDuration(Integer sequenceDuration) {
		_sequenceDuration = sequenceDuration;
	}

	/**
	 * @return the sequenceType
	 * @see #setSequenceType(service.tut.pori.contentanalysis.video.VideoParameters.SequenceType)
	 */
	public SequenceType getSequenceType() {
		return _sequenceType;
	}

	/**
	 * @param sequenceType the sequenceType to set
	 * @see #getSequenceType()
	 */
	public void setSequenceType(SequenceType sequenceType) {
		_sequenceType = sequenceType;
	}

	/**
	 * @return the timecodes
	 * @see #setTimecodes(TimecodeList)
	 */
	public TimecodeList getTimecodes() {
		return _timecodes;
	}

	/**
	 * @param timecodes the timecodes to set
	 * @see #getTimecodes()
	 */
	public void setTimecodes(TimecodeList timecodes) {
		_timecodes = timecodes;
	}

	@Override
	public void initialize(Map<String, String> metadata) throws IllegalArgumentException {
		_analysisTypes = null;
		_timecodes = null;
		_sequenceDuration = null;
		_sequenceType = null;
		if(metadata != null && !metadata.isEmpty()){
			String[] timecodesStart = StringUtils.split(metadata.get(Definitions.ELEMENT_TIMECODE_START), core.tut.pori.http.Definitions.SEPARATOR_URI_QUERY_PARAM_VALUES);
			String[] timecodesEnd = StringUtils.split(metadata.get(Definitions.ELEMENT_TIMECODE_END), core.tut.pori.http.Definitions.SEPARATOR_URI_QUERY_PARAM_VALUES);
			int startLength = ArrayUtils.getLength(timecodesStart);
			if(startLength != ArrayUtils.getLength(timecodesEnd)){
				throw new IllegalArgumentException(Definitions.ELEMENT_TIMECODE_START+" does not match "+Definitions.ELEMENT_TIMECODE_END+".");
			}
			if(startLength > 0){
				_timecodes = new TimecodeList();
				for(int i=0;i<startLength;++i){
					_timecodes.addTimecode(new Timecode(Double.valueOf(timecodesStart[i]), Double.valueOf(timecodesEnd[i])));
				}
			}
			
			String temp = metadata.get(Definitions.ELEMENT_SEQUENCE_TYPE);
			if(!StringUtils.isBlank(temp)){
				_sequenceType = SequenceType.fromSequenceTypeString(temp);
			}
			
			temp = metadata.get(Definitions.ELEMENT_SEQUENCE_DURATION);
			if(!StringUtils.isBlank(temp)){
				_sequenceDuration = Integer.valueOf(temp);
			}
			
			String[] analysisTypes = StringUtils.split(metadata.get(service.tut.pori.contentanalysis.Definitions.ELEMENT_ANALYSIS_TYPELIST), core.tut.pori.http.Definitions.SEPARATOR_URI_QUERY_PARAM_VALUES);
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
		HashMap<String, String> map = new HashMap<>(4);
		
		if(_sequenceDuration != null){
			map.put(Definitions.ELEMENT_SEQUENCE_DURATION, _sequenceDuration.toString());
		}
		
		if(_sequenceType != null){
			map.put(Definitions.ELEMENT_SEQUENCE_TYPE, _sequenceType.toSequenceTypeString());
		}
		
		if(!TimecodeList.isEmpty(_timecodes)){
			List<Timecode> timecodes = _timecodes.getTimecodes();
			Iterator<Timecode> tcIter = timecodes.iterator();
			Timecode tc = tcIter.next();
			if(timecodes.size() == 1){
				map.put(Definitions.ELEMENT_TIMECODE_START, tc.getStart().toString()); // let it throw null pointer on bad value
				map.put(Definitions.ELEMENT_TIMECODE_END, tc.getEnd().toString()); // let it throw null pointer on bad value
			}else{
				StringBuilder start = new StringBuilder(tc.getStart().toString()); // let it throw null pointer on bad value
				StringBuilder end = new StringBuilder(tc.getEnd().toString()); // let it throw null pointer on bad value
				while(tcIter.hasNext()){
					start.append(core.tut.pori.http.Definitions.SEPARATOR_URI_QUERY_PARAM_VALUES);
					end.append(core.tut.pori.http.Definitions.SEPARATOR_URI_QUERY_PARAM_VALUES);
					tc = tcIter.next();
					start.append(tc.getStart().toString()); // let it throw null pointer on bad value
					end.append(tc.getEnd().toString()); // let it throw null pointer on bad value
				}
				map.put(Definitions.ELEMENT_TIMECODE_START, start.toString());
				map.put(Definitions.ELEMENT_TIMECODE_END, end.toString());
			}
		}
		
		if(_analysisTypes != null && !_analysisTypes.isEmpty()){
			Iterator<AnalysisType> typeIter = _analysisTypes.iterator();
			if(_analysisTypes.size() == 1){
				map.put(service.tut.pori.contentanalysis.Definitions.ELEMENT_ANALYSIS_TYPELIST, typeIter.next().toAnalysisTypeString());
			}else{
				StringBuilder tb = new StringBuilder(typeIter.next().toAnalysisTypeString());
				while(typeIter.hasNext()){
					tb.append(core.tut.pori.http.Definitions.SEPARATOR_URI_QUERY_PARAM_VALUES);
					tb.append(typeIter.next().toAnalysisTypeString());
				}
				map.put(service.tut.pori.contentanalysis.Definitions.ELEMENT_ANALYSIS_TYPELIST, tb.toString());
			}
		}
		
		return (map.isEmpty() ? null : map);
	}

	/**
	 * @return the analysisTypes
	 */
	public Set<AnalysisType> getAnalysisTypes() {
		return _analysisTypes;
	}

	/**
	 * @param analysisTypes the analysisTypes to set
	 */
	public void setAnalysisTypes(Set<AnalysisType> analysisTypes) {
		_analysisTypes = analysisTypes;
	}
}
