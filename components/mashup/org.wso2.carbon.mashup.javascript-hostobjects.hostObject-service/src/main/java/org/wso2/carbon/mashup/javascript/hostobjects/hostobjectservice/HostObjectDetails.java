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
package org.wso2.carbon.mashup.javascript.hostobjects.hostobjectservice;

public class HostObjectDetails {

    private String className;
    private String hostObjectName;
    private String globalObjectName;
    private boolean hostObjectServiceNeeded = false;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getHostObjectName() {
        return hostObjectName;
    }

    public void setHostObjectName(String hostObjectName) {
        this.hostObjectName = hostObjectName;
    }

    public String getGlobalObjectName() {
        return globalObjectName;
    }

    public void setGlobalObjectName(String globalObjectName) {
        this.globalObjectName = globalObjectName;
    }

    public boolean isHostObjectServiceNeeded() {
        return hostObjectServiceNeeded;
    }

    public void setHostObjectServiceNeeded(boolean hostObjectServiceNeeded) {
        this.hostObjectServiceNeeded = hostObjectServiceNeeded;
    }
}
