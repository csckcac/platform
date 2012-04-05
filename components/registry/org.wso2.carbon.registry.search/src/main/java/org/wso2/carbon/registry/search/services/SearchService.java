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

package org.wso2.carbon.registry.search.services;

import org.apache.abdera.model.Source;
import org.apache.axis2.AxisFault;
import org.apache.log4j.lf5.util.Resource;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.registry.admin.api.search.ISearchService;
import org.wso2.carbon.registry.common.ResourceData;
import org.wso2.carbon.registry.common.services.RegistryAbstractAdmin;
import org.wso2.carbon.registry.common.utils.RegistryUtil;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.CurrentSession;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.indexing.service.ContentBasedSearchService;
import org.wso2.carbon.registry.indexing.service.ContentSearchService;
import org.wso2.carbon.registry.search.Utils;
import org.wso2.carbon.registry.search.beans.AdvancedSearchResultsBean;
import org.wso2.carbon.registry.search.beans.CustomSearchParameterBean;
import org.wso2.carbon.registry.search.beans.MediaTypeValueList;
import org.wso2.carbon.registry.search.beans.SearchResultsBean;
import org.wso2.carbon.registry.search.services.utils.AdvancedSearchFilterActions;
import org.wso2.carbon.registry.search.services.utils.AdvancedSearchResultsBeanPopulator;
import org.wso2.carbon.registry.search.services.utils.CustomSearchParameterPopulator;
import org.wso2.carbon.registry.search.services.utils.SearchResultsBeanPopulator;

import java.util.*;


public class SearchService extends RegistryAbstractAdmin implements ISearchService<SearchResultsBean, AdvancedSearchResultsBean, CustomSearchParameterBean, MediaTypeValueList> {

    /* (non-Javadoc)
	 * @see org.wso2.carbon.registry.search.services.ISearchService#getSearchResults(java.lang.String, java.lang.String)
	 */
    public SearchResultsBean getSearchResults(String searchType, String criteria) throws RegistryException {    	
        RegistryUtils.recordStatistics(searchType, criteria);
        UserRegistry registry = (UserRegistry) getRootRegistry();
        return SearchResultsBeanPopulator.populate(registry, searchType, criteria);
    }

/*    public AdvancedSearchResultsBean getAdvancedSearchResults(String resourceName, String authorName, String updaterName,
                                                              String createdAfter, String createdBefore, String updatedAfter,
                                                              String updatedBefore, String tags, String commentWords,
                                                              String propertyName, String propertyValue, String content)
            throws RegistryException {
        UserRegistry registry = (UserRegistry) getRootRegistry();
        Registry configSystemRegistry = getConfigSystemRegistry();
        return AdvancedSearchResultsBeanPopulator.populate(configSystemRegistry, registry, resourceName,
                                                           authorName, updaterName, createdAfter, createdBefore, updatedAfter, updatedBefore, tags,
                                                           commentWords, propertyName, propertyValue, content);
    }*/

    //newly added method
    //this method is to get the custom search results

