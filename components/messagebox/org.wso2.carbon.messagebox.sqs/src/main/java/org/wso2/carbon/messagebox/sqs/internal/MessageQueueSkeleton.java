/**
 * MessageQueueSkeleton.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: SNAPSHOT  Built on : Aug 07, 2010 (07:59:55 IST)
 */
package org.wso2.carbon.messagebox.sqs.internal;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.messagebox.SQSMessage;
import org.wso2.carbon.messagebox.MessageBoxConstants;
import org.wso2.carbon.messagebox.MessageBoxException;
import org.wso2.carbon.messagebox.sqs.internal.util.Utils;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MessageQueueSkeleton java skeleton for the SQS MessageQueue service
 */
public class MessageQueueSkeleton {

    private static final Log log = LogFactory.getLog(MessageQueueSkeleton.class);


    /**
     * Auto generated method signature
     * Sets an attribute of a queue. Currently, you can set only the VisibilityTimeout attribute for a queue.
     *
     * @param setQueueAttributes - contains map of attributes with values
     * @throws AxisFault -InvalidAttributeName, AccessDenied, NonExistentQueue, InternalError
     */

    public SetQueueAttributesResponse setQueueAttributes(SetQueueAttributes setQueueAttributes)
            throws AxisFault {
        //ToDo: max message size, message retention period, policy, these attributes are not supported yet
        String messageBoxId = Utils.getQueueNameFromRequestURI();
        Attribute_type0[] attributes = setQueueAttributes.getAttribute();
        Map<String, String> attributeMap = new ConcurrentHashMap<String, String>(attributes.length);
        for (Attribute_type0 attribute : attributes) {
            String attributeName = attribute.getName();
            if (attributeName.equals(MessageBoxConstants.SQS_QUEUE_ATTRIBUTE_VISIBILITY_TIMEOUT) ||
                attributeName.equals(MessageBoxConstants.SQS_QUEUE_ATTRIBUTE_MESSAGE_RETENTION_PERIOD) ||
                attributeName.equals(MessageBoxConstants.SQS_QUEUE_ATTRIBUTE_MAX_MESSAGE_SIZE)) {
                String attributeValue = attribute.getValue();
                attributeMap.put(attributeName, attributeValue);
            } else {
                throw new FaultResponse(Utils.getMessageRequestId(), "InvalidAttributeName",
                                        "Unknown attribute").
                        createAxisFault();
            }
        }

        try {
            Utils.getMessageBoxService().setMessageBoxAttributes(messageBoxId, attributeMap);
            SetQueueAttributesResponse setQueueAttributesResponse = new SetQueueAttributesResponse();
            setQueueAttributesResponse.setResponseMetadata(getResponseMetadata_type0());
            if (log.isInfoEnabled()) {
                log.info("Queue attributes successfully set in queue, " + messageBoxId);
            }
            return setQueueAttributesResponse;
        } catch (MessageBoxException e) {
            throw new FaultResponse(e, Utils.getMessageRequestId()).createAxisFault();
        }
    }


    /**
     * Auto generated method signature
     * This action unconditionally deletes the queue specified by the queue URL. Use this operation
     * WITH CARE!  The queue is deleted even if it is NOT empty.
     *
     * @param deleteQueue - this is not needed as queue name is taken from requested uri
     */

    public DeleteQueueResponse deleteQueue(DeleteQueue deleteQueue) {

        DeleteQueueResponse deleteQueueResponse = new DeleteQueueResponse();
        deleteQueueResponse.setResponseMetadata(getResponseMetadata_type0());

        String messageboxId = Utils.getQueueNameFromRequestURI();

        try {
            Utils.getMessageBoxService().deleteMessageBox(messageboxId);
            if (log.isInfoEnabled()) {
                log.info(messageboxId + " ,queue is successfully deleted.");
            }
        } catch (MessageBoxException e) {
            log.debug("Failed to delete messagebox with id " + messageboxId);
            // here we do not throw axis fault since sqs api provide no errors even if
            // queue is not deleted
        }
        return deleteQueueResponse;
    }


