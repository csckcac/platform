/**
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.bam.ui;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.wso2.carbon.bam.util.BAMException;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class BAMConfiguration {

    private static OMElement bamElement = null;
    private static String eventBrokerName = null;

    public static OMElement getBAMElement() throws BAMException {

        // Only load bam xml configuration once
        if (bamElement != null) {
            return bamElement;
        }

        StAXOMBuilder builder;
        FileReader reader = null;
        XMLStreamReader parse = null;

        try {
            String configFile = CarbonUtils.getCarbonConfigDirPath() + File.separator +
                                BAMUIConstants.BAM_CONSTANTS_CONFIG_FILE;
            try {
                reader = new FileReader(configFile);
                parse = XMLInputFactory.newInstance().createXMLStreamReader(reader);

                builder = new StAXOMBuilder(parse);
                bamElement = builder.getDocumentElement();
                bamElement.build();

                return bamElement;

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
    }

    public static String getPropertyFromBAMOMElement(OMElement bamElement, String propertyName) {
        OMElement element = bamElement.getFirstChildWithName(new QName(propertyName));
        return element.getText();
    }

    public static String getEventBrokerName() throws BAMException {
        try {
            if (eventBrokerName != null) {
                return eventBrokerName;
            }
            OMElement bamElement = getBAMElement();
            eventBrokerName = getPropertyFromBAMOMElement(bamElement, BAMUIConstants.EVENTBROKER_SERVICE_NAME);
            return eventBrokerName;
        } catch (Exception e) {
            return BAMUIConstants.DEFAULT_EVENT_BROKER_NAME;
        }
    }

}