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
package org.wso2.registry.checkin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;
import org.wso2.carbon.registry.synchronization.SynchronizationException;
import org.wso2.carbon.registry.synchronization.message.MessageCode;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.CarbonContextHolder;

import java.io.File;

/**
 * check-in client class
 *
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
public class Client
{
    private static final Log log = LogFactory.getLog(Client.class);
    
    public final static int CHECKOUT = 1;
    public final static int CHECK_IN = 2;
    public final static int UPDATE = 3;

    ClientOptions clientOptions;

    public Client() {
        this(new ClientOptions());
    }

    public Client(ClientOptions clientOptions) {
        this.clientOptions = clientOptions;
    }

    public void start(String[] arguments) {
        initializeLog4j();
        UserInteractor userInteractor = new DefaultUserInteractor();
        clientOptions.setUserInteractor(userInteractor);
        try {
            execute(arguments);
        } catch (SynchronizationException e) {
            MessageCode exceptionMsgCode  = e.getCode();
            String[] parameters = e.getParameters();
            // In the command line tool, we will just print the message code
            String msg = userInteractor.showMessage(exceptionMsgCode, parameters);
            log.error(msg, e);
        }
    }

    public void execute(String[] arguments) throws SynchronizationException {
        int operation = -1;
        if (arguments.length == 0) {
            throw new SynchronizationException(MessageCode.NO_OPTIONS_PROVIDED);
        }
        if (arguments.length == 1 && arguments[0].equals("-h")) {
            ClientUtils.printMessage(clientOptions, MessageCode.HELP);
            return;
        }

        // now loop through the arguments list to capture the options
        for (int i = 0; i < arguments.length; i ++) {
            if (operation == -1) {
                if (arguments[i].equals("checkout") || arguments[i].equals("co")) {
                    if ( arguments.length <= i) {
                        throw new SynchronizationException(MessageCode.CO_PATH_MISSING);
                    }
                    i ++;
                    if (arguments.length -1 == i) {
                        throw new SynchronizationException(MessageCode.CO_PATH_MISSING);
                    }
                    String url = arguments[i];
                    clientOptions.setUserUrl(url);
                    operation = CHECKOUT;
                }
                else if (arguments[i].equals("ci") || arguments[i].equals("checkin")) {
                    operation = CHECK_IN;
                    if (arguments.length > i + 1 ) {
                        String url = arguments[i +1];
                        if (url.startsWith("http") || url.startsWith("/")) {
                            clientOptions.setUserUrl(url);
                            i ++;
                        }
                    }
                }
                else if(arguments[i].equals("up") || arguments[i].equals("update")) {
                    operation = UPDATE;
                    if (arguments.length > i + 1 ) {
                        String url = arguments[i +1];
                        if (url.startsWith("http") || url.startsWith("/")) {
                            clientOptions.setUserUrl(url);
                            i ++;
                        }
                    }
                }
            }

            if (arguments[i].equals("-u") || arguments[i].equals("--user")) {
                if (arguments.length -1 == i) {
                    throw new SynchronizationException(MessageCode.USERNAME_MISSING);
                }
                i ++;
                String username = arguments[i];
                clientOptions.setUsername(username);
            }
            if (arguments[i].equals("-p") || arguments[i].equals("--password")) {
                if (arguments.length -1 == i) {
                    throw new SynchronizationException(MessageCode.PASSWORD_MISSING);
                }
                i ++;
                String password = arguments[i];
                clientOptions.setPassword(password);
            }
            if (arguments[i].equals("-l") || arguments[i].equals("--location")) {
                if (arguments.length -1 == i) {
                    throw new SynchronizationException(MessageCode.WORKING_DIR_MISSING);
                }
                i ++;
                String workingDirectory = arguments[i];
                File workingDirFile = new File(workingDirectory);
                if (workingDirFile.exists()) {
                    /*if (!workingDirFile.isDirectory()) {
                        throw new SynchronizationException(MessageCode.WRONG_WORKING_DIR);
                    }*/
                }
                else {
                    // ignores the return value
                    workingDirFile.mkdirs();
                }
                clientOptions.setWorkingDir(workingDirectory);
            }
            if (arguments[i].equals("-f") || arguments[i].equals("--filename")) {
                if (arguments.length -1 == i) {
                    throw new SynchronizationException(MessageCode.DUMP_FILE_MISSING);
                }
                i ++;
                String outputFile = arguments[i];
                clientOptions.setOutputFile(outputFile);
            }
            if (arguments[i].equals("-i") || arguments[i].equals("--interactive")) {
                clientOptions.setInteractive(true);
            }
            if (arguments[i].equals("-t") || arguments[i].equals("--type")) {
                if (arguments.length -1 == i) {
                    throw new SynchronizationException(MessageCode.REGISTRY_TYPE_MISSING);
                }
                i ++;
                String type = arguments[i].toUpperCase();
                clientOptions.setType(ClientOptions.RegistryType.valueOf(type));
            }
            if (arguments[i].equals("--tenant")) {
                if (arguments.length - 1 != i) {
                    i++;
                    clientOptions.setTenantId(Integer.parseInt(arguments[i]));
                }
            }
        }

        if (operation == -1) {
            throw new SynchronizationException(MessageCode.OPERATION_NOT_FOUND);
        }

        if (clientOptions.getUsername() == null ||
                clientOptions.getUsername().equals("")) {
            throw new SynchronizationException(MessageCode.USERNAME_NOT_PROVIDED);
        }

        ClientUtils.setSystemProperties();

        if( null != clientOptions.getUserUrl() && clientOptions.getUserUrl().startsWith("/")) {
            // Enforce the initialization of the CarbonContextHolder if it's run from local registry. This
            // will make it possible to do required initializations for Multi-Tenant JNDI and caching.
            CarbonContextHolder.getCurrentCarbonContextHolder();

        }
        // now call the checkout operation.
        if (operation == CHECKOUT) {
            Checkout checkout = new Checkout(clientOptions);
            checkout.execute();
        }
        else if (operation == CHECK_IN) {
            new Checkin(clientOptions).execute();
        }
        else { //  if (operation == UPDATE)
            Update update = new Update(clientOptions);
            update.execute();
        }

    }

    private static void initializeLog4j() {
        String log4jConfFile = "log4j.properties";
        String carbonHome = CarbonUtils.getCarbonHome();
        if (carbonHome != null) {
            log4jConfFile = carbonHome + "/lib/checkin-client/log4j.properties";
        }
        if (new File(log4jConfFile).exists()) {
            PropertyConfigurator.configure(log4jConfFile);
        }
    }
}
