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


import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.StringUtils;

import core.tut.pori.context.ServiceInitializer;
import service.tut.pori.contentanalysis.AsyncTask.TaskType;

/**
 * An implementation of AbstractTaskDetails, which can be used to define a photo analysis task, or a feedback task.
 * 
 * <h2>Conditional Elements</h2>
 * <ul>
 *  <li>{@value service.tut.pori.contentanalysis.Definitions#ELEMENT_DELETED_PHOTOLIST}</li>
 *  <li>{@value service.tut.pori.contentanalysis.Definitions#ELEMENT_DISSIMILAR_PHOTOLIST}</li>
 *  <li>{@value service.tut.pori.contentanalysis.Definitions#ELEMENT_PHOTOLIST}</li>
 *  <li>{@value service.tut.pori.contentanalysis.Definitions#ELEMENT_REFERENCE_PHOTOLIST}</li>
 *  <li>{@value service.tut.pori.contentanalysis.Definitions#ELEMENT_SIMILAR_PHOTOLIST}</li>
 * </ul>
 * 
 * 
 * One of {@value service.tut.pori.contentanalysis.Definitions#ELEMENT_PHOTOLIST}, {@value service.tut.pori.contentanalysis.Definitions#ELEMENT_REFERENCE_PHOTOLIST} or {@value service.tut.pori.contentanalysis.Definitions#ELEMENT_DELETED_PHOTOLIST} must be present in a task. If {@value service.tut.pori.contentanalysis.Definitions#ELEMENT_DISSIMILAR_PHOTOLIST} or {@value service.tut.pori.contentanalysis.Definitions#ELEMENT_SIMILAR_PHOTOLIST} is present, {@value service.tut.pori.contentanalysis.Definitions#ELEMENT_REFERENCE_PHOTOLIST} must be present.
 * 
 * When including {@link service.tut.pori.contentanalysis.SimilarPhotoList} and/or {@link service.tut.pori.contentanalysis.DissimilarPhotoList}, it is recommended to provide only a single reference photo in the {@link service.tut.pori.contentanalysis.ReferencePhotoList}, and the photos should not contain media objects with updated metadata (i.e. media object feedback). 
 * For the media object feedback, a separate feedback task should be generated. 
 * For tasks of type {@link service.tut.pori.contentanalysis.AsyncTask.TaskType#ANALYSIS} and {@link service.tut.pori.contentanalysis.AsyncTask.TaskType#BACKEND_FEEDBACK}, the valid list is {@link service.tut.pori.contentanalysis.PhotoList}, and for tasks of type {@link service.tut.pori.contentanalysis.AsyncTask.TaskType#FEEDBACK} the valid lists are {@link service.tut.pori.contentanalysis.DeletedPhotoList}, {@link service.tut.pori.contentanalysis.DissimilarPhotoList}, {@link service.tut.pori.contentanalysis.ReferencePhotoList} and {@link service.tut.pori.contentanalysis.SimilarPhotoList}.
 * 
 * <h2>Optional Elements</h2>
 * <ul>
 *  <li>{@value service.tut.pori.contentanalysis.Definitions#ELEMENT_BACKEND_STATUS_LIST}</li>
 *  <li>{@value service.tut.pori.contentanalysis.Definitions#ELEMENT_USER_CONFIDENCE}, recommended values are 0...1 normalized, though other values are also accepted.</li>
 * </ul>
 * 
 * <h3>XML Example - Analysis Task</h3>
 * 
 * {@doc.restlet service="[service.tut.pori.contentanalysis.reference.Definitions#SERVICE_CA_REFERENCE_EXAMPLE]" method="[service.tut.pori.contentanalysis.Definitions#ELEMENT_TASK_DETAILS]" type="GET" query="[service.tut.pori.contentanalysis.reference.Definitions#PARAMETER_TASK_TYPE]=[service.tut.pori.contentanalysis.AsyncTask$TaskType#ANALYSIS]" body_uri=""}
 * 
 * <h3>XML Example - Backend Feedback Task</h3>
 * 
 * {@doc.restlet service="[service.tut.pori.contentanalysis.reference.Definitions#SERVICE_CA_REFERENCE_EXAMPLE]" method="[service.tut.pori.contentanalysis.Definitions#ELEMENT_TASK_DETAILS]" type="GET" query="[service.tut.pori.contentanalysis.reference.Definitions#PARAMETER_TASK_TYPE]=[service.tut.pori.contentanalysis.AsyncTask$TaskType#BACKEND_FEEDBACK]" body_uri=""}
 * 
 * <h3>XML Example - Feedback Task</h3>
 * 
 * {@doc.restlet service="[service.tut.pori.contentanalysis.reference.Definitions#SERVICE_CA_REFERENCE_EXAMPLE]" method="[service.tut.pori.contentanalysis.Definitions#ELEMENT_TASK_DETAILS]" type="GET" query="[service.tut.pori.contentanalysis.reference.Definitions#PARAMETER_TASK_TYPE]=[service.tut.pori.contentanalysis.AsyncTask$TaskType#FEEDBACK]" body_uri=""}
 *   
 * @see service.tut.pori.contentanalysis.DeletedPhotoList
 * @see service.tut.pori.contentanalysis.DissimilarPhotoList
 * @see service.tut.pori.contentanalysis.SimilarPhotoList
 * @see service.tut.pori.contentanalysis.PhotoList
 * @see service.tut.pori.contentanalysis.ReferencePhotoList
 */
