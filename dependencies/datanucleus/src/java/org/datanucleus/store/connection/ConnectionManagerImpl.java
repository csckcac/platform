/**********************************************************************
Copyright (c) 2007 Erik Bengtson and others. All rights reserved.
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License. 

Contributors:
2007 Andy Jefferson - javadocs, formatted, copyrighted
2007 Andy Jefferson - added lock/unlock/hasConnection/hasLockedConnection and enlisting
    ...
**********************************************************************/
package org.datanucleus.store.connection;

import java.util.HashMap;
import java.util.Map;

import javax.transaction.xa.XAResource;

import org.datanucleus.NucleusContext;
import org.datanucleus.TransactionEventListener;
import org.datanucleus.exceptions.NucleusUserException;
import org.datanucleus.management.ManagementManager;
import org.datanucleus.management.ManagementServer;
import org.datanucleus.management.runtime.ConnectionManagerRuntime;
import org.datanucleus.transaction.Transaction;
import org.datanucleus.util.ClassUtils;
import org.datanucleus.util.Localiser;
import org.datanucleus.util.NucleusLogger;

/**
 * Manager of connections for a datastore, allowing ManagedConnection pooling, enlistment in transaction.
 * The pool caches one connection per poolKey object.
 * The "allocateConnection" method can create connections and enlist them (like most normal persistence operations need)
 * or create a connection and return it without enlisting it into a transaction, for example the connections used to
 * generate object identity, create the database schema or obtaining the schema metadata.
 * 
 * Connections can be locked per object poolKey basis. Locking of connections is used to
 * handle the connection over to the user application. A locked connection denies any further
 * access to the datastore, until the user application unlock it.
 */
public class ConnectionManagerImpl implements ConnectionManager
{
    /** Localisation of messages. */
    protected static final Localiser LOCALISER=Localiser.getInstance("org.datanucleus.Localisation",
        org.datanucleus.ClassConstants.NUCLEUS_CONTEXT_LOADER);

    /** Context for this connection manager. */
    NucleusContext nucleusContext;
    
    ManagedConnectionPool connectionPool = new ManagedConnectionPool();

    /** Connection Runtime. Used when providing management of services. */
    ConnectionManagerRuntime connMgrRuntime = null;

    /** Registry of factories for connections, keyed by their symbolic name. */
    HashMap<String, ConnectionFactory> factories = new HashMap<String, ConnectionFactory>();

    /**
     * Whether connection pooling is enabled
     */
    boolean connectionPoolEnabled = true;
    
    /**
     * Constructor.
     * @param context Context for this manager.
     */
    public ConnectionManagerImpl(NucleusContext context)
    {
        this.nucleusContext = context;

        if (context.getJMXManager() != null)
        {
            // register MBean in MbeanServer
            ManagementManager mgmtMgr = this.nucleusContext.getJMXManager();
            ManagementServer mgmtServer = mgmtMgr.getManagementServer();
            connMgrRuntime = new ConnectionManagerRuntime();
            String mbeanName = mgmtMgr.getDomainName() + ":InstanceName=" + mgmtMgr.getInstanceName() +
                ",Type=" + ClassUtils.getClassNameForClass(connMgrRuntime.getClass()) + 
                ",Name=ConnectionManagerRuntime";
            mgmtServer.registerMBean(connMgrRuntime, mbeanName);
        }
    }

    /**
     * Pool of managed connections keyed by poolKey objects.
     * Each "poolKey" key has its own pool of ManagedConnection's
     */
    class ManagedConnectionPool
    {
        /**
         * Connection pool keeps a reference to a connection for each "poolKey" object (and so the connection
         * used by each transaction).
         * This permits reuse of connections in the same transaction, but not at same time!!!
         * ManagedConnection must be released to return to pool.
         * On transaction commit/rollback, connection pool is cleared
         *
         * For each combination of "poolKey"-ConnectionFactory there is 0 or 1 ManagedConnection
         */
        Map<Object, Map<ConnectionFactory, ManagedConnection>> connectionsPool = new HashMap();

        /**
         * Remove from pool
         * @param factory The factory is the nested key
         * @param om The om is the key for the ManagedConnection
         */
        public void removeManagedConnection(ConnectionFactory factory, Object poolKey)
        {
            synchronized (connectionsPool)
            {
                Map connectionsForOM = connectionsPool.get(poolKey);
                if (connectionsForOM != null)
                {
                    if (connectionsForOM.remove(factory) != null && connMgrRuntime != null)
                    {
                        connMgrRuntime.decrementActiveConnections();
                    }

                    if (connectionsForOM.size() == 0)
                    {
                        // No connections remaining for this OM so remove the entry for the "poolKey"
                        connectionsPool.remove(poolKey);
                    }
                }
            }
        }
        
