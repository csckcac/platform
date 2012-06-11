/*
 * Copyright 2012 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.bam.eventreceiver.internal;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.agent.server.AgentServer;
import org.wso2.carbon.agent.server.exception.AgentServerException;
import org.wso2.carbon.bam.eventreceiver.BAMEventReceiverComponentManager;
import org.wso2.carbon.bam.eventreceiver.datastore.CassandraConnector;
import org.wso2.carbon.identity.authentication.AuthenticationService;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * @scr.component name="org.wso2.carbon.bam.eventreceiver.component" immediate="true"
 * @scr.reference name="user.realmservice.default" interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1" policy="dynamic" bind="setRealmService"  unbind="unsetRealmService"
 * @scr.reference name="org.wso2.carbon.identity.authentication.internal.AuthenticationServiceComponent"
 * interface="org.wso2.carbon.identity.authentication.AuthenticationService"
 * cardinality="1..1" policy="dynamic" bind="setAuthenticationService"  unbind="unsetAuthenticationService"
 * @scr.reference name="registry.service" interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic" bind="setRegistryService" unbind="unsetRegistryService"
 */
public class BAMEventReceiverDSComponent {
    private static Log log = LogFactory.getLog(BAMEventReceiverDSComponent.class);

    private RealmService realmService;
    private AuthenticationService authenticationService;
    private AgentServer agentServer;
    private RegistryService registryService;

    protected void activate(ComponentContext componentContext) throws AgentServerException {
        if (log.isDebugEnabled()) {
            log.debug("Starting the Bam Event Receiver Server component");
        }
         initialize();
        BAMEventReceiverComponentManager.getInstance().init(registryService);
        ServiceReference serviceReference = componentContext.getBundleContext().getServiceReference(AgentServer.class.getName());
        if(serviceReference != null){
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

    protected void setRegistryService(RegistryService registryService) throws
            RegistryException {
        this.registryService = registryService;
    }

    protected void unsetRegistryService(RegistryService registryService) {
        registryService = null;
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
