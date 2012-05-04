/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.usage.client.dto;

public class ProviderAPIVersionLastAccessDTO {
    private String api_version;
    private String lastAccess;

    public String getLastAccess(){
        return lastAccess;
    }

    public String getApi_version(){
        return api_version;
    }

    public void setLastAccess(String lastAccess){
        this.lastAccess = lastAccess;
    }

    public void setApi_version(String api_version){
        this.api_version = api_version;
    }

    public ProviderAPIVersionLastAccessDTO(String api_version, String lastAccess){
        this.api_version = api_version;
        this.lastAccess = lastAccess;
    }
}
