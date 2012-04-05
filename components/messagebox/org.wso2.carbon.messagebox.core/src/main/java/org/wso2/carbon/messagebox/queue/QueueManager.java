/*
 * Copyright 2004,2005 The Apache Software Foundation.
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

package org.wso2.carbon.messagebox.queue;

import org.wso2.carbon.messagebox.MessageBoxException;

import java.util.List;

public interface QueueManager {

    public List<Queue> getAllQueues() throws MessageBoxException;

    public void addQueue(String queueName, String createdFrom) throws MessageBoxException;

    public List<QueueUserPermission> getQueueUserPermissions(String queueName)
            throws MessageBoxException;

    public List<QueueRolePermission> getQueueRolePermissions(String queueName)
            throws MessageBoxException;

    public void updateUserPermission(List<QueueUserPermission> queueUserPermissions,
                                     String queueName)
            throws MessageBoxException;

    public void updateRolePermission(List<QueueRolePermission> queueRolePermissions,
                                     String queueName)
            throws MessageBoxException;

    public void deleteQueue(String queueName) throws MessageBoxException;

    public void setQueueUpdatedTime(String queueName) throws MessageBoxException;

    public boolean isQueueExists(String queueName) throws MessageBoxException;
}
