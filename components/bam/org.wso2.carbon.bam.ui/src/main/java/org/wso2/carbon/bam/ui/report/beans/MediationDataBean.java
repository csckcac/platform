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
package org.wso2.carbon.bam.ui.report.beans;

/**
 * used to define columns for the MediationData report
 */
public class MediationDataBean {

	private String serverURL;
	private String serviceName;
	private String operation;
	private String avgTime;
	private String maxTime;
	private String minTime;
	private String reqCount;
	private String faultCount;
    private String statType;

    public String getStatType() {
        return statType;
    }

    public void setStatType(String statType) {
        this.statType = statType;
    }

    public String getServerURL() {
		return serverURL;
	}

	public void setServerURL(String serverURL) {
		this.serverURL = serverURL;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public String getAvgTime() {
		return avgTime;
	}

	public void setAvgTime(String avgTime) {
		this.avgTime = avgTime;
	}

	public String getMaxTime() {
		return maxTime;
	}

	public void setMaxTime(String maxTime) {
		this.maxTime = maxTime;
	}

	public String getMinTime() {
		return minTime;
	}

	public void setMinTime(String minTime) {
		this.minTime = minTime;
	}

	public String getReqCount() {
		return reqCount;
	}

	public void setReqCount(String reqCount) {
		this.reqCount = reqCount;
	}

	public String getFaultCount() {
		return faultCount;
	}

	public void setFaultCount(String faultCount) {
		this.faultCount = faultCount;
	}
}
