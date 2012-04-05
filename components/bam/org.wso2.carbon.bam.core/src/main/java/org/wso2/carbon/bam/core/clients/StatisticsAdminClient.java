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

package org.wso2.carbon.bam.core.clients;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.core.util.BAMUtil;
import org.wso2.carbon.statistics.stub.StatisticsAdminStub;
import org.wso2.carbon.statistics.stub.types.carbon.OperationStatistics;
import org.wso2.carbon.statistics.stub.types.carbon.ServiceStatistics;
import org.wso2.carbon.statistics.stub.types.carbon.SystemStatistics;

import java.rmi.RemoteException;

import static org.wso2.carbon.bam.core.BAMConstants.SERVICES_SUFFIX;
import static org.wso2.carbon.bam.core.BAMConstants.STATISTICS_ADMIN_SERVICE;

public class StatisticsAdminClient extends AbstractAdminClient<StatisticsAdminStub> {
    private static Log log = LogFactory.getLog(StatisticsAdminClient.class);

    public StatisticsAdminClient(String serverURL) throws AxisFault {

        String serviceURL = generateURL(new String[]{serverURL, SERVICES_SUFFIX, STATISTICS_ADMIN_SERVICE});
        stub = new StatisticsAdminStub(BAMUtil.getConfigurationContextService().getClientConfigContext(), serviceURL);
        stub._getServiceClient().getOptions().setManageSession(true);
    }

    public StatisticsAdminClient(String serverURL, String sessionCookie) throws AxisFault {
         this(serverURL);
        setSessionCookie(sessionCookie);
    }

    public ServiceStatistics getServiceStatistics(String serviceName) throws RemoteException {
        return stub.getServiceStatistics(serviceName);
    }

    public SystemStatistics getSystemStatistics() throws RemoteException {
        return stub.getSystemStatistics();
    }

    public OperationStatistics getOperationStatistics(String serviceName, String operationName) throws RemoteException {
        return stub.getOperationStatistics(serviceName, operationName);
    }

    public int getServiceRequestCount(String serviceName) throws RemoteException {
        return stub.getServiceRequestCount(serviceName);
    }

    public int getServiceFaultCount(String serviceName) throws RemoteException {
        return stub.getServiceFaultCount(serviceName);
    }

    public int getServiceResponseCount(String serviceName) throws RemoteException {
        return stub.getServiceResponseCount(serviceName);
    }

    public void cleanup() {
        try {
            stub._getServiceClient().cleanupTransport();
            stub._getServiceClient().cleanup();
            stub.cleanup();
        } catch (AxisFault axisFault) {
            if (log.isErrorEnabled()) {
                log.error("Stub cleanup failed: " + this.getClass().getName(), axisFault);
            }
        }
    }

}
