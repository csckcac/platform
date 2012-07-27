package org.wso2.carbon.logging.summarizer;

/*
 * Copyright 2005,2006 WSO2, Inc. http://www.wso2.org
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

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class QueryGenerator {

    String keyspaceName = "EVENT_KS";


    public List<String> createFilePaths() {
        ColumnFamilyHandler cfHandler = new ColumnFamilyHandler();

        List<String> selectedColFamilies = cfHandler.filterColumnFamilies(keyspaceName);
        String logFileLocation = "home/manisha/Desktop/logDir/logs/";

        List<String> filePathList = new ArrayList<String>(selectedColFamilies.size());


        for (int i = 0; i < selectedColFamilies.size(); i++) {
            String colFamilyName = selectedColFamilies.get(i);
            String[] strArrayColFamilyNameParts = colFamilyName.split("_");
            String tenantId = strArrayColFamilyNameParts[1];
            String serverName = strArrayColFamilyNameParts[2] + "_" + strArrayColFamilyNameParts[3];
            String date = strArrayColFamilyNameParts[4] + "_" + strArrayColFamilyNameParts[5] + "_" + strArrayColFamilyNameParts[6];
            filePathList.add(i, logFileLocation + tenantId + "/" + serverName + "/" +date);
        }
        return filePathList;
    }


    public void createQuery() throws Exception {
        System.out.println("came to createQuery()");
        List<String> filePaths = createFilePaths();
        List<String> filteredColFamilies = new ColumnFamilyHandler().filterColumnFamilies(keyspaceName);
        for (int i = 0; i < filePaths.size(); i++) {
            String filePath = "SET file_path = " + filePaths.get(i) + ";";
            String columnFamilyName = "SET logs_column_family = " + "\"" + filteredColFamilies.get(i) + "\";";

            StringBuilder hiveScript = new StringBuilder();
            try {

                BufferedReader reader = new BufferedReader(new FileReader("/home/manisha/" +
                        "trunk/carbon4/platform_new/trunk/components/logging/org.wso2.carbon." +
                        "logging.service/src/main/resources/hive/query/holder/logSummarizer.hql"));
                String line;

                String ls = System.getProperty("line.separator");

                while ((line = reader.readLine()) != null) {
                    if (line.equals("set logs_column_family = ? ;")) {
                        line = columnFamilyName;
                    }
                    if (line.equals("set file_path= ? ;")) {
                        line = filePath;
                    }
                    hiveScript.append(line);
                    hiveScript.append(ls);
                }


                reader.close();
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }

            //Delete this  *******************************************
             System.out.println("file path: " + filePath);
            System.out.println(hiveScript);

            ScriptScheduler scriptScheduler = new ScriptScheduler();
            scriptScheduler.runScript(hiveScript.toString());

            //Delete this  *******************************************
            System.out.println("\n\n\n\n\n\n");
        }


    }


//    public void createQuery() throws Exception {
//        System.out.println("Create ...");
//    }

     public static void main (String[] args) throws Exception {
         new QueryGenerator().createQuery();
     }
}

