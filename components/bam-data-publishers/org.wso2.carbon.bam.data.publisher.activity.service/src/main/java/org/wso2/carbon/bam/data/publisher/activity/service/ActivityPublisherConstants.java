/*
 * Copyright 2005-2010 WSO2, Inc. (http://wso2.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.wso2.carbon.bam.data.publisher.activity.service;

import org.wso2.carbon.core.RegistryResources;

/*
 * Predefined Activity specific Constants.
 */
public final class ActivityPublisherConstants {
    public static final String BAM_ACTIVITY_PUBLISHER_MODULE_NAME = "wso2bamactivitypublisher";

    // Registry persistence related constants
    public static final String STATISTISTICS_REG_PATH =
            RegistryResources.ROOT +
            "bam/data/publishers/activity";
    public static final String BAM_REG_PATH = "/carbon/bam/data/publishers/activity";
    public static final String ENABLE_EVENTING = "EnableEventing";
    public static final String EVENTING_ON = "ON";
    public static final String EVENTING_OFF = "OFF";
    public static final String ENABLE_EVENTING_DEFAULT = EVENTING_ON;
    public final static String BAM_ACTIVITY_ID_HEADER_NAMESPACE_URI = "http://wso2.org/ns/2010/10/bam";
    public final static String BAM_MESSAGE_COUNT = "BAMMessageCount";
    public static final String MESSAGE_THRESHOLD = "MessageThreshold";
    public final static int MESSAGE_THRESHOLD_DEFAULT = 2;
    public static final String MESSAGE_LOOKUP_ON = "ON";
    public static final String MESSAGE_LOOKUP_OFF = "OFF";
    public static final String MESSAGE_LOOKUP_DEFAULT = MESSAGE_LOOKUP_OFF;
    public static final String ENABLE_MESSAGE_LOOKUP = "EnableMessageLookup";
    public final static String XPATH_EXPRESSION = "";
    public final static String WSO2_ACTIVITY_MESSAGE_ID = "wso2ActivityMessageID";
    public static final String MESSAGE_DUMPING_ON = "ON";
    public static final String MESSAGE_DUMPING_OFF = "OFF";
    public static final String MESSAGE_DUMPING_DEFAULT = MESSAGE_DUMPING_OFF;
    public static final String ENABLE_MESSAGE_DUMPING = "EnableMessageDumping";
    public final static String BAM_MESSAGE_DATA_COUNT = "BAMMessageDataCount";
    public final static String BAM_XPATH_COUNT = "BAMXPathCount";
    // Event specific constants
    public static final String ACTIVITY_DATA_NS_URI = "http://wso2.org/ns/2009/09/bam/service/activity/data";
    public static final String ACTIVITY_DATA_NS_PREFIX = "activitydata";
    public static final String ACTIVITY_DATA_ELEMENT_EVENT = "Event";
    public static final String ACTIVITY_DATA_ELEMENT_ACTIVITY_DATA = "ActivityData";
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
    public static final String ACTIVITY_DATA_ELEMENT_MESSAGE_DIRECTION_IN = "Request";
    public static final String ACTIVITY_DATA_ELEMENT_MESSAGE_DIRECTION_OUT = "Response";
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
    public static final String BAM_SERVICE_ACTIVITY_STATISTICS_SUBSCRIBER_SERVICE = "BAMActivityDataStatisticsSubscriberService";
    public static String BAM_SERVER_URL = "BamServerURL";
    public static final String ACTIVITY_ELEMENT_NAME_SUBSCRIPTIONID = "SubscriptionId";

    public static final String XPATH_PROPERTY = "XPath";
    public static final String NAMESAPCE_PROPERTY_PREFIX = "ns";
    public static final String XPATH_ROOT_PATH = "xpaths";

    public static final String ACTIVITY_DATA_ELEMENT_PROPERTY_FILTER = "PropertyFilter";
    public static final String ACTIVITY_XPATH_EXPRESSION_KEY = "ExpressionKey";
    public static final String ACTIVITY_XPATH_ALIAS = "Alias";
    public static final String XPATH_NAMESPACES = "Namespaces";
    public static final String XPATH_NAMESPACE = "Namespace";

}