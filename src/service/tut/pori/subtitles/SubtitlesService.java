/**
 * Copyright 2015 Tampere University of Technology, Pori Department
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
package service.tut.pori.subtitles;

import service.tut.pori.subtitles.SubtitlesCore.FileFormat;
import service.tut.pori.subtitles.SubtitlesCore.SubtitleFormat;
import core.tut.pori.http.Response.Status;
import core.tut.pori.http.StringResponse;
import core.tut.pori.http.StringResponse.StringData;
import core.tut.pori.http.annotations.HTTPAuthenticationParameter;
import core.tut.pori.http.annotations.HTTPMethodParameter;
import core.tut.pori.http.annotations.HTTPService;
import core.tut.pori.http.annotations.HTTPServiceMethod;
import core.tut.pori.http.parameters.AuthenticationParameter;
import core.tut.pori.http.parameters.LongParameter;
import core.tut.pori.http.parameters.StringParameter;

/**
 * This service enables subtitle generation.
 * 
 * @see service.tut.pori.subtitles.reference.ClientService
 */
@HTTPService(name=Definitions.SERVICE_SUBS)
public class SubtitlesService {

	/**
	 * 
	 * @param authenticatedUser
	 * @param guid
	 * @param fileFormat
	 * @param subtitleFormat
	 * @param userIdFilter 
	 * @return the formatted subtitles
	 * @see service.tut.pori.subtitles.reference.ClientService#generateSubtitles(AuthenticationParameter, StringParameter, StringParameter, StringParameter, core.tut.pori.http.parameters.LongParameter)
	 */
	@HTTPServiceMethod(name = Definitions.METHOD_GENERATE_SUBTITLES, acceptedMethods={core.tut.pori.http.Definitions.METHOD_GET})
	public StringResponse generateSubtitles(
			@HTTPAuthenticationParameter(required = false) AuthenticationParameter authenticatedUser,
			@HTTPMethodParameter(name=service.tut.pori.contentanalysis.Definitions.PARAMETER_GUID) StringParameter guid,
			@HTTPMethodParameter(name=Definitions.PARAMETER_FILE_FORMAT) StringParameter fileFormat,
			@HTTPMethodParameter(name=Definitions.PARAMETER_SUBTITLE_FORMAT) StringParameter subtitleFormat,
			@HTTPMethodParameter(name=service.tut.pori.users.Definitions.PARAMETER_USER_ID, required = false) LongParameter userIdFilter
			)
	{
		StringData data = SubtitlesCore.generateSubtitles(authenticatedUser.getUserIdentity(), guid.getValue(), FileFormat.fromFormatString(fileFormat.getValue()), SubtitleFormat.fromFormatString(subtitleFormat.getValue()), userIdFilter.getValues());
		if(data == null){
			return new StringResponse(Status.BAD_REQUEST);
		}else{
			return new StringResponse(data);
		}
	}
}

