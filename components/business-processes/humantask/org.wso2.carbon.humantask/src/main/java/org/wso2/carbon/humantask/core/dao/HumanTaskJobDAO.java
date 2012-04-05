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

public interface HumanTaskJobDAO {
    /**
     * Get primary key
     * @return primary key
     */
    public Long getId();

    /**
     * Set primary key
     * @param jobId Job ID
     */
    public void setId(Long jobId);

    /**
     * Get the assigned node id of the job
     * @return node id
     */
    public String getNodeId();

    /**
     * Set the assigned node id of the job
     * @param id node id
     */
    public void setNodeId(String id);

    /**
     * Get the name of the job
     * @return name
     */
    public String getName();

    /**
     * Set the name of the job
     * @param name Name of the job
     */
    public void setName(String name);

    /**
     * Get scheduled time of the job execution
     * @return time
     */
    public Long getTime();

    /**
     * Set scheduled time of the job execution
     * @param time Time
     */
    public void setTime(Long time);

    /**
     * Set whether the job is scheduled
     * @param scheduled true or false
     */
    public void setScheduled(boolean scheduled);

    /**
     * Check whether the job should be executed in a transaction
     * @return whether the transaction enabled
     */
    public boolean isTransacted();

    /**
     * Set whether the job should be executed in a transaction
     * @param transacted true or false
     */
    public void setTransacted(boolean transacted);

    /**
     * Get details of the job
     * @return details
     */
    public String getDetails();

    /**
     * Set details of the job
     * @param details details
     */
    public void setDetails(String details);

    /**
     * Get the task id of the job
     * @return task id
     */
    public Long getTaskId();

    /**
     * Set the task id of the job
     * @param taskId Task id
     */
    public void setTaskId(Long taskId);

    /**
     * Get the type of the job
     * @return job type
     */
    public String getType();

    /**
     * Get the type of the job
     * @param type type of the job
     */
    public void setType(String type);

    /**
     * Delete the object
     */
    public void delete();
}
