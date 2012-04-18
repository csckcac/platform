/**********************************************************************
Copyright (c) 2010 Andy Jefferson and others. All rights reserved.
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
package org.datanucleus.store.types;

import java.util.BitSet;
import java.util.StringTokenizer;

import org.datanucleus.exceptions.NucleusDataStoreException;

/**
 * Class to handle the conversion between java.util.BitSet and a String form.
 */
public class BitSetStringConverter implements ObjectStringConverter
{
    /* (non-Javadoc)
     * @see org.datanucleus.store.types.ObjectStringConverter#toObject(java.lang.String)
     */
    public Object toObject(String str)
    {
        if (str == null)
        {
            return null;
        }

        BitSet set = new BitSet();
        StringTokenizer tokeniser = new StringTokenizer(str.substring(1, str.length()-1), ",");
        while (tokeniser.hasMoreTokens())
        {
            String token = tokeniser.nextToken().trim();
            try
            {
                int position = new Integer(token).intValue();
                set.set(position);
            }
            catch (NumberFormatException nfe)
            {
                throw new NucleusDataStoreException(LOCALISER.msg("016002", str, BitSet.class.getName()), nfe);
            }
        }
        return set;
    }

    /* (non-Javadoc)
     * @see org.datanucleus.store.types.ObjectStringConverter#toString(java.lang.Object)
     */
    public String toString(Object obj)
    {
        String str;
        if (obj instanceof BitSet)
        {
            BitSet set = (BitSet)obj;
            str = set.toString();
        }
        else
        {
            str = (String)obj;
        }

        return str;
    }
}