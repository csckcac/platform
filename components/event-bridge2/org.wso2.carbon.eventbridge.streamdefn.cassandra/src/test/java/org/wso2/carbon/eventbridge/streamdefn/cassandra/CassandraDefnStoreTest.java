package org.wso2.carbon.eventbridge.streamdefn.cassandra;

import me.prettyprint.hector.api.ddl.ColumnFamilyDefinition;
import me.prettyprint.hector.api.factory.HFactory;
import org.junit.Test;
import org.wso2.carbon.eventbridge.commons.Event;
import org.wso2.carbon.eventbridge.commons.EventStreamDefinition;
import org.wso2.carbon.eventbridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.eventbridge.commons.utils.EventConverterUtils;
import org.wso2.carbon.eventbridge.core.Utils.EventBridgeUtils;
import org.wso2.carbon.eventbridge.core.exception.EventProcessingException;
import org.wso2.carbon.eventbridge.core.exception.StreamDefinitionStoreException;
import org.wso2.carbon.eventbridge.streamdefn.cassandra.Utils.CassandraSDSUtils;
import org.wso2.carbon.eventbridge.streamdefn.cassandra.datastore.CassandraConnector;

import java.util.*;

import static junit.framework.Assert.*;

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
public class CassandraDefnStoreTest extends BaseCassandraSDSTest {


    @Test(expected = Exception.class)
    public void createCFDuringStreamDefn() {
        cassandraConnector.saveStreamIdToStore(getCluster(), streamDefinition1);
        cassandraConnector.saveStreamDefinitionToStore(getCluster(), streamDefinition1);
        ColumnFamilyDefinition columnFamilyDefinition =
                HFactory.createColumnFamilyDefinition(CassandraConnector.BAM_EVENT_DATA_KEYSPACE,
                        CassandraSDSUtils.convertStreamNameToCFName(streamDefinition1.getName()));
        cluster.addColumnFamily(columnFamilyDefinition);
    }

    @Test(expected = Exception.class)
    public void tooLongStreamName() {
        cassandraConnector.saveStreamIdToStore(getCluster(), tooLongStreamDefinition);
        cassandraConnector.saveStreamDefinitionToStore(getCluster(), tooLongStreamDefinition);
    }

    @Test
    public void checkEqualityofEventIdAndStreamDefnId() {

        cassandraConnector.saveStreamIdToStore(getCluster(), streamDefinition1);
        cassandraConnector.saveStreamDefinitionToStore(getCluster(), streamDefinition1);

        String retrievedStreamId = cassandraConnector.getStreamIdFromStore(getCluster(), streamDefinition1);

        List<Event> eventList = EventConverterUtils
                .convertFromJson(CassandraTestConstants.properEvent, retrievedStreamId);

        for (Event event : eventList) {
            assertEquals(event.getStreamId(), streamDefinition1.getStreamId());
        }
    }

    @Test
    public void saveSameStreamMultipleTimes() {
        cassandraConnector.saveStreamIdToStore(getCluster(), streamDefinition1);
        cassandraConnector.saveStreamDefinitionToStore(getCluster(), streamDefinition1);

        String firstTimeStreamId1 = cassandraConnector.getStreamIdFromStore(getCluster(), streamDefinition1);

        cassandraConnector.saveStreamIdToStore(getCluster(), streamDefinition1);
        cassandraConnector.saveStreamDefinitionToStore(getCluster(), streamDefinition1);

        String secondTimeStreamId1 = cassandraConnector.getStreamIdFromStore(getCluster(), streamDefinition1);

        assertEquals(firstTimeStreamId1, secondTimeStreamId1);
        assertEquals(streamDefinition1.getStreamId(), firstTimeStreamId1 );
        assertEquals(streamDefinition1.getStreamId(), secondTimeStreamId1);



    }




