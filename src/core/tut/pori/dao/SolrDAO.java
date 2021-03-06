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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Base class for SOLR DAOs.
 * 
 * Subclassing this class will automatically add the new class to DAOHandler, and will be retrievable
 * run-time from ServiceInitializer.getDAOHandler().getDAO(...)
 *
 */
public abstract class SolrDAO implements DAO, ApplicationContextAware{
	/** Specifies time to wait in milliseconds before invoking "soft commit" for Solr index. */
	public static final int SOLR_COMMIT_WITHIN = 1000;
	/** Maximum document count for SOLR queries */
	public static final int MAX_DOCUMENT_COUNT = Integer.MAX_VALUE;
	/** default value for solr id field */
	public static final String SOLR_FIELD_ID = "id";
	private Map<String, SimpleSolrTemplate> _templates = null;
	
	/**
	 * 
	 * @param beanId
	 * @return the SimpleSolrTemplate for the requested beanId or null if not found
	 */
	public SimpleSolrTemplate getSolrTemplate(String beanId){
		return (_templates == null ? null : _templates.get(beanId));
	}

	@Override
	public void setApplicationContext(ApplicationContext context) throws BeansException {
		Map<String, SolrClient> map = context.getBeansOfType(SolrClient.class);
		int size = map.size();
		Logger.getLogger(SolrDAO.class).debug("Found: "+size+" SolrServers for "+getClass().toString());
		if(size > 0){
			_templates = new HashMap<>(size);
			for(Entry<String, SolrClient> e : map.entrySet()){
				_templates.put(e.getKey(), new SimpleSolrTemplate(e.getValue()));	// create new templates for each dao instance, but share the same solrserver instance
			}
		}	// if
	}
}
