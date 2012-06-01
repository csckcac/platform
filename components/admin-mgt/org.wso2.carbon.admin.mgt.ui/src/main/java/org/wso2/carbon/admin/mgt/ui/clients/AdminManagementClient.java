/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.admin.mgt.ui.clients;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.admin.mgt.stub.AdminManagementServiceAdminManagementExceptionException;
import org.wso2.carbon.admin.mgt.stub.AdminManagementServiceStub;
import org.wso2.carbon.admin.mgt.stub.beans.xsd.AdminMgtInfoBean;
import org.wso2.carbon.admin.mgt.stub.beans.xsd.CaptchaInfoBean;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.utils.ServerConstants;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpSession;
import java.rmi.RemoteException;

/**
 * Admin Management Client class
 */
public class AdminManagementClient {
    private static final Log log = LogFactory.getLog(AdminManagementClient.class);

    private AdminManagementServiceStub stub;
    private String epr;

    public AdminManagementClient(
            String cookie, String backendServerURL, ConfigurationContext configContext)
            throws RegistryException {

        epr = backendServerURL + "AdminManagementService";

        try {
            stub = new AdminManagementServiceStub(configContext, epr);

            ServiceClient client = stub._getServiceClient();
            Options option = client.getOptions();
            option.setManageSession(true);
            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);

        } catch (AxisFault axisFault) {
            String msg = "Failed to initiate AddServices service client. " + axisFault.getMessage();
            log.error(msg, axisFault);
            throw new RegistryException(msg, axisFault);
        }
    }

    public AdminManagementClient(ServletConfig config, HttpSession session)
            throws RegistryException {

        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext = (ConfigurationContext) config.
                getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        epr = backendServerURL + "AdminManagementService";

        try {
            stub = new AdminManagementServiceStub(configContext, epr);

            ServiceClient client = stub._getServiceClient();
            Options option = client.getOptions();
            option.setManageSession(true);
            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);

        } catch (AxisFault axisFault) {
            String msg = "Failed to initiate Add Services service client. " + axisFault.getMessage();
            log.error(msg, axisFault);
            throw new RegistryException(msg, axisFault);
        }
    }

    /**
     * Updates the admin password with the user input
     *
     * @param adminInfoBean   AdminMgtInfoBean
     * @param captchaInfoBean CaptchaInfoBean
     * @param confirmationKey  key to confirm the password reset request.
     * @return true, if update password was successful.
     * @throws java.rmi.RemoteException, axis2 exception
     * @throws AdminManagementServiceAdminManagementExceptionException AdminManagementException
     */
    public boolean updatePasswordWithUserInput(
            AdminMgtInfoBean adminInfoBean, CaptchaInfoBean captchaInfoBean,
            String confirmationKey) throws AdminManagementServiceAdminManagementExceptionException,
            RemoteException {
        return stub.updatePasswordWithUserInput(adminInfoBean, captchaInfoBean,
                confirmationKey);
    }

    /**
     * User calls the reset password method
     *
     * @param adminInfoBean   AdminMgtInfoBean
     * @param captchaInfoBean CaptchaInfoBean
     * @return true/false
     * @throws Exception, if sending the link failed
     */
    public boolean initiatePasswordReset(
            AdminMgtInfoBean adminInfoBean, CaptchaInfoBean captchaInfoBean) throws Exception {
        return stub.initiatePasswordReset(adminInfoBean, captchaInfoBean);
    }

    /**
     * Generates Random Captcha
     * @return CaptchaInfoBean object
     * @throws Exception, if the captcha generation failed.
     */
    public CaptchaInfoBean generateRandomCaptcha() throws Exception {
        return stub.generateRandomCaptcha();
    }

}
