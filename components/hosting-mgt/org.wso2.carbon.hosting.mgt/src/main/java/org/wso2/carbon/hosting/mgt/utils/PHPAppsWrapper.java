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

import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.Pageable;

import java.util.List;

/**
 * This class holds summary information about all the webapps in the system
 */
public final class PHPAppsWrapper implements Pageable {
    private String[] phpapps;
    private int numberOfPhpapps;
    private int numberOfFaultyWebapps;
    private int numberOfPages;
    private String webappsDir;
    private String hostName;
    private int httpPort;
    private int httpsPort;

    public int getNumberOfPages() {
        return numberOfPages;
    }

    public void setNumberOfPages(int numberOfPages) {
        this.numberOfPages = numberOfPages;
    }

    public <T> void set(List<T> items) {
        this.phpapps = items.toArray(new String[items.size()]);
    }

    public int getNumberOfPhpapps() {
        return numberOfPhpapps;
    }

    public void setNumberOfPhpapps(int numberOfCorrectWebapps) {
        this.numberOfPhpapps = numberOfCorrectWebapps;
    }

    public int getNumberOfFaultyWebapps() {
        return numberOfFaultyWebapps;
    }

    public void setNumberOfFaultyWebapps(int numberOfFaultyWebapps) {
        this.numberOfFaultyWebapps = numberOfFaultyWebapps;
    }

    public String getWebappsDir() {
        return webappsDir;
    }

    public void setWebappsDir(String webappsDir) {
        this.webappsDir = webappsDir;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public int getHttpPort() {
        return httpPort;
    }

    public void setHttpPort(int httpPort) {
        this.httpPort = httpPort;
    }

    public int getHttpsPort() {
        return httpsPort;
    }

    public void setHttpsPort(int httpsPort) {
        this.httpsPort = httpsPort;
    }

    public String[] getPhpapps() {
        return CarbonUtils.arrayCopyOf(phpapps);
    }

    public void setPhpapps(String[] webapps) {
        this.phpapps = CarbonUtils.arrayCopyOf(webapps);
    }

}

