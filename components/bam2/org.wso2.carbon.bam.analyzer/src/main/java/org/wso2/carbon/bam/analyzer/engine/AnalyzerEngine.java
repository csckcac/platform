/**
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.bam.analyzer.engine;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.analyzer.Utils;
import org.wso2.carbon.bam.analyzer.analyzers.IndexingAnalyzer;
import org.wso2.carbon.bam.analyzer.analyzers.configs.IndexingConfig;
import org.wso2.carbon.bam.analyzer.task.BAMTaskInfo;
import org.wso2.carbon.bam.core.configurations.IndexConfiguration;
import org.wso2.carbon.bam.core.configurations.IndexingTaskConfiguration;
import org.wso2.carbon.bam.core.persistence.IndexManager;
import org.wso2.carbon.bam.core.persistence.IndexingTaskProvider;
import org.wso2.carbon.bam.core.persistence.MetaDataManager;
import org.wso2.carbon.bam.core.persistence.PersistencyConstants;
import org.wso2.carbon.bam.core.persistence.exceptions.ConfigurationException;
import org.wso2.carbon.bam.core.persistence.exceptions.IndexingException;
import org.wso2.carbon.core.multitenancy.SuperTenantCarbonContext;
import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.core.TaskInfo;
import org.wso2.carbon.ntask.core.TaskManager;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;

import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AnalyzerEngine implements IndexingTaskProvider {

    private static final Log log = LogFactory.getLog(AnalyzerEngine.class);

    private TaskManager taskManager;

    private ExecutorService loaderThreadPool = Executors.newSingleThreadExecutor();

    public static String CREDENTIALS = "credentials";

    public static String ANALYSER_SEQUENCE_NAME = "name";

    public static String TENANT_ID = "tenantId";

    public static String ANALYSER_SEQUENCE = "analyserSequence";

    public AnalyzerEngine(TaskManager taskManager)
            throws AnalyzerException {
        this.taskManager = taskManager;
        // Running this in a separate thread without blocking the server start-up since this operation
        // can become bulky
        ConfigurationLoader loader = new ConfigurationLoader();
        loaderThreadPool.submit(loader);

    }

    private class ConfigurationLoader implements Callable<Boolean> {
        public Boolean call() throws Exception {
//            loadIndexConfigurations();
            try {
                loadAnaylzers();
                loadIndexingTasks();
            } catch (Exception e) {
                log.error("Cannot load analyzer sequences", e);
            }
            return true;
        }
    }

    private boolean loadAnaylzers() throws AnalyzerException {
        Map<Integer, List<OMElement>> tenantsWithActiveTasks = getTenantsWithActiveTasks();
        for (Map.Entry<Integer, List<OMElement>> tenantWithActiveTask :
                tenantsWithActiveTasks.entrySet()) {

            Integer tenantId = tenantWithActiveTask.getKey();

            Map<String, String> credentials = getConnectionParametersForTenant(tenantId);

            // Initializing data store before analyzers access them
/*            try {
                NoSQLDataStoreFactory.getInstance().initializeDataStore(credentials, true);
            } catch (InitializationException e) {
                log.error("Error initializing data store for tenant : " + tenantId, e);
                throw new AnalyzerException("Error initializing data store for tenant : " + tenantId,
                        e);
            }*/ // TODO : Look in to this

            // Create and add the indexing system task for each tenant
//            AnalyzerSequence indexingSequence = new AnalyzerSequence();
//            indexingSequence.setName(AnalyzerConfigConstants.INDEXING_SEQUENCE + "-" +
//                                     tenantWithActiveTask.getKey());
//            AnalyzerConfig indexingConfig = new IndexingConfig(ConfigurationHolder.getInstance().
//                    getIndexConfigurations(tenantWithActiveTask.getKey()));
//            Analyzer indexingAnalyzer = new IndexingAnalyzer(indexingConfig);
//            indexingSequence.getAnalyzers().add(indexingAnalyzer);
//            indexingSequence.setInterval(AnalyzerConfigConstants.DEFAULT_INDEXING_INTERVAL);

