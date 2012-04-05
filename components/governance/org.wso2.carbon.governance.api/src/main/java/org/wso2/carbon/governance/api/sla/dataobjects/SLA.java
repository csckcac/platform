/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.governance.api.sla.dataobjects;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMText;
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.registry.extensions.utils.CommonUtil;

import javax.xml.namespace.QName;
import java.util.Iterator;


/**
 * This represents a sla artifact stored on the Registry. SLA artifacts are created as a
 * result of adding a new sla or uploading or importing a WSDL file into the registry.
 */
public class SLA extends GovernanceArtifact {

    private QName qName;

    /**
     * Copy constructor used for cloning.
     *
     * @param sla the object to be copied.
     */
    protected SLA(SLA sla) {
        this.qName = sla.qName;
        this.attributes = sla.attributes;
        try {
            associateRegistry(sla.getAssociatedRegistry());
        } catch (GovernanceException ignored) {
        }
        setId(sla.getId());
    }

    /**
     * Constructor accepting resource identifier and the qualified name.
     *
     * @param id    the resource identifier.
     * @param qName the qualified name.
     */
    public SLA(String id, QName qName) {
        super(id);
        this.qName = qName;
    }

    /**
     * Constructor accepting resource identifier and the sla content.
     *
     * @param id                    the resource identifier.
     * @param slaContentElement an XML element containing the sla content.
     *
     * @throws GovernanceException if the construction fails.
     */
    public SLA(String id, OMElement slaContentElement) throws GovernanceException {
        super(id);
        serializeToAttributes(slaContentElement, null);

        String slaName = CommonUtil.getServiceName(slaContentElement);
        if (slaName != null && !slaName.equals("")) {
            this.qName = new QName(slaName);
        }
    }

    public QName getQName() {
        return qName;
    }

    /**
     * Method to set the qualified name of this sla artifact.
     *
     * @param qName the qualified name.
     *
     * @throws GovernanceException if the operation failed.
     */
    public void setQName(QName qName) throws GovernanceException {
        // the path will be synced with the qualified name
        this.qName = qName;
    }

    // Method to serialize sla attributes.
    private void serializeToAttributes(OMElement contentElement, String parentAttributeName)
            throws GovernanceException {
        Iterator childIt = contentElement.getChildren();
        while (childIt.hasNext()) {
            Object childObj = childIt.next();
            if (childObj instanceof OMElement) {
                OMElement childElement = (OMElement) childObj;
                String elementName = childElement.getLocalName();
                String attributeName =
                        (parentAttributeName == null ? "" : parentAttributeName + "_") +
                                elementName;
                serializeToAttributes(childElement, attributeName);
            } else if (childObj instanceof OMText) {
                OMText childText = (OMText) childObj;
                if (childText.getNextOMSibling() == null &&
                        childText.getPreviousOMSibling() == null) {
                    // if it is only child, we consider it is a value.
                    String textValue = childText.getText();
                    addAttribute(parentAttributeName, textValue);
                }
            }
        }
    }
}
