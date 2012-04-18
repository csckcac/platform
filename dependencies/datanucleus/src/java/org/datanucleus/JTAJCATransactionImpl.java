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
    ...
**********************************************************************/
package org.datanucleus;

import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

import org.datanucleus.store.ExecutionContext;
import org.datanucleus.transaction.NucleusTransactionException;
import org.datanucleus.util.NucleusLogger;
import org.datanucleus.util.StringUtils;

/**
 * A transaction that is synchronized with a Java Transaction Service (JTA) transaction with JCA.
 * 
 * TODO This is revision 984 of JTATransactionImpl that Erik wrote. It should be merged with JTATransactionImpl
 * when Erik and Joerg understand what both are requiring, merge them, and then document them.
 */
public class JTAJCATransactionImpl extends TransactionImpl implements Synchronization 
{
    /** TransactionManager. **/
    private TransactionManager tm;

    /** JTA transaction we currently are synchronized with. Null when there is no JTA transaction active or not yet detected. */
    private javax.transaction.Transaction jtaTx;

    private boolean markedForRollback = false;

    /**
     * Constructor.
     * @param ec ExecutionContext
     */
    JTAJCATransactionImpl(ExecutionContext ec)
    {
        super(ec);
        joinTransaction();
    }

    /**
     * Accessor for whether the transaction is active.
     * @return Whether the transaction is active.
     **/
    public boolean isActive()
    {
        boolean isActive = super.isActive();
        if (isActive)
        {
            //do not join transaction if org.datanucleus.Transaction already started
            return true;
        }
        joinTransaction();
        return active;
    }

    // ------------------- Methods to get the JTA transaction for synchronising --------------------------

    /**
     * Synchronize our active state with that of the JTA tx, if it exists.
     * Look for an active JTA transaction. if there is one, begin() ourselves
     * and register synchronization. We must poll because there is no
     * way of getting notified of a newly begun transaction.<p>
     */
    private synchronized void joinTransaction()
    {       
        if (active)
        {
            return;
        }

        // try to registerSynchronization()
        try
        {
            if (tm == null)
            {
                tm = obtainTransactionManager();
            }
            jtaTx = tm.getTransaction();
            if (jtaTx != null && jtaTx.getStatus() == Status.STATUS_ACTIVE)
            {
                if (!ec.getNucleusContext().isJcaMode())
                {
                    //in JCA mode, we do not register Synchronization
                    jtaTx.registerSynchronization(this);
                }                

                //the transaction is active here
                begin();
            }
            else
            {
                // jtaTx can be null when there is no active transaction.
                // There is no app-server agnostic way of getting notified
                // when a global transaction has started. Instead, we
                // poll for jtaTx' status in getConnection() and isActive()

                // If a transaction was marked for rollback before we could
                // register synchronization, we won't be called back when it
                // is rolled back
                if (markedForRollback)
                {
                    // as jtaTx is null there is no active transaction, meaning
                    // that the jtaTx was actually rolled back after it had
                    // been marked for rollback: catch up
                    rollback();
                    markedForRollback = false;
                }
            }
        }
        catch (SystemException se)
        {
            throw new NucleusTransactionException(LOCALISER.msg("015026"), se);
        }
        catch (RollbackException e)
        {
            NucleusLogger.TRANSACTION.error("Exception while joining transaction: " + StringUtils.getStringFromStackTrace(e));
            // tx is marked for rollback: leave registeredSynchronizationOnJtaTx==false
            // so that we try to register again next time we're called
        }
    }

    /**
     * Accessor for the JTA TransactionManager. Unfortunately, before J2EE 5 there is no specified way to do it, 
     * only appserver-specific ways. Taken from http://www.onjava.com/pub/a/onjava/2005/07/20/transactions.html.
     * <P>
     * In J2EE 5, we'll be able to use the following
     * https://glassfish.dev.java.net/nonav/javaee5/api/s1as-javadocs/javax/transaction/TransactionSynchronizationRegistry.html
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
    
    // --------------------------- methods for javax.transaction.Synchronization -----------------------------

    /**
     * The beforeCompletion method is called by the transaction manager prior to the start of the two-phase 
     * transaction commit process.
     * This is not called in JCA mode
     */
    public void beforeCompletion()
    {
        try
        {
            internalPreCommit();
        }
        catch (Throwable th)
        {
            // TODO Localise these messages
            NucleusLogger.TRANSACTION.error("Exception flushing work in JTA transaction. Mark for rollback", th);
            try
            {
                jtaTx.setRollbackOnly();
            }
            catch (Exception e)
            {
                NucleusLogger.TRANSACTION.fatal("Cannot mark transaction for rollback after exception in beforeCompletion. PersistenceManager might be in inconsistent state", e);
            }
        }
    }

    /**
     * This method is called by the transaction manager after the transaction is committed or rolled back.
     * Must be synchronized because some callees expect to be owner of this object's monitor (internalPostCommit() 
     * calls closeSQLConnection() which calls notifyAll()).
     * 
     * This is not called in JCA mode
     * @param status The status
     */
    public synchronized void afterCompletion(int status)
    {
        try
        {
            if (status == Status.STATUS_ROLLEDBACK)
            {
                rollback();
            }
            else if (status == Status.STATUS_COMMITTED)
            {
                internalPostCommit();
            }
            else
            {
                // this method is called after*Completion*(), so we can expect not to be confronted with intermediate status codes
                // TODO Localise this
                NucleusLogger.TRANSACTION.fatal("Received unexpected transaction status + " + status);
            }
        }
        catch (Throwable th)
        {
            // TODO Localise this
            NucleusLogger.TRANSACTION.error("Exception during afterCompletion in JTA transaction. PersistenceManager might be in inconsistent state");
        }
        finally
        {
            // done with this jtaTx. Make us synchronizeWithJta() again,
            // as there there is no callback for a newly begun tx
            jtaTx = null;
        }
    }   
}