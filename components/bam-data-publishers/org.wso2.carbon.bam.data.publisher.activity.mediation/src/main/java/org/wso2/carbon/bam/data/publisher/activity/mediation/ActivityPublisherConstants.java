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

package org.wso2.carbon.bam.data.publisher.activity.mediation;

import org.wso2.carbon.core.RegistryResources;

import javax.xml.namespace.QName;

public class ActivityPublisherConstants {

    public static final String PROP_APPLICATION_FAILURE = "application_failure";
    public static final String PROP_TECHNICAL_FAILURE = "technical_failure";
    public static final String PROP_APPLICATION_FAILURE_DETAIL = "application_failure_detail";
    public static final String PROP_TECHNICAL_FAILURE_DETAIL = "technical_failure_detail";
    public static final String PROP_ACTIVITY_TYPE = "activity_type";
    public static final String PROP_MESSAGE_TYPE = "message_type";
    public static final String PROP_MESSAGE_FORMAT = "message_format";
    public static final String PROP_ACTIVITY_PROPERTY = "activity_property";
    public static final String PROP_ACTIVITY_ID = "bam_activity_id";
    public static final String PROP_BAM_MESSAGE_BODY = "bam_message_body";

    public static final String PROP_ARC_KEY = "arc_key";
    public static final String PROP_ARC_STATUS = "arc_status";
    public static final String PROP_ARC_DETAIL = "arc_detail";
    public static final String PROP_FAILURE_UUID = "if_fail_uuid";
    public static final String PROP_FAILURE_REPLAY_OPERATION = "if_fail_replay_operation";

    public static final String PROP_REMOTE_ADDRESS = "REMOTE_ADDR";
    public static final String PROP_RECEIVER_ADDRESS = "RECEIVER_ADDR";
    public static final String PROP_MSG_ARRIVAL_TIME = "BAM_MESSAGE_ARRIVAL_TIME";

    public static final String EMPTY_STRING = "";
    public static final String ENABLE_EVENTING = "EnableEventing";
    public static final String EVENTING_ON = "ON";
    public static final String EVENTING_OFF = "OFF";
    public static final String ENABLE_EVENTING_DEFAULT = EVENTING_ON;
    public static final String MESSAGE_THRESHOLD = "MessageThreshold";
    public final static int MESSAGE_THRESHOLD_DEFAULT = 2;
    public static final String MESSAGE_LOOKUP_ON = "ON";
    public static final String MESSAGE_LOOKUP_OFF = "OFF";
    public static final String MESSAGE_LOOKUP_DEFAULT = MESSAGE_LOOKUP_OFF;
    public static final String ENABLE_MESSAGE_LOOKUP = "EnableMessageLookup";
    public static final String XPATH_EXPRESSION = "XPathExpression";
    public static final String MESSAGE_DUMPING_ON = "ON";
    public static final String MESSAGE_DUMPING_OFF = "OFF";
    public static final String MESSAGE_DUMPING_DEFAULT = MESSAGE_DUMPING_OFF;
    public static final String ENABLE_MESSAGE_DUMPING = "EnableMessageDumping";

    public static final String XPATH_PROPERTY = "XPath";
    public static final String NAMESAPCE_PROPERTY_PREFIX = "ns";
    public static final String XPATH_ROOT_PATH = "xpaths";

    public static final String ACTIVITY_XPATH_EXPRESSION_KEY = "ExpressionKey";
    public static final String ACTIVITY_XPATH_ALIAS = "Alias";
    public static final String XPATH_NAMESPACES = "Namespaces";
    public static final String XPATH_NAMESPACE = "Namespace";

    public final static String BAM_ACTIVITY_ID_HEADER_NAMESPACE_URI = "http://wso2.org/ns/2010/10/bam";
    public static final String ACTIVITY_DATA_NS_URI = "http://wso2.org/ns/2009/09/bam/service/activity/data";
    public static final String ACTIVITY_DATA_NS_PREFIX = "bam";
    public static final String ACTIVITY_DATA_ELEMENT_EVENT = "Event";
    public static final String ACTIVITY_DATA_ELEMENT = "ActivityData";
    public static final String ACTIVITY_DATA_ELEMENT_SERVER_NAME = "ServerName";
    public static final String ACTIVITY_DATA_ELEMENT_ACTIVITY_ID = "ActivityID";
    public static final String ACTIVITY_DATA_ELEMENT_MESSAGE_ID = "MessageID";
    public static final String ACTIVITY_DATA_ELEMENT_SERVICE_NAME = "ServiceName";
    public static final String ACTIVITY_DATA_ELEMENT_OPERATION_NAME = "OperationName";
    public static final String ACTIVITY_DATA_ELEMENT_ACTIVITY_NAME = "ActivityName";
    public static final String ACTIVITY_DATA_ELEMENT_ACTIVITY_DESCRIPTION = "ActivityDescription";
    public static final String ACTIVITY_DATA_ELEMENT_REMOTE_IP_ADDRESS = "RemoteIPAddress";
    public static final String ACTIVITY_DATA_ELEMENT_USER_AGENT = "UserAgent";
    public static final String ACTIVITY_REQUEST_MESSAGE_STATUS = "RequestMessageStatus";
    public static final String ACTIVITY_RESPONSE_MESSAGE_STATUS = "ResponseMessageStatus";
    public static final String ACTIVITY_DATA_ELEMENT_TIMESTAMP = "TimeStamp";
    public static final String ACTIVITY_DATA_ELEMENT_MESSAGE_BODY = "MessageBody";
    public static final String ACTIVITY_DATA_ELEMENT_MESSAGE_DIRECTION = "MessageDirection";
    public static final String ACTIVITY_DATA_ELEMENT_XPATH_EXPRESSIONS = "XPathExpressions";
    public static final String ACTIVITY_DATA_ELEMENT_XPATH_EXPRESSION = "XPathExpression";
    public static final String ACTIVITY_XPATH_EXPRESSION = "Expression";
    public static final String XPATH_VALUE = "XPathValue";
    public static final String ACTIVITY_DATA_ELEMENT_PROPERTY = "ActivityProperty";
    public static final String ACTIVITY_DATA_ELEMENT_PROPERTY_CHILD = "ActivityPropertyChild";
    public static final String ACTIVITY_DATA_ELEMENT_PROPERTIES = "ActivityProperties";
    public static final String ACTIVITY_DATA_ELEMENT_PROPERTY_VALUE = "PropertyValue";
    public static final String ACTIVITY_DATA_ELEMENT_MESSAGE_STATUS = "MessageStatus";
    public static final String ACTIVITY_DATA_ELEMENT_XPATH_VALUE = "XPathValue";
    public static final String ACTIVITY_OUT_MESSAGE_ID = "OutMessageID";

    public static final QName BAM_EVENT_QNAME = new QName(BAM_ACTIVITY_ID_HEADER_NAMESPACE_URI, "BAMEvent");
    public static final QName ACTIVITY_ID_QNAME = new QName("activityID");

    public static final String BAM_REG_PATH = "/carbon/bam/data/publishers/activity";
    public static final String STATISTISTICS_REG_PATH = RegistryResources.ROOT +
                                                        "bam/data/publishers/activity";

    public static final int DIRECTION_IN_OUT = 0;
    public static final int DIRECTION_IN = 1;
    public static final int DIRECTION_OUT = 2;
}