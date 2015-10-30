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
package service.tut.pori.contentstorage;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import service.tut.pori.users.OAuth2Token;
import service.tut.pori.users.google.GoogleCredential;
import service.tut.pori.users.google.GoogleUserCore;
import core.tut.pori.users.UserIdentity;
import core.tut.pori.utils.XMLFormatter;

/**
 * This is a simple client that provides high-level operations on the Picasa Web
 * Albums GData API. It can also be used as a command-line application to test
 * out some of the features of the API.
 * 
 * Note that even though this class is in principle thread-safe, it uses a single-connection http client internally, which means
 * that multiple method calls are not guaranteed to work at the same time.
 * 
 * To get user details (albums etc):
 *
 * https://picasaweb.google.com/data/feed/api/user/114407540644219368282 (or with e.g. /user/otula123)
 *
 *   - albums: in "entry" -elements: in gphoto:id the id of the album
 * 
 * 
 * to get album details:
 * 
 * https://picasaweb.google.com/data/feed/api/user/114407540644219368282/albumid/5789788743364446385 (or with otula123 
 *    and Private = name of the album)
 * 
 *    - photos: from entry, get link (rel="self", type="application/atom+xml", optionally: get id from gphoto:id
 * 
 * 
 * 
 * to get static access URL:
 * 
 * https://picasaweb.google.com/data/entry/api/user/114407540644219368282/albumid/5789788743364446385/photoid/
 *    5789788743072584594
 * 
 * 
 *    - in content -element (src), or in media:group/media:content (url)
 *    
 * 
 * NOTE: This client is may not work on private folders, it is recommended to use public folders, and because of buggy API on Google's side, there might be issues when retrieving more than 998 photos.
 */
public final class PicasawebClient implements Closeable {
	private static final String PARAMETER_FIELDS = "fields";
	private static final String PREFIX_GPHOTO = "gphoto";
	private static final String PREFIX_MEDIA = "media";
	private static final String ATTRIBUTE_SRC = "src";
	private static final String ELEMENT_CONTENT = "content";
	private static final String ELEMENT_ENTRY = "entry";
	private static final String ELEMENT_FEED = "feed";
	private static final String ELEMENT_ACCESS = "access";
	private static final String ELEMENT_ALBUM_ID = "albumid";
	private static final String ELEMENT_ID = "id";
	private static final String ELEMENT_GROUP = "group";
	private static final String ELEMENT_KEYWORDS = "keywords";
	private static final String ELEMENT_SUMMARY = "summary";
	private static final String ELEMENT_TITLE = "title";
	private static final String ELEMENT_UPDATED = "updated";
	private static final String FIELDS_GET_ALBUM_IDS = "?"+PARAMETER_FIELDS+"="+ELEMENT_ENTRY+"("+PREFIX_GPHOTO+":"+ELEMENT_ID+")";
	private static final String FIELDS_GET_PHOTO_ENTRIES = "?"+PARAMETER_FIELDS+"="+ELEMENT_ENTRY+"("+PREFIX_GPHOTO+":"+ELEMENT_ID+","+PREFIX_GPHOTO+":"+ELEMENT_ALBUM_ID+","+ELEMENT_UPDATED+","+ELEMENT_SUMMARY+","+ELEMENT_TITLE+","+PREFIX_GPHOTO+":"+ELEMENT_ACCESS+","+ELEMENT_CONTENT+","+PREFIX_MEDIA+":"+ELEMENT_GROUP+"("+PREFIX_MEDIA+":"+ELEMENT_KEYWORDS+"))";
	private static final String FIELDS_GET_URL = "?"+PARAMETER_FIELDS+"="+ELEMENT_CONTENT;
	private static final String GDATA_VERSION = "2";
	private static final String HEADER_AUTHRORIZATION = "Authorization";
	private static final String HEADER_GDATA_VERSION = "GData-Version";
	private static final Logger LOGGER = Logger.getLogger(PicasawebClient.class);
	private static final String NAMESPACE_ATOM = "http://www.w3.org/2005/Atom";
	private static final String NAMESPACE_GPHOTO = "http://schemas.google.com/photos/2007";
	private static final String NAMESPACE_MEDIA = "http://search.yahoo.com/mrss/";
	private static final String PICASA_DATA_HOST = "https://picasaweb.google.com/data";
	private static final String PICASA_PARAMETER_ALBUM_ID = "albumid";
	private static final String PICASA_PARAMETER_PHOTO_ID = "photoid";
	private static final String PICASA_URI_ENTRY = PICASA_DATA_HOST+"/entry/api/user/";
	private static final String PICASA_URI_FEED = PICASA_DATA_HOST+"/feed/api/user/";
	private static final String TOKEN_TYPE = "Bearer ";
	private CloseableHttpClient _client = null;
	private GoogleCredential _credential = null;
	private OAuth2Token _token = null;

