/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.andes.server.cassandra;


/**
 * <code>CassandraQueueMessage</code> holds the message meta data that are transferred between
 * Global Queues and User queues.
 */
public class CassandraQueueMessage {

    private String messageId;
    private String queue;
    private byte[] message;

    public CassandraQueueMessage(String messageId , String queue , byte[] data) {
        this.messageId = messageId;
        this.queue = queue;
        this.message = data;
    }

    /**
     * Get the Queue Name for the the of the message
     * @return queue name
     */
    public String getQueue() {
        return queue;
    }

    /**
     * Get Meta data content of the message
     * @return Meta data content as byte[]
     */
    public byte[] getMessage() {
        return message;
    }


    /**
     * Get qpid message id of the message
     * @return  message id
     */
    public String getMessageId() {
        return messageId;
    }
}
