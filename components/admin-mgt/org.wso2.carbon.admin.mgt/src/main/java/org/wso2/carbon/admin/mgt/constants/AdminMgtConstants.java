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
package org.wso2.carbon.admin.mgt.constants;

/*
  Constants related to the admin-mgt
 */
public class AdminMgtConstants {
    public static final String ADMIN_MANAGEMENT_FLAG_PATH =
            "/repository/components/org.wso2.carbon.admin-management-flag";

    public static final String CONFIRMATION_KEY_NOT_MACHING =
            "The credential update confirmation key is not matching.";

    public static final String ILLEGAL_CHARACTERS_FOR_TENANT_DOMAIN =
            ".*[^a-zA-Z0-9\\._\\-].*";
    
    public static final String NO_EMAIL_ADDRESS_SET_ERROR =
            "No email address associated with the given user account";
    
    public static final String EMAIL_CONF_DIRECTORY = "email";
    
    public static final String EMAIL_ADMIN_CONF_FILE = "email-admin-config.xml";
}
