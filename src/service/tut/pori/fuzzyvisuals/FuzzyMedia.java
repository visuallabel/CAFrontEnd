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
package service.tut.pori.fuzzyvisuals;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import core.tut.pori.utils.MediaUrlValidator.MediaType;
import service.tut.pori.contentanalysis.MediaObject;
import service.tut.pori.contentanalysis.MediaObjectList;

/**
 * Minimal implementation of Media for Fuzzy Visuals
 * 
 */
@XmlRootElement(name=Definitions.ELEMENT_MEDIA)
@XmlAccessorType(XmlAccessType.NONE)
public class FuzzyMedia extends service.tut.pori.contentanalysis.Media {
	
	/**
	 *  for serialization
	 */
	public FuzzyMedia(){
		// nothing needed
	}
	
	/**
	 * 
	 * @param guid
	 * @param mediaObjects
	 * @param mediaType
	 */
	public FuzzyMedia(String guid, List<MediaObject> mediaObjects, MediaType mediaType){
		super.setGUID(guid);
		super.setMediaType(mediaType);
		super.setMediaObjects(MediaObjectList.getMediaObjectList(mediaObjects, null));
	}
}
