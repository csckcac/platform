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
package org.wso2.carbon.rest.api.ui.client.template;

import org.apache.axiom.om.OMElement;

public interface APITemplateBuilder {

    public static final String KEY_FOR_API_NAME = "key_for_api_name";
    public static final String KEY_FOR_API_CONTEXT = "key_for_api_context";
    public static final String KEY_FOR_API_VERSION = "key_for_api_version";

    public static final String KEY_FOR_RESOURCE_URI_TEMPLATE = "key_for_resource_uri_template";
    public static final String KEY_FOR_RESOURCE_METHODS = "key_for_resource_methods";
    public static final String KEY_FOR_RESOURCE_URI = "key_for_resource_uri";

    public static final String KEY_FOR_HANDLER = "key_for_handler_class";
    public static final String KEY_FOR_HANDLER_POLICY_KEY = "key_for_handler_policy";

    public String getConfigStringForTemplate();

    public OMElement getConfigXMLForTemplate();

    public String getAPIName();

    public String getAPIContext();


}
