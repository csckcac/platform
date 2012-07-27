/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/
package org.wso2.carbon.registry.subscription.test.util;

import java.util.Date;
import java.util.Hashtable;

import javax.management.MBeanServerConnection;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.utils.NetworkUtils;


public class JMXClient implements NotificationListener {

    private MBeanServerConnection mbsc = null;
    private static ObjectName nodeAgent;
    private static boolean success = false;
    private String path = "";

    private static final Log log = LogFactory.getLog(JMXClient.class);

    public void connect(String userName, String password) throws Exception {
        try {
            String RMIRegistryPort = "9999";
            String RMIServerPort = "11111";
            JMXServiceURL url = new JMXServiceURL("service:jmx:rmi://" +
                                                  NetworkUtils.getLocalHostname() + ":" + RMIServerPort +
                                                  "/jndi/rmi://" + NetworkUtils.getLocalHostname() + ":" +
                                                  RMIRegistryPort + "/jmxrmi");

            Hashtable<String, String[]> hashT = new Hashtable<String, String[]>();
            String[] credentials = new String[]{userName, password};
            hashT.put("jmx.remote.credentials", credentials);
            JMXConnector jmxc = JMXConnectorFactory.connect(url, hashT);
            mbsc = jmxc.getMBeanServerConnection();
            String connectsName = "org.wso2.carbon:Type=Registry,ConnectorName=Events";
            nodeAgent = new ObjectName(connectsName);
        } catch (Exception ex) {
            log.error("infoAdminServiceStub Initialization fail ");
            throw new Exception("infoAdminServiceStub Initialization fail " + ex.getMessage());
        }
    }

    public void registerNotificationListener(String pathName) throws Exception {
        path = pathName;
        try {
            mbsc.addNotificationListener(nodeAgent, this, null, null);
            log.info("Registered for event notifications");
        } catch (Exception e) {
            log.error("NotificationListener registration fail");
            throw new Exception("NotificationListener registration fail" + e.getMessage());
        }
    }

    public void handleNotification(Notification ntfyObj, Object handback) {
        log.info("***************************************************");
        log.info("* Notification received at " + new Date().toString());
        log.info("* type      = " + ntfyObj.getType());
        log.info("* message   = " + ntfyObj.getMessage());

        if (ntfyObj.getMessage().contains("at path " + path + " was updated.")) {
            setSuccess(true);
        }

        log.info("* seqNum    = " + ntfyObj.getSequenceNumber());
        log.info("* source    = " + ntfyObj.getSource());
        log.info("* seqNum    = " + Long.toString(ntfyObj.getSequenceNumber()));
        log.info("* timeStamp = " + new Date(ntfyObj.getTimeStamp()));
        log.info("* userData  = " + ntfyObj.getUserData());
        log.info("***************************************************");
    }


    public void getNotifications() throws InterruptedException {
        try {
            while (!isSuccess()) {
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            log.error("JMX notification listner interrupted");
            throw new InterruptedException("JMX notification listner interrupted" + e.getMessage());
        }
    }

    public static boolean isSuccess() {
        return success;
    }

    private static void setSuccess(boolean success) {
        JMXClient.success = success;
    }

}
