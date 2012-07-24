/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.analytics.hive.web;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hive.service.Utils;
import org.wso2.carbon.analytics.hive.ServiceHolder;
import org.wso2.carbon.analytics.hive.dto.QueryResult;
import org.wso2.carbon.analytics.hive.exception.HiveExecutionException;
import org.wso2.carbon.analytics.hive.impl.HiveExecutorServiceImpl;
import org.wso2.carbon.analytics.hive.service.HiveExecutorService;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HiveExecutionService {

    public boolean setConnectionParameters(String driver, String url, String username,
                                           String password) throws HiveExecutionException {
        return ServiceHolder.getHiveExecutorService().setConnectionParameters(driver, url, username, password);
    }

    public QueryResult[] executeHiveScript(String script) throws HiveExecutionException {

        HiveExecutorService service = ServiceHolder.getHiveExecutorService();
        return service.execute(script);
        
    }

}
