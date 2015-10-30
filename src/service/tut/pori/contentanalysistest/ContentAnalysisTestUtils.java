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
package service.tut.pori.contentanalysistest;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import service.tut.pori.contentanalysis.AnalysisBackend;
import service.tut.pori.contentanalysis.AnalysisBackend.Capability;
import service.tut.pori.contentanalysis.AsyncTask.TaskStatus;
import service.tut.pori.contentanalysis.BackendDAO;
import service.tut.pori.contentanalysis.BackendStatus;
import service.tut.pori.contentanalysis.BackendStatusList;
import service.tut.pori.contentanalysis.CAContentCore;
import service.tut.pori.contentanalysis.PhotoDAO;
import service.tut.pori.contentanalysis.PhotoTaskDAO;
import service.tut.pori.contentanalysis.PhotoTaskDetails;
import service.tut.pori.contentanalysis.video.VideoContentCore;
import service.tut.pori.contentanalysis.video.VideoTaskDAO;
import service.tut.pori.contentanalysis.video.VideoTaskDetails;
import service.tut.pori.contentstorage.ContentStorageCore;
import service.tut.pori.facebookjazz.FacebookExtractor;
import service.tut.pori.facebookjazz.FacebookProfile;
import service.tut.pori.fuzzyvisuals.FuzzyAnalyzer;
import service.tut.pori.twitterjazz.TwitterExtractor;
import service.tut.pori.twitterjazz.TwitterExtractor.ContentType;
import service.tut.pori.twitterjazz.TwitterProfile;
import service.tut.pori.users.OAuth2Token;
import service.tut.pori.users.google.GoogleCredential;
import service.tut.pori.users.google.GoogleUserCore;
import core.tut.pori.context.ServiceInitializer;
import core.tut.pori.http.parameters.DataGroups;
import core.tut.pori.users.UserIdentity;


/**
 * A collection of various test methods, and more or less everything not yet (or never will be) part of the official specification/implementation.
 * One should NOT use these methods in any serious code.
 *
 */
@Deprecated
public final class ContentAnalysisTestUtils {
	/** data group for including href elements */
	public static final String DATA_GROUPS_HREF = "href";
	/** data group for including img elements */
	public static final String DATA_GROUPS_IMG = "img";
	/** data group for including video elements */
	public static final String DATA_GROUPS_VIDEO = "video";
	private static final String ATTRIBUTE_HREF = "href";
	private static final String ATTRIBUTE_SRC = "src";
	private static final Logger LOGGER = Logger.getLogger(ContentAnalysisTestUtils.class);
	private static final String SELECT_A_HREF = "a["+ATTRIBUTE_HREF+"]";
	private static final String SELECT_IMAGE_SRC = "img["+ATTRIBUTE_SRC+"]";
	private static final String SELECT_VIDEO_SRC = "video["+ATTRIBUTE_SRC+"]";
	private static final String SEPARATOR = ",";

	/**
	 * 
	 * 
	 */
	private ContentAnalysisTestUtils(){
		// nothing needed
	}

	/**
	 * 
	 * @param s can be a single capability or a list of capabilities separated by comma
	 * @return capability set parsed from the given string
	 * @throws IllegalArgumentException 
	 */
	public static EnumSet<Capability> fromCapabilityString(String s) throws IllegalArgumentException{
		if(StringUtils.isBlank(s)){
			return null;
		}
		EnumSet<Capability> capabilities = EnumSet.noneOf(Capability.class);
		String[] caps = s.split(SEPARATOR);
		for(int i=0;i<caps.length;++i){
			Capability capability = null;
			for(Capability c : Capability.values()){
				if(c.name().equalsIgnoreCase(caps[i].trim())){
					capability = c;
					break;
				}
			}
			if(capability == null){
				throw new IllegalArgumentException("Bad capability string: "+caps[i]);
			}
			capabilities.add(capability);
		}
		return capabilities;
	}

	/**
	 * 
	 * @param capabilityString
	 * @param description
	 * @param enabled
	 * @param taskDatagroups
	 * @param url
	 * @return id of the generated back-end
	 */
	public static Integer createAnalysisBackend(List<String> capabilityString, String description, Boolean enabled, DataGroups taskDatagroups, String url){
		AnalysisBackend end = new AnalysisBackend();
		end.setCapabilities(fromCapabilityString(org.apache.commons.lang3.StringUtils.join(capabilityString, ',')));
		if(enabled != null){
			end.setEnabled(enabled);
		}
		end.setDescription(description);
		end.setDefaultTaskDataGroups(taskDatagroups);
		end.setAnalysisUri(url);
		ServiceInitializer.getDAOHandler().getSQLDAO(BackendDAO.class).createBackend(end);
		return end.getBackendId();
	}

