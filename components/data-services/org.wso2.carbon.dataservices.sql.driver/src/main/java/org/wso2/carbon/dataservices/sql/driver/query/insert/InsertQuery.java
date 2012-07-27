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
package org.wso2.carbon.dataservices.sql.driver.query.insert;

import org.wso2.carbon.dataservices.sql.driver.parser.Constants;
import org.wso2.carbon.dataservices.sql.driver.parser.SQLParserUtil;
import org.wso2.carbon.dataservices.sql.driver.query.ParamInfo;
import org.wso2.carbon.dataservices.sql.driver.query.Query;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public abstract class InsertQuery extends Query{

    private String targetTable;

      private List<ParamInfo> inputParams;

      private Map<Integer, String> paramValues;

      private boolean isAll = false;

      private boolean isParameterized = false;

      int paramCount = 0;

      int valueCount = 0;

      public InsertQuery(Connection connection, Queue<String> processedTokens,
                         ParamInfo[] parameters) {
          super(connection, processedTokens, parameters);
          this.inputParams = Arrays.asList(parameters);
          this.paramValues = new HashMap<Integer, String>();
          try {
              preprocessTokens(processedTokens);
          } catch (SQLException e) {
              e.printStackTrace();
          }
      }

      private void preprocessTokens(Queue<String> tokens) throws SQLException {
          if (tokens == null || tokens.isEmpty()) {
              throw new SQLException("Unable to populate attributes");
          }
          tokens.poll();
          tokens.poll();
          if (!Constants.TABLE.equals(tokens.peek())) {
              throw new SQLException("Table name is missing");
          }
          tokens.poll();
          if (!Constants.COLUMNS.equals(tokens.peek())) {
              this.targetTable = tokens.poll();
          } else {
              this.isAll = true;
          }
          if (Constants.COLUMNS.equals(tokens.peek())) {
              tokens.poll();
              if (Constants.START_OF_LBRACKET.equals(tokens.peek())) {
                  tokens.poll();
                  this.processInputParamNames(tokens);
                  if (Constants.START_OF_RBRACKET.equals(tokens.peek())) {
                      tokens.poll();
                      tokens.poll(); //hack
                      if (Constants.VALUES.equals(tokens.peek())) {
                          tokens.poll();
                          this.processInputParamNames(tokens);
                      }
                  }
              }
          }
      }

      private void processInputParamNames(Queue<String> tokens) throws SQLException {
          if (tokens == null || tokens.isEmpty()) {
              throw new SQLException("Error occurred while extracting input parameter names");
          }
          String tmp = tokens.poll();
          if (Constants.COLUMN.equals(tmp)) {
              tmp = tokens.poll();
              if (tmp != null && !SQLParserUtil.isKeyword(tmp)) {
                  ParamInfo param = new ParamInfo(paramCount, tmp);
                  this.getInputParams().add(this.getParamCount(), param);
                  paramCount++;

                  if (Constants.COLUMN.equals(tokens.peek())) {
                      this.processInputParamNames(tokens);
                  }
              }
          }
      }

      private void processInputParamValues(Queue<String> tokens) throws SQLException {
          if (tokens == null || tokens.isEmpty()) {
              throw new SQLException("Error occurred while extracting input parameter names");
          }
          String tmp = tokens.poll();
          if (Constants.OP_VALUE.equals(tmp)) {
              tmp = tokens.poll();
              if ("?".equals(tmp)) {
                  if (!isParameterized) {
                      throw new SQLException("Values have to be either inline or parameterized but not both");
                  }
                  isParameterized = true;
                  getParamValues().put(valueCount, tmp);
                  valueCount++;
                  if (Constants.OP_VALUE.equals(tokens.peek())) {
                      processInputParamValues(tokens);
                  }
              } else if (Constants.SINGLE_QUOTATION.equals(tmp)) {
                  if (isParameterized) {
                      throw new SQLException("Values have to be either inline or parameterized but not both");
                  }
                  isParameterized = false;
                  tmp = tokens.poll();
                  getParamValues().put(valueCount, tmp);
                  valueCount++;
                  tmp = tokens.poll();
                  if (Constants.SINGLE_QUOTATION.equals(tmp)) {
                      processInputParamValues(tokens);
                  }
              } else {
                  if (isParameterized) {
                      throw new SQLException("Values have to be either inline or parameterized but not both");
                  }
                  isParameterized = false;
                  this.getParamValues().put(valueCount, tmp);
                  valueCount++;
                  processInputParamValues(tokens);
              }
          }
      }

      public String getTargetTable() {
          return targetTable;
      }

      public List<ParamInfo> getInputParams() {
          return inputParams;
      }

      public Map<Integer, String> getParamValues() {
          return paramValues;
      }

      public boolean isAll() {
          return isAll;
      }

      public boolean isParameterized() {
          return isParameterized;
      }

      public int getParamCount() {
          return paramCount;
      }

      public int getValueCount() {
          return valueCount;
      }

}
