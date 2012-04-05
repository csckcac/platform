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
package org.wso2.carbon.apimgt.usage.publisher;

import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;

public final class APIMgtUsagePublisherConstants {

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static String _OAUTH_HEADERS_SPLITTER = ",";
    public static String _OAUTH_CONSUMER_KEY = "Bearer";
    public static  String HEADER_SEGMENT_DELIMETER = " ";


    public  static final String CONFIG_PATH = CarbonUtils.getCarbonHome() + File.separator +
                                                 "repository" +File.separator +"conf" +File.separator + "amConfig.xml";

}
