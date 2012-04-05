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
package org.wso2.bam.integration.test.tasks;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

public class JDBCProcessor {

    private static final Log log = LogFactory.getLog(JDBCProcessor.class);

    private DataSource dataSource;

    public JDBCProcessor() throws IOException {
        this.dataSource = initDataSource();
    }

    public void backDateServiceData() throws Exception {
        String select = "SELECT DISTINCT BAM_TIMESTAMP FROM BAM_SERVER_DATA";
        String update = "UPDATE BAM_SERVER_DATA SET BAM_TIMESTAMP='2011-03-28 10:25:52' WHERE BAM_ID='3'";

        Connection conn = dataSource.getConnection();
        Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_UPDATABLE);
        ResultSet selectResults = stmt.executeQuery(select);

        while (selectResults.next()) {
            Date date = selectResults.getDate(1);
            log.info("TimeStamp:******************* " + date.getTime());
        }
    }

    private DataSource initDataSource() throws IOException {

        Properties props = new Properties();
        props.load(JDBCProcessor.class.getResourceAsStream("/" + "datasource.properties"));
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(props.getProperty("driver"));
        dataSource.setUrl(props.getProperty("url"));
        dataSource.setUsername(props.getProperty("username"));
        dataSource.setPassword(props.getProperty("password"));

        String validationQuery = props.getProperty("validationQuery");
        if (validationQuery != null) {
            dataSource.setValidationQuery(validationQuery);
        }

        String maxActive = props.getProperty("maxActive");
        if (maxActive != null) {
            dataSource.setMaxActive(Integer.parseInt(maxActive));
        }

        String initialSize = props.getProperty("initialSize");
        if (initialSize != null) {
            dataSource.setInitialSize(Integer.parseInt(initialSize));
        }

        String maxIdle = props.getProperty("maxIdle");
        if (maxIdle != null) {
            dataSource.setMaxIdle(Integer.parseInt(maxIdle));
        }

        log.info("Created new data source to: " + dataSource.getUrl());
        return dataSource;
    }

}