    /**
     * Auto generated method signature
     * The ChangeMessageVisibility action extends the read lock timeout of the specified message
     * from the specified queue to the specified value.
     *
     * @param changeMessageVisibility - contains receipt handler and new visibility timeout value
     */

    public ChangeMessageVisibilityResponse changeMessageVisibility(
            ChangeMessageVisibility changeMessageVisibility) throws AxisFault {

        String messageBoxId = Utils.getQueueNameFromRequestURI();
        String receiptHandler = changeMessageVisibility.getReceiptHandle();

        // get visibility time out value
        long visibilityTimeout = 0;
        BigInteger timeoutValue = changeMessageVisibility.getVisibilityTimeout();
        if (timeoutValue != null) {
            visibilityTimeout = timeoutValue.longValue();
            if (visibilityTimeout > MessageBoxConstants.TWELVE_HOURS_IN_SECONDS && visibilityTimeout < 0) {
                throw new FaultResponse(Utils.getMessageRequestId(), "InvalidParameterValue",
                                        "One or more parameters cannot be validated.").
                        createAxisFault();
            }
        }

        try {
            Utils.getMessageBoxService().changeVisibility(messageBoxId, receiptHandler,
                                                          visibilityTimeout);
            ChangeMessageVisibilityResponse changeMessageVisibilityResponse =
                    new ChangeMessageVisibilityResponse();
            changeMessageVisibilityResponse.setResponseMetadata(getResponseMetadata_type0());
            if (log.isInfoEnabled()) {
                log.info("Message visibility time out was changed in messages in queue, " +
                         messageBoxId);
            }
            return changeMessageVisibilityResponse;
        } catch (MessageBoxException e) {
            log.debug("unable to change visibilityTimeout with receiptHandler: " + receiptHandler +
                      " in message box: " + messageBoxId);
            throw new FaultResponse(e, Utils.getMessageRequestId()).createAxisFault();
        }
    }

    private ResponseMetadata_type0 getResponseMetadata_type0() {
        ResponseMetadata_type0 responseMetadata_type0 = new ResponseMetadata_type0();
        responseMetadata_type0.setRequestId(Utils.getMessageRequestId());
        return responseMetadata_type0;
    }


    /**
     * Auto generated method signature
     * Adds the specified permission(s) to a queue for the specified principal(s).
     * This allows for sharing access to the queue.
     *
     * @param addPermission - contains permission label name, actions array and shared users array
     */

    public AddPermissionResponse addPermission(AddPermission addPermission) throws AxisFault {

        String permissionLabel = addPermission.getLabel();
        if (!Utils.validQueueName(permissionLabel)) {
            throw new FaultResponse(Utils.getMessageRequestId(), "InvalidParameterValue",
                                    "One or more parameters cannot be validated.").
                    createAxisFault();
        }

        List<String> operationsList = Arrays.asList(addPermission.getActionName());
        checkValidOperationNames(operationsList);

        // if all operations are allowed, allow them separately
        if (operationsList.contains(MessageBoxConstants.SQS_OPERATION_ALL)) {
            operationsList.clear();
            getAllOperations(operationsList);
        }


        List<String> sharedUsers = Arrays.asList(addPermission.getAWSAccountId());

        String messageBoxId = Utils.getQueueNameFromRequestURI();

        try {
            Utils.getMessageBoxService().addPermission(messageBoxId, operationsList,
                                                       permissionLabel, sharedUsers);
            AddPermissionResponse addPermissionResponse = new AddPermissionResponse();
            addPermissionResponse.setResponseMetadata(getResponseMetadata_type0());
            if (log.isInfoEnabled()) {
                log.info("New permissions is added to queue, " + messageBoxId +
                         " with permission label, " + permissionLabel);
            }
            return addPermissionResponse;
        } catch (MessageBoxException e) {
            throw new FaultResponse(e, Utils.getMessageRequestId()).createAxisFault();
        }

    }