	/**
	 * 
	 * @param backendId
	 * @param capabilityString
	 * @param description
	 * @param enabled
	 * @param taskDatagroups
	 * @param url
	 */
	public static void modifyAnalysisBackend(Integer backendId, List<String> capabilityString, String description, Boolean enabled, DataGroups taskDatagroups, String url){
		AnalysisBackend end = new AnalysisBackend();
		end.setCapabilities(fromCapabilityString(org.apache.commons.lang3.StringUtils.join(capabilityString,',')));
		end.setEnabled(enabled);
		end.setDescription(description);
		end.setAnalysisUri(url);
		end.setBackendId(backendId);
		end.setDefaultTaskDataGroups(taskDatagroups);
		ServiceInitializer.getDAOHandler().getSQLDAO(BackendDAO.class).updateBackend(end);
	}

	/**
	 * 
	 * @param backendId
	 */
	public static void removeAnalysisBackend(Integer backendId){
		ServiceInitializer.getDAOHandler().getSQLDAO(BackendDAO.class).removeBackend(backendId);
	}

	/**
	 * 
	 * @param guid
	 * @param userId
	 * @return true if the user has access to the given GUID
	 */
	public static String hasAccess(String guid, UserIdentity userId){
		switch(ServiceInitializer.getDAOHandler().getSolrDAO(PhotoDAO.class).getAccessDetails(userId, guid).getPermission()){
			case NO_ACCESS:
				return "No access";
			case PRIVATE_ACCESS:
				return "Private access";
			case PUBLIC_ACCESS:
				return "Public access";
			default:
				break;
		}
		return "Unknown access mode";
	}

	/**
	 * 
	 * @param backendId 
	 * @return back-end details for the requested back-ends
	 */
	public static BackendDetails listAnalysisBackends(int[] backendId) {
		BackendDetails list = new BackendDetails();
		List<Integer> backendIds = (ArrayUtils.isEmpty(backendId) ? null : Arrays.asList(ArrayUtils.toObject(backendId)));
		list.setBackends(ServiceInitializer.getDAOHandler().getSQLDAO(BackendDAO.class).getBackends(backendIds));
		return list;
	}

	/**
	 * 
	 * @param dataGroups 
	 * @param userId
	 * @return facebook profile
	 * @throws IllegalArgumentException
	 */
	public static FacebookProfile extractFacebookProfile(DataGroups dataGroups, UserIdentity userId) throws IllegalArgumentException{
		FacebookExtractor e = FacebookExtractor.getExtractor(userId);
		if(e == null){
			throw new IllegalArgumentException("Failed to retrieve profile for the given user.");
		}else{
			return e.getProfile(facebookContentTypesFromDatagroups(dataGroups));
		}
	}

	/**
	 * 
	 * @param dataGroups 
	 * @param userId
	 * @return twitter profile
	 * @throws IllegalArgumentException
	 */
	public static TwitterProfile extractTwitterProfile(DataGroups dataGroups, UserIdentity userId) throws IllegalArgumentException{
		TwitterExtractor e = TwitterExtractor.getExtractor(userId);
		if(e == null){
			throw new IllegalArgumentException("Failed to retrieve profile for the given user.");
		}else{
			return e.getProfile(twitterContentTypesFromDatagroups(dataGroups));
		}
	}

	/**
	 * 
	 * @param dataGroups if contains all data group or empty is passed, all content types will be returned
	 * @return content types extracted from the given data groups or null if none was found or null data group list was given
	 */
	private static EnumSet<service.tut.pori.facebookjazz.FacebookExtractor.ContentType> facebookContentTypesFromDatagroups(DataGroups dataGroups){
		if(DataGroups.isEmpty(dataGroups) || DataGroups.hasDataGroup(DataGroups.DATA_GROUP_ALL, dataGroups)){
			return EnumSet.allOf(service.tut.pori.facebookjazz.FacebookExtractor.ContentType.class);
		}else{
			EnumSet<service.tut.pori.facebookjazz.FacebookExtractor.ContentType> retval = EnumSet.noneOf(service.tut.pori.facebookjazz.FacebookExtractor.ContentType.class);
			for(service.tut.pori.facebookjazz.FacebookExtractor.ContentType t : service.tut.pori.facebookjazz.FacebookExtractor.ContentType.values()){
				if(DataGroups.hasDataGroup(t.name(), dataGroups)){
					retval.add(t);
				}
			}
			if(retval.isEmpty()){
				return null;
			}else{
				return retval;
			}
		}
	}

