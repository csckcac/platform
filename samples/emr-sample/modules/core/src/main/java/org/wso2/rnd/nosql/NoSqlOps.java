package org.wso2.rnd.nosql;
/**
 ~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 ~
 ~ WSO2 Inc. licenses this file to you under the Apache License,
 ~ Version 2.0 (the "License"); you may not use this file except
 ~ in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~    http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing,
 ~ software distributed under the License is distributed on an
 ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 ~ KIND, either express or implied.  See the License for the
 ~ specific language governing permissions and limitations
 ~ under the License.
 **/

import me.prettyprint.cassandra.model.IndexedSlicesQuery;
import me.prettyprint.cassandra.serializers.LongSerializer;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.cassandra.serializers.UUIDSerializer;
import me.prettyprint.cassandra.service.CassandraHostConfigurator;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.beans.HColumn;
import me.prettyprint.hector.api.beans.OrderedRows;
import me.prettyprint.hector.api.beans.Row;
import me.prettyprint.hector.api.exceptions.HectorException;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.mutation.Mutator;
import me.prettyprint.hector.api.query.QueryResult;
import me.prettyprint.hector.api.query.SliceQuery;
import org.wso2.rnd.nosql.model.Record;
import org.wso2.rnd.nosql.model.User;

import java.util.ArrayList;
import java.util.List;


/**
 * TODO
 */
public class NoSqlOps {

    //private static final Log log = LogFactory.getLog(NoSqlOps.class);

    private final static Cluster cassandraCluster = HFactory.createCluster("Stratos", new CassandraHostConfigurator("127.0.0.1:9160"));
    private final static Keyspace emr = HFactory.createKeyspace("emr", cassandraCluster);

    private final static StringSerializer ss = StringSerializer.get();
    private final static LongSerializer ls = LongSerializer.get();
    private final static UUIDSerializer us = UUIDSerializer.get();

    private final static String USER = "User";
    private final static String EMR = "Record";
    private final static String USER_RECORD = "UserRecord";

    public String getClusterName() {
        return cassandraCluster.getClusterName();
    }

    public User getUserbyUserId(String userId) {
        SliceQuery<String, String, String> query = HFactory.createSliceQuery(emr, ss, ss, ss);
        query.setColumnFamily(USER).setKey(userId).setColumnNames("Name", "Email", "DOB", "BloodGroup", "Password");
        List<HColumn<String, String>> columns = query.execute().get().getColumns();
        if (columns.size() == 0) {
            //log.info("User does not exist: " + userId);
            return null;
        }

        User user = new User(userId, columns.get(4).getValue(), columns.get(2).getValue(), "");
        user.setFullName(columns.get(3).getValue());
        user.setBloodGroup(columns.get(0).getValue());
        user.setDateOfBirth(columns.get(1).getName());
        return user;
    }

    public void saveUser(User user) {
        Mutator<String> mutator = HFactory.createMutator(emr, ss);
        mutator.addInsertion(user.getUserID(), USER, HFactory.createStringColumn("Name", user.getFullName()))
                .addInsertion(user.getUserID(), USER, HFactory.createStringColumn("Email", user.getEmail()))
                .addInsertion(user.getUserID(), USER, HFactory.createStringColumn("DOB", user.getDateOfBirth()))
                .addInsertion(user.getUserID(), USER, HFactory.createStringColumn("BloodGroup", user.getBloodGroup()))
                .addInsertion(user.getUserID(), USER, HFactory.createStringColumn("Password", user.getPassword()));


        mutator.execute();
    }

    public Record getEmrRecordbyId(String recordId) {
        SliceQuery<String, String, String> query = HFactory.createSliceQuery(emr, ss, ss, ss);
        query.setColumnFamily(EMR).setKey(recordId).setColumnNames("TimeStamp", "RecordType", "Sickness");
        List<HColumn<String, String>> columns = query.execute().get().getColumns();
        if (columns.size() == 0) {
            //log.info("EMR is empty:" + recordId);
            return null;
        }
        Record record = new Record(recordId, columns.get(2).getValue(), columns.get(1).getValue());
        record.setSickness(columns.get(0).getValue());
        return record;
    }

    public void saveEmrRecord(Record record) {
        Mutator<String> mutator = HFactory.createMutator(emr, ss);
        mutator.addInsertion(record.getRecordID(), EMR, HFactory.createStringColumn("TimeStamp", record.getTimeStamp()))
                .addInsertion(record.getRecordID(), EMR, HFactory.createStringColumn("RecordType", record.getRecordType()))
                .addInsertion(record.getRecordID(), EMR, HFactory.createStringColumn("Sickness", record.getSickness()));
        try {
            mutator.execute();
        } catch (HectorException he) {
            //log.info(he.getMessage());
        }
    }


    public List<String> getUserRecordIds(User user) {
        List<String> recordId = new ArrayList<String>();
        IndexedSlicesQuery<String, String, String> query = HFactory.createIndexedSlicesQuery(emr, ss, ss, ss);
        query.addEqualsExpression("UserID", user.getUserID());
        query.setColumnNames("RecordId", "UserID");
        query.setColumnFamily("UserRecord");
        query.setStartKey("");
        QueryResult<OrderedRows<String, String, String>> result = query.execute();
        for (Row<String, String, String> row : result.get()) {
            if (row == null) {
                continue;
            }
            String keyValue = row.getKey();
            recordId.add(keyValue);
            System.out.println(keyValue + ": ID\n");
        }
        return recordId;
    }


    public void saveUserRecord(Record userRecord, User user) {
        Mutator<String> mutator = HFactory.createMutator(emr, ss);
        mutator.addInsertion(userRecord.getRecordID(), USER_RECORD, HFactory.createStringColumn("UserID", user.getUserID()));
        try {
            mutator.execute();
        } catch (HectorException he) {
            //log.info(he.getMessage());
        }
    }

}
