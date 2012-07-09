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

import java.util.ArrayList;
import java.util.List;

public class ProductConfig {

    private String productName;
    List<Artifact> artifactList = new ArrayList<Artifact>();

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setProductArtifactList(List<Artifact> artifactList) {
        this.artifactList = artifactList;
    }

    public List<Artifact> getProductArtifactList() {
        return artifactList;
    }

    public String getProductName() {
        return productName;
    }
}
