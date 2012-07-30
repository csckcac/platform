package org.wso2.carbon.lb.endpoint;


import org.apache.axis2.clustering.ClusteringAgent;
import org.apache.axis2.clustering.Member;
import org.apache.axis2.clustering.management.GroupManagementAgent;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseException;
import org.apache.synapse.core.LoadBalanceMembershipHandler;
import org.apache.synapse.endpoints.algorithms.AlgorithmContext;
import org.apache.synapse.endpoints.algorithms.LoadbalanceAlgorithm;
import org.wso2.carbon.lb.common.conf.util.HostContext;
import org.wso2.carbon.lb.common.conf.util.TenantDomainContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Bridge between Axis2 membership notification and Synapse load balancing
 */
public class TenantLoadBalanceMembershipHandler implements LoadBalanceMembershipHandler {
    private static final Log log = LogFactory.getLog(TenantLoadBalanceMembershipHandler.class);

    private ConfigurationContext configCtx;

    private LoadbalanceAlgorithm lbAlgo;
    /**
     * Key - Host, Value - HostContext
     */
    private static Map<String, HostContext> hostContextsMap =
            new HashMap<String, HostContext>();
    private ClusteringAgent clusteringAgent;

    public TenantLoadBalanceMembershipHandler(Map<String, HostContext> hostContexts,
                                              LoadbalanceAlgorithm algorithm,
                                              ConfigurationContext configCtx,
                                              boolean isClusteringEnabled,
                                              String endpointName) {

        lbAlgo = algorithm;
        
        for (HostContext host : hostContexts.values()) {

            String hostName = host.getHostName();

            AlgorithmContext algorithmContext =
                                                new AlgorithmContext(isClusteringEnabled,
                                                                     configCtx, endpointName + "." +
                                                                                hostName);

            host.setAlgorithm(algorithm.clone());
            host.setAlgorithmContext(algorithmContext);

            hostContextsMap.put(hostName, host);

        }
    }

    public void init(Properties props, LoadbalanceAlgorithm algorithm) {
        // Nothing to do
    }

    public void setConfigurationContext(ConfigurationContext configCtx) {
        this.configCtx = configCtx;

        // The following code does the bridging between Axis2 and Synapse load balancing
        clusteringAgent = configCtx.getAxisConfiguration().getClusteringAgent();
        if (clusteringAgent == null) {
            String msg = "In order to enable load balancing across an Axis2 cluster, " +
                         "the cluster entry should be enabled in the axis2.xml file";
            log.error(msg);
            throw new SynapseException(msg);
        }
    }

    public ConfigurationContext getConfigurationContext() {
        return configCtx;
    }

    /**
     * Getting the next member to which the request has to be sent in a round-robin fashion
     *
     * @param context The AlgorithmContext
     * @return The current member
     * @deprecated Use {@link #getNextApplicationMember(AlgorithmContext)}
     */
    public Member getNextApplicationMember(AlgorithmContext context) {
        throw new UnsupportedOperationException("This operation is invalid. " +
                                                "Call getNextApplicationMember(String host)");
    }

    public Member getNextApplicationMember(String host, int tenantId) {
        HostContext hostContext = getHostContext(host);

        // here we have to pass tenant id to get domain from hostContext
        String domain = hostContext.getDomainFromTenantId(tenantId);
        String subDomain = hostContext.getSubDomainFromTenantId(tenantId);

        LoadbalanceAlgorithm algorithm = hostContext.getAlgorithm();
        GroupManagementAgent groupMgtAgent = clusteringAgent.getGroupManagementAgent(domain, subDomain);
        if (groupMgtAgent == null) {
            String msg =
                    "A LoadBalanceEventHandler has not been specified in the axis2.xml " +
                    "file for the domain: " + domain + ", subDomain:" + subDomain +
                    " for host " + host;
            log.error(msg);
            throw new SynapseException(msg);
        }
        algorithm.setApplicationMembers(groupMgtAgent.getMembers());
        //groupMgtAgent.getMembers().get(0).properties.getProperty("tenantRange");
        AlgorithmContext context = hostContext.getAlgorithmContext();
        return algorithm.getNextApplicationMember(context);
    }

    private HostContext getHostContext(String host) {
        HostContext hostContext = hostContextsMap.get(host);
        if (hostContext == null) {
            int indexOfDot;
            if ((indexOfDot = host.indexOf(".")) != -1) {
                hostContext = getHostContext(host.substring(indexOfDot + 1));
            } else {
                throw new SynapseException("Domain not found for host" + host);
            }
        }
        return hostContext;
    }

    public LoadbalanceAlgorithm getLoadbalanceAlgorithm() {
        return lbAlgo;
    }

    public Properties getProperties() {
        return null;
    }

//    /**
//     * This method allows get clustering domain from tenantId and host
//     *
//     * @param targetHost address of the host
//     * @param tenantId   tenant id of given tenant
//     * @return returns clustering domain of given tenant for given host address
//     */
//    public static String getDomainFormHostTenant(String targetHost, int tenantId) {
//        return hostContextsMap.get(targetHost).getDomain(tenantId);
//    }

    /**
     * POJO for maintaining the domain & AlgorithmContext for a particular host
     */
//    private static class DomainAlgorithmContext {
//
//        private AlgorithmContext algorithmContext;
//        private LoadbalanceAlgorithm algorithm;
//        private HostContext hostContext;
//
//        private DomainAlgorithmContext(HostContext hostContext, LoadbalanceAlgorithm algorithm,
//                                       AlgorithmContext algorithmContext) {
//
//            this.hostContext = hostContext;
//            this.algorithm = algorithm;
//            this.algorithmContext = algorithmContext;
//        }
//
//        public LoadbalanceAlgorithm getAlgorithm() {
//            return algorithm;
//        }
//
//        public String getDomain(int tenantId) {
//            return hostContext.getDomainFromTenantId(tenantId);
//        }
//
//        public String getSubDomain(int tenantId) {
//            return hostContext.getSubDomainFromTenantId(tenantId);
//        }
//
//        public AlgorithmContext getAlgorithmContext() {
//            return algorithmContext;
//        }
//    }
}
