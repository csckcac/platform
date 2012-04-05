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
public class Address {
    private Instance instance;
    private String publicIp;

    public Address(Instance instance, String publicIp) {
        this.instance = instance;
        this.publicIp = publicIp;
    }

    public Address(String publicIp) {
        this.publicIp = publicIp;
    }

    public void setInstance(Instance instance) {
        this.instance = instance;
    }

    public Instance getInstance() {
        return instance;
    }

    public String getPublicIp() {
        return publicIp;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Address address = (Address) o;

        return publicIp.equals(address.getPublicIp());

    }

    public int hashCode() {
        return publicIp.hashCode();
    }

    public String toString() {
        return "Address{" +
               "publicIp='" + publicIp + '\'' +
               '}';
    }
}
