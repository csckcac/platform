/**********************************************************************
Copyright (c) 2005 Andy Jefferson and others. All rights reserved.
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
package org.datanucleus.cache;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.datanucleus.util.StringUtils;

/**
 * An object that is stored in the Level2 Cache keyed by the identity of the persistable object.
 * Comprises a persistable object, its loaded fields array, the version of the persistable object that is stored,
 * and a map of relation field values.
 * The loaded fields and version are used when re-instating the object getting it out of the cache in a
 * consistent state for when it was put in the cache.
 * The relation fields map is used to store the OIDs of any referenced persistable objects. If we have a field
 * "otherObject" that stores a persistable object then we will have an entry keyed by "otherObject" and will
 * contain the OID of the other persistable object. Similarly if we have a field "elements" that is a Collection
 * of persistable objects then we will have an entry keyed by "elements" and will contain a Collection (of
 * the correct instantiation type) containing the OIDs of the element objects. Similarly for Maps, arrays.
 */
public class CachedPC implements Serializable
{
    /**
     * The persistable object. 
     * If this isn't Serializable then some L2 caches will throw errors upon serialisation.
     */
    private Object pc;

    /** The loaded fields array */
    private boolean[] loadedFields;

    /** Version of the cached object (if any) - Long, Timestamp etc. */
    private Object version;

    /** Map of relation fields values, keyed by the field name. */
    private Map<String, Object> relationFields = null;

    /**
     * Constructor.
     * @param pc The persistable object
     * @param loadedFields The loaded fields
     * @param vers The version (optional)
     */
    public CachedPC(Object pc, boolean[] loadedFields, Object vers)
    {
        this.pc = pc;
        setLoadedFields(loadedFields);
        setVersion(vers);
    }

    /**
     * Accessor for the persistable object
     * @return The PC object
     */
    public Object getPersistableObject()
    {
        return pc;
    }

    /**
     * Accessor for the class of the persistable object.
     * @return The class of the object
     */
    public Class getPCClass()
    {
        return pc.getClass();
    }

    /**
     * Version accessor.
     * @return Version of cached object
     */
    public Object getVersion()
    {
        return version;
    }

    /**
     * Setter for the version.
     * Called when updating a stored CachedPC.
     * @param ver New version
     */
    public void setVersion(Object ver)
    {
        this.version = ver;
    }

    /**
     * Accessor for the loaded fields of this object
     * @return The loaded fields
     */
    public boolean[] getLoadedFields()
    {
        return loadedFields;
    }

    /**
     * Mutator for the loaded fields.
     * Called when updating a stored CachedPC.
     * @param flds The loaded fields
     */
    public void setLoadedFields(boolean[] flds)
    {
        this.loadedFields = new boolean[flds.length];
        for (int i=0;i<flds.length;i++)
        {
            this.loadedFields[i] = flds[i];
        }
    }

    /**
     * Setter for the value for the relation field with the specified name.
     * Passing in null for the value will remove the relation field entry.
     * @param fieldName Name of field
     * @param value The value. For a field storing a PC object this is an OID. For a Collection field
     *     this is a Collection<OID>. For a Map field this is a Map<OID, OID>
     */
    public void setRelationField(String fieldName, Object value)
    {
        if (relationFields == null)
        {
            relationFields = new HashMap();
        }
        if (value == null)
        {
            relationFields.remove(fieldName);
        }
        else
        {
            relationFields.put(fieldName, value);
        }
    }

    /**
     * Accessor for the value for the relation field at the field with the specified name.
     * @param fieldName Name of field
     * @return The value
     */
    public Object getRelationField(String fieldName)
    {
        if (relationFields == null)
        {
            return null;
        }
        return relationFields.get(fieldName);
    }

    public String[] getRelationFieldNames()
    {
        if (relationFields == null)
        {
            return null;
        }
        return relationFields.keySet().toArray(new String[relationFields.size()]);
    }

    /**
     * Method to return a sting form of the cached object.
     * Returns something like "CachedPC : org.datanucleus.test.A@6295eb version=1 loadedFlags=[YY]"
     * @return The string form
     */
    public String toString()
    {
        return "CachedPC : " + StringUtils.toJVMIDString(pc) + " version=" + version +
            " loadedFlags=" + StringUtils.booleanArrayToString(loadedFields);
    }
}