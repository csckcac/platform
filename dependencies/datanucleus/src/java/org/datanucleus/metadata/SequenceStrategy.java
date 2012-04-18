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
package org.datanucleus.metadata;

import java.io.Serializable;

/**
 * Representation of strategy of a Sequence.
 */
public class SequenceStrategy implements Serializable
{
    /** language="nontransactional" */
    public static final SequenceStrategy NONTRANSACTIONAL = new SequenceStrategy(1);

    /** language="continguous" */
    public static final SequenceStrategy CONTIGUOUS = new SequenceStrategy(2);

    /** language="noncontiguous" */
    public static final SequenceStrategy NONCONTIGUOUS = new SequenceStrategy(3);

    private final int typeId;

    private SequenceStrategy(int i)
    {
        this.typeId = i;
    }

    public int hashCode()
    {
        return typeId;
    }

    public boolean equals(Object o)
    {
        if (o instanceof SequenceStrategy)
        {
            return ((SequenceStrategy)o).typeId == typeId;
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
                return "nontransactional";
            case 2 :
                return "contiguous";
            case 3 :
                return "noncontiguous";
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
    public static SequenceStrategy getStrategy(final String value)
    {
        if (value == null)
        {
            return null;
        }
        else if (SequenceStrategy.NONTRANSACTIONAL.toString().equalsIgnoreCase(value))
        {
            return SequenceStrategy.NONTRANSACTIONAL;
        }
        else if (SequenceStrategy.CONTIGUOUS.toString().equalsIgnoreCase(value))
        {
            return SequenceStrategy.CONTIGUOUS;
        }
        else if (SequenceStrategy.NONCONTIGUOUS.toString().equalsIgnoreCase(value))
        {
            return SequenceStrategy.NONCONTIGUOUS;
        }
        return null;
    }
}