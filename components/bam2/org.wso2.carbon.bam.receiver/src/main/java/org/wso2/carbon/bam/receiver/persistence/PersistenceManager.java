package org.wso2.carbon.bam.receiver.persistence;

import org.apache.commons.collections.map.UnmodifiableMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.receiver.BAMReceiverException;
import org.wso2.carbon.bam.receiver.ReceiverConstants;
import org.wso2.carbon.bam.receiver.authentication.ThriftSession;
import org.wso2.carbon.bam.service.Event;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

/**
 * The Persistence Manager is an abstraction that hides the details
 * of the data stores that are used underneath. It allows, the integration of additional data stores, if needed.
 */
public class PersistenceManager {

    /*private static final Log log = LogFactory.getLog(PersistenceManager.class);

    private static volatile PersistenceManager persistenceManager = null;

    //private static NoSQLDataStore noSQLDataStore;

    private static DataStoreFactory factory;

    private static SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    //private static List<CFConfigBean> columnFamilyConfigs;

    private PersistenceManager() throws ConfigurationException {
        //noSQLDataStore = NoSQLDataStore.getNoSQLDataStore();
        factory = NoSQLDataStoreFactory.getInstance();
        //columnFamilyConfigs = ConfigurationUtils.getCFConfigurations();
    }

    public static PersistenceManager getManager() throws ConfigurationException {
        if (persistenceManager == null) {
            synchronized (PersistenceManager.class) {
                if (persistenceManager == null) {
                    persistenceManager = new PersistenceManager();
                }
            }
        }
        return persistenceManager;
    }

    *//**
     * When given a map of column keys and column values this method will insert into necessary column families and to necessary rows
     *
     * @param cfData - Map where key is the column name and value is the column value
     * @return success of operation
     * @throws BAMReceiverException
     *//*
*//*    public boolean persistEvent(Map<String, String> cfData) throws BAMReceiverException {
        String defaultRowKey = null;

        // Note: Performance gain by starting a pseudo batch commit for Cassandra,
        // One event, will only have one commit, independent of the number of CFs and Rows being written to
        noSQLDataStore.startBatchCommit();

        // Iterate through all the CF configs and decide which one to insert event or index event
        for (CFConfigBean cfConfig : columnFamilyConfigs) {
            String rowKey;

            // All events will be at least committed to the default CF, which usually is the Event CF
            if (cfConfig.isDefaultCF()) {
                defaultRowKey = createRowKey(cfConfig.getRowKeyParts(), cfConfig.getGranularity(),
                                             cfData, true);

                noSQLDataStore.persistData(cfConfig.getCfName(), defaultRowKey, cfData);
                noSQLDataStore.persistIndexes(cfConfig.getCfName(), cfConfig.getRowKeyParts(),
                                              cfData);
            } else {
                rowKey = createRowKey(cfConfig.getRowKeyParts(), cfConfig.getGranularity(), cfData,
                                      false);
                if (rowKey == null) {
                    // this row key is does not apply to this event, skip it
                    continue;
                }

                // We are creating a data map, with the default row key as the column name, i.e. default row key is stored
                // as a pointer
                Map<String, String> nonDefaultDataMap = createNonDefaultDataMap(defaultRowKey);

                noSQLDataStore.persistData(cfConfig.getCfName(), rowKey, nonDefaultDataMap);
                // persist any indexes if they are given
                noSQLDataStore.persistIndexes(cfConfig.getCfName(), cfConfig.getRowKeyParts(),
                                              cfData);

                if (cfConfig.getIndexRowKey() != null) {
                    // Cassandra does not sort in rows, but Cassandra columns are sorted.
                    // So we store a separate column, i.e. defaults to 'allKeys', that stores pointers to all the row keys
                    // in the same CF
                    Map<String, String> indexRowKeyDataMap = createNonDefaultDataMap(rowKey);
                    noSQLDataStore.persistData(cfConfig.getCfName(), cfConfig.getIndexRowKey(),
                                               indexRowKeyDataMap);

                }
            }
        }

        noSQLDataStore.endBatchCommit();

        return true;
    }*//*

    *//**
     * Persist the de-queued event to the data store. Uses a factory to obtain the relevant data store
     * for each user.
     *
     * @param event
     * @return
     * @throws BAMReceiverException
     *//*
    public boolean persistEvent(EventData event) throws BAMReceiverException {

        ThriftSession session = event.getSessionInfo();

        String userName = session.getUserName();
        String password = session.getPassword();

        Map<String, String> credentials = new HashMap<String, String>();
        credentials.put(PersistencyConstants.USER_NAME, userName);
        credentials.put(PersistencyConstants.PASSWORD, password);
        DataStore store = getDataStore(credentials);

        // Store three fold event data (meta, correlation, event) to three initial tables
        String timeStamp = formatter.format(new Date());
        String uuid = UUID.randomUUID().toString();

        String rowKey = timeStamp + "--" + System.nanoTime() + "--" + uuid;

        Event rawEvent = event.getRawEvent();

        // Note: Performance gain by starting a pseudo batch commit for Cassandra,
        // One event, will only have one commit, independent of the number of CFs and Rows being written to
        store.startBatchCommit();

        Map<String, String> rowIndex = createNonDefaultDataMap(rowKey);

        store.persistBinaryData(ReceiverConstants.META_TABLE, rowKey, rawEvent.getMeta());
        store.persistData(ReceiverConstants.META_TABLE, ReceiverConstants.DEFAULT_INDEX_ROW_KEY,
                          rowIndex); // Store row key at Index Row

        store.persistBinaryData(ReceiverConstants.CORRELATION_TABLE, rowKey, rawEvent.getCorrelation());
        store.persistData(ReceiverConstants.CORRELATION_TABLE, ReceiverConstants.DEFAULT_INDEX_ROW_KEY,
                          rowIndex);

        store.persistBinaryData(ReceiverConstants.EVENT_TABLE, rowKey, rawEvent.getEvent());
        store.persistData(ReceiverConstants.EVENT_TABLE, ReceiverConstants.DEFAULT_INDEX_ROW_KEY,
                          rowIndex);

        store.endBatchCommit();

        return true;
    }

    // Use double check locking idiom to prevent unnecessary locking. Usual pitfalls of DCL doesn't
    // apply since the variable is local so no partially constructed object can become visible during
    // the first null check.
    private DataStore getDataStore(Map<String, String> credentials) throws BAMReceiverException {
        DataStore store = null;
        try {
            store = NoSQLDataStoreFactory.getInstance().getDataStore(
                    credentials.get(PersistencyConstants.USER_NAME));
        } catch (DataStoreException e) {
            log.error("Error getting data store..", e);
        }

        if (store == null) {
            synchronized (NoSQLDataStoreFactory.getInstance()) {
                try {
                    store = NoSQLDataStoreFactory.getInstance().getDataStore(
                            credentials.get(PersistencyConstants.USER_NAME));
                } catch (DataStoreException e) {
                    log.error("Error getting data store..", e);
                }
                
                // Initialize the data store and get a connection if it hasn't been already initialized for
                // this user.
                if (store == null) {
                    try {
                        store = factory.initializeDataStore(credentials, false);
                    } catch (InitializationException e) {
                        throw new BAMReceiverException("Unable to store event..", e);
                    }
                }
            }
        }

        return store;

    }

    *//**
     * This will create a map that will be used as a pointer to the entry created in the default CF (i.e. usually the Event CF)
     *
     * @param rowKeyToIndex String that is the row key to index - i.e. usually, the row key in the event CF
     * @return
     *//*
    private Map<String, String> createNonDefaultDataMap(String rowKeyToIndex) {

        Map<String, String> nonDefaultMap = new HashMap<String, String>();
        nonDefaultMap.put(rowKeyToIndex, "");
        return UnmodifiableMap.decorate(nonDefaultMap);

    }

    *//**
     * Creates the row key which is probably the most important entry in an inserted row. This row key allows us to later find this
     * row which will be an event or a pointer to the event
     *
     * @param rowKeyParts
     * @param granularity
     * @param cfData
     * @param appendUUID
     * @return
     * @throws BAMReceiverException
     *//*
    private String createRowKey(List<KeyPart> rowKeyParts, String granularity,
                                Map<String, String> cfData, boolean appendUUID)
            throws BAMReceiverException {

        StringBuffer buffer = new StringBuffer();

        for (int i = 0; i < rowKeyParts.size(); i++) {
            KeyPart rowKeyPart = rowKeyParts.get(i);
            String rowKeyPartName = rowKeyPart.getName();
            if (cfData.containsKey(rowKeyPartName)) {
                String rowKeyPartValue = cfData.get(rowKeyPartName);

                // handle timestamp case according to granularity
                if (rowKeyPartName.equals(ReceiverConstants.TIMESTAMP_KEY_NAME)) {
                    try {
                        // we use the time stamp factory to generate the time stamp according to granularity
                        rowKeyPartValue = TimeStampFactory.getFactory().getTimeStamp(
                                cfData.get(rowKeyPartName), granularity);
                    } catch (ParseException e) {
                        throw new BAMReceiverException("Cannot parse time stamp : " +
                                                       cfData.get(rowKeyPartName));
                    }

                }
                buffer.append(rowKeyPartValue);

                // Skip appending row key delimiter for the last row key part
                if ((i + 1) != rowKeyParts.size()) {
                    buffer.append("---");
                }


            } else {
                // if there is no column name that corresponds to the row key parts, that means this event should not be inserted,
                // we return null, in that case
                return null;
            }
        }

        // Add an optional uuid
        if (appendUUID) {
            buffer.append("---");
            buffer.append(UUID.randomUUID());
        }

        buffer.trimToSize();
        return buffer.toString();
    }*/

}