    private void getAllOperations(List<String> operationsList) {
        operationsList.add(MessageBoxConstants.SQS_OPERATION_CHANGE_MESSAGE_VISIBILITY);
        operationsList.add(MessageBoxConstants.SQS_OPERATION_DELETE_MESSAGE);
        operationsList.add(MessageBoxConstants.SQS_OPERATION_GET_QUEUE_ATTRIBUTES);
        operationsList.add(MessageBoxConstants.SQS_OPERATION_RECEIVE_MESSAGE);
        operationsList.add(MessageBoxConstants.SQS_OPERATION_SEND_MESSAGE);
    }

    private void checkValidOperationNames(List<String> operationsList) throws AxisFault {
        for (String operation : operationsList) {
            if (!(MessageBoxConstants.SQS_OPERATION_ALL.equals(operation) ||
                  MessageBoxConstants.SQS_OPERATION_CHANGE_MESSAGE_VISIBILITY.equals(operation) ||
                  MessageBoxConstants.SQS_OPERATION_DELETE_MESSAGE.equals(operation) ||
                  MessageBoxConstants.SQS_OPERATION_GET_QUEUE_ATTRIBUTES.equals(operation) ||
                  MessageBoxConstants.SQS_OPERATION_RECEIVE_MESSAGE.equals(operation) ||
                  MessageBoxConstants.SQS_OPERATION_SEND_MESSAGE.equals(operation))) {
                throw new FaultResponse(Utils.getMessageRequestId(), "InvalidActionName",
                                        "The action specified was invalid. ").
                        createAxisFault();
            }
        }
    }


    /**
     * Auto generated method signature
     * Gets one or all attributes of a queue. Queues currently have two attributes you can
     * get: ApproximateNumberOfMessages and VisibilityTimeout.
     *
     * @param getQueueAttributes - contains queue attribute names
     */

    public GetQueueAttributesResponse getQueueAttributes(GetQueueAttributes getQueueAttributes)
            throws AxisFault {
        String[] attributeNames = getQueueAttributes.getAttributeName();
        List<String> attributeNameList = Arrays.asList(attributeNames);

        checkValidAttributeNames(attributeNames);

        int attributeNamesLength = attributeNames.length;
        GetQueueAttributesResponse getQueueAttributesResponse = new GetQueueAttributesResponse();
        GetQueueAttributesResult_type0 getQueueAttributesResult_type0 =
                new GetQueueAttributesResult_type0();
        Attribute_type0[] attribute_type0s = new Attribute_type0[attributeNamesLength];
        String messageBoxId = Utils.getQueueNameFromRequestURI();
        Map<String, String> attributeMap;
        try {
            attributeMap = Utils.getMessageBoxService().getMessageBoxAttributes(messageBoxId);
            // if all attributes are requested
            if (attributeNameList.contains(MessageBoxConstants.SQS_QUEUE_ATTRIBUTE_ALL)) {
                for (int index = 0; index < attributeNamesLength; index++) {
                    String attributeName = attributeNames[index];
                    String attributeValue = "";
                    if (attributeMap.get(attributeName) != null) {
                        attributeValue = attributeMap.get(attributeName);
                    }
                    attribute_type0s[index]= new Attribute_type0();
                    attribute_type0s[index].setName(attributeName);
                    attribute_type0s[index].setValue(attributeValue);
                }
            } else {
                // only few attributes are requested
                for (int index = 0; index < attributeNamesLength; index++) {
                    String attributeName = attributeNames[index];
                    if (attributeNameList.contains(attributeName)) {
                        String attributeValue = "";
                        if (attributeMap.get(attributeName) != null) {
                            attributeValue = attributeMap.get(attributeName);
                        }
                        attribute_type0s[index]= new Attribute_type0();
                        attribute_type0s[index].setName(attributeName);
                        attribute_type0s[index].setValue(attributeValue);
                    }
                }
            }

            getQueueAttributesResult_type0.setAttribute(attribute_type0s);
            getQueueAttributesResponse.setGetQueueAttributesResult(getQueueAttributesResult_type0);
            getQueueAttributesResponse.setResponseMetadata(getResponseMetadata_type0());
            if (log.isInfoEnabled()) {
                log.info("Queue attributes successfully received in queue, " + messageBoxId);
            }
            return getQueueAttributesResponse;
        } catch (MessageBoxException e) {
            log.debug("Failed to retrieve number of messages in " + messageBoxId);
            throw new FaultResponse(e, Utils.getMessageRequestId()).createAxisFault();

        }
    }

