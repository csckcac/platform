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
package org.wso2.carbon.bam.data.publisher.mediationstats.config;

import org.wso2.carbon.bam.data.publisher.mediationstats.MDPublisherConstants;

/**
 * Eventing configuration data that goes into the registry.
 */
public class MediationStatConfig {
    private String enableEventing;
    private int proxyRequestCountThreshold;
    private int endpointRequestCountThreshold;
    private int sequenceRequestCountThreshold;

    public MediationStatConfig() {
    }

    public String getEnableEventing() {
        return enableEventing;
    }

    public void setEnableEventing(String enableEventing) {
        this.enableEventing = enableEventing;
    }

    public boolean eventingEnabled() {
        return MDPublisherConstants.EVENTING_ON.equals(enableEventing);
    }

    public int getProxyRequestCountThreshold() {
        return proxyRequestCountThreshold;
    }

    public void setProxyRequestCountThreshold(int proxyRequestCountThreshold) {
        this.proxyRequestCountThreshold = proxyRequestCountThreshold;
    }

    public int getEndpointRequestCountThreshold() {
        return endpointRequestCountThreshold;
    }

    public void setEndpointRequestCountThreshold(int endpointRequestCountThreshold) {
        this.endpointRequestCountThreshold = endpointRequestCountThreshold;
    }

    public int getSequenceRequestCountThreshold() {
        return sequenceRequestCountThreshold;
    }

    public void setSequenceRequestCountThreshold(int sequenceRequestCountThreshold) {
        this.sequenceRequestCountThreshold = sequenceRequestCountThreshold;
    }
}