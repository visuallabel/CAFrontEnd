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
package service.tut.pori.contentanalysistest;

import service.tut.pori.contentanalysis.Definitions;
import core.tut.pori.http.RedirectResponse;
import core.tut.pori.http.Response;
import core.tut.pori.http.Response.Status;
import core.tut.pori.http.annotations.HTTPAuthenticationParameter;
import core.tut.pori.http.annotations.HTTPMethodParameter;
import core.tut.pori.http.annotations.HTTPService;
import core.tut.pori.http.annotations.HTTPServiceMethod;
import core.tut.pori.http.parameters.AuthenticationParameter;
import core.tut.pori.http.parameters.BooleanParameter;
import core.tut.pori.http.parameters.DataGroups;
import core.tut.pori.http.parameters.InputStreamParameter;
import core.tut.pori.http.parameters.IntegerParameter;
import core.tut.pori.http.parameters.LongParameter;
import core.tut.pori.http.parameters.StringParameter;


/**
 * This service includes test API for contentAnalysis service (analysis example APIs & such)
 * 
 * The reference implementations will not belong here anymore, clean this class of unneeded methods,
 * or remove the whole class if needed
 */
@Deprecated
@HTTPService(name = "catest")
public class ContentAnalysisTestService {	
	/**
	 * 
	 * @param guid
	 * @param userId 
	 * @return response
	 * 
	 */
	@HTTPServiceMethod(name = "hasAccess")
	public Response hasAccess(
			@HTTPMethodParameter(name = Definitions.PARAMETER_GUID) StringParameter guid, 
			@HTTPAuthenticationParameter AuthenticationParameter userId
			)
	{
		Response r = new Response();
		r.setMessage(ContentAnalysisTestUtils.hasAccess(guid.getValue(), userId.getUserIdentity()));
		return r;
	}

	/**
	 * 
	 * @param backendId 
	 * @return response
	 */
	@HTTPServiceMethod(name = "listAnalysisBackends")
	public Response listAnalysisBackends(
			@HTTPMethodParameter(name = Definitions.PARAMETER_BACKEND_ID, required = false) IntegerParameter backendId)
	{
		return new Response(ContentAnalysisTestUtils.listAnalysisBackends(backendId.getValues()));
	}

	/**
	 * 
	 * @param dataGroups
	 * @param identity
	 * @return response
	 */
	@HTTPServiceMethod(name = "extractFacebookProfile")
	public Response extractFacebookProfile(
			@HTTPMethodParameter(name = DataGroups.PARAMETER_DEFAULT_NAME, required = false) DataGroups dataGroups,
			@HTTPAuthenticationParameter(showLoginPrompt=true) AuthenticationParameter identity
			)
	{
		return new Response(ContentAnalysisTestUtils.extractFacebookProfile(dataGroups, identity.getUserIdentity()));
	}
	
	/**
	 * 
	 * @param dataGroups
	 * @param identity
	 * @return response
	 */
	@HTTPServiceMethod(name = "extractTwitterProfile")
	public Response extractTwitterProfile(
			@HTTPMethodParameter(name = DataGroups.PARAMETER_DEFAULT_NAME, required = false) DataGroups dataGroups,
			@HTTPAuthenticationParameter(showLoginPrompt=true) AuthenticationParameter identity
			)
	{
		return new Response(ContentAnalysisTestUtils.extractTwitterProfile(dataGroups, identity.getUserIdentity()));
	}

	/**
	 * 
	 * @param backendId
	 * @param capabilityString
	 * @param description
	 * @param enabled
	 * @param identity
	 * @param taskDataGroups
	 * @param url
	 */
	@HTTPServiceMethod(name = "modifyAnalysisBackend")
	public void modifyAnalysisBackend(
			@HTTPMethodParameter(name = Definitions.PARAMETER_BACKEND_ID) IntegerParameter backendId,
			@HTTPMethodParameter(name = "capability_string") StringParameter capabilityString,
			@HTTPMethodParameter(name = "description") StringParameter description,
			@HTTPMethodParameter(name = "enabled") BooleanParameter enabled,
			@HTTPAuthenticationParameter(showLoginPrompt=true) AuthenticationParameter identity,
			@HTTPMethodParameter(name = "task_data_groups") DataGroups taskDataGroups,
			@HTTPMethodParameter(name = Definitions.PARAMETER_URL) StringParameter url
			)
	{
		ContentAnalysisTestUtils.modifyAnalysisBackend(backendId.getValue(), capabilityString.getValues(), description.getValue(), enabled.getValue(), taskDataGroups, url.getValue());
	}

	/**
	 * 
	 * @param backendId
	 * @param identity
	 */
	@HTTPServiceMethod(name = "removeAnalysisBackend")
	public void removeAnalysisBackend(
			@HTTPMethodParameter(name = Definitions.PARAMETER_BACKEND_ID) IntegerParameter backendId,
			@HTTPAuthenticationParameter(showLoginPrompt=true) AuthenticationParameter identity
			)
	{
		ContentAnalysisTestUtils.removeAnalysisBackend(backendId.getValue());
	}

	/**
	 * 
	 * @param capabilityString
	 * @param description
	 * @param enabled
	 * @param identity
	 * @param taskDataGroups
	 * @param url
	 * @return response
	 */
	@HTTPServiceMethod(name = "createAnalysisBackend")
	public Response createAnalysisBackend(
			@HTTPMethodParameter(name = "capability_string") StringParameter capabilityString,
			@HTTPMethodParameter(name = "description") StringParameter description,
			@HTTPMethodParameter(name = "enabled", defaultValue = "false") BooleanParameter enabled,
			@HTTPAuthenticationParameter(showLoginPrompt=true) AuthenticationParameter identity,
			@HTTPMethodParameter(name = "task_data_groups", defaultValue = "all") DataGroups taskDataGroups,
			@HTTPMethodParameter(name = Definitions.PARAMETER_URL) StringParameter url
			)
	{
		Response r = new Response();
		r.setMessage(String.valueOf(ContentAnalysisTestUtils.createAnalysisBackend(capabilityString.getValues(), description.getValue(), enabled.getValue(), taskDataGroups, url.getValue())));
		return r;
	}
	
