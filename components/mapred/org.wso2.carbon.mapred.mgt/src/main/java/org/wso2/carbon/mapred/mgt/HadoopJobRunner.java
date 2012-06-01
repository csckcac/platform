package org.wso2.carbon.mapred.mgt;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.util.HashMap;
import java.util.Map;

import javax.activation.DataHandler;
import javax.servlet.http.HttpServletRequest;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.context.*;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.utils.ServerConstants;
import org.wso2.carbon.hadoop.security.HadoopCarbonMessageContext;
import org.wso2.carbon.hadoop.security.HadoopCarbonSecurity;

public class HadoopJobRunner extends AbstractAdmin {
	private Log log = LogFactory.getLog(HadoopJobRunner.class);
	public static final String HADOOP_CONFIG = System.getProperty(ServerConstants.CARBON_HOME)+File.separator+"repository"+File.separator+"conf"+File.separator+"etc"+File.separator+"hadoop.properties";
	public static final String REG_JAR_PATH = "/job/jar/";
	public static final String DEFAULT_HADOOP_JAR_PATH = ".";
	public static int DEFAULT_READ_LENGTH = 1024;
	
	private static final String MAPRED_SITE = "mapred-site.xml";
	private static final String CORE_SITE = "core-site.xml";
	private static final String HDFS_SITE = "hdfs-site.xml";
	private static final String HADOOP_POLICY = "hadoop-policy.xml";
	private static final String CAPACITY_SCHED = "cacpacity-scheduler.xml";
	private static final String MAPRED_QUEUE_ACLS = "mapred-queue-acls.xml";
	private static Configuration conf;
	
	static {
		conf = new Configuration();
		conf.addResource(new Path(HADOOP_CONFIG+File.separator+CORE_SITE));
        conf.addResource(new Path(HADOOP_CONFIG+File.separator+MAPRED_SITE));
        conf.addResource(new Path(HADOOP_CONFIG+File.separator+HDFS_SITE));
        conf.addResource(new Path(HADOOP_CONFIG+File.separator+HADOOP_POLICY));
        conf.addResource(new Path(HADOOP_CONFIG+File.separator+CAPACITY_SCHED));
        conf.addResource(new Path(HADOOP_CONFIG+File.separator+MAPRED_QUEUE_ACLS));
	}
	
	public void runJob(String jarName, String className, String args) {
		MessageContext msgCtx = MessageContext.getCurrentMessageContext();
		HttpServletRequest request = (HttpServletRequest) msgCtx.getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
		String cookie = request.getHeader(HTTPConstants.COOKIE_STRING);
		ConfigurationContext cfgCtx = msgCtx.getConfigurationContext();
		HadoopCarbonMessageContext hadoopMsgCtx = new HadoopCarbonMessageContext(cfgCtx, cookie);
		HadoopCarbonMessageContext.set(hadoopMsgCtx);
		HadoopJobRunnerThread hadoopJobThread = new HadoopJobRunnerThread(jarName, className, args);
		hadoopJobThread.start();
	}
	
