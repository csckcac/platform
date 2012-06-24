/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.bam.service.data.publisher.util;


import org.wso2.carbon.core.RegistryResources;

public class ActivityPublisherConstants {

    public static final String BAM_ACTIVITY_ID_HEADER_NAMESPACE_URI = "http://wso2.org/ns/2010/10/bam";

    public static final String ACTIVITY_ID = "activityID";
    public static final String ACTIVITY_ID_HEADER_BLOCK_NAME = "BAMEvent";
    public static final String ACTIVITY_DATA_MESSAGE_DIRECTION_IN = "Request";
    public static final String ACTIVITY_DATA_MESSAGE_DIRECTION_OUT = "Response";

    public static final String ENABLE_ACTIVITY = "EnableActivity";



    public static final String ACTIVITY_REG_PATH = RegistryResources.COMPONENTS
                                                   + "org.wso2.carbon.bam.service.data.publisher/activity/";


}
