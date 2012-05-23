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
package org.wso2.carbon.governance.api.sla;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMText;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.sla.dataobjects.SLA;
import org.wso2.carbon.governance.api.util.GovernanceConstants;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.utils.RegistryUtils;

import javax.xml.namespace.QName;
import java.util.*;

/**
 * This provides the management functionality for sla artifacts stored on the registry.
 */
public class SLAManager {

    private static final Log log = LogFactory.getLog(SLAManager.class);
    private Registry registry;

    /**
     * Constructor accepting an instance of the registry to use.
     *
     * @param registry the instance of the registry.
     */
    public SLAManager(Registry registry) {
        this.registry = registry;
    }

    /**
     * Creates a new sla artifact from the given qualified name.
     *
     * @param qName the qualified name of this sla.
     *
     * @return the artifact added.
     * @throws GovernanceException if the operation failed.
     */
    public SLA newSLA(QName qName) throws GovernanceException {
        String slaId = UUID.randomUUID().toString();
        SLA sla = new SLA(slaId, qName);
        sla.associateRegistry(registry);
        return sla;
    }

    /**
     * Creates a new sla artifact from the given content.
     *
     * @param content the sla content.
     *
     * @return the artifact added.
     * @throws GovernanceException if the operation failed.
     */
    public SLA newSLA(OMElement content) throws GovernanceException {
        String slaId = UUID.randomUUID().toString();
        SLA sla = new SLA(slaId, content);
        sla.associateRegistry(registry);
        return sla;
    }

    /**
     * Adds the given sla artifact to the registry.
     *
     * @param sla the sla artifact.
     *
     * @throws GovernanceException if the operation failed.
     */
    public void addSLA(SLA sla) throws GovernanceException {
        if (sla.getQName() == null) {
            String msg = "Name is not set. It have to be set as an qname.";
            log.error(msg);
            throw new GovernanceException(msg);
        }
        String slaName = sla.getQName().getLocalPart();
        sla.setAttributes(GovernanceConstants.SERVICE_NAME_ATTRIBUTE,
                new String[]{slaName});

        sla.associateRegistry(registry);
        boolean succeeded = false;
        try {
            registry.beginTransaction();
            String slaId = sla.getId();
            Resource slaResource = registry.newResource();
            slaResource.setMediaType(GovernanceConstants.SLA_MEDIA_TYPE);
            setContent(sla, slaResource);
            slaResource.setUUID(slaId);
            // the sla will not actually stored in the tmp path.
            String tmpPath = "/" + slaName;
            registry.put(tmpPath, slaResource);
            succeeded = true;
        } catch (RegistryException e) {
            String msg = "Failed to add artifact: artifact id: " + sla.getId() +
                    ", path: " + sla.getPath() + ".";
            log.error(msg, e);
            throw new GovernanceException(msg, e);
        } finally {
            if (succeeded) {
                try {
                    registry.commitTransaction();
                } catch (RegistryException e) {
                    String msg =
                            "Error in committing transactions. Failed to add artifact: artifact " +
                                    "id: " + sla.getId() + ", path: " + sla.getPath() + ".";
                    log.error(msg, e);
                }
            } else {
                try {
                    registry.rollbackTransaction();
                } catch (RegistryException e) {
                    String msg =
                            "Error in rolling back transactions. Failed to add artifact: " +
                                    "artifact id: " + sla.getId() + ", path: " +
                                    sla.getPath() + ".";
                    log.error(msg, e);
                }
            }
        }
    }

    /**
     * Updates the given sla artifact on the registry.
     *
     * @param sla the sla artifact.
     *
     * @throws GovernanceException if the operation failed.
     */
    public void updateSLA(SLA sla) throws GovernanceException {
        boolean succeeded = false;
        try {
            registry.beginTransaction();
            SLA oldSLA = getSLA(sla.getId());
            // first check for the old sla and remove it.
            if (oldSLA != null) {
                QName oldQname = oldSLA.getQName();
                if (!oldQname.equals(sla.getQName())) {
                    // then it is analogue to moving the resource for the new location
                    String oldPath = oldSLA.getPath();
                    // so just delete the old path
                    registry.delete(oldPath);
                }
            }
            addSLA(sla);
            sla.updatePath();
            succeeded = true;
        } catch (RegistryException e) {
            String msg = "Error in updating the artifact, artifact id: " + sla.getId() +
                    ", artifact path: " + sla.getPath() + ".";
            log.error(msg, e);
            throw new GovernanceException(msg, e);
        } finally {
            if (succeeded) {
                try {
                    registry.commitTransaction();
                } catch (RegistryException e) {
                    String msg =
                            "Error in committing transactions. Update artifact failed: artifact " +
                                    "id: " + sla.getId() + ", path: " + sla.getPath() + ".";
                    log.error(msg, e);
                }
            } else {
                try {
                    registry.rollbackTransaction();
                } catch (RegistryException e) {
                    String msg =
                            "Error in rolling back transactions. Update artifact failed: " +
                                    "artifact id: " + sla.getId() + ", path: " +
                                    sla.getPath() + ".";
                    log.error(msg, e);
                }
            }
        }
    }

    /**
     * Fetches the given sla artifact on the registry.
     *
     * @param slaId the identifier of the sla artifact.
     *
     * @return the sla artifact.
     * @throws GovernanceException if the operation failed.
     */
    public SLA getSLA(String slaId) throws GovernanceException {
        GovernanceArtifact artifact =
                GovernanceUtils.retrieveGovernanceArtifactById(registry, slaId);
        if (artifact != null && !(artifact instanceof SLA)) {
            String msg = "The artifact request is not an SLA. id: " + slaId + ".";
            log.error(msg);
            throw new GovernanceException(msg);
        }
        return (SLA) artifact;
    }

