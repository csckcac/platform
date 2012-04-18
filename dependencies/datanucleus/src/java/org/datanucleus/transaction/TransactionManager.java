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
package org.datanucleus.transaction;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.datanucleus.management.ManagementServer;
import org.datanucleus.management.runtime.TransactionRuntime;
import org.datanucleus.util.ClassUtils;

/**
 * TransactionManager is facade for creating (Open/XA) transactions.
 * A cache of transactions is held with each transaction for a user object.
 * If using with a multithreaded PM/EM then you must lock access external to TransactionManager since
 * this is for a PMF/EMF.
 */
public class TransactionManager
{
    private boolean containerManagedConnections = false;

    /** Transaction for the keyed user object (typically ObjectManager). */
    private Map<Object, Transaction> transactions = new ConcurrentHashMap<Object, Transaction>();

    /** runtime metrics for the transaction system **/
    private TransactionRuntime txRuntime = null;

    public void setContainerManagedConnections(boolean flag)
    {
        containerManagedConnections = flag;
    }

    /**
     * Register MBean with JMX.
     * @param domainName Domain name
     * @param instanceName Instance name
     * @param mgmtServer JMX server
     */
    public void registerMbean(String domainName, String instanceName, ManagementServer mgmtServer)
    {
        if (mgmtServer != null)
        {
            // Register MBean with server
            txRuntime = new TransactionRuntime();
            String mbeanName = domainName + ":InstanceName="+ instanceName +
                ",Type="+ClassUtils.getClassNameForClass(txRuntime.getClass()) + ",Name=TransactionRuntime";
            mgmtServer.registerMBean(txRuntime, mbeanName);
        }
    }

    public TransactionRuntime getTransactionRuntime()
    {
        return txRuntime;
    }

    /**
     * Begin a new transaction
     * @param om the user object associated to this transaction
     * @throws NucleusTransactionException if there is already a Transaction associated to this user object
     */
    public void begin(Object om)
    {
        if (transactions.get(om) != null)
        {
            throw new NucleusTransactionException("Invalid state. Transaction has already started");
        }
        transactions.put(om, new Transaction());
    }

    public void commit(Object om)
    {
        Transaction tx = transactions.get(om);
        if (tx == null)
        {
            throw new NucleusTransactionException("Invalid state. Transaction does not exist");
        }

        try
        {
            if (!containerManagedConnections) 
            {
                tx.commit();
            }
        }
        finally
        {
            transactions.remove(om);
        }
    }

    public void rollback(Object om)
    {
        Transaction tx = transactions.get(om);
        if (tx == null)
        {
            throw new NucleusTransactionException("Invalid state. Transaction does not exist");
        }

        try
        {
            if (!containerManagedConnections) 
            {
                tx.rollback();
            }
        }
        finally
        {
            transactions.remove(om);
        }
    }

    public Transaction getTransaction(Object om)
    {
        if (om == null)
        {
            return null;
        }
        return transactions.get(om);
    }

    public void setRollbackOnly(Object om)
    {
        Transaction tx = transactions.get(om);
        if (tx == null)
        {
            throw new NucleusTransactionException("Invalid state. Transaction does not exist");
        }
        tx.setRollbackOnly();
    }

    public void setTransactionTimeout(Object om, int millis)
    {
        throw new UnsupportedOperationException();        
    }

    public void resume(Object om, Transaction tx)
    {
        throw new UnsupportedOperationException();        
    }

    public Transaction suspend(Object om)
    {
        throw new UnsupportedOperationException();        
    }
}