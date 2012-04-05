/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 **/


package org.wso2.carbon.qpid.messagestore.jdbc;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.log4j.Logger;
import org.apache.qpid.AMQException;
import org.apache.qpid.AMQStoreException;
import org.apache.qpid.framing.AMQShortString;
import org.apache.qpid.framing.FieldTable;
import org.apache.qpid.server.exchange.Exchange;
import org.apache.qpid.server.logging.LogSubject;
import org.apache.qpid.server.logging.actors.CurrentActor;
import org.apache.qpid.server.logging.messages.ConfigStoreMessages;
import org.apache.qpid.server.logging.messages.MessageStoreMessages;
import org.apache.qpid.server.logging.messages.TransactionLogMessages;
import org.apache.qpid.server.queue.AMQQueue;
import org.apache.qpid.server.store.ConfigurationRecoveryHandler;
import org.apache.qpid.server.store.MessageMetaDataType;
import org.apache.qpid.server.store.MessageStore;
import org.apache.qpid.server.store.MessageStoreRecoveryHandler;
import org.apache.qpid.server.store.StorableMessageMetaData;
import org.apache.qpid.server.store.StoredMessage;
import org.apache.qpid.server.store.TransactionLogRecoveryHandler;
import org.apache.qpid.server.store.TransactionLogResource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.nio.ByteBuffer;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * An implementation of a {@link org.apache.qpid.server.store.MessageStore} that uses JDBC as the persistance
 * mechanism.
 * <p/>
 */

public class JDBCMessageStore implements MessageStore {

    private static final Logger _logger = Logger.getLogger(JDBCMessageStore.class);


    private static final String EXCHANGE_TABLE_NAME = "QPID_EXCHANGE";
    private static final String QUEUE_TABLE_NAME = "QPID_QUEUE";
    private static final String BINDINGS_TABLE_NAME = "QPID_BINDINGS";
    private static final String QUEUE_ENTRY_TABLE_NAME = "QPID_QUEUE_ENTRY";

    private static final String META_DATA_TABLE_NAME = "QPID_META_DATA";
    private static final String MESSAGE_CONTENT_TABLE_NAME = "QPID_MESSAGE_CONTENT";
    public static final String JDBC_CONNECTION_URL = "url";
    public static final String JDBC_CONNECTION_USER_NAME = "userName";
    public static final String JDBC_CONNECTION_PASSWORD = "password";
    public static final String JDBC_DRIVER_NAME = "driverName";
    public static final String JDBC_MAX_ACTIVE = "maxActive";
    public static final String JDBC_MAX_WAIT = "maxWait";
    public static final String JDBC_MIN_IDLE = "minIdle";

    private final AtomicLong _messageId = new AtomicLong(0);
    private AtomicBoolean _closed = new AtomicBoolean(false);

    private BasicDataSource _basicDataSource;

    private static final String SELECT_FROM_QUEUE = "SELECT name, owner, exclusive, arguments FROM " + QUEUE_TABLE_NAME;
    private static final String FIND_QUEUE = "SELECT name, owner FROM " + QUEUE_TABLE_NAME + " WHERE name = ?";
    private static final String UPDATE_QUEUE_EXCLUSIVITY = "UPDATE " + QUEUE_TABLE_NAME + " SET exclusive = ? WHERE name = ?";
    private static final String SELECT_FROM_EXCHANGE = "SELECT name, type, autodelete FROM " + EXCHANGE_TABLE_NAME;
    private static final String SELECT_FROM_BINDINGS = "SELECT exchange_name, queue_name, binding_key, arguments FROM " + BINDINGS_TABLE_NAME + " ORDER BY exchange_name";
    private static final String FIND_BINDING = "SELECT * FROM " + BINDINGS_TABLE_NAME + " WHERE exchange_name = ? AND queue_name = ? AND binding_key = ? ";
    private static final String INSERT_INTO_EXCHANGE = "INSERT INTO " + EXCHANGE_TABLE_NAME + " ( name, type, autodelete ) VALUES ( ?, ?, ? )";
    private static final String DELETE_FROM_EXCHANGE = "DELETE FROM " + EXCHANGE_TABLE_NAME + " WHERE name = ?";
    private static final String FIND_EXCHANGE = "SELECT name FROM " + EXCHANGE_TABLE_NAME + " WHERE name = ?";
    private static final String INSERT_INTO_BINDINGS = "INSERT INTO " + BINDINGS_TABLE_NAME + " ( exchange_name, queue_name, binding_key, arguments ) values ( ?, ?, ?, ? )";
    private static final String DELETE_FROM_BINDINGS = "DELETE FROM " + BINDINGS_TABLE_NAME + " WHERE exchange_name = ? AND queue_name = ? AND binding_key = ?";
    private static final String INSERT_INTO_QUEUE = "INSERT INTO " + QUEUE_TABLE_NAME + " (name, owner, exclusive, arguments) VALUES (?, ?, ?, ?)";
    private static final String DELETE_FROM_QUEUE = "DELETE FROM " + QUEUE_TABLE_NAME + " WHERE name = ?";
    private static final String INSERT_INTO_QUEUE_ENTRY = "INSERT INTO " + QUEUE_ENTRY_TABLE_NAME + " (queue_name, message_id) values (?,?)";
    private static final String DELETE_FROM_QUEUE_ENTRY = "DELETE FROM " + QUEUE_ENTRY_TABLE_NAME + " WHERE queue_name = ? AND message_id =?";
    private static final String SELECT_FROM_QUEUE_ENTRY = "SELECT queue_name, message_id FROM " + QUEUE_ENTRY_TABLE_NAME + " ORDER BY queue_name, message_id";
    private static final String INSERT_INTO_MESSAGE_CONTENT = "INSERT INTO " + MESSAGE_CONTENT_TABLE_NAME + "( message_id, offset, last_byte, content ) values (?, ?, ?, ?)";
    private static final String SELECT_FROM_MESSAGE_CONTENT = "SELECT offset, content FROM " + MESSAGE_CONTENT_TABLE_NAME + " WHERE message_id = ? AND last_byte > ? AND offset < ? ORDER BY message_id, offset";
    private static final String DELETE_FROM_MESSAGE_CONTENT = "DELETE FROM " + MESSAGE_CONTENT_TABLE_NAME + " WHERE message_id = ?";
    private static final String INSERT_INTO_META_DATA = "INSERT INTO " + META_DATA_TABLE_NAME + "( message_id , meta_data ) values (?, ?)";
    private static final String SELECT_FROM_META_DATA = "SELECT meta_data FROM " + META_DATA_TABLE_NAME + " WHERE message_id = ?";
    private static final String DELETE_FROM_META_DATA = "DELETE FROM " + META_DATA_TABLE_NAME + " WHERE message_id = ?";
    private static final String SELECT_ALL_FROM_META_DATA = "SELECT message_id, meta_data FROM " + META_DATA_TABLE_NAME;

