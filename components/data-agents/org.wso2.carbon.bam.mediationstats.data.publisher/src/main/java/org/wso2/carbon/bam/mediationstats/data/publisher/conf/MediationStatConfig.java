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
package org.wso2.carbon.bam.mediationstats.data.publisher.conf;


public class MediationStatConfig {

    private boolean enableMediationStats;
    private String url = "";
    private String userName = "";
    private String password = "";
    private boolean isHttpTransportEnable;
    private boolean isSocketTransportEnable;
    private int port;
    private Property[] properties;


    public Property[] getProperties() {
        return properties;
    }

    public void setProperties(Property[] properties) {
        this.properties = properties;
    }

    public boolean isHttpTransportEnable() {
        return isHttpTransportEnable;
    }

    public void setHttpTransportEnable(boolean httpTransportEnable) {
        isHttpTransportEnable = httpTransportEnable;
    }

    public boolean isSocketTransportEnable() {
        return isSocketTransportEnable;
    }

    public void setSocketTransportEnable(boolean socketTransportEnable) {
        isSocketTransportEnable = socketTransportEnable;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        if (!url.endsWith("/")) {
            url = url + "/";
        }
        this.url = url;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isEnableMediationStats() {
        return enableMediationStats;
    }

    public void setEnableMediationStats(boolean enableMediationStats) {
        this.enableMediationStats = enableMediationStats;
    }

}
