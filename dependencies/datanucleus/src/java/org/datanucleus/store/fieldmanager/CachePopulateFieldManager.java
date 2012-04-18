/**********************************************************************
Copyright (c) 2008 Andy Jefferson and others. All rights reserved.
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
package org.datanucleus.store.fieldmanager;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.datanucleus.api.ApiAdapter;
import org.datanucleus.cache.CachedPC;
import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.metadata.FieldPersistenceModifier;
import org.datanucleus.metadata.MetaDataUtils;
import org.datanucleus.store.ObjectProvider;
import org.datanucleus.store.types.sco.SCO;
import org.datanucleus.store.types.sco.SCOContainer;
import org.datanucleus.util.NucleusLogger;

/**
 * Field manager to handle the population of a CachedPC object ready for putting in the L2 cache.
 */
public class CachePopulateFieldManager extends AbstractFieldManager
{
    /** The CachedPC that we are populating. */
    CachedPC cachedPC;

    /** StateManager of the instance being fetched (detached or made transient). **/
    protected final ObjectProvider sm;

    /**
     * Constructor for a field manager for fetch plan processing.
     * @param sm the StateManager of the instance being processed.
     * @param cachedPC The CachedPC that we are populating
     */
    public CachePopulateFieldManager(ObjectProvider sm, CachedPC cachedPC)
    {
        this.sm = sm;
        this.cachedPC = cachedPC;
    }

