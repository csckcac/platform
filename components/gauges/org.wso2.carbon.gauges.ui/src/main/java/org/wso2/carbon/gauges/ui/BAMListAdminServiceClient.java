/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.gauges.ui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.client.Options;
import org.wso2.carbon.gauges.ui.types.carbon.ActivityDTO;
import org.wso2.carbon.gauges.ui.types.carbon.MessageDTO;
import org.wso2.carbon.gauges.ui.types.carbon.MonitoredServerDTO;
import org.wso2.carbon.gauges.ui.types.carbon.ServiceDTO;
import java.util.Locale;
import java.rmi.RemoteException;

public class BAMListAdminServiceClient {
	private static final Log log = LogFactory.getLog(BAMStatQueryDSClient.class);

	BAMListAdminServiceStub stub;

	public BAMListAdminServiceClient(String cookie, String backendServerURL, ConfigurationContext configCtx,
			Locale locale) throws AxisFault {
		String serviceURL = backendServerURL + "BAMListAdminService";

		stub = new BAMListAdminServiceStub(configCtx, serviceURL);
		ServiceClient client = stub._getServiceClient();
		Options option = client.getOptions();
		option.setManageSession(true);
		option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);

	}

	public MonitoredServerDTO[] getServerList() throws RemoteException, BAMException {
		return stub.getServerList();
	}

	public ServiceDTO[] getServicesList(int serverID) throws RemoteException, BAMException {
		return stub.getServiceList(serverID);
	}

	// activity
	public ActivityDTO[] getActivityList() throws RemoteException, BAMException {
		return stub.getActivityList();
	}

	public MessageDTO[] getMessageList() throws RemoteException, BAMException {
		return stub.getMessageList();
	}

	public MessageDTO[] getMessageList(int activityID) throws RemoteException, BAMException {
		return stub.getMessageListForActivity(activityID);
	}
}
