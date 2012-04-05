/*
 *  Copyright (c) 2005-2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.registry.indexing.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.registry.common.ResourceData;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.indexing.IndexingManager;
import org.wso2.carbon.registry.indexing.Utils;
import org.wso2.carbon.registry.indexing.indexer.IndexerException;
import org.wso2.carbon.registry.indexing.service.ContentBasedSearchService;
import org.wso2.carbon.registry.indexing.service.ContentSearchService;
import org.wso2.carbon.registry.indexing.service.SearchResultsBean;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @scr.component name="org.wso2.carbon.registry.indexing" immediate="true"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService" cardinality="1..1"
 * policy="dynamic" bind="setRegistryService" unbind="unsetRegistryService"
 */
public class IndexingServiceComponent {

    /**
     * This class is the bridge between Carbon and Indexing code
     */

    private static Log log = LogFactory.getLog(IndexingServiceComponent.class);

    private Registry registry = null;
    private boolean initialized = false;
    private ServiceRegistration serviceRegistration;

    protected void activate(ComponentContext context) {
        serviceRegistration = context.getBundleContext().registerService(
                ContentSearchService.class.getName(), new ContentSearchServiceImpl(), null);
        log.debug("******* Registry Indexing bundle is activated ******* ");
    }

    protected void deactivate(ComponentContext context) {
        if (serviceRegistration != null) {
            serviceRegistration.unregister();
            serviceRegistration = null;
        }
        log.debug("******* Registry Indexing bundle is deactivated ******* ");
    }

    protected void setRegistryService(RegistryService registryService) {
        Utils.setRegistryService(registryService);
        startIndexing();
    }

    protected void unsetRegistryService(RegistryService registryService) {
        stopIndexing();
        Utils.setRegistryService(null);
    }

    private void startIndexing() {
        IndexingManager.getInstance().startIndexing();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                IndexingManager.getInstance().stopIndexing(); // To be on the safe side
            }
        });
    }

    private void stopIndexing() {
        IndexingManager.getInstance().stopIndexing();
    }

    private static class ContentSearchServiceImpl implements ContentSearchService {

        public ResourceData[] search(UserRegistry registry, String query)
                throws RegistryException {
            SearchResultsBean resultsBean =
                    null;
            try {
                resultsBean = new ContentBasedSearchService().searchContent(query, registry);
            } catch (IndexerException e) {
                throw new RegistryException("Unable to obtain an instance of a Solr client", e);
            }
            String errorMessage = resultsBean.getErrorMessage();
            if (errorMessage != null) {
                throw new RegistryException(errorMessage);
            }
            return resultsBean.getResourceDataList();
        }

        public ResourceData[] search(int tenantId, String query)
                throws RegistryException {
            return search(Utils.getRegistryService().getRegistry(
                    CarbonConstants.REGISTRY_SYSTEM_USERNAME, tenantId), query);
        }

        public ResourceData[] search(String query) throws RegistryException {
            return search(MultitenantConstants.SUPER_TENANT_ID, query);
        }
    }

   /* @Deprecated
    private void initailize() {
        if(initialized){
            return;
        }else{
            try {
                initialized = true;
                RegistryService registryService = Utils.getRegistryService();
                // We can't get Registry from Utils, as the MessageContext is not available at
                // activation time.
                Registry userRegistry = registryService.getUserRegistry();
                if (registry != null && registry == userRegistry) {
                    log.info("Handler has already been set.");
                    return;
                }
                registry = userRegistry;
                if (registry == null ||
                        registry.getRegistryContext() == null ||
                        registry.getRegistryContext().getHandlerManager() == null) {
                    String msg = "Error Initializing Registry Eventing Handler";
                    log.error(msg);
                } else {
                    IndexingHandler handler = new IndexingHandler();

                    Filter filter = new Filter() {

                        @Override
                        public boolean handleRename(RequestContext requestContext) throws RegistryException {
                            return true;
                        }

                        @Override
                        public boolean handleCopy(RequestContext requestContext) throws RegistryException {
                            return true;
                        }

                        @Override
                        public boolean handleMove(RequestContext requestContext) throws RegistryException {
                            return true;
                        }

                        @Override
                        public boolean handleDelete(RequestContext arg0)
                                throws RegistryException {
                            return true;
                        }

                        @Override
                        public boolean handleGet(RequestContext arg0)
                                throws RegistryException {
                            return false;
                        }

                        @Override
                        public boolean handleImportChild(RequestContext arg0)
                                throws RegistryException {
                            return false;
                        }

                        @Override
                        public boolean handleImportResource(RequestContext arg0)
                                throws RegistryException {
                            return true;
                        }

                        @Override
                        public boolean handlePut(RequestContext arg0)
                                throws RegistryException {
                            return true;
                        }

                        @Override
                        public boolean handlePutChild(RequestContext arg0)
                                throws RegistryException {
                            return false;
                        }

                        @Override
                        public boolean handleSearchContent(
                                RequestContext requestContext)
                                throws RegistryException {
                            return true;
                        }

                    };

                    registry.getRegistryContext().getHandlerManager().addHandler(null, filter, handler);
                    log.info("Successfully Initialized the Indexing Handler");
                }
            } catch (RegistryException e) {
                log.error(e);
            }
        }
    }
    */
}