	/**
	 * 
	 * @param backendId
	 * @param taskId
	 */
	@HTTPServiceMethod(name = "reschedulePhotoTask")
	public void reschedulePhotoTask(
			@HTTPMethodParameter(name = Definitions.PARAMETER_BACKEND_ID) IntegerParameter backendId,
			@HTTPMethodParameter(name = Definitions.PARAMETER_TASK_ID) LongParameter taskId
			)
	{
		ContentAnalysisTestUtils.reschedulePhotoTask(backendId.getValues(), taskId.getValue());
	}
	
	/**
	 * 
	 * @param backendId
	 * @param taskId
	 */
	@HTTPServiceMethod(name = "rescheduleVideoTask")
	public void rescheduleVideoTask(
			@HTTPMethodParameter(name = Definitions.PARAMETER_BACKEND_ID) IntegerParameter backendId,
			@HTTPMethodParameter(name = Definitions.PARAMETER_TASK_ID) LongParameter taskId
			)
	{
		ContentAnalysisTestUtils.rescheduleVideoTask(backendId.getValues(), taskId.getValue());
	}
	
	/**
	 * 
	 * @param identity
	 * @param backendId 
	 * @param enabled 
	 */
	@HTTPServiceMethod(name = "enableBackend")
	public void enableBackend(
			@HTTPAuthenticationParameter(showLoginPrompt=true) AuthenticationParameter identity,
			@HTTPMethodParameter(name = Definitions.PARAMETER_BACKEND_ID) IntegerParameter backendId,
			@HTTPMethodParameter(name = "enabled") BooleanParameter enabled
			)
	{
		ContentAnalysisTestUtils.enableBackend(backendId.getValue(), enabled.getValue());
	}
	
	/**
	 * @param identity
	 * @param albumId
	 * @param includeGUIDs
	 * @param resolveNames 
	 * @return list of picasa albums
	 */
	@HTTPServiceMethod(name = "getPicasaAlbums")
	public Response getPicasaAlbums(
			@HTTPAuthenticationParameter AuthenticationParameter identity,
			@HTTPMethodParameter(name = "album_id", required = false) StringParameter albumId,
			@HTTPMethodParameter(name = "include_uids", defaultValue = "false", required = false) BooleanParameter includeGUIDs,
			@HTTPMethodParameter(name = "resolve_names", defaultValue = "true", required = false) BooleanParameter resolveNames
			)
	{
		return new Response(ContentAnalysisTestUtils.getPicasaAlbums(identity.getUserIdentity(), albumId.getValues(), includeGUIDs.isTrue(), resolveNames.isTrue()));
	}
	
	/**
	 * 
	 * @param identity
	 * @param guid
	 * @return url redirection to picasa
	 */
	@HTTPServiceMethod(name = "getPicasaUri")
	public Response getPicasaUri(
			@HTTPAuthenticationParameter AuthenticationParameter identity,
			@HTTPMethodParameter(name = "guid") StringParameter guid
			)
	{
		return new RedirectResponse(ContentAnalysisTestUtils.getPicasaUri(identity.getUserIdentity(), guid.getValue()));
	}
	
	/**
	 * 
	 * @param identity
	 * @param backendId 
	 * @param dataGroups optional data groups
	 * @param url
	 * @return response
	 */
	@HTTPServiceMethod(name = "analyzePage")
	public Response analyzePage(
			@HTTPAuthenticationParameter(showLoginPrompt=true) AuthenticationParameter identity,
			@HTTPMethodParameter(name = Definitions.PARAMETER_BACKEND_ID, required=false) IntegerParameter backendId,
			@HTTPMethodParameter(name = DataGroups.PARAMETER_DEFAULT_NAME, required = false, defaultValue=ContentAnalysisTestUtils.DATA_GROUPS_HREF) DataGroups dataGroups,
			@HTTPMethodParameter(name = "url") StringParameter url
			)
	{
		if(ContentAnalysisTestUtils.analyzePage(identity.getUserIdentity(), backendId.getValues(), dataGroups, url.getValue())){
			return new Response();
		}else{
			return new Response(Status.BAD_REQUEST);
		}
	}
	
	/**
	 * 
	 * @param identity
	 * @param file
	 * @return the extracted words in the message
	 */
	@HTTPServiceMethod(name = "fuzzyFile")
	public Response fuzzyFile(
			@HTTPAuthenticationParameter(showLoginPrompt=true) AuthenticationParameter identity,
			@HTTPMethodParameter(name = InputStreamParameter.PARAMETER_DEFAULT_NAME, bodyParameter=true) InputStreamParameter file
			)
	{
		return new Response(Status.OK, ContentAnalysisTestUtils.fuzzyFile(file.getValue()));
	}
	
	/**
	 * 
	 * @param identity
	 * @param url
	 * @return the extracted words in the message
	 */
	@HTTPServiceMethod(name = "fuzzyUrl")
	public Response fuzzyUrl(
			@HTTPAuthenticationParameter(showLoginPrompt=true) AuthenticationParameter identity,
			@HTTPMethodParameter(name = "url") StringParameter url
			)
	{
		return new Response(Status.OK, ContentAnalysisTestUtils.fuzzyUrl(url.getValue()));
	}
}
