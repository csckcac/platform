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
package org.wso2.andes.server.cluster.coordination;

import org.wso2.andes.server.ClusterResourceHolder;
import org.wso2.andes.server.store.CassandraMessageStore;


/**
 * Generate Message ids based on the TimeStamp.
 *
 * Here to preserve the long range we use a time stamp that is created by getting difference between
 * System.currentTimeMillis() and a configured reference time. Reference time can be configured.
 *
 * Message Id will created by appending time stamp , two digit node id and two digit sequence number
 *
 * <time stamp> + <node id> + <seq number>
 *
 * sequence number is used in a scenario when two or more messages comes with same timestamp
 * (within the same millisecond). So to allow message rages higher than 1000 msg/s we use this sequence number
 * where it will be incremented in case of message comes in same millisecond within the same node. With this approach
 * We can go up to 100,000 msg/s
 */
public class TimeStampBasedMessageIdGenerator implements MessageIdGenerator{



    private volatile long lastCurrentTime;

    private volatile long lastMessageId;

    @Override
    public long getNextId() {
        long ts = ClusterResourceHolder.getInstance().getReferenceTime().getCurrentTime();
        CassandraMessageStore ms = ClusterResourceHolder.getInstance().getCassandraMessageStore();

        if(lastCurrentTime == ts) {
            synchronized (this) {
                if(lastCurrentTime == ts) {
                    lastMessageId = ms.currentMessageId().incrementAndGet();
                    return lastMessageId;
                } else {
                    String id = "" + ts +
                            getTwoDigitNodeId(ClusterResourceHolder.getInstance().getClusterManager().getNodeId())
                            + "00";
                    long mid = Long.parseLong(id);
                    lastMessageId = mid;
                    ms.currentMessageId().set(lastMessageId);
                    lastCurrentTime = ts;
                    return lastMessageId;
                }
            }
        } else {
            String id = "" + ts +
                    getTwoDigitNodeId(ClusterResourceHolder.getInstance().getClusterManager().getNodeId())
                    + "00";
            long mid = Long.parseLong(id);
            synchronized (this) {
                lastCurrentTime = ts;
                lastMessageId = mid;
                ms.currentMessageId().set(lastMessageId);
                return lastMessageId;
            }


        }


    }


    private static String getTwoDigitNodeId(int nodeId) {
        switch (nodeId/10) {
            case 0: { return "0"+nodeId; }
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9: { return ""+nodeId; }
            default : throw new RuntimeException("Node id range exceeded - supported range 0-99");

        }
    }
}
