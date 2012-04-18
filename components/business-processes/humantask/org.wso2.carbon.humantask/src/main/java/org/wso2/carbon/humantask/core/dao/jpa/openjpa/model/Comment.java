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

import org.apache.commons.lang.Validate;
import org.wso2.carbon.humantask.core.dao.CommentDAO;
import org.wso2.carbon.humantask.core.dao.TaskDAO;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

/**
 * The task comment JPA implementation..
 */
@Entity
@Table(name = "TASK_COMMENT")
public class Comment implements CommentDAO {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "COMMENTED_ON")
    @Temporal(TemporalType.TIMESTAMP)
    private Date commentedDate;

    @Column(name = "MODIFIED_ON", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date modifiedDate;

    @Column(name = "COMMENT_TEXT", length = 8192)
    private String commentText;

    @Column(name = "COMMENTED_BY", length = 100)
    private String commentedBy;

    @Column(name = "MODIFIED_BY", length = 100, nullable = true)
    private String modifiedBy;

    @ManyToOne
    private Task task;

    public Comment() {
    }

    /**
     * The comment constructor.
     *
     * @param commentText : The comment text.
     * @param commentedBy : The commented by user id.
     */
    public Comment(String commentText, String commentedBy) {
        Validate.notNull(commentText);
        Validate.notNull(commentedBy);
        this.commentText = commentText;
        this.commentedBy = commentedBy;
        this.commentedDate = new Date();
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public Date getCommentedDate() {
        return commentedDate;
    }

    @Override
    public void setCommentedDate(Date commentedDate) {
        this.commentedDate = commentedDate;
    }

    @Override
    public String getCommentText() {
        return commentText;
    }

    @Override
    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    @Override
    public String getCommentedBy() {
        return commentedBy;
    }

    @Override
    public void setCommentedBy(String commentedBy) {
        this.commentedBy = commentedBy;
    }

    @Override
    public TaskDAO getTask() {
        return task;
    }

    @Override
    public void setTask(TaskDAO task) {
        this.task = (Task) task;
    }

    @Override
    public Date getModifiedDate() {
        return modifiedDate;
    }

    @Override
    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    @Override
    public String getModifiedBy() {
        return modifiedBy;
    }

    @Override
    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }
}
