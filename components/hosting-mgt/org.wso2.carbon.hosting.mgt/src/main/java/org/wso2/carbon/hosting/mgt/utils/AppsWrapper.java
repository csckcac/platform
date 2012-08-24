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
package org.wso2.carbon.hosting.mgt.utils;

import org.wso2.carbon.utils.Pageable;

import java.util.List;

/**
 * This class holds summary information about all the webapps in the system
 */
public final class AppsWrapper implements Pageable {
    private String[] apps;
    private int numberOfApps;
    private int numberOfPages;
    private String appsDir;
    private String hostName;

    public int getNumberOfPages() {
        return numberOfPages;
    }

    public void setNumberOfPages(int numberOfPages) {
        this.numberOfPages = numberOfPages;
    }

    public <T> void set(List<T> items) {
        this.apps = items.toArray(new String[items.size()]);
    }

    public int getNumberOfApps() {
        return numberOfApps;
    }

    public void setNumberOfApps(int numberOfCorrectWebapps) {
        this.numberOfApps = numberOfCorrectWebapps;
    }

    public String getAppsDir() {
        return appsDir;
    }

    public void setAppsDir(String appsDir) {
        this.appsDir = appsDir;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String[] getApps() {
        return this.apps;
    }

    public void setApps(String[] Apps) {
        this.apps = Apps;
    }

}

