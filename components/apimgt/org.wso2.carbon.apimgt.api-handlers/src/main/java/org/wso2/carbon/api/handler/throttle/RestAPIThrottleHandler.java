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
package org.wso2.carbon.api.handler.throttle;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.clustering.ClusteringAgent;
import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.state.Replicator;
import org.apache.axis2.context.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.neethi.PolicyEngine;
import org.apache.synapse.*;
import org.apache.synapse.MessageContext;
import org.apache.synapse.config.Entry;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.AbstractHandler;
import org.apache.synapse.rest.RESTConstants;
import org.apache.synapse.transport.nhttp.NhttpConstants;
import org.wso2.carbon.api.handler.throttle.rolebase.AuthenticationFuture;
import org.wso2.carbon.api.handler.throttle.rolebase.AuthenticatorFactory;
import org.wso2.carbon.api.handler.throttle.rolebase.UserPriviligesHandler;
import org.wso2.carbon.api.handler.throttle.utils.StatCollector;
import org.wso2.throttle.*;

import java.util.HashMap;
import java.util.Map;

public class RestAPIThrottleHandler extends AbstractHandler {

    public static final String O_AUTH_HEADER = CarbonAPIThrottleConstants._O_AUTH_HEADER;
    Log log = LogFactory.getLog(RestAPIThrottleHandler.class);

    /* The key for getting the throttling policy - key refers to a/an [registry] entry    */
    private String policyKey = null;
    /* The concurrect access control group id */
    private String id;
    /* Access rate controller - limit the remote caller access*/
    private AccessRateController accessControler;

    private RoleBasedAccessRateController roleBasedAccessController;

    /* ConcurrentAccessController - limit the remote calleres concurrent access */
    private ConcurrentAccessController concurrentAccessController = null;
    /* The property key that used when the ConcurrentAccessController
       look up from ConfigurationContext */
    private String key;
    /* Is this env. support clustering*/
    private boolean isClusteringEnable = false;
    /* The Throttle object - holds all runtime and configuration data */
    private Throttle throttle;
    /* Lock used to ensure thread-safe creation of the throttle */
    private final Object throttleLock = new Object();
    /* Last version of dynamic policy resource*/
    private long version;

    private AuthenticatorFactory authFactoryForThrottling = CarbonAPIThrottleConstants._DEFAULT_AUTH_FACTORY;

    private String  authFactory = CarbonAPIThrottleConstants._AUTH_FACTORY;

    public RestAPIThrottleHandler() {
        this.accessControler = new AccessRateController();
        this.roleBasedAccessController = new RoleBasedAccessRateController();
    }


