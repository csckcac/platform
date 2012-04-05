/*
 * Copyright (c) 20012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.bpel.bam.publisher;


public class BamPublisherConstants {
    public static final String BAM_PUBLISHER_NS = "http://wso2.org/bps/monitoring/publisher";

    public static final String PROCESS_NAME = "ProcessName";
    public static final String SERVER = "Server";
    public static final String TENANT_ID = "TenantId";
    public static final String DEFAULT_SERVER_NAME = "WSO2BPS";

    public static final String BAM_KEY = "key";
    public static final String BAM_FROM = "from";
    public static final String NAME_ATTR = "name";
    public static final String VARIABLE_ATTR = "variable";
    public static final String PART_ATTR = "part";

    public static final String PROCESS_VARIABLE_NAME = "VariableName";
    public static final String PROCESS_VARIABLE_VALUE = "VariableValue";

    public static final String CONFIG_RESOURCE_PATH =
            "repository/components/org.wso2.carbon.bpel.bam.publisher/bam_publisher_info";

    public static final String BAM_SERVER_URL = "BAM_SERVER_URL";
    public static final String BAM_SERVER_PORT = "BAM_SERVER_PORT";
    public static final String BAM_SERVER_USERNAME = "BAM_SERVER_USERNAME";
    public static final String BAM_SERVER_PASSWORD = "BAM_SERVER_PASSWORD";
    public static final String BAM_SERVER_ENABLE_SOCKET_TRANSPORT = "ENABLE_SOCKET_TRANSPORT";
    public static final String BAM_SERVER_ENABLE_HTTP_TRANSPORT = "ENABLE_HTTP_TRANSPORT";
}
