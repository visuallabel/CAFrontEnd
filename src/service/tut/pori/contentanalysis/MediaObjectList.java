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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import core.tut.pori.http.ResponseData;
import core.tut.pori.utils.MediaUrlValidator.MediaType;
import service.tut.pori.contentanalysis.MediaObject.MediaObjectType;

/**
 * A list of media objects, which can be directly used as a data for XML response.
 * 
 * <h3>XML Example</h3>
 * 
 * {@doc.restlet service="[service.tut.pori.contentanalysis.reference.Definitions#SERVICE_CA_REFERENCE_EXAMPLE]" method="[service.tut.pori.contentanalysis.Definitions#ELEMENT_MEDIA_OBJECTLIST]" type="GET" query="" body_uri=""}
 * 
 * @see service.tut.pori.contentanalysis.MediaObject
 */
@XmlRootElement(name=Definitions.ELEMENT_MEDIA_OBJECTLIST)
@XmlAccessorType(XmlAccessType.NONE)
public class MediaObjectList extends ResponseData{
	private static final Logger LOGGER = Logger.getLogger(MediaObjectList.class);
	private static final String MEDIA_OBJECT_PARAMETER_DELIMITER = ":"; //like: OBJECT1_VALUE1:OBJECT1_VALUE2...
	@XmlElement(name = Definitions.ELEMENT_MEDIA_OBJECT)
	private List<MediaObject> _mediaObjects = null;
	@XmlElement(name = Definitions.ELEMENT_RESULT_INFO)
	private ResultInfo _resultInfo = null;

	/**
	 * @return list of photos
	 * @see #addMediaObject(MediaObject)
	 * @see #addMediaObjects(MediaObjectList)
	 */
	public List<MediaObject> getMediaObjects() {
		if(_mediaObjects != null && _mediaObjects.size() > 0){
			return _mediaObjects;
		}else{
			return null;
		}
	}

	/**
	 * Note: calling this method will clear the list's result info if any is present
	 * 
	 * @param mediaObject 
	 * @see #getMediaObjects()
	 */
	public void addMediaObject(MediaObject mediaObject){
		if(_mediaObjects == null){
			_mediaObjects = new ArrayList<>();
		}
		_mediaObjects.add(mediaObject);
		if(_resultInfo != null){ // if there was a result info, it may now be incorrect
			LOGGER.debug("Removing possibly incorrect result info.");
			_resultInfo = null;
		}
	}
	
	/**
	 * add the media object list to this media object list, empty list is ignored
	 * 
	 * Note: calling this method will clear the list's result info if any is present
	 * 
	 * @param mediaObjects
	 * @see #getMediaObjects()
	 */
	public void addMediaObjects(MediaObjectList mediaObjects){
		if(MediaObjectList.isEmpty(mediaObjects)){
			return;
		}
		if(_mediaObjects == null){
			_mediaObjects = new ArrayList<>();
		}
		_mediaObjects.addAll(mediaObjects.getMediaObjects());
		if(_resultInfo != null){ // if there was a result info, it may now be incorrect
			LOGGER.debug("Removing possibly incorrect result info.");
			_resultInfo = null;
		}
	}
	
	/**
	 * 
	 * @param mediaObjectId non-null id
	 * @return the media object or null if not found
	 */
	public MediaObject getMediaObject(String mediaObjectId){
		if(isEmpty()){
			return null;
		}
		for(Iterator<MediaObject> iter = _mediaObjects.iterator(); iter.hasNext();){
			MediaObject v = iter.next();
			if(mediaObjectId.equals(v.getMediaObjectId())){
				return v;
			}
		}
		return null;
	}
	
	/**
	 * 
	 * @return list of media object ids for this list or null if none
	 */
	public List<String> getMediaObjectIds(){
		if(isEmpty()){
			LOGGER.debug("Empty media object list.");
			return null;
		}
		List<String> voids = new ArrayList<>(_mediaObjects.size());
		for(MediaObject v : _mediaObjects){
			String mediaObjectId = v.getMediaObjectId();
			if(StringUtils.isBlank(mediaObjectId)){
				LOGGER.debug("Ignored media object without id.");
			}else{
				voids.add(mediaObjectId);
			}
		}
		return (voids.isEmpty() ? null : voids);
	}

	/**
	 * 
	 * @param list 
	 * @return true if this list contains no objects, or the list is null
	 */
	public static boolean isEmpty(MediaObjectList list){
		if(list == null){
			return true;
		}else{
			return list.isEmpty();
		}
	}
	 
	/**
	 * use the static, only for sub-classing
	 * @return true if the list is empty
	 * @see #isEmpty(MediaObjectList)
	 */
	protected boolean isEmpty(){
		return (_mediaObjects == null || _mediaObjects.isEmpty() ? true : false);
	}

	/**
	 * 
	 * @param objects
	 * @param resultInfo
	 * @return list or null if null or empty list is passed
	 */
	public static MediaObjectList getMediaObjectList(List<? extends MediaObject> objects, ResultInfo resultInfo){
		if(objects == null || objects.isEmpty()){
			LOGGER.debug("Ignored empty list.");
			return null;
		}else{
			MediaObjectList list = new MediaObjectList();
			list._mediaObjects = new ArrayList<>(objects);
			list._resultInfo = resultInfo;
			return list;
		}
	}

	/**
	 * Parse given keywords given in following format: 
	 * keyword:confidence:backendId,keyword:confidence:backendId,...<br/>
	 * <em>keyword</em> is required, <em>confidence</em> and <em>backendId</em> are optional.
	 * @param keywords
	 * @return list of media objects based on the keywords or null if none was found
	 */
	public static MediaObjectList getMediaObjectListFromKeywords(List<String> keywords){
		List<MediaObject> objects = null;
		if(keywords != null && !keywords.isEmpty()){
			objects = new ArrayList<>();
			for(Iterator<String> iter = keywords.iterator();iter.hasNext();){
				String[] keywordParts = iter.next().split(MEDIA_OBJECT_PARAMETER_DELIMITER);
				MediaObject o = new MediaObject(MediaType.PHOTO, MediaObjectType.KEYWORD);
				o.setValue(keywordParts[0]);
				if(keywordParts.length > 1){
					o.setConfidence(Double.valueOf(keywordParts[1]));
				}
				if(keywordParts.length > 2){
					o.setBackendId(Integer.valueOf(keywordParts[2]));
				}
				objects.add(o);
			}  // for
		}
		return getMediaObjectList(objects, null);
	}
	
	/**
	 * 
	 * @param list can be null
	 * @return true if the passed list is valid
	 */
	public static boolean isValid(MediaObjectList list){
		if(list == null){
			return false;
		}else{
			return list.isValid();
		}
	}
	
	/**
	 * use the static, only for sub-classing
	 * @return true if the list is valid
	 * @see #isValid(MediaObjectList)
	 */
	protected boolean isValid(){
		if(_mediaObjects == null){
			return false;
		}else{
			for(Iterator<MediaObject> iter = _mediaObjects.iterator();iter.hasNext();){
				if(!iter.next().isValid()){
					return false;
				}
			}
			return true;
		}
	}

	/**
	 * Default data groups: {@value service.tut.pori.contentanalysis.Definitions#DATA_GROUP_RESULT_INFO}
	 * 
	 * @return the resultInfo
	 * @see #setResultInfo(ResultInfo)
	 */
	public ResultInfo getResultInfo() {
		return _resultInfo;
	}

	/**
	 * @param resultInfo the resultInfo to set
	 * @see #getResultInfo()
	 */
	public void setResultInfo(ResultInfo resultInfo) {
		_resultInfo = resultInfo;
	}
}
