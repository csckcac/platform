/*
 * Copyright (c) 2005-2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.webapp.mgt;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.context.ApplicationContext;
import org.wso2.carbon.core.multitenancy.utils.TenantAxisUtils;
import org.wso2.carbon.tomcat.ext.valves.CarbonTomcatValve;
import org.wso2.carbon.url.mapper.HotUpdateService;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.webapp.mgt.utils.GhostWebappDeployerUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Handles management of webapps when ghost deployer is enabled. This includes deployment of
 * actual webapp from ghost form, dispatching requests to correct webapps, etc.
 */
public class GhostWebappDeployerValve implements CarbonTomcatValve {

    private static final Log log = LogFactory.getLog(GhostWebappDeployerValve.class);

    @Override
    public void invoke(HttpServletRequest request,
                       HttpServletResponse response) {
        if (!GhostWebappDeployerUtils.isGhostOn()) {
            return;
        }
        String requestURI = request.getRequestURI();

        //getting actual uri when accessing a virtual host through url mapping from the Map
        String requestedHostName = request.getServerName();
        ApplicationContext appContext = ApplicationContext.getCurrentApplicationContext();
        String uriOfVirtualHost = appContext.getApplicationFromUrlMapping(requestedHostName);
        //getting the host name of first request from registry if & only if the request contains url-mapper suffix
        if(TomcatUtil.isVirtualHostRequest(requestedHostName)) {
            uriOfVirtualHost = DataHolder.getHotUpdateService().
                    getApplicationContextForHost(requestedHostName);
        }
        if(uriOfVirtualHost != null) {
            requestURI = uriOfVirtualHost;
        }

        ConfigurationContext currentCtx;
        if (requestURI.contains("/t/")) {
            currentCtx = getCurrentConfigurationCtxFromURI(requestURI);
        } else {
            currentCtx = DataHolder.getServerConfigContext();
        }


        WebApplication deployedWebapp;
        if ((deployedWebapp = getDeployedWebappFromThisURI(requestURI, currentCtx)) == null) {
            WebApplication transitWebapp = GhostWebappDeployerUtils.
                    dispatchWebAppFromTransitGhosts(requestURI,
                                                    currentCtx);
            if (transitWebapp != null) {
                // if the webapp is found in the temp ghost list, we have to wait until the
                // particular webapp is deployed or unloaded..
                String isBeingUnloaded = (String) transitWebapp.
                        getProperty(CarbonConstants.IS_ARTIFACT_BEING_UNLOADED);
                if (isBeingUnloaded != null && "true".equals(isBeingUnloaded)) {
                    // wait until the webapp is unloaded by the unload task
                    GhostWebappDeployerUtils.
                            waitForWebAppToLeaveTransit(transitWebapp.getContextName(),
                                                        currentCtx);
                    // now the webapp is unloaded and in ghost form so we can safely
                    // continue with invocation
                    handleWebapp(transitWebapp.getWebappFile().getName(), currentCtx);
                } else {
                    // wait until webapp is deployed
                    GhostWebappDeployerUtils.
                            waitForWebAppToLeaveTransit(transitWebapp.getContextName(),
                                                        currentCtx);
                    try {
                        TomcatUtil.remapRequest(request);
                        return;
                    } catch (Exception e) {
                        log.error("Error when redirecting response to " + requestURI, e);
                    }
                }
            }
        } else {
            if (GhostWebappDeployerUtils.isGhostWebApp(deployedWebapp)) {
                handleWebapp(deployedWebapp.getWebappFile().getName(), currentCtx);
                try {
                    TomcatUtil.remapRequest(request);
                    return;
                } catch (Exception e) {
                    log.error("Error when redirecting response to " + requestURI, e);
                }
            } else {
                // This means the webapp is being accessed in normal form so we have to update
                // the lase used time
                GhostWebappDeployerUtils.updateLastUsedTime(deployedWebapp);
            }
        }
        if (!requestURI.contains(WebappsConstants.WEBAPP_INFO_JSP_PAGE)) {
            return;
        }

        String webappFileName = request.getParameter("webappFileName");
        handleWebapp(webappFileName, currentCtx);
    }

    private WebApplication getDeployedWebappFromThisURI(String requestURI,
                                                        ConfigurationContext cfgCtx) {
        WebApplication deployedWebapp = null;
        WebApplicationsHolder webApplicationsHolder = getWebApplicationHolder(cfgCtx);
        for (WebApplication webApplication : webApplicationsHolder.getStartedWebapps().values()) {
            if (requestURI.contains(webApplication.getContextName())) {
                deployedWebapp = webApplication;
            }
        }
        return deployedWebapp;
    }

    private WebApplicationsHolder getWebApplicationHolder(ConfigurationContext cfgCtx) {
        WebApplicationsHolder webApplicationsHolder;
        webApplicationsHolder = (WebApplicationsHolder)
                cfgCtx.getProperty(CarbonConstants.WEB_APPLICATIONS_HOLDER);

        return webApplicationsHolder;
    }

    private void handleWebapp(String webappFileName, ConfigurationContext cfgCtx) {
        if (webappFileName != null) {
            WebApplication ghostWebapp;
            WebApplicationsHolder webApplicationsHolder = getWebApplicationHolder(cfgCtx);

            if (webApplicationsHolder != null) {
                ghostWebapp = webApplicationsHolder.getStartedWebapps().get(webappFileName);

                if (ghostWebapp != null) {
                    GhostWebappDeployerUtils.
                            deployActualWebApp(ghostWebapp, cfgCtx);
                }
            }
        }
    }

    private ConfigurationContext getCurrentConfigurationCtxFromURI(String uri) {
        return TenantAxisUtils.
                getTenantConfigurationContextFromUrl(uri, DataHolder.getServerConfigContext());
    }
}