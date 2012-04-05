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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.coordination.common.CoordinationException;
import org.wso2.carbon.coordination.common.CoordinationException.ExceptionCode;
import org.wso2.carbon.coordination.core.sync.Group;
import org.wso2.carbon.coordination.core.sync.GroupEventListener;
import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.common.TaskException.Code;
import org.wso2.carbon.ntask.core.TaskInfo;
import org.wso2.carbon.ntask.core.TaskLocationResolver;
import org.wso2.carbon.ntask.core.TaskRepository;
import org.wso2.carbon.ntask.core.TaskServiceContext;
import org.wso2.carbon.ntask.core.internal.TasksDSComponent;

/**
 * This class represents a clustered task manager, which is used when tasks are distributed across a
 * cluster.
 */
public class ClusteredTaskManager extends AbstractQuartzTaskManager {

	private static final String TASK_SERVER_COUNT_SYS_PROP = "task.server.count";

	private static final Log log = LogFactory.getLog(ClusteredTaskManager.class);
	
	public static final String TASK_GROUP_BASE_NAME = "__TASK_GROUP_";
		
	private ClusterGroupCommunicator clusterComm;
	
	private Map<String, String> taskNameMemberIdMap;
	
	public ClusteredTaskManager(TaskRepository taskRepository, boolean checkServerCount) 
			throws TaskException {
		super(taskRepository);
		this.taskNameMemberIdMap = new HashMap<String, String>();
		String taskType = this.getTaskRepository().getTasksType();
		try {
			Group group = TasksDSComponent.getCoordinationService().createGroup(
					TASK_GROUP_BASE_NAME + taskType);
			if (checkServerCount) {
			    this.checkServerCount(group);
			}
			this.clusterComm = new ClusterGroupCommunicator(group);
		} catch (Exception e) {
			throw new TaskException("Error in creating clustered task manager for task type: " + 
		            taskType, Code.UNKNOWN, e);
		}
	}
	
	private void checkServerCount(Group group) throws Exception {
		int serverCount = 1;
		try {
			String countVal = System.getProperty(TASK_SERVER_COUNT_SYS_PROP);
			if (countVal != null) {
		        serverCount = Integer.parseInt(countVal);
			}
		} catch (Exception ignore) {
			log.warn("Invalid value for Java system property: " + TASK_SERVER_COUNT_SYS_PROP);
		}
		log.info("Waiting for " + serverCount + " task servers...");
		group.waitForMemberCount(serverCount);
		log.info("All task servers activated.");
	}
	
	public Map<String, String> getTaskNameMemberIdMap() {
		return taskNameMemberIdMap;
	}
	
	public ClusterGroupCommunicator getClusterComm() {
		return clusterComm;
	}

	public void scheduleAllTasks() throws TaskException {
		if (this.getClusterComm().isLeader()) {
			log.info("Scheduling all tasks...");
			List<TaskInfo> tasks = this.getAllTasks();
			for (TaskInfo task : tasks) {
				this.scheduleTask(task.getName());
			}
		}
	}
	
	private void scheduleMissingTasks() throws TaskException {
		List<List<TaskInfo>> tasksInServers = this.getAllTasksInServers();
		List<TaskInfo> scheduledTasks = new ArrayList<TaskInfo>();
		for (List<TaskInfo> entry : tasksInServers) {
			scheduledTasks.addAll(entry);
		}
		List<TaskInfo> allTasks = this.getAllTasks();
		List<TaskInfo> missingTasks = new ArrayList<TaskInfo>(allTasks);
		missingTasks.removeAll(scheduledTasks);
		for (TaskInfo task : missingTasks) {
			this.scheduleTask(task.getName());
		}
	}
	
	@Override
	public void scheduleTask(String taskName) throws TaskException {
		try {
			/* first call the leader node and get the target member id */
		    String memberId = this.getClusterComm().getMemberIdFromTaskName(taskName);
		    /* now talk directly to the target member */
		    this.getClusterComm().scheduleTask(memberId, taskName);
		} catch (Exception e) {
			throw new TaskException("Error in scheduling task: " + taskName + " : "
		            + e.getMessage(), Code.UNKNOWN, e);
		}
	}
	
	@Override
	public void rescheduleTask(String taskName) throws TaskException {
		try {
			String memberId = this.getClusterComm().getMemberIdFromTaskName(taskName);
		    this.getClusterComm().rescheduleTask(memberId, taskName);
		} catch (Exception e) {
			throw new TaskException("Error in rescheduling task: " + taskName + " : "
		            + e.getMessage(), Code.UNKNOWN, e);
		}
	}
	
