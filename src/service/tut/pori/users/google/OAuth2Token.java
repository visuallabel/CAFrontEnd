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
package service.tut.pori.users.google;

import java.lang.reflect.Type;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.annotations.SerializedName;


/**
 * OAuth2 Token
 */
public class OAuth2Token {
	private static final Logger LOGGER = Logger.getLogger(OAuth2Token.class);
	@SerializedName(value=Definitions.JSON_NAME_OAUTH2_ACCESS_TOKEN)
	private String _accessToken = null;
	@SerializedName(value=Definitions.JSON_NAME_OAUTH2_EXPIRES_IN)
	private Date _expires = null;
	@SerializedName(value=Definitions.JSON_NAME_OAUTH2_REFRESH_TOKEN)
	private String _refreshToken = null;
	@SerializedName(value=Definitions.JSON_NAME_OAUTH2_TOKEN_TYPE)
	private String _type = null;
	
	/**
	 * 
	 * @return serializer capable of properly serializing an OAuth2Token
	 */
	public static Gson getTokenGSONSerializer(){
		GsonBuilder builder = new GsonBuilder();
		builder.setPrettyPrinting();
		
		JsonSerializer<Date> dateSerializer = new JsonSerializer<Date>() {	

			@Override
			public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
				return (src == null ? null : new JsonPrimitive(toExpiresIn(src)));
			}
		};	
		builder.registerTypeAdapter(Date.class, dateSerializer);
			
		builder.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {	// register custom adapter for Dates
			
			@Override
			public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
				return (json == null ? null : fromExpiresIn(json.getAsLong()));
			}
		});
		return builder.create();
	}
	
	/**
	 * 
	 * @param expiresIn in seconds
	 * @return the expiration date based on the given expiresIn or null if null value was passed
	 */
	public static Date fromExpiresIn(Long expiresIn){
		if(expiresIn == null){
			return null;
		}
		return new Date(System.currentTimeMillis()+expiresIn*1000);
	}
	
	/**
	 * 
	 * @param expirationDate
	 * @return seconds to the expiration date (always at least 0) as designated b the expirationDate or null if null passed
	 */
	public static Long toExpiresIn(Date expirationDate){
		if(expirationDate == null){
			return null;
		}
		long milliSecondsTo = expirationDate.getTime()-System.currentTimeMillis();	// expiration time somewhere in the future, so unix time is greater
		if(milliSecondsTo <= 0){	// the expiration time has passed
			return 0L;
		}else{
			return milliSecondsTo/1000;	// convert to seconds truncating if necessary
		}
	}
	
	/**
	 * Note: if you want to know if this token has expired, use isExpired() instead
	 * 
	 * Note: this does not validate the token stricly as defined by the OAuth2 spec
	 * (e.g. http://tools.ietf.org/html/draft-ietf-oauth-v2-31#section-4.2.2),
	 * This is generally because the draft seems to change ~7 times a year, and because
	 * there is an ambiguity on what to do on missing expires_in field. This class
	 * should be subclassed to provide a more accurate (provider specific) implementation
	 * if one is needed.
	 * 
	 * @return true if:
	 * 	- the token contains an accessToken, which is NOT expired OR
	 *  - ...the token contains an refreshToken
	 */
	public boolean isValid(){
		if(!StringUtils.isEmpty(_refreshToken)){
			return true;
		}else if(StringUtils.isEmpty(_accessToken)){
			return false;
		}else{
			return !(isExpired());
		}
	}
	
	/**
	 * Same as calling expiresIn(0)
	 * @see #expiresIn(long)
	 * 
	 * @return true if this token has expired
	 */
	public boolean isExpired(){
		return expiresIn(0);
	}
	
	/**
	 * This returns true if the token has already expired, expires in the the given timeframe or does not have a valid
	 * expiration time
	 * 
	 * @param time the timeframe in ms
	 * @return true if the token expires in the given timeframe
	 */
	public boolean expiresIn(long time){
		if(_expires == null){
			LOGGER.debug("No expiration date given.");
			return true;
		}else if(_expires.getTime() <= (System.currentTimeMillis()+time)){
			return true;
		}else{
			return false;
		}
	}

	/**
	 * @return the accessToken
	 */
	public String getAccessToken() {
		return _accessToken;
	}

	/**
	 * @param accessToken the accessToken to set
	 */
	public void setAccessToken(String accessToken) {
		_accessToken = accessToken;
	}

	/**
	 * @return the refreshToken
	 */
	public String getRefreshToken() {
		return _refreshToken;
	}

	/**
	 * @param refreshToken the refreshToken to set
	 */
	public void setRefreshToken(String refreshToken) {
		_refreshToken = refreshToken;
	}

	/**
	 * @return the expires
	 */
	public Date getExpires() {
		return _expires;
	}

	/**
	 * @param expires the expires to set
	 */
	public void setExpires(Date expires) {
		_expires = expires;
	}
	
	/**
	 * 
	 * @param expiresIn (in seconds)
	 */
	public void setExpiresIn(Long expiresIn){
		_expires = fromExpiresIn(expiresIn);
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return _type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		_type = type;
	}
}
