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
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;


/**
 * A special photo list used to list similar photos.
 * 
 * <h3>XML Example</h3>
 * 
 * {@doc.restlet service="[service.tut.pori.contentanalysis.reference.Definitions#SERVICE_CA_REFERENCE_EXAMPLE]" method="[service.tut.pori.contentanalysis.Definitions#ELEMENT_SIMILAR_PHOTOLIST]" type="GET" query="" body_uri=""}
 *  
 */
@XmlRootElement(name=Definitions.ELEMENT_SIMILAR_PHOTOLIST)	// override root element name
@XmlAccessorType(XmlAccessType.NONE)
public class SimilarPhotoList extends PhotoList{
	private static final Logger LOGGER = Logger.getLogger(SimilarPhotoList.class);
	
	@Override
	protected boolean isValid() {
		if(isEmpty()){
			return false;
		}
		LOGGER.debug("Using "+SimilarPhotoList.class.toString()+" for validation of a photo list.");
		for(Photo p : getPhotos()){
			if(StringUtils.isBlank(p.getGUID())){
				return false;
			}
		}
		return true;
	}
}
