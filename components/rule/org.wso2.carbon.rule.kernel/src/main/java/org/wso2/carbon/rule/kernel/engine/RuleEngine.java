/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.rule.kernel.engine;

import org.wso2.carbon.rule.kernel.backend.RuleBackendRuntime;
import org.wso2.carbon.rule.kernel.backend.Session;
import org.wso2.carbon.rule.kernel.backend.RuleBackendRuntimeFactory;
import org.wso2.carbon.rule.kernel.config.RuleEngineProvider;
import org.wso2.carbon.rule.kernel.config.RuleEngineConfigService;
import org.wso2.carbon.rule.kernel.internal.ds.RuleValueHolder;
import org.wso2.carbon.rule.kernel.internal.build.RuleEngineConfigBuilder;
import org.wso2.carbon.rule.kernel.internal.config.CarbonRuleEngineConfigService;
import org.wso2.carbon.rule.common.exception.RuleRuntimeException;
import org.wso2.carbon.rule.common.exception.RuleConfigurationException;
import org.wso2.carbon.rule.common.RuleSet;

/**
 * this class is the layer in between top layers (i.e web service layer and the mediation layer) and the rule back end runtime.
 * This class is used to create the sessions which does the data binding work.
 */
public class RuleEngine {

    private RuleBackendRuntime ruleBackendRuntime;

    /**
     * Creates a Rule Engine Object. Rule engine object is kept per mediator and per service. When creating the rule
     * Engine it has to pass the Backe end runtime factory and the class loader to load the fact classes properly.
     * @param ruleSet  - rule set specified either in the web service descriptor file or rule mediator file.
     * @param factClassLoader   - class loader which other pojo classes may exists.
     * @throws RuleConfigurationException
     */
    public RuleEngine(RuleSet ruleSet,
                      ClassLoader factClassLoader) throws RuleConfigurationException {

        RuleEngineProvider ruleEngineProvider = null;
        try {
            ruleEngineProvider = RuleValueHolder.getInstance().getRuleEngineProvider();

            if (ruleEngineProvider == null){
                // if the rule service is not loaded we need to create a one
                RuleEngineConfigBuilder ruleEngineConfigBuilder =  new RuleEngineConfigBuilder();
                RuleEngineConfigService ruleEngineConfigService =
                                new CarbonRuleEngineConfigService(ruleEngineConfigBuilder.getRuleConfig());
                RuleValueHolder.getInstance().registerRuleEngineConfigService(ruleEngineConfigService);
                ruleEngineProvider = RuleValueHolder.getInstance().getRuleEngineProvider();
            }

            Class ruleBackendRuntimeFactoryClass = Class.forName(ruleEngineProvider.getClassName());
            RuleBackendRuntimeFactory ruleBackendRuntimeFactory =
                    (RuleBackendRuntimeFactory) ruleBackendRuntimeFactoryClass.newInstance();
            this.ruleBackendRuntime =
                    ruleBackendRuntimeFactory.getRuleBackendRuntime(ruleEngineProvider.getProperties(),factClassLoader);
            this.ruleBackendRuntime.addRuleSet(ruleSet);

        } catch (ClassNotFoundException e) {
            throw new RuleConfigurationException("Class " + ruleEngineProvider.getClassName() + " can not be found ");
        } catch (IllegalAccessException e) {
            throw new RuleConfigurationException("Can not instantiate the " + ruleEngineProvider.getClassName() + " class");
        } catch (InstantiationException e) {
            throw new RuleConfigurationException("Can not instantiate the " + ruleEngineProvider.getClassName() + " class");
        }

    }

    /**
     * creates a session to be used with a pirticular service invocation. 
     * @param type
     * @return
     * @throws RuleRuntimeException
     */
    public RuleSession createSession(int type) throws RuleRuntimeException {

        Session session = this.ruleBackendRuntime.createSession(type);
        return new RuleSession(session);
    }
}
