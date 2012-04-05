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
package org.wso2.carbon.mashup.javascript.stubgenerator;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisEndpoint;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.description.WSDL11ToAllAxisServicesBuilder;
import org.apache.axis2.description.WSDL11ToAxisServiceBuilder;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.namespace.Constants;
import org.apache.axis2.util.XMLUtils;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.mashup.utils.MashupConstants;
import org.wso2.carbon.mashup.utils.MashupUtils;
import org.wso2.carbon.wsdl2form.Util;

import javax.activation.DataHandler;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Result;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This service facilitates the genaratin of JavaScript stubs given a WSDL document or url to a WSDL.
 */
public class JSStubGenerator {

    private static Log log = LogFactory.getLog(JSStubGenerator.class);

    /**
     * Given a WSDL this operation returns the JavaScript stub for that service
     *
     * @param type - dom or e4x
     * @param wsdl - DataHandler representing the WSDL
     * @return - The JavaScript stub as a String
     * @throws CarbonException - Thrown in case an exception occurs
     */
    public String genarateStub(String type, DataHandler wsdl) throws CarbonException {
        InputStream inputStream;
        try {
            inputStream = wsdl.getDataSource().getInputStream();
        } catch (IOException e) {
            throw new CarbonException(e);
        }
        return getStub(type, inputStream, null);
    }

    /**
     * Given a uri to a WSDL this operation returns the JavaScript stub for that service
     *
     * @param type - dom or e4x
     * @param url  - URL to the WSDL document
     * @return - The JavaScript stub as a String
     * @throws CarbonException - Thrown in case an exception occurs
     */
    public String genarateStubFromURL(String type, String url) throws CarbonException {

        HttpMethod httpMethod = new GetMethod(url);

        InputStream inputStream;
        try {
            URL wsdlURL = new URL(url);
            int statusCode = MashupUtils.executeHTTPMethod(httpMethod, wsdlURL, null, null);
            if (statusCode != HttpStatus.SC_OK) {
                throw new CarbonException(
                        "An error occured while getting the WSDL at " + wsdlURL +
                        ". Reason :" +
                        httpMethod.getStatusLine());
            }
            inputStream = httpMethod.getResponseBodyAsStream();
            return getStub(type, inputStream, url);
        } catch (IOException e) {
            throw new CarbonException(e);
        } finally {
            httpMethod.releaseConnection();
        }
    }

    private String getStub(String type, InputStream inputStream, String uri)
            throws CarbonException {
        AxisService service = null;

        ByteArrayOutputStream stubOutStream = new ByteArrayOutputStream();
        try {
            OMElement documentElement = (OMElement) XMLUtils.toOM(inputStream);
            OMNamespace documentElementNS = documentElement.getNamespace();
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            if (documentElementNS != null) {
                WSDL11ToAxisServiceBuilder wsdl11ToAxisServiceBuilder;
                if (Constants.NS_URI_WSDL11.
                        equals(documentElementNS.getNamespaceURI())) {
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    documentElement.serialize(outputStream);
                    InputStream inStream = new ByteArrayInputStream(outputStream.toByteArray());
                    wsdl11ToAxisServiceBuilder = new WSDL11ToAllAxisServicesBuilder(inStream);
                    (wsdl11ToAxisServiceBuilder).setBaseUri(uri);
                    wsdl11ToAxisServiceBuilder.setAllPorts(true);
                    service = wsdl11ToAxisServiceBuilder.populateService();
                    Map endpointsMap = service.getEndpoints();
                    Iterator iterator = endpointsMap.values().iterator();
                    String[] eprs = new String[endpointsMap.size()];
                    int i = 0;
                    while (iterator.hasNext()) {
                        AxisEndpoint axisEndpoint = (AxisEndpoint) iterator.next();
                        eprs[i] = axisEndpoint.getEndpointURL();
                        i++;
                    }
                    service.setEPRs(eprs);

                    // With the change to the deployment mechanism in axis2 now it checks weather a
                    // transport is active before displaying it in the WSDL. This check needs access
                    // to the AxisConfiguration, and it it is null it returns false. Hence we have
                    // to put in a hck here to get the conversion to work correctly.
                    MessageContext messageContext = MessageContext.getCurrentMessageContext();
                    AxisServiceGroup axisServiceGroup = new AxisServiceGroup();
                    axisServiceGroup.setParent(
                            messageContext.getConfigurationContext().getAxisConfiguration());
                    service.setParent(axisServiceGroup);
                    service.printWSDL2(outStream);
                } else if (WSDL2Constants.WSDL_NAMESPACE
                        .equals(documentElementNS.getNamespaceURI())) {
                    documentElement.serialize(outStream);
                    if (uri != null) {
                       try {
                            String serviceName = null;
                            String wsdlURL = new URL(uri).getPath().split("/services")[1];
                            String[] serviceURLParts = wsdlURL.split("/");
                            int serviceNameVal = serviceURLParts.length; //Calculate the length of url parts
                             if (serviceNameVal == 5) {
                            //If the requested url is a tenant-specific service wsdl2 generated in carbon platform
                                serviceName = MashupConstants.SEPARATOR_CHAR + serviceURLParts[2] +
                                              MashupConstants.SEPARATOR_CHAR + serviceURLParts[3];
                            } else if(serviceNameVal == 3) {
                            //If the requested url is a service wsdl2 generated in carbon platform
                                serviceName = MashupConstants.SEPARATOR_CHAR + serviceURLParts[1] +
                                              MashupConstants.SEPARATOR_CHAR + serviceURLParts[2];
                            }
                            service = new AxisService(serviceName);
                        } catch (IOException e) {
                            throw new CarbonException(e);
                        }
                    }

                } else {
                    throw new CarbonException("Invalid WSDL");
                }

            } else {
                throw new CarbonException("Invalid WSDL");
            }
            DOMSource sigStream = Util.getSigStream(service, outStream, null);
            Result result = new StreamResult(stubOutStream);
            Map<String, String> paramMap = null;
            if ("e4x".equals(type)) {
                paramMap = new HashMap<String, String>();
                paramMap.put("e4x", "true");
            }
            Util.generateStub(sigStream, result, paramMap);
        } catch (XMLStreamException e) {
            throw new CarbonException(e);
        } catch (AxisFault axisFault) {
            throw new CarbonException(axisFault);
        } catch (TransformerException e) {
            throw new CarbonException(e);
        } catch (ParserConfigurationException e) {
            throw new CarbonException(e);
        }
        return stubOutStream.toString();
    }
}
