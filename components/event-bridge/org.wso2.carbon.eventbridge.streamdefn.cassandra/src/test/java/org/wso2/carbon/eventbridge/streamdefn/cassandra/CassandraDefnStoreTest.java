package org.wso2.carbon.eventbridge.streamdefn.cassandra;

import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.factory.HFactory;
import org.cassandraunit.AbstractCassandraUnit4TestCase;
import org.cassandraunit.dataset.DataSet;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.junit.Test;
import org.wso2.carbon.eventbridge.core.beans.Event;
import org.wso2.carbon.eventbridge.core.beans.EventStreamDefinition;
import org.wso2.carbon.eventbridge.core.exceptions.MalformedStreamDefinitionException;
import org.wso2.carbon.eventbridge.core.exceptions.StreamDefinitionException;
import org.wso2.carbon.eventbridge.core.utils.EventBridgeUtils;
import org.wso2.carbon.eventbridge.core.utils.EventConverterUtils;
import org.wso2.carbon.eventbridge.core.utils.StreamDefnConverterUtils;
import org.wso2.carbon.eventbridge.streamdefn.cassandra.datastore.CassandraConnector;
import org.wso2.carbon.eventbridge.streamdefn.cassandra.datastore.ClusterFactory;
import org.wso2.carbon.eventbridge.streamdefn.cassandra.internal.util.Utils;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
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
public class CassandraDefnStoreTest extends AbstractCassandraUnit4TestCase {



    private String properEvent = "[\n" +
            "     {\n" +
            "      \"payloadData\" : [\"IBM\", 26.0, 848, 43.33, 2.3] ,\n" +
            "      \"metaData\" : [\"123.233.0.1\"] ,\n" +
            "      \"timeStamp\" : 1312345432\n" +
            "     }\n" +
            "    ,\n" +
            "     {\n" +
            "      \"streamId\" : \"bar::2.1.0\", \n" +
            "      \"payloadData\" : [\"MSFT\", 22.0, 233, 22.22, 4.3] ,\n" +
            "      \"metaData\" : [\"5.211.1.1\"] ,\n" +
            "     }\n" +
            "\n" +
            "   ]";

    private String properandImproperEvent = "[\n" +
            "     {\n" +
            "      \"payloadData\" : [\"IBM\", 26.0, 848, 43.33, 2.3] ,\n" +
            "      \"metaData\" : [\"123.233.0.1\"] ,\n" +
            "      \"timeStamp\" : 1312345432\n" +
            "     }\n" +
            "    ,\n" +
            "     {\n" +
            "      \"streamId\" : \"bar::2.1.0\", \n" +
            "      \"payloadData\" : [\"MSFT\", 233, 22.22, 4.3] ,\n" +
            "      \"metaData\" : [\"5.211.1.1\"] ,\n" +
            "     }\n" +
            "\n" +
            "   ]";



    private String definition = "{" +
                            "  'name':'org.wso2.esb.MediatorStatistics'," +
                            "  'version':'2.3.0'," +
                            "  'nickName': 'Stock Quote Information'," +
                            "  'description': 'Some Desc'," +
                            "  'tags':['foo', 'bar']," +
                            "  'metaData':[" +
                            "          {'name':'ipAdd','type':'STRING'}" +
                            "  ]," +
                            "  'payloadData':[" +
                            "          {'name':'symbol','type':'string'}," +
                            "          {'name':'price','type':'double'}," +
                            "          {'name':'volume','type':'int'}," +
                            "          {'name':'max','type':'double'}," +
                            "          {'name':'min','type':'double'}" +
                            "  ]" +
                            "}";
    private CassandraConnector cassandraConnector;

    private EventStreamDefinition streamDefinition;

    private Cluster cluster;

    @Override
    public void before() throws Exception {
        EmbeddedCassandraServerHelper.startEmbeddedCassandra("cassandra.yaml");
        cluster = HFactory.getOrCreateCluster("TestCluster", "localhost:9171");
        Utils.setCassandraConnector(new CassandraConnector());
        ClusterFactory.initCassandraKeySpaces(getCluster());
        cassandraConnector = Utils.getCassandraConnector();
        try {
            streamDefinition = StreamDefnConverterUtils.convertFromJson(definition);
        } catch (MalformedStreamDefinitionException e) {
            fail();
        }
    }

    @Override
    public Cluster getCluster() {
        return cluster;
    }

    @Override
    public DataSet getDataSet() {
        return null;
    }



    @Test
    public void checkHappyPathStreamStoreOperations() {

        String streamIdKey = EventBridgeUtils
                .constructStreamKey(streamDefinition.getName(), streamDefinition.getVersion());
        cassandraConnector.saveStreamIdToStore(getCluster(), streamIdKey
                , streamDefinition.getStreamId());
        cassandraConnector.saveStreamDefinitionToStore(getCluster(), streamDefinition.getStreamId(), streamDefinition);

        String streamIdFromStore = cassandraConnector.getStreamIdFromStore(getCluster(), streamIdKey);
        assertEquals(streamDefinition.getStreamId(), streamIdFromStore);

        String retrievedStreamId = cassandraConnector.getStreamKeyFromStreamId(getCluster(), streamIdFromStore);
        assertEquals(retrievedStreamId, streamIdKey);

        EventStreamDefinition streamDefinitionFromStore = null;
        try {
            streamDefinitionFromStore = cassandraConnector.getStreamDefinitionFromStore(getCluster(), retrievedStreamId);
        } catch (StreamDefinitionException e) {
            fail();
        }
        assertEquals(streamDefinition, streamDefinitionFromStore);
        List<Event> eventList = EventConverterUtils.convertFromJson(properEvent, streamIdFromStore);
        try {
            for (Event event : eventList) {
                cassandraConnector.insertEvent(cluster, event );
            }
        } catch (MalformedStreamDefinitionException e) {
            e.printStackTrace();
            fail();
        } catch (StreamDefinitionException e) {
            e.printStackTrace();
            fail();
        }

    }

    @Test
    public void insertWronglyFormedEvents() {

    }

    private void createSampleEventList(String streamId)  {
        List<Event> eventList = new ArrayList<Event>();
//        Event event1

    }

    @Test
    public void getExistingStreamDefinition() {


    }

    public void g() {

    }
}
