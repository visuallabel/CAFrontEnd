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

import service.tut.pori.users.UserCore;
import core.tut.pori.dao.clause.SQLClause.SQLType;
import core.tut.pori.dao.SQLDAO;
import core.tut.pori.users.UserIdentity;

/**
 * DAO for retrieving user details for an IP address.
 */
public class IPAuthenticationDAO extends SQLDAO {
	/* tables */
	private static final String TABLE_USERS_IP = DATABASE+".users_ip";
	/* columns */
	private static final String COLUMN_IP_ADDRESS = "ip_address";
	/* sql scripts */
	private static final String SQL_RESOLVE_USER_ID = "SELECT "+COLUMN_COUNT+", "+COLUMN_USER_ID+" FROM "+TABLE_USERS_IP+" WHERE "+COLUMN_IP_ADDRESS+"=?";
	private static final int[] SQL_RESOLVE_USER_ID_SQL_TYPES = {SQLType.STRING.toInt()};
	
	/**
	 * Makes a table lookup to determine if the given ip address have an associated user identity.
	 * This will NOT check for currently authenticated users' IP address-user identity relations.
	 * 
	 * @param ipAddress
	 * @return the user identity associated with the given ip address, or null if none
	 */
	public UserIdentity resolveUserIdentity(String ipAddress){
		Long userId = (Long) getJdbcTemplate().queryForMap(SQL_RESOLVE_USER_ID, new Object[]{ipAddress}, SQL_RESOLVE_USER_ID_SQL_TYPES).get(COLUMN_USER_ID);
		return (userId == null ? null : UserCore.getUserIdentity(userId));
	}
}
