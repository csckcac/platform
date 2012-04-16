/*
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.humantask.core.dao.sql;

import org.wso2.carbon.humantask.core.dao.GenericHumanRoleDAO;
import org.wso2.carbon.humantask.core.dao.SimpleQueryCriteria;
import org.wso2.carbon.humantask.core.dao.TaskStatus;
import org.wso2.carbon.humantask.core.dao.TaskType;
import org.wso2.carbon.humantask.core.engine.PeopleQueryEvaluator;
import org.wso2.carbon.humantask.core.internal.HumanTaskServiceComponent;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A builder class to create SQL statements for the task filtering.
 */
public class HumanTaskJPQLQueryBuilder {

    private SimpleQueryCriteria queryCriteria;

    private EntityManager em;

    private HumanTaskJPQLQueryBuilder() {
    }

    /**
     * @param criteria : The query criteria on which the tasks will be filtered.
     * @param em       : The EntityManager which would create the Query object based on the JPQL string.
     */
    public HumanTaskJPQLQueryBuilder(SimpleQueryCriteria criteria, EntityManager em) {
        this.queryCriteria = criteria;
        this.em = em;
    }

    /**
     * Build the specific sql query based on the query type.
     *
     * @return : The sql query
     */
    public Query build() {
        switch (queryCriteria.getSimpleQueryType()) {
            case ASSIGNED_TO_ME:
                return buildAssignedToMeQuery();
            case ASSIGNABLE:
                return buildAdministrableTasksQuery();
            case CLAIMABLE:
                return buildClaimableQuery();
            case ALL_TASKS:
                return buildAllTasksQuery();
            case NOTIFICATIONS:
                return buildNotificationsQuery();
            case REMOVE_TASKS:
                return buildRemoveTasksQuery();
        }
        return null;
    }

    private Query buildAdministrableTasksQuery() {

        Query administrableTasksQuery = em.createQuery(" SELECT t FROM org.wso2.carbon.humantask.core.dao.jpa.openjpa.model.Task t " +
                                                       " JOIN t.humanRoles hr JOIN hr.orgEntities oe WHERE  " +
                                                       " oe.name IN :names " +
                                                       " AND " +
                                                       " hr.type = :roleType " +
                                                       " AND " +
                                                       " t.type = :taskType " +
                                                       " AND " +
                                                       " t.tenantId = :tenantId " +
                                                       " ORDER BY t.createdOn DESC ");

        List<String> rolesAndNamesList = getNameListForUser(queryCriteria.getCaller(), true);
        administrableTasksQuery.setParameter("names", rolesAndNamesList);
        administrableTasksQuery.setParameter("tenantId", queryCriteria.getCallerTenantId());
        administrableTasksQuery.setParameter("roleType", GenericHumanRoleDAO.GenericHumanRoleType.BUSINESS_ADMINISTRATORS);
        administrableTasksQuery.setParameter("taskType", TaskType.TASK);
        return administrableTasksQuery;
    }

    private Query buildClaimableQuery() {

        Query claimableTasksQuery = em.createQuery(" SELECT t FROM org.wso2.carbon.humantask.core.dao.jpa.openjpa.model.Task t " +
                                                   " JOIN t.humanRoles hr JOIN hr.orgEntities oe WHERE  " +
                                                   " oe.name IN :names " +
                                                   " AND " +
                                                   " hr.type = :roleType " +
                                                   " AND " +
                                                   " t.type = :taskType " +
                                                   " AND " +
                                                   " t.tenantId = :tenantId " +
                                                   " AND " +
                                                   " t.status IN :taskStatuses " +
                                                   " ORDER BY t.createdOn DESC ");

        List<String> rolesAndNamesList = getNameListForUser(queryCriteria.getCaller(), false);
        claimableTasksQuery.setParameter("names", rolesAndNamesList);
        claimableTasksQuery.setParameter("tenantId", queryCriteria.getCallerTenantId());
        claimableTasksQuery.setParameter("roleType", GenericHumanRoleDAO.GenericHumanRoleType.POTENTIAL_OWNERS);
        claimableTasksQuery.setParameter("taskType", TaskType.TASK);
        List<TaskStatus> statusList = Arrays.asList(TaskStatus.READY);
        claimableTasksQuery.setParameter("taskStatuses", statusList);
        return claimableTasksQuery;
    }

