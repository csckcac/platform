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
package org.wso2.carbon.bam.activity.mediation.data.publisher.util;


import org.wso2.carbon.core.RegistryResources;

public class ActivityPublisherConstants {

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

}