    private void checkValidAttributeNames(String[] attributeNames) throws AxisFault {
        for (String attributeName : attributeNames) {
            if (!(attributeName.equals(MessageBoxConstants.SQS_QUEUE_ATTRIBUTE_ALL) ||
                  attributeName.equals(MessageBoxConstants.SQS_QUEUE_ATTRIBUTE_NUMBER_OF_MESSAGES) ||
                  attributeName.equals(MessageBoxConstants.SQS_QUEUE_ATTRIBUTE_NUMBER_OF_MESSAGES_NOT_VISIBLE) ||
                  attributeName.equals(MessageBoxConstants.SQS_QUEUE_ATTRIBUTE_VISIBILITY_TIMEOUT) ||
                  attributeName.equals(MessageBoxConstants.SQS_QUEUE_ATTRIBUTE_CREATED_TIMESTAMP) ||
                  attributeName.equals(MessageBoxConstants.SQS_QUEUE_ATTRIBUTE_LAST_MODIFIED_TIMESTAMP) ||
                  attributeName.equals(MessageBoxConstants.SQS_QUEUE_ATTRIBUTE_MAX_MESSAGE_SIZE) ||
                  attributeName.equals(MessageBoxConstants.SQS_QUEUE_ATTRIBUTE_MESSAGE_RETENTION_PERIOD) ||
                  attributeName.equals(MessageBoxConstants.SQS_QUEUE_ATTRIBUTE_POLICY))) {
                throw new FaultResponse(Utils.getMessageRequestId(), "InvalidAttributeName",
                                        "Unknown attribute").
                        createAxisFault();
            }
        }
    }


    /**
     * Auto generated method signature
     * The DeleteMessage action unconditionally removes the specified message from the specified
     * queue. Even if the message is locked by another reader due to the visibility timeout
     * setting, it is still deleted from the queue.
     *
     * @param deleteMessage - contains array of receipt handlers
     */

    public DeleteMessageResponse deleteMessage(DeleteMessage deleteMessage) throws AxisFault {
        //response
        DeleteMessageResponse deleteMessageResponse = new DeleteMessageResponse();
        String[] receiptHandlers = deleteMessage.getReceiptHandle();
        String messageBoxId = Utils.getQueueNameFromRequestURI();
        try {
            for (String receiptHandler : receiptHandlers) {
                Utils.getMessageBoxService().deleteMessage(messageBoxId, receiptHandler);
                deleteMessageResponse.setResponseMetadata(getResponseMetadata_type0());
            }
            if (log.isInfoEnabled()) {
                log.info("Messages are deleted in queue, " + messageBoxId);
            }
            return deleteMessageResponse;
        }
        catch (MessageBoxException e) {
            log.debug("Unable to delete messages in messagebox: " + messageBoxId);
            throw new FaultResponse(e, Utils.getMessageRequestId()).createAxisFault();
        }
    }


    /**
     * Auto generated method signature
     * The SendMessage action delivers a message to the specified queue.
     *
     * @param sendMessage - contains message body
     */

