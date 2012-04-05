/*
 * Copyright WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.cloud.csg.transport.server;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.apache.axis2.AxisFault;
import org.apache.axis2.transport.base.threads.WorkerPool;
import org.apache.axis2.transport.base.threads.WorkerPoolFactory;
import org.wso2.carbon.cloud.csg.common.thrift.gen.Message;

import java.util.Map;
import java.util.concurrent.BlockingQueue;

public class CSGThriftServerHandlerTest extends TestCase {

    private CSGThriftServerHandler handler;

    private String token;

    public static final String QUEUE_NAME = "q1";

    @Override
    public void setUp() throws Exception {
        super.setUp();
        WorkerPool workerPool = WorkerPoolFactory.getWorkerPool(5, 100, 5, -1, "TestThread",
                "TestThreadID");
        handler = new CSGThriftServerHandler(workerPool);
        token = "uuid-123"; // dummy token for tests
        testAddNewRequestBuffer();
    }

    public void testAddRequestMessage() throws Exception {
        Message msg = new Message();
        try {
            handler.addRequestMessage(msg, token);
        } catch (AxisFault axisFault) {
            Assert.fail("Should not throw any exception since this is a " +
                    "valid token");
        }
    }

    public void testInvalidAddRequestMessage() throws Exception {
        Message msg = new Message();
        try {
            handler.addRequestMessage(msg, "1234123");
            Assert.fail("Should not allow to add invalid messages!");
        } catch (AxisFault axisFault) {
            // expected
        }
    }

    public void testGetRequestBuffer() throws Exception {
        BlockingQueue<Message> buffer = handler.getRequestBuffer(token);
        assertNotNull("There should be a buffer for this token", buffer);
    }

    public void testInvalidGetRquestBuffer() throws Exception {
        BlockingQueue<Message> buffer = handler.getRequestBuffer("12345");
        assertNull("There can not be a buffer for this token", buffer);
    }

    public void testInvalidGetSecureUUID() throws Exception {
        String actualToken = handler.getSecureUUID("In-valid-queue-name");
        assertNull("There can not be a token for this queue", actualToken);
    }

    public void testGetRequestBuffers() throws Exception {
        Map<String, BlockingQueue<Message>> buf = handler.getRequestBuffers();
        assertNotNull("There should be at least one request buffer", buf);
    }

    public void testAddNewRequestBuffer() throws Exception {
        try {
            handler.addNewRequestBuffer(token);
        } catch (Exception e) {
            Assert.fail("Should not fail to add a new request buffer since this is a valid token");
        }
    }
}
