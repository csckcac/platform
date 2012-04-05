/*                                                                             
 * Copyright 2004,2005 The Apache Software Foundation.                         
 *                                                                             
 * Licensed under the Apache License, Version 2.0 (the "License");             
 * you may not use this file except in compliance with the License.            
 * You may obtain a copy of the License at                                     
 *                                                                             
 *      http://www.apache.org/licenses/LICENSE-2.0                             
 *                                                                             
 * Unless required by applicable law or agreed to in writing, software         
 * distributed under the License is distributed on an "AS IS" BASIS,           
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.    
 * See the License for the specific language governing permissions and         
 * limitations under the License.                                              
 */
package org.wso2.carbon.ec2client.data;

/**
 *
 */
public enum InstanceType {
    SMALL("m1.small"),
    LARGE("m1.large"),
    EXTRA_LARGE("m1.xlarge"),
    MEDIUM_HCPU("c1.medium"),
    EXTRA_LARGE_HCPU("c1.xlarge");

    private String type;

    InstanceType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public static InstanceType getTypeFromString(String type) {
        for (InstanceType t : InstanceType.values()) {
            if (t.getType().equals(type)) {
                return t;
            }
        }
        return null;
    }
}
