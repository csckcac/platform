/*
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.dataservices.sql.driver.parser.analyzer;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Queue;

public abstract class SQLKeyWordAnalyser {

    private String keywordName;

    private Queue<String> tokens;

    private Queue<String> processedTokens;


    public SQLKeyWordAnalyser(Queue<String> tokens) {
        this.tokens = tokens;
        this.keywordName = tokens.poll();
        this.processedTokens = new LinkedList<String>();
    }

    public abstract void analyze() throws SQLException;

    public Queue<String> getTokens() {
        return tokens;
    }

    public Queue<String> getProcessedTokens() {
        return processedTokens;
    }

    public String getKeywordName() {
        return keywordName;
    }

}
