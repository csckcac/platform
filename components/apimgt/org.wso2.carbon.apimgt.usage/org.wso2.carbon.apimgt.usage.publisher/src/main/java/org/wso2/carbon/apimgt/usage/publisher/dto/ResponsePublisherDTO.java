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
package org.wso2.carbon.apimgt.usage.publisher.dto;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class ResponsePublisherDTO extends PublisherDTO {

    private String response = "1";

    private String responseTime;

    private String serviceTime;

    public void setResponseTime(String responseTime) {
        this.responseTime = responseTime;
    }

    public String getResponseTime() {
        return responseTime;
    }

    public void setServiceTime(String serviceTime) {
        this.serviceTime = serviceTime;
    }

    public String getServiceTime() {
        return serviceTime;
    }

    public Map<String, ByteBuffer> createEventDataMap() {
        Map<String, ByteBuffer> eventMap = new HashMap<String, ByteBuffer>();
        eventMap.put("consumerKey", ByteBuffer.wrap(getConsumerKey().getBytes()));
        eventMap.put("context", ByteBuffer.wrap(getContext().getBytes()));
        eventMap.put("api_version", ByteBuffer.wrap(getApi_version().getBytes()));
        eventMap.put("api", ByteBuffer.wrap(getApi().getBytes()));
        eventMap.put("resource", ByteBuffer.wrap(getResource().getBytes()));
        eventMap.put("method", ByteBuffer.wrap(getMethod().getBytes()));
        eventMap.put("version", ByteBuffer.wrap(getVersion().getBytes()));
        eventMap.put("response", ByteBuffer.wrap(response.getBytes()));
        eventMap.put("responseTime", ByteBuffer.wrap(getResponseTime().getBytes()));
        eventMap.put("serviceTime", ByteBuffer.wrap(getServiceTime().getBytes()));
        return eventMap;
    }
}
