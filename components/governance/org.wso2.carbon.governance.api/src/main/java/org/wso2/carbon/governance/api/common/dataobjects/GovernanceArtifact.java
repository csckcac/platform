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
package org.wso2.carbon.governance.api.common.dataobjects;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMText;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.util.GovernanceConstants;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.Association;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import javax.xml.namespace.QName;
import java.util.*;

/**
 * Governance Artifact abstract class, This is overwritten by Endpoint, Policy, Schema, Service,
 * WSDL, People classes. This keeps common methods shared by all the governance artifacts
 */
@SuppressWarnings("unused")
public abstract class GovernanceArtifact {

    private static final Log log = LogFactory.getLog(GovernanceArtifact.class);

    private String id;
    private String path;
    private Registry registry; // associated registry

    /**
     * Map of attributes associated with this governance artifact.
     */
    protected Map<String, List<String>> attributes = new HashMap<String, List<String>>();

    /**
     * Construct a governance artifact object from the path and the id.
     *
     * @param id   the id
     */
    public GovernanceArtifact(String id) {
        this.id = id;
    }

    /**
     * Construct a governance artifact. The default constructor.
     */
    public GovernanceArtifact() {
        // the default constructor
    }

    /**
     * Copy constructor used for cloning.
     *
     * @param artifact the object to be copied.
     */
    protected GovernanceArtifact(GovernanceArtifact artifact) {
        this.attributes = artifact.attributes;
        try {
            associateRegistry(artifact.getAssociatedRegistry());
        } catch (GovernanceException ignored) {
        }
        setId(artifact.getId());
    }

    /**
     * Constructor accepting resource identifier and the XML content.
     *
     * @param id             the resource identifier.
     * @param contentElement an XML element containing the content.
     *
     * @throws GovernanceException if the construction fails.
     */
    public GovernanceArtifact(String id, OMElement contentElement) throws GovernanceException {
        this(id);
        serializeToAttributes(contentElement, null);
    }

    // Method to serialize attributes.
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

    public static GovernanceArtifact create(final Registry registry, final String artifactId)
            throws GovernanceException {
        return new GovernanceArtifact(artifactId) {
            {
                associateRegistry(registry);
            }

            public QName getQName() {
                return null;
            }
        };
    }

    public static GovernanceArtifact create(final Registry registry, final String artifactId,
                                            final OMElement content) throws GovernanceException {
        return new GovernanceArtifact(artifactId, content) {
            {
                associateRegistry(registry);
            }

            public QName getQName() {
                return null;
            }
        };
    }

    /**
     * Returns the QName of the artifact.
     *
     * @return the QName of the artifact
     */
    public abstract QName getQName();

    /**
     * Returns the id of the artifact
     *
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Set the id
     *
     * @param id the id
     */
    public void setId(String id) {
        this.id = id;
    }

    
    /**
     * Returns the path of the artifact, need to save the artifact before
     * getting the path.
     * 
     * @return here we return the path of the artifact.
     *
     * @throws GovernanceException if an error occurred.
     */
    public String getPath() throws GovernanceException {
        if (path == null) {
            path = GovernanceUtils.getArtifactPath(registry, id);
        }
        return path;
    }

    /**
     * Returns the name of the lifecycle associated with this artifact.
     *
     * @return the name of the lifecycle associated with this artifact.
     *
     * @throws GovernanceException if an error occurred.
     */
    public String getLifecycleName() throws GovernanceException {
        String path = getPath();
        if (path != null) {
            try {
                if (!registry.resourceExists(path)) {
                    String msg =
                            "The artifact is not added to the registry. Please add the artifact " +
                                    "before reading lifecycle information.";
                    log.error(msg);
                    throw new GovernanceException(msg);
                }
                return registry.get(path).getProperty("registry.LC.name");
            } catch (RegistryException e) {
                String msg = "Error in obtaining lifecycle name for the artifact. id: " + id +
                        ", path: " + path + ".";
                log.error(msg, e);
                throw new GovernanceException(msg, e);
            }
        }
        return null;
    }

