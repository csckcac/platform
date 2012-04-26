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

package org.wso2.carbon.apimgt.handlers.throttling;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.clustering.ClusteringAgent;
import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.state.Replicator;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.apache.neethi.PolicyEngine;
import org.apache.synapse.Mediator;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.SynapseException;
import org.apache.synapse.config.Entry;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.core.axis2.Axis2Sender;
import org.apache.synapse.rest.AbstractHandler;
import org.apache.synapse.transport.nhttp.NhttpConstants;
import org.wso2.carbon.apimgt.handlers.security.APISecurityConstants;
import org.wso2.carbon.apimgt.handlers.security.APISecurityException;
import org.wso2.carbon.apimgt.handlers.security.APISecurityUtils;
import org.wso2.carbon.apimgt.handlers.security.AuthenticationContext;
import org.wso2.throttle.*;

public class APIThrottleHandler extends AbstractHandler {

    private static final Log log = LogFactory.getLog(APIThrottleHandler.class);

    /** The Throttle object - holds all runtime and configuration data */
    private volatile Throttle throttle;
    /** ConcurrentAccessController - limit the remote callers concurrent access */
    private ConcurrentAccessController concurrentAccessController = null;
    /** Access rate controller - limit the remote caller access*/
    private AccessRateController accessController;

    private RoleBasedAccessRateController roleBasedAccessController;

    /** The property key that used when the ConcurrentAccessController
     look up from ConfigurationContext */
    private String key;
    /** The key for getting the throttling policy - key refers to a/an [registry] entry    */
    private String policyKey = null;
    /** The concurrent access control group id */
    private String id;
    /** Version number of the throttle policy */
    private long version;

    /** Does this env. support clustering*/
    private boolean isClusteringEnable = false;

    public APIThrottleHandler() {
        this.accessController = new AccessRateController();
        this.roleBasedAccessController = new RoleBasedAccessRateController();
    }

    public boolean handleRequest(MessageContext messageContext) {
        return doThrottle(messageContext);
    }

    public boolean handleResponse(MessageContext messageContext) {
        return doThrottle(messageContext);
    }