    private LogSubject _logSubject;
    private boolean _configured;
    private static final int DEFAULT_MAX_ACTIVE = 50;
    private static final long DEFAULT_MAX_WAIT = 60000;
    private static final int DEFAULT_MIN_IDLE = 10;


    private enum State {
        INITIAL,
        CONFIGURING,
        RECOVERING,
        STARTED,
        CLOSING,
        CLOSED
    }

    private State _state = State.INITIAL;

    @Override
    public void configureConfigStore(String name,
                                     ConfigurationRecoveryHandler recoveryHandler,
                                     Configuration storeConfiguration,
                                     LogSubject logSubject) throws Exception {
        stateTransition(State.INITIAL, State.CONFIGURING);
        _logSubject = logSubject;
        CurrentActor.get().message(_logSubject, ConfigStoreMessages.CREATED(this.getClass().getName()));

        if (!_configured) {
            commonConfiguration(name, storeConfiguration, logSubject);
            _configured = true;
        }

        // this recovers durable exchanges, queues, and bindings
        recover(recoveryHandler);


        stateTransition(State.RECOVERING, State.STARTED);

    }

    @Override
    public void configureMessageStore(String name,
                                      MessageStoreRecoveryHandler recoveryHandler,
                                      Configuration storeConfiguration,
                                      LogSubject logSubject) throws Exception {
        CurrentActor.get().message(_logSubject, MessageStoreMessages.CREATED(this.getClass().getName()));

        if (!_configured) {

            _logSubject = logSubject;

            commonConfiguration(name, storeConfiguration, logSubject);
            _configured = true;
        }

        recoverMessages(recoveryHandler);

    }

    @Override
    public void configureTransactionLog(String name,
                                        TransactionLogRecoveryHandler recoveryHandler,
                                        Configuration storeConfiguration,
                                        LogSubject logSubject) throws Exception {
        CurrentActor.get().message(_logSubject, TransactionLogMessages.CREATED(this.getClass().getName()));

        if (!_configured) {

            _logSubject = logSubject;

            commonConfiguration(name, storeConfiguration, logSubject);
            _configured = true;
        }

        recoverQueueEntries(recoveryHandler);

    }


    private void commonConfiguration(String name, Configuration storeConfiguration,
                                     LogSubject logSubject)
            throws ClassNotFoundException, SQLException {
        _basicDataSource = buildDataSource(storeConfiguration);
    }

    public static BasicDataSource buildDataSource(Configuration storeConfiguration) {

        BasicDataSource basicDataSource = new BasicDataSource();
        basicDataSource.setUrl(storeConfiguration.getString(JDBC_CONNECTION_URL));
        basicDataSource.setDriverClassName(storeConfiguration.getString(JDBC_DRIVER_NAME));
        basicDataSource.setUsername(storeConfiguration.getString(JDBC_CONNECTION_USER_NAME));
        basicDataSource.setPassword(storeConfiguration.getString(JDBC_CONNECTION_PASSWORD));

        if (storeConfiguration.getString(JDBC_MAX_ACTIVE) != null) {
            basicDataSource.setMaxActive(Integer.parseInt(storeConfiguration.getString(JDBC_MAX_ACTIVE)));
        } else {
            basicDataSource.setMaxActive(DEFAULT_MAX_ACTIVE);
        }

        if (storeConfiguration.getString(JDBC_MAX_WAIT) != null) {
            basicDataSource.setMaxWait(Integer.parseInt(storeConfiguration.getString(JDBC_MAX_WAIT)));
        } else {
            basicDataSource.setMaxWait(DEFAULT_MAX_WAIT);
        }

        if (storeConfiguration.getString(JDBC_MIN_IDLE) != null) {
            basicDataSource.setMaxIdle(Integer.parseInt(storeConfiguration.getString(JDBC_MIN_IDLE)));
        } else {
            basicDataSource.setMinIdle(DEFAULT_MIN_IDLE);
        }

        return basicDataSource;
    }