        /**
         * Object a ManagedConnection from pool
         * @param factory
         * @param poolKey
         * @return
         */
        public ManagedConnection getManagedConnection(ConnectionFactory factory, Object poolKey)
        {
            synchronized (connectionsPool)
            {
                Map connectionsForOM = connectionsPool.get(poolKey);
                if (connectionsForOM == null)
                {
                    return null;
                }
                //obtain a ManagedConnection for an specific ConnectionFactory
                ManagedConnection mconn = (ManagedConnection) connectionsForOM.get(factory);
                if (mconn != null)
                {
                    if (mconn.isLocked())
                    {
                        // Enlisted connection that is locked so throw exception
                        throw new NucleusUserException(LOCALISER.msg("009000"));
                    }                        
                    // Already registered enlisted connection present so return it
                    return mconn;
                }
            }
            return null;
        }
        
        public void putManagedConnection(ConnectionFactory factory, Object poolKey, ManagedConnection mconn)
        {
            synchronized (connectionsPool)
            {
                Map connectionsForOM = connectionsPool.get(poolKey);
                if (connectionsForOM == null)
                {
                    connectionsForOM = new HashMap();
                    connectionsPool.put(poolKey, connectionsForOM);
                }
                if (connectionsForOM.put(factory, mconn) == null && connMgrRuntime != null)
                {
                    connMgrRuntime.incrementActiveConnections();
                }
            }
        }
    }

    /**
     * Method to close all pooled connections for the specified key of the specified factory.
     * @param factory The factory
     * @param poolKey The key in the pool
     */
    public void closeAllConnections(final ConnectionFactory factory, final Object poolKey)
    {
        if (poolKey != null && connectionPoolEnabled)
        {
            ManagedConnection mconnFromPool = connectionPool.getManagedConnection(factory, poolKey);
            if (mconnFromPool != null)
            {
                // Already registered enlisted connection present so return it
                if (NucleusLogger.CONNECTION.isDebugEnabled())
                {
                    NucleusLogger.CONNECTION.debug("Connection found in the pool : " + mconnFromPool + 
                        " for key=" + poolKey + " in factory=" + factory + " but owner object closing so closing connection");
                }
                mconnFromPool.close();
            }
        }
    }

    /**
     * Method to return a connection for this "poolKey".
     * If a connection for the "poolKey" exists in the cache will return it.
     * If no connection exists will create a new one using the ConnectionFactory.
     * @param factory ConnectionFactory it relates to
     * @param poolKey the object that is bound the connection during its lifecycle
     * @param options Options for the connection (e.g isolation). These will override those of the txn itself
     * @return The ManagedConnection
     */
    public ManagedConnection allocateConnection(final ConnectionFactory factory, final Object poolKey,
            final org.datanucleus.Transaction transaction, Map options)
    {
        if (poolKey != null && connectionPoolEnabled)
        {
            ManagedConnection mconnFromPool = connectionPool.getManagedConnection(factory, poolKey);
            if (mconnFromPool != null)
            {
                // Already registered enlisted connection present so return it
                if (NucleusLogger.CONNECTION.isDebugEnabled())
                {
                    NucleusLogger.CONNECTION.debug("Connection found in the pool : " + mconnFromPool + 
                        " for key=" + poolKey + " in factory=" + factory);
                }
                return mconnFromPool;
            }
        }

        // Create new connection
        Map<String, Object> txOptions = options;
        if (options == null && transaction != null)
        {
            txOptions = transaction.getOptions();
        }
        ManagedConnection mconn = factory.createManagedConnection(poolKey, txOptions);
        configureManagedConnectionListener(poolKey, mconn, factory);

        // Enlist the connection in this transaction
        if (poolKey != null)
        {
            if (transaction.isActive())
            {
                configureTransactionEventListener(transaction,mconn);
                Transaction tx = nucleusContext.getTransactionManager().getTransaction(poolKey);
                //must be set before getting the XAResource
                mconn.setManagedResource();
                enlistResource(mconn, tx, options);
            }

            // Register this connection against the "poolKey" - connection is valid
            if (NucleusLogger.CONNECTION.isDebugEnabled())
            {
                NucleusLogger.CONNECTION.debug("Connection added to the pool : " + mconn + 
                    " for key=" + poolKey + " in factory=" + factory);
            }
            if (connectionPoolEnabled)
            {
                connectionPool.putManagedConnection(factory, poolKey, mconn);
            }
        }

        return mconn;
    }

