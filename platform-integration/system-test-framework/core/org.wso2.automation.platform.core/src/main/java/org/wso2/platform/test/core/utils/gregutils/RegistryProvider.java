/*
* Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.platform.test.core.utils.gregutils;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.platform.test.core.utils.UserInfo;
import org.wso2.platform.test.core.utils.UserListCsvReader;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.platform.test.core.utils.frameworkutils.FrameworkFactory;
import org.wso2.platform.test.core.utils.frameworkutils.FrameworkProperties;

import java.io.File;


public class RegistryProvider {
    private static final Log log = LogFactory.getLog(RegistryProvider.class);
    private static WSRegistryServiceClient registry = null;
    private static Registry governance = null;

    public WSRegistryServiceClient getRegistry(int userID, String productName)
            throws RegistryException, AxisFault {
        String userName;
        String password;
        String tenantDomain;
        ConfigurationContext configContext;
        String serverURL;
        EnvironmentBuilder env = new EnvironmentBuilder();
        if (env.getFrameworkSettings().getEnvironmentSettings().is_runningOnStratos()) {   //if Stratos tests are enabled.
            UserInfo userDetails = UserListCsvReader.getUserInfo(userID);
            userName = userDetails.getUserName();
            password = userDetails.getPassword();
            tenantDomain = userDetails.getDomain();
            serverURL = getServiceURL(tenantDomain, productName);
        } else {
            UserInfo userDetails = UserListCsvReader.getUserInfo(userID);
            userName = userDetails.getUserName();
            password = userDetails.getPassword();
            serverURL = getServiceURL(null, productName);
        }

        String resourcePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION;
        String axis2Repo = ProductConstant.getModuleClientPath();
        String axis2Conf = resourcePath + File.separator + "axis2config" + File.separator + "axis2_client.xml";
        setSystemProperties();
        try {
            configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem(axis2Repo, axis2Conf);
            int timeOutInMilliSeconds = 1000 * 60;
            configContext.setProperty(HTTPConstants.CONNECTION_TIMEOUT, timeOutInMilliSeconds);
            log.info("Group ConfigurationContext Timeout " + configContext.getServiceGroupContextTimeoutInterval());
            registry = new WSRegistryServiceClient(serverURL, userName, password, configContext);
            log.info("WS Registry -Login Success");
        } catch (AxisFault axisFault) {
            log.error("Unable to initialize WSRegistryServiceClient :" + axisFault.getMessage());
            throw new AxisFault("Unable to initialize WSRegistryServiceClient :" + axisFault.getMessage());
        } catch (RegistryException e) {
            log.error("Unable to initialize WSRegistryServiceClient:" + e);
            throw new RegistryException("Unable to initialize WSRegistryServiceClient:" + e);
        }
        return registry;
    }

    public Registry getGovernance(WSRegistryServiceClient registry, int userId)
            throws RegistryException {
        UserInfo userDetails = UserListCsvReader.getUserInfo(userId);
        String userName = userDetails.getUserName();
        setSystemProperties();

        try {
            governance = GovernanceUtils.getGovernanceUserRegistry(registry, userName);
        } catch (RegistryException e) {
            log.error("getGovernance Registry Exception thrown:" + e);
            throw new RegistryException("getGovernance Registry Exception thrown:" + e);
        }
        return governance;
    }

    private static void setSystemProperties() {
        EnvironmentBuilder env = new EnvironmentBuilder();
        System.setProperty("javax.net.ssl.trustStore", env.getFrameworkSettings().getEnvironmentVariables().getKeystorePath());
        System.setProperty("javax.net.ssl.trustStorePassword", env.getFrameworkSettings().getEnvironmentVariables().getKeyStrorePassword());
        System.setProperty("javax.net.ssl.trustStoreType", "JKS");
        System.setProperty("carbon.repo.write.mode", "true");
    }

    private static String getServiceURL(String tenantDomain, String productName) {
        String serverURL;
        EnvironmentBuilder env = new EnvironmentBuilder();
        FrameworkProperties gregProperties = FrameworkFactory.getFrameworkProperties(productName);

//        if (env.getFrameworkSettings().getEnvironmentSettings().is_runningOnStratos()) {
//            serverURL = "https://" + gregProperties.getProductVariables().getHostName() + "/t/" + tenantDomain + "/" + "services" + "/";
//        } else {
        serverURL = gregProperties.getProductVariables().getBackendUrl();
        log.info(serverURL);
//        }
        log.info("Server URL is :" + serverURL);
        return serverURL;
    }
}