	public void getJar(String jarPath) {
		CarbonContext cc = CarbonContext.getCurrentContext();
		Registry reg = cc.getRegistry(RegistryType.USER_CONFIGURATION);
		Resource resource = null;
		try {
			resource = reg.get(REG_JAR_PATH+jarPath);
		} catch (RegistryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		try {
			InputStream resIS = resource.getContentStream();
			//Write jarFile to default location
			String[] uriParts = jarPath.split("/");
			String jarFilePath = DEFAULT_HADOOP_JAR_PATH+"/"+uriParts[uriParts.length - 1];
			FileOutputStream fos = new FileOutputStream(jarFilePath);
			byte[] buffer = new byte[DEFAULT_READ_LENGTH];
			int readLen = 0;
			while ((readLen = resIS.read(buffer)) > -1) {
				fos.write(buffer, 0, readLen);
				buffer = new byte[DEFAULT_READ_LENGTH];
			}
			resIS.close();
			fos.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
	}
	
	public void putJar(String friendlyName, DataHandler dataHandler) {
		CarbonContext cc = CarbonContext.getCurrentContext();
		Registry reg = cc.getRegistry(RegistryType.USER_CONFIGURATION);
		try {
			if (reg.resourceExists(REG_JAR_PATH+friendlyName)) {
				log.info("Deleting already exsiting "+REG_JAR_PATH+friendlyName);
				reg.delete(REG_JAR_PATH+friendlyName);
			}
			Resource resource = reg.newResource();
			resource.setContentStream(dataHandler.getInputStream());
			String out = reg.put(REG_JAR_PATH+friendlyName, resource);
			log.info(out);
		} catch (RegistryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String[] getJarList() {
		CarbonContext cc = CarbonContext.getCurrentContext();
		Registry reg = cc.getRegistry(RegistryType.USER_CONFIGURATION);
		String sql1 = "SELECT REG_PATH_ID,REG_NAME FROM REG_RESOURCE WHERE REG_NAME LIKE ?";
		String[] paths = null;
		try {
			Resource query = reg.newResource();
			query.setContent(sql1);
			query.setMediaType(RegistryConstants.SQL_QUERY_MEDIA_TYPE);
	        query.addProperty(RegistryConstants.RESULT_TYPE_PROPERTY_NAME, RegistryConstants.RESOURCES_RESULT_TYPE);
	        reg.put(RegistryConstants.CONFIG_REGISTRY_BASE_PATH + RegistryConstants.QUERIES_COLLECTION_PATH + "/custom-queries", query);
	        Map parameters = new HashMap();
	        parameters.put("1", "%.jar");
	        Resource result = reg.executeQuery(RegistryConstants.CONFIG_REGISTRY_BASE_PATH + RegistryConstants.QUERIES_COLLECTION_PATH + "/custom-queries", parameters);
	        paths = (String[])result.getContent();
	        for (int i=0; i<paths.length; i++) {
	        	String[] subStrs = paths[i].split("/");
	        	paths[i] = subStrs[subStrs.length - 1];
	        }
	        result.discard();
		} catch (RegistryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return paths;
	}
	
	public static org.apache.hadoop.conf.Configuration getConf() {
		return conf;
	}
	
	public static void sanitizeConfiguration(Configuration conf) {
		//Clean everything related to hadoop.security.group.mapping and admin stuff
		conf.set("hadoop.security.group.mapping", "");
		conf.set("hadoop.security.group.mapping.service.url", "");
		conf.set("hadoop.security.admin.username", "");
		conf.set("hadoop.security.admin.password", "");
		//Clean all sensitive dfs details
		conf.set("dfs.name.dir", "");
		conf.set("dfs.name.edits.dir", "");
		conf.set("dfs.data.dir", "");
		conf.set("dfs.namenode.keytab.file", "");
		conf.set("dfs.namenode.kerberos.principal", "");
		conf.set("dfs.namenode.kerberos.https.principal", "");
		conf.set("dfs.secondary.namenode.keytab.file", "");
		conf.set("dfs.datanode.keytab.file", "");
		conf.set("dfs.datanode.kerberos.principal", "");
		//Clean all sensitive mapred details
		conf.set("mapred.system.dir", "");
		conf.set("mapreduce.jobtracker.kerberos.principal", "");
		conf.set("mapreduce.jobtracker.kerberos.https.principal", "");
		conf.set("mapreduce.jobtracker.keytab.file", "");
		conf.set("mapreduce.tasktracker.kerberos.principal", "");
		conf.set("mapreduce.tasktracker.kerberos.https.principal", "");
		conf.set("mapreduce.tasktracker.keytab.file", "");
		conf.set("mapreduce.tasktracker.group", "");
		conf.set("mapred.local.dir", "");
		conf.set("hadoop.log.dir", "");
		conf.set("mapred.tasktracker.carbon.proxy.user", "");
		conf.set("hadoop.job.history.user.location", "");
	}
}
