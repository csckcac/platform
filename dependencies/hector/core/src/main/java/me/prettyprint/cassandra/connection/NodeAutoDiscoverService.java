package me.prettyprint.cassandra.connection;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import me.prettyprint.cassandra.service.CassandraHost;
import me.prettyprint.cassandra.service.CassandraHostConfigurator;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;

import me.prettyprint.hector.api.ddl.KeyspaceDefinition;
import me.prettyprint.hector.api.exceptions.HectorException;
import org.apache.cassandra.thrift.KsDef;
import org.apache.cassandra.thrift.TokenRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class NodeAutoDiscoverService extends BackgroundCassandraHostService {

    private static final Logger log = LoggerFactory.getLogger(NodeAutoDiscoverService.class);

    public static final int DEF_AUTO_DISCOVERY_DELAY = 30;

    private final Cluster cluster;

    public NodeAutoDiscoverService(HConnectionManager connectionManager,
                                   CassandraHostConfigurator cassandraHostConfigurator, Cluster cluster) {
        super(connectionManager, cassandraHostConfigurator);
        this.cluster = cluster;
        this.retryDelayInSeconds = cassandraHostConfigurator.getAutoDiscoveryDelayInSeconds();
        sf = executor.scheduleWithFixedDelay(new QueryRing(), retryDelayInSeconds, retryDelayInSeconds, TimeUnit.SECONDS);
    }

    @Override
    void shutdown() {
        log.error("Auto Discovery retry shutdown hook called");
        if (sf != null) {
            sf.cancel(true);
        }
        if (executor != null) {
            executor.shutdownNow();
        }
        log.error("AutoDiscovery retry shutdown complete");
    }

    @Override
    public void applyRetryDelay() {
        // no op for now
    }

    class QueryRing implements Runnable {

        @Override
        public void run() {
            if (log.isDebugEnabled()) {
                log.debug("Auto discovery service running...");
            }
            Set<CassandraHost> foundHosts = discoverNodes();
            if (foundHosts != null && foundHosts.size() > 0) {
                log.info("Found {} new host(s) in Ring", foundHosts.size());
                for (CassandraHost cassandraHost : foundHosts) {
                    log.info("Addding found host {} to pool", cassandraHost);
                    cassandraHostConfigurator.applyConfig(cassandraHost);
                    connectionManager.addCassandraHost(cassandraHost);
                }
            }
            if (log.isDebugEnabled()) {
                log.debug("Auto discovery service run complete.");
            }
        }

    }

    public Set<CassandraHost> discoverNodes() {
        Set<CassandraHost> existingHosts = connectionManager.getHosts();
        Set<CassandraHost> foundHosts = new HashSet<CassandraHost>();
        List<KeyspaceDefinition> keyspaces = null;
        if (log.isDebugEnabled()) {
            log.debug("Using existing hosts {}", existingHosts);
        }
        try {
            keyspaces = cluster.describeKeyspaces();
        } catch (HectorException e) {
            log.error("Error getting keyspaces list", e);
        }
        if (keyspaces != null) {
            for (KeyspaceDefinition keyspace : keyspaces) {
                String keyspaceName = keyspace.getName();
                if (keyspaceName == null || "".equals(keyspaceName)) {
                    if (log.isDebugEnabled()) {
                        log.debug("Returned a keyspace with a name as null value or empty");
                    }
                    continue;
                }
                if (Keyspace.KEYSPACE_SYSTEM.equals(keyspaceName)) {
                    continue;
                }
                try {
                    List<TokenRange> tokenRanges = cluster.describeRing(keyspaceName);
                    for (TokenRange tokenRange : tokenRanges) {
                        for (String host : tokenRange.getEndpoints()) {
                            CassandraHost foundHost = new CassandraHost(host, cassandraHostConfigurator.getPort());
                            if (!existingHosts.contains(foundHost)) {
                                if (log.isDebugEnabled()) {
                                    log.debug("Found a node we don't know about {} for TokenRange {}", foundHost, tokenRange);
                                }
                                foundHosts.add(foundHost);
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("Error getting nodes list for keyspace : " + keyspaceName, e);
                }
                break;

            }
        }
        return foundHosts;
    }

}

