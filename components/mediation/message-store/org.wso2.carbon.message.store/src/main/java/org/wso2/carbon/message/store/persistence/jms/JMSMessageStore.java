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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.message.store.AbstractMessageStore;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.message.store.persistence.jms.message.JMSPersistentMessage;
import org.wso2.carbon.message.store.persistence.jms.util.JMSPersistentMessageHelper;
import org.wso2.carbon.message.store.persistence.jms.util.JMSUtil;

import javax.jms.*;
import javax.jms.Queue;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings({"UnusedDeclaration"})
public class JMSMessageStore extends AbstractMessageStore {


    /**
     * Implementation level properties
     */
    private Properties properties = new Properties();

    /**
     * Look up context
     */
    private Context jndiContext;

    /**
     * JMS cachedConnection factory
     */
    private ConnectionFactory connectionFactory;

    /**
     * provider username
     */
    private String username;

    /**
     * provider password
     */
    private String password;

    /**
     * Message helper class that is used to convert Message serializable form
     */
    private JMSPersistentMessageHelper jmsPersistentMessageHelper;

    /**
     * Message Store size;
     */
    private volatile AtomicInteger size = new AtomicInteger(0);


    private int cacheLevel = JMSMessageStoreConstants.CACHE_NOTHING;

    /**
     * Cached shared Read Connection
     */
    private volatile Connection cachedReadConnection = null;

    /**
     * Cached shared Write Connection
     */
    private volatile Connection cachedWriteConnection = null;


    private String jmsMessageStoreDestination;

    private static final Log log = LogFactory.getLog(JMSMessageStore.class);


    private boolean jmsSpec11 = true;

    private Semaphore removeLock = new Semaphore(1);

    private Semaphore cleanUpOfferLock = new Semaphore(1);

    private AtomicBoolean cleaning = new AtomicBoolean(false);

    @Override
    public void init(SynapseEnvironment se) {
        super.init(se);
        init();
        jmsPersistentMessageHelper = new JMSPersistentMessageHelper(se);
        syncSize();
    }

    public boolean offer(MessageContext messageContext) {
        if (messageContext != null) {
            if (log.isDebugEnabled()) {
                log.debug("Storing the Message with Id :" +
                        messageContext.getMessageID() + " from the Message Store");
            }
        } else {
            return false;
        }

        JMSPersistentMessage jmsMessage = jmsPersistentMessageHelper.createPersistentMessage(
                messageContext);

        Connection con = null;
        Session session = null;
        MessageProducer producer = null;
        boolean error = false;

        if(cleaning.get()) {
            try {
                cleanUpOfferLock.acquire();
            } catch (InterruptedException e) {
                log.error("Message Cleanup lock released unexpectedly," +
                    "Message count value might show a wrong value ," +
                    "Restart the system to re sync the message count" ,e);
            }
        }

        try {
            con = getWriteConnection();
            session = JMSUtil.createSession(con,jmsSpec11);
            Destination destination = getDestination(session);
            producer = JMSUtil.createProducer(session,destination,jmsSpec11);
            ObjectMessage objectMessage = session.createObjectMessage(jmsMessage);
            producer.send(objectMessage);
            size.incrementAndGet();

        } catch (JMSException e) {
            log.error("JMS exception while saving a message to the store: " + name, e);
            error = true;
            throw new SynapseException("JMS Message Store Exception " + e);
        } finally {
            cleanUpOfferLock.release();
            cleanupJMSResources(con, session, producer, error, ConnectionType.WRITE_CONNECTION);
        }
        return true;
    }

    public MessageContext poll() {

        log.debug("Polling Message from Message store " + name);
        try {
            removeLock.acquire();
        } catch (InterruptedException e) {
            log.error("Message Removal lock released unexpectedly," +
                    "Message count value might show a wrong value ," +
                    "Restart the system to re sync the message count" ,e);
        }
        MessageContext smsg = null;
        Connection con = null;
        Session session = null;
        MessageConsumer consumer = null;
        boolean error = false;

        ClassLoader originalCl = getContextClassLoader();
        setContextClassLoader(this.getClass().getClassLoader());

        try {
            con = getWriteConnection();
            session = JMSUtil.createSession(con,jmsSpec11);
            Destination destination = getDestination(session);
            consumer = JMSUtil.createConsumer(session,destination,jmsSpec11);

            Object msg = consumer.receive(1000);
            if (msg instanceof ObjectMessage) {
                JMSPersistentMessage jmsMeg = (JMSPersistentMessage) ((ObjectMessage) msg).getObject();
                smsg = jmsPersistentMessageHelper.createMessageContext(jmsMeg);
            }

        } catch (JMSException e) {
            log.error("JMS error while removing messages from the store: " + name, e);
            error = true;
            throw new SynapseException("JMS Message Store Exception " + e);
        } finally {
            removeLock.release();
            cleanupJMSResources(con, session, consumer, error, ConnectionType.WRITE_CONNECTION);
        }

        size.decrementAndGet();
        setContextClassLoader(originalCl);
        return smsg;
    }

