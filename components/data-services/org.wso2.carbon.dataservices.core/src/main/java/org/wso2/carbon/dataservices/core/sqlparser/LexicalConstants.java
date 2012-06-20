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

import java.util.ArrayList;
import java.util.List;

public class LexicalConstants {

    public static List<String> keyWords;
    public static List<String> operators;
    public static List<String> delimiters;
    public static List<String> specialFunctions;
    public static List<String> stringFunctions;
    public static List<String> aggregateFunctions;

    public static final String SEMI_COLON = ";";
    public static final String EQUAL = "=";
    public static final String WHITE_SPACE = " ";
    public static final String COMMA = ",";
    public static final String ASTERISK = "*";
    public static final String DOT = ".";
    public static final String HYPHEN = "`";
    public static final String GREATER_THAN = ">";
    public static final String LESS_THAN = "<";
    public static final String LEFT_BRACKET = "(";
    public static final String RIGHT_BRACKET = ")";
    public static final String LEFT_BRACE = "{";
    public static final String RIGHT_BRACE = "}";
    public static final String PLUS = "+";
    public static final String MINUS = "-";
    public static final String UNDERSCORE = "_";
    public static final String DIVISION = "/";
    public static final String FORWARD_SLASH = "/";
    public static final String COLON = ":";
    public static final String SINGLE_QUOTATION = "'";

    public static final String SELECT="SELECT";
    public static final String FROM = "FROM";
    public static final String WHERE = "WHERE";
    public static final String INSERT = "INSERT";
    public static final String INTO = "INTO";
    public static final String GROUP_BY = "GROUP BY";
    public static final String ORDER_BY = "ORDER_BY";
    public static final String COUNT = "COUNT";
    public static final String MAX = "MAX";
    public static final String VALUES = "VALUES";
    public static final String UPDATE = "UPDATE";
    public static final String SET = "SET";
    public static final String DELAYED = "DELAYED";
    public static final String LOW_PRIORITY = "LOW_PRIORITY";
    public static final String HIGH_PRIORITY = "HIGH_PRIORITY";
    public static final String ON = "ON";
    public static final String DUPLICATE = "DUPLICATE";
    public static final String KEY = "KEY";
    public static final String LAST_INSERT_ID = "LAST_INSERT_ID";
    public static final String ALL = "ALL";
    public static final String DISTINCT = "DISTINCT";
    public static final String DISTINCTROW = "DISTINCTROW";
    public static final String STRAIGHT_JOIN = "STRAIGHT_JOIN";
    public static final String SQL_SMALL_RESULT = "SQL_SMALL_RESULT";
    public static final String SQL_BIG_RESULT = "SQL_BIG_RESULT";
    public static final String SQL_BUFFER_RESULT = "SQL_BUFFER_RESULT";
    public static final String SQL_CACHE = "SQL_CACHE";
    public static final String SQL_NO_CACHE = "SQL_NO_CACHE";
    public static final String SQL_CALC_FOUND_ROWS = "SQL_CALC_FOUND_ROWS";
    public static final String ASC = "ASC";
    public static final String DESC = "DESC";
    public static final String LIMIT = "LIMIT";
    public static final String OFFSET = "OFFSET";
    public static final String WITH = "WITH";
    public static final String ROLLUP = "ROLLUP";
    public static final String PROCEDURE = "PROCEDURE";
    public static final String OUTFILE = "OUTFILE";
    public static final String DUMPFILE = "DUMPFILE";
    public static final String LOCK = "LOCK";
    public static final String SHARE = "SHARE";
    public static final String MODE = "MODE";
    public static final String CONCAT = "CONCAT";
    public static final String AS = "AS";
    public static final String AVG = "AVG";
    public static final String MIN = "MIN";
    public static final String TRIM = "TRIM";
    public static final String LTRIM = "LTRIM";
    public static final String RTRIM = "RTRIM";
    public static final String SUBSTR = "SUBSTR";
    public static final String NOT = "NOT";
    public static final String IS = "IS";
    public static final String IN = "IN";
    public static final String NULL = "NULL";
    public static final String LIKE = "LIKE";
    public static final String AND = "AND";
    public static final String OR = "OR";
    public static final String JOIN = "JOIN";
    public static final String INNER = "INNER";
    public static final String SUM = "SUM";

    public static final String COLUMN = "COLUMN";
    public static final String TABLE = "TABLE";
    public static final String OPERATOR = "OPERATOR";
    public static final String OPVALUE = "OPVALUE";
    public static final String ASREF = "ASREF";
    public static final String AGGREGATEFUNC = "AGGREGATEFUNC";
    public static final String STRINGFUNC = "STRINGFUNC";
    public static final String START_OF_LBRACKET = "START_OF_LBRACKET";
    public static final String START_OF_RBRACKET = "START_OF_RBRACKET";
    public static final String ASCOLUMN = "ASCOLUMN";
    public static final String COLUMNS="COLUMNS";

