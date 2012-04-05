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
import org.wso2.carbon.service.mgt.stub.ServiceAdminStub;
import org.wso2.carbon.service.mgt.stub.types.carbon.ServiceMetaData;
import org.wso2.carbon.service.mgt.stub.types.carbon.ServiceMetaDataWrapper;

import java.rmi.RemoteException;

import static org.wso2.carbon.bam.core.BAMConstants.SERVICES_SUFFIX;
import static org.wso2.carbon.bam.core.BAMConstants.SERVICE_ADMIN_SERVICE;

public class ServiceAdminClient extends AbstractAdminClient<ServiceAdminStub> {
	private static Log log = LogFactory.getLog(ServiceAdminClient.class);

	public ServiceAdminClient(String serverURL) throws AxisFault {

		String serviceURL = generateURL(new String[] { serverURL, SERVICES_SUFFIX, SERVICE_ADMIN_SERVICE });
		stub = new ServiceAdminStub(BAMUtil.getConfigurationContextService().getClientConfigContext(), serviceURL);
		stub._getServiceClient().getOptions().setManageSession(true);
	}

	public ServiceAdminClient(String serverURL, String sessionCookie) throws AxisFault {
		this(serverURL);
		setSessionCookie(sessionCookie);
	}

	public ServiceMetaData[] getAllServiceGroups() throws RemoteException {
		ServiceMetaDataWrapper sgInfo;
        sgInfo=stub.listServices(null,null,0);

		return sgInfo.getServices();
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