	/**
	 * 
	 * @param dataGroups if contains all data group or empty is passed, all content types will be returned
	 * @return content types extracted from the given data groups or null if none was found or null or empty data group list was given
	 */
	private static EnumSet<ContentType> twitterContentTypesFromDatagroups(DataGroups dataGroups){
		if(DataGroups.isEmpty(dataGroups) || DataGroups.hasDataGroup(DataGroups.DATA_GROUP_ALL, dataGroups)){
			return EnumSet.allOf(ContentType.class);
		}else{
			EnumSet<ContentType> retval = EnumSet.noneOf(ContentType.class);
			for(ContentType t : ContentType.values()){
				if(DataGroups.hasDataGroup(t.name(), dataGroups)){
					retval.add(t);
				}
			}
			if(retval.isEmpty()){
				return null;
			}else{
				return retval;
			}
		}
	}

	/**
	 * 
	 * @param backendId
	 * @param taskId
	 * @throws IllegalArgumentException
	 */
	public static void reschedulePhotoTask(int[] backendId, Long taskId) throws IllegalArgumentException {
		PhotoTaskDAO taskDao = ServiceInitializer.getDAOHandler().getSQLDAO(PhotoTaskDAO.class);
		try{
			PhotoTaskDetails details = taskDao.getTask(null, null, null, taskId);

			BackendStatusList statuses = details.getBackends();
			if(BackendStatusList.isEmpty(statuses)){
				LOGGER.debug("No pre-existing statuses.");
				statuses = new BackendStatusList();
			}

			for(int i=0;i<backendId.length;++i){ // reset status information to not started
				statuses.setBackendStatus(new BackendStatus(new AnalysisBackend(backendId[i]), TaskStatus.NOT_STARTED));
			}
			taskDao.updateTaskStatus(statuses, taskId);

			CAContentCore.scheduleTask(details);
		} catch(ClassCastException ex){
			LOGGER.error(ex, ex);
			throw new IllegalArgumentException("Attempted to schedule task, which is not a photo analysis or feedback task.");
		}
	}

	/**
	 * 
	 * @param backendId
	 * @param taskId
	 * @throws IllegalArgumentException
	 */
	public static void rescheduleVideoTask(int[] backendId, Long taskId) throws IllegalArgumentException {
		VideoTaskDAO taskDao = ServiceInitializer.getDAOHandler().getSQLDAO(VideoTaskDAO.class);
		try{
			VideoTaskDetails details = taskDao.getTask(null, null, null, taskId);

			BackendStatusList statuses = details.getBackends();
			if(BackendStatusList.isEmpty(statuses)){
				LOGGER.debug("No pre-existing statuses.");
				statuses = new BackendStatusList();
			}

			for(int i=0;i<backendId.length;++i){ // reset status information to not started
				statuses.setBackendStatus(new BackendStatus(new AnalysisBackend(backendId[i]), TaskStatus.NOT_STARTED));
			}
			taskDao.updateTaskStatus(statuses, taskId);

			VideoContentCore.scheduleTask(details);
		} catch(ClassCastException ex){
			LOGGER.error(ex, ex);
			throw new IllegalArgumentException("Attempted to schedule task, which is not a photo analysis or feedback task.");
		}
	}

	/**
	 * 
	 * @param backendId
	 * @param enabled
	 * @throws IllegalArgumentException
	 */
	public static void enableBackend(Integer backendId, Boolean enabled) throws IllegalArgumentException {
		BackendDAO backendDAO = ServiceInitializer.getDAOHandler().getSQLDAO(BackendDAO.class);
		AnalysisBackend backend = backendDAO.getBackend(backendId);
		if(backend == null){
			throw new IllegalArgumentException("No such backend, id: "+backendId);
		}

		backend.setEnabled(enabled);
		backendDAO.updateBackend(backend);
	}

