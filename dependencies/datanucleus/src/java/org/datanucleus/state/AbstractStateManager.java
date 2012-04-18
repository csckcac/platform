/**********************************************************************
Copyright (c) 2006 Andy Jefferson and others. All rights reserved.
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
2007 Xuan Baldauf - use IdentityReference instead of super.toString() as a unique identifier.
2007 Xuan Baldauf - moved initialization of AbstractStateManager to Initialization.
2007 Xuan Baldauf - remove the field "srm".
2007 Xuan Baldauf - remove the field "secondClassMutableFieldNumbers".
2007 Xuan Baldauf - remove the field "allNonPrimaryKeyFieldNumbers".
2007 Xuan Baldauf - remove the field "allFieldNumbers".
2007 Xuan Baldauf - remove the field "nonPrimaryKeyFields".
2007 Xuan Baldauf - remove the field "secondClassMutableFields".
2007 Xuan Baldauf - move the field "fieldCount" to AbstractClassMetaData.
2007 Xuan Baldauf - remove the field "callback".  
2007 Andy Jefferson - removed unloadedFields
    ...
**********************************************************************/
package org.datanucleus.state;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.BitSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.jdo.JDOFatalInternalException;
import javax.jdo.JDOFatalUserException;
import javax.jdo.JDOUserException;
import javax.jdo.PersistenceManager;
import javax.jdo.spi.Detachable;
import javax.jdo.spi.PersistenceCapable;
import javax.jdo.spi.StateManager;

import org.datanucleus.ClassLoaderResolver;
import org.datanucleus.FetchPlan;
import org.datanucleus.FetchPlanForClass;
import org.datanucleus.ObjectManager;
import org.datanucleus.exceptions.NucleusException;
import org.datanucleus.identity.IdentityReference;
import org.datanucleus.metadata.AbstractClassMetaData;
import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.metadata.MetaDataManager;
import org.datanucleus.metadata.Relation;
import org.datanucleus.state.lock.LockManager;
import org.datanucleus.store.ExecutionContext;
import org.datanucleus.store.ObjectReferencingStoreManager;
import org.datanucleus.store.StoreManager;
import org.datanucleus.store.fieldmanager.FieldManager;
import org.datanucleus.store.fieldmanager.SingleTypeFieldManager;
import org.datanucleus.store.fieldmanager.SingleValueFieldManager;
import org.datanucleus.store.fieldmanager.AbstractFetchFieldManager.EndOfFetchPlanGraphException;
import org.datanucleus.store.types.sco.SCO;
import org.datanucleus.store.types.sco.SCOContainer;
import org.datanucleus.util.Localiser;
import org.datanucleus.util.NucleusLogger;
import org.datanucleus.util.StringUtils;

/**
 * Abstract representation of a StateManager.
 * Provides some of the basic StateManager methods that do very little.
 */
public abstract class AbstractStateManager implements StateManager, org.datanucleus.state.StateManager
{
    /** Localiser for messages. */
    protected static final Localiser LOCALISER = Localiser.getInstance("org.datanucleus.Localisation",
        org.datanucleus.ClassConstants.NUCLEUS_CONTEXT_LOADER);

    protected static final SingleTypeFieldManager HOLLOWFIELDMANAGER = new SingleTypeFieldManager();

    /** Flag to signify that we are currently storing the PC object, so we dont detach it on any serialisation. */
    protected static final int FLAG_STORING_PC = (2<<15);
    /** Whether the managed object needs the inheritance level validating before loading fields. */
    protected static final int FLAG_NEED_INHERITANCE_VALIDATION = (2<<14);
    protected static final int FLAG_POSTINSERT_UPDATE = (2<<13);
    protected static final int FLAG_LOADINGFPFIELDS = (2<<12);
    protected static final int FLAG_POSTLOAD_PENDING = (2<<11);
    protected static final int FLAG_CHANGING_STATE = (2<<10);
    /** if the PersistenceCapable instance is new and was flushed to the datastore. */
    protected static final int FLAG_FLUSHED_NEW = (2<<9);
    protected static final int FLAG_BECOMING_DELETED = (2<<8);
    /** Flag whether this SM is updating the ownership of its embedded/serialised field(s). */
    protected static final int FLAG_UPDATING_EMBEDDING_FIELDS_WITH_OWNER  = (2<<7);
    /** Flag for {@link #flags} whether we are retrieving detached state from the detached object. */
    protected static final int FLAG_RETRIEVING_DETACHED_STATE = (2<<6);
    /** Flag for {@link #flags} whether we are resetting the detached state. */
    protected static final int FLAG_RESETTING_DETACHED_STATE  = (2<<5);
    /** Flag for {@link #flags} whether we are in the process of attaching the object. */
    protected static final int FLAG_ATTACHING = (2<<4);
    /** Flag for {@link #flags} whether we are in the process of detaching the object. */
    protected static final int FLAG_DETACHING = (2<<3);
    /** Flag for {@link #flags} whether we are in the process of making transient the object. */
    protected static final int FLAG_MAKING_TRANSIENT = (2<<2);
    /** Flag for {@link #flags} whether we are in the process of flushing changes to the object. */
    protected static final int FLAG_FLUSHING = (2<<1);
    /** Flag for {@link #flags} whether we are in the process of disconnecting the object. */
    protected static final int FLAG_DISCONNECTING = (2<<0);

    /** Bit-packed flags for operational settings. */
    protected int flags;

    /** Flags for DFG state stored with the object. */
    protected byte jdoDfgFlags;

    /** the Object Manager for this StateManager */
    protected ObjectManager myOM;

    /** The PersistenceCapable instance managed by this StateManager */
    protected PersistenceCapable myPC;

    /** the metadata for the class. */
    protected AbstractClassMetaData cmd;

    // TODO Drop this and just use myID. Why do we need both ?
    /** The object identity in the JVM. Will be "myID" (if set) or otherwise a temporary id based on this StateManager. */
    protected Object myInternalID;

    /** The object identity in the datastore */
    protected Object myID;

    /** The actual LifeCycleState for the persistable instance */
    protected LifeCycleState myLC;

    /** version field for optimistic transactions */
    protected Object myVersion;

    /** version field for optimistic transactions, after a insert/update but not yet committed. */
    protected Object transactionalVersion;

    /** Fetch plan for the class of the managed object. */
    protected FetchPlanForClass myFP;

    /**
     * Indicator for whether the persistable instance is dirty.
     * Note that "dirty" in this case is not equated to being in the P_DIRTY state.
     * The P_DIRTY state means that at least one field in the object has been written by the user during 
     * the current transaction, whereas for this parameter, a field is "dirty" if it's been written by the 
     * user but not yet updated in the data store.  The difference is, it's possible for an object's state
     * to be P_DIRTY, yet have no "dirty" fields because flush() has been called at least once during the transaction.
     */
    protected boolean dirty = false;

    /** indicators for which fields are currently dirty in the persistable instance. */
    protected boolean[] dirtyFields;

    /** indicators for which fields are currently loaded in the persistable instance. */
    protected boolean[] loadedFields;

    /** Whether to restore values at StateManager. If true, overwrites the restore values at tx level. */
    protected boolean restoreValues = false;

    /** Current FieldManager. */
    protected FieldManager currFM = null;

    /** Lock object to synchronise execution when reading/writing fields. */
    protected Lock lock = new ReentrantLock();

    /** The type of the managed object (0 = PC, 1 = embedded PC, 2 = embedded element, 3 = embedded key, 4 = embedded value. */
    protected short pcObjectType = 0;

    /** The current lock mode for this object. */
    protected short lockMode = LockManager.LOCK_MODE_NONE;

    /** Flags of the PersistenceCapable instance when the instance is enlisted in the transaction. */
    protected byte savedFlags;

    /** Image of the PersistenceCapable instance when the instance is enlisted in the transaction. */
    protected PersistenceCapable savedImage = null;

    /** Loaded fields of the PersistenceCapable instance when the instance is enlisted in the transaction. */
    protected boolean[] savedLoadedFields = null;

    /**
     * Constructor.
     * @param om ObjectManager
     * @param cmd the metadata for the class.
     */
    public AbstractStateManager(ObjectManager om, AbstractClassMetaData cmd)
    {
        myOM = om;
        this.cmd = cmd;

        // Set up the field arrays
        initialiseFieldInformation();

        myFP = myOM.getFetchPlan().manageFetchPlanForClass(cmd);
    }

    /**
     * Convenience method to initialise the field information.
     **/
    protected void initialiseFieldInformation()
    {
        int fieldCount = getHighestFieldNumber();

        dirtyFields = new boolean[fieldCount];
        loadedFields = new boolean[fieldCount];
    }

    /**
     * Method to save all fields of the object so we can potentially restore them later.
     */
    public void saveFields()
    {
        savedImage = myPC.jdoNewInstance(this);
        savedImage.jdoCopyFields(myPC, getAllFieldNumbers());
        savedFlags = jdoDfgFlags;
        savedLoadedFields = loadedFields.clone();
    }

    /**
     * Method to clear all saved fields on the object.
     */
    public void clearSavedFields()
    {
        savedImage = null;
        savedFlags = 0;
        savedLoadedFields = null;
    }

    /**
     * Method to restore all fields of the object.
     */
    public void restoreFields()
    {
        if (savedImage != null)
        {
            loadedFields = savedLoadedFields;
            jdoDfgFlags = savedFlags;
            myPC.jdoReplaceFlags();
            myPC.jdoCopyFields(savedImage, getAllFieldNumbers());

            clearDirtyFlags();
            clearSavedFields();
        }
    }

