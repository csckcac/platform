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

package org.wso2.carbon.humantask.core.deployment;

import org.wso2.carbon.humantask.HumanInteractionsDocument;
import org.wso2.carbon.humantask.core.deployment.config.HTDeploymentConfigDocument;

import javax.wsdl.Definition;
import java.net.URI;
import java.util.List;

public abstract class HumanTaskDeploymentUnitBuilder {
    protected HumanTaskDeploymentUnit htDeploymentUnit;

    public HumanTaskDeploymentUnit getHumanTaskDeploymentUnit() {

        return htDeploymentUnit;
    }

    public HumanTaskDeploymentUnit createNewHumanTaskDeploymentUnit()
            throws HumanTaskDeploymentException {
        if (isValidHumanTaskDeploymentUnit()) {
            throw new IllegalStateException("Please build the Human Task Deployment unit completely.");
        }
        htDeploymentUnit = new HumanTaskDeploymentUnit();
        htDeploymentUnit.setHumanInteractionsDefinition(getHumanInteractionsDocument());
        htDeploymentUnit.setDeploymentConfiguration(getHTDeploymentConfigDocument());
        htDeploymentUnit.setWSDLs(getWsdlDefinitions());
        htDeploymentUnit.setName(getArchiveName());
        //htDeploymentUnit.setUIResourceProviderURI(getUIResourceProviderURI());

        return htDeploymentUnit;
    }

    private boolean isValidHumanTaskDeploymentUnit() {
        return htDeploymentUnit != null &&
                htDeploymentUnit.getHumanInteractionsDefinition() != null &&
                htDeploymentUnit.getWSDLs() != null &&
                htDeploymentUnit.getName() != null &&
                htDeploymentUnit.getDeploymentConfiguration() != null;
    }

    public abstract void buildHumanInteractionDocuments() throws HumanTaskDeploymentException;

    public abstract void buildDeploymentConfiguration() throws HumanTaskDeploymentException;

    public abstract void buildWSDLs() throws HumanTaskDeploymentException;

    public abstract void buildSchemas() throws HumanTaskDeploymentException;

    public abstract HumanInteractionsDocument getHumanInteractionsDocument()
            throws HumanTaskDeploymentException;

    public abstract HTDeploymentConfigDocument getHTDeploymentConfigDocument()
            throws HumanTaskDeploymentException;

    public abstract String getArchiveName();

    public abstract List<Definition> getWsdlDefinitions() throws HumanTaskDeploymentException;
}
