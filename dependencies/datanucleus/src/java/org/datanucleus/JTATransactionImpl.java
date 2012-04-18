/**********************************************************************
Copyright (c) 2006 Jorg von Frantzius and others. All rights reserved.
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
2006 Andy Jefferson - localised, adapted to latest CVS
2007 GUido Anzuoni - move TX Manager lookup to Context
2008 Jorg von Frantzius - Fix bugs and test with JBOSS 4.0.3
2009 Guido Anzuoni - changes to allow PM close in afterCompletion
    ...
**********************************************************************/
package org.datanucleus;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.NotSupportedException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import org.datanucleus.exceptions.NucleusException;
import org.datanucleus.store.ExecutionContext;
import org.datanucleus.store.connection.ConnectionFactory;
import org.datanucleus.store.connection.ConnectionResourceType;
import org.datanucleus.transaction.NucleusTransactionException;
import org.datanucleus.util.NucleusLogger;

/**
 * A transaction that is synchronized with a Java Transaction Service (JTA) transaction.
 * Works only in a J2EE environments where a TranactionManager is present
 * <p>
 * When this feature is turned on, transactions must be controlled using javax.transaction.UserTransaction,
 * not e.g. using PM.currentTransaction().begin(). Should also work for SessionBeans, as 
 * per spec UserTransaction reflects SessionBean-based tx demarcation. 
 * 
 * {@link org.datanucleus.Transaction}
 * TODO Merge functionality with JTAJCATransactionImpl when J�rg/Erik document what each are providing.
 */
public class JTATransactionImpl extends TransactionImpl implements Synchronization 
{
    private enum JoinStatus {
        NO_TXN, IMPOSSIBLE, JOINED
    }
    
    /** TransactionManager. * */
    private TransactionManager tm;

    /** JTA txn we currently are synced with. Null when no JTA transaction active or not yet detected. */
    private javax.transaction.Transaction jtaTx;

    protected JoinStatus joinStatus = JoinStatus.NO_TXN;

    private UserTransaction userTransaction;

    /**
     * Constructor.
     * @param ec ExecutionContext
     */
    JTATransactionImpl(ExecutionContext ec)
    {
        super(ec);

        // we only make sense in combination with ResourceType.JTA. Verify this has been set.
        PersistenceConfiguration conf = ec.getNucleusContext().getPersistenceConfiguration();
        if (!(ConnectionResourceType.JTA.toString().equalsIgnoreCase(conf.getStringProperty(
                ConnectionFactory.DATANUCLEUS_CONNECTION_RESOURCE_TYPE)) && 
            ConnectionResourceType.JTA.toString().equalsIgnoreCase(conf.getStringProperty(
                ConnectionFactory.DATANUCLEUS_CONNECTION2_RESOURCE_TYPE))))
        {
            throw new NucleusException("Internal error: either " +
                ConnectionFactory.DATANUCLEUS_CONNECTION_RESOURCE_TYPE + 
                " or " + ConnectionFactory.DATANUCLEUS_CONNECTION2_RESOURCE_TYPE + " have not been set to JTA, this should have happened " +
                		"automatically.");
        }

        // tell the TransactionManager to not do anything with the actual datastore connection
        // as it is managed by the JTA (commit(), rollback() etc)
        txnMgr.setContainerManagedConnections(true);
        tm = obtainTransactionManager();
        checkTransactionJoin();
    }

    protected void checkTransactionJoin()
    {
        if (joinStatus != JoinStatus.JOINED)
        {
            try
            {
                javax.transaction.Transaction txn = tm.getTransaction();
                int txnstat = tm.getStatus();
                if (jtaTx != null && !jtaTx.equals(txn))
                {
                    // changed transaction, clear saved jtaTxn, reset join status and reprocess
                    // it should happen only if joinStatus == JOIN_STATUS_IMPOSSIBLE
                    if (! (joinStatus == JoinStatus.IMPOSSIBLE))
                    {
                        throw new InternalError("JTA Transaction changed without being notified");
                    }
                    jtaTx = null;
                    joinStatus = JoinStatus.NO_TXN;
                    checkTransactionJoin();
                }
                else
                {
                    if (jtaTx == null)
                    {
                        jtaTx = txn;
                        boolean allow_join = canJoinTransaction(txnstat);
                        if (allow_join)
                        {
                            joinStatus = JoinStatus.IMPOSSIBLE;
                            execJoinTransaction();
                            joinStatus = JoinStatus.JOINED;
                        }
                        else
                        {
                            if (jtaTx != null)
                            {
                                joinStatus = JoinStatus.IMPOSSIBLE;
                            }
                        }
                    }
                }
            }
            catch (SystemException e)
            {
                throw new NucleusTransactionException(LOCALISER.msg("015026"), e);
            }
        }
    }

