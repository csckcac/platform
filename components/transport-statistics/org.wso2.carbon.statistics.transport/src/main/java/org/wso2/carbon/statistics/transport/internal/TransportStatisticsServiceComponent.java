/*
 * Copyright 2005-2007 WSO2, Inc. (http://wso2.com)
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

package org.wso2.carbon.statistics.transport.internal;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.statistics.transport.services.util.ConfigHolder;
import org.wso2.carbon.statistics.transport.services.TransportStatisticsAdmin;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.utils.MBeanRegistrar;

/**
 * @scr.component name="jmx.service.component" immediate="true"
 * @scr.reference name="config.context.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService"
 * cardinality="1..1" policy="dynamic"
 * bind="setConfigurationContextService"
 * unbind="unsetConfigurationContextService"
 * @scr.reference name="server.configuration"
 * interface="org.wso2.carbon.base.api.ServerConfigurationService"
 * cardinality="1..1" policy="dynamic"
 * bind="setServerConfiguration"
 * unbind="unsetServerConfiguration"
 */
public class TransportStatisticsServiceComponent {

    private static final Log log = LogFactory.getLog(TransportStatisticsServiceComponent.class);

    private ConfigurationContext configContext;

    private ServerConfigurationService serverConfiguration;

    private ServiceRegistration transportStatAdminServiceRegistration;

    protected void activate(ComponentContext ctxt) {
        try {
            // Registering TransportStatisticsAdmin as an OSGi service.
            transportStatAdminServiceRegistration = ctxt.getBundleContext().registerService(
                    TransportStatisticsAdmin.class.getName(),new TransportStatisticsAdmin(), null);
            // TODO : uncomment the following line after adding the uipermissions
            //this.addUIPermission();

        } catch (Throwable e) {
            log.error("Failed to activate Transport Statistics bundle", e);
        }
    }

    protected void deactivate(ComponentContext ctxt) {
        // unregistering TransportStatisticsAdmin service from the OSGi Service Register.
        transportStatAdminServiceRegistration.unregister();
        log.debug("Transport Statistics bundle is deactivated");
    }

    protected void setConfigurationContextService(ConfigurationContextService contextService) {
        this.configContext = contextService.getServerConfigContext();
        ConfigHolder.getInstance().setConfigurationContext(configContext);
    }

    protected void unsetConfigurationContextService(ConfigurationContextService contextService) {
        AxisConfiguration axisConf = configContext.getAxisConfiguration();
        this.configContext = null;
    }

    private void registerMBeans(ServerConfigurationService serverConfigurationService) {
        if (serverConfiguration.getFirstProperty("Ports.Transport") != null) {
            MBeanRegistrar.registerMBean(new TransportStatisticsAdmin());
        }
    }

    protected void setServerConfiguration(ServerConfigurationService serverConfigurationService) {
        this.serverConfiguration = serverConfigurationService;
    }

    protected void unsetServerConfiguration(ServerConfigurationService serverConfigurationService) {
        this.serverConfiguration = null;
    }

    public ConfigurationContext getConfigurationConetxt(){
        return configContext;
    }
}
