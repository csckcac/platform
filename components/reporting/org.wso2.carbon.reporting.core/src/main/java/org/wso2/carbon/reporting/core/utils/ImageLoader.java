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

package org.wso2.carbon.reporting.core.utils;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.jaxen.JaxenException;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.reporting.api.ReportingException;
import org.wso2.carbon.reporting.util.ReportParamMap;

import javax.activation.DataHandler;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import java.io.*;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class ImageLoader {
    private ArrayList<String> imageNames;
    private String directory = "repository/resources/reporting/images";

    //private static final String IMAGE_JRXML_NAME_KEY = "image_jrxml_template";

    public void loadTempImages(String templateName, String jrxmlContent) throws ReportingException {
        imageNames = getImageNames(jrxmlContent);

        for (String imageName : imageNames) {
            String imagePath = directory + "/" + templateName + "_" + imageName;
            copyImagesToHome(imagePath, imageName);
        }

    }

    public boolean saveImage(String imageName, String reportName, DataHandler imageContent) {
        boolean success = true;

        boolean exists = (new File(directory).exists());
        if (!exists)
            success = (new File(directory)).mkdirs();
        if (success) {
            File destFile = new File(directory + "/" + reportName + "_" + imageName);
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(destFile);
                imageContent.writeTo(fos);
                fos.flush();
                fos.close();
                success = true;
            } catch (FileNotFoundException e) {
                success = false;
            } catch (IOException e) {
                success = false;
            }
        }
        return success;

    }

    private ArrayList<String> getImageNames(String jrxmlContent) throws ReportingException {
        try {
            InputStream is = new ByteArrayInputStream(jrxmlContent.getBytes());
            XMLInputFactory xif = XMLInputFactory.newInstance();
            XMLStreamReader reader = null;
            reader = xif.createXMLStreamReader(is);
            StAXOMBuilder builder = new StAXOMBuilder(reader);
            OMElement docElement = builder.getDocumentElement();

            AXIOMXPath xpathExpression = new AXIOMXPath("//a:title//a:band//a:image//a:imageExpression");
            xpathExpression.addNamespace("a", "http://jasperreports.sourceforge.net/jasperreports");
            List nodeList = xpathExpression.selectNodes(docElement);

            // Iterator iterator = docElement.getChildrenWithLocalName("imageExpression");
            ArrayList<String> imageNames = new ArrayList<String>();
            for (int i = 0; i < nodeList.size(); i++) {
                OMElement element = (OMElement) nodeList.get(i);
                String imageName = element.getText();
                if (imageName != null && !imageName.equalsIgnoreCase("")) {
                    String imageText = imageName.replaceAll("\"", "");
                    imageNames.add(imageText);
                }

            }
            return imageNames;
        } catch (XMLStreamException e) {
            throw new ReportingException(e.getMessage(), e);
        } catch (JaxenException e) {
            throw new ReportingException(e.getMessage(), e);
        }
    }

    private void copyImagesToHome(String imagePath, String imageName) throws ReportingException {

        File src = new File(imagePath);
        File destFile = new File(imageName);
        FileChannel source = null;
        FileChannel destination = null;
        try {
            destFile.createNewFile();
            source = new FileInputStream(src).getChannel();
            destination = new FileOutputStream(destFile).getChannel();

            long count = 0;
            long size = source.size();
            while ((count += destination.transferFrom(source, 0, size - count)) < size) ;

            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        } catch (IOException e) {
           throw new ReportingException(e.getMessage(), e);
        }

    }

    public void deleteTempImages() {
        for (String imageName : imageNames) {
            File file = new File(imageName);
            file.delete();
        }
    }


}
