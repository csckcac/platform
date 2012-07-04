/*
 * Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.attachment.mgt.configuration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.attachment.mgt.util.ConfigurationUtil;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * In-memory configuration manager for AttachmentServer
 */
public class AttachmentServerConfiguration {
    /**
     * Logger class
     */
    private static Log log = LogFactory.getLog(AttachmentServerConfiguration.class);

    private String dataSourceName;

    private String dataSourceJNDIRepoInitialContextFactory;

    private String dataSourceJNDIRepoProviderURL;

    private String daoConnectionFactoryClass;

    private String daoTransformerFactoryClass;

    private String transactionFactoryClass;

    /**
     * Referred when constructing the JPA specific DAO-Manager
     */
    private boolean generateDdl;

    /**
     * Referred when constructing the JPA specific DAO-Manager
     */
    private boolean showSql;

    public AttachmentServerConfiguration() {
        this.dataSourceName = "attachmentds";
        this.dataSourceJNDIRepoInitialContextFactory = null;
        this.dataSourceJNDIRepoProviderURL = null;
        this.daoConnectionFactoryClass = "org.wso2.carbon.attachment.mgt.core.dao.impl.jpa" +
                ".openjpa.AttachmentMgtDAOConnectionFactoryImpl";
        this.daoTransformerFactoryClass = "org.wso2.carbon.attachment.mgt.core.dao.impl.jpa.openjpa.AttachmentMgtDAOTransformerFactoryImpl";
        this.transactionFactoryClass = null;
        this.generateDdl = true;
        this.showSql = false;
    }

    public AttachmentServerConfiguration(File attMgtConfigFile) {
        try {
            Properties serverConfigProperties = ConfigurationUtil.getPropertyMap(attMgtConfigFile);

            this.dataSourceName =
                    serverConfigProperties.getProperty(AttachmentMgtConfigurationConstants.
                            DATASOURCE_NAME);
            this.dataSourceJNDIRepoInitialContextFactory =
                    serverConfigProperties.getProperty(AttachmentMgtConfigurationConstants.
                            DATASOURCE_JNDI_CONTEXT_FACTORY);
            if (serverConfigProperties.getProperty(
                    AttachmentMgtConfigurationConstants.DATASOURCE_JNDI_PROVIDER_URL) != null) {
                this.dataSourceJNDIRepoProviderURL =
                        applyPortOffset(serverConfigProperties.getProperty(
                                AttachmentMgtConfigurationConstants.DATASOURCE_JNDI_PROVIDER_URL));
            }
            this.daoConnectionFactoryClass =
                    serverConfigProperties.getProperty(AttachmentMgtConfigurationConstants.
                            DAO_CONNECTION_FACTORY_IMPL_CLASS);
            this.daoTransformerFactoryClass =
                    serverConfigProperties.getProperty(AttachmentMgtConfigurationConstants.
                            DAO_TRANSFORMER_FACTORY_IMPL_CLASS);
            this.transactionFactoryClass = serverConfigProperties.getProperty
                    (AttachmentMgtConfigurationConstants.TRANSACTION_FACTORY_CLASS);
            this.generateDdl = Boolean.parseBoolean(serverConfigProperties.getProperty
                    (AttachmentMgtConfigurationConstants.DAO_PERSISTENCE_CONFIG_GENERATE_DDL));
            this.showSql = Boolean.parseBoolean(serverConfigProperties.getProperty
                    (AttachmentMgtConfigurationConstants.DAO_PERSISTENCE_CONFIG_SHOW_SQL));

        } catch (IOException e) {
            log.error("Failed to load Attachment Mgt server configuration file.", e);
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

    public String getDaoConnectionFactoryClass() {
        return daoConnectionFactoryClass;
    }

    public String getDaoTransformerFactoryClass() {
        return daoTransformerFactoryClass;
    }

    public String getTransactionFactoryClass() {
        return transactionFactoryClass;
    }

    public boolean isGenerateDdl() {
        return generateDdl;
    }

    public boolean isShowSql() {
        return showSql;
    }

    private String applyPortOffset(String url) {
        int portOffset = getCarbonPortOffset();

        // We need to adjust the port value according to the offset defined in the carbon configuration.
        String portValueString = url.substring(
                url.lastIndexOf(':') + 1,
                url.length());

        String urlWithoutPort = url
                .substring(0, url.lastIndexOf(':') + 1);


        int actualPortValue = Integer.parseInt(portValueString);
        int correctedPortValue = actualPortValue + portOffset;

        return urlWithoutPort.concat(Integer.toString(correctedPortValue));
    }

    private int getCarbonPortOffset() {

        String offset = CarbonUtils.getServerConfiguration().getFirstProperty(
                AttachmentMgtConfigurationConstants.CARBON_CONFIG_PORT_OFFSET_NODE);

        try {
            return ((offset != null) ? Integer.parseInt(offset.trim()) :
                    0);
        } catch (NumberFormatException e) {
            log.warn("Error occurred while reading port offset. Invalid port offset: " +
                    offset + " Setting the port offset to 0",
                    e);
            return 0;
        }
    }
}
