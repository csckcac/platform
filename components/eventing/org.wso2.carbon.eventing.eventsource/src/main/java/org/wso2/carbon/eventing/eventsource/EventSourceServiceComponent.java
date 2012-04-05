/**
 *  Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.eventing.eventsource;

import org.apache.axis2.deployment.DeploymentEngine;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.config.xml.MultiXMLConfigurationBuilder;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.deployers.SynapseArtifactDeploymentStore;
import org.apache.synapse.eventing.SynapseEventSource;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.eventing.eventsource.util.ConfigHolder;
import org.wso2.carbon.mediation.initializer.ServiceBusConstants;
import org.wso2.carbon.mediation.initializer.ServiceBusUtils;
import org.wso2.carbon.mediation.initializer.services.SynapseConfigurationService;
import org.wso2.carbon.mediation.initializer.services.SynapseEnvironmentService;
import org.wso2.carbon.mediation.initializer.services.SynapseRegistrationsService;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.File;
import java.util.Map;
import java.util.Set;


/**
 * @scr.component name="org.wso2.carbon.eventing.eventsource" immediate="true"
 * @scr.reference name="configuration.context.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService" cardinality="1..1"
 * policy="dynamic" bind="setConfigurationContextService" unbind="unsetConfigurationContextService"
 * @scr.reference name="synapse.config.service"
 * interface="org.wso2.carbon.mediation.initializer.services.SynapseConfigurationService"
 * cardinality="1..1" policy="dynamic" bind="setSynapseConfigurationService"
 * unbind="unsetSynapseConfigurationService"
 * @scr.reference name="synapse.env.service"
 * interface="org.wso2.carbon.mediation.initializer.services.SynapseEnvironmentService"
 * cardinality="1..n" policy="dynamic" bind="setSynapseEnvironmentService"
 * unbind="unsetSynapseEnvironmentService"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic"
 * bind="setRegistryService" unbind="unsetRegistryService"
 * @scr.reference name="synapse.registrations.service"
 * interface="org.wso2.carbon.mediation.initializer.services.SynapseRegistrationsService"
 * cardinality="1..n" policy="dynamic" bind="setSynapseRegistrationsService"
 * unbind="unsetSynapseRegistrationsService"
 */
@SuppressWarnings({"UnusedDeclaration", "JavaDoc"})
public class EventSourceServiceComponent {

    private static final Log log = LogFactory.getLog(EventSourceServiceComponent.class);

    private boolean activated = false;

    protected void activate(ComponentContext context) {
        try {
            SynapseEnvironmentService synEnvService =
                    ConfigHolder.getInstance().getSynapseEnvironmentService(
                            MultitenantConstants.SUPER_TENANT_ID);

            registerDeployer(ConfigHolder.getInstance().getAxisConfiguration(),
                    synEnvService.getSynapseEnvironment());
            if (log.isDebugEnabled()) {
                log.debug("EventSource Admin bundle is activated for Super tenant");
            }
            activated = true;
        } catch (EventSourceAdminException e) {
            log.error("Error while initializing the Event sources component.", e);
        }
    }

    /**
     * Registers the eventsource.
     *
     * @param axisConfig         AxisConfiguration to which this deployer belongs
     * @param synapseEnvironment SynapseEnvironment to which this deployer belongs
     */
    private void registerDeployer(AxisConfiguration axisConfig, SynapseEnvironment synapseEnvironment)
            throws EventSourceAdminException {
        SynapseConfiguration synCfg = synapseEnvironment.getSynapseConfiguration();
        DeploymentEngine deploymentEngine = (DeploymentEngine) axisConfig.getConfigurator();
        SynapseArtifactDeploymentStore deploymentStore = synCfg.getArtifactDeploymentStore();

        String synapseConfigPath = ServiceBusUtils.getSynapseConfigAbsPath(
                synapseEnvironment.getServerContextInformation());
        String eventSourceDirPath = synapseConfigPath
                + File.separator + MultiXMLConfigurationBuilder.EVENTS_DIR;

        for (SynapseEventSource ses : synCfg.getEventSources()) {
            if (ses.getFileName() != null) {
                deploymentStore.addRestoredArtifact(
                        eventSourceDirPath + File.separator + ses.getFileName());
            }
        }
        deploymentEngine.addDeployer(new EventSourceDeployer(),
                eventSourceDirPath, ServiceBusConstants.ARTIFACT_EXTENSION);
    }

    protected void deactivate(ComponentContext context) {
        try {
            Set<Map.Entry<Integer, SynapseEnvironmentService>> entrySet =
                    ConfigHolder.getInstance().getSynapseEnvironmentServices().entrySet();
            for (Map.Entry<Integer, SynapseEnvironmentService> entry : entrySet) {
                unregisterDeployer(
                        entry.getValue().getConfigurationContext().getAxisConfiguration(),
                        entry.getValue().getSynapseEnvironment());
            }
        } catch (EventSourceAdminException e) {
            log.warn("Couldn't remove the EventSourceDeployer");
        }
    }

