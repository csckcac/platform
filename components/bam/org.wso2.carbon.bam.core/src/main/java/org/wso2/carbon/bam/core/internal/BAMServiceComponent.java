/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.bam.core.internal;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.bam.core.BAMConstants;
import org.wso2.carbon.bam.core.archive.ArchiverTask;
import org.wso2.carbon.bam.core.cache.CacheConstant;
import org.wso2.carbon.bam.core.cache.CacheTask;
import org.wso2.carbon.bam.core.collector.DataCollector;
import org.wso2.carbon.bam.core.persistence.BAMDatabaseCreator;
import org.wso2.carbon.bam.core.persistence.BAMPersistenceManager;
import org.wso2.carbon.bam.core.persistence.DatabaseConfiguration;
import org.wso2.carbon.bam.core.persistence.DatabaseConstants;
import org.wso2.carbon.bam.core.receivers.ActivityEventQueue;
import org.wso2.carbon.bam.core.receivers.ServerEventQueue;
import org.wso2.carbon.bam.core.receivers.ServerUserDefinedDataEventingMessageQueue;
import org.wso2.carbon.bam.core.summary.SummaryGenerationTask;
import org.wso2.carbon.bam.core.util.BAMUtil;
import org.wso2.carbon.bam.util.BAMCalendar;
import org.wso2.carbon.bam.util.BAMException;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ConfigurationContextService;

import javax.sql.DataSource;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.Timer;

/**
 * @scr.component name="bam.core.component" immediate="true"
 * @scr.reference name="registry.service" interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic" bind="setRegistryService" unbind="unsetRegistryService"
 * @scr.reference name="config.context.service" interface="org.wso2.carbon.utils.ConfigurationContextService"
 * cardinality="1..1" policy="dynamic" bind="setConfigurationContextService"
 * unbind="unsetConfigurationContextService"
 * @scr.reference name="realm.service" interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1" policy="dynamic" bind="setRealmService" unbind="unsetRealmService"
 */
public class BAMServiceComponent {

    private static Log log = LogFactory.getLog(BAMServiceComponent.class);
    private Timer collectionTimer;
    private Timer summaryTimer;
    private Timer cacheTimer;
    private DataCollector collector;
    private SummaryGenerationTask generator;
    private CacheTask cacheTask;
    private BundleContext bundleContext;

    private long summaryGenDelay;
    private long summaryGenInterval;
    private long dataCollectionDelay;
    private long dataCollectionInterval;


    private long taskBreakDownLength;
    private long sleepTimeBetweenTasks;

    private static int activityThreadPoolSize;
    private static int mediationThreadPoolSize;
    private static int serviceThreadPoolSize;

    private static ServerEventQueue serverEventQueue;
    private static ActivityEventQueue activityEventQueue;
    private static ServerUserDefinedDataEventingMessageQueue serverUserDefinedDataMessageQueue;

    public BAMServiceComponent() {
    }

    protected void activate(ComponentContext ctx) {
        try {
            bundleContext = ctx.getBundleContext();
            BAMPersistenceManager persistenceManager = BAMPersistenceManager.getPersistenceManager(null);
            bundleContext.registerService(BAMPersistenceManager.class.getName(), persistenceManager, null);

            setup();

            try {
                processConfigurations();
            } catch (Exception ignored) {
                // Can be ignored. In this case default configuration values will be used.
            }

            processDefaults();

            serverEventQueue = new ServerEventQueue(serviceThreadPoolSize);
            activityEventQueue = new ActivityEventQueue(activityThreadPoolSize);
            serverUserDefinedDataMessageQueue = new ServerUserDefinedDataEventingMessageQueue(serviceThreadPoolSize);

            startTasks();

        } catch (Throwable e) {
            log.error("Error during BAMServiceComponent activation. Ignoring..", e);
        }

        if (log.isDebugEnabled()) {
            log.debug("BAM Core bundle is activated");
        }

    }

    protected void deactivate(ComponentContext ctx) {
        if (log.isDebugEnabled()) {
            log.debug("BAM Core bundle is deactivated");
        }
        collectionTimer.cancel();
        summaryTimer.cancel();
        serverEventQueue.cleanup();
    }

    public static ServerEventQueue getServerEventQueue() {
        return serverEventQueue;
    }

