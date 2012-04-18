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
 * Query runtime statistics
 */
public interface QueryRuntimeMBean
{
    /**
     * The total number of queries that are executing
     * @return the total
     */
    long getQueryActiveTotalCount();

    /**
     * The total number of queries that failed executing
     * @return the total
     */
    long getQueryErrorTotalCount();

    /**
     * The total number of queries executed
     * @return the total
     */
    long getQueryExecutionTotalCount();
    
    /**
     * Lowest execution time
     * @return Lowest execution time in milleseconds
     */
    long getQueryExecutionTimeLow();

    /**
     * Highest execution time
     * @return Highest execution time in milleseconds
     */
    long getQueryExecutionTimeHigh();
    
    /**
     * execution total time
     * @return execution total time in milleseconds
     */
    long getQueryExecutionTotalTime();

    /**
     * Simple Moving Average execution time of transactions
     * @return Average execution time of transactions in milleseconds
     */
    long getQueryExecutionTimeAverage();
    
}
