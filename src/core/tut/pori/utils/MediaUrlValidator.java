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
package core.tut.pori.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.EnumSet;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

/**
 * This class can be used to validate whether content given as an URL link contains image content.
 * 
 * The validation of the URL is done by downloading the first few bytes of the content and comparing the retrieved bytes to the list of known magic numbers.
 * 
 * The supported video formats for this validator are:
 * <ul>
 *  <li>GIF</li>
 *  <li>JPEG</li>
 *  <li>PNG</li>
 * </ul>
 * 
 * The supported photo formats for this validator are:
 * <ul>
 *  <li>AVI</li>
 *  <li>Flash Video</li>
 *  <li>MKV</li>
 *  <li>MPEG Video</li>
 *  <li>MOV</li>
 * </ul>
 * 
 * Note that the magic number validation is a simplified operation and no end bytes for file types will ever be checked, whether they denote the file type or not.
 * 
 */
public class MediaUrlValidator {
	/* media types */
	/** media type name for audio content */
	public static final String MEDIA_TYPE_AUDIO = "AUDIO";
	/** media type name for photo/image content */
	public static final String MEDIA_TYPE_PHOTO = "PHOTO";
	/** media type name for video content */
	public static final String MEDIA_TYPE_VIDEO = "VIDEO";
	/** media type name for unknown/unspecified content */
	public static final String MEDIA_TYPE_UNKNOWN = "UNKNOWN";
	private static final Logger LOGGER = Logger.getLogger(MediaUrlValidator.class);
	/** avi actually is of format 52 49 46 46 xx xx xx xx 41 56 49 20 4C 49 53 54 where xx denote file size, the 52 49 46 46 also denote other formats such as wave files, but we'll simply ignore this minor issue */
	private static final byte[] MAGIC_BYTE_AVI;
	private static final byte[] MAGIC_BYTE_BMP;
	private static final byte[] MAGIC_BYTE_FLASH_VIDEO;
	private static final byte[] MAGIC_BYTE_GIF_1;
	private static final byte[] MAGIC_BYTE_GIF_2;
	/** note: a valid jpeg file should also end with ffd9, but this is will not be checked by the validator */
	private static final byte[] MAGIC_BYTE_JPEG;
	private static final byte[] MAGIC_BYTE_MKV;
	/** mpeg file is actually 00 00 01 Bx where x is a number. Last byte "Bx" is ignored because Hex.decodeHex requires Even number of bytes */
	private static final byte[] MAGIC_BYTE_MPEG_VIDEO;
	private static final byte[] MAGIC_BYTE_MOV;
	private static final byte[] MAGIC_BYTE_PNG;
	static{
		try {
			MAGIC_BYTE_BMP = Hex.decodeHex("424d".toCharArray());
			MAGIC_BYTE_GIF_1 = Hex.decodeHex("474946383961".toCharArray());
			MAGIC_BYTE_GIF_2 = Hex.decodeHex("474946383761".toCharArray());
			MAGIC_BYTE_JPEG = Hex.decodeHex("ffd8".toCharArray());
			MAGIC_BYTE_PNG = Hex.decodeHex("89504e470d0a1a0a".toCharArray());
			MAGIC_BYTE_MOV = Hex.decodeHex("000000146674797071742020".toCharArray());
			MAGIC_BYTE_MPEG_VIDEO = Hex.decodeHex("000001".toCharArray());
			MAGIC_BYTE_MKV = Hex.decodeHex("1a45dfa3934282886d6174726f736b61".toCharArray());
			MAGIC_BYTE_FLASH_VIDEO = Hex.decodeHex("464c5601".toCharArray());
			MAGIC_BYTE_AVI = Hex.decodeHex("52494646".toCharArray());
		} catch (DecoderException ex) { // this should never happen
			LOGGER.error(ex,ex);
			throw new IllegalArgumentException(ex.getMessage());
		}
	}
	private static final int BUFFER_SIZE = 20; // take first 20 bytes in to the buffer

	/**
	 * Media type declaration.
	 */
	@XmlEnum
	public enum MediaType {
		/** media type is unknown or unspecified */
		@XmlEnumValue(value = MEDIA_TYPE_UNKNOWN)
		UNKNOWN(0),
		/** media is of photo/image content */
		@XmlEnumValue(value = MEDIA_TYPE_PHOTO)
		PHOTO(1),
		/** media is of video content */
		@XmlEnumValue(value = MEDIA_TYPE_VIDEO)
		VIDEO(2),
		/** media is of audio content */
		@XmlEnumValue(value = MEDIA_TYPE_AUDIO)
		AUDIO(3);
		
