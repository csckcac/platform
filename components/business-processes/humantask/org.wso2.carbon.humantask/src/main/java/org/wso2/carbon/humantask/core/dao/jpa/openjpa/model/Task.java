/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.humantask.core.dao.jpa.openjpa.model;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Element;
import org.wso2.carbon.humantask.core.dao.AttachmentDAO;
import org.wso2.carbon.humantask.core.dao.CommentDAO;
import org.wso2.carbon.humantask.core.dao.DeadlineDAO;
import org.wso2.carbon.humantask.core.dao.EventDAO;
import org.wso2.carbon.humantask.core.dao.GenericHumanRoleDAO;
import org.wso2.carbon.humantask.core.dao.MessageDAO;
import org.wso2.carbon.humantask.core.dao.OrganizationalEntityDAO;
import org.wso2.carbon.humantask.core.dao.PresentationDescriptionDAO;
import org.wso2.carbon.humantask.core.dao.PresentationNameDAO;
import org.wso2.carbon.humantask.core.dao.PresentationParameterDAO;
import org.wso2.carbon.humantask.core.dao.PresentationSubjectDAO;
import org.wso2.carbon.humantask.core.dao.TaskDAO;
import org.wso2.carbon.humantask.core.dao.TaskStatus;
import org.wso2.carbon.humantask.core.dao.TaskType;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityTransaction;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * The task model object. Represents the task data.
 */
@Entity
@Table(name = "TASK")
public class Task extends OpenJPAEntity implements TaskDAO {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * The tenant id of the tenant which this task belongs to.
     */
    @Column(name = "TENANT_ID", nullable = false)
    private Integer tenantId;

