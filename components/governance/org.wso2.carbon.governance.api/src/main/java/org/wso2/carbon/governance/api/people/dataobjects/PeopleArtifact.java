package org.wso2.carbon.governance.api.people.dataobjects;

import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
import org.wso2.carbon.governance.api.exception.GovernanceException;

/**
 * This is the base class for People artifacts stored in the Registry. This can is extended by
 * <code>Organization</code>, <code>Department</code>, <code>ProjectGroup</code> and
 * <code>Person</code> classes
 */
public abstract class PeopleArtifact extends GovernanceArtifact {

/*
    //TODO: some codes are duplicated from Service class
    private static final Log log = LogFactory.getLog(PeopleArtifact.class);

    private QName qName;

    */
/**
     * Constructor accepting resource identifier and the qualified name.
     *
     * @param id    the resource identifier.
     * @param name the name.
     * @param type the type.
     *//*

    public PeopleArtifact(String id, String name, String type) {
        super(id);
        this.qName = new QName(name);
        try {
            this.setAttribute(GovernanceConstants.PEOPLE_TYPE_ATTRIBUTE, type);
        } catch (GovernanceException ignored) {
           // This is unreachable, hence ignored.
        }
    }

    */
/**
     * Constructor accepting resource identifier and the service content.
     *
     * @param id                    the resource identifier.
     * @param artifactContentElem an XML element containing the service content.
     *
     * @throws org.wso2.carbon.governance.api.exception.GovernanceException if the construction fails.
     *//*

    public PeopleArtifact(String id, OMElement artifactContentElem) throws GovernanceException {
        super(id);
        serializeToAttributes(artifactContentElem, null);

        String name = CommonUtil.getServiceName(artifactContentElem);
        if (name != null && !name.equals("")) {
            this.qName = new QName(name);
        }
    }

    public QName getQName() {
        return qName;
    }

    */
/**
     * Method to get the name of this people artifact.
     *//*

    public String getName() {
        return getQName().getLocalPart();
    }

    */
/**
     * Method to get the type of this people artifact. (consumer, provider, or internal)
     *
     * @throws GovernanceException if the operation failed.
     *//*

    public String getType() throws GovernanceException {
        return getAttribute(GovernanceConstants.PEOPLE_TYPE_ATTRIBUTE);
    }

    */
/**
     * Method to set the qualified name of this service artifact.
     *
     * @param qName the qualified name.
     *
     * @throws GovernanceException if the operation failed.
     *//*

    public void setQName(QName qName) throws GovernanceException {
        // the path will be synced with the qualified name
        this.qName = qName;
    }

    */
/**
     * Attach a sla artifact to a service artifact. Both the artifacts should be saved, before
     * calling this method.
     *
     * @param sla the sla to attach.
     *
     * @throws GovernanceException if the operation failed.
     *//*

    public void attachSLA(GovernanceArtifact sla) throws GovernanceException {
        attach(sla);
    }

    */
/**
     * Detach a slaId artifact from a service artifact. Both the artifacts should be saved, before
     * calling this method.
     *
     * @param slaId the identifier of the slaId to detach.
     *
     * @throws GovernanceException if the operation failed.
     *//*

    @SuppressWarnings("unused")
    public void detachSLA(String slaId) throws GovernanceException {
        detach(slaId);
    }

    */
/**
     * Attach a policy artifact to a service artifact. Both the artifacts should be saved, before
     * calling this method.
     *
     * @param policy the policy to attach.
     *
     * @throws GovernanceException if the operation failed.
     *//*

    public void attachPolicy(Policy policy) throws GovernanceException {
        attach(policy);
    }

    */
/**
     * Detach a policy artifact from a service artifact. Both the artifacts should be saved, before
     * calling this method.
     *
     * @param policyId the identifier of the policy to detach.
     *
     * @throws GovernanceException if the operation failed.
     *//*

    @SuppressWarnings("unused")
    public void detachPolicy(String policyId) throws GovernanceException {
        detach(policyId);
    }

    */
/**
     * Method to retrieve all SLAs attached to this service artifact.
     *
     * @return all SLAs attached to this service artifact.
     * @throws GovernanceException if the operation failed.
     *//*

    public GovernanceArtifact[] getAttachedSLAs() throws GovernanceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    */
/**
     * Method to retrieve all policies attached to this service artifact.
     *
     * @return all policies attached to this service artifact.
     * @throws GovernanceException if the operation failed.
     *//*

    public Policy[] getAttachedPolicies() throws GovernanceException {
        checkRegistryResourceAssociation();
        Registry registry = getAssociatedRegistry();
        String path = getPath();
        List<Policy> policies = new ArrayList<Policy>();
        try {
            Association[] associations =
                    registry.getAssociations(path, GovernanceConstants.DEPENDS);
            for (Association association : associations) {
                String destinationPath = association.getDestinationPath();
                GovernanceArtifact governanceArtifact =
                        GovernanceUtils.retrieveGovernanceArtifactByPath(registry, destinationPath);
                if (governanceArtifact instanceof Policy) {
                    policies.add((Policy) governanceArtifact);
                }
            }
        } catch (RegistryException e) {
            String msg =
                    "Error in getting attached policies from the artifact at path: " + path + ".";
            log.error(msg, e);
            throw new GovernanceException(msg, e);
        }
        return policies.toArray(new Policy[policies.size()]);
    }

    // Method to serialize service attributes.
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
*/

}
