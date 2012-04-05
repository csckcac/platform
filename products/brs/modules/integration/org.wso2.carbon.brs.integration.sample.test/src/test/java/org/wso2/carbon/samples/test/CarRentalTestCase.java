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
import org.wso2.carbon.samples.test.carRentalService.stub.CarRentalServiceStub;
import org.wso2.carbon.samples.test.carRentalService.reservation.CarReservation;
import org.wso2.carbon.samples.test.carRentalService.reservation.Charge;
import org.wso2.carbon.samples.test.carRentalService.reservation.Reservation;
import org.wso2.carbon.integration.framework.TestServerManager;
import org.wso2.carbon.utils.FileManipulator;
import org.testng.annotations.Test;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNotEquals;

import java.rmi.RemoteException;

public class CarRentalTestCase {

    @Test(groups = {"wso2.brs"})
    public void testCarRental() {
        try {
            CarRentalServiceStub carRentalServiceStub =
                    new CarRentalServiceStub("http://localhost:9763/services/CarRentalService");
            CarReservation carReservation = new CarReservation();
            Reservation reservation = new Reservation();
            reservation.setMPD(18.5);
            reservation.setType("weekly");
            Reservation[] reservations = new Reservation[1];
            reservations[0] = reservation;
            carReservation.setReserve(reservations);

            Charge[] charges = carRentalServiceStub.rent(reservations);
            String result = charges[0].getMessage();
            assertNotNull(result, "Result cannot be null");
            assertNotEquals(result, "");

        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();
            assertNotNull(null);
        } catch (RemoteException e) {
            e.printStackTrace();
            assertNotNull(null);
        }
    }
}
