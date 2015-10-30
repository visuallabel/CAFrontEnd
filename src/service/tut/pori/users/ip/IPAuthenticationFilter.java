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
package service.tut.pori.users.ip;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;

import core.tut.pori.context.ServiceInitializer;
import core.tut.pori.users.UserIdentity;

/**
 * Provides user authentication solely by using an IP address lookup.
 */
public class IPAuthenticationFilter extends AbstractPreAuthenticatedProcessingFilter {
	private static final Logger LOGGER = Logger.getLogger(IPAuthenticationFilter.class);

	@Override
	protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
		return null;
	}

	@Override
	protected UserIdentity getPreAuthenticatedPrincipal(HttpServletRequest request) {
		LOGGER.debug("Authenticating user by IP address...");
		String ipAddress = request.getRemoteAddr();
		UserIdentity userIdentity = ServiceInitializer.getDAOHandler().getSQLDAO(IPAuthenticationDAO.class).resolveUserIdentity(ipAddress);
		if(userIdentity != null){
			LOGGER.debug("Granting permissions for user, id: "+userIdentity.getUserId()+" from IP address: "+ipAddress);
		}
		return userIdentity;
	}
}