	/**
	 * 
	 * @param credential
	 * @throws IllegalArgumentException on bad credentials
	 */
	public PicasawebClient(GoogleCredential credential) throws IllegalArgumentException{
		if(credential == null || StringUtils.isBlank(credential.getId())){
			throw new IllegalArgumentException("Invalid credential.");
		}
		_client = HttpClients.createDefault();
		_credential = credential;
		_token = GoogleUserCore.getToken(credential.getUserId());
		if(_token == null){
			throw new IllegalArgumentException("Could not retrieve access token.");
		}
	}

	/**
	 * 
	 * @param albumId
	 * @param picasaPhotoId
	 * @return static URL for the requested content or null if not found
	 * @throws IllegalArgumentException
	 */
	public String generateStaticUrl(String albumId, String picasaPhotoId) throws IllegalArgumentException{
		HttpGet get = new HttpGet(PICASA_URI_ENTRY+_credential.getId()+"/"+PICASA_PARAMETER_ALBUM_ID+"/"+albumId+"/"+PICASA_PARAMETER_PHOTO_ID+"/"+picasaPhotoId+FIELDS_GET_URL);
		setHeaders(get);
		try (CloseableHttpResponse response = _client.execute(get)) {
			InputStream body = getBody(response);
			if(body == null){
				LOGGER.warn("Failed to resolve static URL.");
				return null;
			}
			
			NodeList contentNodes = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(body).getElementsByTagName(ELEMENT_CONTENT);
			if(contentNodes.getLength() < 1){
				LOGGER.debug("No content found.");
				return null;
			}
			
			Node n = contentNodes.item(0).getAttributes().getNamedItem(ATTRIBUTE_SRC);
			if(n == null){
				LOGGER.warn("Element "+ELEMENT_CONTENT+" did not contain attribute "+ATTRIBUTE_SRC);
				return null;
			}
			
			return n.getNodeValue();
		} catch (IOException | SAXException | ParserConfigurationException ex) {
			LOGGER.error(ex, ex);
			throw new IllegalArgumentException("Could not generate URL for user: "+_credential.getId()+" with albumId: "+albumId+" and photoId: "+picasaPhotoId);
		}
	}

	/**
	 * 
	 * @param get
	 */
	private void setHeaders(HttpGet get) {
		get.setHeader(HEADER_GDATA_VERSION, GDATA_VERSION);
		String token = _token.getAccessToken();
		if(StringUtils.isBlank(token)){
			LOGGER.warn("No access token : limited to public access.");
		}else{
			get.setHeader(HEADER_AUTHRORIZATION, TOKEN_TYPE+token);
		}
	}

	/**
	 * 
	 * @return list of photos or null if none
	 */
	public List<PhotoEntry> getPhotos(){
		String picasaUserFeed = PICASA_URI_FEED+_credential.getId();
		HttpGet get = new HttpGet(picasaUserFeed+FIELDS_GET_ALBUM_IDS);	//get list of albums
		setHeaders(get);
		NodeList albumIdNodes = null;
		try (CloseableHttpResponse response = _client.execute(get)) {
			InputStream body = getBody(response);
			if(body == null){
				LOGGER.warn("Failed to retrieve photo albums.");
				return null;
			}
			
			albumIdNodes = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(body).getElementsByTagName(PREFIX_GPHOTO+":"+ELEMENT_ID);
		} catch (IOException | ParserConfigurationException | SAXException ex) {
			LOGGER.error(ex, ex);
			return null;
		}

		int nodeCount = albumIdNodes.getLength();
		if(nodeCount < 1){
			LOGGER.debug("No photo albums found for google user, id: "+_credential.getId());
			return null;
		}
		List<PhotoEntry> entries = new ArrayList<>(nodeCount);
		XMLFormatter formatter = new XMLFormatter();
		try {
			for(int i=0;i<nodeCount;++i){	//loop thru albums and get photos
				String albumId = albumIdNodes.item(i).getTextContent();
				get.setURI(new URI(picasaUserFeed+"/"+PICASA_PARAMETER_ALBUM_ID+"/"+albumId+FIELDS_GET_PHOTO_ENTRIES));
				try(CloseableHttpResponse response = _client.execute(get)){
					InputStream body = getBody(response);
					if(body == null){
						LOGGER.warn("Failed to retrieve album photos.");
						return null;
					}

					Feed feed = formatter.toObject(body, Feed.class);
					if(feed._entries == null || feed._entries.isEmpty()){
						LOGGER.debug("No entries for album, id: "+albumId);
						continue;
					}

					entries.addAll(feed._entries);
				} catch (IOException ex) {
					LOGGER.error(ex, ex);
					return null;
				}	
			} // for
		} catch (DOMException | URISyntaxException ex) {
			LOGGER.error(ex, ex);	
		}

		return (entries.isEmpty() ? null : entries);
	}

	/**
	 * 
	 * @param response
	 * @return response entity or null on non OK response or if no entity is present
	 */
	private InputStream getBody(CloseableHttpResponse response) {
		StatusLine sl = response.getStatusLine();
		int statusCode = sl.getStatusCode();
		if(statusCode < 200 || statusCode >= 300){
			LOGGER.warn("Server responded: "+statusCode+" "+sl.getReasonPhrase());
			return null;
		}
		HttpEntity entity = response.getEntity();
		if(entity == null){
			LOGGER.warn("No response entity provided.");
			return null;
		}
		try {
			return entity.getContent();
		} catch (IllegalStateException | IOException ex) {
			LOGGER.error(ex, ex);
			return null;
		}
	}

