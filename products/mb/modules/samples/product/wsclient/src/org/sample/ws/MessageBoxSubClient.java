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

package org.sample.ws;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.util.Base64;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.databinding.utils.ConverterUtil;
import org.apache.axis2.transport.http.HTTPConstants;
import org.wso2.carbon.event.client.broker.BrokerClient;
import org.wso2.carbon.event.client.broker.BrokerClientException;
import org.wso2.carbon.event.client.stub.generated.authentication.AuthenticationAdminServiceStub;
import org.wso2.carbon.event.client.stub.generated.authentication.AuthenticationExceptionException;
import org.wso2.carbon.messagebox.stub.*;
import org.wso2.carbon.messagebox.stub.admin.internal.MessageBoxAdminServiceMessageBoxAdminExceptionException;
import org.wso2.carbon.messagebox.stub.admin.internal.MessageBoxAdminServiceStub;
import org.wso2.carbon.messagebox.stub.admin.internal.xsd.SQSKeys;
import org.wso2.carbon.utils.NetworkUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.net.SocketException;
import java.rmi.RemoteException;
import java.security.SignatureException;
import java.util.Calendar;

public class MessageBoxSubClient {

    private SQSKeys sqsKeys;
    private BrokerClient brokerClient;
    private String messageBoxURI;
    private String subscriptionID;


    public void getAccessKeys() {

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
            this.sqsKeys = sqsKeys;

            //create the broker client
            this.brokerClient = new BrokerClient("https://localhost:9443/services/EventBrokerService", "admin", "admin");

        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (AuthenticationExceptionException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (MessageBoxAdminServiceMessageBoxAdminExceptionException e) {
            e.printStackTrace();
        }

    }

    private void createMessageBox() {

        try {
            QueueServiceStub stub = new QueueServiceStub("https://localhost:9443/services/QueueService");
            addSoapHeader("CreateQueue", stub._getServiceClient());
            CreateQueue createQueue = new CreateQueue();
            createQueue.setQueueName("testMessageBox");
            createQueue.setDefaultVisibilityTimeout(new BigInteger("30"));
            CreateQueueResponse response = stub.createQueue(createQueue);
            this.messageBoxURI = response.getCreateQueueResult().getQueueUrl().toString();

            System.out.println("Queue Created with URI ==> " + this.messageBoxURI);

        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void subscribe() {
        // subscribe with message box.
        try {
            this.subscriptionID = this.brokerClient.subscribe("foo/messagebox", "sqs://admin/testMessageBox");
        } catch (BrokerClientException e) {
            e.printStackTrace();
        }
    }

    private void publish() {
        try {
            this.brokerClient.publish("foo/messagebox", getOMElementToSend());
        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();
        }
    }

    private void retriveAndDeleteMessage() {

        try {
            MessageQueueStub stub = null;

            stub = new MessageQueueStub(this.messageBoxURI);
            addSoapHeader("ReceiveMessage", stub._getServiceClient());

            ReceiveMessage receiveMessage = new ReceiveMessage();
            receiveMessage.setMaxNumberOfMessages(new BigInteger("1"));
            receiveMessage.setVisibilityTimeout(new BigInteger("30000"));
            ReceiveMessageResponse response = stub.receiveMessage(receiveMessage);

            Message_type0[] messages = response.getReceiveMessageResult().getMessage();
            for (Message_type0 message_type0 : messages) {
                System.out.println("Got the message ==> " + message_type0.getBody());

                stub = new MessageQueueStub(this.messageBoxURI);
                addSoapHeader("DeleteMessage", stub._getServiceClient());
                DeleteMessage deleteMessage = new DeleteMessage();
                deleteMessage.setReceiptHandle(new String[]{message_type0.getReceiptHandle()});
                stub.deleteMessage(deleteMessage);
            }

        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    private void deleteMessageBox() {

        MessageQueueStub stub = null;
        try {
            stub = new MessageQueueStub(this.messageBoxURI);
            addSoapHeader("DeleteQueue", stub._getServiceClient());

            DeleteQueue deleteQueue = new DeleteQueue();
            stub.deleteQueue(deleteQueue);

        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    private void unsubscribe(){

        try {
            this.brokerClient.unsubscribe(this.subscriptionID);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    private OMElement getOMElementToSend() {
        OMFactory omFactory = OMAbstractFactory.getOMFactory();
        OMNamespace omNamespace = omFactory.createOMNamespace("http://ws.sample.org", "ns1");
        OMElement receiveElement = omFactory.createOMElement("receive", omNamespace);
        OMElement messageElement = omFactory.createOMElement("message", omNamespace);
        messageElement.setText("Test publish message");
        receiveElement.addChild(messageElement);
        return receiveElement;

    }

    private void addSoapHeader(String action, ServiceClient serviceClient) {

        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMNamespace awsNs = factory.createOMNamespace("http://security.amazonaws.com/doc/2007-01-01/", "aws");

        OMElement accessKeyId = factory.createOMElement("AWSAccessKeyId", awsNs);
        accessKeyId.setText(this.sqsKeys.getAccessKeyId());

        OMElement timestamp = factory.createOMElement("Timestamp", awsNs);
        timestamp.setText(ConverterUtil.convertToString(Calendar.getInstance()));

        OMElement signature = factory.createOMElement("Signature", awsNs);

        try {
            signature.setText(calculateRFC2104HMAC(action + timestamp.getText(), this.sqsKeys.getSecretAccessKeyId()));
        } catch (SignatureException e) {
            e.printStackTrace();
        }

        serviceClient.removeHeaders();

        serviceClient.addHeader(accessKeyId);
        serviceClient.addHeader(timestamp);
        serviceClient.addHeader(signature);
    }

    public static String calculateRFC2104HMAC(String data, String key)
            throws java.security.SignatureException {

        final String HMAC_SHA1_ALGORITHM = "HmacSHA1";
        String result;
        try {
            // get an hmac_sha1 key from the raw key bytes
            SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), HMAC_SHA1_ALGORITHM);
            // get an hmac_sha1 Mac instance and initialize with the signing key
            Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
            mac.init(signingKey);
            // compute the hmac on input data bytes
            byte[] rawHmac = mac.doFinal(data.getBytes());
            // base64-encode the hmac
            result = Base64.encode(rawHmac);
        } catch (Exception e) {
            throw new SignatureException("Failed to generate HMAC : " + e.getMessage());
        }

        return result;
    }


    public static void main(String[] args) {
        
        MessageBoxSubClient messageBoxSubClient = new MessageBoxSubClient();
        messageBoxSubClient.getAccessKeys();
        messageBoxSubClient.createMessageBox();
        messageBoxSubClient.subscribe();
        messageBoxSubClient.publish();
        messageBoxSubClient.retriveAndDeleteMessage();
        messageBoxSubClient.deleteMessageBox();
        messageBoxSubClient.unsubscribe();
    }

}
