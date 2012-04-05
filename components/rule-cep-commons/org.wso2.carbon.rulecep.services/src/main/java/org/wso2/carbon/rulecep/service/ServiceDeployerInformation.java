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
package org.wso2.carbon.rulecep.service;

import org.wso2.carbon.rulecep.commons.descriptions.service.ExtensionBuilder;

/**
 * Information the deplyer requires
 */
public class ServiceDeployerInformation {

    private String serviceType;
    private String servicePathKey;
    private String fileExtension;
    private String archiveExtension;
    private ExtensionBuilder extensionBuilder;
    private ServiceEngineFactory serviceEngineFactory;
    private MessageReceiverFactory messageReceiverFactory;
    private String serviceArchiveGeneratableKey;
    private boolean isServiceArchiveGeneratable;

    private DeploymentExtenstion deploymentExtenstion;

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getServicePathKey() {
        return servicePathKey;
    }

    public void setServicePathKey(String servicePathKey) {
        this.servicePathKey = servicePathKey;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public String getArchiveExtension() {
        return archiveExtension;
    }

    public void setArchiveExtension(String archiveExtension) {
        this.archiveExtension = archiveExtension;
    }


    public ServiceEngineFactory getServiceProvider() {
        return serviceEngineFactory;
    }

    public void setServiceProvider(ServiceEngineFactory serviceEngineFactory) {
        this.serviceEngineFactory = serviceEngineFactory;
    }

    public ExtensionBuilder getExtensionBuilder() {
        return extensionBuilder;
    }

    public void setExtensionBuilder(ExtensionBuilder extensionBuilder) {
        this.extensionBuilder = extensionBuilder;
    }

    public MessageReceiverFactory getMessageReceiverFactory() {
        return messageReceiverFactory;
    }

    public void setMessageReceiverFactory(MessageReceiverFactory messageReceiverFactory) {
        this.messageReceiverFactory = messageReceiverFactory;
    }

    public ServiceEngineFactory getServiceEngineFactory() {
        return serviceEngineFactory;
    }

    public void setServiceEngineFactory(ServiceEngineFactory serviceEngineFactory) {
        this.serviceEngineFactory = serviceEngineFactory;
    }

    public DeploymentExtenstion getDeploymentExtenstion() {
        return deploymentExtenstion;
    }

    public void setDeploymentExtenstion(DeploymentExtenstion deploymentExtenstion) {
        this.deploymentExtenstion = deploymentExtenstion;
    }

    public String getServiceArchiveGeneratableKey() {
        return serviceArchiveGeneratableKey;
    }

    public void setServiceArchiveGeneratableKey(String serviceArchiveGeneratableKey) {
        this.serviceArchiveGeneratableKey = serviceArchiveGeneratableKey;
    }

    public boolean isServiceArchiveGeneratable() {
        return isServiceArchiveGeneratable;
    }

    public void setServiceArchiveGeneratable(boolean serviceArchiveGeneratable) {
        isServiceArchiveGeneratable = serviceArchiveGeneratable;
    }
}
