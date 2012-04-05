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
package org.wso2.carbon.cloud.csg.transport.server;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TSSLTransportFactory;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransportException;
import org.wso2.carbon.cloud.csg.common.thrift.gen.CSGService;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * The thirft server implementation for csg transport
 */
public class CSGThriftServer {
    private static Log log = LogFactory.getLog(CSGThriftServer.class);

    private CSGThriftServerHandler csgThriftServerHandler;

    public CSGThriftServer(CSGThriftServerHandler handler) {
        this.csgThriftServerHandler = handler;
    }

    /**
     * The server instance
     */
    private TServer server;

    /**
     * Start the thrift server
     *
     * @param hostName         the hostname
     * @param port             thrift server port
     * @param timeOut          the client timeout
     * @param keyStorePath     the path of the key store
     * @param keyStorePassword the password of the key store
     * @param taskName the name of the main server thread
     * @throws AxisFault throws in case of an starting error
     */
    public void start(final String hostName,
                      final int port,
                      final int timeOut,
                      final String keyStorePath,
                      final String keyStorePassword,
                      final String taskName) throws AxisFault {
        try {
            TSSLTransportFactory.TSSLTransportParameters params =
                    new TSSLTransportFactory.TSSLTransportParameters();

            params.setKeyStore(keyStorePath, keyStorePassword);

            TServerSocket socket = TSSLTransportFactory.getServerSocket(
                    port,
                    timeOut,
                    InetAddress.getByName(hostName),
                    params);

            CSGService.Processor<CSGThriftServerHandler> processor =
                    new CSGService.Processor<CSGThriftServerHandler>(csgThriftServerHandler);
            TProtocolFactory protocolFactory = new TBinaryProtocol.Factory();
            server = new TThreadPoolServer(new TThreadPoolServer.Args(socket).
                    processor(processor).inputProtocolFactory(protocolFactory));

            log.info("Starting the CSGThrift server on port : " + port);
            new Thread(new CSGServerMainLoop(server), taskName).start();

        } catch (TTransportException e) {
            throw new AxisFault("TTransportException occurs", e);
        } catch (UnknownHostException e) {
            throw new AxisFault("Unknown host exception occurs", e);
        }
    }

    /**
     * Stop the server
     */
    public void stop() {
        if (server != null) {
            server.stop();
        }
    }

    /**
     * Returns if the server is still running
     *
     * @return true if server is running
     */
    public boolean isServerAlive() {
        return server != null && server.isServing();
    }

    /**
     * The task for starting the thrift server
     */
    private static class CSGServerMainLoop implements Runnable {
        private TServer server;

        private CSGServerMainLoop(TServer server) {
            this.server = server;
        }

        public void run() {
            try {
                server.serve();
            } catch (Exception e) {
                throw new RuntimeException("Could not start the CSGThrift server", e);
            }
        }
    }
}
