/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.bam.cep;

import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;

/**
 * used to define constant related to bam-cep
 */
public class BAMCepConstants {

    protected static final  String BAM_CONF_FILE = CarbonUtils.getCarbonHome() + File.separator + "repository" +
            File.separator + "conf" + File.separator + "bam.xml";
    protected static final String SERVER_URI = "http://wso2.org/ns/2009/09/bam/service/statistics/data";
    protected static final String MEDIATION_URI = "http://wso2.org/ns/2009/09/bam/server/user-defined/data";
    protected static final String ACTIVITY_URI ="http://wso2.org/ns/2009/09/bam/service/activity/data";
    protected static final String SERVICE_DATA_RECEIVER = "BAMServiceStatisticsSubscriberService";
    protected static final String MEDIATION_DATA_RECEIVER = "BAMServerUserDefinedDataSubscriberService";
    protected static final String ACTIVITY_DATA_RECEIVER = "BAMActivityDataStatisticsSubscriberService";
}
