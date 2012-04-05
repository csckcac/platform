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
package org.wso2.carbon.bam.common.dataobjects.service;

/*
 * Operation Data class
 */
public class OperationDO {
	private int operationID;
	private String name;
    private String description;
    private int serviceID;

	public OperationDO() {

	}

	public OperationDO(String operationName) {
		this.name = operationName;
	}

	public int getOperationID() {
		return operationID;
	}

	public void setOperationID(int operationID) {
		this.operationID = operationID;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setServiceID(int ID) {
        this.serviceID = ID;
    }

    public int getServiceID() {
        return this.serviceID;
    }

}