    private Query buildAllTasksQuery() {

        Query allTasksQuery = em.createQuery(" SELECT DISTINCT t FROM org.wso2.carbon.humantask.core.dao.jpa.openjpa.model.Task t " +
                                             " JOIN t.humanRoles hr JOIN hr.orgEntities oe WHERE  " +
                                             " oe.name IN :names " +
                                             " AND " +
                                             " t.status NOT IN :taskStatuses " +
                                             " AND " +
                                             " t.type = :taskType " +
                                             " AND " +
                                             " t.tenantId = :tenantId " +
                                             " ORDER BY t.createdOn DESC ");

        List<String> rolesAndNamesList = getNameListForUser(queryCriteria.getCaller(), true);
        allTasksQuery.setParameter("names", rolesAndNamesList);
        allTasksQuery.setParameter("tenantId", queryCriteria.getCallerTenantId());
        List<TaskStatus> statusList = Arrays.asList(TaskStatus.OBSOLETE);
        allTasksQuery.setParameter("taskStatuses", statusList);
        allTasksQuery.setParameter("taskType", TaskType.TASK);
        return allTasksQuery;
    }

    //Creates the JPQL query to list tasks assigned for the particular user.
    private Query buildAssignedToMeQuery() {

        Query assignedToMeQuery = em.createQuery(" SELECT t FROM org.wso2.carbon.humantask.core.dao.jpa.openjpa.model.Task t " +
                                                 " JOIN t.humanRoles hr JOIN hr.orgEntities oe WHERE  " +
                                                 " oe.name = :name " +
                                                 " AND " +
                                                 " hr.type = :roleType " +
                                                 " AND " +
                                                 " t.tenantId = :tenantId " +
                                                 " AND " +
                                                 " t.type = :taskType " +
                                                 " AND " +
                                                 " t.status NOT IN :taskStatuses " +
                                                 " ORDER BY t.createdOn DESC ");

        assignedToMeQuery.setParameter("tenantId", queryCriteria.getCallerTenantId());
        assignedToMeQuery.setParameter("name", queryCriteria.getCaller());
        assignedToMeQuery.setParameter("taskType", TaskType.TASK);
        assignedToMeQuery.setParameter("roleType", GenericHumanRoleDAO.GenericHumanRoleType.ACTUAL_OWNER);
        List<TaskStatus> statusList = Arrays.asList(TaskStatus.COMPLETED, TaskStatus.OBSOLETE);
        assignedToMeQuery.setParameter("taskStatuses", statusList);
        return assignedToMeQuery;
    }

    private Query buildRemoveTasksQuery() {
        Query removeTasksQuery = em.createQuery(" DELETE FROM  org.wso2.carbon.humantask.core.dao.jpa.openjpa.model.Task t  " +
                                                " WHERE t.status in :removableStatuses" );

        removeTasksQuery.setParameter("removableStatuses" , queryCriteria.getStatuses());
        return removeTasksQuery;
    }


    //Creates the JPQL query to list notifications applicable for a particular user.
    private Query buildNotificationsQuery() {

        Query notificationsQuery = em.createQuery(" SELECT t FROM org.wso2.carbon.humantask.core.dao.jpa.openjpa.model.Task t " +
                                                  " JOIN t.humanRoles hr JOIN hr.orgEntities oe WHERE  " +
                                                  " oe.name IN :names " +
                                                  " AND " +
                                                  " hr.type = :roleType " +
                                                  " AND " +
                                                  " t.tenantId = :tenantId " +
                                                  " AND " +
                                                  " t.type = :taskType " +
                                                  " ORDER BY t.createdOn DESC ");

        notificationsQuery.setParameter("tenantId", queryCriteria.getCallerTenantId());
        List<String> rolesAndNamesList = getNameListForUser(queryCriteria.getCaller(), true);
        notificationsQuery.setParameter("names", rolesAndNamesList);
        notificationsQuery.setParameter("roleType", GenericHumanRoleDAO.GenericHumanRoleType.NOTIFICATION_RECIPIENTS);
        notificationsQuery.setParameter("taskType", TaskType.NOTIFICATION);
        return notificationsQuery;
    }

    private List<String> getNameListForUser(String userName, boolean includeUserName) {
        List<String> roleNameList = new ArrayList<String>();
        if (includeUserName) {
            roleNameList.add(userName);
        }
        PeopleQueryEvaluator peopleQueryEvaluator = HumanTaskServiceComponent.
                getHumanTaskServer().getTaskEngine().getPeopleQueryEvaluator();
        roleNameList.addAll(peopleQueryEvaluator.getRoleNameListForUser(userName));
        return roleNameList;
    }
}
