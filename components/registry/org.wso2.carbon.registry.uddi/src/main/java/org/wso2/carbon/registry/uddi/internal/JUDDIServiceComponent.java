/*
 * Copyright (c) 2006, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.registry.uddi.internal;

import org.apache.axis2.deployment.Deployer;
import org.apache.axis2.deployment.DeploymentEngine;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.registry.uddi.deployer.JUDDIJAXWSDeployer;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.io.File;
import java.io.FilenameFilter;

/**
 * @scr.component name="registry.uddi.component" immediate="true"
 * @scr.reference name="config.context.service" interface="org.wso2.carbon.utils.ConfigurationContextService" cardinality="1..1"
 * policy="dynamic" bind="setConfigurationContextService"
 * unbind="unsetConfigurationContextService"
 */
public class JUDDIServiceComponent {

    private static final Log log = LogFactory.getLog(JUDDIServiceComponent.class);

    private static ConfigurationContext configContext;

    private static final String COMPONENTS = "components";
    private static final String PLUGINS = "plugins";
    private static final String REPOSITORY = "repository";
    private static final String ENABLE = "enable";
    private static final String UDDI_SYSTEM_PROPERTY = "uddi";

    JUDDIJAXWSDeployer uddiJAXWSDeployer;


    /**
     * Deploy the jUDDI jaxws service progammatically. First this will find the juddi bundle in the plugins directory.
     * Because this bundle has the required. JAXWS service for deploying the juddi service. After picking the correct bundle.
     * It will deploy
     *
     * @param ctxt
     */
    protected void activate(ComponentContext ctxt) {
        if(ENABLE.equals(System.getProperty(UDDI_SYSTEM_PROPERTY))){
            deployUDDIService();
        }
    }


    private void deployUDDIService() {
        Deployer jaxwsDeployer =
                ((DeploymentEngine) configContext.getAxisConfiguration().getConfigurator()).getDeployer("servicejars", "jar");
        uddiJAXWSDeployer = new JUDDIJAXWSDeployer(jaxwsDeployer, configContext);
        File jaxwsService = findTheJAXWSService();
        if (jaxwsService != null) {
            DeploymentFileData deploymentFileData = new DeploymentFileData(jaxwsService);
            uddiJAXWSDeployer.deploy(deploymentFileData);
        } else {
            log.error("jUDDI Service Deployment failed");
        }
    }


    /**
     * Find the juddi jar in plugins directory
     *
     * @return
     */
    private File findTheJAXWSService() {
        String carbonPluginsDir = CarbonUtils.getCarbonHome()
                + File.separator + REPOSITORY + File.separator + COMPONENTS + File.separator + PLUGINS;
        File jaxwsService = null;
        File pluginsDir = new File(carbonPluginsDir);
        String[] children = pluginsDir.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.toLowerCase().startsWith("juddi") && name.toLowerCase().endsWith(".jar");
            }
        }
        );
        if (children == null) {
            log.error("Couldn't find jUDDI JAX-WS service");
        } else if (children.length > 1) {
            log.error("Couldn't find the correct jUDDI JAX-WS service, there are more than one juddi bundles in pluggins.");
        } else {
            jaxwsService = new File(carbonPluginsDir + File.separator + children[0]);
        }
        return jaxwsService;
    }

    /**
     * @param ctxt
     */
    protected void deactivate(ComponentContext ctxt) {

    }


    /**
     * @param contextService
     */
    protected void setConfigurationContextService(ConfigurationContextService contextService) {
        updateConfigContext(contextService.getServerConfigContext());
    }

    /**
     * @param contextService
     */
    protected void unsetConfigurationContextService(ConfigurationContextService contextService) {
        updateConfigContext(null);
    }

    private static void updateConfigContext(ConfigurationContext context) {
        configContext = context;
    }
}
