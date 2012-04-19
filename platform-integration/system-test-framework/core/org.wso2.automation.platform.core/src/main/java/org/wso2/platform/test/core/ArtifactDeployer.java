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
import org.wso2.platform.test.core.utils.ArtifactAssociation;
import org.wso2.platform.test.core.utils.ArtifactDependency;
import org.wso2.platform.test.core.utils.ArtifactDeployerUtil;
import org.wso2.platform.test.core.utils.ArtifactType;
import org.wso2.platform.test.core.utils.UnknownArtifactTypeException;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentVariables;
import org.wso2.platform.test.core.utils.frameworkutils.FrameworkProperties;

import java.io.File;
import java.net.URL;
import java.util.List;

/**
 * This class deploys the test artifacts defined under each scenarios in testConfig-new.xml file.
 */
public class ArtifactDeployer {
    private static final Log log = LogFactory.getLog(ArtifactDeployer.class);

    /**
     * Deploy each test artifact defined in the scenario configuration
     *
     * @param userId                  userID of the user the artifact is to be deployed by.
     * @param productName             Name of the product which artifacts will be deployed
     * @param artifactName            Name of the test artifacts.
     * @param type                    artifact type
     * @param artifactDependencyList  list of dependencies the artifact needs.
     * @param artifactAssociationList list of associations that is specific to particular artifact
     * @param frameworkProperties     test framework properties
     * @throws Exception - Artifact deployment exception
     */
    protected void deployArtifact(int userId, String productName, String artifactName,
                                  ArtifactType type,
                                  List<ArtifactDependency> artifactDependencyList,
                                  List<ArtifactAssociation> artifactAssociationList,
                                  FrameworkProperties frameworkProperties) throws Exception {

        ArtifactDeployerUtil deployerUtil = new ArtifactDeployerUtil();
        EnvironmentBuilder builder = new EnvironmentBuilder();
        EnvironmentVariables environmentVariables;
        String artifactLocation = ProductConstant.getResourceLocations(productName);
        environmentVariables = getEnvironment(userId, productName, deployerUtil, builder);

        String sessionCookie = environmentVariables.getSessionCookie();
        String backendURL = environmentVariables.getBackEndUrl();
        log.debug("Server backend URL " + backendURL);

        if (artifactDependencyList.size() == 0) {
            log.info("No dependencies found for the artifact");
        }

        String filePath;
        switch (type) {
            case aar:
                filePath = artifactLocation + File.separator + "aar" + File.separator + artifactName;
                deployerUtil.aarFileUploder(sessionCookie, backendURL, artifactName, filePath);
                break;

            case car:
                URL url = new URL("file:///" + artifactLocation + File.separator + "car" +
                                  File.separator + artifactName);
                deployerUtil.carFileUploder(sessionCookie, backendURL, url, artifactName);
                break;

            case war:
                filePath = artifactLocation + File.separator + "war" + File.separator + artifactName;
                deployerUtil.warFileUploder(sessionCookie, backendURL, filePath);
                break;

            case jar:
                deployerUtil.jarFileUploder(sessionCookie, backendURL, artifactLocation, artifactName
                        , artifactDependencyList, artifactAssociationList);
                break;

            case bpelzip:
                filePath = artifactLocation + File.separator + "bpel";
                deployerUtil.bpelFileUploader(sessionCookie, backendURL, filePath, artifactName);
                break;

            case jaxws:
                filePath = artifactLocation + File.separator + "jaxws" + File.separator + artifactName;
                deployerUtil.jaxwsFileUploader(sessionCookie, backendURL, artifactName, filePath);
                break;

            case ruleservice:
                filePath = artifactLocation + File.separator + "ruleservice" + File.separator + artifactName;
                deployerUtil.brsFileUploader(sessionCookie, artifactName, filePath, backendURL);
                break;

            case jszip:
                filePath = artifactLocation + File.separator + "jszip" + File.separator + artifactName;
                deployerUtil.javaScripServiceUploder(sessionCookie, artifactName, filePath, backendURL);
                break;

            case spring:
                deployerUtil.springServiceUpload(sessionCookie, artifactName, artifactLocation,
                                                 artifactDependencyList, artifactAssociationList, backendURL);
                break;

            case dbs:
                deployerUtil.dbsFileUploader(sessionCookie, backendURL, artifactName, artifactLocation,
                                             artifactDependencyList, artifactAssociationList,
                                             frameworkProperties, userId);
                break;

            case synapseconfig:
                deployerUtil.updateESBConfiguration(sessionCookie, backendURL, artifactName,
                                                    artifactLocation, productName);
                break;

            default:
                log.error("Unknown artifact type found.");
                throw new UnknownArtifactTypeException("Unknown artifact type found.");
        }
    }

    private EnvironmentVariables getEnvironment(int userId, String productName,
                                                ArtifactDeployerUtil deployerUtil,
                                                EnvironmentBuilder builder) {
        EnvironmentVariables environmentVariables;
        if (builder.getFrameworkSettings().getEnvironmentSettings().isClusterEnable()) {
            environmentVariables = deployerUtil.getClusterEnvironment(productName, userId);
        } else {
            environmentVariables = deployerUtil.getProductEnvironment(productName, userId);
        }
        return environmentVariables;
    }
}
