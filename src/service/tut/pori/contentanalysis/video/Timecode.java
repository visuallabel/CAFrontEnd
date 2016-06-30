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
package service.tut.pori.contentanalysis.video;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A media object time code. If start is null and end is not null, end is used for start, and vice versa.
 * 
 * <h3>XML Example</h3>
 * 
 * {@doc.restlet service="[service.tut.pori.contentanalysis.video.reference.Definitions#SERVICE_VCA_REFERENCE_EXAMPLE]" method="[service.tut.pori.contentanalysis.video.Definitions#ELEMENT_TIMECODE]" type="GET" query="" body_uri=""}
 * 
 */
@XmlRootElement(name=Definitions.ELEMENT_TIMECODE)
@XmlAccessorType(value=XmlAccessType.NONE)
public class Timecode {
	private Double _start = null;
	private Double _end = null;

	/**
	 * only for sub-classing, use the static.
	 * 
	 * @return true if the timecode is valid
	 * @see #isValid(Timecode)
	 */
	protected boolean isValid(){
		return (_start != null || _end != null);
	}
	
	/**
	 * 
	 * @param timecode
	 * @return true if the time code is valid and not null
	 */
	public static boolean isValid(Timecode timecode){
		return (timecode == null ? false : timecode.isValid());
	}

	/**
	 * @return the start
	 * @see #setStart(Double)
	 */
	@XmlElement(name = Definitions.ELEMENT_TIMECODE_START)
	public Double getStart() {
		return (_start == null ? _end : _start);
	}

	/**
	 * @param start the start to set
	 * @see #getStart()
	 */
	public void setStart(Double start) {
		_start = start;
	}

	/**
	 * @return the end
	 * @see #setEnd(Double)
	 */
	@XmlElement(name = Definitions.ELEMENT_TIMECODE_END)
	public Double getEnd() {
		return (_end == null ? _start : _end);
	}

	/**
	 * @param end the end to set
	 * @see #getEnd()
	 */
	public void setEnd(Double end) {
		_end = end;
	}
	
	/**
	 * 
	 */
	public Timecode(){
		// nothing needed
	}
	
	/**
	 * 
	 * @param start
	 * @param end
	 */
	public Timecode(Double start, Double end){
		_start = start;
		_end = end;
	}
}
