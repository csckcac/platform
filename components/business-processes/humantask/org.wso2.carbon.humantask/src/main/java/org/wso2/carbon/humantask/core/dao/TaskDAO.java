/*
 * Copyright (c) 2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.humantask.core.dao;

import org.w3c.dom.Element;

import java.util.Date;
import java.util.List;

/**
 * The data access interface to interact with the persistable task object.
 * <p/>
 * NOTE: It is the responsibility of the caller to handle all the authorization and pre/post condition checks.
 */
public interface TaskDAO {

    public void setStatus(TaskStatus status);

    /**
     * @return : Return the taks status.
     */
    public TaskStatus getStatus();

    public TaskType getType();

    public String getName();

    /**
     * @param tenantId : The tenant Id to set.
     */
    public void setTenantId(Integer tenantId);

    /**
     * @return : The tenant id of the task.
     */
    public Integer getTenantId();

    /**
     * Starts this task. i.e Updates the task's status to IN_PROGRESS.
     */
    public void start();

    public void stop();

    public void suspend();

    public void complete(MessageDAO response);

    public void claim(OrganizationalEntityDAO caller);

    public void delegate(OrganizationalEntityDAO orgEntity);

    /**
     * Persist the provided comment to this task.
     *
     * @param comment : The comment to persist.
     * @return : The persisted comment.
     */
    public CommentDAO persistComment(CommentDAO comment);

    /**
     * Deletes the given comment  from the task.
     *
     * @param commentId : The id of the comment to be deleted.
     */
    public void deleteComment(Long commentId);

    /**
     * Forward this task to the provided Org Entity.
     *
     * @param orgEntity : The forwadee.
     */
    public void forward(OrganizationalEntityDAO orgEntity);

    /**
     * Returns the comments of this task.
     *
     * @return : This task's comment list.
     */
    public List<CommentDAO> getComments();

    public MessageDAO getInputMessage();

    public String getTaskDescription(String contentType);

    public void release();

    public void remove();

    public void resume();

    public void activate();

    public void skip();

    public void updateAndPersistComment(Long commentId, String newComment, String modifiedBy);

    public void setSkipable(Boolean skipable);

    /**
     * @return : If the task is skippable. False otherwise.
     */
    public Boolean isSkipable();

    /**
     * @return : The list of sub tasks for this task.
     */
    public List<TaskDAO> getSubTasks();

    /**
     * @param subTasks : The list of sub tasks of this task to set.
     */
    public void setSubTasks(List<TaskDAO> subTasks);

    /**
     * @return : The parent task of this task if one exists.
     */
    public TaskDAO getParentTask();

    /**
     * @param parentTask : The parent of this task to set.
     */
    public void setParentTask(TaskDAO parentTask);

    /**
     * @return : The list of attachment objects for this task.
     */
    public List<AttachmentDAO> getAttachments();

    /**
     * @param attachments : The set of attachments for this task to set.
     */
    public void setAttachments(List<AttachmentDAO> attachments);

    public Long getId();

    public List<GenericHumanRoleDAO> getHumanRoles();

    public void addHumanRole(GenericHumanRoleDAO humanRole);

    public void addPresentationParameter(PresentationParameterDAO param);

    public List<PresentationParameterDAO> getPresentationParameters();

    public void addPresentationName(PresentationNameDAO preName);

    public void addPresentationSubject(PresentationSubjectDAO preSubject);

    public void addPresentationDescription(PresentationDescriptionDAO preDesc);

    /**
     * @return : The status of the task before the task suspension. (if a suspension has happened.)
     */
    public TaskStatus getStatusBeforeSuspension();

    /**
     * @param statusBeforeSuspension : The status before suspension to set.
     */
    public void setStatusBeforeSuspension(TaskStatus statusBeforeSuspension);

    /**
     * Exit the task. i.e Set the task status to TaskStatus.Exit.
     */
    public void exit();

    /**
     * @param input : The input message of the task to set.
     */
    public void setInputMessage(MessageDAO input);

    /**
     * @return : The failure message of the task if one exists. Null otherwise
     */
    public MessageDAO getFailureMessage();

    /**
     * @param failureMessage : The failure message of the task to set.
     */
    public void setFailureMessage(MessageDAO failureMessage);

    /**
     * @return : The priority of the task
     */
    public Integer getPriority();

    /**
     * @param priority : The task priority to set.
     */
    public void setPriority(Integer priority);

    /**
     * @return : The created on date of the task.
     */
    public Date getCreatedOn();

