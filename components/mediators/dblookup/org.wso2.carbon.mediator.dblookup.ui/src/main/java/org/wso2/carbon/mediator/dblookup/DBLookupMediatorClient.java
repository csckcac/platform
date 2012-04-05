/**
 * Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.mediator.dblookup;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpSession;
import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.datasource.ui.stub.DataSourceAdminStub;
import org.wso2.carbon.datasource.ui.stub.DataSourceManagementException;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.utils.ServerConstants;

/**
 * This class is used to get defined carbon datasources. It uses
 * 'DataSourceAdminStub' to do backend call.
 * 
 */
public class DBLookupMediatorClient {
	private DataSourceAdminStub stub;
	private static final String DATASOURCE_EXTENSION_NS =
	                                                      "http://www.wso2.org/products/wso2commons/datasource";
	private static final QName ROOT_QNAME = new QName(DATASOURCE_EXTENSION_NS,
	                                                  "datasourceExtension", "datasource");

	public DBLookupMediatorClient(String cookie, String backendServerURL,
	                              ConfigurationContext configCtx) throws AxisFault {
		String serviceURL = backendServerURL + "DataSourceAdmin";
		stub = new DataSourceAdminStub(configCtx, serviceURL);
		ServiceClient client = stub._getServiceClient();
		Options option = client.getOptions();
		option.setManageSession(true);
		option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);

	}

	public static DBLookupMediatorClient getInstance(ServletConfig config, HttpSession session)
	                                                                                           throws AxisFault {

		String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
		ConfigurationContext configContext =
		                                     (ConfigurationContext) config.getServletContext()
		                                                                  .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

		String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
		return new DBLookupMediatorClient(cookie, backendServerURL, configContext);
	}

	public List<String> getAllDataSourceInformations() throws RemoteException,
	                                                  DataSourceManagementException {
		OMElement element = stub.getAllDataSourceInformation();

		List<String> sourceList = new ArrayList<String>();
		if (element == null) {
			return sourceList;
		}

		OMElement datasourceRoot = element.getFirstChildWithName(ROOT_QNAME);
		if (datasourceRoot == null) {
			return sourceList;
		}
		Iterator iterator = datasourceRoot.getChildElements();
		while (iterator.hasNext()) {
			OMElement datasourceElement = (OMElement) iterator.next();
			if (datasourceElement != null) {
				String name = datasourceElement.getAttributeValue(new QName("", "name", ""));
				if (name != null && !"".equals(name)) {
					sourceList.add(name);
				}
			}
		}
		return sourceList;
	}

}
