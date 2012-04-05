/*
 * Copyright (c) 2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.humantask.core.store;

import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.wso2.carbon.humantask.*;
import org.wso2.carbon.humantask.core.CallBackService;
import org.wso2.carbon.humantask.core.deployment.config.THTDeploymentConfig;
import org.wso2.carbon.humantask.core.utils.HumanTaskNamespaceContext;

import javax.wsdl.Definition;
import javax.wsdl.PortType;
import javax.xml.namespace.QName;
import java.util.List;

/**
 * Human Task configuration. Contains Task definition, deployment configurations and can use to get all task
 * related properties such as deadlines, presentation parameters, etc.
 */
public class TaskConfiguration extends HumanTaskBaseConfiguration {
     private static Log log = LogFactory.getLog(TaskConfiguration.class);

    // Task definition
    private TTask task;

    // Task Service/Callback and other configuration info
    private THTDeploymentConfig.Task taskDeploymentConfiguration;

    // WSDL definition for the task response(task parent's service)
    private Definition responseWSDL;

    // Whether both interfaces(task interface ans callback interface) in one WSDL
    private boolean useOneWSDL = false;

    // Whether this task definition is a sub task of a another task
    private boolean subTask;

    // TODO: Do we want to keep a reference to child tasks
    private TaskConfiguration parent;

    private CallBackService callBackService;

    public TaskConfiguration(TTask task){}

    public TaskConfiguration(TTask task,
                             THTDeploymentConfig.Task taskDeploymentConfiguration,
                             HumanInteractionsDocument humanInteractionsDocument,
                             List<Definition> wsdls,
                             String targetNamespace,
                             String humanTaskArtifactName,
                             AxisConfiguration tenatAxisConf, String packageName) {
        super(humanInteractionsDocument, targetNamespace, humanTaskArtifactName, tenatAxisConf, true, packageName);

        this.task = task;
        this.taskDeploymentConfiguration = taskDeploymentConfiguration;

        Definition taskWSDL = findWSDLDefinition(wsdls, getPortType(), getOperation());
        if (taskWSDL == null) {
            throw new NullPointerException("Cannot find WSDL definition for task: " + task.getName());
        }
        setWSDL(taskWSDL);

        HumanTaskNamespaceContext nsContext = new HumanTaskNamespaceContext();
        populateNamespace(task.getDomNode().getNodeType() == Node.ELEMENT_NODE ?
                (Element) task.getDomNode() : null, nsContext);
        setNamespaceContext(nsContext);

        PortType portType;
        if ((portType = getWSDL().getPortType(getResponsePortType())) != null) {
            if (portType.getOperation(getResponseOperation(), null, null) != null) {
                useOneWSDL = true;
            }
        }

        if (!useOneWSDL) {
            responseWSDL = findWSDLDefinition(wsdls, getResponsePortType(), getResponseOperation());
        }
    }

    public TTask getTask() {
        return task;
    }

    public void setTask(TTask task) {
        this.task = task;
    }

    public THTDeploymentConfig.Task getTaskDeploymentConfiguration() {
        return taskDeploymentConfiguration;
    }

    public void setTaskDeploymentConfiguration(THTDeploymentConfig.Task taskDeploymentConfiguration) {
        this.taskDeploymentConfiguration = taskDeploymentConfiguration;
    }

    public boolean isSubTask() {
        return subTask;
    }

    public void setSubTask(boolean subTask) {
        this.subTask = subTask;
    }

    public TaskConfiguration getParent() {
        return parent;
    }

    public void setParent(TaskConfiguration parent) {
        this.parent = parent;
    }

    public Definition getResponseWSDL() {
        if (!useOneWSDL) {
        return responseWSDL;
        } else {
            return getWSDL();
        }
    }

    public void setResponseWSDL(Definition responseWSDL) {
        this.responseWSDL = responseWSDL;
    }

    public boolean isUseOneWSDL() {
        return useOneWSDL;
    }

    public void setUseOneWSDL(boolean useOneWSDL) {
        this.useOneWSDL = useOneWSDL;
    }

    public QName getResponsePortType() {
        return task.getInterface().getResponsePortType();
    }

    public String getResponseOperation() {
        return task.getInterface().getResponseOperation();
    }

    @Override
    public QName getPortType() {
        return task.getInterface().getPortType();
    }

    @Override
    public String getOperation() {
        return task.getInterface().getOperation();
    }

    public QName getCallbackPortType() {
        return task.getInterface().getResponsePortType();
    }

    public String getCallbackOperation() {
        return task.getInterface().getResponseOperation();
    }

    @Override
    public QName getName() {
        return new QName(getTargetNamespace(), task.getName());
    }

    @Override
    public QName getServiceName() {
        return taskDeploymentConfiguration.getPublish().getService().getName();
    }

    @Override
    public String getPortName() {
        return taskDeploymentConfiguration.getPublish().getService().getPort();
    }

    public QName getCallbackServiceName() {
        return taskDeploymentConfiguration.getCallback().getService().getName();
    }

    public String getCallbackPortName() {
        return taskDeploymentConfiguration.getCallback().getService().getPort();
    }

    @Override
    public TPriorityExpr getPriorityExpression() {
        return task.getPriority();
    }

    @Override
    public TPresentationElements getPresentationElements() {
        return task.getPresentationElements();
    }

    /**
     * Deadline configuration of task.
     *
     * @return The task deadlines.
     */
    @Override
    public TDeadlines getDeadlines() {
            return task.getDeadlines();
    }

    /**
     * Specified Deadline configuration of task.
     *
     * @param name Name of the deadline
     * @return The task deadlines.
     */
    public TDeadline getDeadline(String name) {
            TDeadlines deadlines = getDeadlines();
        for (TDeadline deadline : deadlines.getStartDeadlineArray()) {
            if (deadline.getName().equals(name)) {
                return deadline;
            }
        }
        for (TDeadline deadline : deadlines.getCompletionDeadlineArray()) {
            if (deadline.getName().equals(name)) {
                return deadline;
            }
        }
        return null;
    }

    public CallBackService getCallBackService() {
        return callBackService;
    }

    @Override
    public ConfigurationType getConfigurationType() {
        return ConfigurationType.TASK;
    }

    public void setCallBackService(CallBackService callBackService) {
        this.callBackService = callBackService;
    }
}
