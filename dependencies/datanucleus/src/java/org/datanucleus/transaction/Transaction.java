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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.transaction.Synchronization;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.datanucleus.util.Localiser;
import org.datanucleus.util.NucleusLogger;
import org.omg.CORBA.SystemException;

/**
 * Transaction
 * The Global Transaction ID is formed as: TX-<final static random int>-<static seq>-<current time millis>
 */
public class Transaction
{
    /** Localisation utility for output messages */
    protected static final Localiser LOCALISER = Localiser.getInstance("org.datanucleus.Localisation",
        org.datanucleus.ClassConstants.NUCLEUS_CONTEXT_LOADER);

    /** this id of this instance **/
    private static final int nodeId = new Random().nextInt();

    /** sequence number for global transactions **/
    private static int nextGlobalTransactionId = 1;

    /** number for next branch **/
    private int nextBranchId = 1;

    /** transaction id **/
    private final Xid xid; 

    /** transaction status **/
    private int status;

    /** has completing started ? **/
    private boolean completing = false;

    /** Synchonization **/
    private List<Synchronization> synchronization = new ArrayList();

    /** enlisted XAResource resources **/
    private List<XAResource> enlistedResources = new ArrayList();

    /** branches - each resource is a new branch **/
    private Map<Xid, XAResource> branches = new HashMap();

    /** active branches are resources that have not ended and are not suspended **/
    private Map<XAResource, Xid> activeBranches = new HashMap();

    /** suspended branches **/
    private Map<XAResource, Xid> suspendedResources = new HashMap();

    Transaction()
    {
        xid = new XidImpl(nodeId,0,nextGlobalTransactionId++);
    	if (NucleusLogger.TRANSACTION.isDebugEnabled())
    	{
    		NucleusLogger.TRANSACTION.debug("Transaction created "+toString());
    	}        
    }
    
