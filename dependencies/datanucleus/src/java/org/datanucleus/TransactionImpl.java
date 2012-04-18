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
    ...
**********************************************************************/
package org.datanucleus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.transaction.Status;
import javax.transaction.Synchronization;

import org.datanucleus.exceptions.NucleusDataStoreException;
import org.datanucleus.exceptions.NucleusException;
import org.datanucleus.exceptions.NucleusUserException;
import org.datanucleus.exceptions.TransactionActiveOnBeginException;
import org.datanucleus.exceptions.TransactionNotActiveException;
import org.datanucleus.store.ExecutionContext;
import org.datanucleus.transaction.HeuristicMixedException;
import org.datanucleus.transaction.HeuristicRollbackException;
import org.datanucleus.transaction.NucleusTransactionException;
import org.datanucleus.transaction.RollbackException;
import org.datanucleus.transaction.TransactionManager;
import org.datanucleus.transaction.TransactionUtils;
import org.datanucleus.util.Localiser;
import org.datanucleus.util.NucleusLogger;
import org.datanucleus.util.StringUtils;

/**
 * Implementation of a transaction for a datastore. {@link org.datanucleus.Transaction}
 */
public class TransactionImpl implements Transaction
{
    /** Localisation of messages. */
    protected static final Localiser LOCALISER = Localiser.getInstance("org.datanucleus.Localisation",
        org.datanucleus.ClassConstants.NUCLEUS_CONTEXT_LOADER);

    ExecutionContext ec;

    TransactionManager txnMgr;

    /** Whether the transaction is active. */
    boolean active = false;

    /** Flag for whether we are currently committing. */
    boolean committing;

    /** Synchronisation object, for committing and rolling back. */
    Synchronization sync;

    /** Whether retainValues is enabled. */
    protected boolean retainValues;

    /** Whether restoreValues is enabled. */
    protected boolean restoreValues;

    /** Whether the transaction is optimistic */
    protected boolean optimistic;

    /** Whether non-tx read is enabled. */
    protected boolean nontransactionalRead;

    /** Whether non-tx write is enabled. */
    protected boolean nontransactionalWrite;

    /** Whether the transaction is marked for rollback. JDO 2.0 section 13.4.5 */
    protected boolean rollbackOnly = false;

    /** Whether to serialise (lock) any read objects in this transaction. */
    protected Boolean serializeRead = null;

    /** Listeners for the lifecycle of the active transaction. **/
    private Set<TransactionEventListener> listenersPerTransaction = new HashSet();

    /** Listeners that apply to all transactions. **/
    private Set<TransactionEventListener> listeners = new HashSet();

    private Map<String, Object> options = new HashMap();

    /** start time of the transaction */
    long beginTime;

    /**
     * Constructor for a transaction for the specified ExecutionContext.
     * @param ec ExecutionContext
     */
    public TransactionImpl(ExecutionContext ec)
    {
        this.ec = ec;
        this.txnMgr = ec.getNucleusContext().getTransactionManager();

        PersistenceConfiguration config = ec.getNucleusContext().getPersistenceConfiguration();
        optimistic = config.getBooleanProperty("datanucleus.Optimistic");
        retainValues = config.getBooleanProperty("datanucleus.RetainValues");
        restoreValues = config.getBooleanProperty("datanucleus.RestoreValues");
        nontransactionalRead = config.getBooleanProperty("datanucleus.NontransactionalRead");
        nontransactionalWrite = config.getBooleanProperty("datanucleus.NontransactionalWrite");

        int isolationLevel = TransactionUtils.getTransactionIsolationLevelForName(
            config.getStringProperty("datanucleus.transactionIsolation"));
        setOption(Transaction.TRANSACTION_ISOLATION_OPTION, isolationLevel);

        // Locking of read objects in this transaction
        Boolean serialiseReadProp = config.getBooleanObjectProperty("datanucleus.SerializeRead");
        if (serialiseReadProp != null)
        {
            serializeRead = serialiseReadProp;
        }
        else
        {
            // Backwards compatibility
            Boolean rdbmsProp = config.getBooleanObjectProperty("datanucleus.rdbms.useUpdateLock");
            if (rdbmsProp != null)
            {
                serializeRead = rdbmsProp;
            }
        }
    }

