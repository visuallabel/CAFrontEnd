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
import javax.xml.bind.annotation.XmlRootElement;

import service.tut.pori.contentanalysis.CAContentCore.ServiceType;
import service.tut.pori.contentanalysis.CAContentCore.Visibility;
import service.tut.pori.contentanalysis.Media;
import core.tut.pori.users.UserIdentity;
import core.tut.pori.utils.MediaUrlValidator.MediaType;

/**
 * Contains metadata information of a single video.
 * 
 * <h2>Optional elements</h2>
 * <ul>
 *  <li>{@value service.tut.pori.contentanalysis.Definitions#ELEMENT_BACKEND_STATUS_LIST}</li>
 *  <li>{@value service.tut.pori.contentanalysis.Definitions#ELEMENT_CONFIDENCE}</li>
 *  <li>{@value service.tut.pori.contentanalysis.Definitions#ELEMENT_CREDITS}</li>
 *  <li>{@value service.tut.pori.contentanalysis.Definitions#ELEMENT_DESCRIPTION}</li>
 *  <li>{@value service.tut.pori.contentanalysis.Definitions#ELEMENT_NAME}</li>
 *  <li>{@value service.tut.pori.contentanalysis.Definitions#ELEMENT_MEDIA_OBJECTLIST}</li>
 *  <li>{@value service.tut.pori.contentanalysis.Definitions#ELEMENT_VISIBILITY}</li>
 * </ul>
 * 
 * <h2>Back-end Video List elements</h2>
 * 
 * Certain elements are not applicable when Video is used with back-end responses. The following elements can be omitted in back-end responses:
 * <ul>
 *  <li>{@value service.tut.pori.contentanalysis.Definitions#ELEMENT_SERVICE_ID}</li>
 *  <li>{@value service.tut.pori.contentanalysis.Definitions#ELEMENT_URL}</li>
 *  <li>{@value core.tut.pori.users.Definitions#ELEMENT_USER_ID}</li>
 * </ul>
 * 
 * The elements will be ignored, and thus, will not cause errors.
 * 
 * <h3>XML Example</h3>
 * 
 * {@doc.restlet service="[service.tut.pori.contentanalysis.video.reference.Definitions#SERVICE_VCA_REFERENCE_EXAMPLE]" method="[service.tut.pori.contentanalysis.video.Definitions#ELEMENT_VIDEO]" type="GET" query="" body_uri=""}
 * 
 * @see service.tut.pori.contentanalysis.BackendStatusList
 * @see service.tut.pori.contentanalysis.MediaObjectList
 */
@XmlRootElement(name=Definitions.ELEMENT_VIDEO)
@XmlAccessorType(value=XmlAccessType.NONE)
public class Video extends Media {
	/** Default media type for Video */
	public static final MediaType MEDIA_TYPE = MediaType.VIDEO;

	/**
	 * If URL is not set, this will try to generate default redirect URL with serviceId, and GUID,
	 * finally, returns null if the required information is not available.
	 * 
	 * Default data groups: {@value core.tut.pori.http.parameters.DataGroups#DATA_GROUP_BASIC}, {@value core.tut.pori.http.parameters.DataGroups#DATA_GROUP_DEFAULTS}
	 * 
	 * @return URL
	 * @see #setUrl(String)
	 */
	@Override
	public String getUrl() {
		String url = super.getUrl();
		if(url != null){
			return url;
		}else{
			return VideoContentCore.generateRedirectUrl(getGUID(), getServiceType());
		}
	}

	/**
	 * for serialization, must be public for solr.
	 */
	public Video(){
		super.setMediaType(MEDIA_TYPE);
	}
	
	/**
	 * 
	 * @param guid
	 */
	public Video(String guid){
		super(guid);
		super.setMediaType(MEDIA_TYPE);
	}
	
	/**
	 * 
	 * @param guid
	 * @param ownerUserId
	 * @param serviceType
	 * @param visibility
	 */
	public Video(String guid, UserIdentity ownerUserId, ServiceType serviceType, Visibility visibility){
		super(guid, ownerUserId, serviceType, visibility);
		super.setMediaType(MEDIA_TYPE);
	}
	
	/**
	 * @param mediaType if null, the default media type will be used
	 * @throws IllegalArgumentException on bad media type, i.e. not the default media or null
	 * @see #MEDIA_TYPE
	 */
	@Override
	public void setMediaType(MediaType mediaType) throws IllegalArgumentException {
		if(mediaType != null && !MEDIA_TYPE.equals(mediaType)){ // the pre-defined media type cannot be changed, so only check if the value being set is valid
			throw new IllegalArgumentException("Invalid media type for video : "+mediaType.name());
		}
	}
}
