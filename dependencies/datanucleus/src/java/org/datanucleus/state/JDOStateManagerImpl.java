/**********************************************************************
Copyright (c) 2002 Kelly Grizzle and others. All rights reserved.
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
2003 Erik Bengtson - removed exist() operation
2003 Andy Jefferson - added localiser
2003 Erik Bengtson - added new constructor for App ID
2003 Erik Bengtson - fixed loadDefaultFetchGroup to call jdoPostLoad
2003 Erik Bengtson - fixed evict to call jdoPreClear
2004 Andy Jefferson - converted to use Logger
2004 Andy Jefferson - reordered methods to put in categories, split String utilities across to StringUtils.
2004 Andy Jefferson - added Lifecycle Listener callbacks
2004 Andy Jefferson - removed JDK 1.4 methods so that we support 1.3 also
2005 Martin Taal - Contrib of detach() method for "detachOnClose" functionality.
2007 Xuan Baldauf - Contrib of initialiseForHollowPreConstructed()
2007 Xuan Baldauf - Contrib of internalXXX() methods for fields
2007 Xuan Baldauf - remove the fields "jdoLoadedFields" and "jdoModifiedFields".  
2007 Xuan Baldauf - remove the fields "retrievingDetachedState" and "resettingDetachedState".
2007 Xuan Baldauf - remove the field "updatingEmbeddedFieldsWithOwner"
2008 Andy Jefferson - removed all deps on org.datanucleus.store.mapped
    ...
 **********************************************************************/
package org.datanucleus.state;

import java.io.PrintWriter;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

// TODO Use APIAdapter to decide the exception to throw
import javax.jdo.JDOFatalUserException;
import javax.jdo.JDOUserException;

import javax.jdo.spi.Detachable;
import javax.jdo.spi.JDOImplHelper;
import javax.jdo.spi.PersistenceCapable;

import org.datanucleus.ClassLoaderResolver;
import org.datanucleus.FetchPlan;
import org.datanucleus.FetchPlanForClass;
import org.datanucleus.ObjectManager;
import org.datanucleus.api.ApiAdapter;
import org.datanucleus.cache.CachedPC;
import org.datanucleus.cache.Level2Cache;
import org.datanucleus.exceptions.ClassNotResolvedException;
import org.datanucleus.exceptions.NucleusException;
import org.datanucleus.exceptions.NucleusObjectNotFoundException;
import org.datanucleus.exceptions.NucleusUserException;
import org.datanucleus.identity.OID;
import org.datanucleus.identity.OIDFactory;
import org.datanucleus.metadata.AbstractClassMetaData;
import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.metadata.IdentityStrategy;
import org.datanucleus.metadata.IdentityType;
import org.datanucleus.metadata.Relation;
import org.datanucleus.store.FieldValues;
import org.datanucleus.store.ObjectProvider;
import org.datanucleus.store.exceptions.NotYetFlushedException;
import org.datanucleus.store.fieldmanager.AttachFieldManager;
import org.datanucleus.store.fieldmanager.CachePopulateFieldManager;
import org.datanucleus.store.fieldmanager.CacheRetrieveFieldManager;
import org.datanucleus.store.fieldmanager.DeleteFieldManager;
import org.datanucleus.store.fieldmanager.DetachFieldManager;
import org.datanucleus.store.fieldmanager.FieldManager;
import org.datanucleus.store.fieldmanager.LoadFieldManager;
import org.datanucleus.store.fieldmanager.MakeTransientFieldManager;
import org.datanucleus.store.fieldmanager.NullifyRelationFieldManager;
import org.datanucleus.store.fieldmanager.PersistFieldManager;
import org.datanucleus.store.fieldmanager.ReachabilityFieldManager;
import org.datanucleus.store.fieldmanager.SingleValueFieldManager;
import org.datanucleus.store.fieldmanager.AbstractFetchFieldManager.EndOfFetchPlanGraphException;
import org.datanucleus.store.objectvaluegenerator.ObjectValueGenerator;
import org.datanucleus.store.types.sco.SCO;
import org.datanucleus.store.types.sco.SCOCollection;
import org.datanucleus.store.types.sco.SCOContainer;
import org.datanucleus.store.types.sco.SCOMap;
import org.datanucleus.store.types.sco.SCOUtils;
import org.datanucleus.store.types.sco.UnsetOwners;
import org.datanucleus.util.NucleusLogger;
import org.datanucleus.util.StringUtils;
import org.datanucleus.util.TypeConversionHelper;

/**
 * Implementation of the StateManager.
 * Implemented here as one StateManager per Object so adds on functionality particular 
 * to each object. All PersistenceCapable objects will have a StateManager when they 
 * have had communication with the PersistenceManager. They will typically always have
 * an identity also. The exception to that is for embedded/serialised objects.
 * 
 * <H3>Embedded/Serialised Objects</H3>
 * An object that is being embedded/serialised in an owning object will NOT have an identity
 * unless the object is subject to a makePersistent() call also. When an object
 * is embedded/serialised and a field is changed, the field will NOT be marked as dirty (unless
 * it is also an object in its own right with an identity). When a field is changed
 * any owning objects are updated so that they can update their tables accordingly.
 *
 * <H3>Performance and Memory</H3>
 * StateManagers are very performance-critical, because for each PersistentCapable object made persistent,
 * there will be one StateManager instance, adding up to the total memory footprint of that object.
 * In heap profiling analysis, JDOStateManagerImpls showed to consume bytes 169 per StateManager by itself
 * and about 500 bytes per StateManager when taking PC-individual child-object (like the OID) referred
 * by the StateManager into account. With small Java objects this can mean a substantial memory overhead and
 * for applications using such small objects can be critical. For this reason the StateManager should always
 * be minimal in memory consumption.
 */
public class JDOStateManagerImpl extends AbstractStateManager
{
    private static final JDOImplHelper HELPER;

    /** state for transitions of activities. */
    private ActivityState activity = ActivityState.NONE;

    /** Referenced PC object whilst attaching/detaching, for the other object in the process (if any). */
    private PersistenceCapable referencedPC = null;

    /** List of StateManagers that we must notify when we have completed inserting our record. */
    private List<org.datanucleus.state.StateManager> insertionNotifyList = null;

    /** Fields of this object that we must update when notified of the insertion of the related objects. */
    private Map<org.datanucleus.state.StateManager, FieldContainer> fieldsToBeUpdatedAfterObjectInsertion = null;

    /** List of owners when embedded. */
    private List<EmbeddedOwnerRelation> embeddedOwners = null;

    /**
     * Map of associated values for the object being managed. This can contain anything really and is down
     * to the StoreManager to define. For example RDBMS datastores typically put external FK info in here
     * keyed by the mapping of the field to which it pertains.
     */
    private HashMap associatedValuesMap = null;

    static
    {
        HELPER = (JDOImplHelper) AccessController.doPrivileged(new PrivilegedAction()
        {
            public Object run()
            {
                try
                {
                    return JDOImplHelper.getInstance();
                }
                catch (SecurityException e)
                {
                    throw new JDOFatalUserException(LOCALISER.msg("026000"), e);
                }
            }
        });
    }

    /**
     * Basic constructor. Delegates to the superclass.
     * @param om The ObjectManager
     * @param cmd the metadata for the class.
     */
    public JDOStateManagerImpl(ObjectManager om, AbstractClassMetaData cmd)
    {
        super(om, cmd);
    }

    /**
     * Initialises a state manager to manage a hollow instance having the given object ID and the given
     * (optional) field values. This constructor is used for creating new instances of existing persistent
     * objects, and consequently shouldnt be used when the StoreManager controls the creation of such objects
     * (such as in an ODBMS).
     * @param id the JDO identity of the object.
     * @param fv the initial field values of the object (optional)
     * @param pcClass Class of the object that this will manage the state for
     */
    public void initialiseForHollow(Object id, FieldValues fv, Class pcClass)
    {
        myID = id;
        myLC = myOM.getNucleusContext().getApiAdapter().getLifeCycleState(LifeCycleState.HOLLOW);
        jdoDfgFlags = PersistenceCapable.LOAD_REQUIRED;
        if (id instanceof OID || id == null)
        {
            // Create new PC
            myPC = HELPER.newInstance(pcClass, this);
        }
        else
        {
            // Create new PC, and copy the key class to fields
            myPC = HELPER.newInstance(pcClass, this, myID);
            markPKFieldsAsLoaded();
        }

        // Put in L1 cache just in case referred to by other objects in the FieldValues
        // e.g when we retrieve objects with circular references in the same result set from a query
        myOM.putObjectIntoCache(this);

        if (fv != null)
        {
            loadFieldValues(fv);
        }
    }

    /**
     * Initialises a state manager to manage a HOLLOW / P_CLEAN instance having the given FieldValues.
     * This constructor is used for creating new instances of existing persistent objects using application 
     * identity, and consequently shouldnt be used when the StoreManager controls the creation of such objects
     * (such as in an ODBMS).
     * @param fv the initial field values of the object.
     * @param pcClass Class of the object that this will manage the state for
     */
    public void initialiseForHollowAppId(FieldValues fv, Class pcClass)
    {
        if (cmd.getIdentityType() != IdentityType.APPLICATION)
        {
            throw new NucleusUserException("This constructor is only for objects using application identity.").setFatal();
        }

        myLC = myOM.getNucleusContext().getApiAdapter().getLifeCycleState(LifeCycleState.HOLLOW);
        jdoDfgFlags = PersistenceCapable.LOAD_REQUIRED;
        myPC = HELPER.newInstance(pcClass, this); // Create new PC
        if (myPC == null)
        {
            if (!HELPER.getRegisteredClasses().contains(pcClass))
            {
                // probably never will get here, as JDOImplHelper.newInstance() internally already throws
                // JDOFatalUserException when class is not registered 
                throw new NucleusUserException(LOCALISER.msg("026018", pcClass.getName())).setFatal();
            }
            else
            {
                // Provide advisory information since we can't create an instance of this class, so maybe they
                // have an error in their data ?
                throw new NucleusUserException(LOCALISER.msg("026019", pcClass.getName())).setFatal();
            }
        }

        loadFieldValues(fv); // as a minimum the PK fields are loaded here

        // Create the ID now that we have the PK fields loaded
        myID = myPC.jdoNewObjectIdInstance();
        if (!cmd.usesSingleFieldIdentityClass())
        {
            myPC.jdoCopyKeyFieldsToObjectId(myID);
        }
    }

    /**
     * Initialises a state manager to manage the given hollow instance having the given object ID.
     * Unlike the {@link #initialiseForHollow} method, this method does not create a new instance and instead 
     * takes a pre-constructed instance (such as from an ODBMS).
     * @param id the identity of the object.
     * @param pc the object to be managed.
     */
    public void initialiseForHollowPreConstructed(Object id, Object pc)
    {
        myID = id;
        myLC = myOM.getNucleusContext().getApiAdapter().getLifeCycleState(LifeCycleState.HOLLOW);
        jdoDfgFlags = PersistenceCapable.LOAD_REQUIRED;
        myPC = (PersistenceCapable)pc;

        replaceStateManager(myPC, this); // Assign this StateManager to the PC
        myPC.jdoReplaceFlags();

        // TODO Add to the cache
    }

    /**
     * Initialises a state manager to manage the passed persistent instance having the given object ID.
     * Used where we have retrieved a PC object from a datastore directly (not field-by-field), for example on
     * an object datastore. This initialiser will not add StateManagers to all related PCs. This must be done by
     * any calling process. This simply adds the StateManager to the specified object and records the id, setting
     * all fields of the object as loaded.
     * @param id the identity of the object.
     * @param pc The object to be managed
     */
    public void initialiseForPersistentClean(Object id, Object pc)
    {
        myID = id;
        myLC = myOM.getNucleusContext().getApiAdapter().getLifeCycleState(LifeCycleState.P_CLEAN);
        jdoDfgFlags = PersistenceCapable.LOAD_REQUIRED;
        myPC = (PersistenceCapable)pc;

        replaceStateManager(myPC, this); // Assign this StateManager to the PC
        myPC.jdoReplaceFlags();

        // Mark all fields as loaded
        for (int i=0; i<loadedFields.length; ++i)
        {
            loadedFields[i] = true;
        }

        // Add the object to the cache
        myOM.putObjectIntoCache(this);
    }

    /**
     * Initialises a state manager to manage a PersistenceCapable instance that will be EMBEDDED/SERIALISED 
     * into another PersistenceCapable object. The instance will not be assigned an identity in the process 
     * since it is a SCO.
     * @param pc The PersistenceCapable to manage (see copyPc also)
     * @param copyPc Whether the SM should manage a copy of the passed PC or that one
     */
    public void initialiseForEmbedded(Object pc, boolean copyPc)
    {
        pcObjectType = ObjectProvider.EMBEDDED_PC; // Default to an embedded PC object
        myID = null; // It is embedded at this point so dont need an ID since we're not persisting it
        myLC = myOM.getNucleusContext().getApiAdapter().getLifeCycleState(LifeCycleState.P_NEW);
        jdoDfgFlags = PersistenceCapable.LOAD_REQUIRED;

        myPC = (PersistenceCapable)pc;
        replaceStateManager(myPC, this); // Set SM for embedded PC to be this
        if (copyPc)
        {
            // Create a new PC with the same field values
            PersistenceCapable pcCopy = myPC.jdoNewInstance(this);
            pcCopy.jdoCopyFields(myPC, getAllFieldNumbers());

            // Swap the managed PC to be the copy and not the input
            replaceStateManager(pcCopy, this);
            myPC = pcCopy;
            disconnectClone((PersistenceCapable)pc);
        }

        // Mark all fields as loaded since we are using the passed PersistenceCapable
        for (int i=0;i<loadedFields.length;i++)
        {
            loadedFields[i] = true;
        }
    }