    public MessageContext peek() {
        Connection con = null;
        Session session = null;
        QueueBrowser browser = null;
        boolean error = false;

        ClassLoader originalClassLoader = getContextClassLoader();
        setContextClassLoader(this.getClass().getClassLoader());
        MessageContext messageContext = null;

        try {
            con = getReadConnection();
            session = JMSUtil.createSession(con,jmsSpec11);
            Destination destination = getDestination(session);

            browser = session.createBrowser((Queue) destination);
            Enumeration enumeration = browser.getEnumeration();

            if (enumeration.hasMoreElements()) {
                Object msg = enumeration.nextElement();

                /**Prevents message loss against the polling of the queue**/
                while (enumeration.hasMoreElements()) {
                    enumeration.nextElement();
				}

                JMSPersistentMessage jmsMeg = (JMSPersistentMessage) ((ObjectMessage) msg).getObject();
                messageContext = jmsPersistentMessageHelper.createMessageContext(jmsMeg);
            }

        } catch (JMSException e) {
            log.error("JMS error while retrieving messages from the store: " + name, e);
            error = true;
            throw new SynapseException("JMS Message Store Exception " + e);
        } finally {
            cleanupJMSResources(con, session, browser, error, ConnectionType.READ_CONNECTION);
        }


        setContextClassLoader(originalClassLoader);
        return messageContext;
    }

    public MessageContext remove() throws NoSuchElementException {
        if (size.get() == 0) {
            throw new NoSuchElementException("Message Store " + name + " Empty");
        }

        return poll();
    }

    public void clear() {
        Connection con = null;
        Session session = null;
        MessageConsumer consumer = null;
        boolean error = false;

        int count = 0;
        try {
            removeLock.acquire();
            cleaning.set(true);
            cleanUpOfferLock.acquire();
        } catch (InterruptedException e) {
             log.error("Message Removal lock released unexpectedly," +
                    "Message count value might show a wrong value ," +
                    "Restart the system to re sync the message count" ,e);
        }

        ClassLoader  originalClassLoader = getContextClassLoader();
        setContextClassLoader(this.getClass().getClassLoader());

        try {
            con = getWriteConnection();
            session = JMSUtil.createSession(con,jmsSpec11);
            Destination destination = getDestination(session);
            consumer = JMSUtil.createConsumer(session,destination,jmsSpec11);

            count = size();

            for (int i = 0; i < count;) {
                Message message = consumer.receive(1000);
                if (message != null) {
                    i++;
                } else {
                    break;
                }
            }
        } catch (JMSException e) {
            log.error("JMS error while deleting messages", e);
            error = true;
            throw new SynapseException("JMS Message Store Exception " + e);
        } finally {
            removeLock.release();
            cleaning.set(false);
            cleanUpOfferLock.release();
            size.set(size.get()-count);
            cleanupJMSResources(con, session, consumer, error, ConnectionType.WRITE_CONNECTION);
        }
        setContextClassLoader(originalClassLoader);
    }

    public MessageContext remove(String messageId) {
        // Removing a Random Message is not supported in JMS Message store;
        throw new RuntimeException("Removing a Random Message is not supported in JMS Message store");
    }

