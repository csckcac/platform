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

import java.util.Queue;

public class SetAnalyser extends SQLKeyWordAnalyser {

    public SetAnalyser(Queue<String> tokens) {
        super(tokens);
    }

    public void analyze() {
        //TODO: add dot notation compatibility
        if (SQLParserUtil.isStringLiteral(getTokens().peek())) {
            StringBuilder columnRef = new StringBuilder();
            columnRef.append(getTokens().poll());
            getProcessedTokens().add(Constants.COLUMN);
            getProcessedTokens().add(columnRef.toString());

            if (getTokens().peek().equals(Constants.EQUAL)) {
                getProcessedTokens().add(Constants.OPERATOR);
                getProcessedTokens().add(getTokens().poll());

                if (!SQLParserUtil.isDelimiter(getTokens().peek()) &&
                        !SQLParserUtil.isKeyword(getTokens().peek()) &&
                        !SQLParserUtil.isOperator(getTokens().peek())) {
                    getProcessedTokens().add(Constants.OP_VALUE);
                    getProcessedTokens().add(getTokens().poll());

                } else if (getTokens().peek().equals(Constants.SINGLE_QUOTATION)) {
                    getTokens().poll();
                    StringBuilder opValue = new StringBuilder();
                    while (!getTokens().isEmpty() &&
                            !getTokens().peek().equals(Constants.SINGLE_QUOTATION)) {
                        opValue.append(getTokens().poll());
                    }
                    getProcessedTokens().add(Constants.OP_VALUE);
                    getProcessedTokens().add(opValue.toString());
                }

                if (getTokens().peek().equals(Constants.COMMA)) {
                    analyze();
                }
            }
        }
    }

}
