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

package org.wso2.carbon.event.sink.internal;

import org.apache.axiom.util.base64.Base64Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.core.Message;
import org.wso2.carbon.event.core.subscription.EventDispatcher;
import org.wso2.carbon.event.core.subscription.Subscription;
import org.wso2.carbon.event.sink.internal.ds.EventSinkValueHolder;
import org.wso2.carbon.messagebox.MessageBoxException;
import org.wso2.carbon.messagebox.MessageBoxService;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.UUID;

public class EventSinkDispatcher implements EventDispatcher {

    private static final Log log = LogFactory.getLog(EventSinkDispatcher.class);

    public void notify(Message message, Subscription subscription) {

        String eventSinkURL = subscription.getEventSinkURL();
        String messageBoxID = eventSinkURL.substring("sqs://".length());

        org.wso2.carbon.messagebox.SQSMessage sqsMessage = new org.wso2.carbon.messagebox.SQSMessage();
        sqsMessage.setBody(message.getMessage().toString());
        sqsMessage.setMessageId(UUID.randomUUID().toString());
        sqsMessage.setReceiptHandle(UUID.randomUUID().toString());
        sqsMessage.setMd5ofMessageBody(getMD5OfMessage(sqsMessage.getBody()));
        sqsMessage.setReceivedTimeStamp(Calendar.getInstance().getTimeInMillis());
        sqsMessage.setSenderId(subscription.getOwner());

        try {
            EventSinkValueHolder.getInstance().getMessageBoxService().putMessage(
                                                              messageBoxID, sqsMessage);
        } catch (MessageBoxException e) {
            log.error("Can not put the message ", e);
        }
    }

    private String getMD5OfMessage(String messageBody) {
        String MD5OfMessage = null;
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
            digest.update(messageBody.getBytes());
            byte[] hash = digest.digest();
            MD5OfMessage = Base64Utils.encode(hash);
        } catch (NoSuchAlgorithmException e) {
            log.debug("Failed to get MD5 hash in message " + messageBody);
        }
        return MD5OfMessage;
    }
}
