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
package org.wso2.carbon.rule.engine.jsr94;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.rule.core.*;
import org.wso2.carbon.rulecep.commons.descriptions.PropertyDescription;
import org.wso2.carbon.rulecep.commons.utils.ClassHelper;

import javax.rules.ConfigurationException;
import javax.rules.RuleRuntime;
import javax.rules.RuleServiceProvider;
import javax.rules.RuleServiceProviderManager;
import javax.rules.admin.LocalRuleExecutionSetProvider;
import javax.rules.admin.RuleAdministrator;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

/**
 * The RuleBackendRuntimeFactory based on JSR 94 API. This class creates an JSR 94 API compatible rule engine
 * and exposes as a RuleBackendRuntime. The default rule engine is the Drools JSR 94 RuleServiceProvider
 */
public class JSR94BackendRuntimeFactory implements RuleBackendRuntimeFactory {

    private static Log log = LogFactory.getLog(JSR94BackendRuntimeFactory.class);

    /**
     * @param properties  information required to find the RuleServiceProvider implementation and
     *                    provider URI as name-value pairs. A property with name class is used
     *                    to find RuleServiceProvider implementation and a property with name
     *                    uri is used to find the provider URI.
     * @param classLoader class loader to be used by the underlying rule service provider to load
     *                    facts and other required classes
     * @return a valid <code>RuleBackendRuntime</code> instance
     */
    public RuleBackendRuntime createRuleBackendRuntime(Map<String, PropertyDescription> properties,
                                                       ClassLoader classLoader) {

        String providerClassName = null;
        PropertyDescription provider = properties.get("class");
        if (provider != null) {
            providerClassName = provider.getValue();
        }
        if (providerClassName == null || "".equals(providerClassName)) {
            if (log.isDebugEnabled()) {
                log.debug("Provided RuleServiceProviderClass is null or empty. Using default : " +
                        RuleConstants.DROOLS_RULE_SERVICE_PROVIDER);
            }
            providerClassName = RuleConstants.DROOLS_RULE_SERVICE_PROVIDER;
        }

        Class providerClass = null;
        try {
            if (log.isDebugEnabled()) {
                log.debug("Trying to initialize the RuleServiceProvider " +
                        "with class name :" + providerClassName);
            }
            // RuleServiceProviderImpl will automatically registered
            // via a static initialization block
            providerClass = Class.forName(providerClassName);
            if (log.isDebugEnabled()) {
                log.debug("RuleServiceProvider has been initialized." +
                        " provider class : " + providerClassName);
            }

        } catch (ClassNotFoundException e) {
            throw new LoggedRuntimeException("Error when loading RuleServiceProvider from class " +
                    "with the name " + providerClassName, e, log);
        }

        String providerUri = null;
        PropertyDescription uri = properties.get("uri");
        if (uri != null) {
            providerUri = uri.getValue();
        }
        if (providerUri == null || "".equals(providerUri)) {
            if (log.isDebugEnabled()) {
                log.debug("Provided RuleServiceProviderURI is null or empty. Using default : " +
                        RuleConstants.DROOLS_RULE_SERVICE_PROVIDER_URI);
            }
            providerUri = RuleConstants.DROOLS_RULE_SERVICE_PROVIDER_URI;
        }

        try {
            if (log.isDebugEnabled()) {
                log.debug("Getting RuleServiceProvider using provider Url : " + providerUri);
            }

            RuleServiceProviderManager.registerRuleServiceProvider(
                    providerUri, providerClass, classLoader);
            // Get the rule service provider from the provider manager.
            RuleServiceProvider ruleServiceProvider =
                    RuleServiceProviderManager.getRuleServiceProvider(providerUri);


            if (ruleServiceProvider == null) {
                throw new LoggedRuntimeException("There is no RuleServiceProvider" +
                        " registered for Uri :" + providerUri, log);
            }

            RuleAdministrator ruleAdministrator = createRuleAdministrator(ruleServiceProvider);
            LocalRuleExecutionSetProvider localProvider =
                    createLocalRuleExecutionSetProvider(ruleAdministrator);
            RuleRuntime ruleRuntime = createRuleRuntime(ruleServiceProvider);
            JSR94BackendRuntime jsr94BackendRuntime =
                    new JSR94BackendRuntime(ruleAdministrator, ruleRuntime, localProvider,
                            classLoader);
            PropertyDescription propertyLoader =
                    properties.get(RuleConstants.PROP_DEFAULT_PROPERTIES_PROVIDER);
            if (propertyLoader != null) {
                DefaultPropertiesProvider defaultPropertiesProvider =
                        (DefaultPropertiesProvider) ClassHelper.createInstance(
                                propertyLoader.getValue().trim());
                jsr94BackendRuntime.setDefaultPropertiesProvider(defaultPropertiesProvider);
            }
            return jsr94BackendRuntime;
        } catch (ConfigurationException e) {
            throw new LoggedRuntimeException("Error was occurred when getting RuleServiceProvider" +
                    " which has been registered to the Url " + providerUri, e, log);
        }
    }

    private RuleAdministrator createRuleAdministrator(RuleServiceProvider ruleServiceProvider) {

        try {
            if (log.isDebugEnabled()) {
                log.debug("Getting Rule Administrator from Rule Service Provider");
            }
            // Gets the RuleAdministration
            RuleAdministrator ruleAdministrator = ruleServiceProvider.getRuleAdministrator();
            if (ruleAdministrator == null) {
                throw new LoggedRuntimeException("The loaded Rule Administrator is null", log);
            }
            return ruleAdministrator;
        } catch (ConfigurationException e) {
            throw new LoggedRuntimeException("Error was occurred when creating the " +
                    "Rule Administrator from the RuleServiceProvider", e, log);
        }
    }

    private RuleRuntime createRuleRuntime(RuleServiceProvider ruleServiceProvider) {

        try {
            // get the run time which will be the access point for runtime execution
            // of RuleExecutionSets
            RuleRuntime ruleRuntime = ruleServiceProvider.getRuleRuntime();
            if (ruleRuntime == null) {
                throw new LoggedRuntimeException("The created rule runtime is null", log);
            }
            return ruleRuntime;

        } catch (ConfigurationException e) {
            throw new LoggedRuntimeException("Error was occurred when getting RuleRuntime", e, log);
        }
    }

    private LocalRuleExecutionSetProvider createLocalRuleExecutionSetProvider(
            RuleAdministrator ruleAdministrator) {

        // get the run time which will be the access point for runtime execution
        try {
            if (log.isDebugEnabled()) {
                log.debug("Getting LocalRuleExecutionSetProvider ");
            }

            LocalRuleExecutionSetProvider localRuleExecutionSetProvider =
                    ruleAdministrator.getLocalRuleExecutionSetProvider(new HashMap());
            if (localRuleExecutionSetProvider == null) {
                throw new LoggedRuntimeException("The loaded LocalRuleExecutionSetProvider is null",
                        log);
            }
            return localRuleExecutionSetProvider;

        } catch (RemoteException e) {
            throw new LoggedRuntimeException("Error was occurred when getting " +
                    "the LocalRuleExecutionSetProvider ", e, log);
        }
    }
}
