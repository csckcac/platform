/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.rulecep.adapters.service.internal;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.rulecep.adapters.service.InputOutputAdaptersConfigurationFactory;
import org.wso2.carbon.rulecep.adapters.service.InputOutputAdaptersService;
import org.wso2.carbon.rulecep.adapters.service.InputOutputAdaptersServiceImpl;
import org.wso2.carbon.rulecep.commons.LoggedRuntimeException;
import org.wso2.carbon.rulecep.commons.descriptions.AXIOMXPathFactory;
import org.wso2.carbon.utils.ServerConstants;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;

/**
 * @scr.component name="inputoutputadapters.component" immediate="true"
 */
public class InputOutputAdaptersComponent {

    private static final Log log = LogFactory.getLog(InputOutputAdaptersComponent.class);

    private ServiceRegistration serviceRegistration;
    private static final String COMPONENT_CONF = "adapters-component.conf";

    protected void activate(ComponentContext componentContext) {

        if (log.isDebugEnabled()) {
            log.debug("Starting  the InputOutputAdapters Component ");
        }

        OMElement config = loadConfigXML();
        InputOutputAdaptersService service = new InputOutputAdaptersServiceImpl(
                InputOutputAdaptersConfigurationFactory.create(config,
                        new AXIOMXPathFactory()));
        serviceRegistration = componentContext.getBundleContext().registerService(
                InputOutputAdaptersService.class.getName(),
                service,
                null);
    }

    protected void deactivate(ComponentContext componentContext) {

        if (log.isDebugEnabled()) {
            log.debug("Stopping the InputOutputAdapters Component");
        }

        componentContext.getBundleContext().ungetService(serviceRegistration.getReference());
    }

    /**
     * Helper method to load the event config
     *
     * @return OMElement representation of the event config
     */
    private OMElement loadConfigXML() {
        String carbonHome = System.getProperty(ServerConstants.CARBON_CONFIG_DIR_PATH);
        String path = carbonHome + File.separator + COMPONENT_CONF;
        BufferedInputStream inputStream = null;
        try {
            File file = new File(path);
            if (!file.exists()) {
                log.info("There is no " + COMPONENT_CONF + ".. Using the default configuration");
                inputStream = new BufferedInputStream(
                        new ByteArrayInputStream("<InputOutputManager/>".getBytes()));
            } else {
                inputStream = new BufferedInputStream(new FileInputStream(file));
            }
            XMLStreamReader parser = XMLInputFactory.newInstance().
                    createXMLStreamReader(inputStream);
            StAXOMBuilder builder = new StAXOMBuilder(parser);
            return builder.getDocumentElement();
        } catch (FileNotFoundException e) {
            throw new LoggedRuntimeException(COMPONENT_CONF + "cannot be found in" +
                    " the path : " + path,
                    e, log);
        } catch (XMLStreamException e) {
            throw new LoggedRuntimeException("Invalid XML for " + COMPONENT_CONF + " located in" +
                    " the path : " +
                    path, e, log);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException ingored) {
            }
        }
    }
}
