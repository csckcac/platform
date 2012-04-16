package org.wso2.carbon.autoscaler.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.lb.common.persistence.AgentPersistenceManager;
import org.wso2.carbon.hosting.wnagent.stub.services.xsd.dto.ContainerInformation;

import java.sql.SQLException;

/**
 *
 * This class is for separating the container selection algorithm. This particular algorithm will
 * select the container based on the load of the worker nodes according to the given zone. That means
 * Initially it will select the Worker nodes which belongs to the input zone. Then it will search
 * the worker node which is currently having least load out of the set. This may be replaced by a
 * different algorithm later according to requirements.E.g. Round robin among worker nodes.
 *
 */
public class
        ContainerProvider {
    private static final Log log = LogFactory.getLog(ContainerProvider.class);

    /**
     * This method will be called when tenant upload applications and physically going to create the
     * container. This may be also called when the container was idle for a reasonable time and it was
     * destroyed, but now we need it again since the traffic is developing(requests are coming).
     * Here it needs to find an available ip, bridge, and worker node for the given zone.Request
     * should determine the zone from input before calling this, we have to find the rest from it.
     *
     * @param zone
     * @throws Exception
     */
    public ContainerInformation retrieveAvailableContainerInformation (String zone, String jailKeysFile)
            throws ClassNotFoundException, SQLException {

        ContainerInformation containerInformation = new ContainerInformation();

        AgentPersistenceManager agentPersistenceManager
                = AgentPersistenceManager.getPersistenceManager();
        if(agentPersistenceManager.isZoneExist(zone)){
            containerInformation = agentPersistenceManager.retrieveAvailableContainerInformation(zone);
            containerInformation.setJailKeysFile(jailKeysFile);
        }else {
            String msg = "Requested zone is not exist !";
            log.error(msg);
        }
        return containerInformation;
    }
}
