/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

package org.wso2.carbon.automation.utils.governance.utils;

import java.io.BufferedReader;
import java.io.IOException;

public class FileReader {

    public static String readFile(String filePath) throws IOException {
        BufferedReader reader = null;
        java.io.FileReader fileReader = null;
        StringBuilder stringBuilder;
        String line;
        String ls;
        try {
            stringBuilder = new StringBuilder();
            fileReader = new java.io.FileReader(filePath);
            reader = new BufferedReader(fileReader);

            ls = System.getProperty("line.separator");

            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append(ls);
            }

        } finally {
            if (fileReader != null) {
                fileReader.close();
            }
            if (reader != null) {
                reader.close();
            }
        }
        return stringBuilder.toString();
    }
}