    /**
     * Method to enlist the managed object in the current transaction.
     */
    public void enlistInTransaction()
    {
        if (!myOM.getTransaction().isActive())
        {
            return;
        }
        myOM.enlistInTransaction(this);

        if (jdoDfgFlags == PersistenceCapable.LOAD_REQUIRED && isDefaultFetchGroupLoaded())
        {
            // All DFG fields loaded and object is transactional so it doesnt need to contact us for those fields
            // Note that this is the DFG and NOT the current FetchPlan since in the enhancement of classes
            // all DFG fields are set to check jdoFlags before relaying back to the StateManager
            jdoDfgFlags = PersistenceCapable.READ_OK;
            myPC.jdoReplaceFlags();
        }
    }

    /**
     * Method to evict the managed object from the current transaction.
     */
    public void evictFromTransaction()
    {
        myOM.evictFromTransaction(this);

        /*
         * A non-transactional object needs to contact us on any field read no
         * matter what fields are loaded.
         */
        jdoDfgFlags = PersistenceCapable.LOAD_REQUIRED;
        myPC.jdoReplaceFlags();
    }

    /**
     * Utility to update the passed object with the passed StateManager (can be null).
     * @param pc The object to update
     * @param sm The new state manager
     */
    protected void replaceStateManager(final PersistenceCapable pc, final StateManager sm)
    {
        try
        {
            // Calls to pc.jdoReplaceStateManager must be run privileged
            AccessController.doPrivileged(new PrivilegedAction()
            {
                public Object run() 
                {
                    pc.jdoReplaceStateManager(sm);
                    return null;
                }
            });
        }
        catch (SecurityException e)
        {
            throw new JDOFatalUserException(LOCALISER.msg("026000"), e);
        }
    }

    /**
     * Replace the current value of jdoStateManager.
     * <P>This method is called by the PersistenceCapable whenever jdoReplaceStateManager is called and 
     * there is already an owning StateManager. This is a security precaution to ensure that the owning 
     * StateManager is the only source of any change to its reference in the PersistenceCapable.</p>
     *
     * @return the new value for the jdoStateManager
     * @param pc the calling PersistenceCapable instance
     * @param sm the proposed new value for the jdoStateManager
     */
    public StateManager replacingStateManager(PersistenceCapable pc, StateManager sm)
    {
        if (myLC == null)
        {
            throw new JDOFatalInternalException("Null LifeCycleState");
        }
        else if (myLC.stateType() == LifeCycleState.DETACHED_CLEAN)
        {
            return sm;
        }
        else if (pc == myPC)
        {
            //TODO check if we are really in transition to a transient instance
            if (sm == null)
            {
                return null;
            }
            if (sm == this)
            {
                return this;
            }

            if (this.myOM == ((AbstractStateManager) sm).getObjectManager())
            {
                // This is a race condition when makePersistent or
                // makeTransactional is called on the same PC instance for the
                // same PM. It has been already set to this SM - just 
                // disconnect the other one. Return this SM so it won't be
                // replaced.
                ((JDOStateManagerImpl) sm).disconnect();
                return this;
            }

            throw new JDOUserException(LOCALISER.msg("026003"));
        }
        else if (pc == savedImage)
        {
            return null;
        }
        else
        {
            return sm;
        }
    }

    /**
     * Method that replaces the PC managed by this StateManager to be the supplied object.
     * This happens when we want to get an object for an id and create a Hollow object, and then validate
     * against the datastore. This validation can pull in a new object graph from the datastore (e.g for DB4O)
     * @param pc The PersistenceCapable to use
     */
    public void replaceManagedPC(Object pc)
    {
        if (pc == null)
        {
            return;
        }

        // Swap the StateManager on the objects
        replaceStateManager((PersistenceCapable)pc, this);
        replaceStateManager(myPC, null);

        // Swap our object
        myPC = (PersistenceCapable) pc;

        // Put it in the cache in case the previous object was stored
        myOM.putObjectIntoCache(this);
    }

    /**
     * Accessor for the PersistenceManager that owns this instance.
     * @param pc The PersistenceCapable instance
     * @return The PersistenceManager that owns this instance
     */
    public javax.jdo.PersistenceManager getPersistenceManager(PersistenceCapable pc)
    {
        //in identifying relationships, jdoCopyKeyFieldsFromId will call
        //this method, and at this moment, myPC in statemanager is null
        // Currently AbstractPersistenceManager.java putObjectInCache prevents any identifying relation object being put in L2

        //if not identifying relationship, do the default check of disconnectClone:
        //"this.disconnectClone(pc)"
        if (myPC != null && this.disconnectClone(pc))
        {
            return null;
        }
        else if (myOM == null)
        {
            return null;
        }
        else
        {
            myOM.hereIsObjectProvider(this, myPC);
            return (PersistenceManager) myOM.getOwner();
        }
    }

    /**
     * returns the handler for callback events.
     * @return the handler for callback events.
     */
    protected CallbackHandler getCallbackHandler()
    {
        return myOM.getCallbackHandler();
    }
    
    /**
     * returns indicators for which fields are second-class mutable.
     * @return indicators for which fields are second-class mutable.
     */
    protected boolean[] getSecondClassMutableFields()
    {
        return cmd.getSCOMutableMemberFlags();
    }
    
    /**
     * returns indicators for which fields are non-primary key fields.
     * @return indicators for which fields are non-primary key fields.
     */
    protected boolean[] getNonPrimaryKeyFields()
    {
        return cmd.getNonPKMemberFlags();
    }
    
    /**
     * returns field numbers of all fields.
     * @return field numbers of all fields.
     */
    protected int[] getAllFieldNumbers()
    {
        return cmd.getAllMemberPositions();
    }
    
    /**
     * returns field numbers of all non-primary-key fields.
     * @return field numbers of all non-primary-key fields.
     */
    protected int[] getNonPrimaryKeyFieldNumbers()
    {
        return cmd.getNonPKMemberPositions();
    }
    
    /**
     * returns field numbers of all second class mutable fields.
     * @return field numbers of all second class mutable fields.
     */
    protected int[] getSecondClassMutableFieldNumbers()
    {
        return cmd.getSCOMutableMemberPositions();
    }

    /**
     * Accessor for the StoreManager used for this object.
     * @return The StoreManager.
     **/
    public StoreManager getStoreManager()
    {
        return myOM.getStoreManager();
    }

    /**
     * Accessor for the ClassMetaData for this object.
     * @return The ClassMetaData.
     **/
    public AbstractClassMetaData getClassMetaData()
    {
        return cmd;
    }

    /**
     * Accessor for the MetaDataManager to use for this object.
     * Simply a wrapper accessor method. 
     * @return The MetaDataManager.
     **/
    public MetaDataManager getMetaDataManager()
    {
        return myOM.getMetaDataManager();
    }

    /**
     * Accessor for the ObjectManager for this object.
     * @return The Object Manager.
     **/
    public ObjectManager getObjectManager()
    {
        return myOM;
    }

    /**
     * Accessor for the ExecutionContext for this object.
     * @return Execution Context
     */
    public ExecutionContext getExecutionContext()
    {
        return myOM;
    }

    /**
     * Accessor for the Persistent Capable object.
     * @return The PersistentCapable object
     **/
    public Object getObject()
    {
        return myPC;
    }

    /**
     * Accessor for the LifeCycleState
     * @return the LifeCycleState
     */
    public LifeCycleState getLifecycleState()
    {
        return myLC;
    }

    /**
     * Accessor for the Restore Values flag 
     * @return Whether to restore values
     */
    public boolean isRestoreValues()
    {
        return restoreValues;
    }

    /**
     * Mutator for the Restore Values flag 
     * @param restore_values Whether to restore values
     */
    protected void setRestoreValues(boolean restore_values)
    {
        restoreValues = restore_values;
    }

    /**
     * Accessor for the internal object id of the object we are managing.
     * This will return the "id" if it has been set, otherwise a temporary id based on this StateManager.
     * @return The internal object id
     */
    public Object getInternalObjectId()
    {
        if (myID != null)
        {
            return myID;
        }
        else if (myInternalID == null)
        {
            // Assign a temporary internal "id" based on the object itself until our real identity is assigned
//          myInternalID = super.toString();
            myInternalID = new IdentityReference(this);
            return myInternalID;
        }
        else
        {
            return myInternalID;
        }
    }

    // -------------------------- Lifecycle Methods ---------------------------

    /* (non-Javadoc)
     * @see org.datanucleus.store.ObjectProvider#isFlushedToDatastore()
     */
    public boolean isFlushedToDatastore()
    {
        return !dirty;
    }

    /**
     * Tests whether this object is dirty.
     *
     * Instances that have been modified, deleted, or newly
     * made persistent in the current transaction return true.
     * <P>Transient nontransactional instances return false (JDO spec).
     * @see PersistenceCapable#jdoMakeDirty(String fieldName)
     * @param pc the calling persistable instance
     * @return true if this instance has been modified in current transaction.
     */
    public boolean isDirty(PersistenceCapable pc)
    {
        if (disconnectClone(pc))
        {
            return false;
        }
        else
        {
            return myLC.isDirty();
        }
    }