@XmlRootElement(name=Definitions.ELEMENT_TASK_DETAILS)
@XmlAccessorType(XmlAccessType.NONE)
public final class PhotoTaskDetails extends AbstractTaskDetails{
	private PhotoParameters _analysisParameters = null;
	@XmlElement(name = Definitions.ELEMENT_DELETED_PHOTOLIST)
	private DeletedPhotoList _deletedPhotoList = null;
	@XmlElement(name = Definitions.ELEMENT_DISSIMILAR_PHOTOLIST)
	private DissimilarPhotoList _dissimilarPhotoList = null;
	@XmlElement(name = Definitions.ELEMENT_PHOTOLIST)
	private PhotoList _photoList = null;
	@XmlElement(name = Definitions.ELEMENT_SIMILAR_PHOTOLIST)
	private SimilarPhotoList _similarPhotoList = null; 
	@XmlElement(name = Definitions.ELEMENT_REFERENCE_PHOTOLIST)
	private ReferencePhotoList _referencePhotoList = null;
	@XmlElement(name = Definitions.ELEMENT_USER_CONFIDENCE)
	private Double _userConfidence = null;

	/**
	 * 
	 * @return photo list
	 */
	public PhotoList getPhotoList() {
		return _photoList;
	}

	/**
	 * 
	 */
	public PhotoTaskDetails(){
		super();
	}
	
	/**
	 * 
	 * @param type
	 */
	public PhotoTaskDetails(TaskType type){
		super();
		setTaskType(type);
	}

	/**
	 * 
	 * @param photoList
	 * @see #getPhotoList()
	 */
	public void setPhotoList(PhotoList photoList) {
		_photoList = photoList;
	}

	/**
	 * 
	 * @return list of deleted photos
	 * @see #setDeletedPhotoList(DeletedPhotoList)
	 */
	public DeletedPhotoList getDeletedPhotoList() {
		return _deletedPhotoList;
	}

	/**
	 * 
	 * @param deletedPhotoList
	 * @see #getDeletedPhotoList()
	 */
	public void setDeletedPhotoList(DeletedPhotoList deletedPhotoList) {
		_deletedPhotoList = deletedPhotoList;
	}

	/**
	 * @see #getReferencePhotoList()
	 * @see #setSimilarPhotoList(SimilarPhotoList)
	 * 
	 * @return list of photos similar to reference photos
	 */
	public SimilarPhotoList getSimilarPhotoList() {
		return _similarPhotoList;
	}

	/**
	 * 
	 * @param similarPhotoList
	 * @see #getSimilarPhotoList()
	 */
	public void setSimilarPhotoList(SimilarPhotoList similarPhotoList) {
		_similarPhotoList = similarPhotoList;
	}

	/**
	 * @see #getReferencePhotoList()
	 * @see #setDissimilarPhotoList(DissimilarPhotoList)
	 * 
	 * @return list of photos dissimilar to the reference photos
	 */
	public DissimilarPhotoList getDissimilarPhotoList() {
		return _dissimilarPhotoList;
	}

	/**
	 * 
	 * @param dissimilarPhotoList
	 * @see #getDissimilarPhotoList()
	 */
	public void setDissimilarPhotoList(DissimilarPhotoList dissimilarPhotoList) {
		_dissimilarPhotoList = dissimilarPhotoList;
	}
	
	/**
	 * @see #getDissimilarPhotoList()
	 * @see #getSimilarPhotoList()
	 * @see #setReferencePhotoList(ReferencePhotoList)
	 * 
	 * @return reference photos for similar and/or dissimilar photos
	 */
	public ReferencePhotoList getReferencePhotoList() {
		return _referencePhotoList;
	}

