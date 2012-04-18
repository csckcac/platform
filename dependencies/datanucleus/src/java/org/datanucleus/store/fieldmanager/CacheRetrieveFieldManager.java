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
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.datanucleus.cache.CachedPC;
import org.datanucleus.exceptions.NucleusException;
import org.datanucleus.exceptions.NucleusObjectNotFoundException;
import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.store.ObjectProvider;
import org.datanucleus.util.NucleusLogger;

/**
 * FieldManager to handle the retrieval of fields from a L2 cached object into a managed object.
 * Will be called for all fields that are loaded in an L2 cached object.
 * Any relation fields will be handled using the OIDs stored in "relationFields" in the CachedPC.
 * These OIDs are taken and the ObjectManager.findObject() is called. This can result in getting that
 * object from the Level2 cache also, or maybe from the Level1 if it is already present there.
 */
public class CacheRetrieveFieldManager extends AbstractFieldManager
{
    /** StateManager of the cached object (temporarily connected) to get values from. */
    ObjectProvider cacheSM;

    /** StateManager of the managed object we are updating. */
    ObjectProvider sm;

    /** Object taken from L2 cache that we are retrieving the values from. */
    CachedPC cachedPC;

    /**
     * Constructor for a field manager for retrieval from the cache.
     * @param sm StateManager of the object being updated
     * @param cacheSM StateManager of the L2 cached object (temporarily connected to allow access to field values)
     * @param cachedPC The L2 cached object we are taking values from
     */
    public CacheRetrieveFieldManager(ObjectProvider sm, ObjectProvider cacheSM, CachedPC cachedPC)
    {
        super();
        this.sm = sm;
        this.cacheSM = cacheSM;
        this.cachedPC = cachedPC;
    }

    /**
     * Method to fetch an object field whether it is collection, map, PC, or whatever.
     * @param fieldNumber Number of the field
     * @return The object stored in this field
     */
    public Object fetchObjectField(int fieldNumber)
    {
        SingleValueFieldManager sfv = new SingleValueFieldManager();
        cacheSM.provideFields(new int[]{fieldNumber}, sfv);
        Object value = sfv.fetchObjectField(fieldNumber);
        AbstractMemberMetaData mmd =
            cacheSM.getClassMetaData().getMetaDataForManagedMemberAtAbsolutePosition(fieldNumber);

        Object relationFieldValue = cachedPC.getRelationField(mmd.getName());
        if (relationFieldValue != null)
        {
            // Field stores a relation and has a value
            if (Collection.class.isAssignableFrom(relationFieldValue.getClass()))
            {
                // Collection field, with relationFieldValue being Collection<OID>
                Collection coll = (Collection)relationFieldValue;
                try
                {
                    // Create Collection<Element> of same type as relationFieldValue
                    Collection fieldColl = coll.getClass().newInstance();
                    Iterator iter = coll.iterator();
                    while (iter.hasNext())
                    {
                        Object elementOid = iter.next();
                        if (elementOid != null)
                        {
                            Object element = cacheSM.getExecutionContext().findObject(elementOid, null, null, false);
                            fieldColl.add(element);
                        }
                        else
                        {
                            fieldColl.add(elementOid);
                        }
                    }
                    return sm.wrapSCOField(fieldNumber, fieldColl, false, false, true);
                }
                catch (Exception e)
                {
                    // Error creating field value
                    NucleusLogger.CACHE.error("Exception thrown creating value for" +
                        " field " + mmd.getFullFieldName() + 
                        " of type " + relationFieldValue.getClass().getName(), e);
                    return null;
                }
            }
            else if (Map.class.isAssignableFrom(relationFieldValue.getClass()))
            {
                // Map field, with relationFieldValue being Map<OID, OID>
                Map map = (Map)relationFieldValue;
                try
                {
                    // Create Map<Key, Value> of same type as relationFieldValue
                    Map fieldMap = map.getClass().newInstance();
                    Iterator iter = map.entrySet().iterator();
                    while (iter.hasNext())
                    {
                        Map.Entry entry = (Map.Entry)iter.next();

                        Object mapKey = null;
                        if (mmd.getMap().keyIsPersistent())
                        {
                            mapKey = cacheSM.getExecutionContext().findObject(entry.getKey(), null, null, false);
                        }
                        else
                        {
                            mapKey = entry.getKey();
                        }

                        Object mapValue = null;
                        if (mmd.getMap().valueIsPersistent())
                        {
                            mapValue = cacheSM.getExecutionContext().findObject(entry.getValue(), null, null, false);
                        }
                        else
                        {
                            mapValue = entry.getValue();
                        }

                        fieldMap.put(mapKey, mapValue);
                    }
                    return sm.wrapSCOField(fieldNumber, fieldMap, false, false, true);
                }
                catch (Exception e)
                {
                    // Error creating field value
                    NucleusLogger.CACHE.error("Exception thrown creating value for" +
                        " field " + mmd.getFullFieldName() + 
                        " of type " + relationFieldValue.getClass().getName(), e);
                    return null;
                }
            }
            else if (relationFieldValue.getClass().isArray())
            {
                try
                {
                    Object[] elementOIDs = (Object[])relationFieldValue;
                    Class componentType = mmd.getType().getComponentType();
                    Object fieldArr = Array.newInstance(componentType, elementOIDs.length);
                    boolean persistableElement = cacheSM.getExecutionContext().getApiAdapter().isPersistable(componentType);
                    for (int i=0;i<elementOIDs.length;i++)
                    {
                        Object element = null;
                        if (elementOIDs[i] == null)
                        {
                            element = null;
                        }
                        else if (componentType.isInterface() || persistableElement || componentType == Object.class)
                        {
                            element = cacheSM.getExecutionContext().findObject(elementOIDs[i], null, null, false);
                        }
                        else
                        {
                            element = elementOIDs[i];
                        }
                        Array.set(fieldArr, i, element);
                    }
                    return fieldArr;
                }
                catch (NucleusException ne)
                {
                    // Unable to find element(s) so set field to unloaded
                    // TODO Unload the field
                    NucleusLogger.CACHE.error(
                        "Exception thrown trying to find element of array while getting object with id " + 
                        cacheSM.getInternalObjectId() + " from the L2 cache", ne);
                    return null;
                }
            }
            else
            {
                // PC field so assume is the identity of the object
                try
                {
                    return cacheSM.getExecutionContext().findObject(relationFieldValue, null, null, false);
                }
                catch (NucleusObjectNotFoundException nonfe)
                {
                    // TODO Log this and set field to not loaded
                    return null;
                }
            }
        }
        else
        {
            if (value == null)
            {
                return null;
            }
            else if (value.getClass() == StringBuffer.class)
            {
                // Make a copy of a StringBuffer from the cache since it is mutable but final
                return new StringBuffer(((StringBuffer)value).toString());
            }

            boolean[] mutables = mmd.getAbstractClassMetaData().getSCOMutableMemberFlags();
            if (mutables[fieldNumber])
            {
                return sm.wrapSCOField(fieldNumber, value, false, false, true);
            }

            return value;
        }
    }

