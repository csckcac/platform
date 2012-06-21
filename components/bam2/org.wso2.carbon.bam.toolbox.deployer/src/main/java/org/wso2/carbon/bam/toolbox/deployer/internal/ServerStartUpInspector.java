package org.wso2.carbon.bam.toolbox.deployer.internal;

import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.toolbox.deployer.core.BAMToolBoxDeployer;
import org.wso2.carbon.core.multitenancy.SuperTenantCarbonContext;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class ServerStartUpInspector extends Thread {
    private static final Log log = LogFactory.getLog(BAMToolBoxDeployer.class);
    private static boolean serverStarted = false;


    private int port;

    public void setPort(int port) {
        this.port = port;
    }

    public void run() {
        waitForServerStartup(port, 180000, true);
    }

    private static void waitForServerStartup(int port, long timeout, boolean verbose) {
        long startTime = System.currentTimeMillis();
        boolean isPortOpen = false;
        while (!isPortOpen && (System.currentTimeMillis() - startTime) < timeout) {
            Socket socket = null;
            try {
                InetAddress address = InetAddress.getByName("localhost");
                socket = new Socket(address, port);
                isPortOpen = socket.isConnected();
                if (isPortOpen) {
                    if (verbose) {
                        SuperTenantCarbonContext.getCurrentContext().setTenantId(MultitenantConstants.SUPER_TENANT_ID);
//                        log.info("Successfully connected to the server on port " + port);
                        serverStarted = true;
                         doPausedDeployments();
//                        try {
//                            saveInFileSystem(ServiceHolder.getRegistry(MultitenantConstants.SUPER_TENANT_ID),"repository/components/org.wso2.carbon.bam.gadgetgen/gadgetgen", "/home/sinthuja/test/registry_content");
//                        } catch (RegistryException e) {
//                            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//                        }
                    }
                    return;
                }
            } catch (IOException e) {
                if (verbose) {
//                    log.info("Waiting until server starts on port " + port);
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                }
            } finally {
                try {
                    if ((socket != null) && (socket.isConnected())) {
                        socket.close();
                    }
                } catch (IOException e) {
                    log.error("Can not close the socket with is used to check the server status ",
                            e);
                }
            }
        }
        throw new RuntimeException("Port " + port + " is not open");
    }

    public static boolean isServerStarted() {
        return serverStarted;
    }


    private static void doPausedDeployments() throws DeploymentException {
            BAMToolBoxDeployer deployer = BAMToolBoxDeployer.getPausedDeployments();
            deployer.doInitialUnDeployments();
            for (DeploymentFileData fileData : deployer.getPausedDeploymentFileDatas()) {
                fileData.deploy();
            }


    }

      private static void saveInFileSystem(Registry registry, String registryPath, String fileSystemPath) {
        try {
            Resource resource = registry.get(registryPath);
            if (resource instanceof Collection) {
                File curPath = new File(fileSystemPath+"/"+getFileName(registryPath));
                curPath.mkdirs();
                String[] allData = (String[]) resource.getContent();
                if (null != allData) {
                    for (String aPath : allData) {
                        saveInFileSystem(registry, aPath, curPath.getAbsolutePath());
                    }
                }
            } else {
                InputStream stream = resource.getContentStream();
                String newFile = fileSystemPath+"/"+getFileName(registryPath);
                DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(newFile)));
                int c;
                while ((c = stream.read()) != -1) {
                    out.writeByte(c);
                }
                stream.close();
                out.close();
            }
        } catch (RegistryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private static String getFileName(String path){
        String[] all = path.split("/");
        return all[all.length-1];
    }
}
