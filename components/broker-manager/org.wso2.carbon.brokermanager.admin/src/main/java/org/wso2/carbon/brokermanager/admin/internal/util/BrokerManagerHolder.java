package org.wso2.carbon.brokermanager.admin.internal.util;


import org.wso2.carbon.brokermanager.core.BrokerManagerService;

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
public class BrokerManagerHolder {
    private BrokerManagerService brokerManagerService;
    private static BrokerManagerHolder instance = new BrokerManagerHolder();

    private BrokerManagerHolder() {
    }

    public BrokerManagerService getBrokerManagerService() {
        return brokerManagerService;
    }

    public static BrokerManagerHolder getInstance() {
        return instance;
    }

    public void registerBrokerManagerService(BrokerManagerService brokerManagerService) {
        this.brokerManagerService = brokerManagerService;
    }

    public void unRegisterBrokerManagerService(BrokerManagerService brokerManagerService) {
        this.brokerManagerService = null;
    }

}
