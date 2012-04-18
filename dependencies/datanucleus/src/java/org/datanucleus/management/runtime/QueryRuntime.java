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

import org.datanucleus.util.MathUtils;

/**
 * Query runtime statistics
 */
public class QueryRuntime implements QueryRuntimeMBean
{
    long activeTotalCount;
    
    long errorTotalCount;
    
    long executionTotalCount;
    
    /** execution total time **/
    long executionTotalTime = 0;
    
    /** highest execution time **/
    long executionTimeHigh =-1;

    /** lowest execution time **/
    long executionTimeLow =-1;

    /** execution time in average **/
    MathUtils.SMA executionTimeAverage = new MathUtils.SMA(50);
    
    /**
     * The total number of queries that are executing
     * @return the total
     */
    public long getQueryActiveTotalCount()
    {
        return activeTotalCount;
    }
    
    /**
     * The total number of queries that failed executing
     * @return the total
     */
    public long getQueryErrorTotalCount()
    {
        return errorTotalCount;
    }
    
    /**
     * The total number of queries executed
     * @return the total
     */
    public long getQueryExecutionTotalCount()
    {
        return executionTotalCount;
    }
    
    /**
     * Lowest execution time
     * @return Lowest execution time in milleseconds
     */
    public long getQueryExecutionTimeLow()
    {
        return executionTimeLow;
    }

    /**
     * Highest execution time
     * @return Highest execution time in milleseconds
     */
    public long getQueryExecutionTimeHigh()
    {
        return executionTimeHigh;
    }
    
    /**
     * execution total time
     * @return execution total time in milleseconds
     */
    public long getQueryExecutionTotalTime()
    {
        return executionTotalTime;
    }

    /**
     * Simple Moving Average execution time of transactions
     * @return Average execution time of transactions in milleseconds
     */
    public long getQueryExecutionTimeAverage()
    {
        return (long) executionTimeAverage.currentAverage();
    }

    public void queryBegin()
    {
        this.activeTotalCount++;
    }

    public void queryExecutedWithError()
    {
        this.errorTotalCount++;
        this.activeTotalCount--;
    }
    
    public void queryExecuted(long executionTime)
    {
        this.executionTotalCount++;
        this.activeTotalCount--;
        executionTimeAverage.compute(executionTime);
        executionTimeLow = Math.min(executionTimeLow==-1?executionTime:executionTimeLow,executionTime);
        executionTimeHigh = Math.max(executionTimeHigh,executionTime);
        executionTotalTime += executionTime;
    }    
}