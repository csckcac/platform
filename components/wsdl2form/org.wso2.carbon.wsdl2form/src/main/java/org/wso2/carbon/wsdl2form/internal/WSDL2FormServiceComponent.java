/**
 *  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.wsdl2form.internal;

import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * @scr.component name="wsdl2form.dscomponent" immediate="true"
 * @scr.reference name="ServerConfiguration.service" interface="org.wso2.carbon.base.api.ServerConfigurationService"
 * cardinality="0..1" policy="dynamic"  bind="setServerConfiguration" unbind="unsetServerConfiguration"
 * @scr.reference name="carbon.core.configurationContextService"
 * interface="org.wso2.carbon.utils.ConfigurationContextService"
 * cardinality="1..1" policy="dynamic" bind="setConfigurationContextService" unbind="unsetConfigurationContextService"
 */
public class WSDL2FormServiceComponent {

    private static ServerConfigurationService serverConfigurationService = null;
    private static ConfigurationContextService configurationCtxService = null;

    protected void setServerConfiguration(ServerConfigurationService serverConfigurationService) {
        WSDL2FormServiceComponent.serverConfigurationService = serverConfigurationService;
    }

    protected void unsetServerConfiguration(ServerConfigurationService serverConfigurationService) {
        WSDL2FormServiceComponent.serverConfigurationService = null;
    }

    public static ServerConfigurationService getServerConfigurationService() {
        return WSDL2FormServiceComponent.serverConfigurationService;
    }

    protected void setConfigurationContextService(
            ConfigurationContextService configurationContextService) {
        WSDL2FormServiceComponent.configurationCtxService = configurationContextService;
    }

    protected void unsetConfigurationContextService(
            ConfigurationContextService configurationContextService) {
        WSDL2FormServiceComponent.configurationCtxService = null;
    }

    public static ConfigurationContextService getConfigurationContextService() {
        return WSDL2FormServiceComponent.configurationCtxService;
    }

}
