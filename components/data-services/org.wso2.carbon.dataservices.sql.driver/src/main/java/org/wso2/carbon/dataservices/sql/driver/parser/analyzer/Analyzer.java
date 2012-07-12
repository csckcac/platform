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

import org.wso2.carbon.dataservices.sql.driver.parser.Constants;
import org.wso2.carbon.dataservices.sql.driver.parser.SQLParserUtil;

import java.sql.SQLException;
import java.util.List;
import java.util.Queue;

public class Analyzer extends SQLKeyWordAnalyser {

    private int columnCounter;

    private int valueCounter;

    private List<String> opValueList;

    public Object getColumnCounter() {
        return columnCounter;
    }

    public Object getValueCounter() {
        return valueCounter;
    }

    public List<String> getOpValueList() {
        return opValueList;
    }

    public enum QueryTypes{
        SELECT, INSERT, UPDATE, DELETE, CREATE
    }

    private QueryTypes types;

    public Analyzer(Queue<String> tokens) {
        super(tokens);
        String type = this.getTokens().poll();
        this.types = QueryTypes.valueOf(type.toUpperCase());
    }

    @Override
    public void analyze() throws SQLException {
        switch(types) {
            case SELECT:
                analyzeSelects();
                break;
            case INSERT:
                break;
            case UPDATE:
                break;
            case DELETE:
                break;
            case CREATE:
                break;
            default:
                break;
        }
    }

    private void analyzeSelects() throws SQLException {
        StringBuilder sb;
        if (SQLParserUtil.isAggregateFunction(getTokens().peek()) ||
                SQLParserUtil.isStringFunction(getTokens().peek())) {
            if (SQLParserUtil.isAggregateFunction(getTokens().peek())) {
                String aggFunction = getTokens().poll();
                getProcessedTokens().add(Constants.AGGREGATE_FUNCTION);
                getProcessedTokens().add(aggFunction);

                if (!getTokens().isEmpty()) {
                    analyzeSelects();
                }
            } else {
                String strFunction = getTokens().poll();
                getProcessedTokens().add(Constants.STRING_FUNCTION);
                getProcessedTokens().add(strFunction);

                if (!getTokens().isEmpty()) {
                    analyzeSelects();
                }
            }
        } else if (getTokens().peek().equals(Constants.LEFT_BRACKET)) {
            getTokens().poll();
            getProcessedTokens().add(Constants.START_OF_LBRACKET);
            if (!getTokens().isEmpty()) {
                analyzeSelects();
            }

        } else if (getTokens().peek().equals(Constants.RIGHT_BRACKET)) {
            getTokens().poll();
            getProcessedTokens().add(Constants.START_OF_RBRACKET);
            if (!getTokens().isEmpty()) {
                analyzeSelects();
            }

        } else if (getTokens().peek().equals(Constants.SINGLE_QUOTATION)) {
            sb = new StringBuilder();
            getTokens().poll();
            while (!getTokens().peek().equals(Constants.SINGLE_QUOTATION)) {
                sb.append(getTokens().poll());
            }
            getProcessedTokens().add(Constants.OP_VALUE);
            getProcessedTokens().add(sb.toString());
            getTokens().poll();

            if (!getTokens().isEmpty()) {
                analyzeSelects();
            }
        } else if (getTokens().peek().equals(Constants.COMMA)) {
            getTokens().poll();
            if (!getTokens().isEmpty()) {
                analyzeSelects();
            }
        } else if (getTokens().peek().equals(Constants.AS)) {
            getTokens().poll();
            if (SQLParserUtil.isStringLiteral(getTokens().peek())) {
                sb = new StringBuilder();
                while (!getTokens().isEmpty() && !getTokens().peek().equals(Constants.COMMA)) {
                    sb.append(getTokens().poll());
                }
                getProcessedTokens().add(Constants.AS_REF);
                getProcessedTokens().add(sb.toString());

                if (!getTokens().isEmpty()) {
                    analyzeSelects();
                }
            }
        } else {
            String strRef = getTokens().poll();
            if (!getTokens().isEmpty()) {
                if (getTokens().peek().equals(Constants.DOT)) {

                    getProcessedTokens().add(Constants.TABLE);
                    getProcessedTokens().add(strRef);
                    getTokens().poll();

                    if (!SQLParserUtil.isStringLiteral(getTokens().peek())) {
                        throw new SQLException("Token is not a string literal");
                    }
                    String columnRef = getTokens().poll();
                    getProcessedTokens().add(Constants.COLUMN);
                    getProcessedTokens().add(columnRef);

                    if (!getTokens().isEmpty()) {
                        if (getTokens().peek().equals(Constants.COMMA)) {
                            getTokens().poll();
                            analyze();
                        } else if (getTokens().peek().equals(Constants.RIGHT_BRACKET)) {
                            if (!getTokens().isEmpty()) {
                                analyze();
                            }
                        }
                    }
                } else if (getTokens().peek().equals(Constants.COMMA)) {
                    getProcessedTokens().add(Constants.COLUMN);
                    getProcessedTokens().add(strRef);
                    getTokens().poll();
                    analyze();
                } else if (getTokens().peek().equals(Constants.AS)) {
                    getProcessedTokens().add(Constants.COLUMN);
                    getProcessedTokens().add(strRef);
                    getTokens().poll();
                    getProcessedTokens().add(Constants.AS_COLUMN);
                    getProcessedTokens().add(getTokens().poll());
                    if (!getTokens().isEmpty()) {
                        analyze();
                    }
                } else {
                    getProcessedTokens().add(Constants.COLUMN);
                    getProcessedTokens().add(strRef);
                    if (!getTokens().isEmpty()) {
                        analyze();
                    }
                }
            } else {
                getProcessedTokens().add(Constants.COLUMN);
                getProcessedTokens().add(strRef);
            }
        }
    }

