/**
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.bam.lwevent.core;

import java.util.concurrent.atomic.AtomicInteger;

public class BackOffCounter {
    private AtomicInteger currentBackOffCount = new AtomicInteger(0);
    private AtomicInteger totalBackOffCount = new AtomicInteger(0);
    private boolean failed = false;

    public boolean isFailed() {
        return failed;
    }

    public void setFailed(boolean failed) {
        this.failed = failed;
    }

    public AtomicInteger getCurrentBackOffCount() {
        return currentBackOffCount;
    }

    public void setCurrentBackOffCount(AtomicInteger currentBackOffCount) {
        this.currentBackOffCount = currentBackOffCount;
    }

    public AtomicInteger getTotalBackOffCount() {
        return totalBackOffCount;
    }

    public void setTotalBackOffCount(AtomicInteger totalBackOffCount) {
        this.totalBackOffCount = totalBackOffCount;
    }
}
