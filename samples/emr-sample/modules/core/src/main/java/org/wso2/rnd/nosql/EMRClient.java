/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
/**
 * TODO
 */
package org.wso2.rnd.nosql;

import me.prettyprint.cassandra.model.IndexedSlicesQuery;
import me.prettyprint.cassandra.serializers.BytesArraySerializer;
import me.prettyprint.cassandra.serializers.LongSerializer;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.cassandra.serializers.UUIDSerializer;
import me.prettyprint.cassandra.service.CassandraHostConfigurator;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.beans.ColumnSlice;
import me.prettyprint.hector.api.beans.HColumn;
import me.prettyprint.hector.api.beans.OrderedRows;
import me.prettyprint.hector.api.beans.Row;
import me.prettyprint.hector.api.beans.Rows;
import me.prettyprint.hector.api.exceptions.HectorException;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.mutation.Mutator;
import me.prettyprint.hector.api.query.MultigetSliceQuery;
import me.prettyprint.hector.api.query.QueryResult;
import me.prettyprint.hector.api.query.SliceQuery;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.rnd.nosql.model.Blob;
import org.wso2.rnd.nosql.model.Record;
import org.wso2.rnd.nosql.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * EMR backend operations
 */

public class EMRClient {
    private static final Log log = LogFactory.getLog(EMRClient.class);

    private static EMRClient ourInstance = new EMRClient();
    private GetConfigurations getConfigurations;
    private Cluster cluster;
    private Keyspace emr;
    private final static StringSerializer ss = StringSerializer.get();
    private final static BytesArraySerializer bs = BytesArraySerializer.get();
    private final static UUIDSerializer us = UUIDSerializer.get();
    private final static LongSerializer ls = LongSerializer.get();

    private final static String USER = "User";
    private final static String EMR = "Record";
    private final static String USER_RECORD = "UserRecord";
    private final static String BLOB = "Blob";
    private final static String RECORD_BLOB = "RecordBlob";


    //User column family
    private final static String USER_ID = "UserID";
    private final static String FULL_NAME = "Name";
    private final static String EMAIL = "Email";
    private final static String DOB = "DOB";
    private final static String BLOOD_GROUP = "BloodGroup";
    private final static String GENDER = "Gender";
    private final static String PASSWORD = "Password";

    //Record Column family
    private final static String RECORD_ID = "RecordId";
    private final static String TIME_STAMP = "TimeStamp";
    private final static String RECORD_TYPE = "RecordType";
    private final static String RECORD_TYPE_DATA =  "RecordTypeData";
    private final static String RECORD_DATA = "RecordData";
    private final static String USER_COMMENT = "UserComment";
    private final static String SICKNESS = "Sickness";

    //Blob column family
    private final static String BLOB_ID = "BlobId";
    private final static String FILE_NAME = "FileName";
    private final static String FILE_SIZE = "FileSize";
    private final static String CONTENT_TYPE = "ContentType";
    private final static String FILE_CONTENT = "FileContent";
    private final static String FILE_COMMENT = "Comment";



    public static EMRClient getInstance() {
        return ourInstance;
    }
    
