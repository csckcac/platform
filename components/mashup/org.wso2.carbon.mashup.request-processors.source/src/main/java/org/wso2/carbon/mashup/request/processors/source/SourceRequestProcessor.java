/*
 * Copyright 2006,2007 WSO2, Inc. http://www.wso2.org
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
package org.wso2.carbon.mashup.request.processors.source;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.protocol.HTTP;
import org.wso2.carbon.core.multitenancy.utils.TenantAxisUtils;
import org.wso2.carbon.core.transports.CarbonHttpRequest;
import org.wso2.carbon.core.transports.CarbonHttpResponse;
import org.wso2.carbon.core.transports.HttpGetRequestProcessor;
import org.wso2.carbon.mashup.utils.MashupConstants;
import org.wso2.carbon.mashup.utils.MashupUtils;
import org.wso2.carbon.utils.ServerConstants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.OutputStream;

public class SourceRequestProcessor implements HttpGetRequestProcessor {

    private static Log log = LogFactory.getLog(SourceRequestProcessor.class);

    /* (non-Javadoc)
     * @see org.wso2.wsas.transport.HttpGetRequestProcessor#process(org.wso2.carbon.core.transports.CarbonHttpRequest, org.wso2.carbon.core.transports.CarbonHttpResponse, org.apache.axis2.context.ConfigurationContext)
     */
    public void process(CarbonHttpRequest request, CarbonHttpResponse response,
                        ConfigurationContext configurationContext) throws Exception {
        String requestURI = request.getRequestURI();
        String contextPath = configurationContext.getServiceContextPath();
        String serviceName = requestURI.substring(requestURI.indexOf(contextPath) +
                                                  contextPath.length() + 1);
        AxisConfiguration configuration = configurationContext.getAxisConfiguration();
        AxisService axisService = configuration.getServiceForActivation(serviceName);
        if (axisService == null) {
            // Try to see whether the service is available in a tenant
            axisService = TenantAxisUtils.getAxisService(serviceName, configurationContext);
        }
        OutputStream outputStream = response.getOutputStream();

        if (axisService != null) {
            if (axisService.isActive()) {

                String serviceType =
                        (String) axisService.getParameterValue(ServerConstants.SERVICE_TYPE);

                // We can show source only if this service is a javascript service
                if (MashupConstants.JS_SERVICE.equals(serviceType)) {
                    String contentTypeParameter =
                            request.getParameter(MashupConstants.CONTENT_TYPE_QUERY_PARAM);
                    String contentType = "application/javascript";
                    if (contentTypeParameter != null && !"".equals(contentTypeParameter.trim())) {
                        contentType = contentTypeParameter;
                    }
                    response.addHeader(HTTP.CONTENT_TYPE, contentType + "; charset=utf-8");
                    String serviceJSFile = (String) axisService
                            .getParameterValue(MashupConstants.SERVICE_JS);
                    if (serviceJSFile != null) {
                        File file = new File(serviceJSFile);
                        MashupUtils.writeFile(response, file);
                    } else {
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        response.addHeader(HTTP.CONTENT_TYPE, "text/html");
                        outputStream
                                .write(("<h4>Source is not available for this service.")
                                        .getBytes());
                        outputStream.flush();
                    }
                } else if (ServerConstants.SERVICE_TYPE_DB.equals(serviceType)) {
                    String contentTypeParameter =
                            request.getParameter(MashupConstants.CONTENT_TYPE_QUERY_PARAM);
                    String contentType = "text/plain";
                    if (contentTypeParameter != null && !"".equals(contentTypeParameter.trim())) {
                        contentType = contentTypeParameter;
                    }
                    response.addHeader(HTTP.CONTENT_TYPE, contentType + "; charset=utf-8");
                    Parameter implInfoParam = axisService
                            .getParameter("data_service");
                    if (implInfoParam != null && implInfoParam.getValue() instanceof String) {
                        String filePath = (String) implInfoParam.getValue();
                        File file = new File(filePath);
                        if (file.exists()) {
                            MashupUtils.writeFile(response, file);
                        } else {
                            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                            response.addHeader(HTTP.CONTENT_TYPE, "text/html");
                            outputStream
                                    .write(("<h4>Cannot find source file for Data Service").getBytes());
                            outputStream.flush();
                        }
                    } else {
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        response.addHeader(HTTP.CONTENT_TYPE, "text/html");
                        outputStream
                                .write(("<h4>Source is not available for this service.")
                                        .getBytes());
                        outputStream.flush();
                    }
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    response.addHeader(HTTP.CONTENT_TYPE, "text/html");
                    outputStream
                            .write(("<h4>Source is not available for this service. The source view " +
                                    "is avalable only for JavaScript services")
                                    .getBytes());
                    outputStream.flush();
                }
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.addHeader(HTTP.CONTENT_TYPE, "text/html");
                outputStream
                        .write(("<h4>Source is not available for this service. This service " +
                                "is not active").getBytes());
                outputStream.flush();
            }

        } else {
            if (log.isDebugEnabled()) {
                log.debug("Cannot find the service" + serviceName + ". Axis Service is null.");
            }
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.addHeader(HTTP.CONTENT_TYPE, "text/html");
            outputStream
                    .write(("<h4>Service cannot be found. Cannot display <em>Source</em>.</h4>")
                            .getBytes());
            outputStream.flush();
        }
    }
}
