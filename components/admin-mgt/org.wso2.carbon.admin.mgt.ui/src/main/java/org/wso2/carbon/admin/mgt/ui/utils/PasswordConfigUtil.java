/*
 *  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.admin.mgt.ui.utils;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.admin.mgt.ui.clients.AdminManagementClient;
import org.wso2.carbon.registry.common.ui.UIException;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.wso2.carbon.admin.mgt.stub.beans.xsd.CaptchaInfoBean;
import org.wso2.carbon.admin.mgt.stub.beans.xsd.AdminMgtInfoBean;

import java.io.StringReader;
import java.util.Iterator;

/**
 * Password Configuration Utility client methods.
 */
public class PasswordConfigUtil {
    private static final Log log = LogFactory.getLog(PasswordConfigUtil.class);


    /**
     * Tenant Sends a Reset Password Request
     *
     * @param request HttpServletRequest
     * @param config  ServletConfig
     * @param session HttpSession
     * @return true if reset password
     * @throws UIException if failed to reset the password
     */
    public static boolean sendResetPasswordLink(HttpServletRequest request, ServletConfig config,
                                        HttpSession session) throws UIException {
        String admin = "";
        String domain = "";
        try {
            // filling tenant info.
            admin = request.getParameter("admin");
            domain = resolveDomainName(request.getParameter("domain"));
            if (admin.trim().equals("")) {
                String msg = "Provided user name is empty";
                log.error(msg);
                return false;
            }

            AdminMgtInfoBean adminInfoBean = new AdminMgtInfoBean();
            adminInfoBean.setAdmin(admin);
            adminInfoBean.setTenantDomain(domain);
            CaptchaInfoBean captchaInfoBean = new CaptchaInfoBean();

            // filling captcha info
            captchaInfoBean.setSecretKey(request.getParameter("captcha-secret-key"));
            captchaInfoBean.setUserAnswer(request.getParameter("captcha-user-answer"));

            AdminManagementClient adminManagementClient =
                    new AdminManagementClient(config, session);
            return adminManagementClient.sendResetPasswordLink(adminInfoBean, captchaInfoBean);
        } catch (Exception e) {
            AxisFault fault = new AxisFault(e.getMessage());
            String msg = fault.getReason() + " Failed to reset password. tenant-domain: " + domain +
                         " admin: " + admin;
            log.error(msg, e);
            // we are preventing more details going ahead further to user.
            throw new UIException(e.getMessage(), e);
        }
    }

    /**
     * Updating the tenant admin password with the new password provided by the user.
     *
     * @param request HttpServletRequest
     * @param config  ServletConfig
     * @param session HttpSession
     * @return if password successfully reset.
     * @throws UIException if password update failed
     */
    public static boolean updateAdminPasswordWithUserInput(HttpServletRequest request,
                                                           ServletConfig config,
                                                           HttpSession session) throws UIException {
        String domain = request.getParameter("domain");
        String adminName = request.getParameter("admin");
        String password = request.getParameter("admin-password");
        AdminMgtInfoBean adminInfoBean = new AdminMgtInfoBean();

        adminInfoBean.setTenantDomain(domain);
        adminInfoBean.setAdmin(adminName);
        adminInfoBean.setAdminPassword(password);
        CaptchaInfoBean captchaInfoBean = new CaptchaInfoBean();

        try {
            // filling captcha info
            captchaInfoBean.setSecretKey(request.getParameter("captcha-secret-key"));
            captchaInfoBean.setUserAnswer(request.getParameter("captcha-user-answer"));

            AdminManagementClient adminManagementClient =
                    new AdminManagementClient(config, session);
            return adminManagementClient.updateAdminPasswordWithUserInput(
                    adminInfoBean, captchaInfoBean);
        } catch (Exception e) {
            AxisFault fault = new AxisFault(e.getMessage());
            String msg = fault.getReason() + " Failed to update password. tenant-domain: " + domain;
            log.error(msg, e);
            // we are preserving the original message.
            throw new UIException(msg, e);
        }
    }
    /**
     * Resolves the correct domain name in the form of example.com from the user input domain name.
     * Currently strips out "www."and white space. Can add more checks.
     *
     * @param domainNameUserInput the user input domain name
     * @return the domain after removing (if entered) www. from the input.
     */
    public static String resolveDomainName(String domainNameUserInput) {
        if (domainNameUserInput == null) {
            String msg = "Provided domain name is null";
            log.error(msg);
            return "";
        }
        String domainName = domainNameUserInput.trim();
        if (domainName.startsWith("www.")) {
            domainName = domainName.substring(4);
        }
        return domainName;
    }

    /**
     * Sets attributes to the HttpServletRequest
     * @param request HttpServletRequest
     * @param data : String
     * @return request: HttpServletRequest.
     */
    public static HttpServletRequest readIntermediateData(HttpServletRequest request, String data) {
        try {
            XMLStreamReader parser =
                    XMLInputFactory.newInstance().createXMLStreamReader(new StringReader(data));
            StAXOMBuilder builder = new StAXOMBuilder(parser);
            OMElement documentElement = builder.getDocumentElement();
            Iterator it = documentElement.getChildElements();
            while (it.hasNext()) {
                OMElement element = (OMElement) it.next();
                if ("admin".equals(element.getLocalName())) {
                    request.setAttribute("admin", element.getText());
                } else if ("firstname".equals(element.getText())) {
                    request.setAttribute("firstname", element.getText());
                } else if ("lastname".equals(element.getText())) {
                    request.setAttribute("lastname", element.getText());
                } else if ("email".equals(element.getLocalName())) {
                    request.setAttribute("email", element.getText());
                } else if ("tenantDomain".equals(element.getLocalName())) {
                    request.setAttribute("tenantDomain", element.getText());
                } else if ("confirmationKey".equals(element.getLocalName())) {
                    request.setAttribute("confirmationKey", element.getText());
                }
            }
        } catch (Exception e) {
            log.error("Error parsing xml", e);
        }
        return request;
    }


    /**
     * Generates a random captcha
     *
     * @param config  ServletConfig
     * @param session HttpSession
     * @return CaptchaInfoBean
     * @throws UIException, if generating the random captcha fails.
     */
    public static CaptchaInfoBean generateRandomCaptcha(ServletConfig config,
                                                        HttpSession session) throws UIException {
        try {
            AdminManagementClient selfRegistrationClient =
                    new AdminManagementClient(config, session);
            return selfRegistrationClient.generateRandomCaptcha();
        } catch (Exception e) {
            String msg = "Error in generating the captcha image.";
            log.error(msg, e);
            throw new UIException(msg, e);
        }
    }
}
