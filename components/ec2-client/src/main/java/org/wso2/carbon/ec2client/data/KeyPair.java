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
public class KeyPair {
    private String keyName;
    private String fingerPrint;

    public KeyPair(String keyName, String fingerPrint) {
        this.keyName = keyName;
        this.fingerPrint = fingerPrint;
    }

    public String getKeyName() {
        return keyName;
    }

    public String getFingerPrint() {
        return fingerPrint;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        KeyPair keyPair = (KeyPair) o;

        return keyName.equals(keyPair.getKeyName());

    }

    public int hashCode() {
        return keyName.hashCode();
    }

    public String toString() {
        return "KeyPair{" +
               "keyName='" + keyName + '\'' +
               ", fingerPrint='" + fingerPrint + '\'' +
               '}';
    }
}
