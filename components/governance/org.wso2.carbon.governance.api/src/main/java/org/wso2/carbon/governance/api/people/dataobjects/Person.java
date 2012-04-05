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

/**
 * This class represents a Person artifact stored in the Registry.
 */
public class Person {/*//extends PeopleArtifact {

    public Person(String id, String name, String type) {
        super(id, name, type);
        try {
            setAttribute(GovernanceConstants.PEOPLE_GROUP_ATTRIBUTE,
                    GovernanceConstants.PEOPLE_GROUP_ATTRIBUTE_VALUE_PERSON);
        } catch (GovernanceException ignored) {
            // This is unreachable, hence ignored.
        }
    }

    public Person(String id, OMElement consumerContentElement) throws GovernanceException {
        super(id, consumerContentElement);
    }

    public void addToOrganization(Organization organization) throws GovernanceException {
        addAttribute(GovernanceConstants.AFFILIATIONS_ATTRIBUTE,
                "Organization:" + organization.getName());
    }

    public void addToDepartment(Department department) throws GovernanceException {
        addAttribute(GovernanceConstants.AFFILIATIONS_ATTRIBUTE,
                "Department:" + department.getName());
    }

    public void addToProjectGroup(ProjectGroup projectGroup) throws GovernanceException {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public Department[] getDepartments() throws GovernanceException {
        return GovernanceUtils.getAffiliatedDepartments(getAssociatedRegistry(), this);
    }

    public Organization[] getOrganizations() throws GovernanceException {
        return GovernanceUtils.getAffiliatedOrganizations(getAssociatedRegistry(), this);
    }

    public ProjectGroup[] getProjectGroups() throws GovernanceException {
        return GovernanceUtils.getAffiliatedProjectGroups(getAssociatedRegistry(), this);
    }*/

}
