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

package org.wso2.carbon.humantask.core.engine.util;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.wso2.carbon.humantask.*;
import org.wso2.carbon.humantask.core.HumanTaskConstants;
import org.wso2.carbon.humantask.core.api.scheduler.Scheduler;
import org.wso2.carbon.humantask.core.dao.*;
import org.wso2.carbon.humantask.core.dao.jpa.openjpa.model.Deadline;
import org.wso2.carbon.humantask.core.engine.HumanTaskException;
import org.wso2.carbon.humantask.core.engine.PeopleQueryEvaluator;
import org.wso2.carbon.humantask.core.engine.runtime.api.EvaluationContext;
import org.wso2.carbon.humantask.core.engine.runtime.api.ExpressionLanguageRuntime;
import org.wso2.carbon.humantask.core.engine.runtime.api.HumanTaskRuntimeException;
import org.wso2.carbon.humantask.core.internal.HumanTaskServiceComponent;
import org.wso2.carbon.humantask.core.store.HumanTaskBaseConfiguration;
import org.wso2.carbon.humantask.core.store.HumanTaskStore;
import org.wso2.carbon.humantask.core.store.TaskConfiguration;
import org.wso2.carbon.humantask.core.utils.Duration;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Common utility method for all the TaskDAO objects.
 */
public final class CommonTaskUtil {
    private CommonTaskUtil() {
    }

    private static final Log log = LogFactory.getLog(CommonTaskUtil.class);

    /**
     * Checks whether the provided task has sub tasks.
     *
     * @param task : The TaskDAO object.
     * @return : True if the task has sub tasks. false otherwise.
     */
    public static Boolean hasSubTasks(TaskDAO task) {
        Boolean hasSubTasks = false;
        if (task.getSubTasks() != null && task.getSubTasks().size() > 0) {
            hasSubTasks = true;
        }
        return hasSubTasks;
    }

    /**
     * Checks whether the provided task has sub tasks.
     *
     * @param task : The TaskDAO object.
     * @return : True if the task has sub tasks. false otherwise.
     */
    public static Boolean hasAttachments(TaskDAO task) {
        Boolean hasAttachments = false;
        if (task.getAttachments() != null && task.getAttachments().size() > 0) {
            hasAttachments = true;
        }
        return hasAttachments;
    }

    /**
     * Checks whether the provided task has sub tasks.
     *
     * @param task : The TaskDAO object.
     * @return : True if the task has sub tasks. false otherwise.
     */
    public static Boolean hasComments(TaskDAO task) {
        Boolean hasComments = false;
        if (task.getComments() != null && task.getComments().size() > 0) {
            hasComments = true;
        }
        return hasComments;
    }

    /**
     * Checks whether the provided task has sub tasks.
     *
     * @param task : The TaskDAO object.
     * @return : True if the task has sub tasks. false otherwise.
     */
    public static Boolean hasOutput(TaskDAO task) {
        Boolean hasOutput = false;
        if (task.getOutputMessage() != null) {
            hasOutput = true;
        }
        return hasOutput;
    }

    /**
     * Checks whether the provided task has sub tasks.
     *
     * @param task : The TaskDAO object.
     * @return : True if the task has sub tasks. false otherwise.
     */
    public static Boolean hasFault(TaskDAO task) {
        Boolean hasFailure = false;
        if (task.getFailureMessage() != null) {
            hasFailure = true;
        }
        return hasFailure;
    }

    /**
     * Gets the task configuration object for the given task.
     *
     * @param task :
     * @return : The task configuration object.
     */
    public static HumanTaskBaseConfiguration getTaskConfiguration(TaskDAO task) {
        HumanTaskStore taskStore = HumanTaskServiceComponent.getHumanTaskServer().
                getTaskStoreManager().getHumanTaskStore(task.getTenantId());
        return taskStore.getTaskConfiguration(QName.valueOf(task.getName()));
    }

    /**
     * Checks whether the given TaskDAO has potential owners.
     *
     * @param task : The task to be checked for potential owners.
     * @return : true if the task has potential owners.
     */
    public static boolean hasPotentialOwners(TaskDAO task) {

        PeopleQueryEvaluator pqe = HumanTaskServiceComponent.getHumanTaskServer().
                getTaskEngine().getPeopleQueryEvaluator();

        boolean hasPotentialOwners = false;
        for (GenericHumanRoleDAO humanRoleDAO : task.getHumanRoles()) {
            if (GenericHumanRoleDAO.GenericHumanRoleType.POTENTIAL_OWNERS.
                    equals(humanRoleDAO.getType()) && humanRoleDAO.getOrgEntities() != null &&
                    humanRoleDAO.getOrgEntities().size() > 0) {
                try {
                    pqe.checkOrgEntitiesExist(humanRoleDAO.getOrgEntities());
                    hasPotentialOwners = true;
                } catch (HumanTaskRuntimeException ex) {
                    hasPotentialOwners = false;
                }
            }
        }
        return hasPotentialOwners;
    }

