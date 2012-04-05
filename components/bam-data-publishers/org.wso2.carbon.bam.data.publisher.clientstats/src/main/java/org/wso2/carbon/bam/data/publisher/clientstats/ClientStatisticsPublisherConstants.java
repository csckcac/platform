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
package org.wso2.carbon.bam.data.publisher.clientstats;

import org.wso2.carbon.core.RegistryResources;

/*
 * Constants which are used to generate client specific stats.
 */
public class ClientStatisticsPublisherConstants {
    
    public static final String BAM_CLIENT_STATISTISTICS_PUBLISHER_MODULE_NAME = "wso2bampubclientstat";

    // Registry persistence related constants
    public static final String STATISTISTICS_REG_PATH = RegistryResources.ROOT
            + "bam/data/publishers/service_stats";
    public static final String BAM_REG_PATH = "/carbon/bam/data/publishers/service_stats";
    
    // Event related constants.
    public static final String ENABLE_EVENTING = "EnableEventing";
    public static final String EVENTING_ON = "ON";
    public static final String EVENTING_OFF = "OFF";
    public static final String ENABLE_EVENTING_DEFAULT = EVENTING_ON;
    public static final String BAM_USER_DEFINED_EVENT_TOPIC_SEPARATOR = "/";
    public static final String BAM_USER_DEFINED_EVENT_NAME = "ChildDeleted";
    public static final String BAM_USER_DEFINED_EVENT_TOPIC = "topic";
    public static final String BAM_USER_DEFINED_EVENT_NOTIFICATION_NAMESPACE = "http://wso2.org/bam/service/statistics/notify";
	public static final String BAM_USER_DEFINED_EVENT_PUBLISH_ACTION = "http://ws.apache.org/ws/2007/05/eventing-extended/Publish";
	public static final String BAM_USER_DEFINED_EVENT_COMMON_SUBSCRIBER_SERVICE = "/services/BAMServerUserDefinedDataSubscriberService";
	public static final String BAM_USER_DEFINED_EVENT_NOTIFICATION_PROPERTY = "EventNotified"; // to
                                                                                               // make
                                                                                               // sure
                                                                                               // event
                                                                                               // generated
                                                                                               // once
	
	// User/client related constants(properties).
	public static final String BAM_USER_DEFINED_HTTPS_SERVER_PROPERTY = "BAMHTTPSServer";
    public static final String BAM_USER_DEFINED_WSAS_SERVER_PROPERTY = "WSASServerEP";
    public static final String BAM_USER_DEFINED_USER_PARAM_PROPERTY = "BAMUserParam";
    public static final String BAM_USER_DEFINED_SERVICE_PROPERTY = "BAMServiceName";
    public static final String BAM_USER_DEFINED_OPERATION_PROPERTY = "BAMOperationName";
   
    /*
     * Client Statistics related constants(properties).
     */
    public static final String BAM_USER_DEFINED_UUID_PROPERTY = "BAMUUID";
    public static final String BAM_USER_DEFINED_REMOTE_IPADDRESS_PROPERTY = "BAMRemoteIP";
    
    // global constants(properties)
    public static final String BAM_USER_DEFINED_GLOBAL_REQUEST_COUNTER_PROPERTY = "BAMGlobalRequestCounter";
    public static final String BAM_USER_DEFINED_GLOBAL_RESPONSE_COUNTER_PROPERTY = "BAMGlobalResponseCounter";
    public static final String BAM_USER_DEFINED_GLOBAL_FAULT_COUNTER_PROPERTY = "BAMGlobalFaultCounter";
    
    public static final String BAM_USER_DEFINED_RESPONSE_TIME_PROCESSOR_PROPERTY = "BAMResponseTimeProcessor";
    public static final String BAM_USER_DEFINED_REQUEST_RECEIVED_TIME_PROPERTY = "BAMRequestReceivedTime";
    
    // service constants(properties)
    public static final String BAM_USER_DEFINED_SERVICE_RESPONSE_TIME_PROCESSOR_PROPERTY = "BAMServiceResponseTimeProcessor";
    public static final String BAM_USER_DEFINED_SERVICE_REQUEST_COUNTER_PROPERTY = "BAMServiceRequestCounter";
    public static final String BAM_USER_DEFINED_SERVICE_FAULT_COUNTER_PROPERTY = "BAMServiceFaultCounter";
   
    // operation constants(properties)
    public static final String BAM_USER_DEFINED_OPERATION_RESPONSE_TIME_PROCESSOR_PROPERTY = "BAMOperationResponseTimeProcessor";
    public static final String BAM_USER_DEFINED_IN_OPERATION_COUNTER_PROPERTY = "BAMInOperationCounter";
    public static final String BAM_USER_DEFINED_OUT_OPERATION_COUNTER_PROPERTY = "BAMOutOperationCounter";
    public static final String BAM_USER_DEFINED_OPERATION_FAULT_COUNTER_PROPERTY = "BAMOperationFaultCounter";
   
   
  

   
}
