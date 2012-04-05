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
import org.wso2.carbon.identity.oauth.ui.OAuth2Servlet;
import org.wso2.carbon.identity.oauth.ui.OAuthServlet;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;
import java.util.Dictionary;
import java.util.Hashtable;

/**
 * @scr.component name="identity.provider.oauth.ui.component" immediate="true"
 */
public class OAuthUIServiceComponent {

    private static final Log log = LogFactory.getLog(OAuthUIServiceComponent.class);

    protected void activate(ComponentContext context) {
        log.debug("Activating Identity OAuth UI bundle.");

        //Register the OAuth 1.0a Servlet to handle OAuth Endpoints, request-token, authorize-token and access-token
        HttpServlet oauthServlet = new OAuthServlet();
        Dictionary oauthServletParams = new Hashtable(2);
        oauthServletParams.put("url-pattern", "/oauth");
        oauthServletParams.put("display-name", "OAuth 1.0a Endpoint Handler.");
        context.getBundleContext().registerService(Servlet.class.getName(), oauthServlet, oauthServletParams);
        log.debug("Successfully registered an instance of OAuthServlet");

        //Register the OAuth 2.0 Servlet to handle OAuth 2.0 Endpoints, authorize-token and access-token
        HttpServlet oauth2Servlet = new OAuth2Servlet();
        Dictionary oauth2ServletParams = new Hashtable(2);
        oauth2ServletParams.put("url-pattern", "/oauth2");
        oauth2ServletParams.put("display-name", "OAuth 2.0 Endpoint Handler.");
        context.getBundleContext().registerService(Servlet.class.getName(), oauth2Servlet, oauth2ServletParams);
        log.debug("Successfully registered an instance of OAuth2Servlet");

        log.debug("Successfully activated Identity OAuth UI bundle.");

    }

    protected void deactivate(ComponentContext context) {
        log.debug("Identity OAuth UI bundle is deactivated");
    }

}
