/**********************************************************************
Copyright (c) 2011 Andy Jefferson and others. All rights reserved.
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
package org.datanucleus.state;

import org.datanucleus.ClassLoaderResolver;
import org.datanucleus.ObjectManager;
import org.datanucleus.cache.CachedPC;
import org.datanucleus.exceptions.ClassNotResolvedException;
import org.datanucleus.exceptions.NucleusUserException;
import org.datanucleus.metadata.AbstractClassMetaData;
import org.datanucleus.metadata.MetaDataManager;
import org.datanucleus.store.ExecutionContext;
import org.datanucleus.store.FieldValues;
import org.datanucleus.store.ObjectProvider;
import org.datanucleus.util.ClassUtils;
import org.datanucleus.util.Localiser;

/**
 * Factory for ObjectProviders.
 */
public class ObjectProviderFactory
{
    /** Localiser for messages. */
    protected static final Localiser LOCALISER = Localiser.getInstance("org.datanucleus.Localisation",
        org.datanucleus.ClassConstants.NUCLEUS_CONTEXT_LOADER);

    /**
     * Constructs a state manager to manage a hollow instance having the given object ID.
     * This constructor is used for creating new instances of existing persistent objects.
     * @param ec the ExecutionContext
     * @param pcClass the class of the new instance to be created.
     * @param id the JDO identity of the object.
     */
    public static ObjectProvider newForHollow(ExecutionContext ec, Class pcClass, Object id)
    {
        Initialization stateManagerInitialization = new Initialization(ec.getClassLoaderResolver(),
            ec.getMetaDataManager(),pcClass); 
        StateManager sm = newStateManager(ec, stateManagerInitialization.getClassMetaData());
        sm.initialiseForHollow(id, null, stateManagerInitialization.getPCClass());
        return sm;
    }

    /**
     * Constructs a state manager to manage a hollow instance having the given object ID.
     * The instance is already supplied.
     * @param ec ExecutionContext
     * @param id the JDO identity of the object.
     * @param pc The object that is hollow that we are going to manage
     */
    public static ObjectProvider newForHollowPreConstructed(ExecutionContext ec, Object id, Object pc)
    {
        Initialization stateManagerInitialization = new Initialization(ec.getClassLoaderResolver(), 
            ec.getMetaDataManager(),pc.getClass()); 
        StateManager sm = newStateManager(ec, stateManagerInitialization.getClassMetaData());
        sm.initialiseForHollowPreConstructed(id, pc);
        return sm;
    }

    /**
     * Constructs a state manager to manage a recently populated hollow instance having the
     * given object ID and the given field values. This constructor is used for
     * creating new instances of persistent objects obtained e.g. via a Query or backed by a view.
     * @param ec ExecutionContext
     * @param pcClass the class of the new instance to be created.
     * @param id the JDO identity of the object.
     * @param fv the initial field values of the object.
     */
    public static ObjectProvider newForHollowPopulated(ExecutionContext ec, Class pcClass, Object id, FieldValues fv)
    {
        Initialization stateManagerInitialization = new Initialization(ec.getClassLoaderResolver(),
            ec.getMetaDataManager(),pcClass); 
        StateManager sm = newStateManager(ec, stateManagerInitialization.getClassMetaData());
        sm.initialiseForHollow(id, fv, stateManagerInitialization.getPCClass());
        return sm;
    }

    /**
     * Constructs a state manager to manage the specified persistent instance having the given object ID.
     * @param ec the execution context controlling this state manager.
     * @param id the JDO identity of the object.
     * @param pc The object that is persistent that we are going to manage
     */
    public static ObjectProvider newForPersistentClean(ExecutionContext ec, Object id, Object pc)
    {
        Initialization stateManagerInitialization = new Initialization(ec.getClassLoaderResolver(),
            ec.getMetaDataManager(),pc.getClass()); 
        StateManager sm = newStateManager(ec, stateManagerInitialization.getClassMetaData());
        sm.initialiseForPersistentClean(id, pc);
        return sm;
    }
    