    private EMRClient() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("username", Constants.USER_NAME);
        map.put("password", Constants.PASSWORD);
        try{
        //create cluster and create connection to cluster	
        cluster = HFactory.createCluster(Constants.CLUSTER_NAME, new CassandraHostConfigurator(Constants.CLUSTER_HOST), map);
        //creating keyspace
        emr = HFactory.createKeyspace(Constants.KEYSPACE, cluster);
        }catch (HectorException he){
            System.out.println("Cassandra Connection Error .!" + he.getMessage());
        }
    }
    /**
     * Get current user informations
     * @param userId
     * @return 
     */
    public User getCurrentUserInformation(String userId) {
    	//Creating slice query for getting current user details
        SliceQuery<String, String, String> query = HFactory.createSliceQuery(emr, ss, ss, ss);
        //Setting column family,key and required columns
        query.setColumnFamily(USER).setKey(userId).setColumnNames(FULL_NAME, EMAIL, DOB, BLOOD_GROUP, GENDER);
        List<HColumn<String, String>> columns = query.execute().get().getColumns();

        if (columns.size() == 0) {
            log.info("User does not exist :" + userId);
            return null;
        }

        User user = new User();
        user.setUserID(userId);
        for (HColumn<String, String> column : columns) {
            if (column == null) {
                continue;
            }
            String name = column.getName();
            String value = column.getValue();

            if (FULL_NAME.equals(name)) {
                user.setFullName(value);
            } else if (PASSWORD.equals(name)) {
                user.setPassword(value);
            } else if (EMAIL.equals(name)) {
                user.setEmail(value);
            } else if (DOB.equals(name)) {
                user.setDateOfBirth(value);
            } else if (BLOOD_GROUP.equals(name)) {
                user.setBloodGroup(value);
            } else if (GENDER.equals(name)) {
                user.setGender(value);
            }
        }
        return user;
    }
    
    /**
     * Get current user details
     * @param userId
     * @return
     */
    public List<String> getCurrentUserRecordIds(String userId) {
        List<String> recordId = new ArrayList<String>();
        //Index query for getting userRecordIds from UserRecord table(Kind of a relational table that maps RecordId and UserId)
        IndexedSlicesQuery<String, String, String> query = HFactory.createIndexedSlicesQuery(emr, ss, ss, ss);
        query.addEqualsExpression(USER_ID, userId);
        query.setColumnNames(RECORD_ID, USER_ID);
        query.setColumnFamily(USER_RECORD);
        query.setStartKey("");
        QueryResult<OrderedRows<String, String, String>> result = query.execute();
        for (Row<String, String, String> row : result.get()) {
            if (row == null) {
                continue;
            }
            String keyValue = row.getKey();
            recordId.add(keyValue);
            //System.out.println(keyValue + ": ID\n");
        }
        return recordId;
    }
    
    /**
     * Get current emr records for user
     * @param userId
     * @return
     */
    public QueryResult<Rows<String, String, String>> getCurrentUserEMRs(String userId) {
        List<String> emrKeys = getCurrentUserRecordIds(userId);
        //multiget slice query for getting more one row of data  
        MultigetSliceQuery<String, String, String> multigetSliceQuery =
                HFactory.createMultigetSliceQuery(emr, ss, ss, ss);
        multigetSliceQuery.setColumnFamily(EMR);
        multigetSliceQuery.setKeys(emrKeys);
        multigetSliceQuery.setRange("", "", false, 20);
        QueryResult<Rows<String, String, String>> result = multigetSliceQuery.execute();

        return result;
    }
    
    /**
     * Save user
     * @param user
     */
    public void saveUser(User user) {
        Mutator<String> mutator = HFactory.createMutator(emr, ss);

        mutator.addInsertion(user.getUserID(), USER, HFactory.createStringColumn(FULL_NAME, user.getFullName()))
                .addInsertion(user.getUserID(), USER, HFactory.createStringColumn(EMAIL, user.getEmail()))
                .addInsertion(user.getUserID(), USER, HFactory.createStringColumn(DOB, user.getDateOfBirth()))
                .addInsertion(user.getUserID(), USER, HFactory.createStringColumn(BLOOD_GROUP, user.getBloodGroup()))
                .addInsertion(user.getUserID(), USER, HFactory.createStringColumn(GENDER, user.getGender()));

        mutator.execute();

    }
    
    /**
     * Check whether user is a valid user
     * @param userId
     * @return
     */
    public boolean isValiedUser(String userId){
      SliceQuery<String, String, String> query = HFactory.createSliceQuery(emr, ss, ss, ss);
//        query.setColumnFamily(USER).setKey(userId).setColumnNames(FULL_NAME, EMAIL, DOB, BLOOD_GROUP, GENDER, PASSWORD);
        query.setColumnFamily(USER).setKey(userId);

        ColumnSlice<String, String> columns = query.execute().get();

        if (columns.getColumns().size() == 0) {
            log.info("User does not exist :" + userId);
            return false;
        }


        return  true;
    }
    
    /**
     * Get emr record by recordId
     * @param recordId
     * @return
     */
    public Record getEmrRecordbyId(String recordId) {
        SliceQuery<String, String, String> query = HFactory.createSliceQuery(emr, ss, ss, ss);
        query.setColumnFamily(EMR).setKey(recordId).setColumnNames(TIME_STAMP, RECORD_TYPE, SICKNESS,RECORD_DATA,RECORD_TYPE_DATA,USER_COMMENT);
        List<HColumn<String, String>> columns = query.execute().get().getColumns();
        if (columns.size() == 0) {
            //log.info("EMR is empty:" + recordId);
            return null;
        }

        Record record = new Record();
        record.setRecordID(recordId);

        for (HColumn<String, String> column : columns) {
            if (column == null) {
                continue;
            }
            String name = column.getName();
            String value = column.getValue();
            if (TIME_STAMP.equals(name)) {
                record.setTimeStamp(value);
            } 
            if (RECORD_TYPE.equals(name)) {
                record.setRecordType(value);
            }
            if (SICKNESS.equals(name)) {
                record.setSickness(value);
            }
            if (RECORD_DATA.equals(name))
            {
            	record.setRecordData(value);
            }
            if(RECORD_TYPE_DATA.equals(name))
            {
            	record.setRecordTypeData(value);
            }
            if(USER_COMMENT.equals(name))
            {
            	record.setUserCommnet(value);
            }
            
        }
        return record;
    }
    
    /**
     * Saving emr record
     * @param record
     */
    public void saveEmrRecord(Record record) {
        Mutator<String> mutator = HFactory.createMutator(emr, ss);
        if(record.getRecordID()!=null)
        {
        mutator.addInsertion(record.getRecordID(), EMR, HFactory.createStringColumn(TIME_STAMP, record.getTimeStamp()));               
        }
        if(record.getRecordType()!=null)
        {
        mutator.addInsertion(record.getRecordID(), EMR, HFactory.createStringColumn(RECORD_TYPE, record.getRecordType()));
        }
        if(record.getRecordTypeData()!=null){
        mutator.addInsertion(record.getRecordID(), EMR, HFactory.createStringColumn(RECORD_TYPE_DATA, record.getRecordTypeData()));
        }
        if(record.getRecordData()!=null ){
         mutator.addInsertion(record.getRecordID(),EMR, HFactory.createStringColumn(RECORD_DATA, record.getRecordData()));
        }
        if(record.getUserCommnet() != null){
         mutator.addInsertion(record.getRecordID(),EMR, HFactory.createStringColumn(USER_COMMENT, record.getUserCommnet()));
        }
        if(record.getSickness() != null){
            mutator.addInsertion(record.getRecordID(),EMR, HFactory.createStringColumn(SICKNESS, record.getSickness()));
           }
        try {
            mutator.execute();
        } catch (HectorException he) {
           log.info(he.getMessage());
        }
    }
    
    /**
     * Save record blob
     * @param recordId
     * @param fileName
     * @param fileContent
     */
    //will move to new method
    public void saveRecordBlob(String recordId, String fileName, byte[] fileContent) {
        Mutator<String> mutator = HFactory.createMutator(emr, ss);
        mutator.insert(recordId, EMR, HFactory.createColumn(fileName, fileContent, ss, bs));
        mutator.execute();

    }
    
    /**
     * Get record blob
     * @param recordId
     * @param fileName
     * @return
     */
    //will move to new method
    public byte[] getRecordBlob(String recordId, String fileName) {

        SliceQuery<String, String, byte[]> query = HFactory.createSliceQuery(emr, ss, ss, bs);
        query.setColumnFamily(EMR);
        query.setKey(recordId);

        return query.execute().get().getColumnByName(fileName).getValue();
    }
    
    /**
     * Have to move the functionality to new method
     * @param recordId
     * @return
     */
    public List<byte[]> getBlobList(String recordId) {
        SliceQuery<String, String, byte[]> query = HFactory.createSliceQuery(emr, ss, ss, bs);
        query.setColumnFamily(EMR).setKey(recordId);
        query.setRange("BLOB", "", false, 50);
        List<HColumn<String, byte[]>> columns = query.execute().get().getColumns();
        List<byte[]> blobList = new ArrayList<byte[]>();
        for (HColumn<String, byte[]> column : columns) {
            if (column == null) {
                continue;
            }
            String name = column.getName();
            byte[] value = column.getValue();
            if (name.contains("BLOB")) {
                blobList.add(value);
            }
        }
        return blobList;
    }

    /**
     * Save blob
     * @param blob
     */
     public void saveEmrBlob(Blob blob){
        Mutator<UUID> mutator = HFactory.createMutator(emr, us);
        mutator.insert(blob.getBlobId(), BLOB, HFactory.createColumn(FILE_NAME, blob.getFileName(), ss, ss));
        mutator.insert(blob.getBlobId(), BLOB, HFactory.createColumn(FILE_SIZE, blob.getFileSize(), ss, ls));
        mutator.insert(blob.getBlobId(), BLOB, HFactory.createColumn(CONTENT_TYPE, blob.getContentType(), ss, ss));
        mutator.insert(blob.getBlobId(), BLOB, HFactory.createColumn(TIME_STAMP, blob.getTimeStamp(), ss, ss));
        mutator.insert(blob.getBlobId(), BLOB, HFactory.createColumn(FILE_CONTENT, blob.getFileContent(), ss, bs));
        mutator.insert(blob.getBlobId(), BLOB, HFactory.createColumn(FILE_COMMENT, blob.getComment(), ss, ss));
        mutator.execute();
    }
    /**
     * Check blob availability
     * @param blobId
     * @return
     */
    public boolean isBlobAvailable(UUID blobId){
        SliceQuery<UUID, String, String> query = HFactory.createSliceQuery(emr, us, ss, ss);
        SliceQuery<UUID, String, Long> queryFileSize = HFactory.createSliceQuery(emr, us, ss, ls);
        SliceQuery<UUID, String, byte[]> queryFileContent = HFactory.createSliceQuery(emr, us, ss, bs);
        query.setColumnFamily(BLOB).setKey(blobId).setColumnNames(FILE_NAME, CONTENT_TYPE, TIME_STAMP, FILE_COMMENT);
        queryFileSize.setColumnFamily(BLOB).setKey(blobId).setColumnNames(FILE_SIZE);
        queryFileContent.setColumnFamily(BLOB).setKey(blobId).setColumnNames(FILE_CONTENT);

        List<HColumn<String, String>> columns = query.execute().get().getColumns();
        if (columns.size() == 0) {
            return false;
        }
        return true;
    }
    
    /**
     * Get blob
     * @param blobId
     * @return
     */
    public Blob getEmrBlob(UUID blobId) {
        SliceQuery<UUID, String, String> query = HFactory.createSliceQuery(emr, us, ss, ss);
        SliceQuery<UUID, String, Long> queryFileSize = HFactory.createSliceQuery(emr, us, ss, ls);
        SliceQuery<UUID, String, byte[]> queryFileContent = HFactory.createSliceQuery(emr, us, ss, bs);
        query.setColumnFamily(BLOB).setKey(blobId).setColumnNames(FILE_NAME, CONTENT_TYPE, TIME_STAMP, FILE_COMMENT);
        queryFileSize.setColumnFamily(BLOB).setKey(blobId).setColumnNames(FILE_SIZE);
        queryFileContent.setColumnFamily(BLOB).setKey(blobId).setColumnNames(FILE_CONTENT);

        List<HColumn<String, String>> columns = query.execute().get().getColumns();
        if (columns.size() == 0) {
            return null;
        }
        Blob blob = new Blob();
        blob.setBlobId(blobId);
        blob.setComment("EMR Scanned Docs");

        for (HColumn<String, String> column : columns) {
            if (column == null) {
                continue;
            }
            String name = column.getName();
            String value = column.getValue();
            if (FILE_NAME.equals(name)) {
                blob.setFileName(value);
            } else if (CONTENT_TYPE.equals(name)) {
                blob.setContentType(value);
            } else if (TIME_STAMP.equals(name)) {
                blob.setTimeStamp(value);
            } else if (FILE_COMMENT.equals(name)) {
                blob.setComment(name);
            }
            HColumn<String, Long> fileSize = queryFileSize.execute().get().getColumnByName(FILE_SIZE);
            if (fileSize.getValue() != null) {
                blob.setFileSize(fileSize.getValue());
            }
            HColumn<String, byte[]> fileContent = queryFileContent.execute().get().getColumnByName(FILE_CONTENT);
            if (fileContent.getValue() != null) {
                blob.setFileContent(fileContent.getValue());
            }
        }
        return blob;
    }
    
    /**
     * Get blob ID list form record
     * @param recordId
     * @return
     */
    public List<UUID> getBlobIdList(String recordId) {
        SliceQuery<String, String, UUID> query = HFactory.createSliceQuery(emr, ss, ss, us);
        query.setColumnFamily(EMR).setKey(recordId);
        query.setRange("BLOB", "", false,2);
        List<HColumn<String, UUID>> columns = query.execute().get().getColumns();
        int columnSize = columns.size();
        List<UUID> blobList = new ArrayList<UUID>();

        for (HColumn<String, UUID> column : columns) {
            if (column == null) {
                continue;
            }
            String name = column.getName();
            UUID value = column.getValue();
            if (name.contains("BLOB")) {
                blobList.add(value);
            }
        }
        return blobList;
    }