    public static List<String> getKeyWords() {
        keyWords = new ArrayList<String>();

        keyWords.add(COUNT);
        keyWords.add(SELECT);
        keyWords.add(FROM);
        keyWords.add(WHERE);
        keyWords.add(MAX);
        keyWords.add(INSERT);
        keyWords.add(INTO);
        keyWords.add(VALUES);
        keyWords.add(GROUP_BY);
        keyWords.add(ORDER_BY);
        keyWords.add(DISTINCT);
        keyWords.add(UPDATE);
        keyWords.add(SET);
        keyWords.add(IN);
        keyWords.add(AND);
        keyWords.add(DELAYED);
        keyWords.add(LOW_PRIORITY);
        keyWords.add(HIGH_PRIORITY);
        keyWords.add(ON);
        keyWords.add(DUPLICATE);
        keyWords.add(KEY);
        keyWords.add(LAST_INSERT_ID);
        keyWords.add(ALL);
        keyWords.add(DISTINCTROW);
        keyWords.add(STRAIGHT_JOIN);
        keyWords.add(SQL_SMALL_RESULT);
        keyWords.add(SQL_BIG_RESULT);
        keyWords.add(SQL_BUFFER_RESULT);
        keyWords.add(SQL_CACHE);
        keyWords.add(SQL_NO_CACHE);
        keyWords.add(SQL_CALC_FOUND_ROWS);
        keyWords.add(ASC);
        keyWords.add(DESC);
        keyWords.add(OFFSET);
        keyWords.add(LIMIT);
        keyWords.add(WITH);
        keyWords.add(ROLLUP);
        keyWords.add(PROCEDURE);
        keyWords.add(OUTFILE);
        keyWords.add(DUMPFILE);
        keyWords.add(LOCK);
        keyWords.add(SHARE);
        keyWords.add(MODE);
        keyWords.add(CONCAT);
        keyWords.add(AS);
        keyWords.add(AVG);
        keyWords.add(MIN);
        keyWords.add(IS);
        keyWords.add(NULL);
        keyWords.add(LIKE);
        keyWords.add(OR);
        keyWords.add(JOIN);
        keyWords.add(INNER);
        keyWords.add(SUM);

        return keyWords;
    }

    public static List<String> getOperators() {
        operators = new ArrayList<String>();

        operators.add(EQUAL);
        operators.add(MINUS);
        operators.add(PLUS);
        operators.add(FORWARD_SLASH);
        operators.add(ASTERISK);
        operators.add(GREATER_THAN);
        operators.add(DIVISION);

        return operators;
    }

    public static List<String> getDelimiters() {
        delimiters = new ArrayList<String>();

        delimiters.add(COMMA);
        delimiters.add(LESS_THAN);
        delimiters.add(SINGLE_QUOTATION);
        delimiters.add(SEMI_COLON);
        delimiters.add(COLON);
        delimiters.add(DOT);
        delimiters.add(LEFT_BRACE);
        delimiters.add(LEFT_BRACKET);
        delimiters.add(RIGHT_BRACE);
        delimiters.add(RIGHT_BRACKET);
        delimiters.add(HYPHEN);
        delimiters.add(WHITE_SPACE);

        return delimiters;
    }

    public static boolean isAggregateFunction(String token) {
        aggregateFunctions = new ArrayList<String>();

        aggregateFunctions.add(AVG);
        aggregateFunctions.add(MAX);
        aggregateFunctions.add(MIN);
        aggregateFunctions.add(COUNT);
        aggregateFunctions.add(SUM);

        return aggregateFunctions.contains(token);
    }

    public static boolean isStringFunction(String token) {
        stringFunctions = new ArrayList<String>();

        stringFunctions.add(TRIM);
        stringFunctions.add(RTRIM);
        stringFunctions.add(LTRIM);
        stringFunctions.add(SUBSTR);
        stringFunctions.add(CONCAT);

        return stringFunctions.contains(token);
    }

    public static boolean isSpecialFunctionInsideBrackets(String token) {
        specialFunctions = new ArrayList<String>();

        specialFunctions.add(OR);
        specialFunctions.add(AND);

        return specialFunctions.contains(token);
    }

    public static boolean isSpecialFunction(String token) {
        specialFunctions = new ArrayList<String>();

        specialFunctions.add(OR);
        specialFunctions.add(AND);
        specialFunctions.add(IS);
        specialFunctions.add(LIKE);
        specialFunctions.add(NOT);
        specialFunctions.add(NULL);
        specialFunctions.add(IN);

        return specialFunctions.contains(token);
    }

    public static boolean isAnOperator(String token) {
        return token.equals(OPERATOR);
    }

    public static boolean isJoinFunction(String token) {
        return (token.equals(INNER) || token.equals(JOIN));
    }
}