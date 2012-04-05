package org.wso2.carbon.brokermanager.admin.internal;

/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Broker Configuration Details are stored
 */
public class BrokerConfigurationDetails {
    private String brokerName;
    private String brokerType;
    // array of broker properties
    private BrokerProperty[] brokerProperties;
    // to keep track of current index of brokerProperties
    private int currentBrokerPropertyIndex=0;

    public BrokerConfigurationDetails(String brokerName, String brokerType, int numberOfProperties) {
        this.brokerName = brokerName;
        this.brokerType = brokerType;
        brokerProperties = new BrokerProperty[numberOfProperties];
    }

    public String getBrokerName() {
        return brokerName;
    }

    public String getBrokerType() {
        return brokerType;
    }

    public BrokerProperty[] getBrokerProperties() {
        return brokerProperties;
    }

    public void addBrokerProperty(String key, String value) {
        brokerProperties[currentBrokerPropertyIndex]= new BrokerProperty(key, value) ;
        currentBrokerPropertyIndex++;
    }

   


}
