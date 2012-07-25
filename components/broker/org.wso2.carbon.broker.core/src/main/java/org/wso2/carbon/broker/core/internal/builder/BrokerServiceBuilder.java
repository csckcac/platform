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

package org.wso2.carbon.broker.core.internal.builder;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.broker.core.BrokerService;
import org.wso2.carbon.broker.core.exception.BrokerConfigException;
import org.wso2.carbon.broker.core.internal.BrokerTypeFactory;
import org.wso2.carbon.broker.core.internal.CarbonBrokerService;
import org.wso2.carbon.broker.core.internal.brokers.agent.AgentBrokerTypeFactory;
import org.wso2.carbon.broker.core.internal.brokers.jms.generic.GenericJMSBrokerTypeFactory;
import org.wso2.carbon.broker.core.internal.brokers.local.LocalBrokerTypeFactory;
import org.wso2.carbon.broker.core.internal.brokers.ws.WSBrokerTypeFactory;
import org.wso2.carbon.broker.core.internal.util.BrokerConstants;
import org.wso2.carbon.utils.ServerConstants;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * reads the broker typesfrom the carbon.xml file and creates the broker types.
 */
public final class BrokerServiceBuilder {

    private static final Log log = LogFactory.getLog(BrokerService.class);

    private BrokerServiceBuilder() {
    }

    /**
     * builds the broker service objects with the configurations defined in the
     * carbon.xml
     *
     * @return
     * @throws BrokerConfigException
     */
    public static BrokerService createBrokerService() throws BrokerConfigException {

        CarbonBrokerService brokerService = new CarbonBrokerService();
        OMElement brokerConfig = loadConfigXML();
        if (brokerConfig != null) {
            if (!brokerConfig.getQName().equals(
                    new QName(BrokerConstants.BROKER_CONF_NS, BrokerConstants.BROKER_CONF_ELE_ROOT))) {
                throw new BrokerConfigException("Broker config exception");
            }
            Iterator brokerTypesIter = brokerConfig.getChildrenWithName(
                    new QName(BrokerConstants.BROKER_CONF_NS,
                              BrokerConstants.BROKER_CONF_ELE_BROKER_TYPE));
            for (; brokerTypesIter.hasNext(); ) {
                OMElement brokerTypeOMElement = (OMElement) brokerTypesIter.next();
                String className = brokerTypeOMElement.getAttributeValue(
                        new QName("", BrokerConstants.BROKER_CONF_ATTR_CLASS));
                registerBrokerType(brokerService, className);
            }
        } else {
            log.info("No broker types are given");
        }

        //by default we add the already existing broker types if they are not added
        List<String> existingBrokerNames = brokerService.getBrokerTypeNames();

        if (!existingBrokerNames.contains(BrokerConstants.BROKER_TYPE_LOCAL)) {
            registerBrokerType(brokerService, LocalBrokerTypeFactory.class.getName());
        }

        if (!existingBrokerNames.contains(BrokerConstants.BROKER_TYPE_WS_EVENT)) {
            registerBrokerType(brokerService, WSBrokerTypeFactory.class.getName());
        }

        //Qpid no more supported
//        if (!existingBrokerNames.contains(BrokerConstants.BROKER_TYPE_JMS_QPID)){
//            registerBrokerType(brokerService, QpidBrokerTypeFactory.class.getName());
//        }

        if (!existingBrokerNames.contains(BrokerConstants.BROKER_TYPE_JMS_GENERIC)) {
            registerBrokerType(brokerService, GenericJMSBrokerTypeFactory.class.getName());
        }

        if (!existingBrokerNames.contains(BrokerConstants.BROKER_TYPE_AGENT)) {
            registerBrokerType(brokerService, AgentBrokerTypeFactory.class.getName());
        }


        return brokerService;

    }

    private static void registerBrokerType(CarbonBrokerService brokerService, String className)
            throws BrokerConfigException {
        try {
            Class brokerTypeFactoryClass = Class.forName(className);
            BrokerTypeFactory factory =
                    (BrokerTypeFactory) brokerTypeFactoryClass.newInstance();
            brokerService.registerBrokerType(factory.getBrokerType());
        } catch (ClassNotFoundException e) {
            throw new BrokerConfigException("Broker class " + className + " can not be found", e);
        } catch (IllegalAccessException e) {
            throw new BrokerConfigException("Can not access the class " + className, e);
        } catch (InstantiationException e) {
            throw new BrokerConfigException("Can not instantiate the class " + className, e);
        }
    }


    /**
     * Helper method to load the event config
     *
     * @return OMElement representation of the event config
     */
    private static OMElement loadConfigXML() throws BrokerConfigException {

        String carbonHome = System.getProperty(ServerConstants.CARBON_CONFIG_DIR_PATH);
        String path = carbonHome + File.separator + BrokerConstants.BROKER_CONF;
        BufferedInputStream inputStream = null;
        File inputFile = new File(path);

        if (!inputFile.exists()) {
            log.info(" The " + BrokerConstants.BROKER_CONF + " can not found ");
            return null;
        }
        try {
            inputStream = new BufferedInputStream(new FileInputStream(new File(path)));
            XMLStreamReader parser = XMLInputFactory.newInstance().
                    createXMLStreamReader(inputStream);
            StAXOMBuilder builder = new StAXOMBuilder(parser);
            OMElement omElement = builder.getDocumentElement();
            omElement.build();
            return omElement;
        } catch (FileNotFoundException e) {
            throw new BrokerConfigException(BrokerConstants.BROKER_CONF
                                            + "cannot be found in the path : " + path, e);
        } catch (XMLStreamException e) {
            throw new BrokerConfigException("Invalid XML for " + BrokerConstants.BROKER_CONF
                                            + " located in the path : " + path, e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException ingored) {
                log.error("Can not close the input stream after reading " + inputFile.getAbsolutePath());
            }
        }
    }
}