    /**
     * Associates the named lifecycle with the artifact
     *
     * @param name the name of the lifecycle to be associated with this artifact.
     *
     * @throws GovernanceException if an error occurred.
     */
    public void attachLifecycle(String name) throws GovernanceException {
        String lifecycleName = getLifecycleName();
        try {
            if(name == null){
                GovernanceUtils.removeAspect(path, lifecycleName, registry);
                return;
            }
            if (!name.equals(lifecycleName)) {
                if (lifecycleName != null) {
                    GovernanceUtils.removeAspect(path, lifecycleName, registry);
                }
                registry.associateAspect(path, name);
            }
        } catch (RegistryException e) {
            String msg = "Error in associating lifecycle for the artifact. id: " + id +
                        ", path: " + path + ".";
                log.error(msg, e);
                throw new GovernanceException(msg, e);
        }

    }

    /**
     * Returns the state of the lifecycle associated with this artifact.
     *
     * @return the state of the lifecycle associated with this artifact.
     *
     * @throws GovernanceException if an error occurred.
     */
    public String getLifecycleState() throws GovernanceException {
        String path = getPath();
        if (path != null) {
            try {
                if (!registry.resourceExists(path)) {
                    String msg =
                            "The artifact is not added to the registry. Please add the artifact " +
                                    "before reading lifecycle information.";
                    log.error(msg);
                    throw new GovernanceException(msg);
                }
                Resource resource = registry.get(path);
                for (Object object : resource.getProperties().keySet()) {
                    String property = (String)object;
                    if (property.startsWith("registry.lifecycle.") && property.endsWith(".state")) {
                        return resource.getProperty(property);
                    }
                }
            } catch (RegistryException e) {
                String msg = "Error in obtaining lifecycle state for the artifact. id: " + id +
                        ", path: " + path + ".";
                log.error(msg, e);
                throw new GovernanceException(msg, e);
            }
        }
        return null;
    }
    
    /**
     * update the path after moving the resource.
     *
     * @throws GovernanceException if an error occurred.
     */
    public void updatePath() throws GovernanceException {
        path = GovernanceUtils.getArtifactPath(registry, id);
    }
    /**
     * update the path after moving the resource.
     *
     * @param artifactId
     * @throws GovernanceException if an error occurred.
     */
    public void updatePath(String artifactId) throws GovernanceException {
        path = GovernanceUtils.getArtifactPath(registry, artifactId);
    }

    /**
     * Create a version of the artifact.
     *
     * @throws GovernanceException throws if the operation failed.
     */
    public void createVersion() throws GovernanceException {
        checkRegistryResourceAssociation();
        try {
            if (!registry.resourceExists(path)) {
                String msg =
                        "The artifact is not added to the registry. Please add the artifact " +
                                "before creating versions.";
                log.error(msg);
                throw new GovernanceException(msg);
            }
            registry.createVersion(path);
        } catch (RegistryException e) {
            String msg = "Error in creating a version for the artifact. id: " + id +
                    ", path: " + path + ".";
            log.error(msg, e);
            throw new GovernanceException(msg, e);
        }
    }

    /**
     * Associate a registry, this is mostly used by the artifact manager when creating the
     * artifact.
     *
     * @param registry the registry.
     *
     * @throws GovernanceException throws if the operation failed.
     */
    public void associateRegistry(Registry registry) throws GovernanceException {
        this.registry = registry;
    }

    /**
     * Adding an attribute to the artifact. The artifact should be saved to get effect the change.
     *
     * @param key   the key.
     * @param value the value.
     *
     * @throws GovernanceException throws if the operation failed.
     */
    public void addAttribute(String key, String value) throws GovernanceException {
        List<String> values = attributes.get(key);
        if (values == null) {
            values = new ArrayList<String>();
            attributes.put(key, values);
        }
        values.add(value);
    }

