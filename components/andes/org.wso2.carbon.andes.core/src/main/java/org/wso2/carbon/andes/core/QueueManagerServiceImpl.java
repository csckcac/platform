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
package org.wso2.carbon.andes.core;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.andes.configuration.ClientProperties;
import org.wso2.carbon.andes.core.internal.registry.QueueManagementBeans;
import org.wso2.carbon.andes.core.internal.util.QueueManagementConstants;
import org.wso2.carbon.andes.core.internal.util.Utils;
import org.wso2.carbon.andes.core.types.Queue;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;

import javax.jms.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.List;
import java.util.Properties;

public class QueueManagerServiceImpl implements QueueManagerService {

    private static int DEFAULT_ANDES_PORT = 5672;
    private static Log log = LogFactory.getLog(QueueManagerServiceImpl.class);
    private static String CARBON_CONFIG_PORT_OFFSET = "Ports.Offset";
    private static int CARBON_DEFAULT_PORT_OFFSET = 0;


    public void createQueue(String queueName) throws QueueManagerException {

         Connection queueConnection = null;
        try {
            System.setProperty(ClientProperties.AMQP_VERSION, "0-91");
            Properties initialContextProperties = new Properties();
            initialContextProperties.put("java.naming.factory.initial",
                    "org.wso2.andes.jndi.PropertiesFileInitialContextFactory");
            String jndiQueueName = "queue." + queueName;
            initialContextProperties.put(jndiQueueName, queueName);
            String connectionString = "amqp://admin:admin@clientID/carbon?brokerlist='tcp://localhost:" +
                    (DEFAULT_ANDES_PORT + readPortOffset())+ "'";
            initialContextProperties.put("connectionfactory.qpidConnectionfactory", connectionString);
            InitialContext initialContext = new InitialContext(initialContextProperties);
            QueueConnectionFactory queueConnectionFactory
                    = (QueueConnectionFactory) initialContext.lookup("qpidConnectionfactory");
            queueConnection = queueConnectionFactory.createConnection();
            queueConnection.start();
            Session queueSession = queueConnection.createSession(false, QueueSession.AUTO_ACKNOWLEDGE);
            Destination destination = (Destination) initialContext.lookup(queueName);

            MessageConsumer messageConsumer = queueSession.createConsumer(destination);
            messageConsumer.receiveNoWait();
            messageConsumer.close();

            queueSession.close();
            queueConnection.stop();
            queueConnection.close();


        } catch (NamingException e) {
            log.error("Error in creating queue connection", e);
            throw new QueueManagerException("Error in creating queue connection", e);
        } catch (JMSException e) {
            log.error("Error in creating queue connection", e);
            throw new QueueManagerException("Error in creating queue connection", e);
        }
    }

    @Override
    public List<Queue> getAllQueues() throws QueueManagerException {
        return QueueManagementBeans.getInstance().getAllQueues();
    }

    @Override
    public void deleteQueue(String queueName) throws QueueManagerException {
         try {
            UserRegistry userRegistry = Utils.getUserRegistry();
            String resourcePath = QueueManagementConstants.MB_QUEUE_STORAGE_PATH + "/" + queueName;
            if(QueueManagementBeans.getInstance().queueExists(queueName)){
                QueueManagementBeans.getInstance().deleteQueue(queueName);
                userRegistry.delete(resourcePath);
            }
        } catch (RegistryException e) {
            throw new QueueManagerException("Failed to delete queue: " + queueName, e);
        }

    }

    private int readPortOffset() {
        ServerConfiguration carbonConfig = ServerConfiguration.getInstance();
        String portOffset = carbonConfig.getFirstProperty(CARBON_CONFIG_PORT_OFFSET);

        try {
            return ((portOffset != null) ? Integer.parseInt(portOffset.trim()) : CARBON_DEFAULT_PORT_OFFSET);
        } catch (NumberFormatException e) {
            return CARBON_DEFAULT_PORT_OFFSET;
        }
    }


}