    /**
     * 
     * @param poolKey the object to which the ManagedConnection is bound to
     * @param mconn
     * @param factory
     */
    private void configureManagedConnectionListener(final Object poolKey, final ManagedConnection mconn, 
            final ConnectionFactory factory)
    {
        mconn.addListener(new ManagedConnectionResourceListener()
        {
            public void transactionFlushed() {}
            public void transactionPreClose() {}
            public void managedConnectionPreClose() {}
            public void managedConnectionPostClose()
            {
                if (poolKey != null)
                {
                    if (NucleusLogger.CONNECTION.isDebugEnabled())
                    {
                        NucleusLogger.CONNECTION.debug("Connection removed from the pool : " + mconn +
                            " for key=" + poolKey + " in factory=" + factory);
                    }
                    if (connectionPoolEnabled)
                    {
                        connectionPool.removeManagedConnection(factory, poolKey); // Connection closed so remove
                    }
                }
            }
            public void resourcePostClose() {}
        });
    }

    /**
     * Configure a TransactionEventListener that closes the managed connection when a 
     * transaction commits or rolls back
     * @param om
     * @param mconn
     */
    private void configureTransactionEventListener(final org.datanucleus.Transaction transaction,
            final ManagedConnection mconn)
    {
        // Add handler for any enlisted connection to the transaction so we know when to close it
        transaction.addTransactionEventListener(
            new TransactionEventListener()
            {
                public void transactionStarted() {}

                public void transactionRolledBack()
                {
                    try
                    {
                        mconn.close();
                    }
                    finally
                    {
                        transaction.removeTransactionEventListener(this);
                    }
                }

                public void transactionCommitted()
                {
                    try
                    {
                        mconn.close();
                    }
                    finally
                    {
                        transaction.removeTransactionEventListener(this);
                    }
                }

                public void transactionEnded()
                {
                    try
                    {
                        mconn.close();
                    }
                    finally
                    {
                        transaction.removeTransactionEventListener(this);
                    }
                }

                public void transactionPreCommit()
                {
                    if (mconn.isLocked())
                    {
                        // Enlisted connection that is locked so throw exception
                        throw new NucleusUserException(LOCALISER.msg("009000"));
                    }
                    mconn.transactionPreClose();
                }

                public void transactionPreRollBack()
                {
                    if (mconn.isLocked())
                    {
                        // Enlisted connection that is locked so throw exception
                        throw new NucleusUserException(LOCALISER.msg("009000"));
                    }
                    mconn.transactionPreClose();
                }

                public void transactionFlushed()
                {
                    mconn.transactionFlushed();
                }
            });
    }

    /**
     * Enlist the mconn in the transaction if using our transaction manager
     * @param mconn Connection
     * @param tx Transaction
     * @param options Any options
     */
    private void enlistResource(ManagedConnection mconn, Transaction tx, Map<String, Object> options)
    {
        XAResource res = mconn.getXAResource();
        if (res != null)
        {
            // Enlist the connection resource if has enlistable resource
            boolean enlistInLocalTM = true;
            if (options != null && options.get(ConnectionFactory.RESOURCE_TYPE_OPTION) != null &&
                ConnectionResourceType.JTA.toString().equalsIgnoreCase((String)options.get(ConnectionFactory.RESOURCE_TYPE_OPTION)))
            {
                //XAResource will be enlisted in an external JTA container,
                //so we don't enlist it in the internal Transaction Manager
                enlistInLocalTM = false;
            }
            if (enlistInLocalTM)
            {
                tx.enlistResource(res);
            }
        }
    }

    /**
     * Method to lookup a connection factory and create it if not yet existing.
     * @param name The lookup name "e.g "jdbc/tx"
     * @return The connection factory
     */
    public ConnectionFactory lookupConnectionFactory(String name)
    {
        return factories.get(name);
    }

    /**
     * Method to register a connection factory
     * @param name The lookup name "e.g "jdbc/tx"
     * @param factory The connection factory
     */
    public void registerConnectionFactory(String name, ConnectionFactory factory)
    {
        factories.put(name, factory);
    }

    /**
     * Disable binding objects to "poolKey" references, so automatically
     * disables the connection pooling 
     */
    public void disableConnectionPool()
    {
        connectionPoolEnabled = false;
    }
}