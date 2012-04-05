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
import org.wso2.carbon.rulecep.adapters.utils.PropertyDescriptionEvaluator;
import org.wso2.carbon.rulecep.commons.descriptions.rule.RuleSetDescription;
import org.wso2.carbon.rulecep.commons.descriptions.rule.SessionDescription;
import org.wso2.carbon.rulecep.commons.utils.ObjectToStreamConverter;

import javax.rules.*;
import javax.rules.admin.*;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.Map;

/**
 * The RuleBackendRuntime implementation for the JSR 94 API. The instances of this class must
 * only be created through the JSR94BackendRuntimeFactory.
 */
public class JSR94BackendRuntime implements RuleBackendRuntime {

    private static Log log = LogFactory.getLog(JSR94BackendRuntime.class);

    /**
     * To register/unregister rule execution sets and to access rule service providers
     */
    private RuleAdministrator ruleAdministrator;

    /**
     * The access point for runtime execution of RuleExecutionSets *
     */
    private RuleRuntime ruleRuntime;

    /**
     * Creates rule execution sets from local resources
     */
    private LocalRuleExecutionSetProvider localRuleExecutionSetProvider;

    /**
     * Provides any default properties required by the underling rule engine
     */
    private DefaultPropertiesProvider defaultPropertiesProvider;

    /**
     * To be used as a class loader by the rule engine
     */
    private ClassLoader classLoader;

    public JSR94BackendRuntime(RuleAdministrator ruleAdministrator,
                               RuleRuntime ruleRuntime,
                               LocalRuleExecutionSetProvider localRuleExecutionSetProvider,
                               ClassLoader classLoader) {
        this.ruleAdministrator = ruleAdministrator;
        this.ruleRuntime = ruleRuntime;
        this.localRuleExecutionSetProvider = localRuleExecutionSetProvider;
        this.classLoader = classLoader;
    }

    /**
     * Adds the rule set and returns the bind URI for the rule set. The bind URI should be used to
     * create a session associated with this rule set
     *
     * @param description information about the rule set
     * @return the URI to associate with the rule set
     */
    public String addRuleSet(RuleSetDescription description) {
        if (log.isDebugEnabled()) {
            log.debug("Creating a Rule Execution Set for JSR94Engine");
        }
        Map<String, Object> properties = PropertyDescriptionEvaluator.evaluate(
                description.getCreationProperties());
        if (defaultPropertiesProvider != null) {
            Map<String, Object> map =
                    defaultPropertiesProvider.getRuleExecutionSetCreationDefaultProperties(
                            classLoader);
            properties.putAll(map);
        }
        //if there isn't already source , then set it to XML
        if (!properties.containsKey(RuleConstants.SOURCE)) {
            properties.put(RuleConstants.SOURCE,
                    RuleConstants.FORMAT_DRL);
        }
        InputStream in = ObjectToStreamConverter.toInputStream(description.getRuleSource(),
                properties);
        if (in == null) {
            throw new LoggedRuntimeException(" The input stream form rule script is null.",
                    log);
        }
        return registerRuleExecutionSet(in, properties, description.getBindURI(), description);

    }

