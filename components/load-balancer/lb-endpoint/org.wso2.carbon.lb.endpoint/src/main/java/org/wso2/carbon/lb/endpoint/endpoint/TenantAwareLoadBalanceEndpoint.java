package org.wso2.carbon.lb.endpoint.endpoint;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.clustering.ClusteringAgent;
import org.apache.axis2.clustering.Member;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.protocol.HTTP;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.SynapseException;
import org.apache.synapse.config.xml.endpoints.utils.LoadbalanceAlgorithmFactory;
import org.apache.synapse.core.LoadBalanceMembershipHandler;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.core.axis2.Axis2SynapseEnvironment;
import org.apache.synapse.endpoints.DynamicLoadbalanceFaultHandler;
import org.apache.synapse.endpoints.Endpoint;
import org.apache.synapse.endpoints.algorithms.LoadbalanceAlgorithm;
import org.apache.synapse.endpoints.dispatch.HttpSessionDispatcher;
import org.apache.synapse.endpoints.dispatch.SALSessions;
import org.apache.synapse.endpoints.dispatch.SessionInformation;
import org.apache.synapse.transport.nhttp.NhttpConstants;
import org.wso2.carbon.lb.common.conf.LoadBalancerConfiguration;
import org.wso2.carbon.lb.common.conf.util.HostContext;
import org.wso2.carbon.lb.common.conf.util.TenantDomainContext;
import org.wso2.carbon.lb.endpoint.SubDomainAwareGroupManagementAgent;
import org.wso2.carbon.lb.endpoint.TenantLoadBalanceMembershipHandler;
import org.wso2.carbon.lb.endpoint.cache.URLMappingCache;
import org.wso2.carbon.lb.endpoint.internal.RegistryManager;
import org.wso2.carbon.lb.endpoint.util.ConfigHolder;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

//import org.wso2.carbon.lb.endpoint.util.TenantDomainRangeContext;

public class TenantAwareLoadBalanceEndpoint extends org.apache.synapse.endpoints.DynamicLoadbalanceEndpoint implements Serializable {

    private static final long serialVersionUID = 1577351815951789938L;
    private static final Log log = LogFactory.getLog(TenantAwareLoadBalanceEndpoint.class);
    private String algorithm;
    private String configuration;
    private String failOver;

    /**
     * Axis2 based membership handler which handles members in multiple clustering domains
     */
    private TenantLoadBalanceMembershipHandler tlbMembershipHandler;

    /**
     * Key - host, Value - list of {@link TenantDomainRangeContext}
     */
//    private Map<String, List<TenantDomainRangeContext>> hostDomainMap;
    
    
    private Map<String, HostContext> hostContexts = new HashMap<String, HostContext>();
    
    private LoadBalancerConfiguration lbConfig;

    /**
     *  to keep the host vs url mappings entry.
     */
    private Map<String, String> suffixes = new HashMap<String, String>();

    /**
     * keep the size of cache which used to keep hostNames of url mapping.
     */
    private URLMappingCache mappingCache;
    
