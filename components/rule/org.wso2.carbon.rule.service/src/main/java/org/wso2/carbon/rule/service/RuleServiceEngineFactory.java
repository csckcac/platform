/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.rule.service;

import org.apache.axis2.description.AxisService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.multitenancy.SuperTenantCarbonContext;
import org.wso2.carbon.rule.core.LoggedRuntimeException;
import org.wso2.carbon.rule.server.RuleEngine;
import org.wso2.carbon.rulecep.commons.descriptions.rule.RuleSetDescription;
import org.wso2.carbon.rulecep.commons.descriptions.rule.service.RuleServiceExtensionDescription;
import org.wso2.carbon.rulecep.commons.descriptions.service.ServiceDescription;
import org.wso2.carbon.rulecep.service.ResourceLoader;
import org.wso2.carbon.rulecep.service.ServiceEngine;
import org.wso2.carbon.rulecep.service.ServiceEngineFactory;

import java.io.InputStream;

/**
 * Create rule service engines
 */
public class RuleServiceEngineFactory implements ServiceEngineFactory {

    private static Log log = LogFactory.getLog(RuleServiceEngineFactory.class);

    public ServiceEngine createServiceEngine(ServiceDescription serviceDescription,
                                             AxisService axisService,
                                             ResourceLoader resourceLoader) {
        //TODO
        RuleEngine ruleEngine =
                RuleServiceManger.getInstance().getRuleServerManagerService().createRuleEngine(
                        axisService.getClassLoader());
        RuleServiceExtensionDescription extensionDescription =
                (RuleServiceExtensionDescription) serviceDescription.getServiceExtensionDescription();
        RuleSetDescription ruleSetDescription = extensionDescription.getRuleSetDescription();
        Object ruleSource = ruleSetDescription.getRuleSource();

        int tenantId =  SuperTenantCarbonContext.getCurrentContext(axisService).getTenantId();
        if (ruleSource == null) {
            Object value = loadRuleScript(axisService.getClassLoader(), ruleSetDescription,
                    resourceLoader,tenantId);
            if (value == null) {
                throw new LoggedRuntimeException("Cannot load the rule script from" +
                        " the " + ruleSetDescription, log);
            }
            ruleSetDescription.setRuleSource(value);
        }

        String uri = ruleEngine.addRuleSet(ruleSetDescription);
        return new RuleServiceEngine(axisService, ruleEngine, uri);
    }


    /**
     * Finds the rule script using the provided key. First look up at the service class loader ,
     * then check in the registry
     *
     * @param cl                 Service class loader
     * @param ruleSetDescription information about rule set
     * @param resourceLoader     Loads resource from registry and classpath
     * @return <code>InputStream</code> if there is a resource at the given key , otherwise null
     */
    private static InputStream loadRuleScript(ClassLoader cl,
                                              RuleSetDescription ruleSetDescription,
                                              ResourceLoader resourceLoader, int tenantID ) {

        String key = ruleSetDescription.getKey();
        String path = ruleSetDescription.getPath();
        if (key != null && !"".equals(key)) {
            return resourceLoader.loadResourceFromRegistry(key,tenantID);
        } else if (path != null && !"".equals(path)) {
            return resourceLoader.loadResourceFromLocal(path, cl);
        }
        return null;
    }

}