    public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException, 
        SecurityException, IllegalStateException, SystemException
    {
        if (completing)
        {
            return;
        }

        if (status == Status.STATUS_MARKED_ROLLBACK)
        {
            rollback();
            return;
        }

        try
        {
            completing = true;
        	if (NucleusLogger.TRANSACTION.isDebugEnabled())
        	{
        		NucleusLogger.TRANSACTION.debug("Committing "+toString());
        	}
    
            // The transaction status must be ACTIVE
            if (status != Status.STATUS_ACTIVE)
            {
                throw new IllegalStateException();
            }

            // Synchronization.beforeCompletion
            Iterator<Synchronization> syncIterator = synchronization.iterator();
            while (syncIterator.hasNext())
            {
                syncIterator.next().beforeCompletion();
            }

            List failures = null;
            boolean failed = false;
            Iterator<Xid> branchKeys = branches.keySet().iterator();
            if (enlistedResources.size() == 1)
            {
                // If we have only one resource, we don't ask to prepare, and we go with one-phase commit
                status = Status.STATUS_COMMITTING;
                while (branchKeys.hasNext())
                {
                    Xid key = branchKeys.next();
                    XAResource resourceManager = branches.get(key);
                    try
                    {
                        if (!failed)
                        {
                            resourceManager.commit(key, true);
                        }
                        else
                        {
                            resourceManager.rollback(key);
                        }
                    }
                    catch (Throwable e)
                    {
                        if (failures == null)
                        {
                            //lazy instantiate this, because we only need on failures
                            failures = new ArrayList();
                        }
                        failures.add(e);
                        failed = true;
                        status = Status.STATUS_MARKED_ROLLBACK;
                        NucleusLogger.TRANSACTION.error(LOCALISER.msg("015038", "commit", resourceManager, 
                            getXAErrorCode(e), toString()));                    
                    }
                }
                if (!failed)
                {
                    status = Status.STATUS_COMMITTED;
                }
                else
                {
                    status = Status.STATUS_ROLLEDBACK;
                }
            }
            else if (enlistedResources.size() > 0)
            {
                // Prepare each enlisted resource
                status = Status.STATUS_PREPARING;
                while ((!failed) && (branchKeys.hasNext()))
                {
                    Xid key = branchKeys.next();
                    XAResource resourceManager = branches.get(key);
                    try
                    {
                        // Preparing the resource manager using its branch xid
                        resourceManager.prepare(key);
                    }
                    catch (Throwable e)
                    {
                        if (failures == null)
                        {
                            //lazy instantiate this, because we only need on failures
                            failures = new ArrayList();
                        }
                        failures.add(e);
                        failed = true;
                        status = Status.STATUS_MARKED_ROLLBACK;
                        NucleusLogger.TRANSACTION.error(LOCALISER.msg("015038", "prepare", resourceManager, 
                            getXAErrorCode(e), toString()));
                    }
                }

                if (!failed)
                {
                    status = Status.STATUS_PREPARED;
                }
    
                // Starts 2nd commit phase
                // If fail, rollback
                if (failed)
                {
                    status = Status.STATUS_ROLLING_BACK;
                    failed = false;
                    // Rolling back all the prepared (and unprepared) branches
                    branchKeys = branches.keySet().iterator();
                    while (branchKeys.hasNext())
                    {
                        Xid key = branchKeys.next();
                        XAResource resourceManager = branches.get(key);
                        try
                        {
                            resourceManager.rollback(key);
                        }
                        catch (Throwable e)
                        {
                            NucleusLogger.TRANSACTION.error(LOCALISER.msg("015038", "rollback", resourceManager, 
                                getXAErrorCode(e), toString()));                        
                            if (failures == null)
                            {
                                //lazy instantiate this, because we only need on failures
                                failures = new ArrayList();
                            }
                            failures.add(e);
                            failed = true;
                        }
                    }
                    status = Status.STATUS_ROLLEDBACK;
                }
                else
                {
                    status = Status.STATUS_COMMITTING;
                    // Commit each enlisted resource
                    branchKeys = branches.keySet().iterator();
                    while (branchKeys.hasNext())
                    {
                        Xid key = branchKeys.next();
                        XAResource resourceManager = branches.get(key);
                        try
                        {
                            resourceManager.commit(key, false);
                        }
                        catch (Throwable e)
                        {
                            NucleusLogger.TRANSACTION.error(LOCALISER.msg("015038", "commit", resourceManager, 
                                getXAErrorCode(e), toString()));                     
                            if (failures == null)
                            {
                                //lazy instantiate this, because we only need on failures
                                failures = new ArrayList();
                            }
                            failures.add(e);
                            failed = true;
                        }
                    }
                    status = Status.STATUS_COMMITTED;
                }
            }
    
            // Synchronization.afterCompletion
            syncIterator = synchronization.iterator();
            while (syncIterator.hasNext())
            {
                syncIterator.next().afterCompletion(status);
            }
            
            if (status == Status.STATUS_ROLLEDBACK)
            {
                if (failed)
                {
                    if (failures.size() == 1) 
                    {
                        throw new HeuristicRollbackException("Transaction rolled back due to failure during commit", 
                            (Throwable)failures.get(0));
                    }
                    else
                    {
                        throw new HeuristicRollbackException("Multiple failures");
                    }
                }
                else
                {
                    throw new RollbackException();
                }
            }
            if ((status == Status.STATUS_COMMITTED) && (failed))
            {
                throw new HeuristicMixedException();
            }
            
        }
        finally
        {
            completing = false;
        }
    }

    public boolean delistResource(XAResource xaRes, int flag) throws IllegalStateException, SystemException
    {
        if (xaRes == null)
        {
            return false;
        }

        // The transaction status must be ACTIVE
        if (status != Status.STATUS_ACTIVE)
        {
            throw new IllegalStateException();
        }

        Xid xid = activeBranches.get(xaRes);
        if (xid == null)
        {
            throw new IllegalStateException();
        }
        activeBranches.remove(xaRes);

        if (NucleusLogger.TRANSACTION.isDebugEnabled())
        {
           NucleusLogger.TRANSACTION.debug(LOCALISER.msg("015039", "delist", xaRes, getXAFlag(flag), toString()));
        }

        XAException exception = null;
        try
        {
            xaRes.end(xid, flag);
        }
        catch (XAException e)
        {
            exception = e;
        }

        if (exception != null)
        {
            NucleusLogger.TRANSACTION.error(LOCALISER.msg("015038", "delist", xaRes, getXAErrorCode(exception), toString()));
            return false;
        }

        if (flag == XAResource.TMSUSPEND)
        {
            suspendedResources.put(xaRes, xid);
        }
        return true;

    }

