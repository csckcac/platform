/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.reporting.core.datasource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.commons.datasource.DataSourceInformationRepository;
import org.apache.synapse.commons.datasource.DataSourceRepositoryHolder;
import org.apache.synapse.commons.datasource.DataSourceRepositoryManager;
import org.wso2.carbon.datasource.DataSourceInformationRepositoryService;
import org.wso2.carbon.reporting.api.ReportingException;
import org.wso2.carbon.reporting.core.internal.ReportingComponent;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * This class used to manage data source related information.
 */
public class ReportDataSourceManager {

    private static Log log = LogFactory.getLog(ReportDataSourceManager.class);

    /**
     * @param dataSourceName na,e of the data source
     * @return Connection
     * @throws org.wso2.carbon.reporting.api.ReportingException if failed to get connection for data source
     */
    public Connection getJDBCConnection(String dataSourceName) throws ReportingException {
        Connection connection = null;
        DataSourceRepositoryManager repositoryManager;
        DataSource dataSource;

        DataSourceInformationRepositoryService repositoryService =
                ReportingComponent.getCarbonDataSourceService();
        if (repositoryService != null) {
            DataSourceInformationRepository datasourceRepo =
                    repositoryService.getDataSourceInformationRepository();
            DataSourceRepositoryHolder dataSourceHelper = DataSourceRepositoryHolder.getInstance();
            dataSourceHelper.init(datasourceRepo, null);
            repositoryManager = dataSourceHelper.getDataSourceRepositoryManager();
            if (repositoryManager != null) {
                dataSource = repositoryManager.getDataSource(dataSourceName);
                if (dataSource != null) {
                    try {
                        connection = dataSource.getConnection();
                    } catch (SQLException e) {
                        throw new ReportingException("Failed to get data source connection for "
                                                     + "\"" + dataSourceName + "\"", e);
                    }
                }
            }
        }
        return connection;
    }

}
