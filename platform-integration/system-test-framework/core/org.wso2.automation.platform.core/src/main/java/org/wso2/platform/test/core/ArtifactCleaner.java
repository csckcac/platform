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
import org.wso2.carbon.admin.service.AdminServiceAuthentication;
import org.wso2.platform.test.core.utils.*;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentVariables;
import org.wso2.platform.test.core.utils.frameworkutils.FrameworkProperties;

import java.util.List;

public class ArtifactCleaner {
    private static final Log log = LogFactory.getLog(ArtifactCleaner.class);
    static ArtifactCleanerUtil artifactCleanerUtil = new ArtifactCleanerUtil();
    static ArtifactDeployerUtil artifactDeployerUtil = new ArtifactDeployerUtil();

    protected void cleanArtifact(int userId, String productName, String artifactName,
                                 ArtifactType type,
                                 List<ArtifactDependency> ArtifactDependencyList,
                                 List<ArtifactAssociation> artifactAssociationList,
                                 FrameworkProperties frameworkProperties)
            throws UnknownArtifactTypeException, Exception {

        //  EnvironmentVariables environmentVariables = artifactDeployerUtil.getProductEnvironment(productName, userId);
        EnvironmentBuilder builder = new EnvironmentBuilder();
        EnvironmentVariables environmentVariables = null;
        if (builder.getFrameworkSettings().getEnvironmentSettings().isClusterEnable()) {
            environmentVariables = artifactDeployerUtil.getClusterEnvironment(productName, userId);
        } else {
            environmentVariables = artifactDeployerUtil.getProductEnvironment(productName, userId);
        }
        String sessionCookie = environmentVariables.getSessionCookie();
        String backendURL = environmentVariables.getBackEndUrl();
        log.debug("Server backend URL " + backendURL);

        if (ArtifactDependencyList.size() == 0) {
            log.info("No dependencies found for the artifact");
        }

        switch (type) {
            case aar:
                artifactCleanerUtil.deleteServiceByGroup(sessionCookie, backendURL, artifactName);
                break;

            case car:
                artifactCleanerUtil.deleteMatchingCarArtifact(sessionCookie, backendURL, artifactName);
                break;

            case war:
                artifactCleanerUtil.deleteWebApp(sessionCookie, artifactName, backendURL);
                break;

            case jar:
                artifactCleanerUtil.deleteServiceByGroup(sessionCookie, backendURL, artifactName);
                break;

            case jaxws:
                artifactCleanerUtil.deleteJaxWsWebapp(sessionCookie, backendURL, artifactName);
                break;

            case bpelzip:
                artifactCleanerUtil.deleteBpel(sessionCookie, backendURL, artifactName.substring
                        (0, artifactName.indexOf(".")));
                break;

            case jszip:
                artifactCleanerUtil.deleteServiceByGroup(sessionCookie, backendURL, artifactName);
                break;

            case spring:
                artifactCleanerUtil.deleteAllServicesByType(sessionCookie, ArtifactTypeFactory.getTypeInString
                        (ArtifactType.spring), backendURL);
            case bpel:
                break;
            case dbs:
                artifactCleanerUtil.deleteDataService(sessionCookie, backendURL, artifactName,
                                                      ArtifactDependencyList);
                break;
            case synapseconfig:
                artifactCleanerUtil.restToDefaultConfiguration(sessionCookie, backendURL);
                break;
            case mar:
                break;
            case gar:
                break;
            default:
                log.error("Unknown artifact type found.");
                throw new UnknownArtifactTypeException("Unknown artifact type found.");
        }
    }

    protected static String login(String userName, String password, String backendURL) {
        AdminServiceAuthentication loginClient = new AdminServiceAuthentication(backendURL);
        return loginClient.login(userName, password, backendURL);
    }
}
