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

import java.util.LinkedList;
import java.util.Queue;

/**
 * This class parses the content between SELECT and FROM keywords of a sql query
 */
public class SELECTAnalyser {

    private Queue<String> tempQueue;
    private Queue<String> syntaxQueue = new LinkedList<String>();

    public SELECTAnalyser(Queue<String> tempQueue) {
        this.tempQueue=tempQueue;
        analyseStatement();
    }

    public void analyseStatement() {
        StringBuilder sb;
        if (LexicalConstants.isAggregateFunction(tempQueue.peek()) ||
                LexicalConstants.isStringFunction(tempQueue.peek())) {
            if (LexicalConstants.isAggregateFunction(tempQueue.peek())) {

                String aggFunction = tempQueue.poll();
                syntaxQueue.add(LexicalConstants.AGGREGATEFUNC);
                syntaxQueue.add(aggFunction);

                if (!tempQueue.isEmpty()) {
                    analyseStatement();
                }

            } else {
                String strFunction = tempQueue.poll();
                syntaxQueue.add(LexicalConstants.STRINGFUNC);
                syntaxQueue.add(strFunction);

                if (!tempQueue.isEmpty()) {
                    analyseStatement();
                }
            }
        } else if (tempQueue.peek().equals(LexicalConstants.LEFT_BRACKET)) {
            tempQueue.poll();
            syntaxQueue.add(LexicalConstants.START_OF_LBRACKET);
            if (!tempQueue.isEmpty()) {
                analyseStatement();
            }

        } else if (tempQueue.peek().equals(LexicalConstants.RIGHT_BRACKET)) {
            tempQueue.poll();
            syntaxQueue.add(LexicalConstants.START_OF_RBRACKET);
            if (!tempQueue.isEmpty()) {
                analyseStatement();
            }

        } else if (tempQueue.peek().equals(LexicalConstants.SINGLE_QUOTATION)) {
            sb = new StringBuilder();
            tempQueue.poll();
            while (!tempQueue.peek().equals(LexicalConstants.SINGLE_QUOTATION)) {
                sb.append(tempQueue.poll());
            }
            syntaxQueue.add(LexicalConstants.OPVALUE);
            syntaxQueue.add(sb.toString());
            tempQueue.poll();

            if (!tempQueue.isEmpty()) {
                analyseStatement();
            }

        } else if (tempQueue.peek().equals(LexicalConstants.COMMA)) {
            tempQueue.poll();
            if (!tempQueue.isEmpty()) {
                analyseStatement();
            }
        } else if (tempQueue.peek().equals(LexicalConstants.AS)) {
            tempQueue.poll();
            if (!LexicalConstants.getDelimiters().contains(tempQueue.peek()) ||
                    !LexicalConstants.getKeyWords().contains(tempQueue.peek()) ||
                    !LexicalConstants.getOperators().contains(tempQueue.peek())) {
                sb = new StringBuilder();
                while (!tempQueue.isEmpty()&& !tempQueue.peek().equals(LexicalConstants.COMMA)) {
                    sb.append(tempQueue.poll());
                }
                syntaxQueue.add(LexicalConstants.ASREF);
                syntaxQueue.add(sb.toString());

                if (!tempQueue.isEmpty()) {
                    analyseStatement();
                }
            }
        } else {
            String strRef = tempQueue.poll();
            if (!tempQueue.isEmpty()) {
                if (tempQueue.peek().equals(LexicalConstants.DOT)) {

                    syntaxQueue.add(LexicalConstants.TABLE);
                    syntaxQueue.add(strRef);
                    tempQueue.poll();

                    if (!LexicalConstants.getDelimiters().contains(tempQueue.peek()) &&
                            !LexicalConstants.getKeyWords().contains(tempQueue.peek()) &&
                            !LexicalConstants.getOperators().contains(tempQueue.peek())) {

                        String columnRef = tempQueue.poll();
                        syntaxQueue.add(LexicalConstants.COLUMN);
                        syntaxQueue.add(columnRef);

                        if (!tempQueue.isEmpty()) {
                            if (tempQueue.peek().equals(LexicalConstants.COMMA)) {
                                tempQueue.poll();
                                analyseStatement();
                            } else if (tempQueue.peek().equals(LexicalConstants.RIGHT_BRACKET)) {
                                if (!tempQueue.isEmpty()) {
                                    analyseStatement();
                                }
                            }
                        }
                    } else {
                        System.out.println("Error-not string!");
                    }
                } else if (tempQueue.peek().equals(LexicalConstants.COMMA)) {

                    syntaxQueue.add(LexicalConstants.COLUMN);
                    syntaxQueue.add(strRef);
                    tempQueue.poll();
                    analyseStatement();
                } else if (tempQueue.peek().equals(LexicalConstants.AS)) {

                    syntaxQueue.add(LexicalConstants.COLUMN);
                    syntaxQueue.add(strRef);
                    tempQueue.poll();
                    syntaxQueue.add(LexicalConstants.ASCOLUMN);
                    syntaxQueue.add(tempQueue.poll());
                    if (!tempQueue.isEmpty()) {
                        analyseStatement();
                    }

                } else {
                    syntaxQueue.add(LexicalConstants.COLUMN);
                    syntaxQueue.add(strRef);
                    if (!tempQueue.isEmpty()) {
                        analyseStatement();
                    }
                }
            } else {
                syntaxQueue.add(LexicalConstants.COLUMN);
                syntaxQueue.add(strRef);
            }
        }
    }

    public Queue<String> getSyntaxQueue() {
        return syntaxQueue;
    }
}