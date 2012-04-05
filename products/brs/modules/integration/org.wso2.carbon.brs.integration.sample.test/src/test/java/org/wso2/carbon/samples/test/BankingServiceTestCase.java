/*
*  Licensed to the Apache Software Foundation (ASF) under one
*  or more contributor license agreements.  See the NOTICE file
*  distributed with this work for additional information
*  regarding copyright ownership.  The ASF licenses this file
*  to you under the Apache License, Version 2.0 (the
*  "License"); you may not use this file except in compliance
*  with the License.  You may obtain a copy of the License at
*
*   http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.wso2.carbon.samples.test;

import org.apache.axis2.AxisFault;
import org.wso2.carbon.samples.test.bankingService.stub.BankingServiceStub;
import org.wso2.carbon.samples.test.bankingService.deposit.Deposit;
import org.wso2.carbon.samples.test.bankingService.deposit.DepositAccept;
import org.wso2.carbon.samples.test.bankingService.deposit.DepositE;
import org.wso2.carbon.samples.test.bankingService.withdraw.*;
import org.wso2.carbon.integration.framework.TestServerManager;
import org.wso2.carbon.utils.FileManipulator;
import org.testng.annotations.Test;
import java.rmi.RemoteException;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNotEquals;

public class BankingServiceTestCase {

    @Test(groups = {"wso2.brs"})
    public void testBankingService() {

        try {
            BankingServiceStub bankingServiceStub =
                    new BankingServiceStub("http://localhost:9763/services/BankingService");

            bankingServiceStub._getServiceClient().getOptions().setManageSession(true);

            DepositE depositRequest = new DepositE();
            Deposit deposit = new Deposit();
            deposit.setAccountNumber("070229x");
            deposit.setAmount(1000);
            depositRequest.addDeposit(deposit);

            Deposit[] deposits = new Deposit[1];
            deposits[0] = deposit;
            depositRequest.setDeposit(deposits);


            DepositAccept[] results = bankingServiceStub.deposit(deposits);
            String result = results[0].getMessage();
            assertNotNull(result, "Result cannot be null");
            assertNotEquals(result, "");

            WithDrawE withDrawRequest = new WithDrawE();
            Withdraw withdraw = new Withdraw();
            withdraw.setAccountNumber("070229x");
            withdraw.setAmount(500);
            Withdraw[] withdraws = new Withdraw[1];
            withdraws[0] = withdraw;
            withDrawRequest.setWithDraw(withdraws);
            WithDrawRespone withDrawRespone = bankingServiceStub.withDraw(withdraws);
            WithdrawAccept[] withdrawAccepts = withDrawRespone.getWithdrawAccept();
            WithdrawReject[] withdrawRejects = withDrawRespone.getWithdrawReject();
            String resultWithDraw = withdrawAccepts[0].getMessage();
            assertNotNull(resultWithDraw, "Result cannot be null");
            assertNotEquals(resultWithDraw, "");


        } catch (AxisFault axisFault) {
                axisFault.printStackTrace();
            assertNotNull(null);

        } catch (RemoteException e) {
            e.printStackTrace();
            assertNotNull(null);
        }

    }
}
