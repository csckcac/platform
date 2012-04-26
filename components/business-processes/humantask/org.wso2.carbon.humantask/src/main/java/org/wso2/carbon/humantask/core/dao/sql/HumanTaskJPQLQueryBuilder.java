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

    private static final String SELECT_TASKS = " SELECT t FROM org.wso2.carbon.humantask.core.dao.jpa.openjpa.model.Task t ";
    private static final String JOIN_HUMAN_ROLES_JOIN_ORG_ENTITIES = " JOIN t.humanRoles hr JOIN hr.orgEntities oe WHERE  ";
    private static final String OE_NAME_IN_NAMES = " oe.name IN :names ";
    private static final String AND = " AND ";
    private static final String HR_TYPE_ROLE_TYPE = " hr.type = :roleType ";
    private static final String T_TYPE_TASK_TYPE = " t.type = :taskType ";
    private static final String T_TENANT_ID_TENANT_ID = " t.tenantId = :tenantId ";
    private static final String ORDER_BY_T_CREATED_ON_DESC = " ORDER BY t.createdOn DESC ";
    private static final String T_STATUS_IN_TASK_STATUSES = " t.status IN :taskStatuses ";
    private static final String T_STATUS_NOT_IN_TASK_STATUSES = " t.status NOT IN :taskStatuses ";
    private static final String NAMES = "names";
    private static final String TENANT_ID = "tenantId";
    private static final String ROLE_TYPE = "roleType";
    private static final String TASK_TYPE = "taskType";
    private static final String TASK_STATUSES = "taskStatuses";
    private SimpleQueryCriteria queryCriteria;

    private EntityManager em;

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

        Query administrableTasksQuery = em.createQuery(SELECT_TASKS +
                JOIN_HUMAN_ROLES_JOIN_ORG_ENTITIES +
                OE_NAME_IN_NAMES +
                AND +
                HR_TYPE_ROLE_TYPE +
                AND +
                T_TYPE_TASK_TYPE +
                AND +
                T_TENANT_ID_TENANT_ID +
                ORDER_BY_T_CREATED_ON_DESC);

        List<String> rolesAndNamesList = getNameListForUser(queryCriteria.getCaller(), true);
        administrableTasksQuery.setParameter(NAMES, rolesAndNamesList);
        administrableTasksQuery.setParameter(TENANT_ID, queryCriteria.getCallerTenantId());
        administrableTasksQuery.setParameter(ROLE_TYPE, GenericHumanRoleDAO.GenericHumanRoleType.BUSINESS_ADMINISTRATORS);
        administrableTasksQuery.setParameter(TASK_TYPE, TaskType.TASK);
        return administrableTasksQuery;
    }

    private Query buildClaimableQuery() {

        Query claimableTasksQuery = em.createQuery(SELECT_TASKS +
                JOIN_HUMAN_ROLES_JOIN_ORG_ENTITIES +
                OE_NAME_IN_NAMES +
                AND +
                HR_TYPE_ROLE_TYPE +
                AND +
                T_TYPE_TASK_TYPE +
                AND +
                T_TENANT_ID_TENANT_ID +
                AND +
                T_STATUS_IN_TASK_STATUSES +
                ORDER_BY_T_CREATED_ON_DESC);

        List<String> rolesAndNamesList = getNameListForUser(queryCriteria.getCaller(), false);
        claimableTasksQuery.setParameter(NAMES, rolesAndNamesList);
        claimableTasksQuery.setParameter(TENANT_ID, queryCriteria.getCallerTenantId());
        claimableTasksQuery.setParameter(ROLE_TYPE, GenericHumanRoleDAO.GenericHumanRoleType.POTENTIAL_OWNERS);
        claimableTasksQuery.setParameter(TASK_TYPE, TaskType.TASK);
        List<TaskStatus> statusList = Arrays.asList(TaskStatus.READY);
        claimableTasksQuery.setParameter(TASK_STATUSES, statusList);
        return claimableTasksQuery;
    }

    private Query buildAllTasksQuery() {

        Query allTasksQuery = em.createQuery(" SELECT DISTINCT t FROM org.wso2.carbon.humantask.core.dao.jpa.openjpa.model.Task t " +
                JOIN_HUMAN_ROLES_JOIN_ORG_ENTITIES +
                OE_NAME_IN_NAMES +
                AND +
                T_STATUS_NOT_IN_TASK_STATUSES +
                AND +
                T_TYPE_TASK_TYPE +
                AND +
                T_TENANT_ID_TENANT_ID +
                ORDER_BY_T_CREATED_ON_DESC);

        List<String> rolesAndNamesList = getNameListForUser(queryCriteria.getCaller(), true);
        allTasksQuery.setParameter(NAMES, rolesAndNamesList);
        allTasksQuery.setParameter(TENANT_ID, queryCriteria.getCallerTenantId());
        List<TaskStatus> statusList = Arrays.asList(TaskStatus.OBSOLETE);
        allTasksQuery.setParameter(TASK_STATUSES, statusList);
        allTasksQuery.setParameter(TASK_TYPE, TaskType.TASK);
        return allTasksQuery;
    }

    //Creates the JPQL query to list tasks assigned for the particular user.
    private Query buildAssignedToMeQuery() {

        Query assignedToMeQuery = em.createQuery(SELECT_TASKS +
                JOIN_HUMAN_ROLES_JOIN_ORG_ENTITIES +
                " oe.name = :name " +
                AND +
                HR_TYPE_ROLE_TYPE +
                AND +
                T_TENANT_ID_TENANT_ID +
                AND +
                T_TYPE_TASK_TYPE +
                AND +
                T_STATUS_NOT_IN_TASK_STATUSES +
                ORDER_BY_T_CREATED_ON_DESC);

        assignedToMeQuery.setParameter(TENANT_ID, queryCriteria.getCallerTenantId());
        assignedToMeQuery.setParameter("name", queryCriteria.getCaller());
        assignedToMeQuery.setParameter(TASK_TYPE, TaskType.TASK);
        assignedToMeQuery.setParameter(ROLE_TYPE, GenericHumanRoleDAO.GenericHumanRoleType.ACTUAL_OWNER);
        List<TaskStatus> statusList = Arrays.asList(TaskStatus.COMPLETED,
                                                    TaskStatus.OBSOLETE,
                                                    TaskStatus.FAILED,
                                                    TaskStatus.REMOVED);
        assignedToMeQuery.setParameter(TASK_STATUSES, statusList);
        return assignedToMeQuery;
    }

    private Query buildRemoveTasksQuery() {
        Query removeTasksQuery = em.createQuery(" DELETE FROM  org.wso2.carbon.humantask.core.dao.jpa.openjpa.model.Task t  " +
                " WHERE t.status in :removableStatuses");

        removeTasksQuery.setParameter("removableStatuses", queryCriteria.getStatuses());
        return removeTasksQuery;
    }


    //Creates the JPQL query to list notifications applicable for a particular user.
    private Query buildNotificationsQuery() {

        Query notificationsQuery = em.createQuery(SELECT_TASKS +
                JOIN_HUMAN_ROLES_JOIN_ORG_ENTITIES +
                OE_NAME_IN_NAMES +
                AND +
                HR_TYPE_ROLE_TYPE +
                AND +
                T_TENANT_ID_TENANT_ID +
                AND +
                T_TYPE_TASK_TYPE +
                ORDER_BY_T_CREATED_ON_DESC);

        notificationsQuery.setParameter(TENANT_ID, queryCriteria.getCallerTenantId());
        List<String> rolesAndNamesList = getNameListForUser(queryCriteria.getCaller(), true);
        notificationsQuery.setParameter(NAMES, rolesAndNamesList);
        notificationsQuery.setParameter(ROLE_TYPE, GenericHumanRoleDAO.GenericHumanRoleType.NOTIFICATION_RECIPIENTS);
        notificationsQuery.setParameter(TASK_TYPE, TaskType.NOTIFICATION);
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
