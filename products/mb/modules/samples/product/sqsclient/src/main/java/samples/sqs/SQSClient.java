package samples.sqs;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.utils.Base64;
import org.wso2.carbon.event.client.stub.generated.authentication.AuthenticationAdminServiceStub;
import org.wso2.carbon.messagebox.stub.CreateQueue;
import org.wso2.carbon.messagebox.stub.CreateQueueResponse;
import org.wso2.carbon.messagebox.stub.DeleteMessage;
import org.wso2.carbon.messagebox.stub.DeleteQueue;
import org.wso2.carbon.messagebox.stub.MessageQueueStub;
import org.wso2.carbon.messagebox.stub.Message_type0;
import org.wso2.carbon.messagebox.stub.QueueServiceStub;
import org.wso2.carbon.messagebox.stub.ReceiveMessage;
import org.wso2.carbon.messagebox.stub.ReceiveMessageResponse;
import org.wso2.carbon.messagebox.stub.SendMessage;
import org.wso2.carbon.messagebox.stub.admin.internal.MessageBoxAdminServiceStub;
import org.wso2.carbon.messagebox.stub.admin.internal.xsd.SQSKeys;
import org.wso2.carbon.utils.NetworkUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.lang.Exception;
import java.math.BigInteger;
import java.rmi.RemoteException;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

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
public class SQSClient {
    private static final Log log = LogFactory.getLog(SQSClient.class);
    public static final String QUEUE_NAME = "MyQueue";
    public static final String DEFAULT_VISIBILITY_TIMEOUT = "60";
    public static final String MAX_NUMBER_OF_MESSAGES = "10";
    private String accessKey = "40bab66dd39a783bbbc3";
    private String secretAccessKey = "783bbbc3e26ee59202564560b6892796032ff5df";

    public SQSClient() {
        try {
            System.setProperty("javax.net.ssl.trustStore", "../../repository/resources/security/wso2carbon.jks");
            System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
            //first login to the server
            String servicesString = "https://localhost:9443/services/";
            AuthenticationAdminServiceStub stub =
                    new AuthenticationAdminServiceStub(servicesString + "AuthenticationAdmin");
            stub._getServiceClient().getOptions().setManageSession(true);
            stub.login("admin", "admin", NetworkUtils.getLocalHostname());

            ServiceContext serviceContext = stub._getServiceClient().getLastOperationContext().getServiceContext();
            String sessionCookie = (String) serviceContext.getProperty(HTTPConstants.COOKIE_STRING);

            MessageBoxAdminServiceStub messageBoxAdminServiceStub = new MessageBoxAdminServiceStub(servicesString + "MessageBoxAdminService");
            messageBoxAdminServiceStub._getServiceClient().getOptions().setManageSession(true);
            messageBoxAdminServiceStub._getServiceClient().getOptions().setProperty(HTTPConstants.COOKIE_STRING, sessionCookie);

            SQSKeys sqsKeys = messageBoxAdminServiceStub.getSQSKeys("admin");
            accessKey = sqsKeys.getAccessKeyId();
            secretAccessKey = sqsKeys.getSecretAccessKeyId();
        } catch (Exception e) {
            System.out.println("Failed to get access key ids." + e.getMessage());
            System.exit(0);
        }
    }

    public static void main(String[] args)  {
        try{
        SQSClient sqsClient = new SQSClient();
        System.out.println("sending message...");
        sqsClient.sendMessages();
        System.out.println("receiving message...");
        sqsClient.receiveMessages();
        Thread.sleep(2000);
        System.out.println("deleting message...");
        sqsClient.deleteMessages();
        System.out.println("deleting queue...");
        sqsClient.deleteQueue();
        }catch (Exception e){
            System.out.println("sqs sample failed." + e);
        }
    }

