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
package org.wso2.carbon.dataservices.core.sqlparser;

import org.wso2.carbon.dataservices.core.DataServiceFault;
import org.wso2.carbon.dataservices.core.sqlparser.analysers.AnalyzerFactory;
import org.wso2.carbon.dataservices.core.sqlparser.analysers.KeyWordAnalyzer;
import org.wso2.carbon.dataservices.core.sqlparser.mappers.SelectMapper;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class SQLParserUtil {

     /**
     * This method returns the list of queried columns of a particular input sql string.
     *
     * @param tokens                Lexical tokens obtained after parsing the SQL query
     * @return                      List if columns expected as the output
     * @throws DataServiceFault     If any error occurs while parsing the SQL query
     */
    public static List<String> extractOutputColumns(Queue<String> tokens) throws DataServiceFault {

        KeyWordAnalyzer analyser;
        Queue<String> syntaxQueue = null;
        Queue<String> tempQueue = new LinkedList<String>();

        if (!tokens.isEmpty() && tokens.peek().toUpperCase().equals(LexicalConstants.SELECT)) {
            tokens.poll();
            while (!tokens.isEmpty() &&
                    !tokens.peek().toUpperCase().equals(LexicalConstants.FROM)) {
                tempQueue.add(tokens.poll());
            }
            analyser = AnalyzerFactory.createAnalyzer(LexicalConstants.SELECT, tempQueue);
            syntaxQueue = analyser.getSyntaxQueue();
            tempQueue.clear();
        }
        tokens.clear();
        return (new SelectMapper(syntaxQueue).getColumns());
    }

/**
     * Extracts out the Input mappings names specified in the query
     *
     * @param tokens            Lexical tokens obtained after parsing the SQL query
     * @return                  List of input mappings specified in the query
     * @throws DataServiceFault If any error occurs while parsing the query
     */
    public static List<String> extractInputMappings(Queue<String> tokens) throws DataServiceFault {
        KeyWordAnalyzer analyser;
        Queue<String> syntaxQueue = null;
        Queue<String> tempQueue = new LinkedList<String>();

        if (!tokens.isEmpty() && tokens.peek().toUpperCase().equals(LexicalConstants.WHERE)) {
            tokens.poll();
            while (!tokens.isEmpty()) {
                tempQueue.add(tokens.poll());
            }
            analyser = AnalyzerFactory.createAnalyzer(LexicalConstants.WHERE, tempQueue);
            syntaxQueue = analyser.getSyntaxQueue();
            tempQueue.clear();
        }
        tokens.clear();
        //return (new SelectMapper(selectQueue).getColumns());
        return new ArrayList<String>();
    }

}
