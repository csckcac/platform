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

public class BAMRegistryResources {
	// "/carbon/bam/poll/jmx.servers/server-id-xxxx/service.data/service_group/service/operation"
	public final static String ROOT_PATH = "/carbon/bam/";
	public final static String GLOBAL_CONFIG_PATH = ROOT_PATH + "global.config";
	public final static String MONITORED_SERVERS_PATH = ROOT_PATH + "monitored.servers/";
	public final static String PULL_SERVERS_PATH = MONITORED_SERVERS_PATH + "pull.servers/";
	public final static String EVENTING_SERVERS_PATH = MONITORED_SERVERS_PATH + "eventing.servers/";
	public final static String GENERIC_SERVERS_PATH = MONITORED_SERVERS_PATH + "generic.servers/";
	public final static String JMX_SERVERS_PATH = MONITORED_SERVERS_PATH + "jmx.servers/";
	public final static String SERVICE_DATA_PATH = "service.data";

	public static final String SERVER_ID_PROPERTY = "serverID";
	public static final String SERVER_URL_PROPERTY = "serverURL";
    public static final String SERVER_TYPE = "serverType";

	// public static final String JMX_URL_PROPERTY = "jmxURL";
	public static final String USERNAME_PROPERTY = "username";
	public static final String PASSWORD_PROPERTY = "password";
	public static final String CATEGORY_PROPERTY = "category";
	public static final String DESCRIPTION_PROPERTY = "description";
	public static final String ACTIVE_PROPERTY = "active";
	public static final String TENANT_ID_PROPERTY = "tenant";
    public static final String SUBSCRIPTION_EPR_PROPERTY = "subscriptionEPR";
	public static final String SUBSCRIPTION_ID_PROPERTY = "subscriptionID";
	public static final String POLLING_INTERVAL_PROPERTY = "pollingInterval";
	public static final String SUMMARY_DELAY_PROPERTY = "summaryDelay";
	public static final String SUMMARY_INTERVAL_PROPERTY = "summaryInterval";
	public static final String DATA_RETENTION_PROPERTY = "dataRetention";
    public static final String DATA_ARCHIVAL_PROPERTY="dataArchival";
}
