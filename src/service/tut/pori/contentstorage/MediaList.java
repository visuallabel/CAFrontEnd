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
package service.tut.pori.contentstorage;

import java.util.HashSet;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

import service.tut.pori.contentanalysis.Media;
import core.tut.pori.http.ResponseData;

/**
 * 
 * This is a container class for media elements, which can be used directly with Response to provide XML output.
 * 
 * <h3>XML Example</h3>
 * 
 * {@doc.restlet service="[service.tut.pori.contentstorage.reference.Definitions#SERVICE_COS_REFERENCE_EXAMPLE]" method="[service.tut.pori.contentstorage.Definitions#ELEMENT_MEDIALIST]" type="GET" query="" body_uri=""}
 *  
 * @see service.tut.pori.contentanalysis.Media
 */
@XmlRootElement(name = Definitions.ELEMENT_MEDIALIST)
@XmlAccessorType(value=XmlAccessType.NONE)
public class MediaList extends ResponseData {
	@XmlElementRef
	private List<Media> _media = null;

	/**
	 * @return the media
	 */
	public List<Media> getMedia() {
		return _media;
	}

	/**
	 * @param media the media to set
	 */
	public void setMedia(List<Media> media) {
		_media = media;
	}
	
	/**
	 * only for sub-classing
	 * @return true if media contains no items
	 * @see #isEmpty(MediaList)
	 */
	protected boolean isEmpty() {
		return (_media == null || _media.isEmpty());
	}

	/**
	 * 
	 * @param media
	 * @return true if the media is null or contains no items
	 */
	public static boolean isEmpty(MediaList media) {
		return (media == null || media.isEmpty());
	}

	@Override
	public Class<?>[] getDataClasses() {
		if(isEmpty()){
			return super.getDataClasses();
		}
		
		HashSet<Class<?>> classes = new HashSet<>();
		classes.add(getClass());
		for(Media m : _media){
			classes.add(m.getClass());
		}
		return classes.toArray(new Class<?>[classes.size()]);
	}
}