    /**
     * Method to begin the transaction.
     */
    public void begin()
    {
        if (ec.getMultithreaded())
        {
            synchronized (this)
            {
                txnMgr.begin(ec);
            }
        }
        else
        {
            txnMgr.begin(ec);
        }
        internalBegin();
    }

    /**
     * Method to begin the transaction.
     */
    protected void internalBegin()
    {
        if (active)
        {
            throw new TransactionActiveOnBeginException(ec);
        }

        active = true;
        beginTime = System.currentTimeMillis();
        if (txnMgr.getTransactionRuntime() != null)
        {
            txnMgr.getTransactionRuntime().transactionStarted();
        }
        if (NucleusLogger.TRANSACTION.isDebugEnabled())
        {
            NucleusLogger.TRANSACTION.debug(LOCALISER.msg("015000", ec, "" + optimistic));
        }

        TransactionEventListener[] ls = getListenersForEvent();
        for (TransactionEventListener tel : ls)
        {
            tel.transactionStarted();
        }
    }

    /**
     * Method to flush the transaction.
     */
    public void flush()
    {
        try
        {
            TransactionEventListener[] ls = getListenersForEvent();
            for (TransactionEventListener tel : ls)
            {
                tel.transactionFlushed();
            }
        }
        catch (Throwable ex)
        {
            if (ex instanceof NucleusException)
            {
                throw (NucleusException)ex;
            }
            // Wrap all other exceptions in a NucleusTransactionException
            throw new NucleusTransactionException(LOCALISER.msg("015005"), ex);
        }
    }

    /**
     * Method to allow the transaction to flush any resources.
     */
    public void end()
    {
        try
        {
            flush();
        }
        finally
        {
            TransactionEventListener[] ls = getListenersForEvent();
            for (TransactionEventListener tel : ls)
            {
                tel.transactionEnded();
            }
        }
    }

    /**
     * Method to commit the transaction.
     */
    public void commit()
    {
        if (!isActive())
        {
            throw new TransactionNotActiveException();
        }

        // JDO 2.0 section 13.4.5 rollbackOnly functionality
        // It isn't clear from the spec if we are expected to do the rollback here.
        // The spec simply says that we throw an exception. This is assumed as meaning that the users code will catch
        // the exception and call rollback themselves. i.e we don't need to close the DB connection or set "active" to false.
        if (rollbackOnly)
        {
            // Throw an exception since can only exit via rollback
            if (NucleusLogger.TRANSACTION.isDebugEnabled())
            {
                NucleusLogger.TRANSACTION.debug(LOCALISER.msg("015020"));
            }

            throw new NucleusDataStoreException(LOCALISER.msg("015020")).setFatal();
        }

        long startTime = System.currentTimeMillis();
        boolean success = false;
        boolean canComplete = true; //whether the transaction can be completed
        List errors = new ArrayList();
        try
        {
            flush();
            internalPreCommit();
            internalCommit();
            success = true;
        }
        catch (RollbackException e)
        {
            //catch only RollbackException because user exceptions can be raised
            //in Transaction.Synchronization and they should cascade up to user code
            if (NucleusLogger.TRANSACTION.isDebugEnabled())
            {
                NucleusLogger.TRANSACTION.debug(StringUtils.getStringFromStackTrace(e));
            }            
            errors.add(e);
        }
        catch (HeuristicRollbackException e)
        {
            //catch only HeuristicRollbackException because user exceptions can be raised
            //in Transaction.Synchronization and they should cascade up to user code
            if (NucleusLogger.TRANSACTION.isDebugEnabled())
            {
                NucleusLogger.TRANSACTION.debug(StringUtils.getStringFromStackTrace(e));
            }            
            errors.add(e);
        }
        catch (HeuristicMixedException e)
        {
            //catch only HeuristicMixedException because user exceptions can be raised
            //in Transaction.Synchronization and they should cascade up to user code
            if (NucleusLogger.TRANSACTION.isDebugEnabled())
            {
                NucleusLogger.TRANSACTION.debug(StringUtils.getStringFromStackTrace(e));
            }            
            errors.add(e);
        }
        catch (NucleusUserException e)
        {
            //catch only NucleusUserException
            //they must be cascade up to user code and transaction is still alive
            if (NucleusLogger.TRANSACTION.isDebugEnabled())
            {
                NucleusLogger.TRANSACTION.debug(StringUtils.getStringFromStackTrace(e));
            }
            canComplete = false;
            throw e;
        }
        catch (NucleusException e)
        {
            //catch only NucleusException because user exceptions can be raised
            //in Transaction.Synchronization and they should cascade up to user code
            if (NucleusLogger.TRANSACTION.isDebugEnabled())
            {
                NucleusLogger.TRANSACTION.debug(StringUtils.getStringFromStackTrace(e));
            }            
            errors.add(e);
        }
        finally
        {
            if (canComplete)
            {
                try
                {
                    if (!success)
                    {
                        rollback();
                    }
                    else
                    {
                        internalPostCommit();
                    }
                }
                catch (Throwable e)
                {
                    errors.add(e);
                }
            }
        }
        if (errors.size() > 0)
        {
            throw new NucleusTransactionException(LOCALISER.msg("015007"), (Throwable[])errors.toArray(
                new Throwable[errors.size()]));
        }

        if (NucleusLogger.TRANSACTION.isDebugEnabled())
        {
            NucleusLogger.TRANSACTION.debug(LOCALISER.msg("015022", (System.currentTimeMillis() - startTime)));
        }
    }

