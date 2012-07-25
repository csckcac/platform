/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

package org.wso2.carbon.identity.oauth2.dao;

public class SQLQueries {
    public static final String STORE_AUTHORIZATION_CODE = "INSERT INTO " +
            "IDN_OAUTH2_AUTHORIZATION_CODE " +
            "(AUTHORIZATION_CODE, CONSUMER_KEY, SCOPE, AUTHZ_USER, TIME_CREATED, VALIDITY_PERIOD) " +
            "VALUES (?,?,?,?,?,?)";

    public static final String VALIDATE_AUTHZ_CODE = "SELECT AUTHZ_USER, SCOPE, " +
            "TIME_CREATED, VALIDITY_PERIOD " +
            "FROM IDN_OAUTH2_AUTHORIZATION_CODE " +
            "where CONSUMER_KEY = ? " +
            "AND AUTHORIZATION_CODE = ?";

    public static final String STORE_ACCESS_TOKEN = "INSERT INTO " +
            "IDN_OAUTH2_ACCESS_TOKEN " +
            "(ACCESS_TOKEN, REFRESH_TOKEN, CONSUMER_KEY, AUTHZ_USER, TIME_CREATED, " +
            "VALIDITY_PERIOD, TOKEN_SCOPE, TOKEN_STATE) " +
            "VALUES (?,?,?,?,?,?,?,?)";

    public static final String REMOVE_AUTHZ_CODE = "DELETE " +
            "FROM IDN_OAUTH2_AUTHORIZATION_CODE " +
            "WHERE AUTHORIZATION_CODE = ?";

    public static final String VALIDATE_BEARER_TOKEN = "SELECT AUTHZ_USER, " +
            "TOKEN_SCOPE, TIME_CREATED,  VALIDITY_PERIOD FROM IDN_OAUTH2_ACCESS_TOKEN " +
            "WHERE ACCESS_TOKEN = ?";

    public static final String VALIDATE_REFRESH_TOKEN = "SELECT ACCESS_TOKEN, AUTHZ_USER, " +
            "TOKEN_SCOPE FROM IDN_OAUTH2_ACCESS_TOKEN " +
            "WHERE CONSUMER_KEY = ? AND REFRESH_TOKEN = ?";

    public static final String REMOVE_ACCESS_TOKEN = "DELETE FROM " +
            "IDN_OAUTH2_ACCESS_TOKEN WHERE ACCESS_TOKEN = ? ";
}
