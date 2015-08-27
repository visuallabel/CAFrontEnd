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
package service.tut.pori.users.ip;

import org.apache.log4j.Logger;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import core.tut.pori.users.UserIdentity;

/**
 * A simple authentication provider which converts the given authentication to UsernamePasswordAuthenticationToken, setting UserIdentity, credentials and authorities
 *
 */
public class PreAuthenticationProvider implements AuthenticationProvider {
	private static final Logger LOGGER = Logger.getLogger(PreAuthenticationProvider.class);
	
	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		UserIdentity userIdentity = (UserIdentity) authentication.getPrincipal();
		LOGGER.debug("Granting access for user, id: "+userIdentity.getUserId());
		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(userIdentity, userIdentity.getPassword(), userIdentity.getAuthorities());
		return token;
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return authentication.isAssignableFrom(PreAuthenticatedAuthenticationToken.class);
	}

}
