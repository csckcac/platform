/**
 *
 * Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.wso2.carbon.agent.conf;

/**
 * configuration details of Agent Server Endpoint
 */
public class ReceiverConfiguration {

    private String userName;
    private String password;
    private String eventReceiverIp;
    private int eventReceiverPort;
    private String authenticatorIp;
    private int authenticatorPort;

    public ReceiverConfiguration(String userName, String password, String eventReceiverIp,
                                 int eventReceiverPort, String authenticatorIp,
                                 int authenticatorPort) {
        this.userName = userName;
        this.password = password;
        this.eventReceiverIp = eventReceiverIp;
        this.eventReceiverPort = eventReceiverPort;
        this.authenticatorIp = authenticatorIp;
        this.authenticatorPort = authenticatorPort;
    }

    public String getEventReceiverIp() {
        return eventReceiverIp;
    }

    public void setEventReceiverIp(String eventReceiverIp) {
        this.eventReceiverIp = eventReceiverIp;
    }

    public int getEventReceiverPort() {
        return eventReceiverPort;
    }

    public void setEventReceiverPort(int eventReceiverPort) {
        this.eventReceiverPort = eventReceiverPort;
    }

    public String getAuthenticatorIp() {
        return authenticatorIp;
    }

    public void setAuthenticatorIp(String authenticatorIp) {
        this.authenticatorIp = authenticatorIp;
    }

    public int getAuthenticatorPort() {
        return authenticatorPort;
    }

    public void setAuthenticatorPort(int authenticatorPort) {
        this.authenticatorPort = authenticatorPort;
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
}