    /**
     * Gets the single user for a given role.
     * Note : this method is applicable for roles which has only one OrganizationalEntityDAO under it
     * Example : task initiator and actual owner.
     *
     * @param task : The task DAO object.
     * @param type : The role type of the user.
     * @return : The name of the actual owner of the task.
     */
    public static OrganizationalEntityDAO getUserEntityForRole(TaskDAO task,
                                                               GenericHumanRoleDAO.GenericHumanRoleType type) {
        OrganizationalEntityDAO matchingUser = null;
        for (GenericHumanRoleDAO humanRoleDAO : task.getHumanRoles()) {
            if (type.equals(humanRoleDAO.getType()) && humanRoleDAO.getOrgEntities() != null &&
                    humanRoleDAO.getOrgEntities().size() == 1) {
                matchingUser = humanRoleDAO.getOrgEntities().get(0);
                break;
            }
        }
        return matchingUser;
    }

    /**
     * Gets a list of OrganizationalEntityDAO for the given task and the role type.
     *
     * @param task : The TaskDAO.
     * @param type : The GenericHumanRoleType for which the OrganizationalEntityDAO list to be retrieved.
     * @return : The list of OrganizationalEntityDAOs
     */
    public static List<OrganizationalEntityDAO> getOrgEntitiesForRole(TaskDAO task,
                                                                      GenericHumanRoleDAO.GenericHumanRoleType type) {
        List<OrganizationalEntityDAO> matchingOrgEntities = new ArrayList<OrganizationalEntityDAO>();
        if (task != null && type != null) {
            for (GenericHumanRoleDAO humanRoleDAO : task.getHumanRoles()) {
                if (humanRoleDAO.getType().equals(type)) {
                    matchingOrgEntities.addAll(humanRoleDAO.getOrgEntities());
                    break;
                }
            }
        }
        return matchingOrgEntities;
    }

    /**
     * Gets the default presentation name of tha task.
     *
     * @param task : The task object.
     * @return : The default presentation name if there's any, null otherwise.
     */
    public static PresentationNameDAO getDefaultPresentationName(TaskDAO task) {
        PresentationNameDAO defaultPresentationName = null;
        if (task.getPresentationNames() != null && task.getPresentationNames().size() > 0) {

            if (task.getPresentationNames().size() == 1) {
                defaultPresentationName = task.getPresentationNames().get(0);
            } else {
                for (PresentationNameDAO presentationName : task.getPresentationNames()) {
                    if (StringUtils.isNotEmpty(presentationName.getXmlLang()) &&
                            presentationName.getXmlLang().toLowerCase().contains("en")) {
                        defaultPresentationName = presentationName;
                        break;
                    }
                }
            }

            // if at this point we cannot get the default presentation name we'd return the first
            // as the task presentation name.
            if (defaultPresentationName == null) {
                task.getPresentationNames().get(0);
            }
        }
        return defaultPresentationName;
    }

    /**
     * @param task TaskDAO
     * @return PresentationSubjectDAO
     */
    public static PresentationSubjectDAO getDefaultPresentationSubject(TaskDAO task) {
        PresentationSubjectDAO defaultPresentationSubject = null;
        if (task.getPresentationSubjects() != null && task.getPresentationSubjects().size() > 0) {

            if (task.getPresentationSubjects().size() == 1) {
                defaultPresentationSubject = task.getPresentationSubjects().get(0);
            } else {
                for (PresentationSubjectDAO subject : task.getPresentationSubjects()) {
                    if (StringUtils.isNotEmpty(subject.getXmlLang()) &&
                            subject.getXmlLang().toLowerCase().contains("en")) {
                        defaultPresentationSubject = subject;
                        break;
                    }
                }
            }

            // if at this point we cannot get the default presentation subject we'd return the first
            // as the task presentation subject.
            if (defaultPresentationSubject == null) {
                task.getPresentationSubjects().get(0);
            }
        }
        return defaultPresentationSubject;
    }

