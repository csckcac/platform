/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.usage.agent.persist;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.usage.agent.beans.BandwidthUsage;
import org.wso2.carbon.usage.agent.exception.UsageException;
import org.wso2.carbon.usage.agent.util.PublisherUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class PersistenceManager {

    private static Log log = LogFactory.getLog(PersistenceManager.class);

    // queue to store Bandwidth usage statistics.
    // usage of  LinkedBlockingQueue ensures operations on the queue to wait for the queue to be non
    // empty when retrieving and wait for space when storing element.
    private Queue<BandwidthUsage> jobQueue = new LinkedBlockingQueue<BandwidthUsage>();


    /**
     * this method add bandwidth usage entries to the jobQueue
     *
     * @param usage Bandwidth usage
     */

    public void addToQueue(BandwidthUsage usage) {
        jobQueue.add(usage);
    }



    /**
     * inner class Summarizer
     * this class is used to accumulate and publish usage statistics.
     * for each tenant this keeps a map to store BandwidthUsage values
     */
    private static class Summarizer {
        private HashMap<String, BandwidthUsage> usageMap;

        public Summarizer() {
            usageMap = new HashMap<String, BandwidthUsage>();
        }

        /**
         * the method to accumulate usage data
         *
         * @param usage BandwidthUsage
         */

        public void accumulate(BandwidthUsage usage) {
            // get the measurement name of usage entry
            String key = usage.getMeasurement();

            // get the existing value of measurement
            BandwidthUsage existingUsage = usageMap.get(key);

            // if this measurement is metered earlier add the new value to the existing value
            if (existingUsage != null) {
                existingUsage.setValue(existingUsage.getValue() + usage.getValue());
            } else {
                // if this measurement is not metered previously we need to add it to the usageMap
                usageMap.put(key, usage);
            }
        }
    }
}

