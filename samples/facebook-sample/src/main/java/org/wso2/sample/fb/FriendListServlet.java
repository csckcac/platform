/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.sample.fb;

import me.prettyprint.cassandra.model.BasicColumnFamilyDefinition;
import me.prettyprint.cassandra.serializers.LongSerializer;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.cassandra.service.CassandraHostConfigurator;
import me.prettyprint.cassandra.service.ThriftCfDef;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.beans.ColumnSlice;
import me.prettyprint.hector.api.ddl.ColumnFamilyDefinition;
import me.prettyprint.hector.api.ddl.ComparatorType;
import me.prettyprint.hector.api.ddl.KeyspaceDefinition;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.mutation.Mutator;
import me.prettyprint.hector.api.query.QueryResult;
import me.prettyprint.hector.api.query.SliceQuery;
import org.apache.cassandra.locator.SimpleStrategy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class FriendListServlet extends HttpServlet {

    // cluster configuration
    private final HashMap credentials = new HashMap<String, String>() {
        {
            put("username", "admin");
            put("password", "admin");
        }
    };
   
    private final static StringSerializer ss = StringSerializer.get();

    private final static LongSerializer ls = LongSerializer.get();

    private static final Log log = LogFactory.getLog(FriendListServlet.class);


    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        // read params from the request and store them
        String value = request.getParameter("friendList");
        String uid = request.getParameter(Constants.UID);

        // get the list of friends in the data store 
        String friendList = getFriendsList(uid);
        String removedList;
        if(!friendList.equals("")){
            removedList = getRemovedFriendsList(friendList, value);
        } else {
            removedList = new JSONArray().toString();    
        }
        
        // save new friend list
        saveUser(uid, value, getKeyspace());

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println(removedList);

        out.close();
    }

    private Keyspace getKeyspace() {
        Cluster cassandraCluster = HFactory.createCluster(
                Constants.CLUSTER_NAME, new CassandraHostConfigurator(Constants.CLUSTER_HOST), credentials);

        if (cassandraCluster.describeKeyspace(Constants.KEYSPACE_NAME) == null) {
            BasicColumnFamilyDefinition columnFamilyDefinition = new BasicColumnFamilyDefinition();
            columnFamilyDefinition.setKeyspaceName(Constants.KEYSPACE_NAME);
            columnFamilyDefinition.setName(Constants.COL_FAMILY_NAME);
            columnFamilyDefinition.setComparatorType(ComparatorType.LONGTYPE);

            ColumnFamilyDefinition cfDef = new ThriftCfDef(columnFamilyDefinition);

            KeyspaceDefinition keyspaceDefinition =
                    HFactory.createKeyspaceDefinition(Constants.KEYSPACE_NAME,
                                                      SimpleStrategy.class.getName(), 1, Arrays.asList(cfDef));

            cassandraCluster.addKeyspace(keyspaceDefinition);
        }

        Keyspace friendListKS = HFactory.createKeyspace(Constants.KEYSPACE_NAME, cassandraCluster);
        //cassandraCluster.getConnectionManager().shutdown(); // shutdown the connection  
        return friendListKS;
    }

    public void saveUser(String id, String jsonString, Keyspace keySpace) {
        String status;
        JSONArray friendListArr;
        Mutator<String> mutator = HFactory.createMutator(keySpace, ss);
        try {
            friendListArr = new JSONArray(jsonString);

            for (int i = 0; i < friendListArr.length(); i++) {
                JSONObject tempFriend = friendListArr.getJSONObject(i);

                mutator.addInsertion(id, Constants.COL_FAMILY_NAME,
                                     HFactory.createColumn(Long.parseLong(tempFriend.getString(Constants.ID)),
                                                           tempFriend.getString(Constants.NAME) + "|" +
                                                           tempFriend.get(Constants.PICTURE), ls, ss));
                if (i != 0 && i % Constants.PAGINATION_FACTOR == 0) {
                    // insert a batch of 100 records 
                    mutator.execute();
                }
            }
            // insert the pending records in the mutator buffer (num of records < 100)
            mutator.execute();
            status = "Successfully stored friend details.";

        } catch (JSONException e) {
            status = "An error occurred while storing the friend list";
            log.error(status, e);
            return;
        }
    }

    private String getFriendsList(String id) {
        /*Cluster cassandraCluster = HFactory.createCluster(
                Constants.CLUSTER_NAME, new CassandraHostConfigurator(Constants.CLUSTER_HOST), credentials);*/
        System.out.println("#######   In getFriendsList()   #######");
        //Keyspace keyspaceOperator = HFactory.createKeyspace(Constants.KEYSPACE_NAME, cassandraCluster);
        Keyspace keyspaceOperator = getKeyspace();
        // query to get the list of friends for a particular user id
        SliceQuery<String, Long, String> query = HFactory.createSliceQuery(keyspaceOperator, ss, ls, ss).
                setColumnFamily(Constants.COL_FAMILY_NAME).setKey(id).setRange(0L, Long.MAX_VALUE, false, 4);
        QueryResult<ColumnSlice<Long, String>> result = query.execute();
        ColumnSlice<Long, String> resultCols = result.get();

        // *** new user, no friend list to compare against *** 
        if (resultCols.getColumns().size() == 0) {
            return "";
        }

        // collection to hold the returned friend details
        List<Long> friendData = new ArrayList<Long>();
        JSONArray friendsJsonArr = new JSONArray();

        do {
            for (int i = 0; i < resultCols.getColumns().size() - 1; i++) {
                try {
                    JSONObject tempObj = new JSONObject().put(Constants.ID, resultCols.getColumns().get(i).getName());
                    tempObj.put(Constants.VALUE, resultCols.getColumns().get(i).getValue());
                    friendsJsonArr.put(tempObj);
                } catch (JSONException e) {
                    String msg = "An error occurred while retrieving friends list";
                    log.error(msg, e);
                    return msg;
                }
            }

            int tmpSize;
            if ((tmpSize = resultCols.getColumns().size()) < 4) {
                try {
                    JSONObject tempObj = new JSONObject().put(Constants.ID, resultCols.getColumns().get(tmpSize - 1).getName());
                    tempObj.put(Constants.VALUE, resultCols.getColumns().get(tmpSize - 1).getValue());
                    friendsJsonArr.put(tempObj);
                } catch (JSONException e) {
                    String msg = "An error occurred while retrieving friends list";
                    log.error(msg, e);
                    return msg;
                }
                break;
            }

            query.setRange(resultCols.getColumns().get(3).getName(), Long.MAX_VALUE, false, 4);
            result = query.execute();
            resultCols = result.get();

        } while (result.get().getColumns().size() > 0);

        return friendsJsonArr.toString();
    }

    private void deleteRecords(String key) {
        Mutator<String> mutator = HFactory.createMutator(getKeyspace(), ss);
        mutator.addDeletion(key, Constants.KEYSPACE_NAME, null, ss);
        mutator.execute();
    }

    public String getRemovedFriendsList(String oldList, String latest) {
        JSONArray removeFriends;
        try {
            // two friend lists to compare
            JSONArray oldFriendsList = new JSONArray(oldList);
            JSONArray latestList = new JSONArray(latest);

            // copy the IDs of existing friends
            Map<Long, String> oldFriendList = new HashMap<Long, String>();
            for (int i=0; i<oldFriendsList.length(); i++) {
                JSONObject tmpObj = oldFriendsList.getJSONObject(i); 
                oldFriendList.put(Long.parseLong(tmpObj.get(Constants.ID).toString()),
                                  tmpObj.get(Constants.VALUE).toString());
            }
            System.out.println("oldFriendList # " + oldFriendList.size());

            List<Long> latestFriendListID = new ArrayList<Long>();
            for (int i=0; i<latestList.length(); i++) {
                latestFriendListID.add(Long.parseLong(latestList.getJSONObject(i).get(Constants.ID).toString()));
            }
            System.out.println("latestFriendListID # " + latestFriendListID.size());

            removeFriends = new JSONArray();
            for (Iterator<Map.Entry<Long, String>> iterator = oldFriendList.entrySet().iterator(); iterator.hasNext();) {
                Map.Entry<Long, String> friendEntry = iterator.next();
                if (!latestFriendListID.contains(friendEntry.getKey())) {
                    String [] tmpArr = friendEntry.getValue().split("\\|");
                    JSONObject tmpObj = new JSONObject().put(Constants.NAME, tmpArr[0]).put(Constants.PICTURE, tmpArr[1]);
                    removeFriends.put(tmpObj);
                }
            }


        } catch (JSONException e) {
            String msg = "Error parsing friends list";
            log.error(msg, e);
            return msg;
        }

        //System.out.println(removeFriends.length());
        return removeFriends.toString();

    }

    public String read() {
        InputStream in = FriendListServlet.class.getClassLoader().getResourceAsStream("response.txt");
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String strLine;
        String str = "";
        JSONObject obj = null;
        String xx = "";
        try {
            while ((strLine = br.readLine()) != null) {
                str += strLine;
            }
        } catch (IOException e) {
        }
        try {
            obj = new JSONObject(str);
            xx =  obj.getString("data").toString();
        } catch (JSONException e) {

        }

        return xx;
        //System.out.println(arr.length());
        //saveUser("00001", obj.toString(), getKeyspace());
    }
    
}