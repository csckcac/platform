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

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMDocument;
import org.apache.axiom.soap.*;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.transport.TransportUtils;
import org.apache.axis2.transport.base.threads.WorkerPool;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HTTPTransportUtils;
import org.apache.axis2.transport.http.util.RESTUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.TException;
import org.wso2.carbon.cloud.csg.agent.*;
import org.wso2.carbon.cloud.csg.agent.heartbeat.CSGAgentHeartBeatTask;
import org.wso2.carbon.cloud.csg.agent.heartbeat.CSGAgentHeartBeatTaskList;
import org.wso2.carbon.cloud.csg.agent.observer.CSGAgentObserver;
import org.wso2.carbon.cloud.csg.agent.observer.CSGAgentObserverImpl;
import org.wso2.carbon.cloud.csg.agent.observer.CSGAgentSubject;
import org.wso2.carbon.cloud.csg.common.CSGConstant;
import org.wso2.carbon.cloud.csg.common.CSGUtils;
import org.wso2.carbon.cloud.csg.common.thrift.CSGThriftClient;
import org.wso2.carbon.cloud.csg.common.thrift.gen.Message;
import org.wso2.carbon.cloud.csg.common.thrift.gen.NotAuthorizedException;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * The polling task implementation for transport receiver, there will be a task per deployed
 * service
 */
public class CSGPollingTransportTaskManager {

    private static final Log log = LogFactory.getLog(CSGPollingTransportTaskManager.class);

    public enum STATE {STOPPED, STARTED, FAILURE}

    private int concurrentClients = 1;

    private String serviceName;

    private CSGPollingTransportEndpoint endpoint;

    private WorkerPool workerPool = null;

    private CSGAgentSubject subject;

    /**
     * The token for secure communication
     */
    private String token;

    /**
     * The size of a request message block.i.e. thirft client will request a message block of size
     * requestBlockSize from the thrift server
     */
    private int requestBlockSize;

    /**
     * The size of the response message block size that the thrift client should send server. i.e.
     * thrift client will send a response message block of size responseBlockSize to the thirft
     * server
     */
    private int responseBlockSize;

    /**
     * The thirft server host name
     */
    private String hostName;

    /**
     * The thirft server port the client should connect to
     */
    private int port;

    /**
     * The client timeout when connecting to thrift server
     */
    private int timeout;

    private String trustStoreLocation;

    private String trustStorePassWord;


    /**
     * Initial duration to suspend the polling tasks
     */
    private int initialReconnectDuration = 10000;

    /**
     * progression factor for the heart beat task
     */
    private double reconnectionProgressionFactor = 2.0;

    /**
     * Response message processing block size
     */
    private int messageProcessingBlockSize;

    /**
     * The list of active polling tasks managed by this instance
     */
    private final List<MessageExchangeTask> pollingTasks =
            Collections.synchronizedList(new ArrayList<MessageExchangeTask>());

    /**
     * The number of worker thread per task for processing
     */
    private int noOfDispatchingTask = 2;


