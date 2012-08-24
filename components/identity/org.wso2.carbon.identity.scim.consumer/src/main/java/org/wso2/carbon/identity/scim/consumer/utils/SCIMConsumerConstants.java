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
package org.wso2.carbon.identity.scim.consumer.utils;

public class SCIMConsumerConstants {
    //constants used to extract info from create user soap envelope
    public static final String USER_MGT_NS = "http://mgt.user.carbon.wso2.org";
    public static final String USER_NAME_ELEMENT_NAME = "userName";
    public static final String PASSWORD_ELEMENT_NAME = "password";
    public static final String TENANT_DOMAIN_ELEMENT_NAME = "tenantDomain";

    //constants used to extract info from create role soap envelope
    public static final String ROLE_NAME_ELEMENT_NAME = "roleName";
    public static final String USER_LIST_ELEMENT_NAME = "userList";
}
