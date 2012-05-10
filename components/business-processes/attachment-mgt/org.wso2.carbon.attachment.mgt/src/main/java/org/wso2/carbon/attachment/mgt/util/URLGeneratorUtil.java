/*
 * Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.attachment.mgt.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.attachment.mgt.core.exceptions.AttachmentMgtException;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.core.multitenancy.SuperTenantCarbonContext;
import org.wso2.carbon.utils.NetworkUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URI;
import java.net.URL;
import java.security.SecureRandom;

/**
 * Logic relevant to URL generation. This URL will be used by outsiders to access the attachment.
 */
public class URLGeneratorUtil {
    /**
     * Logger class
     */
    private static Log log = LogFactory.getLog(URLGeneratorUtil.class);

    private static final SecureRandom random = new SecureRandom();

    /**
     * URL generation logic
     *
     * @return string value of URL
     */
    public static String generateURL() throws AttachmentMgtException {

        /*log.warn("URL generation is not still implemented...");

        String scheme = CarbonConstants.HTTPS_TRANSPORT;
        String host;
        try {
            host = NetworkUtils.getLocalHostname();
        } catch (SocketException e) {
            log.error(e.getMessage(), e);
            throw new AttachmentMgtException(e.getLocalizedMessage(), e);
        }

        log.warn("Port is hardcoded.");
        int port = 9443;

        String webContext = ServerConfiguration.getInstance().getFirstProperty("WebContextRoot");
        if (webContext == null || webContext.equals("/")) {
            webContext = "";
        }

        String tenantDomain = SuperTenantCarbonContext.getCurrentContext().getTenantDomain(true);

        String url = null;
        try {
            String link = scheme + "://" + host + ":" + port + webContext + ((tenantDomain != null) ? "/" +
                                                                                                      MultitenantConstants.TENANT_AWARE_URL_PREFIX + "/" + tenantDomain : "") +
                          "/registry/resource" + "dummyPath";
            url = new URL(link).toString();
        } catch (MalformedURLException e) {
            log.error(e.getMessage(), e);
        }
        */

        return generateUniqueID();
    }

    /**
     * Generate a unique string required for URL generation
     *
     * @return a unique string
     */
    private static String generateUniqueID() {
        return new BigInteger(130, random).toString(32);
    }

    /**
     * Generate the permanent link for the given attachment uri based on current system configurations like host, port
     * eg - if {@code uniqueID} is abc123, then the resultant permanent link would {@code https://127.0.0.1:9443/context/abc123}
     * So this url can be used to download the attachment
     *
     * @param uniqueID uri for the attachment
     * @return downloadable url of the attachment
     * @throws AttachmentMgtException
     */
    public static URL getPermanentLink(URI uniqueID) throws AttachmentMgtException {
        String scheme = CarbonConstants.HTTPS_TRANSPORT;
        String host;
        try {
            host = NetworkUtils.getLocalHostname();
        } catch (SocketException e) {
            log.error(e.getMessage(), e);
            throw new AttachmentMgtException(e.getLocalizedMessage(), e);
        }

        log.warn("Port is hardcoded.");
        int port = 9443;
        /*try {

            port = CarbonUtils.getTransportProxyPort(myConfigContext, scheme);
            if (port == -1) {
                port = CarbonUtils.getTransportPort(myConfigContext, scheme);
            }
        } catch (AxisFault axisFault) {
            log.error(axisFault.getLocalizedMessage(), axisFault);
        }*/

        String webContext = ServerConfiguration.getInstance().getFirstProperty("WebContextRoot");
        if (webContext == null || webContext.equals("/")) {
            webContext = "";
        }

        String tenantDomain = String.valueOf(MultitenantConstants.SUPER_TENANT_ID);
        try {
            tenantDomain = SuperTenantCarbonContext.getCurrentContext().getTenantDomain(true);
        } catch (Throwable e) {
            tenantDomain = null;
        }

        String url = null;
        try {
            String link = scheme + "://" + host + ":" + port + webContext + ((tenantDomain != null) ? "/" +
                                              MultitenantConstants.TENANT_AWARE_URL_PREFIX + "/" + tenantDomain : "") +
                          "/attachment-mgt/download" + "/" + uniqueID.toString();
            return new URL(link);
        } catch (MalformedURLException e) {
            log.error(e.getMessage(), e);
            throw new AttachmentMgtException(e.getLocalizedMessage(), e);
        }
    }
}