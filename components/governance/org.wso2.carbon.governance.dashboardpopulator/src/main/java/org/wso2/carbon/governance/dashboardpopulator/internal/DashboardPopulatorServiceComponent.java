/*
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

package org.wso2.carbon.governance.dashboardpopulator.internal;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.governance.dashboardpopulator.DashboardPopulatorContext;
import org.wso2.carbon.governance.dashboardpopulator.GadgetPopulator;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.utils.ServerConstants;
import org.wso2.carbon.user.core.UserRealm;

import java.io.InputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;

/**
 * @scr.component name="org.wso2.carbon.governance.dashboardpopulator" immediate="true"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setRegistryService"
 * unbind="unsetRegistryService"
 */
public class DashboardPopulatorServiceComponent {

    private static final Log log = LogFactory.getLog(DashboardPopulatorServiceComponent.class);

    private static final String REGISTRY_SYSTEM_DASHBOARDS_ROOT = "/system/dashboards";
    
    protected void activate(ComponentContext context) {
        try {
            log.debug("Dashboard Populator for Governance - bundle is activated ");

            String dashboardDiskRoot = System.getProperty(ServerConstants.CARBON_HOME) + File
                    .separator + "resources" + File.separator + "dashboard";

            String dashboardConfigFile = dashboardDiskRoot + File.separator + "dashboard.xml";
            String gadgetsDiskLocation = dashboardDiskRoot + File.separator + "gadgets";

            // Check whether the system dasboard is already available if not populate
            Registry registry = DashboardPopulatorContext.getRegistry();
            if (!registry.resourceExists(REGISTRY_SYSTEM_DASHBOARDS_ROOT)) {

                // Creating an OMElement from file
                File dashboardConfigXml = new File(dashboardConfigFile);

                if (dashboardConfigXml.exists()) {
                    StAXOMBuilder sab = new StAXOMBuilder(new FileInputStream(dashboardConfigFile));
                    OMElement dashboardsRootEl = sab.getDocument().getOMDocumentElement();

                    FileReader dashboardConfigXmlReader = new FileReader(dashboardConfigXml);

                    // Restoring from file
                    registry.restore(REGISTRY_SYSTEM_DASHBOARDS_ROOT, dashboardConfigXmlReader);

                    log.info("Successfully populated the default Dashboards.");

                } else {
                    log.info("Couldn't find a Dashboard at '" + dashboardConfigFile +
                            "'. Giving up.");
                }
            }

            // Check whether Gadgets are stored. If not store
            if (!registry.resourceExists(GadgetPopulator.SYSTEM_GADGETS_PATH)) {

                File gadgetsDir = new File(gadgetsDiskLocation);
                if (gadgetsDir.exists()) {
                    GadgetPopulator.beginFileTansfer(gadgetsDir);

                    log.info("Successfully populated the default Gadgets.");
                } else {
                    log.info("Couldn't find contents at '" + gadgetsDiskLocation +
                            "'. Giving up.");
                }
            }

        } catch (Exception e) {
            log.debug("Failed to activate Dashboard Populator for Governance bundle ");
        }
    }

    protected void deactivate(ComponentContext context) {
        log.debug("Dashboard Populator for Governance bundle is deactivated ");
    }

    protected void setRegistryService(RegistryService registryService) {
        log.debug("Setting the Registry Service");
        DashboardPopulatorContext.setRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {
        log.debug("Unsetting the Registry Service");
        DashboardPopulatorContext.setRegistryService(null);
    }
}
