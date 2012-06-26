/*
 *
 *   Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 * /
 */

package org.wso2.bps.integration.tests.util;

import junit.framework.Assert;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Scanner;

public class BPSTestUtils {
    private static final Log log = LogFactory.getLog(BPSTestUtils.class);
    public final static boolean TWO_WAY = true;
    public final static boolean ONE_WAY = false;

    public static final String HUMANTASK_SAMPLE_LOCATION = System.getProperty("bps.humantask.sample.location");
    public static final String BPEL_TEST_RESOURCE_LOCATION = System.getProperty("bps.test.resource.location");



    /**
     * Send a request which can be configured with given payload and headerMap.
     * If the request is "two way" the expectedStrings will be checked against the resultant
     * payload and expectedHeaderMap will be verified against the resultant Header element.
     * If the request is "one way", no verification happen.
     *
     * @param eprUrl target endpoint
     * @param operation operation name
     * @param payload payload body which will be the SOAP Body in the request
     * @param numberOfInstances expected number of instances to be created
     * @param expectedStrings expected list of strings in the resultant body
     * @param isTwoWay whether the request expects a response or not
     * @param headerMap list of headers to be included in the request
     * @param expectedHeaderMap list of headers expected to be included in the response
     */
    public static void sendRequest(String eprUrl, String operation, String payload,
                                   int numberOfInstances, List<String> expectedStrings,
                                   boolean isTwoWay, List<OMElement> headerMap,
                                   List<OMElement> expectedHeaderMap
                                   ) throws InterruptedException, XMLStreamException, AxisFault {
        waitForServiceDeployment(eprUrl);
        Assert.assertFalse("Service not found: " + eprUrl, !BPSTestUtils.isServiceAvailable(eprUrl));

        for (int i = 0; i < numberOfInstances; i++) {

            EndpointReference epr = new EndpointReference(eprUrl + "/" + operation);
            if (isTwoWay) {
                SOAPEnvelope resultEnvelop = BPSTestUtils.sendRequest(payload, epr, headerMap);
                OMElement resultBody = resultEnvelop.getBody();
                OMElement resultHeader = resultEnvelop.getHeader();

                //Checking the expected payLoad strings
                if (expectedStrings != null) {
                    verifyPayLoad(resultBody, expectedStrings);
                }
                //Checking expected header elements
                if (expectedHeaderMap != null && !expectedHeaderMap.isEmpty()) {
                    verifyHeaderMap(resultHeader, expectedHeaderMap);
                }
            } else {
                BPSTestUtils.sendRequestOneWay(payload, epr, headerMap);
            }

        }

    }

    /**
     * Verify the resultant Body with given expected strings
     * @param bodyElement target body element to be checked against
     * @param expectedStrings list of expected list of strings
     */
    private static void verifyPayLoad(OMElement bodyElement, List<String> expectedStrings) {
        for (String expectedString : expectedStrings) {
            Assert.assertFalse("Incorrect Test Result: " + bodyElement.toString(),
                               !bodyElement.toString().contains(expectedString));
        }
    }

    /**
     * Verify the a SOAP Header, whether it includes the given header-map
     * @param header target header element to be checked against
     * @param expectedHeaderMap list of expected list of header
     */
    private static void verifyHeaderMap(OMElement header, List<OMElement> expectedHeaderMap) {
        for(OMElement expectedHeader : expectedHeaderMap) {
            try {
                OMElement element = header.getFirstChildWithName(expectedHeader.getQName());
                if (element != null) {
                    log.warn("The expected header found in the reply SOAP Header. Please check the " +
                             "content is equal manually.");
                } else {
                    Assert.fail("Expected Header : " + expectedHeader.getQName().toString() + " " +
                                "not found.");
                }
            } catch (OMException ex) {
                Assert.fail("Unexpected error occurred : " + ex.getLocalizedMessage());
            }
        }
    }

