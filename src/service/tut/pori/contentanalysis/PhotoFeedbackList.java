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
package service.tut.pori.contentanalysis;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


/**
 * Class for representing details of a feedback given by the user.
 * 
 * Feedback Lists are used by the clients to deliver feedback to the front-end.
 * 
 * The feedback must always contain {@link service.tut.pori.contentanalysis.PhotoList} with at least one {@link service.tut.pori.contentanalysis.Photo}. The photos in the {@link service.tut.pori.contentanalysis.SimilarPhotoList} and {@link service.tut.pori.contentanalysis.DissimilarPhotoList} are similar and not similar to the photos listed in the {@link service.tut.pori.contentanalysis.ReferencePhotoList}.
 * 
 * <h2>Conditional Elements</h2>
 * <ul>
 *  <li>{@value service.tut.pori.contentanalysis.Definitions#ELEMENT_SIMILAR_PHOTOLIST}</li>
 *  <li>{@value service.tut.pori.contentanalysis.Definitions#ELEMENT_DISSIMILAR_PHOTOLIST}</li>
 * </ul>
 * At least one of the conditional elements must be present, and have valid content.
 * 
 * <h3>XML Example</h3>
 * 
 * {@doc.restlet service="[service.tut.pori.contentanalysis.reference.Definitions#SERVICE_CA_REFERENCE_EXAMPLE]" method="[service.tut.pori.contentanalysis.Definitions#ELEMENT_FEEDBACKLIST]" type="GET" query="" body_uri=""}
 * 
 * @see service.tut.pori.contentanalysis.DissimilarPhotoList
 * @see service.tut.pori.contentanalysis.ReferencePhotoList
 * @see service.tut.pori.contentanalysis.SimilarPhotoList
 */
@XmlRootElement(name=Definitions.ELEMENT_FEEDBACKLIST)
@XmlAccessorType(XmlAccessType.NONE)
public class PhotoFeedbackList {
	@XmlElement(name = Definitions.ELEMENT_DISSIMILAR_PHOTOLIST)
	private DissimilarPhotoList _dissimilarPhotos = null;
	@XmlElement(name = Definitions.ELEMENT_REFERENCE_PHOTOLIST)
	private ReferencePhotoList _referencePhotos = null;
	@XmlElement(name = Definitions.ELEMENT_SIMILAR_PHOTOLIST)
	private SimilarPhotoList _similarPhotos = null;

	/**
	 * references must be given, similars and/or dissimilars must be given
	 * @param dissimilars
	 * @param references
	 * @param similars
	 * 
	 */
	public PhotoFeedbackList(DissimilarPhotoList dissimilars, ReferencePhotoList references, SimilarPhotoList similars){
		_referencePhotos = references;
		_similarPhotos = similars;
		_dissimilarPhotos = dissimilars;
	}

	/**
	 * for serialization
	 */
	protected PhotoFeedbackList(){
		// nothing needed
	}

	/**
	 * 
	 * @return reference photos
	 */
	public ReferencePhotoList getReferencePhotos() {
		return _referencePhotos;
	}

	/**
	 * 
	 * @param referencePhotos
	 */
	public void setReferencePhotos(ReferencePhotoList referencePhotos) {
		_referencePhotos = referencePhotos;
	}

	/**
	 * 
	 * @return similar photos
	 */
	public SimilarPhotoList getSimilarPhotos() {
		return _similarPhotos;
	}

	/**
	 * 
	 * @param similarPhotos
	 */
	public void setSimilarPhotos(SimilarPhotoList similarPhotos) {
		_similarPhotos = similarPhotos;
	}

	/**
	 * 
	 * @return dissimilar photos
	 */
	public DissimilarPhotoList getDissimilarPhotos() {
		return _dissimilarPhotos;
	}

	/**
	 * 
	 * @param dissimilarPhotos
	 */
	public void setDissimilarPhotos(DissimilarPhotoList dissimilarPhotos) {
		_dissimilarPhotos = dissimilarPhotos;
	}

	/**
	 * 
	 * @param list can be null
	 * @return true if the given list is valid
	 */
	public static boolean isValid(PhotoFeedbackList list){
		if(list == null){
			return false;
		}else{
			return list.isValid();
		}
	}

	/**
	 * only for sub-classing, use the static
	 * @return true if this list is valid
	 * @see #isValid(PhotoFeedbackList)
	 */
	protected boolean isValid(){
		if(!ReferencePhotoList.isValid(_referencePhotos) || (_similarPhotos == null && _dissimilarPhotos == null)){
			return false;
		}else if(_similarPhotos != null && !SimilarPhotoList.isValid(_similarPhotos)){
			return false;
		}else if(_dissimilarPhotos != null && !DissimilarPhotoList.isValid(_dissimilarPhotos)){
			return false;
		}else{
			return true;
		}
	}
}
