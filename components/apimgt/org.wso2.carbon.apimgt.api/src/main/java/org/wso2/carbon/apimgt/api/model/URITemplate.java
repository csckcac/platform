/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.apimgt.api.model;

import java.util.HashSet;
import java.util.Set;

public class URITemplate {

    private String uriTemplate;
    private Set<String> methods = new HashSet<String>();
    private String resourceURI;
    private String resourceSandboxURI;

    public String getResourceURI() {
        return resourceURI;
    }

    public void setResourceURI(String resourceURI) {
        this.resourceURI = resourceURI;
    }

    public String getResourceSandboxURI() {
        return resourceSandboxURI;
    }

    public void setResourceSandboxURI(String resourceSandboxURI) {
        this.resourceSandboxURI = resourceSandboxURI;
    }

    public String getUriTemplate() {
        return uriTemplate;
    }

    public void setUriTemplate(String template) {
        this.uriTemplate = template;
    }

    public Set<String> getMethods() {
        return methods;
    }
    
    public String getMethodsAsString() {
        String str = "";
        for (String method : methods) {
            str += method + " ";
        }
        return str.trim();
    }

    public void addMethod(String method) {
        this.methods.add(method);
    }
}