	/**
	 * 
	 * @param authenticatedUser
	 * @param guid 
	 * @return access uri to picasa
	 * @throws IllegalArgumentException on bad values
	 */
	public static String getPicasaUri(UserIdentity authenticatedUser, String guid) throws IllegalArgumentException{
		GoogleCredential gc = GoogleUserCore.getCredential(authenticatedUser);
		if(gc == null){
			throw new IllegalArgumentException("Failed to resolve Google credentials for the user.");
		}

		String googleUserId = gc.getId();
		Pair<String, String> ids = ServiceInitializer.getDAOHandler().getSQLDAO(PicasaAlbumDAO.class).getIdPair(googleUserId, guid);
		if(ids == null){
			throw new IllegalArgumentException("Could not find album for guid : "+guid);
		}

		Map<String, String> albums = resolvePicasaAlbumNames(authenticatedUser, googleUserId);
		if(albums == null){
			throw new IllegalArgumentException("Could not find album, id : "+ids.getLeft());
		}
		String name = albums.get(ids.getLeft());
		if(StringUtils.isBlank(name)){
			throw new IllegalArgumentException("Could not resolve name for album, id : "+ids.getLeft());
		}

		return "https://picasaweb.google.com/"+googleUserId+"/"+name+"#"+ids.getRight();
	}

	/**
	 * 
	 * @param authenticatedUser
	 * @param googleUserId
	 * @return map of album ids and names or null on error
	 */
	private static Map<String, String> resolvePicasaAlbumNames(UserIdentity authenticatedUser, String googleUserId) {
		OAuth2Token token = GoogleUserCore.getToken(authenticatedUser);
		if(token == null){
			throw new IllegalArgumentException("No active access token for the user.");
		}

		Map<String, String> albumIdNameMap = new HashMap<>();

		try(CloseableHttpClient client = HttpClients.createDefault()){
			HttpGet get = new HttpGet("https://picasaweb.google.com/data/feed/api/user/"+googleUserId+"?fields=entry(gphoto:id,gphoto:name)"); // retrieve only album names and IDs
			get.setHeader("GData-Version", "2");
			get.setHeader("Authorization", "Bearer "+token.getAccessToken());

			try(CloseableHttpResponse response = client.execute(get)){
				int statusCode = response.getStatusLine().getStatusCode();
				if(statusCode < 200 || statusCode >= 300){
					LOGGER.warn("Server responded with status: "+statusCode);
					return null;
				}

				NodeList entries = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(response.getEntity().getContent()).getElementsByTagName("entry");
				int entryCount = entries.getLength();
				if(entryCount < 1){
					LOGGER.warn("No albums in picasa service for user, id: "+authenticatedUser.getUserId());
					return null;
				}

				for(int i=0;i<entryCount;++i){
					Element entry = (Element) entries.item(i);
					String id = entry.getElementsByTagName("gphoto:id").item(0).getTextContent();
					if(StringUtils.isBlank(id)){
						LOGGER.warn("Ignored album with invalid id.");
						continue;
					}

					String name = entry.getElementsByTagName("gphoto:name").item(0).getTextContent();
					if(StringUtils.isBlank(name)){
						LOGGER.warn("Ignored album with invalid name, id: "+id);
					}else{
						albumIdNameMap.put(id, name);
					} // else
				} // for
			}
			return (albumIdNameMap.isEmpty() ? null : albumIdNameMap);
		} catch (IOException | SAXException | ParserConfigurationException ex) {
			LOGGER.error(ex, ex);
			return null;
		}
	}

	/**
	 * 
	 * @param authenticatedUser
	 * @param albumIds
	 * @param includeGUIDs
	 * @param resolveNames if true Picasa service is used to resolve album names
	 * @return list of albums or null if none available
	 * @throws IllegalArgumentException on bad data
	 */
	public static PicasaAlbumList getPicasaAlbums(UserIdentity authenticatedUser, List<String> albumIds, boolean includeGUIDs, boolean resolveNames) throws IllegalArgumentException {
		GoogleCredential gc = GoogleUserCore.getCredential(authenticatedUser);
		if(gc == null){
			throw new IllegalArgumentException("Failed to resolve Google credentials for the user.");
		}

		PicasaAlbumDAO dao = ServiceInitializer.getDAOHandler().getSQLDAO(PicasaAlbumDAO.class);
		String googleUserId = gc.getId();
		List<String> ids = dao.getAlbumIds(albumIds, googleUserId);
		if(ids == null){
			LOGGER.debug("No albums for user, id: "+authenticatedUser.getUserId());
			return null;
		}

		int albumIdCount = ids.size();
		Map<String, String> albumIdNameMap = null;
		if(resolveNames){
			LOGGER.debug("Resolving album names...");
			albumIdNameMap = resolvePicasaAlbumNames(authenticatedUser, googleUserId);
			if(albumIdNameMap == null){
				LOGGER.warn("Failed to resolve any album names.");
				return null;
			}
		}

		List<PicasaAlbum> albums = new ArrayList<>(albumIdCount);
		for(String id : ids){
			PicasaAlbum pa = new PicasaAlbum();		
			pa.setId(id);

			if(resolveNames){
				String name = albumIdNameMap.get(id);
				if(StringUtils.isBlank(name)){
					LOGGER.warn("Ignored album with invalid name: "+name+", album id: "+id);
					continue;
				}
				pa.setName(name);
			}

			if(includeGUIDs){
				List<String> GUIDs = dao.getGUIDs(id);
				if(GUIDs == null){ // this should really not happen, there should always be content
					LOGGER.warn("No GUIDs for album, id: "+id+" and user, id: "+authenticatedUser.getUserId());
				}
				pa.setGUIDs(GUIDs);
			}
			albums.add(pa);
		}

		PicasaAlbumList albumList = new PicasaAlbumList();
		albumList.setAlbums(albums);
		return albumList;
	}