    /**
     * Set/Update an attribute with multiple values. The artifact should be saved to get effect the
     * change.
     *
     * @param key       the key
     * @param newValues the value
     *
     * @throws GovernanceException throws if the operation failed.
     */
    public void setAttributes(String key, String[] newValues) throws GovernanceException {
        List<String> values = new ArrayList<String>();
        values.addAll(Arrays.asList(newValues));
        attributes.put(key, values);
    }

    /**
     * Set/Update an attribute with a single value. The artifact should be saved to get effect the
     * change.
     *
     * @param key      the key
     * @param newValue the value
     *
     * @throws GovernanceException throws if the operation failed.
     */
    public void setAttribute(String key, String newValue) throws GovernanceException {
        List<String> values = new ArrayList<String>();
        values.add(newValue);
        attributes.put(key, values);
    }

    /**
     * Returns the attribute of a given key.
     *
     * @param key the key
     *
     * @return the value of the attribute, if there are more than one attribute for the key this
     *         returns the first value.
     * @throws GovernanceException throws if the operation failed.
     */
    public String getAttribute(String key) throws GovernanceException {
        List<String> values = attributes.get(key);
        if (values == null || values.size() == 0) {
            return null;
        }
        return values.get(0);
    }

    /**
     * Returns the available attribute keys
     *
     * @return an array of attribute keys.
     * @throws GovernanceException throws if the operation failed.
     */
    public String[] getAttributeKeys() throws GovernanceException {
        Set<String> attributeKeys = attributes.keySet();
        if (attributeKeys == null) {
            return null;
        }
        return attributeKeys.toArray(new String[attributeKeys.size()]);
    }

    /**
     * Returns the attribute values for a key.
     *
     * @param key the key.
     *
     * @return attribute values for the key.
     * @throws GovernanceException throws if the operation failed.
     */
    public String[] getAttributes(String key) throws GovernanceException {
        List<String> values = attributes.get(key);
        if (values == null) {
            return null; //TODO: This should return String[0]
        }
        return values.toArray(new String[values.size()]);
    }

    /**
     * Remove attribute with the given key. The artifact should be saved to get effect the change.
     *
     * @param key the key
     *
     * @throws GovernanceException throws if the operation failed.
     */
    public void removeAttribute(String key) throws GovernanceException {
        attributes.remove(key);
    }

    /**
     * Get dependencies of an artifacts. The artifacts should be saved, before calling this method.
     *
     * @return the array of depending artifacts.
     * @throws GovernanceException throws if the operation failed.
     */
    public GovernanceArtifact[] getDependencies() throws GovernanceException {
        checkRegistryResourceAssociation();
        // uses the path from the getter to make sure the used overloaded method
        String path = getPath();
        List<GovernanceArtifact> governanceArtifacts = new ArrayList<GovernanceArtifact>();
        try {
            Association[] associations =
                    registry.getAssociations(path, GovernanceConstants.DEPENDS);
            for (Association association : associations) {
                String destinationPath = association.getDestinationPath();
                GovernanceArtifact governanceArtifact =
                        GovernanceUtils.retrieveGovernanceArtifactByPath(registry, destinationPath);
                governanceArtifacts.add(governanceArtifact);
            }
        } catch (RegistryException e) {
            String msg = "Error in getting dependencies from the artifact. id: " + id +
                    ", path: " + path + ".";
            log.error(msg, e);
            throw new GovernanceException(msg, e);
        }
        return governanceArtifacts.toArray(new GovernanceArtifact[governanceArtifacts.size()]);
    }

