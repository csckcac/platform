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
 * Generate a Message id based on the time message added , node local message id and node id
 * To make it unique across cluster
 *
 * message id = time_stamp + local_message_id + node_id;
 *
 * Here + denotes the string append operation and time stamp is taken from time in milliseconds from a configured
 * reference time.
 */
public class TimeStampBasedMessageIdGenerator implements MessageIdGenerator{



    @Override
    public long getNextId() {
        long ts = System.currentTimeMillis();
        CassandraMessageStore ms = ClusterResourceHolder.getInstance().getCassandraMessageStore();
        StringBuffer midStr = new StringBuffer();

        ts = ClusterResourceHolder.getInstance().getReferenceTime().getTime(ts);

        midStr.append(ts).append(ms.currentMessageId().incrementAndGet()).
                append(ClusterResourceHolder.getInstance().getClusterManager().getNodeId());
        return Long.parseLong(midStr.toString());
    }
}
