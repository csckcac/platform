package org.wso2.carbon.lb.common.persistence;

import org.wso2.carbon.lb.common.dao.ContainerDAO;
import org.wso2.carbon.lb.common.dao.HostMachineDAO;
import org.wso2.carbon.lb.common.dao.ZoneDAO;
import org.wso2.carbon.lb.common.dto.Container;
import org.wso2.carbon.lb.common.dto.ContainerInformation;
import org.wso2.carbon.lb.common.dto.HostMachine;
import org.wso2.carbon.lb.common.dto.Zone;

import java.sql.SQLException;

/**
 *   This class is written to interface all the dao classes through a singleton class. All the access
 *   requests to database are gone through this class. Therefore there is a relevant method in this
 *   class for all the public methods in all the DAO classes.
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

    public boolean addHostMachine(HostMachine hostMachine) throws SQLException, ClassNotFoundException {
        HostMachineDAO hostMachineDAO = new HostMachineDAO();
        return hostMachineDAO.create(hostMachine);
    }
    
    public void makeWorkerNodeUnavailable(String endPoint)
            throws SQLException, ClassNotFoundException {
        HostMachineDAO hostMachineDAO =  new HostMachineDAO();
        hostMachineDAO.makeUnavailable(endPoint);
    }
    public boolean addZone(Zone zone, String[] domains)
            throws ClassNotFoundException, SQLException {
        ZoneDAO zoneResourcePlanDAO = new ZoneDAO();
        return zoneResourcePlanDAO.create(zone, domains);
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
        HostMachineDAO hostMachineDAO = new HostMachineDAO();
        return hostMachineDAO.delete(epr);
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
    public boolean isHostMachineExist(String endPoint) throws ClassNotFoundException, SQLException {
        HostMachineDAO hostMachineDAO = new HostMachineDAO();
        return hostMachineDAO.isHostMachineExist(endPoint);
    }
    public boolean isHostMachinesAvailableInDomain(String domain) throws ClassNotFoundException, SQLException {
        HostMachineDAO hostMachineDAO = new HostMachineDAO();
        return hostMachineDAO.isAvailableInDomain(domain);
    }
}
