/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.proxyadmin.tooling.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.proxyadmin.common.MetaData;
import org.wso2.carbon.proxyadmin.common.ProxyAdminException;
import org.wso2.carbon.proxyadmin.common.ProxyData;
import org.wso2.carbon.proxyadmin.common.service.IProxyServiceAdmin;

/**
 * The class <code>ProxyServiceAdmin</code> provides the administration service to configure
 * proxy services.
 */
public class ProxyServiceAdmin implements IProxyServiceAdmin{

    private static String SUCCESSFUL = "successful";
    private static String FAILED = "failed";
    private static Log log = LogFactory.getLog(ProxyServiceAdmin.class);
	public String addProxy(ProxyData pd) throws ProxyAdminException {
		// TODO Auto-generated method stub
		return null;
	}
	public String deleteProxyService(String proxyName)
			throws ProxyAdminException {
		// TODO Auto-generated method stub
		return null;
	}
	public String disableStatistics(String proxyName)
			throws ProxyAdminException {
		// TODO Auto-generated method stub
		return null;
	}
	public String disableTracing(String proxyName) throws ProxyAdminException {
		// TODO Auto-generated method stub
		return null;
	}
	public String enableStatistics(String proxyName) throws ProxyAdminException {
		// TODO Auto-generated method stub
		return null;
	}
	public String enableTracing(String proxyName) throws ProxyAdminException {
		// TODO Auto-generated method stub
		return null;
	}
	public String[] getAvailableEndpoints() throws ProxyAdminException {
		// TODO Auto-generated method stub
		return null;
	}
	public String[] getAvailableSequences() throws ProxyAdminException {
		// TODO Auto-generated method stub
		return null;
	}
	public String[] getAvailableTransports() throws ProxyAdminException {
		// TODO Auto-generated method stub
		return null;
	}
	public String getEndpoint(String name) throws ProxyAdminException {
		// TODO Auto-generated method stub
		return null;
	}
	public MetaData getMetaData() throws ProxyAdminException {
		// TODO Auto-generated method stub
		return null;
	}
	public ProxyData getProxy(String proxyName) throws ProxyAdminException {
		// TODO Auto-generated method stub
		return null;
	}
	public String getSourceView(ProxyData pd) throws ProxyAdminException {
		// TODO Auto-generated method stub
		return null;
	}
	public String modifyProxy(ProxyData pd) throws ProxyAdminException {
		// TODO Auto-generated method stub
		return null;
	}
	public String redeployProxyService(String proxyName)
			throws ProxyAdminException {
		// TODO Auto-generated method stub
		return null;
	}
	public String startProxyService(String proxyName)
			throws ProxyAdminException {
		// TODO Auto-generated method stub
		return null;
	}
	public String stopProxyService(String proxyName) throws ProxyAdminException {
		// TODO Auto-generated method stub
		return null;
	}



}