    /**
     * Returns the default presentation description of the task.
     *
     * @param task : The task object
     * @return : The default presentation description.
     */
    public static PresentationDescriptionDAO getDefaultPresentationDescription(TaskDAO task) {
        PresentationDescriptionDAO presentationDescriptionDAO = null;
        if (task.getPresentationDescriptions() != null && task.getPresentationDescriptions().size() > 0) {

            if (task.getPresentationDescriptions().size() == 1) {
                presentationDescriptionDAO = task.getPresentationDescriptions().get(0);
            } else {
                for (PresentationDescriptionDAO description : task.getPresentationDescriptions()) {
                    if (StringUtils.isNotEmpty(description.getXmlLang()) &&
                            description.getXmlLang().toLowerCase().contains("en")) {
                        presentationDescriptionDAO = description;
                        break;
                    }
                }
            }

            // if at this point we cannot get the default presentation description we'd return the first
            // as the task presentation subject.
            if (presentationDescriptionDAO == null) {
                task.getPresentationDescriptions().get(0);
            }
        }
        return presentationDescriptionDAO;
    }

//    /**
//     * Gets the presentation name of the task for the given language.
//     *
//     * @param task    : The task object.
//     * @param xmlLang : The xml lang to filter.
//     * @return : The matching PresentationNameDAO if one exists, else null.
//     */
//    public static PresentationSubjectDAO getPresentationSubject(TaskDAO task, String xmlLang) {
//        PresentationSubjectDAO matchingSubject = null;
//        if (task.getPresentationSubjects() != null && task.getPresentationSubjects().size() > 0) {
//            for (PresentationSubjectDAO subjectDAO : task.getPresentationSubjects()) {
//                if (StringUtils.isNotEmpty(subjectDAO.getXmlLang()) &&
//                        subjectDAO.getXmlLang().toLowerCase().contains(xmlLang)) {
//                    matchingSubject = subjectDAO;
//                    break;
//                }
//            }
//        }
//        return matchingSubject;
//    }

    /**
     * Nominates the given task to a matching actual owner, if there's only 1 user in the potential owners list. In that case
     * the task status would be set to RESERVED. If there are more than 1 user only the task status would be set to READY.
     *
     * @param task : The task to be nominated.
     * @param pqe  : The people query evaluator for people queries.
     */
    public static void nominate(TaskDAO task, PeopleQueryEvaluator pqe) {
        //we can nominate only if there's no specified activation time or the provided activation time has expired.
        if (task.getActivationTime() == null || !task.getActivationTime().after(new Date())) {
            for (GenericHumanRoleDAO ghr : task.getHumanRoles()) {
                if (GenericHumanRoleDAO.GenericHumanRoleType.POTENTIAL_OWNERS.equals(ghr.getType())) {
                    for (OrganizationalEntityDAO orgEntity : ghr.getOrgEntities()) {
                        if (OrganizationalEntityDAO.OrganizationalEntityType.GROUP.equals(orgEntity.getOrgEntityType())) {
                            String roleName = orgEntity.getName();

                            if (pqe.isExistingRole(roleName) && pqe.getUserNameListForRole(roleName).size() == 1) {
                                // There's only 1 user matching for potential owners. so we can safely reserve the
                                // task for that particular user.
                                GenericHumanRoleDAO actualOwnerRole =
                                        pqe.createGHRForRoleName(roleName,
                                                GenericHumanRoleDAO.GenericHumanRoleType.ACTUAL_OWNER);
                                actualOwnerRole.setTask(task);
                                task.addHumanRole(actualOwnerRole);
                                task.setStatus(TaskStatus.RESERVED);
                            } else if (pqe.isExistingRole(roleName)) {
                                // this means there might be more than 1 user or zero users with
                                // the given role name. In both cases we make the task status to be READY.
                                task.setStatus(TaskStatus.READY);
                            }
                        }
                    }
                    break;
                }
            }
        }
    }

    /**
     * Calculates the task priority.
     *
     * @param taskConfig : The task's configuration
     * @param evalCtx    : The evaluation context.
     * @return : The task priority
     */
    public static Integer calculateTaskPriority(HumanTaskBaseConfiguration taskConfig,
                                                EvaluationContext evalCtx) {

        TPriorityExpr priorityDef = taskConfig.getPriorityExpression();

        Integer taskPriorityInt = HumanTaskConstants.DEFAULT_TASK_PRIORITY;

        if (priorityDef != null) {
            String expLang = priorityDef.getExpressionLanguage() == null ?
                    taskConfig.getExpressionLanguage() :
                    priorityDef.getExpressionLanguage();
            ExpressionLanguageRuntime expLangRuntime = HumanTaskServiceComponent.getHumanTaskServer().
                    getTaskEngine().getExpressionLanguageRuntime(expLang);

            Number priority = expLangRuntime.evaluateAsNumber(priorityDef.newCursor().
                    getTextValue().trim(), evalCtx);
            if (priority.intValue() > 10 || priority.intValue() < 1) {
                log.warn(String.format("Ignoring the task priority value " +
                        ":[%d] The task priority has to be with 1 and 10. ",
                        priority.intValue()));
            } else {
                taskPriorityInt = priority.intValue();
            }
        }

        return taskPriorityInt;
    }

