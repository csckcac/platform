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
package org.wso2.carbon.dataservices.core.sqlparser.analysers;

import org.wso2.carbon.dataservices.core.sqlparser.constants.LexicalConstants;
import org.wso2.carbon.dataservices.core.sqlparser.mappers.SelectMapper;

import java.util.*;

/**
 * This classes analyses and maps SQL statements that appear between SELECT and FROM
 * keywords of a SQL query.
 */
public class SyntaxAnalyser {

    private Queue<String> tokenQueue;

    public SyntaxAnalyser(Queue<String> tokenQueue) {
        this.tokenQueue = tokenQueue;
    }

    /**
     * This method returns the list of queried columns of a particular input sql string.
     *
     * @return a list of strings
     */
    public List<String> processSelectStatement() {

        SelectKeywordAnalyser keyAnalyser;
        Queue<String> selectQueue = null;
        Queue<String> tempQueue = new LinkedList<String>();

        if (!tokenQueue.isEmpty() &&
                tokenQueue.peek().toUpperCase().equals(LexicalConstants.SELECT)) {
            tokenQueue.poll();
            while (!tokenQueue.isEmpty() &&
                    !tokenQueue.peek().toUpperCase().equals(LexicalConstants.FROM)) {
                tempQueue.add(tokenQueue.poll());
            }
            keyAnalyser = new SelectKeywordAnalyser(tempQueue);
            selectQueue = keyAnalyser.getSyntaxQueue();
            tempQueue.clear();
        }

        tokenQueue.clear();

        return (new SelectMapper(selectQueue).getColumns());
    }

}