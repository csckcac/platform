/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.bpel.core.ode.integration.config.bam;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds the BAM Server profile information
 */
public class BAMServerProfile {
    private String name;
    private String userName;
    private String password;
    private String ip;
    private String authenticationPort;
    private String receiverPort;
    private String keyStoreLocation;
    private String keyStorePassword;
    private boolean isSecurityEnabled = true;
    private Map<String, BAMStreamConfiguration> streamConfigurations =
            new HashMap<String, BAMStreamConfiguration>();

    //    public BAMServerProfile() {}
//
//    public BAMServerProfile(String name, String userName, String password, String ip,
//                            String authenticationPort, String receiverPort, String keyStoreLocation,
//                            String keyStorePassword, boolean enableSecurity) {
//        this.name = name;
//        this.userName = userName;
//        this.password = password;
//        this.ip = ip;
//        this.authenticationPort = authenticationPort;
//        this.receiverPort = receiverPort;
//        this.keyStoreLocation = keyStoreLocation;
//        this.keyStorePassword = keyStorePassword;
//        this.isSecurityEnabled = enableSecurity;
//    }
    public String getName() {
        return name;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public String getIp() {
        return ip;
    }

    public String getAuthenticationPort() {
        return authenticationPort;
    }

    public String getReceiverPort() {
        return receiverPort;
    }

    public String getKeyStoreLocation() {
        return keyStoreLocation;
    }

    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    public boolean isSecurityEnabled() {
        return isSecurityEnabled;
    }

    public BAMStreamConfiguration getBAMStreamConfiguration(String streamName) {
        return streamConfigurations.get(streamName);
    }

    public void addBAMStreamConfiguration(String streamName,
                                          BAMStreamConfiguration streamConfiguration) {
        streamConfigurations.put(streamName, streamConfiguration);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setAuthenticationPort(String authenticationPort) {
        this.authenticationPort = authenticationPort;
    }

    public void setReceiverPort(String receiverPort) {
        this.receiverPort = receiverPort;
    }

    public void setKeyStoreLocation(String keyStoreLocation) {
        this.keyStoreLocation = keyStoreLocation;
    }

    public void setKeyStorePassword(String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }

    public void setSecurityEnabled(boolean securityEnabled) {
        isSecurityEnabled = securityEnabled;
    }
}