	@Override
	public List<TaskInfo> getTasksInServer(int location) throws TaskException {
		try {
			List<String> ids = this.getClusterComm().getGroup().getMemberIds();
		    String memberId = ids.get(location % ids.size());
		    return this.getClusterComm().getTasksInServer(memberId);
		} catch (Exception e) {
			throw new TaskException("Error in getting tasks in server: " + location + " : " +
		            e.getMessage(), Code.UNKNOWN, e);
		}
	}
	
	@Override
	public Map<String, TaskState> getAllTaskStates() throws TaskException {
		try {
			List<TaskInfo> tasks = this.getAllTasks();
		    Map<String, TaskState> result = new HashMap<String, TaskState>();
		    for (TaskInfo task : tasks) {
		    	result.put(task.getName(), this.getTaskState(task.getName()));
		    }
		    return result;
		} catch (Exception e) {
			throw new TaskException("Error in getting all task states: " + 
		            e.getMessage(), Code.UNKNOWN, e);
		}
	}
	
	@Override
	public TaskState getTaskState(String taskName) throws TaskException {
		try {
			String memberId = this.getClusterComm().getMemberIdFromTaskName(taskName);
		    return this.getClusterComm().getTaskState(memberId, taskName);
		} catch (Exception e) {
			throw new TaskException("Error in getting task state: " + taskName + " : "
		            + e.getMessage(), Code.UNKNOWN, e);
		}
	}

	@Override
	public void deleteTask(String taskName) throws TaskException {
		try {
		    String memberId = this.getClusterComm().getMemberIdFromTaskName(taskName);
		    this.getClusterComm().deleteTask(memberId, taskName);
		    /* the delete has to be done here, because, this would be the admin node with read/write
		     * registry access, and the target slave will not have write access */
		    this.getTaskRepository().deleteTask(taskName);
		} catch (Exception e) {
			throw new TaskException("Error in deleting task: " + taskName + " : "
		            + e.getMessage(), Code.UNKNOWN, e);
		}
	}

	@Override
	public void pauseTask(String taskName) throws TaskException {
		try {
		    String memberId = this.getClusterComm().getMemberIdFromTaskName(taskName);
		    this.getClusterComm().pauseTask(memberId, taskName);
		} catch (Exception e) {
			throw new TaskException("Error in pausing task: " + taskName + " : "
		            + e.getMessage(), Code.UNKNOWN, e);
		}
	}
	
	@Override
	public void resumeTask(String taskName) throws TaskException {
		try {
		    String memberId = this.getClusterComm().getMemberIdFromTaskName(taskName);
		    this.getClusterComm().resumeTask(memberId, taskName);
		} catch (Exception e) {
			throw new TaskException("Error in resuming task: " + taskName + " : "
		            + e.getMessage(), Code.UNKNOWN, e);
		}
	}

	@Override
	public void registerTask(TaskInfo taskInfo) throws TaskException {
		this.registerLocalTask(taskInfo);
	}

	@Override
	public TaskInfo getTask(String taskName) throws TaskException {
		return this.getTaskRepository().getTask(taskName);
	}

	@Override
	public List<TaskInfo> getAllTasks() throws TaskException {
		return this.getTaskRepository().getAllTasks();
	}

	@Override
	public int getServerCount() throws TaskException {
		try {
			return this.getClusterComm().getGroup().getMemberIds().size();
		} catch (CoordinationException e) {
			throw new TaskException("Error in getting server count: " + e.getMessage(), 
					Code.UNKNOWN, e);
		}
	}

	private TaskServiceContext getTaskServiceContext() throws Exception {
		TaskServiceContext context = new TaskServiceContext(this.getAllTasks(), 
				this.getServerCount());
		return context;
	}
	
	private String locateMemberForTask(String taskName) throws Exception {
		int location = getTaskLocation(taskName);
		List<String> ids = this.getClusterComm().getGroup().getMemberIds();
		int index = location % ids.size();
		return ids.get(index);
	}
	
	private int getTaskLocation(String taskName) throws Exception {
		TaskInfo taskInfo = this.getTask(taskName);
		TaskLocationResolver locationResolver = (TaskLocationResolver) Class.forName(
				taskInfo.getLocationResolverClass()).newInstance();
		return locationResolver.getLocation(this.getTaskServiceContext(), taskInfo);
	}
	
	@Override
	public List<List<TaskInfo>> getAllTasksInServers() throws TaskException {
		List<List<TaskInfo>> result = new ArrayList<List<TaskInfo>>();
		try {
			List<String> ids = this.getClusterComm().getGroup().getMemberIds();
			for (int i = 0; i < ids.size(); i++) {
				result.add(this.getTasksInServer(i));
			}
		} catch (CoordinationException e) {
			throw new TaskException("Error in retreiving all tasks in servers: " + 
		            e.getMessage(), Code.UNKNOWN, e);
		}
		return result;
	}

	@Override
	public boolean isTaskScheduled(String taskName) throws TaskException {
		return false;
	}
	
