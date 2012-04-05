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
public class InstanceState {
    public static final InstanceState PENDING = new InstanceState(0, "pending");
    public static final InstanceState RUNNING = new InstanceState(16, "running");
    public static final InstanceState SHUTTING_DOWN = new InstanceState(32, "shutting-down");
    public static final InstanceState TERMINATED = new InstanceState(48, "terminated");

    private int code;
    private String name;

    private InstanceState(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        InstanceState that = (InstanceState) o;
        return code == that.getCode();

    }

    public int hashCode() {
        return code;
    }

    public String toString() {
        return "EC2InstanceState{" +
               "code=" + code +
               ", name='" + name + '\'' +
               '}';
    }
}
