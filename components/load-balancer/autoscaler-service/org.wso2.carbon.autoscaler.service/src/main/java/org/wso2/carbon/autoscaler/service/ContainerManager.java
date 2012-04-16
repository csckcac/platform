package org.wso2.carbon.autoscaler.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.wso2.carbon.lb.common.dto.Container;
import org.wso2.carbon.lb.common.persistence.AgentPersistenceManager;

import java.sql.SQLException;

/**
 * This class provides capability of registering, unregistering, and the changing the status of
 * containers and handles the database calls related to those actions.
 */

public class ContainerManager {
	
    private static final Log log = LogFactory.getLog(ContainerManager.class);


    /**
     * This method will be called when apps are uploaded
     * This is the time to create a container for the tenant user. Note that call to
     * this method will be called after container created physically.
     *
     * Container will be stored in database.
     * @param container container
     * @throws Exception
     */
    public void registerContainer(Container container) throws ClassNotFoundException, SQLException {
        AgentPersistenceManager hostingPersistenceManager
                = AgentPersistenceManager.getPersistenceManager();
        hostingPersistenceManager.addContainer(container);


    }

    /**
     * This method will be called when the container is idle for some time. Note that call to
     * this method will delete the container from database and container is already deleted physically
     * as well.
     *
     * Container will be stored in database.
     * @param containerName
     * @throws Exception
     */
    public void unRegisterContainer(String containerName) throws ClassNotFoundException, SQLException {
        AgentPersistenceManager hostingPersistenceManager
                = AgentPersistenceManager.getPersistenceManager();
        hostingPersistenceManager.deleteContainer(containerName);


    }


    /**
     * This method will be called when the container needs to stop or start. It will change the status.
     * @param containerName
     * @param status
     * @throws Exception
     */
    public void changeContainerStatus (String containerName, Boolean status)
            throws  ClassNotFoundException, SQLException {
        AgentPersistenceManager hostingPersistenceManager
                = AgentPersistenceManager.getPersistenceManager();
        hostingPersistenceManager.changeContainerState(containerName, status);


    }







}

