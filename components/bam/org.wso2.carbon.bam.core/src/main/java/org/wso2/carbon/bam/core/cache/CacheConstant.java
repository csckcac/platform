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
package org.wso2.carbon.bam.core.cache;


public class CacheConstant {

    public static final String CACHE_SEPARATOR = "_";

    public static final long DEFAULT_CACHING_REMOVAL_DELAY = 25 * 60 * 1000; //25 minute
    public static final long DEFAULT_CACHING_REMOVAL_INTERVAL = 20 * 60 * 1000; //20 minute

    public static final String BAM_CACHING_THREAD = "BAM-Caching-Thread";
}
