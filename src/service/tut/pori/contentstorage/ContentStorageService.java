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
package service.tut.pori.contentstorage;

import service.tut.pori.contentanalysis.CAContentCore.ServiceType;
import core.tut.pori.http.Response;
import core.tut.pori.http.Response.Status;
import core.tut.pori.http.annotations.HTTPAuthenticationParameter;
import core.tut.pori.http.annotations.HTTPMethodParameter;
import core.tut.pori.http.annotations.HTTPService;
import core.tut.pori.http.annotations.HTTPServiceMethod;
import core.tut.pori.http.parameters.AuthenticationParameter;
import core.tut.pori.http.parameters.IntegerParameter;
import core.tut.pori.http.parameters.StringParameter;

/**
 * Service definitions for the Content Storage service.
 * 
 * @see service.tut.pori.contentstorage.reference.ClientService
 */
@HTTPService(name=Definitions.SERVICE_CS)
public class ContentStorageService {
	
	/**
	 * @see service.tut.pori.contentstorage.reference.ClientService#synchronize(AuthenticationParameter, IntegerParameter, IntegerParameter)
	 * 
	 * @param authenticatedUser
	 * @param backendId
	 * @param serviceId
	 */
	@HTTPServiceMethod(name=Definitions.METHOD_SYNCHRONIZE)
	public void synchronize(
			@HTTPAuthenticationParameter AuthenticationParameter authenticatedUser,
			@HTTPMethodParameter(name=service.tut.pori.contentanalysis.Definitions.PARAMETER_BACKEND_ID, required = false) IntegerParameter backendId,
			@HTTPMethodParameter(name=service.tut.pori.contentanalysis.Definitions.PARAMETER_SERVICE_ID) IntegerParameter serviceId
			)
	{
		ContentStorageCore.synchronize(authenticatedUser.getUserIdentity(), backendId.getValues(), ServiceType.fromIdArray(serviceId.getValues()));
	}
	
	/**
	 * @see service.tut.pori.contentstorage.reference.ClientService#addUrl(AuthenticationParameter, IntegerParameter, StringParameter)
	 * 
	 * @param authenticatedUser
	 * @param backendId
	 * @param url
	 * @return response
	 */
	@HTTPServiceMethod(name=Definitions.METHOD_ADD_URL, acceptedMethods={core.tut.pori.http.Definitions.METHOD_POST})
	public Response addUrl(
			@HTTPAuthenticationParameter AuthenticationParameter authenticatedUser,
			@HTTPMethodParameter(name=service.tut.pori.contentanalysis.Definitions.PARAMETER_BACKEND_ID, required = false) IntegerParameter backendId,
			@HTTPMethodParameter(name=service.tut.pori.contentanalysis.Definitions.PARAMETER_URL) StringParameter url
			)
	{
		MediaList media = ContentStorageCore.addUrls(authenticatedUser.getUserIdentity(), backendId.getValues(), url.getValues());
		if(MediaList.isEmpty(media)){
			return new Response(Status.BAD_REQUEST);
		}else{
			return new Response(media);
		}
	}
}
