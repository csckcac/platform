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

package org.wso2.carbon.eventbridge.streamdefn.cassandra.internal;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.cassandra.dataaccess.DataAccessService;
import org.wso2.carbon.eventbridge.core.EventBridgeSubscriberService;
import org.wso2.carbon.eventbridge.streamdefn.cassandra.datastore.CassandraConnector;
import org.wso2.carbon.eventbridge.streamdefn.cassandra.internal.util.Utils;
import org.wso2.carbon.eventbridge.streamdefn.cassandra.subscriber.BAMEventSubscriber;
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
 * @scr.reference name="dataaccess.service" interface="org.wso2.carbon.cassandra.dataaccess.DataAccessService"
 * cardinality="1..1" policy="dynamic" bind="setDataAccessService" unbind="unsetDataAccessService"
 * @scr.reference name="eventbridge.core"
 * interface="org.wso2.carbon.eventbridge.core.EventBridgeSubscriberService"
 * cardinality="1..1" policy="dynamic" bind="setEventBridgeSubscriberService"  unbind="unsetEventBridgeSubscriberService"
 *
 */
public class ReceiverServiceComponent {
    private static Log log = LogFactory.getLog(ReceiverServiceComponent.class);

    protected void activate(ComponentContext componentContext) {
        Utils.setCassandraConnector(new CassandraConnector());
    }

    protected void deactivate(ComponentContext componentContext) {
        if (log.isDebugEnabled()) {
            log.debug("Stopping the Bam Event Receiver Server component");
        }
    }

    protected void setRealmService(RealmService realmService) {
        Utils.setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {
        Utils.setRealmService(null);
    }

    protected void setAuthenticationService(AuthenticationService authenticationService) {
        Utils.setAuthenticationService(authenticationService);
    }

    protected void unsetAuthenticationService(AuthenticationService authenticationService) {
        Utils.setAuthenticationService(null);
    }

    protected void setRegistryService(RegistryService registryService) throws
            RegistryException {
        Utils.setRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {
        Utils.setRegistryService(null);
    }

    protected void setDataAccessService(DataAccessService dataAccessService) {
        Utils.setDataAccessService(dataAccessService);
    }

    protected void unsetDataAccessService(DataAccessService dataAccessService) {
        Utils.setDataAccessService(null);
    }

    protected void setEventBridgeSubscriberService(EventBridgeSubscriberService eventBridgeSubscriberService) {
        eventBridgeSubscriberService.subscribe( new BAMEventSubscriber());
    }

    protected void unsetEventBridgeSubscriberService(EventBridgeSubscriberService eventBridgeSubscriberService) {


    }



}
