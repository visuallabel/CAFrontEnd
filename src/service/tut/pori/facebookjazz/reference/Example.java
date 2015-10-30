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
package service.tut.pori.facebookjazz.reference;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import service.tut.pori.contentanalysis.AbstractTaskDetails;
import service.tut.pori.facebookjazz.Definitions;
import service.tut.pori.facebookjazz.FBFeedbackTaskDetails;
import service.tut.pori.facebookjazz.FBSummarizationTaskDetails;
import service.tut.pori.facebookjazz.FBTaskResponse;
import service.tut.pori.facebookjazz.FacebookComment;
import service.tut.pori.facebookjazz.FacebookEvent;
import service.tut.pori.facebookjazz.FacebookGroup;
import service.tut.pori.facebookjazz.FacebookLike;
import service.tut.pori.facebookjazz.FacebookLocation;
import service.tut.pori.facebookjazz.FacebookPhotoDescription;
import service.tut.pori.facebookjazz.FacebookPhotoTag;
import service.tut.pori.facebookjazz.FacebookProfile;
import service.tut.pori.facebookjazz.FacebookRelationship;
import service.tut.pori.facebookjazz.FacebookStatusMessage;
import service.tut.pori.facebookjazz.FacebookUserDetails;
import service.tut.pori.facebookjazz.FacebookVideoDescription;
import service.tut.pori.facebookjazz.WeightModifier;
import service.tut.pori.facebookjazz.WeightModifierList;
import core.tut.pori.http.ResponseData;

/**
 * Response example
 * 
 */
@XmlRootElement(name=service.tut.pori.facebookjazz.reference.Definitions.ELEMENT_EXAMPLE)
@XmlAccessorType(XmlAccessType.NONE)
public class Example extends ResponseData {
	@XmlElement(name=Definitions.ELEMENT_COMMENT)
	private FacebookComment _facebookComment = null;
	@XmlElement(name=Definitions.ELEMENT_EVENT)
	private FacebookEvent _facebookEvent = null;
	@XmlElement(name=Definitions.ELEMENT_GROUP)
	private FacebookGroup _facebookGroup = null;
	@XmlElement(name=Definitions.ELEMENT_LIKE)
	private FacebookLike _facebookLike = null;
	@XmlElement(name=Definitions.ELEMENT_LOCATION)
	private FacebookLocation _facebookLocation = null;
	@XmlElement(name=Definitions.ELEMENT_PHOTO_DESCRIPTION)
	private FacebookPhotoDescription _facebookPhotoDescription = null;
	@XmlElement(name=Definitions.ELEMENT_FACEBOOK_PROFILE)
	private FacebookProfile _facebookProfile = null;
	@XmlElement(name=Definitions.ELEMENT_PHOTO_TAG)
	private FacebookPhotoTag _facebookPhotoTag = null;
	@XmlElement(name=Definitions.ELEMENT_RELATIONSHIP)
	private FacebookRelationship _facebookRelationship = null;
	@XmlElement(name=Definitions.ELEMENT_STATUS_MESSAGE)
	private FacebookStatusMessage _facebookStatusMessage = null;
	@XmlElement(name=Definitions.ELEMENT_USER_DETAILS)
	private FacebookUserDetails _facebookUserDetails = null;
	@XmlElement(name=Definitions.ELEMENT_VIDEO_DESCRIPTION)
	private FacebookVideoDescription _facebookVideoDescription = null;
	@XmlElement(name=Definitions.ELEMENT_WEIGHT_MODIFIER)
	private WeightModifier _weightModifier = null;
	@XmlElement(name=Definitions.ELEMENT_WEIGHT_MODIFIER_LIST)
	private WeightModifierList _weightModifierList = null;
	@XmlElement(name=service.tut.pori.contentanalysis.Definitions.ELEMENT_TASK_DETAILS)
	private AbstractTaskDetails _taskDetails = null;
	@XmlElement(name=service.tut.pori.contentanalysis.Definitions.ELEMENT_TASK_RESULTS)
	private FBTaskResponse _taskResponse = null;

