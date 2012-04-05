/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.bam.data.publisher.servicestats;

import org.wso2.carbon.core.RegistryResources;

public final class ServiceStatisticsPublisherConstants {
    public static final String BAM_SERVICE_STATISTISTICS_PUBLISHER_MODULE_NAME = "wso2bampubsvcstat";

    // Registry persistence related constants
    public static final String STATISTISTICS_REG_PATH = RegistryResources.ROOT
            + "bam/data/publishers/service_stats";
    public static final String BAM_REG_PATH = "/carbon/bam/data/publishers/service_stats";
    public static final String ENABLE_EVENTING = "EnableEventing";
    public static final String EVENTING_ON = "ON";
    public static final String EVENTING_OFF = "OFF";
    public static final String SYSTEM_REQUEST_COUNT_THRESHOLD = "SystemRequestCountThreshold";
    public static final String ENABLE_EVENTING_DEFAULT = EVENTING_ON;
    public static final int SYSTEM_REQUEST_COUNT_THRESHOLD_DEFAULT = 20;
    public static final String BAM_SERVICE_STAT_RECEIVER_SERVICE = "BAMServiceStatisticsSubscriberService";
    public static final String BAM_SERVER_URL = "BamServerURL";

    public static final String STATISTICS_DATA_ELEMENT_NAME_FAULT_COUNT = "FaultCount";

    public static final String TRANSPORT = "https"; // TODO: it is not ideal to assume https is always availabe

    public static final String STATISTICS_DATA_NS_URI = "http://wso2.org/ns/2009/09/bam/service/statistics/data";
    public static final String STATISTICS_DATA_NS_PREFIX = "statdata";

    // OM element names
    public static final String STATISTICS_DATA_ELEMENT_NAME_EVENT = "Event";
    public static final String STATISTICS_DATA_ELEMENT_NAME_SERVICE_STATISTICS_DATA = "ServiceStatisticsData";
    public static final String STATISTICS_DATA_ELEMENT_NAME_TENANT_ID = "TenantID";
    public static final String STATISTICS_DATA_ELEMENT_NAME_SERVER_NAME = "ServerName";
    public static final String STATISTICS_DATA_ELEMENT_NAME_AVG_RESPONSE_TIME = "AverageResponseTime";
    public static final String STATISTICS_DATA_ELEMENT_NAME_MIN_RESPONSE_TIME = "MinimumResponseTime";
    public static final String STATISTICS_DATA_ELEMENT_NAME_MAX_RESPONSE_TIME = "MaximumResponseTime";
    public static final String STATISTICS_DATA_ELEMENT_NAME_REQUEST_COUNT = "RequestCount";
    public static final String STATISTICS_DATA_ELEMENT_NAME_RESPONSE_COUNT = "ResponseCount";
    public static final String STATISTICS_DATA_ELEMENT_TIMESTAMP = "Timestamp";

    public static final String STATISTICS_DATA_ELEMENT_NAME_SERVICE_NAME = "ServiceName";
    public static final String STATISTICS_DATA_ELEMENT_NAME_OPERATION_NAME = "OperationName";

    public static final String SERVICE_STATS_TOPIC = "/carbon/bam/data/publishers/service_stats";

    public static final String  TOPIC_REGISTRY_PROPERTY_NAME = "topicName";
    public static final String UUID_PROPERTY_NAME = "uuid";
    public static final String EVENT_SINK_URL_PROPERTY_NAME = "eventSinkURL";

    public static final String STATISTICS_LAST_COUNT = "lastCount";
    public static final String STATISTICS_SERVICE_NAME = "serviceName";
    public static final String STATISTICS_OPERATION_NAME = "operationName";



    public static final int EVENT_PUBLISHER_CLIENT_TIMEOUT = 2 * 60 * 1000;

}
