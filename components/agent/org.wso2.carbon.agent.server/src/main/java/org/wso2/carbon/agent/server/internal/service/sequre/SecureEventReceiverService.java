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

package org.wso2.carbon.agent.server.internal.service.sequre;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.agent.commons.exception.AuthenticationException;
import org.wso2.carbon.agent.server.internal.EventDispatcher;
import org.wso2.carbon.agent.server.internal.authentication.Authenticator;
import org.wso2.carbon.agent.server.internal.service.general.EventReceiverService;

/**
 * The client implementation for SecureEventReceiverService
 */
public class SecureEventReceiverService extends EventReceiverService{
    private static final Log log = LogFactory.getLog(Authenticator.class);

    public SecureEventReceiverService(EventDispatcher eventDispatcher) {
        super(eventDispatcher);
    }

    public static String connect(String username, String password) throws AuthenticationException {
        log.info(username + " connected");
        try {
            return Authenticator.getInstance().authenticate(username, password);
        } catch (AuthenticationException e) {
            throw new AuthenticationException(username + " is not authorised to access the server "+e.getErrorMessage());
        }
    }

    public static void disconnect(String sessionId) throws Exception {
        log.info(sessionId + " disconnected");
        Authenticator.getInstance().logout(sessionId);
    }
}
