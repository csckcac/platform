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

/**
 * Representation of the values for discriminator "strategy".
 */
public class DiscriminatorStrategy implements Serializable
{
    /** strategy="none" */
    public static final DiscriminatorStrategy NONE=new DiscriminatorStrategy(0);

    /** strategy="value-map" */
    public static final DiscriminatorStrategy VALUE_MAP=new DiscriminatorStrategy(1);

    /** strategy="class-name" */
    public static final DiscriminatorStrategy CLASS_NAME=new DiscriminatorStrategy(2);

    /** The type id. */
    private final int typeId;

    /**
     * Constructor
     * @param id type id
     */
    private DiscriminatorStrategy(int id)
    {
        this.typeId = id;
    }

    public int hashCode()
    {
        return typeId;
    }

    public boolean equals(Object o)
    {
        if (o instanceof DiscriminatorStrategy)
        {
            return ((DiscriminatorStrategy)o).typeId == typeId;
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
            case 0:
                return "none";
            case 1 :
                return "value-map";
            case 2 :
                return "class-name";
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
     * Accessor for the strategy
     * @param value The string form
     * @return The strategy
     */
    public static DiscriminatorStrategy getDiscriminatorStrategy(final String value)
    {
        if (value == null)
        {
            return null;
        }
        else if (DiscriminatorStrategy.NONE.toString().equals(value))
        {
            return DiscriminatorStrategy.NONE;
        }
        else if (DiscriminatorStrategy.VALUE_MAP.toString().equals(value))
        {
            return DiscriminatorStrategy.VALUE_MAP;
        }
        else if (DiscriminatorStrategy.CLASS_NAME.toString().equals(value))
        {
            return DiscriminatorStrategy.CLASS_NAME;
        }
        return null;
    }
}