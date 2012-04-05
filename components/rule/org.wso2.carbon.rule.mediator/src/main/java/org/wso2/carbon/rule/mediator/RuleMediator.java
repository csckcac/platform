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
package org.wso2.carbon.rule.mediator;

import org.apache.axiom.om.OMElement;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.mediators.AbstractListMediator;
import org.wso2.carbon.rule.core.Expirable;
import org.wso2.carbon.rule.core.Session;
import org.wso2.carbon.rule.server.*;
import org.wso2.carbon.rulecep.adapters.InputAdapterFactory;
import org.wso2.carbon.rulecep.adapters.InputManager;
import org.wso2.carbon.rulecep.adapters.OutputManager;
import org.wso2.carbon.rulecep.adapters.service.InputOutputAdaptersConfiguration;
import org.wso2.carbon.rulecep.adapters.service.InputOutputAdaptersConfigurationFactory;
import org.wso2.carbon.rulecep.adapters.service.InputOutputAdaptersService;
import org.wso2.carbon.rulecep.adapters.service.InputOutputAdaptersServiceImpl;
import org.wso2.carbon.rulecep.commons.ReturnValue;
import org.wso2.carbon.rulecep.commons.descriptions.ResourceDescription;
import org.wso2.carbon.rulecep.commons.descriptions.rule.RuleSetDescription;
import org.wso2.carbon.rulecep.commons.descriptions.rule.SessionDescription;
import org.wso2.carbon.rulecep.commons.descriptions.rule.mediator.RuleMediatorDescription;
import org.wso2.carbon.rulecep.commons.utils.OMElementHelper;

import java.util.List;

/**
 * Mediator adopting the rule component to provide integrations services in terms of rules
 */
public class RuleMediator extends AbstractListMediator {

    private final static String EXECUTE_CHILDREN = "execute_children";

    /**
     * Required information to use the rule engine
     */
    private RuleMediatorDescription ruleMediatorDescription;

    /**
     * Access facade to rule service provider
     */
    private RuleEngine ruleEngine;

    /**
     * To formulate the facts from the Synapse Message Context
     */
    private InputManager inputManager;

    /**
     * To enrich the Synapse Message Context with the results from the rule execution
     */
    private OutputManager outputManager;

    /**
     * Intercepts the Synapse Message Context
     */
    private final SynapseMessageInterceptor interceptor = new SynapseMessageInterceptor();

    /**
     * if the rule set source is in registry, the key to access it
     */
    private String registryKey;

    /**
     * Identifies whether there is already rule set registered with the rule engine
     */
    private boolean added;

    /* Lock used to ensure thread-safe creation of rule set from registry resources */
    private final Object resourceLock = new Object();

    private Session session;

    /**
     * Creates an instance of the rule mediator
     * Both arguments should be valid
     *
     * @param ruleMediatorDescription <code>RuleMediatorDescription</code> providing information
     *                                required by the rule engine
     */
    public RuleMediator(RuleMediatorDescription ruleMediatorDescription) {
        this.ruleMediatorDescription = ruleMediatorDescription;
    }

    /**
     * Initiates the rule mediator by creating a rule engine, an input manager and an output manager
     * Rule set is registered with the rule engine if the source is in-lined. Otherwise, it will be
     * done at the first execution
     *
     * @param se SynapseEnvironment to be used for initialization
     */
    public void init(SynapseEnvironment se) {

        OSGIServiceLocator osgiServiceLocator = OSGIServiceLocator.getInstance();

        RuleServerManagerService ruleServerManager;
        InputOutputAdaptersService inputOutputAdaptersService;
        if (osgiServiceLocator.isInitialized()) {
            ruleServerManager =
                    (RuleServerManagerService) osgiServiceLocator.locateService(
                            RuleServerManagerService.class.getName());
            inputOutputAdaptersService =
                    (InputOutputAdaptersService) osgiServiceLocator.locateService(
                            InputOutputAdaptersService.class.getName());
        } else {
            log.info("Cannot find RuleServerManagerService, Using the default RuleServerManager" +
                    " implementation ");
            OMElement defaultConf = OMElementHelper.getInstance().toOM("<RuleServer/>");
            RuleServerConfiguration ruleServerConfiguration =
                    RuleServerConfigurationFactory.create(defaultConf, new SynapseXPathFactory());
            ruleServerManager = new RuleServerManager();
            ((RuleServerManager) ruleServerManager).init(ruleServerConfiguration);

            OMElement defaultAdaptersConf =
                    OMElementHelper.getInstance().toOM("<InputOutputManager/>");
            InputOutputAdaptersConfiguration configuration =
                    InputOutputAdaptersConfigurationFactory.create(defaultAdaptersConf,
                            new SynapseXPathFactory());
            inputOutputAdaptersService = new InputOutputAdaptersServiceImpl(configuration);
        }

        ResourceDescription mediatorAsFact = new ResourceDescription();
        mediatorAsFact.setType(MediatorResourceAdapter.TYPE);
        mediatorAsFact.setValue(MediatorResourceAdapter.class.getName());
        InputAdapterFactory inputAdapterFactory = inputOutputAdaptersService.getFactAdapterFactory();
        if (!inputAdapterFactory.containsInputAdapter(MediatorResourceAdapter.TYPE)) {
            inputAdapterFactory.addInputAdapter(mediatorAsFact);
        }

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = this.getClass().getClassLoader();
        }

