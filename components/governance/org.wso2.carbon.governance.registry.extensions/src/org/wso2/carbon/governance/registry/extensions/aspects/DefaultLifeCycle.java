/*
 * Copyright (c) 2006, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.governance.registry.extensions.aspects;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.util.XMLUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.scxml.io.SCXMLParser;
import org.apache.commons.scxml.model.*;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.w3c.dom.Element;
import org.wso2.carbon.governance.registry.extensions.aspects.utils.LifecycleConstants;
import org.wso2.carbon.governance.registry.extensions.aspects.utils.StatCollection;
import org.wso2.carbon.governance.registry.extensions.aspects.utils.StatWriter;
import org.wso2.carbon.governance.registry.extensions.beans.CheckItemBean;
import org.wso2.carbon.governance.registry.extensions.beans.CustomCodeBean;
import org.wso2.carbon.governance.registry.extensions.beans.PermissionsBean;
import org.wso2.carbon.governance.registry.extensions.beans.ScriptBean;
import org.wso2.carbon.governance.registry.extensions.interfaces.CustomValidations;
import org.wso2.carbon.governance.registry.extensions.interfaces.Execution;
import org.wso2.carbon.mashup.javascript.hostobjects.registry.CollectionHostObject;
import org.wso2.carbon.mashup.javascript.hostobjects.registry.RegistryHostObject;
import org.wso2.carbon.mashup.javascript.hostobjects.registry.ResourceHostObject;
import org.wso2.carbon.mashup.utils.MashupConstants;
import org.wso2.carbon.registry.core.Aspect;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.session.CurrentSession;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.user.core.UserStoreException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.CharArrayReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static org.wso2.carbon.governance.registry.extensions.aspects.utils.Utils.getHistoryInfoElement;


public class DefaultLifeCycle extends Aspect {
    private static final Log log = LogFactory.getLog(DefaultLifeCycle.class);

    private String lifecycleProperty = "registry.LC.name";
    private String stateProperty = "registry.lifecycle.SoftwareProjectLifecycle.state";

//    Variables to keep track of lifecycle information
    private List<String> states;
    private Map<String, List<CheckItemBean>> checkListItems;
    private Map<String, List<CustomCodeBean>> transitionValidations;
    private Map<String, List<CustomCodeBean>> transitionExecution;
    private Map<String, List<PermissionsBean>> transitionPermission;
    private Map<String, List<String>> stateEvents;
    private Map<String, List<ScriptBean>> scriptElements;
    private Map<String, Map<String,String>> transitionUIs;


    private boolean isConfigurationFromResource;
    private String configurationResourcePath;
    private OMElement configurationElement;
    private String aspectName;

//    Is there to check whether we need to audit the lifecycle actions.
    private boolean isAuditEnabled;

    private SCXML scxml;

    public DefaultLifeCycle(OMElement config) throws RegistryException {

        initialize();

        String currentAspectName = config.getAttributeValue(new QName(LifecycleConstants.NAME));
        aspectName = currentAspectName;
        currentAspectName = currentAspectName.replaceAll("\\s", "");
        stateProperty = LifecycleConstants.REGISTRY_LIFECYCLE + currentAspectName + ".state";

        Iterator stateElements = config.getChildElements();
        while (stateElements.hasNext()) {
            OMElement stateEl = (OMElement) stateElements.next();

            if (stateEl.getAttribute(new QName(LifecycleConstants.TYPE)) != null) {
                String type = stateEl.getAttributeValue(new QName(LifecycleConstants.TYPE));
                if (type.equalsIgnoreCase("resource")) {
                    isConfigurationFromResource = true;
                    configurationResourcePath = RegistryUtils.getAbsolutePath(
                            RegistryContext.getBaseInstance(), stateEl.getText());
                    clearAll();
                    break;
                } else if (type.equalsIgnoreCase("literal")) {
                    isConfigurationFromResource = false;
                    configurationElement = stateEl.getFirstElement();
                    clearAll();
                    break;
                }
            }
            String name = stateEl.getAttributeValue(new QName(LifecycleConstants.NAME));
            if (name == null) {
                throw new IllegalArgumentException(
                        "Must have a name attribute for each state");
            }
            states.add(name);
        }
    }

    private void clearAll() {
        states.clear();
        checkListItems.clear();
        transitionPermission.clear();
        transitionValidations.clear();
        transitionExecution.clear();
        transitionUIs.clear();
    }

    private void initialize() {
        states = new ArrayList<String>();
        checkListItems = new HashMap<String, List<CheckItemBean>>();
        transitionValidations = new HashMap<String, List<CustomCodeBean>>();
        transitionExecution = new HashMap<String, List<CustomCodeBean>>();
        transitionPermission = new HashMap<String, List<PermissionsBean>>();
        stateEvents = new HashMap<String, List<String>>();
        scriptElements = new HashMap<String, List<ScriptBean>>();
        transitionUIs = new HashMap<String, Map<String, String>>();

//        By default we enable auditing
        isAuditEnabled = true;
    }

    @Override
    public void associate(Resource resource, Registry registry)
            throws RegistryException {

        clearAll();
        try {
            setSCXMLConfiguration(registry);

            if(configurationElement == null){
                return;
            }

            List<String> propertyValues = resource.getPropertyValues(lifecycleProperty);
            if (propertyValues != null && propertyValues.size() > 0) {
                return;
            }
            if (states.size() == 0) {
                populateItems();
            }

//            Creating the checklist
//            this is the first time the life cycle is associated with a resource.
            String initialState = scxml.getInitial();
            AddCheckItems(resource, checkListItems.get(initialState), initialState);
            addScripts(initialState, resource);
            addTransitionUI(resource,initialState);

        } catch (Exception e) {
            String message = "Resource does not contain a valid XML configuration: " + e.toString();
            log.error(message);
            return;
        }

        resource.setProperty(stateProperty, scxml.getInitial().replace(".", " "));
        resource.setProperty(lifecycleProperty, aspectName);

    }

    private void setSCXMLConfiguration(Registry registry) throws RegistryException, XMLStreamException, IOException,
            SAXException, ModelException {
        String xmlContent;
        if (isConfigurationFromResource) {
            if (registry.resourceExists(configurationResourcePath)) {
                try {
                    Resource configurationResource = registry.get(configurationResourcePath);
                    xmlContent = new String((byte[]) configurationResource.getContent());
                    configurationElement = AXIOMUtil.stringToOM(xmlContent);
                } catch (Exception e) {
                    String msg = "Invalid lifecycle configuration found at " + configurationResourcePath;
                    log.error(msg);
                    throw new RegistryException(msg);
                }
            }else{
                String msg = "Unable to find the lifecycle configuration from the given path: "
                        + configurationResourcePath;
                log.error(msg);
                throw new RegistryException(msg);
            }
        }

        try {
//            We check if there is an attribute called "audit" and if it exists, what is the value of that attribute
            if(configurationElement.getAttributeValue(new QName(LifecycleConstants.AUDIT)) != null){
                isAuditEnabled = Boolean.parseBoolean(
                        configurationElement.getAttributeValue(new QName(LifecycleConstants.AUDIT)));
            }

//            Here we are taking the scxml element from the configuration
            OMElement scxmlElement = configurationElement.getFirstElement();
            scxml = SCXMLParser.parse(new InputSource(
                    new CharArrayReader((scxmlElement.toString()).toCharArray())), null);
        } catch (Exception e) {
            String msg = "Invalid SCXML configuration found";
            log.error(msg);
            throw new RegistryException(msg);
        }
    }

    private void addTransitionUI(Resource resource,String stateName){
        Map<String,String> currentStateTransitionUI = transitionUIs.get(stateName);

        List<String> tobeRemoved = new ArrayList<String>();
        Properties properties = resource.getProperties();
        for (Object key : properties.keySet()) {
            if(key.toString().startsWith(LifecycleConstants.REGISTRY_CUSTOM_LIFECYCLE_CHECKLIST_TRANSITION_UI)){
                tobeRemoved.add(key.toString());
            }
        }
        for (String key : tobeRemoved) {
            resource.removeProperty(key);
        }

        if (currentStateTransitionUI != null) {
            for (Map.Entry<String, String> entry : currentStateTransitionUI.entrySet()) {
                resource.setProperty(
                        LifecycleConstants.REGISTRY_CUSTOM_LIFECYCLE_CHECKLIST_TRANSITION_UI +entry.getKey()
                        ,entry.getValue());
            }
        }
    }
    private void AddCheckItems(Resource resource, List<CheckItemBean> currentStateCheckItems, String state){

        if (currentStateCheckItems != null) {
            int order = 0;
            for (CheckItemBean currentStateCheckItem : currentStateCheckItems) {
                List<PermissionsBean> permissions = currentStateCheckItem.getPermissionsBeans();

               List<String> allowedRoles = new ArrayList<String>();

                for (PermissionsBean permission : permissions) {
                    allowedRoles.addAll(permission.getRoles());
                }

                List<String> items = new ArrayList<String>();
                items.add("status:" + state);
                items.add("name:" + currentStateCheckItem.getName());
                items.add("value:false");
                items.add("order:" + order);
                String resourcePropertyNameForItem =
                        LifecycleConstants.REGISTRY_CUSTOM_LIFECYCLE_CHECKLIST_OPTION + order
                                + LifecycleConstants.ITEM;
                String resourcePropertyNameForItemPermission =
                        LifecycleConstants.REGISTRY_CUSTOM_LIFECYCLE_CHECKLIST_OPTION + order
                                + LifecycleConstants.ITEM_PERMISSION;

                resource.setProperty(resourcePropertyNameForItem, items);
                if(allowedRoles.isEmpty()){
                    resource.setProperty(resourcePropertyNameForItemPermission, resourcePropertyNameForItemPermission);
                }else{
                    resource.setProperty(resourcePropertyNameForItemPermission, allowedRoles);
                }

                order++;
            }
        }
    }

    private void addScripts(String state, Resource resource) {
        List<ScriptBean> scriptList = scriptElements.get(state);
        if (scriptList != null) {
            for (ScriptBean scriptBean : scriptList) {
                if (scriptBean.isConsole()) {
                    List<String> items = new ArrayList<String>();
                    items.add(scriptBean.getScript());
                    items.add(scriptBean.getFunctionName());

                    String resourcePropertyNameForScript =
                            LifecycleConstants.REGISTRY_CUSTOM_LIFECYCLE_CHECKLIST_JS_SCRIPT_CONSOLE + state
                                    + "." + scriptBean.getEventName();
                    resource.setProperty(resourcePropertyNameForScript, items);
                }
            }
        }
    }

    @Override
    public String[] getAvailableActions(RequestContext context) {

        Resource resource = context.getResource();
        String currentState;
        if (resource.getProperty(stateProperty) == null) {
            return new String[0];
        }
        currentState = resource.getProperty(stateProperty).replace(" ", ".");

        try {
            if (states.size() == 0 || !states.contains(currentState)) {
                clearAll();
                Registry registry = context.getRegistry();
                setSCXMLConfiguration(registry);
                populateItems();
            }

        } catch (Exception e) {
            throw new RuntimeException("Resource does not contain a valid XML configuration: " + e.toString());
        }

//        Need to check whether the correct user has done the checking
        ArrayList<String> actions = new ArrayList<String>();
        String user = CurrentSession.getUser();

        State currentExecutionState = (State) (scxml.getChildren()).get(currentState);
        List currentTransitions = currentExecutionState.getTransitionsList();

        try {
            List<PermissionsBean> permissionsBeans = transitionPermission.get(currentState);
            String[] roles = CurrentSession.getUserRealm().getUserStoreManager().getRoleListOfUser(user);

            /*In this loop we do both of the following tasks
            * 1.Make check items visible, not visible to the user
            * 2.Get the list of actions that is possible to the user*/

            for (Object currentTransition : currentTransitions) {
                Transition t = (Transition) currentTransition;
                String transitionName = t.getEvent();

                List<String> possibleActions = getPossibleActions(resource, currentState);
                if ((getTransitionPermission(roles, permissionsBeans, transitionName) || permissionsBeans == null)
                        && possibleActions.contains(transitionName)) {
                    actions.add(transitionName);
                }
            }
        } catch (UserStoreException e) {
            log.error("Failed to get the current user role :", e);
            return new String[0];
        }
        return actions.toArray(new String[actions.size()]);
    }

    private boolean getTransitionPermission(String[] roles, List<PermissionsBean> permissionsBeans, String eventName) {
        Set<String> premSet = new HashSet<String>(Arrays.asList(roles));
        if (permissionsBeans != null) {
            for (PermissionsBean permission : permissionsBeans) {
                if (permission.getForEvent().equals(eventName) && permission.getRoles() != null) {
                    List permRoles = permission.getRoles();
                    premSet.retainAll(permRoles);
                }
            }
        }
        return !premSet.isEmpty();
    }

    private boolean doAllCustomValidations(RequestContext context, String currentState) throws RegistryException{
        //doing the check item validations
        List<CheckItemBean> currentStateCheckItems = checkListItems.get(currentState);
        if (currentStateCheckItems != null) {
            for (CheckItemBean currentStateCheckItem : currentStateCheckItems) {
                try {
                    runCustomValidationsCode(context, currentStateCheckItem.getValidationBeans());
                } catch (RegistryException registryException) {
                    throw new RegistryException("Validation failed for check item : "
                          + currentStateCheckItem.getName());
                }
            }
        }
        //doing the transition validations
        return runCustomValidationsCode(context, transitionValidations.get(currentState));
    }

