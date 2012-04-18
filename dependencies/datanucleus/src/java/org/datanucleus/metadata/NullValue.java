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
 * Representation of how to handle a null value (in a field).
 */
public class NullValue implements Serializable
{
    /** null-value="exception" (throw an exception when needing to persist and finding a null). */
    public static final NullValue EXCEPTION = new NullValue(1);

    /** null-value="default" (put in the default value when needing to persist and finding a null). */
    public static final NullValue DEFAULT = new NullValue(2);

    /** null-value="none" (persist null when needing to persist and finding a null). */
    public static final NullValue NONE = new NullValue(3);

    /** The type id */
    private final int typeId;

    /**
     * constructor
     * @param i type id
     */
    private NullValue(int i)
    {
        this.typeId = i;
    }

    public int hashCode()
    {
        return typeId;
    }

    public boolean equals(Object o)
    {
        if (o instanceof NullValue)
        {
            return ((NullValue)o).typeId == typeId;
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
                return "exception";
            case 2 :
                return "default";
            case 3 :
                return "none";
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
     * Obtain a NullValue for the given name by <code>value</code>
     * @param value the name
     * @return the NullValue found or NullValue.NONE if not found. If <code>value</code> is null, returns NullValue.NONE.
     */    
    public static NullValue getNullValue(final String value)
    {
        if (StringUtils.isWhitespace(value))
        {
            return NullValue.NONE;
        }
        else if (NullValue.DEFAULT.toString().equals(value))
        {
            return NullValue.DEFAULT;
        }
        else if (NullValue.EXCEPTION.toString().equals(value))
        {
            return NullValue.EXCEPTION;
        }
        else if (NullValue.NONE.toString().equals(value))
        {
            return NullValue.NONE;
        }
        return NullValue.NONE;
    }
}