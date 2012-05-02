package org.wso2.carbon.bam.gadgetgenwizard.service;

import org.apache.axiom.om.*;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.impl.jaxp.OMSource;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import java.io.*;

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

    public static final String GADGET_GEN_TRANSFORM_XSLT = "gadget-gen-transform.xslt";

    private static final Log log = LogFactory.getLog(GadgetGenService.class);
    public static final String JAGGERY_APP_DIR = CarbonUtils.getCarbonRepository()  + "jaggeryapps";
    public static final String GADGET_GEN_APP_DIR = JAGGERY_APP_DIR + File.separator + "gadgetgen";
    public static final String GADGETGEN_JAG_FILEPATH = GADGET_GEN_APP_DIR + File.separator + "gadgetgen.jag";

    public String createGadget(WSMap map) throws GadgetGenException {
        OMDocument intermediateXML = createIntermediateXML(map);
        OMElement gadgetXML = applyXSLTForGadgetXML(intermediateXML);
        OutputStream jagFile = applyXSLTForJaggeryScript(intermediateXML);
        return null;
    }

    private OutputStream applyXSLTForJaggeryScript(OMDocument intermediateXML) throws GadgetGenException {


        try {
            InputStream xsltStream = this.getClass().getClassLoader().getResourceAsStream(GADGET_GEN_TRANSFORM_XSLT);
            StAXOMBuilder stAXOMBuilder = new StAXOMBuilder(xsltStream);

            Source xsltSource = new OMSource(stAXOMBuilder.getDocumentElement());
            Source xmlSource = new OMSource(intermediateXML.getOMDocumentElement());


            File gadgetGenAppDir = new File(GADGET_GEN_APP_DIR);
            if (!gadgetGenAppDir.exists()) {
                 FileUtils.forceMkdir(gadgetGenAppDir);
            }
            File gadgetGenFile = new File(GADGETGEN_JAG_FILEPATH);
            if (gadgetGenFile.exists()) {
                FileUtils.forceDelete(gadgetGenFile);
            }

            StreamResult result = new StreamResult(FileUtils.openOutputStream(gadgetGenFile));
            TransformerFactory transFact =
                    TransformerFactory.newInstance();
            Transformer transformer = transFact.newTransformer(xsltSource);
            transformer.transform(xmlSource, result);
        } catch (XMLStreamException e) {
            String error = "XML error reading XSLT file.";
            log.error(error, e);
            throw new GadgetGenException(error, e);
        } catch (FileNotFoundException e) {
            String error = "XSLT file not found. This should be in the classpath.";
            log.error(error, e);
            throw new GadgetGenException(error, e);
        } catch (TransformerException e) {
           String error = "XSLT transformation error during Code generation.";
            log.error(error, e);
            throw new GadgetGenException(error, e);
        } catch (IOException e) {
            String error = "Error creating directory structure for jaggery app";
            log.error(error, e);
            throw new GadgetGenException(error, e);
        }

        return null;  //To change body of created methods use File | Settings | File Templates.
    }

    private OMElement applyXSLTForGadgetXML(OMDocument intermediateXML) {
        return null;  //To change body of created methods use File | Settings | File Templates.
    }

    private OMDocument createIntermediateXML(WSMap map) {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMDocument omDocument = factory.createOMDocument();
        OMNamespace gadgetgenNamespace = factory.createOMNamespace("http://wso2.com/bam/gadgetgen", "gg");
        OMElement rootElement = factory.createOMElement("gadgetgen", gadgetgenNamespace);
        WSMapElement[] wsMapElements = map.getWsMapElements();
        boolean barChartFound = false;
        OMElement barChart = factory.createOMElement("BarChart", gadgetgenNamespace);
        for (int i = 0; i < wsMapElements.length; i++) {
            WSMapElement wsMapElement = wsMapElements[i];

            // check for bar graph properties
            if (wsMapElement.getKey().startsWith("bar")) {
                barChartFound = true;
                OMElement omElement = factory.createOMElement(wsMapElement.getKey(), gadgetgenNamespace);
                omElement.setText(wsMapElement.getValue());
                barChart.addChild(omElement);
            }

            OMElement omElement = factory.createOMElement(wsMapElement.getKey(), gadgetgenNamespace);
            omElement.setText(wsMapElement.getValue());
            rootElement.addChild(omElement);
        }
        if (barChartFound) {
           rootElement.addChild(barChart);
        }
        omDocument.addChild(rootElement);
        return omDocument;  //To change body of created methods use File | Settings | File Templates.
    }
}
