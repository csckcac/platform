/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.governance.api.people.dataobjects;

import org.wso2.carbon.governance.api.exception.GovernanceException;

/**
 * This class represents a ProjectGroup artifact stored in the Registry.
 */
public class ProjectGroup{//} extends PeopleArtifact {

/*
    private static final Log log = LogFactory.getLog(ProjectGroup.class);

    public static final String SUB_GROUPS_ATTRIBUTE = "subGroups_entry";

    public ProjectGroup(String id, String name, String type) {
         super(id, name, type);
         try {
            setAttribute(GovernanceConstants.PEOPLE_GROUP_ATTRIBUTE,
                    GovernanceConstants.PEOPLE_GROUP_ATTRIBUTE_VALUE_PROJECT_GROUP);
        } catch (GovernanceException ignored) {
            // This is unreachable, hence ignored.
        }
    }

    public ProjectGroup(String id, OMElement consumerContentElement) throws GovernanceException {
        super(id, consumerContentElement);
    }

    */
/**
     * Copy constructor used for cloning a ProjectGroup to create a sub-ProjectGroup with same
     * attributes. Only group SUB_GROUPS_ATTRIBUTE and NAME attribute will be different.
     *
     * @param projectGroup the object to copy from
     *//*

    public ProjectGroup(ProjectGroup projectGroup, String subGroupName) throws GovernanceException {
        super(UUID.randomUUID().toString(), subGroupName, projectGroup.getAttribute(GovernanceConstants.PEOPLE_TYPE_ATTRIBUTE));
        attributes = new HashMap(projectGroup.attributes);
        attributes.remove(SUB_GROUPS_ATTRIBUTE);
        setAttribute(GovernanceConstants.SERVICE_NAME_ATTRIBUTE, subGroupName);
        setAttribute(GovernanceConstants.PEOPLE_GROUP_ATTRIBUTE,
                            GovernanceConstants.PEOPLE_GROUP_ATTRIBUTE_VALUE_PROJECT_GROUP);
        associateRegistry(projectGroup.getAssociatedRegistry());
    }

    public void addToOrganization(Organization organization) throws GovernanceException {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public void addToDepartment(Department department) throws GovernanceException {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public void addSubGroup(ProjectGroup projectGroup) throws GovernanceException {
        addAttribute(SUB_GROUPS_ATTRIBUTE, "Project Group:" + projectGroup.getName());
        try {
            Registry registry = getAssociatedRegistry();
            registry.addAssociation(this.getPath(), projectGroup.getPath(), GovernanceConstants.SUB_GROUP);
            registry.addAssociation(projectGroup.getPath(), this.getPath(), GovernanceConstants.IS_PART_OF);
        } catch (RegistryException ex) {
            String msg = "Could not associate sub group. Parent project-group: " +
                    this.getPath() + ", sub project-group: " + projectGroup.getPath();
            log.error(msg, ex);
        }
    }

    */
/**
     * This method is used to retrieve all sub groups associated with this project group.
     * Existence of all sub groups in the registry is assumed to have been validated.
     * @return
     * @throws GovernanceException
     *//*

    public ProjectGroup[] getSubGroups() throws GovernanceException {
        PeopleArtifact[] peopleArtifacts = GovernanceUtils.extractPeopleFromAttribute(getAssociatedRegistry(), this,
                SUB_GROUPS_ATTRIBUTE);
        ProjectGroup[] subGroups = new ProjectGroup[peopleArtifacts.length];
        for (int i = 0; i < peopleArtifacts.length; i++) {
            if (peopleArtifacts[i] instanceof ProjectGroup) {
                subGroups[i] = (ProjectGroup) peopleArtifacts[i];
            } else {
                String msg = "Listed sub group name represents a non-sub group artifact in the " +
                        "registry. Project group name: " + this.getName() + ", Listed subgroup " +
                        "name: " + peopleArtifacts[i].getName();
                log.error(msg);
                throw new GovernanceException(msg);
            }
        }
        return subGroups;
    }

    public Department[] getDepartments() throws GovernanceException {
        return GovernanceUtils.getAffiliatedDepartments(getAssociatedRegistry(), this);
    }

    public Organization[] getOrganizations() throws GovernanceException {
        return GovernanceUtils.getAffiliatedOrganizations(getAssociatedRegistry(), this);
    }

    public Person[] getPersons() throws GovernanceException {
        return GovernanceUtils.getAttachedPersons(getAssociatedRegistry(), this);
    }
*/
}
