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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

/**
 * A list of media object time codes.
 * 
 * <h3>XML Example</h3>
 * 
 * {@doc.restlet service="[service.tut.pori.contentanalysis.video.reference.Definitions#SERVICE_VCA_REFERENCE_EXAMPLE]" method="[service.tut.pori.contentanalysis.video.Definitions#ELEMENT_TIMECODELIST]" type="GET" query="" body_uri=""}
 * 
 * @see service.tut.pori.contentanalysis.video.Timecode
 */
@XmlRootElement(name=Definitions.ELEMENT_TIMECODELIST)
@XmlAccessorType(value=XmlAccessType.NONE)
public class TimecodeList {
	private static final Logger LOGGER = Logger.getLogger(TimecodeList.class);
	private static final String SOLR_SEPARATOR = ";";
	@XmlElement(name = Definitions.ELEMENT_TIMECODE)
	private List<Timecode> _timecodes = null;

	/**
	 * @return the timecodes
	 * @see #setTimecodes(List)
	 */
	public List<Timecode> getTimecodes() {
		return _timecodes;
	}

	/**
	 * @param timecodes the timecodes to set
	 * @see #getTimecodes()
	 */
	public void setTimecodes(List<Timecode> timecodes) {
		_timecodes = timecodes;
	}
	
	/**
	 * Only for sub-classing, use the static.
	 * 
	 * @return true if the list contains no items
	 * @see #isEmpty(TimecodeList)
	 */
	protected boolean isEmpty() {
		return (_timecodes == null ? true : _timecodes.isEmpty());
	}
	
	/**
	 * Only for sub-classing, use the static.
	 * 
	 * @return true if the list contains only valid values
	 * @see #isValid(TimecodeList)
	 */
	protected boolean isValid() {
		if(isEmpty()){
			return false;
		}
		for(Timecode tc : _timecodes){
			if(!Timecode.isValid(tc)){
				return false;
			}
		}
		return true;
	}

	/**
	 * 
	 * @param timecodes
	 * @return true if the passed list is not null, not empty, and valid
	 */
	public static boolean isValid(TimecodeList timecodes) {
		return (timecodes == null ? false : timecodes.isValid());
	}
	
	/**
	 * 
	 * @param timecodes
	 * @return true if the passed list is null, empty or contains no items
	 */
	public static boolean isEmpty(TimecodeList timecodes) {
		return (timecodes == null ? true : timecodes.isEmpty());
	}
	
	/**
	 * 
	 * @param solrTimecodes if null or empty, this does nothing
	 * @return a populated timecode list
	 * @throws IllegalArgumentException on bad data
	 */
	public static TimecodeList populateTimecodes(List<String> solrTimecodes) throws IllegalArgumentException{
		if(solrTimecodes == null || solrTimecodes.isEmpty()){
			LOGGER.debug("No timecodes given.");
			return null;
		}
		List<Timecode> timecodes = new ArrayList<>(solrTimecodes.size());
		for(String solrTimecode : solrTimecodes){
			String codes[] = StringUtils.split(solrTimecode, SOLR_SEPARATOR);
			if(codes.length<2){
				LOGGER.warn("Invalid timecode ignored.");
				continue;
			}
			try{
				Timecode code = new Timecode();
				code.setStart(Double.valueOf(codes[0]));
				code.setEnd(Double.valueOf(codes[1]));
				timecodes.add(code);
			}catch(NumberFormatException ex){
				LOGGER.debug(ex,ex);
				throw new IllegalArgumentException("Malformed timecode data.");
			}
		}
		if(timecodes.isEmpty()){
			return null;
		}else{
			TimecodeList timecodeList = new TimecodeList();
			timecodeList.setTimecodes(timecodes);
			return timecodeList;
		}
	}

	/**
	 * 
	 * @param timecodes if null or empty this does nothing
	 * @return list of combined timecodes [{start;end}]
	 * @throws IllegalArgumentException on missing values
	 */
	public static List<String> getSolrTimecodes(TimecodeList timecodes) throws IllegalArgumentException{
		if(timecodes == null || timecodes.isEmpty()){
			LOGGER.debug("No timecodes given.");
			return null;
		}
		List<String> retval = new ArrayList<>(timecodes.getTimecodes().size());
		for(Timecode timecode : timecodes.getTimecodes()){
			if(timecode == null || timecode.getStart() == null || timecode.getEnd() == null){
				throw new IllegalArgumentException("Timecode data was missing.");
			}
			retval.add(timecode.getStart().toString()+SOLR_SEPARATOR+timecode.getEnd().toString());
		}
		return retval;
	}

	/**
	 * Add new timecode. This will NOT check for duplicates.
	 * 
	 * @param timecode
	 */
	public void addTimecode(Timecode timecode) {
		if(_timecodes == null){
			_timecodes = new ArrayList<>();
		}
		_timecodes.add(timecode);
	}
}
