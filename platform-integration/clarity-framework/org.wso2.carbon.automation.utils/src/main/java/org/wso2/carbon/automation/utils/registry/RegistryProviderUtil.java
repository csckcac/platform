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

package org.wso2.carbon.automation.utils.registry;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.coreutils.PlatformUtil;
import org.wso2.carbon.automation.core.utils.environmentutils.ProductUrlGeneratorUtil;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.app.RemoteRegistry;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.frameworkutils.FrameworkFactory;
import org.wso2.carbon.automation.core.utils.frameworkutils.FrameworkProperties;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Provide remote registries - ws-api, remote registry and governance registry
 */
public class RegistryProviderUtil {
    private static final Log log = LogFactory.getLog(RegistryProviderUtil.class);

    public WSRegistryServiceClient getWSRegistry(int userID, String productName)
            throws RegistryException, AxisFault {
        WSRegistryServiceClient registry = null;
        String userName;
        String password;
        ConfigurationContext configContext;
        String serverURL;
        EnvironmentBuilder env = new EnvironmentBuilder();
        if (env.getFrameworkSettings().getEnvironmentSettings().is_runningOnStratos()) {   //if Stratos tests are enabled.
            UserInfo userDetails = UserListCsvReader.getUserInfo(userID);
            userName = userDetails.getUserName();
            password = userDetails.getPassword();
            serverURL = getServiceURL(productName);
        } else {
            UserInfo userDetails = UserListCsvReader.getUserInfo(userID);
            userName = userDetails.getUserName();
            password = userDetails.getPassword();
            serverURL = getServiceURL(productName);
        }

        String axis2Repo = ProductConstant.getModuleClientPath();
        String axis2Conf = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + File.separator + "axis2config" + File.separator + "axis2_client.xml";
        PlatformUtil.setKeyStoreProperties();
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

    public Registry getGovernanceRegistry(WSRegistryServiceClient registry, int userId)
            throws RegistryException {
        Registry governance;
        UserInfo userDetails = UserListCsvReader.getUserInfo(userId);
        String userName = userDetails.getUserName();
        PlatformUtil.setKeyStoreProperties();

        try {
            governance = GovernanceUtils.getGovernanceUserRegistry(registry, userName);
        } catch (RegistryException e) {
            log.error("getGovernance Registry Exception thrown:" + e);
            throw new RegistryException("getGovernance Registry Exception thrown:" + e);
        }
        return governance;
    }

    public RemoteRegistry getRemoteRegistry(int userId, String productName)
            throws MalformedURLException, RegistryException {
        String registryURL;
        RemoteRegistry registry;
        UserInfo userDetails = UserListCsvReader.getUserInfo(userId);
        String username = userDetails.getUserName();
        String password = userDetails.getPassword();
        EnvironmentBuilder env = new EnvironmentBuilder();
        FrameworkProperties properties = FrameworkFactory.getFrameworkProperties(productName);

        if (env.getFrameworkSettings().getEnvironmentSettings().is_runningOnStratos()) {
            registryURL = ProductUrlGeneratorUtil.getRemoteRegistryURLOfStratos(properties.getProductVariables().
                    getHttpsPort(), properties.getProductVariables().getHostName(), properties, userDetails);
        } else {
            registryURL = ProductUrlGeneratorUtil.getRemoteRegistryURLOfProducts(properties.getProductVariables().
                    getHttpsPort(), properties.getProductVariables().getHostName(), properties.getProductVariables().getWebContextRoot());
        }

        log.info("Remote Registry URL" + registryURL);

        try {
            registry = new RemoteRegistry(new URL(registryURL), username, password);
        } catch (RegistryException e) {
            log.error("Error on initializing Remote Registry :" + e);
            throw new RegistryException("Error on initializing Remote Registry error  :" + e);
        } catch (MalformedURLException e) {
            log.error("Invalid registry URL :" + e);
            throw new MalformedURLException("Invalid registry URL" + e);
        }
        return registry;
    }

    private static String getServiceURL(String productName) {
        String serverURL;
        FrameworkProperties gregProperties = FrameworkFactory.getFrameworkProperties(productName);
        serverURL = gregProperties.getProductVariables().getBackendUrl();
        log.info("Server URL is :" + serverURL);
        return serverURL;
    }
}