//    public List<UUID> getBlobIdList(String recordId) {
//        SuperColumnQuery<String,String,String,String> query = HFactory.createSuperColumnQuery(emr,ss,ss,ss,ss);
//        query.setColumnFamily(EMR).setKey(recordId).setSuperName("BLOB");
//        List<HColumn<String, String>> columns = query.execute().get().getColumns();
//
//
//        List<UUID> blobList = new ArrayList<UUID>();
//
//        for (HColumn<String, String > column : columns) {
//            if (column == null) {
//                continue;
//            }
//            String name = column.getName();
//            String value = column.getValue();
//
//                blobList.add(UUID.fromString(value));
//
//        }
//        return blobList;
//    }

    /**
     * Save related userId and recordId in for created record in UserRecord table 
     * @param userId
     * @param recordId
     */
    public void saveUserRecord(String userId, String recordId) {
        Mutator<String> mutator = HFactory.createMutator(emr, ss);
        mutator.addInsertion(recordId, USER_RECORD, HFactory.createStringColumn(USER_ID, userId));
        try {
            mutator.execute();
        } catch (HectorException he) {
            //log.info(he.getMessage());
        }
    }
    
    /**
     * Update the record when user upload new image
     * @param recordId
     * @param blobId
     */
    public void updateRecordBlob(String recordId,UUID blobId) {
        String recordBlobId = "BLOB" + blobId.toString();
        Mutator<String> mutator = HFactory.createMutator(emr, ss);
        mutator.insert(recordId, EMR, HFactory.createColumn(recordBlobId,blobId, ss, us));
        mutator.execute();
    }

