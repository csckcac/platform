/**
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.bam.analyzer.engine;

import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataContext {

    private Map propertiesMap = new HashMap();

    private Map<String, Map> sequenceMap = new HashMap<String, Map>();

    private Cluster bamCluster;

    private List<String> columnFamilyNames;
    private Keyspace bamKeySpace;

    private Map<String, String> credentials;

    private final int tenantId;

    public DataContext(int tenantId) {
        this.tenantId = tenantId;

    }

/*    public boolean getBAMCluster(String username, String password) throws ConfigurationException {
        Cluster cluster = Utils.getDataAccessService().
                getCluster(new ClusterInformation(username, password));
        this.bamCluster = cluster;
        List<CFConfigBean> cfConfigurations = ConfigurationUtils.getCFConfigurations();
        List<String> cfNames = new ArrayList<String>();
        for (CFConfigBean configBean : cfConfigurations) {
            cfNames.add(configBean.getCfName());
        }
        this.columnFamilyNames = cfNames;
        KeyspaceDefinition bamKeyspaceDefinition = bamCluster.describeKeyspace(
                ConfigurationConstants.BAM_KEYSPACE);
        if (bamKeyspaceDefinition == null)  {
            bamCluster.addKeyspace(HFactory.createKeyspaceDefinition(
                    ConfigurationConstants.BAM_KEYSPACE));
            bamKeyspaceDefinition = bamCluster.describeKeyspace(
                    ConfigurationConstants.BAM_KEYSPACE);
        }
        bamKeySpace = (HFactory.createKeyspace(ConfigurationConstants.BAM_KEYSPACE, bamCluster));
        return true;
    }*/

    public Keyspace getBamKeySpace() {
        return bamKeySpace;
    }

    public List<String> getColumnFamilyNames() {
        return columnFamilyNames;
    }

    public Map<String, String> getCredentials() {
        return credentials;
    }

    public void setCredentials(Map<String, String> credentials) {
        this.credentials = credentials;
    }

    public Cluster getBamCluster() {
        return bamCluster;
    }

    public void setProperty(Object key, Object value) {
        propertiesMap.put(key, value);
    }

    public Object getProperty(Object key) {
        return propertiesMap.get(key);
    }

    public Map getSequenceProperties(String sequenceName) {
        return sequenceMap.get(sequenceName);
    }

    public void setSequenceProperties(String sequenceName, Map properties) {
        sequenceMap.put(sequenceName, properties);
    }

    public int getExecutingTenant() {
        return this.tenantId;
    }

}