    /**
     * Un-registers the eventsource.
     *
     * @param axisConfig         AxisConfiguration to which this deployer belongs
     * @param synapseEnvironment SynapseEnvironment to which this deployer belongs
     */
    private void unregisterDeployer(AxisConfiguration axisConfig, SynapseEnvironment synapseEnvironment)
            throws EventSourceAdminException {
        if (axisConfig != null) {
            DeploymentEngine deploymentEngine = (DeploymentEngine) axisConfig.getConfigurator();
            String synapseConfigPath = ServiceBusUtils.getSynapseConfigAbsPath(
                    synapseEnvironment.getServerContextInformation());
            String eventSourceDirPath = synapseConfigPath
                    + File.separator + MultiXMLConfigurationBuilder.EVENTS_DIR;
            deploymentEngine.removeDeployer(
                    eventSourceDirPath, ServiceBusConstants.ARTIFACT_EXTENSION);
        }
    }

    protected void setSynapseRegistrationsService(
            SynapseRegistrationsService synapseRegistrationsService) {

    }

    protected void unsetSynapseRegistrationsService(
            SynapseRegistrationsService synapseRegistrationsService) {
        int tenantId = synapseRegistrationsService.getTenantId();
        if (ConfigHolder.getInstance().getSynapseEnvironmentServices().containsKey(tenantId)) {
            SynapseEnvironment env = ConfigHolder.getInstance().
                    getSynapseEnvironmentService(tenantId).getSynapseEnvironment();

            ConfigHolder.getInstance().removeSynapseEnvironmentService(
                    synapseRegistrationsService.getTenantId());

            AxisConfiguration axisConfig = synapseRegistrationsService.getConfigurationContext().
                    getAxisConfiguration();
            if (axisConfig != null) {
                try {
                    unregisterDeployer(axisConfig, env);
                } catch (EventSourceAdminException e) {
                    log.warn("Couldn't remove the EventSourceDeployer");
                }
            }
        }
    }

    protected void setConfigurationContextService(ConfigurationContextService cfgCtxService) {
        ConfigHolder.getInstance().setAxisConfiguration(
                cfgCtxService.getServerConfigContext().getAxisConfiguration());
    }

    protected void unsetConfigurationContextService(ConfigurationContextService cfgCtxService) {
        ConfigHolder.getInstance().setAxisConfiguration(null);
    }

    protected void setSynapseConfigurationService(
            SynapseConfigurationService synapseConfigurationService) {

        ConfigHolder.getInstance().setSynapseConfiguration(
                synapseConfigurationService.getSynapseConfiguration());
    }

    protected void unsetSynapseConfigurationService(
            SynapseConfigurationService synapseConfigurationService) {

        ConfigHolder.getInstance().setSynapseConfiguration(null);
    }

    /**
     * Here we receive an event about the creation of a SynapseEnvironment. If this is
     * SuperTenant we have to wait until all the other constraints are met and actual
     * initialization is done in the activate method. Otherwise we have to do the activation here.
     *
     * @param synapseEnvironmentService SynapseEnvironmentService which contains information
     *                                  about the new Synapse Instance
     */
    protected void setSynapseEnvironmentService(
            SynapseEnvironmentService synapseEnvironmentService) {
        boolean alreadyCreated = ConfigHolder.getInstance().getSynapseEnvironmentServices().
                containsKey(synapseEnvironmentService.getTenantId());

        ConfigHolder.getInstance().addSynapseEnvironmentService(
                synapseEnvironmentService.getTenantId(),
                synapseEnvironmentService);
        if (activated) {
            if (!alreadyCreated) {
                try {
                    registerDeployer(synapseEnvironmentService.getConfigurationContext().getAxisConfiguration(),
                            synapseEnvironmentService.getSynapseEnvironment());
                } catch (EventSourceAdminException e) {
                    log.error("Error while initializing the Event sources component.", e);
                }
            }
        }
    }

    /**
     * Here we receive an event about Destroying a SynapseEnvironment. This can be the super tenant
     * destruction or a tenant destruction.
     *
     * @param synapseEnvironmentService synapseEnvironment
     */
    protected void unsetSynapseEnvironmentService(
            SynapseEnvironmentService synapseEnvironmentService) {
        ConfigHolder.getInstance().removeSynapseEnvironmentService(
                synapseEnvironmentService.getTenantId());
    }

    protected void setRegistryService(RegistryService regService) {
        if (log.isDebugEnabled()) {
            log.debug("RegistryService bound to the ESB initialization process");
        }
        try {
            ConfigHolder.getInstance().setRegistry(regService.getConfigSystemRegistry());
        } catch (RegistryException e) {
            log.error("Couldn't retrieve the registry from the registry service");
        }
    }

    protected void unsetRegistryService(RegistryService regService) {
        if (log.isDebugEnabled()) {
            log.debug("RegistryService unbound from the ESB environment");
        }
        ConfigHolder.getInstance().setRegistry(null);
    }
}
