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
    ...
**********************************************************************/
package org.datanucleus.store.fieldmanager;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.datanucleus.api.ApiAdapter;
import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.store.ObjectProvider;

/**
 * Field manager that deletes all "dependent" PC objects referenced from the source object.
 * Effectively provides "delete-dependent".
 */
public class DeleteFieldManager extends AbstractFieldManager
{
    /** StateManager for the owning object. */
    private final ObjectProvider sm;

    /**
     * Constructor.
     * @param sm The state manager for the object.
     **/
    public DeleteFieldManager(ObjectProvider sm)
    {
        this.sm = sm;
    }

    /**
     * Utility method to process the passed persistable object.
     * @param pc The PC object
     */
    protected void processPersistable(Object pc)
    {
        ObjectProvider pcSM = sm.getExecutionContext().findObjectProvider(pc);
        if (pcSM != null)
        {
            if (pcSM.isDeleting() || pcSM.becomingDeleted())
            {
                // Already becoming deleted so jump out
                return;
            }
        }

        // Delete it
        sm.getExecutionContext().deleteObjectInternal(pc);
    }

    /**
     * Method to store an object field.
     * @param fieldNumber Number of the field (absolute)
     * @param value Value of the field
     */
    public void storeObjectField(int fieldNumber, Object value)
    {
        if (value != null)
        {
            AbstractMemberMetaData mmd = sm.getClassMetaData().getMetaDataForManagedMemberAtAbsolutePosition(fieldNumber);
            ApiAdapter api = sm.getExecutionContext().getApiAdapter();
            if (api.isPersistable(value))
            {
                // Process PC fields
                if (mmd.isDependent())
                {
                    processPersistable(value);
                }
            }
            else if (value instanceof Collection)
            {
                // Process all elements of the Collection that are PC
                boolean dependent = mmd.getCollection().isDependentElement();
                if (mmd.isCascadeRemoveOrphans())
                {
                    dependent = true;
                }
                if (mmd.hasCollection() && dependent)
                {
                    // Process any elements that are PersistenceCapable
                    Collection coll = (Collection)value;
                    Iterator iter = coll.iterator();
                    while (iter.hasNext())
                    {
                        Object element = iter.next();
                        if (api.isPersistable(element))
                        {
                            processPersistable(element);
                        }
                    }
                }
            }
            else if (value instanceof Map)
            {
                // Process all keys, values of the Map that are PC
                Map map = (Map)value;
                if (mmd.hasMap() && mmd.getMap().isDependentKey())
                {
                    // Process any keys that are PersistenceCapable
                    Set keys = map.keySet();
                    Iterator iter = keys.iterator();
                    while (iter.hasNext())
                    {
                        Object mapKey = iter.next();
                        if (api.isPersistable(mapKey))
                        {
                            processPersistable(mapKey);
                        }
                    }
                }
                if (mmd.hasMap() && mmd.getMap().isDependentValue())
                {
                    // Process any values that are PersistenceCapable
                    Collection values = map.values();
                    Iterator iter = values.iterator();
                    while (iter.hasNext())
                    {
                        Object mapValue = iter.next();
                        if (api.isPersistable(mapValue))
                        {
                            processPersistable(mapValue);
                        }
                    }
                }
            }
            else if (value instanceof Object[])
            {
                // Process all array elements that are PC
                if (mmd.hasArray() && mmd.getArray().isDependentElement())
                {
                    // Process any array elements that are PersistenceCapable
                    for (int i=0;i<Array.getLength(value);i++)
                    {
                        Object element = Array.get(value, i);
                        if (api.isPersistable(element))
                        {
                            processPersistable(element);
                        }
                    }
                }
            }
            else
            {
                // Primitive, or primitive array, or some unsupported container type
            }
        }
    }

    /**
     * Method to store a boolean field.
     * @param fieldNumber Number of the field (absolute)
     * @param value Value of the field
     */
    public void storeBooleanField(int fieldNumber, boolean value)
    {
        // Do nothing
    }

    /**
     * Method to store a byte field.
     * @param fieldNumber Number of the field (absolute)
     * @param value Value of the field
     */
    public void storeByteField(int fieldNumber, byte value)
    {
        // Do nothing
    }

    /**
     * Method to store a char field.
     * @param fieldNumber Number of the field (absolute)
     * @param value Value of the field
     */
    public void storeCharField(int fieldNumber, char value)
    {
        // Do nothing
    }

    /**
     * Method to store a double field.
     * @param fieldNumber Number of the field (absolute)
     * @param value Value of the field
     */
    public void storeDoubleField(int fieldNumber, double value)
    {
        // Do nothing
    }

    /**
     * Method to store a float field.
     * @param fieldNumber Number of the field (absolute)
     * @param value Value of the field
     */
    public void storeFloatField(int fieldNumber, float value)
    {
        // Do nothing
    }

    /**
     * Method to store an int field.
     * @param fieldNumber Number of the field (absolute)
     * @param value Value of the field
     */
    public void storeIntField(int fieldNumber, int value)
    {
        // Do nothing
    }

    /**
     * Method to store a long field.
     * @param fieldNumber Number of the field (absolute)
     * @param value Value of the field
     */
    public void storeLongField(int fieldNumber, long value)
    {
        // Do nothing
    }

    /**
     * Method to store a short field.
     * @param fieldNumber Number of the field (absolute)
     * @param value Value of the field
     */
    public void storeShortField(int fieldNumber, short value)
    {
        // Do nothing
    }

    /**
     * Method to store a string field.
     * @param fieldNumber Number of the field (absolute)
     * @param value Value of the field
     */
    public void storeStringField(int fieldNumber, String value)
    {
        // Do nothing
    }
}