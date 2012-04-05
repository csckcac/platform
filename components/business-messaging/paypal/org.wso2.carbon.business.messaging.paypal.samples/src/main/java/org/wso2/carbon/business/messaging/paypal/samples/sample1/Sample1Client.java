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
package org.wso2.carbon.business.messaging.paypal.samples.sample1;

import org.apache.axiom.om.OMElement;
import org.wso2.carbon.business.messaging.paypal.samples.GetBalanceClient;
import org.wso2.carbon.business.messaging.paypal.samples.GetBalanceHandler;

public class Sample1Client extends GetBalanceClient {

    public OMElement getPayload() {
        String version = getProperty("version", "61");
        String password = getProperty("password", "1265369211");
        String username = getProperty("username",
                "fazlan_1265369202_biz_api1.wso2.com");
        String signature = getProperty("signature",
				"AaNvupC2HsVPs-d5iU9.YgFyjltMAh4wuG8d7jqGMZAIuMO8mvGVtKzd");
        String detailLevel = getProperty("detail","full");
        String lang = getProperty("language","en_us");

        OMElement payload = null;
        payload = new Sample1SOAPBuilder().createRequestPayload(version, username,
                                                               password, signature,detailLevel,lang);
        return payload;
    }
    
}
