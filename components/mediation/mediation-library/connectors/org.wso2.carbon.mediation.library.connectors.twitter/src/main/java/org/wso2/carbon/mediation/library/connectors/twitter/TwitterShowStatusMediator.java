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

import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.mediators.AbstractMediator;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;


public class TwitterShowStatusMediator extends AbstractMediator implements ManagedLifecycle {


    public static final String ID = "id";


    public void init(SynapseEnvironment synapseEnvironment) {

    }

    public void destroy() {
    }

    public boolean mediate(MessageContext messageContext) {
        try {
            String id = TwitterMediatorUtils.lookupFunctionParam(messageContext, ID);
            Twitter twitter = new TwitterClientLoader(messageContext).loadApiClient();
            Status status = twitter.showStatus(Long.parseLong(id));
            TwitterMediatorUtils.storeResponseStatus(messageContext, status);
            System.out.println("@" + status.getUser().getScreenName() + " - " + status.getText());
        } catch (TwitterException te) {
            System.out.println("Failed to show status: " + te.getMessage());
            TwitterMediatorUtils.storeErrorResponseStatus(messageContext, te);
        }
        System.out.println("testing synapse twitter.......");
        return true;
    }

    public static void main(String[] args) {
        new TwitterShowStatusMediator().mediate(null);
    }
}