//      public void updateRecordBlob(String recordId,UUID blobId) {
//        String recordBlobId = blobId.toString();
//        Mutator<String> mutator = HFactory.createMutator(emr, ss);
//        mutator.insert(recordId,EMR,HFactory.createSuperColumn("BLOB",Arrays.asList(HFactory.createStringColumn("BLOBID",recordBlobId)),ss,ss,ss));
//        mutator.execute();
//    }
    
    /**
     * Delete record
     * @param recordId
     */
    public void deleteRecord(String recordId) {
        //To change body of created methods use File | Settings | File Templates.
        Mutator<String> mutator = HFactory.createMutator(emr, ss);
        mutator.delete(recordId,EMR,null,ss);
    }
    
    /**
     * Delete UserRecord entry
     * @param recordId
     */
    public void deleteUserRecord(String recordId) {
        Mutator<String> mutator = HFactory.createMutator(emr, ss);
        mutator.delete(recordId, USER_RECORD, null, ss);
    }
    
    /**
     *Delete blob 
     * @param blobId
     */
    public void deleteBlob(UUID blobId){
        Mutator<UUID> mutator = HFactory.createMutator(emr, us);
        mutator.delete(blobId, BLOB, null, ss);

    }
    
    /**
     * Save blob record
     * @param recordId
     * @param blobId
     */
    public void saveBlobRecord(String recordId, UUID blobId) {
        //To change body of created methods use File | Settings | File Templates.
        Mutator<UUID> mutator = HFactory.createMutator(emr, us);
        mutator.addInsertion(blobId, RECORD_BLOB, HFactory.createStringColumn(RECORD_ID, recordId));
        try {
            mutator.execute();
        } catch (HectorException he) {
            //log.info(he.getMessage());
        }

    }

    /**
     * Get record blobIds
     * @param recordId
     * @return
     */
    public List<UUID> getRecordBlobIds(String recordId) {
        List<UUID> blobId = new ArrayList<UUID>();
        IndexedSlicesQuery<UUID, String, String> query = HFactory.createIndexedSlicesQuery(emr, us, ss, ss);
        query.addEqualsExpression(RECORD_ID, recordId);
        query.setColumnNames(RECORD_ID);
        query.setColumnFamily(RECORD_BLOB);
        //query.setStartKey(null);
        QueryResult<OrderedRows<UUID, String, String>> result = query.execute();
        for (Row<UUID, String, String> row : result.get()) {
            if (row == null) {
                continue;
            }
            UUID keyValue = row.getKey();
            blobId.add(keyValue);
            //System.out.println(keyValue + ": ID\n");
        }
        return blobId;
    }
    
    /**
     * Delete record blob
     * @param blobId
     */
    public void deleteRecordBlob(UUID blobId) {
        //To change body of created methods use File | Settings | File Templates.
        Mutator<UUID> mutator = HFactory.createMutator(emr, us);
                mutator.delete(blobId, RECORD_BLOB, null, ss);


    }
}