    /**
     * Method to perform any pre-commit operations like flushing to the datastore, calling the users
     * "beforeCompletion", and general preparation for the commit.
     */
    protected void internalPreCommit()
    {
        committing = true;

        if (NucleusLogger.TRANSACTION.isDebugEnabled())
        {
            NucleusLogger.TRANSACTION.debug(LOCALISER.msg("015001", ec));
        }

        if (sync != null)
        {
            // JDO2 $13.4.3 Allow the user to perform any updates before we do loading of fields etc
            sync.beforeCompletion();
        }

        // Perform any pre-commit operations
        TransactionEventListener[] ls = getListenersForEvent();
        for (TransactionEventListener tel : ls)
        {
            tel.transactionPreCommit();
        }
    }
    
    /**
     * Internal commit, DataNucleus invokes it's own transaction manager implementation, if
     * an external transaction manager is not used.
     */
    protected void internalCommit()
    {
        // optimistic transactions that don't have dirty
        if (ec.getMultithreaded())
        {
            synchronized (this)
            {
                txnMgr.commit(ec);
            }
        }
        else
        {
            txnMgr.commit(ec);
        }
    }    

    /**
     * Method to rollback the transaction.
     */
    public void rollback()
    {
        if (!isActive())
        {
            throw new TransactionNotActiveException();
        }
        long startTime = System.currentTimeMillis();

        try
        {
            boolean canComplete = true; //whether the transaction can be completed
            committing = true;
            try
            {
                flush();
            }
            finally
            {
                //even if flush fails, we ignore and go ahead cleaning up and rolling back everything ahead...
                try
                {
                    internalPreRollback();
                }
                catch (NucleusUserException e)
                {
                    //catch only NucleusUserException
                    //they must be cascade up to user code and transaction is still alive
                    if (NucleusLogger.TRANSACTION.isDebugEnabled())
                    {
                        NucleusLogger.TRANSACTION.debug(StringUtils.getStringFromStackTrace(e));
                    }
                    canComplete = false;
                    throw e;
                }
                finally
                {
                    if (canComplete)
                    {
                        try
                        {
                            internalRollback();                          
                        }
                        finally
                        {
                            try
                            {
                                active = false;
                                if (txnMgr.getTransactionRuntime() != null)
                                {
                                    txnMgr.getTransactionRuntime().transactionRolledBack(
                                        System.currentTimeMillis()-beginTime);
                                }
                            }
                            finally
                            {
                                listenersPerTransaction.clear(); 
                                rollbackOnly = false; // Reset rollbackOnly flag
                                if (sync != null)
                                {
                                    sync.afterCompletion(Status.STATUS_ROLLEDBACK);
                                }
                            }
                        }
                    }
                }
            }
        }
        catch (NucleusUserException e)
        {
            throw e;
        }
        catch (NucleusException e)
        {
            throw new NucleusDataStoreException(LOCALISER.msg("015009"), e);
        }
        finally
        {
            committing = false;
        }

        if (NucleusLogger.TRANSACTION.isDebugEnabled())
        {
            NucleusLogger.TRANSACTION.debug(LOCALISER.msg("015023", (System.currentTimeMillis() - startTime)));
        }
    }

