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
2007 Xuan Baldauf - added the use of srm.findObject() to cater for different object lifecycle management policies (in RDBMS and DB4O databases)
2007 Xuan Baldauf - changes to allow the disabling of clearing of fields when transitioning from PERSISTENT_NEW to TRANSIENT.
2008 Marco Schulze - added reference counting functionality for get/acquireThreadContextInfo()
     ...
 **********************************************************************/
package org.datanucleus;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.datanucleus.api.ApiAdapter;
import org.datanucleus.cache.CachedPC;
import org.datanucleus.cache.Level1Cache;
import org.datanucleus.cache.Level2Cache;
import org.datanucleus.exceptions.ClassNotDetachableException;
import org.datanucleus.exceptions.ClassNotPersistableException;
import org.datanucleus.exceptions.ClassNotResolvedException;
import org.datanucleus.exceptions.CommitStateTransitionException;
import org.datanucleus.exceptions.NoPersistenceInformationException;
import org.datanucleus.exceptions.NucleusException;
import org.datanucleus.exceptions.NucleusFatalUserException;
import org.datanucleus.exceptions.NucleusObjectNotFoundException;
import org.datanucleus.exceptions.NucleusOptimisticException;
import org.datanucleus.exceptions.NucleusUserException;
import org.datanucleus.exceptions.ObjectDetachedException;
import org.datanucleus.exceptions.RollbackStateTransitionException;
import org.datanucleus.exceptions.TransactionActiveOnCloseException;
import org.datanucleus.exceptions.TransactionNotActiveException;
import org.datanucleus.identity.DatastoreUniqueOID;
import org.datanucleus.identity.IdentityKeyTranslator;
import org.datanucleus.identity.IdentityStringTranslator;
import org.datanucleus.identity.OID;
import org.datanucleus.identity.OIDFactory;
import org.datanucleus.identity.SCOID;
import org.datanucleus.metadata.AbstractClassMetaData;
import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.metadata.IdentityType;
import org.datanucleus.metadata.MetaDataManager;
import org.datanucleus.metadata.TransactionType;
import org.datanucleus.properties.BasePropertyStore;
import org.datanucleus.state.CallbackHandler;
import org.datanucleus.state.DetachState;
import org.datanucleus.state.FetchPlanState;
import org.datanucleus.state.LifeCycleState;
import org.datanucleus.state.ObjectProviderFactory;
import org.datanucleus.state.RelationshipManager;
import org.datanucleus.state.RelationshipManagerImpl;
import org.datanucleus.state.StateManager;
import org.datanucleus.state.lock.LockManager;
import org.datanucleus.state.lock.LockManagerImpl;
import org.datanucleus.store.ExecutionContext;
import org.datanucleus.store.Extent;
import org.datanucleus.store.FieldValues;
import org.datanucleus.store.ObjectProvider;
import org.datanucleus.store.PersistenceBatchType;
import org.datanucleus.store.StoreManager;
import org.datanucleus.store.StorePersistenceHandler;
import org.datanucleus.store.Type;
import org.datanucleus.store.query.Query;
import org.datanucleus.store.types.TypeManager;
import org.datanucleus.util.Localiser;
import org.datanucleus.util.NucleusLogger;
import org.datanucleus.util.StringUtils;
import org.datanucleus.util.WeakValueMap;

/**
 * Representation of an ObjectManager.
 * An object manager provides management for the persistence of objects into a datastore
 * and the retrieval of these objects using various methods. 
 * <h3>Caching</h3>
 * <p>
 * An ObjectManager has its own Level 1 cache. This stores objects against their identity. The Level 1 cache
 * is typically a weak referenced map and so cached objects can be garbage collected. Objects are placed in the
 * Level 1 cache during the transaction. 
 * The ObjectManagerFactory also has a Level 2 cache. This is used to allow cross-communication between
 * ObjectManagers. Objects are placed in the Level 2 cache during commit() of a transaction. If an object is
 * deleted during a transaction then it will be removed from the Level 2 cache at commit(). If an object is
 * no longer enlisted in the transaction at commit then it will be removed from the Level 2 cache (so we
 * remove the chance of handing out old data).
 * </p>
 * <h3>Transactions</h3>
 * <p>
 * An ObjectManager has a single transaction (the "current" transaction). The transaction can be
 * "active" (if begin() has been called on it) or "inactive".
 * </p>
 * <h3>Persisted Objects</h3>
 * <p>
 * When an object involved in the current transaction it is <i>enlisted</i> (calling enlistInTransaction()).
 * Its' identity is saved (in "txEnlistedIds") for use later in any "persistenceByReachability" process run at commit.
 * Any object that is passed via makePersistent() will be stored (as an identity) in "txKnownPersistedIds" and objects 
 * persisted due to reachability from these objects will also have their identity stored (in "txFlushedNewIds").
 * All of this information is used in the "persistence-by-reachability-at-commit" process which detects if some objects
 * originally persisted are no longer reachable and hence should not be persistent after all.
 * </p>
 */
public class ObjectManagerImpl implements ObjectManager
{
    /** Localisation utility for output messages */
    protected static final Localiser LOCALISER = Localiser.getInstance("org.datanucleus.Localisation",
        org.datanucleus.ClassConstants.NUCLEUS_CONTEXT_LOADER);

    /** Context for the persistence process. */
    NucleusContext context;

    /** The owning PersistenceManager/EntityManager object. */
    private Object owner;

    /** State variable for whether the ObjectManager is closed. */
    private boolean closed;

    /** Current FetchPlan for the ObjectManager. */
    private FetchPlan fetchPlan;

    /** The ClassLoader resolver to use for class loading issues. */
    private ClassLoaderResolver clr = null;

    /** Callback handler for this ObjectManager. */
    private CallbackHandler callbacks;

    /** Level 1 Cache */
    private Level1Cache cache;

    /** State variable used when searching for the StateManager for an object, representing the object. */
    private Object objectLookingForStateManager = null;

    /** State variable used when searching for the StateManager for an object, representing the StateManager. */
    private StateManager foundStateManager = null;

    /** Current transaction */
    private Transaction tx;

    /** Cache of StateManagers enlisted in the current transaction, keyed by the object id. */
    private Map<Object, StateManager> enlistedSMCache = new WeakValueMap();

    /** List of StateManagers for all current dirty objects managed by this ObjectManager. */
    private List<StateManager> dirtySMs = new ArrayList();

    /** List of StateManagers for all current dirty objects made dirty by reachability. */
    private List<StateManager> indirectDirtySMs = new ArrayList();

    private Set<StateManager> nontxProcessedSMs = null;

    /** Set of ids to be Level2 cached at commit (if using L2 cache). */
    private Set txCachedIds = null;

    /** Properties controlling runtime behaviour (detach on commit, multithreaded, etc). */
    private BasePropertyStore properties = new BasePropertyStore();

    /** State variable for whether the ObjectManager is currently flushing its operations. */
    private boolean flushing = false;

    /** State variable for whether we are currently running detachAllOnCommit/detachAllOnRollback. */
    private boolean runningDetachAllOnTxnEnd = false;

    /** Manager for dynamic fetch groups. */
    private FetchGroupManager fetchGrpMgr;

    /** Lock manager for object-based locking. */
    private LockManager lockMgr = null;

    /** Lock object for use during commit/rollback/evict, to prevent any further field accesses. */
    protected Lock lock;

    /** State indicator whether we are currently managing the relations. */
    private boolean runningManageRelations = false;

    /** Map of RelationshipManager keyed by the ObjectProvider that it is for. */
    Map<ObjectProvider, RelationshipManager> managedRelationDetails = null;

    /** Flag for whether running persistence-by-reachability-at-commit */
    private boolean runningPBRAtCommit = false;

    /** Set of Object Ids of objects persisted using persistObject, or known as already persistent in the current transaction. */
    private Set txKnownPersistedIds = null;

    /** Set of Object Ids of objects deleted using deleteObject. */
    private Set txKnownDeletedIds = null;

    /** Set of Object Ids of objects newly persistent in the current transaction */
    private Set txFlushedNewIds = null;

    /** Set of the object ids for all objects enlisted in this transaction. Used in reachability at commit to determine what to check. */
    private Set txEnlistedIds = null;

    /**
     * Thread-specific state information (instances of {@link ThreadContextInfo}) used where we don't want
     * to pass information down through a large number of method calls.
     */
    private ThreadLocal contextInfoThreadLocal = new ThreadLocal()
    {
        protected Object initialValue()
        {
            return new ThreadContextInfo();
        }
    };

    /**
     * Context info for a particular thread. Can be used for storing state information for the current
     * thread where we don't want to pass it through large numbers of method calls (e.g persistence by
     * reachability) where such argument passing would damage the structure of DataNucleus.
     */
    static class ThreadContextInfo
    {
        int referenceCounter = 0;

        /** Map of the owner of an attached object, keyed by the object. Present when performing attachment. */
        Map<Object, ObjectProvider> attachedOwnerByObject = null;

        /** Map of attached PC object keyed by the id. Present when performing a attachment. */
        Map attachedPCById = null;

        boolean merging = false;
    }

    /**
     * Accessor for the thread context information, for the current thread.
     * If the current thread is not present, will add an info context for it.
     * <p>
     * You must call {@link #releaseThreadContextInfo()} when you don't need it anymore,
     * since we use reference counting. Use a try...finally-block for this purpose.
     * </p>
     *
     * @return The thread context information
     * @see #getThreadContextInfo()
     */
    protected ThreadContextInfo acquireThreadContextInfo()
    {
        ThreadContextInfo threadInfo = (ThreadContextInfo) contextInfoThreadLocal.get();
        ++threadInfo.referenceCounter;
        return threadInfo;
    }

    /**
     * Get the current ThreadContextInfo assigned to the current thread without changing the
     * reference counter.
     * @return the thread context information
     * @see #acquireThreadContextInfo()
     */
    protected ThreadContextInfo getThreadContextInfo()
    {
        return (ThreadContextInfo) contextInfoThreadLocal.get();
    }

    /**
     * Method to remove the current thread context info for the current thread, after
     * the reference counter reached 0. This method decrements a reference counter (per thread), that
     * is incremented by {@link #acquireThreadContextInfo()}.
     *
     * @see #acquireThreadContextInfo()
     */
    protected void releaseThreadContextInfo()
    {
        ThreadContextInfo threadInfo = (ThreadContextInfo) contextInfoThreadLocal.get();
        if (--threadInfo.referenceCounter <= 0) // might be -1, if acquireThreadContextInfo was not called. shall we throw an exception in this case?
        {
            threadInfo.referenceCounter = 0; // just to be 100% sure, we never have a negative reference counter.

            if (threadInfo.attachedOwnerByObject != null)
                threadInfo.attachedOwnerByObject.clear();
            threadInfo.attachedOwnerByObject = null;

            if (threadInfo.attachedPCById != null)
                threadInfo.attachedPCById.clear();
            threadInfo.attachedPCById = null;

            contextInfoThreadLocal.remove();
        }
    }

    /**
     * Constructor.
     * @param ctx NucleusContext
     * @param owner The owning PM/EM. This will be the PM until we split JPA from JDO
     * @param userName Username for the datastore
     * @param password Password for the datastore
     * @throws NucleusUserException if an error occurs allocating the necessary requested components
     */
    public ObjectManagerImpl(NucleusContext ctx, Object owner, String userName, String password)
    {
        if (ctx.getPersistenceConfiguration().getBooleanProperty("datanucleus.Multithreaded"))
        {
            this.lock = new ReentrantLock();
        }
        this.owner = owner;
        this.context = ctx;
        this.closed = false;

        // Set up class loading
        ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
        clr = ctx.getClassLoaderResolver(contextLoader);
        try
        {
            ImplementationCreator ic = ctx.getImplementationCreator();
            if (ic != null)
            {
                clr.setRuntimeClassLoader(ic.getClassLoader());
            }
        }
        catch (Exception ex)
        {
            // do nothing
        }

        if (NucleusLogger.PERSISTENCE.isDebugEnabled())
        {
            NucleusLogger.PERSISTENCE.debug(LOCALISER.msg("010000", this, ctx.getStoreManager()));
        }

        // copy default configuration from factory for overrideable properties
        PersistenceConfiguration config = ctx.getPersistenceConfiguration();
        Iterator<Map.Entry<String, Object>> propIter = config.getManagerOverrideableProperties().entrySet().iterator();
        while (propIter.hasNext())
        {
            Map.Entry<String, Object> entry = propIter.next();
            properties.setProperty(entry.getKey().toLowerCase(Locale.ENGLISH), entry.getValue());
        }

        // PBR at commit?
        if (getReachabilityAtCommit())
        {
            txKnownPersistedIds = new HashSet();
            txKnownDeletedIds = new HashSet();
            txFlushedNewIds = new HashSet();
            txEnlistedIds = new HashSet();
        }

        // Set up FetchPlan
        fetchPlan = new FetchPlan(this, clr).setMaxFetchDepth(config.getIntProperty("datanucleus.maxFetchDepth"));

        // Set up the Level 1 Cache
        initialiseLevel1Cache();

        // Set up the transaction suitable for this ObjectManagerFactory
        if (TransactionType.JTA.toString().equalsIgnoreCase(config.getStringProperty("datanucleus.TransactionType")))
        {
            if (getNucleusContext().isJcaMode())
            {
                tx = new JTAJCATransactionImpl(this);
            }
            else
            {
                tx = new JTATransactionImpl(this);
            }
        }
        else
        {
            tx = new TransactionImpl(this);
        }

        // Notify ObjectManager/StoreManager when tx.begin(), commit(), rollback() called
        final ExecutionContext ec = this;
        tx.bindTransactionEventListener(new TransactionEventListener()
        {
            public void transactionStarted()
            {
                getStoreManager().transactionStarted(ec);
                postBegin();
            }
            public void transactionRolledBack()
            {
                getStoreManager().transactionRolledBack(ec);
                postRollback();
            }
            public void transactionPreRollBack()
            {
                preRollback();
            }
            public void transactionPreCommit()
            {
                preCommit();
            }
            public void transactionFlushed()
            {
                //nothing to do
            }
            public void transactionEnded()
            {
                //nothing to do
            }
            public void transactionCommitted()
            {
                getStoreManager().transactionCommitted(ec);
                postCommit();
            }
        });

        if (context.hasLevel2Cache())
        {
            txCachedIds = new HashSet();
        }
    }

    /**
     * Method to initialise the L1 cache.
     * @throws NucleusUserException if an error occurs setting up the L1 cache
     */
    protected void initialiseLevel1Cache()
    {
        String level1Type = context.getPersistenceConfiguration().getStringProperty("datanucleus.cache.level1.type");
        if (level1Type != null && level1Type.equalsIgnoreCase("none"))
        {
            return;
        }

        // Find the L1 cache class name from its plugin name
        String level1ClassName = getNucleusContext().getPluginManager().getAttributeValueForExtension("org.datanucleus.cache_level1", 
            "name", level1Type, "class-name");
        if (level1ClassName == null)
        {
            // Plugin of this name not found
            throw new NucleusUserException(LOCALISER.msg("003001", level1Type)).setFatal();
        }

        try
        {
            // Create an instance of the L1 Cache
            cache = (Level1Cache)getNucleusContext().getPluginManager().createExecutableExtension(
                "org.datanucleus.cache_level1", "name", level1Type, "class-name", null, null);
            if (NucleusLogger.CACHE.isDebugEnabled())
            {
                NucleusLogger.CACHE.debug(LOCALISER.msg("003003", level1Type));
            }
        }
        catch (Exception e)
        {
            // Class name for this L1 cache plugin is not found!
            throw new NucleusUserException(LOCALISER.msg("003002", level1Type, level1ClassName),e).setFatal();
        }
    }

    /**
     * Accessor for whether this ObjectManager is closed.
     * @return Whether this manager is closed.
     */
    public boolean isClosed()
    {
        return closed;
    }

    public ClassLoaderResolver getClassLoaderResolver()
    {
        return clr;
    }

    public StoreManager getStoreManager()
    {
        return getNucleusContext().getStoreManager();
    }

    public ApiAdapter getApiAdapter()
    {
        return getNucleusContext().getApiAdapter();
    }

    public TypeManager getTypeManager()
    {
        return getNucleusContext().getTypeManager();
    }

    /* (non-Javadoc)
     * @see org.datanucleus.ObjectManager#getLockManager()
     */
    public LockManager getLockManager()
    {
        if (lockMgr == null)
        {
            lockMgr = new LockManagerImpl();
        }
        return lockMgr;
    }

    /**
     * Acessor for the current FetchPlan.
     * @return FetchPlan
     */
    public FetchPlan getFetchPlan()
    {
        assertIsOpen();
        return fetchPlan;
    }

    /**
     * Method to return the owner PM object.
     * @return The owner manager object
     */
    public Object getOwner()
    {
        return owner;
    }

    /**
     * Gets the context in which this ObjectManager is running
     * @return Returns the context.
     */
    public NucleusContext getNucleusContext()
    {
        return context;
    }

    /**
     * Accessor for the MetaDataManager for this ObjectManager (and its factory).
     * This is used as the interface to MetaData in the ObjectManager/Factory.
     * @return Returns the MetaDataManager.
     */
    public MetaDataManager getMetaDataManager()
    {
        return getNucleusContext().getMetaDataManager();
    }

    /* (non-Javadoc)
     * @see org.datanucleus.ObjectManager#setProperty(java.lang.String, java.lang.Object)
     */
    public void setProperty(String name, Object value)
    {
        /*if (tx.isActive())
        {
            // Don't allow change of options during a transaction
            if (NucleusLogger.PERSISTENCE.isDebugEnabled())
            {
                NucleusLogger.PERSISTENCE.debug("Attempt to set property " + name + " during a transaction. Ignored");
            }
            return;
        }*/
        if (properties.hasProperty(name.toLowerCase(Locale.ENGLISH)))
        {
            String intName = getNucleusContext().getPersistenceConfiguration().getInternalNameForProperty(name);
            properties.setProperty(intName.toLowerCase(Locale.ENGLISH), value);
        }
    }

    /* (non-Javadoc)
     * @see org.datanucleus.ObjectManager#getProperties()
     */
    public Map<String, Object> getProperties()
    {
        return properties.getProperties();
    }

    public Boolean getBooleanProperty(String name)
    {
        if (properties.hasProperty(name.toLowerCase(Locale.ENGLISH)))
        {
            assertIsOpen();
            String intName = getNucleusContext().getPersistenceConfiguration().getInternalNameForProperty(name);
            return properties.getBooleanProperty(intName);
        }
        return null;
    }

