package org.wso2.carbon.lb.common.persistence;

import org.wso2.carbon.lb.common.dao.ContainerDAO;
import org.wso2.carbon.lb.common.dao.WorkerNodeDAO;
import org.wso2.carbon.lb.common.dao.ZoneDAO;
import org.wso2.carbon.lb.common.dto.Container;
import org.wso2.carbon.lb.common.dto.WorkerNode;
import org.wso2.carbon.lb.common.dto.Zone;
import org.wso2.carbon.hosting.wnagent.stub.services.xsd.dto.ContainerInformation;

import java.sql.SQLException;

/**
 *
 *
 *
 */
public class    AgentPersistenceManager {
    private static AgentPersistenceManager manager = new AgentPersistenceManager();

    /**
         * Returning the hosting persistence manager instance : singleton pattern
         *
         * @return manager
         */
    public static AgentPersistenceManager getPersistenceManager()  {
        return manager;
    }

    public boolean addWorkerNode(WorkerNode workerNode, String epr) throws SQLException, ClassNotFoundException {
        WorkerNodeDAO workerNodeDAO = new WorkerNodeDAO();
        return workerNodeDAO.create(workerNode, epr);
    }
    
    public void makeWorkerNodeUnavailable(String endPoint)
            throws SQLException, ClassNotFoundException {
        WorkerNodeDAO workerNodeDAO =  new WorkerNodeDAO();
        workerNodeDAO.makeUnavailable(endPoint);
    }
    public boolean addZone(Zone zone)
            throws ClassNotFoundException, SQLException {
        ZoneDAO zoneResourcePlanDAO = new ZoneDAO();
        return zoneResourcePlanDAO.create(zone);
    }

    public void addContainer(Container container)
            throws ClassNotFoundException, SQLException {
        ContainerDAO containerDAO = new ContainerDAO();
        containerDAO.create(container);
    }

    public void deleteContainer(String containerName)
            throws ClassNotFoundException, SQLException {
        ContainerDAO containerDAO = new ContainerDAO();
        containerDAO.delete(containerName);
    }

    public boolean deleteWorkerNode(String epr)
            throws ClassNotFoundException, SQLException {
        WorkerNodeDAO workerNodeDAO = new WorkerNodeDAO();
        return workerNodeDAO.delete(epr);
    }

    public void changeContainerState(String containerName, Boolean state)
            throws ClassNotFoundException, SQLException {
        ContainerDAO containerDAO = new ContainerDAO();
        containerDAO.changeState(containerName, state);
    }
    public ContainerInformation retrieveAvailableContainerInformation(String zone)
            throws ClassNotFoundException, SQLException {
        ContainerDAO containerDAO = new ContainerDAO();
        return containerDAO.retrieveAvailableContainerInformation(zone);
    }
    
    public boolean isZoneExist(String zone) throws ClassNotFoundException, SQLException {
        ZoneDAO zoneDAO = new ZoneDAO();
        return zoneDAO.isZoneExist(zone);
    }
    public boolean isWorkerNodeExist(String endPoint) throws ClassNotFoundException, SQLException {
        WorkerNodeDAO workerNodeDAO = new WorkerNodeDAO();
        return workerNodeDAO.isWorkerNodeExist(endPoint);
    }
    public boolean isWorkerNodesAvailableInDomain(String domain) throws ClassNotFoundException, SQLException {
        WorkerNodeDAO workerNodeDAO = new WorkerNodeDAO();
        return workerNodeDAO.isAvailableInDomain(domain);
    }
}