    /**
     * Send a request with given headers
     * @param payloadStr payload payload body which will be the SOAP Body in the request
     * @param targetEPR target endpoint
     * @param headerMap list of headers to be included in the request
     * @return Response - SOAP-Envelop
     */
    public static SOAPEnvelope sendRequest(String payloadStr, EndpointReference targetEPR,
                                        List<OMElement> headerMap) throws XMLStreamException,
                                                                     AxisFault {
        OMElement payload = AXIOMUtil.stringToOM(payloadStr);
        Options options = new Options();
        options.setTo(targetEPR);

        //Blocking invocation
        ServiceClient sender = new ServiceClient();

        //Adding the header elements
        if (headerMap != null) {
            for (OMElement header : headerMap) {
                sender.addHeader(header);
            }
        }

        sender.setOptions(options);
        log.info("Request: " + payload.toString());
        OMElement result = sender.sendReceive(payload);
        log.info("Response: " + result.toString());

        //log.info(result.toString());
        return sender.getLastOperationContext().getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE).getEnvelope();
    }

    public static OMElement sendRequest(String payloadStr, EndpointReference targetEPR)
            throws XMLStreamException, AxisFault {
        SOAPEnvelope resultEnv = BPSTestUtils.sendRequest(payloadStr, targetEPR, null);
        return resultEnv.getBody().getFirstElement();
    }

    private static void sendRequestOneWay(String payloadStr, EndpointReference targetEPR, List<OMElement> headerMap) throws XMLStreamException, AxisFault {
        OMElement payload = AXIOMUtil.stringToOM(payloadStr);
        Options options = new Options();
        options.setTo(targetEPR);
        //options.setAction("urn:" + operation); //since soapAction = ""

        //Blocking invocation
        ServiceClient sender = new ServiceClient();

        //Adding the header elements
        if (headerMap != null) {
            for (OMElement header : headerMap) {
                sender.addHeader(header);
            }
        }

        sender.setOptions(options);
        log.info("Request: " + payload.toString());
        sender.fireAndForget(payload);
    }

    public static void sendRequestOneWay(String payloadStr, EndpointReference targetEPR)
            throws XMLStreamException, AxisFault {
        BPSTestUtils.sendRequestOneWay(payloadStr, targetEPR, null);
    }


    public static boolean isServiceAvailable(String serviceUrl) {
        URL wsdlURL;
        InputStream wsdlIS = null;
        try {
            wsdlURL = new URL(serviceUrl + "?wsdl");

            try {
                wsdlIS = wsdlURL.openStream();
            } catch (IOException e) {
                return false;// do nothing, wait for the service
            }

            if (wsdlIS != null) {

                String wsdlContent = convertStreamToString(wsdlIS);
                return wsdlContent.indexOf("definitions") > 0;
            }
            return false;


        } catch (MalformedURLException e) {
            return false;
        } finally {
            if (wsdlIS != null) {
                try {
                    wsdlIS.close();
                } catch (IOException e) {
                    log.error("Error occurred when closing the wsdl input stream");
                }
            }
        }

    }

    public static void waitForServiceDeployment(String serviceUrl) throws InterruptedException {
        int serviceTimeOut = 0;
        while (!isServiceAvailable(serviceUrl)) {
            //TODO - this looping is only happening for 14 times. Need to find the exact cause for this.
            if (serviceTimeOut == 0) {
                log.info("Waiting for the " + serviceUrl + ".");
            } else if (serviceTimeOut > 20) {
                log.error("Time out");
                Assert.fail(serviceUrl + " service is not found");
                break;
            }

            Thread.sleep(5000);
            serviceTimeOut++;

        }
    }

    public static void sendRequest(String eprUrl, String operation, String payload,
                                   int numberOfInstances, List<String> expectedStrings,
                                   boolean twoWay)
            throws InterruptedException, XMLStreamException, AxisFault {
        waitForServiceDeployment(eprUrl);
        Assert.assertFalse("Service not found: " + eprUrl, !BPSTestUtils.isServiceAvailable(eprUrl));

        for (int i = 0; i < numberOfInstances; i++) {

            EndpointReference epr = new EndpointReference(eprUrl + "/" + operation);
            if (twoWay) {
                OMElement result = BPSTestUtils.sendRequest(payload, epr);
                if (expectedStrings != null) {
                    verifyPayLoad(result, expectedStrings);
                }
            } else {
                BPSTestUtils.sendRequestOneWay(payload, epr);
            }

        }
    }

    public static void sendRequest(String eprUrl, String operation, String payload,
                                   int numberOfInstances, String expectedException, boolean twoWay)
            throws XMLStreamException, AxisFault {
        Assert.assertFalse("Service not found: " + eprUrl, !BPSTestUtils.isServiceAvailable(eprUrl));

        for (int i = 0; i < numberOfInstances; i++) {

            EndpointReference epr = new EndpointReference(eprUrl + "/" + operation);
            if (twoWay) {
                OMElement result = BPSTestUtils.sendRequest(payload, epr);

            } else {
                BPSTestUtils.sendRequestOneWay(payload, epr);
            }
        }
    }

    public static String convertStreamToString(InputStream is) {
        return new Scanner(is).useDelimiter("\\A").next();
    }

    /**
     *  Get the BPS Sample folder location relative to the extracted BPS Distribution
     * @param carbonHome Carbon Home
     * @return BPEL sample location in the BPS distribution
     */
    public static String getBpelSampleLocation(String carbonHome){
        return carbonHome + File.separator + "repository" + File.separator + "samples" +
                File.separator  + "bpel" + File.separator;
    }

    /**
     * Get the BPS integration test samples location
     * @return BPS integration test samples location
     */
    public static String getBpelTestSampleLocation(){
        return System.getProperty("bps.sample.location") + "bpel" + File.separator;
    }

    public static String getHumanTaskSampleLocation(String carbonHome){
        return carbonHome + File.separator + "repository" + File.separator + "samples" +
                File.separator  + "humantask" + File.separator;
    }

}
