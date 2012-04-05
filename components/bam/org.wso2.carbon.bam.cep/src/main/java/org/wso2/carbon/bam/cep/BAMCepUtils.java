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
package org.wso2.carbon.bam.cep;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.util.BAMException;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.FileReader;
import java.io.IOException;


/**
 * This is used to add common functions for bam-cep
 */
public class BAMCepUtils {
    
    Log log = LogFactory.getLog(BAMCepUtils.class);
    protected synchronized void invokeSoapClient(OMElement payLoad, EndpointReference reference){
     // creates a new connection manager and a http client object
        MultiThreadedHttpConnectionManager httpConnectionManager =
                                                                   new MultiThreadedHttpConnectionManager();
        HttpClient httpClient = new HttpClient(httpConnectionManager);
        ServiceClient serviceClient=null;
        try {
            serviceClient = new ServiceClient();
            Options options = new Options();

            options.setAction("http://ws.apache.org/ws/2007/05/eventing-extended/Publish");
            options.setTo(reference);
            serviceClient.setOptions(options);
         // set the above created objects to re use.
            serviceClient.getOptions().setProperty(HTTPConstants.REUSE_HTTP_CLIENT,
                                            Constants.VALUE_TRUE);
            serviceClient.getOptions().setProperty(HTTPConstants.CACHED_HTTP_CLIENT,
                                            httpClient);
            serviceClient.fireAndForget(payLoad);
        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();
        }
        finally {
            if (serviceClient!= null) {
                try {
                    serviceClient.cleanupTransport();
                } catch (Exception e) {
                    log.error("Could not clean the transport", e);
                }

                try {
                    serviceClient.cleanup();
                } catch (Exception e) {
                    log.error("Could not clean service client", e);
                }
            }
        }
        httpConnectionManager.closeIdleConnections(0);
        httpConnectionManager.shutdown();
    }

    protected OMElement getBAMConfigFile() throws BAMException {
        StAXOMBuilder builder;
        OMElement bamDocumentElement;
        FileReader reader = null;
        XMLStreamReader parse = null;
        try {

            try {
                reader = new FileReader(BAMCepConstants.BAM_CONF_FILE);
                parse = XMLInputFactory.newInstance().createXMLStreamReader(reader);

                builder = new StAXOMBuilder(parse);
                bamDocumentElement = builder.getDocumentElement();
            } finally {
                if (reader != null) {
                    reader.close();
                }
                if (parse != null) {
                    parse.close();
                }
            }

        } catch (XMLStreamException e) {
            throw new BAMException("error occurred creating stream for bam.xml", e);
        } catch (IOException e) {
            throw new BAMException("error occurred getting bam.xml ", e);
        }

        return bamDocumentElement;
    }
}
