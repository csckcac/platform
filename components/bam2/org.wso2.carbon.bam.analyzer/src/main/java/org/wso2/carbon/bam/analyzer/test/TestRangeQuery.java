/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.bam.analyzer.test;

import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.beans.OrderedRows;
import me.prettyprint.hector.api.beans.Row;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.query.QueryResult;
import me.prettyprint.hector.api.query.RangeSlicesQuery;
import org.wso2.carbon.bam.core.persistence.cassandra.CassandraUtils;
import org.wso2.carbon.cassandra.dataaccess.ClusterInformation;

public class TestRangeQuery {

    private static volatile Keyspace bamKeySpace;

    private static StringSerializer stringSerializer = StringSerializer.get();

    public static void main(String[] args) {
        Cluster bamCluster = CassandraUtils.createCluster(
                new ClusterInformation("admin", "admin"));
        bamKeySpace = (HFactory.createKeyspace("BAMKeyspace", bamCluster));

        RangeSlicesQuery<String, String, String> query = HFactory.createRangeSlicesQuery(
                bamKeySpace, stringSerializer, stringSerializer, stringSerializer);
        query.setColumnFamily("rangeQuery");
        query.setRowCount(3);
        query.setKeys("", "");
        query.setRange("", "", false, 2);
        QueryResult<OrderedRows<String, String, String>> result = query.execute();

        while (result.get().getCount() > 0) {
            Row<String, String, String> lastRow = result.get().peekLast();
/*        result.get().getList().remove(result.get().getList().size());
        for (Row<String,String, String> row : result.get().getList()) {
            System.out.println(row.getKey());
        }*/

            for (int i = 0; i < result.get().getCount() - 1; i++) {
                System.out.println(result.get().getList().get(i).getKey());
            }

            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

            query = HFactory.createRangeSlicesQuery(
                    bamKeySpace, stringSerializer, stringSerializer, stringSerializer);
            query.setColumnFamily("rangeQuery");
            query.setRowCount(3);
            query.setRange("", "", false, 2);
            query.setKeys(lastRow.getKey(), "");
            result = query.execute();
        }
    }

}
