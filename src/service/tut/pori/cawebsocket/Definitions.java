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
package service.tut.pori.cawebsocket;

/**
 * definitions for cawebsocket package
 * 
 */
public final class Definitions {
	/* service */
	/** service name declaration */
	protected static final String SERVICE_TASK_FINISHED = "taskFinished";
	
	/* elements */
	/** xml element declaration */
	protected static final String ELEMENT_LISTEN_ANONYMOUS_TASKS = "anonymousTasks";
	/** xml element declaration */
	protected static final String ELEMENT_BACKEND_ID_LIST = service.tut.pori.contentanalysis.Definitions.ELEMENT_BACKEND_ID+"List";
	/** xml element declaration */
	protected static final String ELEMENT_REGISTRATION = "registration";
	/** xml element declaration */
	protected static final String ELEMENT_TASK_ID_LIST = service.tut.pori.contentanalysis.Definitions.ELEMENT_TASK_ID+"List";
	/** xml element declaration */
	protected static final String ELEMENT_TASK_STATUS = "taskStatus";
	/** xml element declaration */
	protected static final String ELEMENT_TASK_TYPE_LIST = service.tut.pori.contentanalysis.Definitions.ELEMENT_TASK_TYPE+"List";
	/** xml element declaration */
	protected static final String ELEMENT_USER_ID_LIST = core.tut.pori.users.Definitions.ELEMENT_USER_ID+"List";
	
	/**
	 * 
	 */
	private Definitions(){
		// nothing needed
	}
}