    /**
     * Creates a JSR94 stateful or stateless session based on the provided session information
     *
     * @param description information about the session
     * @return <code>Session</code> instance to be used to access rule engine runtime
     */
    public Session createSession(SessionDescription description) {
        boolean stateful = description.isStateful();
        try {
            String name = description.getRuleSetURI();
            final Map<String, Object> properties =
                    PropertyDescriptionEvaluator.evaluate(description.getSessionProperties());

            if (stateful) {
                if (log.isDebugEnabled()) {
                    log.debug("Using stateful rule session ");
                }

                // create state full rule session and sets inputs
                StatefulRuleSession ruleSession =
                        (StatefulRuleSession) ruleRuntime.createRuleSession(name,
                                properties,
                                RuleRuntime.STATEFUL_SESSION_TYPE);

                if (ruleSession == null) {
                    throw new LoggedRuntimeException("The created stateful rule session is null",
                            log);
                }
                return new JSR94StatefulSession(ruleSession);

            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Using stateless rule session");
                }
                // create stateless rule session
                StatelessRuleSession ruleSession =
                        (StatelessRuleSession) ruleRuntime.createRuleSession(
                                name, properties,
                                RuleRuntime.STATELESS_SESSION_TYPE);

                if (ruleSession == null) {
                    throw new LoggedRuntimeException("The created stateless rule session is null",
                            log);
                }
                return new JSR94StatelessSession(ruleSession);

            }
        } catch (
                RuleSessionTypeUnsupportedException e) {
            throw new LoggedRuntimeException("Error was occurred when creating " +
                    (stateful ? " StateFul" : "StateLess") + " Session", e, log);
        } catch (
                RuleSessionCreateException e) {
            throw new LoggedRuntimeException("Error was occurred when creating " +
                    (stateful ? " StateFul" : "StateLess") + " Session", e, log);
        } catch (
                RemoteException e) {
            throw new LoggedRuntimeException("Error was occurred when creating " +
                    (stateful ? " StateFul" : "StateLess") + " Session", e, log);
        } catch (
                RuleExecutionSetNotFoundException e) {
            throw new LoggedRuntimeException("Error was occurred when creating " +
                    (stateful ? " StateFul" : "StateLess") + " Session", e, log);
        } catch (Exception e) {
            throw new LoggedRuntimeException("Unknown Error was when executing rules using " +
                    (stateful ? " StateFul" : "StateLess") + " Session", e, log);
        }
    }

    /**
     * Removed the rule execution set runtime from the rule runtime based on the information
     * in the given rule set description
     *
     * @param description information about the rule set to be removed
     */
    public void removeRuleSet(RuleSetDescription description) {
        String bindUri = description.getBindURI();
        try {
            // Removes the old rule sets , if there are any
            if (log.isDebugEnabled()) {
                log.debug("Removing the rule execution set" +
                        " that has been bind to Uri " + bindUri);
            }
            Map<String, Object> properties = PropertyDescriptionEvaluator.evaluate(
                    description.getDeregistrationProperties());
            ruleAdministrator.deregisterRuleExecutionSet(bindUri, properties);

        } catch (RuleExecutionSetDeregistrationException e) {
            throw new LoggedRuntimeException("Error was occurred when tying to unregistered " +
                    "the RuleExecutionSet which has Uri " + bindUri, e, log);
        } catch (RemoteException e) {
            throw new LoggedRuntimeException("Error was occurred when trying to unregistered " +
                    "the RuleExecutionSet which has Uri " + bindUri, e, log);
        }
    }

    /**
     * TODO
     */
    public void destroy() {
        //TODO
    }

    /**
     * Helper method to register a rule execution set in the local rule set execution provider
     *
     * @param in          Rule set as an input stream
     * @param properties  properties to be used in the registration process.
     * @param bindUri     the URI to be associated with the rule set
     * @param description information about the rule set
     * @return the URI associated with the rule set
     */
    private String registerRuleExecutionSet(InputStream in,
                                            Map<String, Object> properties,
                                            String bindUri,
                                            RuleSetDescription description) {
        try {
            //Create the rule execution set
            RuleExecutionSet ruleExecutionSet =
                    localRuleExecutionSetProvider.createRuleExecutionSet(
                            in,
                            properties);
            if (ruleExecutionSet == null) {
                throw new LoggedRuntimeException("The newly created rule execution" +
                        " set is null ", log);
            }
            if (bindUri == null || "".equals(bindUri)) {
                //set the binding url
                bindUri = ruleExecutionSet.getName();
                description.setBindURI(bindUri);
            }

            Map<String, Object> regMap = PropertyDescriptionEvaluator.evaluate(
                    description.getRegistrationProperties());
            ruleAdministrator.registerRuleExecutionSet(bindUri, ruleExecutionSet, regMap);
            return bindUri;
        } catch (RuleExecutionSetCreateException e) {
            throw new LoggedRuntimeException("Error was occurred when creating" +
                    " the RuleExecutionSet", e, log);
        } catch (IOException e) {
            throw new LoggedRuntimeException("Error was occurred when getting an input stream" +
                    " from provided rule script", e, log);
        } catch (RuleExecutionSetRegisterException e) {
            throw new LoggedRuntimeException("Error was occurred when trying to registering " +
                    "the RuleExecutionSet with Uri " + bindUri, e, log);
        } catch (Exception e) {
            throw new LoggedRuntimeException("Unknown Error was occurred when trying to " +
                    "registering the RuleExecutionSet with Uri " + bindUri, e, log);
        }
    }

    public void setDefaultPropertiesProvider(
            DefaultPropertiesProvider defaultPropertiesProvider) {
        this.defaultPropertiesProvider = defaultPropertiesProvider;
    }
}
