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
package org.wso2.carbon.dataservices.sql.driver.parser;

import org.wso2.carbon.dataservices.sql.driver.query.ParamInfo;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class SQLParserUtil {

    private static List<String> keyWords = new ArrayList<String>();
    private static List<String> operators = new ArrayList<String>();
    private static List<String> delimiters = new ArrayList<String>();
    private static List<String> specialFunctions = new ArrayList<String>();
    private static List<String> stringFunctions = new ArrayList<String>();
    private static List<String> aggregateFunctions = new ArrayList<String>();
    private static List<String> dmlTypes = new ArrayList<String>();


    static {
        keyWords.add(Constants.COUNT);
        keyWords.add(Constants.SELECT);
        keyWords.add(Constants.FROM);
        keyWords.add(Constants.WHERE);
        keyWords.add(Constants.MAX);
        keyWords.add(Constants.INSERT);
        keyWords.add(Constants.INTO);
        keyWords.add(Constants.VALUES);
        keyWords.add(Constants.GROUP_BY);
        keyWords.add(Constants.ORDER_BY);
        keyWords.add(Constants.DISTINCT);
        keyWords.add(Constants.UPDATE);
        keyWords.add(Constants.SET);
        keyWords.add(Constants.IN);
        keyWords.add(Constants.AND);
        keyWords.add(Constants.DELAYED);
        keyWords.add(Constants.LOW_PRIORITY);
        keyWords.add(Constants.HIGH_PRIORITY);
        keyWords.add(Constants.ON);
        keyWords.add(Constants.DUPLICATE);
        keyWords.add(Constants.KEY);
        keyWords.add(Constants.LAST_INSERT_ID);
        keyWords.add(Constants.ALL);
        keyWords.add(Constants.DISTINCTROW);
        keyWords.add(Constants.STRAIGHT_JOIN);
        keyWords.add(Constants.SQL_SMALL_RESULT);
        keyWords.add(Constants.SQL_BIG_RESULT);
        keyWords.add(Constants.SQL_BUFFER_RESULT);
        keyWords.add(Constants.SQL_CACHE);
        keyWords.add(Constants.SQL_NO_CACHE);
        keyWords.add(Constants.SQL_CALC_FOUND_ROWS);
        keyWords.add(Constants.ASC);
        keyWords.add(Constants.DESC);
        keyWords.add(Constants.OFFSET);
        keyWords.add(Constants.LIMIT);
        keyWords.add(Constants.WITH);
        keyWords.add(Constants.ROLLUP);
        keyWords.add(Constants.PROCEDURE);
        keyWords.add(Constants.OUTFILE);
        keyWords.add(Constants.DUMPFILE);
        keyWords.add(Constants.LOCK);
        keyWords.add(Constants.SHARE);
        keyWords.add(Constants.MODE);
        keyWords.add(Constants.CONCAT);
        keyWords.add(Constants.AS);
        keyWords.add(Constants.AVG);
        keyWords.add(Constants.MIN);
        keyWords.add(Constants.IS);
        keyWords.add(Constants.NULL);
        keyWords.add(Constants.LIKE);
        keyWords.add(Constants.OR);
        keyWords.add(Constants.JOIN);
        keyWords.add(Constants.INNER);
        keyWords.add(Constants.SUM);
        keyWords.add(Constants.VALUE);

        operators.add(Constants.EQUAL);
        operators.add(Constants.MINUS);
        operators.add(Constants.PLUS);
        operators.add(Constants.FORWARD_SLASH);
        operators.add(Constants.ASTERISK);
        operators.add(Constants.GREATER_THAN);
        operators.add(Constants.DIVISION);

        delimiters.add(Constants.COMMA);
        delimiters.add(Constants.LESS_THAN);
        delimiters.add(Constants.SINGLE_QUOTATION);
        delimiters.add(Constants.SEMI_COLON);
        delimiters.add(Constants.COLON);
        delimiters.add(Constants.DOT);
        delimiters.add(Constants.LEFT_BRACE);
        delimiters.add(Constants.LEFT_BRACKET);
        delimiters.add(Constants.RIGHT_BRACE);
        delimiters.add(Constants.RIGHT_BRACKET);
        delimiters.add(Constants.HYPHEN);
        delimiters.add(Constants.UNDERSCORE);
        delimiters.add(Constants.WHITE_SPACE);

        aggregateFunctions.add(Constants.AVG);
        aggregateFunctions.add(Constants.MAX);
        aggregateFunctions.add(Constants.MIN);
        aggregateFunctions.add(Constants.COUNT);
        aggregateFunctions.add(Constants.SUM);

        stringFunctions.add(Constants.TRIM);
        stringFunctions.add(Constants.RTRIM);
        stringFunctions.add(Constants.LTRIM);
        stringFunctions.add(Constants.SUBSTR);
        stringFunctions.add(Constants.CONCAT);

        specialFunctions.add(Constants.OR);
        specialFunctions.add(Constants.AND);
        specialFunctions.add(Constants.IS);
        specialFunctions.add(Constants.LIKE);
        specialFunctions.add(Constants.NOT);
        specialFunctions.add(Constants.NULL);
        specialFunctions.add(Constants.IN);

        dmlTypes.add(Constants.INSERT);
        dmlTypes.add(Constants.UPDATE);
        dmlTypes.add(Constants.DELETE);

    }

    public static List<String> getKeyWordList() {
        return keyWords;
    }

    public static List<String> getDMLTypeList() {
        return dmlTypes;
    }

    public static List<String> getDelimiterList() {
        return delimiters;
    }

    public static List<String> getOperatorList() {
        return operators;
    }

    public static List<String> getAggregateFunctionList() {
        return aggregateFunctions;
    }

    public static List<String> getStringFunctionList() {
        return stringFunctions;
    }

    public static List<String> getSpecialFunctionList() {
        return specialFunctions;
    }

    public static boolean isJoinFunction(String token) {
        return (token.equals(Constants.INNER) || token.equals(Constants.JOIN));
    }

    public static boolean isSpecialFunction(String token) {
        return SQLParserUtil.getSpecialFunctionList().contains(token);
    }

    public static boolean isDelimiter(String token) {
        return SQLParserUtil.getDelimiterList().contains(token);
    }

    public static boolean isOperator(String token) {
        return SQLParserUtil.getOperatorList().contains(token);
    }

    public static boolean isAggregateFunction(String token) {
        return SQLParserUtil.getAggregateFunctionList().contains(token);
    }

    public static boolean isStringFunction(String token) {
        return SQLParserUtil.getStringFunctionList().contains(token);
    }

    public static boolean isKeyword(String token) {
        return SQLParserUtil.getKeyWordList().contains(token);
    }

    public static boolean isStringLiteral(String token) {
        return (!SQLParserUtil.isDelimiter(token) && !SQLParserUtil.isOperator(token) &&
                !SQLParserUtil.isKeyword(token));
    }

    public static Queue<String> getTokens(String inputStream) {
        char[] inputCharacters;
        StringBuilder token = new StringBuilder();
        Queue<String> tokenQueue = new LinkedList<String>();

        inputCharacters = new char[inputStream.length()];
        inputStream.getChars(0, inputStream.length(), inputCharacters, 0);

        for (char c : inputCharacters) {
            if (!SQLParserUtil.isDelimiter(Character.valueOf(c).toString()) &&
                    !SQLParserUtil.isOperator(Character.valueOf(c).toString())) {
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
        if (token.length() > 0) {
            tokenQueue.add(token.toString());
        }
        return tokenQueue;
    }

    public static boolean isSpecialFunctionInsideBrackets(String token) {
        return (Constants.OR.equals(token) || Constants.AND.equals(token));
    }

    public static boolean isDMLStatement(String type) {
        return SQLParserUtil.getDMLTypeList().contains(type.toUpperCase());
    }

    public static ParamInfo[] extractParameters(String sql) {
        List<ParamInfo> tmp = new ArrayList<ParamInfo>();
        int i = 0;
        int idx = 0;
        char[] s = sql.toCharArray();
        while (i < s.length - 1) {
            final char c = s[i];
            if (c == '?') {
                ParamInfo param = new ParamInfo(idx, null);
                tmp.add(param);
                idx++;
            }
            i++;
        }
        return tmp.toArray(new ParamInfo[tmp.size()]);
    }

    public static String extractFirstKeyword(String sql) {
        int i = 0;
        char[] s = sql.toCharArray();
        StringBuffer b = new StringBuffer();
        while (i < s.length - 1) {
            char c = s[i];
            if (c != ' ') {
                b.append(c);
            } else {
                break;
            }
            i++;
        }
        String token = b.toString();
        return (SQLParserUtil.isKeyword(token)) ? token : null;
    }



}
