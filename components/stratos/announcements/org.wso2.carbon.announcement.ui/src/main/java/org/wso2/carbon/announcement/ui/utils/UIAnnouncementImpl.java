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
package org.wso2.carbon.announcement.ui.utils;

import org.wso2.carbon.ui.UIAnnouncement;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.announcement.ui.clients.AnnouncementServiceClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpSession;
import javax.servlet.ServletConfig;

public class UIAnnouncementImpl implements UIAnnouncement {

    private static final String NOTIFIER_HTML_KEY = "announcement-html-key";
    private static final String NOTIFIER_TENANT_KEY = "announcement-tenant-key";
    private static Log log = LogFactory.getLog(UIAnnouncementImpl.class);

    public String getAnnouncementHtml(HttpSession session, ServletConfig config) {
        String notificationHtml = (String)session.getAttribute(NOTIFIER_HTML_KEY);
        if (notificationHtml == null || isSessionChanged(session)) {
            try {
                AnnouncementServiceClient client = new AnnouncementServiceClient(config, session);
                String regId = client.retrieveRegId();
                String notificationUrl = Util.generateAnnouncementURL(regId, session);

                notificationHtml = Util.getAnnouncementHtml(notificationUrl);
                session.setAttribute(NOTIFIER_HTML_KEY, notificationHtml);
            } catch (Exception e) {
                // I really don't want to crash the page, because the notification service didn't work
                // I rather show an log and continue the operation by returning an HTML
                log.error(e);
                return "";
            }
        }
        return notificationHtml;
    }

    // to check whether the session changed to not-loggedin state to login state or vice versa 
    private boolean isSessionChanged(HttpSession session) {
        String currentTenantDomain =
                (String) session.getAttribute(MultitenantConstants.TENANT_DOMAIN);
        if (currentTenantDomain == null || currentTenantDomain.equals("")) {
            // this is a reserved domain
            currentTenantDomain = MultitenantConstants.SUPER_TENANT_NAME;
        }
        return currentTenantDomain.equals(session.getAttribute(NOTIFIER_TENANT_KEY));
    }
}
