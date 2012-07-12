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
import java.util.LinkedList;
import java.util.Queue;

public class WhereAnalyser extends SQLKeyWordAnalyser {

    private boolean isAnOpValue = false;

    public WhereAnalyser(Queue<String> tokens) {
        super(tokens);
    }

    //TODO implement the parsing logic for dot notation

    public void analyze() throws SQLException {
        if (getTokens().peek().equals(Constants.NOT)) {
            getTokens().poll();
            if (SQLParserUtil.isStringLiteral(getTokens().peek())) {
                analyseStringLiteral(getTokens(), getProcessedTokens());
            }
        } else if (getTokens().peek().equals(Constants.LEFT_BRACKET)) {
            getTokens().poll();
            getProcessedTokens().add(Constants.START_OF_LBRACKET);
            analyseLeftBracket(getTokens(), getProcessedTokens());
        } else if (SQLParserUtil.isStringLiteral(getTokens().peek())) {
            analyseStringLiteral(getTokens(), getProcessedTokens());
        } else {
            throw new SQLException("Error-error in the input");
        }
    }

    private void analyseStringLiteral(Queue<String> tokens, 
                                 Queue<String> processedTokens) throws SQLException {
        StringBuilder sb;
        if (this.isAnOpValue()) {
            String val = tokens.poll();
            processedTokens.add(Constants.OP_VALUE);
            processedTokens.add(val);
            isAnOpValue = false;
        }
        //add functionality to handle key words
        if (SQLParserUtil.isStringLiteral(tokens.peek())) {
            String columnRef = tokens.poll();
            processedTokens.add(Constants.COLUMN);
            processedTokens.add(columnRef);
        }

        if (!tokens.isEmpty()) {
            if (SQLParserUtil.isOperator(tokens.peek())) {
                StringBuilder sbOperator = new StringBuilder();
                sbOperator.append(tokens.poll());

                isAnOpValue = true;
                processedTokens.add(Constants.OPERATOR);
                processedTokens.add(sbOperator.toString());

                if (SQLParserUtil.isOperator(tokens.peek())) {
                    sbOperator = new StringBuilder().append(tokens.poll());
                    isAnOpValue = true;
                    processedTokens.add(Constants.OPERATOR);
                    processedTokens.add(sbOperator.toString());

                } else if (tokens.peek().equals(Constants.SINGLE_QUOTATION)) {
                    sb = new StringBuilder();
                    tokens.poll();
                    while (!tokens.peek().equals(Constants.SINGLE_QUOTATION)) {
                        sb.append(tokens.poll());
                    }
                    processedTokens.add(Constants.OP_VALUE);
                    processedTokens.add(sb.toString());
                    isAnOpValue = false;

                    tokens.poll();
                    if (!tokens.isEmpty()) {
                        if (SQLParserUtil.isSpecialFunctionInsideBrackets(tokens.peek())) {
                            sb = new StringBuilder().append(tokens.poll());
                            processedTokens.add(sb.toString());
                            if (SQLParserUtil.isStringLiteral(tokens.peek())) {
                                analyseStringLiteral(tokens, processedTokens);
                            }
                        } else if (tokens.peek().equals(Constants.START_OF_RBRACKET)) {
                            processedTokens.add(Constants.START_OF_RBRACKET);
                        }
                    }
                } else if (SQLParserUtil.isStringLiteral(tokens.peek())) {
                    analyseStringLiteral(tokens, processedTokens);
                } else {
                    throw new SQLException("Error-parsing operand");
                }
            } else if (SQLParserUtil.isSpecialFunction(tokens.peek())) {
                String specialFunction = tokens.poll();
                processedTokens.add(specialFunction);

                if (specialFunction.equals(Constants.IN)) {
                    if (tokens.peek().equals(Constants.LEFT_BRACKET)) {
                        processNestedQuery(tokens, processedTokens);
                    }
                } else {
                    if (SQLParserUtil.isSpecialFunction(tokens.peek()) ||
                            tokens.peek().equals(Constants.NOT)) {
                        processedTokens.add(tokens.poll());

                        if (!tokens.isEmpty()) {
                            if (SQLParserUtil.isSpecialFunction(tokens.peek()) ||
                                    tokens.peek().equals(Constants.NOT)) {
                                processedTokens.add(tokens.poll());
                            }
                        }
                    } else if (tokens.peek().equals(Constants.SINGLE_QUOTATION)) {

                        sb = new StringBuilder();
                        tokens.poll();

                        while (!tokens.peek().equals(Constants.SINGLE_QUOTATION)) {
                            sb.append(tokens.poll());
                        }
                        tokens.poll();
                        processedTokens.add(Constants.OP_VALUE);
                        processedTokens.add(sb.toString());

                        if (!tokens.isEmpty()) {
                            if (SQLParserUtil.isSpecialFunctionInsideBrackets(tokens.peek())) {
                                processedTokens.add(tokens.poll());

                                if (tokens.peek().equals(Constants.SINGLE_QUOTATION)) {
                                    sb = new StringBuilder();
                                    tokens.poll();
                                    while (!tokens.peek().equals(Constants.SINGLE_QUOTATION)) {
                                        sb.append(tokens.poll());
                                    }
                                    tokens.poll();
                                }
                            }
                        }
                    } else if (SQLParserUtil.isStringLiteral(tokens.peek())) {
                        processedTokens.add(Constants.COLUMN);
                        processedTokens.add(tokens.poll());
                        analyze();
                    }
                }
            } else if (tokens.peek().equals(Constants.LEFT_BRACKET)) {
                tokens.poll();
                analyseLeftBracket(tokens, processedTokens);
            } else if (tokens.peek().equals(Constants.IN)) {
                tokens.poll();
                if (tokens.peek().equals(Constants.LEFT_BRACKET)) {
                    processNestedQuery(tokens, processedTokens);
                }
            } else {
                throw new SQLException("Error-parsing WHERE clause");
            }
        }
    }


