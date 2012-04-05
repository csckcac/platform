/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.multiple.instance.endpoint.mgt.autoscale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.multiple.instance.endpoint.mgt.PortSelector;

import java.util.*;
/* This class handle instance- startup and managing instance specifig utilities */
public class LocalInstanceManager{

    private static final Log log = LogFactory.getLog(LocalInstanceManager.class);

    private static final String IMPL_PREFIX = "impl.prefix";
    private static final String JAVA_ENDORSED_DIR = "java.endorsed.dirs";
    private static final String JAVA_IO_TMPDIR = "java.io.tmpdir";
    private static final String WSO2_SERVER_STANDALONE = "wso2.server.standalone";
    private static final String JAVA_COMMAND = "java.command";
    private static final String JAVA_OPTS = "java.opts";
    private static final String CARBON_XBOOTCLASSPATH = "carbon.xbootclasspath";
    private static final String CARBON_REGISTRY_ROOT = "carbon.registry.root";
    private static final String CARBON_CLASSPATH = "carbon.classpath";
    private static final String CARBON_HOME = "carbon.home";
    private static final String WSO2_CARBON_XML = "wso2.carbon.xml";
    private static final String WSO2_TRANSPORTS_XML = "wso2.transports.xml";
    private static final String JAVA_UTILS_LOGGING_CONFIG_FILE = "java.util.logging.config.file";
    private static final String CARBON_CONFIG_DIR_PATH = "carbon.config.dir.path";
    private static final String CARBON_LOGS_PATH = "carbon.logs.path";
    private static final String AXIS2_REPO = "axis2.repo";
    private static final String COMPONENTS_REPO = "components.repo";
    private static final String LAUNCHER_CLASS = "org.wso2.carbon.server.Main";
    private static final String WSO2_REGISTRY_XML = "wso2.registry.xml";
    private static final String WSO2_USER_MGT_XML = "wso2.user.mgt.xml";
    private static final String INSTANCE = "instance";
    private static final String INSTANCE_LOG_FILE = "instance.log";
    private static final String LOGS_PATH = "carbon.logs.path";
    public static String MASTER_NODE_INSTANCE_ID = null;
    private static Map<String, LocalInstance> instances;
    private static int startedInstanceNumber = 0;
    Runtime runtime = Runtime.getRuntime();
    Process MasterNode = null;

    /**
     *
     * @return
     * @throws Exception
     */
    public List start() throws Exception {
        PortSelector.init();
        LocalInstance instance = new LocalInstance();
        Process process = null;
        if(TestMasterNode()){
            process = runtime.exec(createCommand("false"));
        }else{
            process = runtime.exec(createCommand("true"));
            MasterNode = process;
        }
        StreamHandler s1 = new StreamHandler ("stdin", process.getInputStream ());
        StreamHandler s2 = new StreamHandler("stderr", process.getErrorStream ());
        s1.start ();
        s2.start ();
        instance.setJavaprocess(process);
        instances.put(UUID.randomUUID().toString(), instance);
        return null;
    }

    /**
     *  This method test whether master node is up and running or not
     *
     * @return true if master is running else return false
     */
    private boolean TestMasterNode(){
        if(MasterNode == null){
            return false;
        }
        try{
            int ExitValue = MasterNode.exitValue();
            if(ExitValue !=1){
                /* process has been killed due to some reason */
                return false;
            }
            /* Exception thrown during the start of the process, we do not consider it as a failure */
            return true;
        }catch(IllegalThreadStateException e){
            /* Mastser node is running successfully so return true */
             return true;
        }
    }

    /**
     *
     * @param instanceIds
     *
     * @return
     *
     * @throws Exception
     */
    public List terminate(String[] instanceIds) throws Exception {
        for (String instanceId : instanceIds) {
            Process process = (instances.get(instanceId)).getJavaprocess();
            process.destroy();
            // After destroying the process remove it from the list
            instances.remove(instanceId);
        }
        return null;
    }

    /**
     * describe the Instance
     *
     * @return
     *
     * @throws Exception
     */
    public List describeInstances() throws Exception {
        List<LocalInstance> instancelist = new ArrayList<LocalInstance>();
        Set keys = instances.keySet();
        String[] keyarray = (String[]) keys.toArray();
        for (String key : keyarray) {
            instancelist.add(instances.get(key));
        }
        return instancelist;
    }

