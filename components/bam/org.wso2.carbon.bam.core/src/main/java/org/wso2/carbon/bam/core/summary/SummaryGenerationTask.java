/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.bam.core.summary;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import org.wso2.carbon.bam.common.dataobjects.mediation.MediationDataDO;
import org.wso2.carbon.bam.common.dataobjects.service.OperationDO;
import org.wso2.carbon.bam.common.dataobjects.service.ServerDO;
import org.wso2.carbon.bam.common.dataobjects.service.ServiceDO;
import org.wso2.carbon.bam.core.persistence.BAMPersistenceManager;
import org.wso2.carbon.bam.core.summary.generators.*;
import org.wso2.carbon.bam.core.util.BAMUtil;
import org.wso2.carbon.bam.util.BAMCalendar;
import org.wso2.carbon.bam.util.BAMException;

import java.util.List;
import java.util.TimerTask;

public class SummaryGenerationTask extends TimerTask {
    private static Log log = LogFactory.getLog(SummaryGenerationTask.class);

    private volatile boolean running=false;        // Indicates the task is currently running
    private boolean signalled=false;      // Sever is shutting down. So do not run this task again
    private BundleContext bundleContext;

    private long taskBreakDownLength;
    private long sleepTimeInBetweenTasks;

    public SummaryGenerationTask(BundleContext bundleContext, long taskBreakDownLength, long sleepTimeInBetweenTasks){
        this.bundleContext = bundleContext;

        if (taskBreakDownLength <= 0) {
            this.taskBreakDownLength = -1;
        }  else {
            this.taskBreakDownLength = taskBreakDownLength;
        }
        if (sleepTimeInBetweenTasks <= 0) {
            this.sleepTimeInBetweenTasks = -1;
        } else {
            this.sleepTimeInBetweenTasks = sleepTimeInBetweenTasks;
        }

    }

    public void run() {
        // If the this Timer has been signalled let Activator know the task is not running and return
//        if(signalled){
//            running = false;
//            return;
//        } else{
//            running = true;
//        }

        if (!running) {
            running = true;
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Task is already running. Skipping this iteration");
            }
            return;
        }