    /**
     * Initialises a state manager to manage a transient instance that is becoming newly persistent.
     * A new object ID for the instance is obtained from the store manager and the object is inserted
     * in the data store.
     * <p>This constructor is used for assigning state managers to existing
     * instances that are transitioning to a persistent state.
     * @param pc the instance being make persistent.
     * @param preInsertChanges Any changes to make before inserting
     */
    public void initialiseForPersistentNew(Object pc, FieldValues preInsertChanges)
    {
        myPC = (PersistenceCapable)pc;
        myLC = myOM.getNucleusContext().getApiAdapter().getLifeCycleState(LifeCycleState.P_NEW);
        jdoDfgFlags = PersistenceCapable.READ_OK;
        for (int i=0; i<loadedFields.length; ++i)
        {
            loadedFields[i] = true;
        }

        replaceStateManager(myPC, this); // Assign this StateManager to the PC
        myPC.jdoReplaceFlags();

        saveFields();

        // Populate all fields that have "value-strategy" and are not datastore populated
        populateStrategyFields();

        if (preInsertChanges != null)
        {
            // Apply any pre-insert field updates
            preInsertChanges.fetchFields(this);
        }

        if (cmd.getIdentityType() == IdentityType.APPLICATION)
        {
            //load key fields from Application Id instance to PC instance

            //if a primary key field is of type PersistenceCapable, it must first be persistent
            for (int fieldNumber = 0; fieldNumber < getAllFieldNumbers().length; fieldNumber++)
            {
                AbstractMemberMetaData fmd = cmd.getMetaDataForManagedMemberAtAbsolutePosition(fieldNumber);
                if (fmd.isPrimaryKey())
                {
                    if (myOM.getMetaDataManager().getMetaDataForClass(fmd.getType(), getObjectManager().getClassLoaderResolver()) != null)
                    {
                        try
                        {
                            if (myOM.getMultithreaded())
                            {
                                myOM.getLock().lock();
                                lock.lock();
                            }

                            FieldManager prevFM = currFM;
                            try
                            {
                                currFM = new SingleValueFieldManager();
                                myPC.jdoProvideField(fieldNumber);
                                PersistenceCapable pkFieldPC = 
                                    (PersistenceCapable) ((SingleValueFieldManager) currFM).fetchObjectField(fieldNumber);
                                if (pkFieldPC == null)
                                {
                                    throw new NucleusUserException(
                                        LOCALISER.msg("026016", fmd.getFullFieldName()));
                                }
                                if (!myOM.getApiAdapter().isPersistent(pkFieldPC))
                                {
                                    // Make sure the PC field is persistent - can cause the insert of our object 
                                    // being managed by this SM via flush() when bidir relation
                                    Object persistedFieldPC = myOM.persistObjectInternal(pkFieldPC, null, null, -1, 
                                        ObjectProvider.PC);
                                    replaceField(myPC, fieldNumber, persistedFieldPC, false);
                                }
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
                }
            }
        }

        /* Set the identity
         * This must come after the above block, because in identifying relationships
         * the PK FK associations must be persisted before, otherwise we
         * don't have an id assigned to the PK FK associations
         */        
        setIdentity(false);

        if (this.getObjectManager().getTransaction().isActive())
        {
            myOM.enlistInTransaction(this);
        }

        // Now in PERSISTENT_NEW so call any callbacks/listeners
        getCallbackHandler().postCreate(myPC);

        if (myOM.getManageRelations())
        {
            // Managed Relations : register non-null bidir fields for later processing
            ClassLoaderResolver clr = myOM.getClassLoaderResolver();
            int[] relationPositions = cmd.getRelationMemberPositions(clr, getMetaDataManager());
            if (relationPositions != null)
            {
                for (int i=0;i<relationPositions.length;i++)
                {
                    AbstractMemberMetaData mmd = 
                        cmd.getMetaDataForManagedMemberAtAbsolutePosition(relationPositions[i]);
                    if (Relation.isBidirectional(mmd.getRelationType(clr)))
                    {
                        Object value = provideField(relationPositions[i]);
                        if (value != null)
                        {
                            // Store the field with value of null so it gets checked
                            myOM.getRelationshipManager(this).relationChange(relationPositions[i], null, null);
                        }
                    }
                }
            }
        }
    }

    /**
     * Initialises a state manager to manage a Transactional Transient instance.
     * A new object ID for the instance is obtained from the store manager and the object is inserted in the data store.
     * <p>
     * This constructor is used for assigning state managers to Transient
     * instances that are transitioning to a transient clean state.
     * @param pc the instance being make persistent.
     */
    public void initialiseForTransactionalTransient(Object pc)
    {
        myPC = (PersistenceCapable)pc;
        myLC = null;
        jdoDfgFlags = PersistenceCapable.READ_OK;
        for (int i=0; i<loadedFields.length; ++i)
        {
            loadedFields[i] = true;
        }
        myPC.jdoReplaceFlags();

        // Populate all fields that have "value-strategy" and are not datastore populated
        populateStrategyFields();

        // Set the identity
        setIdentity(false);

        // for non transactional read, tx might be not active
        // TODO add verification if is non transactional read = true
        if (myOM.getTransaction().isActive())
        {
            myOM.enlistInTransaction(this);
        }
    }

    /**
     * Initialises the StateManager to manage a PersistenceCapable object in detached state.
     * @param pc the detach object.
     * @param id the identity of the object.
     * @param version the detached version
     */
    public void initialiseForDetached(Object pc, Object id, Object version)
    {
        this.myID = id;
        this.myPC = (PersistenceCapable)pc;
        setVersion(version);

        // This lifecycle state is not always correct. It is certainly "detached"
        // but we dont know if it is CLEAN or DIRTY. We need this setting here since all objects
        // have a lifecycle state and other methods e.g isPersistent() depend on it.
        this.myLC = myOM.getNucleusContext().getApiAdapter().getLifeCycleState(LifeCycleState.DETACHED_CLEAN);

        this.myPC.jdoReplaceFlags();
        replaceStateManager(myPC, this);
    }

    /**
     * Initialises the StateManager to manage a PersistenceCapable object that is not persistent but is
     * about to be deleted.
     * @param pc the object to delete
     */
    public void initialiseForPNewToBeDeleted(Object pc)
    {
        this.myID = null;
        this.myPC = (PersistenceCapable)pc;
        this.myLC = myOM.getNucleusContext().getApiAdapter().getLifeCycleState(LifeCycleState.P_NEW);
        for (int i=0; i<loadedFields.length; ++i) // Mark all fields as loaded
        {
            loadedFields[i] = true;
        }
        replaceStateManager(myPC, this);
    }

    /**
     * Initialise to create a StateManager for a PersistenceCapable object, assigning the specified id to the
     * object. This is used when getting objects out of the L2 Cache, where they have no StateManager 
     * assigned, and returning them as associated with a particular PM.
     * @param cachedPC The cached PC object
     * @param id Id to assign to the PersistenceCapable object
     * @param pcClass Class of the object that this will manage the state for
     */
    public void initialiseForCachedPC(CachedPC cachedPC, Object id, Class pcClass)
    {
        // Create a new copy of the input object type, performing the majority of the initialisation
        initialiseForHollow(id, null, pcClass);

        myLC = myOM.getNucleusContext().getApiAdapter().getLifeCycleState(LifeCycleState.P_CLEAN);
        jdoDfgFlags = PersistenceCapable.READ_OK;

        // Synchronise the L2 cached object while we grab its fields
        Object cachePC = cachedPC.getPersistableObject();
        synchronized(cachePC)
        {
            // Load all fields that are cached that are in the fetch plan
            // TODO We could just load all fields that are cached but could lead to graph depth problems
            int[] fieldsToLoad = getFlagsSetTo(cachedPC.getLoadedFields(), myFP.getMemberNumbers(), true);
            if (fieldsToLoad != null)
            {
                // Connect a StateManager - the lifecycle state is unimportant
                JDOStateManagerImpl cacheSM = new JDOStateManagerImpl(myOM, cmd);
                cacheSM.initialiseForDetached(cachePC, getExternalObjectId(myPC), getVersion(myPC));

                // Put in L1 cache for easy referencing (case of bi-dir relations needed by the next step)
                myOM.putObjectIntoCache(this);

                // Copy the field values in
                this.replaceFields(fieldsToLoad, new CacheRetrieveFieldManager(this, cacheSM, cachedPC));

                // Disconnect the cached object
                disconnectClone((PersistenceCapable)cachePC);
            }

            // Set the loaded fields to match what was just loaded
            if (fieldsToLoad != null)
            {
                for (int i=0;i<fieldsToLoad.length;i++)
                {
                    loadedFields[fieldsToLoad[i]] = true;
                }
            }

            if (cachedPC.getVersion() != null)
            {
                // Make sure we start from the same version as was cached
                setVersion(cachedPC.getVersion());
            }

            // Make sure any SCO fields are wrapped
            replaceAllLoadedSCOFieldsWithWrappers();
        }

        if (myOM.getTransaction().isActive())
        {
            myOM.enlistInTransaction(this);
        }

        if (isFetchPlanLoaded())
        {
            // Should we call postLoad when getting the object out of the L2 cache ? Seems incorrect IMHO
            postLoad();
        }
    }

    /**
     * Method to return an L2 cacheable form of the managed object.
     * @return The object suitable for L2 caching
     */
    public CachedPC cache()
    {
        int[] loadedFieldNumbers = getLoadedFieldNumbers();
        if (loadedFieldNumbers == null || loadedFieldNumbers.length == 0)
        {
            // No point caching an object with no loaded fields!
            return null;
        }

        // Create the cacheable object and add to cache state
        Object cachePC = myPC.jdoNewInstance(this, myPC.jdoGetObjectId());
        CachedPC cachedPC = new CachedPC(cachePC, getLoadedFields(), getTransactionalVersion(myPC));

        // Connect a StateManager - the lifecycle state is unimportant
        JDOStateManagerImpl cacheSM = new JDOStateManagerImpl(myOM, cmd);
        cacheSM.initialiseForDetached(cachePC, getExternalObjectId(myPC), getVersion(myPC));

        // Copy across all of the loaded fields that are allowed to be cached
        cacheSM.replaceFields(getLoadedFieldNumbers(), new CachePopulateFieldManager(this, cachedPC));

        // Disconnect the StateManager
        replaceStateManager(((PersistenceCapable)cachePC), null);

        return cachedPC;
    }

    /**
     * Look to the database to determine which class this object is. This parameter is a hint. Set false, if it's
     * already determined the correct pcClass for this pc "object" in a certain
     * level in the hierarchy. Set to true and it will look to the database.
     * @param fv the initial field values of the object.
     */
    public void checkInheritance(FieldValues fv)
    {
        // Inheritance case, check the level of the instance
        ClassLoaderResolver clr = myOM.getClassLoaderResolver();
        String className = getStoreManager().getClassNameForObjectID(myID, clr, myOM);
        if (className == null)
        {
            // className is null when id class exists, and object has been validated and doesn't exist.
            throw new NucleusObjectNotFoundException(LOCALISER.msg("026013", myOM.getIdentityAsString(myID)), myID);
        }
        else if (!cmd.getFullClassName().equals(className))
        {
            Class pcClass;
            try
            {
                //load the class and make sure the class is initialized
                pcClass = clr.classForName(className, myID.getClass().getClassLoader(), true);
                cmd = myOM.getMetaDataManager().getMetaDataForClass(pcClass, clr);
            }
            catch (ClassNotResolvedException e)
            {
                NucleusLogger.PERSISTENCE.warn(LOCALISER.msg("026014", myOM.getIdentityAsString(myID)));
                throw new NucleusUserException(LOCALISER.msg("026014", myOM.getIdentityAsString(myID)), e);
            }
            if (cmd == null)
            {
                throw new NucleusUserException(LOCALISER.msg("026012", pcClass)).setFatal();
            }
            if (cmd.getIdentityType() != IdentityType.APPLICATION)
            {
                throw new NucleusUserException("This method should only be used for objects using application identity.").setFatal();
            }
            myFP = myOM.getFetchPlan().manageFetchPlanForClass(cmd);

            initialiseFieldInformation();

            // Create new PC at right inheritance level
            myPC = HELPER.newInstance(pcClass, this);
            if (myPC == null)
            {
                throw new NucleusUserException(LOCALISER.msg("026018", cmd.getFullClassName())).setFatal();
            }

            // Note that this will mean the fields are loaded twice (loaded earlier in this method)
            // and also that postLoad will be called twice
            loadFieldValues(fv);

            // Create the id for the new PC
            myID = myPC.jdoNewObjectIdInstance();
            if (!cmd.usesSingleFieldIdentityClass())
            {
                myPC.jdoCopyKeyFieldsToObjectId(myID);
            }
        }
    }

    /**
     * Convenience method to populate all fields in the PC object that have "value-strategy" specified
     * and that aren't datastore attributed. This applies not just to PK fields (where it is most
     * useful to use value-strategy) but also to any other field. Fields are populated only if they are null
     * This is called once on a PC object, when makePersistent is called.
     */
    private void populateStrategyFields()
    {
        int totalFieldCount = cmd.getNoOfInheritedManagedMembers() + cmd.getNoOfManagedMembers();

        for (int fieldNumber=0; fieldNumber<totalFieldCount; fieldNumber++)
        {
            AbstractMemberMetaData mmd = cmd.getMetaDataForManagedMemberAtAbsolutePosition(fieldNumber);
            IdentityStrategy strategy = mmd.getValueStrategy();

            // Check for the strategy, and if it is a datastore attributed strategy
            if (strategy != null && !getStoreManager().isStrategyDatastoreAttributed(strategy, false))
            {
                // Assign the strategy value where required.
                // Default JDO2 behaviour is to always provide a strategy value when it is marked as using a strategy
                boolean applyStrategy = true;
                if (!mmd.getType().isPrimitive() && strategy != null &&
                    mmd.hasExtension("strategy-when-notnull") &&
                    mmd.getValueForExtension("strategy-when-notnull").equalsIgnoreCase("false") &&
                    this.provideField(fieldNumber) != null)
                {
                    // extension to only provide a value-strategy value where the field is null at persistence.
                    applyStrategy = false;
                }

                if (applyStrategy)
                {
                    // Apply a strategy value for this field
                    Object obj = getStoreManager().getStrategyValue(myOM, cmd, fieldNumber);
                    this.replaceField(fieldNumber, obj);
                }
            }
            else if (mmd.hasExtension("object-value-generator"))
            {
                // Field has object value-generator so generate value based on this object
                String valGenName = mmd.getValueForExtension("object-value-generator");
                ObjectValueGenerator valGen = getObjectValueGenerator(myOM, valGenName);
                Object value = valGen.generate(myOM, myPC, mmd.getExtensions());
                this.replaceField(myPC, fieldNumber, value, true);
            }
        }
    }

    /** Cache of object-value-generators, keyed by their symbolic name. */
    static HashMap<String, ObjectValueGenerator> objectValGenerators = new HashMap(1);

    /**
     * Method to find an object value generator based on its name. Caches the generators once generated.
     * @param om ObjectManager
     * @param genName The generator name
     * @return The value generator (if any)
     * @throws NucleusException if no generator of that name is found
     */
    protected static ObjectValueGenerator getObjectValueGenerator(ObjectManager om, String genName)
    {
        if (!objectValGenerators.isEmpty())
        {
            ObjectValueGenerator valGen = objectValGenerators.get(genName);
            if (valGen != null)
            {
                return valGen;
            }
        }

        try
        {
            ObjectValueGenerator valGen = (ObjectValueGenerator)
                om.getNucleusContext().getPluginManager().createExecutableExtension(
                    "org.datanucleus.store_objectvaluegenerator", new String[] {"name"},
                    new String[] {genName}, "class-name", null, null);
            objectValGenerators.put(genName, valGen);
            return valGen;
        }
        catch (Exception e)
        {
            NucleusLogger.VALUEGENERATION.info("Exception thrown generating value using objectvaluegenerator " + genName, e);
            throw new NucleusException("Exception thrown generating value for object", e);
        }
    }

    /**
     * Convenience method to load the passed field values.
     * Loads the fields using any required fetch plan and calls jdoPostLoad() as appropriate.
     * @param fv Field Values to load (including any fetch plan to use when loading)
     */
    public void loadFieldValues(FieldValues fv)
    {
        // Fetch the required fields using any defined fetch plan
        FetchPlanForClass origFetchPlan = myFP;
        FetchPlan loadFetchPlan = fv.getFetchPlanForLoading();
        if (loadFetchPlan != null)
        {
            myFP = loadFetchPlan.manageFetchPlanForClass(cmd);
        }

        boolean callPostLoad = myFP.isToCallPostLoadFetchPlan(this.loadedFields);
        if (loadedFields.length == 0)
        {
            // Class has no fields so since we are loading from scratch just call postLoad
            callPostLoad = true;
        }

        fv.fetchFields(this);

        if (callPostLoad && isFetchPlanLoaded() && myOM.getNucleusContext().getApiName().equalsIgnoreCase("JDO"))
        {
            postLoad();
        }

        // Reinstate the original (PM) fetch plan
        myFP = origFetchPlan;
    }

    /**
     * Utility to set the identity for the PersistenceCapable object.
     * Creates the identity instance if the required PK field(s) are all already set (by the user, or by
     * a value-strategy). If the identity is set in the datastore (sequence, autoassign, etc) then this
     * will not set the identity.
     * @param afterPreStore Whether preStore has (just) been invoked
     */
    private void setIdentity(boolean afterPreStore)
    {
        if (cmd.isEmbeddedOnly())
        {
            // Embedded objects don't have an "identity"
            return;
        }

        if (cmd.getIdentityType() == IdentityType.DATASTORE)
        {
            if (cmd.getIdentityMetaData() == null ||
                !getStoreManager().isStrategyDatastoreAttributed(cmd.getIdentityMetaData().getValueStrategy(), true))
            {
                // Assumed to be set
                myID = myOM.newObjectId(cmd.getFullClassName(), myPC);
            }
        }
        else if (cmd.getIdentityType() == IdentityType.APPLICATION)
        {
            boolean idSetInDatastore = false;
            int totalFieldCount = cmd.getNoOfInheritedManagedMembers() + cmd.getNoOfManagedMembers();
            for (int fieldNumber=0; fieldNumber<totalFieldCount; fieldNumber++)
            {
                AbstractMemberMetaData fmd = cmd.getMetaDataForManagedMemberAtAbsolutePosition(fieldNumber);
                if (fmd.isPrimaryKey())
                {
                    if (getStoreManager().isStrategyDatastoreAttributed(fmd.getValueStrategy(), false))
                    {
                        idSetInDatastore = true;
                        break;
                    }
                    else if (cmd.usesSingleFieldIdentityClass())
                    {
                        if (this.provideField(fieldNumber) == null)
                        {
                            // PK field has not had its value set (user/value-strategy)
                            // and must be set for single-field identity
                            if (afterPreStore)
                            {
                                // Not set even after preStore, so user error
                                throw new NucleusUserException(LOCALISER.msg("026017", cmd.getFullClassName(), 
                                    fmd.getName())).setFatal();
                            }
                            else
                            {
                                // Log that the value is not yet set for this field, maybe set later in preStore?
                                NucleusLogger.PERSISTENCE.debug(LOCALISER.msg("026017", cmd.getFullClassName(),
                                    fmd.getName()));
                                return;
                            }
                        }
                    }
                }
            }

            if (!idSetInDatastore)
            {
                // Not generating the identity in the datastore so set it now
                myID = myOM.newObjectId(cmd.getFullClassName(), myPC);
            }
        }

        if (myInternalID != myID && myID != null && myOM.getApiAdapter().getIdForObject(myPC) != null)
        {
            // Update the id with the PM if it is changing
            myOM.replaceObjectId(myPC, myInternalID, myID);

            this.myInternalID = myID;
        }
    }

    /**
     * Convenience method to update our object with the field values from the passed object.
     * Objects need to be of the same type, and the other object should not have a StateManager.
     * @param obj The object that we should copy fields from
     * @param fieldNumbers Numbers of fields to copy
     */
    public void copyFieldsFromObject(Object obj, int[] fieldNumbers)
    {
        if (obj == null)
        {
            return;
        }
        if (!obj.getClass().getName().equals(myPC.getClass().getName()))
        {
            return;
        }
        if (!(obj instanceof PersistenceCapable))
        {
            throw new NucleusUserException("Must be PersistenceCapable");
        }
        PersistenceCapable pc = (PersistenceCapable)obj;

        // Assign the new object to this StateManager temporarily so that we can copy its fields
        replaceStateManager(pc, this);
        myPC.jdoCopyFields(pc, fieldNumbers);

        // Remove the StateManager from the other object
        replaceStateManager(pc, null);

        // Set the loaded flags now that we have copied
        for (int i=0;i<fieldNumbers.length;i++)
        {
            loadedFields[fieldNumbers[i]] = true;
        }
    }

    /**
     * Marks the given field dirty.
     * @param field The no of field to mark as dirty. 
     */
    public void makeDirty(int field)
    {
        if (activity != ActivityState.DELETING)
        {
            // Mark dirty unless in the process of being deleted
            boolean wasDirty = preWriteField(field);
            postWriteField(wasDirty);
            
            if (embeddedOwners != null)
            {
                // Notify any owners that embed this object that it has just changed
                for (EmbeddedOwnerRelation owner : embeddedOwners)
                {
                    JDOStateManagerImpl ownerSM = (JDOStateManagerImpl) owner.sm;

                    if (ownerSM == null || ownerSM.cmd == null)
                    {
                        // for some reason these are null... raised when running JPA TCK
                        continue;
                    }

                    if ((ownerSM.flags&FLAG_UPDATING_EMBEDDING_FIELDS_WITH_OWNER)==0)
                    {
                        ownerSM.makeDirty(owner.fieldNumber);
                    }
                }
            }
        }
    }

    /**
     * Mark the associated PersistenceCapable field dirty.
     *
     * @param pc the calling PersistenceCapable instance
     * @param fieldName the name of the field
     */
    public void makeDirty(PersistenceCapable pc, String fieldName)
    {
        if (!disconnectClone(pc))
        {
            int fieldNumber = cmd.getAbsolutePositionOfMember(fieldName);
            if (fieldNumber == -1)
            {
                throw new JDOUserException(LOCALISER.msg("026002", fieldName, cmd.getFullClassName()));
            }
            
            makeDirty(fieldNumber);
        }
    }

    // -------------------------- Accessor Methods -----------------------------

    /**
     * Return the object representing the JDO identity of the calling instance.
     * According to the JDO specification, if the JDO identity is being changed in the current transaction, 
     * this method returns the JDO identify as of the beginning of the transaction.
     * @param pc the calling PersistenceCapable instance
     * @return the object representing the JDO identity of the calling instance
     */
    public Object getObjectId(PersistenceCapable pc)
    {
        if (disconnectClone(pc))
        {
            return null;
        }
        else
        {
            return getExternalObjectId(pc);
        }
    }

    /**
     * Return the identity object for the managed object as of the start of any transaction.
     * @return Identity of the managed object
     */
    public Object getObjectId()
    {
        return getObjectId(myPC);
    }

    /**
     * Return the object representing the JDO identity of the calling instance.  
     * If the JDO identity is being changed in the current transaction, this method returns the 
     * current identity as changed in the transaction. In this implementation we don't allow
     * change of identity so this is always the same as the result of getObjectId(PersistenceCapable).
     *
     * @param pc the calling PersistenceCapable instance
     * @return the object representing the JDO identity of the calling instance
     */
    public Object getTransactionalObjectId(PersistenceCapable pc)
    {
        return getObjectId(pc);
    }

    /**
     * If the id is obtained after inserting the object into the database, set
     * new a new id for persistent classes (for example, increment).
     * @param id the id received from the datastore
     */
    public void setPostStoreNewObjectId(Object id)
    {
        if (cmd.getIdentityType() == IdentityType.DATASTORE)
        {
            if (id instanceof OID)
            {
                // Provided an OID direct
                this.myID = id;
            }
            else
            {
                // OID "key" value provided
                myID = OIDFactory.getInstance(myOM.getNucleusContext(), cmd.getFullClassName(), id);
            }
        }
        else if (cmd.getIdentityType() == IdentityType.APPLICATION)
        {
            try
            {
                myID = null;

                int fieldCount = getHighestFieldNumber();
                for (int fieldNumber = 0; fieldNumber < fieldCount; fieldNumber++)
                {
                    AbstractMemberMetaData fmd=cmd.getMetaDataForManagedMemberAtAbsolutePosition(fieldNumber);
                    if (fmd.isPrimaryKey() && getStoreManager().isStrategyDatastoreAttributed(fmd.getValueStrategy(), false))
                    {
                        //replace the value of the id, but before convert the value to the field type if needed
                        replaceField(myPC, fieldNumber, TypeConversionHelper.convertTo(id, fmd.getType()), false);
                    }
                }
            }
            catch (Exception e)
            {
                NucleusLogger.PERSISTENCE.error(e);
            }
            finally
            {
                myID = myOM.getApiAdapter().getNewApplicationIdentityObjectId(getObject(), cmd);
            }
        }

        if (myInternalID != myID && myID != null)
        {
            // Update the id with the PM if it is changing
            myOM.replaceObjectId(myPC, myInternalID, myID);

            this.myInternalID = myID;
        }
    }

    /**
     * Return an object id that the user can use.
     * @param obj the PersistenceCapable object
     * @return the object id
     */
    public Object getExternalObjectId(Object obj)
    {
        if (cmd.getIdentityType() == IdentityType.DATASTORE)
        {
            if (!isFlushing())
            {
                // Flush any datastore changes so that myID is set by the time we return
                if (!isFlushedNew() &&
                    activity != ActivityState.INSERTING && activity != ActivityState.INSERTING_CALLBACKS &&
                    myLC.stateType() == LifeCycleState.P_NEW)
                {
                    if (getStoreManager().isStrategyDatastoreAttributed(cmd.getIdentityMetaData().getValueStrategy(), true))
                    {
                        flush();
                    }
                }
            }
        }
        else if (cmd.getIdentityType() == IdentityType.APPLICATION)
        {
            // Note that we always create a new application identity since it is mutable and we can't allow
            // the user to change it. The only drawback of this is that we *must* have the relevant fields
            // set when this method is called, so that the identity can be generated.
            if (!isFlushing())
            {
                // Flush any datastore changes so that we have all necessary fields populated
                // only if the datastore generates the field numbers
                if (!isFlushedNew() &&
                    activity != ActivityState.INSERTING && activity != ActivityState.INSERTING_CALLBACKS &&
                    myLC.stateType() == LifeCycleState.P_NEW)
                {
                    int[] pkFieldNumbers = cmd.getPKMemberPositions();
                    for (int i = 0; i < pkFieldNumbers.length; i++)
                    {
                        AbstractMemberMetaData fmd = cmd.getMetaDataForManagedMemberAtAbsolutePosition(pkFieldNumbers[i]);
                        if (getStoreManager().isStrategyDatastoreAttributed(fmd.getValueStrategy(), false))
                        {
                            flush();
                            break;
                        }
                    }
                }
            }

            if (cmd.usesSingleFieldIdentityClass())
            {
                //SingleFieldIdentity classes are immutable.
                //Note, the instances of SingleFieldIdentity can be changed by the user using reflection,
                //but this is not allowed by the JDO spec
                return myID;
            }
            return myOM.getApiAdapter().getNewApplicationIdentityObjectId(myPC, cmd);
        }

        return myID;
    }

    /**
     * Return an object identity that can be used by the user for the managed object.
     * @return the object id
     */
    public Object getExternalObjectId()
    {
        return getExternalObjectId(myPC);
    }

    // --------------------------- Load Field Methods --------------------------

    /**
     * Convenience method to retrieve field values from an L2 cached object if they are loaded in that object.
     * If the object is not in the L2 cache then just returns, and similarly if the required fields aren't available.
     * @param fieldNumbers Numbers of fields to load from the L2 cache
     * @return The fields that couldn't be loaded
     */
    private int[] loadFieldsFromLevel2Cache(int[] fieldNumbers)
    {
        // Only continue if there are fields, and not being deleted/flushed etc
        if (fieldNumbers == null || fieldNumbers.length == 0 || myOM.isFlushing() || myLC.isDeleted() || isDeleting() ||
            getObjectManager().getTransaction().isCommitting())
        {
            return fieldNumbers;
        }
        // TODO Drop this check when we're confident that this doesn't affect some use-cases
        if (!myOM.getNucleusContext().getPersistenceConfiguration().getBooleanProperty("datanucleus.cache.level2.loadFields", true))
        {
            return fieldNumbers;
        }

        Level2Cache l2cache = myOM.getNucleusContext().getLevel2Cache();
        if (l2cache != null && cmd.isCacheable())
        {
            CachedPC cachedPC = null;
            synchronized (l2cache)
            {
                cachedPC = l2cache.get(myID);
                if (cachedPC != null)
                {
                    int[] cacheFieldsToLoad = getFlagsSetTo(cachedPC.getLoadedFields(), fieldNumbers, true);
                    if (cacheFieldsToLoad != null && cacheFieldsToLoad.length > 0)
                    {
                        if (NucleusLogger.CACHE.isDebugEnabled())
                        {
                            NucleusLogger.CACHE.debug(LOCALISER.msg("026034", StringUtils.toJVMIDString(myPC), myID,
                                StringUtils.intArrayToString(cacheFieldsToLoad)));
                        }

                        Object cachePC = cachedPC.getPersistableObject();
                        JDOStateManagerImpl cacheSM = new JDOStateManagerImpl(myOM, cmd);
                        // TODO Change to use a specific method - lifecycle state is not strictly correct
                        cacheSM.initialiseForDetached(cachePC, getExternalObjectId(myPC), getVersion(myPC));

                        // Copy across the specified fields from the cached object
                        this.replaceFields(cacheFieldsToLoad, new CacheRetrieveFieldManager(this, cacheSM, cachedPC));

                        // Disconnect the StateManager
                        replaceStateManager(((PersistenceCapable)cachePC), null);
                    }
                }
            }
        }
        return getFlagsSetTo(loadedFields, fieldNumbers, false);
    }

    /**
     * Convenience method to update a Level2 cached version of this object if cacheable
     * and has not been modified during this transaction.
     * If any of the specified fields are not loaded in the current L2 cached object this will
     * update the loaded value for that field(s).
     * @param fieldNumbers Numbers of fields to update in L2 cached object
     */
    private void updateLevel2CacheForFields(int[] fieldNumbers)
    {
        if (fieldNumbers == null || fieldNumbers.length == 0)
        {
            return;
        }

        Level2Cache l2cache = myOM.getNucleusContext().getLevel2Cache();
        if (l2cache != null && cmd.isCacheable() && !myOM.isObjectModifiedInTransaction(myID))
        {
            CachedPC cachedPC = null;
            synchronized (l2cache)
            {
                cachedPC = l2cache.get(myID);
            }
            if (cachedPC != null)
            {
                int[] cacheFieldsToLoad = getFlagsSetTo(cachedPC.getLoadedFields(), fieldNumbers, false);
                if (cacheFieldsToLoad != null && cacheFieldsToLoad.length > 0)
                {
                    if (NucleusLogger.CACHE.isDebugEnabled())
                    {
                        NucleusLogger.CACHE.debug(LOCALISER.msg("026033", StringUtils.toJVMIDString(myPC), myID,
                            StringUtils.intArrayToString(cacheFieldsToLoad)));
                    }

                    // Connect a StateManager to the cachedPC
                    Object cachePC = cachedPC.getPersistableObject();
                    JDOStateManagerImpl cacheSM = new JDOStateManagerImpl(myOM, cmd);
                    // TODO Change to use a specific method - lifecycle state is not strictly correct
                    cacheSM.initialiseForDetached(cachePC, getExternalObjectId(myPC), getVersion(myPC));

                    // Copy across the specified fields into the cached object
                    cacheSM.replaceFields(cacheFieldsToLoad, new CachePopulateFieldManager(this, cachedPC));

                    // Disconnect the StateManager
                    replaceStateManager(((PersistenceCapable)cachePC), null);
                }
            }
        }
    }

    /**
     * Fetchs from the database all SCO fields that are not containers that aren't already loaded.
     */
    private void loadSCONonContainerFields()
    {
        int[] noncontainerFieldNumbers = cmd.getSCONonContainerMemberPositions();
        int[] fieldNumbers = getFlagsSetTo(loadedFields, noncontainerFieldNumbers, false);
        if (fieldNumbers != null && fieldNumbers.length > 0)
        {
            int[] unloadedFieldNumbers = loadFieldsFromLevel2Cache(fieldNumbers);
            if (unloadedFieldNumbers != null)
            {
                loadFieldsFromDatastore(unloadedFieldNumbers);
                updateLevel2CacheForFields(unloadedFieldNumbers);
            }
            // We currently don't call postLoad here since this is only called as part of attaching an object
            // and consequently we just read to get the current (attached) values. 
            // Could add a flag on input to allow postLoad
        }
    }

    /**
     * Fetch the specified fields from the database.
     * @param fieldNumbers the numbers of the field(s) to fetch.
     */
    protected void loadSpecifiedFields(int[] fieldNumbers)
    {
        if (myOM.getApiAdapter().isDetached(myPC))
        {
            // Nothing to do since we're detached
            return;
        }

        // Try from the L2 cache first
        int[] unloadedFieldNumbers = loadFieldsFromLevel2Cache(fieldNumbers);
        if (unloadedFieldNumbers != null)
        {
            if (!isEmbedded()) // Embedded should always retrieve all in one go, so likely to be unnecessary
            {
                loadFieldsFromDatastore(unloadedFieldNumbers);
                updateLevel2CacheForFields(unloadedFieldNumbers);
            }
        }
    }

    /**
     * Convenience method to load the specified field if not loaded.
     * @param fieldNumber Absolute field number
     */
    public void loadField(int fieldNumber)
    {
        if (loadedFields[fieldNumber])
        {
            // Already loaded
            return;
        }
        loadSpecifiedFields(new int[]{fieldNumber});
    }

    /**
     * Fetch from the database all fields that are not currently loaded regardless of whether
     * they are in the current fetch group or not. Called by lifecycle transitions.
     */
    public void loadUnloadedFields()
    {
        int[] fieldNumbers = getFlagsSetTo(loadedFields, getAllFieldNumbers(), false);
        if (fieldNumbers == null || fieldNumbers.length == 0)
        {
            // All loaded so return
            return;
        }

        if (preDeleteLoadedFields != null &&
            ((myLC.isDeleted() && myOM.isFlushing()) || activity == ActivityState.DELETING))
        {
            // During deletion process so we know what is really loaded so only load if necessary
            fieldNumbers = getFlagsSetTo(preDeleteLoadedFields, fieldNumbers, false);
        }

        if (fieldNumbers != null && fieldNumbers.length > 0)
        {
            boolean callPostLoad = myFP.isToCallPostLoadFetchPlan(this.loadedFields);
            int[] unloadedFieldNumbers = loadFieldsFromLevel2Cache(fieldNumbers);
            if (unloadedFieldNumbers != null)
            {
                loadFieldsFromDatastore(unloadedFieldNumbers);
            }

            int[] secondClassMutableFieldNumbers = getSecondClassMutableFieldNumbers();

            // Make sure all SCO lazy-loaded fields have been loaded
            for (int i=0;i<secondClassMutableFieldNumbers.length;i++)
            {
                SingleValueFieldManager sfv = new SingleValueFieldManager();
                provideFields(new int[]{secondClassMutableFieldNumbers[i]}, sfv);
                Object value = sfv.fetchObjectField(i);
                if (value instanceof SCOContainer)
                {
                    ((SCOContainer)value).load();
                }
            }

            if (fieldNumbers != null)
            {
                updateLevel2CacheForFields(fieldNumbers);
            }
            if (callPostLoad)
            {
                postLoad();
            }
        }
    }

    /**
     * Method to load all unloaded fields in the FetchPlan.
     * Recurses through the FetchPlan objects and loads fields of sub-objects where needed.
     * Used as a precursor to detaching objects at commit since fields can't be loaded during
     * the postCommit phase when the detach actually happens.
     * @param state The FetchPlan state
     */
    public void loadFieldsInFetchPlan(FetchPlanState state)
    {
        if ((flags&FLAG_LOADINGFPFIELDS)!=0)
        {
            // Already in the process of loading fields in this class so skip
            return;
        }

        flags |= FLAG_LOADINGFPFIELDS;
        try
        {
            // Load unloaded FetchPlan fields of this object
            loadUnloadedFieldsInFetchPlan();

            // Recurse through all fields and do the same
            int[] fieldNumbers = getFlagsSetTo(loadedFields, getAllFieldNumbers(), true);
            if (fieldNumbers != null && fieldNumbers.length > 0)
            {
                // TODO Fix this to just access the fields of the FieldManager yet this actually does a replaceField
                replaceFields(fieldNumbers, new LoadFieldManager(this, getSecondClassMutableFields(), myFP, state));
                updateLevel2CacheForFields(fieldNumbers);
            }
        }
        finally
        {
            flags &= ~FLAG_LOADINGFPFIELDS;
        }
    }

    /**
     * Fetchs from the database all fields that are not currently loaded and that are in the current
     * fetch group. Called by lifecycle transitions.
     */
    public void loadUnloadedFieldsInFetchPlan()
    {
        int[] fieldNumbers = getFlagsSetTo(loadedFields, myFP.getMemberNumbers(), false);
        if (fieldNumbers != null && fieldNumbers.length > 0)
        {
            boolean callPostLoad = myFP.isToCallPostLoadFetchPlan(this.loadedFields);
            int[] unloadedFieldNumbers = loadFieldsFromLevel2Cache(fieldNumbers);
            if (unloadedFieldNumbers != null)
            {
                loadFieldsFromDatastore(unloadedFieldNumbers);
                updateLevel2CacheForFields(unloadedFieldNumbers);
            }
            if (callPostLoad)
            {
                postLoad();
            }
        }
    }

    /**
     * Fetchs from the database all fields in current fetch plan that are not currently loaded as well as
     * the version. Called by lifecycle transitions.
     */
    public void loadUnloadedFieldsInFetchPlanAndVersion()
    {
        if (!cmd.isVersioned())
        {
            loadUnloadedFieldsInFetchPlan();
        }
        else
        {
            int[] fieldNumbers = getFlagsSetTo(loadedFields, myFP.getMemberNumbers(), false);
            if (fieldNumbers == null)
            {
                fieldNumbers = new int[0];
            }

            boolean callPostLoad = myFP.isToCallPostLoadFetchPlan(this.loadedFields);
            int[] unloadedFieldNumbers = loadFieldsFromLevel2Cache(fieldNumbers);
            if (unloadedFieldNumbers != null)
            {
                loadFieldsFromDatastore(unloadedFieldNumbers);
                updateLevel2CacheForFields(unloadedFieldNumbers);
            }
            if (callPostLoad && fieldNumbers.length > 0)
            {
                postLoad();
            }
        }
    }

    /**
     * Fetchs from the database all fields in the actual fetch plan.
     * Called by life-cycle transitions.
     */
    public void loadUnloadedFieldsOfClassInFetchPlan(FetchPlan fetchPlan)
    {
        FetchPlanForClass fpc = fetchPlan.manageFetchPlanForClass(this.cmd);
        int[] fieldNumbers = getFlagsSetTo(loadedFields, fpc.getMemberNumbers(), false);
        if (fieldNumbers != null && fieldNumbers.length > 0)
        {
            boolean callPostLoad = fpc.isToCallPostLoadFetchPlan(this.loadedFields);
            int[] unloadedFieldNumbers = loadFieldsFromLevel2Cache(fieldNumbers);
            if (unloadedFieldNumbers != null)
            {
                loadFieldsFromDatastore(unloadedFieldNumbers);
                updateLevel2CacheForFields(unloadedFieldNumbers);
            }
            if (callPostLoad)
            {
                postLoad();
            }
        }
    }

    /**
     * Convenience method to unload a field/property.
     * @param fieldName Name of the field/property
     * @throws NucleusUserException if the object managed by this StateManager is embedded
     */
    public void unloadField(String fieldName)
    {
        if (pcObjectType == ObjectProvider.PC)
        {
            // Mark as not loaded
            AbstractMemberMetaData mmd = getClassMetaData().getMetaDataForMember(fieldName);
            loadedFields[mmd.getAbsoluteFieldNumber()] = false;
        }
        else
        {
            throw new NucleusUserException("Cannot unload field/property of embedded object");
        }
    }

    /**
     * Convenience method to mark PK fields as loaded (if using app id).
     */
    protected void markPKFieldsAsLoaded()
    {
        if (cmd.getIdentityType() == IdentityType.APPLICATION)
        {
            int[] pkPositions = cmd.getPKMemberPositions();
            for (int i=0;i<pkPositions.length;i++)
            {
                loadedFields[pkPositions[i]] = true;
            }
        }
    }

    /**
     * Refreshes from the database all fields in fetch plan.
     * Called by life-cycle transitions when the object undergoes a "transitionRefresh".
     */
    public void refreshFieldsInFetchPlan()
    {
        int[] fieldNumbers = myFP.getMemberNumbers();
        if (fieldNumbers != null && fieldNumbers.length > 0)
        {
            clearDirtyFlags(fieldNumbers);
            clearFlags(loadedFields, fieldNumbers);
            markPKFieldsAsLoaded(); // Can't refresh PK fields!

            boolean callPostLoad = myFP.isToCallPostLoadFetchPlan(this.loadedFields);

            // Refresh the fetch plan fields in this object
            setTransactionalVersion(null); // Make sure that the version is reset upon fetch
            loadFieldsFromDatastore(fieldNumbers);

            if (cmd.hasRelations(myOM.getClassLoaderResolver(), getMetaDataManager()))
            {
                // Check for cascade refreshes to related objects
                for (int i=0;i<fieldNumbers.length;i++)
                {
                    AbstractMemberMetaData fmd = cmd.getMetaDataForManagedMemberAtAbsolutePosition(fieldNumbers[i]);
                    int relationType = fmd.getRelationType(myOM.getClassLoaderResolver());
                    if (relationType != Relation.NONE && fmd.isCascadeRefresh())
                    {
                        // Need to refresh the related field object(s)
                        Object value = provideField(fieldNumbers[i]);
                        if (value != null)
                        {
                            if (value instanceof Collection)
                            {
                                // Refresh any PC elements in the collection
                                // TODO This should replace the SCO wrapper with a new one, or reload the wrapper
                                SCOUtils.refreshFetchPlanFieldsForCollection(this, ((Collection)value).toArray());
                            }
                            else if (value instanceof Map)
                            {
                                // Refresh any PC keys/values in the map
                                // TODO This should replace the SCO wrapper with a new one, or reload the wrapper
                                SCOUtils.refreshFetchPlanFieldsForMap(this, ((Map)value).entrySet());
                            }
                            else if (value instanceof PersistenceCapable)
                            {
                                // Refresh any PC fields
                                myOM.refreshObject(value);
                            }
                        }
                    }
                }
            }

            if (callPostLoad)
            {
                postLoad();
            }

            getCallbackHandler().postRefresh(myPC);
        }
    }
    
    /**
     * Refreshes from the database all fields currently loaded.
     * Called by life-cycle transitions when making transactional or reading fields.
     */
    public void refreshLoadedFields()
    {
        int[] fieldNumbers = getFlagsSetTo(loadedFields, myFP.getMemberNumbers(), true);

        if (fieldNumbers != null && fieldNumbers.length > 0)
        {
            clearDirtyFlags();
            clearFlags(loadedFields);
            markPKFieldsAsLoaded(); // Can't refresh PK fields!

            boolean callPostLoad = myFP.isToCallPostLoadFetchPlan(this.loadedFields);
            loadFieldsFromDatastore(fieldNumbers);
            if (callPostLoad)
            {
                postLoad();
            }
        }
    }

    /**
     * Method that will unload all fields that are not in the FetchPlan.
     * This is typically for use when the instance is being refreshed.
     */
    public void unloadNonFetchPlanFields()
    {
        int[] fpFieldNumbers = myFP.getMemberNumbers();
        int[] nonfpFieldNumbers = null;
        if (fpFieldNumbers == null || fpFieldNumbers.length == 0)
        {
            nonfpFieldNumbers = getAllFieldNumbers();
        }
        else
        {
            int fieldCount = getHighestFieldNumber();
            if (fieldCount == fpFieldNumbers.length)
            {
                // No fields that arent in FetchPlan
                return;
            }

            nonfpFieldNumbers = new int[fieldCount - fpFieldNumbers.length];
            int currentFPFieldIndex = 0;
            int j = 0;
            for (int i=0;i<fieldCount; i++)
            {
                if (currentFPFieldIndex >= fpFieldNumbers.length)
                {
                    // Past end of FetchPlan fields
                    nonfpFieldNumbers[j++] = i;
                }
                else
                {
                    if (fpFieldNumbers[currentFPFieldIndex] == i)
                    {
                        // FetchPlan field so move to next
                        currentFPFieldIndex++;
                    }
                    else
                    {
                        nonfpFieldNumbers[j++] = i;
                    }
                }
            }
        }

        // Mark all non-FetchPlan fields as unloaded
        for (int i=0;i<nonfpFieldNumbers.length;i++)
        {
            loadedFields[nonfpFieldNumbers[i]] = false;
        }
    }

    /**
     * Convenience method to load a field from the datastore.
     * Used in attaching fields and checking their old values (so we dont
     * want any postLoad method being called).
     * TODO Merge this with one of the loadXXXFields methods.
     * @param fieldNumber The field number.
     */
    public void loadFieldFromDatastore(int fieldNumber)
    {
        if ((flags&FLAG_NEED_INHERITANCE_VALIDATION)!=0) // TODO Merge this into fetch object handler
        {
            validateInheritance();
        }

        // TODO If the field has "loadFetchGroup" defined, then add it to the fetch plan etc
        getStoreManager().getPersistenceHandler().fetchObject(this, new int[]{fieldNumber});
    }

    /**
     * Convenience method to load a field from the datastore.
     * @param fieldNumbers The field numbers.
     */
    private void loadFieldsFromDatastore(int[] fieldNumbers)
    {
        if (myLC.isNew() && myLC.isPersistent() && !isFlushedNew())
        {
            // Not yet flushed new persistent object to datastore so no point in "loading"
            return;
        }

        if ((flags&FLAG_NEED_INHERITANCE_VALIDATION)!=0) // TODO Merge this into fetch object handler
        {
            validateInheritance();
        }

        // TODO If the field has "loadFetchGroup" defined, then add it to the fetch plan etc
        getStoreManager().getPersistenceHandler().fetchObject(this, fieldNumbers);
    }

    /**
     * Method to validate the inheritance of this object. Used in the situation where the user requested
     * an object with an id and didn't want it validating at that point, but we must prevent its usage
     * when fields are loaded if it is basically wrong.
     * @throws NucleusObjectNotFoundException if the object has incorrect inheritance level
     */
    private void validateInheritance()
    {
        String className = getStoreManager().getClassNameForObjectID(myID, myOM.getClassLoaderResolver(), myOM);
        if (!myPC.getClass().getName().equals(className))
        {
            myOM.removeObjectFromCache(myID);
            myOM.removeObjectFromLevel2Cache(myID);
            throw new NucleusObjectNotFoundException("Object with id " + myID + 
                " was created without validating of type " + myPC.getClass().getName() +
                " but is actually of type " + className);
        }
        flags &= ~FLAG_NEED_INHERITANCE_VALIDATION;
    }

    /**
     * Returns the loaded setting for the field of the managed object.
     * Refer to the javadoc of isLoaded(PersistenceCapable, int);
     * @param field the absolute field number
     * @return always returns true (this implementation)
     */
    public boolean isLoaded(int field)
    {
        return isLoaded(myPC, field);
    }

    /**
     * Return true if the field is cached in the calling instance.
     * In this implementation, isLoaded() will always return true. 
     * If the field is not loaded, it will be loaded as a side effect of the 
     * call to this method. If it is in the default fetch group,
     * the default fetch group, including this field, will be loaded.
     *
     * @param pc the calling PersistenceCapable instance
     * @param field the absolute field number
     * @return always returns true (this implementation)
     */
    public boolean isLoaded(PersistenceCapable pc, int field)
    {
        try
        {
            if (disconnectClone(pc))
            {
                return true;
            }
            else
            {
                boolean checkRead = true;
                boolean beingDeleted = false;
                if ((myLC.isDeleted() && myOM.isFlushing()) || activity == ActivityState.DELETING)
                {
                    // Bypass "read-field" check when deleting, or when marked for deletion and flushing
                    checkRead = false;
                    beingDeleted = true;
                }
                if (checkRead)
                {
                    transitionReadField(loadedFields[field]);
                }

                if (!loadedFields[field])
                {
                    // Field not loaded, so load it
                    if (pcObjectType != ObjectProvider.PC)
                    {
                        // Embedded object so we assume that all was loaded before (when it was read)
                        return true;
                    }

                    if (beingDeleted && preDeleteLoadedFields != null && preDeleteLoadedFields[field])
                    {
                        // Field was loaded prior to starting delete so just return true
                        return true;
                    }
                    else if (!beingDeleted && myFP.hasMember(field))
                    {
                        // Load rest of FetchPlan if this is part of it (and not in the process of deletion)
                        loadUnloadedFieldsInFetchPlan();
                    }
                    else
                    {
                        // Just load this field
                        loadSpecifiedFields(new int[] {field});
                    }
                }

                return true;
            }
        }
        catch (NucleusException ne)
        {
            NucleusLogger.PERSISTENCE.warn("Exception thrown by StateManager.isLoaded", ne);

            // Convert into an exception suitable for the current API since this is called from a user update of a field
            throw myOM.getApiAdapter().getApiExceptionForNucleusException(ne);
        }
    }

    /**
     * Method to return the current value of a particular field.
     * @param fieldNumber Number of field
     * @return The value of the field
     */
    public Object provideField(int fieldNumber)
    {
        return provideField(myPC, fieldNumber);
    }

    /**
     * Convenience method to change the value of a field that is assumed loaded.
     * Will mark the object/field as dirty if it isnt previously. If the object is deleted then does nothing.
     * Doesn't cater for embedded fields.
     * *** Only for use in management of relations. ***
     * @param fieldNumber Number of field
     * @param newValue The new value
     */
    public void replaceFieldValue(int fieldNumber, Object newValue)
    {
        if (myLC.isDeleted())
        {
            // Object is deleted so do nothing
            return;
        }
        boolean currentWasDirty = preWriteField(fieldNumber);
        replaceField(myPC, fieldNumber, newValue, true);
        postWriteField(currentWasDirty);
    }

    /**
     * Method to change the value of a particular field and not mark it dirty.
     * @param fieldNumber Number of field
     * @param value New value
     */
    public void replaceField(int fieldNumber, Object value)
    {
        replaceField(myPC, fieldNumber, value, false);
    }

    /**
     * Method to change the value of a particular field and mark it dirty.
     * @param fieldNumber Number of field
     * @param value New value
     */
    public void replaceFieldMakeDirty(int fieldNumber, Object value)
    {
        replaceField(myPC, fieldNumber, value, true);
    }

    /**
     * Method to change the value of a field in the PC object.
     * Adds on handling for embedded fields to the superclass handler.
     * @param pc The PC object
     * @param fieldNumber Number of field
     * @param value The new value of the field
     * @param makeDirty Whether to make the field dirty while replacing its value (in embedded owners)
     */
    protected void replaceField(PersistenceCapable pc, int fieldNumber, Object value, boolean makeDirty)
    {
        if (embeddedOwners != null)
        {
            // Notify any owners that embed this object that it has just changed
            // We do this before we actually change the object so we can compare with the old value
            Iterator<EmbeddedOwnerRelation> ownerIter = embeddedOwners.iterator();
            while (ownerIter.hasNext())
            {
                EmbeddedOwnerRelation owner = ownerIter.next();
                JDOStateManagerImpl ownerSM = (JDOStateManagerImpl)owner.sm;

                if (ownerSM == null || ownerSM.cmd == null)
                {
                    //for some reason these are null... raised when running JPA TCK
                    continue;
                }

                AbstractMemberMetaData ownerMmd = ownerSM.cmd.getMetaDataForManagedMemberAtAbsolutePosition(owner.fieldNumber);
                if (ownerMmd.getCollection() != null)
                {
                    // PC Object embedded in collection
                    Object ownerField = ownerSM.provideField(owner.fieldNumber);
                    if (ownerField instanceof SCOCollection)
                    {
                        ((SCOCollection)ownerField).updateEmbeddedElement(myPC, fieldNumber, value);
                    }
                }
                else if (ownerMmd.getMap() != null)
                {
                    // PC Object embedded in map
                    Object ownerField = ownerSM.provideField(owner.fieldNumber);
                    if (ownerField instanceof SCOMap)
                    {
                        if (pcObjectType == ObjectProvider.EMBEDDED_MAP_KEY_PC)
                        {
                            ((SCOMap)ownerField).updateEmbeddedKey(myPC, fieldNumber, value);
                        }
                        if (pcObjectType == ObjectProvider.EMBEDDED_MAP_VALUE_PC)
                        {
                            ((SCOMap)ownerField).updateEmbeddedValue(myPC, fieldNumber, value);
                        }
                    }
                }
                else
                {
                    // PC Object embedded in PC object
                    if ((ownerSM.flags&FLAG_UPDATING_EMBEDDING_FIELDS_WITH_OWNER)==0)
                    {
                        // Update the owner when one of our fields have changed, EXCEPT when they have just
                        // notified us of our owner field!
                        if (makeDirty)
                        {
                            ownerSM.replaceFieldMakeDirty(owner.fieldNumber, pc);
                        }
                        else
                        {
                            ownerSM.replaceField(owner.fieldNumber, pc);
                        }
                    }
                }
            }
        }

        // Update the field in our PC object
        // TODO Why don't we mark as dirty if non-tx ? Maybe need P_NONTRANS_DIRTY
        if (embeddedOwners == null && makeDirty && !myLC.isDeleted() && myOM.getTransaction().isActive())
        {
            // Mark dirty (if not being deleted)
            boolean wasDirty = preWriteField(fieldNumber);
            super.replaceField(pc, fieldNumber, value);
            postWriteField(wasDirty);
        }
        else
        {
            super.replaceField(pc, fieldNumber, value);
        }
    }

    /**
     * Called from the StoreManager to refresh data in the PersistenceCapable
     * object associated with this StateManager.
     * @param fieldNumbers An array of field numbers to be refreshed by the Store
     * @param fm The updated values are stored in this object. This object is only valid
     *   for the duration of this call.
     * @param replaceWhenDirty Whether to replace the fields when they are dirty here
     */
    public void replaceFields(int fieldNumbers[], FieldManager fm, boolean replaceWhenDirty)
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
                int[] fieldsToReplace = fieldNumbers;
                if (!replaceWhenDirty)
                {
                    int numberToReplace = fieldNumbers.length;
                    for (int i=0;i<fieldNumbers.length;i++)
                    {
                        if (dirtyFields[fieldNumbers[i]])
                        {
                            numberToReplace--;
                        }
                    }
                    if (numberToReplace > 0 && numberToReplace != fieldNumbers.length)
                    {
                        fieldsToReplace = new int[numberToReplace];
                        int n = 0;
                        for (int i=0;i<fieldNumbers.length;i++)
                        {
                            if (!dirtyFields[fieldNumbers[i]])
                            {
                                fieldsToReplace[n++] = fieldNumbers[i];
                            }
                        }
                    }
                    else if (numberToReplace == 0)
                    {
                        fieldsToReplace = null;
                    }
                }

                if (fieldsToReplace != null)
                {
                    myPC.jdoReplaceFields(fieldsToReplace);
                }
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

    /**
     * Called from the StoreManager to refresh data in the PersistenceCapable
     * object associated with this StateManager.
     * @param fieldNumbers An array of field numbers to be refreshed by the Store
     * @param fm The updated values are stored in this object. This object is only valid
     *   for the duration of this call.
     */
    public void replaceFields(int fieldNumbers[], FieldManager fm)
    {
        replaceFields(fieldNumbers, fm, true);
    }

    /**
     * Called from the StoreManager to refresh data in the PersistenceCapable
     * object associated with this StateManager. Only not loaded fields are refreshed
     *
     * @param fieldNumbers An array of field numbers to be refreshed by the Store
     * @param fm The updated values are stored in this object. This object is only valid
     *   for the duration of this call.
     */
    public void replaceNonLoadedFields(int fieldNumbers[], FieldManager fm)
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

            boolean callPostLoad = myFP.isToCallPostLoadFetchPlan(this.loadedFields);
            try
            {
                int[] fieldsToReplace = getFlagsSetTo(loadedFields, fieldNumbers, false);
                if (fieldsToReplace != null && fieldsToReplace.length > 0)
                {
                    myPC.jdoReplaceFields(fieldsToReplace);
                }
            }
            finally
            {
                currFM = prevFM;
            }
            if (callPostLoad && isFetchPlanLoaded())
            {
                // The fetch plan is now loaded so fire off any necessary post load
                postLoad();
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
     * Method to register an owner StateManager with this embedded/serialised object.
     * @param ownerSM The owning State Manager.
     * @param ownerFieldNumber The field number in the owner that the embedded/serialised object is stored as
     */
    public void addEmbeddedOwner(ObjectProvider ownerSM, int ownerFieldNumber)
    {
        if (ownerSM == null)
        {
            return;
        }

        if (embeddedOwners == null)
        {
            embeddedOwners = new ArrayList(1);
        }
        embeddedOwners.add(new EmbeddedOwnerRelation((org.datanucleus.state.StateManager) ownerSM, ownerFieldNumber));
    }

    /**
     * Method to remove an owner StateManager from this embedded/serialised objects owners list.
     * @param ownerSM The owner to remove
     * @param ownerFieldNumber The field in the owner where this object is stored
     */
    public void removeEmbeddedOwner(org.datanucleus.state.StateManager ownerSM, int ownerFieldNumber)
    {
        if (embeddedOwners != null)
        {
            Iterator<EmbeddedOwnerRelation> iter = embeddedOwners.iterator();
            while (iter.hasNext())
            {
                EmbeddedOwnerRelation relation = iter.next();
                if (relation.sm == ownerSM && relation.fieldNumber == ownerFieldNumber)
                {
                    iter.remove();
                    break;
                }
            }
            if (embeddedOwners.isEmpty())
            {
                embeddedOwners = null;
            }
        }
    }

    /**
     * Accessor for the owning StateManagers for the managed object when stored embedded.
     * Should really only have a single owner but users could, in principle, assign it to multiple.
     * @return StateManagers owning this embedded object.
     */
    public org.datanucleus.state.StateManager[] getEmbeddedOwners()
    {
        if (embeddedOwners == null)
        {
            return null;
        }
        org.datanucleus.state.StateManager[] owners = new org.datanucleus.state.StateManager[embeddedOwners.size()];
        for (int i=0;i<owners.length;i++)
        {
            EmbeddedOwnerRelation relation = embeddedOwners.get(i);
            owners[i] = relation.sm;
        }
        return owners;
    }

    /**
     * Wrapper class storing the owning state manager, and the field of the
     * PC managed by the owning state manager where this object is embedded/serialised.
     */
    private static class EmbeddedOwnerRelation
    {
        private org.datanucleus.state.StateManager sm;
        private int fieldNumber;

        /**
         * 
         * @param ownerSM the owner StateManager
         * @param ownerFieldNumber the absolute owner field number
         */
        public EmbeddedOwnerRelation(org.datanucleus.state.StateManager ownerSM, int ownerFieldNumber)
        {
            this.sm = ownerSM;
            this.fieldNumber = ownerFieldNumber;
        }
    }

    /**
     * Method to replace all loaded SCO fields with wrappers.
     * If the loaded field already uses a SCO wrapper nothing happens to that field.
     */
    public void replaceAllLoadedSCOFieldsWithWrappers()
    {
        boolean[] scoMutableFieldFlags = cmd.getSCOMutableMemberFlags();
        for (int i=0;i<scoMutableFieldFlags.length;i++)
        {
            if (scoMutableFieldFlags[i] && loadedFields[i])
            {
                Object value = provideField(i);
                if (!(value instanceof SCO))
                {
                    wrapSCOField(i, value, false, false, true);
                }
            }
        }
    }

    /**
     * Method to replace all loaded SCO fields that have wrappers with their value.
     * If the loaded field doesnt have a SCO wrapper nothing happens to that field.
     */
    public void replaceAllLoadedSCOFieldsWithValues()
    {
        boolean[] scoMutableFieldFlags = cmd.getSCOMutableMemberFlags();
        for (int i=0;i<scoMutableFieldFlags.length;i++)
        {
            if (scoMutableFieldFlags[i] && loadedFields[i])
            {
                Object value = provideField(i);
                if (value instanceof SCO)
                {
                    unwrapSCOField(i, value, true);
                }
            }
        }
    }

    /**
     * Method to unwrap a SCO field (if it is wrapped currently).
     * If the field is not a SCO field will just return the value.
     * If "replaceFieldIfChanged" is set, we replace the value in the object with the unwrapped value.
     * @param fieldNumber The field number
     * @param value The value for the field
     * @param replaceFieldIfChanged Whether to replace the field value in the object if unwrapping the value
     * @return The unwrapped field value
     */
    public Object unwrapSCOField(int fieldNumber, Object value, boolean replaceFieldIfChanged)
    {
        if (value == null)
        {
            return value;
        }
        if (getSecondClassMutableFields()[fieldNumber] && value instanceof SCO)
        {
            SCO sco = (SCO)value;

            // Not a SCO wrapper, or is a SCO wrapper but not owned by this object
            Object unwrappedValue = sco.getValue();
            if (replaceFieldIfChanged)
            {
                AbstractMemberMetaData fmd = cmd.getMetaDataForManagedMemberAtAbsolutePosition(fieldNumber);
                if (NucleusLogger.PERSISTENCE.isDebugEnabled())
                {
                    NucleusLogger.PERSISTENCE.debug(LOCALISER.msg("026030",
                        StringUtils.toJVMIDString(myPC), myOM.getIdentityAsString(myID), fmd.getName()));
                }
                replaceField(myPC, fieldNumber, unwrappedValue, false);
            }
            return unwrappedValue;
        }
        return value;
    }

    /**
     * Method to create a new SCO wrapper for the specified field.
     * If the field is not a SCO field will just return the value.
     * @param fieldNumber The field number
     * @param value The value to initialise the wrapper with (if any)
     * @param forInsert Whether the creation of any wrapper should insert this value into the datastore
     * @param forUpdate Whether the creation of any wrapper should update the datastore with this value
     * @param replaceFieldIfChanged Whether to replace the field in the object if wrapping the value
     * @return The wrapper (or original value if not wrappable)
     */
    public Object wrapSCOField(int fieldNumber, Object value, 
            boolean forInsert, boolean forUpdate, boolean replaceFieldIfChanged)
    {
        if (value == null)
        {
            // We don't wrap null objects currently
            return value;
        }

        if (value instanceof PersistenceCapable)
        {
            // Special case of SCO that we should split into a separate method for clarity, nothing to do with wrapping
            AbstractMemberMetaData fmd = cmd.getMetaDataForManagedMemberAtAbsolutePosition(fieldNumber);
            if (fmd.getEmbeddedMetaData() != null && fmd.getEmbeddedMetaData().getOwnerMember() != null)
            {
                // Embedded field, so assign the embedded/serialised object "owner-field" if specified
                JDOStateManagerImpl subSM = (JDOStateManagerImpl)myOM.findObjectProvider(value);
                int ownerAbsFieldNum = subSM.cmd.getAbsolutePositionOfMember(fmd.getEmbeddedMetaData().getOwnerMember());
                if (ownerAbsFieldNum >= 0)
                {
                    flags |= FLAG_UPDATING_EMBEDDING_FIELDS_WITH_OWNER;
                    subSM.replaceFieldMakeDirty(ownerAbsFieldNum, myPC);
                    flags &= ~FLAG_UPDATING_EMBEDDING_FIELDS_WITH_OWNER;
                }
            }
        }

        if (getSecondClassMutableFields()[fieldNumber])
        {
            if (!(value instanceof SCO) || myPC != ((SCO)value).getOwner())
            {
                // Not a SCO wrapper, or is a SCO wrapper but not owned by this object
                AbstractMemberMetaData fmd = cmd.getMetaDataForManagedMemberAtAbsolutePosition(fieldNumber);
                if (replaceFieldIfChanged)
                {
                    if (NucleusLogger.PERSISTENCE.isDebugEnabled())
                    {
                        NucleusLogger.PERSISTENCE.debug(LOCALISER.msg("026029", 
                            StringUtils.toJVMIDString(myPC), 
                            myOM != null ? myOM.getIdentityAsString(myID) : myID, fmd.getName()));
                    }
                }
                return SCOUtils.newSCOInstance(this, fmd, fmd.getType(), 
                    value.getClass(), value, forInsert, forUpdate, replaceFieldIfChanged);
            }
        }

        return value;
    }

    // ------------------------- Lifecycle Methods -----------------------------

    /**
     * Method to mark an object for reachability.
     * Provides the basis for "persistence-by-reachability", but run at commit time only.
     * The reachability algorithm is also run at makePersistent, but directly via InsertRequest.
     * @param reachables List of object ids currently logged as reachable
     */
    public void runReachability(Set reachables)
    {
        if (reachables == null)
        {
            return;
        }
        if (!reachables.contains(getInternalObjectId()))
        {
            // Make sure all changes are persisted
            flush();

            if (isDeleted(myPC))
            {
                // This object is deleted so nothing further will be reachable
                return;
            }

            // This object was enlisted so make sure all of its fields are loaded before continuing
            if (getObjectManager().isEnlistedInTransaction(getInternalObjectId()))
            {
                loadUnloadedFields();
            }

            if (NucleusLogger.PERSISTENCE.isDebugEnabled())
            {
                NucleusLogger.PERSISTENCE.debug(LOCALISER.msg("007000", 
                    StringUtils.toJVMIDString(myPC), getObjectId(myPC), myLC));
            }
            // Add this object id since not yet reached
            reachables.add(getInternalObjectId());

            // Go through all (loaded FetchPlan) fields for reachability using ReachabilityFieldManager
            int[] loadedFieldNumbers = getFlagsSetTo(loadedFields, getAllFieldNumbers(), true);
            if (loadedFieldNumbers != null && loadedFieldNumbers.length > 0)
            {
                provideFields(loadedFieldNumbers, new ReachabilityFieldManager(this, reachables));
            }
        }
    }

    /**
     * Method to make the object persistent.
     */
    public void makePersistent()
    {
        if (myLC.isDeleted() && !myOM.getNucleusContext().getApiAdapter().allowPersistOfDeletedObject())
        {
            // API doesnt allow repersist of deleted objects
            return;
        }

        if (dirty && !myLC.isDeleted() && myLC.isTransactional() && myOM.isDelayDatastoreOperationsEnabled())
        {
            // Already provisionally persistent, but delaying til commit so just re-run reachability
            // to bring in any new objects that are now reachable
            provideFields(cmd.getAllMemberPositions(), new PersistFieldManager(this, false));
            return;
        }

        getCallbackHandler().prePersist(myPC);

        if (isFlushedNew())
        {
            // With CompoundIdentity bidir relations when the SM is created for this object ("initialiseForPersistentNew") the persist
            // of the PK PC fields can cause the flush of this object, and so it is already persisted by the time we ge here
            registerTransactional();
            return;
        }

        if (cmd.isEmbeddedOnly())
        {
            // Cant persist an object of this type since can only be embedded
            return;
        }

        // If this is an embedded/serialised object becoming persistent in its own right, assign an identity.
        if (myID == null)
        {
            setIdentity(false);
        }

        dirty = true;

        if (myOM.isDelayDatastoreOperationsEnabled())
        {
            // Delaying datastore flush til later
            myOM.markDirty(this, false);
            if (NucleusLogger.PERSISTENCE.isDebugEnabled())
            {
                NucleusLogger.PERSISTENCE.debug(LOCALISER.msg("026028", StringUtils.toJVMIDString(myPC)));
            }
            registerTransactional();

            if (myLC.isTransactional() && myLC.isDeleted())
            {
                // Re-persist of a previously deleted object
                myLC = myLC.transitionMakePersistent(this);
            }

            // TODO Only run this if we have relations from this class
            // Run reachability on all fields of this PC - JDO2 [12.6.7]
            provideFields(cmd.getAllMemberPositions(), new PersistFieldManager(this, false));
        }
        else
        {
            // Persist the object and all reachables
            internalMakePersistent();
            registerTransactional();
        }
    }

    /**
     * Method to persist the object to the datastore.
     */
    private void internalMakePersistent()
    {
        activity = ActivityState.INSERTING;
        boolean[] tmpDirtyFields = dirtyFields.clone();
        try
        {
            getCallbackHandler().preStore(myPC); // This comes after setting the INSERTING flag so we know we are inserting it now
            if (myID == null)
            {
                setIdentity(true); // Just in case user is setting it in preStore
            }

            //in InstanceLifecycleEvents this object could get dirty if a field is changed in preStore or
            //postCreate, we clear dirty flags to make sure this object will not be flushed again
            clearDirtyFlags();

            getStoreManager().getPersistenceHandler().insertObject(this);
            setFlushedNew(true);

            getCallbackHandler().postStore(myPC);

            if (!isEmbedded())
            {
                // Update the object in the cache(s) - has version set etc now
                myOM.putObjectIntoCache(this);
            }
        }
        catch (NotYetFlushedException ex)
        {
            //happening on cyclic relationships
            //if not yet flushed error, we rollback dirty fields, so we can retry inserting
            dirtyFields = tmpDirtyFields;
            myOM.markDirty(this, false);
            dirty = true;
            //we throw exception, so the owning relationship will mark it's foreign key to update later
            throw ex;
        }
        finally
        {
            activity = ActivityState.NONE;
        }
    }

    /**
     * Tests whether this object is being inserted.
     * @return true if this instance is inserting.
     */
    public boolean isInserting()
    {
        return (activity == ActivityState.INSERTING);
    }

    /**
     * Tests whether this object is being deleted.
     * @return true if this instance is being deleted.
     */
    public boolean isDeleting()
    {
        return (activity == ActivityState.DELETING);
    }

    /**
     * Accessor for whether the instance is newly persistent yet hasnt yet been flushed to the datastore.
     * @return Whether not yet flushed to the datastore
     */
    public boolean isWaitingToBeFlushedToDatastore()
    {
        // Return true if object is new and not yet flushed to datastore
        return myLC.stateType() == LifeCycleState.P_NEW && !isFlushedNew();
    }

    /**
     * Change the activity state.
     * @param activityState the new state
     */
    public void changeActivityState(ActivityState activityState)
    {
        activity = activityState;
        if (activityState == ActivityState.INSERTING_CALLBACKS && insertionNotifyList != null)
        {
            // Full insertion has just completed so notify all interested parties
            synchronized (insertionNotifyList)
            {
                Iterator<org.datanucleus.state.StateManager> notifyIter = insertionNotifyList.iterator();
                while (notifyIter.hasNext())
                {
                    org.datanucleus.state.StateManager notifySM = notifyIter.next();
                    ((JDOStateManagerImpl)notifySM).insertionCompleted(this);
                }
            }
            insertionNotifyList.clear();
            insertionNotifyList = null;
        }
    }

    /**
     * Method to add a notifier that we must contact when we have finished our insertion.
     * @param sm the state manager
     * @param activityState the ActivityState (unused)
     */
    public void addInsertionNotifier(org.datanucleus.state.StateManager sm, ActivityState activityState)
    {
        // TODO Use the second param to add the StateManager to other lists for other events
        if (insertionNotifyList == null)
        {
            insertionNotifyList = Collections.synchronizedList(new ArrayList(1));
        }
        insertionNotifyList.add(sm);
    }

    /**
     * Marks the given field as being required to be updated when the specified object has been inserted.
     * @param pc The Persistable object
     * @param fieldNumber Number of the field.
     */
    public void updateFieldAfterInsert(Object pc, int fieldNumber)
    {
        JDOStateManagerImpl otherSM = (JDOStateManagerImpl) myOM.findObjectProvider(pc);

        // Register the other SM to update us when it is inserted
        otherSM.addInsertionNotifier(this, ActivityState.INSERTING_CALLBACKS);

        // Register that we should update this field when the other SM informs us
        if (fieldsToBeUpdatedAfterObjectInsertion == null)
        {
            fieldsToBeUpdatedAfterObjectInsertion = new HashMap(1);
        }
        FieldContainer cont = fieldsToBeUpdatedAfterObjectInsertion.get(otherSM);
        if (cont == null)
        {
            cont = new FieldContainer(fieldNumber);
        }
        else
        {
            cont.set(fieldNumber);
        }
        fieldsToBeUpdatedAfterObjectInsertion.put(otherSM, cont);

        if (NucleusLogger.PERSISTENCE.isDebugEnabled())
        {
            NucleusLogger.PERSISTENCE.debug(LOCALISER.msg("026021", 
                cmd.getMetaDataForManagedMemberAtAbsolutePosition(fieldNumber).getFullFieldName(), 
                StringUtils.toJVMIDString(myPC), StringUtils.toJVMIDString(pc)));
        }
    }

    /**
     * Method called by another StateManager when this object has registered that it needs to know
     * when the other object has been inserted.
     * @param sm State Manager of the other object that has just been inserted
     */
    void insertionCompleted(org.datanucleus.state.StateManager sm)
    {
        if (fieldsToBeUpdatedAfterObjectInsertion == null)
        {
            return;
        }

        // Go through our insertion update list and mark all required fields as dirty
        FieldContainer fldCont = fieldsToBeUpdatedAfterObjectInsertion.get(sm);
        if (fldCont != null)
        {
            dirty = true;
            int[] fieldsToUpdate = fldCont.getFields();
            for (int i=0;i<fieldsToUpdate.length;i++)
            {
                if (NucleusLogger.PERSISTENCE.isDebugEnabled())
                {
                    NucleusLogger.PERSISTENCE.debug(LOCALISER.msg("026022", 
                        cmd.getMetaDataForManagedMemberAtAbsolutePosition(fieldsToUpdate[i]).getFullFieldName(), 
                        myOM.getIdentityAsString(myID),
                        StringUtils.toJVMIDString(sm.getObject())));
                }
                dirtyFields[fieldsToUpdate[i]] = true;
            }
            fieldsToBeUpdatedAfterObjectInsertion.remove(sm);
            if (fieldsToBeUpdatedAfterObjectInsertion.isEmpty())
            {
                fieldsToBeUpdatedAfterObjectInsertion = null;
            }

            try
            {
                flags |= FLAG_POSTINSERT_UPDATE;

                // Perform our update
                flush();
            }
            finally
            {
                flags &= ~FLAG_POSTINSERT_UPDATE;
            }
        }
    }

    /**
     * Convenience method to return if we are in the phase of performing postInsert updates
     * due to related objects having been inserted.
     * @return Whether we are updating for postInsert
     */
    public boolean isUpdatingFieldForPostInsert()
    {
        return (flags&FLAG_POSTINSERT_UPDATE)!=0;
    }

    /** Private class storing the fields to be updated for a StateManager, when it is inserted */
    private class FieldContainer
    {
        boolean[] fieldsToUpdate = new boolean[getAllFieldNumbers().length];
        /**
         * Constructor
         * @param fieldNumber the absolute field number to flag true
         */
        public FieldContainer(int fieldNumber)
        {
            fieldsToUpdate[fieldNumber] = true;
        }
        /**
         * Flag to true the <code>fieldNumber</code>
         * @param fieldNumber the absolute field number to flag true
         */
        public void set(int fieldNumber)
        {
            fieldsToUpdate[fieldNumber] = true;
        }
        /**
         * Array with absolute field numbers with true flag
         * @return array of absolute field numbers
         */
        public int[] getFields()
        {
            return getFlagsSetTo(fieldsToUpdate,true);
        }
    }

    /**
     * Method to set an associated value stored with this object.
     * This is for a situation such as in ORM where this object can have an "external" foreign-key
     * provided by an owning object (e.g 1-N uni relation and this is the element with no knowledge
     * of the owner, so the associated value is the FK value).
     * @param key Key for the value
     * @param value The associated value
     */
    public void setAssociatedValue(Object key, Object value)
    {
        if (associatedValuesMap == null)
        {
            associatedValuesMap = new HashMap(1);
        }
        associatedValuesMap.put(key, value);
    }

    /**
     * Accessor for an associated value stored with this object.
     * This is for a situation such as in ORM where this object can have an "external" foreign-key
     * provided by an owning object (e.g 1-N uni relation and this is the element with no knowledge
     * of the owner, so the associated value is the FK value).
     * @param key Key for the value
     * @return The associated value
     */
    public Object getAssociatedValue(Object key)
    {
        if (associatedValuesMap == null)
        {
            return null;
        }
        return associatedValuesMap.get(key);
    }

    /**
     * Method to change the object state to transactional.
     */
    public void makeTransactional()
    {
        preStateChange();
        try
        {
            if (myLC == null)
            {
                initializeSM(LifeCycleState.T_CLEAN);
                setRestoreValues(true);
            }
            else
            {
                myLC = myLC.transitionMakeTransactional(this, true);
            }
        }
        finally
        {
            postStateChange();
        }
    }

    /**
     * Method to change the object state to transient.
     * @param state Object containing the state of any fetchplan processing
     */
    public void makeTransient(FetchPlanState state)
    {
        if (((flags&FLAG_MAKING_TRANSIENT)!=0))
        {
            return; // In the process of becoming transient
        }

        try
        {
            flags |= FLAG_MAKING_TRANSIENT;
            if (state == null)
            {
                // No FetchPlan in use so just unset the owner of all loaded SCO fields
                int[] fieldNumbers = getFlagsSetTo(loadedFields, getSecondClassMutableFieldNumbers(), true);
                if (fieldNumbers != null && fieldNumbers.length > 0)
                {
                    provideFields(fieldNumbers, new UnsetOwners());
                }
            }
            else
            {
                // Make all loaded SCO fields transient appropriate to this fetch plan
                loadUnloadedFieldsInFetchPlan();
                int[] fieldNumbers = getFlagsSetTo(loadedFields, getAllFieldNumbers(), true);
                if (fieldNumbers != null && fieldNumbers.length > 0)
                {
                    // TODO Fix this to just access the fields of the FieldManager yet this actually does a replaceField
                    replaceFields(fieldNumbers, new MakeTransientFieldManager(this, getSecondClassMutableFields(), myFP, state));
                }
            }

            preStateChange();
            try
            {
                myLC = myLC.transitionMakeTransient(this, state != null, myOM.isRunningDetachAllOnCommit());
            }
            finally
            {
                postStateChange();
            }
        }
        finally
        {
            flags &= ~FLAG_MAKING_TRANSIENT;
        }
    }

    /**
     * Method to detach this object.
     * If the object is detachable then it will be migrated to DETACHED state, otherwise will migrate
     * to TRANSIENT. Used by "DetachAllOnCommit"/"DetachAllOnRollback"
     * @param state State for the detachment process
     */
    public void detach(FetchPlanState state)
    {
        if (myOM == null)
        {
            return;
        }

        ApiAdapter api = myOM.getApiAdapter();
        if (myLC.isDeleted() || api.isDetached(myPC) || ((flags&FLAG_DETACHING)!=0))
        {
            // Already deleted, detached or being detached
            return;
        }

        // Check if detachable ... if so then we detach a copy, otherwise we return a transient copy
        boolean detachable = api.isDetachable(myPC);
        if (detachable)
        {
            if (NucleusLogger.PERSISTENCE.isDebugEnabled())
            {
                NucleusLogger.PERSISTENCE.debug(LOCALISER.msg("010009", StringUtils.toJVMIDString(myPC), 
                    "" + state.getCurrentFetchDepth()));
            }

            // Call any "pre-detach" listeners
            getCallbackHandler().preDetach(myPC);
        }

        try
        {
            flags |= FLAG_DETACHING;

            String detachedState = myOM.getNucleusContext().getPersistenceConfiguration().getStringProperty("datanucleus.detachedState");
            if (detachedState.equalsIgnoreCase("all"))
            {
                loadUnloadedFields();
            }
            else if (detachedState.equalsIgnoreCase("loaded"))
            {
                // Do nothing since just using currently loaded fields
            }
            else
            {
                // Using fetch-groups, so honour detachmentOptions for loading/unloading
                if ((myOM.getFetchPlan().getDetachmentOptions() & FetchPlan.DETACH_LOAD_FIELDS) != 0)
                {
                    // Load any unloaded fetch-plan fields
                    loadUnloadedFieldsInFetchPlan();
                }
                if ((myOM.getFetchPlan().getDetachmentOptions() & FetchPlan.DETACH_UNLOAD_FIELDS) != 0)
                {
                    // Unload any loaded fetch-plan fields that aren't in the current fetch plan
                    unloadNonFetchPlanFields();

                    // Remove the values from the detached object - not required by the spec
                    int[] unloadedFields = getFlagsSetTo(loadedFields, getAllFieldNumbers(), false);
                    if (unloadedFields != null && unloadedFields.length > 0)
                    {
                        PersistenceCapable dummyPC = myPC.jdoNewInstance(this);
                        myPC.jdoCopyFields(dummyPC, unloadedFields);
                        replaceStateManager(dummyPC, null);
                    }
                }
            }

            // Detach all (loaded) fields in the FetchPlan
            FieldManager detachFieldManager = new DetachFieldManager(this, getSecondClassMutableFields(), 
                myFP, state, false);
            for (int i = 0; i < loadedFields.length; i++)
            {
                if (loadedFields[i])
                {
                    try
                    {
                        // Just fetch the field since we are usually called in postCommit() so dont want to update it
                        detachFieldManager.fetchObjectField(i);
                    }
                    catch (EndOfFetchPlanGraphException eofpge)
                    {
                        Object value = provideField(i);
                        if (api.isPersistable(value))
                        {
                            // PC field beyond end of graph
                            org.datanucleus.state.StateManager valueSM = myOM.findStateManager(value);
                            if (!api.isDetached(value) && !(valueSM != null && valueSM.isDetaching()))
                            {
                                // Field value is not detached or being detached so unload it
                                String fieldName = cmd.getMetaDataForManagedMemberAtAbsolutePosition(i).getName();
                                if (NucleusLogger.PERSISTENCE.isDebugEnabled())
                                {
                                    NucleusLogger.PERSISTENCE.debug(LOCALISER.msg("026032", 
                                        StringUtils.toJVMIDString(myPC), myOM.getIdentityAsString(myID), fieldName));
                                }
                                unloadField(fieldName);
                            }
                        }
                        // TODO What if we have collection/map that includes some objects that are not detached?
                        // Currently we just leave as persistent etc but should we????
                        // The problem is that with 1-N bidir fields we could unload the field incorrectly
                    }
                }
            }

            if (detachable)
            {
                // Migrate the lifecycle state to DETACHED_CLEAN
                myLC = myLC.transitionDetach(this);

                // Update the object with its detached state
                myPC.jdoReplaceFlags();
                ((Detachable)myPC).jdoReplaceDetachedState();

                // Call any "post-detach" listeners
                getCallbackHandler().postDetach(myPC, myPC); // there is no copy, so give the same object

                PersistenceCapable toCheckPC = myPC;
                Object toCheckID = myID;
                disconnect();

                if (!toCheckPC.jdoIsDetached())
                {
                    // Sanity check on the objects detached state
                    throw new NucleusUserException(LOCALISER.msg("026025", toCheckPC.getClass().getName(), toCheckID));
                }
            }
            else
            {
                // Warn the user since they selected detachAllOnCommit
                NucleusLogger.PERSISTENCE.warn(LOCALISER.msg("026031", myPC.getClass().getName(), 
                    myOM.getIdentityAsString(myID)));

                // Make the object transient
                makeTransient(null);
            }
        }
        finally
        {
            flags &= ~FLAG_DETACHING;
        }
    }

    /**
     * Method to make detached copy of this instance
     * If the object is detachable then the copy will be migrated to DETACHED state, otherwise will migrate
     * the copy to TRANSIENT. Used by "ObjectManager.detachObjectCopy()".
     * @param state State for the detachment process
     * @return the detached PersistenceCapable instance
     */
    public Object detachCopy(FetchPlanState state)
    {
        if (myLC.isDeleted())
        {
            throw new NucleusUserException(
                LOCALISER.msg("026023", myPC.getClass().getName(), myID));
        }
        if (myOM.getApiAdapter().isDetached(myPC))
        {
            throw new NucleusUserException(
                LOCALISER.msg("026024", myPC.getClass().getName(), myID));
        }
        if (dirty)
        {
            myOM.flushInternal(false);
        }
        if (((flags&FLAG_DETACHING)!=0))
        {
            // Object in the process of detaching (recursive) so return the object which will be the detached object
            return referencedPC;
        }

        // Look for an existing detached copy
        DetachState detachState = (DetachState) state;
        DetachState.Entry existingDetached = detachState.getDetachedCopyEntry(myPC);

        PersistenceCapable detachedPC;
        if (existingDetached == null)
        {
            // No existing detached copy - create new one
            detachedPC = myPC.jdoNewInstance(this);
            detachState.setDetachedCopyEntry(myPC, detachedPC);
        }
        else
        {
            // Found one - if it's sufficient for current FetchPlanState, return it immediately
            detachedPC = (PersistenceCapable) existingDetached.getDetachedCopyObject();
            if (existingDetached.checkCurrentState())
            {
                return detachedPC;
            }

            // Need to process the detached copy using current FetchPlanState
        }

        referencedPC = detachedPC;

        // Check if detachable ... if so then we detach a copy, otherwise we return a transient copy
        boolean detachable = myOM.getApiAdapter().isDetachable(myPC);

        // make sure a detaching PC is not read by another thread while we are detaching
        synchronized (referencedPC)
        {
            if (detachable)
            {
                if (NucleusLogger.PERSISTENCE.isDebugEnabled())
                {
                    NucleusLogger.PERSISTENCE.debug(LOCALISER.msg("010010", StringUtils.toJVMIDString(myPC), 
                        "" + state.getCurrentFetchDepth(), StringUtils.toJVMIDString(detachedPC)));
                }

                // Call any "pre-detach" listeners
                getCallbackHandler().preDetach(myPC);
            }
            try
            {
                flags |= FLAG_DETACHING;

                // Handle any field loading/unloading before the detach
                if ((myOM.getFetchPlan().getDetachmentOptions() & FetchPlan.DETACH_LOAD_FIELDS) != 0)
                {
                    // Load any unloaded fetch-plan fields
                    loadUnloadedFieldsInFetchPlan();
                }

                if (myLC == myOM.getNucleusContext().getApiAdapter().getLifeCycleState(LifeCycleState.HOLLOW) ||
                    myLC == myOM.getNucleusContext().getApiAdapter().getLifeCycleState(LifeCycleState.P_NONTRANS))
                {
                    // Migrate any HOLLOW/P_NONTRANS to P_CLEAN etc
                    myLC = myLC.transitionReadField(this, true);
                }

                // Create a SM for our copy object
                JDOStateManagerImpl smDetachedPC = new JDOStateManagerImpl(myOM, cmd);
                smDetachedPC.initialiseForDetached(detachedPC, getExternalObjectId(myPC), getVersion(myPC));
                smDetachedPC.referencedPC = myPC;

                // If detached copy already existed, take note of fields previously loaded
                if (existingDetached != null)
                {
                    smDetachedPC.retrieveDetachState(smDetachedPC);
                }

                smDetachedPC.replaceFields(getFieldsNumbersToDetach(), new DetachFieldManager(this, 
                    getSecondClassMutableFields(), myFP, state, true));

                smDetachedPC.referencedPC = null;
                if (detachable)
                {
                    // Update the object with its detached state - not to be confused with the "state" object above
                    detachedPC.jdoReplaceFlags();
                    ((Detachable)detachedPC).jdoReplaceDetachedState();
                }
                else
                {
                    smDetachedPC.makeTransient(null);
                }

                // Remove its StateManager since now detached or transient
                replaceStateManager(detachedPC, null);
            }
            catch (Exception e)
            {
                // What could possible be thrown here ?
                NucleusLogger.PERSISTENCE.debug("DETACH ERROR : Error thrown while detaching " +
                    StringUtils.toJVMIDString(myPC) + " (id=" + myID + ")", e);
            }
            finally
            {
                flags &= ~FLAG_DETACHING;
                referencedPC = null;
            }

            if (detachable && !myOM.getApiAdapter().isDetached(detachedPC))
            {
                // Sanity check on the objects detached state
                throw new NucleusUserException(LOCALISER.msg("026025", detachedPC.getClass().getName(), myID));
            }

            if (detachable)
            {
                // Call any "post-detach" listeners
                getCallbackHandler().postDetach(myPC, detachedPC);
            }
        }
        return detachedPC;
    }

    /**
     * Return an array of field numbers that must be included in the detached object
     * @return the field numbers array
     */
    private int[] getFieldsNumbersToDetach()
    {
        // This will cause the detach of any other fields in the FetchPlan.
        int[] fieldsToDetach = myFP.getMemberNumbers();
        if ((myOM.getFetchPlan().getDetachmentOptions() & FetchPlan.DETACH_UNLOAD_FIELDS) == 0)
        {
            // Detach fetch-plan fields plus any other loaded fields
            int[] allFieldNumbers = getAllFieldNumbers();
            int[] loadedFieldNumbers = getFlagsSetTo(loadedFields, allFieldNumbers, true);
            if (loadedFieldNumbers != null && loadedFieldNumbers.length > 0)
            {
                boolean[] flds = new boolean[allFieldNumbers.length];
                for (int i=0;i<fieldsToDetach.length;i++)
                {
                    flds[fieldsToDetach[i]] = true;
                }
                for (int i=0;i<loadedFieldNumbers.length;i++)
                {
                    flds[loadedFieldNumbers[i]] = true;
                }
                fieldsToDetach = getFlagsSetTo(flds,true);
            }
        }
        return fieldsToDetach;
    }

    /**
     * Accessor for the referenced PC object when we are attaching or detaching.
     * When attaching and this is the detached object this returns the newly attached object.
     * When attaching and this is the newly attached object this returns the detached object.
     * When detaching and this is the newly detached object this returns the attached object.
     * When detaching and this is the attached object this returns the newly detached object.
     * @return The referenced object (or null).
     */
    public Object getReferencedPC()
    {
        return referencedPC;
    }

    /* (non-Javadoc)
     * @see org.datanucleus.state.StateManager#attach(java.lang.Object)
     */
    public void attach(Object trans)
    {
        if (((flags&FLAG_ATTACHING)!=0))
        {
            return;
        }

        flags |= FLAG_ATTACHING;
        try
        {
            // Call any "pre-attach" listeners
            getCallbackHandler().preAttach(myPC);

            // Connect the transient object to a StateManager so we can get its values
            JDOStateManagerImpl detachedSM = new JDOStateManagerImpl(myOM, cmd);
            detachedSM.initialiseForDetached(trans, myID, null);

            // Make sure the attached object is in the cache
            myOM.putObjectIntoCache(this);

            int[] nonPKFieldNumbers = getNonPrimaryKeyFieldNumbers();
            if (nonPKFieldNumbers != null && nonPKFieldNumbers.length > 0)
            {
                // Attach the (non-PK) fields from the transient
                detachedSM.provideFields(nonPKFieldNumbers,
                    new AttachFieldManager(this, getSecondClassMutableFields(), getNonPrimaryKeyFields(),
                        true, true, false));
            }

            // Disconnect the transient object
            replaceStateManager((PersistenceCapable) trans, null);

            // Call any "post-attach" listeners
            getCallbackHandler().postAttach(myPC, myPC);
        }
        finally
        {
            flags &= ~FLAG_ATTACHING;
        }
    }

    /**
     * Method to attach the object managed by this StateManager.
     * @param embedded Whether it is embedded
     */
    public void attach(boolean embedded)
    {
        if (((flags&FLAG_ATTACHING)!=0))
        {
            return;
        }

        flags |= FLAG_ATTACHING;
        try
        {
            // Check if the object is already persisted
            boolean persistent = false;
            if (embedded)
            {
                persistent = true;
            }
            else
            {
                if (!myOM.getNucleusContext().getPersistenceConfiguration().getBooleanProperty("datanucleus.attachSameDatastore"))
                {
                    // We cant assume that this object was detached from this datastore so we check it
                    try
                    {
                        locate();
                        persistent = true;
                    }
                    catch (NucleusObjectNotFoundException onfe)
                    {
                        // Not currently present!
                    }
                }
                else
                {
                    // Assumed detached from this datastore
                    persistent = true;
                }
            }

            // Call any "pre-attach" listeners
            getCallbackHandler().preAttach(myPC);

            // Retrieve the updated values from the detached object
            replaceStateManager(myPC, this);
            retrieveDetachState(this);

            if (!persistent)
            {
                // Persist the object into this datastore first
                makePersistent();
            }

            // Migrate the lifecycle state to persistent
            myLC = myLC.transitionAttach(this);

            // Make sure the attached object goes in the cache
            // [would not get cached when not changed if we didnt do this here]
            myOM.putObjectIntoCache(this);

            int[] attachFieldNumbers = getFieldNumbersOfLoadedOrDirtyFields(loadedFields, dirtyFields);
            if (attachFieldNumbers != null)
            {
                // Only update the fields that were detached, and only update them if there are any to update
                provideFields(attachFieldNumbers,
                    new AttachFieldManager(this, getSecondClassMutableFields(), dirtyFields,
                        persistent, true, false));
            }

            // Call any "post-attach" listeners
            getCallbackHandler().postAttach(myPC, myPC);
        }
        finally
        {
            flags &= ~FLAG_ATTACHING;
        }
    }

    /**
     * Method to attach a copy of the detached persistable instance and return the (attached) copy.
     * @param obj the detached persistable instance to be attached
     * @param embedded Whether the object is stored embedded/serialised in another object
     * @return The attached copy
     */
    public Object attachCopy(Object obj, boolean embedded)
    {
        if (((flags&FLAG_ATTACHING)!=0))
        {
            return myPC;
        }
        flags |= FLAG_ATTACHING;

        PersistenceCapable detachedPC = (PersistenceCapable)obj;
        try
        {
            // Check if the object is already persisted
            boolean persistent = false;
            if (embedded)
            {
                persistent = true;
            }
            else
            {
                if (!myOM.getNucleusContext().getPersistenceConfiguration().getBooleanProperty("datanucleus.attachSameDatastore"))
                {
                    // We cant assume that this object was detached from this datastore so we check it
                    try
                    {
                        locate();
                        persistent = true;
                    }
                    catch (NucleusObjectNotFoundException onfe)
                    {
                        // Not currently present!
                    }
                }
                else
                {
                    // Assumed detached from this datastore
                    persistent = true;
                }
            }

            // Call any "pre-attach" listeners
            getCallbackHandler().preAttach(detachedPC);

            if (myOM.getApiAdapter().isDeleted(detachedPC))
            {
                // The detached object has been deleted
                myLC = myLC.transitionDeletePersistent(this);
            }

            if (!myOM.getTransaction().getOptimistic() &&
                (myLC == myOM.getApiAdapter().getLifeCycleState(LifeCycleState.HOLLOW) ||
                 myLC == myOM.getApiAdapter().getLifeCycleState(LifeCycleState.P_NONTRANS)))
            {
                // Pessimistic txns and in HOLLOW/P_NONTRANS, so move to P_CLEAN
                // TODO Move this into the lifecycle state classes as a "transitionAttach"
                myLC = myLC.transitionMakeTransactional(this, persistent);
            }

            if (persistent)
            {
                // Make sure that all non-container SCO fields are loaded so we can make valid dirty checks
                // for whether these fields have been updated whilst detached. The detached object doesnt know if the contents
                // have been changed.
                loadSCONonContainerFields();
            }

            // Add a state manager to the detached PC so that we can retrieve its detached state
            JDOStateManagerImpl smDetachedPC = new JDOStateManagerImpl(myOM, cmd);
            smDetachedPC.initialiseForDetached(detachedPC, getExternalObjectId(detachedPC), null);

            // Cross-reference the attached and detached objects for the attach process
            smDetachedPC.referencedPC = myPC;
            this.referencedPC = detachedPC;

            // Retrieve the updated values from the detached object
            retrieveDetachState(smDetachedPC);

            if (!persistent)
            {
                // Object is not yet persisted! so make it persistent

                // Make sure all field values in the attach object are ready for inserts (but dont trigger any cascade attaches)
                internalAttachCopy(this, smDetachedPC, smDetachedPC.loadedFields, smDetachedPC.dirtyFields, persistent, 
                    smDetachedPC.myVersion, false);

                makePersistent();
            }

            // Go through all related fields and attach them (including relationships)
            internalAttachCopy(this, smDetachedPC, smDetachedPC.loadedFields, smDetachedPC.dirtyFields, persistent, 
                smDetachedPC.myVersion, true);

            // Remove the state manager from the detached PC
            replaceStateManager(detachedPC, null);

            // Remove the corss-referencing now we have finished the attach process
            smDetachedPC.referencedPC = null;
            this.referencedPC = null;

            // Call any "post-attach" listeners
            getCallbackHandler().postAttach(myPC,detachedPC);
        }
        catch (NucleusException ne)
        {
            // Log any errors in the attach
            NucleusLogger.PERSISTENCE.debug("Unexpected exception thrown in attach", ne);
            throw ne;
        }
        finally
        {
            flags &= ~FLAG_ATTACHING;
        }
        return myPC;
    }

    /**
     * Attach the fields of a persistent object.
     * @param sm StateManager for the attached object.
     * @param smDetached StateManager for the detached object.
     * @param loadedFields Fields that were detached with the object
     * @param dirtyFields Fields that have been modified while detached
     * @param persistent whether the object is already persistent
     * @param version the version
     * @param cascade Whether to cascade the attach to related fields
     */
    private void internalAttachCopy(org.datanucleus.state.StateManager sm,
                                   org.datanucleus.state.StateManager smDetached,
                                   boolean[] loadedFields,
                                   boolean[] dirtyFields,
                                   boolean persistent,
                                   Object version,
                                   boolean cascade)
    {
        // Need to take all loaded fields plus all modified fields
        // (maybe some werent detached but have been modified) and attach them
        int[] attachFieldNumbers = getFieldNumbersOfLoadedOrDirtyFields(loadedFields, dirtyFields);
        sm.setVersion(version);
        if (attachFieldNumbers != null)
        {
            // Attach all dirty fields, and load other loaded fields
            smDetached.provideFields(attachFieldNumbers,
                new AttachFieldManager(sm, getSecondClassMutableFields(), dirtyFields, persistent, cascade, true));
        }
    }

    /**
     * Convenience accessor to return the field numbers for the input loaded and dirty field arrays.
     * @param loadedFields Fields that were detached with the object
     * @param dirtyFields Fields that have been modified while detached
     */
    private int[] getFieldNumbersOfLoadedOrDirtyFields(boolean[] loadedFields, boolean[] dirtyFields)
    {
        // Find the number of fields that are loaded or dirty
        int numFields = 0;
        for (int i=0;i<loadedFields.length;i++)
        {
            if (loadedFields[i] || dirtyFields[i])
            {
                numFields++;
            }
        }

        int[] fieldNumbers = new int[numFields];
        int n=0;
        for (int i=0;i<loadedFields.length;i++)
        {
            if (loadedFields[i] || dirtyFields[i])
            {
                fieldNumbers[n++] = getAllFieldNumbers()[i];
            }
        }
        return fieldNumbers;
    }

    /**
     * Method to delete the object from persistence.
     */
    public void deletePersistent()
    {
        if (!myLC.isDeleted())
        {
            if (myOM.isDelayDatastoreOperationsEnabled())
            {
                // Optimistic transactions, with all updates delayed til flush/commit

                // Call any lifecycle listeners waiting for this event
                getCallbackHandler().preDelete(myPC);

                // Delay deletion until flush/commit so run reachability now to tag all reachable instances as necessary
                myOM.markDirty(this, false);

                // Reachability
                if (myLC.stateType() == LifeCycleState.P_CLEAN || 
                    myLC.stateType() == LifeCycleState.P_DIRTY || 
                    myLC.stateType() == LifeCycleState.HOLLOW ||
                    myLC.stateType() == LifeCycleState.P_NONTRANS ||
                    myLC.stateType() == LifeCycleState.P_NONTRANS_DIRTY)
                {
                    // Make sure all fields are loaded so we can perform reachability
                    loadUnloadedFields();
                }
                flags |= FLAG_BECOMING_DELETED;
                // TODO Only run this if we have relations from this class
                provideFields(getAllFieldNumbers(), new DeleteFieldManager(this));

                // Update lifecycle state (after running reachability since it will unload all fields)
                dirty = true;
                preStateChange();
                try
                {
                    // Keep "loadedFields" settings til after delete is complete to save reloading
                    preDeleteLoadedFields = new boolean[loadedFields.length];
                    for (int i=0;i<preDeleteLoadedFields.length;i++)
                    {
                        preDeleteLoadedFields[i] = loadedFields[i];
                    }

                    myLC = myLC.transitionDeletePersistent(this);
                }
                finally
                {
                    flags &= ~FLAG_BECOMING_DELETED;
                    postStateChange();
                }
            }
            else
            {
                // Datastore transactions, with all updates processed now

                // Call any lifecycle listeners waiting for this event.
                getCallbackHandler().preDelete(myPC);

                // Update lifecycle state
                dirty = true;
                preStateChange();
                try
                {
                    // Keep "loadedFields" settings til after delete is complete to save reloading
                    preDeleteLoadedFields = new boolean[loadedFields.length];
                    for (int i=0;i<preDeleteLoadedFields.length;i++)
                    {
                        preDeleteLoadedFields[i] = loadedFields[i];
                    }

                    myLC = myLC.transitionDeletePersistent(this);
                }
                finally
                {
                    postStateChange();
                }

                // Delete the object from the datastore (includes reachability)
                internalDeletePersistent();

                // Call any lifecycle listeners waiting for this event.
                getCallbackHandler().postDelete(myPC);
            }
        }
    }

    /**
     * Whether this object is moving to a deleted state.
     * @return Whether the object will be moved into a deleted state during this operation
     */
    public boolean becomingDeleted()
    {
        return (flags&FLAG_BECOMING_DELETED)>0;
    }

    /** Copy of the "loadedFields" just before delete was started to avoid reload during delete */
    boolean[] preDeleteLoadedFields = null;

    /**
     * Method to delete the object from the datastore.
     */
    private void internalDeletePersistent()
    {
        if (isDeleting())
        {
            throw new NucleusUserException(LOCALISER.msg("026008"));
        }

        activity = ActivityState.DELETING;
        try
        {
            if (dirty)
            {
                clearDirtyFlags();

                // Clear the PM's knowledge of our being dirty. This calls flush() which does nothing
                myOM.flushInternal(false);
            }

            getStoreManager().getPersistenceHandler().deleteObject(this);

            preDeleteLoadedFields = null;
        }
        finally
        {
            activity = ActivityState.NONE;
        }
    }

    /**
     * Locate the object in the datastore.
     * @throws NucleusObjectNotFoundException if the object doesnt exist.
     */
    public void locate()
    {
        // Validate the object existence
        getStoreManager().getPersistenceHandler().locateObject(this);
    }


    /**
     * Nullify fields with reference to PersistenceCapable or SCO instances 
     */
    public void nullifyFields()
    {
        if (!myLC.isDeleted() && !myOM.getApiAdapter().isDetached(myPC))
        {
            // Update any relationships for fields of this object that aren't dependent
            replaceFields(getNonPrimaryKeyFieldNumbers(), new NullifyRelationFieldManager(this));
            flush();
        }
    }

    /* (non-Javadoc)
     * @see org.datanucleus.StateManager#markForValidation()
     */
    public void markForInheritanceValidation()
    {
        flags |= FLAG_NEED_INHERITANCE_VALIDATION;
    }

    /**
     * Validates whether the persistence capable instance exists in the datastore.
     * If the instance doesn't exist in the datastore, this method will fail raising a 
     * NucleusObjectNotFoundException. If the object is transactional then does nothing.
     * If the object has unloaded (non-SCO, non-PK) fetch plan fields then fetches them.
     * Else it checks the existence of the object in the datastore.
     */
    public void validate()
    {
        if (!myLC.isTransactional())
        {
            // Find all FetchPlan fields that are not PK, not SCO and still not loaded
            int[] fieldNumbers = getFlagsSetTo(loadedFields, myFP.getMemberNumbers(), false);
            if (fieldNumbers != null && fieldNumbers.length > 0)
            {
                fieldNumbers = getFlagsSetTo(getNonPrimaryKeyFields(), fieldNumbers, true);
            }
            if (fieldNumbers != null && fieldNumbers.length > 0)
            {
                fieldNumbers = getFlagsSetTo(getSecondClassMutableFields(), fieldNumbers, false);
            }

            boolean versionNeedsLoading = false;
            if (cmd.isVersioned() && transactionalVersion == null)
            {
                versionNeedsLoading = true;
            }
            if ((fieldNumbers != null && fieldNumbers.length > 0) || versionNeedsLoading)
            {
                // Some fetch plan fields, or the version are not loaded so try to load them, and this by itself 
                // validates the existence. Loads the fields in the current FetchPlan (JDO2 spec 12.6.5)
                transitionReadField(false);

                fieldNumbers = myFP.getMemberNumbers();
                if (fieldNumbers != null || versionNeedsLoading)
                {
                    boolean callPostLoad = myFP.isToCallPostLoadFetchPlan(this.loadedFields);
                    setTransactionalVersion(null); // Make sure we get the latest version
                    loadFieldsFromDatastore(fieldNumbers);
                    if (callPostLoad)
                    {
                        postLoad();
                    }
                }
            }
            else
            {
                // Validate the object existence
                locate();
                transitionReadField(false);
            }
        }
    }

    // --------------------------- Process Methods -----------------------------

    /**
     * Method called before a change in state.
     */
    protected void preStateChange()
    {
        flags |= FLAG_CHANGING_STATE;
    }

    /**
     * Method called after a change in state.
     */
    protected void postStateChange()
    {
        flags &= ~FLAG_CHANGING_STATE;
        if ((flags&FLAG_POSTLOAD_PENDING)>0 && isFetchPlanLoaded())
        {
            // Only call postLoad when all FetchPlan fields are loaded
            flags &= ~FLAG_POSTLOAD_PENDING;
            postLoad();
        }
    }

    /**
     * Method called before a write of the specified field.
     * @param field The field to write
     * @return true if the field was already dirty before
     */
    protected boolean preWriteField(int field)
    {
        boolean wasDirty = dirty;
        /*
         * If we're writing a field in the process of inserting it must be due 
         * to jdoPreStore().  We haven't actually done the INSERT yet so we 
         * don't want to mark anything as dirty, which would make us want to do 
         * an UPDATE later. 
         */
        if (activity != ActivityState.INSERTING && activity != ActivityState.INSERTING_CALLBACKS)
        {
            //TODO dirty already??? this is not correct, only gets dirty after state transition
//            dirty = true;
            if (!wasDirty) // (only do it for first dirty event).
            {
                // Call any lifecycle listeners waiting for this event
                getCallbackHandler().preDirty(myPC);
            }

            transitionWriteField();

            dirty = true;
            dirtyFields[field] = true;
            loadedFields[field] = true;
        }
        return wasDirty;
    }

    /**
     * Method called after the write of a field.
     * @param wasDirty whether before writing this field the pc was dirty
     */
    protected void postWriteField(boolean wasDirty)
    {
        if (dirty && !wasDirty) // (only do it for first dirty event).
        {
            // Call any lifecycle listeners waiting for this event
            getCallbackHandler().postDirty(myPC);
        }

        if (activity == ActivityState.NONE && ((flags&FLAG_FLUSHING)==0) && 
            !(myLC.isTransactional() && !myLC.isPersistent()))
        {
            if (((flags&FLAG_DETACHING)!=0) && referencedPC == null)
            {
                // detachAllOnCommit caused a field to be dirty so ignore it
                return;
            }
            else
            {
                // Not during flush, and not transactional-transient, and not inserting - so mark as dirty
                myOM.markDirty(this, true);
            }
        }
    }

    /**
     * Called whenever the default fetch group fields have all been loaded.
     * Updates jdoFlags and calls jdoPostLoad() as appropriate.
     * <p>
     * If it's called in the midst of a life-cycle transition both actions will
     * be deferred until the transition is complete.
     * <em>This deferral is important</em>. Without it, we could enter user
     * code (jdoPostLoad()) while still making a state transition, and that way
     * lies madness.
     * <p>
     * As an example, consider a jdoPostLoad() that calls other enhanced methods
     * that read fields (jdoPostLoad() itself is not enhanced). A P_NONTRANS
     * object accessed within a transaction would produce the following infinite
     * loop:
     * <p>
     * <blockquote>
     * 
     * <pre>
     * 
     *  isLoaded()
     *  transitionReadField()
     *  refreshLoadedFields()
     *  jdoPostLoad()
     *  isLoaded()
     *  ...
     *  
     * </pre>
     * 
     * </blockquote>
     * <p>
     * because the transition from P_NONTRANS to P_CLEAN can never be completed.
     */
    private void postLoad()
    {
        if ((flags&FLAG_CHANGING_STATE)>0)
        {
            flags |= FLAG_POSTLOAD_PENDING;
        }
        else
        {
            /*
             * A transactional object whose DFG fields are loaded does not need
             * to contact us in order to read those fields, so we can safely set
             * READ_OK.
             *
             * A non-transactional object needs to notify us on all field reads
             * so that we can decide whether or not any transition should occur,
             * so we leave the flags at LOAD_REQUIRED.
             */
            if (jdoDfgFlags == PersistenceCapable.LOAD_REQUIRED && myLC.isTransactional())
            {
                jdoDfgFlags = PersistenceCapable.READ_OK;
                myPC.jdoReplaceFlags();
            }

            getCallbackHandler().postLoad(myPC);
        }
    }

    /**
     * Method to set the storing PC flag.
     */
    public void setStoringPC()
    {
        flags |= FLAG_STORING_PC;
    }

    /**
     * Method to unset the storing PC flag.
     */
    public void unsetStoringPC()
    {
        flags &= ~FLAG_STORING_PC;
    }

    /**
     * Guarantee that the serializable transactional and persistent fields are loaded into the instance. 
     * This method is called by the generated jdoPreSerialize method prior to serialization of the instance.
     * @param pc the calling PersistenceCapable instance
     */
    public void preSerialize(PersistenceCapable pc)
    {
        if (disconnectClone(pc))
        {
            return;
        }

        // Retrieve all fields prior to serialisation
        retrieve(false);

        myLC = myLC.transitionSerialize(this);

        if ((flags&FLAG_STORING_PC)==0 && pc instanceof Detachable)
        {
            if (!myLC.isDeleted() && myLC.isPersistent())
            {
                if (myLC.isDirty())
                {
                    flush();
                }

                // Normal PC Detachable object being serialised so load up the detached state into the instance
                // JDO2 spec "For Detachable classes, the jdoPreSerialize method must also initialize the jdoDetachedState
                // instance so that the detached state is serialized along with the instance."
                ((Detachable)pc).jdoReplaceDetachedState();
            }
        }
    }

    /* (non-Javadoc)
     * @see org.datanucleus.store.ObjectProvider#setFlushing(boolean)
     */
    public void setFlushing(boolean flushing)
    {
        if (flushing)
        {
            flags |= FLAG_FLUSHING;
        }
        else
        {
            flags &= ~FLAG_FLUSHING;
        }
    }

    protected boolean isFlushing()
    {
        return (flags&FLAG_FLUSHING)!=0;
    }

    /**
     * Flushes any outstanding changes to the object to the datastore. 
     * This will process :-
     * <ul>
     * <li>Any objects that have been marked as provisionally persistent yet havent been flushed to the 
     * datastore.</li>
     * <li>Any objects that have been marked as provisionally deleted yet havent been flushed to the 
     * datastore.</li>
     * <li>Any fields that have been updated.</li>
     * </ul>
     */
    public void flush()
    {
        if (dirty)
        {
            if (isFlushing())
            {
                // In the case of persisting a new object using autoincrement id within an optimistic
                // transaction, flush() will initially be called at the point of recognising that the
                // id is generated in the datastore, and will then be called again at the point of doing
                // the InsertRequest for the object itself. Just return since we are flushing right now
                return;
            }
            if (activity == ActivityState.INSERTING || activity == ActivityState.INSERTING_CALLBACKS)
            {
                return;
            }

            setFlushing(true);
            try
            {
                if (myLC.stateType() == LifeCycleState.P_NEW && !isFlushedNew())
                {
                    // Newly persisted object but not yet flushed to datastore (e.g optimistic transactions)
                    if (!isEmbedded())
                    {
                        // internalMakePersistent does preStore, postStore
                        internalMakePersistent();
                    }
                    else
                    {
                        getCallbackHandler().preStore(myPC);
                        if (myID == null)
                        {
                            setIdentity(true); // Just in case user is setting it in preStore
                        }

                        getCallbackHandler().postStore(myPC);
                    }
                    dirty = false;
                }
                else if (myLC.stateType() == LifeCycleState.P_DELETED)
                {
                    // Object marked as deleted but not yet deleted from datastore
                    getCallbackHandler().preDelete(myPC);
                    if (!isEmbedded())
                    {
                        internalDeletePersistent();
                    }
                    getCallbackHandler().postDelete(myPC);
                }
                else if (myLC.stateType() == LifeCycleState.P_NEW_DELETED)
                {
                    // Newly persisted object marked as deleted but not yet deleted from datastore
                    if (isFlushedNew())
                    {
                        // Only delete it if it was actually persisted into the datastore
                        getCallbackHandler().preDelete(myPC);
                        if (!isEmbedded())
                        {
                            internalDeletePersistent();
                        }
                        setFlushedNew(false); // No longer newly persisted flushed object since has been deleted
                        getCallbackHandler().postDelete(myPC);
                    }
                    else
                    {
                        // Was never persisted to the datastore so nothing to do
                        dirty = false;
                    }
                }
                else
                {
                    // Updated object with changes to flush to datastore
                    if (!isDeleting())
                    {
                        getCallbackHandler().preStore(myPC);
                        if (myID == null)
                        {
                            setIdentity(true); // Just in case user is setting it in preStore
                        }
                    }

                    int[] dirtyFieldNumbers = getFlagsSetTo(dirtyFields, true);
                    if (dirtyFieldNumbers == null)
                    {
                        throw new NucleusException(LOCALISER.msg("026010")).setFatal();
                    }
                    if (!isEmbedded())
                    {
                        getStoreManager().getPersistenceHandler().updateObject(this, dirtyFieldNumbers);

                        // Update the object in the cache(s)
                        myOM.putObjectIntoCache(this);
                    }

                    clearDirtyFlags();

                    getCallbackHandler().postStore(myPC);
                }
            }
            finally
            {
                setFlushing(false);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.datanucleus.store.ObjectProvider#isFlushedNew()
     */
    public boolean isFlushedNew()
    {
        return (flags&FLAG_FLUSHED_NEW)!=0;
    }

    /* (non-Javadoc)
     * @see org.datanucleus.store.ObjectProvider#setFlushedNew(boolean)
     */
    public void setFlushedNew(boolean flag)
    {
        if (flag)
        {
            flags |= FLAG_FLUSHED_NEW;
        }
        else
        {
            flags &= ~FLAG_FLUSHED_NEW;
        }
    }

    /* (non-Javadoc)
     * @see org.datanucleus.store.ObjectProvider#markAsFlushed()
     */
    public void markAsFlushed()
    {
        clearDirtyFlags();
    }

    /* (non-Javadoc)
     * @see org.datanucleus.StateManager#flushDirtyFields()
     */
    public void flushDirtyFields()
    {
        int[] dirtyFieldNumbers = getFlagsSetTo(dirtyFields, true);
        if (dirtyFieldNumbers == null)
        {
            return;
        }

        if (!isEmbedded())
        {
            getStoreManager().getPersistenceHandler().updateObject(this, dirtyFieldNumbers);

            // Update the object in the cache(s)
            myOM.putObjectIntoCache(this);
        }

        clearFlags(dirtyFields);
        if (myLC.stateType() == LifeCycleState.P_DELETED)
        {
            // Leave as dirty since we are deleting it
            dirty = true;
        }
    }

    /**
     * Initialize SM reference in PC and Oid
     * @param newState The new StateManager state 
     */
    private void initializeSM(int newState)
    {
        final JDOStateManagerImpl thisSM = this;
        myLC = myOM.getNucleusContext().getApiAdapter().getLifeCycleState(newState);

        try
        {
            if (myLC.isPersistent())
            {
                myOM.addStateManager(this);
            }

            // Everything OK so far. Now we can set SM reference in PC 
            // It can be done only after myLC is set to deligate validation
            // to the LC and objectId verified for uniqueness
            replaceStateManager(myPC, thisSM);
        }
        catch (SecurityException e)
        {
            throw new NucleusUserException(e.getMessage());
        }
        catch (NucleusException ne)
        {
            if (myOM.getStateManagerById(myID) == this)
            {
                myOM.removeStateManager(this);
            }
            throw ne;
        }
    }

    /**
     * Convenience method to unset the owners of all SCO fields in the PC object.
     */
    private void unsetOwnerInSCOFields()
    {
        // Call unsetOwner() on all loaded SCO fields.
        int[] fieldNumbers = getFlagsSetTo(loadedFields, getSecondClassMutableFieldNumbers(), true);
        if (fieldNumbers != null && fieldNumbers.length > 0)
        {
            provideFields(fieldNumbers, new UnsetOwners());
        }        
    }

    /**
     * Disconnect the StateManager from the PersistenceManager and PC object.
     */
    public void disconnect()
    {
        if (NucleusLogger.PERSISTENCE.isDebugEnabled())
        {
            NucleusLogger.PERSISTENCE.debug(LOCALISER.msg("026011", StringUtils.toJVMIDString(myPC), this));
        }

        //we are transitioning to TRANSIENT state, so if any postLoad
        //action is pending we do it before. This usually happens when
        //we make transient instances using the fetch plan and some
        //fields were loaded during this action which triggered a jdoPostLoad event
        if ((flags&FLAG_POSTLOAD_PENDING)>0)
        {
            flags &= ~FLAG_CHANGING_STATE; //hack to make sure postLoad does not return without processing
            flags &= ~FLAG_POSTLOAD_PENDING;
            postLoad();
        }
        
        unsetOwnerInSCOFields();

        myOM.removeStateManager(this);
        jdoDfgFlags = PersistenceCapable.READ_WRITE_OK;
        myPC.jdoReplaceFlags();

        flags |= FLAG_DISCONNECTING;
        try
        {
            replaceStateManager(myPC, null);
        }
        finally
        {
            flags &= ~FLAG_DISCONNECTING;
        }

        if (associatedValuesMap != null)
        {
            associatedValuesMap.clear();
            associatedValuesMap = null;
        }
        clearSavedFields();
        myOM = null;
        myFP = null;
        myPC = null;
        myID = null;
        myLC = null;
        cmd = null;
    }

    /**
     * Registers the pc class in the cache
     */
    public void registerTransactional()
    {
        myOM.addStateManager(this);
    }

    // ------------------------------ Helper Methods ---------------------------

    /**
     * Method to dump a PersistenceCapable object to the specified PrintWriter.
     * @param pc The PersistenceCapable object
     * @param out The PrintWriter
     */
    private static void dumpPC(PersistenceCapable pc, PrintWriter out)
    {
        out.println(StringUtils.toJVMIDString(pc));

        if (pc == null)
        {
            return;
        }

        out.print("jdoStateManager = " + peekField(pc, "jdoStateManager"));
        out.print("jdoFlags = ");
        Object flagsObj = peekField(pc, "jdoFlags");
        if (flagsObj instanceof Byte)
        {
            out.println(jdoFlagsToString(((Byte) flagsObj).byteValue()));
        }
        else
        {
            out.println(flagsObj);
        }

        Class c = pc.getClass();
        do
        {
            String[] fieldNames = HELPER.getFieldNames(c);
            for (int i = 0; i < fieldNames.length; ++i)
            {
                out.print(fieldNames[i]);
                out.print(" = ");
                out.println(peekField(pc, fieldNames[i]));
            }
            c = c.getSuperclass();
        }
        while (c != null && PersistenceCapable.class.isAssignableFrom(c));
    }

    /**
     * Utility to dump the contents of the StateManager.
     * @param out PrintWriter to dump to
     */
    public void dump(PrintWriter out)
    {
        out.println("myPM = " + myOM);
        out.println("myID = " + myID);
        out.println("myLC = " + myLC);
        out.println("cmd = " + cmd);
        out.println("srm = " + getStoreManager());
        out.println("fieldCount = " + getHighestFieldNumber());
        out.println("dirty = " + dirty);
        out.println("flushing = " + isFlushing());
        out.println("changingState = " + ((flags&FLAG_CHANGING_STATE)!=0));
        out.println("postLoadPending = " + ((flags&FLAG_POSTLOAD_PENDING)!=0));
        out.println("disconnecting = " + ((flags&FLAG_DISCONNECTING)!=0));
        out.println("dirtyFields = " + StringUtils.booleanArrayToString(dirtyFields));
        out.println("getSecondClassMutableFields() = " + StringUtils.booleanArrayToString(getSecondClassMutableFields()));
        out.println("getAllFieldNumbers() = " + StringUtils.intArrayToString(getAllFieldNumbers()));
        out.println("secondClassMutableFieldNumbers = " + StringUtils.intArrayToString(getSecondClassMutableFieldNumbers()));

        out.println();
        out.println("jdoFlags = " + jdoFlagsToString(jdoDfgFlags));
        out.println("loadedFields = " + StringUtils.booleanArrayToString(loadedFields));
        out.print("myPC = ");
        dumpPC(myPC, out);

        out.println();
        out.println("savedFlags = " + jdoFlagsToString(savedFlags));
        out.println("savedLoadedFields = " + StringUtils.booleanArrayToString(savedLoadedFields));

        out.print("savedImage = ");
        dumpPC(savedImage, out);
    }

    /**
     * Utility to convert JDO specific flags to a String.
     * @param flags The JDO flags
     * @return String version 
     */
    private static String jdoFlagsToString(byte flags)
    {
        switch (flags)
        {
            case PersistenceCapable.LOAD_REQUIRED:
                return "LOAD_REQUIRED";
            case PersistenceCapable.READ_OK:
                return "READ_OK";
            case PersistenceCapable.READ_WRITE_OK:
                return "READ_WRITE_OK";
            default:
                return "???";
        }
    }
}