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
package org.wso2.carbon.mediation.library.connectors.linkedin.util;

import com.google.code.linkedinapi.client.LinkedInApiClient;
import com.google.code.linkedinapi.client.LinkedInApiClientFactory;
import org.wso2.carbon.mediation.library.connectors.linkedin.LinkedinConstants;
import org.apache.synapse.MessageContext;


public class LinkedInClientLoader {

    private MessageContext messageContext;


    public LinkedInClientLoader(MessageContext ctxt) {
        this.messageContext = ctxt;
    }

    public LinkedInApiClient loadApiClient() {
        LinkedInApiClient client;
        //check for keys in message context - load dynamically
        if (messageContext.getProperty(LinkedinConstants.LINKEDIN_USER_CONSUMER_KEY) != null && messageContext.getProperty(LinkedinConstants.LINKEDIN_USER_CONSUMER_SECRET) != null &&
            messageContext.getProperty(LinkedinConstants.LINKEDIN_USER_ACCESS_TOKEN) != null && messageContext.getProperty(LinkedinConstants.LINKEDIN_USER_ACCESS_TOKEN_SECRET) != null) {
            final LinkedInApiClientFactory factory = LinkedInApiClientFactory.newInstance(messageContext.getProperty(LinkedinConstants.LINKEDIN_USER_CONSUMER_KEY).toString(),
                                                                                          messageContext.getProperty(LinkedinConstants.LINKEDIN_USER_CONSUMER_SECRET).toString());
            client = factory.createLinkedInApiClient(messageContext.getProperty(LinkedinConstants.LINKEDIN_USER_ACCESS_TOKEN).toString(),
                                                     messageContext.getProperty(LinkedinConstants.LINKEDIN_USER_ACCESS_TOKEN_SECRET).toString());
        } else {
            //load defaults
            final LinkedInApiClientFactory factory = LinkedInApiClientFactory.newInstance(LinkedinConstants._CONSUMER_KEY, LinkedinConstants._CONSUMER_SECRET);
            client = factory.createLinkedInApiClient(LinkedinConstants._ACCESS_TOKEN, LinkedinConstants._ACCESS_TOKEN_SECRET);
        }

        return client;
    }


}