//    This method is used to validate both checkitem validations and transition validations
    private boolean runCustomValidationsCode(RequestContext context, List<CustomCodeBean> customCodeBeans)
            throws RegistryException{
        if (customCodeBeans != null) {
            for (CustomCodeBean customCodeBean : customCodeBeans) {
                CustomValidations customValidations = (CustomValidations) customCodeBean.getClassObeject();

//                logging
                ((StatCollection)context.getProperty(LifecycleConstants.STAT_COLLECTION))
                        .addValidations(customCodeBean.getClass().getName(),null);

                if (!customValidations.validate(context)) {
                    ((StatCollection)context.getProperty(LifecycleConstants.STAT_COLLECTION))
                            .addValidations(customCodeBean.getClass().getName(),getHistoryInfoElement("validation failed"));
                    throw new RegistryException("Validation : " + customCodeBean.getClassObeject().getClass().getName()
                            + " failed for action : " + customCodeBean.getEventName());
                }
            }
        }
        return true;
    }

//        This method is used to run the custom executors
    private boolean runCustomExecutorsCode(String action,RequestContext context, List<CustomCodeBean> customCodeBeans
            ,String currentState,String nextState)
            throws RegistryException{
        if (customCodeBeans != null) {
            for (CustomCodeBean customCodeBean : customCodeBeans) {
                if (customCodeBean.getEventName().equals(action)) {
                    Execution customExecutor = (Execution) customCodeBean.getClassObeject();

//                    logging
                    ((StatCollection)context.getProperty(LifecycleConstants.STAT_COLLECTION))
                            .addExecutors(customExecutor.getClass().getName(),null);

                    if (!customExecutor.execute(context,currentState,nextState)) {
                        ((StatCollection)context.getProperty(LifecycleConstants.STAT_COLLECTION))
                                .addExecutors(customExecutor.getClass().getName(),getHistoryInfoElement("executor failed"));
                        throw new RegistryException("Execution failed for action : " + customCodeBean.getEventName());
                    }
                }
            }
        }
        return true;
    }

    private CustomValidations loadCustomValidators(String className, Map parameterMap) throws Exception {

        CustomValidations customValidations;
        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            Class<?> customCodeClass = Class.forName(className, true, loader);
            customValidations = (CustomValidations) customCodeClass.newInstance();
            customValidations.init(parameterMap);

        }  catch (Exception e) {
            String msg = "Unable to load validations class";
            log.error(msg, e);
            throw new Exception(msg,e);
        }
        return customValidations;
    }
    private Execution loadCustomExecutors(String className, Map parameterMap) throws Exception {

        Execution customExecutors;
        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            Class<?> customCodeClass = Class.forName(className, true, loader);
            customExecutors = (Execution) customCodeClass.newInstance();
            customExecutors.init(parameterMap);

        } catch (Exception e) {
            String msg = "Unable to load executions class";
            log.error(msg, e);
            throw new Exception(msg,e);
        }
        return customExecutors;
    }

    private void handleItemClick(Resource resource,Map<String,String> itemParameterMap,RequestContext context)
            throws RegistryException{
        for (Map.Entry<String, String> entry : itemParameterMap.entrySet()) {
            List<String> propertyValues = resource.getPropertyValues(
                    LifecycleConstants.REGISTRY_CUSTOM_LIFECYCLE_CHECKLIST_OPTION + entry.getKey());
            if (propertyValues != null) {
                for (String propertyValue : propertyValues) {
                    if(propertyValue.startsWith("value:") && !propertyValue.contains(entry.getValue())){
                        List<String> newProps = new ArrayList<String>(propertyValues);
                        newProps.remove(propertyValue);
                        String replace = propertyValue.replace(Boolean.toString(!Boolean.valueOf(entry.getValue()))
                                , entry.getValue());
                        newProps.add(replace);
                        resource.removeProperty(LifecycleConstants.REGISTRY_CUSTOM_LIFECYCLE_CHECKLIST_OPTION
                                + entry.getKey());
                        resource.setProperty(LifecycleConstants.REGISTRY_CUSTOM_LIFECYCLE_CHECKLIST_OPTION
                                + entry.getKey(),newProps);

    //                    logging
                        StatCollection statCollection =
                                ((StatCollection)context.getProperty(LifecycleConstants.STAT_COLLECTION));
                        statCollection.setAction(getCheckItemName(propertyValues));
                        statCollection.setActionType(LifecycleConstants.ITEM_CLICK);
                        statCollection.setActionValue(replace);

                        if(resource.getProperty(LifecycleConstants.REGISTRY_LIFECYCLE_HISTORY_ORIGINAL_PATH) != null){
                            statCollection.setOriginalPath(
                                    resource.getProperty(LifecycleConstants.REGISTRY_LIFECYCLE_HISTORY_ORIGINAL_PATH));
                        }
                    }
                }
            }
        }
    }
    
    private String getCheckItemName(List<String> propValues){
        String name = null;

        for (String propValue : propValues) {
            if(propValue.startsWith("name:")){
                name = propValue.split("name:")[1];
            }
        }

        return name;
    }
    
    private Map<String,String> extractCheckItemValues(Map<String,String> parameterMap){
        Map<String,String> checkItems = new HashMap<String, String>();

        for (Map.Entry<String, String> entry : parameterMap.entrySet()) {
            if(entry.getKey().endsWith(LifecycleConstants.ITEM)){
                checkItems.put(entry.getKey(),entry.getValue());
            }
        }
        return checkItems;
    }
    
    @Override
    public void invoke(RequestContext context, String action) throws RegistryException {
        invoke(context,action,Collections.<String, String>emptyMap());
    }

    @Override
    public void invoke(RequestContext requestContext, String action,Map<String,String> parameterMap)
            throws RegistryException {
        boolean preserveOldResource = !Boolean.toString(false).equals(parameterMap.remove(
                "preserveOriginal"));
        Resource resource = requestContext.getResource();
        String currentState = resource.getProperty(stateProperty).replace(" ", ".");
        String resourcePath = requestContext.getResourcePath().getPath();
        String newResourcePath;
        String nextState=currentState;
        String user = CurrentSession.getUser();

        State currentExecutionState = (State) scxml.getChildren().get(currentState);

//        Stat collection object
        StatCollection statCollection = new StatCollection();
        statCollection.setAction(action);
        statCollection.setRegistry(requestContext.getSystemRegistry());
        statCollection.setState(currentState);
        statCollection.setResourcePath(resourcePath);
        statCollection.setUserName(user);
        statCollection.setOriginalPath(resourcePath);
        requestContext.setProperty(LifecycleConstants.STAT_COLLECTION,statCollection);

//        Here we are doing the checkitem related operations.
        handleItemClick(resource,extractCheckItemValues(parameterMap),requestContext);

//        Modify here for the checkitem and other validations.
        List transitions = currentExecutionState.getTransitionsList();
        try {
            String[] roles = CurrentSession.getUserRealm().getUserStoreManager().getRoleListOfUser(user);
            List<String> possibleEvents = getPossibleActions(resource, currentState);
            if (possibleEvents.size() > 0) {
                for (Object o : transitions) {
                    String eventName = ((Transition) o).getEvent();
                    if (possibleEvents.contains(eventName) && eventName.equals(action)) {
//                           transition validations go here
//                           There is need to check the transition permissions again as well to avoid fraud
                        if (getTransitionPermission(roles, transitionPermission.get(currentState), eventName)) {
                            if (doAllCustomValidations(requestContext, currentState)) {
//                              adding log
                                statCollection.setActionType(LifecycleConstants.TRANSITION);
                                if (resource.getProperty(
                                        LifecycleConstants.REGISTRY_LIFECYCLE_HISTORY_ORIGINAL_PATH) != null) {
                                    statCollection.setOriginalPath(resource.getProperty(
                                                    LifecycleConstants.REGISTRY_LIFECYCLE_HISTORY_ORIGINAL_PATH));
                                }
//                              The transition happens here.
                                nextState = ((Transition) o).getNext();
//                              We have identified the next state here
//                              We are going to run the custom executors now
                                runCustomExecutorsCode(action, requestContext, transitionExecution.get(currentState)
                                        , currentState, nextState);
//                              Doing the JS execution
                                List<ScriptBean> scriptElement = scriptElements.get(currentState);
                                try {
                                    if (scriptElement != null) {
                                        for (ScriptBean scriptBean : scriptElement) {
                                            if (scriptBean.getEventName().equals(eventName) && !scriptBean.isConsole()) {
                                                executeJS(AXIOMUtil.stringToOM(scriptBean.getScript()).getText()
                                                        + "\n" + scriptBean.getFunctionName() + "()");
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    String msg = "JavaScript execution failed.";
                                    log.error(msg);
                                    throw new RegistryException(msg);
                                }
                                break;
                            } else {
                                String msg = "Transition validations failed.";
                                log.info(msg);
                                throw new RegistryException(msg);
                            }
                        }
                    }
                }
            }
        } catch (UserStoreException e) {
            String msg = "Failed to get the current user role :" + e.toString();
            log.error(msg);
            throw new RegistryException(msg);
        }
//        We are getting the resource again because the users can change its properties from the executors
        if(requestContext.getResource() == null){
            requestContext.setResource(resource);
            requestContext.setProcessingComplete(true);
            return;
        }
//        Persisting the old resource
        if(!requestContext.getResource().equals(resource))
            requestContext.getRegistry().put(resourcePath,resource);

        resource = requestContext.getResource();
        newResourcePath = requestContext.getResourcePath().getPath();

        if (!currentState.equals(nextState)) {
            State state = (State) scxml.getChildren().get(nextState);
            resource.setProperty(stateProperty, state.getId().replace(".", " "));

            clearCheckItems(resource);
            AddCheckItems(resource, checkListItems.get(state.getId()), state.getId());
            addScripts(state.getId(), resource);
            addTransitionUI(resource, state.getId());

//            For auditing purposes
            statCollection.setTargetState(nextState);
        }
        if (!preserveOldResource) {
            requestContext.getRegistry().delete(resourcePath);
        }
        requestContext.getRegistry().put(newResourcePath, resource);
        
//        adding the logs to the registry
        if (isAuditEnabled) {
            (new StatWriter()).writeHistory((StatCollection) requestContext.getProperty(
                    LifecycleConstants.STAT_COLLECTION));
        }
    }

    private void clearCheckItems(Resource resource){
        Properties properties = (Properties) resource.getProperties().clone();
        for (Object o : properties.keySet()) {
            String key = (String) o;
            if(key.startsWith(LifecycleConstants.REGISTRY_CUSTOM_LIFECYCLE_CHECKLIST_OPTION)){
                resource.removeProperty(key);
            }
        }
    }

    private void populateItems() throws Exception {
        Map stateList = scxml.getChildren();

        for (Object stateObject : stateList.entrySet()) {

            Map.Entry state = (Map.Entry) stateObject;

            String currentStateName = (String) state.getKey();
            State currentState = (State) state.getValue();
            Datamodel model = currentState.getDatamodel();

            states.add(currentStateName);
            if (model != null) {
                List dataList = model.getData();
                for (Object dataObject : dataList) {
                    Data data = (Data) dataObject;
                    OMElement node = XMLUtils.toOM((Element) data.getNode());
                    /*
                    * when associating we will map the custom data model to a set of beans.
                    * These will be used for further actions.
                    * */
                    populateCheckItems(currentStateName, node);
                    populateTransitionValidations(currentStateName, node);
                    populateTransitionPermissions(currentStateName, node);
                    populateTransitionScripts(currentStateName, node);
                    populateTransitionUIs(currentStateName, node);
                    populateTransitionExecutors(currentStateName, node);
                }
            }

            List<String> events = new ArrayList<String>();
            for (Object t : currentState.getTransitionsList()) {
                Transition transition = (Transition) t;
                events.add(transition.getEvent());
            }
            stateEvents.put(currentStateName, events);
        }
    }

    private void populateTransitionExecutors(String currentStateName, OMElement node) throws Exception {
        if (!transitionExecution.containsKey(currentStateName)
                && (node.getAttributeValue(new QName(LifecycleConstants.NAME)).equals("transitionExecution"))) {
            List<CustomCodeBean> customCodeBeanList = new ArrayList<CustomCodeBean>();
            Iterator executorsIterator = node.getChildElements();
            while (executorsIterator.hasNext()) {
                OMElement executorChild = (OMElement) executorsIterator.next();
                customCodeBeanList.add(createCustomCodeBean(executorChild, LifecycleConstants.EXECUTION));
            }
            transitionExecution.put(currentStateName, customCodeBeanList);
        }
    }

    private void populateTransitionUIs(String currentStateName, OMElement node) {
        //                    Adding the transition UIs
        if (!transitionUIs.containsKey(currentStateName)
                && (node.getAttributeValue(new QName(LifecycleConstants.NAME)).equals("transitionUI"))) {
            Map<String,String> uiEventMap = new HashMap<String, String>();
            Iterator uiIterator = node.getChildElements();

            while (uiIterator.hasNext()) {
                OMElement uiElement = (OMElement) uiIterator.next();
                uiEventMap.put(uiElement.getAttributeValue(new QName(LifecycleConstants.FOR_EVENT))
                            ,uiElement.getAttributeValue(new QName("href")));
            }
            transitionUIs.put(currentStateName, uiEventMap);
        }
    }

    private void populateTransitionScripts(String currentStateName, OMElement node) {
        //                  Adding the script elements
        if (!scriptElements.containsKey(currentStateName)
                && (node.getAttributeValue(new QName(LifecycleConstants.NAME)).equals("transitionScripts"))) {
            List<ScriptBean> scriptBeans = new ArrayList<ScriptBean>();
            Iterator scriptIterator = node.getChildElements();

            while (scriptIterator.hasNext()) {
                OMElement script = (OMElement) scriptIterator.next();
                Iterator scriptChildIterator = script.getChildElements();
                while (scriptChildIterator.hasNext()) {
                    OMElement scriptChild = (OMElement) scriptChildIterator.next();
                    scriptBeans.add(new ScriptBean(scriptChild.getQName().getLocalPart().equals("console"),
                            scriptChild.getAttributeValue(new QName("function")),
                            script.getAttributeValue(new QName(LifecycleConstants.FOR_EVENT)),
                            scriptChild.getFirstElement().toString()));
                }
            }
            scriptElements.put(currentStateName, scriptBeans);
        }
    }

    private void populateTransitionPermissions(String currentStateName, OMElement node) {
        //                  Adding the transition permissions
        if (!transitionPermission.containsKey(currentStateName)
                && (node.getAttributeValue(new QName(LifecycleConstants.NAME)).equals("transitionPermission"))) {
            List<PermissionsBean> permissionsBeanList = new ArrayList<PermissionsBean>();
            Iterator permissionIterator = node.getChildElements();
            while (permissionIterator.hasNext()) {
                OMElement permChild = (OMElement) permissionIterator.next();
                permissionsBeanList.add(createPermissionBean(permChild));
            }
            transitionPermission.put(currentStateName, permissionsBeanList);
        }
    }

    private void populateTransitionValidations(String currentStateName, OMElement node) throws Exception {
        //                  Adding the state validations
        if (!transitionValidations.containsKey(currentStateName)
                && (node.getAttributeValue(new QName(LifecycleConstants.NAME)).equals("transitionValidation"))) {
            List<CustomCodeBean> customCodeBeanList = new ArrayList<CustomCodeBean>();
            Iterator validationsIterator = node.getChildElements();
            while (validationsIterator.hasNext()) {
                OMElement validationChild = (OMElement) validationsIterator.next();
                customCodeBeanList.add(createCustomCodeBean(validationChild, LifecycleConstants.VALIDATION));
            }
            transitionValidations.put(currentStateName, customCodeBeanList);
        }
    }

    private void populateCheckItems(String currentStateName, OMElement node) throws Exception {
        //                    adding the checkItems
        if (!checkListItems.containsKey(currentStateName)
               && (node.getAttributeValue(new QName(LifecycleConstants.NAME)).equals("checkItems"))) {

           List<CheckItemBean> checkItems = new ArrayList<CheckItemBean>();

           Iterator checkItemIterator = node.getChildElements();
           while (checkItemIterator.hasNext()) {
               CheckItemBean checkItemBean = new CheckItemBean();
               OMElement childElement = (OMElement) checkItemIterator.next();

               //setting the check item name
               checkItemBean.setName(childElement.getAttributeValue(new QName(LifecycleConstants.NAME)));

               //setting the transactionList
               if ((childElement.getAttributeValue(new QName(LifecycleConstants.FOR_EVENT))) != null) {
                   checkItemBean.setEvents(Arrays.asList((childElement
                           .getAttributeValue(new QName(LifecycleConstants.FOR_EVENT))).split(",")));
               }

               Iterator permissionElementIterator = childElement
                       .getChildrenWithName(new QName("permissions"));

               while (permissionElementIterator.hasNext()) {
                   OMElement permissionElement = (OMElement) permissionElementIterator.next();

                   Iterator permissions = permissionElement.getChildElements();
                   List<PermissionsBean> permBeanList = new ArrayList<PermissionsBean>();

                   while (permissions.hasNext()) {
                       OMElement permChild = (OMElement) permissions.next();
                       permBeanList.add(createPermissionBean(permChild));
                   }
                   checkItemBean.setPermissionsBeans(permBeanList);
               }

               Iterator validationsElementIterator = childElement
                       .getChildrenWithName(new QName("validations"));

               while (validationsElementIterator.hasNext()) {
//                          setting the validation bean
                   List<CustomCodeBean> customCodeBeanList = new ArrayList<CustomCodeBean>();
                   OMElement validationElement = (OMElement) validationsElementIterator.next();
                   Iterator validations = validationElement.getChildElements();

//                             this loop is to iterate the validation elements
                   while (validations.hasNext()) {
                       OMElement validationChild = (OMElement) validations.next();
                       customCodeBeanList.add(createCustomCodeBean(validationChild, LifecycleConstants.VALIDATION));
                   }
                   checkItemBean.setValidationBeans(customCodeBeanList);
               }
               checkItems.add(checkItemBean);
           }
           if (checkItems.size() > 0) {
               checkListItems.put(currentStateName, checkItems);
           }
       }
    }


    private PermissionsBean createPermissionBean(OMElement permChild) {
        PermissionsBean permBean = new PermissionsBean();
        permBean.setForEvent(permChild.getAttributeValue(new QName(LifecycleConstants.FOR_EVENT)));
        if (permChild.getAttributeValue(new QName("roles")) != null)
            permBean.setRoles(Arrays.asList(permChild.getAttributeValue(new QName("roles"))
                    .split(",")));
        return permBean;
    }

    private CustomCodeBean createCustomCodeBean(OMElement customCodeChild,String type) throws Exception {
        CustomCodeBean customCodeBean = new CustomCodeBean();
        Map<String, String> paramNameValues = new HashMap<String, String>();

        Iterator parameters = customCodeChild.getChildElements();
        while (parameters.hasNext()) {
            // this loop is for the parameter name and values
            OMElement paramChild = (OMElement) parameters.next();
            paramNameValues.put(paramChild.getAttributeValue(new QName(LifecycleConstants.NAME)),
                    paramChild.getAttributeValue(new QName("value")));
        }
        if (type.equals(LifecycleConstants.VALIDATION)) {
            customCodeBean.setClassObeject(loadCustomValidators(
                    customCodeChild.getAttributeValue(new QName("class")), paramNameValues));
        } else if(type.equals(LifecycleConstants.EXECUTION)) {
            customCodeBean.setClassObeject(loadCustomExecutors(
                    customCodeChild.getAttributeValue(new QName("class")), paramNameValues));
        }
        customCodeBean.setEventName(customCodeChild.getAttributeValue(new QName(LifecycleConstants.FOR_EVENT)));
        return customCodeBean;
    }

    private List<String> getPossibleActions(Resource resource, String currentState) {

        Properties propertyNameValues = resource.getProperties();
        Iterator propIterator = propertyNameValues.entrySet().iterator();
        List<CheckItemBean> checkItems = checkListItems.get(currentState);
        List<String> events = new ArrayList<String>(stateEvents.get(currentState));

        if (checkItems !=null && checkItems.size()>0) {
            while (propIterator.hasNext()) {
                Map.Entry entry = (Map.Entry) propIterator.next();
                String propertyName = (String) entry.getKey();

                if (propertyName.startsWith(LifecycleConstants.REGISTRY_CUSTOM_LIFECYCLE_CHECKLIST_OPTION)) {
                    List<String> propValues = (List<String>) entry.getValue();
                    for (String propValue : propValues)
                        if (propValue.startsWith("name:"))
                            for (CheckItemBean checkItem : checkItems)
                                if ((checkItem.getName().equals(propValue.substring(propValue.indexOf(":") + 1))) &&
                                        (checkItem.getEvents() != null) && propValues.contains("value:false")) {
                                    events.removeAll(checkItem.getEvents());
                                }
                }

            }
        }
        return events;
    }

    private void executeJS(String script) throws Exception{
        Context cx = Context.enter();
        try {
            ConfigurationContext configurationContext =
                    MessageContext.getCurrentMessageContext().getConfigurationContext();
            cx.putThreadLocal(MashupConstants.AXIS2_CONFIGURATION_CONTEXT, configurationContext);
            AxisService service = new AxisService();
            service.addParameter(MashupConstants.MASHUP_AUTHOR, CurrentSession.getUser());
            cx.putThreadLocal(MashupConstants.AXIS2_SERVICE, service);
            Scriptable scope = cx.initStandardObjects();
            ScriptableObject.defineClass(scope, ResourceHostObject.class);
            ScriptableObject.defineClass(scope, CollectionHostObject.class);
            ScriptableObject.defineClass(scope, RegistryHostObject.class);
            Object result = cx.evaluateString(scope, script, "<cmd>", 1, null);
            if (result != null && log.isInfoEnabled()) {
                log.info("JavaScript Result: " + Context.toString(result));
            }
        } catch (IllegalAccessException e) {
            String msg = "Unable to defining registry host objects.";
            throw new Exception(msg,e);
        } catch (InstantiationException e) {
            String msg = "Unable to instantiate the given registry host object.";
            throw new Exception(msg,e);
        } catch (InvocationTargetException e) {
            String msg = "An exception occurred while creating registry host objects.";
            throw new Exception(msg,e);
        } catch (AxisFault e) {
            String msg = "Failed to set user name parameter.";
            throw new Exception(msg,e);
        } catch (SecurityException ignored) {
            // If there is a security issue, simply live with that. This portion of the sample is
            // not intended to work on a system with security restrictions.
        } finally {
            Context.exit();
        }
    }

    @Override
    public void dissociate(RequestContext requestContext) {

        Resource resource = requestContext.getResource();

        if (resource != null) {
            resource.removeProperty(stateProperty);
            resource.removeProperty(lifecycleProperty);
        }
    }
}
