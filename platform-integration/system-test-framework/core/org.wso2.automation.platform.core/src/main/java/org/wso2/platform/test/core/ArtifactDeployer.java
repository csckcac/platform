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
     * @param productName         Name of the product which artifacts will be deployed
     * @param artifact            test artifacts Object.
     * @param frameworkProperties test framework properties
     * @throws Exception - Artifact deployment exception
     */
    protected void deployArtifact(String productName, Artifact artifact,
                                  FrameworkProperties frameworkProperties) throws Exception {

        ArtifactDeployerUtil deployerUtil = new ArtifactDeployerUtil();
        EnvironmentBuilder builder = new EnvironmentBuilder();
        EnvironmentVariables environmentVariables;
        String artifactLocation = ProductConstant.getResourceLocations(productName);
        environmentVariables = getEnvironment(artifact.getUserId(), productName, deployerUtil, builder);

        String sessionCookie = environmentVariables.getSessionCookie();
        String backendURL = environmentVariables.getBackEndUrl();
        log.debug("Server backend URL " + backendURL);

        if (artifact.getDependencyArtifactList().size() == 0) {
            log.info("No dependencies found for the artifact");
        }

        String filePath;
        switch (artifact.getArtifactType()) {
            case aar:
                filePath = artifactLocation + File.separator + "aar" + File.separator + artifact.getArtifactName();
                deployerUtil.aarFileUploder(sessionCookie, backendURL, artifact.getArtifactName(), filePath);
                break;

            case car:
                URL url = new URL("file:///" + artifactLocation + File.separator + "car" +
                                  File.separator + artifact.getArtifactName());
                deployerUtil.carFileUploder(sessionCookie, backendURL, url, artifact);
                break;

            case war:
                filePath = artifactLocation + File.separator + "war" + File.separator + artifact.getArtifactName();
                deployerUtil.warFileUploder(sessionCookie, backendURL, filePath);
                break;

            case jar:
                deployerUtil.jarFileUploder(sessionCookie, backendURL, artifactLocation, artifact);
                break;

            case bpelzip:
                filePath = artifactLocation + File.separator + "bpel";
                deployerUtil.bpelFileUploader(sessionCookie, backendURL, filePath, artifact.getArtifactName());
                break;

            case jaxws:
                filePath = artifactLocation + File.separator + "jaxws" + File.separator + artifact.getArtifactName();
                deployerUtil.jaxwsFileUploader(sessionCookie, backendURL, artifact.getArtifactName(), filePath);
                break;

            case ruleservice:
                filePath = artifactLocation + File.separator + "ruleservice" + File.separator + artifact.getArtifactName();
                deployerUtil.brsFileUploader(sessionCookie, artifact.getArtifactName(), filePath, backendURL);
                break;

            case jszip:
                filePath = artifactLocation + File.separator + "jszip" + File.separator + artifact.getArtifactName();
                deployerUtil.javaScripServiceUploder(sessionCookie, artifact.getArtifactName(), filePath, backendURL);
                break;

            case spring:
                deployerUtil.springServiceUpload(sessionCookie, artifact, artifactLocation, backendURL);
                break;

            case dbs:
                deployerUtil.dbsFileUploader(sessionCookie, backendURL, artifact, artifactLocation,
                                             frameworkProperties);
                break;

            case synapseconfig:
                deployerUtil.updateESBConfiguration(sessionCookie, backendURL, artifact.getArtifactName(),
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
