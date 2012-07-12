/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.gateway.mediators;

import org.apache.axiom.util.base64.Base64Utils;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityUtils;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationContext;

import java.util.Calendar;
import java.util.Map;

/**
 *  Mediator class used to add custom header containing API subscriber's name (callee)
 *  to request message being forwarded to actual endpoint.
 */
public class TokenPasser extends AbstractMediator {

    private static final String JWT_HEADER = "{\"typ\":\"JWT\", \"alg\":\"NONE\"}";
    private static String JWT_BODY = "{\"iss\":\"[1]\", \"exp\":[2], \"http://wso2.org/claims/subscriber\":\"[3]\"}";
    private static final String API_GATEWAY_ID = "wso2.org/products/am";

    public boolean mediate(MessageContext synCtx) {
        AuthenticationContext authContext = APISecurityUtils.getAuthenticationContext(synCtx);
        addHTTPHeader(synCtx,authContext);
        return true;
    }

    private void addHTTPHeader(MessageContext synCtx, AuthenticationContext authContext) {
        //generating expiring timestamp
        long currentTime = Calendar.getInstance().getTimeInMillis();
        //expire the token in 60s after generation
        long expireIn = currentTime + 1000 * 60;

        String base64EncodedHeader = Base64Utils.encode(JWT_HEADER.getBytes());
        String replacedBody = JWT_BODY.replaceAll("\\[1\\]",API_GATEWAY_ID)
                .replaceAll("\\[3\\]",authContext.getUsername())
                .replaceAll("\\[2\\]",String.valueOf(expireIn));

        String base64EncodedBody = Base64Utils.encode(replacedBody.getBytes());
        String assertion = base64EncodedHeader + "." + base64EncodedBody;

        Map transportHeaders = (Map)((Axis2MessageContext) synCtx).getAxis2MessageContext()
                .getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        transportHeaders.put("assertion", assertion);

        if (log.isDebugEnabled()) {
            log.debug("Adding custom header with subscriber name");
            log.debug(synCtx.getMessageID() + ": assertion value : "+assertion);
        }
    }
}
