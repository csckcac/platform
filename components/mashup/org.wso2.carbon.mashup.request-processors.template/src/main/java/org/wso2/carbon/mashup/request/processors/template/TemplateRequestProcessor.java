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
package org.wso2.carbon.mashup.request.processors.template;

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
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;
import java.io.OutputStream;

public class TemplateRequestProcessor implements HttpGetRequestProcessor {

    private static Log log = LogFactory.getLog(TemplateRequestProcessor.class);

    public static final String TEMPLATE_XSL_LOCATION = "/org/wso2/carbon/mashup/request/processors/template/";

    /* (non-Javadoc)
     * @see org.wso2.wsas.transport.HttpGetRequestProcessor#process(org.wso2.carbon.core.transports.CarbonHttpRequest, org.wso2.carbon.core.transports.CarbonHttpResponse, org.apache.axis2.context.ConfigurationContext)
     */
    public void process(CarbonHttpRequest request, CarbonHttpResponse response,
                        ConfigurationContext configurationContext) throws Exception {
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

            if (axisService != null) {
                if (!axisService.isActive()) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    response.addHeader(HTTP.CONTENT_TYPE, "text/html");
                    outputStream
                            .write(("<h4>Service " + serviceName +
                                    " is inactive. Cannot generate API Documentation.</h4>")
                                    .getBytes());
                    outputStream.flush();
                    return;
                }

                DOMSource xmlSource = Util.getSigStream(axisService, null);

                String flavorParameter = request.getParameter("flavor");
                if (flavorParameter != null && !(("").equals(flavorParameter))) {
                    if (!flavorParameter.equalsIgnoreCase("gadget")
                            && !flavorParameter.equalsIgnoreCase("html")) {
                        response.setError(404);
                        response.addHeader(HTTP.CONTENT_TYPE, "text/html; charset=utf-8");
                        outputStream.write(
                                ("<h4>Unsupported template flavor parameter " + flavorParameter +
                                        "</h4>").getBytes());
                        return;
                    }
                } else {
                    flavorParameter = "html";
                }

                InputStream templateStream = this.getClass().getClassLoader().getResourceAsStream(TEMPLATE_XSL_LOCATION + "template-" + flavorParameter + ".xslt");
                if (templateStream == null) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    response.addHeader(HTTP.CONTENT_TYPE, "text/html; charset=utf-8");
                    outputStream.write(("<h4>Cannot find the stylesheet</h4>")
                            .getBytes());
                    return;
                }

                /*
                * XSLT transform to generate the template
                */
                Source xsltSource = new StreamSource(templateStream);
                Result result = new StreamResult(outputStream);
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                transformerFactory.setURIResolver(new TemplateURIResolver());
                Transformer transformer = transformerFactory
                        .newTransformer(xsltSource);

                /*
                * Handling the "service" parameter
                */
                String serviceParameter = request.getParameter("service");
                if (serviceParameter != null && !(("").equals(serviceParameter))) {
                    transformer.setParameter("service", serviceParameter);
                }

                /*
                * Handling the "lang" parameter
                */
                String langParameter = request.getParameter("lang");
                if ((langParameter == null) || (langParameter.equals(""))
                        || (langParameter.equalsIgnoreCase("javascript"))
                        || (langParameter.equalsIgnoreCase("ecmascript"))
                        || (langParameter.equalsIgnoreCase("js"))) {
                } else if (langParameter.equalsIgnoreCase("e4x")) {
                    transformer.setParameter("e4x", "true");
                } else {
                    response.setError(404);
                    response.addHeader(HTTP.CONTENT_TYPE, "text/html; charset=utf-8");
                    outputStream.write(
                            ("<h4>Unsupported lang parameter " + langParameter +
                                    "</h4>").getBytes());
                    return;
                }

                String contentTypeParameter =
                        request.getParameter("content-type");
                String contentType;
                if (contentTypeParameter != null && !"".equals(contentTypeParameter.trim())) {
                    contentType = contentTypeParameter;
                } else {
                    if ("html".equals(flavorParameter)) {
                        contentType = "text/html";
                    } else {
                        contentType = "application/xml";
                    }
                }
                response.addHeader(HTTP.CONTENT_TYPE, contentType + "; charset=utf-8");

                // Handling the localhost parameter. If set to tru the stub will use localhost instead of the IP.
                // Needed when stubs are needed fir inclusion in a mashup.
                String localhostParameter = request.getParameter("localhost");
                if ("true".equals(localhostParameter)) {
                    transformer.setParameter("localhost-endpoints", "true");
                }

                /*if ("googlegadget".equalsIgnoreCase(flavorParameter)) {
                    String servicesPath = configurationContext.getServicePath();
                    String requestURL = request.getRequestURL().toString();
                    String urlPrefix = requestURL.substring(0, requestURI.indexOf(servicesPath));

                }*/
                transformer.setParameter("wsrequest-location", "?wsdl2form&type=js&resource=WSRequest.js");
                transformer.setParameter("stub-location", "../../" + axisService.getName() + "?stub");
                
                // Performing the XSLT transformation
                transformer.transform(xmlSource, result);
            } else {
                response.addHeader(HTTP.CONTENT_TYPE, "text/html");
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                outputStream
                        .write(("<h4>Service cannot be found. Cannot display <em>API Documentation</em>.</h4>")
                                .getBytes());
                outputStream.flush();
            }
        } catch (OMException e) {
            log.error(e);
        } catch (Exception e) {
            log.error(e);
        }

    }

    private class TemplateURIResolver implements URIResolver {
        TemplateURIResolver() {
        }

        public Source resolve(String href, String base) {
            InputStream is = TemplateURIResolver.class.getResourceAsStream(href);
            return new StreamSource(is);
        }
    }
}


