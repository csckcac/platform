/*
 * Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.appfactory.core.deploy;

public class ArtifactDeploymentStatusBean {

    String applicationId;
    String stage;
    String version;
    String revision;
    String deploymentServer;

    /**
     * The status of the deployment operation ( i.e. success|failed)
     */
    String status;
    /**
     * Details about the outcome ( e.g. if the operation failed then error
     * message will be here)
     */
    String description;

    public ArtifactDeploymentStatusBean() {
    }

    public ArtifactDeploymentStatusBean(String applicationId, String stage,
                                        String version, String revision,
                                        String deploymentServer, String status,
                                        String statusDescription) {
        super();
        this.applicationId = applicationId;
        this.stage = stage;
        this.version = version;
        this.revision = revision;
        this.deploymentServer = deploymentServer;
        this.status = status;
        this.description = statusDescription;
    }

    public String getDeploymentServer() {
        return deploymentServer;
    }

    public void setDeploymentServer(String deploymentServer) {
        this.deploymentServer = deploymentServer;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusDescription() {
        return description;
    }

    public void setStatusDescription(String statusDescription) {
        this.description = statusDescription;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getRevision() {
        return revision;
    }

    public void setRevision(String revision) {
        this.revision = revision;
    }

}
