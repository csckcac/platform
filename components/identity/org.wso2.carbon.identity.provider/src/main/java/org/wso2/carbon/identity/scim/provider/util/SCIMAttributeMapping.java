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
package org.wso2.carbon.identity.scim.provider.util;

import org.wso2.charon.core.schema.SCIMSchemaDefinitions;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SCIMAttributeMapping {

    public static Map<String, String> scimAttributeMap = new ConcurrentHashMap<String, String>();
    public static SCIMAttributeMapping scimAttributemapping = null;

    public static SCIMAttributeMapping getInstance() {
        if(scimAttributemapping == null){
            synchronized (SCIMAttributeMapping.class) {
                if(scimAttributemapping == null){
                    scimAttributemapping = new SCIMAttributeMapping();
                    return scimAttributemapping;
                }
                return scimAttributemapping;
            }
        }
        return scimAttributemapping;
    }

    public SCIMAttributeMapping(){
        scimAttributeMap.put("userName", SCIMSchemaDefinitions.USER_NAME.getName());
        scimAttributeMap.put("","");
    }
}
