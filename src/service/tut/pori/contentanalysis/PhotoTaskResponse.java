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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class for representing a response received from a back-end to a previously submitted analysis task.
 * 
 * <h3>XML Example</h3>
 * 
 * {@doc.restlet service="[service.tut.pori.contentanalysis.reference.Definitions#SERVICE_CA_REFERENCE_EXAMPLE]" method="[service.tut.pori.contentanalysis.Definitions#ELEMENT_TASK_RESULTS]" type="GET" query="" body_uri=""}
 * 
 * @see service.tut.pori.contentanalysis.PhotoList
 */
@XmlRootElement(name=Definitions.ELEMENT_TASK_RESULTS)
@XmlAccessorType(XmlAccessType.NONE)
public class PhotoTaskResponse extends TaskResponse{
	@XmlElement(name = Definitions.ELEMENT_PHOTOLIST)
	private PhotoList _photoList = null;
	
	/**
	 * @return the response photo list
	 * @see #setPhotoList(PhotoList)
	 */
	public PhotoList getPhotoList() {
		return _photoList;
	}
	
	/**
	 * @param photoList the data to set
	 * @see #getPhotoList()
	 */
	public void setPhotoList(PhotoList photoList) {
		_photoList = photoList;
	}
}
