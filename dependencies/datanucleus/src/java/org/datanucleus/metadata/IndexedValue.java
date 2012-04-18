/**********************************************************************
Copyright (c) 2004 Andy Jefferson and others. All rights reserved.
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
package org.datanucleus.metadata;

import java.io.Serializable;

import org.datanucleus.util.StringUtils;

/**
 * Representation of whether an item is indexed or not.
 */
public class IndexedValue implements Serializable
{
    /** indexed true **/
    public static final IndexedValue TRUE=new IndexedValue(1);
    /** indexed false **/
    public static final IndexedValue FALSE=new IndexedValue(2);
    /** indexed unique **/
    public static final IndexedValue UNIQUE=new IndexedValue(3);

    /**
     * The type id.
     */
    private final int typeId;

    /**
     * constructor
     * @param i type id
     */
    private IndexedValue(int i)
    {
        this.typeId = i;
    }

    public int hashCode()
    {
        return typeId;
    }

    public boolean equals(Object o)
    {
        if (o instanceof IndexedValue)
        {
            return ((IndexedValue)o).typeId == typeId;
        }
        return false;
    }

    /**
     * Returns a string representation of the object.
     * @return a string representation of the object.
     */
    public String toString()
    {
        switch (typeId)
        {
            case 1 :
                return "true";
            case 2 :
                return "false";
            case 3 :
                return "unique";
        }
        return "";
    }

    /**
     * Accessor for the type.
     * @return Type
     **/
    public int getType()
    {
        return typeId;
    }

    /**
     * Obtain a IndexedValue for the given name by <code>value</code>
     * @param value the name
     * @return the IndexedValue found or IndexedValue.TRUE if not found. If <code>value</code> is null, returns null.
     */
    public static IndexedValue getIndexedValue(final String value)
    {
        if (StringUtils.isWhitespace(value))
        {
            return null;
        }
        else if (IndexedValue.TRUE.toString().equals(value))
        {
            return IndexedValue.TRUE;
        }
        else if (IndexedValue.FALSE.toString().equals(value))
        {
            return IndexedValue.FALSE;
        }
        else if (IndexedValue.UNIQUE.toString().equals(value))
        {
            return IndexedValue.UNIQUE;
        }
        return IndexedValue.TRUE;
    }
}