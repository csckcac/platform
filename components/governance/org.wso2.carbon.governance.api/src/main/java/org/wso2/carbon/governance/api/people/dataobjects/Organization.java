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
 * This class represents a Organization artifact stored in the Registry.
 */
public class Organization{/*//} extends PeopleArtifact {

    public Organization(String id, String name, String type) {
        super(id, name, type);
         try {
            setAttribute(GovernanceConstants.PEOPLE_GROUP_ATTRIBUTE,
                    GovernanceConstants.PEOPLE_GROUP_ATTRIBUTE_VALUE_ORGANIZATION);
        } catch (GovernanceException ignored) {
            // This is unreachable, hence ignored.
        }
    }

    public Organization(String id, OMElement consumerContentElement) throws GovernanceException {
        super(id, consumerContentElement);
    }

    public Department[] getDepartments() throws GovernanceException {
        List<Department> list = new ArrayList<Department>();
        for (Department dept : new PeopleManager(getAssociatedRegistry()).getAllDepartments()) {
            if (getName().equals(dept.getAttribute(Department.ORGANIZATION_ATTRIBUTE))) {
                list.add(dept);
            }
        }
        return list.toArray(new Department[list.size()]);
    }

    public ProjectGroup[] getProjectGroups() throws GovernanceException {
        return GovernanceUtils.getAttachedProjectGroups(getAssociatedRegistry(), this);
    }

    public Person[] getPersons() throws GovernanceException {
        return GovernanceUtils.getAttachedPersons(getAssociatedRegistry(), this);
    }*/
}
