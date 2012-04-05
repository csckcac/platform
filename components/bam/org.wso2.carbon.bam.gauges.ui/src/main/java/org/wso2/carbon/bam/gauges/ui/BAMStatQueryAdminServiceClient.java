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
package org.wso2.carbon.bam.gauges.ui;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.rmi.RemoteException;
import java.util.Locale;

public class BAMStatQueryAdminServiceClient {
/*	private static final Log log = LogFactory.getLog(BAMStatQueryAdminServiceClient.class);

	BAMStatQueryAdminServiceStub stub;

	public BAMStatQueryAdminServiceClient(String cookie, String backendServerURL,
			ConfigurationContext configCtx, Locale locale) throws AxisFault {
		String serviceURL = backendServerURL + "BAMStatQueryAdminService";

		stub = new BAMStatQueryAdminServiceStub(configCtx, serviceURL);
		ServiceClient client = stub._getServiceClient();
		Options option = client.getOptions();
		option.setManageSession(true);
		option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
	}

	public MediationFaultStatList getMediationRealTimeFaultStat_temp(String categoryType, int serverId,
			String mediationName, String cacheId) throws RemoteException, BAMExceptionException {
		return stub.getMediationRealTimeFaultStat_temp(categoryType, serverId, mediationName, cacheId);
	}*/
}