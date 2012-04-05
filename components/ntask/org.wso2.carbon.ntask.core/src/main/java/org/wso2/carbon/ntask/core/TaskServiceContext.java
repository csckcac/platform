/**
 *  Copyright (c) 2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.ntask.core;

import java.util.List;

/**
 * This class represents a runtime context of the task service.
 */
public class TaskServiceContext {

	private List<TaskInfo> tasks;
	
	private int serverCount;
	
	public TaskServiceContext(List<TaskInfo> tasks, int serverCount) {
		this.tasks = tasks;
		this.serverCount = serverCount;
	}
	
	public List<TaskInfo> getTasks() {
		return tasks;
	}
	
	public int getServerCount() {
		return serverCount;
	}
	
}
