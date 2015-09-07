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
package service.tut.pori.contentstorage.reference;

import service.tut.pori.contentanalysis.CAContentCore.ServiceType;
import service.tut.pori.contentstorage.Definitions;
import service.tut.pori.contentstorage.MediaList;
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
 * Reference implementation for client API methods.
 * 
 * <h1>Implementation Service path {@value service.tut.pori.contentstorage.Definitions#SERVICE_CS}</h1>
 * 
 * @see service.tut.pori.contentstorage.ContentStorageService
 * 
 */
@HTTPService(name = service.tut.pori.contentstorage.reference.Definitions.SERVICE_COS_REFERENCE_CLIENT)
public class ClientService {
	/**
	 * This method allows the user to initialize external account synchronization. The synchronization process is always performed as a scheduled task, and thus, may not complete immediately. For new content discovered during the synchronization process, a new analysis task will be created, and the task will be automatically added for the appropriate analysis back-ends.
	 * 
	 * @param authenticatedUser Note: this method requires authentication, but for the reference implementation, anonymous access is granted.
	 * @param backendId Back-ends used for the analysis of the generated tasks. If no back-end IDs are given, the default back-ends will be used. An attempt to give incompatible back-ends may result in failure of the entire task. When providing specific back-ends by Id one should make sure that the back-ends are capable of performing the requested tasks.
	 * @param serviceId Any combination of supported external providers designated by serviceIds. The supported id values are: 1 (Picasa), 2 (FSIO), 4 (Facebook), 6 (Twitter), 7 (Url Storage). See {@link service.tut.pori.contentanalysis.CAContentCore.ServiceType}
	 */
	@HTTPServiceMethod(name=Definitions.METHOD_SYNCHRONIZE)
	public void synchronize(
			@HTTPAuthenticationParameter(required=false) AuthenticationParameter authenticatedUser,
			@HTTPMethodParameter(name=service.tut.pori.contentanalysis.Definitions.PARAMETER_BACKEND_ID, required = false) IntegerParameter backendId,
			@HTTPMethodParameter(name=service.tut.pori.contentanalysis.Definitions.PARAMETER_SERVICE_ID) IntegerParameter serviceId
			)
	{
		ContentStorageReferenceCore.synchronize(authenticatedUser.getUserIdentity(), backendId.getValues(), ServiceType.fromIdArray(serviceId.getValues()));
	}
	
	/**
	 * This method can be used to add files denoted by arbitrary URL links to the service. Photo and video analysis tasks will be scheduled for the added URLs and submitted for back-ends.
	 * 
	 * The files must be publicly accessible and of one of file types supported by the validator {@link core.tut.pori.utils.MediaUrlValidator}.
	 * 
	 * @param authenticatedUser Note: this method requires authentication, but for the reference implementation, anonymous access is granted.
	 * @param backendId Back-ends used for the analysis of the generated tasks. If no back-end IDs are given, the default back-ends will be used. An attempt to give incompatible back-ends may result in failure of the entire task. When providing specific back-ends by Id one should make sure that the back-ends are capable of performing the requested tasks.
	 * @param url Any number of URL links.
	 * @return See {@link service.tut.pori.contentstorage.MediaList}
	 */
	@HTTPServiceMethod(name=Definitions.METHOD_ADD_URL, acceptedMethods={core.tut.pori.http.Definitions.METHOD_POST})
	public Response addUrl(
			@HTTPAuthenticationParameter(required=false) AuthenticationParameter authenticatedUser,
			@HTTPMethodParameter(name=service.tut.pori.contentanalysis.Definitions.PARAMETER_BACKEND_ID, required = false) IntegerParameter backendId,
			@HTTPMethodParameter(name=service.tut.pori.contentanalysis.Definitions.PARAMETER_URL) StringParameter url
			)
	{
		MediaList media = ContentStorageReferenceCore.addUrls(authenticatedUser.getUserIdentity(), backendId.getValues(), url.getValues());
		if(MediaList.isEmpty(media)){
			return new Response(Status.BAD_REQUEST);
		}else{
			return new Response(media);
		}
	}
}
