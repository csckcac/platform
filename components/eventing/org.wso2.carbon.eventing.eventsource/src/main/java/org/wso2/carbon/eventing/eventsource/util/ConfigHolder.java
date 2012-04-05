package org.wso2.carbon.eventing.eventsource.util;
/*
*  Copyright (c) 2005-2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
*
*/

import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.core.SynapseEnvironment;
import org.wso2.carbon.eventing.eventsource.EventSourceAdminException;
import org.wso2.carbon.mediation.initializer.services.SynapseEnvironmentService;
import org.wso2.carbon.registry.core.session.UserRegistry;

import java.util.HashMap;
import java.util.Map;

/**
 * Singleton class to provide the synapseconfig/synapseenviornment from the synapse public services.
 */
public class ConfigHolder {

    private static ConfigHolder instance;
    private static final Log log = LogFactory.getLog(ConfigHolder.class);

    private SynapseConfiguration synapseConfiguration;
    private AxisConfiguration axisConfiguration;
    private UserRegistry registry;

    private Map<Integer, SynapseEnvironmentService> synapseEnvironmentServices =
            new HashMap<Integer, SynapseEnvironmentService>();

    private ConfigHolder() {
        // private constructor to make the class singleton 
    }

    public static ConfigHolder getInstance() {
        if (instance == null) {
            instance = new ConfigHolder();
        }
        return instance;
    }

    public SynapseConfiguration getSynapseConfiguration() throws EventSourceAdminException {
        assertNull("SynapseConfiguration", synapseConfiguration);
        return synapseConfiguration;
    }

    public void setSynapseConfiguration(SynapseConfiguration synapseConfiguration) {
        this.synapseConfiguration = synapseConfiguration;
    }

    public AxisConfiguration getAxisConfiguration() throws EventSourceAdminException {
        assertNull("AxisConfiguration", axisConfiguration);
        return axisConfiguration;
    }

    public void setAxisConfiguration(AxisConfiguration axisConfiguration) {
        this.axisConfiguration = axisConfiguration;
    }

    public UserRegistry getRegistry() throws EventSourceAdminException {
        assertNull("Registry", registry);
        return registry;
    }

    public void setRegistry(UserRegistry registry) {
        this.registry = registry;
    }

    private void assertNull(String name, Object object) throws EventSourceAdminException {
        if (object == null) {
            String message = name + " reference in the proxy admin config holder is null";
            log.error(message);
            throw new EventSourceAdminException(message);
        }
    }

    public SynapseEnvironmentService getSynapseEnvironmentService(int id) {
        return synapseEnvironmentServices.get(id);
    }

    public void addSynapseEnvironmentService(int id,
                                             SynapseEnvironmentService synapseEnvironmentService) {
        synapseEnvironmentServices.put(id, synapseEnvironmentService);
    }

    public void removeSynapseEnvironmentService(int id) {
        synapseEnvironmentServices.remove(id);
    }

    public Map<Integer, SynapseEnvironmentService> getSynapseEnvironmentServices() {
        return synapseEnvironmentServices;
    }
}
