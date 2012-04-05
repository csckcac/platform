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

package org.wso2.carbon.gadget.initializer.internal;

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
import org.wso2.carbon.gadget.initializer.GadgetInitializerContext;
import org.wso2.carbon.gadget.initializer.modules.oauth.GSOAuthModule;
import org.wso2.carbon.gadget.initializer.modules.oauth.utils.OAuthUtils;
import org.wso2.carbon.gadget.initializer.servlets.MakeSoapRequestServlet;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.ui.util.CarbonUIAuthenticationUtil;
import org.wso2.carbon.utils.CarbonUtils;
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
import java.util.concurrent.ExecutionException;

/**
 * @scr.component name="org.wso2.carbon.gadget.initializer" immediate="true"
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
public class GadgetInitializerServiceComponent {

    // Shindig properties
    protected static final String INJECTOR_ATTRIBUTE = "guice-injector";
    protected static final String MODULES_ATTRIBUTE = "guice-modules";

    private static HttpService httpServiceInstance;

    private static final Log log = LogFactory.getLog(GadgetInitializerServiceComponent.class);

    private List<ServiceRegistration> serviceRegistrations = null;

    private static ServletContext servletCtx = null;

    private static BundleContext bctx = null;


    protected void activate(ComponentContext context) {
        try {
            initShindig(servletCtx);
            bctx = context.getBundleContext();
            registerServlets(context.getBundleContext());

            log.debug("Gadget Component initialized");

        } catch (Exception e) {
            log.error("Failed to initialize gadget component : " + e.getMessage(), e);
        }
    }

    protected void deactivate(ComponentContext context) {
        unregisterServlets();
        log.debug("******* Dashboard UI Component bundle is deactivated ******* ");
    }

    protected void setServletContextService(ServletContext servletContext) {
        this.servletCtx = servletContext;
        //initShindig(servletContext);
    }

    protected void unsetServletContextService(ServletContext servletContext) {
        servletContext.removeAttribute(INJECTOR_ATTRIBUTE);
    }

    /**
     * Initilizes Shindig
     *
     * @param context
     */
    protected void initShindig(ServletContext context) throws Exception {

        System.setProperty("shindig-host-context", getHostWithContext());
        System.setProperty("shindig.host", getHostName());
        System.setProperty("shindig.port", getHttpPort());

        String moduleNames = "org.apache.shindig.common.PropertiesModule:" +
                "org.apache.shindig.gadgets.DefaultGuiceModule:" +
                "org.apache.shindig.social.core.config.SocialApiGuiceModule:" +
                "org.apache.shindig.social.sample.SampleModule:" +
                "org.apache.shindig.gadgets.oauth.OAuthModule:" +
                "org.apache.shindig.gadgets.oauth2.OAuth2Module:" +
                "org.apache.shindig.gadgets.oauth2.OAuth2MessageModule:" +
                "org.apache.shindig.gadgets.oauth2.handler.OAuth2HandlerModule:" +
                "org.apache.shindig.gadgets.oauth2.persistence.sample.OAuth2PersistenceModule:" +
                "org.apache.shindig.common.cache.ehcache.EhCacheModule:" +
                "org.apache.shindig.extras.ShindigExtrasGuiceModule:" +
                "org.apache.shindig.gadgets.admin.GadgetAdminModule";

        List<Module> modules = Lists.newLinkedList();
        for (String moduleName : moduleNames.split(":")) {
            try {
                moduleName = moduleName.trim();
                if (moduleName.length() > 0) {
                    modules.add((Module) Class.forName(moduleName).newInstance());
                }
            } catch (ClassNotFoundException cnf) {
                if (moduleName.contains("org.wso2.carbon.dashboard.social.GuiceModuleImpl")) {
                    modules.add((Module) Class.forName("org.apache.shindig.social.sample.SampleModule").newInstance());
                } else {
                    throw new RuntimeException(cnf);
                }
            }
        }
        try {
            Injector injector = null;
            injector = Guice.createInjector(Stage.PRODUCTION, modules);
            GSOAuthModule.OAuthStoreProvider provider = injector.getInstance(GSOAuthModule.OAuthStoreProvider.class);
            OAuthUtils.setOauthStoreProvider(provider);
            context.setAttribute(INJECTOR_ATTRIBUTE, injector);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    private String getHostWithContext() {
        String hostWithContext;
        try {
            String webAppContext = GadgetInitializerContext.getConfigContext().getContextRoot();
            if (webAppContext.equals("/")) {
                hostWithContext = System.getProperty("carbon.local.ip") + ":" + getHttpPort();
            } else {
                hostWithContext = System.getProperty("carbon.local.ip") + ":" + getHttpPort() + webAppContext;
            }

            return hostWithContext;

        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }
    }

    private String getHttpPort() {
        int port = -1;
        try {
            port = CarbonUtils.getTransportProxyPort(GadgetInitializerContext.getConfigContext(), "http");
            if (port == -1) {
                port = CarbonUtils.getTransportPort(GadgetInitializerContext.getConfigContext(), "http");
            }

        } catch (Exception e) {
            log.error(e);
        }

        return Integer.toString(port);
    }

    private String getHostName() {
        String hostName = CarbonUtils.getServerConfiguration().getFirstProperty("HostName");

        if (hostName == null) {
            hostName = System.getProperty("carbon.local.ip");
        }

        return hostName;
    }

    /**
     * Registers the requires Shindig Servlets
     *
     * @param bundleContext
     */
    private void registerServlets(BundleContext bundleContext) throws Exception {
        try {

            ServiceRegistration temp = null;
            if (serviceRegistrations != null) {
                // Avoid duplicate registration
                return;
            }

            serviceRegistrations = new LinkedList<ServiceRegistration>();

            //Registering a filter for a stub servlet
            HttpServlet authServlet = new HttpServlet() {
                // the redirector filter will forward the request before this servlet is hit
                protected void doGet(HttpServletRequest request, HttpServletResponse response)
                        throws ServletException, IOException {
                }
            };

            registerServlets();

        } catch (Exception e) {
            String msg = "Failed to Register the Shindig Servlets";
            log.error(msg, e);
            throw new Exception(msg, e);
        }
    }

    private void unregisterServlets() {
        for (ServiceRegistration reg : serviceRegistrations) {
            reg.unregister();
        }
        serviceRegistrations = null;
    }

    protected void setConfigurationContextService(ConfigurationContextService configCtx) {
        GadgetInitializerContext.setConfigContextService(configCtx);
    }

    protected void unsetConfigurationContextService(ConfigurationContextService configCtx) {
        GadgetInitializerContext.setConfigContextService(null);
    }

    private void registerServlets() throws Exception {
        registerXMLtoHTMLServlet();
        registerAccelServlet();
        registerMakeRequestServlet();
        registerProxyServlet();
        registerConcatServlet();
        registerOAuthCallbackServlet();
        registerMetadataServlet();
        registerJsServlet();
        registerRestApiServlet();
        registerJsonRpcServlet();
        registerSOAPRequestServlet();
        registerOAuth2callbackServlet();
    }

    private void registerServlet(BundleContext bundleContext, HttpServlet servlet, Dictionary servletParams) throws Exception {
        ServiceRegistration temp = bundleContext
                .registerService(Servlet.class.getName(), servlet, servletParams);
        if (temp != null) {
            serviceRegistrations.add(temp);
            temp = null;
        }
    }

    private void registerXMLtoHTMLServlet() throws Exception {
        GadgetRenderingServlet gadgetRenderingServlet = new GadgetRenderingServlet();
        AuthenticationServletFilter authFilter = new AuthenticationServletFilter();
        Dictionary servletMappings = new Hashtable();
        servletMappings.put("servlet-name", "xml-to-html");
        servletMappings.put("url-pattern", "/gadgets/ifr");
        registerServlet(bctx, gadgetRenderingServlet, servletMappings);
    }

    private void registerAccelServlet() throws Exception {
        HtmlAccelServlet htmlAccelServlet = new HtmlAccelServlet();
        Dictionary servletMappings = new Hashtable();
        servletMappings.put("servlet-name", "accel");
        servletMappings.put("url-pattern", "/gadgets/accel");
        registerServlet(bctx, htmlAccelServlet, servletMappings);
    }

    private void registerMakeRequestServlet() throws Exception {
        MakeRequestServlet makeRequestServlet = new MakeRequestServlet();
        AuthenticationServletFilter authFilter = new AuthenticationServletFilter();
        Dictionary servletMappings = new Hashtable();
        servletMappings.put("servlet-name", "makeRequest");
        servletMappings.put("url-pattern", "/gadgets/makeRequest");
        servletMappings.put("associated-filter", authFilter);
        registerServlet(bctx, makeRequestServlet, servletMappings);
    }

    private void registerProxyServlet() throws Exception {
        ProxyServlet proxyServlet = new ProxyServlet();
        Dictionary servletMappings = new Hashtable();
        servletMappings.put("servlet-name", "proxy");
        servletMappings.put("url-pattern", "/gadgets/proxy");
        registerServlet(bctx, proxyServlet, servletMappings);
    }

    private void registerConcatServlet() throws Exception {
        ConcatProxyServlet concatProxyServlet = new ConcatProxyServlet();
        Dictionary servletMappings = new Hashtable();
        servletMappings.put("servlet-name", "concat");
        servletMappings.put("url-pattern", "/gadgets/concat");
        registerServlet(bctx, concatProxyServlet, servletMappings);
    }

    private void registerOAuthCallbackServlet() throws Exception {
        OAuthCallbackServlet oauthCallbackServlet = new OAuthCallbackServlet();
        Dictionary servletMappings = new Hashtable();
        servletMappings.put("servlet-name", "oauthCallback");
        servletMappings.put("url-pattern", "/gadgets/oauthcallback");
        registerServlet(bctx, oauthCallbackServlet, servletMappings);
    }

    private void registerMetadataServlet() throws Exception {
        RpcServlet rpcServlet = new RpcServlet();
        Dictionary servletMappings = new Hashtable();
        servletMappings.put("servlet-name", "metadata");
        servletMappings.put("url-pattern", "/gadgets/metadata");
        registerServlet(bctx, rpcServlet, servletMappings);
    }

    private void registerJsServlet() throws Exception {
        JsServlet jsServlet = new JsServlet();
        Dictionary servletMappings = new Hashtable();
        servletMappings.put("servlet-name", "js");
        servletMappings.put("url-pattern", "/gadgets/js");
        registerServlet(bctx, jsServlet, servletMappings);
    }

    private void registerRestApiServlet() throws Exception {
        registerRestApiServlet("/rest");
        registerRestApiServlet("/gadgets/api/rest");
        registerRestApiServlet("/social/rest");
    }

    private void registerRestApiServlet(String urlPattern) throws Exception {
        DataServiceServlet dataServiceServlet = new DataServiceServlet();
        AuthenticationServletFilter authFilter = new AuthenticationServletFilter();
        Dictionary servletMappings = new Hashtable();
        Dictionary<String, String> initParams = new Hashtable<String, String>();
        initParams.put("handlers", "org.apache.shindig.handlers");

        servletMappings.put("url-pattern", urlPattern);
        servletMappings.put("servlet-params", initParams);
        servletMappings.put("associated-filter", authFilter);
        registerServlet(bctx, dataServiceServlet, servletMappings);
    }

    private void registerJsonRpcServlet() throws Exception {
        registerJsonRpcServlet("/rpc");
        registerJsonRpcServlet("/gadgets/api/rpc");
        registerJsonRpcServlet("/social/rpc");
    }

    private void registerJsonRpcServlet(String urlPattern) throws Exception {
        JsonRpcServlet jsonRpcServlet = new JsonRpcServlet();
        AuthenticationServletFilter authFilter = new AuthenticationServletFilter();
        Dictionary servletMappings = new Hashtable();
        Dictionary<String, String> initParams = new Hashtable<String, String>();
        initParams.put("handlers", "org.apache.shindig.handlers");
        servletMappings.put("url-pattern", urlPattern);
        servletMappings.put("servlet-params", initParams);
        servletMappings.put("associated-filter", authFilter);
        registerServlet(bctx, jsonRpcServlet, servletMappings);
    }

    private void registerSOAPRequestServlet() throws Exception {
        MakeSoapRequestServlet makeSoapRequestServlet = new MakeSoapRequestServlet();
        Dictionary servletMappings = new Hashtable();
        servletMappings.put("servlet-name", "soapRequest");
        servletMappings.put("url-pattern", "/gadgets/makeSOAPRequest");
        registerServlet(bctx, makeSoapRequestServlet, servletMappings);
    }

    private void registerOAuth2callbackServlet() throws Exception {
        OAuth2CallbackServlet oauth2CallbackServlet = new OAuth2CallbackServlet();
        Dictionary servletMappings = new Hashtable();
        servletMappings.put("servlet-name", "oauth2callback");
        servletMappings.put("url-pattern", "/gadgets/oauth2callback");
        registerServlet(bctx, oauth2CallbackServlet, servletMappings);
    }

}
