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
package org.wso2.carbon.analytics.hive;

public class HiveConstants {

    public static final String HIVE_DRIVER = "org.apache.hadoop.hive.jdbc.HiveDriver";
    public static final String HIVE_DEFAULT_URL = "jdbc:hive://localhost:10000/default";
    public static final String HIVE_DEFAULT_USER = "admin";
    public static final String HIVE_DEFAULT_PASSWORD = "admin";
    public static final String HIVE_SCRIPT_BASE_PATH= "/repository/hive/scripts/";
    public static final String HIVE_SCRIPT_EXT = ".hiveql";
    public static final String HIVE_CONNECTION_CONF_PATH="/repository/hive/conf/";
    public static final String HIVE_CONF_LOCAL_PATH = "conf/";
    public static final String HIVE_CONNECTION_FILE_NAME = "hive-jdbc-conf.xml";
    public static final String HIVE_DRIVER_KEY = "driver";
    public static final String HIVE_URL_KEY = "url";
    public static final String HIVE_USERNAME_KEY = "username";
    public static final String HIVE_PASSWORD_KEY = "password";
    public static final String HIVE_SCRIPT_NAME = "scriptName";
    public static final String SCRIPT_TRIGGER_CRON = "cron";
    public static final String DEFAULT_TRIGGER_CRON = "1 * * * * ? *";

    public static final String TENANT_TRACKER_PATH = "/repository/hive/tenants";
    public static final String TENANTS_PROPERTY = "Tenants";

    public static final String HIVE_DEFAULT_TASK_CLASS = "org.wso2.carbon.analytics.hive.task.HiveScriptExecutorTask";
    public static final String HIVE_TASK = "HIVE_TASK";

    public static final String DEFAULT_HIVE_DATASOURCE = "HIVE_DATASOURCE";

    public static final String TASK_TENANT_ID_KEY = "__TENANT_ID_PROP__";

    public static final String DEFAULT_HIVE_DATASOURCE_CONFIGURATION =
            "<configuration>\n" +
            "   <url>" + HIVE_DEFAULT_URL + "</url>\n" +
            "   <username>" + HIVE_DEFAULT_USER + "</username>\n" +
            "   <password>" + HIVE_DEFAULT_PASSWORD + "</password>\n" +
            "   <driverClassName>" + HIVE_DRIVER + "</driverClassName>\n" +
            "   <maxActive>50</maxActive>\n" +
            "   <maxWait>60000</maxWait>\n" +
            "   <testOnBorrow>true</testOnBorrow>\n" +
            "   <validationQuery>SELECT 1</validationQuery>\n" +
            "   <validationInterval>30000</validationInterval>\n" +
            "</configuration>";

}