    /**
     * Create a queue, send a message
     *
     * @throws java.rmi.RemoteException
     */
    public void sendMessages() throws RemoteException {
        QueueServiceStub queueServiceStub = new QueueServiceStub("http://localhost:9763/services/QueueService");
        CreateQueue createQueue = new CreateQueue();
        createQueue.setQueueName(QUEUE_NAME);
        createQueue.setDefaultVisibilityTimeout(new BigInteger(DEFAULT_VISIBILITY_TIMEOUT));
        // add security soap header for action CreateQueue
        addSoapHeader(queueServiceStub, "CreateQueue");
        CreateQueueResponse createQueueResponse = queueServiceStub.createQueue(createQueue);
        System.out.println("Queue created with URL ==>" +createQueueResponse.getCreateQueueResult().getQueueUrl());

        MessageQueueStub messageQueueStub = new MessageQueueStub(createQueueResponse.getCreateQueueResult().getQueueUrl().toString());
        SendMessage sendMessage = new SendMessage();
        sendMessage.setMessageBody("Test Send Message");
        addSoapHeader(messageQueueStub, "SendMessage");
        messageQueueStub.sendMessage(sendMessage);
        System.out.println("'Test Send Message' was sent");
    }

    /**
     * Receive messages from queue
     *
     * @throws java.rmi.RemoteException
     */
    public void receiveMessages() throws RemoteException {
        QueueServiceStub queueServiceStub = new QueueServiceStub("http://localhost:9763/services/QueueService");
        CreateQueue createQueue = new CreateQueue();
        createQueue.setQueueName(QUEUE_NAME);
        createQueue.setDefaultVisibilityTimeout(new BigInteger(DEFAULT_VISIBILITY_TIMEOUT));
        addSoapHeader(queueServiceStub, "CreateQueue");
        CreateQueueResponse createQueueResponse = queueServiceStub.createQueue(createQueue);

        MessageQueueStub messageQueueStub = new MessageQueueStub(createQueueResponse.getCreateQueueResult().getQueueUrl().toString());
        ReceiveMessage receiveMessage = new ReceiveMessage();
        receiveMessage.setMaxNumberOfMessages(new BigInteger(MAX_NUMBER_OF_MESSAGES));
        receiveMessage.setVisibilityTimeout(new BigInteger("2000"));
        addSoapHeader(messageQueueStub, "ReceiveMessage");
        ReceiveMessageResponse receiveMessageResponse = messageQueueStub.receiveMessage(receiveMessage);
        Message_type0[] message_type0s = receiveMessageResponse.getReceiveMessageResult().getMessage();
        if (message_type0s != null) {
            for (Message_type0 message_type0 : message_type0s) {
                System.out.println("Received message ==> " + message_type0.getBody());

            }
        }
    }

    /**
     * Delete messages from queue
     *
     * @throws java.rmi.RemoteException
     */
    public void deleteMessages() throws RemoteException {
        QueueServiceStub queueServiceStub = new QueueServiceStub("http://localhost:9763/services/QueueService");
        CreateQueue createQueue = new CreateQueue();
        createQueue.setQueueName(QUEUE_NAME);
        createQueue.setDefaultVisibilityTimeout(new BigInteger(DEFAULT_VISIBILITY_TIMEOUT));
        addSoapHeader(queueServiceStub, "CreateQueue");
        CreateQueueResponse createQueueResponse = queueServiceStub.createQueue(createQueue);

        MessageQueueStub messageQueueStub = new MessageQueueStub(createQueueResponse.getCreateQueueResult().getQueueUrl().toString());
        ReceiveMessage receiveMessage = new ReceiveMessage();
        receiveMessage.setMaxNumberOfMessages(new BigInteger(MAX_NUMBER_OF_MESSAGES));
        receiveMessage.setVisibilityTimeout(new BigInteger("20000"));
        addSoapHeader(messageQueueStub, "ReceiveMessage");
        ReceiveMessageResponse receiveMessageResponse = messageQueueStub.receiveMessage(receiveMessage);
        Message_type0[] message_type0s = receiveMessageResponse.getReceiveMessageResult().getMessage();
        List<String> receiptHandlers = new ArrayList<String>();
        if (message_type0s != null) {
            for (Message_type0 message_type0 : message_type0s) {
                receiptHandlers.add(message_type0.getReceiptHandle());
            }
        }
        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setReceiptHandle(receiptHandlers.toArray(new String[receiptHandlers.size()]));
        addSoapHeader(messageQueueStub, "DeleteMessage");
        messageQueueStub.deleteMessage(deleteMessage);
        System.out.println("Messages are deleted.");
    }

