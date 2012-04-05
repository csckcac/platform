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

import java.util.Date;

/**
 * DAO representation of the task comment,
 */
public interface CommentDAO {

    /**
     * @return : The comment id.
     */
    public Long getId();

    /**
     * @return : The comment time
     */
    public Date getCommentedDate();

    /**
     * @param date : The comment date to set.
     */
    public void setCommentedDate(Date date);

    /**
     * @return : The comment text
     */
    public String getCommentText();

    /**
     * @param commentText : The comment text to set.
     */
    public void setCommentText(String commentText);

    /**
     * @return : The commented user id.
     */
    public String getCommentedBy();

    /**
     * @param commentedBy : The id of the user who's adding the comment to set.
     */
    public void setCommentedBy(String commentedBy);

    /**
     * @return : The task which this comment belongs to.
     */
    public TaskDAO getTask();

    /**
     * @param task : The task to set.
     */
    public void setTask(TaskDAO task);

    /**
     * @return : The last modified date of the comment.
     */
    public Date getModifiedDate();

    /**
     * @param modifiedDate : The last modified date to set.
     */
    public void setModifiedDate(Date modifiedDate);

    /**
     * @return : The last modified user name.
     */
    public String getModifiedBy();

    /**
     * @param modifiedBy : The last modified user name.
     */
    public void setModifiedBy(String modifiedBy);

}