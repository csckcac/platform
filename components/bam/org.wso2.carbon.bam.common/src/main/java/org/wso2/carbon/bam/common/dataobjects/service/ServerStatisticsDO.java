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

package org.wso2.carbon.bam.common.dataobjects.service;

import org.wso2.carbon.bam.common.dataobjects.stats.StatisticsDO;

import java.util.Calendar;

/*
 * Server statistics Data class
 */
public class ServerStatisticsDO extends StatisticsDO {
	private String serverURL;
	private Calendar timestamp;
	private double avgResTime;
	private double maxResTime;
	private double minResTime;
	private int reqCount;
	private int resCount;
	private int faultCount;

	public ServerStatisticsDO() {

	}

	public ServerStatisticsDO(String serverURL, Calendar timestamp,double avgResTime, double maxResTime, double minResTime,
			int reqCount, int resCount, int faultCount) {
		this.serverURL = serverURL;
		this.timestamp = timestamp;
		this.avgResTime = avgResTime;
		this.maxResTime = maxResTime;
		this.minResTime = minResTime;
		this.reqCount = reqCount;
		this.resCount = resCount;
		this.faultCount = faultCount;
	}

	public String getServerURL() {
		return serverURL;
	}

	public void setServerURL(String serverURL) {
		this.serverURL = serverURL;
	}

	public Calendar getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Calendar timestamp) {
		this.timestamp = timestamp;
	}

	public double getAvgResTime() {
		return avgResTime;
	}

	public void setAvgResTime(double avgResTime) {
		this.avgResTime = avgResTime;
	}

	public double getMaxResTime() {
		return maxResTime;
	}

	public void setMaxResTime(double maxResTime) {
		this.maxResTime = maxResTime;
	}

	public double getMinResTime() {
		return minResTime;
	}

	public void setMinResTime(double minResTime) {
		this.minResTime = minResTime;
	}

	public int getReqCount() {
		return reqCount;
	}

	public void setReqCount(int reqCount) {
		this.reqCount = reqCount;
	}

	public int getResCount() {
		return resCount;
	}

	public void setResCount(int resCount) {
		this.resCount = resCount;
	}

	public int getFaultCount() {
		return faultCount;
	}

	public void setFaultCount(int faultCount) {
		this.faultCount = faultCount;
	}
}