    /**
     * Method to fetch an object field whether it is SCO collection, PC, or whatever.
     * @param fieldNumber Number of the field
     * @return The object
     */
    public Object fetchObjectField(int fieldNumber)
    {
        SingleValueFieldManager sfv = new SingleValueFieldManager();
        sm.provideFields(new int[]{fieldNumber}, sfv);
        Object value = sfv.fetchObjectField(fieldNumber);
        ApiAdapter api = sm.getExecutionContext().getApiAdapter();
        cachedPC.getLoadedFields()[fieldNumber] = true; // Overridden later if necessary

        if (value == null)
        {
            return null;
        }
        else
        {
            AbstractMemberMetaData mmd = 
                sm.getClassMetaData().getMetaDataForManagedMemberAtAbsolutePosition(fieldNumber);
            if (mmd.getPersistenceModifier() == FieldPersistenceModifier.TRANSACTIONAL)
            {
                // Cannot cache transactional fields
                cachedPC.getLoadedFields()[fieldNumber] = false;
                return null;
            }
            if (!mmd.isCacheable())
            {
                // Field is marked as not cacheable so unset its loaded flag and return null
                cachedPC.getLoadedFields()[fieldNumber] = false;
                return null;
            }

            if (api.isPersistable(value))
            {
                // 1-1, N-1 PC field
                if (mmd.isSerialized() || mmd.isEmbedded())
                {
                    // TODO Support serialised/embedded PC fields
                    cachedPC.getLoadedFields()[fieldNumber] = false;
                    return null;
                }

                // Put field OID in CachedPC "relationFields" and store null in this field
                cachedPC.setRelationField(mmd.getName(), api.getIdForObject(value));
                return null;
            }
            else if (value instanceof Collection)
            {
                // 1-N, M-N Collection
                if (MetaDataUtils.getInstance().storesPersistable(mmd, sm.getExecutionContext()))
                {
                    if (value instanceof List && mmd.getOrderMetaData() != null && 
                        !mmd.getOrderMetaData().isIndexedList())
                    {
                        // Ordered list so don't cache since dependent on datastore-retrieve order
                        cachedPC.getLoadedFields()[fieldNumber] = false;
                        return null;
                    }
                    if (mmd.isSerialized() || mmd.isEmbedded() ||
                        mmd.getCollection().isSerializedElement() || mmd.getCollection().isEmbeddedElement())
                    {
                        // TODO Support serialised/embedded elements
                        cachedPC.getLoadedFields()[fieldNumber] = false;
                        return null;
                    }
                    Collection collValue = (Collection)value;
                    if (collValue instanceof SCO && !((SCOContainer)value).isLoaded())
                    {
                        // Contents not loaded so just mark as unloaded
                        cachedPC.getLoadedFields()[fieldNumber] = false;
                        return null;
                    }

                    Iterator collIter = collValue.iterator();
                    Collection returnColl = null;
                    try
                    {
                        if (value.getClass().isInterface())
                        {
                            if (List.class.isAssignableFrom(value.getClass()) || mmd.getOrderMetaData() != null)
                            {
                                // List based
                                returnColl = new ArrayList();
                            }
                            else
                            {
                                // Set based
                                returnColl = new HashSet();
                            }
                        }
                        else
                        {
                            if (value instanceof SCO)
                            {
                                returnColl = (Collection)((SCO)value).getValue().getClass().newInstance();
                            }
                            else
                            {
                                returnColl = (Collection)value.getClass().newInstance();
                            }
                        }

                        // Recurse through elements, and put ids of elements in return value
                        while (collIter.hasNext())
                        {
                            Object elem = collIter.next();
                            if (elem == null)
                            {
                                returnColl.add(null); // Allow for null elements
                            }
                            else
                            {
                                returnColl.add(api.getIdForObject(elem));
                            }
                        }

                        // Put Collection<OID> in CachedPC "relationFields" and store null in this field
                        cachedPC.setRelationField(mmd.getName(), returnColl);
                        return null;
                    }
                    catch (Exception e)
                    {
                        NucleusLogger.CACHE.warn("Unable to create object of type " + value.getClass().getName() +
                            " for L2 caching : " + e.getMessage());

                        // Contents not loaded so just mark as unloaded
                        cachedPC.getLoadedFields()[fieldNumber] = false;
                        return null;
                    }
                }
                else
                {
                    // Collection<Non-PC> so just return it
                    if (value instanceof SCOContainer)
                    {
                        if (((SCOContainer)value).isLoaded())
                        {
                            // Return unwrapped collection
                            return ((SCO)value).getValue();
                        }
                        else
                        {
                            // Contents not loaded so just mark as unloaded
                            cachedPC.getLoadedFields()[fieldNumber] = false;
                            return null;
                        }
                    }
                    else
                    {
                        return value;
                    }
                }
            }
            else if (value instanceof Map)
            {
                // 1-N, M-N Map
                if (MetaDataUtils.getInstance().storesPersistable(mmd, sm.getExecutionContext()))
                {
                    if (mmd.isSerialized() || mmd.isEmbedded() ||
                        mmd.getMap().isSerializedKey() || mmd.getMap().isEmbeddedKey() ||
                        mmd.getMap().isSerializedValue() || mmd.getMap().isEmbeddedValue())
                    {
                        // TODO Support serialised/embedded keys/values
                        cachedPC.getLoadedFields()[fieldNumber] = false;
                        return null;
                    }

                    if (value instanceof SCO && !((SCOContainer)value).isLoaded())
                    {
                        // Contents not loaded so just mark as unloaded
                        cachedPC.getLoadedFields()[fieldNumber] = false;
                        return null;
                    }

                    try
                    {
                        Map returnMap = null;
                        if (value.getClass().isInterface())
                        {
                            returnMap = new HashMap();
                        }
                        else
                        {
                            if (value instanceof SCO)
                            {
                                returnMap = (Map)((SCO)value).getValue().getClass().newInstance();
                            }
                            else
                            {
                                returnMap = (Map)value.getClass().newInstance();
                            }
                        }
                        Iterator mapIter = ((Map)value).entrySet().iterator();
                        while (mapIter.hasNext())
                        {
                            Map.Entry entry = (Map.Entry)mapIter.next();
                            Object mapKey = null;
                            Object mapValue = null;
                            if (mmd.getMap().keyIsPersistent())
                            {
                                mapKey = api.getIdForObject(entry.getKey());
                            }
                            else
                            {
                                mapKey = entry.getKey();
                            }
                            if (mmd.getMap().valueIsPersistent())
                            {
                                mapValue = api.getIdForObject(entry.getValue());
                            }
                            else
                            {
                                mapValue = entry.getValue();
                            }
                            returnMap.put(mapKey, mapValue);
                        }

                        // Put Map<X, Y> in CachedPC "relationFields" and store null in this field
                        // where X, Y can be OID if they are persistable objects
                        cachedPC.setRelationField(mmd.getName(), returnMap);
                        return null;
                    }
                    catch (Exception e)
                    {
                        NucleusLogger.CACHE.warn("Unable to create object of type " + value.getClass().getName() +
                            " for L2 caching : " + e.getMessage());

                        // Contents not loaded so just mark as unloaded
                        cachedPC.getLoadedFields()[fieldNumber] = false;
                        return null;
                    }
                }
                else
                {
                    // Map<Non-PC, Non-PC> so just return it
                    if (value instanceof SCOContainer)
                    {
                        if (((SCOContainer)value).isLoaded())
                        {
                            // Return unwrapped map
                            return ((SCO)value).getValue();
                        }
                        else
                        {
                            // Contents not loaded so just mark as unloaded
                            cachedPC.getLoadedFields()[fieldNumber] = false;
                            return null;
                        }
                    }
                    else
                    {
                        return value;
                    }
                }
            }
            else if (value instanceof Object[])
            {
                // Array, maybe of Persistable objects
                if (MetaDataUtils.getInstance().storesPersistable(mmd, sm.getExecutionContext()))
                {
                    if (mmd.isSerialized() || mmd.isEmbedded() ||
                        mmd.getArray().isSerializedElement() || mmd.getArray().isEmbeddedElement())
                    {
                        // TODO Support serialised/embedded elements
                        cachedPC.getLoadedFields()[fieldNumber] = false;
                        return null;
                    }

                    Object[] returnArr = new Object[Array.getLength(value)];
                    for (int i=0;i<Array.getLength(value);i++)
                    {
                        Object element = Array.get(value, i);
                        returnArr[i] = api.getIdForObject(element);
                    }

                    // Put OID[] in CachedPC "relationFields" and store null in this field
                    cachedPC.setRelationField(mmd.getName(), returnArr);
                    return null;
                }
                else
                {
                    // Array element type is not persistable so just return the value
                    return value;
                }
            }
            else if (value instanceof StringBuffer)
            {
                // Make a copy of a StringBuffer for the cache since it is mutable but final
                return new StringBuffer(((StringBuffer)value).toString());
            }
            else if (value instanceof SCO)
            {
                // SCO wrapper - so replace with unwrapped
                if (NucleusLogger.CACHE.isDebugEnabled())
                {
                    // TODO Localise this
                    NucleusLogger.CACHE.debug("CachePopulateFM.fetchObjectField this=" + 
                        sm.toPrintableID() +
                        " field=" + fieldNumber + " is having its SCO wrapper replaced prior to L2 caching.");
                }
                // TODO Need to take copy of the object see NUCCORE-52
                return ((SCO)value).getValue();
            }
            else
            {
                return value;
            }
        }
    }

