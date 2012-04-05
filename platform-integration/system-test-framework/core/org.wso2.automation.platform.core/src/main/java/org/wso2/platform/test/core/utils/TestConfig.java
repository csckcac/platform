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


import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

public class TestConfig {

    private String testClassName;

    private String description;

    private Map<Integer, String> productNames = new Hashtable<Integer, String>();

    private Map<String, String> artifacts = new Hashtable<String, String>();

    private String artifactType;

    MultiValueMapUtil<String, String> jarArtifactMap =
            new MultiValueMapUtil<String, String>(new ArrayList<String>());

    MultiValueMapUtil<String, String> carArtifactMap =
            new MultiValueMapUtil<String, String>(new ArrayList<String>());

    MultiValueMapUtil<String, String> marArtifactMap =
            new MultiValueMapUtil<String, String>(new ArrayList<String>());

    MultiValueMapUtil<String, String> garArtifactMap =
            new MultiValueMapUtil<String, String>(new ArrayList<String>());

    MultiValueMapUtil<String, String> xmlArtifactMap =
            new MultiValueMapUtil<String, String>(new ArrayList<String>());

    MultiValueMapUtil<String, String> aarArtifactMap =
            new MultiValueMapUtil<String, String>(new ArrayList<String>());

    MultiValueMapUtil<String, String> warArtifactMap =
            new MultiValueMapUtil<String, String>(new ArrayList<String>());

    public TestConfig(String testClassName, String description, Map<Integer,
            String> products, Map<String, String> artifacts, String artifactType, MultiValueMapUtil<String, String>
            jarArtifactMap, MultiValueMapUtil<String, String> carArtifactMap,
                      MultiValueMapUtil<String, String> marArtifactMap,
                      MultiValueMapUtil<String, String> garArtifactMap, MultiValueMapUtil<String, String> xmlArtifactMap,
                      MultiValueMapUtil<String, String> aarArtifactMap, MultiValueMapUtil<String, String> warArtifactMap) {
        this.testClassName = testClassName;
        this.description = description;
        this.productNames = products;
        this.artifacts = artifacts;
        this.artifactType = artifactType;
        this.jarArtifactMap = jarArtifactMap;
        this.carArtifactMap = carArtifactMap;
        this.marArtifactMap = marArtifactMap;
        this.garArtifactMap = garArtifactMap;
        this.xmlArtifactMap = xmlArtifactMap;
        this.aarArtifactMap = aarArtifactMap;
        this.warArtifactMap = warArtifactMap;

    }

    public String getTestClassName() {
        return testClassName;
    }

    public String getDescription() {
        return description;
    }

    public String getArtifactType() {
        return artifactType;
    }

    public Map<Integer, String> getProductName() {
        return productNames;
    }

    public Map<String, String> getArtifactList() {
        return artifacts;
    }

    public MultiValueMapUtil<String, String> getAarArtifact() {
        return aarArtifactMap;
    }

    public MultiValueMapUtil<String, String> getMarArtifact() {
        return marArtifactMap;
    }

    public MultiValueMapUtil<String, String> getJarArtifact() {
        return jarArtifactMap;
    }

    public MultiValueMapUtil<String, String> getCarArtifact() {
        return carArtifactMap;
    }

    public MultiValueMapUtil<String, String> getXmlArtifact() {
        return xmlArtifactMap;
    }

    public MultiValueMapUtil<String, String> getGarArtifact() {
        return garArtifactMap;
    }

    public MultiValueMapUtil<String, String> getWarArtifact() {
        return warArtifactMap;
    }
}