		private int _value;
		
		/**
		 * 
		 * @param value
		 */
		private MediaType(int value){
			_value = value;
		}
		
		/**
		 * 
		 * @return the media type as integer
		 */
		public int toInt(){
			return _value;
		}
		
		/**
		 * 
		 * @param value
		 * @return the value as MediaType
		 * @throws IllegalArgumentException on bad value
		 */
		public static MediaType fromInt(int value) throws IllegalArgumentException {
			for(MediaType mt : MediaType.values()){
				if(mt._value == value){
					return mt;
				}
			}
			throw new IllegalArgumentException("Bad "+MediaType.class.toString()+" : "+value);
		}
		
		/**
		 * 
		 * @param mediaTypes
		 * @return the passed media types converted to primitive int array or null if empty or null set was passed
		 */
		public static int[] toInt(EnumSet<MediaType> mediaTypes){
			if(mediaTypes == null || mediaTypes.isEmpty()){
				return null;
			}
			int[] types = new int[mediaTypes.size()];
			int index = -1;
			for(MediaType t : mediaTypes){
				types[++index] = t.toInt();
			}
			return types;
		}
	} // enum MediaType

	/**
	 * 
	 * @param array must be at least as long as the comparator
	 * @param with
	 * @return true if the given array starts with the given with
	 */
	private static boolean startsWith(byte[] array, byte[] with){
		for(int i=0; i<with.length; ++i){
			if(array[i] != with[i]){
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 
	 * @param url
	 * @return media type for the given URL
	 */
	public MediaType validateUrl(String url){
		if(StringUtils.isBlank(url)){
			LOGGER.warn("Empty URL.");
			return MediaType.UNKNOWN;
		}
		
		LOGGER.debug("Validating URL: "+url);	
		try {	
			URL u = new URL(url);
			try (InputStream input = u.openStream()){
				byte[] bytes = new byte[BUFFER_SIZE];
				if(input.read(bytes) < BUFFER_SIZE){
					LOGGER.warn("Failed to read first "+BUFFER_SIZE+" bytes.");
					return MediaType.UNKNOWN;
				}
				
				if(startsWith(bytes, MAGIC_BYTE_BMP)){
					LOGGER.debug("Detected a bmp file.");
					return MediaType.PHOTO;
				}else if(startsWith(bytes, MAGIC_BYTE_PNG)){
					LOGGER.debug("Detected a png file.");
					return MediaType.PHOTO;
				}else if(startsWith(bytes, MAGIC_BYTE_JPEG)){
					LOGGER.debug("Detected a jpeg file.");
					return MediaType.PHOTO;
				}else if(startsWith(bytes, MAGIC_BYTE_GIF_1) || startsWith(bytes, MAGIC_BYTE_GIF_2)){
					LOGGER.debug("Detected a gif file.");
					return MediaType.PHOTO;
				}else if(startsWith(bytes, MAGIC_BYTE_AVI)){
					LOGGER.debug("Detected an avi file.");
					return MediaType.VIDEO;
				}else if(startsWith(bytes, MAGIC_BYTE_FLASH_VIDEO)){
					LOGGER.debug("Detected a flash file.");
					return MediaType.VIDEO;
				}else if(startsWith(bytes, MAGIC_BYTE_MKV)){
					LOGGER.debug("Detected a mkv file.");
					return MediaType.VIDEO;
				}else if(startsWith(bytes, MAGIC_BYTE_MOV)){
					LOGGER.debug("Detected a mov file.");
					return MediaType.VIDEO;
				}else if(startsWith(bytes, MAGIC_BYTE_MPEG_VIDEO)){
					LOGGER.debug("Detected a mpg file.");
					return MediaType.VIDEO;
				}
			}
		} catch (IOException | IllegalArgumentException ex) { // java's URL connection randomly throws illegal argument exception on certain valid urls, so catch and ignore it		
			LOGGER.warn("Failed to read URL: "+url);
			LOGGER.debug(ex, ex);
		}
		return MediaType.UNKNOWN;
	}
}