    public void setConcurrentClients(int concurrentClients) {
        this.concurrentClients = concurrentClients;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setInitialReconnectDuration(int initialReconnectDuration) {
        this.initialReconnectDuration = initialReconnectDuration;
    }

    public void setReconnectionProgressionFactor(double reconnectionProgressionFactor) {
        this.reconnectionProgressionFactor = reconnectionProgressionFactor;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public void setEndpoint(CSGPollingTransportEndpoint endpoint) {
        this.endpoint = endpoint;
    }


    public void setWorkerPool(WorkerPool workerPool) {
        this.workerPool = workerPool;
    }

    public void setRequestBlockSize(int requestBlockSize) {
        this.requestBlockSize = requestBlockSize;
    }

    public void setResponseBlockSize(int responseBlockSize) {
        this.responseBlockSize = responseBlockSize;
    }


    public void setToken(String token) {
        this.token = token;
    }

    public void setTrustStoreLocation(String trustStoreLocation) {
        this.trustStoreLocation = trustStoreLocation;
    }

    public void setTrustStorePassWord(String trustStorePassWord) {
        this.trustStorePassWord = trustStorePassWord;
    }

    public void setNoOfDispatchingTask(int noOfDispatchingTask) {
        this.noOfDispatchingTask = noOfDispatchingTask;
    }

    public void setSubject(CSGAgentSubject subject) {
        this.subject = subject;
    }

    public void setMessageProcessingBlockSize(int messageProcessingBlockSize) {
        this.messageProcessingBlockSize = messageProcessingBlockSize;
    }

    public synchronized void start(CSGPollingTransportBuffers buffers) {
        // start the worker task for message dispatching from transport queue to actual
        // processing pool
        for (int i = 0; i < noOfDispatchingTask; i++) {
            workerPool.execute(new MessageDispatchTask(buffers));
        }

        // start receiving the messages on different N clients
        for (int i = 0; i < concurrentClients; i++) {
            CSGThriftClient client = new CSGThriftClient(
                    CSGUtils.getCSGThriftClient(
                            hostName,
                            port,
                            timeout,
                            trustStoreLocation,
                            trustStorePassWord));
            workerPool.execute(new MessageExchangeTask(
                    client,
                    requestBlockSize,
                    responseBlockSize,
                    buffers));
        }
    }

    public synchronized void stop() {
        synchronized (pollingTasks) {
            for (MessageExchangeTask exchangeTask : pollingTasks) {
                exchangeTask.requestShutDown();
            }
        }
        log.info("Task manager for service '" + serviceName + "', shutdown");
    }

    /**
     * A periodic task to poll remote Thrift server buffers and submitting messages for processing
     */
    private final class MessageExchangeTask implements Runnable {

        private CSGThriftClient client;

        private volatile STATE workerState = STATE.STOPPED;

        private int responseBlockSize;

        private int requestBlockSize;

        private CSGPollingTransportBuffers buffers;

        private MessageExchangeTask(CSGThriftClient client,
                                    int requestBlockSize,
                                    int responseBlockSize,
                                    CSGPollingTransportBuffers buffers) {
            this.client = client;
            this.requestBlockSize = requestBlockSize;
            this.responseBlockSize = responseBlockSize;
            this.buffers = buffers;

            // add the created task to the task store
            synchronized (pollingTasks) {
                pollingTasks.add(this);
            }
        }

        public void run() {

            workerState = STATE.STARTED;
            //if this service failed earlier make sure we start from fresh
            String taskKey = hostName + ":" + port;
            if (CSGAgentHeartBeatTaskList.isScheduledHeartBeatTaskAvailable(taskKey)) {
                CSGAgentHeartBeatTaskList.removeScheduledHeartBeatTask(taskKey);
            }

            List<Message> requestMsgList;

            // the busy loop which polls the thrift server for messages
            try {
                while (workerState == STATE.STARTED &&
                        !CSGAgentPollingTaskFlags.isFlaggedForShutDown(serviceName)) {
                    try {
                        // submit the transport response buffer to server also process any requests
                        requestMsgList = client.exchange(
                                buffers.getResponseMessageList(responseBlockSize),
                                requestBlockSize,
                                token);
                        if (requestMsgList != null && requestMsgList.size() > 0) {
                            buffers.getRequestMessageBuffer().addAll(requestMsgList);
                        }
                    } catch (TException e) {
                        log.error("Polling Task Manager encountered an error..", e);
                        // should be a connection error with the remote server
                        // schedule a heart beat task and end this loop
                        registerObserver(hostName, serviceName, port);
                        scheduleHeartBeatTaskIfRequired(hostName, port);
                        return;
                    } catch (NotAuthorizedException e) {
                        // just logged the error and re-try in the next attempt
                        if (log.isDebugEnabled()) {
                            log.debug(e);
                        }
                    } catch (AxisFault e) {
                        // just log and re-try in the next attempt
                        if (log.isDebugEnabled()) {
                            log.debug(e);
                        }
                    }
                }
            } finally {
                workerState = STATE.STOPPED;
                synchronized (pollingTasks) {
                    pollingTasks.remove(this);
                }
            }
        }

        protected void requestShutDown() {
            workerState = STATE.STOPPED;
        }

        private void registerObserver(String hostName, String serviceName, int port) {
            CSGAgentObserver o = new CSGAgentObserverImpl(hostName, serviceName, port);
            subject.addObserver(o);
        }

        private void scheduleHeartBeatTaskIfRequired(String host, int port) {
            // scheduled a heat beat task for this host, if not already done
            String heartBeatTaskKey = host + ":" + port;
            if (!CSGAgentHeartBeatTaskList.isScheduledHeartBeatTaskAvailable(heartBeatTaskKey)) {
                CSGAgentHeartBeatTaskList.addScheduledHeartBeatTask(heartBeatTaskKey);
                workerPool.execute(new CSGAgentHeartBeatTask(
                        subject,
                        reconnectionProgressionFactor,
                        initialReconnectDuration,
                        host,
                        port));
            }
        }
    }

    /**
     * The message dispatch task which dispatch messages from the source buffers to actual
     * processing logic
     */
    private final class MessageDispatchTask implements Runnable {
        private CSGPollingTransportBuffers buffers;

        private MessageDispatchTask(CSGPollingTransportBuffers buffers) {
            this.buffers = buffers;
        }

        public void run() {
            while (true) {
                Message msg = buffers.getRequestMessage();
                if (msg != null) {
                    workerPool.execute(new MessageProcessingTask(msg, buffers));
                }
            }
        }
    }

    /**
     * Process any request messages
     */
    private final class MessageProcessingTask implements Runnable {
        private Message message;
        private boolean isSOAP11;
        private CSGPollingTransportBuffers buffers;

        private MessageProcessingTask(Message message, CSGPollingTransportBuffers buffers) {
            this.message = message;
            this.buffers = buffers;
        }

        public void run() {
            try {
                handleIncomingMessage(message);
            } catch (AxisFault axisFault) {
                // there has been a fault while trying to execute the back end service
                // send that fault to the client
                try {
                    handleFaultMessage(message, buffers, axisFault);
                } catch (Exception e) {
                    // do not let the task die!
                    log.error("Error while sending the fault message to the client. Client will not" +
                            " receive any errors!", e);
                }
            }
        }

        private void handleIncomingMessage(Message message) throws AxisFault {
            if (message == null) {
                log.warn("A null Message received!");
            } else {
                try {
                    MessageContext msgContext = endpoint.createMessageContext();
                    String msgId = message.getMessageId();
                    msgContext.setMessageID(msgId);
                    msgContext.setProperty(CSGConstant.CSG_CORRELATION_KEY, msgId);
                    Map<String, String> trpHeaders = message.getTransportHeaders();
                    String contentType = CSGUtils.getContentType(trpHeaders);

                    HTTPTransportUtils.initializeMessageContext(
                            msgContext,
                            message.getSoapAction(),
                            message.getRequestURI(),
                            contentType);
                    msgContext.setProperty(Constants.OUT_TRANSPORT_INFO,
                            new CSGPollingTransportOutTransportInfo(contentType));

                    if (message.isIsDoingREST()) {

                        msgContext.setAxisService(null);
                        msgContext.setProperty(HTTPConstants.HTTP_METHOD, message.getHttpMethod());
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        RESTUtil.processURLRequest(msgContext, out, contentType);

                    } else {

                        ByteArrayInputStream inputStream = new ByteArrayInputStream(message.getMessage());

                        msgContext.setProperty(Constants.Configuration.CONTENT_TYPE, contentType);
                        msgContext.setProperty(MessageContext.TRANSPORT_HEADERS, trpHeaders);

                        InputStream gzipInputStream =
                                HTTPTransportUtils.handleGZip(msgContext, inputStream);
                        msgContext.setEnvelope(
                                TransportUtils.createSOAPMessage(
                                        msgContext,
                                        gzipInputStream,
                                        contentType));
                        isSOAP11 = msgContext.isSOAP11();

                        AxisEngine.receive(msgContext);
                    }
                } catch (XMLStreamException e) {
                    throw new AxisFault(e.getMessage(), e);
                } catch (IOException e) {
                    throw new AxisFault(e.getMessage(), e);
                }
            }
        }

        private void handleFaultMessage(Message originalMsg,
                                        CSGPollingTransportBuffers buffers,
                                        AxisFault axisFault) throws Exception {
            Message thriftMsg = new Message();
            thriftMsg.setMessageId(originalMsg.getMessageId());

            SOAPFactory factory = (isSOAP11 ?
                    OMAbstractFactory.getSOAP11Factory() : OMAbstractFactory.getSOAP12Factory());
            OMDocument soapFaultDocument = factory.createOMDocument();
            SOAPEnvelope faultEnvelope = factory.getDefaultFaultEnvelope();
            soapFaultDocument.addChild(faultEnvelope);

            // create the fault element  if it is need
            SOAPFault fault = faultEnvelope.getBody().getFault();
            if (fault == null) {
                fault = factory.createSOAPFault();
            }
            SOAPFaultCode code = factory.createSOAPFaultCode();
            code.setText(axisFault.getMessage());
            fault.setCode(code);

            SOAPFaultReason reason = factory.createSOAPFaultReason();
            reason.setText(axisFault.getMessage());
            fault.setReason(reason);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            faultEnvelope.serialize(out);
            thriftMsg.setMessage(out.toByteArray());
            buffers.addResponseMessage(thriftMsg);
        }
    }
}