    private boolean doThrottle(MessageContext messageContext) {
        boolean isResponse = messageContext.isResponse();
        org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) messageContext).
                getAxis2MessageContext();
        ConfigurationContext cc = axis2MC.getConfigurationContext();
        if (throttle == null) {
            synchronized (this) {
                if (throttle == null) {
                    ClusteringAgent clusteringAgent = cc.getAxisConfiguration().getClusteringAgent();
                    isClusteringEnable = (clusteringAgent != null &&
                            clusteringAgent.getStateManager() != null);

                    if (!isResponse) {
                        //check the availability of the ConcurrentAccessController
                        //if this is a clustered environment
                        if (isClusteringEnable) {
                            concurrentAccessController = (ConcurrentAccessController) cc.getProperty(key);
                        }
                        initThrottle(messageContext, cc);
                    } else {
                        // if the message flow path is OUT , then must lookup from ConfigurationContext -
                        // never create ,just get the existing one
                        concurrentAccessController = (ConcurrentAccessController) cc.getProperty(key);
                    }
                }
            }
        }

        // perform concurrency throttling
        boolean canAccess = doThrottleByConcurrency(isResponse);
        // if the access is success through concurrency throttle and if this is a request message
        // then do access rate based throttling
        if (canAccess && !isResponse && throttle != null) {
            canAccess = throttleByAccessRate(axis2MC, cc) &&
                    doRoleBasedAccessThrottling(messageContext, cc);
        }

        // All the replication functionality of the access rate based throttling handled by itself
        // Just replicate the current state of ConcurrentAccessController
        if (isClusteringEnable && concurrentAccessController != null) {
            if (cc != null) {
                try {
                    Replicator.replicate(cc);
                } catch (ClusteringFault clusteringFault) {
                    handleException("Error during the replicating  states ", clusteringFault);
                }
            }
        }

        if (!canAccess) {
            handleThrottleOut(messageContext);
            return false;
        }
        return true;
    }

    private void handleThrottleOut(MessageContext messageContext) {
        messageContext.setProperty(SynapseConstants.ERROR_CODE, 900800);
        messageContext.setProperty(SynapseConstants.ERROR_MESSAGE, "Message throttled out");

        Mediator sequence = messageContext.getSequence(APIThrottleConstants.API_THROTTLE_OUT_HANDLER);
        // Invoke the custom error handler specified by the user
        if (sequence != null && !sequence.mediate(messageContext)) {
            // If needed user should be able to prevent the rest of the fault handling
            // logic from getting executed
            return;
        }

        // By default we send a 401 response back
        org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) messageContext).
                getAxis2MessageContext();

        axis2MC.setProperty(NhttpConstants.HTTP_SC, HttpStatus.SC_SERVICE_UNAVAILABLE);
        messageContext.setResponse(true);
        messageContext.setProperty("RESPONSE", "true");
        messageContext.setTo(null);

        OMElement firstChild = messageContext.getEnvelope().getBody().getFirstElement();
        if (firstChild != null) {
            firstChild.insertSiblingAfter(getFaultPayload());
            firstChild.detach();
        } else {
            messageContext.getEnvelope().getBody().addChild(getFaultPayload());
        }
        axis2MC.removeProperty("NO_ENTITY_BODY");
        Axis2Sender.sendBack(messageContext);
    }

    private OMElement getFaultPayload() {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace ns = fac.createOMNamespace(APIThrottleConstants.API_THROTTLE_NS,
                APIThrottleConstants.API_THROTTLE_NS_PREFIX);
        OMElement payload = fac.createOMElement("fault", ns);

        OMElement errorCode = fac.createOMElement("code", ns);
        errorCode.setText(String.valueOf(APIThrottleConstants.THROTTLE_OUT_ERROR_CODE));
        OMElement errorMessage = fac.createOMElement("message", ns);
        errorMessage.setText("Message Throttled Out");
        OMElement errorDetail = fac.createOMElement("description", ns);
        errorDetail.setText("You have exceeded your quota");

        payload.addChild(errorCode);
        payload.addChild(errorMessage);
        payload.addChild(errorDetail);
        return payload;
    }

    private boolean doThrottleByConcurrency(boolean isResponse) {
        boolean canAccess = true;
        if (concurrentAccessController != null) {
            // do the concurrency throttling
            int concurrentLimit = concurrentAccessController.getLimit();
            if (log.isDebugEnabled()) {
                log.debug("Concurrent access controller for ID: " + id +
                        " allows: " + concurrentLimit + " concurrent accesses");
            }
            int available;
            if (!isResponse) {
                available = concurrentAccessController.getAndDecrement();
                canAccess = available > 0;
                if (log.isDebugEnabled()) {
                    log.debug("Concurrency Throttle: Access " +
                            (canAccess ? "allowed" : "denied") + " :: " + available
                            + " of available of " + concurrentLimit + " connections");
                }
            } else {
                available = concurrentAccessController.incrementAndGet();
                if (log.isDebugEnabled()) {
                    log.debug("Concurrency Throttle : Connection returned" + " :: " +
                            available + " of available of " + concurrentLimit + " connections");
                }
            }
        }
        return canAccess;
    }

    private boolean throttleByAccessRate(org.apache.axis2.context.MessageContext axisMC,
                                         ConfigurationContext cc) {

        String callerId = null;
        boolean canAccess = true;
        //remote ip of the caller
        String remoteIP = (String) axisMC.getPropertyNonReplicable(
                org.apache.axis2.context.MessageContext.REMOTE_ADDR);
        //domain name of the caller
        String domainName = (String) axisMC.getPropertyNonReplicable(NhttpConstants.REMOTE_HOST);

        //Using remote caller domain name , If there is a throttle configuration for
        // this domain name ,then throttling will occur according to that configuration
        if (domainName != null) {
            // do the domain based throttling
            if (log.isDebugEnabled()) {
                log.debug("The Domain Name of the caller is :" + domainName);
            }
            // loads the DomainBasedThrottleContext
            ThrottleContext context
                    = throttle.getThrottleContext(ThrottleConstants.DOMAIN_BASED_THROTTLE_KEY);
            if (context != null) {
                //loads the DomainBasedThrottleConfiguration
                ThrottleConfiguration config = context.getThrottleConfiguration();
                if (config != null) {
                    //checks the availability of a policy configuration for  this domain name
                    callerId = config.getConfigurationKeyOfCaller(domainName);
                    if (callerId != null) {  // there is configuration for this domain name

                        //If this is a clustered env.
                        if (isClusteringEnable) {
                            context.setConfigurationContext(cc);
                            context.setThrottleId(id);
                        }

                        try {
                            //Checks for access state
                            AccessInformation accessInformation = accessController.canAccess(context,
                                    callerId, ThrottleConstants.DOMAIN_BASE);
                            canAccess = accessInformation.isAccessAllowed();
                            if (log.isDebugEnabled()) {
                                log.debug("Access " + (canAccess ? "allowed" : "denied")
                                        + " for Domain Name : " + domainName);
                            }

                            //In the case of both of concurrency throttling and
                            //rate based throttling have enabled ,
                            //if the access rate less than maximum concurrent access ,
                            //then it is possible to occur death situation.To avoid that reset,
                            //if the access has denied by rate based throttling
                            if (!canAccess && concurrentAccessController != null) {
                                concurrentAccessController.incrementAndGet();
                                if (isClusteringEnable) {
                                    cc.setProperty(key, concurrentAccessController);
                                }
                            }
                        } catch (ThrottleException e) {
                            handleException("Error occurred during throttling", e);
                        }
                    }
                }
            }
        } else {
            log.debug("The Domain name of the caller cannot be found");
        }

        //At this point , any configuration for the remote caller hasn't found ,
        //therefore trying to find a configuration policy based on remote caller ip
        if (callerId == null) {
            //do the IP-based throttling
            if (remoteIP == null) {
                if (log.isDebugEnabled()) {
                    log.debug("The IP address of the caller cannot be found");
                }
                canAccess = true;

            } else {
                if (log.isDebugEnabled()) {
                    log.debug("The IP Address of the caller is :" + remoteIP);
                }
                try {
                    // Loads the IPBasedThrottleContext
                    ThrottleContext context =
                            throttle.getThrottleContext(ThrottleConstants.IP_BASED_THROTTLE_KEY);
                    if (context != null) {
                        //Loads the IPBasedThrottleConfiguration
                        ThrottleConfiguration config = context.getThrottleConfiguration();
                        if (config != null) {
                            //Checks the availability of a policy configuration for  this ip
                            callerId = config.getConfigurationKeyOfCaller(remoteIP);
                            if (callerId != null) {   // there is configuration for this ip

                                //For clustered env.
                                if (isClusteringEnable) {
                                    context.setConfigurationContext(cc);
                                    context.setThrottleId(id);
                                }
                                //Checks access state
                                AccessInformation accessInformation = accessController.canAccess(
                                        context,
                                        callerId,
                                        ThrottleConstants.IP_BASE);

                                canAccess = accessInformation.isAccessAllowed();
                                if (log.isDebugEnabled()) {
                                    log.debug("Access " + (canAccess ? "allowed" : "denied")
                                            + " for IP : " + remoteIP);
                                }
                                //In the case of both of concurrency throttling and
                                //rate based throttling have enabled ,
                                //if the access rate less than maximum concurrent access ,
                                //then it is possible to occur death situation.To avoid that reset,
                                //if the access has denied by rate based throttling
                                if (!canAccess && concurrentAccessController != null) {
                                    concurrentAccessController.incrementAndGet();
                                    if (isClusteringEnable) {
                                        cc.setProperty(key, concurrentAccessController);
                                    }
                                }
                            }
                        }
                    }
                } catch (ThrottleException e) {
                    handleException("Error occurred during throttling", e);
                }
            }
        }
        return canAccess;
    }

    private boolean doRoleBasedAccessThrottling(MessageContext synCtx, ConfigurationContext cc) {

        boolean canAccess = true;

        if (throttle.getThrottleContext(ThrottleConstants.ROLE_BASED_THROTTLE_KEY) == null) {
            //there is no throttle configuration for RoleBase Throttling
            //skip role base throttling
            return canAccess;
        }

        ConcurrentAccessController cac = null;
        if (isClusteringEnable) {
            // for clustered  env.,gets it from axis configuration context
            cac = (ConcurrentAccessController) cc.getProperty(key);
        }

        if (!synCtx.isResponse()) {
            //gets the remote caller role name
            AuthenticationContext authContext = APISecurityUtils.getAuthenticationContext(synCtx);
            String consumerKey = authContext.getApiKey();
            String roleID = authContext.getTier();

            // Domain name based throttling
            //check whether a configuration has been defined for this role name or not
            String consumerRoleID;
            if (consumerKey != null) {
                //loads the ThrottleContext
                ThrottleContext context =
                        throttle.getThrottleContext(ThrottleConstants.ROLE_BASED_THROTTLE_KEY);
                if (context != null) {
                    //Loads the ThrottleConfiguration
                    ThrottleConfiguration config = context.getThrottleConfiguration();
                    if (config != null) {
                        //check for configuration for this caller
                        consumerRoleID = config.getConfigurationKeyOfCaller(roleID);
                        if (consumerRoleID != null) {
                            // If this is a clustered env.
                            if (isClusteringEnable) {
                                context.setConfigurationContext(cc);
                                context.setThrottleId(id);
                            }

                            AccessInformation info = null;
                            try {
                                info = roleBasedAccessController.canAccess(context, consumerKey,
                                        consumerRoleID);
                            } catch (ThrottleException e) {
                                log.debug("Exception occurred while performing role " +
                                        "based throttling", e);
                                canAccess = false;
                            }

                            //check for the permission for access
                            if (info != null && !info.isAccessAllowed()) {

                                //In the case of both of concurrency throttling and
                                //rate based throttling have enabled ,
                                //if the access rate less than maximum concurrent access ,
                                //then it is possible to occur death situation.To avoid that reset,
                                //if the access has denied by rate based throttling
                                if (cac != null) {
                                    cac.incrementAndGet();
                                    // set back if this is a clustered env
                                    if (isClusteringEnable) {
                                        cc.setProperty(key, cac);
                                        //replicate the current state of ConcurrentAccessController
                                        try {
                                            if (log.isDebugEnabled()) {
                                                log.debug("Going to replicates the " +
                                                        "states of the ConcurrentAccessController" +
                                                        " with key : " + key);
                                            }
                                            Replicator.replicate(cc, new String[]{key});
                                        } catch (ClusteringFault clusteringFault) {
                                            log.error("Error during replicating states ",
                                                    clusteringFault);
                                        }
                                    }
                                }
                                canAccess = false;
                            }
                        } else {
                            if (log.isDebugEnabled()) {
                                log.debug("Could not find the Throttle Context for role-Based " +
                                        "Throttling for role name " + consumerKey + " Throttling for this " +
                                        "role name may not be configured from policy");
                            }
                        }
                    }
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Could not find the API Key of the caller - role based throttling NOT applied");
                }
            }
        }
        return canAccess;
    }

    private void initThrottle(MessageContext synCtx, ConfigurationContext cc) {
        if (policyKey != null) {
            Entry entry = synCtx.getConfiguration().getEntryDefinition(policyKey);
            if (entry == null) {
                handleException("Cannot find throttling policy using key: " + policyKey);
                return;
            }

            boolean reCreate = false;
            // if the key refers to a dynamic resource
            if (entry.isDynamic()) {
                if ((!entry.isCached() || entry.isExpired()) && version != entry.getVersion()) {
                    reCreate = true;
                }
            }

            if (reCreate || throttle == null) {
                Object entryValue = synCtx.getEntry(policyKey);
                if (entryValue == null || !(entryValue instanceof OMElement)) {
                    handleException("Unable to load throttling policy using key: " + policyKey);
                    return;
                }

                version = entry.getVersion();

                //Check for reload in a cluster environment
                // For clustered environment ,if the concurrent access controller
                // is not null and throttle is not null , then must reload.
                if (isClusteringEnable && concurrentAccessController != null && throttle != null) {
                    concurrentAccessController = null; // set null ,
                    // because need to reload
                }

                try {
                    // Creates the throttle from the policy
                    throttle = ThrottleFactory.createMediatorThrottle(
                            PolicyEngine.getPolicy((OMElement) entryValue));

                    //For non-clustered  environment , must re-initiates
                    //For  clustered  environment,
                    //concurrent access controller is null ,
                    //then must re-initiates
                    if (throttle != null && (concurrentAccessController == null || !isClusteringEnable)) {
                        concurrentAccessController = throttle.getConcurrentAccessController();
                        if (concurrentAccessController != null) {
                            cc.setProperty(key, concurrentAccessController);
                        } else {
                            cc.removeProperty(key);
                        }
                    }
                } catch (ThrottleException e) {
                    handleException("Error processing the throttling policy", e);
                }
            }
        }
    }

    public void setId(String id) {
        this.id = id;
        this.key = ThrottleConstants.THROTTLE_PROPERTY_PREFIX + id + ThrottleConstants.CAC_SUFFIX;
    }

    public String getId(){
        return id;
    }
    public void setPolicyKey(String policyKey){
        this.policyKey = policyKey;
    }

    public String gePolicyKey(){
        return policyKey;
    }

    protected void handleException(String msg, Exception e) {
        log.error(msg, e);
        throw new SynapseException(msg, e);
    }

    protected void handleException(String msg) {
        log.error(msg);
        throw new SynapseException(msg);
    }
}
