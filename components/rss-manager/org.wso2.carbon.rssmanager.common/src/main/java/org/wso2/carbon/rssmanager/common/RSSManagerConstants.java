/*
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.rssmanager.common;

import javax.naming.Name;

/**
 * Contains the constants associated with the component.
 */
public final class RSSManagerConstants {

    public static final String WSO2_RSS_INSTANCE_TYPE = "WSO2_RSS";
    public static final String WSO2_LOCAL_RDS_INSTANCE_TYPE = "WSO2_LOCAL_RDS";
    public static final String USER_DEFINED_INSTANCE_TYPE = "USER_DEFINED";
    public static final String RSS_CONFIG_XML_NAME = "rss-config.xml";
    public static final String STRATOS_RSS = "STRATOS_RSS";
    public static final String LOCAL = "LOCAL";
    public static final String RDS = "RDS";

    public static final String JDBC_PREFIX = "jdbc";

    /* Prefixes of supported database types */
    public static final String MYSQL_PREFIX = "mysql";
    public static final String ORACLE_PREFIX = "oracle";

    /* Driver Names of supported database types */
    public static final String MYSQL_DRIVER = "com.mysql.jdbc.Driver";
    public static final String ORACLE_DRIVER = "oracle.jdbc.driver.OracleDriver";

    /* Database permissions */
    public static final String SELECT_PRIV = "Select_priv";
    public static final String INSERT_PRIV = "Insert_priv";
    public static final String UPDATE_PRIV = "Update_priv";
    public static final String DELETE_PRIV = "Delete_priv";
    public static final String CREATE_PRIV = "Create_priv";
    public static final String DROP_PRIV = "Drop_priv";
    public static final String RELOAD_PRIV = "Reload_priv";
    public static final String SHUTDOWN_PRIV = "Shutdown_priv";
    public static final String PROCESS_PRIV = "Process_priv";
    public static final String FILE_PRIV = "File_priv";
    public static final String GRANT_PRIV = "Grant_priv";
    public static final String REFERENCES_PRIV = "References_priv";
    public static final String INDEX_PRIV = "Index_priv";
    public static final String ALTER_PRIV = "Alter_priv";
    public static final String SHOW_DB_PRIV = "Show_db_priv";
    public static final String SUPER_PRIV = "Super_priv";
    public static final String CREATE_TMP_TABLE_PRIV = "Create_tmp_table_priv";
    public static final String LOCK_TABLES_PRIV = "Lock_tables_priv";
    public static final String EXECUTE_PRIV = "Execute_priv";
    public static final String REPL_SLAVE_PRIV = "Repl_slave_priv";
    public static final String REPL_CLIENT_PRIV = "Repl_client_priv";
    public static final String CREATE_VIEW_PRIV = "Create_view_priv";
    public static final String SHOW_VIEW_PRIV = "Show_view_priv";
    public static final String CREATE_ROUTINE_PRIV = "Create_routine_priv";
    public static final String ALTER_ROUTINE_PRIV = "Alter_routine_priv";
    public static final String CREATE_USER_PRIV = "Create_user_priv";
    public static final String EVENT_PRIV = "Event_priv";
    public static final String TRIGGER_PRIV = "Trigger_priv";
    public static final String SSL_TYPE = "ssl_type";
    public static final String SSL_CIPHER = "ssl_cipher";
    public static final String X509_ISSUER = "x509_issuer";
    public static final String X509_SUBJECT = "x509_subject";
    public static final String MAX_QUESTIONS = "max_questions";
    public static final String MAX_CONNECTIONS = "max_connections";
    public static final String MAX_USER_CONNECTIONS = "max_user_connections";
    public static final String MAX_UPDATES = "max_updates";

    public static final String DATA_SOURCE = "dataSource";
    public static final String URL = "url";
    public static final String USER_NAME = "username";
    public static final String PASSWORD = "password";
    public static final String DRIVER_NAME = "driverClassName";
    public static final String MAX_ACTIVE = "maxActive";
    public static final String MAX_WAIT = "maxWait";
    public static final String MIN_IDLE = "minIdle";
    public static final String MAX_IDLE = "maxIdle";
    public static final String VALIDATION_QUERY = "validationQuery";
    public static final String DIALECT_FLAG = "dialectFlag";
    public static final String TEST_WHILE_IDLE = "testWhileIdle";
    public static final String TIME_BETWEEN_EVICTION_RUNS_MILLIS = "timeBetweenEvictionRunsMillis";
    public static final String MIN_EVIC_TABLE_IDLE_TIME_MILLIS = "minEvictableIdleTimeMillis";
    public static final String NUM_TESTS_PEREVICTION_RUN = "numTestsPerEvictionRun";
    public static final int DEFAULT_MAX_ACTIVE = 40;
    public static final int DEFAULT_MAX_WAIT = 1000 * 60;
    public static final int DEFAULT_MIN_IDLE = 5;
    public static final int DEFAULT_MAX_IDLE = 6;

    public static final String STANDARD_TRANSACTION_MANAGER_JNDI_NAME = "java:comp/TransactionManager";
    public static final String STANDARD_USER_TRANSACTION_JNDI_NAME = "java:comp/UserTransaction";
    public static final String RSS_MANAGER_SYNC_GROUP_NAME = "_RSS_MANAGER_GROUP";
    public static final String RDBMS_DATA_SOURCE_TYPE = "RDBMS";
}