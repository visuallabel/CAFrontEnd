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
package service.tut.pori.fileservice;

import java.util.Arrays;

import core.tut.pori.http.Response;
import core.tut.pori.http.Response.Status;
import core.tut.pori.http.annotations.HTTPAuthenticationParameter;
import core.tut.pori.http.annotations.HTTPMethodParameter;
import core.tut.pori.http.annotations.HTTPService;
import core.tut.pori.http.annotations.HTTPServiceMethod;
import core.tut.pori.http.parameters.AuthenticationParameter;
import core.tut.pori.http.parameters.InputStreamParameter;
import core.tut.pori.http.parameters.Limits;
import core.tut.pori.http.parameters.LongParameter;
import core.tut.pori.http.parameters.StringParameter;

/**
 * Service declarations for File Service.
 * 
 * @see service.tut.pori.fileservice.reference.ClientService
 */
@HTTPService(name=Definitions.SERVICE_FS)
public class FileService {

	/**
	 * @see service.tut.pori.fileservice.reference.ClientService#addFile(AuthenticationParameter, InputStreamParameter, StringParameter)
	 * 
	 * @param authenticatedUser
	 * @param file
	 * @param filename
	 * @return response
	 */
	@HTTPServiceMethod(name=Definitions.METHOD_ADD_FILE, acceptedMethods={core.tut.pori.http.Definitions.METHOD_POST})
	public Response addFile(
			@HTTPAuthenticationParameter AuthenticationParameter authenticatedUser,
			@HTTPMethodParameter(name=InputStreamParameter.PARAMETER_DEFAULT_NAME, bodyParameter=true) InputStreamParameter file,
			@HTTPMethodParameter(name=Definitions.PARAMETER_FILE_NAME, required = false) StringParameter filename
			)
	{
		File result = FileCore.addFile(authenticatedUser.getUserIdentity(), file.getValue(), filename.getValue());
		if(result == null){
			return new Response(Status.BAD_REQUEST);
		}else{
			return new Response(FileList.getFileList(Arrays.asList(result)));
		}
	}
	
	/**
	 * @see service.tut.pori.fileservice.reference.ClientService#deleteFile(AuthenticationParameter, LongParameter)
	 * 
	 * @param authenticatedUser
	 * @param fileId
	 */
	@HTTPServiceMethod(name=Definitions.METHOD_DELETE_FILE, acceptedMethods=core.tut.pori.http.Definitions.METHOD_DELETE)
	public void deleteFile(
			@HTTPAuthenticationParameter AuthenticationParameter authenticatedUser,
			@HTTPMethodParameter(name=Definitions.PARAMETER_FILE_ID) LongParameter fileId
			)
	{
		FileCore.deleteFiles(authenticatedUser.getUserIdentity(), fileId.getValues());
	}
	
	/**
	 * @see service.tut.pori.fileservice.reference.ClientService#listFiles(AuthenticationParameter, LongParameter, Limits)
	 * 
	 * @param authenticatedUser
	 * @param fileId
	 * @param limits 
	 * @return response
	 */
	@HTTPServiceMethod(name=Definitions.METHOD_LIST_FILES, acceptedMethods={core.tut.pori.http.Definitions.METHOD_GET})
	public Response listFiles(
			@HTTPAuthenticationParameter AuthenticationParameter authenticatedUser,
			@HTTPMethodParameter(name=Definitions.PARAMETER_FILE_ID, required=false) LongParameter fileId,
			@HTTPMethodParameter(name=Limits.PARAMETER_DEFAULT_NAME, required=false) Limits limits
			)
	{
		return new Response(FileCore.listFiles(authenticatedUser.getUserIdentity(), fileId.getValues(), limits));
	}
}