    /**
     * This method create the command to start an instance which set different system porperties
     *
     * @param isMaster tell whether this is a command to start a master or a child
     *
     * @return command String to be used in Java.exec
     */
    public static String createCommand(String isMaster) {
        StringBuffer command = new StringBuffer("");
        command.append(getProperty(JAVA_COMMAND));
        command.append("-Xbootclasspath/a:");
        command.append(System.getProperty(CARBON_XBOOTCLASSPATH));
        command.append(" -Xms256m -Xmx512m -XX:MaxPermSize=128m ");
        command.append(getProperty(JAVA_OPTS));
        command.append(createSystemPropertyParam(IMPL_PREFIX));
        command.append("-Dcom.sun.management.jmxremote ");
        command.append("-classpath" + getProperty(CARBON_CLASSPATH));
        command.append(createSystemPropertyParam(JAVA_ENDORSED_DIR));
        command.append(createSystemPropertyParam(JAVA_IO_TMPDIR));
        command.append(createSystemPropertyParam(WSO2_SERVER_STANDALONE));
        command.append(createSystemPropertyParam(JAVA_COMMAND));
        command.append(createSystemPropertyParam(JAVA_OPTS));
        command.append(createSystemPropertyParam(CARBON_XBOOTCLASSPATH));
        command.append(createSystemPropertyParam(CARBON_REGISTRY_ROOT));
        command.append(createSystemPropertyParam(CARBON_CLASSPATH));
        command.append(createSystemPropertyParam(CARBON_HOME));
        command.append(createSystemPropertyParam(WSO2_CARBON_XML));
        command.append(createSystemPropertyParam(WSO2_REGISTRY_XML));
        command.append(createSystemPropertyParam(WSO2_USER_MGT_XML));
        command.append(createSystemPropertyParam(WSO2_TRANSPORTS_XML));
        command.append(createSystemPropertyParam(JAVA_UTILS_LOGGING_CONFIG_FILE));
        command.append(createSystemPropertyParam(CARBON_CONFIG_DIR_PATH));
        command.append(createSystemPropertyParam(CARBON_LOGS_PATH));
        command.append(createSystemPropertyParam(AXIS2_REPO));
        command.append(createSystemPropertyParam(COMPONENTS_REPO));
        command.append(createSystemPropertyParam(LOGS_PATH));
        /* This system property is used to recognize instance process to populate axisConfig
            to change the NIO transport ports in carbon.core
         */
        command.append(createSystemPropertyParam("master",isMaster));
        command.append(createSystemPropertyParam(INSTANCE, "true"));
        /* Set the system property to change the log file name for the starting instance */
        String instanceLog = getInstancelogFileName();
        command.append(createSystemPropertyParam(INSTANCE_LOG_FILE,instanceLog));
        String instanceId = UUID.randomUUID().toString();
        if("true".equals(isMaster)){
           instanceId = instanceId + "-master";
           MASTER_NODE_INSTANCE_ID = instanceId;
        }
        command.append(createSystemPropertyParam("instance.ID",instanceId));
        command.append(PortSelector.genaratePort(instanceId,instanceLog));
        command.append(LAUNCHER_CLASS);
        return command.toString();
    }


    private static String getProperty(String key) {
        if (System.getProperty(key) != null) {
            return " " + System.getProperty(key) + " ";
        }
        return "";
    }

    private static String createSystemPropertyParam(String key) {
        if (System.getProperty(key) != null) {
            return " -D" + key + "=" + System.getProperty(key) + " ";
        }
        return "";
    }

    private static String createSystemPropertyParam(String name, String value) {
        return " -D" + name + "=" + value + " ";
    }

    public Map<String, LocalInstance> getInstances() {
        return instances;
    }

    public void setInstances(Map<String, LocalInstance> instances) {
        LocalInstanceManager.instances = instances;
    }

    private static String getInstancelogFileName() {
        return "Instance" + Integer.toString(++startedInstanceNumber);
    }
}
