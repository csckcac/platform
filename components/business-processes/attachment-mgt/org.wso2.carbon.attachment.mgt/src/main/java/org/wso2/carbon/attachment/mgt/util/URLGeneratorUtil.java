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
import org.wso2.carbon.base.CarbonBaseUtils;
import org.wso2.carbon.base.CarbonContextHolderBase;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.NetworkUtils;

import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;

/**
 * Logic relevant to URL generation. This URL will be used by outsiders to access the attachment.
 */
public class URLGeneratorUtil {
    /**
     * Logger class
     */
    private static Log log = LogFactory.getLog(URLGeneratorUtil.class);

    /**
     * URL generation logic
     * @return string value of URL
     */
    public static String getURL() {

        log.warn("URL generation is not still implemented...");

        String scheme = "http";
        try {
            String host = NetworkUtils.getLocalHostname();
        } catch (SocketException e) {
            log.error(e.getMessage(), e);
        }

        String url = null;
        try {
            url = new URL("http://wso2.org/bps").toString();
        } catch (MalformedURLException e) {
            log.error(e.getMessage(), e);
        }

        return url;
    }
}