    /* (non-Javadoc)
     * @see FieldSupplier#fetchBooleanField(int)
     */
    public boolean fetchBooleanField(int fieldNumber)
    {
        SingleValueFieldManager sfv = new SingleValueFieldManager();
        cacheSM.provideFields(new int[]{fieldNumber}, sfv);
        return sfv.fetchBooleanField(fieldNumber);
    }

    /* (non-Javadoc)
     * @see FieldSupplier#fetchByteField(int)
     */
    public byte fetchByteField(int fieldNumber)
    {
        SingleValueFieldManager sfv = new SingleValueFieldManager();
        cacheSM.provideFields(new int[]{fieldNumber}, sfv);
        return sfv.fetchByteField(fieldNumber);
    }

    /* (non-Javadoc)
     * @see FieldSupplier#fetchCharField(int)
     */
    public char fetchCharField(int fieldNumber)
    {
        SingleValueFieldManager sfv = new SingleValueFieldManager();
        cacheSM.provideFields(new int[]{fieldNumber}, sfv);
        return sfv.fetchCharField(fieldNumber);
    }

    /* (non-Javadoc)
     * @see FieldSupplier#fetchDoubleField(int)
     */
    public double fetchDoubleField(int fieldNumber)
    {
        SingleValueFieldManager sfv = new SingleValueFieldManager();
        cacheSM.provideFields(new int[]{fieldNumber}, sfv);
        return sfv.fetchDoubleField(fieldNumber);
    }

    /* (non-Javadoc)
     * @see FieldSupplier#fetchFloatField(int)
     */
    public float fetchFloatField(int fieldNumber)
    {
        SingleValueFieldManager sfv = new SingleValueFieldManager();
        cacheSM.provideFields(new int[]{fieldNumber}, sfv);
        return sfv.fetchFloatField(fieldNumber);
    }

    /* (non-Javadoc)
     * @see FieldSupplier#fetchIntField(int)
     */
    public int fetchIntField(int fieldNumber)
    {
        SingleValueFieldManager sfv = new SingleValueFieldManager();
        cacheSM.provideFields(new int[]{fieldNumber}, sfv);
        return sfv.fetchIntField(fieldNumber);
    }

    /* (non-Javadoc)
     * @see FieldSupplier#fetchLongField(int)
     */
    public long fetchLongField(int fieldNumber)
    {
        SingleValueFieldManager sfv = new SingleValueFieldManager();
        cacheSM.provideFields(new int[]{fieldNumber}, sfv);
        return sfv.fetchLongField(fieldNumber);
    }

    /* (non-Javadoc)
     * @see FieldSupplier#fetchShortField(int)
     */
    public short fetchShortField(int fieldNumber)
    {
        SingleValueFieldManager sfv = new SingleValueFieldManager();
        cacheSM.provideFields(new int[]{fieldNumber}, sfv);
        return sfv.fetchShortField(fieldNumber);
    }

    /* (non-Javadoc)
     * @see FieldSupplier#fetchStringField(int)
     */
    public String fetchStringField(int fieldNumber)
    {
        SingleValueFieldManager sfv = new SingleValueFieldManager();
        cacheSM.provideFields(new int[]{fieldNumber}, sfv);
        return sfv.fetchStringField(fieldNumber);
    }
}