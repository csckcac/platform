/**********************************************************************
Copyright (c) 2005 Erik Bengtson and others. All rights reserved.
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
package org.datanucleus.store.types.sco.backed;

import java.io.ObjectStreamException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.datanucleus.ClassLoaderResolver;
import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.metadata.FieldPersistenceModifier;
import org.datanucleus.state.ObjectProviderFactory;
import org.datanucleus.store.BackedSCOStoreManager;
import org.datanucleus.store.ExecutionContext;
import org.datanucleus.store.ObjectProvider;
import org.datanucleus.store.scostore.MapStore;
import org.datanucleus.store.types.sco.SCOUtils;
import org.datanucleus.store.types.sco.queued.ClearMapOperation;
import org.datanucleus.store.types.sco.queued.OperationQueue;
import org.datanucleus.store.types.sco.queued.PutOperation;
import org.datanucleus.store.types.sco.queued.QueuedOperation;
import org.datanucleus.store.types.sco.queued.RemoveMapOperation;
import org.datanucleus.util.NucleusLogger;

/**
 * A mutable second-class Properties object. Backed by a MapStore object.
 * The key and value types of this class is {@link java.lang.String}.
 */
public class Properties extends org.datanucleus.store.types.sco.simple.Properties
{
    protected transient boolean allowNulls = false;
    protected transient MapStore backingStore;
    protected transient boolean useCache = true;
    protected transient boolean isCacheLoaded = false;
    protected transient boolean queued = false;
    protected transient OperationQueue<MapStore> operationQueue = null;

    /**
     * Constructor
     * @param ownerSM the owner of this Map
     * @param fieldName the declared field name
     */
    public Properties(ObjectProvider ownerSM, String fieldName)
    {
        super(ownerSM, fieldName);

        // Set up our "delegate"
        this.delegate = new java.util.Properties();

        ExecutionContext ec = ownerSM.getExecutionContext();
        AbstractMemberMetaData fmd = ownerSM.getClassMetaData().getMetaDataForMember(fieldName);
        fieldNumber = fmd.getAbsoluteFieldNumber();
        queued = ec.isDelayDatastoreOperationsEnabled();
        useCache = SCOUtils.useContainerCache(ownerSM, fieldName);

        if (!SCOUtils.mapHasSerialisedKeysAndValues(fmd) && 
                fmd.getPersistenceModifier() == FieldPersistenceModifier.PERSISTENT)
        {
            ClassLoaderResolver clr = ec.getClassLoaderResolver();
            this.backingStore = (MapStore)
            ((BackedSCOStoreManager)ec.getStoreManager()).getBackingStoreForField(clr, fmd, java.util.Map.class);
        }

        if (NucleusLogger.PERSISTENCE.isDebugEnabled())
        {
            NucleusLogger.PERSISTENCE.debug(SCOUtils.getContainerInfoMessage(ownerSM, fieldName, this,
                useCache, queued, allowNulls, SCOUtils.useCachedLazyLoading(ownerSM, fieldName)));
        }
    }

    /**
     * Method to initialise the SCO from an existing value.
     * @param o Object to set value using.
     * @param forInsert Whether the object needs inserting in the datastore with this value
     * @param forUpdate Whether to update the datastore with this value
     */
    public void initialise(Object o, boolean forInsert, boolean forUpdate)
    {
        java.util.Map m = (java.util.Map)o;
        if (m != null)
        {
            // Check for the case of serialised maps, and assign StateManagers to any PC keys/values without
            AbstractMemberMetaData fmd = ownerSM.getClassMetaData().getMetaDataForMember(fieldName);
            if (SCOUtils.mapHasSerialisedKeysAndValues(fmd) &&
                (fmd.getMap().keyIsPersistent() || fmd.getMap().valueIsPersistent()))
            {
                ExecutionContext ec = ownerSM.getExecutionContext();
                Iterator iter = m.entrySet().iterator();
                while (iter.hasNext())
                {
                    Map.Entry entry = (Map.Entry)iter.next();
                    Object key = entry.getKey();
                    Object value = entry.getValue();
                    if (fmd.getMap().keyIsPersistent())
                    {
                        ObjectProvider objSM = ec.findObjectProvider(key);
                        if (objSM == null)
                        {
                            objSM = ObjectProviderFactory.newForEmbedded(ec, key, false, ownerSM, fieldNumber);
                        }
                    }
                    if (fmd.getMap().valueIsPersistent())
                    {
                        ObjectProvider objSM = ec.findObjectProvider(value);
                        if (objSM == null)
                        {
                            objSM = ObjectProviderFactory.newForEmbedded(ec, value, false, ownerSM, fieldNumber);
                        }
                    }
                }
            }

            if (backingStore != null && useCache && !isCacheLoaded)
            {
                // Mark as loaded
                isCacheLoaded = true;
            }

            if (forInsert)
            {
                if (NucleusLogger.PERSISTENCE.isDebugEnabled())
                {
                    NucleusLogger.PERSISTENCE.debug(LOCALISER.msg("023007", 
                        ownerSM.toPrintableID(), fieldName, "" + m.size()));
                }
                putAll(m);
            }
            else if (forUpdate)
            {
                if (NucleusLogger.PERSISTENCE.isDebugEnabled())
                {
                    NucleusLogger.PERSISTENCE.debug(LOCALISER.msg("023008", 
                        ownerSM.toPrintableID(), fieldName, "" + m.size()));
                }
                clear();
                putAll(m);
            }
            else
            {
                if (NucleusLogger.PERSISTENCE.isDebugEnabled())
                {
                    NucleusLogger.PERSISTENCE.debug(LOCALISER.msg("023007", 
                        ownerSM.toPrintableID(), fieldName, "" + m.size()));
                }
                delegate.clear();
                delegate.putAll(m);
            }
        }
    }

