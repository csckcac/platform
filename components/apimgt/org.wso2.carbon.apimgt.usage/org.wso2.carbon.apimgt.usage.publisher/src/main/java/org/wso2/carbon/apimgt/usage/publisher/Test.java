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

import org.wso2.carbon.apimgt.usage.publisher.dto.RequestPublisherDTO;
import org.wso2.carbon.apimgt.usage.publisher.dto.ResponsePublisherDTO;
import org.wso2.carbon.apimgt.usage.publisher.internal.UsageComponent;
import org.wso2.carbon.apimgt.usage.publisher.service.APIMGTConfigReaderService;


public class Test {
    public static void main(String[] args) {

        // Have to hard code Carbon Home in APIMGTConfigReaderService and APIMgtUsagePublisherConstants
        UsageComponent.apimgtConfigReaderService = new APIMGTConfigReaderService();
        APIMgtUsageConfigHolder configHolder = new APIMgtUsageConfigHolder();
        APIMgtUsageBAMDataPublisher testPublisher = new APIMgtUsageBAMDataPublisher(configHolder);
        RequestPublisherDTO testRequestPublisherDTO = new RequestPublisherDTO();
        ResponsePublisherDTO testResponsePublisherDTO = new ResponsePublisherDTO();

        //Only the properties needed for the test are set
        testRequestPublisherDTO.setApi("DeliciousAPI");
        testRequestPublisherDTO.setVersion("v1.0.0");
        testRequestPublisherDTO.setRequestTime(((Long)System.currentTimeMillis()).toString());
        testPublisher.publishEvent(testRequestPublisherDTO);

        testRequestPublisherDTO.setApi("DeliciousAPI");
        testRequestPublisherDTO.setVersion("v1.0.0");
        testRequestPublisherDTO.setRequestTime(((Long)System.currentTimeMillis()).toString());
        testPublisher.publishEvent(testRequestPublisherDTO);

        testRequestPublisherDTO.setApi("FacebookAPI");
        testRequestPublisherDTO.setVersion("v1.0.0");
        testRequestPublisherDTO.setRequestTime(((Long)System.currentTimeMillis()).toString());
        testPublisher.publishEvent(testRequestPublisherDTO);

        testRequestPublisherDTO.setApi("TwitterAPI");
        testRequestPublisherDTO.setVersion("v1.0.0");
        testRequestPublisherDTO.setRequestTime(((Long)System.currentTimeMillis()).toString());
        testPublisher.publishEvent(testRequestPublisherDTO);

        testRequestPublisherDTO.setApi("DeliciousAPI");
        testRequestPublisherDTO.setVersion("v1.1.0");
        testRequestPublisherDTO.setRequestTime(((Long)System.currentTimeMillis()).toString());
        testPublisher.publishEvent(testRequestPublisherDTO);

        testRequestPublisherDTO.setApi("TwitterAPI");
        testRequestPublisherDTO.setVersion("v1.1.0");
        testRequestPublisherDTO.setRequestTime(((Long)System.currentTimeMillis()).toString());
        testPublisher.publishEvent(testRequestPublisherDTO);

        testRequestPublisherDTO.setApi("DeliciousAPI");
        testRequestPublisherDTO.setVersion("v1.1.0");
        testRequestPublisherDTO.setRequestTime(((Long)System.currentTimeMillis()).toString());
        testPublisher.publishEvent(testRequestPublisherDTO);

        testRequestPublisherDTO.setApi("DeliciousAPI");
        testRequestPublisherDTO.setVersion("v1.2.0");
        testRequestPublisherDTO.setRequestTime(((Long)System.currentTimeMillis()).toString());
        testPublisher.publishEvent(testRequestPublisherDTO);

        testRequestPublisherDTO.setApi("FacebookAPI");
        testRequestPublisherDTO.setVersion("v1.2.0");
        testRequestPublisherDTO.setRequestTime(((Long)System.currentTimeMillis()).toString());
        testPublisher.publishEvent(testRequestPublisherDTO);

        testResponsePublisherDTO.setApi("DeliciousAPI");
        testResponsePublisherDTO.setVersion("v1.0.0");
        testResponsePublisherDTO.setResponseTime(((Long)System.currentTimeMillis()).toString());
        testResponsePublisherDTO.setServiceTime("5");
        testPublisher.publishEvent(testResponsePublisherDTO);

    }
}

