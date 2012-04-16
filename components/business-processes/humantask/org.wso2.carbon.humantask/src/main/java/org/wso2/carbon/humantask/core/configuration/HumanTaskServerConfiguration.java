/*
 * Copyright (c) 2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.humantask.core.configuration;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlbeans.XmlException;
import org.quartz.CronExpression;
import org.wso2.carbon.humantask.core.dao.TaskStatus;
import org.wso2.carbon.humantask.server.config.*;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The memory model of the humantask configuration - humantask.xml.
 */
public class HumanTaskServerConfiguration {
    private static final Log log = LogFactory.getLog(HumanTaskServerConfiguration.class);

    private HumanTaskServerConfigDocument htServerConfigDocument;

    private String dataSourceName;

    private String dataSourceJNDIRepoInitialContextFactory;

    private String dataSourceJNDIRepoProviderURL;

    private boolean generateDdl = false;

    private boolean showSql = false;

    private String daoConnectionFactoryClass;

    private int portOffset = 0;

    private String peopleQueryEvaluatorClass;

	private int threadPoolMaxSize = 50;

    private static int CARBON_DEFAULT_PORT_OFFSET = 0;

    private static String CARBON_CONFIG_PORT_OFFSET_NODE = "Ports.Offset";

//    private String transactionFactoryClass = "com.atomikos.icatch.jta.UserTransactionManager";
    private String transactionFactoryClass = "org.apache.ode.il.EmbeddedGeronimoFactory";

    private List<TaskStatus> removableTaskStatuses = Collections.emptyList();

    private String taskCleanupCronExpression;

    private boolean enableTaskEventPersistence = false;


    /**
     * Create Human Task Server Configuration from a configuration file. If error occurred while parsing configuration
     * file, default configuration will be created.
     *
     * @param htServerConfig XMLBeans object of human task server configuration file
     */
    public HumanTaskServerConfiguration(File htServerConfig) {
        htServerConfigDocument = readConfigurationFromFile(htServerConfig);

        if (htServerConfigDocument == null) {
            return;
        }

        initConfigurationFromFile();
    }

    public HumanTaskServerConfiguration() {
        this.dataSourceName = "htds";
        this.daoConnectionFactoryClass = "org.wso2.carbon.humantask.dao.jpa.openjpa.HumanTaskDAOConnectionFactoryImpl";
        this.dataSourceJNDIRepoInitialContextFactory = "com.sun.jndi.rmi.registry.RegistryContextFactory";
        this.dataSourceJNDIRepoProviderURL = "rmi://localhost:2199";
        this.peopleQueryEvaluatorClass = "org.wso2.carbon.humantask.core.integration.UserManagerBasedPeopleQueryEvaluator";
//        this.threadPoolMaxSize = 50;
    }

    private HumanTaskServerConfigDocument readConfigurationFromFile(File htServerConfiguration) {
        try {
            return HumanTaskServerConfigDocument.Factory.parse(new FileInputStream(htServerConfiguration));
        } catch (XmlException e) {
            log.error("Error parsing human task server configuration.", e);
        } catch (FileNotFoundException e) {
            log.info("Cannot find the human task server configuration in specified location "
                     + htServerConfiguration.getPath() + " . Loads the default configuration.");
        } catch (IOException e) {
            log.error("Error reading human task server configuration file" + htServerConfiguration.getPath() + " .");
        }

        return null;
    }

    // Initialise the configuration object from the properties in the human task server config xml file.
    private void initConfigurationFromFile() {
        THumanTaskServerConfig tHumanTaskServerConfig = htServerConfigDocument.getHumanTaskServerConfig();
        if (tHumanTaskServerConfig == null) {
            return;
        }

        if (tHumanTaskServerConfig.getPersistenceConfig() != null) {
            initPersistenceConfig(tHumanTaskServerConfig.getPersistenceConfig());
        }

        if (tHumanTaskServerConfig.getPeopleQueryEvaluatorConfig() != null) {
            initPeopleQueryEvaluator(tHumanTaskServerConfig.getPeopleQueryEvaluatorConfig());
        }

        if (tHumanTaskServerConfig.getSchedulerConfig() != null) {
            initSchedulerConfig(tHumanTaskServerConfig.getSchedulerConfig());
        }

        if (tHumanTaskServerConfig.getTransactionManagerConfig() != null) {
            initTransactionManagerConfig(tHumanTaskServerConfig.getTransactionManagerConfig());
        }

        if(tHumanTaskServerConfig.getTaskCleanupConfig() != null) {
            iniTaskCleanupConfig(tHumanTaskServerConfig.getTaskCleanupConfig());
        }
    }

    private void iniTaskCleanupConfig(TTaskCleanupConfig taskCleanupConfig) {

        if(taskCleanupConfig != null) {
            if(StringUtils.isNotEmpty(taskCleanupConfig.getCronExpression())) {
                if(CronExpression.isValidExpression(taskCleanupConfig.getCronExpression().trim())) {
                    this.taskCleanupCronExpression = taskCleanupConfig.getCronExpression();
                } else {
                    String warnMsg = String.format("The task clean up cron expression[%s] is invalid." +
                                                   " Ignoring task clean up configurations! ",
                                                   taskCleanupConfig.getCronExpression());
                    log.warn(warnMsg);
                    return;
                }
            }

            if(StringUtils.isNotEmpty(taskCleanupConfig.getStatuses())) {
                String[] removableStatusesArray = taskCleanupConfig.getStatuses().split(",");

                List<TaskStatus> removableTaskStatusList = new ArrayList<TaskStatus>();
                for(String removableStatus : removableStatusesArray) {
                    for (TaskStatus taskStatusEnum : TaskStatus.values() ) {
                        if(taskStatusEnum.toString().equals(removableStatus.trim())) {
                            removableTaskStatusList.add(taskStatusEnum);
                            break;
                        }
                    }
                }
                this.removableTaskStatuses = removableTaskStatusList;
            }
        }
    }

