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

package org.wso2.carbon.cep.fusion.backend;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.drools.KnowledgeBase;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.ResourceType;
import org.drools.definition.KnowledgePackage;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.rule.WorkingMemoryEntryPoint;
import org.wso2.carbon.cep.core.Expression;
import org.wso2.carbon.cep.core.backend.CEPBackEndRuntime;
import org.wso2.carbon.cep.core.exception.CEPConfigurationException;
import org.wso2.carbon.cep.core.exception.CEPEventProcessingException;
import org.wso2.carbon.cep.core.mapping.input.Input;
import org.wso2.carbon.cep.core.mapping.input.mapping.InputMapping;
import org.wso2.carbon.cep.core.internal.ds.CEPServiceValueHolder;
import org.wso2.carbon.cep.core.internal.util.CEPConstants;
import org.wso2.carbon.cep.core.listener.CEPEventListener;
import org.wso2.carbon.cep.fusion.listener.FusionEventListener;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Fusion based CEP back end runtime.
 */

public class FusionBackEndRuntime implements CEPBackEndRuntime {

    public static final String FUSION_ENTRY_POINT = "entry-point";
    public static final String FUSION_LISTENER_NAME = "fusionListener";

    private static final Log log = LogFactory.getLog(FusionBackEndRuntime.class);

    private KnowledgeBuilder knowledgeBuilder;
    private KnowledgeBase knowledgeBase;
    private StatefulKnowledgeSession statefulKnowledgeSession;
    private int tenantId;
    private Map<String, List<String>> knowledgePackagesMap;


    public FusionBackEndRuntime(KnowledgeBuilder knowledgeBuilder, KnowledgeBase knowledgeBase, int tenantId) {
        this.knowledgeBuilder = knowledgeBuilder;
        this.knowledgeBase = knowledgeBase;
        this.statefulKnowledgeSession = knowledgeBase.newStatefulKnowledgeSession();
        this.tenantId = tenantId;
        this.knowledgePackagesMap = new ConcurrentHashMap<String, List<String>>();
    }

    /**
     * get the cep entry point and insert the event. the entry point should have given
     * in the cep file it self
     *
     * @param event   - object representing the event data
     * @param inputMapping - Mapping to the topic which publish data
     *                details of the input stream to add events. eg. entry points in drools fusion.
     * @throws CEPEventProcessingException
     */
    public void insertEvent(Object event, InputMapping inputMapping) throws CEPEventProcessingException {

        String entryPoint = inputMapping.getStream();
        if (entryPoint != null) {
            WorkingMemoryEntryPoint cepEntryPoint =
                    this.statefulKnowledgeSession.getWorkingMemoryEntryPoint(entryPoint);
            if (cepEntryPoint != null) {
                cepEntryPoint.insert(event);
            } else {
                this.statefulKnowledgeSession.insert(event);
            }
        } else {
            this.statefulKnowledgeSession.insert(event);
        }
        this.statefulKnowledgeSession.fireAllRules();
    }

    /**
     * adds the query to the statefull session. if the expression is inline it takes the query
     * from it. otherwise read from the registry.
     *
     * @param expression       - cep rule source
     * @param queryName        - Name of the Query To be added
     * @param cepEventListener - wso2 cep engine pass this object to receive the events
     *                         back from the cep engine.
     * @throws CEPConfigurationException
     */
    public void addQuery(String queryName, Expression expression, CEPEventListener cepEventListener)
            throws CEPConfigurationException {

        InputStream inputStream = null;
        if (expression.getType().equals(CEPConstants.CEP_CONF_EXPRESSION_INLINE)) {
            inputStream = new ByteArrayInputStream(expression.getText().getBytes());
        } else if (expression.getType().equals(CEPConstants.CEP_CONF_EXPRESSION_REGISTRY)) {
            try {
                inputStream = new ByteArrayInputStream(readSourceTextFromRegistry(expression.getText().trim()).getBytes());
            } catch (RegistryException e) {
                String errorMessage = "Error in reading query from registry";
                log.error(errorMessage, e);
                throw new CEPConfigurationException(errorMessage, e);
            }
        } else {
            String errorMessage = "In valid expression type " + expression.getType();
            log.error(errorMessage);
            throw new CEPConfigurationException(errorMessage);
        }

        knowledgeBuilder.add(ResourceFactory.newInputStreamResource(inputStream),
                ResourceType.DRL);
        if (knowledgeBuilder.hasErrors()) {
            String errorMessage = "Error during creating rule set: " + knowledgeBuilder.getErrors().toString();
            log.error(errorMessage);
            throw new CEPConfigurationException(errorMessage);
        }
        knowledgeBase.addKnowledgePackages(knowledgeBuilder.getKnowledgePackages());

        List<String> packages = new ArrayList<String>();
        for (Iterator<KnowledgePackage> packageIter =
                     knowledgeBuilder.getKnowledgePackages().iterator(); packageIter.hasNext();){
            packages.add(packageIter.next().getName());
        }

        this.knowledgePackagesMap.put(queryName, packages);

        if (cepEventListener != null) {
            FusionEventListener fusionEventListener = new FusionEventListener(cepEventListener);
            if (expression.getListenerName() != null){
                statefulKnowledgeSession.setGlobal(expression.getListenerName(), fusionEventListener);
            } else {
                statefulKnowledgeSession.setGlobal(FUSION_LISTENER_NAME, fusionEventListener);
            }
        }



    }

    public void removeQuery(String queryName) throws CEPConfigurationException {
        // remove the query packages from knowledge base
        List<String> knowledgePackages = this.knowledgePackagesMap.remove(queryName);
        for (String knowledgePackage : knowledgePackages){
            this.knowledgeBase.removeKnowledgePackage(knowledgePackage);
        }
    }

    public void removeAllQueries() throws CEPConfigurationException {

        for (String queryName : this.knowledgePackagesMap.keySet()) {
            List<String> knowledgePackages = this.knowledgePackagesMap.get(queryName);
            for (String knowledgePackage : knowledgePackages) {
                this.knowledgeBase.removeKnowledgePackage(knowledgePackage);
            }
        }

        this.knowledgePackagesMap.clear();
    }

    @Override
    public void addInput(Input input) throws CEPConfigurationException {
        //Todo Implement
    }

    @Override
    public void removeInput(Input input) throws CEPConfigurationException {
        //Todo Implement
    }

    @Override
    public void init() {
        //todo Implement
    }

    private String readSourceTextFromRegistry(String key) throws RegistryException {
        Registry registry = CEPServiceValueHolder.getInstance().getRegistry(tenantId);
        Resource resource = registry.get(key);
        String content = new String((byte[]) resource.getContent());
        return content;

    }
}
