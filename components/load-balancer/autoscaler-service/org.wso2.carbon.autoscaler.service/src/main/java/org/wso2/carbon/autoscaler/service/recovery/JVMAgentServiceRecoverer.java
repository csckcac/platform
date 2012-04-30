package org.wso2.carbon.autoscaler.service.recovery;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.autoscaler.service.jvm.agentmgt.IAgentManagementService;
import org.wso2.carbon.autoscaler.service.agent.clients.AgentServiceClient;
import org.wso2.carbon.autoscaler.service.internal.AgentManagementDSHolder;
import org.wso2.carbon.autoscaler.service.registry.JVMAdapterRegistry;
import org.wso2.carbon.lb.common.persistence.AgentPersistenceManager;

/**
 * Creates a new thread to recover an Agent Service.
 */
public class JVMAgentServiceRecoverer extends Thread {

    private static final Log log = LogFactory.getLog(JVMAgentServiceRecoverer.class);

    /**
     * Time this tries to recover AgentService (in milliseconds).
     */
    private static final long TIME_OUT = 60000;

    private String epr;

    private boolean canStop = false;
    
    /**
     * Registry instance of JVM Adapter
     */
    private JVMAdapterRegistry registry = JVMAdapterRegistry.getInstance();

    public JVMAgentServiceRecoverer(String epr) {

        this.epr = epr;
    }

    public void run() {
        while (!canStop) {
            recoverService(epr);
        }
    }

    /**
     * Checks whether the Agent Service is up till {@link #TIME_OUT} occurred, in
     * intervals of 5 seconds.
     * If failed to recover before {@link #TIME_OUT}, this will try to unregister the
     * particular Agent.
     * FIXME: is {@value #TIME_OUT} enough?
     * 
     * @param epr
     */
    private void recoverService(String epr) {

        long startTime = System.currentTimeMillis();

        while ((System.currentTimeMillis() - startTime) < TIME_OUT) {
            try {

                AgentServiceClient agentService = new AgentServiceClient(epr);

                //do a dummy call and see whether the service is reachable
                agentService.getNumberOfInstances();
                
                // if we connected to AgentService successfully, set canStop to true and 
                // break the loop.
                canStop = true;
                break;

            } catch (RemoteException ex) {
                // no need to do anything till the time out comes
                try {
                    // waits for 5 seconds
                    Thread.sleep(5000);
                } catch (InterruptedException ignored) {
                }

            } catch (Exception ignored){
                //do nothing
            }
        }
        
        // if service is recovered.
        if (canStop) {
            
            // load from registry
            List<String> temporarilySkippedAgentEprList = 
                    registry.getTemporarilySkippedAgentEprList();
            
            // this service is recovered, hence this should not be kept in the 
            // temporarilySkippedAgentEprList.
            temporarilySkippedAgentEprList.remove(epr);
            
            // add it back to registry
            registry.setTemporarilySkippedAgentEprList(temporarilySkippedAgentEprList);

            //change the host machine availability true
            AgentPersistenceManager agentPersistenceManager = AgentPersistenceManager.getPersistenceManager();
            try {
                agentPersistenceManager.changeHostMachineAvailability(epr, true);
            } catch (SQLException e) {
                String msg = "Database error while changing host machine availability ";
                log.error(msg);
            }
            log.debug("Agent Service (" + epr + ") recovered successfully!");
            
        } else {

            // if not recovered.
            
            if (log.isDebugEnabled()) {
                log.debug("Tried to recover the AgentService (" + epr + ") for " + TIME_OUT +
                    " milliseconds, but failed.");
            }

        IAgentManagementService agentManagementService
                = AgentManagementDSHolder.getInstance().getAgentMgtService();

            try {

                agentManagementService.unregisterAgent(epr);

            } catch (Exception e) {
                String msg =
                    "Service (" + epr + ") is still not recoverable. " +
                        "Recoverying this service is stopped now. Tried to unregister the Agent" +
                        " but failed.";
                log.error(msg, e);
                throw new RuntimeException(msg, e);

            } finally {
                canStop = true;
            }

            log.error("Service (" + epr + ") is still not recoverable. " +
                "Recoverying this service is stopped now. Hence we removed this Agent Service.");
        }

    }

}
