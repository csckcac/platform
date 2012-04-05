/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.bam.core.clients;

import org.apache.axis2.client.Stub;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;
import org.wso2.carbon.bam.core.util.BAMUtil;

/**
 * Provides common cleanup services to all the admin service clients.
 * @param <T> The stub type
 */
public abstract class AbstractAdminClient<T extends Stub> {
    private static Log log = LogFactory.getLog(AbstractAdminClient.class);
    protected T stub;

    public static String generateURL(String[] components) {
        return BAMUtil.generateURL(components);
    }

    public void setSessionCookie(String cookie) {
        stub._getServiceClient().getOptions().setProperty(HTTPConstants.COOKIE_STRING, cookie);
    }

    public void cleanup() {
        try {
            stub._getServiceClient().cleanupTransport();
            stub._getServiceClient().cleanup();
            stub.cleanup();
        } catch (AxisFault axisFault) {
            if (log.isErrorEnabled()) {
                log.error("Stub cleanup failed: " + this.getClass().getName(), axisFault);
            }
        }
    }
}
