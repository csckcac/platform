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
package org.wso2.carbon.dataservices.sql.driver.processor.reader;

import org.wso2.carbon.dataservices.sql.driver.TExcelConnection;
import org.wso2.carbon.dataservices.sql.driver.parser.AnalyzerException;
import org.wso2.carbon.dataservices.sql.driver.parser.Constants;

public class DataReaderFactory {

    public static DataReader createDataReader(String type,
                                              TExcelConnection con) throws AnalyzerException {
        type = type.toUpperCase();
        if (Constants.EXCEL.equals(type))  {
            return new ExcelDataReader(con);
        } else if (Constants.GSPREAD.equals(type)) {
            return new GSpreadDataReader(con);
        } else if (Constants.CSV.equals(type)) {
            return new CSVDataReader(con);
        } else {
            throw new AnalyzerException("Unsupported config type");
        }
    }

}
