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
package org.wso2.platform.test.core.utils;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.platform.test.core.ProductConstant;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class ScenarioConfigurationParser {

    private static Log log = LogFactory.getLog(ScenarioConfigurationParser.class);

    private static ScenarioConfigurationParser instance = new ScenarioConfigurationParser();
    private TestScenarioConfig testScenarioConfig = new TestScenarioConfig();

    public ScenarioConfigurationParser() {
        readConfig();
    }

    public static ScenarioConfigurationParser getInstance() {
        return instance;
    }

    private void readConfig() {


        if (ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION == null) {
            log.error("System property: system.test.sample.location cannot be null");
        }

        String testConfigFilePath = ArtifactReader.SYSTEM_TEST_RESOURCE_LOCATION +
                                    File.separator + "conf" + File.separator + "testconfig-new.xml";

        OMElement documentElement;
        FileInputStream inputStream = null;
        File file = new File(testConfigFilePath);
        if (file.exists()) {
            try {
                inputStream = new FileInputStream(testConfigFilePath);
                XMLStreamReader parser = XMLInputFactory.newInstance().createXMLStreamReader(inputStream);
                StAXOMBuilder builder = new StAXOMBuilder(parser);
                documentElement = builder.getDocumentElement();

                processScenarioConfigurationElements(documentElement);
            } catch (FileNotFoundException ignored) {
                log.error("Config file not found");
            } catch (XMLStreamException ignored) {
                log.error("Error reading config file");
            } finally {
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (IOException ignored) {
                    log.warn("Unable to close the file input stream created for config file");
                }
            }
        }
    }

    private void processScenarioConfigurationElements(OMElement documentElement) {
        for (Iterator itrScenario = documentElement.getChildrenWithName(new QName("scenario")); itrScenario.hasNext(); ) {
            OMElement OmClassDocument = (OMElement) itrScenario.next();

            OMAttribute testMethodAttr = OmClassDocument.getAttribute(new QName("testName"));

            List<ProductConfig> productMapList = new ArrayList<ProductConfig>();
            for (Iterator itrProduct = OmClassDocument.getChildrenWithName(new QName("product")); itrProduct.hasNext(); ) {
                OMElement omProduct = (OMElement) itrProduct.next();
                OMAttribute omProductAttr = omProduct.getAttribute(new QName("name"));

                ProductConfig productConfig = new ProductConfig();
                productConfig.setProductName(omProductAttr.getAttributeValue());


                List<Artifact> artifactMapList = new ArrayList<Artifact>();

                for (Iterator itrArtifacts = omProduct.getChildrenWithName(new QName("artifacts"));
                     itrArtifacts.hasNext(); ) {
                    OMElement omArtifacts = (OMElement) itrArtifacts.next();
                    OMAttribute omUserIdAttr = omArtifacts.getAttribute(new QName("userId"));

                    for (Iterator itrArtifact = omArtifacts.getChildrenWithName(new QName("artifact"));
                         itrArtifact.hasNext(); ) {
                        OMElement omArtifact = (OMElement) itrArtifact.next();
                        OMAttribute omArtifactNameAttr = omArtifact.getAttribute(new QName("name"));
                        OMAttribute omArtifactTypeAttr = omArtifact.getAttribute(new QName("type"));

                        Artifact artifact = new Artifact();

                        artifact.setArtifactName(omArtifactNameAttr.getAttributeValue());
                        artifact.setArtifactType(ArtifactTypeFactory.getType(omArtifactTypeAttr.getAttributeValue()));
                        artifact.setUserId(Integer.parseInt(omUserIdAttr.getAttributeValue()));


                        List<ArtifactDependency> depMapList = new ArrayList<ArtifactDependency>();
                        List<ArtifactAssociation> assoMapList = new ArrayList<ArtifactAssociation>();

                        for (Iterator itrDependency = omArtifact.getChildrenWithName(new QName("dependency"));
                             itrDependency.hasNext(); ) {
                            OMElement omDependency = (OMElement) itrDependency.next();
                            OMAttribute omDependencyNameAttr = omDependency.getAttribute(new QName("name"));
                            OMAttribute omDependencyTypeAttr = omDependency.getAttribute(new QName("dependencyType"));

                            ArtifactDependency artifactDependencies = new ArtifactDependency();


                            if (omDependencyNameAttr != null && omDependencyTypeAttr != null) {
                                artifactDependencies.setDepArtifactName(omDependencyNameAttr.getAttributeValue());
                                artifactDependencies.setDepArtifactType(ArtifactTypeFactory.
                                        getType(omDependencyTypeAttr.getAttributeValue()));
                                depMapList.add(artifactDependencies);
                                artifact.setDependencyArtifactList(depMapList);
                            } else {
                                artifact.setDependencyArtifactList(null);
                            }

                        }

                        for (Iterator itrAssociation = omArtifact.getChildrenWithName(new QName("association")); itrAssociation.hasNext(); ) {
                            OMElement omAssociation = (OMElement) itrAssociation.next();
                            OMAttribute omAssoNameAttr = omAssociation.getAttribute(new QName("name"));
                            OMAttribute omAssoValueAttr = omAssociation.getAttribute(new QName("value"));

                            ArtifactAssociation association = new ArtifactAssociation();

                            if (omAssoNameAttr != null && omAssoValueAttr != null) {
                                association.setAssociationName(omAssoNameAttr.getAttributeValue());
                                association.setAssociationValue(omAssoValueAttr.getAttributeValue());
                                assoMapList.add(association);
                                artifact.setAssociationList(assoMapList);
                            } else {
                                artifact.setAssociationList(null);
                            }

                        }
                        artifactMapList.add(artifact);
                    }

                    productConfig.setProductArtifactList(artifactMapList);
                }
                productMapList.add(productConfig);
            }
            testScenarioConfig.setProductConfigMap(testMethodAttr.getAttributeValue(), productMapList);
        }
    }

    public List<ProductConfig> getProductConfigList(String testMethod) {
        return testScenarioConfig.getProductConfigMap(testMethod);
    }
}
