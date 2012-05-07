/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.analytics.hive.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.apache.hadoop.hive.common.LogUtils;
import org.apache.hadoop.hive.common.ServerUtils;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.service.HiveServer;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportFactory;
import org.wso2.carbon.analytics.hive.ServiceHolder;
import org.wso2.carbon.analytics.hive.impl.HiveExecutorServiceImpl;
import org.wso2.carbon.analytics.hive.service.HiveExecutorService;
import org.wso2.carbon.utils.CarbonUtils;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @scr.component name="bam.hive.component" immediate="true"
 */

public class HiveServiceComponent {

    private static final Log log = LogFactory.getLog(HiveServiceComponent.class);

    private static final String CARBON_HOME_ENV = "CARBON_HOME";

    private ServiceRegistration hiveServiceRegistration;

    private ExecutorService hiveServerPool = Executors.newSingleThreadExecutor();

    protected void activate(ComponentContext ctx) {

        if (log.isDebugEnabled()) {
            log.debug("Starting 'HiveServiceComponent'");
        }

        // Set CARBON_HOME if not already set for the use of Hive in order to load hive configurations.
        String carbonHome = System.getProperty(CARBON_HOME_ENV);
        if (carbonHome == null) {
            carbonHome = CarbonUtils.getCarbonHome();
            System.setProperty(CARBON_HOME_ENV, carbonHome);
        }

        hiveServerPool.submit(new HiveRunnable());

        // Set and register HiveExecutorService
        ServiceHolder.setHiveExecutorService(new HiveExecutorServiceImpl());

        hiveServiceRegistration = ctx.getBundleContext().registerService(
                HiveExecutorService.class.getName(),
                ServiceHolder.getHiveExecutorService(),
                null);

    }

    protected void deactivate(ComponentContext ctxt) {

        if (log.isDebugEnabled()) {
            log.debug("Stopping 'HiveServiceComponent'");
        }
        ctxt.getBundleContext().ungetService(hiveServiceRegistration.getReference());

    }

    public class HiveRunnable implements Runnable {

        public void run() {
            initialize();
        }

        public void initialize() {
            try {
                HiveServer.HiveServerCli cli = new HiveServer.HiveServerCli();

                cli.parse(null);

                // NOTE: It is critical to do this prior to initializing log4j, otherwise
                // any log specific settings via hiveconf will be ignored
                Properties hiveconf = cli.addHiveconfToSystemProperties();

                // NOTE: It is critical to do this here so that log4j is reinitialized
                // before any of the other core hive classes are loaded
/*                try {
                    LogUtils.initHiveLog4j();
                } catch (LogUtils.LogInitializationException e) {
                    HiveServer.HiveServerHandler.LOG.warn(e.getMessage());
                }*/

                HiveConf conf = new HiveConf(HiveServer.HiveServerHandler.class);
                ServerUtils.cleanUpScratchDir(conf);
                TServerTransport serverTransport = new TServerSocket(cli.port);

                // set all properties specified on the command line
                for (Map.Entry<Object, Object> item : hiveconf.entrySet()) {
                    conf.set((String) item.getKey(), (String) item.getValue());
                }

                HiveServer.ThriftHiveProcessorFactory hfactory =
                        new HiveServer.ThriftHiveProcessorFactory(null, conf);
                TThreadPoolServer.Args sargs = new TThreadPoolServer.Args(serverTransport)
                        .processorFactory(hfactory)
                        .transportFactory(new TTransportFactory())
                        .protocolFactory(new TBinaryProtocol.Factory())
                        .minWorkerThreads(cli.minWorkerThreads)
                        .maxWorkerThreads(cli.maxWorkerThreads);

                TServer server = new TThreadPoolServer(sargs);

                String msg = "Starting hive server on port " + cli.port
                             + " with " + cli.minWorkerThreads + " min worker threads and "
                             + cli.maxWorkerThreads + " max worker threads";

                HiveServer.HiveServerHandler.LOG.info(msg);

                // Start Hive Thrift service
                server.serve();

            } catch (Exception e) {
                log.error("Hive server initialization failed..", e);
            }
        }
    }
}
