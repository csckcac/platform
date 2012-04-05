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
package org.wso2.carbon.bam.core.persistence;

public class DatabaseConstants {

    public static final String DATASOURCES_FILE = "datasources.properties";
    public static final String BAM_SCHEMA_PREFIX = "bam_schema_";
    public static final String BAM_SCHEMA_LOCATION = "/dbscripts/bam";
    public static final String DEFAULT_VALIDATION_QUERY = "SELECT * from BAM_SERVER";

    public static final String DATASOURCE_NAME_PROPERTY = "synapse.datasources";
    public static final String DATABASE_URL_PROPERTY = "url";
    public static final String DRIVER_CLASS_PROPERTY = "driverClassName";
    public static final String USERNAME_PROPERTY = "username";
    public static final String PASSWORD_PROPERTY = "password";
    public static final String CONFIG_NAME_PROPERTY = "dsName";
    public static final String MAX_ACTIVE_PROPERTY = "maxActive";
    public static final String MAX_IDLE_PROPERTY = "maxIdle";
    public static final String MAX_WAIT_PROPERTY = "maxWait";
    public static final String MIN_IDLE_PROPERTY = "minIdle";
    public static final String VALIDATION_QUERY_PROPERTY = "validationQuery";

    public static final int DEFAULT_MAX_ACTIVE = 40;
    public static final int DEFAULT_MAX_IDLE = 20;
    public static final int DEFAULT_MIN_IDLE = 5;
    public static final int DEFAULT_MAX_WAIT = 1000 * 60;

}