    public static List<OrganizationalEntityDAO> getOrganizationalEntities(

            PeopleQueryEvaluator peopleQueryEvaluator,
            TFrom tFrom) throws HumanTaskException {

        String roleName = null;
        for (TArgument tArgument : tFrom.getArgumentArray()) {
            //TODO what about expression language
            if ("role".equals(tArgument.getName())) {
                roleName = tArgument.newCursor().getTextValue();
                break;
            }
        }

        if (roleName == null || StringUtils.isEmpty(roleName)) {
            throw new HumanTaskRuntimeException("The role name cannot be empty: " + tFrom.toString());
        } else {
            roleName = roleName.trim();
        }

        List<OrganizationalEntityDAO> orgEnties = new ArrayList<OrganizationalEntityDAO>();
        orgEnties.add(peopleQueryEvaluator.createGroupOrgEntityForRole(roleName));

        return orgEnties;

    }

    public static String replaceUsingPresentationParams(
            List<PresentationParameterDAO> presentationParameters,
            String expression) {
        String result;
        result = expression.replaceAll("\\{\\{", "{");
        result = result.replaceAll("\\}\\}", "}");
        for (PresentationParameterDAO param : presentationParameters) {
            result = result.replaceAll("\\{\\$" + param.getName() + "\\}", param.getValue());
        }

        // According to the samples of the spec we need to support $paramName$ kind of replacements as well.
        for (PresentationParameterDAO param : presentationParameters) {
            result = result.replaceAll("\\$" + param.getName() + "\\$", param.getValue());
        }

        return result;
    }

    /**
     * Sets the task object to input message.
     *
     * @param task: The task.
     */
    public static void setTaskToMessage(TaskDAO task) {
        task.getInputMessage().setTask(task);
    }

    /**
     * Returns the element with the given name.
     *
     * @param inputMessage : The input message.
     * @param partName     : The part name for which the data element is required.
     * @return : The matching element.
     */
    public static Element getMessagePart(MessageDAO inputMessage, String partName) {
        Element node = inputMessage.getBodyData();
        if (node.hasChildNodes()) {
            NodeList nodeList = node.getChildNodes();
            Element matchingElement = null;
            for (int i = 0; i < nodeList.getLength(); i++) {
                if (nodeList.item(i).getNodeName().equals(partName)) {
                    matchingElement = (Element) nodeList.item(i);
                    break;
                }
            }

            return matchingElement;
        } else {
            throw new HumanTaskRuntimeException("The input message does not have any child elements");
        }

    }

    public static void processDeadlines(TaskDAO task, TaskConfiguration taskConf,
                                        EvaluationContext evalContext) {
        //TODO is it possible to process deadlines once per a task definition and set the duration per a task instance?
        TDeadlines deadlines = taskConf.getDeadlines();

        if (deadlines != null) {
            TDeadline[] startDeadlines = deadlines.getStartDeadlineArray();
            TDeadline[] completionDeadlines = deadlines.getCompletionDeadlineArray();

            for (TDeadline deadline : startDeadlines) {
                DeadlineDAO startDeadline = HumanTaskServiceComponent.getHumanTaskServer().
                        getTaskEngine().getDaoConnectionFactory().getConnection().createDeadline();
                startDeadline.setStatus(TaskStatus.IN_PROGRESS);
                startDeadline.setName((deadline.getName() == null ? "startDeadline" : deadline.getName()));
                startDeadline.setTask(task);
                startDeadline.setDeadlineDate(calculateDeadline(task, deadline, taskConf, evalContext).getTime());
                task.addDeadline(startDeadline);
            }

            for (TDeadline deadline : completionDeadlines) {
                Deadline completionDeadline = new Deadline();
                completionDeadline.setStatus(TaskStatus.COMPLETED);
                completionDeadline.setName((deadline.getName() == null ? "completionDeadline" : deadline.getName()));
                completionDeadline.setTask(task);
                completionDeadline.setDeadlineDate(calculateDeadline(task, deadline, taskConf, evalContext).
                        getTime());
                task.addDeadline(completionDeadline);
            }
        }
    }