	/**
	 * @param referencePhotoList the referencePhotoList to set
	 * @see #getReferencePhotoList()
	 */
	public void setReferencePhotoList(ReferencePhotoList referencePhotoList) {
		_referencePhotoList = referencePhotoList;
	}
	
	/**
	 * 
	 * @param photo
	 * @see #getReferencePhotoList()
	 */
	public void addReferencePhoto(Photo photo){
		if(_referencePhotoList == null){
			_referencePhotoList = new ReferencePhotoList();
		}
		_referencePhotoList.addPhoto(photo);
	}
	
	/**
	 * 
	 * @param photo
	 * @see #getPhotoList()
	 */
	public void addPhoto(Photo photo){
		if(_photoList == null){
			_photoList = new PhotoList();
		}
		_photoList.addPhoto(photo);
	}
	
	/**
	 * 
	 * @param photo
	 * @see #getDeletedPhotoList()
	 */
	public void addDeletedPhoto(Photo photo){
		if(_deletedPhotoList == null){
			_deletedPhotoList = new DeletedPhotoList();
		}
		_deletedPhotoList.addPhoto(photo);
	}
	
	/**
	 * 
	 * @param photo
	 * @see #getSimilarPhotoList()
	 */
	public void addSimilarPhoto(Photo photo){
		if(_similarPhotoList == null){
			_similarPhotoList = new SimilarPhotoList();
		}
		_similarPhotoList.addPhoto(photo);
	}
	
	/**
	 * 
	 * @param photo
	 * @see #getDissimilarPhotoList()
	 */
	public void addDissimilarPhoto(Photo photo){
		if(_dissimilarPhotoList == null){
			_dissimilarPhotoList = new DissimilarPhotoList();
		}
		_dissimilarPhotoList.addPhoto(photo);
	}
	
	/**
	 * 
	 * @return true if no photo lists are given or the lists are empty
	 */
	public boolean isEmpty(){
		if(!PhotoList.isEmpty(_photoList) || !ReferencePhotoList.isEmpty(_referencePhotoList) || !DeletedPhotoList.isEmpty(_deletedPhotoList) || !SimilarPhotoList.isEmpty(_similarPhotoList) || !DissimilarPhotoList.isEmpty(_dissimilarPhotoList)){
			return false;
		}else{
			return true;
		}
	}
	
	/**
	 * 
	 * @return confidence of the task owner, or null if unknown
	 * @see #setUserConfidence(Double)
	 */
	public Double getUserConfidence() {
		return _userConfidence;
	}

	/**
	 * 
	 * @param userConfidence
	 * @see #getUserConfidence()
	 */
	public void setUserConfidence(Double userConfidence) {
		_userConfidence = userConfidence;
	}

	@Override
	public Map<String, String> getMetadata() {
		if(_userConfidence == null){
			return super.getMetadata();
		}else{
			Map<String, String> metadata = super.getMetadata();
			if(metadata == null){
				metadata = new HashMap<>(1);
				super.setMetadata(metadata);
			}
			metadata.put(Definitions.ELEMENT_CONFIDENCE, _userConfidence.toString());
			return metadata;
		}
	}

	@Override
	public void setMetadata(Map<String, String> metadata) {
		if(metadata == null){
			_userConfidence = null;
		}else{
			String confidence = metadata.get(Definitions.ELEMENT_CONFIDENCE);
			if(StringUtils.isBlank(confidence)){
				_userConfidence = null;
			}else{
				_userConfidence = Double.valueOf(confidence);
			}
		}
		super.setMetadata(metadata);
	}
	
	@Override
	public String getCallbackUri() {
		String callbackUri = super.getCallbackUri();
		return (StringUtils.isBlank(callbackUri) ? generateFinishedCallbackUri() : callbackUri);
	}

	/**
	 * 
	 * @return the default task finished callback uri
	 */
	public static String generateFinishedCallbackUri(){
		return ServiceInitializer.getPropertyHandler().getRESTBindContext()+Definitions.SERVICE_CA+"/"+Definitions.METHOD_TASK_FINISHED;
	}

	@Override
	public PhotoParameters getTaskParameters() {
		return _analysisParameters;
	}

	@Override
	public void setTaskParameters(TaskParameters parameters) {
		if(parameters == null){
			_analysisParameters = null;
		}else if(parameters instanceof PhotoParameters){
			setTaskParameters((PhotoParameters) parameters);
		}else{
			_analysisParameters = new PhotoParameters();
		}
	}
	
	/**
	 * 
	 * @param parameters
	 */
	public void setTaskParameters(PhotoParameters parameters) {
		_analysisParameters = parameters;
	}
}
