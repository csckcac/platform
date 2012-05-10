package org.wso2.carbon.bam.analyzer.service;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.analyzer.Utils;
import org.wso2.carbon.bam.analyzer.analyzers.IndexingAnalyzer;
import org.wso2.carbon.bam.analyzer.engine.Analyzer;
import org.wso2.carbon.bam.analyzer.engine.AnalyzerConfigConstants;
import org.wso2.carbon.bam.analyzer.engine.AnalyzerException;
import org.wso2.carbon.bam.analyzer.engine.AnalyzerSequence;
import org.wso2.carbon.bam.analyzer.task.BAMTaskInfo;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.core.multitenancy.SuperTenantCarbonContext;
import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;

import javax.xml.stream.XMLStreamException;
import java.util.*;

/**
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Obtains the index and analyzer configurations from the front end and store it the configuration
 * registry in a tenant aware manner.
 * <p/>
 * Registry structure to store the configurations is as below.
 * <p/>
 * <p/>
 * Super Tenant Registry :
 * <p/>
 * /_system/config
 * |
 * /components/org.wso2.carbon.bam.analyzer [Analyzer Configuration Root]
 * |
 * /analyzers [Collection holding analyzer sequence resources]
 * |   |
 * |   analyzerConfig_1..N (Analyzer sequences of super tenant)
 * |
 * /tenantTracker [Holds tenant list having configurations in the registry]
 * |   |
 * |   /analyzerTracker [Holds tenants having analyzer sequence configurations]
 * |       Tenants-Property (Property holding the tenant list)
 * |   /indexTracker    [Holds tenants having index configurations]
 * |       Tenants-Property (Property holding the tenant list)
 * |
 * /indexes [Holds index configurations xml]
 * |
 * /connection
 * Username-Property
 * Password-Property
 * <p/>
 * <p/>
 * Each Tenant Registry :
 * <p/>
 * /_system/config
 * |
 * /components/org.wso2.carbon.bam.analyzer [Analyzer Configuration Root]
 * |
 * /analyzers [Collection holding analyzer sequence resources]
 * |   |
 * |   analyzerConfig_1..N (Analyzer sequences of the tenant)
 * |
 * /indexes [Holds index configuration xml]
 * |
 * /connection
 * Username-Property
 * Password-Property
 */
public class AnalyzerMgtService extends AbstractAdmin {

    private static final Log log = LogFactory.getLog(AnalyzerMgtService.class);

    public boolean addTask(String analyzerXML) throws AnalyzerException {
        int tenantId = CarbonContext.getCurrentContext().getTenantId();
        try {
            UserRegistry configSystemRegistry = Utils.getRegistryService().getConfigSystemRegistry(tenantId);
            OMElement analyzerEl = AXIOMUtil.stringToOM(analyzerXML);
            AnalyzerSequence analyzerSequence = Utils.getAnalyzerSequence(tenantId, analyzerEl);
            Resource analyzerSeqResource = configSystemRegistry.newResource();
            analyzerSeqResource.setContent(analyzerXML);
            analyzerSeqResource.setMediaType("text/xml");
            configSystemRegistry.put(AnalyzerConfigConstants.analyzerParentRegistryPath +
                                     AnalyzerConfigConstants.analyzers +
                                     analyzerSequence.getName(), analyzerSeqResource);
            
            updateTenantTracker(AnalyzerConfigConstants.ANALYZER_TRACKER);

/*            UserRegistry superTenantRegistry = Utils.getRegistryService().
                    getConfigSystemRegistry(SuperTenantCarbonContext.
                            getCurrentContext(getAxisConfig()).getTenantId());

            Resource tenantTrackerResource = null;
            if (superTenantRegistry.resourceExists(AnalyzerConfigConstants.TENANT_TRACKER_PATH)) {
                tenantTrackerResource = superTenantRegistry.get(
                        AnalyzerConfigConstants.TENANT_TRACKER_PATH +
                        AnalyzerConfigConstants.INDEX_TRACKER);
                List<String> propertyValues = tenantTrackerResource.getPropertyValues(AnalyzerConfigConstants.TENANTS_PROPERTY);
                if (propertyValues == null) {
                    propertyValues = new ArrayList<String>();
                }
                propertyValues.add(String.valueOf(tenantId));
            } else {
                tenantTrackerResource = superTenantRegistry.newResource();
                ArrayList<String> propertyValues = new ArrayList<String>();
                propertyValues.add(String.valueOf(tenantId));
                tenantTrackerResource.setProperty(AnalyzerConfigConstants.TENANTS_PROPERTY, propertyValues);
            }
            superTenantRegistry.put(AnalyzerConfigConstants.TENANT_TRACKER_PATH +
                                    AnalyzerConfigConstants.INDEX_TRACKER, tenantTrackerResource);*/
            Map<String, String> credentials = org.wso2.carbon.bam.core.utils.Utils.
                    getConnectionParameters();
            /* Populating BAM task configuration data  */
            BAMTaskInfo taskInfo = new BAMTaskInfo();
            taskInfo.setAnlyzerSequence(analyzerSequence);
            taskInfo.setCredentials(credentials);
            taskInfo.setAnalyzerSeqXML(analyzerEl);

            Utils.getEngine().startAnalyzerSequence(taskInfo);

            log.info("Added analyzer sequence : " + analyzerSequence.getName() + " by tenant : " +
                    tenantId);
        } catch (RegistryException e) {
            String message = "Error adding analyzer config to registry for tenant : "
                             + tenantId;
            log.error(message, e);
            throw new AnalyzerException(message);

        } catch (XMLStreamException e) {
            String message = "Error parsing analyzer configuration for tenant : "
                             + tenantId;
            log.error(message, e);
            throw new AnalyzerException(message);

        } catch (Exception e) {
            String message = "Error while scheduling analyzer task for tenant : " + tenantId;
            log.error(message, e);
            throw new AnalyzerException(message);
        }

        return true;
    }

