/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.transaction.manager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

/**
 * @scr.component name="transactionmanager.component" immediate="true"
 * @scr.reference name="transactionmanager" interface="javax.transaction.TransactionManager"
 * cardinality="0..1" policy="dynamic" bind="setTransactionManager"  unbind="unsetTransactionManager"
 * @scr.reference name="usertransaction" interface="javax.transaction.UserTransaction"
 * cardinality="0..1" policy="dynamic" bind="setUserTransaction"  unbind="unsetUserTransaction"
 */
public class TransactionManagerComponent {

    private static Log log = LogFactory.getLog(TransactionManagerComponent.class);

    private static TransactionManager txManager;

    private static UserTransaction userTransaction;

    private static Object txManagerComponentLock = new Object(); /* class level lock for controlling synchronized access to static variables */

    protected void activate(ComponentContext ctxt) {
        log.debug("Transaction Manager bundle is activated ");
    }

    protected void deactivate(ComponentContext ctxt) {
        log.debug("Transaction Manager bundle is deactivated ");
    }

    protected void setTransactionManager(TransactionManager txManager) {
        synchronized (txManagerComponentLock) {
            if (log.isDebugEnabled()) {
                log.debug("Setting the Transaction Manager Service");
            }
            TransactionManagerComponent.txManager = txManager;
            try {
                InitialContext ctx = new InitialContext();
                Context javaCtx = null;
                try {
                    javaCtx = (Context) ctx.lookup("java:comp");
                } catch (NameNotFoundException ignore) {
                    //ignore
                }

                if (javaCtx == null) {
                    javaCtx = ctx.createSubcontext("java:comp");
                }

                javaCtx.bind("TransactionManager", txManager);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    protected void unsetTransactionManager(TransactionManager txManager) {
        synchronized (txManagerComponentLock) {
            if (log.isDebugEnabled()) {
                log.debug("Unsetting the Transaction Manager Service");
            }
            TransactionManagerComponent.txManager = null;
        }
    }

    public static TransactionManager getTransactionManager() {
        return txManager;
    }

    protected void setUserTransaction(UserTransaction userTransaction) {
        synchronized (txManagerComponentLock) {
            if (log.isDebugEnabled()) {
                log.debug("Setting the UserTransaction Service");
            }
            TransactionManagerComponent.userTransaction = userTransaction;
            try {
                InitialContext ctx = new InitialContext();
                Context javaCtx = null;

                try {
                    javaCtx = (Context) ctx.lookup("java:comp");
                } catch (NameNotFoundException ignore) {
                    //ignore
                }

                if (javaCtx == null) {
                    javaCtx = ctx.createSubcontext("java:comp");
                }

                javaCtx.bind("UserTransaction", userTransaction);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    protected void unsetUserTransaction(UserTransaction userTransaction) {
        synchronized (txManagerComponentLock) {
            if (log.isDebugEnabled()) {
                log.debug("Unsetting the UserTransaction Service");
            }
            TransactionManagerComponent.userTransaction = null;
        }
    }

    public static UserTransaction getUserTransaction() {
        return userTransaction;
    }

}
