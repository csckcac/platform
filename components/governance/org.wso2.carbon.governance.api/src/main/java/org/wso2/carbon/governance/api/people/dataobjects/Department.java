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
 * This class represents a Department artifact stored in the Registry.
 */
public class Department{/*//} extends PeopleArtifact {

    public static final String ORGANIZATION_ATTRIBUTE = "overview_organization";

    public Department(String id, OMElement consumerContentElement) throws GovernanceException {
        super(id, consumerContentElement);
    }

    public Department(String id, String name, String type) {
         super(id, name, type);
         try {
            setAttribute(GovernanceConstants.PEOPLE_GROUP_ATTRIBUTE,
                    GovernanceConstants.PEOPLE_GROUP_ATTRIBUTE_VALUE_DEPARTMENT);
        } catch (GovernanceException ignored) {
            // This is unreachable, hence ignored.
        }
    }

    public void addToOrganization(Organization organization) throws GovernanceException {
        setAttribute(ORGANIZATION_ATTRIBUTE, organization.getName());
    }

    public Organization getOrganization() throws GovernanceException {
        PeopleArtifact pa = new PeopleManager(getAssociatedRegistry()).getPeopleArtifactByName(
                getAttribute(ORGANIZATION_ATTRIBUTE));
        if (pa instanceof Organization) {
            return (Organization)pa;
        } else {
            return null;
        }
    }

    public ProjectGroup[] getProjectGroups() throws GovernanceException {
        return GovernanceUtils.getAttachedProjectGroups(getAssociatedRegistry(), this);
    }

    public Person[] getPersons() throws GovernanceException {
        return GovernanceUtils.getAttachedPersons(getAssociatedRegistry(), this);
    }*/
}
