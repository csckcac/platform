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
package org.wso2.carbon.dataservices.sql.driver.processor;

import org.wso2.carbon.dataservices.sql.driver.parser.AnalyzerException;
import org.wso2.carbon.dataservices.sql.driver.parser.Constants;
import org.wso2.carbon.dataservices.sql.driver.query.TQuery;

public class DataProcessorFactory {

    public static DataProcessor createDataProcessor(String type,
                                                    TQuery query) throws AnalyzerException {
         type = type.toUpperCase();
        if (Constants.EXCEL.equals(type))  {
            return new ExcelDataProcessor(query);
        } else {
            throw new AnalyzerException("Unsupported config type");
        }
    }

}
