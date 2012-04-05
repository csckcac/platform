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
package org.wso2.carbon.bam.core.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.common.dataobjects.activity.ActivityDO;
import org.wso2.carbon.bam.common.dataobjects.activity.MessageDO;
import org.wso2.carbon.bam.common.dataobjects.activity.NamespaceDO;
import org.wso2.carbon.bam.common.dataobjects.activity.PropertyFilterDO;
import org.wso2.carbon.bam.common.dataobjects.common.ClientDO;
import org.wso2.carbon.bam.common.dataobjects.common.TenantDO;
import org.wso2.carbon.bam.common.dataobjects.service.OperationDO;
import org.wso2.carbon.bam.common.dataobjects.service.ServerDO;
import org.wso2.carbon.bam.common.dataobjects.service.ServiceDO;
import org.wso2.carbon.bam.util.BAMException;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BAMConfigurationCache {
	private static final Log log = LogFactory.getLog(BAMConfigurationCache.class);
	private static Map<String,ServiceDO> serviceMap = new ConcurrentHashMap<String,ServiceDO>();
	private static Map<String,ActivityDO> activityUserDefinedIDMap = new ConcurrentHashMap<String,ActivityDO>();
	private static Map<Integer,ActivityDO> activityIDMap = new ConcurrentHashMap<Integer,ActivityDO>();
	private static Map<String,ServerDO> tenantServerMap = new ConcurrentHashMap<String,ServerDO>();
	private static Map<String,ServerDO> serverMap = new ConcurrentHashMap<String,ServerDO>();
	private static Map<String,OperationDO> operationMap = new ConcurrentHashMap<String,OperationDO>();


	public static TenantDO getTenant(int tenantID) throws BAMException {
		// Nothing is chached for the moment.
		return null;
		// //TODO if in, cache return from cache
		// try {
		// return new BAMConfigurationDSClient(BAMUtil.getBackendServerURLHTTPS())
		// .getTenant(tenantID);
		// } catch (RemoteException e) {
		// throw new BAMException("unable to request tenant from DS", e);
		// } catch (SocketException e) {
		// throw new BAMException("unable to request tenant from DS", e);
		// }
	}

	public static ServerDO getMonitoredServer(int serverID) throws BAMException {
		return null;
		// MonitoredServer server = BAMUtil.getServersListCache().getServer(serverID);
		// if (server == null) {
		// //TODO
		// //TODO
		// }
		//
		// return server;
	}

	public static ServiceDO getService(int serviceID) throws BAMException {
		return null;
		// try {
		// return new BAMConfigurationDSClient(BAMUtil.getBackendServerURLHTTPS())
		// .getService(serviceID);
		// } catch (SocketException e) {
		// throw new BAMException("unable to request service from DS", e);
		// } catch (RemoteException e) {
		// throw new BAMException("unable to request service from DS", e);
		// }
	}

	/**
	 * Retrieving Service using serverID and ServiceName
	 * @param serverID
	 * @param serviceName
	 * @return
	 */
	public static ServiceDO getService(int serverID, String serviceName) {
		ServiceDO service = serviceMap.get(serverID+"_"+serviceName);
		return service;
	}

	/**
	 * Storing Service against serverID and ServiceName
	 * @param serverID
	 * @param service
	 */
	public static void addService(int serverID,ServiceDO service) {
		serviceMap.put(serverID+"_"+service.getName(),service);
	}

	public static void addService(ServiceDO service) {
		// TODO
	}

	public static OperationDO getOperation(int operationId) {
		return null;
	}

	public static void addTenant(TenantDO tenant) {
		// TODO
	}

	public static List<ServiceDO> getAllServices(int serverID) {
		return null;
	}

	public static List<OperationDO> getAllOperations(int serviceID) {
		return null;
	}

	public static List<OperationDO> getAllOperations() {
		return null;
	}

	// //////////////////////Activity Need to store in cache?????????????????????
	public static ActivityDO getActivity(int activityID) {
		ActivityDO activityDO = activityIDMap.get(activityID);
		return activityDO;
	}

	public static void addActivity(int activityID,ActivityDO activity) {
		activityIDMap.put(Integer.valueOf(activityID), activity);
	}

	public static ActivityDO getActivity(String userDefineActivityID) {
		ActivityDO activityDO = activityUserDefinedIDMap.get(userDefineActivityID);
		return activityDO;
	}

	public static ActivityDO getActivity(String name, String description) {
		// TODO
		return null;
	}

	public static void addActivity(String userDefineActivityID,ActivityDO activity) {
		activityUserDefinedIDMap.put(userDefineActivityID, activity);
	}

    public static void addXpathConfig(PropertyFilterDO xpathConfig) {
        // TODO
    }

    public static void addNamespace(NamespaceDO ns) {
        // TODO
    }

	public static List<ActivityDO> getAllActivitiesForDescription(String description) {
		return null;
	}

	public static List<ActivityDO> getAllActivities() {
		return null;
	}

    public static List<PropertyFilterDO> getAllXPathConfigurations(int serverId) {
        return null;
    }

    public static List<NamespaceDO> getAllNamespaces(int xpathId) {
        return null;
    }

	// //////////////////////Message Need to store in cache?????????????????????
    public static MessageDO getMessage(int messageKeyId) {
		// TODO
		return null;
	}
	public static MessageDO getMessage(String messageId) {
        // TODO
        return null;
    }

	public static MessageDO getMessageForOperation(int operationID, String requestMessageID) {
		// TODO
		return null;
	}

	public static void addMessage(MessageDO message) {
		// TODO
	}

	public static List<MessageDO> getAllMessagesForOperationID(int operationID) {
		return null;
	}

	public static List<MessageDO> getAllMessages(int activityID) {
		return null;
	}

	public static List<MessageDO> getAllMessages() {
		return null;
	}

	public static List<ClientDO> getAllClients(int serverID) {
		return null;
	}

	public static void addClient(ClientDO client) {
		// TODO
	}


	/**
	 *
	 * @param tenantId
	 * @param URL
	 * @return
	 */
	public static ServerDO getServer(int tenantId, String URL) {
		ServerDO server = tenantServerMap.get(tenantId+"_"+URL);
		return server;
	}

	/**
	 *
	 * @param tenantId
	 * @param server
	 */
	public static void addServer(int tenantId, ServerDO server) {
		tenantServerMap.put(tenantId+"_"+server.getServerURL(),server);
	}


	public static ServerDO getServer(String URL) {
		ServerDO server = serverMap.get(URL);
		return server;
	}


	public static void addServer(ServerDO server) {
		serverMap.put(server.getServerURL(),server);
	}


	public static OperationDO getOperation(int serviceId,String operationName) {
		OperationDO operation = operationMap.get(String.valueOf(serviceId)+"_"+operationName);
		return operation;
	}

	public static void addOperation(OperationDO operation) {
		String key = String.valueOf(operation.getServiceID())+"_"+operation.getName();
		operationMap.put(key,operation);
	}
}
