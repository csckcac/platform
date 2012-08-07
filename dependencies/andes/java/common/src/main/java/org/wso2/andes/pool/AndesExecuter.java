package org.wso2.andes.pool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AndesExecuter {

    private static Log log = LogFactory.getLog(AndesExecuter.class);
    private static ExecutorService executorService = Executors.newFixedThreadPool(8);
    static {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        if (log.isDebugEnabled()) {
                            int workqueueSize = ((ThreadPoolExecutor) AndesExecuter.executorService).getQueue().size();
                            log.debug("AndesExecuter pool queue size " + workqueueSize);
                            Thread.sleep(30000);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public static Future<?> submit(Runnable job) {
        return executorService.submit(job);
    }
}