    private int sizeOfCache;
    @Override
    public void init(SynapseEnvironment synapseEnvironment) {
        try {

            String configURL = System.getProperty("loadbalancer.conf");
            lbConfig = new LoadBalancerConfiguration();
            lbConfig.init(configURL);
//            hostDomainMap = lbConfig.getHostDomainMap();
            hostContexts = lbConfig.getHostContextMap();
            sizeOfCache = lbConfig.getLoadBalancerConfig().getSizeOfCache();
            mappingCache = new URLMappingCache(sizeOfCache);

        } catch (Exception e) {
            log.error("Error While reading Load Balancer configuration file" + e.toString());
        }


        LoadbalanceAlgorithm algorithm = null;
        try {
            OMElement payload = AXIOMUtil.stringToOM(generatePayLoad());
            algorithm =
                    LoadbalanceAlgorithmFactory.
                            createLoadbalanceAlgorithm(payload, null);
        } catch (Exception e) {
            log.error("Error While creating Load balance algorithm" + e.toString());
        }

        if (!initialized) {
            super.init(synapseEnvironment);
            ConfigurationContext cfgCtx =
                                          ((Axis2SynapseEnvironment) synapseEnvironment).getAxis2ConfigurationContext();
            ClusteringAgent clusteringAgent = cfgCtx.getAxisConfiguration().getClusteringAgent();
            if (clusteringAgent == null) {
                throw new SynapseException("Axis2 ClusteringAgent not defined in axis2.xml");
            }

            // Add the Axis2 GroupManagement agents
            if (hostContexts != null) {
                // iterate through each host context
                for (HostContext hostCtxt : hostContexts.values()) {
                    suffixes.put(hostCtxt.getUrlSuffix(), hostCtxt.getHostName());
                    // each host can has multiple Tenant Contexts, iterate through them
                    for (TenantDomainContext tenantCtxt : hostCtxt.getTenantDomainContexts()) {

                        String domain = tenantCtxt.getDomain();
                        String subDomain = tenantCtxt.getSubDomain();

                        if (clusteringAgent.getGroupManagementAgent(domain, subDomain) == null) {
                            clusteringAgent.addGroupManagementAgent(new SubDomainAwareGroupManagementAgent(
                                                                                                           subDomain),
                                                                    domain, subDomain);
                            if (log.isDebugEnabled()) {
                                log.debug("Group management agent added to cluster domain " +
                                          domain);
                            }
                        }
                    }
                }

                tlbMembershipHandler =
                                       new TenantLoadBalanceMembershipHandler(hostContexts,
                                                                              algorithm, cfgCtx,
                                                                              isClusteringEnabled,
                                                                              getName());
            }

            // Initialize the SAL Sessions if already has not been initialized.
            SALSessions salSessions = SALSessions.getInstance();
            if (!salSessions.isInitialized()) {
                salSessions.initialize(isClusteringEnabled, cfgCtx);
            }
            setSessionAffinity(true);
            setDispatcher(new HttpSessionDispatcher());
            initialized = true;
            log.info("===== Tenant Aware Load Balance Endpoint initialized =====");
        }
    }

    public void setConfiguration(String paramEle) {
        this.configuration = paramEle;
        System.out.print(paramEle.toString());
        CarbonUtils.getCarbonHome();
    }

    public void setAlgorithm(String paramEle) {
        this.algorithm = paramEle;
        System.out.print(paramEle.toString());
    }

    public void setFailOver(String paramEle) {
        this.failOver = paramEle;
        System.out.print(paramEle.toString());
    }

    //TODO remove following hard coded element
    private String generatePayLoad() {
        return " <serviceDynamicLoadbalance failover=\"true\"\n" +
                "                                           algorithm=\"org.apache.synapse.endpoints.algorithms.RoundRobin\"" +
                //"                                           configuration=\"$system:loadbalancer.xml\"" +
                "/>";
    }

    public LoadBalanceMembershipHandler getLbMembershipHandler() {
        return tlbMembershipHandler;
    }


