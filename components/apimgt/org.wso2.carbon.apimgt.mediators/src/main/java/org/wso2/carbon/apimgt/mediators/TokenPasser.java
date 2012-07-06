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

package org.wso2.carbon.apimgt.mediators;

import org.apache.axiom.util.base64.Base64Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.wso2.carbon.apimgt.handlers.security.*;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.addressing.RelatesTo;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.mediators.AbstractMediator;
import org.apache.synapse.util.xpath.SynapseXPath;

import java.io.IOException;
import java.util.Calendar;
import java.util.Map;


/**
 *  Mediator class used to add custom header containing API subscriber's name (callee)
 *  to request message being forwarded to actual endpoint.
 */
public class TokenPasser extends AbstractMediator {
    private static final Log log = LogFactory.getLog(TokenPasser.class);
    private static final String JWT_HEADER = "{\"typ\":\"JWT\", \"alg\":\"NONE\"}";
    private static String JWT_BODY = "{\"iss\":\"[1]\", \"exp\":1373100108854, \"http://wso2.org/claims/subscriber\":\"[2]\"}";
    private static final String API_GATEWAY_ID = "wso2.org/products/am";

    public boolean mediate(MessageContext synCtx) {
        AuthenticationContext authContext = APISecurityUtils.getAuthenticationContext(synCtx);
        addHTTPHeader(synCtx,authContext);
        return true;
    }

    private void addHTTPHeader(MessageContext synCtx, AuthenticationContext authContext) {
        String base64EncodedHeader = Base64Utils.encode(JWT_HEADER.getBytes());
        String replacedBody = JWT_BODY.replaceAll("\\[1\\]",API_GATEWAY_ID).replaceAll("\\[2\\]",authContext.getUsername());
        String base64EncodedBody = Base64Utils.encode(replacedBody.getBytes());
        String assertion = base64EncodedHeader + "." + base64EncodedBody;

        Map transportHeaders = (Map)((Axis2MessageContext) synCtx).getAxis2MessageContext()
                .getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        transportHeaders.put("assertion",assertion);

        if(log.isDebugEnabled()){
            log.debug("Adding custom header with subscriber name");
            log.debug(synCtx.getMessageID() + ": assertion value : "+assertion);
        }
    }
}