    public boolean enlistResource(XAResource xaRes) throws RollbackException, IllegalStateException, SystemException
    {
        if (xaRes == null)
        {
            return false;
        }
        
        if (status == Status.STATUS_MARKED_ROLLBACK)
        {
            throw new RollbackException();
        }

        // The transaction status must be ACTIVE
        if (status != Status.STATUS_ACTIVE)
        {
            throw new IllegalStateException();
        }

        // Preventing two branches from being active at the same time on the
        // same resource manager
        Xid activeXid = activeBranches.get(xaRes);
        if (activeXid != null)
        {
            return false;
        }

        boolean alreadyEnlisted = false;
        int flag = XAResource.TMNOFLAGS;

        Xid branchXid = suspendedResources.get(xaRes);
        if (branchXid == null)
        {
            Iterator<XAResource> enlistedIterator = enlistedResources.iterator();
            while ((!alreadyEnlisted) && (enlistedIterator.hasNext()))
            {
                XAResource resourceManager = enlistedIterator.next();
                try
                {
                    if (resourceManager.isSameRM(xaRes))
                    {
                        flag = XAResource.TMJOIN;
                        alreadyEnlisted = true;
                    }
                }
                catch (XAException e)
                {
                    //do nothing
                }
            }
            branchXid = new XidImpl(nextBranchId++,xid.getFormatId(),xid.getGlobalTransactionId());
        }
        else
        {
            alreadyEnlisted = true;
            flag = XAResource.TMRESUME;
            suspendedResources.remove(xaRes);
        }

        if (NucleusLogger.TRANSACTION.isDebugEnabled())
        {
            NucleusLogger.TRANSACTION.debug(LOCALISER.msg("015039", "enlist", xaRes, getXAFlag(flag), toString()));
        }

        try
        {
            xaRes.start(branchXid, flag);
        }
        catch (XAException e)
        {
            NucleusLogger.TRANSACTION.error(LOCALISER.msg("015038", "enlist", xaRes, getXAErrorCode(e), toString()));
            return false;
        }

        if (!alreadyEnlisted)
        {
            enlistedResources.add(xaRes);
        }

        branches.put(branchXid, xaRes);
        activeBranches.put(xaRes, branchXid);

        return true;
    }

    public int getStatus() throws SystemException
    {
        return status;
    }

    public void registerSynchronization(Synchronization sync) throws RollbackException, IllegalStateException, SystemException
    {
        if (sync == null)
        {
            return;
        }
        if (status == Status.STATUS_MARKED_ROLLBACK)
        {
            throw new RollbackException();
        }
        if (status != Status.STATUS_ACTIVE)
        {
            throw new IllegalStateException();
        }
        synchronization.add(sync);
    }

    public void rollback() throws IllegalStateException, SystemException
    {
        if (completing)
        {
         return;   
        }

        try
        {
            completing = true;
        	if (NucleusLogger.TRANSACTION.isDebugEnabled())
        	{
        		NucleusLogger.TRANSACTION.debug("Rolling back "+toString());
        	}
            // Must be ACTIVE and MARKED ROLLBACK
            if (status != Status.STATUS_ACTIVE && status != Status.STATUS_MARKED_ROLLBACK)
            {
                throw new IllegalStateException();
            }

            List failures = null;
            Iterator branchKeys = branches.keySet().iterator();
            status = Status.STATUS_ROLLING_BACK;
            while (branchKeys.hasNext())
            {
                Xid xid = (Xid) branchKeys.next();
                XAResource resourceManager = branches.get(xid);
                try
                {
                    resourceManager.rollback(xid);
                }
                catch (Throwable e)
                {
                    if (failures == null)
                    {
                        //lazy instantiate this, because we only need on failures
                        failures = new ArrayList();
                    }
                    failures.add(e);
                    NucleusLogger.TRANSACTION.error(LOCALISER.msg("015038", "rollback", resourceManager, 
                        getXAErrorCode(e), toString()));
                }
            }
            status = Status.STATUS_ROLLEDBACK;

            // Synchronization.afterCompletion
            Iterator<Synchronization> syncIterator = synchronization.iterator();
            while (syncIterator.hasNext())
            {
                syncIterator.next().afterCompletion(status);
            }
        }
        finally
        {
            completing = false;
        }
    }

