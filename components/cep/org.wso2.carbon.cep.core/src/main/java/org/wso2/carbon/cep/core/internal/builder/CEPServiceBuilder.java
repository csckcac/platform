/*
 * Copyright 2004,2005 The Apache Software Foundation.
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

package org.wso2.carbon.cep.core.internal.builder;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cep.core.CEPServiceInterface;
import org.wso2.carbon.cep.core.exception.CEPConfigurationException;
import org.wso2.carbon.cep.core.internal.CEPService;
import org.wso2.carbon.cep.core.internal.ds.CEPServiceValueHolder;
import org.wso2.carbon.cep.core.internal.util.CEPConstants;
import org.wso2.carbon.utils.ServerConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.util.List;

/**
 * this class builds the CEPService from the configuration file. It uses teh CEPService methods
 * interact.
 */
public class CEPServiceBuilder {

    private static final Log log = LogFactory.getLog(CEPServiceBuilder.class);

//    private static CEPService cepService;
//

    /**
     * creates the main cep service using details given in the configuration file
     *
     * @return - cep service
     * @throws CEPConfigurationException
     */
    public static CEPServiceInterface createCEPService() throws CEPConfigurationException {

        CEPService cepService = new CEPService();
        CEPServiceValueHolder.getInstance().setCepService(cepService);
        OMElement cepConfig = loadConfigXML();

        if (cepConfig != null) {
            if (!cepConfig.getQName().equals(
                    new QName(CEPConstants.CEP_CONF_NAMESPACE, CEPConstants.CEP_CONF_ELE_ROOT))) {
                throw new CEPConfigurationException("Invalid root element in cep config");
            }

            // creates the buckets with this provider
            OMElement bucketsElement =
                    cepConfig.getFirstChildWithName(new QName(CEPConstants.CEP_CONF_NAMESPACE,
                            CEPConstants.CEP_CONF_ELE_BUCKETS));
            // If there are new buckets elements those are added to the registry
            if (bucketsElement != null) {
                CEPBucketBuilder.addNewBucketsToRegistry(bucketsElement, MultitenantConstants.SUPER_TENANT_ID);  //these will be added to super tenant
            }
        }

        // load all the available bucket in registry to the CEP engine
//        CEPBucketBuilder.loadBucketsFromRegistry(cepService);

        deployAllCEPBuckets();

        AxisConfiguration axisConfiguration =
                CEPServiceValueHolder.getInstance().getConfigurationContextService().getServerConfigContext().getAxisConfiguration();
        loadBucketsFromRegistry(axisConfiguration);
        return cepService;
    }

    private static void deployAllCEPBuckets() throws CEPConfigurationException {
        //only super tenant's deployer will we populating unDeployedBuckets as its the only one called at server startup
        //these are added as super tenant buckets
        List<OMElement> unDeployedBuckets = CEPServiceValueHolder.getInstance().getUnDeployedBuckets();
        for (OMElement bucketElement : unDeployedBuckets) {
            CEPBucketBuilder.addNewBucketToRegistry(bucketElement,MultitenantConstants.SUPER_TENANT_ID);
        }
        unDeployedBuckets.clear();
    }


    /**
     * Helper method to load the event config
     *
     * @return OMElement representation of the event config
     */
    private static OMElement loadConfigXML() throws CEPConfigurationException {

        String carbonHome = System.getProperty(ServerConstants.CARBON_CONFIG_DIR_PATH);
        String path = carbonHome + File.separator + CEPConstants.CEP_CONF;

        // if the cep config file not exists then simply return null.
        File cepConfigFile = new File(path);
        if (!cepConfigFile.exists()) {
            return null;
        }

        BufferedInputStream inputStream = null;
        try {
            inputStream = new BufferedInputStream(new FileInputStream(new File(path)));
            XMLStreamReader parser = XMLInputFactory.newInstance().
                    createXMLStreamReader(inputStream);
            StAXOMBuilder builder = new StAXOMBuilder(parser);
            OMElement omElement = builder.getDocumentElement();
            omElement.build();
            return omElement;
        } catch (FileNotFoundException e) {
            String errorMessage = CEPConstants.CEP_CONF
                    + "cannot be found in the path : " + path;
            log.error(errorMessage, e);
            throw new CEPConfigurationException(errorMessage, e);
        } catch (XMLStreamException e) {
            String errorMessage = "Invalid XML for " + CEPConstants.CEP_CONF
                    + " located in the path : " + path;
            log.error(errorMessage, e);
            throw new CEPConfigurationException(errorMessage, e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                String errorMessage = "Can not close the input stream";
                log.error(errorMessage, e);
                throw new CEPConfigurationException(errorMessage, e);
            }
        }
    }

    public static void loadBucketsFromRegistry(AxisConfiguration axisConfiguration) {
        try {
            CEPService cepService = CEPServiceValueHolder.getInstance().getCepService();
            CEPBucketBuilder.loadBucketsFromRegistry(cepService, axisConfiguration);
        } catch (CEPConfigurationException e) {
            log.error("Unable to load buckets from registry" + e);
        }
    }

}
