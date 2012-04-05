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
package org.wso2.carbon.messagebox.sqs.internal.dispatcher;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.builder.BuilderUtil;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.axis2.util.MultipleEntryHashMap;
import org.wso2.carbon.messagebox.MessageBoxConstants;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

public class SQSRestBasedHandler extends AbstractHandler {
    public InvocationResponse invoke(MessageContext messageContext) throws AxisFault {
        MultipleEntryHashMap multipleEntryHashMap =
                (MultipleEntryHashMap) messageContext.getProperty(Constants.REQUEST_PARAMETER_MAP);
        if (multipleEntryHashMap != null) {
            Object accessKeyIdProperty = multipleEntryHashMap.get(MessageBoxConstants.AWSACCESS_KEY_ID);
            Object actionNameProperty = multipleEntryHashMap.get(MessageBoxConstants.ACTION);
            Object messageBodyProperty = multipleEntryHashMap.get("MessageBody");
            Object maxNumberOfMessagesProperty = multipleEntryHashMap.get("MaxNumberOfMessages");
            Object visibilityTimeoutProperty = multipleEntryHashMap.get("VisibilityTimeout");
            Object receiptHandleProperty = multipleEntryHashMap.get("ReceiptHandle");
            Object queueNamePrefixProperty = multipleEntryHashMap.get("QueueNamePrefix");
            Object permissionLabelProperty = multipleEntryHashMap.get("Label");
            Object queueNameProperty = multipleEntryHashMap.get("QueueName");

            // AWSAccountId.n  ActionName.n AttributeName.n  Attribute.Name  Attribute.Value
            List<Object> aWSAccountIdList = new ArrayList<Object>();
            List<Object> actionNameList = new ArrayList<Object>();
            List<Object> attributeNameList = new ArrayList<Object>();
            List<Object> setQueueAttributeNameList = new ArrayList<Object>();
            List<Object> setQueueAttributeValueList = new ArrayList<Object>();

            getParametersFromHashMap(multipleEntryHashMap, aWSAccountIdList, actionNameList,
                                     attributeNameList, setQueueAttributeNameList,
                                     setQueueAttributeValueList);


            if (actionNameProperty != null) {
                String actionName = actionNameProperty.toString();
                emptyMultipleHashMap(multipleEntryHashMap);

                if (actionName.equalsIgnoreCase("CreateQueue") ||
                    actionName.equalsIgnoreCase("DeleteQueue")) {
                    multipleEntryHashMap.put("QueueName", queueNameProperty);
                } else if (actionName.equalsIgnoreCase("SendMessage")) {
                    try {
                        String messageBody = URLDecoder.decode(messageBodyProperty.toString(),
                                                               MessageBoxConstants.URL_ENCODING);
                        multipleEntryHashMap.put("MessageBody", messageBody);
                    } catch (UnsupportedEncodingException e) {
                        throw new AxisFault("Failed to decode message body " +
                                            messageBodyProperty.toString(), e);
                    }
                } else if (actionName.equalsIgnoreCase("ReceiveMessage")) {
                    setReceiveMessageParameters(multipleEntryHashMap, maxNumberOfMessagesProperty,
                                                visibilityTimeoutProperty, attributeNameList);
                } else if (actionName.equalsIgnoreCase("DeleteMessage")) {
                    multipleEntryHashMap.put("ReceiptHandle", receiptHandleProperty);
                } else if (actionName.equalsIgnoreCase("ListQueues")) {
                    multipleEntryHashMap.put("QueueNamePrefix", queueNamePrefixProperty);
                } else if (actionName.equalsIgnoreCase("ChangeMessageVisibility")) {
                    multipleEntryHashMap.put("ReceiptHandle", receiptHandleProperty);
                    multipleEntryHashMap.put("VisibilityTimeout", visibilityTimeoutProperty);
                } else if (actionName.equalsIgnoreCase("GetQueueAttributes")) {
                    for (Object attributeNameProperty : attributeNameList) {
                        multipleEntryHashMap.put("AttributeName", attributeNameProperty);
                    }
                } else if (actionName.equalsIgnoreCase("SetQueueAttributes")) {
                    setSetQueueAttributeParameters(multipleEntryHashMap, setQueueAttributeNameList,
                                                   setQueueAttributeValueList);

                } else if (actionName.equalsIgnoreCase("AddPermission")) {
                    setAddPermissionParameters(multipleEntryHashMap, accessKeyIdProperty,
                                               permissionLabelProperty, aWSAccountIdList,
                                               actionNameList);
                } else if (actionName.equalsIgnoreCase("RemovePermission")) {
                    multipleEntryHashMap.put("AWSAccountId", accessKeyIdProperty);
                    multipleEntryHashMap.put("Label", permissionLabelProperty);
                }


                org.apache.axiom.soap.SOAPFactory soapFactory = OMAbstractFactory.getSOAP11Factory();
                messageContext.setEnvelope(BuilderUtil.buildsoapMessage(messageContext,
                                                                        multipleEntryHashMap,
                                                                        soapFactory));
            }

        }

        return InvocationResponse.CONTINUE;
    }

