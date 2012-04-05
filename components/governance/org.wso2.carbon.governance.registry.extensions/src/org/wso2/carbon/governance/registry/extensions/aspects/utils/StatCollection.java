package org.wso2.carbon.governance.registry.extensions.aspects.utils;

import org.wso2.carbon.registry.core.Registry;

import java.util.HashMap;
import java.util.Map;

public class StatCollection {

    private Registry registry;
    private String resourcePath;
    private String originalPath;
    private String userName;
    private String state;
    private String action;
    private String actionType;
    private Map<String,String> validations;
    private Map<String,String> executors;
    private String actionValue;

    public String getOriginalPath() {
        return originalPath;
    }

    public void setOriginalPath(String originalPath) {
        this.originalPath = originalPath;
    }

    public String getActionValue() {
        return actionValue;
    }

    public void setActionValue(String comment) {
        this.actionValue = comment;
    }

    public void addValidations(String validationName,String info){
        if(validations == null){
            validations = new HashMap<String, String> ();
        }
        validations.put(validationName,info);
    }

    public void addExecutors(String executorName,String info){
        if(executors == null){
            executors =  new HashMap<String, String>();
        }
        executors.put(executorName,info);
    }
    public Map<String,String> getValidations() {
        return validations;
    }

    public void setValidations(Map<String,String> validations) {
        this.validations = validations;
    }

    public Map<String,String> getExecutors() {
        return executors;
    }
    public void setExecutors(Map<String,String> executors) {
        this.executors = executors;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Registry getRegistry() {
        return registry;
    }

    public void setRegistry(Registry registry) {
        this.registry = registry;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getResourcePath() {
        return resourcePath;
    }

    public void setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
