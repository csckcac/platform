/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.messagebox;

import org.wso2.carbon.registry.core.RegistryConstants;

import javax.xml.namespace.QName;

public class MessageBoxConstants {
    // Adding permission to queue operations are defined as below in SQS,
    // No other queue operations are allowed to use for shared users.
    public static final String SQS_OPERATION_ALL = "*";
    public static final String SQS_OPERATION_SEND_MESSAGE = "SendMessage";
    public static final String SQS_OPERATION_RECEIVE_MESSAGE = "ReceiveMessage";
    public static final String SQS_OPERATION_DELETE_MESSAGE = "DeleteMessage";
    public static final String SQS_OPERATION_CHANGE_MESSAGE_VISIBILITY = "ChangeMessageVisibility";
    public static final String SQS_OPERATION_GET_QUEUE_ATTRIBUTES = "GetQueueAttributes";

    //SQS message has following message meta data defined. At receiving required fields are mentioned.
    public static final String SQS_MESSAGE_ATTRIBUTE_ALL = "All";
    public static final String SQS_MESSAGE_ATTRIBUTE_SENDER_ID = "SenderId";
    public static final String SQS_MESSAGE_ATTRIBUTE_SENT_TIMESTAMP = "SentTimestamp";
    public static final String SQS_MESSAGE_ATTRIBUTE_RECEIVE_COUNT = "ApproximateReceiveCount";
    public static final String SQS_MESSAGE_ATTRIBUTE_FIRST_RECEIVE_TIMESTAMP = "ApproximateFirstReceiveTimestamp";

    //SQS queue has following queue meta data defined.
    public static final String SQS_QUEUE_ATTRIBUTE_ALL = "All";
    public static final String SQS_QUEUE_ATTRIBUTE_POLICY = "Policy";
    public static final String SQS_QUEUE_ATTRIBUTE_NUMBER_OF_MESSAGES = "ApproximateNumberOfMessages";
    public static final String SQS_QUEUE_ATTRIBUTE_NUMBER_OF_MESSAGES_NOT_VISIBLE = "ApproximateNumberOfMessagesNotVisible";
    public static final String SQS_QUEUE_ATTRIBUTE_VISIBILITY_TIMEOUT = "VisibilityTimeout";
    public static final String SQS_QUEUE_ATTRIBUTE_CREATED_TIMESTAMP = "CreatedTimestamp";
    public static final String SQS_QUEUE_ATTRIBUTE_LAST_MODIFIED_TIMESTAMP = "LastModifiedTimestamp";
    public static final String SQS_QUEUE_ATTRIBUTE_MAX_MESSAGE_SIZE = "MaximumMessageSize";
    public static final String SQS_QUEUE_ATTRIBUTE_MESSAGE_RETENTION_PERIOD = "MessageRetentionPeriod";

    public static final String JMS_MESSAGE_PROPERTY_QUEUE_NAME = "QueueName";
    public static final String JMS_MESSAGE_PROPERTY_VISIBILITY_TIME_OUT = "VisibilityTimeOut";
    public static final String JMS_MESSAGE_PROPERTY_QUEUE_OWNER = "QueueOwner";
    public static final String JMS_MESSAGE_PROPERTY_MD5_OF_MESSAGE = "MD5OfMessage";
    public static final String JMS_MESSAGE_PROPERTY_RECEIPT_HANDLER = "receiptHandler";
    public static final String JMS_MESSAGE_PROPERTY_RECEIVED_TIME_STAMP = "receivedTimeStamp";
    public static final String JMS_MESSAGE_PROPERTY_CREATED_TIME_STAMP = "createdTimeStamp";
    public static final String JMS_MESSAGE_PROPERTY_PERMISSION_LABEL = "permissionLabel";
    public static final String JMS_MESSAGE_PROPERTY_OPERATION_LIST = "operationsList";
    public static final String JMS_MESSAGE_SHARED_USER_OPERATION_SEPARATOR = "|";
    public static final String JMS_MESSAGE_PROPERTY_MESSAGE_ID = "messageId";
    public static final String JMS_MESSAGE_PROPERTY_SHARED_USERS = "sharedUsers";
    public static final String JMS_MESSAGE_PROPERTY_RECEIVED_COUNT = "messageReceivedCount";
    public static final String JMS_MESSAGE_PROPERTY_SENT_TIMESTAMP = "messageSentTimestamp";


