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

package org.wso2.carbon.bam.util;

public class BAMConstants {

	public final static String SERVICES_SUFFIX = "services";

	// public final static String AUTH_ADMIN_SERVICE_OLD = "AuthenticationAdminService";
	// public final static String AUTH_ADMIN_SERVICE_NEW = "AuthenticationAdmin";

	public final static String AUTH_ADMIN_SERVICE_3_0_0 = "AuthenticationAdmin";
	public final static String AUTH_ADMIN_SERVICE_2_0_X = "AuthenticationAdminService";

	public final static String SERVICE_ADMIN_SERVICE = "ServiceAdmin";
	public final static String OPERATION_ADMIN_SERVICE = "OperationAdmin";
	public final static String SERVICE_GROUP_ADMIN_SERVICE = "ServiceGroupAdmin";
	public final static String STATISTICS_ADMIN_SERVICE = "StatisticsAdmin";
	public static final String LOGIN_STAT_SERVICE = "LoginStatisticsAdminService";

	// BAM Data Services
	public final static String BAM_CONFIGURATION_SERVICE = "BAMConfigurationDS";
	public final static String BAM_DATACOLLECTION_SERVICE = "BAMDataCollectionDS";
	public final static String BAM_SUMMARYGENERATION_SERVICE = "BAMSummaryGenerationDS";
	public final static String BAM_STATQUERY_SERVICE = "BAMStatQueryDS";
	public final static String BAM_SUMMARYQUERY_SERVICE = "BAMSummaryQueryDS";

	public final static int DEFAULT_TENANT = 0;
    public final static int NO_TENANT_MODE= -1; // This is the value in Carbon context when no tenant is set

	// Eventing service related constants
	public final static String BAM_SERVICE_STATISTICS_SUBSCRIBER_SERVICE = "BAMServiceStatisticsSubscriberService";
	public final static String BAM_SERVICE_STATISTICS_PUBLISHER_SERVICE = "/services/BAMServiceStatisticsPublisherService";
	public final static String BAM_MEDIATION_STATISTICS_PUBLISHER_SERVICE = "/services/BAMMediationStatisticsPublisherService";
	public final static String BAM_USER_DEFINED_DATA_SUBSCRIBER_SERVICE = "BAMServerUserDefinedDataSubscriberService";

	public final static String BAM_SERVICE_ACTIVITY_STATISTICS_SUBSCRIBER_SERVICE = "BAMActivityDataStatisticsSubscriberService";
	public final static String BAM_SERVICE_ACTIVITY_STATISTICS_PUBLISHER_SERVICE = "/services/BAMServiceActivityStatisticsPublisherService";

	public final static String EVENT_BROKER_SERVICE = "/services/EventBrokerService";

	public static final String SQL_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

	public static final String MEDIATION_DIR_IN = "In";
	public static final String MEDIATION_DIR_OUT = "Out";

    public static final boolean SERVER_ACTIVE_STATE = true;
    public static final boolean SERVER_INACTIVE_STATE = false;

    public static final String LOCAL_TRANSPORT = "local:/";

    public static final int UNASSIGNED_SERVER_ID = -1;

    public static final int SERVER_SUCCESSFULLY_ADDED = 0;
    public static final int SERVER_ALREADY_EXIST = 1;
    public static final int SERVER_NOT_RUNNING = 2;
    public static final int SERVER_AUTH_FAILED = 3;
    public static final int SERVER_URL_MALFORMED = 4;
    public static final int SERVER_AUTH_FAILED_404 = 5;


    public final static String SERVER_TYPE_GENERIC = "GenericServer";
    public final static String SERVER_TYPE_EVENTING = "EventingServer";
    public final static String SERVER_TYPE_PULL = "PullServer";
    public final static String SERVER_TYPE_JMX = "JMXServer";


    public final static int SERVICE_STAT_TYPE = 1;
    public final static int MESSAGE_STAT_TYPE = 2;
    public final static int MEDIATION_STAT_TYPE = 4;
    public final static int GENERIC_STAT_TYPE = 8;
}