    /**
     * Delete queue
     *
     * @throws java.rmi.RemoteException
     */
    public void deleteQueue() throws RemoteException {
        QueueServiceStub queueServiceStub = new QueueServiceStub("http://localhost:9763/services/QueueService");
        CreateQueue createQueue = new CreateQueue();
        createQueue.setQueueName(QUEUE_NAME);
        createQueue.setDefaultVisibilityTimeout(new BigInteger(DEFAULT_VISIBILITY_TIMEOUT));
        addSoapHeader(queueServiceStub, "CreateQueue");
        CreateQueueResponse createQueueResponse = queueServiceStub.createQueue(createQueue);

        MessageQueueStub messageQueueStub = new MessageQueueStub(createQueueResponse.getCreateQueueResult().getQueueUrl().toString());
        DeleteQueue deleteQueue = new DeleteQueue();
        addSoapHeader(messageQueueStub, "DeleteQueue");
        messageQueueStub.deleteQueue(deleteQueue);
        System.out.println("Queue is deleted.");
    }

    /**
     * Add security headers for queue service stub
     *
     * @param queueServiceStub - queue service stub created with given end point
     * @param action           - the action to be performed as CreateQueue, ListQueue
     */
    private void addSoapHeader(QueueServiceStub queueServiceStub, String action) {

        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMNamespace awsNs = factory.createOMNamespace("http://security.amazonaws.com/doc/2007-01-01/", "aws");
        OMElement accessKeyId = factory.createOMElement("AWSAccessKeyId", awsNs);
        accessKeyId.setText(accessKey);
        OMElement timestamp = factory.createOMElement("Timestamp", awsNs);
        timestamp.setText(new Date().toString());
        OMElement signature = factory.createOMElement("Signature", awsNs);

        try {
            signature.setText(calculateRFC2104HMAC(action + timestamp.getText(), secretAccessKey));
        } catch (SignatureException e) {
        }

        queueServiceStub._getServiceClient().removeHeaders();

        queueServiceStub._getServiceClient().addHeader(accessKeyId);
        queueServiceStub._getServiceClient().addHeader(timestamp);
        queueServiceStub._getServiceClient().addHeader(signature);


    }

    /**
     * Add security headers for message queue service stub
     *
     * @param messageQueueStub - message queue service stub created with queue url
     * @param action           - the action to be performed as SendMessage,DeleteMessage
     */
    private void addSoapHeader(MessageQueueStub messageQueueStub, String action) {
        OMFactory factory = OMAbstractFactory.getSOAP11Factory();
        OMNamespace awsNs = factory.createOMNamespace("http://security.amazonaws.com/doc/2007-01-01/", "aws");
        OMElement accessKeyId = factory.createOMElement("AWSAccessKeyId", awsNs);
        accessKeyId.setText(accessKey);
        OMElement timestamp = factory.createOMElement("Timestamp", awsNs);
        timestamp.setText(new Date().toString());
        OMElement signature = factory.createOMElement("Signature", awsNs);

        try {
            signature.setText(calculateRFC2104HMAC(action + timestamp.getText(), secretAccessKey));
        } catch (SignatureException e) {
        }

        messageQueueStub._getServiceClient().removeHeaders();

        messageQueueStub._getServiceClient().addHeader(accessKeyId);
        messageQueueStub._getServiceClient().addHeader(timestamp);
        messageQueueStub._getServiceClient().addHeader(signature);
    }

    /**
     * Calculate signature for given data using secret access key
     *
     * @param data - data to be signed, action+timestamp
     * @param key- secret access key
     * @return signature
     * @throws java.security.SignatureException
     *
     */
    public static String calculateRFC2104HMAC(String data, String key)
            throws SignatureException {
        final String HMAC_SHA1_ALGORITHM = "HmacSHA1";
        String result;
        try {
            SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), HMAC_SHA1_ALGORITHM);
            Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
            mac.init(signingKey);
            byte[] rawHmac = mac.doFinal(data.getBytes());
            result = Base64.encode(rawHmac);
        } catch (Exception e) {
            throw new SignatureException("Failed to generate HMAC : " + e.getMessage());
        }
        return result;
    }


}
