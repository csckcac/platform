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

import org.wso2.carbon.humantask.core.dao.HumanTaskJobDAO;

import javax.persistence.*;

/**
 * Human task Job.
 */
@Entity
@Table(name = "HTJOB")
public class HumanTaskJob extends OpenJPAEntity implements HumanTaskJobDAO {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "NODEID")
    private String nodeId;

    @Column(name = "NAME")
    private String name;

    @Column(name = "TIME", nullable = false)
    private Long time = 0L;

    @Column(name = "SCHEDULED", nullable = false)
    private boolean scheduled;

    @Column(name = "TRANSACTED", nullable = false)
    private boolean transacted;

    @Column(name = "DETAILS", length = 4096)
    private String details;

    //    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.REFRESH})
//    private Task task;
    @Column(name = "TASKID", nullable = false)
    private Long taskId;

    @Column(name = "TYPE", nullable = false)
    private String type;

    /**
     * Get primary key
     *
     * @return primary key
     */
    public Long getId() {
        return id;
    }

    /**
     * Set primary key
     *
     * @param id primary key
     */
    public void setId(Long id) {
        this.id = id;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public boolean isScheduled() {
        return scheduled;
    }

    public void setScheduled(boolean scheduled) {
        this.scheduled = scheduled;
    }

    public boolean isTransacted() {
        return transacted;
    }

    public void setTransacted(boolean transacted) {
        this.transacted = transacted;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * Delete the object
     */
    public void delete() {
//        System.out.println("HumanTaskJob - delete");
//        HumanTaskServiceComponent.getHumanTaskServer().getDaoConnectionFactory().getConnection().getEntityManager().remove(this);
////        getEntityManager().remove(this);
    }
}