    private void initTransactionManagerConfig(TTransactionManagerConfig tTransactionManagerConfig) {
        if (tTransactionManagerConfig.getTransactionManagerClass() != null) {
            this.transactionFactoryClass = tTransactionManagerConfig.getTransactionManagerClass().
                    trim();
        }
    }

    private void initSchedulerConfig(TSchedulerConfig tSchedulerConfig) {
        if (tSchedulerConfig.getMaxThreadPoolSize() > 0) {
            this.threadPoolMaxSize = tSchedulerConfig.getMaxThreadPoolSize();
        }
    }

    private void initPeopleQueryEvaluator(TPeopleQueryEvaluatorConfig tUserManagerConfig) {
        if (tUserManagerConfig.getPeopleQueryEvaluatorClass() != null) {
            this.peopleQueryEvaluatorClass = tUserManagerConfig.getPeopleQueryEvaluatorClass().trim();
        }
    }

    private void initPersistenceConfig(TPersistenceConfig tPersistenceConfig) {
        if (tPersistenceConfig.getDataSource() != null) {
            this.dataSourceName = tPersistenceConfig.getDataSource().trim();
        }
        if (tPersistenceConfig.getJNDIInitialContextFactory() != null) {
            this.dataSourceJNDIRepoInitialContextFactory =
                    tPersistenceConfig.getJNDIInitialContextFactory().trim();
        }
        if (tPersistenceConfig.getJNDIProviderUrl() != null) {
            this.dataSourceJNDIRepoProviderURL = tPersistenceConfig.getJNDIProviderUrl().trim();
            this.portOffset = getCarbonPortOffset();

            // We need to adjust the port value according to the offset defined in the carbon configuration.
            String portValueString = dataSourceJNDIRepoProviderURL.substring(
                    dataSourceJNDIRepoProviderURL.lastIndexOf(":") + 1,
                    dataSourceJNDIRepoProviderURL.length());

            String urlWithoutPort = dataSourceJNDIRepoProviderURL
                    .substring(0, dataSourceJNDIRepoProviderURL.lastIndexOf(":") + 1);


            int actualPortValue = Integer.parseInt(portValueString);
            int correctedPortValue = actualPortValue + portOffset;

            this.dataSourceJNDIRepoProviderURL = urlWithoutPort.concat(Integer.toString(correctedPortValue));

        }
        if (tPersistenceConfig.getDAOConnectionFactoryClass() != null) {
            this.daoConnectionFactoryClass = tPersistenceConfig.getDAOConnectionFactoryClass().trim();
        }

        this.generateDdl = tPersistenceConfig.getGenerateDdl();
        this.showSql = tPersistenceConfig.getShowSql();
    }

    //gets the carbon port offset value.
    private int getCarbonPortOffset() {

        String portOffset = CarbonUtils.getServerConfiguration().getFirstProperty(
                CARBON_CONFIG_PORT_OFFSET_NODE);

        try {
            return ((portOffset != null) ? Integer.parseInt(portOffset.trim()) :
                    CARBON_DEFAULT_PORT_OFFSET);
        } catch (NumberFormatException e) {
            return CARBON_DEFAULT_PORT_OFFSET;
        }
    }

    public String getDataSourceName() {
        return dataSourceName;
    }

    public String getDataSourceJNDIRepoInitialContextFactory() {
        return dataSourceJNDIRepoInitialContextFactory;
    }

    public String getDataSourceJNDIRepoProviderURL() {
        return dataSourceJNDIRepoProviderURL;
    }

    public boolean isGenerateDdl() {
        return generateDdl;
    }

    public boolean isShowSql() {
        return showSql;
    }

    public String getDaoConnectionFactoryClass() {
        return daoConnectionFactoryClass;
    }

    /**
     * @return : The class name of the people query evaluation implementation.
     */
    public String getPeopleQueryEvaluatorClass() {
        return peopleQueryEvaluatorClass;
    }

    public int getThreadPoolMaxSize() {
        return threadPoolMaxSize;
    }

    public String getTransactionFactoryClass() {
        return transactionFactoryClass;
    }

    public String getTaskCleanupCronExpression() {
        return taskCleanupCronExpression;
    }

    public void setTaskCleanupCronExpression(String taskCleanupCronExpression) {
        this.taskCleanupCronExpression = taskCleanupCronExpression;
    }

    public List<TaskStatus> getRemovableTaskStatuses() {
        return removableTaskStatuses;
    }

    public void setRemovableTaskStatuses(List<TaskStatus> removableTaskStatuses) {
        this.removableTaskStatuses = removableTaskStatuses;
    }

    public boolean isEnableTaskEventPersistence() {
        return enableTaskEventPersistence;
    }

    public void setEnableTaskEventPersistence(boolean enableTaskEventPersistence) {
        this.enableTaskEventPersistence = enableTaskEventPersistence;
    }

    /**
     * @return :  true if we have a valid task cleanup configuration parameters. False otherwise.
     */
    public boolean isTaskCleanupEnabled() {
        return StringUtils.isNotEmpty(this.taskCleanupCronExpression) &&
               CronExpression.isValidExpression(taskCleanupCronExpression) &&
               removableTaskStatuses.size()>0;
    }
}
