/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.messagebox.qpid;

import org.wso2.carbon.messagebox.MessageBoxException;
import org.wso2.carbon.messagebox.MessageBoxService;
import org.wso2.carbon.messagebox.SQSMessage;
import org.wso2.carbon.messagebox.internal.qpid.JMSMessageBoxService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TestJMSMessageBoxService /*extends TestCase*/ {
    /**
     * Send messages
     * Receive messages
     * Delete received messages
     * Try receiving messages again
     *
     * @throws MessageBoxException
     */
   /* public void testScenario1() throws MessageBoxException {
        System.out.println("Test scenario 1...");
        String queueName = "MyQueueAAA";
        int numberOfMessages = 10;
        int defaultVisibilityTimeout = 15000;
        deleteMessageBox(queueName);
        sendMessages(queueName, numberOfMessages, defaultVisibilityTimeout);
        List<String> receiptHandlers = receiveMessages(queueName, numberOfMessages, defaultVisibilityTimeout);
        deleteMessages(queueName, receiptHandlers);
        System.out.println("No messages should be received now...");
        receiveMessages(queueName, numberOfMessages, defaultVisibilityTimeout);

    }
*/
    /**
     * Send messages with default visibility time out to zero
     * Receive messages
     * Try deleting received messages
     * Receive again to check if messages are deleted
     * Change visibility time out to 5000
     * Delete received messages again
     * Try receiving messages again
     *
     * @throws MessageBoxException
     */
   /* public void testScenario2() throws MessageBoxException {
        System.out.println("Test scenario 2...");
        String queueName = "MyQueueB";
        int numberOfMessages = 8;
        int defaultVisibilityTimeout = 0;
        deleteMessageBox(queueName);
        sendMessages(queueName, numberOfMessages, defaultVisibilityTimeout);
        List<String> receiptHandlers = receiveMessages(queueName, numberOfMessages, defaultVisibilityTimeout);
        deleteMessages(queueName, receiptHandlers);

        defaultVisibilityTimeout = 25000;
        receiptHandlers = receiveMessages(queueName, numberOfMessages, defaultVisibilityTimeout);
        changeVisibilityTimeout(queueName, receiptHandlers, defaultVisibilityTimeout);
        deleteMessages(queueName, receiptHandlers);
        receiveMessages(queueName, numberOfMessages, defaultVisibilityTimeout);

    }*/

    public void sendMessages(String queueName, int numberOfMessages, long visibilityTimeout)
            throws MessageBoxException {

        MessageBoxService messageBoxService = new JMSMessageBoxService();
        // create SQSMessage box
        messageBoxService.createMessageBox(queueName, visibilityTimeout);

        // send messages
        SQSMessage SQSMessage = new SQSMessage();
        for (int i = 0; i < numberOfMessages; i++) {
            SQSMessage.setBody("my SQSMessage " + i);
            SQSMessage.setMd5ofMessageBody("md5ofmessage");
            SQSMessage.setMessageId(UUID.randomUUID().toString());
            messageBoxService.putMessage(queueName, SQSMessage);
        }
    }

    public List<String> receiveMessages(String queueName, int numberOfMessages,
                                        long visibilityTimeout)
            throws MessageBoxException {
        MessageBoxService messageBoxService = new JMSMessageBoxService();
        List<SQSMessage> SQSMessageList = messageBoxService.receiveMessage(queueName, numberOfMessages, visibilityTimeout, null);
        List<String> receiptHandlers = new ArrayList<String>();
        if (SQSMessageList != null) {
            for (SQSMessage SQSMessageReceived : SQSMessageList) {
                System.out.println("message received ==> " + SQSMessageReceived.getBody() +
                                   " with receipt handler " + SQSMessageReceived.getReceiptHandle());
                receiptHandlers.add(SQSMessageReceived.getReceiptHandle());
            }
        }
        if (SQSMessageList !=null && SQSMessageList.size() == 0) {
            System.out.println("No messages received from " + queueName);
        }
        return receiptHandlers;
    }

    public void deleteMessages(String queueName, List<String> receiptHandlers)
            throws MessageBoxException {
        MessageBoxService messageBoxService = new JMSMessageBoxService();
        for (String receiptHandler : receiptHandlers) {
            messageBoxService.deleteMessage(queueName, receiptHandler);
            System.out.println("trying to delete message with receipt handler ==> " + receiptHandler);

        }
    }

    public void changeVisibilityTimeout(String queueName, List<String> receiptHandlers,
                                        long visibilityTimeout)
            throws MessageBoxException {
        MessageBoxService messageBoxService = new JMSMessageBoxService();
        for (String receiptHandler : receiptHandlers) {
            messageBoxService.changeVisibility(queueName, receiptHandler, visibilityTimeout);
            System.out.println("default visibility changed with receipt handler ==> " +
                               receiptHandler + " to value " + visibilityTimeout);
        }
    }

    public void deleteMessageBox(String queueName) throws MessageBoxException {
        MessageBoxService messageBoxService = new JMSMessageBoxService();
        messageBoxService.deleteMessageBox(queueName);
    }

   /* public void t1estDeleteMessageBox() throws MessageBoxException {
        String queueName = "MyQueueC";
        int numberOfMessages = 12;
        int defaultVisibilityTimeout = 0;
        sendMessages(queueName, numberOfMessages, defaultVisibilityTimeout);
        deleteMessageBox(queueName);

    }*/
}
