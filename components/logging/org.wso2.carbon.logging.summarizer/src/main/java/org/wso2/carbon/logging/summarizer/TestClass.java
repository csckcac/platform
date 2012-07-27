package org.wso2.carbon.logging.summarizer;

import java.io.File;
import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: manisha
 * Date: 7/26/12
 * Time: 2:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class TestClass {
    public static void main(String[] args) throws Exception {
        /*Timer summaryTimer = new Timer(true);

        SummaryGeneratorTask summaryGeneratorTask = new SummaryGeneratorTask();
        // summaryTimer.scheduleAtFixedRate(summaryGeneratorTask, date, DEFAULT_SUMMARY_GEN_INTERVAL);
        summaryTimer.schedule(summaryGeneratorTask, 10, 10);*/

//        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
//
//        scheduler.scheduleAtFixedRate(new SummaryGeneratorTask(),10, 10, TimeUnit.MILLISECONDS );
        // Construct the file object. Does NOT create a file on disk!
//    File f = new File("/home/manisha/Desktop/logDir/logs/0/Application_Server/2012_07_27/000000_0.gz"); // backup of this source file.
//
//    // Rename the backup file to "junk.dat"
//    // Renaming requires a File object for the target.
//    f.renameTo(new File("/home/manisha/Desktop/logDir/logs/0/Application_Server/2012_07_27/abc.gz"));

        listPath(new File("/home/manisha/Desktop/logDir"));
    }



    static void listPath(File path) {
        File files[];

        files = path.listFiles();

        Arrays.sort(files);
        for (int i = 0, n = files.length; i < n; i++) {


            if (files[i].isDirectory()) {

                   listPath(files[i]);
               }
            if(files[i].toString().contains("000000_0")){
                String fileNameStr = files[i].toString();
                    System.out.println(fileNameStr);
//                File f = new File(fileNameStr);
//                f.renameTo(new File("/home/manisha/Desktop/logDir/logs/0/Application_Server/2012_07_27/abc.gz"));
            }


            }
        }

    }



