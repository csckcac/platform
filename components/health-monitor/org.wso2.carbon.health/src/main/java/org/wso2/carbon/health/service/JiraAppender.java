/*
 *  Copyright (c) 2005-2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.health.service;

import org.apache.axiom.om.*;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.*;
import org.apache.log4j.spi.LoggingEvent;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.wso2.carbon.eventing.broker.exceptions.EventBrokerException;
import org.wso2.carbon.eventing.broker.utils.EventBrokerUtils;
import org.wso2.carbon.logging.service.data.LogMessage;
import org.wso2.carbon.logging.util.LoggingConstants;
import org.wso2.carbon.logging.appenders.MemoryAppender;
import org.wso2.eventing.Event;

import javax.activation.FileDataSource;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.util.*;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class JiraAppender extends AppenderSkeleton {
    private static Log log = LogFactory.getLog(JiraAppender.class);
    FileWriter systemInfoFileWriter;
    FileWriter osgiInfoFileWriter;
    FileWriter errorInfoFileWriter;
    FileWriter bundleInfoFileWriter;

    String systemInfoLog = "systemInfo.log";
    String osgiInfoLog = "osgiInfo.log";
    String errorInfoLog = "errorInfo.log";
    String bundleInfoLog = "bundleInfo.log";
    String logFileArray[];

    String zipFile = "jira-email-attachment.zip";
    int BUFFER_SIZE = 1024;

    String to = "heshan.ucsc@yahoo.com";
    String from = "carbon-health-monitor@wso2.org";
    String host = "mail2.wso2.org";

    protected void append(LoggingEvent loggingEvent) {
        if (loggingEvent.getLevel().toString().equalsIgnoreCase("ERROR")) {
            try {
                log.info("ERROR event found");

                // Get platform information from the jiraAppender
                getPlatformInfo();

                // Get OSGi context information and bundle information from carbon
                BundleContext bundleContext = HealthMonitorEventingServiceComponent.getBundleContext();
                getOSGiContextInfo(bundleContext);

                // Get Bundle information from OSGi framework
                //getBundleInfo(bundleContext);

                // Get log message details
                // TODO: uncomment the following lines
               /* LogMessage logMessage[] = getLogs();
                getLogMessage(logMessage);
                System.out.println("getLogMessage() executed ");*/

                // zip all log giles
                zipAllLogFiles(systemInfoLog, osgiInfoLog, errorInfoLog);
                System.out.println("zipAllLogFiles() executed ");

                // send log message via email
                //new SendEmail(to, from, host, zipFile);

                OMFactory fac = OMAbstractFactory.getOMFactory();
                OMNamespace omNs = fac.createOMNamespace("bar", "x");
                OMElement zipElement = fac.createOMElement("zipAttachment", omNs);

                javax.activation.DataHandler dataHandler = new javax.activation.DataHandler(new FileDataSource(zipFile));
                OMText textData = fac.createOMText(dataHandler, true);
                //User can set optimized to false by using the following
                //textData.doOptimize(false);
                zipElement.addChild(textData);
                Event<OMElement> event = new Event<OMElement>(zipElement);
                
                event.setTopic("/mail-sending/mail");
                OMElement topic = EventBrokerUtils.buildTopic(fac,event);
                EventBrokerUtils.generateEvent(event.getMessage(),topic,
                        HealthMonitorEventingServiceComponent.getHealthMonitorEventBrokerService());

            } /*catch (AxisFault axisFault) {
                axisFault.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }*/ catch (EventBrokerException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }

    /**
     * Prints symbolic names of all the bundles specified in the Bundle array
     * @param array array containing bundle information
     */
    private void printBundleArray(Bundle array[]) {
        int i;
        for (i = 0; i < array.length; i++) {
            System.out.println(array[i].getSymbolicName());
        }
    }

    /**
     * Write the log message to error information log
     * @param array array of log messages
     */
    private void getLogMessage(LogMessage array[]) {
        int i;
        try {
            errorInfoFileWriter = new FileWriter(errorInfoLog);
            for (i = 0; i < array.length; i++) {
                //System.out.println(array[i].getLogMessage());
                errorInfoFileWriter.write(array[i].getLogMessage());
                errorInfoFileWriter.close();
            }
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    /**
     * Zip all log files
     * @param file1 File that contains System information (OS,JVM,Thread dump)
     * @param file2 File that contains OSGi information (OSGi context information, bundle information)
     * @param file3 File that contains error information (stacktrace)
     */
    private void zipAllLogFiles(String file1, String file2, String file3) {
        try {
            // Reference to the file we will be adding to the zipfile
            BufferedInputStream origin;

            // Reference to our zip file
            FileOutputStream dest = new FileOutputStream(zipFile);

            // Wrap our destination zipfile with a ZipOutputStream
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));

            // Create a byte[] buffer that we will read data from the source
            // files into and then transfer it to the zip file
            byte[] data = new byte[BUFFER_SIZE];

            List<String> files = new ArrayList<String>();
            files.add(file1);
            files.add(file2);
            files.add(file3);
            //files.add(file4);

            // Iterate over all of the files in our list
            for (String filename : files) {
                // Get a BufferedInputStream that we can use to read the source file
                log.info("Adding: " + filename);
                FileInputStream fi = new FileInputStream(filename);
                origin = new BufferedInputStream(fi, BUFFER_SIZE);

                // Setup the entry in the zip file
                ZipEntry entry = new ZipEntry(filename);
                out.putNextEntry(entry);

                // Read data from the source file and write it out to the zip file
                int count;
                while ((count = origin.read(data, 0, BUFFER_SIZE)) != -1) {
                    out.write(data, 0, count);
                }

                // Close the source file
                origin.close();
            }

            // Close the zip file
            out.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * zip a give file
     */
    private void doZip() {
        try {
            byte[] buf = new byte[1024];
            FileInputStream fis = new FileInputStream(osgiInfoLog);
            fis.read(buf, 0, buf.length);

            CRC32 crc = new CRC32();
            ZipOutputStream s = new ZipOutputStream(
                    new FileOutputStream(zipFile));

            s.setLevel(6);

            ZipEntry entry = new ZipEntry(osgiInfoLog);
            entry.setSize((long) buf.length);
            crc.reset();
            crc.update(buf);
            entry.setCrc(crc.getValue());
            s.putNextEntry(entry);
            s.write(buf, 0, buf.length);
            s.finish();
            s.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Retreive bundle information and write it to bundle information log
     * @param bundleContext bundle context
     */
    private void getBundleInfo(BundleContext bundleContext) {
        try {
            bundleInfoFileWriter = new FileWriter(bundleInfoLog);
            Bundle[] bundles = bundleContext.getBundles();

            for (Bundle bundle : bundles) {
                bundleInfoFileWriter.write("Bundle Symbolic name : " + bundle.getSymbolicName() + "\n");
                bundleInfoFileWriter.write("Bundle ID            : " + bundle.getBundleId() + "\n");
                bundleInfoFileWriter.write("Bundle Location      : " + bundle.getLocation() + "\n");
                bundleInfoFileWriter.write("Bundle Version       : " + bundle.getVersion().toString() + "\n");
                bundleInfoFileWriter.write("Bundle State         : " + bundle.getState() + "\n\n");
            }
            bundleInfoFileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    /**
     * Retreive OSGi context information and write it to OSGi information log
     * @param bundleContext bundle context
     */
    private void getOSGiContextInfo(BundleContext bundleContext) {
        try {
            // Capturing OSGi framework information
            osgiInfoFileWriter = new FileWriter(osgiInfoLog);
            osgiInfoFileWriter.write("OSGi Framework Version    : " + bundleContext.getProperty(Constants.FRAMEWORK_VERSION) + "\n");
            osgiInfoFileWriter.write("OSGi Framework Language   : " + bundleContext.getProperty(Constants.FRAMEWORK_LANGUAGE) + "\n");
            osgiInfoFileWriter.write("OSGi Framework OS name    : " + bundleContext.getProperty(Constants.FRAMEWORK_OS_NAME) + "\n");
            osgiInfoFileWriter.write("OSGi Framework Vendor     : " + bundleContext.getProperty(Constants.FRAMEWORK_VENDOR) + "\n");
            osgiInfoFileWriter.write("OSGi Framework Preocessor : " + bundleContext.getProperty(Constants.FRAMEWORK_PROCESSOR) + "\n\n");

            // Capturing Bundle information
            Bundle[] bundles = bundleContext.getBundles();
            for (Bundle bundle : bundles) {
                osgiInfoFileWriter.write("Bundle Symbolic name : " + bundle.getSymbolicName() + "\n");
                osgiInfoFileWriter.write("Bundle ID : " + bundle.getBundleId() + "\n");
                osgiInfoFileWriter.write("Bundle Location : " + bundle.getLocation() + "\n");
                osgiInfoFileWriter.write("Bundle Version : " + bundle.getVersion().toString() + "\n");
                osgiInfoFileWriter.write("Bundle State : " + bundle.getState() + "\n");

                Dictionary headerDict = bundle.getHeaders();
                for (Enumeration e = headerDict.elements(); e.hasMoreElements();) {
                    osgiInfoFileWriter.write(e.nextElement() + "\n");
                }
                osgiInfoFileWriter.write("\n\n");
            }

            osgiInfoFileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    /**
     * Retreive platform information and write it to System information log.
     * The file will contain OS, JVM information, Thread dump
     */
    private void getPlatformInfo() {
        try {
            systemInfoFileWriter = new FileWriter(systemInfoLog);
            // Get a list of all filesystem roots on this system
            File[] roots = File.listRoots();

            systemInfoFileWriter.write("User                                              : " + System.getProperty("user.name") + ", " +
                    System.getProperty("user.language") + "-" + System.getProperty("user.country") +
                    ", " + System.getProperty("user.timezone") + "\n");

            for (File root : roots) {
                systemInfoFileWriter.write("File system root                                  : " + root.getAbsolutePath() + "\n");
                // TODO: uncomment the following lines after migrating to 1.5
                //systemInfoFileWriter.write("File system - Total space (bytes)                 : " + root.getTotalSpace() + "\n");
                //systemInfoFileWriter.write("File system - Free space (bytes)                  : " + root.getFreeSpace() + "\n");
                //systemInfoFileWriter.write("File system - Usable space (bytes)                : " + root.getUsableSpace() + "\n");
            }

            String carbonHome;
            if ((carbonHome = System.getProperty("carbon.home")).equals(".")) {
                carbonHome = new File(".").getAbsolutePath();
            }
            systemInfoFileWriter.write("Carbon Home                                       : " + carbonHome + "\n");

            systemInfoFileWriter.write("Java Home                                         : " + System.getProperty("java.home") + "\n");
            systemInfoFileWriter.write("Java Version                                      : " + System.getProperty("java.version") + "\n");
            systemInfoFileWriter.write("Java VM                                           : " + System.getProperty("java.vm.name") + " " +
                    System.getProperty("java.vm.version") +
                    "," +
                    System.getProperty("java.vendor") + "\n");
            systemInfoFileWriter.write("Java Temp Dir                                     : " + System.getProperty("java.io.tmpdir") + "\n");

            // Total amount of free memory available to the JVM
            systemInfoFileWriter.write("Java VM - Free memory (bytes)                     : " +
                    Runtime.getRuntime().freeMemory() + "\n");

            // This will return Long.MAX_VALUE if there is no preset limit
            long maxMemory = Runtime.getRuntime().maxMemory();
            // Maximum amount of memory the JVM will attempt to use
            systemInfoFileWriter.write("Java VM - Maximum memory (bytes)                  : " +
                    (maxMemory == Long.MAX_VALUE ? "no limit" : maxMemory) + "\n");

            // Total memory currently in use by the JVM
            systemInfoFileWriter.write("Java VM - Total memory (bytes)                    : " +
                    Runtime.getRuntime().totalMemory() + "\n");

            // Operating System MXBean
            systemInfoFileWriter.write("Operating system architecture                     : " + ManagementFactory.getOperatingSystemMXBean().getArch() + "\n");
            systemInfoFileWriter.write("Available processors                              : " + ManagementFactory.getOperatingSystemMXBean().getAvailableProcessors() + "\n");
            systemInfoFileWriter.write("Operating system name                             : " + ManagementFactory.getOperatingSystemMXBean().getName() + "\n");
            // TODO: uncomment the following lines after migrating to 1.5 
            //systemInfoFileWriter.write("System load average                               : " + ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage() + "\n");
            systemInfoFileWriter.write("Operating system version                          : " + ManagementFactory.getOperatingSystemMXBean().getVersion() + "\n");

            // Memory MXBean
            systemInfoFileWriter.write("Heap memory usage                                 : " + ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().toString() + "\n");
            systemInfoFileWriter.write("Non-heap memory usage                             : " + ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage().toString() + "\n");

            // Class loading MXBean
            systemInfoFileWriter.write("Loaded class count                                : " + ManagementFactory.getClassLoadingMXBean().getLoadedClassCount() + "\n");
            systemInfoFileWriter.write("Total loaded class count                          : " + ManagementFactory.getClassLoadingMXBean().getTotalLoadedClassCount() + "\n");
            systemInfoFileWriter.write("Unloaded class count                              : " + ManagementFactory.getClassLoadingMXBean().getUnloadedClassCount() + "\n");

            // Compilation MXBean
            systemInfoFileWriter.write("Just-in-time (JIT) compiler                       : " + ManagementFactory.getCompilationMXBean().getName() + "\n");
            systemInfoFileWriter.write("Total compilation time                            : " + ManagementFactory.getCompilationMXBean().getTotalCompilationTime() + "\n");

            // Garbage Collecttion
            /*systemInfoFileWriter.write("Garbage Collection  : " + ManagementFactory.getGarbageCollectorMXBeans());
            List gcBeans  =  ManagementFactory.getGarbageCollectorMXBeans();
            while(gcBeans.listIterator().hasNext()){
                systemInfoFileWriter.write(gcBeans.iterator().next().toString());
            }*/

            // TODO: do this for all MXBeans which returns a List
            while (!ManagementFactory.getMemoryManagerMXBeans().iterator().hasNext())
                systemInfoFileWriter.write("Memory Manager   : " + ManagementFactory.getMemoryManagerMXBeans().iterator().next().getName() + "\n");

            // Platform MBean
            systemInfoFileWriter.write("Default domain used for naming the MBean          : " + ManagementFactory.getPlatformMBeanServer().getDefaultDomain() + "\n");
            systemInfoFileWriter.write("Number of MBeans registered in the MBean server   : " + ManagementFactory.getPlatformMBeanServer().getMBeanCount() + "\n");
            systemInfoFileWriter.write("ClassLoaderRepository for this MBeanServer        : " + ManagementFactory.getPlatformMBeanServer().getClassLoaderRepository().toString() + "\n");

            // Runtime MXBean
            systemInfoFileWriter.write("Boot classpath                                    : " + ManagementFactory.getRuntimeMXBean().getBootClassPath() + "\n");
            //systemInfoFileWriter.write("Classpath                                         : " +ManagementFactory.getRuntimeMXBean().getClassPath()+ "\n");
            systemInfoFileWriter.write("Library path                                      : " + ManagementFactory.getRuntimeMXBean().getLibraryPath() + "\n");
            systemInfoFileWriter.write("Management specification version                  : " + ManagementFactory.getRuntimeMXBean().getManagementSpecVersion() + "\n");
            systemInfoFileWriter.write("Running Java virtual machine name                 : " + ManagementFactory.getRuntimeMXBean().getName() + "\n");
            systemInfoFileWriter.write("Java virtual machine specification name           : " + ManagementFactory.getRuntimeMXBean().getSpecName() + "\n");
            systemInfoFileWriter.write("Java virtual machine specification vendor         : " + ManagementFactory.getRuntimeMXBean().getSpecVendor() + "\n");
            systemInfoFileWriter.write("Java virtual machine specification version        : " + ManagementFactory.getRuntimeMXBean().getSpecVersion() + "\n");
            systemInfoFileWriter.write("start time of the JVM in milliseconds             : " + ManagementFactory.getRuntimeMXBean().getStartTime() + "\n");
            systemInfoFileWriter.write("uptime of the JVM in milliseconds                 : " + ManagementFactory.getRuntimeMXBean().getUptime() + "\n");
            systemInfoFileWriter.write("Java virtual machine implementation name          : " + ManagementFactory.getRuntimeMXBean().getVmName() + "\n");
            systemInfoFileWriter.write("Java virtual machine implementation vendor        : " + ManagementFactory.getRuntimeMXBean().getVmVendor() + "\n");
            systemInfoFileWriter.write("Java virtual machine implementation version       : " + ManagementFactory.getRuntimeMXBean().getVmVersion() + "\n");

            Map systemPropertyMap = ManagementFactory.getRuntimeMXBean().getSystemProperties();
            Iterator iterator = systemPropertyMap.keySet().iterator();
            systemInfoFileWriter.write("\n**System Properties**\n");
            while (iterator.hasNext()) {
                String key = iterator.next().toString();
                String value = systemPropertyMap.get(key).toString();
                systemInfoFileWriter.write(key + "  : " + value + "\n");
                //System.out.format(key + "  | %3d | " + value);
            }

            // Thread MXBEan
            systemInfoFileWriter.write("\n**Thread state**\n");
            systemInfoFileWriter.write("CPU time for the current thread in nanoseconds    : " + ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime() + "\n");
            systemInfoFileWriter.write("CPU time for current thread(user mode) nanoseconds: " + ManagementFactory.getThreadMXBean().getCurrentThreadUserTime() + "\n");
            systemInfoFileWriter.write("Current number of live daemon threads             : " + ManagementFactory.getThreadMXBean().getDaemonThreadCount() + "\n");
            systemInfoFileWriter.write("Peak live thread count since the JVM started      : " + ManagementFactory.getThreadMXBean().getPeakThreadCount() + "\n");
            systemInfoFileWriter.write("Number of live threads daemon and non-daemon      : " + ManagementFactory.getThreadMXBean().getThreadCount() + "\n");
            systemInfoFileWriter.write("Total number of threads created                   : " + ManagementFactory.getThreadMXBean().getTotalStartedThreadCount() + "\n");

            // TODO : uncomment the following lines after migrating to 1.5
            /*systemInfoFileWriter.write("\n**Thread dump**\n");
            ThreadInfo[] threadInfo = ManagementFactory.getThreadMXBean().dumpAllThreads(true, true);
            for (int i = 0; i < threadInfo.length; i++) {
                systemInfoFileWriter.write(i + "   :   " + threadInfo[i]);
            }*/
            systemInfoFileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Retuns information associated with the error. This information is retreived using the Carbon Memory Appender
     * @return log message array
     * @throws AxisFault axis fault
     */
    private LogMessage[] getLogs() throws AxisFault {
        int DEFAULT_NO_OF_LOGS = 100;
        int definedamount;
        Appender appender
                = Logger.getRootLogger().getAppender(LoggingConstants.WSO2CARBON_MEMORY_APPENDER);
        if (appender instanceof MemoryAppender) {
            MemoryAppender memoryAppender = (MemoryAppender) appender;
            if ((memoryAppender.getCircularQueue() != null)) {
                definedamount = memoryAppender.getBufferSize();
            } else {
                return new LogMessage[]{new LogMessage("--- No log entries found. " +
                        "You may try increasing the log level ---", "")};
            }

            Object[] objects;
            if (definedamount < 1) {
                objects = memoryAppender.getCircularQueue().getObjects(DEFAULT_NO_OF_LOGS);
            } else {
                objects = memoryAppender.getCircularQueue().getObjects(definedamount);
            }
            if ((memoryAppender.getCircularQueue().getObjects(definedamount) == null) ||
                    (memoryAppender.getCircularQueue().getObjects(definedamount).length == 0)) {
                return new LogMessage[]{new LogMessage("--- No log entries found. " +
                        "You may try increasing the log level ---", "")};
            }
            LogMessage[] logMessages = new LogMessage[objects.length];

            Layout layout = memoryAppender.getLayout();

            for (int i = 0; i < objects.length; i++) {
                LoggingEvent logEvt = (LoggingEvent) objects[i];
                if (logEvt != null) {
                    Level level = logEvt.getLevel();
                    logMessages[i] = new LogMessage(layout.format(logEvt), level.toString());
                }
            }
            return logMessages;
        } else {
            return new LogMessage[]{new LogMessage("The log must be configured to use the org.wso2.carbon." +
                    "logging.appenders.MemoryAppender to view entries on the admin console", "")};
        }
    }


    public boolean requiresLayout() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }


    public void close() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

}
