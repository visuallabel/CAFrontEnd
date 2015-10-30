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
package core.tut.pori.dao;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrRequest.METHOD;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrException.ErrorCode;
import org.apache.solr.common.params.SolrParams;

/**
 * Simplified Solr Template. 
 *
 */
public class SimpleSolrTemplate{
	private static final Logger LOGGER = Logger.getLogger(SimpleSolrTemplate.class);
	private SolrClient _server = null;

	/**
	 * 
	 * @param solrServer
	 */
	public SimpleSolrTemplate(SolrClient solrServer) {
		_server = solrServer;
	}

	/**
	 * @param obj
	 * @return response
	 * @throws SolrException
	 */
	public UpdateResponse addBean(Object obj) throws SolrException {
		try {
			return _server.addBean(obj, SolrDAO.SOLR_COMMIT_WITHIN);
		} catch (IOException | SolrServerException ex) {
			LOGGER.error(ex, ex);
			throw new SolrException(ErrorCode.UNKNOWN, "Failed to add bean.");
		}
	}

	/**
	 * @param beans
	 * @return response
	 * @throws SolrException
	 */
	public UpdateResponse addBeans(Collection<?> beans) throws SolrException {
		try {
			return _server.addBeans(beans, SolrDAO.SOLR_COMMIT_WITHIN);
		} catch (SolrServerException | IOException ex) {
			LOGGER.error(ex, ex);
			throw new SolrException(ErrorCode.UNKNOWN, "Failed to add beans.");
		}
	}

	/**
	 * @param ids
	 * @return response
	 * @throws SolrException
	 * @see org.apache.solr.client.solrj.SolrClient#deleteById(java.util.List)
	 */
	public UpdateResponse deleteById(List<String> ids) throws SolrException {
		try {
			return _server.deleteById(ids, SolrDAO.SOLR_COMMIT_WITHIN);
		} catch (SolrServerException | IOException ex) {
			LOGGER.error(ex, ex);
			throw new SolrException(ErrorCode.UNKNOWN, "Failed to delete.");
		}
	}
	
	/**
	 * Overloaded convenience method for passing a Collection instead of a list.
	 * 
	 * @param ids
	 * @return response
	 * @throws SolrException
	 */
	public UpdateResponse deleteById(Collection<String> ids) throws SolrException {
		return deleteById(new ArrayList<>(ids));
	}

	/**
	 * Invokes a "hard commit" to Solr index. 
	 * Generally there should not be need for calling this because Solr cores should be configured to do &lt;autoCommit&gt;s on their own.
	 * See <a href="https://wiki.apache.org/solr/NearRealtimeSearch">Solr Near Realtime Search (NRT)</a> for more information.
	 * @return response
	 * @throws SolrException
	 */
	public UpdateResponse commit() throws SolrException {
		try {
			return _server.commit();
		} catch (SolrServerException | IOException ex) {
			LOGGER.error(ex, ex);
			throw new SolrException(ErrorCode.UNKNOWN, "Failed to add beans.");
		}
	}

	/**
	 * @param params
	 * @return response
	 * @throws SolrException
	 * @see org.apache.solr.client.solrj.SolrClient#query(org.apache.solr.common.params.SolrParams)
	 */
	public QueryResponse query(SolrParams params) throws SolrException {
		try {
			return _server.query(params, METHOD.POST);
		} catch (SolrServerException | IOException ex) {
			LOGGER.error(ex, ex);
			throw new SolrException(ErrorCode.UNKNOWN, "Query Failed.");
		}
	}
	
	/**
	 * 
	 * @param params
	 * @param fieldName
	 * @param cls
	 * @return All values for the given field or null if none
	 * @throws SolrException
	 */
	public <T> List<T> queryForObjects(SolrParams params, String fieldName, Class<T> cls) throws SolrException{
		try {
			return getObjects(_server.query(params, METHOD.POST), fieldName, cls);
		} catch (SolrServerException | IllegalArgumentException | IOException ex) {
			LOGGER.error(ex, ex);
			throw new SolrException(ErrorCode.UNKNOWN, "Query failed.");
		}
	}
	
	/**
	 * 
	 * @param response
	 * @param fieldName
	 * @param cls
	 * @return All values for the given field or null if none
	 * @throws IllegalArgumentException
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<T> getObjects(QueryResponse response, String fieldName, Class<T> cls) throws IllegalArgumentException{
		if(response == null){
			LOGGER.debug("Null response.");
			return null;
		}
		if(fieldName == null){
			throw new IllegalArgumentException("Field name was null.");
		}
		SolrDocumentList list = response.getResults();
		if(list.isEmpty()){
			LOGGER.debug("No documents.");
			return null;
		}
		
		List<T> results = new ArrayList<>(list.size());
		for(Iterator<SolrDocument> iter = list.iterator(); iter.hasNext();){
			Object o = iter.next().getFieldValue(fieldName);
			if(o == null){
				LOGGER.debug("Ignored null value for field: "+fieldName);
			}else{
				results.add((T) o); // ignore type-safety, the objects returned SHOULD be simple objects (not arrays or complex types), and even if they aren't the user should not pass invalid object class
			}
		}
		
		return (results.isEmpty() ? null : results);
	}
	
	/**
	 * @param params
	 * @param cls type of the beans
	 * @return the list of beans of the requested type or null if none was found
	 * @throws SolrException
	 */
	public <T> List<T> queryForList(SolrParams params, Class<T> cls) throws SolrException {
		try {
			return getList(_server.query(params, METHOD.POST), cls);
		} catch (SolrServerException | IOException ex) {
			LOGGER.error(ex, ex);
			throw new SolrException(ErrorCode.UNKNOWN, "Query Failed.");
		}
	}
	
	/**
	 * 
	 * @param response
	 * @param cls
	 * @return list of objects of the given class or null if none found in the class or the response is null
	 */
	public static <T> List<T> getList(QueryResponse response, Class<T> cls){
		if(response == null){
			LOGGER.debug("null response.");
			return null;
		}
		List<T> list = response.getBeans(cls);
		if(list == null || list.isEmpty()){
			return null;
		}else{
			return list;
		}
	}
}
