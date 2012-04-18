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
 * Transaction System Statistics. Statistics for all transactions  
 */
public class TransactionRuntime implements TransactionRuntimeMBean
{
    /** total number of committed transactions **/
    long transactionTotalCount;
    /** total number of committed transactions **/
    long transactionCommittedTotalCount;
    /** total number of rolled back transactions **/
    long transactionRolledBackTotalCount;
    /** total number of active transactions **/
    long transactionActiveTotalCount;
    
    /** execution time in average **/
    MathUtils.SMA executionTimeAverage = new MathUtils.SMA(50);

    /** execution total time **/
    long executionTotalTime = 0;
    
    /** highest execution time **/
    long executionTimeHigh =-1;

    /** lowest execution time **/
    long executionTimeLow =-1;

    /**
     * Simple Moving Average execution time of transactions
     * @return Average execution time of transactions in milleseconds
     */
    public long getTransactionExecutionTimeAverage()
    {
        return (long) executionTimeAverage.currentAverage();
    }

    /**
     * Lowest execution time
     * @return Lowest execution time in milleseconds
     */
    public long getTransactionExecutionTimeLow()
    {
        return executionTimeLow;
    }

    /**
     * Highest execution time
     * @return Highest execution time in milleseconds
     */
    public long getTransactionExecutionTimeHigh()
    {
        return executionTimeHigh;
    }

    /**
     * execution total time
     * @return execution total time in milleseconds
     */
    public long getTransactionExecutionTotalTime()
    {
        return executionTotalTime;
    }

    /* (non-Javadoc)
     * @see org.datanucleus.management.runtime.TransactionRuntimeMBean#getTransactionTotalCount()
     */
    public long getTransactionTotalCount()
    {
        return transactionTotalCount;
    }
    
    public long getTransactionActiveTotalCount()
    {
        return transactionActiveTotalCount;
    }

    /* (non-Javadoc)
     * @see org.datanucleus.management.runtime.TransactionRuntimeMBean#getTransactionCommittedTotalCount()
     */
    public long getTransactionCommittedTotalCount()
    {
        return transactionCommittedTotalCount;
    }
    
    /* (non-Javadoc)
     * @see org.datanucleus.management.runtime.TransactionRuntimeMBean#getTransactionRolledBackTotalCount()
     */
    public long getTransactionRolledBackTotalCount()
    {
        return transactionRolledBackTotalCount;
    }
    
    public void transactionCommitted(long executionTime)
    {
        this.transactionCommittedTotalCount++;
        this.transactionActiveTotalCount--;
        executionTimeAverage.compute(executionTime);
        executionTimeLow = Math.min(executionTimeLow==-1?executionTime:executionTimeLow,executionTime);
        executionTimeHigh = Math.max(executionTimeHigh,executionTime);
        executionTotalTime += executionTime;
    }
    
    public void transactionRolledBack(long executionTime)
    {
        this.transactionRolledBackTotalCount++;
        this.transactionActiveTotalCount--;
        executionTimeAverage.compute(executionTime);
        executionTimeLow = Math.min(executionTimeLow==-1?executionTime:executionTimeLow,executionTime);
        executionTimeHigh = Math.max(executionTimeHigh,executionTime);
        executionTotalTime += executionTime;
    }
    
    public void transactionStarted()
    {
        this.transactionTotalCount++;
        this.transactionActiveTotalCount++;
    }
}