    public MessageContext get(int i) {

        if (i < 0 || i > (size() - 1)) {
            return null;
        }

        int pointer = 0;
        Connection con = null;
        Session session = null;
        QueueBrowser browser = null;
        boolean error = false;

        ClassLoader originalCl = getContextClassLoader();
        setContextClassLoader(this.getClass().getClassLoader());
        MessageContext messageContext = null;

        try {
            con = getReadConnection();
            session = JMSUtil.createSession(con,jmsSpec11);
            Destination destination = getDestination(session);

            browser = session.createBrowser((Queue) destination);
            Enumeration enumeration = browser.getEnumeration();

            while (enumeration.hasMoreElements()) {
                Object msg = enumeration.nextElement();
                if (pointer == i) {
                    if (msg != null) {
                        JMSPersistentMessage jmsMeg = (JMSPersistentMessage) ((ObjectMessage) msg).getObject();
                        messageContext = jmsPersistentMessageHelper.createMessageContext(jmsMeg);

                        /**Prevents message loss against the polling of the queue(issue occurs at ActiveMQ V5.4.2)**/
						while (enumeration.hasMoreElements()) {
							 enumeration.nextElement();
						}
                        return messageContext;
                    } else {
                        return null;
                    }
                } else {
                    pointer++;
                }
            }

        } catch (JMSException e) {
            log.error("JMS error while retrieving messages from the store: " + name, e);
            error = true;
            throw new SynapseException("JMS Message Store Exception " + e);
        } finally {
            cleanupJMSResources(con, session, browser, error, ConnectionType.READ_CONNECTION);
        }


        setContextClassLoader(originalCl);
        return messageContext;
    }

    public List<MessageContext> getAll() {
        List<MessageContext> list = new ArrayList<MessageContext>();
        Connection con = null;
        Session session = null;
        QueueBrowser browser = null;
        boolean error = false;

        ClassLoader originalCL = getContextClassLoader();
        setContextClassLoader(this.getClass().getClassLoader());

        try {
            con = getReadConnection();
            session = JMSUtil.createSession(con,jmsSpec11);
            Destination destination = getDestination(session);
            browser = session.createBrowser((Queue) destination);
            Enumeration enumeration = browser.getEnumeration();

            while (enumeration.hasMoreElements()) {
                Object msg = enumeration.nextElement();
                if (msg instanceof ObjectMessage) {
                	/**Prevents message loss against the polling of the queue(issue occurs at ActiveMQ V5.4.2)**/
					while (enumeration.hasMoreElements()) {
						 enumeration.nextElement();
					}
                    JMSPersistentMessage jmsMeg = (JMSPersistentMessage) ((ObjectMessage) msg).getObject();
                    list.add(jmsPersistentMessageHelper.createMessageContext(jmsMeg));
                }
            }

        } catch (JMSException e) {
            log.error("JMS error while retrieving messages from the store: " + name, e);
            error = true;
            throw new SynapseException("JMS Message Store Exception " + e);
        } finally {
            cleanupJMSResources(con, session, browser, error, ConnectionType.READ_CONNECTION);
        }

        setContextClassLoader(originalCL);

        return list;
    }

    public MessageContext get(String s) {
        if (s == null) {
            return null;
        }

        Connection con = null;
        Session session = null;
        QueueBrowser browser = null;
        boolean error = false;

        ClassLoader originalCl = getContextClassLoader();
        setContextClassLoader(this.getClass().getClassLoader());

        MessageContext messageContext = null;

        try {
            con = getReadConnection();
            session = JMSUtil.createSession(con,jmsSpec11);
            Destination destination = getDestination(session);

            browser = session.createBrowser((Queue) destination);
            Enumeration enumeration = browser.getEnumeration();

            while (enumeration.hasMoreElements()) {
				Object msg = enumeration.nextElement();
				JMSPersistentMessage jmsMeg =
				                              (JMSPersistentMessage) ((ObjectMessage) msg).getObject();

				if (s.equals(jmsMeg.getJmsPersistentAxis2Message().getMessageID())) {
					messageContext = jmsPersistentMessageHelper.createMessageContext(jmsMeg);
					  /**Prevents message loss against the polling of the queue(issue occurs at ActiveMQ V5.4.2)**/
					while (enumeration.hasMoreElements()) {
						enumeration.nextElement();
					}
					break;
				}
			}
        } catch (JMSException e) {
            log.error("JMS error while retrieving messages from the store: " + name, e);
            error = true;
            throw new SynapseException("JMS Message Store Exception " + e);
        } finally {
            cleanupJMSResources(con, session, browser, error, ConnectionType.READ_CONNECTION);
        }

        setContextClassLoader(originalCl);
        return messageContext;
    }


