/*
 * Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.autoscaler.service.jvm.agentmgt.impl;

import java.sql.SQLException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.autoscaler.service.jvm.agentmgt.exception.AgentAlreadyRegisteredException;
import org.wso2.carbon.autoscaler.service.jvm.agentmgt.exception.AgentNotAlreadyRegisteredException;
import org.wso2.carbon.autoscaler.service.jvm.agentmgt.exception.AgentNotFoundException;
import org.wso2.carbon.autoscaler.service.jvm.agentmgt.exception.AgentRegisteringException;
import org.wso2.carbon.autoscaler.service.jvm.agentmgt.exception.NullAgentException;
import org.wso2.carbon.autoscaler.service.jvm.agentmgt.IAgentManagementService;
import org.wso2.carbon.lb.common.dto.WorkerNode;
import org.wso2.carbon.lb.common.dto.Zone;
import org.wso2.carbon.lb.common.persistence.AgentPersistenceManager;

/**
 * Implements the IAgentManagementService interface.
 * 
 * @scr.component name="org.wso2.carbon.autoscaler.service.jvm.agentmgt"
 * 
 * @scr.service
 *              value=
 *              "org.wso2.carbon.autoscaler.service.jvm.agentmgt.IAgentManagementService"
 * 
 */
public class AgentManagementServiceImpl implements IAgentManagementService {
    
    private static final Log log = LogFactory.getLog(AgentManagementServiceImpl.class);

    public boolean registerAgent(WorkerNode workerNode, String epr) throws NullAgentException,
                                                               AgentAlreadyRegisteredException,
                                                               ClassNotFoundException,
                                                               SQLException,
                                                               AgentRegisteringException {
        boolean successfullyRegistered = false;
        Zone zone = new Zone();
        zone.setAvailable(true);
        zone.setName(workerNode.getZone());
        AgentPersistenceManager agentPersistenceManager
                = AgentPersistenceManager.getPersistenceManager();

        if (!agentPersistenceManager.isZoneExist(zone.getName())) {
            String msg = "Zone does not exists ";
            log.warn(msg);
            agentPersistenceManager.addZone(zone);
        } else {
            String msg = "Zone exist";
            log.warn(msg);
        }

        // For a successful registration EPR should not be null, EPR should not be already
        // registered and should successfully get added.
        // is EPR null?
        //String epr = workerNode.getEndPoint();
        if ( epr == null) {
            String msg = "EPR of the Agent is null.";
            log.error(msg);
            throw new NullAgentException(msg);
        }
        // is EPR already registered?
        else if (agentPersistenceManager.isWorkerNodeExist(epr)) {
            String msg = "EPR of the Agent (" + epr + ") is already registered.";
            log.error(msg);
            throw new AgentAlreadyRegisteredException(msg);
        }

        // is EPR successfully added?
        else if (agentPersistenceManager.addWorkerNode(workerNode, epr)) {
            log.info("Agent (" + epr + ") is successfully registered!");
            successfullyRegistered = true;
        }
        // EPR failed to add
        else {
            String msg = "EPR (" + epr + ") failed to get added to the data base.";
            log.error(msg);
            throw new AgentRegisteringException(msg);
        }
        return successfullyRegistered;
    }

    public boolean unregisterAgent(String epr) throws NullAgentException,
                                                      AgentNotAlreadyRegisteredException,
                                                      ClassNotFoundException, SQLException {
        boolean successfullyUnregistered = false;
        AgentPersistenceManager agentPersistenceManager
                        = AgentPersistenceManager.getPersistenceManager();
        // is EPR null?
        if (epr == null) {
            String msg = "EPR of the Agent is null.";
            log.error(msg);
            throw new NullAgentException(msg);
        }
        // is a registered EPR?
        else if (agentPersistenceManager.deleteWorkerNode(epr)) {
            log.info("Agent (" + epr + ") is successfully unregistered!");
            successfullyUnregistered = true;
        }
        // not a registered EPR
        else {
            String msg = "EPR (" + epr + ") is not a registered one.";
            log.error(msg);
            throw new AgentNotAlreadyRegisteredException(msg);
        }
        return successfullyUnregistered;
    }
    
    public String pickAnAgent() throws AgentNotFoundException {
        String selectedpr = "epr";
        //TODO get it from ContainerProvider
        return selectedpr;
    }    

}
