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

import junit.framework.TestCase;
import org.apache.axis2.AxisFault;
import org.apache.axis2.transport.base.threads.WorkerPool;
import org.apache.axis2.transport.base.threads.WorkerPoolFactory;

import java.net.URL;

public class CSGThriftServerTest extends TestCase {

    private CSGThriftServer server;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        WorkerPool workerPool = WorkerPoolFactory.getWorkerPool(5, 100, 5, -1, "TestThread",
                "TestThreadID");
        CSGThriftServerHandler handler = new CSGThriftServerHandler(workerPool);
        server = new CSGThriftServer(handler);

    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        if (server.isServerAlive()) {
            server.stop();
        }
    }

    public void testStart() throws Exception {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        URL url = cl.getResource("wso2carbon.jks");
        assertNotNull("KeyStore URL can not be null", url);
        server.start("localhost", 23003, 80, url.getPath(), "wso2carbon", "CSG-ThriftServer-test-thread");
    }

    public void testStop() throws Exception {
        server.stop();
    }
}
