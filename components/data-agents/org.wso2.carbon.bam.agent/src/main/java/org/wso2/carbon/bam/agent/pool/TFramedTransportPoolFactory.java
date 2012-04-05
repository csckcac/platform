/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.bam.agent.pool;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool.BaseKeyedPoolableObjectFactory;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.wso2.carbon.bam.data.publisher.util.BAMDataPublisherConstants;

public class TFramedTransportPoolFactory extends BaseKeyedPoolableObjectFactory {

    private static final Log log = LogFactory.getLog(TFramedTransportPoolFactory.class);

    @Override
    public TTransport makeObject(Object key) throws Exception {
        String[] hostNameAndPort = key.toString().split(BAMDataPublisherConstants.HOSTNAME_AND_PORT_SEPARATOR);

        TTransport receiverTransport = new TFramedTransport(new TSocket(hostNameAndPort[0],
                                                                        Integer.parseInt(hostNameAndPort[1])));
        receiverTransport.open();
        log.info("Made connection for key : " + key);
        return receiverTransport;
    }

    @Override
    public boolean validateObject(Object key, Object obj) {
        TTransport receiverTransport= (TTransport)obj;
        boolean isOpen = receiverTransport.isOpen();
        return isOpen;
    }


}