    private void analyseLeftBracket(Queue<String> tokens,
                                    Queue<String> processedTokens) throws SQLException {
        if (SQLParserUtil.isKeyword(tokens.peek())) {
            processNestedQuery(tokens, processedTokens);
        } else if (SQLParserUtil.isStringLiteral(tokens.peek())) {
            analyseStringLiteral(tokens, processedTokens);
            
            if (!tokens.isEmpty()) {
                if (!SQLParserUtil.isSpecialFunctionInsideBrackets(tokens.peek())) {
                    throw new SQLException("Error-while parsing special function keys inside brackets");
                }
                processedTokens.add(tokens.poll());

                if (!SQLParserUtil.isStringLiteral(tokens.peek())) {
                    throw new SQLException("Error-while parsing string literal inside the brackets");
                }
                analyseStringLiteral(tokens, processedTokens);
            }
        }
    }

    private void processNestedQuery(Queue<String> tokens, Queue<String> processedTokens) {
        processedTokens.add(Constants.START_OF_LBRACKET);
        //TODO: limitation - if there is not a query this may throw an exception
        tokens.poll();

        SQLKeyWordAnalyser keyAnalyser;
        Queue<String> testQueue = new LinkedList<String>();
        if (!tokens.isEmpty() && tokens.peek().equals(Constants.SELECT)) {
            tokens.poll();
            while (!tokens.isEmpty() && !tokens.peek().equals(Constants.FROM)) {
                testQueue.add(tokens.poll());
            }
            keyAnalyser = new SelectAnalyser(null);
            processedTokens.addAll(keyAnalyser.getProcessedTokens());
            testQueue.clear();
        }
        if (!tokens.isEmpty() && tokens.peek().equals(Constants.FROM)) {
            tokens.poll();
            while (!tokens.isEmpty() && !tokens.peek().equals(Constants.WHERE)) {
                testQueue.add(tokens.poll());
            }
            keyAnalyser = new FromAnalyser(null);
            processedTokens.addAll(keyAnalyser.getProcessedTokens());
            testQueue.clear();

        }
        if (!tokens.isEmpty() && tokens.peek().equals(Constants.WHERE)) {
            tokens.poll();
            while (!tokens.isEmpty()) {
                testQueue.add(tokens.poll());
            }
            keyAnalyser = new WhereAnalyser(null);
            processedTokens.addAll(keyAnalyser.getProcessedTokens());
            testQueue.clear();
        }

        //TODO: Adding the right bracket to the syntax queue at this point might be tricky
        processedTokens.add(Constants.START_OF_RBRACKET);
    }

    public boolean isAnOpValue() {
        return isAnOpValue;
    }

    public Queue<String> getProcessedTokens(Queue<String> tokens) throws SQLException {
        return null;
    }
}
