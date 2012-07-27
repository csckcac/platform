/*
 *  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.dataservices.sql.driver.parser;

import java.sql.SQLException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Parser {

    private static final String DELIMITER = ";";

    public enum QueryTypes {
        SELECT, UPDATE, INSERT, DELETE, DROP, CREATE
    }

    public static Queue<String> parse(String sql, String type) throws SQLException {
        QueryTypes types = QueryTypes.valueOf(type);
        Queue<String> tokens = SQLParserUtil.getTokens(sql);
        Queue<String> processed = new ConcurrentLinkedQueue<String>();
        switch (types) {
            case SELECT:
                parseSelect(tokens, processed);
                break;
            case INSERT:
                parseInsert(tokens, processed);
                break;
            case UPDATE:
                parseUpdate(tokens, processed);
                break;
            case DELETE:
                parseDelete(tokens, processed);
                break;
            case CREATE:
                break;
            case DROP:
                break;
            default:
                throw new SQLException("Query type unsupported");
        }
        return processed;
    }

    private static void parseSelect(Queue<String> tokens,
                                    Queue<String> processed) throws SQLException {

    }

    private static void processSelect(Queue<String> tokens,
                                      Queue<String> processed) throws SQLException {
        StringBuilder sb;
        if (SQLParserUtil.isAggregateFunction(tokens.peek()) ||
                SQLParserUtil.isStringFunction(tokens.peek())) {
            if (SQLParserUtil.isAggregateFunction(tokens.peek())) {
                String aggFunction = tokens.poll();
                processed.add(Constants.AGGREGATE_FUNCTION);
                processed.add(aggFunction);

                if (!tokens.isEmpty()) {
                    throw new SQLException("Syntax Error");
                }
                parseSelect(tokens, processed);
            } else {
                String strFunction = tokens.poll();
                processed.add(Constants.STRING_FUNCTION);
                processed.add(strFunction);

                if (!tokens.isEmpty()) {
                    throw new SQLException("Syntax Error");
                }
                parseSelect(tokens, processed);
            }
        } else if (tokens.peek().equals(Constants.LEFT_BRACKET)) {
            tokens.poll();
            processed.add(Constants.START_OF_LBRACKET);
            if (!tokens.isEmpty()) {
                throw new SQLException("Syntax Error");
            }
            parseSelect(tokens, processed);

        } else if (tokens.peek().equals(Constants.RIGHT_BRACKET)) {
            tokens.poll();
            processed.add(Constants.START_OF_RBRACKET);
            if (!tokens.isEmpty()) {
                throw new SQLException("Syntax Error");
            }
            parseSelect(tokens, processed);
        } else if (tokens.peek().equals(Constants.SINGLE_QUOTATION)) {
            sb = new StringBuilder();
            tokens.poll();
            while (!tokens.peek().equals(Constants.SINGLE_QUOTATION)) {
                sb.append(tokens.poll());
            }
            processed.add(Constants.OP_VALUE);
            processed.add(sb.toString());
            tokens.poll();

            if (!tokens.isEmpty()) {
                throw new SQLException("Syntax Error");
            }
            parseSelect(tokens, processed);
        } else if (tokens.peek().equals(Constants.COMMA)) {
            tokens.poll();
            if (!tokens.isEmpty()) {
                throw new SQLException("Syntax Error");
            }
            parseSelect(tokens, processed);
        } else if (tokens.peek().equals(Constants.AS)) {
            tokens.poll();
            if (SQLParserUtil.isStringLiteral(tokens.peek())) {
                sb = new StringBuilder();
                while (!tokens.isEmpty() && !tokens.peek().equals(Constants.COMMA)) {
                    sb.append(tokens.poll());
                }
                processed.add(Constants.AS_REF);
                processed.add(sb.toString());

                if (!tokens.isEmpty()) {
                    throw new SQLException("Syntax Error");
                }
                parseSelect(tokens, processed);
            }
        } else {
            String strRef = tokens.poll();
            if (!tokens.isEmpty()) {
                if (tokens.peek().equals(Constants.DOT)) {

                    processed.add(Constants.TABLE);
                    processed.add(strRef);
                    tokens.poll();

                    if (!SQLParserUtil.isStringLiteral(tokens.peek())) {
                        throw new SQLException("Token is not a string literal");
                    }
                    String columnRef = tokens.poll();
                    processed.add(Constants.COLUMN);
                    processed.add(columnRef);

                    if (!tokens.isEmpty()) {
                        throw new SQLException("Syntax Error");
                    }
                    if (tokens.peek().equals(Constants.COMMA)) {
                        tokens.poll();
                        if (!tokens.isEmpty()) {
                            throw new SQLException("Syntax Error");
                        }
                        parseSelect(tokens, processed);
                    } else if (tokens.peek().equals(Constants.RIGHT_BRACKET)) {
                        if (!tokens.isEmpty()) {
                            throw new SQLException("Syntax Error");
                        }
                        parseSelect(tokens, processed);
                    }
                } else if (tokens.peek().equals(Constants.COMMA)) {
                    processed.add(Constants.COLUMN);
                    processed.add(strRef);
                    tokens.poll();
                    if (!tokens.isEmpty()) {
                        throw new SQLException("Syntax Error");
                    }
                    parseSelect(tokens, processed);
                } else if (tokens.peek().equals(Constants.AS)) {
                    processed.add(Constants.COLUMN);
                    processed.add(strRef);
                    tokens.poll();
                    processed.add(Constants.AS_COLUMN);
                    processed.add(tokens.poll());
                    if (!tokens.isEmpty()) {
                        throw new SQLException("Syntax Error");
                    }
                    parseSelect(tokens, processed);
                } else {
                    processed.add(Constants.COLUMN);
                    processed.add(strRef);
                    if (!tokens.isEmpty()) {
                        throw new SQLException("Syntax Error");
                    }
                    parseSelect(tokens, processed);
                }
            } else {
                processed.add(Constants.COLUMN);
                processed.add(strRef);
            }
        }
    }

    private static void parseInsert(Queue<String> tokens,
                                    Queue<String> processed) throws SQLException {
        boolean isParameterized = false;
        boolean isEnd = false;
        boolean isInit = true;

        if (!Constants.INSERT.equals(tokens.peek())) {
            throw new SQLException("Syntax Error : INSERT keyword is missing");
        }
        processed.add(tokens.poll());
        if (!Constants.INTO.equals(tokens.peek())) {
            throw new SQLException("Syntax Error : INTO keyword is missing");
        }
        processed.add(tokens.poll());
        if (!SQLParserUtil.isStringLiteral(tokens.peek())) {
            throw new SQLException("Syntax Error : String literal expected");
        }
        processed.add(Constants.TABLE);
        processed.add(tokens.poll());
        if (Constants.LEFT_BRACKET.equals(tokens.peek())) {
            tokens.poll();
            processInsertedColumns(tokens, processed);
            if (!Constants.RIGHT_BRACKET.equals(tokens.peek())) {
                throw new SQLException("Syntax Error : ')' expected");
            }
        }
        tokens.poll();
        if (!(Constants.VALUES.equals(tokens.peek()) || Constants.VALUE.equals(tokens.peek()))) {
            throw new SQLException("Syntax Error : VALUE/VALUES keyword missing");
        }
        processed.add(tokens.poll());
        if (!Constants.LEFT_BRACKET.equals(tokens.peek())) {
            throw new SQLException("Syntax Error : Left bracket is expected");
        }
        tokens.poll();
        processInsertedValues(tokens, processed, isParameterized, isEnd, isInit);
        if (!Constants.RIGHT_BRACKET.equals(tokens.peek())) {
            throw new SQLException("Syntax exception : ')' expected");
        }
        tokens.poll();
        processDelimiter(tokens);
    }

    private static void processInsertedColumns(Queue<String> tokens,
                                               Queue<String> processed) throws SQLException {
        if (!SQLParserUtil.isStringLiteral(tokens.peek())) {
            throw new SQLException("Syntax Error : String literal expected");
        }
        processed.add(Constants.COLUMN);
        processed.add(tokens.poll());
        if (Constants.COMMA.equals(tokens.peek())) {
            tokens.poll();
            processInsertedColumns(tokens, processed);
        }
    }

    private static void processInsertedValues(Queue<String> tokens,
                                              Queue<String> processed,
                                              boolean isParameterized,
                                              boolean isEnd, boolean isInit) throws SQLException {
        if (!isEnd) {
            if (!SQLParserUtil.isStringLiteral(tokens.peek())) {
                throw new SQLException("Syntax Error : String literal expected");
            }
            if ("?".equals(tokens.peek())) {
                if (isInit) {
                    isParameterized = true;
                    isInit = false;
                }
                if (!isParameterized) {
                    throw new SQLException("Both parameters and inline parameter values are not " +
                            "allowed to exist together");
                }
                isParameterized = true;
                processed.add(Constants.PARAM_VALUE);
                processed.add(tokens.poll());
            } else if (Constants.SINGLE_QUOTATION.equals(tokens.peek())) {
                if (isInit) {
                    isInit = false;
                    isParameterized = false;
                }
                if (isParameterized) {
                    throw new SQLException("Both parameters and inline parameter values are not " +
                            "allowed to exist together");
                }
                processed.add(tokens.poll());
                StringBuilder b = new StringBuilder();
                while (Constants.SINGLE_QUOTATION.equals(tokens.peek()) || tokens.isEmpty()) {
                    b.append(tokens.poll());
                }
                processed.add(b.toString());
                processed.add(Constants.SINGLE_QUOTATION);
            } else if (SQLParserUtil.isStringLiteral(tokens.peek())) {
                if (isInit) {
                    isInit = false;
                    isParameterized = false;
                }
                if (isParameterized) {
                    throw new SQLException("Both parameters and inline parameter values are not " +
                            "allowed to exist together");
                }
                processed.add(tokens.peek());
            }
            if (!Constants.COMMA.equals(tokens.peek())) {
                isEnd = true;
            } else {
                tokens.poll();
            }
            processInsertedValues(tokens, processed, isParameterized, isEnd, isInit);
        }
    }

    private static void parseUpdate(Queue<String> tokens,
                                    Queue<String> processed) throws SQLException {
        if (!Constants.UPDATE.equals(tokens.peek())) {
            throw new SQLException("Syntax Error : UPDATE keyword missing");
        }
        processed.add(tokens.poll());
        if (!SQLParserUtil.isStringLiteral(tokens.peek())) {
            throw new SQLException("Syntax Error : Table name missing");
        }
        processed.add(Constants.TABLE);
        processed.add(tokens.poll());
        if (!Constants.SET.equals(tokens.peek())) {
            throw new SQLException("Syntax Error : SET keyword missing");
        }
        processed.add(Constants.SET);
        setUpdateTargets(tokens, processed);
        if (Constants.WHERE.equals(tokens.peek())) {
            processed.add(tokens.poll());
            processWhere(tokens, processed);
        }
        processDelimiter(tokens);
    }

    private static void setUpdateTargets(Queue<String> tokens,
                                         Queue<String> processed) throws SQLException {
        if (!SQLParserUtil.isStringLiteral(tokens.peek())) {
            throw new SQLException("Syntax Error");
        }
        processed.add(Constants.COLUMN);
        processed.add(tokens.poll());
        if (Constants.WHERE.equals(tokens.peek())) {
            //do nothing
        } else if (Constants.COMMA.equals(tokens.peek())) {
            tokens.poll();
            setUpdateTargets(tokens, processed);
        } else {
            throw new SQLException("Syntax Error");
        }
    }

    private static void parseDelete(Queue<String> tokens,
                                    Queue<String> processed) throws SQLException {
        if (!Constants.DELETE.equals(tokens.peek())) {
            throw new SQLException("Syntax Error : 'DELETE' expected");
        }
        processed.add(tokens.poll());
        if (!Constants.FROM.equals(tokens.peek())) {
            throw new SQLException("Syntax Error : 'FROM' expected");
        }
        processed.add(tokens.poll());
        if (!SQLParserUtil.isStringFunction(tokens.peek())) {
            throw new SQLException("Syntax Error : String literal expected");
        }
        processed.add(Constants.TABLE);
        processed.add(tokens.poll());
        if (Constants.WHERE.equals(tokens.peek())) {
            processWhereTargets(tokens, processed);
        }
        processDelimiter(tokens);
    }

    private static void processWhere(Queue<String> tokens,
                                     Queue<String> processed) throws SQLException {
        if (!Constants.WHERE.equals(tokens.peek())) {
            throw new SQLException("Syntax Error : 'WHERE' clause expected");
        }
        processed.add(tokens.poll());
        if (!SQLParserUtil.isStringLiteral(tokens.peek())) {
            throw new SQLException("Syntax Error : String literal expected");
        }
        processWhereTargets(tokens, processed);
        processDelimiter(tokens);
    }

    private static void processDelimiter(Queue<String> tokens) throws SQLException {
        if (Parser.DELIMITER.equals(tokens.peek())) {
            tokens.peek();
        } else if (SQLParserUtil.isKeyword(tokens.peek())) {
            throw new SQLException("Synatax Error : ';' expected");
        } else {
            //do nothing
        }
    }

    private static void processWhereTargets(Queue<String> tokens,
                                            Queue<String> processed) throws SQLException {
        boolean isParameterized = false;
        boolean isEnd = false;
        if (!SQLParserUtil.isStringLiteral(tokens.peek())) {
            throw new SQLException("Syntax Error : String literal expected");
        }
        processed.add(Constants.COLUMN);
        if (!Constants.EQUAL.equals(tokens.peek())) {
            throw new SQLException("Syntax Error : '=' expected");
        }
        processWhereColumnValues(tokens, processed, isParameterized, isEnd);
    }

    private static void processWhereColumnValues(Queue<String> tokens,
                                                 Queue<String> processed,
                                                 boolean isParameterized,
                                                 boolean isEnd) throws SQLException {
        if (!isEnd) {
            if (!SQLParserUtil.isStringLiteral(tokens.peek())) {
                throw new SQLException("Syntax Error : String literal expected");
            }
            if ("?".equals(tokens.peek())) {
                if (!isParameterized) {
                    throw new SQLException("Both parameters and inline parameter values are not " +
                            "allowed to exist together");
                }
                isParameterized = true;
                processed.add(Constants.PARAM_VALUE);
                processed.add(tokens.poll());
            } else if (Constants.SINGLE_QUOTATION.equals(tokens.peek())) {
                if (isParameterized) {
                    throw new SQLException("Both parameters and inline parameter values are not " +
                            "allowed to exist together");
                }
                isParameterized = false;
                processed.add(tokens.poll());
                StringBuilder b = new StringBuilder();
                while (Constants.SINGLE_QUOTATION.equals(tokens.peek()) || tokens.isEmpty()) {
                    b.append(tokens.poll());
                }
                processed.add(b.toString());
                processed.add(Constants.SINGLE_QUOTATION);
            } else if (SQLParserUtil.isStringLiteral(tokens.peek())) {
                if (isParameterized) {
                    throw new SQLException("Both parameters and inline parameter values are not " +
                            "allowed to exist together");
                }
                isParameterized = false;
                processed.add(tokens.poll());
            }
            if (!Constants.AND.equals(tokens.peek()) || !Constants.OR.equals(tokens.peek())) {
                isEnd = true;
            }
            tokens.poll();
            processWhereColumnValues(tokens, processed, isParameterized, isEnd);
        }
    }

}
