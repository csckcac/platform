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

package org.wso2.carbon.humantask.core.dao.jpa.openjpa.model;

import org.wso2.carbon.humantask.core.dao.EventDAO;
import org.wso2.carbon.humantask.core.dao.TaskDAO;
import org.wso2.carbon.humantask.core.dao.TaskStatus;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

/**
 * Row presentation of a human task event.
 */
@Entity
@Table(name = "EVENT")
public class Event implements EventDAO {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "TIMESTAMP", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date timeStamp;

    @Column(name = "TYPE", nullable = false)
    private String type;

    @Column(name = "DETAILS", nullable = true)
    private String details;

    @Column(name = "USER", nullable = false)
    private String user;

    @Enumerated(EnumType.STRING)
    @Column(name = "OLD_STATE", nullable = true)
    private TaskStatus oldState;

    @Enumerated(EnumType.STRING)
    @Column(name = "NEW_STATE", nullable = true)
    private TaskStatus newState;

    @ManyToOne
    private Task task;

    public Event() {
    }

    public Long getId() {
        return id;
    }

    @Override
    public Date getTimeStamp() {
        return timeStamp;
    }

    @Override
    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String getDetails() {
        return details;
    }

    @Override
    public void setDetails(String details) {
        this.details = details;
    }

    @Override
    public String getUser() {
        return user;
    }

    @Override
    public void setUser(String user) {
        this.user = user;
    }

    @Override
    public Task getTask() {
        return task;
    }

    @Override
    public void setTask(TaskDAO task) {
        this.task = (Task)task;
    }

    @Override
    public TaskStatus getOldState() {
        return oldState;
    }

    @Override
    public void setOldState(TaskStatus oldState) {
        this.oldState = oldState;
    }

    @Override
    public TaskStatus getNewState() {
        return newState;
    }

    @Override
    public void setNewState(TaskStatus newState) {
        this.newState = newState;
    }
}
