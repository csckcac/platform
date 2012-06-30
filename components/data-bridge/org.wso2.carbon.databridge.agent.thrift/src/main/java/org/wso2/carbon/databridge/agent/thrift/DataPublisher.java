/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.databridge.agent.thrift;

import com.google.gson.Gson;
import org.wso2.carbon.databridge.agent.thrift.conf.DataPublisherConfiguration;
import org.wso2.carbon.databridge.agent.thrift.conf.ReceiverConfiguration;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.commons.exception.TransportException;
import org.wso2.carbon.databridge.agent.thrift.internal.EventQueue;
import org.wso2.carbon.databridge.agent.thrift.internal.publisher.client.EventPublisher;
import org.wso2.carbon.databridge.agent.thrift.internal.publisher.client.EventPublisherFactory;
import org.wso2.carbon.databridge.agent.thrift.internal.utils.AgentServerURL;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.AuthenticationException;
import org.wso2.carbon.databridge.commons.exception.DifferentStreamDefinitionAlreadyDefinedException;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.databridge.commons.exception.NoStreamDefinitionExistException;
import org.wso2.carbon.databridge.commons.exception.StreamDefinitionException;
import org.wso2.carbon.databridge.commons.thrift.utils.CommonThriftConstants;

import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Publisher will maintain a single connection to a server,
 * and define/find event streams and send events.
 */
public class DataPublisher {

//    private static Log log = LogFactory.getLog(DataPublisher.class);

    private Agent agent;
    private DataPublisherConfiguration dataPublisherConfiguration;
    private EventPublisher eventPublisher;
    private EventQueue<Event> eventQueue;
    private Gson gson = new Gson();

    private ThreadPoolExecutor threadPool;

    /**
     * To create the Data Publisher and the respective agent to publish events
     * For secure data transfer the Authenticator URL will be the same as the Receiver URL and
     * for normal data transfer the Authenticator ip will be the same as receiver ip but its port will be 100+<receiver port>
     *
     * @param receiverUrl the event receiver url
     *                    use tcp://<HOST>:<PORT> for normal data transfer and
     *                    use ssl://<HOST>:<PORT> for secure data transfer
     * @param userName    user name
     * @param password    password
     * @throws java.net.MalformedURLException
     * @throws org.wso2.carbon.databridge.agent.thrift.exception.AgentException
     * @throws org.wso2.carbon.databridge.commons.exception.AuthenticationException
     * @throws org.wso2.carbon.databridge.commons.exception.TransportException
     */
    public DataPublisher(String receiverUrl, String userName, String password)
            throws MalformedURLException, AgentException, AuthenticationException,
                   TransportException {
        this(receiverUrl, userName, password, new Agent());
    }

    /**
     * To create the Data Publisher and the respective agent to publish events
     *
     * @param authenticationUrl the secure authentication url, use ssl://<HOST>:<PORT>
     * @param receiverUrl       the event receiver url
     *                          use tcp://<HOST>:<PORT> for normal data transfer and
     *                          use ssl://<HOST>:<PORT> for secure data transfer
     * @param userName          user name
     * @param password          password
     * @throws java.net.MalformedURLException
     * @throws org.wso2.carbon.databridge.agent.thrift.exception.AgentException
     * @throws org.wso2.carbon.databridge.commons.exception.AuthenticationException
     * @throws org.wso2.carbon.databridge.commons.exception.TransportException
     */
    public DataPublisher(String authenticationUrl, String receiverUrl, String userName,
                         String password)
            throws MalformedURLException, AgentException, AuthenticationException,
                   TransportException {
        this(authenticationUrl, receiverUrl, userName, password, new Agent());
    }

