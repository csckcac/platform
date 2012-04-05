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
package org.wso2.carbon.hdfs.dataaccess;

import org.apache.hadoop.conf.Configuration;

/**
 * Set HDFS Cluster Configuration
 */
public class ClusterConfiguration {

    private static final String FS_HDFS_IMPL = "org.apache.hadoop.hdfs.DistributedFileSystem";
    private static final String FS_DEFAUT_NAME = "hdfs://master:9000";
    private static final String HADOOP_SECURITY_AUTHENTICATION = "carbon";
    private static final String HADOOP_SECURITY_AUTHORIAZATION = "true";

    private static Configuration configuration = new Configuration(false);

    public static void setDefaultConfiguration() {
        configuration.set("fs.default.name", "hdfs://master:9000");
        //configuration.set("dfs.replication", "1");
        configuration.set("fs.hdfs.impl", "org.apache.hadoop.hdfs.DistributedFileSystem");
        //configuration.set("hadoop.security.authentication", "carbon"); //have to implement wso2 auth metod in server to support
        //configuration.set("hadoop.security.authorization", "true");
        //configuration.set("carbon.tenant.id", "carbon");
       // configuration.set("carbon.tenant.password", "carbon");

    }

    public static Configuration getDefaultConfiguration() {
        setDefaultConfiguration();
        return configuration;
    }

    //ClusterConfiguration has to read the config files and create the configuration
    //or use setters to set configs.

    public void setFsDefaultName(String fsDefaultName) {
        configuration.set("fs.default.name", fsDefaultName);
    }

    public void setFsHdfsImpl(String hdfsImpl) {
        configuration.set("fs.hdfs.impl", hdfsImpl);
    }

    public void setHadoopSecurityAuthentication(String securityAuthentication) {
        configuration.set("hadoop.security.authentication", securityAuthentication);
    }

    public void setHadoopSecurityAuthoriazation(String securityAuthoriazation) {
        configuration.set("hadoop.security.authorization", securityAuthoriazation);
    }

    public void setCarbonTenantId() {

    }

    public void setCarbonTenentPassword() {

    }

}
