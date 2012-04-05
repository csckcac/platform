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
package org.wso2.carbon.ntask.core.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.core.TaskServiceContext;
import org.wso2.carbon.ntask.core.TaskInfo;
import org.wso2.carbon.ntask.core.TaskLocationResolver;

/**
 * This class represents a TaskLocationResolver implementation, which assigns a location to the task
 * in a round robin fashion.
 */
public class RoundRobinTaskLocationResolver implements TaskLocationResolver {

	@Override
	public int getLocation(TaskServiceContext ctx, TaskInfo taskInfo) throws TaskException {
		List<TaskInfo> tasks = ctx.getTasks();
		List<String> names = new ArrayList<String>();
		for (TaskInfo task : tasks) {
			names.add(task.getName());
		}
		Collections.sort(names);
		int n = names.size();
		for (int i = 0; i < n; i++) {
			if (taskInfo.getName().equals(names.get(i))) {
				return i;
			}
		}
		return 0;
	}

}
