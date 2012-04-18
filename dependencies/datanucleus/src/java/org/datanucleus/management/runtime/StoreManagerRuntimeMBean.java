/**********************************************************************
Copyright (c) 2007 Erik Bengtson and others. All rights reserved.
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
***********************************************************************/
package org.datanucleus.management.runtime;

/**
 * Exposes runtime statistics and operations for the StoreManager
 */
public interface StoreManagerRuntimeMBean
{

    /**
     * The StoreManager start time
     * @return StoreManager start time
     */
    long getStartTime();

    /**
     * The number of deletes of FCO objects in the data store 
     * @return number of deletes of FCO objects in the data store 
     */
    long getDeleteCount();

    /**
     * The number of inserts of FCO objects in the data store 
     * @return number of inserts of FCO objects in the data store 
     */
    long getFetchCount();

    /**
     * The number of inserts of FCO objects in the data store 
     * @return number of inserts of FCO objects in the data store 
     */
    long getInsertCount();

    /**
     * The number of updates of FCO objects in the data store 
     * @return number of updates of FCO objects in the data store 
     */
    long getUpdateCount();

}