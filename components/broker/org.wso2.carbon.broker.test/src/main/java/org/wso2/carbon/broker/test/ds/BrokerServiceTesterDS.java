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

package org.wso2.carbon.broker.test.ds;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.broker.core.BrokerService;
import org.wso2.carbon.broker.test.util.BrokerHolder;

/**
 * this class is used to get the Broker service.
 *
 * @scr.component name="broker.test.component" immediate="true"
 * @scr.reference name="broker.service"
 * interface="org.wso2.carbon.broker.core.BrokerService" cardinality="1..1"
 * policy="dynamic" bind="setBrokerService" unbind="unSetBrokerService"
 */
public class BrokerServiceTesterDS {
    private static final Log log = LogFactory.getLog(BrokerServiceTesterDS.class);

    protected void activate(ComponentContext context) {
        log.info("successfully deployed broker service tester");
    }

    protected void setBrokerService(BrokerService brokerService) {
        BrokerHolder.getInstance().registerBrokerService(brokerService);
    }

    protected void unSetBrokerService(BrokerService brokerService) {
        BrokerHolder.getInstance().unRegisterBrokerService(brokerService);
    }
}