    /**
     * Tests whether this object is transactional.
     *
     * Instances that respect transaction boundaries return true.  These
     * instances include transient instances made transactional as a result of
     * being the target of a makeTransactional method call; newly made
     * persistent or deleted persistent instances; persistent instances read
     * in data store transactions; and persistent instances modified in
     * optimistic transactions.
     * <P>
     * Transient nontransactional instances return false.
     *
     * @param pc the calling persistable instance
     * @return true if this instance is transactional.
     */
    public boolean isTransactional(PersistenceCapable pc)
    {
        if (disconnectClone(pc))
        {
            return false;
        }
        else
        {
            return myLC.isTransactional();
        }
    }

    /**
     * Tests whether this object is persistent.
     * Instances whose state is stored in the data store return true.
     * Transient instances return false.
     * @param pc the calling persistable instance
     * @return true if this instance is persistent.
     */
    public boolean isPersistent(PersistenceCapable pc)
    {
        if (disconnectClone(pc))
        {
            return false;
        }
        else
        {
            return myLC.isPersistent();
        }
    }

    /**
     * Tests whether this object has been newly made persistent.
     * Instances that have been made persistent in the current transaction
     * return true.
     * <P>
     * Transient instances return false.
     * @param pc the calling persistable instance
     * @return true if this instance was made persistent
     * in the current transaction.
     */
    public boolean isNew(PersistenceCapable pc)
    {
        if (disconnectClone(pc))
        {
            return false;
        }
        else
        {
            return myLC.isNew();
        }
    }

    /**
     * Tests whether this object has been deleted.
     * Instances that have been deleted in the current transaction return true.
     * <P>Transient instances return false.
     * @param pc the calling persistable instance
     * @return true if this instance was deleted in the current transaction.
     */
    public boolean isDeleted(PersistenceCapable pc)
    {
        if (disconnectClone(pc))
        {
            return false;
        }
        else
        {
            return myLC.isDeleted();
        }
    }

    /**
     * Method to change the object state to evicted.
     */
    public void evict()
    {
        if (myLC != myOM.getNucleusContext().getApiAdapter().getLifeCycleState(LifeCycleState.P_CLEAN) &&
            myLC != myOM.getNucleusContext().getApiAdapter().getLifeCycleState(LifeCycleState.P_NONTRANS))
        {
            return;
        }

        preStateChange();
        try
        {
            try
            {
                getCallbackHandler().preClear(myPC);

                getCallbackHandler().postClear(myPC);
            }
            finally
            {
                myLC = myLC.transitionEvict(this);
            }
        }
        finally
        {
            postStateChange();
        }
    }

    /**
     * Method to refresh the object.
     */
    public void refresh()
    {
        preStateChange();
        try
        {
            myLC = myLC.transitionRefresh(this);
        }
        finally
        {
            postStateChange();
        }
    }

    /**
     * Method to retrieve the object.
     * @param fgOnly Only load the current fetch group fields
     */
    public void retrieve(boolean fgOnly)
    {
        preStateChange();
        try
        {
            myLC = myLC.transitionRetrieve(this, fgOnly);
        }
        finally
        {
            postStateChange();
        }
    }

    /**
     * Method to retrieve the object.
     * @param fetchPlan the fetch plan to load fields
     */
    public void retrieve(FetchPlan fetchPlan)
    {
        preStateChange();
        try
        {
            myLC = myLC.transitionRetrieve(this, fetchPlan);
        }
        finally
        {
            postStateChange();
        }
    }

    /**
     * Makes Transactional Transient instances persistent.
     */
    public void makePersistentTransactionalTransient()
    {
        preStateChange();
        try
        {
            if (myLC.isTransactional && !myLC.isPersistent)
            {
                // make the transient instance persistent in the datastore, if is transactional and !persistent 
                makePersistent();
                myLC = myLC.transitionMakePersistent(this);
            }
        }
        finally
        {
            postStateChange();
        }
    }

    /**
     * Method to change the object state to nontransactional.
     */
    public void makeNontransactional()
    {
        preStateChange();
        try
        {
            myLC = myLC.transitionMakeNontransactional(this);
        }
        finally
        {
            postStateChange();
        }
    }

    /**
     * Method to change the object state to read-field.
     * @param isLoaded if the field was previously loaded
     */
    protected void transitionReadField(boolean isLoaded)
    {
        try
        {
            if (myOM.getMultithreaded())
            {
                myOM.getLock().lock();
                lock.lock();
            }

            if (myLC == null)
            {
                return;
            }
            preStateChange();
            try
            {
                myLC = myLC.transitionReadField(this, isLoaded);
            }
            finally
            {
                postStateChange();
            }
        }
        finally
        {
            if (myOM.getMultithreaded())
            {
                lock.unlock();
                myOM.getLock().unlock();
            }
        }
    }

    /**
     * Method to change the object state to write-field.
     */
    protected void transitionWriteField()
    {
        try
        {
            if (myOM.getMultithreaded())
            {
                myOM.getLock().lock();
                lock.lock();
            }

            preStateChange();
            try
            {
                myLC = myLC.transitionWriteField(this);
            }
            finally
            {
                postStateChange();
            }
        }
        finally
        {
            if (myOM.getMultithreaded())
            {
                lock.unlock();
                myOM.getLock().unlock();
            }
        }
    }

    /**
     * Method invoked just before a transaction starts for the ObjectManager managing us.
     * @param tx The transaction
     */
    public void preBegin(org.datanucleus.Transaction tx)
    {
        preStateChange();
        try
        {
            myLC = myLC.transitionBegin(this, tx);
        }
        finally
        {
            postStateChange();
        }
    }

    /**
     * This method is invoked just after a commit is performed in a Transaction
     * involving the persistable object managed by this StateManager
     * @param tx The transaction
     */
    public void postCommit(org.datanucleus.Transaction tx)
    {
        preStateChange();
        try
        {
            myLC = myLC.transitionCommit(this, tx);
            if (transactionalVersion != myVersion)
            {
                myVersion = transactionalVersion;
            }
            this.lockMode = LockManager.LOCK_MODE_NONE;
        }
        finally
        {
            postStateChange();
        }
    }

    /**
     * This method is invoked just before a rollback is performed in a Transaction
     * involving the persistable object managed by this StateManager.
     * @param tx The transaction
     */
    public void preRollback(org.datanucleus.Transaction tx)
    {
        preStateChange();
        try
        {
            myOM.clearDirty(this);
            myLC = myLC.transitionRollback(this, tx);
            if (transactionalVersion != myVersion)
            {
                transactionalVersion = myVersion;
            }
            this.lockMode = LockManager.LOCK_MODE_NONE;
        }
        finally
        {
            postStateChange();
        }
    }

    /**
     * Method called before a change in state.
     */
    protected abstract void preStateChange();

    /**
     * Method called after a change in state.
     */
    protected abstract void postStateChange();

    // -------------------------- Version handling ----------------------------

    /** 
     * Return the object representing the version of the calling instance.
     * @param pc the calling persistable instance
     * @return the object representing the version of the calling instance
     * @since JDO 2.0
     */    
    public Object getVersion(PersistenceCapable pc)
    {
        if (pc == myPC)
        {
            // JIRA-2993 This used to return myVersion but now we use transactionalVersion
            return transactionalVersion;
        }
        else
        {
            return null;
        }
    }

    /**
     * Method to return the current version of the managed object.
     * @return The version
     */
    public Object getVersion()
    {
        return getVersion(myPC);
    }

    /**
     * Sets the value for the version column in a transaction not yet committed
     * @param version The version
     */
    public void setTransactionalVersion(Object version)
    {
        this.transactionalVersion = version;
    }

    /**
     * Return the object representing the transactional version of the calling instance.
     * @param pc the calling persistable instance
     * @return the object representing the version of the calling instance
     */    
    public Object getTransactionalVersion(Object pc)
    {
        return this.transactionalVersion;
    }

    /**
     * Return the transactional version of the managed object.
     * @return Version of the managed instance at this point in the transaction
     */    
    public Object getTransactionalVersion()
    {
        return getTransactionalVersion(myPC);
    }

    /**
     * Sets the value for the version column in the datastore
     * @param version The version
     */
    public void setVersion(Object version)
    {
        this.myVersion = version;
        this.transactionalVersion = version;
    }

    /**
     * Convenience accessor for whether this StateManager manages an embedded/serialised object.
     * @return Whether the managed object is embedded/serialised.
     */
    public boolean isEmbedded()
    {
        return pcObjectType > 0;
    }

    /**
     * Method to set this StateManager as managing an embedded/serialised object.
     * @param embeddedType The type of object being managed
     */
    public void setPcObjectType(short embeddedType)
    {
        this.pcObjectType = embeddedType;
    }

    // -------------------------- Field Handling Methods ------------------------------

    /**
     * Accessor for the highest field number in this class
     * @return The highest field number
     */
    public int getHighestFieldNumber()
    {
        return cmd.getMemberCount();
    }