    private void execJoinTransaction()
    {
        try
        {
            jtaTx.registerSynchronization(this);
            boolean was_active = super.isActive();
            if (!was_active)
            {
                // the transaction is active here
                internalBegin();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new NucleusTransactionException("Cannot register Synchronization to a valid JTA Transaction");
        }
    }

    private boolean canJoinTransaction(int txnstat)
    {
        return txnstat == Status.STATUS_ACTIVE;
    }

    /**
     * Accessor for whether the transaction is active. The UserTransaction is considered active if its status is
     * anything other than {@link Status#STATUS_NO_TRANSACTION}, i.e. when the current thread is associated with a
     * JTA transaction.
     * @return Whether the transaction is active.
     */
    public boolean isActive()
    {
        // avoid JNDI lookups
        if (joinStatus == JoinStatus.JOINED)
        {
            // once we were able to join, then super.isActive() is in sync due
            // to the Synchronization callbacks
            return super.isActive();
        }
        else
        {
            checkTransactionJoin();
            return super.isActive() || joinStatus == JoinStatus.IMPOSSIBLE;
        }
    }

    /**
     * "16.1.3 Stateless Session Bean with Bean Managed Transactions": "acquiring a PersistenceManager without beginning
     * a UserTransaction results in the PersistenceManager being able to manage transaction boundaries via begin,
     * commit, and rollback methods on javax. jdo.Transaction. The PersistenceManager will automatically begin the User-
     * Transaction during javax.jdo.Transaction.begin and automatically commit the UserTransaction during
     * javax.jdo.Transaction.commit"
     */
    public void begin()
    {
        checkTransactionJoin();
        if (joinStatus != JoinStatus.NO_TXN)
        {
            throw new NucleusTransactionException("JTA Transaction is already active");
        }

        UserTransaction utx;
        try
        {
            utx = getUserTransaction();
        }
        catch (NamingException e)
        {
            throw ec.getApiAdapter().getUserExceptionForException("Failed to obtain UserTransaction", e);
        }

        try
        {
            utx.begin();
        }
        catch (NotSupportedException e)
        {
            throw ec.getApiAdapter().getUserExceptionForException("Failed to begin UserTransaction", e);
        }
        catch (SystemException e)
        {
            throw ec.getApiAdapter().getUserExceptionForException("Failed to begin UserTransaction", e);
        }

        checkTransactionJoin();
        if (joinStatus != JoinStatus.JOINED)
        {
            throw new NucleusTransactionException("Cannot join an auto started UserTransaction");
        }
        userTransaction = utx;
    }

    /**
     * Allow UserTransaction demarcation
     */
    public void commit()
    {
        if (userTransaction == null)
        {
            throw new NucleusTransactionException("No internal UserTransaction");
        }

        try
        {
            userTransaction.commit();
        }
        catch (Exception e)
        {
            throw ec.getApiAdapter().getUserExceptionForException("Failed to commit UserTransaction", e);
        }
        finally
        {
            userTransaction = null;
        }
    }

    /**
     * Allow UserTransaction demarcation
     */
    public void rollback()
    {
        if (userTransaction == null)
        {
            throw new NucleusTransactionException("No internal UserTransaction");
        }

        try
        {
            userTransaction.rollback();
        }
        catch (Exception e)
        {
            throw ec.getApiAdapter().getUserExceptionForException("Failed to rollback UserTransaction", e);
        }
        finally
        {
            userTransaction = null;
        }

    }

    /**
     * Allow UserTransaction demarcation
     */
    public void setRollbackOnly()
    {
        if (userTransaction == null)
        {
            throw new NucleusTransactionException("No internal UserTransaction");
        }

        try
        {
            userTransaction.setRollbackOnly();
        }
        catch (Exception e)
        {
            throw ec.getApiAdapter().getUserExceptionForException("Failed to rollback-only UserTransaction", e);
        }
    }

    // ------------------- Methods to get the JTA transaction for synchronising --------------------------

    /**
     * Accessor for the JTA TransactionManager. Unfortunately, before J2EE 5 there is no specified way to do it, only
     * appserver-specific ways. Taken from http://www.onjava.com/pub/a/onjava/2005/07/20/transactions.html.
     * <P>
     * In J2EE 5, we'll be able to use the following
     * https://glassfish.dev.java.net/nonav/javaee5/api/s1as-javadocs/javax
     * /transaction/TransactionSynchronizationRegistry.html
     * @return The TransactionManager
     * @throws NucleusTransactionException if an error occurs obtaining the transaction manager
     */
    private TransactionManager obtainTransactionManager()
    {
        TransactionManager tm = ec.getNucleusContext().getJtaTransactionManager();
        if (tm == null)
        {
            throw new NucleusTransactionException(LOCALISER.msg("015030"));
        }
        else
        {
            return tm;
        }
    }

    private static boolean INSIDE_JBOSS = System.getProperty("jboss.server.name") != null;

    private UserTransaction getUserTransaction() throws NamingException
    {
        Context ctx = new InitialContext();
        UserTransaction ut;
        if (INSIDE_JBOSS)
        {
            // JBOSS unfortunately doesn't always provide UserTransaction at the
            // J2EE standard location
            // see e.g. http://docs.jboss.org/admin-devel/Chap4.html
            ut = (UserTransaction) ctx.lookup("UserTransaction");
        }
        else
        {
            ut = (UserTransaction) ctx.lookup("java:comp/UserTransaction");
        }

        return ut;
    }

    // ----------------- Implementation of javax.transaction.Synchronization -------------------

    /**
     * The beforeCompletion method is called by the transaction manager prior to the start of the two-phase 
     * transaction commit process. This is not called in JCA mode
     */
    public void beforeCompletion()
    {
        
        boolean success = false;
        try
        {
            flush();
            // internalPreCommit() can lead to new updates performed by usercode  
            // in the Synchronization.beforeCompletion() callback
            internalPreCommit();
            flush();
            success = true;
        }
        finally
        {
            if (!success) 
            {
                // TODO Localise these messages
                NucleusLogger.TRANSACTION.error("Exception flushing work in JTA transaction. Mark for rollback");
                try
                {
                    jtaTx.setRollbackOnly();
                }
                catch (Exception e)
                {
                    NucleusLogger.TRANSACTION.fatal(
                        "Cannot mark transaction for rollback after exception in beforeCompletion. PersistenceManager might be in inconsistent state", e);
                }
            }
        }
    }

    /**
     * This method is called by the transaction manager after the transaction is committed or rolled back.
     * Must be synchronized because some callees expect to be owner of this object's monitor 
     * (internalPostCommit() calls closeSQLConnection() which calls notifyAll()).
     * @param status The status
     */
    public synchronized void afterCompletion(int status)
    {
        boolean success = false;
        try
        {
            if (status == Status.STATUS_ROLLEDBACK)
            {
                super.rollback();
            }
            else if (status == Status.STATUS_COMMITTED)
            {
                internalPostCommit();
            }
            else
            {
                // this method is called after*Completion*(), so we can expect
                // not to be confronted with intermediate status codes
                // TODO Localise this
                NucleusLogger.TRANSACTION.fatal("Received unexpected transaction status + " + status);
            }
            success = true;
        }
        finally
        {
            jtaTx = null;
            joinStatus = JoinStatus.NO_TXN;
            if (!success)
            {                
                // TODO Localise this
                NucleusLogger.TRANSACTION.error("Exception during afterCompletion in JTA transaction. PersistenceManager might be in inconsistent state");
            }
        }

        if (active)
        {
            throw new NucleusTransactionException("internal error, must not be active after afterCompletion()!");
        }
    }
}