	/**
	 * @return the weightModifierList
	 */
	public WeightModifierList getWeightModifierList() {
		return _weightModifierList;
	}

	/**
	 * @param weightModifierList the weightModifierList to set
	 */
	public void setWeightModifierList(WeightModifierList weightModifierList) {
		_weightModifierList = weightModifierList;
	}

	/**
	 * @return the taskDetails
	 */
	public AbstractTaskDetails getTaskDetails() {
		return _taskDetails;
	}

	/**
	 * @param taskDetails the fbSummarizationTaskDetails to set
	 */
	public void setTaskDetails(FBSummarizationTaskDetails taskDetails) {
		_taskDetails = taskDetails;
	}
	
	/**
	 * @param taskDetails the FBFeedbackTaskDetails to set
	 */
	public void setTaskDetails(FBFeedbackTaskDetails taskDetails) {
		_taskDetails = taskDetails;
	}
	
	/**
	 * @return the taskResponse
	 */
	public FBTaskResponse getTaskResponse() {
		return _taskResponse;
	}

	/**
	 * @param taskResponse the taskResponse to set
	 */
	public void setTaskResponse(FBTaskResponse taskResponse) {
		_taskResponse = taskResponse;
	}

	/**
	 * @return the facebookPhotoTag
	 */
	public FacebookPhotoTag getFacebookPhotoTag() {
		return _facebookPhotoTag;
	}

	/**
	 * @param facebookPhotoTag the facebookPhotoTag to set
	 */
	public void setFacebookPhotoTag(FacebookPhotoTag facebookPhotoTag) {
		_facebookPhotoTag = facebookPhotoTag;
	}

	/**
	 * @return the weightModifier
	 */
	public WeightModifier getWeightModifier() {
		return _weightModifier;
	}

	/**
	 * @param weightModifier the weightModifier to set
	 */
	public void setWeightModifier(WeightModifier weightModifier) {
		_weightModifier = weightModifier;
	}

	/**
	 * @return the facebookComment
	 */
	public FacebookComment getFacebookComment() {
		return _facebookComment;
	}

	/**
	 * @param facebookComment the facebookComment to set
	 */
	public void setFacebookComment(FacebookComment facebookComment) {
		_facebookComment = facebookComment;
	}

	/**
	 * @return the facebookEvent
	 */
	public FacebookEvent getFacebookEvent() {
		return _facebookEvent;
	}

	/**
	 * @param facebookEvent the facebookEvent to set
	 */
	public void setFacebookEvent(FacebookEvent facebookEvent) {
		_facebookEvent = facebookEvent;
	}

	/**
	 * @return the facebookGroup
	 */
	public FacebookGroup getFacebookGroup() {
		return _facebookGroup;
	}

	/**
	 * @param facebookGroup the facebookGroup to set
	 */
	public void setFacebookGroup(FacebookGroup facebookGroup) {
		_facebookGroup = facebookGroup;
	}

	/**
	 * @return the facebookLocation
	 */
	public FacebookLocation getFacebookLocation() {
		return _facebookLocation;
	}

	/**
	 * @param facebookLocation the facebookLocation to set
	 */
	public void setFacebookLocation(FacebookLocation facebookLocation) {
		_facebookLocation = facebookLocation;
	}

	/**
	 * @return the facebookPhotoDescription
	 */
	public FacebookPhotoDescription getFacebookPhotoDescription() {
		return _facebookPhotoDescription;
	}

	/**
	 * @param facebookPhotoDescription the facebookPhotoDescription to set
	 */
	public void setFacebookPhotoDescription(FacebookPhotoDescription facebookPhotoDescription) {
		_facebookPhotoDescription = facebookPhotoDescription;
	}

	/**
	 * @return the facebookProfile
	 */
	public FacebookProfile getFacebookProfile() {
		return _facebookProfile;
	}

