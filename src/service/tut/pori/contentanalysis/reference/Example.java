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
package service.tut.pori.contentanalysis.reference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

import service.tut.pori.contentanalysis.AnalysisBackend;
import service.tut.pori.contentanalysis.PhotoParameters;
import service.tut.pori.contentanalysis.BackendStatus;
import service.tut.pori.contentanalysis.BackendStatusList;
import service.tut.pori.contentanalysis.MediaObject;
import service.tut.pori.contentanalysis.MediaObjectList;
import service.tut.pori.contentanalysis.Photo;
import service.tut.pori.contentanalysis.PhotoFeedbackList;
import service.tut.pori.contentanalysis.PhotoList;
import service.tut.pori.contentanalysis.PhotoTaskDetails;
import service.tut.pori.contentanalysis.PhotoTaskResponse;
import service.tut.pori.contentanalysis.ResultInfo;
import service.tut.pori.contentanalysis.VisualShape;
import core.tut.pori.http.ResponseData;

/**
 * Response example
 * 
 */
@XmlRootElement(name=service.tut.pori.contentanalysis.reference.Definitions.ELEMENT_EXAMPLE)
@XmlAccessorType(XmlAccessType.NONE)
public class Example extends ResponseData {
	@XmlElementRef
	private PhotoParameters _analysisParameters = null;
	@XmlElementRef
	private AnalysisBackend _analysisBackend = null;
	@XmlElementRef
	private BackendStatus _backendStatus = null;
	@XmlElementRef
	private BackendStatusList _backendStatusList = null;
	@XmlElementRef
	private PhotoFeedbackList _feedbackList = null;
	@XmlElementRef
	private Photo _photo = null;
	@XmlElementRef
	private PhotoList _photoList = null;
	@XmlElementRef
	private ResultInfo _resultInfo = null;
	@XmlElementRef
	private MediaObject _mediaObject = null;
	@XmlElementRef
	private MediaObjectList _mediaObjectList = null;
	@XmlElementRef
	private VisualShape _visualShape = null;
	@XmlElementRef
	private PhotoTaskResponse _taskResponse = null;
	@XmlElementRef
	private PhotoTaskDetails _taskDetails = null;

	/**
	 * overridden to gather the real classes required for serialization
	 */
	@Override
	public Class<?>[] getDataClasses() {
		List<Class<?>> classes = new ArrayList<>();
		classes.add(getClass());
		if(_feedbackList != null){
			classes.add(_feedbackList.getClass());
		}
		if(_photoList != null){
			classes.add(_photoList.getClass());
		}
		if(_mediaObjectList != null){
			classes.add(_mediaObjectList.getClass());
		}
		if(_taskResponse != null){
			classes.addAll(Arrays.asList(_taskResponse.getDataClasses()));
		}
		if(_taskDetails != null){
			classes.addAll(Arrays.asList(_taskDetails.getDataClasses()));
		}
		if(_backendStatusList != null){
			classes.add(_backendStatusList.getClass());
		}
		if(_photo != null){
			classes.add(_photo.getClass());
		}
		if(_resultInfo != null){
			classes.add(_resultInfo.getClass());
		}
		if(_mediaObject != null){
			classes.add(_mediaObject.getClass());
		}
		if(_visualShape != null){
			classes.add(_visualShape.getClass());
		}
		if(_analysisBackend != null){
			classes.add(_analysisBackend.getClass());
		}
		if(_analysisParameters != null){
			classes.add(_analysisParameters.getClass());
		}
		return classes.toArray(new Class<?>[classes.size()]);
	}

	/**
	 * @return the analysisBackend
	 */
	public AnalysisBackend getAnalysisBackend() {
		return _analysisBackend;
	}

	/**
	 * @param analysisBackend the analysisBackend to set
	 */
	public void setAnalysisBackend(AnalysisBackend analysisBackend) {
		_analysisBackend = analysisBackend;
	}

	/**
	 * @return the feedbackList
	 */
	public PhotoFeedbackList getFeedbackList() {
		return _feedbackList;
	}

	/**
	 * @param feedbackList the feedbackList to set
	 */
	public void setFeedbackList(PhotoFeedbackList feedbackList) {
		_feedbackList = feedbackList;
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

	/**
	 * @return the mediaObjectList
	 */
	public MediaObjectList getMediaObjectList() {
		return _mediaObjectList;
	}

	/**
	 * @param mediaObjectList the mediaObjectList to set
	 */
	public void setMediaObjectList(MediaObjectList mediaObjectList) {
		_mediaObjectList = mediaObjectList;
	}

	/**
	 * @return the taskResponse
	 */
	public PhotoTaskResponse getTaskResponse() {
		return _taskResponse;
	}

	/**
	 * @param taskResponse the taskResponse to set
	 */
	public void setTaskResponse(PhotoTaskResponse taskResponse) {
		_taskResponse = taskResponse;
	}

	/**
	 * @return the photoTaskDetails
	 */
	public PhotoTaskDetails getTaskDetails() {
		return _taskDetails;
	}

	/**
	 * @param taskDetails the photoTaskDetails to set
	 */
	public void setTaskDetails(PhotoTaskDetails taskDetails) {
		_taskDetails = taskDetails;
	}

	/**
	 * @return the backendStatusList
	 */
	public BackendStatusList getBackendStatusList() {
		return _backendStatusList;
	}

	/**
	 * @param backendStatusList the backendStatusList to set
	 */
	public void setBackendStatusList(BackendStatusList backendStatusList) {
		_backendStatusList = backendStatusList;
	}

	/**
	 * @return the photo
	 */
	public Photo getPhoto() {
		return _photo;
	}

	/**
	 * @param photo the photo to set
	 */
	public void setPhoto(Photo photo) {
		_photo = photo;
	}

	/**
	 * @return the backendStatus
	 */
	public BackendStatus getBackendStatus() {
		return _backendStatus;
	}

	/**
	 * @param backendStatus the backendStatus to set
	 */
	public void setBackendStatus(BackendStatus backendStatus) {
		_backendStatus = backendStatus;
	}

	/**
	 * @return the resultInfo
	 */
	public ResultInfo getResultInfo() {
		return _resultInfo;
	}

	/**
	 * @param resultInfo the resultInfo to set
	 */
	public void setResultInfo(ResultInfo resultInfo) {
		_resultInfo = resultInfo;
	}

	/**
	 * @return the mediaObject
	 */
	public MediaObject getMediaObject() {
		return _mediaObject;
	}

	/**
	 * @param mediaObject the mediaObject to set
	 */
	public void setMediaObject(MediaObject mediaObject) {
		_mediaObject = mediaObject;
	}

	/**
	 * @return the visualShape
	 */
	public VisualShape getVisualShape() {
		return _visualShape;
	}

	/**
	 * @param visualShape the visualShape to set
	 */
	public void setVisualShape(VisualShape visualShape) {
		_visualShape = visualShape;
	}

	/**
	 * @return the analysisParameters
	 */
	public PhotoParameters getAnalysisParameters() {
		return _analysisParameters;
	}

	/**
	 * @param analysisParameters the analysisParameters to set
	 */
	public void setAnalysisParameters(PhotoParameters analysisParameters) {
		_analysisParameters = analysisParameters;
	}
}