    private static Calendar calculateDeadline(TaskDAO taskDAO, TDeadline deadline,
                                              TaskConfiguration taskConf,
                                              EvaluationContext evalCtx) {
        if (deadline.getUntil() != null) {
            String expLang = deadline.getUntil().getExpressionLanguage() == null ?
                    taskConf.getExpressionLanguage() :
                    deadline.getUntil().getExpressionLanguage();
            return HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().
                    getExpressionLanguageRuntime(expLang).evaluateAsDate(deadline.getUntil().
                    newCursor().getTextValue(), evalCtx);
        } else if (deadline.getFor() != null) {
            String expLang = deadline.getFor().getExpressionLanguage() == null ?
                    taskConf.getExpressionLanguage() :
                    deadline.getFor().getExpressionLanguage();
            Duration duration = HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().
                    getExpressionLanguageRuntime(expLang).evaluateAsDuration(deadline.getFor().
                    newCursor().getTextValue(), evalCtx);
            Calendar durationCalendar = Calendar.getInstance();
            durationCalendar.setTime(taskDAO.getActivationTime());
            duration.addTo(durationCalendar);
            return durationCalendar;
        }
        return null;
    }

    public static void scheduleDeadlines(TaskDAO task) {
        List<DeadlineDAO> deadlines = task.getDeadlines();
        for (DeadlineDAO deadline : deadlines) {
            HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    scheduleJob(System.currentTimeMillis(), deadline.getDeadlineDate().getTime(),
                            Scheduler.JobType.TIMER_DEADLINE, null, task.getId(), deadline.getName());
        }
    }

    /**
     * Gets the potential owner role name for the given task.
     * @param task : The task object.
     * @return : The potential owner role name.
     */
    public static String getPotentialOwnerRoleName(TaskDAO task) {
          String roleName = null;
        GHR_ITERATION:
        for(GenericHumanRoleDAO role : task.getHumanRoles()){
            if(GenericHumanRoleDAO.GenericHumanRoleType.POTENTIAL_OWNERS.equals(role.getType())) {
                for(OrganizationalEntityDAO orgEntity : role.getOrgEntities()) {
                    if(OrganizationalEntityDAO.OrganizationalEntityType.GROUP.equals(orgEntity.getOrgEntityType())) {
                        roleName = orgEntity.getName();
                        break GHR_ITERATION;
                    }
                }
            }
        }
        return roleName;
    }

    /**
     * Returns the list of assignable user name list.
     *
     * @param task               : The task object.
     * @param excludeActualOwner : Whether to exclude the actual owner from the returned list.
     * @return : the list of assignable user name list.
     */
    public static List<String> getAssignableUserNameList(TaskDAO task, boolean excludeActualOwner) {
        String potentialOwnerRole = getPotentialOwnerRoleName(task);
        RegistryService registryService = HumanTaskServiceComponent.getRegistryService();
        try {
            UserRealm userRealm = registryService.getUserRealm(task.getTenantId());

            String[] assignableUsersArray =
                    userRealm.getUserStoreManager().getUserListOfRole(potentialOwnerRole);

            List<String> allPotentialOwners = new ArrayList<String>(Arrays.asList(assignableUsersArray));

            OrganizationalEntityDAO actualOwner = getActualOwner(task);

            if (excludeActualOwner && actualOwner != null) {
                allPotentialOwners.remove(actualOwner.getName());
            }

            return allPotentialOwners;

        } catch (RegistryException e) {
            throw new HumanTaskRuntimeException("Cannot locate user realm for tenant id " +
                                                task.getTenantId());
        } catch (UserStoreException e) {
            throw new HumanTaskRuntimeException("Error retrieving the UserStoreManager " +
                                                task.getTenantId(), e);
        }
    }

    /**
     * Returns the actual owner OrganizationalEntityDAO of the given task.
     *
     * @param task : The task object.
     * @return : The actual owner of the task IF exists, null otherwise. The caller should always
     *           check for null in the return value.
     */
    public static OrganizationalEntityDAO getActualOwner(TaskDAO task) {

        OrganizationalEntityDAO actualOwnerOrgEntity = null;
        List<OrganizationalEntityDAO> actualOwner =
                getOrgEntitiesForRole(task,
                                      GenericHumanRoleDAO.GenericHumanRoleType.ACTUAL_OWNER);

        // There should be only 1 actual owner for the task if any exists.
        if(actualOwner != null && actualOwner.size() ==1) {
                 actualOwnerOrgEntity = actualOwner.get(0);
        }

        return actualOwnerOrgEntity;
    }
}