    /**
     * Removes the given sla artifact from the registry.
     *
     * @param slaId the identifier of the sla artifact.
     *
     * @throws GovernanceException if the operation failed.
     */
    public void removeSLA(String slaId) throws GovernanceException {
        GovernanceUtils.removeArtifact(registry, slaId);
    }

    /**
     * Sets content of the given sla artifact to the given resource on the registry.
     *
     * @param sla         the sla artifact.
     * @param slaResource the content resource.
     *
     * @throws GovernanceException if the operation failed.
     */
    protected void setContent(SLA sla, Resource slaResource) throws
            GovernanceException {
        try {
            OMNamespace namespace = OMAbstractFactory.getOMFactory().
                    createOMNamespace(GovernanceConstants.SERVICE_ELEMENT_NAMESPACE, "");
            Map<String, OMElement> elementsMap = new HashMap<String, OMElement>();
            String[] attributeKeys = sla.getAttributeKeys();
            if (attributeKeys != null) {
                OMElement contentElement =
                        OMAbstractFactory.getOMFactory().createOMElement(
                                GovernanceConstants.SERVICE_ELEMENT_ROOT, namespace);
                for (String aggregatedKey : attributeKeys) {
                    String[] keys = aggregatedKey.split("_");
                    String elementsMapKey = null;
                    OMElement parentKeyElement = contentElement;
                    for (int i = 0; i < keys.length - 1; i++) {
                        String key = keys[i];
                        elementsMapKey = (elementsMapKey == null ? "" : elementsMapKey + "_") + key;
                        OMElement keyElement = elementsMap.get(elementsMapKey);
                        if (keyElement == null) {
                            keyElement = OMAbstractFactory.getOMFactory()
                                    .createOMElement(key, namespace);
                            parentKeyElement.addChild(keyElement);
                            elementsMap.put(elementsMapKey, keyElement);
                        }
                        parentKeyElement = keyElement;
                    }
                    String[] attributeValues = sla.getAttributes(aggregatedKey);
                    String elementName = keys[keys.length - 1];
                    for (String value : attributeValues) {
                        OMElement keyElement = OMAbstractFactory.getOMFactory()
                                .createOMElement(elementName, namespace);
                        OMText textElement = OMAbstractFactory.getOMFactory().createOMText(value);
                        keyElement.addChild(textElement);
                        parentKeyElement.addChild(keyElement);
                    }
                }
                String updatedContent = GovernanceUtils.serializeOMElement(contentElement);
                slaResource.setContent(updatedContent);
            }
        } catch (RegistryException e) {
            String msg = "Error in saving attributes for the artifact. id: " + sla.getId() +
                    ", path: " + sla.getPath() + ".";
            log.error(msg, e);
            throw new GovernanceException(msg, e);
        }
    }

    /**
     * Finds all sla artifacts matching the given filter criteria.
     *
     * @param criteria the filter criteria to be matched.
     *
     * @return the sla artifacts that match.
     * @throws GovernanceException if the operation failed.
     */
    public SLA[] findSLA(SLAFilter criteria) throws GovernanceException {
        List<SLA> slas = new ArrayList<SLA>();
        for (SLA sla : getAllSLA()) {
            if (sla != null) {
                if (criteria.matches(sla)) {
                    slas.add(sla);
                }
            }
        }
        return slas.toArray(new SLA[slas.size()]);
    }

    /**
     * Finds all sla artifacts on the registry.
     *
     * @return all sla artifacts on the registry.
     * @throws GovernanceException if the operation failed.
     */
    public SLA[] getAllSLA() throws GovernanceException {
        List<String> slaPaths =
                Arrays.asList(GovernanceUtils.getResultPaths(registry,
                        GovernanceConstants.SLA_MEDIA_TYPE));
        Collections.sort(slaPaths, new Comparator<String>() {
            public int compare(String o1, String o2) {
                String[] version1 = new String[3];
                String[] version2 = new String[3];
                String temp1 = RegistryUtils.getParentPath(o1);
                String temp2 = RegistryUtils.getParentPath(o2);
                for (int j = 0; j < 3; j++) {
                    version1[j] = RegistryUtils.getResourceName(temp1);
                    version2[j] = RegistryUtils.getResourceName(temp2);
                    temp1 = RegistryUtils.getParentPath(temp1);
                    temp2 = RegistryUtils.getParentPath(temp2);
                }
                // First order by name
                int result = RegistryUtils.getResourceName(temp1).compareToIgnoreCase(
                        RegistryUtils.getResourceName(temp2));
                if (result != 0) {
                    return result;
                }
                // Finally by version
                long l1 = -1;
                long l2 = -1;
                int j = 3;
                while (l1 == l2) {
                    if (j-- == 0) {
                        return 0;
                    }
                    l1 = Long.parseLong(version1[j]);
                    l2 = Long.parseLong(version2[j]);
                }
                return (l1 > l2) ? -1 : 1;
            }
        });
        List<SLA> sla = new ArrayList<SLA>();
        for (String slaPath : slaPaths) {
            GovernanceArtifact artifact =
                    GovernanceUtils.retrieveGovernanceArtifactByPath(registry, slaPath);
            sla.add((SLA) artifact);
        }
        return sla.toArray(new SLA[sla.size()]);
    }

    /**
     * Finds all identifiers of the sla artifacts on the registry.
     *
     * @return an array of identifiers of the sla artifacts.
     * @throws GovernanceException if the operation failed.
     */
    public String[] getAllSLAIds() throws GovernanceException {
        List<String> slaIds = new ArrayList<String>();
        for (SLA sla : getAllSLA()) {
            slaIds.add(sla.getId());
        }
        return slaIds.toArray(new String[slaIds.size()]);
    }
}
