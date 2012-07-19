/*
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.rssmanager.core.internal.dao.entity;

import java.util.HashMap;
import java.util.Map;

/**
 * This class represents the database privileges assigned to a particular user.
 */
public class DatabasePermissions {

    private Map<String, Object> privilegeMap = new HashMap<String, Object>();

    public Object getPermission(String permissionName) {
         return this.privilegeMap.get(permissionName);
    }

    public void setPermission(String permissionName, Object value) {
         this.privilegeMap.put(permissionName, value);
    }

    public Map<String, Object> getPrivilegeMap() {
        return privilegeMap;
    }

}
