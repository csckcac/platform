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

package org.wso2.carbon.apimgt.handlers.security;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.synapse.*;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.core.axis2.Axis2Sender;
import org.apache.synapse.rest.AbstractHandler;
import org.apache.synapse.transport.nhttp.NhttpConstants;
import org.wso2.carbon.apimgt.handlers.Utils;
import org.wso2.carbon.apimgt.handlers.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.handlers.security.oauth.OAuthAuthenticator;

import java.util.HashMap;
import java.util.Map;

/**
 * Authentication handler for REST APIs exposed in the API gateway. This handler will
 * drop the requests if an authentication failure occurs. But before a message is dropped
 * it looks for a special custom error handler sequence APISecurityConstants.API_AUTH_FAILURE_HANDLER
 * through which the message will be mediated when available. This is a custom extension point
 * provided to the users to handle authentication failures in a deployment specific manner.
 * Once the custom error handler has been invoked, this implementation will further try to
 * respond to the client with a 401 Unauthorized response. If this is not required, the users
 * must drop the message in their custom error handler itself.
 *
 * If no authentication errors are encountered, this will add some AuthenticationContext
 * information to the request and let it through to the next handler in the chain.
 */
public class APIAuthenticationHandler extends AbstractHandler implements ManagedLifecycle {
    
    private static final Log log = LogFactory.getLog(APIAuthenticationHandler.class);

    private volatile Authenticator authenticator;

    public void init(SynapseEnvironment synapseEnvironment) {
        log.debug("Initializing API authentication handler instance");
        String authenticatorType = ServiceReferenceHolder.getInstance().getAPIManagerConfiguration().
                getFirstProperty(APISecurityConstants.API_SECURITY_AUTHENTICATOR);
        if (authenticatorType == null) {
            authenticatorType = OAuthAuthenticator.class.getName();
        }
        try {
            authenticator = (Authenticator) Class.forName(authenticatorType).newInstance();
        } catch (Exception e) {
            // Just throw it here - Synapse will handle it
            throw new SynapseException("Error while initializing authenticator of " +
                    "type: " + authenticatorType);
        }
        authenticator.init(synapseEnvironment);
    }

    public void destroy() {
        log.debug("Destroying API authentication handler instance");
        authenticator.destroy();
    }

    public boolean handleRequest(MessageContext messageContext) {
        try {
            if (authenticator.authenticate(messageContext)) {
                return true;
            }
        } catch (APISecurityException e) {
            log.error("API authentication failure", e);
            handleAuthFailure(messageContext, e);
        }
        return false;
    }

    public boolean handleResponse(MessageContext messageContext) {
        return true;
    }

    private void handleAuthFailure(MessageContext messageContext, APISecurityException e) {
        messageContext.setProperty(SynapseConstants.ERROR_CODE, e.getErrorCode());
        messageContext.setProperty(SynapseConstants.ERROR_MESSAGE, "Authentication failure");
        messageContext.setProperty(SynapseConstants.ERROR_EXCEPTION, e);
        
        Mediator sequence = messageContext.getSequence(APISecurityConstants.API_AUTH_FAILURE_HANDLER);
        // Invoke the custom error handler specified by the user
        if (sequence != null && !sequence.mediate(messageContext)) {
            // If needed user should be able to prevent the rest of the fault handling
            // logic from getting executed
            return;
        }
        
        // By default we send a 401 response back
        org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) messageContext).
                getAxis2MessageContext();
        
        if (e.getErrorCode() == APISecurityConstants.API_AUTH_GENERAL_ERROR) {
            axis2MC.setProperty(NhttpConstants.HTTP_SC, HttpStatus.SC_INTERNAL_SERVER_ERROR);
        } else {
            axis2MC.setProperty(NhttpConstants.HTTP_SC, HttpStatus.SC_UNAUTHORIZED);
            Map<String,String> headers = new HashMap<String,String>();
            headers.put(HttpHeaders.WWW_AUTHENTICATE, authenticator.getChallengeString());
            axis2MC.setProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS, headers);
        }
        messageContext.setResponse(true);
        messageContext.setProperty("RESPONSE", "true");
        messageContext.setTo(null);
        if (messageContext.isDoingPOX() || messageContext.isDoingGET()) {
            Utils.setFaultPayload(messageContext, getFaultPayload(e));
        } else {
            Utils.setSOAPFault(messageContext, "Client", "Authentication Failure", e.getMessage());
        }
        axis2MC.removeProperty("NO_ENTITY_BODY");
        Axis2Sender.sendBack(messageContext);
    }

    private OMElement getFaultPayload(APISecurityException e) {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace ns = fac.createOMNamespace(APISecurityConstants.API_SECURITY_NS, 
                APISecurityConstants.API_SECURITY_NS_PREFIX);
        OMElement payload = fac.createOMElement("fault", ns);

        OMElement errorCode = fac.createOMElement("code", ns);
        errorCode.setText(String.valueOf(e.getErrorCode()));
        OMElement errorMessage = fac.createOMElement("message", ns);
        errorMessage.setText("Authentication Failure");
        OMElement errorDetail = fac.createOMElement("description", ns);
        errorDetail.setText(e.getMessage());

        payload.addChild(errorCode);
        payload.addChild(errorMessage);
        payload.addChild(errorDetail);
        return payload;
    }
}
