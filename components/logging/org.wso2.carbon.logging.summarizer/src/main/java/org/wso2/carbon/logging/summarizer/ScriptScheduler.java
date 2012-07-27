package org.wso2.carbon.logging.summarizer;

/*
 * Copyright 2005,2006 WSO2, Inc. http://www.wso2.org
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


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.hive.exception.HiveExecutionException;
import org.wso2.carbon.analytics.hive.impl.HiveExecutorServiceImpl;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;

public class ScriptScheduler {

    private static final Log log = LogFactory.getLog(ScriptScheduler.class);


    public static final long DEFAULT_INITIAL_SUMMARY_GEN_DELAY = 10; //10 minutes
    public static final long DEFAULT_SUMMARY_GEN_INTERVAL = 10; // one Day

    public ScriptScheduler() {
        System.out.println("Cons");
    }

    public void runSummarizer() throws Exception {
        System.out.println("came to summarizer");
        QueryGenerator queryGenerator = new QueryGenerator();
        queryGenerator.createQuery();
    }


    public void runScript(String script) throws Exception {

        HiveExecutorServiceImpl hiveExecutorService = new HiveExecutorServiceImpl();

        String driverName = "org.apache.hadoop.hive.jdbc.HiveDriver";
        String url = "jdbc:hive://localhost:10000/default";
        String userName = "admin";
        String password = "admin";
        //boolean connected = hiveExecutorService.setConnectionParameters(driverName, url, userName, password);

        //System.out.println(connected);

        try {
            hiveExecutorService.execute(script);
        } catch (HiveExecutionException e) {
            String msg = "Error while connecting to Hive service";
            log.error(msg, e);
            throw e;
        } finally {
            new ColumnFamilyHandler().deleteColumnFamilies();
            String outputFilePath = "/home/manisha/Desktop/logDir/";
                    File f = new File("/home/manisha/Desktop/logDir/logs/0/Application_Server/2012_07_27/000000_0.gz") ;
        }

    }

    public void invokeScheduleTask() {
        Timer summaryTimer = new Timer(true);
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 10);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        Date date = cal.getTime();
        SummaryGeneratorTask summaryGeneratorTask = new SummaryGeneratorTask();
        // summaryTimer.scheduleAtFixedRate(summaryGeneratorTask, date, DEFAULT_SUMMARY_GEN_INTERVAL);
        summaryTimer.schedule(summaryGeneratorTask, DEFAULT_INITIAL_SUMMARY_GEN_DELAY,
                DEFAULT_SUMMARY_GEN_INTERVAL);

    }

}

    