    public void setRollbackOnly() throws IllegalStateException, SystemException
    {
        status = Status.STATUS_MARKED_ROLLBACK;
    }

    public static String getXAErrorCode(Throwable xae)
    {
        if (!(xae instanceof XAException))
        {
            return "UNKNOWN";
        }

        switch (((XAException)xae).errorCode)
        {
            case XAException.XA_HEURCOM :
                return "XA_HEURCOM";
            case XAException.XA_HEURHAZ :
                return "XA_HEURHAZ";
            case XAException.XA_HEURMIX :
                return "XA_HEURMIX";
            case XAException.XA_HEURRB :
                return "XA_HEURRB";
            case XAException.XA_NOMIGRATE :
                return "XA_NOMIGRATE";
            case XAException.XA_RBBASE :
                return "XA_RBBASE";
            case XAException.XA_RBCOMMFAIL :
                return "XA_RBCOMMFAIL";
            case XAException.XA_RBDEADLOCK :
                return "XA_RBBEADLOCK";
            case XAException.XA_RBEND :
                return "XA_RBEND";
            case XAException.XA_RBINTEGRITY :
                return "XA_RBINTEGRITY";
            case XAException.XA_RBOTHER :
                return "XA_RBOTHER";
            case XAException.XA_RBPROTO :
                return "XA_RBPROTO";
            case XAException.XA_RBTIMEOUT :
                return "XA_RBTIMEOUT";
            case XAException.XA_RDONLY :
                return "XA_RDONLY";
            case XAException.XA_RETRY :
                return "XA_RETRY";
            case XAException.XAER_ASYNC :
                return "XAER_ASYNC";
            case XAException.XAER_DUPID :
                return "XAER_DUPID";
            case XAException.XAER_INVAL :
                return "XAER_INVAL";
            case XAException.XAER_NOTA :
                return "XAER_NOTA";
            case XAException.XAER_OUTSIDE :
                return "XAER_OUTSIDE";
            case XAException.XAER_PROTO :
                return "XAER_PROTO";
            case XAException.XAER_RMERR :
                return "XAER_RMERR";
            case XAException.XAER_RMFAIL :
                return "XAER_RMFAIL";
            default :
                return "UNKNOWN";
        }
    }

    private static String getXAFlag(int flag)
    {
        switch (flag)
        {
            case XAResource.TMENDRSCAN :
                return "TMENDRSCAN";
            case XAResource.TMFAIL :
                return "TMFAIL";
            case XAResource.TMJOIN :
                return "TMJOIN";
            case XAResource.TMNOFLAGS :
                return "TMNOFLAGS";
            case XAResource.TMONEPHASE :
                return "TMONEPHASE";
            case XAResource.TMRESUME :
                return "TMRESUME";
            case XAResource.TMSTARTRSCAN :
                return "TMSTARTRSCAN";
            case XAResource.TMSUCCESS :
                return "TMSUCCESS";
            case XAResource.TMSUSPEND :
                return "TMSUSPEND";
            default :
                return "UNKNOWN";
        }
    }
    
    public String toString()
    {
        String resString = null;
        synchronized(enlistedResources)
        {
            resString = enlistedResources.toString();
        }
        return "[DataNucleus Transaction, ID=" + xid.toString()+ ", enlisted resources="+resString+"]";
    }
}