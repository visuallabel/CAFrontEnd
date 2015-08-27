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
package service.tut.pori.contentanalysis.video.reference;

/**
 * reference definitions
 */
public final class Definitions {
	/* services */
	/** service name declaration */
	public static final String SERVICE_VCA_REFERENCE_BACKEND = "vcarb";
	/** service name declaration */
	public static final String SERVICE_VCA_REFERENCE_CLIENT = "vcarc";
	/** service name declaration */
	public static final String SERVICE_VCA_REFERENCE_EXAMPLE = "vcare";
	/** service name declaration */
	public static final String SERVICE_VCA_REFERENCE_SERVER = "vcars";
	
	/* elements */
	/** xml element declaration */
	protected static final String ELEMENT_EXAMPLE = "example";
	
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