    /**
     * Accessor for whether the current fetch plan fields are loaded.
     * @return Whether the fetch plan fields are all loaded.
     */
    protected boolean isFetchPlanLoaded()
    {
        int[] fpFields = myFP.getMemberNumbers();
        for (int i=0; i<fpFields.length; ++i)
        {
            if (!loadedFields[fpFields[i]])
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Accessor for whether the DFG fields are loaded.
     * @return Whether the DFG fields are all loaded.
     */
    protected boolean isDefaultFetchGroupLoaded()
    {
        int[] dfgFields = cmd.getDFGMemberPositions();
        for (int i=0; i<dfgFields.length; ++i)
        {
            if (!loadedFields[dfgFields[i]])
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Creates a copy of the {@link #dirtyFields} bitmap.
     * @return a copy of the {@link #dirtyFields} bitmap.
     */
    public boolean[] getDirtyFields()
    {
        boolean[] copy = new boolean[dirtyFields.length];
        System.arraycopy(dirtyFields, 0, copy, 0, dirtyFields.length);
        return copy;
    }

    /**
     * Accessor for the field numbers of all dirty fields.
     * @return Absolute field numbers of the dirty fields in this instance.
     */
    public int[] getDirtyFieldNumbers()
    {
        return getFlagsSetTo(dirtyFields, true);
    }

    /**
     * Accessor for the fields
     * @return boolean array of loaded state in order of absolute field numbers
     */
    public boolean[] getLoadedFields() 
    {
        return loadedFields.clone();
    }

    /**
     * Accessor for the field numbers of all loaded fields in this managed instance.
     * @return Field numbers of all (currently) loaded fields
     */
    public int[] getLoadedFieldNumbers()
    {
        return getFlagsSetTo(loadedFields, true);
    }

    /**
     * Returns whether all fields are loaded.
     * @return Returns true if all fields are loaded.
     */
    public boolean getAllFieldsLoaded()
    {
        for (int i = 0;i<loadedFields.length;i++)
        {
            if (!loadedFields[i])
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Accessor for the names of the fields that are dirty.
     * @return Names of the dirty fields
     */
    public String[] getDirtyFieldNames()
    {
        int[] dirtyFieldNumbers = getFlagsSetTo(dirtyFields, true);
        if (dirtyFieldNumbers != null && dirtyFieldNumbers.length > 0)
        {
            String[] dirtyFieldNames = new String[dirtyFieldNumbers.length];
            for (int i=0;i<dirtyFieldNumbers.length;i++)
            {
                dirtyFieldNames[i] = cmd.getMetaDataForManagedMemberAtAbsolutePosition(dirtyFieldNumbers[i]).getName();
            }
            return dirtyFieldNames;
        }
        return null;
    }

    /**
     * Accessor for the names of the fields that are loaded.
     * @return Names of the loaded fields
     */
    public String[] getLoadedFieldNames()
    {
        int[] loadedFieldNumbers = getFlagsSetTo(loadedFields, true);
        if (loadedFieldNumbers != null && loadedFieldNumbers.length > 0)
        {
            String[] loadedFieldNames = new String[loadedFieldNumbers.length];
            for (int i=0;i<loadedFieldNumbers.length;i++)
            {
                loadedFieldNames[i] = cmd.getMetaDataForManagedMemberAtAbsolutePosition(loadedFieldNumbers[i]).getName();
            }
            return loadedFieldNames;
        }
        return null;
    }

    /**
     * Accessor for whether a field is currently loaded.
     * Just returns the status, unlike "isLoaded" which also loads it if not.
     * @param fieldNumber The (absolute) field number
     * @return Whether it is loaded
     */
    public boolean isFieldLoaded(int fieldNumber)
    {
        return loadedFields[fieldNumber];
    }

    /**
     * Method to clear all dirty flags on the object.
     */
    protected void clearDirtyFlags()
    {
        dirty = false;
        clearFlags(dirtyFields);
    }
    
    /**
     * Method to clear all dirty flags on the object.
     * @param fields the fields to clear
     */
    protected void clearDirtyFlags(int[] fields)
    {
        dirty = false;
        clearFlags(dirtyFields,fields);
    }

    /**
     * Method to clear all fields of the object.
     */
    public void clearFields()
    {
        try
        {
            getCallbackHandler().preClear(myPC);
        }
        finally
        {
            clearFieldsByNumbers(getAllFieldNumbers());
            clearDirtyFlags();

            if (getStoreManager() instanceof ObjectReferencingStoreManager)
            {
                // For datastores that manage the object reference
                ((ObjectReferencingStoreManager)getStoreManager()).notifyObjectIsOutdated(this);
            }
            jdoDfgFlags = PersistenceCapable.LOAD_REQUIRED;
            myPC.jdoReplaceFlags();

            getCallbackHandler().postClear(myPC);
        }
    }

    /**
     * Method to clear all fields that are not part of the primary key of the object.
     */
    public void clearNonPrimaryKeyFields()
    {
        try
        {
            getCallbackHandler().preClear(myPC);
        }
        finally
        {
            clearFieldsByNumbers(getNonPrimaryKeyFieldNumbers());
            clearDirtyFlags(getNonPrimaryKeyFieldNumbers());

            if (getStoreManager() instanceof ObjectReferencingStoreManager)
            {
                // For datastores that manage the object reference
                ((ObjectReferencingStoreManager)getStoreManager()).notifyObjectIsOutdated(this);
            }

            jdoDfgFlags = PersistenceCapable.LOAD_REQUIRED;
            myPC.jdoReplaceFlags();

            getCallbackHandler().postClear(myPC);
        }
    }

    /**
     * Method to clear all loaded flags on the object.
     * Note that the contract of this method implies, especially for object database backends, that the memory form
     * of the object is outdated.
     * Thus, for features like implicit saving of dirty object subgraphs should be switched off for this PC, even if the 
     * object actually looks like being dirty (because it is being changed to null values).
     */
    public void clearLoadedFlags()
    {
        if (getStoreManager() instanceof ObjectReferencingStoreManager)
        {
            // For datastores that manage the object reference
            ((ObjectReferencingStoreManager)getStoreManager()).notifyObjectIsOutdated(this);
        }

        jdoDfgFlags = PersistenceCapable.LOAD_REQUIRED;
        myPC.jdoReplaceFlags();
        clearFlags(loadedFields);
    }

    protected void clearFieldsByNumbers(int[] fieldNumbers)
    {
        replaceFields(fieldNumbers, HOLLOWFIELDMANAGER);
        for (int i=0;i<fieldNumbers.length;i++)
        {
            loadedFields[fieldNumbers[i]] = false;
            dirtyFields[fieldNumbers[i]] = false;
        }
    }

    /**
     * The StateManager uses this method to supply the value of jdoFlags to the
     * associated PersistenceCapable instance.
     * @param pc the calling PersistenceCapable instance
     * @return the value of jdoFlags to be stored in the PersistenceCapable instance
     */
    public byte replacingFlags(PersistenceCapable pc)
    {
        // If this is a clone, return READ_WRITE_OK.
        if (pc != myPC)
        {
            return PersistenceCapable.READ_WRITE_OK;
        }
        else
        {
            return jdoDfgFlags;
        }
    }

    // -------------------------- providedXXXField Methods ----------------------------

    /**
     * This method is called from the associated persistable when its
     * PersistenceCapable.jdoProvideFields() method is invoked. Its purpose is
     * to provide the value of the specified field to the StateManager.
     *
     * @param pc the calling persistable instance
     * @param field the field number
     * @param currentValue the current value of the field
     */
    public void providedBooleanField(PersistenceCapable pc, int field, boolean currentValue)
    {
        currFM.storeBooleanField(field, currentValue);
    }

    /**
     * This method is called from the associated persistable when its
     * PersistenceCapable.jdoProvideFields() method is invoked. Its purpose is
     * to provide the value of the specified field to the StateManager.
     *
     * @param pc the calling persistable instance
     * @param field the field number
     * @param currentValue the current value of the field
     */
    public void providedByteField(PersistenceCapable pc, int field, byte currentValue)
    {
        currFM.storeByteField(field, currentValue);
    }

    /**
     * This method is called from the associated PersistenceCapable when its
     * PersistenceCapable.jdoProvideFields() method is invoked. Its purpose is
     * to provide the value of the specified field to the StateManager.
     *
     * @param pc the calling persistable instance
     * @param field the field number
     * @param currentValue the current value of the field
     */
    public void providedCharField(PersistenceCapable pc, int field, char currentValue)
    {
        currFM.storeCharField(field, currentValue);
    }

    /**
     * This method is called from the associated PersistenceCapable when its
     * PersistenceCapable.jdoProvideFields() method is invoked. Its purpose is
     * to provide the value of the specified field to the StateManager.
     *
     * @param pc the calling persistable instance
     * @param field the field number
     * @param currentValue the current value of the field
     */
    public void providedDoubleField(PersistenceCapable pc, int field, double currentValue)
    {
        currFM.storeDoubleField(field, currentValue);
    }

    /**
     * This method is called from the associated PersistenceCapable when its
     * PersistenceCapable.jdoProvideFields() method is invoked. Its purpose is
     * to provide the value of the specified field to the StateManager.
     *
     * @param pc the calling persistable instance
     * @param field the field number
     * @param currentValue the current value of the field
     */
    public void providedFloatField(PersistenceCapable pc, int field, float currentValue)
    {
        currFM.storeFloatField(field, currentValue);
    }

    /**
     * This method is called from the associated PersistenceCapable when its
     * PersistenceCapable.jdoProvideFields() method is invoked. Its purpose is
     * to provide the value of the specified field to the StateManager.
     *
     * @param pc the calling persistable instance
     * @param field the field number
     * @param currentValue the current value of the field
     */
    public void providedIntField(PersistenceCapable pc, int field, int currentValue)
    {
        currFM.storeIntField(field, currentValue);
    }

    /**
     * This method is called from the associated PersistenceCapable when its
     * PersistenceCapable.jdoProvideFields() method is invoked. Its purpose is
     * to provide the value of the specified field to the StateManager.
     *
     * @param pc the calling persistable instance
     * @param field the field number
     * @param currentValue the current value of the field
     */
    public void providedLongField(PersistenceCapable pc, int field, long currentValue)
    {
        currFM.storeLongField(field, currentValue);
    }

    /**
     * This method is called from the associated PersistenceCapable when its
     * PersistenceCapable.jdoProvideFields() method is invoked. Its purpose is
     * to provide the value of the specified field to the StateManager.
     *
     * @param pc the calling persistable instance
     * @param field the field number
     * @param currentValue the current value of the field
     */
    public void providedShortField(PersistenceCapable pc, int field, short currentValue)
    {
        currFM.storeShortField(field, currentValue);
    }

    /**
     * This method is called from the associated persistable when its
     * PersistenceCapable.jdoProvideFields() method is invoked. Its purpose is
     * to provide the value of the specified field to the StateManager.
     *
     * @param pc the calling persistable instance
     * @param field the field number
     * @param currentValue the current value of the field
     */
    public void providedStringField(PersistenceCapable pc, int field, String currentValue)
    {
        currFM.storeStringField(field, currentValue);
    }

    /**
     * This method is called from the associated persistable when its
     * PersistenceCapable.jdoProvideFields() method is invoked. Its purpose is
     * to provide the value of the specified field to the StateManager.
     *
     * @param pc the calling PersistenceCapable instance
     * @param fieldNumber the field number
     * @param currentValue the current value of the field
     */
    public void providedObjectField(PersistenceCapable pc, int fieldNumber, Object currentValue)
    {
        currFM.storeObjectField(fieldNumber, currentValue);
    }

    /**
     * Method to retrieve the value of a field from the PC object.
     * Assumes that it is loaded.
     * @param pc The PC object
     * @param fieldNumber Number of field
     * @return The value of the field
     */
    protected Object provideField(PersistenceCapable pc, int fieldNumber)
    {
        Object obj;
        try
        {
            if (myOM.getMultithreaded())
            {
                myOM.getLock().lock();
                lock.lock();
            }

            FieldManager prevFM = currFM;
            currFM = new SingleValueFieldManager();
            try
            {
                pc.jdoProvideField(fieldNumber);
                obj = currFM.fetchObjectField(fieldNumber);
            }
            finally
            {
                currFM = prevFM;
            }
        }
        finally
        {
            if (myOM.getMultithreaded())
            {
                lock.unlock();
                myOM.getLock().unlock();
            }
        }

        return obj;
    }

    /**
     * Called from the StoreManager after StoreManager.update() is called to obtain updated values 
     * from the PersistenceCapable associated with this StateManager.
     * @param fieldNumbers An array of field numbers to be updated by the Store
     * @param fm The updated values are stored in this object. This object is only valid
     *   for the duration of this call.
     */
    public void provideFields(int fieldNumbers[], FieldManager fm)
    {
        try
        {
            if (myOM.getMultithreaded())
            {
                myOM.getLock().lock();
                lock.lock();
            }

            FieldManager prevFM = currFM;
            currFM = fm;

            try
            {
                // This will respond by calling this.providedXXXFields() with the value of the field
                myPC.jdoProvideFields(fieldNumbers);
            }
            finally
            {
                currFM = prevFM;
            }
        }
        finally
        {
            if (myOM.getMultithreaded())
            {
                lock.unlock();
                myOM.getLock().unlock();
            }
        }
    }

    // -------------------------- replacingXXXField Methods ----------------------------

    /**
     * Fetchs from the database all fields in current fetch plan that are not currently loaded as well as
     * the version. Called by lifecycle transitions.
     */
    public abstract void loadUnloadedFieldsInFetchPlanAndVersion();

    /**
     * Method to change the value of a field in the PC object.
     * Adds on handling for embedded fields to the superclass handler.
     * @param pc The PC object
     * @param fieldNumber Number of field
     * @param value The new value of the field
     * @param makeDirty Whether to make the field dirty while replacing its value (in embedded owners)
     */
    protected abstract void replaceField(PersistenceCapable pc, int fieldNumber, Object value, boolean makeDirty);

    /**
     * Method called before a write of the specified field.
     * @param field The field to write
     * @return true if the field was already dirty before
     */
    protected abstract boolean preWriteField(int field);

    /**
     * Method called after the write of a field.
     * @param wasDirty whether before writing this field the pc was dirty
     */
    protected abstract void postWriteField(boolean wasDirty);

    /**
     * This method is called by the associated PersistenceCapable when the
     * corresponding mutator method (setXXX()) is called on the PersistenceCapable.
     *
     * @param pc the calling PersistenceCapable instance
     * @param field the field number
     * @param currentValue the current value of the field
     * @param newValue the new value for the field
     */
    public void setBooleanField(PersistenceCapable pc, int field, boolean currentValue, boolean newValue)
    {
        if (pc != myPC)
        {
            replaceField(pc, field, newValue ? Boolean.TRUE : Boolean.FALSE, true);
            disconnectClone(pc);
        }
        else if (myLC != null)
        {
            if (cmd.isVersioned() && transactionalVersion == null)
            {
                // Not got version but should have
                loadUnloadedFieldsInFetchPlanAndVersion();
            }

            if (!loadedFields[field] || currentValue != newValue)
            {
                boolean wasDirty = preWriteField(field);
                replaceField(pc, field, newValue ? Boolean.TRUE : Boolean.FALSE, true);
                postWriteField(wasDirty);
            }
        }
        else
        {
            replaceField(pc, field, newValue ? Boolean.TRUE : Boolean.FALSE, true);
        }
    }

    /**
     * This method is called by the associated PersistenceCapable when the
     * corresponding mutator method (setXXX()) is called on the PersistenceCapable.
     *
     * @param pc the calling PersistenceCapable instance
     * @param field the field number
     * @param currentValue the current value of the field
     * @param newValue the new value for the field
     */
    public void setByteField(PersistenceCapable pc, int field, byte currentValue, byte newValue)
    {
        if (pc != myPC)
        {
            replaceField(pc, field, Byte.valueOf(newValue), true);
            disconnectClone(pc);
        }
        else if (myLC != null)
        {
            if (cmd.isVersioned() && transactionalVersion == null)
            {
                // Not got version but should have
                loadUnloadedFieldsInFetchPlanAndVersion();
            }

            if (!loadedFields[field] || currentValue != newValue)
            {
                boolean wasDirty = preWriteField(field);
                replaceField(pc, field, Byte.valueOf(newValue), true);
                postWriteField(wasDirty);
            }
        }
        else
        {
            replaceField(pc, field, Byte.valueOf(newValue), true);
        }
    }

    /**
     * This method is called by the associated PersistenceCapable when the
     * corresponding mutator method (setXXX()) is called on the PersistenceCapable.
     *
     * @param pc the calling PersistenceCapable instance
     * @param field the field number
     * @param currentValue the current value of the field
     * @param newValue the new value for the field
     */
    public void setCharField(PersistenceCapable pc, int field, char currentValue, char newValue)
    {
        if (pc != myPC)
        {
            replaceField(pc, field, Character.valueOf(newValue), true);
            disconnectClone(pc);
        }
        else if (myLC != null)
        {
            if (cmd.isVersioned() && transactionalVersion == null)
            {
                // Not got version but should have
                loadUnloadedFieldsInFetchPlanAndVersion();
            }

            if (!loadedFields[field] || currentValue != newValue)
            {
                boolean wasDirty = preWriteField(field);
                replaceField(pc, field, Character.valueOf(newValue), true);
                postWriteField(wasDirty);
            }
        }
        else
        {
            replaceField(pc, field, Character.valueOf(newValue), true);
        }
    }

    /**
     * This method is called by the associated PersistenceCapable when the
     * corresponding mutator method (setXXX()) is called on the PersistenceCapable.
     *
     * @param pc the calling PersistenceCapable instance
     * @param field the field number
     * @param currentValue the current value of the field
     * @param newValue the new value for the field
     */
    public void setDoubleField(PersistenceCapable pc, int field, double currentValue, double newValue)
    {
        if (pc != myPC)
        {
            replaceField(pc, field, Double.valueOf(newValue), true);
            disconnectClone(pc);
        }
        else if (myLC != null)
        {
            if (cmd.isVersioned() && transactionalVersion == null)
            {
                // Not got version but should have
                loadUnloadedFieldsInFetchPlanAndVersion();
            }

            if (!loadedFields[field] || currentValue != newValue)
            {
                boolean wasDirty = preWriteField(field);
                replaceField(pc, field, Double.valueOf(newValue), true);
                postWriteField(wasDirty);
            }
        }
        else
        {
            replaceField(pc, field, Double.valueOf(newValue), true);
        }
    }

    /**
     * This method is called by the associated PersistenceCapable when the
     * corresponding mutator method (setXXX()) is called on the PersistenceCapable.
     *
     * @param pc the calling PersistenceCapable instance
     * @param field the field number
     * @param currentValue the current value of the field
     * @param newValue the new value for the field
     */
    public void setFloatField(PersistenceCapable pc, int field, float currentValue, float newValue)
    {
        if (pc != myPC)
        {
            replaceField(pc, field, Float.valueOf(newValue), true);
            disconnectClone(pc);
        }
        else if (myLC != null)
        {
            if (cmd.isVersioned() && transactionalVersion == null)
            {
                // Not got version but should have
                loadUnloadedFieldsInFetchPlanAndVersion();
            }

            if (!loadedFields[field] || currentValue != newValue)
            {
                boolean wasDirty = preWriteField(field);
                replaceField(pc, field, Float.valueOf(newValue), true);
                postWriteField(wasDirty);
            }
        }
        else
        {
            replaceField(pc, field, Float.valueOf(newValue), true);
        }
    }

    /**
     * This method is called by the associated PersistenceCapable when the
     * corresponding mutator method (setXXX()) is called on the PersistenceCapable.
     *
     * @param pc the calling PersistenceCapable instance
     * @param field the field number
     * @param currentValue the current value of the field
     * @param newValue the new value for the field
     */
    public void setIntField(PersistenceCapable pc, int field, int currentValue, int newValue)
    {
        if (pc != myPC)
        {
            replaceField(pc, field, Integer.valueOf(newValue), true);
            disconnectClone(pc);
        }
        else if (myLC != null)
        {
            if (cmd.isVersioned() && transactionalVersion == null)
            {
                // Not got version but should have
                loadUnloadedFieldsInFetchPlanAndVersion();
            }

            if (!loadedFields[field] || currentValue != newValue)
            {
                boolean wasDirty = preWriteField(field);
                replaceField(pc, field, Integer.valueOf(newValue), true);
                postWriteField(wasDirty);
            }
        }
        else
        {
            replaceField(pc, field, Integer.valueOf(newValue), true);
        }
    }

    /**
     * This method is called by the associated PersistenceCapable when the
     * corresponding mutator method (setXXX()) is called on the PersistenceCapable.
     *
     * @param pc the calling PersistenceCapable instance
     * @param field the field number
     * @param currentValue the current value of the field
     * @param newValue the new value for the field
     */
    public void setLongField(PersistenceCapable pc, int field, long currentValue, long newValue)
    {
        if (pc != myPC)
        {
            replaceField(pc, field, Long.valueOf(newValue), true);
            disconnectClone(pc);
        }
        else if (myLC != null)
        {
            if (cmd.isVersioned() && transactionalVersion == null)
            {
                // Not got version but should have
                loadUnloadedFieldsInFetchPlanAndVersion();
            }

            if (!loadedFields[field] || currentValue != newValue)
            {
                boolean wasDirty = preWriteField(field);
                replaceField(pc, field, Long.valueOf(newValue), true);
                postWriteField(wasDirty);
            }
        }
        else
        {
            replaceField(pc, field, Long.valueOf(newValue), true);
        }
    }

    /**
     * This method is called by the associated PersistenceCapable when the
     * corresponding mutator method (setXXX()) is called on the PersistenceCapable.
     *
     * @param pc the calling PersistenceCapable instance
     * @param field the field number
     * @param currentValue the current value of the field
     * @param newValue the new value for the field
     */
    public void setShortField(PersistenceCapable pc, int field, short currentValue, short newValue)
    {
        if (pc != myPC)
        {
            replaceField(pc, field, Short.valueOf(newValue), true);
            disconnectClone(pc);
        }
        else if (myLC != null)
        {
            if (cmd.isVersioned() && transactionalVersion == null)
            {
                // Not got version but should have
                loadUnloadedFieldsInFetchPlanAndVersion();
            }

            if (!loadedFields[field] || currentValue != newValue)
            {
                boolean wasDirty = preWriteField(field);
                replaceField(pc, field, Short.valueOf(newValue), true);
                postWriteField(wasDirty);
            }
        }
        else
        {
            replaceField(pc, field, Short.valueOf(newValue), true);
        }
    }

    /**
     * This method is called by the associated PersistenceCapable when the
     * corresponding mutator method (setXXX()) is called on the
     * PersistenceCapable.
     *
     * @param pc the calling PersistenceCapable instance
     * @param field the field number
     * @param currentValue the current value of the field
     * @param newValue the new value for the field
     */
    public void setStringField(PersistenceCapable pc, int field, String currentValue, String newValue)
    {
        if (pc != myPC)
        {
            replaceField(pc, field, newValue, true);
            disconnectClone(pc);
        }
        else if (myLC != null)
        {
            if (cmd.isVersioned() && transactionalVersion == null)
            {
                // Not got version but should have
                loadUnloadedFieldsInFetchPlanAndVersion();
            }

            if (!loadedFields[field] || !equals(currentValue, newValue))
            {
                boolean wasDirty = preWriteField(field);
                replaceField(pc, field, newValue, true);
                postWriteField(wasDirty);
            }
        }
        else
        {
            replaceField(pc, field, newValue, true);
        }
    }

    /**
     * This method is called by the associated PersistenceCapable when the
     * corresponding mutator method (setXXX()) is called on the PersistenceCapable.
     * @param pc the calling PersistenceCapable instance
     * @param field the field number
     * @param currentValue the current value of the field
     * @param newValue the new value for the field
     */
    public void setObjectField(PersistenceCapable pc, int field, Object currentValue, Object newValue)
    {
        if (currentValue != null && currentValue != newValue && currentValue instanceof PersistenceCapable)
        {
            // Where the object is embedded, remove the owner from its old value since it is no longer managed by this StateManager
            JDOStateManagerImpl currentSM = (JDOStateManagerImpl)myOM.findObjectProvider(currentValue);
            if (currentSM != null && currentSM.isEmbedded())
            {
                currentSM.removeEmbeddedOwner(this, field);
            }
        }

        if (pc != myPC)
        {
            // Clone
            replaceField(pc, field, newValue, true);
            disconnectClone(pc);
        }
        else if (myLC != null)
        {
            if (cmd.isVersioned() && transactionalVersion == null)
            {
                // Not got version but should have
                loadUnloadedFieldsInFetchPlanAndVersion();
            }

            boolean loadedOldValue = false;
            Object oldValue = currentValue;
            AbstractMemberMetaData fmd = cmd.getMetaDataForManagedMemberAtAbsolutePosition(field);
            ClassLoaderResolver clr = myOM.getClassLoaderResolver();
            int relationType = fmd.getRelationType(clr);

            // Remove this object from L2 cache since now dirty to avoid potential problems
            myOM.removeObjectFromLevel2Cache(myID);

            if (!loadedFields[field] && currentValue == null)
            {
                // Updating value of a field that isnt currently loaded
                if (myOM.getManageRelations() &&
                    (relationType == Relation.ONE_TO_ONE_BI || relationType == Relation.MANY_TO_ONE_BI))
                {
                    // Managed relation field, so load old value
                    loadField(field);
                    loadedOldValue = true;
                    oldValue = provideField(field);
                }

                if (relationType != Relation.NONE && newValue == null && 
                    (fmd.isDependent() || fmd.isCascadeRemoveOrphans()))
                {
                    // Field being nulled and is dependent so load the existing value so it can be deleted
                    loadField(field);
                    loadedOldValue = true;
                    oldValue = provideField(field);
                }
                // TODO When field has relation consider loading it always for managed relations
            }

            // Check equality of old and new values
            boolean equal = false;
            if (oldValue == null && newValue == null)
            {
                equal = true;
            }
            else if (oldValue != null && newValue != null)
            {
                if (oldValue instanceof PersistenceCapable)
                {
                    // PC object field so compare object equality
                    // See JDO2 [5.4] "The JDO implementation must not use the application's hashCode and equals methods 
                    // from the persistence-capable classes except as needed to implement the Collections Framework" 
                    if (oldValue == newValue)
                    {
                        equal = true;
                    }
                }
                else
                {
                    // Non-PC object field so compare using equals()
                    if (oldValue.equals(newValue))
                    {
                        equal = true;
                    }
                }
            }

            // Update the field
            boolean needsSCOUpdating = false;
            if (!loadedFields[field] || !equal || fmd.hasArray())
            {
                // Either field isn't loaded, or has changed, or is an array.
                // We include arrays here since we have no way of knowing if the array element has changed
                // except if the user sets the array field. See JDO2 [6.3] that the application should
                // replace the value with its current value.
                boolean wasDirty = preWriteField(field);

                if (oldValue instanceof SCO)
                {
                    if (oldValue instanceof SCOContainer)
                    {
                        // Make sure container values are loaded
                        ((SCOContainer)oldValue).load();
                    }
                    ((SCO) oldValue).unsetOwner();
                }
                if (newValue instanceof SCO)
                {
                    SCO sco = (SCO) newValue;
                    Object owner = sco.getOwner();
                    if (owner != null)
                    {
                        throw new JDOUserException(LOCALISER.msg("026007", sco.getFieldName(), owner));
                    }
                }

                replaceField(pc, field, newValue, true);
                postWriteField(wasDirty);

                if (cmd.getSCOMutableMemberFlags()[field] && !(newValue instanceof SCO))
                {
                    // Need to wrap this field change
                    needsSCOUpdating = true;
                }
            }
            else if (loadedOldValue)
            {
                // We've updated the value with the old value (when retrieving it above), so put the new value back again
                boolean wasDirty = preWriteField(field);
                replaceField(pc, field, newValue, true);
                postWriteField(wasDirty);
            }

            if (!equal && Relation.isBidirectional(relationType)&& myOM.getManageRelations())
            {
                // Managed Relationships - add the field to be managed so we can analyse its value at flush
                myOM.getRelationshipManager(this).relationChange(field, oldValue, newValue);
            }

            if (needsSCOUpdating)
            {
                // Wrap with SCO so we can detect future updates
                newValue = wrapSCOField(field, newValue, false, true, true);
            }

            if (oldValue != null && newValue == null && oldValue instanceof PersistenceCapable)
            {
                if (fmd.isDependent() || fmd.isCascadeRemoveOrphans())
                {
                    if (myOM.getApiAdapter().isPersistent(oldValue))
                    {
                        // PC field being nulled, so delete previous PC value
                        NucleusLogger.PERSISTENCE.debug(LOCALISER.msg("026026", oldValue, fmd.getFullFieldName()));
                        myOM.deleteObjectInternal(oldValue);
                    }
                }
            }
        }
        else
        {
            replaceField(pc, field, newValue, true);
        }
    }

    /**
     * This method is invoked by the PersistenceCapable object's
     * jdoReplaceField() method to refresh the value of a boolean field.
     *
     * @param pc the calling persistable instance
     * @param field the field number
     * @return the new value for the field
     */
    public boolean replacingBooleanField(PersistenceCapable pc, int field)
    {
        boolean value = currFM.fetchBooleanField(field);
        loadedFields[field] = true;
    
        return value;
    }

    /**
     * This method is invoked by the PersistenceCapable object's
     * jdoReplaceField() method to refresh the value of a byte field.
     *
     * @param obj the calling persistable instance
     * @param field the field number
     * @return the new value for the field
     */
    public byte replacingByteField(PersistenceCapable obj, int field)
    {
        byte value = currFM.fetchByteField(field);
        loadedFields[field] = true;
    
        return value;
    }

    /**
     * This method is invoked by the PersistenceCapable object's
     * jdoReplaceField() method to refresh the value of a char field.
     *
     * @param obj the calling persistable instance
     * @param field the field number
     * @return the new value for the field
     */
    public char replacingCharField(PersistenceCapable obj, int field)
    {
        char value = currFM.fetchCharField(field);
        loadedFields[field] = true;
    
        return value;
    }

    /**
     * This method is invoked by the PersistenceCapable object's
     * jdoReplaceField() method to refresh the value of a double field.
     *
     * @param obj the calling PersistenceCapable instance
     * @param field the field number
     * @return the new value for the field
     */
    public double replacingDoubleField(PersistenceCapable obj, int field)
    {
        double value = currFM.fetchDoubleField(field);
        loadedFields[field] = true;
    
        return value;
    }

    /**
     * This method is invoked by the PersistenceCapable object's
     * jdoReplaceField() method to refresh the value of a float field.
     *
     * @param obj the calling PersistenceCapable instance
     * @param field the field number
     * @return the new value for the field
     */
    public float replacingFloatField(PersistenceCapable obj, int field)
    {
        float value = currFM.fetchFloatField(field);
        loadedFields[field] = true;
    
        return value;
    }

    /**
     * This method is invoked by the persistable object's
     * jdoReplaceField() method to refresh the value of a int field.
     *
     * @param obj the calling persistable instance
     * @param field the field number
     * @return the new value for the field
     */
    public int replacingIntField(PersistenceCapable obj, int field)
    {
        int value = currFM.fetchIntField(field);
        loadedFields[field] = true;
    
        return value;
    }

    /**
     * This method is invoked by the persistable object's
     * jdoReplaceField() method to refresh the value of a long field.
     *
     * @param obj the calling persistable instance
     * @param field the field number
     * @return the new value for the field
     */
    public long replacingLongField(PersistenceCapable obj, int field)
    {
        long value = currFM.fetchLongField(field);
        loadedFields[field] = true;
    
        return value;
    }

    /**
     * This method is invoked by the persistable object's
     * jdoReplaceField() method to refresh the value of a short field.
     *
     * @param obj the calling persistable instance
     * @param field the field number
     * @return the new value for the field
     */
    public short replacingShortField(PersistenceCapable obj, int field)
    {
        short value = currFM.fetchShortField(field);
        loadedFields[field] = true;
    
        return value;
    }

    /**
     * This method is invoked by the persistable object's
     * jdoReplaceField() method to refresh the value of a String field.
     *
     * @param obj the calling persistable instance
     * @param field the field number
     * @return the new value for the field
     */
    public String replacingStringField(PersistenceCapable obj, int field)
    {
        String value = currFM.fetchStringField(field);
        loadedFields[field] = true;
    
        return value;
    }

    /**
     * This method is invoked by the persistable object's
     * jdoReplaceField() method to refresh the value of an Object field.
     * @param obj the calling persistable instance
     * @param field the field number
     * @return the new value for the field
     */
    public Object replacingObjectField(PersistenceCapable obj, int field)
    {
        try
        {
            Object value = currFM.fetchObjectField(field);
            loadedFields[field] = true;
            return value;
        }
        catch (EndOfFetchPlanGraphException eodge)
        {
            // Beyond the scope of the fetch-depth when detaching
            return null;
        }
    }

    /**
     * Method to change the value of a field in the PC object.
     * @param pc The PC object
     * @param fieldNumber Number of field
     * @param value The new value of the field
     */
    protected void replaceField(PersistenceCapable pc, int fieldNumber, Object value)
    {
        try
        {
            if (myOM.getMultithreaded())
            {
                myOM.getLock().lock();
                lock.lock();
            }

            // Update the field in our PC object
            FieldManager prevFM = currFM;
            currFM = new SingleValueFieldManager();

            try
            {
                currFM.storeObjectField(fieldNumber, value);
                pc.jdoReplaceField(fieldNumber);
            }
            finally
            {
                currFM = prevFM;
            }
        }
        finally
        {
            if (myOM.getMultithreaded())
            {
                lock.unlock();
                myOM.getLock().unlock();
            }
        }
    }

    // -------------------------- getXXXField Methods ----------------------------

    /**
     * This method is called by the associated persistable if the
     * value for the specified field is not cached (StateManager.isLoaded()
     * fails). In this implementation of the StateManager, isLoaded() has a
     * side effect of loading unloaded information and will always return true.
     * As such, this method should never be called.
     * @param pc the calling persistable instance
     * @param field the field number 
     * @param currentValue the current value of the field
     * @return the new value for the field
     */
    public boolean getBooleanField(PersistenceCapable pc, int field, boolean currentValue)
    {
        throw new NucleusException(LOCALISER.msg("026006"));
    }

    /**
     * This method is called by the associated PersistenceCapable if the
     * value for the specified field is not cached (StateManager.isLoaded()
     * fails). In this implementation of the StateManager, isLoaded() has a
     * side effect of loading unloaded information and will always return true.
     * As such, this method should never be called.
     * @param pc the calling PersistenceCapable instance
     * @param field the field number 
     * @param currentValue the current value of the field
     * @return the new value for the field
     */
    public byte getByteField(PersistenceCapable pc, int field, byte currentValue)
    {
        throw new NucleusException(LOCALISER.msg("026006"));
    }

    /**
     * This method is called by the associated persistable if the
     * value for the specified field is not cached (StateManager.isLoaded()
     * fails). In this implementation of the StateManager, isLoaded() has a
     * side effect of loading unloaded information and will always return true.
     * As such, this method should never be called.
     * @param pc the calling persistable instance
     * @param field the field number 
     * @param currentValue the current value of the field
     * @return the new value for the field
     */
    public char getCharField(PersistenceCapable pc, int field, char currentValue)
    {
        throw new NucleusException(LOCALISER.msg("026006"));
    }

    /**
     * This method is called by the associated persistable if the
     * value for the specified field is not cached (StateManager.isLoaded()
     * fails). In this implementation of the StateManager, isLoaded() has a
     * side effect of loading unloaded information and will always return true.
     * As such, this method should never be called.
     * @param pc the calling persistable instance
     * @param field the field number 
     * @param currentValue the current value of the field
     * @return the new value for the field
     */
    public double getDoubleField(PersistenceCapable pc, int field, double currentValue)
    {
        throw new NucleusException(LOCALISER.msg("026006"));
    }

    /**
     * This method is called by the associated persistable if the
     * value for the specified field is not cached (StateManager.isLoaded()
     * fails). In this implementation of the StateManager, isLoaded() has a
     * side effect of loading unloaded information and will always return true.
     * As such, this method should never be called.
     * @param pc the calling persistable instance
     * @param field the field number 
     * @param currentValue the current value of the field
     * @return the new value for the field
     */
    public float getFloatField(PersistenceCapable pc, int field, float currentValue)
    {
        throw new NucleusException(LOCALISER.msg("026006"));
    }

    /**
     * This method is called by the associated persistable if the
     * value for the specified field is not cached (StateManager.isLoaded()
     * fails). In this implementation of the StateManager, isLoaded() has a
     * side effect of loading unloaded information and will always return true.
     * As such, this method should never be called.
     * @param pc the calling persistable instance
     * @param field the field number 
     * @param currentValue the current value of the field
     * @return the new value for the field
     */
    public int getIntField(PersistenceCapable pc, int field, int currentValue)
    {
        throw new NucleusException(LOCALISER.msg("026006"));
    }

    /**
     * This method is called by the associated persistable if the
     * value for the specified field is not cached (StateManager.isLoaded()
     * fails). In this implementation of the StateManager, isLoaded() has a
     * side effect of loading unloaded information and will always return true.
     * As such, this method should never be called.
     * @param pc the calling persistable instance
     * @param field the field number 
     * @param currentValue the current value of the field
     * @return the new value for the field
     */
    public long getLongField(PersistenceCapable pc, int field, long currentValue)
    {
        throw new NucleusException(LOCALISER.msg("026006"));
    }

    /**
     * This method is called by the associated persistable if the
     * value for the specified field is not cached (StateManager.isLoaded()
     * fails). In this implementation of the StateManager, isLoaded() has a
     * side effect of loading unloaded information and will always return true.
     * As such, this method should never be called.
     * @param pc the calling persistable instance
     * @param field the field number 
     * @param currentValue the current value of the field
     * @return the new value for the field
     */
    public short getShortField(PersistenceCapable pc, int field, short currentValue)
    {
        throw new NucleusException(LOCALISER.msg("026006"));
    }

    /**
     * This method is called by the associated persistable if the
     * value for the specified field is not cached (StateManager.isLoaded()
     * fails). In this implementation of the StateManager, isLoaded() has a
     * side effect of loading unloaded information and will always return true.
     * As such, this method should never be called.
     * @param pc the calling persistable instance
     * @param field the field number 
     * @param currentValue the current value of the field
     * @return the new value for the field
     */
    public String getStringField(PersistenceCapable pc, int field, String currentValue)
    {
        throw new NucleusException(LOCALISER.msg("026006"));
    }

    /**
     * This method is called by the associated persistable if the
     * value for the specified field is not cached (StateManager.isLoaded()
     * fails). In this implementation of the StateManager, isLoaded() has a
     * side effect of loading unloaded information and will always return true.
     * As such, this method should never be called.
     * @param pc the calling persistable instance
     * @param field the field number 
     * @param currentValue the current value of the field
     * @return the new value for the field
     */
    public Object getObjectField(PersistenceCapable pc, int field, Object currentValue)
    {
        throw new NucleusException(LOCALISER.msg("026006"));
    }

    // ------------------------------ Helper Methods ---------------------------

    /**
     * Compares two objects for equality, where one or both of the object
     * references may be null.
     *
     * @return  <code>true</code> if the objects are both <code>null</code> or
     *          compare equal according to their equals() method,
     *          <code>false</code> otherwise.
     */
    protected static boolean equals(Object o1, Object o2)
    {
        return o1 == null ? (o2 == null) : o1.equals(o2);
    }

    /**
     * Utility to clear the supplied flags.
     * @param flags
     */
    protected static void clearFlags(boolean[] flags)
    {
        for (int i = 0; i < flags.length; i++)
        {
            flags[i] = false;
        }
    }

    /**
     * Utility to clear the supplied flags.
     * @param flags
     * @param fields fields numbers where the flags will be cleared
     */
    protected static void clearFlags(boolean[] flags, int[] fields)
    {
        for (int i = 0; i < fields.length; i++)
        {
            flags[fields[i]] = false;
        }
    }
    
    /**
     * Returns an array of integers containing the indices of all elements in
     * <tt>flags</tt> that are in the <tt>state</tt> passed as argument.
     * @param flags Array of flags (true or false)
     * @param state The state to search (true or false)
     * @return The settings of the flags
     */
    public static int[] getFlagsSetTo(boolean[] flags, boolean state)
    {
        int[] temp = new int[flags.length];
        int j = 0;

        for (int i = 0; i < flags.length; i++)
        {
            if (flags[i] == state)
            {
                temp[j++] = i;
            }
        }

        if (j != 0)
        {
            int[] fieldNumbers = new int[j];
            System.arraycopy(temp, 0, fieldNumbers, 0, j);

            return fieldNumbers;
        }
        else
        {
            return null;
        }
    }

    /**
     * Returns an array of integers containing the indices of all elements in
     * <tt>flags</tt> whose index occurs in <tt>indices</tt> and whose value is
     * <tt>state</tt>.
     */
    protected static int[] getFlagsSetTo(boolean[] flags, int[] indices, boolean state)
    {
        int[] temp = new int[indices.length];
        int j = 0;

        for (int i = 0; i < indices.length; i++)
        {
            if (flags[indices[i]] == state)
            {
                temp[j++] = indices[i];
            }
        }

        if (j != 0)
        {
            int[] fieldNumbers = new int[j];
            System.arraycopy(temp, 0, fieldNumbers, 0, j);

            return fieldNumbers;
        }
        else
        {
            return null;
        }
    }
    
    /**
     * Utility to take a peek at a field in the persistable object.
     * @param obj The persistable object
     * @param fieldName The field to peek at
     * @return The value of the field.
     */
    protected static Object peekField(Object obj, String fieldName)
    {
        try
        {
            /*
             * This doesn't work due to security problems but you get the idea.
             * I'm trying to get field values directly without going through
             * the provideField machinery.
             */
            Object value = obj.getClass().getDeclaredField(fieldName).get(obj);
            if (value instanceof PersistenceCapable)
            {
                return StringUtils.toJVMIDString(value);
            }
            else
            {
                return value;
            }
        }
        catch (Exception e)
        {
            return e.toString();
        }
    }

    /* (non-Javadoc)
     * @see org.datanucleus.StateManager#lock(short)
     */
    public void lock(short lockMode)
    {
        this.lockMode = lockMode;
    }

    /* (non-Javadoc)
     * @see org.datanucleus.StateManager#unlock()
     */
    public void unlock()
    {
        this.lockMode = LockManager.LOCK_MODE_NONE;
    }

    /* (non-Javadoc)
     * @see org.datanucleus.StateManager#getLockMode()
     */
    public short getLockMode()
    {
        return lockMode;
    }

    public String toPrintableID()
    {
        return StringUtils.toJVMIDString(myPC);
    }

    /**
     * Stringifier method.
     * @return String form of the StateManager
     */
    public String toString()
    {
        return "StateManager[pc=" + StringUtils.toJVMIDString(myPC) + ", lifecycle=" + myLC + "]";
    }

    /**
     * Tests whether this object is being detached.
     * @return true if this instance is detaching.
     */
    public boolean isDetaching()
    {
        return ((flags&FLAG_DETACHING)!=0);
    }

    /**
     * Method to disconnect any cloned persistence capable objects from their StateManager.
     * @param pc The PersistenceCapable object
     * @return Whether the object was disconnected.
     */
    protected boolean disconnectClone(PersistenceCapable pc)
    {
        if (((flags&FLAG_DETACHING)!=0))
        {
            return false;
        }
        if (pc != myPC)
        {
            if (NucleusLogger.PERSISTENCE.isDebugEnabled())
            {
                NucleusLogger.PERSISTENCE.debug(LOCALISER.msg("026001", StringUtils.toJVMIDString(pc), this));
            }

            // Reset jdoFlags in the clone to PersistenceCapable.READ_WRITE_OK 
            // and clear its state manager.
            pc.jdoReplaceFlags();
            replaceStateManager(pc, null);
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Convenience method to retrieve the detach state from the passed State Manager's object.
     * @param sm The State Manager
     */
    public void retrieveDetachState(org.datanucleus.state.StateManager sm)
    {
        if (sm.getObject() instanceof Detachable)
        {
            ((JDOStateManagerImpl)sm).flags |= FLAG_RETRIEVING_DETACHED_STATE;
            ((Detachable)sm.getObject()).jdoReplaceDetachedState();
            ((JDOStateManagerImpl)sm).flags &=~FLAG_RETRIEVING_DETACHED_STATE;
        }
    }

    /**
     * Convenience method to reset the detached state in the current object.
     */
    public void resetDetachState()
    {
        if (getObject() instanceof Detachable)
        {
            flags |= FLAG_RESETTING_DETACHED_STATE;
            try
            {
                ((Detachable)getObject()).jdoReplaceDetachedState();
            }
            finally
            {
                flags &=~FLAG_RESETTING_DETACHED_STATE;
            }
        }
    }

    /**
     * Method to update the "detached state" in the detached object to obtain the "detached state" 
     * from the detached object, or to reset it (to null).
     * @param pc The PersistenceCapable beind updated
     * @param currentState The current state values
     * @return The detached state to assign to the object
     */
    public Object[] replacingDetachedState(Detachable pc, Object[] currentState)
    {
        if ((flags&FLAG_RESETTING_DETACHED_STATE)!=0)
        {
            return null;
        }
        else if ((flags&FLAG_RETRIEVING_DETACHED_STATE)!=0)
        {
            // Retrieving the detached state from the detached object
            // Don't need the id or version since they can't change
            BitSet jdoLoadedFields = (BitSet)currentState[2];
            for (int i = 0; i < this.loadedFields.length; i++)
            {
                this.loadedFields[i] = jdoLoadedFields.get(i);
            }

            BitSet jdoModifiedFields = (BitSet)currentState[3];
            for (int i = 0; i < dirtyFields.length; i++)
            {
                dirtyFields[i] = jdoModifiedFields.get(i);
            }
            setVersion(currentState[1]);
            return currentState;
        }
        else
        {
            // Updating the detached state in the detached object with our state
            Object[] state = new Object[4];
            state[0] = myID;
            state[1] = getVersion(myPC);

            // Loaded fields
            BitSet loadedState = new BitSet();
            for (int i = 0; i < loadedFields.length; i++)
            {
                if (loadedFields[i])
                {
                    loadedState.set(i);
                }
                else
                {
                    loadedState.clear(i);
                }
            }
            state[2] = loadedState;

            // Modified fields
            BitSet modifiedState = new BitSet();
            for (int i = 0; i < dirtyFields.length; i++)
            {
                if (dirtyFields[i])
                {
                    modifiedState.set(i);
                }
                else
                {
                    modifiedState.clear(i);
                }
            }
            state[3] = modifiedState;

            return state;
        }
    }
}