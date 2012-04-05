/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.bam.integration.test.datacollection.activity.mockobjects;

import org.wso2.carbon.bam.data.publisher.activity.service.config.EventingConfigData;
import org.wso2.carbon.bam.data.publisher.activity.service.config.XPathConfigData;
import org.wso2.carbon.bam.data.publisher.activity.service.services.ActivityPublisherAdmin;

public class MockActivityPublisherAdmin extends ActivityPublisherAdmin {

    EventingConfigData configData;

    public void configureEventing(EventingConfigData eventingConfigData) throws Exception {
        this.configData = eventingConfigData;
    }

    public EventingConfigData getEventingConfigData() {
        return this.configData;
    }

    public XPathConfigData[] getXPathData() throws Exception {
        return super.getXPathData();
    }

}
