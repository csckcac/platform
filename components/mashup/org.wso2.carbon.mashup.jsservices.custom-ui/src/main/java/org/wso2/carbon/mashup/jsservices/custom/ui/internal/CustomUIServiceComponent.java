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

package org.wso2.carbon.mashup.jsservices.custom.ui.internal;

import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.core.util.MIMEType2FileExtensionMap;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.osgi.service.component.ComponentContext;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;
import org.apache.axis2.context.ConfigurationContext;

/**
 * @scr.component name="mashup.jsservices.customui.dscomponent" immediate="true"
 * @scr.reference name="MIMEType2FileExtensionMap.service" interface="org.wso2.carbon.core.util.MIMEType2FileExtensionMap"
 * cardinality="1..1" policy="dynamic"  bind="setMIMEType2FileExtensionMap" unbind="unsetMIMEType2FileExtensionMap"
 * @scr.reference name="ConfigurationContextService.service" interface="org.wso2.carbon.utils.ConfigurationContextService"
 * cardinality="1..1" policy="dynamic"  bind="setConfigurationContextService" unbind="unsetConfigurationContextService"
 * @scr.reference name="ServerConfiguration.service" interface="org.wso2.carbon.base.ServerConfiguration"
 * cardinality="1..1" policy="dynamic"  bind="setServerConfiguration" unbind="unsetServerConfiguration"
 */
public class CustomUIServiceComponent {

    private static MIMEType2FileExtensionMap extensionMap;
    private static ConfigurationContext configurationContext;
    private static String carbonContextRoot;

    private static Log log = LogFactory.getLog(CustomUIServiceComponent.class);

    protected void activate(ComponentContext ctxt) {
        log.debug("JSServices Custom UI bundle is activated ");
    }

    protected void deactivate(ComponentContext ctxt) {
        log.debug("JSServices Custom UI bundle is deactivated ");
    }

    protected void setMIMEType2FileExtensionMap(MIMEType2FileExtensionMap extensionMap) {
        CustomUIServiceComponent.extensionMap = extensionMap;
    }

    protected void unsetMIMEType2FileExtensionMap(MIMEType2FileExtensionMap extensionMap) {
        CustomUIServiceComponent.extensionMap = null;
    }

    protected void setConfigurationContextService(ConfigurationContextService configurationContextService) {
        CustomUIServiceComponent.configurationContext = configurationContextService.getServerConfigContext();
    }

    protected void unsetConfigurationContextService(ConfigurationContextService configurationContextService) {
        CustomUIServiceComponent.configurationContext = null;
    }

    protected void setServerConfiguration(ServerConfiguration serverConfiguration) {
        CustomUIServiceComponent.carbonContextRoot = serverConfiguration.getFirstProperty("WebContextRoot");
    }

    protected void unsetServerConfiguration(ServerConfiguration serverConfiguration) {
        CustomUIServiceComponent.carbonContextRoot = null;
    }

    public static MIMEType2FileExtensionMap getExtensionMap() {
        return extensionMap;
    }

    public static ConfigurationContext getConfigurationContext() {
        return configurationContext;
    }

    public static String getCarbonContextRoot() {
        return carbonContextRoot;
    }
}