    public void send(MessageContext synCtx) {
        /*   setCookieHeader(synCtx);     */
        Member currentMember = null;
        SessionInformation sessionInformation = null;

        // if (isSessionAffinityBasedLB()) {
        if (isSessionAffinityBasedLB()) {
            // first check if this session is associated with a session. if so, get the endpoint
            // associated for that session.
            sessionInformation =
                    (SessionInformation) synCtx.getProperty(
                            SynapseConstants.PROP_SAL_CURRENT_SESSION_INFORMATION);

            currentMember = (Member) synCtx.getProperty(
                    SynapseConstants.PROP_SAL_ENDPOINT_CURRENT_MEMBER);

            if (sessionInformation == null && currentMember == null) {
                sessionInformation = dispatcher.getSession(synCtx);
                if (sessionInformation != null) {

                    if (log.isDebugEnabled()) {
                        log.debug("Current session id : " + sessionInformation.getId());
                    }

                    currentMember = sessionInformation.getMember();
                    synCtx.setProperty(
                            SynapseConstants.PROP_SAL_ENDPOINT_CURRENT_MEMBER, currentMember);
                    // This is for reliably recovery any session information if while response is getting ,
                    // session information has been removed by cleaner.
                    // This will not be a cost as  session information a not heavy data structure
                    synCtx.setProperty(
                            SynapseConstants.PROP_SAL_CURRENT_SESSION_INFORMATION, sessionInformation);
                }
            }

        }

        // Dispatch request the relevant member
        String targetHost = getTargetHost(synCtx);
        ConfigurationContext configCtx =
                ((Axis2MessageContext) synCtx).getAxis2MessageContext().getConfigurationContext();

        if (tlbMembershipHandler.getConfigurationContext() == null) {
            tlbMembershipHandler.setConfigurationContext(configCtx);
        }

        TenantDynamicLoadBalanceFaultHandlerImpl faultHandler = new TenantDynamicLoadBalanceFaultHandlerImpl();
        faultHandler.setHost(targetHost);
        if (sessionInformation != null && currentMember != null) {
            //send message on current session
            sessionInformation.updateExpiryTime();
            sendToApplicationMember(synCtx, currentMember, faultHandler, false);
        } else {
            Boolean isURLMapping = false;
            String host = null;
            int tenantId;
            for(Map.Entry<String, String> entry : suffixes.entrySet()) {
                if(targetHost.contains("." + entry.getKey())) {
                    isURLMapping = true;
                    host = entry.getValue();
                    break;
                }
            }
            // prepare for a new session
            if(isURLMapping) {
                //if the hostName is like "appid.as.wso2.com" where as.wso2.com is URL suffix, and the URL is only
                //"https://appid.as.wso2.com/", then the tenant will be identified from governance registry by the URL.
                String serviceContext = mappingCache.getMapping(targetHost);
                if(serviceContext == null) {
                    RegistryManager registryManager = new RegistryManager();
                    try {
                        serviceContext = registryManager.getApplicationURLFromRegistry(targetHost);
                        mappingCache.addValidMapping(targetHost, serviceContext);
                    } catch (Exception e) {
                        log.error("error while getting tenant id for url mapping from registry", e);
                    }
                }
                if(serviceContext != null) {
                    tenantId = getTenantId(serviceContext);
                    currentMember = tlbMembershipHandler.getNextApplicationMember(host,tenantId);
                    sendToApplicationMember(synCtx,currentMember,faultHandler,true);
                }
            } else {
                tenantId = getTenantId(synCtx.getTo().toString());
                currentMember = tlbMembershipHandler.getNextApplicationMember(targetHost, tenantId);
                if (currentMember == null) {
                    String msg = "No application members available";
                    log.error(msg);
                    throw new SynapseException(msg);
                }
                sendToApplicationMember(synCtx, currentMember, faultHandler, true);
            }
        }
    }


//    public List<HostContext> getHostContexts() {
//        return Collections.unmodifiableList(hostContexts);
//    }

    /**
     * This FaultHandler will try to resend the message to another member if an error occurs
     * while sending to some member. This is a failover mechanism
     */

    /**
     * @param url to url for target
     * @return tenantID if tenant id available else 0
     */
    private int getTenantId(String url) {
        String servicesPrefix = "/t/";
        if (url != null && url.contains(servicesPrefix)) {
            int domainNameStartIndex =
                    url.indexOf(servicesPrefix) + servicesPrefix.length();
            int domainNameEndIndex = url.indexOf('/', domainNameStartIndex);
            String domainName = url.substring(domainNameStartIndex,
                    domainNameEndIndex == -1 ? url.length() : domainNameEndIndex);
            // return tenant id if domain name is not null
            if (domainName != null) {
                try {
                    return ConfigHolder.getInstance().getRealmService().getTenantManager().getTenantId(domainName);
                } catch (org.wso2.carbon.user.api.UserStoreException e) {
                    log.error("An error occurred while obtaining the tenant id.", e);
                }
            }
        }
        // return 0 if the domain name is null
        return 0;
    }

