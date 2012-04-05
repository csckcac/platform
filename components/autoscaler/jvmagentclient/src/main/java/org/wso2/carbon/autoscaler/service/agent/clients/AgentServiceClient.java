package org.wso2.carbon.autoscaler.service.agent.clients;

import java.rmi.RemoteException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.autoscaler.agent.service.stub.AgentServiceStub;

public class AgentServiceClient {

    private static final Log log = LogFactory.getLog(AgentServiceClient.class);

    private AgentServiceStub stub;

    public AgentServiceClient(String backendServerURL) throws Exception {

        String epr = backendServerURL + "AgentService";

        try {
            stub = new AgentServiceStub(epr);

        } catch (RemoteException ex) {
            String msg = "Failed to initiate a client for AgentService at " + epr + ".";
            log.error(msg, ex);
            throw new Exception(ex);
        }
    }

    public boolean registerInAgentManagementService() throws Exception {

        return stub.registerInAgentManagementService();
    }

    public boolean unregisterInAgentManagementService() throws Exception {

        return stub.unregisterInAgentManagementService();
    }

}
