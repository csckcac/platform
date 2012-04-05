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


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.core.util.BAMUtil;

import java.util.Calendar;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;

public class CacheTask extends TimerTask {

    private static final Log log = LogFactory.getLog(CacheTask.class);


    @Override
    public void run() {

        if (log.isDebugEnabled()) {
            log.debug("Cache Cleaned Started..");
        }
        HashMap<String, CacheData> cacheMap = BAMUtil.getBAMCache();
        boolean cacheCleaned = false;
        Thread.currentThread().setName(CacheConstant.BAM_CACHING_THREAD);

        try {
            for (Map.Entry<String, CacheData> entry : cacheMap.entrySet()) {
                CacheData cacheData = entry.getValue();
                if (cacheData != null) {
                    long cachedTime = cacheData.getTimestamp().getTimeInMillis();
                    long currentTime = Calendar.getInstance().getTimeInMillis();
                    long timeDiff = currentTime - cachedTime;
                    if (timeDiff > CacheConstant.DEFAULT_CACHING_REMOVAL_INTERVAL) {
                        cacheMap.remove(entry.getKey());
                        cacheCleaned = true;
                        if (log.isDebugEnabled()) {
                            log.debug("Cached Cleaned for Key: " + entry.getKey());
                        }
                    }
                }
            }
            if (cacheCleaned) {
                log.info("Cache Cleaned..");
            }
        } catch (ConcurrentModificationException e) {

        }

    }
}