        try {
            BAMPersistenceManager bpm = BAMPersistenceManager.getPersistenceManager(BAMUtil.getRegistry());
            List<ServerDO> serverList;

            log.info("Running Summary Generator...");

//        try {
            //Get the server list of all tenants from database
            serverList = bpm.getMonitoredServers(-1);
//        } catch (BAMException e) {
//        	log.error("Failed retrieving server list.", e);
//        	return;    //no point continuing if server list is not there
//        }
            if (serverList != null) {
                for (ServerDO svr : serverList) {
                    generateServerSummaries(svr);
                    generateServiceStatSummaries(svr, bpm);
                    generateMediationStatsummaries(svr, bpm);
                    generateSummary(svr);
                }
                log.info("Summary generation successful ...");

            } else{
                log.info("No servers to generate summary ...");
            }
        } catch (Throwable t) {
            log.error("Server summary generation failed.", t);
        } finally {
            running = false;
        }

    }

    private void generateSummary(ServerDO svr){
        if (bundleContext == null) {
        	log.error("Unable to get bundle context. Cannot run summary generators");
            //Can't get the services
            return;
        }

        ServiceTracker tracker = new ServiceTracker(bundleContext,
                SummaryGeneratorFactory.class.getName(), null);
        tracker.open();

        Object[] services = tracker.getServices();
        if(services == null){
            //services are not registered. So, nothing to invoke
            return;
        }
        for (Object service : services) {
            for (int range: summaryRanges){
                SummaryGenerator sg =
                    ((SummaryGeneratorFactory) service).getSummaryGenerator(svr, range);
                try {
                    sg.generateSummary();
                } catch (Exception e) {
                	log.error(getSummaryRangeName(range) + " summary generation failed for server:" + svr.getServerURL(), e);
                }
            }
        }
        tracker.close();
    }

    private void generateMediationStatsummaries(ServerDO svr, BAMPersistenceManager bpm) {
    	try {
    		List<MediationDataDO> medDataList = bpm.getEndpoints(svr.getId());
    		for (MediationDataDO ep : medDataList) {
    			generateEndpointSummaries(svr, ep);
    		}
    	} catch (BAMException e) {
    		log.error("Failed retrieving endpoint list.", e);
    		//can't return because we have to do other summarizations
    	}

    	try {
    		List<MediationDataDO> medDataList = bpm.getSequences(svr.getId());
    		for (MediationDataDO seq : medDataList) {
    			generateSequenceSummaries(svr, seq);
    		}
    	} catch (BAMException e) {
    		log.error("Failed retrieving sequence list.", e);
    	}

    	try {
    		List<MediationDataDO> medDataList = bpm.getProxyServices(svr.getId());
    		for (MediationDataDO proxy : medDataList) {
    			generateProxyServiceSummaries(svr, proxy);
    		}
    	} catch (BAMException e) {
    		log.error("Failed retrieving proxy service list.", e);
    	}
    }


    private void generateServiceStatSummaries(ServerDO svr, BAMPersistenceManager bpm) {
        List<ServiceDO> serviceList;

        try {
            serviceList = bpm.getAllServices(svr.getId());
        } catch (BAMException e) {
        	log.error("Failed retrieving services list for server: " + svr.getServerURL(), e);
            return;
        }

        int i = 0;
        for (ServiceDO svc : serviceList) {
            generateServiceSummaries(svr, svc);

            List<OperationDO> operationList;
            try {
                operationList = bpm.getAllOperations(svc.getId());
            } catch (BAMException e) {
                log.error("Failed retrieving operation list for server: " + svr.getServerURL() +
                          " service:" + svc.getName(), e);
                continue; //Still we have to go through other services and finish it
            }

            for (OperationDO op : operationList) {
                generateOperationSummaries(svr, svc, op);
            }
            // run by chunks of task breakdown length
            if (log.isDebugEnabled()) {
                log.debug(i + " mod " + taskBreakDownLength + " = " + (i % taskBreakDownLength));
            }
            if ((this.taskBreakDownLength > 0) && (this.sleepTimeInBetweenTasks > 0)
                    && ((i % this.taskBreakDownLength) == 0)) {
                try {
                    log.debug("Summary exceeded task break down limit : " + taskBreakDownLength + ". Summary task will sleep for " + sleepTimeInBetweenTasks + " ms");
                    Thread.sleep(sleepTimeInBetweenTasks);
                } catch (InterruptedException e) {
                    // ignore
                }
            }
            i++;
        }
    }

    private void generateOperationSummaries(ServerDO svr, ServiceDO svc, OperationDO op) {

        for (int range : summaryRanges) {
            SummaryGenerator sg = new OperationSummaryGenerator(svr, svc, op, range);
            try {
                sg.generateSummary();
            } catch (Exception e) {
                log.error(getSummaryRangeName(range) + " summary generation failed for operation:" +
                        op.getName() + " of service: " + svc.getName() + " of server: " + svr.getServerURL(), e);
            }
        }
    }


    private void generateServiceSummaries(ServerDO svr, ServiceDO svc) {

        for (int range : summaryRanges) {
            SummaryGenerator sg = new ServiceSummaryGenerator(svr, svc, range);
            try {
                sg.generateSummary();
            } catch (Exception e) {
                log.error(getSummaryRangeName(range) + " summary generation failed for service: " +
                        svc.getName() + " of server: " + svr.getServerURL(), e);
            }
        }
    }

    private void generateServerSummaries(ServerDO svr) {
        for (int range : summaryRanges) {
            SummaryGenerator sg = new ServerSummaryGenerator(svr, range);
            try {
                sg.generateSummary();
            } catch (Exception e) {
                log.error(getSummaryRangeName(range) + " summary generation failed for server: " +
                        svr.getServerURL(), e);
            }
        }
    }

    private void generateEndpointSummaries(ServerDO svr, MediationDataDO endpoint) {

        for (int range : summaryRanges) {
            SummaryGenerator sg = new EndpointSummaryGenerator(svr, endpoint, range);
            try {
                sg.generateSummary();
            } catch (Exception e) {
                log.error(getSummaryRangeName(range) + " summary generation failed for endpoint:" + endpoint, e);
            }
        }
    }

    private void generateProxyServiceSummaries(ServerDO svr, MediationDataDO proxy) {
        for (int range : summaryRanges) {
            SummaryGenerator sg = new ProxyServiceSummaryGenerator(svr, proxy, range);
            try {
                sg.generateSummary();
            } catch (Exception e) {
                log.error(getSummaryRangeName(range) + " summary generation failed for proxy: " + proxy, e);
            }
        }
    }

    private void generateSequenceSummaries(ServerDO svr, MediationDataDO sequence) {

        for (int range : summaryRanges) {
            SummaryGenerator sg = new SequenceSummaryGenerator(svr, sequence, range);
            try {
                sg.generateSummary();
            } catch (Exception e) {
                log.error(getSummaryRangeName(range) + " summary generation failed for sequence:" +sequence, e);
            }
        }
    }

    private static String getSummaryRangeName(int range) {
        switch (range) {
            case BAMCalendar.HOUR_OF_DAY:
                return "Hourly";
            case BAMCalendar.DAY_OF_MONTH:
                return "Daily";
            case BAMCalendar.MONTH:
                return "Monthly";
            case BAMCalendar.QUATER:
                return "Quarterly";
            case BAMCalendar.YEAR:
                return "Yearly";
            default:
                throw new IllegalArgumentException("Unexpected summary range value");
        }
    }

    public boolean getRunningState(){
        return running;
    }

    public void setSignalledState(boolean signalled){
        this.signalled = signalled;
    }

    private static final int[] summaryRanges = new int[]{BAMCalendar.HOUR_OF_DAY, BAMCalendar.DAY_OF_MONTH,
            BAMCalendar.MONTH, BAMCalendar.QUATER, BAMCalendar.YEAR};

}
