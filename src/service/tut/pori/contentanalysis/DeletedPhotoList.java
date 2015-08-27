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
import java.util.Collection;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;


/**
 * A special photo list used to list deleted photos.
 * 
 * Deleted Photo List element is generally found in the Feedback Task. It is used to notify back-ends that the photo or a list of photos is no longer available.
 * 
 * Note that the information in the Photo object is provided only for reference, and the details provided are not guaranteed to valid. Moreover, the Photo object is not guaranteed to be valid. The list is generally only meant to provide the UIDs of the deleted photos; other information may or may not be present.
 *
 * <h3>XML Example</h3>
 * 
 * {@doc.restlet service="[service.tut.pori.contentanalysis.reference.Definitions#SERVICE_CA_REFERENCE_EXAMPLE]" method="[service.tut.pori.contentanalysis.Definitions#ELEMENT_DELETED_PHOTOLIST]" type="GET" query="" body_uri=""}
 *  
 */
@XmlRootElement(name=Definitions.ELEMENT_DELETED_PHOTOLIST)	// override root element name
@XmlAccessorType(XmlAccessType.NONE)
public class DeletedPhotoList extends PhotoList{
	private static final Logger LOGGER = Logger.getLogger(DeletedPhotoList.class);
	
	/**
	 * 
	 * @param photos
	 * @param resultInfo optional resultInfo
	 * @return new photo list of null if null or empty photos given
	 */
	public static DeletedPhotoList getPhotoList(Collection<Photo> photos, ResultInfo resultInfo){
		if(photos == null || photos.isEmpty()){
			return null;
		}
		
		DeletedPhotoList photoList = new DeletedPhotoList();
		photoList.setPhotos(new ArrayList<>(photos));
		photoList.setResultInfo(resultInfo);
		return photoList;
	}

	@Override
	protected boolean isValid() {
		if(isEmpty()){
			return false;
		}
		LOGGER.debug("Using "+DeletedPhotoList.class.toString()+" for validation of a photo list.");
		for(Photo p : getPhotos()){
			if(StringUtils.isBlank(p.getGUID())){
				return false;
			}
		}
		return true;
	}
}