	/**
	 * @param facebookProfile the facebookProfile to set
	 */
	public void setFacebookProfile(FacebookProfile facebookProfile) {
		_facebookProfile = facebookProfile;
	}

	/**
	 * @return the facebookRelationship
	 */
	public FacebookRelationship getFacebookRelationship() {
		return _facebookRelationship;
	}

	/**
	 * @param facebookRelationship the facebookRelationship to set
	 */
	public void setFacebookRelationship(FacebookRelationship facebookRelationship) {
		_facebookRelationship = facebookRelationship;
	}

	/**
	 * @return the facebookStatusMessage
	 */
	public FacebookStatusMessage getFacebookStatusMessage() {
		return _facebookStatusMessage;
	}

	/**
	 * @param facebookStatusMessage the facebookStatusMessage to set
	 */
	public void setFacebookStatusMessage(FacebookStatusMessage facebookStatusMessage) {
		_facebookStatusMessage = facebookStatusMessage;
	}

	/**
	 * @return the facebookUserDetails
	 */
	public FacebookUserDetails getFacebookUserDetails() {
		return _facebookUserDetails;
	}

	/**
	 * @param facebookUserDetails the facebookUserDetails to set
	 */
	public void setFacebookUserDetails(FacebookUserDetails facebookUserDetails) {
		_facebookUserDetails = facebookUserDetails;
	}

	/**
	 * @return the facebookVideoDescription
	 */
	public FacebookVideoDescription getFacebookVideoDescription() {
		return _facebookVideoDescription;
	}

	/**
	 * @param facebookVideoDescription the facebookVideoDescription to set
	 */
	public void setFacebookVideoDescription(FacebookVideoDescription facebookVideoDescription) {
		_facebookVideoDescription = facebookVideoDescription;
	}

	/**
	 * @return the facebookLike
	 */
	public FacebookLike getFacebookLike() {
		return _facebookLike;
	}

	/**
	 * @param facebookLike the facebookLike to set
	 */
	public void setFacebookLike(FacebookLike facebookLike) {
		_facebookLike = facebookLike;
	}

	/**
	 * overridden to gather the real classes required for serialization
	 */
	@Override
	public Class<?>[] getDataClasses() {
		List<Class<?>> classes = new ArrayList<>();
		classes.add(getClass());
		if(_weightModifierList != null){
			classes.add(_weightModifierList.getClass());
		}
		if(_taskDetails != null){
			classes.add(_taskDetails.getClass());
		}
		if(_taskResponse != null){
			classes.add(_taskResponse.getClass());
		}
		if(_facebookPhotoTag != null){
			classes.add(_facebookPhotoTag.getClass());
		}
		if(_weightModifier != null){
			classes.add(_weightModifier.getClass());
		}
		if(_facebookComment != null){
			classes.add(_facebookComment.getClass());
		}
		if(_facebookEvent != null){
			classes.add(_facebookEvent.getClass());
		}
		if(_facebookGroup != null){
			classes.add(_facebookGroup.getClass());
		}
		if(_facebookLocation != null){
			classes.add(_facebookLocation.getClass());
		}
		if(_facebookPhotoDescription != null){
			classes.add(_facebookPhotoDescription.getClass());
		}
		if(_facebookProfile != null){
			classes.add(_facebookProfile.getClass());
		}
		if(_facebookRelationship != null){
			classes.add(_facebookRelationship.getClass());
		}
		if(_facebookStatusMessage != null){
			classes.add(_facebookStatusMessage.getClass());
		}
		if(_facebookUserDetails != null){
			classes.add(_facebookUserDetails.getClass());
		}
		if(_facebookVideoDescription != null){
			classes.add(_facebookVideoDescription.getClass());
		}
		if(_facebookLike != null){
			classes.add(_facebookLike.getClass());
		}
		return classes.toArray(new Class<?>[classes.size()]);
	}
}
