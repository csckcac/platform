package org.wso2.carbon.governance.api.people;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMText;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
import org.wso2.carbon.governance.api.people.dataobjects.*;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.util.GovernanceConstants;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.utils.RegistryUtils;

import java.util.*;

/**
 * <code>PeopleManager</code> class is used to manage People artifacts stored in the Registry.
 * This class defines methods for creating, adding and retrieving People artifacts.
 */
public class PeopleManager {

/*
    private static final Log log = LogFactory.getLog(PeopleManager.class);
    private Registry registry;

    */
/**
     * Constructor accepting an instance of the registry to use.
     *
     * @param registry the instance of the registry.
     *//*

    public PeopleManager(Registry registry) {
        this.registry = registry;
    }

    */
/**
     * Creates a new organization artifact from the given qualified name.
     *
     * @param name the name of this organization.
     * @param type the type of this organization.
     *
     * @return the artifact added.
     * @throws org.wso2.carbon.governance.api.exception.GovernanceException if the operation failed.
     *//*

    public Organization newOrganization(String name, String type) throws GovernanceException {
        Organization organization = new Organization(UUID.randomUUID().toString(), name, type);
        organization.associateRegistry(registry);
        return organization;
    }

    */
/**
     * Creates a new department artifact from the given qualified name.
     *
     * @param name the name of this department.
     * @param type the type of this department.
     *
     * @return the artifact added.
     * @throws org.wso2.carbon.governance.api.exception.GovernanceException if the operation failed.
     *//*

    public Department newDepartment(String name, String type) throws GovernanceException {
        Department department = new Department(UUID.randomUUID().toString(), name, type);
        department.associateRegistry(registry);
        return department;
    }

    */
/**
     * Creates a new project group artifact from the given qualified name.
     *
     * @param name the name of this project group.
     * @param type the type of this project group.
     *
     * @return the artifact added.
     * @throws org.wso2.carbon.governance.api.exception.GovernanceException if the operation failed.
     *//*

    public ProjectGroup newProjectGroup(String name, String type) throws GovernanceException {
        ProjectGroup projectGroup = new ProjectGroup(UUID.randomUUID().toString(), name, type);
        projectGroup.associateRegistry(registry);
        return projectGroup;
    }

    */
/**
     * Creates a new person artifact from the given qualified name.
     *
     * @param name the name of this person.
     * @param type the type of this person.
     *
     * @return the artifact added.
     * @throws org.wso2.carbon.governance.api.exception.GovernanceException if the operation failed.
     *//*

    public Person newPerson(String name, String type) throws GovernanceException {
        Person person = new Person(UUID.randomUUID().toString(), name, type);
        person.associateRegistry(registry);
        return person;
    }

    */
/**
     * Creates a new organization artifact from the given content.
     *
     * @param content the artifact content.
     *
     * @return the artifact added.
     * @throws GovernanceException if the operation failed.
     *//*

    public Organization newOrganization(OMElement content) throws GovernanceException {
        Organization organization = new Organization(UUID.randomUUID().toString(), content);
        organization.associateRegistry(registry);
        return organization;
    }

    */
/**
     * Creates a new department artifact from the given content.
     *
     * @param content the artifact content.
     *
     * @return the artifact added.
     * @throws GovernanceException if the operation failed.
     *//*

    public Department newDepartment(OMElement content) throws GovernanceException {
        Department department = new Department(UUID.randomUUID().toString(), content);
        department.associateRegistry(registry);
        return department;
    }

    */
/**
     * Creates a new project group artifact from the given content.
     *
     * @param content the artifact content.
     *
     * @return the artifact added.
     * @throws GovernanceException if the operation failed.
     *//*

    public ProjectGroup newProjectGroup(OMElement content) throws GovernanceException {
        ProjectGroup projectGroup = new ProjectGroup(UUID.randomUUID().toString(), content);
        projectGroup.associateRegistry(registry);
        return projectGroup;
    }

    */
/**
     * Creates a new person artifact from the given content.
     *
     * @param content the artifact content.
     *
     * @return the artifact added.
     * @throws GovernanceException if the operation failed.
     *//*

    public Person newPerson(OMElement content) throws GovernanceException {
        Person person = new Person(UUID.randomUUID().toString(), content);
        person.associateRegistry(registry);
        return person;
    }

    */
/**
     * Adds the given peopleArtifact artifact to the registry.
     *
     * @param peopleArtifact the peopleArtifact artifact.
     *
     * @throws GovernanceException if the operation failed.
     *//*

    public void addPeopleArtifact(PeopleArtifact peopleArtifact) throws GovernanceException {
        // adding the attributes for name, namespace + peopleArtifact
        if (peopleArtifact.getQName() == null) {
            String msg = "PeopleArtifact name is not set. It has to be set as an qname.";
            log.error(msg);
            throw new GovernanceException(msg);
        }
        if (getPeopleArtifactByName(peopleArtifact.getName()) != null) {
            String msg = "A PeopleArtifact already exists with the same name, artifact name: " +
                    peopleArtifact.getName();
            log.error(msg);
            throw new GovernanceException(msg);
        }
        String artifactName = peopleArtifact.getName();
        peopleArtifact.setAttributes(GovernanceConstants.SERVICE_NAME_ATTRIBUTE,
                new String[]{artifactName});
        peopleArtifact.associateRegistry(registry);
        boolean succeeded = false;
        try {
            registry.beginTransaction();
            String artifactId = peopleArtifact.getId();
            Resource artifactRes = registry.newResource();
            artifactRes.setMediaType(GovernanceConstants.PEOPLE_MEDIA_TYPE);
            setContent(peopleArtifact, artifactRes);
            artifactRes.setProperty(GovernanceConstants.ARTIFACT_ID_PROP_KEY, artifactId);
            // the peopleArtifact will not actually stored in the tmp path.
            String tmpPath = artifactName;
            registry.put(tmpPath, artifactRes);
            if (peopleArtifact instanceof ProjectGroup && peopleArtifact.getAttributes(
                        ProjectGroup.SUB_GROUPS_ATTRIBUTE) != null) {
                for (String subGroupText : peopleArtifact.getAttributes(
                        ProjectGroup.SUB_GROUPS_ATTRIBUTE)) {
                    String subGroupName = subGroupText.split(
                            GovernanceConstants.ENTRY_VALUE_SEPARATOR)[1];
                    PeopleArtifact existingArtifact = getPeopleArtifactByName(subGroupName);
                    if (existingArtifact == null) {
                        addPeopleArtifact(new ProjectGroup((ProjectGroup)peopleArtifact,
                                            subGroupName));
                    } else if (!(existingArtifact instanceof ProjectGroup)) {
                        String msg = "Cannot add the subgroup as a different people artifact " +
                "exists in the same name. Existing artifact path: " + existingArtifact.getPath();
                        log.error(msg);
                        throw new GovernanceException(msg);
                    }
                }
                GovernanceUtils.writeSubGroupAssociations(registry, (ProjectGroup)peopleArtifact);
            }
            succeeded = true;
        } catch (RegistryException e) {
            String msg = "Add peopleArtifact failed: peopleArtifact id: " + peopleArtifact.getId() +
                    ", path: " + peopleArtifact.getPath() + ".";
            log.error(msg, e);
            throw new GovernanceException(msg, e);
        } finally {
            if (succeeded) {
                try {
                    registry.commitTransaction();
                } catch (RegistryException e) {
                    String msg =
                            "Error in committing transactions. Add peopleArtifact failed: peopleArtifact id: " +
                                    peopleArtifact.getId() + ", path: " + peopleArtifact.getPath() + ".";
                    log.error(msg, e);
                }
            } else {
                try {
                    registry.rollbackTransaction();
                } catch (RegistryException e) {
                    String msg =
                            "Error in rolling back transactions. Add peopleArtifact failed: peopleArtifact id: " +
                                    peopleArtifact.getId() + ", path: " + peopleArtifact.getPath() + ".";
                    log.error(msg, e);
                }
            }
        }
    }

    */
/**
     * Updates the given peopleArtifact artifact on the registry.
     *
     * @param peopleArtifact the peopleArtifact artifact.
     *
     * @throws GovernanceException if the operation failed.
     *//*

    public void updatePeopleArtifact(PeopleArtifact peopleArtifact) throws GovernanceException {
        boolean succeeded = false;
        try {
            registry.beginTransaction();
            GovernanceUtils.removeArtifact(registry, peopleArtifact.getId());
            addPeopleArtifact(peopleArtifact);
            peopleArtifact.updatePath();
            succeeded = true;
        } catch (RegistryException e) {
            String msg = "Error in updating the peopleArtifact, peopleArtifact id: " + peopleArtifact.getId() +
                    ", peopleArtifact path: " + peopleArtifact.getPath() + ".";
            log.error(msg, e);
            throw new GovernanceException(msg, e);
        } finally {
            if (succeeded) {
                try {
                    registry.commitTransaction();
                } catch (RegistryException e) {
                    String msg =
                            "Error in committing transactions. Add peopleArtifact failed: peopleArtifact id: " +
                                    peopleArtifact.getId() + ", path: " + peopleArtifact.getPath() + ".";
                    log.error(msg, e);
                }
            } else {
                try {
                    registry.rollbackTransaction();
                } catch (RegistryException e) {
                    String msg =
                            "Error in rolling back transactions. Add peopleArtifact failed: peopleArtifact id: " +
                                    peopleArtifact.getId() + ", path: " + peopleArtifact.getPath() + ".";
                    log.error(msg, e);
                }
            }
        }
    }

    */
/**
     * Fetches the given People artifact from the registry.
     *
     * @param artifactId the identifier of the People artifact.
     *
     * @return the People artifact.
     * @throws GovernanceException if the operation failed.
     *//*

    public PeopleArtifact getPeopleArtifact(String artifactId) throws GovernanceException {
        GovernanceArtifact artifact =
                GovernanceUtils.retrieveGovernanceArtifactById(registry, artifactId);
        if (artifact != null && !(artifact instanceof PeopleArtifact)) {
            String msg = "The artifact request is not a People artifact. id: " + artifactId + ".";
            log.error(msg);
            throw new GovernanceException(msg);
        }
        return (PeopleArtifact) artifact;
    }

    */
/**
     * Removes the given service artifact from the registry.
     *
     * @param  artifactId the identifier of the service artifact.
     *
     * @throws GovernanceException if the operation failed.
     *//*

    public void removePeopleArtifact(String artifactId) throws GovernanceException {
        GovernanceUtils.removeArtifact(registry, artifactId);
    }

    */
/**
     * Sets content of the given peopleArtifact artifact to the given resource on the registry.
     *
     * @param peopleArtifact         the peopleArtifact artifact.
     * @param resource the content resource.
     *
     * @throws GovernanceException if the operation failed.
     *//*

    protected void setContent(PeopleArtifact peopleArtifact, Resource resource) throws
            GovernanceException {
        try {
            OMNamespace namespace = OMAbstractFactory.getOMFactory().
                    createOMNamespace(GovernanceConstants.SERVICE_ELEMENT_NAMESPACE, "");
            Map<String, OMElement> elementsMap = new HashMap<String, OMElement>();
            String[] attributeKeys = peopleArtifact.getAttributeKeys();
            if (attributeKeys != null) {
                OMElement contentElement =
                        OMAbstractFactory.getOMFactory().createOMElement(
                                GovernanceConstants.PEOPLE_ELEMENT_ROOT, namespace);
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
                    String[] attributeValues = peopleArtifact.getAttributes(aggregatedKey);
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
                resource.setContent(updatedContent);
            }
        } catch (RegistryException e) {
            String msg = "Error in saving attributes for the artifact. id: " + peopleArtifact.getId() +
                    ", path: " + peopleArtifact.getPath() + ".";
            log.error(msg, e);
            throw new GovernanceException(msg, e);
        }
    }

    */
/**
     * Finds all people artifacts matching the given filter criteria.
     *
     * @param criteria the filter criteria to be matched.
     *
     * @return the people artifacts that match.
     * @throws GovernanceException if the operation failed.
     *//*

    public PeopleArtifact[] findPeople(PeopleArtifactFilter criteria) throws GovernanceException {
        List<PeopleArtifact> peopleArtifacts = new ArrayList<PeopleArtifact>();
        for (PeopleArtifact peopleArtifact : getAllPeopleArtifacts()) {
            if (peopleArtifact != null) {
                if (criteria.matches(peopleArtifact)) {
                    peopleArtifacts.add(peopleArtifact);
                }
            }
        }
        return peopleArtifacts.toArray(new PeopleArtifact[peopleArtifacts.size()]);
    }

    */
/**
     * This method returns all the People artifacts (Organizations, Departments, Project Groups and
     * Persons) stored in GReg.
     *
     * @return all People artifacts on the registry.
     * @throws GovernanceException if the operation failed.
     *//*

    public PeopleArtifact[] getAllPeopleArtifacts() throws GovernanceException {
        List<String> peoplePaths =
                Arrays.asList(GovernanceUtils.getResultPaths(registry,
                        GovernanceConstants.PEOPLE_MEDIA_TYPE));
        Collections.sort(peoplePaths, new Comparator<String>() {
            public int compare(String o1, String o2) {
                return RegistryUtils.getResourceName(o1).compareToIgnoreCase(
                        RegistryUtils.getResourceName(o2));
            }
        });
        List<PeopleArtifact> peopleArtifacts = new ArrayList<PeopleArtifact>();
        for (String peoplePath : peoplePaths) {
            GovernanceArtifact artifact =
                    GovernanceUtils.retrieveGovernanceArtifactByPath(registry, peoplePath);
            peopleArtifacts.add((PeopleArtifact) artifact);
        }
        return peopleArtifacts.toArray(new PeopleArtifact[peopleArtifacts.size()]);
    }

    */
/**
     * This method retrieves all the Organization artifacts stored in GReg.
     * @return All the Organizations stored in GReg
     * @throws GovernanceException
     *//*

    public Organization[] getAllOrganizations() throws GovernanceException {
        List<Organization> list = new ArrayList<Organization>();
        for (PeopleArtifact artifact : getAllPeopleArtifacts()) {
            if (artifact instanceof Organization) {
                list.add((Organization)artifact);
            }
        }
        return list.toArray(new Organization[list.size()]);
    }

    */
/**
     * This retrieves all the Organization artifacts stored in GReg.
     * @return All the Organizations stored in GReg
     * @throws GovernanceException
     *//*

    public Department[] getAllDepartments() throws GovernanceException {
        List<Department> list = new ArrayList<Department>();
        for (PeopleArtifact artifact : getAllPeopleArtifacts()) {
            if (artifact instanceof Department) {
                list.add((Department)artifact);
            }
        }
        return list.toArray(new Department[list.size()]);
    }

    */
/**
     * This used to retrieves all the Project Group artifacts stored in GReg.
     * @return All the Project Groups stored in GReg
     * @throws GovernanceException
     *//*

    public ProjectGroup[] getAllProjectGroups() throws GovernanceException {
        List<ProjectGroup> list = new ArrayList<ProjectGroup>();
        for (PeopleArtifact artifact : getAllPeopleArtifacts()) {
            if (artifact instanceof ProjectGroup) {
                list.add((ProjectGroup)artifact);
            }
        }
        return list.toArray(new ProjectGroup[list.size()]);
    }

    */
/**
     * This method retrieves all the Person artifacts stored in GReg.
     * @return All the Persons stored in GReg
     * @throws GovernanceException
     *//*

    public Person[] getAllPersons() throws GovernanceException {
        List<Person> list = new ArrayList<Person>();
        for (PeopleArtifact artifact : getAllPeopleArtifacts()) {
            if (artifact instanceof Person) {
                list.add((Person)artifact);
            }
        }
        return list.toArray(new Person[list.size()]);
    }

    */
/**
     * This method is used to find people artifact by name. Only one people artifact can exist
     * in the registry with a given name. Can also be used to check the existence of an artifact.
     *
     * @param peopleArtifactName Name of the people artifact
     * @return The people artifact object represented by the given name, <code>null</code> if no
     * people artifact exists with the given name.
     * @throws GovernanceException If an error occurs
     *//*

    public PeopleArtifact getPeopleArtifactByName(String peopleArtifactName)
                                                        throws GovernanceException {
        for (PeopleArtifact pa : getAllPeopleArtifacts()) {
            if (pa.getName().equals(peopleArtifactName)) {
                return pa;
            }
        }
        return null;
    }
*/

}
