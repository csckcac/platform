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
package org.wso2.carbon.governance.generic.ui.clients;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.governance.generic.stub.ManageGenericArtifactServiceStub;
import org.wso2.carbon.governance.generic.stub.beans.xsd.ArtifactsBean;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.utils.ServerConstants;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpSession;

public class ManageGenericArtifactServiceClient {

    private static final Log log = LogFactory.getLog(ManageGenericArtifactServiceClient.class);

    private ManageGenericArtifactServiceStub stub;
    private String epr;

    @SuppressWarnings("unused")
    public ManageGenericArtifactServiceClient(
            String cookie, String backendServerURL, ConfigurationContext configContext)
            throws RegistryException {

        epr = backendServerURL + "ManageGenericArtifactService";

        try {
            stub = new ManageGenericArtifactServiceStub(configContext, epr);

            ServiceClient client = stub._getServiceClient();
            Options option = client.getOptions();
            option.setManageSession(true);
            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);

        } catch (AxisFault axisFault) {
            String msg = "Failed to initiate ManageGenericArtifactServiceClient. " +
                    axisFault.getMessage();
            log.error(msg, axisFault);
            throw new RegistryException(msg, axisFault);
        }
    }

    public ManageGenericArtifactServiceClient(ServletConfig config, HttpSession session)
            throws RegistryException {

        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext = (ConfigurationContext) config.
                getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        epr = backendServerURL + "ManageGenericArtifactService";

        try {
            stub = new ManageGenericArtifactServiceStub(configContext, epr);

            ServiceClient client = stub._getServiceClient();
            Options option = client.getOptions();
            option.setManageSession(true);
            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);

        } catch (AxisFault axisFault) {
            String msg = "Failed to initiate ManageGenericArtifactServiceClient. " +
                    axisFault.getMessage();
            log.error(msg, axisFault);
            throw new RegistryException(msg, axisFault);
        }
    }

    public String addArtifact(String key, String path, String lifecycleAttribute) throws Exception {
        return stub.addArtifact(key, path, lifecycleAttribute);
    }

    public String editArtifact(String key, String path, String lifecycleAttribute)
            throws Exception {
        return stub.editArtifact(key, path, lifecycleAttribute);
    }

    public ArtifactsBean listArtifacts(String key, String criteria) throws Exception {
        return stub.listArtifacts(key, criteria);
    }

    public String getArtifactContent(String path) throws Exception {
        return stub.getArtifactContent(path);
    }

    public String getArtifactUIConfiguration(String key) throws Exception {
        return stub.getArtifactUIConfiguration(key);
    }

    public boolean setArtifactUIConfiguration(String key, String content) throws Exception {
        return stub.setArtifactUIConfiguration(key, content);
    }

    public boolean canChange(String path) throws Exception {
        return stub.canChange(path);
    }
}