    /* (non-Javadoc)
     * @see FieldSupplier#fetchBooleanField(int)
     */
    public boolean fetchBooleanField(int fieldNumber)
    {
        AbstractMemberMetaData mmd = 
            sm.getClassMetaData().getMetaDataForManagedMemberAtAbsolutePosition(fieldNumber);
        if (mmd.getPersistenceModifier() == FieldPersistenceModifier.TRANSACTIONAL)
        {
            // Cannot cache transactional fields
            cachedPC.getLoadedFields()[fieldNumber] = false;
            return true;
        }

        SingleValueFieldManager sfv = new SingleValueFieldManager();
        sm.provideFields(new int[]{fieldNumber}, sfv);
        cachedPC.getLoadedFields()[fieldNumber] = true;
        return sfv.fetchBooleanField(fieldNumber);
    }

    /* (non-Javadoc)
     * @see FieldSupplier#fetchByteField(int)
     */
    public byte fetchByteField(int fieldNumber)
    {
        AbstractMemberMetaData mmd = 
            sm.getClassMetaData().getMetaDataForManagedMemberAtAbsolutePosition(fieldNumber);
        if (mmd.getPersistenceModifier() == FieldPersistenceModifier.TRANSACTIONAL)
        {
            // Cannot cache transactional fields
            cachedPC.getLoadedFields()[fieldNumber] = false;
            return 0;
        }

        SingleValueFieldManager sfv = new SingleValueFieldManager();
        sm.provideFields(new int[]{fieldNumber}, sfv);
        cachedPC.getLoadedFields()[fieldNumber] = true;
        return sfv.fetchByteField(fieldNumber);
    }

    /* (non-Javadoc)
     * @see FieldSupplier#fetchCharField(int)
     */
    public char fetchCharField(int fieldNumber)
    {
        AbstractMemberMetaData mmd = 
            sm.getClassMetaData().getMetaDataForManagedMemberAtAbsolutePosition(fieldNumber);
        if (mmd.getPersistenceModifier() == FieldPersistenceModifier.TRANSACTIONAL)
        {
            // Cannot cache transactional fields
            cachedPC.getLoadedFields()[fieldNumber] = false;
            return 0;
        }

        SingleValueFieldManager sfv = new SingleValueFieldManager();
        sm.provideFields(new int[]{fieldNumber}, sfv);
        cachedPC.getLoadedFields()[fieldNumber] = true;
        return sfv.fetchCharField(fieldNumber);
    }

