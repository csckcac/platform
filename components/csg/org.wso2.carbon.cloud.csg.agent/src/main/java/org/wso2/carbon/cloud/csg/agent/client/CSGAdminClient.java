/*
 * Copyright WSO2, Inc. (http://wso2.com)
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
package org.wso2.carbon.cloud.csg.agent.client;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cloud.csg.common.CSGException;
import org.wso2.carbon.cloud.csg.stub.CSGAdminServiceStub;
import org.wso2.carbon.cloud.csg.stub.types.common.CSGProxyToolsURLs;
import org.wso2.carbon.cloud.csg.stub.types.common.CSGServiceMetaDataBean;
import org.wso2.carbon.cloud.csg.stub.types.common.CSGThriftServerBean;

import java.rmi.RemoteException;

/**
 * <code>CSGAdminClient </code> provides the admin client for CSGAdmin service
 */
public class CSGAdminClient {
    private CSGAdminServiceStub stub;

    private static final Log log = LogFactory.getLog(CSGAdminClient.class);

    public CSGAdminClient(String cookie, String backendServerUrl) throws CSGException {
        String serviceURL = backendServerUrl + "CSGAdminService";
        try {
            stub = new CSGAdminServiceStub(serviceURL);
        } catch (AxisFault axisFault) {
            throw new CSGException(axisFault);
        }
        Options options = stub._getServiceClient().getOptions();
        options.setTimeOutInMilliSeconds(15 * 60 * 1000);
        options.setProperty(HTTPConstants.SO_TIMEOUT, 15 * 60 * 1000);
        options.setProperty(HTTPConstants.CONNECTION_TIMEOUT, 15 * 60 * 1000);
        options.setManageSession(true);
        options.setProperty(HTTPConstants.COOKIE_STRING, cookie);
    }

    public CSGAdminClient(String cookie,
                          String backendServerURL,
                          ConfigurationContext configCtx) throws CSGException {
        String serviceURL = backendServerURL + "CSGAdminService";
        try {
            stub = new CSGAdminServiceStub(configCtx, serviceURL);
        } catch (AxisFault axisFault) {
            throw new CSGException(axisFault);
        }
        Options options = stub._getServiceClient().getOptions();
        options.setTimeOutInMilliSeconds(15 * 60 * 1000);
        options.setProperty(HTTPConstants.SO_TIMEOUT, 15 * 60 * 1000);
        options.setProperty(HTTPConstants.CONNECTION_TIMEOUT, 15 * 60 * 1000);
        options.setManageSession(true);
        options.setProperty(HTTPConstants.COOKIE_STRING, cookie);
    }

    public void deployProxy(CSGServiceMetaDataBean serviceMetaData) throws CSGException {
        try {
            stub.deployProxy(serviceMetaData);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void unDeployProxy(String serviceName) throws CSGException {
        try {
            stub.unDeployProxy(serviceName);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public CSGThriftServerBean getThriftServerConnectionBean() throws CSGException {
        try {
            return stub.getThriftServerConnectionBean();
        } catch (Exception e) {
            throw new CSGException(e);
        }
    }

    public void updateProxy(String serviceName, int eventType) throws CSGException {
        try {
            stub.updateProxy(serviceName, eventType);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public CSGProxyToolsURLs getPublishedProxyToolsURLs(String serviceName, String domainName)
            throws CSGException {
        try {
            return stub.getPublishedProxyToolsURLs(serviceName, domainName);
        } catch (Exception e) {
            handleException(e);
        }
        return null;
    }

    private void handleException(Throwable t) throws CSGException {
        log.error(t);
        throw new CSGException(t);
    }
}
