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

import org.datanucleus.ClassLoaderResolver;

/**
 * Class to handle the conversion between java.lang.Class and a String form.
 */
public class ClassStringConverter implements ObjectStringConverter
{
    ClassLoaderResolver clr = null;

    public void setClassLoaderResolver(ClassLoaderResolver clr)
    {
        this.clr = clr;
    }

    /* (non-Javadoc)
     * @see org.datanucleus.store.types.ObjectStringConverter#toObject(java.lang.String)
     */
    public Object toObject(String str)
    {
        if (str == null)
        {
            return null;
        }

        try
        {
            if (clr != null)
            {
                return clr.classForName(str);
            }
            return Class.forName(str);
        }
        catch (ClassNotFoundException cnfe)
        {
            return null;
        }
    }

    /* (non-Javadoc)
     * @see org.datanucleus.store.types.ObjectStringConverter#toString(java.lang.Object)
     */
    public String toString(Object obj)
    {
        String str;
        if (obj instanceof Class)
        {
            str = ((Class)obj).getName();
        }
        else
        {
            str = (String)obj;
        }
        return str;
    }
}