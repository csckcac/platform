/**
 *  Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.transport.passthru.util;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpVersion;
import org.wso2.carbon.transport.passthru.PassThroughConstants;
import org.wso2.carbon.transport.passthru.TargetRequest;
import org.wso2.carbon.transport.passthru.config.TargetConfiguration;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

public class TargetRequestFactory {
    private static Log log = LogFactory.getLog(TargetRequestFactory.class);

    public static TargetRequest create(MessageContext msgContext,
                                       TargetConfiguration configuration) throws AxisFault {
        try {
            String httpMethod = (String) msgContext.getProperty(
                    Constants.Configuration.HTTP_METHOD);
            if (httpMethod == null) {
                httpMethod = "POST";
            }

            // basic request
            Boolean noEntityBody = (Boolean) msgContext.getProperty(PassThroughConstants.NO_ENTITY_BODY);

            EndpointReference epr = PassThroughTransportUtils.getDestinationEPR(msgContext);
            URL url = new URL(epr.getAddress());
            TargetRequest request = new TargetRequest(configuration, url, httpMethod,
                    noEntityBody == null || !noEntityBody);

            // headers
            PassThroughTransportUtils.removeUnwantedHeaders(msgContext,
                    configuration.isPreserveServerHeader(),
                    configuration.isPreserveUserAgentHeader());


            Object o = msgContext.getProperty(MessageContext.TRANSPORT_HEADERS);
            if (o != null && o instanceof Map) {
                Map headers = (Map) o;

                for (Object entryObj : headers.entrySet()) {
                    Map.Entry entry = (Map.Entry) entryObj;
                    if (entry.getValue() != null && entry.getKey() instanceof String &&
                            entry.getValue() instanceof String) {
                        if (!HTTPConstants.HEADER_HOST.equalsIgnoreCase((String) entry.getKey())) {
                            request.addHeader((String) entry.getKey(), (String) entry.getValue());
                        }
                    }
                }
            }

            // version
            String forceHttp10 = (String) msgContext.getProperty(PassThroughConstants.FORCE_HTTP_1_0);
            if ("true".equals(forceHttp10)) {
                request.setVersion(HttpVersion.HTTP_1_0);
            }

            // keep alive
            String noKeepAlie = (String) msgContext.getProperty(PassThroughConstants.NO_KEEPALIVE);
            if ("true".equals(noKeepAlie)) {
                request.setKeepAlive(false);
            }

            // port
            int port = url.getPort();
            request.setPort(port != -1 ? port : 80);

            // chunk
            String disableChunking = (String) msgContext.getProperty(
                    PassThroughConstants.DISABLE_CHUNKING);
            if ("true".equals(disableChunking)) {
                request.setChunk(false);
            }

            // full url
            String fullUrl = (String) msgContext.getProperty(PassThroughConstants.FULL_URI);
            if ("true".equals(fullUrl)) {
                request.setFullUrl(true);                
            }

            return request;
        } catch (MalformedURLException e) {
            handleException("Invalid to address" + msgContext.getTo().getAddress(), e);
        }

        return null;
    }

    /**
     * Throws an AxisFault if an error occurs at this level
     * @param s a message describing the error
     * @param e original exception leads to the error condition
     * @throws org.apache.axis2.AxisFault wrapping the original exception
     */
    private static void handleException(String s, Exception e) throws AxisFault {
        log.error(s, e);
        throw new AxisFault(s, e);
    }
}
