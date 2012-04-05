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
package org.wso2.carbon.identity.sts.passive.ui;


import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.identity.sts.passive.stub.types.RequestToken;
import org.wso2.carbon.identity.sts.passive.stub.types.ResponseToken;
import org.wso2.carbon.identity.sts.passive.ui.client.IdentityPassiveSTSClient;
import org.wso2.carbon.ui.CarbonUIUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;

public class PassiveSTS extends HttpServlet {

    private static final Log log = LogFactory.getLog(PassiveSTS.class);

    /**
     *
     */
    private static final long serialVersionUID = 1927253892844132565L;

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
                                                                                  IOException {

        RequestToken reqToken = null;
        ResponseToken respToken = null;

        String userName = null;
        String password = null;
        HttpSession session = null;
        IdentityPassiveSTSClient passiveSTSClient = null;
        Map paramMap = req.getParameterMap();
        String frontEndUrl = null;

        frontEndUrl = getAdminConsoleURL(req);

        session = req.getSession();
        userName = (String) req.getParameter(PassiveRequestorConstants.USER_NAME);
        password = (String) req.getParameter(PassiveRequestorConstants.PASSWORD);

        if (userName == null || password == null) {
            session.setAttribute(PassiveRequestorConstants.ACTION, getAttribute(paramMap,
                                                                                PassiveRequestorConstants.ACTION));
            session.setAttribute(PassiveRequestorConstants.ATTRIBUTE, getAttribute(paramMap,
                                                                                   PassiveRequestorConstants.ATTRIBUTE));
            session.setAttribute(PassiveRequestorConstants.CONTEXT, getAttribute(paramMap,
                                                                                 PassiveRequestorConstants.CONTEXT));
            session.setAttribute(PassiveRequestorConstants.REPLY_TO, getAttribute(paramMap,
                                                                                  PassiveRequestorConstants.REPLY_TO));
            session.setAttribute(PassiveRequestorConstants.PSEUDO, getAttribute(paramMap,
                                                                                PassiveRequestorConstants.PSEUDO));
            session.setAttribute(PassiveRequestorConstants.REALM, getAttribute(paramMap,
                                                                               PassiveRequestorConstants.REALM));
            session.setAttribute(PassiveRequestorConstants.REQUEST, getAttribute(paramMap,
                                                                                 PassiveRequestorConstants.REQUEST));
            session.setAttribute(PassiveRequestorConstants.REQUEST_POINTER, getAttribute(paramMap,
                                                                                         PassiveRequestorConstants.REQUEST_POINTER));
            session.setAttribute(PassiveRequestorConstants.POLCY, getAttribute(paramMap,
                                                                               PassiveRequestorConstants.POLCY));
            resp.sendRedirect(frontEndUrl + "passive-sts/login.jsp");
            return;
        }

        paramMap = (Map) session.getAttribute(PassiveRequestorConstants.PASSIVE_REQ_ATTR_MAP);
        session.removeAttribute(PassiveRequestorConstants.PASSIVE_REQ_ATTR_MAP);

        reqToken = new RequestToken();
        reqToken.setAction((String) session.getAttribute(PassiveRequestorConstants.ACTION));
        reqToken.setAttributes((String) session.getAttribute(PassiveRequestorConstants.ATTRIBUTE));
        reqToken.setContext((String) session.getAttribute(PassiveRequestorConstants.CONTEXT));
        reqToken.setReplyTo((String) session.getAttribute(PassiveRequestorConstants.REPLY_TO));
        reqToken.setPseudo((String) session.getAttribute(PassiveRequestorConstants.PSEUDO));
        reqToken.setRealm((String) session.getAttribute(PassiveRequestorConstants.REALM));
        reqToken.setRequest((String) session.getAttribute(PassiveRequestorConstants.REQUEST));
        reqToken.setRequestPointer((String) session
                .getAttribute(PassiveRequestorConstants.REQUEST_POINTER));
        reqToken.setPolicy((String) session.getAttribute(PassiveRequestorConstants.POLCY));
        reqToken.setUserName(userName);
        reqToken.setPassword(password);

        String serverURL = CarbonUIUtil.getServerURL(session.getServletContext(), session);
        ConfigurationContext configContext = (ConfigurationContext) session.getServletContext()
                .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        passiveSTSClient = new IdentityPassiveSTSClient(serverURL, configContext);

        respToken = passiveSTSClient.getResponse(reqToken);

        if (respToken != null && respToken.getAuthenticated()) {
            sendData(req, resp, respToken, frontEndUrl, reqToken.getAction());
        } else {
            resp.sendRedirect(frontEndUrl + "passive-sts/login.jsp");
            return;
        }
    }

    private void sendData(HttpServletRequest httpReq, HttpServletResponse httpResp,
                          ResponseToken respToken, String frontEndUrl, String action)
            throws ServletException,
                   IOException {
        String page = null;
        HttpSession session = null;
        page = frontEndUrl + "passive-sts/redirect.jsp";

        session = httpReq.getSession();

        respToken.setResults(respToken.getResults().replace("<", "&lt;"));
        respToken.setResults(respToken.getResults().replace(">", "&gt;"));
        respToken.setResults(respToken.getResults().replace("\"", "'"));

        // HTML FORM Redirection
        session.setAttribute("replyTo", respToken.getReplyTo());
        session.setAttribute("results", respToken.getResults());
        session.setAttribute("context", respToken.getContext());
        session.setAttribute("action", action);
        httpResp.sendRedirect(page);
    }

    private String getAttribute(Map paramMap, String name) {
        if (paramMap.get(name) != null && paramMap.get(name) instanceof String[]) {
            return ((String[]) paramMap.get(name))[0];
        }
        return null;
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        doGet(req, resp);
    }

    private String getAdminConsoleURL(HttpServletRequest request) {
        String url = CarbonUIUtil.getAdminConsoleURL(request);
        if (url.indexOf("/passivests/") != -1) {
            url = url.replace("/passivests", "");
        }
        return url;
    }

}
