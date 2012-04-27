/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/
package org.wso2.automation.common.test.dss.utils;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.wso2.automation.common.test.dss.utils.exception.ConcurrencyTestFailedError;
import org.wso2.automation.common.test.dss.utils.exception.ExceptionHandler;
import org.wso2.platform.test.core.utils.axis2client.AxisServiceClient;

public class ConcurrencyTest {
    private static final Log log = LogFactory.getLog(ConcurrencyTest.class);

    private int concurrencyNumber;
    private int numberOfIterations;

    public ConcurrencyTest(int threadGroup, int loopCount) {
        concurrencyNumber = threadGroup;
        numberOfIterations = loopCount;
    }

    public void run(final String serviceEndPoint, final OMElement payload,
                    final String operation)
            throws ConcurrencyTestFailedError, InterruptedException {
        log.info("Starting Concurrency test with " + concurrencyNumber + " Threads and " + numberOfIterations
                 + " loop count");
        ExceptionHandler handler = new ExceptionHandler();
        Thread[] clientThread = new Thread[concurrencyNumber];
        final AxisServiceClient serviceClient = new AxisServiceClient();
        for (int i = 0; i < concurrencyNumber; i++) {
            clientThread[i] = new Thread(new Runnable() {
                public void run() {
                    for (int j = 0; j < numberOfIterations; j++) {
                        try {
                            serviceClient.sendReceive(payload, serviceEndPoint, operation);
                        } catch (AxisFault axisFault) {
                            Assert.fail("AxisFault when getting response. " + axisFault.getMessage());
                        }
                    }
                }
            });
            clientThread[i].setUncaughtExceptionHandler(handler);

        }

        for (int i = 0; i < concurrencyNumber; i++) {
            clientThread[i].start();
        }

        for (int i = 0; i < concurrencyNumber; i++) {
            try {
                clientThread[i].join();
            } catch (InterruptedException e) {
                throw new InterruptedException("Exception Occurred while joining Thread");
            }
        }

        if (!handler.isTestPass()) {
            throw new ConcurrencyTestFailedError(handler.getFailCount() + " service invocation/s failed out of "
                                                 + concurrencyNumber * numberOfIterations + " service invocations.\n"
                                                 + "Concurrency Test Failed for Thread Group=" + concurrencyNumber
                                                 + " and loop count=" + numberOfIterations, handler.getException());
        }

    }
}