    /**
     * To create the Data Publisher to publish events
     * For secure data transfer the Authenticator URL will be the same as the Receiver URL and
     * for normal data transfer the Authenticator ip will be the same as receiver ip but its port will be 100+<receiver port>
     *
     * @param receiverUrl the event receiver url
     *                    use tcp://<HOST>:<PORT> for normal data transfer and
     *                    use ssl://<HOST>:<PORT> for secure data transfer
     * @param userName    user name
     * @param password    password
     * @param agent       the underlining agent
     * @throws java.net.MalformedURLException
     * @throws org.wso2.carbon.databridge.agent.thrift.exception.AgentException
     * @throws org.wso2.carbon.databridge.commons.exception.AuthenticationException
     * @throws org.wso2.carbon.databridge.commons.exception.TransportException
     */
    public DataPublisher(String receiverUrl, String userName,
                         String password, Agent agent)
            throws MalformedURLException, AgentException, AuthenticationException,
                   TransportException {
        AgentServerURL receiverURL = new AgentServerURL(receiverUrl);
        if (receiverURL.isSecured()) {
            this.start(new ReceiverConfiguration(userName, password,
                                                 receiverURL.getHost(), receiverURL.getPort(),
                                                 receiverURL.getHost(), receiverURL.getPort(), receiverURL.isSecured()),
                       agent);
        } else {
            this.start(new ReceiverConfiguration(userName, password,
                                                 receiverURL.getHost(), receiverURL.getPort(),
                                                 receiverURL.getHost(), receiverURL.getPort() + CommonThriftConstants.SECURE_EVENT_RECEIVER_PORT_OFFSET, receiverURL.isSecured()),
                       agent);
        }

    }

    /**
     * To create the Data Publisher and the respective agent to publish events
     *
     * @param authenticationUrl the secure authentication url, use ssl://<HOST>:<PORT>
     * @param receiverUrl       the event receiver url
     *                          use tcp://<HOST>:<PORT> for normal data transfer and
     *                          use ssl://<HOST>:<PORT> for secure data transfer
     * @param userName          user name
     * @param password          password
     * @param agent             the underlining agent
     * @throws java.net.MalformedURLException
     * @throws org.wso2.carbon.databridge.agent.thrift.exception.AgentException
     * @throws org.wso2.carbon.databridge.commons.exception.AuthenticationException
     * @throws org.wso2.carbon.databridge.commons.exception.TransportException
     */
    public DataPublisher(String authenticationUrl, String receiverUrl, String userName,
                         String password, Agent agent)
            throws MalformedURLException, AgentException, AuthenticationException,
                   TransportException {
        AgentServerURL authenticationURL = new AgentServerURL(authenticationUrl);
        if (!authenticationURL.isSecured()) {
            throw new MalformedURLException("Authentication url protocol is not ssl, expected = ssl://<HOST>:<PORT> but actual = " + authenticationUrl);
        }
        AgentServerURL receiverURL = new AgentServerURL(receiverUrl);
        this.start(new ReceiverConfiguration(userName, password,
                                             receiverURL.getHost(), receiverURL.getPort(),
                                             authenticationURL.getHost(), authenticationURL.getPort(), receiverURL.isSecured()),
                   agent);

    }

    /**
     * to set the underlining agent
     * which could be sheared by many data publishers
     *
     * @param agent the underlining agent
     * @throws org.wso2.carbon.databridge.agent.thrift.exception.AgentException
     * @throws org.wso2.carbon.databridge.commons.exception.AuthenticationException
     * @throws org.wso2.carbon.databridge.commons.exception.TransportException
     */
    public void setAgent(Agent agent)
            throws AgentException, AuthenticationException, TransportException {
        this.agent.shutdown(this);// to shutdown the old agent
        start(this.dataPublisherConfiguration.getReceiverConfiguration(), agent);
    }

    /**
     * to get the underlining agent
     *
     * @return agent
     */
    public Agent getAgent() {
        return agent;
    }

    private void start(ReceiverConfiguration receiverConfiguration, Agent agent)
            throws AgentException, AuthenticationException, TransportException {
        agent.addDataPublisher(this);
        this.agent = agent;
        this.dataPublisherConfiguration = new DataPublisherConfiguration(receiverConfiguration);
        this.eventQueue = new EventQueue<Event>();
        this.threadPool = agent.getThreadPool();
        if (receiverConfiguration.isDataTransferSecured()) {
            this.eventPublisher = EventPublisherFactory.getEventPublisher(dataPublisherConfiguration, eventQueue, agent, agent.getSecureTransportPool());
        } else {
            this.eventPublisher = EventPublisherFactory.getEventPublisher(dataPublisherConfiguration, eventQueue, agent, agent.getTransportPool());
        }
        //Connect to the server
        dataPublisherConfiguration.setSessionId(agent.getAgentAuthenticator().connect(
                dataPublisherConfiguration));
    }