	private class ClusterGroupCommunicator implements GroupEventListener {

		private Group group;
		
		private boolean leader;
		
		public ClusterGroupCommunicator(Group group) throws TaskException {
			this.group = group;
			this.getGroup().setGroupEventListener(this);
			try {
			    this.leader = this.getGroup().getLeaderId().equals(this.getGroup().getMemberId());
			} catch (CoordinationException e) {
				throw new TaskException("Error in creating cluster group communicator: " + 
			            e.getMessage(), Code.UNKNOWN, e);
			}
		}
		
		public boolean isLeader() {
			return leader;
		}
		
		public Group getGroup() {
			return group;
		}
		
		@Override
		public void onGroupMessage(byte[] data) {
		}

		@Override
		public void onLeaderChange(String leaderId) {
			if (log.isDebugEnabled()) {
				log.info("Task server leader changed: " + leaderId);
			}
			this.leader = leaderId.equals(this.getGroup().getMemberId());
			try {
				if (this.isLeader()) {
					log.info("Task server leader changed, rescheduling missing tasks...");
				    scheduleMissingTasks();
				}
			} catch (TaskException e) {
				log.error("Error in scheduling missing tasks: " + e.getMessage(), e);
			}
		}

		@Override
		public void onMemberArrival(String mid) {
			if (log.isDebugEnabled()) {
			    log.debug("New task member arrived: " + mid);
			}
		}

		@Override
		public void onMemberDeparture(String mid) {
			if (log.isDebugEnabled()) {
				log.debug("Task member departed: " + mid);
			}
			try {
				if (this.isLeader()) {
					this.adjustTaskNameMemberIdMapWithRemoval(mid);
					log.info("Task member departed, rescheduling missing tasks...");
					scheduleMissingTasks();
				}
			} catch (TaskException e) {
				log.error("Error in scheduling missing tasks: " + e.getMessage(), e);
			}
		}
		
		private void adjustTaskNameMemberIdMapWithRemoval(String mid) {
			Entry<String, String> entry;
			for (Iterator<Entry<String, String>> itr = getTaskNameMemberIdMap().entrySet().iterator(); itr.hasNext();) {
				entry = itr.next();
				if (entry.getValue().equals(mid)) {
					itr.remove();
				}
			}
		}
		
		private byte[] sendReceive(String memberId, String messageHeader, byte[] payload) 
				throws Exception {
			OperationRequest req = new OperationRequest(messageHeader, payload);
			byte[] result = this.getGroup().sendReceive(memberId, objectToBytes(req));
			OperationResponse res = (OperationResponse) bytesToObject(result);
			return res.getPayload();
		}
		
		public String getMemberIdFromTaskName(String taskName) throws Exception {
			byte[] result = this.sendReceive(this.getGroup().getLeaderId(), 
					OperationNames.MEMBER_ID_FROM_TASK_NAME, taskName.getBytes());
			return new String(result);
		}
		
		private String getMemberIdFromTaskNameServer(String taskName) throws Exception {
			String value = getTaskNameMemberIdMap().get(taskName);
			if (value == null) {
				value = locateMemberForTask(taskName);
				getTaskNameMemberIdMap().put(taskName, value);
			} 
			return value;
		}
		
		@SuppressWarnings("unchecked")
		public List<TaskInfo> getTasksInServer(String memberId) throws Exception {
			byte[] data = this.sendReceive(memberId, OperationNames.GET_TASKS_IN_SERVER,
					new byte[0]);
			return (List<TaskInfo>) bytesToObject(data);
		}
		
		public List<TaskInfo> getTasksInServerServer() throws Exception {
			return getAllLocalScheduledTasks();
		}
		
		public TaskState getTaskState(String memberId, String taskName) throws Exception {
			byte[] data = this.sendReceive(memberId, OperationNames.GET_TASK_STATE,
					new byte[0]);
			return (TaskState) bytesToObject(data);
		}
		
		public TaskState getTaskStateServer(String taskName) throws Exception {
			return getLocalTaskState(taskName);
		}
		
		public void scheduleTask(String memberId, String taskName) throws Exception {
			this.sendReceive(memberId, OperationNames.SCHEDULE_TASK, taskName.getBytes());
		}
		
        private void scheduleTaskServer(String taskName) throws Exception {
			scheduleLocalTask(taskName);
		}
        
		public void rescheduleTask(String memberId, String taskName) throws Exception {
			this.sendReceive(memberId, OperationNames.RESCHEDULE_TASK, taskName.getBytes());
		}
		
        private void rescheduleTaskServer(String taskName) throws Exception {
			rescheduleLocalTask(taskName);
		}
        
		public void deleteTask(String memberId, String taskName) throws Exception {
			this.sendReceive(memberId, OperationNames.DELETE_TASK, taskName.getBytes());
		}
		