    public static ActivityEventQueue getActivityEventQueue() {
        return activityEventQueue;
    }

    public static ServerUserDefinedDataEventingMessageQueue getServerUserDefinedDataMessageQueue() {
        return serverUserDefinedDataMessageQueue;
    }

    public static int getActivityThreadPoolSize() {
        return activityThreadPoolSize;
    }

    public static int getServiceThreadPoolSize() {
        return serviceThreadPoolSize;
    }

    public static int getMediationThreadPoolSize() {
        return mediationThreadPoolSize;
    }

    protected void setRegistryService(RegistryService registryService) throws RegistryException {
        if (log.isDebugEnabled()) {
            log.debug("RegistryService set in BAM bundle");
        }

        BAMUtil.setRegistry(registryService.getConfigSystemRegistry());
    }

    private static DataSource dataSource;

    public static DataSource getDataSource() {
        return dataSource;
    }

    private void setup() {

        String dataSourcesFile = CarbonUtils.getCarbonConfigDirPath() + File.separator +
                                 DatabaseConstants.DATASOURCES_FILE;
        Properties properties = new Properties();

        FileInputStream in = null;
        try {
            in = new FileInputStream(dataSourcesFile);
            properties.load(in);
        } catch (IOException e) {
            log.error("Unable to load DataSource property file. Database tables will not be created..",
                      e);
            return;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    if (log.isTraceEnabled()) {
                        log.trace("Error while closing DataSource property file stream..", e);
                    }
                }
            }
        }

        //Initialized BAM caching
        BAMUtil.initBAMCache();

        DatabaseConfiguration dbConfig = getDatabaseConfiguration(properties);

        if (dbConfig == null) {
            log.error("Required properties not defined in DataSource property file. " +
                      "Database tables will not be created..");
            return;
        }

        dataSource = buildDataSource(dbConfig);

        if (System.getProperty("setup") != null) {
            BAMDatabaseCreator dbCreator = new BAMDatabaseCreator(dataSource);
            try {
                String validationQuery = dbConfig.getValidationQuery();

                if (validationQuery == null) {
                    validationQuery = DatabaseConstants.DEFAULT_VALIDATION_QUERY;
                }

                if (!dbCreator.isDatabaseStructureCreated(validationQuery)) {
                    CarbonUtils.checkSecurity();
                    dbCreator.createBAMDatabase();
                }
            } catch (Exception e) {
                String msg = "Error in creating database tables..";
                log.error(msg, e);
            }

        }
    }

    private DatabaseConfiguration getDatabaseConfiguration(Properties properties) {
        String dataSourceName = properties.getProperty(DatabaseConstants.DATASOURCE_NAME_PROPERTY);

        if (dataSourceName != null) {
            String dbUrl = properties.getProperty(DatabaseConstants.DATASOURCE_NAME_PROPERTY + "." +
                                                  dataSourceName + "." +
                                                  DatabaseConstants.DATABASE_URL_PROPERTY);
            if (dbUrl == null) {
                return null;
            }

            String driverName = properties.getProperty(DatabaseConstants.DATASOURCE_NAME_PROPERTY +
                                                       "." + dataSourceName + "." +
                                                       DatabaseConstants.DRIVER_CLASS_PROPERTY);
            if (driverName == null) {
                return null;
            }

            String userName = properties.getProperty(DatabaseConstants.DATASOURCE_NAME_PROPERTY +
                                                     "." + dataSourceName + "." +
                                                     DatabaseConstants.USERNAME_PROPERTY);
            if (userName == null) {
                return null;
            }

            String password = properties.getProperty(DatabaseConstants.DATASOURCE_NAME_PROPERTY +
                                                     "." + dataSourceName + "." +
                                                     DatabaseConstants.PASSWORD_PROPERTY);
            if (password == null) {
                return null;
            }

            String configName = properties.getProperty(DatabaseConstants.DATASOURCE_NAME_PROPERTY +
                                                       "." + dataSourceName + "." +
                                                       DatabaseConstants.CONFIG_NAME_PROPERTY);
            if (configName == null) {
                return null;
            }

            DatabaseConfiguration dbConfig = new DatabaseConfiguration();
            dbConfig.setDataSourceName(dataSourceName);
            dbConfig.setDbUrl(dbUrl);
            dbConfig.setDriverName(driverName);
            dbConfig.setUserName(userName);
            dbConfig.setPassword(password);
            dbConfig.setConfigName(configName);

            String maxActive = properties.getProperty(DatabaseConstants.DATASOURCE_NAME_PROPERTY +
                                                      "." + dataSourceName + "." +
                                                      DatabaseConstants.MAX_ACTIVE_PROPERTY);
            if (maxActive != null) {
                dbConfig.setMaxActive(Integer.parseInt(maxActive));
            } else {
                dbConfig.setMaxActive(DatabaseConstants.DEFAULT_MAX_ACTIVE);
            }

            String maxWait = properties.getProperty(DatabaseConstants.DATASOURCE_NAME_PROPERTY +
                                                    "." + dataSourceName + "." +
                                                    DatabaseConstants.MAX_WAIT_PROPERTY);
            if (maxWait != null) {
                dbConfig.setMaxWait(Integer.parseInt(maxWait));
            } else {
                dbConfig.setMaxWait(DatabaseConstants.DEFAULT_MAX_WAIT);
            }

            String maxIdle = properties.getProperty(DatabaseConstants.DATASOURCE_NAME_PROPERTY +
                                                    "." + dataSourceName + "." +
                                                    DatabaseConstants.MAX_IDLE_PROPERTY);
            if (maxIdle != null) {
                dbConfig.setMaxIdle(Integer.parseInt(maxIdle));
            } else {
                dbConfig.setMaxIdle(DatabaseConstants.DEFAULT_MAX_IDLE);
            }

            String minIdle = properties.getProperty(DatabaseConstants.DATASOURCE_NAME_PROPERTY +
                                                    "." + dataSourceName + "." +
                                                    DatabaseConstants.MIN_IDLE_PROPERTY);
            if (minIdle != null) {
                dbConfig.setMinIdle(Integer.parseInt(minIdle));
            } else {
                dbConfig.setMinIdle(DatabaseConstants.DEFAULT_MIN_IDLE);
            }

            String validationQuery = properties.getProperty(DatabaseConstants.DATASOURCE_NAME_PROPERTY +
                                                            "." + dataSourceName + "." +
                                                            DatabaseConstants.VALIDATION_QUERY_PROPERTY);
            dbConfig.setValidationQuery(validationQuery);

            return dbConfig;

        }

        return null;

    }

    private DataSource buildDataSource(DatabaseConfiguration config) {
        BasicDataSource basicDataSource = new BasicDataSource();
        basicDataSource.setUrl(config.getDbUrl().trim());
        basicDataSource.setDriverClassName(config.getDriverName().trim());
        basicDataSource.setUsername(config.getUserName().trim());
        basicDataSource.setPassword(config.getPassword().trim());
        basicDataSource.setMaxActive(config.getMaxActive());
        basicDataSource.setMaxIdle(config.getMaxIdle());
        basicDataSource.setMaxWait(config.getMaxWait());
        basicDataSource.setMinIdle(config.getMinIdle());
        basicDataSource.setValidationQuery(config.getValidationQuery());

        return basicDataSource;

    }

    private void startTasks() {

        collectionTimer = new Timer(true);
        summaryTimer = new Timer(true);
        cacheTimer = new Timer(true);

        Timer archiveTimer = new Timer(true);

        collector = new DataCollector();
        collectionTimer.schedule(collector, dataCollectionDelay, dataCollectionInterval);

        log.info("BAM Data Collector started...");

        generator = new SummaryGenerationTask(bundleContext, taskBreakDownLength, sleepTimeBetweenTasks);
        BAMPersistenceManager persistenceManager = BAMPersistenceManager.getPersistenceManager(BAMUtil.getRegistry());
        if (persistenceManager != null) {
            if ((summaryGenDelay < 0) || (summaryGenInterval < 0 )) {
                log.warn("Negative value detected for summary generation delay or " +
                         "summary generation interval. Disabling Summary Generation ...");
            } else {
                summaryTimer.schedule(generator, summaryGenDelay, summaryGenInterval);
                log.info("BAM Summary Generator started...");
            }
        }

        ArchiverTask archiver = new ArchiverTask();
        long timeTillMidNight = BAMCalendar.timeTillMidNight();
        archiveTimer.schedule(archiver, timeTillMidNight, BAMConstants.DEFAULT_ARCHIVAL_CHECK_INTERVAL);
        log.info("BAM Message Archiving started...");

        cacheTask = new CacheTask();
        cacheTimer.schedule(cacheTask, CacheConstant.DEFAULT_CACHING_REMOVAL_DELAY,
                            CacheConstant.DEFAULT_CACHING_REMOVAL_INTERVAL);
    }

    protected void unsetRegistryService(RegistryService registryService) {
        if (log.isDebugEnabled()) {
            log.debug("RegistryService unset in BAM bundle");
        }

        BAMUtil.setRegistry(null);
    }

    protected void setConfigurationContextService(ConfigurationContextService ccService) {
        if (log.isDebugEnabled()) {
            log.debug("ConfigurationContextService set in BAM bundle");
        }

//        if (!CarbonUtils.isRunningOnLocalTransportMode()) {
//            ConfigurationContext serverCtx = ccService.getServerConfigContext();
//            AxisConfiguration serverConfig = serverCtx.getAxisConfiguration();
//            LocalTransportReceiver.CONFIG_CONTEXT = new ConfigurationContext(serverConfig);
//            LocalTransportReceiver.CONFIG_CONTEXT.setServicePath("services");
//            LocalTransportReceiver.CONFIG_CONTEXT.setContextRoot("local:/");
//        }

        BAMUtil.setConfigurationContextService(ccService);
    }

    protected void unsetConfigurationContextService(ConfigurationContextService ccService) {
        if (log.isDebugEnabled()) {
            log.debug("ConfigurationContextService unset in BAM bundle");
        }

        BAMUtil.setConfigurationContextService(null);
    }

    public RealmService getRealmService() {
        return BAMUtil.getRealmService();
    }

    protected void setRealmService(RealmService realmService) {
        BAMUtil.setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {
        BAMUtil.setRealmService(null);
    }

    private void waitForCompletion() {
        generator.setSignalledState(true);
        collector.setSignalledState(true);

        // Busy wait until both the timer tasks finish
        while (generator.getRunningState() || collector.getRunningState()) {

        }

    }

    private void processConfigurations() throws BAMException {

        StAXOMBuilder builder;
        FileReader reader = null;
        XMLStreamReader parse = null;

        try {
            String configFile = CarbonUtils.getCarbonConfigDirPath() + File.separator +
                                BAMConstants.BAM_CONSTANTS_CONFIG_FILE;
            try {
                reader = new FileReader(configFile);
                parse = XMLInputFactory.newInstance().createXMLStreamReader(reader);

                builder = new StAXOMBuilder(parse);
                OMElement bamElement = builder.getDocumentElement();

                processSumamryElement(bamElement);
                processDataCollectionElement(bamElement);
                processThreadPoolsElement(bamElement);

            } finally {
                if (reader != null) {
                    reader.close();
                }
                if (parse != null) {
                    parse.close();
                }
            }

        } catch (XMLStreamException e) {
            throw new BAMException("error occurred creating stream for bam.xml", e);
        } catch (IOException e) {
            throw new BAMException("error occurred getting bam.xml ", e);
        }
    }

    private void processSumamryElement(OMElement bamElement) {
        OMElement summaryElement = bamElement.getFirstChildWithName(
                new QName(BAMConstants.SUMMARY_GENERATION_ELEMENT));
        if (summaryElement != null) {
            OMElement delay = summaryElement.getFirstChildWithName(
                    new QName(BAMConstants.INITIAL_DELAY_ELEMENT));
            if (delay != null) {
                summaryGenDelay = Long.parseLong(delay.getText()) *
                                  BAMConstants.MILLISECONDS_MULTIPLIER;
            } else {
                log.debug("No summaryGeneration initial-delay found in bam.xml. Using default" +
                          " value : " + BAMConstants.DEFAULT_INITIAL_SUMMARY_GEN_DELAY
                                        / BAMConstants.MILLISECONDS_MULTIPLIER + "s");
            }

            OMElement interval = summaryElement.getFirstChildWithName(
                    new QName(BAMConstants.INTERVAL_ELEMENT));
            if (interval != null) {
                summaryGenInterval = Long.parseLong(interval.getText()) *
                                     BAMConstants.MILLISECONDS_MULTIPLIER;
            } else {
                log.debug("No summaryGeneration interval found in bam.xml. Using default" +
                          " value : " + BAMConstants.DEFAULT_SUMMARY_GEN_INTERVAL
                                        / BAMConstants.MILLISECONDS_MULTIPLIER + "s");
            }

            OMElement taskBreakDownLengthEl = summaryElement.getFirstChildWithName(
                    new QName(BAMConstants.TASK_BREAKDOWN_LENGTH_ELEMENT));
            if (taskBreakDownLengthEl != null) {
                taskBreakDownLength = Long.parseLong(taskBreakDownLengthEl.getText());
                if (log.isDebugEnabled()) {
                    log.debug("Task break down length is set to : " + taskBreakDownLength);
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("No task break length found in bam.xml. Using default" +
                          " value : " + BAMConstants.DEFAULT_TASK_BREAKDOWN_LENGTH);
                }
            }

            OMElement sleepBetweenTimeTasksEl = summaryElement.getFirstChildWithName(
                    new QName(BAMConstants.SLEEP_TIME_BETWEEN_TASKS_ELEMENT));
            if (sleepBetweenTimeTasksEl != null) {
                sleepTimeBetweenTasks = Long.parseLong(sleepBetweenTimeTasksEl.getText());
                if (log.isDebugEnabled()) {
                    log.debug("Sleep time between tasks is set to : " + sleepTimeBetweenTasks);
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("No sleep time between tasks found in bam.xml. Using default" +
                              " value : " + BAMConstants.DEFAULT_SUMMARY_GEN_INTERVAL);
                }
            }
        } else {
            log.debug("No summaryGeneration element found in bam.xml. Using default values : " +
                      "initial-delay = " + BAMConstants.DEFAULT_INITIAL_SUMMARY_GEN_DELAY /
                                           BAMConstants.MILLISECONDS_MULTIPLIER + "s" +
                      " interval = " + BAMConstants.DEFAULT_SUMMARY_GEN_INTERVAL /
                                       BAMConstants.MILLISECONDS_MULTIPLIER + "s");
        }
    }

    private void processDataCollectionElement(OMElement bamElement) {
        OMElement dataCollectionElement = bamElement.getFirstChildWithName(
                new QName(BAMConstants.DATA_COLLECTION_ELEMENT));
        if (dataCollectionElement != null) {
            OMElement delay = dataCollectionElement.getFirstChildWithName(
                    new QName(BAMConstants.INITIAL_DELAY_ELEMENT));
            if (delay != null) {
                dataCollectionDelay = Long.parseLong(delay.getText()) *
                                      BAMConstants.MILLISECONDS_MULTIPLIER;
            } else {
                log.debug("No dataCollection initial-delay found in bam.xml. Using default" +
                          " value : " + BAMConstants.DEFAULT_INITIAL_DATA_COLLECTION_DELAY /
                                        BAMConstants.MILLISECONDS_MULTIPLIER + "s");
            }

            OMElement interval = dataCollectionElement.getFirstChildWithName(
                    new QName(BAMConstants.INTERVAL_ELEMENT));
            if (interval != null) {
                dataCollectionInterval = Long.parseLong(interval.getText()) *
                                         BAMConstants.MILLISECONDS_MULTIPLIER;
            } else {
                log.debug("No dataCollection interval found in bam.xml. Using default" +
                          " value : " + BAMConstants.DEFAULT_DATA_COLLECTION_INTERVAL /
                                        BAMConstants.MILLISECONDS_MULTIPLIER + "s");
            }
        } else {
            log.debug("No dataCollection element found in bam.xml. Using default values : " +
                      "initial-delay = " + BAMConstants.DEFAULT_INITIAL_DATA_COLLECTION_DELAY /
                                           BAMConstants.MILLISECONDS_MULTIPLIER + "s" +
                      " interval = " + BAMConstants.DEFAULT_DATA_COLLECTION_INTERVAL /
                                       BAMConstants.MILLISECONDS_MULTIPLIER + "s");
        }

    }

    private void processThreadPoolsElement(OMElement bamElement) {
        OMElement threadPoolsElement = bamElement.getFirstChildWithName(
                new QName(BAMConstants.THREAD_POOL_SIZES_ELEMENT));
        if (threadPoolsElement != null) {
            OMElement activity = threadPoolsElement.getFirstChildWithName(
                    new QName(BAMConstants.ACTIVITY_ELEMENT));
            if (activity != null) {
                activityThreadPoolSize = Integer.parseInt(activity.getText());
            } else {
                log.debug("No size for activity thread pool found in bam.xml. Using default" +
                          " value : " + BAMConstants.DEFAULT_ACTIVITY_POOL_SIZE);
            }

            OMElement mediation = threadPoolsElement.getFirstChildWithName(
                    new QName(BAMConstants.MEDIATION_ELEMENT));
            if (mediation != null) {
                mediationThreadPoolSize = Integer.parseInt(mediation.getText());
            } else {
                log.debug("No size for mediation thread pool found in bam.xml. Using default" +
                          " value : " + BAMConstants.DEFAULT_MEDIATION_POOL_SIZE);
            }

            OMElement service = threadPoolsElement.getFirstChildWithName(
                    new QName(BAMConstants.SERVICE_ELEMENT));
            if (service != null) {
                serviceThreadPoolSize = Integer.parseInt(service.getText());
            } else {
                log.debug("No size for service thread pool found in bam.xml. Using default" +
                          " value : " + BAMConstants.DEFAULT_SERVICE_POOL_SIZE);
            }
        } else {
            log.debug("No threadPoolSizes element found in bam.xml. Using default values : " +
                      "activity =" + BAMConstants.DEFAULT_ACTIVITY_POOL_SIZE +
                      "\n mediation = " + BAMConstants.DEFAULT_MEDIATION_POOL_SIZE +
                      "\n service = " + BAMConstants.DEFAULT_SERVICE_POOL_SIZE);
        }
    }

    private void processDefaults() {
//        if (summaryGenDelay <= 0) {
//            summaryGenDelay = BAMConstants.DEFAULT_INITIAL_SUMMARY_GEN_DELAY;
//            log.warn("Using default value for summary generation initial-delay : " +
//                     BAMConstants.DEFAULT_INITIAL_SUMMARY_GEN_DELAY /
//                     BAMConstants.MILLISECONDS_MULTIPLIER + "s");
//        }
//
//        if (summaryGenInterval <= 0) {
//            summaryGenInterval = BAMConstants.DEFAULT_SUMMARY_GEN_INTERVAL;
//            log.warn("Using default value for summary generation interval : " +
//                     BAMConstants.DEFAULT_SUMMARY_GEN_INTERVAL /
//                     BAMConstants.MILLISECONDS_MULTIPLIER + "s");
//        }

        if (dataCollectionDelay <= 0) {
            dataCollectionDelay = BAMConstants.DEFAULT_INITIAL_DATA_COLLECTION_DELAY;
            log.warn("Using default value for data collection initial-delay : " +
                     BAMConstants.DEFAULT_INITIAL_DATA_COLLECTION_DELAY /
                     BAMConstants.MILLISECONDS_MULTIPLIER + "s");
        }

        if (dataCollectionInterval <= 0) {
            dataCollectionInterval = BAMConstants.DEFAULT_DATA_COLLECTION_INTERVAL;
            log.warn("Using default value for data collection interval : " +
                     BAMConstants.DEFAULT_DATA_COLLECTION_INTERVAL /
                     BAMConstants.MILLISECONDS_MULTIPLIER + "s");
        }

        if (activityThreadPoolSize <= 0) {
            activityThreadPoolSize = BAMConstants.DEFAULT_ACTIVITY_POOL_SIZE;
            log.warn("Using default value for activity thread pool size :" +
                     BAMConstants.DEFAULT_ACTIVITY_POOL_SIZE);
        }

        if (serviceThreadPoolSize <= 0) {
            serviceThreadPoolSize = BAMConstants.DEFAULT_SERVICE_POOL_SIZE;
            log.warn("Using default value for service thread pool size :" +
                     BAMConstants.DEFAULT_SERVICE_POOL_SIZE);
        }

        if (mediationThreadPoolSize <= 0) {
            mediationThreadPoolSize = BAMConstants.DEFAULT_MEDIATION_POOL_SIZE;
            log.warn("Using default value for mediation thread pool size :" +
                     BAMConstants.DEFAULT_MEDIATION_POOL_SIZE);
        }

    }

}
