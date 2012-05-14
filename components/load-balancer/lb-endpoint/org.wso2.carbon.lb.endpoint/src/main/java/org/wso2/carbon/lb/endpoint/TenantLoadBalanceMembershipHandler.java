package org.wso2.carbon.lb.endpoint;


import org.apache.axis2.clustering.ClusteringAgent;
import org.apache.axis2.clustering.Member;
import org.apache.axis2.clustering.management.GroupManagementAgent;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseException;
import org.apache.synapse.core.LoadBalanceMembershipHandler;
import org.apache.synapse.endpoints.algorithms.AlgorithmContext;
import org.apache.synapse.endpoints.algorithms.LoadbalanceAlgorithm;
import org.wso2.carbon.lb.common.conf.util.TenantDomainRangeContext;

import java.util.HashMap;
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
     * Key - Host, Value - DomainAlgorithmContext
     */
    private static Map<String, DomainAlgorithmContext> hostDomainAlgorithmContextMap =
            new HashMap<String, DomainAlgorithmContext>();
    private ClusteringAgent clusteringAgent;

    public TenantLoadBalanceMembershipHandler(Map<String, TenantDomainRangeContext> hostDomainMap,
                                              LoadbalanceAlgorithm algorithm,
                                              ConfigurationContext configCtx,
                                              boolean isClusteringEnabled,
                                              String endpointName) {

        lbAlgo = algorithm;
        for (Map.Entry<String, TenantDomainRangeContext> entry : hostDomainMap.entrySet()) {
            {
                AlgorithmContext algorithmContext =
                        new AlgorithmContext(isClusteringEnabled, configCtx, endpointName + "." + entry.getKey());
                this.hostDomainAlgorithmContextMap.put(entry.getKey(),
                        new DomainAlgorithmContext(entry.getValue(), algorithm.clone(), algorithmContext));
            }
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
     * @deprecated Use {@link #getNextApplicationMember(String)}
     */
    public Member getNextApplicationMember(AlgorithmContext context) {
        throw new UnsupportedOperationException("This operation is invalid. " +
                "Call getNextApplicationMember(String host)");
    }

    public Member getNextApplicationMember(String host, int tenantId) {
        DomainAlgorithmContext domainAlgorithmContext = getDomainAlgorithmContext(host);
        String lbDomain = domainAlgorithmContext.getDomain(tenantId);
        // here we have to pass tenant id to get domain from domainAlgorithmContext  > getDomain(tenantid)

        LoadbalanceAlgorithm algorithm = domainAlgorithmContext.getAlgorithm();
        GroupManagementAgent groupMgtAgent = clusteringAgent.getGroupManagementAgent(lbDomain);
        if (groupMgtAgent == null) {
            String msg =
                    "A LoadBalanceEventHandler has not been specified in the axis2.xml " +
                            "file for the domain " + lbDomain + " for host " + host;
            log.error(msg);
            throw new SynapseException(msg);
        }
        algorithm.setApplicationMembers(groupMgtAgent.getMembers());
        //groupMgtAgent.getMembers().get(0).properties.getProperty("tenantRange");
        AlgorithmContext context = domainAlgorithmContext.getAlgorithmContext();
        return algorithm.getNextApplicationMember(context);
    }

    private DomainAlgorithmContext getDomainAlgorithmContext(String host) {
        DomainAlgorithmContext domainAlgorithmContext = hostDomainAlgorithmContextMap.get(host);
        if (domainAlgorithmContext == null) {
            int indexOfDot;
            if ((indexOfDot = host.indexOf(".")) != -1) {
                domainAlgorithmContext = getDomainAlgorithmContext(host.substring(indexOfDot + 1));
            } else {
                throw new SynapseException("Domain not found for host" + host);
            }
        }
        return domainAlgorithmContext;
    }

    public LoadbalanceAlgorithm getLoadbalanceAlgorithm() {
        return lbAlgo;
    }

    public Properties getProperties() {
        return null;
    }

    /**
     * This method allows get clustering domain from tenantId and host
     *
     * @param targetHost address of the host
     * @param tenantId   tenant id of given tenant
     * @return returns clustering domain of given tenant for given host address
     */
    public static String getDomainFormHostTenant(String targetHost, int tenantId) {
        return hostDomainAlgorithmContextMap.get(targetHost).getDomain(tenantId);
    }

    /**
     * POJO for maintaining the domain & AlgorithmContext for a particular host
     */
    private static class DomainAlgorithmContext {

        private AlgorithmContext algorithmContext;
        private LoadbalanceAlgorithm algorithm;
        private TenantDomainRangeContext tenantDomainRangeContext;

        private DomainAlgorithmContext(TenantDomainRangeContext tenantDomainRangeContext, LoadbalanceAlgorithm algorithm,
                                       AlgorithmContext algorithmContext) {

            this.tenantDomainRangeContext = tenantDomainRangeContext;
            this.algorithm = algorithm;
            this.algorithmContext = algorithmContext;
        }

        public LoadbalanceAlgorithm getAlgorithm() {
            return algorithm;
        }

        public String getDomain(int tenantId) {
            return tenantDomainRangeContext.getClusterDomainFormTenantId(tenantId);
        }

        public AlgorithmContext getAlgorithmContext() {
            return algorithmContext;
        }
    }
}
