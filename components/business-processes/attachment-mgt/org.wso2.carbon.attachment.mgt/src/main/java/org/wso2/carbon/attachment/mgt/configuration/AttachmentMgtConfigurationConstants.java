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


public class AttachmentMgtConfigurationConstants {

    public static final String ATTACHMENT_MGT_CONFIG_FILE = "attach-mgt-conf.properties";
    public static final String DATASOURCE_NAME = "datasource.name";
    public static final String DATASOURCE_JNDI_CONTEXT_FACTORY = "datasource.JNDI.contextFactory";
    public static final String DATASOURCE_JNDI_PROVIDER_URL = "datasource.JNID.providerURL";
    public static final String DAO_CONNECTION_FACTORY_IMPL_CLASS = "dao.connection.factory.impl" +
                                                                   ".class";
    public static final String DAO_TRANSFORMER_FACTORY_IMPL_CLASS = "dao.transformer.factory" +
                                                                    ".impl" +
                                                                   ".class";
    public static final String TRANSACTION_FACTORY_CLASS = "transaction.factory.class";
    public static final String DAO_PERSISTENCE_CONFIG_GENERATE_DDL = "dao.PersistenceConfig" +
                                                                    ".GenerateDdl";
    public static final String DAO_PERSISTENCE_CONFIG_SHOW_SQL = "dao.PersistenceConfig.ShowSql";

    public static final String CARBON_CONFIG_PORT_OFFSET_NODE = "Ports.Offset";

    public static final String ATTACHMENT_DOWNLOAD_SERVELET_URL_PATTERN = "/attachment-mgt/download";
}