    public void analyzeInserts() throws SQLException {
        if (getTokens().peek().equals(Constants.INTO)) {
            /* processes INTO keyword */
            getTokens().poll();
            if (!SQLParserUtil.isStringLiteral(getTokens().peek())) {
                throw new SQLException("Inappropriate Table Name");
            }

            StringBuilder tableRef = new StringBuilder();
            while (!getTokens().isEmpty() && !getTokens().peek().equals(Constants.LEFT_BRACKET)) {
                tableRef.append(getTokens().poll());
            }
            getProcessedTokens().add(Constants.TABLE);
            getProcessedTokens().add(tableRef.toString());
            if (getTokens().peek().equals(Constants.LEFT_BRACKET)) {
                getTokens().poll();
                getProcessedTokens().add(Constants.COLUMNS);
                getProcessedTokens().add(Constants.START_OF_LBRACKET);

                processColumnNames(getTokens(), getProcessedTokens());

                if (!getTokens().peek().equals(Constants.VALUES)) {
                    throw new SQLException("Invalid Insert Statement - VALUES keyword missing");
                }
                getTokens().poll();
                if (getTokens().peek().equals(Constants.LEFT_BRACKET)) {
                    getTokens().poll();
                    getProcessedTokens().add(Constants.START_OF_LBRACKET);

                    processValues(getTokens(), getProcessedTokens());

                    if (this.getColumnCounter() != this.getValueCounter()) {
                        throw new SQLException("Number of values should be equal to the number " +
                                "of columns");
                    }
                    getProcessedTokens().add(Constants.VALUES);
                    for (String opValue : this.getOpValueList()) {
                        getProcessedTokens().add(Constants.OP_VALUE);
                        getProcessedTokens().add(opValue);
                    }

                    if (!getTokens().peek().equals(Constants.RIGHT_BRACKET)) {
                        throw new SQLException("Invalid end to the statement - Right bracket " +
                                "expected");
                    }
                    getTokens().poll();
                    getProcessedTokens().add(Constants.START_OF_RBRACKET);
                }
            }
        } else {
            //TODO: implement respective functionality for each of the keywords that can come
            // between INSERT & INTO
        }
    }

    private void processColumnNames(Queue<String> tokens,
                                    Queue<String> processedTokens) throws SQLException {
        if (!SQLParserUtil.isStringLiteral(tokens.peek())) {
            throw new SQLException("Invalid column name");
        }
        processedTokens.add(Constants.COLUMN);
        processedTokens.add(tokens.poll());
        columnCounter++;
        if (tokens.peek().equals(Constants.COMMA)) {
            tokens.poll();
            processColumnNames(tokens, processedTokens);
        } else if (tokens.peek().equals(Constants.RIGHT_BRACKET)) {
            tokens.poll();
            processedTokens.add(Constants.START_OF_RBRACKET);
        }
    }

    private void processValues(Queue<String> tokens, Queue<String> processedTokens) {
        StringBuilder sb;
        if (tokens.peek().equals(Constants.SINGLE_QUOTATION)) {
            tokens.poll();
            sb = new StringBuilder();
            while (!tokens.isEmpty() && !tokens.peek().equals(Constants.SINGLE_QUOTATION)) {
                sb.append(tokens.poll());
            }
            valueCounter++;
            this.getOpValueList().add(sb.toString());
        } else if (SQLParserUtil.isStringLiteral(tokens.peek())) {
            this.getOpValueList().add(tokens.poll());
            valueCounter++;
        }
        if (tokens.peek().equals(Constants.COMMA)) {
            tokens.poll();
            processValues(tokens, processedTokens);
        }
    }

    

}