    public boolean mediate(MessageContext synCtx) {
        boolean isResponse = synCtx.isResponse();
        ConfigurationContext cc;
        org.apache.axis2.context.MessageContext axisMC;

        if (log.isDebugEnabled()) {
            log.debug("Start : Throttle API handler");
        }
        // To ensure the creation of throttle is thread safe Ã¢â‚¬â€œ It is possible create same throttle
        // object multiple times  by multiple threads.

        synchronized (throttleLock) {

            // get Axis2 MessageContext and ConfigurationContext
            axisMC = ((Axis2MessageContext) synCtx).getAxis2MessageContext();
            cc = axisMC.getConfigurationContext();

            //To ensure check for clustering environment only happens one time
            if ((throttle == null && !isResponse) || (isResponse
                    && concurrentAccessController == null)) {
                ClusteringAgent clusteringAgent = cc.getAxisConfiguration().getClusteringAgent();
                if (clusteringAgent != null &&
                        clusteringAgent.getStateManager() != null) {
                    isClusteringEnable = true;
                }
            }

            // Throttle only will be created ,if the massage flow is IN
            if (!isResponse) {
                //check the availability of the ConcurrentAccessController
                //if this is a clustered environment
                if (isClusteringEnable) {
                    concurrentAccessController =
                            (ConcurrentAccessController) cc.getProperty(key);
                }
                // for request messages, read the policy for throttling and initialize
                if (policyKey != null) {

                    // If the policy has specified as a registry key.
                    // load or re-load policy from registry or local entry if not already available

                    Entry entry = synCtx.getConfiguration().getEntryDefinition(policyKey);
                    if (entry == null) {
                        handleException("Cannot find throttling policy using key : "
                                + policyKey, synCtx);

                    } else {
                        boolean reCreate = false;
                        // if the key refers to a dynamic resource
                        if (entry.isDynamic()) {
                            if ((!entry.isCached() || entry.isExpired()) &&
                                    version != entry.getVersion()) {
                                reCreate = true;
                                version = entry.getVersion();
                            }
                        }
                        if (reCreate || throttle == null) {
                            Object entryValue = synCtx.getEntry(policyKey);
                            if (entryValue == null) {
                                handleException(
                                        "Null throttling policy returned by Entry : "
                                                + policyKey, synCtx);

                            } else {
                                if (!(entryValue instanceof OMElement)) {
                                    handleException("Policy returned from key : " + policyKey +
                                            " is not an OMElement", synCtx);

                                } else {
                                    //Check for reload in a cluster environment Ã¢â‚¬â€œ
                                    // For clustered environment ,if the concurrent access controller
                                    // is not null and throttle is not null , then must reload.
                                    if (isClusteringEnable && concurrentAccessController != null
                                            && throttle != null) {
                                        concurrentAccessController = null; // set null ,
                                        // because need reload
                                    }

                                    try {
                                        // Creates the throttle from the policy
                                        throttle = ThrottleFactory.createMediatorThrottle(
                                                PolicyEngine.getPolicy((OMElement) entryValue));

                                        //For non-clustered  environment , must re-initiates
                                        //For  clustered  environment,
                                        //concurrent access controller is null ,
                                        //then must re-initiates
                                        if (throttle != null && (concurrentAccessController == null
                                                || !isClusteringEnable)) {
                                            concurrentAccessController =
                                                    throttle.getConcurrentAccessController();
                                            if (concurrentAccessController != null) {
                                                cc.setProperty(key, concurrentAccessController);
                                            } else {
                                                cc.removeProperty(key);
                                            }
                                        }
                                    } catch (ThrottleException e) {
                                        handleException("Error processing the throttling policy",
                                                e, synCtx);
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // if the message flow path is OUT , then must lookp from ConfigurationContext -
                // never create ,just get the existing one
                concurrentAccessController =
                        (ConcurrentAccessController) cc.getProperty(key);
            }
        }
        //perform concurrency throttling
        boolean canAccess = doThrottleByConcurrency(isResponse);

        //if the access is success through concurrency throttle and if this is a request message
        //then do access rate based throttling
        if (throttle != null && !isResponse && canAccess) {
            canAccess = throttleByAccessRate(synCtx, axisMC, cc);

            if(canAccess){
                doRoleBasedAccessThrottling(synCtx, axisMC, cc);
            }
        }
        // all the replication functionality of the access rate based throttling handles by itself
        // Just replicate the current state of ConcurrentAccessController
        if (isClusteringEnable && concurrentAccessController != null) {
            if (cc != null) {
                try {
                    if (log.isDebugEnabled()) {
                        log.debug("Going to replicates the  " +
                                "states of the ConcurrentAccessController with key : " + key);
                    }
                    Replicator.replicate(cc);
                } catch (ClusteringFault clusteringFault) {
                    handleException("Error during the replicating  states ",
                            clusteringFault, synCtx);
                }
            }
        }
        if (!canAccess) {
            handleException("Rejected throttling for API", synCtx);
        }

        return canAccess;
    }

    /**
     * Helper method that handles the concurrent access through throttle
     *
     * @param isResponse Current Message is response or not
     * @return true if the caller can access ,o.w. false
     */
    private boolean doThrottleByConcurrency(boolean isResponse) {
        boolean canAcess = true;
        if (concurrentAccessController != null) {
            // do the concurrecy throttling
            int concurrentLimit = concurrentAccessController.getLimit();
            if (log.isDebugEnabled()) {
                log.debug("Concurrent access controller for ID : " + id +
                        " allows : " + concurrentLimit + " concurrent accesses");
            }
            int available;
            if (!isResponse) {
                available = concurrentAccessController.getAndDecrement();
                canAcess = available > 0;
                if (log.isDebugEnabled()) {
                    log.debug("Concurrency Throttle : Access " +
                            (canAcess ? "allowed" : "denied") + " :: " + available
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
        return canAcess;
    }

    /**
     * Helper method that handles the access-rate based throttling
     *
     * @param synCtx MessageContext(Synapse)
     * @param axisMC MessageContext(Axis2)
     * @param cc     ConfigurationContext
     * @return ue if the caller can access ,o.w. false
     */
    private boolean throttleByAccessRate(MessageContext synCtx,
                                         org.apache.axis2.context.MessageContext axisMC,
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

                        //If this is a clusterred env.
                        if (isClusteringEnable) {
                            context.setConfigurationContext(cc);
                            context.setThrottleId(id);
                        }

                        try {
                            //Checks for access state
                            AccessInformation accessInformation = accessControler.canAccess(context,
                                    callerId, ThrottleConstants.DOMAIN_BASE);
                            canAccess = accessInformation.isAccessAllowed();
                            StatCollector.collect(accessInformation, domainName, ThrottleConstants.DOMAIN_BASE);
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
                            handleException("Error occurd during throttling", e, synCtx);
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
                                AccessInformation accessInformation = accessControler.canAccess(
                                        context,
                                        callerId,
                                        ThrottleConstants.IP_BASE);

                                canAccess = accessInformation.isAccessAllowed();
                                StatCollector.collect(accessInformation, remoteIP, ThrottleConstants.IP_BASE);
                                if (log.isDebugEnabled()) {
                                    log.debug("Access " +
                                            (canAccess ? "allowed" : "denied")
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
                    handleException("Error occurd during throttling", e, synCtx);
                }
            }
        }
        return canAccess;
    }

    /**
     * Helper method for handling role based Access throttling
     *
     *
     * @param synCtx
     * @param messageContext             MessageContext - message level states
     * @param cc
     * @return true if access is allowed through concurrent throttling ,o.w false
     */
    private boolean doRoleBasedAccessThrottling(MessageContext synCtx, org.apache.axis2.context.MessageContext messageContext,
                                                ConfigurationContext cc) {

        boolean canAccess = true;

        if (throttle.getThrottleContext(ThrottleConstants.ROLE_BASED_THROTTLE_KEY) == null) {
            //there is no throttle configuration for RoleBase Throttling
            //skip role base throttling
            return canAccess;
        }
//        String throttleId = throttle.getId();
        ConcurrentAccessController cac = null;
        if (isClusteringEnable) {
            // for clustered  env.,gets it from axis configuration context
            cac = (ConcurrentAccessController) cc.getProperty(key);
        }

        if (!synCtx.isResponse()) {
            //gets the remote caller role name
            String consumerKey = null;
            boolean isAuthenticated = false;
            String roleID = null;
            Object headers = messageContext.getProperty(
                        org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);

            if (headers != null && headers instanceof Map) {
                Map headersMap = (Map) headers;
//                String oAuthHeader = (String) headersMap.get(O_AUTH_HEADER);
//                consumerKey = Utils.extractCustomerKeyFromAuthHeader(oAuthHeader);
//                roleID = Utils.extractCustomerKeyFromAuthHeader(oAuthHeader);
                Map settings = new HashMap(headersMap);
                settings.put(RESTConstants.SYNAPSE_REST_API,synCtx.getProperty(RESTConstants.SYNAPSE_REST_API));
                settings.put(RESTConstants.SYNAPSE_REST_API_VERSION,synCtx.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION));
                settings.put(RESTConstants.REST_FULL_REQUEST_PATH,synCtx.getProperty(RESTConstants.REST_FULL_REQUEST_PATH));
                settings.put(RESTConstants.REST_API_CONTEXT,synCtx.getProperty(RESTConstants.REST_API_CONTEXT));
                UserPriviligesHandler handler = authFactoryForThrottling.createAuthenticationHandler(settings);
                AuthenticationFuture authFuture = handler.getAuthenticator();

                consumerKey = authFuture.getAPIKey();
                //do authentication
                handler.authenticateUser();

                isAuthenticated = authFuture.isAuthenticated();

                if (isAuthenticated) {
                    //get feedback from the authenritcator
                    roleID = (String) authFuture.getAuthorizedRoles().get(0);
                }
            }

            if(!isAuthenticated){
                handleException(" Access deny for a " +
                                "caller with consumer Key: " + consumerKey + " " +
                                " : Reason : Authentication failure", synCtx);

            }
            // Domain name based throttling
                //check whether a configuration has been defined for this role name or not
                String consumerRoleID = null;
                if (consumerKey != null && isAuthenticated) {
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
                                AccessInformation infor =  null;
                                try {
                                    infor = roleBasedAccessController.canAccess(context, consumerKey,
                                                                        consumerRoleID);
                                } catch (ThrottleException e) {
                                    handleException("Exception ocurred while performing role based throttling",e, synCtx);
                                }
                                StatCollector.collect(infor, consumerKey, ThrottleConstants.ROLE_BASE);
                                //check for the permission for access
                                if (!infor.isAccessAllowed()) {

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
                                    handleException(" Access deny for a " +
                                                    "caller with consumerKey " + consumerKey + " with Role :"
                                                    + consumerRoleID +
                                                    " : Reason : " + infor.getFaultReason(), synCtx);
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

    protected void handleException(String msg, Exception e, MessageContext msgContext) {
        log.error(msg, e);
        if (msgContext.getServiceLog() != null) {
            msgContext.getServiceLog().error(msg, e);
        }
        throw new SynapseException(msg, e);
    }

    protected void handleException(String msg, MessageContext msgContext) {
        log.error(msg);
        if (msgContext.getServiceLog() != null) {
            msgContext.getServiceLog().error(msg);
        }
        throw new SynapseException(msg);
    }




    public boolean handleRequest(MessageContext messageContext) {
        return mediate(messageContext);
    }

    public boolean handleResponse(MessageContext messageContext) {
        return mediate(messageContext);
    }


    public String  getAuthFactory() {
        return authFactory;
    }

    public void setAuthFactory(String  authFactory) {
        this.authFactory = authFactory;
        try {
            if (authFactory != null && !"".equals(authFactory.trim())) {
                authFactoryForThrottling = (AuthenticatorFactory) Class.forName(authFactory).newInstance();
            }
        } catch (Exception e) {
            log.warn("unable to create factory instnace for authentication handling for class: " +
                     authFactory + " Error :" + e.getMessage());
        }
    }
}
