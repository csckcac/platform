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
package org.datanucleus.metadata;

import java.io.Serializable;

/**
 * Representation of a transaction type.
 */
public class TransactionType implements Serializable
{
    /** JTA transaction. */
    public static final TransactionType JTA = new TransactionType(1);

    /** Local transaction. */
    public static final TransactionType RESOURCE_LOCAL = new TransactionType(2);

    private final int typeId;

    private TransactionType(int i)
    {
        this.typeId = i;
    }

    public int hashCode()
    {
        return typeId;
    }

    public boolean equals(Object o)
    {
        if (o instanceof TransactionType)
        {
            return ((TransactionType)o).typeId == typeId;
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
                return "JTA";
            case 2 :
                return "RESOURCE_LOCAL";
        }
        return "";
    }

    /**
     * Accessor to the sequence strategy type
     * @return the type
     */    
    public int getType()
    {
        return typeId;
    }

    /**
     * Return Sequence strategy from String.
     * @param value sequence strategy
     * @return Instance of SequenceStrategy. If parse failed, return null.
     */
    public static TransactionType getValue(final String value)
    {
        if (value == null)
        {
            return null;
        }
        else if (TransactionType.JTA.toString().equalsIgnoreCase(value))
        {
            return TransactionType.JTA;
        }
        else if (TransactionType.RESOURCE_LOCAL.toString().equalsIgnoreCase(value))
        {
            return TransactionType.RESOURCE_LOCAL;
        }
        return null;
    }
}