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
package org.wso2.carbon.bam.data.publisher.clientstats.config;

import org.wso2.carbon.bam.data.publisher.clientstats.ClientStatisticsPublisherConstants;

/**
 * Eventing configuration data that goes into the registry. Consists of the enable/disable status for
 * statistics eventing, and the threshold values for request counts at system, servce and operation levels.
 */
public class EventingConfigData {
	private String enableEventing;
	private int systemRequestCountThreshold;
	private int serviceRequestCountThreshold;
	private int operationRequestCountThreshold;

	public EventingConfigData() {
	}

	public String getEnableEventing() {
		return enableEventing;
	}

	public void setEnableEventing(String enableEventing) {
		this.enableEventing = enableEventing;
	}

	public boolean eventingEnabled() {
		return ClientStatisticsPublisherConstants.EVENTING_ON.equals(enableEventing);
	}

	public int getSystemRequestCountThreshold() {
		return systemRequestCountThreshold;
	}

	public void setSystemRequestCountThreshold(int systemRequestCountThreshold) {
		this.systemRequestCountThreshold = systemRequestCountThreshold;
	}

	public int getServiceRequestCountThreshold() {
		return serviceRequestCountThreshold;
	}

	public void setServiceRequestCountThreshold(int serviceRequestCountThreshold) {
		this.serviceRequestCountThreshold = serviceRequestCountThreshold;
	}

	public int getOperationRequestCountThreshold() {
		return operationRequestCountThreshold;
	}

	public void setOperationRequestCountThreshold(int operationRequestCountThreshold) {
		this.operationRequestCountThreshold = operationRequestCountThreshold;
	}
}