    public void recover(ConfigurationRecoveryHandler recoveryHandler) throws AMQException {
        stateTransition(State.CONFIGURING, State.RECOVERING);

        CurrentActor.get().message(_logSubject, MessageStoreMessages.RECOVERY_START());

        try {
            ConfigurationRecoveryHandler.QueueRecoveryHandler qrh = recoveryHandler.begin(this);
            loadQueues(qrh);

            ConfigurationRecoveryHandler.ExchangeRecoveryHandler erh = qrh.completeQueueRecovery();
            List<String> exchanges = loadExchanges(erh);
            ConfigurationRecoveryHandler.BindingRecoveryHandler brh = erh.completeExchangeRecovery();
            recoverBindings(brh, exchanges);
            brh.completeBindingRecovery();
        } catch (SQLException e) {

            throw new AMQStoreException("Error recovering persistent state: " + e.getMessage(), e);
        }


    }

    private void loadQueues(ConfigurationRecoveryHandler.QueueRecoveryHandler qrh)
            throws SQLException {
        Connection conn = newAutoCommitConnection();
        try {
            Statement stmt = conn.createStatement();
            try {
                ResultSet rs = stmt.executeQuery(SELECT_FROM_QUEUE);
                try {

                    while (rs.next()) {
                        String queueName = rs.getString(1);
                        String owner = rs.getString(2);
                        boolean exclusive = rs.getBoolean(3);
                        Blob argumentsAsBlob = rs.getBlob(4);

                        byte[] dataAsBytes = argumentsAsBlob.getBytes(1, (int) argumentsAsBlob.length());
                        FieldTable arguments;
                        if (dataAsBytes.length > 0) {
                            org.apache.mina.common.ByteBuffer buffer = org.apache.mina.common.ByteBuffer.wrap(dataAsBytes);

                            arguments = new FieldTable(buffer, buffer.limit());
                        } else {
                            arguments = null;
                        }

                        qrh.queue(queueName, owner, exclusive, arguments);

                    }

                } finally {
                    rs.close();
                }
            } finally {
                stmt.close();
            }
        } finally {
            conn.close();
        }
    }


    private List<String> loadExchanges(ConfigurationRecoveryHandler.ExchangeRecoveryHandler erh)
            throws SQLException {

        List<String> exchanges = new ArrayList<String>();
        Connection conn = null;
        try {
            conn = newAutoCommitConnection();

            Statement stmt = conn.createStatement();
            try {
                ResultSet rs = stmt.executeQuery(SELECT_FROM_EXCHANGE);
                try {
                    while (rs.next()) {
                        String exchangeName = rs.getString(1);
                        String type = rs.getString(2);
                        boolean autoDelete = rs.getShort(3) != 0;

                        exchanges.add(exchangeName);

                        erh.exchange(exchangeName, type, autoDelete);

                    }
                    return exchanges;
                } finally {
                    rs.close();
                }
            } finally {
                stmt.close();
            }
        } finally {
            if (conn != null) {
                conn.close();
            }
        }

    }

