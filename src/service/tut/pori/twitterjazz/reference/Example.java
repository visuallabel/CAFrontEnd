package service.tut.pori.twitterjazz.reference;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import service.tut.pori.contentanalysis.Definitions;
import service.tut.pori.twitterjazz.TwitterLocation;
import service.tut.pori.twitterjazz.TwitterPhotoDescription;
import service.tut.pori.twitterjazz.TwitterPhotoTag;
import service.tut.pori.twitterjazz.TwitterProfile;
import service.tut.pori.twitterjazz.TwitterStatusMessage;
import service.tut.pori.twitterjazz.TwitterSummarizationTaskDetails;
import service.tut.pori.twitterjazz.TwitterTaskResponse;
import service.tut.pori.twitterjazz.TwitterUserDetails;
import service.tut.pori.twitterjazz.TwitterVideoDescription;
import core.tut.pori.http.ResponseData;

/**
 * Response example
 * 
 */
@XmlRootElement(name=service.tut.pori.twitterjazz.reference.Definitions.ELEMENT_EXAMPLE)
@XmlAccessorType(XmlAccessType.NONE)
public class Example extends ResponseData {
	@XmlElement(name=Definitions.ELEMENT_TASK_DETAILS)
	private TwitterSummarizationTaskDetails _taskDetails = null;
	@XmlElement(name=Definitions.ELEMENT_TASK_RESULTS)
	private TwitterTaskResponse _taskResponse = null;
	@XmlElement(name=service.tut.pori.twitterjazz.Definitions.ELEMENT_LOCATION)
	private TwitterLocation _twitterLocation = null;
	@XmlElement(name=service.tut.pori.twitterjazz.Definitions.ELEMENT_PHOTO_DESCRIPTION)
	private TwitterPhotoDescription _twitterPhotoDescription = null;
	@XmlElement(name=service.tut.pori.twitterjazz.Definitions.ELEMENT_PHOTO_TAG)
	private TwitterPhotoTag _twitterPhotoTag = null;
	@XmlElement(name=service.tut.pori.twitterjazz.Definitions.ELEMENT_TWITTER_PROFILE)
	private TwitterProfile _twitterProfile = null;
	@XmlElement(name=service.tut.pori.twitterjazz.Definitions.ELEMENT_STATUS_MESSAGE)
	private TwitterStatusMessage _twitterStatusMessage = null;
	@XmlElement(name=service.tut.pori.twitterjazz.Definitions.ELEMENT_USER_DETAILS)
	private TwitterUserDetails _twitterUserDetails = null;
	@XmlElement(name=service.tut.pori.twitterjazz.Definitions.ELEMENT_VIDEO_DESCRIPTION)
	private TwitterVideoDescription _twitterVideoDescription = null;

	/**
	 * @return the twitterSummarizationTaskDetails
	 */
	public TwitterSummarizationTaskDetails getTaskDetails() {
		return _taskDetails;
	}

	/**
	 * @param taskDetails the twitterSummarizationTaskDetails to set
	 */
	public void setTaskDetails(TwitterSummarizationTaskDetails taskDetails) {
		_taskDetails = taskDetails;
	}

	/**
	 * @return the taskResponse
	 */
	public TwitterTaskResponse getTaskResponse() {
		return _taskResponse;
	}

	/**
	 * @param taskResponse the taskResponse to set
	 */
	public void setTaskResponse(TwitterTaskResponse taskResponse) {
		_taskResponse = taskResponse;
	}

	/**
	 * @return the twitterLocation
	 */
	public TwitterLocation getTwitterLocation() {
		return _twitterLocation;
	}

	/**
	 * @param twitterLocation the twitterLocation to set
	 */
	public void setTwitterLocation(TwitterLocation twitterLocation) {
		_twitterLocation = twitterLocation;
	}

	/**
	 * @return the twitterPhotoDescription
	 */
	public TwitterPhotoDescription getTwitterPhotoDescription() {
		return _twitterPhotoDescription;
	}

	/**
	 * @param twitterPhotoDescription the twitterPhotoDescription to set
	 */
	public void setTwitterPhotoDescription(TwitterPhotoDescription twitterPhotoDescription) {
		_twitterPhotoDescription = twitterPhotoDescription;
	}

	/**
	 * @return the twitterPhotoTag
	 */
	public TwitterPhotoTag getTwitterPhotoTag() {
		return _twitterPhotoTag;
	}

	/**
	 * @param twitterPhotoTag the twitterPhotoTag to set
	 */
	public void setTwitterPhotoTag(TwitterPhotoTag twitterPhotoTag) {
		_twitterPhotoTag = twitterPhotoTag;
	}

	/**
	 * @return the twitterProfile
	 */
	public TwitterProfile getTwitterProfile() {
		return _twitterProfile;
	}

	/**
	 * @param twitterProfile the twitterProfile to set
	 */
	public void setTwitterProfile(TwitterProfile twitterProfile) {
		_twitterProfile = twitterProfile;
	}

	/**
	 * @return the twitterStatusMessage
	 */
	public TwitterStatusMessage getTwitterStatusMessage() {
		return _twitterStatusMessage;
	}

	/**
	 * @param twitterStatusMessage the twitterStatusMessage to set
	 */
	public void setTwitterStatusMessage(TwitterStatusMessage twitterStatusMessage) {
		_twitterStatusMessage = twitterStatusMessage;
	}

	/**
	 * @return the twitterUserDetails
	 */
	public TwitterUserDetails getTwitterUserDetails() {
		return _twitterUserDetails;
	}

	/**
	 * @param twitterUserDetails the twitterUserDetails to set
	 */
	public void setTwitterUserDetails(TwitterUserDetails twitterUserDetails) {
		_twitterUserDetails = twitterUserDetails;
	}

	/**
	 * @return the twitterVideoDescription
	 */
	public TwitterVideoDescription getTwitterVideoDescription() {
		return _twitterVideoDescription;
	}

	/**
	 * @param twitterVideoDescription the twitterVideoDescription to set
	 */
	public void setTwitterVideoDescription(TwitterVideoDescription twitterVideoDescription) {
		_twitterVideoDescription = twitterVideoDescription;
	}
}
