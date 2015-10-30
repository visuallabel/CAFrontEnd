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
package service.tut.pori.contentanalysis.reference;

/**
 * reference definitions
 * 
 */
public final class Definitions {
	/* services */
	/** service name declaration */
	public static final String SERVICE_CA_REFERENCE_BACKEND = "carb";
	/** service name declaration */
	public static final String SERVICE_CA_REFERENCE_CLIENT = "carc";
	/** service name declaration */
	public static final String SERVICE_CA_REFERENCE_EXAMPLE = "care";
	/** service name declaration */
	public static final String SERVICE_CA_REFERENCE_SERVER = "cars";
	
	/* elements */
	/** xml element declaration */
	protected static final String ELEMENT_EXAMPLE = "example";
	
	/* common */
	/** how long to wait before calling callback in async responses */
	protected static final long ASYNC_CALLBACK_DELAY = 2000;	// in ms
	
	/* parameters */
	/** service method parameter declaration */
	protected static final String PARAMETER_TASK_TYPE = "task_type";

	/**
	 * 
	 */
	private Definitions(){
		// nothing needed
	}
}
