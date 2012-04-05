/*
 * Copyright (c) 2006, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.registry.indexing.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrException;
import org.wso2.carbon.registry.admin.api.indexing.IContentBasedSearchService;
import org.wso2.carbon.registry.common.ResourceData;
import org.wso2.carbon.registry.common.services.RegistryAbstractAdmin;
import org.wso2.carbon.registry.common.utils.CommonUtil;
import org.wso2.carbon.registry.core.ActionConstants;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.indexing.IndexingConstants;
import org.wso2.carbon.registry.indexing.IndexingManager;
import org.wso2.carbon.registry.indexing.indexer.IndexerException;
import org.wso2.carbon.registry.indexing.solr.SolrClient;
import org.wso2.carbon.registry.indexing.utils.IndexingUtils;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.utils.ServerConstants;

public class ContentBasedSearchService extends RegistryAbstractAdmin 
        implements IContentBasedSearchService {
	private static final Log log = LogFactory.getLog(ContentBasedSearchService.class);

	private String solrServerUrl;


	public String getSolrUrl(int tenantId)throws IOException,FileNotFoundException{
		if(solrServerUrl == null){
			solrServerUrl = IndexingUtils.getSolrUrl();
		}	
		return solrServerUrl;
	}


	public SearchResultsBean getContentSearchResults(String searchQuery) throws AxisFault{
        try {
			UserRegistry registry = (UserRegistry) getRootRegistry();
            return searchContent(searchQuery, registry);
		} catch (Exception e) {
			log.error("Error " + e.getMessage() + "at the content search back end component.", e );
		}
        return new SearchResultsBean();
	}

    public SearchResultsBean searchContent(String searchQuery,
                              UserRegistry registry) throws IndexerException, RegistryException {
        SearchResultsBean resultsBean = new SearchResultsBean();
        SolrClient client = SolrClient.getInstance();
        SolrDocumentList results = client.query(searchQuery, registry.getTenantId());

        if (log.isDebugEnabled()) log.debug("result received "+ results);

        List<ResourceData> filteredResults = new ArrayList<ResourceData>();
        for (int i = 0;i < results.getNumFound();i++){
            SolrDocument solrDocument = results.get(i);
            String path = getPathFromId((String)solrDocument.getFirstValue("id"));
            //if (AuthorizationUtils.authorize(path, ActionConstants.GET)){
            if ((registry.resourceExists(path)) && (isAuthorized(registry,path, ActionConstants.GET))) {
                filteredResults.add(loadResourceByPath(registry, path));
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("filtered results "+ filteredResults + " for user "+ registry.getUserName());
        }

        resultsBean.setResourceDataList(filteredResults.toArray(new ResourceData[0]));

        return resultsBean;
    }

    public void restartIndexing() throws RegistryException {

        IndexingManager manager = IndexingManager.getInstance();
        manager.restartIndexing();
    }

	private String getPathFromId(String id) {
		return id.substring(0, id.lastIndexOf("tenantId"));
	}

	private boolean isAuthorized(UserRegistry registry, String resourcePath, String action) throws RegistryException{
		UserRealm userRealm = registry.getUserRealm();
		String userName = getLoggedInUserName();

		try {
			if (!userRealm.getAuthorizationManager().isUserAuthorized(userName,
					resourcePath, action)) {
				return false;
			}
		} catch (UserStoreException e) {
			throw new RegistryException("Error at Authorizing " + resourcePath
					+ " with user " + userName + ":" + e.getMessage(), e);
		}

		return true;
	}

	private ResourceData loadResourceByPath(UserRegistry registry, String path) throws RegistryException {
		ResourceData resourceData = new ResourceData();
		resourceData.setResourcePath(path);

		if (path != null) {
			if (RegistryConstants.ROOT_PATH.equals(path)) {
				resourceData.setName("root");
			} else {
				String[] parts = path.split(RegistryConstants.PATH_SEPARATOR);
				resourceData.setName(parts[parts.length - 1]);
			}
		}

		Resource child = registry.get(path);

		resourceData.setResourceType(child instanceof Collection ? "collection"
				: "resource");
		resourceData.setAuthorUserName(child.getAuthorUserName());
		resourceData.setDescription(child.getDescription());
		resourceData.setAverageRating(registry
				.getAverageRating(child.getPath()));
		Calendar createdDateTime = Calendar.getInstance();
		createdDateTime.setTime(child.getCreatedTime());
		resourceData.setCreatedOn(createdDateTime);
		CommonUtil.populateAverageStars(resourceData);

		child.discard();

		return resourceData;
	}

	public static String getLoggedInUserName(){
		MessageContext messageContext = MessageContext.getCurrentMessageContext();
		HttpServletRequest request = 
			(HttpServletRequest) messageContext.getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
		String userName = (String) request.getSession().getAttribute(ServerConstants.USER_LOGGED_IN);
		return userName;
	}

}