    private Connection getReadConnection() throws JMSException {
        if (cacheLevel > JMSMessageStoreConstants.CACHE_NOTHING) {
            return getCachedReadConnection();
        }
        return createConnection();
    }

    private Connection getWriteConnection() throws JMSException {
        if (cacheLevel > JMSMessageStoreConstants.CACHE_NOTHING) {
            return getCachedWriteConnection();
        }
        return createConnection();
    }

    private Destination getDestination(Session session) throws JMSException {
        Destination destination = null;
        if (jmsMessageStoreDestination == null) {
            jmsMessageStoreDestination = name + "_Queue";
        }

        try {
            destination = lookup(jndiContext, Destination.class, jmsMessageStoreDestination);
        } catch (NamingException e) {
            log.debug("Error creating Destination  " + jmsMessageStoreDestination + " : " + e +
                    " Destination is not defined in the JNDI context");
        } finally {
            if (destination == null) {
                destination = session.createQueue(jmsMessageStoreDestination);
            }
            return destination;
        }
    }


    private Connection getCachedReadConnection() throws JMSException {
        if (cachedReadConnection == null) {
            synchronized (this) {
                if (cachedReadConnection == null) {
                    if (log.isDebugEnabled()) {
                        log.debug("Creating a cached JMS connection for the store: " + name);
                    }
                    cachedReadConnection = createConnection();
                }
            }
        }
        return cachedReadConnection;
    }

    private Connection getCachedWriteConnection() throws JMSException {
        if (cachedWriteConnection == null) {
            synchronized (this) {
                if (cachedWriteConnection == null) {
                    if (log.isDebugEnabled()) {
                        log.debug("Creating a cached JMS connection for the store: " + name);
                    }
                    cachedWriteConnection = createConnection();
                }
            }
        }
        return cachedWriteConnection;
    }

    private Connection createConnection() throws JMSException {

        Connection con = JMSUtil.createConnection(connectionFactory,username,password,jmsSpec11);
        con.start();

        return con;
    }

    private void init() {
        log.debug("Initializing the JMS Message Store");
        try {

            jndiContext = CarbonContext.getCurrentContext().getJNDIContext(properties);

            String connectionFac = (String) parameters.get(JMSMessageStoreConstants.CONNECTION_FACTORY);
            if (connectionFac == null) {
                connectionFac = "QueueConnectionFactory";
            }

            connectionFactory = lookup(jndiContext, ConnectionFactory.class, connectionFac);

            if (connectionFactory == null) {
                throw new SynapseException("Connection factory not found :" + "QueueConnectionFactory");
            }

        } catch (NamingException e) {
            log.error("Naming Exception", e);
        }
    }

    private static <T> T lookup(Context context, Class<T> clazz, String name)
            throws NamingException {

        Object object = context.lookup(name);
        try {
            return clazz.cast(object);
        } catch (ClassCastException ex) {
            log.error("Error while performing the JNDI lookup for the name: " + name, ex);
            return null;
        }
    }


    @Override
    public void setParameters(Map<String, Object> parameters) {
        super.setParameters(parameters);

        if (parameters != null && !parameters.isEmpty()) {

            Set<Map.Entry<String, Object>> mapSet = parameters.entrySet();

            for (Map.Entry<String, Object> e : mapSet) {

                if (e.getValue() instanceof String) {
                    properties.put(e.getKey(), e.getValue());
                }

            }

            username = (String) parameters.get(JMSMessageStoreConstants.JMS_USERNAME);
            password = (String) parameters.get(JMSMessageStoreConstants.JMS_PASSWORD);

            String conCaching = (String) parameters.get(JMSMessageStoreConstants.ENABLE_CONNECTION_CACHING);

            if ("true".equals(conCaching)) {
                if (log.isDebugEnabled()) {
                    log.debug("Enabling the connection Caching");
                }
                cacheLevel = JMSMessageStoreConstants.CACHE_CONNECTION;
            }

            String jmsDest = (String) parameters.get(JMSMessageStoreConstants.JMS_DESTINATION);

            if(jmsDest != null) {
                jmsMessageStoreDestination = jmsDest;
            }

            String jmsSpecVersion = (String) parameters.
                                        get(JMSMessageStoreConstants.JMS_SPEC_VERSION);

            if(jmsSpecVersion != null) {

                if(!"1.1".equals(jmsSpecVersion)) {
                    jmsSpec11 = false;
                }

            }

        } else {
            throw new SynapseException("Required Parameters missing. Can't initialize " +
                    "JMS Message Store");
        }

    }

