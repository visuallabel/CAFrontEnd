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
package service.tut.pori.contentstorage.reference;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import service.tut.pori.contentanalysis.CAContentCore.ServiceType;
import service.tut.pori.contentanalysis.CAContentCore.Visibility;
import service.tut.pori.contentanalysis.Media;
import service.tut.pori.contentanalysis.Photo;
import service.tut.pori.contentanalysis.reference.CAXMLObjectCreator;
import service.tut.pori.contentanalysis.video.Video;
import service.tut.pori.contentstorage.Definitions;
import service.tut.pori.contentstorage.MediaList;
import service.tut.pori.contentstorage.URLContentStorage;
import core.tut.pori.http.parameters.Limits;
import core.tut.pori.users.UserIdentity;

/**
 * Class that can be used to created example objects/object lists.
 */
public class ContentXMLObjectCreator {
	private CAXMLObjectCreator _CACreator = null;

	/**
	 * 
	 * @param seed for random generator, or null to use default (system time in nanoseconds)
	 */
	public ContentXMLObjectCreator(Long seed){
		if(seed == null){
			seed = System.nanoTime();
		}
		_CACreator = new CAXMLObjectCreator(seed);
	}

	/**
	 * @return randomly generated user identity
	 * @see service.tut.pori.contentanalysis.reference.CAXMLObjectCreator#createUserIdentity()
	 */
	public UserIdentity createUserIdentity() {
		return _CACreator.createUserIdentity();
	}
	
	/**
	 * 
	 * @param limits 
	 * @param serviceTypes
	 * @param userId
	 * @return randomly generated service types
	 */
	public MediaList createMediaList(Limits limits, EnumSet<ServiceType> serviceTypes, UserIdentity userId){
		int count = limits.getMaxItems(Definitions.ELEMENT_MEDIALIST);
		List<Media> media = new ArrayList<>(count);
		Random r = _CACreator.getRandom();
		for(int i=0;i<count;++i){
			Media m = null;
			if(r.nextBoolean()){
				m = new Photo();
			}else{
				m = new Video();
			}
			m.setGUID(UUID.randomUUID().toString());
			m.setOwnerUserId(userId);
			m.setServiceType(URLContentStorage.SERVICE_TYPE);
			m.setVisibility(Visibility.PUBLIC);
			media.add(m);
		}

		MediaList mediaList = new MediaList();
		mediaList.setMedia(media);
		return mediaList;
	}
}
