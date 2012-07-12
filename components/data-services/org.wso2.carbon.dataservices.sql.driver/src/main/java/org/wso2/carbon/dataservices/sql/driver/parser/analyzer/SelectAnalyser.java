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
import java.util.Queue;

public class SelectAnalyser extends SQLKeyWordAnalyser {
    
    public SelectAnalyser(Queue<String> tokens) {
        super(tokens);
    }

    public void analyze() throws SQLException {
         StringBuilder sb;
        if (SQLParserUtil.isAggregateFunction(getTokens().peek()) ||
                SQLParserUtil.isStringFunction(getTokens().peek())) {
            if (SQLParserUtil.isAggregateFunction(getTokens().peek())) {
                String aggFunction = getTokens().poll();
                getProcessedTokens().add(Constants.AGGREGATE_FUNCTION);
                getProcessedTokens().add(aggFunction);

                if (!getTokens().isEmpty()) {
                    analyze();
                }
            } else {
                String strFunction = getTokens().poll();
                getProcessedTokens().add(Constants.STRING_FUNCTION);
                getProcessedTokens().add(strFunction);

                if (!getTokens().isEmpty()) {
                    analyze();
                }
            }
        } else if (getTokens().peek().equals(Constants.LEFT_BRACKET)) {
            getTokens().poll();
            getProcessedTokens().add(Constants.START_OF_LBRACKET);
            if (!getTokens().isEmpty()) {
                analyze();
            }

        } else if (getTokens().peek().equals(Constants.RIGHT_BRACKET)) {
            getTokens().poll();
            getProcessedTokens().add(Constants.START_OF_RBRACKET);
            if (!getTokens().isEmpty()) {
                analyze();
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
                analyze();
            }
        } else if (getTokens().peek().equals(Constants.COMMA)) {
            getTokens().poll();
            if (!getTokens().isEmpty()) {
                analyze();
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
                    analyze();
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

}