    /**
     * Method to initialise the SCO for use.
     */
    public void initialise()
    {
        if (useCache && !SCOUtils.useCachedLazyLoading(ownerSM, fieldName))
        {
            // Load up the container now if not using lazy loading
            loadFromStore();
        }
    }

    /**
     * Accessor for the unwrapped value that we are wrapping.
     * @return The unwrapped value
     */
    public Object getValue()
    {
        loadFromStore();
        return super.getValue();
    }

    /**
     * Method to effect the load of the data in the SCO.
     * Used when the SCO supports lazy-loading to tell it to load all now.
     */
    public void load()
    {
        if (useCache)
        {
            loadFromStore();
        }
    }

    /**
     * Method to return if the SCO has its contents loaded.
     * If the SCO doesn't support lazy loading will just return true.
     * @return Whether it is loaded
     */
    public boolean isLoaded()
    {
        return useCache ? isCacheLoaded : false;
    }

    /**
     * Method to load all elements from the "backing store" where appropriate.
     */
    protected void loadFromStore()
    {
        if (backingStore != null && !isCacheLoaded)
        {
            if (NucleusLogger.PERSISTENCE.isDebugEnabled())
            {
                NucleusLogger.PERSISTENCE.debug(LOCALISER.msg("023006", 
                    ownerSM.toPrintableID(), fieldName));
            }
            delegate.clear();

            // Populate the delegate with the keys/values from the store
            SCOUtils.populateMapDelegateWithStoreData(delegate, backingStore, ownerSM);

            isCacheLoaded = true;
        }
    }

    /**
     * Method to flush the changes to the datastore when operating in queued mode.
     * Does nothing in "direct" mode.
     */
    public void flush()
    {
        if (queued)
        {
            if (operationQueue != null)
            {
                operationQueue.performAll(backingStore, ownerSM, fieldName);
            }
        }
    }

    /**
     * Convenience method to add a queued operation to the operations we perform at commit.
     * @param op The operation
     */
    protected void addQueuedOperation(QueuedOperation<? super MapStore> op)
    {
        if (operationQueue == null)
        {
            operationQueue = new OperationQueue<MapStore>();
        }
        operationQueue.enqueue(op);
    }

    /**
     * Method to update an embedded key in this map.
     * @param key The key
     * @param fieldNumber Number of field in the key
     * @param newValue New value for this field
     */
    public void updateEmbeddedKey(Object key, int fieldNumber, Object newValue)
    {
        if (backingStore != null)
        {
            backingStore.updateEmbeddedKey(ownerSM, key, fieldNumber, newValue);
        }
    }

    /**
     * Method to update an embedded value in this map.
     * @param value The value
     * @param fieldNumber Number of field in the value
     * @param newValue New value for this field
     */
    public void updateEmbeddedValue(Object value, int fieldNumber, Object newValue)
    {
        if (backingStore != null)
        {
            backingStore.updateEmbeddedValue(ownerSM, value, fieldNumber, newValue);
        }
    }

    /**
     * Method to unset the owner and field details.
     **/
    public synchronized void unsetOwner()
    {
        super.unsetOwner();
        if (backingStore != null)
        {
            backingStore = null;
        }
    }

