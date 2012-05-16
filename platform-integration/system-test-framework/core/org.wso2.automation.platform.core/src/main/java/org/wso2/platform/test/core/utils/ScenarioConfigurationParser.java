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

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Parse the testconfig.xml and store the scenarios in TestScenarioConfig object.
 */
public class ScenarioConfigurationParser {

    private static Log log = LogFactory.getLog(ScenarioConfigurationParser.class);

    private static ScenarioConfigurationParser instance;
    private static final String SCENARIO = "scenario";
    private static final String TEST_NAME = "testName";
    private static final String PRODUCT = "product";
    private static final String PRODUCT_NAME = "name";
    private static final String ARTIFACTS = "artifacts";
    private static final String USER_ID = "userId";
    private static final String ARTIFACT = "artifact";
    private static final String ARTIFACT_NAME = "name";
    private static final String LOCATION = "location";
    private static final String ARTIFACT_TYPE = "type";
    private static final String DEPENDENCY = "dependency";
    private static final String DEPENDENCY_NAME = "name";
    private static final String DEPENDENCY_TYPE = "dependencyType";
    private static final String ASSOCIATION = "association";
    private static final String ASSOCIATION_NAME = "name";
    private static final String ASSOCIATION_VALUE = "value";
    private static final String PRODUCTS = "products";
    private static TestScenarioConfig testScenarioConfig = new TestScenarioConfig();

    private ScenarioConfigurationParser() {
    }

    public static ScenarioConfigurationParser getInstance() throws Exception {
        if (instance == null) {
            readConfig();
            instance = new ScenarioConfigurationParser();
        }
        return instance;
    }

    private static void readConfig() throws Exception {
        //get test configuration file path
        String testConfigFilePath = ArtifactReader.SYSTEM_TEST_RESOURCE_LOCATION +
                                    File.separator + "conf" + File.separator + "testconfig.xml";
        //parse the test configuration xml file
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

            } catch (FileNotFoundException e) {
                String msg = "Config file not found";
                log.error(msg, e);
                throw new Exception(msg, e);

            } catch (XMLStreamException e) {
                String msg = "Error reading config file";
                log.error("Error reading config file");
                throw new XMLStreamException(msg, e);

            } finally {
                closeStream(inputStream);
            }
        }
    }

    private static void closeStream(FileInputStream inputStream) throws IOException {
        try {
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (IOException e) {
            String msg = "Unable to close the file input stream created for config file";
            log.warn(msg + " " + e);
            throw new IOException(msg, e);
        }
    }

    /**
     * iterate though the document and store elements to testScenarioConfig object
     *
     * @param documentElement - OMElement of processed test configuration file.
     */
    private static void processScenarioConfigurationElements(OMElement documentElement) {
        for (Iterator itrScenario = documentElement.getChildrenWithName(new QName(SCENARIO));
             itrScenario.hasNext(); ) {
            OMElement OmClassDocument = (OMElement) itrScenario.next();

            OMAttribute testMethodAttr = OmClassDocument.getAttribute(new QName(TEST_NAME));

            for (Iterator itrProducts = OmClassDocument.getChildrenWithName(new QName(PRODUCTS));
                 itrProducts.hasNext(); ) {
                OMElement omProducts = (OMElement) itrProducts.next();

                List<ProductConfig> productMapList = new ArrayList<ProductConfig>();
                for (Iterator itrProduct = omProducts.getChildrenWithName(new QName(PRODUCT));
                     itrProduct.hasNext(); ) {
                    OMElement omProduct = (OMElement) itrProduct.next();
                    OMAttribute omProductAttr = omProduct.getAttribute(new QName(PRODUCT_NAME));

                    ProductConfig productConfig = new ProductConfig();
                    productConfig.setProductName(omProductAttr.getAttributeValue());

                    List<Artifact> artifactMapList = new ArrayList<Artifact>();

                    for (Iterator itrArtifacts = omProduct.getChildrenWithName(new QName(ARTIFACTS));
                         itrArtifacts.hasNext(); ) {
                        OMElement omArtifacts = (OMElement) itrArtifacts.next();
                        OMAttribute omUserIdAttr = omArtifacts.getAttribute(new QName(USER_ID));

                        for (Iterator itrArtifact = omArtifacts.getChildrenWithName(new QName(ARTIFACT));
                             itrArtifact.hasNext(); ) {
                            OMElement omArtifact = (OMElement) itrArtifact.next();
                            OMAttribute omArtifactNameAttr = omArtifact.getAttribute(new QName
                                                                                     (ARTIFACT_NAME));
                            OMAttribute omArtifactTypeAttr = omArtifact.getAttribute(new QName
                                                                                     (ARTIFACT_TYPE));
                            OMAttribute omArtifactLocationAttr = omArtifact.getAttribute(new
                                                                                         QName(LOCATION));

                            Artifact artifact = new Artifact();

                            artifact.setArtifactName(omArtifactNameAttr.getAttributeValue());
                            if (omArtifactLocationAttr != null) {
                                artifact.setArtifactLocation(omArtifactLocationAttr.getAttributeValue());
                            } else {
                                artifact.setArtifactLocation("");
                            }
                            artifact.setArtifactType(ArtifactTypeFactory.getType(omArtifactTypeAttr.getAttributeValue()));
                            artifact.setUserId(Integer.parseInt(omUserIdAttr.getAttributeValue()));


                            List<ArtifactDependency> depMapList = new ArrayList<ArtifactDependency>();
                            List<ArtifactAssociation> assoMapList = new ArrayList<ArtifactAssociation>();

                            for (Iterator itrDependency = omArtifact.getChildrenWithName(new QName(DEPENDENCY));
                                 itrDependency.hasNext(); ) {
                                OMElement omDependency = (OMElement) itrDependency.next();
                                OMAttribute omDependencyNameAttr = omDependency.getAttribute(new
                                                                                             QName(DEPENDENCY_NAME));
                                OMAttribute omDependencyTypeAttr = omDependency.getAttribute(new
                                                                                             QName(DEPENDENCY_TYPE));
                                OMAttribute omDependencyLocationAttr = omDependency.getAttribute
                                                                                            (new QName(LOCATION));

                                ArtifactDependency artifactDependencies = new ArtifactDependency();

                                if (omDependencyNameAttr != null && omDependencyTypeAttr != null) {
                                    artifactDependencies.setDepArtifactName(omDependencyNameAttr.getAttributeValue());
                                    if (omDependencyLocationAttr != null) {
                                        artifactDependencies.setDepArtifactLocation(omDependencyLocationAttr.getAttributeValue());
                                    } else {
                                        artifactDependencies.setDepArtifactLocation("");
                                    }
                                    artifactDependencies.setDepArtifactType(ArtifactTypeFactory
                                                                                    .getType(omDependencyTypeAttr.getAttributeValue()));
                                    depMapList.add(artifactDependencies);
                                    artifact.setDependencyArtifactList(depMapList);
                                } else {
                                    artifact.setDependencyArtifactList(null);
                                }
                            }

                            for (Iterator itrAssociation = omArtifact.getChildrenWithName(new QName(ASSOCIATION)); itrAssociation.hasNext(); ) {
                                OMElement omAssociation = (OMElement) itrAssociation.next();
                                OMAttribute omAssoNameAttr = omAssociation.getAttribute(new QName(ASSOCIATION_NAME));
                                OMAttribute omAssoValueAttr = omAssociation.getAttribute(new QName(ASSOCIATION_VALUE));

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
    }

    public List<ProductConfig> getProductConfigList(String testMethod) {
        return testScenarioConfig.getProductConfigMap(testMethod);
    }
}
