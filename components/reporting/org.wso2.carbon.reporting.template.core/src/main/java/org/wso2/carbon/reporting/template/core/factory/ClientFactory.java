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

package org.wso2.carbon.reporting.template.core.factory;

import org.wso2.carbon.reporting.api.ReportingException;
import org.wso2.carbon.reporting.template.core.client.BAMDBClient;
import org.wso2.carbon.reporting.template.core.client.DatasourceClient;
import org.wso2.carbon.reporting.template.core.client.ReportingClient;


public class ClientFactory {
    private static ReportingClient reportingClient;
    private static BAMDBClient BAMDBClient;
    private static DatasourceClient DSClient;

    public static ReportingClient getReportingClient() {
        if (reportingClient == null) {
            reportingClient = new ReportingClient();
        }
        return reportingClient;
    }

    public static BAMDBClient getBAMDBClient() throws ReportingException {
        if (BAMDBClient == null) {
            BAMDBClient = new BAMDBClient();
        }
        return BAMDBClient;
    }


    public static DatasourceClient getDSClient() {
        if (DSClient == null) {
            DSClient = new DatasourceClient();
        }
        return DSClient;
    }


}
