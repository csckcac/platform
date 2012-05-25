/*
 *   Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.appserver.integration.tests.utils;

import org.apache.axis2.client.async.AxisCallback;
import org.apache.axis2.context.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class HelloServiceCallbackHandler implements AxisCallback {

    public boolean isComplete = false;
    private MessageContext messageContext = null;
    private static final Log log = LogFactory.getLog(HelloServiceCallbackHandler.class);

    public HelloServiceCallbackHandler() {

    }

    public void onMessage(MessageContext msgContext) {
        log.info("asynchronous response received.");
        this.messageContext = msgContext;
    }

    public void onFault(MessageContext msgContext) {
        Exception e = msgContext.getFailureReason();
        String exMessage = "";

        if (e != null) {
            exMessage = e.getMessage();
        }

        log.error("Fault in asynchronous request -" + exMessage, e);
    }

    public void onError(Exception e) {
        String exMessage = "";
        if (e != null) {
            exMessage = e.getMessage();
        }

        log.error("Fault in asynchronous request -" + exMessage, e);
    }

    public void onComplete() {
        log.info("Asynchronous Message request+response completed");
        isComplete = true;
    }

    public MessageContext getMessageContext() {
        return messageContext;
    }
}
    