    public Integer getIntProperty(String name)
    {
        if (properties.hasProperty(name.toLowerCase(Locale.ENGLISH)))
        {
            assertIsOpen();
            String intName = getNucleusContext().getPersistenceConfiguration().getInternalNameForProperty(name);
            return properties.getIntProperty(intName);
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.datanucleus.ObjectManager#getProperty(java.lang.String)
     */
    public Object getProperty(String name)
    {
        if (properties.hasProperty(name.toLowerCase(Locale.ENGLISH)))
        {
            assertIsOpen();
            String intName = getNucleusContext().getPersistenceConfiguration().getInternalNameForProperty(name);
            return properties.getProperty(intName.toLowerCase(Locale.ENGLISH));
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.datanucleus.ObjectManager#getSupportedProperties()
     */
    public Set<String> getSupportedProperties()
    {
        return properties.getPropertyNames();
    }

    /**
     * Accessor for the datastore read timeout in milliseconds.
     * @return Datastore read timeout in milliseconds (if specified)
     */
    public Integer getDatastoreReadTimeoutMillis()
    {
        assertIsOpen();
        return properties.getIntProperty(PROP_READ_TIMEOUT.toLowerCase(Locale.ENGLISH));
    }

    /**
     * Accessor for the datastore write timeout in milliseconds.
     * @return Datastore write timeout in milliseconds (if specified)
     */
    public Integer getDatastoreWriteTimeoutMillis()
    {
        assertIsOpen();
        return properties.getIntProperty(PROP_WRITE_TIMEOUT.toLowerCase(Locale.ENGLISH));
    }

    /**
     * Accessor for whether the object manager is multithreaded.
     * @return Whether to run multithreaded.
     */
    public boolean getMultithreaded()
    {
        return false;
    }

    /**
     * Accessor for whether to detach objects on close of the ObjectManager.
     * <b>This is not suitable for use in JCA mode.</b>
     * @return Whether to detach on close.
     */
    protected boolean getDetachOnClose()
    {
        assertIsOpen();
        return properties.getBooleanProperty(PROP_DETACH_ON_CLOSE.toLowerCase(Locale.ENGLISH));
    }

    /**
     * Accessor for whether to detach all objects on commit of the transaction.
     * @return Whether to detach all on commit.
     */
    protected boolean getDetachAllOnCommit()
    {
        assertIsOpen();
        return properties.getBooleanProperty(PROP_DETACH_ON_COMMIT.toLowerCase(Locale.ENGLISH));
    }

    /**
     * Accessor for whether to detach all objects on rollback of the transaction.
     * @return Whether to detach all on rollback.
     */
    protected boolean getDetachAllOnRollback()
    {
        assertIsOpen();
        return properties.getBooleanProperty(PROP_DETACH_ON_ROLLBACK.toLowerCase(Locale.ENGLISH));
    }

    /**
     * Accessor for whether to run the reachability algorithm at commit time.
     * @return Whether to run PBR at commit
     */
    protected boolean getReachabilityAtCommit()
    {
        assertIsOpen();
        return properties.getBooleanProperty(PROP_PBR_AT_COMMIT.toLowerCase(Locale.ENGLISH));
    }

    /**
     * Accessor for whether to copy on attaching.
     * @return Whether to copy on attaching
     */
    public boolean getCopyOnAttach()
    {
        assertIsOpen();
        return properties.getBooleanProperty(PROP_COPY_ON_ATTACH.toLowerCase(Locale.ENGLISH));
    }

    /**
     * Accessor for whether to ignore the cache.
     * @return Whether to ignore the cache.
     */
    public boolean getIgnoreCache()
    {
        assertIsOpen();
        return properties.getBooleanProperty(PROP_IGNORE_CACHE.toLowerCase(Locale.ENGLISH));
    }

    /**
     * Whether the datastore operations are delayed until commit/flush. 
     * In optimistic transactions this is automatically enabled. In datastore transactions there is a
     * persistence property to enable it.
     * If we are committing/flushing then will return false since the delay is no longer required.
     * @return true if datastore operations are delayed until commit/flush
     */
    public boolean isDelayDatastoreOperationsEnabled()
    {
        if (flushing || tx.isCommitting())
        {
            // Already sending to the datastore so return false to not confuse things
            return false;
        }
        else if (!tx.isActive() && isNonTxAtomic())
        {
            // Non-tx atomic operation, so just do proceed
            // DN up to and including 3.0M5 returned true here
            return false;
        }

        Boolean delayProp = getNucleusContext().getPersistenceConfiguration().getBooleanObjectProperty(
            "datanucleus.datastoreTransactionDelayOperations");
        if (delayProp != null)
        {
            return delayProp.booleanValue();
        }
        else
        {
            return tx.getOptimistic();
        }
    }

    /**
     * Tests whether this persistable object is in the process of being inserted.
     * @param pc the object to verify the status
     * @return true if this instance is inserting.
     */
    public boolean isInserting(Object pc)
    {
        ObjectProvider sm = findObjectProvider(pc);
        if (sm == null)
        {
            return false;
        }
        return sm.isInserting();
    }

    /**
     * Accessor for the current transaction.
     * @return The transaction
     */
    public Transaction getTransaction()
    {
        assertIsOpen();
        return tx;
    }

    /**
     * Method to enlist the specified ObjectProvider in the current transaction.
     * @param sm The ObjectProvider
     */
    public void enlistInTransaction(ObjectProvider sm)
    {
        assertActiveTransaction();
        if (NucleusLogger.TRANSACTION.isDebugEnabled())
        {
            NucleusLogger.TRANSACTION.debug(LOCALISER.msg("015017", 
                StringUtils.toJVMIDString(sm.getObject()), sm.getInternalObjectId().toString()));
        }

        if (getReachabilityAtCommit() && tx.isActive())
        {
            if (getApiAdapter().isNew(sm.getObject()))
            {
                // Add this object to the list of new objects in this transaction
                txFlushedNewIds.add(sm.getInternalObjectId());
            }
            else if (getApiAdapter().isPersistent(sm.getObject()) && !getApiAdapter().isDeleted(sm.getObject()))
            {
                // Add this object to the list of known valid persisted objects (unless it is a known new object)
                if (!txFlushedNewIds.contains(sm.getInternalObjectId()))
                {
                    txKnownPersistedIds.add(sm.getInternalObjectId());
                }
            }

            // Add the object to those enlisted
            if (!runningPBRAtCommit)
            {
                // Keep a note of the id for use by persistence-by-reachability-at-commit
                txEnlistedIds.add(sm.getInternalObjectId());
            }
        }

        enlistedSMCache.put(sm.getInternalObjectId(), (StateManager)sm);
    }

    /**
     * Method to evict the specified ObjectProvider from the current transaction.
     * @param sm The ObjectProvider
     */
    public void evictFromTransaction(ObjectProvider sm)
    {
        if (NucleusLogger.TRANSACTION.isDebugEnabled())
        {
            NucleusLogger.TRANSACTION.debug(LOCALISER.msg("015019", 
                StringUtils.toJVMIDString(sm.getObject()), getIdentityAsString(sm.getInternalObjectId())));
        }

        if (enlistedSMCache.remove(sm.getInternalObjectId()) == null)
        {
            //probably because the object was garbage collected
            if (NucleusLogger.TRANSACTION.isDebugEnabled())
            {
                NucleusLogger.TRANSACTION.debug(LOCALISER.msg("010023", 
                    StringUtils.toJVMIDString(sm.getObject()), getIdentityAsString(sm.getInternalObjectId())));
            }
        }
    }

    /**
     * Method to return if an object is enlisted in the current transaction.
     * This is only of use when running "persistence-by-reachability-at-commit"
     * @param id Identity for the object
     * @return Whether it is enlisted in the current transaction
     */
    public boolean isEnlistedInTransaction(Object id)
    {
        if (!getReachabilityAtCommit() || !tx.isActive())
        {
            return false;
        }

        if (id == null)
        {
            return false;
        }
        return txEnlistedIds.contains(id);
    }

    /**
     * Convenience method to return the attached object for the specified id if one exists.
     * Returns null if there is no currently enlisted/cached object with the specified id.
     * @param id The id
     * @return The attached object
     */
    public Object getAttachedObjectForId(Object id)
    {
        StateManager sm = enlistedSMCache.get(id);
        if (sm != null)
        {
            return sm.getObject();
        }
        if (cache != null)
        {
            sm = (StateManager)cache.get(id);
            if (sm != null)
            {
                return sm.getObject();
            }
        }
        return null;
    }

    /**
     * Method to add the object managed by the specified StateManager to the (L1) cache.
     * @param sm The StateManager
     */
    public void addStateManager(StateManager sm)
    {
        // Add to the Level 1 Cache
        putObjectIntoCache(sm);
    }

    /**
     * Method to remove the object managed by the specified StateManager from the cache.
     * @param sm The StateManager
     */
    public void removeStateManager(StateManager sm)
    {
        // Remove from the Level 1 Cache
        removeObjectFromCache(sm.getInternalObjectId());

        // Remove it from any transaction
        enlistedSMCache.remove(sm.getInternalObjectId());
    }

    /**
     * Accessor for the StateManager of an object given the object id.
     * @param id Id of the object.
     * @return The StateManager
     */
    public StateManager getStateManagerById(Object id)
    {
        assertIsOpen();
        return findStateManager(getObjectFromCache(id));
    }

    /**
     * Method to find the StateManager for an object.
     * @param pc The object we require the StateManager for.
     * @return The StateManager, null if StateManager not found.
     *     See JDO spec for the calling behavior when null is returned
     */
    public StateManager findStateManager(Object pc)
    {
        StateManager sm = null;
        Object previousLookingFor = objectLookingForStateManager;
        StateManager previousFound = foundStateManager;
        try
        {
            objectLookingForStateManager = pc;
            foundStateManager = null;
            // We call "ObjectManagerHelper.getObjectManager(pc)".
            // This then calls "JDOHelper.getPersistenceManager(pc)".
            // Which calls "StateManager.getPersistenceManager(pc)".
            // That then calls "hereIsStateManager(sm, pc)" which sets "foundStateManager".
            ExecutionContext ec = getApiAdapter().getExecutionContext(pc);
            if (ec != null && this != ec)
            {
                throw new NucleusUserException(LOCALISER.msg("010007", getApiAdapter().getIdForObject(pc)));
            }
            sm = foundStateManager;
        }
        finally
        {
            objectLookingForStateManager = previousLookingFor;
            foundStateManager = previousFound;
        }
        return sm;
    }

    /**
     * Method to find the ObjectProvider for an object.
     * @param pc The object we are checking
     * @return The ObjectProvider, null if not found.
     */
    public ObjectProvider findObjectProvider(Object pc)
    {
        return findStateManager(pc);
    }

    /**
     * @param persist persists the object if not yet persisted. 
     */
    public ObjectProvider findObjectProvider(Object pc, boolean persist)
    {
        ObjectProvider sm = findObjectProvider(pc);
        if (sm == null && persist)
        {
            int objectType = ObjectProvider.PC;
            Object object2 = persistObjectInternal(pc, null, null, -1, objectType);
            sm = findObjectProvider(object2);
        }
        else if (sm == null)
        {
            return null;
        }
        return sm;
    }

    public ObjectProvider findObjectProviderForEmbedded(Object value, ObjectProvider owner, AbstractMemberMetaData mmd)
    {
        ObjectProvider embeddedSM = findObjectProvider(value);
        if (embeddedSM == null)
        {
            // Assign a StateManager to manage our embedded object
            embeddedSM = ObjectProviderFactory.newForEmbedded(this, value, false, owner,
                owner.getClassMetaData().getMetaDataForMember(mmd.getName()).getAbsoluteFieldNumber());
        }
        if (embeddedSM.getEmbeddedOwners() == null || embeddedSM.getEmbeddedOwners().length == 0)
        {
            int absoluteFieldNumber = owner.getClassMetaData().getMetaDataForMember(mmd.getName()).getAbsoluteFieldNumber();
            embeddedSM.addEmbeddedOwner(owner, absoluteFieldNumber);
            embeddedSM.setPcObjectType(ObjectProvider.EMBEDDED_PC);
        }
        return embeddedSM;
    }

    /* (non-Javadoc)
     * @see org.datanucleus.store.ExecutionContext#newObjectProviderForEmbedded(org.datanucleus.metadata.AbstractMemberMetaData, org.datanucleus.metadata.AbstractClassMetaData, org.datanucleus.store.ObjectProvider, int)
     */
    public ObjectProvider newObjectProviderForEmbedded(AbstractMemberMetaData ownerMmd, AbstractClassMetaData cmd, 
            ObjectProvider ownerOP, int ownerFieldNumber)
    {
        Class pcClass = getClassLoaderResolver().classForName(cmd.getFullClassName());
        StateManager sm = (StateManager) ObjectProviderFactory.newForHollow(this, pcClass, null);
        sm.initialiseForEmbedded(sm.getObject(), false);
        if (ownerOP != null)
        {
            sm.addEmbeddedOwner(ownerOP, ownerFieldNumber);
        }
        return sm;
    }

    /**
     * Method to add the ObjectProvider for an object to this ObjectManager's list.
     * @param sm The ObjectProvider
     * @param pc The object managed by the ObjectProvider
     */
    public void hereIsObjectProvider(ObjectProvider sm, Object pc)
    {
        if (objectLookingForStateManager == pc)
        {
            foundStateManager = (StateManager) sm;
        }
    }

    /**
     * Method to close the Object Manager.
     */
    public void close()
    {
        if (closed)
        {
            throw new NucleusUserException(LOCALISER.msg("010002"));
        }
        if (tx.isActive())
        {
            throw new TransactionActiveOnCloseException(this);
        }

        // Commit any outstanding non-tx updates
        if (!dirtySMs.isEmpty() && tx.getNontransactionalWrite())
        {
            if (isNonTxAtomic())
            {
                // TODO Remove this when all mutator operations handle it atomically
                // Process as nontransactional update
                processNontransactionalUpdate();
            }
            else
            {
                // Process within a transaction
                try
                {
                    tx.begin();
                    tx.commit();
                }
                finally
                {
                    if (tx.isActive())
                    {
                        tx.rollback();
                    }
                }
            }
        }

        if (getDetachOnClose())
        {
            // "detach-on-close", detaching all currently cached objects.
            performDetachOnClose();
        }

        // Call all listeners to do their clean up
        ExecutionContext.LifecycleListener[] listener = context.getObjectManagerListeners();
        for (int i=0; i<listener.length; i++)
        {
            listener[i].preClose(this);
        }

        // Disconnect remaining resources
        disconnectSMCache();
        disconnectLifecycleListener();

        // Reset the Fetch Plan to its DFG setting
        fetchPlan.clearGroups().addGroup(FetchPlan.DEFAULT);

        closed = true;
        tx = null;

        if (NucleusLogger.PERSISTENCE.isDebugEnabled())
        {
            NucleusLogger.PERSISTENCE.debug(LOCALISER.msg("010001", this));
        }
    }

    /**
     * Disconnect SM instances, clear cache and reset settings 
     */
    public void disconnectSMCache()
    {
        if (cache != null)
        {
            // Clear out the cache (use separate list since sm.disconnect will remove the object from "cache" so we avoid
            // any ConcurrentModification issues)
            Collection cachedSMsClone = new HashSet(cache.values());
            Iterator iter = cachedSMsClone.iterator();
            while (iter.hasNext())
            {
                StateManager sm = (StateManager) iter.next();
                if (sm != null)
                {
                    sm.disconnect();
                }
            }
            cache.clear();
            if (NucleusLogger.CACHE.isDebugEnabled())
            {
                NucleusLogger.CACHE.debug(LOCALISER.msg("003011"));
            }
        }
    }

    /**
     * Convenience method for whether any non-tx operations are considered "atomic" (i.e auto-commit).
     * @return Whether atomic non-tx behaviour
     */
    private boolean isNonTxAtomic()
    {
        return getNucleusContext().getPersistenceConfiguration().getBooleanProperty("datanucleus.nontx.atomic");
    }

    /**
     * Method called at the completion of a nontransactional write.
     * If "datanucleus.nontx.atomic" is false then returns immediately.
     * Otherwise will flush any updates that are outstanding (updates to an object), will perform detachAllOnCommit
     * if enabled (so user always has detached objects), update objects in any L2 cache, and migrates any 
     * objects through lifecycle changes.
     * Is similar in content to "flush"+"preCommit"+"postCommit"
     */
    public void processNontransactionalUpdate()
    {
        if (tx.isActive() || !tx.getNontransactionalWrite())
        {
            return;
        }
        else if (!isNonTxAtomic())
        {
            // Not using atomic nontransactional operations so just return
            return;
        }

        if (!dirtySMs.isEmpty())
        {
            // Make sure all non-tx dirty objects are enlisted so they get lifecycle changes
            Iterator<StateManager> iter = dirtySMs.iterator();
            while (iter.hasNext())
            {
                StateManager sm = iter.next();
                enlistedSMCache.put(sm.getInternalObjectId(), sm);
            }

            // Flush any outstanding changes to the datastore
            flushInternal(true);

            if (context.hasLevel2Cache())
            {
                // L2 caching of enlisted objects
                performLevel2CacheUpdateAtCommit();
            }

            if (getDetachAllOnCommit())
            {
                // "detach-on-commit"
                performDetachAllOnTxnEndPreparation();
                performDetachAllOnTxnEnd();
            }

            // Make sure lifecycle changes take place to all "enlisted" objects
            List failures = null;
            try
            {
                // "commit" all enlisted StateManagers
                ApiAdapter api = getApiAdapter();
                StateManager[] sms = enlistedSMCache.values().toArray(new StateManager[enlistedSMCache.size()]);
                for (int i = 0; i < sms.length; ++i)
                {
                    try
                    {
                        // Run through "postCommit" to migrate the lifecycle state
                        if (sms[i] != null && sms[i].getObject() != null &&
                                (api.isPersistent(sms[i].getObject()) && api.isTransactional(sms[i].getObject())))
                        {
                            sms[i].postCommit(getTransaction());
                        }
                    }
                    catch (RuntimeException e)
                    {
                        if (failures == null)
                        {
                            failures = new ArrayList();
                        }
                        failures.add(e);
                    }
                }
            }
            finally
            {
                resetTransactionalVariables();
            }
            if (failures != null && !failures.isEmpty())
            {
                throw new CommitStateTransitionException((Exception[]) failures.toArray(new Exception[failures.size()]));
            }
        }

        if (nontxProcessedSMs != null && !nontxProcessedSMs.isEmpty())
        {
            for (StateManager sm : nontxProcessedSMs)
            {
                if (sm != null && sm.getLifecycleState() != null && sm.getLifecycleState().isDeleted())
                {
                    removeObjectFromCache(sm.getInternalObjectId());
                    removeObjectFromLevel2Cache(sm.getInternalObjectId());
                }
            }
            nontxProcessedSMs.clear();
        }
    }

    // ----------------------------- Lifecycle Methods ------------------------------------

    /**
     * Internal method to evict an object from L1 cache.
     * @param obj The object
     * @throws NucleusException if an error occurs evicting the object
     */
    public void evictObject(Object obj)
    {
        if (obj == null)
        {
            return;
        }

        try
        {
            clr.setPrimary(obj.getClass().getClassLoader());
            assertClassPersistable(obj.getClass());
            assertNotDetached(obj);

            // we do not directly remove from cache level 1 here. The cache level 1 will be evicted 
            // automatically on garbage collection, if the object can be evicted. it means not all
            // jdo states allows the object to be evicted.
            StateManager sm = findStateManager(obj);
            if (sm == null)
            {
                throw new NucleusUserException(LOCALISER.msg("010007", getApiAdapter().getIdForObject(obj)));
            }
            sm.evict();
        }
        finally
        {
            clr.unsetPrimary();
        }
    }

    /**
     * Method to evict all objects of the specified type (and optionaly its subclasses) that are present in the L1 cache.
     * @param cls Type of persistable object
     * @param subclasses Whether to include subclasses
     */
    public void evictObjects(Class cls, boolean subclasses)
    {
        assertIsOpen();

        if (cache != null)
        {
            try
            {
                if (getMultithreaded())
                {
                    // Lock since updates fields in object(s)
                    lock.lock();
                }

                ArrayList stateManagersToEvict = new ArrayList();
                stateManagersToEvict.addAll(cache.values());
                Iterator smIter = stateManagersToEvict.iterator();
                while (smIter.hasNext())
                {
                    StateManager sm = (StateManager)smIter.next();
                    Object pc = sm.getObject();
                    boolean evict = false;
                    if (!subclasses && pc.getClass() == cls)
                    {
                        evict = true;
                    }
                    else if (subclasses && cls.isAssignableFrom(pc.getClass()))
                    {
                        evict = true;
                    }

                    if (evict)
                    {
                        sm.evict();
                        removeObjectFromCache(getApiAdapter().getIdForObject(pc));
                    }
                }
            }
            finally
            {
                if (getMultithreaded())
                {
                    lock.unlock();
                }
            }
        }
    }

    /**
     * Method to evict all current objects from L1 cache.
     */
    public void evictAllObjects()
    {
        assertIsOpen();

        if (cache != null)
        {
            try
            {
                if (getMultithreaded())
                {
                    lock.lock();
                }

                // All persistent non-transactional instances should be evicted here, but not yet supported
                ArrayList stateManagersToEvict = new ArrayList();
                stateManagersToEvict.addAll(cache.values());

                // Evict StateManagers and remove objects from cache
                // Performed in separate loop to avoid ConcurrentModificationException
                Iterator smIter = stateManagersToEvict.iterator();
                while (smIter.hasNext())
                {
                    StateManager sm = (StateManager)smIter.next();
                    Object pc = sm.getObject();
                    sm.evict();

                    // Evict from L1
                    removeObjectFromCache(getApiAdapter().getIdForObject(pc));
                }
            }
            finally
            {
                if (getMultithreaded())
                {
                    lock.unlock();
                }
            }
        }
    }

    /**
     * Method to do a refresh of an object, updating it from its
     * datastore representation. Also updates the object in the L1/L2 caches.
     * @param obj The Object
     */
    public void refreshObject(Object obj)
    {
        if (obj == null)
        {
            return;
        }

        try
        {
            clr.setPrimary(obj.getClass().getClassLoader());
            assertClassPersistable(obj.getClass());
            assertNotDetached(obj);

            StateManager sm = findStateManager(obj);
            if (sm == null)
            {
                throw new NucleusUserException(LOCALISER.msg("010007", getApiAdapter().getIdForObject(obj)));
            }

            if (getApiAdapter().isPersistent(obj) && sm.isWaitingToBeFlushedToDatastore())
            {
                // Persistent but not yet flushed so nothing to "refresh" from!
                return;
            }

            sm.refresh();
        }
        finally
        {
            clr.unsetPrimary();
        }
    }

    /**
     * Method to do a refresh of all objects.
     * @throws NucleusUserException thrown if instances could not be refreshed.
     */
    public void refreshAllObjects()
    {
        assertIsOpen();

        Set toRefresh = new HashSet();
        toRefresh.addAll(enlistedSMCache.values());
        toRefresh.addAll(dirtySMs);
        toRefresh.addAll(indirectDirtySMs);
        if (!tx.isActive() && cache != null)
        {
            toRefresh.addAll(cache.values());
        }

        try
        {
            if (getMultithreaded())
            {
                // Lock since updates fields in object(s)
                lock.lock();
            }

            List failures = null;
            Iterator iter = toRefresh.iterator();
            while (iter.hasNext())
            {
                try
                {
                    Object obj = iter.next();
                    StateManager sm;
                    if (getApiAdapter().isPersistable(obj))
                    {
                        sm = findStateManager(obj);
                    }
                    else
                    {
                        sm = (StateManager) obj;
                    }
                    sm.refresh();
                }
                catch (RuntimeException e)
                {
                    if (failures == null)
                    {
                        failures = new ArrayList();
                    }
                    failures.add(e);
                }
            }
            if (failures != null && !failures.isEmpty())
            {
                throw new NucleusUserException(LOCALISER.msg("010037"), (Exception[]) failures.toArray(new Exception[failures.size()]));
            }
        }
        finally
        {
            if (getMultithreaded())
            {
                lock.unlock();
            }
        }
    }

    /**
     * Method to retrieve an object.
     * @param obj The object
     * @param fgOnly Whether to retrieve the current fetch group fields only
     */
    public void retrieveObject(Object obj, boolean fgOnly)
    {
        if (obj == null)
        {
            return;
        }

        try
        {
            clr.setPrimary(obj.getClass().getClassLoader());
            assertClassPersistable(obj.getClass());
            assertNotDetached(obj);

            StateManager sm = findStateManager(obj);
            if (sm == null)
            {
                throw new NucleusUserException(LOCALISER.msg("010007", getApiAdapter().getIdForObject(obj)));
            }
            sm.retrieve(fgOnly);
        }
        finally
        {
            clr.unsetPrimary();
        }
    }

    /**
     * Method to make an object persistent.
     * NOT to be called by internal DataNucleus methods. Only callable by external APIs (JDO/JPA).
     * @param obj The object
     * @param merging Whether this object (and dependents) is being merged
     * @return The persisted object
     * @throws NucleusUserException if the object is managed by a different manager
     */
    public Object persistObject(Object obj, boolean merging)
    {
        if (obj == null)
        {
            return null;
        }

        // Allocate thread-local persistence info
        ThreadContextInfo threadInfo = acquireThreadContextInfo();
        try
        {
            if (merging && getNucleusContext().getPersistenceConfiguration().getBooleanProperty("datanucleus.allowAttachOfTransient", false))
            {
                threadInfo.merging = true;
            }
            if (threadInfo.attachedOwnerByObject == null)
            {
                threadInfo.attachedOwnerByObject = new HashMap();
            }
            if (threadInfo.attachedPCById == null)
            {
                threadInfo.attachedPCById = new HashMap();
            }

            if (tx.isActive())
            {
                return persistObjectWork(obj, merging);
            }
            else
            {
                boolean success = true;
                Set cachedIds = new HashSet(cache.keySet());
                try
                {
                    return persistObjectWork(obj, merging);
                }
                catch (RuntimeException re)
                {
                    // Make sure we evict any objects that have been put in the L1 cache during this step
                    // TODO Also ought to disconnect any state manager
                    success = false;
                    Iterator cacheIter = cache.keySet().iterator();
                    while (cacheIter.hasNext())
                    {
                        Object id = cacheIter.next();
                        if (!cachedIds.contains(id))
                        {
                            // Remove from L1 cache
                            cacheIter.remove();
                        }
                    }
                    throw re;
                }
                finally
                {
                    if (success)
                    {
                        // Commit any non-tx changes
                        processNontransactionalUpdate();
                    }
                }
            }
        }
        finally
        {
            // Deallocate thread-local persistence info
            releaseThreadContextInfo();
        }
    }

    /**
     * Method to persist an array of objects to the datastore.
     * @param objs The objects to persist
     * @return The persisted objects
     * @throws NucleusUserException Thrown if an error occurs during the persist process.
     *     Any exception could have several nested exceptions for each failed object persist
     */
    public Object[] persistObjects(Object[] objs)
    {
        if (objs == null)
        {
            return null;
        }

        Object[] persistedObjs = new Object[objs.length];

        // Allocate thread-local persistence info
        ThreadContextInfo threadInfo = acquireThreadContextInfo();
        try
        {
            if (threadInfo.attachedOwnerByObject == null)
            {
                threadInfo.attachedOwnerByObject = new HashMap();
            }
            if (threadInfo.attachedPCById == null)
            {
                threadInfo.attachedPCById = new HashMap();
            }

            try
            {
                getStoreManager().getPersistenceHandler().batchStart(this, PersistenceBatchType.PERSIST);
                ArrayList<RuntimeException> failures = null;
                for (int i=0;i<objs.length;i++)
                {
                    try
                    {
                        if (objs[i] != null)
                        {
                            persistedObjs[i] = persistObjectWork(objs[i], false);
                        }
                    }
                    catch (RuntimeException e)
                    {
                        if (failures == null)
                        {
                            failures = new ArrayList();
                        }
                        failures.add(e);
                    }
                }
                if (failures != null && !failures.isEmpty())
                {
                    RuntimeException e = failures.get(0);
                    if (e instanceof NucleusException && ((NucleusException)e).isFatal())
                    {
                        // Should really check all and see if any are fatal not just first one
                        throw new NucleusFatalUserException(LOCALISER.msg("010039"), 
                            failures.toArray(new Exception[failures.size()]));
                    }
                    else
                    {
                        throw new NucleusUserException(LOCALISER.msg("010039"), 
                            failures.toArray(new Exception[failures.size()]));
                    }
                }
            }
            finally
            {
                getStoreManager().getPersistenceHandler().batchEnd(this, PersistenceBatchType.PERSIST);

                if (!tx.isActive())
                {
                    // Commit any non-tx changes
                    processNontransactionalUpdate();
                }
            }
        }
        finally
        {
            // Deallocate thread-local persistence info
            releaseThreadContextInfo();
        }
        return persistedObjs;
    }

    /**
     * Method to make an object persistent.
     * NOT to be called by internal DataNucleus methods. Only callable by external APIs (JDO/JPA).
     * @param obj The object
     * @param merging Whether this object (and dependents) is being merged
     * @return The persisted object
     * @throws NucleusUserException if the object is managed by a different manager
     */
    Object persistObjectWork(Object obj, boolean merging)
    {
        boolean detached = getApiAdapter().isDetached(obj);

        // Persist the object
        Object persistedPc = persistObjectInternal(obj, null, null, -1, ObjectProvider.PC);

        // If using reachability at commit and appropriate save it for reachability checks when we commit
        StateManager sm = findStateManager(persistedPc);
        if (sm != null)
        {
            if (indirectDirtySMs.contains(sm))
            {
                dirtySMs.add(sm);
                indirectDirtySMs.remove(sm);
            }
            else if (!dirtySMs.contains(sm))
            {
                dirtySMs.add(sm);
                if (txCachedIds != null)
                {
                    txCachedIds.add(sm.getInternalObjectId());
                }
            }

            if (getReachabilityAtCommit() && tx.isActive())
            {
                if (detached || getApiAdapter().isNew(persistedPc))
                {
                    txKnownPersistedIds.add(sm.getInternalObjectId());
                }
            }
        }

        return persistedPc;
    }

    /**
     * Method to make an object persistent which should be called from internal calls only.
     * All PM/EM calls should go via persistObject(Object obj).
     * @param obj The object
     * @param preInsertChanges Any changes to make before inserting
     * @param ownerOP ObjectProvider of the owner when embedded
     * @param ownerFieldNum Field number in the owner where this is embedded (or -1 if not embedded)
     * @param objectType Type of object (see org.datanucleus.StateManager, e.g StateManager.PC)
     * @return The persisted object
     * @throws NucleusUserException if the object is managed by a different manager
     */
    public Object persistObjectInternal(Object obj, FieldValues preInsertChanges, 
            ObjectProvider ownerOP, int ownerFieldNum, int objectType)
    {
        if (obj == null)
        {
            return null;
        }

        // TODO Support embeddedOwner/objectType, so we can add StateManager for embedded objects here
        ApiAdapter api = getApiAdapter();
        Object id = null; // Id of the object that was persisted during this process (if any)
        try
        {
            clr.setPrimary(obj.getClass().getClassLoader());
            assertClassPersistable(obj.getClass());
            ExecutionContext ec = api.getExecutionContext(obj);
            if (ec != null && ec != this)
            {
                // Object managed by a different manager
                throw new NucleusUserException(LOCALISER.msg("010007", obj));
            }

            Object persistedPc = obj; // Persisted object is the passed in pc (unless being attached as a copy)
            if (api.isDetached(obj))
            {
                // Detached : attach it
                assertDetachable(obj);
                if (getCopyOnAttach())
                {
                    // Attach a copy and return the copy
                    persistedPc = attachObjectCopy(ownerOP, obj, api.getIdForObject(obj) == null);
                }
                else
                {
                    // Attach the object
                    attachObject(ownerOP, obj, api.getIdForObject(obj) == null);
                    persistedPc = obj;
                }
            }
            else if (api.isTransactional(obj) && !api.isPersistent(obj))
            {
                // TransientTransactional : persist it
                if (NucleusLogger.PERSISTENCE.isDebugEnabled())
                {
                    NucleusLogger.PERSISTENCE.debug(LOCALISER.msg("010015", StringUtils.toJVMIDString(obj)));
                }
                StateManager sm = findStateManager(obj);
                if (sm == null)
                {
                    throw new NucleusUserException(LOCALISER.msg("010007", getApiAdapter().getIdForObject(obj)));
                }
                sm.makePersistentTransactionalTransient();
            }
            else if (!api.isPersistent(obj))
            {
                // Transient : persist it
                if (NucleusLogger.PERSISTENCE.isDebugEnabled())
                {
                    NucleusLogger.PERSISTENCE.debug(LOCALISER.msg("010015", StringUtils.toJVMIDString(obj)));
                }
                boolean merged = false;
                ThreadContextInfo threadInfo = acquireThreadContextInfo();
                try
                {
                    if (threadInfo.merging)
                    {
                        AbstractClassMetaData cmd = getMetaDataManager().getMetaDataForClass(obj.getClass(), clr);
                        if (cmd.getIdentityType() == IdentityType.APPLICATION)
                        {
                            Object transientId = api.getNewApplicationIdentityObjectId(obj, cmd);
                            Object existingObj = findObject(transientId, true, true, cmd.getFullClassName());
                            ObjectProvider existingOP = findObjectProvider(existingObj);
                            ((StateManager)existingOP).attach(obj);
                            id = transientId;
                            merged = true;
                            persistedPc = existingObj;
                        }
                    }
                }
                catch (NucleusObjectNotFoundException onfe)
                {
                    // Object with this id doesn't exist, so just persist the transient (below)
                }
                finally
                {
                    releaseThreadContextInfo();
                }

                if (!merged)
                {
                    StateManager sm = findStateManager(obj);
                    if (sm == null)
                    {
                        if ((objectType == ObjectProvider.EMBEDDED_COLLECTION_ELEMENT_PC || 
                                objectType == ObjectProvider.EMBEDDED_MAP_KEY_PC ||
                                objectType == ObjectProvider.EMBEDDED_MAP_VALUE_PC ||
                                objectType == ObjectProvider.EMBEDDED_PC) && ownerOP != null)
                        {
                            // SCO object
                            sm = (StateManager) ObjectProviderFactory.newForEmbedded(this, obj, false, ownerOP, ownerFieldNum);
                            sm.setPcObjectType((short) objectType);
                            sm.makePersistent();
                            id = sm.getInternalObjectId();
                        }
                        else
                        {
                            // FCO object
                            sm = (StateManager) ObjectProviderFactory.newForPersistentNew(this, obj, preInsertChanges);
                            sm.makePersistent();
                            id = sm.getInternalObjectId();
                        }
                    }
                    else
                    {
                        if (sm.getReferencedPC() == null)
                        {
                            // Persist it
                            sm.makePersistent();
                            id = sm.getInternalObjectId();
                        }
                        else
                        {
                            // Being attached, so use the attached object
                            persistedPc = sm.getReferencedPC();
                        }
                    }
                }
            }
            else if (api.isPersistent(obj) && api.getIdForObject(obj) == null)
            {
                // Embedded/Serialised : have SM but no identity, allow persist in own right
                // Should we be making a copy of the object here ?
                if (NucleusLogger.PERSISTENCE.isDebugEnabled())
                {
                    NucleusLogger.PERSISTENCE.debug(LOCALISER.msg("010015", StringUtils.toJVMIDString(obj)));
                }
                StateManager sm = findStateManager(obj);
                sm.makePersistent();
                id = sm.getInternalObjectId();
            }
            else if (api.isDeleted(obj))
            {
                // Deleted : (re)-persist it (permitted in JPA, but not JDO - see StateManager)
                if (NucleusLogger.PERSISTENCE.isDebugEnabled())
                {
                    NucleusLogger.PERSISTENCE.debug(LOCALISER.msg("010015", StringUtils.toJVMIDString(obj)));
                }
                StateManager sm = findStateManager(obj);
                sm.makePersistent();
                id = sm.getInternalObjectId();
            }
            else
            {
                if (api.isPersistent(obj) && api.isTransactional(obj) && api.isDirty(obj) &&
                    isDelayDatastoreOperationsEnabled())
                {
                    // Object provisionally persistent (but not in datastore) so re-run reachability maybe
                    if (NucleusLogger.PERSISTENCE.isDebugEnabled())
                    {
                        NucleusLogger.PERSISTENCE.debug(LOCALISER.msg("010015", StringUtils.toJVMIDString(obj)));
                    }
                    StateManager sm = findStateManager(obj);
                    sm.makePersistent();
                    id = sm.getInternalObjectId();
                }
            }

            if (id != null && txCachedIds != null)
            {
                txCachedIds.add(id);
            }
            return persistedPc;
        }
        finally
        {
            clr.unsetPrimary();
        }
    }

    /**
     * Method to persist the passed object (internally).
     * @param pc The object
     * @param ownerSM StateManager of the owner when embedded
     * @param ownerFieldNum Field number in the owner where this is embedded (or -1 if not embedded)
     * @param objectType Type of object (see org.datanucleus.StateManager, e.g StateManager.PC)
     * @return The persisted object
     */
    public Object persistObjectInternal(Object pc, ObjectProvider ownerSM, int ownerFieldNum, int objectType)
    {
        if (ownerSM != null)
        {
            StateManager sm = findStateManager(ownerSM.getObject());
            return persistObjectInternal(pc, null, sm, ownerFieldNum, objectType);
        }
        else
        {
            return persistObjectInternal(pc, null, null, ownerFieldNum, objectType);
        }
    }

    public Object persistObjectInternal(Object pc, final FieldValues preInsertChanges, int objectType)
    {
        return persistObjectInternal(pc, preInsertChanges, null, -1, objectType);
    }

    /**
     * Method to delete an array of objects from the datastore.
     * @param objs The objects
     * @throws NucleusUserException Thrown if an error occurs during the deletion process.
     *     Any exception could have several nested exceptions for each failed object deletion
     */
    public void deleteObjects(Object[] objs)
    {
        if (objs == null)
        {
            return;
        }

        try
        {
            getStoreManager().getPersistenceHandler().batchStart(this, PersistenceBatchType.DELETE);

            ArrayList<RuntimeException> failures = null;
            for (int i=0;i<objs.length;i++)
            {
                try
                {
                    if (objs[i] != null)
                    {
                        deleteObjectWork(objs[i]);
                    }
                }
                catch (RuntimeException e)
                {
                    if (failures == null)
                    {
                        failures = new ArrayList();
                    }
                    failures.add(e);
                }
            }
            if (failures != null && !failures.isEmpty())
            {
                RuntimeException e = failures.get(0);
                if (e instanceof NucleusException && ((NucleusException)e).isFatal())
                {
                    // Should really check all and see if any are fatal not just first one
                    throw new NucleusFatalUserException(LOCALISER.msg("010040"), 
                        failures.toArray(new Exception[failures.size()]));
                }
                else
                {
                    throw new NucleusUserException(LOCALISER.msg("010040"), 
                        failures.toArray(new Exception[failures.size()]));
                }
            }
        }
        finally
        {
            getStoreManager().getPersistenceHandler().batchEnd(this, PersistenceBatchType.DELETE);

            if (!tx.isActive())
            {
                // Commit any non-tx changes
                processNontransactionalUpdate();
            }
        }
    }

    /**
     * Method to delete an object from the datastore.
     * NOT to be called by internal methods. Only callable by external APIs (JDO/JPA).
     * @param obj The object
     */
    public void deleteObject(Object obj)
    {
        if (obj == null)
        {
            return;
        }

        try
        {
            deleteObjectWork(obj);
        }
        finally
        {
            if (!tx.isActive())
            {
                // Commit any non-tx changes
                processNontransactionalUpdate();
            }
        }
    }

    /**
     * Method to delete an object from the datastore.
     * NOT to be called by internal methods. Only callable by external APIs (JDO/JPA).
     * @param obj The object
     */
    void deleteObjectWork(Object obj)
    {
        StateManager sm = findStateManager(obj);
        if (sm == null && getApiAdapter().isDetached(obj))
        {
            // Delete of detached, so find a managed attached version and delete that
            Object attachedObj = findObject(getApiAdapter().getIdForObject(obj), true, false, obj.getClass().getName());
            sm = findStateManager(attachedObj);
        }
        if (sm != null)
        {
            // Add the object to the relevant list of dirty StateManagers
            if (indirectDirtySMs.contains(sm))
            {
                // Object is dirty indirectly, but now user-requested so move to direct list of dirty objects
                indirectDirtySMs.remove(sm);
                dirtySMs.add(sm);
            }
            else if (!dirtySMs.contains(sm))
            {
                dirtySMs.add(sm);
                if (txCachedIds != null)
                {
                    txCachedIds.add(sm.getInternalObjectId());
                }
            }
        }

        // Delete the object
        deleteObjectInternal(obj);

        if (getReachabilityAtCommit() && tx.isActive())
        {
            if (sm != null)
            {
                if (getApiAdapter().isDeleted(obj))
                {
                    txKnownDeletedIds.add(sm.getInternalObjectId());
                }
            }
        }
    }

    /**
     * Method to delete an object from persistence which should be called from internal calls only.
     * All PM/EM calls should go via deleteObject(Object obj).
     * @param obj Object to delete
     */
    public void deleteObjectInternal(Object obj)
    {
        if (obj == null)
        {
            return;
        }

        try
        {
            clr.setPrimary(obj.getClass().getClassLoader());
            assertClassPersistable(obj.getClass());

            Object pc = obj;
            if (getApiAdapter().isDetached(obj))
            {
                // Load up the attached instance with this identity
                pc = findObject(getApiAdapter().getIdForObject(obj), true, true, null);
            }

            if (NucleusLogger.PERSISTENCE.isDebugEnabled())
            {
                NucleusLogger.PERSISTENCE.debug(LOCALISER.msg("010019", StringUtils.toJVMIDString(pc)));
            }

            // Check that the object is valid for deleting
            if (getApiAdapter().getName().equals("JDO"))
            {
                // JDO doesn't allow deletion of transient
                if (!getApiAdapter().isPersistent(pc) && !getApiAdapter().isTransactional(pc))
                {
                    throw new NucleusUserException(LOCALISER.msg("010020"));
                }
                else if (!getApiAdapter().isPersistent(pc) && getApiAdapter().isTransactional(pc))
                {
                    throw new NucleusUserException(LOCALISER.msg("010021"));
                }
            }

            // Delete it
            StateManager sm = findStateManager(pc);
            if (sm == null)
            {
                if (!getApiAdapter().allowDeleteOfNonPersistentObject())
                {
                    // Not permitted by the API
                    throw new NucleusUserException(LOCALISER.msg("010007", getApiAdapter().getIdForObject(pc)));
                }

                // Put StateManager around object so it is P_NEW (unpersisted), then P_NEW_DELETED soon after
                sm = (StateManager) ObjectProviderFactory.newForPNewToBeDeleted(this, pc);
            }

            if (txCachedIds != null)
            {
                // Mark for L2 cache update
                txCachedIds.add(sm.getInternalObjectId());
            }

            // Move to deleted state
            sm.deletePersistent();
        }
        finally
        {
            clr.unsetPrimary();
        }
    }

    /**
     * Method to migrate an object to transient state.
     * @param obj The object
     * @param state Object containing the state of the fetch plan process (if any)
     * @throws NucleusException When an error occurs in making the object transient
     */
    public void makeObjectTransient(Object obj, FetchPlanState state)
    {
        if (obj == null)
        {
            return;
        }

        try
        {
            clr.setPrimary(obj.getClass().getClassLoader());
            assertClassPersistable(obj.getClass());
            assertNotDetached(obj);

            if (NucleusLogger.PERSISTENCE.isDebugEnabled())
            {
                NucleusLogger.PERSISTENCE.debug(LOCALISER.msg("010022", StringUtils.toJVMIDString(obj)));
            }

            if (getApiAdapter().isPersistent(obj))
            {
                StateManager sm = findStateManager(obj);
                sm.makeTransient(state);
            }
        }
        finally
        {
            clr.unsetPrimary();
        }
    }

    /**
     * Method to make an object transactional.
     * @param obj The object
     * @throws NucleusException Thrown when an error occurs
     */
    public void makeObjectTransactional(Object obj)
    {
        if (obj == null)
        {
            return;
        }

        try
        {
            clr.setPrimary(obj.getClass().getClassLoader());
            assertClassPersistable(obj.getClass());
            assertNotDetached(obj);

            if (getApiAdapter().isPersistent(obj))
            {
                assertActiveTransaction();
            }
            StateManager sm = findStateManager(obj);
            if (sm == null)
            {
                sm = (StateManager) ObjectProviderFactory.newForTransactionalTransient(this, obj);
            }
            sm.makeTransactional();
        }
        finally
        {
            clr.unsetPrimary();
        }
    }

    /**
     * Method to make an object nontransactional.
     * @param obj The object
     */
    public void makeObjectNontransactional(Object obj)
    {
        if (obj == null)
        {
            return;
        }

        try
        {
            clr.setPrimary(obj.getClass().getClassLoader());
            assertClassPersistable(obj.getClass());
            if (!getApiAdapter().isPersistent(obj) && getApiAdapter().isTransactional(obj) && getApiAdapter().isDirty(obj))
            {
                throw new NucleusUserException(LOCALISER.msg("010024"));
            }

            StateManager sm = findStateManager(obj);
            sm.makeNontransactional();
        }
        finally
        {
            clr.unsetPrimary();
        }
    }

    /* (non-Javadoc)
     * @see org.datanucleus.store.ExecutionContext#getObjectProviderOfOwnerForAttachingObject(java.lang.Object)
     */
    public ObjectProvider getObjectProviderOfOwnerForAttachingObject(Object pc)
    {
        ThreadContextInfo threadInfo = acquireThreadContextInfo();
        try
        {
            if (threadInfo.attachedOwnerByObject == null)
            {
                return null;
            }
            return threadInfo.attachedOwnerByObject.get(pc);
        }
        finally
        {
            releaseThreadContextInfo();
        }
    }

    /**
     * Method to attach a persistent detached object.
     * If a different object with the same identity as this object exists in the L1 cache then an exception
     * will be thrown.
     * @param ownerOP ObjectProvider of the owner object that has this in a field that causes this attach
     * @param pc The persistable object
     * @param sco Whether the PC object is stored without an identity (embedded/serialised)
     */
    public void attachObject(ObjectProvider ownerOP, Object pc, boolean sco)
    {
        assertIsOpen();
        assertClassPersistable(pc.getClass());

        // Store the owner for this persistable object being attached
        Map attachedOwnerByObject = getThreadContextInfo().attachedOwnerByObject; // For the current thread
        if (attachedOwnerByObject != null)
        {
            attachedOwnerByObject.put(pc, ownerOP);
        }

        ApiAdapter api = getApiAdapter();
        Object id = api.getIdForObject(pc);
        if (id != null && isInserting(pc))
        {
            // Object is being inserted in this transaction so just return
            return;
        }
        else if (id == null && !sco)
        {
            // Transient object so needs persisting
            persistObjectInternal(pc, null, null, -1, ObjectProvider.PC);
            return;
        }

        if (api.isDetached(pc))
        {
            // Detached, so migrate to attached
            if (cache != null)
            {
                StateManager l1CachedSM = (StateManager)cache.get(id);
                if (l1CachedSM != null && l1CachedSM.getObject() != pc)
                {
                    // attached object with the same id already present in the L1 cache so cannot attach in-situ
                    throw new NucleusUserException(LOCALISER.msg("010017",
                        StringUtils.toJVMIDString(pc)));
                }
            }

            if (NucleusLogger.PERSISTENCE.isDebugEnabled())
            {
                NucleusLogger.PERSISTENCE.debug(LOCALISER.msg("010016", StringUtils.toJVMIDString(pc)));
            }
            StateManager sm = 
                (StateManager) ObjectProviderFactory.newForDetached(this, pc, id, api.getVersionForObject(pc));
            sm.attach(sco);
        }
        else
        {
            // Not detached so can't attach it. Just return
            return;
        }
    }

    /**
     * Method to attach a persistent detached object returning an attached copy of the object.
     * If the object is of class that is not detachable, a ClassNotDetachableException will be thrown.
     * @param ownerOP ObjectProvider of the owner object that has this in a field that causes this attach
     * @param pc The object
     * @param sco Whether it has no identity (second-class object)
     * @return The attached object
     */
    public Object attachObjectCopy(ObjectProvider ownerOP, Object pc, boolean sco)
    {
        assertIsOpen();
        assertClassPersistable(pc.getClass());
        assertDetachable(pc);

        // Store the owner for this persistable object being attached
        Map attachedOwnerByObject = getThreadContextInfo().attachedOwnerByObject; // For the current thread
        if (attachedOwnerByObject != null)
        {
            attachedOwnerByObject.put(pc, ownerOP);
        }

        ApiAdapter api = getApiAdapter();
        Object id = api.getIdForObject(pc);
        if (id != null && isInserting(pc))
        {
            // Object is being inserted in this transaction
            return pc;
        }
        else if (id == null && !sco)
        {
            // Object was not persisted before so persist it
            return persistObjectInternal(pc, null, null, -1, ObjectProvider.PC);
        }
        else if (api.isPersistent(pc))
        {
            // Already persistent hence can't be attached
            return pc;
        }

        // Object should exist in this datastore now
        Object pcTarget = null;
        if (sco)
        {
            // SCO PC (embedded/serialised)
            boolean detached = getApiAdapter().isDetached(pc);
            StateManager smTarget = (StateManager) ObjectProviderFactory.newForEmbedded(this, pc, true, null, -1);
            pcTarget = smTarget.getObject();
            if (detached)
            {
                // If the object is detached, re-attach it
                if (NucleusLogger.PERSISTENCE.isDebugEnabled())
                {
                    NucleusLogger.PERSISTENCE.debug(LOCALISER.msg("010018", 
                        StringUtils.toJVMIDString(pc), StringUtils.toJVMIDString(pcTarget)));
                }

                smTarget.attachCopy(pc, sco);
            }
        }
        else
        {
            // FCO PC
            boolean detached = getApiAdapter().isDetached(pc);
            pcTarget = findObject(id, false, false, pc.getClass().getName());
            if (detached)
            {
                Object obj = null;
                Map attachedPCById = getThreadContextInfo().attachedPCById; // For the current thread
                if (attachedPCById != null) // Only used by persistObject process
                {
                    obj = attachedPCById.get(getApiAdapter().getIdForObject(pc));
                }
                if (obj != null)
                {
                    pcTarget = obj;
                }
                else
                {
                    // If the object is detached, re-attach it
                    if (NucleusLogger.PERSISTENCE.isDebugEnabled())
                    {
                        NucleusLogger.PERSISTENCE.debug(LOCALISER.msg("010018", 
                            StringUtils.toJVMIDString(pc), StringUtils.toJVMIDString(pcTarget)));
                    }
                    pcTarget = findStateManager(pcTarget).attachCopy(pc, sco);

                    // Save the detached-attached PCs for later reference
                    if (attachedPCById != null) // Only used by persistObject process
                    {
                        attachedPCById.put(getApiAdapter().getIdForObject(pc), pcTarget);
                    }
                }
            }
        }

        return pcTarget;
    }

    /**
     * Method to detach a persistent object without making a copy. Note that 
     * also all the objects which are refered to from this object are detached.
     * If the object is of class that is not detachable a ClassNotDetachableException
     * will be thrown. If the object is not persistent a NucleusUserException is thrown.
     * <B>For internal use only</B>
     * @param obj The object
     * @param state State for the detachment process
     */
    public void detachObject(Object obj, FetchPlanState state)
    {
        assertIsOpen();
        assertClassPersistable(obj.getClass());
        assertDetachable(obj); // Is this required?

        if (getApiAdapter().isDetached(obj))
        {
            return;
        }

        if (!getApiAdapter().isPersistent(obj))
        {
            // Transient object passed so persist it before thinking about detaching
            if (tx.isActive())
            {
                persistObjectInternal(obj, null, null, -1, ObjectProvider.PC);
            }
        }

        StateManager sm = findStateManager(obj);
        if (sm == null)
        {
            throw new NucleusUserException(LOCALISER.msg("010007", getApiAdapter().getIdForObject(obj)));
        }
        sm.detach(state);

        // Clear any changes from this since it is now detached
        if (dirtySMs.contains(sm) || indirectDirtySMs.contains(sm))
        {
            NucleusLogger.GENERAL.info(LOCALISER.msg("010047", StringUtils.toJVMIDString(obj)));
            clearDirty(sm);
        }
    }

    /**
     * Detach a copy of the passed persistent object using the provided detach state.
     * If the object is of class that is not detachable it will be detached as transient.
     * If it is not yet persistent it will be first persisted.
     * @param pc The object
     * @param state State for the detachment process
     * @return The detached object
     */
    public Object detachObjectCopy(Object pc, FetchPlanState state)
    {
        assertIsOpen();
        assertClassPersistable(pc.getClass());

        Object thePC = pc;
        try
        {
            clr.setPrimary(pc.getClass().getClassLoader());
            if (!getApiAdapter().isPersistent(pc) && !getApiAdapter().isDetached(pc))
            {
                // Transient object passed so persist it before thinking about detaching
                if (tx.isActive())
                {
                    thePC = persistObjectInternal(pc, null, null, -1, ObjectProvider.PC);
                }
                else
                {
                    // JDO2 [12.6.8] "If a detachCopy method is called outside an active transaction, the reachability 
                    // algorithm will not be run; if any transient instances are reachable via persistent fields, a 
                    // XXXUserException is thrown for each persistent instance containing such fields.
                    throw new NucleusUserException(LOCALISER.msg("010014"));
                }
            }

            if (getApiAdapter().isDetached(thePC))
            {
                // Passing in a detached (dirty or clean) instance, so get a persistent copy to detach
                thePC = findObject(getApiAdapter().getIdForObject(thePC), false, true, null);
            }

            StateManager sm = findStateManager(thePC);
            if (sm == null)
            {
                throw new NucleusUserException(LOCALISER.msg("010007", getApiAdapter().getIdForObject(thePC)));
            }

            return sm.detachCopy(state);
        }
        finally
        {
            clr.unsetPrimary();
        }
    }

    /**
     * Method to detach all objects in the ObjectManager.
     * Detaches all objects enlisted as well as all objects in the L1 cache.
     * Of particular use with JPA when doing a clear of the persistence context.
     */
    public void detachAll()
    {
        Collection<StateManager> smsToDetach = new HashSet();
        smsToDetach.addAll(this.enlistedSMCache.values());
        if (cache != null)
        {
            smsToDetach.addAll(this.cache.values());
        }

        FetchPlanState fps = new FetchPlanState();
        Iterator<StateManager> iter = smsToDetach.iterator();
        while (iter.hasNext())
        {
            iter.next().detach(fps);
        }
    }

    // ----------------------------- New Instances ----------------------------------

    /**
     * Method to generate an instance of an interface, abstract class, or concrete PC class.
     * @param cls The class of the interface or abstract class, or concrete class defined in MetaData
     * @return The instance of this type
     */
    public Object newInstance(Class cls)
    {
        assertIsOpen();

        if (getApiAdapter().isPersistable(cls) && !Modifier.isAbstract(cls.getModifiers()))
        {
            // Concrete PC class so instantiate here
            try
            {
                return cls.newInstance();
            }
            catch (IllegalAccessException iae)
            {
                throw new NucleusUserException(iae.toString(), iae);
            }
            catch (InstantiationException ie)
            {
                throw new NucleusUserException(ie.toString(), ie);
            }
        }

        // Use ImplementationCreator
        assertHasImplementationCreator();
        return getNucleusContext().getImplementationCreator().newInstance(cls, clr);
    }

    // ----------------------------- Object Retrieval by Id ----------------------------------

    /**
     * Method to return if the specified object exists in the datastore.
     * @param obj The (persistable) object
     * @return Whether it exists
     */
    public boolean exists(Object obj)
    {
        if (obj == null)
        {
            return false;
        }

        Object id = getApiAdapter().getIdForObject(obj);
        if (id == null)
        {
            return false;
        }

        try
        {
            findObject(id, true, false, obj.getClass().getName());
        }
        catch (NucleusObjectNotFoundException onfe)
        {
            return false;
        }

        return true;
    }

    /**
     * Accessor for the currently managed objects for the current transaction.
     * If the transaction is not active this returns null.
     * @return Collection of managed objects enlisted in the current transaction
     */
    public Set getManagedObjects()
    {
        if (!tx.isActive())
        {
            return null;
        }

        Set objs = new HashSet();
        Collection sms = enlistedSMCache.values();
        Iterator<StateManager> smsIter = sms.iterator();
        while (smsIter.hasNext())
        {
            StateManager sm = smsIter.next();
            objs.add(sm.getObject());
        }
        return objs;
    }

    /**
     * Accessor for the currently managed objects for the current transaction.
     * If the transaction is not active this returns null.
     * @param classes Classes that we want the enlisted objects for
     * @return Collection of managed objects enlisted in the current transaction
     */
    public Set getManagedObjects(Class[] classes)
    {
        if (!tx.isActive())
        {
            return null;
        }

        Set objs = new HashSet();
        Collection sms = enlistedSMCache.values();
        Iterator<StateManager> smsIter = sms.iterator();
        while (smsIter.hasNext())
        {
            StateManager sm = smsIter.next();
            for (int i=0;i<classes.length;i++)
            {
                if (classes[i] == sm.getObject().getClass())
                {
                    objs.add(sm.getObject());
                    break;
                }
            }
        }
        return objs;
    }

    /**
     * Accessor for the currently managed objects for the current transaction.
     * If the transaction is not active this returns null.
     * @param states States that we want the enlisted objects for
     * @return Collection of managed objects enlisted in the current transaction
     */
    public Set getManagedObjects(String[] states)
    {
        if (!tx.isActive())
        {
            return null;
        }

        Set objs = new HashSet();
        Collection sms = enlistedSMCache.values();
        Iterator<StateManager> smsIter = sms.iterator();
        while (smsIter.hasNext())
        {
            StateManager sm = smsIter.next();
            for (int i=0;i<states.length;i++)
            {
                if (getApiAdapter().getObjectState(sm.getObject()).equals(states[i]))
                {
                    objs.add(sm.getObject());
                    break;
                }
            }
        }
        return objs;
    }

    /**
     * Accessor for the currently managed objects for the current transaction.
     * If the transaction is not active this returns null.
     * @param states States that we want the enlisted objects for
     * @param classes Classes that we want the enlisted objects for
     * @return Collection of managed objects enlisted in the current transaction
     */
    public Set getManagedObjects(String[] states, Class[] classes)
    {
        if (!tx.isActive())
        {
            return null;
        }

        Set objs = new HashSet();
        Collection sms = enlistedSMCache.values();
        Iterator<StateManager> smsIter = sms.iterator();
        while (smsIter.hasNext())
        {
            boolean matches = false;
            StateManager sm = smsIter.next();
            for (int i=0;i<states.length;i++)
            {
                if (getApiAdapter().getObjectState(sm.getObject()).equals(states[i]))
                {
                    for (int j=0;i<classes.length;i++)
                    {
                        if (classes[j] == sm.getObject().getClass())
                        {
                            matches = true;
                            objs.add(sm.getObject());
                            break;
                        }
                    }
                }
                if (matches)
                {
                    break;
                }
            }
        }
        return objs;
    }

    /**
     * Accessor for the StateManager of an object given the object AID.
     * @param pcType Type of the PC object
     * @param fv The field values to be loaded
     * @param ignoreCache true if it must ignore the cache
     * @param checkInheritance Whether look to the database to determine which
     * class this object is. This parameter is a hint. Set false, if it's
     * already determined the correct pcClass for this pc "object" in a certain
     * level in the hierarchy. Set to true and it will look to the database.
     * @return Object
     * @deprecated Get hold of the application identity and use a different findObject method using that
     */
    public Object findObjectUsingAID(Type pcType, final FieldValues fv, boolean ignoreCache, boolean checkInheritance)
    {
        assertIsOpen();

        Class pcClass = pcType.getType();

        // Create StateManager to generate an identity NOTE THIS IS VERY INEFFICIENT
        StateManager sm = (StateManager) ObjectProviderFactory.newForHollowPopulatedAppId(this, pcClass, fv);
        if (!ignoreCache)
        {
            // Check the cache
            Object oid = sm.getInternalObjectId();
            Object pc = getObjectFromCache(oid);
            if (pc != null)
            {
                sm = findStateManager(pc);
                // Note that this can cause problems like NUCRDBMS-402 due to attempt to re-read the field values
                sm.loadFieldValues(fv); // Load the values retrieved by the query
                return pc;
            }
            if (checkInheritance)
            {
                ApiAdapter api = getApiAdapter();
                if (oid instanceof OID || api.isSingleFieldIdentity(oid))
                {
                    // Check if this id for any known subclasses is in the cache to save searching
                    String[] subclasses = getMetaDataManager().getSubclassesForClass(pcClass.getName(), true);
                    if (subclasses != null)
                    {
                        for (int i=0;i<subclasses.length;i++)
                        {
                            if (api.isDatastoreIdentity(oid))
                            {
                                oid = OIDFactory.getInstance(getNucleusContext(), subclasses[i], ((OID)oid).getKeyValue());
                            }
                            else if (api.isSingleFieldIdentity(oid))
                            {
                                oid = api.getNewSingleFieldIdentity(oid.getClass(), clr.classForName(subclasses[i]), 
                                    api.getTargetKeyForSingleFieldIdentity(oid));
                            }
                            pc = getObjectFromCache(oid);
                            if (pc != null)
                            {
                                sm = findStateManager(pc);
                                sm.loadFieldValues(fv); // Load the values retrieved by the query
                                putObjectIntoLevel2Cache(sm, false);
                                return pc;
                            }
                        }
                    }
                }
            }
        }

        if (checkInheritance)
        {
            sm.checkInheritance(fv); // Find the correct PC class for this object, hence updating the object id
            if (!ignoreCache)
            {
                // Check the cache in case this updated object id is present (since we should use that if available)
                Object oid = sm.getInternalObjectId();
                Object pc = getObjectFromCache(oid);
                if (pc != null)
                {
                    // We have an object with this new object id already so return it with the retrieved field values imposed
                    sm = findStateManager(pc);
                    sm.loadFieldValues(fv); // Load the values retrieved by the query
                    putObjectIntoLevel2Cache(sm, false);
                    return pc;
                }
            }
        }

        // Cache the object as required
        putObjectIntoCache(sm);
        if (txCachedIds != null && !txCachedIds.contains(sm.getInternalObjectId()))
        {
            putObjectIntoLevel2Cache(sm, false);
        }

        return sm.getObject();
    }

    /**
     * Accessor for an object given the object id. 
     * @param id Id of the object.
     * @param fv Field values for the object
     * @param cls the type which the object is (optional). Used to instantiate the object
     * @param ignoreCache true if it must ignore the cache
     * @return The Object
     */
    public Object findObject(Object id, FieldValues fv, Class cls, boolean ignoreCache)
    {
        assertIsOpen();

        boolean createdHollow = false;
        Object pc = null;
        if (!ignoreCache)
        {
            pc = getObjectFromCache(id);
        }

        if (pc == null)
        {
            // Find direct from the store if supported. NOTE : This ignores the provided FieldValues!
            pc = getStoreManager().getPersistenceHandler().findObject(this, id);
        }

        if (pc == null)
        {
            String className = (cls != null ? cls.getName() : null);
            if (cls == null)
            {
                // Try to derive the class name from the id, since not provided
                className = getStoreManager().getClassNameForObjectID(id, clr, this);
                if (className == null)
                {
                    throw new NucleusObjectNotFoundException(LOCALISER.msg("010026"), id);
                }
                if (id instanceof OID)
                {
                    // Try again using the derived class name
                    id = OIDFactory.getInstance(getNucleusContext(), className, ((OID)id).getKeyValue());
                    pc = getObjectFromCache(id);
                }
            }

            if (pc == null)
            {
                // Still not found so create a Hollow instance with the supplied field values
                if (cls == null)
                {
                    try
                    {
                        cls = clr.classForName(className, id.getClass().getClassLoader());
                    }
                    catch (ClassNotResolvedException e)
                    {
                        String msg = LOCALISER.msg("010027", getIdentityAsString(id));
                        NucleusLogger.PERSISTENCE.warn(msg);
                        throw new NucleusUserException(msg, e);
                    }
                }

                createdHollow = true;
                StateManager sm = (StateManager) ObjectProviderFactory.newForHollowPopulated(this, cls, id, fv);
                pc = sm.getObject();
                putObjectIntoCache(sm);
                putObjectIntoLevel2Cache(sm, false);
            }
        }

        if (pc != null && fv != null && !createdHollow)
        {
            // Object found in the cache so load the requested fields
            StateManager sm = findStateManager(pc);
            if (sm != null)
            {
                // Load the requested fields
                fv.fetchNonLoadedFields(sm);
            }
        }

        return pc;
    }

    /**
     * Accessor for objects with the specified identities.
     * @param identities Ids of the object(s).
     * @param validate Whether to validate the object state
     * @return The Objects with these ids (same order)
     * @throws NucleusObjectNotFoundException if an object doesn't exist in the datastore
     */
    public Object[] findObjects(Object[] identities, boolean validate)
    {
        assertIsOpen();

        ApiAdapter api = getApiAdapter();
        Object[] objs = new Object[identities.length];
        Object[] ids = new Object[identities.length];
        List idsToFind = new ArrayList();
        for (int i=0;i<identities.length;i++)
        {
            if (identities[i] == null)
            {
                throw new NucleusUserException(LOCALISER.msg("010044"));
            }

            // Translate the identity if required
            if (getNucleusContext().getIdentityStringTranslator() != null && identities[i] instanceof String)
            {
                // DataNucleus extension to translate input identities into valid persistent identities.
                IdentityStringTranslator translator = getNucleusContext().getIdentityStringTranslator();
                ids[i] = translator.getIdentity(this, (String)identities[i]);
            }
            else
            {
                ids[i] = identities[i];
            }

            // Find the object in the cache if possible
            Object pc = getObjectFromCache(ids[i]);
            if (pc != null)
            {
                objs[i] = pc;
                if (ids[i] instanceof SCOID)
                {
                    if (api.isPersistent(pc) && !api.isNew(pc) && !api.isDeleted(pc) && !api.isTransactional(pc))
                    {
                        // JDO2 [5.4.4] Can't return HOLLOW nondurable objects
                        throw new NucleusUserException(LOCALISER.msg("010005"));
                    }
                }
            }
            else
            {
                idsToFind.add(ids[i]);
            }
        }

        // Try to find missing objects direct from the datastore if supported by the StoreManager
        Object[] foundPcs = null;
        foundPcs = getStoreManager().getPersistenceHandler().findObjects(this, idsToFind.toArray());

        int foundPcIdx = 0;

        for (int i=0;i<ids.length;i++)
        {
            Object id = ids[i];
            Object pc = objs[i];
            boolean fromCache = true;
            if (pc == null)
            {
                // Try the "findObjects" results in case supported by StoreManager
                pc = foundPcs[foundPcIdx];
                foundPcIdx++;
            }

            StateManager sm = null;
            if (pc == null)
            {
                // Object not found yet, so work out class name
                String className = null;
                String originalClassName = null;
                boolean checkedClassName = false;
                if (id instanceof SCOID)
                {
                    throw new NucleusUserException(LOCALISER.msg("010006"));
                }
                else if (id instanceof DatastoreUniqueOID)
                {
                    // Should have been found using "persistenceHandler.findObject()"
                    throw new NucleusObjectNotFoundException(LOCALISER.msg("010026"), id);
                }
                else if (api.isDatastoreIdentity(id) || api.isSingleFieldIdentity(id))
                {
                    // OID or SingleFieldIdentity, so check that the implied class is managed
                    originalClassName = getStoreManager().manageClassForIdentity(id, clr);
                }
                else
                {
                    // We dont know the object class so try to deduce it from what is known by the StoreManager
                    originalClassName = getStoreManager().getClassNameForObjectID(id, clr, this);
                    checkedClassName = true;
                }

                if (validate)
                {
                    // Validate the inheritance level
                    className =
                        (checkedClassName ? originalClassName : getStoreManager().getClassNameForObjectID(id, clr, this));
                    if (className == null)
                    {
                        throw new NucleusObjectNotFoundException(LOCALISER.msg("010026"), id);
                    }

                    if (originalClassName != null && !originalClassName.equals(className))
                    {
                        // Inheritance check implies different inheritance level, so retry
                        if (api.isDatastoreIdentity(id))
                        {
                            // Create new OID using correct target class, and recheck cache
                            id = OIDFactory.getInstance(getNucleusContext(), className, ((OID)id).getKeyValue());
                            pc = getObjectFromCache(id);
                        }
                        else if (api.isSingleFieldIdentity(id))
                        {
                            // Create new SingleFieldIdentity using correct targetClass, and recheck cache
                            id = api.getNewSingleFieldIdentity(id.getClass(), clr.classForName(className), 
                                api.getTargetKeyForSingleFieldIdentity(id));
                            pc = getObjectFromCache(id);
                        }
                    }
                }
                else
                {
                    className = originalClassName;
                }

                if (pc == null)
                {
                    // Still not found so create a Hollow instance with the supplied field values
                    try
                    {
                        Class pcClass = clr.classForName(className, (id instanceof OID) ? null : id.getClass().getClassLoader());
                        sm = (StateManager) ObjectProviderFactory.newForHollow(this, pcClass, id);
                        pc = sm.getObject();
                        fromCache = false;
                    }
                    catch (ClassNotResolvedException e)
                    {
                        NucleusLogger.PERSISTENCE.warn(LOCALISER.msg("010027", getIdentityAsString(id)));
                        throw new NucleusUserException(LOCALISER.msg("010027", getIdentityAsString(id)), e);
                    }
                }
            }

            // TODO Perform locate of uncached objects in single operation using persistenceHandler.locateObjects
            boolean performValidationWhenCached = 
                (context.getPersistenceConfiguration().getBooleanProperty("datanucleus.findObject.validateWhenCached"));
            if (validate && (!fromCache || performValidationWhenCached))
            {
                if (fromCache && pc != null && api.isTransactional(pc))
                {
                    // JDO2 [12.6.5] Already an object with the same id and it's transactional, so use it
                    objs[i] = pc;
                    continue;
                }

                // User requests validation of the instance so go to the datastore to validate it
                // loading any fetchplan fields that are needed in the process.
                sm = findStateManager(pc);

                if (sm != null && !fromCache)
                {
                    // Cache the object in case we have bidirectional relations that would need to find this
                    putObjectIntoCache(sm);
                }

                try
                {
                    sm.validate();
                }
                catch (NucleusObjectNotFoundException onfe)
                {
                    // Object doesn't exist, so remove from L1 cache
                    removeObjectFromCache(sm.getInternalObjectId());
                    throw onfe;
                }

                if (sm.getObject() != pc)
                {
                    // Underlying object was changed in the validation process
                    // Can happen when datastore is responsible for managing object refs e.g db4o
                    // and needs to create the objects itself
                    removeObjectFromCache(sm.getInternalObjectId());
                    fromCache = false;
                    pc = sm.getObject();
                }
            }

            objs[i] = pc;

            if (sm != null && !fromCache)
            {
                // Cache the object (update it if already present)
                putObjectIntoCache(sm);
                putObjectIntoLevel2Cache(sm, false);
            }
        }

        return objs;
    }

    /**
     * Accessor for an object given the object id. If validate is false, we return the object
     * if found in the cache, or otherwise a Hollow object with that id. If validate is true
     * we check with the datastore and return an object with the FetchPlan fields loaded.
     * @param id Id of the object.
     * @param validate Whether to validate the object state
     * @param checkInheritance Whether look to the database to determine which class this object is.
     * @param objectClassName Class name for the object with this id (if known, optional)
     * @return The Object with this id
     * @throws NucleusObjectNotFoundException if the object doesn't exist in the datastore
     */
    public Object findObject(Object id, boolean validate, boolean checkInheritance, String objectClassName)
    {
        assertIsOpen();

        if (id == null)
        {
            throw new NucleusUserException(LOCALISER.msg("010044"));
        }

        IdentityStringTranslator translator = getNucleusContext().getIdentityStringTranslator();
        if (translator != null && id instanceof String)
        {
            // DataNucleus extension to translate input identities into valid persistent identities.
            id = translator.getIdentity(this, (String)id);
        }

        // try to find object in cache(s)
        Object pc = getObjectFromCache(id);
        boolean fromCache = true;
        ApiAdapter api = getApiAdapter();
        if (id instanceof SCOID && pc != null)
        {
            if (api.isPersistent(pc) && !api.isNew(pc) && !api.isDeleted(pc) && !api.isTransactional(pc))
            {
                // JDO2 [5.4.4] Cant return HOLLOW nondurable objects
                throw new NucleusUserException(LOCALISER.msg("010005"));
            }
        }

        if (pc != null && api.isTransactional(pc))
        {
            // JDO2 [12.6.5] If there's already an object with the same id and it's transactional, return it
            return pc;
        }

        StateManager sm = null;
        if (pc == null)
        {
            // Find it direct from the store if the store supports that
            pc = getStoreManager().getPersistenceHandler().findObject(this, id);

            if (pc == null)
            {
                // Object not found in cache(s) with this identity
                String className = null;
                String originalClassName = null;
                boolean checkedClassName = false;
                if (id instanceof SCOID)
                {
                    throw new NucleusUserException(LOCALISER.msg("010006"));
                }
                else if (id instanceof DatastoreUniqueOID)
                {
                    throw new NucleusObjectNotFoundException(LOCALISER.msg("010026"), id);
                }
                else if (api.isDatastoreIdentity(id) || api.isSingleFieldIdentity(id))
                {
                    // DatastoreIdentity or SingleFieldIdentity, so check that the implied class is managed
                    originalClassName = getStoreManager().manageClassForIdentity(id, clr);
                }
                else if (objectClassName != null)
                {
                    // Object class name specified so use that directly
                    originalClassName = objectClassName;
                }
                else
                {
                    // We dont know the object class so try to deduce it from what is known by the StoreManager
                    originalClassName = getStoreManager().getClassNameForObjectID(id, clr, this);
                    checkedClassName = true;
                }

                if (checkInheritance)
                {
                    // Verify if correct class inheritance level is set
                    if (!checkedClassName)
                    {
                        className = getStoreManager().getClassNameForObjectID(id, clr, this);
                    }
                    else
                    {
                        // We just checked the name of the class in the section above so just use that
                        className = originalClassName;
                    }

                    if (className == null)
                    {
                        throw new NucleusObjectNotFoundException(LOCALISER.msg("010026"), id);
                    }

                    if (originalClassName != null && !originalClassName.equals(className))
                    {
                        // Inheritance checking has found a different inherited
                        // object with this identity so create new id
                        if (api.isDatastoreIdentity(id))
                        {
                            // Create new OID using correct target class and recheck the cache
                            id = OIDFactory.getInstance(getNucleusContext(), className, ((OID)id).getKeyValue());
                            pc = getObjectFromCache(id);
                        }
                        else if (api.isSingleFieldIdentity(id))
                        {
                            // Create new SingleFieldIdentity using correct targetClass and recheck the cache
                            id = api.getNewSingleFieldIdentity(id.getClass(), clr.classForName(className), 
                                    api.getTargetKeyForSingleFieldIdentity(id));
                            pc = getObjectFromCache(id);
                        }
                    }
                }
                else
                {
                    className = originalClassName;
                }

                if (pc == null)
                {
                    // Still not found, so create a Hollow instance with supplied PK values if possible
                    try
                    {
                        Class pcClass = clr.classForName(className, (id instanceof OID) ? null : id.getClass().getClassLoader());
                        if (Modifier.isAbstract(pcClass.getModifiers()))
                        {
                            // This class is abstract so impossible to have an instance of this type
                            throw new NucleusObjectNotFoundException(LOCALISER.msg("010027", 
                                getIdentityAsString(id), className));
                        }

                        sm = (StateManager) ObjectProviderFactory.newForHollow(this, pcClass, id);
                        pc = sm.getObject();
                        fromCache = false;

                        if (!checkInheritance && !validate)
                        {
                            // Mark the StateManager as needing to validate this object before loading fields
                            sm.markForInheritanceValidation();
                        }
                    }
                    catch (ClassNotResolvedException e)
                    {
                        NucleusLogger.PERSISTENCE.warn(LOCALISER.msg("010027", getIdentityAsString(id)));
                        throw new NucleusUserException(LOCALISER.msg("010027", getIdentityAsString(id)), e);
                    }
                }
            }
        }

        // TODO If we have serializeReadObjects set and in pessimistic txn and get the object from the cache
        // we need to apply a lock here
        boolean performValidationWhenCached = (context.getPersistenceConfiguration().getBooleanProperty("datanucleus.findObject.validateWhenCached"));
        if (validate && (!fromCache || performValidationWhenCached))
        {
            // User requests validation of the instance so go to the datastore to validate it
            // loading any fetchplan fields that are needed in the process.
            if (sm == null)
            {
                sm = findStateManager(pc);
            }

            if (sm != null && !fromCache)
            {
                // Cache the object in case we have bidirectional relations that would need to find this
                putObjectIntoCache(sm);
            }

            try
            {
                sm.validate();
            }
            catch (NucleusObjectNotFoundException onfe)
            {
                // Object doesn't exist, so remove from L1 cache
                removeObjectFromCache(sm.getInternalObjectId());
                throw onfe;
            }

            if (sm.getObject() != pc)
            {
                // Underlying object was changed in the validation process. This can happen when the datastore
                // is responsible for managing object references and it no longer recognises the cached value.
                fromCache = false;
                removeObjectFromCache(sm.getInternalObjectId());
            }
            if (!fromCache)
            {
                // We created a Hollow PC earlier but then went to the datastore and let it find the real object
                // This allows the datastore to replace this temporary Hollow object with the real datastore object if required
                // This doesnt change with RDBMS datastores since we just pull in fields, but for DB4O we pull in object graphs
                pc = sm.getObject();
            }
        }

        if (sm != null && !fromCache)
        {
            // Cache the object (update it if already present)
            putObjectIntoCache(sm);
            putObjectIntoLevel2Cache(sm, false);
        }

        return pc;
    }

    /**
     * This method returns an object id instance corresponding to the pcClass and key arguments.
     * Operates in 2 modes :-
     * <ul>
     * <li>The class uses SingleFieldIdentity and the key is the value of the key field</li>
     * <li>In all other cases the key is the String form of the object id instance</li>
     * </ul>
     * @param pcClass Class of the PersistenceCapable to create the identity for
     * @param key Value of the key for SingleFieldIdentity (or the toString value)
     * @return The new object-id instance
     */
    public Object newObjectId(Class pcClass, Object key)
    {
        assertIsOpen();
        if (pcClass == null)
        {
            throw new NucleusUserException(LOCALISER.msg("010028"));
        }
        assertClassPersistable(pcClass);

        AbstractClassMetaData cmd = getMetaDataManager().getMetaDataForClass(pcClass, clr);
        if (cmd == null)
        {
            throw new NoPersistenceInformationException(pcClass.getName());
        }

        // If the class is not yet managed, manage it
        if (!getStoreManager().managesClass(cmd.getFullClassName()))
        {
            getStoreManager().addClass(cmd.getFullClassName(), clr);
        }

        IdentityKeyTranslator translator = getNucleusContext().getIdentityKeyTranslator();
        if (translator != null)
        {
            // Use the provided translator to convert it
            key = translator.getKey(this, pcClass, key);
        }

        Object id = null;
        if (cmd.usesSingleFieldIdentityClass())
        {
            // Single Field Identity
            Class idType = clr.classForName(cmd.getObjectidClass());
            id = getApiAdapter().getNewSingleFieldIdentity(idType, pcClass, key);
        }
        else if (key instanceof java.lang.String)
        {
            // String-based PK (datastore identity or application identity)
            if (cmd.getIdentityType() == IdentityType.APPLICATION)
            {
                if (Modifier.isAbstract(pcClass.getModifiers()) && cmd.getObjectidClass() != null) 
                {
                    try
                    {
                        Constructor c = clr.classForName(cmd.getObjectidClass()).getDeclaredConstructor(new Class[] {java.lang.String.class});
                        id = c.newInstance(new Object[] {(String)key});
                    }
                    catch(Exception e) 
                    {
                        String msg = LOCALISER.msg("010030", cmd.getObjectidClass(), cmd.getFullClassName());
                        NucleusLogger.PERSISTENCE.error(msg);
                        NucleusLogger.PERSISTENCE.error(e);

                        throw new NucleusUserException(msg);
                    }
                }
                else
                {
                    clr.classForName(pcClass.getName(), true);
                    id = getApiAdapter().getNewApplicationIdentityObjectId(pcClass, key);
                }
            }
            else
            {
                id = OIDFactory.getInstance(getNucleusContext(), (String)key);
            }
        }
        else
        {
            // Key is not a string, and is not SingleFieldIdentity
            throw new NucleusUserException(LOCALISER.msg("010029", pcClass.getName(), key.getClass().getName()));
        }

        return id;
    }

    /**
     * This method returns an object id instance corresponding to the class name, and the passed
     * object (when using app identity).
     * @param className Name of the class of the object.
     * @param pc The persistable object. Used for application-identity
     * @return A new object ID.
     */
    public Object newObjectId(String className, Object pc)
    {
        AbstractClassMetaData cmd = getMetaDataManager().getMetaDataForClass(className, clr); 
        if (cmd.getIdentityType() == IdentityType.DATASTORE)
        {
            // Populate any strategy value for the "datastore-identity" element
            Object nextIdentifier = getStoreManager().getStrategyValue(this, cmd, -1);
            return OIDFactory.getInstance(getNucleusContext(), cmd.getFullClassName(), nextIdentifier);
        }
        else if (cmd.getIdentityType() == IdentityType.APPLICATION)
        {
            return getApiAdapter().getNewApplicationIdentityObjectId(pc, cmd); // All values will have been populated before arriving here
        }
        else
        {
            // All "nondurable" cases (e.g views) will come through here
            return new SCOID(className);
        }
    }

    /**
     * Method to clear an object from the list of dirty objects.
     * @param sm The StateManager
     */
    public void clearDirty(StateManager sm)
    {
        dirtySMs.remove(sm);
        indirectDirtySMs.remove(sm);
    }

    /**
     * Method to clear all objects marked as dirty (whether directly or indirectly).
     */
    public void clearDirty()
    {
        dirtySMs.clear();
        indirectDirtySMs.clear();
    }

    /**
     * Method to mark an object (ObjectProvider) as dirty.
     * @param op ObjectProvider
     * @param directUpdate Whether the object has had a direct update made on it (if known)
     */
    public void markDirty(ObjectProvider op, boolean directUpdate)
    {
        if (tx.isCommitting() && !tx.isActive())
        {
            //post commit cannot change objects (sanity check - avoid changing avoids on detach)
            throw new NucleusException("Cannot change objects when transaction is no longer active.");
        }

        StateManager sm = (StateManager)op;
        boolean isInDirty = dirtySMs.contains(sm);
        boolean isInIndirectDirty = indirectDirtySMs.contains(sm);
        if (!isDelayDatastoreOperationsEnabled() && !isInDirty && !isInIndirectDirty && 
            dirtySMs.size() >= getNucleusContext().getPersistenceConfiguration().getIntProperty("datanucleus.datastoreTransactionFlushLimit"))
        {
            // Reached flush limit so flush
            flushInternal(false);
        }

        if (directUpdate)
        {
            if (isInIndirectDirty)
            {
                indirectDirtySMs.remove(sm);
                dirtySMs.add(sm);
            }
            else if (!isInDirty)
            {
                dirtySMs.add(sm);
                if (txCachedIds != null)
                {
                    txCachedIds.add(sm.getInternalObjectId());
                }
            }
        }
        else
        {
            if (!isInDirty && !isInIndirectDirty)
            {
                // Register as an indirect dirty
                indirectDirtySMs.add(sm);
                if (txCachedIds != null)
                {
                    txCachedIds.add(sm.getInternalObjectId());
                }
            }
        }
    }

    /**
     * Accessor for whether to manage relationships at flush/commit.
     * @return Whether to manage relationships at flush/commit.
     */
    public boolean getManageRelations()
    {
        return properties.getBooleanProperty(PROP_MANAGE_RELATIONS);
    }

    /**
     * Accessor for whether to manage relationships checks at flush/commit.
     * @return Whether to manage relationships checks at flush/commit.
     */
    public boolean getManageRelationsChecks()
    {
        return properties.getBooleanProperty(PROP_MANAGE_RELATIONS_CHECKS);
    }

    public RelationshipManager getRelationshipManager(ObjectProvider op)
    {
        if (!getManageRelations())
        {
            return null;
        }

        if (managedRelationDetails == null)
        {
            managedRelationDetails = new ConcurrentHashMap();
        }
        RelationshipManager relMgr = managedRelationDetails.get(op);
        if (relMgr == null)
        {
            relMgr = new RelationshipManagerImpl((StateManager)op);
            managedRelationDetails.put(op, relMgr);
        }
        return relMgr;
    }

    /**
     * Returns whether this ObjectManager is currently performing the manage relationships task.
     * @return Whether in the process of managing relations
     */
    public boolean isManagingRelations()
    {
        return runningManageRelations;
    }

    /**
     * Method to perform managed relationships tasks.
     * @throws NucleusUserException if a consistency check fails
     */
    protected void performManagedRelationships()
    {
        if (getManageRelations() && managedRelationDetails != null && !managedRelationDetails.isEmpty())
        {
            try
            {
                runningManageRelations = true;
                if (NucleusLogger.PERSISTENCE.isDebugEnabled())
                {
                    NucleusLogger.PERSISTENCE.debug(LOCALISER.msg("013000"));
                }

                if (getManageRelationsChecks())
                {
                    // Tests for negative situations where inconsistently assigned
                    for (ObjectProvider sm : managedRelationDetails.keySet())
                    {
                        LifeCycleState lc = sm.getLifecycleState();
                        if (lc == null || lc.isDeleted())
                        {
                            // Has been deleted so ignore all relationship changes
                            continue;
                        }
                        RelationshipManager relMgr = managedRelationDetails.get(sm);
                        relMgr.checkConsistency();
                    }
                }

                // Process updates to manage the other side of the relations
                Iterator<ObjectProvider> opIter = managedRelationDetails.keySet().iterator();
                while (opIter.hasNext())
                {
                    ObjectProvider op = opIter.next();
                    LifeCycleState lc = op.getLifecycleState();
                    if (lc == null || lc.isDeleted())
                    {
                        // Has been deleted so ignore all relationship changes
                        continue;
                    }
                    RelationshipManager relMgr = managedRelationDetails.get(op);
                    relMgr.process();
                    relMgr.clearFields();
                }
                managedRelationDetails.clear();

                if (NucleusLogger.PERSISTENCE.isDebugEnabled())
                {
                    NucleusLogger.PERSISTENCE.debug(LOCALISER.msg("013001"));
                }
            }
            finally
            {
                runningManageRelations = false;
            }
        }
    }

    /**
     * Convenience method to inspect the list of objects with outstanding changes to flush.
     * @return StateManagers for the objects to be flushed.
     */
    public List<StateManager> getObjectsToBeFlushed()
    {
        List<StateManager> sms = new ArrayList();
        try
        {
            if (getMultithreaded())
            {
                lock.lock();
            }

            sms.addAll(dirtySMs);
            sms.addAll(indirectDirtySMs);
        }
        finally
        {
            if (getMultithreaded())
            {
                lock.unlock();
            }
        }
        return sms;
    }

    /**
     * Returns whether the ObjectManager is in the process of flushing.
     * @return true if the ObjectManager is flushing
     */
    public boolean isFlushing()
    {
        return flushing;
    }

    /**
     * Method callable from external APIs for user-management of flushing.
     * Called by JDO PM.flush, or JPA EM.flush().
     * Performs management of relations, prior to performing internal flush of all dirty/new/deleted
     * instances to the datastore.
     */
    public void flush()
    {
        assertIsOpen();
        if (tx.isActive())
        {
            // Managed Relationships
            performManagedRelationships();

            // Perform internal flush
            flushInternal(true);
        }
    }

    /**
     * This method flushes all dirty, new, and deleted instances to the datastore.
     * @param flushToDatastore Whether to ensure any changes reach the datastore
     *     Otherwise they will be flushed to the datastore manager and leave it to
     *     decide the opportune moment to actually flush them to the datastore
     * @throws NucleusOptimisticException when optimistic locking error(s) occur
     */
    public void flushInternal(boolean flushToDatastore)
    {
        assertIsOpen();

        if (!flushToDatastore && dirtySMs.size() == 0 && indirectDirtySMs.size() == 0)
        {
            // Nothing to flush so abort now
            return;
        }

        if (!tx.isActive())
        {
            // Non transactional flush, so store the ids for later
            if (nontxProcessedSMs == null)
            {
                nontxProcessedSMs = new HashSet();
            }
            nontxProcessedSMs.addAll(dirtySMs);
            nontxProcessedSMs.addAll(indirectDirtySMs);
        }

        flushing = true;
        try
        {
            List optimisticFailures = null;
            Boolean optimisedFlag = 
                getNucleusContext().getPersistenceConfiguration().getBooleanObjectProperty("datanucleus.flush.optimised");
            if (optimisedFlag != null)
            {
                // User requested particular flush type
                optimisticFailures = (optimisedFlag ? flushInternalNonReferential() : flushInternalWithOrdering());
            }
            else
            {
                if (!getStoreManager().getPersistenceHandler().useReferentialIntegrity())
                {
                    // Datastore doesn't use referential integrity, or user requests we do flushing as "optimised"
                    // Means we can send all deletes at once, then all inserts, then the rest
                    optimisticFailures = flushInternalNonReferential();
                }
                else
                {
                    // Flush all dirty, new, deleted instances to the datastore when transaction is active
                    optimisticFailures = flushInternalWithOrdering();
                }
            }

            if (flushToDatastore)
            {
                // Make sure flushes its changes to the datastore
                tx.flush();
            }

            if (optimisticFailures != null)
            {
                // Throw a single NucleusOptimisticException containing all optimistic failures
                throw new NucleusOptimisticException(LOCALISER.msg("010031"), 
                    (Throwable[])optimisticFailures.toArray(new Throwable[optimisticFailures.size()]));
            }
        }
        finally
        {
            if (NucleusLogger.PERSISTENCE.isDebugEnabled())
            {
                NucleusLogger.PERSISTENCE.debug(LOCALISER.msg("010004"));
            }
            flushing = false;
        }
    }

    /**
     * Flush process that takes the objects in the order that they became dirty.
     * If a datastore uses referential integrity this is typically the best way of maintaining
     * a valid update process.
     * @return Any optimistic exception(s) thrown by the update
     */
    protected List<NucleusOptimisticException> flushInternalWithOrdering()
    {
        if (NucleusLogger.PERSISTENCE.isDebugEnabled())
        {
            NucleusLogger.PERSISTENCE.debug(LOCALISER.msg("010003", (dirtySMs.size() + indirectDirtySMs.size())));
        }
        List<NucleusOptimisticException> optimisticFailures = null;

        // Flush all dirty, new, deleted instances to the datastore when transaction is active
        Object[] toFlushDirect;
        Object[] toFlushIndirect;
        try
        {
            if (getMultithreaded())
            {
                lock.lock();
            }

            toFlushDirect = dirtySMs.toArray();
            dirtySMs.clear();

            toFlushIndirect = indirectDirtySMs.toArray();
            indirectDirtySMs.clear();
        }
        finally
        {
            if (getMultithreaded())
            {
                lock.unlock();
            }
        }

        Set<Class> classesToFlush = null;
        if (getNucleusContext().getStoreManager().getQueryManager().getQueryResultsCache() != null)
        {
            classesToFlush = new HashSet();
        }

        // a). direct dirty objects
        for (int i = 0; i < toFlushDirect.length; i++)
        {
            StateManager sm = (StateManager) toFlushDirect[i];
            try
            {
                sm.flush();
                if (classesToFlush != null)
                {
                    classesToFlush.add(sm.getObject().getClass());
                }
            }
            catch (NucleusOptimisticException oe)
            {
                if (optimisticFailures == null)
                {
                    optimisticFailures = new ArrayList();
                }
                optimisticFailures.add(oe);
            }
        }

        // b). indirect dirty objects
        for (int i = 0; i < toFlushIndirect.length; i++)
        {
            StateManager sm = (StateManager) toFlushIndirect[i];
            try
            {
                sm.flush();
                if (classesToFlush != null)
                {
                    classesToFlush.add(sm.getObject().getClass());
                }
            }
            catch (NucleusOptimisticException oe)
            {
                if (optimisticFailures == null)
                {
                    optimisticFailures = new ArrayList();
                }
                optimisticFailures.add(oe);
            }
        }

        if (classesToFlush != null)
        {
            // Flush any query results from cache for these types
            Iterator<Class> queryClsIter = classesToFlush.iterator();
            while (queryClsIter.hasNext())
            {
                Class cls = queryClsIter.next();
                getNucleusContext().getStoreManager().getQueryManager().evictQueryResultsForType(cls);
            }
        }

        return optimisticFailures;
    }

    /**
     * Flush method for cases where the datastore doesn't use referential integrity so we can send batches
     * of deletes, then batches of inserts, then any updates to optimise the persistence.
     * @return Any optimistic exceptions during the deletes/inserts/updates
     */
    protected List<NucleusOptimisticException> flushInternalNonReferential()
    {
        List<NucleusOptimisticException> optimisticFailures = null;
        StorePersistenceHandler persistenceHandler = getStoreManager().getPersistenceHandler();

        // No referential integrity so we can send all deletes at once, then all inserts, then the rest
        Set<ObjectProvider> opsToFlush = new HashSet<ObjectProvider>();
        opsToFlush.addAll(dirtySMs);
        dirtySMs.clear();
        opsToFlush.addAll(indirectDirtySMs);
        indirectDirtySMs.clear();

        Set<Class> classesToFlush = null;
        if (getNucleusContext().getStoreManager().getQueryManager().getQueryResultsCache() != null)
        {
            classesToFlush = new HashSet();
        }

        Set<ObjectProvider> opsToDelete = new HashSet<ObjectProvider>();
        Set<ObjectProvider> opsToInsert = new HashSet<ObjectProvider>();
        Iterator<ObjectProvider> opIter = opsToFlush.iterator();
        while (opIter.hasNext())
        {
            ObjectProvider op = opIter.next();
            if (op.isEmbedded())
            {
                op.markAsFlushed(); // Embedded have nothing to flush since the owner manages it
                opIter.remove();
            }
            else
            {
                if (classesToFlush != null)
                {
                    classesToFlush.add(op.getObject().getClass());
                }
                if (op.getLifecycleState().isNew() && !op.isFlushedToDatastore() && !op.isFlushedNew())
                {
                    // P_NEW and not yet flushed to datastore
                    opsToInsert.add(op);
                    opIter.remove();
                }
                else if (op.getLifecycleState().isDeleted() && !op.isFlushedToDatastore())
                {
                    if (!op.getLifecycleState().isNew())
                    {
                        // P_DELETED
                        opsToDelete.add(op);
                        opIter.remove();
                    }
                    else if (op.getLifecycleState().isNew() && op.isFlushedNew())
                    {
                        // P_NEW_DELETED already persisted
                        opsToDelete.add(op);
                        opIter.remove();
                    }
                }
            }
        }

        if (NucleusLogger.PERSISTENCE.isDebugEnabled())
        {
            NucleusLogger.PERSISTENCE.debug(LOCALISER.msg("010046", opsToDelete.size(), opsToInsert.size(), opsToFlush.size()));
        }

        if (!opsToDelete.isEmpty())
        {
            // Perform preDelete - deleteAll - postDelete, and mark all ObjectProviders as flushed
            // TODO This omits some parts of sm.internalDeletePersistent
            for (ObjectProvider op : opsToDelete)
            {
                op.setFlushing(true);
                getCallbackHandler().preDelete(op.getObject());
            }
            try
            {
                persistenceHandler.deleteObjects(opsToDelete.toArray(new ObjectProvider[opsToDelete.size()]));
            }
            catch (NucleusOptimisticException noe)
            {
                // TODO Could get multiple failures from the deletions
                if (optimisticFailures == null)
                {
                    optimisticFailures = new ArrayList();
                }
                optimisticFailures.add(noe);
            }
            for (ObjectProvider op : opsToDelete)
            {
                getCallbackHandler().postDelete(op.getObject());
                op.setFlushedNew(false);
                op.markAsFlushed();
                op.setFlushing(false);
            }
        }

        if (!opsToInsert.isEmpty())
        {
            // Perform preStore - insertAll - postStore, and mark all ObjectProviders as flushed
            // TODO This omits some parts of sm.internalMakePersistent
            for (ObjectProvider op : opsToInsert)
            {
                op.setFlushing(true);
                getCallbackHandler().preStore(op.getObject());
                // TODO Make sure identity is set since user could have updated fields in preStore
            }
            persistenceHandler.insertObjects(opsToInsert.toArray(new ObjectProvider[opsToInsert.size()]));
            for (ObjectProvider op : opsToInsert)
            {
                getCallbackHandler().postStore(op.getObject());
                op.markAsFlushed();
                op.setFlushing(false);
                putObjectIntoCache(op); // Update the object in the cache(s) now that version/id are set
            }
        }

        if (!opsToFlush.isEmpty())
        {
            // Objects to update
            for (ObjectProvider op : opsToFlush)
            {
                try
                {
                    op.flush();
                }
                catch (NucleusOptimisticException oe)
                {
                    if (optimisticFailures == null)
                    {
                        optimisticFailures = new ArrayList();
                    }
                    optimisticFailures.add(oe);
                }
            }
        }

        if (classesToFlush != null)
        {
            // Flush any query results from cache for these types
            Iterator<Class> queryClsIter = classesToFlush.iterator();
            while (queryClsIter.hasNext())
            {
                Class cls = queryClsIter.next();
                getNucleusContext().getStoreManager().getQueryManager().evictQueryResultsForType(cls);
            }
        }

        return optimisticFailures;
    }

    /**
     * Method to perform any post-begin checks.
     */
    public void postBegin()
    {
        try
        {
            if (getMultithreaded())
            {
                lock.lock();
            }

            StateManager[] sms = dirtySMs.toArray(new StateManager[dirtySMs.size()]);
            for (int i=0; i<sms.length; i++)
            {
                sms[i].preBegin(tx);
            }
            sms = indirectDirtySMs.toArray(new StateManager[indirectDirtySMs.size()]);
            for (int i=0; i<sms.length; i++)
            {
                sms[i].preBegin(tx);
            }
        }
        finally
        {
            if (getMultithreaded())
            {
                lock.unlock();
            }
        }
    }

    /**
     * Method to perform any pre-commit checks.
     */
    public void preCommit()
    {
        try
        {
            if (getMultithreaded())
            {
                // Lock since updates fields in object(s)
                lock.lock();
            }

            // Make sure all is flushed before we start
            flush();

            if (getReachabilityAtCommit())
            {
                // Persistence-by-reachability at commit
                try
                {
                    runningPBRAtCommit = true;
                    performReachabilityAtCommit();
                    getTransaction().flush();
                }
                catch (Throwable t)
                {
                    NucleusLogger.PERSISTENCE.error(t);
                    if (t instanceof NucleusException)
                    {
                        throw (NucleusException) t;
                    }
                    else
                    {
                        throw new NucleusException("Unexpected error during precommit",t);
                    }
                }
                finally
                {
                    runningPBRAtCommit = false;
                }
            }

            if (context.hasLevel2Cache())
            {
                // L2 caching of enlisted objects
                performLevel2CacheUpdateAtCommit();
            }

            if (getDetachAllOnCommit())
            {
                // "detach-on-commit"
                performDetachAllOnTxnEndPreparation();
            }
        }
        finally
        {
            if (getMultithreaded())
            {
                lock.unlock();
            }
        }
    }

    /**
     * Accessor for whether the object with this identity is modified in the current transaction.
     * Only returns true when using the L2 cache and the object has been modified during the txn.
     * @param id The identity
     * @return Whether it is modified/new/deleted in this transaction
     */
    public boolean isObjectModifiedInTransaction(Object id)
    {
        if (txCachedIds != null)
        {
            return txCachedIds.contains(id);
        }
        return false;
    }

    /**
     * Method invoked during commit() to perform updates to the L2 cache.
     * <ul>
     * <li>Any objects modified during the current transaction will be added/updated in the L2 cache.</li>
     * <li>Any objects that aren't modified but have been enlisted will be added to the L2 cache.</li>
     * <li>Any objects that are modified but no longer enlisted (due to garbage collection) will be
     * removed from the L2 cache (to avoid giving out old data).</li>
     * </ul>
     */
    private void performLevel2CacheUpdateAtCommit()
    {
        // Lock the L2 cache so nobody else can have it while we are updating objects
        // Without this we can get race conditions between threads taking objects out, and
        // us putting objects in leading to assorted exceptions in AbstractStateManager or
        // in the PC object jdoReplaceField() methods.
        Level2Cache l2Cache = context.getLevel2Cache();
        synchronized (l2Cache)
        {
            // Process all modified objects adding/updating/removing from L2 cache as appropriate
            Iterator txCachedIter = txCachedIds.iterator();
            while (txCachedIter.hasNext())
            {
                Object id = txCachedIter.next();
                StateManager sm = enlistedSMCache.get(id);
                if (sm == null)
                {
                    // Modified object no longer enlisted so has been GCed, so remove from L2
                    if (NucleusLogger.CACHE.isDebugEnabled())
                    {
                        NucleusLogger.CACHE.debug(LOCALISER.msg("004014", id));
                    }
                    l2Cache.evict(id);
                }
                else
                {
                    // Modified object still enlisted so cacheable
                    Object objID = getApiAdapter().getIdForObject(sm.getObject());
                    if (objID == null)
                    {
                        // Must be embedded
                    }
                    else if (getApiAdapter().isDeleted(sm.getObject()))
                    {
                        // Object has been deleted so remove from L2 cache
                        if (NucleusLogger.CACHE.isDebugEnabled())
                        {
                            NucleusLogger.CACHE.debug(LOCALISER.msg("004007",
                                StringUtils.toJVMIDString(sm.getObject()), sm.getInternalObjectId()));
                        }
                        l2Cache.evict(objID);
                    }
                    else if (!getApiAdapter().isDetached(sm.getObject()))
                    {
                        // Object has been added/modified so update in L2 cache
                        putObjectIntoLevel2CacheInternal(sm, true);
                    }
                }
            }
            txCachedIds.clear();
        }
    }

    /**
     * Method to perform persistence-by-reachability at commit.
     * Utilises txKnownPersistedIds, and txFlushedNewIds, together with txKnownDeletedIds
     * and runs reachability, performing any necessary delettions of no longer reachable objects.
     */
    private void performReachabilityAtCommit()
    {
        if (NucleusLogger.PERSISTENCE.isDebugEnabled())
        {
            NucleusLogger.PERSISTENCE.debug(LOCALISER.msg("010032"));
        }

        // If we have some new objects in this transaction, and we have some known persisted objects (either
        // from makePersistent in this txn, or enlisted existing objects) then run reachability checks
        if (txKnownPersistedIds.size() > 0 && txFlushedNewIds.size() > 0)
        {
            Set currentReachables = new HashSet();

            // Run "reachability" on all known persistent objects for this txn
            Object ids[] = txKnownPersistedIds.toArray();
            Set objectNotFound = new HashSet();
            for (int i=0; i<ids.length; i++)
            {
                if (!txKnownDeletedIds.contains(ids[i]))
                {
                    if (NucleusLogger.PERSISTENCE.isDebugEnabled())
                    {
                        NucleusLogger.PERSISTENCE.debug("Performing reachability algorithm on object with id \""+ids[i]+"\"");
                    }
                    try
                    {
                        StateManager sm = findStateManager(findObject(ids[i], true, true, null));
                        sm.runReachability(currentReachables);
                        if (i % 10000 == 0 || i == ids.length-1)
                        {
                            // Flush every 10000 or on the last one to make sure tx cache is empty
                            flushInternal(true);
                        }
                    }
                    catch (NucleusObjectNotFoundException ex)
                    {
                        objectNotFound.add(ids[i]);
                    }
                }
                else
                {
                    // Was deleted earlier so ignore
                }
            }

            // Remove any of the "reachable" instances that are no longer "reachable"
            txFlushedNewIds.removeAll(currentReachables);

            Object nonReachableIds[] = txFlushedNewIds.toArray();
            if (nonReachableIds != null && nonReachableIds.length > 0)
            {
                // For all of instances no longer reachable we need to delete them from the datastore
                // A). Nullify all of their fields.
                // TODO See CORE-3276 for a possible change to this
                for (int i=0; i<nonReachableIds.length; i++)
                {
                    if (NucleusLogger.PERSISTENCE.isDebugEnabled())
                    {
                        NucleusLogger.PERSISTENCE.debug(LOCALISER.msg("010033", nonReachableIds[i]));
                    }
                    try
                    {
                        if (!objectNotFound.contains(nonReachableIds[i]))
                        {
                            StateManager sm = findStateManager(findObject(nonReachableIds[i], true, true, null));
                            sm.nullifyFields();

                            if (i % 10000 == 0 || i == nonReachableIds.length-1)
                            {
                                // Flush every 10000 or on the last one to clear out dirties
                                flushInternal(true);
                            }
                        }
                    }
                    catch (NucleusObjectNotFoundException ex)
                    {
                        // just ignore if the object does not exist anymore  
                    }
                }

                // B). Remove the objects
                for (int i=0; i<nonReachableIds.length; i++)
                {
                    try
                    {
                        if (!objectNotFound.contains(nonReachableIds[i]))
                        {
                            StateManager sm = findStateManager(findObject(nonReachableIds[i], true, true, null));
                            sm.deletePersistent();
                            if (i % 10000 == 0 || i == nonReachableIds.length-1)
                            {
                                // Flush every 10000 or on the last one to clear out dirties
                                flushInternal(true);
                            }
                        }
                    }
                    catch (NucleusObjectNotFoundException ex)
                    {
                        //just ignore if the file does not exist anymore  
                    }
                }
            }
        }

        if (NucleusLogger.PERSISTENCE.isDebugEnabled())
        {
            NucleusLogger.PERSISTENCE.debug(LOCALISER.msg("010034"));
        }
    }

    /**
     * Temporary array of StateManagers to detach at commit (to prevent garbage collection). 
     * Set up in preCommit() and used in postCommit().
     */
    private StateManager[] detachAllOnTxnEndSMs = null;

    /**
     * Method to perform all necessary preparation for detach-all-on-commit/detach-all-on-rollback.
     * Identifies all objects affected and makes sure that all fetch plan fields are loaded.
     */
    private void performDetachAllOnTxnEndPreparation()
    {
        // JDO2 spec 12.7.3 "Root instances"
        // "Root instances are parameter instances for retrieve, detachCopy, and refresh; result
        // instances for queries. Root instances for DetachAllOnCommit are defined explicitly by
        // the user via the FetchPlan property DetachmentRoots or DetachmentRootClasses. 
        // If not set explicitly, the detachment roots consist of the union of all root instances of
        // methods executed since the last commit or rollback."
        Collection sms = new ArrayList();
        Collection roots = fetchPlan.getDetachmentRoots();
        Class[] rootClasses = fetchPlan.getDetachmentRootClasses();
        if (roots != null && roots.size() > 0)
        {
            // Detachment roots specified
            Iterator rootsIter = roots.iterator();
            while (rootsIter.hasNext())
            {
                Object obj = rootsIter.next();
                sms.add(findStateManager(obj));
            }
        }
        else if (rootClasses != null && rootClasses.length > 0)
        {
            // Detachment root classes specified
            StateManager[] txSMs = enlistedSMCache.values().toArray(new StateManager[enlistedSMCache.size()]);
            for (int i=0;i<txSMs.length;i++)
            {
                for (int j=0;j<rootClasses.length;j++)
                {
                    // Check if object is of this root type
                    if (txSMs[i].getObject().getClass() == rootClasses[j])
                    {
                        // This SM is for a valid root object
                        sms.add(txSMs[i]);
                        break;
                    }
                }
            }
        }
        else if (cache != null)
        {
            // Detach all objects in the L1 cache
            sms.addAll(cache.values());
        }

        // Make sure that all FetchPlan fields are loaded
        Iterator smsIter = sms.iterator();
        while (smsIter.hasNext())
        {
            StateManager sm = (StateManager)smsIter.next();
            Object pc = sm.getObject();
            if (pc != null && !getApiAdapter().isDetached(pc) && !getApiAdapter().isDeleted(pc))
            {
                // Load all fields (and sub-objects) in the FetchPlan
                FetchPlanState state = new FetchPlanState();
                try
                {
                    sm.loadFieldsInFetchPlan(state);
                }
                catch (NucleusObjectNotFoundException onfe)
                {
                    // This object doesnt exist in the datastore at this point.
                    // Either the user has some other process that has deleted it or they have
                    // defined datastore based cascade delete and it has been deleted that way
                    NucleusLogger.PERSISTENCE.warn(LOCALISER.msg("010013",
                        StringUtils.toJVMIDString(pc), sm.getInternalObjectId()));
                    smsIter.remove();
                    // TODO Move the object state to P_DELETED for consistency
                }
            }
        }
        detachAllOnTxnEndSMs = (StateManager[])sms.toArray(new StateManager[sms.size()]);
    }

    /**
     * Method to perform detach-all-on-commit, using the data identified by
     * performDetachAllOnCommitPreparation().
     */
    private void performDetachAllOnTxnEnd()
    {
        try
        {
            runningDetachAllOnTxnEnd = true;

            if (detachAllOnTxnEndSMs != null)
            {
                // Detach all detachment root objects (causes recursion through the fetch plan)
                StateManager[] smsToDetach = detachAllOnTxnEndSMs;
                DetachState state = new DetachState(getApiAdapter());
                for (int i=0;i<smsToDetach.length;i++)
                {
                    Object pc = smsToDetach[i].getObject();
                    if (pc != null)
                    {
                        smsToDetach[i].detach(state);
                    }
                }
                detachAllOnTxnEndSMs = null; // No longer need these references
            }
        }
        finally
        {
            runningDetachAllOnTxnEnd = false;
        }
    }

    /**
     * Accessor for whether this ObjectManager is currently running detachAllOnCommit.
     * @return Whether running detachAllOnCommit
     */
    public boolean isRunningDetachAllOnCommit()
    {
        return runningDetachAllOnTxnEnd;
    }

    /**
     * Method to perform detach on close (of the ObjectManager).
     * Processes all (non-deleted) objects in the L1 cache and detaches them.
     */
    private void performDetachOnClose()
    {
        if (cache != null)
        {
            NucleusLogger.PERSISTENCE.debug(LOCALISER.msg("010011"));
            List<StateManager> toDetach = new ArrayList();
            toDetach.addAll(cache.values());

            if (tx.getNontransactionalRead())
            {
                // Handle it non-transactionally
                performDetachOnCloseWork(toDetach);
            }
            else
            {
                // Perform in a transaction
                try
                {
                    tx.begin();
                    performDetachOnCloseWork(toDetach);
                    tx.commit();
                }
                finally
                {
                    if (tx.isActive())
                    {
                        tx.rollback();
                    }
                }
            }
            NucleusLogger.PERSISTENCE.debug(LOCALISER.msg("010012"));
        }
    }

    private void performDetachOnCloseWork(List<StateManager> smsToDetach)
    {
        Iterator<StateManager> iter = smsToDetach.iterator();
        while (iter.hasNext())
        {
            StateManager sm = iter.next();
            if (sm != null && sm.getObject() != null && 
                !sm.getObjectManager().getApiAdapter().isDeleted(sm.getObject()) &&
                sm.getExternalObjectId() != null)
            {
                // If the object is deleted then no point detaching.
                // An object can be in L1 cache if transient and passed in to a query as a param for example
                try
                {
                    sm.detach(new DetachState(getApiAdapter()));
                }
                catch (NucleusObjectNotFoundException onfe)
                {
                    // Catch exceptions for any objects that are deleted in other managers whilst having this open
                }
            }
        }
    }

    /**
     * Commit any changes made to objects managed by the object manager to the database.
     */
    public void postCommit()
    {
        try
        {
            if (getMultithreaded())
            {
                // Lock since don't want any changes to objects during this step
                lock.lock();
            }

            if (getDetachAllOnCommit())
            {
                // Detach-all-on-commit
                performDetachAllOnTxnEnd();
            }

            List failures = null;
            try
            {
                // Commit all enlisted StateManagers
                ApiAdapter api = getApiAdapter();
                StateManager[] sms = enlistedSMCache.values().toArray(new StateManager[enlistedSMCache.size()]);
                for (int i = 0; i < sms.length; ++i)
                {
                    try
                    {
                        // Perform any operations required after committing
                        //TODO this if is due to sms that can have lc == null, why?, should not be here then
                        if (sms[i] != null && sms[i].getObject() != null &&
                                (api.isPersistent(sms[i].getObject()) || api.isTransactional(sms[i].getObject())))
                        {
                            sms[i].postCommit(getTransaction());

                            // TODO Change this check so that we remove all objects that are no longer suitable for caching
                            if (getDetachAllOnCommit() && api.isDetachable(sms[i].getObject()))
                            {
                                // "DetachAllOnCommit" - Remove the object from the L1 cache since it is now detached
                                removeStateManager(sms[i]);
                            }
                        }
                    }
                    catch (RuntimeException e)
                    {
                        if (failures == null)
                        {
                            failures = new ArrayList();
                        }
                        failures.add(e);
                    }
                }
            }
            finally
            {
                resetTransactionalVariables();
            }
            if (failures != null && !failures.isEmpty())
            {
                throw new CommitStateTransitionException((Exception[]) failures.toArray(new Exception[failures.size()]));
            }
        }
        finally
        {
            if (getMultithreaded())
            {
                lock.unlock();
            }
        }
    }

    /**
     * Rollback any changes made to objects managed by the object manager to the database.
     */
    public void preRollback()
    {
        try
        {
            if (getMultithreaded())
            {
                // Lock since updates fields in object(s)
                lock.lock();
            }

            ArrayList failures = null;
            try
            {
                Collection sms = enlistedSMCache.values();
                Iterator<StateManager> smsIter = sms.iterator();
                while (smsIter.hasNext())
                {
                    StateManager sm = smsIter.next();
                    try
                    {
                        sm.preRollback(getTransaction());
                    }
                    catch (RuntimeException e)
                    {
                        if (failures == null)
                        {
                            failures = new ArrayList();
                        }
                        failures.add(e);
                    }
                }
                clearDirty();
            }
            finally
            {
                resetTransactionalVariables();
            }

            if (failures != null && !failures.isEmpty())
            {
                throw new RollbackStateTransitionException((Exception[]) failures.toArray(new Exception[failures.size()]));
            }

            if (getDetachAllOnRollback())
            {
                // "detach-on-rollback"
                performDetachAllOnTxnEndPreparation();
            }
        }
        finally
        {
            if (getMultithreaded())
            {
                lock.unlock();
            }
        }
    }

    /**
     * Callback invoked after the actual datastore rollback.
     */
    public void postRollback()
    {
        try
        {
            if (getMultithreaded())
            {
                // Lock since updates fields in object(s)
                lock.lock();
            }

            if (getDetachAllOnRollback())
            {
                // "detach-on-rollback"
                performDetachAllOnTxnEnd();
            }
        }
        finally
        {
            if (getMultithreaded())
            {
                lock.unlock();
            }
        }
    }

    /**
     * Convenience method to reset all state variables for the transaction, performed at commit/rollback.
     */
    private void resetTransactionalVariables()
    {
        if (getReachabilityAtCommit())
        {
            txEnlistedIds.clear();
            txKnownPersistedIds.clear();
            txKnownDeletedIds.clear();
            txFlushedNewIds.clear();
        }

        enlistedSMCache.clear();
        dirtySMs.clear();
        indirectDirtySMs.clear();
        fetchPlan.resetDetachmentRoots();
        if (getManageRelations() && managedRelationDetails != null)
        {
            managedRelationDetails.clear();
        }
        if (txCachedIds != null)
        {
            txCachedIds.clear();
        }
    }

    // -------------------------------------- Cache Management ---------------------------------------

    /**
     * Convenience method to add an object to the L1 cache.
     * @param op The ObjectProvider
     */
    public void putObjectIntoCache(ObjectProvider op)
    {
        if (cache != null)
        {
            Object id = op.getInternalObjectId();
            if (id == null || op.getObject() == null)
            {
                NucleusLogger.CACHE.warn(LOCALISER.msg("003006"));
                return;
            }

            // Put into Level 1 Cache
            Object oldSM = cache.put(op.getInternalObjectId(), op);
            if (NucleusLogger.CACHE.isDebugEnabled())
            {
                if (oldSM == null)
                {
                    NucleusLogger.CACHE.debug(LOCALISER.msg("003004", 
                        StringUtils.toJVMIDString(op.getObject()), 
                        getIdentityAsString(op.getInternalObjectId()),
                        StringUtils.booleanArrayToString(op.getLoadedFields())));
                }
                else if (oldSM != op)
                {
                    NucleusLogger.CACHE.debug(LOCALISER.msg("003005", 
                        StringUtils.toJVMIDString(op.getObject()), 
                        getIdentityAsString(op.getInternalObjectId()),
                        StringUtils.booleanArrayToString(op.getLoadedFields())));
                }
            }
        }
    }

    /**
     * Method to add/update the managed object into the L2 cache as long as it isn't modified
     * in the current transaction.
     * @param sm StateManager for the object
     * @param updateIfPresent Whether to update it in the L2 cache if already present
     */
    public void putObjectIntoLevel2Cache(StateManager sm, boolean updateIfPresent)
    {
        if (sm.getInternalObjectId() == null)
        {
            // Cannot cache something with no identity
            return;
        }

        if (txCachedIds != null && !txCachedIds.contains(sm.getInternalObjectId()))
        {
            // Object hasn't been modified in this transaction so put in the L2 cache
            putObjectIntoLevel2CacheInternal(sm, updateIfPresent);
        }
    }

    /**
     * Convenience method to add/update an object in the L2 cache.
     * @param sm StateManager of the object to add.
     * @param updateIfPresent Whether to update the L2 cache if it is present
     */
    protected void putObjectIntoLevel2CacheInternal(StateManager sm, boolean updateIfPresent)
    {
        if (sm.getClassMetaData().isCacheable())
        {
            Object id = sm.getInternalObjectId();
            Level2Cache l2Cache = context.getLevel2Cache();
            if (!updateIfPresent && l2Cache.containsOid(id))
            {
                // Already present and not wanting to update
                return;
            }

            synchronized (l2Cache)
            {
                CachedPC cachedPC = sm.cache();
                if (cachedPC != null)
                {
                    // Update/add in the L2 cache
                    if (NucleusLogger.CACHE.isDebugEnabled())
                    {
                        if (l2Cache.containsOid(id))
                        {
                            NucleusLogger.CACHE.debug(LOCALISER.msg("004013", 
                                StringUtils.toJVMIDString(sm.getObject()), id, 
                                StringUtils.booleanArrayToString(cachedPC.getLoadedFields()),
                                StringUtils.objectArrayToString(cachedPC.getRelationFieldNames())));
                        }
                        else
                        {
                            NucleusLogger.CACHE.debug(LOCALISER.msg("004003", 
                                StringUtils.toJVMIDString(sm.getObject()), id, 
                                StringUtils.booleanArrayToString(cachedPC.getLoadedFields()),
                                StringUtils.objectArrayToString(cachedPC.getRelationFieldNames())));
                        }
                    }
                    l2Cache.put(id, cachedPC);
                }
            }
        }
    }

    /**
     * Convenience method to remove the object with the specified identity from the L2 cache.
     * @param id Identity of the object
     */
    public void removeObjectFromLevel2Cache(Object id)
    {
        if (id != null)
        {
            Level2Cache l2Cache = context.getLevel2Cache();
            synchronized (l2Cache)
            {
                if (l2Cache.containsOid(id))
                {
                    if (NucleusLogger.CACHE.isDebugEnabled())
                    {
                        NucleusLogger.CACHE.debug(LOCALISER.msg("004016", id));
                    }
                    l2Cache.evict(id);
                }
            }
        }
    }

    /**
     * Convenience method to evict an object from the L1 cache.
     * @param id The Persistable object id
     */
    public void removeObjectFromCache(Object id)
    {
        if (id != null && cache != null)
        {
            if (NucleusLogger.CACHE.isDebugEnabled())
            {
                NucleusLogger.CACHE.debug(LOCALISER.msg("003009", getIdentityAsString(id), String.valueOf(cache.size())));
            }
            Object pcRemoved = cache.remove(id);
            if (pcRemoved == null && NucleusLogger.CACHE.isDebugEnabled())
            {
                // For some reason the object isn't in the L1 cache - garbage collected maybe ?
                NucleusLogger.CACHE.debug(LOCALISER.msg("003010", getIdentityAsString(id)));
            }
        }
    }

    /**
     * Whether the specified identity is cached currently. Looks in L1 cache and L2 cache.
     * @param id The identity
     * @return Whether an object exists in the cache(s) with this identity
     */
    public boolean hasIdentityInCache(Object id)
    {
        // Try Level 1 first
        if (cache != null && cache.containsKey(id))
        {
            return true;
        }

        // Try Level 2 since not in Level 1
        if (context.hasLevel2Cache())
        {
            Level2Cache l2Cache = context.getLevel2Cache();
            synchronized (l2Cache)
            {
                if (l2Cache.containsOid(id))
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Convenience method to access an object in the cache.
     * Firstly looks in the L1 cache for this ObjectManager, and if not found looks in the L2 cache.
     * @param id Id of the object
     * @return Persistence Capable object (with connected StateManager).
     */
    public Object getObjectFromCache(Object id)
    {
        Object pc = null;
        StateManager sm = null;

        // Try Level 1 first
        if (cache != null)
        {
            sm = (StateManager)cache.get(id);
            if (sm != null)
            {
                pc = sm.getObject();
                if (NucleusLogger.CACHE.isDebugEnabled())
                {
                    NucleusLogger.CACHE.debug(LOCALISER.msg("003008", StringUtils.toJVMIDString(pc), 
                        getIdentityAsString(id), 
                        StringUtils.booleanArrayToString(sm.getLoadedFields()),
                        "" + cache.size()));
                }

                // Wipe the detach state that may have been added if the object has been serialised in the meantime
                sm.resetDetachState();

                return pc;
            }
            else
            {
                if (NucleusLogger.CACHE.isDebugEnabled())
                {
                    NucleusLogger.CACHE.debug(LOCALISER.msg("003007", getIdentityAsString(id), "" + cache.size()));
                }
            }
        }

        // Try Level 2 since not in Level 1
        if (context.hasLevel2Cache())
        {
            Level2Cache l2Cache = context.getLevel2Cache();
            CachedPC cachedPC = null;
            synchronized (l2Cache)
            {
                cachedPC = l2Cache.get(id);
            }

            // Create active version of cached object with StateManager connected and same id
            if (cachedPC != null)
            {
                if (NucleusLogger.CACHE.isDebugEnabled())
                {
                    NucleusLogger.CACHE.debug(LOCALISER.msg("004015", 
                        getIdentityAsString(id), 
                        StringUtils.booleanArrayToString(cachedPC.getLoadedFields()),
                        StringUtils.objectArrayToString(cachedPC.getRelationFieldNames())));
                }

                sm = (StateManager) ObjectProviderFactory.newForCachedPC(this, id, cachedPC);
                pc = sm.getObject(); // Object in P_CLEAN state
                if (NucleusLogger.CACHE.isDebugEnabled())
                {
                    NucleusLogger.CACHE.debug(LOCALISER.msg("004006", 
                        getIdentityAsString(id), StringUtils.toJVMIDString(pc)));
                }
                if (tx.isActive() && tx.getOptimistic())
                {
                    // Optimistic txns, so return as P_NONTRANS (as per JDO2 spec)
                    sm.makeNontransactional();
                }
                else if (!tx.isActive() && getApiAdapter().isTransactional(pc))
                {
                    // Non-tx context, so return as P_NONTRANS (as per JDO2 spec)
                    sm.makeNontransactional();
                }

                return pc;
            }
            else
            {
                if (NucleusLogger.CACHE.isDebugEnabled())
                {
                    NucleusLogger.CACHE.debug(LOCALISER.msg("004005", 
                        getIdentityAsString(id)));
                }
            }
        }

        return null;
    }

    /**
     * Replace the previous object id for a persistable object with a new one.
     * This is used where we have already added the object to the cache(s) and/or enlisted it in the txn before
     * its real identity was fixed (attributed in the datastore).
     * @param pc The Persistable object
     * @param oldID the old id it was known by
     * @param newID the new id
     */
    public void replaceObjectId(Object pc, Object oldID, Object newID)
    {
        if (pc == null || getApiAdapter().getIdForObject(pc) == null)
        {
            NucleusLogger.CACHE.warn(LOCALISER.msg("003006"));
            return;
        }

        StateManager sm = findStateManager(pc);

        // Update L1 cache
        if (cache != null)
        {
            Object o = cache.get(oldID); //use get() because a cache.remove operation returns a weakReference instance
            if (o != null)
            {
                // Remove the old variant
                if (NucleusLogger.CACHE.isDebugEnabled())
                {
                    NucleusLogger.CACHE.debug(LOCALISER.msg("003012", StringUtils.toJVMIDString(pc), 
                        getIdentityAsString(oldID), getIdentityAsString(newID)));
                }
                cache.remove(oldID);
            }
            if (sm != null)
            {
                putObjectIntoCache(sm);
            }
        }

        if (enlistedSMCache.get(oldID) != null)
        {
            // Swap the enlisted object identity
            if (sm != null)
            {
                enlistedSMCache.remove(oldID);
                enlistedSMCache.put(newID, sm);
                if (NucleusLogger.TRANSACTION.isDebugEnabled())
                {
                    NucleusLogger.TRANSACTION.debug(LOCALISER.msg("015018",
                        StringUtils.toJVMIDString(pc), getIdentityAsString(oldID), getIdentityAsString(newID)));
                }
            }
        }

        if (getReachabilityAtCommit() && tx.isActive())
        {
            if (txEnlistedIds.remove(oldID))
            {
                txEnlistedIds.add(newID);
            }
            if (txFlushedNewIds.remove(oldID))
            {
                txFlushedNewIds.add(newID);
            }
            if (txKnownPersistedIds.remove(oldID))
            {
                txKnownPersistedIds.add(newID);
            }
            if (txKnownDeletedIds.remove(oldID))
            {
                txKnownDeletedIds.add(newID);
            }
        }
    }

    /**
     * Convenience method to return the identity as a String.
     * Typically outputs the toString() form of the identity object however with SingleFieldIdentity
     * it outputs the class+key since SingleFieldIdentity just return the key.
     * @param id The id
     * @return String form
     */
    public String getIdentityAsString(Object id)
    {
        if (id == null)
        {
            return null;
        }
        if (getApiAdapter().isSingleFieldIdentity(id))
        {
            return getApiAdapter().getTargetClassNameForSingleFieldIdentity(id) + ":" +
                getApiAdapter().getTargetKeyForSingleFieldIdentity(id);
        }
        else
        {
            return id.toString();
        }
    }

    /**
     * Convenience method to return the setting for serialize read for the current transaction for
     * the specified class name. Returns the setting for the transaction (if set), otherwise falls back to
     * the setting for the class, otherwise returns false.
     * @param className Name of the class
     * @return Setting for serialize read
     */
    public boolean getSerializeReadForClass(String className)
    {
        if (tx.isActive() && tx.getSerializeRead() != null)
        {
            // Within a transaction, and serializeRead set for txn
            return tx.getSerializeRead();
        }
        else if (getProperty(PROP_SERIALISE_READ) != null)
        {
            // Set for the ObjectManager as a property
            return properties.getBooleanProperty(PROP_SERIALISE_READ);
        }
        else if (className != null)
        {
            // Set for the class
            AbstractClassMetaData cmd = getMetaDataManager().getMetaDataForClass(className, clr);
            if (cmd != null)
            {
                return cmd.isSerializeRead();
            }
        }
        return false;
    }

    // ------------------------------------- Queries/Extents --------------------------------------

    /**
     * Extents are collections of datastore objects managed by the datastore,
     * not by explicit user operations on collections. Extent capability is a
     * boolean property of classes that are persistence capable. If an instance
     * of a class that has a managed extent is made persistent via reachability,
     * the instance is put into the extent implicitly.
     * @param pcClass The class to query
     * @param subclasses Whether to include subclasses in the query.
     * @return returns an Extent that contains all of the instances in the
     * parameter class, and if the subclasses flag is true, all of the instances
     * of the parameter class and its subclasses.
     */
    public Extent getExtent(Class pcClass, boolean subclasses)
    {
        assertIsOpen();
        try
        {
            clr.setPrimary(pcClass.getClassLoader());
            assertClassPersistable(pcClass);

            return getStoreManager().getExtent(this, pcClass, subclasses);
        }
        finally
        {
            clr.unsetPrimary();
        }
    }

    /**
     * Construct an empty query instance.
     * @return The query
     */
    public Query newQuery()
    {
        return getStoreManager().getQueryManager().newQuery("JDOQL", this, null);
    }

    // ------------------------------------- Callback Listeners --------------------------------------

    /**
     * This method removes all previously registered lifecycle listeners.
     * It is necessary to make sure, that a cached ObjectManager (in j2ee environment)
     * will have no listener before the listeners are copied from the PMF/EMF.
     * Otherwise they might be registered multiple times.
     */
    public void removeAllInstanceLifecycleListeners()
    {
        if (callbacks != null)
        {
            callbacks.close();
        }
    }

    /**
     * Retrieve the callback handler for this ObjectManager.
     * @return the callback handler
     */
    public CallbackHandler getCallbackHandler()
    {
        if (callbacks != null)
        {
            return callbacks;
        }

        if (!getNucleusContext().getPersistenceConfiguration().getBooleanProperty("datanucleus.allowCallbacks"))
        {
            callbacks = new NullCallbackHandler();
            return callbacks;
        }
        else
        {
            String callbackHandlerClassName = getNucleusContext().getPluginManager().getAttributeValueForExtension(
                "org.datanucleus.callbackhandler", "name", getNucleusContext().getApiName(), "class-name");
            if (callbackHandlerClassName != null)
            {
                try
                {
                    callbacks = (CallbackHandler) getNucleusContext().getPluginManager().createExecutableExtension(
                        "org.datanucleus.callbackhandler", "name", getNucleusContext().getApiName(), "class-name",
                        new Class[] {NucleusContext.class}, new Object[] {getNucleusContext()});
                    return callbacks;
                }
                catch (Exception e)
                {
                    NucleusLogger.PERSISTENCE.error(LOCALISER.msg("025000", callbackHandlerClassName, e));
                }
            }
        }

        return null;
    }

    /**
     * Method to register a listener for instances of the specified classes.
     * @param listener The listener to sends events to
     * @param classes The classes that it is interested in
     */
    public void addListener(Object listener, Class[] classes)
    {
        assertIsOpen();
        if (listener == null)
        {
            return;
        }
        getCallbackHandler().addListener(listener, classes);
    }

    /**
     * Method to remove a currently registered listener.
     * @param listener The listener to remove.
     */
    public void removeListener(Object listener)
    {
        assertIsOpen();
        if (listener != null)
        {
            getCallbackHandler().removeListener(listener);
        }
    }

    /**
     * Disconnect the registered LifecycleListener
     */
    public void disconnectLifecycleListener()
    {
        // Clear out lifecycle listeners that were registered
        if (callbacks != null)
        {
            callbacks.close();
        }
    }

    // ------------------------------- Assert Utilities ---------------------------------

    /**
     * Method to assert if this Object Manager is open. 
     * Throws a NucleusUserException if the ObjectManager is closed.
     */
    protected void assertIsOpen()
    {
        if (isClosed())
        {
            throw new NucleusUserException(LOCALISER.msg("010002")).setFatal();
        }
    }

    /**
     * Method to assert if the specified class is Persistence Capable.
     * @param cls The class to check
     * @throws ClassNotPersistableException if class is not persistable
     * @throws NoPersistenceInformationException if no metadata/annotations are found for class
     */
    public void assertClassPersistable(Class cls)
    {
        if (cls != null && !getNucleusContext().getApiAdapter().isPersistable(cls) && !cls.isInterface())
        {
            throw new ClassNotPersistableException(cls.getName());
        }
        if (!hasPersistenceInformationForClass(cls))
        {
            throw new NoPersistenceInformationException(cls.getName());
        }
    }

    /**
     * Method to assert if the specified object is Detachable. 
     * Throws a ClassNotDetachableException if not capable
     * @param object The object to check
     */
    protected void assertDetachable(Object object)
    {
        if (object != null && !getApiAdapter().isDetachable(object))
        {
            throw new ClassNotDetachableException(object.getClass().getName());
        }
    }

    /**
     * Method to assert if the specified object is detached.
     * Throws a ObjectDetachedException if it is detached.
     * @param object The object to check
     */
    protected void assertNotDetached(Object object)
    {
        if (object != null && getApiAdapter().isDetached(object))
        {
            throw new ObjectDetachedException(object.getClass().getName());
        }
    }

    /**
     * Method to assert if the current transaction is active. Throws a
     * TransactionNotActiveException if not active
     */
    protected void assertActiveTransaction()
    {
        if (!tx.isActive())
        {
            throw new TransactionNotActiveException();
        }
    }

    /**
     * Validates that an ImplementationCreator instance is accessible.
     * @throws NucleusUserException if an ImplementationCreator instance does not exist
     */
    protected void assertHasImplementationCreator()
    {
        if (getNucleusContext().getImplementationCreator() == null)
        {
            throw new NucleusUserException(LOCALISER.msg("010035"));
        }
    }

    /**
     * Utility method to check if the specified class has reachable metadata or annotations.
     * @param cls The class to check
     * @return Whether the class has reachable metadata or annotations
     */
    public boolean hasPersistenceInformationForClass(Class cls)
    {
        if (cls == null)
        {
            return false;
        }
        
        if ((getMetaDataManager().getMetaDataForClass(cls, clr) != null))
        {
            return true;
        }

        if (cls.isInterface())
        {
            // JDO2 "persistent-interface"
            // Try to create an implementation of the interface at runtime. 
            // It will register the MetaData and make an implementation available
            try
            {
                newInstance(cls);
            }
            catch (RuntimeException ex)
            {
                NucleusLogger.PERSISTENCE.warn(ex);
            }
            return getMetaDataManager().getMetaDataForClass(cls, clr) != null;
        }
        return false;
    }

    // --------------------------- Fetch Groups ---------------------------------

    /** 
     * Convenience accessor for the FetchGroupManager.
     * Creates it if not yet existing.
     * @return The FetchGroupManager
     */
    protected FetchGroupManager getFetchGroupManager()
    {
        if (fetchGrpMgr == null)
        {
            fetchGrpMgr = new FetchGroupManager(getNucleusContext());
        }
        return fetchGrpMgr;
    }

    /**
     * Method to add a dynamic FetchGroup.
     * @param grp The group
     */
    public void addInternalFetchGroup(FetchGroup grp)
    {
        getFetchGroupManager().addFetchGroup(grp);
    }

    /**
     * Method to remove a dynamic FetchGroup.
     * @param grp The group
     */
    protected void removeInternalFetchGroup(FetchGroup grp)
    {
        getFetchGroupManager().removeFetchGroup(grp);
    }

    /**
     * Accessor for an internal fetch group for the specified class.
     * @param cls The class
     * @param name Name of the group
     * @return The FetchGroup
     * @throws NucleusUserException if the class is not persistable
     */
    public FetchGroup getInternalFetchGroup(Class cls, String name)
    {
        if (!cls.isInterface() && !getNucleusContext().getApiAdapter().isPersistable(cls))
        {
            // Class but not persistable!
            throw new NucleusUserException("Cannot create FetchGroup for " + cls + " since it is not persistable");
        }
        else if (cls.isInterface() && !getNucleusContext().getMetaDataManager().isPersistentInterface(cls.getName()))
        {
            // Interface but not persistent
            throw new NucleusUserException("Cannot create FetchGroup for " + cls + " since it is not persistable");
        }
        
        return getFetchGroupManager().getFetchGroup(cls, name);
    }

    /**
     * Accessor for the fetch groups for the specified name.
     * @param name Name of the group
     * @return The FetchGroup
     */
    public Set getFetchGroupsWithName(String name)
    {
        return getFetchGroupManager().getFetchGroupsWithName(name);
    }

    /**
     * Accessor for the ObjectManager lock object. 
     * @return The lock object
     */
    public Lock getLock()
    {
        return lock;
    }
}