    /**
     * Task name must be a QName. To make it easy to store in DB we are using string type.
     * When setting the task name, we convert QName to string. When getting name, caller must
     * convert name back to a QName.
     */
    @Column(name = "NAME", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "TYPE", nullable = false)
    private TaskType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false)
    private TaskStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS_BEFORE_SUSPENSION", nullable = true)
    private TaskStatus statusBeforeSuspension;

    /**
     * Tasks priority. 0 is the highest priority.
     * If this field is null it means task's priority is unspecified.
     */
    @Column(name = "PRIORITY", nullable = false)
    private Integer priority = 5;


    /**
     * The list of sub tasks for a given task
     */
    @OneToMany(targetEntity = Task.class, mappedBy = "parentTask", fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
    private List<TaskDAO> subTasks = new ArrayList<TaskDAO>();

    /**
     * The parent task.
     */
    @ManyToOne
    private Task parentTask;

    /**
     * GenericHumanRole has a type and GenericHumanRole has many-to-many relationship with OrganizationalEntity.
     * Through the GenericHumanRole type we get the assigned OrganizationalEntities for each role.
     */
    @OneToMany(targetEntity = GenericHumanRole.class, mappedBy = "task", fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
    private List<GenericHumanRoleDAO> humanRoles = new ArrayList<GenericHumanRoleDAO>();

    /**
     * Time related fields.
     * Created On - Task creation time. Set at the task creation
     * Activation time - The time in UTC when the task has been activated.
     * Expiration time - The time in UTC when the task will expire.
     */
    @Column(name = "CREATED_ON")
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date createdOn;

    @Column(name = "UPDATED_ON")
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date updatedOn;

    @Column(name = "ACTIVATION_TIME")
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date activationTime;


    @Column(name = "EXPIRATION_TIME")
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date expirationTime;


    @Column(name = "START_BY_TIME")
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date startByTime;


    @Column(name = "COMPLETE_BY_TIME")
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date completeByTime;

    /**
     * input
     */
    @Column(name = "INPUT_MESSAGE")
    @OneToOne(targetEntity = Message.class, fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
    private MessageDAO inputMessage;

    /**
     * output
     */
    @Column(name = "OUTPUT_MESSAGE")
    @OneToOne(targetEntity = Message.class, fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
    private MessageDAO outputMessage;

    /**
     * failure
     */
    @Column(name = "FAILURE_MESSAGE")
    @OneToOne(targetEntity = Message.class, fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
    private MessageDAO failureMessage;

    /**
     * Comments by people who can influence on task's progress
     */
    @OneToMany(targetEntity = Comment.class, mappedBy = "task", fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
    private List<CommentDAO> comments = new ArrayList<CommentDAO>();

    /**
     * Attachments attached during the life cycle of the task
     */
    @OneToMany(targetEntity = Attachment.class, mappedBy = "task", fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
    private List<AttachmentDAO> attachments = new ArrayList<AttachmentDAO>();

    /**
     * Skippable flag.
     */
    @Column(name = "SKIPABLE")
    private Boolean skipable;

    /**
     * Escalated flag.
     */
    @Column(name = "ESCALATED")
    private Boolean escalated;

    /**
     * Task presentation parameters.
     */
    @OneToMany(targetEntity = PresentationParameter.class, mappedBy = "task",
            fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
    private List<PresentationParameterDAO> presentationParameters =
            new ArrayList<PresentationParameterDAO>();

    /**
     * Task presentation subjects.
     */
    @OneToMany(targetEntity = PresentationSubject.class, mappedBy = "task",
            fetch = FetchType.EAGER, cascade = {CascadeType.ALL})
    private List<PresentationSubjectDAO> presentationSubjects = new ArrayList<PresentationSubjectDAO>();

    /**
     * Task presentation names.
     */
    @OneToMany(targetEntity = PresentationName.class, mappedBy = "task",
            fetch = FetchType.EAGER, cascade = {CascadeType.ALL})
    private List<PresentationNameDAO> presentationNames = new ArrayList<PresentationNameDAO>();

    /**
     *
     */
    @OneToMany(targetEntity = PresentationDescription.class, mappedBy = "task",
            fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
    private List<PresentationDescriptionDAO> presentationDescriptions =
            new ArrayList<PresentationDescriptionDAO>();

    /**
     * Deadlines.
     * This version use a list to store dealines. Need to check whether we can use
     * startDeadline and completionDeadline attributes separately without using many-to-one
     * relationship.
     */
    @OneToMany(targetEntity = Deadline.class, mappedBy = "task", fetch = FetchType.EAGER, cascade = {CascadeType.ALL})
    private List<DeadlineDAO> deadlines = new ArrayList<DeadlineDAO>();

    /**
     * Events which records changes in task. For example change from CREATED state to RESERVED.
     */
    @OneToMany(targetEntity = Event.class, mappedBy = "task", fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
    private List<EventDAO> events = new ArrayList<EventDAO>();

    public Task() {
    }

    public Task(QName name, TaskType type, Integer tenantId) {
        this.createdOn = new Date();
        this.status = TaskStatus.UNDEFINED;
        this.name = name.toString();
        this.type = type;
        this.tenantId = tenantId;
    }

    /**
     * Get the Task ID.
     *
     * @return The task id.
     */
    @Override
    public Long getId() {
        return id;
    }

    /**
     * The task id.
     *
     * @param id primary key
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * This method assumes that external modules only set input message at the creation of task
     *
     * @param input Input MessageDAO
     */
    @Override
    public void setInputMessage(MessageDAO input) {
        inputMessage = input;
    }

    @Override
    public MessageDAO getFailureMessage() {
        return failureMessage;
    }

    @Override
    public void setFailureMessage(MessageDAO failureMessage) {
        this.failureMessage = failureMessage;
    }

    @Override
    public void setTenantId(Integer tenantId) {
        this.tenantId = tenantId;
    }

    @Override
    public Integer getTenantId() {
        return this.tenantId;
    }

    @Override
    public void addPresentationParameter(PresentationParameterDAO param) {
        presentationParameters.add(param);
    }

    @Override
    public List<PresentationParameterDAO> getPresentationParameters() {
        return presentationParameters;
    }

    @Override
    public void addPresentationName(PresentationNameDAO preName) {
        this.presentationNames.add(preName);
    }

    @Override
    public void addPresentationSubject(PresentationSubjectDAO preSubject) {
        this.presentationSubjects.add(preSubject);
    }

    @Override
    public void addPresentationDescription(PresentationDescriptionDAO preDesc) {
        this.presentationDescriptions.add(preDesc);
    }

    @Override
    public void addHumanRole(GenericHumanRoleDAO humanRole) {
        this.humanRoles.add(humanRole);
    }

    @Override
    public List<GenericHumanRoleDAO> getHumanRoles() {
        List<GenericHumanRoleDAO> humanRoleDAOs = new ArrayList<GenericHumanRoleDAO>();
        if (this.humanRoles != null) {
            humanRoleDAOs.addAll(this.humanRoles);
        }
        return humanRoleDAOs;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public TaskType getType() {
        return type;
    }

    public void setType(TaskType type) {
        this.type = type;
    }

    @Override
    public TaskStatus getStatusBeforeSuspension() {
        return statusBeforeSuspension;
    }

    @Override
    public void setStatusBeforeSuspension(TaskStatus statusBeforeSuspension) {
        this.statusBeforeSuspension = statusBeforeSuspension;
    }

    @Override
    public Integer getPriority() {
        return priority;
    }

    @Override
    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    @Override
    public Date getCreatedOn() {
        return new Date(createdOn.getTime());
    }

    @Override
    public void setCreatedOn(Date createdOn) {
        this.createdOn = new Date(createdOn.getTime());
    }

    @Override
    public Date getUpdatedOn() {
        return updatedOn;
    }

    @Override
    public void setUpdatedOn(Date updatedOn) {
        this.updatedOn = updatedOn;
    }

    @Override
    public Date getActivationTime() {
        if (activationTime != null) {
            return new Date(activationTime.getTime());
        } else {
            return null;
        }
    }

    @Override
    public void setActivationTime(Date activationTime) {
        if (activationTime != null) {
            this.activationTime = new Date(activationTime.getTime());
        }
    }

    @Override
    public Date getExpirationTime() {
        if (expirationTime != null) {
            return new Date(expirationTime.getTime());
        } else {
            return null;
        }
    }

    @Override
    public void setExpirationTime(Date expirationTime) {
        if (expirationTime != null) {
            this.expirationTime = new Date(expirationTime.getTime());
        }
    }

    @Override
    public Date getStartByTime() {
        return startByTime;
    }

    @Override
    public void setStartByTime(Date startByTime) {
        this.startByTime = startByTime;
    }

    @Override
    public Date getCompleteByTime() {
        return completeByTime;
    }

    @Override
    public void setCompleteByTime(Date completeByTime) {
        this.completeByTime = completeByTime;
    }

    @Override
    public MessageDAO getOutputMessage() {
        return outputMessage;
    }

    @Override
    public void setOutputMessage(MessageDAO outputMessage) {
        this.outputMessage = outputMessage;
    }

    @Override
    public List<AttachmentDAO> getAttachments() {
        return attachments;
    }

    @Override
    public void setAttachments(List<AttachmentDAO> attachments) {
        this.attachments = attachments;
    }

    @Override
    public Boolean isEscalated() {
        return escalated;
    }

    @Override
    public void setEscalated(Boolean escalated) {
        this.escalated = escalated;
    }

    @Override
    public List<PresentationSubjectDAO> getPresentationSubjects() {
        return presentationSubjects;
    }


    @Override
    public List<PresentationNameDAO> getPresentationNames() {
        return presentationNames;
    }

    @Override
    public List<PresentationDescriptionDAO> getPresentationDescriptions() {
        return presentationDescriptions;
    }

    @Override
    public List<DeadlineDAO> getDeadlines() {
        return deadlines;
    }

    @Override
    public void addDeadline(DeadlineDAO deadlineDAO) {
        deadlines.add(deadlineDAO);
    }

    @Override
    public List<EventDAO> getEvents() {
        return events;
    }

    @Override
    public void setEvents(List<EventDAO> events) {
        this.events = events;
    }

    @Override
    public void addEvent(EventDAO event) {
        this.events.add(event);
    }

    @Override
    public void persistEvent(EventDAO event) {
        event.setTask(this);
        this.getEvents().add(event);
    }

    @Override
    public List<TaskDAO> getSubTasks() {
        return subTasks;
    }

    @Override
    public void setSubTasks(List<TaskDAO> subTasks) {
        this.subTasks = subTasks;
    }

    @Override
    public Task getParentTask() {
        return parentTask;
    }

    @Override
    public void setParentTask(TaskDAO parentTask) {
        this.parentTask = (Task) parentTask;
    }

    @Override
    public TaskStatus getStatus() {
        return status;
    }

    @Override
    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    @Override
    public void start() {
        this.setStatus(TaskStatus.IN_PROGRESS);
        getEntityManager().merge(this);
    }

    @Override
    public void stop() {
        this.setStatus(TaskStatus.RESERVED);
        getEntityManager().merge(this);
    }

    @Override
    public void suspend() {
        this.setStatusBeforeSuspension(this.getStatus());
        this.setStatus(TaskStatus.SUSPENDED);
        getEntityManager().merge(this);
    }

    @Override
    public void complete(MessageDAO response) {
        response.setTask(this);
        this.setOutputMessage(response);
        this.setStatus(TaskStatus.COMPLETED);
        getEntityManager().merge(this);
    }

    @Override
    public void claim(OrganizationalEntityDAO caller) {
        List<OrganizationalEntityDAO> organizationalEntities = new ArrayList<OrganizationalEntityDAO>();
        organizationalEntities.add(caller);

        GenericHumanRoleDAO actualOwnerRole = new GenericHumanRole();
        actualOwnerRole.setType(GenericHumanRoleDAO.GenericHumanRoleType.ACTUAL_OWNER);
        actualOwnerRole.setOrgEntities(organizationalEntities);
        actualOwnerRole.setTask(this);

        caller.addGenericHumanRole(actualOwnerRole);

        this.addHumanRole(actualOwnerRole);
        this.setStatus(TaskStatus.RESERVED);
    }

    @Override
    public void exit() {
        this.setStatus(TaskStatus.EXITED);
        getEntityManager().merge(this);
    }

    @Override
    public void delegate(OrganizationalEntityDAO orgEntity) {
        claim(orgEntity);
    }

    @Override
    public CommentDAO persistComment(CommentDAO comment) {
        List<CommentDAO> originalCommentList = new ArrayList<CommentDAO>(this.getComments());
        comment.setTask(this);
        this.getComments().add(comment);
        getEntityManager().merge(this);
        List<CommentDAO> newCommentList = this.getComments();
        if (newCommentList.size() - originalCommentList.size() == 1) {
            return (CommentDAO) ListUtils.subtract(newCommentList, originalCommentList).get(0);
        } else {
            return null;
        }
    }

    @Override
    public void deleteComment(Long commentId) {

        for (Iterator<CommentDAO> i = this.getComments().iterator(); i.hasNext(); ) {
            CommentDAO comment = i.next();
            if (comment.getId().equals(commentId)) {
                getEntityManager().remove(comment);
                i.remove();
                getEntityManager().merge(this);
                break;
            }
        }
    }

    @Override
    public void forward(OrganizationalEntityDAO orgEntity) {
        throw new UnsupportedOperationException("The delegate operation is no supported currently.");
    }

    @Override
    public List<CommentDAO> getComments() {
        return comments;
    }

    @Override
    public String getTaskDescription(String contentType) {
        String presentationDescriptionString = null;
        for (PresentationDescriptionDAO preDesc : this.getPresentationDescriptions()) {
            if (preDesc.getContentType().trim().equals(contentType.trim())) {
                presentationDescriptionString = preDesc.getValue();
                break;
            }
        }
        return presentationDescriptionString;
    }

    @Override
    public void release() {
        for (Iterator<GenericHumanRoleDAO> iterator = getHumanRoles().iterator();
             iterator.hasNext(); ) {
            GenericHumanRoleDAO ghr = iterator.next();
            if (ghr.getType().equals(GenericHumanRoleDAO.GenericHumanRoleType.ACTUAL_OWNER)) {
                for (OrganizationalEntityDAO orgEntity : ghr.getOrgEntities()) {
                    orgEntity.getGenericHumanRoles().clear();
                    orgEntity.setGenericHumanRoles(null);
                }

                ghr.getOrgEntities().clear();
                ghr.setOrgEntities(null);
                ghr.setTask(null);
                iterator.remove();
                getEntityManager().remove(ghr);
                break;
            }
        }
        this.setStatus(TaskStatus.READY);
    }

    @Override
    public void remove() {
        getEntityManager().remove(this.getInputMessage());
        getEntityManager().remove(this.getHumanRoles());
        getEntityManager().remove(this);
    }

    @Override
    public void resume() {
        this.setStatus(this.getStatusBeforeSuspension());
        this.setStatusBeforeSuspension(null);
        getEntityManager().merge(this);
    }

    @Override
    public void activate() {
        this.setStatus(TaskStatus.READY);
        getEntityManager().merge(this);
    }

    @Override
    public void skip() {
        this.setStatus(TaskStatus.OBSOLETE);
    }

    @Override
    public void updateAndPersistComment(Long commentId, String newComment, String modifiedBy) {
        for (CommentDAO comment : this.getComments()) {
            if (comment.getId().equals(commentId)) {
                comment.setModifiedBy(modifiedBy);
                comment.setModifiedDate(new Date());
                comment.setCommentText(newComment);
                break;
            }
        }
        getEntityManager().merge(this);
    }

    @Override
    public Boolean isSkipable() {
        return this.skipable;
    }

    @Override
    public MessageDAO getInputMessage() {
        return inputMessage;
    }

    @Override
    public void setSkipable(Boolean skipable) {
        this.skipable = skipable;
    }

    @Override
    public void nominate(List<OrganizationalEntityDAO> nominees) {
        if (nominees != null && nominees.size() > 0) {
            if (nominees.size() == 1) {
                OrganizationalEntityDAO nominee = nominees.get(0);
                if (OrganizationalEntityDAO.OrganizationalEntityType.GROUP.
                        equals(nominee.getOrgEntityType())) {
                    //TODO - implement me.
                    throw new UnsupportedOperationException("Group nominees are not supported");

                } else {
                    GenericHumanRoleDAO actualOwnerRole = new GenericHumanRole();
                    actualOwnerRole.setType(GenericHumanRole.GenericHumanRoleType.ACTUAL_OWNER);
                    actualOwnerRole.setTask(this);
                    actualOwnerRole.setOrgEntities(nominees);

                    for (OrganizationalEntityDAO orgE : nominees) {
                        orgE.addGenericHumanRole(actualOwnerRole);
                    }

                    this.addHumanRole(actualOwnerRole);
                    this.setStatus(TaskStatus.RESERVED);
                }

            } else {
                throw new UnsupportedOperationException("Multiple nominees are not supported yet.");
            }

        }

        getEntityManager().merge(this);
    }

    @Override
    public void persistPriority(Integer newPriority) {
        this.setPriority(newPriority);
        this.getEntityManager().merge(this);
    }

    @Override
    public void fail(String faultName, Element faultData) {
        if (faultData != null && StringUtils.isNotEmpty(faultName)) {
            MessageDAO faultMessage = new Message();
            faultMessage.setMessageType(MessageDAO.MessageType.FAILURE);
            faultMessage.setData(faultData);
            faultMessage.setName(QName.valueOf(faultName));
            faultMessage.setTask(this);
            this.setFailureMessage(faultMessage);
        }
        this.setStatus(TaskStatus.FAILED);
        this.getEntityManager().merge(this);
    }

    @Override
    public void deleteFault() {
        this.getEntityManager().remove(this.getFailureMessage());
        this.setFailureMessage(null);
        this.getEntityManager().merge(this);
    }

    @Override
    public void deleteOutput() {
        this.getEntityManager().remove(this.getOutputMessage());
        this.setOutputMessage(null);
        this.getEntityManager().merge(this);
    }

    @Override
    public void persistFault(String faultName, Element faultData) {
        if (StringUtils.isNotEmpty(faultName) && faultData != null) {
            MessageDAO faultMessage = new Message();
            faultMessage.setMessageType(MessageDAO.MessageType.FAILURE);
            faultMessage.setData(faultData);
            faultMessage.setName(QName.valueOf(faultName));
            faultMessage.setTask(this);
            this.setFailureMessage(faultMessage);
            this.getEntityManager().merge(this);
        }
    }

    @Override
    public void persistOutput(String outputName, Element outputData) {
        if (StringUtils.isNotEmpty(outputName) && outputData != null) {
            MessageDAO message = new Message();
            message.setMessageType(MessageDAO.MessageType.OUTPUT);
            message.setData(outputData);
            message.setName(QName.valueOf(outputName));
            message.setTask(this);
            this.setOutputMessage(message);
            this.getEntityManager().merge(this);
        }
    }

    @Override
    public void persistToPotentialOwners(OrganizationalEntityDAO delegatee) {
        for (GenericHumanRoleDAO role : this.getHumanRoles()) {
            if (GenericHumanRoleDAO.GenericHumanRoleType.POTENTIAL_OWNERS.equals(role.getType())) {
                delegatee.addGenericHumanRole(role);
                role.addOrgEntity(delegatee);
                break;
            }
        }
    }

    @Override
    public GenericHumanRoleDAO getGenericHumanRole(GenericHumanRoleDAO.GenericHumanRoleType type) {
        GenericHumanRoleDAO matchingRole = null;
        for (GenericHumanRoleDAO role : getHumanRoles()) {
            if (type.equals(role.getType())) {
                matchingRole = role;
            }
        }

        return matchingRole;
    }

    @Override
    public void replaceOrgEntitiesForLogicalPeopleGroup(
            GenericHumanRoleDAO.GenericHumanRoleType type,
            List<OrganizationalEntityDAO> orgEntities) {
        for (GenericHumanRoleDAO role : this.getHumanRoles()) {
            if (type.equals(role.getType())) {
                role.getOrgEntities().clear();

                for (OrganizationalEntityDAO orgEntity : orgEntities) {
                    orgEntity.addGenericHumanRole(role);
                    role.addOrgEntity(orgEntity);
                }

                break;
            }
        }
        this.release();
    }


    @PrePersist
    @PreUpdate
    private void persistLastUpdated() {
        this.setUpdatedOn(new Date());
    }
}
