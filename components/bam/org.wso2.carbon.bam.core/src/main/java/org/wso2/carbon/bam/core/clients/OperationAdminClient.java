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
import org.wso2.carbon.operation.mgt.stub.OperationAdminStub;
import org.wso2.carbon.operation.mgt.stub.types.OperationMetaData;
import org.wso2.carbon.operation.mgt.stub.types.OperationMetaDataWrapper;

import java.rmi.RemoteException;

import static org.wso2.carbon.bam.core.BAMConstants.OPERATION_ADMIN_SERVICE;
import static org.wso2.carbon.bam.core.BAMConstants.SERVICES_SUFFIX;

public class OperationAdminClient extends AbstractAdminClient<OperationAdminStub> {
    private static Log log = LogFactory.getLog(OperationAdminClient.class);

    public OperationAdminClient(String serverURL) throws AxisFault {
        String serviceURL = generateURL(new String[]{serverURL, SERVICES_SUFFIX, OPERATION_ADMIN_SERVICE});
        stub = new OperationAdminStub(BAMUtil.getConfigurationContextService().getClientConfigContext(),
                                      serviceURL);
        stub._getServiceClient().getOptions().setManageSession(true);
    }

    public OperationAdminClient(String serverURL, String sessionCookie) throws AxisFault {
        this(serverURL);
        setSessionCookie(sessionCookie);
    }

    public OperationMetaData[] getAllOperations(String service) throws RemoteException {
        OperationMetaDataWrapper wrapper;
        wrapper = stub.listAllOperations(service);
        return wrapper.getPublishedOperations();
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

