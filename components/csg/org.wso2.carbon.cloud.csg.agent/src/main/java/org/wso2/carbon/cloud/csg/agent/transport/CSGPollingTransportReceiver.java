/*
 * Copyright WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.cloud.csg.agent.transport;

import org.apache.axis2.AxisFault;
import org.apache.axis2.transport.base.AbstractTransportListenerEx;
import org.apache.axis2.transport.base.threads.WorkerPool;
import org.apache.axis2.transport.base.threads.WorkerPoolFactory;
import org.wso2.carbon.cloud.csg.agent.*;
import org.wso2.carbon.cloud.csg.agent.observer.CSGAgentSubject;
import org.wso2.carbon.cloud.csg.agent.observer.CSGAgentSubjectImpl;
import org.wso2.carbon.cloud.csg.common.CSGConstant;
import org.wso2.carbon.cloud.csg.common.CSGUtils;

/**
 * CSG Polling Transport receiver implementation
 */
public class CSGPollingTransportReceiver
        extends AbstractTransportListenerEx<CSGPollingTransportEndpoint> {

    /**
     * The worker pool for polling tasks
     */
    private WorkerPool csgWorkerPool;

    /**
     * Keep track of any changes in remote server for notifying the observers
     */
    private CSGAgentSubject subject;


    @Override
    protected void doInit() throws AxisFault {
        // create the transport buffers
        getConfigurationContext().setProperty(CSGConstant.CSG_POLLING_TRANSPORT_BUF_KEY,
                new CSGPollingTransportBuffers());
        subject = new CSGAgentSubjectImpl();

        csgWorkerPool = WorkerPoolFactory.getWorkerPool(
                CSGUtils.getIntProperty(
                        CSGConstant.CSG_THRIFT_T_CORE, CSGConstant.WORKERS_CORE_THREADS),
                CSGUtils.getIntProperty(
                        CSGConstant.CSG_THRIFT_T_MAX, CSGConstant.CSG_WORKERS_MAX_THREADS),
                CSGUtils.getIntProperty(
                        CSGConstant.CSG_THRIFT_T_ALIVE, CSGConstant.WORKER_KEEP_ALIVE),
                CSGUtils.getIntProperty(
                        CSGConstant.CSG_THRIFT_T_QLEN, CSGConstant.WORKER_BLOCKING_QUEUE_LENGTH),
                "CSGPollingTransportReceiver-worker-thread-group",
                "CSGPollingTransportReceiver-worker");
        log.info("CSGThrift transport receiver started");
    }

    @Override
    protected CSGPollingTransportEndpoint createEndpoint() {
        return new CSGPollingTransportEndpoint(csgWorkerPool, this);
    }

    @Override
    protected void startEndpoint(CSGPollingTransportEndpoint csgThriftEndpoint) throws AxisFault {
        CSGPollingTransportTaskManager tm = csgThriftEndpoint.getTaskManager();
        // create transport buffer per deployed task so that there is no need to
        // maintain multiple queues for request messages for each task
        CSGPollingTransportBuffers buffers = (CSGPollingTransportBuffers)
                getConfigurationContext().getProperty(CSGConstant.CSG_POLLING_TRANSPORT_BUF_KEY);

        tm.start(buffers);

        log.info("CSGThrift polling task started for service '" + tm.getServiceName() + "'");
    }

    @Override
    protected void stopEndpoint(CSGPollingTransportEndpoint csgThriftEndpoint) {
        CSGPollingTransportTaskManager tm = csgThriftEndpoint.getTaskManager();
        tm.stop();
        log.info("CSGThrift polling task stopped listen for service '" +
                csgThriftEndpoint.getService() + "'");
    }

    public CSGAgentSubject getSubject() {
        return subject;
    }

}
