/*
 * Copyright 2012 WSO2, Inc. (http://wso2.com)
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

package org.wso2.carbon.databridge.streamdefn.cassandra.datastore;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import me.prettyprint.cassandra.model.BasicColumnFamilyDefinition;
import me.prettyprint.cassandra.serializers.ByteBufferSerializer;
import me.prettyprint.cassandra.serializers.LongSerializer;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.cassandra.service.ThriftCfDef;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.beans.ColumnSlice;
import me.prettyprint.hector.api.beans.HColumn;
import me.prettyprint.hector.api.beans.OrderedRows;
import me.prettyprint.hector.api.beans.Row;
import me.prettyprint.hector.api.ddl.ColumnFamilyDefinition;
import me.prettyprint.hector.api.ddl.KeyspaceDefinition;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.mutation.Mutator;
import me.prettyprint.hector.api.query.ColumnQuery;
import me.prettyprint.hector.api.query.QueryResult;
import me.prettyprint.hector.api.query.RangeSlicesQuery;
import me.prettyprint.hector.api.query.SliceQuery;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.databridge.commons.utils.EventDefinitionConverterUtils;
import org.wso2.carbon.databridge.core.Utils.DataBridgeUtils;
import org.wso2.carbon.databridge.core.exception.EventProcessingException;
import org.wso2.carbon.databridge.core.exception.StreamDefinitionStoreException;
import org.wso2.carbon.databridge.streamdefn.cassandra.Utils.CassandraSDSUtils;
import org.wso2.carbon.databridge.streamdefn.cassandra.caches.CFCache;
import org.wso2.carbon.databridge.streamdefn.cassandra.exception.NullValueException;
import org.wso2.carbon.databridge.streamdefn.cassandra.inserter.*;
import org.wso2.carbon.databridge.streamdefn.cassandra.internal.util.AppendUtils;
import org.wso2.carbon.databridge.streamdefn.cassandra.internal.util.ServiceHolder;
import org.wso2.carbon.databridge.streamdefn.cassandra.internal.util.Utils;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
//import org.wso2.carbon.agent.server.StreamDefnConverterUtils;

/**
 * Cassandra backend connector  and related operations
 */
public class CassandraConnector {
    //    public static final String LOCAL_NODE = "localhost";
    private static final String STREAM_NAME_KEY = "Name";


    private static final String STREAM_VERSION_KEY = "Version";
    private static final String STREAM_NICK_NAME_KEY = "Nick_Name";
    private static final String STREAM_TIMESTAMP_KEY = "Timestamp";
    private static final String STREAM_DESCRIPTION_KEY = "Description";

    private static final String STREAM_ID_KEY = "StreamId";
    public static final String BAM_META_STREAM_ID_CF = "STREAM_ID";
    public static final String BAM_META_STREAM_DEF_CF = "STREAM_DEFINITION";
    public static final String BAM_META_STREAM_ID_KEY_CF = "STREAM_DEFINITION_ID_TO_KEY";

    //    public static final String BAM_META_STREAMID_TO_STREAM_ID_KEY = "STREAM_ID_TO_STREAM_ID_KEY";
    public static final String BAM_META_KEYSPACE = "META_KS";

    public static final String BAM_EVENT_DATA_KEYSPACE = "EVENT_KS";

    //    public static final String BAM_EVENT_DATA_STREAM_DEFN_CF = "EVENT_STREAM_DEFINITION";
    private static final String STREAM_ID = "STREAM_DEFINITION_ID";


    private volatile AtomicInteger eventCounter = new AtomicInteger();

    private static final String STREAM_DEF = "STREAM_DEFINITION";
    private final static StringSerializer stringSerializer = StringSerializer.get();
    // private final static BytesArraySerializer bytesArraySerializer = BytesArraySerializer.get();
    // private final static UUIDSerializer uuidSerializer = UUIDSerializer.get();
//    private final static IntegerSerializer integerSerializer = IntegerSerializer.get();
    private final static LongSerializer longSerializer = LongSerializer.get();


//    private final static DoubleSerializer doubleSerializer = DoubleSerializer.get();


    private final static ByteBufferSerializer byteBufferSerializer = ByteBufferSerializer.get();


    private AtomicInteger rowkeyCounter = new AtomicInteger();

    static Log log = LogFactory.getLog(CassandraConnector.class);

    private Map<AttributeType, TypeInserter> inserterMap = new ConcurrentHashMap<AttributeType, TypeInserter>();

//    private static final float EVENT_RATE_THRESHOLD = 10; // 10 events a second and we process it batch wise

    //    private Map<Cluster, Mutator> activeMutators = new ConcurrentHashMap<Cluster, Mutator>();
    //    private List<Mutator> activeMutators = Collections.synchronizedList(new ArrayList<Mutator>());
//
//    private volatile CountDownLatch waitSignal = new CountDownLatch(0);
//    private volatile boolean batchMutationStarted = false;
//    private volatile boolean processing = false;
    private int port = 0;
    private String localAddress = null;
    private long startTime;


    public CassandraConnector() {

        try {
            AxisConfiguration axisConfiguration =
                    ServiceHolder.getConfigurationContextService().getServerConfigContext().getAxisConfiguration();

            String portOffset = CarbonUtils.getServerConfiguration().
                    getFirstProperty("Ports.Offset");
            port = CarbonUtils.getTransportPort(axisConfiguration, "https") +
                    Integer.parseInt(portOffset);

            localAddress = Utils.getLocalAddress().getHostAddress();
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.warn("Error when detecting Host/Port, using defaults");
            }
            localAddress = (localAddress == null) ? "127.0.0.1" : localAddress;
            port = (port == 0) ? 9443 : port;
        }

