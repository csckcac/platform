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
import org.apache.axis2.description.AxisService;
import org.apache.axis2.transport.base.threads.WorkerPool;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cloud.csg.common.CSGConstant;
import org.wso2.carbon.cloud.csg.common.CSGUtils;
import org.wso2.carbon.core.util.CryptoException;

/**
 * The factory for {@link CSGPollingTransportTaskManager}
 */
public class CSGPollingTransportTaskManagerFactory {

    private static final Log log = LogFactory.getLog(CSGPollingTransportTaskManagerFactory.class);

    private CSGPollingTransportTaskManagerFactory() {

    }

    public static CSGPollingTransportTaskManager createTaskManagerForService(AxisService service,
                                                                   WorkerPool workerPool,
                                                                   CSGPollingTransportEndpoint endpoint,
                                                                   CSGPollingTransportReceiver
                                                                           receiver)
            throws AxisFault {
        String serviceName = service.getName();

        String encryptedToken = (String) service.getParameterValue(CSGConstant.TOKEN);
        String token;

        try {
            token = CSGUtils.getPlainToken(encryptedToken);
        } catch (CryptoException e) {
            throw new AxisFault(e.getMessage(), e);
        }

        if ("".equals(token)) {
            throw new AxisFault("The secure token is not set for service '" + serviceName + "'");
        }
        CSGPollingTransportTaskManager stm = new CSGPollingTransportTaskManager();
        stm.setToken(token);
        stm.setServiceName(serviceName);
        stm.setWorkerPool(workerPool);
        stm.setConcurrentClients(CSGUtils.getIntProperty(CSGConstant.NO_OF_CONCURRENT_CONSUMERS, 1));
        stm.setSubject(receiver.getSubject());

        // for maximum performance keep the dispatching task busy, i.e. allocate a thread per each
        // CPU( if there are two physical CPUs let the NO_OF_DISPATCH_TASK to be 2)
        stm.setNoOfDispatchingTask(CSGUtils.getIntProperty(CSGConstant.NO_OF_DISPATCH_TASK, 2));
        stm.setEndpoint(endpoint);

        int requestBlockSize = CSGUtils.getIntProperty(
                CSGConstant.MESSAGE_BLOCK_SIZE, 5);
        int responseBlockSize = CSGUtils.getIntProperty(
                CSGConstant.RESPONSE_MESSAGE_BLOCK_SIZE, 1000);
        int messageProcessingBlockSize = CSGUtils.getIntProperty(
                CSGConstant.MESSAGE_PROCESSING_BLOCK_SIZE, CSGConstant.DEFAULT_MESSAGE_PROCESSING_BLOCK_SIZE);
        if (messageProcessingBlockSize > CSGConstant.CSG_WORKERS_MAX_THREADS) {
            // guard against system running out of resources
            log.warn("The message processing block size '" + messageProcessingBlockSize + "' " +
                    "is large than the worker pool size '" + CSGConstant.CSG_WORKERS_MAX_THREADS +
                    "'. All polling tasks and the message processing tasks share the worker pool," +
                    "so the default value for the message processing block '"
                    + CSGConstant.DEFAULT_MESSAGE_PROCESSING_BLOCK_SIZE + "' will be used");
            messageProcessingBlockSize = CSGConstant.DEFAULT_MESSAGE_PROCESSING_BLOCK_SIZE;
        }

        org.wso2.carbon.cloud.csg.stub.types.common.CSGThriftServerBean bean =
                (org.wso2.carbon.cloud.csg.stub.types.common.CSGThriftServerBean)
                        service.getParameterValue(CSGConstant.CSG_SERVER_BEAN);
        if (bean == null) {
            throw new AxisFault("Remote CSG server information is missing");
        }
        String hostName = bean.getHostName();
        int port = bean.getPort();
        int timeout = bean.getTimeOut();

        int initialReconnectionDuration = CSGUtils.getIntProperty(
                CSGConstant.INITIAL_RECONNECT_DURATION, 10000);
        double progressionFactor = CSGUtils.getDoubleProperty(
                CSGConstant.PROGRESSION_FACTOR, 2.0);

        stm.setRequestBlockSize(requestBlockSize);
        stm.setMessageProcessingBlockSize(messageProcessingBlockSize);
        stm.setResponseBlockSize(responseBlockSize);
        stm.setHostName(hostName);
        stm.setPort(port);
        stm.setTimeout(timeout);
        stm.setReconnectionProgressionFactor(progressionFactor);
        stm.setInitialReconnectDuration(initialReconnectionDuration);
        stm.setTrustStoreLocation(CSGUtils.getWSO2TrustStoreFilePath());
        stm.setTrustStorePassWord(CSGUtils.getWSO2TrustStorePassword());

        return stm;
    }
}