	/**
	 * 
	 * @return user identity set to this client or null if none
	 */
	public UserIdentity getUserIdentity(){
		return (_credential == null ? null : _credential.getUserId());
	}

	/**
	 * @see service.tut.pori.users.google.GoogleCredential#getId()
	 * 
	 * @return google user id
	 */
	public String getGoogleUserId() {
		return (_credential == null ? null : _credential.getId());
	}

	@Override
	public void close() throws IOException {
		_client.close();
	}

	/**
	 * Represents a single photo entry returned by picasa.
	 */
	@XmlRootElement(name=ELEMENT_ENTRY)
	@XmlAccessorType(XmlAccessType.NONE)
	public static class PhotoEntry{
		@XmlElement(name=ELEMENT_ACCESS, namespace=NAMESPACE_GPHOTO)
		private String _albumAccess = null;
		@XmlElement(name=ELEMENT_ALBUM_ID, namespace=NAMESPACE_GPHOTO)
		private String _albumId = null;
		@XmlElement(name=ELEMENT_ID, namespace=NAMESPACE_GPHOTO)
		private String _gphotoId = null;
		private List<String> _keywords = null;
		@XmlElement(name=ELEMENT_SUMMARY, namespace=NAMESPACE_ATOM)
		private String _summary = null;
		@XmlElement(name=ELEMENT_TITLE, namespace=NAMESPACE_ATOM)
		private String _title = null;
		@XmlElement(name=ELEMENT_UPDATED, namespace=NAMESPACE_ATOM)
		private Date _updated = null;
		private String _url = null;

		/**
		 * 
		 * @param content
		 */
		@XmlElement(name=ELEMENT_CONTENT, namespace=NAMESPACE_ATOM)
		private void setContent(Content content){
			if(content != null && !StringUtils.isBlank(content._src)){
				_url = content._src;
			}
		}

		/**
		 * 
		 * @param mediaGroup
		 */
		@XmlElement(name=ELEMENT_GROUP, namespace=NAMESPACE_MEDIA)
		private void setMediaGroup(MediaGroup mediaGroup){
			if(mediaGroup != null && !StringUtils.isBlank(mediaGroup._keywords)){
				String[] keywords = StringUtils.split(mediaGroup._keywords, core.tut.pori.http.Definitions.SEPARATOR_URI_QUERY_PARAM_VALUES);
				if(!ArrayUtils.isEmpty(keywords)){
					_keywords = new ArrayList<>(keywords.length);
					for(int i=0;i<keywords.length;++i){
						_keywords.add(keywords[i].trim());
					}
				} // if
			} // if
		}

		/**
		 * 
		 */
		private PhotoEntry(){
			// nothing needed
		}

		/**
		 * 
		 * @return album access permission string
		 */
		public String getAlbumAccess(){
			return _albumAccess;
		}

		/**
		 * 
		 * @return summary
		 */
		public String getSummary(){
			return _summary;
		}

		/**
		 * 
		 * @return title
		 */
		public String getTitle() {
			return _title;
		}

		/**
		 * 
		 * @return updated timestamp
		 */
		public Date getUpdated() {
			return _updated;
		}

		/**
		 * 
		 * @return list of keywords
		 */
		public List<String> getKeywords() {
			return _keywords;
		}

		/**
		 * 
		 * @return url
		 */
		public String getUrl() {
			return _url;
		}

		/**
		 * 
		 * @return album id
		 */
		public String getAlbumId() {
			return _albumId;
		}

		/**
		 * 
		 * @return google photo id
		 */
		public String getGphotoId() {
			return _gphotoId;
		}
	} // class PhotoEntry

	/**
	 * Represents a photo entry content element
	 *
	 */
	@XmlRootElement
	@XmlAccessorType(XmlAccessType.NONE)
	private static class Content {
		@XmlAttribute(name=ATTRIBUTE_SRC)
		private String _src = null;
	} // class PhotoEntryContent

	/**
	 * Represents a media group, which contains the user given keywords for a photo content
	 *
	 */
	@XmlRootElement
	@XmlAccessorType(XmlAccessType.NONE)
	private static class MediaGroup {
		@XmlElement(name=ELEMENT_KEYWORDS, namespace=NAMESPACE_MEDIA)
		private String _keywords = null;
	} // class PhotoEntryContent

	/**
	 * The photo feed
	 *
	 */
	@XmlRootElement(name=ELEMENT_FEED, namespace=NAMESPACE_ATOM)
	@XmlAccessorType(XmlAccessType.NONE)
	private static class Feed {
		@XmlElement(name=ELEMENT_ENTRY, namespace=NAMESPACE_ATOM)
		private List<PhotoEntry> _entries = null;
	} // class PhotoEntryContent
}
