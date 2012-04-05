package org.wso2.carbon.hosting.mgt.persistence;

import org.wso2.carbon.hosting.mgt.dao.WorkerNodeDAO;
import org.wso2.carbon.hosting.mgt.dao.ZoneResourcePlanDAO;
import org.wso2.carbon.hosting.mgt.dto.WorkerNode;
import org.wso2.carbon.hosting.mgt.dto.ZoneResourcePlan;

/**
 * Created by IntelliJ IDEA.
 * User: lahiru
 * Date: 3/23/12
 * Time: 10:59 AM
 * To change this template use File | Settings | File Templates.
 */
public class HostingPersistenceManager {
    private static HostingPersistenceManager manager = new HostingPersistenceManager();

    /**
         * Returning the identity persistence manager instance : singleton pattern
         *
         * @return
         */
    public static HostingPersistenceManager getPersistenceManager()  {
        return manager;
    }

    public void addWorkerNode(WorkerNode workerNode){
        WorkerNodeDAO workerNodeDAO = new WorkerNodeDAO();
        workerNodeDAO.create(workerNode);
    }
    public void addZoneResourcePlan(ZoneResourcePlan zoneResourcePlan){
        ZoneResourcePlanDAO zoneResourcePlanDAO = new ZoneResourcePlanDAO();
        zoneResourcePlanDAO.create(zoneResourcePlan);
    }
    
    public boolean isZoneExist(String zone){
        ZoneResourcePlanDAO zoneResourcePlanDAO = new ZoneResourcePlanDAO();
        return zoneResourcePlanDAO.isZoneExist(zone);
    }
}
