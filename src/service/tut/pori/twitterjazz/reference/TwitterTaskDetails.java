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
package service.tut.pori.twitterjazz.reference;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import service.tut.pori.contentanalysis.AbstractTaskDetails;
import service.tut.pori.contentanalysis.Definitions;
import service.tut.pori.contentanalysis.PhotoList;
import service.tut.pori.twitterjazz.TwitterProfile;

/**
 * A minimal implementation of TwitterTaskDetails used for testing
 */
@XmlRootElement(name=Definitions.ELEMENT_TASK_DETAILS)
@XmlAccessorType(XmlAccessType.NONE)
public final class TwitterTaskDetails extends AbstractTaskDetails {
	@XmlElement(name=Definitions.ELEMENT_PHOTOLIST)
	private PhotoList _photoList = null;
	@XmlElement(name = service.tut.pori.twitterjazz.Definitions.ELEMENT_TWITTER_PROFILE)
	private TwitterProfile _profile = null;

	/**
	 * @return the profile
	 */
	public TwitterProfile getProfile() {
		return _profile;
	}

	/**
	 * @param profile the profile to set
	 */
	public void setProfile(TwitterProfile profile) {
		_profile = profile;
	}

	/**
	 * @return the photoList
	 */
	public PhotoList getPhotoList() {
		return _photoList;
	}

	/**
	 * @param photoList the photoList to set
	 */
	public void setPhotoList(PhotoList photoList) {
		_photoList = photoList;
	}

	@Override
	public TaskParameters getTaskParameters() {
		return null;
	}

	@Override
	public void setTaskParameters(TaskParameters parameters) {
		throw new UnsupportedOperationException("Method not supported.");
	}
}
