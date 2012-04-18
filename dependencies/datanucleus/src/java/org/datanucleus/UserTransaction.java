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
package org.datanucleus;

/**
 * UserTransaction is the interface exposed to User Applications.
 * It allows proprietary extensions to be used by user applications.
 * To exemplify the usage of this interface, the below is a JDO snippet:
 * <code>
 * Transaction tx = pm.currentTransaction();
 * ((UserTransaction)tx).setUseUpdateLock(true);
 * </code>
 * 
 * This interface does not make any distinction between user APIs, such as
 * JDO or JPA, neither the datastores, such as RDBMS or DB4O. Unsupported
 * operations will throw {@link UnsupportedOperationException}.
 * 
 * User applications must be aware that the behaviour of this interface and
 * effects caused by invoking these operations may not be portable between 
 * different datastores kinds (RBDMS, DB4O, LDAP, etc )or datastores of same 
 * kind (RDBMS Oracle, RDBMS DB2, RDBMS MySQL, RDBMS Derby, etc). 
 */
public interface UserTransaction
{
    /**
     * Turn on serialized access to data fetch from datastore.
     * Calling this in the middle of a transaction will only affect data read after it.
     * Some datastores do not support Update Lock feature, and for such datastores
     * this setting is silently ignored.
     */
    void useUpdateLockOnFetch();
    
    /**
     * Turn on/off serialized access to data fetch from datastore.
     * Calling this in the middle of a transaction will only affect data read after it.
     * Some datastores do not support Update Lock feature, and for such datastores
     * this setting is silently ignored.
     * @param lock whether to lock data or not
     */
    void setUseUpdateLock(boolean lock);

    /**
     * Configure isolation level for the transaction
     * Some datastores do not support Isolation Level feature, and for such datastores
     * this setting is silently ignored.
     * Calling this in the middle of a transaction has behaviour undetermined
     * @param isolation level
     */
    void setTransactionIsolation(int isolation);

    /**
     * A constant indicating that transactions are not supported. 
     */
    int TRANSACTION_NONE         = 0;

    /**
     * A constant indicating that
     * dirty reads, non-repeatable reads and phantom reads can occur.
     * This level allows a row changed by one transaction to be read
     * by another transaction before any changes in that row have been
     * committed (a "dirty read").  If any of the changes are rolled back, 
     * the second transaction will have retrieved an invalid row.
     */
    int TRANSACTION_READ_UNCOMMITTED = 1;

    /**
     * A constant indicating that
     * dirty reads are prevented; non-repeatable reads and phantom
     * reads can occur.  This level only prohibits a transaction
     * from reading a row with uncommitted changes in it.
     */
    int TRANSACTION_READ_COMMITTED   = 2;

    /**
     * A constant indicating that
     * dirty reads and non-repeatable reads are prevented; phantom
     * reads can occur.  This level prohibits a transaction from
     * reading a row with uncommitted changes in it, and it also
     * prohibits the situation where one transaction reads a row,
     * a second transaction alters the row, and the first transaction
     * rereads the row, getting different values the second time
     * (a "non-repeatable read").
     */
    int TRANSACTION_REPEATABLE_READ  = 4;

    /**
     * A constant indicating that
     * dirty reads, non-repeatable reads and phantom reads are prevented.
     * This level includes the prohibitions in
     * <code>TRANSACTION_REPEATABLE_READ</code> and further prohibits the 
     * situation where one transaction reads all rows that satisfy
     * a <code>WHERE</code> condition, a second transaction inserts a row that
     * satisfies that <code>WHERE</code> condition, and the first transaction
     * rereads for the same condition, retrieving the additional
     * "phantom" row in the second read.
     */
    int TRANSACTION_SERIALIZABLE     = 8;

}