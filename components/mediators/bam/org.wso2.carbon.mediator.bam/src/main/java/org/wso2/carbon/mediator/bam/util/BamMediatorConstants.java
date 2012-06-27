/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.mediator.bam.util;


import org.wso2.carbon.core.RegistryResources;

public class BamMediatorConstants {

    public static final String BAM_HEADER_NAMESPACE_URI = "http://wso2.org/ns/2010/10/bam";
    public static final String BAM_EVENT = "BAMEvent";
    public static final String ACTIVITY_ID = "activityID";

    public static final String PROP_BAM_MESSAGE_BODY = "bam_message_body";

    public static final String PROP_REMOTE_ADDRESS = "REMOTE_ADDR";
    public static final String PROP_RECEIVER_ADDRESS = "RECEIVER_ADDR";


    public static final String PROPERTY = "Property";

    public static final String DIRECTION_IN_OUT = "IN_OUT";
    public static final String DIRECTION_IN = "IN";
    public static final String DIRECTION_OUT = "OUT";

    public static final String PROP_MSG_ARRIVAL_TIME = "BAM_MESSAGE_ARRIVAL_TIME";

    public static final String SENDER_HOST = "sender_host";

    public static final String ADMIN_SERVICE_PARAMETER = "adminService";
    public static final String HIDDEN_SERVICE_PARAMETER = "hiddenService";

    public static final String ENABLE_ACTIVITY = "EnableActivity";

    public static final String BAM_PREFIX = "bam_";

    public static final String ACTIVITY_REG_PATH = RegistryResources.COMPONENTS
                                                   + "org.wso2.carbon.bam.activity.mediation.data.publisher/activity";




    public static final String AGENT_CONFIG = "agent.xml";
    public static final String PUBLISHER_CONFIG_THREAD_POOL_ELEMENT = "ThreadPool";

    public static final String PUBLISHER_CONFIG_TASK_QUEUE_SIZE_ELEMENT = "TaskQueue";
    public static final String PUBLISHER_CONFIG_CORE_POOL_SIZE_ELEMENT = "CorePool";
    public static final String PUBLISHER_CONFIG_MAX_POOL_SIZE_ELEMENT = "MaxPool";

    public static final String PUBLISHER_CONFIG_EVENT_QUEUE_SIZE_ELEMENT = "EventQueue";

    public static final String PUBLISHER_CONFIG_CONNECTION_POOL_ELEMENT = "ConnectionPool";
    public static final String PUBLISHER_CONFIG_MAX_IDLE_SIZE_ELEMENT = "MaxIdle";
    public static final String PUBLISHER_CONFIG_TIME_GAP_EVICTION_RUN_ELEMENT = "TimeBetweenEvictionRunsMillis";
    public static final String PUBLISHER_CONFIG_MIN_IDLE_TIME_ELEMENT = "MinEvictableIdleTimeMillis";


    public static final String STATISTICS_REQUEST_RECIEVED_TIME = "wso2statistics.request.received.time";


    public static final String HTTP_HEADER_USER_AGENT = "user-agent";
    public static final String HTTP_HEADER_HOST = "host";
    public static final String HTTP_HEADER_REFERER = "referer";

    public static final String REMOTE_ADDRESS = "remote_address";
    public static final String USER_AGENT = "user_agent";
    public static final String HOST = "host";
    public static final String CONTENT_TYPE = "content_type";
    public static final String REFERER = "referer";
    public static final String REQUEST_URL = "request_url";

    public static final String SERVICE_NAME = "service_name";
    public static final String OPERATION_NAME = "operation_name";
    public static final String TIMESTAMP = "timestamp";

    public static final String MSG_ACTIVITY_ID = "bam_activity_id";
    public static final String MSG_ID = "message_id";
    public static final String MSG_BODY = "message_body";
    public static final String IN_MSG_ID = "in_message_id";
    public static final String IN_MSG_BODY = "in_message_body";
    public static final String OUT_MSG_ID = "out_message_id";
    public static final String OUT_MSG_BODY = "out_message_body";
    public static final String MSG_DIRECTION = "message_direction";


    public static final String BAM_URL = "BAMUrl";
    public static final String BAM_USER_NAME = "BAMUserName";
    public static final String BAM_PASSWORD = "BAMPassword";
    public static final String ENABLE_HTTP_TRANSPORT = "EnableHttp";
    public static final String ENABLE_SOCKET_TRANSPORT = "EnableSocket";
    public static final String BAM_SOCKET_PORT = "port";

    public static final String STREAM_NAME = "streamName";
    public static final String VERSION = "version";
    public static final String NICK_NAME = "nickName";
    public static final String DESCRIPTION = "description";


    public static final String SOAP_ENVELOP_NAMESPACE_URI = "soap_envelop_namespace";

    public static final String PUBLISH_DATA = "event_data";
    public static final String HOSTNAME_AND_PORT_SEPARATOR = ":";
    public static final String PREFIX_FOR_REGISTRY_HIDDEN_PROPERTIES = "registry.";

}
