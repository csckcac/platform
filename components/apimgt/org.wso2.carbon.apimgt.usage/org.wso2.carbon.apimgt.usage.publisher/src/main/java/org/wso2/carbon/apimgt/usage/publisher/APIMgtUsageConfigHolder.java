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
package org.wso2.carbon.apimgt.usage.publisher;

import org.wso2.carbon.apimgt.usage.publisher.internal.UsageComponent;
import org.wso2.carbon.apimgt.usage.publisher.service.APIMGTConfigReaderService;
import org.wso2.carbon.bam.agent.publish.EventReceiver;

public class APIMgtUsageConfigHolder {

    public EventReceiver createEventReceiver() {
        APIMGTConfigReaderService apimgtConfigReaderService = UsageComponent.getApiMgtConfigReaderService();
        return createEventReceiver(apimgtConfigReaderService);
    }

    public EventReceiver createEventReceiver(APIMGTConfigReaderService apimgtConfigReaderService) {
        EventReceiver eventReceiver = new EventReceiver();
        eventReceiver.setUrl(apimgtConfigReaderService.getBamServerURL());
        eventReceiver.setUserName(apimgtConfigReaderService.getBamServerUser());
        eventReceiver.setPassword(apimgtConfigReaderService.getBamServerPassword());
        eventReceiver.setPort(Integer.parseInt(
                apimgtConfigReaderService.getBamServerThriftPort()));
        eventReceiver.setSocketTransportEnabled(true);
        return eventReceiver;
    }
}
