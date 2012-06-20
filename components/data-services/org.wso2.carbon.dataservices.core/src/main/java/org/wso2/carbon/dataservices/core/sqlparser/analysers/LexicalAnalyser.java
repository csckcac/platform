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


import org.wso2.carbon.dataservices.core.sqlparser.LexicalConstants;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class LexicalAnalyser {

    private String inputStream = null;
    private List<String> delimiters;
    private List<String> operators;

    public LexicalAnalyser(String inputStream) {
        this.inputStream = inputStream;
        delimiters = LexicalConstants.getDelimiters();
        operators = LexicalConstants.getOperators();
    }

    /**
     * This particular method transform a particular SQL string to a set of tokens and returns
     * a queue which contains the tokens(String objects) produced by the logic of the method.
     *
     * @return a Queue of String objects.
     */
    public Queue<String> getTokens() {

        char[] inputCharacters;
        StringBuilder token = new StringBuilder();
        Queue<String> tokenQueue = new LinkedList<String>();

        inputCharacters = new char[this.inputStream.length()];
        inputStream.getChars(0, inputStream.length(), inputCharacters, 0);

        for (char c : inputCharacters) {

            if (!delimiters.contains(Character.valueOf(c).toString()) &&
                    !operators.contains(Character.valueOf(c).toString())) {
                token.append(c);
            } else {
                if (token.length() > 0) {
                    tokenQueue.add(token.toString());
                }
                if (!Character.valueOf(c).toString().equals(" ")) {
                    tokenQueue.add(new StringBuilder().append(c).toString());
                }
                token = new StringBuilder();
            }
        }

        //This condition is checked in order to avoid enqueing null tokens into the token queue.
        if (token.length() > 0) {
            tokenQueue.add(token.toString());
        }
        return tokenQueue;
    }
}