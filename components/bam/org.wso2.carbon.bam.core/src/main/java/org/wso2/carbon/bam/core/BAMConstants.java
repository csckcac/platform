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

package org.wso2.carbon.bam.core;

public class BAMConstants {

	public final static String SERVICES_SUFFIX = "services";
	public final static String AUTH_ADMIN_SERVICE_2_0_X = "AuthenticationAdminService";
    public final static String AUTH_ADMIN_SERVICE_3_1_0 = "AuthenticationAdmin";
    public final static String AUTH_ADMIN_SERVICE_3_2_0 = "AuthenticationAdmin";

	public final static String SERVICE_GROUP_ADMIN_SERVICE = "ServiceGroupAdmin";
	public final static String SERVICE_ADMIN_SERVICE = "ServiceAdmin";
	public final static String OPERATION_ADMIN_SERVICE = "OperationAdmin";
	public final static String STATISTICS_ADMIN_SERVICE = "StatisticsAdmin";
	public static final String LOGIN_STAT_SERVICE = "LoginStatisticsAdminService";

	// BAM Data Services
	public final static String BAM_CONFIGURATION_SERVICE = "BAMConfigurationDS";
	public final static String BAM_DATACOLLECTION_SERVICE = "BAMDataCollectionDS";
	public final static String BAM_SUMMARYGENERATION_SERVICE = "BAMSummaryGenerationDS";
	public final static String BAM_STATQUERY_SERVICE = "BAMStatQueryDS";
	public final static String BAM_SUMMARYQUERY_SERVICE = "BAMSummaryQueryDS";

	public final static String SERVER_TYPE_GENERIC = "GenericServer";
	public final static String SERVER_TYPE_EVENTING = "EventingServer";
	public final static String SERVER_TYPE_PULL = "PullServer";
	public final static String SERVER_TYPE_JMX = "JMXServer";
	public final static int DEFAULT_TENANT = 0;

	// Eventing service related constants
	public static final String MEDIATION_DIR_IN = "In";
	public static final String MEDIATION_DIR_OUT = "Out";

    public static final int MILLISECONDS_MULTIPLIER = 1000;

    public static final long DEFAULT_INITIAL_SUMMARY_GEN_DELAY = 10 * 60000; //ten minutes
    public static final long DEFAULT_SUMMARY_GEN_INTERVAL = 60 * 60000; //one hour
    public static final long DEFAULT_ARCHIVAL_CHECK_INTERVAL = 60 * 60000 * 24; // one day

    public static final int DEFAULT_ACTIVITY_POOL_SIZE = 50;
    public static final int DEFAULT_SERVICE_POOL_SIZE = 200;
    public static final int DEFAULT_MEDIATION_POOL_SIZE = 50;

    public static final long DEFAULT_INITIAL_DATA_COLLECTION_DELAY = 60000; //one minute
    public static final long DEFAULT_DATA_COLLECTION_INTERVAL = 60000; //one minute

    public static final long DEFAULT_TASK_BREAKDOWN_LENGTH = 100;
    public static final long DEFAULT_SLEEP_TIME_BETWEEN_TASKS = 100;

    public static final String BAM_CONSTANTS_CONFIG_FILE = "bam.xml";
    public static final String SUMMARY_GENERATION_ELEMENT = "summaryGeneration";
    public static final String DATA_COLLECTION_ELEMENT = "dataCollection";
    public static final String INITIAL_DELAY_ELEMENT = "initial-delay";
    public static final String INTERVAL_ELEMENT = "interval";
    public static final String THREAD_POOL_SIZES_ELEMENT = "threadPoolSizes";
    public static final String ACTIVITY_ELEMENT = "activity";
    public static final String SERVICE_ELEMENT = "service";
    public static final String MEDIATION_ELEMENT = "mediation";

    public static final String TASK_BREAKDOWN_LENGTH_ELEMENT = "taskBreakDownLength";

    public static final String SLEEP_TIME_BETWEEN_TASKS_ELEMENT = "sleepTimeBetweenTasks";

}
