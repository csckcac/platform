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
**********************************************************************/
package org.datanucleus.management.runtime;

/**
 * Runtime information about the ConnectionManager 
 */
public class ConnectionManagerRuntime implements ConnectionManagerRuntimeMBean
{
    long connectionActiveCurrent;
    
    long connectionActiveHigh;

    long connectionActiveTotal;

    public long getConnectionActiveCurrent()
    {
        return this.connectionActiveCurrent;
    }

    public long getConnectionActiveHigh()
    {
        return this.connectionActiveHigh;
    }

    public long getConnectionActiveTotal()
    {
        return this.connectionActiveTotal;
    }

    public void incrementActiveConnections()
    {
        this.connectionActiveCurrent++;
        this.connectionActiveTotal++;
        this.connectionActiveHigh = Math.max(this.connectionActiveHigh, this.connectionActiveCurrent);
    }

    public void decrementActiveConnections()
    {
        this.connectionActiveCurrent--;
    }

}