    public static final String JMS_DUPLICATE_QUEUE_PREFIX = "temp.";
    public static final String COMPOSITE_QUEUE_NAME_SYMBOL = "/";
    public static final int MESSAGE_RECEIVE_WAIT_TIME = 50;

    public static final String MB_QUEUE_STORAGE_PATH = "event/queues/jms";
    public static final String MB_MESSAGE_BOX_STORAGE_PATH = "message/messageBoxes";

    public static final String MB_PERMISSION_CONSUME = "consume";
    public static final String MB_PERMISSION_PUBLISH = "publish";
    public static final String MB_PERMISSION_BROWSE = "browse";
    public static final String MB_PERMISSION_CHANGE_PERMISSION = "changePermission";


    public static final String MB_PROPERYY_NAME = "name";
    public static final String MB_PROPERYY_VISIBILITY_TIMEOUT = SQS_QUEUE_ATTRIBUTE_VISIBILITY_TIMEOUT;
    public static final String MB_PROPERYY_OWNER = "owner";

    public static final String MB_REGISTRY_PROPERTY_SHARED_USERS = "sharedUsers";
    public static final String MB_REGISTRY_PROPERTY_OPERATIONS = "operations";
    public static final int TWELVE_HOURS_IN_SECONDS = 43200;
    public static final long DEFAULT_VISIBILITY_TIMEOUT = 30;

    public static final String MB_QUEUE_PROPERTY_CREATED_TIME = "createdTime";
    public static final String MB_QUEUE_PROPERTY_UPDATED_TIME = "updatedTime";
    public static final String MB_QUEUE_PROPERTY_CREATED_FROM = "createdFrom";

    public static final String MB_QUEUE_CREATED_FROM_SQS_CLIENT = "sqsClient";
    public static final String MB_QUEUE_CREATED_FROM_AMQP = "amqp";


    public static final String HMAC_SHA256_ALGORITHM = "HmacSHA256";
    public static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";
    public static final String AWSACCESS_KEY_ID = "AWSAccessKeyId";
    public static final QName ACCESS_KEY_ID_QNAME = new QName(AWSACCESS_KEY_ID);
    public static final String TIMESTAMP = "Timestamp";
    public static final QName TIMESTAMP_QNAME = new QName(TIMESTAMP);
    public static final String SIGNATURE = "Signature";
    public static final QName SIGNATURE_QNAME = new QName(SIGNATURE);
    public static final String SECRET_ID_REGISTRY_PROPERTY = "SecretId";
    public static final String ACTION = "Action";
    public static final String SQS_AUTHENTICATION_MODULE_NAME = "sqsAuthentication";
    public static final String SIGNATURE_METHOD = "SignatureMethod";
    public static final String DEFAULT_ENCODING = "UTF-8";
    public static final String SIGNATURE_VERSION = "SignatureVersion";
    public static final String REQUEST_PARAMETER_MAP = "requestParameterMap";
    public static final String REGISTRY_ACCESS_KEY_INDEX_PATH = RegistryConstants.PROFILES_PATH + "access.key.index";
    public static final String SECRET_ACCESS_KEY_ID = "secretAccessKeyId";
    public static final String ACCESS_KEY_ID = "accessKeyId";

    public static final String MB_QUEUE_ATTR_QUEUE_DEPTH = "QueueDepth";
    public static final String MB_QUEUE_ATTR_MESSAGE_COUNT = "MessageCount";
    public static final String URL_ENCODING = "UTF-8";

    public static final String MESSAGE_BOX_CONF = "messagebox-config.xml";
    public static final String MB_CONF_ELE_ROOT = "messageBoxConfig";
    public static final String MB_CONF_NAMESPACE = "http://wso2.org/carbon/messagebox/";
    public static final String MB_CONF_ELE_SERVICE = "messageBoxService";
    public static final String MB_CONF_ATTR_CLASS = "class";
    public static final String QPID_VHOST_NAME = "carbon";

    public static final String SQS_AUTHENTICATED = "sqsAuthenticated";
}