//            tenantWithActiveTask.getValue().add(indexingSequence);

            // Start the analyzer sequences of the tenant
            for (OMElement analyzerSeqXML : tenantWithActiveTask.getValue()) {
                try {
                    AnalyzerSequence sequence =
                            Utils.getAnalyzerSequence(tenantId, analyzerSeqXML);

                    BAMTaskInfo taskInfo = new BAMTaskInfo();
                    taskInfo.setAnlyzerSequence(sequence);
                    taskInfo.setCredentials(credentials);
                    taskInfo.setAnalyzerSeqXML(analyzerSeqXML);

                    startAnalyzerSequence(taskInfo);
                } catch (Exception ignored) {
                    // Ignore and continue scheduling other tasks
                }
            }
        }

        log.info("Done initializing analyzer tasks..");

        return true;
    }

    private void loadIndexingTasks() {
        MetaDataManager metaDataManager = MetaDataManager.getInstance();
        int[] tenantIds = null;

        try {
            tenantIds = metaDataManager.getAllTenantsWithDefinedIndexes();
        } catch (ConfigurationException ignored) {
            // Ignored
        }

        if (tenantIds != null) {
            for (int tenantId : tenantIds) {
                try {
                    Map<String, String> credentials = null;
                    try {
                        credentials = getConnectionParametersForTenant(tenantId);
                    } catch (AnalyzerException e) {
                        log.error("Unable to fetch connection parameters for tenant " +
                                tenantId + " . Not initializing indexing tasks for this " +
                                "tenant..");
                        continue;
                    }

                    List<IndexConfiguration> configurations = MetaDataManager.getInstance().
                            getAllIndexMetaData(credentials);

                    for (IndexConfiguration configuration : configurations) {
                        if (!configuration.isAutoGenerated() && configuration.isManuallyIndexed()) {

                            IndexingTaskConfiguration taskConfiguration =
                                    new IndexingTaskConfiguration();
                            taskConfiguration.setCredentials(credentials);
                            taskConfiguration.setTenantId(tenantId);
                            taskConfiguration.setTaskName(configuration.getIndexName() + "_Index_" +
                                    configuration.getIndexedTable());
                            taskConfiguration.setInterval(PersistencyConstants.
                                    DEFAULT_INDEXING_INTERVAL);
                            try {
                                IndexManager.getInstance().scheduleIndexingTask(configuration,
                                        taskConfiguration);
                            } catch (IndexingException e) {
                                log.error("Error while schedule indexing task for index " +
                                        configuration.getIndexName() + " for tenant " +
                                        tenantId + "..");
                            }
                        }
                    }
                } catch (ConfigurationException e) {
                    log.error("Error while fetching index meta data for tenant " + tenantId +
                            ". Not" + " loading indexing tasks for this tenant..");
                }
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("Loaded indexing tasks for all tenants..");
        }
    }

    public Map<String, String> getConnectionParametersForTenant(int tenantId)
            throws AnalyzerException {
        Map<String, String> credentials;
        try {
            credentials = org.wso2.carbon.bam.core.utils.Utils.
                    getConnectionParameters(tenantId);
        } catch (ConfigurationException e) {
            String message = "Failed to fetch connection parameters for tenant : " + tenantId;
            log.error(message, e);
            throw new AnalyzerException(message, e);
        }

        return credentials;
    }

    private Map<Integer, List<OMElement>> getTenantsWithActiveTasks()
            throws AnalyzerException {
        try {
            UserRegistry superTenantRegistry = Utils.getRegistryService().getConfigSystemRegistry(
                    SuperTenantCarbonContext.getCurrentContext(
                            Utils.getConfigurationContextService().getServerConfigContext())
                            .getTenantId());

            Resource tenantTrackerResource;
            Map<Integer, List<OMElement>> tenantAnalyzersMap =
                    new HashMap<Integer, List<OMElement>>();

            String analyzerTrackerPath = AnalyzerConfigConstants.TENANT_TRACKER_PATH + "/" +
                    AnalyzerConfigConstants.ANALYZER_TRACKER;
            if (superTenantRegistry.resourceExists(analyzerTrackerPath)) {
                tenantTrackerResource = superTenantRegistry.get(analyzerTrackerPath);
                List<String> propertyValues = tenantTrackerResource.getPropertyValues(
                        AnalyzerConfigConstants.TENANTS_PROPERTY);
                if (propertyValues == null) {
                    return tenantAnalyzersMap;
                }
                for (String propertyValue : propertyValues) {
                    Integer tenantId = Integer.parseInt(propertyValue);

                    //ConfigurationHolder.getInstance().setCurrentConfigProcessingTenant(tenantId); TODO : Look in to this to see if this is still valid. See IndexingAnalyzerBuilder
                    List<OMElement> analyzerSequenceList = getAnalyzerSeqs(tenantId);
                    tenantAnalyzersMap.put(tenantId, analyzerSequenceList);
                }
            }
            return tenantAnalyzersMap;
        } catch (RegistryException e) {
            String message = "Cannot retrive task configurations for tenants";
            log.error(message, e);
            throw new AnalyzerException(message, e);
        }
    }

/*    public Map<Integer, List<CFConfigBean>> getTenantsWithIndexConfigurations()
            throws AnalyzerException {
        try {
            UserRegistry superTenantRegistry = Utils.getRegistryService().getConfigSystemRegistry(
                    SuperTenantCarbonContext.getCurrentContext(
                            Utils.getConfigurationContextService().getServerConfigContext())
                            .getTenantId());

            Map<Integer, List<CFConfigBean>> tenantIndexConfigMap = new HashMap<Integer,
                    List<CFConfigBean>>();

            Resource tenantTrackerResource;
            String indexTrackerPath = AnalyzerConfigConstants.TENANT_TRACKER_PATH + "/" +
                    AnalyzerConfigConstants.INDEX_TRACKER;
            if (superTenantRegistry.resourceExists(indexTrackerPath)) {
                tenantTrackerResource = superTenantRegistry.get(indexTrackerPath);
                List<String> propertyValues = tenantTrackerResource.getPropertyValues(
                        AnalyzerConfigConstants.TENANTS_PROPERTY);

                if (propertyValues != null) {
                    for (String propertyValue : propertyValues) {
                        Integer tenantId = Integer.parseInt(propertyValue);
                        List<CFConfigBean> indexConfigurations =
                                getIndexConfigurationsOfTenant(tenantId);
                        tenantIndexConfigMap.put(tenantId, indexConfigurations);
                    }
                }
            }

            return tenantIndexConfigMap;
        } catch (RegistryException e) {
            String message = "Cannot retrive index configurations for tenants";
            log.error(message, e);
            throw new AnalyzerException(message, e);
        }

        return null;

    }*/

/*    public List<CFConfigBean> getIndexConfigurationsOfTenant(Integer tenantId)
            throws AnalyzerException {

        List<CFConfigBean> indexConfigurations = null;
        try {
            // Load tenant registry to ensure the mounts are created before getting the tenant registry
            Utils.getTenantRegistryLoader().loadTenantRegistry(tenantId);

            // Get the config registry of the tenant
            UserRegistry tenantConfigSystemRegistry = Utils.getRegistryService().
                    getConfigSystemRegistry(tenantId);

            if (tenantConfigSystemRegistry.resourceExists(
                    AnalyzerConfigConstants.analyzerParentRegistryPath +
                            AnalyzerConfigConstants.indexes)) {

                Resource indexResource = tenantConfigSystemRegistry.get(
                        AnalyzerConfigConstants.analyzerParentRegistryPath +
                                AnalyzerConfigConstants.indexes);
                String indexXMLString = new String((byte[]) indexResource.getContent());
                indexConfigurations = Utils.getIndexConfigurations(AXIOMUtil.stringToOM(indexXMLString));
            }

        } catch (RegistryException e) {
            String message = "Cannot retrive index configurations for tenant" + tenantId;
            log.error(message, e);
            throw new AnalyzerException(message, e);

        } catch (XMLStreamException e) {
            String message = "Cannot retrive index configurations for tenant" + tenantId;
            log.error(message, e);
            throw new AnalyzerException(message, e);
        }

        return indexConfigurations;
    }*/

    public List<OMElement> getAnalyzerSeqs(Integer tenantId) throws AnalyzerException {
        try {
            // Load tenant registry to ensure the mounts are created before getting the tenant registry
            Utils.getTenantRegistryLoader().loadTenantRegistry(tenantId);

            // Get the config registry of the tenant
            UserRegistry tenantConfigSystemRegistry = Utils.getRegistryService().
                    getConfigSystemRegistry(tenantId);
            List<OMElement> analyzerSequenceList = new ArrayList<OMElement>();
            if (tenantConfigSystemRegistry.resourceExists(
                    AnalyzerConfigConstants.analyzerParentRegistryPath +
                            AnalyzerConfigConstants.analyzers)) {

                Collection collection = (Collection) tenantConfigSystemRegistry.get(
                        AnalyzerConfigConstants.analyzerParentRegistryPath +
                                AnalyzerConfigConstants.analyzers);
                String[] analyzerSeqRegistryPaths = collection.getChildren();
                for (String analyzerSeqRegistryPath : analyzerSeqRegistryPaths) {
                    Resource analyzerSeqResource = tenantConfigSystemRegistry.get(analyzerSeqRegistryPath);
                    String analyzerXMLString = new String((byte[]) analyzerSeqResource.getContent());

                    analyzerSequenceList.add(AXIOMUtil.stringToOM(analyzerXMLString));
                }
            }
            return analyzerSequenceList;
        } catch (RegistryException e) {
            String message = "Cannot retrieve task configurations for tenant" + tenantId;
            log.error(message, e);
            throw new AnalyzerException(message, e);
        } catch (XMLStreamException e) {
            String message = "Cannot retrieve task configurations for tenant" + tenantId;
            log.error(message, e);
            throw new AnalyzerException(message, e);
        }
    }

    public List<String> getAnalyzerSeqXMLs(Integer tenantId) throws AnalyzerException {
        try {
            // Load tenant registry to ensure the mounts are created before getting the tenant registry
            Utils.getTenantRegistryLoader().loadTenantRegistry(tenantId);

            // Get the config registry of the tenant
            UserRegistry tenantConfigSystemRegistry = Utils.getRegistryService().getConfigSystemRegistry(tenantId);
            List<String> analyzerSequenceXMLList = new ArrayList<String>();
            if (tenantConfigSystemRegistry.resourceExists(
                    AnalyzerConfigConstants.analyzerParentRegistryPath +
                            AnalyzerConfigConstants.analyzers)) {

                Collection collection = (Collection) tenantConfigSystemRegistry.get(
                        AnalyzerConfigConstants.analyzerParentRegistryPath +
                                AnalyzerConfigConstants.analyzers);
                String[] analyzerSeqRegistryPaths = collection.getChildren();
                for (String analyzerSeqRegistryPath : analyzerSeqRegistryPaths) {
                    Resource analyzerSeqResource = tenantConfigSystemRegistry.get(analyzerSeqRegistryPath);
                    String analyzerXMLString = new String((byte[]) analyzerSeqResource.getContent());

                    analyzerSequenceXMLList.add(analyzerXMLString);
                }
            }
            return analyzerSequenceXMLList;
        } catch (RegistryException e) {
            String message = "Cannot retrieve task configurations for tenant" + tenantId;
            log.error(message, e);
            throw new AnalyzerException(message, e);
        }
    }

    public String getAnalyzerSeqXML(Integer tenantId, String analyzerSeqName)
            throws AnalyzerException {
        try {
            if (analyzerSeqName == null) {
                throw new AnalyzerException("Analyzer sequence name cannot be null");
            }
            UserRegistry tenantConfigSystemRegistry =
                    Utils.getRegistryService().getConfigSystemRegistry(tenantId);
            String analyzerSeqRegistryPath = AnalyzerConfigConstants.analyzerParentRegistryPath +
                    AnalyzerConfigConstants.analyzers + analyzerSeqName;

            if (tenantConfigSystemRegistry.resourceExists(analyzerSeqRegistryPath)) {
                Resource analyzerSeqResource = tenantConfigSystemRegistry.get(analyzerSeqRegistryPath);
                return new String((byte[]) analyzerSeqResource.getContent());
            } else {
                String message = "Analyzer sequence " + analyzerSeqName + "does not exist for tenant : "
                        + tenantId;
                AnalyzerException analyzerException = new AnalyzerException(message);
                log.error(message, analyzerException);
                throw analyzerException;
            }
        } catch (RegistryException e) {
            String message = "Cannot retrieve Analyzer sequence : " + analyzerSeqName + " for tenant : " + tenantId;
            log.error(message, e);
            throw new AnalyzerException(message, e);
        }
    }

    @Override
    public void scheduleIndexingTask(
            IndexConfiguration configuration, IndexingTaskConfiguration taskConfiguration)
            throws IndexingException {

        Analyzer idxAnalyzer = new IndexingAnalyzer(new IndexingConfig(configuration));
        idxAnalyzer.setAnalyzerSeqeunceName(taskConfiguration.getTaskName());

        List<Analyzer> analyzers = new ArrayList<Analyzer>();
        analyzers.add(idxAnalyzer);

        AnalyzerSequence sequence = new AnalyzerSequence();
        sequence.setAnalyzers(analyzers);
        sequence.setCount(taskConfiguration.getCount());
        sequence.setCron(taskConfiguration.getCron());
        sequence.setInterval(taskConfiguration.getInterval());
        sequence.setName(taskConfiguration.getTaskName());
        sequence.setTenantId(taskConfiguration.getTenantId());

        try {
            BAMTaskInfo taskInfo = new BAMTaskInfo();
            taskInfo.setAnalyzerSeqXML(Utils.serializeIndexAnalyser(configuration));
            taskInfo.setCredentials(taskConfiguration.getCredentials());
            taskInfo.setAnlyzerSequence(sequence);

            startAnalyzerSequence(taskInfo);
        } catch (Exception e) {
            throw new IndexingException("Unable to schedule task..", e);
        }
    }

    public void unscheduleIndexingTask(IndexingTaskConfiguration taskConfiguration) {
        AnalyzerSequence sequence = new AnalyzerSequence();
        sequence.setName(taskConfiguration.getTaskName());
        sequence.setTenantId(taskConfiguration.getTenantId());

        try {
            deleteAnalyzerSequence(sequence, taskConfiguration.getTenantId());
        } catch (TaskException e) {
            log.error("Error in deleting analyser sequence", e);
        }
    }

    public void startAnalyzerSequence(BAMTaskInfo bamTaskInfo) throws Exception {
        TaskInfo taskInfo = Utils.getTaskInfo(bamTaskInfo);
        getTaskManager().registerTask(taskInfo);

        if (log.isDebugEnabled()) {
            log.debug("Registered task : " + bamTaskInfo.getAnalyzerSequence().getName() +
                    " for tenant : " + bamTaskInfo.getAnalyzerSequence().getTenantId());
        }
    }

    public void deleteAnalyzerSequence(AnalyzerSequence sequence, int tenantId) throws TaskException {
        this.getTaskManager().deleteTask(sequence.getName());
        // Finally do the clean up to clear any cursors created by this analyzer sequence
        sequence.cleanup();

    }


    public void stopAnalyzerSequence(AnalyzerSequence sequence) throws TaskException {
        this.getTaskManager().deleteTask(sequence.getName());
    }

    public TaskManager getTaskManager() {
        return taskManager;
    }

}
