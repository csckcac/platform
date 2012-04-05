/**
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.bam.receiver;

import javax.xml.namespace.QName;

public class ReceiverConstants {
    public static final int EVENT_QUEUE_CAPACITY = 300;

    public static final int NO_OF_WORKER_THREADS = 100;

    public static final int DEFAULT_RECEIVER_PORT = 7620;

    public static final String CARBON_CONFIG_PORT_OFFSET_NODE = "Ports.Offset";
    public static final int CARBON_DEFAULT_PORT_OFFSET = 0;

    public static final String BAM_CONFIGURATION_FILE = "bam.xml";
    public static final String RECEIVER_PORT_ELEMENT = "receiverServicePort";

    public static final String TIMESTAMP_KEY_NAME = "timeStamp";
    public static final String SERVER_ADDR_KEY_NAME = "serverAddress";
    public static final String EVENT_BODY_KEY_NAME = "eventBody";

    public static final String EVENT_CORRELATION_DATA_LOCALPART = "correlationdata";
    public static final String EVENT_METADATA_LOCALPART = "metadata";
    public static final String EVENT_EVENTDATA_LOCALPART = "eventdata";
    public static final String PARENT_EVENT_LOCALPART = "eventData";

    public static final String EVENT_NAMESPACE_URI = "http://receivers.bam.carbon.wso2.org";

    public static final String BAM_KEYSPACE = "BAMKeyspace";
    public static final String EVENT_COLUMNFAMILY_NAME = "EVENT";

    public static final String MSG_RECEIVED_TIME_KEY = "MESSAGE_RECEIVED_TIME";

    public static final QName NOSQL_KEY_XPATH_QNAME = new QName("noSQLKeyXPath");
    public static final QName CF_INDEXES_QNAME = new QName("CFIndexes");
    public static final QName CF_QNAME = new QName("ColumnFamily");
    public static final QName GRANULARITY_QNAME = new QName("granularity");
    public static final QName ROWKEY_QNAME = new QName("rowKey");
    public static final QName DEFAULT_CF_QNAME = new QName("defaultCF");
    
    public static final QName INDEX_ROW_KEY_QNAME = new QName("indexRowKey");
    public static final QName BAM_EVENT_QNAME = new QName("http://wso2.org/bam/2011/07/31", "data", "bam");
    public static final int EVENT_CAPACITY = 10000;

    public static final String CORRELATION_TABLE = "CORRELATION";
    public static final String META_TABLE = "META";
    public static final String EVENT_TABLE ="EVENT";

    public static final String DEFAULT_INDEX_ROW_KEY = "allkeys";
    
}
