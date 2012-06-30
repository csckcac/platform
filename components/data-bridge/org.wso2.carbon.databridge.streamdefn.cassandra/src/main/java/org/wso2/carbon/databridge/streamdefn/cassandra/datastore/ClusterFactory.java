package org.wso2.carbon.databridge.streamdefn.cassandra.datastore;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import me.prettyprint.cassandra.service.CassandraHostConfigurator;
import me.prettyprint.hector.api.Cluster;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cassandra.dataaccess.ClusterConfiguration;
import org.wso2.carbon.cassandra.dataaccess.ClusterConfigurationFactory;
import org.wso2.carbon.cassandra.dataaccess.ClusterInformation;
import org.wso2.carbon.cassandra.dataaccess.DataAccessComponentException;
import org.wso2.carbon.databridge.commons.Credentials;
import org.wso2.carbon.databridge.streamdefn.cassandra.internal.util.Utils;
import org.wso2.carbon.utils.ServerConstants;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.util.concurrent.TimeUnit;

/**
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class ClusterFactory {

    private static LoadingCache<Credentials, Cluster> clusterLoadingCache;

    private static Log log = LogFactory.getLog(ClusterFactory.class);

    private static final String USERNAME_KEY = "username";
    private static final String PASSWORD_KEY = "password";
    private static final String DEFAULT_HOST = "localhost:9160";
    private static final String LOCAL_HOST_NAME = "localhost";

    private ClusterFactory() {

    }

    private static void init() {
        synchronized (ClusterFactory.class) {
            if (clusterLoadingCache != null) {
                return;
            }
            clusterLoadingCache = CacheBuilder.newBuilder()
                    .maximumSize(1000)
                    .expireAfterAccess(30, TimeUnit.MINUTES)
                    .build(new CacheLoader<Credentials, Cluster>() {

                        @Override
                        public Cluster load(Credentials credentials) throws Exception {
                            if (log.isTraceEnabled()) {
                                log.trace("Cache not hit. Loading cluster for user : " + credentials.getUsername());
                            }

//                            ClusterConfiguration configuration = ClusterConfigurationFactory.create(loadConfigXML());
//                            CassandraHostConfigurator cassandraHostConfigurator = createCassandraHostConfigurator();
//                            Map<String, String> creds = new HashMap<String, String>();
//                            creds.put(USERNAME_KEY, credentials.getUsername());
//                            creds.put(PASSWORD_KEY, credentials.getPassword());
//                            Cluster cluster =
//                                    HFactory.createCluster(configuration.getClusterName(), cassandraHostConfigurator,
//                                            creds);
                            ClusterInformation clusterInformation = new ClusterInformation(credentials.getUsername(),
                                    credentials.getPassword());
                            Cluster cluster = Utils.getDataAccessService().getCluster(clusterInformation);
                            initCassandraKeySpaces(cluster);
                            return cluster;
                        }
                    });
        }
    }

    public static void initCassandraKeySpaces(Cluster cluster) {
        log.info("Initializing cluster");
        CassandraConnector connector = Utils.getCassandraConnector();
        connector.createKeySpaceIfNotExisting(cluster, CassandraConnector.BAM_META_KEYSPACE);

        connector.createKeySpaceIfNotExisting(cluster, CassandraConnector.BAM_EVENT_DATA_KEYSPACE);


        // Create BAM meta column families if not existing
        connector.createColumnFamily(cluster, CassandraConnector.BAM_META_KEYSPACE,
                CassandraConnector.BAM_META_STREAM_ID_CF);
        connector.createColumnFamily(cluster, CassandraConnector.BAM_META_KEYSPACE,
                CassandraConnector.BAM_META_STREAM_ID_KEY_CF);
        connector.createColumnFamily(cluster, CassandraConnector.BAM_META_KEYSPACE,
                CassandraConnector.BAM_META_STREAMID_TO_STREAM_ID_KEY);
        connector.createColumnFamily(cluster, CassandraConnector.BAM_META_KEYSPACE,
                CassandraConnector.BAM_META_STREAM_DEF_CF);
    }


    private static final String CASSANDRA_COMPONENT_CONF = File.separator + "repository" + File.separator + "conf"
            + File.separator + "etc" + File.separator + "cassandra-component.xml";



    private static CassandraHostConfigurator createCassandraHostConfigurator() {

        ClusterConfiguration configuration = ClusterConfigurationFactory.create(loadConfigXML());
        String carbonCassandraRPCPort;
        carbonCassandraRPCPort = System.getProperty("cassandra.rpc_port");
        String cassandraHosts;
        int cassandraDefaultPort = 0;

        if (carbonCassandraRPCPort != null) {
            cassandraHosts = LOCAL_HOST_NAME + ":" + carbonCassandraRPCPort;
            cassandraDefaultPort = Integer.parseInt(carbonCassandraRPCPort);
        } else {
            cassandraHosts = configuration.getNodesString();
        }
        if (cassandraHosts == null || "".equals(cassandraHosts)) {
            cassandraHosts = DEFAULT_HOST;
        }

        CassandraHostConfigurator configurator = new CassandraHostConfigurator(cassandraHosts);
        configurator.setAutoDiscoverHosts(configuration.isAutoDiscovery());
        configurator.setAutoDiscoveryDelayInSeconds(configuration.getAutoDiscoveryDelay());

        if (cassandraDefaultPort > 0 && cassandraDefaultPort < 65536) {
            configurator.setPort(cassandraDefaultPort);
        } else {
            configurator.setPort(configuration.getDefaultPort());
        }
        return configurator;
    }


    private static OMElement loadConfigXML() {

        String carbonHome = System.getProperty(ServerConstants.CARBON_HOME);
        String path = carbonHome + CASSANDRA_COMPONENT_CONF;
        BufferedInputStream inputStream = null;
        try {
            File file = new File(path);
            if (!file.exists()) {
                log.info("There is no " + CASSANDRA_COMPONENT_CONF + ". Using the default configuration");
                inputStream = new BufferedInputStream(
                        new ByteArrayInputStream("<Cassandra/>".getBytes()));
            } else {
                inputStream = new BufferedInputStream(new FileInputStream(file));
            }
            XMLStreamReader parser = XMLInputFactory.newInstance().
                    createXMLStreamReader(inputStream);
            StAXOMBuilder builder = new StAXOMBuilder(parser);
            return builder.getDocumentElement();
        } catch (FileNotFoundException e) {
            throw new DataAccessComponentException(CASSANDRA_COMPONENT_CONF + "cannot be found in the path : " + path, e, log);
        } catch (XMLStreamException e) {
            throw new DataAccessComponentException("Invalid XML for " + CASSANDRA_COMPONENT_CONF + " located in " +
                    "the path : " + path, e, log);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException ingored) {
                log.error("Cannot close input stream for : " + path);
            }
        }
    }


    public static Cluster getCluster(Credentials credentials) {
        init();
        return clusterLoadingCache.getUnchecked(credentials);
    }
}
