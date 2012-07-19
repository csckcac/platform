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
package org.wso2.carbon.rssmanager.core.internal.manager;

import org.wso2.carbon.rssmanager.core.RSSManagerException;

public class RSSManagerFactory {

    public enum Types {
        MYSQL, ORACLE
    }

    public static RSSManager getRSSManager(String type) throws RSSManagerException {
        Types t = Types.valueOf(type.toUpperCase());
        switch (t) {
            case MYSQL:
                return MySQLRSSManager.getMySQLRSSManager();
            case ORACLE:
                return OracleRSSManager.getRSSManager();
            default:
                throw new RSSManagerException("Unsupported database server type");
        }
    }

}
