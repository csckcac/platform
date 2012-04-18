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

import java.awt.Rectangle;
import java.util.StringTokenizer;

import org.datanucleus.exceptions.NucleusDataStoreException;

/**
 * Class to handle the conversion between java.awt.Rectangle and a String form.
 * The String form is <pre>(x,y,width,height)</pre>
 */
public class RectangleStringConverter implements ObjectStringConverter
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

        Rectangle r = new Rectangle();
        StringTokenizer tokeniser = new StringTokenizer(str.substring(1, str.length()-1), ",");

        double x = 0;
        if (tokeniser.hasMoreTokens())
        {
            String token = tokeniser.nextToken().trim();
            try
            {
                x = new Double(token).doubleValue();
            }
            catch (NumberFormatException nfe)
            {
                throw new NucleusDataStoreException(LOCALISER.msg("016002", str, Rectangle.class.getName()), nfe);
            }
        }
        else
        {
            return null;
        }

        double y = 0;
        if (tokeniser.hasMoreTokens())
        {
            String token = tokeniser.nextToken().trim();
            try
            {
                y = new Double(token).doubleValue();
            }
            catch (NumberFormatException nfe)
            {
                throw new NucleusDataStoreException(LOCALISER.msg("016002", str, Rectangle.class.getName()), nfe);
            }
        }
        else
        {
            return null;
        }

        double width = 0;
        if (tokeniser.hasMoreTokens())
        {
            String token = tokeniser.nextToken().trim();
            try
            {
                width = new Double(token).doubleValue();
            }
            catch (NumberFormatException nfe)
            {
                throw new NucleusDataStoreException(LOCALISER.msg("016002", str, Rectangle.class.getName()), nfe);
            }
        }
        else
        {
            return null;
        }

        double height = 0;
        if (tokeniser.hasMoreTokens())
        {
            String token = tokeniser.nextToken().trim();
            try
            {
                height = new Double(token).doubleValue();
            }
            catch (NumberFormatException nfe)
            {
                throw new NucleusDataStoreException(LOCALISER.msg("016002", str, Rectangle.class.getName()), nfe);
            }
        }
        else
        {
            return null;
        }

        r.setRect(x, y, width, height);
        return r;
    }

    /* (non-Javadoc)
     * @see org.datanucleus.store.types.ObjectStringConverter#toString(java.lang.Object)
     */
    public String toString(Object obj)
    {
        String str;
        if (obj instanceof Rectangle)
        {
            Rectangle r = (Rectangle)obj;
            str = "(" + r.getX() + "," + r.getY() + "," + r.getWidth() + "," + r.getHeight() + ")";
        }
        else
        {
            str = (String)obj;
        }
        return str;
    }
}