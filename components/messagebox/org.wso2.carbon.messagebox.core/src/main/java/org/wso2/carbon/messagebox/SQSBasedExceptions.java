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
package org.wso2.carbon.messagebox;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SQSBasedExceptions {
    private static final Log log = LogFactory.getLog(SQSBasedExceptions.class);

    public static MessageBoxException getAccessDeniedException(String messageBoxId) {
        log.error("No permission to access message box, " + messageBoxId);
        return new MessageBoxException("Access to the resource is denied.", "AccessDenied");
    }

    public static MessageBoxException getAuthenticationFailureException() {
        return new MessageBoxException("A value used for authentication could not be validated, " +
                                       "such as Signature.", "AuthFailure");
    }

    public static MessageBoxException getInternalErrorException() {
        return new MessageBoxException("There is an internal problem with SQS,which you cannot resolve." +
                                       " Retry the request.", "InternalError");
    }

    public static MessageBoxException getNonExistingQueueException(String messageBoxId) {
        log.error("No such message box exists with name, " + messageBoxId);
        return new MessageBoxException("Queue does not exist.",
                                       "AWS.SimpleQueueService.NonExistentQueue");
    }

    public static MessageBoxException getQueueAlreadyExistsException(String messageBoxId) {
        log.error(messageBoxId + " already exists.");
        return new MessageBoxException(" Queue already exists. SQS returns this error only if the" +
                                       "  request includes a DefaultVisibilityTimeout value that" +
                                       " differs from the value  for the existing queue.",
                                       "AWS.SimpleQueueService.QueueNameExists ");
    }

    public static MessageBoxException getInvalidAttributeException() {
        return new MessageBoxException("Unknown attribute ", "InvalidAttributeName ");
    }

    public static MessageBoxException getMalformedReceiptHandlerException() {
        return new MessageBoxException("ReceiptHandle is malformed.");
    }

    public static MessageBoxException getMaxNumberOfMessagesNotValidException() {
        return new MessageBoxException("The value for MaxNumberOfMessages is not valid (must be from 1 to 10).",
                                       "ReadCountOutOfRange ");
    }

    public static MessageBoxException getInvalidMessageContentException() {
        return new MessageBoxException("The message contains characters outside the allowed set.",
                                       "InvalidMessageContents ");
    }

    public static MessageBoxException getInvalidParameterValueException() {
        return new MessageBoxException("One or more parameters cannot be validated.",
                                       "InvalidParameterValue ");
    }


}
