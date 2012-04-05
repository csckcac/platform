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
package org.wso2.carbon.upgrade.ui.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.common.ui.UIException;
import org.wso2.carbon.upgrade.stub.beans.xsd.PackageInfoBean;
import org.wso2.carbon.upgrade.stub.beans.xsd.SubscriptionInfoBean;
import org.wso2.carbon.upgrade.ui.clients.UpgradeServiceClient;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpSession;

public class UpgradeUtil {
    private static final Log log = LogFactory.getLog(UpgradeUtil.class);

    public static PackageInfoBean[] getPackageInfo(
            ServletConfig config, HttpSession session) throws UIException {

        try {
            UpgradeServiceClient serviceClient = new UpgradeServiceClient(config, session);
            return serviceClient.getPackageInfo();
        } catch (Exception e) {
            String msg = "Failed to get the package information.";
            log.error(msg, e);
            throw new UIException(msg, e);
        }
    }


    public static SubscriptionInfoBean getCurrentSubscription(
            ServletConfig config, HttpSession session) throws UIException {

        try {
            UpgradeServiceClient serviceClient = new UpgradeServiceClient(config, session);
            return serviceClient.getCurrentSubscription();
        } catch (Exception e) {
            String msg = "Failed to get the current subscription.";
            log.error(msg, e);
            throw new UIException(msg, e);
        }
    }

    public static void updateSubscription(
            ServletConfig config, HttpSession session) throws UIException {
        try {
            String packageName = (String)session.getAttribute("packageName");
            String duration = (String)session.getAttribute("duration");

            UpgradeServiceClient serviceClient = new UpgradeServiceClient(config, session);
            serviceClient.updateSubscription(packageName, duration);
        } catch (Exception e) {
            String msg = "Failed to update the subscription.";
            log.error(msg, e);
            throw new UIException(msg, e);
        }
    }

    public static void cancelSubscription(
            ServletConfig config, HttpSession session) throws UIException {

        try {
            UpgradeServiceClient serviceClient = new UpgradeServiceClient(config, session);
            serviceClient.cancelSubscription();
        } catch (Exception e) {
            String msg = "Failed to cancel the current subscription.";
            log.error(msg, e);
            throw new UIException(msg, e);
        }
    }
}
