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
package org.wso2.platform.test.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.jarservices.stub.DuplicateServiceExceptionException;
import org.wso2.carbon.jarservices.stub.DuplicateServiceGroupExceptionException;
import org.wso2.carbon.jarservices.stub.JarUploadExceptionException;
import org.wso2.carbon.rule.service.stub.fileupload.ExceptionException;
import org.wso2.platform.test.core.utils.*;
import org.wso2.platform.test.core.utils.frameworkutils.FrameworkFactory;
import org.wso2.platform.test.core.utils.frameworkutils.FrameworkProperties;

import java.rmi.RemoteException;
import java.util.List;

public class ArtifactManager {
    private static final Log log = LogFactory.getLog(ArtifactManager.class);
    ScenarioConfigurationParser scenarioConfig = ScenarioConfigurationParser.getInstance();
    private ArtifactDeployer deployer = new ArtifactDeployer();
    private ArtifactCleaner cleaner = new ArtifactCleaner();
    public static final boolean ARTIFACT_DEPLOYMENT_STATUS = true;
    public static final boolean ARTIFACT_CLEANER_STATUS = false;
    private String testCaseName = null;

    public ArtifactManager(String testCaseName) {
        this.testCaseName = testCaseName;

    }

    public void deployArtifacts() throws Exception, UnknownArtifactTypeException {
        log.info("Deploying Artifacts required for scenario...");
        artifactTraverser(ARTIFACT_DEPLOYMENT_STATUS);
    }

    public void cleanArtifacts() throws Exception, UnknownArtifactTypeException {
        log.info("Cleaning Artifacts...");
        artifactTraverser(ARTIFACT_CLEANER_STATUS);

    }

    private void cleanArtifactByType(ArtifactType type, String productName, String artifactName,
                                     int userId,
                                     List<ArtifactDependency> artifactDependencyList,
                                     List<ArtifactAssociation> artifactAssociationList) throws
                                                                                        UnknownArtifactTypeException,
                                                                                        Exception {

        FrameworkProperties frameworkProperties = FrameworkFactory.getFrameworkProperties(productName);
        cleaner.cleanArtifact(userId, productName, artifactName, type, artifactDependencyList, artifactAssociationList, frameworkProperties);

    }

    private void deployArtifactByType(ArtifactType type, String productName, String artifactName,
                                      int userId,
                                      List<ArtifactDependency> artifactDependencyList,
                                      List<ArtifactAssociation> artifactAssociationList) throws
                                                                                         UnknownArtifactTypeException,
                                                                                         Exception,
                                                                                         DuplicateServiceGroupExceptionException,
                                                                                         JarUploadExceptionException,
                                                                                         RemoteException,
                                                                                         DuplicateServiceExceptionException,
                                                                                         ExceptionException,
                                                                                         org.wso2.carbon.mashup.jsservices.stub.fileupload.ExceptionException,
                                                                                         InterruptedException {



        FrameworkProperties frameworkProperties = FrameworkFactory.getFrameworkProperties(productName);
        deployer.deployArtifact(userId, productName, artifactName, type, artifactDependencyList, artifactAssociationList, frameworkProperties);

    }


    private void artifactTraverser(boolean deploymentStatus)
            throws UnknownArtifactTypeException, Exception {

        List<ProductConfig> productConfig = scenarioConfig.getProductConfigList(testCaseName);
        if (productConfig != null) {
            for (int i = 0, n = productConfig.size(); i < n; i++) {
                List<Artifact> artifactList = productConfig.get(i).getProductArtifactList();

                for (int a = 0, n1 = artifactList.size(); a < n1; a++) {

                    List<ArtifactDependency> artifactDependencyList = artifactList.get(a).getDependencyArtifactList();
                    List<ArtifactAssociation> artifactAssociationList = artifactList.get(a).getAssociationList();

                    if (deploymentStatus) {
                        log.info("Deploying : " + artifactList.get(a).getArtifactName() + " of type : "
                                 + artifactList.get(a).getArtifactType().toString().toUpperCase()
                                 + " on :" + productConfig.get(i).getProductName().toUpperCase()
                                 + " by user :" + artifactList.get(a).getUserId());
                        deployArtifactByType(artifactList.get(a).getArtifactType(),
                                             productConfig.get(i).getProductName(),
                                             artifactList.get(a).getArtifactName(),
                                             artifactList.get(a).getUserId(),
                                             artifactDependencyList, artifactAssociationList);

                    } else {
                        log.info("UnDeploying :" + artifactList.get(a).getArtifactName() + " of type : "
                                 + artifactList.get(a).getArtifactType().toString().toUpperCase() +
                                 " on :" + productConfig.get(i).getProductName().toUpperCase()
                                 + " by user : " + artifactList.get(a).getUserId());
                        cleanArtifactByType(artifactList.get(a).getArtifactType(),
                                            productConfig.get(i).getProductName(),
                                            artifactList.get(a).getArtifactName(),
                                            artifactList.get(a).getUserId(),
                                            artifactDependencyList, artifactAssociationList);
                    }

                    for (int b = 0, n2 = artifactDependencyList.size(); b < n2; b++) {
//                        System.out.println(artifactDependencyList.get(b).getDepArtifactName());
//                        System.out.println(artifactDependencyList.get(b).getDepArtifactType());
                    }

                    for (int asso = 0, size = artifactAssociationList.size(); asso < size; asso++) {
//                        System.out.println(artifactAssociationList.get(asso).getAssociationName());
//                        System.out.println(artifactAssociationList.get(asso).getAssociationValue());
                    }
                }
            }
        }
    }
}
