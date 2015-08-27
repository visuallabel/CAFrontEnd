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
package service.tut.pori.fileservice.reference;

import java.util.Arrays;

import service.tut.pori.fileservice.Definitions;
import service.tut.pori.fileservice.FileList;
import core.tut.pori.http.Response;
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
 * Reference implementation for client API methods.
 * 
 * This class defines the APIs available for file storage. Note that the file storage API is a "light-weight" service provided to enable analysis of arbitrary files not located on external providers. For a proper, backed up, cloud storage one should take a look at the one of the external providers supported by the front-end service (e.g. Facebook, Twitter, Picasa, FSIO), and use the synchronization methods for importing the content to the front-end service.
 * 
 * <h1>Implementation Service path {@value service.tut.pori.fileservice.Definitions#SERVICE_FS}</h1>
 * 
 * @see service.tut.pori.fileservice.FileService
 */
@HTTPService(name = service.tut.pori.fileservice.reference.Definitions.SERVICE_FS_REFERENCE_CLIENT)
public class ClientService {
	/**
	 * This method allows an authenticated user to upload a file directly using HTTP Post. Note that this method does not implement HTTP PUT, i.e. the file will not be available in a user defined URI regardless of the given filename. The uploaded file can only be accessed using the returned URL. The URL is generally a static link, and you can use the List Files method to retrieve up-to-date URLs.
	 * The File Service stores the files in binary format, and thus enforces no limits on the file type. The maximum file size depends on the features of the system to where the front-end service has been deployed - the File Service itself imposes no limits on file sizes. In general files larger than 2 GB should be avoided.
	 * Note that simply uploading a file will <i>not</i> start the analysis process. One must pass the received URL link to the Content Storage service to initialize the file analysis.
	 * 
	 * @param authenticatedUser Note: this method requires authentication, but for the reference implementation, anonymous access is granted.
	 * @param file file contents
	 * @param filename Optional filename for the uploaded data. The filename is processed simply as metadata and omitting the parameter has no effect on the file upload. 
	 * @return See {@link service.tut.pori.fileservice.FileList}
	 */
	@HTTPServiceMethod(name=Definitions.METHOD_ADD_FILE, acceptedMethods={core.tut.pori.http.Definitions.METHOD_POST})
	public Response addFile(
			@HTTPAuthenticationParameter(required=false) AuthenticationParameter authenticatedUser,
			@HTTPMethodParameter(name=InputStreamParameter.PARAMETER_DEFAULT_NAME, bodyParameter=true) InputStreamParameter file,
			@HTTPMethodParameter(name=Definitions.PARAMETER_FILE_NAME, required = false) StringParameter filename
			)
	{
		return new Response(FileList.getFileList(Arrays.asList(FileReferenceCore.addFile(authenticatedUser.getUserIdentity(), file.getValue(), filename.getValue()))));
	}
	
	/**
	 * This method can be used to delete one or more files. Non-existent file IDs will be ignored.
	 * 
	 * @param authenticatedUser Note: this method requires authentication, but for the reference implementation, anonymous access is granted.
	 * @param fileId
	 */
	@HTTPServiceMethod(name=Definitions.METHOD_DELETE_FILE, acceptedMethods=core.tut.pori.http.Definitions.METHOD_DELETE)
	public void deleteFile(
			@HTTPAuthenticationParameter(required=false) AuthenticationParameter authenticatedUser,
			@HTTPMethodParameter(name=Definitions.PARAMETER_FILE_ID) LongParameter fileId
			)
	{
		FileReferenceCore.deleteFile(authenticatedUser.getUserIdentity(), fileId.getValue());
	}
	
	/**
	 * List all or a subset of the files user has uploaded to the system.
	 * 
	 * @param authenticatedUser Note: this method requires authentication, but for the reference implementation, anonymous access is granted.
	 * @param fileId Optional file ID filter for retrieving a subset of the uploaded files. If ID is not given, all files will be retrieved respecting the optional limits parameter.
	 * @param limits paging limits
	 * @return See {@link service.tut.pori.fileservice.FileList}
	 */
	@HTTPServiceMethod(name=Definitions.METHOD_LIST_FILES, acceptedMethods={core.tut.pori.http.Definitions.METHOD_GET})
	public Response listFiles(
			@HTTPAuthenticationParameter(required=false) AuthenticationParameter authenticatedUser,
			@HTTPMethodParameter(name=Definitions.PARAMETER_FILE_ID, required=false) LongParameter fileId,
			@HTTPMethodParameter(name=Limits.PARAMETER_DEFAULT_NAME, required=false) Limits limits
			)
	{
		return new Response(FileReferenceCore.listFiles(authenticatedUser.getUserIdentity(), fileId.getValues(), limits));
	}
}
