/**
 * Copyright 2014 Tampere University of Technology, Pori Department
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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.log4j.Logger;

import core.tut.pori.http.ResponseData;

/**
 * 
 * This is a container class for photo elements, which can be used directly with Response to provide XML output.
 * 
 * <h3>XML Example</h3>
 * 
 * {@doc.restlet service="[service.tut.pori.contentanalysis.reference.Definitions#SERVICE_CA_REFERENCE_EXAMPLE]" method="[service.tut.pori.contentanalysis.Definitions#ELEMENT_PHOTOLIST]" type="GET" query="" body_uri=""}
 *  
 * @see service.tut.pori.contentanalysis.Photo
 * @see service.tut.pori.contentanalysis.ResultInfo
 */
@XmlRootElement(name=Definitions.ELEMENT_PHOTOLIST)
@XmlAccessorType(value=XmlAccessType.NONE)
public class PhotoList extends ResponseData{
	private static final Logger LOGGER = Logger.getLogger(PhotoList.class);
	@XmlElement(name = Definitions.ELEMENT_PHOTO)
	private List<Photo> _photos = null;
	@XmlElement(name = Definitions.ELEMENT_RESULT_INFO)
	private ResultInfo _resultInfo = null;

	/**
	 * @return list of photos
	 * @see #setPhotos(List)
	 */  
	public List<Photo> getPhotos() {
		if(_photos == null || _photos.isEmpty()){
			return null;
		}else{
			return _photos;
		}
	}
	
	/**
	 * 
	 * @return list of GUIDs in this list or null if the list contains no GUIDs.
	 */
	public List<String> getGUIDs(){
		return getGUIDs(this);
	}
	
	/**
	 * 
	 * @param photoList
	 * @return GUIDs contained in the given photo list or null if none. Photos without GUIDs will be ignored.
	 */
	public static List<String> getGUIDs(PhotoList photoList){
		if(isEmpty(photoList)){
			LOGGER.debug("Empty photo list.");
			return null;
		}else{
			List<Photo> photos = photoList.getPhotos();
			List<String> guids = new ArrayList<>(photos.size());
			for(Iterator<Photo> iter = photos.iterator(); iter.hasNext();){
				String guid = iter.next().getGUID();
				if(guid == null){
					LOGGER.debug("Skipped photo without GUID.");
				}else{
					guids.add(guid);
				}
			}
			return (guids.isEmpty() ? null : guids);
		}
	}

	/**
	 * 
	 * @return resultInfo or null if not available
	 * @see #setResultInfo(ResultInfo)
	 */
	public ResultInfo getResultInfo(){
		return _resultInfo;
	}

	/**
	 * 
	 * @param resultInfo 
	 * @see #getResultInfo()
	 */
	public void setResultInfo(ResultInfo resultInfo){
		_resultInfo = resultInfo;
	}

	/**
	 * 
	 * replace the currently set set of photos with a new set of photos
	 * @param photos 
	 * @see #getPhotos()
	 */
	public void setPhotos(List<Photo> photos) {
		_photos = photos;
	}

	/**
	 * 
	 * @param photo 
	 * @see #getPhotos()
	 */
	public void addPhoto(Photo photo) {
		if(_photos == null){
			_photos = new ArrayList<>();
		}
		_photos.add(photo);
	}
	
	/**
	 * 
	 * @param photos
	 * @see #getPhotos()
	 */
	public void addPhotos(PhotoList photos){
		if(PhotoList.isEmpty(photos)){
			LOGGER.debug("Ignored empty photo list.");
			return;
		}
		if(_photos == null){
			_photos = new ArrayList<>(photos.getPhotos());
		}else{
			_photos.addAll(photos.getPhotos());
		}
	}

	/**
	 * 
	 * NOTE: to use getPhotoList factory functions, the nullary constructor of the inherited class MUST be available
	 */
	public PhotoList(){
		// nothing needed
	}
	
	/**
	 * 
	 * @param guid
	 * @return photo with the given guid or null if not in the list
	 */
	public Photo getPhoto(String guid){
		if(isEmpty()){
			return null;
		}
		if(guid == null){
			LOGGER.debug("Ignored null GUID.");
			return null;
		}
		for(Iterator<Photo> iter = _photos.iterator(); iter.hasNext();){
			Photo p = iter.next();
			if(guid.equals(p.getGUID())){
				return p;
			}
		}
		return null;
	}

	/**
	 * 
	 * @param photos
	 * @param resultInfo optional resultInfo
	 * @return new photo list of null if null or empty photos given
	 */
	public static PhotoList getPhotoList(Collection<Photo> photos, ResultInfo resultInfo){
		if(photos == null || photos.isEmpty()){
			return null;
		}
		
		PhotoList photoList = new PhotoList();
		photoList.setPhotos(new ArrayList<>(photos));
		photoList.setResultInfo(resultInfo);
		return photoList;
	}

	/**
	 * @param list
	 * @return true if the given list is empty or null
	 */
	public static boolean isEmpty(PhotoList list){
		if(list == null || list.isEmpty()){
			return true;
		}else{
			return false;
		}
	}

	/**
	 * use the static, only for sub-classing
	 * @return true if this list is empty
	 * @see #isEmpty(PhotoList)
	 */
	protected boolean isEmpty(){
		if(_photos == null || _photos.isEmpty()){
			return true;
		}else{
			return false;
		}
	}

	/**
	 * 
	 * @param list can be null
	 * @return true if the given list is valid
	 */
	public static boolean isValid(PhotoList list){
		if(list == null){
			return false;
		}else{
			return list.isValid();
		}
	}

	/**
	 * use the static, only for sub-classing
	 * @return true if this list is valid
	 * @see #isValid(PhotoList)
	 */
	protected boolean isValid(){
		if(isEmpty()){
			return false;
		}else{
			for(Iterator<Photo> iter = _photos.iterator();iter.hasNext();){
				if(!Photo.isValid(iter.next())){
					return false;
				}
			}
			return true;
		}
	}
	
	/**
	 * use the static, only for sub-classing
	 * @return number of photos in the list
	 * @see #count(PhotoList)
	 */
	protected int count(){
		return (_photos == null ? 0 : _photos.size());
	}
	
	/**
	 * 
	 * @param list
	 * @return number of photos in the list or 0 if null or empty list passed
	 */
	public static int count(PhotoList list){
		return (list == null ? 0 : list.count());
	}
}