        createInserterMap();
//        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
//
//        scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    eventRate = (eventCounter.get() / EVENT_RATE_CALC_TIMEOUT);
//
//                    eventCounter.set(0);
//
//                    String debugMsg = "";
//                    if (log.isTraceEnabled()) {
//                        debugMsg += "Scheduled high event rate commitor thread is running";
//                        debugMsg += "\n Event Rate is : " + eventRate;
//                        debugMsg += "\n Size of active mutator map is : " + activeMutators.size();
//                        log.trace(debugMsg);
//                        debugMsg = "";
//                    }
//
//
//                    // make mutators wait
//
//                    waitSignal = new CountDownLatch(1);
//
//                    if (log.isDebugEnabled() && nonBatchedCounter.get() > 0) {
//                        debugMsg += "\n Holding all insert threads until active mutators are cleared";
//                        debugMsg += "\n No. of non batched mutators executed after previous execution: " +
//                                nonBatchedCounter.getAndSet(0);
//                        log.debug(debugMsg);
//                    }
//
//                    for (Mutator mutator : activeMutators) {
//
//                        if (log.isDebugEnabled()) {
//                            debugMsg += "\n Executing mutator with no. of pending mutations " +
//                                    mutator.getPendingMutationCount();
//                            log.debug(debugMsg);
//                            debugMsg = "";
//                        }
//                        mutator.execute();
//                        mutator.discardPendingMutations();
//                    }
//
//                    activeMutators.clear();
//                    batchedCounter.set(0);
//
//                    processing = false;
//
//                    if (log.isDebugEnabled()) {
//                        debugMsg += "\n Scheduled high event rate commitor thread has finished \n";
//                        log.trace(debugMsg);
//                    }
//                    waitSignal.countDown();
//
//                } catch (Throwable t) {
//                    log.error("Error occurred during running of scheduled mutator", t);
//                }
//
//            }
//
//        }, EVENT_RATE_CALC_DELAY, EVENT_RATE_CALC_TIMEOUT, TimeUnit.SECONDS);

    }

    private Mutator<String> getMutator(Cluster cluster) throws StreamDefinitionStoreException {
//        try {
//            if (!processing) {
//                if (eventRate > EVENT_RATE_THRESHOLD && batchedCounter.get() < ALLOWED_BATCH_MUTATOR_SIZE) {
//                    batchMutationStarted = true;
//                    processing = true;
//                } else {
//                    batchMutationStarted = false;
//                }
//            }
//            if (batchMutationStarted) {
//                return TenantAwareMutatorCache.getMutator(cluster);
//            } else {
        Keyspace keyspace = HFactory.createKeyspace(BAM_EVENT_DATA_KEYSPACE, cluster);
        return HFactory.createMutator(keyspace, stringSerializer);
//            }
//        } catch (ExecutionException e) {
//            String errorMsg = "Unable to get mutator for cluster " + cluster.getName();
//            log.error(errorMsg, e);
//            throw new StreamDefinitionStoreException(errorMsg, e);
//        }
    }


//    private AtomicInteger batchedCounter = new AtomicInteger(0);


    private void commit(Mutator mutator) throws StreamDefinitionStoreException {

//        if (!batchMutationStarted) {

        mutator.execute();
//            mutator.discardPendingMutations();
//            if (log.isDebugEnabled()) {
//                nonBatchedCounter.incrementAndGet();
//                log.trace("No of non batched mutators executed : " + nonBatchedCounter.get());
//                log.trace("Event rate is : " + eventRate + " is less than High event rate threshold : " +
//                        EVENT_RATE_THRESHOLD +
//                        "\n Therefore, Executing mutator...");
//            }
//        } else {
//            try {
//                if (log.isDebugEnabled()) {
//                    log.trace("High event rate reached Waiting for latch to release");
//                }
//
//                batchedCounter.incrementAndGet();
//
//
//                // wait if mutators have to be committed
//                waitSignal.await();
//
//                if (!activeMutators.contains(mutator)) {
//                    activeMutators.add(mutator);
//                }
//
//                if (log.isDebugEnabled()) {
//                    String debugMsg = "High event rate is reached, adding mutator to active mutator list \n";
//                    debugMsg += "Pending mutations in mutator : " + mutator.getPendingMutationCount() + " \n";
//                    debugMsg += "Active mutator map : \n " + activeMutators;
//                    debugMsg += "Active mutator map size : \n " + activeMutators.size();
//                    log.trace(debugMsg);
//                }
//            } catch (InterruptedException e) {
//                // if interrupted, store the accumulated data in cassandra
//                mutator.execute();
//            }
//        }

    }

    private void createInserterMap() {
        inserterMap.put(AttributeType.INT, new IntInserter());
        inserterMap.put(AttributeType.BOOL, new BoolInserter());
        inserterMap.put(AttributeType.LONG, new LongInserter());
        inserterMap.put(AttributeType.FLOAT, new FloatInserter());
        inserterMap.put(AttributeType.STRING, new StringInserter());
        inserterMap.put(AttributeType.DOUBLE, new DoubleInserter());
    }



    public void createColumnFamily(Cluster cluster, String keyspaceName, String columnFamilyName)
            throws StreamDefinitionStoreException {
        Keyspace keyspace = HFactory.createKeyspace(keyspaceName, cluster);
        KeyspaceDefinition keyspaceDef =
                cluster.describeKeyspace(keyspace.getKeyspaceName());
        List<ColumnFamilyDefinition> cfDef = keyspaceDef.getCfDefs();
        for (ColumnFamilyDefinition cfdef : cfDef) {
            if (cfdef.getName().equals(columnFamilyName)) {
                if (log.isDebugEnabled()) {
                    log.debug("Column Family " + columnFamilyName + " already exists.");
                }
                CFCache.putCF(cluster, keyspaceName, columnFamilyName, true);
                return;
            }
        }
        ColumnFamilyDefinition columnFamilyDefinition = new BasicColumnFamilyDefinition();
        columnFamilyDefinition.setKeyspaceName(keyspaceName);
        columnFamilyDefinition.setName(columnFamilyName);


        cluster.addColumnFamily(new ThriftCfDef(columnFamilyDefinition), true);


        // give some time to propogate changes
        keyspaceDef =
                cluster.describeKeyspace(keyspace.getKeyspaceName());
        int retryCount = 0;
        while (retryCount < 100) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // ignore
            }

            for (ColumnFamilyDefinition cfdef : keyspaceDef.getCfDefs()) {
                if (cfdef.getName().equals(columnFamilyName)) {
                    if (log.isDebugEnabled()) {
                        log.debug("Column Family " + columnFamilyName + " already exists.");
                    }
                    CFCache.putCF(cluster, keyspaceName, columnFamilyName, true);
                    return;
                }
            }
            retryCount++;
        }

        throw new RuntimeException("The column family " + columnFamilyName + " was  not created");
    }


    public boolean createKeySpaceIfNotExisting(Cluster cluster, String keySpaceName) {

        KeyspaceDefinition keySpaceDef = cluster.describeKeyspace(keySpaceName);

        if (keySpaceDef == null) {
            cluster.addKeyspace(HFactory.createKeyspaceDefinition(keySpaceName));

            keySpaceDef = cluster.describeKeyspace(keySpaceName);
            //Sometimes it takes some time to make keySpaceDef!=null
            int retryCount = 0;
            while (keySpaceDef == null && retryCount < 100) {
                try {
                    Thread.sleep(100);
                    keySpaceDef = cluster.describeKeyspace(keySpaceName);
                    if (keySpaceDef != null) {
                        break;
                    }
                } catch (InterruptedException e) {
                    break;
                }
            }
            return true;
        } else {
            return false;
        }


    }


