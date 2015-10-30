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
package service.tut.pori.contentstorage.reference;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.ArrayUtils;

import service.tut.pori.contentstorage.Definitions;
import service.tut.pori.contentstorage.MediaList;
import core.tut.pori.http.ResponseData;

/**
 * Response example
 * 
 */
@XmlRootElement(name=service.tut.pori.contentstorage.reference.Definitions.ELEMENT_EXAMPLE)
@XmlAccessorType(XmlAccessType.NONE)
public class Example extends ResponseData {
	@XmlElement(name = Definitions.ELEMENT_MEDIALIST)
	private MediaList _media = null;

	/**
	 * @return the media
	 */
	public MediaList getMediaList() {
		return _media;
	}

	/**
	 * @param media the media to set
	 */
	public void setMediaList(MediaList media) {
		_media = media;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<?>[] getDataClasses() {
		if(_media == null){
			return super.getDataClasses();
		}
		return ArrayUtils.addAll(_media.getDataClasses(), getClass());
	}
}
