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
package org.wso2.bam.integration.test.common.publisher;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QueryTemplate {

    public static String getSelectString(String table, List<String> columns, Map<String, String> whereClause,
                                         boolean isUnique) {
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT ");

        if (isUnique) {
            sb.append("DISTINCT ");
        }

        int count = 0;
        if (columns != null && columns.size() == 1 && columns.get(0).trim().equals("*")) {

            sb.append("*");

        } else if (columns != null) {

            for (String column : columns) {
                sb.append(column);
                count++;

                if (count != columns.size()) {
                    sb.append(",");
                }
            }
        }

        sb.append(" FROM ");
        sb.append(table);

        count = 0;
        if (whereClause != null && whereClause.size() > 0) {
            sb.append(" WHERE ");
            for (String column : whereClause.keySet()) {
                sb.append(column);
                sb.append("='");
                sb.append(whereClause.get(column));
                sb.append("'");

                count++;
                if (count != whereClause.size()) {
                    sb.append(" AND ");
                }
            }
        }
        return sb.toString();
    }

    public static String getInsertString(String table, Map<String, String> columns) {
        StringBuffer sb = new StringBuffer();
        sb.append("INSERT INTO " + table);
        sb.append("(");

        int count = 0;
        for (String column : columns.keySet()) {
            sb.append(column);
            count++;

            if (count != columns.keySet().size()) {
                sb.append(",");
            }
        }

        sb.append(")");
        sb.append(" VALUES ");
        sb.append("(");

        count = 0;
        for (String column : columns.keySet()) {
            sb.append("'");
            sb.append(columns.get(column));
            sb.append("'");

            count++;
            if (count != columns.keySet().size()) {
                sb.append(",");
            }
        }

        sb.append(")");
        return sb.toString();
    }

    public static void main(String[] args) {
        Map<String, String> whereClause = new HashMap<String, String>();
        whereClause.put("BAM_URL", "1");
        whereClause.put("BAM_TENANT_ID", "2");
        whereClause.put("BAM_TYPE", "3");
        whereClause.put("BAM_CATEGORY", "4");

        String insert = QueryTemplate.getInsertString("BAM_SERVER", whereClause);
        System.out.println(insert);
    }
}