    /**
     * Call om.preRollback() and listeners.
     */
    protected void internalPreRollback()
    {
        if (NucleusLogger.TRANSACTION.isDebugEnabled())
        {
            NucleusLogger.TRANSACTION.debug(LOCALISER.msg("015002", ec));
        }

        TransactionEventListener[] ls = getListenersForEvent();
        for (TransactionEventListener tel : ls)
        {
            tel.transactionPreRollBack();
        }
    }

    /**
     * Internal rollback, DataNucleus invokes it's own transaction manager implementation, if
     * an external transaction manager is not used.
     */
    protected void internalRollback()
    {
        org.datanucleus.transaction.Transaction tx = txnMgr.getTransaction(ec);
        if (tx != null)
        {
            if (ec.getMultithreaded())
            {
                synchronized (this)
                {
                    txnMgr.rollback(ec);
                }
            }
            else
            {
                txnMgr.rollback(ec);
            }
        }         

        TransactionEventListener[] ls = getListenersForEvent();
        for (TransactionEventListener tel : ls)
        {
            tel.transactionRolledBack();
        }
    }

    /**
     * Method to perform any post-commit operations like calling the users "afterCompletion"
     * and general clean up after the commit.
     */
    protected void internalPostCommit()
    {
        try
        {
            active = false;
            if (txnMgr.getTransactionRuntime() != null)
            {
                txnMgr.getTransactionRuntime().transactionCommitted(
                    System.currentTimeMillis()-beginTime);
            }
        }
        finally
        {
            try
            {
                TransactionEventListener[] ls = getListenersForEvent();
                for (TransactionEventListener tel : ls)
                {
                    tel.transactionCommitted();
                }
            }
            finally
            {
                committing = false;
                listenersPerTransaction.clear();
                // call sync.afterCompletion() only now to support the use-case of closing the PM in afterCompletion()
                if (sync != null)
                {
                    sync.afterCompletion(Status.STATUS_COMMITTED);
                }
            }
        }
    }

    private TransactionEventListener[] getListenersForEvent()
    {
        TransactionEventListener[] ls = new TransactionEventListener[listeners.size()+listenersPerTransaction.size()];
        System.arraycopy(listenersPerTransaction.toArray(), 0, ls, 0, listenersPerTransaction.size());
        System.arraycopy(listeners.toArray(), 0,  ls, listenersPerTransaction.size(), listeners.size());
        return ls;
    }

    /**
     * Accessor for whether the transaction is active.
     * @return Whether the transaction is active.
     **/
    public boolean isActive()
    {
        return active;
    }

    /**
     * Accessor for whether the transaction is comitting.
     * @return Whether the transaction is committing.
     **/
    public boolean isCommitting()
    {
        return committing;
    }

    // ------------------------------- Accessors/Mutators ---------------------------------------

    /**
     * Accessor for the nontransactionalRead flag for this transaction.
     * @return Whether nontransactionalRead is set.
     */
    public boolean getNontransactionalRead()
    {
        return nontransactionalRead;
    }

    /**
     * Accessor for the nontransactionalWrite flag for this transaction.
     * @return Whether nontransactionalWrite is set.
     */
    public boolean getNontransactionalWrite()
    {
        return nontransactionalWrite;
    }

    /**
     * Accessor for the Optimistic setting
     * @return Whether optimistic transactions are in operation.
     */
    public boolean getOptimistic()
    {
        return optimistic;
    }

