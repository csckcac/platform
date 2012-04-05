/*
 *  Copyright (c) 2005-2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.messagebox.internal.ds;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.core.multitenancy.SuperTenantCarbonContext;
import org.wso2.carbon.messagebox.MessageBoxConfigurationException;
import org.wso2.carbon.messagebox.MessageBoxService;
import org.wso2.carbon.messagebox.internal.builder.Axis2ConfigurationContextObserverImpl;
import org.wso2.carbon.messagebox.internal.builder.MessageBoxServiceBuilder;
import org.wso2.carbon.messagebox.internal.qpid.QueueConnectionManager;
import org.wso2.carbon.messagebox.internal.qpid.QueueConnectionManagerException;
import org.wso2.carbon.qpid.service.QpidService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.Axis2ConfigurationContextObserver;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

/**
 * @scr.component name="org.wso2.carbon.messagebox" immediate="true"
 * @scr.reference name="qpid.service"
 * interface="org.wso2.carbon.qpid.service.QpidService" cardinality="1..1"
 * policy="dynamic" bind="setQpidService" unbind="unsetQpidService"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService" cardinality="1..1"
 * policy="dynamic" bind="setRegistryService" unbind="unsetRegistryService"
 * @scr.reference name="realm.service" interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1" policy="dynamic" bind="setRealmService"  unbind="unsetRealmService"
 * @scr.reference name="configurationcontext.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService" cardinality="1..1"
 * policy="dynamic" bind="setConfigurationContextService" unbind="unsetConfigurationContextService"
 */
public class MessageBoxServiceComponent {

    private static Log log = LogFactory.getLog(MessageBoxServiceComponent.class);

    protected void activate(ComponentContext context) {
        try {
            SuperTenantCarbonContext.startTenantFlow();
            SuperTenantCarbonContext.getCurrentContext().setTenantId(MultitenantConstants.SUPER_TENANT_ID);
            MessageBoxService messageBoxService = MessageBoxServiceBuilder.createMessageBoxService();
            context.getBundleContext().registerService(
                    MessageBoxService.class.getName(), messageBoxService, null);
            context.getBundleContext().registerService(Axis2ConfigurationContextObserver.class.getName(),
                                                       new Axis2ConfigurationContextObserverImpl(),
                                                       null);
        } catch (MessageBoxConfigurationException e) {
            log.error("MessageBoxService bundle activation is failed.", e);
        }
    }

    protected void deactivate(ComponentContext context) {
        try {
            QueueConnectionManager.getInstance().closeAllConnections();
        } catch (QueueConnectionManagerException e) {
            log.warn("Error while closing QueueConnectionManager connections : " +
                     e.getLocalizedMessage());
        }
    }

    protected void setQpidService(QpidService qpidService) {
        MessageBoxServiceValueHolder.getInstance().registerQpidService(qpidService);
    }

    protected void unsetQpidService(QpidService qpidService) {

    }

    protected void setRealmService(RealmService realmService) {
        MessageBoxServiceValueHolder.getInstance().registerRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {

    }

    protected void setRegistryService(RegistryService registryService) {
        MessageBoxServiceValueHolder.getInstance().registerRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {

    }

    protected void setConfigurationContextService(
            ConfigurationContextService configurationContextService) {
        MessageBoxServiceValueHolder.getInstance().registerConfigurationContextService(configurationContextService);
    }

    protected void unsetConfigurationContextService(
            ConfigurationContextService configurationContextService) {

    }
}
