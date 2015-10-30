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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import service.tut.pori.contentanalysis.AsyncTask.TaskStatus;


/**
 * Status details of a single analysis back-end.
 * 
 * <h2>Optional Elements</h2>
 * <ul>
 *  <li>{@value service.tut.pori.contentanalysis.Definitions#ELEMENT_MESSAGE}</li>
 * </ul>
 * 
 * <h3>XML Example</h3>
 * 
 * {@doc.restlet service="[service.tut.pori.contentanalysis.reference.Definitions#SERVICE_CA_REFERENCE_EXAMPLE]" method="[service.tut.pori.contentanalysis.Definitions#ELEMENT_BACKEND_STATUS]" type="GET" query="" body_uri=""}
 * 
 * @see service.tut.pori.contentanalysis.AnalysisBackend
 */
@XmlRootElement(name=Definitions.ELEMENT_BACKEND_STATUS)
@XmlAccessorType(XmlAccessType.NONE)
public class BackendStatus{
	private AnalysisBackend _backend = null;
	@XmlElement(name = Definitions.ELEMENT_MESSAGE)
	private String _message = null;  
	@XmlElement(name = Definitions.ELEMENT_STATUS)
	private AsyncTask.TaskStatus _status = null;

	/**
	 * 
	 * @param backend
	 * @param status
	 */
	public BackendStatus(AnalysisBackend backend, TaskStatus status){
		_backend= backend;
		_status = status;
	}
	
	/**
	 * for serialization
	 */
	protected BackendStatus(){
		// nothing needed
	}

	/**
	 * 
	 * @return Status of this back-end
	 * @see #setStatus(service.tut.pori.contentanalysis.AsyncTask.TaskStatus)
	 */
	public AsyncTask.TaskStatus getStatus() {
		return _status;
	}

	/**
	 * 
	 * @param status
	 * @see #getStatus()
	 */
	public void setStatus(AsyncTask.TaskStatus status) {
		_status = status;
	}

	/**
	 * 
	 * @return status message or null if not available
	 * @see #setMessage(String)
	 */
	public String getMessage() {
		return _message;
	}

	/**
	 * 
	 * @param message
	 * @see #getMessage()
	 */
	public void setMessage(String message) {
		_message = message;
	}

	/**
	 * @return the back-end
	 * @see #setBackend(AnalysisBackend)
	 */
	public AnalysisBackend getBackend() {
		return _backend;
	}

	/**
	 * @param backend the backend to set
	 * @see #getBackend()
	 */
	public void setBackend(AnalysisBackend backend) {
		_backend = backend;
	}
	
	/**
	 * for serialization
	 * 
	 * @param backendId
	 * @see #setBackend(AnalysisBackend)
	 */
	@SuppressWarnings("unused")
	private void setBackendId(Integer backendId) {
		if(backendId == null){
			_backend = null;
		}else{
			_backend = new AnalysisBackend(backendId);
		}
	}
	
	/**
	 * 
	 * @return back-end id
	 * @see #getBackend()
	 */
	@XmlElement(name=Definitions.ELEMENT_BACKEND_ID)
	public Integer getBackendId() {
		return (_backend == null ? null : _backend.getBackendId());
	}
}