    /**
     * Accessor for the restoreValues flag for this transaction.
     * @return Whether restoreValues is set.
     */
    public boolean getRestoreValues()
    {
        return restoreValues;
    }

    /**
     * Accessor for the retainValues flag for this transaction.
     * @return Whether retainValues is set.
     */
    public boolean getRetainValues()
    {
        return retainValues;
    }

    /**
     * Accessor for the "rollback only" flag.
     * @return The rollback only flag
     * @since 1.1
     */
    public boolean getRollbackOnly()
    {
        return rollbackOnly;
    }

    /**
     * Accessor for the synchronization object to be notified on transaction completion.
     * @return The synchronization instance to be notified on transaction completion.
     */
    public Synchronization getSynchronization()
    {
        return sync;
    }

    /**
     * Mutator for the setting of nontransactional read.
     * @param nontransactionalRead Whether to allow nontransactional read operations
     */
    public void setNontransactionalRead(boolean nontransactionalRead)
    {
        this.nontransactionalRead = nontransactionalRead;
    }

    /**
     * Mutator for the setting of nontransactional write.
     * @param nontransactionalWrite Whether to allow nontransactional write operations
     */
    public void setNontransactionalWrite(boolean nontransactionalWrite)
    {
        this.nontransactionalWrite = nontransactionalWrite;
    }

    /**
     * Mutator for the optimistic transaction setting.
     * @param optimistic The optimistic transaction setting.
     */
    public void setOptimistic(boolean optimistic)
    {
        this.optimistic = optimistic;
    }

    /**
     * Mutator for the setting of restore values.
     * @param restoreValues Whether to restore values at commit
     */
    public void setRestoreValues(boolean restoreValues)
    {
        this.restoreValues = restoreValues;
    }

    /**
     * Mutator for the setting of retain values.
     * @param retainValues Whether to retain values at commit
     */
    public void setRetainValues(boolean retainValues)
    {
        this.retainValues = retainValues;
        if (retainValues)
        {
            nontransactionalRead = true;
        }
    }

    /**
     * Mutator for the "rollback only" flag. Sets the transaction as for rollback only.
     * @since 1.1
     */
    public void setRollbackOnly()
    {
        // Only apply to active transactions
        if (active)
        {
            rollbackOnly = true;
        }
    }

    /**
     * Mutator for the synchronization object to be notified on transaction completion.
     * @param sync The synchronization object to be notified on transaction completion
     */
    public void setSynchronization(Synchronization sync)
    {
        this.sync = sync;
    }

    public void addTransactionEventListener(TransactionEventListener listener)
    {
        this.listenersPerTransaction.add(listener);
    }

    public void removeTransactionEventListener(TransactionEventListener listener)
    {
        this.listenersPerTransaction.remove(listener);
        this.listeners.remove(listener);
    }
    
    public void bindTransactionEventListener(TransactionEventListener listener)
    {
        this.listeners.add(listener);
    }

    public Map<String, Object> getOptions()
    {
        return options;
    }

    /* (non-Javadoc)
     * @see org.datanucleus.Transaction#getSerializeRead()
     */
    public Boolean getSerializeRead()
    {
        return serializeRead;
    }

    /* (non-Javadoc)
     * @see org.datanucleus.Transaction#setSerializeRead(java.lang.Boolean)
     */
    public void setSerializeRead(Boolean serializeRead)
    {
        this.serializeRead = serializeRead;
    }

    /* (non-Javadoc)
     * @see org.datanucleus.Transaction#lockReadObjects()
     */
    public boolean lockReadObjects()
    {
        // Default to not locking if not specified
        // TODO Remove this method and just use getSerializeRead()
        return (serializeRead != null ? serializeRead.booleanValue() : false);
    }

    public void setOption(String option, int value)
    {
        options.put(option, Integer.valueOf(value));
    }
    
    public void setOption(String option, boolean value)
    {
        options.put(option, Boolean.valueOf(value));
    }

    public void setOption(String option, String value)
    {
        options.put(option, value);
    }
}