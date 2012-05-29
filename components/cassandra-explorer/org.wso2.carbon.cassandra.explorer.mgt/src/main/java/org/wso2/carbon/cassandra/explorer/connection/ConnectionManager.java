package org.wso2.carbon.cassandra.explorer.connection;

import me.prettyprint.cassandra.service.CassandraHostConfigurator;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.ddl.KeyspaceDefinition;
import me.prettyprint.hector.api.factory.HFactory;
import org.wso2.carbon.cassandra.explorer.exception.CassandraExplorerException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: shelan
 * Date: 5/28/12
 * Time: 2:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class ConnectionManager {

    private static Cluster cluster = null;

    private void setCassandraCluster(String clusterName, CassandraHostConfigurator hostConfigurator,
                                       Map<String, String> credentials) {
        this.cluster = HFactory.getOrCreateCluster(clusterName, hostConfigurator, credentials);
    }

    public List<Keyspace> getCassandraKeySpacesList(Cluster cluster) {
        List<KeyspaceDefinition> KeyspaceDefsList = cluster.describeKeyspaces();
        List<Keyspace> keyspaceList = new ArrayList<Keyspace>();
        for (KeyspaceDefinition keyspaceDefinition : KeyspaceDefsList) {
            keyspaceList.add(HFactory.createKeyspace(keyspaceDefinition.getName(), cluster));
        }
        return keyspaceList;
    }

    public static Keyspace getKeyspace(Cluster cluster, String keyspaceName) {
        return HFactory.createKeyspace(keyspaceName, cluster);
    }

    public static Cluster getCluster() throws CassandraExplorerException {
      if(cluster!= null){
          return cluster;
      } else{
          throw new CassandraExplorerException("Cannot find a cluster, Please connect");
      }
    }


}