        private void deleteTaskServer(String taskName) throws Exception {
			deleteLocalTask(taskName, false);
		}
        
        public void pauseTask(String memberId, String taskName) throws Exception {
			this.sendReceive(memberId, OperationNames.PAUSE_TASK, taskName.getBytes());
		}
		
        private void pauseTaskServer(String taskName) throws Exception {
			pauseLocalTask(taskName);
		}
        
        public void resumeTask(String memberId, String taskName) throws Exception {
			this.sendReceive(memberId, OperationNames.RESUME_TASK, taskName.getBytes());
		}
		
        private void resumeTaskServer(String taskName) throws Exception {
			resumeLocalTask(taskName);
		}

		@Override
		public byte[] onPeerMessage(byte[] buff) throws CoordinationException {
			try {
				byte[] result = null;
			    OperationRequest req = (OperationRequest) bytesToObject(buff);
			    if (OperationNames.MEMBER_ID_FROM_TASK_NAME.equals(req.getOpName())) {
			    	result = this.getMemberIdFromTaskNameServer(
			    			new String(req.getPayload())).getBytes();
			    } else if (OperationNames.SCHEDULE_TASK.equals(req.getOpName())) {
			    	this.scheduleTaskServer(new String(req.getPayload()));
			    	result = new byte[0];
			    } else if (OperationNames.RESCHEDULE_TASK.equals(req.getOpName())) {
			    	this.rescheduleTaskServer(new String(req.getPayload()));
			    	result = new byte[0];
			    } else if (OperationNames.DELETE_TASK.equals(req.getOpName())) {
			    	this.deleteTaskServer(new String(req.getPayload()));
			    	result = new byte[0];
			    } else if (OperationNames.PAUSE_TASK.equals(req.getOpName())) {
			    	this.pauseTaskServer(new String(req.getPayload()));
			    	result = new byte[0];
			    } else if (OperationNames.RESUME_TASK.equals(req.getOpName())) {
			    	this.resumeTaskServer(new String(req.getPayload()));
			    	result = new byte[0];
			    } else if (OperationNames.GET_TASKS_IN_SERVER.equals(req.getOpName())) {
			    	List<TaskInfo> tasks = this.getTasksInServerServer();
			    	result = objectToBytes(tasks);
			    } else if (OperationNames.GET_TASK_STATE.equals(req.getOpName())) {
			    	TaskState taskState = this.getTaskStateServer(new String(req.getPayload()));
			    	result = objectToBytes(taskState);
			    }
			    OperationResponse res = new OperationResponse(result);
			    return objectToBytes(res);
			} catch (Exception e) {
				e.printStackTrace();
				throw new CoordinationException("Error in handling peer message: " + 
			        e.getMessage(), ExceptionCode.GENERIC_ERROR, e);
			}
		}
		
	}
	
	private static byte[] objectToBytes(Object obj) throws Exception {
		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		ObjectOutputStream objOut = new ObjectOutputStream(byteOut);
		objOut.writeObject(obj);
		objOut.close();
		return byteOut.toByteArray();
	}
	
	private static Object bytesToObject(byte[] data) throws Exception {
		ByteArrayInputStream byteIn = new ByteArrayInputStream(data);
		ObjectInputStream objIn = new ObjectInputStream(byteIn);
		Object obj = objIn.readObject();
		objIn.close();
		return obj;
	}
	
	public static class OperationRequest implements Serializable {
		
		private static final long serialVersionUID = 1L;

		private String opName;
		
		private byte[] payload;
		
		public OperationRequest(String opName, byte[] payload) {
			this.opName = opName;
			this.payload = payload;
		}

		public String getOpName() {
			return opName;
		}

		public byte[] getPayload() {
			return payload;
		}

	}
	
	public static class OperationResponse implements Serializable {
				
		private static final long serialVersionUID = 1L;
		
		private byte[] payload;
		
		public OperationResponse(byte[] payload) {
			this.payload = payload;
		}

		public byte[] getPayload() {
			return payload;
		}

	}
	
	public static final class OperationNames {
		
		public static final String MEMBER_ID_FROM_TASK_NAME = "MEMBER_ID_FROM_TASK_NAME";
		public static final String SCHEDULE_TASK = "SCHEDULE_TASK";
		public static final String RESCHEDULE_TASK = "RESCHEDULE_TASK";
		public static final String DELETE_TASK = "DELETE_TASK";
		public static final String PAUSE_TASK = "PAUSE_TASK";
		public static final String RESUME_TASK = "RESUME_TASK";
		public static final String GET_TASKS_IN_SERVER = "GET_TASKS_IN_SERVER";
		public static final String GET_TASK_STATE = "GET_TASK_STATE";
		
	}

}