    private void recoverBindings(ConfigurationRecoveryHandler.BindingRecoveryHandler brh,
                                 List<String> exchanges) throws SQLException {
        _logger.info("Recovering bindings...");

        Connection conn = null;
        try {
            conn = newAutoCommitConnection();

            PreparedStatement stmt = conn.prepareStatement(SELECT_FROM_BINDINGS);

            try {
                ResultSet rs = stmt.executeQuery();

                try {

                    while (rs.next()) {
                        String exchangeName = rs.getString(1);
                        String queueName = rs.getString(2);
                        String bindingKey = rs.getString(3);
                        Blob arguments = rs.getBlob(4);
                        ByteBuffer buf;

                        if (arguments != null && arguments.length() != 0) {
                            byte[] argumentBytes = arguments.getBytes(1, (int) arguments.length());
                            buf = ByteBuffer.wrap(argumentBytes);
                        } else {
                            buf = null;
                        }

                        brh.binding(exchangeName, queueName, bindingKey, buf);
                    }
                } finally {
                    rs.close();
                }
            } finally {
                stmt.close();
            }

        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }


    public void close() throws Exception {
        CurrentActor.get().message(_logSubject, MessageStoreMessages.CLOSED());
        _closed.getAndSet(true);

        try {
            _basicDataSource.close();
        } catch (SQLException e) {
            _logger.error("Exception whilst shutting down the store: " + e);
        }
    }


    public StoredMessage addMessage(StorableMessageMetaData metaData) {
        if (metaData.isPersistent()) {
            return new StoredJDBCMessage(_messageId.incrementAndGet(), metaData);
        } else {
            return new StoredJDBCMessage(_messageId.incrementAndGet(), metaData);
        }
    }


    public void removeMessage(long messageId) {
        try {
            Connection conn = newConnection();
            try {
                PreparedStatement stmt = conn.prepareStatement(DELETE_FROM_META_DATA);
                try {
                    stmt.setLong(1, messageId);
                    int results = stmt.executeUpdate();
                    stmt.close();

                    if (results == 0) {
                        throw new RuntimeException("Message metadata not found for message id " + messageId);
                    }

                    if (_logger.isDebugEnabled()) {
                        _logger.debug("Deleted metadata for message " + messageId);
                    }

                    stmt = conn.prepareStatement(DELETE_FROM_MESSAGE_CONTENT);
                    stmt.setLong(1, messageId);
                    results = stmt.executeUpdate();
                } finally {
                    stmt.close();
                }
                conn.commit();
            } catch (SQLException e) {
                try {
                    conn.rollback();
                } catch (SQLException t) {
                    // ignore - we are re-throwing underlying exception
                }

                throw e;

            } finally {
                conn.close();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error removing message with id " + messageId + " from database: " + e.getMessage(), e);
        }

    }


    public void createExchange(Exchange exchange) throws AMQStoreException {
        if (_state != State.RECOVERING) {
            try {
                Connection conn = newAutoCommitConnection();

                try {


                    PreparedStatement stmt = conn.prepareStatement(FIND_EXCHANGE);
                    try {
                        stmt.setString(1, exchange.getNameShortString().toString());
                        ResultSet rs = stmt.executeQuery();
                        try {

                            // If we don't have any data in the result set then we can add this exchange
                            if (!rs.next()) {

                                PreparedStatement insertStmt = conn.prepareStatement(INSERT_INTO_EXCHANGE);
                                try {
                                    insertStmt.setString(1, exchange.getName());
                                    insertStmt.setString(2, exchange.getTypeShortString().asString());
                                    insertStmt.setShort(3, exchange.isAutoDelete() ? (short) 1 : (short) 0);
                                    insertStmt.execute();
                                } finally {
                                    insertStmt.close();
                                }
                            }
                        } finally {
                            rs.close();
                        }
                    } finally {
                        stmt.close();
                    }

                } finally {
                    conn.close();
                }
            } catch (SQLException e) {
                throw new AMQStoreException("Error writing Exchange with name " + exchange.getNameShortString() + " to database: " + e.getMessage(), e);
            }
        }

    }

    public void removeExchange(Exchange exchange) throws AMQStoreException {

        try {
            Connection conn = newAutoCommitConnection();
            try {
                PreparedStatement stmt = conn.prepareStatement(DELETE_FROM_EXCHANGE);
                try {
                    stmt.setString(1, exchange.getNameShortString().toString());
                    int results = stmt.executeUpdate();
                    stmt.close();
                    if (results == 0) {
                        throw new AMQStoreException("Exchange " + exchange.getNameShortString() + " not found");
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException e) {
            throw new AMQStoreException("Error deleting Exchange with name " + exchange.getNameShortString() + " from database: " + e.getMessage(), e);
        }
    }

    public void bindQueue(Exchange exchange, AMQShortString routingKey, AMQQueue queue,
                          FieldTable args)
            throws AMQStoreException {
        if (_state != State.RECOVERING) {
            try {
                Connection conn = newAutoCommitConnection();

                try {

                    PreparedStatement stmt = conn.prepareStatement(FIND_BINDING);
                    try {
                        stmt.setString(1, exchange.getNameShortString().toString());
                        stmt.setString(2, queue.getNameShortString().toString());
                        stmt.setString(3, routingKey == null ? null : routingKey.toString());

                        ResultSet rs = stmt.executeQuery();
                        try {
                            // If this binding is not already in the store then create it.
                            if (!rs.next()) {
                                PreparedStatement insertStmt = conn.prepareStatement(INSERT_INTO_BINDINGS);
                                try {
                                    insertStmt.setString(1, exchange.getNameShortString().toString());
                                    insertStmt.setString(2, queue.getNameShortString().toString());
                                    insertStmt.setString(3, routingKey == null ? null : routingKey.toString());
                                    if (args != null) {
// This would be the Java 6 way of setting a Blob
                                        /*  Blob blobArgs = conn.createBlob();
                                       blobArgs.setBytes(0, args.getDataAsBytes());
                                       stmt.setBlob(4, blobArgs);*/


                                        byte[] bytes = args.getDataAsBytes();
                                        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
                                        insertStmt.setBinaryStream(4, bis, bytes.length);
                                    } else {
                                        insertStmt.setNull(4, Types.BLOB);
                                    }

                                    insertStmt.executeUpdate();
                                } finally {
                                    insertStmt.close();
                                }
                            }
                        } finally {
                            rs.close();
                        }
                    } finally {
                        stmt.close();
                    }
                } finally {
                    conn.close();
                }
            } catch (SQLException e) {
                throw new AMQStoreException("Error writing binding for AMQQueue with name " + queue.getNameShortString() + " to exchange "
                                            + exchange.getNameShortString() + " to database: " + e.getMessage(), e);
            }

        }


    }

    public void unbindQueue(Exchange exchange, AMQShortString routingKey, AMQQueue queue,
                            FieldTable args)
            throws AMQStoreException {
        Connection conn = null;

        try {
            conn = newAutoCommitConnection();
            // exchange_name varchar(255) not null, queue_name varchar(255) not null, binding_key varchar(255), arguments blob
            PreparedStatement stmt = conn.prepareStatement(DELETE_FROM_BINDINGS);
            stmt.setString(1, exchange.getNameShortString().toString());
            stmt.setString(2, queue.getNameShortString().toString());
            stmt.setString(3, routingKey == null ? null : routingKey.toString());

            int result = stmt.executeUpdate();
            stmt.close();

            if (result != 1) {
                throw new AMQStoreException("Queue binding for queue with name " + queue.getNameShortString() + " to exchange "
                                            + exchange.getNameShortString() + "  not found");
            }
        } catch (SQLException e) {
            throw new AMQStoreException("Error removing binding for AMQQueue with name " + queue.getNameShortString() + " to exchange "
                                        + exchange.getNameShortString() + " in database: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    _logger.error(e);
                }
            }

        }


    }

    public void createQueue(AMQQueue queue) throws AMQStoreException {
        createQueue(queue, null);
    }

    public void createQueue(AMQQueue queue, FieldTable arguments) throws AMQStoreException {
        _logger.debug("public void createQueue(AMQQueue queue = " + queue + "): called");

        if (_state != State.RECOVERING) {
            try {
                Connection conn = newAutoCommitConnection();

                PreparedStatement stmt = conn.prepareStatement(FIND_QUEUE);
                try {
                    stmt.setString(1, queue.getNameShortString().toString());
                    ResultSet rs = stmt.executeQuery();
                    try {

                        // If we don't have any data in the result set then we can add this queue
                        if (!rs.next()) {
                            PreparedStatement insertStmt = conn.prepareStatement(INSERT_INTO_QUEUE);

                            try {
                                String owner = queue.getOwner() == null ? null : queue.getOwner().toString();

                                insertStmt.setString(1, queue.getNameShortString().toString());
                                insertStmt.setString(2, owner);
                                insertStmt.setBoolean(3, queue.isExclusive());

                                final byte[] underlying;
                                if (arguments != null) {
                                    underlying = arguments.getDataAsBytes();
                                } else {
                                    underlying = new byte[0];
                                }

                                ByteArrayInputStream bis = new ByteArrayInputStream(underlying);
                                insertStmt.setBinaryStream(4, bis, underlying.length);

                                insertStmt.execute();
                            } finally {
                                insertStmt.close();
                            }
                        }
                    } finally {
                        rs.close();
                    }
                } finally {
                    stmt.close();
                }
                conn.close();

            } catch (SQLException e) {
                throw new AMQStoreException("Error writing AMQQueue with name " + queue.getNameShortString() + " to database: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Updates the specified queue in the persistent store, IF it is already present. If the queue
     * is not present in the store, it will not be added.
     * <p/>
     * NOTE: Currently only updates the exclusivity.
     *
     * @param queue The queue to update the entry for.
     * @throws org.apache.qpid.AMQStoreException
     *          If the operation fails for any reason.
     */


    public void updateQueue(final AMQQueue queue) throws AMQStoreException {
        if (_state != State.RECOVERING) {
            try {
                Connection conn = newAutoCommitConnection();

                try {
                    PreparedStatement stmt = conn.prepareStatement(FIND_QUEUE);
                    try {
                        stmt.setString(1, queue.getNameShortString().toString());

                        ResultSet rs = stmt.executeQuery();
                        try {
                            if (rs.next()) {
                                PreparedStatement stmt2 = conn.prepareStatement(UPDATE_QUEUE_EXCLUSIVITY);
                                try {
                                    stmt2.setBoolean(1, queue.isExclusive());
                                    stmt2.setString(2, queue.getNameShortString().toString());

                                    stmt2.execute();
                                } finally {
                                    stmt2.close();
                                }
                            }
                        } finally {
                            rs.close();
                        }
                    } finally {
                        stmt.close();
                    }
                } finally {
                    conn.close();
                }
            } catch (SQLException e) {
                throw new AMQStoreException("Error updating AMQQueue with name " + queue.getNameShortString() + " to database: " + e.getMessage(), e);
            }
        }

    }

/*
*
     * Convenience method to create a new Connection configured for TRANSACTION_READ_COMMITED
     * isolation and with auto-commit transactions enabled.
*/


    private Connection newAutoCommitConnection() throws SQLException {
        final Connection connection = newConnection();
        try {
            connection.setAutoCommit(true);
        } catch (SQLException sqlEx) {

            try {
                connection.close();
            } finally {
                throw sqlEx;
            }
        }

        return connection;
    }

    /**
     * Convenience method to create a new Connection configured for TRANSACTION_READ_COMMITED
     * isolation and with auto-commit transactions disabled.
     *
     * @return sql connection
     * @throws java.sql.SQLException
     */
    private Connection newConnection() throws SQLException {
        final Connection connection = _basicDataSource.getConnection();
        try {
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        } catch (SQLException sqlEx) {
            try {
                connection.close();
            } finally {
                throw sqlEx;
            }
        }
        return connection;
    }


    public void removeQueue(final AMQQueue queue) throws AMQStoreException {
        AMQShortString name = queue.getNameShortString();
        _logger.debug("public void removeQueue(AMQShortString name = " + name + "): called");
        Connection conn = null;

        try {
            conn = newAutoCommitConnection();
            PreparedStatement stmt = conn.prepareStatement(DELETE_FROM_QUEUE);
            stmt.setString(1, name.toString());
            int results = stmt.executeUpdate();
            stmt.close();

            if (results == 0) {
                throw new AMQStoreException("Queue " + name + " not found");
            }
        } catch (SQLException e) {
            throw new AMQStoreException("Error deleting AMQQueue with name " + name + " from database: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    _logger.error(e);
                }
            }

        }


    }


    public Transaction newTransaction() {
        return new JDBCTransaction();
    }

    public void enqueueMessage(ConnectionWrapper connWrapper, final TransactionLogResource queue,
                               Long messageId) throws AMQStoreException {
        String name = queue.getResourceName();

        Connection conn = connWrapper.getConnection();


        try {
            if (_logger.isDebugEnabled()) {
                _logger.debug("Enqueuing message " + messageId + " on queue " + name + "[Connection" + conn + "]");
            }

            PreparedStatement stmt = conn.prepareStatement(INSERT_INTO_QUEUE_ENTRY);
            try {
                stmt.setString(1, name);
                stmt.setLong(2, messageId);
                stmt.executeUpdate();
            } finally {
                stmt.close();
            }
        } catch (SQLException e) {
            _logger.error("Failed to enqueue: " + e.getMessage(), e);
            throw new AMQStoreException("Error writing enqueued message with id " + messageId + " for queue " + name
                                        + " to database", e);
        }

    }

    public void dequeueMessage(ConnectionWrapper connWrapper, final TransactionLogResource queue,
                               Long messageId) throws AMQStoreException {
        String name = queue.getResourceName();


        Connection conn = connWrapper.getConnection();


        try {
            PreparedStatement stmt = conn.prepareStatement(DELETE_FROM_QUEUE_ENTRY);
            try {
                stmt.setString(1, name);
                stmt.setLong(2, messageId);
                int results = stmt.executeUpdate();


                if (results != 1) {
                    throw new AMQStoreException("Unable to find message with id " + messageId + " on queue " + name);
                }

                if (_logger.isDebugEnabled()) {
                    _logger.debug("Dequeuing message " + messageId + " on queue " + name);//+ "[Connection" + conn + "]");
                }
            } finally {
                stmt.close();
            }
        } catch (SQLException e) {
            _logger.error("Failed to dequeue: " + e.getMessage(), e);
            throw new AMQStoreException("Error deleting enqueued message with id " + messageId + " for queue " + name
                                        + " from database", e);
        }

    }

    private static final class ConnectionWrapper {
        private final Connection _connection;

        public ConnectionWrapper(Connection conn) {
            _connection = conn;
        }

        public Connection getConnection() {
            return _connection;
        }
    }


    public void commitTran(ConnectionWrapper connWrapper) throws AMQStoreException {

        try {
            Connection conn = connWrapper.getConnection();
            conn.commit();

            if (_logger.isDebugEnabled()) {
                _logger.debug("commit tran completed");
            }

            conn.close();
        } catch (SQLException e) {
            throw new AMQStoreException("Error commit tx: " + e.getMessage(), e);
        } finally {

        }
    }

    public StoreFuture commitTranAsync(ConnectionWrapper connWrapper) throws AMQStoreException {
        commitTran(connWrapper);
        return new StoreFuture() {
            public boolean isComplete() {
                return true;
            }

            public void waitForCompletion() {

            }
        };

    }

    public void abortTran(ConnectionWrapper connWrapper) throws AMQStoreException {
        if (connWrapper == null) {
            throw new AMQStoreException("Fatal internal error: transactional context is empty at abortTran");
        }

        if (_logger.isDebugEnabled()) {
            _logger.debug("abort tran called: " + connWrapper.getConnection());
        }

        try {
            Connection conn = connWrapper.getConnection();
            conn.rollback();
            conn.close();
        } catch (SQLException e) {
            throw new AMQStoreException("Error aborting transaction: " + e.getMessage(), e);
        }

    }

    public Long getNewMessageId() {
        return _messageId.incrementAndGet();
    }


    private void storeMetaData(Connection conn, long messageId, StorableMessageMetaData metaData)
            throws SQLException {
        if (_logger.isDebugEnabled()) {
            _logger.debug("Adding metadata for message " + messageId);
        }

        PreparedStatement stmt = conn.prepareStatement(INSERT_INTO_META_DATA);
        try {
            stmt.setLong(1, messageId);

            final int bodySize = 1 + metaData.getStorableSize();
            byte[] underlying = new byte[bodySize];
            underlying[0] = (byte) metaData.getType().ordinal();
            ByteBuffer buf = ByteBuffer.wrap(underlying);
            buf.position(1);
            buf = buf.slice();

            metaData.writeToBuffer(0, buf);
            ByteArrayInputStream bis = new ByteArrayInputStream(underlying);
            try {
                stmt.setBinaryStream(2, bis, underlying.length);
                int result = stmt.executeUpdate();

                if (result == 0) {
                    throw new RuntimeException("Unable to add meta data for message " + messageId);
                }
            } finally {
                try {
                    bis.close();
                } catch (IOException e) {

                    throw new SQLException(e);
                }
            }

        } finally {
            stmt.close();
        }

    }


    private void recoverMessages(MessageStoreRecoveryHandler recoveryHandler) throws SQLException {
        Connection conn = newAutoCommitConnection();
        try {
            MessageStoreRecoveryHandler.StoredMessageRecoveryHandler messageHandler = recoveryHandler.begin();

            Statement stmt = conn.createStatement();
            try {
                ResultSet rs = stmt.executeQuery(SELECT_ALL_FROM_META_DATA);
                try {

                    long maxId = 0;

                    while (rs.next()) {

                        long messageId = rs.getLong(1);
                        Blob dataAsBlob = rs.getBlob(2);

                        if (messageId > maxId) {
                            maxId = messageId;
                        }

                        byte[] dataAsBytes = dataAsBlob.getBytes(1, (int) dataAsBlob.length());
                        ByteBuffer buf = ByteBuffer.wrap(dataAsBytes);
                        buf.position(1);
                        buf = buf.slice();
                        MessageMetaDataType type = MessageMetaDataType.values()[dataAsBytes[0]];
                        StorableMessageMetaData metaData = type.getFactory().createMetaData(buf);
                        StoredJDBCMessage message = new StoredJDBCMessage(messageId, metaData, false);
                        messageHandler.message(message);
                    }

                    _messageId.set(maxId);

                    messageHandler.completeMessageRecovery();
                } finally {
                    rs.close();
                }
            } finally {
                stmt.close();
            }
        } finally {
            conn.close();
        }
    }


    private void recoverQueueEntries(TransactionLogRecoveryHandler recoveryHandler)
            throws SQLException {
        Connection conn = newAutoCommitConnection();
        try {
            TransactionLogRecoveryHandler.QueueEntryRecoveryHandler queueEntryHandler = recoveryHandler.begin(this);

            Statement stmt = conn.createStatement();
            try {
                ResultSet rs = stmt.executeQuery(SELECT_FROM_QUEUE_ENTRY);
                try {
                    while (rs.next()) {

                        String queueName = rs.getString(1);
                        long messageId = rs.getLong(2);
                        queueEntryHandler.queueEntry(queueName, messageId);
                    }
                } finally {
                    rs.close();
                }
            } finally {
                stmt.close();
            }

            queueEntryHandler.completeQueueEntryRecovery();
        } finally {
            conn.close();
        }
    }

    StorableMessageMetaData getMetaData(long messageId) throws SQLException {

        Connection conn = newAutoCommitConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement(SELECT_FROM_META_DATA);
            try {
                stmt.setLong(1, messageId);
                ResultSet rs = stmt.executeQuery();
                try {

                    if (rs.next()) {
                        Blob dataAsBlob = rs.getBlob(1);

                        byte[] dataAsBytes = dataAsBlob.getBytes(1, (int) dataAsBlob.length());
                        ByteBuffer buf = ByteBuffer.wrap(dataAsBytes);
                        buf.position(1);
                        buf = buf.slice();
                        MessageMetaDataType type = MessageMetaDataType.values()[dataAsBytes[0]];

                        return type.getFactory().createMetaData(buf);
                    } else {
                        throw new RuntimeException("Meta data not found for message with id " + messageId);
                    }
                } finally {
                    rs.close();
                }
            } finally {
                stmt.close();
            }
        } finally {
            conn.close();
        }
    }


    private void addContent(Connection conn, long messageId, int offset, ByteBuffer src) {
        if (_logger.isDebugEnabled()) {
            _logger.debug("Adding content chunk offset " + offset + " for message " + messageId);
        }

        try {
            src = src.slice();

            byte[] chunkData = new byte[src.limit()];
            src.duplicate().get(chunkData);

            PreparedStatement stmt = conn.prepareStatement(INSERT_INTO_MESSAGE_CONTENT);
            stmt.setLong(1, messageId);
            stmt.setInt(2, offset);
            stmt.setInt(3, offset + chunkData.length);


/*// this would be the Java 6 way of doing things
            Blob dataAsBlob = conn.createBlob();
            dataAsBlob.setBytes(1L, chunkData);
            stmt.setBlob(3, dataAsBlob);*/


            ByteArrayInputStream bis = new ByteArrayInputStream(chunkData);
            stmt.setBinaryStream(4, bis, chunkData.length);
            stmt.executeUpdate();
            stmt.close();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e1) {

                }
            }

            throw new RuntimeException("Error adding content chunk offset " + offset + " for message " + messageId + ": " + e.getMessage(), e);
        }

    }


    public int getContent(long messageId, int offset, ByteBuffer dst) {
        Connection conn = null;


        try {
            conn = newAutoCommitConnection();

            PreparedStatement stmt = conn.prepareStatement(SELECT_FROM_MESSAGE_CONTENT);
            stmt.setLong(1, messageId);
            stmt.setInt(2, offset);
            stmt.setInt(3, offset + dst.remaining());
            ResultSet rs = stmt.executeQuery();

            int written = 0;

            while (rs.next()) {
                int offsetInMessage = rs.getInt(1);
                Blob dataAsBlob = rs.getBlob(2);

                final int size = (int) dataAsBlob.length();
                byte[] dataAsBytes = dataAsBlob.getBytes(1, size);

                int posInArray = offset + written - offsetInMessage;
                int count = size - posInArray;
                if (count > dst.remaining()) {
                    count = dst.remaining();
                }
                dst.put(dataAsBytes, posInArray, count);
                written += count;

                if (dst.remaining() == 0) {
                    break;
                }
            }

            stmt.close();
            conn.close();
            return written;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e1) {

                }
            }

            throw new RuntimeException("Error retrieving content from offset " + offset + " for message " + messageId + ": " + e.getMessage(), e);
        }


    }

    public boolean isPersistent() {
        return true;
    }


    private synchronized void stateTransition(State requiredState, State newState)
            throws AMQStoreException {
        if (_state != requiredState) {
            throw new AMQStoreException("Cannot transition to the state: " + newState + "; need to be in state: " + requiredState
                                        + "; currently in state: " + _state);
        }

        _state = newState;
    }


    private class JDBCTransaction implements Transaction {
        private final ConnectionWrapper _connWrapper;


        private JDBCTransaction() {
            try {
                _connWrapper = new ConnectionWrapper(newConnection());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void enqueueMessage(TransactionLogResource queue, Long messageId)
                throws AMQStoreException {
            JDBCMessageStore.this.enqueueMessage(_connWrapper, queue, messageId);
        }

        @Override
        public void dequeueMessage(TransactionLogResource queue, Long messageId)
                throws AMQStoreException {
            JDBCMessageStore.this.dequeueMessage(_connWrapper, queue, messageId);

        }

        public void commitTran() throws AMQStoreException {
            JDBCMessageStore.this.commitTran(_connWrapper);
        }

        public StoreFuture commitTranAsync() throws AMQStoreException {
            return JDBCMessageStore.this.commitTranAsync(_connWrapper);
        }

        public void abortTran() throws AMQStoreException {
            JDBCMessageStore.this.abortTran(_connWrapper);
        }
    }

    private class StoredJDBCMessage implements StoredMessage {

        private final long _messageId;
        private volatile SoftReference<StorableMessageMetaData> _metaDataRef;
        private Connection _conn;

        StoredJDBCMessage(long messageId, StorableMessageMetaData metaData) {
            this(messageId, metaData, true);
        }


        StoredJDBCMessage(long messageId,
                          StorableMessageMetaData metaData, boolean persist) {
            try {
                _messageId = messageId;

                _metaDataRef = new SoftReference<StorableMessageMetaData>(metaData);
                if (persist) {
                    _conn = newConnection();
                    storeMetaData(_conn, messageId, metaData);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

        }

        public StorableMessageMetaData getMetaData() {
            StorableMessageMetaData metaData = _metaDataRef.get();
            if (metaData == null) {
                try {
                    metaData = JDBCMessageStore.this.getMetaData(_messageId);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                _metaDataRef = new SoftReference<StorableMessageMetaData>(metaData);
            }

            return metaData;
        }

        public long getMessageNumber() {
            return _messageId;
        }

        public void addContent(int offsetInMessage, ByteBuffer src) {
            JDBCMessageStore.this.addContent(_conn, _messageId, offsetInMessage, src);
        }

        public int getContent(int offsetInMessage, ByteBuffer dst) {
            return JDBCMessageStore.this.getContent(_messageId, offsetInMessage, dst);
        }

        public StoreFuture flushToStore() {
            try {
                if (_conn != null) {
                    if (_logger.isDebugEnabled()) {
                        _logger.debug("Flushing message " + _messageId + " to store");
                    }

                    _conn.commit();
                    _conn.close();
                }
            } catch (SQLException e) {
                if (_logger.isDebugEnabled()) {
                    _logger.debug("Error when trying to flush message " + _messageId + " to store: " + e);
                }
                throw new RuntimeException(e);
            } finally {
                _conn = null;
            }
            return IMMEDIATE_FUTURE;
        }

        public void remove() {
            flushToStore();
            JDBCMessageStore.this.removeMessage(_messageId);
        }
    }

}

