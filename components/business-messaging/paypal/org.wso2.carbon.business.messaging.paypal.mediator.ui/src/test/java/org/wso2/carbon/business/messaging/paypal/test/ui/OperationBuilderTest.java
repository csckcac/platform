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
package org.wso2.carbon.business.messaging.paypal.test.ui;

import org.wso2.carbon.business.messaging.paypal.mediator.ui.Input;
import org.wso2.carbon.business.messaging.paypal.mediator.ui.Operation;
import org.wso2.carbon.business.messaging.paypal.mediator.ui.factory.OperationFactory;
import org.wso2.carbon.business.messaging.paypal.test.util.PaypalTestUtil;

import java.util.HashMap;
import java.util.List;

public class OperationBuilderTest extends BaseUITestCase {
    private Operation getBalanceOp;
    private Operation billUserOp;

    public void setUp() {
        super.setUp();
    }

    public void testSimpleOperationMetaData() {
        getBalanceOp = OperationFactory.getInstance().create("GetBalance");
        //test getBalance Operation meta data
        assertTrue(getBalanceOp.getName().equals("GetBalance"));
        assertTrue(getBalanceOp.getVersion().equals("2.0"));
        assertTrue(getBalanceOp.getCurrency().equals("US"));
        assertTrue(getBalanceOp.getInputs().size() == 2);
        assertTrue(getBalanceOp.getOutputs().size() == 0);
    }

    public void testSimpleOperationInputs() {
        getBalanceOp = OperationFactory.getInstance().create("GetBalance");
        //test getBalance Operation inputs
        HashMap map = new HashMap();

        Input detailInput = new Input();
        detailInput.setName("DetailLevel");
        detailInput.setRequired(true);
        map.put(detailInput.getName(), detailInput);

        Input errorLang = new Input();
        errorLang.setName("ErrorLanguage");
        errorLang.setRequired(true);
        map.put(errorLang.getName(), errorLang);

        List<Input> inputs = getBalanceOp.getInputs();
        PaypalTestUtil.validateSimpleInputs(map, inputs);

    }

    public void testComplexOperationMetaData() {
        billUserOp = OperationFactory.getInstance().create("BillUser");
        //test getBalance Operation meta data
        assertTrue(billUserOp.getName().equals("BillUser"));
        assertTrue(billUserOp.getVersion().equals("2.0"));
        assertTrue(billUserOp.getCurrency().equals("US"));
        assertTrue(billUserOp.getInputs().size() == 3);
        assertTrue(billUserOp.getOutputs().size() == 0);
    }

    public void testComplexOperationInputs() {
        billUserOp = OperationFactory.getInstance().create("BillUser");
        //test getBalance Operation inputs
        HashMap map = new HashMap();

        Input detailInput = new Input();
        detailInput.setName("DetailLevel");
        detailInput.setRequired(true);
        map.put(detailInput.getName(), detailInput);

        Input errorLang = new Input();
        errorLang.setName("ErrorLanguage");
        errorLang.setRequired(true);
        map.put(errorLang.getName(), errorLang);

        Input merchantPaymentDetails = new Input();
        merchantPaymentDetails.setName("MerchantPaymentDetails");
        merchantPaymentDetails.setRequired(true);
        HashMap inlineInputs = new HashMap();
        map.put(merchantPaymentDetails.getName(), inlineInputs);

        Input amount = new Input();
        amount.setName("Amount");
        amount.setRequired(true);
        HashMap amountInlineInputs = new HashMap();
        inlineInputs.put(amount.getName(), amountInlineInputs);

        Input currency = new Input();
        currency.setName("currencyID");
        currency.setRequired(true);
        amountInlineInputs.put(currency.getName(), currency);

        Input mpId = new Input();
        mpId.setName("MpId");
        mpId.setRequired(true);
        inlineInputs.put(mpId.getName(), mpId);

        Input tax = new Input();
        tax.setName("Tax");
        tax.setRequired(true);
        HashMap taxInlineInputs = new HashMap();
        inlineInputs.put(tax.getName(), taxInlineInputs);

        taxInlineInputs.put(currency.getName(), currency);

        Input shipping = new Input();
        shipping.setName("Shipping");
        shipping.setRequired(true);
        HashMap shippingInlineInputs = new HashMap();
        inlineInputs.put(shipping.getName(), shippingInlineInputs);

        shippingInlineInputs.put(currency.getName(), currency);

        Input handling = new Input();
        handling.setName("Handling");
        handling.setRequired(true);
        HashMap handlingInlineInputs = new HashMap();
        inlineInputs.put(handling.getName(), handlingInlineInputs);

        handlingInlineInputs.put(currency.getName(), currency);

        Input paymentType = new Input();
        paymentType.setName("PaymentType");
        inlineInputs.put(paymentType.getName(), paymentType);

        Input itemNumber = new Input();
        itemNumber.setName("ItemNumber");
        inlineInputs.put(itemNumber.getName(), itemNumber);

        Input itemName = new Input();
        itemName.setName("ItemName");
        inlineInputs.put(itemName.getName(), itemName);

        Input emailSubject = new Input();
        emailSubject.setName("EmailSubject");
        inlineInputs.put(emailSubject.getName(), emailSubject);

        Input memo = new Input();
        memo.setName("Memo");
        inlineInputs.put(memo.getName(), memo);

        List<Input> inputs = billUserOp.getInputs();
        PaypalTestUtil.validateComplexInputs(map, inputs);

    }

}