    // ------------------ Implementation of Hashtable methods ------------------
    
    /**
     * Creates and returns a copy of this object.
     *
     * <P>Mutable second-class Objects are required to provide a public
     * clone method in order to allow for copying PersistenceCapable
     * objects. In contrast to Object.clone(), this method must not throw a
     * CloneNotSupportedException.
     * @return The cloned object
     */
    public Object clone()
    {
        if (useCache)
        {
            loadFromStore();
        }

        return delegate.clone();
    }
    
    /**
     * Method to return if the map contains this key
     * @param key The key
     * @return Whether it is contained
     **/
    public boolean containsKey(Object key)
    {
        if (useCache && isCacheLoaded)
        {
            // If the "delegate" is already loaded, use it
            return delegate.containsKey(key);
        }
        else if (backingStore != null)
        {
            return backingStore.containsKey(ownerSM, key);
        }
        
        return delegate.containsKey(key);
    }
    
    /**
     * Method to return if the map contains this value.
     * @param value The value
     * @return Whether it is contained
     **/
    public boolean containsValue(Object value)
    {
        if (useCache && isCacheLoaded)
        {
            // If the "delegate" is already loaded, use it
            return delegate.containsValue(value);
        }
        else if (backingStore != null)
        {
            return backingStore.containsValue(ownerSM, value);
        }
        
        return delegate.containsValue(value);
    }
    
    /**
     * Accessor for the set of entries in the Map.
     * @return Set of entries
     **/
    public java.util.Set entrySet()
    {
        if (useCache)
        {
            loadFromStore();
        }
        else if (backingStore != null)
        {
            return new Set(ownerSM, fieldName, false, backingStore.entrySetStore());
        }
        
        return delegate.entrySet();
    }
    
    /**
     * Method to check the equality of this map, and another.
     * @param o The map to compare against.
     * @return Whether they are equal.
     **/
    public synchronized boolean equals(Object o)
    {
        if (useCache)
        {
            loadFromStore();
        }
        
        if (o == this)
        {
            return true;
        }
        if (!(o instanceof java.util.Map))
        {
            return false;
        }
        java.util.Map m = (java.util.Map)o;
        
        return entrySet().equals(m.entrySet());
    }

    /**
     * Accessor for the value stored against a key.
     * @param key The key
     * @return The value.
     */
    public Object get(Object key)
    {
        if (useCache)
        {
            loadFromStore();
        }
        else if (backingStore != null)
        {
            return backingStore.get(ownerSM, key);
        }
        
        return delegate.get(key);
    }

    /**
     * Accessor for the string value stored against a string key.
     * @param key The key
     * @return The value.
     */
    public String getProperty(String key)
    {
        if (useCache)
        {
            loadFromStore();
        }
        else if (backingStore != null)
        {
            Object val = backingStore.get(ownerSM, key);
            String strVal = (val instanceof String) ? (String)val : null;
            return ((strVal == null) && (defaults != null)) ? defaults.getProperty(key) : strVal;
        }

        return delegate.getProperty(key);
    }
    
    /**
     * Method to generate a hashcode for this Map.
     * @return The hashcode.
     **/
    public synchronized int hashCode()
    {
        if (useCache)
        {
            loadFromStore();
        }
        else if (backingStore != null)
        {
            int h = 0;
            Iterator i = entrySet().iterator();
            while (i.hasNext())
            {
                h += i.next().hashCode();
            }
            
            return h;
        }
        return delegate.hashCode();
    }
    
    /**
     * Method to return if the Map is empty.
     * @return Whether it is empty.
     **/
    public boolean isEmpty()
    {
        return size() == 0;
    }
    
    /**
     * Accessor for the set of keys in the Map.
     * @return Set of keys.
     **/
    public java.util.Set keySet()
    {
        if (useCache)
        {
            loadFromStore();
        }
        else if (backingStore != null)
        {
            return new Set(ownerSM, fieldName, false, backingStore.keySetStore());
        }
        
        return delegate.keySet();
    }
    
    /**
     * Method to return the size of the Map.
     * @return The size
     **/
    public int size()
    {
        if (useCache && isCacheLoaded)
        {
            // If the "delegate" is already loaded, use it
            return delegate.size();
        }
        else if (backingStore != null)
        {
            return backingStore.entrySetStore().size(ownerSM);
        }
        
        return delegate.size();
    }
    
    /**
     * Accessor for the set of values in the Map.
     * @return Set of values.
     **/
    public Collection values()
    {
        if (useCache)
        {
            loadFromStore();
        }
        else if (backingStore != null)
        {
            return new Set(ownerSM, fieldName, true, backingStore.valueSetStore());
        }
        
        return delegate.values();
    }
    
