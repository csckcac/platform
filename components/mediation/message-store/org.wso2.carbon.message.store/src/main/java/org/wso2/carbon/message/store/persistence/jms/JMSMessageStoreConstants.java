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
package org.wso2.carbon.message.store.persistence.jms;

public final class JMSMessageStoreConstants {

    /**
     * JNDI Name of the Queue Name that message store is connected to
     */
    public static final String JMS_DESTINATION = "store.jms.destination";

    /**
     * User Name that is used to create the connection with the broker
     */
    public static final String JMS_USERNAME = "store.jms.username";

    /**
     * Password that is used to create the connection with the broker
     */
    public static final String JMS_PASSWORD = "store.jms.password";

    /**
     * If Connection caching is set to true it will share the same connection
     */
    public static final String ENABLE_CONNECTION_CACHING = "store.jms.cache.connection";

    /**
     * JNDI name of the Connection factory which is used to create jms connections
     */
    public static final String CONNECTION_FACTORY = "store.jms.connection.factory";

    /**
     * The parameter indicating the JMS API specification to be used - if this is "1.1" the JMS
     * 1.1 API would be used, else the JMS 1.0.2B
     */
    public static final String JMS_SPEC_VERSION = "store.jms.JMSSpecVersion";

    public static final int CACHE_CONNECTION = 1;

    public static final int CACHE_NOTHING = 0;

}