        RuleSetDescription ruleSetDescription = ruleMediatorDescription.getRuleSetDescription();
        ruleEngine = ruleServerManager.createRuleEngine(classLoader);
        if (ruleSetDescription.getRuleSource() != null) {
            String bindURI = ruleEngine.addRuleSet(ruleSetDescription);
            SessionDescription sessionDescription = ruleMediatorDescription.getSessionDescription();
            sessionDescription.setRuleSetURI(bindURI);
        } else {
            registryKey = ruleSetDescription.getKey();
        }

        inputManager = inputOutputAdaptersService.createInputManager(
                ruleMediatorDescription.getFacts(),
                interceptor);
        outputManager = inputOutputAdaptersService.createOutputManager(
                ruleMediatorDescription.getResults(),
                interceptor);
        super.init(se);
    }

    /**
     * General mediator method. This method provides what will be happened when message is going
     * through the rule mediator
     *
     * @param synCtx The Synapse Message Context
     * @return Always true , to continue mediation
     * @see org.apache.synapse.Mediator#mediate(org.apache.synapse.MessageContext)
     */
    public boolean mediate(MessageContext synCtx) {

        SynapseLog synLog = getLog(synCtx);

        if (synLog.isTraceOrDebugEnabled()) {
            synLog.traceOrDebug("Start : Rule mediator");

            if (synLog.isTraceTraceEnabled()) {
                synLog.traceTrace("Message : " + synCtx.getEnvelope());
            }
        }

        SessionDescription sessionDescription = ruleMediatorDescription.getSessionDescription();

        if (registryKey != null) {
            ReturnValue returnValue = interceptor.extract(registryKey, synCtx, null);
            if (returnValue.getValue() == null) {
                handleException("RuleSet cannot be found for the registry key : " + registryKey,
                        synCtx);
            }
            if (returnValue.isFresh()) {
                synchronized (resourceLock) {
                    addRuleSet(returnValue.getValue(), sessionDescription);
                }
            }
            if (!added) {
                synchronized (resourceLock) {
                    if (!added) {
                        addRuleSet(returnValue.getValue(), sessionDescription);
                    }
                }
            }
        }

        boolean reCreateSession = true;
        if (session != null) {     //todo synchronize as needed 
            if (session instanceof Expirable && ((Expirable) session).isExpired()) {
                session.release();
            } else {
                reCreateSession = false;
            }
        }

        if (reCreateSession) {
            session = ruleEngine.createSession(sessionDescription);
        }

        List<Object> facts = inputManager.processInputs(synCtx);
        if (facts.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("There is no facts to be injected into the rule engine");
            }
            return true;
        }
        List results = session.execute(facts);
        outputManager.processOutputs(results, synCtx);

        //execute the rules
        String executeChildren = (String) synCtx.getProperty(EXECUTE_CHILDREN);
        if (executeChildren != null) {
            synCtx.getPropertyKeySet().remove(EXECUTE_CHILDREN);
            if ("true".equals(executeChildren)) {
                return super.mediate(synCtx);
            }
        }
        if (synLog.isTraceOrDebugEnabled()) {
            synLog.traceOrDebug("End : Rule mediator");
        }
        return true;
    }

    public RuleMediatorDescription getRuleMediatorDescription() {
        return ruleMediatorDescription;
    }

    /**
     * Helper method to add a rule set
     *
     * @param ruleSet            rule set as a object
     * @param sessionDescription information about the session to be associated with the rule set
     */
    private void addRuleSet(Object ruleSet, SessionDescription sessionDescription) {
        RuleSetDescription ruleSetDescription =
                ruleMediatorDescription.getRuleSetDescription();
        if (added) {
            ruleEngine.removeRuleSet(ruleSetDescription);
        }
        ruleSetDescription.setRuleSource(ruleSet);
        String bindURI = ruleEngine.addRuleSet(ruleSetDescription);
        added = true;
        sessionDescription.setRuleSetURI(bindURI);
    }
}
