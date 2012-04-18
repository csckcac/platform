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
package org.datanucleus.cache;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Implementation of a Level 1 cache keeping strong references to the objects.
 * This means that objects are not garbage collected, and have to be removed directly by calls to remove(). 
 * This differs from the WeakRefCache/SoftRefCache which do not guarantee to retain objects.
 */
public class StrongRefCache implements Level1Cache
{
    private Map hardCache = new HashMap();

    /**
     * Default constructor (required)
     */
	public StrongRefCache()
	{
		//default constructor
	}

	public synchronized Object put(Object key, Object value)
	{
		return hardCache.put(key,value);        
	}

	public Object get(Object key)
	{
		return hardCache.get(key);
	}

	public boolean containsKey(Object key)
	{
		return hardCache.containsKey(key);
	}

	public synchronized Object remove(Object key)
	{
		return hardCache.remove(key);       
	}

	public synchronized void clear()
	{
		hardCache.clear();
	}
    
	/* (non-Javadoc)
	 * @see java.util.Map#containsValue(java.lang.Object)
	 */
	public boolean containsValue(Object value)
	{
		return hardCache.containsValue(value);
	}

	/* (non-Javadoc)
	 * @see java.util.Map#entrySet()
	 */
	public Set entrySet()
	{
		return hardCache.entrySet();
	}

	/* (non-Javadoc)
	 * @see java.util.Map#isEmpty()
	 */
	public boolean isEmpty()
	{
		return hardCache.isEmpty();
	}

	/* (non-Javadoc)
	 * @see java.util.Map#keySet()
	 */
	public Set keySet()
	{
		return hardCache.keySet();
	}

	/* (non-Javadoc)
	 * @see java.util.Map#putAll(java.util.Map)
	 */
	public synchronized void putAll(Map t)
	{
		hardCache.putAll(t);
	}

	/* (non-Javadoc)
	 * @see java.util.Map#size()
	 */
	public int size()
	{
		return hardCache.size();
	}

	/* (non-Javadoc)
	 * @see java.util.Map#values()
	 */
	public Collection values()
	{
		return hardCache.values();
	}
}