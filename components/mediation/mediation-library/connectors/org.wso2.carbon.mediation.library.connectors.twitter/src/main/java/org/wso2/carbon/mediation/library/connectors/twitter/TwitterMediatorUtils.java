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
package org.wso2.carbon.mediation.library.connectors.twitter;

import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.mediators.template.TemplateContext;
import twitter4j.Status;

import java.util.Stack;

public class TwitterMediatorUtils {

    public static String lookupFunctionParam(MessageContext ctxt, String paramName) {
        Stack<TemplateContext> funcStack = (Stack) ctxt.getProperty(SynapseConstants.SYNAPSE__FUNCTION__STACK);
        TemplateContext currentFuncHolder = funcStack.peek();
        String paramValue = (String) currentFuncHolder.getParameterValue(paramName);
        return paramValue;
    }

    public static void storeResponseStatus(MessageContext ctxt, Status status) {
        ctxt.setProperty(TwitterConnectConstants.TWITTER_STATUS_USER_SCREEN_NAME, status.getUser().getScreenName());
        ctxt.setProperty(TwitterConnectConstants.TWITTER_STATUS_STATUS_TEXT, status.getText());
        ctxt.setProperty(TwitterConnectConstants.TWITTER_API_RESPONSE, status);
    }

    public static void storeLoginUser(MessageContext ctxt, String consumerKey, String consumerSecret, String accessToken, String accessTokenSecret){
        ctxt.setProperty(TwitterConnectConstants.TWITTER_USER_CONSUMER_KEY, consumerKey);
        ctxt.setProperty(TwitterConnectConstants.TWITTER_USER_CONSUMER_SECRET, consumerSecret);
        ctxt.setProperty(TwitterConnectConstants.TWITTER_USER_ACCESS_TOKEN, accessToken);
        ctxt.setProperty(TwitterConnectConstants.TWITTER_USER_ACCESS_TOKEN_SECRET, accessTokenSecret);
    }

    public static void storeErrorResponseStatus(MessageContext ctxt, Exception e) {
        ctxt.setProperty(SynapseConstants.ERROR_EXCEPTION, e);
        ctxt.setProperty(SynapseConstants.ERROR_MESSAGE, e.getMessage());
    }
}
