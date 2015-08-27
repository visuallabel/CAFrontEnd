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
package service.tut.pori.fuzzyvisuals;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.StatusLine;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;

import core.tut.pori.context.ServiceInitializer;
import core.tut.pori.utils.HTTPHeaderUtil;

/**
 * Fuzzy content analyzer.
 * 
 * Note that this uses Google's Translation API scraped from the web page (<a href="https://translate.google.com/">Google Translator</a>).
 * Using the results for ANY official or commercial use is most likely against <a href="http://www.google.com/intl/en/policies/terms/">Google's Terms of Use</a>.
 * 
 * The code provided in this class is provided ONLY for testing purposes. See <a href="https://cloud.google.com/translate/docs">Translate API</a> for examples and documentation.
 * 
 * Note: this class is NOT thread-safe.
 * 
 */
public class FuzzyAnalyzer implements Closeable {
	private static final int CONTENT_BUFFER_SIZE = 1000;
	private static final String GOOGLE_TRANSLATE_URL = "https://translate.google.com/translate_a/single?client=t&sl=zh-CN&tl=en&hl=en&dt=t&ie=UTF-8&oe=UTF-8"; // for real use one should provide an API key
	private static final Logger LOGGER = Logger.getLogger(FuzzyAnalyzer.class);
	private static final String PARAMETER_Q = "q";
	private CloseableHttpClient _client = null;
	private Charset _utf16 = null;
	private Charset _utf8 = null;

	/**
	 * 
	 */
	public FuzzyAnalyzer(){
		_client = HttpClients.createDefault();
		_utf8 = Charset.forName("UTF-8");
		_utf16 = Charset.forName("UTF-16");
	}

	/**
	 * 
	 * @param input
	 * @return set of words or null if none was extracted
	 * @throws IllegalArgumentException on bad data
	 */
	public Set<String> analyze(InputStream input) throws IllegalArgumentException{
		if(input == null){
			throw new IllegalArgumentException("Invalid input : null");
		}
		
		try{
			byte[] array = new byte[CONTENT_BUFFER_SIZE];
			if(IOUtils.read(input, array) < 0){ // discard the first 1000 characters to get different results for each content, this is generally magic bytes for the file type
				throw new IllegalArgumentException("File is too small : less than "+(CONTENT_BUFFER_SIZE*2)+" bytes.");
			}

			IOUtils.read(input, array); // read the actual content

			String data = new String(array, _utf16); // convert to UTF-18 to get Chinese characters of the bytes
			LOGGER.debug("Converted to "+_utf16.name()+" : "+data);

			HttpPost post = new HttpPost(GOOGLE_TRANSLATE_URL);	
			List<BasicNameValuePair> parameters = new ArrayList<>(1);
			parameters.add(new BasicNameValuePair(PARAMETER_Q, data));
			post.setEntity(new UrlEncodedFormEntity(parameters, _utf8));
			
			LOGGER.debug("Calling "+GOOGLE_TRANSLATE_URL);
			try(CloseableHttpResponse r = _client.execute(post)){
				StatusLine l = r.getStatusLine();
				int status = l.getStatusCode();
				if(status < 200 || status >= 300){
					throw new IllegalArgumentException("Translation server error : "+status+" "+l.getReasonPhrase());
				}
				
				String[] words = StringUtils.split(IOUtils.toString(r.getEntity().getContent())); // the response is JSON, but we can simple split everything from whitespace
				if(ArrayUtils.isEmpty(words)){
					LOGGER.debug("No results.");
					return null;
				}
				
				HashSet<String> finalWords = new HashSet<>(words.length);
				for(int i=0;i<words.length;++i){
					if(words[i].length() > 3 && StringUtils.isAsciiPrintable(words[i]) && StringUtils.isAllLowerCase(words[i])){ // filter out everything not proper English words
						finalWords.add(words[i]);
					}
				}
				return finalWords;
			}
		} catch (IOException ex) {
			LOGGER.error(ex, ex);
		}
		return null;
	}

	/**
	 * 
	 * @param url
	 * @return set of words or null if none was extracted
	 * @throws IllegalArgumentException on bad data
	 */
	public Set<String> analyze(String url) throws IllegalArgumentException{
		HttpGet get = new HttpGet(url);
		FuzzyProperties fp = ServiceInitializer.getPropertyHandler().getSystemProperties(FuzzyProperties.class);
		String username = fp.getAuthUsername();
		if(username != null){ // checking either password or username is OK
			LOGGER.debug("Using authentication...");
			HTTPHeaderUtil.setHTTPBasicAuthHeader(get, username, fp.getAuthPassword());
		}
		
		LOGGER.debug("Calling GET "+url);
		try(CloseableHttpResponse r = _client.execute(get)){
			StatusLine l = r.getStatusLine();
			int status = l.getStatusCode();
			if(status < 200 || status >= 300){
				throw new IllegalArgumentException("Failed to retrieve file : "+status+" "+l.getReasonPhrase());
			}
			
			return analyze(r.getEntity().getContent());
		} catch (IOException ex) {
			LOGGER.error(ex, ex);
		}
		return null;
	}

	@Override
	public void close() {
		try {
			_client.close();
		} catch (IOException ex) {
			LOGGER.error(ex, ex);
		}
	}
}