    private void setReceiveMessageParameters(MultipleEntryHashMap multipleEntryHashMap,
                                             Object maxNumberOfMessagesProperty,
                                             Object visibilityTimeoutProperty,
                                             List<Object> attributeNameList) {
        multipleEntryHashMap.put("MaxNumberOfMessages", maxNumberOfMessagesProperty);
        multipleEntryHashMap.put("VisibilityTimeout", visibilityTimeoutProperty);
        int attributeIndex = 0;
        for (Object attributeNameProperty : attributeNameList) {
            attributeIndex++;
            multipleEntryHashMap.put("AttributeName." + attributeIndex, attributeNameProperty);
        }
    }

    private void setSetQueueAttributeParameters(MultipleEntryHashMap multipleEntryHashMap,
                                                List<Object> setQueueAttributeNameList,
                                                List<Object> setQueueAttributeValueList) {
        for (Object attributeNameProperty : setQueueAttributeNameList) {
            multipleEntryHashMap.put("AttributeName.Name", attributeNameProperty);
        }
        for (Object attributeValueProperty : setQueueAttributeValueList) {
            multipleEntryHashMap.put("AttributeName.Value", attributeValueProperty);
        }
    }

    private void setAddPermissionParameters(MultipleEntryHashMap multipleEntryHashMap,
                                            Object accessKeyIdProperty,
                                            Object permissionLabelProperty,
                                            List<Object> aWSAccountIdList,
                                            List<Object> actionNameList) {
        for (Object awsAccountIdProperty : aWSAccountIdList) {
            multipleEntryHashMap.put("AWSAccountId", awsAccountIdProperty);
        }
        for (Object awsActionNameProperty : actionNameList) {
            multipleEntryHashMap.put("ActionName", awsActionNameProperty);
        }
        multipleEntryHashMap.put("Label", permissionLabelProperty);
        multipleEntryHashMap.put("AWSAccountId", accessKeyIdProperty);
    }

    private void getParametersFromHashMap(MultipleEntryHashMap multipleEntryHashMap,
                                          List<Object> aWSAccountIdList,
                                          List<Object> actionNameList,
                                          List<Object> attributeNameList,
                                          List<Object> setQueueAttributeNameList,
                                          List<Object> setQueueAttributeValueList) {
        int attributeNameIndex = 1;
        int attributeValueIndex = 1;
        for (Object propertyName : multipleEntryHashMap.keySet()) {
            Object propertyValue = multipleEntryHashMap.get(propertyName);
            if (propertyName.toString().startsWith("AWSAccountId") && propertyValue != null) {
                aWSAccountIdList.add(propertyValue);
            } else if (propertyName.toString().startsWith("ActionName") && propertyValue != null) {
                actionNameList.add(propertyValue);
            } else if (propertyName.toString().startsWith("AttributeName") && propertyValue != null) {
                attributeNameList.add(propertyValue);
            } else if (propertyName.toString().startsWith("Attribute." + attributeNameIndex + ".Name") &&
                       propertyValue != null) {
                setQueueAttributeNameList.add(propertyValue);
                attributeNameIndex++;
            } else if (propertyName.toString().startsWith("Attribute." + attributeValueIndex + ".Value") &&
                       propertyValue != null) {
                setQueueAttributeValueList.add(propertyValue);
                attributeValueIndex++;
            }
        }
    }

    private void emptyMultipleHashMap(MultipleEntryHashMap multipleEntryHashMap) {
        for (Object aKeySet : multipleEntryHashMap.keySet()) {
            multipleEntryHashMap.get(aKeySet);
        }
    }
}
