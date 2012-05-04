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

package org.wso2.carbon.mashup.request.processors.gadget;

import org.apache.axiom.om.OMException;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.protocol.HTTP;
import org.wso2.carbon.core.multitenancy.utils.TenantAxisUtils;
import org.wso2.carbon.core.transports.CarbonHttpRequest;
import org.wso2.carbon.core.transports.CarbonHttpResponse;
import org.wso2.carbon.core.transports.HttpGetRequestProcessor;
import org.wso2.carbon.wsdl2form.Util;

import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class GadgetRequestProcessor implements HttpGetRequestProcessor {
    private static Log log = LogFactory.getLog(GadgetRequestProcessor.class);
    public static final String GADGET_XSL_LOCATION = "org/wso2/carbon/mashup/request/processors/gadget/xslt/gadget.xslt";

    public void process(CarbonHttpRequest request, CarbonHttpResponse response, ConfigurationContext configurationContext) throws Exception {
        try {
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

            if (axisService == null) {
                response.addHeader(HTTP.CONTENT_TYPE, "text/html");
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                outputStream
                        .write(("<h4>Service cannot be found. Cannot display <em>Gadget</em>.</h4>").getBytes());
                outputStream.flush();
                return;
            }

            if (!axisService.isActive()) {
                response.addHeader(HTTP.CONTENT_TYPE, "text/html");
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                outputStream
                        .write(("<h4>Service " + serviceName +
                                " is inactive. Cannot display <em>Gadget</em>.</h4>")
                                .getBytes());
                outputStream.flush();
                return;
            }

            DOMSource xmlSource = Util.getSigStream(axisService, null);
            Result result = new StreamResult(response.getOutputStream());
            Map<String, String> paramMap = new HashMap<String, String>();
            paramMap.put("wsrequest-location", "../../carbon/admin/js/WSRequest.js");
            paramMap.put("stub-location", "?stub");

            generateGadget(xmlSource, result, paramMap);

        } catch (OMException e) {
            log.error(e);
        } catch (Exception e) {
            log.error(e);
        }
    }

    private void generateGadget(Source xmlIn, Result result, Map paramMap)
            throws TransformerException {
        InputStream gadgetXSLTStream =
                GadgetRequestProcessor.class.getClassLoader().getResourceAsStream(GADGET_XSL_LOCATION);
        Source gadgetXSLSource = new StreamSource(gadgetXSLTStream);
        Util.transform(xmlIn, gadgetXSLSource, result, paramMap, new GadgetURIResolver());
    }

    private class GadgetURIResolver implements URIResolver {

        public Source resolve(String href, String base) {
            InputStream is = Util.class.getResourceAsStream(href);
            return new StreamSource(is);
        }
    }
}