    public boolean editTask(String analyzerXML) throws AnalyzerException {
        deleteTask(analyzerXML);
        addTask(analyzerXML);
        // TODO: Think how to refresh cursors related to this sequence. Ideally there should be two
        // options.
        // 1. Allow user to specify whether to keep old cursors
        // 2. By default keep old cursors
        // This depends on whether the analyzerSequence name is changed or position of get analyzer
        // is changed. Ideally analyzerSequence name should be immutable once created so this and also
        // new position in analyzer is should start with new cursor. So this would not be a problem. 
        return true;
    }

    public boolean deleteTask(String analyzerXML) throws AnalyzerException {
        int tenantId = CarbonContext.getCurrentContext().getTenantId();
        try {
            UserRegistry configSystemRegistry = Utils.getRegistryService().getConfigSystemRegistry(tenantId);
            OMElement omElement = AXIOMUtil.stringToOM(analyzerXML);
            AnalyzerSequence analyzerSequence = Utils.getAnalyzerSequence(tenantId, omElement);
            Resource analyzerSeqResource = configSystemRegistry.newResource();
            analyzerSeqResource.setContent(analyzerXML);

            if (!configSystemRegistry.resourceExists(AnalyzerConfigConstants.analyzerParentRegistryPath +
                                                     AnalyzerConfigConstants.analyzers +
                                                     analyzerSequence.getName())) {
                String message = "Analyzer sequence does not exist.";
                log.error(message);
                throw new AnalyzerException(message);
            }

            configSystemRegistry.delete(AnalyzerConfigConstants.analyzerParentRegistryPath +
                                        AnalyzerConfigConstants.analyzers +
                                        analyzerSequence.getName());

            Utils.getEngine().deleteAnalyzerSequence(analyzerSequence, tenantId);

            for (Analyzer analyzer : analyzerSequence.getAnalyzers()) {
                if (analyzer instanceof IndexingAnalyzer) {
                    //((IndexingAnalyzer) analyzer).deleteCursor(tenantId); // TODO : Look in to this.
                }
            }

        } catch (RegistryException e) {
            String message = "Error adding analyzer config to registry for tenant : "
                             + tenantId;
            log.error(message, e);
            throw new AnalyzerException(message);

        } catch (XMLStreamException e) {
            String message = "Error parsing analyzer configuration for tenant : "
                             + tenantId;
            log.error(message, e);
            throw new AnalyzerException(message);

        } catch (TaskException e) {
            String message = "Unable to delete analyser sequence";
            log.error(message, e);
            throw new AnalyzerException(message);
        }
        return true;
    }

    public String[] getAnalyzerXMLs() throws AnalyzerException {
        int tenantId = CarbonContext.getCurrentContext().getTenantId();
        List<String> analyzerSeqXML = Utils.getEngine().getAnalyzerSeqXMLs(tenantId);
        return analyzerSeqXML.toArray(new String[analyzerSeqXML.size()]);
    }

    public String getAnalyzerXML(String analyzerSeqName) throws AnalyzerException {
        int tenantId = CarbonContext.getCurrentContext().getTenantId();
        return Utils.getEngine().getAnalyzerSeqXML(tenantId, analyzerSeqName);
    }

    public boolean pauseTask(String analyzerXML) {
        return true;
    }

    private void updateTenantTracker(String trackerType) throws RegistryException {
        int tenantId = CarbonContext.getCurrentContext().getTenantId();

        ConfigurationContext superTenantContext = Utils.getConfigurationContextService().
                getServerConfigContext();
        UserRegistry superTenantRegistry = Utils.getRegistryService().
                getConfigSystemRegistry(SuperTenantCarbonContext.
                        getCurrentContext(superTenantContext).getTenantId());

        Resource tenantTrackerResource = null;

        String trackerPath = AnalyzerConfigConstants.TENANT_TRACKER_PATH + "/" +
                             trackerType;
        if (superTenantRegistry.resourceExists(trackerPath)) {
            tenantTrackerResource = superTenantRegistry.get(trackerPath);
            List<String> propertyValues = tenantTrackerResource.getPropertyValues(
                    AnalyzerConfigConstants.TENANTS_PROPERTY);
            if (propertyValues == null) {
                propertyValues = new ArrayList<String>();
            }
            propertyValues.add(String.valueOf(tenantId));
            //propertyValues.add("1");

            // Make sure that there is no duplication of tenant id's
            Set<String> uniqueProperties = new HashSet<String>();
            uniqueProperties.addAll(propertyValues);

            propertyValues.clear();
            for (String id : uniqueProperties) {
                propertyValues.add(id);
            }

            tenantTrackerResource.setProperty(AnalyzerConfigConstants.TENANTS_PROPERTY,
                                              propertyValues);
        } else {
            tenantTrackerResource = superTenantRegistry.newResource();
            List<String> propertyValues = new ArrayList<String>();
            propertyValues.add(String.valueOf(tenantId));

            // Make sure that there is no duplication of tenant id's
            Set<String> uniqueProperties = new HashSet<String>();
            uniqueProperties.addAll(propertyValues);

            propertyValues.clear();
            for (String id : uniqueProperties) {
                propertyValues.add(id);
            }

            tenantTrackerResource.setProperty(AnalyzerConfigConstants.TENANTS_PROPERTY,
                                              propertyValues);
        }

        superTenantRegistry.put(trackerPath, tenantTrackerResource);
    }

}
