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
import org.wso2.platform.test.core.utils.Artifact;
import org.wso2.platform.test.core.utils.ArtifactAssociation;
import org.wso2.platform.test.core.utils.ArtifactDependency;
import org.wso2.platform.test.core.utils.ArtifactType;
import org.wso2.platform.test.core.utils.ProductConfig;
import org.wso2.platform.test.core.utils.ScenarioConfigurationParser;
import org.wso2.platform.test.core.utils.frameworkutils.FrameworkFactory;
import org.wso2.platform.test.core.utils.frameworkutils.FrameworkProperties;

import java.util.List;

/**
 * Managing artifact deployment and removing.
 */
public class ArtifactManager {
    private static final Log log = LogFactory.getLog(ArtifactManager.class);
    private ScenarioConfigurationParser scenarioConfig;
    private ArtifactDeployer deployer;
    private ArtifactCleaner cleaner;
    public static final boolean ARTIFACT_DEPLOYMENT_STATUS = true;
    public static final boolean ARTIFACT_CLEANER_STATUS = false;
    private String testCaseName = null;

    public ArtifactManager(String testCaseName) {
        this.testCaseName = testCaseName;
        deployer = new ArtifactDeployer();
        cleaner = new ArtifactCleaner();
        scenarioConfig = ScenarioConfigurationParser.getInstance();
    }

    /**
     * deploying artifacts before executing the test case
     *
     * @throws Exception - artifact deployment exception
     */
    public void deployArtifacts() throws Exception {
        log.info("Deploying Artifacts required for scenario...");
        artifactTraverser(ARTIFACT_DEPLOYMENT_STATUS);
    }

    /**
     * Clean artifacts after each test run
     *
     * @throws Exception - throws artifact removal exception
     */
    public void cleanArtifacts() throws Exception {
        log.info("Cleaning Artifacts...");
        artifactTraverser(ARTIFACT_CLEANER_STATUS);
    }

    /**
     * Traverse though product configuration list and call for appropriate deployers and undeployers based on
     * the provided deployment status
     *
     * @param deploymentStatus true or false to check execute artifact deployer or undeployer
     * @throws Exception if artifact deployment error occurred.
     */
    private void artifactTraverser(boolean deploymentStatus)
            throws Exception {

        List<ProductConfig> productConfig = scenarioConfig.getProductConfigList(testCaseName);
        if (productConfig != null) {
            for (ProductConfig aProductConfig : productConfig) {
                List<Artifact> artifactList = aProductConfig.getProductArtifactList();

                for (Artifact anArtifactList : artifactList) {

                    List<ArtifactDependency> artifactDependencyList = anArtifactList.getDependencyArtifactList();
                    List<ArtifactAssociation> artifactAssociationList = anArtifactList.getAssociationList();

                    if (deploymentStatus) {
                        log.info("Deploying : " + anArtifactList.getArtifactName() + " of type : "
                                 + anArtifactList.getArtifactType().toString().toUpperCase()
                                 + " on :" + aProductConfig.getProductName().toUpperCase()
                                 + " by user :" + anArtifactList.getUserId());
                        deployArtifactByType(anArtifactList.getArtifactType(),
                                             aProductConfig.getProductName(),
                                             anArtifactList.getArtifactName(),
                                             anArtifactList.getUserId(),
                                             artifactDependencyList, artifactAssociationList);

                    } else {
                        log.info("UnDeploying :" + anArtifactList.getArtifactName() + " of type : "
                                 + anArtifactList.getArtifactType().toString().toUpperCase() +
                                 " on :" + aProductConfig.getProductName().toUpperCase()
                                 + " by user : " + anArtifactList.getUserId());
                        cleanArtifactByType(anArtifactList.getArtifactType(),
                                            aProductConfig.getProductName(),
                                            anArtifactList.getArtifactName(),
                                            anArtifactList.getUserId(),
                                            artifactDependencyList, artifactAssociationList);
                    }
                }
            }
        }
    }

    /**
     * calls for artifact undeployment after test scenario execution.
     *
     * @param type                    artifact type
     * @param productName             name of the product
     * @param artifactName            name of the artifact to be undeployed
     * @param userId                  user do the undeployment
     * @param artifactDependencyList  artifact dependencies if any
     * @param artifactAssociationList artifact associations if any
     * @throws Exception if artifact undeployment error
     */
    private void cleanArtifactByType(ArtifactType type, String productName, String artifactName,
                                     int userId, List<ArtifactDependency> artifactDependencyList,
                                     List<ArtifactAssociation> artifactAssociationList)
            throws Exception {

        FrameworkProperties frameworkProperties = FrameworkFactory.getFrameworkProperties(productName);
        cleaner.cleanArtifact(userId, productName, artifactName, type, artifactDependencyList,
                              artifactAssociationList, frameworkProperties);

    }

    /**
     * calls for artifact deployment before test scenario execution.
     *
     * @param type                    artifact type
     * @param productName             name of the product
     * @param artifactName            name of the artifact to be deployed
     * @param userId                  user do the deployment
     * @param artifactDependencyList  artifact dependencies if any
     * @param artifactAssociationList artifact associations if any
     * @throws Exception if artifact deployment error
     */
    private void deployArtifactByType(ArtifactType type, String productName, String artifactName,
                                      int userId,
                                      List<ArtifactDependency> artifactDependencyList,
                                      List<ArtifactAssociation> artifactAssociationList)
            throws Exception {

        FrameworkProperties frameworkProperties = FrameworkFactory.getFrameworkProperties(productName);
        deployer.deployArtifact(userId, productName, artifactName, type, artifactDependencyList,
                                artifactAssociationList, frameworkProperties);

    }
}
