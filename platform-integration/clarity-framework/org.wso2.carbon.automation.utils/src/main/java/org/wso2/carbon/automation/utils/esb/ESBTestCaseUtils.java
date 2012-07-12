/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

package org.wso2.carbon.automation.utils.esb;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.client.Stub;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.wso2.carbon.automation.api.clients.mediation.ConfigServiceAdminClient;
import org.wso2.carbon.automation.api.clients.mediation.SynapseConfigAdminClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.mediation.configadmin.stub.ConfigServiceAdminStub;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ServerConstants;

import javax.servlet.ServletException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.RemoteException;

public class ESBTestCaseUtils {

    private static final int ESB_HTTP_PORT = 8280;
    private static final int ESB_HTTPS_PORT = 8243;
    private static final int ESB_SERVLET_HTTP_PORT = 9763;
    private static final int ESB_SERVLET_HTTPS_PORT = 9443;

    protected Log log = LogFactory.getLog(getClass());

    /**
     * Loads the specified resource from the classpath and returns its content as an OMElement.
     *
     * @param path A relative path to the resource file
     * @return An OMElement containing the resource content
     */
    public OMElement loadClasspathResource(String path) {
        OMElement documentElement = null;
        FileInputStream inputStream;
        File file = new File((getClass().getResource(path).getPath()));
        if (file.exists()) {
            try {
                inputStream = new FileInputStream((getClass().getResource(path).getPath()));
                XMLStreamReader parser = XMLInputFactory.newInstance().createXMLStreamReader(inputStream);
                //create the builder
                StAXOMBuilder builder = new StAXOMBuilder(parser);
                //get the root element (in this case the envelope)
                documentElement = builder.getDocumentElement();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (XMLStreamException e) {
                e.printStackTrace();
            }
        }
        return documentElement;
    }

    /**
     * Loads the specified ESB configuration file from the classpath and deploys it into the ESB.
     *
     * @param filePath A relative path to the configuration file
     * @throws java.rmi.RemoteException If an error occurs while loading the specified configuration
     */
    public void loadESBConfigurationFromClasspath(String filePath, String backendURL,
                                                  String sessionCookie)
            throws RemoteException, XMLStreamException, ServletException {
        OMElement configElement = loadClasspathResource(filePath);
        updateESBConfiguration(configElement, backendURL, sessionCookie);
    }

    /**
     * Loads the configuration of the specified sample into the ESB.
     *
     * @param number     Sample number
     * @param backendURL backend ULR of the server admin services
     * @throws Exception If an error occurs while loading the sample configuration
     */
    public void loadSampleESBConfiguration(int number, String backendURL, String sessionCookie)
            throws Exception {
        String filePath = System.getProperty(ServerConstants.CARBON_HOME) + File.separator + "repository" +
                          File.separator + "samples" + File.separator + "synapse_sample_" + number + ".xml";
        File configFile = new File(filePath);
        FileInputStream inputStream = new FileInputStream(configFile.getAbsolutePath());
        XMLStreamReader parser = XMLInputFactory.newInstance().createXMLStreamReader(inputStream);
        StAXOMBuilder builder = new StAXOMBuilder(parser);
        OMElement documentElement = builder.getDocumentElement();
        updateESBConfiguration(documentElement, backendURL, sessionCookie);
    }


    private void updateESBConfiguration(OMElement config, String backendURL, String sessionCookie)
            throws RemoteException, XMLStreamException, ServletException {
        SynapseConfigAdminClient synapseConfigAdminClient =
                new SynapseConfigAdminClient(backendURL, sessionCookie);
        synapseConfigAdminClient.updateConfiguration(config.toString());
    }
}