    // -------------------------------- Mutator methods ------------------------
    
    /**
     * Method to clear the Hashtable
     **/
    public synchronized void clear()
    {
        makeDirty();
        
        if (backingStore != null)
        {
            if (SCOUtils.useQueuedUpdate(queued, ownerSM))
            {
                addQueuedOperation(new ClearMapOperation());
            }
            else
            {
                backingStore.clear(ownerSM);
            }
        }
        delegate.clear();
    }
    
    /**
     * Method to add a value against a key to the Hashtable
     * @param key The key
     * @param value The value
     * @return The previous value for the specified key.
     **/
    public Object put(Object key,Object value)
    {
        // Reject inappropriate elements
        if (!allowNulls)
        {
            if (key == null)
            {
                throw new NullPointerException("Nulls not allowed for map at field " + fieldName + " but key is null");
            }
            if (value == null)
            {
                throw new NullPointerException("Nulls not allowed for map at field " + fieldName + " but value is null");
            }
        }

        if (useCache)
        {
            // Make sure we have all values loaded (e.g if in optimistic tx and we put new entry)
            loadFromStore();
        }

        makeDirty();

        Object oldValue = null;
        if (backingStore != null)
        {
            if (SCOUtils.useQueuedUpdate(queued, ownerSM))
            {
                addQueuedOperation(new PutOperation(key, value));
            }
            else
            {
                oldValue = backingStore.put(ownerSM, key, value);
            }
        }
        Object delegateOldValue = delegate.put(key, value);
        if (backingStore == null)
        {
            oldValue = delegateOldValue;
        }
        else if (SCOUtils.useQueuedUpdate(queued, ownerSM))
        {
            oldValue = delegateOldValue;
        }
        return oldValue;
    }
    
    /**
     * Method to add the specified Map's values under their keys here.
     * @param m The map
     **/
    public void putAll(java.util.Map m)
    {
        makeDirty();

        if (useCache)
        {
            // Make sure we have all values loaded (e.g if in optimistic tx and we put new entry)
            loadFromStore();
        }

        if (backingStore != null)
        {
            if (SCOUtils.useQueuedUpdate(queued, ownerSM))
            {
                Iterator iter = m.entrySet().iterator();
                while (iter.hasNext())
                {
                    Map.Entry entry = (Map.Entry)iter.next();
                    addQueuedOperation(new PutOperation(entry.getKey(), entry.getValue()));
                }
            }
            else
            {
                backingStore.putAll(ownerSM, m);
            }
        }
        delegate.putAll(m);
    }
    
    /**
     * Method to remove the value for a key from the Hashtable
     * @param key The key to remove
     * @return The value that was removed from this key.
     **/
    public Object remove(Object key)
    {
        makeDirty();

        if (useCache)
        {
            // Make sure we have all values loaded (e.g if in optimistic tx and we put new entry)
            loadFromStore();
        }

        Object removed = null;
        Object delegateRemoved = delegate.remove(key);
        if (backingStore != null)
        {
            if (SCOUtils.useQueuedUpdate(queued, ownerSM))
            {
                addQueuedOperation(new RemoveMapOperation(key));
                removed = delegateRemoved;
            }
            else
            {
                removed = backingStore.remove(ownerSM, key);
            }
        }
        else
        {
            removed = delegateRemoved;
        }
        return removed;
    }

    /**
     * Method to add a string value against a string key to the Hashtable
     * @param key The key
     * @param value The value
     * @return The previous value for the specified key.
     */
    public Object setProperty(String key, String value)
    {
        return put(key, value);
    }

    /**
     * The writeReplace method is called when ObjectOutputStream is preparing
     * to write the object to the stream. The ObjectOutputStream checks whether
     * the class defines the writeReplace method. If the method is defined, the
     * writeReplace method is called to allow the object to designate its
     * replacement in the stream. The object returned should be either of the
     * same type as the object passed in or an object that when read and 
     * resolved will result in an object of a type that is compatible with all
     * references to the object.
     * 
     * @return the replaced object
     * @throws ObjectStreamException
     */
    protected Object writeReplace() throws ObjectStreamException
    {
        if (useCache)
        {
            loadFromStore();
            return new java.util.Hashtable(delegate);
        }
        else
        {
            // TODO Cater for non-cached map, load elements in a DB call.
            return new java.util.Hashtable(delegate);
        }
    }
}