    /**
     * @param synCtx synapse context from incoming message to be sent
     * @return target host address of mesaage
     */
    private String getTargetHost(MessageContext synCtx) {
        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) synCtx).getAxis2MessageContext();
        Map<String, String> headers =
                (Map<String, String>) axis2MessageContext.
                        getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        String address = headers.get(HTTP.TARGET_HOST);
        synCtx.setProperty("LB_REQUEST_HOST", address); // Need to set with the port
        if (address.contains(":")) {
            address = address.substring(0, address.indexOf(":"));
        }
        return address;
    }

    /**
     * This FaultHandler will try to resend the message to another member if an error occurs
     * while sending to some member. This is a failover mechanism
     */
    private class TenantDynamicLoadBalanceFaultHandlerImpl extends DynamicLoadbalanceFaultHandler {

        private EndpointReference to;
        private Member currentMember;
        private Endpoint currentEp;
        private String host;

        private static final int MAX_RETRY_COUNT = 5;

        // ThreadLocal variable to keep track of how many times this fault handler has been
        // called
        private ThreadLocal<Integer> callCount = new ThreadLocal<Integer>() {
            protected Integer initialValue() {
                return 0;
            }
        };

        public void setHost(String host) {
            this.host = host;
        }

        public void setCurrentMember(Member currentMember) {
            this.currentMember = currentMember;
        }

        public void setTo(EndpointReference to) {
            this.to = to;
        }

        private TenantDynamicLoadBalanceFaultHandlerImpl() {
        }

        public void onFault(MessageContext synCtx) {
            if (currentMember == null) {
                return;
            }
            currentMember.suspend(10000);     // TODO: Make this configurable.
            log.info("Suspended member " + currentMember + " for 10s");

            // Prevent infinite retrying to failed members
            callCount.set(callCount.get() + 1);
            if (callCount.get() >= MAX_RETRY_COUNT) {
                return;
            }

            //cleanup endpoint if exists
            if (currentEp != null) {
                currentEp.destroy();
            }
            Integer errorCode = (Integer) synCtx.getProperty(SynapseConstants.ERROR_CODE);
            if (errorCode != null) {
                if (errorCode.equals(NhttpConstants.CONNECTION_FAILED) ||
                        errorCode.equals(NhttpConstants.CONNECT_CANCEL) ||
                        errorCode.equals(NhttpConstants.CONNECT_TIMEOUT)) {
                    // Try to resend to another member
                    Member newMember = tlbMembershipHandler.getNextApplicationMember(host, getTenantId(synCtx.toString()));
                    if (newMember == null) {
                        String msg = "No application members available";
                        log.error(msg);
                        throw new SynapseException(msg);
                    }
                    log.info("Failed over to " + newMember);
                    synCtx.setTo(to);
                    if (isSessionAffinityBasedLB()) {
                        //We are sending the this message on a new session,
                        // hence we need to remove previous session information
                        Set pros = synCtx.getPropertyKeySet();
                        if (pros != null) {
                            pros.remove(SynapseConstants.PROP_SAL_CURRENT_SESSION_INFORMATION);
                        }
                    }
                    try {
                        Thread.sleep(1000);  // Sleep for sometime before retrying
                    } catch (InterruptedException ignored) {
                    }
                    sendToApplicationMember(synCtx, newMember, this, true);
                } else if (errorCode.equals(NhttpConstants.SND_IO_ERROR_SENDING) ||
                        errorCode.equals(NhttpConstants.CONNECTION_CLOSED)) {
                    // TODO: Envelope is consumed
                }
            }
            // We cannot failover since we are using binary relay
        }

        public void setCurrentEp(Endpoint currentEp) {
            this.currentEp = currentEp;
        }
    }
}