    public SendMessageResponse sendMessage(SendMessage sendMessage) throws AxisFault {

        String messageBody = sendMessage.getMessageBody();
        if (!Utils.validMessageBody(messageBody)) {
            throw new FaultResponse(Utils.getMessageRequestId(), "InvalidMessageContents",
                                    "The message contains characters outside the allowed set.").
                    createAxisFault();
        }
        // we are not throwing fault if message size is more than 64KB

        String messageboxId = Utils.getQueueNameFromRequestURI();
        String messageId = UUID.randomUUID().toString();
        String receiptHandler = UUID.randomUUID().toString();
        String MD5OfMessage = Utils.getMD5OfMessage(messageBody);


        SQSMessage message = new SQSMessage();
        message.setBody(messageBody);
        message.setMd5ofMessageBody(MD5OfMessage);
        message.setMessageId(messageId);
        message.setReceiptHandle(receiptHandler);
        message.setSenderId(MultitenantUtils.getTenantAwareUsername(CarbonContext.getCurrentContext().getUsername()));


        try {
            Utils.getMessageBoxService().putMessage(messageboxId, message);
            SendMessageResponse sendMessageResponse = new SendMessageResponse();
            SendMessageResult_type0 sendMessageResult_type0 = new SendMessageResult_type0();
            sendMessageResult_type0.setMessageId(messageId);
            sendMessageResult_type0.setMD5OfMessageBody(MD5OfMessage);
            sendMessageResponse.setResponseMetadata(getResponseMetadata_type0());
            sendMessageResponse.setSendMessageResult(sendMessageResult_type0);
            if (log.isInfoEnabled()) {
                log.info("Message,  " + message.getBody() + " is sent to queue, " + messageboxId);
            }
            return sendMessageResponse;
        } catch (MessageBoxException e) {
            if (log.isDebugEnabled()) {
                log.debug("Unable to send message:" + messageBody + " to messagebox: " + messageboxId);
            }
            throw new FaultResponse(e, Utils.getMessageRequestId()).createAxisFault();
        }
    }


    /**
     * Auto generated method signature
     * Retrieves one or more messages from the specified queue.  For each message returned,
     * the response includes the message body; MD5 digest of the message body; receipt handle,
     * which is the identifier you must provide when deleting the message; and message ID of
     * each message. Messages returned by this action stay in the queue until you delete them.
     * However, once a message is returned to a ReceiveMessage request, it is not returned
     * on subsequent ReceiveMessage requests for the duration of the VisibilityTimeout. If you
     * do not specify a VisibilityTimeout in the request, the overall visibility timeout for the
     * queue is used for the returned messages.
     *
     * @param receiveMessage - contains visibility timeout and number of messages
     */

