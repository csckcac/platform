package org.wso2.carbon.hosting.mgt.scheduler;

import java.util.concurrent.ThreadFactory;

class InstanceCheckThreadFactory implements ThreadFactory {
    private int counter = 0;

    public Thread newThread(Runnable r) {
        return new Thread(r, "InstanceCheckThread-" + counter++);
    }
}
