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

import org.wso2.carbon.utils.FileUtil;
import org.wso2.carbon.utils.CarbonUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.abdera.protocol.client.AbderaClient;
import org.apache.abdera.protocol.client.ClientResponse;
import org.apache.abdera.protocol.Response;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Entry;
import org.apache.abdera.Abdera;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.servlet.http.HttpSession;
import javax.xml.namespace.QName;
import java.util.List;

public class Util {
    private static Log log = LogFactory.getLog(UIAnnouncementImpl.class);
    // the domain format
    private static final String NOTIFICATION_HOST_URL_PATTERN_DEFAULT =
       "http://wso2.com/announcements/product-banner/index.html?id={instance.id}&amp;product={product}&amp;build={build}";
    private static String hostUrlPattern = null;
    private static final String INSTANCE_ID_REPLACER = "\\{instance.id\\}";
    private static final String PRODUCT_NAME_REPLACER = "\\{product\\}";
    private static final String BUILD_NUMBER_REPLACER = "\\{build\\}";
    private static final String TENANT_REPLACER = "\\{tenant\\}";

    private static final String ATOM_NAMESPACE = "http://www.w3.org/2005/Atom";

    public static String generateAnnouncementURL(String regUuid, HttpSession session) {
        AnnouncementsConfiguration announcementConfig = AnnouncementsConfiguration.getAnnouncementsConfiguration();
        String isEnableVal = announcementConfig.getValue("Announcements.enabled");
        boolean isEnable = isEnableVal == null || isEnableVal.equals("true");
        if (!isEnable) {
            return "";
        }

        if (hostUrlPattern == null) {
            // getting the starting pattern from the carbon.xml
            hostUrlPattern = announcementConfig.getValue("Announcements.host");
            if (hostUrlPattern == null || hostUrlPattern.equals("")) {
                hostUrlPattern = NOTIFICATION_HOST_URL_PATTERN_DEFAULT;
            }

            String carbonHome = CarbonUtils.getCarbonHome();
            String versionFilename = carbonHome + "/bin/version.txt";

            String productInfo;
            try {
                productInfo = FileUtil.readFileToString(versionFilename);
            } catch (Exception e) {
                // the failure in add will not thrown exception
                return "";
            }
            int lastSpaceId = productInfo.lastIndexOf(' ');
            // prepare the product name
            String productName = productInfo.substring(0, lastSpaceId);
            productName = productName.toLowerCase();
            productName = productName.replaceAll("wso2", "");
            productName = productName.trim();
            productName = productName.replaceAll(" ", "-");
            productName = productName.toLowerCase();

            // prepare the version
            String buildNumber = productInfo.substring(lastSpaceId + 1);
            buildNumber = buildNumber.trim();


            // get the carbon xml configurations
            hostUrlPattern = hostUrlPattern.replaceAll(INSTANCE_ID_REPLACER, regUuid).
                        replaceAll(PRODUCT_NAME_REPLACER, productName).
                        replaceAll(BUILD_NUMBER_REPLACER, buildNumber);
        }
        // get the ad content from the cache
        String currentTenantDomain =
                (String) session.getAttribute(MultitenantConstants.TENANT_DOMAIN);
        if (currentTenantDomain == null || currentTenantDomain.equals("")) {
            // this is a reserved domain
            currentTenantDomain = MultitenantConstants.SUPER_TENANT_NAME;
        }
        String notificationUrl = hostUrlPattern.replaceAll(TENANT_REPLACER, currentTenantDomain);

        // this should be changed to log debug, once the testing is done
        if (log.isDebugEnabled()) {
            log.debug("Announcement url: " + notificationUrl);
        }
        return notificationUrl;
    }

    public static String getAnnouncementHtml(String url) {
        Abdera abdera = new Abdera();
        AbderaClient client = new AbderaClient(abdera);
        ClientResponse resp = client.get(url);
        Feed feed;
        if (resp.getType() == Response.ResponseType.SUCCESS) {
            Document<Feed> respDoc = resp.getDocument();
            feed = respDoc.getRoot();
        } else {
            // there was an error
            log.error("The request to the url: " + url + " failed.");
            return "";
        }
        // get the feed entries
        List<Entry> entries = feed.getEntries();
        if (entries.size() == 0) {
            return "";
        }
        // recover only the first entry for the time being
        Entry entry = entries.get(0);
        String iconUrl = entry.getSimpleExtension(new QName(ATOM_NAMESPACE, "icon"));
        String link = (entry.getLinks().size() == 0)? null: entry.getLinks().get(0).getHref().toString();

        String html = "";
        if (link != null) {
            html += "<a href=\"" + link + "\">";
        }
        if (iconUrl != null) {
            html += "<img src=\"" + iconUrl + "\"/>";
        }
        if (link != null) {
            html += "</a>";
        }
        return html;
    }
}
