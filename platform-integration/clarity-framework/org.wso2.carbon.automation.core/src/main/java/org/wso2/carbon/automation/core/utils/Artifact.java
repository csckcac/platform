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
package org.wso2.carbon.automation.core.utils;

import java.util.*;

public class Artifact {

    private String artifactName;
    private ArtifactType artifactType;
    private String artifactLocation;
    private int userId;
    List<ArtifactDependency> dependencyList
            = new ArrayList<ArtifactDependency>();
    List<ArtifactAssociation> associationList
            = new ArrayList<ArtifactAssociation>();

    public void setArtifactName(String artifactName) {
        this.artifactName = artifactName;
    }

    public void setArtifactLocation(String artifactLocation) {
        this.artifactLocation = artifactLocation;
    }

    public void setDependencyArtifactList(List<ArtifactDependency> dependencyList) {
        this.dependencyList = dependencyList;
    }

    public List<ArtifactDependency> getDependencyArtifactList() {
        return dependencyList;
    }

    public void setAssociationList(List<ArtifactAssociation> associationList) {
        this.associationList = associationList;
    }

    public List<ArtifactAssociation> getAssociationList() {
        return associationList;
    }

    public void setArtifactType(ArtifactType artifactType) {
        this.artifactType = artifactType;
    }

    public ArtifactType getArtifactType() {
        return artifactType;
    }

    public String getArtifactName() {
        return artifactName;
    }

    public String getArtifactLocation() {
        return artifactLocation;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getUserId() {
        return userId;
    }
}


