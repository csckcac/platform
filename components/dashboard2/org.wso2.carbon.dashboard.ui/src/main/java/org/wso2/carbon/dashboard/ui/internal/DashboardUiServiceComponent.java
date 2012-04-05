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

package org.wso2.carbon.dashboard.ui.internal;

import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.shindig.auth.AuthenticationServletFilter;
import org.apache.shindig.gadgets.servlet.*;
import org.apache.shindig.protocol.DataServiceServlet;
import org.apache.shindig.protocol.JsonRpcServlet;
import org.osgi.framework.BundleContext;
import org.osgi.service.http.HttpService;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.wso2.carbon.gadget.initializer.modules.oauth.utils.OAuthUtils;
import org.wso2.carbon.gadget.initializer.modules.oauth.GSOAuthModule;
import org.wso2.carbon.dashboard.ui.DashboardUiContext;
import org.wso2.carbon.dashboard.ui.servlets.MakeSoapRequestServlet;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.base.ServerConfiguration;

import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

/**
 * @scr.component name="org.wso2.carbon.dashboard.ui" immediate="true"
 * @scr.reference name="servlet.context.service"
 * interface="javax.servlet.ServletContext"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setServletContextService"
 * unbind="unsetServletContextService"
 * @scr.reference name="config.context.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setConfigurationContextService"
 * unbind="unsetConfigurationContextService"
 */
public class DashboardUiServiceComponent {

    // Shindig properties
    protected static final String INJECTOR_ATTRIBUTE = "guice-injector";
    protected static final String MODULES_ATTRIBUTE = "guice-modules";

    private static HttpService httpServiceInstance;

    private static final Log log = LogFactory.getLog(DashboardUiServiceComponent.class);

    private List<ServiceRegistration> serviceRegistrations = null;

    private static ServletContext servletCtx = null;

    private static BundleContext bctx = null;


    protected void activate(ComponentContext context) {

    }

    protected void deactivate(ComponentContext context) {
        log.debug("Dashboard UI Component bundle is deactivated");
    }

    protected void setServletContextService(ServletContext servletContext) {
        this.servletCtx = servletContext;
    }

    protected void unsetServletContextService(ServletContext servletContext) {
        servletContext.removeAttribute(INJECTOR_ATTRIBUTE);
    }

    private String getHostWithContext() {
        String hostWithContext;
        try {
            String webAppContext = DashboardUiContext.getConfigContext().getContextRoot();
            if (webAppContext.equals("/")) {
                hostWithContext = System.getProperty("carbon.local.ip") + ":" + getBackendHttpPort();
            } else {
                hostWithContext = System.getProperty("carbon.local.ip") + ":" + getBackendHttpPort() + webAppContext;
            }

            return hostWithContext;

        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }
    }

    private String getBackendHttpPort() {
        String httpPort = null;
        try {
            httpPort = ServerConfiguration.getInstance().getFirstProperty("RegistryHttpPort");
            if (httpPort == null) {
                httpPort = (String) DashboardUiContext.getConfigContext().getAxisConfiguration().getTransportIn("http")
                        .getParameter("port").getValue();
            }

        } catch (Exception e) {
            log.error(e);
        }

        return httpPort;
    }

    protected void setConfigurationContextService(ConfigurationContextService configCtx) {
        DashboardUiContext.setConfigContextService(configCtx);
    }

    protected void unsetConfigurationContextService(ConfigurationContextService configCtx) {
        DashboardUiContext.setConfigContextService(null);
    }
}