    /**
     * Constructs a state manager to manage a hollow (or pclean) instance having the given FieldValues.
     * This constructor is used for creating new instances of existing persistent objects using application identity.
     * @param ec ExecutionContext
     * @param pcClass the class of the new instance to be created.
     * @param fv the initial field values of the object.
     */
    public static ObjectProvider newForHollowPopulatedAppId(ExecutionContext ec, Class pcClass, final FieldValues fv)
    {
        Initialization stateManagerInitialization = new Initialization(ec.getClassLoaderResolver(),
            ec.getMetaDataManager(),pcClass); 
        StateManager sm = newStateManager(ec, stateManagerInitialization.getClassMetaData());
        sm.initialiseForHollowAppId(fv, stateManagerInitialization.getPCClass());
        return sm;
    }    

    /**
     * Constructs a state manager to manage a persistable instance that will
     * be EMBEDDED/SERIALISED into another persistable object. The instance will not be
     * assigned an identity in the process since it is a SCO.
     * @param ec ExecutionContext
     * @param pc The persistable to manage (see copyPc also)
     * @param copyPc Whether the SM should manage a copy of the passed PC or that one
     * @param ownerOP Owner ObjectProvider
     * @param ownerFieldNumber Field number in owner object where this is stored
     */
    public static ObjectProvider newForEmbedded(ExecutionContext ec, Object pc, boolean copyPc, 
            ObjectProvider ownerOP, int ownerFieldNumber)
    {
        Initialization stateManagerInitialization = new Initialization(ec.getClassLoaderResolver(),
            ec.getMetaDataManager(),pc.getClass()); 
        StateManager sm = newStateManager(ec, stateManagerInitialization.getClassMetaData());
        sm.initialiseForEmbedded(pc, copyPc);
        if (ownerOP != null)
        {
            sm.addEmbeddedOwner(ownerOP, ownerFieldNumber);
        }
        return sm;
    }

    /**
     * Constructs a state manager to manage a transient instance that is 
     * becoming newly persistent.  A new object ID for the
     * instance is obtained from the store manager and the object is inserted
     * in the data store.
     * This constructor is used for assigning state managers to existing
     * instances that are transitioning to a persistent state.
     * @param ec ExecutionContext
     * @param pc the instance being make persistent.
     * @param preInsertChanges Any changes to make before inserting
     */
    public static ObjectProvider newForPersistentNew(ExecutionContext ec, Object pc, FieldValues preInsertChanges)
    {
        Initialization stateManagerInitialization = new Initialization(ec.getClassLoaderResolver(),
            ec.getMetaDataManager(), pc.getClass()); 
        StateManager sm = newStateManager(ec, stateManagerInitialization.getClassMetaData());
        sm.initialiseForPersistentNew(pc, preInsertChanges);
        return sm;
    }

    /**
     * Constructs a state manager to manage a Transactional Transient instance.
     * A new object ID for the instance is obtained from the store manager and
     * the object is inserted in the data store.
     * This constructor is used for assigning state managers to Transient
     * instances that are transitioning to a transient clean state.
     * @param ec ExecutionContext
     * @param pc the instance being make persistent.
     */
    public static ObjectProvider newForTransactionalTransient(ExecutionContext ec, Object pc)
    {
        Initialization stateManagerInitialization = new Initialization(ec.getClassLoaderResolver(),
            ec.getMetaDataManager(),pc.getClass()); 
        StateManager sm = newStateManager(ec, stateManagerInitialization.getClassMetaData());
        sm.initialiseForTransactionalTransient(pc);
        return sm;
    }

    /**
     * Constructor for creating SM instances to manage persistable objects in detached state.
     * @param ec ExecutionContext
     * @param pc the detached object
     * @param id the JDO identity of the object.
     * @param version the detached version
     */
    public static ObjectProvider newForDetached(ExecutionContext ec, Object pc, Object id, Object version)
    {
        Initialization stateManagerInitialization = new Initialization(ec.getClassLoaderResolver(),
            ec.getMetaDataManager(),pc.getClass()); 
        StateManager sm = newStateManager(ec, stateManagerInitialization.getClassMetaData());
        sm.initialiseForDetached(pc, id, version);
        return sm;
    }

