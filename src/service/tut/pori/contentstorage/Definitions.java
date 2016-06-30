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
package service.tut.pori.contentstorage;

/**
 * definitions for contentstorage
 */
public final class Definitions {
	/* services */
	/** service name declaration */
	public static final String SERVICE_CS = "cos";
	
	/* methods */
	/** service method declaration */
	public static final String METHOD_ADD_URL = "addUrl";
	/** service method declaration */
	public static final String METHOD_SYNCHRONIZE = "synchronize";
	
	/* elements */
	/** xml element declaration */
	public static final String ELEMENT_MEDIALIST = "mediaList";
	
	/* definitions */
	/** default confidence for retrieved media objects @see {@link service.tut.pori.contentanalysis.MediaObject#getConfidence()} */
	public static final Double DEFAULT_CONFIDENCE = 1.0;
	/** default rank for retrieved media objects @see {@link service.tut.pori.contentanalysis.MediaObject#getRank()} */
	public static final Integer DEFAULT_RANK = 0;
	
	/**
	 * 
	 */
	private Definitions(){
		// nothing needed
	}
}
