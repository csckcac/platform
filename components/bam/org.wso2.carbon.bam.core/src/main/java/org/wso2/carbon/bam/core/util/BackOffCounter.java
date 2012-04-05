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
package org.wso2.carbon.bam.core.util;

import org.wso2.carbon.bam.common.dataobjects.service.ServerDO;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Exponential backoff algorithm used to pull data from pull mode servers.
 */
public class BackOffCounter {
    private static int backoffSequence[] = new int[]{0, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192};
    private static int MAX_RETRY_LEVEL = 8; // roughly 2 hours (2^7 minutes)
    private static ConcurrentHashMap<Integer, Integer> retryCountPerServer = new ConcurrentHashMap<Integer, Integer>();
    private static ConcurrentHashMap<Integer, Integer> callCountPerServer = new ConcurrentHashMap<Integer, Integer>();



    public static BackOffCounter getInstance() {
     return new BackOffCounter();
    }

    private void resetCallCount(ServerDO server) {
        callCountPerServer.put(server.getId(), 0);
    }

    private void incrementCallCount(ServerDO server) {
        int count = 0;
        if (callCountPerServer.containsKey(server.getId())) {
            count = callCountPerServer.get(server.getId());
        }

        if (count < MAX_RETRY_LEVEL) {
            callCountPerServer.put(server.getId(), count + 1);
        }
    }

    public void incrementFailCount(ServerDO server) {
        int count = 0;

        if (retryCountPerServer.containsKey(server.getId())) {
            count = retryCountPerServer.get(server.getId());
        }

        if (count < MAX_RETRY_LEVEL) {
            retryCountPerServer.put(server.getId(), count + 1);
        }
    }

    public void resetFailCount(ServerDO server) {
        resetCallCount(server);
        retryCountPerServer.put(server.getId(), 0);
    }

    public boolean shouldBackoff(ServerDO server) {
        int failCount = 0;
        int callCount = 0;

        if (retryCountPerServer.containsKey(server.getId())) {
            failCount = retryCountPerServer.get(server.getId());
        }

        if (callCountPerServer.containsKey(server.getId())) {
            callCount = callCountPerServer.get(server.getId());
        }


        incrementCallCount(server);

        // treat the 0-failure case seperately. (x % 0) is undefined
        if (failCount == 0) return false;

        // if the callCount has been increased the number of steps of this backoff level
        // call should be invoked.
        return callCount % backoffSequence[failCount] != 0;

    }


}
