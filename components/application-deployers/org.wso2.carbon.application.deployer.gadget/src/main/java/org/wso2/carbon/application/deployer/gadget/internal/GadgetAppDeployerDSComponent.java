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
package org.wso2.carbon.application.deployer.gadget.internal;

import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.application.deployer.AppDeployerConstants;
import org.wso2.carbon.application.deployer.AppDeployerUtils;
import org.wso2.carbon.application.deployer.Feature;
import org.wso2.carbon.application.deployer.gadget.GadgetAppDeployer;
import org.wso2.carbon.application.deployer.gadget.GadgetAppUndeployer;
import org.wso2.carbon.application.deployer.service.ApplicationManagerService;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * @scr.component name="application.deployer.gadget" immediate="true"
 * @scr.reference name="application.manager"
 * interface="org.wso2.carbon.application.deployer.service.ApplicationManagerService"
 * cardinality="1..1" policy="dynamic" bind="setAppManager" unbind="unsetAppManager"
 */
public class GadgetAppDeployerDSComponent {

    private static Log log = LogFactory.getLog(GadgetAppDeployerDSComponent.class);

    private static ApplicationManagerService applicationManager;
    private static Map<String, List<Feature>> requiredFeatures;

    private GadgetAppDeployer gadgetDeployer = null;
    private GadgetAppUndeployer gadgetUndeployer = null;

    protected void activate(ComponentContext ctxt) {
        try {
            //register gadget deployer and undeployer in the ApplicationManager
            gadgetDeployer = new GadgetAppDeployer();
            gadgetUndeployer = new GadgetAppUndeployer();
            applicationManager.registerDeploymentHandler(gadgetDeployer);
            applicationManager.registerUndeploymentHandler(gadgetUndeployer);

            // read required-features.xml
            URL reqFeaturesResource = ctxt.getBundleContext().getBundle()
                    .getResource(AppDeployerConstants.REQ_FEATURES_XML);
            if (reqFeaturesResource != null) {
                InputStream xmlStream = reqFeaturesResource.openStream();
                requiredFeatures = AppDeployerUtils
                        .readRequiredFeaturs(new StAXOMBuilder(xmlStream).getDocumentElement());
            }
        } catch (Throwable e) {
            log.error("Failed to activate Gadget Application Deployer", e);
        }
    }

    protected void deactivate(ComponentContext ctxt) {
        //unregister deployer and undeployer in the ApplicationManager
        applicationManager.unregisterDeploymentHandler(gadgetDeployer);
        applicationManager.unregisterUndeploymentHandler(gadgetUndeployer);
    }

    protected void setAppManager(ApplicationManagerService appManager) {
        applicationManager = appManager;
    }

    protected void unsetAppManager(ApplicationManagerService appManager) {
        applicationManager = null;
    }

    public static Map<String, List<Feature>> getRequiredFeatures() {
        return requiredFeatures;
    }

}
