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
package org.wso2.carbon.automation.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.automation.core.utils.Artifact;
import org.wso2.carbon.automation.core.utils.ArtifactAssociation;
import org.wso2.carbon.automation.core.utils.ArtifactDependency;
import org.wso2.carbon.automation.core.utils.ArtifactType;
import org.wso2.carbon.automation.core.utils.ProductConfig;
import org.wso2.carbon.automation.core.utils.ScenarioConfigurationParser;
import org.wso2.carbon.automation.core.utils.frameworkutils.FrameworkFactory;
import org.wso2.carbon.automation.core.utils.frameworkutils.FrameworkProperties;

import java.util.List;

/**
 * Managing artifact deployment and removing.
 */
public class ArtifactManager {
    private static final Log log = LogFactory.getLog(ArtifactManager.class);
    private static ArtifactManager artifactManagerInstance;
    private ScenarioConfigurationParser scenarioConfig;
    private ArtifactDeployer deployer;
    private ArtifactCleaner cleaner;

    private ArtifactManager() throws Exception {
        deployer = new ArtifactDeployer();
        cleaner = new ArtifactCleaner();
        scenarioConfig = ScenarioConfigurationParser.getInstance();
    }

    /**
     * deploying artifacts before executing the test case
     *
     * @param testCaseName test class name is being executed.
     * @throws Exception - artifact deployment exception
     */
    public void deployArtifacts(String testCaseName) throws Exception {
        log.info("Deploying Artifacts required for scenario...");
        artifactTraverser(true, testCaseName);
    }

    public static ArtifactManager getInstance() throws Exception {
        if (artifactManagerInstance == null) {
            artifactManagerInstance = new ArtifactManager();
        }
        return artifactManagerInstance;
    }

    /**
     * Clean artifacts after each test run
     *
     * @param testCaseName - testClass name is being executed.
     * @throws Exception - throws artifact removal exception
     */
    public void cleanArtifacts(String testCaseName) throws Exception {
        log.info("Cleaning Artifacts...");
        artifactTraverser(false, testCaseName);
    }

    /**
     * Traverse though product configuration list and call for appropriate deployers and undeployers based on
     * the provided deployment status
     *
     * @param deploymentStatus true or false to check execute artifact deployer or undeployer
     * @param testCaseName     testClass name is being executed.
     * @throws Exception if artifact deployment error occurred.
     */
    private void artifactTraverser(boolean deploymentStatus, String testCaseName)
            throws Exception {

        List<ProductConfig> productConfig = scenarioConfig.getProductConfigList(testCaseName);
        if (productConfig != null) {
            for (ProductConfig aProductConfig : productConfig) {
                List<Artifact> artifactList = aProductConfig.getProductArtifactList();

                for (Artifact anArtifact : artifactList) {

                    List<ArtifactDependency> artifactDependencyList = anArtifact.getDependencyArtifactList();
                    List<ArtifactAssociation> artifactAssociationList = anArtifact.getAssociationList();

                    if (deploymentStatus) {
                        log.info("Deploying : " + anArtifact.getArtifactName() + " of type : "
                                 + anArtifact.getArtifactType().toString().toUpperCase()
                                 + " on :" + aProductConfig.getProductName().toUpperCase()
                                 + " by user :" + anArtifact.getUserId());
                        deployArtifactByType(anArtifact, aProductConfig.getProductName());

                    } else {
                        log.info("UnDeploying :" + anArtifact.getArtifactName() + " of type : "
                                 + anArtifact.getArtifactType().toString().toUpperCase() +
                                 " on :" + aProductConfig.getProductName().toUpperCase()
                                 + " by user : " + anArtifact.getUserId());
                        cleanArtifactByType(anArtifact.getArtifactType(),
                                            aProductConfig.getProductName(),
                                            anArtifact.getArtifactName(),
                                            anArtifact.getUserId(),
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
     * @param productName name of the product
     * @param artifact    artifact object to be deployed
     * @throws Exception if artifact deployment error
     */
    private void deployArtifactByType(Artifact artifact, String productName)
            throws Exception {

        FrameworkProperties frameworkProperties = FrameworkFactory.getFrameworkProperties(productName);
        deployer.deployArtifact(productName, artifact, frameworkProperties);

    }
}