    private void cleanupJMSResources(Connection connection, Session session, Object jmsObject,
                                     boolean error, ConnectionType connectionType) {
        try {
            if (jmsObject != null) {
                if (jmsObject instanceof MessageProducer) {
                    ((MessageProducer) jmsObject).close();
                } else if (jmsObject instanceof MessageConsumer) {
                    ((MessageConsumer) jmsObject).close();
                } else if (jmsObject instanceof QueueBrowser) {
                    ((QueueBrowser) jmsObject).close();
                }
            }
        } catch (JMSException e) {
            log.error("Error while cleaning up JMS objects in the message store: " + name, e);
        }

        try {
            if (session != null) {
                session.close();
            }
        } catch (JMSException e) {
            log.error("Error while cleaning up JMS session in the message store: " + name, e);
        }

        try {
            if (connection != null && cacheLevel == JMSMessageStoreConstants.CACHE_NOTHING) {
                connection.close();
            } else if (error) {
                // if we are using a cached JMS connection, then we should only close it in case
                // of errors

                switch (connectionType) {
                    case READ_CONNECTION: {
                        cleanupCachedReadConnection();
                        break;
                    }
                    case WRITE_CONNECTION: {
                        cleanupCachedWriteConnection();
                        break;
                    }
                }
            }
        } catch (JMSException e) {
            log.error("Error while cleaning up JMS connections in the message store: " + name, e);
        }
    }

    private void cleanupCachedReadConnection() {
        if (cachedReadConnection != null) {
            if (log.isDebugEnabled()) {
                log.debug("Closing the cached JMS connection in: " + name);
            }

            try {
                cachedReadConnection.close();
            } catch (JMSException e) {
                log.warn("Error closing the JMS connection", e);
            }
            cachedReadConnection = null;
        }
    }

    private void cleanupCachedWriteConnection() {
        if (cachedReadConnection != null) {
            if (log.isDebugEnabled()) {
                log.debug("Closing the cached JMS connection in: " + name);
            }

            try {
                cachedReadConnection.close();
            } catch (JMSException e) {
                log.warn("Error closing the JMS connection", e);
            }
            cachedReadConnection = null;
        }
    }

    private void syncSize() {
        log.debug("Synchronizing Message Store size with the Queue Size");
        int count = 0;
        Connection con = null;
        Session session = null;
        QueueBrowser browser = null;
        boolean error = false;

        ClassLoader originalCL = getContextClassLoader();

        this.setContextClassLoader(this.getClass().getClassLoader());

        try {
            con = getReadConnection();
            session = con.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination destination = getDestination(session);

            browser = session.createBrowser((Queue) destination);
            Enumeration enumeration = browser.getEnumeration();

            while (enumeration.hasMoreElements()) {
                enumeration.nextElement();
                count++;
            }

        } catch (JMSException e) {
            log.error("JMS error while updating size of the store: " + name, e);
            error = true;
        } finally {
            cleanupJMSResources(con, session, browser, error, ConnectionType.READ_CONNECTION);
        }

        this.size.set(count);
        log.debug("Updated JMS Message Store Size :" + size);

        this.setContextClassLoader(originalCL);

    }


   private void setContextClassLoader(final ClassLoader cl) {
        AccessController.doPrivileged(
                 new PrivilegedAction() {
                     public Object run() {
                         Thread.currentThread().setContextClassLoader(cl);
                         return null;
                     }
                 }
        );
   }

    private ClassLoader getContextClassLoader(){
        ClassLoader cl = (ClassLoader) AccessController.doPrivileged(
                 new PrivilegedAction() {
                     public Object run() {
                         return Thread.currentThread().getContextClassLoader();
                     }
                 }
         );

        return  cl;
    }

    @Override
    public int size() {
        if(size.get() < 0) {
            size.set(0);
        }
        return size.get();
    }

    @Override
    public void destroy() {
        super.destroy();
        try {

            cleanupCachedReadConnection();
            cleanupCachedWriteConnection();

            if(jndiContext != null) {
                jndiContext.close();
            }
        } catch (NamingException e) {
            log.error("Error closing the JNDI Context" ,e);
        }
    }

    private enum ConnectionType {
        READ_CONNECTION, WRITE_CONNECTION
    }


}
