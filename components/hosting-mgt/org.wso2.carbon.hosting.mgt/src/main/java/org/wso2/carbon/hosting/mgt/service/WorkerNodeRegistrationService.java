/**
 * 
 */
package org.wso2.carbon.hosting.mgt.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.hosting.mgt.dao.WorkerNodeDAO;
import org.wso2.carbon.hosting.mgt.dao.ZoneResourcePlanDAO;
import org.wso2.carbon.hosting.mgt.dto.Bridge;
import org.wso2.carbon.hosting.mgt.dto.WorkerNode;
import org.wso2.carbon.hosting.mgt.dto.ZoneResourcePlan;
import org.wso2.carbon.hosting.mgt.persistence.HostingPersistenceManager;

import java.util.ArrayList;

/**
 * This service handle zone registration and worker node registration. A worker node is added to a
 * zone. The zone consists a Zone Resource plan which contain the resources defined to be allocated
 * to the containers created in the worker nodes of that zone.
 */
public class WorkerNodeRegistrationService {

    private static final Log log = LogFactory.getLog(WorkerNodeRegistrationService.class);

    /**
         * Register a worker node when it is physically added to a zone. When a worker node is first
         * created it will first check whether the zone already exist. If not this method will call
         * methods to register it first.
     * @param workerNode
     * @param zoneResourcePlan
     */
    public void registerWorkerNode(WorkerNode workerNode, ZoneResourcePlan zoneResourcePlan)
    {
        HostingPersistenceManager hostingPersistenceManager = new HostingPersistenceManager();

        if (!hostingPersistenceManager.isZoneExist(zoneResourcePlan.getZone())) {
            String msg = "Zone not exists !";
            log.info(msg);
            hostingPersistenceManager.addZoneResourcePlan(zoneResourcePlan);
        } else {
            String msg = "Zone exist";
            log.info(msg);
        }
        hostingPersistenceManager.addWorkerNode(workerNode);

    }


}
