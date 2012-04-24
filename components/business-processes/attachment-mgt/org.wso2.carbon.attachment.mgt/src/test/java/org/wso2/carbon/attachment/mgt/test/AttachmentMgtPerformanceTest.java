/*
 * Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.attachment.mgt.test;

import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.attachment.mgt.server.internal.AttachmentServerHolder;

/**
 * Intention of this test class is to cover concurrency/performance issues in this components.
 */
public class AttachmentMgtPerformanceTest extends TestCase {
    /**
     * Class Logger
     */
    private static Log log = LogFactory.getLog(AttachmentMgtPerformanceTest.class);
    protected MockAttachmentServer server;
    protected AttachmentServerHolder attachmentServerHolder;

    /**
     *
     */
    public void testConcurrencyThreshold() {
       //TODO: Impl this
        log.warn("Haven't imple this");
    }

    @Override
    protected void setUp() throws Exception {
        //Setup the MockAttachment-Server
        server = new MockAttachmentServer();

        attachmentServerHolder = AttachmentServerHolder.getInstance();
        attachmentServerHolder.setAttachmentServer(server);

        server.init();
    }

    @Override
    protected void tearDown() throws Exception {
        server.shutdown();
    }

}
