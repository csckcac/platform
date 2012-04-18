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
 * Transaction Statistics  
 */
public interface TransactionRuntimeMBean
{

    /**
     * Simple Moving Average execution time of transactions
     * @return Average execution time of transactions in milleseconds
     */
    long getTransactionExecutionTimeAverage();

    /**
     * Lowest execution time
     * @return Lowest execution time in milleseconds
     */
    long getTransactionExecutionTimeLow();
    
    /**
     * Highest execution time
     * @return Highest execution time in milleseconds
     */
    long getTransactionExecutionTimeHigh();

    /**
     * execution total time
     * @return execution total time in milleseconds
     */
    long getTransactionExecutionTotalTime();

    /**
     * Total number of transactions
     * @return Total number of transactions
     */
    long getTransactionTotalCount();

    /**
     * Total number of active transactions
     * @return Total number of active transactions
     */
    long getTransactionActiveTotalCount();
    
    /**
     * Total number of committed transactions
     * @return Total number of committed transactions
     */
     long getTransactionCommittedTotalCount();

    /**
     * Total number of rolled back transactions
     * @return Total number of rolled back transactions
     */
    long getTransactionRolledBackTotalCount();

}