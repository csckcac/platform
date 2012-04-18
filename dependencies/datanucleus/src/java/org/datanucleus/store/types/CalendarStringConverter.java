/**********************************************************************
Copyright (c) 2011 Andy Jefferson and others. All rights reserved.
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.datanucleus.exceptions.NucleusDataStoreException;

/**
 * Class to handle the conversion between java.util.Calendar and a String form.
 */
public class CalendarStringConverter implements ObjectStringConverter
{
    static SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");

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
            Date date = format.parse(str);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            return cal;
        }
        catch (ParseException pe)
        {
            throw new NucleusDataStoreException(LOCALISER.msg("016002", str, Calendar.class.getName()), pe);
        }
    }

    /* (non-Javadoc)
     * @see org.datanucleus.store.types.ObjectStringConverter#toString(java.lang.Object)
     */
    public String toString(Object obj)
    {
        String str;
        if (obj instanceof Calendar)
        {
            str = format.format(((Calendar)obj).getTime());
        }
        else
        {
            str = (String)obj;
        }
        return str;
    }
}