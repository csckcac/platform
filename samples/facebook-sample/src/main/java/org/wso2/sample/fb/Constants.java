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

package org.wso2.sample.fb;

public class Constants {
    // keyspace name
    static final String KEYSPACE_NAME = "Facebook_KS";

    // column family name
    static final String COL_FAMILY_NAME = "Friend_List";

    // number of records to paginate
    static final int PAGINATION_FACTOR = 100;

    // fields representing data for a particular friend
    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String PICTURE = "picture";

    // user id 
    public static final String UID = "uid";

    // cluster configuration
    //public static final String CLUSTER_HOST = "10.100.0.128:9160";
    public static final String CLUSTER_HOST = "127.0.0.1:9160";
    public static final String CLUSTER_NAME = "Test Cluster";
    public static final String VALUE = "value";
}
