/*
 * Copyright 2004,2005 The Apache Software Foundation.
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
package org.wso2.carbon.webapp.mgt.internal;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.core.multitenancy.GenericArtifactUnloader;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.tomcat.ext.valves.CarbonTomcatValve;
import org.wso2.carbon.tomcat.ext.valves.TomcatValveContainer;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.webapp.mgt.DataHolder;
import org.wso2.carbon.webapp.mgt.TenantLazyLoaderValve;
import org.wso2.carbon.webapp.mgt.TomcatGhostValve;
import org.wso2.carbon.webapp.mgt.WebApplication;
import org.wso2.carbon.webapp.mgt.WebApplicationsHolder;
import org.wso2.carbon.webapp.mgt.WebContextParameter;
import org.wso2.carbon.webapp.mgt.multitenancy.WebappUnloader;
import org.wso2.carbon.webapp.mgt.utils.GhostWebappDeployerUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @scr.component name="org.wso2.carbon.webapp.mgt.internal.WebappManagementServiceComponent"
 * immediate="true"
 * @scr.reference name="config.context.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService"
 * cardinality="1..1" policy="dynamic"  bind="setConfigurationContextService"
 * unbind="unsetConfigurationContextService"
 * @scr.reference name="user.realmservice.default" interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1" policy="dynamic" bind="setRealmService"  unbind="unsetRealmService"
 * @scr.reference name="registry.service" interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic"  bind="setRegistryService" unbind="unsetRegistryService"
 * @scr.reference name="artifact.unloader.service" interface="org.wso2.carbon.core.multitenancy.GenericArtifactUnloader"
 * cardinality="1..1" policy="dynamic"  bind="setArtifactUnloaderService" unbind="unsetArtifactUnloaderService"
 */
public class WebappManagementServiceComponent {
    private static final Log log = LogFactory.getLog(WebappManagementServiceComponent.class);
    private WebappUnloader webappUnloader;


    protected void activate(ComponentContext ctx) {
        try {
            // Register the valves with Tomcat
            ArrayList<CarbonTomcatValve> valves = new ArrayList<CarbonTomcatValve>();
            valves.add(new TenantLazyLoaderValve());
            valves.add(new TomcatGhostValve());
            TomcatValveContainer.addValves(valves);

            if (!GhostWebappDeployerUtils.isGhostOn()) {
                // Adding server url as a parameter to webapps servlet context init parameter
                ConfigurationContext configurationContext = DataHolder.getServerConfigContext();

                WebApplicationsHolder webApplicationsHolder = (WebApplicationsHolder)
                        configurationContext.getProperty(CarbonConstants.WEB_APPLICATIONS_HOLDER);

                WebContextParameter serverUrlParam =
                        new WebContextParameter("webServiceServerURL", CarbonUtils.
                                getServerURL(ServerConfiguration.getInstance(),
                                             configurationContext));

                List<WebContextParameter> servletContextParameters =
                        (ArrayList<WebContextParameter>) configurationContext.
                                getProperty(CarbonConstants.SERVLET_CONTEXT_PARAMETER_LIST);

                if (servletContextParameters != null) {
                    servletContextParameters.add(serverUrlParam);
                }

                if (webApplicationsHolder != null) {
                    for (WebApplication application :
                            webApplicationsHolder.getStartedWebapps().values()) {
                        application.getContext().getServletContext().
                                setInitParameter(serverUrlParam.getName(),
                                                 serverUrlParam.getValue());
                    }
                }
            }
        } catch (Throwable e) {
            log.error("Error occurred while activating WebappManagementServiceComponent", e);
        }
    }

    protected void deactivate(ComponentContext ctx) {
//         TomcatValveContainer.removeValves();
    }

    protected void setConfigurationContextService(ConfigurationContextService contextService) {
        DataHolder.setServerConfigContext(contextService.getServerConfigContext());
    }

    protected void unsetConfigurationContextService(ConfigurationContextService contextService) {
        DataHolder.setServerConfigContext(null);
    }


    protected void setRealmService(RealmService realmService) {
        //keeping the realm service in the DataHolder class
        DataHolder.setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {
    }

    protected void setRegistryService(RegistryService registryService) {
    }

    protected void unsetRegistryService(RegistryService registryService) {
    }

    protected void setArtifactUnloaderService(GenericArtifactUnloader genericArtifactUnloader) {
         webappUnloader = new WebappUnloader();
         genericArtifactUnloader.registerArtifactUnloader(webappUnloader);
    }
    protected void unsetArtifactUnloaderService(GenericArtifactUnloader genericArtifactUnloader) {
    }
}
