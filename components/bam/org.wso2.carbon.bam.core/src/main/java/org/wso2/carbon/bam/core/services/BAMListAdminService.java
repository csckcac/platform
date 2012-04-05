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

package org.wso2.carbon.bam.core.services;

import org.wso2.carbon.bam.common.dataobjects.activity.*;
import org.wso2.carbon.bam.common.dataobjects.common.ClientDO;
import org.wso2.carbon.bam.common.dataobjects.common.MonitoredServerDTO;
import org.wso2.carbon.bam.common.dataobjects.service.OperationDO;
import org.wso2.carbon.bam.common.dataobjects.service.ServerDO;
import org.wso2.carbon.bam.common.dataobjects.service.ServiceDO;
import org.wso2.carbon.bam.core.persistence.BAMPersistenceManager;
import org.wso2.carbon.bam.core.util.BAMUtil;
import org.wso2.carbon.bam.util.BAMException;
import org.wso2.carbon.core.AbstractAdmin;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class BAMListAdminService extends AbstractAdmin {
    private BAMPersistenceManager persistenceManager;

    public BAMListAdminService() {
        persistenceManager = BAMPersistenceManager.getPersistenceManager(BAMUtil.getRegistry());
    }

    public MonitoredServerDTO[] getServerList() throws BAMException {
        int tenantId = BAMUtil.getTenantID(getTenantDomain());
        List<ServerDO> servers = persistenceManager.getMonitoredServers(tenantId);
        List<MonitoredServerDTO> list = new ArrayList<MonitoredServerDTO>();
        if (servers != null) {
            for (ServerDO s : servers) {
                MonitoredServerDTO dto = new MonitoredServerDTO();
                dto.setServerId(s.getId());
                dto.setServerURL(s.getServerURL());
                dto.setServerType(s.getServerType());
                dto.setCategory(s.getCategory());
                list.add(dto);
            }
        }
        return list.toArray(new MonitoredServerDTO[list.size()]);
    }

    public ServerDO[] getServerListWithCategoryName() throws BAMException{
        int tenantId = BAMUtil.getTenantID(getTenantDomain());
        List<ServerDO> servers = persistenceManager.getMonitoredServerListWithCategoryName(tenantId);
        if(servers!=null){
            return servers.toArray(new ServerDO[servers.size()]);
        }
        return null;
    }

    public ServiceDO[] getServiceList(int serverID) throws BAMException {
        List<ServiceDO> services = persistenceManager.getAllServices(serverID);
        List<ServiceDO> svcList = new ArrayList<ServiceDO>();
        if (services != null) {
            for (ServiceDO svc : services) {
                ServiceDO dto = new ServiceDO();
                dto.setId(svc.getId());
                dto.setName(svc.getName());
                svcList.add(dto);
            }
        }
        return svcList.toArray(new ServiceDO[svcList.size()]);
    }

    public OperationDO[] getOperationList(int serviceId) throws BAMException {
        List<OperationDO> ops = persistenceManager.getAllOperations(serviceId);
        List<OperationDO> opList = new ArrayList<OperationDO>();
        if (ops != null) {
            for (OperationDO op : ops) {
                OperationDO dto = new OperationDO();
                dto.setOperationID(op.getOperationID());
                dto.setName(op.getName());
                opList.add(dto);
            }
        }
        return opList.toArray(new OperationDO[opList.size()]);
    }

    public ActivityDTO[] getActivityList() throws BAMException {

        List<ActivityDO> activities = persistenceManager.getAllActivities();
        List<ActivityDTO> actList = new ArrayList<ActivityDTO>();
        if (activities != null) {
            for (ActivityDO act : activities) {
                ActivityDTO dto = new ActivityDTO();
                dto.setActivityKeyId(act.getActivityKeyId());
                dto.setName(act.getName());
                dto.setDescription(act.getDescription());
                dto.setActivityId(act.getActivityId());
                actList.add(dto);
            }
        }
        return actList.toArray(new ActivityDTO[actList.size()]);

    }

    public MessageDTO[] getMessageList() throws BAMException {
        List<MessageDO> messages = persistenceManager.getAllMessages();
        List<MessageDTO> msgList = new ArrayList<MessageDTO>();
        if (messages != null) {
            for (MessageDO msg : messages) {
                MessageDTO dto = new MessageDTO();
                dto.setActivityKeyId(msg.getActivityKeyId());
                dto.setMessageKeyId(msg.getMessageKeyId());
                dto.setIPAddress(msg.getIPAddress());
                dto.setOperationId(msg.getOperationId());
                dto.setTimeStamp(Calendar.getInstance());
                dto.setUserAgent(msg.getUserAgent());
                dto.setMessageId(msg.getMessageId());
                msgList.add(dto);
            }
        }
        return msgList.toArray(new MessageDTO[msgList.size()]);

    }

    public ClientDTO[] getClientList(int serverID) throws BAMException {
        List<ClientDO> clients = persistenceManager.getAllClients(serverID);
        List<ClientDTO> cliList = new ArrayList<ClientDTO>();
        if (clients != null) {
            for (ClientDO client : clients) {
                ClientDTO dto = new ClientDTO();
                dto.setUUID(client.getUUID());
                dto.setName(client.getName());
                cliList.add(dto);
            }
        }
        return cliList.toArray(new ClientDTO[cliList.size()]);
    }

    public PropertyFilterDTO[] getXpathConfigurations(int serverID) throws BAMException {
        List<PropertyFilterDO> configs = persistenceManager.getAllXPathConfigurations(serverID);
        List<PropertyFilterDTO> configList = new ArrayList<PropertyFilterDTO>();
        if (configs != null) {
            for (PropertyFilterDO config : configs) {
                PropertyFilterDTO configDTO = new PropertyFilterDTO();
                configDTO.setId(config.getId());
                configDTO.setAlias(config.getAlias());
                configDTO.setExpressionKey(config.getExpressionKey());
                configDTO.setExpression(config.getExpression());
                configList.add(configDTO);
            }
        }

        return configList.toArray(new PropertyFilterDTO[configList.size()]);
    }

    public NamespaceDTO[] getNamespaces(int xpathId) throws BAMException {
        List<NamespaceDO> configs = persistenceManager.getAllNamespaces(xpathId);
        List<NamespaceDTO> configList = new ArrayList<NamespaceDTO>();
        if (configs != null) {
            for (NamespaceDO config : configs) {
                NamespaceDTO configDTO = new NamespaceDTO();
                configDTO.setId(config.getId());
                configDTO.setPrefix(config.getPrefix());
                configDTO.setUri(config.getUri());
                configList.add(configDTO);
            }
        }

        return configList.toArray(new NamespaceDTO[configList.size()]);
    }
}
