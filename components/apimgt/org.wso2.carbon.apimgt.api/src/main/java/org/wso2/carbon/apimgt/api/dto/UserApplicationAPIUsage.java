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
package org.wso2.carbon.apimgt.api.dto;

import org.wso2.carbon.apimgt.api.model.APIIdentifier;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to transfer data for User/Keys UI in Provider view.
 */
public class UserApplicationAPIUsage {

    private String userId;
    private String applicationName;
    private List<APIIdentifier> apiIdentifiers = new ArrayList<APIIdentifier>();

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public APIIdentifier[] getApiIdentifiers() {
        return apiIdentifiers.toArray(new APIIdentifier[apiIdentifiers.size()]);
    }

    public void addApiIdentifier(APIIdentifier apiIdentifier) {
        apiIdentifiers.add(apiIdentifier);
    }
}
