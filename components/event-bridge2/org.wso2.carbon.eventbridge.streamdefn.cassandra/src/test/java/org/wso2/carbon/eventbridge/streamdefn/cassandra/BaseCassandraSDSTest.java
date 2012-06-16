package org.wso2.carbon.eventbridge.streamdefn.cassandra;

import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.factory.HFactory;
import org.cassandraunit.AbstractCassandraUnit4TestCase;
import org.cassandraunit.dataset.DataSet;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.junit.BeforeClass;
import org.wso2.carbon.eventbridge.commons.EventStreamDefinition;
import org.wso2.carbon.eventbridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.eventbridge.commons.utils.EventDefinitionConverterUtils;
import org.wso2.carbon.eventbridge.streamdefn.cassandra.datastore.CassandraConnector;
import org.wso2.carbon.eventbridge.streamdefn.cassandra.datastore.ClusterFactory;
import org.wso2.carbon.eventbridge.streamdefn.cassandra.internal.util.Utils;

import static junit.framework.Assert.fail;

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
public class BaseCassandraSDSTest extends AbstractCassandraUnit4TestCase {

    protected static CassandraConnector cassandraConnector;


    protected static Cluster cluster;
    protected static EventStreamDefinition streamDefinition1;
    protected static EventStreamDefinition streamDefinition2;
    protected static EventStreamDefinition tooLongStreamDefinition;
    protected static EventStreamDefinition streamDefinition3;

    @BeforeClass
    public static void beforeClass() throws Exception {
        EmbeddedCassandraServerHelper.startEmbeddedCassandra("cassandra.yaml");
        cluster = HFactory.getOrCreateCluster("TestCluster", "localhost:9171");
        Utils.setCassandraConnector(new CassandraConnector());
        cassandraConnector = Utils.getCassandraConnector();

        ClusterFactory.initCassandraKeySpaces(cluster);

        try {
            streamDefinition1 = EventDefinitionConverterUtils.convertFromJson(CassandraTestConstants.definition);
            streamDefinition2 = EventDefinitionConverterUtils.convertFromJson(CassandraTestConstants.definition2);
            tooLongStreamDefinition = EventDefinitionConverterUtils.convertFromJson(CassandraTestConstants.tooLongdefinition);
            streamDefinition3 = EventDefinitionConverterUtils.convertFromJson(CassandraTestConstants.definition3);
        } catch (MalformedStreamDefinitionException e) {
            fail();
        }
    }

    @Override
    public void before() throws Exception {


    }

    @Override
    public Cluster getCluster() {
        return cluster;
    }

    @Override
    public DataSet getDataSet() {
        return null;
    }

}
