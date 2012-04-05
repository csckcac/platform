package org.wso2.carbon.autoscaler.service.recovery;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.autoscaler.agentmgt.service.IAgentManagementService;
import org.wso2.carbon.autoscaler.service.agent.clients.AgentServiceClient;
import org.wso2.carbon.autoscaler.service.internal.AgentManagementDSHolder;

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

                @SuppressWarnings("unused")
                AgentServiceClient agentService = new AgentServiceClient(epr);

                // if we connected to AgentService successfully, break the loop.
                break;

            } catch (Exception ex) {
                // no need to do anything till the time out comes
                try {
                    // waits for 5 seconds
                    Thread.sleep(5000);
                } catch (InterruptedException ignored) {
                }

            }
        }

        if (log.isDebugEnabled()) {
            log.debug("Tried to recover the AgentService (" + epr + ") for " + TIME_OUT +
                " milliseconds, but failed.");
        }

        IAgentManagementService agentManagementService =
            AgentManagementDSHolder.getInstance().getAgentMgtService();

        try {

            agentManagementService.unregisterAgentForcefully(epr);

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