    /* (non-Javadoc)
     * @see FieldSupplier#fetchDoubleField(int)
     */
    public double fetchDoubleField(int fieldNumber)
    {
        AbstractMemberMetaData mmd = 
            sm.getClassMetaData().getMetaDataForManagedMemberAtAbsolutePosition(fieldNumber);
        if (mmd.getPersistenceModifier() == FieldPersistenceModifier.TRANSACTIONAL)
        {
            // Cannot cache transactional fields
            cachedPC.getLoadedFields()[fieldNumber] = false;
            return 0;
        }

        SingleValueFieldManager sfv = new SingleValueFieldManager();
        sm.provideFields(new int[]{fieldNumber}, sfv);
        cachedPC.getLoadedFields()[fieldNumber] = true;
        return sfv.fetchDoubleField(fieldNumber);
    }

    /* (non-Javadoc)
     * @see FieldSupplier#fetchFloatField(int)
     */
    public float fetchFloatField(int fieldNumber)
    {
        AbstractMemberMetaData mmd = 
            sm.getClassMetaData().getMetaDataForManagedMemberAtAbsolutePosition(fieldNumber);
        if (mmd.getPersistenceModifier() == FieldPersistenceModifier.TRANSACTIONAL)
        {
            // Cannot cache transactional fields
            cachedPC.getLoadedFields()[fieldNumber] = false;
            return 0;
        }

        SingleValueFieldManager sfv = new SingleValueFieldManager();
        sm.provideFields(new int[]{fieldNumber}, sfv);
        cachedPC.getLoadedFields()[fieldNumber] = true;
        return sfv.fetchFloatField(fieldNumber);
    }

    /* (non-Javadoc)
     * @see FieldSupplier#fetchIntField(int)
     */
    public int fetchIntField(int fieldNumber)
    {
        AbstractMemberMetaData mmd = 
            sm.getClassMetaData().getMetaDataForManagedMemberAtAbsolutePosition(fieldNumber);
        if (mmd.getPersistenceModifier() == FieldPersistenceModifier.TRANSACTIONAL)
        {
            // Cannot cache transactional fields
            cachedPC.getLoadedFields()[fieldNumber] = false;
            return 0;
        }

        SingleValueFieldManager sfv = new SingleValueFieldManager();
        sm.provideFields(new int[]{fieldNumber}, sfv);
        cachedPC.getLoadedFields()[fieldNumber] = true;
        return sfv.fetchIntField(fieldNumber);
    }

    /* (non-Javadoc)
     * @see FieldSupplier#fetchLongField(int)
     */
    public long fetchLongField(int fieldNumber)
    {
        AbstractMemberMetaData mmd = 
            sm.getClassMetaData().getMetaDataForManagedMemberAtAbsolutePosition(fieldNumber);
        if (mmd.getPersistenceModifier() == FieldPersistenceModifier.TRANSACTIONAL)
        {
            // Cannot cache transactional fields
            cachedPC.getLoadedFields()[fieldNumber] = false;
            return 0;
        }

        SingleValueFieldManager sfv = new SingleValueFieldManager();
        sm.provideFields(new int[]{fieldNumber}, sfv);
        cachedPC.getLoadedFields()[fieldNumber] = true;
        return sfv.fetchLongField(fieldNumber);
    }

    /* (non-Javadoc)
     * @see FieldSupplier#fetchShortField(int)
     */
    public short fetchShortField(int fieldNumber)
    {
        AbstractMemberMetaData mmd = 
            sm.getClassMetaData().getMetaDataForManagedMemberAtAbsolutePosition(fieldNumber);
        if (mmd.getPersistenceModifier() == FieldPersistenceModifier.TRANSACTIONAL)
        {
            // Cannot cache transactional fields
            cachedPC.getLoadedFields()[fieldNumber] = false;
            return 0;
        }

        SingleValueFieldManager sfv = new SingleValueFieldManager();
        sm.provideFields(new int[]{fieldNumber}, sfv);
        cachedPC.getLoadedFields()[fieldNumber] = true;
        return sfv.fetchShortField(fieldNumber);
    }

    /* (non-Javadoc)
     * @see FieldSupplier#fetchStringField(int)
     */
    public String fetchStringField(int fieldNumber)
    {
        AbstractMemberMetaData mmd = 
            sm.getClassMetaData().getMetaDataForManagedMemberAtAbsolutePosition(fieldNumber);
        if (mmd.getPersistenceModifier() == FieldPersistenceModifier.TRANSACTIONAL)
        {
            // Cannot cache transactional fields
            cachedPC.getLoadedFields()[fieldNumber] = false;
            return null;
        }

        SingleValueFieldManager sfv = new SingleValueFieldManager();
        sm.provideFields(new int[]{fieldNumber}, sfv);
        cachedPC.getLoadedFields()[fieldNumber] = true;
        return sfv.fetchStringField(fieldNumber);
    }
}