/*
 *  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.hosting.wnagent.util;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.rmi.RemoteException;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.hosting.wnagent.client.AutoscaleServiceClient;
import org.wso2.carbon.hosting.wnagent.exception.AgentServiceException;
import org.wso2.carbon.hosting.wnagent.service.AgentService;
import org.wso2.carbon.lb.common.dto.ContainerInformation;


/**
 * This thread tries to detect a server startup, at a given InetAddress and a port
 * combination, within some time period.
 */
public class ServerStartupDetector extends Thread {
    
    private static final Log log = LogFactory.getLog(ServerStartupDetector.class);
    
    /**
     * Time this tries to recover AgentService (in milliseconds).
     */
    private static final long TIME_OUT = 60000;
    
    private InetAddress serverAddress = null;
    private int port;
    private AgentService agent;
    private ContainerInformation containerInfo;
    private String domainName;
    //TODO set following correctly
    private String autoscalerEpr = "https://10.100.3.81:9443/services/AutoscalerService";
    
    public ServerStartupDetector(AgentService agent, ContainerInformation containerInfo, 
                                 String domain) {
        
        this.agent = agent;
        this.containerInfo = containerInfo;
        this.domainName = domain;
        
        try {
            serverAddress = InetAddress.getByName(containerInfo.getIp());
        } catch (UnknownHostException e) {
            log.error("IP address of the container is unknown: "+containerInfo.getIp(), e);
        }
        // FIXME should grab the port correctly
        this.port = 9443;
    }
    
    public void run() {
        
        boolean isServerStarted;
        
        long startTime = System.currentTimeMillis();

        // loop if and only if time out hasn't reached and server address isn't null
        while (serverAddress != null && ((System.currentTimeMillis() - startTime) < TIME_OUT)) {

            try {
                isServerStarted = isServerStarted(serverAddress, port);

                System.out.println("Server has started in address: " +
                    serverAddress.getHostAddress() + " and port: " + port);

                if (isServerStarted) {
                    // and decrement the pending instance count
                    decrementPendingInstanceCount();
                    return;
                }

                // sleep for 5s before next check
                Thread.sleep(5000);

            } catch (Exception ignored) {
                // do nothing
            }
        }
        
        //when it reaches here, we declare the server start up as a failure.
        // hence killing the container
        try {
            agent.stopAndDestroyContainer(containerInfo.getContainerId(), 
                                          containerInfo.getContainerRoot());
        } catch (AgentServiceException e) {
            log.error("Failed to destroy the container "+containerInfo.getContainerId(), e);
        }
        // and decrement the pending instance count
        decrementPendingInstanceCount();
        
    }
    
    private void decrementPendingInstanceCount() {

        try {
            AutoscaleServiceClient client = new AutoscaleServiceClient(autoscalerEpr);
            
            client.addPendingInstanceCount(domainName, -1);
            
        } catch (AxisFault e) {
            
            log.error("Failed to initiate Autoscaler Service client! ", e);
        } catch (RemoteException e) {
            
            log.error("Failed to send pending instance count to Autoscaler Service!", e);
        }
        
    }

    /**
     * Checks whether the given ip, port combination is not available.
     * @param ip {@link InetAddress} to be examined.
     * @param port port to be examined.
     * @return true if the ip, port combination is not available to be used and false
     * otherwise.
     */
    private static boolean isServerStarted(InetAddress ip, int port) {

        ServerSocket ss = null;

        try {
            ss = new ServerSocket(port, 0, ip);
            ss.setReuseAddress(true);
            return false;

        } catch (IOException e) {
        } finally {

            if (ss != null) {
                try {
                    ss.close();
                } catch (IOException e) {
                    /* should not be thrown */
                }
            }
        }

        return true;

    }

}