    /**
     * Defining stream on which events will be published by this DataPublisher
     *
     * @param streamDefinition on json format
     * @return the stream id
     * @throws org.wso2.carbon.databridge.agent.thrift.exception.AgentException
     * @throws org.wso2.carbon.databridge.commons.exception.DifferentStreamDefinitionAlreadyDefinedException
     *
     * @throws org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException
     *
     * @throws org.wso2.carbon.databridge.commons.exception.StreamDefinitionException
     */
    public String defineStream(String streamDefinition)
            throws AgentException, MalformedStreamDefinitionException, StreamDefinitionException,
                   DifferentStreamDefinitionAlreadyDefinedException {
        String sessionId = dataPublisherConfiguration.getSessionId();
        return eventPublisher.defineStream(sessionId, streamDefinition);
    }

    /**
     * Defining stream on which events will be published by this DataPublisher
     *
     * @param streamDefinition on json format
     * @return the stream id
     * @throws org.wso2.carbon.databridge.agent.thrift.exception.AgentException
     * @throws org.wso2.carbon.databridge.commons.exception.DifferentStreamDefinitionAlreadyDefinedException
     *
     * @throws org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException
     *
     * @throws org.wso2.carbon.databridge.commons.exception.StreamDefinitionException
     */
    public String defineStream(StreamDefinition streamDefinition)
            throws AgentException, MalformedStreamDefinitionException, StreamDefinitionException,
                   DifferentStreamDefinitionAlreadyDefinedException {
        String sessionId = dataPublisherConfiguration.getSessionId();
        String streamId = eventPublisher.defineStream(sessionId, gson.toJson(streamDefinition));
        try {
            Field field = StreamDefinition.class.getDeclaredField("streamId");
            field.setAccessible(true);
            field.set(streamDefinition, streamId);
        } catch (NoSuchFieldException e) {

        } catch (IllegalAccessException e) {

        }
        return streamId;
    }

    /**
     * Finding already existing stream's Id to publish data
     *
     * @param name    the stream name
     * @param version the version of the stream
     * @return stream id
     * @throws org.wso2.carbon.databridge.agent.thrift.exception.AgentException
     *          if client cannot publish the type definition
     */
    public String findStream(String name, String version)
            throws AgentException, StreamDefinitionException, NoStreamDefinitionExistException {
        String sessionId = dataPublisherConfiguration.getSessionId();
        return eventPublisher.findStreamId(sessionId, name, version);
    }


    /**
     * Publishing the events to the server
     *
     * @param event the event to be published
     * @throws org.wso2.carbon.databridge.agent.thrift.exception.AgentException
     *          if client cannot publish the event
     */
    public void publish(Event event) throws AgentException {
        try {
            agent.getQueueSemaphore().acquire();//control the total number of buffered events

            //Adds event to the queue and checks whether its scheduled for event dispatching
            if (!eventQueue.put(event)) {
                try {
                    //if not schedule for event dispatching
                    threadPool.submit(eventPublisher);
                } catch (RejectedExecutionException ignored) {
                }
            }
        } catch (InterruptedException e) {
            throw new AgentException("Cannot add " + event + " to event queue", e);
        }
    }

    /**
     * Publishing events to the server
     *
     * @param streamId             of the stream on which the events are published
     * @param metaDataArray        metadata array of the event
     * @param correlationDataArray correlation data array of the event
     * @param payloadDataArray     payload data array of the event
     * @throws org.wso2.carbon.databridge.agent.thrift.exception.AgentException
     */
    public void publish(String streamId, Object[] metaDataArray, Object[] correlationDataArray,
                        Object[] payloadDataArray)
            throws AgentException {
        publish(new Event(streamId, System.currentTimeMillis(), metaDataArray, correlationDataArray, payloadDataArray));
    }

    /**
     * Publishing events to the server
     *
     * @param streamId             of the stream on which the events are published
     * @param timeStamp            time stamp of the event
     * @param metaDataArray        metadata array of the event
     * @param correlationDataArray correlation data array of the event
     * @param payloadDataArray     payload data array of the event
     * @throws org.wso2.carbon.databridge.agent.thrift.exception.AgentException
     */
    public void publish(String streamId, long timeStamp, Object[] metaDataArray,
                        Object[] correlationDataArray, Object[] payloadDataArray)
            throws AgentException {
        publish(new Event(streamId, timeStamp, metaDataArray, correlationDataArray, payloadDataArray));
    }

    /**
     * Disconnecting from the server
     */
    public void stop() {
        agent.getAgentAuthenticator().disconnect(dataPublisherConfiguration);
        agent.shutdown(this);
    }


}