//    }


//    }

    public List<String> insertEventList(Cluster cluster, List<Event> eventList) throws StreamDefinitionStoreException {
        StreamDefinition streamDef;

        Mutator<String> mutator = getMutator(cluster);

        List<String> rowKeyList = new ArrayList<String>();
        startTimeMeasurement(IS_PERFORMANCE_MEASURED);


        for (Event event : eventList) {

            String rowKey;
            streamDef = getStreamDefinitionFromStore(cluster, event.getStreamId());
            String streamColumnFamily = getCFNameFromStreamId(cluster, event.getStreamId());
            if ((streamDef == null) || (streamColumnFamily == null)) {
                String errorMsg = "Event stream definition or column family cannot be null";
                log.error(errorMsg);
                throw new StreamDefinitionStoreException(errorMsg);
            }


            if (log.isTraceEnabled()) {
                KeyspaceDefinition keyspaceDefinition = cluster.describeKeyspace(BAM_EVENT_DATA_KEYSPACE);
                log.trace("Keyspace desc. : " + keyspaceDefinition);

                String CFInfo = "CFs present \n";
                for (ColumnFamilyDefinition columnFamilyDefinition : keyspaceDefinition.getCfDefs()) {
                    CFInfo += "cf name : " + columnFamilyDefinition.getName() + "\n";
                }
                log.trace(CFInfo);
            }


            eventCounter.incrementAndGet();


            // / add  current server time as time stamp if time stamp is not set
            long timestamp;
            if (event.getTimeStamp() != 0L) {
                timestamp = event.getTimeStamp();
            } else {
                timestamp = System.currentTimeMillis();
            }


            rowKey = CassandraSDSUtils.createRowKey(timestamp, localAddress, port, rowkeyCounter.incrementAndGet());

            String streamDefDescription = streamDef.getDescription();
            String streamDefNickName = streamDef.getNickName();

            mutator.addInsertion(rowKey, streamColumnFamily,
                    HFactory.createStringColumn(STREAM_ID_KEY, streamDef.getStreamId()));
            mutator.addInsertion(rowKey, streamColumnFamily,
                    HFactory.createStringColumn(STREAM_NAME_KEY, streamDef.getName()));
            mutator.addInsertion(rowKey, streamColumnFamily,
                    HFactory.createStringColumn(STREAM_VERSION_KEY, streamDef.getVersion()));

            if (streamDefDescription != null) {
                mutator.addInsertion(rowKey, streamColumnFamily,
                        HFactory.createStringColumn(STREAM_DESCRIPTION_KEY, streamDefDescription));
            }
            if (streamDefNickName != null) {
                mutator.addInsertion(rowKey, streamColumnFamily,
                        HFactory.createStringColumn(STREAM_NICK_NAME_KEY, streamDefNickName));
            }

            mutator.addInsertion(rowKey, streamColumnFamily,
                    HFactory.createColumn(STREAM_TIMESTAMP_KEY, timestamp, stringSerializer,
                            longSerializer));


//        int eventDataIndex = 0;
//        if (streamDef.getMetaData() != null) {
//            for (Attribute attribute : streamDef.getMetaData()) {
//                prepareBatchMutate(attribute, eventData.getMetaData(), DataType.meta, eventDataIndex,
//                        rowKey, streamColumnFamily, mutator);
//                eventDataIndex++;
//            }
//        }
//        //Iterate for correlation  data
//        if (eventData.getCorrelationData() != null) {
//            eventDataIndex = 0;
//            for (Attribute attribute : streamDef.getCorrelationData()) {
//                prepareBatchMutate(attribute, eventData.getCorrelationData(), DataType.correlation, eventDataIndex,
//                        rowKey, streamColumnFamily, mutator);
//                eventDataIndex++;
//            }
//        }
//        //Iterate for payload data
//        if (eventData.getPayloadData() != null) {
//            eventDataIndex = 0;
//            for (Attribute attribute : streamDef.getPayloadData()) {
//                prepareBatchMutate(attribute, eventData.getPayloadData(), DataType.payload, eventDataIndex,
//                        rowKey, streamColumnFamily, mutator);
//                eventDataIndex++;
//            }
//        }


            if (streamDef.getMetaData() != null) {
                prepareDataForInsertion(event.getMetaData(), streamDef.getMetaData(), DataType.meta, rowKey,
                        streamColumnFamily, mutator);

            }
            //Iterate for correlation  data
            if (event.getCorrelationData() != null) {
                prepareDataForInsertion(event.getCorrelationData(), streamDef.getCorrelationData(),
                        DataType.correlation,
                        rowKey, streamColumnFamily, mutator);
            }

            //Iterate for payload data
            if (event.getPayloadData() != null) {
                prepareDataForInsertion(event.getPayloadData(), streamDef.getPayloadData(), DataType.payload,
                        rowKey, streamColumnFamily, mutator);
            }

            rowKeyList.add(rowKey);

        }

        commit(mutator);

        endTimeMeasurement(IS_PERFORMANCE_MEASURED);

        return rowKeyList;

    }

    private final boolean IS_PERFORMANCE_MEASURED = true;

    private void endTimeMeasurement(boolean isPerformanceMeasured) {
        if (isPerformanceMeasured) {
            long endTime = System.currentTimeMillis();
            if (eventCounter.get() > 100000) {
                synchronized (this) {
                    if (eventCounter.get() > 100000) {

                        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                        Date date = new Date();
                        System.out.println();
                        String line = "[" + dateFormat.format(date) + "] # of events : " + eventCounter.get() +
                                " start timestamp : " + startTime +
                                " end time stamp : " + endTime + " Throughput is (events / sec) : " +
                                (eventCounter.get() * 1000) / (endTime - startTime) + " \n";
                        File file = new File(CarbonUtils.getCarbonHome() + File.separator + "receiver-perf.txt");

                        try {
                            AppendUtils.appendToFile(IOUtils.toInputStream(line), file);
                        } catch (IOException e) {
                            log.error(e.getMessage(), e);
                        }

                        eventCounter.set(0);
                        startTime = 0;

                    }
                }
            }
        }

    }


    private void startTimeMeasurement(boolean isPerformanceMeasured) {
        if (isPerformanceMeasured) {
            if (startTime == 0) {
                startTime = System.currentTimeMillis();
            }
        }
    }

    public String insertEvent(Cluster cluster, Event eventData)
            throws MalformedStreamDefinitionException, StreamDefinitionStoreException {
        startTimeMeasurement(IS_PERFORMANCE_MEASURED);

        StreamDefinition streamDef;
        streamDef = getStreamDefinitionFromStore(cluster, eventData.getStreamId());
        String streamColumnFamily = getCFNameFromStreamId(cluster, eventData.getStreamId());
        if ((streamDef == null) || (streamColumnFamily == null)) {
            String errorMsg = "Event stream definition or column family cannot be null";
            log.error(errorMsg);
            throw new StreamDefinitionStoreException(errorMsg);
        }


        KeyspaceDefinition keyspaceDefinition = cluster.describeKeyspace(BAM_EVENT_DATA_KEYSPACE);

        if (log.isTraceEnabled()) {
            log.trace("Keyspace desc. : " + keyspaceDefinition);
        }
        if (log.isTraceEnabled()) {
            String CFInfo = "CFs present \n";
            for (ColumnFamilyDefinition columnFamilyDefinition : keyspaceDefinition.getCfDefs()) {
                CFInfo += "cf name : " + columnFamilyDefinition.getName() + "\n";
            }
            log.trace(CFInfo);
        }


        eventCounter.incrementAndGet();


        Mutator<String> mutator = getMutator(cluster);

        // / add  current server time as time stamp if time stamp is not set
        long timestamp;
        if (eventData.getTimeStamp() != 0L) {
            timestamp = eventData.getTimeStamp();
        } else {
            timestamp = System.currentTimeMillis();
        }


        String rowKey = CassandraSDSUtils.createRowKey(timestamp, localAddress, port, rowkeyCounter.incrementAndGet());

        String streamDefDescription = streamDef.getDescription();
        String streamDefNickName = streamDef.getNickName();

        mutator.addInsertion(rowKey, streamColumnFamily,
                HFactory.createStringColumn(STREAM_ID_KEY, streamDef.getStreamId()));
        mutator.addInsertion(rowKey, streamColumnFamily,
                HFactory.createStringColumn(STREAM_NAME_KEY, streamDef.getName()));
        mutator.addInsertion(rowKey, streamColumnFamily,
                HFactory.createStringColumn(STREAM_VERSION_KEY, streamDef.getVersion()));

        if (streamDefDescription != null) {
            mutator.addInsertion(rowKey, streamColumnFamily,
                    HFactory.createStringColumn(STREAM_DESCRIPTION_KEY, streamDefDescription));
        }
        if (streamDefNickName != null) {
            mutator.addInsertion(rowKey, streamColumnFamily,
                    HFactory.createStringColumn(STREAM_NICK_NAME_KEY, streamDefNickName));
        }

        mutator.addInsertion(rowKey, streamColumnFamily,
                HFactory.createColumn(STREAM_TIMESTAMP_KEY, timestamp, stringSerializer,
                        longSerializer));


//        int eventDataIndex = 0;
//        if (streamDef.getMetaData() != null) {
//            for (Attribute attribute : streamDef.getMetaData()) {
//                prepareBatchMutate(attribute, eventData.getMetaData(), DataType.meta, eventDataIndex,
//                        rowKey, streamColumnFamily, mutator);
//                eventDataIndex++;
//            }
//        }
//        //Iterate for correlation  data
//        if (eventData.getCorrelationData() != null) {
//            eventDataIndex = 0;
//            for (Attribute attribute : streamDef.getCorrelationData()) {
//                prepareBatchMutate(attribute, eventData.getCorrelationData(), DataType.correlation, eventDataIndex,
//                        rowKey, streamColumnFamily, mutator);
//                eventDataIndex++;
//            }
//        }
//        //Iterate for payload data
//        if (eventData.getPayloadData() != null) {
//            eventDataIndex = 0;
//            for (Attribute attribute : streamDef.getPayloadData()) {
//                prepareBatchMutate(attribute, eventData.getPayloadData(), DataType.payload, eventDataIndex,
//                        rowKey, streamColumnFamily, mutator);
//                eventDataIndex++;
//            }
//        }


        if (streamDef.getMetaData() != null) {
            prepareDataForInsertion(eventData.getMetaData(), streamDef.getMetaData(), DataType.meta, rowKey,
                    streamColumnFamily, mutator);

        }
        //Iterate for correlation  data
        if (eventData.getCorrelationData() != null) {
            prepareDataForInsertion(eventData.getCorrelationData(), streamDef.getCorrelationData(),
                    DataType.correlation,
                    rowKey, streamColumnFamily, mutator);
        }

        //Iterate for payload data
        if (eventData.getPayloadData() != null) {
            prepareDataForInsertion(eventData.getPayloadData(), streamDef.getPayloadData(), DataType.payload,
                    rowKey, streamColumnFamily, mutator);
        }

        commit(mutator);

        endTimeMeasurement(IS_PERFORMANCE_MEASURED);

        return rowKey;
    }


    private Mutator prepareDataForInsertion(Object[] data, List<Attribute> streamDefnAttrList, DataType dataType,
                                            String rowKey, String streamColumnFamily, Mutator<String> mutator) {
        for (int i = 0; i < streamDefnAttrList.size(); i++) {
            Attribute attribute = streamDefnAttrList.get(i);
            TypeInserter typeInserter = inserterMap.get(attribute.getType());
            String columnName = CassandraSDSUtils.getColumnName(dataType, attribute);

            typeInserter.addDataToBatchInsertion(data[i], streamColumnFamily, columnName, rowKey, mutator);
        }
        return mutator;
    }


    public Event getEvent(Cluster cluster, String streamId, String rowKey) throws EventProcessingException {

        // get Event definition

        StreamDefinition streamDefinition;
        try {
            streamDefinition = getStreamDefinitionFromStore(cluster, streamId);
        } catch (StreamDefinitionStoreException e) {
            String errorMsg = "Error processing stream definition for stream Id : " + streamId;
            log.error(errorMsg, e);
            throw new EventProcessingException(errorMsg, e);
        }
        List<Attribute> payloadDefinitions = streamDefinition.getPayloadData();
        List<Attribute> correlationDefinitions = streamDefinition.getCorrelationData();
        List<Attribute> metaDefinitions = streamDefinition.getMetaData();


        // start conversion

        SliceQuery<String, String, ByteBuffer> sliceQuery =
                HFactory.createSliceQuery(HFactory.createKeyspace(BAM_EVENT_DATA_KEYSPACE, cluster),
                        stringSerializer, stringSerializer, byteBufferSerializer);
        String cfName = getCFNameFromStreamId(cluster, streamId);
        sliceQuery.setKey(rowKey).setRange("", "", true, Integer.MAX_VALUE).setColumnFamily(
                cfName);
        ColumnSlice<String, ByteBuffer> columnSlice = sliceQuery.execute().get();

        Event event = new Event();
        List<Object> metaData = new ArrayList<Object>();
        List<Object> correlationData = new ArrayList<Object>();
        List<Object> payloadData = new ArrayList<Object>();

        try {
            event.setStreamId(CassandraSDSUtils.getString(columnSlice.getColumnByName(STREAM_ID_KEY).getValue()));
            event.setTimeStamp(CassandraSDSUtils.getLong(columnSlice.getColumnByName(STREAM_TIMESTAMP_KEY).getValue()));

            if (payloadDefinitions != null) {
                for (Attribute payloadDefinition : payloadDefinitions) {
                    payloadData.add(getValueForDataTypeList(columnSlice, payloadDefinition, DataType.payload));
                }
            }

            if (metaDefinitions != null) {
                for (Attribute payloadDefinition : metaDefinitions) {
                    metaData.add(getValueForDataTypeList(columnSlice, payloadDefinition, DataType.meta));
                }
            }

            if (correlationDefinitions != null) {
                for (Attribute payloadDefinition : correlationDefinitions) {
                    correlationData.add(correlationData
                            .add(getValueForDataTypeList(columnSlice, payloadDefinition, DataType.correlation)));
                }
            }
        } catch (IOException e) {
            String errorMsg = "Error during event data conversions.";
            log.error(errorMsg, e);
            throw new EventProcessingException(errorMsg, e);
        }

        Object[] metas = metaDefinitions == null ? null : metaData.toArray();
        Object[] correlations = correlationDefinitions == null ? null : correlationData.toArray();
        Object[] payloads = payloadDefinitions == null ? null : payloadData.toArray();
        event.setMetaData(metas);
        event.setCorrelationData(correlations);
        event.setPayloadData(payloads);

        return event;
    }

    private String getCFNameFromStreamId(Cluster cluster, String streamId) {
        return CassandraSDSUtils.convertStreamNameToCFName(DataBridgeUtils
                .getStreamNameFromStreamKey(getStreamKeyFromStreamId(cluster, streamId)));
    }

    private Object getValueForDataTypeList(ColumnSlice<String, ByteBuffer> columnSlice,
                                           Attribute payloadDefinition, DataType dataType) throws IOException {
        HColumn<String, ByteBuffer> eventCol =
                columnSlice.getColumnByName(CassandraSDSUtils.getColumnName(dataType, payloadDefinition));
        return CassandraSDSUtils
                .getOriginalValueFromColumnValue(eventCol.getValue(), payloadDefinition.getType());
    }

    public String getStreamKeyFromStreamId(Cluster cluster, StreamDefinition streamDefinition) {
        return getStreamKeyFromStreamId(cluster, streamDefinition.getStreamId());
    }

    public String getStreamKeyFromStreamId(Cluster cluster, String streamId) {
        try {
            return StreamKeyCache.getStreamKeyFromStreamId(cluster, streamId);
        } catch (ExecutionException e) {
            return null;
        }
    }

    private static class StreamKeyCache {
        private volatile static LoadingCache<StreamIdClusterBean, String> streamKeyCache = null;

        private StreamKeyCache() {
        }

        private static void init() {
            if (streamKeyCache != null) {
                return;
            }
            synchronized (StreamKeyCache.class) {
                if (streamKeyCache != null) {
                    return;
                }
                streamKeyCache = CacheBuilder.newBuilder()
                        .maximumSize(1000)
                        .expireAfterAccess(30, TimeUnit.MINUTES)
                        .build(new CacheLoader<StreamIdClusterBean, String>() {
                            @Override
                            public String load(StreamIdClusterBean streamIdClusterBean) throws Exception {
                                Keyspace keyspace =
                                        HFactory.createKeyspace(BAM_META_KEYSPACE, streamIdClusterBean.getCluster());
                                ColumnQuery<String, String, String> columnQuery =
                                        HFactory.createStringColumnQuery(keyspace)
                                                .setColumnFamily(BAM_META_STREAM_ID_KEY_CF)
                                                .setKey(streamIdClusterBean.getStreamId())
                                                .setName(STREAM_ID_KEY);
                                HColumn<String, String> queryResult = columnQuery.execute().get();
                                if (queryResult != null) {
                                    return queryResult.getValue();
                                }
                                throw new NullValueException("No value found");
                            }
                        }
                        );
            }

        }

        public static String getStreamKeyFromStreamId(Cluster cluster, String streamId) throws ExecutionException {
            init();
            return streamKeyCache.get(new StreamIdClusterBean(cluster, streamId));
        }

        private static class StreamIdClusterBean {
            private Cluster cluster;
            private String streamId;

            private StreamIdClusterBean(Cluster cluster, String streamId) {
                this.cluster = cluster;
                this.streamId = streamId;
            }

            public Cluster getCluster() {
                return cluster;
            }

            public String getStreamId() {
                return streamId;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;

                StreamIdClusterBean that = (StreamIdClusterBean) o;

                return cluster.equals(that.cluster) && streamId.equals(that.streamId);

            }

            @Override
            public int hashCode() {
                int result = cluster.hashCode();
                result = 31 * result + streamId.hashCode();
                return result;
            }

        }
    }


    /**
     * Store event stream definition to Cassandra data store
     *
     * @param cluster          Cluster of the tenant
     * @param streamDefinition Stream Defn
     * @throws StreamDefinitionStoreException thrown if column family is not available
     */
    public void saveStreamDefinitionToStore(Cluster cluster,
                                            StreamDefinition streamDefinition) throws StreamDefinitionStoreException {
        saveStreamDefinitionToStore(cluster, streamDefinition.getStreamId(), streamDefinition);
    }

    /**
     * Store event stream definition to Cassandra data store
     *
     * @param cluster          Cluster of the tenant
     * @param streamId         Stream Id
     * @param streamDefinition Event stream definition
     * @throws StreamDefinitionStoreException thrown if column family is not available
     */
    public void saveStreamDefinitionToStore(Cluster cluster, String streamId,
                                            StreamDefinition streamDefinition) throws StreamDefinitionStoreException {


        String CFName = DataBridgeUtils
                .getStreamNameFromStreamKey(CassandraSDSUtils
                        .convertStreamNameToCFName(DataBridgeUtils.getStreamNameFromStreamKey
                                (getStreamKeyFromStreamId
                                        (cluster, streamId))));


        try {
            if (!CFCache.getCF(cluster, BAM_EVENT_DATA_KEYSPACE, CFName)) {
                createColumnFamily(cluster, BAM_EVENT_DATA_KEYSPACE, CFName);
            }



                Keyspace keyspace = HFactory.createKeyspace(BAM_META_KEYSPACE, cluster);
                Mutator<String> mutator = HFactory.createMutator(keyspace, stringSerializer);
                mutator.addInsertion(streamId, BAM_META_STREAM_DEF_CF,
                        HFactory.createStringColumn(STREAM_DEF, EventDefinitionConverterUtils
                                .convertToJson(streamDefinition)
                        ));

                mutator.execute();

                log.info("Saving Stream Definition : " + streamDefinition);

                if (log.isDebugEnabled()) {
                    String logMsg = "saveStreamDefinition executed. \n";
                    try {
                        StreamDefinition streamDefinitionFromStore = getStreamDefinitionFromStore(cluster, streamId);
                        logMsg += " stream definition saved : " + streamDefinitionFromStore.toString() + " \n";
                    } catch (StreamDefinitionStoreException e) {
                        log.error(e.getErrorMessage(), e);
                    }
                    log.debug(logMsg);
                }

        } catch (ExecutionException e) {
            throw new StreamDefinitionStoreException("Error getting column family : " + CFName, e);
        }




    }

    /**
     * Store stream Id and the stream Id key to Cassandra data store
     *
     * @param cluster          Tenant cluster
     * @param streamDefinition stream defn
     */
    public void saveStreamIdToStore(Cluster cluster, StreamDefinition streamDefinition) {

        saveStreamIdToStore(cluster, DataBridgeUtils.constructStreamKey(streamDefinition.getName(),
                streamDefinition.getVersion()), streamDefinition.getStreamId());
    }

    /**
     * Store stream Id and the stream Id key to Cassandra data store
     *
     * @param cluster     Tenant cluster
     * @param streamIdKey Stream Id Key
     * @param streamId    Stream Id
     */
    public void saveStreamIdToStore(Cluster cluster, String streamIdKey, String streamId) {

        Keyspace keyspace = HFactory.createKeyspace(BAM_META_KEYSPACE, cluster);
        Mutator<String> mutator = HFactory.createMutator(keyspace, stringSerializer);
// domainName));
        mutator.addInsertion(streamId, BAM_META_STREAM_ID_KEY_CF,
                HFactory.createStringColumn(STREAM_ID_KEY, streamIdKey));
        mutator.addInsertion(streamIdKey, BAM_META_STREAM_ID_CF, HFactory.createStringColumn(STREAM_ID, streamId));
        mutator.execute();

        if (log.isDebugEnabled()) {
            String logMsg = "saveStreamID executed. \n";
            String streamIdFromStore = getStreamIdFromStore(cluster, streamIdKey);
            logMsg += " stream id saved : " + streamIdFromStore + " \n";
            log.debug(logMsg);
        }
    }

    /**
     * Returns Stream ID stored under  key domainName-streamIdKey
     *
     * @param cluster          Tenant cluster
     * @param streamDefinition Stream Definition
     * @return Returns stored stream Ids
     */
    public String getStreamIdFromStore(Cluster cluster, StreamDefinition streamDefinition) {
        String streamIdKey =
                DataBridgeUtils.constructStreamKey(streamDefinition.getName(), streamDefinition.getVersion());
        return getStreamIdFromStore(cluster, streamIdKey);
    }

    /**
     * Returns Stream ID stored under  key domainName-streamIdKey
     *
     * @param cluster     Tenant cluster
     * @param streamIdKey Stream Id key streamName::streamVersion
     * @return Returns stored stream Ids
     */
    public String getStreamIdFromStore(Cluster cluster, String streamIdKey) {
        try {
            return StreamIdCache.getStreamIdFromStreamKey(cluster, streamIdKey);
        } catch (ExecutionException e) {
            return null;
        }
    }

    private static class StreamIdCache {

        private volatile static LoadingCache<StreamKeyClusterBean, String> streamIdCache = null;

        private static void init() {
            if (streamIdCache != null) {
                return;
            }
            synchronized (StreamIdCache.class) {
                if (streamIdCache != null) {
                    return;
                }
                streamIdCache = CacheBuilder.newBuilder()
                        .maximumSize(1000)
                        .expireAfterAccess(30, TimeUnit.MINUTES)
                        .build(new CacheLoader<StreamKeyClusterBean, String>() {
                            @Override
                            public String load(StreamKeyClusterBean streamKeyClusterBean) throws Exception {
                                Keyspace keyspace = HFactory.createKeyspace(BAM_META_KEYSPACE,
                                        streamKeyClusterBean.getCluster());
                                ColumnQuery<String, String, String> columnQuery =
                                        HFactory.createStringColumnQuery(keyspace);
                                columnQuery.setColumnFamily(BAM_META_STREAM_ID_CF)
                                        .setKey(streamKeyClusterBean.getStreamKey())
                                        .setName(STREAM_ID);
                                QueryResult<HColumn<String, String>> result = columnQuery.execute();
                                HColumn<String, String> hColumn = result.get();
                                if (hColumn != null) {
                                    return hColumn.getValue();
                                }
                                throw new NullValueException("No value found");
                            }
                        }
                        );
            }

        }

        public static String getStreamIdFromStreamKey(Cluster cluster, String streamKey) throws ExecutionException {
            init();
            return streamIdCache.get(new StreamKeyClusterBean(cluster, streamKey));
        }

        private static class StreamKeyClusterBean {
            private Cluster cluster;
            private String streamKey;

            private StreamKeyClusterBean(Cluster cluster, String streamKey) {
                this.cluster = cluster;
                this.streamKey = streamKey;
            }

            public Cluster getCluster() {
                return cluster;
            }

            public String getStreamKey() {
                return streamKey;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;

                StreamKeyClusterBean that = (StreamKeyClusterBean) o;

                return cluster.equals(that.cluster) && streamKey.equals(that.streamKey);

            }

            @Override
            public int hashCode() {
                int result = cluster.hashCode();
                result = 31 * result + streamKey.hashCode();
                return result;
            }
        }
    }


    /**
     * Retrun Stream Definition   stored in stream definition column family under key domainName-streamIdKey
     *
     * @param cluster  Tenant cluster
     * @param streamId Stream Id
     * @return Returns event stream definition stored in BAM meta data keyspace
     * @throws StreamDefinitionStoreException Thrown if the stream definitions are malformed
     */

    public StreamDefinition getStreamDefinitionFromStore(Cluster cluster, String streamId)
            throws StreamDefinitionStoreException {
        try {
            return StreamDefnCache.getStreamDefinition(cluster, streamId);
        } catch (ExecutionException e) {
            return null;
        }
    }

    private static class StreamDefnCache {

        private volatile static LoadingCache<StreamIdClusterBean, StreamDefinition> streamDefnCache = null;

        private static void init() {
            if (streamDefnCache != null) {
                return;
            }
            synchronized (StreamDefnCache.class) {
                if (streamDefnCache != null) {
                    return;
                }
                streamDefnCache = CacheBuilder.newBuilder()
                        .maximumSize(1000)
                        .expireAfterAccess(30, TimeUnit.MINUTES)
                        .build(new CacheLoader<StreamIdClusterBean, StreamDefinition>() {
                            @Override
                            public StreamDefinition load(StreamIdClusterBean streamIdClusterBean)
                                    throws Exception {
                                Keyspace keyspace =
                                        HFactory.createKeyspace(BAM_META_KEYSPACE, streamIdClusterBean.getCluster());
                                ColumnQuery<String, String, String> columnQuery =
                                        HFactory.createStringColumnQuery(keyspace);
                                columnQuery.setColumnFamily(BAM_META_STREAM_DEF_CF)
                                        .setKey(streamIdClusterBean.getStreamId()).setName(STREAM_DEF);
                                QueryResult<HColumn<String, String>> result = columnQuery.execute();
                                HColumn<String, String> hColumn = result.get();
                                try {
                                    if (hColumn != null) {
                                        return EventDefinitionConverterUtils.convertFromJson(hColumn.getValue());
                                    }
                                } catch (MalformedStreamDefinitionException e) {
                                    throw new StreamDefinitionStoreException(
                                            "Retrieved definition from Cassandra store is malformed. Retrieved "
                                                    +
                                                    "value : " + hColumn.getValue());
                                }
                                throw new NullValueException("No value found");
                            }
                        }
                        );
            }

        }

        public static StreamDefinition getStreamDefinition(Cluster cluster, String streamId)
                throws ExecutionException {
            init();
            return streamDefnCache.get(new StreamIdClusterBean(cluster, streamId));
        }

        private static class StreamIdClusterBean {
            private Cluster cluster;
            private String streamId;

            private StreamIdClusterBean(Cluster cluster, String streamId) {
                this.cluster = cluster;
                this.streamId = streamId;
            }

            public Cluster getCluster() {
                return cluster;
            }

            public String getStreamId() {
                return streamId;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;

                StreamIdClusterBean that = (StreamIdClusterBean) o;

                return cluster.equals(that.cluster) && streamId.equals(that.streamId);

            }

            @Override
            public int hashCode() {
                int result = cluster.hashCode();
                result = 31 * result + streamId.hashCode();
                return result;
            }

        }
    }


    /**
     * Retrun all stream definitions stored under one domain
     *
     * @param cluster Tenant cluster
     * @return All stream definitions related to given tenant domain
     * @throws StreamDefinitionStoreException If the stream definitions are malformed
     */
    public Collection<StreamDefinition> getAllStreamDefinitionFromStore(Cluster cluster)
            throws StreamDefinitionStoreException {

        List<StreamDefinition> streamDefinitions = new ArrayList<StreamDefinition>();

        Keyspace keyspace = HFactory.createKeyspace(BAM_META_KEYSPACE, cluster);

        // get all stream ids
        RangeSlicesQuery<String, String, String> query =
                HFactory.createRangeSlicesQuery(keyspace, stringSerializer, stringSerializer, stringSerializer);
        query.setColumnFamily(BAM_META_STREAM_ID_CF);
        query.setKeys("", "");
        query.setColumnNames(STREAM_ID);
        QueryResult<OrderedRows<String, String, String>> result = query.execute();

        String logMsg = null;
        if (log.isDebugEnabled()) {
            logMsg = "getAllStreamDefinitions called : \n";
        }
        for (Row<String, String, String> row : result.get()) {
            if (row == null) {
                continue;
            }
            String streamId = row.getColumnSlice().getColumnByName(STREAM_ID).getValue();

            StreamDefinition streamDefinitionFromStore = getStreamDefinitionFromStore(cluster, streamId);

            // Stream defn is null if there if there is a valid stream id but no corresponding stream defn

            if (streamDefinitionFromStore != null) {
                streamDefinitions.add(streamDefinitionFromStore);
                if (log.isDebugEnabled()) {
                    logMsg += "Stream definitions with stream id : " + streamDefinitionFromStore.getStreamId() +
                            " found. Stream Definition is : " + streamDefinitionFromStore.toString() + " \n";
                }

            }
        }
        if (log.isDebugEnabled()) {
            log.debug(logMsg);
        }
        return streamDefinitions;
    }


}
