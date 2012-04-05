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

package org.wso2.carbon.bam.ui;

public class BAMUIConstants {
	/* public static final String SERVER_NAME = "serverName"; */
	public static final String USERNAME = "username";
    public static final String EPR = "epr";
	public static final String PASSWORD = "password";
	public static final String SERVER_JMX_STRING = "jmxString";
	public static final String SERVER_URL = "serverURL";

	public static final String ACTION_SUBMIT = "submitAction";

	public static final String PARAM_SERVER_ID = "serverID";

	public static final String POLLING_INTERVAL = "pollingInterval";

    public static final String POLLING_DELAY = "pollingDelay";

	public static final String BAM_CONFIG_ADMIN_SERVICE = "BAMConfigAdminService";

	public static final String DATA_RETENTION_PERIOD = "dataRetentionPeriod";
    public static final String DATA_ARCHIVAL_PERIOD = "dataArchivalPeriod";
	public final static String BAM_SERVICE_STATISTICS_SUBSCRIBER_SERVICE = "BAMServiceStatisticsSubscriberService";
	public final static String BAM_SERVICE_STATISTICS_PUBLISHER_SERVICE = "/services/BAMServiceStatisticsPublisherService";
	public final static String BAM_MEDIATION_STATISTICS_PUBLISHER_SERVICE = "/services/BAMMediationStatisticsPublisherService";
	public final static String BAM_USER_DEFINED_DATA_SUBSCRIBER_SERVICE = "BAMServerUserDefinedDataSubscriberService";

	public final static String BAM_SERVICE_ACTIVITY_STATISTICS_SUBSCRIBER_SERVICE = "BAMActivityDataStatisticsSubscriberService";
	public final static String BAM_SERVICE_ACTIVITY_STATISTICS_PUBLISHER_SERVICE = "/services/BAMServiceActivityStatisticsPublisherService";

//	public final static String EVENT_BROKER_SERVICE = "/services/EventBrokerService";

    public static final String DEFAULT_EVENT_BROKER_NAME = "EventBrokerService";
    
    public final static String BAM_MESSAGE_RECEIVER ="BAMMessageReceiver";

    public static final String SERVICES_PATH = "/services/";

    public static final String  BAM_CONSTANTS_CONFIG_FILE = "bam.xml";

    public static final String  EVENTBROKER_SERVICE_NAME = "eventBrokerServiceName";
}
