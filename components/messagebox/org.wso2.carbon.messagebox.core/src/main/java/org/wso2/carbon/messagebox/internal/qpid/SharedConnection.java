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

package org.wso2.carbon.messagebox.internal.qpid;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jms.QueueConnection;
import javax.jms.JMSException;

/**
 * An object of this class will keep a JMS Connection object together with it's reference count.
 */
public class SharedConnection {
    private static final Log log = LogFactory.getLog(SharedConnection.class);

    private QueueConnection connection = null;
    private int refCount = 0;

    /**
        * Create shared JMS queue connection
        *
        * @param connection
        *               Shared JMS queue connection
        */
    public SharedConnection(QueueConnection connection) {
        this.connection = connection;
    }

    /**
        * Get reference to shared JMS queue connection.
        *
        * IMPORTANT : This method should not be used when it is required to use the connection
        *
        * @return
        *           Reference to the shared JMS queue connection object
        */
    public QueueConnection getConnection() {
        return connection;
    }

    /**
        * This method is used when it is required to use the shared connection
        *
        * @return
        *           Reference to the shared JMS queue connection 
        */
    public QueueConnection useConnection() {
        refCount++;
        return getConnection();
    }

    /**
        * Release connection so that it's reference count is reduced
        *
        * @return
        *           Remaining reference count 
        * @throws JMSException
        */
    public int releaseConnection() throws JMSException {
        refCount--;

        // Connection no longer in use
        if (0 == refCount) {
            connection.stop();
            connection.close();
        }

        return refCount;
    }
}
