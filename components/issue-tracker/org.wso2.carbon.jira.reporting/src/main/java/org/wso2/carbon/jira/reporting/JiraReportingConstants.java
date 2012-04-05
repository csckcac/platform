/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.jira.reporting;

/**
 * class containing constants related with JIRA
 */
public class JiraReportingConstants {

    public static final String FILTER_ID = "10010"; // replace filter id with custom filter id
    public static final String JIRA_SOAP_URL = "/rpc/soap/jirasoapservice-v2";
    public static final String BROWSE = "/browse/";
    public static final String JQL_QUERY = "reporter=currentUser()";
    public static final String JQL_QUERY_SEARCH_BY_PROJECT="reporter=currentUser() && project=";



}