    @Test
    public void checkHappyPathStreamStoreOperations() {

        String streamIdKey = EventBridgeUtils
                .constructStreamKey(streamDefinition1.getName(), streamDefinition1.getVersion());
        cassandraConnector.saveStreamIdToStore(getCluster(), streamDefinition1);
        cassandraConnector.saveStreamDefinitionToStore(getCluster(), streamDefinition1);

        String retrievedStreamId = cassandraConnector.getStreamIdFromStore(getCluster(), streamDefinition1);
        assertEquals(streamDefinition1.getStreamId(), retrievedStreamId);

        String retrievedStreamIdKey = cassandraConnector.getStreamKeyFromStreamId(getCluster(), retrievedStreamId);
        assertEquals(retrievedStreamIdKey, streamIdKey);

        EventStreamDefinition streamDefinitionFromStore = null;
        try {
            streamDefinitionFromStore =
                    cassandraConnector.getStreamDefinitionFromStore(getCluster(), retrievedStreamId);
        } catch (Exception e) {
            fail();
        }
        assertEquals(streamDefinition1, streamDefinitionFromStore);
        List<Event> eventList = EventConverterUtils.convertFromJson(CassandraTestConstants.properEvent, retrievedStreamId);
        try {
            for (Event event : eventList) {
                cassandraConnector.insertEvent(cluster, event);
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void insertMixOfWronglyFormedAndCorrectlyFormedEvents() {
        String streamIdKey = EventBridgeUtils
                .constructStreamKey(streamDefinition1.getName(), streamDefinition1.getVersion());
        cassandraConnector.saveStreamIdToStore(getCluster(), streamDefinition1);
        cassandraConnector.saveStreamDefinitionToStore(getCluster(), streamDefinition1);

        String retrievedStreamId = cassandraConnector.getStreamIdFromStore(getCluster(), streamIdKey);
        List<Event> eventList = EventConverterUtils.convertFromJson(CassandraTestConstants.properandImproperEvent, retrievedStreamId);
        List<String> insertedEvents = new ArrayList<String>();
        for (Event event : eventList) {
            try {
                String rowKey = cassandraConnector.insertEvent(cluster, event);
                // inserts row key only if event is valid, i.e. only proper events will add a row key
                insertedEvents.add(rowKey);
            } catch (Exception e) {
                // ignore
            }
        }
        assertEquals(2, insertedEvents.size());

    }

    @Test
    public void insertEventsFromTwoVersions() {
        // save stream defn 1
        String streamIdKey1 = EventBridgeUtils
                .constructStreamKey(streamDefinition1.getName(), streamDefinition1.getVersion());
        cassandraConnector.saveStreamIdToStore(getCluster(), streamDefinition1);
        cassandraConnector.saveStreamDefinitionToStore(getCluster(), streamDefinition1);

        // save stream defn 2
        String streamIdKey2 = EventBridgeUtils
                .constructStreamKey(streamDefinition2.getName(), streamDefinition2.getVersion());
        cassandraConnector.saveStreamIdToStore(getCluster(), streamDefinition2);
        cassandraConnector.saveStreamDefinitionToStore(getCluster(), streamDefinition2);


        List<Event> eventList = new ArrayList<Event>();
        // retrieve stream id 1
        String retrievedStreamId1 = cassandraConnector.getStreamIdFromStore(getCluster(), streamIdKey1);
        eventList.addAll(EventConverterUtils.convertFromJson(CassandraTestConstants.multipleProperEvent1, retrievedStreamId1));

        // retrieve stream id 2
        String retrievedStreamId2 = cassandraConnector.getStreamIdFromStore(getCluster(), streamIdKey2);
        eventList.addAll(EventConverterUtils.convertFromJson(CassandraTestConstants.multipleProperEvent2, retrievedStreamId2));


        Map<String, Event> insertedEvents = new HashMap<String, Event>();
        for (Event event : eventList) {
            try {
                String rowKey = cassandraConnector.insertEvent(cluster, event);
                // inserts row key only if event is valid, i.e. only proper events will add a row key
                insertedEvents.put(rowKey, event);
            } catch (Exception e) {
                // ignore
            }
        }
        assertEquals(4, insertedEvents.size());

        Map<String, Event> retrievedEvents = new HashMap<String, Event>();

        for (Map.Entry<String, Event> eventProps : insertedEvents.entrySet()) {
            try {
                retrievedEvents.put(eventProps.getKey(),
                        cassandraConnector.getEvent(cluster, eventProps.getValue().getStreamId(), eventProps.getKey()));
            } catch (EventProcessingException e) {
                e.printStackTrace();
                fail();
            }
        }


        for (Map.Entry<String, Event> rowKeyAndEvent : retrievedEvents.entrySet()) {
            Event retrievedEvent = rowKeyAndEvent.getValue();
            Event originialEvent = insertedEvents.get(rowKeyAndEvent.getKey());
            System.out.println("Retrieved Event : " + retrievedEvent + "\n Original Event : " + originialEvent + "\n\n");
            if (streamDefinition1.getStreamId().equals(originialEvent.getStreamId())) {
                assertTrue(EventBridgeUtils.equals(originialEvent, retrievedEvent, streamDefinition1));
            } else if (streamDefinition2.getStreamId().equals(originialEvent.getStreamId())) {
                assertTrue(EventBridgeUtils.equals(originialEvent, retrievedEvent, streamDefinition2));


            }
        }
    }


    @Test
    public void insertEventsFromMultipleStreams() {
        // save stream defn 1
        String streamIdKey1 = EventBridgeUtils
                .constructStreamKey(streamDefinition1.getName(), streamDefinition1.getVersion());
        cassandraConnector.saveStreamIdToStore(getCluster(), streamDefinition1);
        cassandraConnector.saveStreamDefinitionToStore(getCluster(), streamDefinition1);

        // save stream defn 3
        String streamIdKey2 = EventBridgeUtils
                .constructStreamKey(streamDefinition3.getName(), streamDefinition3.getVersion());
        cassandraConnector.saveStreamIdToStore(getCluster(), streamDefinition3);
        cassandraConnector.saveStreamDefinitionToStore(getCluster(), streamDefinition3);


        List<Event> eventList = new ArrayList<Event>();
        // retrieve stream id 1
        String retrievedStreamId1 = cassandraConnector.getStreamIdFromStore(getCluster(), streamIdKey1);
        eventList.addAll(EventConverterUtils.convertFromJson(CassandraTestConstants.multipleProperEvent1, retrievedStreamId1));

        // retrieve stream id 3
        String retrievedStreamId2 = cassandraConnector.getStreamIdFromStore(getCluster(), streamIdKey2);
        eventList.addAll(EventConverterUtils.convertFromJson(CassandraTestConstants.multipleProperEvent3, retrievedStreamId2));


        Map<String, Event> insertedEvents = new HashMap<String, Event>();
        for (Event event : eventList) {
            try {
                String rowKey = cassandraConnector.insertEvent(cluster, event);
                // inserts row key only if event is valid, i.e. only proper events will add a row key
                insertedEvents.put(rowKey, event);
            } catch (Exception e) {
                // ignore
            }
        }
        assertEquals(4, insertedEvents.size());

        Map<String, Event> retrievedEvents = new HashMap<String, Event>();

        for (Map.Entry<String, Event> eventProps : insertedEvents.entrySet()) {
            try {
                retrievedEvents.put(eventProps.getKey(),
                        cassandraConnector.getEvent(cluster, eventProps.getValue().getStreamId(), eventProps.getKey()));
            } catch (EventProcessingException e) {
                e.printStackTrace();
                fail();
            }
        }


        for (Map.Entry<String, Event> rowKeyAndEvent : retrievedEvents.entrySet()) {
            Event retrievedEvent = rowKeyAndEvent.getValue();
            Event originialEvent = insertedEvents.get(rowKeyAndEvent.getKey());
            System.out.println("Retrieved Event : " + retrievedEvent + "\n Original Event : " + originialEvent + "\n\n");
            if (streamDefinition1.getStreamId().equals(originialEvent.getStreamId())) {
                assertTrue(EventBridgeUtils.equals(originialEvent, retrievedEvent, streamDefinition1));
            } else if (streamDefinition2.getStreamId().equals(originialEvent.getStreamId())) {
                assertTrue(EventBridgeUtils.equals(originialEvent, retrievedEvent, streamDefinition3));


            }
        }

    }

    @Test
    public void getAllStreamDefns() throws StreamDefinitionStoreException {
        Collection<EventStreamDefinition> allStreamDefinitionFromStore =
                cassandraConnector.getAllStreamDefinitionFromStore(getCluster());
        assertNotNull(allStreamDefinitionFromStore);
    }

    @Test
    public void nullcheck() throws MalformedStreamDefinitionException {
        String nullKey = "abc";
        EventStreamDefinition nullEventStreamDefinition = new EventStreamDefinition("abc", "1.0.0");
        String streamIdFromStore = cassandraConnector.getStreamIdFromStore(getCluster(), nullKey);
        assertNull(streamIdFromStore);
        String streamIdFromStore1 = cassandraConnector.getStreamIdFromStore(getCluster(), nullEventStreamDefinition);
        assertNull(streamIdFromStore1);

        String streamKeyFromStreamId = cassandraConnector.getStreamKeyFromStreamId(getCluster(), nullKey);
        assertNull(streamKeyFromStreamId);

        String streamKeyFromStreamId1 =
                cassandraConnector.getStreamKeyFromStreamId(getCluster(), nullEventStreamDefinition);
        assertNull(streamKeyFromStreamId1);


    }

}