    /**
     * @param createdOn : The created on date to set.
     */
    public void setCreatedOn(Date createdOn);

    /**
     * @return : The last updated date of the task.
     */
    public Date getUpdatedOn();

    /**
     * @param updatedOn : The last updated date of the task to set.
     */
    public void setUpdatedOn(Date updatedOn);

    /**
     * @return : The activation time of the task.
     */
    public Date getActivationTime();

    /**
     * @return : True if the task is escalated.
     */
    public Boolean isEscalated();

    /**
     * @param escalated The task escalation flag to set.
     */
    public void setEscalated(Boolean escalated);

    /**
     * @param activationTime : The activation time to set.
     */
    public void setActivationTime(Date activationTime);

    /**
     * @return : The expiration time of the task.
     */
    public Date getExpirationTime();

    /**
     * @param expirationTime : The expiration time of the task to set.
     */
    public void setExpirationTime(Date expirationTime);

    /**
     * @return : The start by time of the task.
     */
    public Date getStartByTime();

    /**
     * @param startByTime : The start by time to set.
     */
    public void setStartByTime(Date startByTime);

    /**
     * @return : The complete by time of the task.
     */
    public Date getCompleteByTime();

    /**
     * @param completeByTime : The complete by time of the task to set.
     */
    public void setCompleteByTime(Date completeByTime);

    /**
     * @return : the output message of the task.
     */
    public MessageDAO getOutputMessage();

    /**
     * @param outputMessage : The output message of the task to set.
     */
    public void setOutputMessage(MessageDAO outputMessage);

    /**
     * @return : The presentation subjects of the task.
     */
    public List<PresentationSubjectDAO> getPresentationSubjects();

    /**
     * @return : The presentation names of the task.
     */
    public List<PresentationNameDAO> getPresentationNames();

    /**
     * @return : The presentation descriptions of the task.
     */
    public List<PresentationDescriptionDAO> getPresentationDescriptions();

    /**
     * Persist a new task priority.
     *
     * @param newPriority : The new priority to set.
     */
    public void persistPriority(Integer newPriority);


    /**
     * Nominate the task
     * @param nominees : The list of nominees.
     */
    public void nominate(List<OrganizationalEntityDAO> nominees);

    /**
     * Fails this task . Changes the task's status to FAIL. Also a failure message and the failure name is persisted.
     *
     * @param faultName : The failure name.
     * @param faultData : The fault data to persist.
     */
    public void fail(String faultName, Element faultData);

    /**
     * Delete the fault message of this task.
     */
    public void deleteFault();

    /**
     * Delete the output message of this task.
     */
    public void deleteOutput();

    /**
     * Persist the fault message.
     *
     * @param faultName    : The fault name to persist. .
     * @param faultElement : The fault data.
     */
    public void persistFault(String faultName, Element faultElement);

    /**
     * Persist the output message of the task.
     *
     * @param outputName : The name of the output
     * @param outputData : The output mesage data.
     */
    public void persistOutput(String outputName, Element outputData);

    /**
     * Persist a new org entity to the potential owners.
     *
     * @param delegatee : The org entity to be persisted to potential owners.
     */
    public void persistToPotentialOwners(OrganizationalEntityDAO delegatee);

    /**
     * Gets the GenericHumanRoleDAO of the provided GenericHumanRoleType.
     *
     * @param type : The type of the required human role.
     * @return : The matching GenericHumanRoleDAO.
     */
    public GenericHumanRoleDAO getGenericHumanRole(GenericHumanRoleDAO.GenericHumanRoleType type);

    public void addDeadline(DeadlineDAO deadlineDAO);

    public List<DeadlineDAO> getDeadlines();

    /**
     * Replaces the organizational entities for the provided human role type with the
     * set of provided entities.
     * @param type : The human role type of which the org entities should be replaced.
     * @param orgEntities : The new list of organizational entities to be added to the human role
     */
    public void replaceOrgEntitiesForLogicalPeopleGroup(GenericHumanRoleDAO.GenericHumanRoleType type, List<OrganizationalEntityDAO> orgEntities);

    /**
     * Gets the task events for the particular task.
     * @return : The list of task events for a particular task.
     */
    public List<EventDAO> getEvents();

    /**
     * TODO
     * @param events
     */
    public void setEvents(List<EventDAO> events);

    /**
     * Adds a new event to the task.
     *
     * @param eventDAO : The new event to be added to the task.
     */
    public void addEvent(EventDAO eventDAO);

    /**
     * @param eventDAO : the event to be persisted.
     */
    public void persistEvent(EventDAO eventDAO);
}
