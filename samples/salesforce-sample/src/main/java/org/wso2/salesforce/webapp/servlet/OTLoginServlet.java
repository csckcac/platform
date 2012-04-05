/*
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.salesforce.webapp.servlet;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.salesforce.webapp.constants.SalesForceWebAppConstants;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * This servlet class handles the OT login process.
 */
public class OTLoginServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static Log log = LogFactory.getLog(OTLoginServlet.class);

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response) throws ServletException, IOException {
        int statusCode = 0;
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        HttpSession session = request.getSession();
        String context = request.getContextPath();
        HttpClient httpclient = new HttpClient();
        PostMethod post = new PostMethod(SalesForceWebAppConstants.OT_LOGIN_XML_PATH);
        try {
            post.setRequestEntity(new StringRequestEntity("mail=" +
                    URLEncoder.encode(username, "UTF-8") + "&password=" + URLEncoder.encode(
                    password, "UTF-8"), "application/x-www-form-urlencoded", "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            log.error("Unable to encode username/password");
            response.sendRedirect(response.encodeRedirectURL(context + "/otauth/login-error.jsp"));
        }
        try {
            statusCode = httpclient.executeMethod(post);
        } catch (Throwable e) {
            log.error("Unable to execute post method towards the endpoint " +
                    SalesForceWebAppConstants.OT_LOGIN_XML_PATH);
            response.sendRedirect(response.encodeRedirectURL(context + "/otauth/login-error.jsp"));
        }

        if (statusCode == 200) {
            log.info(username + ", successfully logged in!");
            session.setAttribute("isLoggedIn", true);
            session.setAttribute("username", username);
            response.sendRedirect(response.encodeRedirectURL(context +
                    "/salesforce/login.jsp?username=" + username));
        } else {
            log.error("Login failed for user " + username);
            response.sendRedirect(response.encodeRedirectURL(context + "/otauth/login-error.jsp"));
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws
            IOException, ServletException {
        this.doPost(request, response);
    }

}