    /**
     * Constructor for creating SM instances to manage persistable objects that are not persistent yet
     * are about to be deleted. Consequently the initial lifecycle state will be P_NEW, but will soon
     * move to P_NEW_DELETED.
     * @param ec Execution Context
     * @param pc the object being deleted from persistence
     */
    public static ObjectProvider newForPNewToBeDeleted(ExecutionContext ec, Object pc)
    {
        Initialization stateManagerInitialization = new Initialization(ec.getClassLoaderResolver(),
            ec.getMetaDataManager(),pc.getClass()); 
        StateManager sm = newStateManager(ec, stateManagerInitialization.getClassMetaData());
        sm.initialiseForPNewToBeDeleted(pc);
        return sm;
    }

    /**
     * Constructor to create a ObjectProvider for an object taken from the L2 cache with the specified id.
     * Makes a copy of the cached object, assigns a ObjectProvider to it, and copies across the fields that 
     * were loaded when cached.
     * @param ec ExecutionContext
     * @param id Id to assign to the persistable object
     * @param cachedPC CachedPC object from the L2 cache
     */
    public static ObjectProvider newForCachedPC(ExecutionContext ec, Object id, CachedPC cachedPC)
    {
        Initialization stateManagerInitialization = new Initialization(ec.getClassLoaderResolver(),
            ec.getMetaDataManager(), cachedPC.getPersistableObject().getClass()); 
        StateManager sm = newStateManager(ec, stateManagerInitialization.getClassMetaData());
        sm.initialiseForCachedPC(cachedPC, id, stateManagerInitialization.getPCClass());
        return sm;
    }

    /**
     * Method to create a new StateManager for the ObjectManager and class.
     * @param ec ExecutionContext
     * @param acmd MetaData for the class/interface
     */
    static StateManager newStateManager(ExecutionContext ec, AbstractClassMetaData acmd)
    {
        return new JDOStateManagerImpl(((ObjectManager)ec), acmd);
    }

    protected static class Initialization
    {
        /** Class of the object being managed. This is only stored for some initialise methods that also need the class. */
        protected Class pcClass;

        /** the metadata for the class. */
        protected AbstractClassMetaData cmd;

        /**
         * Constructor.
         * @param clr ClassLoader resolver
         * @param mmgr Metadata manager
         * @param pcClass The class of the object that this will manage the state for
         */
        protected Initialization(ClassLoaderResolver clr, MetaDataManager mmgr, Class pcClass)
        {
            if (ClassUtils.isReferenceType(pcClass))
            {
                // TODO interfaces
                // in this case we support only one level for interfaces. in case of many levels, we support only one branch
                cmd = mmgr.getMetaDataForImplementationOfReference(pcClass, null, clr);
    
                // calling the CLR will make sure the class is initialized
                this.pcClass = clr.classForName(cmd.getFullClassName(), pcClass.getClassLoader(), true);
            }
            else
            {
                try
                {
                    // calling the CLR will make sure the class is initialized
                    this.pcClass = clr.classForName(pcClass.getName(), pcClass.getClassLoader(), true);
    
                    cmd = mmgr.getMetaDataForClass(pcClass, clr);
                }
                catch (ClassNotResolvedException e)
                {
                    throw new NucleusUserException(LOCALISER.msg("026015", pcClass.getName())).setFatal();
                }
            }
            if (cmd == null)
            {
                throw new NucleusUserException(LOCALISER.msg("026012", pcClass)).setFatal();
            }
        }
        
        protected Class getPCClass()
        {
            return pcClass;
        }
        
        protected AbstractClassMetaData getClassMetaData()
        {
            return cmd;
        }
    }
}