    /**
     * Get dependents of an artifact. The artifacts should be saved, before calling this method.
     *
     * @return the array of dependents.
     * @throws GovernanceException throws if the operation failed.
     */
    public GovernanceArtifact[] getDependents() throws GovernanceException {
        checkRegistryResourceAssociation();
        // uses the path from the getter to make sure the used overloaded method
        String path = getPath();
        List<GovernanceArtifact> governanceArtifacts = new ArrayList<GovernanceArtifact>();
        try {
            Association[] associations =
                    registry.getAssociations(path, GovernanceConstants.USED_BY);
            for (Association association : associations) {
                String destinationPath = association.getDestinationPath();
                GovernanceArtifact governanceArtifact =
                        GovernanceUtils.retrieveGovernanceArtifactByPath(registry, destinationPath);
                governanceArtifacts.add(governanceArtifact);
            }
        } catch (RegistryException e) {
            String msg = "Error in getting dependents from the artifact. id: " + id +
                    ", path: " + path + ".";
            log.error(msg, e);
            throw new GovernanceException(msg, e);
        }
        return governanceArtifacts.toArray(new GovernanceArtifact[governanceArtifacts.size()]);
    }

    /**
     * Attach the current artifact to an another artifact. Both the artifacts should be saved,
     * before calling this method. This method will two generic artifact types. There are specific
     * methods
     *
     * @param attachedToArtifact the artifact the current artifact is attached to
     *
     * @throws GovernanceException throws if the operation failed.
     */
    protected void attach(GovernanceArtifact attachedToArtifact) throws GovernanceException {
        checkRegistryResourceAssociation();
        // uses the path from the getter to make sure the used overloaded method
        String path = getPath();
        String attachedToArtifactPath = attachedToArtifact.getPath();
        if (attachedToArtifactPath == null) {
            String msg = "'Attached to artifact' is not associated with a registry path.";
            log.error(msg);
            throw new GovernanceException(msg);
        }
        try {
            registry.addAssociation(path, attachedToArtifactPath, GovernanceConstants.DEPENDS);
            registry.addAssociation(attachedToArtifactPath, path, GovernanceConstants.USED_BY);
        } catch (RegistryException e) {
            String msg = "Error in attaching the artifact. source id: " + id + ", path: " + path +
                    ", target id: " + attachedToArtifact.getId() + ", path:" +
                    attachedToArtifactPath +
                    ", attachment type: " + attachedToArtifact.getClass().getName() + ".";
            log.error(msg, e);
            throw new GovernanceException(msg, e);
        }
    }


    /**
     * Detach the current artifact from the provided artifact. Both the artifacts should be saved,
     * before calling this method.
     *
     * @param artifactId the artifact id of the attached artifact
     *
     * @throws GovernanceException throws if the operation failed.
     */
    protected void detach(String artifactId) throws GovernanceException {
        checkRegistryResourceAssociation();
        // uses the path from the getter to make sure the used overloaded method
        String path = getPath();
        String artifactPath = GovernanceUtils.getArtifactPath(registry, artifactId);
        if (artifactPath == null) {
            String msg = "Attached to artifact is not associated with a registry path.";
            log.error(msg);
            throw new GovernanceException(msg);
        }
        try {
            registry.removeAssociation(path, artifactPath, GovernanceConstants.DEPENDS);
            registry.removeAssociation(artifactPath, path, GovernanceConstants.USED_BY);
        } catch (RegistryException e) {
            String msg = "Error in detaching the artifact. source id: " + id + ", path: " + path +
                    ", target id: " + artifactId +
                    ", target path:" + artifactPath + ".";
            log.error(msg, e);
            throw new GovernanceException(msg, e);
        }
    }

    /**
     * Validate the resource is associated with a registry
     *
     * @throws GovernanceException if the resource is not associated with a registry.
     */
    protected void checkRegistryResourceAssociation() throws GovernanceException {
        // uses the path from the getter to make sure the used overloaded method
        String path = getPath();
        if (registry == null) {
            String msg = "A registry is not associated with the artifact.";
            log.error(msg);
            throw new GovernanceException(msg);
        }
        if (path == null) {
            String msg = "A path is not associated with the artifact.";
            log.error(msg);
            throw new GovernanceException(msg);
        }
        if (id == null) {
            String msg = "An id is not associated with the artifact.";
            log.error(msg);
            throw new GovernanceException(msg);
        }
    }

    /**
     * Returns the associated registry to the artifact.
     *
     * @return the associated registry
     */
    protected Registry getAssociatedRegistry() {
        return registry;
    }

}
