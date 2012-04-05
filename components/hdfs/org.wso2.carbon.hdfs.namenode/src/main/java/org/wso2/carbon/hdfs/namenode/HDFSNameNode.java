/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.hdfs.namenode;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.server.namenode.NameNode;
import org.wso2.carbon.utils.ServerConstants;

import java.io.File;


/**
 * Activate and deactivate HDFS Name Node daemon.
 */
public class HDFSNameNode {
    private static Log log = LogFactory.getLog(HDFSNameNode.class);

    private static final String CORE_SITE_XML = "core-site.xml";
    private static final String HDFS_SITE_XML = "hdfs-site.xml";
    private static final String HADOOP_POLICY_XML = "hadoop-policy.xml";
    private static final String MAPRED_SITE_XML = "mapred-site.xml";


    private Thread thread;

    public HDFSNameNode() {
        Configuration conf = new Configuration(false);
        String carbonHome = System.getProperty(ServerConstants.CARBON_HOME);
        String hadoopCoreSiteConf = carbonHome + File.separator + "repository" + File.separator +
                                    "conf" + File.separator + "advanced" + File.separator + CORE_SITE_XML;
        String hdfsCoreSiteConf = carbonHome + File.separator + "repository" + File.separator +
                                  "conf" + File.separator + "advanced" + File.separator + HDFS_SITE_XML;
        String hadoopPolicyConf = carbonHome + File.separator + "repository" + File.separator +
                                  "conf" + File.separator + "advanced" + File.separator + HADOOP_POLICY_XML;
        String mapredSiteConf = carbonHome + File.separator + "repository" + File.separator +
                                "conf" + File.separator + "advanced" + File.separator + MAPRED_SITE_XML;


        conf.addResource(new Path(hadoopCoreSiteConf));
        conf.addResource(new Path(hdfsCoreSiteConf));
        conf.addResource(new Path(hadoopPolicyConf));
        conf.addResource(new Path(mapredSiteConf));


//        try{
//        org.wso2.carbon.user.api.UserRealm userRealm = realmService.getTenantUserRealm(CarbonContext.getCurrentContext().getTenantId());
//            System.out.println(userRealm.getUserStoreManager().getRoleNames());
//        }catch (UserStoreException e){
//
//        }

        try {

            //DefaultMetricsSystem.initialize("namenode");
            NameNode namenode = new NameNode(conf);
//            if (namenode != null) {
//                namenode.join();
//            }
        } catch (Throwable e) {
            log.error(e);
            //System.exit(-1);
        }


    }

    /**
     * Starts the Hadoop Name Node daemon
     */
    public void start() {
        thread = new Thread(new Runnable() {
            public void run() {
                if (log.isDebugEnabled()) {
                    log.debug("Activating the HDFS Name Node");
                }
                new HDFSNameNode();
                log.info("Hadoop Server Controller Thread was destroyed successfully");
            }
        }, "HDFSNameNode");

        try {
            Thread.sleep(40000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        thread.start();
    }

    /**
     * Stops the Hadoop Name Node daemon
     */
    public void shutdown() {
        if (log.isDebugEnabled()) {
            log.debug("Deactivating the HDFS Name Node");
        }
        try {
            thread.join();
        } catch (InterruptedException ignored) {
        }
    }


}
