/*
 *  Copyright (c) 2005-2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.stratos.gadget.login.ui.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.common.ui.UIException;
import org.wso2.stratos.gadget.login.stub.beans.xsd.TenantInfoBean;
import org.wso2.stratos.gadget.login.ui.clients.TenantServiceClient;

import javax.servlet.http.HttpSession;
import javax.servlet.ServletConfig;


public class TenantConfigUtil {
    private static final Log log = LogFactory.getLog(TenantConfigUtil.class);


    public static boolean checkDomainAvaialability(
            String domain, ServletConfig config, HttpSession session) throws UIException {
        try {
            TenantServiceClient serviceClient = new TenantServiceClient(config, session);
            return serviceClient.checkDomainAvailability(domain);
        } catch (Exception e) {
            String msg = "Failed to check the domain availability:" + domain + ".";
            log.error(msg, e);
            throw new UIException(msg, e);
        }
    }

    public static boolean isDomainRegistered (
            String domain, ServletConfig config, HttpSession session) throws UIException {
        return !checkDomainAvaialability(domain, config, session);
    }
}
