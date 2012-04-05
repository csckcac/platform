/*
 *  Copyright (c) 2005-2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.governance.api.generic.dataobjects;

import org.apache.axiom.om.OMElement;
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.util.GovernanceUtils;

import javax.xml.namespace.QName;

/**
 * Represents a generic governance artifact.
 */
public class GenericArtifact extends GovernanceArtifact {

    private QName qName;

    /**
     * Copy constructor used for cloning.
     *
     * @param artifact the object to be copied.
     */
    protected GenericArtifact(GovernanceArtifact artifact) {
        super(artifact);
        this.qName = artifact.getQName();
    }

    /**
     * Constructor accepting resource identifier and the qualified name.
     *
     * @param id    the resource identifier.
     * @param qName the qualified name.
     */
    public GenericArtifact(String id, QName qName) {
        super(id);
        this.qName = qName;
    }

    /**
     * Constructor accepting resource identifier and the artifact content.
     *
     * @param id                         the resource identifier.
     * @param artifactContentElement     an XML element containing the content.
     * @param artifactNameAttribute      the attribute that specifies the name of the artifact.
     * @param artifactNamespaceAttribute the attribute that specifies the namespace of the artifact.
     * @param artifactElementNamespace   the attribute that specifies the artifact element's
     *                                   namespace.
     *
     * @throws GovernanceException if the construction fails.
     */
    public GenericArtifact(String id, OMElement artifactContentElement,
                           String artifactNameAttribute,
                           String artifactNamespaceAttribute,
                           String artifactElementNamespace) throws GovernanceException {
        super(id, artifactContentElement);
        String name = GovernanceUtils.getAttributeValue(artifactContentElement,
                artifactNameAttribute, artifactElementNamespace);
        String namespace = (artifactNamespaceAttribute != null) ?
                GovernanceUtils.getAttributeValue(artifactContentElement,
                artifactNamespaceAttribute, artifactElementNamespace) : null;
        if (name != null && !name.equals("")) {
            this.qName = new QName(namespace, name);
        }
    }

    public QName getQName() {
        return qName;
    }

    /**
     * Method to set the qualified name of this artifact.
     *
     * @param qName the qualified name.
     *
     * @throws GovernanceException if the operation failed.
     */
    public void setQName(QName qName) throws GovernanceException {
        // the path will be synced with the qualified name
        this.qName = qName;
    }

}
