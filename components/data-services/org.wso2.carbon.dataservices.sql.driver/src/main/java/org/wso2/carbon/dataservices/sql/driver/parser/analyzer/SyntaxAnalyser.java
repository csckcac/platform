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

import org.wso2.carbon.dataservices.sql.driver.parser.AnalyzerException;
import org.wso2.carbon.dataservices.sql.driver.parser.Constants;
import org.wso2.carbon.dataservices.sql.driver.query.TQuery;
import org.wso2.carbon.dataservices.sql.driver.query.TQueryFactory;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class SyntaxAnalyser {

    private Queue<String> tokenQueue;

    public SyntaxAnalyser(Queue<String> tokenQueue) {
        this.tokenQueue = tokenQueue;
    }

    public TQuery getSQLQuery() {
        if (!this.getTokenQueue().isEmpty()) {
            String type = this.getTokenQueue().peek();
            try {
                SQLKeyWordAnalyser analyser =
                    SQLKeyWordAnalyzerFactory.createAnalyzer(this.getTokenQueue());
            } catch (AnalyzerException e) {
                //ignore
            }
        }
        return null;
    }

    private TQuery processUpdateStatement() throws AnalyzerException {
        Queue<String> tempQueue = new LinkedList<String>();
        Map<String, Queue<String>> map = new HashMap<String, Queue<String>>();

        if (!this.getTokenQueue().isEmpty()) {
            String peekToken = this.getTokenQueue().peek();
            if (Constants.UPDATE.equals(peekToken)) {
                this.getTokenQueue().poll();
                while (!this.getTokenQueue().isEmpty() &&
                        !this.getTokenQueue().peek().equals(Constants.SET)) {
                    tempQueue.add(this.getTokenQueue().poll());
                }
                //map.put(Constants.UPDATE, new UpdateAnalyser().getSyntaxQueue());
                tempQueue.clear();
            }

            if (Constants.SET.equals(peekToken)) {
                this.getTokenQueue().poll();
                while (!this.getTokenQueue().isEmpty() &&
                        !this.getTokenQueue().peek().equals(Constants.WHERE)) {
                    tempQueue.add(this.getTokenQueue().poll());
                }
                //map.put(Constants.SET, new SetAnalyser().getSyntaxQueue());
                tempQueue.clear();
            }

            if (Constants.WHERE.equals(peekToken)) {
                this.getTokenQueue().poll();
                while (!this.getTokenQueue().isEmpty()) {
                    tempQueue.add(this.getTokenQueue().poll());
                }
                //map.put(Constants.WHERE, new WhereAnalyser().getSyntaxQueue());
                tempQueue.clear();
            }
        }
        return TQueryFactory.createQuery(Constants.UPDATE, null);
    }

    private TQuery processInsertStatement() throws AnalyzerException {
        if (!this.getTokenQueue().isEmpty() &&
                Constants.INSERT.equals(this.getTokenQueue().peek())) {
            this.getTokenQueue().poll();
        }
        //Queue<String> tokens = (new InsertAnalyser()).getProcessedTokens();
        //String table = getSelectedTables(tokens).get(0);
        //List<String> columns = getSelectedColumns(tokens);
        //return new InsertQuery(Constants.INSERT, columns, table);
        return TQueryFactory.createQuery(Constants.INSERT, null);
    }

    private TQuery processSelectStatement() throws AnalyzerException {
        Queue<String> tempQueue = new LinkedList<String>();
        TQuery query = TQueryFactory.createQuery(Constants.SELECT, null);

        if (!this.getTokenQueue().isEmpty()) {
            if (!this.getTokenQueue().isEmpty() &&
                    Constants.SELECT.equals(this.getTokenQueue().peek())) {
                this.getTokenQueue().poll();
                while (!this.getTokenQueue().isEmpty() &&
                        !this.getTokenQueue().peek().equals(Constants.FROM)) {
                    tempQueue.add(this.getTokenQueue().poll());
                }
//                query.setSelectedColumns(getSelectedColumns(
//                        new SelectAnalyser().getSyntaxQueue()));
                tempQueue.clear();
            }

            if (!this.getTokenQueue().isEmpty() &&
                    Constants.FROM.equals(this.getTokenQueue().peek())) {
                this.getTokenQueue().poll();
                while (!this.getTokenQueue().isEmpty() &&
                        !this.getTokenQueue().peek().equals(Constants.WHERE)) {
                    tempQueue.add(this.getTokenQueue().poll());
                }
//                query.setSelectedTables(getSelectedTables(
//                        new FromAnalyser().getSyntaxQueue()));
                tempQueue.clear();
            }

            if (!this.getTokenQueue().isEmpty() &&
                    Constants.WHERE.equals(this.getTokenQueue().peek())) {
                this.getTokenQueue().poll();
                while (!this.getTokenQueue().isEmpty()) {
                    tempQueue.add(this.getTokenQueue().poll());
                }
                tempQueue.clear();
            }
        }

        return query;
    }

    public Queue<String> getTokenQueue() {
        return tokenQueue;
    }


}
