package org.wso2.carbon.bam.eventreceiver.internal;

import org.apache.log4j.Logger;
import org.wso2.carbon.agent.server.exception.AgentServerException;
import org.wso2.carbon.agent.server.internal.AbstractAgentServer;
import org.wso2.carbon.agent.server.internal.ThriftAgentServer;
import org.wso2.carbon.agent.server.internal.authentication.CarbonAuthenticationHandler;
import org.wso2.carbon.bam.eventreceiver.datastore.CassandraStreamDefinitionStore;
import org.wso2.carbon.identity.authentication.AuthenticationService;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * Created by IDE.
 * User: deep
 * Date: 4/30/12
 * Time: 12:13 PM
 */
public class ThriftServer {
    Logger log = Logger.getLogger(ThriftServer.class);
    AbstractAgentServer agentServer;
    private RealmService realmService;
    private AuthenticationService authenticationService;

    public ThriftServer(RealmService realmService, AuthenticationService authenticationService) {
        realmService = realmService;
        authenticationService = authenticationService;
        //To change body of created methods use File | Settings | File Templates.
    }

    public static void main(String[] args) {
        System.out.println("Starting Thrift Server in BAM Receiver: ");
    }


    public void start(int receiverPort) throws AgentServerException {

        // org.wso2.carbon.bam.receiver.KeyStoreUtil.setKeyStoreParams();
        agentServer = new ThriftAgentServer(receiverPort, new CarbonAuthenticationHandler(authenticationService), new CassandraStreamDefinitionStore());
        agentServer.subscribe(new BAMAgentCallback());
        agentServer.start();
        log.info("Bam Receiver ThriftServer is Started");
    }

    public void stop() {
        agentServer.stop();
        log.info("Bam Receiver ThriftServer is Stoped");
    }

    /**
     *  For testing
     */
    //
//    public void testServerTest() throws AgentServerException, InterruptedException {
//       ThriftServer testServer = new ThriftServer();
//        testServer.start(7611);
//        Thread.sleep(1000);
//        testServer.stop();
//    }

}
