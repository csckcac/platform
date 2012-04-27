package org.wso2.carbon.bam.gadgetgenwizard.service;

import org.apache.axiom.om.*;
import org.apache.axiom.om.impl.jaxp.OMSource;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import java.io.OutputStream;

/**
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class GadgetGenService {

    public String createGadget(WSMap map) throws TransformerException {
        OMDocument intermediateXML = createIntermediateXML(map);
        OMElement gadgetXML = applyXSLTForGadgetXML(intermediateXML);
        OutputStream jagFile = applyXSLTForJaggeryScript(intermediateXML);
        return null;
    }

    private OutputStream applyXSLTForJaggeryScript(OMDocument intermediateXML) throws TransformerException {

        Source xsltsource = new OMSource(null);
        Source xmlsource = new OMSource(intermediateXML.getOMDocumentElement());
        StreamResult result = new StreamResult(System.out);
        TransformerFactory transFact =
                TransformerFactory.newInstance();
        Transformer transformer = transFact.newTransformer(xsltsource);
        transformer.transform(xmlsource, result);

        return null;  //To change body of created methods use File | Settings | File Templates.
    }

    private OMElement applyXSLTForGadgetXML(OMDocument intermediateXML) {
        return null;  //To change body of created methods use File | Settings | File Templates.
    }

    private OMDocument createIntermediateXML(WSMap map) {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMDocument omDocument = factory.createOMDocument();
        OMNamespace gadgetgen = factory.createOMNamespace("gadgetgen", "http://wso2.com/bam");
        WSMapElement[] wsMapElements = map.getWsMapElements();
        boolean barChartFound = false;
        for (int i = 0; i < wsMapElements.length; i++) {
            WSMapElement wsMapElement = wsMapElements[i];

            // check for bar graph properties
            OMElement barChart = factory.createOMElement("BarChart", gadgetgen);
            if (wsMapElement.getKey().startsWith("bar")) {
                barChartFound = true;
                OMElement omElement = factory.createOMElement(wsMapElement.getKey(), gadgetgen);
                omElement.setText(wsMapElement.getValue());
                barChart.addChild(omElement);
            }

            OMElement omElement = factory.createOMElement(wsMapElement.getKey(), gadgetgen);
            omElement.setText(wsMapElement.getValue());
            omDocument.addChild(omElement);
        }
        omDocument.
        return omDocument;  //To change body of created methods use File | Settings | File Templates.
    }
}
