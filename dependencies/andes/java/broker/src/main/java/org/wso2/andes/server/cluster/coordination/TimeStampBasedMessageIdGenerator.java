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

import java.util.concurrent.atomic.AtomicInteger;

import org.wso2.andes.server.ClusterResourceHolder;


/**
 * Generate Message ids based on the TimeStamp.
 * <p/>
 * Here to preserve the long range we use a time stamp that is created by getting difference between
 * System.currentTimeMillis() and a configured reference time. Reference time can be configured.
 * <p/>
 * Message Id will created by appending time stamp , two digit node id and two digit sequence number
 * <p/>
 * <time stamp> + <node id> + <seq number>
 * <p/>
 * sequence number is used in a scenario when two or more messages comes with same timestamp
 * (within the same millisecond). So to allow message rages higher than 1000 msg/s we use this sequence number
 * where it will be incremented in case of message comes in same millisecond within the same node. With this approach
 * We can go up to 100,000 msg/s
 */
public class TimeStampBasedMessageIdGenerator implements MessageIdGenerator {


    private volatile long lastCurrentTime;

    private volatile long lastMessageId;

//    @Override
//    public long getNextId() {
//        long ts = ClusterResourceHolder.getInstance().getReferenceTime().getCurrentTime();
//        CassandraMessageStore ms = ClusterResourceHolder.getInstance().getCassandraMessageStore();
//
//        if (lastCurrentTime == ts) {
//            synchronized (this) {
//                if (lastCurrentTime == ts) {
//                    lastMessageId = ms.currentMessageId().incrementAndGet();
//                    return lastMessageId;
//                } else {
//
//                    StringBuffer buffer = new StringBuffer();
//
//                    buffer.append(ts).
//                            append(getTwoDigitNodeId(ClusterResourceHolder.getInstance().getClusterManager().
//                                    getNodeId())).append("00");
//
//
//                    long mid = Long.parseLong(buffer.toString());
//                    lastMessageId = mid;
//                    ms.currentMessageId().set(lastMessageId);
//                    lastCurrentTime = ts;
//                    return lastMessageId;
//                }
//            }
//        } else {
//            StringBuffer buffer = new StringBuffer();
//
//            buffer.append(ts).
//                    append(getTwoDigitNodeId(ClusterResourceHolder.getInstance().getClusterManager().
//                            getNodeId())).append("00");
//            long mid = Long.parseLong(buffer.toString());
//            synchronized (this) {
//                lastCurrentTime = ts;
//                lastMessageId = mid;
//                ms.currentMessageId().set(lastMessageId);
//                return lastMessageId;
//            }
//
//
//        }
//
//
//    }
//
//
//    private static String getTwoDigitNodeId(int nodeId) {
//        switch (nodeId / 10) {
//            case 0: {
//                return "0" + nodeId;
//            }
//            case 1:
//            case 2:
//            case 3:
//            case 4:
//            case 5:
//            case 6:
//            case 7:
//            case 8:
//            case 9: {
//                return "" + nodeId;
//            }
//            default:
//                throw new RuntimeException("Node id range exceeded - supported range 0-99");
//
//        }
//    }
    
    int nodeID = 0; 
    long lastTimestamp = 0; 
    long lastID = 0;
    private AtomicInteger offsetOnthisslot = new AtomicInteger();
    private long referenaceStart = 41*365*24*60*60*10000; //this is 2011

    /**
     * Out of 64 bits for long, we will use the range as follows
     * [1 sign bit][45bits for time spent from reference time in milliseconds][8bit node id][10 bit offset for ID falls within the same timestamp]
     * This assumes there will not be more than 1024 hits within a given milisecond. Range is sufficient for 6029925857 years.  
     * @return
     */
    
    public synchronized long getNextId() {
        nodeID = ClusterResourceHolder.getInstance().getClusterManager().getNodeId();
        long ts = System.currentTimeMillis();
        int offset = 0; 
        if(ts == lastTimestamp){
            offset = offsetOnthisslot.incrementAndGet();
        }else{
            offsetOnthisslot.set(0);
        }
        lastTimestamp = ts;
        long id = (ts - referenaceStart) * 256* 1024 + nodeID * 1024 + offset;
        if(lastID == id){
            throw new RuntimeException("duplicate ids detected. This should never happen"); 
        }
        lastID = id;
        return id; 
    }
    
    public static void main(String[] args) {
        long bitmask = 0x3FF;
        System.out.println(bitmask & 351974976074826752l);
        //System.out.println( );
    }
    
}