    /* (non-Javadoc)
	 * @see org.wso2.carbon.registry.search.services.ISearchService#getAdvancedSearchResults(org.wso2.carbon.registry.search.beans.CustomSearchParameterBean)
	 */
    public AdvancedSearchResultsBean getAdvancedSearchResults(CustomSearchParameterBean parameters) throws RegistryException {    	
        RegistryUtils.recordStatistics(parameters);
        AdvancedSearchResultsBean metaDataSearchResultsBean;
        UserRegistry registry = (UserRegistry) getRootRegistry();
        Registry configSystemRegistry = getConfigSystemRegistry();
        ContentSearchService contentSearchService = Utils.getContentSearchService();
        ResourceData[] resourceDatas=null;
        String[][] tempParameterValues = parameters.getParameterValues();

        for (int i = 0; i < tempParameterValues.length; i++) {
            if (tempParameterValues[i][0].equals("content") && tempParameterValues[i][1] != null && 
                    tempParameterValues[i][1].length() > 0) {
                resourceDatas = contentSearchService.search(registry, tempParameterValues[i][1]);
                metaDataSearchResultsBean = AdvancedSearchResultsBeanPopulator.populate(configSystemRegistry, registry, parameters);
                if (metaDataSearchResultsBean != null && resourceDatas != null && resourceDatas.length > 0) {
                    ResourceData[] resourceDataList = metaDataSearchResultsBean.getResourceDataList();
                    Map<String, ResourceData> resourceDataMap = new HashMap<String, ResourceData>();
                    if (resourceDataList != null && resourceDataList.length > 0) {
                        for (ResourceData resourceData : resourceDataList) {
                            resourceDataMap.put(resourceData.getResourcePath(), resourceData);
                        }
                        List<ResourceData> output = new ArrayList<ResourceData>();
                        for (ResourceData resourceData : resourceDatas) {
                            ResourceData entry = resourceDataMap.get(resourceData.getResourcePath());
                            if (entry != null) {
                                output.add(entry);
                            }
                        }
                        metaDataSearchResultsBean.setResourceDataList(output.toArray(new ResourceData[output.size()]));
                        return metaDataSearchResultsBean;
                    }
                }
                AdvancedSearchResultsBean advancedSearchResultsBean = new AdvancedSearchResultsBean();
                advancedSearchResultsBean.setResourceDataList(new ResourceData[0]);
                return advancedSearchResultsBean;
            }
        }
        return AdvancedSearchResultsBeanPopulator.populate(configSystemRegistry, registry, parameters);
    }

    /* (non-Javadoc)
	 * @see org.wso2.carbon.registry.search.services.ISearchService#getMediaTypeSearch(java.lang.String)
	 */
    public MediaTypeValueList getMediaTypeSearch(String mediaType) throws RegistryException{
        UserRegistry registry = (UserRegistry) getRootRegistry();
        return CustomSearchParameterPopulator.getMediaTypeParameterValues(registry, mediaType);
    }

    /* (non-Javadoc)
	 * @see org.wso2.carbon.registry.search.services.ISearchService#saveAdvancedSearchFilter(org.wso2.carbon.registry.search.beans.CustomSearchParameterBean, java.lang.String)
	 */
    public void saveAdvancedSearchFilter(CustomSearchParameterBean queryBean, String filterName) throws
                                                                                               RegistryException {
        UserRegistry configUserRegistry = (UserRegistry)getConfigUserRegistry();
        AdvancedSearchFilterActions.saveAdvancedSearchQueryBean(configUserRegistry, queryBean, filterName);
    }

    /* (non-Javadoc)
	 * @see org.wso2.carbon.registry.search.services.ISearchService#getAdvancedSearchFilter(java.lang.String)
	 */
    public CustomSearchParameterBean getAdvancedSearchFilter(String filterName) throws
                                                                              RegistryException {
        UserRegistry configUserRegistry = (UserRegistry)getConfigUserRegistry();
        return AdvancedSearchFilterActions.getAdvancedSearchQueryBean(configUserRegistry, filterName);
    }

    /* (non-Javadoc)
	 * @see org.wso2.carbon.registry.search.services.ISearchService#getSavedFilters()
	 */
    public String[] getSavedFilters() throws RegistryException {
        UserRegistry configUserRegistry = (UserRegistry)getConfigUserRegistry();
        return AdvancedSearchFilterActions.getSavedFilterNames(configUserRegistry);
    }

    public void deleteFilter(String filterName) throws RegistryException {
        UserRegistry configUserRegistry = (UserRegistry) getConfigUserRegistry();
        configUserRegistry.delete(RegistryConstants.PATH_SEPARATOR+"users"+RegistryConstants.PATH_SEPARATOR+CarbonContext.getCurrentContext().getUsername()+ RegistryConstants.PATH_SEPARATOR +"searchFilters" +RegistryConstants.PATH_SEPARATOR+filterName);
    }
}
