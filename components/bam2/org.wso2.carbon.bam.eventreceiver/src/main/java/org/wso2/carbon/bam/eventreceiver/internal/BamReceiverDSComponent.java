package org.wso2.carbon.bam.eventreceiver.internal;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.agent.server.AgentServer;
import org.wso2.carbon.agent.server.exception.AgentServerException;
import org.wso2.carbon.bam.eventreceiver.datastore.CassandraConnector;
import org.wso2.carbon.identity.authentication.AuthenticationService;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * @scr.component name="org.wso2.carbon.bam.eventreceiver.component" immediate="true"
 * @scr.reference name="user.realmservice.default" interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1" policy="dynamic" bind="setRealmService"  unbind="unsetRealmService"
 * @scr.reference name="org.wso2.carbon.identity.authentication.internal.AuthenticationServiceComponent"
 * interface="org.wso2.carbon.identity.authentication.AuthenticationService"
 * cardinality="1..1" policy="dynamic" bind="setAuthenticationService"  unbind="unsetAuthenticationService"
 */
public class BamReceiverDSComponent {
    private static Log log = LogFactory.getLog(BamReceiverDSComponent.class);

    private RealmService realmService;
    private AuthenticationService authenticationService;
    private AgentServer agentServer;


    protected void activate(ComponentContext componentContext) throws AgentServerException {
        if (log.isDebugEnabled()) {
            log.debug("Starting the Bam Event Receiver Server component");
        }

        initialize();

        ServiceReference serviceReference = componentContext.getBundleContext().getServiceReference(AgentServer.class.getName());
        if (serviceReference != null) {
            agentServer = (AgentServer) componentContext.getBundleContext().getService(serviceReference);
        }

        agentServer.subscribe(new BAMAgentCallback());
    }

    protected void deactivate(ComponentContext componentContext) {
        if (log.isDebugEnabled()) {
            log.debug("Stopping the Bam Event Receiver Server component");
        }
    }

    protected void setRealmService(RealmService realmService) {
        this.realmService = realmService;
    }

    protected void unsetRealmService(RealmService realmService) {
        this.realmService = null;
    }

    protected void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    protected void unsetAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = null;
    }

    private void initialize() {
        // Create BAM_AGENT_API_META_DATA if not existing as a super tenant key space
        CassandraConnector connector = new CassandraConnector();
        connector.createKeySpaceIfNotExisting(CassandraConnector.BAM_META_KEYSPACE, "admin", "admin");

        // Create BAM meta column families if not existing 
        connector.createColumnFamily(null, CassandraConnector.BAM_META_STREAM_ID_CF, "admin", "admin");
        connector.createColumnFamily(null, CassandraConnector.BAM_META_STREAM_ID_KEY_CF, "admin", "admin");
        connector.createColumnFamily(null, CassandraConnector.BAM_META_STREAMID_TO_STREAM_ID_KEY, "admin", "admin");
        connector.createColumnFamily(null, CassandraConnector.BAM_META_STREAM_DEF_CF, "admin", "admin");
    }

}
