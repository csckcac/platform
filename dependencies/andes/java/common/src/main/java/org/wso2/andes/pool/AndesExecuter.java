package org.wso2.andes.pool;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AndesExecuter {

    private static Log log = LogFactory.getLog(AndesExecuter.class);
    private static Map<String, PendingJob> pendingJobsTracker = new ConcurrentHashMap<String, PendingJob>();
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

    public static Future<?> submit(Runnable job, String channelId) {
        if(channelId != null){
            synchronized (pendingJobsTracker) {
                PendingJob pendingJob = pendingJobsTracker.get(channelId);
                if(pendingJob == null){
                    pendingJob = new PendingJob(); 
                    pendingJobsTracker.put(channelId, pendingJob);
                }
                pendingJob.submittedJobs = pendingJob.submittedJobs + 1;
            }
        }
        return executorService.submit(new RunnableWrapper(job, channelId));
    }

    public static class RunnableWrapper implements Runnable {
        Runnable runnable;
        String channelID;
        @Override
        public void run() {
            try {
                long start = System.currentTimeMillis();
                runnable.run();
                if(log.isDebugEnabled()){
                    log.debug(new StringBuffer().append("took ").append(runnable.getClass().getName())
                            .append(" ").append(System.currentTimeMillis() - start));
                }
            } finally{
                if(channelID != null){
                    synchronized (pendingJobsTracker) {
                        pendingJobsTracker.get(channelID).semaphore.release();
                    }
                }
            }
        }

        public RunnableWrapper(Runnable runnable, String channelID) {
            this.runnable = runnable;
            this.channelID = channelID;
        }
    }
    
    public static class PendingJob{
        Semaphore semaphore = new Semaphore(0); 
        int submittedJobs = 0;
    }
    
    
    public static void wait4JobsfromThisChannel2End(String channelId){
        PendingJob pendingJobs; 
        synchronized (pendingJobsTracker) {
            pendingJobs = pendingJobsTracker.get(channelId);
        }

        if(pendingJobs != null){
            try {
                pendingJobs.semaphore.tryAcquire(pendingJobs.submittedJobs, 1, TimeUnit.SECONDS);
                log.info("All "+ pendingJobs.submittedJobs + " completed for channel "+ channelId);
            } catch (InterruptedException e) {
                log.warn("Closing Channnel "+channelId+ "timedout waiting for submitted jobs to finish");
            }finally{
                synchronized (pendingJobsTracker) {
                    pendingJobsTracker.remove(channelId);
                }
            }
        }
    }
}
