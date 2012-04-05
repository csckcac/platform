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
package org.wso2.carbon.bam.analyzer.engine;

import javax.xml.namespace.QName;

public class AnalyzerConfigConstants {

    public static final String ANALYZER_FILE_NAME = "analyzer.xml";
    public static final String ANALYZER_SEQUENCE = "analyzerSequence";
    public static final String ANALYZER = "analyzer";
    public static final String JOB_GROUP = "BAM_JOB";

    public static final String DATA_CONTEXT = "dataContext";

    public static final String EVENT = "Event";
    public static final String META = "Meta";
    public static final String CORRELATION = "Correlation";

    public static final String ROW = "row";
    public static final String COLUMN = "column";
    public static final String GROUP = "group";

    public static final String AND_SEMANTIC = "and";
    public static final String OR_SEMANTIC = "or";

    //    public static final QName FREQUENCY_IN_SEC = new QName("frequencyInSecs");
    public static final QName returnType = new QName("return");
    public static final QName operations = new QName("operations");
    public static final QName value = new QName("value");
    public static final QName attributes = new QName("attributes");
    public static final QName column = new QName("column");
    public static final QName name = new QName("name");
    public static final QName namespace = new QName("namespace");
    public static final QName matchUsing = new QName("matchUsing");
    public static final QName measure = new QName("measure");
    public static final QName fromLastCursor = new QName("fromLastCursor");
    public static final QName batchSize = new QName("batchSize");
    public static final QName defaultCf = new QName("default");
    public static final QName dataSourceType = new QName("dataSourceType");
    public static final QName fetchDirty = new QName("fetchDirty");
    public static final QName fieldSet = new QName("fieldSet");
    public static final QName start = new QName("start");
    public static final QName end = new QName("end");
    public static final QName groupBy = new QName("groupBy");
    public static final QName index = new QName("index");
    public static final QName indexRow = new QName("indexRow");
    public static final QName granularity = new QName("granularity");
    public static final QName time = new QName("time");
    public static final QName type = new QName("type");
    public static final QName className = new QName("className");
    public static final QName aggregate = new QName("aggregate");
    public static final QName aggregationType = new QName("aggregationType");
    public static final QName fieldType = new QName("fieldType");
    public static final QName group = new QName("group");
    public static final QName groupSet = new QName("groupSet");
    public static final QName field = new QName("field");
    public static final QName from = new QName("from");
    public static final QName regex = new QName("regex");
    public static final QName replace = new QName("replace");
    public static final QName onExist = new QName("onExist");
    public static final QName orderBy = new QName("orderBy");
    public static final QName prefix = new QName("prefix");
    public static final QName lookup = new QName("lookup");
    public static final QName mbean = new QName("mbean");
    public static final QName nodeIdentifier = new QName("nodeIdentifier");
    public static final QName uri = new QName("uri");
    public static final QName url = new QName("url");
    public static final QName xpath = new QName("xpath");

    //Constants for detect fault analyzer
    public static final QName errorFields = new QName("errorFields");
    public static final QName currentSequenceIdentifier = new QName("currentSequenceIdentifier");

    public static final QName TRIGGER_ELEMENT = new QName("trigger");
    public static final QName ANALYZERS_ELEMENT = new QName("analyzers");
    public static final QName INDEX_ELEMENT = new QName("index");
    public static final QName CF_ELEMENT = new QName("ColumnFamily");
    public static final QName DEFAULT_CF_QNAME = new QName("defaultCF");
    public static final QName INDEX_ROW_KEY_QNAME = new QName("indexRowKey");
    public static final QName ROWKEY_QNAME = new QName("rowKey");
    public static final QName PART_QNAME = new QName("part");
    public static final QName STORE_INDEX = new QName("storeIndex");

    public static final QName CRON_ATTRIBUTE = new QName("cron");
    public static final QName COUNT_ATTRIBUTE = new QName("count");
    public static final QName INTERVAL_ATTRIBUTE = new QName("interval");

    public static final String JOB_DATA_MAP = "JobDataMap";

    public static final String GET_ANALYZER = "get";
    public static final String INDEX = "index";
    public static final String WHERE = "where";
    public static final String RANGE = "range";
    public static final String PROPERTY = "property";

    public static final String AGGREGATE_ANALYZER = "aggregate";
    public static final String MEASURE = "measure";
    
    public static final String JMX_ANALYZER = "jmx";
    public static final String ATTRIBUTE = "attribute";
    public static final String OPERATION = "operation";
    public static final String PARAMETER = "parameter";

    public static final String LOOKUP_ANALYZER = "lookup";
    public static final String PUT_ANALYZER = "put";
    public static final String LOG_ANALYZER = "log";

    public static final String DROP_ANALYZER = "drop";

    public static final String CORRELATE_ANALYZER = "correlate";

    public static final String INDEX_ANALYZER = "index";
    public static final String GROUPBY_ANALYZER = "groupBy";
    public static final String CLASS_ANALYZER = "class";
    public static final String ORDERBY_ANALYZER = "orderBy";
    public static final String EXTRACT_ANALYZER = "extract";
    public static final String DETECT_FAULTS_ANALYZER = "detectFault";

    public static final String RESULT = "result";
    public static final String IS_GROUPED = "isGrouped";
    public static final String TEMPORARY_INDEX = "temporaryIndex";
    public static final String analyzerParentRegistryPath = "/components/org.wso2.carbon.bam.analyzer/";
    public static final String tenantTracker = "tenantTracker";
    public static final String ANALYZER_TRACKER = "analyzerTracker";
    public static final String INDEX_TRACKER = "indexTracker";
    public static final String TENANTS_PROPERTY = "Tenants";
    public static final String USERNAME_PROPERTY = "Username";
    public static final String PASSWORD_PROPERTY = "Password";
    public static final String TENANT_TRACKER_PATH = analyzerParentRegistryPath + tenantTracker;
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";

    public static final String analyzers = "analyzers/";
    public static final String indexes = "indexes";
    public static final String connection = "connection";

    public static final int DEFAULT_INDEXING_INTERVAL = 100000;
    public static final String INDEXING_SEQUENCE = "indexing";

    public static final String DETECT_FAULTS_ERROR_FIELDS_SPLITTER = ",";


    public static final String ALERT_TRIGGER = "alert";
    public static final QName ALERT_FIELDS = new QName("fields");
    public static final QName TO_EMAIL = new QName("to");
    public static final QName SUBJECT = new QName("subject");
    public static final QName FROM_EMAIL = new QName("from");
    public static final QName MAILHOST = new QName("mailhost");
    public static final QName MAIL_USERNAME = new QName("username");
    public static final QName MAIL_PASSWORD = new QName("password");


    public static final QName MAIL_TRANSPORT = new QName("transport");
}
