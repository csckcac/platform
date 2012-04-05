/*
 * Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * WSO2 Inc. licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.wso2.carbon.bam.data.publisher.mediationstats;

import org.wso2.carbon.core.RegistryResources;

/**
 * Mediation publisher constants
 */
public class MDPublisherConstants {
  

    // Registry persistence related constants
    public static final String STATISTICS_REG_PATH =
            RegistryResources.ROOT +
            "bam/data/publishers/mediation_stats";
    public static final String BAM_REG_PATH = "/carbon/bam/data/publishers/mediation_stats";
    public static final String ENABLE_EVENTING = "EnableEventing";
    public static final String EVENTING_ON = "ON";
    public static final String EVENTING_OFF = "OFF";
    public static final String PROXY_COUNT_THRESHOLD = "ProxyCountThreshold";
    public static final String SEQUENCE_COUNT_THRESHOLD = "SequenceCountThreshold";
    public static final String ENDPOINT_COUNT_THRESHOLD = "EndpointCountThreshold";
    public static final String ENABLE_EVENTING_DEFAULT = EVENTING_ON;
    public static final int PROXY_COUNT_THRESHOLD_DEFAULT = 20;
    public static final int SEQUENCE_COUNT_THRESHOLD_DEFAULT = 20;
    public static final int ENDPOINT_COUNT_THRESHOLD_DEFAULT = 20;
    public static final String BAM_MEDIATION_STAT_RECEIVER_SERVICE =
            "BAMServerUserDefinedDataSubscriberService";

    // key, value constants
    public static final String BAM_MAX_PROCESS_TIME = "MaxProcessingTime";
    public static final String BAM_MIN_PROCESS_TIME = "MinProcessingTime";
    public static final String BAM_AVG_PROCESS_TIME = "AvgProcessingTime";
    public static final String BAM_COUNT = "Count";
    public static final String BAM_CUMULATIVE_COUNT = "CumulativeCount";
    public static final String BAM_FAULT_COUNT = "FaultCount";
    public static final String BAM_ID = "ID";
}