	/**
	 * 
	 * @param authenticatedUser
	 * @param backendIds optional back-end id filter
	 * @param dataGroups optional datagroups, applicable values are: {@value #DATA_GROUPS_HREF}, {@value #DATA_GROUPS_IMG}, {@value #DATA_GROUPS_VIDEO}
	 * @param url
	 * @return true if the given uri was successfully parsed. Note that this does NOT mean that the uri had any applicable content.
	 */
	public static boolean analyzePage(UserIdentity authenticatedUser, int[] backendIds, DataGroups dataGroups, String url) {
		LOGGER.debug("Analyzing page : "+url);

		Document doc = null;
		try {
			doc = Jsoup.connect(url).get();
		} catch (IOException ex) {
			LOGGER.error(ex, ex);
			return false;
		}
		ArrayList<String> urls = new ArrayList<>();

		if(DataGroups.hasDataGroup(DATA_GROUPS_HREF, dataGroups)){
			Elements links = doc.select(SELECT_A_HREF);
			if(links.isEmpty()){
				LOGGER.debug("No "+DATA_GROUPS_HREF+" links in url : "+url);
			}else{
				for(org.jsoup.nodes.Element link : links){
					String aurl = link.absUrl(ATTRIBUTE_HREF);
					if(StringUtils.isBlank(aurl)){
						LOGGER.warn("Ignored empty "+ATTRIBUTE_HREF);
					}else{
						urls.add(aurl);
					}
				}
			}
		}

		if(DataGroups.hasDataGroup(DATA_GROUPS_IMG, dataGroups)){
			Elements links = doc.select(SELECT_IMAGE_SRC);
			if(links.isEmpty()){
				LOGGER.debug("No "+DATA_GROUPS_IMG+" links in url : "+url);
			}else{
				for(org.jsoup.nodes.Element link : links){
					String aurl = link.absUrl(ATTRIBUTE_SRC);
					if(StringUtils.isBlank(aurl)){
						LOGGER.warn("Ignored empty "+ATTRIBUTE_SRC);
					}else{
						urls.add(aurl);
					}
				}
			}
		}

		if(DataGroups.hasDataGroup(DATA_GROUPS_VIDEO, dataGroups)){
			Elements links = doc.select(SELECT_VIDEO_SRC);
			if(links.isEmpty()){
				LOGGER.debug("No "+DATA_GROUPS_VIDEO+" links in url : "+url);
			}else{
				for(org.jsoup.nodes.Element link : links){
					String aurl = link.absUrl(ATTRIBUTE_SRC);
					if(StringUtils.isBlank(aurl)){
						LOGGER.warn("Ignored empty "+ATTRIBUTE_SRC);
					}else{
						urls.add(aurl);
					}
				}
			}
		}

		if(urls.isEmpty()){
			LOGGER.warn("No applicable content in url : "+url);
			return false;
		}

		ContentStorageCore.addUrls(authenticatedUser, backendIds, urls);

		return true;
	}

	/**
	 * 
	 * @param file
	 * @return the extracted words as a string
	 */
	public static String fuzzyFile(InputStream file) {
		Set<String> words = null;
		try(FuzzyAnalyzer fa = new FuzzyAnalyzer()){
			words = fa.analyze(file);
		}
		
		return StringUtils.join(words, ',');
	}
	
	/**
	 * 
	 * @param url
	 * @return the extracted words as a string
	 */
	public static String fuzzyUrl(String url) {
		Set<String> words = null;
		try(FuzzyAnalyzer fa = new FuzzyAnalyzer()){
			words = fa.analyze(url);
		}
		
		return StringUtils.join(words, ',');
	}
}
