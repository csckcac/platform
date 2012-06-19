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
package org.wso2.carbon.identity.oauth.ui.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.http.HttpService;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.identity.oauth.ui.OAuthServlet;
import org.wso2.carbon.identity.oauth.ui.endpoints.authz.OAuth2AuthzEndpoint;
import org.wso2.carbon.identity.oauth.ui.endpoints.token.OAuth2EndpointApp;
import org.wso2.carbon.identity.oauth.ui.endpoints.token.OAuth2TokenEndpointServlet;
import org.wso2.carbon.utils.ConfigurationContextService;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;
import java.util.Dictionary;
import java.util.Hashtable;

/**
 * @scr.component name="identity.provider.oauth.ui.component" immediate="true"
 * @scr.reference name="osgi.httpservice" interface="org.osgi.service.http.HttpService"
 * cardinality="1..1" policy="dynamic" bind="setHttpService"  unbind="unsetHttpService"
 * @scr.reference name="config.context.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService" cardinality="1..1"
 * policy="dynamic" bind="setConfigurationContextService"
 * unbind="unsetConfigurationContextService"
 * @scr.reference name="server.configuration" interface="org.wso2.carbon.base.api.ServerConfigurationService"
 * cardinality="1..1" policy="dynamic" bind="setServerConfigurationService" unbind="unsetServerConfigurationService"
 */
public class OAuthUIServiceComponent {

    private static final Log log = LogFactory.getLog(OAuthUIServiceComponent.class);
    private static final String PATH = "/oauth2/token";

    protected void activate(ComponentContext context) {
        log.debug("Activating Identity OAuth UI bundle.");

        //Register the OAuth 1.0a Servlet to handle OAuth Endpoints, request-token, authorize-token and access-token
        HttpServlet oauthServlet = new OAuthServlet();
        Dictionary oauthServletParams = new Hashtable(2);
        oauthServletParams.put("url-pattern", "/oauth");
        oauthServletParams.put("display-name", "OAuth 1.0a Endpoint Handler.");
        context.getBundleContext().registerService(Servlet.class.getName(), oauthServlet, oauthServletParams);
        log.debug("Successfully registered an instance of OAuthServlet");

        //Register a servlet to to act as the OAuth 2.0 Authorize Endpoint
        HttpServlet oauth2AuthzEndpointServlet = new OAuth2AuthzEndpoint();
        Dictionary oauth2AuthzEndpointParams = new Hashtable(2);
        oauth2AuthzEndpointParams.put("url-pattern", "/oauth2/authorize");
        oauth2AuthzEndpointParams.put("display-name", "OAuth 2.0 Authorize Endpoint.");
        context.getBundleContext().registerService(Servlet.class.getName(),
                oauth2AuthzEndpointServlet,
                oauth2AuthzEndpointParams);
        log.debug("Successfully registered an instance of OAuth2 Authorization Endpoint");

        log.debug("Successfully activated Identity OAuth UI bundle.");

    }

    protected void deactivate(ComponentContext context) {
        log.debug("Identity OAuth UI bundle is deactivated");
    }

    protected void setHttpService(HttpService httpService){
            Dictionary oauth2TokEndpointParams = new Hashtable();
            oauth2TokEndpointParams.put("javax.ws.rs.Application", OAuth2EndpointApp.class.getName());
            try {
                httpService.registerServlet(PATH, new OAuth2TokenEndpointServlet(), oauth2TokEndpointParams, null);
            } catch (Exception e) {
                log.error("Error when registering the OAuth2TokenEndpointServlet via the HttpService.", e);
                throw new RuntimeException("Error when registering the OAuth2TokenEndpointServlet via the HttpService.", e);
            }
            log.debug("Successfully registered an instance of OAuth2 Token Endpoint");
    }

    protected void unsetHttpService(HttpService httpService){
        httpService.unregister("/oauth2/token");
    }

    protected void setConfigurationContextService(ConfigurationContextService configurationContextService){
        OAuthUIServiceComponentHolder.getInstance().setConfigurationContextService(configurationContextService);
        log.debug("ConfigurationContextService Instance was set.");
    }

    protected void unsetConfigurationContextService(ConfigurationContextService configurationContextService){
        OAuthUIServiceComponentHolder.getInstance().setConfigurationContextService(null);
        log.debug("ConfigurationContextService Instance was unset.");
    }

    protected void setServerConfigurationService(ServerConfigurationService serverConfigService){
        OAuthUIServiceComponentHolder.getInstance().setServerConfigurationService(serverConfigService);
        log.debug("ServerConfigurationService instance was set.");
    }

    protected void unsetServerConfigurationService(ServerConfigurationService serverConfigService){
        OAuthUIServiceComponentHolder.getInstance().setServerConfigurationService(null);
        log.debug("ServerConfigurationService instance was unset.");
    }

}