    public ReceiveMessageResponse receiveMessage(ReceiveMessage receiveMessage) throws AxisFault {
        // get number of maximum messages to receive
        int maxNumberOfMessages = 1;
        BigInteger messageCount = receiveMessage.getMaxNumberOfMessages();
        if (messageCount != null) {
            maxNumberOfMessages = messageCount.intValue();
            if (maxNumberOfMessages > 10 || maxNumberOfMessages < 1) {
                throw new FaultResponse(Utils.getMessageRequestId(), "ReadCountOutOfRange",
                                        "The value for MaxNumberOfMessages is not valid " +
                                        "(must be from 1 to 10).").
                        createAxisFault();
            }
        }
        // get message box id
        String messageBoxId = Utils.getQueueNameFromRequestURI();

        // get visibility time out value
        long visibilityTimeout = 0;
        BigInteger timeoutValue = receiveMessage.getVisibilityTimeout();
        if (timeoutValue != null) {
            visibilityTimeout = timeoutValue.longValue();
            if (visibilityTimeout > MessageBoxConstants.TWELVE_HOURS_IN_SECONDS && visibilityTimeout < 0) {
                throw new FaultResponse(Utils.getMessageRequestId(), "InvalidParameterValue",
                                        "One or more parameters cannot be validated.").
                        createAxisFault();
            }
        }

        // get set of attribute names and set to attribute map to receive their values
        Map<String, String> attributeMap = new ConcurrentHashMap<String, String>();
        boolean attributesRequired = true;
        if (receiveMessage.getAttributeName() == null) {
            attributesRequired = false;
        }

        try {
            List<SQSMessage> messageList = Utils.getMessageBoxService().
                    receiveMessage(messageBoxId, maxNumberOfMessages, visibilityTimeout, attributeMap);

            int numberOfMessagesRetreived = messageList.size();
            Message_type0[] message_type0 = new Message_type0[numberOfMessagesRetreived];

            int index = 0;
            for (SQSMessage receivedMessage : messageList) {
                message_type0[index] = new Message_type0();
                message_type0[index].setBody(receivedMessage.getBody());
                message_type0[index].setMessageId(receivedMessage.getMessageId());
                message_type0[index].setMD5OfBody(receivedMessage.getMd5ofMessageBody());
                message_type0[index].setReceiptHandle(receivedMessage.getReceiptHandle());
                if (attributesRequired) {
                    message_type0[index].setAttribute(getAttributeValues(
                            attributeMap, receiveMessage.getAttributeName()));
                }
                index++;
            }

            ReceiveMessageResponse receiveMessageResponse = new ReceiveMessageResponse();
            ReceiveMessageResult_type0 receiveMessageResult_type0 = new ReceiveMessageResult_type0();
            receiveMessageResult_type0.setMessage(message_type0);
            receiveMessageResponse.setReceiveMessageResult(receiveMessageResult_type0);
            receiveMessageResponse.setResponseMetadata(getResponseMetadata_type0());

            if (log.isInfoEnabled()) {
                log.info(numberOfMessagesRetreived + " messages are received from queue,  " + messageBoxId);
            }
            return receiveMessageResponse;
        } catch (MessageBoxException e) {
            throw new FaultResponse(e, Utils.getMessageRequestId()).createAxisFault();
        }
    }

    private Attribute_type0[] getAttributeValues(Map<String, String> attributeMap,
                                                 String[] attributeNames) {
        Attribute_type0[] attribute_type0s;
        // convert array to list for searching the SQS_MESSAGE_ATTRIBUTE_ALL
        List<String> attributeList = Arrays.asList(attributeNames);

        // if all attributes are required get all from map and set to attribute_type0
        if (attributeList.contains(MessageBoxConstants.SQS_MESSAGE_ATTRIBUTE_ALL)) {
            int index = 0;
            attribute_type0s = new Attribute_type0[attributeMap.size()];
            for (Map.Entry entry : attributeMap.entrySet()) {
                attribute_type0s[index] = new Attribute_type0();
                attribute_type0s[index].setName(entry.getKey().toString());
                attribute_type0s[index].setValue(entry.getValue().toString());
                index++;
            }
        } else {
            // if all attributes are not required, set only the required attribute values
            int index = 0;
            attribute_type0s = new Attribute_type0[attributeList.size()];
            for (String attributeName : attributeNames) {
                attribute_type0s[index] = new Attribute_type0();
                attribute_type0s[index].setName(attributeName);
                attribute_type0s[index].setValue(attributeMap.get(attributeName));
                index++;
            }
        }
        return attribute_type0s;
    }


    /**
     * Auto generated method signature
     * Removes the permission with the specified statement id from the queue.
     *
     * @param removePermission - contains permission label name
     */

    public RemovePermissionResponse removePermission(RemovePermission removePermission)
            throws AxisFault {

        String permissionLabel = removePermission.getLabel();
        String messageBoxId = Utils.getQueueNameFromRequestURI();

        try {
            Utils.getMessageBoxService().removePermission(messageBoxId, permissionLabel);
            RemovePermissionResponse removePermissionResponse = new RemovePermissionResponse();
            removePermissionResponse.setResponseMetadata(getResponseMetadata_type0());
            if (log.isInfoEnabled()) {
                log.info("Permission is removed from queue, " + messageBoxId +
                         " with permission label, " + permissionLabel);
            }
            return removePermissionResponse;
        } catch (MessageBoxException e) {
            throw new FaultResponse(e, Utils.getMessageRequestId()).createAxisFault();
        }

    }

}
    