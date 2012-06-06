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
package org.wso2.carbon.samples;
import org.wso2.carbon.samples.orderApprovalService.order.OrderAccept;
import org.wso2.carbon.samples.orderApprovalService.order.PlaceOrder;
import org.wso2.carbon.samples.orderApprovalService.order.PlaceOrderE;
import org.wso2.carbon.samples.orderApprovalService.stub.OrderApprovalServiceCallbackHandler;
import org.wso2.carbon.samples.orderApprovalService.stub.OrderApprovalServiceStub;

public class PlaceOrderTestCase {

    public static void main(String[] args) {

        try {
            OrderApprovalServiceStub orderApprovalServiceStub =
                    new OrderApprovalServiceStub("http://localhost:9763/services/OrderApprovalService");

            PlaceOrderE placeOrderRequest = new PlaceOrderE();
            PlaceOrder placeOrder = new PlaceOrder();
            placeOrder.setPrice(2);
            placeOrder.setSymbol("IBM");
            placeOrder.setQuantity(22);
            placeOrderRequest.addOrder(placeOrder);
            PlaceOrder[] placeOrdersArray = new PlaceOrder[1];
            placeOrdersArray[0] = placeOrder;

            orderApprovalServiceStub.placeOrder(placeOrdersArray);

            OrderApprovalServiceCallbackHandler callback = new OrderApprovalServiceCallbackHandler() {

                public void receiveResultplaceOrder(
                        org.wso2.carbon.samples.orderApprovalService.order.PlaceOrderResponse result) {

                    OrderAccept[] orderAcceptList = result.getOrderAccept();
                    String acceptMessage = orderAcceptList[0].getMessage();
                    System.out.println(acceptMessage);

                    synchronized (this) {
                        this.notify();
                    }
                }

                public void receiveErrorapproveOrder(Exception e) {
                    e.printStackTrace();
                }
            };

            orderApprovalServiceStub.startplaceOrder(placeOrdersArray, callback);
            Thread.sleep(10000);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

